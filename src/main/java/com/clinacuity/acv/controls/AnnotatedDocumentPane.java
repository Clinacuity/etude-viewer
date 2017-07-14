package com.clinacuity.acv.controls;

import com.clinacuity.acv.context.Annotations;
import com.clinacuity.acv.tasks.CreateButtonsTask;
import com.clinacuity.acv.tasks.GetLabelsFromDocumentTask;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactfx.util.FxTimer;
import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;


public class AnnotatedDocumentPane extends ScrollPane {
    private static final Logger logger = LogManager.getLogger();

    @FXML private AnchorPane anchor;
    private Annotations annotationsJson;
    private List<Label> labelList = new ArrayList<>();
    private List<AnnotationButton> buttonList = new ArrayList<>();
    private Set<String> annotationKeys = new HashSet<>();
    private GetLabelsFromDocumentTask getLabelsTask;

    private double characterHeight = -1.0;

    public AnnotatedDocumentPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/AnnotatedDocumentPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }
    }

    /**
     * This method will (re)initialize the Document Pane with new text, labels, annotations, etc.
     * without re-initializing the top-level UI controls for it
     */
    public void initialize(Annotations target) {
        annotationsJson = target;
        annotationKeys = annotationsJson.getAnnotationKeySet();

        // TODO: refactor!
//        parent.notifyAnnotationSet(annotationKeys);

        arbitraryLabelsForSizeCalculations();
        FxTimer.runLater(Duration.ofMillis(100), this::addLabels);

        logger.debug("Annotated Document Pane initialized.");
    }

    private void addLabels() {
        if (characterHeight <= 0.0d) {
            getCharacterHeight();
        }

        if (getLabelsTask != null && getLabelsTask.isRunning()) {
            getLabelsTask.cancel();
        }

        labelList.clear();
        anchor.getChildren().clear();

        String[] lines = annotationsJson.getRawText().split("\n");
        getLabelsTask = new GetLabelsFromDocumentTask(lines, characterHeight);
        getLabelsTask.setOnSucceeded(event -> {
            labelList = getLabelsTask.getValue();
            anchor.getChildren().addAll(labelList);
        });
        getLabelsTask.setOnCancelled(event -> logger.warn("Get Labels Task has been cancelled -- starting a new one."));
        new Thread(getLabelsTask).start();
    }

    private void arbitraryLabelsForSizeCalculations() {
        Label label = new Label("Loading . . .");
        labelList.add(label);
        label.getStyleClass().add("mono-text");
        anchor.getChildren().addAll(labelList);
    }

    private void getCharacterHeight() {
        Label label = labelList.get(0);
        characterHeight = label.getHeight();
        logger.debug("Character Height set to {}", characterHeight);
    }

    // TODO clear buttons
    public void resetButtons(String key) {
        clearButtons();
        List<JsonObject> annotations = annotationsJson.getAnnotationsByKey(key);

        // TODO: bind some progress property AND the value or succeed event to extract buttons list
        CreateButtonsTask task = new CreateButtonsTask(annotations, labelList, characterHeight);
        task.setOnSucceeded(event -> {
            buttonList = task.getValue();
            logger.error("{} buttons successfully added", buttonList.size());
            anchor.getChildren().addAll(buttonList);
        });
        task.setOnFailed(event -> logger.throwing(task.getException()));
        new Thread(task).start();
    }

    public void clearButtons() {
        anchor.getChildren().removeAll(buttonList);
    }
}
