package com.clinacuity.acv.controls;

import com.clinacuity.acv.controllers.ConfigurationBuilderController;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.List;

public class AnnotationDropCard extends StackPane {
    private static Logger logger = LogManager.getLogger();
    private static final Duration ANIMATION_DURATION = Duration.millis(200.0d);
    private static final double START_ROTATION = 0.0d;
    private static final double END_ROTATION = -90.0d;

    @FXML private Label cardLabel;
    @FXML private VBox targetBox;
    private AnnotationTypeDraggable source;
    private List<String> attributes;

    public AnnotationDropCard(AnnotationTypeDraggable draggableSource) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/AnnotationDropCard.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        source = draggableSource;
        initialize();
    }

    private void initialize() {
        cardLabel.setText(source.getLabelName());

        attributes = source.getAttributes();

        checkBeginEndAttribtues();

        source.getAttributes().forEach(attribute -> createAttributeRow(attribute, "", true));

        // TODO
        if (getCorpusType() == ConfigurationBuilderController.CorpusType.SYSTEM) {
            setStyle("-fx-background-color: rgba(70, 130, 180, 0.2);");
        } else {
            setStyle("-fx-background-color: rgba(255, 140, 0, 0.2);");
        }
    }

    private void checkBeginEndAttribtues() {
        if (attributes.contains("begin")) {
            attributes.remove("begin");
            createAttributeRow("begin", "Begin Attr", false);
        } else {
            if (attributes.contains("start")) {
                attributes.remove("start");
                createAttributeRow("start", "Begin Attr", false);
            }
        }

        if (attributes.contains("end")) {
            attributes.remove("end");
            createAttributeRow("end", "End Attr", false);
        } else {
            attributes.remove("finish");
            createAttributeRow("finish", "End Attr", false);
        }

        if (attributes.contains("XPath")) {
            attributes.remove("XPath");
            createAttributeRow("XPath", "XPath", false);
        }
    }

    private void createAttributeRow(String attribute, String initialValue, boolean includeButtons) {
        /*
        VBox
            StackPane
                Label -- attribute name
                HBox
                    Label -- Lock
                    Label -- hide
            JFXTextField -- attribute value
         */
        Label attributeLabel = new Label(attribute);
        attributeLabel.getStyleClass().add("text-medium-normal");
        attributeLabel.setAlignment(Pos.CENTER_LEFT);

        Label lockLabel = new Label();
        lockLabel.getStyleClass().add("button-lock");
        ImageView lockView = new ImageView(new Image("/img/icons8/lock.png"));
        lockView.setPreserveRatio(true);
        lockView.setFitHeight(16.0d);
        lockLabel.setGraphic(lockView);
        lockLabel.setOnMouseClicked(event -> logger.error("OK LOCK CLICKED"));

        Label hideLabel = new Label();
        hideLabel.getStyleClass().add("button-delete");
        ImageView hideView = new ImageView(new Image("/img/icons8/delete.png"));
        hideView.setPreserveRatio(true);
        hideView.setFitHeight(16.0d);
        hideLabel.setGraphic(hideView);
        hideLabel.setOnMouseClicked(event -> logger.error("OK HIDE CLICKED"));

        JFXTextField attributeValue = new JFXTextField(initialValue);
        attributeValue.setPromptText("attribute value");
        attributeValue.getStyleClass().add("text-medium-normal");

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(3.0d);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(lockLabel, hideLabel);
        buttonBox.setPadding(new Insets(3.0d, 3.0d, 0.0d, 0.0d));

        StackPane stack = new StackPane();
        stack.setAlignment(Pos.CENTER_LEFT);
        if (includeButtons) {
            stack.getChildren().addAll(attributeLabel, buttonBox);
        } else {
            stack.getChildren().add(attributeLabel);
        }

        VBox attributeRow = new VBox();
        attributeRow.setSpacing(3.0d);
        attributeRow.setPadding(new Insets(5.0d));
        attributeRow.getChildren().addAll(stack, attributeValue);

        hideLabel.setOnMouseClicked(event -> targetBox.getChildren().remove(attributeRow));

        targetBox.getChildren().add(attributeRow);
    }

    ConfigurationBuilderController.CorpusType getCorpusType() { return source.getCorpusType(); }
}
