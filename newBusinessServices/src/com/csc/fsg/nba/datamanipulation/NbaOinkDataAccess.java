package com.csc.fsg.nba.datamanipulation; //NBA201

/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 *  are proprietary to CSC Financial Services Group®.  The use,<BR>
 *  reproduction, distribution or disclosure of this program, in whole or in<BR>
 *  part, without the express written permission of CSC Financial Services<BR>
 *  Group is prohibited.  This program is also an unpublished work protected<BR>
 *  under the copyright laws of the United States of America and other<BR>
 *  countries.  If this program becomes published, the following notice shall<BR>
 *  apply:
 *      Property of Computer Sciences Corporation.<BR>
 *      Confidential. Not for publication.<BR>
 *      Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.tableaccess.NbaAwdTranslationsData;
import com.csc.fsg.nba.tableaccess.NbaFundsData;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaPlansRidersData;
import com.csc.fsg.nba.tableaccess.NbaRequirementsData;
import com.csc.fsg.nba.tableaccess.NbaRolesData;
import com.csc.fsg.nba.tableaccess.NbaStatesData;
import com.csc.fsg.nba.tableaccess.NbaStatesUnisexData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.tableaccess.NbaUctData;
import com.csc.fsg.nba.tableaccess.NbaValidationTranslationsData;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fs.accel.valueobject.AccelProduct;

/**
 * The Object Interactive Name Keeper (OINK) Data Access allows information to be 
 * retrieved from or stored into NbaTXLife or NbaLob objects using pre-defined 
 * variable names.
 *
 * The source/destination objects are supplied to NbaOinkDataAccess either with 
 * constructer methods (Sources only) or as arguments to setXxxSource or setXxxDest 
 * methods. When an object is identified, the class responsible for accessing the 
 * values for the object is instantiated by NbaOinkDataAccess and the variable names 
 * and Method objects that may be used are added to a Map of available variable names
 * maintained by NbaOinkDataAccess.
 *
 * When NbaOinkDataAccess is invoked to access information, the variable name is parsed.
 * The value for the root variable is used to locate the corresponding entry in the Map.
 * Reflection is used to message the instance of the class responsible for accessing the 
 * values using the Method object in the Map.
 *
 * For information retrieval, an explicit formatter may be specified using the
 * setFormatter() method. A default formatter is used otherwise.
 *
 * NbaOinkDataAccess may be used to copy values from an NbaLob into a new NbaLob 
 * instance using the cloneNbaLobValues() method. 
 *  <p>
 *  <b>Modifications:</b><br>
 *  <table border=0 cellspacing=5 cellpadding=5>
 *  <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 *  </thead>
 *  <tr><td>NBA021</td><td>Version 2</td><td>Object Interactive Name Keeper</td></tr>
 *  <tr><td>NBA045</td><td>Version 3</td><td>Contract Copy</td></tr>
 *  <tr><td>NBA051</td><td>Version 3</td><td>Allow Search on Work Items</td></tr>
 *  <tr><td>NBA053</td><td>Version 3</td><td>Application Update Enhancement</td></tr>
 *  <tr><td>NBA082</td><td>Version 3</td><td>Change Base Plan</td></tr>
 *  <tr><td>SPR1336</td><td>Version 3</td><td>While resolving Risk Extension and Risk object,  error is encountered.</td></tr>
 *  <tr><td>SPR1799</td><td>Version 4</td><td>Debit and Credit account numbers are not assigned to AccountingActivity during validation. </td><tr>
 *  <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 *  <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 *  <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
 *  <tr><td>ACN015</td><td>Version 4</td><td>AC Database</td></tr>
 *  <tr><td>SPR2061</td><td>Version 4</td><td>NbaBaseException can occur in OINK processing.</td></tr>
 * <tr><td>SPR1753</td><td>Version 5</td><td>Automated Underwriting and Requirements Determination Should Detect Severe Errors for Both AC and Non - AC</td></tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>Code Cleanup</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3329</td><td>Version 7</td><td>Prevent erroneous "Retrieve variable name is invalid" messages from being generated by OINK</td></tr>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR3586</td><td>AXALife Phase 1</td><td>No Impairments are Generated by nbA AC Requirement Evaluation Lab Result Processing for Abnormal Test Results from 1122 XML</td></tr>
 *  <tr><td>AXAL3.7.07</td><td>AXA Life Phase 1</td><td>Automated Underwriting</td></tr>
 * <tr><td>AXAL3.7.06</td><td>AXA Life Phase 1</td><td>Requirement Determination</td></tr>
 * <tr><td>NBA237</td><td>Version 8</td><td>Migrate Policy Product Transmittal XML1201 to 2.15.00</td></tr>
 * <tr><td>P2AXAL016</td><td>AXA Life Phase 2</td><td>Product Validation</td></tr>
 *  <tr><td>NBA256</td><td>Version 8</td><td>WAS 7 and RAD 7.5 Upgradation</td></tr>
 * <tr><td>A4_AXAL001</td><td>AXA Life NewApp</td><td>New Application – Application Entry A4</td></tr>
 *  </table>
 *  <p>
 *  @author CSC FSG Developer
 * @version 7.0.0
 *  @since New Business Accelerator - Version 2
 * 
 */
