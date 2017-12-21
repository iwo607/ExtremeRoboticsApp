package gui;

import main.Global;
import models.Currency;
import models.CurrencyPrice;
import models.gui.CurrencyTableModel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.joda.time.DateTime;
import services.CurrencyChartService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.util.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Iwo Skwierawski on 11.12.17.
 * Primary FrontEnd class, containing form elements and action listeners
 */
public class App

{
    private JPanel mainPanel;
    private JTable currencyTable;
    private JButton year5Button;
    private JButton yearButton;
    private JButton monthButton;
    private JButton weekButton;
    private JButton helpButton;
    private JButton buttonAdd;
    private JPanel chartPanel;
    private JButton month6button;

    private Map<String, ChartPanel> chartPanelsMap = new HashMap<>();                                                   // K: currencyName V: chart panel created for that currency

    private static final CurrencyChartService chartService = new CurrencyChartService();

    /**
     * Constructor responsible for creating whole FrontEnd
     */
    public App()
    {
        JFrame frame = new JFrame("Extreme Robotics App");
        Image image = new ImageIcon("files/res/icon.jpg").getImage();
        frame.setIconImage(image);
        $$$setupUI$$$();
        chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.Y_AXIS));
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        connectDataSources();
        createActionListeners();
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
     * Adds new chart panel to JScrollPane
     *
     * @param chart chart, that this chart panel will contain
     */
    private void addChartPanel(JFreeChart chart, JFreeChart predictionChart) {
        ChartPanel newChartPanel = new ChartPanel(chart, true, true, true, false, true);
        ChartPanel predictionChartPanel = new ChartPanel(predictionChart);
        newChartPanel.setToolTipText("Select area to zoom in");
        predictionChartPanel.setToolTipText("Select area to zoom in");
        JPanel toolBar1 = new JPanel();
        toolBar1.setLayout(new BorderLayout(0, 0));
        JButton closeButton = new JButton();
        closeButton.setMaximumSize(new Dimension(30, 30));
        closeButton.setMinimumSize(new Dimension(30, 30));
        closeButton.setPreferredSize(new Dimension(30, 30));
        closeButton.setText("X");
        closeButton.setToolTipText("Remove graph");
        closeButton.setMargin(new Insets(0, 0, 0, 0));
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.addActionListener(e -> {
            removeChartPanel(newChartPanel);
            removeChartPanel(predictionChartPanel);
        });
        toolBar1.add(closeButton, BorderLayout.EAST);
        toolBar1.setOpaque(false);
        newChartPanel.setLayout(new BorderLayout(0, 0));
        newChartPanel.add(toolBar1, BorderLayout.NORTH);
        chartPanel.add(newChartPanel);
        chartPanel.add(predictionChartPanel);
        chartPanel.revalidate();
        chartPanel.repaint();
        chartPanelsMap.put(chart.getTitle().getText(), newChartPanel);
        chartPanelsMap.put(predictionChart.getTitle().getText(), predictionChartPanel);
    }

    /**
     * Removes chart panel for currently selected currency from JScrollPane
     */
    private void removeChartPanel(ChartPanel panelToRemove) {
        try {
            chartPanel.remove(panelToRemove);
            chartPanel.revalidate();
            chartPanel.repaint();
            chartPanelsMap.remove(panelToRemove.getChart().getTitle().getText());
        } catch (ArrayIndexOutOfBoundsException ex) {
            JOptionPane.showMessageDialog(new JFrame("Error"), "Please choose a currency for which you wish to create graph.", "ERROR", JOptionPane.ERROR_MESSAGE);
        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(new JFrame("Error"), "This graph wasn't previously created.", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Adds action listeners to form elements.
     */
    private void createActionListeners() {
        helpButton.addActionListener(e -> {                                                                             // Shows help window on click.
            String helpMessage = "- Zoom in: To zoom graph, select with mouse area you wish to zoom, starting from top left corner.\n" +
                    "- Zoom out: To unzoom, swipe line with mouse, over graph, starting from bottom left corner, to bottom right corner.\n" +
                    "- Add graph: New currency graph is added by using Add graph button, after selecting desired currency on list.\n" +
                    "- Remove graph: Possible, by selecting currency from list, that had previously created graph and then pushing Remove graph button.\n" +
                    "- Graph range: Changed, by selecting one of buttons above graphs with desired range.";
            JOptionPane.showMessageDialog(new JFrame("Help"), helpMessage, "HELP", JOptionPane.QUESTION_MESSAGE);
        });
        buttonAdd.addActionListener(e -> {                                                                              // Creates new graph on click. Currency must be selected from list
            try {
                Currency selectedCurrency = ((CurrencyTableModel) currencyTable.getModel()).getObjectAt(currencyTable.getSelectedRow());
                if (chartPanelsMap.get(selectedCurrency.getName()) == null)
                    addChartPanel(chartService.createChart(selectedCurrency.getName(), selectedCurrency.getAvgPrices()), chartService.createPredictionChart(selectedCurrency.getName(), selectedCurrency.getAvgPrices(), 30));
                else
                    JOptionPane.showMessageDialog(new JFrame("Error"), "Graph already created for that currency.", "ERROR", JOptionPane.ERROR_MESSAGE);
            } catch (ArrayIndexOutOfBoundsException ex) {
                JOptionPane.showMessageDialog(new JFrame("Error"), "Please choose a currency for which you wish to create graph.", "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        });
        year5Button.addActionListener(e -> {                                                                            // On click, sets scope of each graph to 5 years
            chartPanelsMap.forEach((k, v) -> {
                String processedKey = k.replace(" - Prediction", "");
                Currency currency = ((CurrencyTableModel) currencyTable.getModel()).getCurrencyByName(processedKey);
                if (k.contains("Prediction"))
                    v.setChart(chartService.createPredictionChart(processedKey, currency.getAvgPrices(), 30));
                else {
                    List<CurrencyPrice> filteredPrices = currency.getAvgPrices().stream().filter(cp -> cp.getDate().plusYears(5).getMillis() >= DateTime.now().getMillis()).collect(Collectors.toList());
                    v.setChart(chartService.createChart(k, filteredPrices));
                }

            });
            chartPanel.revalidate();
            chartPanel.repaint();
        });
        yearButton.addActionListener(e -> {                                                                             // On click, sets scope of each graph to one year
            chartPanelsMap.forEach((k, v) -> {
                String processedKey = k.replace(" - Prediction", "");
                Currency currency = ((CurrencyTableModel) currencyTable.getModel()).getCurrencyByName(processedKey);
                if (k.contains("Prediction"))
                    v.setChart(chartService.createPredictionChart(processedKey, currency.getAvgPrices(), 30));
                else {
                    List<CurrencyPrice> filteredPrices = currency.getAvgPrices().stream().filter(cp -> cp.getDate().plusYears(1).getMillis() >= DateTime.now().getMillis()).collect(Collectors.toList());
                    v.setChart(chartService.createChart(k, filteredPrices));
                }
            });
            chartPanel.revalidate();
            chartPanel.repaint();
        });
        month6button.addActionListener(e -> {                                                                           // On click, sets scope of each graph to six month
            chartPanelsMap.forEach((k, v) -> {
                String processedKey = k.replace(" - Prediction", "");
                Currency currency = ((CurrencyTableModel) currencyTable.getModel()).getCurrencyByName(processedKey);
                if (k.contains("Prediction"))
                    v.setChart(chartService.createPredictionChart(processedKey, currency.getAvgPrices(), 30));
                else {
                    List<CurrencyPrice> filteredPrices = currency.getAvgPrices().stream().filter(cp -> cp.getDate().plusMonths(6).getMillis() >= DateTime.now().getMillis()).collect(Collectors.toList());
                    v.setChart(chartService.createChart(k, filteredPrices));
                }
            });
            chartPanel.revalidate();
            chartPanel.repaint();
        });
        monthButton.addActionListener(e -> {                                                                            // On click, sets scope of each graph to one month
            chartPanelsMap.forEach((k, v) -> {
                String processedKey = k.replace(" - Prediction", "");
                Currency currency = ((CurrencyTableModel) currencyTable.getModel()).getCurrencyByName(processedKey);
                if (k.contains("Prediction"))
                    v.setChart(chartService.createPredictionChart(processedKey, currency.getAvgPrices(), 30));
                else {
                    List<CurrencyPrice> filteredPrices = currency.getAvgPrices().stream().filter(cp -> cp.getDate().plusMonths(1).getMillis() >= DateTime.now().getMillis()).collect(Collectors.toList());
                    v.setChart(chartService.createChart(k, filteredPrices));
                }
            });
            chartPanel.revalidate();
            chartPanel.repaint();
        });
        weekButton.addActionListener(e -> {                                                                             // On click, sets scope of each graph to one week
            chartPanelsMap.forEach((k, v) -> {
                String processedKey = k.replace(" - Prediction", "");
                Currency currency = ((CurrencyTableModel) currencyTable.getModel()).getCurrencyByName(processedKey);
                List<CurrencyPrice> filteredPrices = currency.getAvgPrices().stream().filter(cp -> cp.getDate().plusWeeks(1).getMillis() >= DateTime.now().getMillis()).collect(Collectors.toList());
                if (k.contains("Prediction"))
                    v.setChart(chartService.createPredictionChart(processedKey, new HashSet<>(filteredPrices), 7));
                else {
                    v.setChart(chartService.createChart(k, filteredPrices));
                }
            });
            chartPanel.revalidate();
            chartPanel.repaint();
        });
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
        month6button = new JButton();
        month6button.setMaximumSize(new Dimension(60, 32));
        month6button.setMinimumSize(new Dimension(60, 32));
        month6button.setPreferredSize(new Dimension(60, 32));
        month6button.setText("6m");
        toolBar2.add(month6button);
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
