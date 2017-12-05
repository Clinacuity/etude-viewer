package com.clinacuity.acv.tasks;

import com.clinacuity.acv.controllers.ConfigurationBuilderController;
import com.clinacuity.acv.controls.AnnotationDropBox;
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

    private Map<String, List<AnnotationDropBox.Attribute>> sysAnnotationMatchMap;
    private Map<String, List<AnnotationDropBox.Attribute>> refAnnotationMatchMap;
    private Map<String, List<String>> systemXpathList;
    private Map<String, List<String>> referenceXpathList;
    private File targetDirectory;

    public SaveConfigurationTask(Map<String, List<AnnotationDropBox.Attribute>> sysList,
                                 Map<String, List<String>> sysXPaths,
                                 Map<String, List<AnnotationDropBox.Attribute>> refList,
                                 Map<String, List<String>> refXPaths,
                                 File directory) {
        sysAnnotationMatchMap = sysList;
        refAnnotationMatchMap = refList;
        targetDirectory = directory;
        systemXpathList = sysXPaths;
        referenceXpathList = refXPaths;
    }

    @Override
    public Void call() {
        File systemFile = getFile("/system.conf");
        File referenceFile = getFile("/reference.conf");

        saveXPaths(systemFile, ConfigurationBuilderController.CorpusType.SYSTEM, sysAnnotationMatchMap, systemXpathList);
        saveXPaths(referenceFile, ConfigurationBuilderController.CorpusType.REFERENCE, refAnnotationMatchMap, referenceXpathList);

//        logger.error(getState());
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

    private void saveXPaths(File file,
                            ConfigurationBuilderController.CorpusType corpusType,
                            Map<String, List<AnnotationDropBox.Attribute>> annotationMatchMap,
                            Map<String, List<String>> xpathMap) {
        StringBuilder text = new StringBuilder();

        // missing raw attribute list if it contains XPath
        xpathMap.forEach((annotationMatch, xpathList) -> {
            List<AnnotationDropBox.Attribute> attributes = annotationMatchMap.get(annotationMatch);
            xpathList.forEach(xpath -> {
                text.append("[ ");
                text.append(annotationMatch);
                text.append(" ]\nXPath: ");
                text.append(xpath);
                text.append("\n");

                attributes.forEach(attribute -> {
                    if (!attribute.name.equals("XPath")) {
                        text.append(attribute.name);
                        text.append(": ");
                        text.append(attribute.getValue(corpusType));
                        text.append("\n");
                    }
                });

                text.append("\n");
            });
        });

        annotationMatchMap.forEach((annotationMatch, attributes) -> {
            boolean hasXPath = false;
            for (AnnotationDropBox.Attribute attribute: attributes) {
                if (attribute.name.equals("XPath") && attribute.getValue(corpusType).length() > 0) {
                    hasXPath = true;
                }
            }
            if (hasXPath) {
                text.append("[ ");
                text.append(annotationMatch);
                text.append(" ]\n");

                attributes.forEach(attribute -> {
                    text.append(attribute.name);
                    text.append(": ");
                    text.append(attribute.getValue(corpusType));
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
