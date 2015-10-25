package at.ac.tuwien.dsg.hcu.monitor.test;

import javax.script.ScriptException;

import at.ac.tuwien.dsg.hcu.util.Util;

public class TestEval {

    public static void main(String[] args) {
        try {
            System.out.println(Util.eval("%1 / 1000 + %2", 10000, 230));
        } catch (ScriptException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
