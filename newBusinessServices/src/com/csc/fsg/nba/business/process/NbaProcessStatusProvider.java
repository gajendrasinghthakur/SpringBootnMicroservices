package com.csc.fsg.nba.business.process;
/*
 * ************************************************************** <BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group�.  The use,<BR>
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.access.contract.NbaServerUtility;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.Category;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.TrackingInfo;
import com.csc.fsg.nba.vpms.NbaVPMSHelper;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsVO;

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
 * <tr><td>NBA231</td><td>Version 8</td><td>Replacement Processing</td></tr>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * <tr><td>AXAL.7.20</td><td>AXA Life Phase 1</td><td>Workflow</td></tr>
 * <tr><td>AXAL3.7.20R</td><td>AXA Life Phase 1</td><td>Replacement Workflow</td>
 * <tr><td>NBA300</td><td>AXA Life Phase 2</td><td>Term Conversion</td>
 * <tr><td>CR57950 and 57951</td><td>Version 8</td><td>Aggregate Contract - Pre-Sale/Reg60</td></tr>
 * <tr><td>CR59174</td><td>XA Life Phase 2</td><td>1035 Exchange Case Manager</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 * @see NbaAutomatedProcess
 */
public class NbaProcessStatusProvider {
	/** The string representing the pass status */
	public java.lang.String passStatus;
	/** The string representing the fail status */
	public java.lang.String failStatus;
	/** The string representing the AWD error status */
	public java.lang.String awdErrorStatus;
	/** The string representing the host status */
	public java.lang.String hostErrorStatus;
	/** The string representing the VPMS status */
	public java.lang.String vpmsErrorStatus;
	/** The string representing the SQL error status */
	public java.lang.String sqlErrorStatus;
	/** The string representing other status status */
	public java.lang.String otherStatus; //SPR1050 added contract error
	//begin NBA020
	/** The string representing the case Action */
	protected String caseAction  = null;
	/** The string representing the case priority */
	protected String casePriority  = null;
	/** The string representing the owrk item action */
	protected String wiAction  = null;
	/** The string representing the work item priority */
	protected String wiPriority  = null;
	//end NBA020
	protected boolean cache = false; //ACN012
	/** The Map object for variables which are not accessible through ONIK for VPMS model */
	protected Map deOinkMap; //NBA044
	protected NbaDst work = null; //AXAL3.7.20
	public java.lang.String alternateStatus; //NBA068 
	/** The string representing the licenseCaseMangerQueue  */
	public java.lang.String licenseCaseMangerQueue=null;//AXAL3.7.20
	
	/** The string representing the route reason */
	public java.lang.String reason; //ALS5260

	/**
	 * This string represents the property P_Rule returned from VP/MS model and if its value is true it indicates that this is the final approval
	 * level and final disposition needs to be recorded
	 */
	protected int rule = 0; //NBA186
	/** The string representing the approval level */
	protected int level = 0; //NBA186
	
	protected String reg60CaseMangerQueue = null; //AXAL3.7.20R
	
	protected String replCaseManagerQueue = null; //CR57950 and CR57951
	
