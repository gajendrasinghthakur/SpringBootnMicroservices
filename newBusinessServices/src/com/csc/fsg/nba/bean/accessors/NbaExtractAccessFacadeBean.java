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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.SessionBean;

import com.csc.fsg.nba.database.NbaExtractDataBaseAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaExtractAccessException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaDepositAccountingData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.AxaCheckDepositAccountingExtractVO;
import com.csc.fsg.nba.vo.AxaCheckExtractVO;
import com.csc.fsg.nba.vo.AxaContractsCheckExtractVO;
import com.csc.fsg.nba.vo.NbaCheckDepositAccountingExtractVO;
import com.csc.fsg.nba.vo.NbaCreditCardAccountingExtractVO;
import com.csc.fsg.nba.vo.NbaDepositAccountingExtractsVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.AccountingActivity;
import com.csc.fsg.nba.vo.txlife.AccountingStatement;
import com.csc.fsg.nba.vo.txlife.AccountingStatementExtension;
import com.csc.fsg.nba.vo.txlife.CheckDepositInfo;
import com.csc.fsg.nba.vo.txlife.CheckPolNumberCC;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.FinancialStatement;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Payment;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.webservice.client.AxaInvokeWebservice;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;
/**
* The NbaExtractAccessFacadeBean stateless session bean provides an interface 
*  to the datastore to retrieve, insert, update and delete data on nbaexxtract database.  
* <p>
* <b>Modifications:</b><br>
* <table border=0 cellspacing=5 cellpadding=5>
* <thead>
* <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
* </thead>
* <tr><td>NBA066</td><td>Version 3</td><td>nbA Accounting and Disbursements extracts</td></tr>
* <tr><td>SPR1656</td><td>Version 4</td><td>Allow for Refund/Reversal Extracts.</td></tr>
* <tr><td>SPR1726</td><td>Version 4</td><td>NBA_CHECK_DEPOSIT_ACCOUNTING extracts are not updated after Deposit Ticket Correction</td></tr>
* <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
* <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr> 
* <tr><td>SPR1906</td><td>Version 4</td><td>General source code clean up</td></tr>
* <tr><td>NBA115</td><td>Version 5</td><td>Credit Card</td></tr> 
* <tr><td>NBA208-26</td><td>Version 7</td><td>Remove synchronized keyword from getLogger() methods</td></tr>
* <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
* <tr><td>ALPC7</td><td>Version 7</td><td>Schema migration from 2.8.90 to 2.9.03</td></tr>
* <tr><td>NBA228</td><td>Version 8</td><td>Cash Management Enhancement</td></tr> 
* <tr><td>AXAL3.7.23</td><td>AXA Life Phase 1</td><td>Accounting Interface</td></tr> 
* </table>
* <p>
* @author CSC FSG Developer
 * @version 7.0.0
* @since New Business Accelerator - Version 3
*/
public class NbaExtractAccessFacadeBean implements SessionBean {
		
	protected static NbaLogger logger = null;
	private javax.ejb.SessionContext mySessionCtx;
	private boolean checkDeposit = true;//NBA115
	private static String CHECK_PAYMENT = "1";//NBA115
	private static String CREDIT_CARD_PAYMENT = "2";//NBA115
	
