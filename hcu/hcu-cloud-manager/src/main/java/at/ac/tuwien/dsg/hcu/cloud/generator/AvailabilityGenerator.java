package at.ac.tuwien.dsg.hcu.cloud.generator;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.json.JSONObject;

import at.ac.tuwien.dsg.hcu.cloud.metric.AvailabilityMetric;
import at.ac.tuwien.dsg.hcu.util.ConfigJson;
import at.ac.tuwien.dsg.hcu.util.Util;

public class AvailabilityGenerator {

    private JSONObject config = null;

    // reusable generator
    private static UniformIntegerDistribution distAvail = null;
    private static UniformIntegerDistribution distUnavail = null;
    private static UniformRealDistribution distStartState = null;

    public AvailabilityGenerator(ConfigJson config) {
        this.config = config.getRoot().getJSONObject("availability"); 
    }

    // simulate virtual availability
    public String generate(long minLength, String current) {

        String result = "";

        if (distAvail==null) {
            // get param
            int minAvail = config.getInt("minAvailBlock");
            int maxAvail = config.getInt("maxAvailBlock");
            int minUnavail = config.getInt("minUnavailBlock");
            int maxUnavail = config.getInt("maxUnavailBlock");
            int seed = config.getInt("seed");
            distAvail = new UniformIntegerDistribution(new MersenneTwister(seed++), 
                    minAvail, maxAvail); 
            distUnavail = new UniformIntegerDistribution(new MersenneTwister(seed++), 
                    minUnavail, maxUnavail+10); 
            distStartState = new UniformRealDistribution(new MersenneTwister(seed++), 0, 1);          
        }

        // get next type
        int lastState;
        if (current.length()>0) {
            lastState = Character.getNumericValue(current.charAt(current.length()-1));
        } else {
            lastState = Character.getNumericValue(Double.toString(Math.round(distStartState.sample())).charAt(0));
        }
        int nextState = AvailabilityMetric.AVAILABLE;
        if (lastState==AvailabilityMetric.AVAILABLE || lastState==AvailabilityMetric.BUSY) 
            nextState = AvailabilityMetric.NOT_AVAILABLE;

        int nextLength = 0; 
        while (result.length()<minLength) {
            if (nextState==AvailabilityMetric.AVAILABLE) {
                nextLength = distAvail.sample();
            } else {
                nextLength = distUnavail.sample();
            }
            String block = Util.stringRepeat(Integer.toString(nextState), nextLength);
            result += block;
            if (nextState==AvailabilityMetric.AVAILABLE) {
                nextState = AvailabilityMetric.NOT_AVAILABLE;
            } else {
                nextState = AvailabilityMetric.AVAILABLE;
            }
        }

        return result;
    }
}
