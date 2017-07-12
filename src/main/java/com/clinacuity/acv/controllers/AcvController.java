package com.clinacuity.acv.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AcvController implements Initializable {
    private static final Logger logger = LogManager.getLogger();

    @FXML private GridPane mainGrid;
    @FXML private AnnotationComparisonViewController annotationComparisonView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            GridPane textViewController = FXMLLoader.load(getClass().getResource("/pages/AnnotationComparisonView.fxml"), resources);
            mainGrid.add(textViewController, 0, 0);

            GridPane controlsPane = FXMLLoader.load(getClass().getResource("/pages/ViewControls.fxml"), resources);
            mainGrid.add(controlsPane, 2, 0);
        } catch (IOException e) {
            logger.throwing(e);
        }
    }
}
