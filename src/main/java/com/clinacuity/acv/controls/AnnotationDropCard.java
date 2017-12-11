package com.clinacuity.acv.controls;

import com.clinacuity.acv.controllers.ConfigurationBuilderController;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

public class AnnotationDropCard extends StackPane {
    private static Logger logger = LogManager.getLogger();
    private static final Duration ANIMATION_DURATION = Duration.millis(200.0d);
    private static final double START_ROTATION = 0.0d;
    private static final double END_ROTATION = -90.0d;

    @FXML private VBox targetBox;
    private AnnotationTypeDraggable source;

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
        source.getAttributes().forEach(
                attribute -> targetBox.getChildren().addAll(createAttributeRow(attribute), new Separator()));

        // TODO
        if (getCorpusType() == ConfigurationBuilderController.CorpusType.SYSTEM) {
            // add system card background
        } else {
            // add reference card background
        }
    }

    private VBox createAttributeRow(String attribute) {
        /*
        VBox
            StackPane
                Label -- attribute name
                HBox
                    Label -- Lock
                    Label -- hide
            JFXTextField -- attribute value
         */

        VBox attributeRow = new VBox();
        attributeRow.setSpacing(5.0d);

        Label attributeLabel = new Label(attribute);
        attributeLabel.getStyleClass().add("text-medium-normal");

        HBox stackBox = new HBox();
        stackBox.setAlignment(Pos.CENTER_RIGHT);

        Label lockLabel = new Label();
        lockLabel.getStyleClass().add("button-lock");
        ImageView lockView = new ImageView(new Image("/img/icons8/lock.png"));
        lockView.setPreserveRatio(true);
        lockView.setFitHeight(16.0d);
        lockLabel.setGraphic(lockView);

        Label hideLabel = new Label();
        hideLabel.getStyleClass().add("button-delete");
        ImageView hideView = new ImageView(new Image("/img/icons8/delete.png"));
        hideView.setPreserveRatio(true);
        hideView.setFitHeight(16.0d);
        hideLabel.setGraphic(hideView);

        StackPane stack = new StackPane();
        stack.getChildren().addAll(attributeLabel, stackBox);

        JFXTextField attributeValue = new JFXTextField();
        attributeValue.setPromptText("attribute value");
        attributeValue.getStyleClass().add("text-medium-normal");

        attributeRow.getChildren().addAll(stack, attributeValue);
        return attributeRow;
    }

    ConfigurationBuilderController.CorpusType getCorpusType() { return source.getCorpusType(); }
}
