package com.clinacuity.acv.controls;

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
import java.util.List;
import java.util.ArrayList;

public class AnnotatedDocumentPane extends ScrollPane {
    public static final double STANDARD_INSET = 5.0d;
    private static final Logger logger = LogManager.getLogger();

    @FXML private AnchorPane anchor;
    private List<Label> labelList = new ArrayList<>();
    private List<AnnotationButton> buttonList = new ArrayList<>();
    private double characterHeight = -1.0;

    public AnchorPane getAnchor() { return anchor; }

    public AnnotatedDocumentPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/AnnotatedDocumentPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        arbitraryLabelsForSizeCalculations();
        FxTimer.runLater(Duration.ofMillis(100), this::getCharacterHeight);

        logger.debug("Annotated Document Pane initialized.");
    }

    private void arbitraryLabelsForSizeCalculations() {
        Label label = new Label("Loading . . .");
        labelList.add(label);
        label.getStyleClass().add("mono-text");
        anchor.getChildren().addAll(labelList);
    }

    public double getCharacterHeight() {
        if (characterHeight < 0.0d) {
            Label label = labelList.get(0);
            characterHeight = label.getHeight();
            logger.debug("Character Height set to {}", characterHeight);
        }
        return characterHeight;
    }

    public void addLabels(List<Label> labels) {
        anchor.getChildren().removeAll(labelList);
        labelList = labels;

        if (!labelList.isEmpty()) {
            anchor.getChildren().addAll(labelList);
        }
    }

    public void addButtons(List<AnnotationButton> buttons) {
        anchor.getChildren().removeAll(buttonList);
        buttonList = buttons;

        if (!buttonList.isEmpty()) {
            anchor.getChildren().addAll(buttonList);
        }
    }

    public List<Label> getLabelList() { return labelList; }

    public List<AnnotationButton> getAnnotationButtonList() { return buttonList; }
}
