package com.csc.fsg.nba.process.evaluate;
/* 
 * *******************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.LockRetrieveWorkRequest;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaPerformanceLogger;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.contract.CommitContractBP;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaValidationMessageData;
import com.csc.fsg.nba.vo.NbaContractUpdateVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaEvaluateRequest;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;

/**
 * Ensure that an Evaluate work item exists for the contract and that it has the correct
 * AWD initial status as defined in the AutoProcessStatusModel. 
 * If no work item exists, create a new one.
 * If one exists, determine if it is in the correct status.  If not, update to the initial
 * status retrieved from the VPMS model.  
 * When performing this processing for Impairments generated from the Underwriter workbench
 * (initial status = "IMREQDETND"), if there is already an Evaluate work item present
 * and the status of that work item is "RREQCMPT" (work item has already passed requirements
 * determination) the work item needs to be set to a "IMREQDETND" status to route it back to
 * requirements determination.
 * When an Evaluate work item is added or re-routed, a system message with a severe error is added
 * to prohibit issue.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>ALS5388</td><td>AXALife Phase 1</td><td>QC#4560-Provider results, CRL, urine rqmt not evaluated</td></tr>
 * <tr><td>SR564247(APSL2525)</td><td>Discretionary</td><td>Predictive Analytics Full Implementation</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class GenerateEvaluateWorkItemBP extends CommitContractBP {
	String EVAL_NEEDED_ERROR = String.valueOf(NbaConstants.EVAL_NEEDED_ERROR);

	/*(non-Javadoc) 
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			NbaEvaluateRequest req = (NbaEvaluateRequest) input;
			//ALS4893 refactored
			if (req.getContract().isUnderwriterApproved() || NbaUtils.isNegativeDisposition(req.getWork())) {
				result.addResult(req);
				return result;
			}
			generateEvaluateWorkItem(req);
			result.addResult(req);
		} catch (NbaAWDLockedException a) { //ALS5388 
			addErrorMessage(result, NbaAWDLockedException.LOCKED_BY_USER); //ALS5388
		} catch (Exception e) {
			result = new AccelResult();
			addExceptionMessage(result, e);
		}
		return result;
	}
	
	/**
	 * Ensure that an Evaluate work item exists for the contract and that it has the correct AWD status. 
	 * If no work item exists, add a new one.
	 * If one exists, determine if  it is in the correct status and, if not, update to the initial status
	 * retrieved from the VPMS model.  
	 * When performing this procssing for Impairments generated from the Underwriter workbench (initial status = "IMREQDETND"),
	 * if there is already an Evaluate work item present and the status of that work item is 
	 * "RREQCMPT" (work item has already passed requirements determination) the work item needs 
	 * to be set to a "IMREQDETND" status to route it back to requirements determination.
	 * When an Evaluate work item is added or re-routed, a system message with a severe error is added
	 * to prohibit issue.
	 * @param req An instance of <code>NbaEvaluateRequest</code>
	 * @exception NbaBaseException
	 */
	//ALS4655 Refactored.
	public void generateEvaluateWorkItem(NbaEvaluateRequest req) throws NbaBaseException {
		NbaUserVO user = new NbaUserVO(req.getUserFunction(), "");
		NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(user, req.getWork(), req.getContract(), new HashMap());
		//begin AXAL3.7.20
		if (provider.getInitialStatus() == null) {
			return;
		}
		//end AXAL3.7.20
		NbaPerformanceLogger.initComponent(this.getClass().getName()); //NBA208-6
		long getReEvalTransTime = System.currentTimeMillis();//NBA208-6
		WorkItem evalTransaction = getReEvalTransaction(req.getWork(), provider.getWorkType());
		NbaPerformanceLogger.logElapsed("Get Eval Transation", getReEvalTransTime); //NBA208-6

		if (evalTransaction == null) {
			long createReEvalTransTime = System.currentTimeMillis();//NBA208-6
			evalTransaction = createReEvalTransaction(req.getWork(), provider);
			NbaPerformanceLogger.logElapsed("Create reEval Transation", createReEvalTransTime); //NBA208-6
		} else {
			//status when a new impairment is added
			if (NbaConstants.A_STATUS_EVAL_NEEDS_EVAL.equals(provider.getInitialStatus())) {
				//requirements done
				if (!NbaConstants.A_STATUS_EVAL_REQ_DET_DONE.equals(evalTransaction.getStatus())) {
					return;
				}
			}
			// if transaction is at the initial status, nothing to do
			if (evalTransaction.getStatus().equalsIgnoreCase(provider.getInitialStatus()) && !req.getPredictiveAnalysis()) { //Updated for SR564247(APSL2525)
				return;
			}
			evalTransaction.setStatus(provider.getInitialStatus());
			evalTransaction.setUpdate("Y");
		}
		
		//begin ALS3972
		if(req.isResetUWWB()){
			LobData lobData = evalTransaction.getLob("UWWB");
			if (lobData == null) {
				lobData = new LobData();
				lobData.setDataName("UWWB");
				evalTransaction.getLobData().add(lobData);
			}
			lobData.setDataValue(convertBooleanToLOB(req.isUnderwritingWB()));
		}
		//End ALS3972
		//begin SR564247(APSL2525)
		if(req.getPredictiveAnalysis()){
			LobData lobData = evalTransaction.getLob("RVST");
			if (lobData == null) {
				lobData = new LobData();
				lobData.setDataName("RVST");
				evalTransaction.getLobData().add(lobData);
			}
			lobData.setDataValue(convertBooleanToLOB(true));
		}
		//end SR564247(APSL2525)
				
		NbaContractUpdateVO contractUpdate = new NbaContractUpdateVO();
		NbaTXLife txLife = req.getContract(); // SR564247(APSL2525) CR1345969 code moved up
		boolean valMsgNeeded = isValidationMessageNeeded(txLife); // SR564247(APSL2525) CR1345969 
		if (valMsgNeeded) {
			addValidationMessage(req);
			txLife.setBusinessProcess("EVALUATE");			
			contractUpdate.setNbaTXLife(txLife);			
		}
		// SR564247(APSL2525) CR1345969 begin
		if(req.getPredictiveAnalysis()) {
			PolicyExtension polExtn = NbaUtils.getFirstPolicyExtension(txLife.getPolicy());
			polExtn.setPredManualTriggerInd(true);
			polExtn.setActionUpdate();
			txLife.setBusinessProcess("EVALUATE");
			contractUpdate.setNbaTXLife(txLife);
		}
		// SR564247(APSL2525) CR1345969 end
		if (!req.isOverrideContractCommit()) { //AXAL3.7.07
			req.getWork().setUpdate();
			contractUpdate.setNbaDst(req.getWork());
			contractUpdate.setNbaUserVO(req.getNbaUserVO());
			long persistContractTime = System.currentTimeMillis();//NBA208-6
			AccelResult result = persistContract(contractUpdate);
			NbaPerformanceLogger.logElapsed("Persist Contract", persistContractTime); //NBA208-6
			processResult(result);

			contractUpdate = (NbaContractUpdateVO) result.getFirst();
			req.setWork(contractUpdate.getNbaDst());
			if (valMsgNeeded) { //ALS4655
				req.setContract(contractUpdate.getNbaTXLife());
			} //ALS4655
		} //AXAL3.7.07
		NbaPerformanceLogger.removeComponent();//NBA208-6
	}
	

	/**
	 * Retrieves a list of evaluate transactions based on worktype.
	 * @param work
	 * @param workType
	 * @return
	 */
	//NBA208-36 New Method
	protected WorkItem getReEvalTransaction(NbaDst work, String workType) throws NbaBaseException {
		LockRetrieveWorkRequest request = new LockRetrieveWorkRequest();
		//setup the search lobs
		NbaLob workLob = work.getNbaLob();
		NbaLob tempLob = new NbaLob();
		tempLob.setCompany(workLob.getCompany());
		tempLob.setPolicyNumber(workLob.getPolicyNumber());

		request.setBusinessArea(workLob.getBusinessArea());
		request.setWorkType(workType);
		request.setPageNumber("1"); // APSL5055-NBA331
		request.setLobData((LobData[]) tempLob.getLobs().toArray(new LobData[tempLob.getLobs().size()]));
		request.setWorkItem(work.getCase());
		request.setRetrieveWorkLocked(true);
		
		getServiceContext();  //AXAL3.7.07
		AccelResult result = (AccelResult) callService("LockRetrieveWorkBP", request);
		processResult(result);

		work.addCase((WorkItem) result.getFirst());

		List aList = work.getTransactions();
		WorkItem evalTransaction = null;
		int count = aList.size();
		for (int i = 0; i < count; i++) {
			evalTransaction = (WorkItem) aList.get(i);
			if (workType.equalsIgnoreCase(evalTransaction.getWorkType())) {
				return evalTransaction;
			}
		}
		return null;
	}

	/**
	 * Creates a re-evaluate work item setting the initial status, priority, and
	 * appropriate lobs.
	 * @param work
	 * @param provider
	 * @return
	 */
	protected WorkItem createReEvalTransaction(NbaDst work, NbaProcessWorkItemProvider provider) throws NbaBaseException {
		NbaTransaction evalTransaction = work.addTransaction(provider.getWorkType(), provider.getInitialStatus());
		evalTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
		evalTransaction.getTransaction().setWorkType(provider.getWorkType());
		evalTransaction.getTransaction().setLock("Y");
		evalTransaction.setStatus(provider.getInitialStatus());

		//Copy lobs from the case to the new transaction
		NbaLob caseLob = work.getNbaLob();
		NbaLob reEvalLob = evalTransaction.getNbaLob();

		reEvalLob.setPolicyNumber(caseLob.getPolicyNumber());
		reEvalLob.setCompany(caseLob.getCompany());
		reEvalLob.setLastName(caseLob.getLastName());
		reEvalLob.setFirstName(caseLob.getFirstName());
		reEvalLob.setSsnTin(caseLob.getSsnTin());
		reEvalLob.setTaxIdType(caseLob.getTaxIdType());
		return evalTransaction.getTransaction();
	}

	/**
	 * Add a validation error message to the contract. 
	 * @param req An instance of <code>NbaEvaluateRequest</code>
	 * @throws NbaBaseException
	 */
	protected void addValidationMessage(NbaEvaluateRequest req) throws NbaBaseException {
		NbaTableAccessor nbaTableAccessor = new NbaTableAccessor();
		HashMap aMap = nbaTableAccessor.setupTableMap(req.getWork());
		aMap.put("msgCode", EVAL_NEEDED_ERROR);
		NbaValidationMessageData nbaTableData = (NbaValidationMessageData) nbaTableAccessor.getDataForOlifeValue(aMap, NbaTableConstants.NBA_VALIDATION_MESSAGE, EVAL_NEEDED_ERROR);

		Holding holding = req.getContract().getPrimaryHolding();
		SystemMessage msg = new SystemMessage();
		NbaOLifEId nbaOLifEId = new NbaOLifEId(req.getContract());//ALS4814
		nbaOLifEId.setId(msg);//ALS4814
		msg.setMessageCode(NbaConstants.EVAL_NEEDED_ERROR);
		msg.setMessageDescription(nbaTableData.getMsgDescription());
		msg.setRelatedObjectID(holding.getId());
		msg.setSequence("0");
		msg.setMessageSeverityCode(nbaTableData.getMsgSeverityTypeCode());		 
		msg.setMessageStartDate(new Date(System.currentTimeMillis()));
		msg.setActionAdd();

		OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_SYSTEMMESSAGE);
		msg.addOLifEExtension(olifeExt);
		SystemMessageExtension systemMessageExtension = olifeExt.getSystemMessageExtension();
		systemMessageExtension.setMsgOverrideInd(false);
		systemMessageExtension.setMsgRestrictCode(NbaOliConstants.NBA_MSGRESTRICTCODE_RESTAPPROVAL);

		holding.addSystemMessage(msg);
	}

	/**
	 * Returns true if the "Evaluation Needed" system message already exists on
	 * the contract. Any errors which are marked to be deleted will be ignored.
	 * @param contract
	 * @return boolean
	 */
	protected boolean isValidationMessageNeeded(NbaTXLife contract) {
		
		if (contract.isInformalApplication()){
			return false; //ALS5315
		}
		SystemMessage msg = null;
		Holding holding = contract.getPrimaryHolding();
		int count = holding.getSystemMessageCount();
		
		for (int i = 0; i < count; i++) {
			msg = holding.getSystemMessageAt(i);
			if (!msg.isActionDelete() && msg.getMessageCode() == NbaConstants.EVAL_NEEDED_ERROR) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Converts a boolean value into an LOB string value
	 * @param booleanValue the boolean to be converted
	 * @return the boolean value represented as a String
	 */
	//ALS3972 new method
	protected String convertBooleanToLOB(boolean booleanValue) {
		if (booleanValue) {
			return String.valueOf(NbaConstants.TRUE);
		} else {
			return String.valueOf(NbaConstants.FALSE);
		}
	}	
}
