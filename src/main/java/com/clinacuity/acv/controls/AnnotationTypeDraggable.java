package com.clinacuity.acv.controls;

import com.clinacuity.acv.controllers.ConfigurationController;
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
import java.util.ArrayList;
import java.util.List;

public class AnnotationTypeDraggable extends StackPane {
    private static final Logger logger = LogManager.getLogger();

    @FXML private VBox targetBox;
    @FXML private Label annotationLabel;
    private String annotationName;
    private List<String> attributes = new ArrayList<>();
    private String xpath;

    private String corpusType = "";
    String getCorpusType() { return corpusType; }

    private boolean isSelected = false;

    public AnnotationTypeDraggable(String corpus, String name, List<String> attributeList) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/AnnotationTypeDraggable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        corpusType = corpus;
        annotationName = name;
        attributes = attributeList;

        initialize();
    }

    private void initialize() {
        setLabelName();

        setAttributes();

        setEvents();
    }

    private void setLabelName() {
        annotationLabel.setText(annotationName);
    }

    private void setAttributes() {
        for (String attribute: attributes) {
            addAttribute(attribute);
        }
    }

    private void setEvents() {
        setOnDragDetected(event -> {
            if (!isSelected) {
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

    private void addAttribute(String attribute) {
        HBox attributeBox = new HBox();
        attributeBox.setSpacing(10.d);
        attributeBox.setPadding(new Insets(0, 5, 0, 5));
        attributeBox.setAlignment(Pos.TOP_CENTER);

        Label attributeName = new Label(attribute);
        attributeName.getStyleClass().add("text-small-normal");

        attributeBox.getChildren().addAll(attributeName);

        targetBox.getChildren().add(attributeBox);
    }

    List<String> getAttributes() {
        return attributes;
    }

    void hide() {
        setOpacity(0.25d);
        isSelected = true;
    }

    void show() {
        setOpacity(1.0d);
        isSelected = false;
    }
}
