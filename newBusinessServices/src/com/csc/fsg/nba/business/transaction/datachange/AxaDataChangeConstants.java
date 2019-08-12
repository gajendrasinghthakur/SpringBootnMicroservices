package com.csc.fsg.nba.business.transaction.datachange;
/*
 * ************************************************************** <BR>
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
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */
/**
 * 
 * Helpers classes to determine Data change 
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>AXAL3.7.07</td><td>AXA Life Phase 2</td><td>Data Change Architecture</td>
 * <tr><td>AXAL3.7.21</td><td>AXA Life Phase 1</td><td>Prior Insurance</td></tr>
 * <tr><td>AXAL3.7.22</td><td>AXA Life Phase 1</td><td>Compensation Interface</td></tr>
 * <tr><td>P2AXAL007</td><td>AXA Life Phase 2</td><td>Producer and Compensation</td></tr>
 * <tr><td>P2AXAL038</td><td>AXA Life Phase 2</td><td>Zip Code Interface</td></tr>
 * <tr><td>P2AXAL041</td><td>AXA Life Phase 2</td><td>Message received from OLSA Unit Number Validation Interface</td></tr>
 * <tr><td>CR61627</td><td>AXA Life Phase 2</td><td>Assign Replacement Case Manager</td></tr>
 * <tr><td>P2AXAL053</td><td>AXA Life Phase 2</td><td>R2 Auto Underwriting</td></tr>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */


public interface AxaDataChangeConstants {
	
	//APP INFO DATA CHANGE CONSTANTS
	public final static  long DC_SIGNEDDATE = 100;
	public final static  long DC_UNDERWRITING_STATUS = 101;
	public final static  long DC_FACE_AMT = 102;
	public final static  long DC_APP_STATE = 103;
	public final static  long DC_ISSUEDATE = 104; //AXAL3.7.21
	public final static  long DC_POL_REQUESTEDDATE = 105; //ALS4153
	public final static  long DC_UND_APPROVAL_CHANGED = 106; //ALS4153
	public final static  long DC_APPL_TYPE_CHG = 107; //ALS5706
	public final static  long DC_INFORMAL_UNAPPROVED = 107; //ALS5701	
	public final static  long DC_REPLACEMENT_IND = 108; //CR61627
	public final static  long DC_DIST_CHANNEL_CHANGED=109; //NBLXA-1538
	//NEW CONTRACT ADDED  
	public final static  long DC_NEW_CONTRACT = 111;
	
	//POLICY DATA CHANGE CONSTANTS
	public final static  long DC_PRODUCTCODE = 200;
	public final static  long DC_PENDING_CONTRACT_STATUS = 201;
	public final static  long DC_BILLING_UNIT_NUMBER = 202; //P2AXAL041
	public final static  long DC_PAYMENT_METHOD = 203; //P2AXAL041
	public final static  long DC_PAYMENT_AMOUNT = 204; //APSL4112
	public final static  long DC_GOLDEN_IND = 205; //NBLXA-1831
	//PARTY DATA CHANGE CONSTANTS
	public final static  long DC_OWNER_CHANGE = 301;
	public final static  long DC_OWNER_NAME = 302;
	public final static  long DC_OWNER_SSN = 303;
	public final static  long DC_OWNER_SSNTYPE = 304;
	public final static  long DC_OWNER_PARTY_TYPE = 305;
	public final static  long DC_OWNER_DELETE = 306;
	public final static  long DC_OWNER_ADDRESS = 307;
	public final static  long DC_OWNER_DOB = 308;
	public final static  long DC_OWNER_GENDER = 309;
	public final static  long DC_OWNER_ADDED = 310; //AXAL3.7.21
	public final static  long DC_INSURED_CHANGE = 311;
	public final static  long DC_INSURED_NAME = 312;
	public final static  long DC_INSURED_SSN = 313;
	public final static  long DC_INSURED_PARTY_TYPE = 314;
	public final static  long DC_INSURED_DELETE = 315;
	public final static  long DC_INSURED_ADDRESS = 316;
	public final static  long DC_INSURED_DOB = 317;
	public final static  long DC_INSURED_GENDER = 318;
	public final static  long DC_INSURED_SSNTYPE = 319;
	//Begin AXAL3.7.21
	public final static  long DC_INSURED_FIRSTNAME = 320;
	public final static  long DC_INSURED_LASTNAME = 321;
	public final static  long DC_INSURED_MIDDLENAME = 322;
	public final static  long DC_INSURED_SUFFIX = 323;
	public final static  long DC_INSURED_PREFIX = 324;
	public final static  long DC_INSURED_ADDED = 325;
	public final static  long DC_INSURED_DELETED = 326;
	public final static  long DC_OWNER_DELETED = 327;
	//End AXAL3.7.21
	public final static  long DC_INSURED_AGE = 328; //ALS4153
	
