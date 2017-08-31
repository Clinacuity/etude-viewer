package com.clinacuity.acv.controls;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationBlock extends StackPane {
    private static final Logger logger = LogManager.getLogger();

    @FXML private VBox mainBox;
    @FXML private JFXTextField keyTextField;
    private VBox parent;
    private List<JFXTextField> attributeTextFieldsList = new ArrayList<>();
    private List<JFXTextField> valueTextFieldsList = new ArrayList<>();

    public ConfigurationBlock(VBox box) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/ConfigurationBlock.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        parent = box;
        addAttributeRow();
    }

    @FXML private void deleteCard() {
        if (parent.getChildren().contains(this)) {
            parent.getChildren().remove(this);
        }
    }
    
    @FXML private void addAttributeRow() {
        HBox row = new HBox();
        row.setSpacing(25.0d);

        JFXTextField attributeTextField = new JFXTextField();
        attributeTextField.promptTextProperty().setValue("Attribute name");
        attributeTextField.getStyleClass().add("text-medium-normal");

        JFXTextField valueTextField = new JFXTextField();
        valueTextField.promptTextProperty().setValue("Value");
        valueTextField.getStyleClass().add("text-medium-normal");

        JFXButton button = new JFXButton();
        ImageView image = new ImageView(new Image("/img/icons8/delete.png"));
        image.setPreserveRatio(true);
        image.setFitHeight(12.0d);
        button.setAlignment(Pos.CENTER);
        button.getStyleClass().addAll("button-raised", "button-red", "no-focus");
        button.setButtonType(JFXButton.ButtonType.RAISED);
        button.setMaxWidth(16.0d);
        button.setMinWidth(16.0d);
        button.setMaxHeight(16.0d);
        button.setMinHeight(16.0d);
        button.setGraphic(image);
        button.setOnAction(event -> mainBox.getChildren().remove(row));
        
        row.getChildren().addAll(attributeTextField, valueTextField, button);
        attributeTextFieldsList.add(attributeTextField);
        valueTextFieldsList.add(valueTextField);

        mainBox.getChildren().add(mainBox.getChildren().size() - 2, row);
    }

    public String getText() {
        StringBuilder text = new StringBuilder();

        if (attributeTextFieldsList.size() == valueTextFieldsList.size() && keyTextField.getText().length() > 0) {
            text.append(String.format("[%s]\n", keyTextField.getText()));

            for (int i = 0; i < attributeTextFieldsList.size(); i++) {
                text.append(String.format("%s: %s\n",
                        attributeTextFieldsList.get(i).getText(), valueTextFieldsList.get(i).getText()));
            }

            text.append("\n");
            return text.toString();
        } else {
            return null;
        }
    }
}
