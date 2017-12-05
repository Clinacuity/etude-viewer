package com.clinacuity.acv.controls;

import com.clinacuity.acv.controllers.ConfigurationBuilderController;
import com.clinacuity.acv.tasks.CreateAnnotationDraggableTask;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnnotationTypeDraggable extends StackPane {
    private static final Logger logger = LogManager.getLogger();
    private static final String HIGHLIGHT_CLASS = "draggable-highlight";
    private static final Duration ANIMATION_DURATION = Duration.millis(200.0d);
    private static final double START_ROTATION = 0.0d;
    private static final double END_ROTATION = -90.0d;

    @FXML private VBox targetBox;
    @FXML private VBox collapsibleContentBox;
    @FXML private Label annotationLabel;
    @FXML private Label collapseButton;
    @FXML private Pane collapsiblePane;
    private String annotationName;
    private List<String> attributes = new ArrayList<>();
    private double expandedHeight = 0.0d;
    private boolean isCollapsed = false;
    private Timeline collapsingTimeline = new Timeline();

    private String xpath;
    String getXPath() { return xpath; }

    private ConfigurationBuilderController.CorpusType corpusType;
    ConfigurationBuilderController.CorpusType getCorpusType() { return corpusType; }

    private boolean isSelected = false;

    public AnnotationTypeDraggable(ConfigurationBuilderController.CorpusType corpus, CreateAnnotationDraggableTask.XmlParsedAnnotation parsedAnnotation) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/AnnotationTypeDraggable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        corpusType = corpus;
        annotationName = parsedAnnotation.name;
        xpath = parsedAnnotation.xpath;
        attributes = parsedAnnotation.attributes;

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
                ConfigurationBuilderController.draggableAnnotationCorpus = corpusType;
                ConfigurationBuilderController.draggedAnnotation = this;

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
        collapsibleContentBox.getChildren().add(attributeBox);
    }

    List<String> getAttributes() {
        return attributes;
    }

    void setHighlight(boolean set) {
        if (set) {
            targetBox.getStyleClass().add(HIGHLIGHT_CLASS);
        } else {
            targetBox.getStyleClass().remove(HIGHLIGHT_CLASS);
        }
    }

    void hide() {
        setOpacity(0.25d);
        isSelected = true;
    }

    void show() {
        setOpacity(1.0d);
        isSelected = false;
    }

    @FXML private void hideCard() {
        if (!collapsingTimeline.getStatus().equals(Animation.Status.RUNNING)) {
            collapsiblePane.setMinHeight(collapsiblePane.getHeight());
            collapsiblePane.setMaxHeight(collapsiblePane.getHeight());
            targetBox.setMinHeight(targetBox.getHeight());
            targetBox.setMaxHeight(targetBox.getHeight());

            KeyValue minHeightKeysPane;
            KeyValue maxHeightKeysPane;
            KeyValue minHeightKeysBox;
            KeyValue maxHeightKeysBox;
            KeyValue rotationTarget;

            if (isCollapsed) {
                minHeightKeysPane = new KeyValue(collapsiblePane.minHeightProperty(), expandedHeight);
                maxHeightKeysPane = new KeyValue(collapsiblePane.maxHeightProperty(), expandedHeight);
                minHeightKeysBox = new KeyValue(targetBox.minHeightProperty(), expandedHeight);
                maxHeightKeysBox = new KeyValue(targetBox.maxHeightProperty(), expandedHeight);
                rotationTarget = new KeyValue(collapseButton.rotateProperty(), START_ROTATION);
                collapsingTimeline.setOnFinished(event -> collapsibleContentBox.setVisible(true));
            } else {
                collapsibleContentBox.setVisible(false);
                expandedHeight = getHeight();

                minHeightKeysPane = new KeyValue(collapsiblePane.minHeightProperty(), annotationLabel.getHeight() * 2.0d);
                maxHeightKeysPane = new KeyValue(collapsiblePane.maxHeightProperty(), annotationLabel.getHeight() * 2.0d);
                minHeightKeysBox = new KeyValue(targetBox.minHeightProperty(), annotationLabel.getHeight() * 2.0d);
                maxHeightKeysBox = new KeyValue(targetBox.maxHeightProperty(), annotationLabel.getHeight() * 2.0d);
                rotationTarget = new KeyValue(collapseButton.rotateProperty(), END_ROTATION);
                collapsingTimeline.setOnFinished(null);
            }

            KeyFrame heightFrame = new KeyFrame(ANIMATION_DURATION,
                    minHeightKeysPane, maxHeightKeysPane, minHeightKeysBox, maxHeightKeysBox, rotationTarget);

            collapsingTimeline.stop();
            collapsingTimeline.getKeyFrames().clear();
            collapsingTimeline.getKeyFrames().add(heightFrame);
            collapsingTimeline.play();
            isCollapsed = !isCollapsed;
        }
    }
}
