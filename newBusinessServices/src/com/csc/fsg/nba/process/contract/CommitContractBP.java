package com.csc.fsg.nba.process.contract;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fs.Message;
import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaTransactionValidationException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaPerformanceLogger;
import com.csc.fsg.nba.foundation.NbaPrintLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaContractApprovalDispositionRequest;
import com.csc.fsg.nba.vo.NbaContractUpdateVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TentativeDisp;
import com.csc.fsg.nba.vo.txlife.TransResult;

/**
 * Accepts an <code>NbaContractUpdateVO</code> as input to commit contract and work item
 * changes to the appropriate systems.  If the <code>NbaTXLife</code> is present in the
 * NbaContractUpdateVO, then any change will be persisted to the appropriate back end
 * system.  The work item, <code>NbaDst</code>, will be updated based on the UpdateWork
 * and UnlockWork flags set on the NbaContractUpdateVO.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA151</td><td>Version 6</td><td>UL and VUL Application Entry Rewrite</td></tr>
 * <tr><td>NBA187</td><td>Version 7</td><td>nbA Trial Application</td></tr>
 * <tr><td>NBA211</td><td>Version 7</td><td>Partial Application</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 extends NewBusinessAccelBP
public class CommitContractBP extends NewBusinessAccelBP {
	protected List lockedReqList = new ArrayList();//A2_AXAL003
	private static NbaLogger logger;

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result;
		try {
			result = persistContract((NbaContractUpdateVO) input);
		} catch (NbaTransactionValidationException e) {  //ALS4153
			result = new AccelResult();//ALS4153
			addMessage(result, e.getMessage());  //ALS4153 
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("In Exception block NbaTransactionValidationException");
			}
			e.printStackTrace();
        }catch (Exception e) {
			result = new AccelResult();
			addExceptionMessage(result, e);
	        e.printStackTrace();
		}
		return result;
	}

	/**
	 * Accepts an <code>NbaContractUpdateVO</code> as input to commit contract and work item
	 * changes to the appropriate systems.
	 * @param contractVO
	 * @return
	 * @throws NbaBaseException
	 */
	protected AccelResult persistContract(NbaContractUpdateVO contractVO) throws NbaBaseException {
		NbaTXLife contract = contractVO.getNbaTXLife();
		NbaDst work = contractVO.getNbaDst();
		NbaUserVO user = contractVO.getNbaUserVO();
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("In method  persistContract");
		}
		
		//begin A2_AXAL003
		if (contractVO.isUpdateWork() && contract != null) {
			if (!getLockOnDeletedReqs(contract, user)){
				unlockWorkItems(user);//Immediately unlock all locked items
				AccelResult lockResult = new AccelResult();
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("A lock could not be obtained on the owner requirement work items, please try again later.");
				}
				addMessage(lockResult, "A lock could not be obtained on the owner requirement work items, please try again later.");
				return lockResult;
			}
		}
		//end A2_AXAL003
         NbaPerformanceLogger.initComponent(this.getClass().getName()); //NBA208-6
		AccelResult result = null;
		if (contract != null) {
		    long doContractUpdateTime = System.currentTimeMillis();//NBA208-6
			contract = doContractUpdate(contract, work, user);
			
			NbaPerformanceLogger.logElapsed("Do Contract Update", doContractUpdateTime); //NBA208-6
			if (contract.isTransactionError()) {
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Transaction Error while doContractUpdate");
				}
				result = processErrors(contract);
			} else {
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Contract Update Done");
				}
				contractVO.setNbaTXLife(contract);
			}
		}

		// if the contract update succeeded, update the work item
		//APSL613 begin
		if (NbaPrintLogger.getLogger().isDebugEnabled()) {
			if (NbaUtils.isPrintAttachedToDst(work)) {
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Print attached to DST in persistContract() - "+ work.getNbaLob().getPolicyNumber());
				}
				NbaPrintLogger.getLogger().logDebug("Print attached to DST in persistContract() - " + work.getNbaLob().getPolicyNumber());
			}
		}
		//APSL613 end
		AccelResult workResult = null;
		if (result == null) {
			if (contractVO.isUpdateWork()) {
				//begin A2_AXAL003
				if(contract != null){
					updateAndBreakReqs(user,contract);
					unlockWorkItems(user);
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("Update and Unlock complete");
					}
				}//end A2_AXAL003
			changeStatus(work, contractVO);  //NBA186
		    //begin NBA187 
		    if (contractVO.getUserID() != null){
			    setStatus(work, contractVO.getUserID(), contract); 
			}
		    //end NBA187
		    long updateWorkTime = System.currentTimeMillis();//NBA208-6
			workResult = doWorkUpdate(work, user, contractVO.isUnlockWork());
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Do Update Work " + updateWorkTime);
			}
			NbaPerformanceLogger.logElapsed("Do Update Work", updateWorkTime); //NBA208-6
			if (!workResult.hasErrors()) {
				work = (NbaDst) workResult.getFirst();
				contractVO.setNbaDst(work);
				}
			}
		} else {//begin A2_AXAL003 If result is not null
			if(contract != null){
				unlockWorkItems(user);
			}//end A2_AXAL003
		}

		// update the returning result
		if (result == null) {
			result = new AccelResult();
			if (workResult != null && workResult.hasErrors()) { //NBA211
				result.setErrors(workResult.hasErrors());
				result.addMessages(workResult.getMessages());
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Null result found");
				}
			}
		}
		result.addResult(contractVO);
		NbaPerformanceLogger.removeComponent();//NBA208-6
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("PersistContract method complete");
		}
		return result;
		
	}

	/**
	 * Update a holding inquiry to the back end system.
	 * @param contract the holding inquiry
	 * @param dst a case
	 * @param userVO the user value object
	 * @return the <code>NbaTXLife</code> value object that is the updated
	 * @throws NbaBaseException, NbaContractAccessException, RemoteException 
	 */
	protected NbaTXLife doContractUpdate(NbaTXLife contract, NbaDst dst, NbaUserVO userVO) throws NbaBaseException {
		contract.setAccessIntent(dst.isLocked(userVO.getUserID()) ? NbaConstants.UPDATE : NbaConstants.READ);
		NbaTXLife nbaTXLife = NbaContractAccess.doContractUpdate(contract, dst, userVO);
		if (dst.isCase()) {
			dst.getNbaLob().updateLobFromNbaTxLife(nbaTXLife);
			dst.setUpdate();
		}
		return nbaTXLife;
	}

	/**
	 * Updates the work item to the work flow system.  If the unlockWork flag is set,
	 * the work item will also be unlocked.
	 * @param work
	 * @param user
	 * @param unlockWork
	 * @return
	 */
	protected AccelResult doWorkUpdate(NbaDst work, NbaUserVO user, boolean unlockWork) {
		work.setNbaUserVO(user);
		if (unlockWork) {
			setUnlock(work);
		}
		Result res = callService("NbaUpdateWorkBP", work);  //NBA213
		if (unlockWork && !res.hasErrors()) {
			return removeContractLock(res, work, user);
		}
		return AccelResult.buildResult(res);
	}

	/**
	 * Removes the contract lock.
	 * @param res
	 * @param work
	 * @param user
	 * @return
	 */
	protected AccelResult removeContractLock(Result res, NbaDst work, NbaUserVO user) {
		try {
			NbaContractLock.removeLock(work, user);
		} catch (NbaBaseException nbe) {
			Message msg = new Message();
			String[] messages = new String[1];
			messages[0] = nbe.getMessage();
			msg = msg.setVariableData(messages);
			res.setErrors(true);
			res.addMessage(msg);
		}
		return AccelResult.buildResult(res);
	}

	/**
	 * Processes errors retrieved from the back end system and creates a new AccelResult
	 * to place them in so they can be returned to the caller.
	 * @param contract
	 * @return
	 */
	protected AccelResult processErrors(NbaTXLife contract) {
		AccelResult result = new AccelResult();
		TransResult transResult = contract.getTransResult();
		if (transResult != null) {
			Message msg = new Message();
			int count = transResult.getResultInfoCount();
			String[] messages = new String[count];
			for (int i = 0; i < count; i++) {
				ResultInfo resultInfo = transResult.getResultInfoAt(i);
				messages[i] = resultInfo.getResultInfoDesc();
			}
			msg = msg.setVariableData(messages);
			result.setErrors(true);
			result.addMessage(msg);
		}
		return result;
	}


	/**
	 * Sets the unlock flag on the work item, so during the update process the work item
	 * will change status to start nbA processing.
	 * @param nbaDst
	 */
	protected void setUnlock(NbaDst nbaDst) {
        NbaUserVO user = nbaDst.getNbaUserVO();
        if (user != null) {
        	nbaDst.setUnlock(user.getUserID());
        }
	}
	
	/**
	 * Use the <code>NbaProcessStatusProvider</code> to determine the work item's next
	 * status and priority.
	 * @param work
	 * @throws NbaBaseException
	 */
	//NBA187 New Method
	protected void setStatus(NbaDst work, String userID, NbaTXLife contract) throws NbaBaseException {
        NbaProcessStatusProvider provider = new NbaProcessStatusProvider(new NbaUserVO(userID, ""), work, contract);
        work.setStatus(provider.getPassStatus());
        work.increasePriority(provider.getCaseAction(), provider.getCasePriority());
        if(work.getWorkItem().hasNewStatus()){ //ALS5260
            NbaUtils.setRouteReason(work, work.getStatus(), provider.getReason()); //ALS5260    
        }
        if(!NbaUtils.isBlankOrNull(provider.getReason())){ //ALS5337
		    NbaUtils.addGeneralComment(work,new NbaUserVO(userID, ""),provider.getReason());//ALS5337
		}//ALS5337
	}
	
	/**
	 * Change the status, priority, and route reason for the work item.  If the new status
	 * is set on the <code>NbaContractUpdateVO</code> request, then use it.  Otherwise,
	 * check if the user id is given in the request, then use it to call a rule. 
	 * @param work
	 * @param contractVO
	 */
	//NBA186 New Method
	protected void changeStatus(NbaDst work, NbaContractUpdateVO contractVO) throws NbaBaseException {
	    if (contractVO.getStatus() != null) {
			work.setStatus(contractVO.getStatus());
			work.increasePriority(contractVO.getAction(), contractVO.getPriority());
	        if(work.getWorkItem().hasNewStatus()){ //ALS5260
	        	NbaUtils.setRouteReason(work, contractVO.getStatus(),contractVO.getReason()); //ALS5260    
	        }
	        
	        if(!NbaUtils.isBlankOrNull(contractVO.getReason())){ //ALS5337
			    NbaUtils.addGeneralComment(work,contractVO.getNbaUserVO(),contractVO.getReason());//ALS5337
			}//ALS5337

		} else if (contractVO.getUserID() != null) {
		    setStatus(work, contractVO.getUserID(), contractVO.getNbaTXLife()); 
		}
	}
    /**
     * method locks all the requirement transactions related to deleted owners
     * @return List
     */
	//A2_AXAL003 New Method
	protected boolean getLockOnDeletedReqs(NbaTXLife nbaTXLife, NbaUserVO user) {
		AccelResult result = null;
		ArrayList reqInfoList = nbaTXLife.getPolicy().getRequirementInfo();
		if (!NbaUtils.isBlankOrNull(reqInfoList)) {
			for (int i = 0; i < reqInfoList.size(); i++) {
				RequirementInfo reqInfo = (RequirementInfo) reqInfoList.get(i);
				if (reqInfo.isActionDelete()) {
					RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
					NbaAwdRetrieveOptionsVO options = new NbaAwdRetrieveOptionsVO();
					options.setWorkItem(reqInfoExt.getWorkitemID(), false);
					options.setNbaUserVO(user);
					options.setLockWorkItem();
					options.setLockParentCase();
					options.requestCaseAsParent();
					result = (AccelResult) callService("NbaRetrieveWorkBP", options);
					if (result.hasErrors()) {
						return false;
					}
					lockedReqList.add(result.getFirst());
				}
			}
		}
		return true;
	}
	
	/**
	 * Update and break all locked work items.
	 * @return
	 * @throws NbaBaseException
	 */
