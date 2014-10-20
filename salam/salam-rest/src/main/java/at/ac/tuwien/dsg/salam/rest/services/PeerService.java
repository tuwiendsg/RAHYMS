package at.ac.tuwien.dsg.salam.rest.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import at.ac.tuwien.dsg.salam.cloud.manager.ServiceManagerOnMemory;
import at.ac.tuwien.dsg.salam.common.model.Assignment;
import at.ac.tuwien.dsg.salam.common.model.ComputingElement;
import at.ac.tuwien.dsg.salam.common.model.Functionality;
import at.ac.tuwien.dsg.salam.common.model.Service;
import at.ac.tuwien.dsg.salam.rest.common.Util;
import at.ac.tuwien.dsg.salam.rest.config.AppConfig;
import at.ac.tuwien.dsg.salam.rest.exceptions.AlreadyExistsException;
import at.ac.tuwien.dsg.salam.rest.exceptions.NotFoundException;
import at.ac.tuwien.dsg.salam.rest.resource.Collective;
import at.ac.tuwien.dsg.salam.rest.resource.Peer;
import at.ac.tuwien.dsg.salam.rest.resource.Task;
import at.ac.tuwien.dsg.smartcom.callback.CollectiveInfoCallback;
import at.ac.tuwien.dsg.smartcom.callback.PeerAuthenticationCallback;
import at.ac.tuwien.dsg.smartcom.callback.PeerInfoCallback;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchCollectiveException;
import at.ac.tuwien.dsg.smartcom.callback.exception.NoSuchPeerException;
import at.ac.tuwien.dsg.smartcom.callback.exception.PeerAuthenticationException;
import at.ac.tuwien.dsg.smartcom.model.CollectiveInfo;
import at.ac.tuwien.dsg.smartcom.model.DeliveryPolicy;
import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.PeerChannelAddress;
import at.ac.tuwien.dsg.smartcom.model.PeerInfo;

