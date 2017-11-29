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

class AnnotationDropBoxRow extends HBox {
    private static Logger logger = LogManager.getLogger();

    @FXML private Label attributeLabel;
    @FXML private JFXTextField attributeName;
    @FXML private JFXTextField sysValue;
    @FXML private JFXTextField refValue;
    @FXML private Label lockButton;
    @FXML private Label removeButton;
    private boolean locked = false;

    AnnotationDropBoxRow() {
        this("");
    }

    AnnotationDropBoxRow(String name) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/AnnotationDropBoxRow.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        initialize(name);
    }

    private void initialize(String name) {
        if (name.equals("")) {
            attributeLabel.setVisible(false);
        } else {
            attributeLabel.setText(name);
            attributeName.setVisible(false);
            lockButton.setVisible(false);
            removeButton.setVisible(false);
        }
    }

    @FXML private void toggleLock() {
        locked = !locked;
        if (locked) {
            lockButton.getStyleClass().add("button-locked");
        } else {
            lockButton.getStyleClass().remove("button-locked");
        }
    }

    @FXML private void removeRow() {
        ((VBox)getParent()).getChildren().remove(this);
    }

    AnnotationDropBox.Attribute getAttributeRow() {
        String name = attributeName.isVisible() ? attributeName.getText() : attributeLabel.getText();
        return new AnnotationDropBox.Attribute(name, sysValue.getText(), refValue.getText(), locked);
    }
}
