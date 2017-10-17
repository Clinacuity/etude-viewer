package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import java.net.URL;
import java.util.ResourceBundle;

public class MainPageController implements Initializable {
    @FXML private JFXTextField viewerTextField;
    @FXML private StackPane runnerCard;
    @FXML private StackPane viewerCard;
    @FXML private HBox buttonBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buttonBox.widthProperty().addListener((obs, old, newValue) -> {
            double width = newValue.doubleValue();
            viewerCard.setMinWidth(width * 0.35d);
            viewerCard.setMaxWidth(width * 0.35d);
            runnerCard.setMinWidth(width * 0.35d);
            runnerCard.setMaxWidth(width * 0.35d);
            buttonBox.setSpacing(width * 0.10d);
        });
    }

    @FXML
    private void loadEtudeConfigurationPage() {
        AcvContext.getInstance().mainController.reloadContent(AcvContext.ETUDE_RUNNER);
    }

    @FXML
    private void loadComparisonViewerPage() {
        AcvContext.getInstance().mainController.reloadContent(AcvContext.LOAD_SCREEN);
    }
}
