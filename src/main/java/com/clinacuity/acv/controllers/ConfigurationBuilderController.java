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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactfx.util.FxTimer;
import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.*;

public class ConfigurationBuilderController implements Initializable {
    private static final Logger logger = LogManager.getLogger();
    public static CorpusType draggableAnnotationCorpus;
    public static AnnotationTypeDraggable draggedAnnotation = null;

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

    private CreateAnnotationDraggableTask systemDraggableTask;
    private CreateAnnotationDraggableTask referenceDraggableTask;
    private SaveConfigurationTask saveTask;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addAnnotationDropBox();
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
                referenceSpinner.setVisible(false);
                referenceDraggableBox.getChildren().clear();
                referenceDraggableBox.getChildren().addAll(referenceDraggableTask.getValue());
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
        annotationDropBox.getChildren().add(dropBox);
        FxTimer.runLater(Duration.ofMillis(100), () -> annotationScrollPane.setVvalue(1.0d));
    }

    @FXML private void saveConfigurations() {
        if (saveTask != null && saveTask.isRunning()) {
            saveTask.cancel();
        }

        boolean validInputs = true;
        Map<String, List<AnnotationDropBox.Attribute>> systemAnnotationList = new HashMap<>();
        Map<String, List<String>> systemXPathsPerMatch = new HashMap<>();
        Map<String, List<AnnotationDropBox.Attribute>> referenceAnnotationList = new HashMap<>();
        Map<String, List<String>> referenceXPathsPerMatch = new HashMap<>();

        for (Node child : annotationDropBox.getChildren()) {
            AnnotationDropBox box = (AnnotationDropBox) child;
            if (box.isValid()) {
                if (box.hasSystemAttributes()) {
                    systemAnnotationList.put(box.getName(), box.getAttributes());
                }
                if (box.hasReferenceAttributes()) {
                    referenceAnnotationList.put(box.getName(), box.getAttributes());
                }

                systemXPathsPerMatch.put(box.getName(), box.getSystemXPaths());
                referenceXPathsPerMatch.put(box.getName(), box.getReferenceXPaths());
            } else {
                validInputs = false;
            }
        }

        if (validInputs) {
            File directory = getSaveDirectory();
            if (directory != null) {
                saveTask = new SaveConfigurationTask(
                        systemAnnotationList,
                        systemXPathsPerMatch,
                        referenceAnnotationList,
                        referenceXPathsPerMatch,
                        directory);
                saveTask.setOnSucceeded(event -> {
                    logger.error("succeeded");
                    AcvContext.getInstance().contentLoading.setValue(false);
                });
                saveTask.setOnFailed(event -> {
                    logger.error("FAILED");
                    AcvContext.getInstance().contentLoading.setValue(false);
                });
                AcvContext.getInstance().contentLoading.setValue(true);
                new Thread(saveTask).start();
            } else {
                logger.warn("No valid directory chosen -- cancelling task.");
            }
        } else {
            logger.warn("Something went wrong with empty names or unique naming");
        }
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
