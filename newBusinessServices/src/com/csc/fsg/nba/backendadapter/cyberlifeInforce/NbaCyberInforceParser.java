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
 * *******************************************************************************<BR>
 */
//NBA093 code deleted
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.backendadapter.NbaBackEndAdapterFacade;
import com.csc.fsg.nba.backendadapter.cyberlife.NbaCyberConstants;
import com.csc.fsg.nba.business.process.NbaUnderwritingRiskHelper;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
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
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaPartyInquiryInfo;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.AccountHolderNameCC;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.AltPremMode;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.AnnuityExtension;
import com.csc.fsg.nba.vo.txlife.AnnuityRiderExtension;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ArrDestination;
import com.csc.fsg.nba.vo.txlife.ArrDestinationExtension;
import com.csc.fsg.nba.vo.txlife.ArrSource;
import com.csc.fsg.nba.vo.txlife.Arrangement;
import com.csc.fsg.nba.vo.txlife.ArrangementExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.Banking;
import com.csc.fsg.nba.vo.txlife.BankingExtension;
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
import com.csc.fsg.nba.vo.txlife.HoldingExtension;
import com.csc.fsg.nba.vo.txlife.Investment;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeExtension;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.LifeUSA;
import com.csc.fsg.nba.vo.txlife.MaxDisbursePctOrMaxDisburseAmt;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Participant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.Payout;
import com.csc.fsg.nba.vo.txlife.PayoutExtension;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Phone;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.ReinsuranceInfo;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RelationExtension;
import com.csc.fsg.nba.vo.txlife.RelationProducerExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.Rider;
import com.csc.fsg.nba.vo.txlife.Risk;
import com.csc.fsg.nba.vo.txlife.SubAccount;
import com.csc.fsg.nba.vo.txlife.SubAccountExtension;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TaxReporting;
import com.csc.fsg.nba.vo.txlife.TaxWithholding;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapter;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapterFactory;

