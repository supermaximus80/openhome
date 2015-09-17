package com.icontrol.openhomesimulator.gateway;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


public class GwServletContextListener implements ServletContextListener {
	//ServletContext context;

	public void contextInitialized(ServletContextEvent contextEvent) {
		//context = contextEvent.getServletContext();
	}

	public void contextDestroyed(ServletContextEvent contextEvent) {
        GatewaySimulatorFactory.destroy();
	}
}
