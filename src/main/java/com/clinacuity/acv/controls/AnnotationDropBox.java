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
import javafx.scene.Node;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnnotationDropBox extends StackPane {
    private static Logger logger = LogManager.getLogger();
    private static final Duration ANIMATION_DURATION = Duration.millis(200.0d);
    private static final double START_ROTATION = 0.0d;
    private static final double END_ROTATION = -90.0d;

    @FXML private HBox annotationNameBox;
    @FXML private VBox contentBox;
    @FXML private VBox sourcesBox;
    @FXML private VBox collapsibleBox;
    @FXML private Pane collapsiblePane;
    @FXML private VBox collapsibleContentBox;
    @FXML private JFXTextField matchNameTextField;
    @FXML private Label collapseButton;

    private AnnotationDropBoxRow shortNameRow = new AnnotationDropBoxRow("Short Name");
    private AnnotationDropBoxRow beginAttrRow = new AnnotationDropBoxRow("Begin Attr");
    private AnnotationDropBoxRow endAttrRow = new AnnotationDropBoxRow("End Attr");
    private List<String> systemOptions = new ArrayList<>();
    private List<String> referenceOptions = new ArrayList<>();
    private double expandedHeight = 0.0d;
    private boolean isCollapsed = false;
    private Timeline collapsingTimeline = new Timeline();

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
            if (ConfigurationBuilderController.draggedAnnotation != null) {
                event.acceptTransferModes(TransferMode.ANY);
            }
            event.consume();
        });

        setOnDragDropped(event -> {
            if (ConfigurationBuilderController.draggedAnnotation != null) {
                AnnotationTypeDraggable draggable = ConfigurationBuilderController.draggedAnnotation;

                sourcesBox.getChildren().add(new ReferencedDocument(sourcesBox, draggable));
                draggable.hide();

                if (ConfigurationBuilderController.draggableAnnotationCorpus.equals(ConfigurationBuilderController.CorpusType.SYSTEM)) {
                    for(String attribute: ConfigurationBuilderController.draggedAnnotation.getAttributes()) {
                        if (!systemOptions.contains(attribute)) {
                            systemOptions.add(attribute);
                        }
                    }
                } else {
                    for (String attribute: ConfigurationBuilderController.draggedAnnotation.getAttributes()) {
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
        Set<String> attributes = new HashSet<>();
        for (Node child: contentBox.getChildren()) {
            AnnotationDropBoxRow row = (AnnotationDropBoxRow)child;
            if (row != null) {
                if (attributes.contains(row.getAttributeRow().name)) {
                    logger.warn("Attribute names are not unique.  <{}> is repeated", row.getAttributeRow().name);
                    WarningModal.createModal("Error in names",
                            "The names of attributes have to be unique, but the name \""
                                    + row.getAttributeRow().name
                                    + "\" was repeated.");
                    WarningModal.show();
                    return false;
                } else {
                    attributes.add(row.getAttributeRow().name);
                }
            }
        }

        if (matchNameTextField.getText().equals("")) {
            WarningModal.createModal("Annotation Name is empty",
                    "The Annotation Name (Parent) of a box was left empty; please specify a name for the annotation match.");
            WarningModal.show();
            return false;
        }

        if (!hasSystemAttributes()) {
            WarningModal.createModal("Missing attributes",
                    "Some of the required attributes are missing on the System Output input fields.");
            WarningModal.show();
            return false;
        }

        if (!hasReferenceAttributes()) {
            WarningModal.createModal("Missing attributes",
                    "Some of the required attributes are missing on the Reference input fields.");
            WarningModal.show();
            return false;
        }

        return true;
    }

    public boolean hasSystemAttributes() {
        return (shortNameRow.getAttributeRow().systemValue.length() > 0
                && beginAttrRow.getAttributeRow().systemValue.length() > 0
                && endAttrRow.getAttributeRow().systemValue.length() > 0);
    }

    public boolean hasReferenceAttributes() {
        return (shortNameRow.getAttributeRow().referenceValue.length() > 0
                && beginAttrRow.getAttributeRow().referenceValue.length() > 0
                && endAttrRow.getAttributeRow().referenceValue.length() > 0);
    }

    public List<String> getSystemXPaths() {
        return getXpathList(ConfigurationBuilderController.CorpusType.SYSTEM);
    }

    public List<String> getReferenceXPaths() {
        return getXpathList(ConfigurationBuilderController.CorpusType.REFERENCE);
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

    private List<String> getXpathList(ConfigurationBuilderController.CorpusType corpus) {
        List<String> xpathList = new ArrayList<>();

        int childCount = sourcesBox.getChildren().size();
        for (int i = 0; i < childCount; i++) {
            Node child = sourcesBox.getChildren().get(i);
            if (child instanceof ReferencedDocument) {
                ReferencedDocument document = (ReferencedDocument)child;
                if (document.getCorpus() == corpus && !document.getXpath().equals("")) {
                    xpathList.add(document.getXpath());
                }
            }
        }

        return xpathList;
    }

    @FXML private void addRow() {
        contentBox.getChildren().add(new AnnotationDropBoxRow());
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

    public static class Attribute {
        public String name;
        boolean isLocked;
        private String systemValue;
        private String referenceValue;

        Attribute(String attributeName, String system, String reference, boolean locked) {
            name = attributeName;
            systemValue = system;
            referenceValue = reference;

            isLocked = locked;
        }

        public String getValue(ConfigurationBuilderController.CorpusType corpus) {
            if (corpus == ConfigurationBuilderController.CorpusType.SYSTEM) {
                return systemValue;
            }
            return referenceValue;
        }
    }
}
