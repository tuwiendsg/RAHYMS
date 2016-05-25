package at.ac.tuwien.dsg.hcu.monitor.legacy;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.compiler.PackageBuilder;
import org.drools.lang.descr.PackageDescr;
import org.drools.rule.Package;

import at.ac.tuwien.dsg.hcu.common.interfaces.RuleEngineInterface;

@SuppressWarnings("restriction")
public class LegacyRuleEngine extends Thread implements RuleEngineInterface {

    private WorkingMemory workingMemory;
    private RuleBase ruleBase;
    private String ruleFile;
    private ArrayList<Object> facts;
    private boolean terminated = false; 

    public LegacyRuleEngine(String ruleFile) {
        try {
            this.ruleFile = ruleFile;
            facts = new ArrayList<Object>();
            initRuleBase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static RuleBase readRule(String ruleFile) throws Exception {
        //read in the source
        Reader source = new InputStreamReader( new FileInputStream(ruleFile) );

        //Use package builder to build up a rule package.
        PackageBuilder builder = new PackageBuilder();
        PackageDescr packageDescr = new PackageDescr("at.ac.tuwien.dsg.hcu.monitor");
        builder.addPackage(packageDescr);
        //this will parse and compile in one step
        builder.addPackageFromDrl(source);

        //get the compiled package (which is serializable)
        Package pkg = builder.getPackage();
        //add the package to a rulebase (deploy the rule package).
        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage( pkg );
        return ruleBase;
    }

    private void initRuleBase() throws Exception {
        ruleBase = readRule(ruleFile);
        //workingMemory = ruleBase.newStatefulSession();
    }

    public synchronized void removeFact(Object obj) {
        synchronized (facts) {
            facts.remove(obj);
        }
        notifyAll();
    }

    public synchronized void insertFact(Object obj) {
        synchronized (facts) {
            facts.add(obj);
        }
        notifyAll();
    }

    public synchronized void fireAllRules() {
        //System.out.println("Facts: " + printAllFacts());
        workingMemory = ruleBase.newStatefulSession();
        for (int i=0; i<facts.size(); i++) {
            workingMemory.insert(facts.get(i));
        }
        workingMemory.fireAllRules();
        facts.clear();
        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public synchronized void terminate() {
        terminated = true;
        notifyAll();
    }

    public void run() {
        while (!terminated) {
            fireAllRules();
        }
    }

    public String toString() {
        return "RE";
    }

    public String printAllFacts() {
        String result = "";
        for (Object fact: facts) {
            result += fact + "\n";
        }
        return result;
    }

}
