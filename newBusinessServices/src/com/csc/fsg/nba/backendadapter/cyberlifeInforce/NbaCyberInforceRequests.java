package com.csc.fsg.nba.backendadapter.cyberlifeInforce;
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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.csc.fsg.nba.backendadapter.cyberlife.NbaCyberConstants;
import com.csc.fsg.nba.backendadapter.cyberlife.NbaCyberRequests;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.AccountingActivity;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.AnnuityExtension;
import com.csc.fsg.nba.vo.txlife.AnnuityRiderExtension;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ArrDestination;
import com.csc.fsg.nba.vo.txlife.Arrangement;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.CommissionCalcActivity;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Investment;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.LifeParticipantExtension;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Participant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Payout;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RelationProducerExtension;
import com.csc.fsg.nba.vo.txlife.Rider;
import com.csc.fsg.nba.vo.txlife.SubAccount;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;
/**
 * This class contains the CyberLife adapters for the CyberLife Inforce
 * Web Services with the exception of Inforce App Submit 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>NBA073 and NBA068</td><td>Version 3</td><td>Initial Development</td></tr>
 * <tr><td>SPR1703</td><td>Version 3</td><td>Correct force indicator for inforce payments</td></tr>
 * <tr><td>NBA112</td><td>Version 4</td><td>Agent Name and Address</td></tr>
 * <tr><td>SPR1986</td><td>Version 4</td><td>Remove CLIF Adapter Defaults for Pay up and Term date of Coverages and Covoptions</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change etc.</td></tr>
 * <tr><td>NBA105</td><td>Version 4</td><td>Underwriting Risk.</td></tr>
 * <tr><td>SPR2121</td><td>Version 4</td><td>Deleted code for FUSCORR in Inforce Payments</td></tr>
 * <tr><td>SPR2248</td><td>Version 6</td><td>Reinstatements not sent correctly for Traditional Term products and Advanced Products</td></tr>
 * <tr><td>NBA132</td><td>Version 6</td><td>Equitable Distribution of Work</td></tr>
 * <tr><td>SPR3054</td><td>Version 6</td><td>Contract Number DXE (PUAGRQPN) Should Be Removed from Agent Validation Request</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR3433</td><td>Version 7</td><td>FUSFORCE Should Be Changed to Value of One for All of the U1 Series of Transactions</td></tr>
 * <tr><td>ALPC7</td><td>Version 7</td><td>Schema migration from 2.8.90 to 2.9.03</td></tr>
 * </table>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaCyberInforceRequests extends NbaCyberRequests implements NbaCyberConstants, NbaCyberInforceConstants {
	// Logger
	private static NbaLogger logger = null; //NBA044
	protected String hostRequest = null;
	/**
	 * NbaCyberInforceRequests constructor.
	 */
	public NbaCyberInforceRequests() {
		super();
	}
	/**
	* Return the <code>NbaLogger</code> implementation (e.g. NbaLogService)
	* @return com.csc.fsg.nba.foundation.NbaLogger
	*/
	private static NbaLogger getLogger() { //NBA044
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaCyberInforceRequests.class.getName()); //NBA044
			} catch (Exception e) {
				NbaBootLogger.log("NbaCyberInforceRequests could not get a logger from the factory."); //NBA044 SPR3290
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	* Create the 203 request to send to Cyberlife
	* @param TXLife  
	* @return java.lang.String containing DXE to send to the host
	* @throws NbaBaseException
	*/
	public String create203Request(TXLife txLife) throws NbaBaseException {
		String result = new String();
		try {
			UserAuthRequestAndTXLifeRequest userRequest = txLife.getUserAuthRequestAndTXLifeRequest();
			TXLifeRequest txlifeRequest = userRequest.getTXLifeRequestAt(0);
			OLifE olife = txlifeRequest.getOLifE();
			Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); 
			Policy policy = holding.getPolicy();
			result = HOLDING_TYPE
					+ COMP_CODE
					+ "="
					+ policy.getCarrierCode()
					+ ";"
					+ POL_NUM
					+ "="
					+ policy.getPolNumber()
					+ ";" 
					+ HOLDING_INFORCE_SEGMENTS
					+ HOLDING_INFORCE_RESOLVE
					+ HOLDING_INFORCE_RESOLVE_COV
					+ HOLDING_INFORCE_RESOLVE_BENEFIT   
					+ HOLDING_INFORCE_RESOLVE_RATING 
					+ HOLDING_INFORCE_RESOLVE_ENDORSEMENTS
					+ HOLDING_INFORCE_RESOLVE_PEOPLE 
					+ HOLDING_INFORCE_RESOLVE_ERR0R
					+ HOLDING_INFORCE_RESOLVE_AGENT     
					+ HOLDING_INFORCE_RESOLVE_AUTO_TRAN      
					+ HOLDING_INFORCE_RESOLVE_ADV_PROD     
					+ HOLDING_INFORCE_RESOLVE_SPC_FREQ_BILLING  
					+ HOLDING_INFORCE_RESOLVE_ALLOCATION
					+ HOLDING_INFORCE_RESOLVE_REINSURANCE  
					+ HOLDING_INFORCE_RESOLVE_BANKING  
					+ HOLDING_INFORCE_RESOLVE_MISC;   //NBA077
					

		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_PARSE, e);
		}
		return result;
	}
	/**
	* Create the 203 request for reinstatement to send to the Cyberlife
	* @param TXLife  
	* @return java.lang.String containing DXE to send to the host
	* @throws NbaBaseException
	*/
	//NBA077 New Method
	public String create203RequestForReinstatement(TXLife txLife) throws NbaBaseException {
		StringBuffer result = new StringBuffer();
		try {
			UserAuthRequestAndTXLifeRequest userRequest = txLife.getUserAuthRequestAndTXLifeRequest();
			TXLifeRequest txlifeRequest = userRequest.getTXLifeRequestAt(0);
			OLifE olife = txlifeRequest.getOLifE();
			Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); 
			Policy policy = holding.getPolicy();
			result.append("6PCYWHATTODO=U101,2;AEPSTPDS=1;") ;
			result.append(POL_NUM);
			result.append( "=");
			result.append(policy.getPolNumber());
			result.append(";");
			result.append(COMP_CODE);
			result.append("=");
			result.append(policy.getCarrierCode());
			result.append(";");
			result.append("FUSDATE=");
			result.append(formatCyberDate(new Date()));
			result.append(";");
			result.append("FUSDUEDT=");
			result.append(formatCyberDate(new Date()));
			result.append(";FUSTYPE=B;FUSACT=1;FUSREINI=0;FUSREQCV=0;WHATTODO=DISP,2;");
			result.append(COMP_CODE);
			result.append("=");
			result.append(policy.getCarrierCode());
			result.append(";");
			result.append(POL_NUM);
			result.append( "=");
			result.append(policy.getPolNumber());
			result.append(";");
			result.append(HOLDING_INFORCE_SEGMENTS);
			result.append(HOLDING_INFORCE_RESOLVE);
			result.append(HOLDING_INFORCE_RESOLVE_COV);
			result.append(HOLDING_INFORCE_RESOLVE_BENEFIT);
			result.append(HOLDING_INFORCE_RESOLVE_RATING);
			result.append(HOLDING_INFORCE_RESOLVE_ENDORSEMENTS);
			result.append(HOLDING_INFORCE_RESOLVE_PEOPLE);
			result.append(HOLDING_INFORCE_RESOLVE_ERR0R);
			result.append(HOLDING_INFORCE_RESOLVE_AGENT);
			result.append(HOLDING_INFORCE_RESOLVE_AUTO_TRAN);
			result.append(HOLDING_INFORCE_RESOLVE_ADV_PROD);
			result.append(HOLDING_INFORCE_RESOLVE_SPC_FREQ_BILLING);
			result.append(HOLDING_INFORCE_RESOLVE_ALLOCATION);
			result.append(HOLDING_INFORCE_RESOLVE_REINSURANCE);
			result.append(HOLDING_INFORCE_RESOLVE_BANKING);
			result.append(HOLDING_INFORCE_RESOLVE_MISC);

		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_PARSE, e);
		}
		return result.toString();
	}

	/**
	 * Create the Agent Validation transaction (22812) 
	 * @param txLife the holding inquiry
	 * @return java.lang.String the host request dxe stream
	 */
	//NBA112 changed method name as earlier name had transaction type which is changed
	public String createAgentValidationRequest(TXLife txLife) throws com.csc.fsg.nba.exception.NbaBaseException {
		StringBuffer hostRequest = new StringBuffer();	//NBA112
		try {
			UserAuthRequestAndTXLifeRequest userRequest = txLife.getUserAuthRequestAndTXLifeRequest();
			TXLifeRequest txlifeRequest = userRequest.getTXLifeRequestAt(0);
			OLifE olife = txlifeRequest.getOLifE();
			Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); //NBA044
			Policy policy = holding.getPolicy();
			//begin NBA112
			//Common info
			createAgentValidationCommon(hostRequest, policy);
			//Agent Info
			String sitCode = createAgentValidationAgentInfo(hostRequest, olife);
			if (policy.hasLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty()) { //NBA093
				if (policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isLife()) { //NBA093
					Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife(); //NBA093
					createAgentValidationLifeInfo(hostRequest, life, sitCode);
				} else {
					if (policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isAnnuity()) { //NBA093
						Annuity annuity = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity(); //NBA093
						createAgentValidationAnnuityInfo(hostRequest, policy, annuity, sitCode);
					}
				}
			}
			//end NBA112
			hostRequest.append(AGENT_VALIDATION_RESOLVE);  //NBA132
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_PARSE, e);
		}
		return hostRequest.toString();	//NBA112
	}
	/**
	 * Create the Agent Name and Address transaction (22800) from the holding inquiry 
	 * @param txLife the holding inquiry 
	 * @return java.lang.String the host request dxe stream
	 */
	//NBA112 new method
			
	public String createAgentInfoRequest(TXLife txLife) throws NbaBaseException {
		String hostRequest = null;
		UserAuthRequestAndTXLifeRequest userRequest = txLife.getUserAuthRequestAndTXLifeRequest();
		TXLifeRequest txlifeRequest = userRequest.getTXLifeRequestAt(0);
		OLifE olife = txlifeRequest.getOLifE();
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife);
		Policy policy = holding.getPolicy();
		long relationRole = olife.getRelationAt(0).getRelationRoleCode(); //NBA112
		hostRequest = HOLDING_TYPE;
		hostRequest = hostRequest + createDataExchange(policy.getCarrierCode(), "AEPKEYUS", NbaTableConstants.NBA_COMPANY, CYBTBL_UCT, CT_CHAR, 2); //NBA112
		CarrierAppointment carrierAppointment = olife.getPartyAt(0).getProducer().getCarrierAppointmentAt(0);
		//Begin NBA112
		hostRequest = hostRequest + createDataExchange(carrierAppointment.getCompanyProducerID(), "AEPKEYPL", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0); //NBA112
		
		if(relationRole == OLI_REL_PRIMAGENT || relationRole == OLI_REL_ADDWRITINGAGENT){
			hostRequest = hostRequest + createDataExchange("W", "AEPPOLOC", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			hostRequest = hostRequest + RESOLVE_WRIT_AGENT_INFO;
		} else if(relationRole == OLI_REL_SERVAGENT || relationRole == OLI_REL_SERVAGENCY){
			hostRequest = hostRequest + createDataExchange("S", "AEPPOLOC", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			hostRequest = hostRequest + RESOLVE_SERV_AGENT_INFO;
		}
		//End NBA112
		return hostRequest;
	}
	/**
	 * Create the Party inquirey request 
	 * @param txLife the holding inquiry 
	 * @return java.lang.String the host request dxe stream
	 */
	//NBA105 new method	
	public String createPartyInqRequest(TXLife txLife) throws NbaBaseException {
		StringBuffer hostRequest = new StringBuffer();
		UserAuthRequestAndTXLifeRequest userRequest = txLife.getUserAuthRequestAndTXLifeRequest();
		TXLifeRequest txlifeRequest = userRequest.getTXLifeRequestAt(0);
		OLifE olife = txlifeRequest.getOLifE();
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife);
		Policy policy = holding.getPolicy();
		Party requestedParty = olife.getPartyAt(0);
		Person requestedPerson = requestedParty.getPersonOrOrganization().getPerson();
		StringBuffer dxeString = new StringBuffer("");
		StringBuffer nameString = new StringBuffer("");

		hostRequest.append(HOLDING_TYPE);
		//Set basic fields
		hostRequest.append(
			createDataExchange(
				policy.getCarrierCode(),
				NbaCyberInforceConstants.PARTY_CARRIER_CODE,
				NbaTableConstants.NBA_COMPANY,
				CYBTBL_UCT,
				CT_CHAR,
				2));
		hostRequest.append(createDataExchange("0", NbaCyberInforceConstants.SEGMENT_SEQUENCE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
		hostRequest.append(createDataExchange("0", NbaCyberInforceConstants.FLG_LSNM_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
		hostRequest.append(createDataExchange("ALL", NbaCyberInforceConstants.TYPE_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));

		//Set name, extract it from incoming party object
		nameString.append(requestedPerson.hasLastName() ? requestedPerson.getLastName() + "*" : "");
		nameString.append(requestedPerson.hasFirstName() ? requestedPerson.getFirstName() + "*" : "");
		nameString.append(requestedPerson.hasMiddleName() ? requestedPerson.getMiddleName() + "*" : "");

		if (requestedParty.hasGovtID()) {
			dxeString.append(
				createDataExchange(requestedParty.getGovtID(), NbaCyberInforceConstants.PARTY_GOVT_ID, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
		}

		if (requestedPerson.hasBirthDate()) {
			dxeString.append(
				createDataExchange(
					requestedPerson.getBirthDate(),
					NbaCyberInforceConstants.PARTY_BIRTH_DATE,
					CYBTRANS_NONE,
					CYBTBL_NONE,
					CT_DEFAULT,
					0));
		}

		if (requestedPerson.hasBirthJurisdictionTC()) {
			dxeString.append(
				createDataExchange(
					requestedPerson.getBirthJurisdictionTC(),
					NbaCyberInforceConstants.PARTY_BIRTH_JUR,
					CYBTRANS_NONE,
					CYBTBL_NONE,
					CT_DEFAULT,
					0));
		}

		String nameDxeString =
			createDataExchange(nameString.toString(), NbaCyberInforceConstants.PARTY_NAME, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
		hostRequest.append(nameDxeString);
		hostRequest.append(dxeString.toString());
		hostRequest.append(createDataExchange("0", NbaCyberInforceConstants.RESPONSE_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
		hostRequest.append(createDataExchange("999", NbaCyberInforceConstants.NO_OF_MATCHES, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
		hostRequest.append(createDataExchange("", NbaCyberInforceConstants.CLIENT_PLACE_HOLDER, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
		hostRequest.append(createDataExchange("999", NbaCyberInforceConstants.NUMBER_OF_POLICY_RECORDS, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
		hostRequest.append(createDataExchange("", NbaCyberInforceConstants.POLICY_PLACE_HOLDER, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
		hostRequest.append(PARTY_INQUIRY_INFO);
		return hostRequest.toString();
	}

	/**
	 * Create the inforce Payment transaction (508) - Cyberlife transaction U101
	 * @param txLife the holding inquiry
	 * @param baseProd product type
	 * @return java.lang.String the host request dxe stream
	 */
	public String create508Request(TXLife txLife, String baseProd) throws com.csc.fsg.nba.exception.NbaBaseException {
		String hostRequest = new String();
		try {
			UserAuthRequestAndTXLifeRequest userRequest = txLife.getUserAuthRequestAndTXLifeRequest();
			TXLifeRequest txlifeRequest = userRequest.getTXLifeRequestAt(0);
			OLifE olife = txlifeRequest.getOLifE();
			Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife);
			Policy policy = holding.getPolicy();
			//financial information needed to build the U101 and U104 transactions
			FinancialActivity financialActivity = null;
			FinancialActivityExtension financialActivityEx = null;
			Investment investment = null;
			SubAccount subAccount = null;
			Arrangement arrangement = null;
			ArrDestination arrDestination = null;
			//agent information
			Party party;
			Relation relation = null;
			// SPR3290 code deleted
			Producer producer;
			CarrierAppointment carrierAppointment = null;
			// SPR3290 code deleted
			boolean loan = false;
			//String accountingId = null; //ties the agent to the accounting activity 
			// Initializations
			// plansRidersData = null;
			// UCT example
			//createDataExchange(partyAddress.getAddressTypeCode(), ADDR_TYPE, NbaTableConstants.OLI_LU_ADTYPE, CYBTBL_UCT, CT_CHAR, 5);
			// No Table Lookup...
			//hostRequest = hostRequest + createDataExchange(partyAddress.getAttentionLine(), ATTEN_LINE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 46);
			// check that the incoming transaction is the right type
			// Holding/Policy object
			//
			//			Advanced or traditional / Variable funded or not	
			//			Holding.Policy.ProductType	VT_14	PUAGRQPT  UCT (CLPCTB48)
			int iFinActTot = policy.getFinancialActivityCount();
			for (int iFinAct = 0; iFinAct < iFinActTot; iFinAct++) {
				financialActivity = policy.getFinancialActivityAt(iFinAct);
				/*
				 * U101 is for regular payments and U104 is for loans. 
				 */
				if (financialActivity.getFinActivityType() == 2) {
					hostRequest =
						"6PCYWHATTODO=U104,2;" + COMP_CODE + "=" + policy.getCarrierCode() + ";" + POL_NUM + "=" + policy.getPolNumber() + ";";
					loan = true;
				} else {
					hostRequest =
						"6PCYWHATTODO=U101,2;" + COMP_CODE + "=" + policy.getCarrierCode() + ";" + POL_NUM + "=" + policy.getPolNumber() + ";";
				}
				//basic u101 format
				//set FUSACT to 2 for apply
				hostRequest = hostRequest + createDataExchange("2", "FUSACT", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				hostRequest =
					hostRequest
						+ createDataExchange(financialActivity.getFinActivityType(), "FUSTYPE", "NBA_InforceFinActType", CYBTBL_UCT, CT_DEFAULT, 0);
				//FUSAMT is needed for flexible Premium products; FUSPREM is needed for fixed Premium products
				if (isFlexiblePremium(baseProd) || NbaUtils.isReinstatement(financialActivity)) {	//SPR2248
					hostRequest =
						hostRequest
							+ createDataExchange(financialActivity.getFinActivityGrossAmt(), "FUSAMT", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				} else if (loan) { //Fixed premium and loan
					hostRequest =
						hostRequest
							+ createDataExchange(financialActivity.getFinActivityGrossAmt(), "FUSLOAN", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				} else { //fixed premium and reg premium payment
					hostRequest =
						hostRequest
							+ createDataExchange(financialActivity.getFinActivityGrossAmt(), "FUSPREM", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				}
				if (financialActivity.hasFinEffDate())
					hostRequest =
						hostRequest + createDataExchange(financialActivity.getFinEffDate(), "FUSDATE", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				//set FUSFORCE to 1 to force the transaction
				hostRequest = hostRequest + createDataExchange(FORCE_PAYMENT, "FUSFORCE", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);//SPR1703, NBA077, SPR3433
				hostRequest = hostRequest + createDataExchange(financialActivity.getUserCode(), "FUSORIG", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				//begin SPR2248
				if (!isFlexiblePremium(baseProd)) { //FUSDUEDT applicable only to fixed premiums  
                    hostRequest = hostRequest + createDataExchange(policy.getPaymentDueDate(), "FUSDUEDT", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
                }	 
				if (NbaUtils.isTermLife(policy) && policy.hasReinstatementDate()){	 
					hostRequest = hostRequest + createDataExchange(policy.getReinstatementDate(), "FUSREIND", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);	
				}	 
				if (NbaUtils.isReinstatement(financialActivity)){ 
				    hostRequest = hostRequest + createDataExchange("0", "FUSREINI", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0); //Do not calculate interest due
				}
				//end SPR2248				 
				//Get the agent that goes with the activity Account if there is one.
				String partyId = null;
				// SPR3290 code deleted
				int pTot = olife.getPartyCount();
				int rTot = olife.getRelationCount();
				// for each of our relation object
				for (int i = 0; i < rTot; i++) {
					relation = olife.getRelationAt(i);
					// SPR3290 code deleted
					// see if this a relationship for our party
					partyId = relation.getRelatedObjectID();
					// for each of our party objects
					for (int j = 0; j < pTot; j++) {
						party = olife.getPartyAt(j);
						if (party.getId().equals(partyId) && party.hasProducer()) {
							producer = party.getProducer();
							if (producer.getCarrierAppointmentCount() > 0) {
								carrierAppointment = producer.getCarrierAppointmentAt(0);
								hostRequest =
									hostRequest
										+ createDataExchange(
											carrierAppointment.getCompanyProducerID(),
											"FUSAGTID",
											CYBTRANS_NONE,
											CYBTBL_NONE,
											CT_DEFAULT,
											0);
								hostRequest =
									hostRequest
										+ createDataExchange(carrierAppointment.getCls(), "FUSAGLVL", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
							}
						}
					}
				}
				//end basic section of U101
				//setup the suspense acccouting section of the U101
				if (financialActivity.hasReferenceNo())
					hostRequest =
						hostRequest + createDataExchange(financialActivity.getReferenceNo(), "FUSVOUCH", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				//need to figure out the if here
				//hostRequest = hostRequest + createDataExchange(financialActivity.getFinActivityGrossAmt(), "FUSVAMT", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				if (financialActivity.hasGrandfatheredDate())
					hostRequest =
						hostRequest
							+ createDataExchange(financialActivity.getGrandfatheredDate(), "FUSGFTDT", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				if (financialActivity.hasBestIntRateType())
					hostRequest =
						hostRequest
							+ createDataExchange(financialActivity.getBestIntRateType(), "FUSOVITP", "OLI_LU_BESTRATE", CYBTBL_UCT, CT_DEFAULT, 0);
				if (financialActivity.hasAnnuityContributionAmt())
					hostRequest =
						hostRequest
							+ createDataExchange(financialActivity.getAnnuityContributionAmt(), "FUSFPA", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				if (financialActivity.hasMonthsPaid())
					hostRequest =
						hostRequest + createDataExchange(financialActivity.getMonthsPaid(), "FUSMODE", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				if (financialActivity.hasFirstTaxYear())
					hostRequest =
						hostRequest + createDataExchange(financialActivity.getFirstTaxYear(), "FUSFRSTX", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				if (financialActivity.hasRothIraNetContributionAmt())
					hostRequest =
						hostRequest
							+ createDataExchange(
								financialActivity.getRothIraNetContributionAmt(),
								"FUSTXNET",
								CYBTRANS_NONE,
								CYBTBL_NONE,
								CT_DEFAULT,
								0);
				for (int iExt = 0; iExt < financialActivity.getOLifEExtensionCount(); iExt++) {
					OLifEExtension olifeEx = financialActivity.getOLifEExtensionAt(iExt);
					if (olifeEx.isFinancialActivityExtension()) {
						financialActivityEx = olifeEx.getFinancialActivityExtension();
					}
				}
				if ((financialActivityEx != null) && (financialActivityEx.hasOvrdRothTaxIncomeInd()))
					hostRequest =
						hostRequest
							+ createDataExchange(financialActivityEx.getOvrdRothTaxIncomeInd(), "FUSRCCVI", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				if (financialActivity.hasRothIraConverIncAmt())
					hostRequest =
						hostRequest
							+ createDataExchange(financialActivity.getRothIraConverIncAmt(), "FUSRCCVI", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				if (financialActivity.hasPremLoadOvrRidnAmt())
					hostRequest =
						hostRequest
							+ createDataExchange(financialActivity.getPremLoadOvrRidnAmt(), "FUSLOAMT", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				if (financialActivity.hasCommPremOvrRidnAmt())
					hostRequest =
						hostRequest
							+ createDataExchange(financialActivity.getCommPremOvrRidnAmt(), "FUSCOAMT", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				if (financialActivity.hasRollloverIntAmt()) //NBA223
					hostRequest =
						hostRequest
							+ createDataExchange(financialActivity.getRollloverIntAmt(), "FUSROINT", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);//NBA223
				if (financialActivity.hasRothIra1stYrConverIncome())
					hostRequest =
						hostRequest
							+ createDataExchange(
								financialActivity.getRothIra1stYrConverIncome(),
								"FUSRCTXY",
								CYBTRANS_NONE,
								CYBTBL_NONE,
								CT_DEFAULT,
								0);
				if (financialActivity.hasReportingTaxYear())
					hostRequest =
						hostRequest
							+ createDataExchange(financialActivity.getReportingTaxYear(), "FUSTAXYR", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				if (financialActivity.hasTaxOverriddenInd())
					hostRequest =
						hostRequest
							+ createDataExchange(financialActivity.getTaxOverriddenInd(), "FUSPRMCD", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				if (financialActivity.hasCostBasisAdjAmt())
					hostRequest =
						hostRequest
							+ createDataExchange(financialActivity.getCostBasisAdjAmt(), "FUSCOSTB", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				if (financialActivity.hasCostBasis())
					hostRequest =
						hostRequest + createDataExchange(financialActivity.getCostBasis(), "FUSCOSTB", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				if (financialActivity.hasIntPostingRate())
					hostRequest =
						hostRequest
							+ createDataExchange(financialActivity.getIntPostingRate(), "FUSDEPRT", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				//extension??
				//hostRequest = hostRequest + createDataExchange(financialActivity.getTSALoanDate(), "FUSLRCTL", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				//Get the finacial activity extension
				if (financialActivityEx != null && !loan) {
					//SPR2121 Deleted code for FUSCORR
					if (financialActivityEx.hasPUAUnisexAdjust())
						hostRequest =
							hostRequest
								+ createDataExchange(financialActivityEx.getPUAUnisexAdjust(), "FUSUNSEX", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					if (financialActivityEx.hasPUAUnisexAdjust())
						hostRequest =
							hostRequest
								+ createDataExchange(financialActivityEx.getPUAUnisexAdjust(), "FUSACBAL", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				} //end if
				if (financialActivityEx != null && loan) {
					if (financialActivityEx.hasTSALoanDate())
						hostRequest =
							hostRequest
								+ createDataExchange(financialActivityEx.getTSALoanDate(), "FUSLRCTL", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				} //end if
				for (int iAct = 0; iAct < financialActivity.getAccountingActivityCount(); iAct++) {
					AccountingActivity accountActivity = financialActivity.getAccountingActivityAt(iAct);
					hostRequest =
						hostRequest + createDataExchange(accountActivity.getActivityDate(), "FUSCFDAT", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					hostRequest =
						hostRequest + createDataExchange(accountActivity.getAccountNumber(), "FU2ACTNO", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					hostRequest =
						hostRequest + createDataExchange(accountActivity.getAccountAmount(), "FU2ACTAM", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					hostRequest =
						hostRequest
							+ createDataExchange(
								accountActivity.getAccountDebitCreditType(),
								"FU2ACTDC",
								"OLI_LU_ACCTDBCRTYPE",
								CYBTBL_UCT,
								CT_DEFAULT,
								0);
				}
			}
			investment = holding.getInvestment();
			if (investment != null) {
				int iSubAccTot = investment.getSubAccountCount();
				for (int iSubAcct = 0; iSubAcct < iSubAccTot; iSubAcct++) {
					subAccount = investment.getSubAccountAt(iSubAcct);
					hostRequest = hostRequest + createDataExchange(subAccount.getProductCode(), "FU3FUND", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				}
			}
			for (int iArr = 0; iArr < holding.getArrangementCount(); iArr++) {
				arrangement = holding.getArrangementAt(iArr);
				for (int iArrD = 0; iArrD < arrangement.getArrDestinationCount(); iArrD++) {
					arrDestination = arrangement.getArrDestinationAt(iArrD);
					hostRequest =
						hostRequest
							+ createDataExchange(arrDestination.getTransferAmtType(), "FU3ALTYP", "OLI_TRANSAMTTYPE", CYBTBL_UCT, CT_DEFAULT, 0);
					hostRequest =
						hostRequest + createDataExchange(arrDestination.getTransferAmt(), "FU3ALAMT", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					hostRequest =
						hostRequest + createDataExchange(arrDestination.getTransferPct(), "FU3ALPCT", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				}
			}
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_PARSE, e);
		}
		return hostRequest;
	}
	/**
	 * Answer whether the the policy product type is a flexible premium
	 * @param policyProductType
	 * @return boolean <code>true</code> for flexible premium, 
 	 * <code>false</code> if for not
	 */
	private boolean isFlexiblePremium(String policyProductType) {
		if (policyProductType.equals("F") || policyProductType.equals("U")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Create the DXE for Increase riders (26SR), supplemental benefits (26SB),
	 * substandard rating(s) to the increase rider (26SE) and agent associated 
	 * with the increase rider (26SA). 
	 * @param txLife the holding inquiry 
	 * @return java.lang.String the host request DXE stream
	 * @throws NbaBaseException
	 */
	//NBA077 new method
	public String createIncreaseRequest(TXLife txLife) throws NbaBaseException {
		StringBuffer requestDXE = new StringBuffer();
		NbaTXLife nbaTXLife = new NbaTXLife();
		nbaTXLife.setTXLife(txLife);
		
		Policy policy = nbaTXLife.getPolicy();
		compCode = policy.getCarrierCode();
		if (policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() != null
			&& policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isLife()) {
			Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
			for (int i = 0; i < life.getCoverageCount(); i++) {
				Coverage coverage = life.getCoverageAt(i);

				//Increase rider
				requestDXE.append(create26SRRequest(coverage, nbaTXLife));

				//DXE for benefits
				for (int j = 0; j < coverage.getCovOptionCount(); j++) {
					CovOption conOption = coverage.getCovOptionAt(j);
					requestDXE.append(create26SBRequest(conOption, coverage, nbaTXLife));
				}

				//DXE for ratings
				for (int k = 0; k < coverage.getLifeParticipantCount(); k++) {
					LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(k);
					// SPR3290 code deleted
					if ((lifeParticipant.hasPermTableRating() && lifeParticipant.getPermTableRating() > 0)
						|| (lifeParticipant.hasPermFlatExtraAmt() && lifeParticipant.getPermFlatExtraAmt() > 0)
						|| (lifeParticipant.hasTempTableRating() && lifeParticipant.getTempTableRating() > 0)
						|| (lifeParticipant.hasTempFlatExtraAmt() && lifeParticipant.getTempFlatExtraAmt() > 0)) {
						
						requestDXE.append(create26SERequest(lifeParticipant, coverage, nbaTXLife));
						for (int z = 0; z < lifeParticipant.getSubstandardRatingCount(); z++) {
							SubstandardRating substandardRating = lifeParticipant.getSubstandardRatingAt(z);
							requestDXE.append(create26SERequest(substandardRating, lifeParticipant, coverage, nbaTXLife));
						}
					}
				}

				//DXE fot separate commission agents
				CoverageExtension coverageExtension = NbaUtils.getFirstCoverageExtension(coverage);
				if (coverageExtension != null) {
					for (int y = 0; y < coverageExtension.getCommissionCalcActivityCount(); y++) {
						CommissionCalcActivity commissionCalcActivity = coverageExtension.getCommissionCalcActivityAt(y);
						commissionCalcActivity.setCommissionCalcActivityKey(String.valueOf(y));
						requestDXE.append(create26SARequest(commissionCalcActivity, coverage, nbaTXLife));
					}
				}
			}
		}
		if(requestDXE.length() > 0){
			requestDXE.insert(0,"6PCY");
		}
		return requestDXE.toString();
	}
	
	/**
	 * Creates 26SR request for a coverage
	 * @param coverage the coverage object
	 * @param nbaTXLife the NbaTXLife object
	 * @return java.lang.String the host request DXE stream
	 * @throws NbaBaseException
	 */	
	//NBA077 new method
	public String create26SRRequest(Coverage coverage, NbaTXLife nbaTXLife) throws NbaBaseException {
		StringBuffer requestDXE = new StringBuffer();
		OLifE olife = nbaTXLife.getOLifE();
		Policy policy = nbaTXLife.getPolicy();
		requestDXE.append("WHATTODO=26SR,2;");
		requestDXE.append(COMP_CODE);
		requestDXE.append("=");
		requestDXE.append( policy.getCarrierCode());
		requestDXE.append(";");
		requestDXE.append(POL_NUM);
		requestDXE.append("=");
		requestDXE.append(policy.getPolNumber());
		requestDXE.append(";");
		
		//FDERPHS
		requestDXE.append(
			createDataExchange(zeroPadString(coverage.getCoverageKey(), 2), INF_COVERAGE_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
		//FDERPRSI
		LifeParticipant lifeParticipant = NbaUtils.findPrimaryInsuredLifeParticipant(coverage);
		Relation relation = NbaUtils.getRelationForParty(lifeParticipant.getPartyID(), olife.getRelation().toArray());
		String personIdn =
			getCyberValue(formatCyberLong(relation.getRelationRoleCode()), CYBTRANS_ROLES, CYBTBL_RELATION_ROLE, compCode, DEFAULT_COVID);
		requestDXE.append(createDataExchange(personIdn.concat(relation.getRelatedRefID()), INF_RIDER_PERSON, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 4));
		//FDERSRCH 
		requestDXE.append(createDataExchange(coverage.getProductCode(), INF_COVERAGE_PRODUCT_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 12));
		//FDERUNIT
		requestDXE.append(createDataExchange(coverage.getCurrentNumberOfUnits(), INF_COVERAGE_UNITS, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
		//FDERPOPT		
		requestDXE.append(
			createDataExchange(
				coverage.getDeathBenefitOptType(),
				INF_COVERAGE_DEATH_BENEFIT_OPT,
				NbaTableConstants.OLI_LU_DTHBENETYPE,
				CYBTBL_UCT,
				CT_CHAR,
				1));
		//FDERISS		
		requestDXE.append(createDataExchange(coverage.getEffDate(),INF_COVERAGE_EFF_DATE,CYBTRANS_NONE,CYBTBL_NONE,CT_DEFAULT,0));
		
		CoverageExtension coverageExtension = NbaUtils.getFirstCoverageExtension(coverage);
		if (coverageExtension != null) {
			//FDERUSOI
			requestDXE.append(
				createDataExchange(
					coverageExtension.getUnisexOverride(),
					INF_COVERAGE_DEATH_UNISEX_OVERRIDE,
					CYBTRANS_NONE,
					CYBTBL_NONE,
					CT_CHAR,
					1));
			//FDERUSCD
			requestDXE.append(
				createDataExchange(
					coverageExtension.getUnisexCode(),
					INF_COVERAGE_DEATH_UNISEX_CODE,
					NbaTableConstants.OLIEXT_LU_UNISEXCODE,
					CYBTBL_UCT,
					CT_CHAR,
					1));
			//FDERUSSC
			requestDXE.append(
				createDataExchange(
					coverageExtension.getUnisexSubseries(),
					INF_COVERAGE_DEATH_UNISEX_SUBSERIES,
					NbaTableConstants.OLIEXT_LU_UNISEXSUB,
					CYBTBL_UCT,
					CT_CHAR,
					3));

			if (!NbaUtils.isJointLife(coverage.getLivesType())) {
				//FDERCLAS
				requestDXE.append(
					createDataExchange(coverageExtension.getRateClass(), INF_COVERAGE_RATE_CLASS, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1));
			} else {
				//FDERCLS2
				requestDXE.append(
					createDataExchange(coverageExtension.getRateClass(), INF_COVERAGE_JOINT_RATE_CLASS, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1));
			}
			//If an agent is associated with this new rider
			if (coverageExtension.getCommissionCalcActivityCount() > 0) {
				//FDERCPHS
				requestDXE.append(
					createDataExchange(
						zeroPadString(coverage.getCoverageKey(), 2),
						INF_COVERAGE_COMM_PHASE,
						CYBTRANS_NONE,
						CYBTBL_NONE,
						CT_DEFAULT,
						0));
			}
		}
		
		//FDEDPTDK
		if(policy.getApplicationInfo() != null){
			requestDXE.append(createDataExchange(policy.getApplicationInfo().getUserCode(), INF_USER_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 8));
		}
		
		return requestDXE.toString();
	}
	
	/**
	 * Creates 26SB request for a covotion
	 * @param covOption the CovOption object
	 * @param coverage the coverage object 
	 * @param nbaTXLife the NbaTXLife object
	 * @return java.lang.String the host request DXE stream
	 * @throws NbaBaseException
	 */	
	//NBA077 new method
	public String create26SBRequest(CovOption covOption, Coverage coverage, NbaTXLife nbaTXLife) throws NbaBaseException {
		StringBuffer requestDXE = new StringBuffer();
		OLifE olife = nbaTXLife.getOLifE();
		Policy policy = nbaTXLife.getPolicy();
		requestDXE.append("WHATTODO=26SB,2;");
		requestDXE.append(COMP_CODE);
		requestDXE.append("=");
		requestDXE.append(policy.getCarrierCode());
		requestDXE.append(";");
		requestDXE.append(POL_NUM);
		requestDXE.append("=");
		requestDXE.append(policy.getPolNumber());
		requestDXE.append(";");

		//FDEBPHAS
		requestDXE.append(
			createDataExchange(zeroPadString(coverage.getCoverageKey(), 2), INF_COVOPTION_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
		//FDEBPRSI
		LifeParticipant lifeParticipant = NbaUtils.findPrimaryInsuredLifeParticipant(coverage);
		Relation relation = NbaUtils.getRelationForParty(lifeParticipant.getPartyID(), olife.getRelation().toArray());
		String personIdn =
			getCyberValue(formatCyberLong(relation.getRelationRoleCode()), CYBTRANS_ROLES, CYBTBL_RELATION_ROLE, compCode, DEFAULT_COVID);
		requestDXE.append(
			createDataExchange(personIdn.concat(relation.getRelatedRefID()), INF_BENEFIT_PERSON, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 4));
		//FDEBTYPE 
		requestDXE.append(createDataExchange(covOption.getProductCode(), INF_COVOPTION_PRODUCT_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 12));
		//FDEBCOMM
		requestDXE.append(
			createDataExchange(
				covOption.getRatingCommissionRule(),
				INF_COVOPTION_RATING_COMM_RULE,
				NbaTableConstants.OLIEXT_LU_COMMISCODE,
				CYBTBL_UCT,
				CT_CHAR,
				1));
		//FDEBUNIT
		requestDXE.append(createDataExchange(covOption.getOptionNumberOfUnits(), INF_COVOPTION_UNITS, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));

		if (covOption.getEffDate() != null) {
			GregorianCalendar calender = new GregorianCalendar();
			calender.setTime(covOption.getEffDate());
			//FDEBISYR
			requestDXE.append(createDataExchange(calender.get(Calendar.YEAR), INF_COVOPTION_EFF_YEAR, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
			//FDEBISMO
			requestDXE.append(
				createDataExchange(
					zeroPadString(String.valueOf((calender.get(Calendar.MONTH) + 1)), 2),
					INF_COVOPTION_EFF_MONTH,
					CYBTRANS_NONE,
					CYBTBL_NONE,
					CT_DEFAULT,
					0));
		}

		//FDEBCDAT		
		requestDXE.append(createDataExchange(covOption.getTermDate(), INF_COVOPTION_TERM_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
		//FDEBRAT
		if (covOption.getPermPercentageLoading() > 100) {
			requestDXE.append(
				createDataExchange(covOption.getPermPercentageLoading(), INF_COVOPTION_PCT_LOADING, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0));
		}
		
		//FDEDPTDK
		if(policy.getApplicationInfo() != null){
			requestDXE.append(createDataExchange(policy.getApplicationInfo().getUserCode(), INF_USER_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 8));
		}
		
		return requestDXE.toString();
	}
	
	/**
	 * Creates 26SB request for a substandard rating(2-n rating)
	 * @param substandardRating the SubstandardRating object
	 * @param lifeParticipant the LifeParticipant object
	 * @param coverage the coverage object 
	 * @param nbaTXLife the NbaTXLife object
	 * @return java.lang.String the host request DXE stream
	 * @throws NbaBaseException
	 */	
	//NBA077 new method
	public String create26SERequest(SubstandardRating substandardRating, LifeParticipant lifeParticipant, Coverage coverage, NbaTXLife nbaTXLife)
		throws NbaBaseException {
		StringBuffer requestDXE = new StringBuffer();
		// SPR3290 code deleted
		Policy policy = nbaTXLife.getPolicy();
		requestDXE.append("WHATTODO=26SE,2;");
		requestDXE.append(COMP_CODE);
		requestDXE.append("=");
		requestDXE.append(policy.getCarrierCode());
		requestDXE.append(";");
		requestDXE.append(POL_NUM);
		requestDXE.append("=");
		requestDXE.append(policy.getPolNumber());
		requestDXE.append(";");

		//FDEECPH
		requestDXE.append(
			createDataExchange(zeroPadString(coverage.getCoverageKey(), 2), INF_RATING_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
		//FDEEREAS 
		requestDXE.append(
			createDataExchange(substandardRating.getRatingReason(), INF_RATING_REASON, NbaTableConstants.NBA_RATING_REASON, CYBTBL_UCT, CT_CHAR, 1));
		//FDEECOMM
		requestDXE.append(
			createDataExchange(
				substandardRating.getRatingCommissionRule(),
				INF_RATING_COMM,
				NbaTableConstants.OLIEXT_LU_COMMISCODE,
				CYBTBL_UCT,
				CT_CHAR,
				1));
		if (substandardRating.getPermTableRating() > 0) {
			//FDEETABL
			requestDXE.append(
				createDataExchange(
					substandardRating.getPermTableRating(),
					INF_RATING_TABLE,
					NbaTableConstants.OLI_LU_RATINGS,
					CYBTBL_UCT,
					CT_CHAR,
					2));
			//FELETYP
			requestDXE.append(createDataExchange("1", INF_RATING_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
		} else if (substandardRating.getTempTableRating() > 0) {
			//FDEETABL
			requestDXE.append(
				createDataExchange(
					substandardRating.getTempTableRating(),
					INF_RATING_TABLE,
					NbaTableConstants.OLI_LU_RATINGS,
					CYBTBL_UCT,
					CT_CHAR,
					2));
			//FELETYP
			requestDXE.append(createDataExchange("3", INF_RATING_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
			//FDEECDAT
			requestDXE.append(
				createDataExchange(
					formatCyberDate(substandardRating.getTempTableRatingEndDate()),
					INF_RATING_TABLE_END_DATE,
					CYBTRANS_NONE,
					CYBTBL_NONE,
					CT_CHAR,
					9));
		}

		SubstandardRatingExtension substandardRatingExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
		if (substandardRatingExtension != null) {
			//FDEEEFDT
			requestDXE.append(
				createDataExchange(
					formatCyberDate(substandardRatingExtension.getEffDate()),
					INF_RATING_EFF_DATE,
					CYBTRANS_NONE,
					CYBTBL_NONE,
					CT_CHAR,
					9));
		}

		//FDEDPTDK
		if (policy.getApplicationInfo() != null) {
			requestDXE.append(createDataExchange(policy.getApplicationInfo().getUserCode(), INF_USER_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 8));
		}

		return requestDXE.toString();
	}

	/**
	 * Creates 26SB request for first occurance of rating.
	 * @param lifeParticipant the LifeParticipant object
	 * @param coverage the coverage object 
	 * @param nbaTXLife the NbaTXLife object
	 * @return java.lang.String the host request DXE stream
	 * @throws NbaBaseException
	 */	
	//NBA077 new method
	public String create26SERequest(LifeParticipant lifeParticipant, Coverage coverage, NbaTXLife nbaTXLife) throws NbaBaseException {
		StringBuffer requestDXE = new StringBuffer();
		// SPR3290 code deleted
		Policy policy = nbaTXLife.getPolicy();
		requestDXE.append("WHATTODO=26SE,2;");
		requestDXE.append(COMP_CODE);
		requestDXE.append("=");
		requestDXE.append(policy.getCarrierCode());
		requestDXE.append(";");
		requestDXE.append(POL_NUM);
		requestDXE.append("=");
		requestDXE.append(policy.getPolNumber());
		requestDXE.append(";");

		//FDEECPH
		requestDXE.append(
			createDataExchange(zeroPadString(coverage.getCoverageKey(), 2), INF_RATING_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
		//FDEEREAS 
		requestDXE.append(
			createDataExchange(lifeParticipant.getRatingReason(), INF_RATING_REASON, NbaTableConstants.NBA_RATING_REASON, CYBTBL_UCT, CT_CHAR, 1));
		//FDEECOMM
		requestDXE.append(
			createDataExchange(
				lifeParticipant.getRatingCommissionRule(),
				INF_RATING_COMM,
				NbaTableConstants.OLIEXT_LU_COMMISCODE,
				CYBTBL_UCT,
				CT_CHAR,
				1));
		if (lifeParticipant.getPermTableRating() > 0) {
			//FDEETABL
			requestDXE.append(
				createDataExchange(lifeParticipant.getPermTableRating(), INF_RATING_TABLE, NbaTableConstants.OLI_LU_RATINGS, CYBTBL_UCT, CT_CHAR, 2));
			//FELETYP
			requestDXE.append(createDataExchange("1", INF_RATING_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
		} else if (lifeParticipant.getTempTableRating() > 0) {
			//FDEETABL
			requestDXE.append(
				createDataExchange(lifeParticipant.getTempTableRating(), INF_RATING_TABLE, NbaTableConstants.OLI_LU_RATINGS, CYBTBL_UCT, CT_CHAR, 2));
			//FELETYP
			requestDXE.append(createDataExchange("3", INF_RATING_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
			//FDEECDAT
			requestDXE.append(
				createDataExchange(
					formatCyberDate(lifeParticipant.getTempTableRatingEndDate()),
					INF_RATING_TABLE_END_DATE,
					CYBTRANS_NONE,
					CYBTBL_NONE,
					CT_CHAR,
					9));
		}

		LifeParticipantExtension lifeParticipantExtension = NbaUtils.getFirstLifeParticipantExtension(lifeParticipant);
		if (lifeParticipantExtension != null) {
			//FDEEEFDT
			requestDXE.append(
				createDataExchange(
					formatCyberDate(lifeParticipantExtension.getEffDate()),
					INF_RATING_EFF_DATE,
					CYBTRANS_NONE,
					CYBTBL_NONE,
					CT_CHAR,
					9));
		}

		//FDEDPTDK
		if (policy.getApplicationInfo() != null) {
			requestDXE.append(createDataExchange(policy.getApplicationInfo().getUserCode(), INF_USER_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 8));
		}

		return requestDXE.toString();
	}
	
	/**
	 * Creates 26SA request for a separate commission agents
	 * @param CommissionCalcActivity the commissionCalcActivity object
	 * @param coverage the coverage object 
	 * @param nbaTXLife the NbaTXLife object
	 * @return java.lang.String the host request DXE stream
	 * @throws NbaBaseException
	 */	
	//NBA077 new method
	public String create26SARequest(CommissionCalcActivity commissionCalcActivity,Coverage coverage, NbaTXLife nbaTXLife) throws NbaBaseException {
		StringBuffer requestDXE = new StringBuffer();
		// SPR3290 code deleted
		Policy policy = nbaTXLife.getPolicy();
		requestDXE.append("WHATTODO=26SA,2;");
		requestDXE.append(COMP_CODE);
		requestDXE.append("=");
		requestDXE.append(policy.getCarrierCode());
		requestDXE.append(";");
		requestDXE.append(POL_NUM);
		requestDXE.append("=");
		requestDXE.append(policy.getPolNumber());
		requestDXE.append(";");

		//FDEAGTID 
		requestDXE.append(
			createDataExchange(commissionCalcActivity.getCompanyProducerID(), INF_COMM_AGENT_ID, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 0));
		//FDEAGLVL default to 01
		requestDXE.append(createDataExchange("01", INF_COMM_AGENT_ROLECODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 0));
		//FDEAGPHS
		requestDXE.append(
			createDataExchange(zeroPadString(coverage.getCoverageKey(), 2), INF_COMM_AGENT_COV_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
		//FDEAGPHS
		requestDXE.append(
			createDataExchange(
				zeroPadString(commissionCalcActivity.getCommissionCalcActivityKey(), 2),
				INF_COMM_AGENT_SUB_PHASE,
				CYBTRANS_NONE,
				CYBTBL_NONE,
				CT_DEFAULT,
				0));
		//FDEAGEDT
		requestDXE.append(createDataExchange(formatCyberDate(new Date()), INF_COMM_AGENT_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9));
		//FDEAGPS
		// ALPC7 code deleted

		//FDEDPTDK
		if (policy.getApplicationInfo() != null) {
			requestDXE.append(createDataExchange(policy.getApplicationInfo().getUserCode(), INF_USER_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 8));
		}

		return requestDXE.toString();
	}
	/**
	 * Create DXE for the Common information portion of the validation request.
	 * @param hostRequest
	 * @param policy
	 */
	// NBA112 New Method
	protected void createAgentValidationCommon(StringBuffer hostRequest, Policy policy) {
		ApplicationInfo applicationInfo = policy.getApplicationInfo();
		// SPR3290 code deleted
		hostRequest.append(HOLDING_TYPE); //NBA112 
		//SPR3054 code deleted
		//			Variable funded  - Holding.Policy.ProductType	VT_14	PUAGRQPT  UCT (CLPCTB48)
		hostRequest.append(createDataExchange(policy.getProductType(), "PUAGRQPT", NbaTableConstants.OLI_LU_POLPROD, CYBTBL_UCT, CT_CHAR, 1));
		//			Company Code - Holding.Policy.CarrierCode	VT_BSTR	
		hostRequest.append(createDataExchange(policy.getCarrierCode(), "PUAGRQCO", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
		//			Issue Date - Holding.Policy.EffDate	PUAGRQPE
		if (policy.hasEffDate()) {
			hostRequest.append(createDataExchange(policy.getEffDate(), "PUAGRQPE", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9));
		}
		//			Application Type - Holding.Policy.ApplicationInfo.ApplicationType	PUAGRQAT	
		hostRequest.append(createDataExchange(applicationInfo.getApplicationType(), "PUAGRQAT", NbaTableConstants.OLI_LU_APPTYPE, CYBTBL_UCT, CT_CHAR, 1));
		//			Application Jurisdiction - Holding.Policy.ApplicationInfo.ApplicationJurisdiction	PUAGRQSW
		hostRequest.append(createDataExchange(applicationInfo.getApplicationJurisdiction(), "PUAGRQSW", "NBA_STATES", CYBTBL_STATE_TC, CT_CHAR, 3));
		//			Application Signed Date	RequestedIssueDate - Holding.Policy.ApplicationInfo.SignedDate	PUAGRQSD
		hostRequest.append(createDataExchange(applicationInfo.getSignedDate(), "PUAGRQSD", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9));
		//			Issue Date - Holding.Policy.EffDate	PUAGRQIS
		if (policy.hasEffDate()) {
			hostRequest.append(createDataExchange(policy.getEffDate(), "PUAGRQIS", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9));
		}
	}
	/**
	 * Create DXE for the Agent information portion of the validation request.
	 * @param hostRequest
	 * @param olife
	 * @return
	 */
	// NBA112 New Method
	protected String createAgentValidationAgentInfo(StringBuffer hostRequest, OLifE olife) {
		Party party;
		Relation relation = null;
		Producer producer;
		CarrierAppointment carrierAppointment = null;
		RelationProducerExtension relProducerEx = null;
		String sitCode = "";
		// PUAGRQNA is calculated by DXE.
		int i, j;
		String partyId;
		// SPR3290 code deleted
		int pTot = olife.getPartyCount();
		int rTot = olife.getRelationCount();
		for (i = 0; i < pTot; i++) {
			party = olife.getPartyAt(i);
			if (party.hasProducer()) {
				producer = party.getProducer();
				if (producer.getCarrierAppointmentCount() > 0) {
					carrierAppointment = producer.getCarrierAppointmentAt(0);
					partyId = party.getId();
					for (j = 0; j < rTot; j++) {
						relation = olife.getRelationAt(j);
						String relatedObjectID = relation.getRelatedObjectID();
						// see if this a relationship for our party
						if (relatedObjectID.equals(partyId)) {
							//			Agent Id - Party.Producer.CarrierAppointment.CompanyProducerID	PUAGRQAG
							hostRequest.append(createDataExchange(carrierAppointment.getCompanyProducerID(), "PUAGRQAG", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 11));
							// 			Interest Percent - Relation.InterestPercent PUAGRQPS
							if (relation.hasInterestPercent()) {
								hostRequest.append(createDataExchange((relation.getInterestPercent() / 100), "PUAGRQPS", CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0));
							}
							// 			Volume Share Percent (Relation.RelationProducer) - PUAGRQPV
							if (relation.hasVolumeSharePct()) {
								hostRequest.append(createDataExchange((relation.getVolumeSharePct() / 100), "PUAGRQPV", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
							}
							// 			Relation Role Code (Relation.RelationRoleCode) - PUAGRQLV
							switch ((int) relation.getRelationRoleCode()) {
								case 37 : // writing agent
									hostRequest.append(createDataExchange("01", "PUAGRQLV", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
									break;
								case 52 : // addl writing agent
									hostRequest.append(createDataExchange("01", "PUAGRQLV", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
									break;
							}
							for (int k = 0; k < relation.getOLifEExtensionCount(); k++) {
								OLifEExtension olifeEx = relation.getOLifEExtensionAt(k);
								if (olifeEx.isRelationProducerExtension()) {
									relProducerEx = olifeEx.getRelationProducerExtension();
									if (relProducerEx.hasSituationCode()) {
										sitCode = relProducerEx.getSituationCode();
									}
								}
							}
						}
					}
				}
			}
		}
		return sitCode;
	}
	/**
	 * Create DXE for the Coverage information portion of the validation request.
	 * @param hostRequest
	 * @param life
	 * @param sitCode
	 */
	// NBA112 New Method
	protected void createAgentValidationLifeInfo(StringBuffer hostRequest, Life life, String sitCode) {
		int iCovTot = life.getCoverageCount();
		CoverageExtension coverageEx = null;
		LifeParticipant lifeParticipant;
		for (int iCov = 0; iCov < iCovTot; iCov++) {
			Coverage coverage = life.getCoverageAt(iCov);
			coverageEx = NbaUtils.getFirstCoverageExtension(coverage);
			//			Holding.Policy.Life.Coverage.CoverageKey  	PUAGRQPH
			hostRequest.append(createDataExchange(coverage.getCoverageKey(), "PUAGRQPH", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
			//			Holding.Policy.Life.FaceAmt	 	PUAGRQFA
			if (coverage.hasCurrentAmt()) {
				hostRequest.append(createDataExchange(coverage.getCurrentAmt(), "PUAGRQFA", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
			}
			// 			Class/Base/Sub - PUAGRQCL PUAGRQBS PUAGRQSB
			if (coverageEx != null) {
				if (coverageEx.hasValuationClassType()) {
					hostRequest.append(createDataExchange(coverageEx.getValuationClassType(), "PUAGRQCL", "OLI_LU_VALCLASS", CYBTBL_UCT, CT_DEFAULT, 0));
				}
				hostRequest.append(createDataExchange(coverageEx.getValuationBaseSeries(), "PUAGRQBS", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
				hostRequest.append(createDataExchange(coverageEx.getValuationSubSeries(), "PUAGRQSB", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
			}
			// 			Coverage Code - PUAGRQCC=B=base, R=Rider, O=Other
			if (OLI_COVIND_BASE == coverage.getIndicatorCode()) { // BASE COVERAGE
				hostRequest.append(createDataExchange("B", "PUAGRQCC", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
			} else {
				hostRequest.append(createDataExchange("R", "PUAGRQCC", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
			}
			// 			Holding.Policy.Life.Coverage.LifeCovTypeCode - PUAGRQCT	
			if (coverage.hasLifeCovTypeCode()) {
				hostRequest.append(createDataExchange(coverage.getLifeCovTypeCode(), "PUAGRQCT", NbaTableConstants.OLI_LU_COVTYPE, CYBTBL_UCT, CT_CHAR, 1));
			}
			// 			Holding.Policy.Life.Coverage.CurrentAmt	- PUAGRQAC
			hostRequest.append(createDataExchange(coverage.getCurrentAmt(), "PUAGRQAC", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
			lifeParticipant = NbaUtils.getInsurableLifeParticipant(coverage);
			if (lifeParticipant != null) {
				// 	Holding.Policy.Life.Coverage.LifeParticipant.LifeParticipantRoleCode - PUAGRQID
				hostRequest.append(createDataExchange(lifeParticipant.getLifeParticipantRoleCode(), "PUAGRQID", CYBTRANS_ROLES, CYBTBL_PARTIC_ROLE, CT_CHAR, 2));
				// 	Holding.Policy.Life.Coverage.LifeParticipant.IssueAge - PUAGRQIA
				if (lifeParticipant.hasIssueAge()) {
					hostRequest.append(createDataExchange(lifeParticipant.getIssueAge(), "PUAGRQIA", CYBTRANS_NONE, CYBTBL_NONE, CT_USHORT, 0));
				}
			}
			if (coverageEx != null) {
				if (coverageEx.hasCommissionPlanCode()) {
					hostRequest.append(createDataExchange(coverageEx.getCommissionPlanCode(), "PUAGRQCI", CYBTRANS_NONE, CYBTBL_NONE, CT_USHORT, 0));
				} else {
					hostRequest.append(createDataExchange("0", "PUAGRQCI", CYBTRANS_NONE, CYBTBL_NONE, CT_USHORT, 0));
				}
				if (coverage.hasTermDate()) {
					hostRequest.append(createDataExchange(coverage.getTermDate(), "PUAGRQME", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9));
				}
				if (coverageEx.hasPayUpDate()) {
					hostRequest.append(createDataExchange(coverageEx.getPayUpDate(), "PUAGRQPU", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9));
				}
			}
			hostRequest.append(createDataExchange(sitCode, "PUAGRQSC", "OLIEXT_LU_SITCODE", CYBTBL_UCT_BY_INDEX_TRANS, CT_CHAR, 0));
		}
	}
	/**
	 * Create DXE for the Annuity and Annuity Rider information portion of the validation request.
	 * @param hostRequest
	 * @param policy
	 * @param annuity
	 * @param sitCode
	 */
	// NBA112 New Method
	protected void createAgentValidationAnnuityInfo(StringBuffer hostRequest, Policy policy, Annuity annuity, String sitCode) {
		AnnuityExtension annuityEx = null;
		Payout payout = null;
		Participant participant = null;
		hostRequest.append(createDataExchange(annuity.getAnnuityKey(), "PUAGRQPH", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
		hostRequest.append(createDataExchange("B", "PUAGRQCC", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
		annuityEx = NbaUtils.getFirstAnnuityExtension(annuity);
		if (annuityEx != null) {
			if (annuityEx.hasValuationClassType())
				hostRequest.append(createDataExchange(annuityEx.getValuationClassType(), "PUAGRQCL", "OLI_LU_VALCLASS", CYBTBL_UCT, CT_DEFAULT, 0));
			if (annuityEx.hasValuationBaseSeries())
				hostRequest.append(createDataExchange(annuityEx.getValuationBaseSeries(), "PUAGRQBS", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
			if (annuityEx.hasValuationSubSeries())
				hostRequest.append(createDataExchange(annuityEx.getValuationSubSeries(), "PUAGRQSB", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
			if (annuityEx.hasCommissionPlanCode()) {
				hostRequest.append(createDataExchange(annuityEx.getCommissionPlanCode(), "PUAGRQCI", CYBTRANS_NONE, CYBTBL_NONE, CT_USHORT, 0));
			} else {
				hostRequest.append(createDataExchange("0", "PUAGRQCI", CYBTRANS_NONE, CYBTBL_NONE, CT_USHORT, 0));
			}
		}
		Date termDate;
		if (policy.hasTermDate()) {
			termDate = policy.getTermDate();
		} else {
			termDate = policy.getEffDate();
		}
		hostRequest.append(createDataExchange(termDate, "PUAGRQME", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9)); //SPR1986
		hostRequest.append(createDataExchange(termDate, "PUAGRQPU", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9)); //SPR1986
		for (int iPay = 0; iPay < annuity.getPayoutCount(); iPay++) {
			payout = annuity.getPayoutAt(iPay);
			participant = NbaUtils.findPrimaryInsuredParticipant(payout.getParticipant());
			if (participant != null) {
				// Holding.Policy.Annuity.Payout.Participant.LifeParticipantRoleCode - PUAGRQID
				hostRequest.append(createDataExchange(participant.getParticipantRoleCode(), "PUAGRQID", CYBTRANS_ROLES, CYBTBL_PARTIC_ROLE, CT_CHAR, 2));
				// Holding.Policy.Annuity.Payout.Participant.IssueAge - PUAGRQIA
				if (participant.hasIssueAge()) {
					hostRequest.append(createDataExchange(participant.getIssueAge(), "PUAGRQIA", CYBTRANS_NONE, CYBTBL_NONE, CT_USHORT, 0));
				}
			}
		}
		hostRequest.append(createDataExchange(sitCode, "PUAGRQSC", "OLIEXT_LU_SITCODE", CYBTBL_UCT_BY_INDEX_TRANS, CT_CHAR, 0));
		Rider rider = NbaUtils.getFirstAnnuityRider(annuity);
		if (rider != null) {
			AnnuityRiderExtension annuityRiderEx = NbaUtils.getFirstAnnuityRiderExtension(rider);
			//			Coverage phase -  Holding.Policy.Annuity.Rider.RiderKey PUAGRQPH
			hostRequest.append(createDataExchange(rider.getRiderKey(), "PUAGRQPH", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
			hostRequest.append(createDataExchange("R", "PUAGRQCC", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
			participant = NbaUtils.getInsurableParticipant(rider);
			if (participant != null) {
				// Holding.Policy.Life.Coverage.LifeParticipant.LifeParticipantRoleCode - PUAGRQID
				hostRequest.append(createDataExchange(participant.getParticipantRoleCode(), "PUAGRQID", CYBTRANS_ROLES, CYBTBL_PARTIC_ROLE, CT_CHAR, 2));
				// Holding.Policy.Life.Coverage.LifeParticipant.IssueAge - PUAGRQIA
				if (participant.hasIssueAge()) {
					hostRequest.append(createDataExchange(participant.getIssueAge(), "PUAGRQIA", CYBTRANS_NONE, CYBTBL_NONE, CT_USHORT, 0));
				}
			}
			hostRequest.append(createDataExchange(rider.getTotAmt(), "PUAGRQAC", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
			if (rider.hasTermDate()) {
				termDate = rider.getTermDate();
			}
			hostRequest.append(createDataExchange(termDate, "PUAGRQME", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9)); //SPR1986
			hostRequest.append(createDataExchange(termDate, "PUAGRQPU", CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9)); //SPR1986
			if (annuityRiderEx != null) {
				if (annuityRiderEx.hasValuationClassType()) {
					hostRequest.append(createDataExchange(annuityRiderEx.getValuationClassType(), "PUAGRQCL", "OLI_LU_VALCLASS", CYBTBL_UCT, CT_DEFAULT, 0));
				}
				hostRequest.append(createDataExchange(annuityRiderEx.getValuationBaseSeries(), "PUAGRQBS", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
				hostRequest.append(createDataExchange(annuityRiderEx.getValuationSubSeries(), "PUAGRQSB", CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0));
				if (annuityRiderEx.hasCommissionPlanCode()) {
					hostRequest.append(createDataExchange(annuityRiderEx.getCommissionPlanCode(), "PUAGRQCI", CYBTRANS_NONE, CYBTBL_NONE, CT_USHORT, 0));
				} else {
					hostRequest.append(createDataExchange("0", "PUAGRQCI", CYBTRANS_NONE, CYBTBL_NONE, CT_USHORT, 0));
				}
			}
			hostRequest.append(createDataExchange(sitCode, "PUAGRQSC", "OLIEXT_LU_SITCODE", CYBTBL_UCT_BY_INDEX_TRANS, CT_CHAR, 0));
		}
	}
}
