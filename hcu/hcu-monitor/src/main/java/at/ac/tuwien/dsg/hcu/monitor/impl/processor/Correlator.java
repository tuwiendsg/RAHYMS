package at.ac.tuwien.dsg.hcu.monitor.impl.processor;

import java.util.List;
import java.util.Map;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.ConsumerInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.monitor.model.MetaData;

public class Correlator extends BaseProcessor {

    @SuppressWarnings("unchecked")
    @Override
    public void initiate(EPServiceProvider epService, ConsumerInterface consumer, String topic, Map<String, Object> args) {

        super.initiate(epService, consumer, topic, args);
        
        // create window
        String createWindow = "CREATE WINDOW CorrelatorWindow.win:keepall() AS "
                + "SELECT name, value, metaData, 1 as replicate FROM Data";
        epService.getEPAdministrator().createEPL(createWindow);
        
        // listen to sources
        List<Map<String, Object>> sources = (List<Map<String, Object>>) args.get("sources");
        for (Map<String, Object> source: sources) {
            String from = (String) source.get("from");
            String value = (String) source.get("value");
            String replicate = (String) source.get("replicate");
            String epl = "INSERT INTO CorrelatorWindow " +
                            "SELECT '" + topic + "' as name, " + value + " as value, last(metaData) as metaData ";
            if (replicate != null && !replicate.trim().equals("")) {
                epl += ", " + replicate + " AS replicate ";
            } else {
                epl += ", 1 AS replicate ";
            }
            epl += "FROM " + from;
            epService.getEPAdministrator().createEPL(epl);
        }
        
        // listen to CorrelatorWindow and publish
        String expression = "SELECT name, value, replicate, metaData FROM CorrelatorWindow ";
        this.addListener(expression, new CorrrelatorListener());
        
    }

    protected class CorrrelatorListener implements UpdateListener {
        @SuppressWarnings({ "rawtypes" })
        @Override
        public void update(EventBean[] newEvents, EventBean[] oldEvents) {
            int i = 0;
            for (EventBean event: newEvents) {
                //Util.log().warning("BaseProcessor [" + (i++) + "]: " +  event.getUnderlying());
                Double value = (Double) ((Map)event.getUnderlying()).get("value");
                Integer replicate = (Integer) ((Map)event.getUnderlying()).get("replicate");
                if (replicate==null) {
                    replicate = 1;
                }
                MetaData metaData = (MetaData) ((Map)event.getUnderlying()).get("metaData");
                if (topicName.equals("correlated_utilization")) {
                    //Util.log().info(event.getUnderlying().toString());
                }
                
                String type = (String) metaData.get("type");
                for (int j=0; j<replicate; j++) {
                    Data data = new Data();
                    data.setName(topicName);
                    data.setValue(value);
                    MetaData newMetaData = null;
                    try {
                        newMetaData = (MetaData) metaData.clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    // TODO: id population should not be hardcoded
                    if (type!=null && newMetaData.get("id")==null) {
                        newMetaData.set("id", type + "_" + (j+1));
                    }
                    data.setMetaData(newMetaData);
                    
                    consumer.getAgent().publish(data);
                }
            }
        }
        
    }
}
