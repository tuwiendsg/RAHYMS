package scu.composer.model;

import scu.common.exceptions.NotFoundException;
import scu.common.fuzzy.MembershipFunction;
import scu.common.fuzzy.function.InfinityConnectednessMembershipFunctionCollection;
import scu.common.fuzzy.function.SkillMembershipFunctionCollection;
import scu.common.model.Assignment;
import scu.common.model.Functionality;
import scu.common.model.HumanComputingElement;
import scu.common.model.Role;
import scu.common.model.Service;
import scu.common.model.Task;
import scu.common.sla.Objective;

public class SolutionComponent extends Assignment {

    private int level;
    private double pheromone;
    private long estimatedResponseTime;

    public SolutionComponent() {
        super();
    }

    public SolutionComponent(int level) {
        super();
        this.level = level;
    }

    public SolutionComponent(int level, Service assignee, Task task, 
            Functionality functionality) throws NotFoundException {
        super(assignee, task, functionality);
        this.level = level;
    }

    public SolutionComponent(int level, Service assignee, Task task, Role role) {
        super(assignee, task, role);
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        //if (worker==null || job==null) return "SolutionComponent [level=" + level + "]";
        //else return "SolutionComponent [level=" + level + ", job=" + job + ", worker=" + worker.getId() + "]";
        if (getAssignee()==null || getTask()==null) return "Comp [" + level + "]";
        else return "[" + level + ", " + getTask() + ", S#" + getAssignee().getTitle() + "," + pheromone + "]";
    }

    public String trace() {
        return level + "," + getTask() + ",S#" + getAssignee().getTitle() + "," + pheromone;
    }

    public double getGradeOfSkill() {
        SkillMembershipFunctionCollection functions = new SkillMembershipFunctionCollection();
        double minGrade = Double.MAX_VALUE;
        
        for (Objective slo: getRole().getSpecification().getObjectives()) {
            if (slo.getType()==Objective.Type.SKILL) {
                /* 
                 * TODO: 
                 * - how to check it's fuzzy slo
                 * - mix fuzzy and double?
                 */
                MembershipFunction f = functions.getMembershipFunction((String)slo.getValue());
                HumanComputingElement hce = (HumanComputingElement)getAssignee().getProvider();
                double grade = f.grade((double)hce.getSkills().getValue(slo.getName()));
                if (grade<minGrade) minGrade = grade;
            }
        }
        
        if (minGrade>Double.MAX_VALUE-1) return 0; 
        else return minGrade;
    }

    public static double getGradeOfConnectedness(double connectednessScore, 
            String fuzzyValue) {
        InfinityConnectednessMembershipFunctionCollection functions = 
                new InfinityConnectednessMembershipFunctionCollection();
        MembershipFunction f = functions.getMembershipFunction(fuzzyValue);
        double score = f.grade(connectednessScore);
        return score;
    }

    public double getPheromone() {
        return pheromone;
    }

    public void setPheromone(double pheromone) {
        this.pheromone = pheromone;
    }

    public long getEstimatedResponseTime() {
        return estimatedResponseTime;
    }

    public void setEstimatedResponseTime(long estimatedResponseTime) {
        this.estimatedResponseTime = estimatedResponseTime;
    }
}
