package com.clinacuity.acv.controls;

import com.clinacuity.acv.context.AcvContext;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.NamedArg;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

public class NavBarButton extends StackPane {
    private static final Logger logger = LogManager.getLogger();
    private static final double ANIMATION_DURATION = 200.0d;
    private static final String SELECTED_STYLE = "nav-button-selected";

    @FXML private Label label;
    @FXML private Rectangle hoverBox;
    private Timeline onEnterTimeline = new Timeline();
    private Timeline onExitTimeline = new Timeline();
    private String targetPage = AcvContext.APP_MAIN_PAGE;

    public NavBarButton() {
        this("");
    }

    public NavBarButton(@NamedArg("text") String text) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/NavBarButton.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        initialize();
        label.setText(text);
    }

    private void initialize() {
        setOnMouseEntered(onMouseEntered);
        setOnMouseExited(onMouseExited);
    }

    public void setSelected(boolean selected) {
        if (selected) {
            label.getStyleClass().add(SELECTED_STYLE);
        } else {
            label.getStyleClass().remove(SELECTED_STYLE);
        }
    }

    public void setTargetPage(String page) {
        targetPage = page;
    }

    public void loadPage() {
        AcvContext.getMainController().reloadContent(targetPage);
    }

    private EventHandler<ActionEvent> onTimelineFinished = event -> {
        hoverBox.setVisible(false);
        onEnterTimeline.setOnFinished(null);
    };

    private EventHandler<MouseEvent> onMouseEntered = event -> {
        onExitTimeline.stop();
        hoverBox.setWidth(label.getWidth());

        hoverBox.setVisible(true);
        KeyValue boxHeight = new KeyValue(hoverBox.heightProperty(), label.getHeight());
        KeyFrame leftFrameStartY = new KeyFrame(Duration.millis(ANIMATION_DURATION), boxHeight);

        onEnterTimeline.getKeyFrames().clear();
        onEnterTimeline.getKeyFrames().add(leftFrameStartY);
        onEnterTimeline.play();
    };

    private EventHandler<MouseEvent> onMouseExited = event -> {
        onEnterTimeline.stop();

        KeyValue boxHeight = new KeyValue(hoverBox.heightProperty(), 0.0d);
        KeyFrame leftFrameStart = new KeyFrame(Duration.millis(ANIMATION_DURATION), boxHeight);

        onExitTimeline.setOnFinished(onTimelineFinished);
        onExitTimeline.getKeyFrames().clear();
        onExitTimeline.getKeyFrames().add(leftFrameStart);
        onExitTimeline.play();
    };
}
