package com.clinacuity.acv.tasks;

import com.clinacuity.acv.context.AcvContext;
import com.clinacuity.acv.controls.AnnotatedDocumentPane;
import com.clinacuity.acv.controls.AnnotationButton;
import com.clinacuity.acv.controls.LineNumberedLabel;
import com.google.gson.JsonObject;
import javafx.beans.property.ObjectProperty;
import javafx.concurrent.Task;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.clinacuity.acv.controllers.ConfigurationBuilderController.CorpusType;

public class CreateButtonsTask extends Task<Map<CorpusType, List<AnnotationButton>>> {
    private static final Logger logger = LogManager.getLogger();

    private List<JsonObject> systemAnnotations;
    private List<JsonObject> refAnnotations;
    private List<LineNumberedLabel> systemLabels;
    private List<LineNumberedLabel> refLabels;
    private Map<CorpusType, List<AnnotationButton>> results;
    private final double characterHeight;

    private AnnotatedDocumentPane systemOutPane;
    public void setSystemOutPane(AnnotatedDocumentPane pane) { systemOutPane = pane; }

    private AnnotatedDocumentPane referencePane;
    public void setReferencePane(AnnotatedDocumentPane pane) { referencePane = pane; }

    private ObjectProperty<AnnotationButton> selectedAnnotationButton;
    public void setSelectedAnnotationButton(ObjectProperty<AnnotationButton> button) {
        selectedAnnotationButton = button;
    }

    public CreateButtonsTask(List<JsonObject> systemAnnotations, List<LineNumberedLabel> systemLabels,
                          List<JsonObject> refAnnotations, List<LineNumberedLabel> refLabels) {
        Collections.sort(systemLabels);
        Collections.sort(refLabels);
        this.systemAnnotations = systemAnnotations;
        this.systemLabels = systemLabels;
        this.refAnnotations = refAnnotations;
        this.refLabels = refLabels;
        this.characterHeight = AnnotatedDocumentPane.CHARACTER_HEIGHT;

        results = new HashMap<>();
        results.put(CorpusType.REFERENCE, new ArrayList<>());
        results.put(CorpusType.SYSTEM, new ArrayList<>());

    }

    @Override public Map<CorpusType, List<AnnotationButton>> call() {
        int systemSize = systemAnnotations.size();
        int referenceSize = refAnnotations.size();
        int total = systemSize + referenceSize;

        for (int i = 0; i < systemSize; i++) {
            createAnnotationButtons(systemAnnotations.get(i), systemLabels, results.get(CorpusType.SYSTEM));
            updateProgress(i, total);
        }

        // create reference buttons
        for (int i = 0; i < referenceSize; i++) {
            createAnnotationButtons(refAnnotations.get(i), refLabels, results.get(CorpusType.REFERENCE));
            updateProgress(systemSize + i, total);
        }

        if (results == null) {
            results = new HashMap<>();
        } else {
            Collections.sort(results.get(CorpusType.SYSTEM));
            Collections.sort(results.get(CorpusType.REFERENCE));

            setupAnnotationButtons();
            setPreviousAndNextValues();
        }

        updateValue(results);
        return results;
    }

    /**
     * Creates the annotation buttons for an annotation
     * Note that this will create multiple buttons if an annotation spans multiple lines (labels)
     * @param annotation    The annotation for which to create a button
     */
    private void createAnnotationButtons(JsonObject annotation, List<LineNumberedLabel> targetLabels, List<AnnotationButton> targetButtonList) {
        int begin = annotation.get("begin_pos").getAsInt();
        int end = annotation.get("end_pos").getAsInt();

        List<LineNumberedLabel> spannedLabels = getSpannedLabels(begin, end, targetLabels);
        addButtons(annotation, spannedLabels, begin, end, targetLabels, targetButtonList);
    }

