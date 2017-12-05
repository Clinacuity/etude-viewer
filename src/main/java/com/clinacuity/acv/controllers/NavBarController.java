package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.context.AppMain;
import com.clinacuity.acv.controls.NavBarButton;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
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
        loadScreenButton.setTargetPage("/pages/Configuration.fxml");
        configBuilderButton.setTargetPage(AcvContext.CONFIGURATION_BUILDER);
        compareViewerButton.setTargetPage(AcvContext.COMPARISON_VIEW);

        mainPageButton.setOnMouseClicked(event -> selectedNavBarButton.setValue(mainPageButton));
        etudeRunnerButton.setOnMouseClicked(event -> selectedNavBarButton.setValue(etudeRunnerButton));
        loadScreenButton.setOnMouseClicked(event -> selectedNavBarButton.setValue(loadScreenButton));
        configBuilderButton.setOnMouseClicked(event -> selectedNavBarButton.setValue(configBuilderButton));
        compareViewerButton.setOnMouseClicked(event -> selectedNavBarButton.setValue(compareViewerButton));

        selectedNavBarButton.setValue(mainPageButton);

        AcvContext.setNavBar(this);
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

    public void loadPage(NavBarPages page) {
        switch(page) {
            case MAIN_PAGE:
                selectedNavBarButton.setValue(mainPageButton);
                break;
            case ETUDE_RUNNER:
                selectedNavBarButton.setValue(etudeRunnerButton);
                break;
            case LOAD_SCREEN:
                selectedNavBarButton.setValue(loadScreenButton);
                break;
            case CONFIGURATION_BUILDER:
                selectedNavBarButton.setValue(configBuilderButton);
                break;
            case COMPARISON_VIEW:
                selectedNavBarButton.setValue(compareViewerButton);
                break;
        }
    }

    public enum NavBarPages {
        MAIN_PAGE,
        ETUDE_RUNNER,
        LOAD_SCREEN,
        CONFIGURATION_BUILDER,
        COMPARISON_VIEW
    }
}
