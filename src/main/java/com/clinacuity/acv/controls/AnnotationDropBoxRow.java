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
import java.util.ArrayList;
import java.util.List;

class AnnotationDropBoxRow extends HBox {
    private static Logger logger = LogManager.getLogger();

    @FXML private Label attributeLabel;
    @FXML private JFXComboBox<String> attributeDropdown;
    @FXML private JFXTextField sysValue;
    @FXML private JFXTextField refValue;
    @FXML private Label lockButton;
    @FXML private Label removeButton;
    private boolean locked = false;
    private boolean requiredAttribute = true;

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
            requiredAttribute = false;
        } else {
            attributeLabel.setText(name);
            attributeDropdown.setVisible(false);
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
        String name = requiredAttribute ? attributeLabel.getText() : attributeDropdown.getSelectionModel().getSelectedItem();
        String system = sysValue.getText();
        String reference = refValue.getText();

        return new AnnotationDropBox.Attribute(name, system, reference, locked);
    }

    void updateSystemValue(String value) {
        if (requiredAttribute) {
            sysValue.setText(value);
        } else {
            if (sysValue.getText().equals("")) {
                sysValue.setText(value);
            }
        }
    }

    void updateReferenceValue(String value) {
        if (requiredAttribute) {
            refValue.setText(value);
        } else {
            if (refValue.getText().equals("")) {
                refValue.setText(value);
            }
        }
    }

    void updateOptions(List<String> sysOptions, List<String> refOptions) {
        List<String> values = new ArrayList<>();
        sysOptions.forEach(option -> {
            if (!values.contains(option)) {
                values.add(option);
            }
        });
        refOptions.forEach(option -> {
            if (!values.contains(option)) {
                values.add(option);
            }
        });

        String selectedAttributeName = attributeDropdown.getSelectionModel().getSelectedItem();
        attributeDropdown.setItems(FXCollections.observableList(values));
        if (values.contains(selectedAttributeName)) {
            attributeDropdown.getSelectionModel().select(selectedAttributeName);
        }
    }
}
