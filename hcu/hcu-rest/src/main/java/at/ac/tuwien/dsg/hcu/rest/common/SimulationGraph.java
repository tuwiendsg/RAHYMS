package at.ac.tuwien.dsg.hcu.rest.common;

import com.mongodb.DBObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.ShapeUtilities;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by karaoglan on 31/01/16.
 */
public class SimulationGraph extends ApplicationFrame {

    private String xAxis;
    private String yAxis;

    private ChartPanel panel;

    /**
     * A constant for the number of items in the sample dataset.
     */
    private static int COUNT;

    /**
     * The data.
     */
    private static float[][] data;

    public SimulationGraph(final String title, String xAxis, String yAxis) {
        super(title);
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        JPanel jpanel = createDemoPanel(xAxis, yAxis);
        jpanel.setPreferredSize(new Dimension(640, 480));
        setContentPane(jpanel);

        /*populateData();
        final NumberAxis domainAxis = new NumberAxis(xAxis);
        domainAxis.setAutoRangeIncludesZero(false);
        final NumberAxis rangeAxis = new NumberAxis(yAxis);
        rangeAxis.setAutoRangeIncludesZero(false);
        final FastScatterPlot plot = new FastScatterPlot(this.data, domainAxis, rangeAxis);
        final JFreeChart chart = new JFreeChart("Fast Scatter Plot", plot);
//        chart.setLegend(null);

        // force aliasing of the rendered content..
        chart.getRenderingHints().put
                (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        panel = new ChartPanel(chart, true);
        panel.setPreferredSize(new java.awt.Dimension(500, 270));
        panel.setOpaque(true);
        panel.setVisible(true);
        //      panel.setHorizontalZoom(true);
        //    panel.setVerticalZoom(true);
        panel.setMinimumDrawHeight(10);
        panel.setMaximumDrawHeight(2000);
        panel.setMinimumDrawWidth(20);
        panel.setMaximumDrawWidth(2000);

        setContentPane(panel);*/

    }

    public JPanel createDemoPanel(String xAxis, String yAxis) {

        JFreeChart jfreechart = ChartFactory.createScatterPlot("Scatter Plot Demo",
                xAxis, yAxis, populateData(), PlotOrientation.VERTICAL, true, true, false);
        Shape cross = ShapeUtilities.createDiagonalCross(3, 1);

        XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
        XYItemRenderer renderer = xyPlot.getRenderer();
        renderer.setSeriesShape(0, cross);
        renderer.setSeriesPaint(0, Color.red);
        return new ChartPanel(jfreechart);
    }


    /**
     * Populates the data array with random values.
     */
    private XYDataset populateData() {

        List<Float> valuesOfColumnForX = MongoDatabase.getColumnForGraph(xAxis);
        List<Float> valuesOfColumnForY = MongoDatabase.getColumnForGraph(yAxis);

        COUNT = valuesOfColumnForX.size();
        data = new float[2][COUNT];
/*
        for (int i = 0; i < data[0].length; i++) {
            final float x =
            data[0][i] = x;
            data[1][i] =
        }*/

        //asda

        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        XYSeries series = new XYSeries("GRAPH");

        for (int i = 0; i < COUNT; i++) {
            float x = valuesOfColumnForX.get(i);
            float y = valuesOfColumnForY.get(i);

            System.out.println("float x and float y " + x + " --- " + y);

            series.add(x, y);
        }
        xySeriesCollection.addSeries(series);
        return xySeriesCollection;


    }

    public ChartPanel getPanel() {
        return panel;
    }

    /**
     * Starting point for the demonstration application.
     *
     * @param args ignored.
     */
    public static void main(final String[] args) {

        final SimulationGraph demo = new SimulationGraph("Scatter Plot Simulation", args[0], args[1]);
        demo.pack();
        RefineryUtilities.centerFrameOnScreen(demo);
        demo.setVisible(true);
        //saveImage(demo.getPanel());
    }

    private static void saveImage(ChartPanel panel) {
        Dimension size = panel.getSize();
        BufferedImage image = new BufferedImage(
                size.width, size.height
                , BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();
        panel.paint(g2);
        try {
            ImageIO.write(image, "png", new File("/Users/karaoglan/Desktop/snapshot.png"));
            System.out.println("Panel saved as Image.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
