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
 * *******************************************************************************<BR>
 */
//NBA093 code deleted
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaRequirementsData;
import com.csc.fsg.nba.tableaccess.NbaRolesParticipantData;
import com.csc.fsg.nba.tableaccess.NbaRolesRelationData;
import com.csc.fsg.nba.tableaccess.NbaStatesData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.tableaccess.NbaUctData;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.AltPremMode;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.AnnuityExtension;
import com.csc.fsg.nba.vo.txlife.AnnuityRiderExtension;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.CovOptionExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.EMailAddress;
import com.csc.fsg.nba.vo.txlife.Endorsement;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Investment;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Participant;
import com.csc.fsg.nba.vo.txlife.Party;
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
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.Rider;
import com.csc.fsg.nba.vo.txlife.Risk;
import com.csc.fsg.nba.vo.txlife.SubAccount;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;

/**
 * Parse the CyberLife host response and create an XML document to send back out.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>SPR1018</td><td>Version 2</td><td>General code clean-up</td><tr>
 * <tr><td>SPR1062</td><td>Version 2</td><td>The holding inquiry adapter should be changed to specify the Applies to Party as well as the Applies to Coverage tag for person level ratings.</td><tr>
 * <tr><td>NBA006</td><td>Version 2</td><td>Annuity support.</td><tr>
 * <tr><td>SPR1097</td><td>Version 2</td><td>Default Party.Address.AddressTypeCode to '1' (residence) if a null value is returned for address type (FNAADRTY) from CyberLife BES.</td><tr>
 * <tr><td>NBA012</td><td>Version 2</td><td>Contract Print Extract</td></tr>
 * <tr><td>NBA013</td><td>Version 2</td><td>Correspondence</td></tr>
 * <tr><td>NBA014</td><td>Version 2</td><td>Email</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>SPR1778</td><td>Version 4</td><td>Correct problems with SmokerStatus and RateClass in Automated Underwriting, and Validation.</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change etc</td></tr>
 * <tr><td>NBA111</td><td>Version 4</td><td>Joint Insured</td></tr>
 * <tr><td>SPR1986</td><td>Version 4</td><td>Remove CLIF Adapter Defaults for Pay up and Term date of Coverages and Covoptions</td></tr>
 * <tr><td>SPR2091</td><td>Version 4</td><td>SSN of third party/beneficiary is not interpreted correctly in holding response xml</td></tr>
 * <tr><td>SPR1549</td><td>Version 4</td><td>UFlat/Table Rating applied to Coverage is wrongly translated to Flat Permanent Rating</td></tr>
 * <tr><td>SPR2388</td><td>Version 5</td><td>CyberLife requirement code does not match due to leading 0s returned from host</td></tr>
 * <tr><td>SPR2365</td><td>Version 5</td><td>Auto Underwriting failing for the reason Smoker status Invalid.</td></tr>
 * <tr><td>SPR1524</td><td>Version 5</td><td>Annuity QualPlanType added to holding.</td></tr>
 * <tr><td>SPR2229</td><td>Version 5</td><td>Add contract level benefit indicator to holding inquiry.</td></tr>
 * <tr><td>SPR1538</td><td>Version 5</td><td>When an Annuity application is being submitted using Application Entry view, the replacement type value (FNBRPINS) is not being sent to host.</td></tr>
 * <tr><td>SPR1346</td><td>Version 5</td><td>Displaying State Drop-down list in Alphabetical order by country/ACORD State Code Change</td></tr>
 * <tr><td>SPR1163</td><td>Version 5</td><td>Adding Endorsements to an Annuity does not work/td></tr>
 * <tr><td>NBA122</td><td>Version 5</td><td>NBA Underwriter Workbench Rewrite Project</td></tr> 
 * <tr><td>SPR2816</td><td>Version 6</td><td>Contract Change Invokes Admin Holding for Wrappered Plans Instead of Pending Holding</td><tr>
 * <tr><td>NBA132</td><td>Version 6</td><td>Equitable Distribution of Work</td></tr>
 * <tr><td>SPR3063</td><td>Version 6</td><td>Underwriting Risk (APUWRSK) process error stops for Cyberlife cases in Wrappered mode</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>SPR3164</td><td>Version 6</td><td>The NBREQRMNT workitem went to NBERROR with reason related to Primary Writing agent address details</td></tr>
 * <tr><td>SPR3216</td><td>Version 6</td><td>RelationProducerExtension.SituationCode is not set from value returned in FAGSITCD</td></tr>
 * <tr><td>NBA151</td><td>Version 6</td><td>UL and VUL Application Entry Rewrite</td></tr>
 * <tr><td>NBA195</td><td>Version 7</td><td>JCA Adapter for DXE Interface to CyberLife</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>NBA211</td><td>Version 7</td><td>Partial Application project</td></tr>
 * <tr><td>SPR3171</td><td>Version 8</td><td>Requirement Restriction of 5 Needs to Be Added to Tables Schema</td></tr>
 * <tr><td>NBA234</td><td>Version 8</td><td>ACORD Transformation project</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaCyberParser implements NbaCyberConstants, NbaOliConstants, NbaTableAccessConstants {

	/*each of these arrays represent various data segments on CyberLife.
	Refer to NbaCyberConstants for the declarations.  Each array element represents
	a DXE value in Cyberlife**Refer to: XML Holding Inquiry.doc*/

	private String[] ADDR_FIELDS = { ADDR_PERS_SEQ_ID,
		//0
		ADDR_TYPE_CODE, //1
		ATTEN_LINE, //2
		ADDR_LINE1, //3
		ADDR_LINE2, //4
		CITY, //5
		STATE, //6
		ZIPCODE, //7
		COUNTRY, //8
		STARTDATE }; //9
	private String[] PRIMARY_FIELDS = { RESIDENCE_ST, //0
		RESIDENCE_CNTRY, //1
		RISK_IND }; //2
	private String[] PERSON_FIELDS = { PERS_SEQ_ID, //0
		FIRST_NAME, //1
		LAST_NAME, //2
		MIDDLE_NAME, //3
		PREFIX, //4
		SUFFIX, //5
		GENDER, //6
		BIRTH_DATE, //7
		BIRTH_STATE, //8
		SMOKE_STATUS, //9
		PARTY_TYPE_CODE, //10
		GOV_ID_TC, //11
		GOV_ID, //12
		HEIGHT_FT, //13
		HEIGHT_IN, //14
		WEIGHT, //15
		AGE, //16
		MAR_ST, //17
		OCCUPATION, //18
		PHONE, //19
		PHONE_ID, //20
		PHONE2, //21
		CORPNAME, //22
		COV_IND }; //23

	//LIFE PARTICIPANT
	private String[] LIFE_PAR_FIELDS = { PERS_SEQ_ID, //0
		COV_IND, //1
		LAST_NAME, //2
		FIRST_NAME, //3
		MIDDLE_NAME, //4
		GENDER, //5
		SMOKE_STATUS }; //6

	private String[] POLICY_FIELDS = { POL_NUM,
		//0
		COMP_CODE, //1
		PROD_TYPE, //2
		PROD_SUB_TYPE, //3
		ISSUE_RESTRICTION, //4
		COND_APPROVAL, //5
		FINAL_ACTION, //6
		JURISDICTION, //7
		REINSUR_IND, //8
		PAYMENT_MODE, //9
		PAYMENT_AMT, //10
		PAYMENT_METH, //11
		ASSIGN_IND, //12
		ENDORSE_IND, //13
		BEN_IND, //14
		RATE_IND, //15
		ADD_ALT_IND, //16
		SIGN_DATE, //17
		CWA_AMOUNT, //18
		UNDER_APPR, //19
		FINAL_ACTION, //20
		REQ_ISS_DATE, //21
		PROD_CODE, //22
		FINAL_DISP, //23
		OTHER_INS_IND, //24
		ALT_IND, //25
		AGENT_ERR_IND, //26
		ANN_PAYMENT_AMT, //27
		APPLICATION_TYPE, //28
		FACE_AMOUNT, //29
		APP_WRITTEN_STATE, //30
		LIST_BILL_NUM, //31 //NBA012
		BILLED_TO_DT, //32 //NBA012
		FRST_NOTICE_EXTR_DAY, //33 //NBA012
		ISSUE_COMPANY, //34 //NBA012
		ADMIN_COMPANY, //35 //NBA012
		TIMING_FOR_NOTICE, //36 //NBA012
		QUOTED_MODE_PREM, //37 //NBA012
		SF_QUOTED_PREM_BASIS_IND, //38 //NBA012
		APP_UNDERWRITER_ID, //39 //NBA012
		APP_DEPT_DESK_CLERK_ID, //40 //NBA012
		NFO_OPTION_A, //41 //NBA012
		CURRENT_INT_RATE, //42 //NBA012
		DIVIDEND_OPTION, //43 //NBA012
		ANNU_INIT_PREM, //44 //NBA012
		GUAR_INT_RATE, //45 //NBA012
		ALT_PREM_MODE_AN, //46 //NBA012
		ALT_PREM_MODE_SA, //47 //NBA012
		ALT_PREM_MODE_QT, //48 //NBA012
		ALT_PREM_MODE_MO,  //49 //NBA012 NBA122
		ASSIGNMENT_CODE }; //50 //NBA122
		
	//FINANCIAL ACTIVITY
	private String[] FIN_FIELDS = { FIN_GROSS_AMT,
		//0
		FIN_ACT_DATE, //1
		CWA_TYP_CODE, //2
		CWA_ERR_CODE, //3
		CWA_ENT_DATE, //4
		CWA_DPT_DESK, //5
		CWA_DISBURSED }; //6

	//ELECTRONIC COMMUNICATION
	private String[] ELECTRONIC_FIELDS = { ELECT_PERS, //0
		ELECT_PERSEQ, //1
		EMAIL_ADDRESS, //2
		ELECT_TYPE, //3
		EMAIL_TYPE }; //4

	//AGENT DATA
	private String[] AGENT_SUB_FIELDS = { WRITING_AGT, //0
		AGENT_LEVEL }; //1

	private String[] AGENT_FIELDS = { CARRIER_CODE, //0
		VOL_SHARE_PCT, //1
		SITUATION_CD, //2
		AGENT_NAME, //3
		AGENT_SUB_ORDER, //4
		COM_SHARE_PCT }; //5

	//Begin NBA013
	private String[] AGENT_PER_ADDR = { AGENT_FIRST_NAME, //0
		AGENT_MIDDLE_INITIAL, //1
		AGENT_LAST_NAME, //2
		AGENT_NAME_ADDENDUM, //3
		AGENT_ADDRESS_LINE1, //4
		AGENT_ADDRESS_LINE2, //5
		AGENT_ADDRESS_CITY, //6
		AGENT_ADDRESS_STATE, //7
		AGENT_ADDRESS_ZIP }; //8
	//End NBA013

	//Begin NBA013
	private String[] AGENT_BUS_ADDR = { AGENT_NAME_ADDENDUM, //0
		AGENCY_ADDRESS_LINE1, //1
		AGENCY_ADDRESS_LINE2, //2
		AGENCY_ADDRESS_CITY, //3
		AGENCY_ADDRESS_STATE, //4
		AGENCY_ADDRESS_ZIP }; //5
	//End NBA013

	private String[] RISK_FIELDS = { RISK_PERS_SEQ, //0
		DRIVER_LIC_NUM, //1
		DRIVER_LIC_ST }; //2
	private String[] ALT_PREM_MODE_FIELDS = { RISK_PERS_SEQ, //0
		DRIVER_LIC_NUM, //1
		DRIVER_LIC_ST }; //2

	private String[] MESSAGE_FIELDS = { MSG_REF_ID, //0
		MSG_START_DATE, //1
		MSG_CODE, //2
		MSG_DESC, //3
		MSG_SEVERITY, //4
		MSG_DETAIL, //5
		MSG_SEQ }; //6

	private String[] ENDORSE_FIELDS = { ENDORS_PERS_SEQ, //0
		APPLIES_TO_CONTRACT, //1
		APPLIES_TO_COV, //2
		APPLIES_TO_COVOPT, //3
		ENDORS_CODE, //4
		ENDORS_DETAILS, //5
		ENDORS_END_DATE }; //6
	private String[] REQ_FIELDS = { REQ_PERS_SEQ_ID, //0
		REQ_CODE, //1
		HO_REQ_REF_ID, //2
		REQ_STATUS, //3
		REQ_DATE, //4
		REQ_RESTRICTION, //5
		REQ_DEPT_DESK, //6
		REQ_OCCURRENCE }; //7
	private String[] COVERAGE_FIELDS = { COVERAGE_ID, //0
		COV_KEY, //1
		COV_PROD_CODE, //2
		COV_STATUS, //3
		COV_STATUS_2, //4
		COV_TYPE_CODE_1, //5
		COV_TYPE_CODE_2, //6
		COV_IND_CODE, //7
		COV_LIVES_TYPE, //8
		COV_CURR_AMT_1, //9
		COV_CURR_AMT_2, //10
		COV_ANN_PREM, //11
		COV_EFF_DATE, //12
		COV_TERM_DATE, //13
		COV_UNIT_TYPE, //14
		COV_ISSUE_AGE, //15
		DEATH_BENEFIT_OPT }; //16 //NBA012

	private String[] COV_OPT_FIELDS = { COV_OPT_BENE_PHASE, //0
		COV_OPT_TYPE, //1
		COV_OPT_ANN_PREM_AMT, //2
		COV_OPT_OPTION_AMT, //3
		COV_OPT_EFFDATE, //4
		COV_OPT_TERMDATE, //5
		COV_OPT_STATUS, //6
		COV_OPT_INV_IND, //7
		COV_OPT_PERC_TYPE, //8
		COV_OPT_PERC , //9
		COV_OPT_LIVES_TYPE, //10
		COV_OPT_PERSON_IDREF, //11 //NBA111 //SPR2229
		COV_OPT_ACTIVE_IND }; //12 //SPR2229
	private String[] SUB_STAND_FIELDS = { SUB_STAND_PERS_SEQ, //0
		SUB_STAND_COV_ID, //1
		SUB_STAND_RATE_TYPE, //2
		SUB_STAND_RATE_SUBTYPE, //3
		SUB_STAND_TABLE_RATE, //4
		SUB_STAND_PERCENT, //5
		SUB_STAND_FLAT_EXTRA, //6
		SUB_STAND_END_DATE, //7
		SUB_STAND_START_DATE, //8
		SUB_STAND_REASON, //9
		SUB_STAND_COMM, //10
		SUB_STAND_DURATION, //11
		SUB_STAND_EXTRA_UNIT, //12
		SUB_STAND_RATE_STATUS, //13
		SUB_STAND_ANN_PREM, //14
		SUB_STAND_FLAG,	//15 //SPR1549 
		SUB_STAND_TEMP_FLAG //16 //SPR1549		 
		}; //SPR1549
	private String[] ANNUITY_FIELDS = { COV_KEY, //0        
		COVERAGE_ID, //1
		COV_PROD_CODE, //2
		COV_CURR_AMT_1, //3
		COV_CURR_AMT_2, //4
		COV_IND_CODE, //5
		// begin NBA006
		COV_ANN_PREM, //6
		COV_UNIT_TYPE, //7
		COV_LIVES_TYPE, //8
		COV_ISSUE_AGE, //9
		COV_EFF_DATE, //10
		COV_TERM_DATE, //11
		COV_STATUS, //12
		COV_STATUS_2, //13
		COV_TYPE_CODE_1, //14
		COV_TYPE_CODE_2, //15
		// end NBA006
		ANNU_INIT_PREM, //16 // NBA012
		CURRENT_INT_RATE, //17 // NBA012
		GUAR_INT_RATE,//18 NBA012
		ANNU_QUAL_TYPE,//19 // SPR1524
		APP_REPLACEMENT_CD}; //20 //SPR1538 

	private String[] ANNUITY_COV_FIELDS = { COV_OPT_BENE_PHASE, //0        
		COV_OPT_TYPE, //1
		COV_OPT_OPTION_AMT, //2
		// begin NBA006
		COV_OPT_PERC_TYPE, //3
		COV_OPT_STATUS, //4
		COV_OPT_INV_IND, //5
		COV_OPT_ANN_PREM_AMT, //6
		COV_OPT_EFFDATE, //7
		COV_OPT_TERMDATE }; //8 // end NBA006

	//BENEFIT RELATIONS
	private String[] BENE_FIELDS = { BENE_ORIG_ID, //0
		BENE_SUB_COUNT }; //1

	//BENEFIT SUB REPEATERS
	private String[] BENE_SUB_FIELDS = { BENE_SUB_ID, //0        
		BENEF_REL_ROLE, //1
		PCTG_DIST_FOR_BENEF, //2
		BENE_INTEREST_AMT, //3
		BENEF_TYPE, //4
		DIST_TYPE }; //5
		
	private String[] SUBACCOUNT_FIELDS = {NbaCyberConstants.FUND_PRODUCT_CODE, //0
	        FUND_ALLOC_PERCENT}; //1

	protected String hostResponse = null;
	protected int transType = 0;
	private String compCode = null;
	private String planType = null;
	private NbaUctData[] genderTable = null;
	private NbaStatesData[] stateTable = null;
	private NbaUctData[] addressTypeTable = null;
	private NbaUctData[] prodTypeTable = null;
	private NbaUctData[] policyStatTable = null;
	// SPR3290 code deleted
	private NbaUctData[] policyModeTable = null;
	// SPR3290 code deleted
	private NbaUctData[] prefixTable = null;
	private NbaUctData[] suffixTable = null;
	private NbaUctData[] maritalStatTable = null;
	private NbaUctData[] occupationTable = null;
	//SPR1778 deleted code
	// SPR3290 code deleted
	private NbaUctData[] countriesTable = null;
	private NbaUctData[] emailTable = null;
	private NbaUctData[] paymentMethodTable = null;
	private NbaUctData[] underWritApprovalTable = null;
	private NbaUctData[] underWritStatusTable = null;
	private NbaUctData[] cwaTable = null;
	private NbaRequirementsData[] ReqCodeTable = null;
	private NbaUctData[] ReqStatusTable = null;
	private NbaUctData[] ReqRestrictionsTable = null;
	private NbaUctData[] covStatusTable = null;
	private NbaUctData[] covTypeCodeTable = null;
	private NbaUctData[] riderTypeCodeTable = null; // NBA006
	private NbaUctData[] covOptTable = null;
	private NbaRolesParticipantData[] roleCodesTable = null;
	private NbaRolesRelationData[] roleRelCodesTable = null;
	private NbaUctData[] endorseTable = null;
	private NbaUctData[] tableRatingTable = null;
	private NbaUctData[] ratingReasonTable = null;
	private NbaUctData[] ratingStatusTable = null;
	private NbaUctData[] ratingCommissionTable = null;
	private NbaUctData[] govIdTypeTable = null;
	private NbaUctData[] phoneTypeTable = null;
	private NbaUctData[] benDesignTable = null;
	private NbaUctData[] benTypeTable = null;
	private NbaUctData[] benDistTable = null;
	private NbaUctData[] prodSitCodeTable = null;
	private NbaUctData[] msgSeverityTable = null;
	private NbaUctData[] covIndCodeTable = null;
	private NbaUctData[] covLivesTable = null;
	private NbaUctData[] covOptStatusTable = null;
	// SPR3290 code deleted
	private NbaUctData[] appInfoTable = null;
	//begin NBA012
	private NbaUctData[] nfoOptionTable = null;
	private NbaUctData[] divTypeTable = null;
	private NbaUctData[] timingForNoticeTable = null;
	private NbaUctData[] deathBenefitOptionTypeTable = null;
	private NbaUctData[] altPremModeFreqTable = null;
	//end NBA012
	private NbaUctData[] replaceTypeTable = null;//SPR1538
	private NbaUctData[] qualPlanTypeTable = null;
	private NbaTableAccessor ntsAccess = null;

	//COUNTERS FOR XML TAG IDS

	private int coverageCount = 1;
	private int covOptionCount = 1;
	private int lifeParCount = 1;
	private int participantCount = 1; // NBA006
	private int addressCount = 1;
	private int phoneCount = 1;
	private int emailaddressCount = 1;

	private static String PARTY_HOST = "Party_host_"; //NBA211
	
	/**
	 * NbaCyberParser constructor
	 */
	public NbaCyberParser() {
		super();
		ntsAccess = new NbaTableAccessor();
	}
	/**
	 * Checks for duplicate people on a contract
	 * @param currentPerson The current person being used to create party.
	 * @param party The current party being created.
	 * @param addresses The address for the party
	 * @param electAddresses The electronic addresses for the party
	 * @return String duplicate PartyId or false if not duplicate
	 */
	//SPR2091 added parameters addresses and electAddresses. Added NbaBaseException
	private String checkForDuplicatePeople(String[] currentPerson, OLifE olife, int flag, String[][] addresses, String[][] electAddresses) throws NbaBaseException{

		//check for duplicate parties, for details on collapse criteria Refer to: XML Holding Inquiry.doc 

		int flag2 = 0;
		String duplicate = "false";
		String prefix = "";
		String suffix = "";

		while (flag2 < flag) {
			Party partyTemp = olife.getPartyAt(flag2);
			PersonOrOrganization personOrgTemp = partyTemp.getPersonOrOrganization();
			Person personTemp = personOrgTemp.getPerson();
			Organization organTemp = personOrgTemp.getOrganization();
			//Check to see if Person or Organization
			if ((currentPerson[10].compareToIgnoreCase(CORPORATION) == 0) || (currentPerson[10].compareToIgnoreCase(TRUST) == 0)) {
				if (organTemp != null) {
					if ((currentPerson[12].compareToIgnoreCase(partyTemp.getGovtID()) == 0)
						&& (currentPerson[22].compareToIgnoreCase(organTemp.getDBA()) == 0)) {
						duplicate = partyTemp.getId();
						break;
					}
				}
			} else {
				if (personTemp != null) {
					//need to translate prefix and suffix to cybervalue
					if (personTemp.getPrefix().length() != 0) {
						if (prefixTable == null) {
							prefixTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLIEXT_LU_PREFIX, compCode, "*"));
						}
						prefix = (findCyberCode(personTemp.getPrefix(), prefixTable));
					}
					if (personTemp.getSuffix().length() != 0) {
						if (suffixTable == null) {
							suffixTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLIEXT_LU_SUFFIX, compCode, "*"));
						}
						suffix = (findCyberCode(personTemp.getSuffix(), suffixTable));
					}

					if ((currentPerson[2].compareToIgnoreCase(personTemp.getLastName()) == 0)
						&& (currentPerson[1].compareToIgnoreCase(personTemp.getFirstName()) == 0)
						&& (currentPerson[3].substring(0).compareToIgnoreCase(personTemp.getMiddleName().substring(0)) == 0)
						&& (currentPerson[4].compareToIgnoreCase(prefix) == 0)
						&& (currentPerson[5].compareToIgnoreCase(suffix) == 0)) {
						duplicate = partyTemp.getId();
						partyTemp = combineDuplicateParties(currentPerson, personTemp, partyTemp, addresses, electAddresses); //SPR2091
						personOrgTemp.setPerson(personTemp);
						partyTemp.setPersonOrOrganization(personOrgTemp);
						olife.setPartyAt(partyTemp, flag2);
						break;
					}
				}
			}
			flag2++;
		} //end while
		return (duplicate);
	}
	/**
	 * Checks for insurable roles on a contract
	 * @param currentRole.
	 * @return true if insurable or false if not.
	 */
	private boolean checkForInsurableRole(String roleCode) {

		if ((roleCode.compareTo(PRIMARY_INS) == 0)
			|| (roleCode.compareTo(JOINT_INS) == 0)
			|| (roleCode.compareTo(SPOUSE) == 0)
			|| (roleCode.compareTo(DEPENDENT) == 0)
			|| (roleCode.compareTo(OTHER_INS) == 0)) {
			return (true);
		}
		return (false);
	}
	/**
	 * Checks for duplicate people on a contract
	 * @param currentPerson The current duplicate person.
	 * @param Person The person being compared.
	 * @param Party The party being compared.
	 * @param addresses The address for the party
	 * @param electAddresses The electronic addresses for the party
	 * @return Party Combined party data.
	 */
	//SPR2091 added parameters addresses and electAddresses. Added NbaBaseException
	private Party combineDuplicateParties(String[] currentPerson, Person personTemp, Party partyTemp, String[][] addresses, String[][] electAddresses) throws NbaBaseException{
		// SPR3290 code deleted

		//for details on collapse criteria Refer to: XML Holding Inquiry.doc

		if (personTemp.getGender() == -1) {

			if (genderTable == null) {
				genderTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_GENDER, compCode, "*"));
			}
			personTemp.setGender(findOLifeCode(currentPerson[6], genderTable));
		}
		if (personTemp.getBirthDate() == null) {
			personTemp.setBirthDate(formatOLifEDate(currentPerson[7]));
		}
		if (personTemp.getAge() == 0) {
			personTemp.setAge(currentPerson[16]);
		}

		if (personTemp.getBirthJurisdiction().length() == 0) {
			personTemp.setBirthJurisdiction(translateStateCode(currentPerson[8]));
		}
		if (personTemp.getOLifEExtensionAt(0) == null) {
			//SPR1778 deleted code
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_PERSON);
			PersonExtension personExt = new PersonExtension();
			personExt.setRateClass(currentPerson[9]); //SPR1778
			olifeExt.setPersonExtension(personExt);
			personTemp.addOLifEExtension(olifeExt);

		}
		if (partyTemp.getGovtIDTC() < 0) { //NBA093 SPR2091
			partyTemp.setGovtIDTC(currentPerson[11]); //NBA093
		}
		if (partyTemp.getGovtID().length() == 0) {
			partyTemp.setGovtID(currentPerson[12]); //NBA093 SPR2091
		}
		/*   if (personTemp.getHeight() == -1) {
		       if (currentPerson[13].length() != 0) {
		           int height = integer.parseInt(currentPerson[13]);
		           height = (height * 12) + (integer.parseInt(currentPerson[14]));
		           height = ((int) (height * 2.54));
		           personTemp.setHeight(height);
		           //convert inches to centimeters
		       }
		   }
		   if (personTemp.getWeight() == 0.0) {
		       //covert weight to kilograms
		       if (currentPerson[15].length() != 0) {
		           double weight = integer.parseInt(currentPerson[15]);
		           personTemp.setWeight(weight / 2.2);
		       }
		   }*/
		   //begin SPR2091
		   if (partyTemp.getPhoneCount() == 0) {
			   /*
				* Add a Home Phone Number.
				*/
			   if (currentPerson[19].length() != 0) {
				   Phone phone = new Phone();
				   phone.setPhoneTypeCode(OLI_PHONETYPE_HOME);
				   if (currentPerson[19].length() == 10) {
					   phone.setAreaCode(currentPerson[19].substring(0, 3));
					   phone.setDialNumber(currentPerson[19].substring(3));
				   } else {
					   phone.setDialNumber(currentPerson[19]);
				   }
				   partyTemp.addPhone(phone);
			   }
			   /*
				* Add a Second Phone Number if present.
				*/
			   if (currentPerson[21].length() != 0) {
				   Phone phone2 = new Phone();
				   if (phoneTypeTable == null) {
					   phoneTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_PHONETYPE, compCode, "*"));
				   }
				   phone2.setPhoneTypeCode(findOLifeCode(currentPerson[20], phoneTypeTable));
				   if (currentPerson[21].length() == 10) {
					   phone2.setAreaCode(currentPerson[21].substring(0, 3));
					   phone2.setDialNumber(currentPerson[21].substring(3));
				   } else {
					   phone2.setDialNumber(currentPerson[21]);
				   }
				   partyTemp.addPhone(phone2);
			   }
		   }

		   /*
		   * Add Addresses.
		   */
		   if (partyTemp.getAddressCount() == 0) {
			   partyTemp = createAddresses(currentPerson, addresses, partyTemp);
		   }

		   /*
			* Add Email Addresses.
			*/
		   if (partyTemp.getEMailAddressCount() == 0) {
			   partyTemp = createEmailAddresses(currentPerson, electAddresses, partyTemp);
		   }
		   //end SPR2091
		return (partyTemp);
	}
	/**
	 * Creates the addresses for current party.
	 * @param currentPerson The current person being used to create party.
	 * @param Addresses The addresses returned on contract.
	 * @param party The current party being created.
	 * @return Party party
	 * @exception throws NbaBaseException.
	 */
	private Party createAddresses(String[] currentPerson, String[][] Addresses, Party party) throws NbaBaseException {

		/*
		 * Add Addresses.
		 */
		try {
			int flag2 = 0, flag3 = 0;
			boolean duplicate = false;
			// SPR3290 code deleted
			Address tempAddress = new Address();
			if (Addresses != null) {
				while (flag2 < (Addresses.length)) {
					if ((Addresses[flag2][0].compareTo(currentPerson[0])) == 0) {
						//check for duplicate address
						flag3 = 0;
						while (flag3 < party.getAddressCount()) {
							tempAddress = party.getAddressAt(flag3);
							if ((tempAddress.getAddressStateTC() //NBA093
							== Integer.parseInt(translateStateCode(Addresses[flag2][6])))	//NBA093
								&& (tempAddress.getLine1().compareToIgnoreCase(Addresses[flag2][3]) == 0)
								&& (tempAddress.getCity().compareToIgnoreCase(Addresses[flag2][5]) == 0)
								&& (tempAddress.getZip().compareToIgnoreCase(Addresses[flag2][7]) == 0)) {
								duplicate = true;
								break;
							}
							flag3++;
						}
						if (duplicate != true) {
							Address address = new Address();
							if (address == null) {
								throw new NbaBaseException("ERROR: Could not create an Address object.");
							}
							address.setId("Address_" + Integer.toString(addressCount));	//NBA093
							addressCount++;
							if (addressTypeTable == null) {
								addressTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_ADTYPE, compCode, "*"));
							}

							if (Addresses[flag2][1].length() == 0) { //SPR1097
								address.setAddressTypeCode(OLI_ADTYPE_HOME);
							} //SPR1097
							else { //SPR1097
								for (int i = 0; i < addressTypeTable.length; i++) {
									if (addressTypeTable[i].besValue.compareTo(Addresses[flag2][1]) == 0) {

										if (addressTypeTable[i].code() == null) {
											address.setAddressTypeCode(OLI_ADTYPE_HOME);
										} else {
											address.setAddressTypeCode(addressTypeTable[i].code());
										}
										break;
									}
								}
							}
							address.setAttentionLine(Addresses[flag2][2]);
							address.setLine1(Addresses[flag2][3]);
							address.setLine2(Addresses[flag2][4]);
							address.setCity(Addresses[flag2][5]);
							address.setAddressStateTC(translateStateCode(Addresses[flag2][6])); //NBA093
							Addresses[flag2][7] = formatZipCode(Addresses[flag2][7]);
							address.setZip(Addresses[flag2][7]);
							address.setStartDate((formatOLifEDate(Addresses[flag2][9])));
							if (countriesTable == null) {
								countriesTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_NATION, compCode, "*"));
							}
							address.setAddressCountryTC(findOLifeCode(Addresses[flag2][8], countriesTable)); //NBA093
							party.addAddress(address);

						} // end if
					} //end if duplicate
					flag2++;
					duplicate = false;
				} //end while
			} //end if
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_ADDRESSES, e);
		}
		return (party);
	}
	/**
	 * Creates the Agent parties and relations.
	 * @param olife The current OLifE transaction.
	 * @param holding The current Holding transaction.
	 * @return OLifE olife
	 * @exception throws NbaBaseException.
	 */
	private OLifE createAgentPartyAndRelations(OLifE olife, Holding holding) throws NbaBaseException {

		/*Multiple agent segments, each possibly containing multiple 
		agent levels (subrepeating data), could be returned from CyberLife.  
		Agent name, share percent, share volume, situation code and agency 
		are in the non repeating portion of the 48 segment.  The agent ID 
		is in the subrepeating portion of the segment.  The writing 
		agent ID (agent level (FAGAGTLV) = "01") will be the only agent ID 
		stored in the xml stream.  All other agents in the hierarchy 
		(agent levels = > 02) will be ignored.  DXE FAGNOLVL indicates 
		the number of  agent levels contained in the subrepreating section of the 
		48 segment. Refer to: XML Holding Inquiry.doc*/

		try {
		    //NBA132 deleted code
			int numInstances = getNumInstances(AGENT_COUNT);
			String[][] agentData = getData(numInstances, AGENT_FIELDS);
			numInstances = getNumInstances(AGENT_SUB_COUNT);
			String[][] agentSubData = getData(numInstances, AGENT_SUB_FIELDS);
			String[] agencyName = { AGENCY_NAME, AGENCY_EMAIL_ADDRESS }; //NBA014
			String[][] agencyNameData = getData(1, agencyName);
			String[] agentEmailArray = { AGENT_EMAIL_ADDRESS }; //NBA014
			String[][] agentEmailData = getData(1, agentEmailArray); //NBA014
			String[][] agentPerAddrData = getData(1, AGENT_PER_ADDR); //NBA013
			String[][] agentBusAddrData = getData(1, AGENT_BUS_ADDR); //NBA013
			ArrayList emailArrayList = new ArrayList(); //NBA014
			ArrayList addressArrayList = new ArrayList(); //NBA013

			int flag = 0, flag2 = 0, subNum = 0, totalSubNum = 0;
			// SPR3290 code deleted
			/*
			* Create the Party and Relation for the Writing Agent.
			*/
			if (agentData != null) {
				while (flag < agentData.length) {
					Party party = new Party();
					Person person = new Person();
					Producer producer = new Producer();
					CarrierAppointment carrAppt = new CarrierAppointment();
					EMailAddress agentEmail = new EMailAddress(); //NBA014
					Address agentPersonalAddress = new Address(); //NBA013
					Address agentBusinessAddress = new Address(); //NBA013

					party.setId(PARTY_HOST + (olife.getPartyCount() + 1)); //NBA132 NBA211
					party.setPartyTypeCode(1);

					//Begin NBA013
					if (flag == 0) {
						person.setFirstName(agentPerAddrData[flag][0]);
						person.setMiddleName(agentPerAddrData[flag][1]);
						person.setLastName(agentPerAddrData[flag][2]);
					} else {
						person.setLastName(agentData[flag][3]);
					}
					//End NBA013

					PersonOrOrganization personOr = new PersonOrOrganization();
					personOr.setPerson(person);
					party.setPersonOrOrganization(personOr);
					carrAppt.setPartyID(party.getId());
					carrAppt.setCarrierName(agentData[flag][0]);

					while (flag2 < Integer.parseInt(agentData[flag][4])) {	//NBA093
						if (agentSubData[subNum][1].compareTo("01") == 0) {
							carrAppt.setCompanyProducerID(agentSubData[subNum][0]);
							flag2 = 0;
							break;
						}
						flag2++;
						subNum = subNum + 1;
					}
					totalSubNum = totalSubNum + Integer.parseInt(agentData[flag][4]);	//NBA093
					subNum = totalSubNum;

					producer.addCarrierAppointment(carrAppt);
					party.setProducer(producer);

					// begin NBA013, NBA014
					if (flag == 0) {
						// set Party.EmailAddress for Primary Writing Agent
						if (!agentEmailData[flag][0].equals("")) {
							agentEmail.setId("EmailAddress_" + emailaddressCount++);
							agentEmail.setEMailType(1);
							agentEmail.setAddrLine(agentEmailData[flag][0]);
							emailArrayList.add(0, agentEmail);
							party.setEMailAddress(emailArrayList);
						}

						// set Party.Address personal address for Primary Writing Agent
						if (agentPerAddrData[flag][4].length() > 0){ //SPR3164
							agentPersonalAddress.setId("Address_" + addressCount++);
							agentPersonalAddress.setAddressTypeCode(OLI_ADTYPE_HOME); //SPR1018
							agentPersonalAddress.setAttentionLine(agentPerAddrData[flag][3]);
							agentPersonalAddress.setLine1(agentPerAddrData[flag][4]);
							agentPersonalAddress.setLine2(agentPerAddrData[flag][5]);
							agentPersonalAddress.setCity(agentPerAddrData[flag][6]);
							agentPersonalAddress.setAddressStateTC(translateStateCode(agentPerAddrData[flag][7])); //NBA093
							agentPersonalAddress.setZip(agentPerAddrData[flag][8]);
							addressArrayList.add(0, agentPersonalAddress);
						}//SPR3164

						// set Party.Address business address for Primary Writing Agent
						if (agentBusAddrData[flag][1].length() > 0){ //SPR3164
							agentBusinessAddress.setId("Address_" + addressCount++);
							agentBusinessAddress.setAddressTypeCode(OLI_ADTYPE_BUS); //SPR1018
							agentBusinessAddress.setAttentionLine(agentBusAddrData[flag][0]);
							agentBusinessAddress.setLine1(agentBusAddrData[flag][1]);
							agentBusinessAddress.setLine2(agentBusAddrData[flag][2]);
							agentBusinessAddress.setCity(agentBusAddrData[flag][3]);
							agentBusinessAddress.setAddressStateTC(translateStateCode(agentBusAddrData[flag][4])); //NBA093
							agentBusinessAddress.setZip(agentBusAddrData[flag][5]);
							addressArrayList.add(1, agentBusinessAddress);
						} //SPR3164
						party.setAddress(addressArrayList);

					}
					// end NBA013, NBA014

					Relation relation = new Relation();
					OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_RELATIONPRODUCER);
					RelationProducerExtension prodExt = new RelationProducerExtension();
					relation.setVolumeSharePct(agentData[flag][1]); //NBA093
					if (prodSitCodeTable == null) {
						prodSitCodeTable = ((NbaUctData[]) getACFUctTable(NbaTableConstants.OLIEXT_LU_SITCODE, compCode, "*"));	//SPR3216
					}
					prodExt.setSituationCode(findOLifeCode(agentData[flag][2], prodSitCodeTable));

					olifeExt.setRelationProducerExtension(prodExt);
					relation.addOLifEExtension(olifeExt);
					relation.setId("Relation_" + (olife.getRelationCount() + 1)); //NBA132
					relation.setOriginatingObjectID(holding.getId());
					relation.setRelatedObjectID(party.getId());
					relation.setRelationRoleCode(OLI_REL_PRIMAGENT); //SPR1018
					relation.setOriginatingObjectType(OLI_HOLDING); //SPR1018
					relation.setRelatedObjectType(OLI_PARTY); //SPR1018
					relation.setInterestPercent(agentData[flag][5]);
					olife.addParty(party);
					olife.addRelation(relation);
					//NBA132 deleted code
					olife = createPrimaryWritingAgentAgency(olife, party.getId(), agentSubData); //NBA132

					flag++;
				} //end while
			}

			/*The Servicing agency is retrieved in DXE FBRAGNCY.  A party object 
			will be created for the servicing agency with FBRAGNCY 
			populated into Party.Person.Lastname.  A relation object 
			will be created for this party with the relation role code of 
			145 - Servicing Agency. Refer to: XML Holding Inquiry.doc*/

			/*
			* Create the Party and Relation for the Servicing Agency.
			*/
			if (agencyNameData != null) {
				Party party = new Party();
				Person person = new Person();
				party.setId(PARTY_HOST + (olife.getPartyCount() + 1)); //NBA211
				party.setPartyTypeCode(OLIX_PARTYTYPE_INDIVIDUAL); //SPR1018
				person.setLastName(agencyNameData[0][0]);
				PersonOrOrganization personOr = new PersonOrOrganization();
				personOr.setPerson(person);
				party.setPersonOrOrganization(personOr);

				//Begin NBA014
				if (!agencyNameData[0][1].equals("")) {
					EMailAddress agencyEmail = new EMailAddress();
					agencyEmail.setId("EmailAddress_" + emailaddressCount++);
					agencyEmail.setEMailType(OLI_EMAIL_BUSINESS); //SPR1018
					agencyEmail.setAddrLine(agencyNameData[0][1]);
					emailArrayList.clear();
					emailArrayList.add(0, agencyEmail);
					party.setEMailAddress(emailArrayList);
				}
				//End NBA014

				olife.addParty(party);
				//add relation
				Relation relation = new Relation();
				relation.setId("Relation_" + (olife.getRelationCount() + 1));
				relation.setOriginatingObjectID(holding.getId());
				relation.setRelatedObjectID(party.getId());
				relation.setRelationRoleCode(OLI_REL_SERVAGENCY); //SPR1018
				relation.setOriginatingObjectType(OLI_HOLDING); //SPR1018
				relation.setRelatedObjectType(OLI_PARTY); //SPR1018
				olife.addRelation(relation);
			}
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_AGENTDATA, e);
		}
		return (olife);
	}
	/**
	 * Creates the primary writing agent's agency party object.  It is then related to the primary
	 * agent party object with a relation code of '121'.  The agency is determined by the highest
	 * agent level returned.
	 * @param olife
	 * @param agentId
	 * @param agentSubData
	 * @return
	 */
	//NBA132 New Method
	protected OLifE createPrimaryWritingAgentAgency(OLifE olife, String agentId, String[][] agentSubData) {
	    String id = null;
	    String level = "";
	    //find the id with the highest level
	    int count = agentSubData.length;
	    for (int i = 0; i < count; i++) {
	        if (level.compareTo(agentSubData[i][1]) < 0) {
	            id = agentSubData[i][0];
	            level = agentSubData[i][1];
	        }
	    }
	    //create a party for the agency and relate it to the primary writing agent
	    if (id != null) {
			Party party = new Party();
			party.setId(PARTY_HOST + (olife.getPartyCount() + 1)); //NBA211
			party.setProducer(new Producer());
			CarrierAppointment carrAppt = new CarrierAppointment();
			carrAppt.setCompanyProducerID(id);
			party.getProducer().addCarrierAppointment(carrAppt);
			olife.addParty(party);
			
			Relation relation = new Relation();
			relation.setId("Relation_" + (olife.getRelationCount() + 1));
			relation.setOriginatingObjectID(agentId);
			relation.setRelatedObjectID(party.getId());
			relation.setRelationRoleCode(OLI_REL_AGENCYOF);
			relation.setOriginatingObjectType(OLI_PARTY);
			relation.setRelatedObjectType(OLI_PARTY);
			olife.addRelation(relation);
	    }

	    return olife;
	}
	/**
	 * Creates the Alternate Premium Mode object.
	 * @param policy The policy response for contract.
	 * @param policyData The policy information returned from host.
	 * @return Policy policy
	 * @exception throws NbaBaseException.
	 */
	//NBA012 new method
	private Policy createAltPremMode(Policy policy, String[] policyData) throws NbaBaseException {

		int count = 1;
		if (altPremModeFreqTable == null) {
			altPremModeFreqTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_PAYMODE, compCode, "*"));
		}
		AltPremMode altPremMode = null;

		if (policyData[46].length() > 0) {
			altPremMode = new AltPremMode();
			altPremMode.setId("AltPremMode_" + count);
			altPremMode.setPaymentMode("1");
			altPremMode.setPaymentAmt(policyData[46]);
			policy.addAltPremMode(altPremMode);
			policy.setAltPremModeAt(altPremMode, count - 1);
			count++;
		}

		if (policyData[47].length() > 0) {
			altPremMode = new AltPremMode();
			altPremMode.setId("AltPremMode_" + count);
			altPremMode.setPaymentMode("2");
			altPremMode.setPaymentAmt(policyData[47]);
			policy.addAltPremMode(altPremMode);
			policy.setAltPremModeAt(altPremMode, count - 1);
			count++;
		}

		if (policyData[48].length() > 0) {
			altPremMode = new AltPremMode();
			altPremMode.setId("AltPremMode_" + count);
			altPremMode.setPaymentMode("3");
			altPremMode.setPaymentAmt(policyData[48]);
			policy.addAltPremMode(altPremMode);
			policy.setAltPremModeAt(altPremMode, count - 1);
			count++;
		}

		if (policyData[49].length() > 0) {
			altPremMode = new AltPremMode();
			altPremMode.setId("AltPremMode_" + count);
			altPremMode.setPaymentMode("4");
			altPremMode.setPaymentAmt(policyData[49]);
			policy.addAltPremMode(altPremMode);
			policy.setAltPremModeAt(altPremMode, count - 1);
			count++;
		}

		return (policy);
	}
	/**
	 * Create an Annuity version of the holding inquiry.
	 * @param annuity the current annuity object being created
	 * @param olife the current olife information
	 * @param policy the current policy information
	 * @return the new Annuity object populated from the holding inquiry
	 */
	// NBA093 Added new parameter policy 
	private Annuity createAnnuity(Annuity annuity, OLifE olife, Policy policy) throws NbaBaseException {
		//GET THE ANNUITY DATA
		try {
			int numCov = getNumInstances(COV_COUNT);
			String[][] annuityData = getData(numCov, ANNUITY_FIELDS); // SPR1018
			int numCovOpt = getNumInstances(COV_OPT_COUNT);
			String[][] annCovOptData = getData(numCovOpt, ANNUITY_COV_FIELDS);
			// SPR3290 code deleted
			String[] currentData = new String[numCov];
			// SPR3290 code deleted
			//NBA093 code deleted
			int flag = 0;
			//get base policy information
			while (flag < numCov) {
				currentData = annuityData[flag]; // SPR1018
				if (currentData[5].compareTo("1") == 0) {
					annuity.setAnnuityKey(currentData[0]);
					//begin NBA012            
					annuity.setInitPaymentAmt(currentData[16]); //NBA012
					annuity.setInitDepIntRateCurrent(currentData[17]);
					annuity.setGuarIntRate(currentData[18]);
					//end NBA012
					//begin SPR1524
					if(qualPlanTypeTable == null){
						qualPlanTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_QUALPLAN, compCode, "*"));
					}
					annuity.setQualPlanType(findOLifeCode(currentData[19], qualPlanTypeTable));
					//End SPR1524
					//add the Payout Participant
					// NBA006 code deleted
					annuity.addPayout(createParticipant(new Payout(), currentData, olife)); // NBA006
					//check for CovOptions on the base policy
					OLifEExtension olifeAnnExt = NbaTXLife.createOLifEExtension(EXTCODE_ANNUITY);
					AnnuityExtension annuityExtension = new AnnuityExtension(); // SPR1018
					// begin NBA006
					// The annuity extension total amount is mapped to three variables: FCVFACE, FCVUNITS, and FCVFLGB0.
					// The adapter will populate Holding.Annuity.AnnuityExtension.TotAmt with the return value in FCVUNITS if FCVFLGB0 is "1"; 
					// otherwise the return value from FCVFACE will be used.
					// Refer to: XML Holding Inquiry.doc
					if (currentData[7].compareTo("1") == 0) {
						annuityExtension.setTotAmt(currentData[4]);
					} else {
						annuityExtension.setTotAmt(currentData[3]);
					}
					annuityExtension.setUnitTypeInd(currentData[7]);
					annuityExtension.setLivesType(currentData[8]);
					policy.setEffDate(currentData[10]); //NBA093
					policy.setTermDate(currentData[11]);	//SPR1986
					if (covStatusTable == null) {
						covStatusTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_COVERAGE_STATUS, compCode, "*"));
					}
					annuityExtension.setAnnuityStatus(findOLifeCode((currentData[12] + currentData[13]), covStatusTable)); //NBA234
					
					//begin SPR1538
					if (replaceTypeTable == null) {
						replaceTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_REPLACETYPE, compCode, "*"));
					}
					policy.setReplacementType(findOLifeCode(currentData[20], replaceTypeTable));
					//end SPR1538
					//NBA093 - Deleted Code
					olifeAnnExt.setAnnuityExtension(annuityExtension); // SPR1018
					annuity.addOLifEExtension(olifeAnnExt);
				}
				// end NBA006
				flag++;
			}
			//Set Rider information
			flag = 0;
			while (flag < numCov) {
				currentData = annuityData[flag]; // SPR1018
				if (currentData[5].compareTo("1") != 0) {
					Rider rider = new Rider();
					currentData = annuityData[flag]; // SPR1018
					rider.setId("Rider_" + Integer.toString(annuity.getRiderCount() + 1));	//NBA093
					rider.setRiderKey(currentData[0]);
					rider.setRiderCode(currentData[2]);
					rider.setTotAmt(currentData[3]);
					// begin NBA006
					if (covStatusTable == null) {
						covStatusTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_COVERAGE_STATUS, compCode, "*"));
					}
					rider.setRiderStatus(findOLifeCode(currentData[12] + currentData[13], covStatusTable));
					if (riderTypeCodeTable == null) {
						riderTypeCodeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_RIDERTYPE, compCode, "*"));
					}
					rider.setRiderTypeCode(findOLifeCode(currentData[14] + currentData[15], riderTypeCodeTable));
					rider.setEffDate(currentData[10]);
					rider.setTermDate(currentData[11]);
					createParticipant(rider, currentData, olife);  //NBA093
					// end NBA006
					//add the Annuity Rider Extension
					OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_ANNUITYRIDER);
					AnnuityRiderExtension annuityRiderExtension = olifeExt.getAnnuityRiderExtension(); //NBA093
					// NBA006 code deleted
					// begin NBA006
					annuityRiderExtension.setAnnualPremAmt(currentData[6]);
					annuityRiderExtension.setUnitTypeInd(currentData[7]);
					annuityRiderExtension.setLivesType(currentData[8]);
					//NBA093 deleted line
					//check for coverage options on the Riders
					if (annCovOptData != null) {
						for (int i = 0; i < annCovOptData.length; i++) {
							String[] annuityCovOptionFields = annCovOptData[i];
							if (annuityCovOptionFields[0].compareTo(currentData[0]) == 0) {
								rider.addCovOption(createAnnuityCovOption(annuityCovOptionFields)); // SPR1018  NBA093
							}
						}
					}
					// end NBA006
					// NBA093 deleted line
					rider.addOLifEExtension(olifeExt);
					annuity.addRider(rider);
				}
				flag++;
			}
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_ANNUITY, e);
		}
		return (annuity);
	}
	/**
	 * Create the CovOption response.
	 * @param currentData the covOption information returned from host
	 * @return com.csc.fsg.nba.vo.txlife.CovOption
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	// NBA006 New Method
	private CovOption createAnnuityCovOption(String[] currentData) throws NbaBaseException {
		CovOption covOption = new CovOption();
		if (covOption == null) {
			throw new NbaBaseException("ERROR: Could not create a CovOption object.");
		}
		covOption.setId("CovOption_" + Integer.toString(covOptionCount));
		covOptionCount++;
		covOption.setCovOptionKey(currentData[0]);
		if (covOptTable == null) {
			covOptTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_OPTTYPE, compCode, "*"));
		}
		covOption.setLifeCovOptTypeCode(findOLifeCode(currentData[1], covOptTable));
		covOption.setOptionAmt(currentData[2]);
		covOption.setAnnualPremAmt(currentData[6]);
		covOption.setEffDate(currentData[7]);
		covOption.setTermDate(currentData[8]);
		OLifEExtension olifeExtensionCovOption = NbaTXLife.createOLifEExtension(EXTCODE_COVOPTION);
		CovOptionExtension covOptionExtension = new CovOptionExtension();
		covOption.setCovOptionPctInd(currentData[3]); //NBA093
		//begin NBA077
		String benStat = "";
		if (currentData[4].equalsIgnoreCase(FLAG_BIT_ON)) { //FSBFLGB7
			benStat = "1";	//1 = BES for Denied
		} else if (currentData[5].equalsIgnoreCase(FLAG_BIT_ON)) { //FSBFLGA7
			benStat = "2";	//2 = BES for Invalid
		}
		if (benStat.length() > 0) {
			if (covOptStatusTable == null) {
				covOptStatusTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_BENEFIT_STATUS, compCode, "*"));
			}
			covOption.setCovOptionStatus(findOLifeCode(benStat, covOptStatusTable));
			covOptionExtension.setInvalidCovOptionIndicator(benStat.equals("2") ? true : false);  
		}
		//end NBA077
		olifeExtensionCovOption.setCovOptionExtension(covOptionExtension);
		covOption.addOLifEExtension(olifeExtensionCovOption);
		return covOption;
	}
	/**
	 * Creates the ApplicationInfo response.
	 * @param policy The policy response for contract.
	 * @param policyData The policy information returned from host.
	 * @return Policy policy
	 * @exception throws NbaBaseException.
	 */
	private Policy createApplicationInfo(Policy policy, String[] policyData) throws NbaBaseException {

		ApplicationInfo appInfo = new ApplicationInfo();
		appInfo.setSignedDate(formatOLifEDate(policyData[17]));
		appInfo.setCWAAmt(policyData[18]);
		appInfo.setApplicationJurisdiction(translateStateCode(policyData[30]));

		//begin NBA012
		appInfo.setHOUnderwriterName(policyData[39]);
		appInfo.setNBContactName(policyData[40]);
		//end NBA012

		OLifEExtension AppolifeExt = NbaTXLife.createOLifEExtension(EXTCODE_APPLICATIONINFO);
		ApplicationInfoExtension appExt = new ApplicationInfoExtension();
		if (underWritApprovalTable == null) {
			underWritApprovalTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLIEXT_LU_UNDAPPROVAL, compCode, "*"));
		}
		if ((policyData[19].compareTo("1") == 0) && (policyData[20].compareTo("0") == 0)) {
			appExt.setUnderwritingApproval(findOLifeCode("0", underWritApprovalTable));
		}
		if (policyData[20].compareTo("1") == 0) {
			appExt.setUnderwritingApproval(findOLifeCode("1", underWritApprovalTable));
		}

		if (underWritStatusTable == null) {
			underWritStatusTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_FINAL_DISPOSITION, compCode, "*"));
		}
		appExt.setUnderwritingStatus(findOLifeCode(policyData[23], underWritStatusTable));

		if (appInfoTable == null) {
			appInfoTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_APPTYPE, compCode, "*"));//NBA093
		}
		appInfo.setApplicationType(findOLifeCode(policyData[28], appInfoTable)); //NBA093

		appInfo.setRequestedPolDate(formatOLifEDate(policyData[21])); //NBA093

		appInfo.setAdditionalInd(policyData[16]); //NBA093
		appInfo.setAlternateInd(policyData[25]); //NBA093

		AppolifeExt.setApplicationInfoExtension(appExt);
		appInfo.addOLifEExtension(AppolifeExt);
		policy.setApplicationInfo(appInfo);
		return (policy);
	}
	/**
	 * Creates the Benefit relations.
	 * @param olife The current OLifE transaction.
	 * @return OLifE olife
	 * @exception throws NbaBaseException.
	 */
	private OLifE createBenefitRelations(OLifE olife) throws NbaBaseException {

		/*The beneficiary (96) segment is a repeating segment.  
		Each beneficiary segment has a sub repeating portion 
		which contains the beneficiary data related to the person code 
		and sequence on the non repeating portion of the segment.   
		FBDNOENT indicates the number of repeating items in the sub repeating portion of the 
		segment. Refer to AppendixA.doc section 6. */

		try {
			int relationCount = olife.getRelationCount();
			int numInstances = getNumInstances(BENE_COUNT);
			Relation relation; //SPR2816
			if (numInstances != 0) {
				String[][] beneData = getData(numInstances, BENE_FIELDS);
				numInstances = getNumInstances(BENE_SUB_REP_COUNT);
				String[][] beneSubData = getData(numInstances, BENE_SUB_FIELDS);

				int flag = 0, flag2 = 0, subNum = 0, totalSubNum = 0;
				// SPR3290 code deleted
				Relation tempRelation; //SPR2816
				
				if (beneData != null) {

					while (flag < beneData.length) {
						flag2 = 0;
						while (flag2 < Integer.parseInt(beneData[flag][1])) {	//NBA093

							relation = new Relation(); //SPR2816
							OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_RELATION);
							RelationExtension relExt = new RelationExtension();

							relExt.setInterestAmount(beneSubData[subNum][3]);
							if (benTypeTable == null) {
								benTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_BENEFICIARY_TYPE, compCode, "*"));
							}
							//NBA093 Deleted code
							if (benDistTable == null) {
								benDistTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_DISTOPTION, compCode, "*"));
							}
							relExt.setBeneficiaryDistributionOption(findOLifeCode(beneSubData[subNum][5], benDistTable));

							olifeExt.setRelationExtension(relExt);
							relation.addOLifEExtension(olifeExt);

							relation.setId("Relation_" + (relationCount + 1));
							//get Originating Party
							int flag3 = 0;
							while (flag3 < relationCount) {
								tempRelation = olife.getRelationAt(flag3);
								if (tempRelation.getRelationKey() != null) {
									if (tempRelation.getOriginatingObjectID().compareTo("Holding") == 0) {
										if (tempRelation.getRelationKey().compareTo(beneData[flag][0]) == 0) {
											relation.setOriginatingObjectID(tempRelation.getRelatedObjectID());
										}
										if (tempRelation.getRelationKey().compareTo(beneSubData[subNum][0]) == 0) {
											relation.setRelatedObjectID(tempRelation.getRelatedObjectID());
										}
									}
								}
								flag3++;
							}
							if (roleRelCodesTable == null) {
								//begin NBA006
								if (planType.substring(0, 1).compareTo("F") == 0) {
									if (prodTypeTable == null) {
										prodTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_POLPROD, compCode, "*"));
									}
									String prodType = findOLifeCode(planType, prodTypeTable);
									//end NBA006
									roleRelCodesTable =
										((NbaRolesRelationData[]) getRolesTable(NbaTableConstants.NBA_ROLES_RELATION, compCode, prodType));
									//NBA006
								} else {
									roleRelCodesTable = ((NbaRolesRelationData[]) getRolesTable(NbaTableConstants.NBA_ROLES_RELATION, compCode, "*"));
								}
							}

							relation.setRelationRoleCode(findOLifeCode(beneSubData[subNum][0].substring(0, 2), roleRelCodesTable));
							relation.setRelatedRefID(beneSubData[subNum][0].substring(2, 4));
							relation.setOriginatingObjectType(OLI_PARTY); //SPR1018
							relation.setRelatedObjectType(OLI_PARTY); //SPR1018
							if (benDesignTable == null) {
								benDesignTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_BENEDESIGNATION, compCode, "*"));
							}
							relation.setBeneficiaryDesignation(findOLifeCode(beneSubData[subNum][1], benDesignTable));
							//begin NBA151
							if (relExt.getBeneficiaryDistributionOption() == OLI_DISTOPTION_BALANCE) {
								relation.setInterestPercent(100.0);
							} else if (relExt.getBeneficiaryDistributionOption() == OLI_DISTOPTION_PERCENT) {
								relation.setInterestPercent(beneSubData[subNum][2]);
							}
							//end NBA151
							relation.setRelationKey(beneSubData[subNum][0]);
							olife.addRelation(relation);

							relationCount++;
							flag2++;
							subNum++;

						}

						totalSubNum = totalSubNum + Integer.parseInt(beneData[flag][1]);	//NBA093
						subNum = totalSubNum;
						flag++;
					} //end while
				}
			}
			//begin SPR2816
			int count = olife.getRelationCount();
			//Remove the Party to Holding beneficiary Relations. They are only needed as temporary placeholders for the
			//RelatedObjectID values for the Party to Party beneficiary Relation. See tempRelation.getRelatedObjectID() above.
			for (int i = count - 1; i >= 0; i--) {	
				relation = olife.getRelationAt(i);
				if (relation.getOriginatingObjectType() == OLI_HOLDING
					&& relation.getRelatedObjectType() == OLI_PARTY
					&& relation.getRelationRoleCode() == OLI_REL_BENEFICIARY) {
					olife.removeRelationAt(i);
				}
			}
			//end SPR2816
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_BENEFITS, e);
		}
		return (olife);
	}
	/**
	* Creates the Coverage response.
	* @param policyExt The PolicyExtension information for contract.
	* @param olife The current olife information.
	* @return Life life
	* @exception throws NbaBaseException.
	*/
	private Policy createCoverages(Policy policy, OLifE olife, PolicyExtension policyExt) throws NbaBaseException {

		try {
			//get coverage data
			int numCov = getNumInstances(COV_COUNT);
			String[][] covData = getData(numCov, COVERAGE_FIELDS);
			String[] currentCoverage = new String[numCov];
			LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeSubObj = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
			//NBA093
			Life life = lifeSubObj.getLife();

			int flag = 0;

			while (flag < numCov) {
				Coverage coverage = new Coverage();
				currentCoverage = covData[flag];
				Integer integer = new Integer(coverageCount);
				coverage.setId("Coverage_" + integer.toString());
				coverageCount++;

				coverage.setCoverageKey(currentCoverage[1]);

				coverage.setProductCode(currentCoverage[2]);

				if (covStatusTable == null) {
					covStatusTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_COVERAGE_STATUS, compCode, "*"));
				}
				coverage.setLifeCovStatus(findOLifeCode((currentCoverage[3] + currentCoverage[4]), covStatusTable));

				if (covTypeCodeTable == null) {
					covTypeCodeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_COVTYPE, compCode, "*"));
				}
				coverage.setLifeCovTypeCode(findOLifeCode((currentCoverage[5] + currentCoverage[6]), covTypeCodeTable));

				if (covIndCodeTable == null) {
					covIndCodeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_COVINDCODE, compCode, "*"));
				}
				coverage.setIndicatorCode(findOLifeCode(currentCoverage[7], covIndCodeTable));

				if (covLivesTable == null) {
					covLivesTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_LIVESTYPE, compCode, "*"));
				}
				coverage.setLivesType(findOLifeCode((currentCoverage[8]), covLivesTable));

				/*The coverage current amount is mapped to three variables FCVFACE, FCVUNITS, 
				and FCVFLGB0.  The adapter will populate 
				Holding.Policy.Life.Coverage.CurrentAmt with the return value 
				in FCVUNITS if FCVFLGB0 is one, otherwise the return value from 
				FCVFACE will be used. Refer to: XML Holding Inquiry.doc*/
				//code deleted NBA077
				//begin NBA077	
				coverage.setCurrentAmt(currentCoverage[9]);
				coverage.setCurrentNumberOfUnits(currentCoverage[10]);
				//end NBA077	
				coverage.setAnnualPremAmt(currentCoverage[11]);
				coverage.setEffDate(formatOLifEDate(currentCoverage[12]));
				coverage.setTermDate(formatOLifEDate(currentCoverage[13]));

				OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_COVERAGE);

				CoverageExtension covExt = new CoverageExtension();

				covExt.setUnitTypeInd(currentCoverage[14]);
				//begin NBA093
				int numLife = getNumInstances(PERS_COUNT);
				String[][] lifeData = getData(numLife, LIFE_PAR_FIELDS);
				int flag2 = 0;
				if (lifeData != null) {
					while (flag2 < (lifeData.length)) {
						if ((lifeData[flag2][0].compareTo(currentCoverage[0])) == 0) {
							covExt.setRateClass(lifeData[flag2][6]);  //SPR1778
						}
						flag2++;
					}
				}
				//end NBA093
				olifeExt.setCoverageExtension(covExt);
				coverage.addOLifEExtension(olifeExt);

				coverage = createLifeParticipant(coverage, currentCoverage, olife);//NBA111
				//add CovOptions for current coverage
				coverage = createCovOptions(coverage, currentCoverage, policyExt, olife); //NBA093
				//NBA111 code deleted

				//begin NBA012
				//Death Benefit Option only applicable for Coverage Phase 1 and Product Type = 'U'
				if (flag == 0 && (policy.getProductType() == VAR_UNIV_LIFE || policy.getProductType() == UNIV_LIFE)) {
					if (deathBenefitOptionTypeTable == null) {
						deathBenefitOptionTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_DTHBENETYPE, compCode, "*"));
					}
					coverage.setDeathBenefitOptType(findOLifeCode((currentCoverage[16]), deathBenefitOptionTypeTable));
				}
				//end NBA012

				life.addCoverage(coverage);
				flag++;

			}
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_COVERAGES, e);
		}

		return (policy);
	}
	/**
	 * Creates the CovOption response.
	 * @param coverage The current coverage for contract.
	 * @param currentCoverage The coverage information returned from host.
	 * @return Coverage coverage
	 * @exception throws NbaBaseException.
	 */
	private Coverage createCovOptions(Coverage coverage, String[] currentCoverage, PolicyExtension policyExt, OLifE olife) throws NbaBaseException {

		//get CovOpt data-Benefits
		int numCovOpt = getNumInstances(COV_OPT_COUNT);
		String[][] covOptData = getData(numCovOpt, COV_OPT_FIELDS);
		// SPR3290 code deleted
		NbaOLifEId nbaOLifEId = new NbaOLifEId(olife); //NBA093

		/*associate the benefit based on the benefit phase FSBBPHS 
		and the coverage phase FCVPHASE.  Refer to: XML Holding Inquiry.doc*/

		//check for coverage options
		int flag2 = 0;
		if (covOptData != null) {
			while (flag2 < (covOptData.length)) {
				if ((covOptData[flag2][0].compareTo(currentCoverage[1])) == 0 ) {
					CovOption covOpt = new CovOption();
					if (covOpt == null) {
						throw new NbaBaseException("ERROR: Could not create an CovOpt object.");
					}
					covOpt.setId("CovOption_" + Integer.toString(covOptionCount));	//NBA093
					covOptionCount++;
					covOpt.setCovOptionKey(covOptData[flag2][0]);
					if (covOptTable == null) {
						covOptTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_OPTTYPE, compCode, "*"));
					}
					covOpt.setLifeCovOptTypeCode(findOLifeCode(covOptData[flag2][1], covOptTable));
					covOpt.setAnnualPremAmt(covOptData[flag2][2]);
					covOpt.setOptionAmt(covOptData[flag2][3]);
					covOpt.setEffDate(formatOLifEDate(covOptData[flag2][4]));
					covOpt.setTermDate(formatOLifEDate(covOptData[flag2][5]));

					OLifEExtension olifeExtCov = NbaTXLife.createOLifEExtension(EXTCODE_COVOPTION);
					//begin SPR2229
					CovOptionExtension covOptExt = olifeExtCov.getCovOptionExtension();
					if (TARGET_IDB.equalsIgnoreCase(covOptData[flag2][1]) && covOptData[flag2][1].equalsIgnoreCase("1")) {
						covOptExt.setPolicyLevelBenefitInd(true);
					}
					//end SPR2229				
					//begin NBA077
					String benStat = "";
					if (covOptData[flag2][6].equalsIgnoreCase(FLAG_BIT_ON)) { //FSBFLGB7
						benStat = "1"; //1 = BES for Denied
					} else if (covOptData[flag2][7].equalsIgnoreCase(FLAG_BIT_ON)) { //FSBFLGA7
						benStat = "2"; //2 = BES for Invalid
					}
					if (benStat.length() > 0) {
						if (covOptStatusTable == null) {
							covOptStatusTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_BENEFIT_STATUS, compCode, "*"));
						}
						covOpt.setCovOptionStatus(findOLifeCode(benStat, covOptStatusTable));  
						covOptExt.setInvalidCovOptionIndicator(benStat.equals("2") ? true : false); 
					}
					//end NBA077

					covOpt.setCovOptionPctInd(covOptData[flag2][8]); //NBA093
					if (covOptData[flag2][8].compareTo("1") == 0) {
						covOpt.setOptionAmt(covOpt.getAnnualPremAmt());
					} else {
						covOpt.setOptionAmt(covOptData[flag2][3]);
					}
					/*In CyberLife, the substandard rating for benefits is carried on the benefit 
					04 segment as a percentage and is only 
					applicable to substandard rating if greater than 100 percent.**Refer to: XML Holding Inquiry.doc*/

					if (Integer.parseInt(covOptData[flag2][9]) > 100) {	//NBA093
						//create substandard Rating
						OLifEExtension olifeExtSub = NbaTXLife.createOLifEExtension(EXTCODE_SUBSTANDARDRATING);
						SubstandardRating substandardRating = new SubstandardRating(); //NBA093
						SubstandardRatingExtension subExt = new SubstandardRatingExtension(); //NBA093
						//NBA093 deleted 2 line

						covOpt.setPermPercentageLoading(covOptData[flag2][9]); //NBA093
						subExt.setRatingStatus(SUB_STAND_ACTIVE_STATUS);
						//NBA093 deleted line
						nbaOLifEId.setId(substandardRating); //NBA093  
						olifeExtSub.setSubstandardRatingExtension(subExt); //NBA093
						substandardRating.addOLifEExtension(olifeExtSub); //NBA093
						//NBA093 deleted line
						covOpt.addSubstandardRating(substandardRating); //NBA093

						//NBA093 deleted 5 line
					}
					//begin NBA111
					//If the coverage is joint then only check for life participant ids for covoption
					if (coverage.hasLivesType() && NbaUtils.isJointLife(coverage.getLivesType())) {
						if (FLAG_BIT_OFF.equals(covOptData[flag2][10])) { //Checking whether covoption is single life
							covOpt.setLivesType(NbaOliConstants.OLI_COVLIVES_SINGLE);
							String olifeRoleCode = null;
							LifeParticipant lifePar = null;
							if (covOptData[flag2][11] != null && covOptData[flag2][11].trim().length() > 0) {
								String roleCode = (covOptData[flag2][11].substring(0, 2));

								if (roleCodesTable == null) {
									roleCodesTable =
										((NbaRolesParticipantData[]) getRolesTable(NbaTableConstants.NBA_ROLES_PARTICIPANT, compCode, "*"));
								}
								olifeRoleCode = findOLifeCode(roleCode, roleCodesTable);
								lifePar = NbaUtils.getLifeParticipantWithRoleCode(coverage, Long.valueOf(olifeRoleCode).longValue());
								if (lifePar != null) {
									covOpt.setLifeParticipantRefID(lifePar.getId());
								}
							}
						}
					 	else {
							covOpt.setLivesType(NbaOliConstants.OLI_COVLIVES_JOINTFTD);
					 	}
					}
				
					//end NBA111

					olifeExtCov.setCovOptionExtension(covOptExt);
					covOpt.addOLifEExtension(olifeExtCov);
					coverage.addCovOption(covOpt);

				}
				flag2++;

			}
		}

		return (coverage);
	}
	/**
	 * Creates the drivers license information for current party.
	 * @param currentPerson The current person being used to create party.
	 * @param driversLic The drivers license information returned on contract.
	 * @param person The current person being created.
	 * @return Person person
	 * @exception throws NbaBaseException.
	 */
	private Person createDriversLicenseInfo(String[] currentPerson, String[][] driversLic, Person person) throws NbaBaseException {

		/*
		 * Add Driver's License Information
		 */
		int flag2 = 0;

		if (driversLic != null) {
			while (flag2 < (driversLic.length)) {
				if ((driversLic[flag2][0].compareTo(currentPerson[0])) == 0) {
					person.setDriversLicenseNum(driversLic[flag2][1]);
					person.setDriversLicenseState(translateStateCode(driversLic[flag2][2]));
				} // end if 
				flag2++;
			} //end while
		} //end if
		return (person);
	}
	/**
	 * Creates the email addresses for current party.
	 * @param currentPerson The current person being used to create party.
	 * @param electAddresses The email addresses returned on contract.
	 * @param party The current party being created.
	 * @return Party party
	 * @exception throws NbaBaseException.
	 */
	private Party createEmailAddresses(String[] currentPerson, String[][] electAddresses, Party party) throws NbaBaseException {

		/*Electronic communication segments (95) contain email 
		addresses and workflow queue names.  DXE FECSGTYP contains the type of
		electronic communication to which the segment applies.  
		The valid values are E - an email address or W - workflow queue name. 
		The segments with segment type (FECSGTYP) = "W" will be ignored 
		and the data will not be added to the 
		xml stream**Refer to: XML Holding Inquiry.doc*/

		/*
		 * Add Email Addresses.
		 */
		int flag2 = 0;
		String currentPerSeq = new String();
		// SPR3290 code deleted
		if (electAddresses != null) {
			while (flag2 < (electAddresses.length)) {
				currentPerSeq = electAddresses[flag2][0] + electAddresses[flag2][1];
				if ((currentPerSeq.compareTo(currentPerson[0])) == 0) {
					if (electAddresses[flag2][3].compareToIgnoreCase("E") == 0) {
						EMailAddress address = new EMailAddress();
						if (address == null) {
							throw new NbaBaseException("ERROR: Could not create an Address object.");
						}
						address.setId("EmailAddress_" + Integer.toString(emailaddressCount));	//NBA093
						emailaddressCount++;
						address.setAddrLine(electAddresses[flag2][2]);
						if (emailTable == null) {
							emailTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_EMAILTYPE, compCode, "*"));
						}

						address.setEMailType(findOLifeCode(electAddresses[flag2][4], emailTable));
						party.addEMailAddress(address);
					}

				} // end if 
				flag2++;
			} //end while
		} //end if
		return (party);
	}
	/**
	 * Creates the Endorsement Information response.
	 * @param policy The policy information for contract.
	 * @param policyExt The policy extension for contract.
	 * @param olife The current olife instance.
	 * @return Policy policy
	 * @exception throws NbaBaseException.
	 */
	private Policy createEndorsementInfo(Policy policy, PolicyExtension policyExt, OLifE olife) throws NbaBaseException {

		try {
			int numEndorse = getNumInstances(ENDORSEMENT_COUNT);
			String[][] endorseData = getData(numEndorse, ENDORSE_FIELDS);
			String[] currentEndorsement = new String[numEndorse];

			int flag = 0, flag2 = 0;

			Coverage coverage = new Coverage();
			CovOption covOpt = new CovOption();
				LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeAnn = //NBA093
	policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(); //NBA093
			Life life = lifeAnn.getLife();
			int covCount = life.getCoverageCount();

			while (flag < numEndorse) {

				Endorsement endorsInfo = new Endorsement(); //NBA093
				currentEndorsement = endorseData[flag];

				Integer integer = new Integer(flag + 1);
				endorsInfo.setId("EndorsementInfo_" + integer.toString());

				/*Set the AppliesToContract Boolean to True 
				when the person code and sequence FRDPIDNT on the 82 segment 
				is 0000 which means applies to the contract as a whole. 
				Refer to: XML Holding Inquiry.doc*/

				if (currentEndorsement[0].endsWith("0") == true) {
					endorsInfo.setRelatedObjectType(OLI_POLICY); //NBA093
					endorsInfo.setRelatedObjectID(NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId()); //NBA093
				} else {
					// NBA093 deleted line

					/* set the AppliesToCoverageID reference to the coverage 
					phase referenced if the FRDPHASE is not equal to zero and 
					the use code is equal to zero.
					Refer to: XML Holding Inquiry.doc*/
					if (currentEndorsement[2].compareTo("0") == 0) {
						while (flag2 < covCount) {
							coverage = life.getCoverageAt(flag2);
							if (coverage.getCoverageKey().compareTo(currentEndorsement[1]) == 0) {
								endorsInfo.setRelatedObjectType(OLI_LIFECOVERAGE); //NBA093
								endorsInfo.setRelatedObjectID(coverage.getId()); //NBA093
								break;
							}
							flag2++;
						}
					}

				}

				/*set the AppliesToCovOptionID reference to the specific benefit 
				referenced if the benefit type/subtype is 
				not equal to **.  Refer to: XML Holding Inquiry.doc*/

				if (currentEndorsement[3].compareTo("**") != 0) {
					flag2 = 0;
					boolean covFound = false;
					while (flag2 < covCount) {
						coverage = life.getCoverageAt(flag2);
						if (coverage.getCoverageKey().compareTo(currentEndorsement[1]) == 0) {
							int flag3 = 0;
							// SPR3290 code deleted
							while (flag3 < coverage.getCovOptionCount()) {
								covOpt = coverage.getCovOptionAt(flag3);

								if (covOptTable == null) {
									covOptTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_OPTTYPE, compCode, "*"));
								}

								if ((findCyberCode(Long.toString(covOpt.getLifeCovOptTypeCode()), covOptTable)).compareTo(currentEndorsement[3])	//NBA093
									== 0) {
									endorsInfo.setRelatedObjectType(OLI_COVOPTION); //NBA093
									endorsInfo.setRelatedObjectID(covOpt.getId()); //NBA093
									covFound = true;
									break;
								}
								flag3++;
							}
						}
						if (covFound == true) {
							break;
						} else {
							flag2++;
						}
					}

				}
				//NBA093 deleted line
				//begin SPR1163
				if (endorseTable == null) {
					endorseTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_AmendEndorseType, compCode, "*"));
				}
				endorsInfo.setEndorsementCode(findOLifeCode(currentEndorsement[4], endorseTable));
				//end SPR1163
				endorsInfo.setEndorsementInfo(currentEndorsement[5]); //NBA093
				endorsInfo.setEndDate(formatOLifEDate(currentEndorsement[6]));

				int relCount = olife.getRelationCount();
				flag2 = 0;
				Relation relation = new Relation();
				while (flag2 < relCount) {
					relation = olife.getRelationAt(flag2);
					if (relation.getRelationKey().compareTo(currentEndorsement[0]) == 0) {
						endorsInfo.setAppliesToPartyID(relation.getRelatedObjectID()); //NBA093
						break;
					}
					flag2++;
				}

				policy.addEndorsement(endorsInfo); //NBA093
				policy.setEndorsementAt(endorsInfo, flag); //NBA093
				flag++;

			} //end while
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_ENDORSEMENTS, e);
		}
		return (policy); //NBA093
	}
	/**
	 * Create the Endorsement Information response for an Annuity.
	 * @param policy the policy information for contract
	 * @param policyExt the policy extension for contract
	 * @param olife the current olife instance
	 * @return the policy extension
	 * @exception NbaBaseException
	 */
	private Policy createEndorsementInfoAnnuity(Policy policy, PolicyExtension policyExt, OLifE olife) throws NbaBaseException { //NBA093
		try {
			int numEndorse = getNumInstances(ENDORSEMENT_COUNT);
			String[][] endorseData = getData(numEndorse, ENDORSE_FIELDS);
			String[] currentEndorsement = new String[numEndorse];
			// NBA006 code deleted
			Annuity annuity = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity(); // NBA006  NBA093
			int riderCount = annuity.getRiderCount(); // NBA006
			//begin SPR1163
			int covOptionCount;
			String personID;
			String phaseCode;
			String useCode;
			String benefitType;
			String lifeCovOptTypeCode;
			Rider rider;
			CovOption covOption;
			int phaseCodeInt;
			Relation relation;
			Endorsement endorsInfo;
			Participant participant;
			
			for (int e = 0; e < numEndorse; e++) { // NBA006
				endorsInfo = new Endorsement(); //NBA093
				currentEndorsement = endorseData[e]; // NBA006			
				endorsInfo.setId("EndorsementInfo_" + (new Integer(e + 1)).toString());
				personID = currentEndorsement[0];
				phaseCode = currentEndorsement[1];
				useCode = currentEndorsement[2];
				benefitType = currentEndorsement[3];
				if (personID.endsWith("0")) {
					endorsInfo.setRelatedObjectType(OLI_POLICY); //NBA093
					endorsInfo.setRelatedObjectID(NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId());
				} else if ("1".equals(useCode)) {
					endorsInfo.setRelatedObjectType(OLI_COVOPTION);
					if (covOptTable == null) {
						covOptTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_OPTTYPE, compCode, "*"));
					}
					lifeCovOptTypeCode = findOLifeCode(benefitType, covOptTable);
					main : for (int i = 0; i < riderCount; i++) {
						rider = annuity.getRiderAt(i);
						covOptionCount = rider.getCovOptionCount();
						for (int j = 0; j < covOptionCount; j++) {
							covOption = rider.getCovOptionAt(j);
							if (Long.toString(covOption.getLifeCovOptTypeCode()).equals(lifeCovOptTypeCode)) {
								endorsInfo.setRelatedObjectID(covOption.getId());
								break main;
							}
						}
					}
				} else if ("00".equals(phaseCode)) {
					endorsInfo.setRelatedObjectType(OLI_PAYOUTPARTICIPANT);
					int relCount = olife.getRelationCount();
					main : for (int i = 0; i < relCount; i++) {
						relation = olife.getRelationAt(i);
						if (relation.getRelationKey().equals(personID)) {
							String partyid = relation.getRelatedObjectID();
							for (int j = 0; j < riderCount; j++) {
								rider = annuity.getRiderAt(j);
								int partCount = rider.getParticipantCount();
								for (int k = 0; k < partCount; k++) {
									participant = rider.getParticipantAt(k);
									if (partyid.equals(participant.getPartyID())) {
										endorsInfo.setRelatedObjectID(participant.getId());
										break main;
									}
								}
							}
						}
					}
				} else if ("01".equals(phaseCode)) {
					endorsInfo.setRelatedObjectType(OLI_ANNUITY);
					endorsInfo.setRelatedObjectID(annuity.getId());
				} else {
					endorsInfo.setRelatedObjectType(OLI_ANNRIDER);
					if (phaseCode.length() > 0) {
						phaseCodeInt = Integer.parseInt(phaseCode);
						for (int i = 0; i < riderCount; i++) {
							rider = annuity.getRiderAt(i);
							if (rider.hasRiderKey() && rider.getRiderKey().length() > 0) {
								if (Integer.parseInt(rider.getRiderKey()) == phaseCodeInt) {
									endorsInfo.setRelatedObjectID(rider.getId());
									break;
								}
							}
						}
					}
				}
				//NBA093 deleted line				 
				if (endorseTable == null) {
					endorseTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_AmendEndorseType, compCode, "*"));
				}
				endorsInfo.setEndorsementCode(findOLifeCode(currentEndorsement[4], endorseTable));
				endorsInfo.setEndorsementInfo(currentEndorsement[5]); //NBA093
				endorsInfo.setEndDate(formatOLifEDate(currentEndorsement[6]));
				// begin NBA006
				if (personID.length() > 0) {
					int relationCount = olife.getRelationCount();
					for (int i = 0; i < relationCount; i++) {
						relation = olife.getRelationAt(i);
						if (relation.getRelationKey().equals(personID)) {
							endorsInfo.setAppliesToPartyID(relation.getRelatedObjectID()); //NBA093
							break;
						}
					}
				}
				//end SPR1163
				// end NBA006
				policy.addEndorsement(endorsInfo); //NBA093
				policy.setEndorsementAt(endorsInfo, e); // NBA006, NBA093
				// NBA006 code deleted
			} //end while
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_ENDORSEMENTS, e);
		}
		return (policy); //NBA093
	}
	/**
	 * Creates the Financial Activity response.
	 * @param policy The policy information for contract.
	 * @return Policy policy
	 * @exception throws NbaBaseException.
	 */
	private Policy createFinancialActivity(Policy policy) throws NbaBaseException {

		try {
			int numFin = getNumInstances(FIN_COUNT);
			String[][] finData = getData(numFin, FIN_FIELDS);
			String[] currentFinActiv = new String[numFin];

			int flag = 0;

			while (flag < numFin) {
				FinancialActivity finActivity = new FinancialActivity();
				currentFinActiv = finData[flag];
				Integer integer = new Integer(flag + 1);
				finActivity.setId("FinancialActivity_" + integer.toString());
				//finActivity.setFinActivityGrossAmt(currentFinActiv[0]);
				finActivity.setFinActivityDate(formatOLifEDate(currentFinActiv[1]));

				OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_FINANCIALACTIVITY);

				FinancialActivityExtension finActExt = new FinancialActivityExtension();

				if (cwaTable == null) {
					cwaTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_FINACTSUBTYPE, compCode, "*"));//NBA093
				}

				//NBA093 deleted line
				finActivity.setFinActivityType(findOLifeCode(currentFinActiv[2], cwaTable)); //NBA093
				finActivity.setFinActivityGrossAmt(currentFinActiv[0]); //NBA093
				finActExt.setErrCorrInd(currentFinActiv[3]);  //NBA093
				finActivity.setFinActivityDate(formatOLifEDate(currentFinActiv[4])); //NBA093
				finActivity.setUserCode(currentFinActiv[5]); //NBA093
				finActivity.setAccountingActivityType(currentFinActiv[6]); //NBA093
				//NBA093 deleted line
				olifeExt.setFinancialActivityExtension(finActExt);
				finActivity.addOLifEExtension(olifeExt);

				policy.addFinancialActivity(finActivity);
				policy.setFinancialActivityAt(finActivity, flag);
				flag++;
			} //end while

		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_FIN_ACTIV, e);
		}

		return (policy);
	}
	/**
	 * Creates the Holding response.
	 * @param policyData The policy information for contract.
	 * @return Holding holding
	 * @exception throws NbaBaseException.
	 */
	private Holding createHolding_Response(String[] policyData) throws NbaBaseException {

		/*
		 * Create a Holding object.
		 */
		/* There will be one holding object and the HoldingTypeCode will always be "2" - Policy**
		Refer to: XML Holding Inquiry.doc*/

		Holding holding = new Holding();
		if (holding == null) {
			throw new NbaBaseException("ERROR: Could not create a Holding object.");
		}
		
		holding.setId("Holding");
		holding.setHoldingTypeCode(2);
		holding.setAssignmentCode(policyData[50]); //NBA122

		return (holding);
	}
	/**
	 * Creates the Life Participant response.
	 * @param coverage The current coverage for contract.
	 * @param currentCoverage The coverage information returned from host.
	 * @param olife The current olife information.
	 * @return Coverage coverage
	 * @exception throws NbaBaseException.
	 */
	private Coverage createLifeParticipant(Coverage coverage, String[] currentCoverage, OLifE olife) throws NbaBaseException {

		/*search through the returned life control segments (89 segment) 
		for ones that have coverage.  The person identifier (FLCPIDNT) 
		consists of person code and sequence.  The adapter will use this 
		identifier to associate the Life Participant object with the correct 
		coverage object(s).  The coverage will have the same person code 
		and sequence in the coverage person identifier (FCVPIDNT). 
		A Life Participant object will be created as a child of each 
		coverage object with the same person code 
		and sequence. Refer to: XML Holding Inquiry.doc*/

		int numLife = getNumInstances(PERS_COUNT);
		String[][] lifeData = getData(numLife, LIFE_PAR_FIELDS);
		// SPR3290 code deleted
		// begin SPR3063
		boolean isJointCoverage = false;
		if (currentCoverage[8] != null && currentCoverage[8].length() > 0) {
			try {
				isJointCoverage = NbaUtils.isJointLife(Long.valueOf(currentCoverage[8]).longValue()); //NBA111
			} catch (NumberFormatException nfe) {
				// String must not be a number
			}
		}
		// end SPR3063
		int flag2 = 0;
		if (lifeData != null) {
			while (flag2 < (lifeData.length)) {
				//NBA111 Code deleted.
				if (((lifeData[flag2][0].compareTo(currentCoverage[0])) == 0)
					|| (isJointCoverage && lifeData[flag2][0].startsWith(JOINT_INS))) { //NBA111 If coverage is joint and lifeparticipant id starts with "01" then this life participant should be tied to the coverage as joint insured.

					LifeParticipant lifePar = new LifeParticipant();
					//NBA111 code deleted
					
					lifePar.setId("LifeParticipant_" + (Integer.toString(lifeParCount++)));	//NBA093 //NBA111
					//NBA111 Code deleted
					lifePar.setParticipantName(lifeData[flag2][2] + ", " + lifeData[flag2][3] + " " + lifeData[flag2][4]);
					String roleCode = (lifeData[flag2][0].substring(0, 2));//NBA111
					if (checkForInsurableRole(roleCode)) {
						if (roleCodesTable == null) {
							roleCodesTable = ((NbaRolesParticipantData[]) getRolesTable(NbaTableConstants.NBA_ROLES_PARTICIPANT, compCode, "*"));
						}
						lifePar.setLifeParticipantRoleCode(findOLifeCode(roleCode, roleCodesTable));
					}

					lifePar.setIssueAge(currentCoverage[15]);
					if (genderTable == null) {
						genderTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_GENDER, compCode, "*"));
					}
					lifePar.setIssueGender(findOLifeCode(lifeData[flag2][5], genderTable));
					//SPR1778 deleted code

					//NBA093 deleted 6 lines
					int relCount = olife.getRelationCount();
					int flag3 = 0;
					Relation relation = new Relation();
					while (flag3 < relCount) {
						relation = olife.getRelationAt(flag3);
						if (relation.getRelationKey().compareTo(lifeData[flag2][0]) == 0) {	//NBA111
							lifePar.setPartyID(relation.getRelatedObjectID());
							break;
						}
						flag3++;
					}

					coverage.addLifeParticipant(lifePar);
				}

				flag2++; 
				//NBA111 Code deleted
			}
		}

		return (coverage);
	}
	/**
	 * Creates the Message Detail response.
	 * @param holding The holding information for contract.
	 * @return Holding holding
	 * @exception throws NbaBaseException.
	 */
	private Holding createMessageDetail(Holding holding) throws NbaBaseException {

		int numMess = getNumInstances(MESSAGE_COUNT);
		String[][] messData = getData(numMess, MESSAGE_FIELDS);
		String[] currentMessage = new String[numMess];

		int flag = 0;

		while (flag < numMess) {

			SystemMessage messDetail = new SystemMessage(); //NBA093
			currentMessage = messData[flag];

			Integer integer = new Integer(flag + 1);
			messDetail.setId("SystemMessage_" + integer.toString());  //NBA093

			messDetail.setRelatedObjectID(currentMessage[0]); //NBA093
			messDetail.setMessageStartDate(formatOLifEDate(currentMessage[1])); //NBA093
			messDetail.setMessageCode(currentMessage[2]); //NBA093
			messDetail.setMessageDescription(currentMessage[3]); //NBA093
			if (msgSeverityTable == null) {
				msgSeverityTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_MSGSEVERITY, compCode, "*"));	//NBA093
			}
			messDetail.setMessageSeverityCode(findOLifeCode(currentMessage[4], msgSeverityTable)); //NBA093

			messDetail.setSystemMessageKey(currentMessage[5]); //NBA093
			messDetail.setSequence(currentMessage[6]); //NBA093

			holding.addSystemMessage(messDetail); //NBA093
			holding.setSystemMessageAt(messDetail, flag);
			flag++;

		} //end while

		return (holding); //NBA093
	}
	/**
	 * Create the OLifE object for a response.
	 * @param TXLifeResponse Current txLifeResponse.
	 * @return OLifE olife
	 * @exception NbaBaseException
	 */
	private OLifE createOLifE_Response(TXLifeResponse txLifeResponse) throws NbaBaseException {

		/*
		 * Create the OLifE object.
		 */
		OLifE olife = txLifeResponse.getOLifE();
		if (olife == null) {
			throw new NbaBaseException("ERROR: Could not create a OLifE object.");
		}

		/*
		 * Create Holding.
		 */
		//Only one instance of Policy information
		String[][] policyData = getData(1, POLICY_FIELDS);
		//set the company code
		compCode = policyData[0][1];
		//set the product type
		planType = policyData[0][2].trim() + policyData[0][3].trim(); //NBA006
		Holding holding = createHolding_Response(policyData[0]);
		olife.addHolding(holding);

		/*
		 * Create Party.
		 */
		int numInstances = 0;
		int numPersons = 0;

		//get addresses
		numInstances = getNumInstances(ADDR_COUNT);
		String[][] addresses = getData(numInstances, ADDR_FIELDS);

		//get electronic communications
		numInstances = getNumInstances(ELECT_COUNT);
		String[][] electAddresses = getData(numInstances, ELECTRONIC_FIELDS);

		//get persons
		numPersons = getNumInstances(PERS_COUNT);
		String[][] persons = getData(numPersons, PERSON_FIELDS);

		olife = createParties(olife, persons, addresses, electAddresses, numPersons, holding);

		//get Policy information

		holding.setPolicy(createPolicy_Response(policyData[0], olife));

		/*
		 * Add the Message Detail Extension.
		 */
		holding = createMessageDetail(holding); //NBA093

		//CREATE THE AGENT PARTIES AND RELATIONS
		olife = createAgentPartyAndRelations(olife, holding);

		olife = createBenefitRelations(olife);

		//create Investment and Subaccount objects
		olife = createInvestment(olife); //NBA211
		
		return (olife);
	}
	/**
	 * Create the Participant response.
	 * @param rider the current coverage
	 * @param currentData the coverage information returned from host
	 * @param olife the current olife information
	 * @return com.csc.fsg.nba.vo.txlife.Rider
	 * @exception throws NbaBaseException
	 */
	// NBA006 New Method
	// NBA093 changed return type and first parameter from AnnuityRiderExtension to Rider
	private Rider createParticipant(Rider rider, String[] currentData, OLifE olife)
		throws NbaBaseException {
		/*search through the returned control segments (89 segments) 
			for ones that have coverage.  The person identifier (FLCPIDNT) 
			consists of person code and sequence.  The adapter will use this 
			identifier to associate the Participant object with the correct 
			object(s).  The rider will have the same person code 
			and sequence in the person identifier (FCVPIDNT). 
			A Participant object will be created as a child of each 
			coverage object with the same person code 
			and sequence. Refer to: XML Holding Inquiry.doc*/
		int numberOfParticipants = getNumInstances(PERS_COUNT);
		String[][] participantData = getData(numberOfParticipants, LIFE_PAR_FIELDS);
		if (participantData != null) {
			for (int i = 0; i < participantData.length; i++) {
				String[] participantFields = participantData[i];
				if (participantFields[0].compareTo(currentData[1]) == 0) {
					Participant participant = new Participant();
					if (participant == null) {
						throw new NbaBaseException("ERROR: Could not create a Participant object.");
					}
					participant.setId("Participant_" + Integer.toString(participantCount));
					participantCount++;
					participant.setParticipantName(participantFields[2] + ", " + participantFields[3] + " " + participantFields[4]);
					if (roleCodesTable == null) {
						if (planType.substring(0, 1).compareTo("F") == 0) {
							if (prodTypeTable == null) {
								prodTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_POLPROD, compCode, "*"));
							}
							String prodType = findOLifeCode(planType, prodTypeTable);
							roleCodesTable = ((NbaRolesParticipantData[]) getRolesTable(NbaTableConstants.NBA_ROLES_PARTICIPANT, compCode, prodType));
							//NBA006
						}
					}
					participant.setParticipantRoleCode(findOLifeCode(currentData[1].substring(0, 2), roleCodesTable));
					participant.setIssueAge(currentData[9]);
					if (genderTable == null) {
						genderTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_GENDER, compCode, "*"));
					}
					participant.setIssueGender(findOLifeCode(participantFields[5], genderTable));
					//SPR1778 deleted code
					//NBA093 deleted 5 lines
					Relation relation = new Relation();
					for (int r = 0; r < olife.getRelationCount(); r++) {
						relation = olife.getRelationAt(r);
						if (relation.getRelationKey().compareTo(currentData[1]) == 0) {
							participant.setPartyID(relation.getRelatedObjectID());
							break;
						}
					}
					rider.addParticipant(participant);  //NBA093
				}
			}
		}
		return rider;  //NBA093
	}
	/**
	 * Create the Participant response.
	 * @param payout the current coverage for contract
	 * @param currentData the coverage information returned from host
	 * @param olife the current olife information
	 * @return com.csc.fsg.nba.vo.txlife.Payout
	 * @exception throws NbaBaseException
	 */
	// NBA006 New Method
	private Payout createParticipant(Payout payout, String[] currentData, OLifE olife) throws NbaBaseException {
		/*search through the returned control segments (89 segments) 
			for ones that have coverage.  The person identifier (FLCPIDNT) 
			consists of person code and sequence.  The adapter will use this 
			identifier to associate the Participant object with the correct 
			object(s).  The payout will have the same person code 
			and sequence in the person identifier (FCVPIDNT). 
			A Participant object will be created as a child of each 
			coverage object with the same person code 
			and sequence. Refer to: XML Holding Inquiry.doc*/
		int numberOfParticipants = getNumInstances(PERS_COUNT);
		String[][] participantData = getData(numberOfParticipants, LIFE_PAR_FIELDS);
		if (participantData != null) {
			for (int i = 0; i < participantData.length; i++) {
				String[] participantFields = participantData[i];
				if (participantFields[0].compareTo(currentData[1]) == 0) {
					Participant participant = new Participant();
					if (participant == null) {
						throw new NbaBaseException("ERROR: Could not create a Participant object.");
					}
					participant.setId("Participant_" + (Integer.toString(participantCount)));
					participantCount++;
					participant.setParticipantName(participantFields[2] + ", " + participantFields[3] + " " + participantFields[4]);
					if (roleCodesTable == null) {
						if (planType.substring(0, 1).compareTo("F") == 0) {
							if (prodTypeTable == null) {
								prodTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_POLPROD, compCode, "*"));
							}
							String prodType = findOLifeCode(planType, prodTypeTable);
							roleCodesTable = ((NbaRolesParticipantData[]) getRolesTable(NbaTableConstants.NBA_ROLES_PARTICIPANT, compCode, prodType));
							//NBA006
						}
					}
					participant.setParticipantRoleCode(findOLifeCode(currentData[1].substring(0, 2), roleCodesTable));
					participant.setIssueAge(currentData[9]);
					if (genderTable == null) {
						genderTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_GENDER, compCode, "*"));
					}
					participant.setIssueGender(findOLifeCode(participantFields[5], genderTable));
					//SPR1778 deleted code
					//NBA093 deleted 5 lines
					Relation relation = new Relation();
					for (int r = 0; r < olife.getRelationCount(); r++) {
						relation = olife.getRelationAt(r);
						if (relation.getRelationKey().compareTo(currentData[1]) == 0) {
							participant.setPartyID(relation.getRelatedObjectID());
							break;
						}
					}
					payout.addParticipant(participant);
				}
			}
		}
		return payout;
	}
	/**
	* Create the Party objects and their relation objects to the holding inquiry.
	* @param olife The current OLifE transaction.
	* @param persons The persons on the contract
	* @param addresses The address on the contract
	* @param electAddresses The electronic addresses on the contract
	* @param numPersons The number of people on the contract
	* @param holding The current Holding transaction.
	* @return OLifE olife
	* @exception throws NbaBaseException.
	*/
	private OLifE createParties(OLifE olife, String[][] persons, String[][] addresses, String[][] electAddresses, int numPersons, Holding holding)
		throws NbaBaseException {

		/*The ACORD data model allows for one party object per person / corporation 
		with multiple relation objects indicating multiple roles on the contract.  
		The CyberLife host system has an individual 89 segment for each role a 
		person / corporation has on a contract.  For example, one person has 
		multiple roles on a contract, Primary insured, owner and payor, this person 
		would have three (89) life control segments on the CyberLife host, 
		one for primary insured, one for owner and one for payor.   When an ACORD 
		standard xml transaction is created for this example there will
		be a single party object representing the person and three relation objects 
		representing the persons multiple roles on the contract**Refer to: XML Holding Inquiry.doc
		
		The party and relation objects are constructed first to determine the parties on
		a contract and their relationships to the contract. Refer to: XML Holding Inquiry.doc and
		for the construction of the initial Party and Relation objects refer to Appendix A.*/

		try {
			int flag = 0;
			// SPR3290 code deleted
			String duplicate = "false";

			String[] currentPerson = new String[numPersons];

			/*   //Drivers License Information-not being done for Phase 1
			   int numInstances = getNumInstances(RISK_COUNT);
			   String[][] driversLic = getData(numInstances, RISK_FIELDS);*/
			int relationCount = olife.getRelationCount();

			/*There is a particular order of precedence that parties need to be
			created in. Refer to: XML Holding Inquiry.doc*/

			Party party = null;
			//add Primary party
			while (flag < numPersons) {
				if (PRIMARY_INS.compareTo(persons[flag][0].substring(0, 2)) == 0) {
					currentPerson = persons[flag];
					party = createParty_Response(olife.getPartyCount(), currentPerson, addresses, electAddresses);
					olife.addParty(party);

					/*
					* Create the First Relation for Holding to Current Party.
					*/

					Relation relation = createRelation_HoldingToParty(holding.getId(), party.getId(), currentPerson, relationCount);

					olife.addRelation(relation);
					break;
				}
				flag++;
			} //end while

			//create parties for joint insured

			flag = 0;
			while (flag < numPersons) {

				if (JOINT_INS.compareTo(persons[flag][0].substring(0, 2)) == 0) {
					currentPerson = persons[flag];
					relationCount = olife.getRelationCount();
					duplicate = checkForDuplicatePeople(currentPerson, olife, olife.getPartyCount(),addresses, electAddresses); //SPR2091
					if (duplicate.compareTo("false") == 0) {
						party = createParty_Response(olife.getPartyCount(), currentPerson, addresses, electAddresses);
						olife.addParty(party);
						//add relation for current party
						Relation relation = createRelation_HoldingToParty(holding.getId(), party.getId(), currentPerson, relationCount);
						olife.addRelation(relation);
					} else { /*
																									               * If duplicate person, still need to create relation object
																									               * Create the Relation for Holding to Current Party.
																									               */
						Relation relation = createRelation_HoldingToParty(holding.getId(), duplicate, currentPerson, relationCount);
						olife.addRelation(relation);
						duplicate = "false";
					}
					break;
				}
				flag++;
			} //end while for joint insured

			//create parties for other insured

			flag = 0;
			while (flag < numPersons) {

				if (OTHER_INS.compareTo(persons[flag][0].substring(0, 2)) == 0) {
					currentPerson = persons[flag];
					relationCount = olife.getRelationCount();
					duplicate = checkForDuplicatePeople(currentPerson, olife, olife.getPartyCount(),addresses, electAddresses); //SPR2091
					if (duplicate.compareTo("false") == 0) {
						party = createParty_Response(olife.getPartyCount(), currentPerson, addresses, electAddresses);
						olife.addParty(party);
						//add relation for current party
						Relation relation = createRelation_HoldingToParty(holding.getId(), party.getId(), currentPerson, relationCount);
						olife.addRelation(relation);
					} else { /*
																									               * If duplicate person, still need to create relation object
																									               * Create the Relation for Holding to Current Party.
																									               */
						Relation relation = createRelation_HoldingToParty(holding.getId(), duplicate, currentPerson, relationCount);
						olife.addRelation(relation);
						duplicate = "false";
					}
				}
				flag++;
			} //end while for other insured

			//create parties for Dependents insured

			flag = 0;
			while (flag < numPersons) {

				if (DEPENDENT.compareTo(persons[flag][0].substring(0, 2)) == 0) {
					currentPerson = persons[flag];
					relationCount = olife.getRelationCount();
					duplicate = checkForDuplicatePeople(currentPerson, olife, olife.getPartyCount(),addresses, electAddresses); //SPR2091
					if (duplicate.compareTo("false") == 0) {
						party = createParty_Response(olife.getPartyCount(), currentPerson, addresses, electAddresses);
						olife.addParty(party);
						//add relation for current party
						Relation relation = createRelation_HoldingToParty(holding.getId(), party.getId(), currentPerson, relationCount);
						olife.addRelation(relation);
					} else { /*
																									               * If duplicate person, still need to create relation object
																									               * Create the Relation for Holding to Current Party.
																									               */
						Relation relation = createRelation_HoldingToParty(holding.getId(), duplicate, currentPerson, relationCount);
						olife.addRelation(relation);
						duplicate = "false";
					}
				}
				flag++;
			} //end while for Dependents insured

			//create parties for Spouse insured

			flag = 0;
			while (flag < numPersons) {

				if (SPOUSE.compareTo(persons[flag][0].substring(0, 2)) == 0) {
					currentPerson = persons[flag];
					relationCount = olife.getRelationCount();
					duplicate = checkForDuplicatePeople(currentPerson, olife, olife.getPartyCount(),addresses, electAddresses); //SPR2091
					if (duplicate.compareTo("false") == 0) {
						party = createParty_Response(olife.getPartyCount(), currentPerson, addresses, electAddresses);
						olife.addParty(party);
						//add relation for current party
						Relation relation = createRelation_HoldingToParty(holding.getId(), party.getId(), currentPerson, relationCount);
						olife.addRelation(relation);
					} else { /*
																									               * If duplicate person, still need to create relation object
																									               * Create the Relation for Holding to Current Party.
																									               */
						Relation relation = createRelation_HoldingToParty(holding.getId(), duplicate, currentPerson, relationCount);
						olife.addRelation(relation);
						duplicate = "false";
					}
				}
				flag++;
			} //end while for Spouse insured

			//now create parties without insurable roles

			flag = 0;
			while (flag < numPersons) {

				//skip primary insured-already created
				if (checkForInsurableRole(persons[flag][0].substring(0, 2)) == false) {
					currentPerson = persons[flag];
					relationCount = olife.getRelationCount();
					duplicate = checkForDuplicatePeople(currentPerson, olife, olife.getPartyCount(),addresses, electAddresses); //SPR2091
					if (duplicate.compareTo("false") == 0) {
						party = createParty_Response(olife.getPartyCount(), currentPerson, addresses, electAddresses);
						olife.addParty(party);
						//add relation for current party
						Relation relation = createRelation_HoldingToParty(holding.getId(), party.getId(), currentPerson, relationCount);
						olife.addRelation(relation);
					} else { /*
																									               * If duplicate person, still need to create relation object
																									               * Create the Relation for Holding to Current Party.
																									               */
						Relation relation = createRelation_HoldingToParty(holding.getId(), duplicate, currentPerson, relationCount);
						olife.addRelation(relation);
						duplicate = "false";
					}
				}
				flag++;
			} //end while for parties other than insurable roles
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_PARTIES, e);
		}

		return (olife);
	}
	/**
	* Create the a Party for current person
	* @param flag The current party number being created.
	* @param currentPerson The current person being used to create party.
	* @param addresses The address on the contract
	* @param electAddresses The electronic addresses on the contract
	* @return Party party
	* @exception throws NbaBaseException.
	*/
	private Party createParty_Response(int flag, String[] currentPerson, String[][] Addresses, String[][] electAddresses) throws NbaBaseException {

		/*
		 * Create a Party object.
		 */
		Party party = new Party();
		if (party == null) {
			throw new NbaBaseException("ERROR: Could not create a Party object.");
		}

		Integer integer = new Integer(flag + 1);
		party.setId(PARTY_HOST + integer.toString()); //NBA211
		if (currentPerson[10].compareTo(PERSON) == 0) {
			party.setPartyTypeCode(OLIX_PARTYTYPE_INDIVIDUAL); //SPR1018
		} else if (currentPerson[10].compareTo(CORPORATION) == 0) {
			party.setPartyTypeCode(OLIX_PARTYTYPE_CORPORATION); //SPR1018
		} else if (currentPerson[10].compareTo(TRUST) == 0) {
			party.setPartyTypeCode(OLIX_PARTYTYPE_TRUST); //SPR1018
		} else {
			party.setPartyTypeCode(" ");
		}

		if (govIdTypeTable == null) {
			govIdTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_GOVTIDTC, compCode, "*"));
		}
		party.setGovtIDTC(findOLifeCode(currentPerson[11], govIdTypeTable)); //NBA093

		party.setGovtID(currentPerson[12]);

		/*
		 * Make the party a person.
		 */
		if (currentPerson[10].compareTo(PERSON) == 0) {
			Person person = new Person();
			if (person == null) {
				throw new NbaBaseException("ERROR: Could not create a Person object.");
			}

			person.setFirstName(currentPerson[1]);
			person.setLastName(currentPerson[2]);
			person.setMiddleName(currentPerson[3]);
			if (prefixTable == null) {
				prefixTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLIEXT_LU_PREFIX, compCode, "*"));
			}
			if (currentPerson[4].length() != 0) {
				person.setPrefix(findOLifeCode(currentPerson[4], prefixTable));
			} else {
				person.setPrefix(currentPerson[4]);
			}
			if (suffixTable == null) {
				suffixTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLIEXT_LU_SUFFIX, compCode, "*"));
			}
			if (currentPerson[5].length() != 0) {
				person.setSuffix(findOLifeCode(currentPerson[5], suffixTable));
			} else {
				person.setSuffix(currentPerson[5]);
			}
			if (genderTable == null) {
				genderTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_GENDER, compCode, "*"));
			}
			person.setGender(findOLifeCode(currentPerson[6], genderTable));
			person.setBirthDate(formatOLifEDate(currentPerson[7]));
			person.setBirthJurisdiction(translateStateCode(currentPerson[8]));
			//begin SPR2365
			if (currentPerson[9].equalsIgnoreCase("S")) {
				person.setSmokerStat(OLI_TOBPREMBASIS_SMOKER);
			} else {
				person.setSmokerStat(OLI_TOBPREMBASIS_NONSMOKER);
			}
			//end SPR2365
			//SPR1778 deleted code
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_PERSON);
			PersonExtension personExt = new PersonExtension();
			personExt.setRateClass(currentPerson[9]); //SPR1778
			olifeExt.setPersonExtension(personExt);
			person.addOLifEExtension(olifeExt);

			person.setAge(currentPerson[16]);
			//Set data for Primary Insured only
			if (PRIMARY_INS.compareTo(currentPerson[0].substring(0, 2)) == 0) {
				//Only one instance of primary information
				String[][] primaryData = getData(1, PRIMARY_FIELDS);
				party.setResidenceState(translateStateCode(primaryData[0][0]));
				if (countriesTable == null) {
					countriesTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_NATION, compCode, "*"));
				}
				party.setResidenceCountry(findOLifeCode(primaryData[0][1], countriesTable));
				Risk risk = new Risk();
				risk.setReplacementInd(primaryData[0][2]);
				party.setRisk(risk);
			}

			if (maritalStatTable == null) {
				maritalStatTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_MARSTAT, compCode, "*"));
			}
			person.setMarStat(findOLifeCode(currentPerson[17], maritalStatTable));
			if (occupationTable == null) {
				occupationTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_OCCUPCLASS, compCode, "*"));
			}
			person.setOccupation(findOLifeCode(currentPerson[18], occupationTable));

			PersonOrOrganization personOr = new PersonOrOrganization();
			if (personOr == null) {
				throw new NbaBaseException("ERROR: Could not create a PersonOrOrganization object.");
			}

			personOr.setPerson(person);
			party.setPersonOrOrganization(personOr);
		} //END IF PERSON
		if ((currentPerson[10].compareTo(CORPORATION) == 0) || (currentPerson[10].compareTo(TRUST) == 0)) {
			Organization organ = new Organization();
			organ.setDBA(currentPerson[22]);
			organ.setOrgForm(party.getPartyTypeCode());

			PersonOrOrganization personOr = new PersonOrOrganization();
			if (personOr == null) {
				throw new NbaBaseException("ERROR: Could not create a PersonOrOrganization object.");
			}

			personOr.setOrganization(organ);
			party.setPersonOrOrganization(personOr);
		}
		/*
		 * Add a Home Phone Number.
		 */
		//begin NBA151
		String fullPhoneNumber = currentPerson[19];
		Phone phone;
		if (fullPhoneNumber.length() > 0) {
			phone = new Phone();		
			phone.setId("Phone_" + Integer.toString(phoneCount++));	 			 
			phone.setPhoneTypeCode(OLI_PHONETYPE_HOME);
			setPhoneNumber(fullPhoneNumber, phone);			 
			party.addPhone(phone);
		}
		//end NBA151
		/*
		 * Add a Second Phone Number if present.
		 */
		//begin NBA151
		fullPhoneNumber = currentPerson[21];
		if (fullPhoneNumber.length() > 0) {
			phone = new Phone();
			phone.setId("Phone_" + Integer.toString(phoneCount++));	 
			if (phoneTypeTable == null) {
				phoneTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_PHONETYPE, compCode, "*"));
			}
			phone.setPhoneTypeCode(findOLifeCode(currentPerson[20], phoneTypeTable));
			setPhoneNumber(fullPhoneNumber, phone);	
			party.addPhone(phone);
		
		} //end NBA151 
		/*
		 * Add Addresses.
		 */
		party = createAddresses(currentPerson, Addresses, party);
		/*
		 * Add Email Addresses.
		 */
		party = createEmailAddresses(currentPerson, electAddresses, party);
		return (party);
	}

    /**
	 * Creates the policy object.
	 * @param policyData The policy information for contract.
	 * @param olife The current olife information.
	 * @return Policy policy.
	 * @exception throws NbaBaseException.
	 */
	private Policy createPolicy_Response(String[] policyData, OLifE olife) throws NbaBaseException {

		/*
		 * Add the Policy object.
		 */
		Policy policy = new Policy();
		if (policy == null) {
			throw new NbaBaseException("ERROR: Could not create a Policy object.");
		}
		try {
			policy.setId("Policy");
			policy.setPolNumber(policyData[0]);
			String polType = policyData[2].trim() + policyData[3].trim();
			if (prodTypeTable == null) {
				prodTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_POLPROD, compCode, "*"));
			}

			policy.setProductType(findOLifeCode(polType, prodTypeTable));

			policy.setProductCode(policyData[22]);
			policy.setCarrierCode(policyData[1]);

			policy.setJurisdiction(translateStateCode(policyData[7]));
			policy.setReinsuranceInd(policyData[8]);

			if (policyModeTable == null) {
				policyModeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_PAYMODE, compCode, "*"));
			}
			policy.setPaymentMode(findOLifeCode(policyData[9], policyModeTable));

			policy.setPaymentAmt(policyData[10]);
			policy.setAnnualPaymentAmt(policyData[27]);

			if (paymentMethodTable == null) {
				paymentMethodTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_PAYMETHOD, compCode, "*"));
			}

			policy.setPaymentMethod(findOLifeCode(policyData[11], paymentMethodTable));

			policy.setBillNumber(policyData[31]); //NBA012
			policy.setBilledToDate(policyData[32]); //NBA012

			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_POLICY);

			PolicyExtension policyExt = olifeExt.getPolicyExtension();

			//begin NBA012

			//  NBA093 deleted 3 lines

			//Add the Issue Company and Admin Company        
			policyExt.setIssueCompanyCode(policyData[34]);
			policy.setAdministeringCarrierCode(policyData[35]); //NBA093

			//If the billing use code FBRDUEUS is zero, both the use code and 
			//the due day FBRDUEDY will be ignored and the corresponding XML tags will 
			//not be created
			if (policyData[36] != null && !policyData[36].equals("0")) {
				if (timingForNoticeTable == null) {
					timingForNoticeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLIEXT_LU_TIMING, compCode, "*"));
				}
				policyExt.setTiming(findOLifeCode(policyData[36], timingForNoticeTable)); //NBA093
				policy.setPaymentDraftDay(policyData[33]);
			}

			//  NBA093 deleted 2 line

			policyExt.setQuotedPremiumBasisAmt(policyData[37]);
			policyExt.setQuotedPremiumBasisFrequency(policyData[38]);

			//end NBA012	       

			/*
			 * Add the Policy Indicators.
			 */
			String policyStat = policyData[4].trim() + policyData[5].trim() + policyData[6].trim() + policyData[23].trim();
			if (policyStatTable == null) {
				policyStatTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_PENDING_CONTRACT_STATUS, compCode, "*"));
			}
			policyExt.setPendingContractStatus(findOLifeCode(policyStat, policyStatTable));
			policyExt.setAssignmentInd(policyData[12]);
			policyExt.setEndorsementInd(policyData[13]);
			policyExt.setOtherInsuredInd(policyData[24]);
			policyExt.setBeneficiaryInd(policyData[14]);
			policyExt.setRatedInd(policyData[15]);
			// NBA093 deleted 2 lines
			policyExt.setAgentErrorsInd(policyData[26]);
			
			//begin NBA122
			policy.setEndorsementInd(policyData[13]);
			policy.setOtherInsuredInd(policyData[24]);
			policy.setBeneficiaryInd(policyData[14]);
			policy.setRatedInd(policyData[15]);
			//end NBA122

			/*
			 * Add the ApplicationInfo object.
			 */
			policy = createApplicationInfo(policy, policyData);

			/*
			 * Add the Financial Activity object.
			 */
			policy = createFinancialActivity(policy);

			//NBA093 deleted 4 lines

			policy = createRequirementInfo(policy, olife);

			LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeAnnDisSubObj = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
			//NBA093
			if (lifeAnnDisSubObj == null) {
				throw new NbaBaseException("ERROR: Could not create a LifeOrAnnuityOrDisabilityHealth object.");
			}
			//check to see if Annuity
			if (policyData[2].compareTo("F") == 0) {
				// createAnnuity
				Annuity annuity = new Annuity();
				if (annuity == null) {
					throw new NbaBaseException("ERROR: Could not create an annuity object.");
				}
				annuity.setId("Annuity");
				annuity = createAnnuity(annuity, olife, policy); //NBA093

				lifeAnnDisSubObj.setAnnuity(annuity);
				policy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(lifeAnnDisSubObj); //NBA093
				/*
				* Add the Endorsement Information.
				*/
				policy = createEndorsementInfoAnnuity(policy, policyExt, olife);
			} else {
				/*
				 * Add the Life object.
				 */
				Life life = new Life();
				if (life == null) {
					throw new NbaBaseException("ERROR: Could not create a Life object.");
				}

				life.setId("Life");
				life.setFaceAmt(policyData[29]);
				lifeAnnDisSubObj.setLife(life);

				//begin NBA012
				if (nfoOptionTable == null) {
					nfoOptionTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_NONFORTPROV, compCode, "*"));
				}
				life.setNonFortProv(findOLifeCode(policyData[41], nfoOptionTable));

				//Current Int Rate applicable to only to UL and VUL
				if (policy.getProductType() == 3 || policy.getProductType() == 4) {
					life.setCurrIntRate(policyData[42]);
				}

				if (divTypeTable == null) {
					divTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_DIVTYPE, compCode, "*"));
				}
				life.setDivType(findOLifeCode(policyData[43], divTypeTable));
				life.setInitialPremAmt(policyData[44]);
				life.setProjectedGuarIntRate(policyData[45]);
				//end NBA012

				policy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(lifeAnnDisSubObj); //NBA093

				/*
				 * Add a Coverage objects.
				 */
				policy = createCoverages(policy, olife, policyExt);

				policy = createSubstandardRatings(olife, policy); //NBA093

				/*
				* Add the Endorsement Information.
				*/
				policy = createEndorsementInfo(policy, policyExt, olife); //NBA093
			}
			// policy.setLifeOrAnnuityOrDisabilityHealth(lifeAnnDisSubObj);
			//add the Policy Extension
			olifeExt.setPolicyExtension(policyExt);

			//NBA093 deleted 3 lines
			policy.addOLifEExtension(olifeExt);
			//NBA093 deleted 3 lines

			/*
			 *Add the AltPremMode Object
			 */
			policy = createAltPremMode(policy, policyData); // NBA012

		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_POLICY, e);
		}

		return (policy);
	}
	/**
	 * Create an object Relation for current party.
	 * @param originatingID originating object
	 * @param relatedID related object
	 * @param currentPerson Current person party object being created for.
	 * @param relationCount Total number of relations created so far.
	 * @return Relation relation
	 * @exception NbaBaseException
	 */
	private Relation createRelation_HoldingToParty(String originatingID, String relatedID, String[] currentPerson, int relationCount)
		throws NbaBaseException {

		/*
		 * Create the Relation object.
		 */

		String roleCode = (currentPerson[0].substring(0, 2));
		if (roleRelCodesTable == null) {
			//begin NBA006
			if (planType.substring(0, 1).compareTo("F") == 0) {
				if (prodTypeTable == null) {
					prodTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_POLPROD, compCode, "*"));
				}
				String prodType = findOLifeCode(planType, prodTypeTable);
				//end NBA006
				roleRelCodesTable = ((NbaRolesRelationData[]) getRolesTable(NbaTableConstants.NBA_ROLES_RELATION, compCode, prodType));
				//NBA006
			} else {
				roleRelCodesTable = ((NbaRolesRelationData[]) getRolesTable(NbaTableConstants.NBA_ROLES_RELATION, compCode, "*"));
			}
		}

		Relation relation = new Relation();
		if (relation == null) {
			throw new NbaBaseException("ERROR: Could not create a Relation object.");
		}
		relation.setId("Relation_" + (relationCount + 1));
		relation.setOriginatingObjectID(originatingID);
		relation.setRelatedObjectID(relatedID);
		relation.setRelationRoleCode(findOLifeCode(roleCode, roleRelCodesTable));
		relation.setRelatedRefID(currentPerson[0].substring(2, 4));
		relation.setOriginatingObjectType(OLI_HOLDING); //SPR1018
		relation.setRelatedObjectType(OLI_PARTY); //SPR1018
		relation.setRelationKey(currentPerson[0]);
		return (relation);
	}
	/**
	 * Creates the Requirement Information response.
	 * @param policy The policy information for contract.
	 * @param olife The current olife information.
	 * @return Policy policy
	 * @exception throws NbaBaseException.
	 */
	private Policy createRequirementInfo(Policy policy, OLifE olife) throws NbaBaseException {

		try {
			int numReq = getNumInstances(REQ_COUNT);
			String[][] reqData = getData(numReq, REQ_FIELDS);
			String[] currentReqInfo = new String[numReq];

			int flag = 0;

			while (flag < numReq) {
				RequirementInfo reqInfo = new RequirementInfo();
				currentReqInfo = reqData[flag];
				Integer integer = new Integer(flag + 1);
				reqInfo.setId("Requirement_" + integer.toString());
				if (ReqCodeTable == null) {
					ReqCodeTable = ((NbaRequirementsData[]) getRequirementsTable(NbaTableConstants.NBA_REQUIREMENTS, compCode, "*"));
				}
				//begin SPR2388
				String reqCode = null;
				if(currentReqInfo[1] != null && currentReqInfo[1].trim().length() > 0){
					reqCode = String.valueOf(Integer.parseInt(currentReqInfo[1]));
				}else{
					reqCode = "";
				}
				reqInfo.setReqCode(findOLifeCode(reqCode, ReqCodeTable));
				//end SPR2388
				reqInfo.setHORequirementRefID(currentReqInfo[2]);
				if (ReqStatusTable == null) {
					ReqStatusTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_REQSTAT, compCode, "*"));
				}
				reqInfo.setReqStatus(findOLifeCode(currentReqInfo[3], ReqStatusTable));
				//NBA130 code deleted
				OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_REQUIREMENTINFO);

				RequirementInfoExtension reqInfoExt = new RequirementInfoExtension();
				reqInfoExt.setCreatedDate(formatOLifEDate(currentReqInfo[4])); //NBA130

				if (ReqRestrictionsTable == null) {
					ReqRestrictionsTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_REQRESTRICTION, compCode, "*")); //SPR3171
				}
				reqInfo.setRestrictIssueCode(findOLifeCode(currentReqInfo[5], ReqRestrictionsTable)); //NBA093
				reqInfo.setUserCode(currentReqInfo[6]); //NBA093
				reqInfo.setSequence(currentReqInfo[7]); //NBA093

				int relCount = olife.getRelationCount();
				int flag2 = 0;
				Relation relation = new Relation();
				while (flag2 < relCount) {
					relation = olife.getRelationAt(flag2);
					if (relation.getRelationKey().compareTo(currentReqInfo[0]) == 0) {
						reqInfo.setAppliesToPartyID(relation.getRelatedObjectID());
						break;
					}
					flag2++;
				}
				olifeExt.setRequirementInfoExtension(reqInfoExt);
				reqInfo.addOLifEExtension(olifeExt);

				policy.addRequirementInfo(reqInfo);
				policy.setRequirementInfoAt(reqInfo, flag);
				flag++;
			} //end while
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_REQ_INFO, e);
		}
		return (policy);
	}
	/**
	 * Creates the Substandard Ratings for Policy.
	 * @param policyExt The PolicyExtension information for contract.
	 * @param olife The current olife information.
	 * @return Policy policy//NBA093
	 * @exception throws NbaBaseException.
	 */
	private Policy createSubstandardRatings(OLifE olife, Policy policy) throws NbaBaseException { //NBA093

		try {
			//get substandard data
			int numSub = getNumInstances(SUB_STAND_COUNT);
			String[][] subData = getData(numSub, SUB_STAND_FIELDS);
			String[] currentSub = new String[numSub];
			LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeSubObj = //NBA093
				policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(); //NBA093
			Life life = lifeSubObj.getLife();
			NbaOLifEId nbaOLifEId = new NbaOLifEId(olife); //NBA093

			int coverageCount = life.getCoverageCount();
			// SPR3290 code deleted
			int flag = 0,flag2 = 0; //NBA111
			// SPR3290 code deleted
			Coverage coverage = new Coverage();

			while (flag < numSub) {

				OLifEExtension olifeExtSub = NbaTXLife.createOLifEExtension(EXTCODE_SUBSTANDARDRATING);
				SubstandardRating substandardRating = new SubstandardRating(); //NBA093
				substandardRating.addOLifEExtension(olifeExtSub); //NBA093
				SubstandardRatingExtension subExt = olifeExtSub.getSubstandardRatingExtension(); //NBA093
				currentSub = subData[flag];

				/*CyberLife creates a phase code 102 for ratings on a life.  
				The returned segment with this phase code will be ignored by the adapter. 
				Refer to: XML Holding Inquiry.doc*/
				if (currentSub[1].compareTo("102") == 0) {
					flag++;
				} else {
					//begin NBA093 SPR1549
					nbaOLifEId.setId(substandardRating);
					if (tableRatingTable == null) {
						tableRatingTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_RATINGS, compCode, "*"));
					}
					if (ratingReasonTable == null) {
						ratingReasonTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_RATING_REASON, compCode, "*"));
					}
					if (ratingCommissionTable == null) {
						ratingCommissionTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLIEXT_LU_COMMISCODE, compCode, "*"));
					}
					if (ratingStatusTable == null) {
						ratingStatusTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_ACTIVE_SEGMENT_STATUS, compCode, "*"));
					}
					boolean tempRating = (FLAG_BIT_ON.equals(currentSub[16])); //FELFLGB4
					subExt.setExtraPremSubtype(currentSub[3]); //FELSBTYP
					String table = currentSub[4].trim(); //FELETABL
					if (table.length() > 0) {
						if (tempRating) {
							substandardRating.setTempTableRating(findOLifeCode(table, tableRatingTable));
						} else {
							substandardRating.setPermTableRating(findOLifeCode(table, tableRatingTable));
						}
					}
					double ratingPct = Double.parseDouble(currentSub[5]); //FELPCTRT
					if (ratingPct > 0) {
						if (tempRating) {
							subExt.setTempPercentageLoading(ratingPct);
						} else {
							subExt.setPermPercentageLoading(ratingPct);
						}
					}
					if (currentSub[6].length() > 0) { //FELFLTEX
						double flatAmt = Double.parseDouble(currentSub[6]);
						if (flatAmt > 0) {
							if (tempRating) {
								substandardRating.setTempFlatExtraAmt(flatAmt);
							} else {
								subExt.setPermFlatExtraAmt(flatAmt);
							}
						}
					}
					if (tempRating && currentSub[7].length() > 0) { //FELETDAT
						if (substandardRating.hasTempTableRating()) {
							substandardRating.setTempTableRatingEndDate(formatOLifEDate(currentSub[7]));
						} else {
							substandardRating.setTempFlatEndDate(formatOLifEDate(currentSub[7]));  
						}
					}
					subExt.setEffDate(formatOLifEDate(currentSub[8])); //FELEFFDF
					substandardRating.setRatingReason(findOLifeCode(currentSub[9], ratingReasonTable)); //FELEREAS
					substandardRating.setRatingCommissionRule(findOLifeCode(currentSub[10], ratingCommissionTable)); //FELECOM
					subExt.setExtraPremPerUnit(currentSub[12]); //FELEAP
					subExt.setRatingStatus(findOLifeCode(currentSub[13], ratingStatusTable)); //FELEDUSE
					subExt.setAnnualPremAmt(currentSub[14]); //FELPREMA
					String covPhase = currentSub[1]; //FELEPHS
					substandardRating.setSubstandardRatingKey(covPhase);
					//end SPR1549 
					flag2 = 0;
					while (flag2 < coverageCount) {
						coverage = life.getCoverageAt(flag2);
						if (coverage.getCoverageKey().compareTo(covPhase) == 0) { //SPR1549
							//NBA111 code deleted.
							//begin NBA111
							String roleCode = (currentSub[0].substring(0, 2));
							if (roleCodesTable == null) {
								roleCodesTable = ((NbaRolesParticipantData[]) getRolesTable(NbaTableConstants.NBA_ROLES_PARTICIPANT, compCode, "*"));
							}
							String olifeRoleCode = findOLifeCode(roleCode, roleCodesTable);
							LifeParticipant lifePar = NbaUtils.getLifeParticipantWithRoleCode(coverage, Long.valueOf(olifeRoleCode).longValue());
							if (lifePar != null) {
								lifePar.addSubstandardRating(substandardRating);
							}
						//end NBA111	
						}
						flag2++;
					}
					//end NBA093

					flag++;
				}
			}
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_SUBSTAND, e);
		}

		return (policy); //NBA093
	}
	/**
	 * Create a TXLife response.
	 * @param txLife Current TXLife response.
	 * @return TXLife
	 * @exception NbaBaseException
	 */
	protected TXLife createTXLife_Response(TXLife txLife) throws NbaBaseException {

		/*
		 * Create a UserAuthResponseAndTXLifeResponseAndTXLifeNotify object.
		 * This is a container object for the three pieces of a response.
		 */
		UserAuthResponseAndTXLifeResponseAndTXLifeNotify response = txLife.getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
		if (response == null) {
			throw new NbaBaseException("ERROR: Could not create a UserAuthResponseAndTXLifeResponseAndTXLifeNotify object");
		}

		/*
		 * Build the response.
		 */
		try {

			response.setTXLifeResponseAt((createTXLifeResponse(response)), 0);
			if (response.getTXLifeResponseAt(0).getTransResult().getResultCode() > 1) {
				txLife.setUserAuthResponseAndTXLifeResponseAndTXLifeNotify(response);
				return (txLife);
			}

			txLife.setUserAuthResponseAndTXLifeResponseAndTXLifeNotify(response);
		} catch (NbaBaseException e) {
			throw new NbaBaseException(e);
		}

		return (txLife);
	}
	/**
	 * Create a TXLifeResponse object and children.
	 * 
	 * @return TXLifeResponse
	 * @exception NbaBaseException
	 */
	private TXLifeResponse createTXLifeResponse(UserAuthResponseAndTXLifeResponseAndTXLifeNotify Response) throws NbaBaseException {

		TXLifeResponse txLifeResponse = Response.getTXLifeResponseAt(0);
		if (txLifeResponse == null) {
			throw new NbaBaseException("ERROR: Could not create a TXLifeResponse object.");
		}

		/*
		 * Add a TransResult object.
		 */
		TransResult transResult = new TransResult();
		if (transResult == null) {
			throw new NbaBaseException("ERROR: Could not create a TransResult object");
		}

		txLifeResponse.setTransResult(getTransResult(transResult));

		if (transResult.getResultCode() > 1) {
			return (txLifeResponse);
		}

		/*
		 * Create the OLifE object.
		 */
		if (transType == HOLDING) {
			try {
				txLifeResponse.setOLifE(createOLifE_Response(txLifeResponse));
			} catch (NbaBaseException e) {
				throw new NbaBaseException(e);
			};
		}

		return (txLifeResponse);
	}
	/**
	 * This method acts as the entry point for the creation of the Xml document
	 * @param The incoming response from the host.
	 * @return txLife The TXLife containing the host response
	 * @exception NbaBaseException
	 */
	public TXLife createXmlResponse(TXLife txLifeRequest) throws NbaBaseException {

		TXLife txLife = NbaTXLife.createTXLifeResponse(txLifeRequest);
		txLife = createTXLife_Response(txLife);

		return (txLife);
	}
	/**
	 * Find Cyberlife code for OLife value
	 * @param olifeValue  OLife value to translate
	 * @param table UctTable data to search through
	 * @return cyberValue code
	 */
	private String findCyberCode(String olifeValue, NbaUctData[] table) {
		String cyberValue = " ";
		if (table != null) {
			if (olifeValue.length() != 0) {
				for (int i = 0; i < table.length; i++) {
					if (table[i].getIndexValue() != null && table[i].getIndexValue().compareToIgnoreCase(olifeValue) == 0) {	//NBA093
						cyberValue = table[i].getBesValue();
						break;
					}
				}
			}
		}
		return (cyberValue);
	}
	/**
	 * Find OLife code for Cyberlife value for Requirements data
	 * @param cyberValue Cyberlife value to translate
	 * @param table UctTable data to search through
	 * @return olife code
	 */
	private String findOLifeCode(String cyberValue, NbaRequirementsData[] table) {
		String olifeValue = " ";
		if (table != null) {
			if (cyberValue.length() != 0) {
				for (int i = 0; i < table.length; i++) {
					if (table[i].besValue != null && table[i].besValue.compareToIgnoreCase(cyberValue) == 0) { //NBA093
						olifeValue = table[i].code();
						break;
					}
				}
			}
		}
		return (olifeValue);
	}
	/**
	 * Find OLife code for Cyberlife value for life paricipant roles
	 * @param cyberValue Cyberlife value to translate
	 * @param table UctTable data to search through
	 * @return olife code
	 */
	private String findOLifeCode(String cyberValue, NbaRolesParticipantData[] table) {
		String olifeValue = " ";
		if (table != null) {
			if (cyberValue.length() != 0) {
				for (int i = 0; i < table.length; i++) {
					if (table[i].besValue != null &&  table[i].besValue.compareToIgnoreCase(cyberValue) == 0) { //NBA093
						olifeValue = table[i].code();
						break;
					}
				}
			}
		}
		return (olifeValue);
	}
	/**
	 * Find OLife code for Cyberlife value for relation roles
	 * @param cyberValue Cyberlife value to translate
	 * @param table UctTable data to search through
	 * @return olife code
	 */
	private String findOLifeCode(String cyberValue, NbaRolesRelationData[] table) {
		String olifeValue = " ";
		if (table != null) {
			if (cyberValue.length() != 0) {
				for (int i = 0; i < table.length; i++) {
					if (table[i].besValue != null && table[i].besValue.compareToIgnoreCase(cyberValue) == 0) { //NBA093
						olifeValue = table[i].code();
						break;
					}
				}
			}
		}
		return (olifeValue);
	}
	/**
	 * Find OLife code for Cyberlife value for UCT data
	 * @param cyberValue Cyberlife value to translate
	 * @param table UctTable data to search through
	 * @return olife code
	 */
	private String findOLifeCode(String cyberValue, NbaUctData[] table) {
		String olifeValue = " ";
		if (table != null) {
			if (cyberValue != null && cyberValue.length() != 0) {	//SPR1538
				for (int i = 0; i < table.length; i++) {
					if (table[i].besValue != null && table[i].besValue.compareToIgnoreCase(cyberValue) == 0) {	//NBA093
						olifeValue = table[i].code();
						break;
					}
				}
			}
		}
		return (olifeValue);
	}
	/**
	 * Find OLife description for Cyberlife value for plans data
	 * @param cyberValue Cyberlife value to translate
	 * @param table Table data to search through
	 * @return olife description
	 */
	private String findPlanDesc(String cyberValue, NbaPlansData[] table) {
		String olifeValue = " ";
		if (table != null) {
			if (cyberValue.length() != 0) {
				for (int i = 0; i < table.length; i++) {
					if (table[i].coverageKey != null && table[i].coverageKey.compareToIgnoreCase(cyberValue) == 0) { //NBA093
						olifeValue = table[i].covKeyTranslation;
						break;
					}
				}
			}
		}
		return (olifeValue);
	}
	/**
	 * Format incoming date string as YYYYMMDD
	 * @param date Date to reformat
	 * @return date Reformatted date
	 */
	protected String formatISODate(String date) {
		if (date.length() < 10) {
			return "";
		} else {
			return (date.substring(0, 4) + date.substring(5, 7) + date.substring(8, 10));
		}
	}
	/**
	 * Format incoming date string as YYYY-MM-DD
	 * @param date Date value needed to be reformatted
	 * @return date New date value
	 */
	protected String formatOLifEDate(String date) {
		if (date.length() < 8) {
			return "";
		} else {
			return (date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8));
		}
	}
	/**
	 * Format the incoming string as #####-####.
	 * @param zip ZipCode needed for reformatting
	 * @return zipCode New formatted zip code
	 */
	protected String formatZipCode(String zip) {
		if (zip.length() == 9)
			return (zip.substring(0, 5) + "-" + zip.substring(5, 9));
		else
			return zip;
	}
	/**
	 * Parses through the host response and pulls out the requested data.
	 * @param numInstances The number of instances of segment data.
	 * @param fieldNames The host values to populate data for.
	 * @param fieldNames java.lang.String[]
	 * @return itemArray Parsed data
	
	 */
	protected String[][] getData(int numInstances, String[] fieldNames) {
		int beginIndex = 0, endIndex = 0, flag = 0, field = 0; // SPR3290
		// SPR3290 code deleted

		int numFields = fieldNames.length;
		if (numInstances > 0) {

			String[][] itemArray = new String[numInstances][numFields];
			while (field < numFields) {
				while (flag < numInstances) {
					beginIndex = hostResponse.indexOf(fieldNames[field] + "=", beginIndex);
					if (beginIndex == -1) {
						flag++;
					} else {
						beginIndex = hostResponse.indexOf("=", beginIndex);
						endIndex = hostResponse.indexOf(";", beginIndex);
						itemArray[flag][field] = hostResponse.substring(beginIndex + 1, endIndex);
						flag++;
					}

				}
				field = field + 1;
				beginIndex = 0;
				endIndex = 0;

				flag = 0;
			}

			return itemArray;
		} else {
			return null;
		}
	}
	/**
	 * Gets the number of instances of a particular segment type
	 * @param countField The name of the segment to get a count for.
	 * @return numInstances 
	 */
	protected int getNumInstances(String countField) {
		int beginIndex = 0, endIndex = 0;
		// SPR3290 code deleted
		int numInstances = 0;

		beginIndex = hostResponse.indexOf(countField);
		if (beginIndex == -1) {
			numInstances = 0;
		} else {
			beginIndex = hostResponse.indexOf("=", beginIndex);
			endIndex = hostResponse.indexOf(";", beginIndex);
			numInstances = Integer.parseInt(hostResponse.substring(beginIndex + 1, endIndex));	//NBA093
		}
		return numInstances;

	}
	/**
	 * Calls the translation tables for Plans Tables
	 * @param compCode java.lang.String
	 * @return tarray com.csc.fsg.nba.tableaccess.NbaTableData.
	 */
	private NbaTableData[] getPlansTable(String compCode) {

		Date currentDate = new Date();
		HashMap aCase = new HashMap();
		aCase.put(C_COMPANY_CODE, compCode);
		aCase.put(C_STATE_CODE, "*");
		aCase.put("appDate", currentDate);

		NbaTableData[] tArray = null;

		try {
			tArray = ntsAccess.getDisplayData(aCase, "NBA_PLANS");
		} catch (NbaDataAccessException e) {}

		return (tArray);
	}
	/**
	 * Calls the translation tables for Requirements Tables
	 * @param tableName The name of the Requirements table.
	 * @param compCode Company code.
	 * @param covKey Coverage key(pdfKey).
	 * @return tarray NbaTableData.
	 */
	private NbaTableData[] getRequirementsTable(String tableName, String compCode, String covKey) {

		HashMap aCase = new HashMap();
		aCase.put(C_COMPANY_CODE, compCode);
		aCase.put(C_TABLE_NAME, tableName);
		aCase.put(C_COVERAGE_KEY, covKey);
		aCase.put(C_SYSTEM_ID, NbaConstants.SYST_CYBERLIFE); // SPR1018

		NbaTableData[] tArray = null;
		try {
			tArray = ntsAccess.getDisplayData(aCase, tableName);
		} catch (NbaDataAccessException e) {}

		return (tArray);
	}
	/**
	 * Calls the translation tables for Roles Tables
	 * @param tableName The name of the Roles table.
	 * @param compCode Company code.
	 * @param covKey Coverage key(pdfKey).
	 * @return tarray NbaTableData.
	 */
	private NbaTableData[] getRolesTable(String tableName, String compCode, String productType) {

		HashMap aCase = new HashMap();
		aCase.put(C_COMPANY_CODE, compCode);
		aCase.put(C_TABLE_NAME, tableName);
		aCase.put(C_PRODUCT_TYPE, productType);
		aCase.put(C_SYSTEM_ID, NbaConstants.SYST_CYBERLIFE); // SPR1018

		NbaTableData[] tArray = null;
		try {
			tArray = ntsAccess.getDisplayData(aCase, tableName);
		} catch (NbaDataAccessException e) {}

		return (tArray);
	}
	/**
	 * Calls translation table to retrieve list of States
	 * @param stateTable Holds the table data
	 * @return tarray NbaTableData.
	 */
	private NbaTableData[] getStateTable(String stateTable) {

		HashMap aCase = new HashMap();
		aCase.put(C_COMPANY_CODE, "*");
		aCase.put(C_TABLE_NAME, stateTable);

		NbaTableData[] tArray = null;
		try {
			tArray = ntsAccess.getDisplayData(aCase, stateTable);

		} catch (NbaDataAccessException e) {}
		return (tArray);
	}
	/**
	 * Gets the transaction results
	 * @param transResult 
	 * @return void
	 */
	protected TransResult getTransResult(TransResult transResult) {
		int beginIndex = 0, endIndex = 0;
		int newIndex = 0;
		// SPR3290 code deleted
		int leastResult = 1;
		int currentTransResult = 0;
		String errorDesc = new String();
		// SPR3290 code deleted

		/*The DISP return will contain a return code.  The return code will be 
		translated to the OLIFE values for a return.  If unsuccessful, 
		the return code and error messages will be placed in the XML response and will be stored 
		as comments in AWD. Refer to: XML Holding Inquiry.doc*/

		//first loop through and find the various return codes
		while (hostResponse.length() > beginIndex) {
			beginIndex = hostResponse.indexOf("WHATTODO", beginIndex);
			//if no more occurences return the transResult
			if (beginIndex == -1) {
				break;
			}
			//get the return code
			beginIndex = hostResponse.indexOf(",", beginIndex);
			endIndex = hostResponse.indexOf(";", beginIndex);
			currentTransResult = Integer.parseInt(hostResponse.substring(beginIndex + 1, endIndex));	//NBA093
			//set Result Code and check to see if any error messages
			if (currentTransResult > leastResult) {
				transResult.setResultCode(5);
			} else {
				transResult.setResultCode(1);
			}
			while (hostResponse.length() > beginIndex) {
				newIndex = hostResponse.indexOf("ERR=", beginIndex);
				if (newIndex == -1) {
					break;
				} else {
					ResultInfo resultInfo = new ResultInfo();
					resultInfo.setResultInfoCode("999");
					endIndex = hostResponse.indexOf(";", newIndex);
					String currentError = hostResponse.substring(newIndex + 4, endIndex);
					errorDesc = "";
					errorDesc = errorDesc.concat("  " + currentError);
					if (hostResponse.length() == endIndex + 1) {
						resultInfo.setResultInfoDesc(errorDesc);
						beginIndex = endIndex;
						transResult.addResultInfo(resultInfo);

						break;
					}
					if (hostResponse.substring(endIndex + 1, endIndex + 5).compareTo("ERR=") == 0) {
						resultInfo.setResultInfoDesc(errorDesc);
						beginIndex = endIndex;
						transResult.addResultInfo(resultInfo);
						continue;
					} else {
						resultInfo.setResultInfoDesc(errorDesc);
						beginIndex = endIndex;
						transResult.addResultInfo(resultInfo);

						break;
					}
				}
			}

		}
		return transResult;

	}
	/**
	 * Calls the translation tables for UCT Tables for CyberLife Translation values
	 * @param tableName The name of the UCT table.
	 * @param compCode Company code.
	 * @param covKey Coverage key(pdfKey).
	 * @return tarray NbaTableData.
	 */
	private NbaTableData[] getUctTable(String tableName, String compCode, String covKey) {
	    return getUctTable(tableName, compCode, covKey, NbaConstants.SYST_CYBERLIFE); // SPR3216
	}
	/**
	 * Translate State code
	 * @param state State code to translate
	 * @return transState Translated state value
	 */
	private String translateStateCode(String state) {
		String transState = new String();

		if (state.length() == 0) {
			return (state);
		} else {
			if (stateTable == null) {
				stateTable = (NbaStatesData[]) (getStateTable("NBA_STATES"));
			}

			for (int i = 0; i < stateTable.length; i++) {
				if ((stateTable[i].cybAlphaStateCodeTrans.substring(0, 2).compareTo(state.substring(0, 2)) == 0)
					|| (stateTable[i].cybStateCode.trim().compareTo(state) == 0)) {
						transState = String.valueOf(stateTable[i].getStateCode()).trim();//SPR1346
					break;
				}
			}
		}
		return (transState);
	}
	/**
	 * Calls the translation tables for UCT Tables for Agent Conrol File Translation values
	 * @param tableName The name of the UCT table.
	 * @param compCode Company code.
	 * @param covKey Coverage key(pdfKey).
	 * @return tarray NbaTableData.
	 */
	//SPR3216 New Method
	private NbaTableData[] getACFUctTable(String tableName, String compCode, String covKey) {
	    return getUctTable(tableName, compCode, covKey, NbaConstants.AGENCY_SYSTEM_AGENT_CONTROL_FILE); 
	}
	/**
	 * Calls the translation tables for UCT Tables
	 * @param tableName The name of the UCT table.
	 * @param compCode Company code.
	 * @param covKey Coverage key(pdfKey).
	 * @return tarray NbaTableData.
	 */
	//SPR3216 New Method
	private NbaTableData[] getUctTable(String tableName, String compCode, String covKey, String systemID) {

		HashMap aCase = new HashMap();
		aCase.put(C_COMPANY_CODE, compCode);
		aCase.put(C_TABLE_NAME, tableName);
		aCase.put(C_COVERAGE_KEY, covKey);
		aCase.put(C_SYSTEM_ID, systemID);  

		NbaTableData[] tArray = null;
		try {
			tArray = ntsAccess.getDisplayData(aCase, tableName);
		} catch (NbaDataAccessException e) {}

		return (tArray);
	}
	/**
	 * Update the Phone area code and dial number from the fullPhoneNumber. The first three characters
	 * are the area code. The remainder is the dial number. 
     * @param fullPhoneNumber
     * @param phone - the Phone
     */
    //NBA151 New Method 
    protected void setPhoneNumber(String fullPhoneNumber, Phone phone) {
        int len = fullPhoneNumber.length();
        if (len < 8) {
            phone.setAreaCode("");
            phone.setDialNumber(fullPhoneNumber);
        } else {
            int areaCodeLen = len - 7;
            phone.setAreaCode(fullPhoneNumber.substring(0, areaCodeLen));
            phone.setDialNumber(fullPhoneNumber.substring(areaCodeLen));
        }
    }
	/**
	 * @return Returns the hostResponse.
	 */
    // NBA195 new method
	public String getHostResponse() {
		return hostResponse;
	}
	/**
	 * @param hostResponse The hostResponse to set.
	 */
    // NBA195 new method
	public void setHostResponse(String hostResponse) {
		this.hostResponse = hostResponse;
	}
	/**
	 * @return Returns the transType.
	 */
    // NBA195 new method
	public int getTransType() {
		return transType;
	}
	/**
	 * @param transType The transType to set.
	 */
    // NBA195 new method
	public void setTransType(int transType) {
		this.transType = transType;
	}
	
	/**
     * Creates the Investment response.
     * @param holding The holding information for contract.
     * @return Holding holding
     * @exception throws NbaBaseException.
     */
    //NBA211 New Method
    protected OLifE createInvestment(OLifE olife) throws NbaBaseException {
        int numOfFunds = getNumInstances(FUND_ALLOC_COUNT);
        Holding holding = olife.getHoldingAt(0);
        Investment investment = null;
        NbaOLifEId nbaOLifEId = null;

        if (numOfFunds > 0) {
            investment = new Investment();
            holding.setInvestment(investment);
            nbaOLifEId = new NbaOLifEId(olife);
        }

        String[][] fundsData = getData(numOfFunds, SUBACCOUNT_FIELDS);

        for (int i = 0; i < numOfFunds; i++) {
            String[] currentSubAccount = fundsData[i];
            SubAccount subAccount = new SubAccount();
            nbaOLifEId.setId(subAccount);
            subAccount.setProductCode(currentSubAccount[0]);
            subAccount.setAllocPercent(currentSubAccount[1]);
            subAccount.setSystematicActivityType(null);
            investment.addSubAccount(subAccount);
        }
        return olife;
    }
	
}
