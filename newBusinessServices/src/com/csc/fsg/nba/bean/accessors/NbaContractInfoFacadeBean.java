package com.csc.fsg.nba.bean.accessors;
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
import java.util.Date;
import java.util.ResourceBundle;

import javax.ejb.SessionBean;

import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaTransactionValidationException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaContractInfoVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.HoldingExtension;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeUSA;
import com.csc.fsg.nba.vo.txlife.LifeUSAExtension;
import com.csc.fsg.nba.vo.txlife.MailCodes;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;

/**
 * @NbaContractInfoFacadeBean
 * <p>
 * <b>Description:</b>
 * <br>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>NBA052</td><td>Version 3</td><td>Initial Development</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Nba Pending Contract Database</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>SPR1629</td><td>Version 4</td><td>Transaction Validation</td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * <tr><td>SPR1966</td><td>Version 4</td><td>Add Mail Code fields required by Vantage</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR2834</td><td>Version 6</td><td>Lock on the case gets released when making a change on Contract Info BF</td></tr>
 * <tr><td>SPR3012</td><td>Version 6</td><td>Life Specific Mail Codes Need to be Added to the Contract Information View</td></tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>General Code Clean Up Issues for Version 6</td></tr>
 * <tr><td>ALS4875</td><td>AXA Life Phase 1</td><td>Contract Validation Message appearing on the Contract Messages tab for unknown reason</td></tr>
 * <tr><td>NBA298</td><td>AXA Life Phase 2</td><td>MEC Processing</td></tr>
 * </table>
 *
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
/**
 * Bean implementation class for Enterprise Bean: NbaContractInfoFacade
 */
public class NbaContractInfoFacadeBean implements SessionBean, NbaOliConstants {
	public final static String CONTRACT_INFO = "CONTRACTINFO"; //ALS4875
	private static ResourceBundle props = ResourceBundle.getBundle("properties.nbaApplicationData");//NBA298
	private javax.ejb.SessionContext mySessionCtx;
	protected static NbaLogger logger = null;	
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
	public void ejbCreate() throws javax.ejb.CreateException {
	}
	/**
	 * ejbActivate
	 */
	public void ejbActivate() {
	}
	/**
	 * ejbPassivate
	 */
	public void ejbPassivate() {
	}
	/**
	 * ejbRemove
	 */
	public void ejbRemove() {
	}
	/**
	* Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	* @return com.csc.fsg.nba.foundation.NbaLogger
	*/
	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaContractInfoFacadeBean.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaContractInfoFacadeBean could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	
	/**
	 * Saves contract information. 
	 * @param user An instance of <code>NbaUserVO</code>
	 * @param aNbaDst An instance of <code>NbaDst</code>
	 * @param aNbaTXLife An instance of <code>NbaTXLife</code>
	 * @param aNbaContractInfoVO An instance of <code>NbaContractInfoVO</code>
	 * @throws NbaBaseException
	 */
	//NBA103
	public void saveContractInfo(NbaUserVO user, NbaDst aNbaDst, NbaTXLife aNbaTXLife, NbaContractInfoVO aNbaContractInfoVO)
		throws NbaBaseException {
		//SPR1851 code deleted
		updateNbaTXLife(aNbaTXLife, aNbaContractInfoVO);
		
		try {//NBA103
			if(aNbaContractInfoVO.getRequestedIssueDate() != aNbaDst.getNbaLob().getIssueDate()) {
				updateLOB(user, aNbaDst, aNbaContractInfoVO.getRequestedIssueDate());
			}
			aNbaTXLife.setBusinessProcess(CONTRACT_INFO); //ALS4875
			aNbaTXLife = NbaContractAccess.doContractUpdate(aNbaTXLife, aNbaDst, user); //SPR1851
			//SPR1851 code deleted
		} catch (NbaTransactionValidationException e) {//NBA103
			throw e;//NBA103
		} catch (NbaBaseException e) {//NBA103
			getLogger().logError(e);//NBA103
			throw e;//NBA103
		} catch (Throwable t) {//NBA103
			NbaBaseException e = new NbaBaseException(t); //NBA103
			getLogger().logError(e);//NBA103
			throw e;//NBA103
		}
	}
	
