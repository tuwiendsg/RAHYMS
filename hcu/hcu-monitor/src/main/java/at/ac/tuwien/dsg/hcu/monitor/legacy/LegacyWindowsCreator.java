package at.ac.tuwien.dsg.hcu.monitor.legacy;

import com.espertech.esper.client.EPServiceProvider;

public class LegacyWindowsCreator {

    public static void run(EPServiceProvider epService) {

        String expression;

        expression = "CREATE WINDOW AssignmentCount.win:keepall() AS "
                        + "SELECT unit, 0L as value FROM AssignmentStream";
        epService.getEPAdministrator().createEPL(expression);

        expression = "CREATE WINDOW FinishedCount.win:keepall() AS "
                        + "SELECT unit, 0L as value FROM AssignmentStream";
        epService.getEPAdministrator().createEPL(expression);
        
        expression = "CREATE WINDOW CollectiveStreamWindow.std:unique(collective.id) AS "
                + "SELECT * FROM CollectiveStream ";
        epService.getEPAdministrator().createEPL(expression);

        expression = "CREATE WINDOW AssignmentStreamWindow.std:unique(assignment.id) AS "
                + "SELECT * FROM AssignmentStream ";
        epService.getEPAdministrator().createEPL(expression);

        expression = "CREATE WINDOW Utilization.std:unique(unit.id) AS "
                + "SELECT type, unit, 0.0 as value FROM AssignmentStream ";
        epService.getEPAdministrator().createEPL(expression);

        expression = "CREATE WINDOW HCUUtilization.std:unique(collective.id) AS "
                + "SELECT type, collective, 0.0 as value, 0L as cnt FROM CollectiveStream ";
        epService.getEPAdministrator().createEPL(expression);
    }


}
