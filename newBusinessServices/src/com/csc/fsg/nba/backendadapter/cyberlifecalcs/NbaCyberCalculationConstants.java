package com.csc.fsg.nba.backendadapter.cyberlifecalcs;
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
 * Constants used for the Cyberlife calculation variables and values on the host.
 * <br>
 * table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA133</td><td>Version 6</td><td>nbA CyberLife Interface for Calculations and Contract Print</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
public interface NbaCyberCalculationConstants {
	public static final String RESOLVE_STD_MODES_PREMIUM = "RESOLVE=PUNMPREMPUNMAPRMPUNMSPRM;";
	public static final String RESOLVE_NON_STANDARD_MODE_PREMIUM = "RESOLVE=PUNMSPRMPUNMAPRM;";
	public static final String RESOLVE_COMMISSION_TARGET = "RESOLVE=PUNTCOMT;";
	public static final String RESOLVE_GUIDELINE_PREMIUM = "RESOLVE=PUNGANPMPUNGASPM;";
	public static final String RESOLVE_JOINT_EQUAL_AGE = "PUCPPNCC=999;PUCPPNCP=;RESOLVE=PUCPPNCCPUCPPNCPPUNCPHASPUNCPIDTPUNCISSA;";
	public static final String RESOLVE_MIN_NO_LAPSE_PREMIUM = "RESOLVE=PUNTMAPTPUNTMAPD;";
	public final static String RESOLVE_COVERAGE_PREMIUM = "PUCPS02C=99;PUCPS02P=;RESOLVE=PUCPS02CPUCPS02PFCVPHASEPUNCAPRM;";
	public final static String RESOLVE_COVERAGE_OPTION_PREMIUM = "PUCPS04C=99;PUCPS04P=;RESOLVE=PUCPS04CPUCPS04PFSBBENEFFSBBPHS,PUNSAPRM;";
	public final static String RESOLVE_SUBRATING_PREMIUM = "PUCPS03C=99;PUCPS03P=;RESOLVE=PUCPS03CPUCPS03PFELETYP,FELEPHS,FELPIDNTPUNEAPRM;";
	
	public static final String POL_LOC_CALCULATION = "AEPPOLOC=Z;";
	public static final String TRANSACTION_TYPE_HOLDING = "WHATTODO=DISP,2;";

	//Standard Mode premium
	public static final String MODE_PREMIUM_AMOUNT = "PUNMPREM";
	public static final String ANNUAL_MODE_PREMIUM = "PUNMAPRM";
	
	//Non-Standard Mode premium
	public static final String NON_STANDARD_MODE_PREMIUM = "PUNMSPRM";
	
	//Commission Target
	public static final String COMMISSION_TARGET = "PUNTCOMT";
	
	//Guideline premiums
	public static final String GUIDELINE_ANN_PREM = "PUNGANPM";
	public static final String GUIDELINE_SINGLE_PREM = "PUNGASPM";
	
	//Joint Equal Age
	public static final String COVERAGE_PHASE = "PUNCPHAS";
	public static final String PERSON_IDENT = "PUNCPIDT";
	public static final String ISSUE_AGE = "PUNCISSA";
	public static final String ISSUE_AGE_COUNT = "PUCPPNCC";
	

	//No Lapse Premium
	public static final String MIN_ANN_PREM_TARGET  = "PUNTMAPT";
	public static final String MAP_DATE  = "PUNTMAPD";
	
	//Coverage Premium
	public static final String COVERAGE_COUNT = "PUCPS02C";
	public static final String COVERAGE_PHASE_KEY = "FCVPHASE";
	public static final String COVERAGE_ANN_PREMIUM = "PUNCAPRM";
	
	//CovOption Premium
	public static final String COVOPTION_COUNT = "PUCPS04C";
    public static final String COVOPTION_PHASE = "FSBBPHS";
    public static final String COVOPTION_TYPE = "FSBBENEF";
    public static final String COVOPTION_ANN_PREMIUM = "PUNSAPRM";

    //SubstandardRating Premium
	public static final String SUB_STAND_COUNT = "PUCPS03C";
    public static final String SUB_STAND_PHASE = "FELEPHS";
    public static final String SUB_STAND_PERS_IDNT = "FELPIDNT";
    public static final String SUB_STAND_TYPE = "FELETYP";
    public static final String SUB_STAND_ANN_PREMIUM = "PUNEAPRM";
    
	
	//calculation fields
	public static final String[] MODE_PREMIUM_FIELDS = { MODE_PREMIUM_AMOUNT, //0
	        NON_STANDARD_MODE_PREMIUM, //1
	        ANNUAL_MODE_PREMIUM}; //2
	
	public static final String[] NON_STANDARD_MODE_PREMIUM_FIELDS = { NON_STANDARD_MODE_PREMIUM, //0
	        ANNUAL_MODE_PREMIUM}; //1

	public static final String[] COMMISSION_TARGET_FIELDS = { COMMISSION_TARGET }; //0

	public static final String[] GUIDELINE_PREMIUM_FIELDS = { GUIDELINE_ANN_PREM, //0
	        GUIDELINE_SINGLE_PREM}; //1
	
	public static final String[] JOINT_EQUAL_AGE_FIELDS = { COVERAGE_PHASE, //0
	        PERSON_IDENT, //1
	        ISSUE_AGE}; //2

	public static final String[] MIN_NO_LAPSE_PREMIUM_FIELDS = { MIN_ANN_PREM_TARGET, //0
	        MAP_DATE}; //1

	public static final String[] COVERAGE_PREMIUM_FIELDS = { COVERAGE_PHASE_KEY, //0
	        COVERAGE_ANN_PREMIUM}; //1

	public static final String[] COVOPTION_PREMIUM_FIELDS = { COVOPTION_PHASE, //0
	        COVOPTION_TYPE, //1
	        COVOPTION_ANN_PREMIUM}; //2
	
	public static final String[] SUB_STAND_PREMIUM_FIELDS = { SUB_STAND_PHASE, //0
	        SUB_STAND_PERS_IDNT, //1
	        SUB_STAND_TYPE,  //2
	        SUB_STAND_ANN_PREMIUM}; //3
}
