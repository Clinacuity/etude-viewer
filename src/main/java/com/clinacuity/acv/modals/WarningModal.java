package com.clinacuity.acv.modals;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WarningModal {
    private static final Logger logger = LogManager.getLogger();

    private static Modal modal = null;

    public static void createModal(String title, String message) {
        createModal(title, message, "OK");
    }

    public static void createModal(String title, String message, String confirmButtonText) {
        VBox box = new VBox();
        box.getStylesheets().add("/app.css");
        box.getStyleClass().add("card");
        box.setMaxHeight(Double.MAX_VALUE);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("text-header-two");
        titleLabel.setPadding(new Insets(5.0d, 0.0d, 5.0d, 10.0d));

        TextArea messageArea = new TextArea(message);
        messageArea.setEditable(false);
        messageArea.getStyleClass().add("text-medium-normal");
        messageArea.setWrapText(true);
        messageArea.setMaxHeight(Double.MAX_VALUE);
        messageArea.setFocusTraversable(false);
        messageArea.setMouseTransparent(true);
        VBox.setVgrow(messageArea, Priority.ALWAYS);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.setPadding(new Insets(15.0d, 15.0d, 15.0d, 0.0d));
        buttonBox.maxWidthProperty().bind(box.widthProperty());
        buttonBox.setSpacing(20.0d);

        Button closeButton = new Button(confirmButtonText);
        closeButton.getStyleClass().addAll("button-blue", "text-medium-normal");
        closeButton.setOnAction(event -> closeModal());

        buttonBox.getChildren().addAll(closeButton);
        box.getChildren().addAll(titleLabel, new Separator(), messageArea, new Separator(), buttonBox);

        modal = new Modal(box);
    }

    public static void show() {
        if (modal != null) {
            modal.show();
        } else {
            logger.error("Tried to show warning modal, but the modal has not been initialized!");
        }
    }

    private static void closeModal() {
        modal.close();
        modal = null;
    }
}
