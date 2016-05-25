package at.ac.tuwien.dsg.smartcom.manager.dao;

import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;
import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * ResolverDAO implementation that uses MongoDB as its underlying database system.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class MongoDBPeerChannelAddressResolverDAO implements PeerChannelAddressResolverDAO {
    private static final Logger log = LoggerFactory.getLogger(MongoDBPeerChannelAddressResolverDAO.class);
    private static final String RESOLVER_COLLECTION = "PEER_ADDRESS_RESOLVER_COLLECTION";

    private final DBCollection coll;

    /**
     * Create a new MongoDB resolver DAO providing the host and port of the MongoDB instance and
     * the name of the database that should be used. It will use the default collection of this
     * resolver in the MongoDB database.
     *
     * @param host address of the MongoDB instance
     * @param port port number of the MongoDB instance
     * @param database name of the database that should be used.
     * @throws UnknownHostException if the database cannot be resolved
     * @see MongoDBPeerChannelAddressResolverDAO#RESOLVER_COLLECTION
     */
    public MongoDBPeerChannelAddressResolverDAO(String host, int port, String database) throws UnknownHostException {
        this(new MongoClient(host, port), database, RESOLVER_COLLECTION);
    }

    /**
     * Create a new MongoDB resolver DAO providing an already created client for a MongoDB instance,
     * the name of the database and a collection that should be used to save, retrieve and delete
     * entries.
     *
     * This constructor is especially useful for unit testing because data can be preloaded in the
     * specified collection or the presence of added entries can be checked.
     *
     * @param client MongoClient that is connected to a database
     * @param database name of the database that should be used
     * @param collection name of the collection that should be used
     */
    public MongoDBPeerChannelAddressResolverDAO(MongoClient client, String database, String collection) {
        coll = client.getDB(database).getCollection(collection);
    }

    @Override
    public void insert(PeerChannelAddress address) {
        BasicDBObject doc = serializePeerAddress(address);

        try {
            //try to insert the new document
            coll.insert(doc);
            log.trace("Created document for message: {}", doc);
        } catch (DuplicateKeyException e) {

            //there is already such a document, update it!
            coll.update(new BasicDBObject("_id", address.getPeerId().getId()+"."+address.getChannelType().getId()), doc);
            log.trace("Updated document for message: {}", doc);
        }
    }

    /**
     * Serializes a peer address into a MongoDB specific document format.
     *
     * @param address that should be serialized
     * @return MongoDB specific document format
     */
    BasicDBObject serializePeerAddress(PeerChannelAddress address) {
        BasicDBObject contactParams = new BasicDBObject();
        int i = 0;
        for (Serializable o : address.getContactParameters()) {
            contactParams.append((i++)+"", o);
        }

        BasicDBObject doc = new BasicDBObject()
                .append("_id", address.getPeerId().getId()+"."+address.getChannelType().getId())
                .append("peerId", address.getPeerId().getId())
                .append("adapterId", address.getChannelType().getId())
                .append("contactParameters", contactParams);
        log.trace("Saving peer address in mongoDB: {}", doc);
        return doc;
    }

    @Override
    public PeerChannelAddress find(Identifier peerId, Identifier adapterId) {
        BasicDBObject query = new BasicDBObject("_id", peerId.getId()+"."+adapterId.getId());

        PeerChannelAddress address = null;
        DBObject one = coll.findOne(query);
        if (one != null) {
            address = deserializePeerAddress(one);
        }

        log.trace("Found peer address for query {}: {}", query, address);
        return address;
    }

    @Override
    public void remove(Identifier peerId, Identifier adapterId) {
        coll.remove(new BasicDBObject("_id", peerId.getId()+"."+adapterId.getId()));
    }

    /**
     * Creates a peer address from a retrieved MongoDB specific document object.
     *
     * @param dbObject MongoDB specific document that represents a peer address
     * @return the corresponding peer address
     */
    PeerChannelAddress deserializePeerAddress(DBObject dbObject) {
        PeerChannelAddress address;List<Serializable> list = new ArrayList<>();
        DBObject contactParameters = (DBObject) dbObject.get("contactParameters");
        int i = 0;
        while (contactParameters.containsField(i + "")) {
            list.add((Serializable) contactParameters.get((i++) + ""));
        }

        address = new PeerChannelAddress(Identifier.peer((String) dbObject.get("peerId")), Identifier.adapter((String) dbObject.get("adapterId")), list);
        return address;
    }
}
