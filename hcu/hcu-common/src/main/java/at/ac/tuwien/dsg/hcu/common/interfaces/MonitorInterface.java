package at.ac.tuwien.dsg.hcu.common.interfaces;

import java.util.Map;

public interface MonitorInterface {

	public void sendEvent(Object obj);
	public RuleEngineInterface getRuleEngine();
	
	public void setConfiguration(Map<String, Object> config);
}
