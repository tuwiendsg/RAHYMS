package at.ac.tuwien.dsg.hcu.monitor.impl.processor;

import java.util.List;
import java.util.Map;

import com.espertech.esper.client.EPServiceProvider;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.ConsumerInterface;

public class Correlator extends BaseProcessor {

    @SuppressWarnings("unchecked")
    @Override
    public void initiate(EPServiceProvider epService, ConsumerInterface consumer, String topic, Map<String, Object> args) {

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
