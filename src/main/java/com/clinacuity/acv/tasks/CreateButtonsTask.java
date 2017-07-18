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


            AnnotationButton button = new AnnotationButton(annotation, begin, end);
            button.setMaxSize(size, characterHeight);
            button.setMinSize(size, characterHeight);
            AnchorPane.setTopAnchor(button, topAnchor);
            AnchorPane.setLeftAnchor(button, leftAnchor);
            taskButtons.add(button);
            updateValue(taskButtons);
        } else {
            List<AnnotationButton> multiLineButton = new ArrayList<>();
            for (int i = beginLabelAttributes.getKey(); i <= endLabelAttributes.getKey(); i++) {
                Label label = taskLabels.get(i);
                int textLength = label.getText().length();
                double characterWidth = label.getWidth() / textLength;
                double topAnchor = characterHeight * i * 2.0d;

                // start off assuming the button takes up the entire label
                double leftAnchor = 0.0d;
                double size = characterWidth * textLength;

                // size goes from the offset to the end
                if (i == beginLabelAttributes.getKey()) {
                    size = characterWidth * (textLength - beginLabelAttributes.getValue());
                    leftAnchor = characterWidth * beginLabelAttributes.getValue();
                }

                // size goes from the beginning to the offset
                if (i == endLabelAttributes.getKey()) {
                    size = characterWidth * endLabelAttributes.getValue();
                    leftAnchor = 0.0d;
                }

                if (size > 0.0d) {
                    AnnotationButton button = new AnnotationButton(annotation, begin, end);
                    button.setMaxSize(size, characterHeight);
                    button.setMinSize(size, characterHeight);
                    AnchorPane.setTopAnchor(button, topAnchor);
                    AnchorPane.setLeftAnchor(button, leftAnchor);
                    multiLineButton.add(button);
                } else {
                    logger.debug("Sentence at [{}, {}] had some ignored characters.", begin, end);
                }
            }

            // Now, link all the buttons and return them
            for (int i = 0; i < multiLineButton.size(); i++) {
                for (int j = i + 1; j < multiLineButton.size(); j++) {
                    multiLineButton.get(i).matchingButtons.add(multiLineButton.get(j));
                    multiLineButton.get(j).matchingButtons.add(multiLineButton.get(i));
                }
            }

            for (AnnotationButton button: multiLineButton) {
                taskButtons.add(button);
                updateValue(taskButtons);
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
}