	/**
	 * Update the NbaTXLife object with the values in NbaContractInfoVO 
	 * @param aNbaTXLife An instance of <code>NbaTXLife</code>
	 * @param contractInfoVO An instance of <code>NbaContractInfoVO</code>
	 * @throws NbaBaseException
	 */
	private void updateNbaTXLife(NbaTXLife aNbaTXLife, NbaContractInfoVO contractInfoVO) throws NbaBaseException {
		//begin NBA093
		Holding holding = aNbaTXLife.getPrimaryHolding();
		if (holding == null) {
			throw new NbaBaseException("Missing or invalid Holding");
		}
		Policy policy = holding.getPolicy();
		if (policy == null) {
			throw new NbaBaseException("Missing or invalid Policy");
		}
		ApplicationInfo appInfo = policy.getApplicationInfo();
		if (appInfo == null) {
			throw new NbaBaseException("Missing or invalid ApplicationInfo");
		}
		//end NBA093
		//begin NBA077
		long appType = -1;
		if(appInfo.hasApplicationType()){
			appType = appInfo.getApplicationType();
		}
		//end NBA077
		Party payorParty = null;
		String payerRelationRoleCode = aNbaTXLife.getPartyId(NbaOliConstants.OLI_REL_PAYER);
		if (payerRelationRoleCode != null) {
			payorParty = aNbaTXLife.getParty(payerRelationRoleCode).getParty();	
		}	

		//Begin NBA298
		if (aNbaTXLife.getNbaHolding().isLife()) {
			LifeUSA lifeUSA = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife().getLifeUSA(); //NBA093
			LifeUSAExtension lifeUSAExtn = NbaUtils.getFirstLifeUSAExtension(lifeUSA);
			if (lifeUSAExtn != null) {
				if (lifeUSAExtn.getMECReason() != contractInfoVO.getMecReason()) {
					lifeUSAExtn.setMECReason(contractInfoVO.getMecReason());
					lifeUSAExtn.setActionUpdate();
				}
			}
		}
		//End NBA298
			
		//Set Application details
		if (appInfo.getSignedDate() != contractInfoVO.getSignedDate()) {
			appInfo.setSignedDate(contractInfoVO.getSignedDate());
			appInfo.setActionUpdate();
		}
		if(appInfo.getApplicationJurisdiction() != contractInfoVO.getApplicationJurisdiction()) {
			appInfo.setApplicationJurisdiction(contractInfoVO.getApplicationJurisdiction());
			appInfo.setActionUpdate();
		}
		if(appInfo.getHOAssignedAppNumber() != contractInfoVO.getHOAssignedAppNumber()) {
			appInfo.setHOAssignedAppNumber(contractInfoVO.getHOAssignedAppNumber());
			appInfo.setActionUpdate();
		}
		if(policy.getJurisdiction() != contractInfoVO.getIssueState()) {
			policy.setJurisdiction(contractInfoVO.getIssueState());
			policy.setActionUpdate();
		}

		//Set Residence details	
		if(payorParty != null) {
			if(payorParty.getResidenceCounty() != contractInfoVO.getResidenceCounty()) {
				payorParty.setResidenceCounty(contractInfoVO.getResidenceCounty());
				payorParty.setActionUpdate();
			}
			if(payorParty.getResidenceState() != contractInfoVO.getResidenceState()) {		
				payorParty.setResidenceState(contractInfoVO.getResidenceState());	
				payorParty.setActionUpdate();
			}
			if(payorParty.getResidenceCountry() != contractInfoVO.getResidenceCountry()) {				
				payorParty.setResidenceCountry(contractInfoVO.getResidenceCountry());	
				payorParty.setActionUpdate();
			}
		}	
		
		//Set Options details
		//NBA093 deleted 10 lines
		if (!((appType == NbaOliConstants.OLI_APPTYPE_CHANGE) && (aNbaTXLife.isFixedPremium()))) { //NBA077
			if (appInfo.getRequestedPolDate() != contractInfoVO.getRequestedIssueDate()) { //NBA093
				appInfo.setRequestedPolDate(contractInfoVO.getRequestedIssueDate()); //NBA093
				appInfo.setActionUpdate(); //NBA093
			}
		} //NBA077
		
		if(policy.getReplacementType() != contractInfoVO.getReplacementType()) {  //NBA093
			policy.setReplacementType(contractInfoVO.getReplacementType());  //NBA093
			policy.setActionUpdate();  //NBA093
		}
		if(holding.getAssignmentCode() != contractInfoVO.getAssignmentCode()) {  //NBA093
			holding.setAssignmentCode(contractInfoVO.getAssignmentCode());  //NBA093
			holding.setActionUpdate();	//NBA093
		}					
		// NBA093 deleted 4 lines
		if(holding.getRestrictionCode() != contractInfoVO.getRestrictionCode()) {  //NBA093
			holding.setRestrictionCode(contractInfoVO.getRestrictionCode());  //NBA093
			holding.setActionUpdate();	//NBA093
		}			
		//if(applicationInfoExtn.getPensionCode() != contractInfoVO.getPensionCode()) {
			//applicationInfoExtn.setPensionCode(contractInfoVO.getPensionCode());
			//applicationInfoExtn.setActionUpdate();	
		//}

		//Set Policy details
		if (!(appType == NbaOliConstants.OLI_APPTYPE_CHANGE)) { //NBA077 //ALII1206
			if (appInfo.getApplicationType() != contractInfoVO.getApplicationType()) { //NBA093
				appInfo.setApplicationType(contractInfoVO.getApplicationType()); //NBA093
				appInfo.setActionUpdate(); //NBA093
			}
		} //NBA077
		PolicyExtension policyExtn = null;
		if (policy.getOLifEExtensionCount() > 0 ){	
			policyExtn = NbaUtils.getFirstPolicyExtension(policy);		
		}else{
			OLifEExtension oLiFExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
			oLiFExtension.setActionAdd();
			policy.addOLifEExtension(oLiFExtension);
			policyExtn =  oLiFExtension.getPolicyExtension();		
		}
		if(policyExtn.getChangeInProgressInd() != contractInfoVO.getChangeInProgressInd()) {		
			policyExtn.setChangeInProgressInd(contractInfoVO.getChangeInProgressInd());
			policyExtn.setActionUpdate();
		}
		if(policyExtn.getNotTakenInd() != contractInfoVO.getNotTakenInd()) {			
			policyExtn.setNotTakenInd(contractInfoVO.getNotTakenInd());	
			policyExtn.setActionUpdate();			
		}
		if(holding.getLastAnniversaryDate() != contractInfoVO.getLastAnnivDate()) {  //NBA093
			holding.setLastAnniversaryDate(contractInfoVO.getLastAnnivDate());  //NBA093
			holding.setActionUpdate();  //NBA093			
		}
		//NBA093 deleted 4 lines
		//begin NBA093
		if(holding.getQualifiedCode() != contractInfoVO.getPensionCode()) {
			holding.setQualifiedCode(contractInfoVO.getPensionCode());
			holding.setActionUpdate();	
		}
		//end NBA093	
		if (!(aNbaTXLife.isReissue() || appType == NbaOliConstants.OLI_APPTYPE_CHANGE)) { //NBA077 //ALII1206
			if (policyExtn.getContractChangeType() != contractInfoVO.getChangeType()) {
				policyExtn.setContractChangeType(contractInfoVO.getChangeType());
				policyExtn.setActionUpdate();
			}
		}
		if (!(aNbaTXLife.isReissue() || appType == NbaOliConstants.OLI_APPTYPE_CHANGE)) { //NBA077 //ALII1206
			if (policyExtn.getPendingPremiumPayingStatus() != contractInfoVO.getPendingPremiumPayingStatus()) {
				policyExtn.setPendingPremiumPayingStatus(contractInfoVO.getPendingPremiumPayingStatus());
				policyExtn.setActionUpdate();
			}
		} //NBA077
		if(policyExtn.getPremiumPastDueInd() != contractInfoVO.getPremiumPastDue()) {
			policyExtn.setPremiumPastDueInd(contractInfoVO.getPremiumPastDue());
			policyExtn.setActionUpdate();
		}
		if(policy.getGracePeriodEndDate() != contractInfoVO.getGracePeriodEndDate()) {  //NBA093
			policy.setGracePeriodEndDate(contractInfoVO.getGracePeriodEndDate());  //NBA093
			policy.setActionUpdate();  //NBA093
		}
		if(policyExtn.getGracePeriodExtInd() != contractInfoVO.getGracePeriodExtInd()) {		
			policyExtn.setGracePeriodExtInd(contractInfoVO.getGracePeriodExtInd());
			policyExtn.setActionUpdate();					
		}
		

		//begin NBA093
		if(holding.getLastFinActivityDate() != contractInfoVO.getLastFinancial()) {
			holding.setLastFinActivityDate(contractInfoVO.getLastFinancial());
			holding.setActionUpdate();			
		}	
		//end NBA093				
		
		HoldingExtension holdingExtension = NbaUtils.getFirstHoldingExtension(holding);  //NBA093
		//NBA093 deleted line
		if(holdingExtension != null) {  //NBA093
			//NBA093 deleted 5 lines
			//begin NBA093
			if(holdingExtension.getLastAccountingDate() != contractInfoVO.getLastAccountingDate()) {		
				holdingExtension.setLastAccountingDate(contractInfoVO.getLastAccountingDate());	
				holdingExtension.setActionUpdate();					
			}
			//end NBA093
		} else {
			if (contractInfoVO.getLastFinancial() != null || contractInfoVO.getPensionCode() != 0 || contractInfoVO.getLastAccountingDate() != null) {  //NBA093
				OLifEExtension oLiFExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_HOLDING);
				//NBA093 deleted line
				oLiFExtension.setActionAdd();
				holding.addOLifEExtension(oLiFExtension);
				//begin NBA093
				holdingExtension = oLiFExtension.getHoldingExtension();
				// NBA093 deleted 3 lines
				if (contractInfoVO.getLastAccountingDate() != null) {
					holdingExtension.setLastAccountingDate(contractInfoVO.getLastAccountingDate());	
				}
				//end NBA093
			}	
		}
		if(aNbaTXLife.getNbaHolding().isLife()) {				
			if(((Life)policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty$Contents()).getTargetPremAmt() != contractInfoVO.getTargetPremAmt()) {  //NBA093
				((Life)policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty$Contents()).setTargetPremAmt(contractInfoVO.getTargetPremAmt());  //NBA093
				((Life)policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty$Contents()).setActionUpdate();  //NBA093
			}
		}
		
