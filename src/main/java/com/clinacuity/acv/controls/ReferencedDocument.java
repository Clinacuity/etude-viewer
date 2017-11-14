package com.clinacuity.acv.controls;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

public class ReferencedDocument extends StackPane {
    private static Logger logger = LogManager.getLogger();

    private HBox parent;
    private AnnotationTypeDraggable sourceAnnotation;
    @FXML private Label removeButton;
    @FXML private VBox image;

    public ReferencedDocument(HBox targetBox, AnnotationTypeDraggable source) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/ReferencedDocument.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        sourceAnnotation = source;
        parent = targetBox;

        setOnMouseEntered(event -> {
            removeButton.setVisible(true);
//            getStyleClass().add();
        });
        setOnMouseExited(event -> removeButton.setVisible(false));

        String style;
        if (sourceAnnotation.getCorpusType().equals("system")) {
            style = "-fx-background-color: Red; ";
        } else {
            style = "-fx-background-color: Green; ";
        }
        style += "-fx-background-radius: 6px;";
        style += "-fx-background-insets: 1;";
        image.setStyle(style);
    }

    @FXML private void remove() {
        sourceAnnotation.show();
        parent.getChildren().remove(this);
    }
}
