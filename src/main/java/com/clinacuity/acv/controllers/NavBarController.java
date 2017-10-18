package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.context.AppMain;
import com.clinacuity.acv.controls.NavBarButton;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URL;
import java.util.ResourceBundle;

public class NavBarController implements Initializable {
    @FXML private NavBarButton mainPageButton;
    @FXML private NavBarButton etudeRunnerButton;
    @FXML private NavBarButton loadScreenButton;
    @FXML private NavBarButton configBuilderButton;
    @FXML private NavBarButton compareViewerButton;
    private ObjectProperty<NavBarButton> selectedNavBarButton = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectedNavBarButton.addListener((obs, old, newValue) -> {
            if (old != null) {
                old.setSelected(false);
            }

            if (newValue != null) {
                newValue.setSelected(true);
                newValue.loadPage();
            }
        });
        
        mainPageButton.setTargetPage(AcvContext.APP_MAIN_PAGE);
        etudeRunnerButton.setTargetPage(AcvContext.ETUDE_RUNNER);
        loadScreenButton.setTargetPage(AcvContext.LOAD_SCREEN);
        configBuilderButton.setTargetPage(AcvContext.CONFIGURATION_BUILDER);
        compareViewerButton.setTargetPage(AcvContext.COMPARISON_VIEW);

        mainPageButton.setOnMouseClicked(event -> selectedNavBarButton.setValue(mainPageButton));
        etudeRunnerButton.setOnMouseClicked(event -> selectedNavBarButton.setValue(etudeRunnerButton));
        loadScreenButton.setOnMouseClicked(event -> selectedNavBarButton.setValue(loadScreenButton));
        configBuilderButton.setOnMouseClicked(event -> selectedNavBarButton.setValue(configBuilderButton));
        compareViewerButton.setOnMouseClicked(event -> selectedNavBarButton.setValue(compareViewerButton));

        selectedNavBarButton.setValue(mainPageButton);
    }

    @FXML private void clickedLogo() {
        AppMain.getWebPage("https://www.clinacuity.com");
    }
    
    @FXML private void mouseEnteredLogo() {
        AcvContext.getMainWindow().getScene().setCursor(Cursor.HAND);
    }

    @FXML private void mouseExitedLogo() {
        AcvContext.getMainWindow().getScene().setCursor(Cursor.DEFAULT);
    }
}