    /**
     * Gets the list of LineNumberedLabels spanned by the JsonObject's annotation based on its begin and end values.
     * @param begin The begin offset relative to the JsonObject's raw text
     * @param end   The end offset relative to the JsonObject's rew text
     * @return      Returns a list of the labels which are spanned by the given annotation's begin/end values
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    private List<LineNumberedLabel> getSpannedLabels(int begin, int end, List<LineNumberedLabel> targetLabels) {
        List<LineNumberedLabel> spannedLabels = new ArrayList<>();
        int currentLineNumber = 1;

        boolean beginFound = false;
        for (int i = 0; i < targetLabels.size(); i++) {
            LineNumberedLabel index = targetLabels.get(i);
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
                // this should always resolve to false
                if (!spannedLabels.contains(index)) {
                    spannedLabels.add(index);
                }

                // if we found the end of the label, exit
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
    private void addButtons(JsonObject annotation, List<LineNumberedLabel> labels, int begin, int end,
                            List<LineNumberedLabel> targetLabels, List<AnnotationButton> targetButtonList) {
        // CASE #1: Button's span is within the same line
        if (labels.size() == 1) {
            LineNumberedLabel label = labels.get(0);
            AnnotationButton newButton = createButton(annotation, label, targetLabels, begin, end);

            targetButtonList.add(newButton);
        } else {
            // CASE #2: This button spans at least 2 lines
            List<AnnotationButton> buttons = new ArrayList<>();

            labels.forEach(label -> {
                double charWidth = label.getTextLabel().getWidth() / label.getLineText().length();
                double topAnchor = characterHeight * targetLabels.indexOf(label) * 2.0d;

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

            targetButtonList.addAll(buttons);
        }
    }

    /**
     * User this for single-line buttons
     * @param annotation    The annotation which will be linked to the button
     * @param label         The label whose index will be used for determining the top offset
     * @param begin         The begin position of the annotation's text
     * @param end           The end position of the annotation's text
     * @return              Returns an AnnotationButton appropriately formatted and offset on top of its text
     */
    private AnnotationButton createButton(JsonObject annotation, LineNumberedLabel label, List<LineNumberedLabel> targetLabels, int begin, int end) {
        double characterWidth = label.getTextLabel().getWidth() / label.getLineText().length();
        double size = characterWidth * (end - begin);
        double topAnchor = characterHeight * targetLabels.indexOf(label) * 2.0d;
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

    private void setupAnnotationButtons() {
        // Link the buttons
        String matchType = AcvContext.getInstance().selectedMatchTypeProperty.getValueSafe().toLowerCase();
        for (AnnotationButton sysOutButton : results.get(CorpusType.SYSTEM)) {
            for(AnnotationButton refButton: results.get(CorpusType.REFERENCE)) {
                matchButtons(sysOutButton, refButton, matchType);
            }

            sysOutButton.textArea = systemOutPane.getFeatureTreeText();
            sysOutButton.parent = systemOutPane.getAnchor();
            sysOutButton.setOnMouseClicked(event -> selectedAnnotationButton.setValue(sysOutButton));
            sysOutButton.checkMatchTypes(AnnotationButton.MatchType.FALSE_POS);
        }

        for (AnnotationButton refButton: results.get(CorpusType.REFERENCE)) {
            refButton.parent = referencePane.getAnchor();
            refButton.textArea = referencePane.getFeatureTreeText();
            refButton.setOnMouseClicked(event -> selectedAnnotationButton.setValue(refButton));
            refButton.checkMatchTypes(AnnotationButton.MatchType.FALSE_NEG);
        }

        /*
         * This could be faster by using either of the loops above; but for the sake of separating the logic,
         * we will use a separate for-loop.  The cost to performance is O(n).  This loop determines
         * which color to assign the buttons based on the type of match.
         */
        results.get(CorpusType.SYSTEM).forEach(button -> button.checkMatchTypes(AnnotationButton.MatchType.FALSE_POS));
        results.get(CorpusType.REFERENCE).forEach(button -> button.checkMatchTypes(AnnotationButton.MatchType.FALSE_NEG));
    }

    /**
     * Assign all the "previous" and "next" values for each of the buttons.  The loop goes through every button except
     * the last one, and finds the next button whose "start" value is greater than its own.
     *
     * There is a running "previous" button assigned to the latest used; this is assigned to the last button on the list's
     * "previous" value if their "start" values differ; otherwise, the last button's "previous" value is this running button's
     * "previous" value as well.
     */
    private void setPreviousAndNextValues() {
        List<AnnotationButton> masterList = new ArrayList<>();
        masterList.addAll(results.get(CorpusType.SYSTEM));
        masterList.addAll(results.get(CorpusType.REFERENCE));

        Collections.sort(masterList);
        AnnotationButton previous = null;
        for (int i = 0; i < masterList.size() - 1; i++) {
            previous = masterList.get(i);

            // Start this loop at the index immediately following i; break out of this loop as soon as a "next" is found
            for (int j = i + 1; j < masterList.size(); j++) {
                AnnotationButton next = masterList.get(j);

                // Link the buttons if next.begin > prev.being
                // OR if (next.begin == prev.begin AND next.end > previous.end)
                if (next.getBegin() > previous.getBegin() ||
                        (next.getBegin() == previous.getBegin() && next.getEnd() > previous.getEnd())) {
                    previous.nextButton = next;

                    if (next.previousButton == null) {
                        next.previousButton = previous;
                        for (AnnotationButton sameButton : next.sameAnnotationButtons) {
                            sameButton.previousButton = previous;
                        }
                    }

                    break;
                }
            }
        }

        AnnotationButton last = masterList.get(masterList.size() - 1);
        if (previous != null) {
            if (previous.matchingButtons.contains(last)) {
                last.previousButton = previous.previousButton;
            } else {
                last.previousButton = previous;
                previous.nextButton = last;
            }
        }
    }

    private static void matchButtons(AnnotationButton systemOutButton, AnnotationButton refButton, String matchType) {
        int beginSysOut = systemOutButton.getBegin();
        int endSysOut = systemOutButton.getEnd();
        int beginRef = refButton.getBegin();
        int endRef = refButton.getEnd();

        switch (matchType) {
            case "partial":
                /*
                 * Partial overlap:
                 * |------|     |---------|  |---|
                 *     |----------|  |----|  |------|
                 */
                if ((beginSysOut >= beginRef && beginSysOut < endRef) || beginRef > beginSysOut && beginRef <= endSysOut) {
                    systemOutButton.matchingButtons.add(refButton);
                    refButton.matchingButtons.add(systemOutButton);
                }
                break;
            case "fully-contained":
                /*
                 * Fully contained only considers when system contains reference
                 * |----------|  |------|
                 *   |--------|    |--|
                 */
                if (beginRef > beginSysOut && endRef <= endSysOut || beginRef >= beginSysOut && endRef < endSysOut) {
                    systemOutButton.matchingButtons.add(refButton);
                    refButton.matchingButtons.add(systemOutButton);
                }
                break;
        }

        if (beginSysOut == beginRef && endSysOut == endRef) {
            systemOutButton.matchingButtons.add(refButton);
            refButton.matchingButtons.add(systemOutButton);
        }
    }
}
