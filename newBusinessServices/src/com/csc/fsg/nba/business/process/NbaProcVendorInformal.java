package com.csc.fsg.nba.business.process;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import java.util.ListIterator;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;

public class NbaProcVendorInformal extends NbaAutomatedProcess {
	private String awdTime; //APSL4926
	private boolean isSuspend = false; //APSL4926
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException, NbaAWDLockedException {
		// TODO Auto-generated method stub

		if (!initialize(user, work)) {
			return getResult();
		}
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false);
		retOpt.requestTransactionAsChild();
		retOpt.requestSources();
		retOpt.setLockWorkItem();
		setWork(retrieveWorkItem(getUser(), retOpt));
		setAwdTime(getTimeStamp(user)); //APSL4926
		boolean doContractUpdate = true;
		int decisionValue = getWork().getNbaLob().getCaseFinalDispstn();
		if (isSuspensionNeeded()) { //APSL4926
			suspendWork(Calendar.HOUR,
					Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.VNDR_SUSPEND_DAYS)));
		} else {
		updateFinalDispositionFields(decisionValue);
		updateRequirementsReviewInd();
		updateRatingAndRateClass();
		if (doContractUpdate) {
			nbaTxLife = doContractUpdate();
			handleHostResponse(nbaTxLife);
		}
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		changeStatus(getResult().getStatus());
		doUpdateWorkItem();
		return getResult();
	}
		doUpdateWorkItem(); //APSL4926
		return getResult(); //APSL4926
	}

	protected void updateFinalDispositionFields(int decision) throws NbaBaseException {
		ApplicationInfo appInfo = null;
		ApplicationInfoExtension appInfoExt = null;
		PolicyExtension policyExt = null;
		if (getNbaTxLife() == null || getNbaTxLife().getPolicy() == null) {
			return;
		}
		appInfo = getNbaTxLife().getPolicy().getApplicationInfo();
		appInfoExt = NbaUtils.getAppInfoExtension(appInfo);
		policyExt = NbaUtils.getPolicyExtension(getNbaTxLife().getPolicy());
		appInfoExt.setVendorIndCode(getWork().getNbaLob().getRiskRighterCase());
		if (decision == NbaOliConstants.OLI_POLSTAT_OFFERACCEPTED) {
			appInfoExt.setInformalAppApproval(NbaOliConstants.OLIX_INFORMALAPPROVAL_OFFERPENDING);
			appInfoExt.setInformalOfferDate(getWork().getNbaLob().getOfferDate());
			policyExt.setPendingContractStatus(String.valueOf(NbaOliConstants.NBA_PCS_9111));

		} else if (decision == NbaOliConstants.OLI_POLSTAT_DEFERRED) {
			getNbaTxLife().getPrimaryHolding().getPolicy().setPolicyStatus(NbaOliConstants.OLI_POLSTAT_DEFERRED);
			appInfoExt.setUnderwritingStatus(NbaOliConstants.NBA_FINALDISPOSITION_POSTPONED);
			policyExt.setPendingContractStatus(NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_0004);

		} else if (decision == NbaOliConstants.OLI_POLSTAT_DECISSUE) {
			getNbaTxLife().getPrimaryHolding().getPolicy().setPolicyStatus(NbaOliConstants.OLI_POLSTAT_DECISSUE);
			appInfoExt.setUnderwritingStatus(NbaOliConstants.NBA_FINALDISPOSITION_DECLINED);
			policyExt.setPendingContractStatus(NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_0002);
		}
		if (!appInfoExt.isActionAdd()) {
			appInfoExt.setActionUpdate();
		}
		if (!policyExt.isActionAdd()) {
			policyExt.setActionUpdate();
		}
		if (!getNbaTxLife().getPrimaryHolding().getPolicy().isActionAdd()) {
			getNbaTxLife().getPrimaryHolding().getPolicy().setActionUpdate();
		}

	}

	protected void updateRequirementsReviewInd() throws NbaNetServerDataNotFoundException {
		ListIterator li = getWork().getNbaTransactions().listIterator();
		RequirementInfo reqInfo = null;
		while (li.hasNext()) {
			NbaTransaction trans = (NbaTransaction) li.next();
			if (A_WT_REQUIREMENT.equalsIgnoreCase(trans.getTransaction().getWorkType())) {
				String reqInfoUniqueId = trans.getNbaLob().getReqUniqueID();
				reqInfo = getNbaTxLife().getRequirementInfo(reqInfoUniqueId);
				RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
				if (reqInfoExt != null) {
					reqInfoExt.setReviewedInd(true);
					reqInfoExt.setReviewID(user.getUserID());
					reqInfoExt.setReviewDate(new Date());
					reqInfoExt.setReviewCode(String.valueOf(NbaConstants.REVIEW_NOT_REQUIRED));
					reqInfoExt.setActionUpdate();
					trans.getNbaLob().setReview(NbaConstants.REVIEW_SYSTEMATIC);
				}
				trans.setActionUpdate();
			}
		}
	}
	
	protected void updateRatingAndRateClass() throws NbaBaseException {
		String approvedrateClass = getWork().getNbaLob().getRateClass();
		List insuredList = nbaTxLife.getInsurableParties();
		for (int i = 0; i < insuredList.size(); i++) {
			Party party = (Party) insuredList.get(i);
			if (party.getPersonOrOrganization() != null && party.getPersonOrOrganization().isPerson()) {
				PersonExtension personExtension = NbaUtils.getFirstPersonExtension(party.getPersonOrOrganization().getPerson());
				if (personExtension != null) {
					personExtension.setRateClass(approvedrateClass);
					personExtension.setApprovedRateClass(approvedrateClass);
					personExtension.setActionUpdate();
				}
			}
		}
		Coverage baseCoverage = nbaTxLife.getPrimaryCoverage();
		if (baseCoverage != null) {
			CoverageExtension baseCoverageExt = NbaUtils.getFirstCoverageExtension(baseCoverage);
			if (baseCoverageExt == null) {
				OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaUtils.EXTCODE_COVERAGE);
				baseCoverage.addOLifEExtension(olifeExt);
				baseCoverageExt = olifeExt.getCoverageExtension();
				olifeExt.getCoverageExtension().setActionAdd();
			}
			baseCoverageExt.setRateClass(approvedrateClass);
			if (!baseCoverageExt.isActionAdd()) {
				baseCoverageExt.setActionUpdate();
			}
			int permanentRating = getWork().getNbaLob().getPermanentTableRating(); // APSL4915 
			if (!(permanentRating == 0)) {  // APSL4915 
				createSubstandardRating(baseCoverage, permanentRating);
			}
		}
	}
	
	protected void createSubstandardRating(Coverage baseCoverage, long permanentRating) {
		SubstandardRatingExtension substandardRatingExtension = null;
		LifeParticipant lifeParticipant = NbaUtils.findPrimaryInsuredLifeParticipant(baseCoverage);
		if (lifeParticipant != null) {
			int substandardRatingCount = lifeParticipant.getSubstandardRatingCount();
			if (substandardRatingCount == 0) {
				substandardRatingExtension = NbaUtils.getsubstandardRating(lifeParticipant, permanentRating, getNbaOLifEId());
				if (substandardRatingExtension != null) {
					substandardRatingExtension.setEffDate(getNbaTxLife().getPolicy().getEffDate());
					substandardRatingExtension.setRatingStatus(NbaOliConstants.OLI_POLSTAT_ACTIVE);
					substandardRatingExtension.setInsRatedInd(true);
					substandardRatingExtension.setActionUpdate();
				}
			}
		}
	}
	
	//APSL4926
	protected boolean isSuspensionNeeded() throws NbaNetServerDataNotFoundException {
		ListIterator li = getWork().getNbaTransactions().listIterator();
		while (li.hasNext()) {
			NbaTransaction trans = (NbaTransaction) li.next();
			if (A_WT_MISC_MAIL.equalsIgnoreCase(trans.getTransaction().getWorkType())
					|| A_WT_REQUIREMENT.equalsIgnoreCase(trans.getTransaction().getWorkType())) {
				if (!END_QUEUE.equalsIgnoreCase(trans.getQueue())) {
					isSuspend = true;
				}
			}
		}
		return isSuspend;
	}

	//APSL4926
	protected void suspendWork(int durationType, int durationValue) throws NbaBaseException {
		NbaSuspendVO suspendItem = null;
		suspendItem = getSuspendWorkVO(durationType, durationValue);
		addComment("OutStanding Miscmail Workitems are present- Needs resolution");
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
		if (suspendItem != null) {
			suspendWork(suspendItem);
		}
	}
	//APSL4926
	protected NbaSuspendVO getSuspendWorkVO(int type, int value) {
		GregorianCalendar cal = new GregorianCalendar();
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		cal.setTime(parseTimeStamp(getAwdTime()));
		cal.add(type, value);
		suspendVO.setTransactionID(getWork().getID());
		suspendVO.setActivationDate(cal.getTime());
		suspendVO.setKeepLock(false);
		return suspendVO;
	}

	/**
	 * @return the awdTime
	 */
	public String getAwdTime() {
		return awdTime;
	}

	/**
	 * @param awdTime the awdTime to set
	 */
	public void setAwdTime(String awdTime) {
		this.awdTime = awdTime;
	}
	
	//APSL4926
	protected Date parseTimeStamp(String dateForm) {
		try {
			java.util.Date date = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS").parse(dateForm);
			return date;
		} catch (java.text.ParseException e) {
			return new Date();
		}
	}
	

}
