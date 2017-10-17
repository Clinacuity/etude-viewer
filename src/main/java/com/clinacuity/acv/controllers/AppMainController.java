package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
            addHeader();
            addFooter();

            reloadContent(AcvContext.APP_MAIN_PAGE);
        } catch (IOException e) {
            logger.throwing(e);
        }
    }

    private void addHeader() throws IOException {
        HBox navBar = FXMLLoader.load(getClass().getResource(AcvContext.NAV_BAR), null);
        masterGrid.add(navBar, 0, 0);
    }

    private void addFooter() {
        HBox footerBox = new HBox();
        footerBox.setMaxWidth(Double.MAX_VALUE);
        footerBox.setAlignment(Pos.BOTTOM_RIGHT);
        footerBox.setPadding(new Insets(5.0));
        footerBox.getStyleClass().add("header");

        Label version = new Label();
        version.setText("Version: " + AcvContext.getAppProperty("version"));
        version.getStyleClass().add("text-small-normal");

        footerBox.getChildren().add(version);
        masterGrid.add(footerBox, 0, 2);
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
