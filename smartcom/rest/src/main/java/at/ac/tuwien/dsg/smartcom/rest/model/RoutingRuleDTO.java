package at.ac.tuwien.dsg.smartcom.rest.model;

import at.ac.tuwien.dsg.smartcom.model.Identifier;
import at.ac.tuwien.dsg.smartcom.model.RoutingRule;

/**
 * @author Philipp Zeppezauer (philipp.zeppezauer@gmail.com)
 * @version 1.0
 */
public class RoutingRuleDTO {

    private String type;
    private String subtype;
    private IdentifierDTO receiver;

    private IdentifierDTO route;

    public RoutingRuleDTO() {
    }

    public RoutingRuleDTO(RoutingRule rule) {
        this.type = rule.getType();
        this.subtype = rule.getSubtype();

        if (rule.getReceiver() != null) {
            this.receiver = new IdentifierDTO(rule.getReceiver());
        }

        if (rule.getRoute() != null) {
            this.route = new IdentifierDTO(rule.getRoute());
        }
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public IdentifierDTO getReceiver() {
        return receiver;
    }

    public IdentifierDTO getRoute() {
        return route;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public void setReceiver(IdentifierDTO receiver) {
        this.receiver = receiver;
    }

    public void setRoute(IdentifierDTO route) {
        this.route = route;
    }

    public RoutingRule create() {
        Identifier receiver = null;
        if (this.receiver != null) {
            receiver = this.receiver.create();
        }

        Identifier route = null;
        if (this.route != null) {
            route = this.route.create();
        }

        return new RoutingRule(type, subtype, receiver, route);
    }

    @Override
    public String toString() {
        return "RoutingRuleDTO{" +
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

        RoutingRuleDTO that = (RoutingRuleDTO) o;

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
