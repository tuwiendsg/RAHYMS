package scu.cloud.generator;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.random.MersenneTwister;

import scu.common.model.Task;
import scu.common.sla.Objective;
import scu.util.Config;
import scu.util.Util;

public class TaskGenerator {

    // reusable random number generator
    private static NormalDistribution distRoleNumber = null;
    private static NormalDistribution distSkillNumber = null;
    private static UniformIntegerDistribution distSkillType = null;
    private static UniformIntegerDistribution distQuality = null;
    private static NormalDistribution distDuration = null;
    private static UniformIntegerDistribution distSubmissionTime = null;
    private static NormalDistribution distCost = null;

    private Logger logger = Logger.getLogger("Generator");
    private Config config = null;

    public TaskGenerator(Config config) {
        this.config = config;
    }

    public static ArrayList<Task> generate(String title, String type) {

      initRandomGenerator();
      
      // create task
      Task t = new Task();
      t.setTitle(title);
      t.setType(type);
      
      int nJob = (int)Math.round(distRoleNumber.sample());
      int maxDuration = 0;
      if (nJob<=0) nJob = 1;
      for (int i=0; i<nJob; i++) {
        FuzzyCompetence competence = new FuzzyCompetence();
        int nApt = (int)Math.round(distSkillNumber.sample());
        if (nApt<=0) nApt = 1;
        for (int j=0; j<nApt; j++) {
          int skillType = distSkillType.sample();
          int skillQuality = distQuality.sample();
          competence.getAptitudeSet().add(new FuzzyAptitude(Integer.toString(skillType), intToQuality(skillQuality)));
        }
        int duration = (int)Math.round(distDuration.sample());
        if (duration <= 0) duration = 1; 
        t.getJobSet().add(new Job("J" + i, competence, duration));
        if (duration>maxDuration) maxDuration = duration;
      }
      int submissionTime = distSubmissionTime.sample();
      int connectednessQuality = distQuality.sample();
      double cost = distCost.sample();
      if (cost<=1) cost = 1; 
      cost = cost * nJob;
      int deadline = submissionTime + (maxDuration * 3);
      TaskRequest request = new TaskRequest(t, submissionTime, deadline, intToQuality(connectednessQuality), cost, new Objective(1, 1, 1, 1));
      return request;
    }
    
    public static TaskRequest generate(String title, String type, int jobSize, int aptSize, int duration) {

      initRandomGenerator();
      
      // create task
      Task t = new Task();
      t.setTitle(title);
      t.setType(type);
      
      int nJob = jobSize;
      int maxDuration = 0;
      if (nJob<=0) nJob = 1;
      for (int i=0; i<nJob; i++) {
        FuzzyCompetence competence = new FuzzyCompetence();
        int nApt = aptSize;
        if (nApt<=0) nApt = 1;
        for (int j=0; j<nApt; j++) {
          int skillType = distSkillType.sample();
          int skillQuality = distQuality.sample();
          competence.getAptitudeSet().add(new FuzzyAptitude(Integer.toString(skillType), intToQuality(skillQuality)));
        }
        if (duration <= 0) duration = (int)Math.round(distDuration.sample()); 
        if (duration <= 0) duration = 1; 
        t.getJobSet().add(new Job("J" + i, competence, duration));
        if (duration>maxDuration) maxDuration = duration;
      }
      int submissionTime = distSubmissionTime.sample();
      int connectednessQuality = distQuality.sample();
      double cost = distCost.sample();
      if (cost<=1) cost = 1; 
      cost = cost * nJob;
      int deadline = submissionTime + (maxDuration * 3);
      TaskRequest request = new TaskRequest(t, submissionTime, deadline, intToQuality(connectednessQuality), cost, new Objective(1, 1, 1, 1));
      return request;
    }

