package at.ac.tuwien.dsg.hcu.composer.algorithm;

import at.ac.tuwien.dsg.hcu.composer.Composer;
import at.ac.tuwien.dsg.hcu.composer.model.ConstructionGraph;
import at.ac.tuwien.dsg.hcu.composer.model.Solution;

public interface ComposerAlgorithmInterface {
    public void init(String configFile, ConstructionGraph cons, Composer composer);
    public Solution solve();
}
