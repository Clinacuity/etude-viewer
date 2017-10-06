package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.modals.ConfirmationModal;
import com.clinacuity.acv.context.AppMain;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class EtudeController implements Initializable{
    private static final Logger logger = LogManager.getLogger();

    public static final String REFERENCE_SUBDIR = "reference/";
    public static final String SYSTEM_OUT_SUBDIR = "system/";
    public static final String CORPUS_FILE = "corpus.json";

    private static final String ERROR_ID = "errLabel";
    private static EtudeTask etudeTask = null;

    @FXML private JFXTextField referenceConfigInputField;
    @FXML private JFXTextField testConfigInputField;
    @FXML private JFXTextField referenceInputTextField;
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

    @FXML private JFXCheckBox fuzzyMatchingCheckbox;
    @FXML private JFXCheckBox byFileCheckbox;
    @FXML private JFXCheckBox byFileAndTypeCheckbox;
    @FXML private JFXCheckBox byTypeCheckbox;
    @FXML private JFXCheckBox byTypeAndFileCheckbox;
    @FXML private JFXCheckBox ignoreWhitespaceCheckbox;


    private List<JFXTextField> failingFields = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        metricsTP.setSelected(Boolean.valueOf(AppMain.properties.getProperty("TP")));
        metricsFP.setSelected(Boolean.valueOf(AppMain.properties.getProperty("FP")));
        metricsFN.setSelected(Boolean.valueOf(AppMain.properties.getProperty("FN")));
        metricsPrecision.setSelected(Boolean.valueOf(AppMain.properties.getProperty("PRECISION")));
        metricsRecall.setSelected(Boolean.valueOf(AppMain.properties.getProperty("RECALL")));
        metricsF1.setSelected(Boolean.valueOf(AppMain.properties.getProperty("F1")));
        fuzzyMatchingCheckbox.setSelected(Boolean.valueOf(AppMain.properties.getProperty("FUZZYMATCHING")));
        byFileCheckbox.setSelected(Boolean.valueOf(AppMain.properties.getProperty("BYFILE")));
        byFileAndTypeCheckbox.setSelected(Boolean.valueOf(AppMain.properties.getProperty("BYFILEANDTYPE")));
        byTypeCheckbox.setSelected(Boolean.valueOf(AppMain.properties.getProperty("BYTYPE")));
        byTypeAndFileCheckbox.setSelected(Boolean.valueOf(AppMain.properties.getProperty("BYTYPEANDFILE")));
        ignoreWhitespaceCheckbox.setSelected(Boolean.valueOf(AppMain.properties.getProperty("IGNOREWHITESPACE")));
        referenceConfigInputField.focusedProperty().addListener(
                (obs, old, newValue) -> focusChanged(newValue, referenceConfigInputField, true));
        testConfigInputField.focusedProperty().addListener(
                (obs, old, newValue) -> focusChanged(newValue, testConfigInputField, true));
        referenceInputTextField.focusedProperty().addListener(
                (obs, old, newValue) -> focusChanged(newValue, referenceInputTextField, false));
        testInputTextField.focusedProperty().addListener(
                (obs, old, newValue) -> focusChanged(newValue, testInputTextField, false));
        outputDirectoryTextField.focusedProperty().addListener(
                (obs, old, newValue) -> focusChanged(newValue, outputDirectoryTextField, false));
    }

    @FXML private void runEtudeButtonAction() throws IOException {
        if (checkInputs()) {
            FileOutputStream out = new FileOutputStream("config_en.properties");
            AppMain.properties.setProperty("TP", String.valueOf(metricsTP.isSelected()));
            AppMain.properties.setProperty("FP", String.valueOf(metricsFP.isSelected()));
            AppMain.properties.setProperty("FN", String.valueOf(metricsFN.isSelected()));
            AppMain.properties.setProperty("PRECISION", String.valueOf(metricsPrecision.isSelected()));
            AppMain.properties.setProperty("RECALL", String.valueOf(metricsRecall.isSelected()));
            AppMain.properties.setProperty("F1", String.valueOf(metricsF1.isSelected()));
            AppMain.properties.setProperty("FUZZYMATCHING", String.valueOf(fuzzyMatchingCheckbox.isSelected()));
            AppMain.properties.setProperty("BYFILE", String.valueOf(byFileCheckbox.isSelected()));
            AppMain.properties.setProperty("BYFILEANDTYPE", String.valueOf(byFileAndTypeCheckbox.isSelected()));
            AppMain.properties.setProperty("BYTYPE", String.valueOf(byTypeCheckbox.isSelected()));
            AppMain.properties.setProperty("BYTYPEANDFILE", String.valueOf(byTypeAndFileCheckbox.isSelected()));
            AppMain.properties.setProperty("IGNOREWHITESPACE", String.valueOf(ignoreWhitespaceCheckbox.isSelected()));
            AppMain.properties.store(out, null);

            if (checkOutputDirectory()) {
                runEtude();
            } else {
                confirmEtudeOverwrite();
            }
        }
    }

    private void runEtude() {
        if (etudeTask != null && etudeTask.isRunning()) {
            etudeTask.cancel();
        }

        etudeTask = new EtudeTask();

        etudeTask.reset();

        etudeTask.setReferenceConfigFilePath(referenceConfigInputField.getText());
        etudeTask.setTestConfigFilePath(testConfigInputField.getText());
        etudeTask.setReferenceInputDirPath(referenceInputTextField.getText());
        etudeTask.setTestInputDirPath(testInputTextField.getText());
        etudeTask.setOutputDirectory(outputDirectoryTextField.getText());
        etudeTask.setMetricsTP(Boolean.valueOf(AppMain.properties.getProperty("TP")));
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

        new Thread(etudeTask).start();
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

        File testDirectory = new File(directory.getAbsolutePath() + "/" + SYSTEM_OUT_SUBDIR);
        File referenceDirectory = new File(directory.getAbsolutePath() + "/" + REFERENCE_SUBDIR);
        File corpusFile = new File(directory.getAbsolutePath() + "/" + CORPUS_FILE);

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
            label.getStyleClass().addAll("text-medium-normal", "error-text");
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

    @FXML private void pickReferenceConfigFile() {
        File file = getFile("Reference Standard Configuration File");
        if (file != null) {
            referenceConfigInputField.setText(file.getAbsolutePath());
        }
    }

    @FXML private void pickTestConfigFile() {
        File file = getFile("Test Configuration File");
        if (file != null) {
            testConfigInputField.setText(file.getAbsolutePath());
        }
    }

    @FXML private void pickReferenceInDirectory() {
        File directory = getDirectory("Reference Standard input files");
        if (directory != null) {
            referenceInputTextField.setText(directory.getAbsolutePath());
        }
    }

    @FXML private void pickTestInDirectory() {
        File directory = getDirectory("Test Input Files");
        if (directory != null) {
            testInputTextField.setText(directory.getAbsolutePath());
        }
    }

    @FXML private void pickMainOutputDirectory() {
        File directory = getDirectory("Reference Standard Output Directory");
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
