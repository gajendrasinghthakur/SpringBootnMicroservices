package com.csc.fsg.nba.business.process;

/*
 * ************************************************************** <BR>
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
 * ************************************************************** <BR>
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.business.transaction.AxaUnadmittedReplTransaction;
import com.csc.fsg.nba.database.AxaGIAppOnboardingDataAccessor;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaPartyData;
import com.csc.fsg.nba.vo.AxaGIAppOnboardingDataVO;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.Category;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsPartyInquiryRequestData;
import com.csc.fsg.nba.vpms.NbaVpmsUnderwritingRiskData;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

/**
 * NbaProcUnderwritingRisk is the class that processes nbAccelerator cases found on the AWD Underwritig Risk queue (NBUWRSK).
 * <p>
 * The NbaProcUnderwritingRisk class extends the NbaAutomatedProcess class. Although this class may be instantiated by any module, the NBA polling
 * class will be the primary creator of objects of this type.
 * <p>
 * When the polling process finds a case in the Underwritig Risk queue, it will create an object of this instance and call the object's
 * executeProcess(NbaUserVO, NbaDst) method. This method will send XML 204 request to web service to fetch inforce contracts for primary insured for
 * current case in queue and also use partykey to fetch all pending contracts. This contract information will be added as attachment.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>NBA088</td>
 * <td>Version 3</td>
 * <td>Underwriting Risk</td>
 * </tr>
 * <tr>
 * <td>NBA095</td>
 * <td>Version 4</td>
 * <td>Queues Accept Any Work Type</td>
 * </tr>
 * <tr>
 * <td>NBA105</td>
 * <td>Version 4</td>
 * <td>Underwriting Risk</td>
 * </tr>
 * <tr>
 * <td>NBA124</td>
 * <td>Version 5</td>
 * <td>Underwriting Risk Remap</td>
 * </tr>
 * <tr>
 * <td>SPR2743</td>
 * <td>Version 5</td>
 * <td>Pending database party inquiry during underwriting risk processing producing enormous matches</td>
 * </tr>
 * <tr>
 * <td>SPR1753</td>
 * <td>Version 5</td>
 * <td>Automated Underwriting and Requirements Determination Should Detect Severe Errors for Both AC and Non - AC</td>
 * </tr>
 * <tr>
 * <td>SPR3003</td>
 * <td>Version 6</td>
 * <td>Define secondary search criteria in Client Search VP/MS model to be used for pending database searching in Underwriting risk autoprocess.</td>
 * </tr>
 * <tr>
 * <td>SPR2968</td>
 * <td>Version 6</td>
 * <td>Test web service should determine XML file name dynamically</td>
 * </tr>
 * <tr>
 * <td>SPR2737</td>
 * <td>Version 6</td>
 * <td>Error Return Handling Never Defined for Underwriting Risk CyberLife Service (CREF)</td>
 * </tr>
 * <tr>
 * <td>SPR3192</td>
 * <td>Version 6</td>
 * <td>Underwriter Risk Process routes Vantage case to error queue when Vantage is down</td>
 * </tr>
 * <tr>
 * <td>SPR2992</td>
 * <td>Version 6</td>
 * <td>General Code Clean Up Issues for Version 6</td>
 * </tr>
 * <tr>
 * <td>NBA213</td>
 * <td>Version 7</td>
 * <td>Unified User Interface</td>
 * </tr>
 * <tr>
 * <td>SPR3290</td>
 * <td>Version 7</td>
 * <td>General source code clean up during version 7</td>
 * </tr>
 * <tr>
 * <td>AXAL3.7.24</td>
 * <td>AXA Life Phase 1</td>
 * <td>Unadmitted Replacement Interface</td>
 * <tr>
 * <td>AXAL3.7.05</td>
 * <td>AXA Life Phase 1</td>
 * <td>Prior Insurance</td>
 * <tr>
 * <td>AXAL3.7.21</td>
 * <td>AXA Life Phase 1</td>
 * <td>Prior Insurance Interface</td>
 * <tr>
 * <td>AXAL3.7.20R</td>
 * <td>AXA Life Phase 1</td>
 * <td>Replacement Workflow</td>
 * <tr>
 * <td>NBA300</td>
 * <td>AXA Life Phase 2</td>
 * <td>Term Conversion</td>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 * @see NbaAutomatedProcess
 */
public class NbaProcUnderwritingRisk extends NbaAutomatedProcess {
	public static final String PRIMARY_SEARCH = "1"; // NBA124

	public static final String SECONDRY_SEARCH = "2"; // NBA124

	public static final String PENDING_SEARCH = "3"; // ALPC240

