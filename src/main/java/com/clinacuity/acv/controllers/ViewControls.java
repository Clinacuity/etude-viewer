package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.google.gson.JsonNull;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXCheckBox;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

public class ViewControls extends VBox {
    private static final Logger logger = LogManager.getLogger();

    @FXML private JFXComboBox<String> annotationComboBox;
    @FXML private JFXCheckBox toggleExact;
    @FXML private JFXCheckBox toggleFeatureMismatch;
    @FXML private JFXCheckBox toggleOverlap;
    @FXML private JFXCheckBox toggleSubsumed;
    @FXML private JFXCheckBox toggleNoMatch;
    @FXML private Label recallLabel;
    @FXML private Label precisionLabel;
    @FXML private Label fOneMeasureLabel;

    @FXML private TextArea targetFeatureTree;
    @FXML private TextArea referenceFeatureTree;

    public void setTargetFeatureTreeText(String text) { targetFeatureTree.setText(text); }
    public void setReferenceFeatureTreeText(String text) { referenceFeatureTree.setText(text); }

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
        context.exactMatchesProperty.bindBidirectional(toggleExact.selectedProperty());
        context.exactFeatureMismatchProperty.bindBidirectional(toggleFeatureMismatch.selectedProperty());
        context.overlappingMatchesProperty.bindBidirectional(toggleOverlap.selectedProperty());
        context.subsumedMatchesProperty.bindBidirectional(toggleSubsumed.selectedProperty());
        context.noMatchesProperty.bindBidirectional(toggleNoMatch.selectedProperty());

        // fill in the combo box
        annotationComboBox.itemsProperty().bind(context.annotationList);
        annotationComboBox.getSelectionModel().selectFirst();
        context.selectedAnnotationTypeProperty.bind(annotationComboBox.valueProperty());

        // TODO: Fill in the metrics
    }
}
