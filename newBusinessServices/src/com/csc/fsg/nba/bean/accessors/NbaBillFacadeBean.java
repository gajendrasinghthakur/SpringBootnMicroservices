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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.csc.fsg.nba.vo.NbaBillInfoVO;
import com.csc.fsg.nba.vo.NbaBillingTableInfoVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.AccountHolderNameCC;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.Banking;
import com.csc.fsg.nba.vo.txlife.BankingExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
/**
 * This is a stateless session bean class to send billing transactions to the database.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA041</td><td>Version 3</td><td>Billing Business Function</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>SPR1511</td><td>Version 4</td><td>Billing BF - Value are not displayed on refresh</td></tr>
 * <tr><td>SPR1793</td><td>Version 4</td><td>Logic error prevents changes in Billing View from being permanently comitted to database.</td></tr>
 * <tr><td>SPR1629</td><td>Version 4</td><td>Transaction Validation Clean up</td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr> 
 * <tr><td>SPR1965</td><td>Version 4</td><td>Add Vantage fields to Billing BF</td></tr> 
 * <tr><td>SPR1983</td><td>Version 4</td><td>View Edits Incorrect for Nonstandard Modes</td></tr> 
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr> 
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR2584</td><td>Version 5</td><td>On Refresh Credit Card number was displayed in Bank account field in Billing BF view.</td></tr>
 * <tr><td>SPR2424</td><td>Version 5</td><td>Quoted Premium Basis Frequency and Amount are not committed to database.</td></tr>
 * <tr><td>SPR2630</td><td>Version 5</td><td>Run time error "line 3; error: expected')'" is generated on an update using Billing Business Function.</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR3573</td><td>Version 8</td><td>Credit Card information is not saved</td></tr>
 * <tr><td>ALS4875</td><td>AXA Life Phase 1</td><td>Contract Validation Message appearing on the Contract Messages tab for unknown reason</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaBillFacadeBean implements SessionBean {
	public final static String BILL = "BILL"; //ALS4875
	private javax.ejb.SessionContext mySessionCtx;
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
	 * @return NbaLogger the logger implementation
	 */
	//SPR1793 New Method
	protected static NbaLogger getLogger() {
		NbaLogger logger = null;
		try {
			logger = NbaLogFactory.getLogger(NbaBillFacadeBean.class.getName());
		} catch (Exception e) {
			NbaBootLogger.log("NbaBillFacadeBean class could not get a logger from the factory.");
			e.printStackTrace(System.out);
		}
		return logger;
	}
	/**
	 * Retrieves the billing information and polulate 
	 * <code> NbaBillInfoVO </code> with details.
	 * @param aNbaTXLife An instance of <code>NbaTXLife</code>
	 * @return NbaBillInfoVO Billing value object.
	 * @throws NbaBaseException
	 */
	//NBA103
	public NbaBillInfoVO loadBillInfo(NbaTXLife aNbaTXLife) throws NbaBaseException {
		try {//NBA103
			// SPR3290 code deleted	
			NbaBillInfoVO billInfoVO = new NbaBillInfoVO();		
			Policy policy = aNbaTXLife.getPrimaryHolding().getPolicy();
			//Billing Tab
			if(policy.getPaymentMethod() > 0){
				billInfoVO.setForm(String.valueOf(policy.getPaymentMethod()));
			}
			if(policy.getPaymentMode() > 0){
				billInfoVO.setMode(String.valueOf(policy.getPaymentMode()));
			}
			if(policy.getPaymentAmt() > 0){
				billInfoVO.setPremium(policy.getPaymentAmt());					
			}
			if(policy.getPaymentDraftDay() > 0){
				billInfoVO.setDraftDay(policy.getPaymentDraftDay());					
			}
			if(policy.getBillNumber() != null) {					//SPR1965 //ALS3927
				billInfoVO.setBillNumber(policy.getBillNumber());	//SPR1965 //ALS3927
			}														//SPR1965
			
			//Options tab
			if(policy.getPaidToDate() != null){
				billInfoVO.setOptionPaidToDate(policy.getPaidToDate());		
			}
			if(policy.getBilledToDate() != null){
				billInfoVO.setOptionBilledToDate(policy.getBilledToDate());			
			}
			//Last Billing group box
			if(policy.getLastNoticeDate() != null){
				billInfoVO.setLastBillingDate(policy.getLastNoticeDate());			
			}
			//Last Billing group box
			if(policy.getLastNoticeType() > 0){
				billInfoVO.setKind(String.valueOf(policy.getLastNoticeType()));
			}		
			
			//begin NBA093
			if(policy.getSpecialHandling()>0){
				billInfoVO.setHandling(String.valueOf(policy.getSpecialHandling()));
			}
			//end NBA093
			//Policy Extensions 
			if(policy.getOLifEExtensionCount() > 0){			
				for(int i=0,j=policy.getOLifEExtensionCount();i < j; i++){
					//NBA093 deleted 50 lines
					//begin NBA093
					if(policy.getOLifEExtensionAt(i).isPolicyExtension()){
						PolicyExtension policyExtn = policy.getOLifEExtensionAt(i).getPolicyExtension();
						if(policyExtn.getBillingOption()>0){
							billInfoVO.setBilling(String.valueOf(policyExtn.getBillingOption()));
					}
					if(policyExtn.getAdditionalLevelPremiumAmt() > 0){
						billInfoVO.setAdditionalLevel(policyExtn.getAdditionalLevelPremiumAmt());						
					}
					//SPR1983 deleted code
					// begin NBA104
					if(policyExtn.getNonStandardBillAmt() > 0){
						billInfoVO.setNonStandardBillAmt(policyExtn.getNonStandardBillAmt());						
					}
					// end NBA104					
					if(policyExtn.getFirstMonthlyDate() != null){
						billInfoVO.setFirstMonthlyDate(policyExtn.getFirstMonthlyDate());						
					}	
					if(policyExtn.getNonStandardPaidToDate() != null){
						billInfoVO.setNonStandardPaidToDate(policyExtn.getNonStandardPaidToDate());						
					}
					if(policyExtn.getPayrollFrequency() > 0){
						billInfoVO.setPayrollFrequency(String.valueOf(policyExtn.getPayrollFrequency()));
					}
					if(policyExtn.getFirstSkipMonth() != null){
						billInfoVO.setFirstSkipMonth(policyExtn.getFirstSkipMonth());
					}
					if(policyExtn.getQuotedPremiumBasisAmt() > 0){
						billInfoVO.setQuotedPremium(policyExtn.getQuotedPremiumBasisAmt());						
					}
					//SPR1511 begin
					if(policyExtn.getQuotedPremiumBasisFrequency() > 0){
						billInfoVO.setQuotedPremiumBasis(policyExtn.getQuotedPremiumBasisFrequency());    //NBA104
					}
					//SPR1511 end
					if(policyExtn.getInitialBillToDate() != null){
						billInfoVO.setInitialBilledToDate(policyExtn.getInitialBillToDate());						
					}
					if(policyExtn.getInitialDeductionDate() != null){
						billInfoVO.setDeductionDate(policyExtn.getInitialDeductionDate()); //SPR1511						
					}
					if(policyExtn.getTiming() > 0){
						billInfoVO.setTiming(String.valueOf(policyExtn.getTiming()));
					}					
					if(policyExtn.getExcessCollectedAmt() > 0){
						billInfoVO.setExcessPremium(policyExtn.getExcessCollectedAmt());
					}					
					//SPR1965 begin
					if(policyExtn.getProxyType() > 0) {
						billInfoVO.setProxyType(String.valueOf(policyExtn.getProxyType()));
					}
					if(policyExtn.getLoanInterestBill() > 0) {
						billInfoVO.setLoanInterestBill(String.valueOf(policyExtn.getLoanInterestBill()));
					}
					//SPR1965 end
				}
				//end NBA093
			}
		}		
		
		//SPR1965 begin
		if (policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isAnnuity()) {
			Annuity annuity = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity();
			if (annuity.getPremType() > 0) {
				billInfoVO.setPremType(String.valueOf(annuity.getPremType()));
			}
		}
			//SPR1965 end
	
			billInfoVO.setCurrentDate(new Date());		
			return handleFieldState(aNbaTXLife,billInfoVO);
		} catch(Throwable t) {//NBA103
			NbaBaseException e = new NbaBaseException(t);//NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
			throw e;//NBA103
		}
	}
	
	/**
	 * Creates value objects representing banking contract information.	 
	 * @param aNbaTXLife An instance of <code>NbaTXLife</code>
	 * @return ArrayList A list of <code>NbaBillingTableInfoVO</code> objects to be returned.
	 * @throws NbaBaseException
	 */
	//NBA103
	public ArrayList loadBillingTableInfo(NbaTXLife aNbaTXLife) throws NbaBaseException {
		try {//NBA103				
			ArrayList bills = new ArrayList();
			//NBA093 deleted 2 lines
			NbaBillingTableInfoVO billingTableInfoVO = null;
			Holding holding = aNbaTXLife.getPrimaryHolding();  //NBA093
			// SPR3290 code deleted
			int count = holding.getBankingCount();  //NBA093
			for (int i = 0; i < count; i++) {  //NBA093
				Banking banking = holding.getBankingAt(i);//NBA093
				BankingExtension bankingExt = NbaUtils.getFirstBankingExtension(banking);//NBA093
				if(bankingExt != null && bankingExt.getCreditCardChargeUse()  != NbaOliConstants.OLIEXT_LU_CHARGEUSE_PAYMENT){ //NBA093 SPR3573
					billingTableInfoVO = new NbaBillingTableInfoVO();
					billingTableInfoVO.setEffective(bankingExt.getBillControlEffDate());//NBA093
					billingTableInfoVO.setIdentification(bankingExt.getBillControlNumber());//NBA093
					billingTableInfoVO.setAuthorization(bankingExt.getBillingSequence()); //NBA093
					billingTableInfoVO.setEFTAccountType(String.valueOf(banking.getBankAcctType())); //NBA093
					billingTableInfoVO.setCreditCardType(String.valueOf(banking.getCreditCardType()));				
					if(banking.getRoutingNum() != null){  //NBA093
						billingTableInfoVO.setTransitNumber(String.valueOf(banking.getRoutingNum())); //NBA093
					}
					if(bankingExt.getBranchNumber() != null){  //NBA093
						billingTableInfoVO.setBranchNumber(String.valueOf(bankingExt.getBranchNumber()));  //NBA093				
					}
					if(NbaOliConstants.OLI_BANKACCT_CREDCARD == banking.getBankAcctType()){ //SPR2584
						billingTableInfoVO.setCreditCardNumber(String.valueOf(banking.getAccountNumber()));
					}else{
						billingTableInfoVO.setBankAccount(banking.getAccountNumber());
					}								
					if(banking.getCreditCardExpDate()!=null){
						billingTableInfoVO.setExpirationDate(NbaUtils.getDateFromStringInUSFormat(banking.getCreditCardExpDate())); //NBA093
					}
					billingTableInfoVO.setBankingID(banking.getId());				
					int accountHolders = 0;
					if (bankingExt.hasAccountHolderNameCC()) {  //NBA093
						accountHolders = bankingExt.getAccountHolderNameCC().getAccountHolderNameCount();  //NBA093
					}
					if (accountHolders > 0) {
						for (int j = 0; j < accountHolders; j++) {
							billingTableInfoVO.addSignature(bankingExt.getAccountHolderNameCC().getAccountHolderNameAt(j));  //NBA093
						}
					}
					if(billingTableInfoVO != null){
						bills.add(billingTableInfoVO);
					}
				}//NBA093
			}			
			//NBA093 deleted a line
			return bills;
		} catch (Throwable t) {//NBA103
			NbaBaseException e = new NbaBaseException(t);//NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
			throw e;//NBA103
		}
	}
	
	
	/**
	 * Saves banking details for contracts provided by the calling program. 
	 * @param user An instance of <code>NbaUserVO</code>
	 * @param aNbaDst An instance of <code>NbaDst</code>
	 * @param aNbaTXLife An instance of <code>NbaTXLife</code>
	 * @param billingDetails A list of <code>NbaBillingTableInfoVO</code> objects containing billing details.
	 * @throws NbaBaseException
	 */
	protected void saveBankingDetails(NbaUserVO user, NbaDst aNbaDst, NbaTXLife aNbaTXLife, List billingDetails)
		throws NbaBaseException { //SPR1793
		// SPR3290 code deleted
		//SPR1851 code deleted
		// SPR3290 code deleted
		NbaBillingTableInfoVO contract = null;
		for (int i = 0; i < billingDetails.size(); i++) {
			contract = (NbaBillingTableInfoVO) billingDetails.get(i);
			// SPR3290 code deleted
			if (!contract.isActionDisplay()) {
				handleActions(aNbaTXLife, contract);
				//SPR1793 code deleted
			}
		//SPR1793 code deleted
		}
		//SPR1793 code deleted
	}
	
	/**
	 * Saves banking details for a contract. 
	 * @param user An instance of <code>NbaUserVO</code>
	 * @param aNbaDst An instance of <code>NbaDst</code>
	 * @param aNbaTXLife An instance of <code>NbaTXLife</code>
	 * @param billingVO A <code>NbaBillInfoVO</code> object containing billing information
	 * @throws NbaBaseException
	 */
	protected void saveBillingInfo(NbaUserVO user, NbaDst aNbaDst, NbaTXLife aNbaTXLife, NbaBillInfoVO billingVO) throws NbaBaseException {		//SPR1793		
		Policy policy = aNbaTXLife.getPrimaryHolding().getPolicy();				
		policy.setPaymentMethod(billingVO.getForm());
		policy.setPaymentMode(billingVO.getMode());
		policy.setPaymentAmt(billingVO.getPremium());
		policy.setPaymentDraftDay(billingVO.getDraftDay());
		policy.setBillNumber(billingVO.getBillNumber());	//SPR1965
		//Billing Option		
		policy.setPaidToDate(billingVO.getOptionPaidToDate());
		policy.setBilledToDate(billingVO.getOptionsBilledToDate());		
		//Last Billing
		policy.setLastNoticeDate(billingVO.getLastBillingDate());
		policy.setLastNoticeType(billingVO.getKind());
		policy.setActionUpdate();
		policy.setSpecialHandling(billingVO.getHandling());	 //NBA093		
		//AdvancedBilling Extension
		if(policy.getOLifEExtensionCount()>0){
			// SPR3290 code deleted
			//NBA093 deleted 3 lines
			PolicyExtension policyExtension = null;
			OLifEExtension olifeExt = null;		
			// SPR3290 code deleted
			for(int i=0,j=policy.getOLifEExtensionCount();i < j; i++){
				//NBA093 deleted 9 lines
				if(policy.getOLifEExtensionAt(i).isPolicyExtension()){
					policyExtension = policy.getOLifEExtensionAt(i).getPolicyExtension();	
				}									
			}
			//NBA093 deleted 35 lines
			//begin NBA093
			
			if (policyExtension == null){
				olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);	
				policyExtension  = olifeExt.getPolicyExtension();
				policyExtension.setActionAdd();
				policy.addOLifEExtension(olifeExt);	
			}
			policyExtension.setBillingOption(billingVO.getBilling());
			policyExtension.setAdditionalLevelPremiumAmt(billingVO.getAdditionalLevel());
			//SPR1511 begin
			// SPR1983 deleted code			
			policyExtension.setQuotedPremiumBasisFrequency(billingVO.getQuotedPremiumBasis());
			//SPR1511 end
			policyExtension.setQuotedPremiumBasisAmt(billingVO.getQuotedPremium()); //SPR2424
			policyExtension.setFirstMonthlyDate(billingVO.getFirstMonthlyDate());			
			policyExtension.setNonStandardPaidToDate(billingVO.getNonStandardPaidToDate());
			policyExtension.setPayrollFrequency(billingVO.getPayrollFrequency());
			policyExtension.setFirstSkipMonth(billingVO.getFirstSkipMonth());						
			policyExtension.setInitialBillToDate(billingVO.getInitialBilledToDate());			
			policyExtension.setInitialDeductionDate(billingVO.getDeductionDate());			
			policyExtension.setTiming(billingVO.getTiming());
			//end NBA093
			policyExtension.setProxyType(billingVO.getProxyType());					//SPR1965
			policyExtension.setLoanInterestBill(billingVO.getLoanInterestBill());	//SPR1965
			policyExtension.setExcessCollectedAmt(billingVO.getExcessPremium());
			policyExtension.setActionUpdate();
		}
		
		//SPR1965 begin
		if (policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isAnnuity()) {
			Annuity annuity = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity();
			annuity.setPremType(billingVO.getPremType());
			annuity.setActionUpdate();
		}
		//SPR1965 end
		
		try {
			aNbaTXLife = NbaContractAccess.doContractUpdate(aNbaTXLife, aNbaDst, user); // NBA050 SPR1851
			//SPR1851 code deleted		 
		}catch(NbaTransactionValidationException ntve){ //SPR1629
			throw ntve;  //SPR1629
		}
		catch (Exception e) {
			getLogger().logError(e);
		}
	}	
		
	 /** Handles ADD, DELETE or UPDATE actions on a billing contract.
	 *  @param aNbaTXLife an instance of <code>NbaTXLife</code>
	 *  @param contract A <code>NbaBillingTableInfoVO</code> object containing banking information
	 *  @throws NbaBaseException
	 */
	protected void handleActions(NbaTXLife aNbaTXLife, NbaBillingTableInfoVO contract) throws NbaBaseException {		
		Holding holding = aNbaTXLife.getPrimaryHolding();		
		//NBA093 deleted a line
		Banking banking = null;				
		if (contract.isActionDelete()) {
			if (holding != null) {
				for (int i = holding.getBankingCount() - 1; i >= 0; i--) {  //NBA093
					banking = holding.getBankingAt(i);  //NBA093
					if (banking.getId().equals(contract.getBankingID())) {
						banking.setActionDelete();
						if (contract.getSignatures() != null) {
							if (NbaUtils.getFirstBankingExtension(banking).hasAccountHolderNameCC()) { //NBA093
								NbaUtils.getFirstBankingExtension(banking).getAccountHolderNameCC().setActionDelete();//NBA093
							}
						}
						break;
					}
				}
			}
		}
		else if (contract.isActionAdd()){
			Banking newBanking = new Banking();
			// begin NBA093			
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_BANKING);
			BankingExtension bankingExt = olifeExt.getBankingExtension();
			newBanking.addOLifEExtension(olifeExt);
			//end NBA093
			NbaOLifEId id = new NbaOLifEId(aNbaTXLife);
			id.setId(newBanking);			
			if(contract.getEffective() != null){
				bankingExt.setBillControlEffDate(contract.getEffective());//NBA093
			}
			if(contract.getIdentification() != null){
				bankingExt.setBillControlNumber(contract.getIdentification());//NBA093
			}
			if(contract.getAuthorization() != null){			
				bankingExt.setBillingSequence(contract.getAuthorization());//NBA093
			}
			if(contract.getEftAccountType() != null){
				newBanking.setBankAcctType(contract.getEftAccountType());//NBA093
			}
			if(contract.getCreditCardType() != null){
				newBanking.setCreditCardType(contract.getCreditCardType());
			}			
			if( contract.getEftAccountType().equals(String.valueOf(NbaOliConstants.OLI_BANKACCT_CREDCARD))){
				newBanking.setAccountNumber(contract.getCreditCardNumber());
			}else{
				newBanking.setAccountNumber(contract.getBankAccount());
			}
			if(contract.getTransitNumber() !=null){			
				newBanking.setRoutingNum(contract.getTransitNumber());//NBA093
			}
			if(contract.getBranchNumber() != null){
				bankingExt.setBranchNumber(contract.getBranchNumber());	//NBA093
			}
			if(contract.getExpirationDate() != null){
				newBanking.setCreditCardExpDate(NbaUtils.getStringInUSFormatFromDate(contract.getExpirationDate())); //NBA093
			}			
			newBanking.setActionAdd();
			//NBA093 deleted 8 lines	
			holding.addBanking(newBanking); //Add banking object   NBA093
			if (contract.getSignatures() != null){				
				 if(!bankingExt.hasAccountHolderNameCC()){//NBA093
					 bankingExt.setAccountHolderNameCC(new AccountHolderNameCC());//NBA093
					 bankingExt.getAccountHolderNameCC().setActionAdd();//NBA093
				 }						 					
				 AccountHolderNameCC acntHolderCC = bankingExt.getAccountHolderNameCC();	//NBA093			 				
			 	 for(int j=0; j < contract.getSignatures().size();j++){			 						
					if (!("".equals(contract.getSignatureAt(j).trim()))) { //SPR2630
					 		acntHolderCC.addAccountHolderName(contract.getSignatureAt(j));
				 	}
			 	}
			 	acntHolderCC.setActionUpdate();			 				
			}			
			
		}else if (contract.isActionUpdate()) {
			// NBA093 deleted 4 lines
			int count = holding.getBankingCount();  //NBA093
			for (int i=0; i < count; i++) {  //NBA093
				banking = holding.getBankingAt(i);  //NBA093
				BankingExtension bankingExt = NbaUtils.getFirstBankingExtension(banking);//NBA093
				if(bankingExt != null){ //NBA093
					if (banking.getId().equals(contract.getBankingID())) {								
						if(contract.getEffective() != null){
							bankingExt.setBillControlEffDate(contract.getEffective());//NBA093
						}
						if(contract.getIdentification() != null){								
							bankingExt.setBillControlNumber(contract.getIdentification());//NBA093
						}
						if(contract.getAuthorization()!=null){
							bankingExt.setBillingSequence(contract.getAuthorization());//NBA093
						}
						if(contract.getEftAccountType() != null){
							banking.setBankAcctType(contract.getEftAccountType());
						}
						if(contract.getCreditCardType() != null){
							banking.setCreditCardType(contract.getCreditCardType());
						}
						if( contract.getEftAccountType().equals(String.valueOf(NbaOliConstants.OLI_BANKACCT_CREDCARD))){
							banking.setAccountNumber(contract.getCreditCardNumber());
						}else{
							banking.setAccountNumber(contract.getBankAccount());
						}								
						if(contract.getTransitNumber() != null){
							banking.setRoutingNum(contract.getTransitNumber());//NBA093
						}
						if(contract.getBranchNumber() != null){
							bankingExt.setBranchNumber(contract.getBranchNumber());		//NBA093					
						}
						if(contract.getExpirationDate() != null){
							banking.setCreditCardExpDate(NbaUtils.getStringInUSFormatFromDate(contract.getExpirationDate())); //NBA093																 										
						}
						banking.setId(contract.getBankingID());
			 			if (contract.getSignatures() != null){
							if(!bankingExt.hasAccountHolderNameCC()){ //NBA093
								bankingExt.setAccountHolderNameCC(new AccountHolderNameCC());//NBA093
								bankingExt.getAccountHolderNameCC().setActionAdd();//NBA093
			 				}						 					
			 				AccountHolderNameCC acntHolderCC = bankingExt.getAccountHolderNameCC();	//NBA093			 				
		 					acntHolderCC.getAccountHolderName().clear();
		 					for(int j=0; j < contract.getSignatures().size();j++){			 						
								if (!("".equals(contract.getSignatureAt(j).trim()))) { //SPR2630
				 					acntHolderCC.addAccountHolderName(contract.getSignatureAt(j));
			 					}
		 					}
		 					acntHolderCC.setActionUpdate();			 				
			 			}
			 			bankingExt.setActionUpdate();//NBA093
					} //NBA093
					banking.setActionUpdate();					
					//NBA093 deleted a line
				}
			}
			//NBA093 deleted 2 lines
		}
	}		
	
	
	/** Handles the disabling and enabling of fields
	 *  @param aNbaTXLife an instance of <code>NbaTXLife</code>
	 *  @param billVO A <code>NbaBillInfoVO</code> object containing billing information
	 *  @return NbaBillInfoVO value object having enabled and/or disabled properties set
	 */
	protected NbaBillInfoVO handleFieldState(NbaTXLife aNbaTXLife, NbaBillInfoVO billVO){
		long productType = aNbaTXLife.getPrimaryHolding().getPolicy().getProductType();
		
		//Premium, Quoted Mode and Excess Premium entry fields
		if (productType == NbaOliConstants.OLI_PRODTYPE_WL
			|| productType == NbaOliConstants.OLI_PRODTYPE_TERM
			|| productType == NbaOliConstants.OLI_PRODTYPE_EXINTL
			|| productType == NbaOliConstants.OLI_PRODTYPE_TERMCV
			|| productType == NbaOliConstants.OLI_PRODTYPE_TRADITIONAL
			|| productType == NbaOliConstants.OLI_PRODTYPE_INTWL) {
			billVO.setPremiumDisabled(true);
			// SPR1983 deleted code
			billVO.setExcessPremiumLevelDisabled(false);
		} else {
			billVO.setPremiumDisabled(false);			
			// SPR1983 deleted code
			if( productType == NbaOliConstants.OLI_PRODTYPE_UL) {
				billVO.setExcessPremiumLevelDisabled(false);
			}else{
				billVO.setExcessPremiumLevelDisabled(true);
			}
		}
		
		//Billing drop down
		if (productType == NbaOliConstants.OLI_PRODTYPE_ANN
			|| productType == NbaOliConstants.OLI_PRODTYPE_VAR
			|| productType == NbaOliConstants.OLI_PRODTYPE_UL
			|| productType == NbaOliConstants.OLI_PRODTYPE_VUL) {
			billVO.setBillingDisabled(false);
			billVO.setQuotedPremimDisabled(false); //SPR1511			
		} else {
			billVO.setBillingDisabled(true);
			billVO.setQuotedPremimDisabled(true); //SPR1511			
		}
		
		//Additional Level Premium
		if (productType == NbaOliConstants.OLI_PRODTYPE_WL
			|| productType == NbaOliConstants.OLI_PRODTYPE_INTWL) {
			billVO.setAdditionalLevelDisabled(false);
		} else {
			billVO.setAdditionalLevelDisabled(true);
		}
		
		//SPR1965	begin
		if (aNbaTXLife.getBackendSystem().equals(NbaConstants.SYST_VANTAGE)) {
			//PremType, ProxyType and LoanInterestBill are only enabled for Vantage
			billVO.setBillNumberDisabled(false);
			billVO.setPremTypeDisabled(false);
			billVO.setProxyTypeDisabled(false);
			billVO.setLoanInterestBillDisabled(false);
		} else {
			billVO.setBillNumberDisabled(true);
			billVO.setPremTypeDisabled(true);
			billVO.setProxyTypeDisabled(true);
			billVO.setLoanInterestBillDisabled(true);
		}
		//SPR1965  end
		// begin SPR1983
		if (aNbaTXLife.getPolicy().hasApplicationInfo() &&
			aNbaTXLife.getPolicy().getApplicationInfo().getApplicationType() == NbaOliConstants.OLI_APPTYPE_NEW) {
			billVO.setExcessPremiumLevelDisabled(true);
		}
		// end SPR1983		
		
		return billVO;
	}
	/**
	 * Commit the changes to an Array and the NbaBillInfoVO form and invoke the Session Facade
	 * to apply the form changes to the conract. Resresh the table pane with the results.
 	 * @param nbaUserVO An instance of <code>NbaUserVO</code>
	 * @param nbaDst An instance of <code>NbaDst</code>
	 * @param nbaTXLife This is an instance of NbaTXLife object containing the updated data for the contract.
	 * @param nbaBillInfoVO This contains billing information.
	 * @param billDetails A list of <code>NbaBillingTableInfoVO</code> objects containing billing details.
	 * @return NbaTXLife containing the respose to the commit request.
	 * @throws NbaBaseException
	 */
	//SPR1793 New Method
	//NBA103
	public NbaTXLife commitChanges(NbaUserVO nbaUserVO, NbaDst nbaDst, NbaTXLife aNbaTXLife, NbaBillInfoVO nbaBillInfoVO, List billDetails)
		throws NbaBaseException {
			try {//NBA103
				saveBillingInfo(nbaUserVO, nbaDst, aNbaTXLife, nbaBillInfoVO);
				saveBankingDetails(nbaUserVO, nbaDst, aNbaTXLife, billDetails);
				aNbaTXLife.setBusinessProcess(BILL); //ALS4875
				NbaTXLife result = NbaContractAccess.doContractUpdate(aNbaTXLife, nbaDst, nbaUserVO); //SPR1851
				//SPR1851 code deleted
				return result;
			} catch(NbaBaseException e) {//NBA103
				NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
				throw e;//NBA103
			} catch(Throwable t) {//NBA103
				NbaBaseException e = new NbaBaseException(t);//NBA103
				NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
				throw e;//NBA103
			}				
	}
}