	public static final String PARTY_TYPE_PERSON = "1"; // NBA124
	public boolean isCIPAlertExists;
	private AxaGIAppOnboardingDataAccessor onboardingDataAccessor = null; // NBLXA-2299
	private boolean isEntityOwnedGIApplication = false; // NBLXA-2299
	// NBA105 Code deleted.
	/**
	 * This abstract method must be implemented by each subclass in order to execute the automated process.
	 * 
	 * @param user
	 *            the user/process for whom the process is being executed
	 * @param work
	 *            a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		// SPR3290 code deleted

		// NBA095 code deleted
		if (!initialize(user, work)) {
			return getResult();
		}
		// Begin ALS5252
		// If we are bypassing this because the case is approved/disposed, we need to delete the 7000 Need Eval System Message
		if (PROCESS_BYPASS.equals(getAlternateStatus())) {
			updateContract();
			if (null == getResult()) { // success
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			}
			changeStatus(result.getStatus());
			doUpdateWorkItem();
			return getResult();
		}
		// End ALS5252
		// Begin ALS5143
		NbaDst originalTransaction = null;
		boolean isTransaction = work.isTransaction();
		if (isTransaction) {
			originalTransaction = work;
			setWork(retrieveParentWork(work, true, false));
		}
		// End ALS5143
		try { // NBA095
				// Begin AXAL3.7.40G
			AxaPreventProcessVpmsData preventProcessData = new AxaPreventProcessVpmsData(user, getNbaTxLife(), getVpmsModelToExecute());
			if (preventProcessData.isPreventsProcess()) {
				addComment(preventProcessData.getComments());
				// Begin NBLXA-1288
				if (!isTransaction) {
					NbaUtils.addPreventProcessActivity(nbaTxLife, user.getUserID(), NbaOliConstants.OLI_ACTTYPE_PREVENT_PROCESS);
					doContractUpdate(nbaTxLife);
				}
				// End NBLXA-1288
			} else {// End AXAL3.7.40G
				// NBA105 Code Deleted.
				// begin NBA105
				NbaTXLife nbaTXLife = getNbaTxLife();
				doContractUpdate(nbaTXLife); // ALS4802
				calculateUnderwritingRisk(); // NBA124
				// ALS4563 Code Deleted
				// APSL4412 start
				// SR564247(APSL2525) CIP-Begin
				NbaLob aNbaLob = getWorkLobs();
				isCIPAlertExists = false;
				if (!aNbaLob.getApplicationType().trim().equals(Long.toString(NbaOliConstants.OLI_APPTYPE_TRIAL)) ) {// ALII1975 
					if (!nbaTXLife.isADCApplication()) { //NBLXA-2512
						doCIPInquiry(nbaTXLife);
					}
					// NBLXA-2152 Begin
					// Exclude L/N Bridger and BAE calls for Guaranteed Issue cases, Internal Replacements and Term Conversions cases
					boolean cond1 = nbaTXLife.isReplacement() && nbaTXLife.getPolicy().getReplacementType() == NbaOliConstants.OLI_REPTY_INTERNAL; // Internal replacement
					boolean cond2 = nbaTXLife.isTermConversion(); // Term conversion
					// boolean cond3 = NbaUtils.isGIApplication(nbaTXLife); // NBLXA-2299
					boolean cond4 = NbaUtils.isConfigCallEnabled(NbaConfigurationConstants.ENABLE_CLIENT_INTERFACE_CALL);
					boolean isOPAI = nbaTXLife.isOPAI(); // OPAI applications NBLXA-2152
					if (!(cond1 || cond2 || isOPAI) && cond4) { // NBLXA-2299 Begin
						isEntityOwnedGIApplication = NbaUtils.isEntityOwnedGIApplication(getNbaTxLife());
						if (isEntityOwnedGIApplication) { // NBLXA-2299
							onboardingDataAccessor = new AxaGIAppOnboardingDataAccessor();
							onboardingDataAccessor.startTransaction();
						}
						// NBLXA-2299 End
						retrieveBridgerCIData(nbaTXLife);
						if (!NbaUtils.isWebServiceStubbed(nbaTXLife.getBackendSystem(), "CIP", AxaWSConstants.WS_OP_BAE_CLIENT_RISK_SCORE)) { // NBLXA-2152
							searchCustomer(nbaTXLife); // NBLXA-2152
						}
						retrieveBAEClientRiskScore(nbaTXLife); // NBLXA-2152
					}
					if (isCIPAlertExists) {
						// createTransactionForBridgerCIP(nbaTXLife.getPolicy().getPolNumber());
						createMiscWorkForCDD(nbaTXLife.getPolicy().getPolNumber());
					}
					// NBLXA-2152 End
				}
				setRCMTeamConciergeInd(nbaTXLife); // APSL4412
				// SR564247(APSL2525) CIP-End
				// APSL4412 end
				doContractUpdate(nbaTXLife); // NBA124
				if (onboardingDataAccessor != null) {
					onboardingDataAccessor.commitTransaction();
				}
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Final NbaTxLife after Underwriting Risk computation \n " + nbaTXLife.toXmlString()); // SPR2992
				}
			}

			result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus());
			// Begin ALS5143
			if (isTransaction) {
				originalTransaction.setStatus(getPassStatus());
				update(originalTransaction);
			} else {
				changeStatus(getPassStatus());
			}
			// End ALS5143
			doUpdateWorkItem(); // also unlocks the case

			return result;
		} catch (NbaDataException ndae) {
			changeStatus(getVpmsErrorStatus());
			addComment(ndae.getMessage());
			if (onboardingDataAccessor != null) {
				onboardingDataAccessor.rollbackTransaction();
			}
			return result;
		} catch (NbaBaseException e) {
			if (onboardingDataAccessor != null) {
				onboardingDataAccessor.rollbackTransaction();
			}
			throw e;
		}finally {
			setWork(getOrigWorkItem()); //APSL5055-NBA331
		}
		// end NBA105
	}

	/**
	 * 
	 * This method is to set RCM Team and Concierge Ind on a Case.
	 */
	// APSL4412 New Method
	public void setRCMTeamConciergeInd(NbaTXLife nbaTXLife) {
		try {
			// to set RCM Team
			String rcmTeam = NbaUtils.getRCMTeam(NbaUtils.getAsuCodeForRetail(nbaTXLife), NbaUtils.getEPGInd(nbaTXLife));
			PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(nbaTXLife.getPolicy());
			polExt.setTeamUnit(rcmTeam);
			polExt.setActionUpdate();

			// to set Concierge Ind
			String producerid = "";
			Relation bgaRelation = getNbaTxLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_GENAGENT);
			Relation relation = getNbaTxLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_PRIMAGENT);
			NbaParty party = null;
			if (relation != null) {
				party = getNbaTxLife().getParty(relation.getRelatedObjectID());
			} else if (bgaRelation != null) {
				party = getNbaTxLife().getParty(bgaRelation.getRelatedObjectID());
			}
			if (party != null) {
				CarrierAppointment carrierAppointment = party.getParty().getProducer().getCarrierAppointmentAt(0);
				if (carrierAppointment != null) {
					producerid = carrierAppointment.getCompanyProducerID();
				}
			}
			polExt.setConciergeCaseInd(NbaUtils.isEarcAgent(producerid));
			polExt.setActionUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * This method first iterates over all the owner parties and invokes CIP Request for each. It adds the response from the cip service to the
	 * attachment of the party.
	 */
	// APSL4412 - New Method
	protected void doCIPInquiry(NbaTXLife nbaTXLife) throws NbaBaseException { // SR564247(APSL2525) - New Method
		if (nbaTXLife != null) {
			List cipPartyIds = new ArrayList<String>(); // NBLXA-1254
			// nbaTXLife.setAccessIntent(NbaConstants.UPDATE); //APSL2228
			// List ownerParties = nbaTXLife.getAllParties(NbaOliConstants.OLI_REL_OWNER);
			// NBLXA-1352
			List primaryInsured = nbaTXLife.getAllParties(NbaOliConstants.OLI_REL_INSURED);
			List jointInsured = nbaTXLife.getAllParties(NbaOliConstants.OLI_REL_JOINTINSURED);
			// ownerParties = NbaUtils.mergeInsAndOwnPartiesList(ownerParties, primaryInsured, jointInsured);
			// Begin NBLXA-1254
			determineCIPRisk(nbaTXLife, primaryInsured, cipPartyIds);
			determineCIPRisk(nbaTXLife, jointInsured, cipPartyIds);

			// End NBLXA-1254
			// NBLXA-1352
			/*
			 * if (ownerParties != null) { Iterator ownerPartyIterator = ownerParties.iterator();
			 * 
			 * while (ownerPartyIterator.hasNext()) { Party ownerParty = ((NbaParty) ownerPartyIterator.next()).getParty(); //APSL4412 start try {
			 * NbaTXLife txLife = AxaUtils.getTXLifeFromCIPAttachment(ownerParty); long msgSeverity = -1l; if (txLife != null &&
			 * txLife.getTransResult().getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESS) { msgSeverity = AxaUtils.getCIPMessageSeverity(txLife);
			 * } if( (txLife == null) || (txLife.getTransResult().getResultCode() != NbaOliConstants.TC_RESCODE_SUCCESS) || (msgSeverity !=
			 * NbaOliConstants.OLI_MSGSEVERITY_INFO && msgSeverity != NbaOliConstants.OLI_MSGSEVERITY_WARNING && msgSeverity !=
			 * NbaOliConstants.OLI_MSGSEVERITY_SEVERE) ) { //ALII1564,Removed code-ALII1718 //APSL4224 AxaWSInvoker webServiceInvoker =
			 * AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_CIP, getUser(), nbaTXLife, null, ownerParty); NbaTXLife nbaTxLifeRes
			 * = (NbaTXLife) webServiceInvoker.execute(); Attachment attachment = new Attachment(); AttachmentData attachmentData = new
			 * AttachmentData(); List attachmentList = AxaUtils.getAttachmentsByType(ownerParty, NbaOliConstants.OLI_ATTACH_1009800001); if
			 * (attachmentList.size() > 0) { attachment = (Attachment) attachmentList.get(0); attachmentData = attachment.getAttachmentData();
			 * attachmentData.setActionUpdate(); attachment.setActionUpdate(); } else { attachment = new Attachment(); attachmentData = new
			 * AttachmentData(); attachmentData.setActionAdd(); attachment.setActionAdd(); } //APSL4224 code deleted
			 * attachment.setAttachmentType(NbaOliConstants.OLI_ATTACH_1009800001); attachmentData.setPCDATA(nbaTxLifeRes.toXmlString());
			 * attachment.setDateCreated(new Date()); attachment.setAttachmentData(attachmentData); attachment.setUserCode(getUser().getUserID());
			 * ownerParty.addAttachment(attachment); ownerParty.setActionUpdate(); createTransactionForCIP(nbaTxLifeRes); } } catch (Exception e) {
			 * setWork(getOrigWorkItem()); //APSL4224 throw new NbaBaseException("Error invoking CIP WebService", e); } //APSL4412 end } }
			 * //NbaContractAccess.doContractUpdate(getNbaTxLife(), getWork(), getUser());//APSL2228 ;
			 */
		}
	}