	private int checkDepInfoIdCount = 1; //AXAL3.7.23
	private String checkDepositInfoIDConstant = "CheckDepositInfo_"; //AXAL3.7.23
	private List holdingList = new ArrayList();  //AXAL3.7.23
	
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
	* Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	* @return com.csc.fsg.nba.foundation.NbaLogger
	*/
	protected static NbaLogger getLogger() { // NBA208-26
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaExtractAccessFacadeBean.class.getName());  //SPR1906
			} catch (Exception e) {
				NbaBootLogger.log("NbaExtractAccessFacadeBean could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * This method created accounting extracts for check deposits in the nbA extract database.
	 * @param depositExtractsList ArrayList contains the list of NbaCheckDepositAccountingExtractVO
	 * @param correctedDeposit boolean to indicated whether it's a new deposit or correction in existing deposit
	 * @throws NbaBaseException
	 */
	//NBA103
	public void createAccountingExtractForCheckDeposit(List depositExtractsList, boolean correctedDeposit)throws NbaBaseException{//SPR1726 signature changed
		Connection conn = null;
		setCheckDeposit(true);//NBA115
		try{//NBA103
			conn = NbaExtractDataBaseAccessor.getInstance().getConnection();
			//SPR1726 code deleted
			//begin SPR1726
			NbaDepositAccountingExtractsVO nbaDepositAccountingExtractsVO = new NbaDepositAccountingExtractsVO();
			for(int j =0;j<depositExtractsList.size();j++){
				ArrayList depositExtractVOlist = (ArrayList)depositExtractsList.get(j);
				updateSystemID(depositExtractVOlist);
				for(int k=0;k<depositExtractVOlist.size();k++){
					NbaCheckDepositAccountingExtractVO element = (NbaCheckDepositAccountingExtractVO) depositExtractVOlist.get(k);
					if(element.getCorrection_Ind().equalsIgnoreCase(NbaConstants.YES_VALUE)){
						nbaDepositAccountingExtractsVO = getDepositAccountInfo(element.getCompanyCode(), "FALSE", CHECK_PAYMENT, element
								.getLocationId()); //NBA115 NBA228
						NbaTXLife txLife1225debit =
							createDepositTrx1225(
								nbaDepositAccountingExtractsVO,
								NbaOliConstants.OLI_ACCTDBCRTYPE_DEBIT,
								element.getCheck_Amt());
						NbaTXLife txLife1225credit =
							createDepositTrx1225(
								nbaDepositAccountingExtractsVO,
								NbaOliConstants.OLI_ACCTDBCRTYPE_CREDIT,
								element.getCheck_Amt());
						// create two extract record (one for debit and one for credit) per check.
						createExtract(depositExtractVOlist, txLife1225debit, txLife1225credit, conn);

						nbaDepositAccountingExtractsVO = getDepositAccountInfo(element.getCompanyCode(), "FALSE", CHECK_PAYMENT, element
								.getLocationId()); //NBA115 NBA228
						txLife1225debit =
							createDepositTrx1225(
								nbaDepositAccountingExtractsVO,
								NbaOliConstants.OLI_ACCTDBCRTYPE_DEBIT,
								Double.parseDouble(element.getRevisedAmount())); //NBA228
						txLife1225credit =
							createDepositTrx1225(
								nbaDepositAccountingExtractsVO,
								NbaOliConstants.OLI_ACCTDBCRTYPE_CREDIT,
								Double.parseDouble(element.getRevisedAmount())); //NBA228
						// create two extract record (one for debit and one for credit) per check.
						createExtract(depositExtractVOlist, txLife1225debit, txLife1225credit, conn);
					} else if(!correctedDeposit){
						nbaDepositAccountingExtractsVO = getDepositAccountInfo(element.getCompanyCode(), "FALSE", CHECK_PAYMENT, element
								.getLocationId()); //NBA115 NBA228
						NbaTXLife txLife1225debit =
							createDepositTrx1225(
								nbaDepositAccountingExtractsVO,
								NbaOliConstants.OLI_ACCTDBCRTYPE_DEBIT,
								element.getCheck_Amt());
						NbaTXLife txLife1225credit =
							createDepositTrx1225(
								nbaDepositAccountingExtractsVO,
								NbaOliConstants.OLI_ACCTDBCRTYPE_CREDIT,
								element.getCheck_Amt()); //NBA228
						// create two extract record (one for debit and one for credit) per check.
						createExtract(depositExtractVOlist, txLife1225debit, txLife1225credit, conn);
					}
				}
			}
		}catch (NbaBaseException e) {//NBA103
			getLogger().logException(e);//NBA103
			throw e;//NBA103
		}catch (Throwable t) {//NBA103
			NbaBaseException e = new NbaBaseException(t);//NBA103
			getLogger().logException(e);//NBA103
			throw e;//NBA103
		//end SPR1726
		}finally{
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException sqlException) {
				getLogger().logException(sqlException); //NBA103
			} 
		}
		
	}

	/**
	 * This method creates accounting extracts for credit card deposits in the nbA extract database.
	 * @param depositExtractsList ArrayList contains the list of NbaCheckDepositAccountingExtractVO
	 * @throws NbaBaseException
	 */
	//NBA115 new method
	public void createAccountingExtractForCreditCardDeposit(List depositExtractsList)throws NbaBaseException{//SPR1726 signature changed
		Connection conn = null;
		setCheckDeposit(false);
		try{
			conn = NbaExtractDataBaseAccessor.getInstance().getConnection();
			NbaDepositAccountingExtractsVO nbaDepositAccountingExtractsVO = new NbaDepositAccountingExtractsVO();
			for(int j =0;j<depositExtractsList.size();j++){
				ArrayList depositExtractVOlist = (ArrayList)depositExtractsList.get(j);
				updateSystemID(depositExtractVOlist);
				for(int k=0;k<depositExtractVOlist.size();k++){
					NbaCreditCardAccountingExtractVO element = (NbaCreditCardAccountingExtractVO) depositExtractVOlist.get(k);
					nbaDepositAccountingExtractsVO = getDepositAccountInfo(element.getCompanyCode(), "FALSE", CREDIT_CARD_PAYMENT);//NBA115
					NbaTXLife txLife1225debit =
						createDepositTrx1225(
							nbaDepositAccountingExtractsVO,
							NbaOliConstants.OLI_ACCTDBCRTYPE_DEBIT,
							element.getCharge_Amt()); //NBA228
					NbaTXLife txLife1225credit =
						createDepositTrx1225(
							nbaDepositAccountingExtractsVO,
							NbaOliConstants.OLI_ACCTDBCRTYPE_CREDIT,
							element.getCharge_Amt()); //NBA228
					createExtract(depositExtractVOlist, txLife1225debit, txLife1225credit, conn);
			}
		}
		}catch (NbaBaseException e) {
			getLogger().logException(e);
			throw e;
		}catch (Throwable t) {
			NbaBaseException e = new NbaBaseException(t);
			getLogger().logException(e);
			throw e;
		}finally{
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException sqlException) {
				getLogger().logException(sqlException); 
			} 
		}
	}

	/**
	 * Retrieve account numbers from NBA_DEPOSIT_ACCOUNTING.
	 * @param companyCode String
	 * @param offSettingCorrection String
	 * @return  NbaDepositAccountingExtractsVO object containg account numbers
	 * @throws NbaExtractAccessException, NbaBaseException
	 */	
	//NBA228 changed signature
	protected NbaDepositAccountingExtractsVO getDepositAccountInfo(String companyCode, String offSetCorrection, String paymentType, String locationId)
			throws NbaExtractAccessException, NbaBaseException { //SPR1726 signature changed
		//SPR1726 line deleted
		//begin SPR1726
		NbaDepositAccountingExtractsVO depositAccExtractVO = new NbaDepositAccountingExtractsVO();
		NbaTableAccessor tableAccessor = new NbaTableAccessor();
		Map tblKeys = new HashMap();
		tblKeys.put(NbaTableAccessConstants.C_COMPANY_CODE, companyCode);
		tblKeys.put(NbaTableAccessConstants.OFFSETTING_CORRECTION, offSetCorrection);
		tblKeys.put(NbaTableAccessConstants.PAYMENT_TYPE, paymentType);//NBA115
		tblKeys.put(NbaTableAccessConstants.LOCATION_ID, (!NbaUtils.isBlankOrNull(locationId) ? locationId : NbaTableAccessConstants.WILDCARD)); //NBA228
		
		NbaDepositAccountingData[] dataObj =
			(NbaDepositAccountingData[]) tableAccessor.getDisplayData(tblKeys, NbaTableConstants.NBA_DEPOSIT_ACCOUNTING);

		//throw error if account numbers are not found.
		if (dataObj.length < 1) {
			depositAccExtractVO.setErrorDesc("Account numbers not found");
			throw new NbaExtractAccessException(" Account numbers not found");
		}
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Account numbers not found in NBA_DEPOSIT_ACCOUNTING table");
		}

		depositAccExtractVO.setSystemID(dataObj[0].getSystemId());
		depositAccExtractVO.setCompanyCode(dataObj[0].getCompanyCode());
		depositAccExtractVO.setDebitAccount(dataObj[0].getDebitAccount());
		depositAccExtractVO.setCreditAccount(dataObj[0].getCreditAccount());
		depositAccExtractVO.setLocationID(locationId); //NBA228

		//end SPR1726

		return depositAccExtractVO;
	}
	
	/**
	 * Retrieve account numbers from NBA_DEPOSIT_ACCOUNTING.
	 * @param companyCode String
	 * @param offSettingCorrection String
	 * @return  NbaDepositAccountingExtractsVO object containg account numbers
	 * @throws NbaExtractAccessException, NbaBaseException
	 */	
	//NBA228 new method
	protected NbaDepositAccountingExtractsVO getDepositAccountInfo(String companyCode, String offSetCorrection, String paymentType)
		throws NbaExtractAccessException, NbaBaseException {
		return getDepositAccountInfo(companyCode, offSetCorrection, paymentType, null);
	}	
	
	/**
	 * This method creates Transaction 1225 for Check Deposit Extract.
	 * @param company name of company
	 * @param debitCredit describes if it is debit or credit
	 * @param accNumber account number
	 * @param accountAmount
	 * @return object of NbaTXLife updated with account information
	 * @throws NbaBaseException
	 */
	//NBA228 method signature changed
	private NbaTXLife createDepositTrx1225(NbaDepositAccountingExtractsVO extractVO, long debitCredit, double accountAmount) throws NbaBaseException{ //SPR1726 method signature changed
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_ACCOUNTINGSTMTTRANS);  
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setBusinessProcess("");

		//create txlife with default request fields
		NbaTXLife txLife1225 = new NbaTXLife(nbaTXRequest);
		
		//Begin NBA228
		//Create Financial Activity
		Holding holding = new Holding();
		holding.setId("Holding_1");
		txLife1225.getOLifE().addHolding(holding);
		
		Policy policy = new Policy();
		policy.setId("Policy_1");
		holding.setPolicy(policy);
		
		FinancialActivity finActivity = new FinancialActivity();
		finActivity.setId("FinancialActivity_1");
		OLifEExtension oExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_FINANCIALACTIVITY);
		FinancialActivityExtension finactext = oExt.getFinancialActivityExtension();
		if (!NbaUtils.isBlankOrNull(extractVO.getLocationID())) {
			finactext.setLocationID(extractVO.getLocationID());
		}
		finActivity.addOLifEExtension(oExt);
		//End NBA228
		
		//Create Financial Statement
		FinancialStatement finStatement = new FinancialStatement();
		finStatement.setId("H1");
		finStatement.setStatementType(NbaOliConstants.OLI_STMTTYPE_ACCTING);
		finStatement.setCarrierCode(extractVO.getCompanyCode());
		
		AccountingStatement accStatement = new AccountingStatement();
		finStatement.setAccountingStatement(accStatement);//ALPC7

		AccountingActivity accActivity = new AccountingActivity();
		accStatement.addAccountingActivity(accActivity);
		String accountNumber = 
			(NbaOliConstants.OLI_ACCTDBCRTYPE_CREDIT == debitCredit) ? extractVO.getCreditAccount() : extractVO.getDebitAccount(); //NBA228
		accActivity.setAccountNumber(accountNumber);
		accActivity.setAccountDebitCreditType(debitCredit);
		accActivity.setAccountAmount(accountAmount);//SPR1726
		txLife1225.getOLifE().getHoldingAt(0).getPolicy().addFinancialActivity(finActivity); //NBA228
		txLife1225.getOLifE().addFinancialStatement(finStatement);

		return txLife1225;
	}
	/**
	 * This method puts Transaction 1225 into each NbaCheckDepositAccountingExtractVO.
	 * @param dataRecord An ArrayList
	 * @param debit1225 NbaTXLife object
	 * @param credit1225 NbaTXLife object
	 * @param Connection conn
	 * @throws NbaBaseException
	 */
	protected void createExtract(ArrayList dataRecord, NbaTXLife debit1225, NbaTXLife credit1225, Connection conn) throws NbaBaseException, SQLException { //NBA103
		NbaExtractDataBaseAccessor aNbaExtractDataBaseAccessor = NbaExtractDataBaseAccessor.getInstance();
		// SPR3290 code deleted
		for (int i = 0; i <dataRecord.size(); i++ ) {
			if(isCheckDeposit()){//NBA115
				NbaCheckDepositAccountingExtractVO element = (NbaCheckDepositAccountingExtractVO) dataRecord.get(i);
				//SPR1726 line deleted
				createCheckDepoitExtract(element, debit1225, aNbaExtractDataBaseAccessor, conn);
				//SPR1726 line deleted
				createCheckDepoitExtract(element, credit1225, aNbaExtractDataBaseAccessor, conn);
			}else {//NBA115 begin
				NbaCreditCardAccountingExtractVO element = (NbaCreditCardAccountingExtractVO) dataRecord.get(i);
				createCreditCardDepositExtract(element, debit1225, aNbaExtractDataBaseAccessor, conn);
				createCreditCardDepositExtract(element, credit1225, aNbaExtractDataBaseAccessor, conn);
			}//NBA115 end
		}
		conn.commit();
	}	

	/**
	 * This method updates extract table.
	 * @param element NbaCheckDepositAccountingExtractVO object
	 * @param nbaTXLife NbaTXLife object
	 * @param aNbaExtractDataBaseAccessor NbaExtractDataBaseAccessor object
	 * @param conn Connection object
	 * @throws NbaBaseException
	 */
	//NBA115 new method
	protected void createCreditCardDepositExtract(NbaCreditCardAccountingExtractVO element, NbaTXLife nbaTXLife, NbaExtractDataBaseAccessor aNbaExtractDataBaseAccessor, Connection conn) throws NbaBaseException{
		element.setXml_Data(nbaTXLife.toXmlString());
		aNbaExtractDataBaseAccessor.insert(element, conn);
	}

	/**
	 * This method updates extract table.
	 * @param element NbaCheckDepositAccountingExtractVO object
	 * @param nbaTXLife NbaTXLife object
	 * @param aNbaExtractDataBaseAccessor NbaExtractDataBaseAccessor object
	 * @param conn Connection object
	 * @throws NbaBaseException
	 */
	protected void createCheckDepoitExtract(NbaCheckDepositAccountingExtractVO element, NbaTXLife nbaTXLife, NbaExtractDataBaseAccessor aNbaExtractDataBaseAccessor, Connection conn) throws NbaBaseException{
		element.setXml_Data(nbaTXLife.toXmlString());
		aNbaExtractDataBaseAccessor.insert(element, conn);
	}

	/**
	 * This method retrieves the systemId from NBA_DEOSIT_ACCOUNTING table and updates all NbaCheckDepositAccountingExtractVO
	 * available in the arraylist passed as parameter with this systemId.
	 * @param depositExtractVOlist ArrayList contains NbaCheckDepositAccountingExtractVOs
	 */
	//SPR1726 new method
	protected void updateSystemID(ArrayList depositExtractVOlist) throws NbaBaseException {
		if (depositExtractVOlist.size() > 0) {
			if(isCheckDeposit()){//NBA115
				NbaCheckDepositAccountingExtractVO element = (NbaCheckDepositAccountingExtractVO) depositExtractVOlist.get(0);
				NbaDepositAccountingExtractsVO nbaDepositAccountingExtractsVO = getDepositAccountInfo(element.getCompanyCode(), "FALSE", CHECK_PAYMENT, element.getLocationId()); //NBA115 NBA228
				for (int j = 0; j < depositExtractVOlist.size(); j++) {
					element = (NbaCheckDepositAccountingExtractVO) depositExtractVOlist.get(j);
					element.setSystemIDKey(nbaDepositAccountingExtractsVO.getSystemID());
				}
			}else {//NBA115 begin
				NbaCreditCardAccountingExtractVO element = (NbaCreditCardAccountingExtractVO) depositExtractVOlist.get(0);
				NbaDepositAccountingExtractsVO nbaDepositAccountingExtractsVO = getDepositAccountInfo(element.getCompanyCode(), "FALSE", CREDIT_CARD_PAYMENT);//NBA115
				for (int j = 0; j < depositExtractVOlist.size(); j++) {
					element = (NbaCreditCardAccountingExtractVO) depositExtractVOlist.get(j);
					element.setSystemIDKey(nbaDepositAccountingExtractsVO.getSystemID());
				}//NBA115 end
			}
		}
	}

	/**
	 * This method created accounting extracts for check deposits in the nbA extract database.
	 * @param depositExtractsList ArrayList contains the list of NbaCheckDepositAccountingExtractVO
	 * @param correctedDeposit boolean to indicated whether it's a new deposit or correction in existing deposit
	 * @throws NbaBaseException
	 */
	//AXAL3.7.23 new method 
	public void axaCreateAccountingExtractForCheckDeposit(List depositExtractsList, NbaUserVO user, boolean correctedDeposit)throws NbaBaseException {
		AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_TRANSMIT_CHECK_DEPOSIT_INFO,
				user, null, null, depositExtractsList);
		webServiceInvoker.execute();
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
	 * @return
	 */
	public boolean isCheckDeposit() {
		return checkDeposit;
	}

	/**
	 * @param b
	 */
	public void setCheckDeposit(boolean b) {
		checkDeposit = b;
	}
}
