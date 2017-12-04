package com.clinacuity.acv.controls;

import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

class ReferencedDocument extends HBox {
    private static Logger logger = LogManager.getLogger();

    private VBox parent;
    private AnnotationTypeDraggable sourceAnnotation;
    @FXML private Label removeButton;
    @FXML private VBox image;
    @FXML private JFXTextField xpathTextField;

    ReferencedDocument(VBox targetBox, AnnotationTypeDraggable source) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/ReferencedDocument.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }
        initialize(targetBox, source);
    }

    private void initialize(VBox targetBox, AnnotationTypeDraggable source) {
        sourceAnnotation = source;
        parent = targetBox;

        setMouseEvents();
        setSourceImageStyling();
        setTextField();
    }

    private void setMouseEvents() {
        setOnMouseEntered(event -> {
            // highlight source
        });
        setOnMouseExited(event -> {
            /// unhighlight source
        });
    }

    private void setSourceImageStyling() {
        String style = "-fx-background-insets: -3 -1 -1 -1;";
        style += "-fx-background-radius: 2 6 2 2;";

        // TODO: change to better colors
        if (sourceAnnotation.getCorpusType().equals("system")) {
            style += "-fx-background-color: Red; ";
        } else {
            style += "-fx-background-color: Green; ";
        }
        image.setStyle(style);
    }

    private void setTextField() {
        xpathTextField.setText(sourceAnnotation.getXPath());
    }

    @FXML private void remove() {
        sourceAnnotation.show();
        parent.getChildren().remove(this);
    }

    public String getXpath() {
        return "";
    }
}
