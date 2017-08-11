package com.clinacuity.acv.controls;

import com.jfoenix.controls.JFXButton;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class AnnotationType {
    private final SimpleObjectProperty<CellButton> annotationName = new SimpleObjectProperty<>();
    private final SimpleStringProperty truePositiveCount = new SimpleStringProperty();
    private final SimpleStringProperty falsePositiveCount = new SimpleStringProperty();
    private final SimpleStringProperty falseNegativeCount = new SimpleStringProperty();
    private final SimpleStringProperty recall = new SimpleStringProperty();
    private final SimpleStringProperty precision = new SimpleStringProperty();

    public AnnotationType(String name, double tpCount, double fpCount, double fnCount, double recallPercent, double precisionPercent) {
        annotationName.setValue(new CellButton(name));
        truePositiveCount.setValue(Integer.toString((int)tpCount));
        falsePositiveCount.setValue(Integer.toString((int)fpCount));
        falseNegativeCount.setValue(Integer.toString((int)fnCount));
        recall.setValue(String.format("%,.2f%%", recallPercent));
        precision.setValue(String.format("%,.2f%%", precisionPercent));
    }

    public String getAnnotationName() { return annotationName.getValue().getText(); }
    public String getTruePositiveCount() { return truePositiveCount.getValueSafe(); }
    public String getFalsePositiveCount() { return falsePositiveCount.getValueSafe(); }
    public String getFalseNegativeCount() { return falseNegativeCount.getValueSafe(); }
    public String getRecall() { return recall.getValueSafe(); }
    public String getPrecision() { return precision.getValueSafe(); }

    private class CellButton extends JFXButton {
        private CellButton(String name) {
            super(name);
        }
    }
}