	// Begin NBLXA-2152
	// NBLXA-2378 Method Updated 
	protected void retrieveBridgerCIData(NbaTXLife nbaTXLife) throws NbaBaseException { // SR564247(APSL2525) - New Method
		if (nbaTXLife != null) {
			List masterNbaPartyList = new ArrayList<NbaParty>(); 
			masterNbaPartyList.addAll(nbaTXLife.getAllParties(NbaOliConstants.OLI_REL_OWNER));
			masterNbaPartyList.addAll(nbaTXLife.getAllParties(NbaOliConstants.OLI_REL_TRUSTEE));
			masterNbaPartyList.addAll(nbaTXLife.getAllParties(NbaOliConstants.OLI_REL_AUTHORIZEDPERSON));
			masterNbaPartyList.addAll(nbaTXLife.getAllParties(NbaOliConstants.OLI_REL_BENEFICIALOWNER));
			masterNbaPartyList.addAll(nbaTXLife.getAllParties(NbaOliConstants.OLI_REL_CONTROLLINGPERSON));
			List<Party> bridgerParties =  new ArrayList(); 
			try {
				bridgerParties = getBridgerParties(masterNbaPartyList);
				determineBridgerCDDRisk(masterNbaPartyList, bridgerParties);
			}  catch (NbaBaseException be) {
				setWork(getOrigWorkItem());
				addComment("Error invoking LexisNexis Bridger WebService");
				throw be;
			}
			catch (Exception e) {
				setWork(getOrigWorkItem());
				addComment("Error invoking LexisNexis Bridger WebService");
				e.printStackTrace();
				throw new NbaBaseException("Error invoking LexisNexis Bridger WebService", e);
			
			}
			// NBLXA-1352
			/*
			 * if (ownerParties != null) { Iterator ownerPartyIterator = ownerParties.iterator();
			 * 
			 * while (ownerPartyIterator.hasNext()) { Party ownerParty = ((NbaParty) ownerPartyIterator.next()).getParty(); //APSL4412 start try {
			 * NbaTXLife txLife = AxaUtils.getTXLifeFromCIPAttachment(ownerParty); long msgSeverity = -1l; if (txLife != null &&
			 * txLife.getTransResult().getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESS) { msgSeverity = AxaUtils.getCIPMessageSeverity(txLife);
			 * } if( (txLife == null) || (txLife.getTransResult().getResultCode() != NbaOliConstants.TC_RESCODE_SUCCESS) || (msgSeverity !=
			 * NbaOliConstants.OLI_MSGSEVERITY_INFO && msgSeverity != NbaOliConstants.OLI_MSGSEVERITY_WARNING && msgSeverity !=
			 * NbaOliConstants.OLI_MSGSEVERITY_SEVERE) ) { //ALII1564,Removed code-ALII1718 //APSL4224 AxaWSInvoker webServiceInvoker =
			 * AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_CIP, getUser(), nbaTXLife, null, ownerParty); NbaTXLife nbaTxLifeRes
			 * = (NbaTXLife) webServiceInvoker.execute(); Attachment attachment = new Attachment(); AttachmentData attachmentData = new
			 * AttachmentData(); List attachmentList = AxaUtils.getAttachmentsByType(ownerParty, NbaOliConstants.OLI_ATTACH_1009800001); if
			 * (attachmentList.size() > 0) { attachment = (Attachment) attachmentList.get(0); attachmentData = attachment.getAttachmentData();
			 * attachmentData.setActionUpdate(); attachment.setActionUpdate(); } else { attachment = new Attachment(); attachmentData = new
			 * AttachmentData(); attachmentData.setActionAdd(); attachment.setActionAdd(); } //APSL4224 code deleted
			 * attachment.setAttachmentType(NbaOliConstants.OLI_ATTACH_1009800001); attachmentData.setPCDATA(nbaTxLifeRes.toXmlString());
			 * attachment.setDateCreated(new Date()); attachment.setAttachmentData(attachmentData); attachment.setUserCode(getUser().getUserID());
			 * ownerParty.addAttachment(attachment); ownerParty.setActionUpdate(); createTransactionForCIP(nbaTxLifeRes); } } catch (Exception e) {
			 * setWork(getOrigWorkItem()); //APSL4224 throw new NbaBaseException("Error invoking CIP WebService", e); } //APSL4412 end } }
			 * //NbaContractAccess.doContractUpdate(getNbaTxLife(), getWork(), getUser());//APSL2228 ;
			 */
		}
	}

	// End NBLXA-2152

