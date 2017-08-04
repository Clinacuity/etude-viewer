package com.clinacuity.acv.controls;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class LineNumberedLabel extends HBox {

    @FXML private Label textLabel;
    @FXML private Label lineNumberLabel;
    private int lineNumberIndex = 0;

    public String getLineText() { return textLabel.getText(); }
    public int getLineNumberIndex() { return lineNumberIndex; }

    public LineNumberedLabel(String labelText, int lineNumber) {
        super();

        lineNumberIndex = lineNumber;

        lineNumberLabel = new Label(Integer.toString(lineNumberIndex + 1));
        lineNumberLabel.getStyleClass().add("line-number");
        getChildren().add(lineNumberLabel);

        textLabel = new Label(labelText);
        textLabel.getStyleClass().add("mono-text");
        getChildren().add(textLabel);
    }
}
