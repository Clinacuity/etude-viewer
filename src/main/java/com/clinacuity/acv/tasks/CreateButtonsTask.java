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

    private static boolean willPrint = true;

    private double characterWidth = -1.0d;
    private double characterHeight = -1.0;

    public CreateButtonsTask(List<JsonObject> annots, List<Label> labels, double charWidth, double charHeight) {
        taskAnnotations = annots;
        taskLabels = labels;
        characterWidth = charWidth;
        characterHeight = charHeight;
    }

    @Override public List<AnnotationButton> call() {
        int max = taskAnnotations.size();

        for (int i = 0; i < max; i++) {
            createNewButton(taskAnnotations.get(i));
            updateProgress(i, max);
        }

        succeeded();
        return taskButtons;
    }

    private void createNewButton(JsonObject annotation) {
        // once the running Length exceeds the begin, we have found our target label
        int begin = annotation.get("begin_pos").getAsInt();
        int end = annotation.get("end_pos").getAsInt();

        Pair<Integer, Integer> beginLabel = getLabelAttributes(begin);
        Pair<Integer, Integer> endLabel = getLabelAttributes(end);

        if (beginLabel == null || endLabel == null) {
            setException(new NullPointerException());
            failed();
            return;
        }

        // the Key is the index of the label in the LabelList; the Value is the character offset
        if (beginLabel.getKey().equals(endLabel.getKey())) {
            AnnotationButton button = new AnnotationButton(annotation);
            button.setMaxSize(characterWidth * (end - begin), characterHeight);
            button.setMinSize(characterWidth * (end - begin), characterHeight);
            button.setTranslateY(characterHeight * beginLabel.getKey() * 2.0d);
            button.setTranslateX((int)(characterWidth * beginLabel.getValue()));
//            AnchorPane.setTopAnchor(button, characterHeight * beginLabel.getKey() * 2.0d);
//            AnchorPane.setLeftAnchor(button, characterWidth * (beginLabel.getValue()));

            if (CreateButtonsTask.willPrint) {
                logger.error("=========================================================");

                for (Object key: button.getProperties().keySet()) {
                    logger.error("{}: {}", key, button.getProperties().get(key));
                }

                logger.error("=========================================================");
                CreateButtonsTask.willPrint = false;
            }

            taskButtons.add(button);
            updateValue(taskButtons);
//        } else {
//            // TODO: multiple buttons have to be created
        }
    }

    /**
     * Returns a Pair in the form of (index, charOffset) for the given Label.
     * @param target
     * @return
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
