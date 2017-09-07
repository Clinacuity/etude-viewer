package com.clinacuity.acv.context;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class AppMain extends Application{
    private static final Logger logger = LogManager.getLogger();
    private Scene scene;

    private static Application application = null;
    public static Properties properties;
    public static void getWebPage(String page) { application.getHostServices().showDocument(page); }

    @Override
    synchronized public void start(Stage primaryStage) {
        if (application == null) {
            application = this;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(AcvContext.APP_MAIN_VIEW));
                loader.setResources(ResourceBundle.getBundle("config_en", new Locale("en")));
                Parent root = loader.load();

                scene = new Scene(root, 1600, 900);
                scene.getStylesheets().clear();
                loadProperties();
                prepareCss();

                primaryStage.setTitle("Annotations Comparison Viewer");
                primaryStage.setScene(scene);
                primaryStage.setMinWidth(1000.0d);
                primaryStage.setMinHeight(800.0d);
                primaryStage.show();

                AcvContext.getInstance().mainWindow = scene.getWindow();
            } catch (IOException e) {
                logger.throwing(e);
            }
        }
    }

    private void loadProperties() throws IOException {
        properties = new Properties();
        properties.load(new FileInputStream("config_en.properties"));
    }

    private void prepareCss() throws IOException {
        scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
    }

    public static void main(String[] args) { launch(args); }
}
