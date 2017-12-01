package com.clinacuity.acv.tasks;

import com.clinacuity.acv.controls.AnnotationTypeDraggable;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateAnnotationDraggableTask extends Task<List<AnnotationTypeDraggable>> {
    private static final Logger logger = LogManager.getLogger();

    private String corpusType;
    private String directoryPath;
    private Map<String, List<String>> annotationMap = new HashMap<>();
    private List<AnnotationTypeDraggable> annotations = new ArrayList<>();

    public CreateAnnotationDraggableTask(String directory, String corpus) {
        corpusType = corpus;
        directoryPath = directory;
    }

    @Override
    public List<AnnotationTypeDraggable> call() {
        File directory = new File(directoryPath);

        if (directory.isFile()) {
            logger.error(new NotDirectoryException(directoryPath));
        } else {
            File[] files = directory.listFiles((f, name) -> name.toLowerCase().endsWith(".xml"));
            if (files != null && files.length > 0) {
                for (File file: files) {
                    parseFile(file);
                }
            } else {
                logger.error(new FileNotFoundException("Directory is empty"));
            }
        }

        for (String key: annotationMap.keySet()) {
            annotations.add(new AnnotationTypeDraggable(corpusType, key, annotationMap.get(key)));
        }

        succeeded();
        return annotations;
    }

    private void parseFile(File file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();

            Element root = document.getDocumentElement();
            collectElements(root);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.throwing(e);
        }
    }

    private void collectElements(Element element) {
        if (element.hasAttributes() && !annotationMap.containsKey(element.getNodeName())) {
            addAttributes(element);
        }

        if (element.hasChildNodes()) {
            NodeList children = element.getChildNodes();

            for (int child = 0; child < children.getLength(); child++) {
                if (children.item(child) instanceof Element) {
                    collectElements((Element)children.item(child));
                }
            }
        }
    }

    private void addAttributes(Element element) {
        NamedNodeMap attributes = element.getAttributes();

        if (attributes != null) {
            List<String> attributeNames = new ArrayList<>();
            for (int attributeIndex = 0; attributeIndex < attributes.getLength(); attributeIndex++) {
                attributeNames.add(attributes.item(attributeIndex).getNodeName());
            }

            annotationMap.put(element.getTagName(), attributeNames);
        }
    }
}
