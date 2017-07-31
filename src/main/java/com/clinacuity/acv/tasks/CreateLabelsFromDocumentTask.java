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
    private double offsetIncrement = 0.0d;

    public CreateLabelsFromDocumentTask(String rawText, double characterHeight) {
        lines = rawText.split("\n");
        offsetIncrement = characterHeight * 2.0d;
    }

    @Override public List<Label> call() {
        List<Label> labelList = new ArrayList<>();

        double offset = 0.0d;
        for (String line: lines) {
            Label label = new Label(line);
            label.getStyleClass().clear();
            label.getStyleClass().add("mono-text");
            AnchorPane.setTopAnchor(label, AnnotatedDocumentPane.STANDARD_INSET + offset);
            AnchorPane.setLeftAnchor(label, AnnotatedDocumentPane.STANDARD_INSET);
            labelList.add(label);
            offset += offsetIncrement;
        }

        logger.error(offset);

        succeeded();
        return labelList;
    }
}
