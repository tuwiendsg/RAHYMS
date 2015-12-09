package at.ac.tuwien.dsg.hcu.monitor.interfaces;

public interface Wakeable {
    public void wake(int wakeId);
    public void setWaker(Waker waker);
}
