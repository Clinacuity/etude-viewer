package com.clinacuity.acv.controls;

import com.clinacuity.acv.controllers.ConfigurationController;
import javafx.beans.NamedArg;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AnnotationTypeDraggable extends StackPane {
    private static final Logger logger = LogManager.getLogger();

    @FXML private VBox targetBox;
    private Map<String, String> annotationMap = new HashMap<>();

    private String corpusType = "";
    public String getCorpusType() { return corpusType; }

    private boolean isSelected = false;

    public AnnotationTypeDraggable(@NamedArg("corpus") String corpus) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/AnnotationTypeDraggable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        corpusType = corpus;

        addAttribute();
        addAttribute();
        addAttribute();

        logger.error("Ok.");
        setOnDragDetected(event -> {
            if (!isSelected) {
                logger.debug("Drag started");
                ConfigurationController.draggableAnnotationCorpus = corpusType;
                ConfigurationController.draggedAnnotation = this;

                Dragboard dragboard = this.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putImage(new Image("/img/icons8/checklist.png"));
                dragboard.setContent(content);
            } else {
                logger.debug("This annotation is already displayed");
            }

            event.consume();
        });
    }

    public void addAttribute() {
        HBox attributeBox = new HBox();
        attributeBox.setSpacing(10.d);
        attributeBox.setPadding(new Insets(0, 5, 0, 5));
        attributeBox.setAlignment(Pos.TOP_CENTER);

        Label attributeName = new Label("My Name");
        attributeName.getStyleClass().add("text-small-normal");

        Label attributeValue = new Label("Value");
        attributeValue.getStyleClass().add("text-small-normal");

        attributeBox.getChildren().addAll(attributeName, attributeValue);

        targetBox.getChildren().add(attributeBox);
    }

    public void hide() {
        setOpacity(0.25d);
        isSelected = true;
    }

    public void show() {
        setOpacity(1.0d);
        isSelected = false;
    }
}
