package com.csc.fsg.nba.business.process.formal;
/*
 * **************************************************************************<BR>
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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************************<BR>
 */

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.business.process.NbaAutomatedProcess;
import com.csc.fsg.nba.business.process.NbaAutomatedProcessResult;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.database.NbaAutoClosureAccessor;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAutoClosureContract;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaGeneralComment;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaRetrieveCommentsRequest;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.nbaschema.SecureComment;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

/**
 * This class returns a proxy object based upon the given inputs to execute APFORMAL.
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>ALS3091</td><td>AXA Life Phase 1</td><td>General code clean up of NbaProcFormal</td></tr>
 * <tr><td>QC1630</td><td>AXA Life Phase 1</td><td>New Informal did not route to App Entry queue</td></tr>
 * <tr><td>QC1300</td><td>AXA Life Phase 1</td><td>Work itme created for NBCM, but do detail on what needs to be done</td></tr>
 * <tr><td>SR564247 Retrofit</td><td>Discretionary</td><td>Predictive Analytics Switch</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public abstract class NbaProcFormalBaseProxy extends NbaAutomatedProcess {

	public static final String PRIMARY_SEARCH = "1";
	public static final String SECONDRY_SEARCH = "2";
	public static final String TERTIARY_SEARCH = "3";//APSL3856
	
	private NbaDst lockedMatchingWork;

	public abstract NbaAutomatedProcessResult getDuplicateWorkResult();
	private NbaTXLife matchingTXLife; //ALS4742

	public abstract void doProcess() throws NbaBaseException;

	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException, NbaAWDLockedException {
		initializeWork();
		List duplicateWorks = findDuplicateWork();
		//Check for duplicate work
		if (isDuplicateWorkFound(duplicateWorks)) { //ALS4005
			processDuplicateWork(duplicateWorks); //ALS4005
		} else {
			//No duplicate work found - do rest of the processing
			doProcess();
			//ALS4005 Code Delted
		}
		//Begin ALS4005
		if (getResult() == null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		}
		getWorkLobs().deletePolicyNumber();
		//End ALS4005
		if (!"Suspended".equals(getResult().getStatus())) {
			changeStatus(getResult().getStatus());
			doUpdateWorkItem();
			if (getLockedMatchingWork() != null) {
				NbaContractLock.removeLock(getUser()); //ALS4355
				unlockWork(getLockedMatchingWork());
			}
		}

		return getResult();
	}

	protected void initializeWork() throws NbaBaseException {
		NbaSource appSource = getWork().getNbaCase().getApplicationSource();
		if (appSource != null) {
			NbaLob appSourceLob = appSource.getNbaLob();
			getWorkLobs().setPolicyNumber(appSourceLob.getPolicyNumber());	
			getWorkLobs().setBackendSystem(appSourceLob.getBackendSystem());
		} else {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
			return;
		}
	}

	/**
	 * This method finds the duplicate cases for the current work. Duplicate case means a previous case in AWD who has the same app origin as of current case and which matches other criterion configured in VP/MS. 
	 * @return
	 * @throws NbaBaseException
	 */
	public List findDuplicateWork() throws NbaBaseException {
		NbaSearchVO searchVO = lookupMatchingWork("P_InformalFormalSearchKey", null, PRIMARY_SEARCH, getWorkLobs().getAppOriginType());
		if (searchVO.getSearchResults().isEmpty()) {
			searchVO = lookupMatchingWork("P_InformalFormalSearchKey", null, SECONDRY_SEARCH, getWorkLobs().getAppOriginType());
		}
		// Begin APSL3856 
		if (searchVO.getSearchResults().isEmpty()) {
			searchVO = lookupMatchingWork("P_InformalFormalSearchKey", null, TERTIARY_SEARCH, getWorkLobs().getAppOriginType());//APSL3856
		}
		// End APSL3856 
		List duplicateCases = searchVO.getSearchResults();
		Iterator iterator = duplicateCases.iterator();
		//Remove the cases that do not meet other matching logic
		while (iterator.hasNext()) {
			NbaSearchResultVO searchResultVO = (NbaSearchResultVO) iterator.next();
			if (! isDuplicateMatchingRequired(searchResultVO)) { //QC1630
				iterator.remove();
			}
		}

		return duplicateCases;
	}
	
	//ALS4005 New Method
	protected boolean isDuplicateWorkFound(List duplicateWorks) {
		return (duplicateWorks.size() > 0) ? true : false;
	}
	
	//ALS4005 New Method
	//This is a stub method. See extending classes for actual implementation.
	protected void processDuplicateWork(List duplicateWorks) throws NbaBaseException {
	
	}

	/**
	 * Looks for matching work based on the criteria configured in VP/MS
	 * @param work
	 * @param user
	 * @param entryPoint
	 * @param searchQueue
	 * @param searchType
	 * @param appOrigin
	 * @return
	 * @throws NbaBaseException
	 */
	protected NbaSearchVO lookupMatchingWork(String entryPoint, String searchQueue, String searchType, long lookupAppOrigin) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		NbaVpmsResultsData data = getDataFromVpms(entryPoint, searchType, lookupAppOrigin);
		ArrayList lobList = data.getResultsData();
		NbaLob lookupLOBs = getNbaLobForLookup(lobList);
		lookupLOBs.setAppOriginType(lookupAppOrigin);
		lookupLOBs.setQueue(searchQueue);
		if (checkLobPresence(lookupLOBs, lobList)) {
			searchVO.setNbaLob(lookupLOBs);
			searchVO.setWorkType(NbaConstants.A_WT_APPLICATION);
			searchVO.setQueue(searchQueue);
			searchVO.setResultClassName("NbaSearchResultVO");
			searchVO = WorkflowServiceHelper.lookupWork(user, searchVO);
			//Begin QC1630
			List searchResults = searchVO.getSearchResults();
			Iterator iterator = searchResults.iterator();
			//If the current case is also matched, remove that one
			while (iterator.hasNext()) {
				NbaSearchResultVO searchResultVO = (NbaSearchResultVO) iterator.next();
				if (searchResultVO.getWorkItemID().equalsIgnoreCase(getWork().getID())) {
					iterator.remove();
				}
			}
			//End QC1630
			if (searchVO.isMaxResultsExceeded()) {
				throw new NbaBaseException(NbaBaseException.SEARCH_RESULT_EXCEEDED_SIZE, NbaExceptionType.FATAL);
			}
		} else {
			searchVO.setSearchResults(new ArrayList());
		}

		return searchVO;
	}
	
	//ALS4005 added throws clause
	protected boolean isDuplicateMatchingRequired(NbaSearchResultVO searchResultVO) {
		if (searchResultVO.getStatus().equalsIgnoreCase("DUPLICATE")) { //ALS5030
			return false; //ALS5030
		} //ALS5030
		//Begin ALS5040
		if (!NbaUtils.isBlankOrNull(searchResultVO.getNbaLob().getCompanionType())) {
			return false;
		}
		//End ALS5040
		//Begin ALS4854 ALS4891	
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(searchResultVO.getCreateDate());
		calendar.add(Calendar.DAY_OF_WEEK, 180);
		Date maxDate = (calendar.getTime());
		Date currentDate = new Date();
		if (maxDate.compareTo(currentDate) < 0) {
			return false;
		}
		//End ALS4854 ALS4891
		return true;
	}

	/**
	 * Retrieve criteria from VP/MS to match work
	 * @param work
	 * @param user
	 * @param entryPoint
	 * @param searchType
	 * @return
	 * @throws NbaBaseException
	 * @throws NbaVpmsException
	 */
	protected NbaVpmsResultsData getDataFromVpms(String entryPoint, String searchType, long lookUpAppOrigin) throws NbaBaseException,
			NbaVpmsException {
		NbaVpmsAdaptor vpmsProxy = null;
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
			Map deOink = new HashMap();
			deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser()));
			deOink.put(NbaVpmsConstants.A_PRIMARY_OR_SECONDARY, searchType);
			deOink.put(NbaVpmsConstants.A_CURRENT_APPORIGIN, String.valueOf(getWorkLobs().getAppOriginType()));
			deOink.put(NbaVpmsConstants.A_LOOKUP_APPORIGIN, String.valueOf(lookUpAppOrigin));
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsConstants.INFORMALTOFORMAL);
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			vpmsProxy.setSkipAttributesMap(deOink);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			return data;
		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException("InformalToFormal" + NbaVpmsException.VPMS_EXCEPTION, re);
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
			}
		}
	}

	protected boolean isAllowableSuspendDaysOver(NbaLob lob) throws NbaBaseException {
		Date appHoldSusDate = lob.getAppHoldSuspDate();
		if (null == appHoldSusDate) {
			appHoldSusDate = new Date();
			lob.setAppHoldSuspDate(appHoldSusDate);
			getWork().setUpdate();
		}
		int maxSuspendDaysNum = 0;
		NbaVpmsResultsData suspendData = getDataFromVpms(NbaVpmsAdaptor.EP_GET_MAX_SUSPEND_DAYS, null, -1);
		if (null != suspendData.getResultsData() && suspendData.getResultsData().size() > 0) {
			maxSuspendDaysNum = NbaUtils.convertStringToInt((String) suspendData.getResultsData().get(0));
		}
		lob.setMaxNumSuspDays(maxSuspendDaysNum);
		getWork().setUpdate();

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(appHoldSusDate);
		calendar.add(Calendar.DAY_OF_WEEK, maxSuspendDaysNum);
		Date maxSuspendDate = (calendar.getTime());
		Date currentDate = new Date();
		if (maxSuspendDate.compareTo(currentDate) <= 0) {
		    lob.deleteAppHoldSuspDate();//ALS5060
			return true;
		}

		return false;
	}
	
	/**
	 * This method creates an NbaProcessingErrorComment object
	 * @return
	 */
	//QC1300 New Method
	protected NbaProcessingErrorComment createComment() {
		NbaProcessingErrorComment npec = new NbaProcessingErrorComment();
		npec.setActionAdd();
		npec.setOriginator(getUser().getUserID());
		npec.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
		npec.setProcess(getUser().getUserID());
		return npec;
	}
	
	public NbaDst getLockedMatchingWork() {
		return lockedMatchingWork;
	}
	
	public void setLockedMatchingWork(NbaDst lockedMatchingWork) {
		this.lockedMatchingWork = lockedMatchingWork;
	}
	
	//ALS4005 New Method
	protected void mergeAppWithMatching(NbaSearchResultVO resultVO) throws NbaBaseException, NbaVpmsException {
		NbaDst matchingCase = retrieveCaseWithTransactionsAndSources(resultVO.getWorkItemID());
		//Clone matchingCase to retrieve comments - RetrieveComments does not retrieves sources
		NbaDst matchingClone = (NbaDst) matchingCase.clone(); //ALS4752
		matchingTXLife = doHoldingInquiry(matchingCase, NbaConstants.UPDATE, null); //ALS4742
		//Copy matching to current
		NbaProcFormalUtils.mergeMatchingCase(getWork().getNbaCase(), matchingCase.getNbaCase(), true);
		matchingClone = retrieveWorkItemComments(matchingClone); //ALS4752
		NbaProcFormalUtils.mergeMatchingCaseComments(getWork().getNbaCase(), matchingClone.getNbaCase()); //ALS4752
		//Start QC8401(APSL1988)
		if(!getWork().getNbaLob().getPortalCreated()){
			createXML103(matchingTXLife);
		}else{
			NbaTXLife originalTXLife  = getWork().getXML103Source();
			if(originalTXLife != null){
				updateXML103(originalTXLife,matchingTXLife);
			}
		}//End QC8401(APSL1988)
		Policy informalPolicy = matchingTXLife.getPolicy(); //APSL2714
		setDisplayImagesInd(informalPolicy.getRequirementInfo());//APSL202,2714
		NbaAutoClosureContract autoClosureContract = new NbaAutoClosureContract();
		autoClosureContract.setContractNumber(matchingTXLife.getPrimaryHolding().getPolicy().getPolNumber());
		NbaAutoClosureAccessor.delete(autoClosureContract);
		//delete the informal work from the database
		if (matchingTXLife.isTransactionError()) {
			addComment("Matching work database delete failed");
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
			return;
		}
		matchingCase.setStatus(getOtherStatus());
		//ALS4374 Code Deleted
		matchingCase.increasePriority(getStatusProvider().getCaseAction(), getStatusProvider().getCasePriority());
		NbaUtils.setRouteReason(matchingCase, matchingCase.getStatus());
		try {
			WorkflowServiceHelper.update(getUser(), matchingCase);
			setLockedMatchingWork(matchingCase);
		} catch (NbaBaseException e) {
			throw e;
		}
	}
	/**
	 * @param matchingTXLife
	 * @throws NbaBaseException
	 */
	// New method- ALS4885
	protected void processTentativeOfferRequirement(NbaSearchResultVO resultVO) throws NbaBaseException {
		NbaDst informalCase = retrieveCaseWithTransactionsAndSources(resultVO.getWorkItemID());
		NbaTXLife informalTXLife = doHoldingInquiry(informalCase, NbaConstants.UPDATE, null);
		List reqs = informalTXLife.getPolicy().getRequirementInfo();
		if (reqs != null) {
			for (Iterator iter = reqs.iterator(); iter.hasNext();) {
				RequirementInfo reqInfo = (RequirementInfo) iter.next();
				if (reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_REPLYOFFER && reqInfo.getReqStatus() != NbaOliConstants.OLI_REQSTAT_RECEIVED) {
					reqInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_WAIVED);
					reqInfo.setActionUpdate();
				}
			}
		}
		if (informalCase.isCase()) {
			List transactions = informalCase.getTransactions(); // get all the transactions
			if (transactions != null) {
				Iterator transactionItr = transactions.iterator();
				WorkItem workItem;
				while (transactionItr.hasNext()) {
					workItem = (WorkItem) transactionItr.next();
					if (!END_QUEUE.equals(workItem.getQueueID()) || A_STATUS_NEG_DISPOSED.equals(workItem.getStatus())) {//ALS5476,APSL966,APSL1403
						NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
						retOpt.setWorkItem(workItem.getItemID(), false);
						retOpt.setLockWorkItem();
						NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);
						if(A_WT_REQUIREMENT.equals(workItem.getWorkType())){//ALS5476
							aWorkItem.getNbaLob().setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_WAIVED));
							if (aWorkItem.getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_REPLYOFFER){//APSL966,APSL1403
								changeStatus(aWorkItem, A_STATUS_REQUIREMENT_CANCELLED);//APSL966,APSL1403
							}
						}
						changeStatus(aWorkItem, A_STATUS_MERGE_TO_END);//ALS5476
						aWorkItem.getNbaLob().setActionUpdate();
						aWorkItem.setUpdate();
						if (aWorkItem.isSuspended()) {
							NbaSuspendVO suspendVO = new NbaSuspendVO();
							suspendVO.setTransactionID(aWorkItem.getID());
							unsuspendWork(getUser(), suspendVO);
						}
						updateWork(getUser(), aWorkItem);
						unlockWork(getUser(), aWorkItem);
					}
				}
			}
		}
		NbaContractAccess.doContractUpdate(informalTXLife, informalCase, getUser());
		unlockWork(getUser(), informalCase);
	}
	//ALS4005 New Method
	protected NbaDst retrieveCaseWithTransactionsAndSources(String workItemID) throws NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("APFORMAL Starting retrieveWorkItem");
		}
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(workItemID, true);
		retOpt.setLockWorkItem();
		retOpt.requestSources();
		retOpt.setLockTransaction();
		retOpt.requestTransactionAsChild();
		NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);
		return aWorkItem;
	}
	
	//ALS4005 New Method
	protected void createXML103(NbaTXLife sourceTXLife) throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_NEWBUSSUBMISSION);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setNbaLob(getWorkLobs());
		nbaTXRequest.setNbaUser(getUser());
		NbaTXLife nbaTXLife = new NbaTXLife(nbaTXRequest);

		setNbaTxLife(nbaTXLife);
		createOtherHolding(sourceTXLife);//ALS5215
		nbaTXLife.getPrimaryHolding().setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);
		Policy policy = nbaTXLife.getPrimaryHolding().getPolicy();
		Policy informalPolicy = sourceTXLife.getPolicy();
		createPolicyInfo(policy, informalPolicy);
		createCoverage(sourceTXLife, policy);
		createApplicationInfo(policy, informalPolicy);
		nbaTXLife.getOLifE().setParty(sourceTXLife.getOLifE().getParty());
		createRelations(nbaTXLife.getOLifE(), sourceTXLife.getOLifE().getRelation());
		createPrimaryInsuredInfo(nbaTXLife.getPrimaryParty(), sourceTXLife.getPrimaryParty()); //ALS5349 method signature modified
		createAttachments(sourceTXLife, nbaTXLife);
		resetRateClassAndSubstandardRating(nbaTXLife); //APSL1614
		resetSecureCommentsFromAttachments(nbaTXLife);//APSL1490
		createXML103Source(nbaTXLife);

	}
	//QC8401(APSL1988) Modify 103 XML for iPipeLine Case
	protected void updateXML103(NbaTXLife originalTXLife,NbaTXLife matchingTXLife) throws NbaBaseException {
		setNbaTxLife(originalTXLife);
		Policy policy = originalTXLife.getPrimaryHolding().getPolicy();
		Policy informalPolicy = matchingTXLife.getPolicy();
		setExpireMIBCheckReq(informalPolicy.getRequirementInfo());//ALS4665
		mergeRequirementInfo(originalTXLife, matchingTXLife);
		policy.setEndorsement(informalPolicy.getEndorsement());
	//	setDisplayImagesInd(informalPolicy.getRequirementInfo());//APSL202 commented for APSL2714
		ApplicationInfo appInfo = policy.getApplicationInfo();
		appInfo.setFormalAppInd(true);
		appInfo.setTrackingID(informalPolicy.getPolNumber());
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(policy.getApplicationInfo());
		ApplicationInfoExtension informalAppInfoExt = NbaUtils.getFirstApplicationInfoExtension(informalPolicy.getApplicationInfo());
		appInfoExt.setApplicationOrigin(NbaOliConstants.OLI_APPORIGIN_PARTIAL);
		if (informalAppInfoExt != null) {
			appInfoExt.setInformalOfferDate(informalAppInfoExt.getInformalOfferDate());
			// InformalApplicationDate of informal application is its signed date
			appInfoExt.setInformalApplicationDate(informalPolicy.getApplicationInfo().getSignedDate());
			
		}
		createIpipeLineAttachments(matchingTXLife, originalTXLife);//QC11569/APSL2973
		resetSecureCommentsFromAttachments(originalTXLife);//APSL1490
		NbaOLifEId oLifeId = new NbaOLifEId(originalTXLife);
		oLifeId.resetIds(originalTXLife);
		createXML103Source(originalTXLife);
	}
	
	//QC8401(APSL1988) Merge the requirements from informal contract to the formal contract.
	protected void mergeRequirementInfo(NbaTXLife originalTXLife, NbaTXLife matchingTXLife) {
		Policy policy = originalTXLife.getPrimaryHolding().getPolicy();
		Policy informalPolicy = matchingTXLife.getPolicy();
		RequirementInfo reqInfo = null;
		for (int i = 0; i < informalPolicy.getRequirementInfo().size(); i++) {
			reqInfo = informalPolicy.getRequirementInfoAt(i);
			Relation informalRelation = matchingTXLife.getRelationByRelatedId(reqInfo.getAppliesToPartyID());
			if (informalRelation != null) {
				//Find matching formal relation
				Relation formalRelation = originalTXLife.getRelationForRelationRoleCode(informalRelation.getRelationRoleCode());
				if (formalRelation != null) {
					RequirementInfo formalReqInfo = reqInfo.clone(false);
					//Reset applies to party id
					formalReqInfo.setAppliesToPartyID(formalRelation.getRelatedObjectID());
					policy.addRequirementInfo(formalReqInfo);
				} else {
					Party informalParty = matchingTXLife.getParty(reqInfo.getAppliesToPartyID()).getParty();
					originalTXLife.getOLifE().addParty(informalParty);
					originalTXLife.getOLifE().addRelation(informalRelation);
					policy.addRequirementInfo(reqInfo);
				}
			}
		}
	}
	
	/**
	 * @param sourceTXLife
	 */
	//ALS5215 New Method
	private void createOtherHolding(NbaTXLife sourceTXLife) {
		List otherHolding = NbaUtils.getOtherHolding(sourceTXLife); //P2AXAL037
		for (int i = 0; i < otherHolding.size(); i++) {
			getNbaTxLife().getOLifE().addHolding((Holding) otherHolding.get(i));
		}
	}

	//ALS4005 New Method
	protected void createPolicyInfo(Policy formalPolicy, Policy informalPolicy)throws NbaBaseException {//ALS1667
		formalPolicy.setProductType(getWorkLobs().getProductTypSubtyp());
		OLifEExtension olifeExtn = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
		PolicyExtension policyExtension = olifeExtn.getPolicyExtension();
		formalPolicy.addOLifEExtension(olifeExtn);
		policyExtension.setDistributionChannel(getWorkLobs().getDistChannel());
		policyExtension.setProductSuite(getWorkLobs().getPlanType());
		setExpireMIBCheckReq(informalPolicy.getRequirementInfo()) ;//ALS4665
		formalPolicy.setRequirementInfo(informalPolicy.getRequirementInfo());
		formalPolicy.setEndorsement(informalPolicy.getEndorsement());
		//setDisplayImagesInd(informalPolicy.getRequirementInfo());//APSL202 commented for APSL2714
		
	}
	
	//ALS4005 New Method
	protected void createCoverage(NbaTXLife informalTXLife, Policy policy) throws NbaBaseException {

		NbaOLifEId nbaOLifEId = new NbaOLifEId(informalTXLife);
		Life life = new Life();
		getNbaOLifEId().setId(life);
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladhpc = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
		policy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(ladhpc);
		ladhpc.setLife(life);
		Coverage coverage = new Coverage();
		coverage.setProductCode(policy.getProductCode());
		coverage.setIndicatorCode(NbaOliConstants.OLI_COVIND_BASE);
		String informalPrimaryPartyID = informalTXLife.getPrimaryParty().getID();
		LifeParticipant informalLifePart = informalTXLife.getLifeParticipantFor(informalPrimaryPartyID);
		informalLifePart.setParticipantName(getWorkLobs().getDisplayName());
		nbaOLifEId.setId(informalLifePart);
		coverage.addLifeParticipant(informalLifePart);
		nbaOLifEId.setId(coverage);
		life.addCoverage(coverage);
		life.setFaceAmt(getWorkLobs().getFaceAmount());
	}
	
	//ALS4005 New Method
	protected void createApplicationInfo(Policy policy, Policy informalPolicy) throws NbaBaseException {
		ApplicationInfo appInfo = new ApplicationInfo();
		appInfo.setApplicationType(NbaOliConstants.OLI_APPTYPE_NEW);
		appInfo.setFormalAppInd(true);
		appInfo.setTrackingID(informalPolicy.getPolNumber());
		appInfo.setApplicationJurisdiction(getWorkLobs().getAppState());
		appInfo.setSignedDate(getWorkLobs().getAppDate()); //AXAL3.7.03
		appInfo.setHOAppFormNumber(getWorkLobs().getFormNumber());//QC8121
		OLifEExtension olifeExtn = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_APPLICATIONINFO);
		ApplicationInfoExtension appInfoExt = olifeExtn.getApplicationInfoExtension();
		appInfo.addOLifEExtension(olifeExtn);
	//	appInfoExt.setApplicationOrigin(NbaOliConstants.OLI_APPORIGIN_FORMAL);
		appInfoExt.setApplicationOrigin(NbaOliConstants.OLI_APPORIGIN_PARTIAL);
		ApplicationInfoExtension informalAppInfoExt = NbaUtils.getFirstApplicationInfoExtension(informalPolicy.getApplicationInfo());
		if (informalAppInfoExt != null) {
			appInfoExt.setInformalOfferDate(informalAppInfoExt.getInformalOfferDate());
			// InformalApplicationDate of informal application is its signed date
			appInfoExt.setInformalApplicationDate(informalPolicy.getApplicationInfo().getSignedDate());
			appInfoExt.setSubFirmIndicator(informalAppInfoExt.getSubFirmIndicator()); //APSL2513
		
		}
		appInfoExt.setSpecialCase(getWorkLobs().getSpecialCase());
		appInfoExt.setApplicationSubType(getWorkLobs().getInformalAppType());
		appInfoExt.setScanStation(getWorkLobs().getCreateStation());

		policy.setApplicationInfo(appInfo);
	}
	
	//ALS4005 New Method
	protected void createRelations(OLifE olife, ArrayList relations) {
		Relation relation;
		int relationSize = relations.size();
		for (int i = 0; i < relationSize; i++) {
			relation = (Relation) relations.get(i);
			if (relation.getOriginatingObjectType() != NbaOliConstants.OLI_FORMINSTANCE
					&& relation.getOriginatingObjectType() != NbaOliConstants.OLI_LIFECOVERAGE) {
				olife.addRelation(relation);
			}
		}
	}
	
	//ALS4005 New Method //ALS5349 method signature modified and added if-else.
	protected void createPrimaryInsuredInfo(NbaParty nbaParty, NbaParty srcParty) throws NbaBaseException {
		Person person = nbaParty.getPerson();
		Person srcperson = srcParty.getPerson();
		Party party = nbaParty.getParty();
		Party srcparty = srcParty.getParty(); 
		if (srcParty != null && !NbaUtils.isBlankOrNull(srcperson.getFirstName())) {
			person.setFirstName(srcperson.getFirstName());
		} else {
			person.setFirstName(getWorkLobs().getFirstName());
		}
		if (srcParty != null && !NbaUtils.isBlankOrNull(srcperson.getLastName())) {
			person.setLastName(srcperson.getLastName());
		} else {
			person.setLastName(getWorkLobs().getLastName());
		}
		if (srcParty != null && !NbaUtils.isBlankOrNull(srcperson.getMiddleName())) {
			person.setMiddleName(srcperson.getMiddleName());
		} else {
			person.setMiddleName(getWorkLobs().getMiddleInitial());
		}
		if (srcParty != null && !NbaUtils.isBlankOrNull(srcparty.getGovtID())) {
			party.setGovtID(srcparty.getGovtID());
		} else {
			party.setGovtID(getWorkLobs().getSsnTin());
		}
		if (srcParty != null && !NbaUtils.isBlankOrNull(srcparty.getGovtIDTC())) {
			party.setGovtIDTC(srcparty.getGovtIDTC());
		} else {
			party.setGovtIDTC(getWorkLobs().getTaxIdType());
		}

	}
	
	//ALS4005 New Method
	protected void createAttachments(NbaTXLife informalTXLife, NbaTXLife nbaTXLife) {
		Holding informalHolding = informalTXLife.getPrimaryHolding();
		Holding formalHolding = nbaTXLife.getPrimaryHolding();
		int attachmentCount = informalHolding.getAttachmentCount();
		Attachment attachment = null;
		for (int i = 0; i < attachmentCount; i++) {
			attachment = informalHolding.getAttachmentAt(i);
			if (NbaOliConstants.OLI_ATTACH_UNDRWRTNOTE == attachment.getAttachmentType()) {
				formalHolding.addAttachment(attachment);
			}
		}
	}
	//QC11569/APSL2973 New Method 
	//Copies attachment from both Primary Holding and Insured party. 
	protected void createIpipeLineAttachments(NbaTXLife informalTXLife, NbaTXLife nbaTXLife) {
		Holding informalHolding = informalTXLife.getPrimaryHolding();
		Holding formalHolding = nbaTXLife.getPrimaryHolding();
		int attachmentCount = informalHolding.getAttachmentCount();
		Attachment attachment = null;
		for (int i = 0; i < attachmentCount; i++) {
			attachment = informalHolding.getAttachmentAt(i);
			if (NbaOliConstants.OLI_ATTACH_UNDRWRTNOTE == attachment.getAttachmentType()) {
				formalHolding.addAttachment(attachment);
			}
		}
		Attachment partyAttachment = null;
		Party insuredParty = informalTXLife.getPrimaryParty().getParty();
		Party insuredPartyFormal = nbaTXLife.getPrimaryParty().getParty();
		int partyAttachmentCount = informalTXLife.getPrimaryParty().getParty().getAttachmentCount();
		for (int i = 0; i < partyAttachmentCount; i++) {
			partyAttachment = insuredParty.getAttachmentAt(i);
			if (NbaOliConstants.OLI_ATTACH_UNDRWRTNOTE == partyAttachment.getAttachmentType()) {
				if (partyAttachment.hasAttachmentData()) {
					insuredPartyFormal.addAttachment(partyAttachment);
				}
			}
		}		
	}
	
	//ALS4005 New Method
	protected void createXML103Source(NbaTXLife nbaTXLife) throws NbaBaseException, NbaNetServerDataNotFoundException {
		if (getWork().getXML103Source() == null) {
			getWork().addXML103Source(nbaTXLife);
		} else {
			getWork().updateXML103Source(nbaTXLife);
		}
	}
	
		//ALS4664 New Method
	protected void convertFaxedToPaper(NbaSearchResultVO resultVO) throws NbaBaseException, NbaVpmsException {
		NbaDst matchingCase = retrieveCaseWithTransactionsAndSources(resultVO.getWorkItemID());
		//Convert NBAPPLCTN source to NBINVLDAPP
		List matchingCaseSources = matchingCase.getNbaSources();
		for (int i = 0; i < matchingCaseSources.size(); i++) {
			NbaSource source = (NbaSource) matchingCaseSources.get(i);
			if (source.getSourceType().equalsIgnoreCase(NbaConstants.A_ST_APPLICATION)
					&& source.getNbaLob().getAppOriginType() == getWork().getNbaLob().getAppOriginType()) { //ALS5360
				source.getSource().setSourceType(NbaConstants.A_ST_INVALID_APPLICATION);
				source.getSource().setUpdate(NbaConstants.YES_VALUE);
				break;
			}
		}
		//Copy NBAPPLCTN source onto matching case
		List currentCaseSources = getWork().getNbaSources();
		for (int i = 0; i < currentCaseSources.size(); i++) {
			NbaSource source = (NbaSource) currentCaseSources.get(i);
			if (source.getSourceType().equalsIgnoreCase(NbaConstants.A_ST_APPLICATION)) {
				NbaSource newSource = source.clone(false);
				newSource.getSource().setLobData(source.getSource().getLobData());
				matchingCase.addNbaSource(newSource);
			}
		}
		//Create a comment for matching case
		NbaProcessingErrorComment npec = createComment();
		npec.setText("Paper application received - needs review by NBCM.");
		matchingCase.addManualComment(npec.convertToManualComment());
		//Update LOBs for matching case
		matchingCase.getNbaLob().setFaxedOrEmailedInd(false);
		matchingCase.setUpdate();
		try {
			WorkflowServiceHelper.update(getUser(), matchingCase);
			setLockedMatchingWork(matchingCase);
		} catch (NbaBaseException e) {
			throw e;
		}
		//Begin ALS4854 ALS4891
		if (!NbaUtils.isBlankOrNull(matchingCase.getNbaLob().getPolicyNumber())) {
			NbaTXLife matchingTXLife = doHoldingInquiry(matchingCase, NbaConstants.UPDATE, null);
			ApplicationInfoExtension appInfoExtension = NbaUtils.getFirstApplicationInfoExtension(matchingTXLife.getPolicy().getApplicationInfo());
			appInfoExtension.setSubmissionType(NbaOliConstants.OLI_APPSUBMITTYPE_MAIL);
			appInfoExtension.setActionUpdate();
			NbaContractAccess.doContractUpdate(matchingTXLife, matchingCase, getUser());
		}
		//End ALS4854 ALS4891
	}

	//ALS4752 new method
	protected NbaDst retrieveWorkItemComments(NbaDst nbaDst) throws NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("APFORMAL Starting retrieveWorkItemComments for " + nbaDst.getID());
		}
		AccelResult accelResult = (AccelResult) currentBP.callBusinessService("RetrieveCommentsBP", setRetrievalProperties(nbaDst,NbaGeneralComment.generalType));//APSL1438
		NewBusinessAccelBP.processResult(accelResult);
		return (NbaDst) accelResult.getFirst();

	}

	//ALS4752 new method
	protected NbaRetrieveCommentsRequest setRetrievalProperties(NbaDst work, String commentType) { //APSL1438
		NbaRetrieveCommentsRequest commentsReq = new NbaRetrieveCommentsRequest();
		commentsReq.setNbaDst(work);
		if (work.isCase()) {
			commentsReq.setRetrieveChildren(true);
		} else {
			commentsReq.setRetrieveChildren(false);
		}
		commentsReq.setCommentTypes(commentType); //APSL1438
		return commentsReq;
	}
	//ALS4742 New Method
	protected NbaTXLife getMatchingTXLife() {
		return matchingTXLife;
	}
	//ALS4742 New Method
	protected void setMatchingTXLife(NbaTXLife matchingTXLife) {
		this.matchingTXLife = matchingTXLife;
	}
	
	//ALS4665 new method
	protected void setExpireMIBCheckReq(List reqInfoList) {
        if (!NbaUtils.isBlankOrNull(reqInfoList)) {
            int size = reqInfoList.size();
            RequirementInfo reqInfo = null;
            for (int i = 0; i < size; i++) {
                reqInfo = (RequirementInfo)reqInfoList.get(i);
                if (NbaOliConstants.OLI_REQCODE_MIBCHECK == reqInfo.getReqCode()) {
                    reqInfo.setReqSubStatus(NbaOliConstants.OLI_REQSUBSTAT_CNCLINSCO);
                    reqInfo.setActionUpdate();
                }
            }
        }
    }
	
	//	APSL202 new method
	protected void setDisplayImagesInd(List reqInfoList) {
		for (int i = 0; i < reqInfoList.size(); i++) {
			RequirementInfoExtension reqInfoExtn = NbaUtils.getFirstRequirementInfoExtension((RequirementInfo) reqInfoList.get(i));
			if (reqInfoExtn != null) {
				reqInfoExtn.setDisplayImagesInd(false);
				reqInfoExtn.setActionUpdate();
			}
		}
	}
	//SR564247 Retrofit New Method 
	protected void resetStatusForPredictive () throws NbaBaseException{ 
		Map deOinkMap = new HashMap();
		deOinkMap.put("A_DeterminePredictive","true");
		statusProvider = new NbaProcessStatusProvider(getUser(), getWork(), null, deOinkMap);
	}
	//New Method APSL1490
	protected void resetSecureCommentsFromAttachments(NbaTXLife targetTXLife) {
		Attachment attachment = null;
		Party insuredParty = targetTXLife.getPrimaryParty().getParty();
		int partyAttachmentCount = targetTXLife.getPrimaryParty().getParty().getAttachmentCount();
		for (int i = 0; i < partyAttachmentCount; i++) {
			attachment = insuredParty.getAttachmentAt(i);
			if (NbaOliConstants.OLI_ATTACH_UNDRWRTNOTE == attachment.getAttachmentType()) {
				if (attachment.hasAttachmentData()) {
					filterSecureComment(attachment);
				}
			}
		}
		Holding holding = targetTXLife.getPrimaryHolding();
		int holdingAttachmentCount = holding.getAttachmentCount();
		for (int i = 0; i < holdingAttachmentCount; i++) {
			attachment = holding.getAttachmentAt(i);
			if (NbaOliConstants.OLI_ATTACH_UNDRWRTNOTE == attachment.getAttachmentType()) {
				filterSecureComment(attachment);
			}
		}
	}
	//New Method APSL1490
	protected void filterSecureComment(Attachment attachment) {
		AttachmentData attachmentData = attachment.getAttachmentData();
		if (attachmentData.hasPCDATA()) {
			try {
				SecureComment secure = SecureComment.unmarshal(new ByteArrayInputStream(attachmentData.getPCDATA().getBytes()));
				String commentText = NbaUtils.filterNonUTF8Chars(secure.getComment());
				secure.setComment(commentText);
				attachmentData.setPCDATA(NbaUtils.commentDataToXmlString(secure));
			} catch (Exception e) {
				try {
					String pcdata = attachmentData.getPCDATA();
					String beginComment = "<Comment>";
					String endComment = "</Comment>";
					StringBuffer newPcdata = new StringBuffer();
					String secureCommentText = null;
					if (pcdata.indexOf(beginComment) != -1 && pcdata.indexOf(endComment) != -1) {
						secureCommentText = pcdata.substring(pcdata.indexOf(beginComment) + beginComment.length(), pcdata.indexOf(endComment));
						if (secureCommentText.length() > 0) {
							secureCommentText = NbaUtils.filterNonUTF8Chars(secureCommentText);
							newPcdata.append(pcdata.substring(0, pcdata.indexOf(beginComment) + beginComment.length()));
							newPcdata.append(secureCommentText);
							newPcdata.append(pcdata.substring(pcdata.indexOf(endComment), pcdata.length()));
							attachmentData.setPCDATA(newPcdata.toString());
						}
					} else {
						getLogger().logDebug(
								"Unable to parse SecureComment XML for attachment: " + attachment.getId()
										+ ", while merging original securecomment from informal TXlife. " + e.getMessage());
					}

				} catch (Exception e1) {
					getLogger().logDebug(
							"Unable to parse SecureComment XML for attachment: " + attachment.getId()
									+ ", while merging original securecomment from informal TXlife. " + e1.getMessage());
				}
			}
		}
	}
	/**
	 	APSL1614 - New Method - reset Rate Class in formal txlife so that it is not carried over from informal to formal application
	**/
	public void resetRateClassAndSubstandardRating(NbaTXLife nbaTXLife){
		NbaParty primParty = nbaTXLife.getPrimaryParty();
		PersonExtension primPrsnExtension = NbaUtils.getFirstPersonExtension(primParty.getPerson());
		if(!NbaUtils.isBlankOrNull(primPrsnExtension)){
			primPrsnExtension.deleteRateClass();
			primPrsnExtension.deleteRateClassAppliedFor();
			primPrsnExtension.deleteRateClassOverrideInd();
		}
		LifeParticipant lifePart = nbaTXLife.getLifeParticipantFor(primParty.getID());
		if(!NbaUtils.isBlankOrNull(lifePart) && !NbaUtils.isBlankOrNull(lifePart.getSubstandardRating())){
			lifePart.getSubstandardRating().clear();
		}
	}
}