	public final static  long DC_PRIMARY_AGENT_CHANGE = 330;
	public final static  long DC_PRIMARY_AGENT_ADD = 331;
	public final static  long DC_PRIMARY_AGENT_DELETE = 332;
	public final static  long DC_AGENT_COUNT = 333;
	public final static  long DC_REPL_TYPE_CHG = 334;
	public final static  long DC_AGENT_INELIGIBLE_TO_ELIGIBLE = 335; //AXAL3.7.22
	public final static  long DC_AGENT_ELIGIBLE_TO_ELIGIBLE = 336; //AXAL3.7.22
	public final static  long DC_AGENT_ELIGIBLE_TO_INELIGIBLE = 337; //AXAL3.7.22
	public final static  long DC_ADDI_AGENT_CHANGE = 338; //ALS4908
	public final static  long DC_ADDI_AGENT_ADD = 339; //ALS4908
	public final static  long DC_ADDI_AGENT_DELETE = 340; //ALS4908

	//Address Change for Zip Code Validation
	public final static  long DC_ADDRESS_ZIP = 341; //P2AXAL038
		
	//Begin P2AXAL053
	public final static  long DC_JNT_INSURED_CHANGE = 342;
	public final static  long DC_JNT_INSURED_NAME = 343;
	public final static  long DC_JNT_INSURED_SSN = 344;
	public final static  long DC_JNT_INSURED_PARTY_TYPE = 345;
	public final static  long DC_JNT_INSURED_DELETE = 346;
	public final static  long DC_JNT_INSURED_ADDRESS = 347;
	public final static  long DC_JNT_INSURED_DOB = 348;
	public final static  long DC_JNT_INSURED_GENDER = 349;
	public final static  long DC_JNT_INSURED_SSNTYPE = 350;
	public final static  long DC_JNT_INSURED_FIRSTNAME = 3510;
	public final static  long DC_JNT_INSURED_LASTNAME = 352;
	public final static  long DC_JNT_INSURED_MIDDLENAME = 353;
	public final static  long DC_JNT_INSURED_SUFFIX = 354;
	public final static  long DC_JNT_INSURED_PREFIX = 355;
	public final static  long DC_JNT_INSURED_ADDED = 356;
	public final static  long DC_JNT_INSURED_DELETED = 357;
	//End P2AXAL053
	public final static  long DC_OWNER_BAE_CHANGE = 358; //NBLXA-2152
	//FINANCIAL ACTIVITY CHANGES
	public final static  long DC_FIN_ACT_REVERSE = 400;

	//Coverage changes
	public final static long DC_RIDER_AMT = 500;
	public final static long DC_RIDER_ADDED = 501;
	public final static long DC_RIDER_DELETED = 502;
	public final static long DC_RIDER_CTIR_ADDED = 503;
	public final static long DC_RIDER_CTIR_DELETED = 504;
	public final static long DC_MODAL_AMT_CHG = 505;
	public final static long DC_RIDER_DPW_ADDED = 506; //ALS4153
	public final static long DC_BENEFIT_ADDED = 507; //APSL3360
	
	public final static long DC_RIDER_LTC_ADDED = 508; //APSL4697
	public final static long DC_RIDER_LTC_DELETED = 509; //APSL4697
	public final static long DC_RIDER_LTC_UPDATED = 510; //APSL4697
	public final static long DC_RIDER_ROPR_ADDED = 511; //APSL5128
	
	//Substandard Rating changes
	public final static long DC_PERMANENT_FLAT_EXTRA_RATING_ADDED = 600;
	public final static long DC_PERMANENT_FLAT_EXTRA_RATING_DELETED = 601;
	public final static long DC_TEMP_FLAT_EXTRA_RATING_ADDED = 602;
	public final static long DC_TEMP_FLAT_EXTRA_RATING_DELETED = 603;
	public final static long DC_PERMANENT_RATING_ADDED = 602;
	public final static long DC_PERMANENT_RATING_DELETED = 603;
	
	//Begin AXAL3.7.21
	public final static long DC_RELATION_DELETED = 700;
	public final static long DC_RELATION_ADDED = 701;
	public final static long DC_RELATION_CHANGED = 702;
	//End AXAL3.7.21
	
	//begin ALS2611 Changes on Impairment 
	public final static long DC_IMPAIRENT_PERM_AMT_UPDATED = 800;
	public final static long DC_IMPAIRENT_TEMP_AMT_UPDATED = 801;
	public final static long DC_IMPAIRENT_CREDIT_UPDATED = 802;
	public final static long DC_IMPAIRENT_DEBIT_UPDATED = 803;
	public final static long DC_IMPAIRENT_DURATION_UPDATED = 804;
	//End ALS2611
	
	//begin ALS4287 Changes on RequirementInfo 
	public final static long DC_POLDELRECEIPT_RECEIVED = 901;
	public final static long DC_SIGNILLUS_RECEIVED = 902;
	//End ALS4287
	
    //APSL550 begin
	public final static long DC_PREMQUOTE_RECEIVED=903;
	public final static long DC_PSTAPPREQ_ADDED=904;
	//APSL550 end
	public final static long DC_PREMQUOTE_WAIVED=905; //APSL1441
	//SystemMessage changes
	public final static long DC_SYSMSG_REST_RESOLVED = 1001; //P2AXAL007
	public final static  long DC_FACE_AMT_INCREASE = 1002;//APSL3360

