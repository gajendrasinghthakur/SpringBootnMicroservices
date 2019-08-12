package com.csc.fsg.nba.business.process;

/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.csc.fs.accel.valueobject.RetrieveWorkItemsRequest;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.ResultData;

/**
 * NbaProcAggregateManual is the class that processes nbAccelerator work items
 * found on the aggregate manual queue (NBAGGMNL).
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA192</td><td>Version 7</td><td>Requirement Management Enhancement</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>ALS4459</td><td>AxaLife Phase 1</td><td>#QC2344 - Follow-Up Functionality: Match Suspend Days</td></tr> 
 * <tr><td>ALS4555</td><td>AxaLife Phase 1</td><td>QC #3558 - Incorrect processing of Follow ups in case of Manual requirements</td></tr> 
 * <tr><td>ALNA212</td><td>AXA Life Phase 2</td><td>Performance Improvement</td></tr>
 *  </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class NbaProcManualAggregate extends NbaAutomatedProcess {
    private NbaDst parentWork = null;
    private boolean parentWorkUpdated = false;
    private boolean contractUpdated = false;
    private List suspendList = new ArrayList();
    private List unSuspendList = new ArrayList(); //ALS5633
    
	/**
	 * NbaProcAggregateManual constructor comment.
	 */
	public NbaProcManualAggregate() {
		super();
	}
	/**
	 * Drives the aggregate manual process.  It processes both requirement and completed aggregate work items.
	 * It starts by retrieving parent and all the sibling workitems. 
	 * For requirement type of workitem, it checks if an aggregate requirement wokritem exists on the case and
	 * it is not in end queue then suspend the requirement workitem. It an aggregate requirement wokritem does 
	 * not exist or not in end queue, it creates a new aggregate requirement workitem.
	 * For completed requirement, it processes any manually ordered or followed-up reuqirement available on the case.
	 * @param user the user for whom the process is being executed
	 * @param work the work item to be processed
	 * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
        // Initialization
        if (!initialize(user, work)) {
            return getResult();
        }
        try {
            setParentWork(retrieveParentWork(getWork(),true,true,false));//APSL4758
            boolean hasErrors = false;
            if (NbaConstants.A_WT_REQUIREMENT.equalsIgnoreCase(work.getWorkType())) {
                processManualRequirement();
            } else if (NbaConstants.A_WT_COMPLETED_AGGREGATE.equalsIgnoreCase(work.getWorkType())) {
                processCompletedAggregate();
                updateContract();
                if (getResult() == null) {
                    setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
                } else { // host error encounter
                    hasErrors = true;
                }
                changeStatus(getResult().getStatus());
            }
            commit(hasErrors);
			unsuspendWorkitems();//ALS5633
            unlockWorkItems();  //NBA213
        } finally {
			// begin NBA213
			// unlock any work items not already unlocked excluding the current work item
			// this should only happen in an exception being thrown situation
			if (getParentWork() != null && getParentWork().isCase() && getParentWork().isLocked(user.getUserID())) {
				WorkItem childItem = null;
				Iterator iter = getParentWork().getTransactions().iterator();
				while (iter.hasNext()) {
					childItem = (WorkItem) iter.next();
					if (childItem.getItemID().equals(work.getID())) {
						iter.remove();
						break;
					}
				}
				unlockWorkItems();
			}
			//end NBA213
        }
        return getResult();
    }

    /**
     * Process the requirement workitem that to be manually ordered or followed-up. 
     * Verify if an open aggregate requirement wokritem exists on the case if not creates one
     * and suspend the workitem.
     * @throws NbaBaseException
     */
    protected void processManualRequirement() throws NbaBaseException {
    	//Code deleted APSL3881
        createNewAggregateRequirement();
        suspendWork();
        setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
    }

    /**
     * Verify if an open aggregate requirement wokritem exists on the case. Calls VP/MS model to
     * determine if an aggregate requirement is in the end queue or not.
     * @return true if an open aggregate requirement wokritem exists on the case else return false.
     * @throws NbaBaseException
     */
    protected boolean isAnyOpenAggregateRequirement(String workType, String aggRef) throws NbaBaseException {//APSL3881, APSL4087-SR790436
        List transactions = getParentWork().getNbaTransactions();
        int count = transactions.size();
        NbaTransaction nbaTransaction = null;
        boolean hasOpenAggrReq = false;
        for (int i = 0; i < count; i++) {
            nbaTransaction = (NbaTransaction) transactions.get(i);
            NbaLob nbaLob = nbaTransaction.getNbaLob();//APSL3881
            if (workType.equalsIgnoreCase(nbaTransaction.getWorkType())
                    && (nbaLob.getAggrReference() == null || aggRef.equals(nbaLob.getAggrReference()))){//APSL3881, APSL4087-SR790436
            	if(!isAggregateRequirementInEndQueue(nbaTransaction.getNbaLob())) {//APSL3881
            		hasOpenAggrReq = true;
                    break;
            	}
            }
        }
        return hasOpenAggrReq;
    }

    /**
     * Calls VP/MS model to determine if an aggregate requirement is in the end queue or not.
     * @param nbaLob the work LOBS
     * @return true if an aggregate requirement is in the end queue else return false.
     * @throws NbaBaseException
     */
    protected boolean isAggregateRequirementInEndQueue(NbaLob nbaLob) throws NbaBaseException {
        NbaVpmsModelResult data = getVpmsXmlData(nbaLob, NbaVpmsConstants.QUEUE_STATUS_CHECK, NbaVpmsConstants.EP_GET_END_QUEUE_VERIFICATION);
        boolean inEndQueue = true;
        if (data.getVpmsModelResult() != null && data.getVpmsModelResult().getResultDataCount() > 0) {
            ResultData resultData = data.getVpmsModelResult().getResultDataAt(0);
            if (resultData.getResult().size() > 0) {
                inEndQueue = Boolean.valueOf(resultData.getResultAt(0)).booleanValue();
            }
        }
        return inEndQueue;
    }

    /**
     * Calls VP/MS model and return XML result.
     * @param nbaLob the work LOBs
     * @param vpmsModelName the VP/MS model name
     * @param entryPoint the VP/MS entry point to be called
     * @return the VP/MS XML result
     * @throws NbaBaseException
     */
    protected NbaVpmsModelResult getVpmsXmlData(NbaLob nbaLob, String vpmsModelName, String entryPoint) throws NbaBaseException {
        return new NbaVpmsModelResult(getDataFromVpms(vpmsModelName, entryPoint, new NbaOinkDataAccess(nbaLob), null, null).getResult());
    }

    /**
     * Calls VP/MS model and return non-XML result.
     * @param nbaLob the work LOBs
     * @param vpmsModelName the VP/MS model name
     * @param entryPoint the VP/MS entry point to be called
     * @return the VP/MS non-XML result
     * @throws NbaBaseException
     */
    protected NbaVpmsResultsData getVpmsData(NbaLob nbaLob, String vpmsModelName, String entryPoint) throws NbaBaseException {
        return new NbaVpmsResultsData(getDataFromVpms(vpmsModelName, entryPoint, new NbaOinkDataAccess(nbaLob), null, null));
    }


    /**
     * Creates new aggregate requirement work item. Call VP/MS model to get work type, initial status, work priority and priority action.
     * @throws NbaBaseException
     */
    protected void createNewAggregateRequirement() throws NbaBaseException {
    	//Begin APSL3881
    	RequirementInfo reqInfo = getNbaTxLife().getRequirementInfo(getWork().getNbaLob().getReqUniqueID());
    	NbaOinkRequest oinkRequest = new NbaOinkRequest();
    	oinkRequest.setRequirementIdFilter(reqInfo.getId());
    	//End APSL3881
    	HashMap deOink = new HashMap();     //NBLXA-1696
    	deOink.put("A_CaseManagerQueueLOB", getParentWork().getNbaLob().getCaseManagerQueue());   //NBLXA-1696
        NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork(), oinkRequest , deOink);//APSL3881    //NBLXA-1696
        if(!isAnyOpenAggregateRequirement(provider.getWorkType(), provider.getInitialStatus())) {//APSL3881, APSL4087-SR790436
        	NbaTransaction nbaTransaction = getParentWork().addTransaction(provider.getWorkType(), provider.getInitialStatus());
            nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
            NbaLob nbaLob = nbaTransaction.getNbaLob();//APSL3881
            nbaLob.setAggrReference(provider.getInitialStatus());//APSL3881
            setParentWorkUpdated(true);	
        }
    }

    /**
     * Iterate over all requirement on the case and processes any manually ordered or followed-up 
     * reuqirement available on the case.
     * @throws NbaBaseException
     */
    protected void processCompletedAggregate() throws NbaBaseException {
    	List transactions = getParentWork().getNbaTransactions();
        int count = transactions.size();
        NbaTransaction nbaTransaction = null;
        for (int i = 0; i < count; i++) {
            nbaTransaction = (NbaTransaction) transactions.get(i);
            if (NbaConstants.A_WT_REQUIREMENT.equalsIgnoreCase(nbaTransaction.getWorkType()) && hasValidStatusForAggregate(nbaTransaction)) {
            	lockTransaction(nbaTransaction);//APSL4758
                if (isManuallyFollowedUp(nbaTransaction)) { //manually followed-up
                    processManuallyFollowedUpWork(nbaTransaction);
                } else if (NbaOliConstants.OLI_REQSTAT_SUBMITTED == NbaUtils.convertStringToInt(nbaTransaction.getNbaLob().getReqStatus())) { //else it will be manually ordered
                    updateNextStatus(nbaTransaction);
            	    //Begin ALS5633
            	    if(nbaTransaction.isSuspended()){
            			NbaSuspendVO suspendVO = new NbaSuspendVO();
            			suspendVO.setTransactionID(nbaTransaction.getID());
            	    	unSuspendList.add(suspendVO);
            	    }// End ALS5633
                }
            }
        }    
    }
    
    //APSL4758 New Method
    public void lockTransaction(NbaTransaction nbaTransaction)throws NbaNetServerDataNotFoundException, NbaBaseException {
    	if(nbaTransaction != null) {
    		String workItemID = nbaTransaction.getTransaction().getItemID();
    		RetrieveWorkItemsRequest retrieveWorkItemsRequest = new RetrieveWorkItemsRequest();
    		retrieveWorkItemsRequest.setUserID(getUser().getUserID());
    		retrieveWorkItemsRequest.setWorkItemID(workItemID);
    		retrieveWorkItemsRequest.setRecordType(workItemID.substring(26, 27));
    		retrieveWorkItemsRequest.setLockWorkItem();
    		lockWorkItem(retrieveWorkItemsRequest);	
    		nbaTransaction.getTransaction().setLockStatus(getUser().getUserID());
    	}
    }

    /**
     * Calls VP/MS to get list of valid statuses to determine if a requirement can be processed 
     * for manually ordered or followed-up
     * @param nbaTransaction the requirement work item
     * @return the list of valid statuses
     * @throws NbaBaseException
     */
    protected List getValidStatuses(NbaTransaction nbaTransaction) throws NbaBaseException {
        NbaVpmsModelResult data = getVpmsXmlData(nbaTransaction.getNbaLob(), NbaVpmsConstants.QUEUE_STATUS_CHECK,
                NbaVpmsConstants.EP_GET_VALID_STATUSES);
        List validStatuses = new ArrayList();
        if (data.getVpmsModelResult() != null && data.getVpmsModelResult().getResultDataCount() > 0) {
            ResultData resultData = data.getVpmsModelResult().getResultDataAt(0);
            int resultCount = resultData.getResultCount();
            for (int i = 0; i < resultCount; i++) {
                validStatuses.add(resultData.getResultAt(i));
            }
        }
        return validStatuses;
    }
	
	/**
	 * Determine if an requirement is in the valid status to be processed 
     * for manually ordered or followed-up
	 * @param nbaTransaction the requirement work item
	 * @return true if an requirement is in the valid status
	 * @throws NbaBaseException
	 */
	protected boolean hasValidStatusForAggregate(NbaTransaction nbaTransaction) throws NbaBaseException {
	    List validStatuses = getValidStatuses(nbaTransaction);
	    int size = validStatuses.size();
        String status = nbaTransaction.getStatus();
        boolean isValidStatus = false;
        for (int i = 0; i < size; i++) {
            if (status.equalsIgnoreCase((String) validStatuses.get(i))) {
                isValidStatus = true;
                break;
            }
        }
        return isValidStatus;
    }
	
	/**
	 * Determine if a requirement is manually followed-up
	 * @param nbaTransaction the requirement work item
	 * @return true if a requirement is manually followed-up
	 */
	protected boolean isManuallyFollowedUp(NbaTransaction nbaTransaction) {
        boolean manuallyFollowedUp = false;
        RequirementInfo reqInfo = getNbaTxLife().getRequirementInfo(nbaTransaction.getNbaLob().getReqUniqueID());
        RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
        if (reqInfoExt != null && reqInfoExt.getTrackingInfo() != null && reqInfoExt.getTrackingInfo().getFollowUpCompleted()) {
            manuallyFollowedUp = true;
        }
        return manuallyFollowedUp;
    }
	
	/**
	 * Updates reuiqrement workitem and contract for followed-up information. 
	 * @param nbaTransaction the requirement workitem 
	 * @throws NbaBaseException
	 */
	protected void processManuallyFollowedUpWork(NbaTransaction nbaTransaction) throws NbaBaseException{
	    updateNextStatus(nbaTransaction);
        RequirementInfo reqInfo = getNbaTxLife().getRequirementInfo(nbaTransaction.getNbaLob().getReqUniqueID());
        RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
        if (reqInfoExt.hasFollowUpRequestNumber()) {
            reqInfoExt.setFollowUpRequestNumber(reqInfoExt.getFollowUpRequestNumber() + 1);
        } else {
            reqInfoExt.setFollowUpRequestNumber(1); // first time request
        }
        reqInfoExt.setActionUpdate();
        reqInfoExt.getTrackingInfo().setFollowUpCompleted(false);
        reqInfoExt.getTrackingInfo().setActionUpdate();
        if (reqInfoExt.getFollowUpFreq() > 0) { //suspend only if activation date falls in future
            GregorianCalendar cal = new GregorianCalendar();
            cal.add(Calendar.DATE, reqInfoExt.getFollowUpFreq());
            reqInfoExt.setFollowUpDate(cal.getTime());
            suspendTransaction(nbaTransaction.getID(), reqInfoExt.getFollowUpDate());
        }
        setContractUpdated(true);
	}
	
	/**
	 * Update the workitem with the next status returned from VP/MS model
	 * @param nbaTransaction the work item
	 * @throws NbaBaseException
	 */
	protected void updateNextStatus(NbaTransaction nbaTransaction) throws NbaBaseException{
	    NbaProcessStatusProvider provider = new NbaProcessStatusProvider(getUser(), nbaTransaction.getNbaLob());
	    nbaTransaction.setStatus(provider.getPassStatus());
	    nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
	    nbaTransaction.setUpdate();
	    setParentWorkUpdated(true);
	}

    /**
     * Calls VP/MS model to get activate date for a workitem.
     * @throws NbaBaseException
     */
    protected void suspendWork() throws NbaBaseException {
    	// Method content changed for ALS4459
    	int suspendDaysTemp = getFollowUpFrequency(work.getNbaLob().getReqUniqueID());
    	int suspendDays = suspendDaysTemp > 0 ? suspendDaysTemp : 1; //ALS4459
    	if (suspendDays > 0) {
	    	GregorianCalendar calendar = new GregorianCalendar();
	        calendar.setTime(new Date());
	        calendar.add(Calendar.DAY_OF_WEEK, suspendDays);
	        Date reqSusDate = (calendar.getTime());
	        // ALS4555 code deleted
	        suspendTransaction(getWork().getID(), reqSusDate);
	        //addComment("Suspended to allow for manual aggregation");//ALNA212	
        }
    }

    /**
	 * @param calendar
	 * @throws NbaBaseException
	 */
    //ALS4459 New Method    
	private void setFollowupdate(GregorianCalendar calendar) throws NbaBaseException {
		String businessProcess = NbaUtils.getBusinessProcessId(getUser());
		NbaTXLife nbaTXLife = doHoldingInquiry(getWork(), getContractAccess(), businessProcess);
		if (getWork().getNbaLob().getReqUniqueID() != null) {
			RequirementInfo requirementInfo = nbaTXLife.getRequirementInfo(getWork().getNbaLob().getReqUniqueID());
			RequirementInfoExtension extension = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
			if (extension == null) {
				OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
				extension = olifeExt.getRequirementInfoExtension();
			}
				extension.setFollowUpDate(calendar.getTime());
				extension.setActionUpdate(); 
				doContractUpdate(nbaTXLife); 
		}

	}

    /**
     * Creates suspend information value object and added to the suspend work list.
     * @param transactionID the ID of suspended workitem 
     * @param activationDate the activate date
     */
    protected void suspendTransaction(String transactionID, Date activationDate) {
        NbaSuspendVO suspendVO = new NbaSuspendVO();
        suspendVO.setTransactionID(transactionID);
        suspendVO.setActivationDate(activationDate);
        getSuspendList().add(suspendVO);
    }
    
    /**
     * Update the contract.
     * @throws NbaBaseException
     */
    protected void updateContract() throws NbaBaseException {
        if (hasContractUpdated()) {
            handleHostResponse(doContractUpdate());
        }
    }
    
    /**
     * Commit any chhanges to the work flow system
     * @param hasErrors the error indicator 
     * @throws NbaBaseException
     */
    protected void commit(boolean hasErrors) throws NbaBaseException {
        if (!hasErrors) {
            //update work
            if (hasParentWorkUpdated()) {
                setParentWork(update(getParentWork()));
            } 
            setWork(update(getWork()));
            //suspend workitems
            int size = getSuspendList().size();
            for (int i = 0; i < size; i++) {
                suspendWork((NbaSuspendVO) getSuspendList().get(i));
            }
        } else { //has errors, update original work only
            setWork(update(getWork()));
        }
    }
    
    /**
     * Unlocks workitems
     * @throws NbaBaseException
     */
    protected void unlockWorkItems() throws NbaBaseException {
        NbaContractLock.removeLock(getUser());
        if (getParentWork() != null) {
            unlockWork(getParentWork());
        } else {
            unlockWork();
        }
    }
    
    /**
	 * Return the followUpFreq from the RequirementInfoExtension
	 * @param refid
	 * @return int 
	 * @throws NbaBaseException
	 */
    //ALS4459 New Method
	protected int getFollowUpFrequency(String refid) throws NbaBaseException {
		if (null == refid || refid.length() == 0) {
			return 0;
		}
		NbaTXLife nbaTxLife = doHoldingInquiry();
		RequirementInfo reqInfo = nbaTxLife.getRequirementInfo(refid);
		if (null != reqInfo ) {
			RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
			if (null != reqInfoExt && reqInfoExt.hasFollowUpFreq()) {
				return reqInfoExt.getFollowUpFreq();
			}
		}
		return 0;	
	}
	
    /**
     * Returns the parent workitem 
     * @return
     */
    protected NbaDst getParentWork() {
        return parentWork;
    }

    /**
     * Sets the parent workitem
     * @param newWork the parent workitem
     */
    protected void setParentWork(NbaDst newWork) {
        parentWork = newWork;        
    }

    /**
     * Returns true if parent work has been updated
     * @return true if parent work has been updated
     */
    protected boolean hasParentWorkUpdated() {
        return parentWorkUpdated;
    }
    /**
     * Sets true if parent work has been updated
     * @param parentWorkUpdated the parent work update indicator
     */
    protected void setParentWorkUpdated(boolean parentWorkUpdated) {
        this.parentWorkUpdated = parentWorkUpdated;
    }
    /**
     * Returns list of work suspend value objects 
     * @return Returns the suspendList.
     */
    protected List getSuspendList() {
        return suspendList;
    }
    /**
     * Sets list of work suspend value objects 
     * @param suspendList The suspendList to set.
     */
    protected void setSuspendList(List suspendList) {
        this.suspendList = suspendList;
    }
    /**
     * Returns true if contract was updated 
     * @return true if contract was updated 
     */
    protected boolean hasContractUpdated() {
        return contractUpdated;
    }
    /**
     * Sets true if contract was updated 
     * @param contractUpdated the contract update indicators.
     */
    protected void setContractUpdated(boolean contractUpdated) {
        this.contractUpdated = contractUpdated;
    }
    
    //ALS5633 New Method
	protected void unsuspendWorkitems() throws NbaBaseException {
		int size = unSuspendList.size();
		for (int i = 0; i < size; i++) {
			unsuspendWork((NbaSuspendVO) unSuspendList.get(i));
		}
	}

}

