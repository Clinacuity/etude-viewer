package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.context.AppMain;
import com.clinacuity.acv.modals.Modal;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class MenuBarController implements Initializable {
    @FXML private MenuBar menuBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML private void reloadMainPage() {
        AcvContext.getMainController().reloadContent(AcvContext.APP_MAIN_PAGE);
    }

    @FXML private void exitApplication() {
        ((Stage)menuBar.getScene().getWindow()).close();
    }

    @FXML private void showEtudeRunner() {
        AcvContext.getMainController().reloadContent(AcvContext.ETUDE_RUNNER);
    }

    @FXML private void showConfigurationCreator() {
        AcvContext.getMainController().reloadContent(AcvContext.CONFIGURATION_BUILDER);
    }

    @FXML private void showHowToUse() {
        HBox box = new HBox();
        box.getChildren().addAll(new Label("How to Use PDF will go here."));

        // TODO: use a static modal class
        (new Modal(menuBar.getScene().getWindow(), box)).show();
    }

    @FXML private void showLicenses() {
        HBox box = new HBox();
        box.getChildren().addAll(new Label("License information will go here."));

        // TODO: use a static modal class
        (new Modal(menuBar.getScene().getWindow(), box)).show();
    }

    @FXML private void goToHomepage() {
        AppMain.getWebPage("https://www.clinacuity.com");
    }
}
