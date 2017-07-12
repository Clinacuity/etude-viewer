package com.clinacuity.acv.controls;

import com.clinacuity.acv.context.Annotations;
import com.clinacuity.acv.controllers.AnnotationComparisonViewController;
import com.clinacuity.acv.tasks.CreateButtonsTask;
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
    private AnnotationComparisonViewController parent;

    private double characterWidth = -1.0d;
    private double characterHeight = -1.0;

    private int callCount = 0;

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
    public void initialize(Annotations target, AnnotationComparisonViewController parentController) {
        annotationsJson = target;
        annotationKeys = annotationsJson.getAnnotationKeySet();

        parent = parentController;
        parent.notifyAnnotationSet(annotationKeys);

        arbitraryLabelsForSizeCalculations();
        FxTimer.runLater(Duration.ofMillis(100), this::addLabels);
    }

    private void addLabels() {
        if (characterWidth <= 0.0d) {
            getCharacterSizes();
        }

        anchor.getChildren().removeAll(labelList);
        labelList.clear();

        anchor.getChildren().clear();
        String[] lines = annotationsJson.getRawText().split("\n");
        logger.error(annotationsJson.getRawText().substring(91, 97));

        double offset = 0.0d;
        for (String line: lines) {
            Label label = new Label(line);
            label.getStyleClass().clear();
            label.getStyleClass().add("label-mono");
            AnchorPane.setTopAnchor(label, offset);
            labelList.add(label);
            anchor.getChildren().add(label);
            offset += (characterHeight * 2.0d);
        }
    }

    private void arbitraryLabelsForSizeCalculations() {
        Label label = new Label("hhh");
        labelList.add(label);
        label.getStyleClass().add("label-mono");
        anchor.getChildren().addAll(labelList);
    }

    private void getCharacterSizes() {
        Label label = labelList.get(0);
        characterHeight = label.getHeight();
        characterWidth = (label.getWidth() / label.getText().length());
        logger.debug("Character Sizes set to: {} x {}", characterWidth, characterHeight);
    }

    // TODO clear buttons
    public void resetButtons(String key) {
        clearButtons();
        List<JsonObject> annotations = annotationsJson.getAnnotationsByKey(key);

        // TODO: bind some progress property AND the value or succeed event to extract buttons list
        CreateButtonsTask task = new CreateButtonsTask(annotations, labelList, characterWidth, characterHeight);
        task.setOnSucceeded(event -> {
            buttonList = task.getValue();
            logger.error("{} buttons successfully added", buttonList.size());
            anchor.getChildren().addAll(buttonList);

            for (AnnotationButton button: buttonList) {
                logger.error(button.getTranslateX());
            }
        });
        task.setOnFailed(event -> logger.throwing(task.getException()));

        new Thread(task).start();
        callCount++;
        logger.error(callCount);
    }

    public void clearButtons() {
        anchor.getChildren().removeAll(buttonList);
    }
}
