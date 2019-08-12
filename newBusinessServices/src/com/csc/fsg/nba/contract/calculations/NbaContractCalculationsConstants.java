package com.csc.fsg.nba.contract.calculations;
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
* NbaContractCalculationsConstants contains the constants used by Contract Calculations.
* <p>
* <b>Modifications:</b><br>
* <table border=0 cellspacing=5 cellpadding=5>
* <thead>
* <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
* </thead>
* <tr><td>NBA072</td><td>Version 3</td><td>Contract Calculations</td></tr>
* <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
* <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
* <tr><td>NBA142</td><td>Version 6</td><td>Minimum Initial Premium</td></tr>
* <tr><td>NBA133</td><td>Version 6</td><td>nbA CyberLife Interface for Calculations and Contract Print</td></tr>
* <tr><td>AXAL3.7.14</td><td>Version 6</td><td>AXA Contract Print</td></tr>
* <tr><td>P2AXAL016CV</td><td>AXA Life Phase 2</td><td>Product Val - Life 70 Calculations</td></tr>
* 
* </table>
* <p>
* @author CSC FSG Developer
 * @version 7.0.0
* @since New Business Accelerator - Version 3
*/
public interface NbaContractCalculationsConstants {
	// VPMS Models
	public final static java.lang.String CALCULATIONS_MODEL ="CALCULATIONS_CONTROL";
	public final static java.lang.String NO_MODEL ="NONE";	//NBA100
	// VPMS Attributes
	public final static java.lang.String ATR_SPECIFIED_ELEMENT = "A_Specified_Element";
	public final static java.lang.String ATR_SPECIFIED_ELEMENT_INDEX = "A_Specified_Element_Index";
	public final static java.lang.String ATR_SPECIFIED_PHASE = "A_Specified_Phase";
	public final static java.lang.String ATR_MODE_PREMIUM_CALC_OPTION = "A_Mode_Premium_Calc_Option";
	public final static java.lang.String ATR_PAYMENT_MODE = "A_Payment_Mode";	//NBA100	 
	public final static java.lang.String MODE_PREMIUM_CALC_OPTION_ENTIRE_POLICY = "0";
	public final static java.lang.String MODE_PREMIUM_CALC_OPTION_SPECIFIED_ELEMENT = "6";
	public final static java.lang.String SPEC_ELEMENT_RATING = "E";
	public final static java.lang.String SPEC_ELEMENT_COVERAGE = "C";
	public final static java.lang.String SPEC_ELEMENT_RIDER = "R";
	public final static java.lang.String SPEC_ELEMENT_BENEFIT = "B";
	public final static java.lang.String ATR_CALLING_SYSTEM = "A_calling_system";
	public final static java.lang.String CALLING_SYSTEM = "NBA";
	public final static java.lang.String ATR_CALLING_PROGRAM = "A_calling_program";  //NBA104
	public final static java.lang.String ATR_MODE_PREMIUM = "A_Mode_Premium";  //NBA104
	public final static java.lang.String ATR_CALC_FUNCTION_TYPE = "A_calc_function_type";
	public final static java.lang.String ATR_GCP_CALC_TYPE = "A_gcp_calc_type";   //NBA104
	public final static java.lang.String ATR_XML_RESPONSE = "A_XmlResponse";
	public final static java.lang.String TN_MODELNAME = "A_ModelNameTag";
	public final static java.lang.String TV_MODELNAME = "ModelName";
	public final static java.lang.String TN_TABLELOCATION = "A_TableLocationTag";
	public final static java.lang.String TV_TABLELOCATION = "TableLocation";
	public final static java.lang.String TN_ATTRIBUTE = "A_VpmsAttributeTag";
	public final static java.lang.String TV_ATTRIBUTE = "VpmsAttr";
	public final static java.lang.String TN_PROPERTY = "A_VpmsPropertyTag";
	public final static java.lang.String TV_PROPERTY = "VpmsProp";
	public final static java.lang.String TN_MODEL = "A_NameTag";
	public final static java.lang.String TV_MODEL = "Name";
	public final static java.lang.String TN_DATATYPE = "A_DataTypeTag";
	public final static java.lang.String TV_DATATYPE = "DataType";
	public final static java.lang.String TN_SOURCE = "A_SourceTag";
	public final static java.lang.String TV_SOURCE = "Source";
	public final static java.lang.String TN_TARGET = "A_TargetTag";
	public final static java.lang.String TV_TARGET = "Target";
	public final static java.lang.String TN_MODELRESULT = "A_VpmsModelResultTag";
	public final static java.lang.String TV_MODELRESULT = "VpmsModelResult";
	public final static java.lang.String TN_TRANSTABLE = "A_TransTableTag";
	public final static java.lang.String TV_TRANSTABLE = "TransTbl";
	public final static java.lang.String TN_TRANSVALUE = "A_TranslationValueTag";
	public final static java.lang.String TV_TRANSVALUE = "TranslationValue";
	public final static java.lang.String TN_XMLOBJECT = "A_XmlObjectTag";
	public final static java.lang.String TV_XMLOBJECT = "XmlObject";
	public final static java.lang.String TN_ATTR_NAME = "A_AttrNameTag";
	public final static java.lang.String TV_ATTR_NAME = "AttrName";
	public final static java.lang.String TN_ATTR_VALUE = "A_AttrValueTag";
	public final static java.lang.String TV_ATTR_VALUE = "AttrValue";
	public final static java.lang.String TN_STANDARD_ATTR = "A_StandardAttrTag";
	public final static java.lang.String TV_STANDARD_ATTR = "StandardAttr";
	// begin NBA100
	public final static java.lang.String TN_CACHESIZETAG = "A_CACHESIZETAG";
	public final static java.lang.String TV_CACHESIZETAG = "CacheSize";
	public final static java.lang.String TN_DEBUGINDTAG = "A_DEBUGINDTAG";
	public final static java.lang.String TV_DEBUGINDTAG = "DebugInd";
	public final static java.lang.String TN_KEY1 = "A_KEY1";
	public final static java.lang.String TN_KEY2 = "A_KEY2";
	public final static java.lang.String TN_KEY3 = "A_KEY3";
	public final static java.lang.String TN_MODEL_NAME = "A_MODELNAME";
	public final static java.lang.String TN_QUOTATIONMARK = "A_QUOTATIONMARK";
	public final static java.lang.String TN_REQCOMMENTTAG = "A_REQCOMMENTTAG";
	public final static java.lang.String TN_REQPROVIDERTAG = "A_REQPROVIDERTAG";
	public final static java.lang.String TN_REQTYPETAG = "A_REQTYPETAG";
	public final static java.lang.String TN_REQUIREMENTTAG = "A_REQUIREMENTTAG";
	public final static java.lang.String TN_SPACE = "A_SPACE";
	public final static java.lang.String TN_T = "A_T";
	public final static java.lang.String TN_TRANSLATIONTABLE = "A_TRANSLATIONTABLE";
	public final static java.lang.String TN_TRANSLATIONVALUE = "A_TRANSLATIONVALUE";
	public final static java.lang.String TN_XML_OBJECT = "A_XMLOBJECT";
	// end NBA100 
	public final static java.lang.String ATR_MODEL_NAME = "A_ModelName";
	public final static java.lang.String ATR_XML_OBJECT = "A_XmlObject";
	public final static java.lang.String ATR_TRANS_VALUE = "A_translationValue";
	public final static java.lang.String ATR_TRANS_TABLE = "A_translationTable";
	// begin NBA104
	public final static java.lang.String ATR_DEBUG = "$DEBUG";
	public final static java.lang.String ATR_TRACE = "$TRACE";
	public final static java.lang.String ATR_MINCACHESIZE = "$MINCACHESIZE";
	// VPMS Attribute Data Types
	public final static java.lang.String TYP_ASSUMPTION = "A";      // Assumption
	public final static java.lang.String TYP_POLICY = "P";          // Policy level 
	public final static java.lang.String TYP_COVERAGE = "C";        // Coverage level, non subscripted 
	public final static java.lang.String TYP_COVERAGE_S = "D";      // Coverage level, subscripted 
	public final static java.lang.String TYP_RIDER = "E";           // Rider level, non subscripted 
	public final static java.lang.String TYP_RIDER_S = "R";         // Rider level, subscripted 
	public final static java.lang.String TYP_BENEFIT = "N";         // Benefit level, non subscripted 
	public final static java.lang.String TYP_BENEFIT_S = "B";       // Benefit level, subscripted 
	public final static java.lang.String TYP_FUND = "F";            // Fund level, non subcripted 
	public final static java.lang.String TYP_FUND_S = "G";          // Fund level, subcripted 
	public final static java.lang.String TYP_PREMPU_S = "Q";        // Premium per unit change segment level, subscripted
	public final static java.lang.String TYP_VALUEPU_S = "U";       // Value per unit change segment level, subscripted
	public final static java.lang.String TYP_PREMPAY_S = "J";       // Premium payment level, subscripted
	public final static java.lang.String TYP_WTHDRWL_S = "K";       // Withdrawal level, subscripted
	public final static java.lang.String TYP_HISTORY = "H";         // History segment type "I" or "C"
	public final static java.lang.String TYP_MTHLY_PUA_S = "I";     // Monthly interst accumulation subscripted or PUA sgements subscripted
	public final static java.lang.String TYP_CURRINT = "O";         // Current interest rate file table indexed by fund and current interest rate table entry
	public final static java.lang.String TYP_POLYRVAL_S = "W";      // Policy year values segment subscripted
	public final static java.lang.String TYP_VALUEPHASE_S = "Z";    // Values phase level, subscripted 
	public final static java.lang.String TYP_CALENDARYR_S = "Y";    // Calendar year values segment subscripted
	public final static java.lang.String TYP_CURRINT_TIER_S = "T";  // Current interest rate tier level, subscripted
	public final static java.lang.String TYP_TARGET_S = "X";        // Target level, subscripted
	public final static java.lang.String TYP_LOAN_S = "S";          // Loan segment level, subscripted
	public final static java.lang.String TYP_PRIMARY_OR_JNT_EXTRA_S = "L"; // Primary or Joint Ins SubstandardRatings, subscripted  //NBA100
	// end NBA104
	// begin NBA100
	// VPMS Property Calculation Types	
	public final static java.lang.String PROP_POLICY = "P";      	// Policy Level
	public final static java.lang.String PROP_COV = "D";      		// Coverage Level
	public final static java.lang.String PROP_RDR = "R";      		// Rider Level
	public final static java.lang.String PROP_COVOPT = "B";			// CovOption Level  - NBA104
	public final static java.lang.String PROP_POLICY_DUR = "Q";     // Policy Level by duration
	public final static java.lang.String PROP_COV_DUR = "H";      	// Coverage Level by duration
	public final static java.lang.String PROP_RDR_DUR = "S";      	// Rider Level by duration
	public final static java.lang.String PROP_COVOPT_DUR = "X";   	// CovOption Level by duration
	public final static java.lang.String PROP_ALL = "-";            // All Levels  - NBA104
	// end NBA100
		// VPMS Entry Points
	public final static java.lang.String EP_GET_MODEL_KEYS = "P_GetVpmsModelKey";
	public final static java.lang.String EP_GET_MODEL_INFORMATION = "P_GetVpmsModelInformation";
	public final static java.lang.String EP_GET_MODEL_NAME = "P_Get_VPMS_model_name";
	public final static java.lang.String EP_GET_ATTRIBUTE_INFO = "P_Get_attribute_info_all";
	public final static java.lang.String EP_GET_PROPERTY_INFO = "P_Get_property_info_all";
	public final static java.lang.String EP_GET_TRANSLATION_VALUE = "P_Get_translation_value";
	public final static java.lang.String EP_GET_MODEL_SPECIFIC_ATTRIBUTES = "P_GetModelSpecificAttributes";	
	// begin NBA104
	public final static java.lang.String EP_DATE = "$DATE";
	public final static java.lang.String EP_VERSION = "$VERSION";
	public final static java.lang.String EP_COMPILER_VERSION = "compilerversion";
	public final static java.lang.String EP_VPM_DATE = "P_VPM_DATE";
	public final static java.lang.String EP_VPM_VERSION = "P_VPM_VERSION";
	public final static java.lang.String EP_PMS_VERSION_NUMBER = "P_PMSVERSIONNUMBER";
	// end NBA104
	// Calculation Types
	public final static java.lang.String CALC_TYPE_MODE_PREMIUM = "SMP";
	public final static java.lang.String CALC_TYPE_NON_STANDARD_MODE_PREMIUM = "NSM";
	public final static java.lang.String CALC_TYPE_ALL_STD_MODES_PREMIUM = "ASMP";	//NBA100
	public final static java.lang.String CALC_CONTRACT_DOCS = "CDOCS";	//NBA100
	public final static java.lang.String CALC_TYPE_SURRENDER_CHARGE = "VST";  //NBA104
	public final static java.lang.String CALC_TYPE_GUIDELINE_PREMIUM = "GDL";
	public final static java.lang.String CALC_TYPE_MINIMUM_INITIAL_PREMIUM = "MIP";
	// begin NBA104
	public final static java.lang.String CALC_TYPE_JOINT_EQUAL_AGE = "JEA";
	public final static java.lang.String CALC_TYPE_PREMIUM_LOAD = "VLT";
	public final static java.lang.String CALC_TYPE_COMMISSION_TARGET = "VCT";
	public final static java.lang.String CALC_TYPE_RATE_UTIL = "RATEU";
	public final static java.lang.String CALC_TYPE_7PAY_PREMIUM = "7PP";
	public final static java.lang.String CALC_TYPE_MIN_NO_LAPSE_PREMIUM = "MNL";
	public final static java.lang.String CALC_TYPE_CV_CALC = "CV"; //P2AXAL016CV
	public final static java.lang.String CALC_TYPE_PRINT_CALC = "PRINT"; //P2AXAL016CV
	// end NBA104
	//begin NBA133
	public final static String CALC_TYPE_LIFE_COVERAGE_PREMIUM = "LCP";
	public final static String CALC_TYPE_ANNUITY_TERM_RIDER_PREMIUM = "ATRP";
	public final static String CALC_TYPE_LIFE_COVERAGE_OPTION_PREMIUM = "LCOP";
	public final static String CALC_TYPE_ANNUITY_RIDER_COVERAGE_OPTION_PREMIUM = "ARCOP";
	public final static String CALC_TYPE_LIFE_COVERAGE_SUBRATING_PREMIUM = "LCSP";
	//end NBA133
	public final static java.lang.String CALC_AXA_CONTRACT_PRINT = "AXACP";	//AXAL3.7.14
	
