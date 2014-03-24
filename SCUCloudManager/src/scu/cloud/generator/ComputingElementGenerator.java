package scu.cloud.generator;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.MersenneTwister;

import scu.common.model.ComputingElement;
import scu.common.model.HumanComputingElement;
import scu.common.model.PropertyTypes;
import scu.util.Config;
import scu.util.Util;

public class ComputingElementGenerator {

    private Logger logger = Logger.getLogger("Generator");

    // reusable generator
    private UniformIntegerDistribution distAvail = null;
    private UniformIntegerDistribution distUnavail = null;
    private UniformRealDistribution distStartState = null;
    private Config config = null;
    
    public ComputingElementGenerator(Config config) {
        this.config = config;
    }
    
    // TODO: generate mix computing elements
    
    public ArrayList<ComputingElement> generateHumanComputingElement() {
        ArrayList<ComputingElement> list = new ArrayList<ComputingElement>();

        // get configs
        int seed = Integer.parseInt(config.get("seed"));
        int nSkillType = Integer.parseInt(config.get("number_of_skill_types"));
        double nSkillAvg = Double.parseDouble(config.get("average_number_of_skills_per_elements"));
        double nSkillSd = Double.parseDouble(config.get("sd_number_of_skills_per_worker"));
        double nScoreAvg = Double.parseDouble(config.get("average_skills_value"));
        double nScoreSd = Double.parseDouble(config.get("sd_skills_value"));
        double costAvg = Double.parseDouble(config.get("average_cost"));
        double costSd = Double.parseDouble(config.get("sd_cost"));
        double pConnect = Double.parseDouble(config.get("probability_of_connectivity"));
        double weightAvg = Double.parseDouble(config.get("average_connection_weight"));
        double weightSd = Double.parseDouble(config.get("sd_connection_weight"));

        // generate elements
        int nElements = Integer.parseInt(config.get("number_of_elements"));
        logger.info("Generating " + nElements + " elements.");
        for (long i=1; i<=nElements; i++) {
            list.add(new HumanComputingElement(i));
        }

        // initiate the distribution for the types of skills assigned to each element
        UniformIntegerDistribution distQualType = new UniformIntegerDistribution(
                new MersenneTwister(seed), 1, nSkillType);      
        // initiate the distribution for the number of skills assigned to each element
        NormalDistribution distQual = new NormalDistribution(new MersenneTwister(seed), 
                nSkillAvg, nSkillSd, NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
        // initiate the distribution for the value of skills assigned to each element
        NormalDistribution distQualValue = new NormalDistribution(new MersenneTwister(seed), 
                nScoreAvg, nScoreSd, NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
        // populating elements' skill
        logger.info("Generating elements' skills (navg="+nSkillAvg+",scoreavg="+nScoreAvg+").");
        for (ComputingElement e : list) {
            HumanComputingElement element = (HumanComputingElement)e;
            long nQual = Math.round(distQual.sample());
            for (int j=0; j<nQual; j++) {
                int qualType = distQualType.sample();
                double value = distQualValue.sample();
                if (value<0.0) value = 0.0;
                else if (value>1.0) value = 1.0;
                element.getSkills().setValue(Integer.toString(qualType), value);
            }
        }

        // initiate the distribution for the element cost
        NormalDistribution distCost = new NormalDistribution(new MersenneTwister(seed), 
                costAvg, costSd, NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
        // populating elements' cost
        logger.info("Generating elements' cost (avg="+costAvg+").");
        for (ComputingElement e : list) {
            HumanComputingElement element = (HumanComputingElement)e;
            element.getProperties().setValue(PropertyTypes.PROP_COST, distCost.sample());
        }
        
        // TODO: generate performance rating and timezone

        // initiate the distribution for the element relation
        UniformRealDistribution distConnect = new UniformRealDistribution(new MersenneTwister(seed), 
                0, 1);      
        NormalDistribution distWeight = new NormalDistribution(new MersenneTwister(seed), 
                weightAvg, weightSd, NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);      
        // populating elements' relation, this is undirected and no loop. so, we always use edge (i,j) where i<j
        logger.info("Generating workers' relation (pconnect="+pConnect+",weightavg="+weightAvg+").");
        for (int i=0; i<nElements; i++) {
            ComputingElement e = list.get(i); 
            HumanComputingElement element = (HumanComputingElement)e;
            for (int j=i; j<nElements; j++) {
                if (distConnect.sample()<=pConnect) {
                    double weight = distWeight.sample();
                    element.setConnection(list.get(j), weight);
                }
            }
        }

        logger.info("Generating elements' availability");
        for (ComputingElement e : list) {
            HumanComputingElement element = (HumanComputingElement)e;
            String block = generateAvailability(30, "");
            element.getQueue().extendSequence(block);
        }

        logger.info("Human computing element generation DONE.");

        return list;
    }

    public String generateAvailability(int minLength, 
            String currentQueue) {

        String result = "";

        // get configs
        int seed = Integer.parseInt(config.get("seed"));
        int minAvail = Integer.parseInt(config.get("min_avail_block"));
        int maxAvail = Integer.parseInt(config.get("max_avail_block"));
        int minUnavail = Integer.parseInt(config.get("min_unavail_block"));
        int maxUnavail = Integer.parseInt(config.get("max_unavail_block"));

        if (distAvail==null) {
            distAvail = new UniformIntegerDistribution(new MersenneTwister(seed), 
                    minAvail, maxAvail); 
            distUnavail = new UniformIntegerDistribution(new MersenneTwister(seed), 
                    minUnavail, maxUnavail); 
            distStartState = new UniformRealDistribution(new MersenneTwister(seed), 0, 1);          
        }

        // get next state
        char lastState;
        if (currentQueue.length()>0) {
            lastState = currentQueue.charAt(currentQueue.length()-1);
        } else {
            lastState = Double.toString(Math.round(distStartState.sample())).charAt(0);
        }
        int nextState = 1;
        if (lastState=='1' || lastState=='2') nextState = 0;

        int nextLength = 0; 
        while (result.length()<minLength) {
            if (nextState==1) {
                nextLength = distAvail.sample();
            } else {
                nextLength = distUnavail.sample();
            }
            String block = Util.stringRepeat(Integer.toString(nextState), nextLength);
            nextState = (nextState+1) % 2;
            result += block;
        }

        return result;
    }
}
