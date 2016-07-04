package at.ac.tuwien.dsg.hcu.common.model;

public class Connection {

    // TODO: directed connection
    
    private Long node1; // node1.id<node2
    private Long node2;
    private double weight;

    public Connection() {
    }

    public Connection(long node1) {
        this.node1 = node1;
        this.node2 = 0L;
        this.weight = Double.MAX_VALUE; // TODO: this max value will depend on our conectedness membership function
    }

    public Connection(long node1, long node2, double weight) {
        if (node1<node2) {
            this.node1 = node1;
            this.node2 = node2;
        } else {
            this.node1 = node2;
            this.node2 = node1;      
        }
        this.weight = weight;
    }

    public void setNode1(long node1) {
        this.node1 = node1;
    }

    public void setNode2(long node2) {
        this.node2 = node2;
    }

    public long getNode1() {
        return node1;
    }
    public long getNode2() {
        return node2;
    }
    public double getWeight() {
        return weight;
    }
    public void setWeight(double weight2) {
        this.weight = weight2;
    }
    
    public long getOther(long e1) {
        Long other = null;
        if (e1==node1) other = node2;
        else if (e1==node2) other= node1;
        return other;
    }

    @Override
    public String toString() {
        return "Conn [node1=" + node1 + ", node2=" + node2
                + ", weight=" + weight + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((node1 == null) ? 0 : node1.hashCode());
        result = prime * result + ((node2 == null) ? 0 : node2.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Connection other = (Connection) obj;
        if (node1 == null) {
            if (other.node1 != null)
                return false;
        } else if (!node1.equals(other.node1))
            return false;
        if (node2 == null) {
            if (other.node2 != null)
                return false;
        } else if (!node2.equals(other.node2))
            return false;
        return true;
    }


}
