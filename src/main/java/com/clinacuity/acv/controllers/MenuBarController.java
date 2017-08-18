package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.context.AppMain;
import com.clinacuity.acv.controls.Modal;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuBarController implements Initializable {
    private static final Logger logger = LogManager.getLogger();

    @FXML private MenuBar menuBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML private void showHowToUse() {
        HBox box = new HBox();
        box.getChildren().addAll(new Label("How to Use PDF will go here."));
        new Modal(menuBar.getScene().getWindow(), box);
    }

    @FXML private void showLicenses() {
        HBox box = new HBox();
        box.getChildren().addAll(new Label("License information will go here."));
        new Modal(menuBar.getScene().getWindow(), box);
    }

    @FXML private void goToHomepage() {
        logger.warn("not yet implemented");
    }

    @FXML private void reloadMainPage() {
        AcvContext.getInstance().mainController.reloadContent("/pages/LoadScreenView.fxml");
    }

    @FXML private void exitApplication() {
        ((Stage)menuBar.getScene().getWindow()).close();
    }
}