		//begin SPR1966
		//set Mail Codes
		updateMailCode(aNbaTXLife, policyExtn, NbaOliConstants.OLIX_MAILCODETYPE_ANNSTATEMENT, contractInfoVO.getAnnualStatementMailCode());
		updateMailCode(aNbaTXLife, policyExtn, NbaOliConstants.OLIX_MAILCODETYPE_CONFIRMATION, contractInfoVO.getConfirmationMailCode());
		updateMailCode(aNbaTXLife, policyExtn, NbaOliConstants.OLIX_MAILCODETYPE_BILLORIGINAL, contractInfoVO.getBillOriginalMailCode());
		updateMailCode(aNbaTXLife, policyExtn, NbaOliConstants.OLIX_MAILCODETYPE_PROXY, contractInfoVO.getProxyMailCode());
		updateMailCode(aNbaTXLife, policyExtn, NbaOliConstants.OLIX_MAILCODETYPE_LOANINTORIG, contractInfoVO.getLoanInterestMailCode());
		updateMailCode(aNbaTXLife, policyExtn, NbaOliConstants.OLIX_MAILCODETYPE_OTHER, contractInfoVO.getOtherMailCode());
		//end SPR1966
		//begin SPR3012 
		if(NbaConstants.SYST_VANTAGE.equalsIgnoreCase(aNbaTXLife.getBackendSystem())){
			updateMailCode(aNbaTXLife, policyExtn, NbaOliConstants.OLIX_MAILCODETYPE_NFOORIGINAL, contractInfoVO.getNfoOriginalMailCode()); 
			updateMailCode(aNbaTXLife, policyExtn, NbaOliConstants.OLIX_MAILCODETYPE_LAPSEORIGINAL, contractInfoVO.getLapseMailCode());
		}
		//end SPR3012
	}
	
