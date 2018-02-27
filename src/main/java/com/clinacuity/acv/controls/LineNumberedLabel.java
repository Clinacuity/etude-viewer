package com.clinacuity.acv.controls;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

@SuppressWarnings("FieldCanBeLocal")
public class LineNumberedLabel extends HBox implements Comparable<LineNumberedLabel> {

    @FXML private Label textLabel;
    @FXML private Label lineNumberLabel;
    private int lineNumberIndex = 0;
    private int offset = 0;

    public Label getTextLabel() { return textLabel; }
    public String getLineText() { return textLabel.getText(); }
    public int getLineNumberIndex() { return lineNumberIndex; }
    public int getTextOffset() { return offset; }
    public void setTextOffset(int value) { offset = value; }

    public LineNumberedLabel(String labelText, int lineNumber) {
        super();

        lineNumberIndex = lineNumber;

        lineNumberLabel = new Label(Integer.toString(lineNumberIndex));
        lineNumberLabel.getStyleClass().add("line-number");
        lineNumberLabel.setMinWidth(AnnotatedDocumentPane.LINE_NUMBER_WIDTH);
        lineNumberLabel.setMaxWidth(AnnotatedDocumentPane.LINE_NUMBER_WIDTH);
        getChildren().add(lineNumberLabel);

        textLabel = new Label(labelText);
        textLabel.getStyleClass().add("mono-text");
        getChildren().add(textLabel);
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * <p>
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     * <p>
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     * <p>
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     * <p>
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     * <p>
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(LineNumberedLabel o) {
        return Integer.compare(lineNumberIndex, o.lineNumberIndex);
    }
}
