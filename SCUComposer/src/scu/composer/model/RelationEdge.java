package scu.composer.model;

import org.jgrapht.graph.DefaultWeightedEdge;

public class RelationEdge extends DefaultWeightedEdge {

    private static final long serialVersionUID = 3470367083442610368L;

    @Override
    public String toString() {
        return "[" + String.format("%.3f", getWeight()) + "]";
    }


}
