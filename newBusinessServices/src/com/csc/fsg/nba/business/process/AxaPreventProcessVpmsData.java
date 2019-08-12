package com.csc.fsg.nba.business.process;

/*
 * ************************************************************** <BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group?.  The use,<BR>
 * reproduction, distribution or disclosure of this program, in whole or in<BR>
 * part, without the express written permission of CSC Financial Services<BR>
 * Group is prohibited.  This program is also an unpublished work protected<BR>
 * under the copyright laws of the United States of America and other<BR>
 * countries.  If this program becomes published, the following notice shall<BR>
 * apply:
 *     Property of Computer Sciences Corporation.<BR>
 *     Confidential. Not for publication.<BR>
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.StandardAttr;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * The NbaProcessStatusProvider services the <code>NbaAutomatedProcess<code> by calling
 * VPMS to retrieve the statuses for a given process.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *   <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td><tr>
 * <tr><td>SPR1018</td><td>Version 2</td><td>JavaDoc, comments and minor source code changes.</td></tr>
 * <tr><td>SPR1050</td><td>Version 2</td><td>Add contract error status</td></tr>
 * <tr><td>NBA021</td><td>Version 2</td><td>Data Resolver</td></tr>
 * <tr><td>NBA004</td><td>Version 2</td><td>VP/MS Model Support for Work Items Project</td></tr> 
 * <tr><td>NBA020</td><td>Version 2</td><td>AWD Priority</td></tr>
 * <tr><td>NBA058</td><td>Version 3</td><td>Upgrade to J-VPMS version 1.5.0</td></tr>
 * <tr><td>NBA035</td><td>Version 3</td><td>App submit to Pending DB</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA088</td><td>Version 3</td><td>Underwriting Risk</td></tr>
 * <tr><td>NBA068</td><td>Version 3</td><td>Inforce Payment</td></tr>
 * <tr><td>SPR1715</td><td>Version 4</td><td>Wrappered/Standalone Mode Should Be By BackEnd System and by Plan</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr> 
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>ACN026</td><td>Version 5</td><td>Receiving Different Requirement And Electronic Unsolicited Mail</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirement/Reinsurance Changes</td></tr>
 * <tr><td>NBA192</td><td>Version 7</td><td>Requirement Management Enhancement</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 * @see NbaAutomatedProcess
 */
public class AxaPreventProcessVpmsData{
	protected boolean preventsProcess;
	protected java.lang.String nextOpt;
	protected java.lang.String comments;
	protected java.lang.String suspendtime;
	protected java.lang.String maxSuspendTime;
	protected java.lang.String suspendUnit;
	protected java.util.Date suspendActivationDate;
	protected java.lang.String modelName;
	protected java.lang.String modelEntryPoint;
	private static final String ATTR_NAME_PREVENT_PROCESSING = "PreventProcess";
	private static final String ATTR_NAME_NEXT_OPERATION = "NextOperation";
	private static final String ATTR_NAME_COMMENTS = "Comments";
	private static final String ATTR_NAME_SUSPEND_TIME = "SuspendTime";
	private static final String ATTR_NAME_MAX_SUSPEND_TIME = "MaxSuspendTime";
	private static final String ATTR_NAME_SUSPEND_UNIT = "SuspendUnit";
	private static final String ATTR_NAME_SUSPEND_ACTIVATION_DATE = "SuspendActivationDate";
	private static final String OPT_ROUTE = "ROUTE";
	private static final String OPT_SUSPEND = "SUSPEND";
	protected Map deOinkMap; 

	/**
	 * @return Returns the modelEntryPoint.
	 */
	public java.lang.String getModelEntryPoint() {
		return modelEntryPoint;
	}

	/**
	 * @param modelEntryPoint The modelEntryPoint to set.
	 */
	public void setModelEntryPoint(java.lang.String modelEntryPoint) {
		this.modelEntryPoint = modelEntryPoint;
	}

	/**
	 * @return Returns the modelName.
	 */
	public java.lang.String getModelName() {
		return modelName;
	}

	/**
	 * @param modelName The modelName to set.
	 */
	public void setModelName(java.lang.String modelName) {
		this.modelName = modelName;
	}

