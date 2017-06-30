package com.clinacuity.acv.controls;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HighlightedTextArea extends ScrollPane {
    private static final Logger logger = LogManager.getLogger();

    @FXML private TextArea textArea;
    @FXML private VBox anchor;
    private final Text textProxy = new Text();
    private List<Button> buttons = new ArrayList<>();

    public HighlightedTextArea() {
        initialize();
    }

    private void initialize() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/HighlightedTextArea.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

        textArea = new TextArea();
        textArea.maxWidth(Double.MAX_VALUE);
        textArea.maxHeight(Double.MAX_VALUE);
        textArea.setWrapText(true);

        anchor = new VBox(textArea);
        anchor.setAlignment(Pos.TOP_LEFT);
        anchor.setFillWidth(true);

        this.setContent(anchor);

        anchor.maxHeightProperty().bind(textArea.maxHeightProperty());
        anchor.prefHeightProperty().bind(textArea.prefHeightProperty());
        anchor.minHeightProperty().bind(textArea.minHeightProperty());

        this.heightProperty().addListener(event -> updateScrollPaneHeight());
        this.widthProperty().addListener(
                (observable, oldValue, newValue) -> updateScrollPaneWidth(newValue.doubleValue()));
    }

    private void updateText() {

    }

    private void updateScrollPaneWidth(double newValue) {
        textArea.setMinWidth(newValue - 20.0d);
        textArea.setMaxWidth(newValue - 20.0d);

        // TODO: reposition button elements
    }

    private void updateScrollPaneHeight() {
        if (textArea.getHeight() < this.getHeight()) {
            textArea.setMinHeight(this.getHeight() - 5.0d);
        }
    }
}
