package com.clinacuity.acv.controllers;

import com.clinacuity.acv.context.AcvContext;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class LoadScreenController implements Initializable {
    private AppMainController mainController;
    public void setAppMainController(AppMainController controller) { mainController = controller; }

    @FXML private TextField gsInputTextField;
    @FXML private TextField testInputTextField;

//    @FXML private TextField dictionariesGsInputField;
//    @FXML private TextField dictionariesTestInputField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML private void pickGsInputDir() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Gold Standard Dictionaries Folder");
        File directory = directoryChooser.showDialog(gsInputTextField.getScene().getWindow());

        if (directory != null) {
            String path = addTrailingSlash(directory.getAbsolutePath());
            gsInputTextField.setText(path);
        }
    }

    @FXML private void pickTestInputDir() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Test Dictionaries Folder");
        File directory = directoryChooser.showDialog(gsInputTextField.getScene().getWindow());

        if (directory != null) {
            String path = addTrailingSlash(directory.getAbsolutePath());
            testInputTextField.setText(path);
        }
    }

    @FXML private void runAcv() {
        AcvContext.getInstance().getMainController().reloadContent("/pages/AcvContent.fxml");
    }

    private String addTrailingSlash(String path) {
        if (path.charAt(path.length() - 1) != '/') {
            return path + '/';
        }

        return path;
    }
}
