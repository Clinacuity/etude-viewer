package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.FileUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoadScreenController implements Initializable {
    private static final Logger logger = LogManager.getLogger();

    @FXML private TextField gsInputTextField;
    @FXML private TextField testInputTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML private void pickReferenceFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Gold Standard Dictionaries Folder");
        fileChooser.getExtensionFilters().add(getFilter());
        File file = fileChooser.showOpenDialog(gsInputTextField.getScene().getWindow());

        if (file != null) {
            gsInputTextField.setText(file.getAbsolutePath());
        }
    }

    @FXML private void pickTestFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Test Dictionaries Folder");
        fileChooser.getExtensionFilters().add(getFilter());
        File file = fileChooser.showOpenDialog(testInputTextField.getScene().getWindow());

        if (file != null) {
            testInputTextField.setText(file.getAbsolutePath());
        }
    }

    @FXML private void runAcv() {
        // TODO: Activate loading spinner
        if (checkDocumentPaths()) {
            // load documents' file paths into context [triggers creating Annotations objects]
            AcvContext context = AcvContext.getInstance();
            context.targetDocumentPathProperty.setValue(testInputTextField.getText());
            context.referenceDocumentPathProperty.setValue(gsInputTextField.getText());

            // load the Acv main view
            context.mainController.reloadContent("/pages/AcvContent.fxml");
        } else {
            // TODO: deactivate the loading spinner
            logger.debug("loading spinner will deactivate on its own if the content is reloaded");
        }

    }

    private boolean checkDocumentPaths() {
        String test = testInputTextField.getText().trim();
        String gold = gsInputTextField.getText().trim();

        if (test.equals("") || !(new File(test).exists())) {
            // TODO: logger pop-up window saying this field is required
            return false;
        }
        if (gold.equals("") || !(new File(gold).exists())) {
            // TODO: logger pop-up window saying this field is required
            return false;
        }

        return true;
    }

    private FileChooser.ExtensionFilter getFilter() {
        return new FileChooser.ExtensionFilter("(*.json) ETUDE Output Dictionary", "*.json", "*.xml");
    }
}
