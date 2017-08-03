package com.clinacuity.acv.tasks;

import com.clinacuity.acv.controls.AnnotatedDocumentPane;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class CreateLabelsFromDocumentTask extends Task<List<Label>> {
    private static final Logger logger = LogManager.getLogger();
    private String[] lines;
    private List<Label> lineNumbers = new ArrayList<>();
    public List<Label> getLineNumbers() { return lineNumbers; }

    public CreateLabelsFromDocumentTask(String rawText) {
        lines = rawText.split("\n");
    }

    @Override public List<Label> call() {
        List<Label> labelList = new ArrayList<>();

        // TODO: improve the line-wrapping to break on spaces
        int maxChars = AnnotatedDocumentPane.getMaxCharactersPerLabel() - 10;
        double offset = 0.0d;
        double offsetIncrement = AnnotatedDocumentPane.getCharacterHeight() * 2.0d;

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
                    labelList.add(getTextLabel(subLine, offset));
                    lineNumbers.add(getNextLineNumber(Integer.valueOf(i + 1).toString(), offset));
                    offset += offsetIncrement;
                    line = line.substring(subLine.length());
                }
            } else {
                labelList.add(getTextLabel(line, offset));
                lineNumbers.add(getNextLineNumber(Integer.valueOf(i + 1).toString(), offset));
                offset += offsetIncrement;
            }
        }

        succeeded();
        return labelList;
    }

    private Label getTextLabel(String line, double offset) {
        Label label = new Label(line);
        label.getStyleClass().clear();
        label.getStyleClass().add("mono-text");
        AnchorPane.setTopAnchor(label, AnnotatedDocumentPane.STANDARD_INSET + offset);
        AnchorPane.setLeftAnchor(label, AnnotatedDocumentPane.LINE_NUMBER_WIDTH);

        return label;
    }

    private Label getNextLineNumber(String number, double offset) {
        Label lineNumber = new Label(number);
        lineNumber.getStyleClass().clear();
        lineNumber.getStyleClass().add("line-number");
        lineNumber.setMaxWidth(AnnotatedDocumentPane.LINE_NUMBER_WIDTH * 0.9d);
        lineNumber.setMinWidth(AnnotatedDocumentPane.LINE_NUMBER_WIDTH * 0.9d);
        AnchorPane.setTopAnchor(lineNumber, AnnotatedDocumentPane.STANDARD_INSET + offset);
        AnchorPane.setLeftAnchor(lineNumber, 0.0d);

        return lineNumber;
    }
}
