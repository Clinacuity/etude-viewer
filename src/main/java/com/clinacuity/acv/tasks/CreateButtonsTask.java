package com.clinacuity.acv.tasks;

import com.clinacuity.acv.controls.AnnotationButton;
import com.google.gson.JsonObject;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CreateButtonsTask extends Task<List<AnnotationButton>> {
    private static final Logger logger = LogManager.getLogger();

    private List<JsonObject> taskAnnotations;
    private List<Label> taskLabels;
    private List<AnnotationButton> taskButtons = new ArrayList<>();

    private double characterHeight = -1.0;

    public CreateButtonsTask(List<JsonObject> annotations, List<Label> labels, double charHeight) {
        taskAnnotations = annotations;
        taskLabels = labels;
        characterHeight = charHeight;
    }

    @Override public List<AnnotationButton> call() {
        int max = taskAnnotations.size();

        for (int i = 0; i < max; i++) {
            processAnnotation(taskAnnotations.get(i));
            updateProgress(i, max);
        }

        succeeded();
        return taskButtons;
    }

    private void processAnnotation(JsonObject annotation) {
        // once the running Length exceeds the begin, we have found our target label
        int begin = annotation.get("begin_pos").getAsInt();
        int end = annotation.get("end_pos").getAsInt();

        Pair<Integer, Integer> beginLabelAttributes = getLabelAttributes(begin);
        Pair<Integer, Integer> endLabelAttributes = getLabelAttributes(end);

        if (beginLabelAttributes == null || endLabelAttributes == null) {
            setException(new NullPointerException());
            failed();
            return;
        }

        // the Key is the index of the label in the LabelList; the Value is the character offset
        if (beginLabelAttributes.getKey().equals(endLabelAttributes.getKey())) {
            Label label = taskLabels.get(beginLabelAttributes.getKey());
            double characterWidth = label.getWidth() / label.getText().length();
            double size = characterWidth * (end - begin);
            double leftAnchor = characterWidth * beginLabelAttributes.getValue();
            double topAnchor = characterHeight * beginLabelAttributes.getKey() * 2.0d;

            taskButtons.add(createButton(annotation, size, topAnchor, leftAnchor));
            updateValue(taskButtons);
        } else {
            // TODO: multiple buttons have to be created
            logger.error("FROM {} TO {}", beginLabelAttributes.getKey(), endLabelAttributes.getKey());
            for (int i = beginLabelAttributes.getKey(); i <= endLabelAttributes.getKey(); i++) {
                int textLength = taskLabels.get(i).getText().length();
                double characterWidth = taskLabels.get(i).getWidth() / textLength;
                double topAnchor = characterHeight * i * 2.0d;

                // start off assuming the button takes up the entire label
                double leftAnchor = 0.0d;
                double size = characterWidth * textLength;

                // size goes from the offset to the end
                if (i == beginLabelAttributes.getKey()) {
                    size = characterWidth * (textLength - beginLabelAttributes.getValue());
                    leftAnchor = characterWidth * beginLabelAttributes.getValue();
                    logger.error("BEGIN at label {}--- Size: {} ; Left: {}  ; value: {} ---- {} -> {}",
                            i, size, leftAnchor, beginLabelAttributes.getValue(), begin, end);
                }

                // size goes from the beginning to the offset
                if (i == endLabelAttributes.getKey()) {
                    logger.error("END!!!");
                    size = characterWidth * endLabelAttributes.getValue();
                    leftAnchor = 0.0d;
                }

                if (size > 0.0d) {
                    taskButtons.add(createButton(annotation, size, topAnchor, leftAnchor));
                    updateValue(taskButtons);
                } else {
                    logger.warn("Sentence at [{}, {}] had some ignored characters.", begin, end);
                }
            }
        }
    }

    /**
     * Returns a Pair in the form of (index, charOffset) for the given Label.
     * @param target    The index being searched for in terms of the entire document text
     * @return          A Pair whose key equals the label's index and value equals appropriate char index offset
     */
    private Pair<Integer, Integer> getLabelAttributes(int target) {
        int max = taskLabels.size();
        int runningLength = 0;
        int currentLength = 0;

        for (int i = 0; i < max; i++) {
            // The +1 accounts for removed new-lines when splitting the text into labels
            currentLength = taskLabels.get(i).getText().length() + 1;

            if (runningLength + currentLength > target) {
                return new Pair<>(i, target - runningLength);
            }

            runningLength += currentLength;
        }

        setException(new ArrayIndexOutOfBoundsException("Cannot extract Begin index; not enough labels."));
        logger.error("Target index = {}, but the total length of the text is {} + {}", target, runningLength, currentLength);
        failed();

        return null;
    }

    private AnnotationButton createButton(JsonObject annotation, double size, double topAnchor, double leftAnchor) {
        AnnotationButton button = new AnnotationButton(annotation);
        button.setMaxSize(size, characterHeight);
        button.setMinSize(size, characterHeight);
        AnchorPane.setTopAnchor(button, topAnchor);
        AnchorPane.setLeftAnchor(button, leftAnchor);
        return button;
    }
}
