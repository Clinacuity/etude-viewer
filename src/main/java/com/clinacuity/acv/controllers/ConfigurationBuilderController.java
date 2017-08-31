package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.controls.ConfigurationBlock;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ConfigurationBuilderController implements Initializable {
    private static Logger logger = LogManager.getLogger();

    @FXML private VBox itemBox;
    private List<ConfigurationBlock> blocks = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Configuration Builder Controller initialized");
        addConfigurationBlock();
    }

    @FXML
    private void addConfigurationBlock() {
        int size = itemBox.getChildren().size();
        int targetIndex = size == 1 ? 0 : size - 1;
        ConfigurationBlock block = new ConfigurationBlock(itemBox);
        itemBox.getChildren().add(targetIndex, block);
        blocks.add(block);
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

        StringBuilder text = new StringBuilder();
        blocks.forEach(block -> text.append(block.getText()));

        try {
            FileUtils.writeStringToFile(file, text.toString());
        } catch (IOException e) {
            logger.throwing(e);
        }
    }
}
