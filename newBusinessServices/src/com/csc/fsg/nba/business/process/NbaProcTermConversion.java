package com.csc.fsg.nba.business.process;

/*
 * **************************************************************<BR>
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
 * **************************************************************<BR>
 */
import java.util.HashMap;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.business.uwAssignment.AxaUnderwriterAssignmentEngine;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.AxaUWAssignmentEngineVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;


/**
 * NbaProcTermConversion is the class to process Cases found in NBCSEMCC queue.It:
 * - retrieves the child work items and sources
 * - assigns a default User ID if necesary
 * - assigns a policy number if necessary.
 * - sends the XML 103 message to the adaptor for processing. For duplicate Contract
 *   errors, it retries a user constant number of times with different Contract numbers.
 * - for each CWA source item attached to the case, it creates child Transactions 
 *   with LOB information from the Case and source item and attaches the source to
 *   the child transaction.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA300</td><td>AXA Life Phase 2</td><td>Term Conversion</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess
 * @since New Business Accelerator - Version 1
 */
public class NbaProcTermConversion extends com.csc.fsg.nba.business.process.NbaAutomatedProcess {

	protected NbaDst parentCase = null;

	/**
	 * NbaProcTermConversion constructor comment.
	 */
	public NbaProcTermConversion() {
		super();
	}

	/**

	 * @param user the user for whom the work was retrieved.
	 * @param work the AWD case to be processed
	 * @return NbaAutomatedProcessResult the results of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {

		if (!initialize(user, work)) {
			return getResult();
		}
		if (getResult() == null) {
			doProcess();
		}
		if (getResult() == null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		}
		if (getResult() != null) {
			changeStatus(getResult().getStatus());
			// Update the Work Item with it's new status and update the work item in AWD
			doUpdateWorkItem();
		}
		if (null != parentCase) {
			unlockWork(parentCase);
		}
		// Return the result
		return getResult();
	}
	
	/**
	 * To call the new ConversionUnderwriting VP/MS model and then to call the CaseAssignment VP/MS model, 
	 * passing in the new deOINK variables in addition to the existing variables
	 * @throws NbaBaseException
	 */
	public void doProcess() throws NbaBaseException {
		
		//1. Calling a new Term Conversion model to determine if underwriting is required based on certain data 
		//entered on the Replacement view for the term conversion. This model will return two pieces of data:
		//(a)a Boolean to indicate if underwriting is required, (b) a conversion increase amount to be used for underwriting, if applicable
		HashMap deOink = new HashMap();
		deOinkTermConvData(deOink);
		
		//2. Calling the CaseAssignment model, and passing in the two pieces of data returned 
		//from the Term Conversion model (see step 1 above).  
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
		oinkData.setContractSource(nbaTxLife);
        VpmsComputeResult data = getDataFromVpms(NbaVpmsAdaptor.AUTO_PROCESS_STATUS,
				NbaVpmsAdaptor.EP_WORKITEM_STATUSES,  oinkData, deOink, null);
        setStatusProvider(new NbaProcessStatusProvider(data));
        String undReqd = (String) deOink.get("A_UndRequired");
        undReqd = "true";//APSL4339
        assignQueues(undReqd);
        createRelationshipCaseManagerTransaction();//APSL4412
        doContractUpdate(nbaTxLife); //ALII1106
    }

	/** 
	 * Responsible to check if UNDQ and CSMQ LOBs are already set on the case or not,
	 * if any of the LOB is not set to the value returned from the CaseAssignment model.
	 * Uses VP/MS autoprocessstatus model for Underwriter/Case Manager queues,passes them to getEquitableUWQueue 
	 * and getEquitableCMQueue methods to determine which Underwriter/Case Manager queues are having less load to assign this case.   
	 * @return NbaSource or null
	 */
	//NBA300 new method
	protected void assignQueues(String undReqd) throws NbaBaseException {
		NbaLob caseLob = getWork().getNbaLob();//Updated LOBs
		// Code deleted for Equitable Assignment - CR 57873
		// CR57873(APSL2428) Begin
		AxaUWAssignmentEngineVO uwAssignment = new AxaUWAssignmentEngineVO();
		uwAssignment.setTxLife(nbaTxLife);
		uwAssignment.setNbaDst(getWork());
		uwAssignment.setReassignment(false);
		if("true".equalsIgnoreCase(undReqd)){
			nbaTxLife.getPrimaryHolding().getPolicy().setIssueType(NbaOliConstants.OLI_COVISSU_FULL); //ALII1106
		}else{ //If Underwriting is not required, and UNDQ is already set to some value, reset UNDQ to blank
			nbaTxLife.getPrimaryHolding().getPolicy().setIssueType(NbaOliConstants.OLI_COVISSU_CONVERTED); //ALII1106
			if(caseLob.getUndwrtQueue()!=null && caseLob.getUndwrtQueue().length()>0){
				caseLob.setUndwrtQueue("");
			}
			uwAssignment.setUnderwriterRequired(false);
		}
		new AxaUnderwriterAssignmentEngine().execute(uwAssignment);
		// CR57873(APSL2428) End		
		nbaTxLife.getPrimaryHolding().getPolicy().setActionUpdate(); //ALII1106
	}
	
	/*
	 * This method creates a RelationshipCase Manager WI for RCM for formal cases.
	 */
	//APSL4412 - New Method
	protected void createRelationshipCaseManagerTransaction() {
		try {
			if (nbaTxLife != null && NbaUtils.isRetail(nbaTxLife.getPolicy())) {
				Map deOinkMap = new HashMap();
				deOinkMap.put("A_RelationCaseManagerTransaction", "true");
				String[] workTypeAndStatus = getWorkTypeAndStatus(deOinkMap);
				if (workTypeAndStatus[0] != null && workTypeAndStatus[1] != null) {
					NbaTransaction aTransaction;
					aTransaction = getWork().addTransaction(workTypeAndStatus[0], workTypeAndStatus[1]);
					aTransaction.getNbaLob().setRouteReason(NbaUtils.getStatusTranslation(workTypeAndStatus[0], workTypeAndStatus[1]) + " - New Case");
					aTransaction.getNbaLob().setCaseManagerQueue(getWork().getNbaLob().getCaseManagerQueue());
					aTransaction.getNbaLob().setFirstName(getWork().getNbaLob().getFirstName());
					aTransaction.getNbaLob().setLastName(getWork().getNbaLob().getLastName());
					aTransaction.getNbaLob().setSsnTin(getWork().getNbaLob().getSsnTin());
					aTransaction.getNbaLob().setDOB(getWork().getNbaLob().getDOB());
					aTransaction.getNbaLob().setAgentID(getWork().getNbaLob().getAgentID());
					aTransaction.getNbaLob().setFaceAmount(getWork().getNbaLob().getFaceAmount());
					getWork().setUpdate();
				}
			}
		} catch (Exception e) {
			addComment(e.getMessage());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, e.getMessage(), getAWDFailStatus()));
		}
	}

	/**
	 * Return the Work Type and Status for the new transaction.
	 * @param Map of deOink variables
	 * @return String of work type and status
	 */
	//APSL4412 new method
	protected String[] getWorkTypeAndStatus(Map deOinkMap) {
		String[] result = new String[4];
		try {
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork(), getNbaTxLife(), deOinkMap);
			result[0] = provider.getWorkType();
			result[1] = provider.getInitialStatus();
			result[2] = provider.getWIAction();
			result[3] = provider.getWIPriority();
		} catch (NbaBaseException nbe) {
		}
		return result;
	}
	
}