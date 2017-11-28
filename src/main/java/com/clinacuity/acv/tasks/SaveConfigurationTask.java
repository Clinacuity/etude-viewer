package com.clinacuity.acv.tasks;

import com.clinacuity.acv.controls.AnnotationDropBox;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

public class SaveConfigurationTask extends Task<Void> {
    private static final Logger logger = LogManager.getLogger();

    private List<AnnotationDropBox.Attribute> attributeList;
    private File targetDirectory;

    public SaveConfigurationTask(List<AnnotationDropBox.Attribute> attributes, File directory) {
        attributeList = attributes;
        targetDirectory = directory;
    }

    @Override
    public Void call() {
        File systemFile = getFile("/system.conf");
        File referenceFile = getFile("/reference.conf");

        for (AnnotationDropBox.Attribute attribute: attributeList) {
            logger.error("{}, {}, {}, {}", attribute.name, attribute.systemValue, attribute.referenceValue, attribute.isLocked);
        }


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
}
