package at.ac.tuwien.dsg.smartcom.model;

import java.util.List;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class PeerInfo {
    private Identifier id;
    private DeliveryPolicy.Peer deliveryPolicy;
    private List<PrivacyPolicy> privacyPolicies;
    private List<PeerChannelAddress> addresses; //ordered list, ordering defines the preferences

    public PeerInfo(Identifier id, DeliveryPolicy.Peer deliveryPolicy, List<PrivacyPolicy> privacyPolicies, List<PeerChannelAddress> addresses) {
        this.id = id;
        this.deliveryPolicy = deliveryPolicy;
        this.privacyPolicies = privacyPolicies;
        this.addresses = addresses;
    }

    public PeerInfo() {
    }

    public Identifier getId() {
        return id;
    }

    public void setId(Identifier id) {
        this.id = id;
    }

    public DeliveryPolicy.Peer getDeliveryPolicy() {
        return deliveryPolicy;
    }

    public void setDeliveryPolicy(DeliveryPolicy.Peer deliveryPolicy) {
        this.deliveryPolicy = deliveryPolicy;
    }

    public List<PrivacyPolicy> getPrivacyPolicies() {
        return privacyPolicies;
    }

    public void setPrivacyPolicies(List<PrivacyPolicy> privacyPolicies) {
        this.privacyPolicies = privacyPolicies;
    }

    public List<PeerChannelAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<PeerChannelAddress> addresses) {
        this.addresses = addresses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PeerInfo peerInfo = (PeerInfo) o;

        if (id != null ? !id.equals(peerInfo.id) : peerInfo.id != null) return false;

        return true;
    }
    
    /**
     * Checks if the two objects are equal by also checking whether the collection-type members contain the same elements.
     * @param o
     * @return
     */
    public boolean equalsByDeepCoparison(Object o) {
        if (!this.equals(o)) return false;
        
        PeerInfo other = (PeerInfo) o; //so far the same by Id
        
        if (other.deliveryPolicy == null || deliveryPolicy != other.deliveryPolicy) return false; //differ in privacy policy
        
        if (other.privacyPolicies != null && other.privacyPolicies != this.privacyPolicies){
        	if (this.privacyPolicies == null) return false;
        	boolean equalLists = other.privacyPolicies.size() == this.privacyPolicies.size() && other.privacyPolicies.containsAll(this.privacyPolicies);
        	if (!equalLists) return false; //this should work if duplicates are not allowed, and they should not be allowed.
        }
        
        if (other.addresses != null && other.addresses != this.addresses){
        	if (this.addresses == null) return false;
        	boolean equalLists = other.addresses.size() == this.addresses.size() && other.addresses.containsAll(this.addresses);
        	if (!equalLists) return false; //this should work if duplicates are not allowed, and they should not be allowed.
        }
        
        	
        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "PeerInfo{" +
                "id=" + id +
                ", deliveryPolicy=" + deliveryPolicy +
                ", privacyPolicies=" + privacyPolicies +
                ", addresses=" + addresses +
                '}';
    }
}