	protected String exchangeCaseMgrQueue = null; //CR59174
/**
 * Using the user and work objects, it calls the <code>initializeStatusFields</code>
 * method to set the statuses for the process.
 * @param user  the user for whom the work was retrieved
 * @param lob  the NbaLob object to be processed
 * @throws NbaBaseException
 */
//ACN026 New Method
public NbaProcessStatusProvider(NbaUserVO user, NbaLob lob) throws NbaBaseException {
	super();
	initializeStatusFields(user, lob);
}

/**
 * Using the user and work objects, it calls the <code>initializeStatusFields</code>
 * method to set the statuses for the process.
 * @param user  the user for whom the work was retrieved
 * @param work  the AWD case to be processed
 * @throws NbaBaseException
 */
public NbaProcessStatusProvider(NbaUserVO user, NbaDst work) throws NbaBaseException {
	super();
	initializeStatusFields(user, work.getNbaLob()); //ACN026
}
/**
 * Using the user and work objects, it calls the <code>initializeStatusFields</code>
 * method to set the statuses for the process.
 * @param user  the user for whom the work was retrieved
 * @param work  the AWD case to be processed
 * @param requirementInfo the Requirement Info obejct
 * @throws NbaBaseException
 */
//NBA192 New Method
public NbaProcessStatusProvider(NbaUserVO user, NbaDst work, RequirementInfo requirementInfo) throws NbaBaseException {
	super();
	setWork(work); //AXAL3.7.20
	initializeStatusFields(user, work.getNbaLob(), requirementInfo);
}
/**
 * Using the user and work objects, it calls the <code>initializeStatusFields</code>
 * method to set the statuses for the process.
 * @param user  the user for whom the work was retrieved
 * @param work  the AWD case to be processed
 * @param The Map object for variables which are not accessible through ONIK
 * @throws NbaBaseException
 */
//NBA044 new method
public NbaProcessStatusProvider(NbaUserVO user, NbaDst work, Map deOinkMap) throws NbaBaseException {
	super();
	setWork(work); //AXAL3.7.20
	setDeOinkMap(deOinkMap);
	initializeStatusFields(user, work.getNbaLob()); //ACN026
}
/**
 * Using the user and work objects, it calls the <code>initializeStatusFields</code>
 * method to set the statuses for the process.
 * @param user  the user for whom the work was retrieved
 * @param work  the AWD case to be processed
 * @param NbabTXLife Object
 * @throws NbaBaseException
 */
//NBA050 NEW METHOD
public NbaProcessStatusProvider(NbaUserVO user, NbaDst work, NbaTXLife nbaTXLife) throws NbaBaseException {
	super();
	setWork(work); //AXAL3.7.20
	initializeStatusFields(user, work, nbaTXLife);
}

/**
 * Using the user, work and nbaTXLife objects, it calls the <code>initializeStatusFields</code>
 * method to set the statuses for the process.
 * @param user  the user for whom the work was retrieved
 * @param work  the AWD case to be processed
 * @param NbabTXLife Object
 * @param deOinkMap the Map object for variables which are not accessible through ONIK
 * @throws NbaBaseException
 */
//NBA192 New Method
public NbaProcessStatusProvider(NbaUserVO user, NbaDst work, NbaTXLife nbaTXLife, Map deOinkMap) throws NbaBaseException {
	super();
	setWork(work); //AXAL3.7.20
	setDeOinkMap(deOinkMap);
	initializeStatusFields(user, work, nbaTXLife);
}

/**
 * Using the user and work objects, it calls the <code>initializeStatusFields</code>
 * method to set the statuses for the process.
 * @param user  the user for whom the work was retrieved
 * @param work  the AWD case to be processed
 * @param NbabTXLife trhe holding inquiry
 * @param requirementInfo the Requirement Info obejct
 * @throws NbaBaseException
 */
//NBA192 New Method
public NbaProcessStatusProvider(NbaUserVO user, NbaDst work, NbaTXLife nbaTXLife, RequirementInfo requirementInfo) throws NbaBaseException {
	super();
	setWork(work); //AXAL3.7.20
	initializeStatusFields(user, work.getNbaLob(), nbaTXLife, requirementInfo);
}

/**
 * Using the user and work objects, it calls the <code>initializeStatusFields</code>
 * method to set the statuses for the process.
 * @param user  the user for whom the work was retrieved
 * @param work  the AWD case to be processed
 * @param NbabTXLife trhe holding inquiry
 * @param requirementInfo the Requirement Info obejct
 * @param Map deOinkMap
 * @throws NbaBaseException
 */
//NBA231 New Method
public NbaProcessStatusProvider(NbaUserVO user, NbaDst work, NbaTXLife nbaTXLife, RequirementInfo requirementInfo, Map deOinkMap) throws NbaBaseException {
	super();
	setWork(work); //AXAL3.7.20
	setDeOinkMap(deOinkMap);
	initializeStatusFields(user, work.getNbaLob(), nbaTXLife, requirementInfo);
}
/**
 * Using the VpmsComputeResult, it calls the <code>updateProcessStatus</code>
 * method to set the statuses for the process.
 * @param vcResult the VpmsComputeResult
 * @throws NbaBaseException
 */
//ACN024 NEW METHOD
public NbaProcessStatusProvider(VpmsComputeResult vcResult) throws NbaBaseException {
	super();
	updateProcessStatus(vcResult );
}

/**
 * Answers the awdErrorStatus
 * @return the error status for AWD
 */
public java.lang.String getAwdErrorStatus() {
	return awdErrorStatus;
}
/**
 * Answers the caseAction
 * @return the case action for priority increase
 */
//NBA020 New Method
public java.lang.String getCaseAction() {
	return caseAction;
}
/**
 * Answers the casePriority
 * @return the case priority
 */
//NBA020 New Method
public java.lang.String getCasePriority() {
	return casePriority;
}
/**
 * Answers the fail status for a process
 * @return the fail status
 */
public java.lang.String getFailStatus() {
	return failStatus;
}
/**
 * Answers the host error for a status
 * @return error status for host processing
 */
public java.lang.String getHostErrorStatus() {
	return hostErrorStatus;
}
/**
 * SPR1050 Added new method for contract error status
 * Answers the contract error status for a process
 * @return the contract error status
 */
public java.lang.String getOtherStatus() {
	return otherStatus;
}
/**
 * NBA068 Added new method for inforce payment status
 * Answers the alternate status for a process
 * @return the alternate status
 */
public java.lang.String getAlternateStatus() {
	return alternateStatus;
}

/**
 * Answers the pass status for a process
 * @return the pass status
 */
public java.lang.String getPassStatus() {
	return passStatus;
}
/**
 * Answers the SQL error status for a process
 * @return the SQL error status
 */
public java.lang.String getSqlErrorStatus() {
	return sqlErrorStatus;
}
/**
 * Answers the VPMS error status
 * @return the VPMS error status
 */
public java.lang.String getVpmsErrorStatus() {
	return vpmsErrorStatus;
}
/**
 * Answers the wiAction
 * @return the work items action for priority increase
 */
//NBA020 New Method
public java.lang.String getWIAction() {
	return wiAction;
}
/**
 * Answers the wiPriority
 * @return the work item priority
 */
//NBA020 New Method
public java.lang.String getWIPriority() {
	return wiPriority;
}
/**
 * This methods determines the statuses to be used by a process based on
 * the user ID and values from the work items. (The user ID becomes the 
 * process ID used by VPMS to determine the LOB fields and statuses for 
 * a process.)
 * <P>After instantiating an <code>NbaVpmsAdaptor</code> object, VPMS is called
 * to get the statuses for the work item. The resulting values are used to populate
 * the member variables in the <code>updateProcessStatus</code> method.
 * @param newUser the process ID 
 * @param lob the NbaLob value object to be processed
 * @throws NbaBaseException
 */
//ACN026 chnaged parameter from NbaDst to NbaLob
public void initializeStatusFields(NbaUserVO user, NbaLob lob) throws NbaBaseException {
    initializeStatusFields(user, lob, null, null); //NBA192
}

/**
 * This methods determines the statuses to be used by a process based on
 * the user ID and values from the work items. (The user ID becomes the 
 * process ID used by VPMS to determine the LOB fields and statuses for 
 * a process.)
 * <P>After instantiating an <code>NbaVpmsAdaptor</code> object, VPMS is called
 * to get the statuses for the work item. The resulting values are used to populate
 * the member variables in the <code>updateProcessStatus</code> method.
 * @param newUser the process ID 
 * @param lob the NbaLob value object to be processed
 * @param requirementInfo the Requirement Info obejct
 * @throws NbaBaseException
 */
//NBA192 New Method
public void initializeStatusFields(NbaUserVO user, NbaLob lob, RequirementInfo requirementInfo) throws NbaBaseException {
    initializeStatusFields(user, lob, null, requirementInfo); 
}
//SPR1715 method deleted

/**
 * This methods determines the statuses to be used by a process based on
 * the user ID and values from the work items. (The user ID becomes the 
 * process ID used by VPMS to determine the LOB fields and statuses for 
 * a process.)
 * <P>After instantiating an <code>NbaVpmsAdaptor</code> object, VPMS is called
 * to get the statuses for the work item. The resulting values are used to populate
 * the member variables in the <code>updateProcessStatus</code> method.
 * @param newUser the process ID 
 * @param newWork the NbaDst value object to be processed
 * @param NbaTXLife Object
 * @throws NbaBaseException
 */
public void initializeStatusFields(NbaUserVO user, NbaDst work, NbaTXLife aNbaTXLife) throws NbaBaseException {
    initializeStatusFields(user, work.getNbaLob(), aNbaTXLife, null); //NBA192
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
//NBA192 New Method
public void initializeStatusFields(NbaUserVO user, NbaLob lob, NbaTXLife aNbaTXLife, RequirementInfo requirementInfo) throws NbaBaseException {
    NbaVpmsAdaptor vpmsProxy = null;
    try {
        NbaOinkDataAccess data = new NbaOinkDataAccess(lob);
        if (aNbaTXLife != null) {
            data.setContractSource(aNbaTXLife);
        } 
        vpmsProxy = new NbaVpmsAdaptor(data, NbaVpmsAdaptor.AUTO_PROCESS_STATUS);
        String busFunc = NbaUtils.getBusinessProcessId(user);
        getDeOinkMap().put(NbaVpmsConstants.A_PROCESS_ID, busFunc);
        String mode = NbaServerUtility.getDataStore(lob, null);
        getDeOinkMap().put(NbaVpmsConstants.A_DATASTORE_MODE, mode);
        if (NbaConstants.PROC_APP_SUBMIT.equals(busFunc) || NbaConstants.PROC_GI_APP_SUBMIT.equals(busFunc) ) {
            Category configCategory = NbaConfiguration.getInstance().getIntegrationCategory(lob.getBackendSystem(),
                    NbaConfigurationConstants.UNDERWRITINGRISK);
            if (configCategory != null && configCategory.hasValue()) {
                getDeOinkMap().put(NbaVpmsConstants.A_INTEGRATED_CLIENT, configCategory.getValue());
            } else {
                getDeOinkMap().put(NbaVpmsConstants.A_INTEGRATED_CLIENT, "");
            }
        }
        //Begin NBA250
        if (NbaConstants.PROC_PRV_FLWUP.equals(busFunc) && aNbaTXLife != null) {//ALII457
        	RequirementInfo reqInfo = aNbaTXLife.getRequirementInfo(lob.getReqUniqueID());
        	RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
            TrackingInfo trackingInfo = reqInfoExt.getTrackingInfo();
        	if (trackingInfo != null && trackingInfo.getFollowUpCompleted()) {
                getDeOinkMap().put(NbaVpmsConstants.A_FLWUP_COMPLETD, String.valueOf(trackingInfo.getFollowUpCompleted()));
            } else {
                getDeOinkMap().put(NbaVpmsConstants.A_FLWUP_COMPLETD, "false");
            }
        }//End NBA250
        // begin AXAL3.7.20
		if (NbaConstants.PROC_NBAFORMAL.equalsIgnoreCase(busFunc) ||
				//NbaConstants.PROC_AUTO_UNDERWRITING.equalsIgnoreCase(busFunc) || // code commented by ALS3347
				NbaConstants.PROC_NBACREATE.equalsIgnoreCase(busFunc)) {
			AxaUtils.deOinkCWAInd(getWork(), (HashMap) getDeOinkMap());
		}
		// end AXAL3.7.20
		//start NBA300
		if ((NbaConstants.PROC_AUTO_UNDERWRITING.equals(busFunc) || NbaConstants.PROC_TERMCONV.equals(busFunc)
				|| NbaConstants.PROC_AGGREGATE.equals(busFunc) || NbaConstants.PROC_ELECTRONIC_MONEY.equals(busFunc)
				|| NbaConstants.PROC_AGGR_CONTRACT.equals(busFunc)
				|| NbaConstants.PROC_PRINT_HOLD.equals(busFunc)) 
				&& (aNbaTXLife != null)) {//ALII457,APSL2735,APSL4765
			NbaVPMSHelper.deOinkTermConvData(getDeOinkMap(), aNbaTXLife, lob);
		}
		
		//end NBA300
		//start APSL4412
		if ((NbaConstants.PROC_AGGR_CONTRACT.equals(busFunc)) && (aNbaTXLife != null)) {
			String rcmTeam = NbaUtils.getRCMTeam(NbaUtils.getAsuCodeForRetail(aNbaTXLife), NbaUtils.getEPGInd(aNbaTXLife));
			getDeOinkMap().put(NbaVpmsConstants.A_RCMTEAM, rcmTeam);
		}
		//end APSL4412
		
		// Begin NBLXA-2035
		if ((NbaConstants.PROC_AGGREGATE.equals(busFunc))
				&& (lob.getReview() == NbaConstants.REVIEW_USER_REQUIRED || lob.getReview() == NbaConstants.REVIEW_SYSTEMATIC)
				&& (aNbaTXLife != null)) {
					Map resultMap = getAppQueueAndStatus(user, aNbaTXLife.getPolicy().getPolNumber());
			
				String appQueue = (String)resultMap.get("appQueue");
				String appStatus = (String)resultMap.get("appStatus");
				getDeOinkMap().put(NbaVpmsConstants.A_APPLICATION_QUEUE_LOB, appQueue);
				getDeOinkMap().put(NbaVpmsConstants.A_APPLICATION_STATUS, appStatus);
		}
		// End NBLXA-2035
		// NBLXA-2343,NBLXA-2658 deleted switch over logic
		vpmsProxy.setSkipAttributesMap(getDeOinkMap());
        vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_WORKITEM_STATUSES);
        if (requirementInfo != null) {
            NbaOinkRequest oinkRequest = new NbaOinkRequest();
            oinkRequest.setRequirementIdFilter(requirementInfo.getId());
            vpmsProxy.setANbaOinkRequest(oinkRequest);
        }
        VpmsComputeResult result = vpmsProxy.getResults();
        updateProcessStatus(result);
    } catch (java.rmi.RemoteException re) {
        throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_STATUS, re);
    } catch (Throwable t) {
        throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_STATUS, t);
    } finally {
        //begin SPR3362
        try {
            if (vpmsProxy != null) {
                vpmsProxy.remove();
            }
        } catch (Throwable th) {
            LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
        }
        //end SPR3362
    }
}

