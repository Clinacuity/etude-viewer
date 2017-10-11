package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AppMainController implements Initializable {
    private static final Logger logger = LogManager.getLogger();

    @FXML private GridPane masterGrid;
    @FXML private Node targetGridContent;

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        AcvContext.getInstance().mainController = this;

        try {
            MenuBar menuBar = FXMLLoader.load(getClass().getResource(AcvContext.MENU_BAR), null);
            masterGrid.getChildren().addAll(menuBar);

            reloadContent(AcvContext.LOAD_SCREEN);
        } catch (IOException e) {
            logger.throwing(e);
        }
    }

    void reloadContent(String page) {
        try {
            masterGrid.getChildren().remove(targetGridContent);
            targetGridContent = FXMLLoader.load(getClass().getResource(page), null);
            masterGrid.add(targetGridContent, 0, 1);
        } catch (IOException e) {
            logger.throwing(e);
        }
    }
}
