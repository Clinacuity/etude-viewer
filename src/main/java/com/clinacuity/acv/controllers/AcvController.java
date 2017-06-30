package com.clinacuity.acv.controllers;

import com.clinacuity.acv.controls.AnnotatedTextView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URL;
import java.util.ResourceBundle;

public class AcvController implements Initializable {
    private static final Logger logger = LogManager.getLogger();

    @FXML private GridPane mainGrid;
    @FXML private AnnotatedTextView annotatedTextView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
