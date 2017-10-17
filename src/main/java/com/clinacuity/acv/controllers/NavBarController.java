package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.controls.NavBarButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainPageButton.setOnMouseClicked(event -> AcvContext.getMainController().reloadContent(AcvContext.APP_MAIN_PAGE));
        etudeRunnerButton.setOnMouseClicked(event -> AcvContext.getMainController().reloadContent(AcvContext.ETUDE_RUNNER));
        loadScreenButton.setOnMouseClicked(event -> AcvContext.getMainController().reloadContent(AcvContext.LOAD_SCREEN));
        configBuilderButton.setOnMouseClicked(event -> AcvContext.getMainController().reloadContent(AcvContext.CONFIGURATION_BUILDER));
        compareViewerButton.setOnMouseClicked(event -> AcvContext.getMainController().reloadContent(AcvContext.COMPARISON_VIEW));
    }
}
