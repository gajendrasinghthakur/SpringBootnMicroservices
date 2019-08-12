package com.csc.fsg.nba.business.process;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaTransactionValidationException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

public class NbaProcGIApplicationMatch extends NbaAutomatedProcess {

	private NbaDst lockedMatchingWork;

	protected NbaDst nbaDstWithAllTransactions = null;

	private NbaTXLife matchingTXLife;

	private NbaOLifEId nbaOLifEId = null;

	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException, NbaAWDLockedException {
		if (!initialize(user, work)) {
			return getResult();
		}
		setWork(retrieveWorkItem(getWork()));
		try{ //NBLXA-2206
			findMatchingGICases();
			if (getResult() == null) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
				changeStatus(getResult().getStatus());
			}
			doUpdateWorkItem();
		}catch (NullPointerException nbe) {// Begin NBLXA-2206
			addComment("Employer Name is missing on Indexing");
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, nbe.getMessage(), getHostFailStatus()));
			changeStatus(getHostFailStatus());
			doUpdateWorkItem();
			if (getLockedMatchingWork() != null) {
				NbaContractLock.removeLock(getUser());
				unlockWork(getLockedMatchingWork());
			}
			return getResult();
			//END NBLXA-2206
		}


		if (getLockedMatchingWork() != null) {
			NbaContractLock.removeLock(getUser());
			unlockWork(getLockedMatchingWork());
		}
		return getResult();
	}

	/**
	 * This method retrieves a work item from AWD.
	 *
	 * @param nbaDst
	 *            a work item to be retrieved
	 * @return the retrieved work item
	 * @throws NbaBaseException
	 */
	protected NbaDst retrieveWorkItem(NbaDst nbaDst) throws NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("APFORMAL Starting retrieveWorkItem for " + nbaDst.getID());
		}
		NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
		retrieveOptionsValueObject.setWorkItem(nbaDst.getID(), false);
		retrieveOptionsValueObject.requestSources();
		retrieveOptionsValueObject.setLockTransaction();
		return retrieveWorkItem(getUser(), retrieveOptionsValueObject);
	}

	/**
	 * This method finds the duplicate cases for the current work. Duplicate case means a previous case in AWD who has the same app origin as of
	 * current case and which matches other criterion configured in VP/MS.
	 *
	 * @return
	 * @throws NbaBaseException
	 */
	public List findDuplicateWork() throws NbaBaseException {
		NbaSearchVO searchVO = lookupMatchingWork(NbaVpmsConstants.EP_GICASE_SEARCH_KEY, QUEUE_GI_CASE_HOLD);
		List duplicateCases = searchVO.getSearchResults();
		return duplicateCases;
	}

	// ALS4005 New Method
	protected boolean isDuplicateWorkFound(List duplicateWorks) {
		return (duplicateWorks.size() > 0) ? true : false;
	}

	/**
	 * Looks for matching work based on the criteria configured in VP/MS
	 *
	 * @param work
	 * @param user
	 * @param entryPoint
	 * @param searchQueue
	 * @param searchType
	 * @param appOrigin
	 * @return
	 * @throws NbaBaseException
	 */
	protected NbaSearchVO lookupMatchingWork(String entryPoint, String searchQueue) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		NbaVpmsResultsData data = getDataFromVpms(entryPoint);
		ArrayList lobList = data.getResultsData();
		NbaLob lookupLOBs = getNbaLobForLookup(lobList);
		lookupLOBs.setQueue(searchQueue);
		if (checkLobPresence(lookupLOBs, lobList)) {
			searchVO.setNbaLob(lookupLOBs);
			searchVO.setWorkType(NbaConstants.A_WT_APPLICATION);
			searchVO.setQueue(searchQueue);
			searchVO.setResultClassName("NbaSearchResultVO");
			searchVO = WorkflowServiceHelper.lookupWork(user, searchVO);
			if (searchVO.isMaxResultsExceeded()) {
				throw new NbaBaseException(NbaBaseException.SEARCH_RESULT_EXCEEDED_SIZE, NbaExceptionType.FATAL);
			}
		} else {
			searchVO.setSearchResults(new ArrayList());
		}

		return searchVO;
	}

	/**
	 * Retrieve criteria from VP/MS to match work
	 *
	 * @param work
	 * @param user
	 * @param entryPoint
	 * @param searchType
	 * @return
	 * @throws NbaBaseException
	 * @throws NbaVpmsException
	 */
	protected NbaVpmsResultsData getDataFromVpms(String entryPoint) throws NbaBaseException, NbaVpmsException {
		NbaVpmsAdaptor vpmsProxy = null;
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
			Map deOink = new HashMap();
			deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser()));
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsConstants.CONTRACTVALIDATIONCOMPANYCONSTANTS);
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			vpmsProxy.setSkipAttributesMap(deOink);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			return data;
		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException("GI App match" + NbaVpmsException.VPMS_EXCEPTION, re);
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
		NbaVpmsResultsData suspendData = getDataFromVpms(NbaVpmsAdaptor.EP_GET_MAX_SUSPEND_DAYS);
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
			lob.deleteAppHoldSuspDate();// ALS5060
			return true;
		}

		return false;
	}

	/**
	 * This method creates an NbaProcessingErrorComment object
	 *
	 * @return
	 */
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

	protected boolean mergeGIAppWithMatching(NbaSearchResultVO resultVO, NbaLob nbaLob) throws NbaBaseException, NbaVpmsException {
		boolean censusMatchedInd = false;
		NbaDst matchingCase = retrieveCaseWithTransactionsAndSources(resultVO.getWorkItemID());
		// Clone matchingCase to retrieve comments - RetrieveComments does not retrieves sources
		matchingTXLife = doHoldingInquiry(matchingCase, NbaConstants.UPDATE, NbaUtils.getBusinessProcessId(getUser()));
		// Copy matching to current
		copyLobs(matchingCase.getNbaLob(), nbaLob);
		if (NbaSystemDataDatabaseAccessor.isGIAppMatchedFromGICaseTemplate(nbaLob.getDOB(),nbaLob.getSsnTin(), nbaLob.getEmployerName())) {
			censusMatchedInd = true;
			getLogger().logError("Census data Matched for GI Application ");
		} else {
			getLogger().logError("Census data not found for GI Application ");
		}
		createXML103(matchingTXLife, censusMatchedInd, matchingCase);
		// WorkflowServiceHelper.update(getUser(), matchingCase);
		setLockedMatchingWork(matchingCase);
		return censusMatchedInd;
	}

	protected NbaDst retrieveCaseWithTransactionsAndSources(String workItemID) throws NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("A2GAPMCH Starting retrieveWorkItem");
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

	protected void createXML103(NbaTXLife sourceTXLife, boolean censusMatchedInd, NbaDst matchingCase) throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_NEWBUSSUBMISSION);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setNbaLob(getWorkLobs());
		nbaTXRequest.setNbaUser(getUser());
		NbaTXLife nbaTXLife = new NbaTXLife(nbaTXRequest);
		setNbaTxLife(nbaTXLife);
		setNbaOLifEId(new NbaOLifEId(nbaTXLife));
		createOtherHolding(sourceTXLife);
		nbaTXLife.getPrimaryHolding().setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);
		Policy policy = nbaTXLife.getPrimaryHolding().getPolicy();
		Policy giCasePolicy = sourceTXLife.getPolicy();
		createPolicyInfo(policy, giCasePolicy);
		Life life = getLife(policy);
		createApplicationInfo(policy, giCasePolicy, censusMatchedInd);
		Life giCaseLife = getLife(giCasePolicy);
		life.setCoverage(giCaseLife.getCoverage());
		NbaUtils.getBaseCoverage(life).setProductCode(policy.getProductCode());//NBLXA-2105
		nbaTXLife.getOLifE().setParty(sourceTXLife.getOLifE().getParty());
		createRelations(nbaTXLife.getOLifE(), sourceTXLife.getOLifE().getRelation());
		createPrimaryInsuredParty(nbaTXLife); //NBLXA-1426
		getNbaOLifEId().resetIds(nbaTXLife);
		createXML103Source(nbaTXLife);
		updatePendingContractStatusOfGICase(sourceTXLife, giCasePolicy, matchingCase);
	}

	//NBLXA-1426 : New Method
	private void createPrimaryInsuredParty(NbaTXLife nbaTXLife) throws NbaBaseException {
		//getWork()
		Party party = new Party();
		party.setActionAdd();
		party.setId("Party_032_01");
		party.setPartyTypeCode(NbaOliConstants.OLI_PT_PERSON);
		party.setGovtID(getWork().getNbaLob().getSsnTin());
		party.setGovtIDTC(NbaOliConstants.OLI_GOVTID_SSN);
		party.setPersonOrOrganization(new PersonOrOrganization());
		party.getPersonOrOrganization().setActionAdd();
		party.getPersonOrOrganization().setPerson(new Person());
		party.getPersonOrOrganization().getPerson().setActionAdd();
		party.getPersonOrOrganization().getPerson().setFirstName(getWork().getNbaLob().getFirstName());
		party.getPersonOrOrganization().getPerson().setLastName(getWork().getNbaLob().getLastName());
		party.getPersonOrOrganization().getPerson().setBirthDate(getWork().getNbaLob().getDOB());
		nbaTXLife.getOLifE().addParty(party);

		Relation newRelation = new Relation();
		newRelation.setActionAdd();
		newRelation.setRelationRoleCode(NbaOliConstants.OLI_REL_INSURED);
		newRelation.setOriginatingObjectID(nbaTXLife.getPrimaryHolding().getId());
		newRelation.setRelatedObjectID(party.getId());
		newRelation.setOriginatingObjectType(NbaOliConstants.OLI_HOLDING);
		newRelation.setRelatedObjectType(NbaOliConstants.OLI_PARTY);
		newRelation.setRelatedRefID("01");
		getNbaOLifEId().setId(newRelation);
		nbaTXLife.getOLifE().addRelation(newRelation);

		Life life = getLife(nbaTXLife.getPrimaryHolding().getPolicy());
		List covLst = life.getCoverage();
		Iterator itrCov = covLst.iterator();
		Coverage cov = null;
		CovOption covOpt = null;
		List covOptLst = null;
		List lPartLst = null;
		LifeParticipant lPart = null;
		while(itrCov.hasNext()){
			cov = (Coverage)itrCov.next();
			if(cov != null){
				lPartLst = cov.getLifeParticipant();
				if(lPartLst != null && lPartLst.size() > 0){
					Iterator itrLifePart = lPartLst.iterator();
					while(itrLifePart.hasNext()){
						lPart = (LifeParticipant)itrLifePart.next();
						if(!(lPart != null && lPart.getPartyID() != null && lPart.getPartyID().equalsIgnoreCase(party.getId()))){
						 lPart.setPartyID(party.getId());
						}
					}
				} else {
					lPart = new LifeParticipant();
					lPart.setPartyID(party.getId());
					getNbaOLifEId().setId(lPart);
					lPart.setLifeParticipantRoleCode(NbaOliConstants.OLI_PARTICROLE_PRIMARY);
					lPart.setActionAdd();
					cov.addLifeParticipant(lPart);
				}
			}

			lPart = nbaTXLife.getPrimaryInuredLifeParticipant();
			covOptLst = cov.getCovOption();
			if(lPart != null && covOptLst != null && covOptLst.size() > 0){
				Iterator itrCovOpt = covOptLst.iterator();
				while (itrCovOpt.hasNext()){
					covOpt = (CovOption)itrCovOpt.next();
					if(covOpt != null && !(covOpt.getLifeParticipantRefID() != null && covOpt.getLifeParticipantRefID().equalsIgnoreCase(lPart.getId()))){
						covOpt.setLifeParticipantRefID(lPart.getId());
						covOpt.setActionUpdate();
					}
				}
			}
		}
	}

	protected void createXML103() throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_NEWBUSSUBMISSION);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setNbaLob(getWorkLobs());
		nbaTXRequest.setNbaUser(getUser());
		NbaTXLife nbaTXLife = new NbaTXLife(nbaTXRequest);
		setNbaTxLife(nbaTXLife);
		nbaTXLife.getPrimaryHolding().setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);
		Policy policy = nbaTXLife.getPrimaryHolding().getPolicy();
		ApplicationInfo appInfo = new ApplicationInfo();
		appInfo.setApplicationType(NbaOliConstants.OLI_APPTYPE_GROUPAPP);
		policy.setApplicationInfo(appInfo);
		OLifEExtension olifeExtn = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_APPLICATIONINFO);
		ApplicationInfoExtension appInfoExt = olifeExtn.getApplicationInfoExtension();
		appInfo.addOLifEExtension(olifeExtn);
		appInfoExt.setGiAppMatchingInd(false);
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladhpc = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
		Life aLife = new Life();
		policy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(ladhpc);
		policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().setLife(aLife);
		createXML103Source(nbaTXLife);
	}

	/**
	 * @param sourceTXLife
	 */
	// ALS5215 New Method
	private void createOtherHolding(NbaTXLife sourceTXLife) {
		List otherHolding = NbaUtils.getOtherHolding(sourceTXLife); // P2AXAL037
		for (int i = 0; i < otherHolding.size(); i++) {
			getNbaTxLife().getOLifE().addHolding((Holding) otherHolding.get(i));
		}
	}

	protected void createPolicyInfo(Policy policy, Policy giCasePolicy) throws NbaBaseException {
		policy.setProductType(getWorkLobs().getProductTypSubtyp());
		policy.setPaymentMethod(giCasePolicy.getPaymentMethod());
		OLifEExtension olifeExtn = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
		PolicyExtension policyExtension = olifeExtn.getPolicyExtension();
		policy.addOLifEExtension(olifeExtn);
		policyExtension.setProductSuite(getWorkLobs().getPlanType());
		policyExtension.setDistributionChannel(getWorkLobs().getDistChannel());
		PolicyExtension gIPolicyExt = NbaUtils.getFirstPolicyExtension(giCasePolicy);
		policyExtension.setGuarIssOfferNumber(gIPolicyExt.getGuarIssOfferNumber());
		policyExtension.setPrintTogetherIND(gIPolicyExt.getPrintTogetherIND());
		policyExtension.setTemplateID(gIPolicyExt.getTemplateID());
		policyExtension.setBusinessStrategiesInd(getWorkLobs().getBusinessStrategyInd());//NBLXA-1823
		//NBLXA-1632 Start
		if(NbaUtils.isProductCOIL(giCasePolicy.getProductCode())){ //NBAXA-2528
			policyExtension.setCaseAdminReqInd(!gIPolicyExt.getCaseAdminReqInd());
		}
		//NBLXA-1632 End
		PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(policy);
		//ESLI product: BatchId creation exclusion,NBLXA-1528
		if (null != policyExt && !NbaUtils.isESLIProduct(getNbaTxLife()) && policyExt.getPrintTogetherIND()) {
			String batchID = NbaSystemDataDatabaseAccessor.getBatchIDOfEmployerThroughEndDate(getWorkLobs().getEmployerName());
			if(NbaUtils.isBlankOrNull(batchID)){
//				String formattedDate = NbaUtils.getGIDateFromString(getWorkLobs().getScanDate());
//				String currentDate = NbaUtils.getGIDateFromString(new Date());
//				policyExt.setGIBatchID(formattedDate + getWorkLobs().getEmployerName() + "_" +  currentDate);
				long currentDateInMilliSeconds = System.currentTimeMillis()/1000;
				policyExt.setGIBatchID(String.valueOf(currentDateInMilliSeconds));
				NbaSystemDataDatabaseAccessor.insertGIAppBatchID(policyExt.getGIBatchID(), getWork().getNbaLob().getEmployerName(), NbaUtils.getEndDateForGIAppBatchID());
			}else{
				policyExt.setGIBatchID(batchID);
			}
		}
	}

	// ALS4005 New Method
	protected void createCoverage(NbaTXLife informalTXLife, Policy policy, Life life) throws NbaBaseException {
		NbaOLifEId nbaOLifEId = new NbaOLifEId(informalTXLife);
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
	}

	protected Life getLife(Policy policy) { //NBLXA-1426 : Method refactor
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladhpc = null;
		Life life = null;
		if(policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() == null){
			ladhpc = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
			policy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(ladhpc);
			life = new Life();
			//getNbaOLifEId().setId(life);
			policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().setLife(life);
		} else{
			ladhpc = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
			life = ladhpc.getLife();
		}
		return life;
	}

	protected void createApplicationInfo(Policy policy, Policy giCasePolicy, boolean censusMatchedInd) throws NbaBaseException {
		ApplicationInfo appInfo = new ApplicationInfo();
		appInfo.setActionAdd();
		appInfo.setApplicationType(NbaOliConstants.OLI_APPTYPE_GROUPAPP);
		//appInfo.setTrackingID(giCasePolicy.getPolNumber());
		appInfo.setSignedDate(getWorkLobs().getAppDate()); // AXAL3.7.03
		appInfo.setApplicationJurisdiction(getWorkLobs().getAppState()); //NBLXA-2255
		OLifEExtension olifeExtn = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_APPLICATIONINFO);
		ApplicationInfoExtension appInfoExt = olifeExtn.getApplicationInfoExtension();
		appInfo.addOLifEExtension(olifeExtn);
		appInfoExt.setApplicationOrigin(NbaOliConstants.OLI_APPORIGIN_FORMAL); // NBLXA-2522
		ApplicationInfoExtension informalAppInfoExt = NbaUtils.getFirstApplicationInfoExtension(giCasePolicy.getApplicationInfo());
		if (informalAppInfoExt != null) {
			appInfoExt.setInformalOfferDate(informalAppInfoExt.getInformalOfferDate());
			// InformalApplicationDate of informal application is its signed date
			appInfoExt.setInformalApplicationDate(giCasePolicy.getApplicationInfo().getSignedDate());
			appInfoExt.setSubFirmIndicator(informalAppInfoExt.getSubFirmIndicator());
		}
		appInfoExt.setApplicationSubType(getWorkLobs().getInformalAppType());
		appInfoExt.setScanStation(getWorkLobs().getCreateStation());
		appInfoExt.setGiAppMatchingInd(censusMatchedInd);
		policy.setApplicationInfo(appInfo);
	}

	// ALS4005 New Method
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

	// ALS4005 New Method
	protected void createXML103Source(NbaTXLife nbaTXLife) throws NbaBaseException, NbaNetServerDataNotFoundException {
		if (getWork().getXML103Source() == null) {
			getWork().addXML103Source(nbaTXLife);
		} else {
			getWork().updateXML103Source(nbaTXLife);
		}
	}

	/**
	 * This method finds matching GI cases for the current case. The criterion for matching are configured in VP/MS.
	 *
	 * @return
	 * @throws NbaBaseException
	 */
	protected void findMatchingGICases() throws NbaBaseException {
		NbaLob nbaLob = getWork().getNbaLob();
		List giCases = null;
		NbaSource appSource = getWork().getNbaCase().getApplicationSource();
		NbaLob appSourceLob = appSource.getNbaLob();
		nbaLob.setEmployerName(appSourceLob.getEmployerName());
		nbaLob.setBackendSystem(appSourceLob.getBackendSystem());
		NbaSearchVO searchVO = lookupMatchingWork(NbaVpmsConstants.EP_GICASE_SEARCH_KEY, QUEUE_GI_CASE_HOLD);
		giCases = searchVO.getSearchResults();
		processMatchingGICases(giCases, nbaLob);
	}

	protected void processMatchingGICases(List giCases, NbaLob nbaLob) throws NbaBaseException {
		boolean matchInd = false;
		if (null != giCases) {
			nbaLob.setApplicationType(String.valueOf(NbaOliConstants.OLI_APPTYPE_GROUPAPP));
			getLogger().logDebug("Total Matched Cases found " + giCases.size());
			if (giCases.size() == 1) {
				matchInd = mergeGIAppWithMatching((NbaSearchResultVO) giCases.get(0), nbaLob);
			} else {
				if (giCases.size() > 1) {
					addComment("Multiple match found, Case routed to application entry");
					getLogger().logDebug("Multiple match found, Case routed to application entry");
				}
			}
		} else {
			getLogger().logDebug("No Matched Cases found ");
		}
		if(!matchInd){
			Map deOinkMap = new HashMap();
	        deOinkMap.put("A_GIAPPMATCHINGIND",String.valueOf(matchInd));
	        NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(getUser(), getWork(),deOinkMap);
	        setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", statusProvider.getFailStatus()));
	        changeStatus(getResult().getStatus(),statusProvider.getReason());

		}
	}

	protected NbaTXRequestVO createRequestObject(NbaSearchResultVO searchResult, String businessProcess) {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTALL);
		nbaTXRequest.setNbaLob(searchResult.getNbaLob());
		nbaTXRequest.setNbaUser(getUser());
		nbaTXRequest.setWorkitemId(searchResult.getWorkItemID());
		nbaTXRequest.setCaseInd(searchResult.isCase());
		nbaTXRequest.setAccessIntent(READ);
		nbaTXRequest.setBusinessProcess(businessProcess);
		return nbaTXRequest;
	}

	protected void suspendWorkItem() throws NbaBaseException, NbaVpmsException {
		if (!isAllowableSuspendDaysOver(getWorkLobs())) {
			NbaVpmsResultsData data = null;
			data = getDataFromVpms(NbaVpmsAdaptor.EP_GET_SUSPEND_ACTIVATE_DATE);
			if (data.getResultsData() != null && data.getResultsData().size() > 0) {
				String activateDate = (String) data.getResultsData().get(0);
				suspendWorkItem("Suspended waiting for informal match", NbaUtils.getDateFromStringInAWDFormat(activateDate));
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "Suspended"));
			}
		} else {
			addComment("Informal contract not received");
			NbaProcessingErrorComment npec = createComment();
			npec.setText("No matches found for the case.");
			getWork().addManualComment(npec.convertToManualComment());
			if (getWorkLobs() != null && NbaOliConstants.OLI_APPORIGIN_PARTIAL == getWorkLobs().getAppOriginType())
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getAlternateStatus()));
			else
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getFailStatus()));
		}
	}

	/**
	 * @return Returns the nbaDstWithAllTransactions. APSL4417
	 */

	public NbaDst getNbaDstWithAllTransactions() {
		return nbaDstWithAllTransactions;
	}

	/**
	 * @param nbaDstWithAllTransactions
	 *            The nbaDstWithAllTransactions to set.
	 */
	public void setNbaDstWithAllTransactions(NbaDst nbaDstWithAllTransactions) {
		this.nbaDstWithAllTransactions = nbaDstWithAllTransactions;
	}

	/**
	 * @purpose This method will copy the GI Case LOB to GI APP LOB
	 */
	public static void copyLobs(NbaLob sourceLobs, NbaLob destLobs) throws NbaBaseException {
		//destLobs.setCompany(sourceLobs.getCompany()); NBLXA-1440
		destLobs.setOfferNumber(sourceLobs.getOfferNumber());
		//destLobs.setPlan(sourceLobs.getPlan());//NBLXA-2105
		destLobs.setProductTypSubtyp(sourceLobs.getProductTypSubtyp());
		destLobs.setUndwrtQueue(sourceLobs.getUndwrtQueue());
		destLobs.setCaseManagerQueue(sourceLobs.getCaseManagerQueue());
		destLobs.setAgentID(sourceLobs.getAgentID());
//		String dobFromCensus = NbaSystemDataDatabaseAccessor.selectDOBFromMatchedCensusData(destLobs.getSsnTin(), destLobs.getEmployerName());
//		destLobs.setDOB(NbaUtils.getGIDateFromString(dobFromCensus));
	}

	/**
	 * @throws NbaBaseException
	 * @purpose This method will set the pendingContractStatus to "Sold" if certain conditions get passed and will update the TXLife of GI Case
	 */
	protected void updatePendingContractStatusOfGICase(NbaTXLife sourceTXLife, Policy policy, NbaDst dst) throws NbaBaseException {
		PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(policy);
		if (policyExt != null && (policyExt.getPendingContractStatus() != NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_1009800002)) {
			policyExt.setPendingContractStatus(NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_1009800002);// Reset the Pending contract status for contract

			//NBLXA-1632 Start
			if(!policyExt.getCaseAdminReqInd() && NbaUtils.isProductCOIL(policy.getProductCode())){ //NBLXA-2528
				policyExt.setCaseAdminReqInd(true);
			}
			//NBLXA-1632 End
			policyExt.setActionUpdate();
			try {
				sourceTXLife.setAccessIntent(UPDATE); // SPR1851
				sourceTXLife = NbaContractAccess.doContractUpdate(sourceTXLife, dst, getUser());
				handleHostResponse(sourceTXLife);
			} catch (NbaBaseException nbe) {
				/*
				 * If the transaction validation fails the returned error messages will be added to the work item being processed, the status of the
				 * work item being processed will be changed to validation error (VALDERRD) sending the work item to the error queue.
				 */
				if (nbe instanceof NbaTransactionValidationException) {
					handleTransactionValidationErrors(nbe.getMessage());
				} else {
					throw nbe;
				}
			}
		}
	}

	/**
	 * @return the nbaOLifEId
	 */
	public NbaOLifEId getNbaOLifEId() {
		return nbaOLifEId;
	}

	/**
	 * @param nbaOLifEId the nbaOLifEId to set
	 */
	public void setNbaOLifEId(NbaOLifEId nbaOLifEId) {
		this.nbaOLifEId = nbaOLifEId;
	}
}
