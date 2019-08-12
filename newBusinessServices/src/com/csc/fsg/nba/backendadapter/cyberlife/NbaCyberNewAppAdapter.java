package com.csc.fsg.nba.backendadapter.cyberlife;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import com.csc.fs.sa.accel.interaction.services.AccelCyberLifeDXEDataTransformationIntf;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaAllowableBenefitsData;
import com.csc.fsg.nba.tableaccess.NbaPlansRidersData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.AccountHolderNameCC;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.AnnuityExtension;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Banking;
import com.csc.fsg.nba.vo.txlife.BankingExtension;
import com.csc.fsg.nba.vo.txlife.CWAActivity;
import com.csc.fsg.nba.vo.txlife.CWADestOrCreditAccountNumber;
import com.csc.fsg.nba.vo.txlife.CWASourceOrDebitAccountNumber;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.CovOptionExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.EMailAddress;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.FormsReceived;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.HoldingExtension;
import com.csc.fsg.nba.vo.txlife.Investment;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeExtension;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Participant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.Payout;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Phone;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RelationExtension;
import com.csc.fsg.nba.vo.txlife.RelationProducerExtension;
import com.csc.fsg.nba.vo.txlife.Rider;
import com.csc.fsg.nba.vo.txlife.SubAccount;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.TaxWithholding;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;
import com.tbf.xml.XmlValidationError;

