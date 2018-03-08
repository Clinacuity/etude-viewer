package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.controls.AnnotationDropBox;
import com.clinacuity.acv.controls.AnnotationTypeDraggable;
import com.clinacuity.acv.tasks.CreateAnnotationDraggableTask;
import com.clinacuity.acv.tasks.SaveConfigurationTask;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactfx.util.FxTimer;
import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ConfigurationBuilderController implements Initializable {
    private static final Logger logger = LogManager.getLogger();

    public static CorpusType draggableAnnotationCorpus;
    public static AnnotationTypeDraggable draggedAnnotation = null;

    // Main HBox and VBoxes
    @FXML private ScrollPane annotationScrollPane;
    @FXML private ScrollPane systemDragScrollPane;
    @FXML private ScrollPane referenceDragScrollPane;
    @FXML private VBox annotationDropBox;
    @FXML private VBox systemDraggableBox;
    @FXML private VBox referenceDraggableBox;
    @FXML private StackPane systemSpinner;
    @FXML private StackPane referenceSpinner;
    @FXML private JFXTextField systemDirectoryTextField;
    @FXML private JFXTextField referenceDirectoryTextField;

    @FXML private HBox addMatchButtonBox;

    private CreateAnnotationDraggableTask systemDraggableTask;
    private CreateAnnotationDraggableTask referenceDraggableTask;
    private SaveConfigurationTask saveTask;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        annotationScrollPane.widthProperty().addListener((obs, old, newValue) -> {
            // The subtraction comes from the padding of the <StackPane> and the width of the scrollbar
            double width = newValue.doubleValue() - 50.0d;

            annotationDropBox.setMinWidth(width);
            annotationDropBox.setMaxWidth(width);
            addMatchButtonBox.setMinWidth(width);
            addMatchButtonBox.setMaxWidth(width);
        });

        FxTimer.runLater(Duration.ofMillis(250), this::addAnnotationDropBox);
    }

    @FXML private void pickSystemCorpus() {
        if (systemDraggableTask != null && systemDraggableTask.isRunning()) {
            logger.warn("The System Corpus extraction task has been cancelled; a new directory will be picked");
            systemDraggableTask.cancel();
        }
        systemSpinner.setVisible(false);

        File systemCorpusDirectory = getDirectory("Choose directory with the system output");
        if (systemCorpusDirectory != null && systemCorpusDirectory.exists()) {
            systemDirectoryTextField.setText(systemCorpusDirectory.getAbsolutePath());

            systemDraggableTask = new CreateAnnotationDraggableTask(systemDirectoryTextField.getText(), CorpusType.SYSTEM);
            systemDraggableTask.setOnFailed(event -> {
                logger.throwing(systemDraggableTask.getException());
                systemSpinner.setVisible(false);
            });
            systemDraggableTask.setOnCancelled(event -> systemSpinner.setVisible(false));
            systemDraggableTask.setOnSucceeded(event -> {
                double width = systemDragScrollPane.getWidth() - 40.0d;
                List<AnnotationTypeDraggable> items = systemDraggableTask.getValue();
                items.forEach(item -> {
                    item.setMinWidth(width);
                    item.setMaxWidth(width);
                });

                systemSpinner.setVisible(false);
                systemDraggableBox.getChildren().clear();
                systemDraggableBox.getChildren().addAll(systemDraggableTask.getValue());
            });

            new Thread(systemDraggableTask).start();
            systemSpinner.setVisible(true);
        }
    }

    @FXML private void pickReferenceCorpus() {
        if (referenceDraggableTask != null && referenceDraggableTask.isRunning()) {
            logger.warn("The Reference Corpus extraction task has been cancelled; a new directory will be picked");
            referenceDraggableTask.cancel();
        }
        referenceSpinner.setVisible(false);

        File referenceCorpusDirectory = getDirectory("Choose directory with the reference corpus");
        if (referenceCorpusDirectory != null && referenceCorpusDirectory.exists()) {
            referenceDirectoryTextField.setText(referenceCorpusDirectory.getAbsolutePath());

            referenceDraggableTask = new CreateAnnotationDraggableTask(referenceDirectoryTextField.getText(), CorpusType.REFERENCE);
            referenceDraggableTask.setOnCancelled(event -> referenceSpinner.setVisible(false));
            referenceDraggableTask.setOnSucceeded(event -> {
                double width = referenceDragScrollPane.getWidth() - 40.0d;
                List<AnnotationTypeDraggable> items = referenceDraggableTask.getValue();
                items.forEach(item -> {
                    item.setMinWidth(width);
                    item.setMaxWidth(width);
                });

                referenceSpinner.setVisible(false);
                referenceDraggableBox.getChildren().clear();
                referenceDraggableBox.getChildren().addAll(items);
            });

            new Thread(referenceDraggableTask).start();
            referenceSpinner.setVisible(true);
        }
    }

    @FXML private void cancelSystemTask() {
        systemDraggableTask.cancel();
    }

    @FXML private void cancelReferenceTask() {
        referenceDraggableTask.cancel();
    }

    @FXML private void addAnnotationDropBox() {
        AnnotationDropBox dropBox = new AnnotationDropBox();
        annotationDropBox.setMinWidth(annotationScrollPane.getWidth() - 50.0d);
        annotationDropBox.setMaxWidth(annotationScrollPane.getWidth() - 50.0d);
        annotationDropBox.getChildren().add(dropBox);
    }

    @FXML private void saveConfigurations() {
        if (saveTask != null && saveTask.isRunning()) {
            saveTask.cancel();
        }

        if (areAnnotationMatchesValid()) {
            File directory = getSaveDirectory();

            if (directory != null) {
                startSaveTask(directory);
            }
        } else {
            logger.error("Invalid stuff -- display a message window here");
        }
    }

    private boolean areAnnotationMatchesValid() {
        for (Node child: annotationDropBox.getChildren()) {
            if (child instanceof AnnotationDropBox) {
                AnnotationDropBox box = (AnnotationDropBox)child;
                // TODO: there are no child cards here .. there are child BOXES
                if (!box.hasValidCards()) {
                    return false;
                }
            }
        }

        return true;
    }

    private void startSaveTask(File directory) {
        /*
        These structures represent the following:
        Annotation Parent Name (String)
            List of Map < Attribute Key , Attribute Value >
         */
        Map<String, List<Map<String, String>>> systemAnnotationMatches = new HashMap<>();
        Map<String, List<Map<String, String>>> referenceAnnotationMatches = new HashMap<>();

        for (Node child : annotationDropBox.getChildren()) {
            if (child instanceof AnnotationDropBox) {
                AnnotationDropBox box = (AnnotationDropBox)child;
                systemAnnotationMatches.put(box.getName(), box.getSystemCards());
                referenceAnnotationMatches.put(box.getName(), box.getReferenceCards());
            }
        }

        saveTask = new SaveConfigurationTask(systemAnnotationMatches, referenceAnnotationMatches, directory);
        saveTask.setOnSucceeded(event -> AcvContext.getInstance().contentLoading.setValue(false));
        saveTask.setOnFailed(event -> {
            logger.error("FAILED");
            AcvContext.getInstance().contentLoading.setValue(false);
        });
        AcvContext.getInstance().contentLoading.setValue(true);
        new Thread(saveTask).start();
    }

    private File getDirectory(String title) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);
        return chooser.showDialog(AcvContext.getMainWindow());
    }

    private File getSaveDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Test Dictionaries Folder");
        return directoryChooser.showDialog(AcvContext.getMainWindow());
    }

    public enum CorpusType {
        SYSTEM,
        REFERENCE
    }
}
