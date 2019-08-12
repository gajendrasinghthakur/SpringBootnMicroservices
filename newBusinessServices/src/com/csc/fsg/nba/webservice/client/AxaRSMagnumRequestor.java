package com.csc.fsg.nba.webservice.client;

import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;

public class AxaRSMagnumRequestor extends AxaRSWebServiceRequestor {
	protected NbaLogger getLogger() {
		NbaLogger logger = null;
		try {
			logger = NbaLogFactory.getLogger(this.getClass().getName());
		} catch (Exception e) {
			NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
			e.printStackTrace(System.out);
		}

		return logger;
	}
}
