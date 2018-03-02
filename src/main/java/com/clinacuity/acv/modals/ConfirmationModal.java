package com.clinacuity.acv.modals;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConfirmationModal {
    private static final Logger logger = LogManager.getLogger();

    private static Modal modal = null;
    private static Button confirmButton = null;
    private static Button cancelButton = null;

    public static void createModal(String title, String message) {
        createModal(title, message, "CONFIRM", "CANCEL");
    }

    public static void createModal(String title, String message, String confirmText, String cancelText) {
        VBox box = new VBox();
        box.getStylesheets().add("/app.css");
        box.setMaxHeight(Double.MAX_VALUE);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("text-header-two");
        titleLabel.setPadding(new Insets(5.0d, 0.0d, 5.0d, 10.0d));

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("text-medium-normal");
        messageLabel.setPadding(new Insets(10.0d, 0.0d, 10.0d, 5.0d));
        messageLabel.setWrapText(true);
        messageLabel.setMaxHeight(Double.MAX_VALUE);
        messageLabel.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(messageLabel, Priority.ALWAYS);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.setPadding(new Insets(15.0d, 15.0d, 15.0d, 0.0d));
        buttonBox.maxWidthProperty().bind(box.widthProperty());
        buttonBox.setSpacing(20.0d);

        confirmButton = new Button(confirmText);
        confirmButton.getStyleClass().addAll("button-blue", "text-medium-normal");

        cancelButton = new Button(cancelText);
        cancelButton.getStyleClass().addAll("button-gray", "text-medium-normal");
        cancelButton.maxWidthProperty().bind(confirmButton.widthProperty());
        cancelButton.minWidthProperty().bind(confirmButton.widthProperty());

        buttonBox.getChildren().addAll(cancelButton, confirmButton);
        box.getChildren().addAll(titleLabel, new Separator(), messageLabel, new Separator(), buttonBox);

        modal = new Modal(box);
    }

    public static void setConfirmAction(EventHandler<ActionEvent> action) {
        if (confirmButton != null) {
            confirmButton.setOnAction(event -> {
                closeModal();
                action.handle(event);
            });
        }
    }

    public static void setCancelAction(EventHandler<ActionEvent> action) {
        if (cancelButton != null) {
            cancelButton.setOnAction(event -> {
                closeModal();
                action.handle(event);
            });
        }
    }

    public static void show() {
        if (modal != null) {
            modal.show();
        } else {
            logger.error("Tried to show Confirmation Modal, but it has not been initialized!");
        }
    }

    private static void closeModal() {
        modal.close();
        modal = null;
        confirmButton = null;
        cancelButton = null;
    }
}
