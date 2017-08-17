package com.clinacuity.acv.tasks;

import com.clinacuity.acv.controls.AnnotatedDocumentPane;
import com.clinacuity.acv.controls.LineNumberedLabel;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CreateLabelsFromDocumentTask extends Task<List<LineNumberedLabel>> {
    private static final Logger logger = LogManager.getLogger();
    private String[] lines;
    private List<Label> lineNumbers = new ArrayList<>();
    public List<Label> getLineNumbers() { return lineNumbers; }

    public CreateLabelsFromDocumentTask(String rawText) {
        lines = rawText.split("\n");
    }

    @Override public List<LineNumberedLabel> call() {
        List<LineNumberedLabel> labelList = new ArrayList<>();

        // TODO: improve the line-wrapping to break on spaces
        int maxChars = AnnotatedDocumentPane.getMaxCharactersPerLabel();
        double offset = 0.0d;
        double offsetIncrement = AnnotatedDocumentPane.getCharacterHeight() * 2.0d;
        if (offsetIncrement <= 0.0d) {
            offsetIncrement = 34.0d;
        }

        int runningLength = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // TODO: always true; needs DEV-24 and a UI checkbox bound to this toggle
//            boolean softWrapEnabled = true;
//            if (softWrapEnabled) {
//                // code-block from below
//            }
//            } else {
//                labelList.add(getTextLabel(line, offset));
//                lineNumbers.add(getNextLineNumber(Integer.valueOf(i + 1).toString(), offset));
//                offset += offsetIncrement;
//            }

            if (line.length() > maxChars) {
                while (line.length() > 0) {
                    String subLine = line.length() > maxChars ? line.substring(0, maxChars) : line;

                    LineNumberedLabel label = new LineNumberedLabel(subLine, i + 1);
                    label.setTextOffset(runningLength);
                    AnchorPane.setTopAnchor(label, AnnotatedDocumentPane.STANDARD_INSET + offset);
                    labelList.add(label);

                    offset += offsetIncrement;
                    runningLength += subLine.length();
                    line = line.substring(subLine.length());
                }

                runningLength++;
            } else {
                LineNumberedLabel label = new LineNumberedLabel(line, i + 1);
                label.setTextOffset(runningLength);
                AnchorPane.setTopAnchor(label, AnnotatedDocumentPane.STANDARD_INSET + offset);
                labelList.add(label);
                offset += offsetIncrement;
                runningLength += line.length() + 1;
            }
        }

        succeeded();
        return labelList;
    }
}
