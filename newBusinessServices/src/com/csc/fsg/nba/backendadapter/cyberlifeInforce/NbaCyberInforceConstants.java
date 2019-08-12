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

/**
 * Constants used for the Cyberlife values on the host.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA076</td><td>Version 3</td><td>Initial Development</td></tr>
 * <tr><td>SPR1696</td><td>Version 3</td><td>Add Variables for the endorsement segment</td></tr>
 * <tr><td>SPR1079</td><td>Version 3</td><td>Special Frequency changes</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change</td></tr>
 * <tr><td>NBA112</td><td>Version 4</td><td>Agent Name and Address</td></tr>
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 * <tr><td>SPR1950</td><td>Version 4</td><td>Issue to Admin Adapter Fields Not Mapped</td></tr>
 * <tr><td>NBA111</td><td>Version 4</td><td>Joint Insured</td></tr>
 * <tr><td>SPR1986</td><td>Version 4</td><td>Remove CLIF Adapter Defaults for Pay up and Term date of Coverages and Covoptions</td></tr>
 * <tr><td>NBA105</td><td>Version 4</td><td>Underwriting Risk.</td></tr>
 * <tr><td>SPR2115</td><td>Version 4</td><td>OLife values are not translated to BES values </td></tr>
 * <tr><td>SPR2582</td><td>Version 5</td><td>CLIF Some DXE Missing - 55 Seg</td></tr>
 * <tr><td>SPR1073</td><td>Version 5</td><td>WRAPPERED CLIF - DXE not being created for Credit Card Number, Name and Expiry Date</td></tr>
 * <tr><td>SPR2341</td><td>Version 5</td><td>Issues  with segment 04.</td></tr>
 * <tr><td>SPR1929</td><td>Version 6</td><td>Allocation (57) segment constructed incorrectly.</td></tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>General Code Cleanup</td></tr>
 * <tr><td>NBA132</td><td>Version 6</td><td>Equitable Distribution of Work</td></tr>
 * <tr><td>NBA143</td><td>version 6</td><td>Inherent benefits processing</td></tr>
 * <tr><td>SPR3148</td><td>Version 6</td><td>Guaranteed Premium Period End Date Not Sent in IS00 for COI Rate Guarantee Rule 2 Causes Abend in Offline (CKDCARTH) </td></tr>
 * <tr><td>SPR3191</td><td>Version 6</td><td>Required Fields Not Set for Guideline Calculations in Issue to Admin </td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up</td></tr>
 * <tr><td>NBA140</td><td>Version 7</td><td>Indeterminate Premium Product</td></tr>
 * <tr><td>SPR3433</td><td>Version 7</td><td>FUSFORCE Should Be Changed to Value of One for All of the U1 Series of Transactions</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public interface NbaCyberInforceConstants {

    //SPR2992 deleted code
	//Begin NBA112
	public static final String RESOLVE_WRIT_AGENT_INFO =
		"RESOLVE=PUGAWAIDPUGAWASCPUGAWALVPUGAWAGYPUGAWALNPUGAWAFNPUGAWAMIPUGAWANPPUGAWANSPUGAWDIVPUGAWBRAPUGAWTYPPUGAWADDPUGAWAA1PUGAWAA2PUGAWACTPUGAWASTPUGAWAZPPUGAWACNPUGAWAMDPUGAWAFXPUGAWABNPUGAWAHNPUGAWAMNPUGAWAEM;";
	public static final String RESOLVE_SERV_AGENT_INFO =
		"RESOLVE=PUGASAIDPUGASASCPUGASALVPUGASAGYPUGASALNPUGASAFNPUGASAMIPUGASANPPUGASANSPUGASDIVPUGASBRAPUGASTYPPUGASADDPUGASAA1PUGASAA2PUGASACTPUGASASTPUGASAZPPUGASACNPUGASAMDPUGASAFXPUGASABNPUGASAHNPUGASAMNPUGASAEM;";
		
	public static final String ADTYPE_HOME = "01"; 
	public static final String ADTYPE_BUS = "02";
	public static final String PHONETYPE_BUS = "B";
	public static final String PHONETYPE_BUSFAX = "F";
	public static final String PHONETYPE_HOM = "H";
	public static final String PHONETYPE_MOB = "M";
	public static final String PREFCOMM_EMAIL = "4";
	public static final String PREFCOMM_FAX= "3";
	//End NBA112 

	//Begin NBA105
	public static final String PARTY_INQUIRY_INFO =
		"RESOLVE=PUCALSECPUCALSEPPUCAXSECPUCAXSEPPUSETXNOPUSEAGTNPUSEBHDTPUSEBHSTPUSESXCDPUCRPNDIPUSERSEQPUSENAMEAEPCRUSRPUSEFLGBPUSENENTPUSEPPHSPUSEPUSRPUSEPPOLPUSEPSTSPUSEPSTDPUSEPRSNPUSEPPRC;";
	public static final String SEGMENT_SEQUENCE = "AEPCRSEQ";
	public static final String FLG_LSNM_IND = "PUCRLNMI";
	public static final String TYPE_IND = "PUCRNTYP";
	public static final String RESPONSE_TYPE = "PUCRPNDI";
	public static final String CLIENT_PLACE_HOLDER = "PUCALSEP";
	public static final String NUMBER_OF_POLICY_RECORDS = "PUCAXSEC";
	public static final String POLICY_PLACE_HOLDER = "PUCAXSEP";
	public static final String PARTY_CARRIER_CODE = "AEPCRUSR";
	public static final String PARTY_NAME = "PUCRNAME";
	public static final String PARTY_GOVT_ID = "PUCRTXNO";
	public static final String PARTY_BIRTH_DATE = "PUCRBTDT";
	public static final String PARTY_BIRTH_JUR = "PUCRBPLC";
	public static final String NO_OF_MATCHES = "PUCALSEC";
	public static final String POL_CARRIER_CODE = "PUSEPUSR";
	public static final String POL_NUMBER = "PUSEPPOL";
	public static final String POL_STATUS = "PUSEPSTS";
	public static final String PARTY_ROLE = "PUSEPPRC";
	public static final String PARTY_NAME_RETURNED = "PUSENAME";
	public static final String NO_OF_ENTRIES_RETURNED = "PUSENENT";
	public static final String POL_PENDING = "0";
	//end NBA105
	// begin NBA077
    public static final String HOLDING_INFORCE_RESOLVE =
        "RESOLVE=PUSPSTYPPUCPS02CPUCPS02PFPCGPEDT" ; //NBA077
	public static final String HOLDING_INFORCE_RESOLVE_COV =
        "FCVPROD,FCVIDENTFCVTCGDTFCVFACE,FCVUNITSFCVFQTYPFCVFSOPTFCVMAJLIFCVUOINDFCVUSEXCFCVUSUBCFCVPLFNOFCVPDSKYFCVCMCCDFCVNCTYPFCVPREMAFCVISSDFFCVAGETRFCVAGEUSFCVAGADMFCVPERCVFCVPAYDFFCVMATDFFCVUNVALFCVBINT,FCVBVAL,FCVLVLD,FCVFAPVRFCVGDURIFCVPHASEFCVPIDNTFCVLIVESFCVAGEISFCVFLGB0FCVFLGB5FCVFLGD0FCVAPREMFCVNFOPTFCVCVYR,FCVADJ,FCVRPU,FCVEI,FCVBEN,FCVPAR,FCVRATBKFCVSPAMTFCVRPOPTFCVRCORRFCVRNAARFCVNSPTBFCVNSPRPFCVNSPINFCVRDEXPFCVMECINFCVRPRINFCVINTRWFCVSRDURFCVSRPRDFCVIPGURFBRSTAT,FTDMATURFTDMATDUFCVBAND,FCVSBASEFCVBASE,FCVCLASS"; //NBA077 NBA104	           
	public static final String HOLDING_INFORCE_RESOLVE_BENEFIT =   
        "PUCPS04CPUCPS04PFSBBCDTEFSBBENEFFSBBISDFFSBBPHS,FSBBRAT,FSBPREMAFSBPIDNTFSBBUNT,FSBFLGB5FSBFLGA4FSBFLGA7FSBFLGB7FCVPSTYPFSBPRMCPFSBFLGA3FSBBAP,FSBPUDATPUSPI0RLPUSPI0PCPUSPI0NY"; //NBA077 NBA104 NBA143
	public static final String HOLDING_INFORCE_RESOLVE_RATING =
        "PUCPS03CPUCPS03PFELIDENTFELETDATFELECOM,FELEFFDFFELETYP,FELFLTEXFELEREASFELETABLFELPCTRTFELEAP,FELEPHS,FELPIDNTFELPREMAFELEDUSEFELFLGB0FELOCSDFFELSBTYPFELEMOYR";
	public static final String HOLDING_INFORCE_RESOLVE_ENDORSEMENTS =
        "PUCPS82CPUCPS82PFRDIDENTFRDPHASEFRDCAUTNFRDBENE,FRDPIDNTPUCPS82PFRDENDCDFRDERDATFRDBUSE,";
	public static final String HOLDING_INFORCE_RESOLVE_PEOPLE =
        "PUCPS89CPUCPS89PPUCPS90CPUCPS90PPUCPS95CPUCPS95PPUCPS96CPUCPS96PPUCSX96CPUCSX96PFLCIDENTFLCFNAMEFLCMINITFLCLNAMEFLCNMPFXFLCNMSFXFLCCORP,FLCTAXNOFLCMARSTFLCODESCFLCOCLASFLCTAXCDFLCLVERBFLCALIENFLCSGSEQFLCCOVERFLCBHDATFLCSEXCDFLCAGE,FLCBLHFTFLCBLHINFLCBLWGTFLCBHPLCFLCPHONEFLC2PHNTFLC2PHN,FLCPIDNTFMDAPXNWFMDANINCFRSIDRNOFRSIDRNOFBRSTATEFBRRCTRYFNAPIDNTFNAOCCURFNAEFFDTFNAADRTYFNACNTRYFNANMADDFNALINE1FNALINE2FNACITY,FNASTATEFNAZIPCDFECSGTYPFECUSECDFECPERSNFECPRSEQFECVARBLFBDCVPRSFBDCVSEQFBDCVSEQFBDCVPIDFBDNOENTFBDSEGIDFBDSGSEQFBDPRPIDFBDBNAMTFBDBDISTFBDBNPCTFBDPRSN,FBDPRSEQFBDBNRELFBDBTYPEFBRAGENTAEPKEYPLAEPKEYUSBASEPRODBASEPLANFBRISTATFBRREINSFBRBMODEFBRMPREMFBRFORM,FBRRESTAFBRRESTRPUNBENDRPUNBOTHIPUNBBENEPUNBRATSFBRAPPDTFLCW9INDFLCSTWHIFLCFEDEXFLCSTDEPFLCSTEX,FLCTINCCFBRAREA,FLCMEDIAFLCLVERB";//NBA077
	public static final String HOLDING_INFORCE_RESOLVE_ERR0R =
        "PUCPS83CPUCPS83PFUEIDENTFUESGSEQFUEPHASEFUENODATFUEERRCDFUESEVERFUEFDESCFSPTRCTL";
	public static final String HOLDING_INFORCE_RESOLVE_AGENT =    
        "PUCPS48CPUCPS48PPUCSX48CPUCSX48PFAGIDENTFAGORGBAFAGSITCDFAGNOLVLFAGPCTSHFAGPCTSVFAGAGTIDFAGAGTLVPUNBASGPPUNBADDPPUNBALTPPUNBAGTEFPRPIDNTPUNBREPP";
	public static final String HOLDING_INFORCE_RESOLVE_AUTO_TRAN =        
		"PUCPS53CPUCPS53PARUATFRQARUATTYPARUATCSDARUATSTDARUATDAYARUATCGAARUATSUDARUATCGOTRLFNDIDTRLALVALTRLALDOLTRLALPERDCUATFRQDCUATTYPDCUATCSDDCUATSTDDCUATDAYDCUATCGADCUATSUDDCUATCGOTALFNDIDTALALVALTALALDOLTALALPERAPUFREQ,APUTXNTPAPUCSDTEAPUSTDTEAPUOVRAMAPUMAXDPAPUMAXDAAPUYRINDAPUFNDALAPUSUSDTAPUOVMNBAPUSRCGOTWLFNDIDTWLALDOLTWLALPERTPLFNDIDTPLALVALTPLALDOLTPLALPERTRLALNUMTRLFRMTOTALALNUMTALFRMTOTWLALNUMTWLFRMTOTPLALNUMFAUWHINDFAUSWHNDFAUWHPCTFAUSWHPCFAUWHPAFFAUSWAGFFAUEXAMTFAUTXNTPFAUFREQ,FAUSTDTEFAUDSDAYFAUPAYPTFAUCSDTEFAUMAXDPFAUMAXDAFAUYRINDFAUFNDALFAUOVMNBFAUSRCGOFAUPAYEIFAUPOLNOFAUOVRAMFAUSUSDT";//NBA077
	public static final String HOLDING_INFORCE_RESOLVE_ADV_PROD =	
		"FULFLGC5FULIGIR,FULPLOPTFULASFRQFULCNFRQFULBILLCFULMTDATFULGPEDT";  //NBA077
	public static final String HOLDING_INFORCE_RESOLVE_SPC_FREQ_BILLING =	
		"FFCSFPDTFFCPYDFRFFCQPBASFFCQPAMTFFCINBDT"; 
	public static final String HOLDING_INFORCE_RESOLVE_ALLOCATION =	
		"PUCPS57CPUCPS57PFALTYPE,FALALNUMFALFRMTOFALFNDIDFALALUNIFALALDOLFALALPERFUSCOSTB"; //NBA077
	public static final String HOLDING_INFORCE_RESOLVE_REINSURANCE =	
		"PUCPS10CPUCPS10PFRECPHS,FRECRID,FRECRTY,FRECRUSEFRECRAMT";//NBA077
	public static final String HOLDING_INFORCE_RESOLVE_BANKING =	
		"PUCPS33CFBCOCCURFBCVRSN,FBCACTYPFBCCTYPEFBCCEXPRFBCBKACTFBCTRNSOFBCNAME1FBCNAME2FBCNAME3FBCBBNUMFBCEFFDTFBCTRNBRFBCMEDIA";//NBA077			 
	public static final String HOLDING_INFORCE_RESOLVE_MISC =   
		"FBRAGNCYPUNBAPRMFRCRCTYP1CVFACE,FBRPDTDTFBRBILTDFBRDUEDYFBRSTSKMFBRMDNSTFBRENTCDFBRPAYDTFBRBILDTFBRBILKDFBRISSCOFBRADMCOFBRLSTDFFBRDUEUSFFCQPAMTFFCQPBASFBROPT,FTDICRATFBRPROPDFBRPROSDFBRPENSNFBRHANDLFTDPINITPUNBPRMMPUNBPRMQPUNBPRMSPUNBPRMAPUNBWAFNPUNBWAMIPUNBWALNPUNBWAADPUNBWAL1PUNBWAL2PUNBWACTPUNBWASTPUNBWAZPPUNBAGADPUNBAGL1PUNBAGL2PUNBAGCTPUNBAGSTPUNBAGZPPUNBWAEMPUNBAGEMFILPUAOEPUCPS58CFFCLDEDTPUCPS72CFNPSOURCFNPSGSEQFNPNODATFNPNDEPTFNPVNOTEPUSPUBEXFBRSUSPDFFCECP,PUREMREAPUREREFAPUREMPADPUREINTDPUSPRCYRPUSPRCAMPUSPINAMPUSPASAMPUSPTXYRPUSPTXAM;";//NBA077
    public static final String HOLDING_INFORCE_SEGMENTS =
        "PUCPS02C=999;PUCPS02P=;AEPPOLOC=I;PUCPS04C=999;PUCPS04P=;PUCPS03C=999;PUCPS03P=;PUCPS82C=999;PUCPS82P=;PUCPS89C=999;PUCPS89P=;PUCPS90C=999;PUCPS90P=;PUCPS95C=999;PUCPS95P=;PUCPS80C=999;PUCPS80P=;PUCPS83C=999;PUCPS83P=;PUCPS96C=999;PUCPS96P=;PUCSX96C=999;PUCSX96P=;PUCPS40C=999;PUCPS40P=;PUCPS48C=999;PUCPS48P=;PUCSX48C=999;PUCSX48P=;PUCPS53C=999;PUCPS53P=;PUCPS57C=999;PUCPS57P=;PUCPS58C=999;PUCPS58P=;PUCPS10C=999;PUCPS10P=;PUCPS72C=999;PUCPS72P=;PUCPS33C=999;PUCPS33P=;"; //NBA077

    public static final String AGENT_VALIDATION_RESOLVE = "RESOLVE=PUAGVLEMPUAGVLAY;";  //NBA132

	//Inforce transactions
	public static final String INCREASE_RIDER_TRANS = "6PCYWHATTODO=26SR;";
	public static final String INCREASE_BENEFIT_TRANS = "6PCYWHATTODO=26SB;";
	public static final String INCREASE_AGENT_TRANS = "6PCYWHATTODO=26SA;";
	// NBA077 end
	
	// Inforce DXE
	// NBA076
	// begin SPR3290
	public static final String AV_FLAGB_1 = "FAVFLGB1"; // bit 3
	public static final String CW_FLAGB_3 = "FCWFLGB3";
	public static final String CW_FLAGB_6 = "FCWFLGB6";
	public static final String RC_FLAGA_7 = "FRCFLGA7";
	public static final String LC_FLAGA_2 = "FLCFLGA2";
	public static final String CLIENT_CITIZENSHIP = "FLCALIEN";
	public static final String COVERAGE_ID_2 = "FCLPIDNT";
	public static final String RC_FLAGA_5 = "FRCFLGA5";
	
	//Inforce basic fields (01 segment)
	public static final String INF_BASIC_FLAGE_0 = "FBRFLGE0";
	public static final String INF_BASIC_FLAGE_1 = "FBRFLGE1";
	public static final String INF_BASIC_FLAGE_2 = "FBRFLGE2"; 
	public static final String INF_BASIC_FLAGE_3 = "FBRFLGE3"; //SPR3191
	public static final String INF_BASIC_FLAGE_5 = "FBRFLGE5"; 
	public static final String INF_BASIC_FLAGE_7 = "FBRFLGE7"; //SPR3191
	public static final String INF_TAX_LAW_QUAL_TEST = "FULTFDF"; //SPR3191
	public static final String INF_FINANCIAL_CO = "FBRFINCO";
	public static final String INF_ASSIGNMENT_CODE = "FBRRESTA";
	public static final String INF_PAID_TO_DATE = "FBRPDTDT";
	// end SPR3290
	public static final String INF_SP_FREQ_SKIP_MONTH = "FBRSTSKM";
	public static final String INF_NEXT_MV = "FBRNMVDT";
	public static final String INF_ENTRY_CODE = "FBRENTCD";
	public static final String INF_REC_STATUS = "FBRRECST";
	public static final String INF_PREMIUM_STATUS = "FBRSTAT";
	public static final String INF_PREV_PREM_METH = "FBRHOWPD";
	public static final String INF_BASIC_FLAG_E = "FBRFLAGE";
	public static final String INF_EIL_IND = "FBREIL";
	public static final String INF_SEMI_ANN_MODE_FACTOR = "FBRMCALS";
	public static final String INF_QUARTERLY_MODE_FACTOR = "FBRMCALQ";
	public static final String INF_MONTHLY_MODE_FACTOR = "FBRMCALM";
	public static final String INF_MULTIPLY_ORDER_RULE = "FBRORDCD";
	public static final String INF_RATING_ORDER_RULE = "FBRRORCD";
	public static final String INF_ROUNDING_ORDER_RULE = "FBRROUND";
	public static final String INF_AUTO_PREM_LOAN = "FBRAPL";
	public static final String INF_APL_STOP = "FBRLIM";
	public static final String INF_LOAN_INTEREST_TYPE = "FBRLTYPE";
	public static final String INF_POLFEE_FACTOR_TBL_CODE = "FBRPFEEF";	
	public static final String INF_POLFEE_ADD_CODE = "FBRPFEEA";
	public static final String INF_POLFEE_COMM_IND = "FBRPFEEC";
	public static final String INF_POLFEE_USER = "FBRPFUSR";
	public static final String INF_POLFEE_AMT = "FBRFEE";
	public static final String INF_COLLECTION_FEE_ADD_RULE = "FBRCFEEA";
	public static final String INF_COLLECTION_FEE_COMM_IND = "FBRCFEEC";  //NBA104
	public static final String INF_COLLECTION_FEE_AMT = "FBRMCALC";
	public static final String INF_LOAN_INTEREST_RATE = "FBRLINTR";
	public static final String INF_DEATH_BEN_IND = "FBRDBIND";
	public static final String INF_COMM_CHARGE_BACK = "FBRCMCCD";
	public static final String INF_MODE_FACTOR_MODIFY = "FBRFACTM";  //NBA104
	public static final String INF_MODE_PREM_TABLE = "FBRMPCOD";  //SPR1950

	//Inforce coverage fields (02 segment)
	public static final String INF_COV_SEX = "FCVSEX";
	public static final String INF_COV_MAJOR_LINE = "FCVMAJLI";
	public static final String INF_COV_COMM_CODE = "FCVCMCCD";
	public static final String INF_COV_RATE_CLASS = "FCVRTCLS";
	public static final String INF_INCOME_OPTION = "1CVFSOPT";
	public static final String INF_COV_VAL_INTEREST_RATE = "FCVVINTR";
	public static final String INF_COV_CLASS = "FCVCLASS";
	public static final String INF_COV_SERIES = "FCVBASE";
	public static final String INF_COV_SUB_SERIES = "FCVSBASE";
	public static final String INF_COV_RATE_BOOK = "FCVRATBK";
	public static final String INF_COV_PAYUP_DATE = "FCVPAYDF";
	public static final String INF_COV_MAT_EXP_DATE = "FCVMATDF";
	public static final String INF_COV_AGE_ADMITTED = "FCVAGADM";
	public static final String INF_COV_PERSON_COVERED  = "FCVPERCV";
	public static final String INF_COV_ANN_PREM = "FCVAPREM";
	public static final String INF_COV_TRUE_AGE = "FCVAGETR";
	public static final String INF_COV_AGE_USE_CODE = "FCVAGEUS";
	public static final String INF_COV_MORT_TABLE = "FCVTABLE";
	public static final String INF_COV_MORT_FUNCTION = "FCVVFNCT";
	public static final String INF_COV_MODIFICATION_CODE = "FCVMOD";
	public static final String INF_COV_DEFICIENT_CODE = "FCVDEF";
	public static final String INF_COV_FLAG_A = "FCVFLAGA";
	public static final String INF_COV_FLAGB5_COV_STATUS = "FCVFLGB5";
	public static final String INF_COV_EI_MORTALITY_TABLE = "FCVNSPTB";
	public static final String INF_COV_ETI_DEATH_BENEFIT = "FCVADJ";
	public static final String INF_COV_ETI_INITIAL_UNADJ_DB = "FCVEI";
	public static final String INF_COV_ETI_BENEFIT = "FCVBEN";
	public static final String INF_COV_RPU_MORTALITY_TABLE = "FCVNSPRP";
	public static final String INF_COV_NONFORFEITURE_INT_RATE = "FCVNSPIN";
	public static final String INF_COV_RPU_BENEFIT = "FCVRPU";
	// NBA077 code deleted
	public static final String INF_COV_VALUE_PER_UNIT = "FCVUNVAL";
	public static final String INF_COV_RENEWAL_PREMIUM_TYPE = "FCVRPRIN";
	public static final String INF_COV_INITIAL_RENEWAL_PERIOD = "FCVINTRW";
	public static final String INF_COV_SUBSEQUENT_RENEWAL_ST_DUR = "FCVSRDUR";
	public static final String INF_COV_SUBSEQUENT_RENEWAL_PERIOD = "FCVSRPRD";
	public static final String INF_COV_IND_PREM_GUAR_PERIOD = "FCVIPGUR";
	public static final String INF_COV_BAND_CODE = "FCVBNDSC";
	public static final String INF_COV_BAND_CODE_RULE = "FCVRBAND";
	public static final String INF_COV_MEC_IND = "FCVMECIN";
	public static final String INF_COV_ANNUITY_PUCHASE_VAL_RULE = "FCVFAPVR";
	public static final String INF_COV_AP_RIDER_EXPENSE_RULE = "FCVRDEXP";
	public static final String INF_COV_DUR_GUAR_ISSUE = "FCVGDURI";
	public static final String INF_COV_CORRIDOR_RIDER_AMT = "FCVRCORR";
	public static final String INF_COV_DAETH_BENEFIT_OPTTYPE = "FCVRPOPT"; //NBA077
	public static final String INF_COV_JOINT_MORTALITY_TABLE = "FCVJTMTB"; //NBA111
	public static final String INF_COV_JOINT_TRUE_AGE = "FCVJTAGE"; //NBA111
	// begin NBA104
	public static final String INF_COV_BAND = "FCVBAND";
	public static final String INF_COV_CASH_VAL_YEAR = "FCVCVYR";
	public static final String INF_COV_RIDER_CORR_AMT = "FCVRCORR";
	public static final String INF_COV_REFRESH_AGE = "FCVREFAG";
	// end NBA104
		
	//04 segment
	public static final String INF_COV_OPT_PREM_CALC_PERC = "FSBPRMCP";
	public static final String INF_COV_OPT_ISSUE_AGE = "FSBBAGE";
	public static final String INF_COV_OPT_PAYUP_DATE = "FSBPUDAT";
	public static final String INF_COV_OPT_COMM_IND = "FSBBCOM";
	public static final String INF_COV_OPT_RESERVE_PLAN = "FSBBPLN";
	public static final String INF_COV_OPT_RENEW_RATE_IND = "FSBRENRT";
	public static final String INF_COV_OPT_VPU = "FSBBVPU";
	public static final String INF_COV_OPT_FORM_NO = "FSBFRMNO";
	//NBA111 code deleted
	public static final String INF_COV_OPT_SELECTRULE_IND = "FSBFLGB5"; //NBA143	

	
	//begin NBA077
	//Reinsurance (10 segment)	
	public static final String INF_REINS_COUNT = "PUCPS10C"; 
	public static final String INF_REINS_PHASE = "FRECPHS";
	public static final String INF_REINS_PCT = "FRECEXP";
	public static final String INF_REINS_ID = "FRECRID";
	public static final String INF_REINS_RISKBASIS = "FRECRTY";
	public static final String INF_REINS_EFF_DATE = "FREEFFDT";
	public static final String INF_REINS_PAID_UP_DATE = "FRECESDT";
	public static final String INF_REINS_AMT = "FRECRAMT";
	public static final String INF_REINS_USE = "FRECRUSE";
	public static final String INF_REINS_EXP_ALLOWANCE = "FRECEXAL";
	
	//Billing control (33 segment)
	public static final String INF_BILLING_COUNT = "PUCPS33C";
	//SPR1073 deleted code
	public static final String INF_BILLING_ACCOUNT_TYPE = "FBCACTYP";
	public static final String INF_BILLING_CC_TYPE = "FBCCTYPE";
	public static final String INF_BILLING_CC_EXPDATE = "FBCCEXPR";
	public static final String INF_BILLING_ACCOUNT_NUM = "FBCBKACT";
	public static final String INF_BILLING_ROUTING_NUM = "FBCTRNSO";
	public static final String INF_BILLING_ACT_HOLDER_NAME1 = "FBCNAME1";
	public static final String INF_BILLING_ACT_HOLDER_NAME2 = "FBCNAME2";
	public static final String INF_BILLING_ACT_HOLDER_NAME3 = "FBCNAME3";
	public static final String INF_BILLING_CONTROL_NUM = "FBCBBNUM";
	public static final String INF_BILLING_CONTROL_EFFDATE = "FBCEFFDT";
	public static final String INF_BILLING_BRANCH_NUM = "FBCTRNBR";
	public static final String INF_BILLING_ACH_IND = "FBCMEDIA";			
	//end NBA077
	 
	//Special Frequency Billing (35 segment)
	public static final String INF_SP_FREQ_PAID_TO_DATE = "FFCSFPDT";
	//deleted 1 line SPR1079
	
	//Traditional segment (51 segment)
	public static final String INF_TRAD_MATURITY_DATE = "FPCMTDAT";
	public static final String INF_TRAD_BUILD_DATE = "FPCBLDDT";		
	public static final String INF_TRAD_PROCESS_BACK_DATE = "FPCPBDAT";	
	public static final String INF_TRAD_LAST_ANNIVERSARY = "FPCLANNV";	
	public static final String INF_TRAD_PAID_TO_DATE = "FPCLPDDT";	
	
	public static final String INF_TRAD_LAST_MNTHLY_ANN = "FPCLMAP";	
	public static final String INF_TRAD_LAST_DIV_UPDT_DUR = "FPCLDVUP";	
	public static final String INF_TRAD_LATEST_HIST_DUR = "FPCHSDUR";	
	public static final String INF_TRAD_LOWEST_HIST_DUR = "FPCLHDUR";	
	public static final String INF_TRAD_NET_COST_ACC_PREM = "FPCNCACP";	
	public static final String INF_TRAD_NET_COST_ACC_DIV = "FPCNCACD";	
	public static final String INF_TRAD_NET_COST_CONTENT_IND = "FPCNCCNV";	
	public static final String INF_TRAD_DIR_RECOG_DATE = "FPCDRDTE";	
	
	//Automatic transaction (53 segment)
	public static final String INF_AUTO_TRANS_TYPE = "FAUTXNTP";
	public static final String INF_AUTO_TRANS_SEQ = "FAUATSEQ";
	public static final String INF_PAYOUT_NUM_PAYMENTS = "FAUCERTP";
	public static final String INF_PAYOUT_AMOUNT = "FAUAWDAM";
	public static final String INF_PAYOUT_PERCENT = "FAUAWDPC";
	public static final String INF_PAYOUT_MODE = "FAUFREQ"; 
	public static final String INF_AUTO_MODE = "FAUFREQ"; //NBA077
	public static final String INF_PAYOUT_START_DATE = "FAUSTDTE";
	public static final String INF_AUTO_START_DATE = "FAUSTDTE"; //NBA077
	public static final String INF_AUTO_START_DAY = "FAUDSDAY"; //NBA077
	public static final String INF_PAYOUT_FORM = "FAUPAYPT"; 
	public static final String INF_AUTO_FORM = "FAUPAYPT"; //NBA077
	public static final String INF_PAYOUT_EXCLUSION_RATIO = "FAUEXRAT";
	public static final String INF_PAYOUT_PRIMARY_REDUCTION_PERCENT = "FAUJSPC1";
	public static final String INF_PAYOUT_END_DATE = "FAUCSDTE";
	public static final String INF_AUTO_END_DATE = "FAUCSDTE"; //NBA077
	public static final String INF_PAYOUT_SEC_REDUCTION_PERCENT = "FAUJSPC2";
	public static final String INF_PAYOUT_ADJ_INVESTED_AMT = "FAUADJIV";
	public static final String INF_PAYOUT_ASSUMED_INTEREST_RATE = "FAUASMIR";
	public static final String INF_PAYOUT_EXCLUSION_AMT = "FAUEXAMT";
	public static final String INF_AUTO_TRANS_MAX_DISB_PCT = "FAUMAXDP";
	public static final String INF_AUTO_TRANS_MAX_DISB_AMT = "FAUMAXDA";
	public static final String INF_AUTO_TRANS_WD_BASIS = "FAUYRIND";
	public static final String INF_AUTO_TRANS_WD_ALLOC_RULE = "FAUFNDAL";
	public static final String INF_AUTO_TRANS_MINBALOVRD = "FAUOVMNB";
	public static final String INF_AUTO_TRANS_SURRGHROVRD = "FAUSRCGO";
	public static final String INF_AUTO_TRANS_PAYEE_INDV = "FAUPAYEI";
	public static final String INF_AUTO_TRANS_POLNUM = "FAUPOLNO";
	public static final String INF_AUTO_TRANS_OVRD_AMT = "FAUOVRAM";
	public static final String INF_AUTO_TRANS_SUSPEND_DATE = "FAUSUSDT";
	
	//tax withholding information
	public static final String INF_FED_TAX_WITHHOLDING_TYPE = "FAUWHIND";
	public static final String INF_FED_TAX_WITHHELD_PCT = "FAUWHPCT";
	public static final String INF_FED_TAX_WITHHELD_AMT = "FAUWHPAF";
	
	public static final String INF_STATE_TAX_WITHHOLDING_TYPE = "FAUSWHND";
	public static final String INF_STATE_TAX_WITHHELD_PCT = "FAUSWHPC";
	public static final String INF_STATE_TAX_WITHHELD_AMT = "FAUSWAGF";
	
	//Fund fields (55 segment)
	public static final String INF_FUND_ID = "FIFFNDID";
	public static final String INF_FUND_COV_PHASE = "FIFCOVPH";
	public static final String INF_FUND_TYPE = "FIFFNDTY";
	public static final String INF_FUND_QUALIFICATION = "FIFFNDQL";
	public static final String INF_FUND_MIN_BAL_TBL_CODE = "FIFMBTBL";
	public static final String INF_FUND_MIN_BAL_INIT_RULE = "FIFMBINL";
	public static final String INF_FUND_PURCHASE_RULE = "FIFPURCH";
	public static final String INF_FUND_INVEST_TYPE = "FIFFITYP";
	public static final String INF_FUND_INVEST_SUBTYPE = "FIFFISTP";
	public static final String INF_FUND_INTEREST_FREQ = "FIFFIFRQ";
	public static final String INF_FUND_INT_COMPOUND_RULE = "FIFFCMPD";
	public static final String INF_FUND_GUAR_INT_RATE = "FIFFGIRT";
	public static final String INF_FUND_GUAR_INT_RULE = "FIFFGRUL";
	public static final String INF_FUND_GUAR_INT_PERIOD = "FIFFGPRD";
	public static final String INF_FUND_ASSUMED_INT_RATE = "FIFVAIRT";
	public static final String INF_FUND_RATE_FILE_SRCH_KEY = "FIFFKEY";
	//begin SPR2582
	public static final String INF_FUND_ALLOW_TRANSFER_CODE = "FIFDTRNF";
	public static final String INF_FUND_ALLOW_WITHDRAW_CODE = "FIFDWDRW";
	public static final String INF_FUND_ALLOW_LOAN_CODE = "FIFDLOAN";
	public static final String INF_FUND_ALLOW_CHARGES_CODE = "FIFDCHRG";
	public static final String INF_FUND_ALLOW_LOAN_INT_CODE = "FIFDLINT";
	//end SPR2582
	
	//Multiple Fund fields (56 Segment)
	public static final String INF_MF_ALLOW_TRANSFER_CODE = "FMFTALOW";
	public static final String INF_MF_TRANSFER_CHARGE_TBL = "FMFTCTBL";
	public static final String INF_MF_PRIMARY_TRANS_RULE = "FMFTCRU1";
	public static final String INF_MF_SEC_TRANS_RULE = "FMFTCRU2";
	public static final String INF_MF_MIN_TRANS_AMT = "FMFTMAMT";
	public static final String INF_MF_TRANS_FREQ_RULE = "FMFTFRUL";
	public static final String INF_MF_TRANS_MIN_DAYS_INTERVALS = "FMFTFINT";
	public static final String INF_MF_MAX_NO_TRANS_PER_YEAR = "FMFTNMYR";
	public static final String INF_MF_MVA_DB_RULE = "FMFMVDBR";
	public static final String INF_MF_MVA_LOAN_RULE = "FMFMVLNR";
	public static final String INF_MF_MVA_ANNUITY_PURCHASE_RULE = "FMFMVAPR";
	public static final String INF_MF_MVA_FUND_MAT_RULE = "FMFMVFMB";
	public static final String INF_MF_MVA_DEFAULT_REINVEST_OPT = "FMFMVDRE";
	public static final String INF_MF_MVA_ADJ_CALC_CODE = "FMFMVACC";
	public static final String INF_MF_MVA_ADJ_CALC_RULE = "FMFMVACR";
	public static final String INF_MF_MVA_FULL_SURR_FREE_RULE = "FMFMVAFS";
	public static final String INF_MF_MVA_FULL_SURR_PCT = "FMFMVFSU";
	public static final String INF_MF_MVA_FULL_SURR_MONTHS = "FMFMVFSM";
	public static final String INF_MF_MVA_PART_WITHDRAWAL_FREE_RULE = "FMFMVAPW";
	public static final String INF_MF_MVA_PART_WITHDRAWAL_PCT = "FMFMVPWP";
	public static final String INF_MF_MVA_PART_WITHDRAWAL_MONTHS = "FMFMVPWM";
	public static final String INF_MF_MVA_NEG_LIMIT_RULE = "FMFMVNLR";
	public static final String INF_MF_MVA_NEG_LIMIT_PCT = "FMFMVNLP";
	public static final String INF_MF_MVA_POS_LIMIT_RULE = "FMFMVPRR";
	public static final String INF_MF_MVA_TRUE_UP = "FMFMVTUO";
	public static final String INF_MF_MVA_ALT_INDEX_KEY = "FMFMVAIK";
	public static final String INF_MF_ALLOC_MAX_CHANGES_PER_YR = "FMFANCYR";
	public static final String INF_MF_ALLOC_MIN_DAYS_INTERVAL = "FMFAFREQ";
	public static final String INF_MF_ALLOC_MIN_PCT = "FMFAMPCT";
	public static final String INF_MF_ALLOC_MIN_BAL_RULE = "FMFALMIN";
	public static final String INF_MF_ALLOC_MAX_NUM = "FMFANFUN";
	public static final String INF_MF_DISBURSEMENT_CODE = "FMFCDSBR";
	
	//Allocation fields (57 segment)
	public static final String INF_ALLOC_COUNT = "PUCPS57C";
	public static final String INF_ALLOC_FROM_TO_IND = "FALFRMTO";
	public static final String INF_ALLOC_FUND_EXCLUSION_IND = "FALEXCLD";
				
	//Inforce AP field (66 segment)
	public static final String INF_AP_FLAG_A = "FULFLAGA";
	public static final String INF_AP_FLAG_B = "FULFLAGB";
	public static final String INF_BILLING_OPTION = "FULBILLC";
	//SPR1986 code deleted
	public static final String INF_AP_PREM_CREDIT_INT = "FULPRCRD";
	public static final String INF_AP_PREM_GRACE_PERIOD ="FULPRGPR"; 
	public static final String INF_AP_MATURITY_DATE = "FULMTDAT";
	public static final String INF_AP_PRO_RATA_REFUND = "FULSFPRA";
	public static final String INF_AP_COMM_EXTRACT_RULE = "FULCRULE";
	public static final String INF_AP_FREEZE_PERIOD = "FULPRFRZ";
	public static final String INF_AP_GRACE_PERIOD = "FULPRGPR";
	public static final String INF_AP_REINSTATEMENT_RULE = "FULRRULE";
	public static final String INF_AP_SPC_MIN_AMT = "FULSAMIN";
	//decrease and increase info
	public static final String INF_AP_DECREASE_LIFO_FIFO = "FULDLIFI";
	public static final String INF_AP_DECREASE_RULE = "FULDRULE";
	public static final String INF_AP_INCREASE_LIFO_FIFO = "FULILIFI";
	public static final String INF_AP_INCREASE_RULE = "FULIRULE";
	//Cash val info
	public static final String INF_AP_CV_EXP_FREQ = "FULCVEFR";
	public static final String INF_AP_CV_EXP_BASIS = "FULCVEBA";
	public static final String INF_AP_CV_EXP_TBL = "FULCVETB";
	public static final String INF_AP_CV_EXP_RULE1 = "FULCVER1";
	public static final String INF_AP_CV_EXP_RULE2 = "FULCVER2";
	public static final String INF_AP_CV_EXP_RULE3 = "FULCVER3";
	//Premium load info
	public static final String INF_AP_PREM_LOAD_RULE1 = "FULPLRU1";
	public static final String INF_AP_PREM_LOAD_RULE2 = "FULPLRU2";
	public static final String INF_AP_PREM_LOAD_RULE3 = "FULPLRU3";
	public static final String INF_AP_PREM_LOAD_TBL = "FULPLTBL";
	public static final String INF_AP_PREM_TAX_IND = "FULPRMTX";
	//COI info
	public static final String INF_AP_COI_GUAR_RULE = "FULCGRUL";
	public static final String INF_AP_COI_PERIOD = "FULCGPER";
	public static final String INF_AP_COI_FREQ = "FULCFREQ";
	public static final String INF_AP_COI_CALC_RULE = "FULCCALC";
	public static final String INF_AP_COI_NAR_RULE ="FULCNARC";
	public static final String INF_AP_COI_END_DATE = "FULCGEND"; //SPR3148
	//grace info 
	public static final String INF_AP_GRACE_DAYS ="FULGRCDY";
	public static final String INF_AP_GRACE_CREDIT_CODE ="FULGRICD";
	public static final String INF_AP_GRACE_INT ="FULGRIRT";
	public static final String INF_AP_GRACE_RULE ="FULGRVRU";	//SPR2115	
	//partial and full surrender info
	public static final String INF_AP_PARTIAL_SURR_ALLOWED = "FULSPALW";
	public static final String INF_AP_PARTIAL_SURR_MIN = "FULSPMIN";
	public static final String INF_AP_PARTIAL_SURR_NUM = "FULSPNUM";
	public static final String INF_AP_PARTIAL_SURR_TABLE = "FULSPCTB";
	public static final String INF_AP_FULL_SURR_ALLOWED ="FULSFALW";
	public static final String INF_AP_FULL_SURR_PRORATA ="FULSFPRA";	
	public static final String INF_AP_FULL_SURR_TABLE = "FULSFCTB";	
	public static final String INF_AP_FULL_SURR_RULE = "FULFLRUL";
	public static final String INF_AP_GUAR_PERIOD_RULE = "FULGPRUL";
	public static final String INF_AP_GUAR_PERIOD_DAYS = "FULGPDYS";
	public static final String INF_AP_PARTIAL_SURR_RULE1 = "FULSPCR1";
	public static final String INF_AP_PARTIAL_SURR_RULE2 = "FULSPCR2";
	public static final String INF_AP_FULL_SURR_RULE1 = "FULSFCR1";
	public static final String INF_AP_FULL_SURR_RULE2 = "FULSFCR2";
	public static final String INF_AP_PART_SURR_MIN_BAL_CALC_RULE = "FULSPBRU";
	public static final String INF_AP_PART_SURR_MIN_MON_COI = "FULSPBMT";
	public static final String INF_AP_PART_SURR_MIN_FLAT_AMT = "FULSPBAM";
	//Loan info
	public static final String INF_AP_LOAN_MIN_DUR ="FULLMDUR";  
	public static final String INF_AP_LOAN_PREF_OPTION = "FULLPOPT";
	public static final String INF_AP_LOAN_PREF_AMT_PERC = "FULLPACP";
	public static final String INF_AP_LOAN_PREF_YR_AVAIL = "FULLPPYA";
	public static final String INF_AP_LOAN_PREF_CHARGE_RATE = "FULLPCDR";
	public static final String INF_AP_LOAN_PREF_CREDIT_ADJ_AMT = "FULLPCDA";
	public static final String INF_AP_LOAN_INT_DISB = "FULLINTD";
	public static final String INF_AP_LOAN_INT_CREDIT_CODE = "FULLINTC";
	public static final String INF_AP_LOAN_INT_RATE = "FULLINRT";
	public static final String INF_AP_LOAN_MIN_AMT = "FULLMAMT";
	public static final String INF_AP_PREF_INT_RATE = "FULLPCRT";
	public static final String INF_AP_LOAN_MIN_CALC_TABLE = "FULLMBTB";//NBA104
	public static final String INF_AP_LOAN_MIN_CALC_RULE = "FULLMBRU";//NBA104
	//CORRIDOR info
	public static final String INF_AP_CORR_RULE = "FULCRRUL";
	public static final String INF_AP_CORR_AMT = "FULCRAMT";
	public static final String INF_AP_CORR_PCT = "FULCRPCT";
	// begin NBA104
	public static final String INF_AP_MIN_CV_BAL_AMT = "FULCVMAM";
	public static final String INF_AP_MIN_CV_BAL_INT = "FULCVMIR";
	public static final String INF_AP_ADDL_PAY_1YR = "FULPANO";
	public static final String INF_AP_ADDL_PAY_DB_RULE = "FULPADB";
	public static final String INF_AP_ADDL_PAY_LOW = "FULPALMI";
	public static final String INF_AP_ADDL_PAY_HI = "FULPALMX";
	public static final String INF_AP_ADDL_PAY_RULE = "FULPALRU"; 
	// end NBA104
	// NBA104 code deleted
	
	//Inforce note pad fields (72 segment)
	
	public static final String INF_NOTE_PAD_COUNT = "PUCPS72C";
	public static final String INF_NOTE_PAD_SOURCE = "FNPSOURC";
	public static final String INF_NOTE_PAD_DEPT = "FNPNDEPT";
	public static final String INF_NOTE_PAD_SEQ = "FNPSGSEQ";
 	public static final String INF_NOTE_PAD_DATE = "FNPNODAT";
 	public static final String INF_NOTE_PAD_DATA = "FNPVNOTE";
   	//nba076 end 
   
   	//SPR1696
    //Endorsement fields (82 segment)
	public static final String INF_ENDORSE_END_DATE = "FRDERDAT";
   	public static final String INF_ENDORSE_TYPE = "FRDENDCD";
   	public static final String INF_ENDORSE_DETAIL_IND = "FRDCAUTN";
  	public static final String INF_ENDORSE_DATE = "FSPERDAT";
	public static final String INF_ENDORSE_PHASE = "FRDPHASE";
  	public static final String INF_ENDORSE_PERS_ID = "FRDPIDNT";
	public static final String INF_ENDORSE_IND = "FRDBUSE";
	public static final String INF_ENDORSE_BEN_TYPE = "FRDBTYPE";
	public static final String INF_ENDORSE_BEN_SUBTYPE = "FRDBSUBT";
    //end SPR1696
   
   //begin NBA077 - 203 holding constants different from 103 request
   
   //AAR
   public static final String INF_AAR_AUTO_TRANS_MODE = "ARUATFRQ";
   public static final String INF_AAR_AUTO_TRANS_TYPE = "ARUATTYP";
   public static final String INF_AAR_AUTO_TRANS_END_DATE = "ARUATCSD";
   public static final String INF_AAR_AUTO_TRANS_OVRD_AMT = "ARUATCGA";
   public static final String INF_AAR_AUTO_TRANS_SUSPEND_DATE = "ARUATSUD";
   public static final String INF_AAR_AUTO_TRANS_SURRGHROVRD = "ARUATCGO";
   public static final String INF_AAR_AUTO_TRANS_START_DATE = "ARUATSTD";
   public static final String INF_AAR_AUTO_TRANS_START_DAY = "ARUATDAY"; //NBA077
   
   public static final String INF_AAR_FUND_COUNT = "TRLALNUM";
   public static final String INF_AAR_FUND_ID = "TRLFNDID";
   public static final String INF_AAR_FUND_ALLOC_VAL = "TRLALDOL";
   public static final String INF_AAR_FUND_ALLOC_TYPE = "TRLALVAL";
   public static final String INF_AAR_FUND_ALLOC_PCT = "TRLALPER";
   public static final String INF_AAR_FUND_ALLOC_FROMTO = "TRLFRMTO"; //NBA077
   //DCA
   public static final String INF_DCA_AUTO_TRANS_MODE = "DCUATFRQ";
   public static final String INF_DCA_AUTO_TRANS_TYPE = "DCUATTYP";
   public static final String INF_DCA_AUTO_TRANS_END_DATE = "DCUATCSD";
   public static final String INF_DCA_AUTO_TRANS_OVRD_AMT = "DCUATCGA";
   public static final String INF_DCA_AUTO_TRANS_SUSPEND_DATE = "DCUATSUD";
   public static final String INF_DCA_AUTO_TRANS_SURRGHROVRD = "DCUATCGO";
   public static final String INF_DCA_AUTO_TRANS_START_DATE = "DCUATSTD";
   public static final String INF_DCA_AUTO_TRANS_START_DAY = "DCUATDAY"; //NBA077
   
   public static final String INF_DCA_FUND_COUNT = "TALALNUM";
   public static final String INF_DCA_FUND_ID = "TALFNDID";
   public static final String INF_DCA_FUND_ALLOC_VAL = "TALALDOL";
   public static final String INF_DCA_FUND_ALLOC_TYPE = "TALALVAL";
   public static final String INF_DCA_FUND_ALLOC_PCT = "TALALPER";
   public static final String INF_DCA_FUND_ALLOC_FROMTO = "TALFRMTO"; //NBA077

	public static final String INF_AWD_FUND_COUNT = "TWLALNUM"; //NBA077
	public static final String INF_AWD_FUND_ID = "TWLFNDID";
	public static final String INF_AWD_FUND_ALLOC_PCT = "TWLALPER";
	public static final String INF_AWD_FUND_ALLOC_TYPE = "TWLALVAL";
	public static final String INF_AWD_FUND_ALLOC_VAL = "TWLALDOL";
	public static final String INF_AWD_FUND_ALLOC_FROMTO = "TWLFRMTO"; //NBA077
	
   //Others
   public static final String INF_POLICY_STATEMENT_BASIS = "FULFLGC5";
   public static final String INF_POLICY_PAID_ADDT_ELEC = "FILPUAOE";
 	// NBA104 code deleted
	//NBA111 code deleted
   public static final String INF_COV_OPT_PERM_PER_UNIT = "FSBBAP";
   public static final String INF_FUND_SYSACT_TYPE = "FALTYPE";
   public static final String INF_FUND_ALLOC_TYPE = "FUSCOSTB"; 
   public static final String INF_SUB_ACCOUNT_COUNT = "FALALNUM";
   public static final String INF_TAX_REPORTING_COUNT = "PUCPS58C";
   public static final String INF_PARTY_KEY = "CS1CID";
   public static final String INF_PARTY_GOVTID_STAT = "FLCTINCC";
   public static final String INF_ORG_FORM = "FLCLVERB";
   public static final String INF_FED_DEPENDENTS = "FLCSTWHI";
   public static final String INF_FED_EXEMPTIONS = "FLCFEDEX";
   
   public static final String INF_POLICY_LOAN_IND = "PUSPUBEX";
   public static final String INF_POLICY_SUSPEND_IND = "FBRSUSPD";
   public static final String INF_POLICY_EXCESS_COLLECT_AMT = "FFCECP";
   public static final String INF_POLICY_REINSTATEMENT_AMT = "PUREMPAD"; 
   public static final String INF_POLICY_REINSTATEMENT_QUOTE = "PUREMREA";
   public static final String INF_POLICY_TRAD_GRACE_END_DATE = "FPCGPEDT";
   public static final String INF_POLICY_AP_GRACE_END_DATE = "FULGPEDT";  
   
   public static final String INF_COVERAGE_PHASE = "FDERPHS";
   public static final String INF_COVERAGE_COMM_PHASE = "FDERCPHS";   
   public static final String INF_COVERAGE_PRODUCT_CODE = "FDERSRCH";
   public static final String INF_COVERAGE_UNITS = "FDERUNIT";
   public static final String INF_COVERAGE_DEATH_BENEFIT_OPT = "FDERPOPT";
   public static final String INF_COVERAGE_EFF_DATE = "FDERISS";
   public static final String INF_COVERAGE_DEATH_UNISEX_OVERRIDE = "FDERUSOI";
   public static final String INF_COVERAGE_DEATH_UNISEX_CODE = "FDERUSCD";
   public static final String INF_COVERAGE_DEATH_UNISEX_SUBSERIES = "FDERUSSC";
   public static final String INF_COVERAGE_RATE_CLASS = "FDERCLAS";
   public static final String INF_COVERAGE_JOINT_RATE_CLASS = "FDERCLS2";
   public static final String INF_RIDER_PERSON = "FDERPRSI";
   public static final String INF_BENEFIT_PERSON = "FDEBPRSI";
   public static final String INF_USER_CODE = "FDEDPTDK";
   
   public static final String INF_COVOPTION_PHASE = "FDEBPHAS";
   public static final String INF_COVOPTION_PRODUCT_CODE = "FDEBTYSB";
   public static final String INF_COVOPTION_RATING_COMM_RULE = "FDEBCOMM";
   public static final String INF_COVOPTION_UNITS = "FDEBUNIT";
   public static final String INF_COVOPTION_EFF_YEAR = "FDEBISYR";
   public static final String INF_COVOPTION_EFF_MONTH = "FDEBISMO";
   public static final String INF_COVOPTION_TERM_DATE = "FDEBCDAT";
   public static final String INF_COVOPTION_PCT_LOADING = "FDEBRAT"; 
   
   public static final String INF_RATING_PHASE = "FDEECPH";   
   public static final String INF_RATING_REASON = "FDEEREAS";
   public static final String INF_RATING_COMM = "FDEECOMM";
   public static final String INF_RATING_TABLE = "FDEETABL";
   public static final String INF_RATING_TYPE = "FDETYPE";
   public static final String INF_RATING_TABLE_END_DATE = "FDEECDAT";
   public static final String INF_RATING_EFF_DATE = "FDEEEFDT";   
   
   public static final String INF_COMM_AGENT_COV_PHASE = "FDEAGPHS";
   public static final String INF_COMM_AGENT_SUB_PHASE = "FDEAGSP";  
   public static final String INF_COMM_AGENT_DATE = "FDEAGEDT";
   public static final String INF_COMM_AGENT_ROLECODE = "FDEAGLVL";   
   public static final String INF_COMM_AGENT_VOLUME_PCT = "FDEAGPS";
   public static final String INF_COMM_AGENT_SITCODE = "FDESITCD";
   public static final String INF_COMM_AGENT_ID = "FDEAGTID";   
   //end NBA077
    
   //Begin NBA112
   //writing agent's DXE variables
   //basic information
   public static final String WRIT_AGENT_ID = "PUGAWAID";
   public static final String WRIT_AGENT_AGENCY = "PUGAWBRA";
   public static final String WRIT_AGENT_STATUS = "PUGAWASC";   
   public static final String WRIT_AGENT_DIVISION = "PUGAWDIV";
   //Communication Information 
   public static final String WRIT_AGENT_PREF_COMM = "PUGAWAMD";   
   public static final String WRIT_AGENT_BUS_PHONE = "PUGAWABN";   
   public static final String WRIT_AGENT_HOM_PHONE = "PUGAWAHN";   
   public static final String WRIT_AGENT_MOB_PHONE = "PUGAWAMN";   
   public static final String WRIT_AGENT_FAX_PHONE = "PUGAWAFX";   
   public static final String WRIT_AGENT_EMAIL_ID = "PUGAWAEM";
   //Name  
   public static final String WRIT_AGENT_GENDER = "PUGAWSEX";   
   public static final String WRIT_AGENT_LEGAL_VERBIAGE = "PUGAWALV";   
   public static final String WRIT_AGENT_CORP_NAME = "PUGAWAGY";   
   public static final String WRIT_AGENT_LAST_NAME = "PUGAWALN";   
   public static final String WRIT_AGENT_FIRST_NAME = "PUGAWAFN";   
   public static final String WRIT_AGENT_MIDDLE_INITIAL = "PUGAWAMI";   
   public static final String WRIT_AGENT_PREFIX = "PUGAWANP";   
   public static final String WRIT_AGENT_SUFFIX = "PUGAWANS";
   //Address      
   public static final String WRIT_AGENT_ADDRESS_TYPE = "PUGAWTYP";   
   public static final String WRIT_AGENT_COUNTRY = "PUGAWACN";   
   public static final String WRIT_AGENT_ADDRESS_ADDENDUM = "PUGAWADD";   
   public static final String WRIT_AGENT_ADDRESS_LINE1 = "PUGAWAA1";   
   public static final String WRIT_AGENT_ADDRESS_LINE2 = "PUGAWAA2";
   public static final String WRIT_AGENT_CITY = "PUGAWACT";
   public static final String WRIT_AGENT_STATE = "PUGAWAST";
   public static final String WRIT_AGENT_ZIP = "PUGAWAZP";
   //Agency - Highest Level in Agent Hierarchy
   public static final String WRIT_AGENT_AGENCY_HL = "PUAGVLAY";  //NBA132
   //servicing agent's DXE variables
   //basic information
   public static final String SERV_AGENT_ID = "PUGASAID";
   public static final String SERV_AGENT_AGENCY = "PUGASBRA";
   public static final String SERV_AGENT_STATUS = "PUGASASC";   
   public static final String SERV_AGENT_DIVISION = "PUGASDIV";
   //Communication Information 
   public static final String SERV_AGENT_PREF_COMM = "PUGASAMD";   
   public static final String SERV_AGENT_BUS_PHONE = "PUGASABN";   
   public static final String SERV_AGENT_HOM_PHONE = "PUGASAHN";   
   public static final String SERV_AGENT_MOB_PHONE = "PUGASAMN";   
   public static final String SERV_AGENT_FAX_PHONE = "PUGASAFX";   
   public static final String SERV_AGENT_EMAIL_ID = "PUGASAEM";
   //Name  
   public static final String SERV_AGENT_GENDER = "PUGASSEX";   
   public static final String SERV_AGENT_LEGAL_VERBIAGE = "PUGASALV";   
   public static final String SERV_AGENT_CORP_NAME = "PUGASAGY";   
   public static final String SERV_AGENT_LAST_NAME = "PUGASALN";   
   public static final String SERV_AGENT_FIRST_NAME = "PUGASAFN";   
   public static final String SERV_AGENT_MIDDLE_INITIAL = "PUGASAMI";   
   public static final String SERV_AGENT_PREFIX = "PUGASANP";   
   public static final String SERV_AGENT_SUFFIX = "PUGASANS";
   //Address      
   public static final String SERV_AGENT_ADDRESS_TYPE = "PUGASTYP";   
   public static final String SERV_AGENT_COUNTRY = "PUGASACN";   
   public static final String SERV_AGENT_ADDRESS_ADDENDUM = "PUGASADD";   
   public static final String SERV_AGENT_ADDRESS_LINE1 = "PUGASAA1";   
   public static final String SERV_AGENT_ADDRESS_LINE2 = "PUGASAA2";
   public static final String SERV_AGENT_CITY = "PUGASACT";
   public static final String SERV_AGENT_STATE = "PUGASAST";
   public static final String SERV_AGENT_ZIP = "PUGASAZP";
   //End NBA112
   public static final String RENEWABLE_IND = "X";//SPR2341
   public static final String PERCENTAGE = "P";//SPR1929
   public static final String UNITS = "U";//SPR1929
   public static final String DOLLARS = "D";//SPR1929
   public static final String NON_INHERENT_COVOPTION_IND = "0"; //NBA143
   public static final String INHERENT_COVOPTION_IND = "1"; //NBA143
   public static final String INF_BASIC_FLAGD_4 = "FBRFLGD4"; //NBA140
   /*
    * Indicator to force the payment transaction on the host. DXE: FUSFORCE
    * Value of 1 - force the transaction
    * Value of 2 - do not force the transaction
    */
   public static final String FORCE_PAYMENT = "1";  //SPR3433 
   public static final String DO_NOT_FORCE_PAYMENT = "2"; //SPR3433 
}
