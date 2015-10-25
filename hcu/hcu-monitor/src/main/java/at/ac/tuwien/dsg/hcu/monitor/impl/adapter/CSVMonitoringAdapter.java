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

import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringAdapterInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringAgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.util.Util;

public class CSVMonitoringAdapter implements MonitoringAdapterInterface {

    public static final String CFG_SET_CSV_FILE = "csv_file";
    public static final String CFG_SET_CSV_TIME_CFG = "csv_time_cfg";    
    public static final String CFG_TICK = "tick";
    
    private CSVParser parser = null;
    private MonitoringAgentInterface agent = null;
    
    private String filePath;
    private String timeCol;
    private long timeOffset = 0;
    private String timeFormat;
    private String timeExpression;
    private String timeFunction;
    private List<String> dataCol = new ArrayList<String>();
    private Map<String, Object> topics = new HashMap<String, Object>();
    private boolean shouldInit = false;
    
    @Override
    public void start() {
        if (shouldInit) {
            init();
        }
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
                        agent.getProducer().publish(topicData);
                    }
                }
            } catch (NoSuchElementException e) {
                for (String topic: topics.keySet()) {
                    Data topicData = new Data();
                    topicData.setName(topic);
                    topicData.setMetaData("eof", true);
                    dataList.add(topicData);
                    if (shouldPublish) {
                        agent.getProducer().publish(topicData);
                    }
                }
            }
        }
        return dataList;
    }
    
    private List<Data> tick() {
        return tick(true);
    }
    
    private Double getNormalizedValue(String svalue, Double offset, String expression, String function, String format) throws ParseException, ScriptException {
        Double result = null;
        if (expression!=null) {
            result = (double)Util.eval(expression, svalue);
        }
        else if (function!=null) {
            Method method = getMethod(function);
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
            result = (double)date.getTime();
        } else {
            result = Double.parseDouble(svalue);
        }
        if (offset!=null) {
            result += offset;
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Data getTopicData(Map<String, Object> recordData, Map<String, Object> config) {
        HashMap<String, Object> metaData = (HashMap<String, Object>) recordData.get("metadata");
        Data data = new Data();
        Map<String, Object> values = (Map<String, Object>) recordData.get("values");
        String svalue = (String) values.get(config.get("csv_col"));
        metaData.put("originalData", svalue);
        try {
            Double value = getNormalizedValue(
                    svalue,
                    (Double)config.get("offset"),
                    (String)config.get("convert_expression"),
                    (String)config.get("convert_function"),
                    (String)config.get("format"));
            data.setValue(value);
        } catch (ParseException | ScriptException e) {
            data.setValue(null);
        }
        data.setMetaData(metaData);
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
            time = getNormalizedValue(stime, (double) timeOffset, timeExpression, timeFunction, timeFormat);
            // read values
            for (String col: cols) {
                values.put(col, record.get(col));
            }
        } catch (NumberFormatException e) {
            metaData.put("originalTimeData", record.get(timeCol));
        } catch (ParseException e) {
            metaData.put("originalTimeData", record.get(timeCol));
        } catch (ScriptException e) {
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
    public void adjust(HashMap<String, Object> config) {
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
                    timeOffset = (Integer)timeCfg.get("offset");
                    timeFormat = (String)timeCfg.get("format");
                    timeExpression = (String)timeCfg.get("convert_expression");
                    timeFunction = (String)timeCfg.get("convert_function");
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
        return tick(false);
    }

    @Override
    public void setMonitoringAgent(MonitoringAgentInterface agent) {
        this.agent = agent;
    }
    
    private Method getMethod(String function) {

        // get class name and method name
        int dotPos = function.lastIndexOf(".");
        String className = null;
        String methodName = null;
        if (dotPos==-1) {
            className = this.getClass().getCanonicalName();
            methodName = function;
        } else {
            className = function.substring(0, dotPos);
            methodName = function.substring(dotPos+1);
        }

        Method method = null;
        try {
            Class<?> clazz = Class.forName(className);
            method = clazz.getMethod(methodName, String.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return method;
    }

    @Override
    public void addTopic(String topicName, HashMap<String, Object> config) {
        topics.put(topicName, config);
        String col = (String) config.get("csv_col");
        if (!dataCol.contains(col)) {
            dataCol.add(col);
        }
        
    }
}
