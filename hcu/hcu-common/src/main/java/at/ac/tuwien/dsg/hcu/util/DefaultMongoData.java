package at.ac.tuwien.dsg.hcu.util;

import at.ac.tuwien.dsg.hcu.util.MongoDatabase;
import at.ac.tuwien.dsg.hcu.util.Util;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DefaultMongoData {

    private static final String PROP_FILE = "config/simulation-web-default.properties";

    public static void saveDefaultUnitsAndTasks() throws IOException {

        if(MongoDatabase.getTaskCollection().count() == 0) {
            saveToDB(Util.getProperty(PROP_FILE, "HUMAN_NAME"),
                    readFile(Util.getProperty(PROP_FILE, "HUMAN_PATH")),
                    false);
            saveToDB(Util.getProperty(PROP_FILE, "MACHINE_NAME"),
                    readFile(Util.getProperty(PROP_FILE, "MACHINE_PATH")),
                    false);

        }

        if(MongoDatabase.getUnitCollection().count() == 0) {
            saveToDB(Util.getProperty(PROP_FILE, "CITIZEN_NAME"),
                    readFile(Util.getProperty(PROP_FILE, "CITIZEN_PATH")),
                    true);
            saveToDB(Util.getProperty(PROP_FILE, "SENSOR_NAME"),
                    readFile(Util.getProperty(PROP_FILE, "SENSOR_PATH")),
                    true);
            saveToDB(Util.getProperty(PROP_FILE, "SURVEYOR_NAME"),
                    readFile(Util.getProperty(PROP_FILE, "SURVEYOR_PATH")),
                    true);
        }

    }

    private static void saveToDB(String name, String data, Boolean isUnit) {
        ObjectId objectId = new ObjectId();
        DBObject object = new BasicDBObject("_id", objectId);

        String generatorType = isUnit ? "unit" : "task";

        object.put("name", name);
        object.put(generatorType, data);

        try {
            String generatorTableName = "simulation-" + generatorType;
            DBCollection collection = MongoDatabase.getDatabase().getCollection(generatorTableName);
            collection.insert(object);
        } catch (Exception e) {
            System.out.println("#### - PLEASE CHECK YOUR MONGOD DRIVER 3.0.12 VERSION NEEDED AND MUST BE RUNNING!!!");
        }
    }

    public static String readFile(String path)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

}
