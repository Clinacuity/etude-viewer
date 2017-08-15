package com.clinacuity.acv.tasks;

import com.clinacuity.acv.controls.AnnotatedDocumentPane;
import com.clinacuity.acv.controls.AnnotationButton;
import com.clinacuity.acv.controls.LineNumberedLabel;
import com.google.gson.JsonObject;
import javafx.concurrent.Task;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class CreateButtonsTask extends Task<List<AnnotationButton>> {
    private static final Logger logger = LogManager.getLogger();

    private List<JsonObject> taskAnnotations;
    private List<LineNumberedLabel> taskLabels;
    private List<AnnotationButton> taskButtons = new ArrayList<>();
    private AnnotationButton previousButton = null;

    private double characterHeight = -1.0;

    public CreateButtonsTask(List<JsonObject> annotations, List<LineNumberedLabel> labels) {
        taskAnnotations = annotations;
        Collections.sort(labels);
        taskLabels = labels;
        characterHeight = AnnotatedDocumentPane.getCharacterHeight();
    }

    @Override public List<AnnotationButton> call() {
        int size = taskAnnotations.size();

        for (int i = 0; i < size; i++) {
            processAnnotation(taskAnnotations.get(i));
            updateProgress(i, size);
        }

        succeeded();
        return taskButtons;
    }

    private void processAnnotation(JsonObject annotation) {
        // once the running Length exceeds the begin, we have found our BEGIN label
        int begin = annotation.get("begin_pos").getAsInt();
        int end = annotation.get("end_pos").getAsInt();

        List<LineNumberedLabel> spannedLabels = getSpannedLabels(begin, end);
        addButtons(annotation, spannedLabels, begin, end);
    }

    /**
     * Gets the list of LineNumberedLabels spanned by the JsonObject's annotation based on its begin and end values.
     * @param begin The begin offset relative to the JsonObject's raw text
     * @param end   The end offset relative to the JsonObject's rew text
     * @return
     */
    private List<LineNumberedLabel> getSpannedLabels(int begin, int end) {
        List<LineNumberedLabel> spannedLabels = new ArrayList<>();
        int currentLineNumber = 1;

        boolean beginFound = false;
        for (int i = 0; i < taskLabels.size(); i++) {
            LineNumberedLabel index = taskLabels.get(i);
            int indexTextLength = index.getLineText().length();
            int offset = index.getTextOffset();

            if (currentLineNumber != index.getLineNumberIndex()) {
                currentLineNumber = index.getLineNumberIndex();
            }

            if (!beginFound) {
                if (offset <= begin && offset + indexTextLength >= begin) {
                    beginFound = true;
                    spannedLabels.add(index);
                }
            }

            if (beginFound) {
                if (!spannedLabels.contains(index)) {
                    spannedLabels.add(index);
                } else {
                    logger.debug("Label already in List");
                }

                if (offset <= end && offset + indexTextLength >= end) {
                    break;
                }
            }
        }

        return spannedLabels;
    }

    /**
     * Creates the AnnotationButton objects.  There are four possibilities, detailed below:
     *
     * 1. If labels.size() == 1, the button is entirely within that single line.  Otherwise:
     * 2. The start label will be offset and go through until the end of the line
     * 3. The middle labels will cover the entire line
     * 4. The end label will start at the beginning of the line and end at the appropriate offset
     *
     * @param annotation    The JsonObject to be associated with the label
     * @param labels        The labels spanned by the JsonObject
     * @param begin         The begin offset relative to the JsonObject's raw text
     * @param end           The end offset relative to the JsonObject's rew text
     */
    private void addButtons(JsonObject annotation, List<LineNumberedLabel> labels, int begin, int end) {
        // CASE #1
        if (labels.size() == 1) {
            LineNumberedLabel label = labels.get(0);
            AnnotationButton newButton = createButton(annotation, label, begin, end);

            if (previousButton != null) {
                newButton.previousButton = previousButton;
                previousButton.nextButton = newButton;
            }

            previousButton = newButton;
            taskButtons.add(newButton);
        } else {
            // This button spans at least 2 lines
            List<AnnotationButton> buttons = new ArrayList<>();

            labels.forEach(label -> {
                double charWidth = label.getTextLabel().getWidth() / label.getLineText().length();
                double topAnchor = characterHeight * taskLabels.indexOf(label) * 2.0d;

                AnnotationButton newButton;

                // if the offset is less than the begin, this is the starting line
                if (label.getTextOffset() <= begin) {
                    double size = charWidth * (label.getTextOffset() + label.getLineText().length() - begin);
                    double leftAnchor = charWidth * (begin - label.getTextOffset());

                    // create button
                    newButton = createButton(annotation, size, leftAnchor, topAnchor);
                    buttons.add(newButton);
                } else {
                    // if the offset + length are greater than the end, this is the ending line
                    if (label.getTextOffset() + label.getLineText().length() > end) {
                        double size = charWidth * (end - label.getTextOffset());
                        double leftAnchor = 0.0d;

                        newButton = createButton(annotation, size, leftAnchor, topAnchor);
                    } else {
                        // this is a middle line fully covered by the button
                        double size = charWidth * label.getLineText().length();
                        double leftAnchor = 0.0d;

                        newButton = createButton(annotation, size, leftAnchor, topAnchor);
                    }

                    for (AnnotationButton sameButton: buttons) {
                        sameButton.sameAnnotationButtons.add(newButton);
                    }
                    buttons.add(newButton);
                    newButton.sameAnnotationButtons.addAll(buttons);
                }
            });

            if (previousButton != null) {
                buttons.forEach(button -> button.previousButton = previousButton);
                previousButton.nextButton = buttons.get(0);
                previousButton.sameAnnotationButtons.forEach(button -> button.nextButton = buttons.get(0));
            }

            previousButton = buttons.get(0);
            taskButtons.addAll(buttons);
        }

        updateValue(taskButtons);
    }

    /**
     * User this for single-line buttons
     * @param annotation    The annotation which will be linked to the button
     * @param label         The label whose index will be used for determining the top offset
     * @param begin         The begin position of the annotation's text
     * @param end           The end position of the annotation's text
     * @return              Returns an AnnotationButton appropriately formatted and offset on top of its text
     */
    private AnnotationButton createButton(JsonObject annotation, LineNumberedLabel label, int begin, int end) {
        double characterWidth = label.getTextLabel().getWidth() / label.getLineText().length();
        double size = characterWidth * (end - begin);
        double topAnchor = characterHeight * taskLabels.indexOf(label) * 2.0d;
        double leftAnchor = characterWidth * (begin - label.getTextOffset());

        return createButton(annotation, size, leftAnchor, topAnchor);
    }

    private AnnotationButton createButton(JsonObject annotation, double size, double leftAnchor, double topAnchor) {
        AnnotationButton button = new AnnotationButton(annotation);
        button.setMaxSize(size, characterHeight);
        button.setMinSize(size, characterHeight);
        AnchorPane.setTopAnchor(button, AnnotatedDocumentPane.STANDARD_INSET + topAnchor);
        AnchorPane.setLeftAnchor(button, leftAnchor + AnnotatedDocumentPane.LINE_NUMBER_WIDTH);

        return button;
    }
}