/**
 * Parse the CyberLife host response and create an XML document to send back out.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA073</td><td>Version 3</td><td>Add agent ressponse to parser</td></tr>
 * <tr><td>SPR1778</td><td>Version 4</td><td>Correct problems with SmokerStatus and RateClass in Automated Underwriting, and Validation.</td></tr>
 * <tr><td>NBA112</td><td>Version 4</td><td>Agent Name and Address</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change</td></tr>
 * <tr><td>NBA111</td><td>Version 4</td><td>Joint Insured</td></tr>
 * <tr><td>SPR1986</td><td>Version 4</td><td>Remove CLIF Adapter Defaults for Pay up and Term date of Coverages and Covoptions</td></tr>
 * <tr><td>NBA105</td><td>Version 4</td><td>Underwriting Risk</td></tr>
 * <tr><td>SPR2084</td><td>Version 4</td><td>Last Notice Date wrongly interpreted and Last Notice Type not constructed in holding response xml</td></tr>
 * <tr><td>SPR2155</td><td>Version 4</td><td>In the contract change view, Backend Adapter parse error is thrown for the annuity contract when the Next button is clicked.</td></tr>
 * <tr><td>SPR2366</td><td>Version 5</td><td>Added unique file names for webservice stubs</td></tr>
 * <tr><td>SPR2388</td><td>Version 5</td><td>CyberLife requirement code does not match due to leading 0s returned from host</td></tr>
 * <tr><td>SPR2408</td><td>Version 5</td><td>Validation error 2035 should be generated for Annuities when the issue date of a rider is before the issue date of Annuity.</td></tr>
 * <tr><td>SPR1346</td><td>Version 5</td><td>Displaying State Drop-down list in Alphabetical order by country/ACORD State Code Change</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>NBA124</td><td>Version 5</td><td>Underwriting Risk Remap</td></tr>
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>General Code Cleanup</td></tr>
 * <tr><td>SPR2737</td><td>Version 6</td><td>Error Return Handling Never Defined for Underwriting Risk CyberLife Service (CREF)</td></tr>
 * <tr><td>NBA132</td><td>Version 6</td><td>Equitable Distribution of Work</td></tr>
 * <tr><td>NBA143</td><td>version 6</td><td>Inherent benefits processing</td></tr>
 * <tr><td>NBA195</td><td>Version 7</td><td>JCA Adapter for DXE Interface to CyberLife</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR2151</td><td>Version 8</td><td>Correct the Contract Validation edits and Adaptor logic for EFT, PAC and Credit Card Billing</td></tr>
 * <tr><td>SPR3171</td><td>Version 8</td><td>Requirement Restriction of 5 Needs to Be Added to Tables Schema</td></tr>
 * <tr><td>NBA234</td><td>Version 8</td><td>ACORD Transformation project</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaCyberInforceParser implements NbaCyberConstants, NbaOliConstants, NbaTableAccessConstants, NbaCyberInforceConstants {
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {


    String filename = "c:\\nba\\dxe\\AgentReqXml.xml";    
    String filename2 = "c:\\nba\\dxe\\AgentResponse.txt";    
	
    
    String xmlResponse = new String();
    String xmlDoc = null;
    String dxeDoc = null;

    NbaCyberInforceParser holding = new NbaCyberInforceParser();
    

    try {
        xmlDoc = readFile(filename);
        dxeDoc = readFile(filename2);  
    } catch (IOException ioe) {

    }

    try {
        ByteArrayInputStream inputstream = new ByteArrayInputStream(xmlDoc.getBytes());
        holding.hostResponse = dxeDoc;
        
        TXLife txLifeRequest = TXLife.unmarshal(inputstream);

       
        // SPR3290 code deleted
    	UserAuthRequestAndTXLifeRequest request = txLifeRequest.getUserAuthRequestAndTXLifeRequest();
   		TXLifeRequest txlifeR = request.getTXLifeRequestAt(0);
        holding.transType  = (int)txlifeR.getTransType();
       
		TXLife txLife = holding.createXmlResponse(txLifeRequest);
		
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        txLife.marshal(stream);
        xmlResponse = stream.toString();
    } catch (Exception e) {
    }
    //use this for temporary test purposes
   
    try {
        FileWriter file = new FileWriter("c:\\nba\\dxe\\OUTPUTXML.XML");
        int length = xmlResponse.length();
        file.write(xmlResponse, 0, length - 1);
        file.close();
    } catch (IOException e) {
    }

}

	protected static String readFile(String fileName) throws java.io.IOException, java.io.FileNotFoundException {
	File inFile = new File(fileName);
	FileReader in = new FileReader(inFile);
	char[] xml = new char[(int) inFile.length()];
	in.read(xml, 0, (int) inFile.length());
	return new String(xml);
}


	protected NbaOLifEId nbaOLifEId = null; //NBA077
	
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
		COV_IND, //23
		INF_PARTY_KEY, //24 NBA077
		INF_PARTY_GOVTID_STAT, //25 NBA077
		APP_RESIDENT_AREA, //26 NBA077
		CORRESP_PREF, //27 NBA077
		CLIENT_CITIZENSHIP, //28 NBA077
		INF_ORG_FORM, //29 NBA077
		FEDERAL_WITHHOLDING_IND, //30 NBA077
		INF_FED_DEPENDENTS, //31 NBA077
		ST_DEPENDENTS, //32 NBA077
		INF_FED_EXEMPTIONS, //33 NBA077
		ST_EXEMPTIONS	//34 NBA077
	}; 

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
		ALT_PREM_MODE_MO,  //49 //NBA012
		INF_COV_MAJOR_LINE, //50 
		INF_PAID_TO_DATE, //51 
		LST_BILL_DT, //52 
		HANDLING, //53
		STATEMENT_FREQ, //54
		CONFIRMATION_FREQ, //55
		INF_BILLING_OPTION, //56
		INF_SP_FREQ_PAID_TO_DATE, //57
		SF_PAYROLL_FREQ, //58
		INF_SP_FREQ_SKIP_MONTH, //59
		SF_BILL_TO_DT, //60
		SEC_DIV_OPT, //61
		APP_ASSN_NUM, //62 //NBA077
		INF_PREMIUM_STATUS, //63 //NBA077 
		COV_EFF_DATE,  //64 //NBA077
		INF_POLICY_STATEMENT_BASIS,  //65 //NBA077
		INF_POLICY_PAID_ADDT_ELEC,  //66 //NBA077
		TARGET_INAP_AMT_RETRIEVE,  //67 //NBA077  NBA104
		TARGET_ADDL_SINGLE_PREM_AMT_RETRIEVE,  //68 //NBA077 NBA104
		INF_PREMIUM_STATUS,  //69 //NBA077
		INF_ENTRY_CODE,  //70 //NBA077
		SF_FIRST_MONTHLY_DT, //71 //NBA077
		COV_CURR_AMT_2, //72 //NBA077
		RQST_MATURITY_AGE, //73 //NBA077 SPR1986
		RQST_MATURITY_DUR, //74 //NBA077 SPR1986
		COV_UNIT_TYPE, //75 NBA077
		INF_POLICY_LOAN_IND, //76 NBA077
		INF_POLICY_SUSPEND_IND, //77 NBA077
		INF_POLICY_EXCESS_COLLECT_AMT, //78 NBA077
		INF_POLICY_REINSTATEMENT_AMT, //79 NBA077   		
		INF_POLICY_TRAD_GRACE_END_DATE, //80 NBA077
		INF_POLICY_AP_GRACE_END_DATE, //81 NBA077
		INF_POLICY_REINSTATEMENT_QUOTE, //82 NBA077
		LST_BILL_KIND //83 SPR2084
	}; 
	
	//FINANCIAL ACTIVITY
	private String[] FIN_FIELDS = { FIN_GROSS_AMT,//0
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

	private String[] AGENT_PER_ADDR = { AGENT_FIRST_NAME, //0
		AGENT_MIDDLE_INITIAL, //1
		AGENT_LAST_NAME, //2
		AGENT_NAME_ADDENDUM, //3
		AGENT_ADDRESS_LINE1, //4
		AGENT_ADDRESS_LINE2, //5
		AGENT_ADDRESS_CITY, //6
		AGENT_ADDRESS_STATE, //7
		AGENT_ADDRESS_ZIP }; //8

	private String[] AGENT_BUS_ADDR = { AGENT_NAME_ADDENDUM, //0
		AGENCY_ADDRESS_LINE1, //1
		AGENCY_ADDRESS_LINE2, //2
		AGENCY_ADDRESS_CITY, //3
		AGENCY_ADDRESS_STATE, //4
		AGENCY_ADDRESS_ZIP }; //5

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
		DEATH_BENEFIT_OPT,  //16 NBA077 
		APP_FORM_TYPE_NUM_ASSN,  //17 NBA077
		INF_COV_ANN_PREM,  //18 NBA077
		INF_COV_DAETH_BENEFIT_OPTTYPE,  //19 NBA077
		UNISEX_OVERRIDE_IND,  //20 NBA077
		UNISEX_OVERRIDE_CD,  //21 NBA077
		UNISEX_OVERRIDE_SUBSERIES,  //22 NBA077
		COV_TERM_DATE,  //23 NBA077
		INF_COV_COMM_CODE,  //24 NBA077
		INF_COV_CLASS,  //25 NBA077
		INF_COV_SERIES,  //26 NBA077
		INF_COV_SUB_SERIES,  //27 NBA077
		INF_COV_PAYUP_DATE };  //28 NBA077

	private String[] COV_OPT_FIELDS = { COV_OPT_BENE_PHASE, //0
		COV_OPT_TYPE, //1
		COV_OPT_ANN_PREM_AMT, //2
		COV_OPT_OPTION_AMT, //3
		COV_OPT_EFFDATE, //4
		COV_OPT_TERMDATE, //5
		COV_OPT_STATUS, //6
		COV_OPT_INV_IND, //7
		COV_OPT_PERC_TYPE, //8
		COV_OPT_PERC,  //9
		INF_COV_OPT_PREM_CALC_PERC, //10
		COV_OPT_LIVES_TYPE, // 11 NBA077  // NBA111
		INF_COV_OPT_PERM_PER_UNIT, // 12 NBA077
		COV_OPT_BENE_PHASE, // 13 NBA077
		TARGET_IDB_RULE_RETRIEVE, // 14 NBA077 NBA104
		TARGET_IDB_PCT_RETRIEVE, // 15 NBA077 NBA104
		TARGET_IDB_YEARS_RETRIEVE, // 16 NBA077 NBA104
		INF_COV_OPT_PAYUP_DATE, //17 NBA077  NBA104
		"",// 18 NBA077 NBA104
		COV_OPT_PERSON_IDREF, // 19 NBA111
		INF_COV_OPT_SELECTRULE_IND}; //20 	

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
		SUB_STAND_FLAG }; //15
	private String[] ANNUITY_FIELDS = { COV_KEY, //0        
		COVERAGE_ID, //1
		COV_PROD_CODE, //2
		COV_CURR_AMT_1, //3
		COV_CURR_AMT_2, //4
		COV_IND_CODE, //5
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
		ANNU_INIT_PREM, //16 
		CURRENT_INT_RATE, //17 
		GUAR_INT_RATE, //18
		TARGET_ROTH_TAX_YEAR_RETRIEVE, // 19 	NBA104
		TARGET_ROTH_AMOUNT_RETRIEVE, //20 	NBA104
		INF_AP_MATURITY_DATE, //21		//SPR1986
		INF_COV_CLASS, //22
		INF_COV_SERIES, //23
		INF_COV_SUB_SERIES, //24 NBA077
		ANNU_QUAL_TYPE, //25 NBA077
		RQST_MATURITY_AGE, //26 NBA077	SPR1986
		RQST_MATURITY_DUR, //27 NBA077	SPR1986
		INF_COV_COMM_CODE, //28 NBA077
		INF_COV_PAYUP_DATE };//29 NBA077

	private String[] ANNUITY_COV_FIELDS = { COV_OPT_BENE_PHASE, //0        
		COV_OPT_TYPE, //1
		COV_OPT_OPTION_AMT, //2
		// begin NBA006
		COV_OPT_PERC_TYPE, //3
		COV_OPT_STATUS, //4
		COV_OPT_INV_IND, //5
		COV_OPT_ANN_PREM_AMT, //6
		COV_OPT_EFFDATE, //7
		COV_OPT_TERMDATE, //8 // end NBA006
		INF_COV_OPT_SELECTRULE_IND}; //9 NBA143
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
		
	private String[] HOLDING_FIELDS = { A_LAST_ANNIV_PROC, //0
		INF_ASSIGNMENT_CODE, //1
		APP_PENSION_CD,	//2
		APP_RESTRICT_CD, //3
		APP_LST_ACCT_DT}; //4
		
	private String[] LIFEUSA_FIELDS = { A_LAST_ANNIV_PROC, //0
		INF_ASSIGNMENT_CODE, //1
		APP_PENSION_CD,	//2
		APP_RESTRICT_CD, //3
		APP_LST_ACCT_DT}; //4
		
	private String[] PAYOUT_FIELDS = {INF_INCOME_OPTION, //0
		INF_PAYOUT_NUM_PAYMENTS, //1
		INF_PAYOUT_AMOUNT, //2
		INF_PAYOUT_PERCENT, //3
		INF_AUTO_MODE, //4 NBA077
		INF_AUTO_START_DATE, //5 NBA077
		INF_AUTO_FORM, //6 //NBA077
		INF_PAYOUT_EXCLUSION_RATIO, //7
		INF_PAYOUT_PRIMARY_REDUCTION_PERCENT, //8
		INF_AUTO_END_DATE, //9 NBA077 
		INF_PAYOUT_SEC_REDUCTION_PERCENT, //10
		INF_PAYOUT_ADJ_INVESTED_AMT, //11
		INF_PAYOUT_ASSUMED_INTEREST_RATE, //12
		INF_PAYOUT_EXCLUSION_AMT, //13 NBA077
		FEDERAL_WITHHOLDING_IND //14 NBA077
	};

	private String[] TAX_FIELDS = {INF_FED_TAX_WITHHOLDING_TYPE, //0
		INF_FED_TAX_WITHHELD_PCT, //1
		INF_FED_TAX_WITHHELD_AMT, //2
		INF_STATE_TAX_WITHHOLDING_TYPE, //3
		INF_STATE_TAX_WITHHELD_PCT, //4
		INF_STATE_TAX_WITHHELD_AMT}; //5
		
	//Begin NBA077
	private String[] AAR_FIELDS = { INF_AAR_AUTO_TRANS_MODE, //0
		INF_AAR_AUTO_TRANS_TYPE, //1
		INF_AAR_AUTO_TRANS_START_DATE, //2
		INF_AAR_AUTO_TRANS_END_DATE, //3
		INF_AAR_AUTO_TRANS_OVRD_AMT, //4
		INF_AAR_AUTO_TRANS_SUSPEND_DATE, //5 
		INF_AAR_AUTO_TRANS_SURRGHROVRD, //6	
		INF_AAR_AUTO_TRANS_START_DAY //7
	};

	private String[] AAR_FUND_FIELDS = { INF_AAR_FUND_ID, //0
		INF_AAR_FUND_ALLOC_VAL, //1
		INF_AAR_FUND_ALLOC_VAL, //2
		INF_AAR_FUND_ALLOC_PCT //3
	};

	//DCA
	private String[] DCA_FIELDS = { INF_DCA_AUTO_TRANS_MODE, //0 
		INF_DCA_AUTO_TRANS_TYPE, //1
		INF_DCA_AUTO_TRANS_START_DATE, //2
		INF_DCA_AUTO_TRANS_END_DATE, //3
		INF_DCA_AUTO_TRANS_OVRD_AMT, //4
		INF_DCA_AUTO_TRANS_SUSPEND_DATE, //5
		INF_DCA_AUTO_TRANS_SURRGHROVRD, //6
		INF_DCA_AUTO_TRANS_START_DAY //7		
	}; 

	private String[] DCA_FUND_FIELDS = { INF_DCA_FUND_ID, //0
		INF_DCA_FUND_ALLOC_VAL, //1
		INF_DCA_FUND_ALLOC_VAL, //2
		INF_DCA_FUND_ALLOC_PCT, //3
		INF_DCA_FUND_ALLOC_FROMTO //4
	};

	//	AWD
	private String[] AWD_FIELDS = { INF_AUTO_MODE, //0 
		INF_AUTO_TRANS_TYPE, //1 
		INF_AUTO_START_DATE, //2 
		INF_AUTO_END_DATE, //3 
		INF_AUTO_TRANS_OVRD_AMT, //4 
		INF_AUTO_TRANS_MAX_DISB_PCT, //5 
		INF_AUTO_TRANS_MAX_DISB_AMT, //6
		INF_AUTO_TRANS_WD_BASIS, //7
		INF_AUTO_TRANS_WD_ALLOC_RULE, //8
		INF_AUTO_TRANS_SUSPEND_DATE, //9
		INF_AUTO_TRANS_MINBALOVRD, //10 
		INF_AUTO_TRANS_SURRGHROVRD, //11
		INF_AUTO_TRANS_PAYEE_INDV, //12
		INF_AUTO_TRANS_POLNUM, //13
		INF_AUTO_FORM, //14
		INF_AUTO_START_DAY //15 
	}; 

	private String[] AWD_FUND_FIELDS = { INF_AWD_FUND_ID, //0 
		INF_AWD_FUND_ALLOC_PCT, //1
		INF_AWD_FUND_ALLOC_TYPE, //2
		INF_AWD_FUND_ALLOC_VAL, //3
		INF_AWD_FUND_ALLOC_FROMTO, //4
	};
	private String[] REINS_FIELDS = {INF_REINS_COUNT, //0
		INF_REINS_PHASE, //1
		INF_REINS_ID, //2
		INF_REINS_RISKBASIS, //3
		INF_REINS_USE, //4
		INF_REINS_AMT}; //5
		
	private String[] TAXREPORTING_FIELDS = { TARGET_TAX_YEAR_RETRIEVE, //0	//NBA104
		TARGET_TAX_AMOUNT_RETRIEVE}; //1	//NBA104
		// NBA104 code deleted
		
	private String[] SUBACCOUNT_FIELDS = {NbaCyberConstants.FUND_ID, //0
		FUND_ALLOC_UNITS, //1
		FUND_ALLOC_VAL, //2
		FUND_ALLOC_PCT, //3
		INF_FUND_ALLOC_TYPE }; //4
			
	private String[] INVESTMENT_FIELDS = {INF_FUND_SYSACT_TYPE, //0
		INF_SUB_ACCOUNT_COUNT }; //1 		
		
	private String[] ATTACHMENTS_FIELDS = { INF_NOTE_PAD_COUNT, //0
		INF_NOTE_PAD_SOURCE, //1
		INF_NOTE_PAD_SEQ, //2
		INF_NOTE_PAD_DATE, //3
		INF_NOTE_PAD_DEPT, //4
		INF_NOTE_PAD_DATA }; //5	
		
	private String[] BANKING_FIELDS = { 
		INF_BILLING_KEY, //0
		INF_BILLING_CRDR_TYPE, //1
		INF_BILLING_ACCOUNT_TYPE, //2
		INF_BILLING_CC_TYPE, //3
		INF_BILLING_CC_EXPDATE, //4
		INF_BILLING_ACCOUNT_NUM, //5
		INF_BILLING_ROUTING_NUM, //6
		INF_BILLING_ACT_HOLDER_NAME1, //7
		INF_BILLING_ACT_HOLDER_NAME2, //8
		INF_BILLING_ACT_HOLDER_NAME3, //9
		INF_BILLING_CONTROL_NUM, //10
		INF_BILLING_CONTROL_EFFDATE, //11
		INF_BILLING_BRANCH_NUM,//12
		INF_BILLING_ACH_IND //13
	}; 
	//end NBA077
	//begin NBA112
	private String[] WRIT_AGENT_BASIC_FIELDS = { WRIT_AGENT_ID, //0
		WRIT_AGENT_AGENCY, //1
		WRIT_AGENT_STATUS, //2 
		WRIT_AGENT_DIVISION  //3	
	};

	private String[] WRIT_AGENT_COMM_FIELDS = { WRIT_AGENT_PREF_COMM, //0   
		WRIT_AGENT_BUS_PHONE, //1 
		WRIT_AGENT_HOM_PHONE, //2  
		WRIT_AGENT_MOB_PHONE, //3  
		WRIT_AGENT_FAX_PHONE, //4  
		WRIT_AGENT_EMAIL_ID   //5 
	};

	private String[] WRIT_AGENT_NAME_FIELDS = { WRIT_AGENT_GENDER, //0  
		WRIT_AGENT_LEGAL_VERBIAGE, //1
		WRIT_AGENT_CORP_NAME, //2 
		WRIT_AGENT_LAST_NAME, //3   
		WRIT_AGENT_FIRST_NAME, //4   
		WRIT_AGENT_MIDDLE_INITIAL, //5   
		WRIT_AGENT_PREFIX, //6
		WRIT_AGENT_SUFFIX  //7
	}; 

	private String[] WRIT_AGENT_ADDRESS_FIELDS = { WRIT_AGENT_ADDRESS_TYPE, //0
		WRIT_AGENT_COUNTRY, //1   
		WRIT_AGENT_ADDRESS_ADDENDUM, //2   
		WRIT_AGENT_ADDRESS_LINE1, //3   
		WRIT_AGENT_ADDRESS_LINE2, //4
		WRIT_AGENT_CITY, //5
		WRIT_AGENT_STATE, //6
		WRIT_AGENT_ZIP //7 
	}; 

	//begin NBA132
	private String[] WRIT_AGENT_AGENCY_FIELDS = {
		WRIT_AGENT_AGENCY_HL  //0
	};
	//end NBA132

	private String[] SERV_AGENT_BASIC_FIELDS = { SERV_AGENT_ID, //0
		SERV_AGENT_AGENCY, //1
		SERV_AGENT_STATUS, //2 
		SERV_AGENT_DIVISION //3
	}; 	

	private String[] SERV_AGENT_COMM_FIELDS = { SERV_AGENT_PREF_COMM, //0   
		SERV_AGENT_BUS_PHONE, //1 
		SERV_AGENT_HOM_PHONE, //2  
		SERV_AGENT_MOB_PHONE, //3  
		SERV_AGENT_FAX_PHONE, //4  
		SERV_AGENT_EMAIL_ID //5
	}; 

	private String[] SERV_AGENT_NAME_FIELDS = { SERV_AGENT_GENDER, //0  
		SERV_AGENT_LEGAL_VERBIAGE, //1
		SERV_AGENT_CORP_NAME, //2   
		SERV_AGENT_LAST_NAME, //3   
		SERV_AGENT_FIRST_NAME, //4   
		SERV_AGENT_MIDDLE_INITIAL, //5   
		SERV_AGENT_PREFIX, //6
		SERV_AGENT_SUFFIX  //7 
	}; 

	private String[] SERV_AGENT_ADDRESS_FIELDS = { SERV_AGENT_ADDRESS_TYPE, //0
		SERV_AGENT_COUNTRY, //1   
		SERV_AGENT_ADDRESS_ADDENDUM, //2   
		SERV_AGENT_ADDRESS_LINE1, //3   
		SERV_AGENT_ADDRESS_LINE2, //4
		SERV_AGENT_CITY, //5
		SERV_AGENT_STATE, //6
		SERV_AGENT_ZIP //7 
	}; 
	//end NBA112
	//begin NBA105
	private String[] PARTY_SEARCH_RESULT_INFO_FIELDS = { PARTY_NAME_RETURNED, //0
		NO_OF_MATCHES, //1   
		NO_OF_ENTRIES_RETURNED //2   
	};

	private String[] PARTY_SEARCH_RESULT_FIELDS = { POL_CARRIER_CODE, //0
		POL_NUMBER, //1   
		POL_STATUS, //2 
		PARTY_ROLE //3  
	};
	//end NBA105
	
	protected String hostResponse = null;
	protected int transType = 0;
	protected int transSubType = 0; //NBA112
	protected long changeSubType = 0; //NBA077
	private String compCode = null;
	private String planType = null;
	private NbaUctData[] genderTable = null;
	private NbaStatesData[] stateTable = null;
	private NbaUctData[] addressTypeTable = null;
	private NbaUctData[] prodTypeTable = null;
	private NbaUctData[] policyStatTable = null;
	private NbaPlansData[] prodCodeTable = null;
	private NbaUctData[] policyModeTable = null;
	private NbaUctData[] carrierCodeTable = null;
	private NbaUctData[] prefixTable = null;
	private NbaUctData[] suffixTable = null;
	private NbaUctData[] maritalStatTable = null;
	private NbaUctData[] occupationTable = null;
	private NbaUctData[] smokerTable = null;
	private NbaUctData[] citizenshipTable = null;
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
	private NbaUctData[] riderTypeCodeTable = null; 
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
	private NbaUctData[] ratingTypeTable = null;
	private NbaUctData[] appInfoTable = null;
	//begin NBA012
	private NbaUctData[] nfoOptionTable = null;
	private NbaUctData[] divTypeTable = null;
	private NbaUctData[] timingForNoticeTable = null;
	private NbaUctData[] deathBenefitOptionTypeTable = null;
	private NbaUctData[] altPremModeFreqTable = null;
	//end NBA012
	private NbaUctData[] noticeTypeTable = null; 
	private NbaUctData[] specialHandlingTable = null; 
	private NbaUctData[] billingOptionTable = null;
	private NbaUctData[] arrangementTypeTable = null;
	//begin NBA077
	private NbaUctData[] statementBasisTable = null;
	private NbaUctData[] changeTypeTable = null;
	private NbaUctData[] unisexCodeTable = null;
	private NbaUctData[] unisexSubTable = null;
	private NbaUctData[] valuationClassTable = null;
	private NbaUctData[] calcMethodTable = null;
	private NbaUctData[] reinsRiskBaseTable = null;
	private NbaUctData[] withCalcMehtodTable = null;
	private NbaUctData[] qualPlanTypeTable = null;
	private NbaUctData[] systematicTypeTable = null;
	private NbaUctData[] tranFramTypeTable  = null;	
	private NbaUctData[] paymentFormTable  = null;
	private NbaUctData[] nbaModeTable  = null;
	private NbaUctData[] acctDebitCreditTypeTable  = null;
	private NbaUctData[] bankAccountTypeTable  = null;
	private NbaUctData[] ccTypeTable  = null;
	private NbaUctData[] govIdStatTable  = null;
	private NbaUctData[] perfCommTable  = null;
	private NbaUctData[] orgFormTable  = null;
	private NbaUctData[] premPayingStatus  = null;	
	//end NBA077
	private NbaUctData[] rateClassTable = null;  //NBAJWM
	private NbaTableAccessor ntsAccess = null;
	//NBA077 code deleted
	

	/**
	 * NbaCyberParser constructor
	 */
	public NbaCyberInforceParser() {
		super();
		ntsAccess = new NbaTableAccessor();
	}
	/**
	 * Checks for duplicate people on a contract
	 * @param currentPerson The current person being used to create party.
	 * @param party The current party being created.
	 * @param flag Number of people present
	 * @param addresses The address on the contract
	 * @param electAddresses The electronic addresses on the contract
	 * @return String duplicate PartyId or false if not duplicate
	 */
	//NBA077 added parameters addresses and electAddresses. Added NbaBaseException
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
						partyTemp = combineDuplicateParties(currentPerson, personTemp, partyTemp, addresses, electAddresses); //NBA077
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
	 * @param addresses The address on the contract
	 * @param electAddresses The electronic addresses on the contract
	 * @return Party Combined party data.
	 */
	//NBA077 added parameters addresses and electAddresses. Added NbaBaseException
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
			if (smokerTable == null) {
				smokerTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_SMOKERSTAT, compCode, "*"));//NBA093
			}
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_PERSON);
			PersonExtension personExt = new PersonExtension();
			personExt.setRateClass(currentPerson[9]); //SPR1778 
			olifeExt.setPersonExtension(personExt);
			personTemp.addOLifEExtension(olifeExt);

		}
		if (partyTemp.getGovtIDTC()< 0) { //NBA077
			partyTemp.setGovtIDTC(currentPerson[11]); 
		}
		if (partyTemp.getGovtID() == null || partyTemp.getGovtID().length() == 0) { //NBA077
			partyTemp.setGovtID(currentPerson[12]); //NBA077 
		}
		
		//begin NBA077
		if (partyTemp.getPhoneCount() == 0) {
			/*
			 * Add a Home Phone Number.
			 */
			if (currentPerson[19].length() != 0) {
				Phone phone = new Phone();
				nbaOLifEId.setId(phone);

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
				nbaOLifEId.setId(phone2);

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
		//end NBA077
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
							== Integer.parseInt(translateStateCode(Addresses[flag2][6])))	
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
							nbaOLifEId.setId(address); //NBA077
							if (addressTypeTable == null) {
								addressTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_ADTYPE, compCode, "*"));
							}

							if (Addresses[flag2][1].length() == 0) { 
								address.setAddressTypeCode(OLI_ADTYPE_HOME);
							} 
							else { 
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
							address.setAddressStateTC(translateStateCode(Addresses[flag2][6])); 
							Addresses[flag2][7] = formatZipCode(Addresses[flag2][7]);
							address.setZip(Addresses[flag2][7]);
							address.setStartDate((formatOLifEDate(Addresses[flag2][9])));
							if (countriesTable == null) {
								countriesTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_NATION, compCode, "*"));
							}
							address.setAddressCountryTC(findOLifeCode(Addresses[flag2][8], countriesTable)); 
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
			int relationCount = olife.getRelationCount();
			int partyCount = olife.getPartyCount();

			int numInstances = getNumInstances(AGENT_COUNT);
			String[][] agentData = getData(numInstances, AGENT_FIELDS);
			numInstances = getNumInstances(AGENT_SUB_COUNT);
			String[][] agentSubData = getData(numInstances, AGENT_SUB_FIELDS);
			String[] agencyName = { AGENCY_NAME, AGENCY_EMAIL_ADDRESS }; 
			String[][] agencyNameData = getData(1, agencyName);
			String[] agentEmailArray = { AGENT_EMAIL_ADDRESS }; 
			String[][] agentEmailData = getData(1, agentEmailArray); 
			String[][] agentPerAddrData = getData(1, AGENT_PER_ADDR); 
			String[][] agentBusAddrData = getData(1, AGENT_BUS_ADDR); 
			ArrayList emailArrayList = new ArrayList();
			ArrayList addressArrayList = new ArrayList(); 

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
					EMailAddress agentEmail = new EMailAddress(); 
					Address agentPersonalAddress = new Address(); 
					Address agentBusinessAddress = new Address(); 
					nbaOLifEId.setId(party); //NBA077					
					party.setPartyTypeCode(1);

					if (flag == 0) {
						person.setFirstName(agentPerAddrData[flag][0]);
						person.setMiddleName(agentPerAddrData[flag][1]);
						person.setLastName(agentPerAddrData[flag][2]);
					} else {
						person.setLastName(agentData[flag][3]);
					}

					PersonOrOrganization personOr = new PersonOrOrganization();
					personOr.setPerson(person);
					party.setPersonOrOrganization(personOr);
					carrAppt.setPartyID(party.getId());
					carrAppt.setCarrierName(agentData[flag][0]);

					while (flag2 < Integer.parseInt(agentData[flag][4])) {	
						if (agentSubData[subNum][1].compareTo("01") == 0) {
							carrAppt.setCompanyProducerID(agentSubData[subNum][0]);
							flag2 = 0;
							break;
						}
						flag2++;
						subNum = subNum + 1;
					}
					totalSubNum = totalSubNum + Integer.parseInt(agentData[flag][4]);	
					subNum = totalSubNum;

					producer.addCarrierAppointment(carrAppt);
					party.setProducer(producer);

					if (flag == 0) {
						// set Party.EmailAddress for Primary Writing Agent
						if (!agentEmailData[flag][0].equals("")) {
							nbaOLifEId.setId(agentEmail); //NBA077
							agentEmail.setEMailType(1);
							agentEmail.setAddrLine(agentEmailData[flag][0]);
							emailArrayList.add(0, agentEmail);
							party.setEMailAddress(emailArrayList);
						}

						// set Party.Address personal address for Primary Writing Agent
						nbaOLifEId.setId(agentPersonalAddress); //NBA077
						agentPersonalAddress.setAddressTypeCode(OLI_ADTYPE_HOME); 
						agentPersonalAddress.setAttentionLine(agentPerAddrData[flag][3]);
						agentPersonalAddress.setLine1(agentPerAddrData[flag][4]);
						agentPersonalAddress.setLine2(agentPerAddrData[flag][5]);
						agentPersonalAddress.setCity(agentPerAddrData[flag][6]);
						agentPersonalAddress.setAddressStateTC(translateStateCode(agentPerAddrData[flag][7])); 
						agentPersonalAddress.setZip(agentPerAddrData[flag][8]);
						addressArrayList.add(0, agentPersonalAddress);

						// set Party.Address business address for Primary Writing Agent
						nbaOLifEId.setId(agentBusinessAddress); //NBA077
						agentBusinessAddress.setAddressTypeCode(OLI_ADTYPE_BUS); 
						agentBusinessAddress.setAttentionLine(agentBusAddrData[flag][0]);
						agentBusinessAddress.setLine1(agentBusAddrData[flag][1]);
						agentBusinessAddress.setLine2(agentBusAddrData[flag][2]);
						agentBusinessAddress.setCity(agentBusAddrData[flag][3]);
						agentBusinessAddress.setAddressStateTC(translateStateCode(agentBusAddrData[flag][4])); 
						agentBusinessAddress.setZip(agentBusAddrData[flag][5]);
						addressArrayList.add(1, agentBusinessAddress);

						party.setAddress(addressArrayList);

					}
					
					Relation relation = new Relation();
					OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_RELATIONPRODUCER);
					RelationProducerExtension prodExt = new RelationProducerExtension();
					relation.setVolumeSharePct(agentData[flag][1]); 
					if (prodSitCodeTable == null) {
						prodSitCodeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLIEXT_LU_SITCODE, compCode, "*"));
					}
					prodExt.setSituationCode(findOLifeCode(agentData[flag][2], prodSitCodeTable));

					olifeExt.setRelationProducerExtension(prodExt);
					relation.addOLifEExtension(olifeExt);
					nbaOLifEId.setId(relation); //NBA077					
					relation.setOriginatingObjectID(holding.getId());
					relation.setRelatedObjectID(party.getId());
					relation.setRelationRoleCode(OLI_REL_PRIMAGENT); 
					relation.setOriginatingObjectType(OLI_HOLDING); 
					relation.setRelatedObjectType(OLI_PARTY); 
					relation.setInterestPercent(agentData[flag][5]);
					olife.addParty(party);
					olife.addRelation(relation);
					partyCount = partyCount + 1;
					relationCount = relationCount + 1;

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
				nbaOLifEId.setId(party); //NBA077				
				party.setPartyTypeCode(OLIX_PARTYTYPE_INDIVIDUAL); 
				person.setLastName(agencyNameData[0][0]);
				PersonOrOrganization personOr = new PersonOrOrganization();
				personOr.setPerson(person);
				party.setPersonOrOrganization(personOr);

				//Begin NBA014
				if (!agencyNameData[0][1].equals("")) {
					EMailAddress agencyEmail = new EMailAddress();
					nbaOLifEId.setId(agencyEmail); //NBA077
					agencyEmail.setEMailType(OLI_EMAIL_BUSINESS); 
					agencyEmail.setAddrLine(agencyNameData[0][1]);
					emailArrayList.clear();
					emailArrayList.add(0, agencyEmail);
					party.setEMailAddress(emailArrayList);
				}
				//End NBA014

				olife.addParty(party);
				//add relation
				Relation relation = new Relation();
				nbaOLifEId.setId(relation); //NBA077
				relation.setOriginatingObjectID(holding.getId());
				relation.setRelatedObjectID(party.getId());
				relation.setRelationRoleCode(OLI_REL_SERVAGENCY); 
				relation.setOriginatingObjectType(OLI_HOLDING); 
				relation.setRelatedObjectType(OLI_PARTY); 
				olife.addRelation(relation);
			}
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_AGENTDATA, e);
		}
		return (olife);
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
			nbaOLifEId.setId(altPremMode); //NBA077
			altPremMode.setPaymentMode("1");
			altPremMode.setPaymentAmt(policyData[46]);
			policy.addAltPremMode(altPremMode);
			policy.setAltPremModeAt(altPremMode, count - 1);
			count++;
		}

		if (policyData[47].length() > 0) {
			altPremMode = new AltPremMode();
			nbaOLifEId.setId(altPremMode); //NBA077
			altPremMode.setPaymentMode("2");
			altPremMode.setPaymentAmt(policyData[47]);
			policy.addAltPremMode(altPremMode);
			policy.setAltPremModeAt(altPremMode, count - 1);
			count++;
		}

		if (policyData[48].length() > 0) {
			altPremMode = new AltPremMode();
			nbaOLifEId.setId(altPremMode); //NBA077
			altPremMode.setPaymentMode("3");
			altPremMode.setPaymentAmt(policyData[48]);
			policy.addAltPremMode(altPremMode);
			policy.setAltPremModeAt(altPremMode, count - 1);
			count++;
		}

		if (policyData[49].length() > 0) {
			altPremMode = new AltPremMode();
			nbaOLifEId.setId(altPremMode); //NBA077			
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
			String[][] annuityData = getData(numCov, ANNUITY_FIELDS); 
			int numCovOpt = getNumInstances(COV_OPT_COUNT);
			String[][] annCovOptData = getData(numCovOpt, ANNUITY_COV_FIELDS);
			String[] currentData = new String[numCov];
			// SPR3290 code deleted
			
			int flag = 0;
			//get base policy information
			while (flag < numCov) {
				currentData = annuityData[flag]; 
				if (currentData[5].compareTo("1") == 0) {
					annuity.setAnnuityKey(currentData[0]);
					//begin NBA012            
					annuity.setInitPaymentAmt(currentData[16]); 
					annuity.setInitDepIntRateCurrent(currentData[17]);
					annuity.setGuarIntRate(currentData[18]);
					annuity.setFirstTaxYear(currentData[19]);
					annuity.setRothIraNetContributionAmt(currentData[20]);
					annuity.setRequestedMaturityDate(currentData[21]);
					//add the Payout Participant
					
					//begin NBA077
					String[][] payoutData = getData(1, PAYOUT_FIELDS);
					Payout payout = new Payout();
					nbaOLifEId.setId(payout);
					payout.setIncomeOption(payoutData[0][1]);
					payout.setNumModalPayouts(payoutData[0][2]);
					payout.setPayoutAmt(payoutData[0][2]);
					payout.setPayoutPct(payoutData[0][3]);
					payout.setPayoutMode(payoutData[0][4]);
					payout.setStartDate(payoutData[0][5]);
					payout.setPayoutForm(payoutData[0][6]);
					payout.setExclusionRatio(payoutData[0][7]);
					payout.setPrimaryReductionPct(payoutData[0][8]);
					payout.setPayoutEndDate(payoutData[0][9]);
					payout.setSecondaryReductionPct(payoutData[0][10]);
					payout.setAdjInvestedAmt(payoutData[0][11]);
					payout.setAssumedInterestRate(payoutData[0][12]);
					//create payout extension
					OLifEExtension olifePayoutExt = NbaTXLife.createOLifEExtension(EXTCODE_PAYOUT);
					PayoutExtension payOutExt = olifePayoutExt.getPayoutExtension();
					payOutExt.setExclusionAmt(payoutData[0][13]);	
					payout.addOLifEExtension(olifePayoutExt);
					if(payoutData[0][14] != null && payoutData[0][14].trim().length() > 0){
						TaxWithholding taxWithholding = new TaxWithholding();
						nbaOLifEId.setId(taxWithholding);
						if (withCalcMehtodTable == null) {
							withCalcMehtodTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_WITHCALCMTH, compCode, "*"));
						}
						taxWithholding.setTaxWithholdingType(findOLifeCode(payoutData[0][14], withCalcMehtodTable));
					}
					
					annuity.addPayout(createParticipant(payout, currentData, olife));
					//end NBA077 
					//check for CovOptions on the base policy
					OLifEExtension olifeAnnExt = NbaTXLife.createOLifEExtension(EXTCODE_ANNUITY);
					AnnuityExtension annuityExtension = new AnnuityExtension(); 
					//NBA066 code deleted
					policy.setEffDate(currentData[10]); 
					annuityExtension.setValuationClassType(currentData[11]);
					annuityExtension.setValuationBaseSeries(currentData[22]);
					annuityExtension.setValuationSubSeries(currentData[23]);
					policy.setTermDate(currentData[24]);	//SPR1986
					if (covStatusTable == null) {
						covStatusTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_COVERAGE_STATUS, compCode, "*"));
					}
					annuityExtension.setAnnuityStatus(findOLifeCode((currentData[12] + currentData[13]), covStatusTable)); //NBA234
					//begin NBA077
					if (qualPlanTypeTable == null) {
						qualPlanTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_QUALPLAN, compCode, "*"));
					}
					annuity.setQualPlanType(findOLifeCode(currentData[25], qualPlanTypeTable));
					annuity.setRequestedMaturityAge(currentData[26]);
					annuity.setRequestedMaturityDur(currentData[27]);
					annuityExtension.setCommissionPlanCode(currentData[28]); //[TODO]NBA077 table TBD
					//end NBA077
					olifeAnnExt.setAnnuityExtension(annuityExtension);
					annuity.addOLifEExtension(olifeAnnExt);
				}
				
				flag++;
			}
			//Set Rider information
			flag = 0;
			while (flag < numCov) {
				currentData = annuityData[flag]; 
				if (currentData[5].compareTo("1") != 0) {
					Rider rider = new Rider();
					currentData = annuityData[flag]; 
					nbaOLifEId.setId(rider); //NBA077						
					rider.setRiderKey(currentData[0]);
					rider.setRiderCode(currentData[2]);
					rider.setTotAmt(currentData[3]);
					rider.setNumberOfUnits(currentData[4]); //NBA077
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
					PolicyExtension policyEx = NbaUtils.getFirstPolicyExtension(policy);
					policyEx.setStatutoryCompanyCode(currentData[3]);
					createParticipant(rider, currentData, olife);  
					
					//add the Annuity Rider Extension
					OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_ANNUITYRIDER);
					AnnuityRiderExtension annuityRiderExtension = olifeExt.getAnnuityRiderExtension();
				
					// NBA104 deleted code
					annuityRiderExtension.setUnitTypeInd(currentData[7]);
					annuityRiderExtension.setLivesType(currentData[8]);
					annuityRiderExtension.setValuationClassType(currentData[11]);
					annuityRiderExtension.setValuationBaseSeries(currentData[22]);
					annuityRiderExtension.setValuationSubSeries(currentData[23]);
					annuityRiderExtension.setCommissionPlanCode(currentData[28]); //[TODO]NBA077 table TBD
					annuityRiderExtension.setPayUpDate(formatOLifEDate(currentData[29])); //NBA077
					//check for coverage options on the Riders
					if (annCovOptData != null) {
						for (int i = 0; i < annCovOptData.length; i++) {
							String[] annuityCovOptionFields = annCovOptData[i];
							if (annuityCovOptionFields[0].compareTo(currentData[0]) == 0) {
								rider.addCovOption(createAnnuityCovOption(annuityCovOptionFields)); 
							}
						}
					}
										
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
	 * Create the Annuity CovOption response.
	 * @param currentData the covOption information returned from host
	 * @return com.csc.fsg.nba.vo.txlife.CovOption
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	private CovOption createAnnuityCovOption(String[] currentData) throws NbaBaseException {
		CovOption covOption = new CovOption();
		if (covOption == null) {
			throw new NbaBaseException("ERROR: Could not create a CovOption object.");
		}
		nbaOLifEId.setId(covOption); //NBA077
		covOption.setCovOptionKey(currentData[0]);
		if (covOptTable == null) {
			covOptTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_OPTTYPE, compCode, "*"));
		}
		covOption.setLifeCovOptTypeCode(findOLifeCode(currentData[1], covOptTable));
		covOption.setOptionAmt(currentData[2]);
		// NBA104 deleted code
		covOption.setEffDate(currentData[7]);
		covOption.setTermDate(currentData[8]);
		// SPR2408 code deleted
		OLifEExtension olifeExtensionCovOption = NbaTXLife.createOLifEExtension(EXTCODE_COVOPTION);
		CovOptionExtension covOptionExtension = new CovOptionExtension();
		covOption.setCovOptionPctInd(currentData[3]);
		if (NON_INHERENT_COVOPTION_IND.equals(currentData[9])) { //NBA143
            covOptionExtension.setSelectionRule(NbaOliConstants.OLI_RIDERSEL_INHERENTMODIFIED); //NBA143
        } //NBA143
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
	 * @param olife The current olife information.
	 * @return Policy policy
	 * @exception throws NbaBaseException.
	 */
	//NBA077 added new parameter olife
	private Policy createApplicationInfo(Policy policy, String[] policyData, OLifE olife) throws NbaBaseException {

		ApplicationInfo appInfo = new ApplicationInfo();
		appInfo.setHOAssignedAppNumber(policyData[62]); //NBA077
		appInfo.setSignedDate(formatOLifEDate(policyData[17]));
		appInfo.setCWAAmt(policyData[18]);
		//begin NBA077 If AppState is not presend default it to primary insured residence state
		if(policyData[30] != null && policyData[30].trim().length() > 0){
			appInfo.setApplicationJurisdiction(translateStateCode(policyData[30]));
		}else{
			appInfo.setApplicationJurisdiction(olife.getPartyAt(0).getResidenceState());
		}
		//end NBA077
		if (appInfoTable == null) {
			appInfoTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_APPTYPE, compCode, "*"));
		}
		appInfo.setApplicationType(findOLifeCode(policyData[28], appInfoTable)); 

		appInfo.setRequestedPolDate(formatOLifEDate(policyData[21]));

		appInfo.setAdditionalInd(policyData[16]); 
		appInfo.setAlternateInd(policyData[25]); 
		appInfo.setApplicationJurisdiction(policy.getJurisdiction());	//NBA077
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
			if (numInstances != 0) {
				String[][] beneData = getData(numInstances, BENE_FIELDS);
				numInstances = getNumInstances(BENE_SUB_REP_COUNT);
				String[][] beneSubData = getData(numInstances, BENE_SUB_FIELDS);

				int flag = 0, flag2 = 0, subNum = 0, totalSubNum = 0;
				// SPR3290 code deleted
				Relation tempRelation = new Relation();

				if (beneData != null) {

					while (flag < beneData.length) {
						flag2 = 0;
						while (flag2 < Integer.parseInt(beneData[flag][1])) {	
							int relatioPosition = 0; //NBA077
							Relation relation = new Relation();
							OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_RELATION);
							RelationExtension relExt = new RelationExtension();

							relExt.setInterestAmount(beneSubData[subNum][3]);
							if (benTypeTable == null) {
								benTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_BENEFICIARY_TYPE, compCode, "*"));
							}
							
							if (benDistTable == null) {
								benDistTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_DISTOPTION, compCode, "*"));
							}
							relExt.setBeneficiaryDistributionOption(findOLifeCode(beneSubData[subNum][5], benDistTable));

							olifeExt.setRelationExtension(relExt);
							relation.addOLifEExtension(olifeExt);
							nbaOLifEId.setId(relation); //NBA077
							//get Originating Party
							int flag3 = 0;
							while (flag3 < relationCount) {
								tempRelation = olife.getRelationAt(flag3);
								if (tempRelation.getRelationKey() != null) {
									if (tempRelation.getOriginatingObjectType() == OLI_HOLDING) { //NBA077
										if (tempRelation.getRelationKey().compareTo(beneData[flag][0]) == 0) {
											relation.setOriginatingObjectID(tempRelation.getRelatedObjectID());
										}
										if (tempRelation.getRelationKey().compareTo(beneSubData[subNum][0]) == 0) {
											relation.setRelatedObjectID(tempRelation.getRelatedObjectID());
											relatioPosition = flag3; //NBA077
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
									
									roleRelCodesTable =
										((NbaRolesRelationData[]) getRolesTable(NbaTableConstants.NBA_ROLES_RELATION, compCode, prodType));
									
								} else {
									roleRelCodesTable = ((NbaRolesRelationData[]) getRolesTable(NbaTableConstants.NBA_ROLES_RELATION, compCode, "*"));
								}
							}

							relation.setRelationRoleCode(findOLifeCode(beneSubData[subNum][0].substring(0, 2), roleRelCodesTable));
							relation.setRelatedRefID(beneSubData[subNum][0].substring(2, 4));
							relation.setOriginatingObjectType(OLI_PARTY); 
							relation.setRelatedObjectType(OLI_PARTY); 
							if (benDesignTable == null) {
								benDesignTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_BENEDESIGNATION, compCode, "*"));
							}
							relation.setBeneficiaryDesignation(findOLifeCode(beneSubData[subNum][1], benDesignTable));

							if ((beneSubData[subNum][4].compareTo(Long.toString(OLI_REL_BENEFICIARY)) == 0) 
								|| (beneSubData[subNum][4].compareTo(Long.toString(OLI_REL_CONTGNTBENE)) == 0) 
								|| (beneSubData[subNum][4].compareTo(Long.toString(OLI_REL_ASSIGNBENE)) == 0) 
								|| (beneSubData[subNum][4].compareTo(Long.toString(OLI_REL_TERTBENE)) == 0)) { 
								relation.setInterestPercent(beneSubData[subNum][3]);
							} else {
								relation.setInterestPercent(beneSubData[subNum][2]);
							}
							relation.setRelationKey(beneSubData[subNum][0]);
							if(relation.getOriginatingObjectID() != null && relation.getRelatedObjectID() != null && relation.getRelationRoleCode() > -1){	//NBA077						
								olife.setRelationAt(relation, relatioPosition); //NBA077
							}
							//[TODO]NBA077
							//olife.addRelation(relation);

							//relationCount++;
							flag2++;
							subNum++;

						}

						totalSubNum = totalSubNum + Integer.parseInt(beneData[flag][1]);	
						subNum = totalSubNum;
						flag++;
					} //end while
				}

			}
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
		
			Life life = lifeSubObj.getLife();

			int flag = 0;

			while (flag < numCov) {
				Coverage coverage = new Coverage();
				currentCoverage = covData[flag];
				
				coverage.setCoverageKey(currentCoverage[1]);
				nbaOLifEId.setId(coverage); //NBA077
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

				//NBA077 set both amount and units
				coverage.setCurrentNumberOfUnits(currentCoverage[10]); //NBA077
				coverage.setPremiumPerUnit(currentCoverage[18]); //NBA077
				coverage.setCurrentAmt(currentCoverage[9]); 
				// NBA104 deleted code
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
					// begin NBAJWM
					if (rateClassTable == null) {
						rateClassTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_RATECLASS, compCode, "*"));
					}
					// end NBAJWM
					while (flag2 < (lifeData.length)) {
						if ((lifeData[flag2][0].compareTo(currentCoverage[0])) == 0) {
							covExt.setRateClass(findOLifeCode(lifeData[flag2][6], rateClassTable));  //NBAJWM
						}
						flag2++;
					}
				}
				//end NBA093
				//begin NBA077
				coverage.setFormNo(currentCoverage[17]);
				covExt.setUnisexOverride(currentCoverage[20]);
				if (unisexCodeTable == null) {
					unisexCodeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLIEXT_LU_UNISEXCODE, compCode, "*"));
				}
				if (unisexSubTable == null) {
					unisexSubTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLIEXT_LU_UNISEXSUB, compCode, "*"));
				}
				covExt.setUnisexCode(findOLifeCode((currentCoverage[21]), unisexCodeTable));
				covExt.setUnisexSubseries(findOLifeCode((currentCoverage[22]), unisexSubTable));
				covExt.setCoverageCeaseDate(formatOLifEDate(currentCoverage[23]));
				//[TODO]NBA077 table does not exist for commissionplancode currentCoverage[24]
				if (valuationClassTable == null) {
					valuationClassTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_VALCLASS, compCode, "*"));
				}
				covExt.setValuationClassType(findOLifeCode((currentCoverage[25]), valuationClassTable));
				covExt.setValuationBaseSeries(currentCoverage[26]);
				covExt.setValuationSubSeries(currentCoverage[27]);
				covExt.setPayUpDate(formatOLifEDate(currentCoverage[28]));
				//end NBA077
				olifeExt.setCoverageExtension(covExt);
				coverage.addOLifEExtension(olifeExt);

				coverage = createLifeParticipant(coverage, currentCoverage, olife);//NBA111
				//add CovOptions for current coverage
				coverage = createCovOptions(coverage, currentCoverage, policyExt, olife); //NBA093
				//NBA111 code deleted
				coverage = createReinsuranceInfo(coverage, currentCoverage, olife);
				//begin NBA012
				//Death Benefit Option only applicable for Coverage Phase 1 and Product Type = 'U'
				//[TODO]NBA077 need to set for first to die(with currentCoverage[19]) also
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
	 * @param policyExt The policy extensions
	 * @param olife  OLifE
	 * @return Coverage coverage
	 * @exception throws NbaBaseException.
	 */
	private Coverage createCovOptions(Coverage coverage, String[] currentCoverage, PolicyExtension policyExt, OLifE olife) throws NbaBaseException {
		//get CovOpt data-Benefits
		int numCovOpt = getNumInstances(COV_OPT_COUNT);
		String[][] covOptData = getData(numCovOpt, COV_OPT_FIELDS);
		// SPR3290 code deleted
		//NBA077 code deleted
		/*associate the benefit based on the benefit phase FSBBPHS 
		and the coverage phase FCVPHASE.  Refer to: XML Holding Inquiry.doc*/
		//check for coverage options
		int flag2 = 0;
		if (covOptData != null) {
			while (flag2 < (covOptData.length)) {
				if ((covOptData[flag2][0].compareTo(currentCoverage[1])) == 0) {
					CovOption covOpt = new CovOption();
					if (covOpt == null) {
						throw new NbaBaseException("ERROR: Could not create an CovOpt object.");
					}
					nbaOLifEId.setId(covOpt); //NBA077
					covOpt.setCovOptionKey(covOptData[flag2][0]);
					if (covOptTable == null) {
						covOptTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_OPTTYPE, compCode, "*"));
					}
					covOpt.setLifeCovOptTypeCode(findOLifeCode(covOptData[flag2][1], covOptTable));
					// NBA104 deleted code
					covOpt.setOptionAmt(covOptData[flag2][3]);
					covOpt.setEffDate(formatOLifEDate(covOptData[flag2][4]));
					covOpt.setTermDate(formatOLifEDate(covOptData[flag2][5]));
					OLifEExtension olifeExtCov = NbaTXLife.createOLifEExtension(EXTCODE_COVOPTION);
					CovOptionExtension covOptExt = new CovOptionExtension();
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
					covOpt.setCovOptionPctInd(covOptData[flag2][8]);
					if (covOptData[flag2][8].compareTo("1") == 0) {
						covOpt.setOptionAmt(covOpt.getAnnualPremAmt()); //[TODO]NBA077 should be Pct
					} else {
						covOpt.setOptionAmt(covOptData[flag2][3]);
					}			
					//begin NBA077
					//dont translate. covOpt.ProductCode contains backend value
					covOpt.setProductCode(covOptData[flag2][1]);
					if (covLivesTable == null) {
						covLivesTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_LIVESTYPE, compCode, "*"));
					}
					//NBA111 Code Deleted
					covOpt.setOptionNumberOfUnits(covOptData[flag2][3]);
					// NBA104 deleted code
					if ("102".equals(covOptData[flag2][13])) {
						covOptExt.setPolicyLevelBenefitInd(true);
					}
					//begin NBA104
					if (calcMethodTable == null) {
						calcMethodTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLIEXT_LU_CALCMETHOD, compCode, "*"));
					}
					if (covOptStatusTable == null) {
						covOptStatusTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_BENEFIT_STATUS, compCode, "*"));
					}
					covOptExt.setIncCalculationMethod(findOLifeCode(covOptData[flag2][14], calcMethodTable));
					covOptExt.setIncAnnPercentage(findOLifeCode(covOptData[flag2][15], covOptStatusTable));
					covOptExt.setIncNoOfYears(covOptData[flag2][16]);
					covOptExt.setPayUpDate(formatOLifEDate(covOptData[flag2][17]));
					//end NBA104
					//end NBA077
					/*In CyberLife, the substandard rating for benefits is carried on the benefit 
					04 segment as a percentage and is only 
					applicable to substandard rating if greater than 100 percent.**Refer to: XML Holding Inquiry.doc*/
					if (Integer.parseInt(covOptData[flag2][9]) > 100) {
						//create substandard Rating
						OLifEExtension olifeExtSub = NbaTXLife.createOLifEExtension(EXTCODE_SUBSTANDARDRATING);
						SubstandardRating substandardRating = new SubstandardRating(); //NBA093
						SubstandardRatingExtension subExt = olifeExtSub.getSubstandardRatingExtension(); //NBA077
						covOpt.setPermPercentageLoading(covOptData[flag2][9]);
						subExt.setRatingStatus(SUB_STAND_ACTIVE_STATUS);
						//begin NBA077
						substandardRating.setSubstandardRatingKey(coverage.getCoverageKey());
						subExt.setEffDate(covOpt.getEffDate());
						subExt.setPermPercentageLoading(covOpt.getPermPercentageLoading());
						//end NBA077
						//NBA093 deleted line
						nbaOLifEId.setId(substandardRating); //NBA093  
						//NBA077 code deleted
						substandardRating.addOLifEExtension(olifeExtSub); //NBA093
						covOpt.addSubstandardRating(substandardRating); //NBA093
					}
					//begin NBA111
					//If the coverage is joint then only check for life participant ids for covoption
					if (coverage.hasLivesType() && NbaUtils.isJointLife(coverage.getLivesType())) {
						if (FLAG_BIT_OFF.equals(covOptData[flag2][11])) { //Checking whether benefit is a joint or single life benefit
							//Set LivestType as single life
							covOpt.setLivesType(NbaOliConstants.OLI_COVLIVES_SINGLE);
							String olifeRoleCode = null;
							LifeParticipant lifePar = null;
							if (covOptData[flag2][19] != null && covOptData[flag2][19].trim().length() > 0) {
								String roleCode = (covOptData[flag2][19].substring(0, 2));
								//Taking out first two characters which represent relation role code in cyberlife
								if (checkForInsurableRole(roleCode)) {
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
						} else {
							covOpt.setLivesType(NbaOliConstants.OLI_COVLIVES_JOINTFTD);
						}
					}
					if(NON_INHERENT_COVOPTION_IND.equals(covOptData[flag2][20])){ //NBA143
					    covOptExt.setSelectionRule(NbaOliConstants.OLI_RIDERSEL_INHERENTMODIFIED); //NBA143   
					} //NBA143
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
						nbaOLifEId.setId(address); //NBA077
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
				LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeAnn = 
					policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(); 
			Life life = lifeAnn.getLife();
			int covCount = life.getCoverageCount();

			while (flag < numEndorse) {

				Endorsement endorsInfo = new Endorsement(); 
				currentEndorsement = endorseData[flag];
				nbaOLifEId.setId(endorsInfo); //NBA077
				
				/*Set the AppliesToContract Boolean to True 
				when the person code and sequence FRDPIDNT on the 82 segment 
				is 0000 which means applies to the contract as a whole. 
				Refer to: XML Holding Inquiry.doc*/

				if (currentEndorsement[0].endsWith("0") == true) {
					endorsInfo.setRelatedObjectType(OLI_POLICY); 
					endorsInfo.setRelatedObjectID(NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId()); 
				} else {
				
					/* set the AppliesToCoverageID reference to the coverage 
					phase referenced if the FRDPHASE is not equal to zero and 
					the use code is equal to zero.
					Refer to: XML Holding Inquiry.doc*/
					if (currentEndorsement[2].compareTo("0") == 0) {
						while (flag2 < covCount) {
							coverage = life.getCoverageAt(flag2);
							if (coverage.getCoverageKey().compareTo(currentEndorsement[1]) == 0) {
								endorsInfo.setRelatedObjectType(OLI_LIFECOVERAGE); 
								endorsInfo.setRelatedObjectID(coverage.getId()); 
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
									endorsInfo.setRelatedObjectType(OLI_COVOPTION); 
									endorsInfo.setRelatedObjectID(covOpt.getId()); 
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
				if (endorseTable == null) {
					endorseTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLIEXT_LU_ENDORSECODES, compCode, "*"));
				}
				endorsInfo.setEndorsementCode(findOLifeCode(currentEndorsement[4], endorseTable));
				endorsInfo.setEndorsementInfo(currentEndorsement[5]); 
				endorsInfo.setEndDate(formatOLifEDate(currentEndorsement[6]));

				int relCount = olife.getRelationCount();
				flag2 = 0;
				Relation relation = new Relation();
				while (flag2 < relCount) {
					relation = olife.getRelationAt(flag2);
					if (relation.getRelationKey().compareTo(currentEndorsement[0]) == 0) {
						endorsInfo.setAppliesToPartyID(relation.getRelatedObjectID()); 
						break;
					}
					flag2++;
				}

				policy.addEndorsement(endorsInfo); 
				policy.setEndorsementAt(endorsInfo, flag); 
				flag++;

			} //end while
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_ENDORSEMENTS, e);
		}
		return (policy); 
	}
	/**
	 * Create the Endorsement Information response for an Annuity.
	 * @param policy the policy information for contract
	 * @param policyExt the policy extension for contract
	 * @param olife the current olife instance
	 * @return the policy extension
	 * @exception NbaBaseException
	 */
	private Policy createEndorsementInfoAnnuity(Policy policy, PolicyExtension policyExt, OLifE olife) throws NbaBaseException { 
		try {
			int numEndorse = getNumInstances(ENDORSEMENT_COUNT);
			String[][] endorseData = getData(numEndorse, ENDORSE_FIELDS);
			String[] currentEndorsement = new String[numEndorse];
			
			Annuity annuity = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity(); 
			int riderCount = annuity.getRiderCount(); 
			for (int e = 0; e < numEndorse; e++) { 
				Endorsement endorsInfo = new Endorsement(); 
				currentEndorsement = endorseData[e];
				nbaOLifEId.setId(endorsInfo); //NBA077
				if (currentEndorsement[0].endsWith("0") == true) {
					endorsInfo.setRelatedObjectType(OLI_POLICY);
					endorsInfo.setRelatedObjectID(NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId());
					
				} else {
					if (currentEndorsement[2].compareTo("0") == 0) {
						String phase = currentEndorsement[1];
						if (annuity.getAnnuityKey().compareTo(phase) == 0) {
							endorsInfo.setRelatedObjectType(OLI_ANNUITY); 
							endorsInfo.setRelatedObjectID(annuity.getId()); 
						} else {
							for (int i = 0; i < riderCount; i++) {
								Rider rider = annuity.getRiderAt(i);
								if (rider.getRiderKey().compareTo(phase) == 0) {
									endorsInfo.setRelatedObjectType(OLI_ANNRIDER); 
									endorsInfo.setRelatedObjectID(rider.getId()); 
									break;
								}
							}
						}
					}
					if (currentEndorsement[3].compareTo("**") != 0) {
						/*if (annuity.getAnnuityKey().compareTo(currentEndorsement[1]) == 0) {
							Long covType = new Long(0);
							AnnuityExtension annuityExtension = NbaUtils.getFirstAnnuityExtension(annuity);
							if (annuityExtension != null) {
								java.util.List covOptions = annuityExtension.getCovOption();
								for (int i = 0; i < covOptions.size(); i++) {
									CovOption covOption = (CovOption) covOptions.get(i);
									if (covOptTable == null) {
										covOptTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_OPTTYPE, compCode, "*"));
									}
									if ((findCyberCode(covType.toString(covOption.getLifeCovOptTypeCode()), covOptTable)).compareTo(currentEndorsement[3]) == 0) {
										endorsInfo.setRelatedObjectType(OLI_COVOPTION); // NBA093
										endorsInfo.setRelatedObjectID(covOption.getId()); //NBA093
										break;
									}
								}
							}
						}*/ 
						for (int i = 0; i < riderCount; i++) {
							Rider rider = annuity.getRiderAt(i);
							if (rider.getRiderKey().compareTo(currentEndorsement[1]) == 0) {
								// SPR3290 code deleted
								
								java.util.List covOptions = rider.getCovOption();
								for (int x = 0; x < covOptions.size(); x++) {
									CovOption covOption = (CovOption) covOptions.get(x);
									if (covOptTable == null) {
										covOptTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_OPTTYPE, compCode, "*"));
									}
									if ((findCyberCode(Long.toString(covOption.getLifeCovOptTypeCode()), covOptTable))	//NBA093
										.compareTo(currentEndorsement[3])
										== 0) {
										endorsInfo.setRelatedObjectType(OLI_COVOPTION); 
										endorsInfo.setRelatedObjectID(covOption.getId()); 
										break;
									}
								}
							}
						}
					}
				}
				if (endorseTable == null) {
					endorseTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLIEXT_LU_ENDORSECODES, compCode, "*"));
				}
				endorsInfo.setEndorsementCode(findOLifeCode(currentEndorsement[4], endorseTable));
				endorsInfo.setEndorsementInfo(currentEndorsement[5]); 
				endorsInfo.setEndDate(formatOLifEDate(currentEndorsement[6]));
				int relationCount = olife.getRelationCount();
				for (int i = 0; i < relationCount; i++) {
					Relation relation = olife.getRelationAt(i);
					if (relation.getRelationKey().compareTo(currentEndorsement[0]) == 0) {
						endorsInfo.setAppliesToPartyID(relation.getRelatedObjectID());
						break;
					}
				}
		
				policy.addEndorsement(endorsInfo); 
				policy.setEndorsementAt(endorsInfo, e); 
			} //end while
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_ENDORSEMENTS, e);
		}
		return (policy);
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
				nbaOLifEId.setId(finActivity); //NBA077
				//finActivity.setFinActivityGrossAmt(currentFinActiv[0]);
				finActivity.setFinActivityDate(formatOLifEDate(currentFinActiv[1]));

				OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_FINANCIALACTIVITY);

				FinancialActivityExtension finActExt = new FinancialActivityExtension();

				if (cwaTable == null) {
					cwaTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_FINACTSUBTYPE, compCode, "*"));
				}

				//NBA093 deleted line
				finActivity.setFinActivityType(findOLifeCode(currentFinActiv[2], cwaTable)); 
				finActivity.setFinActivityGrossAmt(currentFinActiv[0]); 
				finActExt.setErrCorrInd(currentFinActiv[3]); 
				finActivity.setFinActivityDate(formatOLifEDate(currentFinActiv[4]));
				finActivity.setUserCode(currentFinActiv[5]); 
				finActivity.setAccountingActivityType(currentFinActiv[6]); 
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
	private Holding createHolding_Response(String[] policyData, String[] holdingData) throws NbaBaseException {

		/*
		 * Create a Holding object.
		 */
		/* There will be one holding object and the HoldingTypeCode will always be "2" - Policy**
		Refer to: XML Holding Inquiry.doc*/

		Holding holding = new Holding();
		if (holding == null) {
			throw new NbaBaseException("ERROR: Could not create a Holding object.");
		}

		nbaOLifEId.setId(holding); //NBA077
		holding.setHoldingTypeCode(2);
		holding.setLastAnniversaryDate(holdingData[0]);
		holding.setAssignmentCode(holdingData[1]);
		holding.setQualifiedCode(holdingData[2]);
		holding.setRestrictionCode(holdingData[3]);
		//begin NBA077
		OLifEExtension olifeExtCov = NbaTXLife.createOLifEExtension(EXTCODE_HOLDING);
		HoldingExtension holdingExt = olifeExtCov.getHoldingExtension();
		holdingExt.setLastAccountingDate(formatOLifEDate(holdingData[4]));
		holding.addOLifEExtension(olifeExtCov);
		int taxReportingCount = getNumInstances(INF_TAX_REPORTING_COUNT);		
		String[][] taxReportingData = getData(taxReportingCount, TAXREPORTING_FIELDS);
		for (int i = 0; i < taxReportingCount; i++) {
			// NBA104 code deleted
			TaxReporting taxReporting = new TaxReporting();
			nbaOLifEId.setId(taxReporting);
			taxReporting.setTaxYear(taxReportingData[i][0]);
			taxReporting.setTaxableAmt(taxReportingData[i][1]);
			holdingExt.addTaxReporting(taxReporting);
			// NBA104 code deleted		
		}		
		//end NBA077 
		//needs to be an extention
		//holding.setLastAccountingDate(holdingData[4]);

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
		boolean isJointCoverage = NbaUtils.isJointLife(Long.valueOf(currentCoverage[8]).longValue());//NBA111
		int flag2 = 0;
		if (lifeData != null) {
			while (flag2 < (lifeData.length)) {
				//NBA111 Code deleted.
				if (((lifeData[flag2][0].compareTo(currentCoverage[0])) == 0)
					|| (isJointCoverage && lifeData[flag2][0].startsWith(JOINT_INS))) { //NBA111 If coverage is joint and lifeparticipant id starts with "01" then this life participant should be ties to the coverage as joint insured.

					LifeParticipant lifePar = new LifeParticipant();
					//NBA111 Code deleted
					nbaOLifEId.setId(lifePar); //NBA077
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
					if (smokerTable == null) {
						smokerTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_SMOKERSTAT, compCode, "*"));//NBA093
					}

					int relCount = olife.getRelationCount();
					int flag3 = 0;
					Relation relation = new Relation();
					while (flag3 < relCount) {
						relation = olife.getRelationAt(flag3);
						if (relation.getRelationKey().compareTo(lifeData[flag2][0]) == 0) {//NBA111
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
	 * Creates the Reinsurance Info response.
	 * @param coverage The current coverage for contract.
	 * @param currentCoverage The coverage information returned from host.
	 * @param olife The current olife information.
	 * @return Coverage coverage
	 * @exception throws NbaBaseException.
	 */
	private Coverage createReinsuranceInfo(Coverage coverage, String[] currentCoverage, OLifE olife) throws NbaBaseException {

		/*search through the returned reinsurance control segments (10 segment) 
		for ones that have coverage.  The reinsurance phase code (FRECPHS) 
		consists of coverage unique identifier.  The adapter will use this 
		identifier to associate the Reinsurance Info object with the correct 
		coverage object(s).  */

		int numReins = getNumInstances(INF_REINS_COUNT);
		String[][] riensData = getData(numReins, REINS_FIELDS);
		
		int flag1 = 0;
		if (riensData != null) {
			while (flag1 < (riensData.length)) {
				if ((riensData[flag1][1].compareTo(currentCoverage[1])) == 0) {
					ReinsuranceInfo reinsInfo = new ReinsuranceInfo();
					if (reinsInfo == null) {
						throw new NbaBaseException("ERROR: Could not create an Reinsurance Info object.");
					}
					nbaOLifEId.setId(reinsInfo); //NBA077
					reinsInfo.setReinsurersTreatyIdent(riensData[flag1][2]);
					if (reinsRiskBaseTable == null) {
						reinsRiskBaseTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_REINRISKBASE, compCode, "*"));
										}
					reinsInfo.setReinsuranceRiskBasis(findOLifeCode(riensData[flag1][3], reinsRiskBaseTable));
					if(riensData[flag1][4].equals("1")){
						reinsInfo.setReinsuredAmt(riensData[flag1][5]);
					}else{
						reinsInfo.setRetentionAmt(riensData[flag1][5]);
					}
					coverage.addReinsuranceInfo(reinsInfo);
				}
				flag1++;
			}
		}

		return (coverage);
	}

	/**
	* Creates the LifeUsa object.
	* @param life The current life information
	* @return Life life
	* @exception throws NbaBaseException.
	*/
	private Life createLifeUSA(Life life) throws NbaBaseException {

		try {
			//get coverage data
		
			// SPR3290 code deleted

			LifeUSA lifeUSA = new LifeUSA();

			life.setLifeUSA(lifeUSA);
		

		
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_COVERAGES, e);
		}

		return (life);
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

			SystemMessage messDetail = new SystemMessage(); 
			currentMessage = messData[flag];
			nbaOLifEId.setId(messDetail); //NBA077
			messDetail.setRelatedObjectID(currentMessage[0]); 
			messDetail.setMessageStartDate(formatOLifEDate(currentMessage[1]));
			messDetail.setMessageCode(currentMessage[2]); 
			messDetail.setMessageDescription(currentMessage[3]); 
			if (msgSeverityTable == null) {
				msgSeverityTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_MSGSEVERITY, compCode, "*"));	//NBA093
			}
			messDetail.setMessageSeverityCode(findOLifeCode(currentMessage[4], msgSeverityTable)); 

			messDetail.setSystemMessageKey(currentMessage[5]); 
			messDetail.setSequence(currentMessage[6]); 

			holding.addSystemMessage(messDetail);
			//NBA077 code deleted 
			flag++;

		} //end while

		return (holding); //NBA093
	}
	
	/**
	 * Creates the Investment response.
	 * @param holding The holding information for contract.
	 * @return Holding holding
	 * @exception throws NbaBaseException.
	 */
	//NBA077 New Method
	private Holding createInvestment(Holding holding) throws NbaBaseException {

		int numInvest = getNumInstances(INF_ALLOC_COUNT);
		String[][] investData = getData(numInvest, INVESTMENT_FIELDS);
		//String[] currentInvestmentData = new String[numInvest];

		int fieldPosition = 0;
		int investFlag = 0;
		Investment investment = new Investment();
		holding.setInvestment(investment);
		while (investFlag < numInvest) {
			String[] currentInvestmentData = investData[investFlag];
			int numSubAcctInstances = 0;
			if(currentInvestmentData[1] != null && currentInvestmentData[1].trim().length() > 0){
				numSubAcctInstances = Integer.parseInt(currentInvestmentData[1]);
			}
			String[][] subAccountData = getData(fieldPosition, numSubAcctInstances , SUBACCOUNT_FIELDS);
			if (subAccountData != null) {
				int flag = 0;
				while (flag < numSubAcctInstances) {
					SubAccount subAccount = new SubAccount();
					String[] currentSubAccount = subAccountData[flag];

					nbaOLifEId.setId(subAccount);
					subAccount.setProductCode(currentSubAccount[0]);
					subAccount.setCurrNumberUnits(currentSubAccount[1]);
					subAccount.setTotValue(currentSubAccount[2]);
					subAccount.setAllocPercent(currentSubAccount[3]);
					subAccount.setAllocationAmt(currentSubAccount[2]);
					if (systematicTypeTable == null) {
						systematicTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_SYSTEMATIC, compCode, "*")); //NBA093
					}
					subAccount.setSystematicActivityType(findOLifeCode(currentInvestmentData[0], systematicTypeTable));

					OLifEExtension oliExt = NbaTXLife.createOLifEExtension(EXTCODE_SUBACCOUNT);
					SubAccountExtension subAcctExt = oliExt.getSubAccountExtension();
					if (tranFramTypeTable == null) {
						tranFramTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_TRNSFRAMTTYPE, compCode, "*")); //NBA093
					}
					subAcctExt.setAllocType(findOLifeCode(currentSubAccount[4], tranFramTypeTable));
					subAccount.addOLifEExtension(oliExt);

					investment.addSubAccount(subAccount);

					fieldPosition++;
					flag++;
				}
			}
			investFlag++;
		} //end while

		return (holding); 
	}

	/**
	 * Creates the Attachment response.
	 * @param holding The holding information for contract.
	 * @return Holding holding
	 * @exception throws NbaBaseException.
	 */
	//NBA077 New Method
	private Holding createAttachments(Holding holding) throws NbaBaseException {

		int numAttach = getNumInstances(INF_NOTE_PAD_COUNT);
		String[][] attachData = getData(numAttach, ATTACHMENTS_FIELDS);
		String[] currentAttachment = new String[numAttach];

		int flag = 0;
		while (flag < numAttach) {
			currentAttachment = attachData[flag];
			if(currentAttachment[1] != null && currentAttachment[1].trim().equals("P")){
				Attachment attachment = new Attachment();
				nbaOLifEId.setId(attachment);
				attachment.setDateCreated(formatOLifEDate(currentAttachment[3]));
				attachment.setUserCode(currentAttachment[4]);
				AttachmentData data = new AttachmentData();
				attachment.setAttachmentData(data);
				data.setPCDATA(currentAttachment[5]);
				holding.addAttachment(attachment);				
			}
			flag++;
		} //end while

		return (holding); 
	}

	/**
	 * Creates the Banking response.
	 * @param holding The holding information for contract.
	 * @return Holding holding
	 * @exception throws NbaBaseException.
	 */
	//NBA077 New Method
	private Holding createBanking(Holding holding) throws NbaBaseException {
		
		int numBanking = getNumInstances(INF_BILLING_COUNT);
		String[][] bankingData = getData(numBanking, BANKING_FIELDS);
		int flag = 0;
		while (flag < numBanking) {
			String[] currentBanking = bankingData[flag];

			Banking banking = new Banking();
			nbaOLifEId.setId(banking);
			banking.setBankingKey(currentBanking[0]);
			
			if (acctDebitCreditTypeTable == null) {
				acctDebitCreditTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_ACCTDBCRTYPE, compCode, "*"));
			}
			if (bankAccountTypeTable == null) {
				bankAccountTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_BANKACCTTYPE, compCode, "*"));
			}
			if (ccTypeTable == null) {
				ccTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_CREDCARDTYPE, compCode, "*"));
			}
			banking.setCreditDebitType(findOLifeCode(currentBanking[1],acctDebitCreditTypeTable));
			banking.setBankAcctType(findOLifeCode(currentBanking[2],bankAccountTypeTable));
			//begin SPR2151
			//Use Bank Account type to differentiate between EFT and Credit Card payment types
			Policy policy = holding.getPolicy();
			if (policy.getPaymentMethod() == OLI_PAYMETH_ETRANS || policy.getPaymentMethod() == OLI_PAYMETH_CREDCARD) {
				if (banking.getBankAcctType() == OLI_BANKACCT_CREDCARD) {
					policy.setPaymentMethod(OLI_PAYMETH_CREDCARD);
				} else {
					policy.setPaymentMethod(OLI_PAYMETH_ETRANS);
				}
			}
			//end SPR2151
			banking.setCreditCardType(findOLifeCode(currentBanking[3],ccTypeTable));
			banking.setCreditCardExpDate(formatExpireYYYYMM(currentBanking[4]));		//SPR2151
			banking.setAccountNumber(currentBanking[5]);
			banking.setAccountNumber(currentBanking[5]);
			banking.setRoutingNum(currentBanking[6]);
			
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_BANKING);	
			BankingExtension bankingExt = olifeExt.getBankingExtension();
			banking.addOLifEExtension(olifeExt);
			
			AccountHolderNameCC acctHolderCC = new AccountHolderNameCC();
			if (currentBanking[7] != null && currentBanking[7].length() > 0) {
				acctHolderCC.addAccountHolderName(currentBanking[7]);
			}
			if (currentBanking[8] != null && currentBanking[8].length() > 0) {
				acctHolderCC.addAccountHolderName(currentBanking[8]);
			}
			if (currentBanking[9] != null && currentBanking[9].length() > 0) {
				acctHolderCC.addAccountHolderName(currentBanking[9]);
			}
			bankingExt.setAccountHolderNameCC(acctHolderCC);
			bankingExt.setBillControlNumber(currentBanking[10]);
			bankingExt.setBillControlEffDate(formatOLifEDate(currentBanking[11]));
			bankingExt.setBranchNumber(currentBanking[12]);
			bankingExt.setAchInd(currentBanking[13]);
				
			holding.addBanking(banking);			
			flag++;
		} //end while

		return (holding); 
	}


	/**
	* Creates the arrangement response.
	* @param holding The current holding information for contract.
	* @param olife The current olife information for contract.
	* @return Holding holding
	* @exception throws NbaBaseException.
	*/
	//NBA077 New Method
	private Holding createArrangements(Holding holding, OLifE olife) throws NbaBaseException {
	
		try {
			
			int numARRfunds = getNumInstances(INF_AAR_FUND_COUNT);
			int numDCAfunds = getNumInstances(INF_DCA_FUND_COUNT);
			int numAWDfunds = getNumInstances(INF_AWD_FUND_COUNT);
			
			String[][] AAR_arrangement = getData(1, AAR_FIELDS);
			String[][] AAR_fund_arrangement = getData(numARRfunds, AAR_FUND_FIELDS);
			String[][] DCA_arrangement = getData(1, DCA_FIELDS);
			String[][] DCA_fund_arrangement = getData(numDCAfunds, DCA_FUND_FIELDS);
			String[][] AWD_arrangement = getData(1, AWD_FIELDS); 
			String[][] AWD_fund_arrangement = getData(numAWDfunds, AWD_FUND_FIELDS);
			
			Map subAccounts = new HashMap();
			Investment investment = holding.getInvestment();
			for (int i = investment.getSubAccountCount() - 1 ; i >= 0; i--) {
				SubAccount subAccount = investment.getSubAccountAt(i);
				subAccounts.put((subAccount.getSystematicActivityType() + subAccount.getProductCode()), subAccount.getId());
			}
							
			//NBA077 code deleted
			if (!AAR_arrangement[0][1].equals("")){
				Arrangement arrangement = new Arrangement();				
				// NBA077 code deleted
				nbaOLifEId.setId(arrangement);
					
				if (arrangementTypeTable == null) {
					arrangementTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_ARRTYPE, compCode, "*"));
				}
				arrangement.setArrType(findOLifeCode(AAR_arrangement[0][1], arrangementTypeTable));
				if (policyModeTable == null) {
					policyModeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_PAYMODE, compCode, "*"));
				}
				if (nbaModeTable == null) {
					nbaModeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_AAR_MODES, compCode, "*"));
				}

				if(arrangement.getArrType() == OLI_ARRTYPE_ASSALLO || arrangement.getArrType() == OLI_ARRTYPE_AA){
					arrangement.setArrMode(findOLifeCode(AAR_arrangement[0][0], nbaModeTable));
				}else{
					arrangement.setArrMode(findOLifeCode(AAR_arrangement[0][0], policyModeTable));
				}
				
				if (AAR_arrangement[0][2] != null
					&& AAR_arrangement[0][2].length() == 6
					&& AAR_arrangement[0][7] != null
					&& AAR_arrangement[0][7].length() > 0) {
					arrangement.setStartDate(
						formatOLifEDate(
							AAR_arrangement[0][2].substring(2, 6)
								+ AAR_arrangement[0][2].substring(0, 2)
								+ (AAR_arrangement[0][7].length() == 2 ? AAR_arrangement[0][7] : "0" + AAR_arrangement[0][7])));
				}
				arrangement.setEndDate(formatOLifEDate(AAR_arrangement[0][3]));
				arrangement.setSurrenderChargeAmt(AAR_arrangement[0][4]);
				
				OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_ARRANGEMENT);	
				ArrangementExtension arrangementEx = olifeExt.getArrangementExtension();
				
				arrangementEx.setSuspendDate(formatOLifEDate(AAR_arrangement[0][5]));
				arrangementEx.setSurrChgOvrdInd(AAR_arrangement[0][6]);
				arrangement.addOLifEExtension(olifeExt);
				
				for (int j=0 ; numARRfunds > j ; j++){
					ArrDestination arrDestination = new ArrDestination();
					nbaOLifEId.setId(arrDestination);
					if (tranFramTypeTable == null) {
						tranFramTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_TRNSFRAMTTYPE, compCode, "*")); //NBA093
					}
					arrDestination.setTransferAmtType(findOLifeCode(AAR_fund_arrangement[j][1], tranFramTypeTable));
					arrDestination.setTransferAmt(AAR_fund_arrangement[j][2]);
					arrDestination.setTransferPct(AAR_fund_arrangement[j][3]);
					arrDestination.setSubAcctID((String)subAccounts.get(OLI_SYSACTTYPE_ASSETREALLOC + AAR_fund_arrangement[j][0]));
					arrangement.addArrDestination(arrDestination);					
				}
				holding.addArrangement(arrangement);
			}
			
			if (!DCA_arrangement[0][1].equals("")){
				Arrangement arrangement = new Arrangement();
				nbaOLifEId.setId(arrangement);
				if (arrangementTypeTable == null) {
					arrangementTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_ARRTYPE, compCode, "*"));
				}
				arrangement.setArrType(findOLifeCode(DCA_arrangement[0][1], arrangementTypeTable));
				if (policyModeTable == null) {
					policyModeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_PAYMODE, compCode, "*"));
				}
				arrangement.setArrMode(findOLifeCode(DCA_arrangement[0][0], policyModeTable));
				if (DCA_arrangement[0][2] != null
					&& DCA_arrangement[0][2].length() == 6
					&& DCA_arrangement[0][7] != null
					&& DCA_arrangement[0][7].length() > 0) {
					arrangement.setStartDate(
						formatOLifEDate(
							DCA_arrangement[0][2].substring(2, 6)
								+ DCA_arrangement[0][2].substring(0, 2)
								+ (DCA_arrangement[0][7].length() == 2 ? DCA_arrangement[0][7] : "0" + DCA_arrangement[0][7])));
				}
				arrangement.setEndDate(formatOLifEDate(DCA_arrangement[0][3]));
				arrangement.setSurrenderChargeAmt(DCA_arrangement[0][4]);

				OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_ARRANGEMENT);	
				ArrangementExtension arrangementEx = olifeExt.getArrangementExtension();

				arrangementEx.setSuspendDate(formatOLifEDate(DCA_arrangement[0][5]));
				arrangementEx.setSurrChgOvrdInd(DCA_arrangement[0][6]);
				arrangement.addOLifEExtension(olifeExt);
				
				for (int j=0 ; numDCAfunds > j ; j++){
					if ("F".equals(DCA_fund_arrangement[j][4])) {
						ArrSource arrSource = new ArrSource();
						nbaOLifEId.setId(arrSource);
						if (tranFramTypeTable == null) {
							tranFramTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_TRNSFRAMTTYPE, compCode, "*")); //NBA093
						}
						arrSource.setTransferAmtType(findOLifeCode(DCA_fund_arrangement[j][1], tranFramTypeTable));
						arrSource.setTransferAmt(DCA_fund_arrangement[j][2]);
						arrSource.setTransferPct(DCA_fund_arrangement[j][3]);
						arrSource.setSubAcctID((String)subAccounts.get(OLI_SYSACTTYPE_DOLLARCOSTAVG + DCA_fund_arrangement[j][0]));
						arrangement.addArrSource(arrSource);
					} else {
						ArrDestination arrDestination = new ArrDestination();
						nbaOLifEId.setId(arrDestination);
						if (tranFramTypeTable == null) {
							tranFramTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_TRNSFRAMTTYPE, compCode, "*")); //NBA093
						}
						arrDestination.setTransferAmtType(findOLifeCode(DCA_fund_arrangement[j][1], tranFramTypeTable));
						arrDestination.setTransferAmt(DCA_fund_arrangement[j][2]);
						arrDestination.setTransferPct(DCA_fund_arrangement[j][3]);
						arrDestination.setSubAcctID((String)subAccounts.get(OLI_SYSACTTYPE_DOLLARCOSTAVG + DCA_fund_arrangement[j][0]));
						arrangement.addArrDestination(arrDestination);
					}
				}
				holding.addArrangement(arrangement);
			}
			
			if (!AWD_arrangement[0][1].equals("")){
				Arrangement arrangement = new Arrangement();				
				nbaOLifEId.setId(arrangement);
					
				if (arrangementTypeTable == null) {
					arrangementTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_ARRTYPE, compCode, "*"));
				}
				arrangement.setArrType(findOLifeCode(AWD_arrangement[0][1], arrangementTypeTable));
				if (policyModeTable == null) {
					policyModeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_PAYMODE, compCode, "*"));
				}
				arrangement.setArrMode(findOLifeCode(AWD_arrangement[0][0], policyModeTable));
				if (AWD_arrangement[0][2] != null
					&& AWD_arrangement[0][2].length() == 6
					&& AWD_arrangement[0][15] != null
					&& AWD_arrangement[0][15].length() > 0) {
					arrangement.setStartDate(
						formatOLifEDate(
							AWD_arrangement[0][2].substring(2, 6)
								+ AWD_arrangement[0][2].substring(0, 2)
								+ (AWD_arrangement[0][15].length() == 2 ? AWD_arrangement[0][15] : "0" + AWD_arrangement[0][15])));
				}
				arrangement.setEndDate(formatOLifEDate(AWD_arrangement[0][3]));
				arrangement.setSurrenderChargeAmt(AWD_arrangement[0][4]);
				OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_ARRANGEMENT);	
				ArrangementExtension arrangementEx = olifeExt.getArrangementExtension();
				MaxDisbursePctOrMaxDisburseAmt maxDisburse = new MaxDisbursePctOrMaxDisburseAmt();
				if(AWD_arrangement[0][5] != null && AWD_arrangement[0][5].trim().length() > 0){
					maxDisburse.setMaxDisbursePct(AWD_arrangement[0][5]);
				}else{
					maxDisburse.setMaxDisburseAmt(AWD_arrangement[0][6]);
				}
				arrangementEx.setMaxDisbursePctOrMaxDisburseAmt(maxDisburse);
				arrangementEx.setWithdrawalBasis(AWD_arrangement[0][7]);
				arrangementEx.setWithdrawalAllocationRule(AWD_arrangement[0][8]);
				if(AWD_arrangement[0][2] != null && AWD_arrangement[0][2].trim().length() > 0){
					arrangementEx.setSetToIssDateInd(true);
				}
				arrangementEx.setSuspendDate(formatOLifEDate(AWD_arrangement[0][9]));
				arrangementEx.setMinBalOvrdInd(AWD_arrangement[0][10]);
				arrangementEx.setSurrChgOvrdInd(AWD_arrangement[0][11]);
				arrangement.addOLifEExtension(olifeExt);

				for (int j=0 ; numAWDfunds > j ; j++){
				if ("F".equals(AWD_fund_arrangement[j][4])) {
					ArrSource arrSource = new ArrSource();
					nbaOLifEId.setId(arrSource);
					if (tranFramTypeTable == null) {
						tranFramTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_TRNSFRAMTTYPE, compCode, "*")); //NBA093
					}
					arrSource.setTransferAmtType(findOLifeCode(AWD_fund_arrangement[j][1], tranFramTypeTable));
					arrSource.setTransferAmt(AWD_fund_arrangement[j][2]);
					arrSource.setTransferPct(AWD_fund_arrangement[j][3]);
					arrSource.setSubAcctID((String)subAccounts.get(OLI_SYSACTTYPE_DOLLARCOSTAVG + AWD_fund_arrangement[j][0]));
					arrangement.addArrSource(arrSource);
				} else {
					ArrDestination arrDestination = new ArrDestination();
					nbaOLifEId.setId(arrDestination);
					if (tranFramTypeTable == null) {
						tranFramTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_TRNSFRAMTTYPE, compCode, "*")); //NBA093
					}
					arrDestination.setTransferAmtType(findOLifeCode(AWD_fund_arrangement[j][1], tranFramTypeTable));
					arrDestination.setTransferAmt(AWD_fund_arrangement[j][2]);
					arrDestination.setTransferPct(AWD_fund_arrangement[j][3]);
					arrDestination.setSubAcctID((String)subAccounts.get(OLI_SYSACTTYPE_DOLLARCOSTAVG + AWD_fund_arrangement[j][0]));
					if(AWD_arrangement[0][13] != null && AWD_arrangement[0][13].trim().length() > 0){
						arrDestination.setPolNumber(AWD_arrangement[0][13]);
					}else{
						String relationKey = null;
						if(AWD_arrangement[0][12] != null && AWD_arrangement[0][12].trim().length() > 2){
							relationKey = AWD_arrangement[0][12];
						}else{
							relationKey = "1001";
						}
						Relation relation = new Relation();
						for (int r = 0; r < olife.getRelationCount(); r++) {
							relation = olife.getRelationAt(r);
							if (relation.getRelationKey() != null && relation.getRelationKey().compareTo(relationKey) == 0) {
								arrDestination.setPaymentPartyID(relation.getRelatedObjectID());
								break;
							}
						}
					}
					
					OLifEExtension olifeArrDestExt = NbaTXLife.createOLifEExtension(EXTCODE_ARRDESTINATION);	
					ArrDestinationExtension arrDestEx = olifeArrDestExt.getArrDestinationExtension();
					if (paymentFormTable == null) {
						paymentFormTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_PAYMENTFORM, compCode, "*")); 
					}
					arrDestEx.setPaymentForm(findOLifeCode(AWD_arrangement[0][14], paymentFormTable));
					arrDestination.addOLifEExtension(olifeArrDestExt);
					arrangement.addArrDestination(arrDestination);
				}
			}
								
			//Tax Witholding information
			String[][] taxWH = getData(1, TAX_FIELDS);
				 //Get federal taxes first
				 if (!taxWH[0][0].equals("")){
					TaxWithholding taxWithholding = new TaxWithholding();
					nbaOLifEId.setId(taxWithholding);
					taxWithholding.setTaxWithholdingPlace(OLI_TAXPLACE_FEDERAL);
					if (withCalcMehtodTable == null) {
						withCalcMehtodTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_WITHCALCMTH, compCode, "*"));
					}
					taxWithholding.setTaxWithholdingType(findOLifeCode(taxWH[0][0], withCalcMehtodTable));					
					taxWithholding.setTaxWithheldPct(taxWH[0][1]);
					taxWithholding.setTaxWithheldAmt(taxWH[0][2]); 
					arrangement.addTaxWithholding(taxWithholding);
				 }
				//get State withholdings 
				if (!taxWH[0][3].equals("")){
					TaxWithholding taxWithholding = new TaxWithholding();
					nbaOLifEId.setId(taxWithholding);
					taxWithholding.setTaxWithholdingPlace(OLI_TAXPLACE_JURISDICTION);
					if (withCalcMehtodTable == null) {
						withCalcMehtodTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_WITHCALCMTH, compCode, "*"));
					}
					taxWithholding.setTaxWithholdingType(findOLifeCode(taxWH[0][3], withCalcMehtodTable));
					taxWithholding.setTaxWithheldPct(taxWH[0][4]);
					taxWithholding.setTaxWithheldAmt(taxWH[0][5]); 
					arrangement.addTaxWithholding(taxWithholding);
				 }
				holding.addArrangement(arrangement);
			}

		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_COVERAGES, e);
		}
	
		return (holding);
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
		nbaOLifEId = new NbaOLifEId(olife); //NBA077
		/*
		 * Create Holding.
		 */
		//Only one instance of Policy information
		String[][] holdingData = getData(1,HOLDING_FIELDS);
		String[][] policyData = getData(1, POLICY_FIELDS);
		//set the company code
		compCode = policyData[0][1];
		//set the product type
		planType = policyData[0][2].trim() + policyData[0][3].trim(); 
		Holding holding = createHolding_Response(policyData[0],holdingData[0]);
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
		//begin NBA077
		/*
		 * Add Arrangements, Banking, Investment and Attachments 
		 */
		
		holding = createInvestment(holding); 
		holding = createAttachments(holding); 
		holding = createArrangements(holding, olife); 	
		holding = createBanking(holding); 
		//end NBA077

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
					nbaOLifEId.setId(participant); //NBA077
					participant.setParticipantName(participantFields[2] + ", " + participantFields[3] + " " + participantFields[4]);
					if (roleCodesTable == null) {
						if (planType.substring(0, 1).compareTo("F") == 0) {
							if (prodTypeTable == null) {
								prodTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_POLPROD, compCode, "*"));
							}
							String prodType = findOLifeCode(planType, prodTypeTable);
							roleCodesTable = ((NbaRolesParticipantData[]) getRolesTable(NbaTableConstants.NBA_ROLES_PARTICIPANT, compCode, prodType));
						}
					}
					participant.setParticipantRoleCode(findOLifeCode(currentData[1].substring(0, 2), roleCodesTable));
					participant.setIssueAge(currentData[9]);
					if (genderTable == null) {
						genderTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_GENDER, compCode, "*"));
					}
					participant.setIssueGender(findOLifeCode(participantFields[5], genderTable));
					if (smokerTable == null) {
						smokerTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_SMOKERSTAT, compCode, "*"));
					}
					
					Relation relation = new Relation();
					for (int r = 0; r < olife.getRelationCount(); r++) {
						relation = olife.getRelationAt(r);
						if (relation.getRelationKey().compareTo(currentData[1]) == 0) {
							participant.setPartyID(relation.getRelatedObjectID());							
							break;
						}
					}
					rider.addParticipant(participant); 
				}
			}
		}
		return rider;  
	}
	/**
	 * Create the Participant response.
	 * @param payout the current coverage for contract
	 * @param currentData the coverage information returned from host
	 * @param olife the current olife information
	 * @return com.csc.fsg.nba.vo.txlife.Payout
	 * @exception throws NbaBaseException
	 */
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
		//NBA077 code deleted
		String[][] participantData = getData(numberOfParticipants, LIFE_PAR_FIELDS);
		if (participantData != null) {
			for (int i = 0; i < participantData.length; i++) {
				String[] participantFields = participantData[i];
				if (participantFields[0].compareTo(currentData[1]) == 0) {
					Participant participant = new Participant();
					if (participant == null) {
						throw new NbaBaseException("ERROR: Could not create a Participant object.");
					}
					nbaOLifEId.setId(participant); //NBA077
					participant.setParticipantName(participantFields[2] + ", " + participantFields[3] + " " + participantFields[4]);
					if (roleCodesTable == null) {
						if (planType.substring(0, 1).compareTo("F") == 0) {
							if (prodTypeTable == null) {
								prodTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_POLPROD, compCode, "*"));
							}
							String prodType = findOLifeCode(planType, prodTypeTable);
							roleCodesTable = ((NbaRolesParticipantData[]) getRolesTable(NbaTableConstants.NBA_ROLES_PARTICIPANT, compCode, prodType));
						}
					}
					participant.setParticipantRoleCode(findOLifeCode(currentData[1].substring(0, 2), roleCodesTable));
					participant.setIssueAge(currentData[9]);
					if (genderTable == null) {
						genderTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_GENDER, compCode, "*"));
					}
					participant.setIssueGender(findOLifeCode(participantFields[5], genderTable));
					if (smokerTable == null) {
						smokerTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_SMOKERSTAT, compCode, "*"));//NBA093
					}
					
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
					duplicate = checkForDuplicatePeople(currentPerson, olife, olife.getPartyCount(),addresses, electAddresses); //NBA077
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
					duplicate = checkForDuplicatePeople(currentPerson, olife, olife.getPartyCount(), addresses, electAddresses); //NBA077
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
					duplicate = checkForDuplicatePeople(currentPerson, olife, olife.getPartyCount(), addresses, electAddresses); //NBA077
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
					duplicate = checkForDuplicatePeople(currentPerson, olife, olife.getPartyCount(), addresses, electAddresses); //NBA077
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
					duplicate = checkForDuplicatePeople(currentPerson, olife, olife.getPartyCount(), addresses, electAddresses); //NBA077
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
		nbaOLifEId.setId(party); //NBA077		
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
		party.setGovtIDTC(findOLifeCode(currentPerson[11], govIdTypeTable)); 

		//begin NBA077
		party.setPartyKey(currentPerson[24]);
		if (govIdStatTable == null) {
			govIdStatTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_GOVTIDSTAT, compCode, "*"));
		}
		if (perfCommTable == null) {
			perfCommTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_PREFCOMM, compCode, "*"));
		}
		party.setGovtIDStat(findOLifeCode(currentPerson[25], govIdStatTable));
		party.setResidenceCounty(currentPerson[26]);
		party.setPrefComm(findOLifeCode(currentPerson[27], perfCommTable));
		//end NBA077
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
			if (smokerTable == null) {
				smokerTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_SMOKERSTAT, compCode, "*"));
			}
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
			//begin NBA077
			if (citizenshipTable == null) {
				citizenshipTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_CITIZENSHIP, compCode, "*"));
			}
			person.setCitizenship(findOLifeCode(currentPerson[28], citizenshipTable));
			//end NBA077
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
			//begin NBA077
			if (orgFormTable == null) {
				orgFormTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_ORGFORM, compCode, "*"));
			}
			organ.setOrgForm(findOLifeCode(currentPerson[29], orgFormTable));
			//end NBA077			

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
		if (currentPerson[19].length() != 0) {
			Phone phone = new Phone();
			if (phone == null) {
				throw new NbaBaseException("ERROR: Could not create a Phone object.");
			}
			nbaOLifEId.setId(phone); //NBA077
			
			phone.setPhoneTypeCode(OLI_PHONETYPE_HOME);
			//begin NBA077
			if(currentPerson[19].length() == 10){
				phone.setAreaCode(currentPerson[19].substring(0,3));
				phone.setDialNumber(currentPerson[19].substring(3));
			}else{
				phone.setDialNumber(currentPerson[19]);
			}
			//end NBA077			
			party.addPhone(phone);
		}
		/*
		 * Add a Second Phone Number if present.
		 */
		if (currentPerson[21].length() != 0) {
			Phone phone2 = new Phone();
			if (phone2 == null) {
				throw new NbaBaseException("ERROR: Could not create a Phone object.");
			}
			nbaOLifEId.setId(phone2); //NBA077			

			if (phoneTypeTable == null) {
				phoneTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_PHONETYPE, compCode, "*"));
			}
			phone2.setPhoneTypeCode(findOLifeCode(currentPerson[20], phoneTypeTable));
			//begin NBA077
			if (currentPerson[21].length() == 10) {
				phone2.setAreaCode(currentPerson[21].substring(0, 3));
				phone2.setDialNumber(currentPerson[21].substring(3));
			} else {
				phone2.setDialNumber(currentPerson[21]);
			}
			//end NBA077
			party.addPhone(phone2);
		}
		//begin NBA077
		OLifEExtension oliExt = NbaTXLife.createOLifEExtension(EXTCODE_PARTY);
		PartyExtension partyExt = oliExt.getPartyExtension();
		party.addOLifEExtension(oliExt);
		//add federal taxes first
		if (!currentPerson[30].equals("")) {
			TaxWithholding taxWithholding = new TaxWithholding();
			nbaOLifEId.setId(taxWithholding);
			taxWithholding.setTaxWithholdingPlace(OLI_TAXPLACE_FEDERAL);
			if (withCalcMehtodTable == null) {
				withCalcMehtodTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_WITHCALCMTH, compCode, "*"));
			}
			taxWithholding.setTaxWithholdingType(findOLifeCode(currentPerson[30], withCalcMehtodTable));
			taxWithholding.setWithholdingNumExemptions(currentPerson[33]);
			partyExt.addTaxWithholding(taxWithholding);
		}
		//add State withholdings 
		if (!currentPerson[31].equals("")) {
			TaxWithholding taxWithholding = new TaxWithholding();
			nbaOLifEId.setId(taxWithholding);
			taxWithholding.setTaxWithholdingPlace(OLI_TAXPLACE_JURISDICTION);
			if (withCalcMehtodTable == null) {
				withCalcMehtodTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_WITHCALCMTH, compCode, "*"));
			}
			taxWithholding.setTaxWithholdingType(findOLifeCode(currentPerson[31], withCalcMehtodTable));
			taxWithholding.setWithholdingNumDependents(currentPerson[32]);
			taxWithholding.setWithholdingNumExemptions(currentPerson[34]);
			partyExt.addTaxWithholding(taxWithholding);
		}
		//end NBA077 
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
			nbaOLifEId.setId(policy); //NBA077
			policy.setPolNumber(policyData[0]);
			String polType = policyData[2].trim() + policyData[3].trim();
			if (prodTypeTable == null) {
				prodTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_POLPROD, compCode, "*"));
			}

			policy.setLineOfBusiness(policyData[50]);
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

			policy.setBillNumber(policyData[31]); 
			policy.setBilledToDate(policyData[32]); 
			policy.setPaidToDate(policyData[51]);
			OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_POLICY);
			policy.addOLifEExtension(olifeExt); //SPR2155
			PolicyExtension policyExt = olifeExt.getPolicyExtension();

			//Add the Issue Company and Admin Company        
			policyExt.setIssueCompanyCode(policyData[34]);
			policy.setAdministeringCarrierCode(policyData[35]);
			//begin NBA077
			if (policyStatTable== null) {
				policyStatTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_POLSTAT, compCode, "*"));
			} 
			//If FBRSTAT is 99 than concat FBRENTCD (BES Value of 2nd Position) 
			if("99".equals(policyData[63])){
				if(policyData[70] != null && policyData[70].length() > 1){
					policyData[63] = policyData[63] + policyData[70].substring(1);
				}
			} 
			policy.setPolicyStatus(findOLifeCode(policyData[63], policyStatTable));
			policy.setEffDate(formatOLifEDate(policyData[64]));
			//end NBA077
			//If the billing use code FBRDUEUS is zero, both the use code and 
			//the due day FBRDUEDY will be ignored and the corresponding XML tags will 
			//not be created
			if (policyData[36] != null && !policyData[36].equals("0")) {
				if (timingForNoticeTable == null) {
					timingForNoticeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLIEXT_LU_TIMING, compCode, "*"));
				}
				//SPR2084 code deleted
				policyExt.setTiming(findOLifeCode(policyData[36], timingForNoticeTable)); 
				policy.setPaymentDraftDay(policyData[33]);
				//SPR2084 code deleted
			}
			//begin SPR2084
			if (noticeTypeTable == null) {
				noticeTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_NOTICETYPE, compCode, "*"));
			}
			if (specialHandlingTable == null) {
				specialHandlingTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_SPECIALHANDLING, compCode, "*"));
			}
			policy.setLastNoticeDate(policyData[52]);
			policy.setLastNoticeType(findOLifeCode(policyData[83], noticeTypeTable));
			policy.setSpecialHandling(findOLifeCode(policyData[53], specialHandlingTable)); //NBA077 
			//end SPR2084
			
			//begin NBA077
			if(NbaUtils.isTraditional(policy)){
				policy.setGracePeriodEndDate(formatOLifEDate(policyData[80]));
			}else{
				policy.setGracePeriodEndDate(formatOLifEDate(policyData[81]));
			}
			if (statementBasisTable == null) {
				statementBasisTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_STMTBASIS, compCode, "*"));
			}
			policy.setStatementBasis(findOLifeCode(policyData[65], statementBasisTable));
			policyExt.setPaidUpAdditionsOptionElected(policyData[66]); //[TODO]NBA077 ISL only
			// NBA104 code deleted
			policyExt.setPlannedAdditionalPremium(policyData[68]);
			// NBA104 code deleted
			if (premPayingStatus == null) {
				premPayingStatus = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_PremPayingStatus, compCode, "*"));
			}		
			policyExt.setPendingPremiumPayingStatus(findOLifeCode(policyData[69], premPayingStatus));
			if (changeTypeTable == null) {
				changeTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLIEXT_LU_CHANGETYPE, compCode, "*"));
			}
			policyExt.setContractChangeType(findOLifeCode(policyData[70], changeTypeTable));
			// NBA104 code deleted
			policyExt.setInitialAnnualPremiumAmt(policyData[67]);	//NBA104
			// NBA104 code deleted
			policyExt.setFirstMonthlyDate(formatOLifEDate(policyData[71]));
			policyExt.setLoansInd(policyData[76]); 
			policyExt.setSuspendInd(policyData[77]);
			policyExt.setExcessCollectedAmt(policyData[78]);
			if(policyData[82] != null && policyData[82].trim().length() > 0){
				policyExt.setReinstateAmt(policyData[82]);
			}else{
				policyExt.setReinstateAmt(policyData[79]);
			}
		  	
			//end NBA077
			
			if (billingOptionTable == null) {
				billingOptionTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLIEXT_LU_BILLOPT, compCode, "*"));
			}
			if (policyData[54] != null)
				policyExt.setStatementFreq(findOLifeCode(policyData[54], policyModeTable));
			if (policyData[55] != null){
				policyExt.setConfirmationFreq(findOLifeCode(policyData[55], policyModeTable));
				policyExt.setBillingOption(findOLifeCode(policyData[55], billingOptionTable));
			}	
			//Special Frequency Fields
			policyExt.setQuotedPremiumBasisAmt(policyData[37]);
			policyExt.setQuotedPremiumBasisFrequency(policyData[38]);
			policyExt.setNonStandardPaidToDate(policyData[57]);
			policyExt.setPayrollFrequency(policyData[58]);
			policyExt.setFirstSkipMonth(policyData[59]);
			policyExt.setInitialBillToDate(policyData[60]);

			
			/*
			 * Add the Policy Indicators.
			 */
			 //NBA077
			policyExt.setAssignmentInd(policyData[12]);
			policyExt.setEndorsementInd(policyData[13]);
			policyExt.setOtherInsuredInd(policyData[24]);
			policyExt.setBeneficiaryInd(policyData[14]);
			policyExt.setRatedInd(policyData[15]);
	
			

			// NBA093 deleted 2 lines
			policyExt.setAgentErrorsInd(policyData[26]);

			/*
			 * Add the ApplicationInfo object.
			 */
			policy = createApplicationInfo(policy, policyData, olife); //NBA077

			/*
			 * Add the Financial Activity object.
			 */
			policy = createFinancialActivity(policy);

			// do not need requirements for inforce
			//policy = createRequirementInfo(policy, olife);

			LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeAnnDisSubObj = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
		
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
				//NBA077 code deleted
				annuity = createAnnuity(annuity, olife, policy); 

				lifeAnnDisSubObj.setAnnuity(annuity);
				policy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(lifeAnnDisSubObj); 
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
				//NBA077 code deleted
				//begin NBA077
				//set both amount and units
				life.setFaceUnits(policyData[72]);
				life.setFaceAmt(policyData[29]);
				//end NBA077
				lifeAnnDisSubObj.setLife(life);

				
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
				
				olifeExt = null;
				olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_LIFE);
				life.addOLifEExtension(olifeExt); //SPR2155
				LifeExtension lifeExt = olifeExt.getLifeExtension();		
				lifeExt.setSecondaryDividendType(policyData[61]);
				//begin NBA077
				
				lifeExt.setRequestedMaturityAge(policyData[73]);
				lifeExt.setRequestedMaturityDur(policyData[74]);
				//end NBA077
				life = createLifeUSA(life);
				
				policy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(lifeAnnDisSubObj); 

				/*
				 * Add a Coverage objects.
				 */
				policy = createCoverages(policy, olife, policyExt);

				policy = createSubstandardRatings(olife, policy); 

				/*
				* Add the Endorsement Information.
				*/
				policy = createEndorsementInfo(policy, policyExt, olife); 
			}
			// policy.setLifeOrAnnuityOrDisabilityHealth(lifeAnnDisSubObj);
			//SPR2155 code deleted
			
			/*
			 *Add the AltPremMode Object
			 */
			policy = createAltPremMode(policy, policyData); 

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
			if (planType.substring(0, 1).compareTo("F") == 0) {
				if (prodTypeTable == null) {
					prodTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_POLPROD, compCode, "*"));
				}
				String prodType = findOLifeCode(planType, prodTypeTable);
				roleRelCodesTable = ((NbaRolesRelationData[]) getRolesTable(NbaTableConstants.NBA_ROLES_RELATION, compCode, prodType));
			} else {
				roleRelCodesTable = ((NbaRolesRelationData[]) getRolesTable(NbaTableConstants.NBA_ROLES_RELATION, compCode, "*"));
			}
		}

		Relation relation = new Relation();
		if (relation == null) {
			throw new NbaBaseException("ERROR: Could not create a Relation object.");
		}
		nbaOLifEId.setId(relation); //NBA077
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
				nbaOLifEId.setId(reqInfo); //NBA077
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
				reqInfo.setRequestedDate(formatOLifEDate(currentReqInfo[4]));

				OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(EXTCODE_REQUIREMENTINFO);

				RequirementInfoExtension reqInfoExt = new RequirementInfoExtension();

				if (ReqRestrictionsTable == null) {
					ReqRestrictionsTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_REQRESTRICTION, compCode, "*")); //SPR3171
				}
				reqInfo.setRestrictIssueCode(findOLifeCode(currentReqInfo[5], ReqRestrictionsTable));
				reqInfo.setUserCode(currentReqInfo[6]); 
				reqInfo.setSequence(currentReqInfo[7]); 

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
	private Policy createSubstandardRatings(OLifE olife, Policy policy) throws NbaBaseException { 

		
		try {
			//get substandard data
			int numSub = getNumInstances(SUB_STAND_COUNT);
			String[][] subData = getData(numSub, SUB_STAND_FIELDS);
			String[] currentSub = new String[numSub];
			LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeSubObj = 
				policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(); 
			Life life = lifeSubObj.getLife();
			NbaOLifEId nbaOLifEId = new NbaOLifEId(olife); 

			int coverageCount = life.getCoverageCount();
			// SPR3290 code deleted
			int flag = 0,  flag2 = 0;//NBA111 Code deleted
			// SPR3290 code deleted
			Coverage coverage = new Coverage();

			while (flag < numSub) {

				OLifEExtension olifeExtSub = NbaTXLife.createOLifEExtension(EXTCODE_SUBSTANDARDRATING);
				SubstandardRating substandardRating = new SubstandardRating(); 
				substandardRating.addOLifEExtension(olifeExtSub); 
				SubstandardRatingExtension subExt = olifeExtSub.getSubstandardRatingExtension(); 
				currentSub = subData[flag];

				/*CyberLife creates a phase code 102 for ratings on a life.  
				The returned segment with this phase code will be ignored by the adapter. 
				Refer to: XML Holding Inquiry.doc*/
				if (currentSub[1].compareTo("102") == 0) {
					flag++;
				} else {
					//NBA093 deleted line
					nbaOLifEId.setId(substandardRating); //NBA093  
					//check if relates to Coverage or Party
					//NBA093  Ratings no longer supported for Parties deleted 27 lines
					subExt.setExtraPremSubtype(currentSub[3]);		

					if (tableRatingTable == null) {
						tableRatingTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_RATINGS, compCode, "*"));
					}
					
					if (currentSub[4].length() > 0) {
						if (currentSub[2].charAt(0) == SUB_STAND_TYPE_PERM_TABLE) {
							substandardRating.setPermTableRating(findOLifeCode(currentSub[4], tableRatingTable)); //NBA077
						} else if (currentSub[2].charAt(0) == SUB_STAND_TYPE_TEMP_TABLE) {
							substandardRating.setTempTableRating(findOLifeCode(currentSub[4], tableRatingTable)); //NBA077
							substandardRating.setTempTableRatingEndDate(formatOLifEDate(currentSub[7])); //NBA077
						}
					}

					
					//begin NBA077 
					if (currentSub[6].length() > 0) {
						if (currentSub[2].charAt(0) == SUB_STAND_TYPE_TEMP_FLAT) {
							substandardRating.setTempFlatExtraAmt(currentSub[6]); 
							substandardRating.setTempFlatEndDate(formatOLifEDate(currentSub[7]));
						} else if (currentSub[2].charAt(0) == SUB_STAND_TYPE_PERM_FLAT) { 							
							subExt.setPermFlatExtraAmt(currentSub[6]);
						}
					}
					//end NBA077

					subExt.setEffDate(formatOLifEDate(currentSub[8])); 
					if (ratingReasonTable == null) {
						ratingReasonTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_RATING_REASON, compCode, "*"));
					}
					substandardRating.setRatingReason(findOLifeCode(currentSub[9], ratingReasonTable)); 
					if (ratingCommissionTable == null) {
						ratingCommissionTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLIEXT_LU_COMMISCODE, compCode, "*"));
					}
					substandardRating.setRatingCommissionRule(findOLifeCode(currentSub[10], ratingCommissionTable)); //NBA093
					//NBA077 Code deleted. 
					subExt.setExtraPremPerUnit(currentSub[12]); 

					if (ratingStatusTable == null) {
						ratingStatusTable = ((NbaUctData[]) getUctTable(NbaTableConstants.NBA_ACTIVE_SEGMENT_STATUS, compCode, "*"));
					}
					subExt.setRatingStatus(findOLifeCode(currentSub[13], ratingStatusTable)); 
					subExt.setAnnualPremAmt(currentSub[14]); 
					flag2 = 0;
					while (flag2 < coverageCount) {
						coverage = life.getCoverageAt(flag2);
						if (coverage.getCoverageKey().compareTo(currentSub[1]) == 0) {
							substandardRating.setSubstandardRatingKey(coverage.getCoverageKey()); //NBA077
							//NBA111 code deleted
							//begin NBA111
							String roleCode = (currentSub[0].substring(0, 2));
							if (roleCodesTable == null) {
								roleCodesTable = ((NbaRolesParticipantData[]) getRolesTable(NbaTableConstants.NBA_ROLES_PARTICIPANT, compCode, "*"));
							}
							String olifeRoleCode = findOLifeCode(roleCode, roleCodesTable);
							LifeParticipant lifePar = NbaUtils.getLifeParticipantWithRoleCode(coverage, Long.valueOf(olifeRoleCode).longValue());
							if (lifePar != null) {
								lifePar.addSubstandardRating(substandardRating); //NBA093
							}
							//end NBA111
						}
						flag2++;
					}

					flag++;
				}
			}
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_SUBSTAND, e);
		}

		return (policy);
	}
	/**
	 * Create a TXLife response.
	 * @param txLife Current TXLife response.
	 * @return TXLife
	 * @exception NbaBaseException
	 */
	protected TXLife createTXLife_Response(TXLife txLife, TXLife txLifeRequest) throws NbaBaseException { //nba073

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

			response.setTXLifeResponseAt((createTXLifeResponse(response,txLifeRequest)), 0); //nba073
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
	private TXLifeResponse createTXLifeResponse(UserAuthResponseAndTXLifeResponseAndTXLifeNotify Response,TXLife txLifeRequest) throws NbaBaseException { //nba073

		TXLifeResponse txLifeResponse = Response.getTXLifeResponseAt(0);
		if (txLifeResponse == null) {
			throw new NbaBaseException("ERROR: Could not create a TXLifeResponse object.");
		}

		/*
		 * Add a TransResult object.
		 */
		TransResult transResult = new TransResult();
		// NBA104 code deleted	

		//nba073
		if (transType == NbaOliConstants.TC_TYPE_PRODUCER) { //NBA112  //SPR2992
			txLifeResponse.setTransResult(getAgentTransResult(transResult));
		} else if (transType == NEW_APP && transSubType == NbaOliConstants.TC_SUBTYPE_NEWBUSSUBMISSION && changeSubType == NbaOliConstants.NBA_CHNGTYPE_INCREASE) { //NBA077
			txLifeResponse.setTransResult(getTransResultForIncrease(transResult)); //NBA077
		} else {
			txLifeResponse.setTransResult(getTransResult(transResult));
		}
		//nba073	
	
		if (transResult.getResultCode() > 1) {
			return (txLifeResponse);
		}

		/*
		 * Create the OLifE object.
		 */
		if (transType == HOLDING) {
			//NBA103 code deleted
				txLifeResponse.setOLifE(createOLifE_Response(txLifeResponse));
			//NBA103 code deleted

		}
		//begin NBA112
		//begin NBA132
		if (transType == NbaOliConstants.TC_TYPE_PRODUCER) {
		    if (transSubType == NbaOliConstants.TC_TYPE_PRODUCERSUBTYPE_AGENTRETRIEVE) {  //SPR2992
				txLifeResponse.setOLifE(createOLifE_Response_ForAgentInfo(txLifeRequest));
		    } else if (transSubType == NbaOliConstants.TC_TYPE_PRODUCERSUBTYPE_AGENTVALIDATION) {
		        txLifeResponse.setOLifE(createOLifE_Response_ForAgentVal(txLifeRequest));
		    }
		}
		//end NBA132
		//end NBA112
		
		//begin NBA105
		if (transType == NbaOliConstants.TC_TYPE_PARTYINQ) {
		    //begin SPR2737
		    OLifE olife = createOLifE_Response_ForPartyInquiry(txLifeRequest);
		    //if party inquiry returned atleast 1 contract and the Holding object count is not equal match count, this means atleast 
		    //one holding inquiry transaction failed. Set a result code 2 (success with info) in such a case.
		    int matchCount = getPartyInqMatchCount();
		    if (matchCount > 0 && matchCount != olife.getHoldingCount()) {
		        transResult.setResultCode(NbaOliConstants.TC_RESCODE_SUCCESSINFO);
		    }
			txLifeResponse.setOLifE(olife); //NBA124
			//end SPR2737
		}
		//end NBA105		
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
		txLife = createTXLife_Response(txLife, txLifeRequest); //nba073

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
			if (olifeValue != null && olifeValue.length() != 0) { //NBA077
				for (int i = 0; i < table.length; i++) {
					if (table[i].getIndexValue().compareToIgnoreCase(olifeValue) == 0) {
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
			if (cyberValue != null && cyberValue.length() != 0) { //NBA077
				for (int i = 0; i < table.length; i++) {
					if (table[i].besValue != null && table[i].besValue.compareToIgnoreCase(cyberValue) == 0) { //NBA077
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
			if (cyberValue != null && cyberValue.length() != 0) { //NBA077
				for (int i = 0; i < table.length; i++) {
					if (table[i].besValue != null && table[i].besValue.compareToIgnoreCase(cyberValue) == 0) { //NBA077
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
			if (cyberValue != null && cyberValue.length() != 0) { //NBA077
				for (int i = 0; i < table.length; i++) {
					if (table[i].besValue != null && table[i].besValue.compareToIgnoreCase(cyberValue) == 0) { //NBA077
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
			if (cyberValue != null && cyberValue.length() != 0) { //NBA077
				String value = cyberValue.trim(); //NBA077
				for (int i = 0; i < table.length; i++) {
					if (table[i].besValue != null && table[i].besValue.compareToIgnoreCase(value) == 0) { //NBA077
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
					if (table[i].coverageKey.compareToIgnoreCase(cyberValue) == 0) {
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
		if (date == null || date.length() < 8) {
			return null; //NBA077
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
	 * Parses through the host response and pulls out the requested data.
	 * @param beginPosition The index of field where it will search for value. It should be 0 for first instance
	 * @param numInstances The number of instances of segment data.
	 * @param fieldNames The host values to populate data for.
	 * @param fieldNames java.lang.String[]
	 * @return itemArray Parsed data
	 */
	//NBA077 New Method
	protected String[][] getData(int beginPosition, int numInstances, String[] fieldNames) {
		int beginIndex = 0, endIndex = 0, flag = 0, field = 0; // SPR3290
		// SPR3290 code deleted

		int numFields = fieldNames.length;
		int totalNumInstances = beginPosition + numInstances;
		if (numInstances > 0) {

			String[][] itemArray = new String[numInstances][numFields];
			while (field < numFields) {
				while (flag < totalNumInstances) {
					beginIndex = hostResponse.indexOf(fieldNames[field] + "=", beginIndex);
					if (beginIndex == -1) {
						flag++;
					} else {
						beginIndex = hostResponse.indexOf("=", beginIndex);
						endIndex = hostResponse.indexOf(";", beginIndex);
						if (flag >= beginPosition) {
							itemArray[flag - beginPosition][field] = hostResponse.substring(beginIndex + 1, endIndex);
						}
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
			//begin NBA077
			String stringValue = hostResponse.substring(beginIndex + 1, endIndex);
			if(stringValue.trim().length() > 0){
				numInstances = Integer.parseInt(stringValue);	//NBA093
			}
			//end NBA077
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
		aCase.put(C_SYSTEM_ID, NbaConstants.SYST_CYBERLIFE); 

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
		aCase.put(C_SYSTEM_ID, NbaConstants.SYST_CYBERLIFE); 

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

		} catch (NbaDataAccessException e) {
			NbaLogFactory.getLogger(this.getClass()).logException(e);	
		}		
		return (tArray);
	}
	/**
	 * Gets the transaction results for the 514 Agent transaction
	 * @param transResult 
	 * @return TransResult
	 */
	protected TransResult getAgentTransResult(TransResult transResult) {
		int beginIndex = 0, endIndex = 0;
		int newIndex = 0;
		// SPR3290 code deleted
		int leastResult = 0;
		int currentTransResult = 0, currentAgentResult= 0, agentResult = 0;
		//Begin NBA112
		int HOST_FAILURE = 2;
		int errIndex = 0;
		StringBuffer agtInfoError = new StringBuffer(); 
		//End NBA112
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
			currentTransResult = Integer.parseInt(hostResponse.substring(beginIndex + 1, endIndex));	
			//set Result Code and check to see if any error messages
			//Begin NBA112
			if (currentTransResult == leastResult) {
				transResult.setResultCode(1); 
			} else if (currentTransResult == HOST_FAILURE) {
				agentResult = currentTransResult;
				transResult.setResultCode(2);
			} else { 
				transResult.setResultCode(5); 
			} 
			//End NBA112
			//NBA112 code deleted
			while (hostResponse.length() > beginIndex) {
				newIndex = hostResponse.indexOf("PUAGVLEM=", beginIndex);
				if (newIndex == -1) {
					break;
				} else {
					ResultInfo resultInfo = new ResultInfo();
					resultInfo.setResultInfoCode("999");
					endIndex = hostResponse.indexOf(";", newIndex);
					String currentError = hostResponse.substring(newIndex + 9, endIndex);
					try {
						currentAgentResult = Integer.parseInt(currentError.substring(0,1));
					}catch(NumberFormatException e) {
					}
					if (currentAgentResult > agentResult)
						agentResult = currentAgentResult;
						
					errorDesc = "";
					errorDesc = currentError;
					if (hostResponse.length() == endIndex + 1) {
						resultInfo.setResultInfoDesc(errorDesc);
						beginIndex = endIndex;
						transResult.addResultInfo(resultInfo);

						break;
					}
					if (hostResponse.substring(endIndex + 1, endIndex + 10).compareTo("PUAGVLEM=") == 0) {
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
			//Begin NBA112
			errIndex = hostResponse.indexOf("ERR=", beginIndex);
			if (errIndex != -1) {
				ResultInfo resultInfo = new ResultInfo();
				int endErrIndex = hostResponse.indexOf(";", errIndex);
				agtInfoError.append(hostResponse.substring(errIndex + 4, errIndex + 6));
				agtInfoError.append(hostResponse.substring(errIndex + 11, endErrIndex));
				resultInfo.setResultInfoDesc(agtInfoError.toString());
				transResult.addResultInfo(resultInfo);
			}
			//End NBA112
		}
		
		if (currentTransResult > agentResult) {
			transResult.setResultCode(5);
		} else if (agentResult > leastResult) {
			transResult.setResultCode(2);
		} else {
			transResult.setResultCode(1);
		}
		return transResult;

	}
	
	/**
	 * Calls the translation tables for UCT Tables
	 * @param tableName The name of the UCT table.
	 * @param compCode Company code.
	 * @param covKey Coverage key(pdfKey).
	 * @return tarray NbaTableData.
	 */
	private NbaTableData[] getUctTable(String tableName, String compCode, String covKey) {

		HashMap aCase = new HashMap();
		aCase.put(C_COMPANY_CODE, compCode);
		aCase.put(C_TABLE_NAME, tableName);
		aCase.put(C_COVERAGE_KEY, covKey);
		aCase.put(C_SYSTEM_ID, NbaConstants.SYST_CYBERLIFE); 

		NbaTableData[] tArray = null;
		try {
			tArray = ntsAccess.getDisplayData(aCase, tableName);
		} catch (NbaDataAccessException e) {}

		return (tArray);
	}

	/**
	 * Gets the transaction results
	 * @param transResult
	 * @return TransResult
	 */
	protected TransResult getTransResult(TransResult transResult) {
		int beginIndex = 0, endIndex = 0;
		int newIndex = 0;
		// SPR3290 code deleted
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
			currentTransResult = Integer.parseInt(hostResponse.substring(beginIndex + 1, endIndex));	
			//set Result Code and check to see if any error messages
			//begin SPR2737
			switch (currentTransResult) {
            	case SUCCESS:
            	case SUCCESS_FORCIBLE:
            	    transResult.setResultCode(NbaOliConstants.TC_RESCODE_SUCCESS);
            	    break;
            	case DATA_FAILURE:
            	case TRANSACTION_FAILURE:
            	case HOST_UNAVAILABLE:
            	case HOST_ABEND:
            	case BAD_INFO_RETURNED:
            	default:
            	    transResult.setResultCode(NbaOliConstants.TC_RESCODE_FAILURE);
            	    break;
            }
			//end SPR2737
			while (hostResponse.length() > beginIndex) {
				newIndex = hostResponse.indexOf("ERR=", beginIndex);
				if (newIndex == -1) {
					break;
				} else {
					ResultInfo resultInfo = new ResultInfo();
					//begin SPR2737
					if((currentTransResult == HOST_UNAVAILABLE) || (currentTransResult == HOST_ABEND)){
					    resultInfo.setResultInfoCode(NbaOliConstants.TC_RESINFO_SYSTEMNOTAVAIL);
					} else {
					    resultInfo.setResultInfoCode(NbaOliConstants.TC_RESINFO_UNKNOWNREASON);
					}
					//end SPR2737
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
	 * create the broken out response for the 514 transaction
	 * @param txLifeResponse The current response from the host
	 * @param txLifeRequest The current XML request 
	 * @return TXLifeResponse
	 */
	private TXLifeResponse createAgent514Response(TXLifeResponse txLifeResponse, TXLife txLifeRequest) throws NbaBaseException{
		//currently not used for parsing the XML response. The getAgentTransResult returns the agent result 
		int beginIndex = 0, endIndex = 0;
		int newIndex = 0;
		// SPR3290 code deleted
		int leastResult = 1;
		int currentTransResult = 0, currentAgentResult = 0;
		HashMap pMap = new HashMap();
		HashMap rMap = new HashMap();
		// SPR3290 code deleted
		String partyID= new String();
		String relationID= new String();
		// SPR3290 code deleted
		OLifE olife = txLifeResponse.getOLifE();
		Policy policy = new Policy();
		
		if (olife == null) {
			throw new NbaBaseException("ERROR: Could not create a OLifE object for Agent Validation response.");
		}
		Holding holding = new Holding();
		holding.setId("Holding_1");
		
		//set up the request 
		OLifE reqOlife = txLifeRequest.getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getOLifE();
		Relation reqRelation = reqOlife.getRelationAt(0);
		Party reqParty = reqOlife.getPartyAt(0);
		
		//loop through and find the various return codes
		while (hostResponse.length() > beginIndex) {
			beginIndex = hostResponse.indexOf("WHATTODO", beginIndex);
			//if no more occurences return the transResult
			if (beginIndex == -1) {
				break;
			}
		
			beginIndex = hostResponse.indexOf(",", beginIndex);
			endIndex = hostResponse.indexOf(";", beginIndex);
			currentTransResult = Integer.parseInt(hostResponse.substring(beginIndex + 1, endIndex));	
			
			String reqPartyID = reqRelation.getRelatedObjectID();
			//String reqRelationID = reqRelation.getId();
			String reqAgentID = reqParty.getProducer().getCarrierAppointmentAt(0).getCompanyProducerID();
			
			
			int i = Integer.parseInt(reqPartyID.substring(6));
			int j = Integer.parseInt(reqPartyID.substring(6));
			
			
			while (hostResponse.length() > beginIndex) {
				newIndex = hostResponse.indexOf("PUAGVLEM=", beginIndex);
				if (newIndex == -1) {
					break;
				} else {
					SystemMessage systemMessage = new SystemMessage();
					endIndex = hostResponse.indexOf(";", newIndex);
					String currentError = hostResponse.substring(newIndex + 9, endIndex);
					systemMessage.setMessageSeverityCode(currentError.substring(0,1));
					currentAgentResult = Integer.parseInt(currentError.substring(0,1));
					systemMessage.setMessageCode(currentError.substring(28,31));
					systemMessage.setMessageDescription(currentError.substring(32,currentError.length()));
					policy.setPolNumber(reqOlife.getHoldingAt(0).getPolicy().getPolNumber());
					policy.setCarrierCode(reqOlife.getHoldingAt(0).getPolicy().getCarrierCode());
					policy.setProductCode(currentError.substring(16,27));
					
					/*
					 * Create the agent party
					 */
					if (pMap.get(currentError.substring(2,12)) == null){
					
						Party party = new Party();
						Producer producer = new Producer();
						CarrierAppointment carrierAppointment = new CarrierAppointment();
						String agentID = currentError.substring(2,12).trim();
						if (agentID.equals(reqAgentID)){
							partyID = reqPartyID;
						}else{
							i++;
							partyID = "party_"+ i;
						}
						carrierAppointment.setPartyID(partyID);
						carrierAppointment.setCompanyProducerID(agentID);
						producer.addCarrierAppointment(carrierAppointment);
						party.setProducer(producer);
						if (currentAgentResult == 0){
							olife.addParty(reqParty);						
						} else {
							olife.addParty(party);
						}
						
						pMap.put(currentError.substring(2,12), partyID);
					}else{
						partyID = pMap.get(currentError.substring(2,12)).toString();
					}

					
					if (rMap.get(partyID+currentError.substring(13,15)) == null){	
						relationID = "Relation_"+ j;
						Relation relation = new Relation();
						rMap.put(partyID + currentError.substring(13,15),relationID);
						relation.setId(relationID);
						relation.setRelatedObjectID(partyID);
						relation.setOriginatingObjectID("Holding_1");
						
						if (currentError.substring(13,15).equals("01")){
							relation.setRelationRoleCode(reqRelation.getRelationRoleCode());
							OLifEExtension olifeEx = NbaTXLife.createOLifEExtension(EXTCODE_RELATIONPRODUCER);
							RelationProducerExtension prodEx = new RelationProducerExtension();
							prodEx.setSituationCode(reqRelation.getOLifEExtensionAt(0).getRelationProducerExtension().getSituationCode());
							olifeEx.setRelationProducerExtension(prodEx);
							relation.addOLifEExtension(olifeEx);
						}else if (currentError.substring(13,15).equals("02")){
							relation.setRelationRoleCode(48);
						}else if (currentError.substring(13,15).equals("03")){
								relation.setRelationRoleCode(53);
						}
						if (currentAgentResult == 0){
							olife.addRelation(reqRelation);
						} else {
							olife.addRelation(relation);
						}
						j++;
					}else {
						relationID = rMap.get(partyID+currentError.substring(13,15)).toString();
					}
						
					systemMessage.setRelatedObjectID(relationID);
					holding.addSystemMessage(systemMessage);

					if (hostResponse.length() == endIndex + 1) {
						beginIndex = endIndex;
						break;
					}
					if (hostResponse.substring(endIndex + 1, endIndex + 10).compareTo("PUAGVLEM=") == 0) {
						beginIndex = endIndex;
						continue;
					} else {
						beginIndex = endIndex;
						break;
					}
				}
			}
	
		}
		TransResult transResult = txLifeResponse.getTransResult();
		//ResultInfo resultInfo = new ResultInfo();
		if (currentTransResult > currentAgentResult) {
			transResult.setResultCode(5);
		} else if (currentAgentResult > leastResult) {
			transResult.setResultCode(2);
		} else {
			transResult.setResultCode(1);
		}
		//transResult.addResultInfo(resultInfo);
		
		holding.setPolicy(policy);
		olife.addHolding(holding);
		txLifeResponse.setOLifE(olife);
		return txLifeResponse;
	
	}
	/**
	 * Translate State code
	 * @param state State code to translate
	 * @return transState Translated state value
	 */
	private String translateStateCode(String state) {
		String transState = new String();

		if (state== null || state.length() == 0) { //NBA077
			return ("");
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
	/** Creates OLifE object containing party information returned from host system for writing or servicing agents.  
	 * @param txLifeResponse 
	 * @return OLifE 
	 */
	//NBA112 new method
	protected OLifE createOLifE_Response_ForAgentInfo(TXLife txLifeRequest) {
		//Begin NBA112
		OLifE olifeRequest = txLifeRequest.getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getOLifE();
		long relationRoleCode = olifeRequest.getRelationAt(0).getRelationRoleCode();
		compCode = olifeRequest.getHoldingAt(0).getPolicy().getCarrierCode();
		OLifE oLife = new OLifE();
		String[][] basicInfo = null;
		String[][] commInfo = null;
		String[][] nameInfo = null;
		String[][] addressInfo = null;
		if (relationRoleCode == OLI_REL_PRIMAGENT || relationRoleCode == OLI_REL_ADDWRITINGAGENT) {
			basicInfo = getData(1, WRIT_AGENT_BASIC_FIELDS);
			commInfo = getData(1, WRIT_AGENT_COMM_FIELDS);
			nameInfo = getData(1, WRIT_AGENT_NAME_FIELDS);
			addressInfo = getData(1, WRIT_AGENT_ADDRESS_FIELDS);
		} else if (relationRoleCode == OLI_REL_SERVAGENT || relationRoleCode == OLI_REL_SERVAGENCY) {
			basicInfo = getData(1, SERV_AGENT_BASIC_FIELDS);
			commInfo = getData(1, SERV_AGENT_COMM_FIELDS);
			nameInfo = getData(1, SERV_AGENT_NAME_FIELDS);
			addressInfo = getData(1, SERV_AGENT_ADDRESS_FIELDS);
		}

		//Set basic information
		CarrierAppointment carrierAppnt = new CarrierAppointment();
		if(basicInfo[0][0] != null && basicInfo[0][0].length() > 0){
			carrierAppnt.setCompanyProducerID(basicInfo[0][0]);
		}
		if(basicInfo[0][3] != null && basicInfo[0][3].length() > 0){
			carrierAppnt.setAppointmentCategory(basicInfo[0][3]);	
		}
		Producer producer = new Producer();
		producer.addCarrierAppointment(carrierAppnt);
		Party party = new Party();
		party.setProducer(producer);

		//set communication information like phones and Email
		if(commInfo[0][0] != null && commInfo[0][0].length() > 0){
			if(commInfo[0][0].compareToIgnoreCase(PREFCOMM_EMAIL) == 0){
				party.setPrefComm(OLI_PREFCOMM_EMAIL) ;
			} else if(commInfo[0][0].compareToIgnoreCase(PREFCOMM_FAX) == 0){
				party.setPrefComm(OLI_PREFCOMM_FAX);
			}
		}
		
		int phCount = 1;
		phoneTypeTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_PHONETYPE, compCode, "*"));
		Phone phone = null;
		if (commInfo[0][1] != null & commInfo[0][1].length() > 3) {
			phone = new Phone();
			phone.setId("Phone_" + Integer.toString(phCount));
			phCount++;
			phone.setPhoneTypeCode(OLI_PHONETYPE_BUS);
			phone.setAreaCode(commInfo[0][1].substring(0, 3));
			phone.setDialNumber(commInfo[0][1].substring(3));
			party.addPhone(phone);
		}
		if (commInfo[0][2] != null & commInfo[0][2].length() > 3) {
			phone = new Phone();
			phone.setId("Phone_" + Integer.toString(phCount));
			phCount++;
			phone.setPhoneTypeCode(OLI_PHONETYPE_HOME);
			phone.setAreaCode(commInfo[0][2].substring(0, 3));
			phone.setDialNumber(commInfo[0][2].substring(3));
			party.addPhone(phone);
		}
		if (commInfo[0][3] != null & commInfo[0][3].length() > 3) {
			phone = new Phone();
			phone.setId("Phone_" + Integer.toString(phCount));
			phCount++;
			phone.setPhoneTypeCode(OLI_PHONETYPE_MOBILE);
			phone.setAreaCode(commInfo[0][3].substring(0, 3));
			phone.setDialNumber(commInfo[0][3].substring(3));
			party.addPhone(phone);
		}
		if (commInfo[0][4] != null & commInfo[0][4].length() > 3) {
			phone = new Phone();
			phone.setId("Phone_" + Integer.toString(phCount));
			phone.setPhoneTypeCode(OLI_PHONETYPE_BUSFAX);
			phone.setAreaCode(commInfo[0][4].substring(0, 3));
			phone.setDialNumber(commInfo[0][4].substring(3));
			party.addPhone(phone);
		}
		if (commInfo[0][5] != null & commInfo[0][5].length() > 0){
			EMailAddress email = new EMailAddress();
			email.setAddrLine(commInfo[0][5]);
			party.addEMailAddress(email);
		}

		//Set Name information
		PersonOrOrganization personOrg = new PersonOrOrganization();
		if (nameInfo[0][1] != null) {
			if (nameInfo[0][1].compareToIgnoreCase(PERSON) == 0) {
				Person person = new Person();
				personOrg.setPerson(person);

				if(nameInfo[0][0] != null && nameInfo[0][0].length() > 0){
					genderTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLI_LU_GENDER, compCode, "*"));
					person.setGender(findOLifeCode(nameInfo[0][0], genderTable));
				}
				party.setPartyTypeCode(OLIX_PARTYTYPE_INDIVIDUAL);
				if (nameInfo[0][3] != null && nameInfo[0][3].length() > 0) {
					person.setLastName(nameInfo[0][3]);
				}
				if (nameInfo[0][4] != null && nameInfo[0][4].length() > 0) {
					person.setFirstName(nameInfo[0][4]);
				}
				if (nameInfo[0][5] != null && nameInfo[0][5].length() > 0) {
					person.setMiddleName(nameInfo[0][5]);
				}
				if (nameInfo[0][6] != null && nameInfo[0][6].length() > 0) {
					prefixTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLIEXT_LU_PREFIX, compCode, "*"));
					person.setPrefix(findOLifeCode(nameInfo[0][6], prefixTable));
				}
				if (nameInfo[0][7] != null && nameInfo[0][7].length() > 0) {
					suffixTable = ((NbaUctData[]) getUctTable(NbaTableConstants.OLIEXT_LU_SUFFIX, compCode, "*"));
					person.setSuffix(findOLifeCode(nameInfo[0][7], suffixTable));
				}
			} else if (nameInfo[0][1].compareToIgnoreCase(CORPORATION) == 0 || nameInfo[0][1].compareToIgnoreCase(TRUST) == 0) {
				party.setPartyTypeCode(OLIX_PARTYTYPE_CORPORATION);
				Organization org = new Organization();
				personOrg.setOrganization(org);
				if(nameInfo[0][1].compareToIgnoreCase(CORPORATION) == 0 ){
					org.setOrgForm(OLI_ORG_PUBCORP);
				}
				if (nameInfo[0][2] != null && nameInfo[0][2].length() > 0) {
					org.setDBA(nameInfo[0][2]);
				}
			}
		}
		//Set Address Information
		Address address = new Address();
		if (addressInfo[0][0].compareToIgnoreCase(ADTYPE_HOME) == 0) {
			address.setAddressTypeCode(OLI_ADTYPE_HOME);
		} else if (addressInfo[0][0].compareToIgnoreCase(ADTYPE_BUS) == 0) {
			address.setAddressTypeCode(OLI_ADTYPE_BUS);
		}
		if (addressInfo[0][1] == null || addressInfo[0][1].trim().length() == 0) {
			address.setAddressCountryTC(OLI_NATION_USA);
		}
		if (addressInfo[0][2] != null && addressInfo[0][2].length() > 0) {
			address.setAttentionLine(addressInfo[0][2]);
		}
		if (addressInfo[0][3] != null && addressInfo[0][3].length() > 0) {
			address.setLine1(addressInfo[0][3]);
		}
		if (addressInfo[0][4] != null && addressInfo[0][4].length() > 0) {
			address.setLine2(addressInfo[0][4]);
		}
		if (addressInfo[0][5] != null && addressInfo[0][5].length() > 0) {
			address.setCity(addressInfo[0][5]);
		}
		if (addressInfo[0][6] != null && addressInfo[0][6].length() > 0) {
			address.setAddressStateTC(translateStateCode(addressInfo[0][6]));
		}
		if (addressInfo[0][7] != null && addressInfo[0][7].length() > 0) {
			address.setZip(addressInfo[0][7]);
		}

		party.setPersonOrOrganization(personOrg);
		party.addAddress(address);
		oLife.addParty(party);
		return oLife;
		//End NBA112
	}
	/** Creates OLifE object containing party information returned from host system for calculating underwriting risk.  
	 * @param txLifeResponse 
	 * @return OLifE 
	 */
	//NBA105 new method
	//NBA124 changed method signature, changed method name
	protected OLifE createOLifE_Response_ForPartyInquiry(TXLife txLifeRequest) throws NbaBaseException {
		NbaTXLife nbaTxLife = new NbaTXLife();
		nbaTxLife.setTXLife(txLifeRequest);
		compCode = nbaTxLife.getCarrierCode();
		NbaUserVO userVO = nbaTxLife.getUser();
		NbaTXLife responseNbaTxLife = new NbaTXLife(); //NBA124
		responseNbaTxLife.setTXLife(NbaTXLife.createTXLifeResponse(txLifeRequest)); //NBA124
		Party party = nbaTxLife.getOLifE().getPartyAt(0);
		String[][] searchResultInfo = null;
		String[][] searchResults = null;
		String personName = "";
		//Get the search result info fields this will tell how many polciy numbers have been returned.Its not a repeating segment
		searchResultInfo = getData(1, PARTY_SEARCH_RESULT_INFO_FIELDS);
		int numberOfRecords = 0;
		Map policyNumberMap = new HashMap();
		// SPR3290 code deleted
		NbaPartyInquiryInfo partyInqInfo = null;
		NbaPartyInquiryInfo tempPartyInqInfo = null;
		//If number of records are returned
		if (searchResultInfo[0][2] != null && searchResultInfo[0][2].trim().length() > 0) {
			numberOfRecords = Integer.parseInt(searchResultInfo[0][2]);
		}
		if (searchResultInfo[0][0] != null && searchResultInfo[0][0].trim().length() > 0) {
			personName = searchResultInfo[0][0];
		}
		//get the actual search results.
		searchResults = getData(numberOfRecords, PARTY_SEARCH_RESULT_FIELDS);
		for (int index = 0; index < numberOfRecords; index++) {
			if (searchResults[index][3] != null && searchResults[index][3].trim().length() > 0) {
				//Take out the person role code for current policy
				String roleCode = (searchResults[index][3].substring(0, 2));
				//Taking out first two characters which represent relation role code in cyberlife
				//consider only the policies in which person is having an insurable role.
				if (checkForInsurableRole(roleCode)) {
					if (searchResults[index][1] != null && searchResults[index][1].trim().length() > 0) {
						//If policy has already been added in the map, takes care of the case in which the person is playing more than one role in the same policy, that should be considered only once.
						if (!policyNumberMap.containsKey(searchResults[index][1])) {
							partyInqInfo = new NbaPartyInquiryInfo();
							partyInqInfo.setPolicyNumber(searchResults[index][1]);
							partyInqInfo.setCarrierCode(searchResults[index][0]);
							partyInqInfo.setPolicyStatus(searchResults[index][2]);
							policyNumberMap.put(searchResults[index][1], partyInqInfo);
						} else {
							//if policy has already been considered, if current policy is from inforced then ignore the last one and consider the inforced one 
							tempPartyInqInfo = (NbaPartyInquiryInfo) policyNumberMap.get(searchResults[index][1]);
							String currentPolicyStatus = searchResults[index][2];
							if (NbaCyberInforceConstants.POL_PENDING.equals(tempPartyInqInfo.getPolicyStatus())
								&& !NbaCyberInforceConstants.POL_PENDING.equals(
									currentPolicyStatus)) { //if current policy status is other then what is there in map already, replace that with this one
								partyInqInfo = new NbaPartyInquiryInfo();
								partyInqInfo.setPolicyNumber(searchResults[index][1]);
								partyInqInfo.setCarrierCode(searchResults[index][0]);
								partyInqInfo.setPolicyStatus(searchResults[index][2]);
								policyNumberMap.put(searchResults[index][1], partyInqInfo);
							}
						}
					}
				} else {
					continue;
				}
			}
		}
		Iterator itr = policyNumberMap.keySet().iterator();
		String polNumber;
		// SPR3290 code deleted
		NbaTXLife partyHoldingPair = null;
		OLifE responseOlife = responseNbaTxLife.getOLifE(); //NBA124
		setNbaOLifEId(new NbaOLifEId(responseOlife)); //NBA124 
		//iterate through the returned policy information, do an holding inquiry for each of the policy.
		while (itr.hasNext()) {
			polNumber = (String) itr.next();
			partyInqInfo = (NbaPartyInquiryInfo) policyNumberMap.get(polNumber);
			partyHoldingPair = getXML203ForParty(partyInqInfo, party, userVO); //NBA124
			if (partyHoldingPair != null) {
				//begin NBA124
				OLifE currentOlife = partyHoldingPair.getOLifE();
				if (currentOlife.getHoldingCount() > 0) {
					responseOlife.addHolding(currentOlife.getHoldingAt(0));
				}
				if (currentOlife.getPartyCount() > 0) {
					responseOlife.addParty(currentOlife.getPartyAt(0));
				}
				if (currentOlife.getRelationCount() > 0) {
					responseOlife.addRelation(currentOlife.getRelationAt(0));
				}
				//end NBA124
			}
		}
		return responseOlife; //NBA124
	}
	
	/** Creates NbaTXLife  object, which is attached with the party for the underwriting risk. It queries the host using the information encapsulated in 
	 *  incoming PartyInquiryInfo object, queries the appropriate host and gets the holding. This holding is the filtered to contain the information relevant to the
	 * party represented by incoming party object. This information used to calculate underwriting risk for the party.  
	 * @param  partyInqInfo NbaPartyInquiryInfo object encapsulates details for the policy to be fetched from the host
	 * @param  party party object used to filter the 203
	 * @param  userVO NbaUserVO represents the current user.
	 * @return OLifE 
	 */
	//NBA105 new method
	//NBA124 changed method signature, changed method name 
	protected NbaTXLife getXML203ForParty(NbaPartyInquiryInfo partyInqInfo, Party party, NbaUserVO userVO) throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = null;
		//Create a new txlife request object to query host
		nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTALL);
		NbaLob lob = new NbaLob();
		lob.setBackendSystem(NbaConstants.SYST_CYBERLIFE);
		lob.setCompany(partyInqInfo.getCarrierCode());
		lob.setPolicyNumber(partyInqInfo.getPolicyNumber());
		nbaTXRequest.setNbaLob(lob);
		nbaTXRequest.setNbaUser(userVO);
		NbaTXLife reqLife = new NbaTXLife(nbaTXRequest);
		NbaTXLife respLife = null;
		//NBA124 code deleted
		//If policy is for a pending system query wrappered mode parser
		if (NbaCyberInforceConstants.POL_PENDING.equals(partyInqInfo.getPolicyStatus())) {
			respLife = new NbaBackEndAdapterFacade().submitRequestToHost(reqLife, "");
		} else { //If policy is for a inforce system query inforce mode parser
			NbaWebServiceAdapter service =
				NbaWebServiceAdapterFactory.createWebServiceAdapter(NbaConstants.SYST_CYBERLIFE, "InforceSubmit", "InforceSubmit");
			respLife = service.invokeWebService(reqLife);//SPR2366 SPR2968
		}
		//Check if any error is returned
		List errors = handleHostResponse(respLife);

		//if error is returned in response, return a null  	
		if (errors.size() == 0) {
			//get the filtered holding and party object
			respLife.setBusinessProcess(NbaUtils.getBusinessProcessId(userVO)); //SPR2639
			respLife = NbaUnderwritingRiskHelper.prepareNbaTXLifeForAttachment(respLife, party, getNbaOLifEId(),null); //NBA124, ALII1869
		} else {
			respLife = null;
		}
		return respLife;

	}
	/**Returns a list of error by traversing the incoming txLifeResponse   
	 * @param  aTXLifeResponse NbaTXLife object representing the response from the host
	 * @return List a list of errors
	 */
	//NBA105 new method
	public List handleHostResponse(NbaTXLife aTXLifeResponse) {
		List errors = new ArrayList();
		if (aTXLifeResponse != null && aTXLifeResponse.isTransactionError()) { //NBA094
			UserAuthResponseAndTXLifeResponseAndTXLifeNotify allResponses =
				aTXLifeResponse.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
			int count = allResponses.getTXLifeResponseCount();
			TransResult transResult = allResponses.getTXLifeResponseAt(count - 1).getTransResult();

			for (int i = 0; i < transResult.getResultInfoCount(); i++) {
				errors.add(transResult.getResultInfoAt(i).getResultInfoDesc());
			}
		}
		return errors;
	}

	/**
	 * Gets the transaction results for the Increase transaction
	 * @param transResult the TransResult object
	 * @return TransResult the modified TransResult object
	 */
	//NBA077 New Method
	protected TransResult getTransResultForIncrease(TransResult transResult) {
		int beginIndex = 0;
		while (hostResponse.indexOf("WHATTODO", beginIndex) > -1) {
			String token = null;
			int endIndex = hostResponse.indexOf("WHATTODO", beginIndex + 8);
			if (endIndex > -1) {
				token = hostResponse.substring(beginIndex, endIndex);
				beginIndex = endIndex;
			} else {
				token = hostResponse.substring(beginIndex);
				beginIndex += 8;
			}
			processTransactionResponse(token, transResult);
		}
		return transResult;
	}

	/**
	 * Gets the transaction result for a single transaction response.
	 * @param response the transaction response string
	 * @param transResult the TransResult object
	 * @return TransResult modified TransResult response
	 */
	//NBA077 New Mehtod	
	protected TransResult processTransactionResponse(String response, TransResult transResult) {
		int beginIndex = 0, endIndex = 0;
		int newIndex = 0;
		// SPR3290 code deleted
		int leastResult = 1;
		int currentTransResult = 0;
		String errorDesc = new String();
		// SPR3290 code deleted
		while (response.length() > beginIndex) {
			beginIndex = response.indexOf("WHATTODO", beginIndex);
			//if no more occurences return the transResult
			if (beginIndex == -1) {
				break;
			}
			//get the return code
			beginIndex = response.indexOf(",", beginIndex);
			endIndex = response.indexOf(";", beginIndex);
			currentTransResult = Integer.parseInt(response.substring(beginIndex + 1, endIndex));
			//set Result Code and check to see if any error messages or already have result code 5
			if (currentTransResult > leastResult || transResult.getResultCode() == 5) {
				transResult.setResultCode(5);
			} else {
				transResult.setResultCode(1);
			}
			while (response.length() > beginIndex) {
				newIndex = response.indexOf("ERR=", beginIndex);
				if (newIndex == -1) {
					break;
				} else {
					ResultInfo resultInfo = new ResultInfo();
					resultInfo.setResultInfoCode("999");
					endIndex = response.indexOf(";", newIndex);
					String currentError = response.substring(newIndex + 4, endIndex);
					errorDesc = "";
					errorDesc = errorDesc.concat("  " + currentError);
					if (response.length() == endIndex + 1) {
						resultInfo.setResultInfoDesc(errorDesc);
						beginIndex = endIndex;
						transResult.addResultInfo(resultInfo);

						break;
					}
					if (response.substring(endIndex + 1, endIndex + 5).compareTo("ERR=") == 0) {
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
	 * Gets the value of nbaOLifEId
	 * @return a reference to NbaOLifEId object.
	 */
	//NBA124 New Method
	protected NbaOLifEId getNbaOLifEId(){
		return nbaOLifEId;
	}
	
	
	/**
	 * Sets the value of nbaOLifEId
	 * @param id an instance of NbaOLifEId to be set
	 */
	//NBA124 New Method
	public void setNbaOLifEId(NbaOLifEId id) {
		nbaOLifEId = id;
	}
	
	/**
	 * Determines the number of matching contracts in current party inquiry transaction response. 
	 * @return matching contract count. 
	 */
	//SPR2737 New Method
	protected int getPartyInqMatchCount(){
		String[][] searchResultInfo = null;
		searchResultInfo = getData(1, PARTY_SEARCH_RESULT_INFO_FIELDS);
		int numberOfRecords = 0;
		if (searchResultInfo[0][2] != null && searchResultInfo[0][2].trim().length() > 0) {
			numberOfRecords = Integer.parseInt(searchResultInfo[0][2]);
		}
		return numberOfRecords;
	} 

	/**
	 * Creates OLifE response containing agent validation information returned from the
	 * host system for the writing agents.  
	 * @return OLifE 
	 */
	//NBA132 New Method
	protected OLifE createOLifE_Response_ForAgentVal(TXLife txLifeRequest) {
		OLifE olife = duplicateOLifE_FromRequest_ForAgentVal(txLifeRequest);

		String[][] agencyData = getData(1, WRIT_AGENT_AGENCY_FIELDS);
		String agencyID = agencyData[0][0];

		// Find primary writing agent relation
		String agentID = null;
		String relationID = null;
		Relation relation = null;
	    int relationCount = olife.getRelationCount();
		for (int i = 0; i < relationCount; i++) {
			relation = olife.getRelationAt(i);
			if (NbaUtils.isPrimaryWritingAgentRelation(relation)) {
				agentID = relation.getRelatedObjectID();
				relationID = relation.getId();
				break;
			}
	    }

		//create a party for the agency and relate it to the primary writing agent
	    if (agencyID != null && agentID != null) {
			Party party = new Party();
			party.setId(agentID + "_1");
			party.setProducer(new Producer());
			CarrierAppointment carrAppt = new CarrierAppointment();
			carrAppt.setCompanyProducerID(agencyID);
			party.getProducer().addCarrierAppointment(carrAppt);
			olife.addParty(party);

			relation = new Relation();
			relation.setId(relationID + "_1");
			relation.setOriginatingObjectID(agentID);
			relation.setRelatedObjectID(party.getId());
			relation.setRelationRoleCode(OLI_REL_AGENCYOF);
			relation.setOriginatingObjectType(OLI_PARTY);
			relation.setRelatedObjectType(OLI_PARTY);
			olife.addRelation(relation);
	    }
		return olife;
	}

	/**
	 * Clones the request OLifE object and its children to be returned in a response.
	 * @param txLifeRequest
	 * @return
	 */
	//NBA132 New Method
	protected OLifE duplicateOLifE_FromRequest_ForAgentVal(TXLife txLifeRequest) {
	    OLifE olife = new OLifE();
	    OLifE reqOlife = txLifeRequest.getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getOLifE();
	    olife = reqOlife.clone(false);
	    return olife;
	}
	/**
	 * @return Returns the changeSubType.
	 */
	// NBA195 new method
	public long getChangeSubType() {
		return changeSubType;
	}
	/**
	 * @param changeSubType The changeSubType to set.
	 */
	// NBA195 new method
	public void setChangeSubType(long changeSubType) {
		this.changeSubType = changeSubType;
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
	 * @return Returns the transSubType.
	 */
	// NBA195 new method
	public int getTransSubType() {
		return transSubType;
	}
	/**
	 * @param transSubType The transSubType to set.
	 */
	// NBA195 new method
	public void setTransSubType(int transSubType) {
		this.transSubType = transSubType;
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
	 * Format incoming date string as YYYY-MM
	 * @param date Date value needed to be reformatted
	 * @return date New date value
	 */
	//SPR2151 New Method
	protected String formatExpireYYYYMM(String date) {
		if (date == null || date.length() < 8) {
			return null; 
		} else {
			return (date.substring(0, 4) + "-" + date.substring(4, 6));
		}
	}
}
	

