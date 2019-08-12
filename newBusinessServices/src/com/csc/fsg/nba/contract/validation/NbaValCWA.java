package com.csc.fsg.nba.contract.validation;
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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.ValProc;
import com.csc.fsg.nba.vo.txlife.AccountingActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
/**
 * NbaValCWA performs Cash With Application validation.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA064</td><td>Version 3</td><td>Contract Validation</td></tr>
 * <tr><td>NBA066</td><td>Version 3</td><td>Accounting and Disbursements Extracts<td></tr>
 * <tr><td>SPR1748</td><td>Version 4</td><td>Correct over/short logic<td></tr>
 * <tr><td>SPR1748</td><td>Version 4</td><td>Set the value of Holding.Policy.PolicyExtension.ContractChangeType.<td></tr>
 * <tr><td>SPR1804</td><td>Version 4</td><td>Billed to date should be set to paid to date.</td></tr> 
 * <tr><td>SPR1800</td><td>Version 4</td><td>Effective and Issue date usage is incorrect.</td></tr>
 * <tr><td>SPR1799</td><td>Version 4</td><td>Debit and Credit account numbers are not assigned to AccountingActivity during validation.</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change</td></tr>
 * <tr><td>SPR1994</td><td>Version 4</td><td>Correct user validation example </td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>SPR1876</td><td>Version 4</td><td>P001 process is not updating the CWA total LOB</td></tr>
 * <tr><td>SPR1234</td><td>Version 4</td><td>General source code clean up </td></tr>
 * <tr><td>SPR1762</td><td>Version 4</td><td>CWA over short logic is displaying Shortage even when CWA received is in excess of Premium Amount</td></tr>
 * <tr><td>SPR2042</td><td>Version 4</td><td>Paid to date is set incorrectly for Fixed premium policies</td></tr>
 * <tr><td>SPR2031</td><td>Version 4</td><td>CWA Validation (P005) create system message # 5003 even if initial CWA is equal to planned initial premium amount</td></tr>
 * <tr><td>SPR2817</td><td>Version 6</td><td>Pending Accounting Needs to Be Added to nbA</td>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR3562</td><td>Version 8</td><td>Premium Amounts Larger than Seven Whole Numbers and Two Decimals Are Not Handled Correctly</td></tr>
 * <tr><td>AXAL3.7.18</td><td>AXA Life Phase 1</td><td>Producer Interfaces</td></tr>
 * <tr><td>NBA228</td><td>Version 8</td><td>Cash Management Enhancement</td></tr> 
 * <tr><td>SPR2125</td><td>Version 8</td><td>Contract Validation Should Set Holding.Policy.FinancialActivity.FinActivityDate to Current System Date if Not Already Set</td></tr>
 * <tr><td>NBA237</td><td>Version 8</td><td>Migrate Policy Product Transmittal XML1201 to 2.15.00</td></tr>
 * <tr><td>P2AXAL019</td><td>AXA Life Phase 2</td><td>Cash Management</td></tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaValCWA
	extends NbaContractValidationCommon
	implements NbaContractValidationBaseImpl, NbaContractValidationImpl {	//SPR1234
	protected double required = 0;	//SPR1748
	protected double actual = 0;	//SPR1748
	protected double diff = 0;	//SPR1748
	/**
	 * Perform one time initialization.
	 */
	 //NBA237 changed method signature
	public void initialze(NbaDst nbaDst, NbaTXLife nbaTXLife, Integer subset, NbaOLifEId nbaOLifEId, AccelProduct nbaProduct, NbaUserVO userVO) { //AXAL3.7.18
		super.initialze(nbaDst, nbaTXLife, subset, nbaOLifEId, nbaProduct, userVO); //AXAL3.7.18
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
		} else { //ALS2600
		    if (getUserImplementation() != null) {
		        getUserImplementation().validate(nbaConfigValProc, objects);
		    }
		}//ALS2600    
	}
	/**
	 * Determines total CWA amount that accompanied the application
	 */
	protected void process_P001() {
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValCWA.process_P001()"); //NBA103
			double totalCWA = 0;
			long finActivitySubType = 0; //SPR1876
			int finCount = getPolicy().getFinancialActivityCount();
			for (int i = 0; i < finCount; i++) {
				FinancialActivity financialActivity = getPolicy().getFinancialActivityAt(i);
				String finActivityType = Long.toString(financialActivity.getFinActivityType());
				finActivitySubType = financialActivity.getFinActivitySubType(); //SPR1876
				boolean paymentAddition = false;
				boolean paymentSubstraction = false; //NBLXA-1457
				for (int j = 0; j < CWA_PAYMENT_ADDITIONS.length; j++) {
					if (finActivityType.equals(CWA_PAYMENT_ADDITIONS[j])) {
						paymentAddition = true;
						break;
					}
				}
				if (paymentAddition) {
					FinancialActivityExtension financialActivityExtension = NbaUtils.getFirstFinancialActivityExtension(financialActivity);
					if (financialActivityExtension != null) {
						if (financialActivityExtension.getDisbursedInd()) {
							paymentAddition = false;
						}
					}
					if (OLI_FINACTSUB_REV == finActivitySubType
						|| OLI_FINACTSUB_REFUND == finActivitySubType
						|| OLI_FINACTSUB_PARTIALREFUND == finActivitySubType) { //SPR1876
						paymentAddition = false; //SPR1876
						paymentSubstraction = false; //NBLXA-1457
					} //SPR1876
					
					// Begin NBLXA-1457
					if(OLI_FINACT_PVT315 == financialActivity.getFinActivityType()) {
						paymentSubstraction = true;
						paymentAddition = false;
					}
					// End NBLXA-1457
					
				}
				if (paymentAddition) {
					totalCWA = (double) Math.round((totalCWA + financialActivity.getFinActivityGrossAmt()) * 100) / 100; // SPR1876,ALII2039, APSL3543

				}
				// Begin NBLXA-1457
				if (paymentSubstraction) {
					totalCWA = (double) Math.round((totalCWA - financialActivity.getFinActivityGrossAmt()) * 100) / 100; // SPR1876,ALII2039, APSL3543

				}
				// End NBLXA-1457				
			}
			getApplicationInfo().setCWAAmt(totalCWA);
			getApplicationInfo().setActionUpdate();
		}
	}
	/**
	 * Verifies that a mode premium amount has been calculated in order to determine CWA required.
	 */
	protected void process_P002() {
		logDebug("Performing NbaValCWA.process_P002()"); //NBA103
		if (!getPolicy().hasPaymentAmt()) {
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getPolicy()));
		}
	}
	/**
	 * Verifies that CWA amount meets minimum limits for billing type and frequency based on company rules.
	 */
	protected void process_P003() {
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValCWA.process_P003()"); //NBA103
			NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(); //Get the  limit
			if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
				if (nbaVpmsResultsData.getResultsData().size() == 2) {
					String strMin = (String) nbaVpmsResultsData.getResultsData().get(1);
					try {
						double min = Double.parseDouble(strMin);
						double diff = min - getApplicationInfo().getCWAAmt();
						if (diff > 0) {
							addNewSystemMessage(
								getNbaConfigValProc().getMsgcode(),
								concatAmt("Premium: ", min)
									+ concatAmt(", Received: ", getApplicationInfo().getCWAAmt())
									+ concatAmt(", Difference: ", diff),
								getIdOf(getApplicationInfo()));
						}
					} catch (Throwable e) {
						addNewSystemMessage(INVALID_VPMS_CALC, concat("Process: ", getNbaConfigValProc().getId(), " Invalid amount: ", strMin), getIdOf(getApplicationInfo()));
					}
				} else {
					addUnexpectedVpmsResultMessage(2, nbaVpmsResultsData.getResultsData().size());
				}
			}
		}
	}
	/**
	 * Verify that CWA amount within overage limit based on company rules.
	 * SR545390
	 * Modify the existing Contract Validation Process to verify that CWA amount received is sufficeint to meet the total premiums due including
	 * the back due premiums as on current date (Sum of the modal premiums that are due as on current date ). If the overage is not within the
	 * retention limits, then generate a validation error message for the total overage as on current date.
	 */
	//SR545390 Method refactored
	protected void process_P004() {
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValCWA.process_P004()"); //NBA103			
			NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(); //Get the overage amount + overage value
			if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
				if (nbaVpmsResultsData.getResultsData().size() == 1) {
					String overageAmtString = (String) nbaVpmsResultsData.getResultsData().get(0);
					try {
						if(!NbaUtils.isBlankOrNull(overageAmtString)){
							String[] overageAmtArray = overageAmtString.split("@@");
							double overageAmt = Double.parseDouble(overageAmtArray[0]);
							double overageValue = Double.parseDouble(overageAmtArray[1]);
							assignPremBalDue(overageAmt, overageValue, false); // ALS4610
							// NBLXA-1643 code deleted
						}						
					} catch (Throwable e) {
						addNewSystemMessage(
							INVALID_VPMS_CALC,
							concat("Process: ", getNbaConfigValProc().getId(), " Invalid amount: ", overageAmtString),
							getIdOf(getApplicationInfo()));
					}
				} else {
					addUnexpectedVpmsResultMessage(1, nbaVpmsResultsData.getResultsData().size());
				}
			}		
		}
	}
	/**
	 * Verify that CWA amount within shortage limit based on company rules.. 
	 * SR545390  
	 * Modify the existing Contract Validation Process to verify that CWA amount
	 * received is sufficeint to meet the total premiums due including the back due premiums as on current date (Sum of the modal premiums that are
	 * due as on current date ). If the shortage is not within the allowable limits, then generate a validation error message for the total shortage
	 * as on current date.
	 *  ( This is CV is reachble until the next premium due date that falls on after contract print )
	 */
	//SR545390 Method refactored NBLXA-1256
	protected void process_P009() {
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValCWA.process_P009()"); //NBA103
			Date initialPaymentDueDate = null;
			PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(getPolicy());
			if(polExt != null){
				initialPaymentDueDate = polExt.getInitialPaymentDueDate();
			}
			if(NbaUtils.isBlankOrNull(initialPaymentDueDate) || !getCurrentDate().after(initialPaymentDueDate)){
				NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(); //Get the due Premium + Shortage Amount
				if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
					if (nbaVpmsResultsData.getResultsData().size() == 1) {
						String duePremiumString = (String) nbaVpmsResultsData.getResultsData().get(0);
						try {
							if (!NbaUtils.isBlankOrNull(duePremiumString)) {
								String[] duePremiumArray = duePremiumString.split("@@");
								double premiumDue = Double.parseDouble(duePremiumArray[0]);
								double shortageAmt = Double.parseDouble(duePremiumArray[1]);
								getApplicationInfo().setPremBalDue(premiumDue);
								getApplicationInfo().setActionUpdate();
								if (premiumDue > 0) {
									addNewSystemMessage(getNbaConfigValProc().getMsgcode(), 
											concat("Update Financial menu for overage return or shortage amount greater than tolerance. ",
											concatAmt("Sum of Modal Premiums: ", premiumDue
											+ getApplicationInfo().getCWAAmt())
											+ concatAmt(", Received: ", getApplicationInfo().getCWAAmt())
											+ concatAmt(", Shortage: ", premiumDue)), getIdOf(getApplicationInfo()));
								}
							}
						} catch (Throwable e) {
							addNewSystemMessage(INVALID_VPMS_CALC, concat("Process: ", getNbaConfigValProc().getId(), " Invalid amount: ",
									duePremiumString), getIdOf(getApplicationInfo()));
						}
					} else {
						addUnexpectedVpmsResultMessage(1, nbaVpmsResultsData.getResultsData().size());
					}
				}
			}			
		}
	}
	
	//P2AXAL019 Method is removed and moved to nbacontractvalidationcommon.java
	/**
	 * Validate that the total amounts of initial CWA (FinActivityType = "7" or "210")
	 * is not less than the planned initial premium amount.
	 */
	protected void process_P005() {
		if (verifyCtl(LIFE)) {
			logDebug("Performing NbaValCWA.process_P005()"); //NBA103
			double totalInitialCWA = 0;
			boolean addMessage = false; //QC8980
			int finCount = getPolicy().getFinancialActivityCount();
			for (int i = 0; i < finCount; i++) {
				FinancialActivity financialActivity = getPolicy().getFinancialActivityAt(i);
				long finActivityType = financialActivity.getFinActivityType();
				if (finActivityType == NbaOliConstants.OLI_FINACT_PREMIUMINIT || finActivityType == NbaOliConstants.OLI_FINACT_1035INIT) {
					totalInitialCWA += financialActivity.getFinActivityGrossAmt(); //SPR2031
					addMessage = true; //QC8980
				}
			}
			double plannedAmt = getLife().getInitialPremAmt(); //SPR2031
			if (addMessage && plannedAmt > totalInitialCWA) { //QC8980
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatAmt("Planned initial: ", plannedAmt, ", Total initial: ",
						totalInitialCWA), getIdOf(getPolicy()));
			}
		}
	}
	/**
	 * Validate that the total amounts of additional CWA (FinActivityType = "9", "197", "211" or "230") is not less than the planned additional
	 * premium amount.
	 */
	protected void process_P006() {
		logDebug("Performing NbaValCWA.process_P006()"); //NBA103
		double totalAddlCWA = 0;
		int finCount = getPolicy().getFinancialActivityCount();
		for (int i = 0; i < finCount; i++) {
			FinancialActivity financialActivity = getPolicy().getFinancialActivityAt(i);
			long finActivityType = financialActivity.getFinActivityType();
			if (finActivityType == NbaOliConstants.OLI_FINACT_PREMIUMIADDSIN
				|| finActivityType == NbaOliConstants.OLI_FINACT_ROLLOVEREXT1035S
				|| finActivityType == NbaOliConstants.OLI_FINACT_1035SUBS 	//SPR1876
				|| finActivityType == NbaOliConstants.OLI_FINACT_ADDTL) {
				totalAddlCWA = +financialActivity.getFinActivityGrossAmt();
			}
		}
		if (getPolicyExtension().getPlannedAdditionalPremium() > totalAddlCWA) {
			addNewSystemMessage(
				getNbaConfigValProc().getMsgcode(),
				concatAmt("Planned additional: ", getPolicyExtension().getPlannedAdditionalPremium(), ", Total additional: ", totalAddlCWA),
				getIdOf(getPolicy()));
		}
	}
	/**
	 * Calculate BilledToDate of contract based on number of modes of CWA remitted.
	 * Set PaidToDate to policy effective Date 
	 */
	protected void process_P007() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValCWA.process_P007()"); //NBA103
			//begin SPR1800
			if (getPolicy().hasEffDate()) {
				getPolicy().setPaidToDate(getPolicy().getEffDate());
				getPolicy().setBilledToDate(getPolicy().getPaidToDate()); //SPR2042
				
				if ((getApplicationInfo().hasCWAAmt() && getApplicationInfo().getCWAAmt() > 0)
					&& (getPolicy().hasPaymentAmt() && getPolicy().getPaymentAmt() > 0))
				{ //SPR2042
					long period =  getPolicy().getPaymentMode(); //SPR3562
					int numModes = (int) (getApplicationInfo().getCWAAmt() / getPolicy().getPaymentAmt());
					//Begin APSL780 Check if received payment is with in the shortage limit
					double diffAmt = NbaUtils.getPaymentDiff(getApplicationInfo().getCWAAmt(), getPolicy().getPaymentAmt());
					if (diffAmt < 0) {
						diffAmt = Math.abs(diffAmt);
						getNbaConfigValProc().setEntrypoint(NbaVpmsConstants.EP_CWA_SHORTAGE_LIMIT);
						getNbaConfigValProc().setModel(NbaVpmsConstants.CONTRACTVALIDATIONCOMPANYCONSTANTS);
						NbaVpmsResultsData vpmsResult = performVpmsCalculation(); //Get the limit
						if (vpmsResult != null && vpmsResult.wasSuccessful() && vpmsResult.getResultsData().size() == 1) {
							String shrtgLmt = (String) vpmsResult.getResultsData().get(0);
							try {
								double overShrt = Double.parseDouble(shrtgLmt);
								//APSL2895  Paid To Date is set to next when there is a waiver allowed on an ADC case
								double waiveAmt = getWaiverAmount();
								if ((NbaUtils.isAdcApplication(getNbaDst()) && (diffAmt <= (overShrt+waiveAmt))) || (diffAmt <= overShrt)) { //with in the shortage limit
									numModes++; //increase the mode by 1
								}
							} catch (Throwable e) {
								addNewSystemMessage(INVALID_VPMS_CALC, concat("Process: ", getNbaConfigValProc().getId(), " Invalid amount: ",
										shrtgLmt), getIdOf(getPolicy()));
							}
						} else {
							addUnexpectedVpmsResultMessage(1, vpmsResult.getResultsData().size());
						}
					}
					//End APSL780
					getPolicy().setBilledToDate(addPeriod(getPolicy().getBilledToDate(),  period,  numModes)); //SPR3562
					getPolicy().setPaidToDate(getPolicy().getBilledToDate());//ALS3196
					getPolicy().setPaymentDueDate(getPolicy().getPaidToDate());//APSL2482
					//SPR2042 Code deleted
				} //SPR2042
				getPolicy().setActionUpdate();
			}
			//end SPR1800
		}
	}
	/**
	 * Calculate PaidToDate of contract based on date of last premium.
	 */
	protected void process_P010() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValCWA.process_P010()"); //NBA103
			//begin SPR1804
			getPolicy().setPaidToDate(getPolicy().getEffDate()); //SPR1804
			for (int i = 0; i < getPolicy().getFinancialActivityCount(); i++) {
				FinancialActivity financialActivity = getPolicy().getFinancialActivityAt(i);
				if (financialActivity.hasFinActivityDate()
					&& NbaUtils.compare(financialActivity.getFinActivityDate(), getPolicy().getPaidToDate()) > 0) {
					getPolicy().setPaidToDate(financialActivity.getFinActivityDate());
				}
			}
			getPolicy().setBilledToDate(getPolicy().getPaidToDate()); 
			//end SPR804
			getPolicy().setActionUpdate();
		}
	}
	/**
	 * Create debit and credit accounting for FinancialActivity objects for new CWA, reversals and refunds. 
	 * Retrieve the appropriate debit and credit AccountNumber for the financial activity from the NBA_Accounting table.
	 */
	protected void process_P008() {
        if (verifyCtl(FINANCIALACTIVITY)) {
            logDebug("Performing NbaValCWA.process_P008()"); //NBA103
            if (getFinancialActivity().getAccountingActivityCount() < 1) {
                //begin SPR1799
                // SPR2817 code deleted
                try {
                    //begin SPR2817
                    NbaTableData[] nbaTableData = NbaUtils.getAccountingTableEntry(getNbaDst(), getPolicy(), getFinancialActivity(),
                            NbaConstants.ACCOUNTING_FOR_NEW_CWA);
                    AccountingActivity debit = NbaUtils.createAccountingActivity(getPolicy(), getFinancialActivity(),
                            getPrimaryInsOrAnnuitantRelation(), getNbaOLifEId(), getCurrentDate(), nbaTableData, OLI_ACCTDBCRTYPE_DEBIT,
                            NbaConstants.ACCOUNTING_FOR_NEW_CWA);
                    AccountingActivity credit = NbaUtils.createAccountingActivity(getPolicy(), getFinancialActivity(),
                            getPrimaryInsOrAnnuitantRelation(), getNbaOLifEId(), getCurrentDate(), nbaTableData, OLI_ACCTDBCRTYPE_CREDIT,
                            NbaConstants.ACCOUNTING_FOR_NEW_CWA);
                    //end SPR2817                            
                    if (debit != null && credit != null) {
                        getFinancialActivity().addAccountingActivity(debit);
                        getFinancialActivity().addAccountingActivity(credit);
                        getFinancialActivity().setActionUpdate();
                        getFinancialActivityExtension(); //Cause extension to be created if necessary
                    } else {
                        addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getFinancialActivity()));
                    }
                } catch (NbaBaseException e) {	//SPR2817
                    addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getFinancialActivity()));
                }
                //end SPR1799
            }
        }
    }
	/**
	 * Answer the actual premium amount
	 * @return actual premium amount
	 */
	//SPR1748 New Method 
	protected double getActual() {
		return actual;
	}
	
	//NBA228 code deleted
	/**
	 * Answer the required premium amount
	 * @return required premium amount
	 */
	//SPR1748 New Method 
	protected double getRequired() {
		return required;
	}
	/**
	 * Set the actual premium amount
	 * @param the actual premium amount
	 */
	//SPR1748 New Method 
	protected void setActual(double d) {
		actual = d;
	}
	/**
	 * Set the required premium amount
	 * @param required premium amount
	 */
	//SPR1748 New Method 
	protected void setRequired(double d) {
		required = d;
	}
	/**
	 * Set the value of Holding.Policy.PolicyExtension.ContractChangeType. 
	 * If ApplicationInfo.CWAAmt is greater than zero, set ContractChangeType to 2. 
	 * Otherwise, set ContractChangeType to 1.
	 */
	//SPR1792 New Method
	protected void process_P011() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValCWA.process_P011()"); //NBA103
			long code;
			if (getApplicationInfo().hasCWAAmt() && getApplicationInfo().getCWAAmt() > 0) {
				code = OLIX_CHANGETYPE_NEWBUS;
			} else {
				code = OLIX_CHANGETYPE_NEWBUSNOTPAID;
			}
			getPolicyExtension().setContractChangeType(code);
			getPolicyExtension().setActionUpdate();
		}
	}	
	/**
	 * Set Holding.Policy.FinancialActivity.FinActivityDate to the Current System Date if Not Present.
	 */
	//SPR2125 New Method 
	protected void process_P013() {
        if (verifyCtl(FINANCIALACTIVITY)) {
            logDebug("Performing NbaValCWA.process_P013()");
            if (!getFinancialActivity().hasFinActivityDate()) {
                getFinancialActivity().setFinActivityDate(getCurrentDate());                
            }
        }
	}
	/**
	 * Return total wiaved amount on the policy.
	 */
	//APSL2895 New Method
	public double getWaiverAmount()
	{
		int finCount = getPolicy().getFinancialActivityCount();
		double waiveAmt = 0.0;
		for (int i = 0; i < finCount; i++) {
			FinancialActivity financialActivity = getPolicy().getFinancialActivityAt(i);
			if (financialActivity != null && financialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_PYMNTSHORTAGE) {
				waiveAmt += financialActivity.getFinActivityGrossAmt();
			}
		}
		return waiveAmt;
	}
    }