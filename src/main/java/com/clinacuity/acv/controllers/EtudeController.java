package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.context.AppMain;
import com.clinacuity.acv.tasks.EtudeTask;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class EtudeController implements Initializable{
    private static final Logger logger = LogManager.getLogger();
    private static EtudeTask etudeTask = null;
    private static final String ERROR_ID = "errLabel";

    @FXML private GridPane grid;
    @FXML private JFXTextField goldConfigInputField;
    @FXML private JFXTextField testConfigInputField;
    @FXML private JFXTextField goldInputTextField;
    @FXML private JFXTextField testInputTextField;
    @FXML private JFXTextField goldOutputTextField;
    @FXML private JFXTextField testOutputTextField;
    @FXML private JFXTextField corpusOutputTextField;
    @FXML private JFXTextField scoreKeyTextField;
    @FXML private JFXTextField scoreValuesTextField;
    @FXML private JFXTextField filePrefixTextField;
    @FXML private JFXTextField fileSuffixTextField;
    @FXML private JFXCheckBox metricsTP;
    @FXML private JFXCheckBox metricsFP;
    @FXML private JFXCheckBox metricsFN;
    @FXML private JFXCheckBox metricsPrecision;
    @FXML private JFXCheckBox metricsRecall;
    @FXML private JFXCheckBox metricsSensitivity;
    @FXML private JFXCheckBox metricsSpecificity;
    @FXML private JFXCheckBox metricsAccuracy;
    @FXML private JFXCheckBox metricsF1;

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
        metricsSensitivity.setSelected(Boolean.valueOf(AppMain.properties.getProperty("SENSITIVITY")));
        metricsSpecificity.setSelected(Boolean.valueOf(AppMain.properties.getProperty("SPECIFICITY")));
        metricsAccuracy.setSelected(Boolean.valueOf(AppMain.properties.getProperty("ACCURACY")));
        metricsF1.setSelected(Boolean.valueOf(AppMain.properties.getProperty("F1")));
        byFileCheckbox.setSelected(Boolean.valueOf(AppMain.properties.getProperty("BYFILE")));
        byFileAndTypeCheckbox.setSelected(Boolean.valueOf(AppMain.properties.getProperty("BYFILEANDTYPE")));
        byTypeCheckbox.setSelected(Boolean.valueOf(AppMain.properties.getProperty("BYTYPE")));
        byTypeAndFileCheckbox.setSelected(Boolean.valueOf(AppMain.properties.getProperty("BYTYPEANDFILE")));
        ignoreWhitespaceCheckbox.setSelected(Boolean.valueOf(AppMain.properties.getProperty("IGNOREWHITESPACE")));
        goldConfigInputField.focusedProperty().addListener(
                (obs, old, newValue) -> focusChanged(newValue, goldConfigInputField, true));
        testConfigInputField.focusedProperty().addListener(
                (obs, old, newValue) -> focusChanged(newValue, testConfigInputField, true));
        goldInputTextField.focusedProperty().addListener(
                (obs, old, newValue) -> focusChanged(newValue, goldInputTextField, false));
        testInputTextField.focusedProperty().addListener(
                (obs, old, newValue) -> focusChanged(newValue, testInputTextField, false));
        goldOutputTextField.focusedProperty().addListener(
                (obs, old, newValue) -> focusChanged(newValue, goldOutputTextField, false));
        testOutputTextField.focusedProperty().addListener(
                (obs, old, newValue) -> focusChanged(newValue, testOutputTextField, false));
        corpusOutputTextField.focusedProperty().addListener(
                (obs, old, newValue) -> focusChanged(newValue, corpusOutputTextField, true));
    }

    @FXML private void runEtude() {
        if (etudeTask == null) {
            etudeTask = new EtudeTask();
        }

        if (etudeTask.isRunning()) {
            etudeTask.cancel();
        }

        etudeTask.reset();

//        if (checkInputs()) {
        try {
            FileOutputStream out = new FileOutputStream("src/main/resources/config_en.properties");
            AppMain.properties.setProperty("TP", String.valueOf(metricsTP.isSelected()));
            AppMain.properties.setProperty("FP", String.valueOf(metricsFP.isSelected()));
            AppMain.properties.setProperty("FN", String.valueOf(metricsFN.isSelected()));
            AppMain.properties.setProperty("PRECISION", String.valueOf(metricsPrecision.isSelected()));
            AppMain.properties.setProperty("RECALL", String.valueOf(metricsRecall.isSelected()));
            AppMain.properties.setProperty("SENSITIVITY", String.valueOf(metricsSensitivity.isSelected()));
            AppMain.properties.setProperty("SPECIFICITY", String.valueOf(metricsSpecificity.isSelected()));
            AppMain.properties.setProperty("ACCURACY", String.valueOf(metricsAccuracy.isSelected()));
            AppMain.properties.setProperty("F1", String.valueOf(metricsF1.isSelected()));
            AppMain.properties.setProperty("BYFILE", String.valueOf(byFileCheckbox.isSelected()));
            AppMain.properties.setProperty("BYFILEANDTYPE", String.valueOf(byFileAndTypeCheckbox.isSelected()));
            AppMain.properties.setProperty("BYTYPE", String.valueOf(byTypeCheckbox.isSelected()));
            AppMain.properties.setProperty("BYTYPEANDFILE", String.valueOf(byTypeAndFileCheckbox.isSelected()));
            AppMain.properties.setProperty("IGNOREWHITESPACE", String.valueOf(ignoreWhitespaceCheckbox.isSelected()));
            AppMain.properties.store(out, null);



            etudeTask.setGoldConfigFilePath(goldConfigInputField.getText());
            etudeTask.setTestConfigFilePath(testConfigInputField.getText());
            etudeTask.setGoldInputDirPath(goldInputTextField.getText());
            etudeTask.setTestInputDirPath(testInputTextField.getText());
            etudeTask.setGoldOutputDirPath(goldOutputTextField.getText());
            etudeTask.setTestOutputDirPath(testOutputTextField.getText());
            etudeTask.setCorpusFilePath(corpusOutputTextField.getText());
            etudeTask.setMetricsTP(Boolean.valueOf(AppMain.properties.getProperty("TP")));
            etudeTask.setMetricsFP(metricsFP.isSelected());
            etudeTask.setMetricsFN(metricsFN.isSelected());
            etudeTask.setMetricsPrecision(metricsPrecision.isSelected());
            etudeTask.setMetricsRecall(metricsRecall.isSelected());
            etudeTask.setMetricsSensitivity(metricsSensitivity.isSelected());
            etudeTask.setMetricsSpecificity(metricsSpecificity.isSelected());
            etudeTask.setMetricsAccuracy(metricsAccuracy.isSelected());
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
        } catch (Exception e) {

        }

            new Thread(etudeTask).start();
//        }
    }

    /**
     * Validates all inputs; if yes, it returns true and allows the etudeTask to run.
     * @return  Returns true if inputs are valid; false otherwise and etude won't run.
     */
    private boolean checkInputs() {
        boolean passes = true;

        if (failingFields.size() != 0) {
            passes = false;
        }
        
        return passes;
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
            goldOutputTextField.setText(directory.getAbsolutePath());
        }
    }

    @FXML private void pickTestOutDirectory() {
        File directory = getDirectory("Test Output Directory");
        if (directory != null) {
            testOutputTextField.setText(directory.getAbsolutePath());
        }
    }

    @FXML private void pickCorpusOutFile() {
        File directory = getDirectory("Choose directory in which to save Corpus dictionary");
        if (directory != null) {
            corpusOutputTextField.setText(directory.getAbsolutePath() + "/corpus.json");

            File file = new File(corpusOutputTextField.getText());
            if (file.exists()) {
                logger.warn("THE FILE EXISTS! Currently... it'll just be overwritten");
            }
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
