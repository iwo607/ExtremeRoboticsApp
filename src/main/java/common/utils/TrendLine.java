package common.utils;

/**
 * Created by Iwo Skwierawski on 2017-12-20.
 */
public interface TrendLine
{
    /**
     * Data that will be used to create trend line
     * @param y  y-axis values of graph
     * @param x x-axis values of graph
     */
    public void setValues(double[] y, double[] x);

    /**
     * Gives a predicted y for a given x
     * @param x x-axis value
     * @return predicted value of y-axis
     */
    public double predict(double x); //
}