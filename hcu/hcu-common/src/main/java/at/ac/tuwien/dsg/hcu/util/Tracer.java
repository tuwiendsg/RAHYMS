package at.ac.tuwien.dsg.hcu.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Tracer {

    private FileWriter fstream;
    private BufferedWriter out;

    public Tracer(String file) {
        try {
            fstream = new FileWriter(file);
            out = new BufferedWriter(fstream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void trace(String text) {
        try {
            out.write(text);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void traceln(String text) {
        trace(text + "\n");
    }

    public String getTraceHeader() {
        return "";
    }

    public void close() {
        try {
            out.close();
            fstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
