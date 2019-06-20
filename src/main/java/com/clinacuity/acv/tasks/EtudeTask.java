package com.clinacuity.acv.tasks;

import com.clinacuity.acv.controllers.EtudeController;
import com.clinacuity.acv.modals.WarningModal;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystemException;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.List;

public class EtudeTask extends Task<Void> {
    private static final Logger logger = LogManager.getLogger();
    private static final String OS = System.getProperty("os.name").toLowerCase();

    private Process etudeProcess;
    private boolean killedByUser = false;

    private String referenceConfigFilePath = null;
    private String testConfigFilePath = null;
    private String referenceInputDirPath = null;
    private String testInputDirPath = null;
    private String mainOutputDirPath = null;
    private String referenceOutputDirPath = null;
    private String testOutputDirPath = null;
    private String corpusFilePath = null;
    private String scoreKey = null;
    private String scoreValues = null;
    private String filePrefix = null;
    private String fileSuffix = null;
    private String ignorePunctuationRegex = null;
    private boolean metricsTP = false;
    private boolean metricsFP = false;
    private boolean metricsFN = false;
    private boolean metricsPrecision = false;
    private boolean metricsRecall = false;
    private boolean metricsF1 = false;
    private boolean exactMatch = false;
    private boolean partialMatch = false;
    private boolean fullyContainedMatch = false;
    private boolean byFile = false;
    private boolean byFileAndType = false;
    private boolean byType = false;
    private boolean byTypeAndFile = false;
    private boolean ignoreWhitespace = false;
    private boolean ignorePunctuation = false;
    private String errorString = "";

    public EtudeTask() {
        setOnCancelled(event -> {
            if (etudeProcess != null) {
                killedByUser = true;
                etudeProcess.destroyForcibly();
            }
        });
    }

    public void setReferenceConfigFilePath(String value) {
        referenceConfigFilePath = value;
    }

    public void setTestConfigFilePath(String value) {
        testConfigFilePath = value;
    }

    public void setReferenceInputDirPath(String value) {
        referenceInputDirPath = value;
    }

    public void setTestInputDirPath(String value) {
        testInputDirPath = value;
    }

    public void setOutputDirectory(String value) {
        mainOutputDirPath = value;
    }

    public void setScoreKey(String value) {
        scoreKey = value;
    }

    public void setScoreValues(String value) {
        scoreValues = value;
    }

    public void setFilePrefix(String value) {
        filePrefix = value;
    }

    public void setFileSuffix(String value) {
        fileSuffix = value;
    }

    public void setIgnorePunctuationRegex(String value) {
        ignorePunctuationRegex = value;
    }

    public void setMetricsTP(boolean value) {
        metricsTP = value;
    }

    public void setMetricsFP(boolean value) {
        metricsFP = value;
    }

    public void setMetricsFN(boolean value) {
        metricsFN = value;
    }

    public void setMetricsPrecision(boolean value) {
        metricsPrecision = value;
    }

    public void setMetricsRecall(boolean value) {
        metricsRecall = value;
    }

    public void setMetricsF1(boolean value) {
        metricsF1 = value;
    }

    public void setExactMatch(boolean value) {
        exactMatch = value;
    }

    public void setPartialMatch(boolean value) {
        partialMatch = value;
    }

    public void setFullyContainedMatch(boolean value) {
        fullyContainedMatch = value;
    }

    public void setByFile(boolean value) {
        byFile = value;
    }

    public void setByFileAndType(boolean value) {
        byFileAndType = value;
    }

    public void setByType(boolean value) {
        byType = value;
    }

    public void setByTypeAndFile(boolean value) {
        byTypeAndFile = value;
    }

    public void setIgnoreWhitespace(boolean value) {
        ignoreWhitespace = value;
    }

    public void setIgnorePunctuation(boolean value) {
        ignorePunctuation = value;
    }

    public String getErrorString() {
        return errorString;
    }

