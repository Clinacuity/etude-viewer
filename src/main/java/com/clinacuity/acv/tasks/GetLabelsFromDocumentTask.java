package com.clinacuity.acv.tasks;

import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class GetLabelsFromDocumentTask extends Task<List<Label>> {
    private static final Logger logger = LogManager.getLogger();
    private String[] lines;
    private double offsetIncrement = 0.0d;

    public GetLabelsFromDocumentTask(String[] lines, double characterHeight) {
        this.lines = lines;
        offsetIncrement = characterHeight * 2.0d;
    }

    @Override public List<Label> call() {
        List<Label> labelList = new ArrayList<>();

        double offset = 0.0d;
        for (String line: lines) {
            Label label = new Label(line);
            label.getStyleClass().clear();
            label.getStyleClass().add("mono-text");
            AnchorPane.setTopAnchor(label, offset);
            labelList.add(label);
            offset += offsetIncrement;
        }

        succeeded();
        return labelList;
    }
}