/**
 * Sets the AWD error status.
 * @param newAwdErrorStatus
 */
public void setAwdErrorStatus(java.lang.String newAwdErrorStatus) {
	awdErrorStatus = newAwdErrorStatus;
}
/**
 * Sets the Case Action
 * @param newCaseAction
 */
// NBA020 New Method
public void setCaseAction(java.lang.String newCaseAction) {
	caseAction = newCaseAction;
}
/**
 * Sets the Case Priority
 * @param newCasePriority
 */
// NBA020 New Method
public void setCasePriority(java.lang.String newCasePriority) {
	casePriority = newCasePriority;
}
/**
 * Sets the fail status.
 * @param newFailStatus
 */
public void setFailStatus(java.lang.String newFailStatus) {
	failStatus = newFailStatus;
}
/**
 * Sets the host error status.
 * @param newHostErrorStatus
 */
public void setHostErrorStatus(java.lang.String newHostErrorStatus) {
	hostErrorStatus = newHostErrorStatus;
}
/**
 * SPR1050 Added new method for contract error status
 * Sets contarct error status.
 * @param newOtherStatus
 */
public void setOtherStatus(java.lang.String newOtherStatus) {
	otherStatus = newOtherStatus;
}
/**
 * NBA068 Added new method for alternate status
 * Sets alternate status.
 * @param alternateStatus
 */
