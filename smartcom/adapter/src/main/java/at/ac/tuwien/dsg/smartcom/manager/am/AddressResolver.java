package at.ac.tuwien.dsg.smartcom.manager.am;

import at.ac.tuwien.dsg.smartcom.manager.dao.PeerChannelAddressResolverDAO;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.picocontainer.annotations.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Class that assists in resolving the addresses of peers. For that
 * purpose new addresses can be stored in the address resolver and
 * will be returned upon request. The resolver saves all new addresses
 * to a database and retrieves them also from the database.
 *
 * To reduce the overhead of querying the database for addresses, the
 * address resolver keeps a cache whereas each entry is valid for 10 minutes.
 * The cache size can be defined using a parameter of the constructor.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class AddressResolver {
    private static final Logger log = LoggerFactory.getLogger(AddressResolver.class);
    private static final int DEFAULT_CACHE_SIZE = 1000;

    private final LoadingCache<AddressKey, PeerChannelAddress> cache;

    private ExecutorService executor;

    @Inject
    private PeerChannelAddressResolverDAO dao; //DAO that is used to find, persist and delete peer addresses

    /**
     * Create a new address resolver with the default cache size.
     *
     * @see AddressResolver#AddressResolver(int)
     * @see AddressResolver#DEFAULT_CACHE_SIZE
     */
    public AddressResolver() {
        this(DEFAULT_CACHE_SIZE);
    }

    /**
     * Create a new address resolver with a predefined cache size.
     * The resolver keeps a cache to reduce the overhead of frequent requests for the same address of a peer.
     * Entries are valid for at most 10 minutes.
     *
     * @param cacheSize the maximum size of the cache.
     */
    public AddressResolver(int cacheSize) {

        //Creates a cache of size *cacheSize* whereas each entry is valid for at most 10 minutes
        cache = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(
                        //load entries from the database in case of a cache miss
                        new CacheLoader<AddressKey, PeerChannelAddress>() {
                            @Override
                            public PeerChannelAddress load(AddressKey addressKey) throws Exception {
                                log.debug("loading address {} from database", addressKey);
                                PeerChannelAddress address = AddressResolver.this.dao.find(addressKey.peerId, addressKey.adapterId);

                                //throw an exception if there is no such address because it is not allowed to return null here
                                if (address == null) {
                                    throw new AddressResolverException();
                                }
                                return address;
                            }


                        });
    }

    @PostConstruct
    public void init() {
        executor  = Executors.newSingleThreadExecutor();
    }

    @PreDestroy
    public void destroy() {
        //shut down the executor
        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("Could not await termination of executor. forcing shutdown", e);
            executor.shutdownNow();
        }
    }

    /**
     * Returns the peer address of a given peer for a specific adapter. The address
     * will be resolved using the cache or - in case of a cache miss - from the database.
     *
     * If there is no such address available, the method will return null.
     * @param peerId id of the peer
     * @param adapterId if of the adapter
     * @return a corresponding address or null if there is no such address
     */
    public PeerChannelAddress getPeerAddress(Identifier peerId, Identifier adapterId) {
        try {
            return cache.get(new AddressKey(peerId, Identifier.adapter(adapterId.returnIdWithoutPostfix())));
        } catch (ExecutionException e) {
            if (e.getCause() instanceof AddressResolverException) {
                log.trace("There is an address for PeerId {} and AdapterId {}", peerId, adapterId);
                return null;
            }
            log.error("Exception during retrieval of peer address!", e);
            return null;
        }
    }

    /**
     * Add a new peer address to the address resolver. It will be stored to the database immediately
     * and will be kept in the cache as well.
     *
     * If there is already such an entry, the entry will be replaced by the new one.
     *
     * @param address the new peer address.
     */
    public void addPeerAddress(final PeerChannelAddress address) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                dao.insert(address);
            }
        });

        cache.put(new AddressKey(address.getPeerId(), address.getChannelType()), address);
    }

    /**
     * Remove a peer address that matches the peer id and the adapter id from the address resolver.
     *
     * If such an entry exists, it will be removed from the database and the corresponding entry in the cache
     * will be invalidated.
     *
     * @param peerId id of the peer
     * @param adapterId id of the adapter
     */
    public void removePeerAddress(Identifier peerId, Identifier adapterId) {
        cache.invalidate(new AddressKey(peerId, adapterId));
        dao.remove(peerId, adapterId);
    }

    /**
     * Internal class used for cache entry to be able to provide a key that consists of two IDs.
     */
    private class AddressKey {
        final Identifier peerId;
        final Identifier adapterId;

        private AddressKey(Identifier peerId, Identifier adapterId) {
            this.peerId = peerId;
            this.adapterId = adapterId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AddressKey that = (AddressKey) o;

            return adapterId.equals(that.adapterId) && peerId.equals(that.peerId);
        }

        @Override
        public int hashCode() {
            int result = peerId.hashCode();
            result = 31 * result + adapterId.hashCode();
            return result;
        }
    }

    /**
     * Internal exception that indicates that the address could not be resolved.
     * It has been introduced because a CacheLoader in the Cache is not allowed to
     * return null if the entry could not be loaded.
     */
    private class AddressResolverException extends Exception {
    }
}
