package com.csc.fsg.nba.contract.extracts;
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
 * *******************************************************************************<BR>
 */
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.csc.fsg.nba.database.NbaExtractDataBaseAccessor;
import com.csc.fsg.nba.database.NbaPostIssueServiceAccessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaContractAccessException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaCWAPaymentsAccountingExtractVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaPostIssueServiceVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.AccountingActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Payment;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;
/**
* NbaCwaPaymentsExtract is used to create Accounting Extracts for Pending CWA Payments.
* <p>
* <b>Modifications:</b><br>
* <table border=0 cellspacing=5 cellpadding=5>
* <thead>
* <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
* </thead>
* <tr><td>SPR1656</td><td>Version 4</td><td>Allow for Refund/Reversal Extracts.</td></tr>
* <tr><td>SPR1665</td><td>Version 4</td><td>Accounting Extracts Indicator should be set in the XML data.</td></tr>
* <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes.</td></tr>
* <tr><td>SPR1906</td><td>Version 4</td><td>General source code clean up</td></tr>
* <tr><td>SPR2817</td><td>Version 6</td><td>Pending Accounting Needs to Be Added to nbA</td>
* <tr><td>NBA208-26</td><td>Version 7</td><td>Remove synchronized keyword from getLogger() methods</td></tr>
* <tr><td>AXAL3.7.23</td><td>AXA life Phase 1</td><td>Accounting Interface</td></tr>
* <tr><td>NBA228</td><td>Version 8</td><td>Cash Management Enhancement</td></tr> 
* <tr><td>ALPC119</td><td>AXA Life Phase 1</td><td>YRT Discount Accounting </td></tr>
* <tr><td>P2AXAL019</td><td>AXA Life Phase 2</td><td>Cash Management </td></tr>
* </table>
* <p>
* @author CSC FSG Developer
 * @version 7.0.0
* @since New Business Accelerator - Version 4
*/
public class NbaCwaPaymentsExtract {
	
