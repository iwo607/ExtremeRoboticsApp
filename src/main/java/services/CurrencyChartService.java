package services;

import common.utils.PolyTrendLine;
import common.utils.TrendLine;
import models.CurrencyPrice;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Created by Iwo Skwierawski on 2017-12-21.
 * Implements methods for usage chart with currencies
 */
public class CurrencyChartService implements ChartService
{
    @Override
    public JFreeChart createPredictionChart(String title, Set<CurrencyPrice> prices, int timeToPredict)
    {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title + " - Prediction",
                "Date",
                "Price",
                createPredictionTrendLine(prices, timeToPredict),
                true,
                true,
                false
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis rangeAxis1 = (NumberAxis) plot.getRangeAxis();
        DecimalFormat format = new DecimalFormat("0.000");
        rangeAxis1.setNumberFormatOverride(format);

        ChartUtilities.applyCurrentTheme(chart);

        return chart;
    }

    @Override
    public JFreeChart createChart(String title, Set<CurrencyPrice> prices) {
        return createChart(title, new ArrayList<>(prices));
    }

    @Override
    public JFreeChart createChart(String title, List<CurrencyPrice> prices) {

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title,
                "Date",
                "Price",
                createTrendLine(prices),
                true,
                true,
                false
        );
        XYPlot plot = (XYPlot) chart.getPlot();

        plot.setDataset(1, createPriceDataSet(prices));
        plot.mapDatasetToRangeAxis(1, 0);
        XYBarRenderer renderer2 = new XYBarRenderer(0.20);
        renderer2.setBaseToolTipGenerator(
                new StandardXYToolTipGenerator(
                        StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
                        new SimpleDateFormat("d-MMM-yyyy"),
                        new DecimalFormat("0.00")));
        plot.setRenderer(1, renderer2);
        ChartUtilities.applyCurrentTheme(chart);
        renderer2.setBarPainter(new StandardXYBarPainter());
        renderer2.setShadowVisible(false);
        return chart;

    }

    /**
     * Creates a trend line for provided data.
     *
     * @return Trend line represented as XYDataSet
     */
    private static XYDataset createTrendLine(List<CurrencyPrice> prices)
    {
        TimeSeries series1 = new TimeSeries("Trend Line");
        TrendLine t = new PolyTrendLine(2);
        t.setValues(prices.stream().mapToDouble(CurrencyPrice::getPrice).toArray(), prices.stream().map(CurrencyPrice::getDate).mapToDouble(DateTime::getMillis).toArray());

        prices.forEach(price -> series1.add(new Day(price.getDate().getDayOfMonth(), price.getDate().getMonthOfYear(), price.getDate().getYear()), t.predict(price.getDate().getMillis())));
        return new TimeSeriesCollection(series1);

    }

    /**
     * Creates prediction trend line based on previous trends
     * @param prices list of past prices for currency
     * @param timeToPredict how much days ahead to predict (affects accuracy)
     * @return dataSet for price prediction chart
     */
    private static XYDataset createPredictionTrendLine(Set<CurrencyPrice> prices, int timeToPredict)
    {
        TimeSeries series1 = new TimeSeries("Prediction");

        TrendLine t = new PolyTrendLine(2);
        t.setValues(prices.stream().mapToDouble(CurrencyPrice::getPrice).toArray(), prices.stream().map(CurrencyPrice::getDate).mapToDouble(DateTime::getMillis).toArray());
        List<Double> mostRecentData = new ArrayList<>();                                                                // Prices from past (timeToPredict) days are taken into consideration when predicting future. Ordered by date, ascending.
        prices.stream().filter(price -> price.getDate().getMillis() >= DateTime.now().minusDays(timeToPredict).getMillis()).sorted(Comparator.comparing(CurrencyPrice::getDate)).forEach(price -> mostRecentData.add(t.predict(price.getDate().getMillis())));

        Double avgSpeed = 0d;
        Double acceleration = 0d;
        List<Double> speeds = new ArrayList<>();

        for (int i = 1; i < mostRecentData.size(); i++)                                                                 // Loop for calculating avg. speed of rising or falling of currency price in time range
        {
            Double speed = mostRecentData.get(i) - mostRecentData.get(i - 1);
            avgSpeed += speed;
            speeds.add(speed);
        }
        avgSpeed = avgSpeed / mostRecentData.size();
        for (int i = 1; i < speeds.size(); i++)                                                                         // Loop for calculating how much speed accelerates every day if at all
        {
            acceleration += speeds.get(i) - speeds.get(i - 1);
        }

        Double lastData = mostRecentData.get(mostRecentData.size() - 1);

        for (int i = 0; i < mostRecentData.size(); i++)                                                                 // Draws line based on data. Last price is enlarged by (days * (avg speed of rising/falling + days of acceleration * value of acceleration)
        {
            DateTime futureDay = DateTime.now().plusDays(i);
            series1.add(new Day(futureDay.getDayOfMonth(), futureDay.getMonthOfYear(), futureDay.getYear()), lastData + i * (avgSpeed + i * acceleration));
        }

        return new TimeSeriesCollection(series1);

    }

    /**
     * Creates bar DataSet from archived currency prices.
     *
     * @return Bar DataSet used by JFreeChart framework
     */
    private static IntervalXYDataset createPriceDataSet(List<CurrencyPrice> prices)
    {

        TimeSeries series1 = new TimeSeries("Price");

        prices.forEach(price -> series1.add(new Day(price.getDate().getDayOfMonth(), price.getDate().getMonthOfYear(), price.getDate().getYear()), price.getPrice()));
        return new TimeSeriesCollection(series1);

    }
}
