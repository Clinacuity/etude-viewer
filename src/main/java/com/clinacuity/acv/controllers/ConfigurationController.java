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
import java.util.*;

public class ConfigurationController implements Initializable {
    private static final Logger logger = LogManager.getLogger();
    public static String draggableAnnotationCorpus = "";
    public static AnnotationTypeDraggable draggedAnnotation = null;

    @FXML private HBox mainBox;
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
        mainBox.widthProperty().addListener((obs, old, newValue) -> {
            double dragBoxWidth = (newValue.doubleValue() - 60.0d) / 5.0d;
            double dropBoxWidth = dragBoxWidth * 2.0d;

            systemDraggableBox.setMinWidth(dragBoxWidth);
            systemDraggableBox.setMaxWidth(dragBoxWidth);
            annotationDropBox.setMinWidth(dropBoxWidth);
            annotationDropBox.setMaxWidth(dropBoxWidth);
            referenceDraggableBox.setMinWidth(dragBoxWidth);
            referenceDraggableBox.setMaxWidth(dragBoxWidth);
        });
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

            systemDraggableTask = new CreateAnnotationDraggableTask(systemDirectoryTextField.getText(), "system");
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

            referenceDraggableTask = new CreateAnnotationDraggableTask(referenceDirectoryTextField.getText(), "reference");
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
        Map<String, List<AnnotationDropBox.Attribute>> annotationList = new HashMap<>();
        for (Node child : annotationDropBox.getChildren()) {
            AnnotationDropBox box = (AnnotationDropBox) child;
            if (box.isValid()) {
                annotationList.put(box.getName(), box.getAttributes());
            } else {
                validInputs = false;
            }
        }

        if (validInputs) {
            File directory = getSaveDirectory();
            if (directory != null) {
                saveTask = new SaveConfigurationTask(annotationList, directory);
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
}
