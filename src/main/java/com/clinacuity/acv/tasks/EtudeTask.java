package com.clinacuity.acv.tasks;

import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class EtudeTask extends Task<Void> {
    private static final Logger logger = LogManager.getLogger();

    private String goldConfigFilePath = null;
    private String testConfigFilePath = null;
    private String goldInputDirPath = null;
    private String testInputDirPath = null;
    private String goldOutputDirPath = null;
    private String testOutputDirPath = null;
    private String corpusFilePath = null;
    private String scoreKey = null;
    private String scoreValues = null;
    private String filePrefix = null;
    private String fileSuffix = null;
    private String fuzzyMatchFlags = null;
    private boolean byFile = false;
    private boolean byFileAndType = false;
    private boolean byType = false;
    private boolean byTypeAndFile = false;
    private boolean ignoreWhitespace = false;

    public void setGoldConfigFilePath(String value) { goldConfigFilePath = value; }
    public void setTestConfigFilePath(String value) { testConfigFilePath = value; }
    public void setGoldInputDirPath(String value) { goldInputDirPath = value; }
    public void setTestInputDirPath(String value) { testInputDirPath = value; }
    public void setGoldOutputDirPath(String value) { goldOutputDirPath = value; }
    public void setTestOutputDirPath(String value) { testOutputDirPath = value; }
    public void setCorpusFilePath(String value) { corpusFilePath = value; }
    public void setScoreKey(String value) { scoreKey = value; }
    public void setScoreValues(String value) { scoreValues = value; }
    public void setFilePrefix(String value) { filePrefix = value; }
    public void setFileSuffix(String value) { fileSuffix = value; }
    public void setFuzzyMatchFlags(String value) { fuzzyMatchFlags = value; }
    public void setByFile(boolean value) { byFile = value; }
    public void setByFileAndType(boolean value) { byFileAndType= value; }
    public void setByType(boolean value) { byType = value; }
    public void setByTypeAndFile(boolean value) { byTypeAndFile = value; }
    public void setIgnoreWhitespace(boolean value) { ignoreWhitespace = value; }

    @Override
    public Void call() {
        logger.error(goldConfigFilePath);
        logger.error(testConfigFilePath);
        logger.error(goldInputDirPath);
        logger.error(testInputDirPath);
        logger.error(goldOutputDirPath);
        logger.error(testOutputDirPath);
        logger.error(corpusFilePath);
        logger.error(scoreKey);
        logger.error(scoreValues);
        logger.error(filePrefix);
        logger.error(fileSuffix);
        logger.error(fuzzyMatchFlags);
        logger.error(byFile);
        logger.error(byFileAndType);
        logger.error(byType);
        logger.error(byTypeAndFile);
        logger.error(ignoreWhitespace);

        // TODO: this is TBD while we figure out how to run ETUDE correctly
//        try {
//            String command = getCommand();
//
//            logger.error("Running: <{}>", command);
//            Process etude = Runtime.getRuntime().exec(command);
//            etude.waitFor();
//
//            BufferedReader inStream = new BufferedReader(new InputStreamReader(etude.getInputStream()));
//            BufferedReader errStream = new BufferedReader(new InputStreamReader(etude.getErrorStream()));
//
//            String line;
//            while ((line = inStream.readLine()) != null) {
//                logger.warn(line);
//            }
//
//            while ((line = errStream.readLine()) != null) {
//                logger.warn(line);
//            }
//        } catch (IOException e) {
//            logger.throwing(e);
//            setException(e);
//            failed();
//        } catch (InterruptedException e) {
//            logger.warn("ETUDE was interrupted: {}", e);
//            cancelled();
//        }

        succeeded();
        return null;
    }
    
    public void reset() {
        goldConfigFilePath = null;
        testConfigFilePath = null;
        goldInputDirPath = null;
        testInputDirPath = null;
        goldOutputDirPath = null;
        testOutputDirPath = null;
        corpusFilePath = null;
        scoreKey = null;
        scoreValues = null;
        filePrefix = null;
        fileSuffix = null;
        fuzzyMatchFlags = null;
        byFile = false;
        byFileAndType = false;
        byType = false;
        byTypeAndFile = false;
        ignoreWhitespace = false;
    }

    private String getCommand() {
        String command = "./etude/dist/etude";

        if (goldConfigFilePath != null) {
            command += " --gold-config " + goldConfigFilePath;
        } else {
            setFailing(new MissingArgumentException("--gold-config"));
        }

        if (testConfigFilePath != null) {
            command += " --test-config " + testConfigFilePath;
        } else {
            setFailing(new MissingArgumentException("--test-config"));
        }

        if (goldInputDirPath != null) {
            command += " --gold-input " + goldInputDirPath;
        } else {
            setFailing(new MissingArgumentException("--gold-input"));
        }

        if (testInputDirPath != null) {
            command += " --test-input " + goldInputDirPath;
        } else {
            setFailing(new MissingArgumentException("--test-input"));
        }

        if (goldOutputDirPath != null) {
            command += " --gold-out " + goldOutputDirPath;
        } else {
            setFailing(new MissingArgumentException("--gold-out"));
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

        if (fuzzyMatchFlags != null) {
            command += " --fuzzy-match-flags " + fuzzyMatchFlags;
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

        return command;
    }

    private void setFailing(Exception exception) {
        logger.throwing(exception);
        setException(exception);
        failed();
    }

    private class MissingArgumentException extends Exception {
        MissingArgumentException(String missingArgument) {
            String message = "The argument <" + missingArgument + "> is required!";
            updateMessage(message);
        }
    }
}
