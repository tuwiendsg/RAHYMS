package at.ac.tuwien.dsg.hcu.composer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import at.ac.tuwien.dsg.hcu.composer.model.Solution;

public class ComposerTracer {

    private FileWriter fstream;
    private BufferedWriter out;

    public ComposerTracer() {}

    public ComposerTracer(String file) {
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

    public void traceln(Solution solution, String prefix) {
        traceln(prefix + solution.trace());
    }

    public String getTraceHeader() {
        return "solution_components,objective_value,cost,norm_cost,competency,connnectedness,mu_connnectedness,response_time,norm_response_time";
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
