package scu.composer.model;

import java.util.ArrayList;

import scu.common.model.Assignment;

public class Solution {

    private ArrayList<SolutionComponent> list = new ArrayList<SolutionComponent>();

    private double aggregateScore;
    private double skillScore;
    private double connectednessScore;
    private double fuzzyConnectedness;
    private double time;
    private double normTime;
    private double cost;
    private double normCost;
    private String data;

    public ArrayList<SolutionComponent> getList() {
        return list;
    }

    public ArrayList<Assignment> getAssignments() {
        ArrayList<Assignment> assignments = new ArrayList<Assignment>();
        for (Assignment a: list) {
            assignments.add(a);
        }
        return assignments;
    }

    public void setList(ArrayList<SolutionComponent> list) {
        this.list = list;
    }

    public double getSkillScore() {
        return skillScore;
    }

    public void setSkillScore(double skillScore) {
        this.skillScore = skillScore;
    }

    public double getConnectednessScore() {
        return connectednessScore;
    }

    public void setConnectednessScore(double connectednessScore) {
        this.connectednessScore = connectednessScore;
    }

    public double getFuzzyConnectedness() {
        return fuzzyConnectedness;
    }

    public void setFuzzyConnectedness(double fuzzyConnectedness) {
        this.fuzzyConnectedness = fuzzyConnectedness;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double completionTime) {
        this.time = completionTime;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getAggregateScore() {
        return aggregateScore;
    }

    public void setAggregateScore(double aggregateScore) {
        this.aggregateScore = aggregateScore;
    }

    public double getNormTime() {
        return normTime;
    }

    public void setNormTime(double normTime) {
        this.normTime = normTime;
    }

    public double getNormCost() {
        return normCost;
    }

    public void setNormCost(double normCost) {
        this.normCost = normCost;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Solution replace(int index, SolutionComponent comp) {
        if (index<list.size()) {
            // arraylist is immutable, so just create a new one
            ArrayList<SolutionComponent> newList = new ArrayList<SolutionComponent>();
            for (int i=0; i<list.size(); i++) {
                if (i==index) newList.add(comp);
                else newList.add(list.get(i));
            }
            list = newList;
            Solution newSolution = new Solution();
            newSolution.setList(newList);
            return newSolution;
        }
        return null;
    }

    @Override
    public String toString() {
        return String
                .format(
                        "[%s, aggr=%.3f, cost=%.3f, nCost=%.3f, cmptcy=%s, connctd=%s, muConnctd=%s, time=%s, nTime=%s]",
                        getList().toString(), aggregateScore, cost, normCost, skillScore, connectednessScore, fuzzyConnectedness, time, normTime);
    }

    public String trace() {
        // "[comp],[comp]",aggr,cost,normcost,compentency,conn,muconn,rt,normrt
        String comps = "";
        for (SolutionComponent c: list) {
            if (!comps.equals("")) comps += ",";
            comps += "[" + c.trace() + "]";
        }
        return String
                .format(
                        "\"%s\",%s,%s,%s,%s,%s,%s,%s,%s",
                        comps, aggregateScore, cost, normCost, skillScore, connectednessScore, fuzzyConnectedness, time, normTime);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Solution other = (Solution) obj;
        if (list == null) {
            if (other.list != null)
                return false;
        } else if (!list.equals(other.list))
            return false;
        return true;
    }


}