	/**
	 * Using the user and work objects, it calls the <code>initializeStatusFields</code>
	 * method to set the statuses for the process.
	 * @param user  the user for whom the work was retrieved
	 * @param work  the AWD case to be processed
	 * @param NbabTXLife Object
	 * @throws NbaBaseException
	 */
	public AxaPreventProcessVpmsData(NbaUserVO user, NbaTXLife nbaTXLife, String vpmsModel) throws NbaBaseException {
		super();
		setModelName(vpmsModel);
		initializeFields(user, nbaTXLife);
	}

	/**
	 * This methods determines the statuses to be used by a process based on
	 * the user ID and values from the work items. (The user ID becomes the 
	 * process ID used by VPMS to determine the LOB fields and statuses for 
	 * a process.
	 * <P>After instantiating an <code>NbaVpmsAdaptor</code> object, VPMS is called
	 * to get the statuses for the work item. The resulting values are used to populate
	 * the member variables in the <code>updateProcessStatus</code> method.
	 * @param newUser the process ID 
	 * @param newWork the NbaDst value object to be processed
	 * @param NbaTXLife Object
	 * @param requirementInfo the Requirement Info obejct
	 * @throws NbaBaseException
	 */
	public void initializeFields(NbaUserVO user, NbaTXLife aNbaTXLife) throws NbaBaseException {
		NbaVpmsAdaptor vpmsProxy = null;
		try {
			NbaOinkDataAccess data = new NbaOinkDataAccess();
			if (aNbaTXLife != null) {
				data.setContractSource(aNbaTXLife);
			}
			//Removed Code NBLXA-2155[NBLXA-2350]
			vpmsProxy = new NbaVpmsAdaptor(data, getModelName());
			String busFunc = NbaUtils.getBusinessProcessId(user);
			getDeOinkMap().put(NbaVpmsConstants.A_PROCESS_ID, busFunc);
			//Removed Code NBLXA-2155[NBLXA-2350]
			vpmsProxy.setSkipAttributesMap(getDeOinkMap());
			vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_PREVENT_PROCESSING_XML);
			VpmsComputeResult result = vpmsProxy.getResults();
			updateVpmsResults(result);
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException(NbaBaseException.PREVENT_PROCESS_XML, re);
		} catch (Throwable t) {
			throw new NbaBaseException(NbaBaseException.PREVENT_PROCESS_XML, t);
		} finally {
			// begin SPR3362
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
				LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
			}
			// end SPR3362
		}
	}

	/**
	 * The results from the VPMS model are parsed and statuses are retrieved
	 * and used to update the various status field members.
	 * @param result the result from the VPMS call
	 */
	// NBA021 Changed signature to remove delimiter and use default delimiter
	public void updateVpmsResults(VpmsComputeResult result) throws NbaBaseException {
		if (!result.isError()) {
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(result);
			List rulesList = vpmsResultsData.getResultsData();
			if (!rulesList.isEmpty()) {
				String xmlString = (String) rulesList.get(0);
				NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
				VpmsModelResult vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
				List strAttrs = vpmsModelResult.getStandardAttr();
				StandardAttr attr = null;
				int i = 0;
				for (Iterator itr = strAttrs.iterator(); itr.hasNext(); i++) {
					attr = (StandardAttr) itr.next();
					if (ATTR_NAME_PREVENT_PROCESSING.equalsIgnoreCase(attr.getAttrName())
							&& NbaConstants.TRUE_STR.equalsIgnoreCase(attr.getAttrValue())) {
						setPreventsProcess(true);
					} else if (ATTR_NAME_COMMENTS.equalsIgnoreCase(attr.getAttrName())) {
						setComments(attr.getAttrValue());
					} else if (ATTR_NAME_NEXT_OPERATION.equalsIgnoreCase(attr.getAttrName())) {
						setNextOpt(attr.getAttrValue());
					} else if (ATTR_NAME_SUSPEND_TIME.equalsIgnoreCase(attr.getAttrName())) {
						setSuspendtime(attr.getAttrValue());
					} else if (ATTR_NAME_MAX_SUSPEND_TIME.equalsIgnoreCase(attr.getAttrName())) {
						setMaxSuspendTime(attr.getAttrValue());
					} else if (ATTR_NAME_SUSPEND_UNIT.equalsIgnoreCase(attr.getAttrName())) {
						setSuspendUnit(attr.getAttrValue());
					}else if (ATTR_NAME_SUSPEND_ACTIVATION_DATE.equalsIgnoreCase(attr.getAttrName())) {
						setSuspendActivationDate(NbaUtils.getDateFromStringInAWDFormat(attr.getAttrValue()));
					}
				}
			}
		}
	}

	/**
	 * Returns the deOinkMap.
	 * @return Map
	 */
	//NBA044 new method
	protected Map getDeOinkMap() {
		if (deOinkMap == null) {
			deOinkMap = new HashMap();
		}
		return deOinkMap;
	}

	/**
	 * Sets the deOinkMap.
	 * @param deOinkMap The deOinkMap to set
	 */
	//NBA044 new method	
	protected void setDeOinkMap(Map deOinkMap) {
		this.deOinkMap = deOinkMap;
	}

	/**
	 * @return Returns the maxSuspendTime.
	 */
	public java.lang.String getMaxSuspendTime() {
		return maxSuspendTime;
	}

	/**
	 * @param maxSuspendTime The maxSuspendTime to set.
	 */
	public void setMaxSuspendTime(java.lang.String maxSuspendTime) {
		this.maxSuspendTime = maxSuspendTime;
	}

	/**
	 * @return Returns the nextOpt.
	 */
	public java.lang.String getNextOpt() {
		return nextOpt;
	}

	/**
	 * @param nextOpt The nextOpt to set.
	 */
	public void setNextOpt(java.lang.String nextOpt) {
		this.nextOpt = nextOpt;
	}

	/**
	 * @return Returns the preventsProcess.
	 */
	public boolean isPreventsProcess() {
		return preventsProcess;
	}

	/**
	 * @param preventsProcess The preventsProcess to set.
	 */
	public void setPreventsProcess(boolean preventsProcess) {
		this.preventsProcess = preventsProcess;
	}

	/**
	 * @return Returns the suspendtime.
	 */
	public java.lang.String getSuspendtime() {
		return suspendtime;
	}

	/**
	 * @param suspendtime The suspendtime to set.
	 */
	public void setSuspendtime(java.lang.String suspendtime) {
		this.suspendtime = suspendtime;
	}

	/**
	 * @return Returns the suspendUnit.
	 */
	public java.lang.String getSuspendUnit() {
		return suspendUnit;
	}

	/**
	 * @param suspendUnit The suspendUnit to set.
	 */
	public void setSuspendUnit(java.lang.String suspendUnit) {
		this.suspendUnit = suspendUnit;
	}

	/**
	 * @return Returns the suspendUnit.
	 */
	public int getCalenderSuspendUnit() {
		if ("DAYS".equalsIgnoreCase(suspendUnit)) {
			return Calendar.DAY_OF_WEEK;
		} else if ("HOURS".equalsIgnoreCase(suspendUnit)) {
			return Calendar.HOUR_OF_DAY;
		} else if ("MINUTES".equalsIgnoreCase(suspendUnit)) {
			return Calendar.MINUTE;
		} else if ("SECONDS".equalsIgnoreCase(suspendUnit)) {
			return Calendar.SECOND;
		}
		return -1;
	}
	/**
	 * @return Returns the comments.
	 */
	public java.lang.String getComments() {
		return comments;
	}

	/**
	 * @param comments The comments to set.
	 */
	public void setComments(java.lang.String comments) {
		this.comments = comments;
	}

	public boolean isNextOptSuspend() {
		return OPT_SUSPEND.equalsIgnoreCase(getNextOpt());
	}

	public boolean isNextOptRoute() {
		return OPT_ROUTE.equalsIgnoreCase(getNextOpt());
	}
		/**
		 * @return Returns the nextSuspendActivationDate.
		 */
	public java.util.Date getSuspendActivationDate() {
		return suspendActivationDate;
	}
	/**
	 * @param nextSuspendActivationDate The nextSuspendActivationDate to set.
	 */
	public void setSuspendActivationDate(java.util.Date nextSuspendActivationDate) {
		this.suspendActivationDate = nextSuspendActivationDate;
	}	
}
