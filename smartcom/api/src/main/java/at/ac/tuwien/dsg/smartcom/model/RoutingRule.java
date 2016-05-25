package at.ac.tuwien.dsg.smartcom.model;

/**
 * This class represents a routing rule that handles messages according to their
 * specific type, subtype and receiver.
 *
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class RoutingRule {
    private final String type;
    private final String subtype;
    private final Identifier receiver;

    private final Identifier route;

    public RoutingRule(String type, String subtype, Identifier receiver, Identifier route) {
        this.type = type;
        this.subtype = subtype;
        this.receiver = receiver;
        this.route = route;
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public Identifier getReceiver() {
        return receiver;
    }

    public Identifier getRoute() {
        return route;
    }

    @Override
    public String toString() {
        return "RoutingRule{" +
                "type='" + type + '\'' +
                ", subtype='" + subtype + '\'' +
                ", receiver='" + receiver + '\'' +
                ", route='" + route + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RoutingRule that = (RoutingRule) o;

        if (receiver != null ? !receiver.equals(that.receiver) : that.receiver != null) return false;
        if (route != null ? !route.equals(that.route) : that.route != null) return false;
        if (subtype != null ? !subtype.equals(that.subtype) : that.subtype != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (subtype != null ? subtype.hashCode() : 0);
        result = 31 * result + (receiver != null ? receiver.hashCode() : 0);
        result = 31 * result + (route != null ? route.hashCode() : 0);
        return result;
    }
}
