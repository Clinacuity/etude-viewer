package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AppMainController implements Initializable {
    private static final Logger logger = LogManager.getLogger();

    @FXML private GridPane masterGrid;
    @FXML private Node targetGridContent;
    @FXML private StackPane contentPane;
    @FXML private StackPane spinnerPane;

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        AcvContext.setMainController(this);

        try {
            addHeader();
            addFooter();
        } catch (IOException e) {
            logger.throwing(e);
        }

        AcvContext.getInstance().contentLoading.
                addListener((obs, old, newValue) -> spinnerPane.setVisible(newValue));
    }

    private void addHeader() throws IOException {
        Parent navBar = FXMLLoader.load(getClass().getResource(AcvContext.NAV_BAR), null);
        masterGrid.add(navBar, 0, 0);
    }

    private void addFooter() throws IOException {
        Parent navBar = FXMLLoader.load(getClass().getResource(AcvContext.FOOTER), null);
        masterGrid.add(navBar, 0, 2);
    }

    public void reloadContent(String page) {
        try {
            AcvContext.getInstance().contentLoading.setValue(false);
            contentPane.getChildren().remove(targetGridContent);
            targetGridContent = FXMLLoader.load(getClass().getResource(page), null);
            contentPane.getChildren().add(targetGridContent);
            spinnerPane.toFront();
        } catch (IOException e) {
            logger.throwing(e);
        }
    }
}
