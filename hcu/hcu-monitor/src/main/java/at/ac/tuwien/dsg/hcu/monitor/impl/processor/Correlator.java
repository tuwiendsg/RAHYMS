package at.ac.tuwien.dsg.hcu.monitor.impl.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.espertech.esper.client.EPServiceProvider;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringConsumerInterface;

public class Correlator extends BaseProcessor {

    @Override
    public void initiate(EPServiceProvider epService, MonitoringConsumerInterface consumer, String topic, HashMap<String, Object> args) {

        super.initiate(epService, consumer, topic, args);
        
        // create window
        String createWindow = "CREATE WINDOW CorrelatorWindow.win:keepall() AS "
                + "SELECT name, value, metaData FROM Data";
        epService.getEPAdministrator().createEPL(createWindow);
        
        // listen to sources
        List<Map<String, Object>> sources = (List<Map<String, Object>>) args.get("sources");
        for (Map<String, Object> source: sources) {
            String from = (String) source.get("from");
            String value = (String) source.get("value");
            String epl = "INSERT INTO CorrelatorWindow " +
                            "SELECT '" + topic + "' as name, " + value + " as value, last(metaData) as metaData " +
                            "FROM " + from;
            epService.getEPAdministrator().createEPL(epl);
        }
        
        // listen to CorrelatorWindow and publish
        String expression = "SELECT name, value, metaData FROM CorrelatorWindow ";
        this.addListener(expression, new BaseListener());
        
    }

}