	// New Method : NBLXA-1254
	private void determineCIPRisk(NbaTXLife nbaTXLife, List cipParties, List cipPartyIds) throws NbaBaseException {
		if (cipParties != null) {
			Iterator cipPartyIterator = cipParties.iterator();

			while (cipPartyIterator.hasNext()) {
				Party cipParty = ((NbaParty) cipPartyIterator.next()).getParty();
				// NBLXA-1254 Start
				if (!cipPartyIds.contains(cipParty.getId()) && !nbaTXLife.isOwner(cipParty.getId())) {//NBLXA-2152 #1
					cipPartyIds.add(cipParty.getId());
					// NBLXA-1254 End
					// APSL4412 start
					try {
						NbaTXLife txLife = AxaUtils.getTXLifeFromCIPAttachment(cipParty);
						long msgSeverity = -1l;
						if (txLife != null && txLife.getTransResult().getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESS) {
							msgSeverity = AxaUtils.getCIPMessageSeverity(txLife);
						}
						if ((txLife == null)
								|| (txLife.getTransResult().getResultCode() != NbaOliConstants.TC_RESCODE_SUCCESS)
								|| (msgSeverity != NbaOliConstants.OLI_MSGSEVERITY_INFO && msgSeverity != NbaOliConstants.OLI_MSGSEVERITY_WARNING && msgSeverity != NbaOliConstants.OLI_MSGSEVERITY_SEVERE)) {
							// ALII1564,Removed code-ALII1718
							// APSL4224
							AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_CIP, getUser(),
									nbaTXLife, null, cipParty);
							NbaTXLife nbaTxLifeRes = (NbaTXLife) webServiceInvoker.execute();
							saveResponseAsAtachment(nbaTxLifeRes,cipParty,NbaOliConstants.OLI_ATTACH_1009800001); //NBLXA-2152 #3
							/*Attachment attachment = new Attachment();
							AttachmentData attachmentData = new AttachmentData();
							List attachmentList = AxaUtils.getAttachmentsByType(cipParty, NbaOliConstants.OLI_ATTACH_1009800001);
							if (attachmentList.size() > 0) {
								attachment = (Attachment) attachmentList.get(0);
								attachmentData = attachment.getAttachmentData();
								attachmentData.setActionUpdate();
								attachment.setActionUpdate();
							} else {
								attachment = new Attachment();
								attachmentData = new AttachmentData();
								attachmentData.setActionAdd();
								attachment.setActionAdd();
							}
							// APSL4224 code deleted
							attachment.setAttachmentType(NbaOliConstants.OLI_ATTACH_1009800001);
							attachmentData.setPCDATA(nbaTxLifeRes.toXmlString());
							attachment.setDateCreated(new Date());
							attachment.setAttachmentData(attachmentData);
							attachment.setUserCode(getUser().getUserID());
							cipParty.addAttachment(attachment);
							cipParty.setActionUpdate(); */
							createTransactionForCIP(nbaTxLifeRes);
						}
					} catch (Exception e) {
						setWork(getOrigWorkItem()); // APSL4224
						throw new NbaBaseException("Error invoking CIP WebService", e);
					}
					// APSL4412 end
				}
			}
		}
	}

	// Begin NBLXA-2152, NBLXA-2299 Change return type
	private boolean determineBridgerCIPRisk(Party bridgerParty) throws NbaBaseException {
		boolean isBridgerAlertExists = false; // NBLXA-2299
		NbaTXLife nbaTXLife = getNbaTxLife();
		String role = (nbaTXLife.isOwner(bridgerParty.getId()) ? "Owner" : NbaUtils.getTranslatedRole(bridgerParty, nbaTXLife));
		try {
			NbaTXLife txLife = AxaUtils.getTXLifeFromCIPAttachmenttype(bridgerParty, NbaOliConstants.OLI_ATTACH_BRIDGERCIP_1009800015);
			if ((txLife == null) || (txLife != null && txLife.getTransResult() != null
					&& txLife.getTransResult().getResultCode() == NbaOliConstants.TC_RESCODE_FAILURE)) {
				AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(
						AxaWSConstants.WS_OP_LEXIS_NEXIS_BRIDGER_RETRIEVE_CIDATA, getUser(), nbaTXLife, null, bridgerParty);
				NbaTXLife nbaTxLifeRes = (NbaTXLife) webServiceInvoker.execute();
				saveResponseAsAtachment(nbaTxLifeRes, bridgerParty, NbaOliConstants.OLI_ATTACH_BRIDGERCIP_1009800015);
				if (nbaTxLifeRes != null
						&& (NbaOliConstants.TC_RESCODE_FAILURE == nbaTxLifeRes.getTransResult().getResultCode()
								|| AxaUtils.getBridgerResultcode(nbaTxLifeRes) == NbaOliConstants.OLI_FINALRESULTCODE_YELLOW)
						|| (AxaUtils.getBridgerResultcode(nbaTxLifeRes) == NbaOliConstants.OLI_FINALRESULTCODE_RED)) {
					isCIPAlertExists = true;
					isBridgerAlertExists = true; // NBLXA-2299
				}
				if (nbaTxLifeRes.getTransResult().getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESS
						|| nbaTxLifeRes.getTransResult().getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESSINFO) {
					addComment("Client Data Onboarding call to LexisNexis is successful. Role: " + role + " " + bridgerParty.getFullName());
				} else {
					addComment("Client Data Onboarding call to LexisNexis failed. Role: " + role + " " + bridgerParty.getFullName());
				}
			}
		} catch (NbaBaseException be) {
			setWork(getOrigWorkItem());
			addComment("Client Data Onboarding call to LexisNexis failed. Role: " + role + " " + bridgerParty.getFullName());
			throw be;
		} catch (Exception e) {
			setWork(getOrigWorkItem());
			addComment("Client Data Onboarding call to LexisNexis failed." + role + " " + bridgerParty.getFullName());
			throw new NbaBaseException("Error invoking LexisNexis Bridger WebService", e);
		}
		return isBridgerAlertExists; // NBLXA-2299
	}



	// End NBLXA-2152
	// NBLXA-2152 New Method
	protected void searchCustomer(NbaTXLife nbaTXLife) throws NbaBaseException {
		try {
			AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_SEARCH_CUSTOMER, getUser(),
					nbaTXLife, null, null);
			NbaTXLife nbaTxLifeRes = (NbaTXLife) webServiceInvoker.execute();
			if (!NbaUtils.isBlankOrNull(nbaTxLifeRes) && !NbaUtils.isBlankOrNull(nbaTxLifeRes.getTransResult())
					&& NbaOliConstants.TC_RESCODE_SUCCESS == nbaTxLifeRes.getTransResult().getResultCode()) {
				// NBLXA-2359 Code refactored to fix the MDM party id matching issue Begin 
				List ownerParties = nbaTXLife.getAllParties(NbaOliConstants.OLI_REL_OWNER);
				Party ownerParty = null;
				List mdmParties = nbaTxLifeRes.getAllParties(NbaOliConstants.OLI_REL_OWNER);
				Party matchingMDMParty = null;
				if (ownerParties != null && mdmParties != null) {
					if (ownerParties.size() == 1 && mdmParties.size() == 1) { // If single Owner and single MDM party, then set MDM party.
						matchingMDMParty = ((NbaParty)mdmParties.get(0)).getParty();
						ownerParty = ((NbaParty)ownerParties.get(0)).getParty();
						PartyExtension ownerPartyExtension = NbaUtils.getFirstPartyExtension(ownerParty);
						ownerPartyExtension.setMDMPartyId(matchingMDMParty.getPartySysKeyAt(0).getPCDATA());
						ownerPartyExtension.setActionUpdate();
					}else {
						Iterator ownerPartyIterator = ownerParties.iterator();
						while (ownerPartyIterator.hasNext()) {
							ownerParty = ((NbaParty) ownerPartyIterator.next()).getParty();
							matchingMDMParty = NbaUtils.getMatchingMDMParty(ownerParty, mdmParties);
							if(matchingMDMParty == null  || NbaUtils.isBlankOrNull(matchingMDMParty.getPartySysKeyAt(0).getPCDATA())) {
								addComment("nbA Party not found for MDMParty from searchCustomer Interface Response.");
								throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH_WS, AxaWSConstants.WS_OP_SEARCH_CUSTOMER + ": "+ "MDMParty ID is null or blank");
							}
							PartyExtension ownerPartyExtension = NbaUtils.getFirstPartyExtension(ownerParty);
							ownerPartyExtension.setMDMPartyId(matchingMDMParty.getPartySysKeyAt(0).getPCDATA());
							ownerPartyExtension.setActionUpdate();						
						}
				}
			}
			}else {
					addComment("nbA Party not found for MDMParty from searchCustomer Interface Response.");
					throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH_WS, AxaWSConstants.WS_OP_SEARCH_CUSTOMER + ": "+ "MDMParty ID is null or blank");
			}
			// NBLXA-2359 Code refactored to fix the MDM party id matching issue Begin
		}catch (AxaErrorStatusException nbe) {
			setWork(getOrigWorkItem());
			throw nbe;
		}catch (NbaBaseException nbe) {
			setWork(getOrigWorkItem());
			addComment("Error invoking searchCustomer WebService");
			throw nbe;
		} catch (Exception e) {
			setWork(getOrigWorkItem());
			throw new NbaBaseException("Error invoking searchCustomer WebService", e);
		}
	}

	// NBLXA-2152 New Method
	private void retrieveBAEClientRiskScore(NbaTXLife nbaTXLife) throws NbaBaseException {
		List ownerParties = nbaTXLife.getAllParties(NbaOliConstants.OLI_REL_OWNER);
		if (ownerParties != null) {
			Iterator ownerPartyIterator = ownerParties.iterator();

			while (ownerPartyIterator.hasNext()) {
				Party ownerParty = ((NbaParty) ownerPartyIterator.next()).getParty();
				// NBLXA-2299 Begin
				if (isEntityOwnedGIApplication) {
					retrieveBAEClientRiskScoreForEntityOwnedGIApplicationParty(ownerParty);
				} else { 
					retrieveBAEClientRiskScoreForParty(ownerParty);					
				}
				// NBLXA-2299 End
			}
		}
	}

	// NBLXA-2299 New Method
	private boolean retrieveBAEClientRiskScoreForParty(Party ownerParty) throws NbaBaseException {
		boolean isBAEAlertExists = false;
				try {
					NbaTXLife txLife = AxaUtils.getTXLifeFromCIPAttachmenttype(ownerParty, NbaOliConstants.OLI_ATTACH_BAE);
					if ((txLife == null) || (NbaOliConstants.TC_RESCODE_FAILURE == txLife.getTransResult().getResultCode())) {
						AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_BAE_CLIENT_RISK_SCORE,
						getUser(), getNbaTxLife(), null, ownerParty);
						NbaTXLife nbaTxLifeRes = (NbaTXLife) webServiceInvoker.execute();
						saveResponseAsAtachment(nbaTxLifeRes, ownerParty, NbaOliConstants.OLI_ATTACH_BAE);
						if (NbaOliConstants.TC_RESCODE_FAILURE == nbaTxLifeRes.getTransResult().getResultCode() || NbaOliConstants.OLI_FINALRESULTCODE_RED == AxaUtils.getBridgerResultcode(nbaTxLifeRes)) {
							isCIPAlertExists = true;
					isBAEAlertExists = true;
						}
						if (nbaTxLifeRes.getTransResult().getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESS ||nbaTxLifeRes.getTransResult().getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESSINFO) {
							addComment("Client Data Onboarding call to BAE is successful. Role: Owner " + ownerParty.getFullName());
						} else {
							addComment("Client Data Onboarding call to BAE failed. Role: Owner " + ownerParty.getFullName());
						}
					}
				} catch (NbaBaseException nbe) {
					setWork(getOrigWorkItem());
					addComment("Client Data Onboarding call to BAE failed. Role: Owner " + ownerParty.getFullName());
					throw nbe;
				} catch (Exception e) {
					setWork(getOrigWorkItem());
					throw new NbaBaseException("Error invoking retrieveClientRiskScore BAE WebService", e);
				}
		return isBAEAlertExists;
	}

	/**
	 * This method calculates risk on all the parties for a given contract. It considers all the insurable parties on a given contract. For each party
	 * an accord 204 party inquiry transaction is generated and is sent to host. and a list of holdings are returned by the host which are covering
	 * that party. Then nbapending database is querird using the same criteria. All the holding thus returned are used to calculate the risk for that
	 * party. The holdings are updated in Party.Attachment objects and risk are updated in Party.Risk object.
	 * 
	 * @throws NbaDataException
	 *             throws when VPMS is not configured to have sufficient search criteria
	 * @throws NbaBaseException
	 *             throws when there is an error in risk calculation and could not be handled
	 */
	// NBA124 New Method
	protected void calculateUnderwritingRisk() throws NbaBaseException {
		NbaDst work = getWork();
		NbaLob workLob = work.getNbaLob();
		NbaTXLife nbaTxLife = getNbaTxLife();
		NbaUserVO userVO = getUser();
		NbaOLifEId nbaOLifEId = null;
		nbaOLifEId = new NbaOLifEId(nbaTxLife.getOLifE());
		Map deOink = null;
		// invoke VPMS to get the search criterion for Tx204
		Category aNbaConfigCategory = NbaConfiguration.getInstance().getIntegrationCategory(workLob.getBackendSystem(),
				NbaConfigurationConstants.UNDERWRITINGRISK);
		String configCategory = aNbaConfigCategory.getValue();

		Party orgParty = null;
		// P2AXAL004 code deleted
		// Get primary search keys from VPMS
		NbaVpmsPartyInquiryRequestData primaryPartyInqReqData = NbaUnderwritingRiskHelper.getDataFromVpmsModelClientSearch(nbaTxLife, work,
				PARTY_TYPE_PERSON, PRIMARY_SEARCH, configCategory);
		NbaVpmsPartyInquiryRequestData secondryPartyInqReqData = null;
		// Get secondry search keys from VPMS
		secondryPartyInqReqData = NbaUnderwritingRiskHelper.getDataFromVpmsModelClientSearch(nbaTxLife, work, PARTY_TYPE_PERSON, SECONDRY_SEARCH,
				configCategory);// SPR3003
		// SPR3003 code deleted / P2AXAL004 code deleted
		// If last name is not setup as a criteria in VPMS then throw an exception to route work to error queue
		if (!primaryPartyInqReqData.getLastName() || !secondryPartyInqReqData.getLastName()) {
			throw new NbaDataException("Insufficient inquiry criteria to perform search");
		}
		// ALS3084 Code deleted
		List insurableParties = nbaTxLife.getInsurablePartiesForPrimHolding();// APSL2785,QC#10468 new method created to get list of all insurable
																				// parties for primary holding
		int noOfParties = insurableParties.size();

		// Perform risk calculation for each insurable party
		Map partyMap = null;
		List attachmentList = null;
		for (int i = 0; i < noOfParties; i++) {
			orgParty = (Party) insurableParties.get(i);
			deOink = new HashMap();
			partyMap = new HashMap();
			partyMap.put(NbaConstants.ORG_PARTY, orgParty);
			partyMap.put(NbaConstants.PARTY_INQDATA, primaryPartyInqReqData);
			// SPR3003 code deleted
			// Begin AXAL3.7.05
			attachmentList = doPriorInsuranceInquiry(partyMap, nbaOLifEId);
			// end AXAL3.7.05
			if ((attachmentList == null || attachmentList.size() == 0)) { // AXAL3.7.05, P2AXAL004 Code deleted
				// Call web service with 2nd search criteria and create attachments
				// Begin AXAL3.7.05
				partyMap.put(NbaConstants.PARTY_INQDATA, secondryPartyInqReqData);
				attachmentList = doPriorInsuranceInquiry(partyMap, nbaOLifEId);
				// end AXAL3.7.05
			}

			// ALII1174 code deleted
			createAttachmentFromPending(orgParty, attachmentList, nbaOLifEId); // SPR2743//SPR3003
			NbaUnderwritingRiskHelper.addAttachmentToParty(orgParty, attachmentList, NbaOliConstants.OLI_ATTACH_PRIORINS); // AXAL3.7.05

			// call TAI to calculate reinsurance information
			if (!nbaTxLife.isSIApplication()) { // APSL2808
				List priorReinsAttachmentList = doPriorReInsuranceInquiry(partyMap, nbaOLifEId); // AXAL3.7.05
				NbaUnderwritingRiskHelper.addAttachmentToParty(orgParty, priorReinsAttachmentList, NbaOliConstants.OLI_ATTACH_PRIORREINS);
			}

			// Add the risk information to the party. Prepare the deoink variables and call the underwriting risk model.
			doRiskCalculations(orgParty, attachmentList); // ALS3084
			// end SPR1753
		}
		// APSL2815 QC#10695
		if (!NbaUtils.isAdcApplication(work) && !nbaTxLife.isSIApplication()) { // APSL2808
			// Begin ALS4563
			AxaUnadmittedReplTransaction unadReplTrans = new AxaUnadmittedReplTransaction();// AXAL3.7.24
			// Begin AXAL3.7.20R
			boolean unadmittedReplInd = unadReplTrans.checkCaseForUnadmittedReplacement(getNbaTxLife(), work, getUser());
			if (isReplacementRequired(unadmittedReplInd)) {
				createReplacementTransaction();
			}
		}
		// End AXAL3.7.20R
		// End ALS4563
	}

	/**
	 * Queries pending contract database to get the contracts covering the incoming party object and performs a holding inquiry for each of them. All
	 * holdings are then filtered out and added to requesting party as attachment. Contract keys which are already found in host will be ignored while
	 * querying the database.
	 * 
	 * @param orgParty
	 *            Party object for which database is to be queried
	 * @param attachmentList
	 *            List of attachments with holding and party object
	 * @param nbaOlifeId
	 *            NbaOLifEId used to set id of attachments generated.
	 * @throws NbaBaseException
	 *             if not able to process pending contract query
	 */
	// NBA124 New Method
	protected void createAttachmentFromPending(Party orgParty, List attachmentList, NbaOLifEId nbaOlifeId) throws NbaBaseException {
		NbaDst work = getWork();
		NbaUserVO userVO = getUser();
		NbaTXLife attachmentNbaTXLife = null;
		List partyPendingContracts;

		Attachment currentAttachment = null;
		NbaTXLife nbaTxLife = null;
		List excludePolicies = new ArrayList(); // ALS4644
		ListIterator attachIterator = attachmentList.listIterator();
		// Extract policy number of each of the policy returned in attachments.
		// Prepares comma separated list of policy numbers,
		// this list will be excluded while querying pending for the party information
		while (attachIterator.hasNext()) {
			currentAttachment = (Attachment) attachIterator.next();
			try {
				nbaTxLife = new NbaTXLife(currentAttachment.getAttachmentData().getPCDATA());
			} catch (Exception e) {
				// since attachments were recently created from NbaTXLife objects, parsing them back will not throw an exception
				getLogger().logException(e);
			}
			// ALS4644 Code deleted
			excludePolicies.add(nbaTxLife.getOLifE().getHoldingAt(0).getPolicy().getPolNumber()); // ALS4644
			// ALS4644 Code deleted
		}
		// ALS4644 Code deleted

		partyPendingContracts = NbaContractAccess.doPartyInquiry(orgParty, excludePolicies); // NBA213 ALS4644
		ListIterator partyIterator = partyPendingContracts.listIterator();
		NbaPartyData partyData = null;
		while (partyIterator.hasNext()) {
			partyData = (NbaPartyData) partyIterator.next();
			String contractKey = partyData.getContractKey();// QC4373 APSL178
			String companyCode = partyData.getCompanyKey();// QC4373 APSL178
			String backendKey = partyData.getBackendKey();// P2AXAL013
			NbaTXLife nbaTXLifeDatabase = NbaUnderwritingRiskHelper.doHoldingInquiry(contractKey, companyCode, backendKey, work, userVO);// QC4373
																																			// APSL178
																																			// P2AXAL013
			// Begin APSL3121,QC#11757 Filter the TxLife to contain only party specific information
			if (nbaTXLifeDatabase != null && nbaTXLifeDatabase.getOLifE() != null) {
				if (isAttachmentFromPendingAdded(nbaTXLifeDatabase, partyData)) {
					nbaTXLifeDatabase.setBusinessProcess(NbaConstants.PROC_UNDERWRITING_RISK);
					attachmentNbaTXLife = NbaUnderwritingRiskHelper.prepareNbaTXLifeForAttachment(nbaTXLifeDatabase, orgParty, null, work); // ALII1869
					if (attachmentNbaTXLife != null) {
						((TXLifeRequest) (attachmentNbaTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequest().get(0)))
								.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ); // AXAL3.7.05
						NbaUnderwritingRiskHelper.addAttachment(attachmentList, attachmentNbaTXLife, nbaOlifeId, NbaOliConstants.OLI_ATTACH_PRIORINS);
					}
				}
			}// End APSL3121,QC#11757
		}
	}

	/**
	 * Handle Tx204 webservice success response. Extract out all the contracts returned from the webservice as a result of party inquiry. Create an
	 * attachment of for each of such contracts and then return the list of the attachments created.
	 * 
	 * @param nbaTXLifeResponse
	 *            webservice response
	 * @param nbaOLifEId
	 *            olife ID generator
	 * @param serviceType
	 *            TransType for which attachments are to be created
	 * @return a List of attachments containing matched contracts for the party.
	 */
	// NBA124 New Method
	// AXAL3.7.05 changed method signature
	protected List getAttachmentList(NbaTXLife nbaTXLifeResponse, NbaOLifEId nbaOLifEId, long serviceType) {
		OLifE responseOlife = nbaTXLifeResponse.getOLifE();
		// begin AXAL3.7.05
		List attachmentListHost = new ArrayList();
		if (responseOlife == null) {
			return attachmentListHost;
		}
		// end AXAL3.7.05
		List holdingList = responseOlife.getHolding();
		ListIterator holdingIterator = holdingList.listIterator();
		Holding holding = null;
		NbaTXLife attachmentContract = null;
		// AXAL3.7.05 code deleted
		while (holdingIterator.hasNext()) {
			NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
			nbaTXRequest.setBusinessProcess(NbaConstants.PROC_UNDERWRITING_RISK);
			attachmentContract = new NbaTXLife(nbaTXRequest);
			OLifE attachmentOlife = attachmentContract.getOLifE();
			holding = (Holding) holdingIterator.next();
			String holdingID = holding.getId();
			// add holding
			attachmentOlife.addHolding(holding);
			// AXAL3.7.05 code deleted
			// add relation
			List relationList = responseOlife.getRelation();
			ListIterator relationIterator = relationList.listIterator();
			Map partyMap = new HashMap(); // AXAL3.7.05
			NbaOLifEId olifeId = new NbaOLifEId(attachmentOlife);
			while (relationIterator.hasNext()) {
				Relation relation = (Relation) relationIterator.next();
				if (relation.hasOriginatingObjectID() && relation.getOriginatingObjectID().equalsIgnoreCase(holdingID)) {
					attachmentOlife.addRelation(relation);
					// begin AXAL3.7.05
					NbaParty nbaParty = nbaTXLifeResponse.getParty(relation.getRelatedObjectID());
					if (nbaParty != null) {
						if (!partyMap.containsKey(relation.getRelatedObjectID())) {
							partyMap.put(relation.getRelatedObjectID(), nbaParty.getParty());
							// begin AXAL3.7.21
							// add LifeParticipant objects for 204
							if (serviceType == NbaOliConstants.TC_TYPE_PARTYINQ) {
								List coverages = holding.getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife()
										.getCoverage();
								for (int i = 0; i < coverages.size(); i++) {
									Coverage coverage = (Coverage) coverages.get(i);
									NbaUnderwritingRiskHelper.addCoverageLifeParticipant(olifeId, relation.getRelatedObjectID(), coverage,
											relation.getRelationRoleCode());
								}
							}
							// end AXAL3.7.21
						}
					}
					// end AXAL3.7.05
				}
			}
			// begin AXAL3.7.05
			// add parties and Hit/Try relation
			Iterator partyIterator = partyMap.values().iterator();
			while (partyIterator.hasNext()) {
				Party party = (Party) (partyIterator.next());
				attachmentOlife.addParty(party);
				// Hit/Try is the only relation having OriginatingObjectId = Party.Id
				List relations = nbaTXLifeResponse.getRelationsByOriginatingId(party.getId());
				if (relations.size() > 0) {
					attachmentOlife.addRelation((Relation) (relations.get(0)));
				}
			}
			((TXLifeRequest) (attachmentContract.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequest().get(0)))
					.setTransType(serviceType);
			long attachmentType = (serviceType == NbaOliConstants.TC_TYPE_PARTYINQ) ? NbaOliConstants.OLI_ATTACH_PRIORINS
					: NbaOliConstants.OLI_ATTACH_PRIORREINS;
			NbaUnderwritingRiskHelper.addAttachment(attachmentListHost, attachmentContract, nbaOLifEId, attachmentType);
			// end AXAL3.7.05
		}
		return attachmentListHost;
	}

	// AXAL3.7.05 new method
	protected List doPriorInsuranceInquiry(Map partyMap, NbaOLifEId olife) throws NbaBaseException {
		AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_RET_PRIOR_INSURANCE, getUser(),
				getNbaTxLife(), null, partyMap);
		NbaTXLife nbaTxLifeRes = (NbaTXLife) webServiceInvoker.execute();
		return getAttachmentList(nbaTxLifeRes, olife, NbaOliConstants.TC_TYPE_PARTYINQ);
	}

	// AXAL3.7.05 new method
	protected List doPriorReInsuranceInquiry(Map partyMap, NbaOLifEId olife) throws NbaBaseException {
		Party orgParty = (Party) partyMap.get(NbaConstants.ORG_PARTY);
		AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_TAI_SERVICE_RETRIEVE, getUser(),
				getNbaTxLife(), null, orgParty);
		NbaTXLife tx302Response = (NbaTXLife) webServiceInvoker.execute();
		return getAttachmentList(tx302Response, olife, NbaOliConstants.TC_TYPE_HOLDINGSRCH);
	}

	/**
	 * This method creates a replacement work item.
	 * 
	 * @throws NbaBaseException
	 */
	// AXAL3.7.20R New Method
	protected void createReplacementTransaction() throws NbaBaseException {
		// Invoke VP/MS to determine work type and status
		NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork(), getNbaTxLife(), new HashMap());
		NbaTransaction aTransaction = getWork().addTransaction(provider.getWorkType(), provider.getInitialStatus());
		aTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
		// Initialize LOBs of the WI
		aTransaction.getNbaLob().setAppOriginType(getWork().getNbaLob().getAppOriginType());
		aTransaction.getNbaLob().setAppState(getWork().getNbaLob().getAppState());
		aTransaction.getNbaLob().setReplacementIndicator(getWork().getNbaLob().getReplacementIndicator());
		aTransaction.getNbaLob().setRouteReason(UNADMITTED_REPL);// NBLXA-1554(NBLXA-2058)
	}

	/**
	 * This method creates a work item for CIP CM.
	 * 
	 * @throws NbaBaseException
	 */
	// APSL4412 New Method
	protected void createTransactionForCIP(NbaTXLife nbaTxLifeRes) throws NbaBaseException {
		if (NbaUtils.isRetail(getNbaTxLife().getPolicy())
				&& nbaTxLifeRes != null
				&& ((nbaTxLifeRes.getTransResult().getResultCode() == NbaOliConstants.TC_RESCODE_FAILURE)
						|| (nbaTxLifeRes.getTransResult().getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESS  && AxaUtils.getCIPMessageSeverity(nbaTxLifeRes) == NbaOliConstants.OLI_OTHER) || (nbaTxLifeRes
						.getTransResult().getResultCode() == NbaOliConstants.TC_RESCODE_SUCCESS && AxaUtils.getCIPMessageSeverity(nbaTxLifeRes) == NbaOliConstants.OLI_MSGSEVERITY_SEVERE))) {
			isCIPAlertExists = true; // NBLXA-2152 #2

		}
	}

	/**
	 * This method determines if a replacement WI should be generated or not.
	 * 
	 * @return
	 * @throws NbaBaseException
	 */
	// AXAL3.7.20R New Method
	protected boolean isReplacementRequired(boolean unadmittedReplInd) throws NbaBaseException {
		if (!unadmittedReplInd) {
			return false;
		}
		List transactions = getWork().getNbaTransactions();
		int size = transactions.size();
		for (int i = 0; i < size; i++) {
			NbaTransaction transaction = (NbaTransaction) transactions.get(i); // ALS5630
			if (transaction.getWorkType().equals(NbaConstants.A_WT_REPLACEMENT)) {
				for (int j = 0; j < NbaConstants.REPLACEMENT_OPEN_STATUSES.length; j++) {
					if (NbaConstants.REPLACEMENT_OPEN_STATUSES[j].equalsIgnoreCase(transaction.getStatus())) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private void updateContract() {
		if (removeErrorMessage()) {
			try {
				nbaTxLife = doContractUpdate();
				handleHostResponse(nbaTxLife);
			} catch (NbaBaseException nbe) {
				getLogger().logError(nbe.getMessage());
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getHostErrorStatus()));
			}
		}
	}

	// ALS3084 New Method
	protected void doRiskCalculations(Party party, List attachmentList) throws NbaBaseException {
		String modelName = getVpmsModelToExecute();
		// Add the risk information to the party. Prepare the deoink variables and call the underwriting risk model.
		Map deOink = new HashMap();
		deOink.put("A_POLICYSTATUS", String.valueOf(nbaTxLife.getPolicy().getPolicyStatus()));
		deOink.put("A_SIGNEDDATE", NbaUtils.getStringFromDate(nbaTxLife.getPolicy().getApplicationInfo().getSignedDate()));
		// if (NbaUtils.isGIApplication(nbaTxLife)) { // NBLXA-1467 NBLXA-188 Changes
		// NbaUnderwritingRiskHelper.prepareDeOinkMapForGIApplication(attachmentList, deOink, nbaTxLife.getPolicy().getContractKey());
		// } else {
		NbaUnderwritingRiskHelper.prepareDeOinkMap(attachmentList, deOink, nbaTxLife.getPolicy().getContractKey());
		// }
		deOinkTermConvData(deOink); // NBA300
		NbaVpmsUnderwritingRiskData data = NbaUnderwritingRiskHelper.getDataFromVpmsModelAutoUnderwriting(nbaTxLife, deOink, work, getUser(),
				modelName);
		if (NbaUnderwritingRiskHelper.hasTotals(data)) {
			NbaUnderwritingRiskHelper.addRiskToParty(party, data);
		} else {
			addComment("Underwriting Risk not calculated because severe errors present");
		}
	}

	/**
	 * This method Check, is Prior Insurance attachment from pending need to be added in prior insurance list. It will added policies only when
	 * primary insured is having insurable relation and related to primary holding in his/her previous policies.
	 * 
	 * @return boolean
	 * @param NbaTXLife
	 *            ,NbaPartyData
	 */
	// APSL3121,QC#11757 new method
	protected boolean isAttachmentFromPendingAdded(NbaTXLife nbaTXLifeDatabase, NbaPartyData partyData) {
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(nbaTXLifeDatabase.getOLifE());
		if (holding != null && partyData != null) {
			List relationList = nbaTXLifeDatabase.getOLifE().getRelation();
			for (int i = 0; i < relationList.size(); i++) {
				Relation relation = (Relation) relationList.get(i);
				if (relation != null && NbaUtils.isInsuredRelation(relation)) {
					if (relation.getRelatedObjectID().equalsIgnoreCase(partyData.getPartyId())
							&& relation.getOriginatingObjectID().equalsIgnoreCase(holding.getId())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * This method creates a work item for CIP CM.
	 * 
	 * @throws NbaBaseException
	 */
	// NBLXA-2152 Start
	protected void createMiscWorkForCDD(String polNumber) throws NbaBaseException {
		NbaSearchResultVO searchResultVO = searchMiscWorkItem(polNumber,getAggReference());
		if(searchResultVO == null) {//No existing MiscWI, create new WI
			createMiscWorkItem();
		}else if (isInEndQueue(searchResultVO)) {//If the existing MiscWI is in End queue, route it.
			// route
			HashMap deoinkMap = new HashMap();
			deoinkMap.put("A_CIPWork", "true");
			deoinkMap.put(NbaVpmsConstants.A_RCMTEAM,
			NbaUtils.getRCMTeam(NbaUtils.getAsuCodeForRetail(getNbaTxLife()), NbaUtils.getEPGInd(getNbaTxLife()))); // APSL4412
			// Invoke VP/MS to determine work type and status
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork(), getNbaTxLife(), deoinkMap);
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(searchResultVO.getWorkItemID(), true);
			retOpt.setLockTransaction();
			NbaDst dst = retrieveWorkItem(getUser(), retOpt);
			dst.setStatus(provider.getInitialStatus());
			dst.getNbaLob().setRouteReason(NbaUtils.getRouteReason(dst, dst.getStatus()));
			updateWork(getUser(), dst);
		}
	}

	// NBLXA-2152
	public NbaSearchResultVO searchMiscWorkItem(String polNumber, String aggRef) throws NbaBaseException {
		List searchResultList = null;
		NbaSearchResultVO searchResultVO = null;
		NbaSearchVO searchMiscWorkVO = new NbaSearchVO();
		searchMiscWorkVO.setWorkType(NbaConstants.A_WT_MISC_WORK);
		searchMiscWorkVO.setContractNumber(polNumber);
		searchMiscWorkVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchMiscWorkVO = WorkflowServiceHelper.lookupWork(getUser(), searchMiscWorkVO);
		if (!NbaUtils.isBlankOrNull(searchMiscWorkVO) && searchMiscWorkVO.getSearchResults() != null
				&& !searchMiscWorkVO.getSearchResults().isEmpty()) {
			searchResultList = searchMiscWorkVO.getSearchResults();
		}
		if (searchResultList != null && !searchResultList.isEmpty()) {
			Iterator iter = searchResultList.iterator();
			while (iter.hasNext()) {
				searchResultVO = (NbaSearchResultVO) iter.next();
				NbaLob nbaLob = searchResultVO.getNbaLob();
				if (nbaLob.getAggrReference() != null && nbaLob.getAggrReference().equalsIgnoreCase(aggRef)) {
					return searchResultVO;
				}
			}
		}
		return null;
	}

	// NBLXA-2152
	public static boolean isStatusPresentInMiscwork(List miscWorkItemsList) {
		boolean flag = false;
		Iterator iter = miscWorkItemsList.iterator();
		while (iter.hasNext()) {
			NbaSearchResultVO searchResultVO = (NbaSearchResultVO) iter.next();
			for (int j = 0; j < NbaConstants.miscWorkStatus.length; j++) {
				if (searchResultVO.getStatus().equals(NbaConstants.miscWorkStatus[j])) {
					flag = true;
					return flag;
				}
			}
		}
		return flag;
	}

	// NBLXA-2152
	public void createMiscWorkItem() throws NbaBaseException {
		try {
					HashMap deoinkMap = new HashMap();
					deoinkMap.put("A_CIPWork", "true");
					deoinkMap.put(NbaVpmsConstants.A_RCMTEAM,
							NbaUtils.getRCMTeam(NbaUtils.getAsuCodeForRetail(getNbaTxLife()), NbaUtils.getEPGInd(getNbaTxLife()))); // APSL4412
					// Invoke VP/MS to determine work type and status
					NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork(), getNbaTxLife(), deoinkMap);
					if (provider.getWorkType() != null && provider.getInitialStatus() != null) {
						NbaTransaction aTransaction = getWork().addTransaction(provider.getWorkType(), provider.getInitialStatus());
						aTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
						// Initialize LOBs of the WI
						aTransaction.getNbaLob().setAppOriginType(getWork().getNbaLob().getAppOriginType());
						aTransaction.getNbaLob().setAppState(getWork().getNbaLob().getAppState());
						aTransaction.getNbaLob().setCaseManagerQueue(getWork().getNbaLob().getCaseManagerQueue());
				aTransaction.getNbaLob().setAggrReference(getAggReference()); 
				
				}
		} catch (Exception e) {
			setWork(getOrigWorkItem());
			throw new NbaBaseException("Error invoking bridgerRetrieveCIData WebService", e);
		}

	}


	// NBLXA-2152 end
		protected boolean isInEndQueue(NbaSearchResultVO searchResultVO){
			if(searchResultVO != null) {
				//TBD Write the logic to check if the WI is in END queue. If yes, return true
				//For now, utilized the same status which was already hard coded. It should come from Status Queue Check VPMS model.
				if (searchResultVO.getStatus().equalsIgnoreCase(AWD_WI_STATUS_REVCMPLTD)) {
					return true;
				}
			}
			return false;
		}

		protected String getAggReference() {
			return NbaConstants.CDD_MISC_ALERT;
		}
		
		protected void saveResponseAsAtachment(NbaTXLife aNbaTXLifeResponse, Party party, long attachmentType) {
			Attachment attachment = new Attachment();
			if(aNbaTXLifeResponse != null && party != null) {
				attachment = new Attachment();
				AttachmentData attachmentData = new AttachmentData();
				List attachmentList = AxaUtils.getAttachmentsByType(party, attachmentType);
				if (attachmentList.size() > 0) {
					attachment = (Attachment) attachmentList.get(0);
					attachmentData = attachment.getAttachmentData();
					attachmentData.setActionUpdate();
					attachment.setActionUpdate();
				} else {
					attachment = new Attachment();
					attachmentData = new AttachmentData();
					attachmentData.setActionAdd();
					attachment.setActionAdd();
				}
				attachment.setAttachmentType(attachmentType);
				attachmentData.setPCDATA(aNbaTXLifeResponse.toXmlString());
				attachment.setDateCreated(new Date());
				attachment.setAttachmentData(attachmentData);
				attachment.setUserCode(getUser().getUserID());
				party.addAttachment(attachment);
				party.setActionUpdate();
			}
		}
		
		//NBLXA-2378 New Method 
	private List<Party> getBridgerParties(List<NbaParty> cddParties) throws Exception {
		List<Party> bridgerParties = new ArrayList(); 
		if (cddParties != null) {
			Iterator<NbaParty> cddPartyIterator = cddParties.iterator();
			while (cddPartyIterator.hasNext()) {
				Party party = ( cddPartyIterator.next()).getParty();
				Party txParty = getNbaTxLife().getParty(party.getId()).getParty();
				NbaTXLife txLife = AxaUtils.getTXLifeFromCIPAttachmenttype(txParty, NbaOliConstants.OLI_ATTACH_BRIDGERCIP_1009800015);// NBLXA-2152
				if ((txLife == null)
						|| (txLife != null && txLife.getTransResult() != null && txLife.getTransResult().getResultCode() == NbaOliConstants.TC_RESCODE_FAILURE)) {
				bridgerParties.add(party);
				}
			}
		}
		return bridgerParties;
	}

	

	//NBLXA-2378 New Method 
	private void determineBridgerCDDRisk(List<NbaParty> masterNbaPartyList, List<Party> bridgerParties) throws Exception {
		if (bridgerParties != null) {
			Iterator bridgerPartiesIterator = bridgerParties.iterator();
			while (bridgerPartiesIterator.hasNext()) {
				Party bridgerParty = (Party) bridgerPartiesIterator.next();
				NbaTXLife nbaTxLifeRes =getAttachment(masterNbaPartyList,bridgerParty);
				if (!NbaUtils.isBlankOrNull(nbaTxLifeRes)) {
					saveResponseAsAtachment(nbaTxLifeRes, bridgerParty, NbaOliConstants.OLI_ATTACH_BRIDGERCIP_1009800015);
				} else {
					// NBLXA-2299 Begin
					if (isEntityOwnedGIApplication) {
						determineBridgerCIPRiskForEntityOwnedGIApplication(bridgerParty);
					} else { // NBLXA-2299 End
					determineBridgerCIPRisk(bridgerParty);
				}
		}
			}
		
	}
	}
	//NBLXA-2378 New Method 
	private NbaTXLife getAttachment(List<NbaParty> masterNbaPartyList, Party bridgerParty) throws Exception {

		if (masterNbaPartyList != null) {
			Iterator masterPartiesIterator = masterNbaPartyList.iterator();
			while (masterPartiesIterator.hasNext()) {
				Party partyFromMasterList = ((NbaParty) masterPartiesIterator.next()).getParty();
				if (partyFromMasterList.getPersonOrOrganization().isPerson() && bridgerParty.getPersonOrOrganization().isPerson()) {
					if (!NbaUtils.isBlankOrNull(bridgerParty.getGovtID()) && !NbaUtils.isBlankOrNull(partyFromMasterList.getGovtID())) {
						if (bridgerParty.getGovtID().equalsIgnoreCase(partyFromMasterList.getGovtID())) {
							Party txPartyFromMasterList = getNbaTxLife().getParty(partyFromMasterList.getId()).getParty();
							NbaTXLife txResponse = AxaUtils.getTXLifeFromCIPAttachmenttype(txPartyFromMasterList,
									NbaOliConstants.OLI_ATTACH_BRIDGERCIP_1009800015);
							if (txResponse != null && txResponse.getTransResult() != null
									&& (NbaOliConstants.TC_RESCODE_SUCCESS == txResponse.getTransResult().getResultCode()
											|| NbaOliConstants.TC_RESCODE_SUCCESSINFO == txResponse.getTransResult().getResultCode())) {
								return txResponse;
							}

						}
					} else {
						Person person = bridgerParty.getPersonOrOrganization().getPerson();
						Person personFromMasterList = partyFromMasterList.getPersonOrOrganization().getPerson();
						if (!NbaUtils.isBlankOrNull(person.getFirstName()) && !NbaUtils.isBlankOrNull(personFromMasterList.getFirstName())
								&& person.getFirstName().equalsIgnoreCase(personFromMasterList.getFirstName())
								&& !NbaUtils.isBlankOrNull(person.getLastName()) && !NbaUtils.isBlankOrNull(personFromMasterList.getLastName())
								&& person.getLastName().equalsIgnoreCase(personFromMasterList.getLastName())) {
							if (!NbaUtils.isBlankOrNull(person.getBirthDate()) && !NbaUtils.isBlankOrNull(personFromMasterList.getBirthDate())
									&& !person.getBirthDate().equals(personFromMasterList.getBirthDate())) {
								return null;
							}
							Party txPartyFromMasterList = getNbaTxLife().getParty(partyFromMasterList.getId()).getParty();
							NbaTXLife txResponse = AxaUtils.getTXLifeFromCIPAttachmenttype(txPartyFromMasterList,
									NbaOliConstants.OLI_ATTACH_BRIDGERCIP_1009800015);
							if (txResponse != null && txResponse.getTransResult() != null
									&& (NbaOliConstants.TC_RESCODE_SUCCESS == txResponse.getTransResult().getResultCode()
											|| NbaOliConstants.TC_RESCODE_SUCCESSINFO == txResponse.getTransResult().getResultCode())) {
								return txResponse;
							}

						}
					}
				} else if (partyFromMasterList.getPersonOrOrganization().isOrganization()
						&& bridgerParty.getPersonOrOrganization().isOrganization()) {
					if (!NbaUtils.isBlankOrNull(bridgerParty.getGovtID()) && !NbaUtils.isBlankOrNull(partyFromMasterList.getGovtID())) {
						if (bridgerParty.getGovtID().equalsIgnoreCase(partyFromMasterList.getGovtID())) {
							Party txPartyFromMasterList = getNbaTxLife().getParty(partyFromMasterList.getId()).getParty();
							NbaTXLife txResponse = AxaUtils.getTXLifeFromCIPAttachmenttype(txPartyFromMasterList,
									NbaOliConstants.OLI_ATTACH_BRIDGERCIP_1009800015);
							if (txResponse != null && txResponse.getTransResult() != null
									&& (NbaOliConstants.TC_RESCODE_SUCCESS == txResponse.getTransResult().getResultCode()
											|| NbaOliConstants.TC_RESCODE_SUCCESSINFO == txResponse.getTransResult().getResultCode())) {
								return txResponse;
							}
						}
					} else {
						Organization organization = bridgerParty.getPersonOrOrganization().getOrganization();
						Organization organizationFromMasterList = partyFromMasterList.getPersonOrOrganization().getOrganization();
						if (!NbaUtils.isBlankOrNull(organization.getDBA()) && !NbaUtils.isBlankOrNull(organizationFromMasterList.getDBA())
								&& organization.getDBA().equalsIgnoreCase(organizationFromMasterList.getDBA())) {
							Party txPartyFromMasterList = getNbaTxLife().getParty(partyFromMasterList.getId()).getParty();
							NbaTXLife txResponse = AxaUtils.getTXLifeFromCIPAttachmenttype(txPartyFromMasterList,
									NbaOliConstants.OLI_ATTACH_BRIDGERCIP_1009800015);
							if (txResponse != null && txResponse.getTransResult() != null
									&& (NbaOliConstants.TC_RESCODE_SUCCESS == txResponse.getTransResult().getResultCode()
											|| NbaOliConstants.TC_RESCODE_SUCCESSINFO == txResponse.getTransResult().getResultCode())) {
								return txResponse;
							}
						}
					}
				}
			}
		}
		return null;

	}
	
	// NBLXA-2299 New method
	private AxaGIAppOnboardingDataVO getOnboardingDataVO(Party bridgerParty, boolean isBridgerCallRequired, boolean isBaeCallRequired, boolean isBriderAlertExists, boolean isBaeAlertExists, String offerNumber) {
		AxaGIAppOnboardingDataVO onboardingDataVO = new AxaGIAppOnboardingDataVO();
		onboardingDataVO.setOfferNumber(offerNumber.trim());
		onboardingDataVO.setContractKey(getNbaTxLife().getPolicy().getPolNumber());
		if(!NbaUtils.isBlankOrNull(bridgerParty.getFullName())){
			onboardingDataVO.setName(bridgerParty.getFullName());
		}else{
			onboardingDataVO.setName(NbaUtils.getFullName(bridgerParty));
		}
		onboardingDataVO.setGovtId(bridgerParty.getGovtID());
		onboardingDataVO.setBridgerCallRequired(isBridgerCallRequired);
		onboardingDataVO.setBaeCallRequired(isBaeCallRequired);
		onboardingDataVO.setBridgerAlert(isBriderAlertExists);				
		onboardingDataVO.setBaeAlert(isBaeAlertExists);
		if (isBriderAlertExists) {
			onboardingDataVO.setHoldForBridgerAlert(true);			
		} 
		if (isBaeAlertExists) {
			onboardingDataVO.setHoldForBAEAlert(true);
		}
		return onboardingDataVO;
	}
	
	// NBLXA-2299 New method
	protected void determineBridgerCIPRiskForEntityOwnedGIApplication(Party bridgerParty) throws NbaBaseException {
		PolicyExtension polExtn = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
		if (polExtn != null) {
			boolean isPreviousCallWithFailureResponse = isPreviousOnboardingCallWithFailureResponse(bridgerParty, NbaOliConstants.OLI_ATTACH_BRIDGERCIP_1009800015);
			AxaGIAppOnboardingDataVO onboardingDataVO = onboardingDataAccessor.selectPartyForOnboardingProcessing(polExtn.getGuarIssOfferNumber(),bridgerParty.getFullName(),bridgerParty.getGovtID());
			if (onboardingDataVO == null || onboardingDataVO.isBridgerCallRequired() || isPreviousCallWithFailureResponse) {
				boolean isBriderAlertExists = determineBridgerCIPRisk(bridgerParty); // call L/N bridger webservice 
				if (onboardingDataVO == null) {
					onboardingDataVO = getOnboardingDataVO(bridgerParty, false, true, isBriderAlertExists, false, polExtn.getGuarIssOfferNumber());									
					onboardingDataAccessor.insert(onboardingDataVO);
				} else {
					onboardingDataVO.setBridgerCallRequired(false);
					onboardingDataVO.setBridgerAlert(isBriderAlertExists);
					if (isBriderAlertExists) {
						onboardingDataVO.setHoldForBridgerAlert(true);						
					}
					onboardingDataAccessor.updateBridgerDetails(onboardingDataVO);
				}
			}
		}
	}
	
	// NBLXA-2299 New method
	protected void retrieveBAEClientRiskScoreForEntityOwnedGIApplicationParty(Party baeParty) throws NbaBaseException {
		PolicyExtension polExtn = NbaUtils.getFirstPolicyExtension(getNbaTxLife().getPolicy());
		if (polExtn != null) {
			boolean isPreviousCallWithFailureResponse = isPreviousOnboardingCallWithFailureResponse(baeParty, NbaOliConstants.OLI_ATTACH_BAE);
			AxaGIAppOnboardingDataVO onboardingDataVO = onboardingDataAccessor.selectPartyForOnboardingProcessing(polExtn.getGuarIssOfferNumber(),baeParty.getFullName(),baeParty.getGovtID());
			if (onboardingDataVO == null || onboardingDataVO.isBaeCallRequired() || isPreviousCallWithFailureResponse) {
				boolean isBAEAlertExists = retrieveBAEClientRiskScoreForParty(baeParty);
				if (onboardingDataVO == null) {
					onboardingDataVO = getOnboardingDataVO(baeParty, true, false, false, isBAEAlertExists, polExtn.getGuarIssOfferNumber());									
					onboardingDataAccessor.insert(onboardingDataVO);
				} else {
					onboardingDataVO.setBaeCallRequired(false);
					onboardingDataVO.setBaeAlert(isBAEAlertExists);
					if (isBAEAlertExists) {
						onboardingDataVO.setHoldForBAEAlert(true);						
					}
					onboardingDataAccessor.updateBAEDetails(onboardingDataVO);
				}
			}
		}
	}
	// NBLXA-2299 New method 
	protected boolean isPreviousOnboardingCallWithFailureResponse(Party party, long attachmentType) throws NbaBaseException {
		try {
			NbaTXLife txLife = AxaUtils.getTXLifeFromCIPAttachmenttype(party, attachmentType);
			if (txLife != null && NbaOliConstants.TC_RESCODE_FAILURE == txLife.getTransResult().getResultCode()) {
				return true;
			}
		} catch (Exception e) {
			throw new NbaBaseException("Error getting CIP attachment data", e);
		}
		return false;
	}
}