    public static void resetRandomGenerator() {
      double nJobAvg = Double.parseDouble(Util.getProperty(PROP_FILE, "average_number_of_job_per_task"));
      double nJobSd = Double.parseDouble(Util.getProperty(PROP_FILE, "sd_number_of_job_per_task"));
      distRoleNumber = new NormalDistribution(new MersenneTwister(SEED_JOB_NUMBER), 
          nJobAvg, nJobSd, NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

      double nAptAvg = Double.parseDouble(Util.getProperty(PROP_FILE, "average_number_of_skill_per_job"));
      double nAptSd = Double.parseDouble(Util.getProperty(PROP_FILE, "sd_number_of_skill_per_job"));
      distSkillNumber = new NormalDistribution(new MersenneTwister(SEED_APTITUDE_NUMBER), 
          nAptAvg, nAptSd, NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

      int nAptituteType = Integer.parseInt(Util.getProperty(PROP_FILE, "number_of_skill_types"));
      distSkillType = new UniformIntegerDistribution(new MersenneTwister(SEED_APTITUDE_TYPE), 
          1, nAptituteType); 

      distQuality = new UniformIntegerDistribution(new MersenneTwister(SEED_QUALITY), 
          1, 4);
      
      double durationAvg = Double.parseDouble(Util.getProperty(PROP_FILE, "average_duration"));
      double durationSd = Double.parseDouble(Util.getProperty(PROP_FILE, "sd_duration"));
      distDuration = new NormalDistribution(new MersenneTwister(SEED_DURATION), 
          durationAvg, durationSd, NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);

      int maxSubmissionTime = Integer.parseInt(Util.getProperty(PROP_FILE, "max_submission_time"));
      distSubmissionTime = new UniformIntegerDistribution(new MersenneTwister(SEED_SUBMISSION_TIME), 
          1, maxSubmissionTime); 

      double costAvg = Double.parseDouble(Util.getProperty(PROP_FILE, "average_cost_per_job"));
      double costSd = Double.parseDouble(Util.getProperty(PROP_FILE, "sd_cost_per_job"));
      distCost = new NormalDistribution(new MersenneTwister(SEED_COST), 
          costAvg, costSd, NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
      
    }

    public static void initRandomGenerator() {
      if (distRoleNumber==null) {
        resetRandomGenerator();
      }
    }
    
    public static Quality intToQuality(int x) {
      if (x==1) return Quality.POOR;
      else if (x==2) return Quality.FAIR;
      else if (x==3) return Quality.GOOD;
      else return Quality.VERY_GOOD;
    }
    
    public static TaskRequest createBuggyTaskRequest() {
      Task t = new Task();
      t.setTitle("Buggy task");
      t.setType("T1");

      // job 1
      FuzzyCompetence competence = new FuzzyCompetence();
      competence.getAptitudeSet().add(new FuzzyAptitude("5", Quality.GOOD));
      competence.getAptitudeSet().add(new FuzzyAptitude("7", Quality.VERY_GOOD));
      competence.getAptitudeSet().add(new FuzzyAptitude("7", Quality.GOOD));
      competence.getAptitudeSet().add(new FuzzyAptitude("9", Quality.GOOD));
      t.getJobSet().add(new Job("J1", competence, 5));
      
      // job 2
      competence = new FuzzyCompetence();
      competence.getAptitudeSet().add(new FuzzyAptitude("8", Quality.GOOD));
      competence.getAptitudeSet().add(new FuzzyAptitude("8", Quality.POOR));
      competence.getAptitudeSet().add(new FuzzyAptitude("8", Quality.POOR));
      competence.getAptitudeSet().add(new FuzzyAptitude("9", Quality.GOOD));
      t.getJobSet().add(new Job("J2", competence, 7));

      // job 3
      competence = new FuzzyCompetence();
      competence.getAptitudeSet().add(new FuzzyAptitude("6", Quality.POOR));
      competence.getAptitudeSet().add(new FuzzyAptitude("4", Quality.POOR));
      competence.getAptitudeSet().add(new FuzzyAptitude("7", Quality.POOR));
      t.getJobSet().add(new Job("J3", competence, 8));
    
      TaskRequest request = new TaskRequest(t, 56, 80, Quality.VERY_GOOD, 25.37110807953035, new Objective(1, 1, 1, 1));
      
      return request;
    }
    

}
