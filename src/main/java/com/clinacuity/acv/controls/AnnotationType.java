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

    public AnnotationType(String name, int tpCount, int fpCount, int fnCount, double recallPercent, double precisionPercent) {
        annotationName.setValue(new CellButton(name));
        truePositiveCount.setValue(Integer.toString(tpCount));
        falsePositiveCount.setValue(Integer.toString(fpCount));
        falseNegativeCount.setValue(Integer.toString(fnCount));
        recall.setValue(Double.toString(recallPercent) + "%");
        precision.setValue(Double.toString(precisionPercent) + "%");
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
