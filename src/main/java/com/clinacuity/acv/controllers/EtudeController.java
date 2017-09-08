package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.modals.ConfirmationModal;
import com.clinacuity.acv.tasks.EtudeTask;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.net.URL;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class EtudeController implements Initializable{
    private static final Logger logger = LogManager.getLogger();
    private static EtudeTask etudeTask = null;
    private static final String ERROR_ID = "errLabel";

    @FXML private JFXTextField goldConfigInputField;
    @FXML private JFXTextField testConfigInputField;
    @FXML private JFXTextField goldInputTextField;
    @FXML private JFXTextField testInputTextField;
    @FXML private JFXTextField outputDirectoryTextField;
    @FXML private JFXTextField scoreKeyTextField;
    @FXML private JFXTextField scoreValuesTextField;
    @FXML private JFXTextField filePrefixTextField;
    @FXML private JFXTextField fileSuffixTextField;
    @FXML private JFXCheckBox metricsTP;
    @FXML private JFXCheckBox metricsFP;
    @FXML private JFXCheckBox metricsFN;
    @FXML private JFXCheckBox metricsPrecision;
    @FXML private JFXCheckBox metricsRecall;
    @FXML private JFXCheckBox metricsF1;

    @FXML private JFXCheckBox byFileCheckbox;
    @FXML private JFXCheckBox byFileAndTypeCheckbox;
    @FXML private JFXCheckBox byTypeCheckbox;
    @FXML private JFXCheckBox byTypeAndFileCheckbox;
    @FXML private JFXCheckBox ignoreWhitespaceCheckbox;


    private List<JFXTextField> failingFields = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        goldConfigInputField.focusedProperty().addListener(
                (obs, old, newValue) -> focusChanged(newValue, goldConfigInputField, true));
        testConfigInputField.focusedProperty().addListener(
                (obs, old, newValue) -> focusChanged(newValue, testConfigInputField, true));
        goldInputTextField.focusedProperty().addListener(
                (obs, old, newValue) -> focusChanged(newValue, goldInputTextField, false));
        testInputTextField.focusedProperty().addListener(
                (obs, old, newValue) -> focusChanged(newValue, testInputTextField, false));
        outputDirectoryTextField.focusedProperty().addListener(
                (obs, old, newValue) -> focusChanged(newValue, outputDirectoryTextField, false));
    }

    @FXML private void runEtudeButtonAction() {
        if (etudeTask == null) {
            etudeTask = new EtudeTask();
        }

        if (etudeTask.isRunning()) {
            etudeTask.cancel();
        }

        etudeTask.reset();

        if (checkInputs()) {
            if (checkOutputDirectory()) {
                runEtude();
            } else {
                confirmEtudeOverwrite();
            }
        }
    }

    private void runEtude() {etudeTask.setGoldConfigFilePath(goldConfigInputField.getText());
            etudeTask.setTestConfigFilePath(testConfigInputField.getText());
            etudeTask.setGoldInputDirPath(goldInputTextField.getText());
            etudeTask.setTestInputDirPath(testInputTextField.getText());
//            etudeTask.setGoldOutputDirPath(goldOutputTextField.getText());
//            etudeTask.setTestOutputDirPath(testOutputTextField.getText());
//            etudeTask.setCorpusFilePath(corpusOutputTextField.getText());
            etudeTask.setMetricsTP(metricsTP.isSelected());
            etudeTask.setMetricsFP(metricsFP.isSelected());
            etudeTask.setMetricsFN(metricsFN.isSelected());
            etudeTask.setMetricsPrecision(metricsPrecision.isSelected());
            etudeTask.setMetricsRecall(metricsRecall.isSelected());

            etudeTask.setMetricsF1(metricsF1.isSelected());
            etudeTask.setByFile(byFileCheckbox.isSelected());
            etudeTask.setByFileAndType(byFileAndTypeCheckbox.isSelected());
            etudeTask.setByType(byTypeCheckbox.isSelected());
            etudeTask.setByTypeAndFile(byTypeAndFileCheckbox.isSelected());
            etudeTask.setIgnoreWhitespace(ignoreWhitespaceCheckbox.isSelected());

        if (!scoreKeyTextField.getText().equals("") && scoreKeyTextField.getText() != null) {
            etudeTask.setScoreKey(scoreKeyTextField.getText());
        }

        if (!scoreValuesTextField.getText().equals("") && scoreValuesTextField.getText() != null) {
            etudeTask.setScoreValues(scoreValuesTextField.getText());
        }

        if (!filePrefixTextField.getText().equals("") && filePrefixTextField.getText() != null) {
            etudeTask.setFilePrefix(filePrefixTextField.getText());
        }

        if (!fileSuffixTextField.getText().equals("") && fileSuffixTextField.getText() != null) {
            etudeTask.setFileSuffix(fileSuffixTextField.getText());
        }

//            new Thread(etudeTask).start();
    }

    /**
     * Validates all inputs; if no fields are failing, it returns true and allows the etudeTask to run.
     * @return  Returns true if inputs are valid; false otherwise and etude won't run.
     */
    private boolean checkInputs() {
        boolean passes = true;

        if (failingFields.size() != 0) {
            passes = false;
        }
        
        return passes;
    }

    /**
     * Checks the directory to which etude will save its output.  Specifically, it checks whether
     * "test" and "reference" directories exist.  If either exists and has files, it will return false.
     * It also checks whether a corpus output file already exists; if so, it will also return false.
     * @return  Returns true if both test and reference directories are clean and no corpus file exists.
     */
    private boolean checkOutputDirectory() {
        File directory = new File(outputDirectoryTextField.getText());

        File testDirectory = new File(directory.getAbsolutePath() + "/test");
        File referenceDirectory = new File(directory.getAbsolutePath() + "/reference");
        File corpusFile = new File(directory.getAbsolutePath() + "/corpus.json");

        boolean isDirectoryClean = true;

        if (testDirectory.exists() && testDirectory.isDirectory() && testDirectory.length() > 0) {
            isDirectoryClean = false;
            logger.warn("Test directory already exists and contains data");
        } else {
            if (!testDirectory.mkdir()) {
                logger.throwing(new FileSystemException(
                        String.format("Unable to create file: %s", testDirectory.getAbsolutePath())));
            }
        }

        if (referenceDirectory.exists() && referenceDirectory.isDirectory() & referenceDirectory.length() > 0) {
            isDirectoryClean = false;
            logger.warn("Reference directory already exists");
        } else {
            if (!referenceDirectory.mkdir()) {
                logger.throwing(new FileSystemException(
                        String.format("Unable to create file: %s", referenceDirectory.getAbsolutePath())));
            }
        }

        if (corpusFile.exists() && corpusFile.isFile()) {
            isDirectoryClean = false;
            logger.warn("Corpus output file already exists");
        }


        return isDirectoryClean;
    }

    private void confirmEtudeOverwrite() {
        StringBuilder message = new StringBuilder();
        message.append("The selected output directory contains files which may conflict with ETUDE.  ");
        message.append("Files would not be deleted, but they would be overwritten with new data.  ");
        message.append("\n\nDo you want to proceed?");

        ConfirmationModal.createModal("Overwrite files?", message.toString(), "YES", "NO");
        ConfirmationModal.setConfirmAction(event -> runEtude());
        ConfirmationModal.setCancelAction(event -> logger.warn("User cancelled Etude due to conflicting files."));
        ConfirmationModal.show();
    }

    private void focusChanged(boolean focusGained, JFXTextField field, boolean checkForFile) {
        HBox box = (HBox)field.getParent();
        Label label;
        String currentId = box.getChildren().get(box.getChildren().size() - 1).getId();

        if (currentId == null || !currentId.equals(ERROR_ID)) {
            label = new Label();
            label.setId(ERROR_ID);
            label.getStyleClass().add("text-medium-normal");
            label.setStyle("-fx-text-fill: Red;");
        } else {
            label = (Label)box.getChildren().get(box.getChildren().size() - 1);
        }

        boolean isValid = true;
        String labelText = null;
        if (!focusGained) {
            File file = new File(field.getText());
            if (checkForFile) {
                if (!file.exists()) {
                    labelText = "\u2022 File doesn't exist!";
                    isValid = false;
                } else {
                    if (!file.isFile()) {
                        labelText = "\u2022 Invalid file!";
                        isValid = false;
                    }
                }
            } else {
                if (!file.exists()) {
                    labelText = "\u2022 File doesn't exist!";
                    isValid = false;
                } else {
                    if (!file.isDirectory()) {
                        labelText = "\u2022 Invalid directory!";
                        isValid = false;
                    }
                }
            }
        }

        label.setText(labelText);
        if (isValid) {
            if (box.getChildren().contains(label)) {
                box.getChildren().remove(label);
            }

            if (failingFields.contains(field)) {
                failingFields.remove(field);
            }
        } else {
            if (!box.getChildren().contains(label)) {
                box.getChildren().add(label);
            }

            if (!failingFields.contains(field)) {
                failingFields.add(field);
            }
        }
    }

    @FXML private void pickGoldConfigFile() {
        File file = getFile("Gold Standard Configuration File");
        if (file != null) {
            goldConfigInputField.setText(file.getAbsolutePath());
        }
    }

    @FXML private void pickTestConfigFile() {
        File file = getFile("Test Configuration File");
        if (file != null) {
            testConfigInputField.setText(file.getAbsolutePath());
        }
    }

    @FXML private void pickGoldInDirectory() {
        File directory = getDirectory("Gold Standard input files");
        if (directory != null) {
            goldInputTextField.setText(directory.getAbsolutePath());
        }
    }

    @FXML private void pickTestInDirectory() {
        File directory = getDirectory("Test Input Files");
        if (directory != null) {
            testInputTextField.setText(directory.getAbsolutePath());
        }
    }

    @FXML private void pickGoldOutDirectory() {
        File directory = getDirectory("Gold Standard Output Directory");
        if (directory != null) {
            outputDirectoryTextField.setText(directory.getAbsolutePath());
        }
    }

    private File getFile(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        return chooser.showOpenDialog(AcvContext.getInstance().mainWindow);
    }

    private File getDirectory(String title) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);
        return chooser.showDialog(AcvContext.getInstance().mainWindow);
    }
}
