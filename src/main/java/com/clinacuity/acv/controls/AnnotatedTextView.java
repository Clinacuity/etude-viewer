package com.clinacuity.acv.controls;

import com.clinacuity.acv.context.AcvContext;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class AnnotatedTextView extends GridPane {
    private static final Logger logger = LogManager.getLogger();

    @FXML private HighlightedTextArea referenceText;
    @FXML private HighlightedTextArea targetText;
    @FXML private ScrollPane referenceScrollPane;
    @FXML private Label test;

    public AnnotatedTextView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/controls/AnnotatedTextView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            logger.throwing(e);
        }

//        referenceText.prefHeightProperty().bind(referenceScrollPane.heightProperty());
//
//        referenceText.textProperty().addListener(event -> {
//            logger.error("Current Height: {}", referenceText.getLength() ,referenceText.heightProperty().get());
//            logger.error("Text Width: {}", referenceText.widthProperty().get());
//        });

//        referenceText.setPrefRowCount(Integer.MAX_VALUE);
//        referenceText.setFont(AcvContext.getInstance().getFont());
//        targetText.setFont(AcvContext.getInstance().getFont());
    }

    @FXML private void addText() {
        test.setText(test.getText() + test.getText());
        logger.debug("called");
    }

//    public void addToReferenceWindow() {}
//    public String getReferenceText() { return referenceText.getText(); }
//    public TextArea getReferenceTextArea() { return referenceText; }
//    public StringProperty getReferenceTextProperty() { return referenceText.textProperty(); }
//
//    public String getTargetText() { return targetText.getText(); }
//    public TextArea getTargetTextArea() { return targetText; }
//    public StringProperty getTargetTextProperty() { return targetText.textProperty(); }
}
