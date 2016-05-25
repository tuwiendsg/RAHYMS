package at.ac.tuwien.dsg.hcu.monitor.interfaces;

public interface StatisticInterface {
    public Object getProperty(String name);
    public void increaseProperty(String name);

}
