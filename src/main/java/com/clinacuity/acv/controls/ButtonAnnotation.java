package com.clinacuity.acv.controls;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ButtonAnnotation extends AnchorPane {
    private static final Logger logger = LogManager.getLogger();

    @FXML private Button button;

    public ButtonAnnotation() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/ButtonAnnotation.fxml"));
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

    }

    @FXML private void onButtonAction() {
        logger.error("Stuff happened");
    }
}
