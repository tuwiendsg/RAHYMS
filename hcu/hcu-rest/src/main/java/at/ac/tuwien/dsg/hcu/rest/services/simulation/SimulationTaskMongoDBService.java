package at.ac.tuwien.dsg.hcu.rest.services.simulation;

import at.ac.tuwien.dsg.hcu.rest.exceptions.IllegalSimulationArgumentException;
import at.ac.tuwien.dsg.hcu.rest.exceptions.NotFoundException;
import at.ac.tuwien.dsg.hcu.rest.resource.simulation.SimulationTask;
import at.ac.tuwien.dsg.hcu.util.MongoDatabase;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class SimulationTaskMongoDBService {

    public SimulationTask createSimulationTask(SimulationTask task) {
        if (task.getName() == null || task.getName().trim().isEmpty()) {
            throw new IllegalSimulationArgumentException();
        }

        if (task.getTask() == null || task.getTask().trim().isEmpty()) {
            throw new IllegalSimulationArgumentException();
        }

        ObjectId objectId = new ObjectId();

        DBObject taskObject = new BasicDBObject("_id", objectId);
        task.setId(objectId.toString());
        taskObject.put("name", task.getName().trim());
        taskObject.put("task", task.getTask());


        MongoDatabase.getTaskCollection().insert(taskObject);
        return task;
    }

    public SimulationTask getDefaultSimulationTask() {

        SimulationTask simulationTask = new SimulationTask();

        try {
            DBObject dbObject = MongoDatabase.getTaskCollection().findOne();
            if (dbObject != null) {
                simulationTask.setName((String) dbObject.get("name"));
                simulationTask.setTask((String) dbObject.get("task"));
            }

        } catch (Exception e) {
            throw new NotFoundException();
        }

        return simulationTask;
    }

    public SimulationTask getSimulationTask(String objectId) {
        if (objectId == null || objectId.trim().isEmpty()) throw new IllegalSimulationArgumentException();

        SimulationTask simulationTask = new SimulationTask();

        try {
            BasicDBObject selectWithObjectId = new BasicDBObject();
            selectWithObjectId.put("_id", new ObjectId(objectId));
            DBCursor cursor = MongoDatabase.getTaskCollection().find(selectWithObjectId);
            DBObject returnedSimulationTask = null;
            if (cursor.hasNext()) {
                returnedSimulationTask = cursor.next();
                simulationTask.setName((String) returnedSimulationTask.get("name"));
                simulationTask.setTask((String) returnedSimulationTask.get("task"));
            }
            simulationTask.setId(objectId);

        } catch (Exception e) {
            throw new NotFoundException();
        }

        return simulationTask;
    }

    public List<SimulationTask> getSimulationTask() {

        List<SimulationTask> simulationTaskList = new ArrayList<>();

        DBCursor cursor = MongoDatabase.getTaskCollection().find();
        DBObject returnedSimulationTask = null;

        while (cursor.hasNext()) {
            SimulationTask simulationTask = new SimulationTask();

            returnedSimulationTask = cursor.next();
            simulationTask.setId((returnedSimulationTask.get("_id")).toString());
            simulationTask.setName((String) returnedSimulationTask.get("name"));
            simulationTask.setTask((String) returnedSimulationTask.get("task"));
            simulationTaskList.add(simulationTask);
        }

        return simulationTaskList;
    }

    public boolean removeSimulationTask(String objectId) {
        if(objectId == null || objectId.trim().isEmpty()) {
            return false;
        }

        try {
            BasicDBObject query = new BasicDBObject();
            query.put("_id", new ObjectId(objectId));

            MongoDatabase.getTaskCollection().remove(query);
        } catch (Exception e) {
            throw new NotFoundException();
        }

        return true;

    }

    public boolean updateSimulationTask(SimulationTask simulationTask) {
        if(simulationTask.getId() == null || simulationTask.getId().trim().isEmpty()) {
            return false;
        }

        if(simulationTask.getName() == null || simulationTask.getName().trim().isEmpty())
            return false;

        try {
            BasicDBObject newDocument = new BasicDBObject();
            newDocument.append("$set", new BasicDBObject().append("name", simulationTask.getName()).
                    append("task", simulationTask.getTask())
            );

            BasicDBObject searchQuery = new BasicDBObject().append("_id", new ObjectId(simulationTask.getId()));
            MongoDatabase.getTaskCollection().update(searchQuery, newDocument);
        } catch (Exception e) {
            throw new NotFoundException();
        }

        return true;
    }
}
