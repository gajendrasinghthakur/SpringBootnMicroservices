package com.csc.fsg.nba.bean.accessors;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.SessionBean;

import com.csc.fs.Result;
import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaCompanionCaseControlData;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaCompanionCaseVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.cash.CheckAllocationVO;

/**
 * This is a stateless session bean class that represents a business model for Companion case.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA033</td><td>Version 3</td><td>Companion Case and HTML Indexing Views</td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * <tr><td>SPR1906</td><td>Version 4</td><td>General clean-up</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>NBA101</td><td>Version 4</td><td>Companion Case Override</td></tr>
 * <tr><td>SPR1429</td><td>Version 5</td><td>Companion Case Improvements</td></tr>
 * <tr><td>SPR2775</td><td>Version 5</td><td>Money Determination generates Out of Balance error when NBPAYMENT indexed with more than 5 contracts</td></tr>
 * <tr><td>SPR2934</td><td>Version 6</td><td>Original case bouncing back to NBAPPENT queue post executing Copy BF/Copied case getting stuck at NBAPPENT queue </td></tr>
 * <tr><td>NBA153</td><td>Version 6</td><td>Companion Case Rewrite</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SR564247</td><td>Discretionary</td><td>Predictive Analysis</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaCompanionCaseFacadeBean implements SessionBean {
	private javax.ejb.SessionContext mySessionCtx;
	private static NbaLogger logger = null;
	/**
	 * getSessionContext
	 */
	public javax.ejb.SessionContext getSessionContext() {
		return mySessionCtx;
	}
	/**
	 * setSessionContext
	 */
	public void setSessionContext(javax.ejb.SessionContext ctx) {
		mySessionCtx = ctx;
	}
	/**
	 * ejbCreate
	 */
	public void ejbCreate() throws javax.ejb.CreateException {}
	/**
	 * ejbActivate
	 */
	public void ejbActivate() {}
	/**
	 * ejbPassivate
	 */
	public void ejbPassivate() {}
	/**
	 * ejbRemove
	 */
	public void ejbRemove() {}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaCompanionCaseFacadeBean.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaCompanionCaseFacadeBean could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * Retrieves a collection of linked cases.
	 * @param user an instance of <code>NbaUserVO</code>
	 * @param aNbaDst an instance of <code>NbaDst</code>
	 * @return List a collection of companion cases to return.
	 * @throws NbaBaseException
	 */
	//NBA153 changed return type to List
	public List retrieveCompanionCases(NbaUserVO user, NbaDst aNbaDst) throws NbaBaseException {
		return retrieveCompanionCases(user, aNbaDst, false);  //NBA153
	}
	/**
	 * Retrieves a collection of linked cases.
	 * @param user an instance of <code>NbaUserVO</code>
	 * @param aNbaDst an instance of <code>NbaDst</code>
	 * @param lock determines whether to lock the work items
	 * @return List a collection of companion cases to return.
	 * @throws NbaBaseException
	 */
	//NBA153 New Method
	public List retrieveCompanionCases(NbaUserVO user, NbaDst aNbaDst, boolean lock) throws NbaBaseException {
		ArrayList result = new ArrayList();
		try {//NBA103
			//Retrieves a snapshot of what is in the control source
			List companionCases = new NbaCompanionCaseControlData().getNbaCompanionCaseVOs(aNbaDst.getID());
			NbaCompanionCaseVO compactVO = null;
			NbaCompanionCaseVO completeVO = null;
			NbaDst dst = null;
			for (int i = 0; i < companionCases.size(); i++) {
				compactVO = (NbaCompanionCaseVO) companionCases.get(i); //a snapshot of what is in the control source
				if (isSameWorkItem(compactVO.getWorkItemID(), aNbaDst)) {
					dst = aNbaDst;
				} else {
					// begin NBA153
					// want to retrieve the work items with a lock.  if that fails, then retrieve without a lock
					try {
						dst = WorkflowServiceHelper.retrieveWorkItem(user, getRetrieveOptionsForCompanionCase(compactVO.getWorkItemID(), lock));
					} catch (NbaAWDLockedException e) {
						dst = WorkflowServiceHelper.retrieveWorkItem(user, getRetrieveOptionsForCompanionCase(compactVO.getWorkItemID(), false));
					}
					// end NBA153
				}
				completeVO = new NbaCompanionCaseVO(dst, user); //Should be a case  NBA153, APSL5055, NBA331
				completeVO.setCompanionReferenceID(compactVO.getCompanionReferenceID());
				completeVO.setCompanionCase(compactVO.getCompanionCase());
				//NBA153 deleted code
				completeVO.setOverride(compactVO.getOverride()); //NBA101
				completeVO.setUserID(compactVO.getUserID()); //NBA101
				completeVO.setOverridedate(compactVO.getOverrideDate()); //NBA101
				completeVO.setCwaCheckShared(compactVO.isCwaCheckShared());  //NBA153
				completeVO.setCwaAmount(compactVO.getCwaAmount());  //NBA153
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Retrieved " + completeVO.getCompanionCaseAsXML());
				}
				result.add(completeVO);
			}
			return result;
		} catch (NbaBaseException e) {//NBA103
			getLogger().logException(e);//NBA103
			throw e;//NBA103
		} catch (Throwable t) {//NBA103
			NbaBaseException e = new NbaBaseException(t);//NBA103
			getLogger().logException(e);//NBA103
			throw e;//NBA103
		}
	}
	/**
	 * Adds companion cases to a "Link".
	 * @param user an instance of <code>NbaUserVO</code>
	 * @param aNbaDst an instance of <code>NbaDst</code>
	 * @param companionCases a collection of companion cases.
	 * @param busFunc Business Function
	 * @return List a collection of companion cases to return.
	 * @throws NbaBaseException
	 */
	//SPR2934 added new parameter, busFunc
	//NBA153 changed return type and companionCases type to List
	public List addCompanionCases(NbaUserVO user, NbaDst aNbaDst, List companionCases, String busFunc) throws NbaBaseException {
		try {//NBA103
			NbaCompanionCaseVO vo = null;
			NbaDst dst = null;
			NbaLob aNbaLob = null;
			NbaLob lobCheckSource = null;
			Map workItems = new HashMap();
			double checkAmount = 0D, amountToApply = 0D;
			//NBA153 deleted code
			NbaSource checkSource = null; //SPR1429
			double checkSourceAmount = 0; //SPR1429
			
			for (int i = 0; i < companionCases.size(); i++) { //check locks, and totals
				vo = (NbaCompanionCaseVO) companionCases.get(i);
				if (vo.isActionAdd() || vo.isActionUpdate()) {	//SPR1429
					dst = vo.getNbaDst();  //NBA153
					aNbaLob = dst.getNbaLob();
					aNbaLob.setCompanionType(vo.getCompanionType());	//SPR1429
					if (vo.isCwaSameSource()) {
						aNbaLob.setCwaSameSource(true);
						if (vo.isCheckSource()) {
							checkSource = getSourceWithCheck(dst); //SPR1429
							lobCheckSource = checkSource.getNbaLob();
							checkAmount = lobCheckSource.getCheckAmount();
							amountToApply = getAmountToApply(checkSource);  //NBA331.1, APSL5055
							if(vo.getCwaAmount() > 0.0){ //cannot set amount to 0.0	
								checkSourceAmount = vo.getCwaAmount(); //SPR1429
							}	
						}
						// NBA153 deleted code
					} else { //SPR1429
						aNbaLob.setCwaSameSource(false); // Reset in case it has been set previously //SPR1429  
					}
					// begin NBA153
					amountToApply += vo.getCwaAmount();			
					// override status changed...  update date from system
					if (!NbaUtils.isBlankOrNull(vo.getUserID()) && vo.getOverrideDate() == null) {
			            NbaDst timeStamp = WorkflowServiceHelper.getTimeStamp(user);
			            vo.setOverridedate(NbaUtils.getDateFromStringInAWDFormat(timeStamp.getTimestamp()));  //NBA208-32
					}
					// end NBA153
					workItems.put(vo.getWorkItemID(), dst);
				}
			}
			// NBA153 deleted code
			if(getLogger().isDebugEnabled()){
				getLogger().logDebug("Check amount " + checkAmount);
				getLogger().logDebug("Amount to apply" + amountToApply);
			}
			// checkAmount is only set when splitting a check & that is the only time
			// we want to validate the checkAmount against the amountToApply
			if (checkAmount > 0 && NbaUtils.isEqualTo2DecimalPlaces(checkAmount,amountToApply) != 0 ) { //ALS5707
				StringBuffer error = new StringBuffer();
				error.append("Check amount [");
				error.append(checkAmount);
				error.append("] not equal to amount applied [");
				error.append(amountToApply);
				error.append("]. Please Correct.");
				throw new NbaBaseException(error.toString());
			}
			//begin SPR1429
			// Since the in-memory DST for the current Case is reused rather than 
			// re-retrieved in the retrieveWorkItem() method above, the Source with the check
			// attached to it should not be updated until after the amount edit in case the 
			// Source is attached to the current Case. 
			if (null != checkSource) {
				if (checkSourceAmount > 0) {
					lobCheckSource.setCwaAmount(checkSourceAmount);	//Amount may have been changed by the user
					checkSource.setUpdate(); 
				}				 
			}
			//end SPR1429
			new NbaCompanionCaseControlData().insert(companionCases); //Insert the companion cases
			for (int i = 0; i < companionCases.size(); i++) {
				vo = (NbaCompanionCaseVO) companionCases.get(i);
				if(workItems.containsKey(vo.getWorkItemID())){ //valid action
				  	dst = (NbaDst) workItems.get(vo.getWorkItemID());
					if (vo.isActionSuccessful()) {
						//begin SPR2934
						if (NbaConstants.MENU_BUS_FUNC_CONTRACT_COPY.equals(busFunc)){
							dst.setStatus(new NbaProcessStatusProvider(new NbaUserVO(NbaConstants.PROC_VIEW_CONTRACT_COPY, ""), dst).getPassStatus());
						}else {
							dst.setStatus(new NbaProcessStatusProvider(new NbaUserVO(NbaConstants.PROC_VIEW_COMPANION_CASE, ""), dst).getPassStatus());
						}
						//end SPR2934
						//SPR2934 code deleted
						dst.setUpdate();
						if (getLogger().isDebugEnabled()) {
							getLogger().logDebug("Added Work Item to Link. Status " + dst.getStatus());
						}
						WorkflowServiceHelper.update(user, dst); //Update the work items
					}
					if (!isSameWorkItem(dst.getID(), aNbaDst)) {
						WorkflowServiceHelper.unlockWork(user, dst); //unlock temporarily locked work items
					}
				}
			}
			return companionCases;
		} catch (NbaBaseException e) {//NBA103
			getLogger().logException(e);//NBA103
			throw e;//NBA103
		} catch (Throwable t) {//NBA103
			NbaBaseException e = new NbaBaseException(t);//NBA103
			getLogger().logException(e);//NBA103
			throw e;//NBA103
		}
	}
	/**
	 * Removes companion cases from a "Link".
	 * @param user an instance of <code>NbaUserVO</code>
	 * @param aNbaDst an instance of <code>NbaDst</code>
	 * @param companionCases a collection of companion cases.
	 * @return List a collection of remaining companion cases.
	 * @throws NbaBaseException
	 */
	//NBA153 changed return type and companionCases type to List
	public List removeCasesFromLink(NbaUserVO user, NbaDst aNbaDst, List companionCases) throws NbaBaseException {
		try {//NBA103
			NbaCompanionCaseVO vo = null;
			//SPR1906 - removed spurious Dst declaration
			NbaCompanionCaseControlData data = new NbaCompanionCaseControlData();

			for (int i = 0; i < companionCases.size(); i++) { //check locks, and totals				
				vo = (NbaCompanionCaseVO) companionCases.get(i);
				vo.resetActionForApply();
				if (vo.isActionDelete()) {
					handleDelete(user, aNbaDst, vo, data);
					if (vo.isActionSuccessful()) {
						companionCases.remove(i--); //remove the deleted cases
					}
				}
			}
			if (companionCases.size() == 1) { //if only one remains, remove it
				vo = (NbaCompanionCaseVO) companionCases.get(0);
				vo.setActionDelete();
				handleDelete(user, aNbaDst, vo, data);
				if (vo.isActionSuccessful()) {
					companionCases.remove(0); //remove the deleted cases
				}
			}
			return companionCases;
		} catch (NbaBaseException e) {//NBA103
			getLogger().logException(e);//NBA103
			throw new NbaBaseException(e);//NBA103
		} catch (Throwable t) {//NBA103
			NbaBaseException e = new NbaBaseException(t);//NBA103
			getLogger().logException(e);	//NBA103		
			throw e;//NBA103
		}
	}
	/**
	 * Sets options for the retrieval of a case.
	 * @param workItemId A work item Id.
	 * @param lock flag to indicate if a lock is needed
	 * @return NbaAwdRetrieveOptionsVO
	 */
	protected NbaAwdRetrieveOptionsVO getRetrieveOptionsForCompanionCase(String workItemId, boolean lock) {
		boolean isCase = (workItemId.substring(workItemId.length() - 3).startsWith("C") ? true : false);
		NbaAwdRetrieveOptionsVO options = new NbaAwdRetrieveOptionsVO();
		options.requestSources();
		options.requestTransactionAsChild();  //NBA153
		options.setWorkItem(workItemId, isCase); // true indicates Case
		if (lock) {
			options.setLockWorkItem();
		}
		return options;
	}
	/**
	 * Compares the work item Id and the Id of the <code>NbaDst</code> instance.
	 * @param workItemId which needs to be matched 
	 * @param aNbaDst Dst object whose Id needs to be matched.
	 * @return boolean whether workitems are same.
	 */
	protected boolean isSameWorkItem(String workItemId, NbaDst aNbaDst) {
		return (aNbaDst != null && workItemId.equals(aNbaDst.getID()));
	}
	/**
	 * Retrieves a work item based for the value object passed in. If the work item needed
	 * is a work item that is already available, this work item is simply returned.
	 * @param user an instance of <code>NbaUserVO</code>
	 * @param aNbaDst an instance of <code>NbaDst</code>
	 * @param vo A companion case value object.
	 * @param nsa A <code>NbaNetServerAccessor</code> instance
	 * @return NbaDst
	 * @throws Exception
	 */
	protected NbaDst retrieveWorkItem(NbaUserVO user, NbaDst aNbaDst, NbaCompanionCaseVO vo) throws Exception {
		if (isSameWorkItem(vo.getWorkItemID(), aNbaDst)) {
			return aNbaDst;
		} else {
			return WorkflowServiceHelper.retrieveWorkItem(user, getRetrieveOptionsForCompanionCase(vo.getWorkItemID(), true)); //SPR1851
		}
	}
	/**
	 * Removes a companion from a link and resets applicable LOBs.
	 * @param user an instance of <code>NbaUserVO</code>
	 * @param aNbaDst an instance of <code>NbaDst</code>
	 * @param vo A companion case value object.
	 * @param nsa A <code>NbaNetServerAccessor</code> instance
	 * @param data A database interface
	 * @throws Exception
	 */
	protected void handleDelete(NbaUserVO user, NbaDst aNbaDst, NbaCompanionCaseVO vo, NbaCompanionCaseControlData data)
		throws Exception {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Begin deleting from Link " + vo.getContractNumber());
		}
		NbaDst dst = null;
		try {
			dst = retrieveWorkItem(user, aNbaDst, vo);
		} catch (NbaLockedException e) { //SPR1851
			vo.setActionFailed();
			return;
		}
		dst.getNbaLob().setCwaSameSource(false);

		data.delete(vo); //remove companion cases	
		if (vo.isActionSuccessful()) {
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Deleting from Link successful " + vo.getContractNumber());
			}
			WorkflowServiceHelper.update(user, dst); //Update the work items
		}
		if (!isSameWorkItem(dst.getID(), aNbaDst) && vo.getIsUnlock()==true) {   // Updated for SR564247 
			WorkflowServiceHelper.unlockWork(user, dst); //unlock temporarily locked work items
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Delete from database & update to AWD successful " + vo.getContractNumber());
		}
	}
	
	/**
	 * Returns the payment or check source from <code>NbaDst</code> object 
	 * @param nbaDst instance of <code>NbaDst</code>.
	 * @return NbaSource Payment or Check source
	 */
	protected NbaSource getSourceWithCheck(NbaDst nbaDst) {
		List sources = nbaDst.getNbaSources();
		for (int i = 0; i < sources.size(); i++) {
			NbaSource source = (NbaSource) sources.get(i);
			if (source.getSource().getSourceType().equals(NbaConstants.A_ST_PAYMENT)
				|| source.getSource().getSourceType().equals(NbaConstants.A_ST_CWA_CHECK)) {
				return source;
			}
		}
		return null;
	}
	/**
	 * Determine if a Case is in a companion case relationship by querying the companion case table,
	 * using the the work item id.
	 * @param workItemId - the work item id used in the query
	 * @return true if there is at least one row mathcing the work item id.
	 * @throws NbaDataAccessException
	 */
	// SPR1429 new method
	public boolean isCompanionCase(String workItemId) throws NbaBaseException {
		return (new NbaCompanionCaseControlData()).isCompanionCase(workItemId);
		
	}
	
	/**
	 * Returns the total amount to apply for all the allocations of the current check.
	 * @param source
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA331.1, APSL5055 New Method
    protected double getAmountToApply(NbaSource source) throws NbaBaseException {
		Result result = ServiceHandler.invoke("RetrieveCheckAllocationsBP", ServiceContext.currentContext(), source);		
		double amountToApply = 0.0;
		for (CheckAllocationVO allocation : (List<CheckAllocationVO>)result.getData()) {
			amountToApply += allocation.getCwaAmount();
		}
		return amountToApply;
	}
}
