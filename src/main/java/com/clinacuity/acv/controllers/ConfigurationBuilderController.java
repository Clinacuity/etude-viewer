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

    @FXML private VBox testItemBox;
    @FXML private VBox referenceItemBox;
    private List<ConfigurationBlock> testBlocks = new ArrayList<>();
    private List<ConfigurationBlock> referenceBlocks = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("Configuration Builder Controller initialized");
        addConfigurationBlock();
        addReferenceConfigurationBlock();
    }

    @FXML
    private void addConfigurationBlock() {
        int index = testItemBox.getChildren().size() - 1;
        ConfigurationBlock block = new ConfigurationBlock(testItemBox);
        testItemBox.getChildren().add(index, block);
        testBlocks.add(block);
    }

    @FXML
    private void addReferenceConfigurationBlock() {
        int index = referenceItemBox.getChildren().size() - 1;
        ConfigurationBlock block = new ConfigurationBlock(referenceItemBox);
        referenceItemBox.getChildren().add(index, block);
        referenceBlocks.add(block);
    }

    @FXML
    private void saveConfigurationDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save the configuration file");
        File file = fileChooser.showSaveDialog(AcvContext.getMainWindow());

        saveFile(file, testBlocks);
    }

    @FXML private void saveConfigurationDialogReference() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save reference configuration");
        File file = fileChooser.showSaveDialog(AcvContext.getMainWindow());

        saveFile(file, referenceBlocks);
    }

    private void saveFile(File file, List<ConfigurationBlock> targetBlocks) {
        if (file != null) {

            if (!file.getAbsolutePath().endsWith(".config")) {
                file = new File(file.getAbsolutePath() + ".config");
            }

            StringBuilder text = new StringBuilder();
            targetBlocks.forEach(block -> text.append(block.getText()));

            try {
                FileUtils.writeStringToFile(file, text.toString());
            } catch (IOException e) {
                logger.throwing(e);
            }
        } else {
            logger.warn("User cancelled file selection!");
        }
    }
}
