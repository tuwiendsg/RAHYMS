package scu.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import scu.common.model.Role;
import scu.common.model.Task;

public class WekaExporter {

    public static void export(List<Task> tasks, String pathPrefix) {

        FileWriter fstream;
        BufferedWriter out;
        
        if (tasks.size()==0) return;
        
        // assuming all tasks statistic have the same set of patterns
        Set<String> patterns = tasks.get(0).getStat().getPatternSet();
        List<Role> sortedRoles = tasks.get(0).getStat().getSortedRoles();

        try {
            
            for (String pattern: patterns) {

                String file = pathPrefix + pattern + ".arff";
                fstream = new FileWriter(file);
                out = new BufferedWriter(fstream);
                
                // print out headers
                out.write("@RELATION pattern\n\n");
                out.write("@ATTRIBUTE task STRING\n");
                out.write("@ATTRIBUTE task_status STRING\n");
                for (Role role: sortedRoles) {
                    out.write("@ATTRIBUTE " + role.getFunctionality().getName() + " NUMERIC\n");
                }
                out.write("@ATTRIBUTE response_time NUMERIC\n\n");
                out.write("@DATA\n");
                out.flush();

                for (Task task: tasks) {
                    String row = task.getStat().dump(pattern, sortedRoles);
                    out.write(row + "\n");
                    out.flush();
                }
                
                out.close();
                fstream.close();
                
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        
    }
    
}
