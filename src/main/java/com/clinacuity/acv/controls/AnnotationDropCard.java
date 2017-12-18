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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactfx.util.FxTimer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class AnnotationDropCard extends StackPane {
    private static Logger logger = LogManager.getLogger();
    private static final Duration ANIMATION_DURATION = Duration.millis(200.0d);
    private static final double START_ROTATION = 0.0d;
    private static final double END_ROTATION = -90.0d;

    @FXML private Label cardLabel;
    @FXML private VBox targetBox;
    @FXML private VBox collapsibleBox;
    @FXML private Pane collapsiblePane;
    @FXML private Label collapseButton;
    private AnnotationTypeDraggable source;
    private AnnotationDropBox parent;
    private List<String> sourceAttributes;
    private Map<JFXTextField, Label> attributeRows = new HashMap<>();
    private Timeline collapsingTimeline = new Timeline();
    private double expandedHeight = 0.0d;
    private boolean isCollapsed = false;

    AnnotationDropCard(AnnotationTypeDraggable draggableSource, AnnotationDropBox parentDropBox) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/AnnotationDropCard.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        source = draggableSource;
        parent = parentDropBox;

        FxTimer.runLater(java.time.Duration.ofMillis(150), this::initialize);
    }

    private void initialize() {
        cardLabel.setText(source.getLabelName());

        sourceAttributes = source.getAttributes();

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
        if (sourceAttributes.contains("begin")) {
            sourceAttributes.remove("begin");
            createAttributeRow("begin", "Begin Attr", false);
        } else {
            if (sourceAttributes.contains("start")) {
                sourceAttributes.remove("start");
                createAttributeRow("start", "Begin Attr", false);
            }
        }

        if (sourceAttributes.contains("end")) {
            sourceAttributes.remove("end");
            createAttributeRow("end", "End Attr", false);
        } else {
            if (sourceAttributes.contains("finish")) {
                sourceAttributes.remove("finish");
                createAttributeRow("finish", "End Attr", false);
            }
        }

        if (sourceAttributes.contains("XPath")) {
            sourceAttributes.remove("XPath");
            createAttributeRow("XPath", "XPath", false);
        } else {
            createAttributeRow(source.getXPath(), "XPath", false);
        }
    }

    private void createAttributeRow(String attribute, String initialValue, boolean includeButtons) {
        /*
        This creates the "Row" elements; the structure looks as follows:
        HBox
            VBox
                JFXTextField -- attribute Key
                Label -- attribtue Value
            HBox
                Label -- hide
                Label -- remove
         */
        JFXTextField attributeValueField = new JFXTextField(initialValue);
        attributeValueField.setPromptText("attribute value");
        attributeValueField.getStyleClass().add("text-medium-normal");
        attributeValueField.setMinWidth(getWidth() - 50.0d);
        attributeValueField.setMaxWidth(getWidth() - 50.0d);

        Label attributeLabel = new Label(attribute);
        attributeLabel.getStyleClass().add("text-medium-italic");
        attributeLabel.setPadding(new Insets(0.0d, 0.0d, 0.0d, 10.0d));

        Label lockLabel = new Label();
        lockLabel.getStyleClass().add("button-lock");
        ImageView lockView = new ImageView(new Image("/img/icons8/lock.png"));
        lockView.setPreserveRatio(true);
        lockView.setFitHeight(16.0d);
        lockLabel.setGraphic(lockView);
        lockLabel.setOnMouseClicked(event -> logger.error("Lock not implemented"));

        Label removeLabel = new Label();
        removeLabel.getStyleClass().add("button-delete");
        ImageView hideView = new ImageView(new Image("/img/icons8/delete.png"));
        hideView.setPreserveRatio(true);
        hideView.setFitHeight(16.0d);
        removeLabel.setGraphic(hideView);

        VBox keyValueBox = new VBox();
        keyValueBox.setSpacing(3.0d);
        keyValueBox.setPadding(new Insets(5.0d));
        keyValueBox.getChildren().addAll(attributeValueField, attributeLabel);

        HBox buttonBox = new HBox();
        buttonBox.setSpacing(3.0d);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(3.0d, 3.0d, 0.0d, 0.0d));
        buttonBox.getChildren().addAll(lockLabel, removeLabel);

        HBox mainBox = new HBox();
        mainBox.setSpacing(5.0d);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.getChildren().addAll(keyValueBox, buttonBox);

        if (includeButtons) {
            buttonBox.setVisible(true);
        } else {
            buttonBox.setVisible(false);
        }

        /*
        This creates the button actions and adds the items to the appropriate lists
         */
        attributeRows.put(attributeValueField, attributeLabel);
        lockLabel.setOnMouseClicked(event -> {
            logger.error("OK LOCK CLICKED");
            // toggle lock
        });

        removeLabel.setOnMouseClicked(event -> {
            targetBox.getChildren().remove(mainBox);
            attributeRows.remove(attributeValueField);
        });

        targetBox.getChildren().add(mainBox);
    }

    ConfigurationBuilderController.CorpusType getCorpusType() { return source.getCorpusType(); }

    Map<String, String> getAttributes() {
        Map<String, String> attributes = new HashMap<>();
        attributeRows.forEach((key, value) -> attributes.put(key.getText(), value.getText()));

        return attributes;
    }

    boolean hasValidAttributes() {
        List<String> keys = new ArrayList<>();
        attributeRows.keySet().forEach(key -> keys.add(key.getText()));

        for (int i = 0; i < keys.size(); i++) {
            // check for begin attribute
            if (!keys.contains("Begin Attr")) {
                WarningModal.createModal("Invalid attribute values", "One or more cards don't have a Begin attribute key and value.");
                WarningModal.show();
                return false;
            }

            // check for end attribute
            if (!keys.contains("End Attr")) {
                WarningModal.createModal("Invalid attribute values", "One or more cards don't have a End attribute key and value.");
                WarningModal.show();
                return false;
            }
        }

        List<String> values = new ArrayList<>();

        for (JFXTextField inputField: attributeRows.keySet()) {
            String text = inputField.getText();

            if (text.equals("")) {
                WarningModal.createModal("Invalid attribute values", "One or more cards have an attribute key with an empty value.  Remove the attribute or fill in the value.");
                WarningModal.show();
                return false;
            }

            if (values.contains(text)) {
                WarningModal.createModal("Invalid attribute values", "The value <" + text + "> is repeated in more than one attribute row.");
                WarningModal.show();
                return false;
            }

            values.add(text);
        }

        return true;
    }

    @FXML private void removeBox() {
        parent.removeCard(this);
    }

    @FXML private void collapseCard() {
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
                collapsingTimeline.setOnFinished(event -> targetBox.setVisible(true));
            } else {
                targetBox.setVisible(false);
                expandedHeight = getHeight();

                minHeightKeysPane = new KeyValue(collapsiblePane.minHeightProperty(), cardLabel.getHeight() * 2.0d);
                maxHeightKeysPane = new KeyValue(collapsiblePane.maxHeightProperty(), cardLabel.getHeight() * 2.0d);
                minHeightKeysBox = new KeyValue(collapsibleBox.minHeightProperty(), cardLabel.getHeight() * 2.0d);
                maxHeightKeysBox = new KeyValue(collapsibleBox.maxHeightProperty(), cardLabel.getHeight() * 2.0d);
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
