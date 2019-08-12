package com.csc.fsg.nba.transaction.validation;

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
 * 
 * *******************************************************************************<BR>
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.contract.validation.NbaContractValidationCommon;
import com.csc.fsg.nba.contract.validation.NbaContractValidationConstants;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkFormatter;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaTransactionValidationException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaContractApprovalDispositionRequest;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaProducerVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.CarrierAppointmentExtension;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.Endorsement;
import com.csc.fsg.nba.vo.txlife.EndorsementExtension;
import com.csc.fsg.nba.vo.txlife.ImpairmentInfo;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.LifeParticipantExtension;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.ReinsuranceInfo;
import com.csc.fsg.nba.vo.txlife.ReinsuranceOffer;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;
import com.csc.fsg.nba.vo.txlife.UnderwritingResult;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.results.TransValMessage;
import com.csc.fsg.nba.vpms.results.TransValResult;


/**
 * Implementation class for basic transaction validations. The value of 900 in 
 * BusValidations.subset indicates the transaction validation will be evaluated 
 * using the transaction validation VP/MS model.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA094</td><td>Version 3</td><td>Transaction Validation</td></tr>
 * <tr><td>SPR1823</td><td>Version 4</td><td>Issue porcess send case to Error queue</td></tr>
 * <tr><td>NBA115</td><td>Version 5</td><td>Credit card payment and authorization</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>NBA122</td><td>Version 5</td><td>Underwriter Workbench Rewrite</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>AXAL3.7.09</td><td>AXA Life Phase 1</td><td>Underwriter Workbench</td></tr>
 * <tr><td>NBA224</td><td>Version 8</td><td>nbA Underwriter Workbench Requirements and Impairments Enhancement</td></tr>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * <tr><td>ALPC075</td><td>AXA Life Phase 1</td><td>State Variations on Exclusion Riders</td></tr>
 * <tr><td>ALS3753</td><td>AXA Life Phase 1</td><td>QC # 1973-Case status changed to Offer Acceptance Pending and it should not</td></tr>
 * <tr><td>ALS5102</td><td>AXA Life Phase 1</td><td>QC # 3167  - Policy 110350696 is rejected in EIB</td></tr>
 * <tr><td>P2AXAL016</td><td>AXA Life Phase 2</td><td>Product Validation</td></tr>
 * <tr><td>P2AXAL062</td><td>AXA Life Phase 2 R2</td><td>UWWB R2</td></tr>
 * <tr><td>SR657319</td><td>Discretionary</td><td>Manual selection of Rate Class</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaTransactionValidationBasic implements NbaTransactionValidation {//P2AXAL016
	private NbaLogger logger;
	
	private NbaTXLife txLife; 	//ALS3898
	private NbaDst nbaDst; 		//ALS3898
	private NbaUserVO userVO;	//ALS3898
	private AccelProduct nbaProduct;//P2AXAL016
	
	//NBA213 deleted code
	/**
	 * Perform the validation as defined in TransactionValidation VPMS model. This method is the
	 * entry point for basic transaction validation implementation class. 
	 * It invokes TransactionValidation VPMS model for the case as well as the child transactions 
	 * @param nbaTXLife the NbaTXLife instance
	 * @param nbaDst the NbaDst object
	 * @param userVO the NbaUserVO object
	 * @throws NbaBaseException 
	 */
	public void validate(NbaTXLife txLife, NbaDst nbaDst, NbaUserVO userVO, AccelProduct nbaProduct) throws NbaBaseException {//P2AXAL016
		setTxLife(txLife); //ALS3898
		setNbaDst(nbaDst); //ALS3898
		setUserVO(userVO); //ALS3898
		setNbaProduct(nbaProduct);//P2AXAL016
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(nbaDst.getNbaLob());
		oinkData.setContractSource(txLife);
		oinkData.setPlanSource(txLife, nbaProduct);//P2AXAL016
		oinkData.setAcdbSource(new NbaAcdb(), txLife);//P2AXAL016

		try {
			//Begin NBA115
			if (NbaConstants.PROC_VIEW_CREDIT_CARD_PAYMENT.equalsIgnoreCase(txLife.getBusinessProcess())) { //SPR2639
				int size = 1;
				List transList;
				if (nbaDst.isCase()) {
					transList = nbaDst.getNbaTransactions();
					size = transList.size();
				} else {
					transList = new ArrayList();
					transList.add(nbaDst.getNbaTransaction());
				}
				NbaTransaction newTrans;
				WorkItem payTrans;
				for (int i = 0; i < size; i++) {
					newTrans = (NbaTransaction) transList.get(i);
					payTrans = newTrans.getTransaction(); //NBA208-32
					if ("Y".equalsIgnoreCase(payTrans.getUpdate()) || "Y".equalsIgnoreCase(payTrans.getCreate())) { //NBA208-32
						oinkData = new NbaOinkDataAccess(newTrans.getNbaLob());
						invokeVPMS(oinkData, txLife.getBusinessProcess(), null, null); //NBA122, NBA130
					}
				}
				//begin ALII1082
			} else if (NbaConstants.PROC_REQUIREMENTS.equalsIgnoreCase(txLife.getBusinessProcess())) { //SPR2639
				int size = 1;
				List transList;
				if (nbaDst.isCase()) {
					transList = nbaDst.getNbaTransactions();
					size = transList.size();
				} else {
					transList = new ArrayList();
					transList.add(nbaDst.getNbaTransaction());
				}
				NbaTransaction newTrans;
				WorkItem workTrans;
				for (int i = 0; i < size; i++) {
					newTrans = (NbaTransaction) transList.get(i);
					workTrans = newTrans.getTransaction(); //NBA208-32
					if ("Y".equalsIgnoreCase(workTrans.getUpdate()) || "Y".equalsIgnoreCase(workTrans.getCreate())) {
						oinkData = new NbaOinkDataAccess(newTrans.getNbaLob());
						oinkData.setContractSource(txLife);
						invokeVPMS(oinkData, txLife.getBusinessProcess(), null, null);
					}
				}
				//end ALII1082
			} else {
				ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(txLife.getPolicy().getApplicationInfo()); //APSL3406
				if (appInfoExt != null && !NbaUtils.isNegativeDisposition(appInfoExt.getUnderwritingStatus())) { //APSL3406
					approvalValidation(oinkData, txLife, nbaDst); // AXAL3.7.09 //P2AXAL062
					// P2AXAL062 code deleted
					// invoke VPMS for transacions
					ListIterator listIter = getWorkItems(nbaDst, userVO).listIterator();
					NbaTransaction trans = null;
					RequirementInfo reqInfo = null;
					RequirementInfoExtension reqInfoExt = null;
					while (listIter.hasNext()) {
						trans = (NbaTransaction) listIter.next();
						if (trans.getNbaLob().getWorkType().equals(NbaConstants.A_WT_REQUIREMENT)) {
							oinkData = new NbaOinkDataAccess(trans.getNbaLob());
							// begin NBA122
							Map deOinkMap = new HashMap(); // P2AXAL062
							// Begin NBA130
							reqInfo = txLife.getRequirementInfo(trans.getNbaLob().getReqUniqueID()); // NBA130
							reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo); // NBA130
							NbaOinkRequest oinkRequest = new NbaOinkRequest();
							oinkRequest.setRequirementIdFilter(reqInfo.getId());
							oinkData.setContractSource(txLife);
							// End NBA130
							if (reqInfoExt != null) {
								deOinkMap.put("A_ReviewedInd", Boolean.toString(reqInfoExt.getReviewedInd()));
							} else {
								deOinkMap.put("A_ReviewedInd", Boolean.toString(false));
							}
							// end NBA122
							invokeVPMS(oinkData, txLife.getBusinessProcess(), deOinkMap, oinkRequest); // NBA130
						}
					}
				}
			}
			//End NBA115
		} catch (Exception nbe) {
			getLogger().logException("Transaction Validation error", nbe);
			/*
			 * If the business process is a business function and the transaction validation fails error(s) will be reported back to the user. If the
			 * business process is an automated process and the transaction validation fails the returned error messages will be added to the work
			 * item being processed, the status of the work item being processed will be changed to validation error (VALDERRD) sending the work item
			 * to the error queue.
			 */
			if (nbe instanceof NbaTransactionValidationException) {
				throw (NbaTransactionValidationException) nbe;
			}//NBA186
			throw new NbaBaseException(nbe.getMessage());//NBA186
			//NBA186 code deleted
		}
	}

	/**
	 * This method invokes a VP/MS model to get the results for the OINK resolved variables. If the VPMS model returns a message this message is
	 * wrapped in NbaTransactionValidationException and this exception is thrown to the invoking method
	 * 
	 * @param access
	 *            the NbaOinkDataAccess instance which is passed to the NbaVpmsAdaptor contructor
	 * @param processID
	 *            the business process for which the transaction validation VPMS should be invoked.
	 * @param deOinkMap
	 *            additional attributes needed by the transaction validation VPMS model
	 */
	//NBA122 added the deOinkMap parameter //P2AXAL062 method refactored
	protected long invokeVPMS(NbaOinkDataAccess access, String processID, Map deOinkMap, NbaOinkRequest oinkRequest) throws NbaBaseException {
		NbaVpmsAdaptor adapter = null; //SPR3362
		TransValResult tranResult = null;
		try {
			if (deOinkMap == null) { //NBA122
				deOinkMap = new HashMap(); //NBA122
			} //NBA122
			access.getFormatter().setDateSeparator(NbaOinkFormatter.DATE_SEPARATOR_DASH); //ALPC075
			deOinkMap.put(NbaVpmsConstants.A_PROCESS_ID, processID); //SPR2639
			deOinkMap.put(NbaVpmsConstants.A_UNDERWRITER_WORKBENCH_APPLET, Boolean.toString(NbaConfiguration.getInstance()
					.isUnderwriterWorkbenchApplet())); //NBA122
			adapter = new NbaVpmsAdaptor(access, NbaVpmsAdaptor.TRANSACTION_VALIDATION); //SPR3362
			adapter.setSkipAttributesMap(deOinkMap);
			adapter.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_TRANSACTION_VALIDATION_RESULTS);
			//Begin NBA130
			if (oinkRequest != null) {
				adapter.setANbaOinkRequest(oinkRequest);
			}
			//End NBA130
			NbaVpmsModelResult data = new NbaVpmsModelResult(adapter.getResults().getResult());
			tranResult = data.getVpmsModelResult().getTransValResult();
			ArrayList messages = tranResult.getTransValMessage();
			//SPR3362 code deleted //ALS3898 refactoring
			String errorMessage = "";
			boolean severeErrors = false;
			String msg = "";
			long severity = 0;
			for (int i = 0; i < messages.size(); i++) {
				TransValMessage transMsg = (TransValMessage) messages.get(i);
				msg = transMsg.getMessageText();
				severity = transMsg.getMessageSeverity().intValue();
				if (NbaOliConstants.OLI_MSGSEVERITY_SEVERE == severity) {
					severeErrors = true;
				} else if (NbaOliConstants.OLI_MSGSEVERITY_INFO == severity) {
					addSystemMessage(msg, severity);
				}
				//construct a error msg -
				if (msg.length() > 1) {
					if (errorMessage.length() > 1) {
						errorMessage = errorMessage + ", " + msg.trim();
					} else {
						errorMessage = msg.trim();
					}
				}
			}

			if (errorMessage.length() > 1) {
				//ALS5475 removed code
				if (severeErrors) { //ALS3898
					throw new NbaTransactionValidationException(errorMessage);
				}
			}
		} catch (Exception e) {
			getLogger().logError("Error invoking VPMS Model" + NbaVpmsAdaptor.TRANSACTION_VALIDATION + ". " + e);
			if (e instanceof NbaTransactionValidationException) {
				throw (NbaTransactionValidationException) e;
			} else {
				throw new NbaBaseException("Error invoking VPMS Model" + NbaVpmsAdaptor.TRANSACTION_VALIDATION + e);
			}
			//begin SPR3362
		} finally {
			try {
				if (adapter != null) {
					adapter.remove();
				}
			} catch (Throwable th) {
				LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}
		//end SPR3362
		return tranResult.getEquivalentRating().intValue();
	}

	/**
	 * This method returns an instance of <code>NbaLogger</code>.
	 * 
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass().getName());
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

	
	//NBA213 deleted code
	/**
	 * This method returns a List of all the work items attached to the case.
	 * @param nbaDst Dst object for the case.
	 * @param userVO user value object.
	 * @return List a list of NbaTransaction objects.
	 */
	protected List getWorkItems(NbaDst nbaDst, NbaUserVO userVO) throws NbaBaseException {
		try {
			//NBA213 deleted code
			nbaDst.getNbaTransactions();
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(nbaDst.getID(), true);
			retOpt.requestTransactionAsChild();
			//SPR1823 code deleted
			nbaDst = WorkflowServiceHelper.retrieveWorkItem(userVO, retOpt); //NBA213
			return nbaDst.getNbaTransactions();
		} catch (NbaBaseException nbe) {
			throw nbe;
		} catch (Exception e) {
			throw new NbaBaseException("Error encountered fetching workitems for transaction validation" + e);
		}
	}

	/**
	 * This method calls the Transaction Validation Model and sends in deoink values.
	 * @param nbaDst Dst object for the case.
	 * @param nbaTXLife the NbaTXLife instance
	 * @param oinkData the NbaOinkDataAccess instance which is passed to the invokeVPMS method
	 * @throws NbaBaseException
	 */
	//Begin AXAL3.7.09 //P2AXAL062 method refactored and signature modified
	public void approvalValidation(NbaOinkDataAccess oinkData, NbaTXLife txLife, NbaDst nbaDst) throws NbaBaseException {
		NbaContractValidationCommon common = new NbaContractValidationCommon(txLife);
		oinkData.setAcdbSource(new NbaAcdb(), txLife);
		oinkData.setLobSource(nbaDst.getNbaLob());
		Map deOinkMap = new HashMap();
		int tempCovCount = 0;
		int covOptionCount = 0;
		int tempCovOptionCount = 0;
		LifeParticipant lifeParticipant = null;
		LifeParticipant oinkLifeParticipant = null;

		//variables to handle Joint cases
		int loopCount = 1;
		boolean jointCase = false;
		long equivalentRatingPrimary = 1;//P2AXAL062
		long equivalentRatingJoint = 1;//P2AXAL062
		LifeParticipant lifeParticipantPrimary = null;
		LifeParticipant lifeParticipantJoint = null;
		if (txLife.getPrimaryCoverage().getLivesType() == NbaOliConstants.OLI_COVLIVES_JOINTLTD) {
			loopCount = 2;
			jointCase = true;
		}

		for (int i = 1; i <= loopCount; i++) {
			for (int j = 0, n = 0, p = 0, temp0 = 0, temp1 = 0, temp2 = 0, temp3 = 0, temp4 = 0, temp5 = 0; j < txLife.getLife().getCoverageCount(); j++) {
				Coverage coverage = txLife.getLife().getCoverageAt(j);
				if (coverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_BASE) {
					if (i == 1) {
						lifeParticipant = lifeParticipantPrimary = NbaUtils.findPrimaryInsuredLifeParticipant(coverage);
					} else {
						lifeParticipant = lifeParticipantJoint = NbaUtils.findJointInsuredLifeParticipant(coverage);
						temp0 = 0;
						temp1 = 0;
						n = 0;
						p = 0;//Reinitialise internal temporary variables for joint only
						if (lifeParticipant == null) {//Joint case but no joint insured entered
							continue;
						}
					}
					oinkLifeParticipant = lifeParticipant;
				} else {
					lifeParticipant = NbaUtils.findOtherInsuredLifeParticipant(coverage);
					//ALII948 Code Deleted
				}

				covOptionCount += coverage.getCovOptionCount();
				if (tempCovCount == 0) {
					deOinkMap.put("A_CoverageKey", txLife.getLife().getCoverageAt(j).getProductCode());
				} else {
					deOinkMap.put("A_CoverageKey[" + tempCovCount + "]", txLife.getLife().getCoverageAt(j).getProductCode());
				}

				//Deoink Base Coverage
				deOinkBaseCoverageValues(lifeParticipant, deOinkMap, coverage);
				//Deoink Rate Class
				deOinkPersonValues(nbaDst, oinkData, txLife, lifeParticipant, deOinkMap, tempCovCount);

				//Deoink Coverage Values
				deOinkCoverageValues(deOinkMap, lifeParticipant, tempCovCount, temp1, temp2, temp3, n);
				//Deoink Coverage Options
				deOinkCoverageOptionValues(txLife, deOinkMap, coverage, lifeParticipant, tempCovOptionCount, covOptionCount, temp0, temp4, temp5, p,
						n);
				//Deoink LifeParticipant specific data
				deOinkLifeParticipantValues(nbaDst, oinkData, txLife, lifeParticipant, deOinkMap, tempCovCount);
				//Deoink reinsurance Info
				deOinkReinsuranceInfo(txLife, deOinkMap, coverage);//ALS5647
				deOinkReinsuranceResultInfo(txLife, deOinkMap, coverage);//NBLXA-1331
				//APSL2903 QC11162
				if (NbaOliConstants.OLI_COVIND_RIDER == coverage.getIndicatorCode()) {
				deOinkLifeParticipantRateClass(nbaDst, oinkData, txLife, deOinkMap, coverage);
				}
				
				deOinkMIBFollowUpReviewInd(txLife, deOinkMap);//NBLXA-2433 US 372704
				
				tempCovOptionCount += covOptionCount;
				tempCovCount++;

				//Reinitialise temporary variables before looping thru other Coverages
				temp2 = 0;
				temp3 = 0;
				temp4 = 0;
				temp5 = 0;

			}
			//Begin NBLXA-2223 
			if (txLife.getPolicy().getApplicationInfo()!= null) {
				ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(txLife.getPolicy().getApplicationInfo());
				if(appInfoExt.getInternationalUWProgInd() && !(isRetailCaseWithForeignUnd(txLife)||isWholeSaleCase(txLife))){
					deOinkMap.put("A_RetailCaseIUPQualified", NbaConstants.FALSE_STR); 
				}
			}
			//End NBLXA-2223 
			//Deoink Policy Level Info
			deOinkMap.put("A_no_of_Coverages", String.valueOf(tempCovCount));
			deOinkMap.put("A_no_of_CovOption", (String.valueOf(covOptionCount)));

			deOinkMap.put("A_RatingAllowed", isRatingAllowed(txLife) ? "1" : "0");//ALS5601

			deOinkImpairmentsInfo(txLife, deOinkMap); //NBA224
			deOinkEndorsementValues(txLife, deOinkMap);//ALPC075

			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			setOinkRequestKeys(oinkRequest, txLife, nbaDst, oinkLifeParticipant.getPartyID());
			long equivalentRating = invokeVPMS(oinkData, txLife.getBusinessProcess(), deOinkMap, oinkRequest); //NBA122, NBA130, AXAL3.7.09
			if(equivalentRating == 0){//If VPMS did not find equivalent rating then pick the applied one
				equivalentRating = getTableRatingApplied(oinkLifeParticipant); //P2AXAL062
			}
			
			if (i == 1) {
				equivalentRatingPrimary = equivalentRating; //P2AXAL062
			} else {
				equivalentRatingJoint = equivalentRating; //P2AXAL062
			}

			//Reinitialise all variables before calling VPMS for Joint Insured
			tempCovCount = 0;
			covOptionCount = 0;
			tempCovOptionCount = 0;
			deOinkMap = new HashMap();
		}

		if (jointCase && isValidBusinessProcess(txLife.getBusinessProcess())) {
			if (!common.validateJointAgeRatingLimits(txLife.getPrimaryCoverage(), lifeParticipantPrimary, equivalentRatingPrimary,
					lifeParticipantJoint, equivalentRatingJoint)) {
				throw new NbaTransactionValidationException("Primary Insured's Age: " + lifeParticipantPrimary.getIssueAge() + ", Underwriting Class: "
						+ common.getDescription(lifeParticipantPrimary.getUnderwritingClass(), NbaTableConstants.OLI_LU_UNWRITECLASS)
						+ ", Equivalent Rating: " + common.getEquivalentRatingText(equivalentRatingPrimary) 
						+ ", is not allowed with, Joint Insured's Age: " + lifeParticipantJoint.getIssueAge() + ", Underwriting Class: "
						+ common.getDescription(lifeParticipantJoint.getUnderwritingClass(), NbaTableConstants.OLI_LU_UNWRITECLASS)
						+ ", Equivalent Rating: " + common.getEquivalentRatingText(equivalentRatingJoint)); //P2AXAL062
			}
		}
	}

	/**
	 * MIB Follow UP Plan F Review pending - NBLXA-2433 US 372704
	 * @param txLife
	 * @param deOinkMap
	 */
	private void deOinkMIBFollowUpReviewInd(NbaTXLife txLife, Map deOinkMap) {
		boolean isMIBFollowUpReviewPending = NbaUtils.isMIBFollowUpReviewPending(txLife);
		if (isMIBFollowUpReviewPending) {
			deOinkMap.put("A_MIBFollowUpReviewPending", String.valueOf(isMIBFollowUpReviewPending));
		}
	}

	//End AXAL3.7.09
	//NBLXA-2223 new method
	protected boolean isRetailCaseWithForeignUnd(NbaTXLife txLife){
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(txLife.getPolicy());
		if(policyExtension.hasDistributionChannel()&&
				policyExtension.getDistributionChannel()== NbaOliConstants.OLI_DISTCHAN_10){
			List producers = txLife.getProducers();
			double volumeSharePct = 0.0;
			for (int i = 0; i < producers.size(); i++) {				
				CarrierAppointmentExtension cAE = ((NbaProducerVO) producers.get(i)).getCarrierAppointmentExtension();
				if (cAE != null && cAE.getForeignUWAllowedInd()) {	
					volumeSharePct =volumeSharePct +((NbaProducerVO) producers.get(i)).getVolumeSharePct();
					if (volumeSharePct>= 25) {
						return true;
					}
				}
			}
		}
		return false;
	}

	
	//NBLXA-2223 new method
	protected boolean isWholeSaleCase(NbaTXLife txLife){
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(txLife.getPolicy());
		if(policyExtension.hasDistributionChannel()&&
				policyExtension.getDistributionChannel()== NbaOliConstants.OLI_DISTCHAN_6){
			return true;
		}
		return false;
	}

		/**
	 * NBLXA-1331
	 * @param txLife
	 * @param deOinkMap
	 * @param coverage
	 */
	private void deOinkReinsuranceResultInfo(NbaTXLife txLife, Map deOinkMap, Coverage coverage) {
		boolean acceptRejectcode = true;
		deOinkMap.put("A_TentDispApproved", String.valueOf(isTentativeDispositionApproved(txLife.getPolicy().getApplicationInfo())));
		ReinsuranceInfo reinsInfo = null;
		if (hasFacultativeReinsurance(coverage)) {
			for (int i = 0; i < coverage.getReinsuranceInfoCount(); i++) {
				reinsInfo = coverage.getReinsuranceInfoAt(i);
				if (reinsInfo.getReinsuranceRiskBasis() == NbaOliConstants.OLI_REINRISKBASE_FA) {
					if(null != reinsInfo.getCarrierPartyID()){
						List<ReinsuranceOffer> reInsOfferList = NbaUtils.getReinsuranceOffer(coverage, reinsInfo.getCarrierPartyID());
						if(reInsOfferList != null && reInsOfferList.size() > 0){
							for (int j = 0; j < reInsOfferList.size(); j++) {
								ReinsuranceOffer reinsuranceOffer = reInsOfferList.get(j);
								if(reinsuranceOffer.getAcceptRejectCode() == NbaConstants.LONG_NULL_VALUE){
									acceptRejectcode = false;
								}
							}
							
						}else{
							acceptRejectcode = false;
						}
					}
					
				}
			}
		}

		//NBLXA-1494
		if(!acceptRejectcode){
			deOinkMap.put("A_AcceptRejectCode", String.valueOf(acceptRejectcode));
		}					
	}

	/**
	 * This method returns true if the rating is allowed on Coverage.
	 * @param NbaTXLife txLife
	 */
	//New Method ALS5601
	public boolean isRatingAllowed(NbaTXLife txLife) {
		if (txLife != null) {
			Coverage coverage = NbaUtils.getCoverage(txLife.getLife(), NbaOliConstants.OLI_COVTYPE_CHILDTERM);
			if (coverage != null) {
				List lifeParticipants = coverage.getLifeParticipant();
				for (int i = 0; i < lifeParticipants.size(); i++) {
					LifeParticipant lifeParticipant = (LifeParticipant) lifeParticipants.get(i);
					if (hasNewAddedSubstandardRating(lifeParticipant.getSubstandardRating())) {
						return false;
					}
				}
				CovOption dpwCovOption = NbaUtils.getCovOption(coverage, NbaConstants.PRODUCTCODE_DPW);
				if (dpwCovOption != null) {
					if (hasNewAddedSubstandardRating(dpwCovOption.getSubstandardRating())) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * This method returns true if the List has any new added SubstandardRating
	 * @param NbaTXLife txLife
	 */
	//New Method ALS5601
	public boolean hasNewAddedSubstandardRating(List subStandardRatings) {
		if (subStandardRatings != null) {
			for (int k = 0; k < subStandardRatings.size(); k++) {
				SubstandardRating rating = (SubstandardRating) subStandardRatings.get(k);
				if (rating.isActionAdd()) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * This method deOinks Person's rateclass values by coverage.
	 * @param NbaDst nbaDst
	 * @param NbaOinkDataAccess oinkData
	 * @param NbaTXLife txLife
	 * @param LifeParticipant lifeParticipant
	 * @param Map deOinkMap
	 * @param int covCnt
	 * @throws NbaBaseException
	 */
	//Begin AXAL3.7.09
	public void deOinkPersonValues(NbaDst nbaDst, NbaOinkDataAccess oinkData, NbaTXLife txLife, LifeParticipant lifeParticipant, Map deOinkMap, 	int covCnt) throws NbaBaseException {
		String partyID = lifeParticipant.getPartyID();
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		setOinkRequestKeys(oinkRequest, txLife, nbaDst, partyID);
		oinkRequest.setVariable("PersonRateClass");
		String rateClass[] = oinkData.getStringValuesFor(oinkRequest);
		// SR657319 Begin
		oinkRequest.setVariable("ApprovedRateClass");
		String approvedRateClass = oinkData.getStringValueFor(oinkRequest);
		// SR657319 End
		oinkRequest.setVariable("PrfTobaccoPremiumBasis");
		if (covCnt == 0) {
			deOinkMap.put("A_CoveragePersonRateClass",rateClass.length > 0 ? rateClass[0] : null); //APSL2580
			deOinkMap.put("A_CoveragePersonApprovedRateClass", approvedRateClass); // SR657319, ALII1488
		} else {
			deOinkMap.put("A_CoveragePersonRateClass[" + covCnt + "]", rateClass.length > 0 ? rateClass[0] : null); //ALII66
			deOinkMap.put("A_CoveragePersonApprovedRateClass[" + covCnt + "]", approvedRateClass); // SR657319, ALII1488
		}
		return;
	}
	//End AXAL3.7.09

	/**
	 * This method deOinks Person's rateclass values by coverage.
	 * @param NbaDst nbaDst
	 * @param NbaOinkDataAccess oinkData
	 * @param NbaTXLife txLife
	 * @param LifeParticipant lifeParticipant
	 * @param Map deOinkMap
	 * @param int covCnt
	 * @throws NbaBaseException
	 */
	//P2AXAL062 new method
	public void deOinkLifeParticipantValues(NbaDst nbaDst, NbaOinkDataAccess oinkData, NbaTXLife txLife, LifeParticipant lifeParticipant,
			Map deOinkMap, int covCnt) throws NbaBaseException {
		String partyID = lifeParticipant.getPartyID();
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		setOinkRequestKeys(oinkRequest, txLife, nbaDst, partyID);
		oinkRequest.setVariable("IssueAge");
		String issueAge = oinkData.getStringValueFor(oinkRequest);//Age
		oinkRequest.setVariable("Gender");
		String gender = oinkData.getStringValueFor(oinkRequest); // Gender
		oinkRequest.setVariable("RelationRoleCode");
		String relationRoleCode = oinkData.getStringValueFor(oinkRequest); // Person Code
		oinkRequest.setVariable("SumSmkPremium");
		String sumSmkPremium = oinkData.getStringValueFor(oinkRequest); // Person Code

		if (covCnt == 0) {
			deOinkMap.put("A_IssueAge", issueAge);
			deOinkMap.put("A_Gender", gender);
			deOinkMap.put("A_RelationRoleCode", relationRoleCode);
			deOinkMap.put("A_SumSmkPremium", sumSmkPremium);
		} else {
			deOinkMap.put("A_IssueAge[" + covCnt + "]", issueAge);
			deOinkMap.put("A_Gender[" + covCnt + "]", gender);
			deOinkMap.put("A_RelationRoleCode[" + covCnt + "]", relationRoleCode);
			deOinkMap.put("A_SumSmkPremium[" + covCnt + "]", sumSmkPremium);
		}
		return;
	}

	//End AXAL3.7.09


	
	/**
	 * This method deOinks Base Coverage values.
	 * @param LifeParticipant lifeParticipant
	 * @param Map deOinkMap
	 * @param int substandardRatingCount
	 * @param Coverage coverage
	 * @throws NbaBaseException
	 */
	//Begin AXAL3.7.09 //P2AXAL062 modified signature
	public void deOinkBaseCoverageValues(LifeParticipant lifeParticipant, Map deOinkMap, Coverage coverage) {
		int baseCovTotalFlatExtraAmt = 0;
		int chkBasePremAdded = 0;
		int substandardRatingCount = lifeParticipant.getSubstandardRatingCount();//P2AXAL062
		if (coverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_BASE) {
			deOinkMap.put("A_BaseProductCode", coverage.getProductCode());
			SubstandardRating substandardRating = null;
			SubstandardRatingExtension subStdRatXtn = null;
			for (int k = 0; k < substandardRatingCount; k++) {
				substandardRating = lifeParticipant.getSubstandardRatingAt(k);
				subStdRatXtn = NbaUtils.getFirstSubstandardExtension(substandardRating);
				if (subStdRatXtn != null && chkBasePremAdded == 0 && subStdRatXtn.hasPermFlatExtraAmt()	&& !subStdRatXtn.getProposedInd() && !substandardRating.isActionDelete()) { //ALS4151
					baseCovTotalFlatExtraAmt++;
					chkBasePremAdded++;
					break;
				}
			}
			ArrayList tempFlatDuration = new ArrayList();
			for (int k = 0; k < substandardRatingCount; k++) {
				substandardRating = lifeParticipant.getSubstandardRatingAt(k);
				subStdRatXtn = NbaUtils.getFirstSubstandardExtension(substandardRating);
				if (subStdRatXtn != null && substandardRating.hasTempFlatExtraAmt() && !subStdRatXtn.getProposedInd() && !substandardRating.isActionDelete()) { //ALS4151
					if (tempFlatDuration.size() == 0) {
						tempFlatDuration.add(String.valueOf(subStdRatXtn.getDuration()));
					} else {
						for (int i = 0; i < tempFlatDuration.size(); i++) {
							if (String.valueOf(subStdRatXtn.getDuration()).equals(tempFlatDuration.get(i))) {
								continue;
							} else {
								tempFlatDuration.add(String.valueOf(subStdRatXtn.getDuration()));
							}
						}
					}
				}
			}
			baseCovTotalFlatExtraAmt = baseCovTotalFlatExtraAmt + tempFlatDuration.size();
			deOinkMap.put("A_BaseCoverageFlatExtra", String.valueOf(baseCovTotalFlatExtraAmt));
		}
		return;
	}
	//	End AXAL3.7.09

	/**
	 * This method deOinks Coverage Values.
	 * @param NbaTXLife txLife
	 * @param Map deOinkMap
	 * @param Coverage coverage
	 * @param LifeParticipant lifeParticipant
	 * @param int substandardRatingCount
	 * @param int tempCovCount
	 * @param int temp1
	 * @param int temp2
	 * @param int temp3
	 * @param int n
	 * @throws NbaBaseException
	 */
	//Begin AXAL3.7.09 //P2AXAL062 modified signature
	public void deOinkCoverageValues(Map deOinkMap, LifeParticipant lifeParticipant, int tempCovCount, int temp1, int temp2, int temp3, int n) {
		
		SubstandardRating substandardRating = null;
		SubstandardRatingExtension substandardRatingXtn = null;
		int substandardRatingCount = lifeParticipant.getSubstandardRatingCount();//P2AXAL062
		String reasonCount = "0"; //ASL5102
		for (int k = 0; k < substandardRatingCount; k++) {
			substandardRating = lifeParticipant.getSubstandardRatingAt(k);
			substandardRatingXtn = NbaUtils.getFirstSubstandardExtension(substandardRating);
			if (substandardRatingXtn != null && !substandardRatingXtn.getProposedInd() && !substandardRating.isActionDelete()) { //ALS4085 //ALS4151
				reasonCount = getReasonCount(substandardRatingXtn.getUnderwritingResult()); //ALS5102
				if (substandardRating.hasPermTableRating()) {
					if (temp1 == 0 && tempCovCount == 0) {
						deOinkMap.put("A_PermFlatExtraRating", String.valueOf(substandardRating.getPermTableRating()));
						deOinkMap.put("A_NoofPermTableRatingReasons", reasonCount); //ALS5102
						temp1++;
					} else {
						deOinkMap.put("A_PermFlatExtraRating[" + tempCovCount + ";" + temp1 + "]", String.valueOf(substandardRating
								.getPermTableRating()));//ALS4662
						deOinkMap.put("A_NoofPermTableRatingReasons[" + temp1 + "]", reasonCount); //ALS5102
						temp1++;//ALS4662
					}
				}
				if (substandardRatingXtn.hasPermFlatExtraAmt()) {
					if (temp2 == 0 && tempCovCount == 0) {
						deOinkMap.put("A_PermFlatExtraAmt", String.valueOf(substandardRatingXtn.getPermFlatExtraAmt()));
						deOinkMap.put("A_NoofPermFlatExtraRatingReasons", reasonCount); //ALS5102
						temp2++;
					} else {
						deOinkMap.put("A_PermFlatExtraAmt[" + tempCovCount + ";" + temp2 + "]", String.valueOf(substandardRatingXtn
								.getPermFlatExtraAmt()));
						deOinkMap.put("A_NoofPermFlatExtraRatingReasons[" + temp2 + "]", reasonCount); //ALS5102
						temp2++;
					}
				}
				if (substandardRating.hasTempFlatExtraAmt()) {
					if (temp3 == 0 && tempCovCount == 0) {
						deOinkMap.put("A_TempFlatExtraAmt", String.valueOf(substandardRating.getTempFlatExtraAmt()));
						deOinkMap.put("A_NoofTempFlatExtraRatingReasons", reasonCount); //ALS5102
						temp3++;
					} else {
						deOinkMap.put("A_TempFlatExtraAmt[" + tempCovCount + ";" + temp3 + "]", String.valueOf(substandardRating
								.getTempFlatExtraAmt()));
						deOinkMap.put("A_NoofTempFlatExtraRatingReasons[" + temp3 + "]", reasonCount); //ALS5102
						temp3++;
					}
					if (n == 0 && tempCovCount == 0) {
						deOinkMap.put("A_TempFlatExtraAmtDuration", String.valueOf(substandardRatingXtn.getDuration()));
						n++;
					} else {
						deOinkMap.put("A_TempFlatExtraAmtDuration[" + tempCovCount + ";" + n + "]", String
								.valueOf(substandardRatingXtn.getDuration()));
						n++;
					}
				}
			}
		}
		if (tempCovCount == 0) {
			deOinkMap.put("A_NoofPermFlatExtra", String.valueOf(temp2));
			deOinkMap.put("A_NoofTempFlatExtra", String.valueOf(temp3));
			deOinkMap.put("A_NoofPermTableRating", String.valueOf(temp1));//ALS4662
		} else {
			deOinkMap.put("A_NoofPermFlatExtra[" + tempCovCount + "]", String.valueOf(temp2));
			deOinkMap.put("A_NoofTempFlatExtra[" + tempCovCount + "]", String.valueOf(temp3));
			deOinkMap.put("A_NoofPermTableRating[" + tempCovCount + "]", String.valueOf(temp1));//ALS4662
			
		}
		return;
	}

	/**
	 * Counts the number of UnderwritingResult objects not marked as delete.
	 * @param underwritingResult
	 * @return count of UnderwritingResult object not deleted
	 */
	//ALS5102 New Method
	private String getReasonCount(ArrayList underwritingResult) {
		int count = 0;
		if( !underwritingResult.isEmpty()) {
			UnderwritingResult uwResult = null;
			for (int i=0; i < underwritingResult.size(); i++) {
				uwResult = (UnderwritingResult)underwritingResult.get(i);
				if (!uwResult.isActionDelete()) {
					count++;
				}
			}
		}
		return String.valueOf(count);
	}

	//	End AXAL3.7.09

	/**
	 * This method deOinks Coverage Option Values.
	 * @param NbaTXLife txLife
	 * @param Map deOinkMap
	 * @param Coverage coverage
	 * @param int tempCovOptionCount
	 * @param int covOptionCount
	 * @param int temp0
	 * @param int temp4
	 * @param int temp5
	 * @param int p
	 * @param int n
	 * @throws NbaBaseException
	 */
	//Begin AXAL3.7.09 //P2AXAL062 signature modified
	public void deOinkCoverageOptionValues(NbaTXLife txLife, Map deOinkMap, Coverage coverage, LifeParticipant lifeParticipant, int tempCovOptionCount, int covOptionCount, int temp0,
			int temp4, int temp5, int p, int n) {
		
		for (int k = tempCovOptionCount, temp = 0; k < covOptionCount; k++, temp++) {
			CovOption covOption = coverage.getCovOptionAt(temp);
			String partyid = lifeParticipant.getPartyID();//P2AXAL062
			NbaParty nbaParty = txLife.getParty(partyid);
			PersonExtension covOptionPersonXtn = NbaUtils.getFirstPersonExtension(nbaParty.getPerson());
			int ratingOnCovOpt = covOption.getSubstandardRatingCount();

			if (covOption.getCovOptionStatus() != NbaOliConstants.OLI_POLSTAT_DECISSUE) { // APSL 2768
				for (int r = 0; r < ratingOnCovOpt; r++) {
					SubstandardRating substandardRatingOnCovOpt = covOption.getSubstandardRatingAt(r);
					if (!substandardRatingOnCovOpt.isActionDelete()) { //ALS4151
						SubstandardRatingExtension substandardRatingXtn = NbaUtils.getFirstSubstandardExtension(substandardRatingOnCovOpt);
						if (covOptionPersonXtn != null && covOptionPersonXtn.hasRateClass()) {
							if (covOptionCount == 0) {
								deOinkMap.put("A_CovOptionRateClass", String.valueOf(covOptionPersonXtn.getRateClass()));
								temp0++;
							} else {
								deOinkMap.put("A_CovOptionRateClass[" + k + ";" + r + "]", String.valueOf(covOptionPersonXtn.getRateClass()));
							}
						}
						if (ratingOnCovOpt > 0) {
							if (substandardRatingOnCovOpt.hasPermTableRating()) {
								if (covOptionCount == 0) {
									deOinkMap.put("A_CovOptionPermFlatExtraRating", String.valueOf(substandardRatingOnCovOpt.getPermTableRating()));
									temp0++;
								} else {
									deOinkMap.put("A_CovOptionPermFlatExtraRating[" + k + "]", String.valueOf(substandardRatingOnCovOpt
											.getPermTableRating()));
								}
							}
						}
						if (substandardRatingXtn != null && substandardRatingXtn.hasPermFlatExtraAmt()) {
							if (temp4 == 0 && ratingOnCovOpt == 0) {
								deOinkMap.put("A_CovOptionPermFlatExtraAmt", String.valueOf(substandardRatingXtn.getPermFlatExtraAmt()));
								temp4++;
							} else {
								deOinkMap.put("A_CovOptionPermFlatExtraAmt[" + k + ";" + temp4 + "]", String.valueOf(substandardRatingXtn
										.getPermFlatExtraAmt()));
								temp4++;
							}
						}
						if (substandardRatingXtn != null && substandardRatingOnCovOpt.hasTempFlatExtraAmt()) {
							if (temp5 == 0 && ratingOnCovOpt == 0) {
								deOinkMap.put("A_CovOptionTempFlatExtraAmt", String.valueOf(substandardRatingOnCovOpt.getTempFlatExtraAmt()));
								temp5++;
							} else {
								deOinkMap.put("A_CovOptionTempFlatExtraAmt[" + k + ";" + temp5 + "]", String.valueOf(substandardRatingOnCovOpt
										.getTempFlatExtraAmt()));
								temp5++;
							}
							if (n == 0 && ratingOnCovOpt == 0) {
								deOinkMap.put("A_CovOptionTempFlatExtraAmtDuration", String.valueOf(substandardRatingXtn.getDuration()));
								p++;
							} else {
								deOinkMap.put("A_CovOptionTempFlatExtraAmtDuration[" + k + ";" + p + "]", String.valueOf(substandardRatingXtn
										.getDuration()));
								p++;
							}
						}
					}
				}
			} // APSL 2768
			if (k == 0) {
				deOinkMap.put("A_CovOptionNoofPermFlatExtra", String.valueOf(temp4));
				deOinkMap.put("A_CovOptionNoofTempFlatExtra", String.valueOf(temp5));
			} else {
				deOinkMap.put("A_CovOptionNoofPermFlatExtra[" + k + "]", String.valueOf(temp4));
				deOinkMap.put("A_CovOptionNoofTempFlatExtra[" + k + "]", String.valueOf(temp5));
			}
		}
		return;
	}
	//	End AXAL3.7.09

	//NBA224 new method
	public void deOinkImpairmentsInfo(NbaTXLife txLife, Map deOinkMap){
		String processid = txLife.getBusinessProcess();
		if (NbaConstants.PROC_UW_APPROVE_CONTRACT.equalsIgnoreCase(processid) || NbaConstants.PROC_APPROVAL.equalsIgnoreCase(processid)) {
			int totalImpairments = 0;
			String index = null;
			List relations = txLife.getUIRelationList();
			if (relations != null) {
				int count = relations.size();
				Relation relation = null;
				String partyID = null;
				List impList = null;
				for (int i = 0; i < count; i++) {
					relation = (Relation) relations.get(i);
					partyID = relation.getRelatedObjectID();
					impList = txLife.getImpairments(partyID);
					int impCount = impList.size();
					for (int j = 0; j < impCount; j++) {
						ImpairmentInfo impInfo = (ImpairmentInfo) impList.get(j);
						if (totalImpairments == 0) {
							deOinkMap.put(NbaVpmsAdaptor.A_IMPAIRMENT_STATUS, Long.toString(impInfo.getImpairmentStatus()));
							deOinkMap.put(NbaVpmsAdaptor.A_RESTRICT_IMPAIRMENT, Boolean.toString(impInfo.getRestrictApprovalInd()));
						} else {
							index = String.valueOf(totalImpairments);
							deOinkMap.put(NbaUtils.formatVPMSArrayAttribute(NbaVpmsAdaptor.A_IMPAIRMENT_STATUS, index), Long.toString(impInfo.getImpairmentStatus()));
							deOinkMap.put(NbaUtils.formatVPMSArrayAttribute(NbaVpmsAdaptor.A_RESTRICT_IMPAIRMENT, index), Boolean.toString(impInfo.getRestrictApprovalInd()));
						}
						totalImpairments++;
					}
				}
				deOinkMap.put(NbaVpmsAdaptor.A_NUMBER_OF_IMPAIRMENTS, String.valueOf(totalImpairments));
			}
		}
	}
	
	/**
	 * This Method Sets the Args for the OinkRequest
	 * @param NbaOinkRequest oinkRequest
	 * @param NbaTXLife txLife
	 * @param NbaDst work
	 * @param String partyID
	 * @return void
	 */
	//Begin AXAL3.7.09
	void setOinkRequestKeys(NbaOinkRequest oinkRequest, NbaTXLife txLife, NbaDst work, String partyID) {
		Object[] keys = new Object[4];
		keys[0] = partyID; //Parentid key.
		keys[1] = work.getNbaLob().getPolicyNumber(); //contract key
		keys[2] = work.getNbaLob().getCompany(); //company key
		keys[3] = work.getNbaLob().getBackendSystem(); //backend key
		oinkRequest.setArgs(keys);
		Relation relation = NbaUtils.getRelationForParty(partyID, txLife.getOLifE().getRelation().toArray());
		if (null != relation) {
			oinkRequest.setPartyFilter(relation.getRelationRoleCode(), relation.getRelatedRefID());
		}
	} //End AXAL3.7.09
	
	/**
	 * @param txLife
	 * @param deOinkMap
	 */
	//ALPC075 New method
	protected void deOinkEndorsementValues(NbaTXLife txLife, Map deOinkMap) {
		List endorsementList = txLife.getPolicy().getEndorsement();
		List endorsementCodeList = new ArrayList();
		EndorsementExtension endorsementExtension = null;
		for (int i = 0; i < endorsementList.size(); i++) {
			Endorsement endorsement = (Endorsement) endorsementList.get(i);
			if (endorsement.isActionAdd() || endorsement.isActionUpdate()) {
				endorsementExtension = NbaUtils.getFirstEndorseExtension(endorsement);
				if (endorsementExtension != null) {
					endorsementCodeList.add(endorsementExtension.getEndorsementCodeContent());
				} else {
					endorsementCodeList.add("");
				}
			}
		}
		deOinkMap.put("A_EndorsementCodeList", endorsementCodeList.toArray(new String[endorsementCodeList.size()]));
		deOinkMap.put("A_No_of_Endorsementcode", Integer.toString(endorsementCodeList.size()));
	}
	
	/**
	 * Returns the current HttpSession.
	 * @return
	 */
	//New method QC 1973 - ALS3753
	public static HttpSession getSession() {
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext extContext = context.getExternalContext();
		return (HttpSession)extContext.getSession(false);
	}
	
	//ALS3898 new method
	protected void addSystemMessage(String msgdesc, long severity) throws NbaBaseException {
		SystemMessage sysMsg = new SystemMessage();
		sysMsg.setMessageDescription(msgdesc);
		sysMsg.setMessageSeverityCode(severity);
		OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_SYSTEMMESSAGE);
		olifeExt.setActionDisplay();
		sysMsg.addOLifEExtension(olifeExt);
		SystemMessageExtension systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(sysMsg);
		systemMessageExtension.setMsgValidationType(NbaContractValidationConstants.SUBSET_TXNVAL);
		systemMessageExtension.setActionDisplay();
		getTxLife().getPrimaryHolding().addSystemMessage(sysMsg);
	}
	
	//ALS3898 new method
	public NbaDst getNbaDst() {
		return nbaDst;
	}
	//ALS3898 new method
	public void setNbaDst(NbaDst nbaDst) {
		this.nbaDst = nbaDst;
	}
	//ALS3898 new method
	public NbaTXLife getTxLife() {
		return txLife;
	}
	//ALS3898 new method
	public void setTxLife(NbaTXLife txLife) {
		this.txLife = txLife;
	}
	//ALS3898 new method
	public NbaUserVO getUserVO() {
		return userVO;
	}
	//ALS3898 new method
	public void setUserVO(NbaUserVO userVO) {
		this.userVO = userVO;
	}
	
	/**
	 * This method deOinks Coverage Values.
	 * @param NbaTXLife txLife
	 * @param Map deOinkMap
	 * @param Coverage coverage
	 * @param LifeParticipant lifeParticipant
	 * @param int substandardRatingCount
	 * @param int tempCovCount
	 * @param int temp1
	 * @param int temp2
	 * @param int temp3
	 * @param int n
	 * @throws NbaBaseException
	 */
	//Begin ALS5647
	public void deOinkReinsuranceInfo(NbaTXLife txLife, Map deOinkMap, Coverage coverage) {
		boolean reinsuranceAccepted = false;
		deOinkMap.put("A_TentDispApproved", String.valueOf(isTentativeDispositionApproved(txLife.getPolicy().getApplicationInfo())));
		ReinsuranceInfo reinsInfo = null;
		if (coverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_BASE) { //ALS5675
			if (hasFacultativeReinsurance(coverage)) { //ALS5675
				for (int i = 0; i < coverage.getReinsuranceInfoCount(); i++) {
					reinsInfo = coverage.getReinsuranceInfoAt(i);
					if (reinsInfo.getReinsuranceRiskBasis() == NbaOliConstants.OLI_REINRISKBASE_FA) {
						ReinsuranceOffer reInsOffer = null;
						CoverageExtension covExt = NbaUtils.getFirstCoverageExtension(coverage);
						if (covExt != null && covExt.getReinsuranceOfferCount() > 0) {
							for (int k = 0; k < covExt.getReinsuranceOfferCount(); k++) {
								reInsOffer = covExt.getReinsuranceOfferAt(k);
								if (reInsOffer.getAcceptInd()) {
									reinsuranceAccepted = true;
									break;
								}
											}
					}else{
						reinsuranceAccepted = true; // ALS5675
										}
						}
					}
				
			} else { // ALS5675
				reinsuranceAccepted = true; // ALS5675
				
			} // ALS5675
			deOinkMap.put("A_ReinsuranceAcceptInd", String.valueOf(reinsuranceAccepted));
			//NBLXA-1331
		} // ALS5675
	}
	//ALS5675 new method
	private boolean hasFacultativeReinsurance(Coverage coverage) {
		boolean hasFacultative = false;
		ReinsuranceInfo reinsInfo= null;
		for(int i=0;i<coverage.getReinsuranceInfoCount(); i++){
			reinsInfo = coverage.getReinsuranceInfoAt(i);
			if(reinsInfo.getReinsuranceRiskBasis() == NbaOliConstants.OLI_REINRISKBASE_FA) {
				hasFacultative = true;
				break;
			}
		}
		return hasFacultative;
	}
	//New Method ALS5647
	public boolean isTentativeDispositionApproved(ApplicationInfo appInfo) {
		if (appInfo != null) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(txLife.getPolicy().getApplicationInfo());
			if(appInfoExt != null){
				for (int i = 0; i < appInfoExt.getTentativeDispCount(); i++) {
					if (appInfoExt.getTentativeDispAt(i).getDisposition() == NbaOliConstants.NBA_TENTATIVEDISPOSITION_APPROVED) {
						return true;
					}
				}
			}
		}
		return false;
	}	
	/**
	 * @return Returns the nbaProduct.
	 */
	 //P2AXAL016 new method
	public AccelProduct getNbaProduct() {
		return nbaProduct;
	}
	/**
	 * @param nbaProduct The nbaProduct to set.
	 */
 	 //P2AXAL016 new method
	public void setNbaProduct(AccelProduct nbaProduct) {
		this.nbaProduct = nbaProduct;
	}
	
	//P2AXAL062 new method
	public boolean isValidBusinessProcess(String processId) {
		return (processId.equalsIgnoreCase("UWAPPROVECONTRACT") || processId.equalsIgnoreCase("UWDISPOSITION") || processId.equalsIgnoreCase("UWUNAPPROVECONTRACT"));
	}	
	
	
	//P2AXAL062 new method
	public long getTableRatingApplied(LifeParticipant lifeParticipant) {
		for (int k = 0; k < lifeParticipant.getSubstandardRatingCount(); k++) {
			SubstandardRating substandardRating = lifeParticipant.getSubstandardRatingAt(k);
			if (!substandardRating.isActionDelete() && substandardRating.hasPermTableRating()) {
				return substandardRating.getPermTableRating();//There must be only ONE table rating applied an insured as per business
			}
		}
		return 1;//None
	}
	//APSL2903 QC11162 new method
	/**
	 * Deoink indicator for Rateclass on Undenied Lifeparticipants of a coverage
	 * If Rate class is applied on all  Undenied Lifeparticipants indicator is set to false else true.
	 * Based on this indicator transaction validation message will added for approved rate class missing.
	 */
	public void deOinkLifeParticipantRateClass(NbaDst nbaDst, NbaOinkDataAccess oinkData, NbaTXLife txLife,Map deOinkMap, Coverage coverage) throws NbaBaseException 
	{
		boolean ind = false;
		LifeParticipant lifeParticipant = null;
		List lifePartList = new ArrayList();
		String partyID;
		String approvedRateClass;
		if (NbaOliConstants.OLI_POLSTAT_DECISSUE != coverage.getLifeCovStatus()
				&& NbaOliConstants.OLI_COVTYPE_CHILDTERM != coverage.getLifeCovTypeCode()) {  // APSL3467
		lifePartList = NbaUtils.getLifeParticipantList(coverage);
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		for (Iterator iter = lifePartList.iterator(); iter.hasNext();) {
			lifeParticipant = (LifeParticipant) iter.next();
			if (lifeParticipant != null) {
				partyID = lifeParticipant.getPartyID();
				setOinkRequestKeys(oinkRequest, txLife, nbaDst, partyID);
				oinkRequest.setVariable("ApprovedRateClass");
				approvedRateClass = oinkData.getStringValueFor(oinkRequest);
				if (NbaUtils.isEmpty(approvedRateClass)) {
					ind = true;
					break;
				}
			}
		}
		}
		deOinkMap.put("A_LifeParticipantRateClassInd", Boolean.toString(ind));
	}
	//end APSL2903 QC11162

}
