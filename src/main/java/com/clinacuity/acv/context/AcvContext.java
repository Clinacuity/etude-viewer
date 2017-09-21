package com.clinacuity.acv.context;

import com.clinacuity.acv.controllers.AppMainController;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.util.Properties;
import java.util.ResourceBundle;

public class AcvContext {
    public static final String LOAD_SCREEN = "/pages/LoadScreenView.fxml";
    public static final String COMPARISON_VIEW = "/pages/AcvContent.fxml";
    public static final String APP_MAIN_VIEW = "/pages/AppMain.fxml";
    public static final String MENU_BAR = "/pages/MenuBar.fxml";
    public static final String ETUDE_RUNNER = "/pages/EtudeRunner.fxml";
    public static final String CONFIGURATION_BUILDER = "/pages/ConfigurationBuilder.fxml";

    private static final Logger logger = LogManager.getLogger();
    private static final String propertiesFileName = "/config_en.properties";
    private static File propertiesFile;
    private static boolean propertiesActive = false;
    private static ResourceBundle resources;
    public void setResources(ResourceBundle bundle) { resources = bundle; }

    private static AcvContext instance;
    public static AcvContext getInstance() {
        if (instance == null) {
            new AcvContext();
        }
        return instance;
    }

    public AppMainController mainController;
    public Window mainWindow;

    private Properties properties;
    public Properties getProperties() { return properties; }

    private CorpusDictionary corpusDictionary;
    public CorpusDictionary getCorpusDictionary() { return corpusDictionary; }

    // Properties
    public StringProperty referenceDocumentPathProperty = new SimpleStringProperty("");
    public StringProperty targetDocumentPathProperty = new SimpleStringProperty("");
    public StringProperty referenceDirectoryProperty = new SimpleStringProperty("");
    public StringProperty targetDirectoryProperty = new SimpleStringProperty("");
    public StringProperty selectedAnnotationTypeProperty = new SimpleStringProperty("");
    public StringProperty selectedMatchTypeProperty = new SimpleStringProperty("exact");
    public StringProperty corpusFilePathProperty = new SimpleStringProperty("");
    public BooleanProperty exactMatchesProperty = new SimpleBooleanProperty(true);
    public BooleanProperty overlappingMatchesProperty = new SimpleBooleanProperty(true);
    public BooleanProperty falsePositivesProperty = new SimpleBooleanProperty(true);
    public BooleanProperty falseNegativesProperty = new SimpleBooleanProperty(true);

    public ListProperty<String> annotationList = new SimpleListProperty<>(FXCollections.observableArrayList());

    private AcvContext() {
        instance = this;

        initProperties();
        addProperty("exactMatch", Boolean.toString(true));

        referenceDocumentPathProperty.addListener((observable, oldValue, newValue) -> cleanupAnnotationList());
        targetDocumentPathProperty.addListener((observable, oldValue, newValue) -> cleanupAnnotationList());
        corpusFilePathProperty.addListener((observable, oldValue, newValue) -> loadCorpusDictionary());
    }

    private void cleanupAnnotationList() {
        if (annotationList.size() > 1) {
            annotationList.remove(1, annotationList.size() - 1);
            selectedAnnotationTypeProperty.setValue(annotationList.get(0));
        }
    }

    private void loadCorpusDictionary() {
        corpusDictionary = new CorpusDictionary(corpusFilePathProperty.getValueSafe());
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

    private void addProperty(String key, String value) {
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
