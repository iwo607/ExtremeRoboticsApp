package common.utils;

/**
 * Created by Iwo Skwierawski on 2017-12-20.
 */
public class ExpTrendLine extends OLSTrendLine {
    @Override
    protected double[] xVector(double x) {
        return new double[]{1,x};
    }

    @Override
    protected boolean logY() {return true;}
}
