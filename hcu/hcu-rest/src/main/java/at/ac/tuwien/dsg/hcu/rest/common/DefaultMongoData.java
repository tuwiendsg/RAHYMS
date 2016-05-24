package at.ac.tuwien.dsg.hcu.rest.common;

import at.ac.tuwien.dsg.hcu.composer.helper.MongoDatabase;
import at.ac.tuwien.dsg.hcu.simulation.MainSimulation;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by karaoglan on 14/04/16.
 */
public class DefaultMongoData {

    public static void saveDefaultUnitsAndTasks() throws IOException {

        //todo brk bunlari ekleme olayini sor su an db den hicbir zaman silinmiyor veriler
        //todo brk db de unit ve task da veri olmadigi zaman mi eklesin?

        saveToDB("human-sensing-task-generator", readFile(MainSimulation.FILE_TEMP + "human-sensing-task-generator.json", StandardCharsets.UTF_8),
                "task");
        saveToDB("machine-sensing-task-generator", readFile(MainSimulation.FILE_TEMP + "machine-sensing-task-generator.json", StandardCharsets.UTF_8),
                "task");

        saveToDB("citizen-generator", readFile(MainSimulation.FILE_TEMP + "citizen-generator.json", StandardCharsets.UTF_8),
                "unit");
        saveToDB("sensor-generator", readFile(MainSimulation.FILE_TEMP + "sensor-generator.json", StandardCharsets.UTF_8),
                "unit");
        saveToDB("surveyor-generator", readFile(MainSimulation.FILE_TEMP + "surveyor-generator.json", StandardCharsets.UTF_8),
                "unit");

    }

    private static void saveToDB(String name, String data, String unitOrTask) {
        ObjectId objectId = new ObjectId();
        DBObject object = new BasicDBObject("_id", objectId);

        object.put("name", name);
        object.put(unitOrTask, data);

        try {
            DBCollection collection = MongoDatabase.getDatabase().getCollection("simulation-" + unitOrTask);
            collection.insert(object);
        } catch (Exception e) {
            System.out.println("#### - PLEASE CHECK YOUR MONGOD DRIVER 3.0.12 VERSION NEEDED AND MUST BE RUNNING!!!");
        }
    }

    public static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

}
