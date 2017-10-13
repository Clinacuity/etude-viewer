package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URL;
import java.util.ResourceBundle;

public class MainPageController implements Initializable {
    private static final Logger logger = LogManager.getLogger();

    @FXML private JFXButton runnerButton;
    @FXML private JFXButton viewerButton;
    @FXML private JFXTextField viewerTextField;
    @FXML private StackPane runnerCard;
    @FXML private StackPane viewerCard;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // We only care about re-adjusting the widths with the text lengths
        runnerCard.widthProperty().addListener((obs, old, newValue) -> {
            if (newValue.doubleValue() > viewerCard.widthProperty().doubleValue()) {
                viewerCard.setMinWidth(newValue.doubleValue());
                viewerCard.setMaxWidth(newValue.doubleValue());
                runnerCard.setMinWidth(newValue.doubleValue());
                runnerCard.setMaxWidth(newValue.doubleValue());
            }
        });
        viewerCard.widthProperty().addListener((obs, old, newValue) -> {
            if (newValue.doubleValue() > runnerCard.widthProperty().doubleValue()) {
                runnerCard.setMinWidth(newValue.doubleValue());
                runnerCard.setMaxWidth(newValue.doubleValue());
                runnerButton.setMinWidth(newValue.doubleValue());
                runnerButton.setMaxWidth(newValue.doubleValue());
            }
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
