package at.ac.tuwien.dsg.hcu.rest.common;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by karaoglan on 31/01/16.
 */
public class MongoDatabase {

    private static final String MONGODB_DATABASE = "HCU-SIMULATION";

    public static DB database;

    static {
        MongoClient client = null;
        try {
            client = new MongoClient("localhost", 12345);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        database = client.getDB(MONGODB_DATABASE);
    }

    public static List<Float> getColumnForGraph(String columnName){
        BasicDBObject select = new BasicDBObject();
        select.put(columnName, 1);

        DBCursor cursor = database.getCollection("simulation-information").find(new BasicDBObject(), select);

        List<Float> listOfColumnElemOfRows = new ArrayList<>();

        int i = 0;
        while(cursor.hasNext()) {
            //todo check
            Object value = cursor.next().get(columnName);
            Double doubleValue = null;
            Long longValue = null;
            /*if(value instanceof Long) {
                longValue = (long)value;
            }else if(value instanceof Double) {
                doubleValue = (double)value;
            }else {
                //string
            }*/

            listOfColumnElemOfRows.add(new Float(value.toString()));
            System.out.println(i + ". element of column row " + columnName + " " + listOfColumnElemOfRows.get(i));
            i++;

        }

        return listOfColumnElemOfRows;
    }

}
