package com.clinacuity.acv.controls;

import com.clinacuity.acv.context.AcvContext;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXCheckBox;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ViewControls extends GridPane {
    private static final Logger logger = LogManager.getLogger();

    @FXML private JFXComboBox<String> annotationComboBox;
    @FXML private JFXCheckBox toggleExact;
    @FXML private JFXCheckBox toggleOverlap;
    @FXML private JFXCheckBox toggleSubsumed;

    public ViewControls() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/ViewControls.fxml"));
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
