package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class LoadScreenController implements Initializable {
    private static final Logger logger = LogManager.getLogger();
    private static final String CORPUS_DICTIONARY_NAME = "/corpus.json";
    private static final String REFERENCE_DIRECTORY_NAME = "/reference/";
    private static final String TEST_DIRECTORY_NAME = "/system/";

    @FXML private TextField gsInputTextField;
    @FXML private JFXTextField masterDirectoryTextField;
    @FXML private Label errorLabel;

    private boolean isValidDirectory = false;
    private File corpusFile;
    private File targetDirectory;
    private File referenceDirectory;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        masterDirectoryTextField.focusedProperty().addListener((obs, old, focusGained) -> {
            if (focusGained) {
                errorLabel.setVisible(false);
            } else {
                String path = masterDirectoryTextField.getText();

                checkItemsInDirectory(path);

                if (isValidDirectory || path.equals("")) {
                    errorLabel.setVisible(false);
                } else {
                    errorLabel.setVisible(true);
                }
            }
        });
    }

    @FXML private void pickMasterDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Test Dictionaries Folder");
        File directory = directoryChooser.showDialog(masterDirectoryTextField.getScene().getWindow());

        if (directory != null) {
            masterDirectoryTextField.setText(directory.getAbsolutePath());
        }
    }

    @FXML private void runAcv() {
        if (isValidDirectory) {
            AcvContext context = AcvContext.getInstance();

            context.corpusFilePathProperty.setValue(corpusFile.getAbsolutePath());
            context.targetDirectoryProperty.setValue(targetDirectory.getAbsolutePath() + "/");
            context.referenceDirectoryProperty.setValue(referenceDirectory.getAbsolutePath() + "/");

            context.mainController.reloadContent(AcvContext.COMPARISON_VIEW);
        } else {
            // TODO: deactivate the loading spinner
            logger.debug("loading spinner will deactivate on its own if the content is reloaded");
        }
    }

    private void checkItemsInDirectory(String path) {
        corpusFile = new File(path + CORPUS_DICTIONARY_NAME);
        targetDirectory = new File(path + TEST_DIRECTORY_NAME);
        referenceDirectory = new File(path + REFERENCE_DIRECTORY_NAME);

        if (!targetDirectory.exists() || targetDirectory.isFile()
                || !referenceDirectory.exists() || referenceDirectory.isFile()
                || !corpusFile.exists() || corpusFile.isDirectory()) {
            isValidDirectory = false;
        } else {
            isValidDirectory = true;
        }
    }
}
