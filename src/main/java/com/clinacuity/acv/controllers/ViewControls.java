package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXCheckBox;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

public class ViewControls extends VBox {
    private static final Logger logger = LogManager.getLogger();

    @FXML private JFXComboBox<String> annotationComboBox;
    @FXML private JFXCheckBox toggleExactMatches;
    @FXML private JFXCheckBox togglePartialMatches;
    @FXML private JFXCheckBox toggleNoMatch;
    @FXML private Label recallLabel;
    @FXML private Label precisionLabel;
    @FXML private Label fOneMeasureLabel;

    public ViewControls() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/ViewControls.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        AcvContext context = AcvContext.getInstance();

        // assign listener events to toggles
        context.exactMatchesProperty.bindBidirectional(toggleExactMatches.selectedProperty());
        context.overlappingMatchesProperty.bindBidirectional(togglePartialMatches.selectedProperty());
        context.noMatchesProperty.bindBidirectional(toggleNoMatch.selectedProperty());

        // fill in the combo box
        annotationComboBox.itemsProperty().bind(context.annotationList);
        annotationComboBox.getSelectionModel().selectFirst();
        context.selectedAnnotationTypeProperty.bind(annotationComboBox.valueProperty());

        // TODO: Fill in the metrics
    }
}
