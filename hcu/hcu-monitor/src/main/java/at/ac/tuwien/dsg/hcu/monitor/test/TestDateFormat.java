package at.ac.tuwien.dsg.hcu.monitor.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TestDateFormat {

    public static void main(String[] args) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
            //dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = dateFormat.parse("Wed Feb 12 16:02:14 UTC 2014");
            System.out.println(date.getTime());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