public class NbaOinkDataAccess implements NbaContractDataAccessConstants {
	protected HashMap allRetrieveVariables = new HashMap();
	protected HashMap allUpdateVariables = new HashMap();
	protected com.csc.fsg.nba.vo.NbaTXLife nbaTXLife;
	protected HashMap excludedVariables;
	protected Map tblKeys = null;
	protected NbaTableAccessor nbaTableAccessor;
	private static NbaLogger logger = null;
	protected java.util.HashMap resolvers = new HashMap();
	protected NbaRetrieveContractData nbaRetrieveContractData;
	//NBA213 deleted code
	protected NbaRetrieveLOB nbaRetrieveLOB;
	protected NbaRetrieveAcdbData nbaRetrieveAcdbData;//ACN015
	protected NbaRetrievePlanData nbaRetrievePlanData;//P2AXAL016
	protected NbaUpdateLOB nbaUpdateLOB;
	protected NbaUpdateContract nbaUpdateContract;
	protected NbaUpdateAcdb nbaUpdateAcdb;//ACN015
	protected NbaOinkFormatter formatter;
	protected NbaOinkHTMLFormatter nbaOinkHTMLFormatter;
	protected NbaRetrieveDstVO nbaRetrieveDstVO;
	protected AccelProduct nbaProduct; //NBA237
	/**
	 * NbaDataAccess default constructor.
	 */
	public NbaOinkDataAccess() {
		super();
	}
	/**
	 * Set the NbaLob and Contract source from the NbaDst source
	 * @param newLobSource
	 */
	public NbaOinkDataAccess(NbaDst newDstSource) throws NbaBaseException {
		super();
		setDstSource(newDstSource);
	}
	/**
	 * Set the NbaLob source containing LOB information.
	 * @param newLobSource
	 */
	public NbaOinkDataAccess(NbaLob newLobSource) {
		super();
		setLobSource(newLobSource);
	}
	/**
	 * Store a NbaTxLife containing contract information.
	 * @param newContractSource 
	 */
	public NbaOinkDataAccess(NbaTXLife newContractSource) throws NbaBaseException {
		super();
		setContractSource(newContractSource);
	}
	/**
	 * Add new variable resolver to the Map containing the resolvers.
	 * @param clazzName - class name string
	 * @param clazzInstance - the instance of the variable resolver
	 */
	protected void addResolvers(String clazzName, Object clazzInstance) {
		resolvers.put(clazzName, clazzInstance);

	}
	/**
	 * Add new variable resolution routines to the Map containing the variable resolution routines.
	 * @param newResolvers - variable resolution routines
	 */
	protected void addResolvers(Map newResolvers) {
		getResolvers().putAll(newResolvers);
	}
	/**
	 * Add new variable resolution routines to the Map containing the variable resolution routines.
	 * @param newVariables - variable resolution routines
	 */
	protected void addRetrieveVariables(Map newVariables) {
		getAllRetrieveVariables().putAll(newVariables);
	}
	/**
	 * Add new variable resolution routines to the Map containing the variable update routines.
	 * @param newVariables - variable update routines
	 */
	protected void addUpdateVariables(Map newVariables) {
		getAllUpdateVariables().putAll(newVariables);
	}
	/**
	 * Answer an NbaLob containing selected values copied from the supplied NbaLob.
	 * @param aNbaLob - the supplied NbaLob
	 * @param anArrayList - the variables to be copied
	 * @return an NbaLob containing the values copied from the supplied NbaLob.
	 */
	public NbaLob cloneNbaLobValues(NbaLob aNbaLob, ArrayList anArrayList) {
		setLobSource(aNbaLob);
		setLobDest(new NbaLob());
		NbaOinkRequest request = new NbaOinkRequest();
		for (int i = 0; i < anArrayList.size(); i++) {
			request.setVariable((String) anArrayList.get(i));
			try {
				retrieveValue(request);
				updateValue(request);
			} catch (NbaBaseException e) {
				//Ignore variables that can't be resolved.
			}
		}
		return getNbaUpdateLOB().getNbaLob();
	}
	/**
	 * Resolve the value for the variable identified in the request. 
	 * Construct the JavaScript to wrapper the value.
	 * @param request - data request container
	 */
	public void generateJavaScript(NbaOinkRequest request) throws NbaBaseException {
		request.initFilters();
		retrieveValue(request);
		getNbaOinkHTMLFormatter().generateJavaScript(request);
	}
	/**
	 * Answer a Map containing all the retrieve variables in my vocabulary.
	 * @return allRetrieveVariables
	 */
	protected Map getAllRetrieveVariables() {
		return allRetrieveVariables;
	}
	/**
	 * Answer a Map containing all the update variables in my vocabulary.
	 * @return allUpdateVariables
	 */
	protected Map getAllUpdateVariables() {
		return allUpdateVariables;
	}
	/**
	 * Retrieve the Back End System value for an OLifE value.
	 * @param tableName  the name of the table from which to retrieve values
	 * @param olifeValue the value to be translated
	 * @return the plan name
	 * @exception NbaDataAccessException if errors occur in <code>translateOlifeData</code> method
	 */
	String getBesTranslation(String tableName, String olifeValue) throws NbaDataAccessException {
		NbaTableData aNbaTableData;
		if (tableName.equals(NbaTableConstants.NBA_ROLES)) {
			aNbaTableData = getNbaTableAccessor().getRolesTranslationData(getTblKeys(), "", olifeValue);
		} else {
			aNbaTableData = getNbaTableAccessor().getDataForOlifeValue(getTblKeys(), tableName, olifeValue);
		}
		if (aNbaTableData instanceof NbaUctData) {
			return ((NbaUctData) aNbaTableData).getBesValue();
		} else if (aNbaTableData instanceof NbaFundsData) {
			return ((NbaFundsData) aNbaTableData).getBesFundId();
		} else if (aNbaTableData instanceof NbaRequirementsData) {
			return ((NbaRequirementsData) aNbaTableData).getBesValue();
		} else if (aNbaTableData instanceof NbaRolesData) {
			return ((NbaRolesData) aNbaTableData).getBesValue();
		} else if (aNbaTableData instanceof NbaStatesData) {
			String bes = (String) getTblKeys().get(NbaTableAccessConstants.C_SYSTEM_ID);
			if (bes.equalsIgnoreCase(NbaConstants.SYST_CYBERLIFE)) {
				return ((NbaStatesData) aNbaTableData).getCybStateCode();
			} else if (bes.equalsIgnoreCase(NbaConstants.SYST_VANTAGE)) {
				return ((NbaStatesData) aNbaTableData).getVanStateCode();
			}
		}
		return olifeValue;
	}
	/**
	 * Answer a Map of the variable to be ignored
	 * @return java.util.HashMap
	 */
	protected Map getExcludedVariables() {
		if (excludedVariables == null) {
			excludedVariables = new HashMap();
			excludedVariables.put("PROCESSNAME", "");
			excludedVariables.put("TABLETYPE", "");
			excludedVariables.put("TABLENAME", "");
			excludedVariables.put("ORACLESEQUENCE", "");
			excludedVariables.put("ASSUMEDINTERESTRATE", "");
			excludedVariables.put("QUESTIONNBR", "");
			excludedVariables.put("SEQUENCENAME", "");	//SPR1799
			excludedVariables.put("MSGCODE", "");		//SPR1799
			excludedVariables.put("FIN_ACTIVITY_SUBTYPE", "");	//SPR1799
			excludedVariables.put("FIN_ACTIVITY_TYPE", "");		//SPR1799	
			excludedVariables.put("LINE_OF_BUSINESS", "");		//SPR1799		
			excludedVariables.put("DATANAME", ""); //NBA051
			excludedVariables.put("USAGE", ""); //NBA082
			excludedVariables.put("ERRORSEVERITY", ""); //NBA100
			excludedVariables.put("INTEGRATEDCLIENT", ""); //NBA100
			excludedVariables.put("LETTERTYPE", ""); //NBA100
			excludedVariables.put("NBAMONEY", ""); //NBA100
			excludedVariables.put("RESOURCES", ""); //NBA100
			excludedVariables.put("SOURCETYPELOB", ""); //NBA100
			excludedVariables.put("REQCODETAG", ""); //NBA100
			excludedVariables.put("REQUESTEDDATETAG", ""); //NBA100
			excludedVariables.put("REQUIREMENTDETAILSTAG", ""); //NBA100
			excludedVariables.put("REQUIREMENTINFOTAG", ""); //NBA100
			excludedVariables.put("REQVENDORLOBTAG", ""); //NBA100
			excludedVariables.put("BUSFUNC", ""); //NBA100
			excludedVariables.put("MODE", ""); //NBA100
			excludedVariables.put("COUNTATTACHMENTS", ""); //NBA100
			excludedVariables.put("COUNTEXTRACTTYPES", ""); //NBA100
			excludedVariables.put("AGERULE", ""); //NBA100
			excludedVariables.put("BANDAMOUNT", ""); //NBA100
			excludedVariables.put("ITERATIONCOUNT", ""); //SPR1753
			excludedVariables.put("BANDSTRUCTURECODE", ""); //NBA100
			//begin SPR3329
			// Variables from acbmieval
			excludedVariables.put("ADULTBMI", "");
			excludedVariables.put("DEBITS", "");
			excludedVariables.put("HIGHHEIGHT", "");
			excludedVariables.put("HIGHWEIGHT", "");
			excludedVariables.put("JUVENILEAGE", "");
			excludedVariables.put("LOWHEIGHT", "");
			excludedVariables.put("LOWWEIGHT", "");
			excludedVariables.put("MEDTHRES", "");
			excludedVariables.put("MESSAGESTAG", "");
			excludedVariables.put("MESSAGETEXTTAG", "");
			excludedVariables.put("MESSAGETAGCLOSE", "");
			excludedVariables.put("MESSAGETAGOPEN", "");
			excludedVariables.put("NOPARAMEDEXAMMATCH", "");
			excludedVariables.put("TEMPABOVEBMI", "");
			excludedVariables.put("TEMPABOVEBMIDEBITS", "");
			excludedVariables.put("TEMPABOVEBMIINDEX", "");
			excludedVariables.put("TEMPABOVEBMIVECTOR", "");
			excludedVariables.put("TEMPAGEMONTHS", "");
			excludedVariables.put("TEMPBELOWBMIDEBITS", "");
			excludedVariables.put("TEMPBELOWBMIVECTOR", "");
			excludedVariables.put("TEMPFT", "");
			excludedVariables.put("TEMPHTSTATUS", "");
			excludedVariables.put("TEMPHTWTFOUND", "");
			excludedVariables.put("TEMPIN", "");
			excludedVariables.put("TEMPVECTOR", "");
			excludedVariables.put("TEMPWTSTATUS", "");
			excludedVariables.put("UNDERWEIGHT","");
			excludedVariables.put("TEMPBELOWBMI","");
			//Variables from nba_xml_standard
			excludedVariables.put("ATTRNAMETAG", "");
			excludedVariables.put("ATTRVALUETAG", "");
			excludedVariables.put("CACHESIZETAG", "");
			excludedVariables.put("DATATYPETAG", "");
			excludedVariables.put("DEBUGINDTAG", "");
			excludedVariables.put("MODELNAMETAG", "");
			excludedVariables.put("NAMETAG", "");
			excludedVariables.put("QUOTATIONMARK", "");
			excludedVariables.put("REQCOMMENTTAG", "");
			excludedVariables.put("REQPROVIDERTAG", "");
			excludedVariables.put("REQTYPETAG", "");
			excludedVariables.put("REQUIREMENTTAG", "");
			excludedVariables.put("RESULT", "");
			excludedVariables.put("SOURCETAG", "");
			excludedVariables.put("SPACE", "");
			excludedVariables.put("STANDARDATTRTAG", "");
			excludedVariables.put("TABLELOCATIONTAG", "");
			excludedVariables.put("TARGETTAG", "");
			excludedVariables.put("TRANSLATIONVALUETAG", "");
			excludedVariables.put("TRANSTABLETAG", "");
			excludedVariables.put("VPMSATTRIBUTETAG", "");
			excludedVariables.put("VPMSMODELRESULTTAG", "");
			excludedVariables.put("VPMSPROPERTYTAG", "");
			excludedVariables.put("XMLOBJECTTAG", "");
			excludedVariables.put("XMLRESPONSE", "");
			//Variables from acheightweighteval
			excludedVariables.put("TEMPGENDER", "");
			excludedVariables.put("TEMPWEIGHTCHANGEIND", "");
			excludedVariables.put("TEMPVECTORHALFWTLOSS", "");
			excludedVariables.put("TEMPVECTORHEIGHT", "");
			excludedVariables.put("TEMPDEBITS", "");
			excludedVariables.put("DEBITS1", "");
			excludedVariables.put("FINALDEBITS", "");
			excludedVariables.put("WTSTATUS", "");
			excludedVariables.put("FINALWTSTATUS", "");
			excludedVariables.put("TEMPDEBITSHALFWTLOSS", "");
			excludedVariables.put("TEMPAVGWEIGHT", "");
			excludedVariables.put("TEMPHALFWEIGHTLOSS", "");
			excludedVariables.put("TEMPHEIGHTNOTFOUND", "");
			excludedVariables.put("TEMPWEIGHTNOTFOUND", "");
			excludedVariables.put("SKIPIMPAIRMENT", "");			
			excludedVariables.put("REQUIREMENTLIST", "");
			excludedVariables.put("TEMPADULTWTLOSSPEND", "");			
			//Variables from acprfcrieteria			
			excludedVariables.put("DESCRIPTION", "");
			excludedVariables.put("JUVNSMAGE", "");
			excludedVariables.put("FAMILYDIAGNOSIS", "");
			excludedVariables.put("KNOWNFAMILYHISTORYIND", "");
			//SPR3586 code deleted
			excludedVariables.put("NSCONVCD", "");
			excludedVariables.put("PRFCIGARETTECOUNT", "");
			excludedVariables.put("PRFDEATHCOUNT", "");			
			excludedVariables.put("PRFINCIDENTCOUNT", "");
			excludedVariables.put("PRFLEVEL", "");
			excludedVariables.put("PRFQUALLABVALUE", "");
			excludedVariables.put("PRFSCORE", "");
			excludedVariables.put("PRFTESTCD", "");
			excludedVariables.put("PRFTOBACCOPREMIUMBASIS", "");
			excludedVariables.put("PRFTYPE", "");			
			excludedVariables.put("PRFUWMAXPREFLEVEL", "");
			excludedVariables.put("PRFVALUE", ""); 
			excludedVariables.put("TOCONVCD", "");
			excludedVariables.put("DEBIT", "");
			excludedVariables.put("SUMTOBQUITMTHS", "");
			excludedVariables.put("AGGCONVCD","");
			excludedVariables.put("REQSUBSTATUS", "");
			excludedVariables.put("SUMPULSERESTIRRINDOUTPUT", "");			
			excludedVariables.put("DEBIT","");
			excludedVariables.put("DESCRIPTION","");			
			excludedVariables.put("PRODUCTCODE_BASE", "");
			excludedVariables.put("REQSUBSTATUS_INS","");
			excludedVariables.put("DEBIT_INS","");
			excludedVariables.put("BLANK","");
			excludedVariables.put("MEDUSAGEFLAG","");
			excludedVariables.put("IMPAIRMENPERMFLATEXTRAAMT","");
			excludedVariables.put("IMPAIRMENTEMPFLATEXTRAAMT", "");
			excludedVariables.put("IMPAIRMENTSTATUS","");
			//Variables from acfinancial
			excludedVariables.put("ABBREVIATED", "");
			excludedVariables.put("ADULT", "");
			excludedVariables.put("BUSINESSEARNINGS", "");
			excludedVariables.put("BUSINESSRATIO", "");
			excludedVariables.put("BUSINESSWORTH", "");
			excludedVariables.put("CHARITYPERCENT", "");
			excludedVariables.put("ELDER", "");			
			excludedVariables.put("INCOME", "");
			excludedVariables.put("JUVENILE", "");
			excludedVariables.put("PERCENTNETWORTH", "");
			excludedVariables.put("PERCENTRATIO", "");
			excludedVariables.put("PERCENTSPOUSEINSURANCE", "");			
			excludedVariables.put("PERFORMDETREVIEW", "");
			excludedVariables.put("PERFORMUNEMPPROCESSING", "");
			excludedVariables.put("PERSONALINSURANCERATIO","");
			excludedVariables.put("SINGLEPREMIUM", "");
			excludedVariables.put("SKIPABBRPROCESSING", "");
			excludedVariables.put("SKIPBENIFITPROCESSING", "");
			excludedVariables.put("SKIPCHARPROCESSING", "");
			excludedVariables.put("SKIPDETREVIEWPROCESSING", "");
			excludedVariables.put("SKIPEMPLPROCESSING", "");
			excludedVariables.put("SKIPFINANCIALPROCESSING", "");
			excludedVariables.put("SKIPJUVPROCESSING", "");
			excludedVariables.put("SKIPPERSONALRATIO", "");
			excludedVariables.put("SKIPPERSONALREVIEW","");         
			excludedVariables.put("SKIPUNEMPPROCESSING","");
			excludedVariables.put("TENFOLDEARNINGS","");
			excludedVariables.put("TOTALADB", "");
			excludedVariables.put("TOTALALLAPPLICANTINSURANCE", "");
			excludedVariables.put("TOTALALLCO", "");
			excludedVariables.put("TOTALAPPLINCOME", "");
			excludedVariables.put("TOTALGIR", "");
			excludedVariables.put("TOTALWP", "");
			excludedVariables.put("UNEARNEDINCOMEPERCENT", "");
			excludedVariables.put("UNEARNEDINCOMEPERCENT", "");
			excludedVariables.put("YOUNGADULT", "");
			//Variables from acprofile
			excludedVariables.put("TEMPFATHERAGE", "");
			excludedVariables.put("TEMPHEARTDISEASECOUNT", "");
			excludedVariables.put("TEMPMOTHERAGE", "");
			excludedVariables.put("TOTALWP", "");
			excludedVariables.put("UNEARNEDINCOMEPERCENT", "");
			excludedVariables.put("UNEARNEDINCOMEPERCENT", "");
			excludedVariables.put("YOUNGADULT", "");
			//Variables from acaviationevaluation
			excludedVariables.put("EVALHRCNP","");
			excludedVariables.put("EVALUATIONCOMPLETE","");
			excludedVariables.put("HAZARDFLAG", "");
			excludedVariables.put("MILITARYFLYING", "");
			excludedVariables.put("NEGLIGENTDRIVER", "");
			excludedVariables.put("PILOTCERTIFIED", "");
			excludedVariables.put("TABLE2USE","");
			excludedVariables.put("TEMPADVERSECOUNTER","");
			excludedVariables.put("TEMPFLYINGTIME","");
			excludedVariables.put("TEMPIFRHOURS", "");
			excludedVariables.put("TEMPSOLOHOURS", "");
			//Variables from acmedicalhistory  
			excludedVariables.put("CRITICALPROVIDERINDEX","");
			excludedVariables.put("DELIMITER1","");
			excludedVariables.put("DELIMITER2", "");
			excludedVariables.put("DELIMITER3", "");
			excludedVariables.put("DESC", "");
			excludedVariables.put("MEDACTIVITYSINCEAPP", "");
			excludedVariables.put("MEDSCREENINDEX","");
			excludedVariables.put("MEDSCREENKEY","");
			excludedVariables.put("PRIORTESTFLAG","");
			excludedVariables.put("SMKHISTMISSING", "");
			excludedVariables.put("STATUS", "");
			excludedVariables.put("STATUS1", "");
			excludedVariables.put("TEMPCONDITIONMONTHS", "");
			excludedVariables.put("TEMPCRITICALPROVIDER", "");
			excludedVariables.put("TEMPELAPSEDTIME", "");
			excludedVariables.put("TRIVIALCOUNT", "");
			excludedVariables.put("TYPEFROMDESC","");
			excludedVariables.put("MEDSCREENKEY","");
			excludedVariables.put("PRIORTESTFLAG","");
			//Variables from accontractsummary
			excludedVariables.put("TEMPSUMCIGARETTEDATE", "");
			excludedVariables.put("TEMPSUMCIGARETTEHABITTXT", "");
			excludedVariables.put("TEMPSUMTOBACCODATE", "");
			excludedVariables.put("TEMPSUMTOBACCOHABITTXT", "");
			// AXAL3.7.56 - removed TOBACCOPREMIUMBASIS from exclude list
			excludedVariables.put("OVERRIDEREASON","");
			excludedVariables.put("SUMINFORCETOTALGIR","");
			//Variables from acfamilyhistory
			excludedVariables.put("OTHERIMPAIRMENTGENERATED", "");
			excludedVariables.put("FAMILYHISTORYSIZE", "");
			excludedVariables.put("FAMILYHISTORYTOTALSIZE", "");
			excludedVariables.put("SPECIFICIMPAIRMENTGENERATED", "");
			//Variables from acbuysell
			excludedVariables.put("FINTHRES","");
			excludedVariables.put("OUTSTCOUNT","");
			excludedVariables.put("TEMPBUSYRS", "");
			excludedVariables.put("TEMPINCMULTAMT", "");
			excludedVariables.put("TEMPINCOMEMULT", "");
			excludedVariables.put("TEMPKPEARNINGS", "");
			excludedVariables.put("TEMPKPVALUE","");
			excludedVariables.put("TEMPPOINTS","");
			excludedVariables.put("TEMPPOINTSEVALINCOME","");
			excludedVariables.put("TEMPPOINTSEVALINSCONTRIB", "");
			excludedVariables.put("TEMPMGMTEARNINGS", "");
			excludedVariables.put("NUMYRSKPEARNINGS", "");
			excludedVariables.put("NETWORTHCONDITION", "");
			excludedVariables.put("LIMITKPVALUE", "");
			excludedVariables.put("INITKPVALUE", "");
			excludedVariables.put("TEMPPERCENTCONTRIB", "");
			excludedVariables.put("REQUIREMENTLISTSTATUS","");
			excludedVariables.put("TEMPBUSVALUE","");
			//Variables from ackeyperson
			excludedVariables.put("TEMPVARIANCE","");
			excludedVariables.put("TEMPVARIANCECHECK","");
			excludedVariables.put("TEMPVARIANCERESET","");
			excludedVariables.put("TEMPPERCENTOFTOTAL","");
			excludedVariables.put("TEMPINSVALUERESET","");
			excludedVariables.put("TEMPINSVALUE","");
			//Variables from acbeneownerrel
			excludedVariables.put("TEMPAGEGROUP","");
			excludedVariables.put("TEMPRELATCD","");
			excludedVariables.put("TEMPRELTABLEOK","");
			excludedVariables.put("TEMPTOTALAMTALLCO","");
			//Variables from autoprocessstatus
			excludedVariables.put("MISCMATCHTYPE","");
			excludedVariables.put("NBACONTRACTPRINT","");
			excludedVariables.put("OPENCASE","");
			excludedVariables.put("SOURCETYPECOUNT","");
			excludedVariables.put("NBACONTRACTPRINT","");
			//Variables from requirements
			excludedVariables.put("INSTALLATION","");
			excludedVariables.put("RESULTID","");
			excludedVariables.put("RESULTIDCOUNT","");			
			excludedVariables.put("COVERAGEID","");
			excludedVariables.put("COVOPTIONID","");
			excludedVariables.put("COVOPTIONOVERRIDERATINGREASON", "");
			excludedVariables.put("LIFECOVOPTTYPECOVOPTION", "");
			excludedVariables.put("LIFEPARTICIPANTID", "");
			excludedVariables.put("REQRESTRICTION", "");
			excludedVariables.put("REQREVIEW","");
			excludedVariables.put("REQMEDICALTYPE","");
			excludedVariables.put("UNDERWRITERWORKBENCHAPPLET","");
			excludedVariables.put("OVERRIDERATINGREASON","");
			//Variables from acrequirementsdetermination
			excludedVariables.put("ADDITIONALINDOTHER","");
			excludedVariables.put("ALTERNATEINDOTHER","");
			excludedVariables.put("CARRIERCODEOTHER", "");
			excludedVariables.put("FACEAMTOTHER", "");
			excludedVariables.put("POLICYNUMBEROTHER", "");
			excludedVariables.put("POLICYSTATUSOTHER", "");
			excludedVariables.put("POLICYSTATUS","");
			excludedVariables.put("PRODUCTTYPEOTHER","");
			excludedVariables.put("APSMATCHFOUND","");
			excludedVariables.put("CURRENTREQCOUNT","");
			excludedVariables.put("CURRENTREQLIST", "");
			excludedVariables.put("IMPCOUNT", "");
			excludedVariables.put("MODULENAME", "");
			excludedVariables.put("REQFROMDATABASE", "");
			excludedVariables.put("REQFROMDATABASELENGTH","");
			excludedVariables.put("SKIP","");
			excludedVariables.put("STATECODEEXISTSAGEAMT","");
			excludedVariables.put("STATECODEEXISTSHIV", "");
			excludedVariables.put("STATECODEEXISTSINSPECTION", "");
			excludedVariables.put("STATECODEEXISTSMVR", "");
			excludedVariables.put("WEIGHTIND", "");
			excludedVariables.put("ACSUMMARYORDEFAULTVALUESFIELDS", "");
			excludedVariables.put("NBAPENDINGCONTRACTFIELDS", "");
			//AUTOCONTRACTNUMBERING 
			excludedVariables.put("ENVIRONMENT","");
			excludedVariables.put("SEEDNUMBER","");
			excludedVariables.put("SEQUENCE", "");
			excludedVariables.put("INTEGRATEDCLIENTSYSTEM", "");
			//Variables from acprfquestionaaire			
			excludedVariables.put("AGCODE", "");
			excludedVariables.put("BMI", "");
			excludedVariables.put("BMIKEY", "");
			excludedVariables.put("MCACCESSKEY", "");
			excludedVariables.put("NSCODE", "");
			excludedVariables.put("NTCODE", "");
			excludedVariables.put("SMCODE", "");
			excludedVariables.put("TOCODE", "");			
			excludedVariables.put("IMPAIRMENTPERMFLATEXTRAAMT", "");
			excludedVariables.put("IMPAIRMENTTEMPFLATEXTRAAMT", "");
			excludedVariables.put("CREDIT", "");
			//Variables from CALCULATIONS_CONTROL
			excludedVariables.put("NEGBALANCEINTRATE","");
			excludedVariables.put("NUMBEROFFUNDS","");
			//Variables from ACFOREIGNTRAVEL 
			excludedVariables.put("FOREIGNTRAVELINDEX","");
			excludedVariables.put("COUNT1","");
			excludedVariables.put("UFSQKEY","");
			excludedVariables.put("IMPAIRMENTDURATION","");
			excludedVariables.put("PROCESSID","");
			excludedVariables.put("NUMBEROFCONTRACTS","");
			excludedVariables.put("RATING","");
			excludedVariables.put("CHECKAMOUNT","");
			//end SPR3329

		}
		return excludedVariables;
	}
	/**
	 * Get the value of the NbaOinkFormatter object.
	 * @return com.csc.fsg.nba.datamanipulation.NbaOinkFormatter
	 */
	public NbaOinkFormatter getFormatter() {
		if (formatter == null) {
			setFormatter(new NbaOinkDefaultFormatter());
		}
		return formatter;
	}
	/**
	 * Return the generated JavaScript
	 * @return String
	 */
	public String getJavaScript(String sourceId) throws NbaBaseException {
		//NBA045 Line deleted
		//NBA045 begin
		if (sourceId == null) {
			return getNbaOinkHTMLFormatter().getNbaHTMLHelper().getData();
		} else {
			return getNbaOinkHTMLFormatter().getJavaScript(sourceId);
		}
		//NBA045 end
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return the logger implementation
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaOinkDataAccess.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaOinkDataAccess could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * Get the value of the NbaOinkFormatter object.
	 * @return com.csc.fsg.nba.datamanipulation.NbaOinkFormatter
	 */
	protected NbaOinkHTMLFormatter getNbaOinkHTMLFormatter() {
		if (nbaOinkHTMLFormatter == null) {
			setNbaOinkHTMLFormatter(new NbaOinkHTMLFormatter(this));
		}
		return nbaOinkHTMLFormatter;
	}
	/**
	 * Get the value of the NbaRetrieveContractData object.
	 * @return com.csc.fsg.nba.datamanipulation.NbaRetrieveContractData
	 */
	protected NbaRetrieveContractData getNbaRetrieveContractData() {
		return nbaRetrieveContractData;
	}
	/**
	 * Get the value of the NbaRetrieveDstVO object.
	 * @return com.csc.fsg.nba.datamanipulation.NbaRetrieveDstVO
	 */
	public NbaRetrieveDstVO getNbaRetrieveDstVO() {
		return nbaRetrieveDstVO;
	}
	/**
	 * Get the value of the NbaRetrieveLOB object.
	 * @return com.csc.fsg.nba.datamanipulation.NbaRetrieveLOB
	 */
	protected NbaRetrieveLOB getNbaRetrieveLOB() {
		return nbaRetrieveLOB;
	}
	/**
	 * Get the value of the NbaTableAccessor object.
	 * @return NbaTableAccessor
	 */
	protected NbaTableAccessor getNbaTableAccessor() {
		if (nbaTableAccessor == null) {
			setNbaTableAccessor(new NbaTableAccessor());
		}
		return nbaTableAccessor;
	}
	/**
	 * Retrieve data from the database for a specified tablename.
	 * @param tableName  the name of the table from which to retrieve values
	 * @return NbaTableData[] containing the retrieved data objects
	 * @exception NbaDataAccessException if errors occur in <code><code>NbaTable.getDisplayData</code>.
	 */
	public NbaTableData[] getNbaTableData(String tableName) throws NbaDataAccessException {
		return getNbaTableAccessor().getDisplayData(getTblKeys(), tableName);
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (10/21/2002 7:48:23 PM)
	 * @return com.csc.fsg.nba.vo.NbaTXLife
	 */
	public com.csc.fsg.nba.vo.NbaTXLife getNbaTXLife() {
		return nbaTXLife;
	}
	/**
	 * Get the value of the NbaUpdateContract object.
	 * @return com.csc.fsg.nba.datamanipulation.NbaUpdateContract
	 */
	protected NbaUpdateContract getNbaUpdateContract() {
		return nbaUpdateContract;
	}
	/**
	 * Get the value of the NbaUpdateLOB object.
	 * @return com.csc.fsg.nba.datamanipulation.NbaUpdateLOB
	 */
	protected NbaUpdateLOB getNbaUpdateLOB() {
		return nbaUpdateLOB;
	}
	/**
	 * Retrieve the plan name for the supplied coverage key
	 * @param aCoverageKey  the coverage key
	 * @return the plan name
	 * @exception NbaDataAccessException if errors occur in <code><code>NbaTable.getDisplayData</code>.
	 */
	String getPlanRiderKeyTranslation(String aCoverageKey) throws NbaDataAccessException {
		NbaPlansRidersData aNbaPlansRidersData = getNbaTableAccessor().getPlanRiderTranslationData(getTblKeys(), aCoverageKey);
		return aNbaPlansRidersData.getPlanRiderKeyTranslation();
	}
	/**
	 * Answer a Map containing the variable resolution routines.
	 * @return resolvers
	 */
	protected java.util.HashMap getResolvers() {
		return resolvers;
	}
	/**
	 * Answer a String containing the value for the variable identified in the request.
	 * @param request - data request container
	 */
	public String getStringValueFor(NbaOinkRequest request) throws NbaBaseException {
		retrieveValue(request);
		return getFormatter().getStringValueFor(request);
	}
	/**
	 * Answer a String [] containing the value for the variable identified in the request.
	 * @param request - data request container
	 */
	public String[] getStringValuesFor(NbaOinkRequest request) throws NbaBaseException {
		retrieveValue(request);
		return getFormatter().getStringValuesFor(request);
	}
	/**
	 * Retrieve the translated value for an OLifE value.
	 * @param tableName  the name of the table from which to retrieve values
	 * @param olifeValue the value to be translated
	 * @return the plan name
	 * @exception NbaDataAccessException if errors occur in <code>translateOlifeData</code> method
	 */
	String getTableTranslation(String tableName, String olifeValue) throws NbaDataAccessException {
		NbaTableData aNbaTableData;
		if (tableName.equals(NbaTableConstants.NBA_ROLES)) {
			aNbaTableData = getNbaTableAccessor().getRolesTranslationData(getTblKeys(), "", olifeValue);
		} else {
			aNbaTableData = getNbaTableAccessor().getDataForOlifeValue(getTblKeys(), tableName, olifeValue);
		}
		if (aNbaTableData instanceof NbaUctData) {
			return ((NbaUctData) aNbaTableData).getIndexTranslation();
		} else if (aNbaTableData instanceof NbaAwdTranslationsData) {
			return ((NbaAwdTranslationsData) aNbaTableData).getTranslation();
		} else if (aNbaTableData instanceof NbaFundsData) {
			return ((NbaFundsData) aNbaTableData).getFundIdTranslation();
		} else if (aNbaTableData instanceof NbaPlansData) {
			return ((NbaPlansData) aNbaTableData).getCovKeyTranslation();
		} else if (aNbaTableData instanceof NbaRequirementsData) {
			return ((NbaRequirementsData) aNbaTableData).getIndexTranslation();
		} else if (aNbaTableData instanceof NbaRolesData) {
			return ((NbaRolesData) aNbaTableData).getIndexTranslation();
		} else if (aNbaTableData instanceof NbaStatesData) {
			return ((NbaStatesData) aNbaTableData).getStateCodeTrans();
		} else if (aNbaTableData instanceof NbaStatesUnisexData) {
			return ((NbaStatesUnisexData) aNbaTableData).getUnisexTranslation();
		} else if (aNbaTableData instanceof NbaValidationTranslationsData) {
			return ((NbaValidationTranslationsData) aNbaTableData).getIndexTranslation();
		}
		return olifeValue;
	}
	/**
	 * Answer a Map containing the key values to be used to retrieve table data.
	 * @return java.util.Map
	 */
	protected java.util.Map getTblKeys() {
		if (tblKeys == null) {
			tblKeys = new HashMap();
			try {
				ArrayList values = new ArrayList(NbaConfiguration.getInstance().getDatabaseSearchKeys().values()); //ACN012
				NbaOinkRequest aNbaOinkRequest = new NbaOinkRequest();
				if (getNbaRetrieveLOB() == null) {
					NbaLob aNbalob = new NbaLob();
					aNbalob.updateLobFromNbaTxLife(getNbaTXLife());
					aNbalob.setBackendSystem(getNbaTXLife().getBackendSystem());
					setLobSource(aNbalob);
				}
				for (int i = 0; i < values.size(); i++) {
					String aVariable = ((String) values.get(i));
					if (!isExcludedVariable(aVariable)) {
						//begin SPR1799
						try {
							aNbaOinkRequest.setVariable(aVariable.toUpperCase() + "LOB");
							retrieveValue(aNbaOinkRequest);
							String aValue;
							if (aNbaOinkRequest.getValue().size() > 0 && aNbaOinkRequest.getValue().elementAt(0) != null) {
								aValue = ((String) aNbaOinkRequest.getValue().elementAt(0)).trim();
							} else {
								aValue = NbaTableAccessor.WILDCARD;
							}
							tblKeys.put(aVariable, aValue);
						} catch (Exception e) {
							getLogger().logError(aVariable + " is not an LOB field and has been ignored.");
						//end SPR1799							
						}
						//SPR1799 code deleted
					}
				}
				return tblKeys;
			} catch (Exception e) {
				getLogger().logDebug("Unable to load table keys for NbaTableAccessor " + e.toString());
			}
		}
		return tblKeys;
	}
	/**
	 * Return true if the variable to be ignored
	 * @return boolean
	 */
	protected boolean isExcludedVariable(String aVariable) {
	   		return getExcludedVariables().containsKey(aVariable.toUpperCase());
	}
	/**
	 * Retrieve the value for the variable identified in the request.
	 * @param request - data request container
	 */
	protected void retrieveValue(NbaOinkRequest request) throws NbaBaseException {
		request.setValue(new Vector());
		request.setValueType(-1); //SPR1336
		if (!request.parseVariable()) {
			getLogger().logDebug("Unable to parse variable name:" + request.getVariable());
			return;  //NBA104
		}
		if (request.getCount() == 0) { // Set default.
			request.setCount(1);
		}
		if (request.isBesTranslate()) {
			setBesTranslationValue(request);
		}
		String thisVariable = request.getRootVariable();
		if (!isExcludedVariable(thisVariable)) {
			//Handle Medical, Risk and RIX data groups
			if (request.getDataGroup().equals("MED")) {
				thisVariable = "MEDCONDTYPE";
			} else if (request.getDataGroup().equals("RIS")) {
				thisVariable = "RISKQUESTIONS";
			} else if (request.getDataGroup().equals("RIX")) {
				thisVariable = "RISKEXTENSIONQUESTIONS";
			}
			//Begin AXAL3.7.07
			  else if (request.getDataGroup().equals("AIX")) {
				thisVariable = "APPLICATIONINFOEXTENSIONQUESTIONS";
			} else if (request.getDataGroup().equals("CLX")) {
				thisVariable = "CLIENTEXTENSIONQUESTIONS";
			} else if (request.getDataGroup().equals("MEX")) {
				thisVariable = "MEDICALEXAMEXTENSIONQUESTIONS";
			} else if (request.getDataGroup().equals("PAX")) {
				thisVariable = "PARTYEXTENSIONQUESTIONS";
			} else if (request.getDataGroup().equals("PEX")) {
				thisVariable = "PERSONEXTENSIONQUESTIONS";
			} else if (request.getDataGroup().equals("INX")) {
				thisVariable = "INTENTEXTENSIONQUESTIONS";
			} else if (request.getDataGroup().equals("EMP")) {
				thisVariable = "EMPLOYMENTQUESTIONS";
			} else if (request.getDataGroup().equals("TIA")) {
				thisVariable = "TEMPINSAGREEMENTINFOQUESTIONS";
			}
			//End AXAL3.7.07
			//Begin 3.7.06
			else if (request.getDataGroup().equals("POX")){
				thisVariable = "POLICYEXTENSIONQUESTIONS";
			} else if (request.getDataGroup().equals("TAD")) { 	//Begin A4_AXAl001
				thisVariable = "TEMPINSAGREEMENTDETAILSQUESTIONS";
			} else if (request.getDataGroup().equals("EIX")) {	//P2AXAL066
				thisVariable = "EMPLOYMENTEXTENSIONQUESTIONS";
			}
			//End AXAL3.7.06
			
			//Set filter for Requirement Person
			if (request.getQualifier().equals(PARTY_REQUIREMENT)) {
				setRequirementPersonFilter(request);
			}
			//Find the entry in the variable name list to get the class and method.
			Object[] methodInfo = (Object[]) getAllRetrieveVariables().get(thisVariable);
			if (methodInfo == null) {
				StringBuffer err = new StringBuffer();
				err.append("Retrieve variable name is invalid: ");
				err.append(request.getVariable());
				throw new NbaBaseException(err.toString(), NbaExceptionType.WARNING);	// SPR3329
				//code deleted SPR3329
			}
			//The class name is the first element. Locate the instance of the resolver in the
			//Resolvers map.
			Object resolverInstance = resolvers.get((String) methodInfo[0]); // SPR3290
			//Get the Method.
			Method aMethod = (Method) methodInfo[1];
			Object[] args = new Object[1];
			args[0] = request;
			//Invoke the method on the class instance, passing the request as the only arument.
			try {
				aMethod.invoke(resolverInstance, args);
			} catch (Exception e) {
				StringBuffer err = new StringBuffer();
				err.append("Error invoking variable resolution routine:");
				err.append(request.getVariable());
				getLogger().logError(err);
				throw new NbaBaseException(err.toString(), e);
			}
		}
	}
	/**
	 * Override the the Back End System id in the Map used for
	 * query arguments.
	 * @param request - data request container
	 */
	protected void setBesTranslationValue(NbaOinkRequest request) throws NbaBaseException {
		getTblKeys().put(NbaTableAccessConstants.C_SYSTEM_ID, request.getBesTranslate());
	}
	//begin-NBA053
	/**
	 * Set the NbaTXLife update Destination.
	 * @param newContractSource 
	 */
	public void setContractDest(NbaTXLife newContractSource) throws NbaBaseException {
		setContractDest(newContractSource, false);
	}

	public void setContractDest(NbaTXLife newContractSource, boolean newApplicationUpdate) throws NbaBaseException {
		setNbaTXLife(newContractSource);
		addUpdateVariables(NbaUpdateContract.getVariables());
		setNbaUpdateContract(new NbaUpdateContract());
		getNbaUpdateContract().setApplicationUpdateMode(newApplicationUpdate);
		addResolvers(getNbaUpdateContract().getClass().getName(), getNbaUpdateContract()); // SPR3290
		getNbaUpdateContract().initializeObjects(newContractSource);
	}
	//end-NBA053
	/**
	 * Set the NbaTXLife and NbaAcdb update Destination.
 	 * @param nbaAcdb 
  	 * @param nbaTXLife 
	 */
	//ACN015 New method	
	public void setAcdbDest(NbaAcdb nbaAcdb, NbaTXLife nbaTXLife) throws NbaBaseException {
		//Add all the retrieve variables in NbaUpdateAcdb to HashMap.	
		addUpdateVariables(NbaUpdateAcdb.getVariables());
		//Create a new instance of NbaUpdateAcdb and set it in this instance of NbaOinkDataAccess.
		setNbaUpdateAcdb(new NbaUpdateAcdb());
		//Add the instance created above to resolvers map to call retrieve methods using reflection.
		addResolvers(getNbaUpdateAcdb().getClass().getName(), getNbaUpdateAcdb()); // SPR3290
		//Set nbaAcdb object passed by the user in NbaRetrieveAcdbData. 
		//Retrieve methods will retrieve values from this nbaAcdb object.
		getNbaUpdateAcdb().setNbaAcdb(nbaAcdb);
		getNbaUpdateAcdb().setUpdateMode(true);
		getNbaUpdateAcdb().initializeObjects(nbaTXLife);
		getNbaUpdateAcdb().setUpdateMode(false);
	}

	/**
	 * Store a NbaTxLife and NbaLob update destinations.
	 * @param aNbaTXLife
	 * @param aNbaLob
	 */
	public void setContractDest(NbaTXLife aNbaTXLife, NbaLob aNbaLob) throws NbaBaseException {
		setContractDest(aNbaTXLife);
		setLobDest(aNbaLob);
	}
	/**
	 * Store a NbaTxLife containing contract information.
	 * @param newContractSource 
	 */
	public void setContractSource(NbaTXLife newContractSource) throws NbaBaseException {
		setNbaTXLife(newContractSource);
		addRetrieveVariables(NbaRetrieveContractData.getVariables());
		setNbaRetrieveContractData(new NbaRetrieveContractData());
		addResolvers(getNbaRetrieveContractData().getClass().getName(), getNbaRetrieveContractData()); // SPR3290
		getNbaRetrieveContractData().setUpdateMode(true);
		getNbaRetrieveContractData().initializeObjects(newContractSource);
		getNbaRetrieveContractData().setUpdateMode(false);
	}
	/**
	 * Store a NbaTxLife and NbaAcdb containing contract information.
	 * @param nbaAcdb 
	 * @param nbaTXLife 
	 */
	//	ACN015 New method		
	public void setAcdbSource(NbaAcdb nbaAcdb, NbaTXLife nbaTXLife) throws NbaBaseException {//ACP
		//Add all the retrieve variables in NbaRetrieveAcdbData to HashMap.	
		addRetrieveVariables(NbaRetrieveAcdbData.getVariables());
		//Create a new instance of NbaRetrieveAcdbData and set it in this instance of NbaOinkDataAccess.
		setNbaRetrieveAcdbData(new NbaRetrieveAcdbData());
		//Add the instance created above to resolvers map to call retrieve methods using reflection.
		addResolvers(getNbaRetrieveAcdbData().getClass().getName(), getNbaRetrieveAcdbData()); // SPR3290
		//Set nbaAcdb object passed by the user in NbaRetrieveAcdbData. 
		//Retrieve methods will retrieve values from this nbaAcdb object.
		getNbaRetrieveAcdbData().setNbaAcdb(nbaAcdb);
		getNbaRetrieveAcdbData().setUpdateMode(true);
		getNbaRetrieveAcdbData().initializeObjects(nbaTXLife);
		getNbaRetrieveAcdbData().setUpdateMode(false);
	}
	
	/**
	 * Store a NbaTxLife containing contract information.
	 * @param newTxLifeSource 
	 */
	 //P2AXAL016 new method added
	public void setPlanSource(NbaTXLife newTxLifeSource, AccelProduct nbaProduct) throws NbaBaseException {
		addRetrieveVariables(NbaRetrievePlanData.getVariables());
		setNbaRetrievePlanData(new NbaRetrievePlanData());
		addResolvers(getNbaRetrievePlanData().getClass().getName(), getNbaRetrievePlanData());
		getNbaRetrievePlanData().setUpdateMode(true);
		getNbaRetrievePlanData().setNbaProduct(nbaProduct);
		getNbaRetrievePlanData().initializeObjects(newTxLifeSource);
		getNbaRetrievePlanData().setUpdateMode(false);
	}	

	
	//NBA213 deleted code
	/**
	 * Store a NbaTxLife and NbaLob from which information will be resolved.
	 * @param aNbaTXLife
	 * @param aNbaLob
	 */
	public void setContractSource(NbaTXLife aNbaTXLife, NbaLob aNbaLob) throws NbaBaseException {
		setContractSource(aNbaTXLife);
		setLobSource(aNbaLob);
		getNbaRetrieveContractData().intializeDataFromLob(aNbaLob);
	}
	/**
	 * Set the NbaLob and Contract destination values from the NbaDst.
	 * @param newLobDest
	 */
	public void setDstDest(NbaDst newDstDest) throws NbaBaseException {
		//begin SPR2061
		if (newDstDest.isCase() && null == getNbaTXLife()) {	//do not replace if already initialized
			NbaTXLife xml103 = newDstDest.getNbaCase().getXML103Source();
			if (null != xml103) {
				setContractDest(xml103);	
			}
		}
		setLobDest(newDstDest.getNbaLob());
		//end SPR2061	
	}
	/**
	 * Set the NbaLob and Contract source from the NbaDst source
	 * @param newLobSource
	 */
	public void setDstSource(NbaDst newDstSource) throws NbaBaseException {
		//setup variables on the NbaDstVO
		addRetrieveVariables(NbaRetrieveDstVO.getVariables());
		setNbaRetrieveDstVO(new NbaRetrieveDstVO());
		addResolvers(getNbaRetrieveDstVO().getClass().getName(), getNbaRetrieveDstVO()); // SPR3290
		getNbaRetrieveDstVO().initializeObjects(newDstSource);

		//begin SPR2061		
		if (newDstSource.isCase() && null == getNbaTXLife()) {	//do not replace if already initialized
			NbaTXLife xml103 = newDstSource.getNbaCase().getXML103Source();
			if (null != xml103) {
				setContractSource(xml103);
				getNbaRetrieveContractData().intializeDataFromLob(newDstSource.getNbaLob());
			}
		}
		setLobSource(newDstSource.getNbaLob());
		//end SPR2061
	}
	/**
	 * Set the value of the NbaOinkFormatter object.
	 * @param newFormatter com.csc.fsg.nba.datamanipulation.NbaOinkFormatter
	 */
	public void setFormatter(NbaOinkFormatter newFormatter) {
		formatter = newFormatter;
		formatter.setNbaOinkDataAccess(this);
	}
	/**
	 * Set the NbaLob update Destination containing LOB information.
	 * @param newLobDestination
	 */
	public void setLobDest(NbaLob newLobDestination) {
		addUpdateVariables(NbaUpdateLOB.getVariables());
		setNbaUpdateLOB(new NbaUpdateLOB());
		addResolvers(getNbaUpdateLOB().getClass().getName(), getNbaUpdateLOB()); // SPR3290
		getNbaUpdateLOB().initializeObjects(newLobDestination);
	}
	/**
	 * Set the NbaLob source containing LOB information.
	 * @param newLobSource
	 */
	public void setLobSource(NbaLob newLobSource) {
		addRetrieveVariables(NbaRetrieveLOB.getVariables());
		setNbaRetrieveLOB(new NbaRetrieveLOB());
		addResolvers(getNbaRetrieveLOB().getClass().getName(), getNbaRetrieveLOB()); // SPR3290
		getNbaRetrieveLOB().initializeObjects(newLobSource);
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (10/10/2002 1:29:01 PM)
	 * @param newNbaOinkHTMLFormatter com.csc.fsg.nba.datamanipulation.NbaOinkHTMLFormatter
	 */
	protected void setNbaOinkHTMLFormatter(NbaOinkHTMLFormatter newNbaOinkHTMLFormatter) {
		nbaOinkHTMLFormatter = newNbaOinkHTMLFormatter;
	}
	/**
	 * Set the value of the NbaRetrieveContractData object.
	 * @param newNbaRetrieveContractData com.csc.fsg.nba.datamanipulation.NbaRetrieveContractData
	 */
	protected void setNbaRetrieveContractData(NbaRetrieveContractData newNbaRetrieveContractData) {
		nbaRetrieveContractData = newNbaRetrieveContractData;
	}
	/**
	 * Set the value of the NbaRetrieveDstVO object.
	 * @param newNbaRetrieveDstVO com.csc.fsg.nba.datamanipulation.NbaRetrieveDstVO
	 */
	public void setNbaRetrieveDstVO(NbaRetrieveDstVO newNbaRetrieveDstVO) {
		nbaRetrieveDstVO = newNbaRetrieveDstVO;
	}
	/**
	 * Set the value of the NbaRetrieveLOB object.
	 * @param newNbaRetrieveLOB com.csc.fsg.nba.datamanipulation.NbaRetrieveLOB
	 */
	protected void setNbaRetrieveLOB(NbaRetrieveLOB newNbaRetrieveLOB) {
		nbaRetrieveLOB = newNbaRetrieveLOB;
	}
	/**
	 * Set the value of the NbaTableAccessor object.
	 * @param newNbaTableAccessor NbaTableAccessor
	 */
	protected void setNbaTableAccessor(NbaTableAccessor newNbaTableAccessor) {
		nbaTableAccessor = newNbaTableAccessor;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (10/21/2002 7:48:23 PM)
	 * @param newNbaTXLife com.csc.fsg.nba.vo.NbaTXLife
	 */
	protected void setNbaTXLife(com.csc.fsg.nba.vo.NbaTXLife newNbaTXLife) {
		nbaTXLife = newNbaTXLife;
	}
	/**
	 * Set the value of the NbaUpdateContract object.
	 * @param newNbaUpdateContract com.csc.fsg.nba.datamanipulation.NbaUpdateContract
	 */
	protected void setNbaUpdateContract(NbaUpdateContract newNbaUpdateContract) {
		nbaUpdateContract = newNbaUpdateContract;
	}
	/**
	 * Set the value of the NbaUpdateLOB object.
	 * @param newNbaUpdateLOB com.csc.fsg.nba.datamanipulation.NbaUpdateLOB
	 */
	protected void setNbaUpdateLOB(NbaUpdateLOB newNbaUpdateLOB) {
		nbaUpdateLOB = newNbaUpdateLOB;
	}
	/**
	 * Set the filter value for the requirement person.
	 * @param request - data request container
	 */
	protected void setRequirementPersonFilter(NbaOinkRequest request) throws NbaBaseException {
		NbaOinkRequest tempRequest = new NbaOinkRequest();
		tempRequest.setVariable("ReqPersonCodeLOB");
		retrieveValue(tempRequest);
		long roleCode = ((Integer) tempRequest.getValue().get(0)).longValue();
		tempRequest.setVariable("ReqPersonSeqLOB");
		retrieveValue(tempRequest);
		String refID = ((Integer) tempRequest.getValue().get(0)).toString();
		request.setPartyFilter(roleCode, refID);
	}
	/**
	 * Set the Map containing the key values to be used to retrieve table data.
	 * @param newTblKeys java.util.Map
	 */
	protected void setTblKeys(java.util.Map newTblKeys) {
		tblKeys = newTblKeys;
	}
	/**
	 * Update the value for the variable identified in the request.
	 * @param request - data request container
	 */
	public void updateValue(NbaOinkRequest request) {
		if (!request.parseVariable()) {
			getLogger().logDebug("Unable to parse variable name:" + request.getVariable());
		}
		//Handle Medical, Risk and RIX data groups
		String thisVariable = request.getRootVariable();
		if (request.getDataGroup().equals("MED")) {
			thisVariable = "MedCondType";
		} else if (request.getDataGroup().equals("RIS")) {
			thisVariable = "RiskQuestions";
		} else if (request.getDataGroup().equals("RIX")) {
			thisVariable = "RiskExtensionQuestions";
		}
//		 begin AXAL3.7.06
		  else if (request.getDataGroup().equals("AIX")) {
			thisVariable = "APPLICATIONINFOEXTENSIONQUESTIONS";
		} else if (request.getDataGroup().equals("CLX")) {
			thisVariable = "CLIENTEXTENSIONQUESTIONS";
		} else if (request.getDataGroup().equals("MEX")) {
			thisVariable = "MEDICALEXAMEXTENSIONQUESTIONS";
		} else if (request.getDataGroup().equals("PAX")) {
			thisVariable = "PARTYEXTENSIONQUESTIONS";
		} else if (request.getDataGroup().equals("PEX")) {
			thisVariable = "PERSONEXTENSIONQUESTIONS";
		} else if (request.getDataGroup().equals("INX")) {
			thisVariable = "INTENTEXTENSIONQUESTIONS";
		} else if (request.getDataGroup().equals("EMP")) {
			thisVariable = "EMPLOYMENTQUESTIONS";
		} else if (request.getDataGroup().equals("TIA")) {
			thisVariable = "TEMPINSAGREEMENTINFOQUESTIONS";
		}else if (request.getDataGroup().equals("POX")) {
			thisVariable = "POLICYEXTENSIONQUESTIONS";
		} else if (request.getDataGroup().equals("TAD")) {//A4_AXAL001
			thisVariable = "TEMPINSAGREEMENTDETAILSQUESTIONS";
		} else if (request.getDataGroup().equals("EIX")) {
			thisVariable = "EMPLOYMENTEXTENSIONQUESTIONS"; //P2AXAL066
		}
		// end AXAL3.7.06		
		
		//Find the entry in the variable name list to get the class and method.
		Object[] methodInfo = (Object[]) getAllUpdateVariables().get(thisVariable.toUpperCase());
		if (methodInfo == null) {
			getLogger().logError("Update variable name is invalid: " + request.getVariable());
			return;
		}
		//The class name is the first element. Locate the instance of the resolver in the
		//Resolvers map.
		Object resolverInstance = resolvers.get((String) methodInfo[0]); // SPR3290
		//Get the Method.
		Method aMethod = (Method) methodInfo[1];
		Object[] args = new Object[1];
		args[0] = request;
		//Invoke the method on the class instance, passing the request as the only arument.
		try {
			aMethod.invoke(resolverInstance, args);
		} catch (Exception e) {
			getLogger().logError("Error invoking variable update routine:" + request.getVariable());
			getLogger().logError("   Exception:" + e.toString());
		}
	}
	/**
	* Retrieves data from the XML source based on tag definitions exposed in the HTTP Request object.
	* @param request
	* @return String
	* @throws NbaBaseException
	*/
	//New method SPR1337
	public void performRetrieves(HttpServletRequest request) throws NbaBaseException {
		NbaOinkRequest aNbaOinkRequest = new NbaOinkRequest();
		Enumeration enumer = request.getParameterNames();  //NBA256
		while (enumer.hasMoreElements()) {  //NBA256
			try {
				String aVariable = (String) enumer.nextElement();  //NBA256
				if (aVariable.length() > 8 && !aVariable.startsWith("Nba_TP")) {
					aNbaOinkRequest.setVariable(aVariable);
					String[] tagOccurrences = request.getParameterValues(aNbaOinkRequest.getVariable());
					aNbaOinkRequest.setCount(tagOccurrences.length);
					this.generateJavaScript(aNbaOinkRequest);
				}
			} catch (NbaBaseException theException) {
				throw theException;
			} catch (Throwable theException) {
				throw new NbaBaseException("Severe error encountered resolving value for " + aNbaOinkRequest.getVariable(), theException);
			}
		}
		NbaOinkTablePaneProcessor processor = new NbaOinkTablePaneProcessor(request);
		processor.retrieveTableData(this);
		processor.buildData(request, this);
	}
	/**
	 * Updates the XML source based on tag definitions exposed via the HTTP Request object.
	 * @param request
	 * @return String
	 * @throws NbaBaseException
	 */
	//New method SPR1337
	public void performUpdates(HttpServletRequest request) throws NbaBaseException {
		NbaOinkRequest aNbaOinkRequest = new NbaOinkRequest();
		Enumeration enumer = request.getParameterNames();  //NBA256
		while (enumer.hasMoreElements()) {   //NBA256
			try {
				String aVariable = (String) enumer.nextElement();  //NBA256
				if (aVariable.length() > 8 && !aVariable.startsWith("Nba_TP")) {
					aNbaOinkRequest.setVariable(aVariable);
					String[] tagValues = request.getParameterValues(aNbaOinkRequest.getVariable());
					aNbaOinkRequest.setValues(tagValues);
					this.updateValue(aNbaOinkRequest);
				}
			} catch (Throwable theException) {
				throw new NbaBaseException(theException);
			}
		}
		NbaOinkTablePaneProcessor processor = new NbaOinkTablePaneProcessor(request);
		processor.saveData(request, this);
	}
	

	//NBA213 deleted code

	/**
	 * @return
	 */
	//NBA237
	public AccelProduct getNbaProduct() {
		return nbaProduct;
	}

	/**
	 * @param product
	 */
	 //NBA237 changed method signature
	public void setNbaProduct(AccelProduct product) {
		nbaProduct = product;
	}

	/**
	 * @return nbaRetrieveAcdbData
	 */
	//	ACN015 New method	
	public NbaRetrieveAcdbData getNbaRetrieveAcdbData() {
		return nbaRetrieveAcdbData;
}
	/**
	 * @return nbaUpdateAcdb
	 */
	//	ACN015 New method		
	public NbaUpdateAcdb getNbaUpdateAcdb() {
		return nbaUpdateAcdb;
	}

	/**
	 * @param data
	 */
	//	ACN015 New method	
	public void setNbaRetrieveAcdbData(NbaRetrieveAcdbData data) {
		nbaRetrieveAcdbData = data;
	}

	/**
	 * @param db
	 */
	//	ACN015 New method		
	public void setNbaUpdateAcdb(NbaUpdateAcdb db) {
		nbaUpdateAcdb = db;
	}
	/**
	 * @return Returns the nbaRetrievePlanData.
	 */
	 //P2AXAL016 new method
	public NbaRetrievePlanData getNbaRetrievePlanData() {
		return nbaRetrievePlanData;
	}
	/**
	 * @param nbaRetrievePlanData The nbaRetrievePlanData to set.
	 */
	 //P2AXAL016 new method
	public void setNbaRetrievePlanData(NbaRetrievePlanData nbaRetrievePlanData) {
		this.nbaRetrievePlanData = nbaRetrievePlanData;
	}
}
