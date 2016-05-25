package at.ac.tuwien.dsg.hcu.monitor.interfaces;

public interface Waker {
    public int wakeMeAfter(Wakeable object, Double time);
    public int wakeMeAt(Wakeable object, Double time);
}
