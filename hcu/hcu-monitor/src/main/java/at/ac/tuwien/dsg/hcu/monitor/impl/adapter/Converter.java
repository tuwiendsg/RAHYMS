package at.ac.tuwien.dsg.hcu.monitor.impl.adapter;

public class Converter {

    public static double perMil(String value) {
        double doubleValue = Double.parseDouble(value);
        return doubleValue / 1000.0;
    }
    
    public static double idleToUtil(String value) {
        double doubleValue = Double.parseDouble(value);
        return 100.0 - doubleValue;
    }
}