public void setAlternateStatus(java.lang.String newAlternateStatus) {
	alternateStatus = newAlternateStatus;
}

/**
 * Sets the pass status.
 * @param newPassStatus
 */
public void setPassStatus(java.lang.String newPassStatus) {
	passStatus = newPassStatus;
}
/**
 * Sets SQL AWD error status.
 * @param newSqlErrorStatus
 */
public void setSqlErrorStatus(java.lang.String newSqlErrorStatus) {
	sqlErrorStatus = newSqlErrorStatus;
}
/**
 * Sets the VPMS error status.
 * @param newVpmsErrorStatus
 */
public void setVpmsErrorStatus(java.lang.String newVpmsErrorStatus) {
	vpmsErrorStatus = newVpmsErrorStatus;
}
/**
 * Sets the Work Items Action
 * @param newWIAction
 */
// NBA020 New Method
public void setWIAction(java.lang.String newWIAction) {
	wiAction = newWIAction;
}
/**
 * Sets the Work Items Priority
 * @param newWIPriority
 */
// NBA020 New Method
public void setWIPriority(java.lang.String newWIPriority) {
	wiPriority = newWIPriority;
}
/**
 * The LOB fields returned from the VPMS model are parsed from the result message
 * and the values are used to determine which NbaLob values are needed for a
 * process.  Once the NbaLob field is found, it's value is retrieved (using Reflection)
 * and placed in the corresponding NbaVpmvVO object's LOB fields.
 * @param vpmsVo the NbaVpmsVO created with values from the case
 * @param result the result from the VPMS call
 * @param aDelimiter the delimiter used by the VPMS model to separate LOB fields
 * @return NbaVpmsVO is updated and returned
 * @throws NbaBaseException
 */
