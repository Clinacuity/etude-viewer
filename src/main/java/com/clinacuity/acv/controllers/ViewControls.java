package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXCheckBox;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ViewControls implements Initializable {
    private static final Logger logger = LogManager.getLogger();

    @FXML private JFXComboBox<String> annotationComboBox;
    @FXML private JFXCheckBox toggleExact;
    @FXML private JFXCheckBox toggleOverlap;
    @FXML private JFXCheckBox toggleSubsumed;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AcvContext context = AcvContext.getInstance();

        // TODO: assign listener events to toggles

        // TODO: fill in the combo box
        annotationComboBox.itemsProperty().bind(context.annotationList);
        annotationComboBox.getSelectionModel().selectFirst();
        context.selectedAnnotationProperty.bind(annotationComboBox.valueProperty());

        // TODO: populate the Feature Lists

        // TODO: Fill in the metrics
    }

    @FXML private void clickedMe() {
        logger.error("CLICKED THE TOOLTIP ICON!");
    }
}