    @Override
    protected Void call() {
        errorString = "";

        try {
            createOutputDirectories();
            if (!setPermissions()) {
                throw new EtudeEngineException("Can't set permissions on evaluation engine");
            }
            String command = getCommand();
            logger.warn("Running: <{}>", command);

            etudeProcess = Runtime.getRuntime().exec(command);
            etudeProcess.waitFor();

            BufferedReader inStream = new BufferedReader(new InputStreamReader(etudeProcess.getInputStream()));
            BufferedReader errStream = new BufferedReader(new InputStreamReader(etudeProcess.getErrorStream()));

            String line;
            while ((line = inStream.readLine()) != null) {
                logger.warn(line);
            }

            StringBuilder errBuilder = new StringBuilder();
            while ((line = errStream.readLine()) != null) {
                logger.error(line);
                errBuilder.append(line);
                errBuilder.append("\n");
            }

            errorString = errBuilder.toString();
            if (errorString.length() > 0) {
                throw new EtudeEngineException(errorString);
            }

        } catch (IOException e) {
            logger.throwing(e);
            setException(e);
            etudeProcess = null;
            failed();
        } catch (InterruptedException e) {
            etudeProcess = null;
            if (!killedByUser) {
                logger.warn("ETUDE was interrupted: {}", e);
            }
            cancel();
        }

        succeeded();
        return null;
    }

    public void reset() {
        killedByUser = false;

        referenceConfigFilePath = null;
        testConfigFilePath = null;
        referenceInputDirPath = null;
        testInputDirPath = null;
        mainOutputDirPath = null;
        referenceOutputDirPath = null;
        testOutputDirPath = null;
        corpusFilePath = null;
        scoreKey = null;
        scoreValues = null;
        filePrefix = null;
        fileSuffix = null;
        exactMatch = false;
        partialMatch = false;
        fullyContainedMatch = false;
        ignorePunctuationRegex = null;
        byFile = false;
        byFileAndType = false;
        byType = false;
        byTypeAndFile = false;
        ignoreWhitespace = false;
        ignorePunctuation = false;
    }

    private String getEtudeLocation() {
        if (OS.contains("mac")) {
            return "./data/etude/osx/etude";
        }

        if (OS.contains("win")) {
            return "./data/etude/windows/etude.exe";
        }

        if (OS.contains("nix") || OS.contains("nux") || OS.contains("aix")) {
            return "./data/etude/linux/etude";
        }

        return "";
    }

    private boolean setPermissions() {//not changing target but using it?
        File etudeFile = new File(getEtudeLocation());
        try {
            if (!etudeFile.setExecutable(true) || !etudeFile.setReadable(true)) {
                WarningModal.createModal("Permission Error", "Error trying to set permissions for evaluator");
                WarningModal.show();
                logger.error("Unable to set permissions");
                return false;
            }
        } catch (SecurityException e) {
            WarningModal.createModal("Permission Error", "Error trying to set permissions for evaluator");
            WarningModal.show();
            logger.throwing(e);
            return false;
        }
        return true;
    }

