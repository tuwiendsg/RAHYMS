package scu.cloud.generator;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.json.JSONObject;

import scu.util.ConfigJson;
import scu.util.Util;

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

        // get next state
        char lastState;
        if (current.length()>0) {
            lastState = current.charAt(current.length()-1);
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
