package com.clinacuity.acv.context;

import com.clinacuity.acv.context.AcvContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppMain extends Application{
    private static final Logger logger = LogManager.getLogger();

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/pages/AppMain.fxml"));

            Scene scene = new Scene(root, 1600, 900);
            primaryStage.setTitle("Annotation Comparison Viewer");
            primaryStage.setScene(scene);
            primaryStage.show();

            loadProperties();
        } catch (IOException e) {
            logger.throwing(e);
        }
    }

    private void loadProperties() {
        Properties properties = null;
        try {
            properties = new Properties();
            properties.load(getClass().getResourceAsStream("/config.properties"));
            logger.debug("Loading font: {}", properties.getProperty("mainFont"));
        } catch (IOException e) {
            logger.throwing(e);
        }

        AcvContext context = AcvContext.getInstance();
        context.setFont(Font.loadFont(getClass().getResourceAsStream(properties.getProperty("mainFont")), 12.0f));
    }

    public static void main(String[] args) { launch(args); }
}
