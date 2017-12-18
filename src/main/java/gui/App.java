package gui;

import main.Global;
import models.Currency;
import models.CurrencyPrice;
import models.gui.CurrencyTableModel;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Iwo Skwierawski on 11.12.17.
 * Primary application class, containing execution command and frontend elements.
 */
public class App

{
    private JPanel mainPanel;
    private JTable currencyTable;
    private ChartPanel priceHistory;
    private ChartPanel pricePrediction;
    private JButton year5Button;
    private JButton yearButton;
    private JButton monthButton;
    private JButton weekButton;
    private JButton helpButton;
    private JButton buttonAdd;
    private JButton buttonRemove;
    private JPanel chartPanel;

    private Map<String, ChartPanel> chartPanelsMap = new HashMap<>();                                                   // K: currencyName V: chart panel created for that currency

    private int lastChartPannelUpdated = 0;

    private static final int maxCharts = 4;

    private static final Logger logger = Logger.getLogger(App.class);

    public App() {
        JFrame frame = new JFrame("App");
        $$$setupUI$$$();
        chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.Y_AXIS));
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        connectDataSources();
        helpButton.addActionListener(e -> {
            String helpMessage = "- Zoom in: To zoom graph, select with mouse area you wish to zoom, starting from top left corner.\n" +
                    "- Zoom out: To unzoom, swipe line with mouse, over graph, starting from bottom left corner, to bottom right corner.\n" +
                    "- Add graph: New currency graph is added by using Add graph button, after selecting desired currency on list.\n" +
                    "- Remove graph: Possible, by selecting currency from list, that had previously created graph and then pushing Remove graph button.\n" +
                    "- Graph range: Changed, by selecting one of buttons above graphs with desired range.";
            JOptionPane.showMessageDialog(new JFrame("Help"), helpMessage, "HELP", JOptionPane.QUESTION_MESSAGE);
        });
        buttonAdd.addActionListener(e -> {
            try {
                Currency selectedCurrency = ((CurrencyTableModel) currencyTable.getModel()).getObjectAt(currencyTable.getSelectedRow());
                addChartPanel(createChart(selectedCurrency.getName(), selectedCurrency.getAvgPrices()));
            } catch (ArrayIndexOutOfBoundsException ex) {
                JOptionPane.showMessageDialog(new JFrame("Error"), "Please choose a currency for which you wish to create graph.", "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeChartPanel();
            }
        });
    }

    /**
     * Adds data to currencyTable and adjusts column sizes
     */
    public void connectDataSources() {
        currencyTable.setModel(new CurrencyTableModel(Global.currencyService.getAllCurrencies()));
        currencyTable.getColumnModel().getColumn(0).setMinWidth(50);
        currencyTable.getColumnModel().getColumn(0).setMaxWidth(350);
        currencyTable.getColumnModel().getColumn(1).setMinWidth(50);
        currencyTable.getColumnModel().getColumn(1).setMaxWidth(50);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        currencyTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
    }

    /**
     * IntelliJ IDEA method, responsible for creating custom components.
     */
    private void createUIComponents() {
        priceHistory = new ChartPanel(null, true, true, true, false, true);
        pricePrediction = new ChartPanel(null, true, true, true, false, true);
    }

    /**
     * Creates a chart.
     *
     * @return a chart.
     */
    private static JFreeChart createChart(String title, Set<CurrencyPrice> prices) {

        XYDataset priceData = createPriceDataset(prices);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title,
                "Date",
                "Price",
                priceData,
                true,
                true,
                false
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis rangeAxis1 = (NumberAxis) plot.getRangeAxis();
        rangeAxis1.setLowerMargin(0.40);  // to leave room for volume bars
        DecimalFormat format = new DecimalFormat("00.00");
        rangeAxis1.setNumberFormatOverride(format);

        XYItemRenderer renderer1 = plot.getRenderer();
        renderer1.setBaseToolTipGenerator(new StandardXYToolTipGenerator(
                StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
                new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0.00")));

        NumberAxis rangeAxis2 = new NumberAxis("Volume");
        rangeAxis2.setUpperMargin(1.00);  // to leave room for price line
        plot.setRangeAxis(1, rangeAxis2);
        plot.setDataset(1, createVolumeDataset(prices));
        plot.setRangeAxis(1, rangeAxis2);
        plot.mapDatasetToRangeAxis(1, 1);
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
     * Creates a sample dataset.
     *
     * @return A sample dataset.
     */
    private static XYDataset createPriceDataset(Set<CurrencyPrice> prices) {
        TimeSeries series1 = new TimeSeries("Price");

        prices.stream().forEach(price -> {
            series1.add(new Day(price.getDate().getDayOfMonth(), price.getDate().getMonthOfYear(), price.getDate().getYear()), price.getPrice());
        });
        return new TimeSeriesCollection(series1);

    }

    /**
     * Creates a sample dataset.
     *
     * @return A sample dataset.
     */
    private static IntervalXYDataset createVolumeDataset(Set<CurrencyPrice> prices) {

        TimeSeries series1 = new TimeSeries("Volume");

        prices.stream().forEach(price -> {
            series1.add(new Day(price.getDate().getDayOfMonth(), price.getDate().getMonthOfYear(), price.getDate().getYear()), price.getPrice());
        });
        return new TimeSeriesCollection(series1);

    }

    private void addChartPanel(JFreeChart chart) {
        ChartPanel newChartPanel = new ChartPanel(chart, true, true, true, false, true);
        newChartPanel.setToolTipText("Select area to zoom in");
        chartPanel.add(newChartPanel);
        chartPanel.revalidate();
        chartPanel.repaint();
        chartPanelsMap.put(chart.getTitle().getText(), newChartPanel);
        logger.debug("Created chart panel!");
    }

    private void removeChartPanel() {
        try {
            Currency selectedCurrency = ((CurrencyTableModel) currencyTable.getModel()).getObjectAt(currencyTable.getSelectedRow());
            chartPanel.remove(chartPanelsMap.get(selectedCurrency.getName()));
            chartPanel.revalidate();
            chartPanel.repaint();
            chartPanelsMap.remove(selectedCurrency.getName());
        } catch (ArrayIndexOutOfBoundsException ex) {
            JOptionPane.showMessageDialog(new JFrame("Error"), "Please choose a currency for which you wish to create graph.", "ERROR", JOptionPane.ERROR_MESSAGE);
        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(new JFrame("Error"), "This graph wasn't previously created.", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.setMaximumSize(new Dimension(0, 0));
        mainPanel.setMinimumSize(new Dimension(800, 600));
        mainPanel.setPreferredSize(new Dimension(1360, 768));
        mainPanel.setToolTipText("");
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        mainPanel.add(panel1, BorderLayout.WEST);
        final JToolBar toolBar1 = new JToolBar();
        panel1.add(toolBar1, BorderLayout.NORTH);
        buttonAdd = new JButton();
        buttonAdd.setText("Add Graph");
        toolBar1.add(buttonAdd);
        buttonRemove = new JButton();
        buttonRemove.setText("Remove Graph");
        toolBar1.add(buttonRemove);
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setHorizontalScrollBarPolicy(31);
        scrollPane1.setPreferredSize(new Dimension(400, 600));
        panel1.add(scrollPane1, BorderLayout.WEST);
        currencyTable = new JTable();
        currencyTable.setAutoResizeMode(4);
        currencyTable.setMaximumSize(new Dimension(400, 600));
        currencyTable.setPreferredScrollableViewportSize(new Dimension(400, 600));
        currencyTable.setToolTipText("");
        scrollPane1.setViewportView(currencyTable);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        mainPanel.add(panel2, BorderLayout.CENTER);
        final JToolBar toolBar2 = new JToolBar();
        panel2.add(toolBar2, BorderLayout.NORTH);
        year5Button = new JButton();
        year5Button.setMaximumSize(new Dimension(60, 32));
        year5Button.setMinimumSize(new Dimension(60, 32));
        year5Button.setPreferredSize(new Dimension(60, 32));
        year5Button.setText("5y");
        year5Button.setToolTipText("5 years period");
        toolBar2.add(year5Button);
        yearButton = new JButton();
        yearButton.setMaximumSize(new Dimension(60, 32));
        yearButton.setMinimumSize(new Dimension(60, 32));
        yearButton.setPreferredSize(new Dimension(60, 32));
        yearButton.setText("1y");
        yearButton.setToolTipText("1 year period");
        toolBar2.add(yearButton);
        monthButton = new JButton();
        monthButton.setMaximumSize(new Dimension(60, 32));
        monthButton.setMinimumSize(new Dimension(60, 32));
        monthButton.setPreferredSize(new Dimension(60, 32));
        monthButton.setText("month");
        monthButton.setToolTipText("1 month period");
        toolBar2.add(monthButton);
        weekButton = new JButton();
        weekButton.setMaximumSize(new Dimension(60, 32));
        weekButton.setMinimumSize(new Dimension(60, 32));
        weekButton.setPreferredSize(new Dimension(60, 32));
        weekButton.setText("week");
        weekButton.setToolTipText("1 week period");
        toolBar2.add(weekButton);
        helpButton = new JButton();
        helpButton.setHorizontalAlignment(0);
        helpButton.setLabel("?");
        helpButton.setMaximumSize(new Dimension(30, 32));
        helpButton.setMinimumSize(new Dimension(30, 32));
        helpButton.setPreferredSize(new Dimension(30, 32));
        helpButton.setText("?");
        helpButton.setToolTipText("Help");
        toolBar2.add(helpButton);
        final JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setAutoscrolls(true);
        scrollPane2.setHorizontalScrollBarPolicy(30);
        panel2.add(scrollPane2, BorderLayout.CENTER);
        chartPanel = new JPanel();
        chartPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        chartPanel.setAutoscrolls(true);
        scrollPane2.setViewportView(chartPanel);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
