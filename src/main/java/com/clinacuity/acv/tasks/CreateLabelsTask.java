package com.clinacuity.acv.tasks;

import com.clinacuity.acv.controls.AnnotatedDocumentPane;
import com.clinacuity.acv.controls.LineNumberedLabel;
import javafx.concurrent.Task;
import javafx.scene.layout.AnchorPane;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.clinacuity.acv.controllers.ConfigurationBuilderController.CorpusType;

public class CreateLabelsTask extends Task<Map<CorpusType, List<LineNumberedLabel>>> {
    private String sysRawText;
    private String refRawText;

    public CreateLabelsTask(String sysRawText, String refRawText) {
        this.sysRawText = sysRawText;
        this.refRawText = refRawText;
    }

    @Override
    public Map<CorpusType, List<LineNumberedLabel>> call() {
        Map<CorpusType, List<LineNumberedLabel>> map = new HashMap<>();

        map.put(CorpusType.SYSTEM, getLines(sysRawText));
        map.put(CorpusType.REFERENCE, getLines(refRawText));
        updateValue(map);
        succeeded();
        return map;
    }

    private List<LineNumberedLabel> getLines(String rawText) {
        String[] lines = rawText.split("\n");
        List<LineNumberedLabel> labelList = new ArrayList<>();

        // TODO: DEV-24; improve the line-wrapping to break on spaces
        int maxChars = AnnotatedDocumentPane.MAX_CHARACTERS_PER_LABEL;
        double offset = 0.0d;
        double offsetIncrement = AnnotatedDocumentPane.CHARACTER_HEIGHT * 2.0d;

        int runningLength = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // TODO: DEV-24; UI checkbox bound to this toggle
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
