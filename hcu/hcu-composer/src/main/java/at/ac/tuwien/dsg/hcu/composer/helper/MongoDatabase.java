package at.ac.tuwien.dsg.hcu.composer.helper;

import com.mongodb.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by karaoglan on 31/01/16.
 */
public class MongoDatabase {

    public static final String SIMULATION_TASK = "simulation-task";
    public static final String SIMULATION_UNIT = "simulation-unit";
    public static final String SIMULATION_GRAPH_INFORMATION = "simulation-graph-information";
    public static final String SIMULATION = "simulation";

    private static DB database;

    private MongoDatabase() {
    }

    public static DB getDatabase() {

        //todo brk bilgileri not al ve teze ekle bunu properties dosyasina at üsttekileri sil
        //todo brk ilk önce mongod service in calismasi lazim bunun icin, 3.0.12 versionundaki ilk basta bir defa calistirmam yetti.

        //todo brk ilk önce ./mongod nin calismasi lazim sonra da ./mongo dan pass ve userna girilecek cözüm bul kendi kendine halletsin.

        if (database == null) {
            MongoClientURI uri = new MongoClientURI("mongodb://rahyms:rahyms@ds025802.mlab.com:25802/simulation");
            MongoClient client = new MongoClient(uri);
            database = client.getDB(uri.getDatabase());
        }


        return database;
    }

    public static DBCollection getUnitCollection() {
        return getDatabase().getCollection(SIMULATION_UNIT);
    }

    public static DBCollection getTaskCollection() {
        return getDatabase().getCollection(SIMULATION_TASK);
    }


    public static DBCollection getSimulationGraphInformationCollection() {
        return getDatabase().getCollection(SIMULATION_GRAPH_INFORMATION);
    }

    public static DBCollection getSimulationCollection() {
        return getDatabase().getCollection(SIMULATION);
    }

    //todo brk remove not used
    public static void removeAllSimulationGraphInformationCollection() {
        getDatabase().getCollection(SIMULATION_GRAPH_INFORMATION).drop();
    }

    public static List<Float> getColumnForGraph(String columnName, String dateOfSimulation) {
        BasicDBObject select = new BasicDBObject();
        select.put(columnName, 1);
        select.put("date", dateOfSimulation);

        DBCursor cursor = getSimulationGraphInformationCollection().find(new BasicDBObject(), select);

        List<Float> listOfColumnElemOfRows = new ArrayList<>();

        int i = 0;
        while (cursor.hasNext()) {

            Object value = cursor.next().get(columnName);

            listOfColumnElemOfRows.add(new Float(value.toString()));
            i++;

        }

        return listOfColumnElemOfRows;
    }

}
