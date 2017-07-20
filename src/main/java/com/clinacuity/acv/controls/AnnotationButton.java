package com.clinacuity.acv.controls;

import com.clinacuity.acv.context.AcvContext;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/*
 TODO: refactor annotation buttons
 The data structure of the annotation buttons could be triangular: the controller refers to the Annot object,
 which houses the JSON, begin and end values, and the matching buttons versus the source buttons; the controller would
 also need the buttons themselves in order to add them to their respective annotated document panes.  This would reduce
 the amount of memory each instance occupies, since there would only be a single reference to the JSON object, regardless
 of how many buttons are related to each other.
  */
public class AnnotationButton extends Button {
    private static final Logger logger = LogManager.getLogger();
    private static final String HIGHLIGHTED_STYLE = "-fx-opacity: 0.75;";

    private JsonObject annotation;
    private int begin;
    private int end;
    public List<AnnotationButton> matchingButtons = new ArrayList<>();
    public List<AnnotationButton> sameAnnotationButtons = new ArrayList<>();
    public TextArea targetTextArea;
    private String matchTypeStyle = "";
    private MatchType matchType;
    public AnchorPane parent;
    public MatchType getMatchType() { return matchType; }

    public int getBegin() { return begin; }

    public int getEnd() { return end; }

    public AnnotationButton(JsonObject json) {
        initialize(json, 0, 0);
    }

    public AnnotationButton(JsonObject json, int beginValue, int endValue) {
        initialize(json, beginValue, endValue);
    }

    private void initialize(JsonObject json, int beginValue, int endValue) {
        getStyleClass().add("button-annotation");
        annotation = json;
        begin = beginValue;
        end = endValue;

        setOnAction(onButtonAction);
        setOnMouseEntered(setHover);
        setOnMouseExited(unsetHover);
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
        logger.error(getStyle());
        setStyle(matchTypeStyle + HIGHLIGHTED_STYLE);
    }

    public void clearSelected() {
        logger.error(getStyle());
        setStyle(matchTypeStyle);
    }

    public void checkForMatchTypes() {
        if (matchingButtons.size() == 0) {
            setMatchType(MatchType.NO_MATCH);
        } else {
            AnnotationButton target = matchingButtons.get(0);

            if (target.getBegin() == getBegin() && target.getEnd() == getEnd()) {
                if (isAnnotationEquivalent(target.annotation)) {
                    setMatchType(MatchType.EXACT_SPAN);
                } else {
                    setMatchType(MatchType.EXACT_SPAN_DIFF_FEATURES);
                }
            } else {
                if (getBegin() == 0 || getBegin() == 7) {
                    logger.error("these should be marked as subsumed");
                }
                // Either is contained by the other
                if ((target.getBegin() >= getBegin() && target.getEnd() <= getEnd())
                        || (getBegin() >= target.getBegin() && getEnd() <= target.getEnd())) {
                    setMatchType(MatchType.SUBSUMED);
                    logger.error("something is subsumed");
                } else {
                    setMatchType(MatchType.OVERLAP);
                }
            }
        }
    }

    public void removeFromParent() {
        parent.getChildren().remove(this);
    }

    public void addToParent() {
        parent.getChildren().add(this);
    }

    private boolean isAnnotationEquivalent(JsonObject target) {
        for (String key: target.keySet()) {
            if (!annotation.keySet().contains(key)) {
                return false;
            }
        }

        for(String key: annotation.keySet()) {
            if (!target.keySet().contains(key)) {
                return false;
            } else {
                if (!target.get(key).equals(annotation.get(key))) {
                    return false;
                }
            }
        }
        return true;
    }

    private void setMatchType(MatchType match) {
        switch(match) {
            case EXACT_SPAN:
                matchTypeStyle = "-fx-background-color: DodgerBlue";
                break;
            case EXACT_SPAN_DIFF_FEATURES:
                matchTypeStyle = "-fx-background-color: OrangeRed";
                break;
            case OVERLAP:
                matchTypeStyle = "-fx-background-color: Purple";
                break;
            case SUBSUMED:
                matchTypeStyle = "-fx-background-color: DarkKhaki";
                break;
            case NO_MATCH:
                matchTypeStyle = "-fx-background-color: Maroon";
                break;
        }

        matchType = match;
        setStyle(matchTypeStyle);
    }

    /* ******************************
     *                              *
     * Event Handlers               *
     *                              *
     *******************************/

    private EventHandler<ActionEvent> onButtonAction = event -> targetTextArea.setText(getAnnotationFeatureTree());

    private EventHandler<MouseEvent> setHover = event -> {
        matchingButtons.forEach(button -> button.setHover(true));
        sameAnnotationButtons.forEach(button -> button.setHover(true));
    };

    private EventHandler<MouseEvent> unsetHover = event -> {
        matchingButtons.forEach(button -> button.setHover(false));
        sameAnnotationButtons.forEach(button -> button.setHover(false));
    };

    public enum MatchType {
        EXACT_SPAN,
        EXACT_SPAN_DIFF_FEATURES,
        OVERLAP,
        SUBSUMED,
        NO_MATCH
    }
}
