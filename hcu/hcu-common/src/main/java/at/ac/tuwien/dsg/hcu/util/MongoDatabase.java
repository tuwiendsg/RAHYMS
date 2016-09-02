package at.ac.tuwien.dsg.hcu.util;

import com.mongodb.*;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class MongoDatabase {

    private static final String PROP_FILE = "config/simulation.properties";
    private static DB database;

    private static final double MAX_NUMBER = 15333333;
    private static final double MIN_NUMBER = -15333333;

    public static DB getDatabase() {

        if (database == null) {
            MongoClientURI uri = new MongoClientURI(Util.getProperty(PROP_FILE, "SIMULATION_MLAB_URI"));
            MongoClient client = new MongoClient(uri);
            database = client.getDB(uri.getDatabase());
        }

        return MongoDatabase.database;

    }

    public static DBCollection getUnitCollection() {
        return getDatabase().getCollection(
                Util.getProperty(PROP_FILE, "SIMULATION_UNIT"));
    }

    public static DBCollection getTaskCollection() {
        return getDatabase().getCollection(
                Util.getProperty(PROP_FILE, "SIMULATION_TASK"));
    }


    public static DBCollection getSimulationGraphInformationCollection() {
        return getDatabase().getCollection(
                Util.getProperty(PROP_FILE, "SIMULATION_GRAPH_INFORMATION")
        );
    }

    public static DBCollection getSimulationCollection() {
        return getDatabase().getCollection(
                Util.getProperty(PROP_FILE, "SIMULATION")
        );
    }

    public static void putSimulationObject(ObjectId simulationObjectId,
                                           String simulationTimeCreated,
                                           String simulationName,
                                           String simulationDescription,
                                           String simulationFilePath) {

        DBObject simulationObject = new BasicDBObject("_id", simulationObjectId);
        simulationObject.put("timeCreated", simulationTimeCreated);
        simulationObject.put("timeFinished", "Running");
        simulationObject.put("name", simulationName);
        simulationObject.put("description", simulationDescription);
        simulationObject.put("filePath", simulationFilePath);

        getSimulationCollection().insert(simulationObject);
    }

    public static void updateSimulationObject(ObjectId simulationId, String simulationFilePath, String timeFinished) {
        BasicDBObject searchQuery = new BasicDBObject("_id", simulationId);

        BasicDBObject newDocument = new BasicDBObject();
        if (simulationFilePath != null) {
            newDocument.append("filePath", simulationFilePath);
        }

        if (timeFinished != null) {
            newDocument.append("timeFinished", timeFinished);
        }


        BasicDBObject setQuery = new BasicDBObject();
        setQuery.append("$set", newDocument);

        getSimulationCollection().update(searchQuery, setQuery);
    }


    public static List<Double> getColumnForGraph(String columnName, String simulationId) {
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("simulation_id", simulationId);

        DBCursor cursor = getSimulationGraphInformationCollection().find(whereQuery);

        List<Double> listOfColumnElemOfRows = new ArrayList<>();
        int i = 0;
        while (cursor.hasNext()) {

            Object value = cursor.next().get(columnName);
            try {

                Double doubleValue = new Double(value.toString());
                listOfColumnElemOfRows.add(round(doubleValue, 5));

            } catch (NumberFormatException e) {
                listOfColumnElemOfRows.add(new Double(0));
            }

            i++;

        }

        return listOfColumnElemOfRows;
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        if (value == 0) return value;

        DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(8);
        df.setMaximumIntegerDigits(9);

        if(value >= Double.MAX_VALUE) {
            value = new Double(MAX_NUMBER);
        }

        if(value <= Double.MIN_VALUE) {
            value = new Double(MIN_NUMBER);
        }

        return value;
    }

    public static void toBringDefaultSimulationUnitsAndTasks() throws IOException {
        getUnitCollection().drop();
        getTaskCollection().drop();

        DefaultMongoData.saveDefaultUnitsAndTasks();
    }
}
