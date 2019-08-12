package com.csc.fsg.nba.business.process;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.csc.fsg.nba.business.uwAssignment.AxaUnderwriterAssignmentEngine;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.AxaUWAssignmentEngineVO;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;


public class NbaProcGICaseSubmit extends NbaProcAppSubmit{

	private List matchingCases = new ArrayList(); 
	private NbaUserVO userVO = null;
	protected Set matchingWorkIds = new HashSet();
	protected static final String CASE_SEARCH_VO = "NbaSearchResultVO";
	protected Map contracts = new HashMap();
	protected String formNumber;
	private List ignoredMatchingCases = new ArrayList();
	protected static final String OFFER_NUM_NOT_ASSIGNED = "not assigned";
	protected static final String UNMATCHED = "Suspended awaiting matching case";


	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		if (!isCorrectQueue(user, work)) {
			return getResult();
		}
		if (!initializeFields(user, work)) {
			return getResult();
		}
		if(work.isCase()){
			processGICase();
		}else{
			processMiscmail(user,work);
		}
		return getResult();
	}

	protected NbaAutomatedProcessResult processGICase() throws NbaBaseException{
		NbaTXLife nbaTXLife = getXML103();
		nbaTXLife.doXMLMarkUp();
		NbaLob aNbaLob = getWorkLobs();
		nbaTXLife.getPolicy().setPolNumber(aNbaLob.getOfferNumber());
		aNbaLob.setPolicyNumber(aNbaLob.getOfferNumber());
		PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(nbaTXLife.getPolicy());
		if (policyExt != null) {
			policyExt.setPendingContractStatus(NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_0000);// Reset the Pending contract status for contract
			nbaTXLife.getPolicy().setActionUpdate(); //ALS2847
			policyExt.setActionUpdate();
		}
		
		setCaseCreateDate(aNbaLob, nbaTXLife);	
		if (getMaxtries() < 1) {
			setMaxtries(1);
		}
		if (getResult() == null) { // Submit the Application to the Back End System and handle the response
			setNbaTxLife(doContractInsert(nbaTXLife));
			handleHostResponse();
		}
		
		AxaUWAssignmentEngineVO uwAssignment = new AxaUWAssignmentEngineVO();
		uwAssignment.setTxLife(getNbaTxLife());
		uwAssignment.setNbaDst(getWork());
		//uwAssignment.setTermExpIndOff(resetTermExpInd);//NBLXA 186 Term Processing Automate Kick Out rules
		uwAssignment.setReassignment(false);
		new AxaUnderwriterAssignmentEngine().execute(uwAssignment);
		
		if (getResult() == null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		}

		changeStatus(getResult().getStatus());
		doUpdateWorkItem();
		return getResult();
	}

	
	/**
	 * This method drives the RequirementOrdered process.  It process both permanent requirement 
	 * and temporary requirement work items.
	 * It starts by retrieving all the sources for the work item being processed and then looks up 
	 * any matching work items.  If none are found, the work item will be suspended or sent to 
	 * an error queue.
	 * When matching work items are located, they are updated by moving the sources associated
	 * with the temporary work item to the permanent work item.  Then all work items are updated
	 * moved to an appropriate queue.
	 * @param user the user/process for whom the process is being executed
	 * @param work a requirement work item, either permanent or temporary, to be matched
	 * @return an NbaAutomatedProcessResult containing information about
	 *         the success or failure of the process
	 * @throws NbaBaseException
	 */
	public void processMiscmail(NbaUserVO user, NbaDst work) throws NbaBaseException {
		try {
			userVO = user;
			if (work.getNbaLob().getAppProdType().equalsIgnoreCase(NbaConstants.GI_CASE)) {
				NbaSearchVO searchVO = lookupCase(work);
				retrieveMatchingCases(searchVO.getSearchResults());
				if (getMatchingCases().isEmpty()) {
					suspendWork(UNMATCHED);
				} else if (getMatchingCases().size() > 1) {
					suspendWork(getCommentForMultipleCaseMatches());
				} else {
					NbaDst currentMatch = (NbaDst) getMatchingCases().get(0);
					retrieveContract(currentMatch);
					if (getResult() == null) {
						attachMiscWorkToMatchingCase(currentMatch);
					} else {
						suspendWork(UNMATCHED);
					}
				}
				if (getResult() == null) {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, getFailStatus(), getFailStatus()));
				} 
				doUpdateWorkItem();
			
			}
		} finally {
			NbaContractLock.removeLock(getUser());
			unlockWorkItems();
			unlockIgnoredMatchingCases();
		}
	}
	/**
	 * This method attach the Misc work to the matching case.
	 * @param parentCase the maching parent case
	 */
	protected void attachMiscWorkToMatchingCase(NbaDst parentCase) throws NbaBaseException {
		//Attach the miscellaneous work to first match of case
		parentCase.getNbaCase().addNbaTransaction(getWork().getNbaTransaction());
		parentCase.getNbaCase().addNbaSource((NbaSource)getWork().getNbaSources().get(0));
		NbaLob workLob = getWork().getNbaLob();
		NbaLob parentLob = parentCase.getNbaLob();  
		workLob.setBackendSystem(parentLob.getBackendSystem());
		workLob.setCompany(parentLob.getCompany());
		workLob.setOfferNumber(parentLob.getOfferNumber());
		workLob.setPlan(parentLob.getPlan());
		workLob.setPolicyNumber(parentLob.getPolicyNumber());
		NbaTXLife nbaTxLife= (NbaTXLife)getContracts().get(parentLob.getOfferNumber());
		//Code to sent the Parent GI Case to App Entry Queue with some pre defined status. 
		Map parentDeOink = new HashMap();
		parentDeOink.put("A_REVISEDCENSUSIND", "true");
		NbaProcessStatusProvider parentStatus = new NbaProcessStatusProvider(getUser(), parentCase, nbaTxLife,parentDeOink);
		parentCase.setStatus(parentStatus.getPassStatus());
		parentCase.getNbaLob().setRouteReason(parentStatus.getReason());
		//remove the relate indicator set during addNbaTransaction call
		List sources = getWork().getNbaSources();
		for (int k = 0; k < sources.size(); k++) {
			NbaSource source = (NbaSource) sources.get(k);
			if (NbaConstants.A_ST_MISC_MAIL.equalsIgnoreCase(source.getNbaLob().getWorkType()) && NbaConstants.A_WT_MISC_MAIL.equalsIgnoreCase(getWorkType())) {
				source.setBreakRelation();
			}
		}
		parentCase.setUpdate();
		updateWork(getUser(), parentCase);
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
		changeStatus(getResult().getStatus());
	}
	
	/**
	 * This method looks at the APORDERD entry in the Requirement Control Source 
	 * for the work item to see if the work item has been previously suspended.
	 * If not, it calculates the activation date by making a call to the Requirements VP/MS
	 * model to get the number of suspend days and then suspends the work item.
	 * If previously suspended, the process sets the work item's status to
	 * the fail status to move it to either the Follow up queue (if an NBREQRMNT
	 * work item) or the Error queue (if an NBTEMPREQ work item). 
	 * @throws NbaBaseException
	 */
		/**
	 * Process unmatched miscellaneous mail work items, suspend them if APHL is not present on it
	 * Send to error queue with unmatched status if already suspended once
	 * @throws NbaBaseException throw base exception if unable to process unmatched work
	 */
	public void suspendWork(String comment) throws NbaBaseException {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_WEEK, 2);
		Date suspendDate = (calendar.getTime());
		addComment(comment);
		suspendTransaction(suspendDate);
	}

	/**
	 * This method retrieves the matching cases found in the NbaSearchResultVO object.
	 * Each case is retrieved and locked.  If unable to retrieve with lock, this work item
	 * will be suspended for a brief period of time due to the auto suspend flag being set.
	 * @param searchResults the results of the previous AWD lookup
	 * @throws NbaBaseException NbaLockException
	 */
	public void retrieveMatchingCases(List searchResults) throws NbaBaseException {
		if(searchResults == null){ 
			return;
		}
		ListIterator results = searchResults.listIterator();
		while (results.hasNext()) { //retrive only first case if available ALS3723 change the condition to loop
			NbaSearchResultVO resultVO = (NbaSearchResultVO) results.next();
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(resultVO.getWorkItemID(), true);
			retOpt.requestSources();
			retOpt.setLockWorkItem();
			NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);   
			if (A_STATUS_BADSCAN.equalsIgnoreCase(aWorkItem.getStatus()) || A_STATUS_PERMAPPDN.equalsIgnoreCase(aWorkItem.getStatus())) {//ALS5753, APSL4407
				getIgnoredMatchingCases().add(aWorkItem);
				continue;
			}		
			getMatchingCases().add(aWorkItem);	
		}
	}

	/**
	 * This method suspends a work item by using the work item information and
	 * the supplied suspend date to populate the suspendVO.
	 * @param suspendDays the number of days to suspend
	 * @throws NbaBaseException
	 */
	public void suspendTransaction(Date reqSusDate) throws NbaBaseException {
		getLogger().logDebug("Starting suspendTransaction");
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		suspendVO.setTransactionID(getWork().getID());
		suspendVO.setActivationDate(reqSusDate);
		updateForSuspend(suspendVO);
	}
	/**
	 * Since the work item must be suspended before it can be unlocked, this method
	 * is used instead of the superclass method to update AWD.
	 * <P>This method updates the work item in the AWD system, suspends the 
	 * work item using the supsendVO, and then unlocks the work item.
	 * @param suspendVO the suspend value object created by the process to be used
	 *                                  in suspending the work item.
	 * @throws NbaBaseException
	 */
	protected void updateForSuspend(NbaSuspendVO suspendVO) throws NbaBaseException {
		getLogger().logDebug("Starting updateForSuspend");  
		updateWork(getUser(), getWork());
		suspendWork(getUser(), suspendVO);  
	}
	/**
	 * Create and initialize an <code>NbaSearchVO</code> object to find any matching work items. 
	 * Call the Requirements VP/MS model to get the criteria (sets of LOB fields) to be used in the search. 
	 * Different criteria is applicable depending on whether a Transaction or Case is being searched for. 
	 * The sets are iterated over until a successful search is performed.
	 * For each search, the LOB values identifed in the set are copied from the work item to the SearchVo. Then the LOB 
	 * values are examined to verify that values for all LOBs were present on the work item. If not, the set is bypassed.
	 * For each search, the worktypes to search against are determined by a VPMS model. The worktypes vary based on
	 * whether a Transaction or Case is being searched for.
	 * If a successful search is performed, the work item referenced in the NbaSearchResultVO object are retrieved.
	 * @return the search value object containing the results of the search
	 * @throws NbaBaseException
	 */
	protected NbaSearchVO lookupCase(NbaDst work) throws NbaBaseException, NbaVpmsException, NbaNetServerDataNotFoundException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setResultClassName(CASE_SEARCH_VO);
		NbaLob lookupLOBs = new NbaLob();
		searchVO.setWorkType(A_WT_APPLICATION);
		searchVO.setQueue(QUEUE_GI_CASE_HOLD);
		lookupLOBs.setEmployerName(work.getNbaLob().getEmployerName());
		lookupLOBs.setAppProdType(GI_CASE);
		searchVO.setNbaLob(lookupLOBs);
		searchVO = WorkflowServiceHelper.lookupWork(user, searchVO);
		if (searchVO.isMaxResultsExceeded()){	
			throw new NbaBaseException(NbaBaseException.SEARCH_RESULT_EXCEEDED_SIZE, NbaExceptionType.FATAL);	
		}	
		return searchVO;
	}
	/**
	 * Returns the contracts
	 * @return contracts.
	 */
	protected Map getContracts() {
		return contracts;
	}

	/**
	 * Retrieve a contract
	 * @param dst
	 * @throws NbaBaseException 
	 */
	protected void retrieveContract(NbaDst dst) throws NbaBaseException {
		NbaContractLock.requestLock(dst, getUser()); //throws NbaLockedException if cannot obtain a lock
		NbaTXLife nbaTXLife;
		String businessProcess = NbaUtils.getBusinessProcessId(getUser());
		try {
			nbaTXLife = doHoldingInquiry(dst, getContractAccess(), businessProcess);
		} catch (NbaDataAccessException e1) {
			addComment("Unable to retrieve contract " + e1.getMessage());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, e1.getMessage(), getFailStatus()));
			return;
		}
		handleHostResponse(nbaTXLife);
		if (getResult() == null) {
			getContracts().put(dst.getNbaLob().getPolicyNumber(), nbaTXLife);
		}
	}

	/**
	 * Returns the matchingCases
	 * @return matchingCases
	 */
	protected List getMatchingCases() {
		return matchingCases;
	}
	/**
	 * Unlock all work items retrieved during processing
	 * @throws NbaBaseException 
	 */
	protected void unlockWorkItems() throws NbaBaseException {
		getLogger().logDebug("Unlocking Work Items");
		if (getMatchingCases().size() > 0) {
			int listSize = getMatchingCases().size();
			for (int j = 0; j < listSize; j++) {
				String userID = getUser().getUserID();
				NbaDst dst = (NbaDst) getMatchingCases().get(j);
				NbaTransaction trx;
				List trxs = dst.getNbaTransactions();
				int count = trxs.size();
				String getWorkID = getWork().getWorkItem().getItemID(); //prevent duplicate unlock if main work
				for (int i = 0; i < count; i++) {
					trx = (NbaTransaction) trxs.get(i);
					if (trx.isLocked(userID) && !(trx.getWorkItem().getItemID().equalsIgnoreCase(getWorkID))) { //prevent unlock dupe
						trx.setUnlock(userID);
					}
				}
				if (dst.isLocked(userID)) {
					unlockWork(getUser(), dst); 
				}
			}
		}
	}

	/**
	 * This inner class represents the result returned by Requirements VP/MS model
	 */
	protected void setStatus(NbaDst work, String userID, NbaTXLife contract) throws NbaBaseException {
		NbaProcessStatusProvider provider = new NbaProcessStatusProvider(new NbaUserVO(userID, ""), work, contract);
		work.setStatus(provider.getPassStatus());
		work.increasePriority(provider.getCaseAction(), provider.getCasePriority());
		NbaUtils.setRouteReason(work, work.getStatus());
	}

	
	/**
	 * @return Returns the formNumber.
	 */
	public String getFormNumber() {
		return formNumber;
	}
	/**
	 * @param formNumber The formNumber to set.
	 */
	public void setFormNumber(String formNumber) {
		this.formNumber = formNumber;
	}

	/**
	 * Check if misc mail source with form number already attached on case then do not attached it to case 
	 * @param permWorkitemCase
	 * @param aSource
	 * @return
	 */
	public boolean isFormAvailableOnCase(NbaDst permWorkitemCase, NbaSource aSource){
		List allSources = permWorkitemCase.getNbaSources();
		boolean isFormNumberAvailable = false;
		String wrkitemFromNumber = aSource.getNbaLob().getFormNumber();
		for (int j = 0; j < allSources.size(); j++) {
			NbaSource caseSource = (NbaSource) allSources.get(j);
			String caseSrcFormNumber = caseSource.getNbaLob().getFormNumber();
			if(!NbaUtils.isBlankOrNull(caseSrcFormNumber) && caseSrcFormNumber.equals(wrkitemFromNumber) && !A_ST_INVALID_FORM.equalsIgnoreCase(caseSource.getSourceType())){ //APSL1537
				isFormNumberAvailable = true;
			}
		}
		return isFormNumberAvailable;
	}
	
	public String getCommentForMultipleCaseMatches() throws NbaBaseException {
		NbaDst caseDst;
		StringBuffer caseComment = new StringBuffer();
		caseComment.append("Possible matching policies: ");
		String offerNumber;
		int matchingCaseSize = getMatchingCases().size();
		for (int i=0;i<matchingCaseSize;i++) {
			caseDst = (NbaDst) getMatchingCases().get(i);
			offerNumber = caseDst.getNbaLob().getOfferNumber();
			if(offerNumber != null){
				caseComment.append(offerNumber);
			}else{
				caseComment.append(OFFER_NUM_NOT_ASSIGNED);
			}
			caseComment.append(", ");
		}
		String caseCommentStr = caseComment.toString();
		if (caseCommentStr.length() > 0) {
			caseCommentStr = caseComment.substring(0, caseComment.length() - 2); //Remove last comma and space
		}
		return caseCommentStr;
	}
	
	/**
	 * add comments to work item when multiple cases are found
	 * @throws NbaBaseException 
	 */
	public void processMultipleCaseMatches() throws NbaBaseException {
		NbaDst caseDst;
		StringBuffer caseComment = new StringBuffer();
		caseComment.append("Possible matching policies: ");
		String offerNumber;
		int matchingCaseSize = getMatchingCases().size();
		for (int i=0;i<matchingCaseSize;i++) {
			caseDst = (NbaDst) getMatchingCases().get(i);
			offerNumber = caseDst.getNbaLob().getOfferNumber();
			if(offerNumber != null){
				caseComment.append(offerNumber);
			}else{
				caseComment.append(OFFER_NUM_NOT_ASSIGNED);
			}
			caseComment.append(", ");
		}
		String caseCommentStr = caseComment.toString();
		if (caseCommentStr.length() > 0) {
			caseCommentStr = caseComment.substring(0, caseComment.length() - 2); //Remove last comma and space
		}
		addComment(caseCommentStr);
	}

	public List getIgnoredMatchingCases() {
		return ignoredMatchingCases;
	}	
	public void setIgnoredMatchingCases(List unmatchedCases) {
		this.ignoredMatchingCases = unmatchedCases;
	}
	protected void unlockIgnoredMatchingCases() throws NbaBaseException {
		int listSize = getIgnoredMatchingCases().size();
		for (int i = 0; i < listSize; i++) {
			NbaDst dstCase = (NbaDst) getIgnoredMatchingCases().get(i);
			unlockWork(getUser(), dstCase);
		}
	}
	
	private boolean isFormWithApp() {
		try {
			return A_WT_MISC_MAIL.equals(getWorkType()) && getWork().getNbaLob().getFormRecivedWithAppInd();
		} catch (NbaBaseException nbe) {
			getLogger().logError(nbe.getMessage());
		}
		return false;
	}

	/**
	 * @param NbaSource
	 * @param NbaLob
	 * copyCaseLobToSource method used to copy Parent Case Lob to Source(Miscmail/Provider result),
	 * If user not entered company/policy/plan/product while indexing Miscmail/Provider result
	 */
	public void copyCaseLobToSource(NbaSource source, NbaLob parentCaseLob) {
		if (!NbaUtils.isBlankOrNull(parentCaseLob)) {
			if (!NbaUtils.isBlankOrNull(parentCaseLob.getCompany())) {
				source.getNbaLob().setCompany(parentCaseLob.getCompany());
			}
			if (!NbaUtils.isBlankOrNull(parentCaseLob.getPolicyNumber())) {
				source.getNbaLob().setPolicyNumber(parentCaseLob.getPolicyNumber());
			}
			if (!NbaUtils.isBlankOrNull(parentCaseLob.getPlan())) {
				source.getNbaLob().setPlan(parentCaseLob.getPlan());
			}
			if (!NbaUtils.isBlankOrNull(parentCaseLob.getProductTypSubtyp())) {
				source.getNbaLob().setProductTypSubtyp(parentCaseLob.getProductTypSubtyp());
			}
			if (!NbaUtils.isBlankOrNull(parentCaseLob.getApplicationType())) {
				source.getNbaLob().setApplicationType(parentCaseLob.getApplicationType());
			}
			source.setUpdate();
		}
	}

	/**
	 * @return the userVO
	 */
	public NbaUserVO getUserVO() {
		return userVO;
	}

	/**
	 * @param userVO the userVO to set
	 */
	public void setUserVO(NbaUserVO userVO) {
		this.userVO = userVO;
	}


}
