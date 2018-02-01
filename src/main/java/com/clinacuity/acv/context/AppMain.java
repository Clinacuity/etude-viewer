package com.clinacuity.acv.context;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

public class AppMain extends Application {
    private static final Logger logger = LogManager.getLogger();
    private Scene scene;

    private static Application application = null;
    public static void getWebPage(String page) { application.getHostServices().showDocument(page); }

    @Override
    synchronized public void start(Stage primaryStage) {
        if (application == null) {
            application = this;


            if (System.getProperty("java.version").startsWith("1.")) {
                logger.error("Please update your Java version");

                Parent root = getErrorMessage();
                scene = new Scene(root, 150, 90);
                primaryStage.setTitle("Incompatible Java");
                primaryStage.setScene(scene);
                primaryStage.setMinWidth(250);
                primaryStage.setMaxWidth(250);
                primaryStage.setMinHeight(100);
                primaryStage.setMaxHeight(100);
                primaryStage.show();
            } else {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(AcvContext.APP_CONTAINER));
                    Parent root = loader.load();

                    scene = new Scene(root, 1600, 900);
                    scene.getStylesheets().clear();
                    prepareCss();

                    primaryStage.setTitle("Annotations Comparison Viewer");
                    primaryStage.setScene(scene);
                    primaryStage.setMinWidth(1000.0d);
                    primaryStage.setMinHeight(800.0d);
                    primaryStage.show();

                    AcvContext.setMainWindow(scene.getWindow());
                } catch (IOException e) {
                    logger.throwing(e);
                }
            }
        }
    }

    private void prepareCss() throws IOException {
        scene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());
    }

    public static void main(String[] args) { launch(args); }

    private static Parent getErrorMessage() {
        Label label = new Label("Please update your Java version\nto 9.0.0 or newer.");
        label.setTextAlignment(TextAlignment.CENTER);

        StackPane box = new StackPane();
        box.setMaxWidth(Double.MAX_VALUE);
        box.setMaxHeight(Double.MAX_VALUE);
        box.getChildren().add(label);

        return box;
    }
}
