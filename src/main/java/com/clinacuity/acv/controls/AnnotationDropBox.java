package com.clinacuity.acv.controls;

import com.clinacuity.acv.controllers.ConfigurationBuilderController;
import com.clinacuity.acv.modals.WarningModal;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class AnnotationDropBox extends StackPane {
    private static Logger logger = LogManager.getLogger();
    private static final Duration ANIMATION_DURATION = Duration.millis(200.0d);
    private static final double START_ROTATION = 0.0d;
    private static final double END_ROTATION = -90.0d;


    @FXML private HBox annotationNameBox;
    @FXML private VBox collapsibleBox;
    @FXML private VBox systemDropCards;
    @FXML private VBox referenceDropCards;
    @FXML private Pane collapsiblePane;
    @FXML private HBox collapsibleContentBox;
    @FXML private JFXTextField matchNameTextField;
    @FXML private Label collapseButton;

    private List<AnnotationDropCard> systemSources = new ArrayList<>();
    private List<AnnotationDropCard> referenceSources = new ArrayList<>();
    private List<String> systemOptions = new ArrayList<>();
    private List<String> referenceOptions = new ArrayList<>();
    private double expandedHeight = 0.0d;
    private boolean isCollapsed = false;
    private Timeline collapsingTimeline = new Timeline();

    private void initialize() {
        setOnDragOver(event -> {
            if (ConfigurationBuilderController.draggedAnnotation != null) {
                event.acceptTransferModes(TransferMode.ANY);
            }
            event.consume();
        });

        setOnDragDropped(event -> addSource());

        widthProperty().addListener((obs, old, newValue) -> {
            double width = newValue.doubleValue() / 2.0d - 30.0d;
            systemDropCards.setMinWidth(width);
            systemDropCards.setMaxWidth(width);
            referenceDropCards.setMinWidth(width);
            referenceDropCards.setMaxWidth(width);
        });

        systemOptions.add("");
        referenceOptions.add("");
    }

    private void addSource() {
        if (ConfigurationBuilderController.draggedAnnotation != null) {
            AnnotationTypeDraggable draggable = ConfigurationBuilderController.draggedAnnotation;

            AnnotationDropCard card = new AnnotationDropCard(draggable, this);
            if (card.getCorpusType() == ConfigurationBuilderController.CorpusType.SYSTEM) {
                systemSources.add(card);
                for (String attribute : ConfigurationBuilderController.draggedAnnotation.getAttributes()) {
                    if (!systemOptions.contains(attribute)) {
                        systemOptions.add(attribute);
                    }
                }
                systemDropCards.getChildren().add(card);
            } else {
                referenceSources.add(card);
                for (String attribute : ConfigurationBuilderController.draggedAnnotation.getAttributes()) {
                    if (!referenceOptions.contains(attribute)) {
                        referenceOptions.add(attribute);
                    }
                }
                referenceDropCards.getChildren().add(card);
            }
        }
    }

    @FXML private void collapseBox() {
        if (!collapsingTimeline.getStatus().equals(Animation.Status.RUNNING)) {
            collapsiblePane.setMinHeight(collapsiblePane.getHeight());
            collapsiblePane.setMaxHeight(collapsiblePane.getHeight());
            collapsibleBox.setMinHeight(collapsibleBox.getHeight());
            collapsibleBox.setMaxHeight(collapsibleBox.getHeight());

            KeyValue minHeightKeysPane;
            KeyValue maxHeightKeysPane;
            KeyValue minHeightKeysBox;
            KeyValue maxHeightKeysBox;
            KeyValue rotationTarget;

            if (isCollapsed) {
                minHeightKeysPane = new KeyValue(collapsiblePane.minHeightProperty(), expandedHeight);
                maxHeightKeysPane = new KeyValue(collapsiblePane.maxHeightProperty(), expandedHeight);
                minHeightKeysBox = new KeyValue(collapsibleBox.minHeightProperty(), expandedHeight);
                maxHeightKeysBox = new KeyValue(collapsibleBox.maxHeightProperty(), expandedHeight);
                rotationTarget = new KeyValue(collapseButton.rotateProperty(), START_ROTATION);
                collapsingTimeline.setOnFinished(event -> {
                    collapsibleContentBox.setVisible(true);
                    matchNameTextField.setEditable(true);
                });
            } else {
                collapsibleContentBox.setVisible(false);
                matchNameTextField.setEditable(false);
                expandedHeight = getHeight();

                minHeightKeysPane = new KeyValue(collapsiblePane.minHeightProperty(), annotationNameBox.getHeight() * 2.0d);
                maxHeightKeysPane = new KeyValue(collapsiblePane.maxHeightProperty(), annotationNameBox.getHeight() * 2.0d);
                minHeightKeysBox = new KeyValue(collapsibleBox.minHeightProperty(), annotationNameBox.getHeight() * 2.0d);
                maxHeightKeysBox = new KeyValue(collapsibleBox.maxHeightProperty(), annotationNameBox.getHeight() * 2.0d);
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

    @FXML private void removeBox() {
        VBox parent = (VBox)getParent();
        parent.getChildren().remove(this);
    }

    void removeCard(AnnotationDropCard card) {
        if (systemSources.contains(card)) {
            systemSources.remove(card);
            systemDropCards.getChildren().remove(card);
        } else if (referenceSources.contains(card)) {
            referenceSources.remove(card);
            referenceDropCards.getChildren().remove(card);
        }
    }

    public AnnotationDropBox() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/AnnotationDropBox.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        initialize();
    }

    public String getName() {
        return matchNameTextField.getText();
    }

    public boolean hasValidCards() {
        if (matchNameTextField.getText().equals("")) {
            WarningModal.createModal("Invalid Parent Name", "A Match Card has a blank Parent Name; please fill in a parent name and try again.");
            WarningModal.show();
            return false;
        }

        for (AnnotationDropCard card: systemSources) {
            if (!card.hasValidAttributes()) {
                return false;
            }
        }

        for (AnnotationDropCard card: referenceSources) {
            if (!card.hasValidAttributes()) {
                return false;
            }
        }

        return true;
    }

    public List<Map<String, String>> getSystemCards() {
        List<Map<String, String>> systemCards = new ArrayList<>();

        systemSources.forEach(card -> {
            if (card.hasValidAttributes()) {
                systemCards.add(card.getAttributes());
            }
        });

        return systemCards;
    }

    public List<Map<String, String>> getReferenceCards() {
        List<Map<String, String>> referenceCards = new ArrayList<>();

        referenceSources.forEach(card -> {
            if (card.hasValidAttributes()) {
                Map<String, String> rowAttributes = card.getAttributes();
                rowAttributes.forEach((key, value) -> logger.error("{} : {}", key, value));
                referenceCards.add(rowAttributes);
            }
        });

        return referenceCards;
    }
}
