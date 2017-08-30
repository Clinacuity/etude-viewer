package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.controls.ConfigurationBlock;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ConfigurationBuilderController implements Initializable {
    private static Logger logger = LogManager.getLogger();

    @FXML private VBox itemBox;
    @FXML private ScrollPane scrollPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Configuration Builder Controller initialized");
        addConfigurationBlock();
    }

    @FXML
    private void addConfigurationBlock() {
        int size = itemBox.getChildren().size();
        int targetIndex = size == 1 ? 0 : size - 2;
        itemBox.getChildren().add(targetIndex, new ConfigurationBlock(scrollPane.getMinWidth()));
    }

    @FXML
    private void saveConfigurationDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save the configuration file");
        File file = fileChooser.showSaveDialog(AcvContext.getInstance().mainWindow);

        if (file != null) {
            saveFile(file);
        } else {
            logger.warn("User cancelled file selection!");
        }
    }

    private void saveFile(File file) {
        if (!file.getAbsolutePath().endsWith(".config")) {
            file = new File(file.getAbsolutePath() + ".config");
        }

        logger.error(file);

        try {
            FileUtils.writeStringToFile(file, "Hello World");
        } catch (IOException e) {
            logger.throwing(e);
        }
    }
}
