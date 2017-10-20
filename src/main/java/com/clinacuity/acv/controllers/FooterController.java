package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import java.net.URL;
import java.util.ResourceBundle;

public class FooterController implements Initializable {
    @FXML private Label selectedDirectory;
    @FXML private Label version;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        version.setText("Version: " + AcvContext.getAppProperty("version"));
        AcvContext.getInstance().corpusFilePathProperty.addListener((obs, old, newValue) -> {
            if (newValue != null) {
                selectedDirectory.setText(newValue);
            } else {
                selectedDirectory.setText("");
            }
        });
    }
}
