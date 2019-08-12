package com.csc.fsg.nba.access.contract;
/*
 * *******************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */
import java.util.ArrayList;
import java.util.List;

import com.csc.fsg.nba.backendadapter.NbaBackEndAdapterFacade;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaActionIndicator;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Endorsement;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Participant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.Rider;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;

/**
 * @NbaBackendTransactionProcesser
 * <p>
 * This class provides services to the NbaContractAccess to retrieve information
 *  from the datastore and to insert, update and delete information in the datastore.
 * <br>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA050</td><td>Version 3</td><td>Nba Pending Contract Database</td></tr>
 * <tr><td>NBA036</td><td>Version 3</td><td>Nba Underwriter Workbench Transactions to Database</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecure changes</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging enchancement</td></tr>
 * <tr><td>SPR1186</td><td>Version 4</td><td>Incorrect table used for Roles for Annuities</td></tr>
 * <tr><td>SPR2092</td><td>Version 4</td><td>Unable to add SubStandardRting to CovOption</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change etc.</td></tr>
 * <tr><td>ACN013</td><td>Version 4</td><td>UW Mortality</td></tr>
 * <tr><td>SPR2278</td><td>Version 5</td><td>Endorsements not appearing in UW in wrappered mode.</td></tr>
 * <tr><td>SPR1163</td><td>Version 5</td><td>Adding Endorsements to an Annuity does not work</td></tr>
 * <tr><td>SPR2799</td><td>Version 5</td><td>Exceptions when denying Coverages or Benefits.</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>SPR3217</td><td>Version 6</td><td>Correct business functions for Underwriter Workbench</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR3605</td><td>Version 8</td><td>An insured Relation for a non-Primary Holding causes processing errors.</td></tr>
 * <tr><td>NBA231</td><td>Version 8</td><td>Replacement Processing</td></tr>
 * </table>
 *
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaBackendTransactionProcesser implements NbaConstants, NbaOliConstants {
	//	public final static int READ = 0;
	//	public final static int UPDATE = 1;
	protected static NbaLogger logger = null;
	/**
	* Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	* @return com.csc.fsg.nba.foundation.NbaLogger
	*/
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaBackendTransactionProcesser.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaContractAccessHelper could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/** This method updates the contract by interrogating the objects within the NbaTXLife
	 * object to determine which need to be processed.
	 * <br>Modifications to the object will have been made by  the originating process 
	 * prior to entry into this module.  The action indicator of each object is evaluated to 
	 * determine if the object has been modified, added or deleted.  If so, then an appropriate
	 * request will be generated for submission to the back end adapter and the request
	 * will be processed.  Results from the request will be stored in the NbaTXLife object
	 * and all will be returned.
	 * @param contract an NbaTXLife object containing the updates to be performed
	 * @param request an NbaTXRequestVO containing information about the work item
	 *  and user requesting the updates
	 * @return an updated NbaTXLife object containing results from the update requests
	 */
	public static NbaTXLife processContractUpdate(NbaTXLife contract, NbaTXRequestVO request) throws NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("processContractUpdate: " + contract.toXmlString());
		}
		String businessProcess = contract.getBusinessProcess();	//NBA093
		// New Application - processed alone
		if (businessProcess.equalsIgnoreCase(PROC_APP_SUBMIT)) {	//NBA093
			if (!isReg60(contract))  {//NBA231
			if (contract.getTXLife().isUserAuthRequestAndTXLifeRequest()) {
				if (contract.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getTransType() // SPR3290
					== TC_TYPE_NEWBUSSUBMISSION) {
					// application
					return processUpdateRequest(contract, contract);
				} else {
					throw new NbaBaseException("Unable to submit application to host.");
				}
			} //NBA231
			}
		}
		OLifE olife = contract.getOLifE();
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); //NBA044

		// Begin NBA036
		if (businessProcess.equalsIgnoreCase(PROC_UW_APPLY_CLIENT)) {	//NBA093
			List parties = contract.getOLifE().getParty();
			for (int i = 0; i < parties.size(); i++) {
				Party party = (Party) parties.get(i);
				if (party.isActionUpdate() && party.getPartyTypeCode() == OLI_PT_PERSON) {
					if (request != null) {
						request.setTransType(TC_TYPE_RATECLASSCHANGE);
						request.setObjectType(OLI_PARTY);
						request.setTranContentCode(TC_CONTENT_UPDATE);
						request.setTransMode(TC_MODE_UPDATE);
					}
					NbaTXLife newNbaTXLife = new NbaTXLife(request);
					if (newNbaTXLife == null) {
						return contract;
					} // transfer the appropriate data
					newNbaTXLife.getOLifE().addParty(party); // add the party
					List relations = contract.getOLifE().getRelation();
					for (int n = 0; n < relations.size(); n++) { // add the appropriate relations - if the party is related to the Holding
						Relation relation = (Relation) relations.get(n);
						if (relation.getRelatedObjectID().equals(party.getId()) && relation.getOriginatingObjectType() == OLI_HOLDING //SPR3605
								&& NbaConstants.PRIMARY_HOLDING_ID.equals(relation.getOriginatingObjectID())) { //SPR3605
							newNbaTXLife.getOLifE().addRelation(relation);
						}
					}
					newNbaTXLife.getOLifE().setHoldingAt(holding, 0); //Used to determine product type for Roles //SPR1186 
					NbaTXLife response =
						new NbaBackEndAdapterFacade().submitRequestToHost(newNbaTXLife, createRequestMessage(contract, request).getWorkitemId());
					TransResult result =
						response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();
					updateTransResult(contract, result);	//ACN013
					if (result.getResultCode() == 5) {
						ArrayList errs = new ArrayList();	//ACN013
						errs.add(result);	//ACN013
						party.applyResult(NbaActionIndicator.ACTION_FAILED, errs);	//ACN013
					} else {
						party.applyResult(NbaActionIndicator.ACTION_SUCCESSFUL, new ArrayList());	//ACN013
					}
				}
			}
			return contract;
		}

		if (businessProcess.equalsIgnoreCase(PROC_UW_APPLY_COVERAGE_BENEFITS) || businessProcess.equalsIgnoreCase (PROC_AUTO_UNDERWRITING)) {	//NBA093 ACN013
			applyCoverageBenefits(contract, request);
			return contract;
		}

		//SPR3217
		// End NBA036

		// Contract Approval from Underwriter Workbench
		//Begin NBA050
		if (businessProcess.equalsIgnoreCase(PROC_UW_APPROVE_CONTRACT)) {	//NBA093
			if (request != null) {
				request.setTransType(TC_TYPE_APPROVE);
				request.setTransMode(TC_MODE_UPDATE);
				request.setObjectType(OLI_POLICY);
				request.setTranContentCode(TC_CONTENT_UPDATE);
			}
			NbaTXLife newNbaTXLife = new NbaTXLife(request);
			if (newNbaTXLife == null) {
				throw new NbaBaseException("Unable to Create Request");
			}
			holding = newNbaTXLife.getPrimaryHolding();
			Policy policy = contract.getPrimaryHolding().getPolicy(); //changed
			policy.resetTransactionErrors();
			policy.getApplicationInfo().getOLifEExtensionAt(0).setDataRep(ApplicationInfo.DataRep.PARTIAL);
			holding.getPolicy().setApplicationInfo(policy.getApplicationInfo());
			// transfer the appropriate data
			NbaTXLife response = null;
			response =
				new NbaBackEndAdapterFacade().submitRequestToHost(newNbaTXLife, createRequestMessage(contract, request).getWorkitemId());
			TransResult result =
				response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();
			if (result.getResultCode() > 1) {
				policy.setActionFailed();
				policy.addTransactionError(result);
				throw new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_PARSE);
			} else {
				policy.setActionSuccessful();
			}
			return contract;

		}
		// End NBA050
		//begin NBA093
		if (businessProcess.equalsIgnoreCase(PROC_FINAL_DISPOSITION)
			|| businessProcess.equalsIgnoreCase(PROC_APPROVAL)
			|| businessProcess.equalsIgnoreCase(PROC_ISSUE)) {
			ApplicationInfo appInfo = holding.getPolicy().getApplicationInfo();
			appInfo.getActionIndicator();
			appInfo.setActionUpdate();
			appInfo.setDataRep(ApplicationInfo.DataRep.PARTIAL);
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
			if (businessProcess.equalsIgnoreCase(PROC_APPROVAL)) {
				if (appInfoExt != null && appInfoExt.getUnderwritingApproval() == OLIX_UNDAPPROVAL_UNDERWRITER) {
					request.setTransType(TC_TYPE_APPROVE);
					request.setObjectType(OLI_POLICY);
				}
			} else {
				request.setTransType(TC_TYPE_FINALDISPOSITION);
				request.setObjectType(OLI_APPLICATIONINFO);
			}
			if (request.getTransType() != OLI_UNKNOWN) {
				//begin NBA077
				if ((request.getTransType() == TC_TYPE_FINALDISPOSITION)
					&& ((appInfoExt != null && appInfoExt.getApplicationSubType() == OLI_APPSUBTYPE_REISSUE) || (appInfo.getApplicationType() == OLI_APPTYPE_CHANGE))) {//ALII1206
					request.setTransMode(TC_MODE_REPLACE);
				} else {
					request.setTransMode(TC_MODE_UPDATE);
				}
				//end NBA077
				//NBA077 line deleted
				request.setTranContentCode(TC_CONTENT_UPDATE);
				contract = processUpdateRequest(contract, createRequestMessage(contract, request));
			}
		}
		//end NBA093
		// Apply Money (508)
		if (businessProcess.equalsIgnoreCase(PROC_APPLY_MONEY)) {	//NBA093
			for (int i = 0; i < holding.getPolicy().getFinancialActivityCount(); i++) {
				if (holding.getPolicy().getFinancialActivityAt(i).getActionIndicator().isAdd()) { // SPR3290
					FinancialActivity finAct = holding.getPolicy().getFinancialActivityAt(i);
					finAct.setDataRep(FinancialActivity.DataRep.FULL);
					request.setTransMode(TC_MODE_ADD);
					request.setObjectType(OLI_FINACTIVITY);
					request.setTransType(TC_TYPE_CWA);
					request.setTranContentCode(TC_CONTENT_INSERT);
					contract = processUpdateRequest(contract, createRequestMessage(contract, request));
				}
			}
		}
		// Requirement receipt and Requirement add
		if (businessProcess.equalsIgnoreCase(PROC_POST_REQUIREMENT) || businessProcess.equalsIgnoreCase(PROC_RECEIPT)) {	//NBA093
			for (int i = 0; i < holding.getPolicy().getRequirementInfoCount(); i++) {
				RequirementInfo reqInfo = holding.getPolicy().getRequirementInfoAt(i);
				reqInfo.setDataRep(RequirementInfo.DataRep.FULL);
				request.setTransType(TC_TYPE_REQUIREMENT);
				request.setObjectType(OLI_REQUIREMENTINFO);
				i = processRequirementUpdate(request, businessProcess, holding, i, reqInfo); //NBA130
            }
            if (holding.getPolicy().getRequirementInfoCount() > 0) { //NBA130
                contract = processUpdateRequest(contract, createRequestMessage(contract, request));
            }
		} //NBA130
		return contract;
	}

	/**
	 * Method to determine if case is Reg 60 or Reg 60 PreSale
	 * @param newNbaTxLife
	 * @return
	 */
	//NBA231
	private static boolean isReg60(NbaTXLife newNbaTxLife) {
		if (isReg60PreSale(newNbaTxLife) || isReg60Case(newNbaTxLife)) {
			return true;
		}
		return false;
	}
	/**
	 * Method reads Application Type to determine if a Reg60 Pre Sale
	 * @param newNbaTXLife
	 * @return
	 */
	//NBA231 new method
	private static boolean isReg60PreSale(NbaTXLife newNbaTXLife) {
		Holding holding = newNbaTXLife.getPrimaryHolding();
		ApplicationInfo appInfo = holding.getPolicy().getApplicationInfo();
		if (appInfo.getApplicationType() == NbaOliConstants.OLI_APPTYPE_PRESALE) {
			return true;
		}
		return false;
	}
	/**
	 * Method determines if contract is reg 60 by reading ReplacementInd and Jurisdiction
	 * @param newNbaTXLife
	 * @return
	 */
	//NBA231 new method
	private static boolean isReg60Case(NbaTXLife newNbaTXLife) {
		Holding holding = newNbaTXLife.getPrimaryHolding();
		ApplicationInfo appInfo = holding.getPolicy().getApplicationInfo();
		if (appInfo.getReplacementInd() && holding.getPolicy().getJurisdiction() == 37) {
			return true;
		}
		return false;
	}
    /**
     * @see java.lang.Object#toString()
     */
	public String toString() {
		return super.toString();
	}
	/**
	 * When a request has been created, it is passed to this method for submission
	 * to the back end adapter.
	 * @param updateRequest an NbaTXLife object containing request information
	 * @return an NbaTXLife containing the resulting transaction 
	 */
	private static NbaTXLife processResponse(NbaTXLife contract, NbaTXLife result) throws NbaBaseException {
		if (result.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify() == null) {
			throw new NbaBaseException("Unable to submit request to host.  Request: " + result.toXmlString() + " ~~~ nResponse: " + result.toXmlString());
		}
		if (contract.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify() == null) {
			contract.getTXLife().setUserAuthResponseAndTXLifeResponseAndTXLifeNotify(
				result.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify());
		} else {
			for (int i = 0; i < result.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseCount(); i++) {
				contract.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().addTXLifeResponse(
					result.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(i));
			}
		}
		return contract;
	}
	/**
	 * When a request has been created, it is passed to this method for submission
	 * to the back end adapter.
	 * @param updateRequest an NbaTXLife object containing request information
	 * @return an NbaTXLife containing the resulting transaction 
	 */
	private static NbaTXLife processUpdateRequest(NbaTXLife contract, NbaTXLife updateRequest) throws NbaBaseException {
		if (updateRequest == null) {
			throw new NbaBaseException("Unable to create update request");
		}
		contract = processResponse(contract, new NbaBackEndAdapterFacade().submitRequestToHost(updateRequest, updateRequest.getWorkitemId()));
		return contract;
	}
	/**
	 * This method constructs a request message by creating a new NbaTXLife object,
	 * updating the request header and copying the input NbaTXLife contract data into it.
	 * Prior to this creation, however, it calls the <code>createRequestObject</code> method
	 * to create the NbaTXRequestVO needed by the process using NbaDst and NbaUserVO
	 * objects as input to that method.
	 * @param contract an NbaTXLife object containing contract data
	 * @param work an NbaDst object containing work item information
	 * @param user an NbaUserVO object containing user information
	 * @return an NbaTXLife containing the transaction to be sent to the back end adapter
	 */
	public static NbaTXLife processContractUpdate(NbaTXLife contract, NbaDst work, NbaUserVO user) throws NbaBaseException {
		return processContractUpdate(contract, createRequestObject(work, user));
	}
	/**
	 * This method constructs a request message by creating a new NbaTXLife object,
	 * updating the request header and copying the input NbaTXLife contract data into it.
	 * @param contract an NbaTXLife object containing contract data
	 * @param nbaTXRequest an NbaTXRequestVO containing transaction, work item
	 *  and user information
	 * @return an NbaTXLife containing the transaction to be sent to the back end adapter
	 */
	private static NbaTXLife createRequestMessage(NbaTXLife contract, NbaTXRequestVO nbaTXRequest) throws NbaBaseException {
		NbaTXLife txLife = new NbaTXLife(nbaTXRequest);
		// SPR3290 code deleted
		//NBA036 Code Deleted
		txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setOLifE(
			contract.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getOLifE());
		txLife.getPrimaryHolding().getPolicy().setPolicyStatus(OLI_POLSTAT_PENDING);
		return txLife;
	}
	/**
	* Creates a request object that will be used to perform an update to the contract.
	* Request is manufactured using input values, NbaLob object and User objectdefault values.
	* @param transMode indicates the type of transaction (add, update, delete, etc.)
	* @param transContentCode indicates the contents of the transaction (insert, update, etc)
	* @return com.csc.fsg.nba.vo.NbaTXLife a value object with the newly created transaction
	*/
	private static NbaTXRequestVO createRequestObject(NbaDst work, NbaUserVO user) throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setNbaLob(work.getNbaLob());
		nbaTXRequest.setNbaUser(user);
		return nbaTXRequest;
	}

	// Begin NBA036
	/**
	 * Apply the coverage and benefits changes to the host.
	 * @param userVO the user value object
	 * @param nbaTXLife the holding inquiry
	 * @param nbaDst the wrapper object
	 * @return com.csc.fsg.nba.vo.NbaTXLife the modified holding inquiry
	 */
	public static NbaTXLife applyCoverageBenefits(NbaTXLife contract, NbaTXRequestVO request) throws NbaBaseException {

		// Approve/Deny
		approveCoverage(contract, request);
		approveBenefit(contract, request);
		denyBenefit(contract, request);
		denyCoverage(contract, request);
		denyPerson(contract, request);
		denyRider(contract, request);

		// SubstandardRating
		removeSubstandardRatingFromBenefit(contract, request);	//ACN013
		removeSubstandardRatingFromCoverage(contract, request);	//ACN013

		addSubstandardRatingToBenefit(contract, request);
		addSubstandardRatingToCoverage(contract, request);
		//NBA093 Code Deleted
		// ACN013 Code Deleted
		//NBA093 Code Deleted

		// EndorsementInfo
		addEndorsement(contract, request);
		removeEndorsement(contract, request);

		return contract;
	}
	// End NBA036

	// Begin NBA036
	/**
	 * Create the XML for the transaction
	 * @param userVO
	 * @param nbaTXLife
	 * @param nbaDst
	 */
	protected static void approveCoverage(NbaTXLife contract, NbaTXRequestVO request) throws NbaBaseException {
		Policy policy = contract.getPrimaryHolding().getPolicy();
		Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();  //NBA093
		if (life != null) {
			List coverages = life.getCoverage();
			for (int i = 0; i < coverages.size(); i++) {
				Coverage coverage = (Coverage) coverages.get(i);
				if (coverage.isAction(NbaActionIndicator.ACTION_UNCONDITIONAL)
					|| coverage.isAction(NbaActionIndicator.ACTION_CONDITIONAL)
					|| coverage.isAction(NbaActionIndicator.ACTION_ASSIGN)) {
					if (request != null) {
						request.setTransType(TC_TYPE_APPROVE);
						request.setTransMode(TC_MODE_UPDATE);
						request.setObjectType(OLI_LIFECOVERAGE);
						request.setTranContentCode(TC_CONTENT_UPDATE);
					}
					NbaTXLife newNbaTXLife = new NbaTXLife(request);
					if (newNbaTXLife == null) {
						return;
					} // transfer the appropriate data
					if (coverage != null) { // add the Party and Relation
						List parties = contract.getOLifE().getParty();
						for (int p = 0; p < parties.size(); p++) {
							Party party = (Party) parties.get(p);
							LifeParticipant lifeParticipant = NbaUtils.findInsuredLifeParticipant(coverage, false); //SPR1282 SPR2799 
							if (lifeParticipant != null && (party.getId().equals(lifeParticipant.getPartyID()))) { //SPR1282 SPR2799
								newNbaTXLife.getOLifE().addParty(party.clone(true));
								//Relation primaryRelation = getPrimaryRelationForParty(party, nbaTXLife.getOLifE().getRelation());
								Relation primaryRelation = getPrimaryRelationForParty(party, contract.getOLifE().getRelation());
								if (primaryRelation != null) {
									newNbaTXLife.getOLifE().addRelation(primaryRelation);
								}
								break;
							}
						}
					}
					newNbaTXLife.getPrimaryHolding().getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife().addCoverage(coverage);  //NBA093
					NbaTXLife response =
						new NbaBackEndAdapterFacade().submitRequestToHost(newNbaTXLife, createRequestMessage(contract, request).getWorkitemId());
					TransResult result =
						response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();
					if (result.getResultCode() > 1) {
						coverage.setActionFailed();
						coverage.addTransactionError(result);
					} else {
						coverage.setActionSuccessful();
					}
				}
			}
		}
		Annuity annuity = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity();  //NBA093
		if (annuity != null) {
			List riders = annuity.getRider();
			for (int i = 0; i < riders.size(); i++) {
				Rider rider = (Rider) riders.get(i);
				if (rider.isAction(NbaActionIndicator.ACTION_UNCONDITIONAL)
					|| rider.isAction(NbaActionIndicator.ACTION_CONDITIONAL)
					|| rider.isAction(NbaActionIndicator.ACTION_ASSIGN)) {
					if (request != null) {
						request.setTransType(TC_TYPE_APPROVE);
						request.setTransMode(TC_MODE_UPDATE);
						request.setObjectType(OLI_LIFECOVERAGE);
						request.setTranContentCode(TC_CONTENT_UPDATE);
					}
					NbaTXLife newNbaTXLife = new NbaTXLife(request);
					if (newNbaTXLife == null) {
						return;
					} // transfer the appropriate data
					if (rider != null) { // add the Party and Relation
						List parties = contract.getOLifE().getParty();
						for (int p = 0; p < parties.size(); p++) {
							Party party = (Party) parties.get(p);
							// NBA093 deleted line
							Participant participant = NbaUtils.getInsurableParticipant(rider); //SPR1282 SPR2799
							if (participant != null && (party.getId().equals(participant.getPartyID()))) { //SPR1282 SPR2799
								newNbaTXLife.getOLifE().addParty(party.clone(true));
								Relation primaryRelation = getPrimaryRelationForParty(party, contract.getOLifE().getRelation());
								if (primaryRelation != null) {
									newNbaTXLife.getOLifE().addRelation(primaryRelation);
								}
								break;
							}
						}
					}
					newNbaTXLife.getPrimaryHolding().getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity().addRider(rider);  //NBA093
					NbaTXLife response =
						new NbaBackEndAdapterFacade().submitRequestToHost(newNbaTXLife, createRequestMessage(contract, request).getWorkitemId());
					TransResult result =
						response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();
					if (result.getResultCode() > 1) {
						rider.setActionFailed();
						rider.addTransactionError(result);
					} else {
						rider.setActionSuccessful();
					}
				}
			}
		}
	}
	// End NBA036

	// Begin NBA036
	/**
	 * Create the XML for the transaction
	 * @param userVO
	 * @param nbaTXLife
	 * @param nbaDst
	 */
	protected static void approveBenefit(NbaTXLife contract, NbaTXRequestVO request) throws NbaBaseException {
		Policy policy = contract.getPrimaryHolding().getPolicy();
		Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife(); //NBA093
		if (life != null) {
			List coverages = life.getCoverage();
			for (int i = 0; i < coverages.size(); i++) {
				Coverage coverage = (Coverage) coverages.get(i);
				List covOptions = coverage.getCovOption();
				for (int j = 0; j < covOptions.size(); j++) {
					CovOption covOption = (CovOption) covOptions.get(j);
					if (covOption.isAction(NbaActionIndicator.ACTION_UNCONDITIONAL)
						|| covOption.isAction(NbaActionIndicator.ACTION_CONDITIONAL)
						|| covOption.isAction(NbaActionIndicator.ACTION_ASSIGN)) {
						if (request != null) {
							request.setTransType(TC_TYPE_APPROVE);
							request.setTransMode(TC_MODE_UPDATE);
							request.setObjectType(OLI_COVOPTION);
							request.setTranContentCode(TC_CONTENT_UPDATE);
						}
						NbaTXLife newNbaTXLife = new NbaTXLife(request);
						if (newNbaTXLife == null) {
							return;
						} // transfer the appropriate data
						if (coverage != null) { // add the Party and Relation
							List parties = contract.getOLifE().getParty();
							for (int p = 0; p < parties.size(); p++) {
								Party party = (Party) parties.get(p);
								LifeParticipant lifeParticipant = NbaUtils.findInsuredLifeParticipant(coverage, false); //SPR1282 SPR2799
								if (lifeParticipant != null && (party.getId().equals(lifeParticipant.getPartyID()))) { //SPR1282 SPR2799
									newNbaTXLife.getOLifE().addParty(party.clone(true));
									Relation primaryRelation = getPrimaryRelationForParty(party, contract.getOLifE().getRelation());
									if (primaryRelation != null) {
										newNbaTXLife.getOLifE().addRelation(primaryRelation);
									}
									break;
								}
							}
						}
						newNbaTXLife.getPrimaryHolding().getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife().getCoverageAt(  //NBA093
							0).addCovOption(
							covOption);
						NbaTXLife response =
							new NbaBackEndAdapterFacade().submitRequestToHost(
								newNbaTXLife,
								createRequestMessage(contract, request).getWorkitemId());
						TransResult result =
							response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();
						if (result.getResultCode() > 1) {
							covOption.setActionFailed();
							covOption.addTransactionError(result);
						} else {
							covOption.setActionSuccessful();
						}
					}
				}
			}
		}
		// begin NBA006
		Annuity annuity = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity();  //NBA093
		if (annuity != null) {
			List riders = annuity.getRider();
			for (int i = 0; i < riders.size(); i++) {
				Rider rider = (Rider) riders.get(i);
				// NBA093 deleted line
				List covOptions = rider.getCovOption();  //NBA093
				for (int j = 0; j < covOptions.size(); j++) {
					CovOption covOption = (CovOption) covOptions.get(j);
					if (covOption.isAction(NbaActionIndicator.ACTION_UNCONDITIONAL)
						|| covOption.isAction(NbaActionIndicator.ACTION_CONDITIONAL)
						|| covOption.isAction(NbaActionIndicator.ACTION_ASSIGN)) {
						if (request != null) {
							request.setTransType(TC_TYPE_APPROVE);
							request.setTransMode(TC_MODE_UPDATE);
							request.setObjectType(OLI_COVOPTION);
							request.setTranContentCode(TC_CONTENT_UPDATE);
							//request.setNbaLob(nbaDst.getNbaLob());
							//request.setNbaUser(userVO);
						}
						NbaTXLife newNbaTXLife = new NbaTXLife(request);
						if (newNbaTXLife == null) {
							return;
						} // transfer the appropriate data
						if (rider != null) { // add the Party and Relation
							List parties = contract.getOLifE().getParty();
							for (int p = 0; p < parties.size(); p++) {
								Party party = (Party) parties.get(p);
								java.util.List participants = rider.getParticipant(); //SPR1282  NBA093
								Participant participant = NbaUtils.findPrimaryInsuredParticipant(participants); //SPR1282
								if (party.getId().equals(participant.getPartyID())) { //SPR1282
									newNbaTXLife.getOLifE().addParty(party.clone(true));
									Relation primaryRelation = getPrimaryRelationForParty(party, contract.getOLifE().getRelation());
									if (primaryRelation != null) {
										newNbaTXLife.getOLifE().addRelation(primaryRelation);
									}
									break;
								}
							}
						}
						Rider newRider =
							newNbaTXLife.getPrimaryHolding().getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity().getRiderAt(0);  //NBA093
						//NBA093 deleted line
						newRider.addCovOption(covOption);  //NBA093
						NbaTXLife response =
							new NbaBackEndAdapterFacade().submitRequestToHost(
								newNbaTXLife,
								createRequestMessage(contract, request).getWorkitemId());
						TransResult result =
							response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();
						if (result.getResultCode() > 1) {
							covOption.setActionFailed();
							covOption.addTransactionError(result);
						} else {
							covOption.setActionSuccessful();
						}
					}
				}
			}
		}
		// end NBA006
	}
	// End NBA036

	// Begin NBA036
	/**
	 * Apply the rate class change to the host.
	 * @param party
	 */
	protected static Relation getPrimaryRelationForParty(Party party, List relations) throws NbaBaseException {
		Relation relation = null;
		int rank = NbaConstants.ROLE_CODES_RANKING.length;
		for (int n = 0; n < relations.size(); n++) { // Dig thru the relations to get the primary role relation
			relation = (Relation) relations.get(n);
			for (int c = 0; c < NbaConstants.ROLE_CODES_RANKING.length; c++) {
				if (relation.getOriginatingObjectType() == OLI_HOLDING
					&& NbaConstants.PRIMARY_HOLDING_ID.equals(relation.getOriginatingObjectID())	//SPR3605
					&& relation.getRelatedObjectType() == OLI_PARTY
					&& relation.getRelatedObjectID().equals(party.getId())
					&& Long.toString(relation.getRelationRoleCode()).equals(NbaConstants.ROLE_CODES_RANKING[c])) {
					if (c < rank) {
						rank = c;
					}
					break;
				}
			}
		}
		if (relation != null) {
			if (rank == NbaConstants.ROLE_CODES_RANKING.length) { // add the Relation
			} else {
				for (int z = 0; z < relations.size(); z++) { // Loop thru the relations again to dig out the appropriate one
					relation = (Relation) relations.get(z);
					if (Long.toString(relation.getRelationRoleCode()).equals(NbaConstants.ROLE_CODES_RANKING[rank])
						&& relation.getRelatedObjectID().equals(party.getId())
						&& relation.getOriginatingObjectType() == OLI_HOLDING
						&& NbaConstants.PRIMARY_HOLDING_ID.equals(relation.getOriginatingObjectID())	//SPR3605
						&& relation.getRelatedObjectType() == OLI_PARTY) {
						return relation;
					}
				}
			}
		}
		return null;
	}
	// End NBA036

	// Begin NBA036
	/**
	 * Create the XML for the transaction
	 * @param userVO
	 * @param nbaTXLife
	 * @param nbaDst
	 */
	protected static void denyBenefit(NbaTXLife contract, NbaTXRequestVO request) throws NbaBaseException {
		Policy policy = contract.getPrimaryHolding().getPolicy();
		Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();  //NBA093
		if (life != null) {
			List coverages = life.getCoverage();
			for (int i = 0; i < coverages.size(); i++) {
				Coverage coverage = (Coverage) coverages.get(i);
				List covOptions = coverage.getCovOption();
				for (int j = 0; j < covOptions.size(); j++) {
					CovOption covOption = (CovOption) covOptions.get(j);
					if (covOption.isAction(NbaActionIndicator.ACTION_DENY)) {
						if (request != null) {
							request.setTransType(TC_TYPE_DENY);
							request.setTransMode(TC_MODE_UPDATE);
							request.setObjectType(OLI_COVOPTION);
							request.setTranContentCode(TC_CONTENT_UPDATE);
							//request.setNbaLob(nbaDst.getNbaLob());
							//request.setNbaUser(userVO);
						}
						NbaTXLife newNbaTXLife = new NbaTXLife(request);
						if (newNbaTXLife == null) {
							return;
						} // transfer the appropriate data
						if (coverage != null) { // add the Party and Relation
							List parties = contract.getOLifE().getParty();
							for (int p = 0; p < parties.size(); p++) {
								Party party = (Party) parties.get(p);
								LifeParticipant lifeParticipant = NbaUtils.findInsuredLifeParticipant(coverage, false); //SPR1282 SPR2799
								if (lifeParticipant != null && (party.getId().equals(lifeParticipant.getPartyID()))) { //SPR1282 SPR2799
									newNbaTXLife.getOLifE().addParty(party.clone(true));
									Relation primaryRelation = getPrimaryRelationForParty(party, contract.getOLifE().getRelation());
									if (primaryRelation != null) {
										newNbaTXLife.getOLifE().addRelation(primaryRelation);
									}
									break;
								}
							}
						}
						Policy newPolicy = newNbaTXLife.getPrimaryHolding().getPolicy();
						if (newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() == null) {  //NBA093
							LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty insurance = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();  //NBA093
							insurance.setLife(new Life());
							newPolicy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(insurance);  //NBA093
						}
						Coverage coverageCopy = coverage.clone(true); // clone the Coverage to add CovOption objects
						newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife().addCoverage(coverageCopy);  //NBA093
						CovOption fullCovOption = covOption.clone(true); // clone the CovOption for the FULL portion of the transaction
						covOption.setDataRep(CovOption.DataRep.REMOVED); // REMOVED portion
						coverageCopy.addCovOption(covOption);
						fullCovOption.setDataRep(CovOption.DataRep.FULL); // FULL portion
						fullCovOption.setId("CovOption_" + Integer.toString(covOptions.size() + 1));
						coverageCopy.addCovOption(fullCovOption);
						Relation newRelation = new Relation(); // Create the new relation object
						newRelation.setOriginatingObjectID(covOption.getId());
						newRelation.setOriginatingObjectType(OLI_COVOPTION);
						newRelation.setRelatedObjectID(fullCovOption.getId());
						newRelation.setRelatedObjectType(OLI_COVOPTION);
						newRelation.setRelationRoleCode(OLI_REL_REPLACEDBY);
						newNbaTXLife.getOLifE().addRelation(newRelation);
						NbaTXLife response =
							new NbaBackEndAdapterFacade().submitRequestToHost(
								newNbaTXLife,
								createRequestMessage(contract, request).getWorkitemId());
						TransResult result =
							response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();
						if (result.getResultCode() > 1) {
							covOption.setActionFailed();
							covOption.addTransactionError(result);
						} else {
							covOption.setActionSuccessful();
						}
					}
				}
			}
		}
		// begin NBA006
		Annuity annuity = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity();  //NBA093
		if (annuity != null) {
			List riders = annuity.getRider();
			for (int i = 0; i < riders.size(); i++) {
				Rider rider = (Rider) riders.get(i);
				// NBA093 deleted line
				List covOptions = rider.getCovOption();  //NBA093
				for (int j = 0; j < covOptions.size(); j++) {
					CovOption covOption = (CovOption) covOptions.get(j);
					if (covOption.isAction(NbaActionIndicator.ACTION_DENY)) {
						if (request != null) {
							request.setTransType(TC_TYPE_DENY);
							request.setTransMode(TC_MODE_UPDATE);
							request.setObjectType(OLI_COVOPTION);
							request.setTranContentCode(TC_CONTENT_UPDATE);
						}
						NbaTXLife newNbaTXLife = new NbaTXLife(request);
						if (newNbaTXLife == null) {
							return;
						} // transfer the appropriate data
						if (rider != null) { // add the Party and Relation
							List parties = contract.getOLifE().getParty();
							for (int p = 0; p < parties.size(); p++) {
								Party party = (Party) parties.get(p);
								if (party.getId().equals(rider.getParticipantAt(0).getPartyID())) {  //NBA093
									newNbaTXLife.getOLifE().addParty(party.clone(true));
									Relation primaryRelation = getPrimaryRelationForParty(party, contract.getOLifE().getRelation());
									if (primaryRelation != null) {
										newNbaTXLife.getOLifE().addRelation(primaryRelation);
									}
									break;
								}
							}
						}
						Policy newPolicy = newNbaTXLife.getPrimaryHolding().getPolicy();
						if (newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() == null) {  //NBA093
							LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty insurance = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();  //NBA093
							insurance.setAnnuity(new Annuity());
							newPolicy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(insurance);  //NBA093
						}
						Rider riderCopy = rider.clone(true); // clone the Rider to add CovOption objects
						//NBA093 deleted 4 lines
						newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity().addRider(riderCopy);  //NBA093
						CovOption fullCovOption = covOption.clone(false); // clone the CovOption for the FULL portion of the transaction
						covOption.setDataRep(CovOption.DataRep.REMOVED); // REMOVED portion
						riderCopy.addCovOption(covOption);  //NBA093
						fullCovOption.setDataRep(CovOption.DataRep.FULL); // FULL portion
						fullCovOption.setId(fullCovOption.getId() + "a");
						riderCopy.addCovOption(fullCovOption);  //NBA093
						Relation newRelation = new Relation(); // Create the new relation object
						newRelation.setOriginatingObjectID(covOption.getId());
						newRelation.setOriginatingObjectType(OLI_COVOPTION);
						newRelation.setRelatedObjectID(fullCovOption.getId());
						newRelation.setRelatedObjectType(OLI_COVOPTION);
						newRelation.setRelationRoleCode(OLI_REL_REPLACEDBY);
						newNbaTXLife.getOLifE().addRelation(newRelation);
						NbaTXLife response =
							new NbaBackEndAdapterFacade().submitRequestToHost(
								newNbaTXLife,
								createRequestMessage(contract, request).getWorkitemId());
						TransResult result =
							response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();
						if (result.getResultCode() > 1) {
							covOption.setActionFailed();
							covOption.addTransactionError(result);
						} else {
							covOption.setActionSuccessful();
						}
					}
				}
			}
		}
	}
	// End NBA036

	// Begin NBA036
	/**
	 * Create the XML for the transaction
	 * @param userVO
	 * @param nbaTXLife
	 * @param nbaDst
	 */
	protected static void denyCoverage(NbaTXLife contract, NbaTXRequestVO request) throws NbaBaseException {
		Policy policy = contract.getPrimaryHolding().getPolicy();
		Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();  //NBA093
		if (life != null) {
			List coverages = life.getCoverage();
			for (int i = 0; i < coverages.size(); i++) {
				Coverage coverage = (Coverage) coverages.get(i);
				if (coverage.isAction(NbaActionIndicator.ACTION_DENY)) {
					if (request != null) {
						request.setTransType(TC_TYPE_DENY);
						request.setTransMode(TC_MODE_UPDATE);
						request.setObjectType(OLI_LIFECOVERAGE);
						request.setTranContentCode(TC_CONTENT_UPDATE);
					}
					NbaTXLife newNbaTXLife = new NbaTXLife(request);
					if (newNbaTXLife == null) {
						return;
					} // transfer the appropriate data

					if (coverage != null) { // add the Party and Relation
						List parties = contract.getOLifE().getParty();
						for (int p = 0; p < parties.size(); p++) {
							Party party = (Party) parties.get(p);
							LifeParticipant lifeParticipant = NbaUtils.findInsuredLifeParticipant(coverage, false); //SPR1282 SPR2799
							if (lifeParticipant != null && (party.getId().equals(lifeParticipant.getPartyID()))) { //SPR1282 SPR2799
								newNbaTXLife.getOLifE().addParty(party.clone(true));
								Relation primaryRelation = getPrimaryRelationForParty(party, contract.getOLifE().getRelation());
								if (primaryRelation != null) {
									newNbaTXLife.getOLifE().addRelation(primaryRelation);
								}
								break;
							}
						}
					}

					Policy newPolicy = newNbaTXLife.getPrimaryHolding().getPolicy();
					if (newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() == null) {  //NBA093
						LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty insurance = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();  //NBA093
						insurance.setLife(new Life());
						newPolicy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(insurance);  //NBA093
					}
					Coverage fullCoverage = coverage.clone(true); // clone the Coverage for the FULL portion of the transaction
					coverage.setDataRep(Coverage.DataRep.REMOVED); // REMOVED portion
					newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife().addCoverage(coverage);  //NBA093
					fullCoverage.setDataRep(Coverage.DataRep.FULL); // FULL portion
					fullCoverage.setId("Coverage_" + Integer.toString(coverages.size() + 1));
					newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife().addCoverage(fullCoverage);  //NBA093
					Relation newRelation = new Relation(); // Create the new relation object
					newRelation.setOriginatingObjectID(coverage.getId());
					newRelation.setOriginatingObjectType(OLI_LIFECOVERAGE);
					newRelation.setRelatedObjectID(fullCoverage.getId());
					newRelation.setRelatedObjectType(OLI_LIFECOVERAGE);
					newRelation.setRelationRoleCode(OLI_REL_REPLACEDBY);
					newNbaTXLife.getOLifE().addRelation(newRelation);
					NbaTXLife response =
						new NbaBackEndAdapterFacade().submitRequestToHost(newNbaTXLife, createRequestMessage(contract, request).getWorkitemId());
					TransResult result =
						response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();
					if (result.getResultCode() > 1) {
						coverage.setActionFailed();
						coverage.addTransactionError(result);
					} else {
						coverage.setActionSuccessful();
					}
				}
			}
		}
	}
	// End NBA036

	//Begin NBA036
	/**
	 * Create the XML for the transaction
	 * @param userVO
	 * @param nbaTXLife
	 * @param nbaDst
	 */
	protected static void denyPerson(NbaTXLife contract, NbaTXRequestVO request) throws NbaBaseException {
		Policy policy = contract.getPrimaryHolding().getPolicy();
		Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();  //NBA093
		if (life != null) {
			List coverages = life.getCoverage();
			for (int i = 0; i < coverages.size(); i++) {
				Coverage coverage = (Coverage) coverages.get(i);
				List participants = coverage.getLifeParticipant();
				for (int j = 0; j < participants.size(); j++) {
					LifeParticipant participant = (LifeParticipant) participants.get(j);
					if (participant.isAction(NbaActionIndicator.ACTION_DENY)) {
						List parties = contract.getOLifE().getParty();
						List relations = contract.getOLifE().getRelation();
						for (int p = 0; p < parties.size(); p++) { // add the party for the participant
							Party party = (Party) parties.get(p);
							if (participant.getPartyID().equals(party.getId()) && party.getPartyTypeCode() == OLI_PT_PERSON) {
								if (request != null) {
									request.setTransType(TC_TYPE_DENY);
									request.setTransMode(TC_MODE_UPDATE);
									request.setObjectType(OLI_LIFEPARTICIPANT);
									request.setTranContentCode(TC_CONTENT_UPDATE);
								}
								NbaTXLife newNbaTXLife = new NbaTXLife(request);
								if (newNbaTXLife == null) {
									return;
								} // transfer the appropriate data
								newNbaTXLife.getOLifE().addParty(party.clone(true)); // add the Party copy
								Relation primaryRelation = getPrimaryRelationForParty(party, relations);
								if (primaryRelation != null) {
									newNbaTXLife.getOLifE().addRelation(primaryRelation);
								} else {}
								Policy newPolicy = newNbaTXLife.getPrimaryHolding().getPolicy();
								if (newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() == null) {  //NBA093
									LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty insurance = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();  //NBA093
									insurance.setLife(new Life());
									newPolicy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(insurance);  //NBA093
								}
								Coverage coverageCopy = coverage.clone(true); // clone the Coverage to add LifeParticipant objects
								newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife().addCoverage(coverageCopy);  //NBA093
								LifeParticipant fullParticipant = participant.clone(true);
								// clone the LifeParticipant for the FULL portion of the transaction
								participant.setDataRep(LifeParticipant.DataRep.REMOVED); // REMOVED portion
								coverageCopy.addLifeParticipant(participant);
								fullParticipant.setDataRep(CovOption.DataRep.FULL); // FULL portion
								fullParticipant.setId("LifeParticipant_" + Integer.toString(participants.size() + 1));
								coverageCopy.addLifeParticipant(fullParticipant);
								Relation newRelation = new Relation(); // create the new relation object
								newRelation.setOriginatingObjectID(participant.getId());
								newRelation.setOriginatingObjectType(OLI_LIFEPARTICIPANT);
								newRelation.setRelatedObjectID(fullParticipant.getId());
								newRelation.setRelatedObjectType(OLI_LIFEPARTICIPANT);
								newRelation.setRelationRoleCode(OLI_REL_REPLACEDBY);
								newNbaTXLife.getOLifE().addRelation(newRelation);
								NbaTXLife response =
									new NbaBackEndAdapterFacade().submitRequestToHost(
										newNbaTXLife,
										createRequestMessage(contract, request).getWorkitemId());
								TransResult result =
									response
										.getTXLife()
										.getUserAuthResponseAndTXLifeResponseAndTXLifeNotify()
										.getTXLifeResponseAt(0)
										.getTransResult();
								if (result.getResultCode() > 1) {
									participant.setActionFailed();
									participant.addTransactionError(result);
								} else {
									participant.setActionSuccessful();
								}
								break;
							}
						}
					}
				}
			}
		}
	}
	//End NBA036

	// Begin NBA036
	/**
	 * Create the XML for the transaction
	 * @param userVO the user value object
	 * @param nbaTXLife the holding inquiry
	 * @param nbaDst the wrapper object
	 */
	protected static void denyRider(NbaTXLife contract, NbaTXRequestVO request) throws NbaBaseException {
		Policy policy = contract.getPrimaryHolding().getPolicy();
		Annuity annuity = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity();  //NBA093
		if (annuity != null) {
			List riders = annuity.getRider();
			for (int i = 0; i < riders.size(); i++) {
				Rider rider = (Rider) riders.get(i);
				if (rider.isAction(NbaActionIndicator.ACTION_DENY)) {
					if (request != null) {
						request.setTransType(TC_TYPE_DENY);
						request.setTransMode(TC_MODE_UPDATE);
						request.setObjectType(OLI_ANNRIDER);
						request.setTranContentCode(TC_CONTENT_UPDATE);
						// request.setNbaLob(nbaDst.getNbaLob());
						// request.setNbaUser(userVO);
					}
					NbaTXLife newNbaTXLife = new NbaTXLife(request);
					if (newNbaTXLife == null) {
						return;
					} // transfer the appropriate data
					if (rider != null) { // add the Party and Relation
						List parties = contract.getOLifE().getParty();
						for (int p = 0; p < parties.size(); p++) {
							Party party = (Party) parties.get(p);
							//NBA093 deleted line
							Participant participant = NbaUtils.getInsurableParticipant(rider); //SPR1282 SPR2799
							if (participant != null && (party.getId().equals(participant.getPartyID()))) { //SPR1282
								newNbaTXLife.getOLifE().addParty(party.clone(true));
								Relation primaryRelation = getPrimaryRelationForParty(party, contract.getOLifE().getRelation());
								if (primaryRelation != null) {
									newNbaTXLife.getOLifE().addRelation(primaryRelation);
								}
								break;
							}
						}
					}
					Policy newPolicy = newNbaTXLife.getPrimaryHolding().getPolicy();
					if (newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() == null) {  //NBA093
						LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty insurance = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();  //NBA093
						insurance.setAnnuity(new Annuity());
						newPolicy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(insurance);  //NBA093
					}
					Rider removedRider = rider.clone(true); // clone the Rider for the REMOVED portion of the transaction
					Rider fullRider = rider.clone(true); // clone the Rider for the FULL portion of the transaction
					removedRider.setDataRep(Rider.DataRep.REMOVED); // REMOVED portion
					newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity().addRider(removedRider);  //NBA093
					fullRider.setDataRep(Rider.DataRep.FULL); // FULL portion
					fullRider.setId(fullRider.getId() + "a");
					newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity().addRider(fullRider);  //NBA093
					Relation newRelation = new Relation(); // Create the new relation object
					newRelation.setOriginatingObjectID(rider.getId());
					newRelation.setOriginatingObjectType(OLI_ANNRIDER);
					newRelation.setRelatedObjectID(removedRider.getId());
					newRelation.setRelatedObjectType(OLI_ANNRIDER);
					newRelation.setRelationRoleCode(OLI_REL_REPLACEDBY);
					newNbaTXLife.getOLifE().addRelation(newRelation);
					NbaTXLife response =
						new NbaBackEndAdapterFacade().submitRequestToHost(newNbaTXLife, createRequestMessage(contract, request).getWorkitemId());
					TransResult result =
						response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();
					if (result.getResultCode() > 1) {
						rider.setActionFailed();
						rider.addTransactionError(result);
					} else {
						rider.setActionSuccessful();
					}
				}
			}
		}
	}
	// End NBA036

	/**
	 * Add a SubStandardRation to a CovOption
	 * @param userVO
	 * @param nbaTXLife
	 * @param nbaDst
	 */
	protected static void addSubstandardRatingToBenefit(NbaTXLife nbaTXLife, NbaTXRequestVO request) throws NbaBaseException {
		//begin SPR2092
		Life life = nbaTXLife.getLife();
		if (life != null) {
			int covCnt = life.getCoverageCount();
			int covOptCnt;
			int subCnt;
			Coverage coverage;
			CovOption covOption;
			SubstandardRating substandardRating;
			for (int i = 0; i < covCnt; i++) {
				coverage = life.getCoverageAt(i);
				covOptCnt = coverage.getCovOptionCount();
				for (int j = 0; j < covOptCnt; j++) {
					covOption = coverage.getCovOptionAt(j);
					subCnt = covOption.getSubstandardRatingCount();
					for (int k = 0; k < subCnt; k++) {
						substandardRating = covOption.getSubstandardRatingAt(k);
						SubstandardRatingExtension substandardRatingExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
						boolean proposed = false;
						if (substandardRatingExtension != null ){
							proposed = substandardRatingExtension.getProposedInd();
						}
						//end ACN013
						if (!proposed && (substandardRating.isActionAdd() || substandardRating.isActionUpdate())) {	//ACN013	
							processAddSubstandardRatingToBenefit(nbaTXLife, request, coverage, covOption, substandardRating);
						}
					}
				}
			}
		}
		//end SPR2092

	}
	/**
	 * Add a SubStandardRation to a CovOption
	 * @param origNbaTXLife
	 * @param request
	 * @param coverage
	 * @param covOption
	 * @param substandardRating
	 * @throws NbaBaseException
	 */
	// SPR2092 New Method
	protected static void processAddSubstandardRatingToBenefit(NbaTXLife origNbaTXLife, NbaTXRequestVO request, Coverage coverage, CovOption covOption, SubstandardRating substandardRating) throws NbaBaseException {
		request.setTransType(TC_TYPE_SUBSTANDARDRATING);
		request.setTransMode(TC_MODE_UPDATE);
		request.setObjectType(OLI_COVOPTION);
		request.setTranContentCode(TC_CONTENT_INSERT);
		NbaTXLife newNbaTXLife = new NbaTXLife(request);
		Policy newPolicy = newNbaTXLife.getPolicy();
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladh = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
		ladh.setLife(new Life());
		newPolicy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(ladh);
		//Copy the objects to the Coverage hierarchy 
		Coverage newCoverage = coverage.clone(true);
		newCoverage.setOLifEExtension(coverage.getOLifEExtension());
		CovOption newCovOption = covOption.clone(true);
		newCovOption.setOLifEExtension(covOption.getOLifEExtension());
		substandardRating.setDataRep(CovOption.DataRep.PARTIAL);
		newCovOption.addSubstandardRating(substandardRating);		
		newCoverage.addCovOption(newCovOption);
		newNbaTXLife.getLife().addCoverage(newCoverage);

		//Message the back end system
		NbaTXLife response = new NbaBackEndAdapterFacade().submitRequestToHost(newNbaTXLife, origNbaTXLife.getWorkitemId());
		TransResult result = response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();
		updateTransResult(origNbaTXLife, result);	//ACN013
		if (result.getResultCode() > 1) {
			ArrayList errs = new ArrayList(); //ACN013
			errs.add(result); //ACN013
			substandardRating.applyResult(NbaActionIndicator.ACTION_FAILED, errs); //ACN013
		} else {
			substandardRating.applyResult(NbaActionIndicator.ACTION_SUCCESSFUL, new ArrayList());	//ACN013
		}
	}
	// Begin NBA036
	/**
	 * Create TxLife 151 transactions for new Substandard Ratings
	 * @param userVO
	 * @param nbaTXLife
	 */
	protected static void addSubstandardRatingToCoverage(NbaTXLife nbaTXLife, NbaTXRequestVO request) throws NbaBaseException {
		//begin NBA093
		Policy policy = nbaTXLife.getPrimaryHolding().getPolicy();
		if (policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isLife()){	//ACN013
			Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
			int covCount = life.getCoverageCount();
			for (int c = 0; c < covCount; c++) {
				Coverage coverage = life.getCoverageAt(c);		//Examine each Coverage
				int lifePartCount = coverage.getLifeParticipantCount();
				for (int l = 0; l < lifePartCount; l++) {
					LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(l);		//Examine each LifeParticipant
					int subStandCount = lifeParticipant.getSubstandardRatingCount();
					for (int s = 0; s < subStandCount; s++) {
						SubstandardRating substandardRating = lifeParticipant.getSubstandardRatingAt(s);		//Examine each SubstandardRating
						//begin ACN013
						SubstandardRatingExtension substandardRatingExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
						boolean proposed = false;
						if (substandardRatingExtension != null ){
							proposed = substandardRatingExtension.getProposedInd();
						}
						//end ACN013
						if (substandardRating.isActionAdd() && !proposed) {	//ACN013	
							TransResult result = generate151Trans(nbaTXLife, request, coverage, lifeParticipant, substandardRating);
							updateTransResult(nbaTXLife, result);	//ACN013
							if (result.getResultCode() > 1) {
								ArrayList errs = new ArrayList();	//ACN013
								errs.add(result);	//ACN013
								substandardRating.applyResult(NbaActionIndicator.ACTION_FAILED, errs);	//ACN013
							} else {
								substandardRating.applyResult(NbaActionIndicator.ACTION_SUCCESSFUL, new ArrayList());	//ACN013
							}
						}
					}
				}
			}
		} //ACN013
	//end NBA093
	}
	/**
	 * Create the XML for the transaction
	 * @param userVO
	 * @param nbaTXLife
	 * @param nbaDst
	 */
	protected static void removeSubstandardRatingFromBenefit(NbaTXLife contract, NbaTXRequestVO request) throws NbaBaseException {
		//NBA093 Code Deleted
		//Begin NBA093
		Policy policy = contract.getPrimaryHolding().getPolicy();
		if (policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isLife()){	//ACN013			
			boolean isCovOptionActionDelete = false;
			SubstandardRating subStdRating = null;
			CovOption covOption = null;
	
			if (request != null) {
				request.setTransType(TC_TYPE_SUBSTANDARDRATING);
				request.setTransMode(TC_MODE_UPDATE);
				request.setObjectType(OLI_COVOPTION);
				request.setTranContentCode(TC_CONTENT_DELETE);
			}
	
			NbaTXLife newNbaTXLife = new NbaTXLife(request);
			if (newNbaTXLife == null) {
				return;
			}
	
			Policy newPolicy = newNbaTXLife.getPrimaryHolding().getPolicy();
			List coverages = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife().getCoverage();
			Coverage coverage = null;
	
			for (int c = 0; c < coverages.size(); c++) {
				coverage = (Coverage) coverages.get(c);
				if (newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() == null) {
					LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty insurance = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
					insurance.setLife(new Life());
					newPolicy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(insurance);
	
				}
				List benefits = coverage.getCovOption();
				for (int b = 0; b < benefits.size(); b++) {
					covOption = (CovOption) benefits.get(b);
	
					List ratingList = covOption.getSubstandardRating();
					for (int r = 0; r < ratingList.size(); r++) {					
						subStdRating = (SubstandardRating) ratingList.get(r);
						//begin ACN013
						SubstandardRatingExtension substandardRatingExtension = NbaUtils.getFirstSubstandardExtension(subStdRating);
						boolean proposed = false;
						if (substandardRatingExtension != null ){
							proposed = substandardRatingExtension.getProposedInd();
						}
						//end ACN013
						if (subStdRating.isActionDelete() && !proposed) {	//ACN013
							isCovOptionActionDelete = true;
							coverage.addCovOption(covOption);
							subStdRating.setDataRep(Endorsement.DataRep.REMOVED);
							break;
						}
					}
					if (isCovOptionActionDelete) {
						break;
					}
				}
				if (isCovOptionActionDelete) {
					newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife().addCoverage(coverage); 
					break;
				}
			}
			if (!isCovOptionActionDelete) {
				return;
			}
	
			NbaTXLife response =
				new NbaBackEndAdapterFacade().submitRequestToHost(newNbaTXLife, createRequestMessage(contract, request).getWorkitemId());
			TransResult result = response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();
			updateTransResult(contract, result);	//ACN013
			if (result.getResultCode() > 1) {
				ArrayList errs = new ArrayList();	//ACN013
				errs.add(result);	//ACN013
				subStdRating.applyResult(NbaActionIndicator.ACTION_FAILED, errs);	//ACN013
			} else {
				subStdRating.applyResult(NbaActionIndicator.ACTION_SUCCESSFUL, new ArrayList());	//ACN013
			}
		}	//ACN013
		//End NBA093
	}
	// End NBA036

	// Begin NBA036
	/**
	 * Determine if deletions of SubstandardRatings should be precessed. 
	 * @param userVO
	 * @param nbaTXLife
	 * @param nbaDst
	 */
	protected static void removeSubstandardRatingFromCoverage(NbaTXLife nbaTXLife, NbaTXRequestVO request) throws NbaBaseException {
		//begin SPR2089
		Life life = nbaTXLife.getLife();
		if (life != null) {
			int covCnt = life.getCoverageCount();
			int lifeCnt;
			int subCnt;
			Coverage coverage;
			LifeParticipant lifeParticipant;
			SubstandardRating substandardRating;
			for (int i = 0; i < covCnt; i++) {
				coverage = life.getCoverageAt(i);
				lifeCnt = coverage.getLifeParticipantCount();
				for (int j = 0; j < lifeCnt; j++) {
					lifeParticipant = coverage.getLifeParticipantAt(j);
					subCnt = lifeParticipant.getSubstandardRatingCount();
					for (int k = 0; k < subCnt; k++) {
						substandardRating = lifeParticipant.getSubstandardRatingAt(k);
						//begin ACN013
						SubstandardRatingExtension substandardRatingExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
						boolean proposed = false;
						if (substandardRatingExtension != null ){
							proposed = substandardRatingExtension.getProposedInd();
						}
						//end ACN013
						if (substandardRating.isActionDelete() && !proposed) {	//ACN013						 
							processRemoveSubstandardRatingFromCoverage(nbaTXLife, request, coverage, lifeParticipant, substandardRating);
						}
					}
				}
			}
		}
		//end SPR2089
	}
	/**
	 * Process the deletion of a SubstandardRating by creating the TxLiife 151 transaction to delete a SubstandardRating
	 * and invoking the NbaBackEndAdapterFacade
	 * @param origNbaTXLife
	 * @param request
	 * @param coverage
	 * @param lifeParticipant
	 * @param substandardRating
	 * @throws NbaBaseException
	 */
	// SPR2089 New Method
	protected static void processRemoveSubstandardRatingFromCoverage(
			NbaTXLife origNbaTXLife,
			NbaTXRequestVO request,
			Coverage coverage,
			LifeParticipant lifeParticipant,
			SubstandardRating substandardRating)
			throws NbaBaseException {
		request.setTransType(TC_TYPE_SUBSTANDARDRATING);
		request.setTransMode(TC_MODE_UPDATE);
		request.setObjectType(OLI_LIFECOVERAGE);
		request.setTranContentCode(TC_CONTENT_DELETE);
		NbaTXLife newNbaTXLife = new NbaTXLife(request);
		Policy newPolicy = newNbaTXLife.getPolicy();
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladh = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
		ladh.setLife(new Life());
		newPolicy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(ladh);
		//Copy the objects to the Coverage hierarchy 
		Coverage newCoverage = coverage.clone(true);
		newCoverage.setOLifEExtension(coverage.getOLifEExtension());
		LifeParticipant newLifeParticipant = lifeParticipant.clone(true);
		newLifeParticipant.setOLifEExtension(lifeParticipant.getOLifEExtension());
		substandardRating.setDataRep(Endorsement.DataRep.REMOVED);
		newLifeParticipant.addSubstandardRating(substandardRating);		
		newCoverage.addLifeParticipant(newLifeParticipant);
		newNbaTXLife.getLife().addCoverage(newCoverage);
		//Copy Party and Relation objects
		String partyId = lifeParticipant.getPartyID();
		NbaParty nbaParty = origNbaTXLife.getParty(partyId);
		Party party = nbaParty.getParty();
		Relation relation = NbaUtils.getRelationForParty(partyId, origNbaTXLife.getOLifE().getRelation().toArray());
		newNbaTXLife.getOLifE().addParty(party);
		newNbaTXLife.getOLifE().addRelation(relation);
		//Message the back end system
		NbaTXLife response = new NbaBackEndAdapterFacade().submitRequestToHost(newNbaTXLife, origNbaTXLife.getWorkitemId());
		TransResult result = response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();
		updateTransResult(origNbaTXLife, result);	//ACN013
		if (result.getResultCode() > 1) {
			ArrayList errs = new ArrayList();	//ACN013
			errs.add(result);	//ACN013
			substandardRating.applyResult(NbaActionIndicator.ACTION_FAILED, errs);	//ACN013
		} else {
			substandardRating.applyResult(NbaActionIndicator.ACTION_SUCCESSFUL, new ArrayList());	//ACN013
		}
	}
 
	// Begin NBA036
	/**
	 * Create the XML for the transaction
	 * @param userVO
	 * @param nbaTXLife
	 * @param nbaDst
	 */
	protected static void addEndorsement(NbaTXLife contract, NbaTXRequestVO request) throws NbaBaseException { 
		Policy policy = contract.getPrimaryHolding().getPolicy();
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(policy); // SPR1018
		if (policyExtension != null) {
			List endorsements = policy.getEndorsement(); //NBA093
			for (int i = 0; i < endorsements.size(); i++) {
				Endorsement endorsement = (Endorsement) endorsements.get(i); //NBA093
				if (endorsement.isActionAdd()) {
					if (request != null) {
						request.setTransType(TC_TYPE_ENDORSEMENT);
						request.setTransMode(TC_MODE_ADD);
						request.setObjectType(OLI_ENDORSEMENT);
						request.setTranContentCode(TC_CONTENT_INSERT);
					}
					NbaTXLife newNbaTXLife = new NbaTXLife(request);
					if (newNbaTXLife == null) {
						return;
					} // transfer the appropriate data
					Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();  //NBA093
					Policy newPolicy = newNbaTXLife.getPrimaryHolding().getPolicy();//SPR2278
					if (life != null) {
						if ((endorsement.getRelatedObjectType() == OLI_LIFECOVERAGE) && (endorsement.getRelatedObjectID() != null)) { // add the Coverage NBA093
							List coverages = life.getCoverage();
							for (int c = 0; c < coverages.size(); c++) {
								Coverage coverage = (Coverage) coverages.get(c);
								if (coverage.getId().equals(endorsement.getRelatedObjectID())) { //NBA093
									//deleted line SPR2278
									if (newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() == null) {  //NBA093
										LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty insurance = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();  //NBA093
										insurance.setLife(new Life());
										insurance.getLife().addCoverage(coverage);//SPR2278
										newPolicy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(insurance);  //NBA093
									}
								//begin NBA093
								}
							}
						} else if ((endorsement.getRelatedObjectType() == OLI_COVOPTION) && (endorsement.getRelatedObjectID() != null)) { // add the Coverage NBA093
							List coverages = life.getCoverage();
							for (int c = 0; c < coverages.size(); c++) {
								Coverage coverage = (Coverage) coverages.get(c);									
								for(int x=0; x<coverage.getCovOptionCount();x++){
									CovOption covOption = coverage.getCovOptionAt(x); // SPR3290
									if (covOption.getId().equals(endorsement.getRelatedObjectID())) {
										//deleted line SPR2278
										if (newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() == null) {
											LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty insurance = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
											insurance.setLife(new Life());
											newPolicy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(insurance);
										}
										Coverage coverageCopy = coverage.clone(true); // Clone the Coverage and add the benefit
										coverageCopy.addCovOption(covOption);	
										newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife().addCoverage(coverageCopy);  //NBA093
										break;
									}										
								}																
							}
						}
					}

					Annuity annuity = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity();  //NBA093
					if (annuity != null) {
						constructEndorsementMessageForAnnuity(newPolicy, annuity, endorsement);		//SPR1163
					}
					if (endorsement.getAppliesToPartyID() != null) { // add the Party and Relation NBA093
						List parties = contract.getOLifE().getParty();
						for (int p = 0; p < parties.size(); p++) {
							Party party = (Party) parties.get(p);
							if (party.getId().equals(endorsement.getAppliesToPartyID())) { //NBA093
								newNbaTXLife.getOLifE().addParty(party.clone(true));
								Relation primaryRelation = getPrimaryRelationForParty(party, contract.getOLifE().getRelation());
								if (primaryRelation != null) {
									newNbaTXLife.getOLifE().addRelation(primaryRelation);
								} else {}
								break;
							}
						}
					}
					//deleted line SPR2278
					OLifEExtension newExtension = NbaTXLife.createOLifEExtension(EXTCODE_POLICY); // Create a new PolicyExtension
					newPolicy.addOLifEExtension(newExtension);
					// SPR3290 code deleted
					endorsement.setDataRep(Endorsement.DataRep.FULL);  //NBA093
					newPolicy.addEndorsement(endorsement); //NBA093
					NbaTXLife response =
						new NbaBackEndAdapterFacade().submitRequestToHost(newNbaTXLife, createRequestMessage(contract, request).getWorkitemId());
					TransResult result =
						response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();
					updateTransResult(contract, result);	//ACN013
					if (result.getResultCode() > 1) {
						ArrayList errs = new ArrayList();	//ACN013
						errs.add(result);	//ACN013
						endorsement.applyResult(NbaActionIndicator.ACTION_FAILED, errs);	//ACN013						
					} else {
						endorsement.applyResult(NbaActionIndicator.ACTION_SUCCESSFUL, new ArrayList());	//ACN013
					}
				}
			}
		}
	}
	// End NBA036

	// Begin NBA036
	/**
	 * Create the XML for the transaction
	 * @param userVO
	 * @param nbaTXLife
	 * @param nbaDst
	 */
	protected static void removeEndorsement(NbaTXLife contract, NbaTXRequestVO request) throws NbaBaseException {
		Policy policy = contract.getPrimaryHolding().getPolicy();
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(policy); // SPR1018
		if (policyExtension != null) {
			List endorsements = policy.getEndorsement(); //NBA093
			for (int i = 0; i < endorsements.size(); i++) {
				Endorsement endorsement = (Endorsement) endorsements.get(i); //NBA093
				if (endorsement.isActionDelete()) {
					if (request != null) {
						request.setTransType(TC_TYPE_ENDORSEMENT);
						request.setTransMode(TC_MODE_UPDATE);
						request.setObjectType(OLI_ENDORSEMENT);
						request.setTranContentCode(TC_CONTENT_DELETE);
					}
					NbaTXLife newNbaTXLife = new NbaTXLife(request);
					if (newNbaTXLife == null) {
						return;
					} // transfer the appropriate data
					Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();  //NBA093
					if (life != null) {
						if ((endorsement.getRelatedObjectType() == OLI_LIFECOVERAGE) && (endorsement.getRelatedObjectID() != null)) { // add the Coverage NBA093
							List coverages = life.getCoverage();
							for (int c = 0; c < coverages.size(); c++) {
								Coverage coverage = (Coverage) coverages.get(c);
								if (coverage.getId().equals(endorsement.getRelatedObjectID())) { //NBA093
									Policy newPolicy = newNbaTXLife.getPrimaryHolding().getPolicy();
									if (newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() == null) {  //NBA093
										LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty insurance = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();  //NBA093
										insurance.setLife(new Life());
										newPolicy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(insurance);  //NBA093
									}
									Coverage coverageCopy = coverage.clone(true); // Clone the coverage and add the benefit
									List benefits = coverage.getCovOption();
									if (endorsement.getRelatedObjectID() != null) { // add the Benefit
										for (int b = 0; b < benefits.size(); b++) {
											CovOption covOption = (CovOption) benefits.get(b);
											if (endorsement.getRelatedObjectID().equals(covOption.getId())) { //NBA093
												coverageCopy.addCovOption(covOption);
												break;
											}
										}
									}
									newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife().addCoverage(coverageCopy);  //NBA093
									break;
								}
							}
						}
					}
					Annuity annuity = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity();  //NBA093
					if (annuity != null) {
						Policy newPolicy = newNbaTXLife.getPrimaryHolding().getPolicy();		//SPR1163
						constructEndorsementMessageForAnnuity(newPolicy, annuity, endorsement);		//SPR1163
					} else if ((endorsement.getRelatedObjectType() == OLI_COVOPTION) && (endorsement.getRelatedObjectID() != null)) {
						List riders = annuity.getRider();
						for (int c = 0; c < riders.size(); c++) {
							Rider rider = (Rider) riders.get(c);
							List benefits = rider.getCovOption();
							for (int b = 0; b < benefits.size(); b++) {
								CovOption covOption = (CovOption) benefits.get(b);
								if (endorsement.getRelatedObjectID().equals(covOption.getId())) {
									Policy newPolicy = newNbaTXLife.getPrimaryHolding().getPolicy();
									if (newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() == null) {
										LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty insurance = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
										insurance.setAnnuity(new Annuity());
										newPolicy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(insurance);
									}
									Rider riderCopy = rider.clone(true); // Clone the Rider and add the benefit to a new extension
									riderCopy.addCovOption(covOption);
									newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity().addRider(riderCopy);
									break;
								}
							}
						}
					//end NBA093
					}
					if (endorsement.getAppliesToPartyID() != null) { // add the Party and Relation NBA093
						List parties = contract.getOLifE().getParty();
						for (int p = 0; p < parties.size(); p++) {
							Party party = (Party) parties.get(p);
							if (party.getId().equals(endorsement.getAppliesToPartyID())) { //NBA093
								newNbaTXLife.getOLifE().addParty(party.clone(true));
								Relation primaryRelation = getPrimaryRelationForParty(party, contract.getOLifE().getRelation());
								if (primaryRelation != null) {
									newNbaTXLife.getOLifE().addRelation(primaryRelation);
								} else {}
								break;
							}
						}
					}
					Policy newPolicy = newNbaTXLife.getPrimaryHolding().getPolicy();
					OLifEExtension newExtension = NbaTXLife.createOLifEExtension(EXTCODE_POLICY); // Create a new PolicyExtension
					newPolicy.addOLifEExtension(newExtension);
					// SPR3290 code deleted
					endorsement.setDataRep(Endorsement.DataRep.REMOVED); // NBA006  NBA093
					newPolicy.addEndorsement(endorsement); //NBA093
					NbaTXLife response =
						new NbaBackEndAdapterFacade().submitRequestToHost(newNbaTXLife, createRequestMessage(contract, request).getWorkitemId());
					TransResult result =
						response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();
					updateTransResult(contract, result);	//ACN013
					if (result.getResultCode() > 1) {
						ArrayList errs = new ArrayList();	//ACN013
						errs.add(result);	//ACN013
						endorsement.applyResult(NbaActionIndicator.ACTION_FAILED, errs);	//ACN013
					} else {
						endorsement.applyResult(NbaActionIndicator.ACTION_SUCCESSFUL, new ArrayList());	//ACN013
					}
				}
			}
		}
	}
	// End NBA036
	/**
	 * Create a TxLife 151 transactions for a new SubstandardRating
	 * @param userVO
	 * @param nbaTXLife
	 * @param coverage - the Coverage containing the SubstandardRating
	 * @param lifeParticipant - the LifeParticipant containing the SubstandardRating
	 * @param substandardRating - the SubstandardRating
	 */
	//NBA093 new method
	protected static TransResult generate151Trans(
			NbaTXLife nbaTXLife,
			NbaTXRequestVO request,
			Coverage coverage,
			LifeParticipant lifeParticipant,
			SubstandardRating substandardRating)
				throws NbaBaseException {
		request.setTransType(TC_TYPE_SUBSTANDARDRATING);
		request.setTransMode(TC_MODE_UPDATE);
		request.setObjectType(OLI_LIFECOVERAGE);
		request.setTranContentCode(TC_CONTENT_INSERT);
		NbaTXLife newNbaTXLife = new NbaTXLife(request);
		Policy newPolicy = newNbaTXLife.getPrimaryHolding().getPolicy();
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty insurance = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
		newPolicy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(insurance);
		Life life = new Life();
		insurance.setLife(life);
		life.addCoverage(coverage.clone(true));
		Coverage newCoverage = life.getCoverageAt(0);
		newCoverage.addLifeParticipant(lifeParticipant.clone(true));
		LifeParticipant newLifeParticipant = newCoverage.getLifeParticipantAt(0);
		newLifeParticipant.addSubstandardRating(substandardRating.clone(false));
		SubstandardRating newSubstandardRating = newLifeParticipant.getSubstandardRatingAt(0);
		newSubstandardRating.setDataRep(Endorsement.DataRep.FULL);
		NbaParty nbaParty = nbaTXLife.getParty(lifeParticipant.getPartyID());
		if (nbaParty != null) {
			Party party = nbaParty.getParty();
			newNbaTXLife.getOLifE().addParty(party);
			Relation primaryRelation = getPrimaryRelationForParty(party, nbaTXLife.getOLifE().getRelation());
			if (primaryRelation != null) {
				newNbaTXLife.getOLifE().addRelation(primaryRelation);
			}
		}
		NbaTXLife response =
			new NbaBackEndAdapterFacade().submitRequestToHost(newNbaTXLife, createRequestMessage(nbaTXLife, request).getWorkitemId());
		return response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransResult();
	}
	/**
	 * Store the TransResult in the NbaTXLife
	 * @param nbaTXLife
	 * @param transResult
	 */
	// ACN013 New Method
	protected static void updateTransResult(NbaTXLife nbaTXLife, TransResult transResult) {
		UserAuthResponseAndTXLifeResponseAndTXLifeNotify uaratratn = nbaTXLife.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
		if (uaratratn.getTXLifeResponseCount() > 0) {
			TXLifeResponse response = uaratratn.getTXLifeResponseAt(0);
			response.setTransResult(transResult);
		}
	}
	/**
	 * Assemble the data necessary for adding or deleting an Endorsement on an Annuity.
	 * @param newPolicy - the new Policy object which data will be added to
	 * @param annuity - the Annuity object from the original contract
	 * @param endorsement - the Endorsement being added or deleted
	 */
	// SPR1163 New Method 
	protected static void constructEndorsementMessageForAnnuity(Policy newPolicy, Annuity annuity, Endorsement endorsement) {
		if ((OLI_ANNUITY == endorsement.getRelatedObjectType()) && (annuity.getId().equals(endorsement.getRelatedObjectID()))) {
			if (newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() == null) {
				LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty insurance = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
				insurance.setAnnuity(annuity.clone(true));
				newPolicy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(insurance);
			}
		} else if ((OLI_ANNRIDER == endorsement.getRelatedObjectType()) && (endorsement.getRelatedObjectID() != null)) {
			List riders = annuity.getRider();
			for (int c = 0; c < riders.size(); c++) {
				Rider rider = (Rider) riders.get(c);
				if (rider.getId().equals(endorsement.getRelatedObjectID())) {
					LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty insurance =
						newPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
					if (insurance == null) {
						insurance = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
						insurance.setAnnuity(new Annuity());
						newPolicy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(insurance);
					}
					Rider riderCopy = rider.clone(true); // Clone the Rider
					insurance.getAnnuity().addRider(riderCopy);
				}
			}
		} else if ((OLI_COVOPTION == endorsement.getRelatedObjectType()) && (endorsement.getRelatedObjectID() != null)) {
			String id = endorsement.getRelatedObjectID();
			Rider rider;
			CovOption covOption;
			int riderCount = annuity.getRiderCount();
			int covOptionCount;
			main : for (int c = 0; c < riderCount; c++) {
				rider = annuity.getRiderAt(c);
				covOptionCount = rider.getCovOptionCount();
				for (int j = 0; j < covOptionCount; j++) { // SPR3290
					covOption = rider.getCovOptionAt(j);
					if (id.equals(covOption.getId())) {
						Annuity annuityCopy = annuity.clone(true);
						Rider riderCopy = rider.clone(true);
						riderCopy.addCovOption(covOption);
						annuityCopy.addRider(riderCopy);
						LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladh = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
						ladh.setAnnuity(annuityCopy);
						newPolicy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(ladh);
						break main;
					}
				}
			}
		}
	}
	/**
	 * Build request for adding or receipting a requirement
     * @param request
     * @param businessProcess
     * @param holding
     * @param i
     * @param reqInfo
     * @return requirement count
     */
	
	//NBA130 New Method
    protected static int processRequirementUpdate(NbaTXRequestVO request, String businessProcess, Holding holding, int i, RequirementInfo reqInfo) {

        //Check to see if this Post requirement has already submitted the requirement
        if (null != reqInfo.getHORequirementRefID() && businessProcess.equalsIgnoreCase(PROC_POST_REQUIREMENT)) {
            //if the requirement has already been submitted by Post, bypass the host adapter
            holding.getPolicy().removeRequirementInfoAt(i);
            --i;
        } else {
            if (reqInfo.getActionIndicator().isAdd()) { // SPR3290
                request.setTransMode(TC_MODE_UPDATE);
                request.setTranContentCode(TC_CONTENT_INSERT);
                // NBA036 code deleted
            } else if (reqInfo.getActionIndicator().isUpdate()) { // SPR3290
                request.setTransMode(TC_MODE_UPDATE);
                request.setTranContentCode(TC_CONTENT_UPDATE);
                // NBA036 code deleted
                //begin NBA036
            } else {
                holding.getPolicy().removeRequirementInfoAt(i);
                --i;
            }
        } 
        return i;
    }
} //END OF CLASS
