package com.clinacuity.acv.controls;

import com.clinacuity.acv.controllers.ConfigurationController;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
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

    @FXML private HBox annotationBox;
    @FXML private VBox contentBox;
    @FXML private HBox sourcesBox;
    @FXML private VBox collapsibleBox;
    @FXML private Pane collapsiblePane;
    @FXML private VBox collapsibleContentBox;
    @FXML private JFXTextField matchNameTextField;
    AnnotationDropBoxRow shortNameRow = new AnnotationDropBoxRow("Short Name");
    AnnotationDropBoxRow beginAttrRow = new AnnotationDropBoxRow("Begin Attr");
    AnnotationDropBoxRow endAttrRow = new AnnotationDropBoxRow("End Attr");

    private List<String> systemOptions = new ArrayList<>();
    private List<String> referenceOptions = new ArrayList<>();

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

                sourcesBox.getChildren().add(new ReferencedDocument(sourcesBox, draggable));
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

        contentBox.getChildren().add(shortNameRow);
        contentBox.getChildren().add(beginAttrRow);
        contentBox.getChildren().add(endAttrRow);
    }

    /**
     * An annotation drop box is valid if it has a Parent Name.  Its attributes are valid if they are unique, but
     * these are checked whenever each value is input.
     * @return Returns true if the Parent Name is not empty
     */
    public boolean isValid() {
        return !(matchNameTextField.getText().equals("") ||
                shortNameRow.getAttributeRow().systemValue.equals("") ||
                beginAttrRow.getAttributeRow().systemValue.equals("") ||
                endAttrRow.getAttributeRow().systemValue.equals(""));
    }

    public List<Attribute> getAttributes() {
        List<Attribute> attributes = new ArrayList<>();
        for (Node child: contentBox.getChildren()) {
            AnnotationDropBoxRow row = (AnnotationDropBoxRow)child;
            if (row != null) {
                attributes.add(row.getAttributeRow());
            }
        }
        return attributes;
    }

    public String getName() {
        return matchNameTextField.getText();
    }

    @FXML private void addRow() {
        contentBox.getChildren().add(new AnnotationDropBoxRow());
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
                collapsibleContentBox.setVisible(true);
                matchNameTextField.setEditable(true);
            });
        } else {
            collapsibleContentBox.setVisible(false);
            matchNameTextField.setEditable(false);
            expandedHeight = getHeight();
            minHeightKeysPane = new KeyValue(collapsiblePane.minHeightProperty(), annotationBox.getHeight() * 2.0d);
            maxHeightKeysPane = new KeyValue(collapsiblePane.maxHeightProperty(), annotationBox.getHeight() * 2.0d);
            minHeightKeysBox = new KeyValue(collapsibleBox.minHeightProperty(), annotationBox.getHeight() * 2.0d);
            maxHeightKeysBox = new KeyValue(collapsibleBox.maxHeightProperty(), annotationBox.getHeight() * 2.0d);
        }

        KeyFrame heightFrame = new KeyFrame(Duration.millis(ANIMATION_DURATION),
                minHeightKeysPane, maxHeightKeysPane, minHeightKeysBox, maxHeightKeysBox);

        collapseTimeline.getKeyFrames().add(heightFrame);
        collapseTimeline.play();
        isCollapsed = !isCollapsed;
    }

    public static class Attribute {
        public String name;
        public String systemValue;
        public String referenceValue;
        public boolean isLocked;

        Attribute(String attributeName, String system, String reference, boolean locked) {
            name = attributeName;
            systemValue = system;
            referenceValue = reference;

            isLocked = locked;
        }
    }
}
