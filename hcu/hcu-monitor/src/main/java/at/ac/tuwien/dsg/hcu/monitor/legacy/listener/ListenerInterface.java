package at.ac.tuwien.dsg.hcu.monitor.legacy.listener;

import at.ac.tuwien.dsg.hcu.common.interfaces.MonitorInterface;

import com.espertech.esper.client.EPServiceProvider;

public interface ListenerInterface {

	public void initiate(EPServiceProvider epService, MonitorInterface monitor);
	public void terminate();
	
}
