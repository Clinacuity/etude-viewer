package com.clinacuity.acv.controls;

import com.clinacuity.acv.context.AcvContext;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    private static final String HIGHLIGHTED_STYLE = "-fx-opacity: 0.45;";

    private JsonObject annotation;
    private int begin;
    private int end;
    private boolean categoryMatch = false;
    private boolean attributesMatch = false;
    private boolean spanMatch = false;
    private boolean fullyContained = false;
    private String matchTypeStyle = "";
    private MatchType matchType;
    private Label categoryLabel;
    public AnnotationButton previousButton = null;
    public AnnotationButton nextButton = null;

    public List<AnnotationButton> matchingButtons = new ArrayList<>();
    public List<AnnotationButton> sameAnnotationButtons = new ArrayList<>();
    public TextArea targetTextArea;
    public AnchorPane parent;
    public MatchType getMatchType() { return matchType; }

    public int getBegin() { return begin; }

    public int getEnd() { return end; }

    public AnnotationButton(JsonObject json) {
        int beginValue = 0;
        int endValue = 0;

        if (json.has("begin_pos") && json.has("end_pos")) {
            beginValue = json.get("begin_pos").getAsInt();
            endValue = json.get("end_pos").getAsInt();
        }

        initialize(json, beginValue, endValue);
    }

    public void setSelected() {
        setStyle(matchTypeStyle + HIGHLIGHTED_STYLE);
        addCategoryLabel();
    }

    public void clearSelected() {
        setStyle(matchTypeStyle);
        removeCategoryLabel();
    }

    public void checkMatchTypes(MatchType noMatchType) {
        if (matchingButtons.size() == 0) {
            setMatchType(noMatchType);
        } else {
            AnnotationButton target = matchingButtons.get(0);

            spanMatch = isSpanMatch(target);
            attributesMatch = isAnnotationEquivalent(target.annotation);
            categoryMatch = isCategoryMatch();
            fullyContained = isFullyContainedOverlap(target);

            if (spanMatch && categoryMatch) {
                setMatchType(MatchType.EXACT_MATCH);
            } else {
                setMatchType(MatchType.PARTIAL_MATCH);
            }
        }
    }

    public void removeFromParent() {
        parent.getChildren().remove(this);
    }

    public void addToParent() {
        parent.getChildren().add(this);
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

    private void addCategoryLabel() {
        if (categoryLabel == null) {
            categoryLabel = new Label(annotation.get("type").getAsString());
            categoryLabel.getStyleClass().add("label-annotation-category");
            categoryLabel.setMaxWidth(getWidth());
            categoryLabel.setMinWidth(getWidth());
            AnchorPane.setTopAnchor(categoryLabel, (double) getProperties().get("pane-top-anchor") - getHeight());
            AnchorPane.setLeftAnchor(categoryLabel, (double) getProperties().get("pane-left-anchor"));
        }
        parent.getChildren().add(categoryLabel);
    }

    private void removeCategoryLabel() {
        if (categoryLabel != null) {
            parent.getChildren().remove(categoryLabel);
        }
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

    private boolean isCategoryMatch() {
        return true;
    }

    private boolean isSpanMatch(AnnotationButton target) {
        return (getBegin() == target.getBegin() && getEnd() == target.getEnd());
    }

    private boolean isFullyContainedOverlap(AnnotationButton target) {
        return ((getBegin() <= target.getBegin() && getEnd() >= target.getEnd())
                || (getBegin() >= target.getBegin() && getEnd() <= target.getEnd()));
    }

    private void setMatchType(MatchType match) {
        switch(match) {
            case EXACT_MATCH:
                matchTypeStyle = "-fx-background-color: DodgerBlue;";

                if (attributesMatch) {
                    // TODO
                    logger.debug("attributes");
                }

                break;

            case PARTIAL_MATCH:
                matchTypeStyle = "-fx-background-color: DarkOrchid;";

                if (attributesMatch) {
                    // TODO
                    logger.debug("ok");
                }

                if (fullyContained) {
                    // TODO
                    logger.debug("this is fully contained");
                }

                break;

            case FALSE_POS:
            case FALSE_NEG:
                matchTypeStyle = "-fx-background-color: OrangeRed;";
                break;

            default:
                matchTypeStyle = "-fx-background-color: DarkGray;";
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

    /**
     * Exact matches -- identical category and span
     * Partial matches -- spans overlap; categories don't match
     * Partial matches -- spans exact; categories don't match
     * Partial matches -- spans don't overlap; categories match
     * No match
     */
    public enum MatchType {
        EXACT_MATCH,
        PARTIAL_MATCH,
        FALSE_POS,
        FALSE_NEG
    }
}
