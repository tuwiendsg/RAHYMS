package at.ac.tuwien.dsg.hcu.composer;

/**
 * Created by karaoglan on 31/03/16.
 */

import at.ac.tuwien.dsg.hcu.composer.helper.MongoDatabase;
import at.ac.tuwien.dsg.hcu.composer.model.Solution;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;

import java.util.Arrays;

/**
 * Created by karaoglan on 19/02/16.
 */
public class DBTracer extends ComposerTracer {

    public static final String ALGORITHM_NAME = "algo_name,";
    public static final String SIMULATION_ID = "simulation_id,";

    private String[] fields;
    private DBObject simulationInformation;

    public DBTracer() {
        super();
        fields = getTraceHeader().split(",");
    }

    public String getTraceHeader() {
        return SIMULATION_ID + ALGORITHM_NAME + Composer.TRACE_HEADER + super.getTraceHeader();
    }

    public String trace(Solution solution) {
        return solution.trace();
    }

    public void traceln(String line) {
        if(line == null || line.isEmpty())
            return;

        String[] values = line.split(",");
        String[] copyValue = null;

        if(fields.length != values.length) {
            //todo brk no solution
            copyValue = Arrays.copyOf(values, fields.length);
            for(int i = values.length; i<fields.length;i++) {
                copyValue[i] = "";
            }
        }

        if(copyValue != null)
            values = copyValue;

        simulationInformation = new BasicDBObject("_id", new ObjectId());

        int index = 0;
        for (String f : values) {
            simulationInformation.put(fields[index], !f.trim().isEmpty() ? f : /*todo brk  hangisi olmali "null" */null);
            index++;
        }

        MongoDatabase.getSimulationGraphInformationCollection().insert(simulationInformation);
    }
}
