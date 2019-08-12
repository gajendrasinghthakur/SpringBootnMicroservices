package com.csc.fsg.nba.datamanipulation; //NBA201

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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.html.support.NbaDataObjectRenderer;
import com.csc.fsg.nba.html.support.NbaHTMLHelper;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.AirSportsExp;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.AviationExp;
import com.csc.fsg.nba.vo.txlife.AviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr;
import com.csc.fsg.nba.vo.txlife.BallooningExp;
import com.csc.fsg.nba.vo.txlife.Banking;
import com.csc.fsg.nba.vo.txlife.BankingExtension;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.Client;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.EMailAddress;
import com.csc.fsg.nba.vo.txlife.Employment;
import com.csc.fsg.nba.vo.txlife.FamilyIllness;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.Height2;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.ImpairmentInfo;
import com.csc.fsg.nba.vo.txlife.Intent;
import com.csc.fsg.nba.vo.txlife.Investment;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.LifeStyleActivity;
import com.csc.fsg.nba.vo.txlife.MedicalCondition;
import com.csc.fsg.nba.vo.txlife.MedicalExam;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.OrganizationFinancialData;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Payout;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.Phone;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.PrescriptionDrug;
import com.csc.fsg.nba.vo.txlife.PriorName;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.Risk;
import com.csc.fsg.nba.vo.txlife.RiskExtension;
import com.csc.fsg.nba.vo.txlife.SubAccount;
import com.csc.fsg.nba.vo.txlife.SubstanceUsage;
import com.csc.fsg.nba.vo.txlife.TrackingInfo;
import com.csc.fsg.nba.vo.txlife.Violation;
import com.csc.fsg.nba.vo.txlife.Weight2;

/**
 * NbaUpdateContract stores information to an NbaTXLife object. A static initializer 
 * method generates a Map containing the variable names that may be used and the 
 * Method objects used to access them. Map entries are present for all methods of 
 * the class whose method name starts with the string "store" and which accept an 
 * NbaOinkRequest as an argument. This Map of variables is returned to the 
 * NbaOinkDataAccess when the NbaTXLife destination is initialized.
 *
 * Values from the NbaOinkRequest are stored in the NbaTXLife, up to the limit in 
 * the count field, into fields that satisfy the variable qualifier and filters. 
 * <p>  
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>NBA021</td><td>Version 2</td><td>Initial Development</td></tr>
 * <tr><td>NBA059</td><td>Version 3</td><td>Jet Suitability</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture changes</td></tr>
 * <tr><td>NBA041</td><td>Version 3</td><td>Billing Business Function</td></tr>
 * <tr><td>NBA053</td><td>Version 3</td><td>Application Update Enhancement</td></tr>
 * <tr><td>SPR1335</td><td>Version 3</td><td>Vantage Beneficiary Changes</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD2.8</td></tr>
 * <tr><td>SPR1766</td><td>Version 4</td><td>Correct GovtIDTc logic</td></tr>
 * <tr><td>SPR1771</td><td>Version 4</td><td>Default the UnitsTypeInd to false</td></tr>
 * <tr><td>SPR1778</td><td>Version 4</td><td>Correct problems with SmokerStatus and RateClass in Automated Underwriting, and Validation.</td></tr> 
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACP002</td><td>Version 4</td><td>IU-Driver (Drv/Cmn)</td></tr> 
 * <tr><td>SPR1430</td><td>Version 4</td><td>Banking Object Issues</td></tr>
 * <tr><td>ACN007</td><td>Version 4</td><td>Reflexive Questioning</td></tr>
 * <tr><td>NBA115</td><td>Version 5</td><td>Credit card payment and authorization</td></tr>
 * <tr><td>SPR2610</td><td>Version 5</td><td>Problems with Height and Weight for Person</td></tr>
 * <tr><td>SPR2574</td><td>Version 5</td><td>Application Entry Page is displaying error message Quality Check Mode Results, on a copied contract.</td></tr> 
 * <tr><td>SPR2722</td><td>Version 6</td><td>Application Entry Creates Multiple Primary Producer Relations Instead of One Primary and Others Additional Writing.</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7 </td></tr>
 * <tr><td>SPR3329</td><td>Version 7</td><td>Prevent erroneous "Retrieve variable name is invalid" messages from being generated by OINK</td></tr>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * <tr><td>SPR3353</td><td>Version 8</td><td>OINK problem handling multiple requirements in the same XML</td></tr>
 * <tr><td>AXAL3.7.03</td><td>AXA Life Phase 1</td><td>Informals</td></tr>
 * </table>
 * </p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.datamanipulation.NbaContractDataAccess
 * @see com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess
 * @see com.csc.fsg.nba.datamanipulation.NbaOinkRequest
 * @since New Business Accelerator - Version 2 
 */

