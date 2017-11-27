package com.clinacuity.acv.controls;

import com.clinacuity.acv.controllers.ConfigurationController;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnnotationDropBox extends StackPane {
    private static Logger logger = LogManager.getLogger();
    private static final double ANIMATION_DURATION = 200.0d;

    @FXML private GridPane annotationGrid;
    @FXML private HBox targetBox;
    @FXML private VBox contentBox;
    @FXML private HBox referencesBox;
    @FXML private VBox collapsibleBox;
    @FXML private Pane collapsiblePane;
    @FXML private JFXTextField matchNameTextField;

    private List<String> systemOptions = new ArrayList<>();
    private List<String> referenceOptions = new ArrayList<>();
    private List<String> lockedRows = new ArrayList<>();

    private int rowCount = 0;
    private double expandedHeight = 0.0d;
    private boolean isCollapsed = false;

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

    private void initialize() {
        setOnDragOver(event -> {
            if (ConfigurationController.draggedAnnotation != null) {
                event.acceptTransferModes(TransferMode.ANY);
            }
            event.consume();
        });

        setOnDragDropped(event -> {
            if (ConfigurationController.draggedAnnotation != null) {
                AnnotationTypeDraggable draggable = ConfigurationController.draggedAnnotation;
                referencesBox.getChildren().add(new ReferencedDocument(referencesBox, draggable));
                draggable.hide();

                if (ConfigurationController.draggableAnnotationCorpus.equals("system")) {
                    for(String attribute: ConfigurationController.draggedAnnotation.getAttributes()) {
                        if (!systemOptions.contains(attribute)) {
                            systemOptions.add(attribute);
                        }
                    }
                } else {
                    for (String attribute: ConfigurationController.draggedAnnotation.getAttributes()) {
                        if (!referenceOptions.contains(attribute)) {
                            referenceOptions.add(attribute);
                        }
                    }
                }
            }
        });

        setRowCount();
    }

    private void setRowCount() {
        for (Node node: annotationGrid.getChildren()) {
            int row = GridPane.getRowIndex(node);
            if (row > rowCount) {
                rowCount = row;
            }
        }
    }

    private Label getRemoveButton() {
        Label button = new Label();
        button.setText("");
        button.getStyleClass().add("button-delete");

        ImageView view = new ImageView(new Image("/img/icons8/delete.png"));
        view.setFitHeight(16.0d);
        view.setPreserveRatio(true);
        view.setPickOnBounds(true);
        button.setGraphic(view);

        button.setId(Integer.toString(rowCount));
        return button;
    }

    private Label getLockButton() {
        Label button = new Label();
        button.setText("");
        button.getStyleClass().add("button-lock");

        ImageView view = new ImageView(new Image("/img/icons8/lock.png"));
        view.setFitHeight(16.0d);
        view.setPreserveRatio(true);
        view.setPickOnBounds(true);
        button.setGraphic(view);

        button.setId(Integer.toString(rowCount));
        button.setOnMouseClicked(event -> {
            // toggle lock
            if (lockedRows.contains(button.getId())) {
                lockedRows.remove(button.getId());
                button.getStyleClass().remove("button-locked");
            } else {
                lockedRows.add(button.getId());
                button.getStyleClass().add("button-locked");
            }
        });

        return button;
    }

    @FXML private void addRow() {
        JFXTextField attributeName = new JFXTextField();
        JFXTextField systemField = new JFXTextField();
        JFXTextField referenceField = new JFXTextField();
        HBox separatorBox = new HBox(new Separator(Orientation.VERTICAL));
        separatorBox.setPadding(new Insets(0, 6, 0, 9));

        HBox buttonsBox = new HBox();
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        buttonsBox.setSpacing(10.0d);

        Label removeButton = getRemoveButton();
        removeButton.setOnMouseClicked(event -> {
            annotationGrid.getChildren().removeAll(attributeName, systemField, separatorBox, referenceField, buttonsBox);
            if (lockedRows.contains(removeButton.getId())) {
                lockedRows.remove(removeButton.getId());
            }
        });
        buttonsBox.getChildren().addAll(getLockButton(), removeButton);

        attributeName.getStyleClass().add("text-medium-normal");
        systemField.getStyleClass().add("text-medium-normal");
        referenceField.getStyleClass().add("text-medium-normal");

        attributeName.setPromptText("Attribute Name");
        systemField.setPromptText("attribute value (sys)");
        referenceField.setPromptText("attribute value (ref)");

        rowCount++;
        annotationGrid.add(attributeName, 0, rowCount);
        annotationGrid.add(systemField, 1, rowCount);
        annotationGrid.add(separatorBox, 2, rowCount);
        annotationGrid.add(referenceField, 3, rowCount);
        annotationGrid.add(buttonsBox, 4, rowCount);
    }

    @FXML private void collapseBox() {
        collapsiblePane.setMinHeight(collapsiblePane.getHeight());
        collapsiblePane.setMaxHeight(collapsiblePane.getHeight());
        collapsibleBox.setMinHeight(collapsibleBox.getHeight());
        collapsibleBox.setMaxHeight(collapsibleBox.getHeight());

        Timeline collapseTimeline = new Timeline();
        KeyValue minHeightKeysPane;
        KeyValue maxHeightKeysPane;
        KeyValue minHeightKeysBox;
        KeyValue maxHeightKeysBox;

        if (isCollapsed) {
            minHeightKeysPane = new KeyValue(collapsiblePane.minHeightProperty(), expandedHeight);
            maxHeightKeysPane = new KeyValue(collapsiblePane.maxHeightProperty(), expandedHeight);
            minHeightKeysBox = new KeyValue(collapsibleBox.minHeightProperty(), expandedHeight);
            maxHeightKeysBox = new KeyValue(collapsibleBox.maxHeightProperty(), expandedHeight);
            collapseTimeline.setOnFinished(event -> {
                contentBox.setVisible(true);
                matchNameTextField.setEditable(true);
            });
        } else {
            contentBox.setVisible(false);
            matchNameTextField.setEditable(false);
            expandedHeight = getHeight();
            minHeightKeysPane = new KeyValue(collapsiblePane.minHeightProperty(), targetBox.getHeight() * 2.0d);
            maxHeightKeysPane = new KeyValue(collapsiblePane.maxHeightProperty(), targetBox.getHeight() * 2.0d);
            minHeightKeysBox = new KeyValue(collapsibleBox.minHeightProperty(), targetBox.getHeight() * 2.0d);
            maxHeightKeysBox = new KeyValue(collapsibleBox.maxHeightProperty(), targetBox.getHeight() * 2.0d);
        }

        KeyFrame heightFrame = new KeyFrame(Duration.millis(ANIMATION_DURATION),
                minHeightKeysPane, maxHeightKeysPane, minHeightKeysBox, maxHeightKeysBox);

        collapseTimeline.getKeyFrames().add(heightFrame);
        collapseTimeline.play();
        isCollapsed = !isCollapsed;
    }
}
