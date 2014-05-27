package scu.composer.algorithm;

import scu.composer.Composer;
import scu.composer.model.ConstructionGraph;
import scu.composer.model.Solution;

public interface ComposerAlgorithmInterface {
    public void init(String configFile, ConstructionGraph cons, Composer composer);
    public Solution solve();
}
