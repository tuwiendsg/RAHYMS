package at.ac.tuwien.dsg.hcu.rest.common;

import at.ac.tuwien.dsg.hcu.composer.helper.MongoDatabase;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Created by karaoglan on 31/01/16.
 */
public class SimulationGraph {

    private String xAxis;
    private String yAxis;
    private String dateOfSimulation;

    private BufferedImage image;

    /**
     * A constant for the number of items in the sample dataset.
     */
    private static int COUNT;

    /**
     * The data.
     */
    private static float[][] data;

    public BufferedImage getImage() {
        return image;
    }

    public SimulationGraph(final String title, String xAxis, String yAxis, String dateOfSimulation) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.dateOfSimulation = dateOfSimulation;
        image = createDemoPanel(xAxis, yAxis);

    }

    public static SimulationGraph startDrawingGraph(String xAxis, String yAxis, String dateOfSimulation) {
        final SimulationGraph graph = new SimulationGraph("Scatter Plot Simulation", xAxis, yAxis, dateOfSimulation);
        //graph.pack();
        //RefineryUtilities.centerFrameOnScreen(graph);
        return graph;
    }

    public BufferedImage/*was JPanel*/ createDemoPanel(String xAxis, String yAxis) {

        JFreeChart jfreechart = null;

        if(xAxis.equals("clock")) {
         /*   jfreechart = ChartFactory.createScatterPlot("Scatter Plot Demo",
                    xAxis, yAxis, populateData(), PlotOrientation.VERTICAL, true, true, false);
            Shape cross = ShapeUtilities.createDiagonalCross(3, 1);
            XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
            xyPlot.setDomainCrosshairVisible(true);
            xyPlot.setRangeCrosshairVisible(true);
            XYItemRenderer renderer = xyPlot.getRenderer();
            renderer.setSeriesShape(0, cross);
            renderer.setSeriesPaint(0, Color.red);

*/

            jfreechart = ChartFactory.createScatterPlot(
                    "Title",                  // chart title
                    xAxis,                      // x axis label
                    yAxis,                      // y axis label
                    populateData(),                  // data
                    PlotOrientation.VERTICAL,
                    true,                     // include legend
                    true,                     // tooltips
                    false                     // urls
            );
            XYPlot plot = (XYPlot) jfreechart.getPlot();
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            renderer.setSeriesLinesVisible(0, true);
            plot.setRenderer(renderer);


        }else {
            //task_id

            jfreechart = ChartFactory.createXYLineChart("Line chart Demo",
                    xAxis, yAxis, populateData(), PlotOrientation.VERTICAL, true, true, false);
            jfreechart.getXYPlot().getRenderer().setBasePaint(Color.BLUE); //todo brk renk degistir
        }

        //todo brk grafik büyüklügü neye göre ayarlanmasi lazim hard code?
        //jfreechart.createBufferedImage(640, 720);
        return jfreechart.createBufferedImage(1280, 640);//new ChartPanel(jfreechart);
    }


    /**
     * Populates the data array with random values.
     */
    private XYDataset populateData() {
        //todo brk ask clock tam kesin degerler olarak lazim mi su an kesiyor direk integer degerini yerlesitiriyro
        //todo brk grafige.


        //todo brk buraya ayar gerekebilir float belki null? belki baska degeer
        List<Float> valuesOfColumnForX = MongoDatabase.getColumnForGraph(xAxis, dateOfSimulation);
        List<Float> valuesOfColumnForY = MongoDatabase.getColumnForGraph(yAxis, dateOfSimulation);

        COUNT = valuesOfColumnForX.size();
        data = new float[2][COUNT];

        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        XYSeries series = new XYSeries("GRAPH");

        for (int i = 0; i < COUNT; i++) {
            float x = valuesOfColumnForX.get(i);
            float y = valuesOfColumnForY.get(i);
            //System.out.println("float x and float y " + x + " --- " + y);

            series.add(x, y);
        }
        xySeriesCollection.addSeries(series);
        return xySeriesCollection;


    }

}
