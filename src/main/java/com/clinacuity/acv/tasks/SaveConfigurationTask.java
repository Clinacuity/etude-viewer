package com.clinacuity.acv.tasks;

import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SaveConfigurationTask extends Task<Void> {
    private static final Logger logger = LogManager.getLogger();

    private Map<String, List<Map<String, String>>> sysAnnotationMatchMap;
    private Map<String, List<Map<String, String>>> refAnnotationMatchMap;
    private File targetDirectory;

    public SaveConfigurationTask(Map<String, List<Map<String, String>>> systemAnnotationMatches,
                                 Map<String, List<Map<String, String>>> referenceAnnotationMatches,
                                 File directory) {
        sysAnnotationMatchMap = systemAnnotationMatches;
        refAnnotationMatchMap = referenceAnnotationMatches;
        targetDirectory = directory;
    }

    @Override
    public Void call() {
        File systemFile = getFile("/system.conf");
        File referenceFile = getFile("/reference.conf");

        saveConfiguration(systemFile, sysAnnotationMatchMap);
        saveConfiguration(referenceFile, refAnnotationMatchMap);

        succeeded();
        return null;
    }

    private File getFile(String name) {
        // add any validation logic here
        File file = new File(targetDirectory.getAbsolutePath() + name);
        if (file.exists()) {
            logger.warn("A file already exists and will be overwritten in path: {}", file.getAbsoluteFile());
        }
        return file;
    }

    private void saveConfiguration(File file, Map<String, List<Map<String, String>>> annotationMatchMap) {
        StringBuilder text = new StringBuilder();

        annotationMatchMap.forEach((annotationMatch, cards) -> {
            for (int i = 0; i < cards.size(); i++) {
                text.append("[ Item");
                text.append(Integer.toString(i));
                text.append(" ]\nParent Name: ");
                text.append(annotationMatch);
                text.append("\n");

                cards.get(i).forEach((attribute, value) -> {
                    text.append(attribute);
                    text.append(": ");
                    text.append(value);
                    text.append("\n");
                });
                text.append("\n");
            }
        });

        try {
            FileUtils.writeStringToFile(file, text.toString());
        } catch (IOException e) {
            logger.error(e);
            failed();
        }
    }
}
