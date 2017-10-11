package com.clinacuity.acv.context;

import com.clinacuity.acv.controllers.AppMainController;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.nio.file.FileSystemException;
import java.util.Properties;

public class AcvContext {
    public static final String LOAD_SCREEN = "/pages/LoadScreenView.fxml";
    public static final String COMPARISON_VIEW = "/pages/AcvContent.fxml";
    public static final String APP_MAIN_VIEW = "/pages/AppMain.fxml";
    public static final String MENU_BAR = "/pages/MenuBar.fxml";
    public static final String ETUDE_RUNNER = "/pages/EtudeRunner.fxml";
    public static final String CONFIGURATION_BUILDER = "/pages/ConfigurationBuilder.fxml";

    private static final Logger logger = LogManager.getLogger();
    private static final String PROPERTIES_FILE_NAME = "./data/config.properties";

    private static AcvContext instance;
    public static AcvContext getInstance() {
        if (instance == null) {
            synchronized (AcvContext.class) {
                if (instance == null) {
                    new AcvContext();
                }
            }
        }
        return instance;
    }

    public AppMainController mainController;
    public Window mainWindow;

    private Properties properties;
    private CorpusDictionary corpusDictionary;
    public CorpusDictionary getCorpusDictionary() { return corpusDictionary; }

    /** This property is the string value of the Reference directory's path containing all the reference files */
    public StringProperty referenceDirectoryProperty = new SimpleStringProperty("");

    /** This property is the string value of the System directory's path containing all the system output files */
    public StringProperty targetDirectoryProperty = new SimpleStringProperty("");

    /** This property is the string value of the annotation type currently selected in the ViewControl's table */
    public StringProperty selectedAnnotationTypeProperty = new SimpleStringProperty("");

    /** A reference to the match type (i.e. Exact, Partial, etc.) selected for populating the table's metrics */
    public StringProperty selectedMatchTypeProperty = new SimpleStringProperty("");

    /** This property is the string value of the Corpus file's path, which is used to populate the list of files,
     *  determining metrics, populating the Radio buttons for the match types, etc. */
    public StringProperty corpusFilePathProperty = new SimpleStringProperty("");

    /** This property indicates whether True Positive annotations will be displayed in the Annotated Document Panes */
    public BooleanProperty truePositivesProperty = new SimpleBooleanProperty(true);

    /** This property indicates whether False Positive annotations will be displayed in the Annotated Document Panes */
    public BooleanProperty falsePositivesProperty = new SimpleBooleanProperty(true);

    /** This property indicates whether False Negative annotations will be displayed in the Annotated Document Panes */
    public BooleanProperty falseNegativesProperty = new SimpleBooleanProperty(true);

    /** This property contains a list of annotation types available in the corpus file*/
    public ListProperty<String> annotationList = new SimpleListProperty<>(FXCollections.observableArrayList());

    private AcvContext() {
        instance = this;

        initProperties();
        setProperty("exactMatch", Boolean.toString(true));

        corpusFilePathProperty.addListener((observable, oldValue, newValue) -> loadCorpusDictionary());
    }

    public static String getProperty(String propertyName) {
        return getInstance().properties.getProperty(propertyName);
    }

    public static String getProperty(String propertyName, Object defaultValue) {
        return getInstance().properties.getProperty(propertyName, defaultValue.toString());
    }

    public static void setProperty(String propertyName, Object value) {
        getInstance().properties.setProperty(propertyName, value.toString());

        try {
            getInstance().properties.store(new FileWriter(PROPERTIES_FILE_NAME), "");
        } catch (IOException e) {
            logger.throwing(e);
        }
    }

    private void loadCorpusDictionary() {
        corpusDictionary = new CorpusDictionary(corpusFilePathProperty.getValueSafe());
    }

    private void initProperties() {
        properties = new Properties();
        File propertiesFile = new File(PROPERTIES_FILE_NAME);

        if (!propertiesFile.exists()) {
            try {
                if (!propertiesFile.exists()) {
                    if (!propertiesFile.createNewFile()) {
                        throw new FileSystemException("Could not create file in path: " + propertiesFile.getAbsolutePath());
                    }
                }
                properties.load(new FileReader(propertiesFile));
            } catch (IOException e) {
                logger.throwing(e);
            }
        }
    }
}
