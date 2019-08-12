package com.csc.fsg.nba.contract.validation;
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
/**
* NbaContractValidationConstants contains the constants used by Contract Validation.
* <p>
* <b>Modifications:</b><br>
* <table border=0 cellspacing=5 cellpadding=5>
* <thead>
* <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
* </thead>
* <tr><td>NBA064</td><td>Contract Validation</td><td>Initial Development</td></tr>
* <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
* <tr><td>NBA124</td><td>Version 5</td><td>Underwriting Risk Remap</td></tr>
* <tr><td>SPR2265</td><td>Version 6</td><td>Severe Errors In Insurance Contract Validation for Rider Product Information For Child Term Rider (CHILDTR) on Dependent On WL Plan</td></tr>
* <tr><td>AXAL3.7.40</td><td>AXA Life Phase 1</td><td>Contract Validation</td></tr>
* <tr><td>P2AXAL016</td><td>AXA Life Phase 2</td><td>Product Validation</td></tr>   
* <tr><td>NBA297</td><td>Version 8</td><td>Suitability</td></tr>
* <tr><td>P2AXAL021</td><td>AXA Life Phase 2</td><td>Suitability</td></tr> 
* <tr><td>A4_AXAL001</td><td>AXA Life NewApp</td><td>New Application – Application Entry A4</td></tr>
* <tr><td>CR1346709</td><td>Discretionary</td><td>Loan Carryover Indicator</td></tr>
* </table>
* <p>
* @author CSC FSG Developer
 * @version 7.0.0
* @since New Business Accelerator - Version 3
*/
public interface NbaContractValidationConstants {
	public static final Integer VALIDATION_PROCESS = new Integer(0);
	public static final int IMPL_NOT_DEFINED = 1;
	public static final int CONTRACT_INVALID = 2;
	public static final int FILTER_INVALID = 3;
	public static final int VALIDATION_PROCESSING = 4;
	public static final int INVALID_VPMS_CALC = 5;
	public static final int INVALID_CTL_ID = 6;
	public static final int INVALID_PROCESS_ID = 7;
	public static final int INVALID_PLAN_INFO = 8;
	public static final int INVALID_RATE_INFO = 9;
	public static final int SUBSET_UW_RISK = 90; //NBA124
	public static final int SUBSET_OLSA = 91; //ALII714
	public static final int SUBSET_TXNVAL = 900; //ALS3898
	// NBA104 deleted code
	public static final int INVALID_ERROR_MESSAGE = 999;
	public static final String ADDRESS = "ADDR";
	public static final String ANNUITY = "ANN";
	public static final String APPLICATIONINFO = "APPINFO";
	public static final String ARRANGEMENT = "ARR";
	public static final String ARRDESTINATION = "ARRDEST";
	public static final String ARRSOURCE = "ARRSRC";
	public static final String BANKING = "BANK";
	public static final String CARRIERAPPOINTMENT = "CARAPPT";
	public static final String COVERAGE = "COV";
	public static final String COVOPTION = "COVOPT";
	public static final String FINANCIALACTIVITY = "FINACT";
	public static final String HOLDING = "HOLD";
	public static final String INVESTMENT = "INV";
	public static final String LIFE = "LIFE";
	public static final String LIFEPARTICIPANT = "LIFEPART";
	public static final String ORGANIZATION = "ORG";
	public static final String PARTICIPANT = "PARTIC";
	public static final String PARTY = "PARTY";
	public static final String PAYOUT = "PAYOUT";
	public static final String PERSON = "PERS";
	public static final String POLICY = "POL";
	public static final String PRODUCER = "PROD";
	public static final String RIDER = "RDR";
	public static final String RELATION = "REL";
	public static final String REQUIREMENTINFO = "REQINFO";
	public static final String RISK = "RISK";
	public static final String SUBACCOUNT = "SUBACCT";
	public static final String SUBSTANDARDRATING = "SUBST";
	public static final String TAXWITHHOLDING = "TAXWH";
	public static final long BEST_MATCH = -1L; //SPR2265 
	public static final String SIGNATUREINFO="SIGNATUREINFO";//AXAL3.7.40	
	public static final String FORMINSTANCE="FRMINST";//AXAL3.7.40
	public static final String HHFAMILYINSURANCE="HHFAMILYINS";//AXAL3.7.40
	public static final String APPSIGNATUREINFO="APPSIGNATUREINFO";//AXAL3.7.40	
	public static final String BANKHOLDING="BANKHOLDING";//ALS3600
	public static final String FORMRESPONSE="FRMRESP";//P2AXAL004
	public static final String LIFEUSA = "LIFEUSA";//CR1346709
	
	//Begin NBA297
	public static final String EMPLOYMENT="EMP";
	public static final String FINANCIALEXPERIENCE="FINEXP";
	public static final String INTENT="INT";
	public static final String SYSTEMMESSAGE="SYSMSG";
	//End NBA297	
	public static final String CLIENT="CLIENT";	//P2AXAL021
	public static final String SUITABILITYDETAILSCC="SUIT";	//P2AXAL021
	public static final String TEMPINSAGREEMENTDETAILS="TAD"; //A4_AXAL001
	
	//Begin P2AXAL016 
	//Bit Position Constants from right
	public static int POS1 = 1;    
	public static int POS2 = 2;    
	public static int POS3 = 4;    
	public static int POS4 = 8;    
	public static int POS5 = 16;  
	public static int POS6 = 32;
	public static int POS7 = 64;  
	public static int POS8 = 128;
	//End P2AXAL016
}
