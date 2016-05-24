package at.ac.tuwien.dsg.hcu.rest.resource.simulation;

/**
 * Created by karaoglan on 13/02/16.
 */
public class GraphData {

    private String xAxis;
    private String yAxis;
    private String simulationDate;

    public String getSimulationDate() {
        return simulationDate;
    }

    public void setSimulationDate(String simulationDate) {
        this.simulationDate = simulationDate;
    }

    public String getxAxis() {
        return xAxis;
    }

    public void setxAxis(String xAxis) {
        this.xAxis = xAxis;
    }

    public String getyAxis() {
        return yAxis;
    }

    public void setyAxis(String yAxis) {
        this.yAxis = yAxis;
    }
}
