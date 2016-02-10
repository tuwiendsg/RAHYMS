package at.ac.tuwien.dsg.hcu.rest.resource.simulation;

import java.util.List;

/**
 * Created by karaoglan on 07/12/15.
 */
public class SimulationData {
    private List<TaskData> tasks;
    private List<UnitData> units;
    private String xAxis;
    private String yAxis;

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

    public List<TaskData> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskData> tasks) {
        this.tasks = tasks;
    }

    public List<UnitData> getUnits() {
        return units;
    }

    public void setUnits(List<UnitData> units) {
        this.units = units;
    }

    public static class TaskData {
        private String name;
        private Task task;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Task getTask() {
            return task;
        }

        public void setTask(Task task) {
            this.task = task;
        }
    }

    public static class UnitData {
        private String name;
        private Unit unit;

        public Unit getUnit() {
            return unit;
        }

        public void setUnit(Unit unit) {
            this.unit = unit;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
