package com.csc.fsg.nba.contract.auditor;
/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group®.  The use,<BR>
 * reproduction, distribution or disclosure of this program, in whole or in<BR>
 * part, without the express written permission of CSC Financial Services<BR>
 * Group is prohibited.  This program is also an unpublished work protected<BR>
 * under the copyright laws of the United States of America and other<BR>
 * countries.  If this program becomes published, the following notice shall<BR>
 * apply:
 *     Property of Computer Sciences Corporation.<BR>
 *     Confidential. Not for publication.<BR>
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */
import com.csc.fsg.nba.exception.NbaAuditorException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaAuditor;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaConfiguration;

/**
 * This is Factory class to instantiate NbaAuditor implementation
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA102</td><td>Version 5</td><td>nbA Transaction Logging Project</td><tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 5
 */
public class NbaAuditorFactory {

	private static NbaAuditorFactory instance = null;
	private static NbaLogger logger = null;
	
	/**
	 * NbaAuditorFactory constructor with access as private as
	 * the class being a singleton
	 */
	private NbaAuditorFactory() {
		// do nothing		   
	}
	
	/**
	 *  Returns the instance of NbaAuditorFactory
	 */
	public static NbaAuditorFactory getInstance() {
		if(instance == null) {
			instance = 	new NbaAuditorFactory ();
		}
		return instance;
	}

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaAuditorFactory.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaServiceLocator could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

	/**
	 * Returns instance of NbaAuditor implementation, 
	 * @throws  NbaAuditorException, if unable to instantiate NbaAuditor implementation configured
	 */
	public NbaAuditor getAuditor() throws NbaAuditorException {
		NbaAuditor auditor = null;
		try {
			String implClass = NbaConfiguration.getInstance().getAuditConfiguration().getImplClass();
			auditor = (NbaAuditor)Class.forName(implClass).newInstance();
		} catch (NbaBaseException e) {
			getLogger().logException(e);
			throw new NbaAuditorException("Unable to instantiate NbaAuditor" ,e);
		} catch (InstantiationException e) {
			getLogger().logException(e);
			throw new NbaAuditorException("Unable to instantiate NbaAuditor" ,e);
		} catch (IllegalAccessException e) {
			getLogger().logException(e);
			throw new NbaAuditorException("Unable to instantiate NbaAuditor" ,e);
		} catch (ClassNotFoundException e) {
			getLogger().logException(e);
			throw new NbaAuditorException("Unable to instantiate NbaAuditor" ,e);
		}
		return auditor;
	}
}