    private String getCommand() {
        String command = getEtudeLocation() + " --progressbar-output none";

        if (referenceConfigFilePath != null) {
            command += " --reference-config " + referenceConfigFilePath;
        } else {
            setFailing(new MissingArgumentException("--reference-config"));
        }

        if (testConfigFilePath != null) {
            command += " --test-config " + testConfigFilePath;
        } else {
            setFailing(new MissingArgumentException("--test-config"));
        }

        if (referenceInputDirPath != null) {
            command += " --reference-input " + referenceInputDirPath;
        } else {
            setFailing(new MissingArgumentException("--reference-input"));
        }

        if (testInputDirPath != null) {
            command += " --test-input " + testInputDirPath;
        } else {
            setFailing(new MissingArgumentException("--test-input"));
        }

        if (referenceOutputDirPath != null) {
            command += " --reference-out " + referenceOutputDirPath;
        } else {
            setFailing(new MissingArgumentException("--reference-out"));
        }

        if (testOutputDirPath != null) {
            command += " --test-out " + testOutputDirPath;
        } else {
            setFailing(new MissingArgumentException("--test-out"));
        }

        if (corpusFilePath != null) {
            command += " --corpus-out " + corpusFilePath;
        }

        if (scoreKey != null) {
            command += " --score-key " + scoreKey;
        }

        if (scoreValues != null) {
            command += " --score-values " + scoreValues;
        }

        if (filePrefix != null) {
            command += " --file-prefix " + filePrefix;
        }

        if (fileSuffix != null) {
            command += " --file-suffix " + fileSuffix;
        }

        if (byFile) {
            command += " --by-file";
        }

        if (byFileAndType) {
            command += " --by-file-and-type";
        }

        if (byType) {
            command += " --by-type";
        }

        if (byTypeAndFile) {
            command += " --by-type-and-file";
        }

        if (ignoreWhitespace) {
            command += " --ignore-whitespace";
        } else {
            command += " --heed-whitespace";
        }

        if (ignorePunctuation) {
            if (ignorePunctuationRegex != null) {
                command += " --skip-chars " + ignorePunctuationRegex;
            } else {
                command += " --skip-chars";
            }
        }

        command += getMetricsValues();

        command += getFuzzyMatchFlags();

        return command;
    }

    private void createOutputDirectories() {
        if (mainOutputDirPath != null) {
            if (mainOutputDirPath.charAt(mainOutputDirPath.length() - 1) != '/') {
                mainOutputDirPath += "/";
            }

            File outDir = new File(mainOutputDirPath);
            if (outDir.exists()) {
                if (outDir.isFile()) {
                    setFailing(new NotDirectoryException("The specified output directory is NOT a directory"));
                }
            } else {
                if (!outDir.mkdirs()) {
                    setFailing(new FileSystemException("Unable to create directory: " + mainOutputDirPath));
                }
            }

            referenceOutputDirPath = mainOutputDirPath + EtudeController.REFERENCE_SUBDIR;
            testOutputDirPath = mainOutputDirPath + EtudeController.SYSTEM_OUT_SUBDIR;
            corpusFilePath = mainOutputDirPath + EtudeController.CORPUS_FILE;

            File referenceDir = new File(referenceOutputDirPath);
            File systemDir = new File(testOutputDirPath);
            if (!referenceDir.exists()) {
                if (!referenceDir.mkdirs()) {
                    setFailing(new FileSystemException("Unable to create directory: " + referenceOutputDirPath));
                }
            }

            if (!systemDir.exists()) {
                if (!systemDir.mkdirs()) {
                    setFailing(new FileSystemException("Unable to create directory: " + testOutputDirPath));
                }
            }
        }
    }

    private String getMetricsValues() {
        List<String> metrics = new ArrayList<>();

        if (metricsTP) {
            metrics.add("TP");
        }
        if (metricsFP) {
            metrics.add("FP");
        }
        if (metricsFN) {
            metrics.add("FN");
        }
        if (metricsPrecision) {
            metrics.add("Precision");
        }
        if (metricsRecall) {
            metrics.add("Recall");
        }
        if (metricsF1) {
            metrics.add("F1");
        }

        return " -m " + String.join(" ", metrics);
    }

    private String getFuzzyMatchFlags() {
        String matches = "";

        if (exactMatch) {
            matches += " exact";
        }

        if (partialMatch) {
            matches += " partial";
        }

        if (fullyContainedMatch) {
            matches += " fully-contained";
        }

        return matches.length() > 0 ? " --fuzzy-match-flags " + matches : " --fuzzy-match-flags exact";
    }

    private void setFailing(Exception exception) {
        logger.throwing(exception);
        setException(exception);
        throw new RuntimeException(exception);
    }

    private class MissingArgumentException extends Exception {
        MissingArgumentException(String missingArgument) {
            String message = "The argument <" + missingArgument + "> is required!";
            updateMessage(message);
        }
    }

    private class EtudeEngineException extends RuntimeException {
        EtudeEngineException(String error) {
            updateMessage(error);
        }
    }
}
