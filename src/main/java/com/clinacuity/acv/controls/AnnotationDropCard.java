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
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactfx.util.FxTimer;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

class AnnotationDropCard extends StackPane {
    private static Logger logger = LogManager.getLogger();
    private static final Duration ANIMATION_DURATION = Duration.millis(200.0d);
    private static final double START_ROTATION = 0.0d;
    private static final double END_ROTATION = -90.0d;

    @FXML private Label cardLabel;
    @FXML private VBox targetBox;
    @FXML private VBox collapsibleBox;
    @FXML private VBox xPathRow;
    @FXML private VBox lockedRowsBox;
    @FXML private Pane collapsiblePane;
    @FXML private Label collapseButton;

    private AnnotationTypeDraggable source;
    private AnnotationDropBox parent;
    private List<String> sourceAttributesCopy = new ArrayList<>();
    private Map<JFXTextField, String> attributeRows = new HashMap<>();
    private Map<String, HBox> lockedRowsMap = new HashMap<>();

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

        sourceAttributesCopy.clear();
        sourceAttributesCopy.addAll(source.getAttributes());

        setXPathAttributeRow();

        setBeginEndAttributes();

        sourceAttributesCopy.forEach(attribute -> targetBox.getChildren().add(getAttributeRow(attribute, "", true)));

        // TODO: make these absolute values or something...
        if (getCorpusType() == ConfigurationBuilderController.CorpusType.SYSTEM) {
            setStyle("-fx-background-color: rgba(70, 130, 180, 0.2);");
        } else {
            setStyle("-fx-background-color: rgba(255, 140, 0, 0.2);");
        }
    }

    private void setXPathAttributeRow() {
        if (sourceAttributesCopy.contains("XPath")) {
            sourceAttributesCopy.remove("XPath");
            xPathRow.getChildren().add(0, getAttributeRow("XPath", "XPath", false));
        } else {
            xPathRow.getChildren().add(0, getAttributeRow(source.getXPath(), "XPath", false));
        }
    }

    private void setBeginEndAttributes() {
        if (sourceAttributesCopy.contains("begin")) {
            sourceAttributesCopy.remove("begin");
            targetBox.getChildren().add(getAttributeRow("begin", "Begin Attr", false));
        } else {
            if (sourceAttributesCopy.contains("start")) {
                sourceAttributesCopy.remove("start");
                targetBox.getChildren().add(getAttributeRow("start", "Begin Attr", false));
            }
        }

        if (sourceAttributesCopy.contains("end")) {
            sourceAttributesCopy.remove("end");
            targetBox.getChildren().add(getAttributeRow("end", "End Attr", false));
        } else {
            if (sourceAttributesCopy.contains("finish")) {
                sourceAttributesCopy.remove("finish");
                targetBox.getChildren().add(getAttributeRow("finish", "End Attr", false));
            }
        }
    }

    private HBox getAttributeRow(String attribute, String initialValue, boolean includeButtons) {
        AnnotationDropCardRow row = new AnnotationDropCardRow(attribute, initialValue, includeButtons, this);

        attributeRows.put(row.getAttributeTextField(), row.getName());

        return row;
    }

    private HBox getLockedRow(String attributeName) {
        HBox locked = new HBox();
        locked.setSpacing(5.0d);
        locked.setAlignment(Pos.CENTER);

        Label lockedAttribute = new Label("@" + attributeName + "=");
        lockedAttribute.getStyleClass().add("text-small-bold");
        lockedAttribute.getStyleClass().add("text-gray");

        JFXTextField attributeValue = new JFXTextField();
        attributeValue.getStyleClass().add("text-medium-normal");

        locked.getChildren().addAll(lockedAttribute, attributeValue);
        return locked;
    }

    ConfigurationBuilderController.CorpusType getCorpusType() { return source.getCorpusType(); }

    Map<String, String> getAttributes() {
        Map<String, String> attributes = new HashMap<>();
        attributeRows.forEach((key, value) -> attributes.put(key.getText(), value));

        String xpath = attributes.get("XPath");
        StringBuilder xpathCombined = new StringBuilder();
        xpathCombined.append(xpath);

        lockedRowsMap.forEach((key, value) -> {
            xpathCombined.append("[@");
            xpathCombined.append(key);
            xpathCombined.append("='");
            xpathCombined.append(((JFXTextField)value.getChildren().get(1)).getText());
            xpathCombined.append("']");
        });

        attributes.put("XPath", xpathCombined.toString());

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

    void toggleLock(AnnotationDropCardRow row) {
        if (row.isLocked()) {
            String name = row.getName();
            HBox lockedRow = getLockedRow(name);

            lockedRowsMap.put(name, lockedRow);
            lockedRowsBox.getChildren().add(lockedRow);
        } else {
            lockedRowsBox.getChildren().remove(lockedRowsMap.get(row.getName()));
        }
    }

    void removeRow(AnnotationDropCardRow row) {
        targetBox.getChildren().remove(row);
        attributeRows.remove(row.getAttributeTextField());
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
                collapsingTimeline.setOnFinished(event -> {
                    targetBox.setVisible(true);
                    collapsiblePane.setMaxHeight(Double.MAX_VALUE);
                    collapsibleBox.setMaxHeight(Double.MAX_VALUE);
                });
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