/**
 * Class to parse new applications XML103 request and generate a dxe request to be sent to the host
 * <p>
 *  <b>Modifications:</b><br>
 *  <table border=0 cellspacing=5 cellpadding=5>
 *  <thead>
 *  <th align=left>Project</th>
 *  <th align=left>Release</th>
 *  <th align=left>Description</th>
 *  </thead>
 *  <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 *  <tr><td>SPR1036</td><td>Version 2</td><td>Added translation table for phone type and changed DXE for phone type 2.</td><tr>
 *  <tr><td>SPR1109</td><td>Version 2</td><td>Move of InitalPremAmt as an extension off the Policy Object.</td><tr>
 *  <tr><td>SPR1106</td><td>Version 2</td><td>Tax ID Type Needs to Default in App Entry Adapter.</td><tr>
 *  <tr><td>SPR1058</td><td>Version 2</td><td>The variable FULPLOPT (Death Benefit Option Type) is not generated for Variable.</td><tr>
 *  <tr><td>NBA009</td><td>Version 2</td><td>Cashiering Component</td><tr>
 *  <tr><td>SPR1018</td><td>Version 2</td><td>Java Code Clean Up</td><tr>
 *  <tr><td>SPR1132</td><td>Version 2</td><td>Add CovOption.LifeCovOptTypeCode translation for annuities in createAnnuity</td><tr>
 *  <tr><td>SPR1131</td><td>Version 2</td><td>The DXE FCLPIDNT must be included AFTER FCVPHASE but BEFORE FCVPIDNT</td><tr>
 *  <tr><td>NBA044</td><td>Version 3</td><td>Architecture Changes</td></tr>
 *  <tr><td>SPR1197</td><td>Version 3</td><td>Remove CalendarYearOverrideInd</td><tr>
 *  <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 *  <tr><td>SPR1079</td><td>Version 3</td><td>Special Frequency Changes</td></tr>
 *  <tr><td>SPR1778</td><td>Version 4</td><td>Correct problems with SmokerStatus and RateClass in Automated Underwriting, and Validation.</td></tr>
 *  <tr><td>SPR1943</td><td>Version 4</td><td>CLIF WRAPPERED Adapter Needs to Send FLCOCLAS for RateClassAppliedFor </td></tr>
 *  <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 *  <tr><td>SPR1999</td><td>Version 4</td><td>DXEs for Restriction and Special Handling are not carrying any values to CyberLife Admin System</td></tr>
 *  <tr><td>NBA111</td><td>Version 4</td><td>Joint Insured.</td></tr>
 *  <tr><td>SPR1986</td><td>Version 4</td><td>Remove CLIF Adapter Defaults for Pay up and Term date of Coverages and Covoptions.</td></tr>
 *  <tr><td>SPR1186</td><td>Version 4</td><td>Incorrect table used for Roles for Annuities</td></tr>
 * 	<tr><td>ACN005</td><td>Version 4</td><td>UW Aviation</td></tr>
 *  <tr><td>SPR1952</td><td>Version 5</td><td>If the cease date for a coverage is input, we should be setting the flag bit on the coverage segment.</td></tr>
 *  <tr><td>SPR1538</td><td>Version 5</td><td>When an Annuity application is being submitted using Application Entry view, the replacement type value (FNBRPINS) is not being sent to host.</td></tr>
 *  <tr><td>SPR2287</td><td>Version 5</td><td>Ownership details are not built in 89 segment of CyberLife Admin system</td></tr>
 *  <tr><td>SPR2229</td><td>Version 5</td><td>Add contract level benefit indicator to holding inquiry.</td></tr>
 *  <tr><td>SPR2631</td><td>Version 5</td><td> coverage termination date (FCVTCGDT)  is not being set by input CoverageCeaseDate.</td></tr>
 *  <tr><td>SPR1073</td><td>Version 5</td><td>WRAPPERED CLIF - DXE not being created for Credit Card Number, Name and Expiry Date</td></tr>
 *  <tr><td>SPR2657</td><td>Version 5</td><td>DXE FBCCTYPE carries the Olife value instead of the BES value.</td></tr>
 *  <tr><td>SPR2099</td><td>version 5</td><td>MEC processing is incorrect.</td></tr>  
 *  <tr><td>SPR3134</td><td>Version 6</td><td>RateClassAppliedFor Should Be Changed to Use NBA_RATECLASS Table in Adapter and in QA XML103 Data</td></tr>
 *  <tr><td>SPR2992</td><td>Version 6</td><td>General Code Clean Up Issues for Version 6</td></tr>
 *  <tr><td>SPR3164</td><td>Version 6</td><td>The NBREQRMNT workitem went to NBERROR with reason related to Primary Writing agent address details</td></tr>
 *  <tr><td>NBA151</td><td>Version 6</td><td>UL and VUL Application Entry Rewrite</td></tr>
 *  <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up</td></tr>
 *  <tr><td>NBA195</td><td>Version 7</td><td>JCA Adapter for DXE Interface to CyberLife</td></tr>
 *  <tr><td>SPR3408</td><td>Version 7</td><td>Benficiary DXE is not created and sent to host if BeneficiaryDesignation tag is missing from ACORD XML103</td></tr> 
 *  <tr><td>SPR3573</td><td>Version 8</td><td>Credit Card Information is not saved</td></tr>3
 *  <tr><td>SPR2151</td><td>Version 8</td><td>Correct the Contract Validation edits and Adaptor logic for EFT, PAC and Credit Card Billing</td></tr>
 *  </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
// NBA195 signature modified
public class NbaCyberNewAppAdapter extends NbaCyberRequests implements NbaCyberConstants, AccelCyberLifeDXEDataTransformationIntf {
	// Logger
	private static NbaLogger logger = null; //NBA044
	protected String hostRequest = null;
	public static final String TRANS_NEWAPP_UL = "6PCYWHATTODO=1000,2;";
	public static final String TRANS_NEWAPP_CWA = "WHATTODO=4000,1;";
	public static final String POL_AUTO = "AUTO";
	public static final String CYB_INSURED = "00";
	// SPR3290 code deleted
	// Class Variables
	private String planType = "";
	// Class Variables
	private String productCode = "";
	// DataExchange 
	private String addressDXE;
	private String writingAgentDXE;
	private String servicingAgentDXE;
	private String servicingAgencyDXE;
	private String beneficiaryDXE;
	private String benefitDXE;
	private String payoutDXE;
	private NbaPlansRidersData[] plansRidersData;
	// Move to Constants
	// begin SPR3290
	protected static String ASSIGNMENT_CODE = "FBRRESTA";
	protected static String AV_FLAGB_1 = "FAVFLGB1"; // bit 3
	protected static String CW_FLAGB_3 = "FCWFLGB3";
	protected static String CW_FLAGB_6 = "FCWFLGB6";
	protected static String RC_FLAGA_7 = "FRCFLGA7";
	protected static String LC_FLAGA_2 = "FLCFLGA2";
	protected static String CLIENT_CITIZENSHIP = "FLCALIEN";
	protected static String COVERAGE_ID_2 = "FCLPIDNT";
	// end SPR3290
	private boolean BENE_ORIG_ID_POPULATED = false;
	//LifeParticipantRoleType HashMap.Keeps count of lifeparticipant:role code combination.
	private HashMap lifeParticipantRoleTypeMap = null; //NBA111
	/**
	 * NbaCyberNewAppAdapter constructor comment.
	 */
	public NbaCyberNewAppAdapter() {
		super();
	}
	/**
	 * Set the value for planType from an OLifE
	 * @param olife
	 */
	// SPR1186 New Method
	protected void setPlanType(OLifE olife) {
		planType = "";
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife);
		Policy policy = holding.getPolicy();
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladh = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
		if (ladh != null && ladh.isAnnuity()) {
			planType = "F";
		}
	}
	/**
	 * Main external method used to create a CL 1000 transaction from a new app XML103
	 * 
	 * @param txlife new app XML input
	 * @return request dxe to be sent to the CyberLife host
	 */
	//SPR3573 changed method signature
	public String create103Request(NbaTXLife nbaTXLife) throws NbaBaseException {
		TXLife txLife = nbaTXLife.getTXLife();	//SPR3573 
		try {
			tblMap = new HashMap();
			partyMap = new HashMap();
			UserAuthRequestAndTXLifeRequest userRequest = txLife.getUserAuthRequestAndTXLifeRequest();
			TXLifeRequest txlifeRequest = userRequest.getTXLifeRequestAt(0);
			OLifE olife = txlifeRequest.getOLifE();
			Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); //NBA044
			Policy policy = holding.getPolicy();
			setPlanType(olife); //SPR1186
			plansRidersData = null;
			// SPR1186 code deleted
			// Initialize request string
			hostRequest = new String();
			// Initialize dxe holders
			writingAgentDXE = "";
			servicingAgentDXE = "";
			servicingAgencyDXE = "";
			benefitDXE = "";
			beneficiaryDXE = "";
			// Company Code
			compCode = policy.getCarrierCode(); // temporary until UCT is loaded
			// SPR3290 code deleted
			hostRequest = TRANS_NEWAPP_UL + COMP_CODE + "=" + compCode + ";" + POL_NUM + "=" + policy.getPolNumber() + ";";
			hostRequest = hostRequest + createParties(olife);
			hostRequest = hostRequest + createLife(holding);
			hostRequest = hostRequest + benefitDXE + beneficiaryDXE + writingAgentDXE + servicingAgentDXE + servicingAgencyDXE;
			hostRequest = hostRequest + createApplicationInfo(policy);
			hostRequest = hostRequest + createBillingInfo(nbaTXLife, holding); //NBA093 SPR3573
			if (holding.hasInvestment())
				hostRequest = hostRequest + createInvestment(holding);
			//NBA009 deleted. CWA update to host has been deleted. This will be done in Apply Money process 
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_PARSE, e);
		}
		return hostRequest;
	}
	protected String createAddressFromParty(Party party, String sPersonId) throws NbaBaseException {
		Address partyAddress = null;
		String addressDXE = "";
		// SPR3290 code deleted
		try {
			// Address Info
			for (int i = 0; i < party.getAddressCount(); i++) {
				partyAddress = party.getAddressAt(i);
				// Address ID
				addressDXE = addressDXE + formatDataExchange(sPersonId, ADDR_PERS_SEQ_ID, CT_CHAR, 10);
				addressDXE = addressDXE + formatDataExchange(String.valueOf(i + 1), FNAOCCUR, CT_CHAR, 10); //SPR1127 SPR2992
				// Address Effective Date
				if (partyAddress.hasStartDate())
					addressDXE = addressDXE + createDataExchange(partyAddress.getStartDate(), STARTDATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
				// Address Type
				if (partyAddress.hasAddressTypeCode())
					addressDXE =
						addressDXE
							+ createDataExchange(partyAddress.getAddressTypeCode(), ADDR_TYPE, NbaTableConstants.OLI_LU_ADTYPE, CYBTBL_UCT, CT_CHAR, 5);
				// Country
				if (partyAddress.hasAddressCountryTC()) //NBA093
					addressDXE =
						addressDXE
							+ createDataExchange(partyAddress.getAddressCountryTC(), COUNTRY, NbaTableConstants.OLI_LU_NATION, CYBTBL_UCT, CT_CHAR, 46);
				//NBA093
				// Addendum
				if (partyAddress.hasAttentionLine())
					addressDXE =
						addressDXE + createDataExchange(partyAddress.getAttentionLine(), ATTEN_LINE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 46);
				// Address Line 1
				if (partyAddress.hasLine1())
					addressDXE = addressDXE + createDataExchange(partyAddress.getLine1(), ADDR_LINE1, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 46);
				// Address Line 2
				if (partyAddress.hasLine2()) {
					if (partyAddress.hasLine3())
						addressDXE =
							addressDXE
								+ createDataExchange(
									partyAddress.getLine2() + " " + partyAddress.getLine3(),
									ADDR_LINE2,
									CYBTRANS_NONE,
									CYBTBL_NONE,
									CT_CHAR,
									46);
					else
						addressDXE = addressDXE + createDataExchange(partyAddress.getLine2(), ADDR_LINE2, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 46);
				}
				// City
				if (partyAddress.hasCity())
					addressDXE = addressDXE + createDataExchange(partyAddress.getCity(), CITY, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 31);
				// State
				// Typecode  OLI_LU_STATE
				if (partyAddress.hasAddressStateTC()) //NBA093
					addressDXE = addressDXE + createDataExchange(partyAddress.getAddressStateTC(), STATE, "NBA_STATES", CYBTBL_STATE, CT_CHAR, 3);
				//NBA093
				// Zip/Postal Code
				if (partyAddress.hasZip())
					addressDXE =
						addressDXE + createDataExchange(formatCyberZipCode(partyAddress.getZip()), ZIPCODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 12);
			}
			// End Address Info
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_ADDRESSES, e);
		}
		return addressDXE;
	}
	/**
	 * Parse Annuity Object of XML103 input
	 * 
	 * @param annuity input Annuity xml object
	 * @param policy input Policy xml object
	 * @return annuity dxe request stream
	 */
	protected String createAnnuity(Annuity annuity, Policy policy) throws NbaBaseException {
		String AnnuDXE = "";
		// SPR1132
		CovOption covoption = null;
		AnnuityExtension annuityEx = null;
		// SPR3290 code deleted
		Payout payout = null;
		// SPR3290 code deleted
		Participant participant = null;
		// SPR3290 code deleted
		OLifEExtension olifeEx = null;
		Rider rider = null;
		payoutDXE = "";
		try {
			if (annuity.getPayoutCount() > 0) {
				payout = annuity.getPayoutAt(0);
				if (payout.getParticipantCount() > 0) {
					participant = payout.getParticipantAt(0);
				}
				// SPR3290 code deleted
			}
			for (int i = 0; i < annuity.getOLifEExtensionCount(); i++) {
				olifeEx = annuity.getOLifEExtensionAt(i);
				if (olifeEx.isAnnuityExtension()) {
					annuityEx = olifeEx.getAnnuityExtension();
					break;
				}
			}
			// SPR3290 code deleted
			//  (Holding.Policy.Annuity) DXE: FCVPHASE
			AnnuDXE = AnnuDXE + createDataExchange(annuity.getAnnuityKey(), COV_KEY, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			// Type of relation the party has to the annuity. (Holding.Policy.Annuity.Payout) DXE: FCVPIDNT
			// Typecode UCT (CLPCTB11)  OLI_LU_PARTICROLE
			String relRoleCode = getCyberValue(participant.getParticipantRoleCode(), CYBTRANS_ROLES, CYBTBL_PARTIC_ROLE_F, compCode, DEFAULT_COVID);
			AnnuDXE =
				AnnuDXE
					+ createDataExchange(getPartyId(participant.getPartyID(), relRoleCode), COVERAGE_ID, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			// Policy / coverage / option plan code.  Assigned by the Carrier administration system -  OR as issued by carrier -  (Holding.Policy) DXE: FCVPDSKY
			// String UCT (PLN_MNE)  nbA Table = NBA_PLANS
			productCode = policy.getProductCode();
			if (validPlanRider(productCode))
				AnnuDXE = AnnuDXE + createDataExchange(policy.getProductCode(), COV_PROD_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 12);
			else
				AnnuDXE = AnnuDXE + createDataExchange("", COV_PROD_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 12);
			
			if (policy.hasReplacementType()) { //SPR1538
				AnnuDXE = AnnuDXE + createDataExchange(policy.getReplacementType(), APP_REPLACEMENT_CD, NbaTableConstants.OLI_LU_REPLACETYPE, CYBTBL_UCT, CT_CHAR, 1);//SPR1538
			} //SPR1538
			
			// Qualification Type (Holding.Policy.Annuity) DXE: FCVFQTYP
			// Typecode UCT (CLAPTB08)  OLI_LU_QUALPLAN
			if (annuity.hasQualPlanType())
				AnnuDXE =
					AnnuDXE
						+ createDataExchange(annuity.getQualPlanType(), ANNU_QUAL_TYPE, NbaTableConstants.OLI_LU_QUALPLAN, CYBTBL_UCT, CT_CHAR, 1);
			//begin SPR1986						
			if (annuity.hasRequestedMaturityDate()) {
				AnnuDXE = AnnuDXE + createDataExchange(RQST_MATURITY_DATE_OVERRIDE_E0_FLG, RQST_MATURITY_DATE_OVERRIDE_E0, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 8);
				AnnuDXE =
					AnnuDXE
						+ createDataExchange(
							formatCyberDate(annuity.getRequestedMaturityDate()),
							RQST_MATURITY_DATE,
							CYBTRANS_NONE,
							CYBTBL_NONE,
							CT_DEFAULT,
							0);
				AnnuDXE = AnnuDXE + createDataExchange(FLAG_BIT_ON, RQST_MATURITY_DATE_OVERRIDE_66, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 8);
			} else if (annuity.hasRequestedMaturityAge()) {
				AnnuDXE =
					AnnuDXE + createDataExchange(annuity.getRequestedMaturityAge(), RQST_MATURITY_AGE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 3);
			} else if (annuity.hasRequestedMaturityDur()) {
				AnnuDXE =
					AnnuDXE + createDataExchange(annuity.getRequestedMaturityDur(), RQST_MATURITY_DUR, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 3);
			}
			//end SPR1986
			// Annuity Settlement Option (Holding.Policy.Annuity.Payout) DXE: FCVFSOPT
			// Typecode UCT (CLAPTB50)  OLI_LU_INCOPTION
			if (payout != null)
				if (payout.hasIncomeOption())
					AnnuDXE =
						AnnuDXE
							+ createDataExchange(
								payout.getIncomeOption(),
								ANNU_PAYOUT_SETTLEMENT_OPT,
								NbaTableConstants.OLI_LU_INCOPTION,
								CYBTBL_UCT,
								CT_CHAR,
								3);
			//// Initial Premium (Holding.Policy.Annuity.InitPaymentAmt) DXE: FTDPINIT
			if (annuity.hasInitPaymentAmt()) //NBA1109
				AnnuDXE = AnnuDXE + createDataExchange(annuity.getInitPaymentAmt(), ANNU_INIT_PREM, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			//NBA1109
			// RMD Calculation Indicator (Initial or Recalculate) (Holding.Policy.Annuity) DXE: FDENRMDC
			// VT_I4 (Typecode) UCT (CLDET02B)  OLIEXT_LU_RMDCALCIND DXE: FDENRMDC
			if (annuityEx != null)
				if (annuityEx.hasRMDCalcInd())
					AnnuDXE =
						AnnuDXE
							+ createDataExchange(
								annuityEx.getRMDCalcInd(),
								ANNU_RMD_CALC_IND_INIT,
								NbaTableConstants.OLIEXT_LU_RMDCALCIND,
								CYBTBL_UCT,
								CT_CHAR,
								9);
			// Begin SPR1132
			//Deleted code NBA093
			// End SPR1132
			// RMD Calculation Indicator (Initial or Recalculate) (Holding.Policy.Annuity)
			// VT_I4 (Typecode) UCT (CLDET02B)  OLIEXT_LU_RMDCALCIND DXE: FDENRMDP
			//createDataExchange( annuity.getRMDCalcInd(), ANNU_RMD_CALC_IND_RECALC, NbaTableConstants.OLIEXT_LU_RMDCALCIND, CYBTBL_UCT, CT_CHAR, 9 );
			// Adjusted Investment (Holding.Policy.Annuity.Payout)  DXE: FSPATAIV
			if (payout != null) //NBA093
				if (payout.hasAdjInvestedAmt()) //NBA093
					payoutDXE =
						payoutDXE + createDataExchange(payout.getAdjInvestedAmt(), ANNU_PAYOUT_ADJ_INV, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
			//NBA093
			// Exclusion Ratio (Holding.Policy.Annuity.Payout) DXE: FSPATERT
			if (payout != null)
				if (payout.hasExclusionRatio())
					payoutDXE =
						payoutDXE + createDataExchange(payout.getExclusionRatio(), ANNU_PAYOUT_EXCL_RATIO, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
			// Certain Period (Holding.Policy.Annuity.Payout) DXE: FSPATCPR
			if (payout != null)
				if (payout.hasNumModalPayouts())
					payoutDXE =
						payoutDXE
							+ createDataExchange(payout.getNumModalPayouts(), ANNU_PAYOUT_CERTAIN_PER, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			// Exclusion Amount (Holding.Policy.Annuity.Payout) DXE: FSPATERA
			if (payout != null)
				if (payout.hasPayoutTaxableAmt())
					payoutDXE =
						payoutDXE + createDataExchange(payout.getPayoutTaxableAmt(), ANNU_PAYOUT_EXCL_AMT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
			// Primary Percent (Holding.Policy.Annuity.Payout) DXE: FSPATJP1
			if (payout != null)
				if (payout.hasPrimaryReductionPct())
					payoutDXE =
						payoutDXE
							+ createDataExchange(payout.getPrimaryReductionPct(), ANNU_PAYOUT_PRIMARY_PCT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
			// Secondary Percent (Holding.Policy.Annuity.Payout) DXE: FSPATJP2
			if (payout != null)
				if (payout.hasSecondaryReductionPct())
					payoutDXE =
						payoutDXE
							+ createDataExchange(payout.getSecondaryReductionPct(), ANNU_PAYOUT_SEC_PCT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
			// Adjusted Investment (Holding.Policy.Annuity.Payout) DXE: FSPATAIV
			if (payout != null) //NBA093
				if (payout.hasAdjInvestedAmt()) //NBA093
					payoutDXE =
						payoutDXE + createDataExchange(payout.getAdjInvestedAmt(), ANNU_PAYOUT_ADJ_INV, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
			//NBA093
			// Payout Start Date (Holding.Policy.Annuity.Payout) DXE: FSPATSDT
			if (payout != null)
				if (payout.hasStartDate())
					payoutDXE = payoutDXE + createDataExchange(payout.getStartDate(), ANNU_PAYOUT_STRT_DT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
			// Payout Start Date (Holding.Policy.Annuity.Payout) DXE: FSPATDAY
			if (payout != null)
				if (payout.hasStartDate())
					payoutDXE = payoutDXE + createDataExchange(payout.getStartDate(), ANNU_PAYOUT_STRT_DAY, CYBTRANS_NONE, CYBTBL_NONE, CT_USHORT, 0);
			for (int i = 0; i < annuity.getRiderCount(); i++) {
				rider = annuity.getRiderAt(i);
				Participant riderParticipant = null;
				// NBA093 deleted 4 lines
				if (rider.getParticipantCount() > 0) { //NBA093
					riderParticipant = rider.getParticipantAt(0); //NBA093
				}
				// NBA093 deleted 2 lines
				// Phase Code (Holding.Policy.Annuity) DXE: FCVPHASE
				AnnuDXE = AnnuDXE + createDataExchange(rider.getRiderKey(), COV_KEY, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				// Type of relation the party has to the annuity. (Holding.Policy.Annuity.Payout) DXE: FCVPIDNT
				// Typecode UCT (CLPCTB11)  OLI_LU_PARTICROLE
				if (riderParticipant != null) {
					relRoleCode =
						getCyberValue(riderParticipant.getParticipantRoleCode(), CYBTRANS_ROLES, CYBTBL_PARTIC_ROLE, compCode, DEFAULT_COVID);
					//Begin 1131
					//This DXE creates a link between the party and the coverage within CyberLife
					AnnuDXE =
						AnnuDXE
							+ createDataExchange(
								getPartyId(riderParticipant.getPartyID(), relRoleCode),
								ANNU_RID_KEY,
								CYBTRANS_NONE,
								CYBTBL_NONE,
								CT_DEFAULT,
								0);
					//End 1131
					AnnuDXE =
						AnnuDXE
							+ createDataExchange(
								getPartyId(riderParticipant.getPartyID(), relRoleCode),
								COVERAGE_ID,
								CYBTRANS_NONE,
								CYBTBL_NONE,
								CT_DEFAULT,
								0);
				}
				// Administration provided code to identify rider (Holding.Policy.Annuity) DXE: FCVPDSKY
				if (rider.hasRiderCode()) {
					if (validPlanRider(rider.getRiderCode()))
						AnnuDXE = AnnuDXE + createDataExchange(rider.getRiderCode(), COV_PROD_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					else
						AnnuDXE = AnnuDXE + createDataExchange("", COV_PROD_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				}
				// Total or face amount of rider, or total options elected, or lifetime amount of benefit. (Holding.Policy.Annuity)  DXE: FCVFACE
				if (rider.hasTotAmt())
					AnnuDXE = AnnuDXE + createDataExchange(rider.getTotAmt(), COV_CURR_AMT_1, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				// Rider number of Units (Holding.Policy.Annuity.Rider) DXE: FCVUNITS
				if (rider.hasNumberOfUnits()) //NBA093
					AnnuDXE = AnnuDXE + createDataExchange(rider.getNumberOfUnits(), COV_CURR_AMT_2, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				// Begin 1132
				// NBA093 deleted line
				for (int m = 0; m < rider.getCovOptionCount(); m++) { // NBA093
					covoption = rider.getCovOptionAt(m); // NBA093
					// Coverage Option Key (Holding.Policy.Life.Coverage.CovOption) DXE: FSBBPHS
					AnnuDXE =
						AnnuDXE + createDataExchange(covoption.getCovOptionKey(), COV_OPT_BENE_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					// Type of benefit (Holding.Policy.Annuiity.AnnuityExtension.CovOption) DXE: FSBBENEF
					// Typecode UCT (CLUDT135)  OLI_LU_OPTTYPE
					AnnuDXE =
						AnnuDXE
							+ createDataExchange(
								covoption.getLifeCovOptTypeCode(),
								COV_OPT_TYPE,
								NbaTableConstants.OLI_LU_OPTTYPE,
								CYBTBL_UCT,
								CT_CHAR,
								3);
				}
				// NBA093 deleted line
			}
			AnnuDXE = AnnuDXE + payoutDXE;
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_ANNUITY, e);
		}
		return AnnuDXE;
	}
	/**
	 * Parse the ApplicationInfo object of the input XML103
	 * 
	 * @param policy input Policy xml object
	 * @return corresponding request dxe
	 */
	protected String createApplicationInfo(Policy policy) {
		OLifEExtension olife;
		ApplicationInfo applicationinfo;
		ApplicationInfoExtension applicationinfoEx;
		String appinfoDXE = "";
		FormsReceived formsReceived;
		if (policy.hasApplicationInfo())
			applicationinfo = policy.getApplicationInfo();
		else
			return "";
		// Contact Name (Holding.Policy.ApplicationInfo)
		if (applicationinfo.hasNBContactName())
			appinfoDXE =
				appinfoDXE + createDataExchange(applicationinfo.getNBContactName(), APP_DEPT_DESK_CLERK_ID, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
		// App written State (Holding.Policy.ApplicationInfo) DXE: FNBSTWCD
		//  UCT (ST_CTL)  OLI_LU_STATE
		if (applicationinfo.hasApplicationJurisdiction())
			appinfoDXE =
				appinfoDXE
					+ createDataExchange(applicationinfo.getApplicationJurisdiction(), APP_WRITTEN_STATE, "NBA_STATES", CYBTBL_STATE_TC, CT_CHAR, 3);
		// Folder Location (Holding.Policy.ApplicationInfo)
		if (applicationinfo.hasCaseOrgCode())
			appinfoDXE = appinfoDXE + createDataExchange(applicationinfo.getCaseOrgCode(), APP_FLDR_LOC, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 6);
		// Number assigned to application form type. (Holding.Policy.ApplicationInfo)
		// FCV
		//appinfoDXE = appinfoDXE + createDataExchange(applicationinfo.getHOAppFormNumber(), APP_FORM_TYPE_NUM_ASSN, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 10);
		// Application Number (Holding.Policy.ApplicationInfo)
		if (applicationinfo.hasHOAssignedAppNumber())
			appinfoDXE =
				appinfoDXE + createDataExchange(applicationinfo.getHOAssignedAppNumber(), APP_ASSN_NUM, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 11);
		// Underwriter ID (Holding.Policy.ApplicationInfo)
		if (applicationinfo.hasHOUnderwriterName())
			appinfoDXE =
				appinfoDXE + createDataExchange(applicationinfo.getHOUnderwriterName(), APP_UNDERWRITER_ID, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
		// Last Accounting Date (Holding.Policy.ApplicationInfo)
		// LastAccounting Data is not in AppInfo
		//appinfoDXE = appinfoDXE + createDataExchange(formatCyberDate(applicationinfoEx.getLastAccountingDate()), APP_LST_ACCT_DT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
		// Last Ann. Processed (Holding.Policy.ApplicationInfo)
		//appinfoDXE = appinfoDXE + createDataExchange(formatCyberDate(applicationinfo.getLastAnnivDate()), A_LAST_ANNIV_PROC, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
		for (int j = 0; j < applicationinfo.getOLifEExtensionCount(); j++) {
			olife = applicationinfo.getOLifEExtensionAt(j);
			if (olife.isApplicationInfoExtension())
				applicationinfoEx = olife.getApplicationInfoExtension();
			else
				continue;
			// Indicates whether or not the MIB Authorization has already been received. (Holding.Policy.ApplicationInfo)
			// Boolean UCT ( )
			// Host boolean flag issue - removed from spreadsheet
			//if (applicationinfoEx.hasMIBAuthorization())
			//	appinfoDXE = appinfoDXE + createDataExchange(applicationinfoEx.getMIBAuthorization(), RC_FLAGA_7, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 12);
			// Replacement Code (Holding.Policy.ApplicationInfo)
			// VT_I4 UCT (CLPCTB07)  OLI_LU_REPLACETYPE
			if (policy.hasReplacementType()) //NBA093
				appinfoDXE =
					appinfoDXE
						+ createDataExchange(
							policy.getReplacementType(),
							APP_REPLACEMENT_CD,
							NbaTableConstants.OLI_LU_REPLACETYPE,
							CYBTBL_UCT,
							CT_CHAR,
							1);
			//NBA093
			// Requested Issue Date (Holding.Policy.ApplicationInfo)
			if (applicationinfo.hasRequestedPolDate()) //NBA093
				appinfoDXE =
					appinfoDXE + createDataExchange(applicationinfo.getRequestedPolDate(), APP_REQ_ISS_DT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
			//NBA093
			for (int i = 0; i < applicationinfoEx.getFormsReceivedCount(); i++) {
				formsReceived = applicationinfoEx.getFormsReceivedAt(i);
				// Type of Form Received (Holding.Policy.ApplicationInfo)
				// Typecode UCT (CLPCTB34)  OLIEXT_LU_FORMSRECTYPE
				if (formsReceived.hasFormsRecType())
					appinfoDXE =
						appinfoDXE
							+ createDataExchange(
								formsReceived.getFormsRecType(),
								APP_FORM_TYPE_RECV,
								NbaTableConstants.OLIEXT_LU_FORMSRECTYPE,
								CYBTBL_UCT,
								CT_DEFAULT,
								0);
			}
		}
		return appinfoDXE;
	}
	/**
	 * Creates the Beneficiary dxe from party and relation input
	 * 
	 * @param party input Party object
	 * @param relation input Relation object
	 * @param roleTypeMap role type hashmap
	 * @return dxe stream containing dxe info
	 */
	protected String createBeneficiary(Party party, Relation relation, HashMap roleTypeMap) throws NbaBaseException {
		OLifEExtension olifeEx;
		// SPR3290 code deleted
		String sPersonId;
		// SPR3290 code deleted
		RelationExtension relationEx;
		String beneDXE = "";
		try {
			String relRoleCode =
				getCyberValue(Long.toString(relation.getRelationRoleCode()), CYBTRANS_ROLES, CYBTBL_RELATION_ROLE, compCode, DEFAULT_COVID);
			sPersonId = getPartyId(party.getId(), relRoleCode);
			// Handle Beneficiary Information if rolecode is 34, 35 or 55
			if (relation.getRelationRoleCode() == OLI_REL_BENEFICIARY
				|| relation.getRelationRoleCode() == OLI_REL_CONTGNTBENE
				|| relation.getRelationRoleCode() == OLI_REL_ASSIGNBENE) { //NBA093
				for (int i = 0; i < relation.getOLifEExtensionCount(); i++) {
					olifeEx = relation.getOLifEExtensionAt(i);
					if (olifeEx.isRelationExtension()) {
						//begin SPR3408
                        relationEx = olifeEx.getRelationExtension();
                        // An identifier that the 'to object' uses to identify the 'from object'. (Relation)
                        //beneDXE = beneDXE + createDataExchange(relation.getRelatedRefID(), PERS_SEQ_ID, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
                        beneDXE = beneDXE + formatDataExchange(sPersonId, BENE_SUB_ID, CT_CHAR, 12);
                        // Beneficiary Type (Prim., Contingent, Assignee) (Relation) DXE: FBDBTYPE
                        //  UCT (CLPCTB17) OLI_LU_PARTICROLE nbA table = NBA_Beneficiary_Type
                        beneDXE = beneDXE
                                + createDataExchange(relation.getRelationRoleCode(), BENEF_TYPE, NbaTableConstants.NBA_BENEFICIARY_TYPE, CYBTBL_UCT,
                                        CT_CHAR, 1);
                        // If the following fields required to build the 96 segment are not present, DO NOT send this information to the
                        // host:FBDBDIST, FBDBNPCT, and FBDBNAMT, - Description code to further define the role of the relationship I.e., the
                        // relationrolecode would contain a value of spouse the relation description would contain a value for husband or wife.
                        // (Relation)
                        // DXE: FBDBNREL
                        // VT_I4 UCT (CLUDT108) OLI_LU_BENEDESIGNATION
                        if (relation.hasBeneficiaryDesignation()) {
                            beneDXE = beneDXE
                                    + createDataExchange(relation.getBeneficiaryDesignation(), BENEF_REL_ROLE,
                                            NbaTableConstants.OLI_LU_BENEDESIGNATION, CYBTBL_UCT, CT_CHAR, 1);
                        }
                        if (relationEx.hasBeneficiaryDistributionOption()) {
                            beneDXE = createBeneficiaryDistribution(relation, relationEx, beneDXE); //NBA151
                        }
                        // Joint Beneficiary Indicator (Relation) DXE: FLCFLGA2
                        // VT_I4 (Typecode)  OLIEXT_LU_BOOLEAN Current Extension
                        if (relationEx.hasJointBenInd())
                            beneDXE = beneDXE + createDataExchange(relationEx.getJointBenInd(), LC_FLAGA_2, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 8);
                    }
                    //end SPR3408
				}
			}
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_PARTIES, e);
		}
		return beneDXE;
	}
	/**
	 * create the beneficiary DXE distrubution amount and type. 
	 * If an interest amount has been set, send the dist type to flat
	 * If an interest percent has been set to 100% send only the type as full
	 * If the interest percent is greater than 0 sent the type as unequal.
	 * If no amount or percent has been set, check to see if the disbution should
	 * be equally divided and set the type accordingly.
     * @param relation
     * @param relationEx
     * @param beneDXE
     * @return
     */
	//NBA151 New Method
    protected String createBeneficiaryDistribution(Relation relation, RelationExtension relationEx, String beneDXE) {
        double interestPercent = 100.0;
        long distributionType = -1;
        // Amount of distribution for beneficiary (Relation) DXE: FBDBNAMT
        if (relationEx.hasInterestAmount()) {
            beneDXE = beneDXE + createDataExchange(relationEx.getInterestAmount(), BENE_INTEREST_AMT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
            distributionType = OLI_DISTOPTION_FLATAMT;
            // If the interest percent for a beneficiary equals 100% the Beneficiary Distribution Option should be set to Balance and the Interest
            // Percent amount should not be sent to the host. - Percentage of distribution for beneficiary (Relation) DXE: FBDBNPCT
        } else if (relation.hasInterestPercent()) {
            interestPercent = relation.getInterestPercent();
            if (interestPercent == 100) {
                distributionType = OLI_DISTOPTION_BALANCE;
            } else if (interestPercent > 0) {
                beneDXE = beneDXE + createDataExchange(relation.getInterestPercent(), PCTG_DIST_FOR_BENEF, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
                distributionType = OLI_DISTOPTION_PERCENT;
            }
        } else if (relationEx.getBeneficiaryDistributionOption() == OLI_DISTOPTION_EQUAL) {
            distributionType = relationEx.getBeneficiaryDistributionOption();
        }
        // Distribution type (Relation) DXE: FBDBDIST
        //  UCT (CLUDT107) OLI_LU_DISTOPTION
        if (distributionType != -1)
            beneDXE = beneDXE + createDataExchange(distributionType, DIST_TYPE, NbaTableConstants.OLI_LU_DISTOPTION, CYBTBL_UCT, CT_CHAR, 1);
        return beneDXE;
    }
    /**
     * Create request dxe for BillingInfo input xml
     * 
     * @param holding
     *            input Holding xml
     * @return request dxe from input xml
     */
    //SPR3573 changed method signature
	protected String createBillingInfo(NbaTXLife nbaTXLife, Holding holding) { //NBA093
		//SPR2151 code deleted
		HoldingExtension holdingEx = null; //NBA093
		Policy policy = null; //NBA093
		ApplicationInfo applicationinfo = null;
		ApplicationInfoExtension applicationinfoEx = null;
		Life life = nbaTXLife.getLife();	//SPR2151
		LifeExtension lifeEx = null;
		//SPR2151 code deleted
		PolicyExtension policyEx = null;
		//NBA093 Deleted Code
		String billingDXE = "";
		//begin NBA093
		if (holding != null) {
			policy = holding.getPolicy();
			holdingEx = NbaUtils.getFirstHoldingExtension(holding);		//SPR2151
		}
		//end NBA093
		if (life != null) { //SPR2151	
			lifeEx = NbaUtils.getFirstLifeExtension(life);	//SPR2151
		}
		//NBA093 Deleted Code
		//SPR2151 code deleted
		if (policy != null && policy.hasApplicationInfo()) {		//SPR2151
			applicationinfo = policy.getApplicationInfo();
			applicationinfoEx = NbaUtils.getFirstApplicationInfoExtension(applicationinfo);	//SPR2151
		}
		//begin SPR1079
		if (policy.getOLifEExtensionCount() > 0) {
			policyEx = NbaUtils.getFirstPolicyExtension(policy);
		}
		//end SPR1079 	
		if (policy != null) {
			// Last Billing Date (Holding.Policy) DXE: FBRBILDT
			if (policy.hasLastNoticeDate())
				billingDXE = billingDXE + createDataExchange(policy.getLastNoticeDate(), LST_BILL_DT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
			// Last Billing Kind (Holding.Policy) DXE: FBRBILKD
			//  UCT (CLPCTB15)  OLI_LU_NOTICETYPE
			if (policy.hasLastNoticeType())
				billingDXE = billingDXE + createDataExchange(policy.getLastNoticeType(), LST_BILL_KIND, "OLI_LU_NOTICETYPE", CYBTBL_UCT, CT_CHAR, 1);
			// Billing Mode  (Holding.Policy) DXE: FBRBMODE
			// Typecode UCT (CLDET0L2)  OLI_LU_PAYMODE
			//begin SPR1079
			if (policy.hasPaymentMode()) {
				billingDXE = billingDXE + createPaymentModeDXE(policy, policyEx);
			}
			//end SPR1079	
			// Only applicable if Product Type = 'U' - Mode Premium Amount (Holding.Policy) DXE: FBRMPREM, DXE Master(s): FBRBMODE
			if (policy.hasPaymentAmt())
				billingDXE = billingDXE + createDataExchange(policy.getPaymentAmt(), PAYMENT_AMT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
			// Billing Form (Holding.Policy) DXE: FBRFORM, DXE Master(s): FBRBMODE, FBRMPREM
			// Typecode UCT (CLUDT831)  OLI_LU_PAYMETHOD
			if (policy.hasPaymentMethod())
				billingDXE = billingDXE + createDataExchange(policy.getPaymentMethod(), PAYMENT_METH, "OLI_LU_PAYMETHOD", CYBTBL_UCT, CT_DEFAULT, 0);
		}
		// 3 lines deleted SPR1079
		if (policyEx != null) { //NBA093
			// Timing for Notice (Holding.Policy) DXE: FBRDUEUS
			// Typecode  OLIEXT_LU_TIMING
			if (policyEx.hasTiming()) //NBA093
				billingDXE = billingDXE + createDataExchange(policyEx.getTiming(), TIMING_FOR_NOTICE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);
			//NBA093
		}
		// First Notice Extract Day (Holding.Policy) DXE: FBRDUEDY
		if (policy != null && policy.hasPaymentDraftDay())	//SPR2151
			billingDXE = billingDXE + createDataExchange(policy.getPaymentDraftDay(), FRST_NOTICE_EXTR_DAY, CYBTRANS_NONE, CYBTBL_NONE, CT_USHORT, 0);
		// Handling (Holding.Policy) DXE: FBRHANDL
		//  UCT (CLDET019)  OLI_LU_SPECIALHANDLING
		//NBA093 Deleted Code
		if (policy != null && policy.hasSpecialHandling())	//SPR2151
			billingDXE =
				billingDXE
					+ createDataExchange(policy.getSpecialHandling(), HANDLING, NbaTableConstants.OLI_LU_SPECIALHANDLING, CYBTBL_UCT, CT_CHAR, 1);
		//SPR1999
		if (policyEx != null) { //NBA093
			if (policyEx.hasInitialAnnualPremiumAmt()) { //NBA093
				billingDXE = billingDXE + createDataExchange(TARGET_INAP_SURR, TARGET_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0); //NBA104
				// Initial Net Annual Premium (Holding.Policy) DXE: FTPAMT
				billingDXE =
					billingDXE 
						+ createDataExchange(policyEx.getInitialAnnualPremiumAmt(), TARGET_AMOUNT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);				//NBA104
				//NBA093
			}
		}
		// Application Resident Area (Party) DXE: FBRAREA
		//billingDXE = billingDXE + createDataExchange(party.getResidenceCounty(), APP_RESIDENT_AREA, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 6);
		if (policy != null) {
			// State (jurisdiction) policy will be issued.. (Holding.Policy) DXE: FBRISTAT
			//  UCT (ST_CTL)  OLI_LU_STATE
			if (policy.hasJurisdiction())
				billingDXE = billingDXE + createDataExchange(policy.getJurisdiction(), JURISDICTION, "NBA_STATES", CYBTBL_STATE_TC, CT_CHAR, 3);
			// Billed To Date (Holding.Policy) DXE: FBRBILTD
			if (policy.hasBilledToDate())
				billingDXE = billingDXE + createDataExchange(policy.getBilledToDate(), BILLED_TO_DT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
		}
		if (lifeEx != null) {
			// Only applicable if Product Type = 'I', 'N' or 'O' - Secondary Div. Option (Holding.Policy.Life) DXE: FBRPROSD
			// Typecode UCT (CLDET020)  OLI_LU_DIVTYPE
			if (lifeEx.hasSecondaryDividendType())
				billingDXE =
					billingDXE + createDataExchange(lifeEx.getSecondaryDividendType(), SEC_DIV_OPT, "OLI_LU_DIVTYPE", CYBTBL_UCT, CT_CHAR, 1);
		}
		// Pension Code (Holding) DXE: FBRPENSN
		// VT_I4 UCT (CEUDT113)  OLI_LU_QUALIFIED
		if (holding != null) { //NBA093
			if (holding.hasQualifiedCode()) //NBA093
				billingDXE = billingDXE + createDataExchange(holding.getQualifiedCode(), APP_PENSION_CD, NbaTableConstants.OLI_LU_QUALIFIED,
					//NBA093
	CYBTBL_UCT, CT_CHAR, 1);
			//NBA093
		}
		// NBA093 deleted line
		if (holdingEx != null) { //NBA093
			// Last Accounting Date (Holding) DXE: FBRPAYDT
			if (holdingEx.hasLastAccountingDate()) //NBA093
				billingDXE =
					billingDXE + createDataExchange(holdingEx.getLastAccountingDate(), APP_LST_ACCT_DT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
			//NBA093
		}
		if (holding != null) { //NBA093
			// Last Ann. Processed (Holding) DXE: FBRLSTDF
			if (holding.hasLastAnniversaryDate()) //NBA093
				billingDXE =
					billingDXE + createDataExchange(holding.getLastAnniversaryDate(), A_LAST_ANNIV_PROC, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
			//NBA093
		}
		// Date app was signed. (Holding.Policy.ApplicationInfo) DXE: FBRAPPDT
		if (billingDXE != null) {
			if (applicationinfo.hasSignedDate()) {
				billingDXE = billingDXE + createDataExchange(applicationinfo.getSignedDate(), SIGN_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
			}
		}
		// begin NBA093
		if (holding != null) {
			// Assignment Code (Holding) DXE: FBRRESTA
			// VT_I4 UCT (CLDET015)  OLI_LU_ASSIGNED
			if (holding.hasAssignmentCode())
				billingDXE =
					billingDXE
						+ createDataExchange(holding.getAssignmentCode(), ASSIGNMENT_CODE, NbaTableConstants.OLI_LU_ASSIGNED, CYBTBL_UCT, CT_CHAR, 1);
			// Restriction Code (Holding) DXE: FBRRESTR
			// VT_I4 UCT (CLDET016)  OLI_LU_RESTRICT
			if (holding.hasRestrictionCode())
				billingDXE = billingDXE + createDataExchange(holding.getRestrictionCode(), APP_RESTRICT_CD, NbaTableConstants.OLI_LU_RESTRICT,
					//SPR1999
	CYBTBL_UCT, CT_CHAR, 1);
		}
		// end NBA093
		if (applicationinfoEx != null) {
			// Indicates whether or not the MIB Authorization has already been received. (Holding.Policy.ApplicationInfo) DXE: FRCFLGA7
			if (applicationinfoEx.hasMIBAuthorization())
				billingDXE =
					billingDXE + createDataExchange(applicationinfoEx.getMIBAuthorization(), RC_FLAGA_7, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);
		}
		billingDXE = billingDXE + createBillingControl(nbaTXLife, policy);	//SPR2151
		return billingDXE;
	}
	/**
	 * Create request DXE from Coverage input object
	 * 
	 * @param coverage input Coverage object
	 * @param policy input Policy object
	 * @param life input Life object
	 * @return Request DXE based on input XML
	 * @throws NbaBaseException
	 */
	protected String createCoverage(Coverage coverage, Policy policy, Life life) throws NbaBaseException {
		String covDXE = "";
		CoverageExtension coverageEx = null;
		LifeParticipant lifeParticipant = null;
		try {
			String phase = coverage.getCoverageKey().trim();
			Integer iPhase = new Integer(phase);
			coverageEx = null;
			for (int i = 0; i < coverage.getOLifEExtensionCount(); i++) {
				OLifEExtension olife = coverage.getOLifEExtensionAt(i);
				if (olife.isCoverageExtension()) {
					coverageEx = olife.getCoverageExtension();
					break;
				}
			}
			if (coverage.getLifeParticipantCount() > 0) {
				lifeParticipant = coverage.getLifeParticipantAt(0);
			}
			// Coverage Key (Holding.Policy.Life.Coverage) DXE: FCVPHASE
			covDXE = covDXE + createDataExchange(zeroPadString(coverage.getCoverageKey(), 2), ANNU_KEY, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			// Role of participant in coverage. (Holding.Policy.Life.Coverage.LifeParticipant) DXE: FCVPIDNT
			// Typecode UCT (CLPCTB11)  OLI_LU_PARTICROLE
			if (lifeParticipant != null) {
				String relRoleCode =
					getCyberValue(lifeParticipant.getLifeParticipantRoleCode(), CYBTRANS_ROLES, CYBTBL_PARTIC_ROLE, compCode, DEFAULT_COVID);
				String sPartyId = getPartyId(lifeParticipant.getPartyID(), relRoleCode);
				if (sPartyId.compareTo("001") != 0)
					covDXE = covDXE + createDataExchange(sPartyId, COVERAGE_ID_2, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				covDXE = covDXE + createDataExchange(sPartyId, COVERAGE_ID, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			}
			if (policy.hasApplicationInfo())
				if (policy.getApplicationInfo().hasHOAppFormNumber())
					// Number assigned to application form type. (Holding.Policy.ApplicationInfo) DXE: FCVPLFNO
					covDXE =
						covDXE
							+ createDataExchange(
								policy.getApplicationInfo().getHOAppFormNumber(),
								APP_FORM_TYPE_NUM_ASSN,
								CYBTRANS_NONE,
								CYBTBL_NONE,
								CT_CHAR,
								10);
			// Coverage number of Units (Holding.Policy.Life.Coverage) DXE: FCVUNITS
			if (coverage != null) { //NBA093
				covDXE = covDXE + createDataExchange(coverage.getCurrentNumberOfUnits(), COV_CURR_AMT_2, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				//NBA093
			}
			// Coverage Face Amount (Holding.Policy.Life.Coverage) DXE: FCVFACE
			if (coverage.getCurrentAmt() > 0) {
				covDXE = covDXE + createDataExchange(coverage.getCurrentAmt(), COV_CURR_AMT_1, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
			} else { // Coverage number of Units (Holding.Policy.Life.Coverage) DXE: FCVUNITS
				if (coverage != null && coverage.getCurrentNumberOfUnits() > 0) { //NBA093
					covDXE =
						covDXE + createDataExchange(coverage.getCurrentNumberOfUnits(), COV_CURR_AMT_2, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
					//NBA093
				}
			}
			// For coverage phase 1.  Only applicable if Product Type = 'U' - Death Benefit Option (Holding.Policy.Life.Coverage) DXE: FULPLOPT
			// Typecode UCT (CLPCT214)  OLI_LU_DTHBENETYPE
			if (iPhase.intValue() == 1 && (policy.getProductType() == VAR_UNIV_LIFE || policy.getProductType() == UNIV_LIFE)) //SPR1058 //SPR1018
				covDXE =
					covDXE
						+ createDataExchange(
							coverage.getDeathBenefitOptType(),
							DEATH_BENEFIT_OPT,
							NbaTableConstants.OLI_LU_DTHBENETYPE,
							CYBTBL_UCT,
							CT_CHAR,
							1);
			// Form Number (Holding.Policy.Life.Coverage) DXE: FCVPLFNO
			if (coverage != null) { //NBA093
				covDXE = covDXE + createDataExchange(coverage.getFormNo(), APP_FORM_TYPE_NUM_ASSN, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 10); //NBA093
			}
			// FCVFLAGA bit 3  ISS045 Exempt from Guideline XXX (Reentry) - Guideline xxx Exempt Indicator (Holding.Policy.Life.Coverage) DXE: REENTRY
			//covDXE = covDXE + createDataExchange( coverageEx.getGuidelineExemptInd(), GUIDELINE_XXX_EXEMPT_INDICATOR, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1 ); 
			// The markup uses this to set the phase code FCVPHASE to 01 for the base plan.  This indicator is not sent to the host as part of the submit. - Coverage classication - e.g. base, rider, etc. (Holding.Policy.Life.Coverage) DXE: N/A
			// Typecode  OLI_LU_COVINDCODE
			//covDXE = covDXE + createDataExchange( coverage.getIndicatorCode(), PREMIUM_TYPE_OF_ANNUITY.__PREMIUM_FOR_ANNUITY_AS_A_SINGLE,_FIXED,_OR_FLEXIBLE., CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0 ); 
			// Coverage Product Code (Holding.Policy.Life.Coverage) DXE: FCVPDSKY
			// Character UCT (PLN_NME)  nbA Table = NBA_PLANS
			if (validPlanRider(coverage.getProductCode()))
				covDXE = covDXE + createDataExchange(coverage.getProductCode(), COV_PROD_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 12);
			else
				covDXE = covDXE + createDataExchange("", COV_PROD_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 12);
			// save product code for processing investments
			if (iPhase.intValue() == 1) {
				//productCode = getCyberValue(coverage.getProductCode(), CYBTRANS_PLAN, CYBTBL_PLAN_COV_KEY, compCode, DEFAULT_COVID);
				productCode = coverage.getProductCode();
			}
			if (coverageEx != null) {
				// Unisex Override Indicator (Holding.Policy.Life.Coverage) DXE: FCVUOIND
				// VT_I4 (Typecode)  OLIEXT_LU_BOOLEAN Current Extension
				covDXE = covDXE + createDataExchange(coverageEx.getUnisexOverride(), UNISEX_OVERRIDE_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);
				// Unisex Override Code (Holding.Policy.Life.Coverage) DXE: FCVUSEXC
				// VT_I4 (Typecode) UCT (CLPCT209)  OLIEXT_LU_UNISEXCODE
				covDXE =
					covDXE
						+ createDataExchange(
							coverageEx.getUnisexCode(),
							UNISEX_OVERRIDE_CD,
							NbaTableConstants.OLIEXT_LU_UNISEXCODE,
							CYBTBL_UCT,
							CT_CHAR,
							1);
				// Unisex Override Subseries (Holding.Policy.Life.Coverage) DXE: FCVUSUBC
				// VT_I4 (Typecode) UCT (CLPCTB85)  OLIEXT_LU_UNISEXSUBSER
				covDXE =
					covDXE
						+ createDataExchange(
							coverageEx.getUnisexSubseries(),
							UNISEX_OVERRIDE_SUBSERIES,
							NbaTableConstants.OLIEXT_LU_UNISEXSUB,
							CYBTBL_UCT,
							CT_CHAR,
							3);
				if (coverageEx.hasCoverageCeaseDate()) { //SPR1952	
					covDXE = covDXE + createDataExchange(FLAG_BIT_ON, CEASE_DATE_FLAG, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1); //SPR1952
					//begin SPR2631
					covDXE =
						covDXE
							+ createDataExchange(
								formatCyberDate(coverageEx.getCoverageCeaseDate()),
								COV_TERM_DATE,
								CYBTRANS_NONE,
								CYBTBL_NONE,
								CT_DEFAULT,
								0);
					//end SPR2631
				} //SPR1952
			}
			// Only applicable if Product Type Code = 'I', 'N' or 'O' - Primary Dividend Option (Holding.Policy.Life) DXE: FCVRODIV 
			// Typecode UCT (CEUDT123)  OLI_LU_DIVTYPE
			if (life.hasDivType())
				covDXE = covDXE + createDataExchange(life.getDivType(), PRIM_DIV_OPT, NbaTableConstants.OLI_LU_DIVTYPE, CYBTBL_UCT, CT_CHAR, 1);
			// 1035 Exchange Indicator (Holding.Policy.Life.LifeUSA) DXE: FCV1035X
			if (life.hasLifeUSA())
				if (life.getLifeUSA().hasInternal1035())
					covDXE =
						covDXE + createDataExchange(life.getLifeUSA().getInternal1035(), POL_1035_EXCH_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);
			if (life.hasInitialPremAmt()) //NBA1109
				covDXE = covDXE + createDataExchange(life.getInitialPremAmt(), ANNU_INIT_PREM, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0); //NBA1109
			//SPR1078-Deleted the code that sent product type
			// NFO Option (Top Level) DXE: FCVRONFO
			// Typecode UCT (CLDET014)  OLI_LU_NONFORTPROV
			if (life.hasNonFortProv())
				covDXE =
					covDXE + createDataExchange(life.getNonFortProv(), NFO_OPTION_B, NbaTableConstants.OLI_LU_NONFORTPROV, CYBTBL_UCT, CT_DEFAULT, 0);
			for (int i = 0; i < coverage.getCovOptionCount(); i++) {
				CovOption covOption = coverage.getCovOptionAt(i);
				benefitDXE = benefitDXE + createCovOption(covOption, coverage);
			}
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_COVERAGES, e);
		}
		return covDXE;
	}
	/**
	 * Create Request DXE from CovOption input XML
	 * 
	 * @param covoption input CovOption xml object
	 * @param coverage Coverage object this covoption is tied to
	 * @param Life The Life object 
	 * @return Request DXE based on input XML
	 * @throws NbaBaseException
	 */
	protected String createCovOption(CovOption covoption, Coverage coverage) throws NbaBaseException {
		String covOptDXE = "";
		CovOptionExtension covoptionEx = NbaUtils.getFirstCovOptionExtension(covoption); //SPR1986
		// SPR1986 code deleted
		// Coverage Option Key (Holding.Policy.Life.Coverage.CovOption) DXE: FSBBPHS
		covOptDXE =	covOptDXE + createDataExchange(covoption.getCovOptionKey(), COV_OPT_BENE_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0); //SPR2229
		if (covoption.hasTermDate()) { //SPR1986
			//Coverage Option Cease Date DXE: FSBBCDTE
			covOptDXE = covOptDXE + createDataExchange(covoption.getTermDate(), COV_OPT_TERMDATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9); //SPR1986
		} //SPR1986
		// Type of benefit (Holding.Policy.Life.Coverage.CovOption) DXE: FSBBENEF
		// Typecode UCT (CLUDT135)  OLI_LU_OPTTYPE
		covOptDXE =
			covOptDXE + createDataExchange(covoption.getLifeCovOptTypeCode(), COV_OPT_TYPE, NbaTableConstants.OLI_LU_OPTTYPE, CYBTBL_UCT, CT_CHAR, 3);
		if (covoptionEx != null) {
			// Not applicable if Product Type = 'F'.   - Benefit Number of Units (Holding.Policy.Life.Coverage.CovOption) DXE: FSBBUNT
			if (covoptionEx.hasOptionUnits() && planType.compareTo("F") != 0)
				covOptDXE =
					covOptDXE + createDataExchange(covoptionEx.getOptionUnits(), COV_OPT_OPTION_AMT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
		}
		//begin NBA111
		long covOptionLivesType = getCovOptionLivesType(NbaConstants.SYST_CYBERLIFE, covoption.getProductCode(), coverage.getProductCode(), compCode);
		if (coverage.hasLivesType() && NbaUtils.isJointLife(coverage.getLivesType())) {
			//FSBPIDNT
			String relRoleCode = "";
			String sPersonID;
			int roleTot;
			String roleTypeKey;
			if (covOptionLivesType != NbaConstants.LONG_NULL_VALUE) {
				if (covOptionLivesType != NbaOliConstants.OLI_COVLIVES_JOINTFTD) {
					//Gets the life participant associated with cov Option
					LifeParticipant lifeParticipant = NbaUtils.getLifeParticipantReference(covoption, coverage);
					if (lifeParticipant != null) {
						relRoleCode =
							getCyberValue(lifeParticipant.getLifeParticipantRoleCode(), CYBTRANS_ROLES, CYBTBL_PARTIC_ROLE, compCode, DEFAULT_COVID);
					}
				} else if (covOptionLivesType != NbaOliConstants.OLI_COVLIVES_JOINTFTD) {
					relRoleCode = getCyberValue(NbaOliConstants.OLI_PARTICROLE_PRIMARY, CYBTRANS_ROLES, CYBTBL_PARTIC_ROLE, compCode, DEFAULT_COVID);
				}
				//Concatenating coverage Id with relation Code .As counting has to be restarted with each  coverage
				roleTypeKey = coverage.getId() + ":" + relRoleCode;
				if (lifeParticipantRoleTypeMap == null) {
					lifeParticipantRoleTypeMap = new HashMap(3, 0.9F);
				}
				if (lifeParticipantRoleTypeMap.containsKey(roleTypeKey)) {
					roleTot = ((Integer) lifeParticipantRoleTypeMap.get(roleTypeKey)).intValue() + 1;
				} else {
					roleTot = 1;
				}
				lifeParticipantRoleTypeMap.put(roleTypeKey, new Integer(roleTot));
				sPersonID = relRoleCode + formatCyberInt(roleTot);
				covOptDXE = covOptDXE + createDataExchange(sPersonID, COV_OPT_PERSON_IDREF, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 12);
			}
		}
		//end NBA111
		return covOptDXE;
	}
	/**
	 * Create request DXE from CWA info in the Policy input XML
	 * @param policy input Policy object
	 * @return Request DXE based on input XML
	 * @throws NbaBaseException
	 */
	protected String createCWA(Policy policy) throws NbaBaseException {
		String cwaDXE = "";
		FinancialActivity financialactivity;
		FinancialActivityExtension financialactivityEx = null;
		CWAActivity cwaactivity;
		OLifEExtension olife;
		for (int i = 0; i < policy.getFinancialActivityCount(); i++) {
			financialactivity = policy.getFinancialActivityAt(i);
			financialactivityEx = null;
			cwaactivity = null;
			for (int j = 0; j < financialactivity.getOLifEExtensionCount(); j++) {
				olife = financialactivity.getOLifEExtensionAt(j);
				if (olife.isFinancialActivityExtension()) {
					financialactivityEx = olife.getFinancialActivityExtension();
					break;
				}
			}
			if (financialactivityEx != null)
				if (financialactivityEx.getCWAActivityCount() > 0)
					cwaactivity = financialactivityEx.getCWAActivityAt(0);
			cwaDXE = cwaDXE + TRANS_NEWAPP_CWA + COMP_CODE + "=" + compCode + ";";
			// CWA Amount (Holding.Policy.FinancialActivity) FCWAMONT
			cwaDXE =
				cwaDXE
					+ createDataExchange(
						formatCyberCurrency(financialactivity.getFinActivityGrossAmt()),
						FIN_GROSS_AMT,
						CYBTRANS_NONE,
						CYBTBL_NONE,
						CT_DOUBLE,
						0);
			//NBA093
			// CWA Date (Holding.Policy.FinancialActivity) FCWASOF
			cwaDXE =
				cwaDXE
					+ createDataExchange(
						formatCyberDate(financialactivity.getFinActivityDate()),
						FIN_ACT_DATE,
						CYBTRANS_NONE,
						CYBTBL_NONE,
						CT_DEFAULT,
						0);
			// CWA Type (Holding.Policy.FinancialActivity) FCWCHGTP
			// VT_I4 (Typecode) UCT (CLCWATP1)  OLIEXT_LU_CWATYPE
			cwaDXE =
				cwaDXE
					+ createDataExchange(
						formatCyberLong(financialactivity.getFinActivityType()),
						CWA_TYP_CODE,
						NbaTableConstants.OLIEXT_LU_CWATYPE,
						CYBTBL_UCT,
						CT_CHAR,
						1);
			//NBA093
			// CWA Entry Date (Holding.Policy.FinancialActivity) FCWENDAT
			cwaDXE =
				cwaDXE
					+ createDataExchange(
						formatCyberDate(financialactivity.getFinActivityDate()),
						CWA_ENT_DATE,
						CYBTRANS_NONE,
						CYBTBL_NONE,
						CT_DEFAULT,
						0);
			//NBA093
			if (cwaactivity.hasCWADestOrCreditAccountNumber()) {
				CWADestOrCreditAccountNumber destOrAccount = cwaactivity.getCWADestOrCreditAccountNumber();
				// Override Acct. Source Code (Holding.Policy.FinancialActivity.CWAActivity) FCWSOURC
				// VT_I4 (Typecode) UCT (CLUDT901)  OLIEXT_LU_CWASOURCE
				if (destOrAccount.isCWADest())
					cwaDXE =
						cwaDXE
							+ createDataExchange(
								formatCyberLong(destOrAccount.getCWADest()),
								OVERRIDE_ACCT_DEST_CD,
								NbaTableConstants.OLIEXT_LU_CWADEST,
								CYBTBL_UCT,
								CT_CHAR,
								3);
				// Accounting Override - Credit (Holding.Policy.FinancialActivity.CWAActivity) FCWCACCT
				// VT_I4 (Typecode)  OLIEXT_LU_CWAACCTNUM
				if (destOrAccount.isCreditAccountNumber())
					cwaDXE =
						cwaDXE
							+ createDataExchange(
								formatCyberLong(destOrAccount.getCreditAccountNumber()),
								ACCTG_OVERRIDE_CRED,
								CYBTRANS_NONE,
								CYBTBL_NONE,
								CT_CHAR,
								11);
			}
			if (cwaactivity.hasCWASourceOrDebitAccountNumber()) {
				CWASourceOrDebitAccountNumber sourceOrDebit = cwaactivity.getCWASourceOrDebitAccountNumber();
				// Override Acct. Destination Code (Holding.Policy.FinancialActivity.CWAActivity) FCWDEST
				// VT_I4 (Typecode) UCT (CLUDT902)  OLIEXT_LU_CWADEST
				if (sourceOrDebit.isCWASource())
					cwaDXE =
						cwaDXE
							+ createDataExchange(
								formatCyberLong(sourceOrDebit.getCWASource()),
								OVERRIDE_ACCT_SRC_CD,
								NbaTableConstants.OLIEXT_LU_CWASOURCE,
								CYBTBL_UCT,
								CT_CHAR,
								3);
				// Accounting Override - Debit (Holding.Policy.FinancialActivity.CWAActivity) FCWDACCT
				// VT_I4 (Typecode)  OLIEXT_LU_CWAACCTNUM
				if (sourceOrDebit.isDebitAccountNumber())
					cwaDXE =
						cwaDXE
							+ createDataExchange(
								formatCyberLong(sourceOrDebit.getDebitAccountNumber()),
								ACCT_OVERRIDE_DEB,
								CYBTRANS_NONE,
								CYBTBL_NONE,
								CT_CHAR,
								11);
			}
			// Best Interest Indicator (Holding.Policy.FinancialActivity.CWAActivity) FCWOVTYP
			// VT_I4 (Typecode)  OLI_LU_BOOLEAN Current Extension 
			cwaDXE =
				cwaDXE
					+ createDataExchange(formatCyberLong(financialactivity.getBestIntRateType()), BEST_INT_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);
			//NBA093
			// Cost Basis Adj. Amount (Holding.Policy.FinancialActivity.CWAActivity) FCWCOSTB
			if (cwaactivity.hasCostBasisAdjAmt())
				cwaDXE =
					cwaDXE
						+ createDataExchange(
							formatCyberCurrency(cwaactivity.getCostBasisAdjAmt()),
							COST_BASIS_ADJ_AMT,
							CYBTRANS_NONE,
							CYBTBL_NONE,
							CT_DOUBLE,
							0);
			// User costAdj to send to host for dtrain - Cost Basis Amount (Holding.Policy.FinancialActivity) COSTBASE
			if (financialactivity.hasCostBasisAdjAmt()) //NBA093
				cwaDXE =
					cwaDXE
						+ createDataExchange(
							formatCyberCurrency(financialactivity.getCostBasisAdjAmt()),
							COST_BASIS_ADJ_AMT,
							CYBTRANS_NONE,
							CYBTBL_NONE,
							CT_DOUBLE,
							0);
			//NBA093
			// Overrride CWA Agent ID (Holding.Policy.FinancialActivity.CWAActivity) FCWAGENT
			cwaDXE = cwaDXE + createDataExchange(cwaactivity.getCWAAgentOverride(), OVERRIDE_CWA_AGENT_ID, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 11);
			// If the asof date of  the CWA payment is to be the issue date, turn on bit in FCWFLAGB. - Set CWA Date to Issue Indicator (Holding.Policy.FinancialActivity.CWAActivity) FCWFLAGB
			// VT_I4 (Typecode)  OLI_LU_BOOLEAN Current Extension
			cwaDXE =
				cwaDXE
					+ createDataExchange(
						formatCyberBoolean(financialactivity.getIntTreatmentInd()),
						CW_FLAGB_3,
						CYBTRANS_NONE,
						CYBTBL_NONE,
						CT_CHAR,
						8);
			//NBA093
			// Override Interest (Holding.Policy.FinancialActivity) FCWOVINT
			cwaDXE =
				cwaDXE
					+ createDataExchange(
						formatCyberDouble(financialactivity.getIntPostingRate()),
						OVERRIDE_INT,
						CYBTRANS_NONE,
						CYBTBL_NONE,
						CT_DOUBLE,
						0);
			//NBA093
			// Pre-TEFRA Indicator (Holding.Policy.Life.LifeUSA) FCWFLAGB
			if (policy.hasLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty()) //NBA093
				if (policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isLife()) { //NBA093
					Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife(); //NBA093
					if (life.hasLifeUSA())
						if (life.getLifeUSA().hasTaxGrandfatheredType())
							cwaDXE =
								cwaDXE
									+ createDataExchange(
										formatCyberLong(life.getLifeUSA().getTaxGrandfatheredType()),
										CW_FLAGB_6,
										CYBTRANS_NONE,
										CYBTBL_NONE,
										CT_CHAR,
										8);
				}
			// SPR1197 lines deleted
			// CWA Originator (Defaults to Dept./Desk Code) (Holding.Policy.FinancialActivity.CWAActivity) FCWORGNT
			cwaDXE = cwaDXE + createDataExchange(financialactivity.getUserCode(), CWA_DPT_DESK, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 6); //NBA093
		}
		return cwaDXE;
	}
	/**
	 * Create Investment request DXE from holding input XML
	 * 
	 * @param holding input Holding xml object
	 * @return Request DXE based on input XML
	 * @throws NbaBaseException
	 */
	protected String createInvestment(Holding holding) throws NbaBaseException {
		Investment investment;
		SubAccount subaccount;
		String invDXE = "";
		long lastType = -1;
		try {
			investment = holding.getInvestment();
			for (int i = 0; i < investment.getSubAccountCount(); i++) {
				subaccount = investment.getSubAccountAt(i);
				// Fund Allocation Type (Holding.Policy.Investment.SubAccount)
				// Typecode UCT (CLAPTB44)  OLI_LU_SYSTEMATIC
				if (subaccount.hasSystematicActivityType()) {
					if (lastType != subaccount.getSystematicActivityType())
						invDXE =
							invDXE
								+ createDataExchange(
									subaccount.getSystematicActivityType(),
									"FALTYPE",
									NbaTableConstants.OLI_LU_SYSTEMATIC,
									CYBTBL_UCT,
									CT_CHAR,
									1);
					lastType = subaccount.getSystematicActivityType();
				}
				// Fund ID (Holding.Policy.Investment.SubAccount)
				// String UCT (FND_ID)  nbA Table = NBA_FUNDS
				if (subaccount.hasProductCode()) //NBA093
					invDXE = invDXE + createDataExchange(subaccount.getProductCode(), FUND_ID, CYBTRANS_FUNDS, CYBTBL_FUNDS, CT_CHAR, 3, productCode);
				//NBA093
				// Fund Allocation Percent (Holding.Policy.Investment.SubAccount)
				if (subaccount.hasAllocPercent()) {
					invDXE = invDXE + formatDataExchange("P", "FALALVAL", CT_CHAR, 12);
					invDXE = invDXE + createDataExchange(subaccount.getAllocPercent(), FUND_ALLOC_PCT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
				}
				// Fund Allocatin Units (Holding.Policy.Investment.SubAccount)
				if (subaccount.hasCurrNumberUnits()) {
					invDXE = invDXE + formatDataExchange("U", "FALALVAL", CT_CHAR, 12);
					invDXE = invDXE + createDataExchange(subaccount.getCurrNumberUnits(), FUND_ALLOC_UNITS, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
				}
				// Do not send to host. - Primary Investment Objective (Growth, Investment, or Both) (Holding.Policy.Investment.SubAccount)
				//  UCT (CLPCT221)  OLI_LU_INVESTOBJ
				//invDXE = invDXE + createDataExchange(subaccount.getProductObjective(), PRIM_INV_OBJ, NbaTableConstants.OLI_LU_INVESTOBJ, CYBTBL_UCT, CT_CHAR, 1);
				// Fund Allocation Value (Holding.Policy.Investment.SubAccount)
				if (subaccount.hasTotValue()) {
					invDXE = invDXE + formatDataExchange("D", "FALALVAL", CT_CHAR, 12);
					invDXE = invDXE + createDataExchange(subaccount.getTotValue(), FUND_ALLOC_VAL, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
				}
			}
		} catch (Exception e) {
			// There isn't a Base Exception for investments yet
			throw new NbaBaseException("Error parsing Investments", e);
		}
		return invDXE;
	}
	/**
	 * Create request DXE from Life input xml object
	 * 
	 * @param holding input Holding object
	 * @return Request DXE based on input XML
	 * @throws NbaBaseException
	 */
	protected String createLife(Holding holding) throws NbaBaseException {
		Policy policy = holding.getPolicy();
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeOrAnnuityOrDisabilityHealth = null; //NBA093
		Life life = null;
		Annuity annuity = null;
		String dxe = "";
		int iCovTot;
		if (policy.hasLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty()) { //NBA093
			lifeOrAnnuityOrDisabilityHealth = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(); //NBA093
			if (lifeOrAnnuityOrDisabilityHealth.isLife())
				life = lifeOrAnnuityOrDisabilityHealth.getLife();
			else if (lifeOrAnnuityOrDisabilityHealth.isAnnuity()) {
				annuity = lifeOrAnnuityOrDisabilityHealth.getAnnuity();
			} else {
				if (getLogger().isWarnEnabled())
					getLogger().logWarn("Error Policy XML has no Life information");
				return "";
			}
		} else {
			if (getLogger().isInfoEnabled())
				getLogger().logInfo("Error Policy XML has no life, annuity, or disability health information");
			return "";
		}
		if (life != null) {
			// Individual Life fields
			//begin SPR1986
			if (policy.getProductType() == VAR_UNIV_LIFE || policy.getProductType() == UNIV_LIFE) {
				LifeExtension lifeEx = NbaUtils.getFirstLifeExtension(life);
				if (lifeEx != null) {
					if (lifeEx.hasRequestedMaturityDate()) {
						dxe = dxe + createDataExchange(RQST_MATURITY_DATE_OVERRIDE_E0_FLG, RQST_MATURITY_DATE_OVERRIDE_E0, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 8);
						dxe =
							dxe
								+ createDataExchange(
									formatCyberDate(lifeEx.getRequestedMaturityDate()),
									RQST_MATURITY_DATE,
									CYBTRANS_NONE,
									CYBTBL_NONE,
									CT_DEFAULT,
									0);
						dxe = dxe + createDataExchange(FLAG_BIT_ON, RQST_MATURITY_DATE_OVERRIDE_66, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 8);
					} else if (lifeEx.hasRequestedMaturityAge()) {
						dxe =
							dxe + createDataExchange(lifeEx.getRequestedMaturityAge(), RQST_MATURITY_AGE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 3);
					} else if (lifeEx.hasRequestedMaturityDur()) {
						dxe =
							dxe + createDataExchange(lifeEx.getRequestedMaturityDur(), RQST_MATURITY_DUR, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 3);
					}
				}
			}
			//end SPR1986
			//begin SPR2099
			//Override MEC Indicator(MEC1035): 0-Obtained from PDF, 1-Entered by user			
			if (life.hasLifeUSA() && life.getLifeUSA().getMEC1035()) {
				//MEC Indicator entered by the user
				dxe = dxe + createDataExchange(life.getLifeUSA().getMECInd(), POL_MEC_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			}
			//end SPR2099
			// Coverages
			iCovTot = life.getCoverageCount();
			for (int i = 0; i < iCovTot; i++) {
				Coverage coverage = life.getCoverageAt(i);
				dxe = dxe + createCoverage(coverage, policy, life);
			}
		}
		if (annuity != null) {
			dxe = dxe + createAnnuity(annuity, policy);
		}
		return dxe;
	}
	/**
     * Create party DXE from input XML103
     * 
     * @param olife input OLife xml object
     * @return Request DXE based on input XML
     * @throws NbaBaseException
     */
    protected String createParties(OLifE olife) throws NbaBaseException {
    	// This map will help determine how many of each role type we have
    	HashMap roleTypeMap = new HashMap();
        Party party;
        Relation relation;
        int i = 0, j, pTot, rTot;
        String partyId;
        String partyDXE;
        int partyOcc = 0;
        String relPID = new String();
        Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); //NBA093
        // Initialize result strings
        partyDXE = "";
        addressDXE = "";
        pTot = olife.getPartyCount();
        rTot = olife.getRelationCount();
        // for each of our party objects
        for (i = 0; i < pTot; i++) {
            party = olife.getPartyAt(i);
            partyId = party.getId();
            partyOcc = getOccurance(partyId);
            if (partyOcc == -1) {
                // This is an error condition. We cannot process this party
                continue;
            }
            // for each of our relation objects
            for (j = 0; j < rTot; j++) {
                relation = olife.getRelationAt(j);
                String relatedObjectID = relation.getRelatedObjectID();
                // see if this a relationship for our party
                //begin NBA151
                //Only allowed insured parties from the primary holding to be built
                if (relatedObjectID.equals(partyId)
                        && (relation.getOriginatingObjectType() != OLI_HOLDING || (relation.getOriginatingObjectType() == OLI_HOLDING && relation
                                .getOriginatingObjectID().equals(holding.getId())))) {
                    //end NBA151
                    //
                    // process Party/Relationship
                    //
                    partyDXE = partyDXE + createPartyWithRelation(party, relation, roleTypeMap, holding);
                }
            }
        }
        //
        // Repeat loop to build beneficiary information
        //
        for (i = 0; i < pTot; i++) {
            party = olife.getPartyAt(i);
            partyId = party.getId();
            partyOcc = getOccurance(partyId);
            if (partyOcc == -1) {
                // This is an error condition. We cannot process this party
                continue;
            }
            // for each of our relation objects
            for (j = 0; j < rTot; j++) {
                relation = olife.getRelationAt(j);
                String relatedObjectID = relation.getRelatedObjectID();
                // see if this a relationship for our party
                if (relatedObjectID.compareTo(partyId) == 0) {
                    //
                    // process Party/Relationship
                    //
                    if (relation.getRelationRoleCode() == 34) {
                        if (getPartyId(relation.getOriginatingObjectID(), "00") != "") {
                            relPID = getPartyId(relation.getOriginatingObjectID(), "00");
                        } else if (getPartyId(relation.getOriginatingObjectID(), "01") != "") {
                            relPID = getPartyId(relation.getOriginatingObjectID(), "01");
                        } else if (getPartyId(relation.getOriginatingObjectID(), "60") != "") {
                            relPID = getPartyId(relation.getOriginatingObjectID(), "60");
                        } else if (getPartyId(relation.getOriginatingObjectID(), "40") != "") {
                            relPID = getPartyId(relation.getOriginatingObjectID(), "40");
                        } else if (getPartyId(relation.getOriginatingObjectID(), "50") != "") {
                            relPID = getPartyId(relation.getOriginatingObjectID(), "50");
                        }
                        if (beneficiaryDXE.indexOf(relPID) == -1) {
                            beneficiaryDXE = beneficiaryDXE + formatDataExchange(relPID, BENE_ORIG_ID, CT_CHAR, 12);
                        }
                        beneficiaryDXE = beneficiaryDXE + createBeneficiary(party, relation, roleTypeMap);
                    }
                }
            }
        }
        return partyDXE + addressDXE;
    }
	/**
	 * Creates party dxe in context of relation information
	 * 
	 * @param party input Party xml object
	 * @param relation input Relation xml object
	 * @param roleTypeMap role type hashmap
	 * @param holding input Holding xml object
	 * @return Request DXE based on input XML
	 * @throws NbaBaseException
	 */
	protected String createPartyWithRelation(Party party, Relation relation, HashMap roleTypeMap, Holding holding) throws NbaBaseException {
        OLifEExtension olifeEx;
        int roleTot;
        String sPersonId;
        Organization organization;
        PersonOrOrganization perOrOrg;
        Person person;
        // SPR3290 code deleted
        RelationProducerExtension relprodEx;
        PartyExtension partyEx;
        // SPR3290 code deleted
        String partyDXE = "";
        //begin SPR2992
        try {
            // 
            switch ((int) relation.getRelationRoleCode()) {
            // FAGAGTID=CK101;FAGUSECD=;FAGSITCD=;FSPTRSEQ=0;FAGPCTSH=100;FAGPCTSV=100;
            case (int) NbaOliConstants.OLI_REL_PRIMAGENT: // writing agent NBA093
                if (party.hasProducer()) {
                    Producer producer = party.getProducer();
                    if (producer.getCarrierAppointmentCount() > 0) {
                        CarrierAppointment carrierAppointment = producer.getCarrierAppointmentAt(0);
                        // Servicing Agency
                        writingAgentDXE = writingAgentDXE
                                + createDataExchange(carrierAppointment.getCompanyProducerID(), WRITING_AGT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 11);
                    }
                }
                for (int i = 0; i < relation.getOLifEExtensionCount(); i++) {
                    olifeEx = relation.getOLifEExtensionAt(i);
                    if (olifeEx.isRelationProducerExtension()) {
                        relprodEx = olifeEx.getRelationProducerExtension();
                        // Situation Code (Relation.RelationProducer)
                        //  UCT (CLCMTB02) nbA table = OLIEXT_LU_SITCODE SPR3164
                        writingAgentDXE = writingAgentDXE
                                + createDataExchange(relprodEx.getSituationCode(), SITUATION_CD, NbaTableConstants.OLIEXT_LU_SITCODE, CYBTBL_UCT,
                                        CT_CHAR, 0); //SPR3164
                        //NBA093
                        // Volumn Share Percent (Relation.RelationProducer)
                        writingAgentDXE = writingAgentDXE
                                + createDataExchange(formatCyberDouble(relation.getVolumeSharePct()), VOL_SHARE_PCT, CYBTRANS_NONE, CYBTBL_NONE,
                                        CT_DEFAULT, 0);
                        //NBA093
                    }
                    // client OLifeExtention not sent to host
                }
                // If the interest percent for a beneficiary equals 100% the Beneficiary Distribution Option should be set to Balance and the Interest
                // Percent amount should not be sent to the host. - Percentage of distribution for beneficiary (Relation)
                writingAgentDXE = writingAgentDXE
                        + createDataExchange(formatCyberDouble(relation.getInterestPercent()), COM_SHARE_PCT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE,
                                0);
                return "";
            // Once the program locates that role it will then it will go to that Party ID within the
            // Party object to find the agent/agency ID
            // FBRAGENT=;FBRAGNCY=;
            case (int) NbaOliConstants.OLI_REL_SERVAGENT: // servicing agent NBA093
                // Producer Info
                if (party.hasProducer()) {
                    Producer producer = party.getProducer();
                    if (producer.getCarrierAppointmentCount() > 0) {
                        CarrierAppointment carrierAppointment = producer.getCarrierAppointmentAt(0);
                        // Servicing Agency
                        servicingAgentDXE = servicingAgentDXE
                                + createDataExchange(carrierAppointment.getCompanyProducerID(), SERVICE_AGENT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR,
                                        11);
                    }
                }
                return "";
            case (int) NbaOliConstants.OLI_REL_SERVAGENCY: // Servicing Agency NBA093
                // Producer Info
                if (party.hasProducer()) {
                    Producer producer = party.getProducer();
                    if (producer.getCarrierAppointmentCount() > 0) {
                        CarrierAppointment carrierAppointment = producer.getCarrierAppointmentAt(0);
                        servicingAgencyDXE = servicingAgencyDXE
                                + createDataExchange(carrierAppointment.getCompanyProducerID(), AGENCY_NAME, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 11);
                    }
                }
                return "";
            }
            String relRoleCode;
            if (planType.compareTo("F") == 0)
                relRoleCode = getCyberValue(relation.getRelationRoleCode(), CYBTRANS_ROLES, CYBTBL_RELATION_ROLE_F, compCode, DEFAULT_COVID);
            else
                relRoleCode = getCyberValue(relation.getRelationRoleCode(), CYBTRANS_ROLES, CYBTBL_RELATION_ROLE, compCode, DEFAULT_COVID);
            if (relRoleCode.compareTo("") == 0) {
                if (getLogger().isWarnEnabled())
                    getLogger().logWarn(
                            "Could not process Party: '" + party.getId() + "' Relation: '" + relation.getId()
                                    + ", no translation provided for relRolCode - Table 'NBA_ROLES', relRoleCode '"
                                    + Long.toString(relation.getRelationRoleCode()) + "', Company Code = '" + compCode + "', Cov Key = '"
                                    + DEFAULT_COVID + "'.");
                return "";
            }
            if (roleTypeMap.containsKey(relRoleCode))
                roleTot = ((Integer) roleTypeMap.get(relRoleCode)).intValue() + 1;
            else
                roleTot = 1;
            roleTypeMap.put(relRoleCode, new Integer(roleTot));
            sPersonId = relRoleCode + formatCyberInt(roleTot);
            partyDXE = partyDXE + formatDataExchange(sPersonId, PERS_SEQ_ID, CT_CHAR, 12);
            //
            // Add this user id to the partyMap for later
            addPartyId(party.getId(), relRoleCode, sPersonId);
            // initialize fields
            //
            person = null;
            organization = null;
            if (party.hasPersonOrOrganization()) {
                perOrOrg = party.getPersonOrOrganization();
                if (perOrOrg.isPerson()) {
                    person = perOrOrg.getPerson();
                } else {
                    organization = perOrOrg.getOrganization();
                }
            }
            //
            // Person Info
            if (person != null) {
                // First Name
                if (person.hasFirstName())
                    partyDXE = partyDXE + createDataExchange(person.getFirstName(), FIRST_NAME, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 20);
                // Middle Name or initial
                if (person.hasMiddleName())
                    partyDXE = partyDXE + createDataExchange(person.getMiddleName(), MIDDLE_NAME, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);
                // Last Name
                if (person.hasLastName())
                    partyDXE = partyDXE + createDataExchange(person.getLastName(), LAST_NAME, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 20);
                // Prefix
                // UCT (CLUDT150) OLIEXT_LU_PREFIX
                if (person.hasPrefix())
                    partyDXE = partyDXE + createDataExchange(person.getPrefix(), PREFIX, NbaTableConstants.OLIEXT_LU_PREFIX, CYBTBL_UCT, CT_CHAR, 9);
                // Suffix
                if (person.hasSuffix())
                    partyDXE = partyDXE + createDataExchange(person.getSuffix(), SUFFIX, NbaTableConstants.OLIEXT_LU_SUFFIX, CYBTBL_UCT, CT_CHAR, 9);
                // Age (Party.Person)
                if (person.hasAge())
                    partyDXE = partyDXE + createDataExchange(formatCyberInt(person.getAge()), AGE, CYBTRANS_NONE, CYBTBL_NONE, CT_USHORT, 0);
                // Date of Birth (Party.Person)
                if (person.hasBirthDate())
                    partyDXE = partyDXE
                            + createDataExchange(formatCyberDate(person.getBirthDate()), BIRTH_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
                // Gender Code (Party.Person) DXE: FLCSEXCD
                // Typecode UCT (CLSEXCD) OLI_LU_GENDER
                if (person.hasGender())
                    partyDXE = partyDXE + createDataExchange(person.getGender(), GENDER, "OLI_LU_GENDER", CYBTBL_UCT, CT_CHAR, 1);
                // Marital Status (Party.Person) DXE: FLCMARST
                // Typecode UCT (CLPCTB27) OLI_LU_MARSTAT
                if (person.hasMarStat())
                    partyDXE = partyDXE + createDataExchange(person.getMarStat(), MAR_ST, "OLI_LU_MARSTAT", CYBTBL_UCT, CT_CHAR, 1);
                PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person); //SPR1778
                if (personExtension != null && personExtension.hasRateClassAppliedFor()) //SPR1778 SPR1943
                    partyDXE = partyDXE + createDataExchange(personExtension.getRateClassAppliedFor(), //SPR1778 SPR1943
                            SMOKE_STATUS, NbaTableConstants.NBA_RATE_CLASS, //SPR1778 SPR1943 SPR3134
                            CYBTBL_UCT, //SPR1778 SPR1943
                            CT_CHAR, 1); //SPR1778
                // NBA093 deleted 4 lines
                // Birth Place Code (Party.Person)
                // Typecode UCT (ST_CTL) OLI_LU_STATE
                if (person.hasBirthJurisdictionTC()) //NBA093
                    partyDXE = partyDXE + createDataExchange(formatCyberLong(person.getBirthJurisdictionTC()), //NBA093
                            BIRTH_STATE, NbaTableConstants.NBA_STATES, CYBTBL_STATE_TC, CT_CHAR, 3);
                // NBA093 deleted 2 lines
                // Citizenship of the client (Party.Person)
                // Typecode UCT (CEUDT121) nbA table = NBA_CITIZENSHIP
                if (person.hasCitizenship())
                    partyDXE = partyDXE + createDataExchange(person.getCitizenship(), CLIENT_CITIZENSHIP, "NBA_Citizenship", CYBTBL_UCT, CT_CHAR, 1);
            }
            //End Person Info
            // If Is Owner
            //begin SPR2287
            if (NbaOliConstants.OLI_REL_OWNER == relation.getRelationRoleCode()) {
                // Interest Percent (Relation) DXE: FLCOWNER
                if (relation.hasInterestPercent()) {
                    partyDXE = partyDXE + createDataExchange(INTEREST_PERCENT_OWNER, INTEREST_PERCENT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);
                }
            }
            //end SPR2287
            // Tax Identification Type (Party) DXE: FLCTAXCD
            //  UCT (??) OLI_LU_GOVTIDTC
            if (party.hasGovtIDTC()) { //NBA093
                partyDXE = partyDXE + createDataExchange(party.getGovtIDTC(), GOV_ID_TC, "OLI_LU_GOVTIDTC", CYBTBL_UCT, CT_CHAR, 1); //NBA093
            }
            //begin SPR1106
            else {
                if (party.hasPersonOrOrganization() && party.hasGovtID()) {
                    if (party.getPersonOrOrganization().isPerson()) {
                        partyDXE = partyDXE + createDataExchange(SOC_SEC_TAXID, GOV_ID_TC, "OLI_LU_GOVTIDTC", CYBTBL_UCT, CT_CHAR, 1);
                    } else {
                        partyDXE = partyDXE + createDataExchange(CORP_TAXID, GOV_ID_TC, "OLI_LU_GOVTIDTC", CYBTBL_UCT, CT_CHAR, 1);
                    }
                }
            } //end SPR1106
            // Tax Identification Number (Party) DXE: FLCTAXNO
            if (party.hasGovtID())
                partyDXE = partyDXE + createDataExchange(party.getGovtID(), GOV_ID, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 10);
            // Party type indicator - (Party)
            // VT_I4 OLIEXT_LU_PARTYTYPE
            if (party.hasPartyTypeCode() && organization == null) //ACN005
                partyDXE = partyDXE
                        + createDataExchange(party.getPartyTypeCode(), PARTY_TYPE_CODE, NbaTableConstants.OLI_LU_PARTY, CYBTBL_UCT, CT_CHAR, 1);
            for (int i = 0; i < party.getOLifEExtensionCount(); i++) {
                olifeEx = party.getOLifEExtensionAt(i);
                if (olifeEx.isPartyExtension()) {
                    partyEx = olifeEx.getPartyExtension();
                    // State Withholding (None/Standard) (Party)
                    //  UCT (CLULT015) OLI_LU_WITHCALCMTH
                    // begin NBA093
                    int count = partyEx.getTaxWithholdingCount();
                    for (int j = 0; j < count; j++) {
                        TaxWithholding taxWithholding = partyEx.getTaxWithholdingAt(j);
                        if ((taxWithholding.getTaxWithholdingPlace() == OLI_TAXPLACE_JURISDICTION) && (taxWithholding.hasTaxWithholdingType())) {
                            // end NBA093
                            partyDXE = partyDXE + createDataExchange(formatCyberLong(taxWithholding.getTaxWithholdingType()), //NBA093
                                    ST_WITHHOLDING, NbaTableConstants.OLI_LU_WITHCALCMTH, CYBTBL_UCT, CT_CHAR, 1);
                        }
                    } //NBA093
                    //NBA093 deleted code
                } // client OLifeExtention not sent to host
            } //
            // VT_I4 UCT (CLULT015) OLI_LU_WITHCALCMTH
            //NBA093 begin
            if (planType.compareTo("F") == 0) {
                Annuity annuity = holding.getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity();
                Payout payout = NbaUtils.getFirstPayout(annuity);
                if (payout.hasTaxWithheldInd()) {
                    TaxWithholding taxWithHolding = NbaUtils.getFirstTaxWithholding(payout);
                    partyDXE = partyDXE
                            + createDataExchange(formatCyberLong(taxWithHolding.getTaxWithholdingType()), FEDERAL_WITHHOLDING_IND,
                                    NbaTableConstants.OLI_LU_WITHCALCMTH, CYBTBL_UCT, CT_CHAR, 1);
                }
            }
            //NBA093 end
            // Organization Info
            if (organization != null) { // Corporation Name (Party.Organization) DXE: FLCCORP
                if (organization.hasDBA())
                    partyDXE = partyDXE + createDataExchange(organization.getDBA(), CORPNAME, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 40);
                //begin ACN005
                if (organization.hasOrgForm()) { // Legal Entity
                    partyDXE = partyDXE
                            + createDataExchange(organization.getOrgForm(), PARTY_TYPE_CODE, NbaTableConstants.OLI_LU_ORGFORM, CYBTBL_UCT, CT_CHAR, 1);
                } else if (party.hasPartyTypeCode() && organization == null) {
                    partyDXE = partyDXE
                            + createDataExchange(party.getPartyTypeCode(), PARTY_TYPE_CODE, NbaTableConstants.OLI_LU_PARTY, CYBTBL_UCT, CT_CHAR, 1);
                }
                //end ACN005
            } // End Organization Info
            // Producer Info
            if (party.hasProducer()) {
                Producer producer = party.getProducer();
                if (producer.getCarrierAppointmentCount() > 0) {
                    CarrierAppointment carrierAppointment = producer.getCarrierAppointmentAt(0);
                    // Servicing Agency
                    partyDXE = partyDXE
                            + createDataExchange(carrierAppointment.getCompanyProducerID(), AGENCY_NAME, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 11);
                }
            } // End Producer Info
            for (int i = 0; i < party.getPhoneCount(); i++) {
                Phone phone = party.getPhoneAt(i);
                // Phone types = 'B' or 'F' or 'H'  (BUSPHN and FAX) - Phone Type (Party.Phone) DXE: FLC2PHNT, DXE Master(s): FLCPIDNT
                // Typecode  OLI_LU_PHONETYPE
                if (phone.getPhoneTypeCode() != OLI_PHONETYPE_HOME) {
                    //SPR1036
                    partyDXE = partyDXE + createDataExchange(phone.getPhoneTypeCode(), PHONE_ID, NbaTableConstants.OLI_LU_PHONETYPE, //SPR1036
                            CYBTBL_UCT, //SPR1036
                            CT_CHAR, 12);
                } //SPR1036
                String fullphone = "";
                if (phone.hasDialNumber()) {
                    if (phone.hasAreaCode())
                        fullphone = phone.getAreaCode();
                    fullphone = fullphone + phone.getDialNumber();
                    // Set concatenation of AreaCode and DialNumber. 
                    // To FLCPHONE or FLC2PHN depending on the value of PhoneTypeCode(FLC2PHNT)
                    // Dial Number (Party.Phone) DXE: FLCPHONE
                    if (phone.getPhoneTypeCode() == OLI_PHONETYPE_HOME) {
                        //SPR1036
                        partyDXE = partyDXE + createDataExchange(fullphone, PHONE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 12);
                    } else {
                        //SPR1036
                        partyDXE = //SPR1036
                        partyDXE //SPR1036
                                + createDataExchange(fullphone, PHONE2, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 12);
                        //SPR1036
                    } //SPR1036
                }
            } //Build email address DXE
            for (int i = 0; i < party.getEMailAddressCount(); i++) {
                EMailAddress email = party.getEMailAddressAt(i);
                if (email.hasAddrLine()) {
                    if (email.hasEMailType()) {
                        partyDXE = partyDXE
                                + createDataExchange(formatCyberLong(email.getEMailType()), EMAIL_TYPE, NbaTableConstants.OLI_LU_EMAILTYPE,
                                        CYBTBL_UCT, CT_CHAR, 3);
                    } else {
                        //if type is missing set default to Personal type
                        partyDXE = partyDXE + createDataExchange(PERSONAL_EMAIL_TYPE, EMAIL_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 3);
                    }
                    partyDXE = partyDXE + createDataExchange(sPersonId, EMAIL_PERS_SEQ, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 4);
                    partyDXE = partyDXE + createDataExchange("E", ELECT_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 3);
                    partyDXE = partyDXE + createDataExchange(email.getAddrLine(), EMAIL_ADDRESS, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 137);
                }
            } //
            // Build address DXE
            //
            addressDXE = addressDXE + createAddressFromParty(party, sPersonId);
        } catch (Exception e) {
            throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_PARTIES, e);
        } // GOBACK Doesn't appear that we use this field...
        //partyDXE = partyDXE + formatDataExchange(relRoleCode, RELATIONSHIP_TYPE, CT_CHAR, 1 );
        //
        // Word document specifies that these OlifeExtensions exist
        // FMDEMPNM=;FMDINVOB=;FMDSPSRS=;FMDSPSMN=;
        return partyDXE;
        //end SPR2992
    }
	/**
	 * Get the logger instance, create NbaLogger class if necessary
	 * 	  
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	private static NbaLogger getLogger() { //NBA044
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaCyberNewAppAdapter.class.getName()); //NBA044
			} catch (Exception e) {
				NbaBootLogger.log("NbaCyberNewAppAdapter could not get a logger from the factory."); //NBA044
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * Retrieve the Plan Riders Table from the DB
	 * 
	 * @param compCd Company code
	 * @return com.csc.fsg.nba.tableaccess.NbaTableData[]
	 */
	protected NbaTableData[] getPlansRidersTable(String compCode) {
		Date currentDate = new Date();
		HashMap aCase = new HashMap();
		aCase.put("company", compCode);
		aCase.put("appState", "*");
		aCase.put("appDate", currentDate);
		aCase.put("backendSystem", NbaConstants.SYST_CYBERLIFE); //SPR1018
		//NBA044 code deleted
		getLogger().logDebug("Loading NBA_PLANS_RIDERS"); //NBA044
		NbaTableData[] tArray = null;
		try {
			tArray = ntsAccess.getDisplayData(aCase, "NBA_PLANS_RIDERS");
		} catch (NbaDataAccessException e) {
			//NBA044 code deleted
			getLogger().logWarn("NbaDataAccessException Loading NBA_PLANS_RIDERS");
		}
		//NBA044 code deleted
		getLogger().logDebug("Loaded NBA_PLANS_RIDERS");
		return (tArray);
	}
	/**
	 * Main method for testing adapter from file based XML test data
	 * 
	 * @param args command line arguments 
	 */
	public static void main(String[] args) throws NbaBaseException, IOException {
		//FileReader inputFile = new FileReader("d:\\Nba Docs\\XML0402\\Test XML\\nba_VUL.xml");
		//FileReader inputFile = new FileReader("d:\\Nba Docs\\XML0402\\Test XML\\XML App 9 Variable Annuity.xml");
		//FileReader inputFile = new FileReader("d:\\Nba Docs\\XML0402\\AppWholeLifeSW.xml");
		//FileWriter outputFile = new FileWriter("c:\\Nbadoc\\generatedDXE.txt");
		// SPR3290 code deleted
		//int chars_read = 0;
		// SPR3290 code deleted
		String filename = "c:\\Nbadoc\\SPR2356.xml";
		//String returnRequest;
		TXLife txLife = null;
		NbaTXLife nbaTxLife = new NbaTXLife();
		try {
			//chars_read = inputFile.read(charbuf, 0, (64 * 1024));
			//inputFile.close();
			//if (chars_read > 0) {
				//String xmlDoc = new String(charbuf, 0, chars_read);
				//NbaCyberNewAppAdapter testAdapt = new NbaCyberNewAppAdapter();
				NbaCyberAdapter testAdapt = new NbaCyberAdapter();
				txLife = TXLife.unmarshal( new FileInputStream(filename) );
				
				getLogger().logDebug("About to call adapter"); //NBA044
				//returnRequest = testAdapt.create103Request(txLife);
			nbaTxLife.setTXLife(txLife);
			nbaTxLife = testAdapt.submitRequestToHost(nbaTxLife);
					System.out.print(nbaTxLife.toXmlString());
			
				//getLogger().logDebug(returnRequest); //NBA044
				//outputFile.write(returnRequest);
				//outputFile.close();
			//}
		} catch (Exception e) {
			//
			// Check for any validation errors.
			//
			if (txLife != null) {
				java.util.Vector v = txLife.getValidationErrors();
				if (v != null) {
					for (int ndx = 0; ndx < v.size(); ndx++) {
						XmlValidationError error = (XmlValidationError) v.get(ndx);
						System.out.print("\tError(" + ndx + "): ");
						if (error != null)
							//NBA044 start
							if (getLogger().isErrorEnabled()) {
								getLogger().logError(error.getErrorMessage());
							}
						//NBA044 end
						else
							getLogger().logError("A problem occurred retrieving the validation error."); //NBA044
					}
				}
			}
			throw new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_PARSE, e);
		}
	}
	/**
	 * Check for valid plan rider value
	 * 
	 * @param planRider java.lang.String
	 * @return boolean
	 */
	protected boolean validPlanRider(String planRider) {
		if (plansRidersData == null)
			plansRidersData = ((NbaPlansRidersData[]) getPlansRidersTable(compCode));
		if (plansRidersData == null) {
			//NBA044 code deleted
			getLogger().logWarn("Error: Could not load NBA_PLANS_RIDERS");
			return false;
		}
		int iTot = plansRidersData.length;
		for (int i = 0; i < iTot; i++) {
			if (plansRidersData[i].getPlanRiderKey().compareTo(planRider) == 0) {
				if (getLogger().isDebugEnabled())
					getLogger().logDebug(planRider + " is a valid plan.");
				return true;
			}
		}
		if (getLogger().isDebugEnabled())
			getLogger().logDebug(planRider + " is not a valid plan.");
		return false;
	}
	/**
	 * Create the payment mode DXEs. If the mode is a special frequency, then the FBRBMODE dxe is set to monthly("01")
	 * and the following new DXEs will be created. FBRMDNST(Non-Standard mode), FFCEXTMD(Extract mode), FFCQPBAS
	 * (Quoted premium basis frequency), FFCINBDT(Initial bill to date), FFCLDEDT(Initial deduction date), FFCPYDFR
	 * (Payroll frequency), FBRSTSKM(First skip month), FFCQPAMT(Quoted premium basis amount) 
	 * @param policy The Policy object
	 * @param policyEx PolicyExtension object
	 */
	//SPR1079 New Method 
	protected String createPaymentModeDXE(Policy policy, PolicyExtension policyEx) {
		String billingDXE = "";
		if (NbaUtils.isSpecialFrequency(policy)) {
			//Payment mode DXE: FRBMODE
			billingDXE = billingDXE + createDataExchange("01", PAYMENT_MODE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			//Non Standard mode DXE: FBRMDNST				
			billingDXE = billingDXE + createDataExchange(getSpcMode(policy), INF_SP_FREQ_NONSTANDARD_MODE, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
			//Extract mode DXE: FFCEXTMD				
			billingDXE =
				billingDXE
					+ createDataExchange(
						policy.getPaymentMode(),
						INF_SP_FREQ_EXTRACT_MODE,
						NbaTableConstants.OLI_LU_PAYMODE,
						CYBTBL_UCT,
						CT_DEFAULT,
						0);
			if (policyEx != null) {
				//Quoted premium basis frequency DXE : FFCQPBAS				  		
				if (policyEx.hasQuotedPremiumBasisFrequency()) {
					billingDXE =
						billingDXE
							+ createDataExchange(
								policyEx.getQuotedPremiumBasisFrequency(),
								QUOTED_PREM_BASIS_FREQUENCY,
								NbaTableConstants.OLIEXT_LU_PREMBASFREQ,
								CYBTBL_UCT,
								CT_DEFAULT,
								0);
				}
				//Initial BillTO Date DXE : FFCINBDT
				if (policyEx.hasInitialBillToDate()) {
					billingDXE =
						billingDXE
							+ createDataExchange(policyEx.getInitialBillToDate(), INITIAL_BILL_TO_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 0);
				}
				// Initial Deduction Date DXE : FFCLDEDT
				if (policyEx.hasInitialDeductionDate()) {
					billingDXE =
						billingDXE
							+ createDataExchange(policyEx.getInitialDeductionDate(), INITIAL_DEDUCTION_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_DATE, 0);
				}
				// Payroll frequency DXE : FFCPYDFR
				if (policyEx.hasPayrollFrequency()) {
					billingDXE =
						billingDXE
							+ createDataExchange(
								policyEx.getPayrollFrequency(),
								PAYROLL_FREQUENCY,
								NbaTableConstants.OLIEXT_LU_PAYROLLFREQ,
								CYBTBL_UCT,
								CT_DEFAULT,
								0);
				}
				// First Month Skip DXE : FBRSTSKM
				if (policyEx.hasFirstSkipMonth() && !(policyEx.getFirstSkipMonth().equalsIgnoreCase("0"))) {
					billingDXE =
						billingDXE + createDataExchange(policyEx.getFirstSkipMonth(), FIRST_MONTH_SKIP, CYBTRANS_NONE, CYBTBL_NONE, CT_DEFAULT, 0);
				}
				// Quoted Premium Basis Amt DXE : FFCQPAMT
				if (policyEx.hasQuotedPremiumBasisAmt() && policyEx.getQuotedPremiumBasisAmt() != 0) {
					billingDXE =
						billingDXE
							+ createDataExchange(policyEx.getQuotedPremiumBasisAmt(), QUOTED_PREM_BASIS_AMT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0);
				}
			}
		} else {
			billingDXE =
				billingDXE + createDataExchange(policy.getPaymentMode(), PAYMENT_MODE, NbaTableConstants.OLI_LU_PAYMODE, CYBTBL_UCT, CT_DEFAULT, 0);
		}
		return billingDXE;
	}
	/**
	* Returns lives type for a given covoption
	* @param String backEndSysId
	* @param String productCode
	* @param String coverageKey
	* @param String compCode
	* @return long livestype.
	*/
	//NBA111 new method
	public long getCovOptionLivesType(String backEndSysId, String productCode, String coverageKey, String compCode) throws NbaDataAccessException {
		NbaAllowableBenefitsData[] benefits = (NbaAllowableBenefitsData[]) getAllowableBenefitsData(compCode, backEndSysId, coverageKey);
		long livesType = NbaConstants.LONG_NULL_VALUE;
		for (int i = 0; i < benefits.length; i++) {
			if (productCode.equals(benefits[i].getBesBenefitId())) {
				livesType = benefits[i].getLives();
				break;
			}
		}
		return livesType;
	}
	/**
	* Returns allowable benefits type for a given coverageKey, backend system and company code
	* @param String backEndSysId
	* @param String coverageKey
	* @param String compCode
	* @return NbaTableData[] tArray.
	*/
	//NBA111 new method
	public NbaTableData[] getAllowableBenefitsData(String compCode, String backEndSysId, String coverageKey) throws NbaDataAccessException {
		NbaTableAccessor nta = new NbaTableAccessor();
		// SPR3290 code deleted
		HashMap aCase = new HashMap();
		aCase.put(NbaTableAccessConstants.C_COMPANY_CODE, compCode);
		aCase.put(NbaTableAccessConstants.C_SYSTEM_ID, backEndSysId);
		aCase.put(NbaTableAccessConstants.C_COVERAGE_KEY, coverageKey);
		NbaTableData[] tArray = null;
		tArray = nta.getDisplayData(aCase, NbaTableConstants.NBA_ALLOWABLE_BENEFITS);
		return tArray;
	}

    /**
	 * Prepares DXE to be sent to host from NbaTxLife
	 * @param XML document Contains the transaction request for the host
	 * @return DXE request for the host
	 * @exception throws NbaBaseException and java.rmi.RemoteException
	 */
	//NBA195 new method
	public String prepareRequestToHost(Object nbATxLife) {
		String request = null;
		try {
			TXLife txLife = ((NbaTXLife) nbATxLife).getTXLife();
			NbaCyberRequests requests = new NbaCyberRequests();
			//set the company code
			requests.compCode = ((NbaTXLife) nbATxLife).getPrimaryHolding().getPolicy().getCarrierCode();

			//Do conversion of xmlDoc to text for DXE

			switch (getTransType(txLife)) {
			case HOLDING:
				request = requests.create203Request(txLife);
				break;
			case CHANGE_REQUIREMENT:
				request = requests.create109Request(txLife);
				break;
			case CHANGE_RATECLASS:
				request = requests.create109RateClassChangeRequest(txLife);
				break;
			case NEW_APP:
				NbaCyberNewAppAdapter newApp = new NbaCyberNewAppAdapter();
				request = newApp.create103Request( ((NbaTXLife) nbATxLife));	//SPR3573
				break;
			case FINAL_DISPOSITION:
				request = requests.create507Request(txLife);
				break;
			case APPROVE:
				request = requests.create505Request(txLife);
				break;
			case DENY_PERS_COV_BEN:
				request = requests.create503Request(txLife);
				break;
			case ENDORSEMENT:
				request = requests.create504EndorsementChangeRequest(txLife);
				break;
			case CWA:
				request = requests.create508CWAReverseRefundRequest(txLife);
				break;
			case SUBSTANDARD_RATING:
				request = requests.create151Request(txLife);
				break;
			default:
				throw new NbaBaseException("Invalid Transaction Type Requested");
			}
		} catch (NbaBaseException e) {
			throw new RuntimeException(e);
		}
		return request;
	}

	/**
	 * Parses DXE response from host and creates XML Transaction
	 * @param nbATxLife XML document Contains the transaction request for the host
	 * @param hostResponse host response DXE
	 * @return nbaTxlifeResponse XML reponse from the host
	 * @exception throws NbaBaseException and java.rmi.RemoteException
	 */
	//NBA195 new method
	public Object parseHostResponse(Object nbATxLife, String hostResponse) {
		if (hostResponse.substring(0, 2).compareTo(String.valueOf(NbaOliConstants.OLI_TC_NULL)) == 0) {
			throw new RuntimeException(new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_HOST_UNAVAILABLE + "  " + hostResponse));
		}
		NbaCyberParser cParser = new NbaCyberParser();
		NbaTXLife nbaTxlifeResponse = new NbaTXLife();
		TXLife txLife = ((NbaTXLife) nbATxLife).getTXLife();
		cParser.setHostResponse(hostResponse);
		try {
			//set the transtype that will be used while creating the response
			cParser.setTransType(getTransType(txLife));
			//create the XML response
			nbaTxlifeResponse.setTXLife(cParser.createXmlResponse(txLife));
		} catch (NbaBaseException e) {
			throw new RuntimeException(e);
		}
		return nbaTxlifeResponse;
	}

	/**
	 * Get the transaction type coming in for current request
	 * @param txLife Current txLife transaction request
	 * @return transType
	 * @exception throws NbaBaseException
	 */
	//NBA195 new method
	protected int getTransType(TXLife txLife) throws NbaBaseException {
		UserAuthRequestAndTXLifeRequest request = txLife.getUserAuthRequestAndTXLifeRequest();
		if (request == null) {
			throw new NbaBaseException("ERROR: Could not locate a UserAuthRequestAndTXLifeRequest object");
		}
		TXLifeRequest txlife = request.getTXLifeRequestAt(0);
		// Cast to an int in order to work with NbaCyberParser
		return ((int) txlife.getTransType());
	}
}
