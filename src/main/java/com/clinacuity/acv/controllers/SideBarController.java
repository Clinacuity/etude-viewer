package com.clinacuity.acv.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDrawer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by andrewtrice on 8/31/17.
 */
public class SideBarController implements Initializable {

    @FXML
    private JFXDrawer sidebar;
    @FXML
    private JFXButton b1;
    @FXML
    private JFXButton b2;
    @FXML
    private JFXButton b3;
    @FXML
    private JFXButton exit;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    @FXML
    private void changeColor(ActionEvent event) {
        JFXButton btn = (JFXButton) event.getSource();

        switch(btn.getText()) {
        case "File 1":
            btn.setStyle("-fx-background-color:#00FF00");
            break;
        }
    }

    @FXML
    private void exit(ActionEvent event) {
        System.exit(0);
    }

}