/**
 * Update or create the appropriate Mail Code Object based on MailCodeType.
 * 
 * @param aNbaTXLife An instance of <code>NbaTXLife</code>
 * @param policyExtn An instance of <code>PolicyExtension</code>
 * @param mailCodeType	MailCodeType to update
 * @param mailCode		update value 
 * @throws NbaBaseException
 */
	//SPR1966 new method
private void updateMailCode(NbaTXLife aNbaTXLife, PolicyExtension policyExtn, long mailCodeType, long mailCode) throws NbaBaseException {
	MailCodes mailCodeObj = null;
	boolean mailCodeFound = false;
	
	//look for matching mail code type
	java.util.ArrayList mailCodesList=policyExtn.getMailCodes();
	for (int i = 0; i < mailCodesList.size(); i++) {
		mailCodeObj = (MailCodes) mailCodesList.get(i);
		if (mailCodeObj.getMailCodeType() == mailCodeType) {
			mailCodeFound = true;
			break;
		}
	}
	if (mailCodeFound) {
		if (mailCodeObj.getMailCode() != mailCode) {
			mailCodeObj.setMailCode(mailCode);
			mailCodeObj.setActionUpdate();
		}
	} else {	//Mail Code type does not exist yet, so create it
		NbaOLifEId olifeId = new NbaOLifEId(aNbaTXLife.getOLifE());
	
		MailCodes newMailCode = new MailCodes();
		newMailCode.setMailCodeType(mailCodeType);
		newMailCode.setMailCode(mailCode);
		olifeId.setId(newMailCode);
		newMailCode.setActionAdd();
		policyExtn.addMailCodes(newMailCode);
	}
}				
	
	/**
	 * Update the Requested Issue date LOB 
	 * @param aNbaUserVO An instance of <code>NbaUserVO</code>
	 * @param aNbaDst An instance of <code>NbaDst</code>
	 * @param aDate An instance of <code>Date</code>
	 * @throws NbaBaseException
	 */
	private void updateLOB(NbaUserVO aNbaUserVO, NbaDst aNbaDst, Date aDate) throws NbaBaseException {
		NbaLob lob = aNbaDst.getNbaLob();
		lob.setIssueDate(aDate);

		WorkflowServiceHelper.update(aNbaUserVO, aNbaDst);
		//SPR2834 code deleted
	}
	/**
	 * Creates value object with contract information
	 * @param An instance of <code>NbaUserVO</code>
	 * @param aNbaTXLife An instance of <code>NbaTXLife</code>
	 * @return aNbaContractInfoVO <code>NbaContractInfoVO</code> object to be returned.
	 */
	//NBA103
	public NbaContractInfoVO loadContractInfo(NbaTXLife aNbaTXLife) throws NbaBaseException {
		try {//NBA103
			NbaContractInfoVO ciVO = new NbaContractInfoVO();
			
			//begin NBA093
			Holding holding = aNbaTXLife.getPrimaryHolding();
			if (holding == null) {
				throw new NbaBaseException("Missing or invalid Holding");
			}
			Policy policy = holding.getPolicy();
			if (policy == null) {
				throw new NbaBaseException("Missing or invalid Policy");
			}
			ApplicationInfo appInfo = policy.getApplicationInfo();
			if (appInfo == null) {
				throw new NbaBaseException("Missing or invalid ApplicationInfo");
			}
			
			//application details
			ciVO.setSignedDate(appInfo.getSignedDate());
			ciVO.setHOAssignedAppNumber(appInfo.getHOAssignedAppNumber());
			ciVO.setApplicationJurisdiction(appInfo.getApplicationJurisdiction());
			ciVO.setIssueState(policy.getJurisdiction());
			//end NBA093
			
			//residence details
			ciVO.setResidenceCountry(-1);
			ciVO.setResidenceState(-1);
			String payerRelationRoleCode = aNbaTXLife.getPartyId(NbaOliConstants.OLI_REL_PAYER);
			if (payerRelationRoleCode != null){
				ciVO.setResidenceCounty(aNbaTXLife.getParty(payerRelationRoleCode).getParty().getResidenceCounty());
				ciVO.setResidenceState(aNbaTXLife.getParty(payerRelationRoleCode).getParty().getResidenceState());
				ciVO.setResidenceCountry(aNbaTXLife.getParty(payerRelationRoleCode).getParty().getResidenceCountry());
				
			}
	
			//options details
			//begin NBA093
			if(ciVO.getRequestedIssueDate() == null && appInfo.hasRequestedPolDate() ) {
				ciVO.setRequestedIssueDate(appInfo.getRequestedPolDate())	;
			}
			
			ciVO.setReplacementType(policy.getReplacementType()); // NBA093
			ciVO.setAssignmentCode(holding.getAssignmentCode());			
			ciVO.setRestrictionCode(holding.getRestrictionCode());		 
			ciVO.setPensionCode(holding.getQualifiedCode());
			
			HoldingExtension holdingExtn = NbaUtils.getFirstHoldingExtension(holding);
			//end NBA093
			//NBA093 deleted 5 lines
			//begin NBA093
				//if (applicationInfoExtn.hasRestrictionCode()){
				//	ciVO.setRestrictionCode(applicationInfoExtn.getRestrictionCode());		 
				//} 
			//end NBA093
			//NBA093 deleted 21 lines
	
			//policy details
			//begin NBA093
			if (holding.hasLastAnniversaryDate()){
				ciVO.setLastAnnivDate(holding.getLastAnniversaryDate());
			}
			if (holdingExtn != null && holdingExtn.hasLastAccountingDate()){
				ciVO.setLastAccountingDate(holdingExtn.getLastAccountingDate());
			}
			
			ciVO.setApplicationType(String.valueOf(appInfo.getApplicationType()));
			
			if (holding.hasLastFinActivityDate()) {//NBA093
				ciVO.setLastFinancial(holding.getLastFinActivityDate());  //NBA093
			}
			if(policy.hasAdministeringCarrierCode()){
				ciVO.setAdministeringCarrierCode(policy.getAdministeringCarrierCode()); // NBA093
			}
			if (policy.hasGracePeriodEndDate()){
				ciVO.setGracePeriodEndDate(policy.getGracePeriodEndDate());
			}	
			//end NBA093
			//NBA077 code deleted
			PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(policy); //NBA077
					
			ciVO.setPendingPremiumPayingStatus(-1);
			ciVO.setChangeType(-1);
			if(policyExtn != null) {
				if(policyExtn.hasChangeInProgressInd()) {
					ciVO.setChangeInProgressInd(policyExtn.getChangeInProgressInd());
				}
				if(policyExtn.hasNotTakenInd()){
					ciVO.setNotTakenInd(policyExtn.getNotTakenInd());
				}					
				//NBA093 deleted 6 lines
				ciVO.setChangeType(policyExtn.getContractChangeType());
				ciVO.setPendingPremiumPayingStatus(policyExtn.getPendingPremiumPayingStatus());
							
				if(policyExtn.hasPremiumPastDueInd()){
					ciVO.setPremiumPastDue(policyExtn.getPremiumPastDueInd());		
				}
				//NBA093 deleted 3 lines
				if(policyExtn.hasGracePeriodExtInd()) {		
					ciVO.setGracePeriodExtInd(policyExtn.getGracePeriodExtInd());
				}
				//NBA093 deleted 12 lines
				if (aNbaTXLife.getNbaHolding().isLife()){			
					double dbl = ((Life)policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty$Contents()).getTargetPremAmt();  //NBA093
					ciVO.setTargetPremAmt(dbl);	
				}	
			
			
				//company details - details page
				if(policyExtn.hasIssueCompanyCode()){
					ciVO.setIssueCompanyCode(policyExtn.getIssueCompanyCode());
				}
				//NBA093 deleted 3 lines
				if(policyExtn.hasFinancialCompanyCode()){
					ciVO.setFinancialCompanyCode(policyExtn.getFinancialCompanyCode());
				}
				
				//qualification details - details page
				if(policyExtn.hasDisqualifiedInd()) {
					ciVO.setDisqualifiedInd(policyExtn.getDisqualifiedInd());
				}
			
			}
			//qualification details - details page (continued)
			if (aNbaTXLife.getNbaHolding().isLife()){
				LifeUSA lifeUSA = ((Life)policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty$Contents()).getLifeUSA();  //NBA093
				if (lifeUSA != null){
					//Begin NBA298
					LifeUSAExtension lifeUSAExtn = NbaUtils.getFirstLifeUSAExtension(lifeUSA);	
					if(lifeUSAExtn != null){
						ciVO.setMecReason(lifeUSAExtn.getMECReason());	
					}
					
					if(lifeUSA.getMEC1035())
						ciVO.setContractMECStatus(props.getString("MEC"));
					else{
						ciVO.setContractMECStatus(props.getString("NOTMEC"));
					}
					
					//End NBA298
					if (lifeUSA.hasDefLifeInsMethod()){
						ciVO.setDefLifeInsMethod(lifeUSA.getDefLifeInsMethod()); 			
					}

					//TAMRA details
					if (lifeUSA.hasSevenPayPrem()){
						ciVO.setSevenPayPrem(lifeUSA.getSevenPayPrem());
					}
					ciVO.setTaxGrandfatheredType(lifeUSA.getTaxGrandfatheredType());
					//Guidelines details
					if (lifeUSA.hasGuidelineAnnPrem()){
						ciVO.setGuidelineAnnPrem(String.valueOf(lifeUSA.getGuidelineAnnPrem()));
					}
					if (lifeUSA.hasGuidelineSinglePrem()){
						ciVO.setGuidelineSinglePremium(lifeUSA.getGuidelineSinglePrem());
					}			
				}
			}
			if (aNbaTXLife.getNbaHolding().isLife()){
				Life life =  (Life)policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty$Contents();  //NBA093
				if (life.hasLoanIntType()){
					ciVO.setLoanIntType(life.getLoanIntType());
				}
				if(life.hasLoanedAmtIntRate()) {
					ciVO.setLoanedAmtIntRate(life.getLoanedAmtIntRate());
				}
			}else if (aNbaTXLife.getNbaHolding().isAnnuity()){
				Annuity annuity = (Annuity)policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty$Contents();  //NBA093
				if(annuity.hasLoanedAmtIntRate()) {
					ciVO.setLoanedAmtIntRate(annuity.getLoanedAmtIntRate());
				}
		
			}		
			
			//begin SPR1966
			//mail codes
			MailCodes mailCodeObj = null;
			java.util.ArrayList mailCodesList=policyExtn.getMailCodes();
			for (int i = 0; i < mailCodesList.size(); i++) {
				mailCodeObj = (MailCodes) mailCodesList.get(i);
				long mailCode = mailCodeObj.getMailCode(); //SPR3012
				if (mailCodeObj.getMailCodeType() == NbaOliConstants.OLIX_MAILCODETYPE_ANNSTATEMENT) {
					ciVO.setAnnualStatementMailCode(mailCode); //SPR2992
				} else
				if (mailCodeObj.getMailCodeType() == NbaOliConstants.OLIX_MAILCODETYPE_CONFIRMATION) {
					ciVO.setConfirmationMailCode(mailCode); //SPR2992		 		
				} else
				if (mailCodeObj.getMailCodeType() == NbaOliConstants.OLIX_MAILCODETYPE_BILLORIGINAL) {
					ciVO.setBillOriginalMailCode(mailCode); //SPR2992				
				} else
				if (mailCodeObj.getMailCodeType() == NbaOliConstants.OLIX_MAILCODETYPE_PROXY) {
					ciVO.setProxyMailCode(mailCode); //SPR2992				
				} else
				if (mailCodeObj.getMailCodeType() == NbaOliConstants.OLIX_MAILCODETYPE_LOANINTORIG) {
					ciVO.setLoanInterestMailCode(mailCode); //SPR2992				
				} else
				if (mailCodeObj.getMailCodeType() == NbaOliConstants.OLIX_MAILCODETYPE_OTHER) {
					ciVO.setOtherMailCode(mailCode); //SPR2992
				//begin SPR3012
				} else if(mailCodeObj.getMailCodeType() == NbaOliConstants.OLIX_MAILCODETYPE_NFOORIGINAL){
				    ciVO.setNfoOriginalMailCode(mailCode);
				} else if(mailCodeObj.getMailCodeType() == NbaOliConstants.OLIX_MAILCODETYPE_LAPSEORIGINAL){
				    ciVO.setLapseMailCode(mailCode);
				//end SPR3012
				}
			}
			return handleFieldState(aNbaTXLife, ciVO);
		} catch (NbaBaseException e) {//NBA103
			getLogger().logException(e);//NBA103
			throw e;//NBA103
		} catch (Throwable t) {//NBA103
			NbaBaseException e = new NbaBaseException(t);//NBA103
			getLogger().logException(e);//NBA103
			throw e;//NBA103
		}
		//end SPR1966

		//SPR1966 deleted code
	}
	
	/** Handles the disabling and enabling of fields
	 *  @param aNbaTXLife an instance of <code>NbaTXLife</code>
	 *  @param ciVO A list of <code>NbaContractInfoVO</code> objects containing contract information
	 *  @return NbaContractInfoVO value object having enabled and/or disabled properties set
	 */
	//SPR1966 new method
	protected NbaContractInfoVO handleFieldState(NbaTXLife aNbaTXLife, NbaContractInfoVO ciVO){

		if (!aNbaTXLife.getBackendSystem().equals(NbaConstants.SYST_VANTAGE)) { //SPR2992
			ciVO.setMailCodesDisabled(true); //SPR3012
		}
		return ciVO;
	}
	
}
