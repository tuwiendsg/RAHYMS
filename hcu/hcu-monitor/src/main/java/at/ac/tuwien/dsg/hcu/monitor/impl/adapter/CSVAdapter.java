package at.ac.tuwien.dsg.hcu.monitor.impl.adapter;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.script.ScriptException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.AdapterInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.AgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.Wakeable;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.Waker;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.util.Util;

public class CSVAdapter extends BaseAdapter implements AdapterInterface, Wakeable {

    public static final String CFG_SET_CSV_FILE = "csv_file";
    public static final String CFG_SET_CSV_TIME_CFG = "csv_time_cfg";    
    public static final String CFG_TICK = "tick";
    
    private CSVParser parser = null;
    
    private String filePath;
    private String timeCol;
    private long timeOffset = 0;
    private double timeScale = 1.0;
    private String timeFormat;
    private String timeExpression;
    private String timeFunction;
    private List<String> dataCol = new ArrayList<String>();
    private boolean shouldInit = false;
    private List<Data> currentData;
    
    @Override
    public void start() {
        if (shouldInit) {
            init();
        }
        scheduleNextData();
    }

    @Override
    public void stop() {
        try {
            parser.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Data> tick(boolean shouldPublish) {
        // Note: All data in the dataList must have the same timestamp
        List<Data> dataList = new ArrayList<Data>();
        Map<String, Object> recordData = new HashMap<String, Object>();
        if (parser!=null) {
            try {
                CSVRecord record = parser.iterator().next();
                recordData = getRecordData(record, dataCol);
                for (String topic: topics.keySet()) {
                    Map<String, Object> topicCfg = (Map<String, Object>) topics.get(topic);
                    Data topicData = getTopicData(recordData, topicCfg);
                    topicData.setName(topic);
                    dataList.add(topicData);
                    if (shouldPublish) {
                        agent.publish(topicData);
                    }
                }
            } catch (NoSuchElementException e) {
                for (String topic: topics.keySet()) {
                    Data topicData = new Data();
                    topicData.setName(topic);
                    topicData.setMetaData("eof", true);
                    dataList.add(topicData);
                    if (shouldPublish) {
                        agent.publish(topicData);
                    }
                }
                agent.stop();
            }
        }
        return dataList;
    }
    
    private List<Data> tick() {
        return tick(true);
    }
    
    private Object getNormalizedValue(String svalue, Double offset, Double scale, String expression, String function, String format) throws ParseException, ScriptException {
        Double result = null;
        if (expression!=null) {
            result = (double)Util.eval(expression, svalue);
        }
        else if (function!=null) {
            Method method = Util.getMethod(function);
            try {
                result = (double)method.invoke(null, svalue);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        else if (format!=null) {
            // only for time-formatted data
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            Date date = dateFormat.parse(svalue);
            result = (double)date.getTime()/1000;
        } else {
            try {
                result = Double.parseDouble(svalue);
            }
            catch (NumberFormatException e) {
                // just let result as String as it is
                return svalue;
            }
        }
        if (offset!=null) {
            result += offset;
        }
        if (scale!=null) {
            result *= scale;
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Data getTopicData(Map<String, Object> recordData, Map<String, Object> config) {
        Map<String, Object> metaData = (HashMap<String, Object>) recordData.get("metadata");
        Data data = new Data();
        data.getMetaData().setAll(metaData);
        Map<String, Object> values = (Map<String, Object>) recordData.get("values");
        String svalue = (String) values.get(config.get("csv_col"));
        data.setMetaData("originalData", svalue);
        try {
            Object value = getNormalizedValue(
                    svalue,
                    (Double)config.get("offset"),
                    (Double)config.get("scale"),
                    (String)config.get("expression"),
                    (String)config.get("function"),
                    (String)config.get("format"));
            data.setValue(value);
        } catch (ParseException | ScriptException e) {
            data.setValue(null);
        }
        // retrieve additional metadata
        List<Map<String, Object>> metadataConfig = (List<Map<String, Object>>) config.get("metadata");
        if (metadataConfig!=null) {
            for (Map<String, Object> mcfg: metadataConfig) {
                String name = (String) mcfg.get("name");
                String col = (String) mcfg.get("csv_col");
                if (col!=null) {
                    String mvalue = (String) values.get(col);
                    if (mvalue!=null) {
                        data.setMetaData(name!=null?name:col, mvalue);
                    }
                }
            }
        }
        return data;
    }
    
    private HashMap<String, Object> getRecordData(CSVRecord record, List<String> cols) {
        Double time = 0.0;
        HashMap<String, Object> results = new HashMap<String, Object>();
        HashMap<String, Object> values = new HashMap<String, Object>();
        HashMap<String, Object> metaData = new HashMap<String, Object>();
        try {
            // get time data
            String stime = record.get(timeCol);
            time = (Double) getNormalizedValue(stime, (double) timeOffset, timeScale, timeExpression, timeFunction, timeFormat);
            // read values
            for (String col: cols) {
                values.put(col, record.get(col));
            }
        } catch (NumberFormatException e) {
            System.out.println(e);
            metaData.put("originalTimeData", record.get(timeCol));
        } catch (ParseException e) {
            System.out.println(e);
            metaData.put("originalTimeData", record.get(timeCol));
        } catch (ScriptException e) {
            System.out.println(e);
            metaData.put("originalTimeData", record.get(timeCol));
        }
        metaData.put("counter", record.getRecordNumber());
        metaData.put("originalTimeData", record.get(timeCol));
        metaData.put("time", time);
        results.put("values", values);
        results.put("metadata", metaData);
        return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void adjust(Map<String, Object> config) {
        super.adjust(config);
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            switch (key) {
                case CFG_SET_CSV_FILE:
                    filePath = (String)value;
                    shouldInit = true;
                    break;
                case CFG_SET_CSV_TIME_CFG:
                    HashMap<String, Object> timeCfg = (HashMap<String, Object>) value;
                    timeCol = (String)timeCfg.get("csv_col");
                    timeOffset = (Integer)timeCfg.getOrDefault("offset", 0);
                    timeScale = (Double)timeCfg.getOrDefault("scale", 1.0);
                    timeFormat = (String)timeCfg.get("format");
                    timeExpression = (String)timeCfg.get("expression");
                    timeFunction = (String)timeCfg.get("function");
                    break;
                case CFG_TICK:
                    tick();
                    break;
            }
        }
    }

    private void init() {
        try {
            if (filePath==null) {
                return;
            }
            FileReader file = new FileReader(filePath);
            parser = CSVFormat.DEFAULT.withHeader().parse(file);
            //parser = CSVParser.parse(csvData, Charset.forName("ISO-8859-1"), CSVFormat.RFC4180);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Data> getData() {
        return currentData;
    }

    private Double getNextWakeTime() {
        currentData = tick(false);
        Double time = (Double) currentData.get(0).getMetaData("time");
        if (time==null) {
            // an EOF
            agent.publish(currentData);
            agent.stop();
        }
        return time;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addTopic(String topicName, Map<String, Object> config) {
        super.addTopic(topicName, config);
        String col = (String) config.get("csv_col");
        if (!dataCol.contains(col)) {
            dataCol.add(col);
        }
        // retrieve additional metadata cols
        List<Map<String, Object>> metadataConfig = (List<Map<String, Object>>) config.get("metadata");
        if (metadataConfig!=null) {
            for (Map<String, Object> mcfg: metadataConfig) {
                String mcol = (String) mcfg.get("csv_col");
                if (mcol!=null) {
                    if (!dataCol.contains(mcol)) {
                        dataCol.add(mcol);
                    }
                }
            }
        }
    }

    @Override
    public void wake(int wakeId) {
        agent.publish(getData());
        scheduleNextData();
    }

    private void scheduleNextData() {
        Double wakeTime = getNextWakeTime();
        if (wakeTime!=null && agent.isRunning()) {
            waker.wakeMeAt((Wakeable)agent, wakeTime);
        }
    }
}