public NbaVpmsVO updateLobFieldsForProcess(NbaVpmsVO vpmsVo, VpmsComputeResult result, String aDelimiter) throws NbaBaseException {
	NbaStringTokenizer tokens = new NbaStringTokenizer(result.getResult().trim(), aDelimiter);
	String aToken = tokens.nextToken(); // First token is empty - don't need it.
	int lobCount = 0;
	while (tokens.hasMoreTokens()) {
		lobCount++;
		if (lobCount > 5)
			break;
		aToken = tokens.nextToken();
		if (aToken.equals(NbaVpmsAdaptor.IGNORE))
			return vpmsVo;
		// use reflection to get the setLob? with the value for the LOB in the token.
		// must use reflection to get the LOB field, too.
		Class v = vpmsVo.getClass();
		Class[] parameter = new Class[] { String.class };
		Method setLobMethod;
		try {
			Class l = NbaUtils.classForName("com.csc.fsg.nba.vo.NbaLob");
			Method getLobValue;
			Object lobValue;
			try {
				getLobValue = l.getMethod("get" + aToken, null);
				lobValue = getLobValue.invoke(vpmsVo, null);
				setLobMethod = v.getMethod("setLob" + String.valueOf(lobCount), parameter);
				Object[] argument = new Object[] { String.valueOf(lobValue) };
				setLobMethod.invoke(vpmsVo, argument);
			} catch (NoSuchMethodException e) {
				NbaLogFactory.getLogger(this.getClass()).logException(e); //NBA103
			} catch (IllegalAccessException e) {
				NbaLogFactory.getLogger(this.getClass()).logException(e);  //NBA103
			} catch (InvocationTargetException e) {
				NbaLogFactory.getLogger(this.getClass()).logException(e);  //NBA103
			}
		} catch (ClassNotFoundException e) {
			NbaLogFactory.getLogger(this.getClass()).logException(e);  //NBA103
		}
	}
	return vpmsVo;
}
/**
 * The results from the VPMS model are parsed and statuses are retrieved
 * and used to update the various status field members.
 * @param result the result from the VPMS call
 */
 // NBA021 Changed signature to remove delimiter and use default delimiter
