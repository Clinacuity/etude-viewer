package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class LoadScreenController implements Initializable {
    private static final Logger logger = LogManager.getLogger();

    @FXML private TextField gsInputTextField;
    @FXML private TextField testInputTextField;
    @FXML private Text errorText;

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
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Test Dictionaries Folder");
        File directory = directoryChooser.showDialog(testInputTextField.getScene().getWindow());

        if (directory != null) {
            testInputTextField.setText(directory.getAbsolutePath());
            for (File file : directory.listFiles()) {
                if(!FilenameUtils.getExtension(file.toString()).equals("json") && !FilenameUtils.getExtension(file.toString()).equals("xml")) {
                    displayExceptionModal(new Exception("Invalid file extension(s)!"));
                }
            }
        }
    }

    @FXML private void runAcv() {
        AcvContext.getInstance().corpusFilePathProperty.setValue("/Users/jkaccetta/Desktop/corpus.json");

        // TODO: Activate loading spinner
        if (checkDocumentPaths()) {
            // load documents' file paths into context [triggers creating Annotations objects]
            AcvContext context = AcvContext.getInstance();
            context.corpusFilePathProperty.setValue(testInputTextField.getText());
//            context.targetDocumentPathProperty.setValue(testInputTextField.getText());
//            context.referenceDocumentPathProperty.setValue(gsInputTextField.getText());

            // load the Acv main view
            context.mainController.reloadContent(AcvContext.COMPARISON_VIEW);
        } else {
            // TODO: deactivate the loading spinner
            logger.debug("loading spinner will deactivate on its own if the content is reloaded");
        }
    }

    private boolean checkDocumentPaths() {
        String test = testInputTextField.getText().trim();

        if (test.equals("") || !(new File(test).exists())) {
            errorText.setVisible(true);
            return false;
        }
        return true;
    }

    private FileChooser.ExtensionFilter getFilter() {
        return new FileChooser.ExtensionFilter("(*.json) ETUDE Output Dictionary", "*.json", "*.xml");
    }

    private void displayExceptionModal(Throwable exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText("An unexpected error occurred! Please send the log file to support@Clinacuity.com");
        alert.setResizable(true);
        alert.show();
    }
}
