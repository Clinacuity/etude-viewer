package com.clinacuity.acv.controls;

import com.clinacuity.acv.context.AcvContext;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class AnnotationButton extends Button {
//    private static final Logger logger = LogManager.getLogger();

    private static final String DEFAULT_STYLE = "-fx-focus-traversable: false;";
    private static final String HIGHLIGHTED_STYLE = "-fx-opacity: 0;";

    private JsonObject annotation;
    private int begin;
    private int end;
    public List<AnnotationButton> matchingButtons = new ArrayList<>();
    public TextArea targetTextArea;

    public AnnotationButton(JsonObject json) {
        initialize(json, 0, 0);
    }

    public AnnotationButton(JsonObject json, int beginValue, int endValue) {
        initialize(json, beginValue, endValue);
    }

    private void initialize(JsonObject json, int beginValue, int endValue) {
        setStyle(DEFAULT_STYLE);
        annotation = json;
        begin = beginValue;
        end = endValue;

        setOnAction(onButtonAction);
    }

    private String getAnnotationFeatureTree() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(AcvContext.getInstance().selectedAnnotationTypeProperty.getValueSafe());
        buffer.append(":\n");

        for (String key: annotation.keySet()) {
            buffer.append("\u2014> ");
            buffer.append(key);

            if (annotation.get(key).equals(JsonNull.INSTANCE)) {
                buffer.append(":  null");
            } else {
                buffer.append(":  ");
                buffer.append(annotation.get(key).getAsString());
            }
            buffer.append("\n");
        }

        return buffer.toString();
    }

    public void setSelected() {
        setStyle(DEFAULT_STYLE + HIGHLIGHTED_STYLE);
    }

    public void clearSelected() {
        setStyle(DEFAULT_STYLE);
    }

    public int getBegin() { return begin; }
    public int getEnd() { return end; }
    public void addMatch(AnnotationButton match) { matchingButtons.add(match); }

    private EventHandler<ActionEvent> onButtonAction = event -> targetTextArea.setText(getAnnotationFeatureTree());
}
