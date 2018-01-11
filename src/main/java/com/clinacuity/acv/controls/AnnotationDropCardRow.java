package com.clinacuity.acv.controls;

import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

class AnnotationDropCardRow extends HBox {
    private static final Logger logger = LogManager.getLogger();
    private static final String LOCKED_STYLE = "button-locked";

    @FXML private JFXTextField attributeTextField;
    @FXML private Label attributeLabel;
    @FXML private Label lockLabel;
    @FXML private Label removeLabel;
    private AnnotationDropCard parent;
    private boolean locked = false;

    JFXTextField getAttributeTextField() { return attributeTextField; }
    boolean isLocked() { return locked; }
    String getName() { return attributeLabel.getText(); }

    AnnotationDropCardRow(String attribute, String initialValue, boolean includeButtons, AnnotationDropCard parentCard) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/AnnotationDropCardRow.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        attributeLabel.setText(attribute);
        attributeTextField.setText(initialValue);
        parent = parentCard;

        if (!includeButtons) {
            lockLabel.setVisible(false);
            removeLabel.setVisible(false);
        }
    }

    @FXML private void toggleLock() {
        locked = !locked;

        if (locked) {
            lockLabel.getStyleClass().add(LOCKED_STYLE);
            removeLabel.setOpacity(0.25d);
            parent.toggleLock(this);
        } else {
            lockLabel.getStyleClass().remove(LOCKED_STYLE);
            removeLabel.setOpacity(1.0d);
            parent.toggleLock(this);
        }
    }

    @FXML private void removeRow() {
        if (!locked) {
            parent.removeRow(this);
        }
    }
}
