package com.csc.fsg.nba.reinsuranceadapter;

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
import java.util.Map;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaPollingException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.provideradapter.NbaProviderAdapter;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.Reinsurer;

/**
 * NbaReinsuranceAdapterFacade provides a common entry point into the insurer interface
 * in order to faciliate the ordering bids and receiving offers from different
 * reinsurers.
 * <p>When a method needs to interface with a reinsurer (for ordering bid or receiving
 * offer), it will create a new NbaReinsuranceAdapterFacade and use that to invoke
 * the appropriate adapter method. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA038</td><td>Version 3</td><td>Reinsurance</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACN009</td><td>Version 4</td><td>ACORD 401/402 MIB Inquiry and Update Migration</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see NbaProviderAdapter
 * @since New Business Accelerator - Version 3
 */
public class NbaReinsuranceAdapterFacade extends NbaReinsuranceAdapter {
	protected static NbaLogger logger;
	protected com.csc.fsg.nba.vo.NbaDst work;
	protected Reinsurer configRien; //ACN012
	protected NbaUserVO user; //AXAL3.7.32
	/**
	 * NbaReinsuranceAdapterFacade constructor uses the <code>NbaDst</code> work item to
	 * set the work object to be used for the process.
	 * @param newWork an <code>NbaDst</code> object containing the reinsurance and it's
	 * associated information.
	 * @param user an <code>NbaUserVO</code> value object containing user information 
	 */
	public NbaReinsuranceAdapterFacade(NbaDst newWork, NbaUserVO user) throws NbaBaseException {
		super();
		setWork(newWork);
		setUser(user);//AXAL3.7.32
	}
	/**
	 * This method converts the XML 552 transactions into a format
	 * that is understandable by the reinsurer.
	 * @param txLife the 552 XML transaction
	 * @param user the user value object
	 * @return a reinsurer ready message in a HashMap which includes any errors that might have occurred.
	 * @exception NbaBaseException thrown if an error occurs.
	 */
	public Map convertXmlToReinsurerFormat(NbaTXLife txLife, NbaUserVO user) throws NbaBaseException {
		String reinsurerID = getWork().getNbaLob().getReinVendorID();
		configRien = NbaConfiguration.getInstance().getReinsurer(reinsurerID);
		NbaReinsuranceAdapter adapter;
		try {
			adapter = (NbaReinsuranceAdapter) NbaUtils.classForName(configRien.getNbaAdapter()).newInstance();
		} catch (InstantiationException e) {
			throw new NbaBaseException(NbaPollingException.CLASS_INVALID,e);
		} catch (IllegalAccessException e) {
			throw new NbaBaseException(NbaPollingException.CLASS_ILLEGAL_ACCESS,e);
		} catch (ClassNotFoundException e) {
			throw new NbaBaseException(NbaPollingException.CLASS_NOT_FOUND,e);
		}
		return adapter.convertXmlToReinsurerFormat(txLife, user,getWork());
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaReinsuranceAdapterFacade.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaReinsuranceAdapterFacade could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * Answers the work item.
	 * @return NbaDst
	 */
	public NbaDst getWork() {
		return work;
	}
	/**
	 * This method converts the reinsurer response into XML transaction.It 
	 * also updates required LOBs and result source with converted XMLife.
	 * @param work the reinsurance work item.
	 * @param user the user value object
	 * @return the requirement work item with formated source.
	 * @exception NbaBaseException thrown if an error occurs.
	 */
	public NbaDst processResponseFromReinsurer(NbaDst work, NbaUserVO user) throws NbaBaseException {
		String reinsurerID = getWork().getNbaLob().getReinVendorID();
		configRien = NbaConfiguration.getInstance().getReinsurer(reinsurerID);
		NbaReinsuranceAdapter adapter;
		try {
			adapter = (NbaReinsuranceAdapter) NbaUtils.classForName(configRien.getNbaAdapter()).newInstance();
		} catch (InstantiationException e) {
			getLogger().logError("Error in instantiating the ReinsuranceAdapter class : " + configRien.getNbaAdapter());//AXAL3.7.32
			throw new NbaBaseException(NbaPollingException.CLASS_INVALID,e);
		} catch (IllegalAccessException e) {
			throw new NbaBaseException(NbaPollingException.CLASS_ILLEGAL_ACCESS,e);
		} catch (ClassNotFoundException e) {
			getLogger().logError("ReinsuranceAdapter class not found: " + configRien.getNbaAdapter());//AXAL3.7.32
			throw new NbaBaseException(NbaPollingException.CLASS_NOT_FOUND,e);
		}
		return adapter.processResponseFromReinsurer(work, user);
	}
	/**
	 * This method provides the means by which a message representing a request for 
	 * a requirement is submitted to the provider.<p>
	 * The means of communication varies and may include many different methods:
	 * HTTP Post, writing the message to a folder on a server, or others.  The
	 * <code>NbaConfigReinsurer</code> that contains information on how to communicate
	 * with the reinsurer is passed to the <code>NbaProviderCommunicator</code> object. The
	 * <code>NbaProviderCommunicator</code> then sends the message to the provider.
	 * @param aTarget the destinatin of the message
	 * @param message the message to be sent to the provider
	 * @return an Object that must be evaluated by the calling process
	 */
	//AXAL3.7.32 Modified the implementation
	public Object sendMessageToProvider(String aTarget, Object aMessage) throws NbaBaseException {
		String reinsurerID = getWork().getNbaLob().getReinVendorID();
		configRien = NbaConfiguration.getInstance().getReinsurer(reinsurerID);
		NbaReinsuranceAdapter adapter;
		try {
			adapter = (NbaReinsuranceAdapter) NbaUtils.classForName(configRien.getNbaAdapter()).newInstance();
		} catch (InstantiationException e) {
			throw new NbaBaseException(NbaPollingException.CLASS_INVALID,e);
		} catch (IllegalAccessException e) {
			throw new NbaBaseException(NbaPollingException.CLASS_ILLEGAL_ACCESS,e);
		} catch (ClassNotFoundException e) {
			throw new NbaBaseException(NbaPollingException.CLASS_NOT_FOUND,e);
		}
		return adapter.sendMessageToProvider(aTarget, aMessage, getUser());
	}
	/**
	 * Sets work item.
	 * @param newWork
	 */
	private void setWork(com.csc.fsg.nba.vo.NbaDst newWork) {
		work = newWork;
	}
	/**
	 * Answer NbaConfigReinsurer
	 * @return NbaConfigReinsurer 
	 */
	//ACN012 CHANGED SIGNATURE
	public Reinsurer getConfigRien() {
		return configRien;
	}

	/**
	 * Set NbaConfigReinsurer
	 * @param reinsurer
	 */
	//ACN012 CHANGED SIGNATURE
	public void setConfigRien(Reinsurer reinsurer) {
		configRien = reinsurer;
	}

	/**
	 * @return Returns the user.
	 */
	//AXAL3.7.32 New Method
	public NbaUserVO getUser() {
		return user;
	}
	/**
	 * @param user The user to set.
	 */
	//AXAL3.7.32 New Method
	public void setUser(NbaUserVO user) {
		this.user = user;
	}
}
