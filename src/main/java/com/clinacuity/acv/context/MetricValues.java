package com.clinacuity.acv.context;

public class MetricValues {
    private double truePositive = 0.0d;
    private double falsePositive = 0.0d;
    private double falseNegative = 0.0d;
    private double trueNegative = 0.0d;

    public double getTruePositive() { return truePositive; }
    public double getFalsePositive() { return falsePositive; }
    public double getFalseNegative() { return falseNegative; }
    public double getTrueNegative() { return trueNegative; }

    public MetricValues(double truePos, double falsePos, double falseNeg) {
        this(truePos, falsePos, falseNeg, 0.0d);
    }

    public MetricValues(double truePos, double falsePos, double falseNeg, double trueNeg) {
        truePositive = truePos;
        falsePositive = falsePos;
        falseNegative = falseNeg;
        trueNegative = trueNeg;
    }

    @Override
    public String toString() {
        return String.format("[%s, %s, %s, %s]", truePositive, falsePositive, falseNegative, trueNegative);
    }
}