//	A2_AXAL003 New Method
	protected void updateAndBreakReqs(NbaUserVO user, NbaTXLife nbaTxlife) throws NbaBaseException {
		AccelResult result = null;
		NbaTransaction transaction = null;
		int size = lockedReqList.size();
		for (int i = 0; i < size; i++) {
			NbaDst workItem = (NbaDst) lockedReqList.get(i);
			List trans = workItem.getNbaTransactions();
			for (int j = 0; j < trans.size(); j++) {
				workItem.setNbaUserVO(user);
				transaction = (NbaTransaction) trans.get(j);
				Map deOinkMap = new HashMap();
				deOinkMap.put("A_WorkTypeLOB",transaction.getNbaLob().getWorkType());
				NbaProcessStatusProvider provider = new NbaProcessStatusProvider(new NbaUserVO(NbaConstants.PROC_VIEW_APPLICATION_ENTRY, ""), workItem, nbaTxlife,deOinkMap);
				transaction.setStatus(provider.getPassStatus());
				transaction.setUpdate();
				NbaUtils.setRouteReason(transaction, provider.getPassStatus());
				result = (AccelResult) callService("NbaUpdateWorkBP", workItem);//Update the status of the transaction first
				if (!result.hasErrors()) {
					transaction.getWorkItem().setBreakRelation("Y");
					result = (AccelResult) callService("NbaUpdateWorkBP", workItem);//Break the relation of the transaction
					if (!result.hasErrors()) {
						continue;
					}
				}
			}
		}
	}	
	
	/**
	 * Unlocks all locked work items.
	 * @return
	 * @throws NbaBaseException
	 */
	//A2_AXAL003 new method
	protected void unlockWorkItems(NbaUserVO user) throws NbaBaseException {
		NbaTransaction transaction = null;
		AccelResult result = null;
		int size = lockedReqList.size();
		for (int i = 0; i < size; i++) {
			NbaDst workItem = (NbaDst) lockedReqList.get(i);
			List trans = workItem.getNbaTransactions();
			for (int j = 0; j < trans.size(); j++) {
				transaction = (NbaTransaction) trans.get(j);
				NbaDst nbaDst = new NbaDst();
				nbaDst.setNbaUserVO(user);
				nbaDst.setWorkItem(transaction.getWorkItem());
				result = (AccelResult) callService("NbaUnlockWorkBP", nbaDst);//unlock the transaction
				if (!result.hasErrors()) {
					continue;
				}
			}
		}
	}
	
	 /**
		 * Returns a string representing the current date from the workflow system. 
		 * @param nbaUserVO NbaUserVO object
		 * @return currentDate Date object
		 * @throws NbaBaseException
		 */
		protected Date getCurrentDateFromWorkflow(NbaUserVO nbaUserVO) throws NbaBaseException {
			Date currentDate = null;
			String timeStamp = getTimeStamp(nbaUserVO);
			if (timeStamp != null) {
				currentDate = NbaUtils.getDateFromStringInAWDFormat(timeStamp);
			}
			return currentDate;
		}
		/**
		 * The method creates a new TentativeDisp object with the disposition parameter passed to the method and adds the new object to the list
		 * @param undStatusReason 
		 * @param disposition disposition selected by user
		 * @param nbaUserVO NbaUserVO object
		 * @param appInfoExt ApplicationInfoExtension object that contains the list of tentative dispositions
		 * @param underwriterRole The underwriter's role
		 * @param underwriterRoleLevel The underwriter's level for the role
		 * @throws NbaBaseException
		 */
		protected void addTentativeDisposition(long undStatusReason, long disposition, NbaUserVO nbaUserVO, ApplicationInfoExtension appInfoExt,
				String underwriterRole, int underwriterRoleLevel) throws NbaBaseException {
			List tentativeDispList = appInfoExt.getTentativeDisp();
			TentativeDisp tentativeDisp = null;
			if (disposition != NbaConstants.LONG_NULL_VALUE) {
				tentativeDisp = createTentativeDisposition(disposition, undStatusReason, nbaUserVO, tentativeDispList, underwriterRole,
						underwriterRoleLevel);
				appInfoExt.addTentativeDisp(tentativeDisp);
			}
		}
		/**
		 * The method creates a new instance of the Tentative Disposition object
		 * @param disp the value of disposition to be set in the new object
		 * @param statusReason the value of status reason to be set in the new object
		 * @param nbaUserVO NbaUserVO object
		 * @param tentDispList the list of Tentative disposition objects
		 * @param underwriterRole The underwriter's role
		 * @param underwriterRoleLevel The underwriter's level for the role
		 * @return tentativeDisp a new TentativeDisp object
		 * @throws NbaBaseException
		 */
		protected TentativeDisp createTentativeDisposition(long disp, long statusReason, NbaUserVO nbaUserVO, List tentDispList, String underwriterRole,
				int underwriterRoleLevel) throws NbaBaseException {
			TentativeDisp tentDisplevelOne = null;
			long disposition = NbaConstants.LONG_NULL_VALUE;
			long reason = NbaConstants.LONG_NULL_VALUE;
			if (tentDispList.size() > 0) {
				tentDisplevelOne = (TentativeDisp) tentDispList.get(0);
				if (tentDisplevelOne.getDispLevel() == NbaConstants.TENT_DISP_LEVEL_ONE) {
					disposition = tentDisplevelOne.getDisposition();
					reason = tentDisplevelOne.getDispReason();
				}
			}

			if (disp != NbaConstants.LONG_NULL_VALUE) {
				disposition = disp;
			}

			if (statusReason != NbaConstants.LONG_NULL_VALUE) {
				reason = statusReason;
			}

			TentativeDisp tentativeDisp = new TentativeDisp();
			tentativeDisp.setDisposition(disposition);
			tentativeDisp.setDispLevel(getHighestDispositionLevel(tentDispList) + 1);
			tentativeDisp.setDispUndID(nbaUserVO.getUserID());
			tentativeDisp.setDispDate(getCurrentDateFromWorkflow(nbaUserVO));
			tentativeDisp.setDispReason(reason);
			tentativeDisp.setUWRole(underwriterRole);
			tentativeDisp.setUWRoleLevel(underwriterRoleLevel);
			tentativeDisp.setActionAdd();
			return tentativeDisp;
		}
		
		/**
		 * The method returns the highest level of disposition among all the tentative disposition objects
		 * @param tentDispList the list of Tentative disposition objects
		 * @return dispLevel the highest disposition level
		 */
		protected int getHighestDispositionLevel(List tentDispList) {
			int dispLevel = 0;
			int dispSize = tentDispList.size();
			TentativeDisp tentDisp = null;
			for (int i = 0; i < dispSize; i++) {
				tentDisp = (TentativeDisp) tentDispList.get(i);
				if (tentDisp.getDispLevel() > dispLevel) {
					dispLevel = tentDisp.getDispLevel();
				}
			}
			return dispLevel;
		}
		/**
		 * This method returns the disposition selected by user from the managed bean
		 * 
		 * @param undApprove
		 *                long value
		 * @param undStatus
		 *                status selected by underwriter while selecting "Do Not Issue" radio on final disposition view
		 * @return disposition long value
		 */
		protected long getUnderwriterDisposition(NbaContractApprovalDispositionRequest request) {
			long disposition = -1;
			if (request.getApproval() == NbaOliConstants.OLI_POLSTAT_APPROVED) {
				disposition = NbaOliConstants.NBA_TENTATIVEDISPOSITION_APPROVED;
			} else if (request.isInformalApp() && request.isInformalAppAccepted()) {
				disposition = request.getApproval();
			} else {
				disposition = request.getUnderwritingStatus();
			}
			return disposition;
		}
		
		public static NbaLogger getLogger() {
			if (logger == null) {
				try {
					logger = NbaLogFactory.getLogger(CommitContractBP.class);
				} catch (Exception e) {
					NbaBootLogger.log("CommitContractBP could not get a logger from the factory.");
					e.printStackTrace(System.out);
				}
			}
			return logger;
		}
		
}
