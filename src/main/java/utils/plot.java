package utils;

import java.io.IOException;
import javax.swing.JFrame;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.RectangleInsets;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;


/**
 * @author imssbora
 *
 */
public class plot extends JFrame {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
	public plot(String title, String chartTitle, XYDataset dataset, String fileName, int n) throws IOException {
        super(title);

        // Set out chart
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                chartTitle,
                "Days", "N. of tweets",
                dataset,
                true, true, true);

        // Create a new plot and set the parameters
          XYPlot plot = (XYPlot) chart.getPlot();
          setParam(chart, Color.WHITE, Color.GRAY);
        // Get the various series to be ploted
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            plot.getRenderer().setSeriesStroke(i, new BasicStroke(2));
        }
        // get the range of our axix
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        //rangeAxis.setTickUnit(new NumberTickUnit(n));
        // set the axis parameters
        DateAxis axis = new DateAxis();
        axis = setAxis(axis, plot);
        // set our chart size
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(900, 900));
        setContentPane(chartPanel);

        //save our chart
        ChartUtilities.saveChartAsPNG(new File(fileName), chart, 800, 600);
    }

    private DateAxis setAxis(DateAxis axis, XYPlot plot){
        axis = (DateAxis) plot.getDomainAxis();
        axis.setLabelFont(new Font("Verdana", Font.BOLD, 14));
        axis.setTickLabelFont(new Font("Verdana", Font.PLAIN, 10));
        axis.setTickLabelsVisible(true);
        axis.setTickMarksVisible(true);
        axis.setLabelAngle(0);
        axis.setVerticalTickLabels(true);
        return axis;
    }

    private XYPlot setParam(JFreeChart chart, Color backgroundColor, Color gridsColor){
        //Changes background color
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(255, 255, 255));
        //plot.getRenderer().setSeriesPaint(0, Color.BLUE);
        //plot.getRenderer().setSeriesPaint(1, Color.RED);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));
        return plot;
    }
}