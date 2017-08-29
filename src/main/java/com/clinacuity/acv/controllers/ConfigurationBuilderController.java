package com.clinacuity.acv.controllers;

import javafx.fxml.Initializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

public class ConfigurationBuilderController implements Initializable {
    private static Logger logger = LogManager.getLogger();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Configuration Builder Controller initialized");
    }
}
