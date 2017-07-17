package com.clinacuity.acv.context;

import com.clinacuity.acv.controllers.AppMainController;
import com.google.gson.JsonObject;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Resource;
import java.io.*;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class AcvContext {
    private static final Logger logger = LogManager.getLogger();
    private static final String propertiesFileName = "/config_en.properties";
    private static File propertiesFile;
    private static boolean propertiesActive = false;

    private static AcvContext instance;
    public static AcvContext getInstance() {
        if (instance == null) {
            new AcvContext();
        }
        return instance;
    }

    public AppMainController mainController;
    private Properties properties;
    public Properties getProperties() { return properties; }

    // Properties
    public StringProperty referenceDocumentPathProperty = new SimpleStringProperty("");
    public StringProperty targetDocumentPathProperty = new SimpleStringProperty("");
    public StringProperty selectedAnnotationTypeProperty = new SimpleStringProperty("");
    public BooleanProperty exactMatchesProperty = new SimpleBooleanProperty(true);
    public BooleanProperty exactFeatureMismatchProperty = new SimpleBooleanProperty(true);
    public BooleanProperty overlappingMatchesProperty = new SimpleBooleanProperty(true);
    public BooleanProperty subsumedMatchesProperty = new SimpleBooleanProperty(true);
    public BooleanProperty noMatchesProperty = new SimpleBooleanProperty(true);

    public ListProperty<String> annotationList = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final String defaultSelectedAnnotation = "Select Annotation...";
    public final String getDefaultSelectedAnnotation() { return defaultSelectedAnnotation; }

    private AcvContext() {
        instance = this;

        initProperties();
        addProperty("exactMatch", true);

        annotationList.add(defaultSelectedAnnotation);

        referenceDocumentPathProperty.addListener((observable, oldValue, newValue) -> cleanupAnnotationList());
        targetDocumentPathProperty.addListener((observable, oldValue, newValue) -> cleanupAnnotationList());
    }

    private void cleanupAnnotationList() {
        if (annotationList.size() > 1) {
            annotationList.remove(1, annotationList.size() - 1);
            selectedAnnotationTypeProperty.setValue(annotationList.get(0));
        }
    }

    private void initProperties() {
        properties = new Properties();
        propertiesFile = new File(getClass().getResource(propertiesFileName).toExternalForm());
        if (propertiesFile.exists()) {
            try {
                properties.load(new FileReader(propertiesFile));
            } catch (IOException e) {
                logger.throwing(e);
            }
        }
    }

    private static void addProperty(Object key, Object value) {
        instance.properties.put(key, value);

        if (propertiesActive) {
            try {
                instance.properties.store(
                        new FileWriter(AcvContext.class.getResource(propertiesFileName).toExternalForm()), "");
            } catch (IOException e) {
                logger.throwing(e);
            }
        }
    }
}
