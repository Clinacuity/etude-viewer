package com.clinacuity.acv.context;

import com.clinacuity.acv.controllers.AppMainController;
import com.clinacuity.acv.controllers.NavBarController;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.nio.file.FileSystemException;
import java.util.Properties;

public class AcvContext {
    public static final String APP_CONTAINER = "/pages/AppMain.fxml";
    public static final String APP_MAIN_PAGE = "/pages/MainPage.fxml";
    public static final String NAV_BAR = "/pages/NavBar.fxml";
    public static final String FOOTER = "/pages/Footer.fxml";
    public static final String LOAD_SCREEN = "/pages/LoadScreenView.fxml";
    public static final String ETUDE_RUNNER = "/pages/EtudeRunner.fxml";
    public static final String CONFIGURATION_BUILDER = "/pages/ConfigurationBuilder.fxml";
    public static final String COMPARISON_VIEW = "/pages/AcvContent.fxml";

    private static final Logger logger = LogManager.getLogger();
    private static final String USER_PROPERTIES_DIR = System.getProperty("user.home") + "/.clinacuity/etude/";
    private static final String USER_PROPERTIES_FILE_NAME = "config.properties";
    private static final String APP_PROPERTIES_FILE_NAME = "/application.properties";

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

    private AppMainController mainController;
    public static AppMainController getMainController() { return getInstance().mainController; }
    public static void setMainController(AppMainController controller) { getInstance().mainController = controller; }

    private NavBarController navBarController;
    public static NavBarController getNavBarController() { return getInstance().navBarController; }
    public static void setNavBar(NavBarController controller) { getInstance().navBarController = controller; }

    private Window mainWindow;
    public static Window getMainWindow() { return getInstance().mainWindow; }
    public static void setMainWindow(Window window) { getInstance().mainWindow = window; }

    private Properties properties;
    private Properties appProperties;
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

    /** This property indicates the content is loading; used for Tasks, when True, a spinner will display over the Content */
    public BooleanProperty contentLoading = new SimpleBooleanProperty(false);

    /** This property contains a list of annotation types available in the corpus file*/
    public ListProperty<String> annotationList = new SimpleListProperty<>(FXCollections.observableArrayList());

    private AcvContext() {
        instance = this;

        initProperties();
        setProperty("exactMatch", Boolean.toString(true));

        corpusFilePathProperty.addListener((observable, oldValue, newValue) -> loadCorpusDictionary());
    }

    public static String getAppProperty(String propertyName) {
        return getInstance().appProperties.getProperty(propertyName);
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
            getInstance().properties.store(new FileWriter(USER_PROPERTIES_FILE_NAME), "");
        } catch (IOException e) {
            logger.throwing(e);
        }
    }

    public static void loadPage(NavBarController.NavBarPages page) {
        getInstance().navBarController.loadPage(page);
    }

    private void loadCorpusDictionary() {
        corpusDictionary = new CorpusDictionary(corpusFilePathProperty.getValueSafe());
    }

    private void initProperties() {
        properties = new Properties();
        appProperties = new Properties();
        File propertiesFile = new File(USER_PROPERTIES_DIR + USER_PROPERTIES_FILE_NAME);

        try {
            if (!propertiesFile.exists()) {
                File rootDir = new File(USER_PROPERTIES_DIR);
                if (rootDir.mkdirs()) {
                    if (!propertiesFile.createNewFile()) {
                        throw new FileSystemException("Could not create file in path: " + propertiesFile.getAbsolutePath());
                    }
                } else {
                    throw new FileSystemException("Could not create directories in path: " + rootDir.getAbsolutePath());
                }
            }
            properties.load(new FileReader(propertiesFile));
            appProperties.load(getInstance().getClass().getResourceAsStream(APP_PROPERTIES_FILE_NAME));
        } catch (IOException e) {
            logger.throwing(e);
        }
    }
}
