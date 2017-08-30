package com.clinacuity.acv.controls;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

public class ConfigurationBlock extends Pane {
    private static final Logger logger = LogManager.getLogger();

    @FXML private VBox mainBox;

    public ConfigurationBlock(double minWidth) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/ConfigurationBlock.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        mainBox.setMinWidth(minWidth);
        logger.error("{}", mainBox.getMinWidth());
    }
}
