package at.ac.tuwien.dsg.hcu.simulation.util;

import at.ac.tuwien.dsg.hcu.util.MongoDatabase;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class SimulationGraphDrawer {

    private String xAxis;
    private String yAxis;
    private String simulationId;

    private BufferedImage image;

    /**
     * A constant for the number of items in the sample dataset.
     */
    private static int COUNT;

    public BufferedImage getImage() {
        return image;
    }

    public SimulationGraphDrawer(String xAxis, String yAxis, String simulationId) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.simulationId = simulationId;
        image = createDemoPanel(xAxis, yAxis);

    }

    public static SimulationGraphDrawer startDrawingGraph(String xAxis, String yAxis, String simulationId) {
        final SimulationGraphDrawer graph = new SimulationGraphDrawer(xAxis, yAxis, simulationId);
        //graph.pack();
        //RefineryUtilities.centerFrameOnScreen(graph);
        return graph;
    }

    public BufferedImage createDemoPanel(String xAxis, String yAxis) {

        JFreeChart jfreechart = null;

        if(xAxis.equals("clock")) {


            jfreechart = ChartFactory.createScatterPlot(
                    "Simulation Scatter chart",                  // chart title
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

            jfreechart = ChartFactory.createXYLineChart("Simulation Line chart",
                    xAxis, yAxis, populateData(), PlotOrientation.VERTICAL, true, true, false);
            jfreechart.getXYPlot().getRenderer().setBasePaint(Color.BLUE);
        }

        return jfreechart.createBufferedImage(1080, 640);
    }


    private XYDataset populateData() {

        //todo brk norm cost ta misal digerlerinde de olabilir bak olay su: x icin clock secilince clock 0.9984 ile 1 arasindak
        //kücük degerlerde gösteriyor, id secinde ama sol tarafta tam degerleri gösterdigidnene 1 e tekamül ediyor.

        List<Double> valuesOfColumnForX = MongoDatabase.getColumnForGraph(xAxis, simulationId);
        List<Double> valuesOfColumnForY = MongoDatabase.getColumnForGraph(yAxis, simulationId);

        COUNT = valuesOfColumnForX.size();

        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        XYSeries series = new XYSeries("GRAPH");

        for (int i = 0; i < COUNT; i++) {

            Double x = valuesOfColumnForX.get(i);
            Double y = valuesOfColumnForY.get(i);

            series.add(x, y);
        }

        xySeriesCollection.addSeries(series);
        return xySeriesCollection;


    }

}
