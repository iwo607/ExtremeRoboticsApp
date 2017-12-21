package services;

import models.CurrencyPrice;
import org.jfree.chart.JFreeChart;

import java.util.List;
import java.util.Set;

/**
 * Created by Iwo Skwierawski on 2017-12-21.
 * Contains methods, that are used while working with charts
 */
public interface ChartService
{
    /**
     * Creates chart, that predicts the future... (TRUE STORY! Greetings from Hawaii!)
     * @param title chart title
     * @param prices data, that we will operate on
     * @param timeToPredict how far ahead we make prediction
     * @return newly created, shiny graph
     */
    JFreeChart createPredictionChart(String title, Set<CurrencyPrice> prices, int timeToPredict);

    /**
     * Creates a chart, where x-axis is date and y-axis us price.
     * Data is represented as blue bars with red trend line over them.
     * @param title chart title
     * @param prices data, from which chart will be created (hopefully)
     * @return a chart, filled with data.
     */
    JFreeChart createChart(String title, List<CurrencyPrice> prices);
    JFreeChart createChart(String title, Set<CurrencyPrice> prices);
}
