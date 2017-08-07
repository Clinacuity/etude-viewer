package com.clinacuity.acv.controls;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactfx.util.FxTimer;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;

public class AnnotatedDocumentPane extends GridPane {
    private static final Logger logger = LogManager.getLogger();
    private static double characterHeight = -1.0d;
    private static double characterWidth = -1.0d;
    private static int maxCharactersPerLabel = -1;

    public static final double STANDARD_INSET = 10.0d;
    public static final double LINE_NUMBER_WIDTH = 30.0d;
    public static double getCharacterHeight() { return characterHeight; }
    public static int getMaxCharactersPerLabel() { return maxCharactersPerLabel; }

    @FXML private AnchorPane anchor;
    @FXML private ScrollPane document;
    @FXML private TextArea featureTree;
    private List<LineNumberedLabel> labelList = new ArrayList<>();
    private List<AnnotationButton> buttonList = new ArrayList<>();
    private double documentScrollWidth = -1.0d;

    public AnchorPane getAnchor() { return anchor; }
    public ScrollPane getScrollPane() { return document; }
    public TextArea getFeatureTreeText() { return featureTree; }
    public List<LineNumberedLabel> getLabelList() { return labelList; }
    public List<AnnotationButton> getAnnotationButtonList() { return buttonList; }

    public AnnotatedDocumentPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/AnnotatedDocumentPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        featureTree.setEditable(false);
        featureTree.setMouseTransparent(true);
        arbitraryLabelsForSizeCalculations();
        FxTimer.runLater(Duration.ofMillis(100), this::getCharacterDimensions);

        document.widthProperty().addListener(((observable, oldValue, newValue) ->
                documentScrollWidth = newValue.doubleValue()));

        logger.debug("Annotated Document Pane initialized.");
    }

    private void arbitraryLabelsForSizeCalculations() {
        labelList.add(new LineNumberedLabel("Loading . . .", 1));
        anchor.getChildren().addAll(labelList);
    }

    private void getCharacterDimensions() {
        LineNumberedLabel label = labelList.get(0);

        if (characterHeight < 0.0d) {
            characterHeight = label.getTextLabel().getHeight();
            logger.debug("Character Height set to {}", characterHeight);
        }

        if ( characterWidth < 0) {
            characterWidth = label.getWidth() / label.getLineText().length();
        }

        // the scroll panes may vary by a handful of pixels; take the smallest
        int maxChars = (int)(documentScrollWidth / characterWidth);
        if (maxCharactersPerLabel < maxChars) {
            maxCharactersPerLabel = maxChars;
            logger.error(maxCharactersPerLabel);
        }
    }

    public void addLineNumberedLabels(List<LineNumberedLabel> labels) {
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
}
