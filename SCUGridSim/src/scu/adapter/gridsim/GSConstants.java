package scu.adapter.gridsim;

public class GSConstants {

    // communication tags
    public static final int SUBMIT_TASK = 1000;
    public static final int RESERVE_ASSIGNMENT = 1010;
    public static final int COMMIT_ASSIGNMENT = 1020;
    public static final int RETURN_ASSIGNMENT = 1030;
    public static final int FORECAST_RESPONSE_TIME = 1040;
    public static final int FORECAST_RESPONSE_TIME_RESULT = 1041;
    public static final int WAKE_SCHEDULER = 1050;
    
    public static final int FORWARD_ASSIGNMENT = 1060;
    
    public static final int HELLO_TAG = 2001;
    public static final int TEST_TAG = 2002;

    // time constant values
    public static final int MILLI_SEC = 1000;
    public static final int SEC = 1;           // 1 second
    public static final int MIN = 60 * SEC;    // 1 min in seconds
    public static final int HOUR = 60 * MIN;   // 1 hour in minutes
    public static final int DAY = 24 * HOUR;   // 1 day in hours
    
}
