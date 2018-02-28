package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.modals.ConfirmationModal;
import com.clinacuity.acv.tasks.EtudeTask;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
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
    private AcvContext context = AcvContext.getInstance();

    @FXML private HBox cardHBox;
    @FXML private ScrollPane scrollPane;
    @FXML private StackPane leftSideCard;
    @FXML private StackPane rightSideCard;
    @FXML private JFXTextField referenceConfigInputField;
    @FXML private JFXTextField testConfigInputField;
    @FXML private JFXTextField referenceInputTextField;
    @FXML private JFXTextField testInputTextField;
    @FXML private JFXTextField outputDirectoryTextField;
    @FXML private JFXTextField scoreKeyTextField;
    @FXML private JFXTextField punctuationTextField;
    @FXML private Label scoreKeyError;
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
    @FXML private JFXCheckBox exactMatching;
    @FXML private JFXCheckBox partialMatching;
    @FXML private JFXCheckBox fullyContainedMatching;
    @FXML private JFXCheckBox byFileCheckbox;
    @FXML private JFXCheckBox byFileAndTypeCheckbox;
    @FXML private JFXCheckBox byTypeCheckbox;
    @FXML private JFXCheckBox byTypeAndFileCheckbox;
    @FXML private JFXCheckBox ignoreWhitespaceCheckbox;
    @FXML private JFXCheckBox ignorePunctuationCheckbox;

    // This could be refactored -- if the Run button is pressed without ever focusing on a required field,
    // it will "pass" and probably crash
    private List<JFXTextField> failingFields = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scrollPane.widthProperty().addListener((obs, old, newValue) -> {
            double width = newValue.doubleValue() - 60.0d;
            rightSideCard.setMinWidth(width * 0.5d);
            rightSideCard.setMaxWidth(width * 0.5d);
            leftSideCard.setMinWidth(width * 0.5d);
            leftSideCard.setMaxWidth(width * 0.5d);
            cardHBox.setSpacing(20.0d);
        });

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

        scoreKeyTextField.focusedProperty().addListener((obs, old, isFocused) -> {
            if (isFocused) {
                scoreKeyError.setVisible(false);
            } else {
                if (scoreKeyTextField.getText().equals("")) {
                    scoreKeyError.setVisible(true);
                } else {
                    scoreKeyError.setVisible(false);
                }
            }
        });

        exactMatching.disableProperty().bind(fuzzyMatchingCheckbox.selectedProperty().not());
        partialMatching.disableProperty().bind(fuzzyMatchingCheckbox.selectedProperty().not());
        fullyContainedMatching.disableProperty().bind(fuzzyMatchingCheckbox.selectedProperty().not());

        bindElementsToProperties();
    }

    @FXML private void runEtudeButtonAction() throws IOException {
        if (checkInputs()) {
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

        etudeTask.setOnSucceeded(event -> {
            context.corpusFilePathProperty.setValue(outputDirectoryTextField.getText() + "/" + CORPUS_FILE);
            context.targetDirectoryProperty.setValue(outputDirectoryTextField.getText() + "/" + SYSTEM_OUT_SUBDIR);
            context.referenceDirectoryProperty.setValue(outputDirectoryTextField.getText() + "/" + REFERENCE_SUBDIR);

            AcvContext.loadPage(NavBarController.NavBarPages.COMPARISON_VIEW);
            AcvContext.getInstance().contentLoading.setValue(false);
        });

        etudeTask.reset();

        etudeTask.setReferenceConfigFilePath(referenceConfigInputField.getText());
        etudeTask.setTestConfigFilePath(testConfigInputField.getText());
        etudeTask.setReferenceInputDirPath(referenceInputTextField.getText());
        etudeTask.setTestInputDirPath(testInputTextField.getText());
        etudeTask.setOutputDirectory(outputDirectoryTextField.getText());
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
        etudeTask.setIgnorePunctuation(ignorePunctuationCheckbox.isSelected());

        if (fuzzyMatchingCheckbox.isSelected()) {
            etudeTask.setExactMatch(exactMatching.isSelected());
            etudeTask.setPartialMatch(partialMatching.isSelected());
            etudeTask.setFullyContainedMatch(fullyContainedMatching.isSelected());
        }

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

        if (!punctuationTextField.getText().equals("") && punctuationTextField.getText() != null) {
            etudeTask.setIgnorePunctuationRegex(punctuationTextField.getText());
        }

        AcvContext.getInstance().contentLoading.setValue(true);
        new Thread(etudeTask).start();
    }

    /**
     * Validates all inputs; if no fields are failing, it returns true and allows the etudeTask to run.
     * @return  Returns true if inputs are valid; false otherwise and etude won't run.
     */
    private boolean checkInputs() {
        boolean passes = true;

        if (failingFields.size() != 0 || scoreKeyError.isVisible()) {
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
        String message = "The selected output directory contains files which may conflict with ETUDE.  ";
        message += "Files would not be deleted, but they would be overwritten with new data.  ";
        message += "\n\nDo you want to proceed?";

        ConfirmationModal.createModal("Overwrite files?", message, "YES", "NO");
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
            if (!field.getText().equals("")) {
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
            } else {
                isValid = false;
                labelText = "\u2022 Required field";
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
    
    private void bindElementsToProperties() {
        testConfigInputField.setText(AcvContext.getProperty("test_config", ""));
        testConfigInputField.textProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("test_config", newValue));

        referenceConfigInputField.setText(AcvContext.getProperty("ref_config", ""));
        referenceConfigInputField.textProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("ref_config", newValue));

        testInputTextField.setText(AcvContext.getProperty("test_directory", ""));
        testInputTextField.textProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("test_directory", newValue));

        referenceInputTextField.setText(AcvContext.getProperty("ref_directory", ""));
        referenceInputTextField.textProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("ref_directory", newValue));

        filePrefixTextField.setText(AcvContext.getProperty("file_prefix", ""));
        filePrefixTextField.textProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("file_prefix", newValue));
        
        fileSuffixTextField.setText(AcvContext.getProperty("file_suffix", ""));
        fileSuffixTextField.textProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("file_suffix", newValue));
        
        outputDirectoryTextField.setText(AcvContext.getProperty("output_directory", ""));
        outputDirectoryTextField.textProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("output_directory", newValue));

        scoreKeyTextField.setText(AcvContext.getProperty("score_key", ""));
        scoreKeyTextField.textProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("score_key", newValue));

        scoreValuesTextField.setText(AcvContext.getProperty("score_values", ""));
        scoreValuesTextField.textProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("score_values", newValue));

        metricsTP.setSelected(Boolean.parseBoolean(AcvContext.getProperty("tp", metricsTP.isSelected())));
        metricsTP.selectedProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("tp", newValue.toString()));

        metricsFP.setSelected(Boolean.parseBoolean(AcvContext.getProperty("fp", metricsFP.isSelected())));
        metricsFP.selectedProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("fp", newValue.toString()));

        metricsFN.setSelected(Boolean.parseBoolean(AcvContext.getProperty("fn", metricsFN.isSelected())));
        metricsFN.selectedProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("fn", newValue.toString()));

        metricsPrecision.setSelected(Boolean.parseBoolean(AcvContext.getProperty("precision", metricsPrecision.isSelected())));
        metricsPrecision.selectedProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("precision", newValue.toString()));

        metricsRecall.setSelected(Boolean.parseBoolean(AcvContext.getProperty("recall", metricsRecall.isSelected())));
        metricsRecall.selectedProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("recall", newValue.toString()));

        metricsF1.setSelected(Boolean.parseBoolean(AcvContext.getProperty("f1", metricsF1.isSelected())));
        metricsF1.selectedProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("f1", newValue.toString()));

        exactMatching.setSelected(Boolean.parseBoolean(AcvContext.getProperty("exact_matching", exactMatching.isSelected())));
        exactMatching.selectedProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("exact_matching", newValue.toString()));

        partialMatching.setSelected(Boolean.parseBoolean(AcvContext.getProperty("partial_matching", partialMatching.isSelected())));
        partialMatching.selectedProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("partial_matching", newValue.toString()));

        fullyContainedMatching.setSelected(Boolean.parseBoolean(AcvContext.getProperty("fully_contained_matching", fullyContainedMatching.isSelected())));
        fullyContainedMatching.selectedProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("fully_contained_matching", newValue.toString()));

        fuzzyMatchingCheckbox.setSelected(Boolean.parseBoolean(AcvContext.getProperty("fuzzy_matching", fuzzyMatchingCheckbox.isSelected())));
        fuzzyMatchingCheckbox.selectedProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("fuzzy_matching", newValue.toString()));

        byFileCheckbox.setSelected(Boolean.parseBoolean(AcvContext.getProperty("by_file", byFileCheckbox.isSelected())));
        byFileCheckbox.selectedProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("by_file", newValue.toString()));

        byFileAndTypeCheckbox.setSelected(Boolean.parseBoolean(AcvContext.getProperty("by_file_and_type", byFileAndTypeCheckbox.isSelected())));
        byFileAndTypeCheckbox.selectedProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("by_file_and_type", newValue.toString()));

        byTypeCheckbox.setSelected(Boolean.parseBoolean(AcvContext.getProperty("by_type", byTypeCheckbox.isSelected())));
        byTypeCheckbox.selectedProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("by_type", newValue.toString()));

        byTypeAndFileCheckbox.setSelected(Boolean.parseBoolean(AcvContext.getProperty("by_type_and_file", byTypeAndFileCheckbox.isSelected())));
        byTypeAndFileCheckbox.selectedProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("by_type_and_file", newValue.toString()));

        ignoreWhitespaceCheckbox.setSelected(Boolean.parseBoolean(AcvContext.getProperty("ignore_whitespace", ignoreWhitespaceCheckbox.isSelected())));
        ignoreWhitespaceCheckbox.selectedProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("ignore_whitespace", newValue.toString()));

        ignorePunctuationCheckbox.setSelected(Boolean.parseBoolean(AcvContext.getProperty("ignore_punctuation", ignorePunctuationCheckbox.isSelected())));
        ignorePunctuationCheckbox.selectedProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("ignore_punctuation", newValue.toString()));

        punctuationTextField.setText(AcvContext.getProperty("punctuation_regex", ""));
        punctuationTextField.textProperty().addListener((obs, old, newValue) -> AcvContext.setProperty("punctuation_regex", newValue));
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
        return chooser.showOpenDialog(AcvContext.getMainWindow());
    }

    private File getDirectory(String title) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);
        return chooser.showDialog(AcvContext.getMainWindow());
    }
}