public class NbaUpdateContract extends NbaContractDataAccess {
	static HashMap variables = new HashMap();
	private static NbaLogger logger = null;
	static HashMap riskVariables = new HashMap();
	static HashMap riskExtensionVariables = new HashMap();
	static {
		NbaUpdateContract aNbaUpdateContract = new NbaUpdateContract();
		String thisClassName = aNbaUpdateContract.getClass().getName();
		Method[] allMethods = aNbaUpdateContract.getClass().getDeclaredMethods();
		for (int i = 0; i < allMethods.length; i++) {
			Method aMethod = allMethods[i];
			String aMethodName = aMethod.getName();
			if (aMethodName.startsWith("store")) {
				Class[] parmClasses = aMethod.getParameterTypes();
				if (parmClasses.length == 1 && parmClasses[0].getName().equals("com.csc.fsg.nba.datamanipulation.NbaOinkRequest")) {
					Object[] args = { thisClassName, aMethod };
					variables.put(aMethodName.substring(5).toUpperCase(), args);
				}
			}
		}
	}
	protected com.csc.fsg.nba.html.support.NbaHTMLHelper nbaHTMLHelper;
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return the logger implementation
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaUpdateContract.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaUpdateContract could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * Answer the NbaHTMLHelper instance.
	 */
	protected NbaHTMLHelper getNbaHTMLHelper() {
		if (nbaHTMLHelper == null) {
			nbaHTMLHelper = new NbaHTMLHelper();
		}
		return nbaHTMLHelper;
	}
	/**
	 * Obtain the Method to be invoked for a variable for a RiskExtension object.
	 * @param aRiskExtension - a RiskExtension object
	 * @param variableName - the variable name
	 */
	protected Object getRiskExtensionMethod(RiskExtension aRiskExtension, String variableName) {
		if (riskExtensionVariables.size() < 1) {
			// SPR3290 code deleted
			Method[] allMethods = aRiskExtension.getClass().getDeclaredMethods();
			String stringClassName = String.class.getName();
			for (int i = 0; i < allMethods.length; i++) {
				Method aMethod = allMethods[i];
				String aMethodStringName = aMethod.getName();
				if (aMethodStringName.startsWith("set")) {
					Class[] parmClasses = aMethod.getParameterTypes();
					if (parmClasses.length == 1 && parmClasses[0].getName().equals(stringClassName)) {
						Object value = aMethod;
						riskExtensionVariables.put(aMethodStringName.substring(3).toUpperCase(), value);
					}
				}
			}
		}
		return riskExtensionVariables.get(variableName.toUpperCase());
	}
	/**
	 * Obtain the Method to be invoked for a variable for a Risk object.
	 * @param aRisk - a Risk object
	 * @param variableName - the variable name
	 */
	protected Object getRiskMethod(Risk aRisk, String variableName) {
		if (riskVariables.size() < 1) {
			// SPR3290 code deleted
			Method[] allMethods = aRisk.getClass().getDeclaredMethods();
			String stringClassName = String.class.getName();
			for (int i = 0; i < allMethods.length; i++) {
				Method aMethod = allMethods[i];
				String aMethodStringName = aMethod.getName();
				if (aMethodStringName.startsWith("set")) {
					Class[] parmClasses = aMethod.getParameterTypes();
					if (parmClasses.length == 1 && parmClasses[0].getName().equals(stringClassName)) {
						Object value = aMethod;
						riskVariables.put(aMethodStringName.substring(3).toUpperCase(), value);
					}
				}
			}
		}
		return riskVariables.get(variableName.toUpperCase());
	}
	/**
	 * Answer a Map of the available variables. The keys to the map are the
	 * variable names. The values are an array containing the class name string
	 * and the Method to be invoked to retrieve the variable.
	 * @return methods
	 */
	public static Map getVariables() {
		return variables;
	}
	/**
	 * This method initializes superclass objects.
	 * @param getOLifE() com.csc.fsg.nba.vo.NbaTXLife;
	 */
	public void initializeObjects(NbaTXLife objNbaTXLife) throws NbaBaseException {
		if (objNbaTXLife == null) {
			throw new NbaBaseException("Invalid NbaTXLife");
		}
		setOLifE(objNbaTXLife);
		setNbaTXLife(objNbaTXLife);//NBA053
		setUpdateMode(true);
		initPartyIndices();
	}
	/**
	 * Set the value for AccountNumber. 
	 * OLifE.Holding.Banking.AccountNumber is the account number associated with Banking object.
	 * @param aNbaOinkRequest 
	 */
	public void storeAccountNumber(NbaOinkRequest aNbaOinkRequest) {
		//NBA041 deleted
		//NBA041 begin
		//begin NBA115
		Banking banking = null;
		String[] values = aNbaOinkRequest.getStringValues();
		int requested = aNbaOinkRequest.getCount();
		List bankingList = getBankingList(aNbaOinkRequest);
		int bankingCount = bankingList.size();

		for (int i = 0; i < requested; i++) {
			banking = null;
			if (values[i].equals("") && i >= bankingCount) {
				break;
			} else if (i < bankingCount) {
				banking = (Banking) bankingList.get(i);
			} else {
				banking = createBanking();
				if (CREDIT_CARD_PAYMENT.equalsIgnoreCase(aNbaOinkRequest.getQualifier())) {
					initCreditCardPayment(banking);
				}
			}
			banking.setAccountNumber(values[i]);
		}
		//end NBA115
		//NBA093 code deleted
		//NBA041 end
	}
	/**
	 * Set the value for AcctHolderName
	 * OLifE.Holding.Policy.AcctHolderName is the name of the holder of either the credit card 
	 * or bank account associated with electronic funds transfer, credit card billing, or 
	 * credit card payments. Stores multiple account holder names on first banking object matched
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeAcctHolderName(NbaOinkRequest aNbaOinkRequest) {
		//NBA041 deleted
		//NBA041 begin
		//NBA093 code deleted
		
		//begin NBA115
		String[] values = aNbaOinkRequest.getStringValues();
		Banking banking = null;
		int requested = aNbaOinkRequest.getCount();
		List bankingList = getBankingList(aNbaOinkRequest);
		if(!bankingList.isEmpty()){
			banking = (Banking) bankingList.get(0);
		}
		
		for (int i = 0; i < requested; i++) {
			boolean accHolderPresent = hasAcctHolderName(banking, i);
			if(values[i].equals("") && !accHolderPresent){
				continue;
			} else {
				if (banking == null) {
					banking = createBanking();
					if (CREDIT_CARD_PAYMENT.equalsIgnoreCase(aNbaOinkRequest.getQualifier())) {
						initCreditCardPayment(banking);
					}
				}
				setAcctHolderName(banking, values[i], i);
			}
		}
		//end NBA115
		// SPR1430 code deleted
		//NBA093 end
		// SPR1430 code deleted

		//NBA041 end
	}
	/**
	 * Set the value for AgentLicNum, an instance of CompanyProducerID
	 * with a specific producer type.
	 * OLifE().Party().Producer().CarrierAppointment().CompanyProducerID() is 
	 * the Producer identification number as issued by an insurance company.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeAgentLicNum(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		int requestCount = aNbaOinkRequest.getCount(); // SPR3290
		for (int i = 0; i < requestCount; i++) { // SPR3290
			if ("".equals(values[i]) // SPR3290
				&& !(hasParty(aNbaOinkRequest, i) && hasCarrierAppointment(aNbaOinkRequest, i, NbaOliConstants.OLI_PROTYPE_AGENT))) {
				{
					continue;
				}
			} else {
				CarrierAppointment carrierAppointment = getCarrierAppointment(aNbaOinkRequest, i);//NBA053
				if (carrierAppointment != null) {
					setActionUpdate(carrierAppointment);//NBA053
					carrierAppointment.setCompanyProducerID(values[i]);
				}
			}
		}
	}
	/**
	 * Set the value for ApplicationJurisdiction.
	 * OLifE.Holding.Policy.ApplicationInfo.ApplicationJurisdiction is the
	 * state (jurisdiction) where the Application was signed.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeApplicationJurisdiction(NbaOinkRequest aNbaOinkRequest) {
		if ((aNbaOinkRequest.getStringValue() == null) || (aNbaOinkRequest.getStringValue().length() == 0)) {
			return;
		}
		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			api.setApplicationJurisdiction(aNbaOinkRequest.getStringValue());
		}
	}
	/**
	 * Set the value for AutomaticPremium
	 * if value is "1" set OLifE.Holding.Policy.PaymentMethod to "19".
	 * A value of false is set otherwise.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeAutomaticPremium(NbaOinkRequest aNbaOinkRequest) {
		Policy policy = getPolicy();
		if (policy != null) {
			if (aNbaOinkRequest.getStringValue().equals("1"))
				policy.setPaymentMethod(NbaOliConstants.OLI_PAYMETH_PERMANENTAPP);
			else
				policy.deletePaymentMethod();
		}
	}
	/**
	 * Set the value for BirthDate.
	 * OLifE.Party.PersonOrOrganization.Person.BirthDate is the 
	 * date of birth for the person.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeBirthDate(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					person.setBirthDate(NbaUtils.getDateFromStringInUSFormat(values[i]));
				}
			}
		}
	}
	/**
	 * Set the value for BirthState, an instance of BirthJurisdictionTc.
	 * OLifE.Party.PersonOrOrganization.Person.PersonExtension.BirthJurisdictionTc is 
	 * the State/province of birthplace.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeBirthState(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					int index_extension = getExtensionIndex(person.getOLifEExtension(), PERSON_EXTN);
					//if there is no OLifeExtension..create one
					if (index_extension == -1) {
						person.addOLifEExtension(NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PERSON));
						index_extension = person.getOLifEExtensionCount() - 1;

					}
					// NBA093 deleted 3 lines
					person.setBirthJurisdictionTC(values[i]); //NBA093
					setActionUpdate(person);//NBA053
					// NBA093 deleted line
				}
			}
		}
	}
	/**
	 * Set the value for CardExpDate, an instance of CreditCardExpDate.
	 * OLifE.Holding.Banking.CreditCardExpDate is the expiration date of the associated credit card when
	 * PaymentMethod is 'credit card billing,' or banking object represents a credit card payment
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeCardExpDate(NbaOinkRequest aNbaOinkRequest) {
		//NBA041 deleted
		//NBA041 begin
		//begin NBA115
		Banking banking = null;
		String[] values = aNbaOinkRequest.getStringValues();
		int requested = aNbaOinkRequest.getCount();
		List bankingList = getBankingList(aNbaOinkRequest);
		int bankingCount = bankingList.size();

		for (int i = 0; i < requested; i++) {
			banking = null;
			if (values[i].equals("") && i >= bankingCount) {
				break;
			} else if (i < bankingCount) {
				banking = (Banking) bankingList.get(i);
			} else {
				banking = createBanking();
				if (CREDIT_CARD_PAYMENT.equalsIgnoreCase(aNbaOinkRequest.getQualifier())) {
					initCreditCardPayment(banking);
				}
			}
			banking.setCreditCardExpDate(values[i]);
		}
		//end NBA115
		//NBA041 end
		}
	/**
	 * Set the value for Citizenship.
	 * OLifE.Party.PersonOrOrganization.Person.Citizenship is the 
	 * citizenship of the person.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeCitizenship(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					person.setCitizenship(values[i]);
				}
			}
		}
	}
	/**
	 * Set the value for CreditCardType.
	 * OLifE.Holding.Banking.CreditCardType holds the type of credit card 
	 * if OLifE.Holding.Banking.BankAcctType is OLI_BANKACCT_CREDCARD  
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeCreditCardType(NbaOinkRequest aNbaOinkRequest) {
		//NBA041 deleted
		//NBA041 begin
		//begin NBA115
		Banking banking = null;
		String[] values = aNbaOinkRequest.getStringValues();
		int requested = aNbaOinkRequest.getCount();
		List bankingList = getBankingList(aNbaOinkRequest);
		int bankingCount = bankingList.size();

		for (int i = 0; i < requested; i++) {
			banking = null;
			if (values[i].equals("") && i >= bankingCount) {
				break;
			} else if (i < bankingCount) {
				banking = (Banking) bankingList.get(i);
			} else {
				banking = createBanking();
				if (CREDIT_CARD_PAYMENT.equalsIgnoreCase(aNbaOinkRequest.getQualifier())) {
					initCreditCardPayment(banking);
				}
			}
			banking.setCreditCardType(values[i]);
		}
		//end NBA115
		//NBA041 end
	}

	/**
	 * Set the value for CurrentAmt.
	 * OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.Coverage.CurrentAmt is the
	 * amount of coverage -- the face amount of the rider without options.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeCurrentAmt(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		char[] parseFields = { '$', ',' };
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			int covIndx = getCoverageForParty(aNbaOinkRequest, i);
			//begin SPR1771
			if (covIndx > -1) {
				Coverage aCoverage = getLife().getCoverageAt(covIndx); // SPR3290
				aCoverage.setCurrentAmt(getNbaHTMLHelper().parseData(values[i], parseFields));
				setActionUpdate(aCoverage);
				CoverageExtension coverageExtension = NbaUtils.getFirstCoverageExtension(aCoverage);
				if (coverageExtension == null) {
					aCoverage.addOLifEExtension(NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_COVERAGE));
					coverageExtension = NbaUtils.getFirstCoverageExtension(aCoverage);
					setActionAdd(coverageExtension);
				}
				coverageExtension.setUnitTypeInd(false); 	//Set UnitsType to false to indicate that an amount is present
				setActionUpdate(coverageExtension);
			}
			//end SPR1771
		}
	}
	/**
	 * Set the value for FinActivityGrossAmt.
	 * OLifE.Holding.Policy.FinancialActivity.FinActivityGrossAmt is the
	 * amount of payment that accompanied application.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA093 NEW METHOD
	public void storeFinActivityGrossAmt(NbaOinkRequest aNbaOinkRequest) {
		char[] parseFields = { '$', ',' };
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			FinancialActivity activity = getFinancialActivity(i);
			if (values[i].equals("") || values[i].equalsIgnoreCase("$0.00")) {
				activity.deleteFinActivityGrossAmt();
			} else {
				setActionUpdate(activity);
				activity.setFinActivityGrossAmt(getNbaHTMLHelper().parseData(values[i], parseFields));
			}
		}
	}

	/**
	 * Set the value for Objective
	 * OLifE.Holding.Intent.IntentExtension.Objective is the investment objective.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA059 new method
	public void storeObjective(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			String partyID = getParty(aNbaOinkRequest, i).getId();
			Intent intent = getIntent(i, partyID);
			// NBA093 deleted 10 lines
			if (!values[i].equals("")) {
				intent.setObjective(values[i]);  //NBA093
			}
		}
	}
	/**
	 * Set the value for DeathBenefitOptType.
	 * OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.Coverage.DeathBenefitOptType is the
	 * option chosen for this contract which would affect the death proceeds, i.e. increasing, level.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeDeathBenefitOptType(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			int covIndx = getCoverageForParty(aNbaOinkRequest, i);
			if (covIndx == -1) {
				break;
			} else {
				Coverage aCoverage = getLife().getCoverageAt(covIndx); // SPR3290
				aCoverage.setDeathBenefitOptType(values[i]);
			}
		}
	}
	/**
	 * Set the value for CWAAmtFinActivity, an instance of CWAAmt.
	 * OLifE.Holding.Policy.FinancialActivity.FinancialActivityExtension.CWAActivity.CWAAmt is the
	 * amount amount of payment that accompanied application.
	 * @param aNbaOinkRequest - data request container
	 * @deprecated this method will be removed in a future release. 
	 * Use {@link #storeFinActivityGrossAmt(NbaOinkRequest)}
	 */
	public void storeCWAAmtFinActivity(NbaOinkRequest aNbaOinkRequest) {
		char[] parseFields = { '$', ',' };
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			FinancialActivity activity = getFinancialActivity(i);
			//begin NBA093
			if (values[i].equals("") || values[i].equalsIgnoreCase("$0.00")) {
				activity.deleteFinActivityGrossAmt();
			} else {
				setActionUpdate(activity);
				activity.setFinActivityGrossAmt(getNbaHTMLHelper().parseData(values[i], parseFields));
			}
			//end NBA093 
		}
	}
	/**
	 * Set the value for DriverLicNum.
	 * OLifE.Party.PersonOrOrganization.Person.DriversLicenseNum is a
	 * string representing the drivers license number of the person.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeDriverLicNum(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					person.setDriversLicenseNum(values[i]);
				}
			}
		}
	}
	/**
	 * Set the value for DriverLicState.
	 * OLifE.Party.PersonOrOrganization.Person.DriverLicState is a
	 * state in which the drivers license was issued.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeDriverLicState(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					person.setDriversLicenseState(values[i]);
				}
			}
		}
	}
	/**
	 * Obtain the value for EmployerName.
	 * OLifE.Party.Client.ClientExtension.EmployerName is a
	 * state in which the drivers license was issued.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeEmployerName(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasClient(aNbaOinkRequest, i))) {
				continue;
			} else {
				Client client = getClient(aNbaOinkRequest, i);
				if (client != null) {
					int index_extension = getExtensionIndex(client.getOLifEExtension(), CLIENT_EXTN);
					//if there is no extension, create one
					if (index_extension == -1) {
						client.addOLifEExtension(NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_CLIENT));
						index_extension = client.getOLifEExtensionCount() - 1;
					}
					OLifEExtension oli = client.getOLifEExtensionAt(index_extension);
					if (oli != null) {
						setActionUpdate(oli);//NBA053
						oli.getClientExtension().setEmployerName(values[i]);
						setActionUpdate(oli.getClientExtension());//NBA053
					}
				}
			}
		}
	}

	/**
	 * Set the value for a Coverage ProductCode. 
	 * Holding.Policy.Life.Coverage.ProductCode is a productCode. 
	 * @param aNbaOinkRequest - data request container
	 * @deprecated - use CoverageProductCode (NBA093)
	 */
	//NBA059 new method
	public void storeProductCode(NbaOinkRequest aNbaOinkRequest) {//NBA093
		storeCoverageProductCode(aNbaOinkRequest); //NBA093
	}
	/**
	 * Set the value for a Coverage ProductCode. 
	 * Holding.Policy.Life.Coverage.ProductCode is a productCode. 
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA093 new method
	public void storeCoverageProductCode(NbaOinkRequest aNbaOinkRequest) { //NBA093
		if (aNbaOinkRequest.getQualifier() != null && aNbaOinkRequest.getQualifier().equalsIgnoreCase(PARTY_INSURED)) {
			Life aLife = getLife();
			if (aLife != null) {
				for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
					int covIndx = getCoverageForParty(aNbaOinkRequest, i);
					if (covIndx == -1) {
						break;
					} else {
						Coverage aCoverage = aLife.getCoverageAt(covIndx); // SPR3290
						aCoverage.setProductCode(aNbaOinkRequest.getStringValues()[i]);
					}
				}
			}
		}
	}
	/**
	 * Set the value for a SubAccount ProductCode. 
	 * Holding.Investment.SubAccount.ProductCode is a productCode.
	 * @param aNbaOinkRequest - data request container
	 * @deprecated - use CoverageProductCode
	 */
	//NBA093 new method
	public void storeSubAccountProductCode(NbaOinkRequest aNbaOinkRequest) { 
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !hasProductCode(aNbaOinkRequest, i)) { 
				return;
			} else {
				Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE());  
				if (!holding.hasInvestment()) {
					holding.setInvestment(new Investment());
				}
				SubAccount subAccount = getSubAccount(i);
				subAccount.setProductCode(values[i]); 
			}
		}
	}
	/**
	 * Set the value for AllocPercent.
	 * Holding.Investment.SubAccount.AllocPercent is a
	 * fund percent.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA059 new method
	public void storeAllocPercent(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !hasAllocPercent(aNbaOinkRequest, i)) {
				return;
			} else {
				Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE()); //NBA044
				if (!holding.hasInvestment()) {
					holding.setInvestment(new Investment());
				}
				SubAccount subAccount = getSubAccount(i);
				subAccount.setAllocPercent(values[i]);
			}
		}
	}

	/**
	 * Set the value for ProductObjective
	 * Holding.Investment.SubAccount.ProductObjective is a
	 * product objective.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA059 new method
	public void storeProductObjective(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !hasProductObjective(aNbaOinkRequest, i)) {
				return;
			} else {
				Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE()); //NBA044
				if (!holding.hasInvestment()) {
					holding.setInvestment(new Investment());
				}
				SubAccount subAccount = getSubAccount(i);
				subAccount.setProductObjective(values[i]);
			}
		}
	}
	/**
	 * Set the value for EstNetWorth.
	 * OLifE.Party.EstNetWorth is the
	 * estimated net-worth as of date record was created.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeEstNetWorth(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		char[] parseFields = { '$', ',' };
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !hasParty(aNbaOinkRequest, i)) {
				continue;
			} else {
				Party party = getParty(aNbaOinkRequest, i);
				if (party != null) {
					party.setEstNetWorth(getNbaHTMLHelper().parseData(values[i], parseFields));
				}
			}
		}
	}
	/**
	 * Set the value for EstSalary.
	 * OLifE.Party.PersonOrOrganization.Person.EstSalary is defined as earning subject to FICA 
	 * including salary, tips, bonuses, self-employment income, other 
	 * employment income, and net earned business income.  All income 
	 * is before qualified retirement plan contributions (401K, etc.).
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeEstSalary(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		char[] parseFields = { '$', ',' };
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					person.setEstSalary(getNbaHTMLHelper().parseData(values[i], parseFields));
				}
			}
		}
	}
	/**
	 * Set the value for Exchange1035
	 * If the  value is true set 
	 * OLifE.Holding.Policy.FinancialActivity.FinActivityType to 11.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeExchange1035(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			FinancialActivity activity = getFinancialActivity(i);
			//NBA093 begin
			if (values[0].equalsIgnoreCase("FALSE")) {
				activity.setFinActivityType(NbaOliConstants.OLI_FINACT_PREMIUMINIT);
			} else {
				activity.setFinActivityType(NbaOliConstants.OLI_FINACT_1035INIT);
			}
			//NBA093 end
		}
	}
	/**
	 * Set the value for FirstName.
	 * OLifE.Party.PersonOrOrganization.Person.FirstName is the 
	 * first name of the person
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeFirstName(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					person.setFirstName(values[i]);
				}
			}
		}
	}
	/**
	 * Obtain the value for Gender.
	 * OLifE.Party.PersonOrOrganization.Person.Gender is the 
	 * gender of the person
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeGender(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					person.setGender(values[i]);
				}
			}
		}
	}
	/**
	 * Set the value for GovtID.
	 * OLifE.Party.GovtID is the string that represents the government ID.  
	 * In the USA,  this is the Social Security Number.   
	 * In South Africa, this is the TaxReferenceNumber which represents the 
	 * receiver of revenue for both a person or organization.   
	 * In Australia - for a person, this field is considered PRIVATE, 
	 * UNSHARABLE INFORMATION and thus it is not applicable.  This is due to
	 * government security requirements surrounding the privatization of the 
	 * field in Australia.   In Australia - for an organization, one of three 
	 * codes can be use 'ACN' or 'ARBN' or 'SIS'.  A company typically will only have one.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeGovtID(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		char[] parseFields = { '-' };
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !hasParty(aNbaOinkRequest, i)) {
				continue;
			} else {
				Party party = getParty(aNbaOinkRequest, i);
				if (party != null) {
					party.setGovtID(getNbaHTMLHelper().parseData(values[i], parseFields));
					if (!party.hasGovtIDTC()) { //NBA093
						party.setGovtIDTC(NbaOliConstants.OLI_GOVTID_SSN); //NBA093
					}
				}
			}
		}
	}
	/**
	 * Set the value for GovtIDTc.
	 * OLifE.Party.GovtIDTc is the Type code describing the contents of GovtID
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeGovtIDTc(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if ((values[i].equals("") || values[i].equals("-1")) && !hasParty(aNbaOinkRequest, i)) { //SPR1766
				continue;
			} else {
				Party party = getParty(aNbaOinkRequest, i);
				if (party != null) {
					party.setGovtIDTC(values[i]); //NBA093
				}
			}
		}
	}
	/**
	 * If the value is true set the value for GovtIDTc to 1.
	 * OLifE.Party.GovtIDTc is the Type code describing the contents of GovtID
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeGovtIDTc1(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !hasParty(aNbaOinkRequest, i)) {
				continue;
			} else {
				Party party = getParty(aNbaOinkRequest, i);
				if (party != null) {
					if (values[i].equalsIgnoreCase("TRUE")) {
						party.setGovtIDTC(NbaOliConstants.OLI_GOVTID_SSN); //NBA093
					}
				}
			}
		}
	}
	/**
	 * If the value is true set the value for GovtIDTc to 2.
	 * OLifE.Party.GovtIDTc is the Type code describing the contents of GovtID
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeGovtIDTc2(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !hasParty(aNbaOinkRequest, i)) {
				continue;
			} else {
				Party party = getParty(aNbaOinkRequest, i);
				if (party != null) {
					if (values[i].equalsIgnoreCase("TRUE")) {
						party.setGovtIDTC(NbaOliConstants.OLI_GOVTID_TID); //NBA093
					}
				}
			}
		}
	}
	/**
	 * Set the value for Height.
	 * OLifE.Party.PersonOrOrganization.Person.Height2.MeasureValue is the
	 * height of the person.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeHeight(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					//begin NBA093
					if (!person.hasHeight2()) {
						person.setHeight2(new Height2());
						person.getHeight2().setMeasureUnits(NbaOliConstants.OLI_MEASURE_USSTANDARD);	//SPR2610
						setActionAdd(person.getHeight2());
					}
					person.getHeight2().setMeasureValue(values[i]);
					setActionUpdate(person.getHeight2());
					//end NBA093
				}
			}
		}
	}
	/**
	 * Set the value for HeightInd.
	 * OLifE.Party.PersonOrOrganization.Person.Height2.MeasureUnits  
	 * identifies the unit of measure for height
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeHeightInd(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					//begin NBA093
					if (!person.hasHeight2()) {
						person.setHeight2(new Height2());
						setActionAdd(person.getHeight2());
					}
										
					person.getHeight2().setMeasureUnits(values[i]); 
					setActionUpdate(person.getHeight2()); 
					//end NBA093
				}
			}
		}
	}
	/**
	 * Set the value for SmokerStat.
	 * OLifE.Party.PersonOrOrganization.Person.SmokerStat is the 
	 * smoker stat of the person
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeSmokerStat(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				person.setSmokerStat(values[i]);//NBA093
				//NBA093 Code Deleted
			} else {
				break;
			}
		}
	}			
	/**
	 * Set the value for HomCity, an instance of City 
	 * for an Address with type code = 1.
	 * OLifE.Party.Address.City is the
	 * city of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeHomCity(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasAddress(aNbaOinkRequest, i, NbaOliConstants.OLI_ADTYPE_HOME))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_HOME);
					if (address != null) {
						address.setCity(values[i]);
					}
				}
			}
		}
	}
	/**
	 * Set the value for HomEmail, an instance of AddrLine 
	 * for an EMailAddress with type code = 2.
	 * OLifE.Party.EMailAddress.AddrLine is the
	 * string representing complete, mailable e-mail  address.  
	 * This is correctly defined as the 'SMTP' address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeHomEmail(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasEMail(aNbaOinkRequest, i, NbaOliConstants.OLI_EMAIL_PERSONAL))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					EMailAddress anEMailAddress = getEMailForType(aParty, NbaOliConstants.OLI_EMAIL_PERSONAL);
					if (anEMailAddress != null) {
						anEMailAddress.setAddrLine(values[i]);
					}
				}
			}
		}
	}
	/**
	 * Set the value for HomLine1, an instance of Line1 
	 * for an Address with type code = 1.
	 * OLifE.Party.Address.Line1 is the
	 * first line of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeHomLine1(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasAddress(aNbaOinkRequest, i, NbaOliConstants.OLI_ADTYPE_HOME))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_HOME);
					if (address != null) {
						address.setLine1(values[i]);
					}
				}
			}
		}
	}
	/**
	 * Set the value for HomLine2, an instance of Line2 
	 * for an Address with type code = 1.
	 * OLifE.Party.Address.Line2 is the
	 * second line of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeHomLine2(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasAddress(aNbaOinkRequest, i, NbaOliConstants.OLI_ADTYPE_HOME))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_HOME);
					if (address != null) {
						address.setLine2(values[i]);
					}
				}
			}
		}
	}
	/**
	 * Obtain the value for HomLine3, an instance of Line3 
	 * for an Address with type code = 1.
	 * OLifE.Party.Address.Line3 is the
	 * third line of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeHomLine3(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasAddress(aNbaOinkRequest, i, NbaOliConstants.OLI_ADTYPE_HOME))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_HOME);
					if (address != null) {
						address.setLine3(values[i]);
					}
				}
			}
		}
	}
	/**
	 * Set the value for HomPhone, a concatenation of
	 * OLifE.Party.Phone.AreaCode and OLifE.Party.Phone.DialNumber 
	 * for an Phone with type code = 1.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeHomPhone(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		char[] parseFields = { '(', ')', ' ', '-' };
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPhone(aNbaOinkRequest, i, NbaOliConstants.OLI_PHONETYPE_HOME))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Phone phone = getPhoneForType(aParty, NbaOliConstants.OLI_PHONETYPE_HOME);
					if (phone != null) {
						String phoneNum = getNbaHTMLHelper().parseData(values[i], parseFields);
						phone.setAreaCode(phoneNum.substring(0, 3));
						phone.setDialNumber(phoneNum.substring(3));
					}
				}
			}
		}
	}
	/**
	 * Set the value for HomState, an instance of AddressStateTc 
	 * for an Address with type code = 1.
	 * OLifE.Party.Address.AddressStateTc is the
	 * address state
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeHomState(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasAddress(aNbaOinkRequest, i, NbaOliConstants.OLI_ADTYPE_HOME))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_HOME);
					if (address != null) {
						address.setAddressStateTC(values[i]); //NBA093
					}
				}
			}
		}
	}
	/**
	 * Set the value for HomZip, an instance of Zip 
	 * for an Address with type code = 1.
	 * OLifE.Party.Address.Zip is the
	 * zip code, postal code, etc. (country dependent)
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeHomZip(NbaOinkRequest aNbaOinkRequest) {
		char[] parseFields = { '-' };
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasAddress(aNbaOinkRequest, i, NbaOliConstants.OLI_ADTYPE_HOME))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_HOME);
					if (address != null) {
						address.setZip(getNbaHTMLHelper().parseData(values[i], parseFields));
					}
				}
			}
		}
	}
	/**
	 * Set the value for InitialPremAmt.
	 * OLifE.Holding.Policy.PolicyExtension.InitialPremAmt is the
	 * Initial Premium Amount.  
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeInitialPremAmt(NbaOinkRequest aNbaOinkRequest) {
		char[] parseFields = { '$', ',' };
		Life life = getLife();
		if (life != null) {
			life.setInitialPremAmt(getNbaHTMLHelper().parseData(aNbaOinkRequest.getStringValue(), parseFields));
		}
	}
	/**
	 * Set the value for InitPaymentAmt.
	 * OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Annuity.InitPaymentAmt is the
	 * Initial Premium Amount.  It is applicable only to Annuity products.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeInitPaymentAmt(NbaOinkRequest aNbaOinkRequest) {
		char[] parseFields = { '$', ',' };
		Annuity annuity = getAnnuity();
		if (annuity != null) {
			annuity.setInitPaymentAmt(getNbaHTMLHelper().parseData(aNbaOinkRequest.getStringValue(), parseFields)); //SPR1072 line changed
		}
	}
	/**
	 * Set the value for InterestPercent.
	 * OLifE.Relation.InterestPercent is the percent of interest 
	 * the related object has in contract. It is used for % commission 
	 * split for agent; percentage of benefits to be received by 
	 * beneficiary, ownership percentage - company, holding, etc.  
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeInterestPercent(NbaOinkRequest aNbaOinkRequest) {
		String origPartyId = null;
		String[] values = aNbaOinkRequest.getStringValues();
		String roleType = aNbaOinkRequest.getQualifier();
		if (roleType.equals(PARTY_COBENEFICIARY) || roleType.equals(PARTY_BENEFICIARY)) {
			//begin SPR1335
			if (getOLifE().getSourceInfo().getFileControlID().equalsIgnoreCase(NbaConstants.SYST_VANTAGE)) {
				if (productType.equals(PRODUCT_ANNUITY)) {
					origPartyId = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE()).getId();
				} else {
					origPartyId = getCoverage(NbaOliConstants.OLI_COVIND_BASE).getId();
				}
			} else {
				origPartyId = getPartyForPrimaryIns().getId();
			}
		} else { //end SPR1335
			origPartyId = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE()).getId(); //NBA044
		}
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !hasParty(aNbaOinkRequest, i)) {
				continue;
			}
			Party relatedParty = getParty(aNbaOinkRequest, i);
			Relation relation = getRelation(roleType, i, origPartyId, relatedParty.getId());
			if (relation != null) {
				relation.setInterestPercent(values[i]);
				if ((roleType.equals(PARTY_PRIWRITINGAGENT)||roleType.equals(PARTY_ADDWRITINGAGENT)) && values[i].length() > 0) {//SPR2722
					relation.setVolumeSharePct(values[i]); //NBA093
					//NBA093 code deleted
				}
			}
		}
	}
	/**
	 * Set the value for IssueState, an instance of Jurisdiction.
	 * OLifE.Holding.Policy.Jurisdiction is the
	 * state (jurisdiction) of issue of the policy
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeIssueState(NbaOinkRequest aNbaOinkRequest) {
		getPolicy().setJurisdiction(aNbaOinkRequest.getStringValue());
	}
	/**
	 * Set the value for LastName.
	 * OLifE.Party.PersonOrOrganization.Person.LastName is the 
	 * last name of the person
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeLastName(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					person.setLastName(values[i]);
				}
			}
		}
	}
	/**
	 * Set the value for MarStat.
	 * OLifE.Party.PersonOrOrganization.Person.MarStat is the 
	 * maritial of the person
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeMarStat(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					person.setMarStat(values[i]);
				}
			}
		}
	}
	/**
	 * Set the value for MedCondType, an instance of MedCondReponse.
	 * OLifE.Party.Risk.MedicalCondition.MedicalConditionExtension.MedCondReponse is the
	 * response associated with a MedicalCondition which has a ConditionType which matches
	 * the value of the variable identifier name (excluding the "MedCondType" string).  
	 * For example, if the variable name is MedCondType123, the respose for the MedicalCondition
	 * which has a ConditionType of "123" is returned.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeMedCondType(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		int startPos = "MedCondType".length();
		String tagName = aNbaOinkRequest.getRootVariable();
		long medicalConditionType = Long.parseLong(tagName.substring(startPos, tagName.length()));
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				// SPR3290 code deleted
				Risk aRisk = getRisk(party);
				if (aRisk != null) {
					MedicalCondition condition = getMedicalCondition(aRisk, medicalConditionType);
					if (condition != null) {
						setActionUpdate(condition);//NBA053
						int index_extension = getExtensionIndex(condition.getOLifEExtension(), MEDICALCONDITION_EXTN);
						if (index_extension > -1) {
							OLifEExtension oli = condition.getOLifEExtensionAt(index_extension);
							setActionUpdate(oli);//NBA053
							oli.getMedicalConditionExtension().setMedCondResponse(values[i]);
							setActionUpdate(oli.getMedicalConditionExtension());//NBA053
						}
					}
				}
			}
		}
	}
	/**
	 * Set the value for MiddleName(.
	 * OLifE.Party.PersonOrOrganization.Person.MiddleName is the 
	 * middle name of the person
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeMiddleName(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					person.setMiddleName(values[i]);
				}
			}
		}
	}
	/**
	 * Set the value for Occupation.
	 * OLifE.Party.PersonOrOrganization.Person.Occupation is the 
	 * occupation for the person.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeOccupation(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					person.setOccupation(values[i]);
				}
			}
		}
	}
	/**
	 * Set the value for OffCity, an instance of City 
	 * for an Address with type code = 15.
	 * OLifE.Party.Address.City is the
	 * city of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeOffCity(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasAddress(aNbaOinkRequest, i, NbaOliConstants.OLI_ADTYPE_INDVWORKLOC))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_INDVWORKLOC);
					if (address != null) {
						address.setCity(values[i]);
					}
				}
			}
		}
	}
	/**
	 * Set the value for OffEmail, an instance of AddrLine 
	 * for an EMailAddress with type code = 1.
	 * OLifE.Party.EMailAddress.AddrLine is the
	 * string representing complete, mailable e-mail  address.  
	 * This is correctly defined as the 'SMTP' address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeOffEmail(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasEMail(aNbaOinkRequest, i, NbaOliConstants.OLI_EMAIL_BUSINESS))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					EMailAddress anEMailAddress = getEMailForType(aParty, NbaOliConstants.OLI_EMAIL_BUSINESS);
					if (anEMailAddress != null) {
						anEMailAddress.setAddrLine(values[i]);
					}
				}
			}
		}
	}
	/**
	 * Set the value for OffLine1, an instance of Line1 
	 * for an Address with type code = 15.
	 * OLifE.Party.Address.Line1 is the
	 * first line of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeOffLine1(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasAddress(aNbaOinkRequest, i, NbaOliConstants.OLI_ADTYPE_INDVWORKLOC))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_INDVWORKLOC);
					if (address != null) {
						address.setLine1(values[i]);
					}
				}
			}
		}
	}
	/**
	 * Set the value for OffLine2, an instance of Line2 
	 * for an Address with type code = 15.
	 * OLifE.Party.Address.Line2 is the
	 * first line of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeOffLine2(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasAddress(aNbaOinkRequest, i, NbaOliConstants.OLI_ADTYPE_INDVWORKLOC))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_INDVWORKLOC);
					if (address != null) {
						address.setLine2(values[i]);
					}
				}
			}
		}
	}
	/**
	 * Set the value for OffLine3, an instance of Line3 
	 * for an Address with type code = 15.
	 * OLifE.Party.Address.Line3 is the
	 * first line of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeOffLine3(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasAddress(aNbaOinkRequest, i, NbaOliConstants.OLI_ADTYPE_INDVWORKLOC))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_INDVWORKLOC);
					if (address != null) {
						address.setLine3(values[i]);
					}
				}
			}
		}
	}
	/**
	 * Set the value for OffPhone, a concatenation of
	 * OLifE.Party.Phone.AreaCode and OLifE.Party.Phone.DialNumber 
	 * for an Phone with type code = 15.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeOffPhone(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		char[] parseFields = { '(', ')', ' ', '-' };
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPhone(aNbaOinkRequest, i, NbaOliConstants.OLI_PHONETYPE_BUS))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Phone phone = getPhoneForType(aParty, NbaOliConstants.OLI_PHONETYPE_BUS);
					if (phone != null) {
						String phoneNum = getNbaHTMLHelper().parseData(values[i], parseFields);
						phone.setAreaCode(phoneNum.substring(0, 3));
						phone.setDialNumber(phoneNum.substring(3));
					}
				}
			}
		}
	}
	/**
	 * Set the value for OffState, an instance of AddressStateTc 
	 * for an Address with type code = 15.
	 * OLifE.Party.Address.AddressStateTc is the
	 * address state
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeOffState(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasAddress(aNbaOinkRequest, i, NbaOliConstants.OLI_ADTYPE_INDVWORKLOC))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_INDVWORKLOC);
					if (address != null) {
						address.setAddressStateTC(values[i]); //NBA093
					}
				}
			}
		}
	}
	/**
	 * Set the value for OffZip, an instance of Zip 
	 * for an Address with type code = 15.
	 * OLifE.Party.Address.Zip is the
	 * zip code, postal code, etc. (country dependent)
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeOffZip(NbaOinkRequest aNbaOinkRequest) {
		char[] parseFields = { '-' };
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasAddress(aNbaOinkRequest, i, NbaOliConstants.OLI_ADTYPE_INDVWORKLOC))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_INDVWORKLOC);
					if (address != null) {
						address.setZip(getNbaHTMLHelper().parseData(values[i], parseFields));
					}
				}
			}
		}
	}
	/**
	 * Set the value for PaymentAmt.
	 * OLifE.Holding.Policy.PaymentAmt is the current modal payment/premium amount.  
	 * This is the amount for the overall policy, including any premiums associated 
	 * with coverages/riders/options.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storePaymentAmt(NbaOinkRequest aNbaOinkRequest) {
		char[] parseFields = { '$', ',' };
		Policy policy = getPolicy();
		if (policy != null) {
			getPolicy().setPaymentAmt(getNbaHTMLHelper().parseData(aNbaOinkRequest.getStringValue(), parseFields));
		}
	}
	/**
	 * Set the value for PaymentMethod.
	 * OLifE.Holding.Policy.PaymentMethod is the payment method.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storePaymentMethod(NbaOinkRequest aNbaOinkRequest) {
		long paymentMethod = Long.parseLong(aNbaOinkRequest.getStringValue());
		Policy policy = getPolicy();
		if (policy != null) {
			getPolicy().setPaymentMethod(paymentMethod);
			// SPR1430 code deleted
		}
	}
	/**
	 * Set the value for PaymentMode.
	 * OLifE.Holding.Policy.PaymentMode is the 
	 * frequency of  payment - monthly, quarterly, or annually, etc.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storePaymentMode(NbaOinkRequest aNbaOinkRequest) {

		Policy policy = getPolicy();

		if (policy != null) {
			getPolicy().setPaymentMode(aNbaOinkRequest.getStringValue());
		}
	}
	/**
	 * Set the value for PrevCity, an instance of City 
	 * for an Address with type code = 12.
	 * OLifE.Party.Address.City is the
	 * city of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storePrevCity(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasAddress(aNbaOinkRequest, i, NbaOliConstants.OLI_ADTYPE_PREVIOUS))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_PREVIOUS);
					if (address != null) {
						address.setCity(values[i]);
					}
				}
			}
		}
	}
	/**
	 * Set the value for PrevLine1, an instance of Line1 
	 * for an Address with type code = 12.
	 * OLifE.Party.Address.Line1 is the
	 * first line of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storePrevLine1(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasAddress(aNbaOinkRequest, i, NbaOliConstants.OLI_ADTYPE_PREVIOUS))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_PREVIOUS);
					if (address != null) {
						address.setLine1(values[i]);
					}
				}
			}
		}
	}
	/**
	 * Set the value for PrevLine2, an instance of Line2 
	 * for an Address with type code = 12.
	 * OLifE.Party.Address.Line2 is the
	 * second line of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storePrevLine2(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasAddress(aNbaOinkRequest, i, NbaOliConstants.OLI_ADTYPE_PREVIOUS))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_PREVIOUS);
					if (address != null) {
						address.setLine2(values[i]);
					}
				}
			}
		}
	}
	/**
	 * Set the value for PrevLine3, an instance of Line3 
	 * for an Address with type code = 12.
	 * OLifE.Party.Address.Line3 is the
	 * third line of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storePrevLine3(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasAddress(aNbaOinkRequest, i, NbaOliConstants.OLI_ADTYPE_PREVIOUS))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_PREVIOUS);
					if (address != null) {
						address.setLine3(values[i]);
					}
				}
			}
		}
	}
	/**
	 * Set the value for PrevState, an instance of AddressStateTc 
	 * for an Address with type code = 12.
	 * OLifE.Party.Address.AddressStateTc is the
	 * address state
	 * @param aNbaOinkRequest - data request container
	 */
	public void storePrevState(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasAddress(aNbaOinkRequest, i, NbaOliConstants.OLI_ADTYPE_PREVIOUS))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_PREVIOUS);
					if (address != null) {
						address.setAddressStateTC(values[i]); //NBA093
					}
				}
			}
		}
	}
	/**
	 * Set the value for PrevZip, an instance of Zip 
	 * for an Address with type code = 12.
	 * OLifE.Party.Address.Zip is the
	 * zip code, postal code, etc. (country dependent)
	 * @param aNbaOinkRequest - data request container
	 */
	public void storePrevZip(NbaOinkRequest aNbaOinkRequest) {
		char[] parseFields = { '-' };
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasAddress(aNbaOinkRequest, i, NbaOliConstants.OLI_ADTYPE_PREVIOUS))) {
				continue;
			} else {
				Party aParty = getParty(aNbaOinkRequest, i);
				if (aParty != null) {
					Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_PREVIOUS);
					if (address != null) {
						address.setZip(getNbaHTMLHelper().parseData(values[i], parseFields));
					}
				}
			}
		}
	}
	/**
	 * Set the value for QualPlanType.
	 * OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.QualPlanType or
	 * OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Annuity.QualPlanType is 
	 * Qualification plan type for this life policy.  Life products can be sold as 
	 * Tax Qualified products.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeQualPlanType(NbaOinkRequest aNbaOinkRequest) {
		if (getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isAnnuity()) { //NBA093
			Annuity annuity = getAnnuity();
			if (annuity != null) {
				annuity.setQualPlanType(aNbaOinkRequest.getStringValue());
			}
		} else {
			Life life = getLife();
			if (life != null) {
				life.setQualPlanType(aNbaOinkRequest.getStringValue());
			}
		}
	}
	/**
	 * Set the value for RelatedRefID.
	 * OLifE.Relation.RelatedRefID is an identifier that 
	 * the 'to object' uses to identify the 'from object'. 
	 * For instance, the Health insurance ID used by the 
	 * insurer for the insured.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeRelatedRefID(NbaOinkRequest aNbaOinkRequest) {
		String origPartyId = null;
		String[] values = aNbaOinkRequest.getStringValues();
		String roleType = aNbaOinkRequest.getQualifier();
		if (roleType.equals(PARTY_COBENEFICIARY) || roleType.equals(PARTY_BENEFICIARY)) {
			//begin SPR1335
			if (getOLifE().getSourceInfo().getFileControlID().equalsIgnoreCase(NbaConstants.SYST_VANTAGE)) {
				if (productType.equals(PRODUCT_ANNUITY)) {
					origPartyId = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE()).getId();
				} else {
					origPartyId = getCoverage(NbaOliConstants.OLI_COVIND_BASE).getId();
				}
			} else {
				origPartyId = getPartyForPrimaryIns().getId();
			}
		} else {//end SPR1335
			origPartyId = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE()).getId(); //NBA044
		}
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !hasParty(aNbaOinkRequest, i)) {
				continue;
			}
			Party relatedParty = getParty(aNbaOinkRequest, i);
			if (relatedParty == null) {
				break;
			} else {
				Relation relation = getRelation(roleType, i, origPartyId, relatedParty.getId());
				if (relation != null) {
					relation.setRelatedRefID(values[i]);
				}
			}
		}
	}
	/**
	 * Set the value for RelationRoleCode.
	 * OLifE.Relation.RelationRoleCode is the Role code of relationship.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeRelationRoleCode(NbaOinkRequest aNbaOinkRequest) {
		String origPartyId = null;
		String[] values = aNbaOinkRequest.getStringValues();
		String roleType = aNbaOinkRequest.getQualifier();
		if (roleType.equals(PARTY_COBENEFICIARY) || roleType.equals(PARTY_BENEFICIARY)) {
			//begin SPR1335
			if (getOLifE().getSourceInfo().getFileControlID().equalsIgnoreCase(NbaConstants.SYST_VANTAGE)) {
				if (productType.equals(PRODUCT_ANNUITY)) {
					origPartyId = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE()).getId();
				} else {
					origPartyId = getCoverage(NbaOliConstants.OLI_COVIND_BASE).getId();
				}
			} else {
				origPartyId = getPartyForPrimaryIns().getId();
			}
		} else { //end SPR1335
			origPartyId = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE()).getId(); //NBA044
		}
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !hasParty(aNbaOinkRequest, i)) {
				continue;
			}
			Party relatedParty = getParty(aNbaOinkRequest, i);
			if (relatedParty == null) {
				break;
			} else {
				Relation relation = getRelation(roleType, i, origPartyId, relatedParty.getId());
				if (relation != null) {
					relation.setRelationRoleCode(values[i]);
				}
			}
		}
	}
	/**
	 * Set the value for ReltoAnnOrIns.
	 * For Beneficiaries, return OLifE.Relation.BeneficiaryDesignation.
	 * Otherwise, return OLifE.Relation.RelationDescription.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeReltoAnnOrIns(NbaOinkRequest aNbaOinkRequest) {
		String origPartyId = null;
		String[] values = aNbaOinkRequest.getStringValues();
		String roleType = aNbaOinkRequest.getQualifier();
		if (roleType.equals(PARTY_COBENEFICIARY) || roleType.equals(PARTY_BENEFICIARY)) {
			//begin SPR1335
			if (getOLifE().getSourceInfo().getFileControlID().equalsIgnoreCase(NbaConstants.SYST_VANTAGE)) {
				if (productType.equals(PRODUCT_ANNUITY)) {
					origPartyId = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE()).getId();
				} else {
					origPartyId = getCoverage(NbaOliConstants.OLI_COVIND_BASE).getId();
				}
			} else {
				origPartyId = getPartyForPrimaryIns().getId();
			}
		} else { //end SPR1335
			origPartyId = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE()).getId(); //NBA044
		}
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !hasParty(aNbaOinkRequest, i)) {
				continue;
			}
			Party relatedParty = getParty(aNbaOinkRequest, i);
			if (relatedParty == null) {
				break;
			} else {
				Relation relation = getRelation(roleType, i, origPartyId, relatedParty.getId());
				if (relation != null) {
					if (roleType.equals(PARTY_OWNER))
						relation.setRelationDescription(values[i]);
					else
						relation.setBeneficiaryDesignation(values[i]);
				}
			}
		}
	}
	/**
	 * Set the value for ReplacementCode.
	 * OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.ReplacementCode.
	 * @param aNbaOinkRequest - data request container
	 * @deprecated this method will be removed in a future release. 
	 * Use {@link #storeReplacementType(NbaOinkRequest)}
	 */
	public void storeReplacementCode(NbaOinkRequest aNbaOinkRequest) {
		//NBA093 code deleted
		getPolicy().setReplacementType(aNbaOinkRequest.getStringValue()); //NBA093
		
	}
	/**
	 * Set the value for a RiskExtension Question value.
	 * Reflection is used to message the RiskExtension object
	 * with a method composed of the variable name preceded by "set".
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeRiskExtensionQuestions(NbaOinkRequest aNbaOinkRequest) throws InvocationTargetException, IllegalAccessException {
		Object methodObject = getRiskExtensionMethod(new RiskExtension(), aNbaOinkRequest.getRootVariable());
		// begin SPR3290
		if (methodObject == null) {
			getLogger().logError("Update variable name is invalid: " + aNbaOinkRequest.getVariable());
			return;
		}
		// end SPR3290
		String[] values = aNbaOinkRequest.getStringValues();
		String[] paramValues = new String[1];
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				Risk aRisk = getRisk(party);
				if (aRisk == null)
					continue;
				int index_extension = getExtensionIndex(aRisk.getOLifEExtension(), RISK_EXTN);
				if (index_extension == -1) {
					aRisk.addOLifEExtension(NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_RISK));
					index_extension = aRisk.getOLifEExtensionCount() - 1;
				}
				RiskExtension extension = aRisk.getOLifEExtensionAt(index_extension).getRiskExtension();
				if (extension != null) {
					setActionUpdate(extension);//NBA053
					paramValues[0] = values[i];
					((Method) methodObject).invoke(extension, paramValues);
					setActionUpdate(aRisk.getOLifEExtensionAt(index_extension));//NBA053
				}
			} else {
				break;
			}
		}
	}
	/**
	 * Set the value for ReplacementType.
	 * OLifE.Holding.Policy.ReplacementType.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA093 NEW METHOD
	public void storeReplacementType(NbaOinkRequest aNbaOinkRequest) {
		getPolicy().setReplacementType(aNbaOinkRequest.getStringValue()); //NBA093
		
	}
	/**
	 * Set the value for a Risk Question value.
	 * Reflection is used to message the Risk object with a method
	 * composed of the variable name preceded by "set".
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeRiskQuestions(NbaOinkRequest aNbaOinkRequest) throws InvocationTargetException, IllegalAccessException {
		Object methodObject = getRiskMethod(new Risk(), aNbaOinkRequest.getRootVariable());
		// begin SPR3290
		if (methodObject == null) {
		    if (getLogger().isWarnEnabled()){ //SPR3329
		        getLogger().logWarn("Retrieve variable name is invalid: " + aNbaOinkRequest.getVariable());  // SPR3329
			}//SPR3329			
			return;
		}
		// end SPR3290
		String[] values = aNbaOinkRequest.getStringValues();
		String[] paramValues = new String[1];
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				// SPR3290 code deleted
				Risk aRisk = getRisk(party);
				if (aRisk != null) {
					paramValues[0] = values[i];
					((Method) methodObject).invoke(aRisk, paramValues);
				}
			} else {
				break;
			}
		}
	}
	/**
	 * Set the value for SignedDate.
	 * OLifE.Holding.Policy.ApplicationInfo.SignedDate is the
	 * date application was signed.  
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeSignedDate(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			api.setSignedDate(NbaUtils.getDateFromStringInUSFormat(aNbaOinkRequest.getStringValue()));
		}
	}
	/**
	 * Set the value for Weight.
	 * OLifE.Party.PersonOrOrganization.Person.Weights.MeasureValue is the
	 * Weight of the person.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeWeight(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					//begin NBA093
					if (!person.hasWeight2()) {
						person.setWeight2(new Weight2());
						person.getWeight2().setMeasureUnits(NbaOliConstants.OLI_MEASURE_USSTANDARD);	//SPR2610					
						setActionAdd(person.getWeight2());
					}
					person.getWeight2().setMeasureValue(values[i]);
					setActionUpdate(person.getWeight2());
					//end NBA093
				}
			}
		}
	}

	/**
	 * Set the value for AlternateInd. 
	 * OLifE.Holding.Policy.PolicyExtension.AlternateInd
	 * @param aNbaOinkRequest 
	 */
	public void storeAlternateInd(NbaOinkRequest aNbaOinkRequest) {
		//NBA093 code deleted
		//NBA093 begin
		ApplicationInfo appInfo = getApplicationInfo();
		if (appInfo != null) {
			setActionUpdate(appInfo);//NBA053
			appInfo.setAlternateInd(aNbaOinkRequest.getStringValue());
			
		}
		//NBA093 end
	}

	/**
	 * Set the value for CarrierCode. 
	 * OLifE.Holding.Policy.CarrierCode is the carrier code for the contract.
	 * @param aNbaOinkRequest 
	 */
	public void storeCarrierCode(NbaOinkRequest aNbaOinkRequest) {
		getPolicy().setCarrierCode(aNbaOinkRequest.getStringValue());
	}

	/**
	 * Set the value for DivType.
	 * OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.DivType is the
	 * Dividend Option.  
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeDivType(NbaOinkRequest aNbaOinkRequest) {
		Life life = getLife();
		if (life != null) {
			life.setDivType(aNbaOinkRequest.getStringValue());
		}
	}

	/**
	 * Set the value for IncomeOption.
	 * OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Annuity.Payout.IncomeOption is 
	 * the Income option.  Used for life annuity. 
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeIncomeOption(NbaOinkRequest aNbaOinkRequest) {
		if (getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isAnnuity()) { //NBA093
			Annuity annuity = getAnnuity();
			if (annuity != null) {
				Payout aPayout = getPayout(0);
				aPayout.setIncomeOption(aNbaOinkRequest.getStringValue());
			}
		}
	}

	/**
	 * Set the value for IssueAge.
	 * OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.Coverage.LifeParticipant.IssueAge is the
	 * age of participant when coverage was issued.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeIssueAge(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		covloop : for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			int covIndx = getCoverageForParty(aNbaOinkRequest, i);
			if (covIndx == -1) {
				break;
			} else {
				String personPartyID = (getParty(aNbaOinkRequest, 0)).getId();
				Coverage aCoverage = getLife().getCoverageAt(covIndx); // SPR3290
				ArrayList participants = (aCoverage).getLifeParticipant();
				int partIndx;
				for (partIndx = 0; partIndx < participants.size(); partIndx++) {
					LifeParticipant aLifeParticipant = (LifeParticipant) participants.get(partIndx);
					if (personPartyID.equals(aLifeParticipant.getPartyID())) {
						aLifeParticipant.setIssueAge(values[i]);
						continue covloop;
					}
				}
			}
		}
	}

	/**
	 * Set the value for LifeCovTypeCode.
	 * OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.Coverage.LifeCovTypeCode is the
	 * type of coverage.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeLifeCovTypeCode(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			int covIndx = getCoverageForParty(aNbaOinkRequest, i);
			if (covIndx == -1) {
				break;
			} else {
				Coverage aCoverage = getLife().getCoverageAt(covIndx); // SPR3290
				aCoverage.setLifeCovTypeCode(values[i]);
			}
		}
	}

	/**
	 * Set the value for PendingContractStatus. 
	 * OLifE.Holding.Policy.PolicyExtension.PendingContractStatus
	 * @param aNbaOinkRequest 
	 */
	public void storePendingContractStatus(NbaOinkRequest aNbaOinkRequest) {
		int index_Extension = getExtensionIndex(getPolicy().getOLifEExtension(), POLICY_EXTN);
		if (index_Extension != -1) {
			if (aNbaOinkRequest.getStringValue().equals("")) {
				return;
			} else {
				getPolicy().addOLifEExtension(NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY));
				index_Extension = getPolicy().getOLifEExtensionCount() - 1;
			}
		}
		OLifEExtension oli = getPolicy().getOLifEExtensionAt(index_Extension);
		if (oli != null) {
			setActionUpdate(oli);//NBA053
			oli.getPolicyExtension().setPendingContractStatus(aNbaOinkRequest.getStringValue());
			setActionUpdate(oli.getPolicyExtension());//NBA053
		}
	}

	/**
	 * Set the value for TaxableStatus.
	 * OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Annuity.TaxableStatus  
	 * defines whether taxing is required - e.g. compulsory or voluntary.
	 * @param aNbaOinkRequest - data request container
	 */
	public void storeTaxableStatus(NbaOinkRequest aNbaOinkRequest) {
		if (getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isAnnuity()) { //NBA093
			Annuity annuity = getAnnuity();
			if (annuity != null) {
				annuity.setTaxableStatus(aNbaOinkRequest.getStringValue());
			}
		}
	}
	
	/**
	 * Set the value for ApplicationType.
	 * OLifE.Holding.Policy.ApplicationInfo.ApplicationType is the
	 * ApplicationType.  
	 * @param aNbaOinkRequest - data request container
	 */
		public void storeApplicationType(NbaOinkRequest aNbaOinkRequest) {
			if ((aNbaOinkRequest.getStringValue() == null) || (aNbaOinkRequest.getStringValue().length() == 0)) {
				return;
			}
			ApplicationInfo api = getApplicationInfo();
			if (api != null) {
				api.setApplicationType(aNbaOinkRequest.getStringValue());
			}
		}
	/**
	 * Set the value for FileControlID.
	 * OLifE.SourceInfo.FileControlID
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA064 New Method
	public void storeFileControlID(NbaOinkRequest aNbaOinkRequest) {
		if (aNbaOinkRequest.getStringValue() != null && aNbaOinkRequest.getStringValue().length() > 0) {
			if (getSourceInfo() != null) {
				getSourceInfo().setFileControlID(aNbaOinkRequest.getStringValue());
			}
		}
	}
	/**
	 * Set the value for RequestedPolDate.
	 * OLifE.Holding.Policy.ApplicationInfo.RequestedPolDate
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA064 New Method
	public void storeRequestedPolDate(NbaOinkRequest aNbaOinkRequest) {
		if (aNbaOinkRequest.getStringValue() != null && aNbaOinkRequest.getStringValue().length() > 0) {
			if (getApplicationInfo() != null) {
				getApplicationInfo().setRequestedPolDate(NbaUtils.getDateFromStringInUSFormat(aNbaOinkRequest.getStringValue()));
			}
		}
	}
	/**
	 * Set the value for IssueDate.
	 * OLifE.Holding.Policy.IssueDate
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA064 New Method
	public void storeIssueDate(NbaOinkRequest aNbaOinkRequest) {
		if (aNbaOinkRequest.getStringValue() != null && aNbaOinkRequest.getStringValue().length() > 0) {
			if (getPolicy() != null) {
				getPolicy().setIssueDate(NbaUtils.getDateFromStringInUSFormat(aNbaOinkRequest.getStringValue()));
			}
		}
	}
	/**
	 * Set the value for a Person RateClass.
	 * OLifE.Party.PersonOrOrganization.Person.PersonExtension.RateClass is the 
	 * rateclass of the person.
	 * @param aNbaOinkRequest - data request container
	 */
	//SPR1778 New Method
	public void storePersonRateClass(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					PersonExtension personExtn = NbaUtils.getFirstPersonExtension(person);
					if (personExtn == null) {
						OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PERSON);
						personExtn = olifeExt.getPersonExtension();
						setActionAdd(personExtn);
						person.addOLifEExtension(olifeExt);
					}
					personExtn.setRateClass(aNbaOinkRequest.getStringValue()); 
					setActionUpdate(personExtn);
				}
			}
		}
	}
	/**
	 * Set the value for a Person RateClassAppliedFor.
	 * OLifE.Party.PersonOrOrganization.Person.PersonExtension.RateClassAppliedFor is the 
	 * applied for rateclass of the person.
	 * @param aNbaOinkRequest - data request container
	 */
	//SPR1778 New Method
	public void storeRateClassAppliedFor(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					PersonExtension personExtn = NbaUtils.getFirstPersonExtension(person);
					if (personExtn == null) {
						OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PERSON);
						personExtn = olifeExt.getPersonExtension();
						setActionAdd(personExtn);
						person.addOLifEExtension(olifeExt);
					}
					personExtn.setRateClassAppliedFor(aNbaOinkRequest.getStringValue()); 
					setActionUpdate(personExtn);
				}
			}
		}
	}

	/**
	 * Set the value for BallooningHours.
	 * OLifE.Party.Risk.LifeStyleActivity.AirSportsExp.BalloonExp.NumberHours is the
	 * number of hours.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void storeBallooningHours(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		Party party = null;
		Risk risk = null;
		LifeStyleActivity activity = null;
		AviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr aviationExp =
			null;
		AirSportsExp airSportsExp = null;
		for (int i = 0; i < values.length; i++) {
			party = getParty(aNbaOinkRequest, i);
			if (party != null && party.hasRisk()) {
				risk = party.getRisk();
				if (risk.getLifeStyleActivityCount() > 0) {
					activity = risk.getLifeStyleActivityAt(0);
					if (activity
						.hasAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr()) {
						aviationExp =
							activity
								.getAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr();
						if (aviationExp.isAirSportsExp()) {
							airSportsExp = aviationExp.getAirSportsExp();
							if (airSportsExp.hasBallooningExp()) {
								airSportsExp.getBallooningExp().setNumberHours(
									values[i]);
								setActionUpdate(
									airSportsExp.getBallooningExp());
							} else {
								airSportsExp.setBallooningExp(
									new BallooningExp());
								airSportsExp.getBallooningExp().setNumberHours(
									values[i]);
								setActionAdd(airSportsExp.getBallooningExp());
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Set the value for TobaccoType.
	 * OLifE.Party.Risk.SubstanceUsage.TobaccoType is the tobacco type
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method	  
	public void storeSubstanceTobaccoType(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		Party party = null;
		Risk risk = null;
		SubstanceUsage subUsage = null;
		party = getParty(aNbaOinkRequest, 0);
		if (party != null && party.hasRisk()) {
			risk = getRisk(party);
			for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
				if (i < risk.getSubstanceUsageCount()) {
					subUsage = risk.getSubstanceUsageAt(i);
					subUsage.setTobaccoType(values[i]);
					setActionUpdate(subUsage);
				} else {
					subUsage = new SubstanceUsage();
					subUsage.setTobaccoType(values[i]);
					risk.addSubstanceUsage(subUsage);
					setActionAdd(subUsage);
				}
			}
		}
	}

	/**
	 * Set the value for LifeStyleActivityOther.
	 * OLifE.Holding.Party.Attachment.AttachmentData is the value of LifeStyleActivityOther. 
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method	  
	public void storeLifeStyleActivityOther(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		Party party = null;
		party = getParty(aNbaOinkRequest, 0);
		Attachment attachment = null;
		AttachmentData data = null;
		if (party != null) {
			for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
				if (i < party.getAttachmentCount()) {
					attachment = party.getAttachmentAt(i);
					attachment.getAttachmentData().setPCDATA(values[i]);
					setActionUpdate(attachment.getAttachmentData());
				} else {
					attachment = new Attachment();
					party.addAttachment(attachment);
					data = new AttachmentData();
					attachment.setAttachmentData(data);
					data.setPCDATA(values[i]);
					setActionAdd(attachment);
				}
			}
		}
	}

	/**
	 * Set the value for PorposeHolding.
	 * OLifE.Holding.Purpose is the Purpose for Holding . 
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method	  
	public void storePurposeHolding(NbaOinkRequest aNbaOinkRequest) {
		Holding aHolding = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE());
		aHolding.setPurpose(aNbaOinkRequest.getStringValue());
		setActionUpdate(aHolding);
	}

	/**
	 * Set the value for PurposeCoverage.
	 * OlifE.Holding.Policy.Life.Coverage.Purpose is the Purpose for Coverage . 
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP014 New Method	  
	public void storePurposeCoverage(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			int covIndx = getCoverageForParty(aNbaOinkRequest, i);
			if (covIndx == -1) {
				break;
			} else {
				Coverage aCoverage =
					getLife().getCoverageAt(covIndx); // SPR3290
				aCoverage.setPurpose(values[i]);
				setActionUpdate(aCoverage);
			}
		}
	}

	/**
	 * Set the value for ReqCategory.
	 * OLifE.Holding.Policy.RequirementInfo.ReqCategory is the Reqirement Category . 
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method	  
	public void storeReqCategory(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		List reqList = getRequirementInfos(aNbaOinkRequest); //SPR3353
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			RequirementInfo aRequirementInfo = getRequirementInfo(reqList, i); //SPR3353
			if (aRequirementInfo != null) {
				aRequirementInfo.setReqCategory(values[i]);
			}
		}
	}
	/**
	 * Update the value of AbbrName
	 * (OLifE.Party.Organization.AbbrName) 
	 * @param aNbaOinkRequest - data request container
	 */	
	//ACP002 New Method
	public void storeAbbrName(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				Organization organization = getOrganization(aNbaOinkRequest, i);
				if (organization != null) {
					organization.setAbbrName(values[i]);
				}
			}
		}
	}

	/**
	 * Update the value of EstabDate
	 * (OLifE.Party.Organization.EstabDate) 
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void storeEstabDate(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				Organization organization = getOrganization(aNbaOinkRequest, i);
				if (organization != null) {
					organization.setEstabDate(NbaUtils.getDateFromStringInUSFormat(values[i]));
				}
			}
		}
	}
	/**
	 * Update the value of NumOwners
	 * (OLifE.Party.Organization.NumOwners)
	 * @param aNbaOinkRequest - data request container
	 */			 
	//ACP002 New Method
	public void storeNumOwners(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				Organization organization = getOrganization(aNbaOinkRequest, i);
				if (organization != null) {
					organization.setNumOwners(values[i]);
				}
			}
		}
	}

	/**
	 * Update the value of OrgForm
	 * (OLifE.Party.Organization.OrgForm)
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void storeOrgForm(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				Organization organization = getOrganization(aNbaOinkRequest, i);
				if (organization != null) {
					organization.setOrgForm(values[i]);
				}
			}
		}
	}
	/**
	 * Update the value of EstGrossAnnualOtherIncome
	 * (OLifE.Party.Person.EstGrossAnnualOtherIncome)
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP014 New Method
	public void storeEstGrossAnnualOtherIncome(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					person.setEstGrossAnnualOtherIncome(values[i]);
				}
			}
		}
	}	
	
	/**
	 * Update the value of SmokingFrequencyNumber
	 * (OLifE.Party.Person.SmokingFrequencyNumber)
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void storeSmokingFrequencyNumber(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					person.setSmokingFrequencyNumber(values[i]);
				}
			}
		}
	}
	/**
	 * Update the value of CurrentAssetsAmt
	 * (OLifE.Party.Organization.OrganizationFinancialData.CurrentAssetsAmt)
	 * @param aNbaOinkRequest - data request container
	 */	
	//ACP002 New Method
	public void storeCurrentAssetsAmt(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				OrganizationFinancialData orgFinData = getOrganizationFinancialData(aNbaOinkRequest, i);
				if (orgFinData != null) {
					orgFinData.setCurrentAssetsAmt(values[i]);
				}
			}
		}
	}
	/**
	 * Update the value of CurrentLiabilitiesAmt
	 * (LifE.Party.Organization.OrganizationFinancialData.CurrentLiabilitiesAmt)
	 * @param aNbaOinkRequest - data request container
	 */	
	//ACP002 New Method
	public void storeCurrentLiabilitiesAmt(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				OrganizationFinancialData orgFinData = getOrganizationFinancialData(aNbaOinkRequest, i);
				if (orgFinData != null) {
					orgFinData.setCurrentLiabilitiesAmt(values[i]);
				}
			}
		}
	}
	/**
	 * Update the value of PrevYrNetIncomeAmt
	 * (OLifE.Party.Organization.OrganizationFinancialData.PrevYrNetIncomeAmt)
	 * @param aNbaOinkRequest - data request container
	 */	
	//ACP002 New Method
	public void storePrevYrNetIncomeAmt(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				OrganizationFinancialData orgFinData = getOrganizationFinancialData(aNbaOinkRequest, i);
				if (orgFinData != null) {
					orgFinData.setPrevYrNetIncomeAmt(values[i]);
				}
			}
		}
	}
	/**
	 * Update the value of PrevYrTaxableEarningsAmt
	 * (OLifE.Party.Organization.OrganizationFinancialData.PrevYrTaxableEarningsAmt)
	 * @param aNbaOinkRequest - data request container
	 */	
	//ACP014 New Method
	public void storePrevYrTaxableEarningsAmt(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				OrganizationFinancialData orgFinData = getOrganizationFinancialData(aNbaOinkRequest, i);
				if (orgFinData != null) {
					orgFinData.setPrevYrTaxableEarningsAmt(values[i]);
				}
			}
		}
	}
	/**
	 * Update the value of YrEndNetWorthAmt
	 * (OLifE.Party.Organization.OrganizationFinancialData.YrEndNetWorthAmt)
	 * @param aNbaOinkRequest - data request container
	 */	
	//ACP002 New Method
	public void storeYrEndNetWorthAmt(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				OrganizationFinancialData orgFinData = getOrganizationFinancialData(aNbaOinkRequest, i);
				if (orgFinData != null) {
					orgFinData.setYrEndNetWorthAmt(values[i]);
				}
			}
		}
	}
	/**
	 * Update the value of OccupClass
	 * (OLifE.Party.Risk.Employment.OccupClass)
	 * @param aNbaOinkRequest - data request container
	 */	
	//ACP002 New Method
	public void storeOccupClass(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				Employment employment = getEmployment(aNbaOinkRequest, i);
				if (employment != null) {
					employment.setOccupClass(values[i]);
				}
			}
		}
	}
	/**
	 * Update the value of AgeAtDeath
	 * (OLifE.Party.Risk.FamilyIllness.AgeAtDeath)
	 * @param aNbaOinkRequest - data request container
	 */	
	//ACP013 New Method
	public void storeAgeAtDeath(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				FamilyIllness familyIllness = getFamilyIllness(aNbaOinkRequest, i);
				if (familyIllness != null) {
					familyIllness.setAgeAtDeath(values[i]);
				}
			}
		}
	}
	/**
	 * Update the value of AgeIfLiving
	 * (OLifE.Party.Risk.FamilyIllness.AgeIfLiving)
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP013 New Method
	public void storeAgeIfLiving(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				FamilyIllness familyIllness = getFamilyIllness(aNbaOinkRequest, i);
				if (familyIllness != null) {
					familyIllness.setAgeIfLiving(values[i]);
				}
			}
		}
	}
	/**
	 * Update the value of RoleCodeDesc
	 * (OLifE.Party.Risk.FamilyIllness.RoleCodeDesc)
	 * @param aNbaOinkRequest - data request container
	 */	
	//ACP013 New Method
	public void storeRoleCodeDesc(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				FamilyIllness familyIllness = getFamilyIllness(aNbaOinkRequest, i);
				if (familyIllness != null) {
					familyIllness.setRoleCodeDesc(values[i]);
				}
			}
		}
	}
	/**
	 * Update the value of ActivityCountLastYear
	 * (OLifE.Party.Risk.LifeStyleActivity.ActivityCountLastYear)
	 * @param aNbaOinkRequest - data request container
	 */	
	//ACP016 New Method
	public void storeActivityCountLastYear(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				LifeStyleActivity lifeStyleActivity = getLifeStyleActivity(aNbaOinkRequest, i);
				if (lifeStyleActivity != null) {
					lifeStyleActivity.setActivityCountLastYear(values[i]);
				}
			}
		}
	}
	/**
	 * Update the value of ActivityCountNextYear
	 * (OLifE.Party.Risk.LifeStyleActivity.ActivityCountNextYear)
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void storeActivityCountNextYear(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				LifeStyleActivity lifeStyleActivity = getLifeStyleActivity(aNbaOinkRequest, i);
				if (lifeStyleActivity != null) {
					lifeStyleActivity.setActivityCountNextYear(values[i]);
				}
			}
		}
	}
	
	/**
	 * Update the value of ActivityCountTotal
	 * (OlifE.Party.Risk.LifeStyleActivity.ActivityCountTotal)
	 * @param aNbaOinkRequest - data request container
	 */	
	//ACP016 New Method
	public void storeActivityCountTotal(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				LifeStyleActivity lifeStyleActivity = getLifeStyleActivity(aNbaOinkRequest, i);
				if (lifeStyleActivity != null) {
					lifeStyleActivity.setActivityCountTotal(values[i]);
				}
			}
		}
	}	
	/**
	 * Update the value of LifeStyleActivityType
	 * (OLifE.Party.Risk.LifeStyleActivity.LifeStyleActivityType)
	 * @param aNbaOinkRequest - data request container
	 */					
	//ACP009 New Method
	public void storeLifeStyleActivityType(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				LifeStyleActivity lifeStyleActivity = getLifeStyleActivity(aNbaOinkRequest, i);
				if (lifeStyleActivity != null) {
					lifeStyleActivity.setLifeStyleActivityType(values[i]);
				}
			}
		}
	}
	
	/**
	 * Update the value of CompeteInd
	 * (OLifE.Party.Risk.LifeStyleActivity.AirSportsExp.CompeteInd)
	 * @param aNbaOinkRequest - data request container
	 */	
	//ACP016 New Method
	public void storeCompeteInd(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") ) {
				continue;
			} else {
				AirSportsExp airSportsExp = getAirSportsExp(aNbaOinkRequest, i);
				if (airSportsExp != null) {
					airSportsExp.setCompeteInd(values[i]);
				}
			}
		}
	}
	/**
	 * Update the value of SafetyStandardsInd
	 * (OLifE.Party.Risk.LifeStyleActivity.AirSportsExp.SafetyStandardsInd)
	 * @param aNbaOinkRequest - data request container
	 */	
	//ACP002 New Method
	public void storeSafetyStandardsInd(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				AirSportsExp airSportsExp = getAirSportsExp(aNbaOinkRequest, i);
				if (airSportsExp != null) {
					airSportsExp.setSafetyStandardsInd(values[i]);
				}
			}
		}
	}
	/**
	 * Update the value of AirCraftType
	 * (OLifE.Party.Risk.LifeStyleActivity.AviationExp.AirCraftType)
	 * @param aNbaOinkRequest - data request container
	 */	
	//ACP016 New Method
	public void storeAirCraftType(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				AviationExp aviationExp = getAviationExp(aNbaOinkRequest, i);
				if (aviationExp != null) {
					aviationExp.setAircraftType(values[i]);
				}
			}
		}
	}
	/**
	 * Update the value of AviationType
	 * (OLifE.Party.Risk.LifeStyleActivity.AviationExp.AviationType)
	 * @param aNbaOinkRequest - data request container
	 */	
	//ACP016 New Method
	public void storeAviationType(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				AviationExp aviationExp = getAviationExp(aNbaOinkRequest, i);
				if (aviationExp != null) {
					aviationExp.setAviationType(values[i]);
				}
			}
		}
	}
	/**
	 * Update the value of FlyingPurpose
	 * (OLifE.Party.Risk.LifeStyleActivity.AviationExp.FlyingPurpose)
	 * @param aNbaOinkRequest - data request container
	 */	
	//ACP016 New Method
	public void storeFlyingPurpose(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				AviationExp aviationExp = getAviationExp(aNbaOinkRequest, i);
				if (aviationExp != null) {
					aviationExp.setFlyingPurpose(values[i]);
				}
			}
		}
	}
	
	/**
	 * Update the value of HIghestQualificationLevel
	 * (OLifE.Party.Risk.LifeStyleActivity.AviationExp.HIghestQualificationLevel)
	 * @param aNbaOinkRequest - data request container
	 */	
	//ACP016 New Method
	public void storeHighestQualificationLevel(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				AviationExp aviationExp = getAviationExp(aNbaOinkRequest, i);
				if (aviationExp != null) {
					aviationExp.setHighestQualificationLevel(values[i]);
				}
			}
		}
	}
	/**
	 * Update the value of IFRInd
	 * (OLifE.Party.Risk.LifeStyleActivity.AviationExp.IFRInd)
	 * @param aNbaOinkRequest - data request container
	 */	
	//ACP016 New Method
	public void storeIFRInd(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				AviationExp aviationExp = getAviationExp(aNbaOinkRequest, i);
				if (aviationExp != null) {
					aviationExp.setIFRInd(values[i]);
				}
			}
		}
	}

	/**
	 * Set the value for AnnualSalary.
	 * OLifE.Party.Employment.AnnualSalary
	 * @param aNbaOinkRequest - data request container
	 */
	 //ACP002 New Method
	public void storeAnnualSalary(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasEmployment(aNbaOinkRequest, i))) {
				continue;
			} else {
				Employment Employment = getEmployment(aNbaOinkRequest, i);
				if (Employment != null) {
					Employment.setAnnualSalary(values[i]);
				}
			}
		}
	}
	
	/**
	 * Set the value for CorporateStockOwnedPct.
	 * OLifE.Party.Employment.CorporateStockOwnedPct
	 * @param aNbaOinkRequest - data request container
	 */
	 //ACP014 New Method
	public void storeCorporateStockOwnedPct(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasEmployment(aNbaOinkRequest, i))) {
				continue;
			} else {
				Employment Employment = getEmployment(aNbaOinkRequest, i);
				if (Employment != null) {
					Employment.setCorporateStockOwnedPct(values[i]);
				}
			}
		}
	}


	/**
	 * Set the value for HireDate.
	 * OLifE.Party.Employment.HireDate
	 * @param aNbaOinkRequest - data request container
	 */
	 //ACP002 New Method
	public void storeHireDate(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasEmployment(aNbaOinkRequest, i))) {
				continue;
			} else {
				Employment Employment = getEmployment(aNbaOinkRequest, i);
				if (Employment != null) {
					Employment.setHireDate(NbaUtils.getDateFromStringInUSFormat(values[i]));
				}
			}
		}
	}


	/**
	 * Set the value for TerminationDate.
	 * OLifE.Party.Employment.TerminationDate
	 * @param aNbaOinkRequest - data request container
	 */
	 //ACP002 New Method
	public void storeTerminationDate(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasEmployment(aNbaOinkRequest, i))) {
				continue;
			} else {
				Employment Employment = getEmployment(aNbaOinkRequest, i);
				if (Employment != null) {
					Employment.setTerminationDate(NbaUtils.getDateFromStringInUSFormat(values[i]));
				}
			}
		}
	}
	
	/**
	 * Set the value for FirstDiastolicBPReading.
	 * OLifE.Party.Risk.MedicalExam.FirstDiastolicBPReading
	 * @param aNbaOinkRequest - data request container
	 */
	 //ACP001 New Method
	public void storeFirstDiastolicBPReading(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasMedicalExam(aNbaOinkRequest, i))) {
				continue;
			} else {
				MedicalExam medicalExam = getMedicalExam(aNbaOinkRequest, i);
				if (medicalExam != null) {
					medicalExam.setFirstDiastolicBPReading(values[i]);
				}
			}
		}
	}	

	/**
	 * Set the value for SecondDiastolicBPReading.
	 * OLifE.Party.Risk.MedicalExam.SecondDiastolicBPReading
	 * @param aNbaOinkRequest - data request container
	 */
	 //ACP001 New Method
	public void storeSecondDiastolicBPReading(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasMedicalExam(aNbaOinkRequest, i))) {
				continue;
			} else {
				MedicalExam medicalExam = getMedicalExam(aNbaOinkRequest, i);
				if (medicalExam != null) {
					medicalExam.setSecondDiastolicBPReading(values[i]);
				}
			}
		}
	}	

	/**
	 * Set the value for ThirdDiastolicBPReading.
	 * OLifE.Party.Risk.MedicalExam.ThirdDiastolicBPReading
	 * @param aNbaOinkRequest - data request container
	 */
	 //ACP001 New Method
	public void storeThirdDiastolicBPReading(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasMedicalExam(aNbaOinkRequest, i))) {
				continue;
			} else {
				MedicalExam medicalExam = getMedicalExam(aNbaOinkRequest, i);
				if (medicalExam != null) {
					medicalExam.setThirdDiastolicBPReading(values[i]);
				}
			}
		}
	}	

	/**
	 * Set the value for FirstSystolicBPReading.
	 * OLifE.Party.Risk.MedicalExam.FirstSystolicBPReading
	 * @param aNbaOinkRequest - data request container
	 */
	 //ACP001 New Method
	public void storeFirstSystolicBPReading(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasMedicalExam(aNbaOinkRequest, i))) {
				continue;
			} else {
				MedicalExam medicalExam = getMedicalExam(aNbaOinkRequest, i);
				if (medicalExam != null) {
					medicalExam.setFirstSystolicBPReading(values[i]);
				}
			}
		}
	}	

	/**
	 * Set the value for SecondSystolicBPReading.
	 * OLifE.Party.Risk.MedicalExam.SecondSystolicBPReading
	 * @param aNbaOinkRequest - data request container
	 */
	 //ACP001 New Method
	public void storeSecondSystolicBPReading(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasMedicalExam(aNbaOinkRequest, i))) {
				continue;
			} else {
				MedicalExam medicalExam = getMedicalExam(aNbaOinkRequest, i);
				if (medicalExam != null) {
					medicalExam.setSecondSystolicBPReading(values[i]);
				}
			}
		}
	}	

	/**
	 * Set the value for ThirdSystolicBPReading.
	 * OLifE.Party.Risk.MedicalExam.ThirdSystolicBPReading
	 * @param aNbaOinkRequest - data request container
	 */
	 //ACP001 New Method
	public void storeThirdSystolicBPReading(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasMedicalExam(aNbaOinkRequest, i))) {
				continue;
			} else {
				MedicalExam medicalExam = getMedicalExam(aNbaOinkRequest, i);
				if (medicalExam != null) {
					medicalExam.setThirdSystolicBPReading(values[i]);
				}
			}
		}
	}	
	
	/**
	 * Set the value for PulseIrregularInd.
	 * OLifE.Party.Risk.MedicalExam.PulseIrregularInd
	 * @param aNbaOinkRequest - data request container
	 */
	 //ACP002 New Method
	public void storePulseIrregularInd(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasMedicalExam(aNbaOinkRequest, i))) {
				continue;
			} else {
				MedicalExam medicalExam = getMedicalExam(aNbaOinkRequest, i);
				if (medicalExam != null) {
					medicalExam.setPulseIrregularInd(values[i]);
				}
			}
		}
	}
	
	//	ACP007 deleted method public void storeTestType(NbaOinkRequest aNbaOinkRequest)	
	//	ACP007 deleted method public void storeTreatmentAmt(NbaOinkRequest aNbaOinkRequest)
	
	/**
	 * Set the value for PrescriptionDosageStrength.
	 * OLifE.Party.Risk.PrescriptionDrug.PrescriptionDosageStrength
	 * @param aNbaOinkRequest - data request container
	 */
	 //ACP002 New Method
	public void storePrescriptionDosageStrength(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPrescriptionDrug(aNbaOinkRequest, i))) {
				continue;
			} else {
				PrescriptionDrug prescriptionDrug = getPrescriptionDrug(aNbaOinkRequest, i);
				if (prescriptionDrug != null) {
					prescriptionDrug.setPrescriptionDosageStrength(values[i]);
				}
			}
		}
	}	
	
	/**
	 * Set the value for PrescriptionDosageUnit.
	 * OLifE.Party.Risk.PrescriptionDrug.PrescriptionDosageUnit
	 * @param aNbaOinkRequest - data request container
	 */
	 //ACP002 New Method
	public void storePrescriptionDosageUnit(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPrescriptionDrug(aNbaOinkRequest, i))) {
				continue;
			} else {
				PrescriptionDrug prescriptionDrug = getPrescriptionDrug(aNbaOinkRequest, i);
				if (prescriptionDrug != null) {
					prescriptionDrug.setPrescriptionDosageUnit(values[i]);
				}
			}
		}
	}	
	
	/**
	 * Set the value for SubstanceAmt.
	 * OLifE.Party.Risk.SubstanceUsage.SubstanceAmt
	 * @param aNbaOinkRequest - data request container
	 */
	 //ACP002 New Method
	public void storeSubstanceAmt(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasSubstanceUsage(aNbaOinkRequest, i))) {
				continue;
			} else {
				SubstanceUsage substanceUsage = getSubstanceUsage(aNbaOinkRequest, i);
				if (substanceUsage != null) {
					substanceUsage.setSubstanceAmt(values[i]);
				}
			}
		}
	}	
	
	/**
	 * Set the value for SubstanceDesc.
	 * OLifE.Party.Risk.SubstanceUsage.SubstanceDesc
	 * @param aNbaOinkRequest - data request container
	 */
	 //ACP002 New Method
	public void storeSubstanceDesc(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasSubstanceUsage(aNbaOinkRequest, i))) {
				continue;
			} else {
				SubstanceUsage substanceUsage = getSubstanceUsage(aNbaOinkRequest, i);
				if (substanceUsage != null) {
					substanceUsage.setSubstanceDesc(values[i]);
				}
			}
		}
	}
	
	/**
	 * Set the value for SubstanceEndDate.
	 * OLifE.Party.Risk.SubstanceUsage.SubstanceEndDate
	 * @param aNbaOinkRequest - data request container
	 */
	 //ACP002 New Method
	public void storeSubstanceEndDate(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasSubstanceUsage(aNbaOinkRequest, i))) {
				continue;
			} else {
				SubstanceUsage substanceUsage = getSubstanceUsage(aNbaOinkRequest, i);
				if (substanceUsage != null) {
					substanceUsage.setSubstanceEndDate(NbaUtils.getDateFromStringInUSFormat(values[i]));
				}
			}
		}
	}
	
	/**
	 * Set the value for SubstanceType.
	 * OLifE.Party.Risk.SubstanceUsage.SubstanceType
	 * @param aNbaOinkRequest - data request container
	 */
	 //ACP009 New Method
	public void storeSubstanceType(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasSubstanceUsage(aNbaOinkRequest, i))) {
				continue;
			} else {
				SubstanceUsage substanceUsage = getSubstanceUsage(aNbaOinkRequest, i);
				if (substanceUsage != null) {
					substanceUsage.setSubstanceType(values[i]);
				}
			}
		}
	}	
	
	/**
	 * Set the value for ViolationType.
	 * OLifE.Party.Risk.Violation.ViolationType
	 * @param aNbaOinkRequest - data request container
	 */
	 //ACP009 New Method
	public void storeViolationType(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasViolation(aNbaOinkRequest, i))) {
				continue;
			} else {
				Violation violation = getViolation(aNbaOinkRequest, i);
				if (violation != null) {
					violation.setViolationType(values[i]);
				}
			}
		}
	}

	/**
	 * Set the value for ViolationDate.
	 * OLifE.Party.Risk.Violation.ViolationDate
	 * @param aNbaOinkRequest - data request container
	 */
	 //ACP016 New Method
	public void storeViolationDate(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasViolation(aNbaOinkRequest, i))) {
				continue;
			} else {
				Violation violation = getViolation(aNbaOinkRequest, i);
				if (violation != null) {
					violation.setViolationDate(NbaUtils.getDateFromStringInUSFormat(values[i]));
				}
			}
		}
	}	
	/**
	 * Set the value for AppOwnerSignatureOK.
	 * OLifE.Holding.Policy.ApplicationInfo.AppOwnerSignatureOK 
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void storeAppOwnerSignatureOK(NbaOinkRequest aNbaOinkRequest) { //start loop for the no of occurrences
		String[] value = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { //check whether the value in request object is null or not			  	
			if (value[i] != null && value[i].length() > 0) {
				//check whether the applicationInfo object exist or not		
				if (getApplicationInfo() != null)
					// && getApplicationInfo().hasAppOwnerSignatureOK())
					{ //update the value for AppOwnerSignatureOK in the ApplicationInfo object.
					getApplicationInfo().setAppOwnerSignatureOK(value[i]);
				} //end if
			} //end if
		} //end for  	 
	} //end method    

	/**
	 * Set the value for AppProposedInsuredSignatureOK.
	 * OLifE.Holding.Policy.ApplicationInfo.AppProposedInsuredSignatureOK 
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP004 New Method
	public void storeAppProposedInsuredSignatureOK(NbaOinkRequest aNbaOinkRequest) { //start loop for the no of occurrences
		String[] value = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { //check whether the value in request object is null or not			  	
			if (value[i] != null && value[i].length() > 0) {
				//check whether the applicationInfo object exist or not		
				if (getApplicationInfo() != null)
					// && getApplicationInfo().hasAppProposedInsuredSignatureOK())
					{ //update the value for AppProposedInsuredSignatureOK in the ApplicationInfo object.
					getApplicationInfo().setAppProposedInsuredSignatureOK(value[i]);
				} //end if
			} //end if
		} //end for  	 
	} //end method

	/**
	 * Set the value for CarrierInputDate.
	 * OLifE.Holding.Policy.ApplicationInfo.CarrierInputDate 
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void storeCarrierInputDate(NbaOinkRequest aNbaOinkRequest) { //start loop for the no of occurrences
		String[] value = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { //check whether the value in request object is null or not			  	
			if (value[i] != null && value[i].length() > 0) {
				//check whether the applicationInfo object exist or not		
				if (getApplicationInfo() != null)
					// && getApplicationInfo().hasCarrierInputDate())
					{ //update the value for CarrierInputDate in the ApplicationInfo object.
					getApplicationInfo().setCarrierInputDate(NbaUtils.getDateFromStringInUSFormat(value[i]));
				} //end if
			} //end if
		} //end for  	 
	} //end method

	/**
	 * Set the value for CaseLocationDate.
	 * OLifE.Holding.Policy.ApplicationInfo.CaseLocationDate 
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void storeCaseLocationDate(NbaOinkRequest aNbaOinkRequest) { //start loop for the no of occurrences
		String[] value = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { //check whether the value in request object is null or not			  	
			if (value[i] != null && value[i].length() > 0) {
				//check whether the applicationInfo object exist or not		
				if (getApplicationInfo() != null)
					// && getApplicationInfo().hasCaseLocationDate())
					{ //update the value for CaseLocationDate in the ApplicationInfo object.
					getApplicationInfo().setCaseLocationDate(NbaUtils.getDateFromStringInUSFormat(value[i]));
				} //end if
			} //end if
		} //end for  	 
	} //end method

	/**
	 * Set the value for FormalAppInd.
	 * OLifE.Holding.Policy.ApplicationInfo.FormalAppInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void storeFormalAppInd(NbaOinkRequest aNbaOinkRequest) { //start loop for the no of occurrences
		String[] value = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { //check whether the value in request object is null or not			  	
			if (value[i] != null && value[i].length() > 0) {
				//check whether the applicationInfo object exist or not		
				if (getApplicationInfo() != null)
					// && getApplicationInfo().hasFormalAppInd())
					{ //update the value for FormalAppInd in the ApplicationInfo object.
					getApplicationInfo().setFormalAppInd(value[i]);
				} //end if
			} //end if
		} //end for  	 
	} //end method

	/**
	 * Set the value for HOCompletionDate.
	 * OLifE.Holding.Policy.ApplicationInfo.HOCompletionDate 
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void storeHOCompletionDate(NbaOinkRequest aNbaOinkRequest) { //start loop for the no of occurrences
		String[] value = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { //check whether the value in request object is null or not			  	
			if (value[i] != null && value[i].length() > 0) {
				//check whether the applicationInfo object exist or not		
				if (getApplicationInfo() != null)
					// && getApplicationInfo().hasHOCompletionDate())
					{ //update the value for HOCompletionDate in the ApplicationInfo object.
					getApplicationInfo().setHOCompletionDate(NbaUtils.getDateFromStringInUSFormat(value[i]));
				} //end if
			} //end if
		} //end for  	 
	} //end method

	/**
	 * Set the value for ReinsuranceInd.
	 * OLifE.Holding.Policy.ReinsuranceInd 
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void storeReinsuranceInd(NbaOinkRequest aNbaOinkRequest) { //start loop for the no of occurrences
		String[] value = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { //check whether the value in request object is null or not			  	
			if (value[i] != null && value[i].length() > 0) {
				//check whether the policy object exist or not		  
				if (getPolicy() != null) // && getPolicy().hasReinsuranceInd())
					{ //update the value for ReinsuranceInd in the policy object.
					getPolicy().setReinsuranceInd(value[i]);
				} //end if
			} //end if
		} //end for  	 
	} //end method	
	/**
	 * Set the value for PriorFirstName.
	 * Olife.Party.PriorName.FirstName 
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP006 New Method
	public void storePriorFirstName(NbaOinkRequest aNbaOinkRequest) { //start loop for the no of occurrences
		String[] value = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { //check whether the value in request object is null or not			  	
			if (value[i] != null && value[i].length() > 0) {
				//check whether the priorName object exist or not
				PriorName priorName = getPriorName(aNbaOinkRequest, i);
				if (priorName != null) // && priorName.hasFirstName())
					{ //update the value for FirstName in the priorName object.
					priorName.setFirstName(value[i]);
				} //end if
			} //end if
		} //end for  	 
	} //end method
	/**
	 * Set the value for PriorLastName.
	 * Olife.Party.PriorName.LastName 
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP006 New Method
	public void storePriorLastName(NbaOinkRequest aNbaOinkRequest) { //start loop for the no of occurrences
		String[] value = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { //check whether the value in request object is null or not			  	
			if (value[i] != null && value[i].length() > 0) {
				//check whether the priorName object exist or not
				PriorName priorName = getPriorName(aNbaOinkRequest, i);
				if (priorName != null) // && priorName.hasLastName())
					{ //update the value for LastName in the priorName object.
					priorName.setLastName(value[i]);
				} //end if
			} //end if
		} //end for  	 
	} //end method

	/**
	 * Set the value for LifeCovOptTypeCode.
	 * OLifE.Holding.Policy.Life.Coverage.CovOption.LifeCovOptTypeCode 
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void storeLifeCovOptTypeCode(NbaOinkRequest aNbaOinkRequest) { //start loop for the no of occurrences	     
		int sizeCov = 0;
		Coverage coverage = null;
		CovOption covOption = null;

		Life life = getLife();
		String[] value = aNbaOinkRequest.getStringValues();
		if (life != null) {
			sizeCov = aNbaOinkRequest.getCount();
			//start loop for the no of occurrences			   	
			for (int i = 0; i < sizeCov; i++) {
				if (value[i] != null && value[i].length() > 0) {
					int covIndx = getCoverageForParty(aNbaOinkRequest, i);
					if (covIndx == -1) {
						break;
					} else {
						coverage = life.getCoverageAt(covIndx); // SPR3290
						if (coverage != null) {
							covOption = getCovOption(coverage, i);
							setActionUpdate(covOption);
							if (covOption != null)
								// && covOption.hasLifeCovOptTypeCode())
								covOption.setLifeCovOptTypeCode(value[i]);
						}
					}
				} //end if	   
			} //end for
		} //end if

	} //end method  	
	/**
	 * Set the value for OptionAmt.
	 * OLifE.Holding.Policy.Life.Coverage.CovOption.OptionAmt 
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void storeOptionAmt(NbaOinkRequest aNbaOinkRequest) { //start loop for the no of occurrences	     
		int sizeCov = 0; // SPR3290
		Coverage coverage = null;
		CovOption covOption = null;

		Life life = getLife();
		String[] value = aNbaOinkRequest.getStringValues();
		if (life != null) //)
			{
			sizeCov = aNbaOinkRequest.getCount();
			//start loop for the no of occurrences			   	
			for (int i = 0; i < sizeCov; i++) {
				int covIndx = getCoverageForParty(aNbaOinkRequest, i);
				if (covIndx == -1) {
					break;
				} else {
					coverage = life.getCoverageAt(covIndx); // SPR3290
					covOption = getCovOption(coverage, i);
					setActionUpdate(covOption);
					if (covOption != null) // && covOption.hasOptionAmt())
						covOption.setOptionAmt(value[i]);
				}
			} //end for
		} //end if

	} //end method  	

	/**
	 * Set the value for IndicatorCode.
	 * OLifE.Holding.Policy.Life.Coverage.IndicatorCode 
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP019 New Method
	public void storeIndicatorCode(NbaOinkRequest aNbaOinkRequest) { //start loop for the no of occurrences	     
		int sizeCov = 0; // SPR3290
		Coverage coverage = null;
		// SPR3290 code deleted

		Life life = getLife();
		String[] value = aNbaOinkRequest.getStringValues();
		if (life != null) {
			sizeCov = aNbaOinkRequest.getCount();
			//start loop for the no of occurrences			   	
			for (int i = 0; i < sizeCov; i++) {
				int covIndx = getCoverageForParty(aNbaOinkRequest, i);
				if (covIndx == -1) {
					break;
				} else {
					coverage = getCoverage(life, covIndx);
					if (coverage != null) // && coverage.hasIndicatorCode())
						coverage.setIndicatorCode(value[i]);
				}
			} //end for
		} //end if

	} //end method  		

	/**
	 * Set the value for AnnualPremAmt.
	 * OLifE.Holding.Policy.Life.Coverage.AnnualPremAmt 
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void storeAnnualPremAmt(NbaOinkRequest aNbaOinkRequest) { //start loop for the no of occurrences	     
		int sizeCov = 0; // SPR3290
		Coverage coverage = null;
		// SPR3290 code deleted

		Life life = getLife();
		String[] value = aNbaOinkRequest.getStringValues();
		if (life != null) {
			sizeCov = aNbaOinkRequest.getCount();
			//start loop for the no of occurrences			   	
			for (int i = 0; i < sizeCov; i++) {
				int covIndx = getCoverageForParty(aNbaOinkRequest, i);
				if (covIndx == -1) {
					break;
				} else {
					coverage = getCoverage(life, covIndx);
					if (coverage != null) // && coverage.hasAnnualPremAmt())
						coverage.setAnnualPremAmt(value[i]);
				}
			} //end for
		} //end if	   	   
	} //end method  		
	/**
	 * Set the value for PermFlatExtraAmt.
	 * OLifE.Holding.Policy.Life.Coverage.LifeParticipant.PermFlatExtraAmt is 
	 * used to store the prior last name
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void storePermFlatExtraAmt(NbaOinkRequest aNbaOinkRequest) { //start loop for the no of occurrences	     
		int sizeCov = 0;
		Coverage coverage = null;
		LifeParticipant lifePart = null;

		Life life = getLife();
		String[] value = aNbaOinkRequest.getStringValues();
		if (life != null) {
			sizeCov = aNbaOinkRequest.getCount();
			//start loop for the no of occurrences			   	
			for (int i = 0; i < sizeCov; i++) {
				int covIndx = getCoverageForParty(aNbaOinkRequest, i);
				if (covIndx == -1) {
					break;
				} else {
					coverage = life.getCoverageAt(covIndx); // SPR3290
					lifePart = getLifeParticipant(coverage, i);
					if (lifePart != null) // && lifePart.hasPermFlatExtraAmt())
						lifePart.setPermFlatExtraAmt(value[i]);
				}
			} //end for
		} //end if	   	   
	} //end method	

	/**
	 * Set the value for PermTableRating.
	 * OLifE.Holding.Policy.Life.Coverage.LifeParticipant.PermTableRating is 
	 * used to store the prior last name
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void storePermLifeTableRating(NbaOinkRequest aNbaOinkRequest) { //start loop for the no of occurrences	     
		int sizeCov = 0;
		Coverage coverage = null;
		LifeParticipant lifePart = null;

		Life life = getLife();
		String[] value = aNbaOinkRequest.getStringValues();
		if (life != null) {
			sizeCov = aNbaOinkRequest.getCount();
			//start loop for the no of occurrences			   	
			for (int i = 0; i < sizeCov; i++) {
				int covIndx = getCoverageForParty(aNbaOinkRequest, i);
				if (covIndx == -1) {
					break;
				} else {
					coverage = life.getCoverageAt(covIndx); // SPR3290
					lifePart = getLifeParticipant(coverage, i);
					if (lifePart != null) // && lifePart.hasPermTableRating())
						lifePart.setPermTableRating(value[i]);
				}
			} //end for
		} //end if	   	   
	} //end method	
	/**
	 * Set the value for RatingOverriddenInd.
	 * OLifE.Holding.Policy.Life.Coverage.LifeParticipant.RatingOverriddenInd is 
	 * used to store the prior last name
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void storeRatingOverriddenInd(NbaOinkRequest aNbaOinkRequest) { //start loop for the no of occurrences	     
		int sizeCov = 0;
		Coverage coverage = null;
		LifeParticipant lifePart = null;

		Life life = getLife();
		String[] value = aNbaOinkRequest.getStringValues();
		if (life != null) {
			sizeCov = aNbaOinkRequest.getCount();
			//start loop for the no of occurrences			   	
			for (int i = 0; i < sizeCov; i++) {
				int covIndx = getCoverageForParty(aNbaOinkRequest, i);
				if (covIndx == -1) {
					break;
				} else {
					coverage = life.getCoverageAt(covIndx); // SPR3290
					lifePart = getLifeParticipant(coverage, i);
					if (lifePart != null)
						// && lifePart.hasRatingOverriddenInd())
						lifePart.setRatingOverriddenInd(value[i]);
				}
			} //end for
		} //end if	   	   
	} //end method	
	/**
	 * Set the value for RatingReason.
	 * OLifE.Holding.Policy.Life.Coverage.LifeParticipant.RatingReason is 
	 * used to store the prior last name
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void storeRatingReason(NbaOinkRequest aNbaOinkRequest) { //start loop for the no of occurrences	     
		int sizeCov = 0;
		Coverage coverage = null;
		LifeParticipant lifePart = null;

		Life life = getLife();
		String[] value = aNbaOinkRequest.getStringValues();
		if (life != null) {
			sizeCov = aNbaOinkRequest.getCount();
			//start loop for the no of occurrences			   	
			for (int i = 0; i < sizeCov; i++) {
				int covIndx = getCoverageForParty(aNbaOinkRequest, i);
				if (covIndx == -1) {
					break;
				} else {
					coverage = life.getCoverageAt(covIndx); // SPR3290
					lifePart = getLifeParticipant(coverage, i);
					if (lifePart != null) // && lifePart.hasRatingReason())
						lifePart.setRatingReason(value[i]);
				}
			} //end for
		} //end if	   	   
	} //end method	
	/**
	 * Set the value for TempFlatEndDate.
	 * OLifE.Holding.Policy.Life.Coverage.LifeParticipant.TempFlatEndDate is 
	 * used to store the prior last name
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void storeTempFlatEndDate(NbaOinkRequest aNbaOinkRequest) { //start loop for the no of occurrences	     
		int sizeCov = 0;
		Coverage coverage = null;
		LifeParticipant lifePart = null;

		Life life = getLife();
		String[] value = aNbaOinkRequest.getStringValues();
		if (life != null) {
			sizeCov = aNbaOinkRequest.getCount();
			//start loop for the no of occurrences			   	
			for (int i = 0; i < sizeCov; i++) {
				int covIndx = getCoverageForParty(aNbaOinkRequest, i);
				if (covIndx == -1) {
					break;
				} else {
					coverage = life.getCoverageAt(covIndx); // SPR3290
					lifePart = getLifeParticipant(coverage, i);
					if (lifePart != null) // && lifePart.hasTempFlatEndDate())
						lifePart.setTempFlatEndDate(NbaUtils.getDateFromStringInUSFormat(value[i]));
				}
			} //end for
		} //end if	   	   
	} //end method	

	/**
	 * Set the value for TempFlatExtraAmt  .
	 * OLifE.Holding.Policy.Life.Coverage.LifeParticipant.TempFlatExtraAmt   is 
	 * used to store the prior last name
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void storeTempLifeFlatExtraAmt(NbaOinkRequest aNbaOinkRequest) { //start loop for the no of occurrences	     
		int sizeCov = 0;
		Coverage coverage = null;
		LifeParticipant lifePart = null;

		Life life = getLife();
		String[] value = aNbaOinkRequest.getStringValues();
		if (life != null) {
			sizeCov = aNbaOinkRequest.getCount();
			//start loop for the no of occurrences			   	
			for (int i = 0; i < sizeCov; i++) {
				int covIndx = getCoverageForParty(aNbaOinkRequest, i);
				if (covIndx == -1) {
					break;
				} else {
					coverage = life.getCoverageAt(covIndx); // SPR3290
					lifePart = getLifeParticipant(coverage, i);
					if (lifePart != null) // && lifePart.hasTempFlatExtraAmt())
						lifePart.setTempFlatExtraAmt(value[i]);
				}
			} //end for
		} //end if	   	   
	} //end method	

	/**
	 * Set the value for CompletionDate.
	 * OlifE.FormInstance.CompletionDate 
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void storeCompletionDate(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				FormInstance formInstance = getFormInstance(aNbaOinkRequest, i);
				if (formInstance != null)
					// && formInstance.hasCompletionDate())
					{
					formInstance.setCompletionDate(NbaUtils.getDateFromStringInUSFormat(values[i]));
				}
			}
		}
	}
	/**
	 * Set the value for Age.
	 * OLifE.Party.PersonOrOrganization.Person.Age is the 
	 * Age of the person
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP019 New Method
	public void storeAge(NbaOinkRequest aNbaOinkRequest) {
		
		String[] values = aNbaOinkRequest.getStringValues();	
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					person.setAge(values[i]);					
			  }
			} 
		}		
	}
	/**
	 * Set the value for DBA.
	 * Olife.Party.Organization.DBA is the 
	 * Age of the person
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP019 New Method
	public void storeDBA(NbaOinkRequest aNbaOinkRequest) {
		
		String[] values = aNbaOinkRequest.getStringValues();	
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				Organization organization = getOrganization(aNbaOinkRequest, i);
				if (organization != null) {
					organization.setDBA(values[i]);					
			  }
			} 
		}		
	}
	/**
	 * Set the value for a Impairment Date.
	 * OLifE.Party.Person.ImpairmentInfo.ImpairmentDate is the 
	 * date of the impairment for the person.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACN006 New Method
	public void storeImpairmentDate(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				ImpairmentInfo impInfo=getImpairmentInfo(aNbaOinkRequest,i);
				 if(impInfo!=null)
				 {	
					impInfo.setImpairmentDate(NbaUtils.getDateFromStringInUSFormat(values[i]));
				 }			  	
				
		   }		
	   }	
	 }

	/**
	 * Set the value for a Impairment Description.
	 * OLifE.Party.Person.ImpairmentInfo.Description is the 
	 * description of the impairment for the person.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACN006 New Method
	public void storeDescription(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				ImpairmentInfo impInfo=getImpairmentInfo(aNbaOinkRequest,i) ;
//				Begin ACN006	
				if(impInfo == null){			  	
					Person person = getPerson(aNbaOinkRequest, i);
					if (person != null){
						PersonExtension personExtn = NbaUtils.getFirstPersonExtension(person);
						if (personExtn == null) {
							OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PERSON);
							personExtn = olifeExt.getPersonExtension();
							setActionAdd(personExtn);
							person.addOLifEExtension(olifeExt);
							impInfo = new ImpairmentInfo();
							impInfo.setActionAdd();
							personExtn.addImpairmentInfo(impInfo);
						}
					}
				}
				impInfo.setDescription(values[i]);
				impInfo.setActionUpdate();
				//end ACN006
			}				   	
		}	
	}

	/**
	 * Set the value for a Impairment Status.
	 * OLifE.Party.Person.ImpairmentInfo.ImpairmentStatus is the 
	 * status of the impairment for the person.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACN006 New Method
	public void storeImpairmentStatus(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				ImpairmentInfo impInfo=getImpairmentInfo(aNbaOinkRequest,i);
				 if(impInfo == null)
				 {	
					Person person = getPerson(aNbaOinkRequest, i);
					if (person != null){
						PersonExtension personExtn = NbaUtils.getFirstPersonExtension(person);
						if (personExtn == null) {
							OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PERSON);
							personExtn = olifeExt.getPersonExtension();
							setActionAdd(personExtn);
							person.addOLifEExtension(olifeExt);
							impInfo = new ImpairmentInfo();
							impInfo.setActionAdd();
							personExtn.addImpairmentInfo(impInfo);
						}
					}
				 }				  	
				impInfo.setImpairmentStatus(values[i]);
				impInfo.setActionUpdate();
			}			   	
		}	
	}

	/**
	 * Set the value for a Impairment Type.
	 * OLifE.Party.Person.ImpairmentInfo.ImpairmentType is the 
	 * type of the impairment for the person.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACN006 New Method
	public void storeImpairmentType(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				ImpairmentInfo impInfo=getImpairmentInfo(aNbaOinkRequest,i);
				if(impInfo == null)
				{	
				   Person person = getPerson(aNbaOinkRequest, i);
				   if (person != null){
					   PersonExtension personExtn = NbaUtils.getFirstPersonExtension(person);
					   if (personExtn == null) {
						   OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PERSON);
						   personExtn = olifeExt.getPersonExtension();
						   setActionAdd(personExtn);
						   person.addOLifEExtension(olifeExt);
						   impInfo = new ImpairmentInfo();
						   impInfo.setActionAdd();
						   personExtn.addImpairmentInfo(impInfo);
					   }
				   }
				}				  	
				impInfo.setImpairmentType(values[i]);
				impInfo.setActionUpdate();
		   }		
	   }	
	 }

	/**
	 * Set the value for a Impairment Workup Indicator.
	 * OLifE.Party.Person.ImpairmentInfo.ImpWorkupInd is the 
	 * workup indicator of the impairment for the person.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP019 New Method
	public void storeImpWorkupInd(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				
				ImpairmentInfo impInfo=getImpairmentInfo(aNbaOinkRequest,i);
				 if(impInfo!=null)
				 {	
					impInfo.setImpWorkupInd(values[i]);
				 }
		   }		
	   }	
	 }
	/**
	 * Set the value for a Impairment Class.
	 * OLifE.Party.Person.ImpairmentInfo.ImpairmentClass is the 
	 * class of the impairment for the person.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACN006 New Method
	public void storeImpairmentClass(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				ImpairmentInfo impInfo=getImpairmentInfo(aNbaOinkRequest,i) ;
				if(impInfo == null){			  	
					Person person = getPerson(aNbaOinkRequest, i);
					if (person != null){
						PersonExtension personExtn = NbaUtils.getFirstPersonExtension(person);
						if (personExtn == null) {
							OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PERSON);
							personExtn = olifeExt.getPersonExtension();
							setActionAdd(personExtn);
							person.addOLifEExtension(olifeExt);
							impInfo = new ImpairmentInfo();
							impInfo.setActionAdd();
							personExtn.addImpairmentInfo(impInfo);
						}
					}
				}
				impInfo.setImpairmentClass(values[i]);
				impInfo.setActionUpdate();
			}				   	
		}	
	}
	/**
	 * Set the value for a Impairment Source.
	 * OLifE.Party.Person.ImpairmentInfo.ImpairmentSource is the 
	 * source from where the impairment is added.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACN006 New Method
	public void storeImpairmentSource(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("") && !(hasParty(aNbaOinkRequest, i) && hasPerson(aNbaOinkRequest, i))) {
				continue;
			} else {
				ImpairmentInfo impInfo=getImpairmentInfo(aNbaOinkRequest,i) ;
				if(impInfo == null){			  	
					Person person = getPerson(aNbaOinkRequest, i);
					if (person != null){
						PersonExtension personExtn = NbaUtils.getFirstPersonExtension(person);
						if (personExtn == null) {
							OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_PERSON);
							personExtn = olifeExt.getPersonExtension();
							setActionAdd(personExtn);
							person.addOLifEExtension(olifeExt);
							impInfo = new ImpairmentInfo();
							impInfo.setActionAdd();
							personExtn.addImpairmentInfo(impInfo);
						}
					}
				}
				impInfo.setImpairmentSource(values[i]);
				impInfo.setActionUpdate();
			}				   	
		}	
	}


	/**
	 * Set the value for a TrackingServiceProvider.
	 * OLifE.Holding.Policy.RequirementInfo.TrackingInfo.TrackingServiceProvider is the 
	 * vendor name of the requirement.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP019 New Method
	public void storeTrackingServiceProvider(NbaOinkRequest aNbaOinkRequest) {
		String[] values = aNbaOinkRequest.getStringValues();
		List reqList = getRequirementInfos(aNbaOinkRequest); //SPR3353
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (values[i].equals("")) {
				continue;
			} else {
				TrackingInfo trackInfo=getTrackingInfo(reqList,i); //SPR3353
				 if(trackInfo!=null)	 
					 trackInfo.setTrackingServiceProvider(values[i]);
			  }    
		}					
	
   }
	/**
	* Answers the ApplicationInfoExtension for the ApplicationInfo object,
	* if present; if not present, one is created. 
	*/
	// ACN007 new method
	public OLifEExtension getApplicationInfoExtension(boolean update) {
		ApplicationInfo apInfo = getApplicationInfo();
		int index_Extension = getExtensionIndex(apInfo.getOLifEExtension(), APPLICATIONINFO_EXTN);
		if (index_Extension == -1) {
			apInfo.addOLifEExtension(NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_APPLICATIONINFO));
			index_Extension = apInfo.getOLifEExtensionCount() - 1;
		}
		if( update ) {
			apInfo.setActionUpdate();
		}
		return apInfo.getOLifEExtensionAt(index_Extension);
	}
   	/**
	* Set the value for ReplacementFormSignatureOK 
	* OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.ReplacementFormSignatureOK
	* @param aNbaOinkRequest
	*/
   	// ACN007 new method
	public void storeReplacementFormSignatureOK(NbaOinkRequest aNbaOinkRequest) {
		OLifEExtension oli = getApplicationInfoExtension(true);
		if (oli != null) {
			setActionUpdate(oli);
			oli.getApplicationInfoExtension().setReplacementFormSignatureOK(aNbaOinkRequest.getStringValue());
			setActionUpdate(oli.getApplicationInfoExtension());
		}
	}
	/**
	* Set the value for ReplacementFormSignatureOK 
	* OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.ReplacementFormSignatureOK
	* @param aNbaOinkRequest
	*/
	// ACN007 new method
	public void storePACValidationOK(NbaOinkRequest aNbaOinkRequest) {
		OLifEExtension oli = getApplicationInfoExtension(true);
		if (oli != null) {
			setActionUpdate(oli);
			oli.getApplicationInfoExtension().setPACValidationOK(aNbaOinkRequest.getStringValue());
			setActionUpdate(oli.getApplicationInfoExtension());
		}
	}
	/**
	* Set the value for ReplacementFormSignatureOK 
	* OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.ReplacementFormSignatureOK
	* @param aNbaOinkRequest
	*/
	// ACN007 new method
	public void storeCheckSignedOK(NbaOinkRequest aNbaOinkRequest) {
		OLifEExtension oli = getApplicationInfoExtension(true);
		if (oli != null) {
			setActionUpdate(oli);
			oli.getApplicationInfoExtension().setCheckSignedOK(aNbaOinkRequest.getStringValue());
			setActionUpdate(oli.getApplicationInfoExtension());
		}
	}
	/**
	* Set the value for QualityCheckModelResults
	* OLifE.Holding.Attachment
	* @param aNbaOinkRequest
	*/
	// ACN007 new method
	public void storeQualityCheckModelResults(NbaOinkRequest aNbaOinkRequest) {
		//begin SPR2574 
		String nbAOinkRequestValue = aNbaOinkRequest.getStringValue();
		if (null == nbAOinkRequestValue || nbAOinkRequestValue.length() == 0) { 
			return; 
		} 
		//end SPR2574 		
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE());
		ArrayList attachList = holding.getAttachment();		
		int count = attachList.size();
		Attachment worksheet = null;
		for (int i = 0; i < count; i++) {
			worksheet = (Attachment)attachList.get(i);
			if( worksheet.getAttachmentType() == NbaOliConstants.OLI_ATTACH_WRKSHT) {
				worksheet.setActionUpdate();
				break;
			}
			worksheet = null;
		}
		if( worksheet == null) {
			worksheet = new Attachment();
			worksheet.setAttachmentType(NbaOliConstants.OLI_ATTACH_WRKSHT);
			worksheet.setActionAdd();
			holding.addAttachment(worksheet);
		}
		AttachmentData attachData = new AttachmentData();
		attachData.setPCDATA(nbAOinkRequestValue); //SPR2574
		worksheet.setAttachmentData(attachData);
		holding.setActionUpdate();
	}
	/**
	  * Set the value for a BillControlEffDate.
	  * @param aNbaOinkRequest - data request container
	 */
	//NBA115 New Method
	public void storeBillControlEffDate(NbaOinkRequest aNbaOinkRequest) {
		Banking banking = null;
		String[] values = aNbaOinkRequest.getStringValues();
		int requested = aNbaOinkRequest.getCount();
		List bankingList = getBankingList(aNbaOinkRequest);
		int bankingCount = bankingList.size();

		for (int i = 0; i < requested; i++) {
			banking = null;
			if (values[i].equals("") && i >= bankingCount) {
				break;
			} else if (i < bankingCount) {
				banking = (Banking) bankingList.get(i);
			} else {
				banking = createBanking();
				if (CREDIT_CARD_PAYMENT.equalsIgnoreCase(aNbaOinkRequest.getQualifier())) {
					initCreditCardPayment(banking);
				}
			}
			BankingExtension bankingExtn = NbaUtils.getFirstBankingExtension(banking);
			if (bankingExtn != null) {
				bankingExtn.setBillControlEffDate(NbaUtils.getDateFromStringInUSFormat(values[i]));
			}
		}
	}
	/**
	  * Set the value for a PaymentChargeAmt. Payment charge amount is the amount charged
	  * on a credit card for credit card payments
	  * @param aNbaOinkRequest - data request container
	 */
	//NBA115 New Method
	public void storePaymentChargeAmt(NbaOinkRequest aNbaOinkRequest) {
		Banking banking = null;
		String[] values = aNbaOinkRequest.getStringValues();
		int requested = aNbaOinkRequest.getCount();
		List bankingList = getBankingList(aNbaOinkRequest);
		int bankingCount = bankingList.size();

		for (int i = 0; i < requested; i++) {
			banking = null;
			if ((values[i].equals("") || values[i].equals("0.0"))&& i >= bankingCount) {
				break;
			} else if (i < bankingCount) {
				banking = (Banking) bankingList.get(i);
			} else {
				banking = createBanking();
				if (CREDIT_CARD_PAYMENT.equalsIgnoreCase(aNbaOinkRequest.getQualifier())) {
					initCreditCardPayment(banking);
				}
			}
			BankingExtension bankingExtn = NbaUtils.getFirstBankingExtension(banking);
			if (bankingExtn != null) {
				bankingExtn.setPaymentChargeAmt(NbaDataObjectRenderer.convertStringToDouble(values[i]));
			}
		}
	}
  
	/**
	  * Set the value for a PaymentType. contains the pending payment
	  * type for a credit card payment
	  * @param aNbaOinkRequest - data request container
	 */
	//NBA115 New Method
	public void storePaymentType(NbaOinkRequest aNbaOinkRequest) {
		Banking banking = null;
		String[] values = aNbaOinkRequest.getStringValues();
		int requested = aNbaOinkRequest.getCount();
		List bankingList = getBankingList(aNbaOinkRequest);
		int bankingCount = bankingList.size();

		for (int i = 0; i < requested; i++) {
			banking = null;
			if (values[i].equals("") && i >= bankingCount) {
				break;
			} else if (i < bankingCount) {
				banking = (Banking) bankingList.get(i);
			} else {
				banking = createBanking();
				if (CREDIT_CARD_PAYMENT.equalsIgnoreCase(aNbaOinkRequest.getQualifier())) {
					initCreditCardPayment(banking);
				}
			}
			BankingExtension bankingExtn = NbaUtils.getFirstBankingExtension(banking);
			if (bankingExtn != null) {
				bankingExtn.setPaymentType(values[i]);
			}
		}
	}
	
	/**
	 * Set the value for CardExpMonth, stored in first two character places of CreditCardExpDate and contains card expiration month.
	 * OLifE.Holding.Banking.CreditCardExpDate is the expiration date of the associated credit card 
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA115 New Method
	public void storeCardExpMonth(NbaOinkRequest aNbaOinkRequest) {
		Banking banking = null;
		String[] values = aNbaOinkRequest.getStringValues();
		int requested = aNbaOinkRequest.getCount();
		List bankingList = getBankingList(aNbaOinkRequest);
		int bankingCount = bankingList.size();

		for (int i = 0; i < requested; i++) {
			banking = null;
			if (values[i].equals("") && i >= bankingCount) {
				break;
			} else if (i < bankingCount) {
				banking = (Banking) bankingList.get(i);
			} else {
				banking = createBanking();
				if (CREDIT_CARD_PAYMENT.equalsIgnoreCase(aNbaOinkRequest.getQualifier())) {
					initCreditCardPayment(banking);
				}
			}
			String year = "";
			if(banking.hasCreditCardExpDate()){
				year = banking.getCreditCardExpDate().length() >= 7 ? banking.getCreditCardExpDate().substring(3, 7) : year;
			}
			banking.setCreditCardExpDate(values[i] + year);
		}
	}
		
	/**
	 * Set the value for CardExpMonth, stored in last four characters of CreditCardExpDate and contains card expiration year.
	 * OLifE.Holding.Banking.CreditCardExpDate is the expiration date of the associated credit card 
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA115 New Method
	public void storeCardExpYear(NbaOinkRequest aNbaOinkRequest) {
		Banking banking = null;
		String[] values = aNbaOinkRequest.getStringValues();
		int requested = aNbaOinkRequest.getCount();
		List bankingList = getBankingList(aNbaOinkRequest);
		int bankingCount = bankingList.size();

		for (int i = 0; i < requested; i++) {
			banking = null;
			if (values[i].equals("") && i >= bankingCount) {
				break;
			} else if (i < bankingCount) {
				banking = (Banking) bankingList.get(i);
			} else {
				banking = createBanking();
				if (CREDIT_CARD_PAYMENT.equalsIgnoreCase(aNbaOinkRequest.getQualifier())) {
					initCreditCardPayment(banking);
				}
			}
			String month = "";
			if(banking.hasCreditCardExpDate()){
				month = banking.getCreditCardExpDate().length() >= 2 ? banking.getCreditCardExpDate().substring(0, 2) : month;
			}
			banking.setCreditCardExpDate(month + '/' + values[i]);
		}
	}
	/**
	 * Set the default value of distribution channel to PolicyExtension.DistributionChannel
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.03 New Method
	public void storeDistributionChannel(NbaOinkRequest aNbaOinkRequest) {
		PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(getNbaTXLife().getPolicy());
		if (policyExtn == null) {
			OLifEExtension olifeExtn = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
			policyExtn = olifeExtn.getPolicyExtension();
			policyExtn.setActionAdd();
			getNbaTXLife().getPolicy().addOLifEExtension(olifeExtn);
		}
		policyExtn.setDistributionChannel(aNbaOinkRequest.getStringValue());
	}
		
}
