package com.clinacuity.acv.controls;

import com.jfoenix.controls.JFXDrawer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

public class SideBar extends VBox {
    private static final Logger logger = LogManager.getLogger();

    public static final double MIN_WIDTH = 250.0d;

    private JFXDrawer parentDrawer;
    public void setDrawer(JFXDrawer drawer) { parentDrawer = drawer; }

    public SideBar() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/SideBar.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }
    }

    @FXML
    private void collapse() {
        if (parentDrawer != null && parentDrawer.isShown()) {
            parentDrawer.close();
        }
    }
}