public void updateProcessStatus(VpmsComputeResult result) {
	// SPR3290 code deleted
	NbaStringTokenizer tokens = new NbaStringTokenizer(result.getResult().trim(), NbaVpmsAdaptor.VPMS_DELIMITER[1]); // NBA021
	String aToken;// = tokens.nextToken(); // First token is empty - don't need it. // NBA021
	while (tokens.hasMoreTokens()) {
			aToken = tokens.nextToken();
			StringTokenizer bToken = new StringTokenizer(aToken, NbaVpmsAdaptor.VPMS_DELIMITER[0]); // NBA021
			String statusType = bToken.nextToken();
			String statusValue = bToken.nextToken();
			if (statusType.equals("PASS"))
				setPassStatus(statusValue);
			else if (statusType.equals("FAIL"))
				setFailStatus(statusValue);
			else if (statusType.equals("HOST"))
				setHostErrorStatus(statusValue);
			else if (statusType.equals("AWD"))
				setAwdErrorStatus(statusValue);
			else if (statusType.equals("VPMS"))
				setVpmsErrorStatus(statusValue);
			else if (statusType.equals("SQL"))
				setSqlErrorStatus(statusValue);
			//SPR1050 Added contract error status -- start
			else if (statusType.equals("OTHER")) // NBA004 renamed to Other status
				setOtherStatus(statusValue);
			//SPR1050 Added contract error status -- end

			//beign NBA020
			else if (statusType.equals("CASEACTION"))
				setCaseAction(statusValue);
			else if (statusType.equals("CASEPRIORITY"))
				setCasePriority(statusValue);
			else if (statusType.equals("WIACTION"))
				setWIAction(statusValue);
			else if (statusType.equals("WIPRIORITY"))
				setWIPriority(statusValue);
			//end NBA020
			//	begin NBA068 Added Alternate status -- start
			else if (statusType.equals("ALTERNATE"))
				setAlternateStatus(statusValue);
			//NBA068 -- end
			//ACN012 BEGIN
			else if (statusType.equals("CACHE")) {
				setCache(Boolean.valueOf(statusValue).booleanValue());
			}
			//ACN012 END
			else if (statusType.equals("LCMQ")) {//AXAL3.7.20
				setLicenseCaseMangerQueue(statusValue);//AXAL3.7.20
			}
	        //Begin NBA186  
			else if (statusType.equals("RULE")) {
				setRule(Integer.parseInt(statusValue));
			} else if (statusType.equals("LEVEL")) {
				setLevel(Integer.parseInt(statusValue));
			}
			//End NBA186
			//Begin AXAL3.7.20R,CR57950 and 57951
			/*else if (statusType.equals("RGMQ")) {
				setReg60CaseMangerQueue(statusValue);
			}*/
			//End AXAL3.7.20R,CR57950 and 57951
			//Begin ALS5260
			else if (statusType.equals("REASON") && !NbaVpmsConstants.IGNORE.equals(statusValue)) {
				setReason(statusValue);
			}
			//End ALS5260
			//Begin CR57950 and 57951
			else if(statusType.equals("RPCM")) {
				setReplCMQueue(statusValue);
			//End CR57950 and 57951
			} else if(statusType.equals("XCMQ")) {     //CR59174
				setExchangeCaseMgrQueue(statusValue);  //CR59174
			}	//CR59174
		}
}
/**
	* Provides additional initialization support by setting the
	* case and user objects to the passed in parameters and by
	* creating a reference to the NbaNetServerAccessor EJB.
	* @param newUser the AWD User for the process
	* @param newWork the NbaDst value object to be processed
	* @return <code>true</code> indicates the statuses were successfully
	*         retrieved while <code>false</code> indicates failure.
	* @throws NbaBaseException
	*/
	// NBA050 NEW METHOD
	public NbaTXLife retrieveContract(NbaUserVO newUser, NbaDst newWork) throws NbaBaseException {
		try {
			return NbaContractAccess.doContractInquiry(NbaContractAccess.createRequestObject(newWork, newUser));  //NBA213
		} catch (NbaBaseException nbe) {
			if (nbe instanceof NbaDataAccessException) {
				return null;
			} else {
				throw nbe;
			}
		//NBA213 deleted code
		}
	}
	
	
	/**
	 * Returns the deOinkMap.
	 * @return Map
	 */
	//NBA044 new method
	protected Map getDeOinkMap() {
		if(deOinkMap == null){
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
	 * @param b
	 */
	// ACN012 New Method
	public void setCache(boolean b) {
		cache = b;
	}

	/**
	 * @return
	 */
	// ACN012 New Method
	public boolean isCache() {
		return cache;
	}
	/**
	 * @return Returns the licenseCaseMangerQueue.
	 */
	//AXAL3.7.20 New Method
	public java.lang.String getLicenseCaseMangerQueue() {
		return licenseCaseMangerQueue;
	}
	/**
	 * @param licenseCaseMangerQueue The licenseCaseMangerQueue to set.
	 */
	//AXAL3.7.20 New Method
	public void setLicenseCaseMangerQueue(java.lang.String licenseCaseMangerQueue) {
		this.licenseCaseMangerQueue = licenseCaseMangerQueue;
	}
	//NBA186 new method
	public int getRule() {
		return rule;
	}
	/**
	 * Sets the rule variable
	 * @param rule The rule to set.
	 */
	//NBA186 new method
	public void setRule(int rule) {
		this.rule = rule;
	}
	/**
	 * Answers the level variable
	 * @return the level variable
	 */
	//NBA186 new method
	public int getLevel() {
		return level;
	}
	/**
	 * Sets the level variable
	 * @param level The level to set.
	 */
	//NBA186 new method
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * get work var if available
	 * @return the work variable
	 */
	//	AXAL3.7.20 New method
	public NbaDst getWork() {
		return work;
	}
	/**
	 * Sets the work variable
	 * @param work to set.
	 */
	//	AXAL3.7.20 New method
	public void setWork(NbaDst work) {
		this.work = work;
	}
	/**
	 * @return reg60CaseMangerQueue
	 */
	//AXAL3.7.20R New Method
	public String getReg60CaseMangerQueue() {
		return reg60CaseMangerQueue;
	}
	/**
	 * @param reg60CaseMangerQueue
	 */
	//AXAL3.7.20R New Method
	public void setReg60CaseMangerQueue(String reg60CaseMangerQueue) {
		this.reg60CaseMangerQueue = reg60CaseMangerQueue;
	}
	/**
	 * @return Returns the reason.
	 */
	//ALS5260 New Method
	public java.lang.String getReason() {
		return reason;
	}
	/**
	 * @param reason The reason to set.
	 */
	//ALS5260 New Method
	public void setReason(java.lang.String reason) {
		this.reason = reason;
	}
	
	//Begin CR57950 and CR57951
	public String getReplCMQueue() {
		return replCaseManagerQueue;
	}

	/**
	 * @param replacementCaseManagerQueue
	 */

	public void setReplCMQueue(String replacementCaseManagerQueue) {
		this.replCaseManagerQueue = replacementCaseManagerQueue;
	}

	//End CR57950 and CR57951
	
	/**
	 * @return Returns the exchangeCaseMgrQueue.
	 */
	//CR59174 new method
	public String getExchangeCaseMgrQueue() {
		return exchangeCaseMgrQueue;
	}
	/**
	 * @param exchangeCaseMgrQueue The exchangeCaseMgrQueue to set.
	 */
	//CR59174 new method
	public void setExchangeCaseMgrQueue(String exchangeCaseMgrQueue) {
		this.exchangeCaseMgrQueue = exchangeCaseMgrQueue;
	}
	
	//NBLXA-2035 New Method
    public NbaSearchVO searchWI(NbaUserVO user,String workType, String contractKey) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(workType);
		searchVO.setContractNumber(contractKey);
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO = WorkflowServiceHelper.lookupWork(user, searchVO);
		return searchVO;
	}

    //NBLXA-2035 New Method
	protected Map getAppQueueAndStatus(NbaUserVO user, String polNumber) throws NbaBaseException {
		String appQueue = null;
		String appStatus = null;
		HashMap resultMap = new HashMap();
		NbaSearchVO searchVO = searchWI(user, NbaConstants.A_WT_APPLICATION, polNumber);
		if (searchVO != null && searchVO.getSearchResults() != null && !searchVO.getSearchResults().isEmpty()) {
			List searchResultList = searchVO.getSearchResults();
			if (searchResultList.size() > 0) {
				appQueue = ((NbaSearchResultVO) searchResultList.get(0)).getQueue();
				appStatus = ((NbaSearchResultVO) searchResultList.get(0)).getStatus();
			}
		}
		if(appQueue != null){
			resultMap.put("appQueue", appQueue);
		}
		if(appStatus != null){
			resultMap.put("appStatus", appStatus);
		}
		return resultMap;
	}
}
