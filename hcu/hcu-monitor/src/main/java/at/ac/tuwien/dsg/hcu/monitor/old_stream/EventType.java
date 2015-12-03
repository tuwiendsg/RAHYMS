package at.ac.tuwien.dsg.hcu.monitor.old_stream;

public enum EventType {
    NOTIFIED, CLAIMED, ASSIGNED, RUNNING,
    SUSPENDED, RESUMED, CANCELED, FAILED, FINISHED,
    CREATED, UPDATED, DELETED,
    TRANSITIONED,
    METRIC // This is for metric stream
}
