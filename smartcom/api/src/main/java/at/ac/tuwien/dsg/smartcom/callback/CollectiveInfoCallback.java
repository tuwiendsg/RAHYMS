package at.ac.tuwien.dsg.smartcom.callback;

import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchCollectiveException;
import at.ac.tuwien.dsg.smartcom.model.CollectiveInfo;
import at.ac.tuwien.dsg.smartcom.model.Identifier;

/**
 * This API is used to provide different information regarding the composition
 * and the state of the collectives to the Middleware, in order for the Middleware
 * to allow to other SmartSociety components the functionality of addressing their
 * messages on the Collective level.
 *
 * At this point, the API consists of a single method, but as the TEE and EPE components
 * get developed later, the API may grow and/or change.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public interface CollectiveInfoCallback {

    /**
     * Resolves and returns the members of a given collective id.
     *
     * @param collective The id of the collective
     * @return Returns a list of peer ids that are part of the collective and other collective related information
     * @throws NoSuchCollectiveException if there exists no such collective.
     */
    public CollectiveInfo getCollectiveInfo(Identifier collective) throws NoSuchCollectiveException;
}
