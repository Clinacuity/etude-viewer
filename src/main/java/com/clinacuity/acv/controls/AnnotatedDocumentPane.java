package com.clinacuity.acv.controls;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class AnnotatedDocumentPane extends VBox {
    private static final Logger logger = LogManager.getLogger();

    public static final double CHARACTER_HEIGHT = 16.0d;
    public static final int MAX_CHARACTERS_PER_LABEL = 50;

    public static final double STANDARD_INSET = 10.0d;
    public static final double LINE_NUMBER_WIDTH = 24.0d;

    @FXML private AnchorPane anchor;
    @FXML private ScrollPane document;
    @FXML private TextArea featureTree;
    private List<LineNumberedLabel> labelList = new ArrayList<>();
    private List<AnnotationButton> buttonList = new ArrayList<>();

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
        arbitraryLabelsForSizeCalculations();

        logger.debug("Annotated Document Pane initialized.");
    }

    private void arbitraryLabelsForSizeCalculations() {
        labelList.add(new LineNumberedLabel("Select a document from the side bar. . .", 1));
        anchor.getChildren().addAll(labelList);
    }

    public void addLineNumberedLabels(List<LineNumberedLabel> labels) {
        anchor.getChildren().clear();
        labelList = labels;
        buttonList = new ArrayList<>();

        if (labelList != null && !labelList.isEmpty()) {
            anchor.getChildren().addAll(labelList);
        }
    }

    public void addButtons(List<AnnotationButton> buttons) {
        anchor.getChildren().clear();
        anchor.getChildren().addAll(labelList);

        buttonList = buttons;

        if (buttonList != null && !buttonList.isEmpty()) {
            anchor.getChildren().addAll(buttonList);
        }
    }

    public void reset() {
        if (labelList == null) {
            labelList = new ArrayList<>();
        } else {
            labelList.clear();
        }

        if (buttonList == null) {
            buttonList = new ArrayList<>();
        } else {
            buttonList.clear();
        }

        anchor.getChildren().clear();
        clearFeatureTree();
    }

    public void clearFeatureTree() {
        featureTree.clear();
    }
}
