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

/**
 * Constants used for the Cyberlife values on the host.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>SPR1106</td><td>Version 2</td><td>Tax ID Type Needs to Default in App Entry Adapter</td></tr>
 * <tr><td>SPR1061</td><td>Version 2</td><td>Incorrect DXE FLCW91ND being sent to the host in 1000 transaction</td></tr>
 * <tr><td>NBA006</td><td>Version 2</td><td>Underwriter Workbench Annuity Support</td></tr>
 * <tr><td>NBA012</td><td>Version 2</td><td>Contract Print Extract</td></tr>
 * <tr><td>NBA013</td><td>Version 2</td><td>Correspondence</td></tr>
 * <tr><td>NBA014</td><td>Version 2</td><td>Email</td></tr>
 * <tr><td>SPR1127</td><td>Version 2</td><td>More than one address for same party not being added to host.</td></tr>
 * <tr><td>SPR1131</td><td>Version 2</td><td>Added to create link between the party and the coverage within CyberLife</td></tr>
 * <tr><td>SPR1079</td><td>Version 3</td><td>Special Frequency Changes</td></tr>
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 * <tr><td>NBA111</td><td>Version 4</td><td>Joint Coverage</td></tr>
 * <tr><td>SPR1986</td><td>Version 4</td><td>Remove CLIF Adapter Defaults for Pay up and Term date of Coverages and Covoptions.</td></tr>
 * <tr><td>SPR1601</td><td>Version 4</td><td>Update OLI_LU_REQSTAT to 2.8.90</td></tr>
 * <tr><td>SPR1549</td><td>Version 4</td><td>UFlat/Table Rating applied to Coverage is wrongly translated to Flat Permanent Rating</td></tr>
 * <tr><td>SPR1524</td><td>Version 5</td><td>Annuity QualPlanType added to holding.</td></tr>
 * <tr><td>SPR1952</td><td>Version 5</td><td>If the cease date for a coverage is input, we should be setting the flag bit on the coverage segment.</td></tr>
 * <tr><td>SPR2287</td><td>Version 5</td><td>Ownership details are not built in 89 segment of CyberLife Admin system</td></tr>
 * <tr><td>SPR2229</td><td>Version 5</td><td>Add contract level benefit indicator to holding inquiry.</td></tr>
 * <tr><td>SPR1073</td><td>Version 5</td><td>WRAPPERED CLIF - DXE not being created for Credit Card Number, Name and Expiry Date</td></tr>
 * <tr><td>SPR2202</td><td>Version 5</td><td>For JL product, the FSBPIDNT DXE is carrying an incorrect value for Joint Benefit attached to the JL coverage</td></tr>
 * <tr><td>SPR2319</td><td>Version 5</td><td>Issue to Admin the DXE - FCVNCTYP and FCVFLGB5 are not sent and the status of coverage is displayed as "Unknown" in 62D2 screen.</td></tr>
 * <tr><td>SPR2099</td><td>version 5</td><td>MEC processing is incorrect.</td></tr>
 * <tr><td>NBA122</td><td>Version 5</td><td>NBA Underwriter Workbench Rewrite Project</td></tr> 
 * <tr><td>SPR2737</td><td>Version 6</td><td>Error Return Handling Never Defined for Underwriting Risk CyberLife Service (CREF)</td></tr>
 * <tr><td>SPR3164</td><td>Version 6</td><td>The NBREQRMNT workitem went to NBERROR with reason related to Primary Writing agent address details</td></tr>
 * <tr><td>NBA211</td><td>Version 7</td><td>Partial Application project</td></tr>
 * <tr><td>SPR2151</td><td>Version 8</td><td>Correct the Contract Validation edits and Adaptor logic for EFT, PAC and Credit Card Billing</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public interface NbaCyberConstants {

    //Transactions
    public static final int HOLDING = 203;
    public static final int NEW_APP = 103;
    public static final int CHANGE_RATECLASS = 502;
    public static final int CHANGE_REQUIREMENT = 109;
    public static final int SUBSTANDARD_RATING = 151;
    public static final int FINANCIAL = 140;
    public static final int DENY_PERS_COV_BEN = 503;
    public static final int ENDORSEMENT = 504;
    public static final int APPROVE = 505;
    public static final int FINAL_DISPOSITION = 507;
    public static final int CWA = 508;
    public static final String HOLDING_TYPE = "6PCYWHATTODO=DISP,2;";
    public static final String CHANGE_RATECLASS_TYPE = "6PCYWHATTODO=2000,2;";
    public static final String POLICY_STATUS = "AEPPOLOC";
    public static final String CHANGE_ENDORSEMENT_TYPE = "6PCYWHATTODO=5800;";
    public static final String TRANS_ADD_RATING = "6PCYWHATTODO=5100,2;";
    public static final String TRANS_DELETE_RATING = "6PCYWHATTODO=3000,2;";
    public static final String CWA_REVERSE_REFUND = "6PCYWHATTODO=4000;";
    
	
    // NBA013 15 DXEs PUNBWAFN, PUNBWAMI, PUNBWALN, PUNBWAAD, PUNBWAL1, PUNBWAL2, PUNBWACT, PUNBWAST, PUNBWAZP, PUNBAGAD, PUNBAGL1, PUNBAGL2, PUNBAGCT, PUNBAGST, PUNBAGZP appended to the HOLDING_RESOLVE
    // NBA014 2 DXEs PUNBWAEM, PUNBAGEM appended to the HOLDING_RESOLVE

    public static final String HOLDING_RESOLVE =
        "RESOLVE=PUSPSTYPPUCPS02CPUCPS02PFCVPROD,FCVIDENTFCVTCGDTFCVFACE,FCVUNITSFCVPDSKYFCVNCTYPFCVPREMAFCVISSDFFCVPHASEFCVPIDNTFCVLIVESFCVAGEISFCVFLGB0FCVFLGB5FCVFLGD0PUCPS04CPUCPS04PFSBBCDTEFSBBENEFFSBBISDFFSBBPHS,FSBBRAT,FSBPREMAFSBPIDNTFSBFLGA3FSBBUNT,FSBFLGA4FSBFLGA7FSBFLGB7FCVPSTYPPUCPS03CPUCPS03PFELIDENTFELETDATFELECOM,FELEFFDFFELETYP,FELFLTEXFELEREASFELETABLFELPCTRTFELEAP,FELEPHS,FELPIDNTFELPREMAFELEDUSEFELFLGB0FELFLGB4PUCPS82CPUCPS82PFRDIDENTFRDPHASEFRDCAUTNFRDBENE,FRDPIDNTPUCPS82PFRDENDCDFRDERDATFRDBUSE,PUCPS89CPUCPS89PPUCPS90CPUCPS90PPUCPS95CPUCPS95PPUCPS96CPUCPS96PPUCSX96CPUCSX96PFLCIDENTFLCFNAMEFLCMINITFLCLNAMEFLCNMPFXFLCNMSFXFLCCORP,FLCTAXNOFLCMARSTFLCODESCFLCOCLASFLCTAXCDFLCLVERBFLCALIENFLCSGSEQFLCCOVERFLCBHDATFLCSEXCDFLCAGE,FLCBLHFTFLCBLHINFLCBLWGTFLCBHPLCFLCPHONEFLC2PHNTFLC2PHN,FLCPIDNTFMDAPXNWFMDANINCFRSIDRNOFRSIDRNOFBRSTATEFBRRCTRYFNAPIDNTFNAOCCURFNAEFFDTFNAADRTYFNACNTRYFNANMADDFNALINE1FNALINE2FNACITY,FNASTATEFNAZIPCDFECSGTYPFECUSECDFECPERSNFECPRSEQFECVARBLFBDCVPRSFBDCVSEQFBDCVSEQFBDCVPIDFBDNOENTFBDSEGIDFBDSGSEQFBDPRPIDFBDBNAMTFBDBDISTFBDBNPCTFBDPRSN,FBDPRSEQFBDBNRELFBDBTYPEFBRAGENTAEPKEYPLAEPKEYUSBASEPRODBASEPLANFNBAISSRFBRISTATFBRREINSFBRBMODEFBRMPREMFBRFORM,FBRRESTAPUNBENDRPUNBOTHIPUNBBENEPUNBRATSFNBALTADFBRAPPDTFNBPCCBLFNBACNDAFNBAUNDAFNBRIDATPUCPS80CPUCPS80PFPRIDENTFPRSGSEQFPRACTINFPRCRDATFPRCRDATFPRCRTIMFPRREQTPFPROCCURFPRRESTRFPRDEPTEPUCPS83CPUCPS83PFUEIDENTFUESGSEQFUEPHASEFUENODATFUEERRCDFUESEVERFUEFDESCFSPTRCTLPUCPS47CPUCPS47PFCWIDENTFCWFLGA3FCWENDATFCWASOF,FCWORGNTFCWAMONTFCWCHGTPFCWFLGA0FRSIDRSTFRSIDENTFRSPIDNTPUCPS40CPUCPS40PFNBAFNLDPUCPS48CPUCPS48PPUCSX48CPUCSX48PFAGIDENTFAGORGBAFAGSITCDFAGNOLVLFAGPCTSHFAGPCTSVFAGAGTIDFAGAGTLVPUNBASGPPUNBADDPPUNBALTPPUNBAGTEFPRPIDNTPUNBREPPFELSBTYPFELEMOYRFBRAGNCYPUNBAPRMFRCRCTYP1CVFACE,FNBSTWCDFBCBBNUMFBRBILTDFBRDUEDYFBRISSCOFBRADMCOFBRDUEUSFFCQPAMTFFCQPBASFNBUNDIDFNBACKIDFBROPT,FTDICRATFBRPROPDFTDPINITFULIGIR,FULPLOPTPUNBPRMMPUNBPRMQPUNBPRMSPUNBPRMAPUNBWAFNPUNBWAMIPUNBWALNPUNBWAADPUNBWAL1PUNBWAL2PUNBWACTPUNBWASTPUNBWAZPPUNBAGADPUNBAGL1PUNBAGL2PUNBAGCTPUNBAGSTPUNBAGZPPUNBWAEMPUNBAGEMFCVFQTYPFNBRPINSPUCPX57CPUCPX57PTPLFNDIDTPLALPER;"; //NBA012 //NBA111 SPR1549 SPR1524 NBA122 NBA211

    
    public static final String HOLDING_SEGMENTS =
        "PUCPS02C=999;PUCPS02P=;AEPPOLOC=P;PUCPS04C=999;PUCPS04P=;PUCPS03C=999;PUCPS03P=;PUCPS82C=999;PUCPS82P=;PUCPS89C=999;PUCPS89P=;PUCPS90C=999;PUCPS90P=;PUCPS95C=999;PUCPS95P=;PUCPS80C=999;PUCPS80P=;PUCPS83C=999;PUCPS83P=;PUCPS47C=999;PUCPS47P=;PUCPS96C=999;PUCPS96P=;PUCSX96C=999;PUCSX96P=;PUCPS40C=999;PUCPS40P=;PUCPS48C=999;PUCPS48P=;PUCSX48C=999;PUCSX48P=;";

    public static final String TRANS_REQ_RECEIPT_WAIVE = "6PCYWHATTODO=5400,2;";
    public static final String FINAL_DISPOSITION_TRANS = "6PCYWHATTODO=5600,2;";
    public static final String TRANS_REQ_ADD_ORDER = "6PCYWHATTODO=5200,2;";
    public static final String DENY_TRANS = "6PCYWHATTODO=5000;";
    public static final String APPROVE_TRANS = "6PCYWHATTODO=5500;";
    public static final long TRANSACTION_NB_CHANGE = 109;
    public static final long TRANSMITTAL_SUBTYPE_OBJECT_REQUIREMENT_INFO = 57;
    public static final long TRANSMITTAL_SUBTYPE_OBJECT_FINAL_DISP = 58;
    public static final int TRANSMITTAL_SUBTYPE_CONTENT_CODE_ADD = 1;
    public static final int TRANSMITTAL_SUBTYPE_CONTENT_CODE_REMOVE = 3;
    public static final long TRANSMITTAL_SUBTYPE_CONTENT_CODE_UPDATE = 2;
    public static final long TRANSMITTAL_SUBTYPE_CONTENT_CODE_INSERT = 1;
    public static final int TRANSMITTAL_SUBTYPE_OBJECT_COVERAGE = 20;
    public static final int TRANSMITTAL_SUBTYPE_OBJECT_BENEFIT = 21;
    public static final int TRANSMITTAL_SUBTYPE_OBJECT_PERSON = 22;
    public static final int TRANSMITTAL_SUBTYPE_OBJECT_DENY_COVERAGE = 20;
    public static final int TRANSMITTAL_SUBTYPE_OBJECT_DENY_RIDER = 86; // NBA006
    public static final int TRANSMITTAL_SUBTYPE_OBJECT_DENY_BENEFIT = 21;
    public static final int TRANSMITTAL_SUBTYPE_OBJECT_DENY_PERSON = 22;
    public static final long TRANSMITTAL_SUBTYPE_OBJECT_APPROVE_CONTRACT = 18;
    public static final long TRANSMITTAL_SUBTYPE_OBJECT_APPROVE_COVERAGE = 20;
    public static final long TRANSMITTAL_SUBTYPE_OBJECT_APPROVE_BENEFIT = 21;
    public static final long TRANSMODE_UPDATE = 5;
    public static final int REQSTAT_ADD = 23;	//SPR1601
    public static final int REQSTAT_ORDER = 22;	//SPR1601
    public static final int REQSTAT_WAIVED = 3;
    public static final int REQSTAT_RECEIVED = 7;
    public static final String FULL = "Full";
    public static final String NO_BENEFIT_RATING = "100";
    public static final long CORP_TAXID = 2; //SPR1106
    public static final long SOC_SEC_TAXID = 1; //SPR1106
    public static final long VAR_UNIV_LIFE = 4; //SPR1058
    public static final long UNIV_LIFE = 3; //SPR1018

	//Miscellaneous constants
	public static final String ZERO_DATE = "19000101";	//NBA104 
	public static final String INTEREST_PERCENT_OWNER = "A"; //SPR2287   

    //Cyberlife role codes

    public static final String PRIMARY_INS = "00"; //Primary Insured
    public static final String JOINT_INS = "01"; //Joint Insured
    public static final String RATING = "03"; // Rating
    public static final String OWNER = "10"; // Owner
    public static final String PAYOR = "20"; //Payor
    public static final String THIRD_PARTY = "25"; // Third Party
    public static final String BENEFICARY = "30"; //Beneficiary
    public static final String SPOUSE = "40"; // Spouse
    public static final String DEPENDENT = "50"; //Dependent
    public static final String OTHER_INS = "60"; //Other Insured
    public static final String ASSIGNEE = "70"; //Assignee
	public static final String JOINT_PRIMARY_INS = "0001"; //SPR2202

    //Party types
    public static final String PERSON = "N";
    public static final String CORPORATION = "C";
    public static final String TRUST = "T"; //Assignee

	//FLAG bit constants
	public static final String FLAG_BIT_OFF = "0"; //NBA111
	public static final String FLAG_BIT_ON = "1"; //NBA111
	
    //Policy Fields

    public static final String POLICY_PERS_SEQ = "AEPPIDNT";
    public static final String POL_NUM = "AEPKEYPL";
    public static final String COMP_CODE = "AEPKEYUS";
    public static final String PROD_TYPE = "BASEPROD";
    public static final String PROD_SUB_TYPE = "PUSPSTYP";
    public static final String ISSUE_RESTRICTION = "FNBAISSR";
    public static final String COND_APPROVAL = "FNBACNDA";
    public static final String FINAL_ACTION = "FNBAUNDA";
    public static final String FINAL_DISP = "FNBAFNLD";
    public static final String JURISDICTION = "FBRISTAT";
    public static final String REINSUR_IND = "FBRREINS";
    public static final String PAYMENT_MODE = "FBRBMODE";
	public static final String INF_SP_FREQ_EXTRACT_MODE = "FFCEXTMD"; //begin SPR1079
	public static final String INF_SP_FREQ_NONSTANDARD_MODE = "FBRMDNST"; 
	public static final String INITIAL_BILL_TO_DATE = "FFCINBDT"; 
	public static final String QUOTED_PREM_BASIS_FREQUENCY = "FFCQPBAS"; 
	public static final String INITIAL_DEDUCTION_DATE = "FFCLDEDT"; 
	public static final String PAYROLL_FREQUENCY = "FFCPYDFR"; 
	public static final String FIRST_MONTH_SKIP = "FBRSTSKM"; 
	public static final String QUOTED_PREM_BASIS_AMT = "FFCQPAMT"; //end SPR1079
    public static final String PAYMENT_AMT = "FBRMPREM";
    public static final String ANN_PAYMENT_AMT = "PUNBAPRM";
    public static final String PAYMENT_METH = "FBRFORM";
    public static final String ASSIGN_IND = "PUNBASGP";
    public static final String ENDORSE_IND = "PUNBENDR";
    public static final String OTHER_INS_IND = "PUNBOTHI";
    public static final String BEN_IND = "PUNBBENE";
    public static final String RATE_IND = "PUNBRATS";
    public static final String ADD_ALT_IND = "PUNBADDP";
    public static final String ALT_IND = "PUNBALTP";
    public static final String AGENT_ERR_IND = "PUNBAGTE";
    public static final String SIGN_DATE = "FBRAPPDT";
    public static final String CWA_AMOUNT = "FNBPCCBL";
    public static final String UNDER_APPR = "FNBACNDA";
    public static final String UNDER_STATUS_CD = "FNBAUNDA";
    public static final String REQ_ISS_DATE = "FNBRIDAT";
    public static final String PROD_CODE = "BASEPLAN";
    public static final String APPLICATION_TYPE = "FRCRCTYP";
    public static final String FACE_AMOUNT = "1CVFACE";
    public static final String CWA_REVERSE_TYPE = "FCWREVTP";
    //begin NBA012    
    public static final String ISSUE_COMPANY = "FBRISSCO";
    public static final String ADMIN_COMPANY = "FBRADMCO";
    public static final String CURRENT_INT_RATE = "FTDICRAT";
    public static final String DIVIDEND_OPTION = "FBRPROPD";
    public static final String GUAR_INT_RATE = "FULIGIR";
    public static final String ALT_PREM_MODE_AN = "PUNBPRMA";
    public static final String ALT_PREM_MODE_SA = "PUNBPRMS";
    public static final String ALT_PREM_MODE_QT = "PUNBPRMQ";
    public static final String ALT_PREM_MODE_MO = "PUNBPRMM";
    //end NBA012 
    //begin NBA104
    public static final String SFC_MONTHLY_PREM = "FFCMPREM";
    public static final String SFC_SPECIAL_FREQ_PREM = "FFCSPREM";
    public static final String SFC_TOTAL_PREM_AMT = "FFCTPAMT";
    //end NBA104

    //FINANCIAL ACTIVITY
    public static final String FIN_GROSS_AMT = "FCWAMONT";
    public static final String FIN_ACT_DATE = "FCWASOF";
    public static final String CWA_TYP_CODE = "FCWCHGTP";
    public static final String CWA_ERR_CODE = "FCWFLGA3";
    public static final String CWA_ENT_DATE = "FCWENDAT";
    public static final String CWA_DPT_DESK = "FCWORGNT";
    public static final String CWA_DISBURSED = "FCWFLGA0";
    public static final String CWA_ASOFDATE = "FCWFLGB3";
    public static final String FIN_COUNT = "PUCPS47C";
    public static final String CWA_CODE = "FCWENTTP";

    //ADDRESS FIELDS
    public static final String ADDR_PERS_SEQ_ID = "FNAPIDNT";
    public static final String ADDR_TYPE_CODE = "FNAADRTY";
    public static final String ATTEN_LINE = "FNANMADD";
    public static final String CITY = "FNACITY";
    public static final String STATE = "FNASTATE";
    public static final String ZIPCODE = "FNAZIPCD";
    public static final String COUNTRY = "FNACNTRY";
    public static final String STARTDATE = "FNAEFFDT";
    public static final String ADDR_LINE1 = "FNALINE1";
    public static final String ADDR_LINE2 = "FNALINE2";
    public static final String ADDR_COUNT = "PUCPS90C";
    public static final String FNAOCCUR = "FNAOCCUR";//SPR1127
    //PARTY AND PERSON FIELDS

    public static final String PERS_SEQ_ID = "FLCPIDNT";
    public static final String FIRST_NAME = "FLCFNAME";
    public static final String LAST_NAME = "FLCLNAME";
    public static final String MIDDLE_NAME = "FLCMINIT";
    public static final String PREFIX = "FLCNMPFX";
    public static final String SUFFIX = "FLCNMSFX";
    public static final String GENDER = "FLCSEXCD";
    public static final String BIRTH_DATE = "FLCBHDAT";
    public static final String BIRTH_STATE = "FLCBHPLC";
    public static final String SMOKE_STATUS = "FLCOCLAS";
    public static final String PARTY_TYPE_CODE = "FLCLVERB";
    public static final String GOV_ID_TC = "FLCTAXCD";
    public static final String GOV_ID = "FLCTAXNO";
    public static final String HEIGHT_FT = "FLCBLHFT";
    public static final String HEIGHT_IN = "FLCBLHIN";
    public static final String WEIGHT = "FLCBLWGT";
    public static final String AGE = "FLCAGE";
    public static final String RESIDENCE_ST = "FBRSTATE";
    public static final String RESIDENCE_CNTRY = "FBRRCTRY";
    public static final String MAR_ST = "FLCMARST";
    public static final String OCCUPATION = "FLCODESC";
    public static final String PERS_COUNT = "PUCPS89C";
    public static final String PHONE = "FLCPHONE";
    public static final String PHONE2 = "FLC2PHN";
    public static final String PHONE_ID = "FLC2PHNT";
    public static final String CORPNAME = "FLCCORP";
    public static final String RISK_IND = "PUNBREPP";
    public static final String COV_IND = "FLCCOVER";

    //Email Address

    public static final String ELECT_PERS = "FECPERSN";
    public static final String ELECT_PERSEQ = "FECPRSEQ";
    public static final String EMAIL_ADDRESS = "FECVARBL";
    public static final String ELECT_COUNT = "PUCPS95C";
    public static final String ELECT_TYPE = "FECSGTYP";
    public static final String PERSONAL_EMAIL_TYPE = "CP";
    public static final String EMAIL_PERS_SEQ = "FECPRSID";

   	//Agent Email Address	
	public static final String AGENT_EMAIL_ADDRESS = "PUNBWAEM"; //NBA014

	//Agency Email Address
	public static final String AGENCY_EMAIL_ADDRESS = "PUNBAGEM"; //NBA014
	

    //Agents

	//Begin NBA013
	public static final String AGENT_FIRST_NAME = "PUNBWAFN";
	public static final String AGENT_MIDDLE_INITIAL = "PUNBWAMI";
	public static final String AGENT_LAST_NAME = "PUNBWALN";
	public static final String AGENT_NAME_ADDENDUM = "PUNBWAAD";
	public static final String AGENT_ADDRESS_LINE1 = "PUNBWAL1";
	public static final String AGENT_ADDRESS_LINE2 = "PUNBWAL2";
	public static final String AGENT_ADDRESS_CITY = "PUNBWACT";
	public static final String AGENT_ADDRESS_STATE = "PUNBWAST";
	public static final String AGENT_ADDRESS_ZIP = "PUNBWAZP";
	
	public static final String AGENCY_NAME_ADDENDUM = "PUNBAGAD";
	public static final String AGENCY_ADDRESS_LINE1 = "PUNBAGL1";
	public static final String AGENCY_ADDRESS_LINE2 = "PUNBAGL2";
	public static final String AGENCY_ADDRESS_CITY = "PUNBAGCT"; //SPR3164
	public static final String AGENCY_ADDRESS_STATE = "PUNBAGST";
	public static final String AGENCY_ADDRESS_ZIP = "PUNBAGZP";
	//End NBA013
    
    public static final String AGENT_NAME = "FAGANAME";
    public static final String SERVICE_AGENT = "FBRAGENT";
    public static final String AGENCY_NAME = "FBRAGNCY";
    public static final String CARRIER_CODE = "FAGORGBA";
    public static final String AGENT_COUNT = "PUCPS48C";
    public static final String AGENT_SUB_COUNT = "PUCSX48C";
    public static final String AGENT_SUB_ORDER = "FAGNOLVL";
    public static final String AGENT_LEVEL = "FAGAGTLV";

    // Commission Share Percent
    public static final String COM_SHARE_PCT = "FAGPCTSH";

    //RISK 
    public static final String RISK_PERS_SEQ = "FRSPIDNT";
    public static final String DRIVER_LIC_NUM = "FRSIDRNO";
    public static final String DRIVER_LIC_ST = "FRSIDRST";
    public static final String RISK_COUNT = "PUCPS40C";

    //MESSAGE DETAIL
    public static final String MESSAGE_COUNT = "PUCPS83C";
    public static final String MSG_REF_ID = "FUEPHASE";
    public static final String MSG_START_DATE = "FUENODAT";
    public static final String MSG_CODE = "FUEERRCD";
    public static final String MSG_DESC = "FUEFDESC";
    public static final String MSG_SEVERITY = "FUESEVER";
    public static final String MSG_DETAIL = "FUEIDENT";
    public static final String MSG_SEQ = "FUESGSEQ";

    //ENDORSEMENT INFO
    public static final String ENDORSEMENT_COUNT = "PUCPS82C";
    public static final String APPLIES_TO_CONTRACT = "FRDPHASE";
    public static final String ENDORS_PERS_SEQ = "FRDPIDNT";
    public static final String APPLIES_TO_COV = "FRDBUSE";
    public static final String APPLIES_TO_COVOPT = "FRDBENE";
    public static final String ENDORS_CODE = "FRDENDCD";
    public static final String ENDORS_DETAILS = "FRDCAUTN";
    public static final String ENDORS_END_DATE = "FRDERDAT";

    //Requirement Information
    public static final String REQ_COUNT = "PUCPS80C";
    public static final String REQ_PERS_SEQ_ID = "FPRPIDNT";
    public static final String REQ_CODE = "FPRREQTP";
    public static final String HO_REQ_REF_ID = "FPRCRTIM";
    public static final String REQ_STATUS = "FPRACTIN";
    public static final String REQ_DATE = "FPRCRDAT";
    public static final String REQ_RESTRICTION = "FPRRESTR";
    public static final String REQ_DEPT_DESK = "FPRDEPTE";
    public static final String REQ_OCCURRENCE = "FPROCCUR";
    public static final String REQ_ORDER_DATE = "FPRORDAT";

    //Requirement Receipt/Waive Information
    public static final String REQ_RECEIPT_WAIVE_REQUEST_DATE = "FSPRCDAT";
    public static final String REQ_RECEIPT_WAIVE_REQUEST_TIME = "FSPRCTIM";
    public static final String REQ_RECEIPT_WAIVE_OCCURRENCE = "FSPROCCR";
    public static final String REQ_RECEIPT_WAIVE_ACTION = "FSPACTIN";

    //Transaction Information
    public static final String TRANS_COVERAGE_PHASE = "FSPPHASE"; // phase code
    public static final String TRANS_BENEFIT = "FSBBENEF"; // benefit
    public static final String TRANS_PERSON = "FSPPERSN"; // person code

    //Rating transaction
    public static final String TRANS_REL_ROLE_CODE = "FSPTRELN"; //person code
    public static final String TRANS_RELATED_REF_ID = "FSPTRELQ"; //person seq
    public static final String TRANS_COVERAGE_KEY = "FSPTRELP"; //phase code
    public static final String TRANS_PARTY_ID = "FSPTRELI"; //party_id
    public static final String TRANS_RATING_TYPE = "FSPTRELT"; //rating type
    public static final String TRANS_RATING_SUBTYPE = "FSPTRELS"; //rating subtype
    public static final String TRANS_RATING_SEGMENT_ID = "FSPTRLID"; //rating trailer id field	

    //Approve coverage
    public static final String APPROVE_UNDERWRITING_TYPE = "FSPAPTYP";

    //COVERAGE
    public static final String COV_COUNT = "PUCPS02C";
    public static final String COVERAGE_ID = "FCVPIDNT";
    public static final String COV_KEY = "FCVPHASE";
    public static final String COV_PROD_CODE = "FCVPDSKY";
    public static final String COV_STATUS = "FCVNCTYP";
    public static final String COV_STATUS_2 = "FCVFLGB5";
    public static final String COV_TYPE_CODE_1 = "FCVPROD";
    public static final String COV_TYPE_CODE_2 = "FCVPSTYP";
    public static final String COV_IND_CODE = "FCVFLGD0";
    public static final String COV_LIVES_TYPE = "FCVLIVES";
    public static final String COV_CURR_AMT_1 = "FCVFACE";
    public static final String COV_CURR_AMT_2 = "FCVUNITS";
    public static final String COV_ANN_PREM = "FCVPREMA";
    public static final String COV_EFF_DATE = "FCVISSDF";
    public static final String COV_TERM_DATE = "FCVTCGDT";
    public static final String COV_UNIT_TYPE = "FCVFLGB0";
    public static final String COV_ISSUE_AGE = "FCVAGEIS";

    //COVOPTION-BENEFIT OBJECT
    public static final String COV_OPT_COUNT = "PUCPS04C";
    public static final String COV_OPT_BENE_PHASE = "FSBBPHS";
    public static final String COV_OPT_TYPE = "FSBBENEF";
    public static final String COV_OPT_ANN_PREM_AMT = "FSBPREMA";
    public static final String COV_OPT_OPTION_AMT = "FSBBUNT";
    public static final String COV_OPT_EFFDATE = "FSBBISDF";
    public static final String COV_OPT_TERMDATE = "FSBBCDTE";
    public static final String COV_OPT_STATUS = "FSBFLGB7";
    public static final String COV_OPT_INV_IND = "FSBFLGA7";
    public static final String COV_OPT_PERC_TYPE = "FSBFLGA4";
    public static final String COV_OPT_PERC = "FSBBRAT";
	public static final String COV_OPT_PERSON_IDREF = "FSBPIDNT"; //NBA111
	public static final String COV_OPT_LIVES_TYPE = "FSBFLGA3"; //NBA111
	public static final String COV_OPT_ACTIVE_IND = "FSBBCUSE"; //SPR2229
	

    //Substandard Rating
 
    public static final String SUB_STAND_COUNT = "PUCPS03C";
    public static final String SUB_STAND_PERS_SEQ = "FELPIDNT";
    public static final String SUB_STAND_COV_ID = "FELEPHS";
    public static final String SUB_STAND_RATE_TYPE = "FELETYP";
    public static final String SUB_STAND_RATE_SUBTYPE = "FELSBTYP";
    public static final String SUB_STAND_TABLE_RATE = "FELETABL";
    public static final String SUB_STAND_PERCENT = "FELPCTRT";
    public static final String SUB_STAND_FLAT_EXTRA = "FELFLTEX";
    public static final String SUB_STAND_END_DATE = "FELETDAT";
    public static final String SUB_STAND_START_DATE = "FELEFFDF";
    public static final String SUB_STAND_REASON = "FELEREAS";
    public static final String SUB_STAND_COMM = "FELECOM";
    public static final String SUB_STAND_DURATION = "FELEMOYR";
    public static final String SUB_STAND_EXTRA_UNIT = "FELEAP";
    public static final String SUB_STAND_RATE_STATUS = "FELEDUSE";
    public static final String SUB_STAND_ANN_PREM = "FELPREMA";
    public static final String SUB_STAND_FLAG = "FELFLGB0";
	public static final String SUB_STAND_TEMP_FLAG = "FELFLGB4";	//SPR1549
	public static final String SUB_STAND_RESERVES_PLAN = "FELEPLN";  //NBA104
	public static final String SUB_STAND_ORIG_CSE_DATE = "FELOCSDF";  //NBA104
    public static final char SUB_STAND_TYPE_PERCENT = '0';
    public static final char SUB_STAND_TYPE_PERM_TABLE = '1';
    public static final char SUB_STAND_TYPE_PERM_FLAT = '2';
    public static final char SUB_STAND_TYPE_TEMP_TABLE = '3';
    public static final char SUB_STAND_TYPE_TEMP_FLAT = '4';
    //Begin NBA093
    public static final String SUB_TYPE_PERCENT = "0";
    public static final String SUB_TYPE_PERM_TABLE = "1";
    public static final String SUB_TYPE_PERM_FLAT = "2";
    public static final String SUB_TYPE_TEMP_TABLE = "3";
    public static final String SUB_TYPE_TEMP_FLAT = "4";
    //End NBA093
    public static final String BENEFIT_RATING_TYPE = "0";
    public static final long SUB_STAND_ACTIVE_STATUS = 1;

    public static final String BENE_INTEREST_AMT = "FBDBNAMT";
    public static final String BENE_SUB_COUNT = "FBDNOENT";
    public static final String BENE_SUB_REP_COUNT = "PUCSX96C";
    public static final String BENE_COUNT = "PUCPS96C";
    public static final String BENE_ORIG_ID = "FBDCVPID";
    public static final String BENE_SUB_ID = "FBDPRPID";

    //Endorsement transaction 
    public static final String ENDORS_TRANS_IND = "FSPDLTCD";
    public static final String FSPBUSE_IND = "FSPBUSE";
    public static final String ENDORSE_TYPE = "FSPENDCD";
    public static final String ENDORSE_DETAIL_IND = "FSPCAUTN";
    public static final String ENDORSE_DATE = "FSPERDAT";
    public static final String ENDORSE_PERS_ID = "FSPTRPID";
    public static final String FSPBENEF = "FSPBENEF";

    // Final Disposition underwriting status
    public static final String FINAL_DISPOSITION_UNDERWRITING_STATUS = "FSPACTCD";
    // Final Disposition requested issue date
    public static final String FINAL_DISPOSITION_REQUESTED_ISSUE_DATE = "FSPISDAT";

	// Target Year
	public static final String TARGET_DATE = "FTPDATE";	//NBA104
    // Target Year for IRA tax control (TX) targets
    public static final String TARGET_TX_YEAR = "FTPYEAR";	//NBA104
	// Target Year for Roth Conversion (RC) targets 
	public static final String TARGET_ROTH_YEAR = "FTPRCYR";	//NBA104
	// Target Code
	public static final String TARGET_CODE = "FTPCODE";	//NBA104
	// Target Code for additional single premium	 
	public static final String TARGET_CODE_ADDL_SINGLE_PREM = "AS";	//NBA104	
	// Target Code for planned 1035 exchange premium	 
	public static final String TARGET_CODE_1035_PREM = "AT";	//NBA104
	// Target Code for commission target 
	public static final String TARGET_COMMIS = "CT";	//NBA104	
	// Target Code for initial net annual premium surrender target
	public static final String TARGET_INAP_SURR = "IP";	//NBA104	
	// Target Code for increasing death benefit
	public static final String TARGET_IDB = "I0";	//NBA104	
	// Target Code for premium target
	public static final String TARGET_PREMIUM = "LT";	//NBA104	
	// Target Code for monthly minimum annual premium
	public static final String TARGET_MAP = "MT";	//NBA104	
	// Target Code for ROTH conversion control
	public static final String TARGET_ROTH_CONVERSION = "RC";	//NBA104	
	// Target Code for surrender
	public static final String TARGET_SURRENDER = "ST";	//NBA104	
	// Target Code for total TEFRA/DEFRA guideline level annual premium
	public static final String TARGET_TOTAL_GLP = "TA";	//NBA104	
	// Target Code for death benefit
	public static final String TARGET_DEATH_BENEFIT = "TD";	//NBA104	
	// Target Code for total TEFRA/DEFRA guideline single premium
	public static final String TARGET_TOTAL_GSP = "TS";	//NBA104	
	// Target Code for IRA tax control
	public static final String TARGET_CODE_TAX_CONTROL = "TX";	//NBA104
	// Target Phase Code
	public static final String TARGET_PHASE = "FTPPHASE";	//NBA104
	// IDB Target rule 
	public static final String TARGET_IDB_RULE = "FTPRULE";	//NBA104
	// IDB Target increase percentage
	public static final String TARGET_IDB_PCT = "FTPINCPC";	//NBA104
	// IDB Target number of increase years
	public static final String TARGET_IDB_YEARS = "FTPNUMYR";	//NBA104
	// Target Amount
	public static final String TARGET_AMOUNT = "FTPAMT";
	// Previously Reported Taxable Year 
	public static final String TARGET_PREV_REPTD_TAXABLE_YR = "FTPDATRC";	//NBA104
	// Additional single premium amount - Retrieve Only 
	public static final String TARGET_ADDL_SINGLE_PREM_AMT_RETRIEVE = "PUSPASAM";	//NBA104
	// Initial net annual premium surrender target amount - Retrieve Only 
	public static final String TARGET_INAP_AMT_RETRIEVE = "PUSPINAM";	//NBA104
	// Increasing death benefit rule - Retrieve Only 
	public static final String TARGET_IDB_RULE_RETRIEVE = "PUSPI0RL";	//NBA104
	// Increasing death benefit percentage - Retrieve Only 
	public static final String TARGET_IDB_PCT_RETRIEVE = "PUSPI0PC";	//NBA104	
	// Increasing death benefit years - Retrieve Only 
	public static final String TARGET_IDB_YEARS_RETRIEVE = "PUSPI0NY";	//NBA104		
	// Increasing death benefit years - Retrieve Only 
	public static final String TARGET_ROTH_TAX_YEAR_RETRIEVE = "PUSPRCYR";	//NBA104	
	// Increasing death benefit years - Retrieve Only 
	public static final String TARGET_ROTH_AMOUNT_RETRIEVE = "PUSPRCAM";	//NBA104
	// IRA tax control year - Retrieve Only 
	public static final String TARGET_TAX_YEAR_RETRIEVE = "PUSPTXYR";	//NBA104
	// IRA tax control year - Retrieve Only 
	public static final String TARGET_TAX_AMOUNT_RETRIEVE = "PUSPTXAM";	//NBA104

   // NFO Option
    public static final String NFO_OPTION_A = "FBROPT";
    public static final String NFO_OPTION_B = "FCVRONFO";
	// NBA104 code deleted
    // (Holding.Party.Client) Will not be sent to the host. - Marginal Tax Bracket
    public static final String MARGINAL_TAX_BRACKET = "FMDMRGTX";

    // (Holding.Policy) This is used for both Bank Account numbers and Credit Card numbers. - Payee's Bank Acct. #
    public static final String PAYEE_BANK_ACCT = "FBCBKACT";

//	(Holding.Policy) Account Holder Name1
	 public static final String ACCOUNT_HOLDER_NAME = "FBCNAME";//SPR1073
	 
    // (Holding.Policy) Account Holder Name1
    public static final String ACCOUNT_HOLDER_NAME1 = "FBCNAME1";

    // (Holding.Policy) Account Holder Name2
    public static final String ACCOUNT_HOLDER_NAME2 = "FBCNAME2";

    // (Holding.Policy) Account Holder Name3
    public static final String ACCOUNT_HOLDER_NAME3 = "FBCNAME3";

    //  Billing Media code
    public static final String MEDIA_CODE = "FBCMEDIA";	//SPR2151
    
    // (Holding.Policy) Planned Additional Premium 
    public static final String PLANNED_ADDITIONAL_PREM = "FUSFPAPM";

    // (Holding.Policy.Annuity) 
    public static final String ANNU_KEY = "FCVPHASE";
    //This DXE creates a link between the party and the coverage within CyberLife
    public static final String ANNU_RID_KEY = "FCLPIDNT";//SPR1131

    // (Holding.Policy.Annuity) Initial Premium
    public static final String ANNU_INIT_PREM = "FTDPINIT";

    // (Holding.Policy.Annuity) Intial Premium Amount
    public static final String ANNU_INIT_PREM_AMT_1 = "PINITPRM";
	
	// SPR1986 code deleted
    // (Holding.Policy.Annuity.Payout) Adjusted Investment
    public static final String ANNU_PAYOUT_ADJ_INV = "FSPATAIV";

    // (Holding.Policy.Annuity.Payout) Exclusion Ratio
    public static final String ANNU_PAYOUT_EXCL_RATIO = "FSPATERT";

    // (Holding.Policy.Annuity.Payout) Annuity Settlement Option
    public static final String ANNU_PAYOUT_SETTLEMENT_OPT = "FCVFSOPT";

    // (Holding.Policy.Annuity.Payout) Certain Period
    public static final String ANNU_PAYOUT_CERTAIN_PER = "FSPATCPR";

    // (Holding.Policy.Annuity.Payout) Exclusion Amount
    public static final String ANNU_PAYOUT_EXCL_AMT = "FSPATERA";

    // (Holding.Policy.Annuity.Payout) Primary Percent
    public static final String ANNU_PAYOUT_PRIMARY_PCT = "FSPATJP1";

    // (Holding.Policy.Annuity.Payout) Secondary Percent
    public static final String ANNU_PAYOUT_SEC_PCT = "FSPATJP2";

    // (Holding.Policy.Annuity.Payout) Payout Start Date
    public static final String ANNU_PAYOUT_STRT_DT = "FSPATSDT";

    // (Holding.Policy.Annuity.Payout) Payout Start Date
    public static final String ANNU_PAYOUT_STRT_DAY = "FSPATDAY";

    // (Holding.Policy.Annuity) Premium type of annuity.  Premium for annuity as a single, fixed, or flexible.
    public static final String ANNU_PREM_TYPE = "XXXXXXXX";

    // (Holding.Policy.Annuity) Qualification Type
    public static final String ANNU_QUAL_TYPE = "FCVFQTYP";

    // (Holding.Policy.Annuity.Rider) Rider number of Units
    public static final String ANNU_RIDER_NUM_UNITS = "FCVUNITS";

    // (Holding.Policy.Annuity) Administration provided code to identify rider
    public static final String ANNU_RIDER_CD = "FCVPDSKY";

    // (Holding.Policy.Annuity) RMD Calculation Indicator (Initial or Recalculate)
    public static final String ANNU_RMD_CALC_IND_INIT = "FDENRMDC";

    // (Holding.Policy.Annuity) RMD Calculation Indicator (Initial or Recalculate)
    public static final String ANNU_RMD_CALC_IND_RECALC = "FDENRMDP";

    // (Holding.Policy.Annuity) Total or face amount of rider, or total options elected, or lifetime amount of benefit.
    public static final String ANNU_TOT_FACE_AMT = "FCVFACE";

    // (Holding.Policy.ApplicationInfo) App written State
    public static final String APP_WRITTEN_STATE = "FNBSTWCD";

    // (Holding.Policy.ApplicationInfo) Application Type
    public static final String APP_TYPE = "FBRCRCTYP";

    // (Holding.Policy.ApplicationInfo) Folder Location
    public static final String APP_FLDR_LOC = "FNBFILOC";

    // (Holding.Policy.ApplicationInfo) Type of Form Received
    public static final String APP_FORM_TYPE_RECV = "FNBSFOBX";

    // (Holding.Policy.ApplicationInfo) Number assigned to application form type.
    public static final String APP_FORM_TYPE_NUM_ASSN = "FCVPLFNO";

    // (Holding.Policy.ApplicationInfo) Application Number
    public static final String APP_ASSN_NUM = "FNBAPPNO";

    // (Holding.Policy.ApplicationInfo) Underwriter ID
    public static final String APP_UNDERWRITER_ID = "FNBUNDID";

    // (Holding.Policy.ApplicationInfo) Last Accounting Date
    public static final String APP_LST_ACCT_DT = "FBRPAYDT";

    // (Holding.Policy.ApplicationInfo) Last Ann. Processed
    public static final String A_LAST_ANNIV_PROC = "FBRLSTDF";

    // (Holding.Policy.ApplicationInfo) Indicates whether or not the MIB Authorization has already been received.
    public static final String RECV_FLAG_A = "FRCFLAGA";

    // (Holding.Policy.ApplicationInfo) Department Desk Code/Clerk Id
    public static final String APP_DEPT_DESK_CLERK_ID = "FNBACKID";

    // (Holding.Policy.ApplicationInfo) Pension Code
    public static final String APP_PENSION_CD = "FBRPENSN";

    // (Holding.Policy.ApplicationInfo) Replacement Code
    public static final String APP_REPLACEMENT_CD = "FNBRPINS";

    // (Holding.Policy.ApplicationInfo) Requested Issue Date
    public static final String APP_REQ_ISS_DT = "FNBITDAT";

    // (Holding.Policy.ApplicationInfo) Restriction Code
    public static final String APP_RESTRICT_CD = "FBRRESTR";

    // (Holding.Policy) Type of Bank Account
    public static final String BANK_ACCT_TYPE = "FBCACTYP";

    // (Holding.Policy) Billed To Date
    public static final String BILLED_TO_DT = "FBRBILTD";

    // (Holding.Policy) Bill Control Eff. Date
    public static final String BILL_CONTROL_EFF_DT = "FBCEFFDT";

    // (Holding.Policy) List bill number
    public static final String LIST_BILL_NUM = "FBCBBNUM";
    // (Holding.Policy) Billing Ident
    public static final String BILL_CONTROL_IDENT = "FBCPIDNT";

    // (Holding.Policy) Branch Number
    public static final String BRANCH_NUM = "FBCTRNBR";

    // (Holding.Policy) Calendar Year Override Indicator
    public static final String CAL_YR_OVERRIDE_IND = "FCWTAXYR";

    // (Holding.Policy) Confimation Frequency
    public static final String CONFIRMATION_FREQ = "FULCNFRQ";

    // (Holding.Policy) Credit Card Exp. Date
    public static final String CREDIT_CARD_EXP_DT = "FBCCEXPR";

    // (Holding.Policy) Credit Card Type
    public static final String CREDIT_CARD_TYPE = "FBCCTYPE";
    //begin NBA122
	// (Holding.AssignmentCode) Assignment code
	public static final String ASSIGNMENT_CODE = "FBRRESTA";
	//end NBA122	
	//begin SPR1073
	// Holding.Banking BankingKey
	public static final String INF_BILLING_KEY = "FBCOCCUR";
	
	// Holding.Banking.CreditDebitType
	public static final String INF_BILLING_CRDR_TYPE = "FBCVRSN";
	//end SPR1073
	
    // (Holding.Policy.FinancialActivity.CWAActivity) Best Interest Indicator
    public static final String BEST_INT_IND = "FCWOVTYP";

    // (Holding.Policy.FinancialActivity.CWAActivity) Cost Basis Adj. Amount
    public static final String COST_BASIS_ADJ_AMT = "FCWCOSTB";

    // (Holding.Policy.FinancialActivity.CWAActivity) User costAdj to send to host for dtrain - Cost Basis Amount
    public static final String COST_BASIS_AMT = "COSTBASE";

    // (Holding.Policy.FinancialActivity.CWAActivity) Accounting Override - Credit
    public static final String ACCTG_OVERRIDE_CRED = "FCWCACCT";

    // (Holding.Policy.FinancialActivity.CWAActivity) Overrride CWA Agent ID
    public static final String OVERRIDE_CWA_AGENT_ID = "FCWAGENT";

    // (Holding.Policy.FinancialActivity.CWAActivity) If the asof date of  the CWA payment is to be the issue date, turn on bit in FCWGLAGB. - Set CWA Date to Issue Indicator
    public static final String CWA_FLAG_B = "FCWFLAGB";

    // (Holding.Policy.FinancialActivity.CWAActivity) Override Acct. Destination Code
    public static final String OVERRIDE_ACCT_DEST_CD = "FCWDEST";

    // (Holding.Policy.FinancialActivity.CWAActivity) Override Acct. Source Code
    public static final String OVERRIDE_ACCT_SRC_CD = "FCWSOURC";

    // (Holding.Policy.FinancialActivity.CWAActivity) Accounting Override - Debit
    public static final String ACCT_OVERRIDE_DEB = "FCWDACCT";

    // (Holding.Policy.FinancialActivity.CWAActivity) Override Interest
    public static final String OVERRIDE_INT = "FCWOVINT";

    // (Holding.Policy) SF First Monthly Date
    public static final String SF_FIRST_MONTHLY_DT = "FFCLDEDT";

    // (Holding.Policy) SF Deduction Date
    public static final String SF_DEDUCTION_DT = "FDELDEDT";

    // (Holding.Policy.Investment.SubAccount) Fund Allocation Percent
    public static final String FUND_ALLOC_PCT = "FALALPER";

    // (Holding.Policy.Investment.SubAccount) Fund Allocatin Units
    public static final String FUND_ALLOC_UNITS = "FALALUNI";

    // (Holding.Policy.Investment.SubAccount) Fund ID
    public static final String FUND_ID = "FALFNDID";

    // (Holding.Policy.Investment.SubAccount) Primary Investment Objective (Growth, Investment, or Both)
    public static final String PRIM_INV_OBJ = "FMDINVOB";

    // (Holding.Policy.Investment.SubAccount) Fund Allocation Type
    public static final String FUND_ALLOC_TYPE = "FALALVAL";

    // (Holding.Policy.Investment.SubAccount) Fund Allocation Value
    public static final String FUND_ALLOC_VAL = "FALALDOL";

    // (Holding.Policy) Last Billing Date
    public static final String LST_BILL_DT = "FBRBILDT";

    // (Holding.Policy) Last Billing Kind
    public static final String LST_BILL_KIND = "FBRBILKD";

    // (Holding.Policy.Life.Coverage) Acutal Cease Date
    public static final String ACT_CEASE_DT = "FSBBCDTE";

    // (Holding.Policy.Life.Coverage.CovOption) Coverage Option Key
    public static final String COV_OPT_KEY = "FSBBPHS";

    
    // (Holding.Policy.Life.Coverage.CovOption) Not applicable if Product Type = 'F'.   - Benefit Number of Units
    public static final String BENEFIT_NUM_UNITS = "FSBBUNT";

    // (Holding.Policy.Life.Coverage) For coverage phase 1.  Only applicable if Product Type = 'U' - Death Benefit Option
    public static final String DEATH_BENEFIT_OPT = "FULPLOPT";

    // (Holding.Policy.Life.Coverage) FCVFLAGA bit 3  ISS045 Exempt from Guideline XXX (Reentry) - Guideline xxx Exempt Indicator
    public static final String COV_FLAG_A = "FCVFLAGA";

    // (Holding.Policy.Life.Coverage) Form Number
    public static final String FORM_NUMBER = "FCVPLFNO";

    // (Holding.Policy.Life.Coverage) Unisex Override Indicator
    public static final String UNISEX_OVERRIDE_IND = "FCVUOIND";

    // (Holding.Policy.Life.Coverage) Unisex Override Code
    public static final String UNISEX_OVERRIDE_CD = "FCVUSEXC";

    // (Holding.Policy.Life.Coverage) Unisex Override Subseries
    public static final String UNISEX_OVERRIDE_SUBSERIES = "FCVUSUBC";

    // (Holding.Policy.Life) Only applicable if Product Type = 'I', 'N' or 'O' - Secondary Div. Option
    public static final String SEC_DIV_OPT = "FBRPROSD";

    // (Holding.Policy.Life) Only applicable if Product Type Code = 'I', 'N' or 'O' - Primary Dividend Option
    public static final String PRIM_DIV_OPT = "FCVRODIV";

    // (Holding.Policy.Life) SF Excess Premium
    public static final String SF_EXCESS_PREM = "FDEECP";

    // (Holding.Policy.Life.LifeUSA) The owners cost basis in the policy that is being exchanged.
    public static final String POL_EXCH_COST_BASIS = "XXXXXXXX";

    // (Holding.Policy.Life.LifeUSA) 1035 Exchange Indicator
    public static final String POL_1035_EXCH_IND = "FCV1035X";

    // (Holding.Policy.Life.LifeUSA) Pre-TEFRA Indicator
    public static final String CW_FLAG_B = "FCWFLAGB";

	// (Holding.Policy.Life.LifeUSA) MEC Indicator
	public static final String POL_MEC_IND = "FBRMECIN"; //SPR2099

    // (Holding.Policy) Exception Note Date
    public static final String EXC_NOTE_DT = "FENNODAT";

    // (Holding.Policy) Exception Note Department/Desk Code
    public static final String EXC_NOTE_DEPT_DESK_CD = "FENDEPT";

    // (Holding.Policy) Exception Note Text
    public static final String EXC_NOTE_TXT = "FENVARBL";

    // (Holding.Policy.Notify) Notify Code
    public static final String NOTIFY_CD = "FNOCNTIF";

    // (Holding.Policy.Notify) Notify Date
    public static final String NOTIFY_DT = "FNOCDATE";

    // (Holding.Policy) First Notice Extract Day
    public static final String FRST_NOTICE_EXTR_DAY = "FBRDUEDY";

    // (Holding.Policy) SF Payroll Frequency
    public static final String SF_PAYROLL_FREQ = "FFCPYDFR";

    // (Holding.Policy) ControlCoverage.ctrlPlanData.prodType and prodSubType for the primary coverage. - Product Type
    public static final String COV_PROD_TYPE = "FCVPROD";

    // (Holding.Policy) Quoted Mode Premium
    public static final String QUOTED_MODE_PREM = "FFCQPAMT";

    // (Holding.Policy) SF Quoted Prem. Basis Ind.
    public static final String SF_QUOTED_PREM_BASIS_IND = "FFCQPBAS";

    // SPR1986 code deleted

    // (Holding.Policy) Why aren't these also in policy? - Requested date of the insured at which the contract matures.
    public static final String REQ_INSURED_MATURITY_DT = "MATDAT";

    // (Holding.Policy) Transit Routing #
    public static final String TRANSIT_ROUTING_NUM = "FBCTRNSO";

    // (Holding.Policy) Handling
    public static final String HANDLING = "FBRHANDL";

    // (Holding.Policy) Statement Frequency
    public static final String STATEMENT_FREQ = "FULASFRQ";

    // (Holding.Policy) Timing for Notice
    public static final String TIMING_FOR_NOTICE = "FBRDUEUS";

    // (IHolding.Policy) SF Bill To Date
    public static final String SF_BILL_TO_DT = "FFCINBDT";

    // (Party.Address) Address Type
    public static final String ADDR_TYPE = "FNAADRTY";

    // (Party.Client) Will not be sent to the host. - Employer Name
    public static final String EMPLOYER_NM = "FMDEMPNM";

    // (Party.Client) Will not be sent to the host. - Number of Dependents
    public static final String NUM_DEPENDENTS = "FMDNODEP";

    // (Party.Client) Will not be sent to the host. - Reason Spouse Not Covered
    public static final String REASON_SPOUSE_NOT_COVERED = "FMDSPSRS";

    // (Party.Client) Will not be sent to the host. - Total Spouse Insurance
    public static final String TOT_SPOUSE_INS = "FMDSPSIA";

    // (Party.Client) Will not be sent to the host. - Spouse Alias Name
    public static final String SPOUSE_ALIAS_NM = "FMDSPSMN";

    // (Party.Client) Dependents
    public static final String ST_DEPENDENTS = "FLCSTDEP";

    // (Party.Client) Exemptions
    public static final String ST_EXEMPTIONS = "FLCSTEX";

    // (Party.EmailAddress) Type of e-mail address
    public static final String EMAIL_TYPE = "FECUSECD";

    // (Party.Person.Producer.CarrierAppointment) Writing Agent
    public static final String WRITING_AGT = "FAGAGTID";

    // (Party.Phone) Set concatenation of <AreaCode>and <DialNumber. To FLCPHONE or FLC2PHN depending on the value of <PhoneTypeCode(FLC2PHNT)> - Area Code
    public static final String AREA_CODE_A = "FLCPHONE";
    public static final String AREA_CODE_B = "FLC2PHN";

    // (Party.Phone) Country Code
    public static final String COUNTRY_CODE = "Not Supported?";

    // (Party) Correspondence Preference 
    public static final String CORRESP_PREF = "FLCMEDIA";

    // (Party) Application Resident Area
    public static final String APP_RESIDENT_AREA = "FBRAREA";

    // (Party) Will not be sent to the host. - State Withholding (None/Standard)
    public static final String ST_WITHHOLDING = "FLCSTWHI";

    // (Party) Federal Withholding Indicator (None/Standard/Backup
    public static final String FEDERAL_WITHHOLDING_IND = "FLCW9IND"; //SPR1061

    // (Relation) Description code to further define the role of the relationship I.e., the relationrolecode would contain a value of spouse the relation description would contain a value for husband or wife.
    public static final String BENEF_REL_ROLE = "FBDBNREL";

    // (Relation) Distribution type
    public static final String DIST_TYPE = "FBDBDIST";

    // (Relation) Beneficiary Type (Prim., Contingent, Assignee)
    public static final String BENEF_TYPE = "FBDBTYPE";

    // (Relation) Amount of distribution for beneficiary
    public static final String AMT_OF_DISTR_FOR_BENEF = "FBDBNAMT";

    // (Relation) Interest Percent
    public static final String INTEREST_PERCENT = "FLCOWNER";
    
	// (Relation) Owner Type
	 public static final String OWNER_TYPE = "FLCOWNTP";//SPR2287

    // (Relation) If the interest percent for a beneficiary equals 100% the Beneficiary Distribution Option should be set to Balance and the Interest Percent amount should not be sent to the host. - Percentage of distribution for beneficiary
    public static final String PCTG_DIST_FOR_BENEF = "FBDBNPCT";

    // (Relation) Joint Beneficiary Indicator
    public static final String JOINT_BENEF_IND = "FLCFLAGA";

    // (Relation.RelationProducer) Comm Advanced Amount is mutually exclusive with Comm Advanced Percent
    public static final String COMM_ADV_AMT = "FAVACAMT";

    // (Relation.RelationProducer) Comm Advanced Waive Minimum check Indicator
    public static final String AV_FLAG_B1 = "FAVFLAGB";

    // (Relation.RelationProducer) Comm Advanced Percent is mutually exclusive with Comm Advanced Amount
    public static final String COMM_ADV_PCT = "FAVAVPCT";

    // (Relation.RelationProducer) Situation Code
    public static final String SITUATION_CD = "FAGSITCD";

    // (Relation.RelationProducer) Volume Share Percent
    public static final String VOL_SHARE_PCT = "FAGPCTSV";

    // (Relation) Identifies the type of relationship.
    public static final String RELATIONSHIP_TYPE = "FLCPERSN";
    
    //Requested maturity date, age, duration
	public static final String RQST_MATURITY_DATE = "FULMTDAT";	//SPR1986
	public static final String RQST_MATURITY_AGE = "FTDMATUR";	//SPR1986
	public static final String RQST_MATURITY_DUR = "FTDMATDU";	//SPR1986
	public static final String RQST_MATURITY_DATE_OVERRIDE_66 = "FULFLGD0";	//SPR1986
	public static final String RQST_MATURITY_DATE_OVERRIDE_E0 = "FTDMATYP";	//SPR1986
	public static final String RQST_MATURITY_DATE_OVERRIDE_E0_FLG = "XX100000";	//SPR1986
	public static final String RQST_MATURITY_DATE_FLG = "FULFLAGD";	//NBA104
	
	//TAMRA  
	public static final String TAMRA_TYPE = "FTMSGTYP";	//NBA104
	public static final String TAMRA_TYPE_1 = "1";	//NBA104
	public static final String TAMRA_TYPE_2 = "2";	//NBA104
	//TAMRA  Type 1
	public static final String TAMRA_FLAGA_BIT0 = "FTMFLGA0";	//NBA104
	public static final String TAMRA_FLAGA_BIT1 = "FTMFLGA1";	//NBA104
	public static final String TAMRA_FLAGA_BIT3 = "FTMFLGA3";	//NBA104
	public static final String TAMRA_FLAGA_BIT5 = "FTMFLGA5";	//NBA104
	public static final String TAMRA_MEC_IND = "FTMMECIN";	//NBA104
	public static final String TAMRA_RATES_IND = "FTMTAMRR";	//NBA104
	public static final String TAMRA_MEC_DATE = "FTMMECDT";	//NBA104
	public static final String TAMRA_7PAY_START_DATE = "FTM7PPDT";	//NBA104
	public static final String TAMRA_7PAY_CHANGE_DATE = "FTM7PPNC";	//NBA104
	public static final String TAMRA_7PAY_LEVEL_PREM = "FTM7PPRM";	//NBA104
	public static final String TAMRA_7PAY_CURR_RATE = "FTM7PPCR";	//NBA104
	public static final String TAMRA_7PAY_CURR_GUAR = "FTM7PPGD";	//NBA104
	public static final String TAMRA_7PAY_SPEC_AMT = "FTM7PPSA";	//NBA104
	//TAMRA  Type 2
	public static final String TAMRA_GUIDELINE_SRCH_KEY_BASE = "FTMIAFGK";	//NBA104
	public static final String TAMRA_7PAY_SRCH_KEY_BASE = "FTMIAF7K";	//NBA104
	public static final String TAMRA_SRCH_KEY_PHASE_RDR = "FTMCVPHS";	//NBA104
	public static final String TAMRA_GUIDELINE_SRCH_KEY_RDR = "FTMCGLPK";	//NBA104
	public static final String TAMRA_7PAY_SRCH_KEY_RDR = "FTMC7PYK";	//NBA104
	//Coverage Cease Date Indicator
	public static final String CEASE_DATE_FLAG = "FCVFLGC0";//SPR1952
	public static final String INVALID_COVERAGE_STATUS = "21";//SPR2319
	
	//begin SPR2737
	//Constant for result codes returned from HOST 
	public static final int SUCCESS = 0;
	public static final int SUCCESS_FORCIBLE = 1;
	public static final int DATA_FAILURE = 2;
	public static final int TRANSACTION_FAILURE = 5;
	public static final int HOST_UNAVAILABLE = 6;
	public static final int HOST_ABEND = 7;
	public static final int BAD_INFO_RETURNED = 9;
	//end SPR2737
	
	//begin NBA211
	public static final String FUND_ALLOC_COUNT = "PUCPX57C";
	public static final String FUND_ALLOC_PLACEHOLDER = "PUCPX57P";
	public static final String FUND_PRODUCT_CODE = "TPLFNDID";
	public static final String FUND_ALLOC_PERCENT = "TPLALPER";
	//end NBA211
	
}
