package at.ac.tuwien.dsg.hcu.common.model;

import java.util.Hashtable;

import at.ac.tuwien.dsg.hcu.common.interfaces.MetricInterface;

public class Metrics extends Properties {

    protected Hashtable<String, MetricInterface> valueSet;
    private ComputingElement owner;

    public Metrics() {
        valueSet = new Hashtable<String, MetricInterface>();
    }

    public void setOwner(ComputingElement owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        String result = "";
        for (String name : valueSet.keySet()) {
            if (!result.equals("")) result += ",";
            result += valueSet.get(name).toString();
        }
        result = "{" + result + "}";
        return result;
    }

    @Override
    public Object getValue(String name, Object _default) {
        MetricInterface metricInterface = valueSet.get(name);
        Object result = null;
        if (metricInterface!=null) {
            result = metricInterface.measure(owner, name, null);
        } else {
            result = _default;
        }
        return result;
    }

    public Object getValue(String name, Object[] params) {
        MetricInterface metricInterface = valueSet.get(name);
        Object result = null;
        if (metricInterface!=null) {
            result = metricInterface.measure(owner, name, params);
        }
        return result;
    }

    @Override
    public void setValue(String name, Object value) {
        MetricInterface metricInterface = valueSet.get(name);
        if (metricInterface!=null) {
            metricInterface.update(owner, name, new Object[]{value});
        }
    }

    public void setValue(String name, Object[] value) {
        MetricInterface metricInterface = valueSet.get(name);
        if (metricInterface!=null) {
            metricInterface.update(owner, name, value);
        }
    }

    @Override
    public boolean has(String name) {
        return valueSet.containsKey(name);
    }

    public void setInterface(String name, MetricInterface iface) {
        valueSet.put(name, iface);
    }

}
