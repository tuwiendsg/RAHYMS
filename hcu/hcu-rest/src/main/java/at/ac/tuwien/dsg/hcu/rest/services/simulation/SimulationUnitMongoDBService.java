package at.ac.tuwien.dsg.hcu.rest.services.simulation;

import at.ac.tuwien.dsg.hcu.rest.exceptions.IllegalSimulationArgumentException;
import at.ac.tuwien.dsg.hcu.rest.exceptions.NotFoundException;
import at.ac.tuwien.dsg.hcu.rest.resource.simulation.SimulationUnit;
import at.ac.tuwien.dsg.hcu.util.MongoDatabase;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class SimulationUnitMongoDBService {

    public SimulationUnit createSimulationUnit(SimulationUnit unit) {

        if (unit.getName() == null || unit.getName().trim().isEmpty()) {
            throw new IllegalSimulationArgumentException();
        }

        if (unit.getUnit() == null || unit.getUnit().trim().isEmpty()) {
            throw new IllegalSimulationArgumentException();
        }

        ObjectId objectId = new ObjectId();

        DBObject unitObject = new BasicDBObject("_id", objectId);
        unit.setId(objectId.toString());
        unitObject.put("name", unit.getName().trim());
        unitObject.put("unit", unit.getUnit());

        MongoDatabase.getUnitCollection().insert(unitObject);
        return unit;
    }

    public SimulationUnit getDefaultSimulationUnit() {

        SimulationUnit simulationUnit = new SimulationUnit();

        try {
            DBObject dbObject = MongoDatabase.getUnitCollection().findOne();
            if (dbObject != null) {
                simulationUnit.setName((String) dbObject.get("name"));
                simulationUnit.setUnit((String) dbObject.get("unit"));
            }

        } catch (Exception e) {
            throw new NotFoundException();
        }

        return simulationUnit;
    }

    public SimulationUnit getSimulationUnit(String objectId) {
        if (objectId == null || objectId.trim().isEmpty()) throw new IllegalSimulationArgumentException();

        SimulationUnit simulationUnit = new SimulationUnit();

        try {
            BasicDBObject selectWithObjectId = new BasicDBObject();
            selectWithObjectId.put("_id", new ObjectId(objectId));
            DBCursor cursor = MongoDatabase.getUnitCollection().find(selectWithObjectId);
            DBObject returnedSimulationUnit = null;
            if (cursor.hasNext()) {
                returnedSimulationUnit = cursor.next();
                simulationUnit.setName((String) returnedSimulationUnit.get("name"));
                simulationUnit.setUnit((String) returnedSimulationUnit.get("unit"));
            }
            simulationUnit.setId(objectId);

        } catch (Exception e) {
            throw new NotFoundException();
        }

        return simulationUnit;
    }

    public List<SimulationUnit> getSimulationUnit() {

        List<SimulationUnit> simulationUnitList = new ArrayList<>();

        DBCursor cursor = MongoDatabase.getUnitCollection().find();
        DBObject returnedSimulationUnit = null;

        while (cursor.hasNext()) {
            SimulationUnit simulationUnit = new SimulationUnit();

            returnedSimulationUnit = cursor.next();
            simulationUnit.setId((returnedSimulationUnit.get("_id")).toString());
            simulationUnit.setName((String) returnedSimulationUnit.get("name"));
            simulationUnit.setUnit((String) returnedSimulationUnit.get("unit"));
            simulationUnitList.add(simulationUnit);
        }

        return simulationUnitList;
    }

    public boolean removeSimulationUnit(String objectId) {
        if(objectId == null || objectId.trim().isEmpty()) {
            return false;
        }

        try {
            BasicDBObject query = new BasicDBObject();
            query.put("_id", new ObjectId(objectId));

            MongoDatabase.getUnitCollection().remove(query);
        } catch (Exception e) {
            throw new NotFoundException();
        }

        return true;
    }

    public boolean updateSimulationUnit(SimulationUnit simulationUnit) {
        if(simulationUnit.getId() == null || simulationUnit.getId().trim().isEmpty()) {
            return false;
        }

        if(simulationUnit.getName() == null || simulationUnit.getName().trim().isEmpty())
            return false;

        try {
            BasicDBObject newDocument = new BasicDBObject();
            newDocument.append("$set", new BasicDBObject().append("name", simulationUnit.getName()).
                    append("unit", simulationUnit.getUnit())
            );

            BasicDBObject searchQuery = new BasicDBObject().append("_id", new ObjectId(simulationUnit.getId()));
            MongoDatabase.getUnitCollection().update(searchQuery, newDocument);
        } catch (Exception e) {
            throw new NotFoundException();
        }

        return true;
    }
}
