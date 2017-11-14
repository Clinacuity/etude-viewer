package com.clinacuity.acv.tasks;

import com.clinacuity.acv.controls.AnnotationTypeDraggable;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.List;

public class CreateAnnotationDraggableTask extends Task<List<AnnotationTypeDraggable>> {
    private static final Logger logger = LogManager.getLogger();

    private String corpusType;

    public CreateAnnotationDraggableTask(String directoryPath, String corpus) {
        File directory = new File(directoryPath);

        if (directory.isFile()) {
            logger.error(new NotDirectoryException(directoryPath));
        } else {
            File[] files = directory.listFiles();
            if (files != null && files.length > 0) {
                for (File file: files) {
                    logger.error(file.getAbsolutePath());
                }
            } else {
                logger.error(new FileNotFoundException("Directory is empty"));
            }
        }

        corpusType = corpus;
    }

    @Override
    public List<AnnotationTypeDraggable> call() {
        AnnotationTypeDraggable item = new AnnotationTypeDraggable(corpusType);
        item.addAttribute();
        item.addAttribute();

        List<AnnotationTypeDraggable> list = new ArrayList<>();
        list.add(item);
        return list;
    }

    private void parseFile(File file) {

    }
}
