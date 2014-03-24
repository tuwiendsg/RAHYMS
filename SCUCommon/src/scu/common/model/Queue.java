package scu.common.model;

public class Queue {

    /**
     *  TODO: 
     *  - use array of int to be more efficient
     *  - define status constant
     */
    private String sequence;

    public Queue() {
        this.sequence = "";
    }
    public Queue(String sequence) {
        this.sequence = sequence;
    }
    public String getSequence() {
        return sequence;
    }
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
    public void extendSequence(String sequence) {
        this.sequence += sequence;
    }
    @Override
    public String toString() {
        //return "Q [seq=" + sequence + "]";
        return sequence;
    }

}