	public final static java.lang.String DEFAULT_VALUE = "0";
	// begin NBA104
	// Debug/Trace Options
	public final static java.lang.String DBG_DEBUG = "D";
	public final static java.lang.String DBG_TRACE = "T";
	public final static java.lang.String DBG_NONE = "N";
	public final static java.lang.String DEBUG_ON = "2";
	public final static java.lang.String DEBUG_OFF = "0";
	public final static java.lang.String TRACE_ON = "1";
	public final static java.lang.String TRACE_OFF = "0";
	// XML Objects
	public final static java.lang.String XO_POLICY = "POLICY";
	public final static java.lang.String XO_COVERAGE = "COVERAGE";
	public final static java.lang.String XO_RIDER = "RIDER";
	public final static java.lang.String XO_COVOPTION = "COVOPTION";
	public final static java.lang.String XO_SUBSTANDARDRATING = "SUBSTANDARDRATING";
	// Specified Element Index for Ratings
	public final static java.lang.String SE_TABLE_RATING = "0";
	public final static java.lang.String SE_FIRST_FLAT = "1";
	public final static java.lang.String SE_SECOND_FLAT = "2";
	// end NBA104
	
	//Field names corresponding to VP/MS model properties
	public final static String P_MIP_TARGET = "MinInitPremium"; //NBA142
}
