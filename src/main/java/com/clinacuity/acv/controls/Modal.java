package com.clinacuity.acv.controls;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class Modal {
    private static final double DEFAULT_MAX_HEIGHT = 250.0d;
    private static final double DEFAULT_MAX_WIDTH = 350.0d;

    /**
     * Creates a Modal pop-up window with the given content, width, and height values.
     * <p>If the width and height are not set, the default values will be used.</p>
     * <p>If the Resizable flag is not set, it is assumed the modal cannot be resized.</p>
     * @param window        The window to which the modal will be attached
     * @param modalContent  The content which will be displayed on the Modal
     */
    public Modal(Window window, Parent modalContent) {
        new Modal(window, modalContent, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT, false);
    }

    /**
     * Creates a Modal pop-up window with the given content, width, and height values.
     * <p>If the width and height are not set, the default values will be used.</p>
     * <p>If the Resizable flag is not set, it is assumed the modal cannot be resized.</p>
     * @param window        The window to which the modal will be attached
     * @param modalContent  The content which will be displayed on the Modal
     * @param width         The maximum width of the modal
     * @param height        The maximum height of the modal
     */
    public Modal(Window window, Parent modalContent, double width, double height) {
        new Modal(window, modalContent, width, height, false);
    }

    /**
     * Creates a Modal pop-up window with the given content, width, and height values.
     * <p>If the width and height are not set, the default values will be used.</p>
     * <p>If the Resizable flag is not set, it is assumed the modal cannot be resized.</p>
     * @param window        The window to which the modal will be attached
     * @param modalContent  The content which will be displayed on the Modal
     * @param width         The maximum width of the modal
     * @param height        The maximum height of the modal
     * @param isResizable   Whether the modal can be resized by the user or its content
     */
    public Modal(Window window, Parent modalContent, double width, double height, boolean isResizable) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(window.getScene().getWindow());
        stage.setResizable(isResizable);

        Scene scene = new Scene(modalContent, width, height);
        stage.setScene(scene);
        stage.show();
    }
}
