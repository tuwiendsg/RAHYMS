package at.ac.tuwien.dsg.hcu.monitor.impl.adapter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;

import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringAdapterInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringAgentInterface;
import at.ac.tuwien.dsg.hcu.monitor.interfaces.MonitoringProducerInterface;
import at.ac.tuwien.dsg.hcu.monitor.model.Data;
import at.ac.tuwien.dsg.hcu.util.Util;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElementMonitoringSnapshot;

public class MELAMonitoringAdapter implements MonitoringAdapterInterface {

    protected MonitoringProducerInterface producer;

    //private static final String REST_API_URL = Configuration.getMonitoringServiceURL();
    private static final String REST_API_URL = "http://128.130.172.215:8180/MELA/REST_WS/";
    private static final String SERVICE_ID = "MachineTaskService"; 
    private static final int MONITORING_DATA_REFRESH_INTERVAL = 10; //in seconds
    private MonitoredElementMonitoringSnapshot latestMonitoringData;
    private AtomicBoolean monitoringDataUsed;
    private boolean monitorRunning = false;

    {
        latestMonitoringData = new MonitoredElementMonitoringSnapshot();
        monitoringDataUsed = new AtomicBoolean(false);
    }

    {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (monitorRunning) {
                    refreshMonitoringData();
                }
            }
        };

        Timer monitoringDataRefreshTimer = new Timer();
        monitoringDataRefreshTimer.schedule(task, 0, MONITORING_DATA_REFRESH_INTERVAL * 1000);
    }

    public void refreshMonitoringData() {
        URL url = null;
        HttpURLConnection connection = null;
        try {
            url = new URL(REST_API_URL + "/" + SERVICE_ID + "/monitoringdata/xml");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/xml");
            connection.setRequestProperty("Accept", "application/xml");

            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    Util.log().log(Level.SEVERE, line);
                }
            }

            InputStream inputStream = connection.getInputStream();
            JAXBContext jAXBContext = JAXBContext.newInstance(MonitoredElementMonitoringSnapshot.class);
            MonitoredElementMonitoringSnapshot retrievedData = (MonitoredElementMonitoringSnapshot) jAXBContext.createUnmarshaller().unmarshal(inputStream);

            if (retrievedData != null) {
                //P
                getLatestMonitoringDataLock();
                latestMonitoringData = retrievedData;
                //V
                releaseLatestMonitoringDataLock();
            }

        } catch (Exception e) {
            Util.log().log(Level.SEVERE, e.getMessage(), e);
            //Util.log().log(Level.WARNING, "Trying to connect to MELA - failing ... . Retrying later");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void getLatestMonitoringDataLock() {
        while (monitoringDataUsed.get()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Util.log().log(Level.SEVERE, null, ex);
            }
        };

        monitoringDataUsed.set(true);
    }

    private void releaseLatestMonitoringDataLock() {
        monitoringDataUsed.set(false);
    }

    @Override
    public void start() {
        monitorRunning = true;
    }

    @Override
    public void stop() {
        monitorRunning = false;
    }

    @Override
    public void adjust(HashMap<String, Object> config) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setMonitoringAgent(MonitoringAgentInterface agent) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Data getData() {
        // TODO Auto-generated method stub
        return null;
    }
}