public class PeerService extends ServiceManagerOnMemory 
implements PeerAuthenticationCallback, PeerInfoCallback, CollectiveInfoCallback{

    protected static String PEER_INFO_KEY = "peer_info";

    // TODO: merge peerCache with elementCache
    protected static ConcurrentMap<String, Peer> peerCache = new ConcurrentHashMap<String, Peer>(); 
    // can't merge Collective and CollectiveInfo now, dont want to expose CollectiveInfo to REST
    protected static ConcurrentMap<Integer, Collective> collectiveCache = new ConcurrentHashMap<Integer, Collective>();
    protected static ConcurrentMap<Integer, CollectiveInfo> collectiveInfoCache = new ConcurrentHashMap<Integer, CollectiveInfo>();
    protected static int lastCollectiveId = 1;
    
    public PeerService() {
        populate();
    }

    public CollectiveInfo getCollectiveInfo(Identifier collective)
            throws NoSuchCollectiveException {
        long id = Long.parseLong(collective.getId());
        return collectiveInfoCache.get(id);
    }

    public PeerInfo getPeerInfo(Identifier id) throws NoSuchPeerException {
        long elementId = Long.parseLong(id.getId());
        ComputingElement element = retrieveElement(elementId);
        if (element==null) return null;
        else {
            PeerInfo info = (PeerInfo)element.getProperties().getValue(PEER_INFO_KEY, null);
            return info;
        }
    }

    public boolean authenticate(Identifier peerId, String password)
            throws PeerAuthenticationException {
        return true;
    }

    public void setPeerInfo(long id, PeerInfo info) {
        ComputingElement element = retrieveElement(id);
        if (element!=null) {
            element.getProperties().setValue(PEER_INFO_KEY, info);
        }
    }

    public Identifier registerCollective(Task task, List<Assignment> assignments, DeliveryPolicy.Collective deliveryPolicy) {
        
        if (assignments==null || assignments.size()==0) return null;
        
        Collective collective = new Collective();
        List<Identifier> peers = new ArrayList<Identifier>();

        // covert SALAM assignment model to REST collective assignment model
        for (Assignment assignment: assignments) {
            ComputingElement provider = assignment.getAssignee().getProvider();
            Peer peer = getPeerById((int)provider.getId());
            String service = assignment.getRole().getFunctionality().getName();
            collective.addAssignment(service, peer);
            peers.add(Identifier.peer(Long.toString(provider.getId())));
        }
        
        int collectiveId = lastCollectiveId++;
        Identifier id = Identifier.collective(Long.toString(collectiveId));
        collective.setId(collectiveId);
        collective.setTask(task);
        CollectiveInfo collectiveInfo = new CollectiveInfo(id, peers, deliveryPolicy);
        collectiveCache.put(collectiveId, collective);
        collectiveInfoCache.put(collectiveId, collectiveInfo);
        
        return id;
    }

    public void populate() {
        String config = System.getProperty(AppConfig.SALAM_REST_CONFIG);
        Boolean populatePeers = Boolean.valueOf(Util.getProperty(config, "populate_peers"));
        String gmailPrefix = Util.getProperty(config, "populate_peers_gmail_prefix");
        String restUrl = Util.getProperty(config, "populate_peers_rest_url");
        if (populatePeers) {
            try {
                addPeer("Alpha", gmailPrefix + "+alpha@gmail.com", Arrays.asList("operator"));
                addPeer("Bravo", gmailPrefix + "+bravo@gmail.com", Arrays.asList("security"));
                addPeer("Charlie", gmailPrefix + "+charlie@gmail.com", Arrays.asList("technician"));
                addPeer("Delta", gmailPrefix + "+delta@gmail.com", Arrays.asList("emergency_response", "security"));
                addPeer("Echo", gmailPrefix + "+echo@gmail.com", Arrays.asList("emergency_response", "security"));
                addPeer("Foxtrot", gmailPrefix + "+foxtrot@gmail.com", Arrays.asList("emergency_response"));
                addPeer("Golf", gmailPrefix + "+golf@gmail.com", Arrays.asList("operator", "technician"));
                addPeer("Hotel", gmailPrefix + "+hotel@gmail.com", Arrays.asList("technician", "emergency_response"));
                addPeer("India", gmailPrefix + "+india@gmail.com", Arrays.asList("operator", "security"));
                addPeer("Juliett", gmailPrefix + "+juliett@gmail.com", Arrays.asList("technician"));
                addPeer("Kilo", gmailPrefix + "+kilo@gmail.com", Arrays.asList("technician", "emergency_response"));
                addPeer("Lima", gmailPrefix + "+lima@gmail.com", Arrays.asList("technician"));
                addPeer("Mike", gmailPrefix + "+mike@gmail.com", Arrays.asList("operator", "emergency_response"));
                addPeer("November", gmailPrefix + "+november@gmail.com", Arrays.asList("technician"));
                addPeer("Oscar", gmailPrefix + "+oscar@gmail.com", Arrays.asList("security"));
                addPeer("Papa", gmailPrefix + "+papa@gmail.com", Arrays.asList("operator"));
                addPeer("Quebec", gmailPrefix + "+quebec@gmail.com", Arrays.asList("technician"));
                addPeer("Romeo", gmailPrefix + "+romeo@gmail.com", Arrays.asList("security", "emergency_response"));
                addPeer("Sierra", gmailPrefix + "+sierra@gmail.com", Arrays.asList("operator"));
                addPeer("Tango", gmailPrefix + "+tango@gmail.com", Arrays.asList("security"));
                addPeer("Uniform", gmailPrefix + "+uniform@gmail.com", Arrays.asList("operator"));
                addPeer("Venezia", gmailPrefix + "+venezia@gmail.com", Arrays.asList("technician"));
                addPeer("Washington", gmailPrefix + "+washington@gmail.com", Arrays.asList("security", "emergency_response"));
                addPeer("X-ray", gmailPrefix + "+xray@gmail.com", Arrays.asList("emergency_response"));
                addPeer("Yankee", gmailPrefix + "+yankee@gmail.com", Arrays.asList("security"));
                addPeer("Zulu", gmailPrefix + "+zulu@gmail.com", Arrays.asList("operator"));
                if (restUrl!=null) {
                    addPeer("Software Alpha", gmailPrefix + "+zw+alpha@gmail.com", restUrl, Arrays.asList("emergency_workflow"));
                    addPeer("Software Bravo", gmailPrefix + "+zw+bravo@gmail.com", restUrl, Arrays.asList("emergency_workflow"));
                    addPeer("Software Charlie", gmailPrefix + "+zw+charlie@gmail.com", restUrl, Arrays.asList("emergency_workflow"));
                    addPeer("Software Delta", gmailPrefix + "+zw+delta@gmail.com", restUrl, Arrays.asList("repair_workflow"));
                    addPeer("Software Echo", gmailPrefix + "+zw+echo@gmail.com", restUrl, Arrays.asList("repair_workflow"));
                }
            } catch (Exception ignored) {
            }            
        }
    }

    // REST SERVICES FOLLOWS
    public Peer addPeer(String name, String email, String rest, List<String> services) {

        Peer peer = new Peer(name, email, rest, services);

        if (peerCache.putIfAbsent(email, peer) != null) {
            throw new AlreadyExistsException();
        }

        // create computing element
        ComputingElement element = createElement();
        peer.setElementId(element.getId());

        // create services
        createServices(element, services);

        // create peer info
        createPeerInfo(peer);

        return peer;
    }


    public Peer addPeer(String name, String email, List<String> services) {
        
        Peer peer = new Peer(name, email, services);

        if (peerCache.putIfAbsent(email, peer) != null) {
            throw new AlreadyExistsException();
        }
        
        // create computing element
        ComputingElement element = createElement();
        peer.setElementId(element.getId());
        
        // create services
        createServices(element, services);
        
        // create peer info
        createPeerInfo(peer);

        return peer;
    }
    
    private List<Service> createServices(ComputingElement element, List<String> services) {
        List<Service> result = new ArrayList<Service>(); 
        // clean up existing services
        for (Service service: element.getServices()) {
            try {
                removeService(service);
            } catch (at.ac.tuwien.dsg.salam.common.exceptions.NotFoundException e) {
                e.printStackTrace();
            }
        }
        // create new services
        for (String s: services) {
            Service service = new Service(new Functionality(s), element);
            element.addService(service);
            registerService(service);
            result.add(service);
        }
        return result;
    }
    
    private PeerInfo createPeerInfo(Peer peer) {
        List<PeerChannelAddress> addresses = new ArrayList<PeerChannelAddress>();
        // add email channel
        Identifier id = Identifier.peer(Long.toString(peer.getElementId()));

        if (peer.getEmail() != null && !peer.getEmail().isEmpty()) {
            // add email channel
            List<Serializable> parameters = new ArrayList<Serializable>(1);
            parameters.add(peer.getEmail());
            PeerChannelAddress address = new PeerChannelAddress(id, Identifier.channelType("Email"), parameters);
            addresses.add(address);
        }

        if (peer.getRest() != null && !peer.getRest().isEmpty()) {
            List<Serializable> parameters = new ArrayList<Serializable>(1);
            parameters.add(peer.getRest());
            PeerChannelAddress address = new PeerChannelAddress(id, Identifier.channelType("REST"), parameters);
            addresses.add(address);
        }

        // create peerinfo
        PeerInfo info = new PeerInfo(id, DeliveryPolicy.Peer.TO_ALL_CHANNELS, null, addresses);
        setPeerInfo(peer.getElementId(), info);
        return info;
    }

    public List<Peer> getPeers(int page, int pageSize) {
        
        final List<Peer> slice = new ArrayList<Peer>(pageSize);
        final Iterator<Peer> iterator = peerCache.values().iterator();
        for( int i = 0; slice.size() < pageSize && iterator.hasNext(); ) {
            if( ++i > ( ( page - 1 ) * pageSize ) ) {
                slice.add(iterator.next());
            }
        }

        return slice;
    }

    public List<Collective> getCollectives(int page, int pageSize) {
        
        final List<Collective> slice = new ArrayList<Collective>(pageSize);
        final Iterator<CollectiveInfo> iterator = collectiveInfoCache.values().iterator();
        for( int i = 0; slice.size() < pageSize && iterator.hasNext(); ) {
            if( ++i > ( ( page - 1 ) * pageSize ) ) {
                CollectiveInfo info = iterator.next();
                Integer id = Integer.parseInt(info.getId().getId());
                slice.add(getCollectiveById(id));
            }
        }

        return slice;
    }

    public Collective getCollectiveById(Integer id) {
        return collectiveCache.get(id);
    }

    public Peer getPeerByEmail(String email) {
        final Peer peer = peerCache.get(email);

        if(peer == null) {
            throw new NotFoundException();
        }

        return peer;
    }

    public Peer getPeerById(Integer id) {
        
        Peer result = null;
        final Iterator<Peer> iterator = peerCache.values().iterator();
        while(iterator.hasNext()) {
            Peer peer = iterator.next();
            if (peer.getElementId()==id.longValue()) {
                result = peer;
                break;
            }
        }
        return result;
    }

    public void removePeer(String email ) {
        if (peerCache.remove(email) == null) {
            throw new NotFoundException();
        }
    }
}
