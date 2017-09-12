package com.clinacuity.acv.controls;

import com.jfoenix.controls.JFXDrawer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reactfx.util.FxTimer;

import java.io.IOException;
import java.time.Duration;

public class SideBar extends VBox {
    private static final Logger logger = LogManager.getLogger();

    public static final double MIN_WIDTH = 250.0d;

    private JFXDrawer parentDrawer;
    public void setDrawer(JFXDrawer drawer) { parentDrawer = drawer; }

    @FXML private VBox box1;
    @FXML private VBox box2;

    public SideBar() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/SideBar.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        FxTimer.runPeriodically(Duration.ofMillis(1000), () -> {
            logger.error("P: {}", getWidth());
            logger.error("Box1: {}", box1.getWidth());
            logger.error("Box2: {}", box2.getWidth());
        });
    }

    @FXML
    private void collapse() {
        if (parentDrawer != null && parentDrawer.isShown()) {
            parentDrawer.close();
        }
    }
}
