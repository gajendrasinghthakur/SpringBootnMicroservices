package com.csc.fsg.nba.contract.validation;
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
 * 
 * *******************************************************************************<BR>
 */
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fs.dataobject.accel.product.Fee;
import com.csc.fs.dataobject.accel.product.FeeExtension;
import com.csc.fs.dataobject.accel.product.FeeTableRef;
import com.csc.fs.dataobject.accel.product.PolicyProduct;
import com.csc.fs.dataobject.accel.product.PolicyProductExtension;
import com.csc.fs.dataobject.accel.product.UnderwritingClassProduct;
import com.csc.fs.dataobject.accel.product.UnderwritingClassProductExtension;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.ValProc;
import com.csc.fsg.nba.vo.txlife.AccountHolderNameCC;
import com.csc.fsg.nba.vo.txlife.Banking;
import com.csc.fsg.nba.vo.txlife.BankingExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.StandardAttr;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;
/**
 * NbaValBilling performs Billing validation.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA064</td><td>Version 3</td><td>Contract Validation</td></tr>
 * <tr><td>SPR1769</td><td>Version 4</td><td>Billing Validation incorrectly assumes that PolicyProduct.FeeExtension will be present </td></tr>
 * <tr><td>SPR1804</td><td>Version 4</td><td>Billed to date should be set to paid to date. </td></tr>
 * <tr><td>SPR1820</td><td>Version 4</td><td>For Vantage remove the BillledToDate if the payment method is No Bill or EFT. </td></tr>
 * <tr><td>SPR1994</td><td>Version 4</td><td>Correct user validation example </td></tr>
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 * <tr><td>SPR1996</td><td>Version 4</td><td>Insurance Validation Min-Max Amts Need to Vary by Issue Age/Add Min-Max for Units/Prem Amt</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>SPR2049</td><td>Version 4</td><td>InvocationTargetException generated if Banking.AccountNumber is not present.</td></tr>
 * <tr><td>SPR1234</td><td>Version 4</td><td>General source code clean up </td></tr>
 * <tr><td>SPR2011</td><td>Version 4</td><td>Billing Validation P011 does not detect missing Initial net annual premium amount</td></tr>
 * <tr><td>SPR2074</td><td>Version 4>/td><td>Edit in Billing validation P024 is incorrect</td></tr>
 * <tr><td>SPR2583</td><td>Version 5</td><td>A valid American express card will have a 15 digit number.</td></tr>
 * <tr><td>SPR2825</td><td>Version 5</td><td>Severe Error getting generated when changing the Payment Method using Billing business function.</td></tr>
 * <tr><td>SPR3020</td><td>Version 6</td><td>BilledToDate is not getting defaulted</td></tr>
 * <tr><td>NBA133</td><td>Version 6</td><td>nbA CyberLife Interface for Calculations and Contract Print</td></tr>
 * <tr><td>SPR3141</td><td>Version 6</td><td>Duplicate code for validating payment mode needs to be removed from NbaValBilling.P001()</td></tr>
 * <tr><td>NBA117</td><td>Version 7</td><td>Pending VANTAGE-ONE Calculations </td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR3573</td><td>Version 8</td><td>Credit Card Information is not saved</td></tr>
 * <tr><td>SPR2151</td><td>Version 8</td><td>Correct the Contract Validation edits and Adaptor logic for EFT, PAC and Credit Card Billing</td></tr>
 * <tr><td>NBA237</td><td>Version 8</td><td>Migrate Policy Product Transmittal XML1201 to 2.15.00</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaValBilling
	extends NbaContractValidationCommon
	implements NbaContractValidationBaseImpl, NbaContractValidationImpl {	//SPR1234
	
	protected static final String ROUTING_NUMBER_MISSING = "Routing number missing";	//SPR2151
	protected static final String DOT = ".";	//SPR2151  
	protected static final String MISSING = " missing";		//SPR2151  
	protected static final String ACCOUNT_NUMBER = "Account Number: ";	//SPR2151 
	protected static final String A_CCTYPELOB = "A_CCTypeLOB";		//SPR2151
	protected static final String A_CCNUMBERLOB = "A_CCNumberLOB";	//SPR2151
	/**
	 * Perform one time initialization.
	 */
	 //NBA237 changed method signature
	public void initialze(NbaDst nbaDst, NbaTXLife nbaTXLife, Integer subset, NbaOLifEId nbaOLifEId, AccelProduct nbaProduct, NbaUserVO userVO) {
		super.initialze(nbaDst, nbaTXLife, subset, nbaOLifEId, nbaProduct, userVO);
		initProcesses();
		Method[] allMethods = this.getClass().getDeclaredMethods();
		for (int i = 0; i < allMethods.length; i++) {
			Method aMethod = allMethods[i];
			String aMethodName = aMethod.getName();
			if (aMethodName.startsWith("process_")) {
				// SPR3290 code deleted
				processes.put(aMethodName.substring(8).toUpperCase(), aMethod);
			}
		}
	}
	// SPR1994 code deleted
	/**
	 * @see com.csc.fsg.nba.contract.validation.NbaContractValidationImpl#validate()
	 */
	// ACN012 changed signature
	public void validate(ValProc nbaConfigValProc, ArrayList objects) {
		if (nbaConfigValProc.getUsebase()) { //ACN012
			super.validate(nbaConfigValProc, objects);
		}else {//ALS2600
		    if (getUserImplementation() != null) {
		        getUserImplementation().validate(nbaConfigValProc, objects);
		    }
	    } //ALS2600    
	}
	/**   
	 * If the payment method is Single Premium (ProductExtension.BillingNotifyOptionType = 1000500003), set the PaymentMode to Single Premium tc(9). 
	 */
	protected void process_P001() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P001()");//NBA103
			PolicyProductExtension policyProductExtension = getPolicyProductExtensionForPlan();
			if (policyProductExtension != null && policyProductExtension.getBillingNotifyOptionType() == 1000500003) { //Single Premiumn
				getPolicy().setPaymentMode(OLI_PAYMODE_SINGLEPAY);
				getPolicy().setActionUpdate();
			}
			//SPR3141 code deleted
		}
	}
	/** 
	 * If the payment method is not Single Premium (ProductExtension.BillingNotifyOptionType = 1000500003), 
	 * validate PaymentMode against table OLI_LU_PAYMODE.
	 */
	protected void process_P902() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P902");//NBA103
			PolicyProductExtension policyProductExtension = getPolicyProductExtensionForPlan();
			if (policyProductExtension != null && policyProductExtension.getBillingNotifyOptionType() != 1000500003) { //Single Premiumn
				long mode = getPolicy().getPaymentMode();
				if (!isValidTableValue(NbaTableConstants.OLI_LU_PAYMODE, mode)) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Payment Mode: ", mode), getIdOf(getPolicy()));
				}
			}
		}
	}
	/**
	 * If the payment method is Single Premium (ProductExtension.BillingNotifyOptionType = 1000500003), set the PaymentMethod to Single Premium tc(20).
	 * Otherwise, validate PaymentMethod against table OLI_LU_PAYMETHOD.
	 */
	protected void process_P002() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P002()");//NBA103
			PolicyProductExtension policyProductExtension = getPolicyProductExtensionForPlan();
			if (policyProductExtension != null && policyProductExtension.getBillingNotifyOptionType() == 1000500003) { //Single Premiumn
				getPolicy().setPaymentMethod(OLI_PAYMETH_SINGLEPREM);
				getPolicy().setActionUpdate();
			} else {
				long method = getPolicy().getPaymentMethod();
				if (!isValidTableValue(NbaTableConstants.OLI_LU_PAYMETHOD, method)) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Payment Method: ", method), getIdOf(getPolicy()));
				}
			}
		}
	}
	/**
	 * Calculate the modal and annualized premium amounts for coverage.
	 * Annual payment = modal premium x number of modes.
	 */
	protected void process_P004() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValBilling.process_P004 for " ,  getCoverage());//NBA103
			updatePremiums(getCoverage());
		}
	}
	/**
	 * Calculate the modal and annualized premium amounts for coverage option.
	 * Annual payment = modal premium x number of modes. 
	 */
	protected void process_P005() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValBilling.process_P005 for" ,  getCovOption());//NBA103
			updatePremiums(getCovOption());
		}
	}
	/**
	 * Calculate the annual premium amount for rider. 
	 * Annual payment = modal premium x number of modes. 
	 */
	protected void process_P006() {
		if (verifyCtl(RIDER)) {
			logDebug("Performing NbaValBilling.process_P006 for" ,  getRider());//NBA103
//			updatePremiums(getRider());
		}
	}
	/**
	 * Calculate the modal and annualized premium amounts for coverage option.
	 * Annual payment = modal premium x number of modes. 
	 */
	protected void process_P007() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValBilling.process_P007 for" ,  getCovOption());//NBA103
			updatePremiums(getCovOption());
		}
	}
	/**
	 * Calculate the annualized premium amount for substandard rating.
	 */
	protected void process_P008() {
		if (verifyCtl(SUBSTANDARDRATING)) {
			logDebug("Performing NbaValBilling.process_P008 for" ,  getSubstandardRating());//NBA103
			updatePremiums(getSubstandardRating());
		}
	}
	/**
	 * Calculate the modal and annualized premium amounts for contract.
	 * This is the modal payment amount for the overall policy, including any premiums associated 
	 * with coverages/riders/options. Annual payment equals modal premium x number of modes 
	 */
	protected void process_P009() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P009");//NBA103
			updatePremiums(getPolicy());
		}
	}
	/**
	 * Retrieve the modal and annualized premium amounts for contract and retrieve the policy fee from the backend system
	 */
	//NBA133 New Method
	protected void process_P037() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P037");
			Policy policy = getPolicy();
			policy.setPaymentAmt(null);
            policy.setAnnualPaymentAmt(null);
            policy.setActionUpdate();

            PolicyExtension policyExt = getPolicyExtension();
            policyExt.setNonStandardBillAmt(null);
            policyExt.setActionUpdate();
            
			updateBESStandardModePremiums(policy);
		}
	}
	
	/**
	 * Retrieve the modal premium amount for contract
	 */
	//NBA117 New Method
	protected void process_P044() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P044");
			Policy policy = getPolicy();
			policy.setPaymentAmt(null);
            policy.setActionUpdate();
			updateBESStandardModePremiums(policy);
		}
	}
	
	/**
	 * Set the BilledToDate equal to PaidToDate if present. Otherwise set the BilledToDate equal to EffDate of contract.
	 */
	protected void process_P010() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P010");//NBA103
			//begin SPR1804
			if (getPolicy().hasPaidToDate()) {
				getPolicy().setBilledToDate(getPolicy().getPaidToDate());
			} else {
				getPolicy().setBilledToDate(getPolicy().getEffDate());
			}
			//end SPR1804
			getPolicy().setActionUpdate();
		}
	}
	/**
	 * If the PaymentMethod is No Bill or EFT, remove the BilledToDate. 
	 * For other payment methods, set the BilledToDate equal to PaidToDate if present. Otherwise set the BilledToDate equal to EffDate of contract.
	 */
	//SPR1820 New Method
	protected void process_P026() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P010");//NBA103
			long method = getPolicy().getPaymentMethod();
			//begin SPR3020
			if (method == NbaOliConstants.OLI_PAYMETH_ETRANS || method == NbaOliConstants.OLI_PAYMETH_NOBILL){ //begin SPR1804 
				getPolicy().deleteBilledToDate();
				getPolicy().setActionUpdate();
			} else {
				process_P010();	
			}
			//end SPR3020
		}
	}
	/**
	 * Verify that an InitialAnnualPremiumAmt (Initial net annual premium amount)  has been specified 
	 * when PolicyProduct.FeeExtension.FeeTableRef.ChargeMethodTC="100500016" (Percent of Initial Net Annual Premium)
	 */
	protected void process_P011() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P011"); //NBA103
			if (!(getPolicyExtension().hasInitialAnnualPremiumAmt() && getPolicyExtension().getInitialAnnualPremiumAmt() > 0)) {
				//begin SPR2011
				PolicyProduct policyProduct = getPolicyProductForPlan();
				Fee fee;
				FeeExtension feeExtension;
				FeeTableRef feeTableRef;
				int tblCnt;
				int feeCnt = policyProduct.getFeeCount();
				main: for (int i = 0; i < feeCnt; i++) {
					fee = policyProduct.getFeeAt(i);
					feeExtension = AccelProduct.getFirstFeeExtension(fee); //NBA237
					if (feeExtension != null) { //SPR1769
						//SPR1769 code deleted
						tblCnt = feeExtension.getFeeTableRefCount();
						for (int j = 0; j < tblCnt; j++) {
							feeTableRef = feeExtension.getFeeTableRefAt(j);
							if (feeTableRef.getChargeMethodTC() == OLIX_CHARGERULECALCMETHOD_PCTANNPREM) {
								addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getPolicy()));
								break main;
							}
						}
					}
				}
				//end SPR2011				
			}
		}
	}
	/**
	 * Validate requested BillingOption against table OLIEXT_LU_BILLNOTOPTTYPE.  
	 * Set billing option to PolicyProductExtension.BillingNotifyOptionType if none specified. 
	 * Mandatory, optional, single premium.
	 */
	protected void process_P012() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P012");//NBA103
			if (!getPolicyExtension().hasBillingOption()) {
				PolicyProductExtension policyProductExtension = getPolicyProductExtensionForPlan();
				if (policyProductExtension == null || !policyProductExtension.hasBillingNotifyOptionType()) {
					addPlanInfoMissingMessage("PolicyProduct.BillingNotifyOptionType", getIdOf(getPolicy()));
				} else {
					getPolicyExtension().setBillingOption(policyProductExtension.getBillingNotifyOptionType() - 1000500000);
					getPolicyExtension().setActionUpdate();
				}
			}
			long billingOption = getPolicyExtension().getBillingOption();
			if (!isValidTableValue(NbaTableConstants.OLIEXT_LU_BILLOPT, billingOption)) {
				addNewSystemMessage(
					getNbaConfigValProc().getMsgcode(),
					concat("Billing Option: ", billingOption),
					getIdOf(getPolicy()));
			}
		}
	}
	/**
	 * Validate loan interest bill.  Set to "No Bill" if invalid.
	 */
	protected void process_P013() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P013");//NBA103
			if (!isValidTableValue(NbaTableConstants.OLIEXT_LU_LOANINTERESTBILL, getPolicyExtension().getLoanInterestBill())) {
				getPolicyExtension().setLoanInterestBill(Long.toString(OLIX_LOANINTBILL_NOBILL));
				getPolicyExtension().setActionUpdate();
			}
		}
	}
	/**
	 * Validate PaymentDraftDay value for the following PaymentMethods:
	 * List Bill (tc="5")
	 * Payroll Deduction (tc="6")
	 * EFT (tc="7")
	 * Government Allotment (tc="8")
	 * Credit Card (tc="9")
	 * Pre-authorized Check (tc="26") 
	 */
	protected void process_P014() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P014");//NBA103
			if (getNbaTXLife().isPaymentMethodAutoDraft()) {
				int draftDay = getPolicy().getPaymentDraftDay();
				if (draftDay < 1 || draftDay > 31) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Draft Day: ", draftDay), getIdOf(getPolicy())); //SPR2825 
				}
			}
		}
	}
	/**
	 * Validate Timing value for the following PaymentMethods:
	 * List Bill (tc="5")
	 * Payroll Deduction (tc="6")
	 * EFT (tc="7")
	 * Government Allotment (tc="8")
	 * Credit Card (tc="9")
	 * Pre-authorized Check (tc="26") 
	 */
	protected void process_P015() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P015");//NBA103
			if (getNbaTXLife().isPaymentMethodAutoDraft()) {
				long timing = getPolicyExtension().getTiming();
				if (!isValidTableValue(NbaTableConstants.OLIEXT_LU_TIMING, timing)) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Timing: ", timing), getIdOf(getPolicy()));
				}
			}
		}
	}
	/**
	 * Validate special frequency Quoted PremiumBasisFreq value when PaymentMode is one of the following:
	 * Weekly (tc="6"),
	 * Biweekly (tc="7"),
	 * 13thly (tc="12"),
	 * 9thly (tc="10"),
	 * 10thly (tc="13")  
	 */
	protected void process_P016() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P016");//NBA103
			if (getNbaTXLife().isPaymentModeSpecialFrequency()) {
				long quotedPremiumBasisFrequency = getPolicyExtension().getQuotedPremiumBasisFrequency();
				if (!isValidTableValue(NbaTableConstants.OLIEXT_LU_PREMBASFREQ, quotedPremiumBasisFrequency)) {
					addNewSystemMessage(
						getNbaConfigValProc().getMsgcode(),
						concat("Quoted Premium Basis Frequency: ", quotedPremiumBasisFrequency),
						getIdOf(getPolicy()));
				}
			}
		}
	}
	/**
	 * Validate special frequency Quoted PremiumBasisAmt value is greater than zero when PaymentMode is one of the following:
	 * Weekly (tc="6"),
	 * Biweekly (tc="7"),
	 * 13thly (tc="12"),
	 * 9thly (tc="10"),
	 * 10thly (tc="13")  
	 * Calculate the monthly premium amount based on the quoted premium amount and basis values.
	 */
	protected void process_P017() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P017");//NBA103
			if (getNbaTXLife().isPaymentModeSpecialFrequency()) {
				double quotedPremiumBasisAmt = getPolicyExtension().getQuotedPremiumBasisAmt();
				if (quotedPremiumBasisAmt > 0) {  //NBA104
					// NBA104 deleted code
					updateSpecialFrequencyPremiums(getPolicy());  //NBA104
				} else {
					addNewSystemMessage(
						getNbaConfigValProc().getMsgcode(),
						concatAmt("Quoted Premium Amount: ", quotedPremiumBasisAmt),
						getIdOf(getPolicy()));
				}
			}
		}
	}
	/**
	 * Verify special frequency Quoted PremiumBasisAmt value is greater than zero when PaymentMode is one of the following:
	 * Weekly (tc="6"),
	 * Biweekly (tc="7"),
	 * 13thly (tc="12"),
	 * 9thly (tc="10"),
	 * 10thly (tc="13")   
	 * Retrieve the MonthlyPrem based on the QuotedPremiumBasisAmt and QuotedPremiumBasisFrequency from the backend system.
	 */
	//NBA133 New Method
	protected void process_P038() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P038");
			if (getNbaTXLife().isPaymentModeSpecialFrequency()) {
				double quotedPremiumBasisAmt = getPolicyExtension().getQuotedPremiumBasisAmt();
				if (quotedPremiumBasisAmt > 0) {
				    Policy policy = getPolicy();
		            policy.setAnnualPaymentAmt(null);
		            policy.setActionUpdate();
					updateBESNonStandardModePremiums(policy);  
				} else {
					addNewSystemMessage(
						getNbaConfigValProc().getMsgcode(),
						concatAmt("Quoted Premium Amount: ", quotedPremiumBasisAmt),
						getIdOf(getPolicy()));
				}
			}
		}
	}
	/**
	 * For Credit Card billing (tc="9") PaymentMethod, set Bank AccountType to 'credit card' (tc="3").
	 * For EFT (tc="7") or Pre-authorized Check (tc=26) PaymentMethods, validate Banking.BankAcctType against table 
	 * OLI_LU_BANKACCTTYPE. If invalid, set BankAccountType to 'checking' (tc="2").
	 */
	protected void process_P018() {
		if (verifyCtl(BANKING)) {
			logDebug("Performing NbaValBilling.process_P018 for" , getBanking());//NBA103
			if (isNotBankingForCreditCardPayment()) { //Bypass credit card payments //SPR3573
				long paymentMethod = getNbaTXLife().getPaymentMethod();
				if (paymentMethod == OLI_PAYMETH_CREDCARD) {
					getBanking().setBankAcctType(OLI_BANKACCT_CREDCARD);
				} else if (paymentMethod == OLI_PAYMETH_PAC || paymentMethod == OLI_PAYMETH_ETRANS) {
					if (!(getBanking().getBankAcctType() == OLI_BANKACCT_CHECKING ||  getBanking().getBankAcctType() == OLI_BANKACCT_SAVINGS)) {	//SPR2151
						getBanking().setBankAcctType(OLI_BANKACCT_CHECKING);
					}
				}
			}	//SPR3573
		}
	}
	/**
	 * If PaymentMethod is EFT (tc="7"), Pre-authorized Check (tc="26")  or Credit Card billing (tc="9") 
	 * verify that AccountNumber is present and contains 16 numeric digits.
	 */
	protected void process_P019() {
		if (verifyCtl(BANKING)) {
			logDebug("Performing NbaValBilling.process_P019 for" ,  getBanking()); //NBA103
			if (isNotBankingForCreditCardPayment()) { //Bypass credit card payments //SPR3573
				long paymentMethod = getNbaTXLife().getPaymentMethod();
				if (NbaUtils.isPaymentMethodBanking(paymentMethod)) {	//SPR2151
					//begin SPR2049
					String accountNumber = accountNumber = "Missing";
					if (getBanking().hasAccountNumber()) {
						accountNumber = getBanking().getAccountNumber().trim();
					}
					//end SPR2049				
					//begin SPR2151
					if (NbaUtils.selectDigits(accountNumber).length() != accountNumber.length())  {	//Verify numeric  
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat(ACCOUNT_NUMBER, accountNumber), getIdOf(getBanking()));
					} else if (NbaUtils.isPaymentMethodCreditCard(paymentMethod)) {	
						if (getBanking().hasCreditCardType()) {	//Skip edit if type not present
							editCreditCardNumber(accountNumber);
						}
					}
					//end SPR2151
				}
			}	//SPR3573
		}
	}
	/** 
	 * If PaymentMethod is EFT (tc="7"), Pre-authorized Check (tc="26")  or Credit Card billing (tc="9")  
	 * set BillControlEffDate to system date if none present.
	 */
	protected void process_P020() {
		if (verifyCtl(BANKING)) {
			logDebug("Performing NbaValBilling.process_P020 for" , getBanking());//NBA103
			if (isNotBankingForCreditCardPayment()) { //Bypass credit card payments //SPR3573
				long paymentMethod = getNbaTXLife().getPaymentMethod();
				if (NbaUtils.isPaymentMethodBanking(paymentMethod)) {		//SPR2151
					BankingExtension bankingExtension = NbaUtils.getFirstBankingExtension(getBanking());	//SPR2151
					if (bankingExtension == null) {
						OLifEExtension oLifEExtension = NbaTXLife.createOLifEExtension(EXTCODE_BANKING);
						oLifEExtension.setActionAdd();
						banking.addOLifEExtension(oLifEExtension);
						bankingExtension = NbaUtils.getFirstBankingExtension(getBanking());	//SPR2151
					}
					if (!bankingExtension.hasBillControlEffDate()) {
						bankingExtension.setBillControlEffDate(getCurrentDate());
						bankingExtension.setActionUpdate();
					}
				}
			}	//SPR3573
		}
	}
	/**
	 * If PaymentMethod is credit card billing (tc="9") validate CreditCardType against table OLI_LU_CREDCARDTYPE
	 */
	protected void process_P023() {
		if (verifyCtl(BANKING)) {
			logDebug("Performing NbaValBilling.process_P023 for" ,  getBanking());//NBA103
			if (isNotBankingForCreditCardPayment()) { //Bypass credit card payments //SPR3573
				if (NbaUtils.isPaymentMethodCreditCard(getNbaTXLife().getPaymentMethod())) {	//SPR2151
					long creditCardType = getBanking().getCreditCardType();
					if (!isValidTableValue(NbaTableConstants.OLI_LU_CREDCARDTYPE, creditCardType)) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Credit Card Type: ", creditCardType), getIdOf(getBanking()));
					}
				}
			}	//SPR3573
		}
	}
	/**
	 * If PaymentMethod is credit card billing (tc="9"), validate that the credit card expiration date is present 
	 * and not less than the current date.  
	 */
	protected void process_P024() { 
		if (verifyCtl(BANKING)) {
			logDebug("Performing NbaValBilling.process_P025 for" ,  getBanking());//NBA103	SPR2151
			if (isNotBankingForCreditCardPayment()) { //Bypass credit card payments //SPR3573
				if (getNbaTXLife().getPaymentMethod() == OLI_PAYMETH_CREDCARD) {
					if (!getBanking().hasCreditCardExpDate()
						|| NbaUtils.compareYymmToDate(getBanking().getCreditCardExpDate(), Calendar.getInstance()) < 0) {//SPR2074
						addNewSystemMessage(
							getNbaConfigValProc().getMsgcode(),
							concat("Expiration Date: ", getBanking().getCreditCardExpDate()),	//SPR2074
							getIdOf(getBanking()));
					}
				}
				// SPR2151 was wrongly retrofitted in this process.
			} //SPR3573
		}
	}
	/**
	 * When a RoutingNum is present, invoke web services to perform  RoutingNum (routing transit number) 
	 * validation against the backend billing system.
	 */
	protected void process_P025() {
		if (verifyCtl(BANKING)) {
			logDebug("Performing NbaValBilling.process_P025 for" ,  getBanking());//NBA103
			if (isNotBankingForCreditCardPayment()) { //Bypass credit card payments //SPR3573
				//begin SPR2151
				if (NbaUtils.isPaymentMethodEFTorPAC(getNbaTXLife().getPaymentMethod())) { //EFT or PAC //SPR2151
					if (!getBanking().hasRoutingNum()) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getBanking()));
					} else {
						String number = getBanking().getRoutingNum();
						if (NbaUtils.selectDigits(number).length() != number.length() || number.length() != 9) {
							addNewSystemMessage(getNbaConfigValProc().getMsgcode(), number, getIdOf(getBanking()));
						}
					}
				}
				//end SPR2151
			}//SPR3573
		}
	}
	/**
	 * If the PaymentMode is one of the following:
	 * Weekly (tc="6"),
	 * Biweekly (tc="7"),
	 * 13thly (tc="12"),
	 * 9thly (tc="10"),
	 * 10thly (tc="13")
	 * and the NonStandardPaidToDate is not set, set the NonStandardPaidToDate equal to BilledToDate.
	 */
	// NBA104 New Method
	protected void process_P027() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P027");//NBA103
			if (getNbaTXLife().isPaymentModeSpecialFrequency() && !getPolicyExtension().hasNonStandardPaidToDate()) {
				getPolicyExtension().setNonStandardPaidToDate(getPolicy().getBilledToDate());
				getPolicyExtension().setActionUpdate();
			}
		}
	}
	/**
	 * Initialize PaidToDate to the EffDate. 
	 */
	// NBA104 New Method
	protected void process_P028() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P028()");//NBA103
			if (getPolicy().hasEffDate()) {
				getPolicy().setPaidToDate(getPolicy().getEffDate());
			}
		}
	}
	/**
	 * Verify that Coverage.AnnualPremAmt is not less than minimum annual premium amount.
	 * AnnualPremAmt is compared against UnderwritingClassProductExtension.MinIssuePremAmt.
	 */
	// SPR1996 New Method
	protected void process_P029() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValBilling.process_P029() for" ,  getCoverage());//NBA103
			if (getCoverage().hasAnnualPremAmt()) {
				UnderwritingClassProduct underwritingClassProduct = getUnderwritingClassProductFor(getCoverage());
				if (underwritingClassProduct != null) {
					UnderwritingClassProductExtension ext = getUnderwritingClassProductExtensionFor(underwritingClassProduct);
					if (ext != null && ext.hasMinIssuePremAmt()) {
						double min = ext.getMinIssuePremAmt();
						double amt = getCoverage().getAnnualPremAmt();
						if (amt < min) {
							addNewSystemMessage(
								getNbaConfigValProc().getMsgcode(),
								concatAmt("Annual Premium: ", amt, ", Minimum: ", min),
								getIdOf(getCoverage()));
						}
					}
				}
			}
		}
	}
	/**
	 * Verify that Coverage.AnnualPremAmt is not greater than maximum annual premium amount.
	 * AnnualPremAmt is compared against UnderwritingClassProductExtension.MinIssuePremAmt.
	 */
	// SPR1996 New Method
	protected void process_P030() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValBilling.process_P030() for" ,  getCoverage());//NBA103
			if (getCoverage().hasAnnualPremAmt()) {
				UnderwritingClassProduct underwritingClassProduct = getUnderwritingClassProductFor(getCoverage());
				if (underwritingClassProduct != null) {
					UnderwritingClassProductExtension ext = getUnderwritingClassProductExtensionFor(underwritingClassProduct);
					if (ext != null && ext.hasMaxIssuePremAmt() && ext.getMaxIssuePremAmt() > 0) {
						double max = ext.getMaxIssuePremAmt();
						double amt = getCoverage().getAnnualPremAmt();
						if (amt > max) {
							addNewSystemMessage(
								getNbaConfigValProc().getMsgcode(),
								concatAmt("Annual Premium: ", amt, ", Maximum: ", max),
								getIdOf(getCoverage()));
						}
					}
				}
			}
		}
	}
	
	/**
	 * Verify Holding.Policy.LastNoticeDate and Holding.Policy.LastNoticeType are present
	 */
	//NBA077 New Method
	protected void process_P031() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P031");//NBA103
			Policy policy = getPolicy();
			if (!policy.hasLastNoticeDate() || !isValidTableValue(NbaTableConstants.OLI_LU_NOTICETYPE, policy.getLastNoticeType())) {
				addNewSystemMessage(
					getNbaConfigValProc().getMsgcode(),
					concat("Last Billing Date: ", policy.getLastNoticeDate(), ", Last Billing Kind", policy.getLastNoticeType()),
					getIdOf(getPolicy()));
			}
		}
	}
	
	/**
	 * Verify Holding.Policy.PaidToDate is present.
	 */
	//NBA077 New Method
	protected void process_P032() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P032");//NBA103
			if (!getPolicy().hasPaidToDate() ) {
				addNewSystemMessage(
					getNbaConfigValProc().getMsgcode(),
					concat("Paid to Date: ", getPolicy().getPaidToDate()),
					getIdOf(getPolicy()));
			}
		}
	}
	
	/**
	 * Verify that day in paid to and billed to dates equal basic coverage issue day.  
	 * If not, adjust to basic coverage issue day.
	 */
	//NBA077 New Method
	protected void process_P033() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P033");//NBA103
			Coverage primaryCov = getNbaTXLife().getPrimaryCoverage();
			Policy policy = getPolicy();
			if (policy.hasPaidToDate()) {
				int months = NbaUtils.calcMonthsDiff(policy.getPaidToDate(), primaryCov.getEffDate());
				policy.setPaidToDate(NbaUtils.calcDayFotFutureDate(primaryCov.getEffDate(), months));
				policy.setActionUpdate();
			}
			if (policy.hasBilledToDate()) {
				int months = NbaUtils.calcMonthsDiff(policy.getBilledToDate(), primaryCov.getEffDate());
				policy.setBilledToDate(NbaUtils.calcDayFotFutureDate(primaryCov.getEffDate(), months));
				policy.setActionUpdate();			
			}
		}
	}
	
	/**
	 * Verify non new-application BilledToDate and PaidToDate are equal OR  BilledToDate and Next Anniversary Date 
	 * are equal if required by billing form.  Model returns 1 - True (they are equal or it doesn't matter for 
	 * billing form)   0 - False  (they are unequal if required to be equal by billing form).  If model returns false,
	 * adjust the billed-to date automatically to be equal to the paid-to date  OR next anniversary date
	 */
	//NBA077 New Method
	protected void process_P034() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P034");//NBA103
			Policy policy = getPolicy();
			if (policy.hasBilledToDate()) {
				NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation();
				if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
					int ind = Integer.parseInt((String) nbaVpmsResultsData.getResultsData().get(0));
					
					if (ind == 1) {
						policy.setBilledToDate(policy.getPaidToDate());
					}else if (ind == 2) {
						policy.setBilledToDate(NbaUtils.calcDayFotFutureDate(getHolding().getLastAnniversaryDate(), 12));
					}
				}
			}
		}
	}	
	   
	/**
	 * Verify that billed to date is valid.  Can not be prior to paid to date and must be a 
	 * premium due date (the paid-to date plus an integral number of frequencies) and can not 
	 * be during the skip months if special frequency mode of ninthly or tenthly. 
	 * If invalid, adjust the billed-to date automatically to be equal to the paid-to date or the 
	 * next valid date greater than the paid to date for special frequency.
	 */
	//NBA077 New Method
	protected void process_P035() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P035");//NBA103
			Policy policy = getPolicy();
			if (policy.hasPaidToDate() && policy.hasPaymentMode()) {
				int payMode = (int) policy.getPaymentMode();
				if (!policy.hasBilledToDate() || policy.getBilledToDate().before(policy.getPaidToDate())) {
					policy.setBilledToDate(policy.getPaidToDate());
				}
				int months = NbaUtils.calcMonthsDiff(policy.getBilledToDate(), policy.getPaidToDate());
				// SPR3290 code deleted
				if ((payMode == OLI_PAYMODE_ANNUAL) || (payMode == OLI_PAYMODE_BIANNUAL)) {
					if (months % (12 / payMode) != 0) { // not an integral number of frequencies
						policy.setBilledToDate(policy.getPaidToDate());
					}
				} else if (payMode == OLI_PAYMODE_QUARTLY){
					if (months % 3 != 0) { // not an integral number of frequencies for quarterly payment mode
						policy.setBilledToDate(policy.getPaidToDate());
					}					
				} else if ((payMode == OLI_PAYMODE_MNTH49) || (payMode == OLI_PAYMODE_MNTH410)) {
					PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(policy);
					if (polExt != null && polExt.hasFirstSkipMonth()) {
						Calendar cal = new GregorianCalendar();
						cal.setTime(policy.getBilledToDate());
						int billedToMonth = cal.get(Calendar.MONTH) + 1; //since cal.month returns the value in index form i.e.Jan=0,feb=1 and so on. 
						cal.setTime(policy.getPaidToDate());
						int paidToMonth = cal.get(Calendar.MONTH) + 1; //since cal.month returns the value in index form i.e.Jan=0,feb=1 and so on.
						int minMonth = 0;
						minMonth = Integer.parseInt(polExt.getFirstSkipMonth());
						int maxMonth = 0;
						if (payMode == OLI_PAYMODE_MNTH49) {
							maxMonth = minMonth + 2;
						} else {
							maxMonth = minMonth + 1;
						}
						if (maxMonth > 12) {
							billedToMonth = billedToMonth + 12;
						}
						if (!((billedToMonth < minMonth) || (billedToMonth > maxMonth))) { // billed to within skipped months
							if ((paidToMonth < minMonth) || (paidToMonth > maxMonth)) {
								policy.setBilledToDate(policy.getPaidToDate()); // paid to not within skipped months
							} else {
								cal.add(Calendar.MONTH, maxMonth - paidToMonth + 1); // paid to within skipped months
								policy.setBilledToDate(cal.getTime());
							}
						}
					}
				}
			}
		}
	}
	/**
	 * Verify that day in paid to and billed to dates equal basic contract issue day.
	 * If not, adjust to contract issue day.
	 */
	//NBA077 New Method
	protected void process_P036() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBilling.process_P036");
			Policy policy = getPolicy();
			if (policy.hasPaidToDate()) {
				int months = NbaUtils.calcMonthsDiff(policy.getPaidToDate(), policy.getEffDate());
				policy.setPaidToDate(NbaUtils.calcDayFotFutureDate(policy.getEffDate(), months));
				policy.setActionUpdate();
			}
			if (policy.hasBilledToDate()) {
				int months = NbaUtils.calcMonthsDiff(policy.getBilledToDate(), policy.getEffDate());
				policy.setBilledToDate(NbaUtils.calcDayFotFutureDate(policy.getEffDate(), months));
				policy.setActionUpdate();
			}
		}
	}
	
	/**
	 * Retrieve the annualized premium amount for coverage from backend.
	 * Holding.Policy.Life.Coverage.AnnualPremAmt
	 */
	//NBA133 New Method
	protected void process_P039() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValBilling.process_P039 for " ,  getCoverage());
			Coverage coverage = getCoverage();
			coverage.setAnnualPremAmt(null);
            coverage.setActionUpdate();
			updateBackendPremiums(coverage);
		}
	}
	/**
	 * Retrieve the annualized premium amount for coverage option. Holding.Policy.Life.Coverage.CovOption.AnnualPremiumAmount
	 */
	//NBA133 New Method
	protected void process_P040() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValBilling.process_P040 for" ,  getCovOption());
			getCovOption().setAnnualPremAmt(null);
			getCovOption().setActionUpdate();
			updateBackendPremiums(getCovOption());
		}
	}
	/**
	 * Retrieve the annualized premium amount for rider.Holding.Policy.Annuity.Rider.AnnualPremAmt
	 */
	//NBA133 New Method
	protected void process_P041() {
		if (verifyCtl(RIDER)) {
			logDebug("Performing NbaValBilling.process_P041 for" ,  getRider());
			//This is reserved for future use and the ground work for the method has been added 
			//Will uncomment following line when support for term riders on annuities is fully functional
			//updateBackendPremiums(getRider()); for future support
		}
	}
	/**
	 * Retrieve the annualized premium amount for coverage option. Holding.Policy.Annuity.Rider.CovOption.AnnualPremiumAmount
	 */
	//NBA133 New Method
	protected void process_P042() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValBilling.process_P042 for" ,  getCovOption());
			//This is reserved for future use and the ground work for the method has been added 
			//Will uncomment following line when support for benefits on annuities is fully functional
			//updateBackendPremiums(getCovOption());
		}
	}
	/**
	 * Retrieve, the annualized premium amount for the substandard rating. Holding.Policy.Life.Coverage.CovOption.SubstandardRating.AnnualPremAmount
	 */
	//NBA133 New Method
	protected void process_P043() {
		if (verifyCtl(SUBSTANDARDRATING)) {
			logDebug("Performing NbaValBilling.process_P043 for" ,  getSubstandardRating());
			getSubstandardRatingExtension().setAnnualPremAmt(null);
            getSubstandardRatingExtension().setActionUpdate();
			updateBackendPremiums(getSubstandardRating());
		}
	}
	/**
	 * Call a VPMS model to edit a credit card number.
	 * @param accountNumber - credit card number
	 */
	//SPR2151 New Method
	protected void editCreditCardNumber(String accountNumber) {
		Map skipAttributes = new HashMap();
		skipAttributes.put(A_CCNUMBERLOB, accountNumber);
		skipAttributes.put(A_CCTYPELOB, Long.toString(getBanking().getCreditCardType()));
		NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(skipAttributes);
		if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
			List rulesList = nbaVpmsResultsData.getResultsData();
			if (!rulesList.isEmpty()) {
				String xmlString = (String) rulesList.get(0);
				NbaVpmsModelResult nbaVpmsModelResult;
				try {
					nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
					VpmsModelResult vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult(); 
					Iterator itr = vpmsModelResult.getStandardAttr().iterator();
					while (itr.hasNext()) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat(
								concat(((StandardAttr) itr.next()).getAttrValue(), " "),  NbaUtils.maskCreditCardNumber(accountNumber)), getIdOf(getBanking()));
					}
				} catch (NbaVpmsException e) {
					addNewSystemMessage(INVALID_VPMS_CALC, concat(getNbaConfigValProc().getId(), DOT,
							getNbaConfigValProc().getModel(), DOT).concat(getNbaConfigValProc().getEntrypoint()), getIdOf(getBanking()));
				}
			}
		}
	}
	/**
	 * For a Credit Card, EFT or PAC billing (tc="9", "7", or "26") Payment Method,  verify that there is a Banking
	 * object for Billing.
	 */
	//SPR2151 New Method
	protected void process_P021() {
		if (verifyCtl(HOLDING)) {
			logDebug("Performing NbaValBilling.process_P021");
			if (NbaUtils.isPaymentMethodBanking(getNbaTXLife().getPaymentMethod())) {
				Banking banking = NbaUtils.getBanking(getOLifE(), OLI_HOLDTYPE_BANKING);
				boolean bankingPresent = false;
				if (banking!= null){
					bankingPresent = !NbaUtils.isCreditCardPayment(banking);
				}
				if (!bankingPresent) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getHolding()));
				}
			}
		}
	}
	/**
	 * For a Credit Card, EFT or PAC billing (tc="9", "7", or "26") Payment Method,  verify that there is an
	 * Account Holder name.
	 */
	//SPR2151 New Method
	protected void process_P022() {
		if (verifyCtl(BANKING)) {
			logDebug("Performing NbaValBilling.process_P022 for", getBanking());
			if (isNotBankingForCreditCardPayment()) { //Bypass credit card payments  
				if (NbaUtils.isPaymentMethodBanking(getNbaTXLife().getPaymentMethod())) {
					boolean nameFound = false;
					BankingExtension bankingExtension = NbaUtils.getFirstBankingExtension(getBanking());
					if (bankingExtension != null && bankingExtension.hasAccountHolderNameCC()) {
						AccountHolderNameCC acntHolderCC = bankingExtension.getAccountHolderNameCC();
						nameFound = acntHolderCC.getAccountHolderNameCount() > 0 && acntHolderCC.getAccountHolderNameAt(0).length() > 0;
					}
					if (!nameFound) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getBanking()));
					}
				}
			}
		}
	}
}
