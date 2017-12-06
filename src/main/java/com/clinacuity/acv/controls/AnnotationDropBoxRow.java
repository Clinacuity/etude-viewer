package com.clinacuity.acv.controls;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.List;

class AnnotationDropBoxRow extends HBox {
    private static Logger logger = LogManager.getLogger();

    @FXML private Label attributeLabel;
    @FXML private JFXTextField attributeName;
    @FXML private JFXComboBox<String> sysValuesCombo;
    @FXML private JFXComboBox<String> refValuesCombo;
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
            sysValue.setVisible(false);
            refValue.setVisible(false);
        } else {
            attributeLabel.setText(name);
            attributeName.setVisible(false);
            sysValuesCombo.setVisible(false);
            refValuesCombo.setVisible(false);
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

    AnnotationDropBox.Attribute getAttribute() {
        String name = attributeName.isVisible() ? attributeName.getText() : attributeLabel.getText();

        return new AnnotationDropBox.Attribute(name,
                sysValuesCombo.getSelectionModel().getSelectedItem(),
                refValuesCombo.getSelectionModel().getSelectedItem(),
                locked);
    }

    void updateOptions(List<String> sysOptions, List<String> refOptions) {
        String selectedSystem = sysValuesCombo.getSelectionModel().getSelectedItem();
        String selectedReference = sysValuesCombo.getSelectionModel().getSelectedItem();

        sysValuesCombo.setItems(FXCollections.observableList(sysOptions));
        refValuesCombo.setItems(FXCollections.observableList(refOptions));

        if (sysOptions.contains(selectedSystem)) {
            sysValuesCombo.getSelectionModel().select(selectedSystem);
        }

        if (refOptions.contains(selectedReference)) {
            refValuesCombo.getSelectionModel().select(selectedReference);
        }
    }
}