	public final static  long DC_BENEFICIARY_CHANGE = 1100;//APSL3360
	public final static  long DC_CONTBENEFICIARY_CHANGE = 1101;//APSL3360
	public final static  long DC_BENEFICIARY_ADD = 1102;//APSL5128
	public final static  long DC_CONTBENEFICIARY_ADD = 1103;//APSL5128
	public final static  long DC_NEWOWNER_ADDED = 1104;//APSL5128
	public final static long DC_OTHER_BENEFIT_ADDED = 1105; //APSL5368 
	public final static long DC_1035EX_KIT_RECEIVED=906;//APSL4280
	public final static  long DC_CWA_AMT=999;//APSL4871;
	public final static  long DC_CV_1797_EXISTS=998;//APSL4967
	public final static  long DC_PRINT_TOGETHER_UPDATED=1000;// NBLXA-188(APSL5318) Legacy Decommissioning
	public final static  long DC_SEVERE_LICENSING_CV_EXIST=1003;
		//NBLXA-1254 Begins
	public final static  long DC_BENOWNER_NAME = 1201;
	public final static  long DC_BENOWNER_DOB = 1202;
	public final static  long DC_BENOWNER_ADDRESS = 1203;
	public final static  long DC_BENOWNER_SSN = 1204;
	public final static  long DC_BENOWNER_ADD = 1205;
	public final static  long DC_BENOWNER_CHANGE = 1206;
	
	public final static  long DC_TRUSTEEOWNER_NAME = 1301;
	public final static  long DC_TRUSTEEOWNER_DOB = 1302;
	public final static  long DC_TRUSTEEOWNER_ADDRESS = 1303;
	public final static  long DC_TRUSTEEOWNER_SSN = 1304;
	public final static  long DC_TRUSTEEOWNER_ADD = 1305;
	public final static  long DC_TRUSTEEOWNER_CHANGE = 1306;
	
	public final static  long DC_CONTPERSONOWNER_NAME = 1401;
	public final static  long DC_CONTPERSONOWNER_DOB = 1402;
	public final static  long DC_CONTPERSONOWNER_ADDRESS = 1403;
	public final static  long DC_CONTPERSONOWNER_SSN = 1404;
	public final static  long DC_CONTPERSONOWNER_ADD = 1405;
	public final static  long DC_CONTPERSONOWNER_CHANGE = 1406;
	
	public final static  long DC_AUTHPERSONOWNER_NAME = 1501;
	public final static  long DC_AUTHPERSONOWNER_DOB = 1502;
	public final static  long DC_AUTHPERSONOWNER_ADDRESS = 1503;
	public final static  long DC_AUTHPERSONOWNER_SSN = 1504;
	public final static  long DC_AUTHPERSONOWNER_ADD = 1505;
	public final static  long DC_AUTHPERSONOWNER_CHANGE = 1506;
	
	
	public final static  long DC_PAYOR_ADD = 1601;
	public final static  long DC_PAYOR_CHANGE = 1602;
	public final static  long DC_PAYOR_DOB = 1603;
	public final static  long DC_PAYOR_NAME = 1604;
	public final static  long DC_PAYOR_ADDRESS = 1605;
	public final static  long DC_PAYOR_SSN = 1606;
	//NBLXA-1254 Ends.

	//NBLXA-1254
	public final static  long DC_REQUESTED_POL_DATE_REASON_ADD = 1701;
	// Start NBLXA-1812
	public final static  long DC_DEPENDENT_DOB = 1650;
	public final static  long DC_DEPENDENT_GENDER = 1651;
	public final static  long DC_DEPENDENT_FIRSTNAME = 1652;
	public final static  long DC_DEPENDENT_LASTNAME = 1653;
	// End NBLXA-1812
	// NBLXA-1896 Begins
	public final static  long DC_CWA_Shortage_CV_EXIST = 1702;
	public final static  long DC_CWA_RECEIVED = 1703;
	// NBLXA-1896 Ends
	public final static  long DC_BUSINESS_STRATEGIES = 1704;//NBLXA-1823
	public final static  long DC_SEVERE_CV_RESOLVED=1004; //NBLXA-1954
	
	public final static  long DC_INITIAL_PREMIUM_METHOD=1005; //NBLXA-2108
	public final static  long DC_NCF_Miscmatch_CV_EXIST = 1705;//NBLXA-1850
	public final static  long DC_UWAPPRVL_SEVERE_CV_RESOLVED = 1706; //NBLXA-2398
	public final static  long DC_ONBOARDING_BRIDGER_ALERT_CV_RESOLVED = 1707; //NBLXA-2299
	public final static  long DC_ONBOARDING_BAE_ALERT_CV_RESOLVED = 1708; //NBLXA-2299
	
	public static final long DC_MAGNUM_DATASOURCE_RECIEVED = 2000; //NBLXA-2402 (NBLXA-2602) US#297686
	public final static  long DC_MULTIPLE_DRAFT_CV_EXIST = 1709; //NBLXA-2519
	public final static  long DC_PRINT_CV_RESOLVED=1006; //NBLXA-2620
	
}