	protected static NbaLogger logger = null;
	/**
	* Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	* @return com.csc.fsg.nba.foundation.NbaLogger
	*/
	protected static NbaLogger getLogger() { // NBA208-26
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaCwaPaymentsExtract.class.getName());  //SPR1906
			} catch (Exception e) {
				NbaBootLogger.log("NbaCwaPaymentsExtract could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

	/**
	 * This method creates Accounting Extract for Pending CWA Payment Activity in the nbA extract database.
	 * @param nbaTXLife NbaTXLife object
	 * @return conn Connection object which contains the connection of extract database
	 * @throws NbaBaseException
	 */
	 //SPR2817 added new parameter, usage
	//AXAL3.7.23 - Added NbaUserVO to the method signature  
	//APSL2440 Internal Perfomance Issue changed method signature
	protected void createAccountingExtractForPendingCWAPayment(NbaTXLife nbaTXLife, String usage, NbaUserVO user) throws NbaBaseException {
		// APSL2440 Internal Perfomance Issue code deleted
		// code deleted for APSL3874
		// AXAL3.7.23
		// APSL2440 Internal Perfomance Issue code deleted
		int finActivityCount = nbaTXLife.getPrimaryHolding().getPolicy().getFinancialActivityCount();
		for (int i = 0; i < finActivityCount; i++) {
			FinancialActivity finAct = nbaTXLife.getPrimaryHolding().getPolicy().getFinancialActivityAt(i);
			// if (finAct.getAccountingActivityCount() > 0) { //AXAL3.7.23
			FinancialActivityExtension finActExt = getFinancialActivityExtension(finAct); // SPR2817
			if (!(finActExt.getAcctgExtractInd()) || NbaConstants.ACCOUNTING_FOR_ISSUE.equals(usage)) { // SPR2817
				// For AXA life the we do not need seperate debit credit 508 xml's, so debitCreditTxLife508List will always have one element
				// AXAL3.7.23 code deleted
				// AXAL3.7.23 begin
				invokeTransmitAccountingInformationWS(nbaTXLife, user, finAct);
				// AXAL3.7.23 end
				// set Accounting Extract Generated indicator
				finActExt.setAcctgExtractInd(true);
				finActExt.setActionUpdate();
				// APSL2440 Internal Perfomance Issue code deleted
			}
		}
		// SPR2817 code deleted
		// }// end if //AXAL3.7.23
		// AXAL3.7.23 begin
		// code deleted for APSL3874
		// SPR2817 code deleted
		//APSL2440 Internal Perfomance Issue code deleted
	}

	/**
	 * @param nbaTXLife
	 * @param user
	 * @param finAct
	 * @throws NbaBaseException
	 */
	//APSL2440 Internal Perfomance Issue new method
	private void invokeTransmitAccountingInformationWS(NbaTXLife nbaTXLife, NbaUserVO user, FinancialActivity finAct) throws NbaBaseException {
		if (!NbaUtils.isProductCodeCOIL(nbaTXLife)) {//NBLXA-1908
			AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_TRANSMIT_ACCOUNTING_INFO, user,
					nbaTXLife, null, finAct);
			webServiceInvoker.execute();
		}
	}

	/**
	 * The method is responsible for closing Extract DataBase connection. 
	 * @param conn
	 * @throws NbaContractAccessException
	 */
	//AXAL3,7,23 new method added.
	private void closeConnection(Connection conn) throws NbaContractAccessException {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException sqle) {
				throw new NbaContractAccessException(NbaBaseException.CLOSE_CONNECTIONS_FAILED, sqle);
			}
		}
	}

	/**
	 * This method updates the extract for CWA.
	 * @param debitCredit508 an ArrayList containg 508 xml for debit and credit extract. 
	 * @param systemID System ID
	 * @throws NbaDataAccessException
	 */
	protected Connection createCWAExtract(
		ArrayList debitCredit508List,
		String systemID,
		NbaExtractDataBaseAccessor aNbaExtractDataBaseAccessor,
		Connection conn)
		throws NbaDataAccessException {
		try {
			for (int i = 0; i < debitCredit508List.size(); i++) {
				
				updateCWAExtract((NbaTXLife) debitCredit508List.get(i), systemID, aNbaExtractDataBaseAccessor, conn);
			}
		} catch (NbaDataAccessException dae) {
			throw dae;
		}
		return conn;
	}

	/**
	 * This method puts Transaction 1225 into each NbaCheckDepositAccountingExtractVO.
	 * @param nbaTXLife NbaTXLife object
	 * @param systemID System ID
	 * @param aNbaExtractDataBaseAccessor NbaExtractDataBaseAccessor object
	 * @param conn Connection object
	 * @throws NbaDataAccessException
	 */
	protected void updateCWAExtract(NbaTXLife nbaTXLife, String systemID, NbaExtractDataBaseAccessor aNbaExtractDataBaseAccessor, Connection conn)
		throws NbaDataAccessException {
		NbaCWAPaymentsAccountingExtractVO element = new NbaCWAPaymentsAccountingExtractVO();
		Policy policy = nbaTXLife.getPrimaryHolding().getPolicy();
		element.setSystemIDKey(systemID);
		element.setCompanyCode(policy.getCarrierCode());
		element.setContractNumber(policy.getPolNumber());
		element.setProductCode(policy.getProductCode());
		element.setProductType(policy.getProductType());
		element.setExtractCreate(new java.util.Date());
		element.setExtractSent(null);
		element.setXml_Data(nbaTXLife.toXmlString());
		aNbaExtractDataBaseAccessor.insert(element, conn);
	}
	//AXAL3.7.23 code deleted
	/**
	 * Searches forAccountingActivity objects based on ID and removes rest of them.
	 * @param finAct FinancialActivity object
	 * @param debitCreditype describes if it is debit or credit
	 * @return FinancialActivity
	 */
	//SPR2817 added new parameter - usage
	protected FinancialActivity setAccountingActivity(FinancialActivity finAct, long debitCreditType, String usage) {
		FinancialActivity debitCreditFinAct = finAct.clone(false);
		int accActCount = debitCreditFinAct.getAccountingActivityCount() - 1; //SPR2817
		for (int m = accActCount; m > -1; m--) {	//SPR2817
			AccountingActivity acntAct = debitCreditFinAct.getAccountingActivityAt(m);
			//begin SPR2817
            if (!acntAct.hasAccountingActivityKey()) {
                acntAct.setAccountingActivityKey(NbaConstants.ACCOUNTING_FOR_NEW_CWA);
            }
            if (acntAct.getAccountDebitCreditType() != debitCreditType || !acntAct.getAccountingActivityKey().equals(usage)) {
                debitCreditFinAct.removeAccountingActivity(acntAct);
            }
            //end SPR2817
		}
		//begin SPR2817
        if (debitCreditFinAct.getAccountingActivityCount() > 0) {
            return debitCreditFinAct;
        } else {
            return null;
        }
        //end SPR2817
		  
	}
	// SPR2817 code deleted
	/**
	 * Retrieve the FinancialActivityExtension for a FinancialActivity with lazy initialization
	 * @param finAct - the FinancialActivity
	 * @return FinancialActivityExtension
	 */
	 // SPR2817 New Method
	protected FinancialActivityExtension getFinancialActivityExtension(FinancialActivity finAct) {
        FinancialActivityExtension finActExt = NbaUtils.getFirstFinancialActivityExtension(finAct);
        if (finActExt == null) {
            OLifEExtension olifExt = new OLifEExtension();
            finActExt = new FinancialActivityExtension();
            finActExt.setActionAdd();
            olifExt.setFinancialActivityExtension(finActExt);
            finAct.addOLifEExtension(olifExt);
        }
        return finActExt;
    }
	//AXAL3.7.23 code deleted
	/**
	 * This method creates accounting extracts for overage/shortage amount
	 * @param nbaTXLife NbaTXLife object
	 * @param dst NbaDst object
	 * @param user NbaUserVO object  
	 * @return conn Connection object which contains the connection of extract database
	 * @throws NbaBaseException
	 */
	 //NBA228 new method
	//APSL2440 Internal Perfomance Issue changed method signature Begin NBLXA-1457 changed method name
	protected void createShortageAccountingExtract(NbaTXLife nbaTXLife, NbaDst dst, NbaUserVO user) throws NbaBaseException {
		double actualPayment = nbaTXLife.getPolicy().getApplicationInfo().getCWAAmt();
		double requiredPayment = nbaTXLife.getPolicy().getPaymentAmt();
		// Begin P2AXAL019
		double paymentDiff = 0;
		if (NbaConstants.SYST_LIFE70.equals(dst.getNbaLob().getBackendSystem())) { //NBLXA-1977
			requiredPayment = nbaTXLife.getPolicy().getMinPremiumInitialAmt();
			double Diff = actualPayment - requiredPayment;
			paymentDiff = (Diff < 0) ? Diff : 0; // skips creating account extract for overage for advanced life products
		} else {
			long modalPayments = 1; // shortage or exact CWA amt condition, APSL2640
			//Start NBLXA-1977 modified block
			double overageAmt = 0;
			NbaVpmsResultsData vpmsOverageAmtResultsData = getDataFromVPMS(NbaVpmsConstants.CONTRACTVALIDATIONCOMPANYCONSTANTS,
					NbaVpmsConstants.EP_CWA_OVERAGE_LIMIT,nbaTXLife);
			if (vpmsOverageAmtResultsData != null && vpmsOverageAmtResultsData.getResultsData() != null && vpmsOverageAmtResultsData.wasSuccessful()
					&& vpmsOverageAmtResultsData.getResultsData().size() == 1) {
				String overageAmtString = (String) vpmsOverageAmtResultsData.getResultsData().get(0);
				if (!NbaUtils.isBlankOrNull(overageAmtString)) {
					String[] overageAmtArray = overageAmtString.split("@@");
					overageAmt = Double.parseDouble(overageAmtArray[0]);
				}
			}
			if (overageAmt == 0) { 
				modalPayments = new Double(Math.ceil(actualPayment / requiredPayment)).longValue(); // APSL2640
				paymentDiff = actualPayment - requiredPayment * modalPayments; // APSL2640
			}
			// End NBLXA-1977
		}
		if (paymentDiff < 0 && paymentDiff > -10d) { // CWA is NOT same as the premium NBLXA-2107
			long overageShortage = NbaOliConstants.OLI_FINACT_PYMNTSHORTAGE;
			// Assuming that the Overage/Shorage is within tolerance limit at the time of Issue
			// create a financial activity for Overage/Shortage
			FinancialActivity finActivity = new FinancialActivity();
			// Begin NBLXA-1977
			NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTXLife);
			nbaOLifEId.setId(finActivity);
			// End NBLXA-1977
			finActivity.setFinancialActivityKey("01");
			finActivity.setFinActivityDate(new Date());
			finActivity.setFinEffDate(new Date());
			finActivity.setFinActivityType(overageShortage);
			finActivity.setFinActivityGrossAmt(Math.abs(paymentDiff));
			finActivity.setUserCode(user.getUserID());
			Policy policy = nbaTXLife.getOLifE().getHoldingAt(0).getPolicy();
			FinancialActivityExtension finactext=null; //NBLXA-2107
			try {
				FinancialActivity finActTxLife = policy.getFinancialActivityAt(0);
				FinancialActivityExtension faExt = NbaUtils.getFirstFinancialActivityExtension(finActTxLife);
				OLifEExtension oExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_FINANCIALACTIVITY);
				finactext = oExt.getFinancialActivityExtension(); //NBLXA-2107
				finactext.setLocationID(faExt.getLocationID());
				finActivity.addOLifEExtension(oExt);
			} catch (Exception exp) {
				getLogger().logException(exp.getMessage(), exp);
				throw new NbaBaseException("No CWA attached with case", exp);
			}
			// AXAL3.7.23 - code deleted
			// AXAL3.7.23 begin
			invokeTransmitAccountingInformationWS(nbaTXLife, user, finActivity);
			// AXAL3.7.23 end
			// AXAL3.7.23 - code deleted
			// AXAL3.7.23 - code deleted
			finactext.setAcctgExtractInd(true); //NBLXA-2107
			finActivity.setActionAdd();
			nbaTXLife.getPolicy().addFinancialActivity(finActivity);
		}
	}

	 // End NBLXA-1457
	/**
	 * This method creates accounting extracts for YRT Discount amount
	 * @param nbaTXLife NbaTXLife object
	 * @param dst NbaDst object
	 * @param user NbaUserVO object  
	 * @return conn Connection object which contains the connection of extract database
	 * @throws NbaBaseException
	 */
	 //ALPC119 new method
	//APSL2704 QC10395 changed method signature - restrict multiple YRT webservice  invoke calls.
	protected void createYRTDiscountAccountingExtract(NbaTXLife nbaTXLife, NbaUserVO user,String workType) throws NbaBaseException {
		double yrtDiscount = NbaUtils.getFirstPolicyExtension(nbaTXLife.getPolicy()).getFirstYrPremDiscountAmt();
		NbaPostIssueServiceVO postIssueVO = null;
		boolean notInvoked = true;
		Policy policy = nbaTXLife.getOLifE().getHoldingAt(0).getPolicy();
		postIssueVO = NbaPostIssueServiceAccessor.selectPostIssueRecord(policy.getPolNumber(),workType);
		if (postIssueVO != null)
		{
			notInvoked = postIssueVO.isYrt();
		}
		if (notInvoked && !NbaUtils.isBlankOrNull(yrtDiscount)) { //ALS2686  
			long yrtDiscountType = NbaOliConstants.OLI_FINACT_YRTDISCOUNTTYPE;
			//create a financial activity for yrt discount
			FinancialActivity finActivity = new FinancialActivity();
			finActivity.setId("FinancialActivity_1");
			finActivity.setFinancialActivityKey("01");
			finActivity.setFinActivityDate(new Date());
			finActivity.setFinEffDate(new Date());
			finActivity.setFinActivityType(yrtDiscountType);
			finActivity.setFinActivityGrossAmt(Math.abs(yrtDiscount));
			finActivity.setUserCode(user.getUserID());
			
			try {
				FinancialActivity finActTxLife = policy.getFinancialActivityAt(0);
				FinancialActivityExtension faExt = NbaUtils.getFirstFinancialActivityExtension(finActTxLife);
				OLifEExtension oExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_FINANCIALACTIVITY);
				FinancialActivityExtension finactext = oExt.getFinancialActivityExtension();
				finactext.setLocationID(faExt.getLocationID());
				finActivity.addOLifEExtension(oExt);
			} catch (Exception exp) {
				getLogger().logException(exp.getMessage(), exp);
				throw new NbaBaseException("No CWA attached with case", exp);
			}
			//ALPC119 begin
			try
			{
				invokeTransmitAccountingInformationWS(nbaTXLife, user, finActivity);
			}
			catch(NbaDataAccessException e )
			{
				throw new NbaDataAccessException("YRT web service failed" + policy.getPolNumber(), e);	
			}
			postIssueVO = new NbaPostIssueServiceVO(workType);
			postIssueVO.setContractNumber(policy.getPolNumber());
			postIssueVO.setYrt(false);
			postIssueVO.setActionAdd();
			NbaPostIssueServiceAccessor.save(postIssueVO);
			//ALPC119 end
			//ALPC119 code deleted
		}
	}	
	/**
	 * This method creates accounting extracts for CarryOverLoan amount
	 * @param nbaTXLife NbaTXLife object
	 * @param dst NbaDst object
	 * @param user  NbaUserVO object
	 * @return conn Connection object which contains the connection of extract database
	 * @throws NbaBaseException
	 */
	//	P2AXAL019 new method
	//APSL2440 Internal Perfomance Issue changed method signature
	protected void createCarryOverLoanExtract(NbaTXLife nbaTXLife, NbaUserVO user) throws NbaBaseException {
		for (int i = 0; i < nbaTXLife.getOLifE().getHoldingCount(); i++) {
			Holding holding = nbaTXLife.getOLifE().getHoldingAt(i);
			for (int j = 0; j < holding.getLoanCount(); j++) {
				FinancialActivity finActivity = new FinancialActivity();
				finActivity.setId("FinancialActivity_1");
				finActivity.setFinancialActivityKey("01");
				finActivity.setFinEffDate(new Date());
				finActivity.setFinActivityType(NbaOliConstants.OLI_FINACT_CARRYOVERLOAN);
				if (holding.getLoanAt(j).hasLoanBalance() && holding.getLoanAt(j).getLoanBalance() > 0) {
					finActivity.setFinActivityGrossAmt(Math.abs(holding.getLoanAt(j).getLoanBalance()));
				}
				finActivity.setUserCode(user.getUserID());
				Policy policy = nbaTXLife.getOLifE().getHoldingAt(0).getPolicy();
				finActivity.setFinActivityDate(policy.getFinancialActivityAt(0).getFinActivityDate());
				try {
					FinancialActivity finActTxLife = policy.getFinancialActivityAt(0);
					FinancialActivityExtension faExt = NbaUtils.getFirstFinancialActivityExtension(finActTxLife);
					OLifEExtension oExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_FINANCIALACTIVITY);
					FinancialActivityExtension finactext = oExt.getFinancialActivityExtension();
					finactext.setLocationID(faExt.getLocationID());
					finActivity.addOLifEExtension(oExt);
				} catch (Exception exp) {
					getLogger().logException(exp.getMessage(), exp);
					throw new NbaBaseException("No CWA attached with case", exp);
				}
				invokeTransmitAccountingInformationWS(nbaTXLife, user, finActivity);
			}
		}		
	}
	
	//APSL3460 Overloading this method for LIM adc.
	protected void createAccountingExtractForPendingCWAPayment(NbaTXLife nbaTXLife, NbaDst dst, String usage, NbaUserVO user) throws NbaBaseException {
        //APSL2440 Internal Perfomance Issue code deleted               
        try {//AXAL3.7.23
            //APSL2440 Internal Perfomance Issue code deleted
            int finActivityCount = nbaTXLife.getPrimaryHolding().getPolicy().getFinancialActivityCount();
            for (int i = 0; i < finActivityCount; i++) {
                FinancialActivity finAct = nbaTXLife.getPrimaryHolding().getPolicy().getFinancialActivityAt(i);
                //if (finAct.getAccountingActivityCount() > 0) { //AXAL3.7.23
                FinancialActivityExtension finActExt = getFinancialActivityExtension(finAct); //SPR2817
                if (!(finActExt.getAcctgExtractInd()) || NbaConstants.ACCOUNTING_FOR_ISSUE.equals(usage)) { //SPR2817
                    // For AXA life the we do not need seperate debit credit 508 xml's, so debitCreditTxLife508List will always have one element
                    //AXAL3.7.23 code deleted
                    //AXAL3.7.23 begin
                    invokeTransmitAccountingInformationWS(nbaTXLife, dst, user, finAct);
                    //AXAL3.7.23 end
                    // set Accounting Extract Generated indicator
                    finActExt.setAcctgExtractInd(true);
                    finActExt.setActionUpdate();    
                    //APSL2440 Internal Perfomance Issue code deleted
                }
            }
            //SPR2817 code deleted
        //}// end if //AXAL3.7.23
            //AXAL3.7.23 begin
        } catch (NbaBaseException nbe) {
            //APSL2440 Internal Perfomance Issue code deleted
            throw new NbaBaseException("Error sending transmit accounting info extract:" + nbe.getMessage(), nbe);
        }
        // SPR2817 code deleted
        //APSL2440 Internal Perfomance Issue code deleted
    }
		
	//APSL3460 Overloading this method for LIM adc.
	//APSL2440 Internal Perfomance Issue new method
    private void invokeTransmitAccountingInformationWS(NbaTXLife nbaTXLife,  NbaDst dst, NbaUserVO user, FinancialActivity finAct) throws NbaBaseException {
		if (!NbaUtils.isProductCodeCOIL(nbaTXLife)) {//NBLXA-1908
			AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_TRANSMIT_ACCOUNTING_INFO, user,
					nbaTXLife, dst, finAct);
			webServiceInvoker.execute();
		}
    }
    
	//QC13592/APSL4000 New Method- creates accounting extracts for Paid Reissue cases
	public void createAccountingExtractForPaidReissue(NbaTXLife nbaTXLife, NbaDst dst, boolean isIssue, NbaUserVO user) throws NbaBaseException {
		try {
			if (isIssue) {
				int finActivityCount = nbaTXLife.getPrimaryHolding().getPolicy().getFinancialActivityCount();
				for (int i = 0; i < finActivityCount; i++) {
					FinancialActivity finAct = nbaTXLife.getPrimaryHolding().getPolicy().getFinancialActivityAt(i);
					if (NbaOliConstants.OLI_FINACT_PREMIUMINIT == finAct.getFinActivityType()) {
						invokeTransmitAccountingInformationWS(nbaTXLife, dst, user, finAct);
					}
				}
			}
		} catch (NbaBaseException nbe) {
			throw new NbaBaseException("Error sending transmit accounting info extract:" + nbe.getMessage(), nbe);
		}
	}
	
	
	// NBLXA-1457 new method	
	public void createOverageAccountingExtract(NbaTXLife nbaTXLife, NbaUserVO user,double OverageAmt) throws NbaBaseException {
			FinancialActivity finActivity = new FinancialActivity();
			NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTXLife);
			nbaOLifEId.setId(finActivity);
			finActivity.setFinancialActivityKey("01");
			finActivity.setFinActivityDate(new Date());
			finActivity.setFinActivityType(NbaOliConstants.OLI_FINACT_PVT315);
			finActivity.setFinActivityGrossAmt(Math.abs(OverageAmt));
			finActivity.setFinEffDate(new Date()); //NBLXA-1686
			finActivity.setUserCode(user.getUserID());
			Policy policy = nbaTXLife.getOLifE().getHoldingAt(0).getPolicy();
			try {
				FinancialActivity finActTxLife = policy.getFinancialActivityAt(0);
				FinancialActivityExtension faExt = NbaUtils.getFirstFinancialActivityExtension(finActTxLife);
				OLifEExtension oExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_FINANCIALACTIVITY);
				FinancialActivityExtension finactext = oExt.getFinancialActivityExtension();
				finactext.setLocationID(faExt.getLocationID());
				finActivity.addOLifEExtension(oExt);
				// Begin NBLXA-1686
				Payment payment = new Payment();
				payment.setPaymentForm("1009800004");
				finActivity.addPayment(payment);
				// Begin NBLXA-1686
				finActivity.setActionAdd();
				nbaTXLife.getPolicy().addFinancialActivity(finActivity);
			} catch (Exception exp) {
				getLogger().logException(exp.getMessage(), exp);
				throw new NbaBaseException("No CWA attached with case", exp);
			}
    	}	
	
	// End NBLXA-1457
	
    //NBLXA-1977 new method 
	public NbaVpmsResultsData getDataFromVPMS(String modelName, String entryPoint, NbaTXLife nbaTXLife) {
		NbaVpmsAdaptor adapter = null;
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
			if (nbaTXLife != null) {
				oinkData.setContractSource(nbaTXLife);
			}
			adapter = new NbaVpmsAdaptor(oinkData, modelName);
			Map deOinkMap = new HashMap();
			adapter.setSkipAttributesMap(deOinkMap);
			adapter.setVpmsEntryPoint(entryPoint);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(adapter.getResults());
			return vpmsResultsData;
		} catch (Exception e) {
			getLogger().logDebug("Problem in getting output from VPMS" + e.getMessage());
		} finally {
			try {
				if (adapter != null) {
					adapter.remove();
				}
			} catch (Throwable th) {
				// ignore, nothing can be done
			}
		}
		return null;
	}

}