package com.csc.fsg.nba.datamanipulation; //NBA201

/* 
 * *******************************************************************************<BR>
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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import com.axa.fsg.nba.foundation.AxaConstants;
import com.csc.fs.accel.ui.util.SortingHelper;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.AxaValueObjectUtils;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.html.support.NbaDataObjectRenderer;
import com.csc.fsg.nba.process.workflow.NbaSystemDataProcessor;
import com.csc.fsg.nba.tableaccess.NbaReasonsData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaHolding;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaTransOliCode;
import com.csc.fsg.nba.vo.txlife.*;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

/**
 *  NbaRetrieveContractData retrieves information from an NbaTXLife object. A static 
 *  initializer method generates a Map containing the variable names that may be used 
 *  and the Method objects used to access them. Map entries are present for all methods
 *  of the class whose method name starts with the string "retrieve" and which accept
 *  an NbaOinkRequest as an argument. This Map of variables is returned to the 
 *  NbaOinkDataAccess when the NbaTXLife source is initialized.
 *
 *  When retrieving information, all values that satisfy the variable qualifier and 
 *  filter values are retrieved from the NbaTXLife, up to the limit in the count field. 
 *  Formatting information (phone, social security, etc) is also stored for use by 
 *  the formatter. If a table is associated with the field, the table name is also 
 *  stored.	
 * <p>  
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>NBA021</td><td>Version 1</td><td>Object Interactive Name Keeper</td></tr>
 * <tr><td>NBA059</td><td>Version 3</td><td>Jet Suitability</td></tr>
 * <tr><td>NBP001</td><td>Version 3</td><td>nbProducer</td></tr>
 * <tr><td>NBP033</td><td>Version 3</td><td>Architecure Changes</td></tr>
 * <tr><td>NBP041</td><td>Version 3</td><td>Billing Business Function</td></tr>
 * <tr><td>NBA087</td><td>Version 3</td><td>Post Approval & Issue Requirements</td></tr>
 * <tr><td>SPR1335</td><td>Version 3</td><td>Vantage Beneficiary Changes</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD2.8</td></tr>
 * <tr><td>NBA064</td><td>Version 3</td><td>Contract Validation</td></tr>
 * <tr><td>NBA072</td><td>Version 3</td><td>Calculations</td></tr>
 * <tr><td>SPR1778</td><td>Version 4</td><td>Correct problems with SmokerStatus and RateClass in Automated Underwriting, and Validation.</td></tr>
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Calculations</td></tr>
 * <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
 * <tr><td>ACP002</td><td>Version 4</td><td>IU-Driver (Drv/Cmn)</td></tr>
 * <tr><td>ACP001</td><td>Version 4</td><td>IU-Lab Result Processing changes</td></tr>
 * <tr><td>ACP005</td><td>Version 4</td><td>IU-UFS</td></tr>
 * <tr><td>ACP007</td><td>Version 4</td><td>Medical Screening</td></tr>
 * <tr><td>ACP022</td><td>Version 4</td><td>Foreign Travel</td></tr>
 * <tr><td>ACP013</td><td>Version 4</td><td>Family History</td></tr>
 * <tr><td>ACP009</td><td>Version 4</td><td>Non Medical Screening</td></tr>
 * <tr><td>ACP006</td><td>Version 4</td><td>MIB Evaluation</td></tr>
 * <tr><td>ACP017</td><td>Version 4</td><td>Key Person</td></tr>
 * <tr><td>ACP018</td><td>Version 4</td><td>Buy Sell</td></tr>
 * <tr><td>ACP014</td><td>Version 4</td><td>Financial Screening</td></tr>
 * <tr><td>ACP016</td><td>Version 4</td><td>Aviation Evaluation</td></tr>
 * <tr><td>ACP015</td><td>Version 4</td><td>Profile Evaluation</td></tr>
 * <tr><td>ACP008</td><td>Version 4</td><td>Preferred Processing</td></tr>
 * <tr><td>SPR2387</td><td>Version 5</td><td>7-Pay premium calculation is not calculated for a VUL plan</td></tr>
 * <tr><td>ACN007</td><td>Version 4</td><td>Reflexive Questioning</td></tr>
 * <tr><td>SPR2396</td><td>Version 5</td><td>Automated Underwriting never generates the Hazardous Activity question failed</td></tr>
 * <tr><td>NBA115</td><td>Version 5</td><td>Credit card payment and authorization</td></tr>
 * <tr><td>SPR2532</td><td>Version 5</td><td>The presence of a Medical Condition via the MedicalConditionType field ALWAYS generates an impairment regardless of the answer.</td></tr>
 * <tr><td>SPR1753</td><td>Version 5</td><td>Automated Underwriting and Requirements Determination Should Detect Severe Errors for Both AC and Non - AC</td></tr>
 * <tr><td>NBA129</td><td>Version 5</td><td>xPression Correspondence</td></tr>
 * <tr><td>NBA128</td><td>Version 5</td><td>Workflow changes project</td></tr>
 * <tr><td>NBA132</td><td>Version 6</td><td>Equitable Distribution of Work</td></tr>
 * <tr><td>SPR2757</td><td>Version 6</td><td>Financial Impairment is not getting generated if the Spouse/Child Address is different from the Primary Insured's Address</td></tr>
 * <tr><td>SPR3047</td><td>Version 6</td><td>Basic validation P035 does not create system message # 1017 when it is invoked by APAPPSUB process and the signature is missing.</td></tr>
 * <tr><td>SPR2590</td><td>Version 6</td><td>Proposed Table Substandard Ratings are not being excluded from premium calculations.</td></tr>
 * <tr><td>SPR2170</td><td>Version 6</td><td>Sub-standard rating extra amounts are not calculated correctly for percent rating.</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>General Code Clean Up Issues for Version 6</td></tr>
 * <tr><td>NBA142</td><td>Version 6</td><td>Minimum Initial Premium</td></tr>
 * <tr><td>SPR3165</td><td>Version 6</td><td>Problems with Replacement Form, Voided Check, and Signed Check Requirements</td></tr>
 * <tr><td>NBA192</td><td>Version 7</td><td>Requirement Management Enhancement</td></tr>
 * <tr><td>SPR3053</td><td>Version 7</td><td>Use thread safe approach while initializing static variables</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7 </td></tr>
 * <tr><td>SPR3329</td><td>Version 7</td><td>Prevent erroneous "Retrieve variable name is invalid" messages from being generated by OINK</td></tr>
 * <tr><td>NBA139</td><td>Version 7</td><td>Plan COde Determination</td></tr>
 * <tr><td>NBA117</td><td>Version 7</td><td>Pending VANTAGE-ONE Calculations </td></tr>
 * <tr><td>SPR3353</td><td>Version 8</td><td>OINK problem handling multiple requirements in the same XML</td></tr>
 * <tr><td>AXAL3.7.38</td><td>AXA Life Phase 1</td><td>Policy Product For Life (PPfL)</td></tr>
 * <tr><td>AXAL3.7.07</td><td>AXA Life Phase 1</td><td>Automated Underwriting</td></tr>
 * <tr><td>AXAL3.7.06</td><td>AXA Life Phase 1</td><td>Requirement Determination</td></tr>
 * <tr><td>AXAL3.7.43</td><td>AXA Life Phase 1</td><td>Money Underwriting</td></tr>
 * <tr><td>AXAL3.7.3M1</td><td>AXA Life Phase 1</td><td>Informal Implementation Miscellaneous Requirements Part 1</td></tr>
 * <tr><td>AXAL3.7.13I</td><td>AXA Life Phase 1</td><td>Informal Corresponsence</td></tr>
 * <tr><td>NBA254</td><td>Version 8</td><td>Automatic Closure and Refund of CWA</td></tr>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * <tr><td>ALS2302</td><td>>AXA Life Phase 1</td><td>Flat premium is incorrect</td></tr>
 * <tr><td>ALCP161</td><td>AXA Life Phase 1</td><td>Ultimate Amounts</td></tr>
 * <tr><td>ALPC96</td><td>AXA Life Phase 1</td><td>xPression OutBound Email</td></tr>
 * <tr><td>ALPC075</td><td>AXA Life Phase 1</td><td>State Variations on Exclusion Riders</td></tr>
 * <tr><td>AXAL3.7.26</td><td>AXA Life Phase 1</td><td>OLSA Interface</td></tr>
 * <tr><td>ALPC119</td><td>AXA Life Phase 1</td><td>YRT Discount Accounting </td></tr>
 * <tr><td>ALS2062</td><td>AXA Life Phase 1</td><td>QC# 857 E2E Xpression On demand letter 4 - Informal Decline Retail</td></tr>
 * <tr><td>ALS4938</td><td>AXA Life Phase 1</td><td>QC # 4096 - Contract Val - 3.7.40 - Delivery Receipt processed prior to Initial Payment. CV error did not generate</td></tr>
 * <tr><td>ALPC234</td><td>AXA Life Phase 1</td><td>Unbound Processing</td></tr>
 * <tr><td>P2AXAL036</td><td>AXA Life Phase 2</td><td>Temporary Express Commission</td></tr>
 * <tr><td>AXAL3.7.10C</td><td>AXA Life Phase 2</td><td>Reinsurance Calculator</td></tr>
 * <tr><td>P2AXAL018</td><td>AXA Life Phase 2</td><td>Ommission Requirements</td></tr>
 * <tr><td>AXAL3.7.10B</td><td>AXA Life Phase 2</td><td>Reinsurance</td></tr>
 * <tr><td>P2AXAL005</td><td>AXA Life Phase 2</td><td>Legal Policy Stop</td></tr>
 * <tr><td>P2AXAL035</td><td>AXA Life Phase 2</td><td>Amendment / Endorsement / Delivery Instructions</td></tr> 
 * <tr><td>NBA300</td><td>AXA Life Phase 2</td><td>Term Conversion</td></tr>
 * <tr><td>A2_AXAL003</td><td>AXA Life NewApp</td><td>New Application � Application Entry A2</td></tr>
 * <tr><td>NBA297</td><td>1201</td><td>Suitability</td></tr>
 * <tr><td>CR60956</td><td>AXA Life Phase 2</td><td>Life 70 Reissue</td></tr>
 * <tr><td>A4_AXAL001</td><td>AXA Life NewApp</td><td>New Application � Application Entry A4</td></tr>
 * <tr><td>P2AXAL054</td><td>AXA Life Phase 2</td><td>Omissions and Contract Validations</td></tr>
 * <tr><td>A3_AXAL005</td><td>AXA Life New App A3</td><td>Amendment & Endorsement</td></tr>
 * <tr><td>CR58636</td><td>Discretionary</td><td>ADC Retrofit</td></tr>
 * <tr><td>CR1343972</td><td>AXA Life Phase 2</td><td>Reinsurance CLR and ROPR Calculation</td></tr>
 * <tr><td>P2AXAL056</td><td>AXA Life Phase 2 Release2</td><td>Reinsurance</td></tr>
 * <tr><td>CR61047</td><td>AXA Life Phase 2 Release2</td><td>LTC Refresh</td></tr>
 * <tr><td>CR1345266</td><td>AXA Life Phase 2 CR</td><td>Advanced Date for OPAI Election</td></tr>
 * <tr><td>CR735253-735254</td><td>AXA Life Phase 2</td><td>Reinsurance Interface</td></tr>
 * <tr><td>P2AXAL053</td><td>AXA Life Phase 2</td><td>R2 Auto Underwriting</td></tr>
 * <tr><td>SR657319</td><td>Discretionary</td><td>Manual selection of Rate Class</td></tr>
 * <tr><td>SR564247(APSL2525)</td><td>Discretionary</td><td>Predictive Full Implementation</td></tr>
 * <tr><td>CR1343973</td><td>Discretionary</td><td>Reinsurance Corr Display</td></tr>
 * <tr><td>CR1455063</td><td>Discretionary</td><td>Joint Insured Correspondence</td></tr>
 * <tr><td>APSL3258</td><td>Discretionary</td><td>FATCA</td></tr>
 * <tr><td>APSL3447</td><td>Discretionary</td><td>HVT</td></tr>
 * <tr><td>APSL4036</td><td>Discretionary</td><td>LTC Replacement</td></tr>
 * <tr><td>APSL4635</td><td>Discretionary</td><td>Term Conversion Underwriting rate conversion</td></tr>
 * <tr><td>APSL4872</td><td>Discretionary</td><td>Requirement As Data</td></tr>
 * </table>
 * </p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.datamanipulation.NbaContractDataAccess
 * @see com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess
 * @see com.csc.fsg.nba.datamanipulation.NbaOinkRequest
 * @since New Business Accelerator - Version 2 
 */

public class NbaRetrieveContractData extends NbaContractDataAccess {
	public static final String METHOD_PREFIX_GET = "get"; //SPR3053

	static HashMap variables = new HashMap();

	static Map riskVariables = initializeMethodMap(Risk.class); //SPR3053

	static Map riskExtensionVariables = initializeMethodMap(RiskExtension.class); //SPR3053

	//Begin AXAL3.7.07
	static Map ApplicationInfoExtensionVariables = initializeMethodMap(ApplicationInfoExtension.class);

	static Map ClientExtensionVariables = initializeMethodMap(ClientExtension.class);

	static Map MedicalExamExtensionVariables = initializeMethodMap(MedicalExamExtension.class);

	static Map PartyExtensionVariables = initializeMethodMap(PartyExtension.class);

	static Map PersonExtensionVariables = initializeMethodMap(PersonExtension.class);

	static Map IntentExtensionVariables = initializeMethodMap(IntentExtension.class);

	static Map EmploymentVariables = initializeMethodMap(Employment.class);

	static Map TempInsAgreementInfoVariables = initializeMethodMap(TempInsAgreementInfo.class);
	//End AXAL3.7.07
	//Begin AXAL3.7.06
	static Map PolicyExtensionVariables = initializeMethodMap(PolicyExtension.class);
	// End AXAL3.7.06
	
	static Map TempInsAgreementDetailsInfoVariables = initializeMethodMap(TempInsAgreementDetails.class); // A4_AXAL001
	
	static Map EmploymentExtensionVariables = initializeMethodMap(EmploymentExtension.class); //ALNA366
	
	static Map priorMap = new HashMap(); //CR61047
	
	private static NbaLogger logger = null;
	
	public static final String EMPTY_STRING = ""; //APSL5164
	
	public static final String ZERO_AMT = "0.0"; //APSL5164
	
	static {
		NbaRetrieveContractData aNbaRetrieveContractData = new NbaRetrieveContractData();
		String thisClassName = aNbaRetrieveContractData.getClass().getName();
		Method[] allMethods = aNbaRetrieveContractData.getClass().getDeclaredMethods();
		for (int i = 0; i < allMethods.length; i++) {
			Method aMethod = allMethods[i];
			String aMethodName = aMethod.getName();
			if (aMethodName.startsWith("retrieve")) {
				Class[] parmClasses = aMethod.getParameterTypes();
				if (parmClasses.length == 1 && parmClasses[0].getName().equals("com.csc.fsg.nba.datamanipulation.NbaOinkRequest")) {
					Object[] args = { thisClassName, aMethod };
					variables.put(aMethodName.substring(8).toUpperCase(), args);
				}
			}
		}
	}

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return the logger implementation
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaRetrieveContractData.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaRetrieveContractData could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

	/**
	 * Create and initiliaze a Map for methods of a class.
	 * @param clazz the Class object
	 * @return the Map containing all methods
	 */
	//SPR3053 New Method
	protected static Map initializeMethodMap(Class clazz) {
		Method[] allMethods = clazz.getDeclaredMethods();
		Method aMethod = null;
		Map methodsMap = new HashMap();
		int prefixLength = METHOD_PREFIX_GET.length();
		for (int i = 0; i < allMethods.length; i++) {
			aMethod = allMethods[i];
			if (aMethod.getName().startsWith(METHOD_PREFIX_GET)) {
				Class[] parmClasses = aMethod.getParameterTypes();
				if (parmClasses.length == 0) { // SPR3290
					methodsMap.put(aMethod.getName().substring(prefixLength).toUpperCase(), aMethod);
				}
			}
		}
		return methodsMap;
	}

	/**
	 * Obtain the Method to be invoked for a variable for a RiskExtension object.
	 * @param variableName - the variable name
	 */
	//SPR3053 removed parameter aRiskExtension
	protected Object getRiskExtensionMethod(String variableName) {
		//SPR3053 code deleted
		return riskExtensionVariables.get(variableName.toUpperCase());
	}

	/**
	 * Obtain the Method to be invoked for a variable for a Risk object.
	 * @param aRisk - a Risk object
	 * @param variableName - the variable name
	 */
	//SPR3053 removed parameter aRisk
	protected Object getRiskMethod(String variableName) {
		//SPR3053 code deleted
		return riskVariables.get(variableName.toUpperCase());
	}

	/**
	 * Obtain the value for a ApplicationInfoExtension Question value. Reflection is used to message the ApplicationInfoExtension object with a method
	 * composed of the variable name preceded by "get".
	 * 
	 * @param aNbaOinkRequest -
	 *            data request container
	 */
	// AXAL3.7.07 New Method
	public void retrieveApplicationInfoExtensionQuestions(NbaOinkRequest aNbaOinkRequest) throws InvocationTargetException, IllegalAccessException {
		Object methodObject = ApplicationInfoExtensionVariables.get(aNbaOinkRequest.getRootVariable().toUpperCase());
		Class aClass;
		Method aMethod;
		if (methodObject != null) {
			aMethod = (Method) methodObject;
			aClass = aMethod.getReturnType();
		} else {
			if (getLogger().isWarnEnabled()) {
				getLogger().logWarn("Retrieve variable name is invalid: " + aNbaOinkRequest.getVariable());
			}
			return;
		}
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			ApplicationInfo applicationInfo = getApplicationInfo();
			if (applicationInfo != null) {
				int index_extension = getExtensionIndex(applicationInfo.getOLifEExtension(), APPLICATIONINFO_EXTN);
				if (index_extension != -1) {
					ApplicationInfoExtension extension = applicationInfo.getOLifEExtensionAt(index_extension).getApplicationInfoExtension();
					if (extension != null) {
						aNbaOinkRequest.addValueForType(((Method) methodObject).invoke(extension, null), aClass);
					} else {
						aNbaOinkRequest.addUnknownValueForType(aClass);
					}
				} else {
					aNbaOinkRequest.addUnknownValueForType(aClass);
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for a ClientExtension Question value. Reflection is used to message the ClientExtension object with a method composed of the
	 * variable name preceded by "get".
	 * 
	 * @param aNbaOinkRequest -
	 *            data request container
	 */
	// AXAL3.7.07 New Method
	public void retrieveClientExtensionQuestions(NbaOinkRequest aNbaOinkRequest) throws InvocationTargetException, IllegalAccessException {
		Object methodObject = ClientExtensionVariables.get(aNbaOinkRequest.getRootVariable().toUpperCase());
		Class aClass;
		Method aMethod;
		if (methodObject != null) {
			aMethod = (Method) methodObject;
			aClass = aMethod.getReturnType();
		} else {
			if (getLogger().isWarnEnabled()) {
				getLogger().logWarn("Retrieve variable name is invalid: " + aNbaOinkRequest.getVariable());
			}
			return;
		}
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Client client = getClient(aNbaOinkRequest, i);
			if (client != null) {
				int index_extension = getExtensionIndex(client.getOLifEExtension(), CLIENT_EXTN);
				if (index_extension != -1) {
					ClientExtension extension = client.getOLifEExtensionAt(index_extension).getClientExtension();
					if (extension != null) {
						aNbaOinkRequest.addValueForType(((Method) methodObject).invoke(extension, null), aClass);
					} else {
						aNbaOinkRequest.addUnknownValueForType(aClass);
					}
				} else {
					aNbaOinkRequest.addUnknownValueForType(aClass);
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for a MedicalExamExtension Question value. Reflection is used to message the MedicalExamExtension object with a method
	 * composed of the variable name preceded by "get".
	 * 
	 * @param aNbaOinkRequest -
	 *            data request container
	 */
	// AXAL3.7.07 New Method
	public void retrieveMedicalExamExtensionQuestions(NbaOinkRequest aNbaOinkRequest) throws InvocationTargetException, IllegalAccessException {
		Object methodObject = MedicalExamExtensionVariables.get(aNbaOinkRequest.getRootVariable().toUpperCase());
		Class aClass;
		Method aMethod;
		if (methodObject != null) {
			aMethod = (Method) methodObject;
			aClass = aMethod.getReturnType();
		} else {
			if (getLogger().isWarnEnabled()) {
				getLogger().logWarn("Retrieve variable name is invalid: " + aNbaOinkRequest.getVariable());
			}
			return;
		}
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				MedicalExam medicalExam = getMedicalExam(aNbaOinkRequest, i);
				if (medicalExam != null) {
					int index_extension = getExtensionIndex(medicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
					if (index_extension != -1) {
						MedicalExamExtension extension = medicalExam.getOLifEExtensionAt(index_extension).getMedicalExamExtension();
						if (extension != null) {
							aNbaOinkRequest.addValueForType(((Method) methodObject).invoke(extension, null), aClass);
						}
					} else {
						aNbaOinkRequest.addUnknownValueForType(aClass);
					}
				} else {
					aNbaOinkRequest.addUnknownValueForType(aClass);
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for a IntentExtension Question value. Reflection is used to message the IntentExtension object with a method composed of the
	 * variable name preceded by "get".
	 * Get "2nd" Intent row, indicated by Category type "3".
	 * @param aNbaOinkRequest -
	 *            data request container
	 */
	// AXAL3.7.07 New Method
	public void retrieveIntentExtensionQuestions(NbaOinkRequest aNbaOinkRequest) throws InvocationTargetException, IllegalAccessException {
		IntentExtension intentExtension = null;
		Object methodObject = IntentExtensionVariables.get(aNbaOinkRequest.getRootVariable().toUpperCase());
		Class aClass;
		Method aMethod;
		if (methodObject != null) {
			aMethod = (Method) methodObject;
			aClass = aMethod.getReturnType();
		} else {
			if (getLogger().isWarnEnabled()) {
				getLogger().logWarn("Retrieve variable name is invalid: " + aNbaOinkRequest.getVariable());
			}
			return;
		}
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				String partyId = party.getId();
				intentExtension = getBusinessIntentExtension(i, partyId);
				if (intentExtension != null) {
					aNbaOinkRequest.addValueForType(((Method) methodObject).invoke(intentExtension, null), aClass);
				} else {
					aNbaOinkRequest.addUnknownValueForType(aClass);
				}
			} else {
				aNbaOinkRequest.addUnknownValueForType(aClass);
			}
		}
	}

	/**
	 * Obtain the value for a PersonExtension Question value. Reflection is used to message the PersonExtension object with a method composed of the
	 * variable name preceded by "get".
	 * 
	 * @param aNbaOinkRequest -
	 *            data request container
	 */
	// AXAL3.7.07 New Method
	public void retrievePersonExtensionQuestions(NbaOinkRequest aNbaOinkRequest) throws InvocationTargetException, IllegalAccessException {
		Object methodObject = PersonExtensionVariables.get(aNbaOinkRequest.getRootVariable().toUpperCase());
		Class aClass;
		Method aMethod;
		if (methodObject != null) {
			aMethod = (Method) methodObject;
			aClass = aMethod.getReturnType();
		} else {
			if (getLogger().isWarnEnabled()) {
				getLogger().logWarn("Retrieve variable name is invalid: " + aNbaOinkRequest.getVariable());
			}
			return;
		}
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				int index_extension = getExtensionIndex(person.getOLifEExtension(), PERSON_EXTN);
				if (index_extension != -1) {
					PersonExtension extension = person.getOLifEExtensionAt(index_extension).getPersonExtension();
					if (extension != null) {
						aNbaOinkRequest.addValueForType(((Method) methodObject).invoke(extension, null), aClass);
					} else {
						aNbaOinkRequest.addUnknownValueForType(aClass);
					}
				} else {
					aNbaOinkRequest.addUnknownValueForType(aClass);
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for a PartyExtension Question value. Reflection is used to message the PartyExtension object with a method composed of the
	 * variable name preceded by "get".
	 * 
	 * @param aNbaOinkRequest -
	 *            data request container
	 */
	// AXAL3.7.06 New Method
	public void retrievePartyExtensionQuestions(NbaOinkRequest aNbaOinkRequest) throws InvocationTargetException, IllegalAccessException {
		Object methodObject = PartyExtensionVariables.get(aNbaOinkRequest.getRootVariable().toUpperCase());
		Class aClass;
		Method aMethod;
		if (methodObject != null) {
			aMethod = (Method) methodObject;
			aClass = aMethod.getReturnType();
		} else {
			if (getLogger().isWarnEnabled()) {
				getLogger().logWarn("Retrieve variable name is invalid: " + aNbaOinkRequest.getVariable());
			}
			return;
		}
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				int index_extension = getExtensionIndex(party.getOLifEExtension(), PARTY_EXTN);
				if (index_extension != -1) {
					PartyExtension extension = party.getOLifEExtensionAt(index_extension).getPartyExtension();
					if (extension != null) {
						aNbaOinkRequest.addValueForType(((Method) methodObject).invoke(extension, null), aClass);
					} else {
						aNbaOinkRequest.addUnknownValueForType(aClass);
					}
				} else {
					aNbaOinkRequest.addUnknownValueForType(aClass);
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for a PolicyExtension Question value. Reflection is used to message the PartyExtension object with a method composed of the
	 * variable name preceded by "get".
	 * 
	 * @param aNbaOinkRequest -
	 *            data request container
	 */
	// AXAL3.7.06 New Method
	public void retrievePolicyExtensionQuestions(NbaOinkRequest aNbaOinkRequest) throws InvocationTargetException, IllegalAccessException {
		Object methodObject = PolicyExtensionVariables.get(aNbaOinkRequest.getRootVariable().toUpperCase());
		Class aClass;
		Method aMethod;
		if (methodObject != null) {
			aMethod = (Method) methodObject;
			aClass = aMethod.getReturnType();
		} else {
			if (getLogger().isWarnEnabled()) {
				getLogger().logWarn("Retrieve variable name is invalid: " + aNbaOinkRequest.getVariable());
			}
			return;
		}
		Policy policy = getPolicy();
		if (policy != null) {
			int index_extension = getExtensionIndex(policy.getOLifEExtension(), POLICY_EXTN);
			if (index_extension != -1) {
				PolicyExtension extension = policy.getOLifEExtensionAt(index_extension).getPolicyExtension();
				if (extension != null) {
					aNbaOinkRequest.addValueForType(((Method) methodObject).invoke(extension, null), aClass);
				} else {
					aNbaOinkRequest.addUnknownValueForType(aClass);
				}
			} else {
				aNbaOinkRequest.addUnknownValueForType(aClass);
			}
		}
	}

	/**
	 * Obtain the value for a Employment Question value. Reflection is used to message the Employment object with a method composed of the variable
	 * name preceded by "get".
	 * 
	 * @param aNbaOinkRequest -
	 *            data request container
	 */
	// AXAL3.7.07 New Method
	public void retrieveEmploymentQuestions(NbaOinkRequest aNbaOinkRequest) throws InvocationTargetException, IllegalAccessException {
		Object methodObject = EmploymentVariables.get(aNbaOinkRequest.getRootVariable().toUpperCase());
		Class aClass;
		Method aMethod;
		if (methodObject != null) {
			aMethod = (Method) methodObject;
			aClass = aMethod.getReturnType();
		} else {
			if (getLogger().isWarnEnabled()) {
				getLogger().logWarn("Retrieve variable name is invalid: " + aNbaOinkRequest.getVariable());
			}
			return;
		}
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				Employment employment = party.getEmploymentAt(0);
				if (employment != null) {
					aNbaOinkRequest.addValueForType(((Method) methodObject).invoke(employment, null), aClass);
				} else {
					aNbaOinkRequest.addUnknownValueForType(aClass);
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for a TempInsAgreementInfo Question value. Reflection is used to message the TempInsAgreementInfo object with a method
	 * composed of the variable name preceded by "get".
	 * 
	 * @param aNbaOinkRequest -
	 *            data request container
	 */
	// AXAL3.7.07 New Method
	public void retrieveTempInsAgreementInfoQuestions(NbaOinkRequest aNbaOinkRequest) throws InvocationTargetException, IllegalAccessException {
		Object methodObject = TempInsAgreementInfoVariables.get(aNbaOinkRequest.getRootVariable().toUpperCase());
		Class aClass;
		Method aMethod;
		if (methodObject != null) {
			aMethod = (Method) methodObject;
			aClass = aMethod.getReturnType();
		} else {
			if (getLogger().isWarnEnabled()) {
				getLogger().logWarn("Retrieve variable name is invalid: " + aNbaOinkRequest.getVariable());
			}
			return;
		}
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			ApplicationInfo applicationInfo = getApplicationInfo();
			if (applicationInfo != null) {
				int index_extension = getExtensionIndex(applicationInfo.getOLifEExtension(), APPLICATIONINFO_EXTN);
				if (index_extension != -1) {
					ApplicationInfoExtension extension = applicationInfo.getOLifEExtensionAt(index_extension).getApplicationInfoExtension();
					if (extension != null) {
						TempInsAgreementInfo tempInsAgreementInfo = extension.getTempInsAgreementInfo();
						if (tempInsAgreementInfo != null) {
							aNbaOinkRequest.addValueForType(((Method) methodObject).invoke(tempInsAgreementInfo, null), aClass);
						} else {
							aNbaOinkRequest.addUnknownValueForType(aClass);
						}
					} else {
						aNbaOinkRequest.addUnknownValueForType(aClass);
					}
				} else {
					aNbaOinkRequest.addUnknownValueForType(aClass);
				}
			} else {
				break;
			}
		}
	}

	//New Method A4_AXAL001
	public void retrieveTempInsAgreementDetailsQuestions(NbaOinkRequest aNbaOinkRequest) throws InvocationTargetException, IllegalAccessException {
		Object methodObject = TempInsAgreementDetailsInfoVariables.get(aNbaOinkRequest.getRootVariable().toUpperCase());
		Class aClass;
		Method aMethod;
		if (methodObject != null) {
			aMethod = (Method) methodObject;
			aClass = aMethod.getReturnType();
		} else {
			if (getLogger().isWarnEnabled()) {
				getLogger().logWarn("Retrieve variable name is invalid: " + aNbaOinkRequest.getVariable());
			}
			return;
		}
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			ApplicationInfo applicationInfo = getApplicationInfo();
			if (applicationInfo != null) {
				int index_extension = getExtensionIndex(applicationInfo.getOLifEExtension(), APPLICATIONINFO_EXTN);
				if (index_extension != -1) {
					ApplicationInfoExtension extension = applicationInfo.getOLifEExtensionAt(index_extension).getApplicationInfoExtension();
					if (extension != null) {
						TempInsAgreementInfo tempInsAgreementInfo = extension.getTempInsAgreementInfo();
						if (tempInsAgreementInfo != null) {
							Party party = getParty(aNbaOinkRequest, i);
							TempInsAgreementDetails tempInsAgreementDetails = getTempInsAgreementDetails(tempInsAgreementInfo, party.getId());
							if (tempInsAgreementDetails != null) {
								aNbaOinkRequest.addValueForType(((Method) methodObject).invoke(tempInsAgreementDetails, null), aClass);
							} else {
								aNbaOinkRequest.addUnknownValueForType(aClass);
							}
						} else {
							aNbaOinkRequest.addUnknownValueForType(aClass);
						}
					} else {
						aNbaOinkRequest.addUnknownValueForType(aClass);
					}
				} else {
					aNbaOinkRequest.addUnknownValueForType(aClass);
				}
			} else {
				break;
			}
		}
	}
	/**
	 * Answer a Map of the available variables. The keys to the map are the variable names. The values are an array containing the class name string
	 * and the Method to be invoked to retrieve the variable.
	 * @return methods
	 */
	public static Map getVariables() {
		return variables;
	}

	/**
	 * This method initializes superclass objects.
	 * @param objOLife com.csc.fsg.nba.vo.txlife.OLifE
	 */
	public void initializeObjects(NbaTXLife objNbaTXLife) throws NbaBaseException {
		if (objNbaTXLife == null) {
			throw new NbaBaseException("Invalid NbaTXLife");
		}
		setOLifE(objNbaTXLife);
		setNbaTXLife(objNbaTXLife); //NBA053
		initPartyIndices();
		setUpdateMode(false);
	}

	/**
	 * This method extracts Lob values into the OLife XML.
	 * @param nbaLob com.csc.fsg.nba.vo.NbaLob
	 */
	public void intializeDataFromLob(NbaLob nbaLob) {
		//if Annuitant/Insured info is not present create it
		setUpdateMode(true); //automatically create a party object
		Party party = getPartyForPrimaryIns();
		//set GovtID from LOBs
		if (!party.hasGovtID()) {
			party.setGovtID(nbaLob.getSsnTin());
		}
		if (!party.hasGovtIDTC()) { //NBA093
			party.setGovtIDTC(NbaOliConstants.OLI_GOVTID_SSN); //NBA093
		}
		if (!party.hasPersonOrOrganization()) {
			createPerson(party);
		}
		//Set Person Information from LOBs
		Person person = party.getPersonOrOrganization().getPerson();
		if (person != null) {
			//begin NBA129
			person.setFirstName(NbaUtils.convertStringInProperCase(nbaLob.getFirstName()));
			person.setMiddleName(NbaUtils.convertStringInProperCase(nbaLob.getMiddleInitial()));
			person.setLastName(NbaUtils.convertStringInProperCase(nbaLob.getLastName()));
			//end NBA129
		}
		//set App State from LOB
		ApplicationInfo api = getApplicationInfo();
		if (api != null && !api.hasApplicationJurisdiction()) { //ALII237
			api.setApplicationJurisdiction(nbaLob.getAppState());
		}
		setUpdateMode(false);
	}

	/**
	 * Obtain the value for AccountNumber. OLifE.Holding.Banking.AccountNumber is the account number for the banking object.
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.40 rewrote the method
	public void retrieveAccountNumber(NbaOinkRequest aNbaOinkRequest) {
		Banking banking = NbaUtils.getBanking(getOLifE(), NbaOliConstants.OLI_HOLDTYPE_BANKING);
		if (banking == null || !banking.hasAccountNumber()) {
			aNbaOinkRequest.addValue("");
		} else {
			aNbaOinkRequest.addValue(banking.getAccountNumber());
		}
	}

	/**
	 * Obtain the value for AcctHolderName OLifE.Holding.Banking.OLifEExtension.BankingExtension.AcctHolderName is the name of the holder of either
	 * the credit card or bank account associated with payments of either 'electronic funds transfer' or 'credit card billing'. It also holds the
	 * credit card holder name for credit card payments This method should only be used when it is guaranteed that the only one banking object matches
	 * the qualifier
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveAcctHolderName(NbaOinkRequest aNbaOinkRequest) {
		//NBA041 deleted
		//NBA041 begin
		//begin NBA093
		//begin NBA115
		List bankingList = getBankingList(aNbaOinkRequest);
		int requestCount = aNbaOinkRequest.getCount();
		for (int i = 0; i < requestCount; i++) {
			if (bankingList.size() > 0) {
				Banking banking = (Banking) bankingList.get(0);
				BankingExtension bankingExtn = NbaUtils.getFirstBankingExtension(banking);
				if ((bankingExtn != null) && (bankingExtn.hasAccountHolderNameCC())) {
					AccountHolderNameCC acntHolderCC = bankingExtn.getAccountHolderNameCC();
					int addCount = acntHolderCC.getAccountHolderNameCount();
					if (i < addCount) {
						aNbaOinkRequest.addValue(acntHolderCC.getAccountHolderNameAt(i));
						continue;
					}
				}
			}
			aNbaOinkRequest.addUnknownValue("");
		}
		//end NBA115
		//end NBA093

		//NBA041 end
	}

	/**
	 * Obtain the value for AdministeringCompanyCode. OLifE.Holding.Policy.PolicyExtension.AdministeringCompanyCode
	 * @param aNbaOinkRequest
	 * @deprecated this method will be removed in a future release. Use {@link #retrieveAdministeringCarrierCode(NbaOinkRequest)}
	 */
	public void retrieveAdministeringCompanyCode(NbaOinkRequest aNbaOinkRequest) {

		//NBA093 code deleted
		aNbaOinkRequest.addValue(getPolicy().getAdministeringCarrierCode(), NbaTableConstants.NBA_COMPANY); //NBA093

		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_COMPANY);
		}
	}

	/**
	 * Obtain the value for AdministeringCarrierCode. LifE.Holding.Policy.AdministeringCarrierCode
	 * @param aNbaOinkRequest
	 */
	// NBA093 NEW METHOD
	public void retrieveAdministeringCarrierCode(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(getPolicy().getAdministeringCarrierCode(), NbaTableConstants.NBA_COMPANY);

		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_COMPANY);
		}
	}

	/**
	 * Obtain the value for Age. OLifE.Party.PersonOrOrganization.Person.Age is the Age of the person
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveAge(NbaOinkRequest aNbaOinkRequest) {
		try {
			for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					aNbaOinkRequest.addValue(person.getAge());
				} else {
					break;
				}
			}
		} catch (Exception e) {
			e.toString();
		}
	}

	/**
	 * Obtain the value for AgentLicNum, an instance of CompanyProducerID with a carrierApptTypeCode of 1 (Agent).
	 * OLifE().Party().Producer().CarrierAppointment().CompanyProducerID() is the Producer identification number as issued by an insurance company.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveAgentLicNum(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			CarrierAppointment carrierAppointment = getCarrierAppointment(aNbaOinkRequest, i); //NBA053
			if (carrierAppointment != null) {
				aNbaOinkRequest.addValue(carrierAppointment.getCompanyProducerID());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for AlternateInd. OLifE.Holding.Policy.PolicyExtension.AlternateInd
	 * @param aNbaOinkRequest
	 */
	public void retrieveAlternateInd(NbaOinkRequest aNbaOinkRequest) {
		//NBA093 code deleted
		if (getApplicationInfo() != null) { //NBA093
			aNbaOinkRequest.addValue(getApplicationInfo().getAlternateInd()); //NBA093
		}
	}

	/**
	 * Obtain the value for AnnuityWithIns. If the contract is an Annuity and if the Annuity has a Coverage, the value is true. Otherwise, the value
	 * is false. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Annuity.Rider
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveAnnuityWithIns(NbaOinkRequest aNbaOinkRequest) {
		boolean found = false;
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladh = getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
		if (ladh != null && ladh.isAnnuity()) {
			Annuity anAnnuity = ladh.getAnnuity();
			if (anAnnuity.getRiderCount() > 0) {
				found = true;
			}
			//NbA093 code deleted
		}
		aNbaOinkRequest.addValue(found);
	}

	/**
	 * Obtain the value for ApplicationJurisdiction. OLifE.Holding.Policy.ApplicationInfo.ApplicationJurisdiction is the state (jurisdiction) where
	 * the Application was signed.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveApplicationJurisdiction(NbaOinkRequest aNbaOinkRequest) {
		if (getApplicationInfo() != null) {
			aNbaOinkRequest.addValue(getApplicationInfo().getApplicationJurisdiction(), NbaTableConstants.NBA_STATES);
		} else {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_STATES);
		}
	}

	/**
	 * Obtain the value for ApplicationType. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.ApplicationType.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveApplicationType(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			//NBA093 code deleted
			aNbaOinkRequest.addValue(api.getApplicationType(), NbaTableConstants.OLI_LU_APPTYPE); //NBA093 //ALS5795
			return;
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_APPTYPE);//NBA093 //ALS5795			
		}
	}

	/**
	 * Obtain the value for AttachmentData. OLifE.Holding.Attachment.AttachmentData.PCDATA
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveAttachmentData(NbaOinkRequest aNbaOinkRequest) {
		Attachment anAttachment = getAttachment();
		if (anAttachment != null) {
			aNbaOinkRequest.addValue(anAttachment.getAttachmentData().getPCDATA());
		} else {
			aNbaOinkRequest.addValue("");
		}
	}

	/**
	 * Obtain the value for AutomaticPremium A value of true is set if OLifE.Holding.Policy.PaymentMethod = "19". A value of false is set otherwise.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveAutomaticPremium(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(getPolicy() != null && getPolicy().getPaymentMethod() == NbaOliConstants.OLI_PAYMETH_PERMANENTAPP);
	}

	/**
	 * Obtain the value for BirthDate. OLifE.Party.PersonOrOrganization.Person.BirthDate is the date of birth for the person.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveBirthDate(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				aNbaOinkRequest.addValue(person.getBirthDate());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for BirthState, an instance of BirthJurisdictionTc.
	 * OLifE.Party.PersonOrOrganization.Person.PersonExtension.BirthJurisdictionTc is the State/province of birthplace.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveBirthState(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				// NBA093 deleted 6 lines
				aNbaOinkRequest.addValue(person.getBirthJurisdictionTC(), NbaTableConstants.NBA_STATES); //NBA093
				// NBA093 deleted line
			} else {
				break;
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_STATES);
		}
	}

	/**
	 * Obtain the value for CardExpDate, an instance of CreditCardExpDate. OLifE.Holding.Banking.CreditCardExpDate is the expiration date of the
	 * associated credit card in the case that the PaymentMethod is 'credit card billing,' or Banking object represents credit card payment
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveCardExpDate(NbaOinkRequest aNbaOinkRequest) {
		//NBA041 deleted
		//NBA041 begin
		//NBA115 code deleted
		//NBA093 begin
		//begin NBA115
		Banking banking = null;
		List bankingList = getBankingList(aNbaOinkRequest);
		int bankingCount = bankingList.size();
		int requestCount = aNbaOinkRequest.getCount();
		for (int i = 0; i < requestCount; i++) {
			if (i < bankingCount) {
				banking = (Banking) bankingList.get(i);
				if (banking.hasCreditCardExpDate()) {
					aNbaOinkRequest.addValue(banking.getCreditCardExpDate());
					continue;
				}
			}
			aNbaOinkRequest.addUnknownValue("");
		}
		//end NBA115
		//NBA093 end
		//NBA041 end
	}

	/**
	 * Obtain the value for CarrierCode. OLifE.Holding.Policy.CarrierCode is the carrier code for the contract.
	 * @param aNbaOinkRequest
	 */
	public void retrieveCarrierCode(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(getPolicy().getCarrierCode(), NbaTableConstants.NBA_COMPANY);
	}

	/**
	 * Obtain the value for CarrierAdminSystem. OLifE.Holding.Policy.CarrierAdminSystem is the carrier's admin system for the contract.
	 * @param aNbaOinkRequest
	 */
	//NBA104 New Method
	public void retrieveCarrierAdminSystem(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(getPolicy().getCarrierAdminSystem());
	}

	/**
	 * Obtain the value for CarrierName. OLifE.Party.Producer.CarrierAppointment.CarrierName is the name of an insurance company.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveCarrierName(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			CarrierAppointment carrierAppointment = null;
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				boolean found = false;
				if (party.hasProducer()) {
					Producer producer = party.getProducer();
					if (producer.getCarrierAppointment().size() > 0) {
						carrierAppointment = producer.getCarrierAppointmentAt(0);
						aNbaOinkRequest.addValue(carrierAppointment.getCarrierName());
						found = true;
					}
				}
				if (!found) {
					aNbaOinkRequest.addUnknownValue("");
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for Citizenship. OLifE.Party.PersonOrOrganization.Person.Citizenship is the citizenship of the person.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveCitizenship(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				aNbaOinkRequest.addValue(person.getCitizenship(), NbaTableConstants.OLI_LU_NATION);
			}
			break;
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_NATION);
		}
	}

	/**
	 * Obtain the value for CompanyProducerID. OLifE.Party.Producer.CarrierAppointment.CompanyProducerID is the Producer identification number as
	 * issued by an insurance company.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveCompanyProducerID(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			CarrierAppointment carrierAppointment = null;
			//begin NBA132
			Party party = null;
			if (PARTY_PRIWRITINGAGENTAGENCY.equals(aNbaOinkRequest.getQualifier())) {
				party = getAgencyParty(aNbaOinkRequest, i);
			} else {
				party = getParty(aNbaOinkRequest, i);
			}
			//end NBA132
			if (party != null) {
				boolean found = false;
				if (party.hasProducer()) {
					Producer producer = party.getProducer();
					if (producer.getCarrierAppointment().size() > 0) {
						carrierAppointment = producer.getCarrierAppointmentAt(0);
						aNbaOinkRequest.addValue(carrierAppointment.getCompanyProducerID());
						found = true;
					}
				}
				if (!found) {
					aNbaOinkRequest.addUnknownValue("");
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the count for the role identified in the qualifier node of the variable.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveCount(NbaOinkRequest aNbaOinkRequest) {
		int count = 0;
		Iterator keySet = getIndices().keySet().iterator();
		// NBA104 deleted code
		String role = aNbaOinkRequest.getQualifier(); //begin nba072
		// NBA104 deleted code
		// begin NBA104
		if (role.equalsIgnoreCase(BENEFIT)) {
			aNbaOinkRequest.addValue(getCovOptions().size());
		} else if (role.equalsIgnoreCase(NON_RIDER_COV)) {
			//begin NBA100
			if (getNbaTXLife().isAnnuity()) {
				aNbaOinkRequest.addValue(1);
			} else {
				aNbaOinkRequest.addValue(getNonRider().size());
			}
			//end NBA100
		} else if (role.equalsIgnoreCase(RIDER)) {
			aNbaOinkRequest.addValue(getRider().size());
		} else { //end nba072
			while (keySet.hasNext()) {
				String aKey = (String) keySet.next();
				if (role == null) {
					count++;
				} else if (aKey.startsWith(role)) {
					count++;
				}
			}
			aNbaOinkRequest.addValue(count);
		}
		// end NBA104
	}

	/**
	 * Obtain the value for CreationDate. OLifE.SourceInfo.CreationDate is Source creation date
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveCreationDate(NbaOinkRequest aNbaOinkRequest) {
		if (oLifE.hasSourceInfo() && oLifE.getSourceInfo().hasCreationDate()) {
			aNbaOinkRequest.addValue(oLifE.getSourceInfo().getCreationDate());
		} else {
			aNbaOinkRequest.addUnknownValue(new Date());
		}
	}

	/**
	 * Obtain the value for CreationTime. OLifE.SourceInfo.CreationTimeis Source creation time
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveCreationTime(NbaOinkRequest aNbaOinkRequest) {
		if (oLifE.hasSourceInfo() && oLifE.getSourceInfo().hasCreationTime()) {
			aNbaOinkRequest.addValue(oLifE.getSourceInfo().getCreationTime());
		} else {
			aNbaOinkRequest.addUnknownValue(new NbaTime());
		}
	}

	/**
	 * Obtain the value for CreditCardType. OLifE.Holding.Banking.CreditCardType is credit card type in the case that the PaymentMethod is 'credit
	 * card billing,' or banking object represents a credit card payment.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveCreditCardType(NbaOinkRequest aNbaOinkRequest) {
		//NBA041 deleted
		//NBA041 begin

		//NBA093 beging
		//begin NBA115
		long cardType;
		Banking banking = null;
		List bankingList = getBankingList(aNbaOinkRequest);
		int bankingCount = bankingList.size();
		int requestCount = aNbaOinkRequest.getCount();
		for (int i = 0; i < requestCount; i++) {
			cardType = -1L;
			if (i < bankingCount) {
				banking = (Banking) bankingList.get(i);
				if (banking.hasCreditCardType()) {
					cardType = banking.getCreditCardType();
				}
			}
			aNbaOinkRequest.addValue(cardType, NbaTableConstants.NBA_PAYMENT_CREDITCARDTYPE);
		}
		//end NBA115
		//NBA093 end
		//NBA041 end
	}

	/**
	 * Obtain the value for CurrentAmt. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.Coverage.CurrentAmt is the amount of coverage -- the
	 * face amount of the rider without options.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveCurrentAmt(NbaOinkRequest aNbaOinkRequest) {
		//begin NBA100
		List coverages = getCoveragesOrRiders(aNbaOinkRequest);
		int next = 0;
		Coverage coverage;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			if (coverage.hasCurrentAmt()) {
				aNbaOinkRequest.addValue(coverage.getCurrentAmt(), FORMAT_TYPE_CURRENCY);
			} else {
				aNbaOinkRequest.addValue(0.0);
			}
		}
		//end NBA100
	}

	/**
	 * Obtain the value for CWAAmtFinActivity, an instance of FinActivityGrossAmt. OLifE.Holding.Policy.FinancialActivity.FinActivityGrossAmt is the
	 * amount amount of payment that accompanied application.
	 * @param aNbaOinkRequest - data request container
	 * @deprecated this method will be removed in a future release. Use {@link #retrieveFinActivityGrossAmt(NbaOinkRequest)}
	 */
	public void retrieveCWAAmtFinActivity(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			//NBA093 begin
			FinancialActivity activity = getFinancialActivity(i);
			if (activity != null) {
				aNbaOinkRequest.addValue(activity.getFinActivityGrossAmt(), FORMAT_TYPE_CURRENCY);
			} else {
				break;
			}
			//NBA093 end
		}
	}

	/**
	 * Obtain the value for Objective OLifE.Holding.Intent.IntentExtension.Objective investment objective.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA059 new method
	public void retrieveObjective(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				String partyId = party.getId();

				Intent intent = getIntent(i, partyId);
				if (intent != null) {
					// NBA093 deleted 5 lines
					if (intent.hasObjective()) { //NBA093
						aNbaOinkRequest.addValue(intent.getObjective(), NbaTableConstants.OLI_LU_INVESTOBJ); //NBA093
					} else { //NBA093
						aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_INVESTOBJ); //NBA093
					} //NBA093
					// NBA093 deleted line
				} else {
					break;
				}
			} else {
				break;
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_INVESTOBJ);
		}
	}

	/**
	 * Obtain the value for DeathBenefitOptType. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.Coverage.DeathBenefitOptType is the option
	 * chosen for this contract which would affect the death proceeds, i.e. increasing, level.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveDeathBenefitOptType(NbaOinkRequest aNbaOinkRequest) {
		Life aLife = getLife();
		if (aLife != null) {
			for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
				int covIndx = getCoverageForParty(aNbaOinkRequest, i);
				if (covIndx == -1) {
					break;
				} else {
					Coverage aCoverage = aLife.getCoverageAt(covIndx); // SPR3290
					aNbaOinkRequest.addValue(aCoverage.getDeathBenefitOptType(), NbaTableConstants.OLI_LU_DTHBENETYPE);
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_DTHBENETYPE);
		}
	}

	/**
	 * Obtain the value for FinActivityGrossAmt. OLifE.Holding.Policy.FinancialActivity.FinActivityGrossAmt is the amount of payment that accompanied
	 * application.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA093 NEW METHOD
	public void retrieveFinActivityGrossAmt(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			FinancialActivity activity = getFinancialActivity(i);
			if (activity != null) {
				aNbaOinkRequest.addValue(activity.getFinActivityGrossAmt(), FORMAT_TYPE_CURRENCY);
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for DivType. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.DivType is the Dividend Option.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveDivType(NbaOinkRequest aNbaOinkRequest) {
		Life life = getLife();
		if (life != null) {
			aNbaOinkRequest.addValue(life.getDivType(), NbaTableConstants.OLI_LU_DIVTYPE);
		} else {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_DIVTYPE);
		}
	}

	/**
	 * Obtain the value for DriverLicNum. OLifE.Party.PersonOrOrganization.Person.DriversLicenseNum is a string representing the drivers license
	 * number of the person.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveDriverLicNum(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				aNbaOinkRequest.addValue(person.getDriversLicenseNum());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for DriverLicState. OLifE.Party.PersonOrOrganization.Person.DriverLicState is a state in which the drivers license was issued.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveDriverLicState(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				aNbaOinkRequest.addValue(person.getDriversLicenseState(), NbaTableConstants.NBA_STATES);
			} else {
				break;
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_STATES);
		}
	}

	/**
	 * Obtain the value for EmployerName. OLifE.Party.Employment.EmployerName.
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.32 Modified
	public void retrieveEmployerName(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null && party.getEmployment().size() > 0) {
				Employment employment = party.getEmploymentAt(0);
				if (employment != null) {
					aNbaOinkRequest.addValue(employment.getEmployerName());
				}
			}
		}
	}

	/**
	 * Obtain the value for EstNetWorth. OLifE.Party.EstNetWorth is the estimated net-worth as of date record was created.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveEstNetWorth(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				aNbaOinkRequest.addValue(aParty.getEstNetWorth(), FORMAT_TYPE_CURRENCY);
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for EstSalary. OLifE.Party.PersonOrOrganization.Person.EstSalary is defined as earning subject to FICA including salary, tips,
	 * bonuses, self-employment income, other employment income, and net earned business income. All income is before qualified retirement plan
	 * contributions (401K, etc.).
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveEstSalary(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person aPerson = getPerson(aNbaOinkRequest, i);
			if (aPerson != null) {
				aNbaOinkRequest.addValue(aPerson.getEstSalary(), FORMAT_TYPE_CURRENCY);
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for Exchange1035 A value of true is set if any of the values for OLifE.Holding.Policy.FinancialActivity.FinActivityType are
	 * OLI_FINACT_1035INIT.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveExchange1035(NbaOinkRequest aNbaOinkRequest) {
		boolean found = false;
		int i = 0;
		while (true) {
			FinancialActivity activity = getFinancialActivity(i++);
			if (activity == null) {
				break;
			} else {
				//NBA093 begin
				if (activity.getFinActivityType() == NbaOliConstants.OLI_FINACT_1035INIT || activity.getFinActivityType() == NbaOliConstants.OLI_FINACT_ROLLOVEREXT1035 || activity.getFinActivityType() == NbaOliConstants.OLI_FINACT_1035SUBS) { // ALII1113
					found = true;
					break;
				}
				//NBA093 end
			}
		}

		aNbaOinkRequest.addValue(found);
	}

	/**
	 * Obtain the value for FaceAmt. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.FaceAmt is the Base Policy Face Amount.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveFaceAmt(NbaOinkRequest aNbaOinkRequest) {
		Life life = getLife();
		if (life != null) {
			aNbaOinkRequest.addValue(life.getFaceAmt(), FORMAT_TYPE_CURRENCY);
		} else {
			aNbaOinkRequest.addValue(Double.NaN, FORMAT_TYPE_CURRENCY);
		}
	}

	/**
	 * Obtain the value for FirstName. OLifE.Party.PersonOrOrganization.Person.FirstName is the first name of the person
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveFirstName(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				aNbaOinkRequest.addValue(NbaUtils.convertStringInProperCase(person.getFirstName()));//AXAL3.7.13I
			} else {
				break;
			}
		}
	}

	//P2AXAL053 new method
	public void retrieveFirstNameX(NbaOinkRequest aNbaOinkRequest) {
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		Party party = null;
		aNbaOinkRequest.setParseMultiple(true);
		for (int i = 0; i < partyList.size() ; i++) { 
			party = (Party)partyList.get(i);
			if(party!= null && party.hasPersonOrOrganization()){
				Person person = party.getPersonOrOrganization().getPerson();
				if (person != null) {
					aNbaOinkRequest.addValue(NbaUtils.convertStringInProperCase(person.getFirstName()));
				}
			}
		}
	}
	
	/**
	 * Obtain the value for FullName. OLifE.Party.FullName - When on Party, Client applications should treat FullName as Read-only when
	 * Party.Type='Person' and read/write for all other types. In the case where it is Read-Only, the server will construct the FullName property, and
	 * update it immediately whenever one of those dependent properties is set. When Party.Type='Person', Fullname is formatted '%L, %F %M, %S' where
	 * %L is LastName, %F is FirstName, %M is MiddleName and %S is Suffix.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveFullName(NbaOinkRequest aNbaOinkRequest) {
		String fullName;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			fullName = "";
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				fullName = aParty.getFullName();
				if (fullName == null || fullName.length() < 1) {
					Object obj = aParty.getPersonOrOrganization$Contents();
					if ((obj != null) && (obj instanceof Person)) {
						Person aPerson = aParty.getPersonOrOrganization().getPerson();
						StringBuffer sb = new StringBuffer();
						if (aPerson.hasLastName()) {
							sb.append(NbaUtils.convertStringInProperCase(aPerson.getLastName()));//AXAL3.7.13I
							sb.append(", ");
						}
						if (aPerson.hasFirstName()) {
							sb.append(NbaUtils.convertStringInProperCase(aPerson.getFirstName()));//AXAL3.7.13I
							sb.append(" ");
						}
						if (aPerson.hasMiddleName()) {
							sb.append(NbaUtils.convertStringInProperCase(aPerson.getMiddleName()));//AXAL3.7.13I
							sb.append(" ");
						}
						if (aPerson.hasSuffix() && aPerson.getSuffix().length() > 0) {
							sb.append(", ");
							sb.append(aPerson.getSuffix());
						}
						fullName = sb.toString();
					}
				}
			} else {
				break;
			}
			aNbaOinkRequest.addValue(fullName);
		}
	}

	/**
	 * Obtain the value for Gender. OLifE.Party.PersonOrOrganization.Person.Gender is the gender of the person
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveGender(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				aNbaOinkRequest.addValue(person.getGender(), NbaTableConstants.OLI_LU_GENDER);
			} else {
				break;
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_GENDER);
		}
	}

	/**
	 * Obtain the value for GovtID. OLifE.Party.GovtID is the string that represents the government ID. In the USA, this is the Social Security
	 * Number. In South Africa, this is the TaxReferenceNumber which represents the receiver of revenue for both a person or organization. In
	 * Australia - for a person, this field is considered PRIVATE, UNSHARABLE INFORMATION and thus it is not applicable. This is due to government
	 * security requirements surrounding the privatization of the field in Australia. In Australia - for an organization, one of three codes can be
	 * use 'ACN' or 'ARBN' or 'SIS'. A company typically will only have one.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveGovtID(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				aNbaOinkRequest.addValue(aParty.getGovtID(), FORMAT_TYPE_SSN);
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for GovtIDTc. OLifE.Party.GovtIDTc is the Type code describing the contents of GovtID
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveGovtIDTc(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				aNbaOinkRequest.addValue(aParty.getGovtIDTC(), NbaTableConstants.OLI_LU_GOVTIDTC); //NBA093
			} else {
				break;
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_GOVTIDTC);
		}
	}

	/**
	 * Obtain the value for PartyID. OLifE.Party.ID identifies the Party
	 * @param aNbaOinkRequest - data request container
	 */
	//ACN010 NEW METHOD
	public void retrievePartyID(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				aNbaOinkRequest.addValue(aParty.getId());
			} else {
				break;
			}
		}
	}

	/**
	 * Return true if the value for GovtIDTc is 1. OLifE.Party.GovtIDTc is the Type code describing the contents of GovtID
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveGovtIDTc1(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				aNbaOinkRequest.addValue(aParty.getGovtIDTC() == NbaOliConstants.OLI_GOVTID_SSN); //NBA093
			} else {
				break;
			}
		}
	}

	/**
	 * Return true if the value for GovtIDTc is 2. OLifE.Party.GovtIDTc is the Type code describing the contents of GovtID
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveGovtIDTc2(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				aNbaOinkRequest.addValue(aParty.getGovtIDTC() == NbaOliConstants.OLI_GOVTID_TID); //NBA093
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for Height. OLifE.Party.PersonOrOrganization.Person.Height is the height of the person in Centimeters.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveHeight(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				//begin NBA093
				if (person.getHeight2() != null && person.getHeight2().hasMeasureValue()) {
                	// begin AXAL3.7.07
                	if (person.getHeight2().getMeasureUnits() == NbaOliConstants.OLI_MEASURE_USSECOND) {
                		aNbaOinkRequest.addValue(String.valueOf(person.getHeight2().getMeasureValue())); //ALII1730
                	}
                	else {
                		aNbaOinkRequest.addValue(Math.round(person.getHeight2().getMeasureValue()));
                	}
                	// end AXAL3.7.07
				}
				//end NBA093
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for HeightInd. OLifE.Party.PersonOrOrganization.Person.PersonExtension.HeightInd identifies the unit of measure for height
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveHeightInd(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			//NBA093 begin
			// SPR3290 code deleted
			if (person != null) {
				if (person.getHeight2() != null) { //NBA093
					aNbaOinkRequest.addValue(person.getHeight2().getMeasureUnits()); //NBA093
				}
			} else {
				break;
			}
			//NBA093 end
		}
	}

	/**
	 * Obtain the value for HomCity, an instance of City for an Address with type code = 1. OLifE.Party.Address.City is the city of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveHomCity(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_HOME);
				if (address == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(address.getCity());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for HomEmail, an instance of AddrLine for an EMailAddress with type code = 2. OLifE.Party.EMailAddress.AddrLine is the string
	 * representing complete, mailable e-mail address. This is correctly defined as the 'SMTP' address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveHomEmail(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				EMailAddress anEMailAddress = getEMailForType(aParty, NbaOliConstants.OLI_EMAIL_PERSONAL);
				if (anEMailAddress == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(anEMailAddress.getAddrLine());
				}
			} else {
				break;
			}
		}
	}
	
	

	/**
	 * Obtain the value for HomLine1, an instance of Line1 for an Address with type code = 1. OLifE.Party.Address.Line1 is the first line of the
	 * address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveHomLine1(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_HOME);
				if (address == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(address.getLine1());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for HomLine2, an instance of Line2 for an Address with type code = 1. OLifE.Party.Address.Line2 is the second line of the
	 * address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveHomLine2(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_HOME);
				if (address == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(address.getLine2());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for HomLine3, an instance of Line3 for an Address with type code = 1. OLifE.Party.Address.Line3 is the third line of the
	 * address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveHomLine3(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_HOME);
				if (address == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(address.getLine3());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for HomPhone, a concatenation of OLifE.Party.Phone.AreaCode and OLifE.Party.Phone.DialNumber for an Phone with type code = 1.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveHomPhone(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Phone phone = getPhoneForType(aParty, NbaOliConstants.OLI_PHONETYPE_HOME);
				if (phone == null) {
					aNbaOinkRequest.addValue("", FORMAT_TYPE_PHONE);
				} else {
					StringBuffer phoneNumber = new StringBuffer();
					if (phone.hasAreaCode()) {
						phoneNumber.append(phone.getAreaCode());
					}
					if (phone.hasDialNumber()) {
						phoneNumber.append(phone.getDialNumber());
					}
					aNbaOinkRequest.addValue(phoneNumber.toString(), FORMAT_TYPE_PHONE);
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for HomState, an instance of AddressStateTc for an Address with type code = 1. OLifE.Party.Address.AddressStateTc is the
	 * address state
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveHomState(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_HOME);
				if (address == null) {
					aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_STATES);
				} else {
					aNbaOinkRequest.addValue(address.getAddressStateTC(), NbaTableConstants.NBA_STATES); //NBA093
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_STATES);
		}
	}
	

	/**
	 * Obtain the value for HomZip, an instance of Zip for an Address with type code = 1. OLifE.Party.Address.Zip is the zip code, postal code, etc.
	 * (country dependent)
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveHomZip(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_HOME);
				if (address == null) {
					aNbaOinkRequest.addValue("", FORMAT_TYPE_ZIP);
				} else {
					aNbaOinkRequest.addValue(address.getZip(), FORMAT_TYPE_ZIP);
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for HOUnderwriterName. OLifE.Holding.Policy.ApplicationInfo.HOUnderwriterName is used to store the HOME Offive Underwriter
	 * name
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveHOUnderwriterName(NbaOinkRequest aNbaOinkRequest) {
		if (getApplicationInfo() != null) {
			aNbaOinkRequest.addValue(getApplicationInfo().getHOUnderwriterName());
		} else {
			aNbaOinkRequest.addValue("");
		}
	}

	/**
	 * Obtain the value for IncomeOption. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Annuity.Payout.IncomeOption is the Income option. Used
	 * for life annuity.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveIncomeOption(NbaOinkRequest aNbaOinkRequest) {
		boolean found = false;
		if (getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() != null) { //NBA093
			if (getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isAnnuity()) { //NBA093
				Payout aPayout = getPayout(0);
				if (aPayout != null) {
					aNbaOinkRequest.addValue(aPayout.getIncomeOption(), NbaTableConstants.OLI_LU_INCOPTION);
					found = true;
				}
			} else {
			}
		}
		if (!found) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_INCOPTION);
		}
	}

	/**
	 * Obtain the value for InitialPremAmt. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.InitialPremAmt is the Initial Premium Amount.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveInitialPremAmt(NbaOinkRequest aNbaOinkRequest) {
		Life life = getLife();
		if (life != null) {
			aNbaOinkRequest.addValue(life.getInitialPremAmt(), FORMAT_TYPE_CURRENCY);
		} else {
			aNbaOinkRequest.addValue(Double.NaN, FORMAT_TYPE_CURRENCY);
		}
	}

	/**
	 * Obtain the value for InitPaymentAmt. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Annuity.InitPaymentAmt is the Initial Premium Amount.
	 * It is applicable only to Annuity products.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveInitPaymentAmt(NbaOinkRequest aNbaOinkRequest) {
		Annuity annuity = getAnnuity();
		if (annuity != null && annuity.hasInitPaymentAmt()) {
			aNbaOinkRequest.addValue(annuity.getInitPaymentAmt(), FORMAT_TYPE_CURRENCY);
		} else {
			aNbaOinkRequest.addValue(Double.NaN, FORMAT_TYPE_CURRENCY);
		}
	}

	/**
	 * Obtain the value for InterestPercent. OLifE.Relation.InterestPercent is the percent of interest the related object has in contract. It is used
	 * for % commission split for agent; percentage of benefits to be received by beneficiary, ownership percentage - company, holding, etc.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveInterestPercent(NbaOinkRequest aNbaOinkRequest) {
		String origPartyId = null;
		String roleType = aNbaOinkRequest.getQualifier();
		if (roleType.equals(PARTY_COBENEFICIARY) || roleType.equals(PARTY_BENEFICIARY)) {
			//begin SPR1335
			if (getOLifE().getSourceInfo().getFileControlID().equalsIgnoreCase(NbaConstants.SYST_VANTAGE)) {
				if (productType.equals(PRODUCT_ANNUITY)) {
					origPartyId = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE()).getId();
				} else {
					origPartyId = getCoverage(NbaOliConstants.OLI_COVIND_BASE).getId();
				}
			} else {
				origPartyId = getPartyForPrimaryIns().getId();
			}

		} else { //end SPR1335
			origPartyId = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE()).getId(); //NBA044
		}
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party relatedParty = getParty(aNbaOinkRequest, i);
			if (relatedParty == null) {
				return;
			} else {
				Relation relation = getRelation(roleType, i, origPartyId, relatedParty.getId());
				if (relation != null && relation.hasInterestPercent()) {
					aNbaOinkRequest.addValue(relation.getInterestPercent());

				}
			}
		}

	}

	/**
	 * Obtain the value for IssueAge. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.Coverage.LifeParticipant.IssueAge is the age of
	 * participant when coverage was issued.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveIssueAge(NbaOinkRequest aNbaOinkRequest) {
		//begin NBA100
		int issueAge = -1;
		if (getNbaTXLife().isAnnuity()) {
			Participant participant = getNbaTXLife().getPrimaryAnnuitantParticipant();
			if (participant != null) {
				issueAge = participant.getIssueAge();
			}
			aNbaOinkRequest.addValue(issueAge);
		} else if (BENEFIT.equals(aNbaOinkRequest.getQualifier())) {
			Life life = getLife();
			if (life != null) {
				int sizeCoverage = life.getCoverageCount();
				Coverage coverage;
				LifeParticipant lifeParticipant;
				CovOption covOption;
				for (int i = 0; i < sizeCoverage; i++) {
					coverage = life.getCoverageAt(i);
					lifeParticipant = NbaUtils.getInsurableLifeParticipant(coverage);
					issueAge = -1;
					if (lifeParticipant != null) {
						issueAge = lifeParticipant.getIssueAge();
					}
					int sizeCovOption = coverage.getCovOptionCount();
					for (int j = 0; j < sizeCovOption; j++) {
						covOption = coverage.getCovOptionAt(j);
						if (!covOption.isActionDelete()) {
							aNbaOinkRequest.addValue(issueAge);
						}
					}
				}
			}
		} else {
			List lifeParticipants = getInsurableLifeParticipants(aNbaOinkRequest);
			int next = 0;
			LifeParticipant lifeParticipant;
			while ((lifeParticipant = getNextLifeParticipant(aNbaOinkRequest, lifeParticipants, next++)) != null) {
				if (lifeParticipant.hasIssueAge()) {
					aNbaOinkRequest.addValue(lifeParticipant.getIssueAge());
				} else {
					aNbaOinkRequest.addValue(0);
				}
			}
		}
		//end NBA100
	}

	/**
	 * Obtain the value for IssueCompanyCode. OLifE.Holding.Policy.PolicyExtension.IssueCompanyCode
	 * @param aNbaOinkRequest
	 */
	public void retrieveIssueCompanyCode(NbaOinkRequest aNbaOinkRequest) {
		int index_Extension = getExtensionIndex(getPolicy().getOLifEExtension(), POLICY_EXTN);
		if (index_Extension != -1) {
			OLifEExtension oli = getPolicy().getOLifEExtensionAt(index_Extension);
			if (oli != null) {
				aNbaOinkRequest.addValue(oli.getPolicyExtension().getIssueCompanyCode(), NbaTableConstants.NBA_COMPANY);
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_COMPANY);
		}
	}

	/**
	 * Obtain the value for IssueState, an instance of Jurisdiction. OLifE.Holding.Policy.Jurisdiction is the state (jurisdiction) of issue of the
	 * policy
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveIssueState(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(getPolicy().getJurisdiction(), NbaTableConstants.NBA_STATES);
	}

	/**
	 * Obtain the value for LastName. OLifE.Party.PersonOrOrganization.Person.LastName is the last name of the person
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveLastName(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				//NBLXA-2397 BEGINS
				String lastName = person.getLastName();
				String[] lastNameSplt = lastName.split(",", 2);
				if(lastNameSplt.length == 2){
					aNbaOinkRequest.addValue(NbaUtils.convertStringInProperCase(lastNameSplt[0])+","+lastNameSplt[1]); //AXAL3.7.13I
				}else{
					aNbaOinkRequest.addValue(NbaUtils.convertStringInProperCase(lastName)); //AXAL3.7.13I	
				}
				//NBLXA-2397 ENDS
			} else {
				break;
			}
		}
	}

	public void retrieveLastNameX(NbaOinkRequest aNbaOinkRequest) {
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		Party party = null;
		aNbaOinkRequest.setParseMultiple(true);
		for (int i = 0; i < partyList.size() ; i++) { 
			party = (Party)partyList.get(i);
			if(party!= null && party.hasPersonOrOrganization()){
				Person person = party.getPersonOrOrganization().getPerson();
				if (person != null) {
					aNbaOinkRequest.addValue(NbaUtils.convertStringInProperCase(person.getLastName()));
				}
			}
		}
	}
	
	/**
	 * Return the count of multiple assignee parties (Party with relation role code = 145 and 250)
	 * @param aNbaOinkRequest
	 */
	//Defect ALII753
	public void retrieveMultipleASGPartyCount(NbaOinkRequest aNbaOinkRequest) {
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		if(partyList != null){
			aNbaOinkRequest.addValue(partyList.size());
		}
	}
	
	/**
	 * Obtain the value for LifeCovTypeCode. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.Coverage.LifeCovTypeCode is the type of
	 * coverage.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveLifeCovTypeCode(NbaOinkRequest aNbaOinkRequest) {
		//begin NBA100
		List coverages = getCoveragesOrRiders(aNbaOinkRequest);
		Coverage coverage = null;
		if (coverages != null) {
			int next = 0;
			while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
				if (coverage.hasLifeCovTypeCode()) {
					aNbaOinkRequest.addValue(coverage.getLifeCovTypeCode());
				} else {
					aNbaOinkRequest.addValue(-1);
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1);
		}
		//end NBA100
	}

	/**
	 * Obtain the value for LineOfBusiness. OLifE.Holding.Policy.LineOfBusiness is the Line of business of the insurance.
	 * @param aNbaOinkRequest
	 */
	public void retrieveLineOfBusiness(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(getPolicy().getLineOfBusiness()); // OLI_LU_LINEBUS??
	}

	/**
	 * Obtain the value for MarStat. OLifE.Party.PersonOrOrganization.Person.MarStat is the maritial of the person
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveMarStat(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				aNbaOinkRequest.addValue(person.getMarStat(), NbaTableConstants.OLI_LU_MARSTAT);
			} else {
				break;
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_MARSTAT);
		}
	}

	/**
	 * Obtain the value for MedCondType, an instance of MedCondReponse. OLifE.Party.Risk.MedicalCondition.MedicalConditionExtension.MedCondReponse is
	 * the response associated with a MedicalCondition which has a ConditionType which matches the value of the variable identifier name (excluding
	 * the "MedCondType" string). For example, if the variable name is MedCondType123, the respose for the MedicalCondition which has a ConditionType
	 * of "123" is returned.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveMedCondType(NbaOinkRequest aNbaOinkRequest) {
		int startPos = "MEDCONDTYPE".length();
		String tagName = aNbaOinkRequest.getRootVariable();
		long medicalConditionType = Long.parseLong(tagName.substring(startPos, tagName.length()));
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				boolean found = false;
				boolean aResponse = false;
				Risk aRisk = getRisk(party);
				if (aRisk != null) {
					MedicalCondition condition = getMedicalCondition(aRisk, medicalConditionType);
					if (condition != null) {
						int index_extension = getExtensionIndex(condition.getOLifEExtension(), MEDICALCONDITION_EXTN);
						if (index_extension > -1) {
							OLifEExtension oli = condition.getOLifEExtensionAt(index_extension);
							aResponse = oli.getMedicalConditionExtension().getMedCondResponse();
							if (aResponse == true) {  //AXAL3.7.3M1, only recognize the condition if response is true
								found = true;
							}
						}
					}
				}
				if (found) {
					aNbaOinkRequest.addValue(aResponse);
				} else {
					try {
						//AXAL3.7.3M1 code deleted, in order for additional insureds to be considered in appsub (by contract, not by party)
					} catch (Exception e) {
					}
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for MiddleName(. OLifE.Party.PersonOrOrganization.Person.MiddleName is the middle name of the person
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveMiddleName(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				aNbaOinkRequest.addValue(NbaUtils.convertStringInProperCase(person.getMiddleName())); //AXAL3.7.13I
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for Occupation. OLifE.Party.PersonOrOrganization.Person.Occupation is the occupation for the person.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveOccupation(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				aNbaOinkRequest.addValue(person.getOccupation(), NbaTableConstants.NBA_OCCUPATION);
			} else {
				break;
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_OCCUPATION);
		}
	}

	/**
	 * Obtain the value for OffCity, an instance of City for an Address with type code = 15  or 2 in case of SI. OLifE.Party.Address.City is the city of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveOffCity(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = null;
				if(nbaTXLife.isSIApplication()){
					address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_BUS); //APSL3066(QC11434) Send Business address for SI App instead of individual work location address
				}else{
					address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_INDVWORKLOC);
				}
				//Begin APSL3447
				if (address == null) {
					address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_BUS);
				}
				//End APSL3447
				if (address == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(address.getCity());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for OffEmail, an instance of AddrLine for an EMailAddress with type code = 1. OLifE.Party.EMailAddress.AddrLine is the string
	 * representing complete, mailable e-mail address. This is correctly defined as the 'SMTP' address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveOffEmail(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				EMailAddress anEMailAddress = getEMailForType(aParty, NbaOliConstants.OLI_EMAIL_BUSINESS);
				if (anEMailAddress == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(anEMailAddress.getAddrLine());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for OffLine1, an instance of Line1 for an Address with type code = 15  or 2 in case of SI. OLifE.Party.Address.Line1 is the first line of the
	 * address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveOffLine1(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = null;
				if(nbaTXLife.isSIApplication()){
					address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_BUS); //APSL3066(QC11434) Send Business address for SI App instead of individual work location address
				}else{
					address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_INDVWORKLOC);
				}
				//Begin APSL3447
				if (address == null) {
					address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_BUS);
				}
				//End APSL3447
				if (address == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(address.getLine1());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for OffLine2, an instance of Line2 for an Address with type code = 15  or 2 in case of SI. OLifE.Party.Address.Line2 is the second line of the
	 * address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveOffLine2(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = null;
				if(nbaTXLife.isSIApplication()){
					address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_BUS); //APSL3066(QC11434) Send Business address for SI App instead of individual work location address
				}else{
					address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_INDVWORKLOC);
				}
				//Begin APSL3447
				if (address == null) {
					address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_BUS);
				}
				//End APSL3447
				if (address == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(address.getLine2());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for OffLine3, an instance of Line3 for an Address with type code = 15 or 2 in case of SI. OLifE.Party.Address.Line3 is the third line of the
	 * address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveOffLine3(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = null;
				if(nbaTXLife.isSIApplication()){
					address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_BUS); //APSL3066(QC11434) Send Business address for SI App instead of individual work location address
				}else{
					address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_INDVWORKLOC);
				}
				//Begin APSL3447
				if (address == null) {
					address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_BUS);
				}
				//End APSL3447
				if (address == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(address.getLine3());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for OffPhone, a concatenation of OLifE.Party.Phone.AreaCode and OLifE.Party.Phone.DialNumber for an Phone with type code = 2.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveOffPhone(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Phone phone = getPhoneForType(aParty, NbaOliConstants.OLI_PHONETYPE_BUS);
				if (phone == null) {
					aNbaOinkRequest.addValue("", FORMAT_TYPE_PHONE);
				} else {
					StringBuffer phoneNumber = new StringBuffer();
					if (phone.hasAreaCode()) {
						phoneNumber.append(phone.getAreaCode());
					}
					if (phone.hasDialNumber()) {
						phoneNumber.append(phone.getDialNumber());
					}
					aNbaOinkRequest.addValue(phoneNumber.toString(), FORMAT_TYPE_PHONE);
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for OffState, an instance of AddressStateTc for an Address with type code = 15  or 2 in case of SI. OLifE.Party.Address.AddressStateTc is the
	 * address state
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveOffState(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = null;
				if(nbaTXLife.isSIApplication()){
					address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_BUS); //APSL3066(QC11434) Send Business address for SI App instead of individual work location address
				}else{
					address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_INDVWORKLOC);
				}
				//Begin APSL3447
				if (address == null) {
					address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_BUS);
				}
				//End APSL3447
				if (address == null) {
					aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_STATES);
				} else {
					aNbaOinkRequest.addValue(address.getAddressStateTC(), NbaTableConstants.NBA_STATES); //NBA093
				}
			} else {
				break;
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_STATES);
		}
	}

	/**
	 * Obtain the value for OffZip, an instance of Zip for an Address with type code = 15 or 2 in case of SI. OLifE.Party.Address.Zip is the zip code, postal code, etc.
	 * (country dependent)
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveOffZip(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = null;
				if(nbaTXLife.isSIApplication()){
					address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_BUS); //APSL3066(QC11434) Send Business address for SI App instead of individual work location address
				}else{
					address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_INDVWORKLOC);
				}
				//Begin APSL3447
				if (address == null) {
					address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_BUS);
				}
				//End APSL3447
				if (address == null) {
					aNbaOinkRequest.addValue("", FORMAT_TYPE_ZIP);
				} else {
					aNbaOinkRequest.addValue(address.getZip(), FORMAT_TYPE_ZIP);
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for OrganizationKey. OLifE.Party.PersonOrOrganization.Organization.OrganizationKey is the unique Key for Organization.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveOrganizationKey(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Organization anOrganization = getOrganization(aNbaOinkRequest, i);
			if (anOrganization != null) {
				aNbaOinkRequest.addValue(anOrganization.getOrganizationKey());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for OrgCode. OLifE.Party.PersonOrOrganization.Organization.OrgCode is the code for Organization.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveOrgCode(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Organization anOrganization = getOrganization(aNbaOinkRequest, i);
			if (anOrganization != null) {
				aNbaOinkRequest.addValue(anOrganization.getOrgCode());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for PaymentAmt. OLifE.Holding.Policy.PaymentAmt is the current modal payment/premium amount. This is the amount for the
	 * overall policy, including any premiums associated with coverages/riders/options.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrievePaymentAmt(NbaOinkRequest aNbaOinkRequest) {
		if (getPolicy().hasPaymentAmt()) {
			aNbaOinkRequest.addValue(getPolicy().getPaymentAmt(), FORMAT_TYPE_CURRENCY);
			return;
		}
		aNbaOinkRequest.addValue("0");
	}

	/**
	 * Obtain the value for PaymentMethod. OLifE.Holding.Policy.PaymentMethod is the payment method.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrievePaymentMethod(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(getPolicy().getPaymentMethod(), NbaTableConstants.OLI_LU_PAYMETHOD);
	}

	/**
	 * Obtain the value for PaymentMode. OLifE.Holding.Policy.PaymentMode is the frequency of payment - monthly, quarterly, or annually, etc.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrievePaymentMode(NbaOinkRequest aNbaOinkRequest) {
		//begin NBA100
		if (getPolicy().hasPaymentMode()) {
			aNbaOinkRequest.addValue(getPolicy().getPaymentMode(), NbaTableConstants.OLI_LU_PAYMODE);
		} else {
			aNbaOinkRequest.addValue(NbaOliConstants.OLI_PAYMODE_NONE, NbaTableConstants.OLI_LU_PAYMODE);
		}
		//end NBA100
	}

	//APSL2735 method deleted

	/**
	 * Obtain the value for PolNumber. OLifE.Holding.Policy.PolNumber is the account number for the contract.
	 * @param aNbaOinkRequest
	 */
	public void retrievePolNumber(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(getPolicy().getPolNumber());
	}

	/**
	 * Obtain the value for PrefCity, an instance of City for the preferred Address. OLifE.Party.Address.City is the city of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrievePrefCity(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getPreferredAddress(aParty);
				if (address == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(address.getCity());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for Prefix. OLifE.Party.PersonOrOrganization.Person.Prefix is the prefix for the person
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrievePrefix(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				aNbaOinkRequest.addValue(person.getPrefix());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for PrefLine1, an instance of Line1 for the preferred Address. OLifE.Party.Address.Line1 is the first line of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrievePrefLine1(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getPreferredAddress(aParty);
				if (address == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(address.getLine1());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for PrefLine2, an instance of Line2 for the preferred Address. OLifE.Party.Address.Line2 is the first line of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrievePrefLine2(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getPreferredAddress(aParty);
				if (address == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(address.getLine2());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for PrefLine3, an instance of Line3 for the preferred Address. OLifE.Party.Address.Line3 is the first line of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrievePrefLine3(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getPreferredAddress(aParty);
				if (address == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(address.getLine3());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for PrefState, an instance of AddressStateTc for the preferred Address. OLifE.Party.Address.AddressStateTc is the address
	 * state
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrievePrefState(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getPreferredAddress(aParty);
				if (address == null) {
					aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_STATES);
				} else {
					aNbaOinkRequest.addValue(address.getAddressStateTC(), NbaTableConstants.NBA_STATES); //NBA093
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_STATES);
		}
	}

	/**
	 * Obtain the value for PrefZip, an instance of Zip for the preferred Address. OLifE.Party.Address.Zip is the zip code, postal code, etc. (country
	 * dependent)
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrievePrefZip(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getPreferredAddress(aParty);
				if (address == null) {
					aNbaOinkRequest.addValue("", FORMAT_TYPE_ZIP);
				} else {
					aNbaOinkRequest.addValue(address.getZip(), FORMAT_TYPE_ZIP);
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for PrevCity, an instance of City for an Address with type code = 12. OLifE.Party.Address.City is the city of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrievePrevCity(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_PREVIOUS);
				if (address == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(address.getCity());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for PrevLine1, an instance of Line1 for an Address with type code = 12. OLifE.Party.Address.Line1 is the first line of the
	 * address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrievePrevLine1(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_PREVIOUS);
				if (address == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(address.getLine1());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for PrevLine2, an instance of Line2 for an Address with type code = 12. OLifE.Party.Address.Line2 is the second line of the
	 * address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrievePrevLine2(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_PREVIOUS);
				if (address == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(address.getLine2());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for PrevLine3, an instance of Line3 for an Address with type code = 12. OLifE.Party.Address.Line3 is the third line of the
	 * address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrievePrevLine3(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_PREVIOUS);
				if (address == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(address.getLine3());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for PrevState, an instance of AddressStateTc for an Address with type code = 12. OLifE.Party.Address.AddressStateTc is the
	 * address state
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrievePrevState(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_PREVIOUS);
				if (address == null) {
					aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_STATES);
				} else {
					aNbaOinkRequest.addValue(address.getAddressStateTC(), NbaTableConstants.NBA_STATES); //NBA093
				}
			} else {
				break;
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_STATES);
		}
	}

	/**
	 * Obtain the value for PrevZip, an instance of Zip for an Address with type code = 12. OLifE.Party.Address.Zip is the zip code, postal code, etc.
	 * (country dependent)
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrievePrevZip(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_PREVIOUS);
				if (address == null) {
					aNbaOinkRequest.addValue("", FORMAT_TYPE_ZIP);
				} else {
					aNbaOinkRequest.addValue(address.getZip(), FORMAT_TYPE_ZIP);
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for rate class. OLifE.Party.PersonOrOrganization.Person.RateClass OLifE.Holding.Policy.Life.Coverage.RateClass
	 * @param aNbaOinkRequest - data request container
	 * @deprecated this method will be removed in a future release. Use {@link #retrieveSmokerStat(NbaOinkRequest)}
	 */
	public void retrieveRateClass(NbaOinkRequest aNbaOinkRequest) {
		//begin NBA100 NBA104
		Coverage coverage;
		CoverageExtension coverageExt;
		String rateclass;
		List coverages = getCoveragesOrRiders(aNbaOinkRequest);
		int next = 0;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			rateclass = "";
			coverageExt = NbaUtils.getFirstCoverageExtension(coverage);
			if (coverageExt != null && coverageExt.hasRateClass()) {
				rateclass = coverageExt.getRateClass();
			}
			aNbaOinkRequest.addValue(rateclass, NbaTableConstants.NBA_RATECLASS);
		}
		//end NBA100 NBA104
	}	

	/**
	 * Obtain the value for smoker stat. OLifE.Party.PersonOrOrganization.Person.SmokerStat is the smoker stat of the person
	 * 
	 * @param aNbaOinkRequest -
	 *                data request container
	 */
	public void retrieveSmokerStat(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				//NBA093 Code Deleted
				aNbaOinkRequest.addValue(person.getSmokerStat());//NBA093
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for a Coverage ProductCode. Holding.Policy.Life.Coverage.ProductCode is a productCode.
	 * @param aNbaOinkRequest - data request container
	 * @deprecated - use CoverageProductCode (NBA093)
	 */
	public void retrieveProductCode(NbaOinkRequest aNbaOinkRequest) {
		// NBA104 deleted code
		retrieveCoverageProductCode(aNbaOinkRequest); //NBA093
	}

	/**
	 * Obtain the value for a Coverage ProductCode. Holding.Policy.Life.Coverage.ProductCode is a productCode.
	 * Holding.Policy.Life.Coverage.CovOption.ProductCode is a productCode.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA093 new method
	public void retrieveCoverageProductCode(NbaOinkRequest aNbaOinkRequest) {
		Life aLife = getLife();
		if (aLife == null) {
			retrievePolicyProductCode(aNbaOinkRequest);
			return; //NBA104
		}

		// begin NBA104
		int count = 0;
		Coverage coverage = null;
		CovOption covOption = null;

		String coverageType = aNbaOinkRequest.getQualifier();
		if (coverageType.equals(BENEFIT) || coverageType.equals(ACCIDENTAL_DEATH_BENEFIT)) {
			List covoptions = null;
			if (coverageType.equals(ACCIDENTAL_DEATH_BENEFIT)) {
				covoptions = getCovOptions(NbaOliConstants.OLI_OPTTYPE_ADB);
			} else {
				covoptions = getCovOptions();
			}
			if (covoptions != null) {
				count = covoptions.size();
				for (int i = 0; i < count; i++) {
					covOption = (CovOption) covoptions.get(i);
					if (covOption != null) {
						aNbaOinkRequest.addValue(covOption.getProductCode(), NbaTableConstants.NBA_PLANS);
					}
				}
			}
		} else if (coverageType.equals(NON_RIDER_COV) || coverageType.equals(RIDER)) {
			List coverages = null;
			if (coverageType.equals(NON_RIDER_COV)) {
				coverages = getNonRider();
			} else {
				coverages = getRider();
			}

			if (coverages != null) {
				count = coverages.size();
				for (int i = 0; i < count; i++) {
					coverage = (Coverage) coverages.get(i);
					if (aNbaOinkRequest.getCoverageFilter() != -1) {
						if (coverage.getLifeCovTypeCode() == aNbaOinkRequest.getCoverageFilter()) {
							aNbaOinkRequest.addValue(coverage.getProductCode(), NbaTableConstants.NBA_PLANS);
						}
					} else {
						aNbaOinkRequest.addValue(coverage.getProductCode(), NbaTableConstants.NBA_PLANS);
					}
				}
			}
			// end NBA104
		} else {
			for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
				int covIndx = getCoverageForParty(aNbaOinkRequest, i);
				if (covIndx == -1) {
					break;
				} else {
					Coverage aCoverage = aLife.getCoverageAt(covIndx); // SPR3290
					aNbaOinkRequest.addValue(aCoverage.getProductCode(), NbaTableConstants.NBA_PLANS);
				}
			}
		}
	}

	/**
	 * Obtain the value for a SubAccount ProductCode. Holding.Investment.SubAccount.ProductCode is a productCode.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA093 new method
	public void retrieveSubAccountProductCode(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			SubAccount subAccount = getSubAccount(i);
			if (subAccount == null) {
				aNbaOinkRequest.addValue("", NbaTableConstants.NBA_FUNDS);
			} else {
				aNbaOinkRequest.addValue(subAccount.getProductCode(), NbaTableConstants.NBA_FUNDS);
			}
		}
	}

	/**
	 * Obtain the value for a Policy ProductCode. OLifE.Policy.ProductCode is the product code.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA093 new method
	public void retrievePolicyProductCode(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(getPolicy().getProductCode(), NbaTableConstants.NBA_PLANS);
	}

	/**
	 * Obtain the value for ProductType. OLifE.Policy.ProductCode determines whether the underlying insurance policy is a Life, Disability Health, or
	 * an annuity.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveProductType(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(getPolicy().getProductType(), NbaTableConstants.OLI_LU_POLPROD);
	}

	/**
	 * Obtain the value for QualPlanType. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.QualPlanType or
	 * OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Annuity.QualPlanType is Qualification plan type for this life policy. Life products can be
	 * sold as Tax Qualified products.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveQualPlanType(NbaOinkRequest aNbaOinkRequest) {
		if (getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() == null) { //NBA093
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_QUALPLAN);
		} else {
			if (aNbaOinkRequest.getQualifier().equals(PRODUCT_ANNUITY)
					|| getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isAnnuity()) { //NBA093
				aNbaOinkRequest.addValue(getAnnuity().getQualPlanType(), NbaTableConstants.OLI_LU_QUALPLAN);
			} else {
				aNbaOinkRequest.addValue(getLife().getQualPlanType(), NbaTableConstants.OLI_LU_QUALPLAN);
			}
		}
	}

	/**
	 * Obtain the value for RelatedRefID. OLifE.Relation.RelatedRefID is an identifier that the 'to object' uses to identify the 'from object'. For
	 * instance, the Health insurance ID used by the insurer for the insured.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveRelatedRefID(NbaOinkRequest aNbaOinkRequest) {
		if (aNbaOinkRequest.getRelatedRefID() != NbaOinkRequest.noFilterString) {
			aNbaOinkRequest.addValue(aNbaOinkRequest.getRelatedRefID());
			//begin NBA100
		} else {
			String roleType = aNbaOinkRequest.getQualifier();
			if (BASE_COV.equalsIgnoreCase(roleType) || NON_RIDER_COV.equalsIgnoreCase(roleType) || RIDER.equalsIgnoreCase(roleType)) {
				List lifeParticipants = getInsurableLifeParticipants(aNbaOinkRequest);
				int next = 0;
				LifeParticipant lifeParticipant;
				String partyId;
				Object[] relations = getOLifE().getRelation().toArray();
				while ((lifeParticipant = getNextLifeParticipant(aNbaOinkRequest, lifeParticipants, next++)) != null) {
					String code = "";
					partyId = lifeParticipant.getPartyID();
					if (partyId != null && partyId.length() > 0) {
						Relation relation = NbaUtils.getRelationForParty(partyId, relations);
						if (relation != null) {
							code = relation.getRelatedRefID();
						}
					}
					aNbaOinkRequest.addValue(code);
				}
			} else { //handle party related qualifiers
				//end NBA100
				String origPartyId = null;
				// NBA100 code deleted
				if (roleType.equals(PARTY_COBENEFICIARY) || roleType.equals(PARTY_BENEFICIARY)) {
					//begin SPR1335
					if (getOLifE().getSourceInfo().getFileControlID().equalsIgnoreCase(NbaConstants.SYST_VANTAGE)) {
						if (productType.equals(PRODUCT_ANNUITY)) {
							origPartyId = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE()).getId();
						} else {
							origPartyId = getCoverage(NbaOliConstants.OLI_COVIND_BASE).getId();
						}
					} else {
						origPartyId = getPartyForPrimaryIns().getId();
					}
				} else { //end SPR1335
					origPartyId = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE()).getId(); //NBA044
				}

				for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
					Party relatedParty = getParty(aNbaOinkRequest, i);
					if (relatedParty == null) {
						break;
					} else {
						Relation relation = getRelation(roleType, i, origPartyId, relatedParty.getId());
						if (relation != null) {
							aNbaOinkRequest.addValue(relation.getRelatedRefID());
						} else {
							aNbaOinkRequest.addValue("");
						}
					}
				}
			}
		}
	} //NBA100

	/**
	 * Obtain the value for RelationRoleCode. OLifE.Relation.RelationRoleCode is the Role code of relationship.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveRelationRoleCode(NbaOinkRequest aNbaOinkRequest) {
		if (aNbaOinkRequest.getRelationRoleCode() != NbaOinkRequest.noFilterLong) {
			aNbaOinkRequest.addValue(aNbaOinkRequest.getRelationRoleCode());
			//begin NBA100
		} else {
			String roleType = aNbaOinkRequest.getQualifier();
			if (BASE_COV.equalsIgnoreCase(roleType) || NON_RIDER_COV.equalsIgnoreCase(roleType) || RIDER.equalsIgnoreCase(roleType)) {
				List lifeParticipants = getInsurableLifeParticipants(aNbaOinkRequest);
				int next = 0;
				LifeParticipant lifeParticipant;
				String partyId;
				Object[] relations = getOLifE().getRelation().toArray();
				while ((lifeParticipant = getNextLifeParticipant(aNbaOinkRequest, lifeParticipants, next++)) != null) {
					long code = -1;
					partyId = lifeParticipant.getPartyID();
					if (partyId != null && partyId.length() > 0) {
						Relation relation = NbaUtils.getRelationForParty(partyId, relations);
						if (relation != null) {
							code = relation.getRelationRoleCode();
						}
					}
					aNbaOinkRequest.addValue(code, NbaTableConstants.NBA_ROLES);
				}
			} else { //handle party related qualifiers
				//end NBA100
				String origPartyId = null;
				// NBA100 code deleted
				if (roleType.equals(PARTY_COBENEFICIARY) || roleType.equals(PARTY_BENEFICIARY)) {
					//begin SPR1335
					if (getOLifE().getSourceInfo().getFileControlID().equalsIgnoreCase(NbaConstants.SYST_VANTAGE)) {
						if (productType.equals(PRODUCT_ANNUITY)) {
							origPartyId = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE()).getId();
						} else {
							origPartyId = getCoverage(NbaOliConstants.OLI_COVIND_BASE).getId();
						}
					} else {
						origPartyId = getPartyForPrimaryIns().getId();
					}
				} else { //end SPR1335
					origPartyId = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE()).getId(); //NBA044
				}
				for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
					Party relatedParty = getParty(aNbaOinkRequest, i);
					if (relatedParty == null) {
						break;
					} else {
						Relation relation = getRelation(roleType, i, origPartyId, relatedParty.getId());
						if (relation != null) {
							aNbaOinkRequest.addValue(relation.getRelationRoleCode(), NbaTableConstants.NBA_ROLES);
						} else {
							aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_ROLES);
						}
					}
				}
			}
		}
	} //NBA100

	/**
	 * Obtain the value for ReltoAnnOrIns. For Beneficiaries, return OLifE.Relation.BeneficiaryDesignation. Otherwise, return
	 * OLifE.Relation.RelationDescription.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveReltoAnnOrIns(NbaOinkRequest aNbaOinkRequest) {
		String origPartyId = null;
		String roleType = aNbaOinkRequest.getQualifier();
		//begin SPR2757
		// There is a possibility of qualifier not being set, so retrieving the value of roleType.
		if (roleType == null || roleType.length() == 0) {
			String variable = aNbaOinkRequest.getVariable();
			String[] values = variable.split("_");
			if (values.length > 1 && NbaOinkRequest.qualifierTypes.contains(values[(values.length - 1)])) { //SPR2992
				roleType = values[(values.length - 1)];
			}
		}
		//end SPR2757
		int role;
		String table;
		if (roleType.equals(PARTY_COBENEFICIARY) || roleType.equals(PARTY_BENEFICIARY)) {
			//begin SPR1335
			if (getOLifE().getSourceInfo().getFileControlID().equalsIgnoreCase(NbaConstants.SYST_VANTAGE)) {
				if (productType.equals(PRODUCT_ANNUITY)) {
					origPartyId = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE()).getId();
				} else {
					origPartyId = getCoverage(NbaOliConstants.OLI_COVIND_BASE).getId();
				}
			} else {
				origPartyId = getPartyForPrimaryIns().getId();
			}//end SPR1335
			role = 1;
			table = NbaTableConstants.OLI_LU_RELDESC; //AXAL3.7.07
		} else if (roleType.equals(PARTY_OWNER)) {//AXAL3.7.06
			if (aNbaOinkRequest.getQualifier().equals("")) {
				origPartyId = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE()).getId(); //NBA044
			}
			role = 2;
			table = NbaTableConstants.OLI_LU_RELDESC;
		} else {
			// AXAL3.7.07 code deleted
			// begin AXAL3.7.07
			if (aNbaOinkRequest.getQualifier().equals("")) {
				origPartyId = getPartyForPrimaryIns().getId();
			} else {
				origPartyId = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE()).getId(); //NBA044
			}
			// end AXAL3.7.07
			role = 2;
			table = NbaTableConstants.OLI_LU_RELDESC;
		}
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party relatedParty = getParty(aNbaOinkRequest, i);
			if (relatedParty == null) {
				break;
			} else {
				Relation relation = getRelation(roleType, i, origPartyId, relatedParty.getId());
				if (relation != null) {
					switch (role) {
					case 1:
						aNbaOinkRequest.addValue(relation.getRelationDescription(), table); //AXAL3.7.07
						break;
					case 2:
						aNbaOinkRequest.addValue(relation.getRelationDescription(), table);
						break;
					}
				} else {
					aNbaOinkRequest.addValue(-1L, table);
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, table);
		}
	}

	/**
	 * Obtain the value for ReplacementCode. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.ReplacementCode.
	 * @param aNbaOinkRequest - data request container
	 * @deprecated this method will be removed in a future release. Use {@link #retrieveReplacementType(NbaOinkRequest)}
	 */
	public void retrieveReplacementCode(NbaOinkRequest aNbaOinkRequest) {

		//NBA093 deleted
		aNbaOinkRequest.addValue(getPolicy().getReplacementType(), NbaTableConstants.OLI_LU_REPLACETYPE); //NBA093

		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_REPLACETYPE);//NBA093
		}
	}

	/**
	 * Obtain the value for ReqCode. OLifE.Holding.Policy.RequirementInfo.ReqCode is the code specifying the underwriting requirement..
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveReqCode(NbaOinkRequest aNbaOinkRequest) {
		List reqList = getRequirementInfos(aNbaOinkRequest); //SPR3353
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			RequirementInfo aRequirementInfo = getRequirementInfo(reqList, i); //SPR3353
			if (aRequirementInfo != null) {
				aNbaOinkRequest.addValue(aRequirementInfo.getReqCode(), NbaTableConstants.NBA_REQUIREMENTS);
			} else {
				break;
			}
		}
	}

	/**
	 * Retrieves the FollowUpRequestNumber value based on the requirement id filter
	 * OLifE.Holding.Policy.RequirementInfo.RequirementInfoExtension.FollowUpRequestNumber
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA130 New Method
	public void retrieveFollowUpRequestNumber(NbaOinkRequest aNbaOinkRequest) {
		String reqIdFilter = aNbaOinkRequest.getRequirementIdFilter();
		Policy policy = getPolicy();
		ArrayList reqInfoList = new ArrayList();
		int listSize = 0;
		RequirementInfo reqInfo = null;
		if (policy != null) {
			reqInfoList = policy.getRequirementInfo();
			listSize = reqInfoList.size();
			for (int i = 0; i < listSize; i++) {
				reqInfo = (RequirementInfo) reqInfoList.get(i);
				if (reqInfo != null && reqInfo.getId().equals(reqIdFilter)) {
					break;
				}
			}
			if (reqInfo != null) {
				int index_extension = getExtensionIndex(reqInfo.getOLifEExtension(), REQUIREMENTINFO_EXTN);
				if (index_extension != -1) {
					RequirementInfoExtension extension = reqInfo.getOLifEExtensionAt(index_extension).getRequirementInfoExtension();
					if (extension != null) {
						aNbaOinkRequest.addValue(extension.getFollowUpRequestNumber());
					}
				}
			}
		}
	}

	/**
	 * Retrieves the RestrictIssueCode value based on the requirement id filter OLifE.Holding.Policy.RequirementInfo.RequirementInfo.RestrictIssueCode
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA130 New Method
	public void retrieveRestrictIssueCode(NbaOinkRequest aNbaOinkRequest) {
		String reqIdFilter = aNbaOinkRequest.getRequirementIdFilter();
		Policy policy = getPolicy();
		ArrayList reqInfoList = new ArrayList();
		int listSize = 0;
		RequirementInfo reqInfo = null;
		if (policy != null) {
			reqInfoList = policy.getRequirementInfo();
			listSize = reqInfoList.size();
			for (int i = 0; i < listSize; i++) {
				reqInfo = (RequirementInfo) reqInfoList.get(i);
				if (reqInfo != null && reqInfo.getId().equals(reqIdFilter)) {
					aNbaOinkRequest.addValue(reqInfo.getRestrictIssueCode());
					break;
				}
			}
		}
	}

	/**
	 * Obtain the value for ReplacementType. OLifE.Holding.Policy.ReplacementType.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA093 NEW METHOD
	public void retrieveReplacementType(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(getPolicy().getReplacementType(), NbaTableConstants.OLI_LU_REPLACETYPE);

		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_REPLACETYPE);
		}
	}
	/**
	 * Determine if all the (primary) beneficiary share method values are single sum.  
	 * If all (primary) beneficiary share method values are single sum, return the single sum value, 
	 * otherwise return that value that is not single sum.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveBeneficiaryShareMethod(NbaOinkRequest aNbaOinkRequest) {
		int relationCnt = getOLifE().getRelationCount();
		long beneShareMethod = NbaOliConstants.OLI_LU_BENESHRMTH_UNKNOWN;
		
		for (int j = 0; j < relationCnt; j++) {
			Relation aRelation = getOLifE().getRelationAt(j);
			if (aRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_BENEFICIARY) {
				if (beneShareMethod == NbaOliConstants.OLI_LU_BENESHRMTH_UNKNOWN && 
						aRelation.getBeneficiaryShareMethod() == NbaOliConstants.OLI_LU_BENESHRMTH_1009800004) {
					beneShareMethod = NbaOliConstants.OLI_LU_BENESHRMTH_1009800004;
				}
				else
				if (aRelation.getBeneficiaryShareMethod() != NbaOliConstants.OLI_LU_BENESHRMTH_1009800004) {
					beneShareMethod = aRelation.getBeneficiaryShareMethod();
				}
			}
		}
		aNbaOinkRequest.addValue(beneShareMethod);
	}
	/**
	 * Obtain the value for ReplacementCompany. ReplacementCompany is populated if there are multiple holdings and the replacement type indicator
	 * notes that it is a replacement contract and any of the relation role codes on the multiple holdings is equal to Holding Company (88). If this
	 * is true, then the Party.FullName specified by the party_id is returned. The first Holding Company relation role code encountered is used to
	 * populate this value. If all replacement companies are needed, use retrieveReplacementCompanies.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA128 NEW METHOD
	public void retrieveReplacementCompany(NbaOinkRequest aNbaOinkRequest) {
		if (!getPolicy().hasReplacementType() || getPolicy().getReplacementType() == NbaOliConstants.OLI_REPTY_NONE
				|| getPolicy().getReplacementType() == NbaOliConstants.OLI_UNKNOWN || getOLifE().getHoldingCount() == 1) {
			aNbaOinkRequest.addUnknownValue("");
			return;
		}
		int relationCnt = getOLifE().getRelationCount();
		for (int j = 0; j < relationCnt; j++) {
			Relation aRelation = getOLifE().getRelationAt(j);
			if (aRelation.hasRelationRoleCode() && aRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_HOLDINGCO) {
				NbaParty aParty = nbaTXLife.getParty(aRelation.getRelatedObjectID());
				if (aParty != null && aParty.getParty().hasFullName()) {
					aNbaOinkRequest.addValue(aParty.getParty().getFullName());
					break;
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addUnknownValue("");
		}
	}

	/**
	 * Obtain the value for ReplacementCompanies. ReplacementCompanies is populated if there are multiple holdings and the replacement type indicator
	 * notes that it is a replacement contract and any of the relation role codes on the multiple holdings is equal to Holding Company (88). If this
	 * is true, then the Party.FullName specified by the party_id is returned
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA128 NEW METHOD
	public void retrieveReplacementCompanies(NbaOinkRequest aNbaOinkRequest) {
		if (!getPolicy().hasReplacementType() || getPolicy().getReplacementType() == NbaOliConstants.OLI_REPTY_NONE
				|| getPolicy().getReplacementType() == NbaOliConstants.OLI_UNKNOWN || getOLifE().getHoldingCount() == 1) {
			aNbaOinkRequest.addUnknownValue("");
			return;
		}
		int relationCnt = getOLifE().getRelationCount();
		for (int j = 0; j < relationCnt; j++) {
			Relation aRelation = getOLifE().getRelationAt(j);
			if (aRelation.hasRelationRoleCode() && aRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_HOLDINGCO) {
				NbaParty aParty = nbaTXLife.getParty(aRelation.getRelatedObjectID());
				if (aParty != null && aParty.getParty().hasFullName()) {
					aNbaOinkRequest.addValue(aParty.getParty().getFullName());
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addUnknownValue("");
		}
	}

	/**
	 * Obtain the value for ReplacementCompaniesCount. ReplacementCompaniesCount is populated if there are multiple holdings and the replacement type
	 * indicator notes that it is a replacement contract and any of the relation role codes on the multiple holdings is equal to Holding Company (88).
	 * If this is true, then the count of those holdings is returned
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA128 NEW METHOD
	public void retrieveReplacementCompaniesCount(NbaOinkRequest aNbaOinkRequest) {
		int aCount = 0;
		if (!getPolicy().hasReplacementType() || getPolicy().getReplacementType() == NbaOliConstants.OLI_REPTY_NONE
				|| getPolicy().getReplacementType() == NbaOliConstants.OLI_UNKNOWN || getOLifE().getHoldingCount() == 1) {
			aNbaOinkRequest.addValue(aCount);
			return;
		}
		int relationCnt = getOLifE().getRelationCount();
		for (int j = 0; j < relationCnt; j++) {
			Relation aRelation = getOLifE().getRelationAt(j);
			if (aRelation.hasRelationRoleCode() && aRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_HOLDINGCO) {
				NbaParty aParty = nbaTXLife.getParty(aRelation.getRelatedObjectID());
				if (aParty != null && aParty.getParty().hasFullName()) {
					aCount += 1;
				}
			}
		}
		aNbaOinkRequest.addValue(aCount);
	}

	/**
	 * Obtain the value for RequirementDetails. OLifE.Holding.Policy.RequirementInfo.RequirementDetails is used to record additional details about the
	 * underwriting requirement.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveRequirementDetails(NbaOinkRequest aNbaOinkRequest) {
		List reqList = getRequirementInfos(aNbaOinkRequest); //SPR3353
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			RequirementInfo aRequirementInfo = getRequirementInfo(reqList, i); //SPR3353
			if (aRequirementInfo != null) {
				aNbaOinkRequest.addValue(aRequirementInfo.getRequirementDetails());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for a RiskExtension Question value. Reflection is used to message the RiskExtension object with a method composed of the
	 * variable name preceded by "get".
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveRiskExtensionQuestions(NbaOinkRequest aNbaOinkRequest) throws InvocationTargetException, IllegalAccessException {
		Object methodObject = getRiskExtensionMethod(aNbaOinkRequest.getRootVariable()); //SPR3053
		Class aClass;
		Method aMethod;
		if (methodObject != null) {
			aMethod = (Method) methodObject;
			aClass = aMethod.getReturnType();
		} else {
			if (getLogger().isWarnEnabled()) { //SPR3329
				getLogger().logWarn("Retrieve variable name is invalid: " + aNbaOinkRequest.getVariable()); // SPR3290 SPR3329
			}//SPR3329
			return;
		}
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				Risk aRisk = getRisk(party);
				if (aRisk != null) {
					int index_extension = getExtensionIndex(aRisk.getOLifEExtension(), RISK_EXTN);
					if (index_extension != -1) {
						RiskExtension extension = aRisk.getOLifEExtensionAt(index_extension).getRiskExtension();
						if (extension != null) {
							aNbaOinkRequest.addValueForType(((Method) methodObject).invoke(extension, null), aClass);
						}
					} else {
						aNbaOinkRequest.addUnknownValueForType(aClass);
					}
				} else {
					aNbaOinkRequest.addUnknownValueForType(aClass);
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for a Risk Question value. Reflection is used to message the Risk object with a method composed of the variable name preceded
	 * by "get".
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveRiskQuestions(NbaOinkRequest aNbaOinkRequest) throws InvocationTargetException, IllegalAccessException {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				// SPR3290 code deleted
				Risk aRisk = getRisk(party);
				if (aRisk != null) {
					Object methodObject = getRiskMethod(aNbaOinkRequest.getRootVariable()); //SPR3053
					if (methodObject != null) {
						Class aClass = ((Method) methodObject).getReturnType();
						aNbaOinkRequest.addValueForType(((Method) methodObject).invoke(aRisk, null), aClass);
					} else {
						getLogger().logDebug("Retrieve variable name is invalid: " + aNbaOinkRequest.getVariable()); // SPR3290
						return;
					}
				} else {
					Object methodObject = getRiskMethod(aNbaOinkRequest.getRootVariable()); //SPR3053
					if (methodObject != null) {
						Class aClass = ((Method) methodObject).getReturnType();
						aNbaOinkRequest.addUnknownValueForType(aClass);
					} else {
						getLogger().logDebug("Retrieve variable name is invalid: " + aNbaOinkRequest.getVariable()); // SPR3290
						return;
					}
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for SignedDate. OLifE.Holding.Policy.ApplicationInfo.SignedDate is the date application was signed.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSignedDate(NbaOinkRequest aNbaOinkRequest) {
		if (getApplicationInfo() != null) {
			aNbaOinkRequest.addValue(getApplicationInfo().getSignedDate());
		} else {
			aNbaOinkRequest.addUnknownValue(new Date());
		}
	}

	/**
	 * Obtain the value for Suffix. OLifE.Party.PersonOrOrganization.Person.Suffix is the suffix for the person
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSuffix(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				aNbaOinkRequest.addValue(person.getSuffix());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for TaxableStatus. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Annuity.TaxableStatus defines whether taxing is
	 * required - e.g. compulsory or voluntary.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveTaxableStatus(NbaOinkRequest aNbaOinkRequest) {
		if (getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() == null) { //NBA093
			aNbaOinkRequest.addValue(-1L);
		} else {
			if (getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isAnnuity()) { //NBA093
				aNbaOinkRequest.addValue(getAnnuity().getTaxableStatus());
			} else {
				aNbaOinkRequest.addValue(-1L);
			}
		}
	}

	/**
	 * Obtain the value for Weight. OLifE.Party.PersonOrOrganization.Person.Weight is the weight of the person in Kilograms.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveWeight(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				if (person.getWeight2() != null && person.getWeight2().hasMeasureValue()) { //NBA093
					aNbaOinkRequest.addValue(person.getWeight2().getMeasureValue()); //NBA093
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for PortfolioCode. Holding.Investment.SubAccount.PortfolioCode is the fund selection
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA059 new method
	public void retrievePortfolioCode(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (getSubAccount(i) != null) {
				aNbaOinkRequest.addValue(getSubAccount(i).getPortfolioCode(), NbaTableConstants.NBA_FUNDS);
			} else {
				aNbaOinkRequest.addValue("", NbaTableConstants.NBA_FUNDS);
			}
		}

	}

	/**
	 * Obtain the value for AllocPercent. Holding.Investment.SubAccount.AllocPercent is the fund percent
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA059 new method
	public void retrieveAllocPercent(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (getSubAccount(i) != null) {
				aNbaOinkRequest.addValue(getSubAccount(i).getAllocPercent());
			}
		}
	}

	/**
	 * Obtain the value for ProductObjetive. Holding.Investment.SubAccount.ProductObjective is the product objective.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA059 new method
	public void retrieveProductObjective(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			if (getSubAccount(i) != null) {
				aNbaOinkRequest.addValue(getSubAccount(i).getProductObjective());
			}
		}
	}

	/**
	 * Obtain the value for ReplacementCode. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.ReplacementCode.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA087 New Method
	public void retrieveUnderwritingApproval(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null) {
					aNbaOinkRequest.addValue(oli.getApplicationInfoExtension().getUnderwritingApproval(), NbaTableConstants.OLIEXT_LU_UNDAPPROVAL);
					return;
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLIEXT_LU_UNDAPPROVAL);
		}
	}

	/**
	 * Obtain the value for FileControlID. OLifE.SourceInfo.FileControlID
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA064 New Method
	public void retrieveFileControlID(NbaOinkRequest aNbaOinkRequest) {
		if (oLifE.hasSourceInfo() && oLifE.getSourceInfo().hasFileControlID()) {
			aNbaOinkRequest.addValue(oLifE.getSourceInfo().getFileControlID());
		} else {
			aNbaOinkRequest.addUnknownValue("");
		}
	}

	/**
	 * Obtain the value for RequestedPolDate. OLifE.Holding.Policy.ApplicationInfo.RequestedPolDate
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA064 New Method
	public void retrieveRequestedPolDate(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo applicationInfo = getApplicationInfo();
		if (applicationInfo != null && applicationInfo.hasRequestedPolDate()) {
			aNbaOinkRequest.addValue(applicationInfo.getRequestedPolDate());
		} else {
			aNbaOinkRequest.addUnknownValue("");
		}
	}

	/**
	 * Obtain the value for IssueDate. OLifE.Holding.Policy.IssueDate
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA064 New Method
	public void retrieveIssueDate(NbaOinkRequest aNbaOinkRequest) {
		if (getPolicy() != null && getPolicy().hasIssueDate()) {
			aNbaOinkRequest.addValue(getPolicy().getIssueDate());
		} else {
			aNbaOinkRequest.addUnknownValue("");
		}
	}

	/**
	 * Obtain the value for Accidental Death Benefit Amount. Holding.Policy.Life.Coverage.CovOption.DeathBenefitAmt.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA072 New Method
	public void retrieveDeathBenefitAmt(NbaOinkRequest aNbaOinkRequest) {
		//NBA104 deleted code
		String covType = aNbaOinkRequest.getQualifier();
		if (covType.equals(ACCIDENTAL_DEATH_BENEFIT)) {
			// begin NBA104
			List covoptions = getCovOptions(NbaOliConstants.OLI_OPTTYPE_ADB);
			if (covoptions != null) {
				int count = covoptions.size();
				for (int i = 0; i < count; i++) {
					CovOption covOption = (CovOption) covoptions.get(i);
					if (covOption != null && covOption.hasDeathBenefitAmt()) {
						aNbaOinkRequest.addValue(covOption.getDeathBenefitAmt());
					}
				}
			}
			// end NBA104
		}
	}

	/**
	 * Obtain the value for ADB Indicator.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA072 New Method
	public void retrieveADBElectedIndicator(NbaOinkRequest aNbaOinkRequest) {
		// NBA104 deleted code
		// begin NBA104
		List covoptions = getCovOptions(NbaOliConstants.OLI_OPTTYPE_ADB);
		if (covoptions != null && covoptions.size() > 0) {
			aNbaOinkRequest.addValue("1");
		} else {
			aNbaOinkRequest.addValue("0");
		}
		// end NBA104
	}

	/**
	 * Obtain the value for CurrentAmt By Coverage OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.Coverage.CurrentAmt is the amount of
	 * coverage -- the face amount of the rider without options.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New method
	public void retrieveCurrentAmtByCoverage(NbaOinkRequest aNbaOinkRequest) {
		Life aLife = getLife();
		String coverageType = aNbaOinkRequest.getQualifier(); //begin nba072
		if ((coverageType.equals(BASE_COV) || coverageType.equals(NON_RIDER_COV) || coverageType.equals(RIDER)) && aLife != null) {
			// SPR3290 code deleted
			Coverage coverage = null;
			// SPR3290 code deleted

			if (coverageType.equals(BASE_COV)) {
				coverage = getBaseCoverage();
				if (coverage != null) {
					aNbaOinkRequest.addValue(coverage.getCurrentAmt(), FORMAT_TYPE_CURRENCY);
				}
			}
			if (coverageType.equals(NON_RIDER_COV) || coverageType.equals(RIDER)) {
				if (aNbaOinkRequest.getCoverageFilter() != -1) {
					coverage = getRider(aNbaOinkRequest.getCoverageFilter());
					if (coverage != null && coverage.hasCurrentAmt()) {
						aNbaOinkRequest.addValue(coverage.getCurrentAmt(), FORMAT_TYPE_CURRENCY);
					}
				} else {
					//  Vector riderVector = new Vector();
					List coverages = null;
					if (coverageType.equals(NON_RIDER_COV)) {
						coverages = getNonRider();
					}
					if (coverageType.equals(RIDER)) {
						coverages = getRider();
					}
					for (int i = 0; i < coverages.size(); i++) {
						coverage = (Coverage) coverages.get(i);
						if (coverage != null && coverage.hasCurrentAmt()) {
							aNbaOinkRequest.addValue(coverage.getCurrentAmt(), FORMAT_TYPE_CURRENCY);
						}
					}
				}
			}
		} else { //end NBA072
			if (aLife != null) {
				for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
					int covIndx = getCoverageForParty(aNbaOinkRequest, i);
					if (covIndx == -1) {
						break;
					} else {
						Coverage aCoverage = aLife.getCoverageAt(covIndx); // SPR3290
						aNbaOinkRequest.addValue(aCoverage.getCurrentAmt(), FORMAT_TYPE_CURRENCY);
					}
				}
			}
		} //NBA072
	}

	/**
	 * Obtain the value for Benefit option amount with Coverage Filter applied. Holding.Policy.Life.Coverage.CovOption.OptionAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveOptionAmtByCoverage(NbaOinkRequest aNbaOinkRequest) {
		int sizeCoverage = 0;
		Life life = getLife();
		Coverage coverage = null;
		CovOption covOption = null;
		String covOptionType = aNbaOinkRequest.getQualifier();
		if ((covOptionType.equals(BENEFIT) || covOptionType.equals(ACCIDENTAL_DEATH_BENEFIT) || covOptionType.equals(WAIVER_OF_PREMIUM) || covOptionType
				.equals(GUARANTEED_INSURABILITY_RIDER))
				&& (life != null)) {
			if (aNbaOinkRequest.getCoverageFilter() != -1 || !aNbaOinkRequest.getCoverageIdFilter().equals("")) {
				// get coverage based on which one of two filters has been applied.
				if (aNbaOinkRequest.getCoverageFilter() != -1)
					coverage = getCoverage(aNbaOinkRequest.getCoverageFilter());
				else if (!aNbaOinkRequest.getCoverageIdFilter().equals(""))
					coverage = getCoverage(aNbaOinkRequest.getCoverageIdFilter());
				for (int j = 0; j < coverage.getCovOptionCount(); j++) {
					covOption = null;
					if (covOptionType.equals(ACCIDENTAL_DEATH_BENEFIT)) {
						if (coverage.getCovOptionAt(j).getLifeCovOptTypeCode() == NbaOliConstants.OLI_OPTTYPE_ADB) {
							covOption = coverage.getCovOptionAt(j);
						}
					} else if (covOptionType.equals(WAIVER_OF_PREMIUM)) {
						if (coverage.getCovOptionAt(j).getLifeCovOptTypeCode() == NbaOliConstants.OLI_OPTTYPE_WP) {
							covOption = coverage.getCovOptionAt(j);
						}
					} else if (covOptionType.equals(GUARANTEED_INSURABILITY_RIDER)) {
						if (coverage.getCovOptionAt(j).getLifeCovOptTypeCode() == NbaOliConstants.OLI_OPTTYPE_GIR) {
							covOption = coverage.getCovOptionAt(j);
						}
					} else {
						covOption = coverage.getCovOptionAt(j);
						if (aNbaOinkRequest.getCovOptionFilter() != -1 && covOption.getLifeCovOptTypeCode() != aNbaOinkRequest.getCovOptionFilter()) {
							continue;
						}
					}
					if (covOption != null && (covOption.hasOptionAmt())) {
						aNbaOinkRequest.addValue(covOption.getOptionAmt());
					}
				}
			} else {
				sizeCoverage = life.getCoverageCount();
				for (int i = 0; i < sizeCoverage; i++) {
					coverage = life.getCoverageAt(i);
					for (int j = 0; j < coverage.getCovOptionCount(); j++) {
						covOption = coverage.getCovOptionAt(j);
						if (aNbaOinkRequest.getCovOptionFilter() != -1 && covOption.getLifeCovOptTypeCode() != aNbaOinkRequest.getCovOptionFilter()) {
							continue;
						}
						if (covOption != null && (covOption.hasOptionAmt())) {
							aNbaOinkRequest.addValue(covOption.getOptionAmt());
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain the value for Benefit option amount. Holding.Policy.Life.Coverage.CovOption.OptionAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA072 New Method
	public void retrieveOptionAmt(NbaOinkRequest aNbaOinkRequest) {
		//NBA104 deleted code
		// begin NBA104
		CovOption covOption = null;
		List covoptions = null;
		String covOptionType = aNbaOinkRequest.getQualifier();
		if (covOptionType.equals(BENEFIT) || covOptionType.equals(ACCIDENTAL_DEATH_BENEFIT)) {
			if (covOptionType.equals(ACCIDENTAL_DEATH_BENEFIT)) {
				covoptions = getCovOptions(NbaOliConstants.OLI_OPTTYPE_ADB);
			} else {
				covoptions = getCovOptions();
			}
			if (covoptions != null) {
				int count = covoptions.size();
				for (int i = 0; i < count; i++) {
					covOption = (CovOption) covoptions.get(i);
					if (covOption != null) {
						aNbaOinkRequest.addValue(covOption.getOptionAmt());
					}
				}
			}
		}
		// end NBA104
	}

	/**
	 * Obtain the value for Benifit number of units. Holding.Policy.Life.Coverage.CovOption.OptionNumberOfUnits
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveOptionNumberOfUnits(NbaOinkRequest aNbaOinkRequest) {
		// SPR3290 code deleted
		Life life = getLife();
		Coverage coverage = null;
		CovOption covOption = null;
		String covOptionType = aNbaOinkRequest.getQualifier();
		if ((covOptionType.equals(ACCIDENTAL_DEATH_BENEFIT) || covOptionType.equals(WAIVER_OF_PREMIUM) || covOptionType
				.equals(GUARANTEED_INSURABILITY_RIDER))
				&& (life != null)) {
			if (aNbaOinkRequest.getCoverageFilter() != -1) {
				coverage = getCoverage(aNbaOinkRequest.getCoverageFilter());
				int sizeCoverage = coverage.getCovOptionCount(); // SPR3290
				for (int j = 0; j < sizeCoverage; j++) { // SPR3290
					covOption = null;
					if (covOptionType.equals(ACCIDENTAL_DEATH_BENEFIT)) {
						if (coverage.getCovOptionAt(j).getLifeCovOptTypeCode() == NbaOliConstants.OLI_OPTTYPE_ADB) {
							covOption = getCovOption(coverage, NbaOliConstants.OLI_OPTTYPE_ADB);
						}
					} else if (covOptionType.equals(WAIVER_OF_PREMIUM)) {
						if (coverage.getCovOptionAt(j).getLifeCovOptTypeCode() == NbaOliConstants.OLI_OPTTYPE_WP) {
							covOption = getCovOption(coverage, NbaOliConstants.OLI_OPTTYPE_WP);
						}
					} else if (covOptionType.equals(GUARANTEED_INSURABILITY_RIDER)) {
						if (coverage.getCovOptionAt(j).getLifeCovOptTypeCode() == NbaOliConstants.OLI_OPTTYPE_GIR) {
							covOption = getCovOption(coverage, NbaOliConstants.OLI_OPTTYPE_GIR);
						}
					} else {
						covOption = coverage.getCovOptionAt(j);
						if (aNbaOinkRequest.getCovOptionFilter() != -1 && covOption.getLifeCovOptTypeCode() != aNbaOinkRequest.getCovOptionFilter()) {
							continue;
						}
					}
					if (covOption != null && (covOption.hasOptionNumberOfUnits())) {
						aNbaOinkRequest.addValue(covOption.getOptionNumberOfUnits());
					}
				}
			}
			//begin NBA104
		} else {
			List covoptions = getCovOptions();
			if (covoptions != null) {
				int count = covoptions.size();
				for (int i = 0; i < count; i++) {
					covOption = (CovOption) covoptions.get(i);
					if (covOption != null) {
						aNbaOinkRequest.addValue(covOption.getOptionNumberOfUnits());
					}
				}
			}
		}
		//end NBA104
	}

	/**
	 * Obtain the value for Annual Premium per unit. Holding.Policy.Life.Coverage.PremiumPerUnit Holding.Policy.Life.Coverage.CovOption.PremiumPerUnit
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA072 New Method
	public void retrievePremiumPerUnit(NbaOinkRequest aNbaOinkRequest) {
		int sizeCoverage = 0;
		// NBA104 deleted code
		Coverage coverage = null;
		CovOption covOption = null;
		String coverageType = aNbaOinkRequest.getQualifier();
		// begin NBA104
		if (coverageType.equals(BENEFIT) || coverageType.equals(ACCIDENTAL_DEATH_BENEFIT)) {
			List covoptions = null;
			if (coverageType.equals(ACCIDENTAL_DEATH_BENEFIT)) {
				covoptions = getCovOptions(NbaOliConstants.OLI_OPTTYPE_ADB);
			} else {
				covoptions = getCovOptions();
			}
			if (covoptions != null) {
				int count = covoptions.size();
				for (int i = 0; i < count; i++) {
					covOption = (CovOption) covoptions.get(i);
					if (covOption != null) {
						aNbaOinkRequest.addValue(covOption.getPremiumPerUnit());
					}
				}
			}
		} else if (coverageType.equals(NON_RIDER_COV) || coverageType.equals(RIDER)) {
			List coverages = null;
			if (coverageType.equals(NON_RIDER_COV)) {
				coverages = getNonRider();
			} else {
				coverages = getRider();
			}

			if (coverages != null) {
				sizeCoverage = coverages.size();
				for (int i = 0; i < sizeCoverage; i++) {
					coverage = (Coverage) coverages.get(i);
					if (aNbaOinkRequest.getCoverageFilter() != -1) {
						if (coverage.getLifeCovTypeCode() == aNbaOinkRequest.getCoverageFilter()) {
							aNbaOinkRequest.addValue(coverage.getPremiumPerUnit());
						}
					} else {
						aNbaOinkRequest.addValue(coverage.getPremiumPerUnit());
					}
				}
			}
		}
		// end NBA104
	}

	/**
	 * Obtain the value for Temporary Flat Extra Amount. Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.TempFlatExtraAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA072 New Method
	public void retrieveTempFlatExtraAmt(NbaOinkRequest aNbaOinkRequest) {
		//begin NBA100
		List ratings = getTempFlatSubstandardRatingsForParty(aNbaOinkRequest); // CR1343973 
		// SPR3290 code deleted
		for (int i = 0; i < ratings.size(); i++) {
			// CR1343973 code deleted			
			aNbaOinkRequest.addValue(((SubstandardRating) ratings.get(i)).getTempFlatExtraAmt());
		}
		//end NBA100
	}

	/**
	 * Obtain the values for Temporary Flat Extra Amount for all Life Participants. Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.TempFlatExtraAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//P2AXAL044 New Method
	public void retrieveTempFlatExtraAmtX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List ratings = getTempFlatSubstandardRatings(aNbaOinkRequest);
		for (int i = 0; i < ratings.size(); i++) {
			if (ratings.get(i) == null) {
				aNbaOinkRequest.addValue(Double.NaN);
			} else {
				aNbaOinkRequest.addValue(((SubstandardRating) ratings.get(i)).getTempFlatExtraAmt());
			}
		}
	}

	/**
	 * Obtain the values for Permanent Flat Cease date. The Cease date is the TermDate of the associated coverage.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrievePermFlatExtraCeaseDate(NbaOinkRequest aNbaOinkRequest) {
		List ratings = getPermFlatSubstandardRatings(aNbaOinkRequest);
		List coverages = getCoveragesOrRiders(aNbaOinkRequest);
		Coverage coverage;
		for (int i = 0; i < ratings.size(); i++) {
			if (ratings.get(i) == null) {
				aNbaOinkRequest.addUnknownValue("");
			} else {
				coverage = (Coverage) coverages.get(i);
				if (coverage.hasTermDate()) {
					aNbaOinkRequest.addValue(coverage.getTermDate());
				} else {
					aNbaOinkRequest.addUnknownValue("");
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addUnknownValue("");
		}
	}

	/**
	 * Obtain the values for Temporary Flat Effdate Date. Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.EffDate
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveTempFlatExtraEffDate(NbaOinkRequest aNbaOinkRequest) {
		List ratings = getTempFlatSubstandardRatings(aNbaOinkRequest);
		SubstandardRatingExtension substandardRatingExt;
		for (int i = 0; i < ratings.size(); i++) {
			if (ratings.get(i) == null) {
				aNbaOinkRequest.addUnknownValue("");
			} else {
				substandardRatingExt = NbaUtils.getFirstSubstandardExtension((SubstandardRating) ratings.get(i));
				if (substandardRatingExt.hasEffDate()) {
					aNbaOinkRequest.addValue(substandardRatingExt.getEffDate());
				} else {
					aNbaOinkRequest.addUnknownValue("");
				}
			}
		}
	}

	/**
	 * Obtain the values for Permanent Flat Effdate Date. Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.EffDate
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrievePermFlatExtraEffDate(NbaOinkRequest aNbaOinkRequest) {
		List ratings = getPermFlatSubstandardRatings(aNbaOinkRequest);
		SubstandardRatingExtension substandardRatingExt;
		for (int i = 0; i < ratings.size(); i++) {
			if (ratings.get(i) == null) {
				aNbaOinkRequest.addUnknownValue("");
			} else {
				substandardRatingExt = NbaUtils.getFirstSubstandardExtension((SubstandardRating) ratings.get(i));
				if (substandardRatingExt.hasEffDate()) {
					aNbaOinkRequest.addValue(substandardRatingExt.getEffDate());
				} else {
					aNbaOinkRequest.addUnknownValue("");
				}
			}
		}
	}	

	/**
	 * Obtain the value for Valuation Class Type Holding.Policy.Life.Coverage.OLifEExtension.CoverageExtension.ValuationClassType
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA072 New Method
	public void retrieveValuationClassType(NbaOinkRequest aNbaOinkRequest) {
		// NBA104 deleted code
		//begin NBA100
		CoverageExtension coverageExt;
		Coverage coverage;
		// SPR3290 code deleted
		if (getNbaTXLife().isAnnuity()) {
			long valuationClassType = -1;
			AnnuityExtension annuityExtension = NbaUtils.getFirstAnnuityExtension(getAnnuity());
			if (annuityExtension != null) {
				valuationClassType = annuityExtension.getValuationClassType();
			}
			aNbaOinkRequest.addValue(valuationClassType);
		} else {
			List coverages = getCoveragesOrRiders(aNbaOinkRequest);
			int next = 0;
			while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
				coverageExt = NbaUtils.getFirstCoverageExtension(coverage);
				if (coverageExt != null) {
					aNbaOinkRequest.addValue(coverageExt.getValuationClassType());
				} else {
					aNbaOinkRequest.addValue(-1);
				}
			}
			//end NBA100
			// NBA100 code deleted
		}
		//begin NBA100
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1);
		}
		//end NBA100
	}

	/**
	 * Obtain the value for Issue Date. Holding.Policy.Life.Coverage.EffDate Holding.Policy.Life.Coverage.CovOption.EffDate
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA072 New Method
	public void retrieveEffDate(NbaOinkRequest aNbaOinkRequest) {
		//begin NBA100 NBA104
		CovOption covOption = null;
		String coverageType = aNbaOinkRequest.getQualifier();
		if (getNbaTXLife().isAnnuity()) {
			aNbaOinkRequest.addValue(getPolicy().getEffDate());
		} else if (coverageType.equals(BENEFIT) || coverageType.equals(ACCIDENTAL_DEATH_BENEFIT)) {
			List covoptions = null;
			if (coverageType.equals(ACCIDENTAL_DEATH_BENEFIT)) {
				covoptions = getCovOptions(NbaOliConstants.OLI_OPTTYPE_ADB);
			} else {
				covoptions = getCovOptions();
			}
			if (covoptions != null) {
				int count = covoptions.size();
				for (int i = 0; i < count; i++) {
					covOption = (CovOption) covoptions.get(i);
					if (covOption != null) {
						aNbaOinkRequest.addValue(covOption.getEffDate());
					}
				}
			}
		} else {
			List coverages = getCoveragesOrRiders(aNbaOinkRequest);
			int next = 0;
			Coverage coverage;
			while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
				if (coverage.hasEffDate()) {
					aNbaOinkRequest.addValue(coverage.getEffDate());
				} else {
					aNbaOinkRequest.addUnknownValue((Date) null);
				}
			}
		}
		//end NBA100 NBA104
	}

	/**
	 * Obtain the value for PaidToDate. Holding.Policy.PaidToDate
	 */
	//NBA104 New Method
	public void retrievePaidToDate(NbaOinkRequest aNbaOinkRequest) {
		Policy policy = getPolicy();
		if (policy != null && policy.hasPaidToDate()) {
			aNbaOinkRequest.addValue(policy.getPaidToDate());
		}
	}

	/**
	 * Obtain the value for FirstSkipMonth Holding.Policy.FirstSkipMonth
	 */
	//NBA104 New Method
	public void retrieveFirstSkipMonth(NbaOinkRequest aNbaOinkRequest) {
		Policy policy = getPolicy();
		if (policy != null) {
			PolicyExtension policyextension = NbaUtils.getFirstPolicyExtension(policy);
			if (policyextension != null && policyextension.hasFirstSkipMonth()) {
				aNbaOinkRequest.addValue(policyextension.getFirstSkipMonth());
			}
		}
	}

	/**
	 * Obtain the value for NonStandardPaidToDate Holding.Policy.PolicyExtension.NonStandardPaidToDate
	 */
	//NBA104 New Method
	public void retrieveNonStandardPaidToDate(NbaOinkRequest aNbaOinkRequest) {
		Policy policy = getPolicy();
		if (policy != null) {
			PolicyExtension policyextension = NbaUtils.getFirstPolicyExtension(policy);
			if (policyextension != null && policyextension.hasNonStandardPaidToDate()) {
				aNbaOinkRequest.addValue(policyextension.getNonStandardPaidToDate());
			}
		}
	}

	/**
	 * Obtain the value for Phase Code. Holding.Policy.Life.Coverage.CoverageKey Holding.Policy.Life.Coverage.CovOption.CovOptionKey
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA072 New Method
	public void retrieveCoverageKey(NbaOinkRequest aNbaOinkRequest) {
		// NBA104 deleted code;
		Coverage coverage = null;
		CovOption covOption = null;
		String coverageType = aNbaOinkRequest.getQualifier();
		//	begin NBA104
		if (coverageType.equals(BENEFIT) || coverageType.equals(ACCIDENTAL_DEATH_BENEFIT)
				|| coverageType.equals(ROPR_BENEFIT) || coverageType.equals(LTC_BENEFIT)) { //ALII1012, P2AXAL040
			List covoptions = null;
			if (coverageType.equals(ACCIDENTAL_DEATH_BENEFIT)) {
				covoptions = getCovOptions(NbaOliConstants.OLI_OPTTYPE_ADB);
			} else if (coverageType.equals(ROPR_BENEFIT)) { //ALII1012, P2AXAL040
				covoptions = getCovOptions(NbaOliConstants.OLI_OPTTYPE_ROPR);
			} else if (coverageType.equals(LTC_BENEFIT)) { //ALII1012, P2AXAL040
				covoptions = getCovOptions(NbaOliConstants.OLI_OPTTYPE_LTCABO);
			} else {
				covoptions = getCovOptions();
			}
			if (covoptions != null) {
				int count = covoptions.size();
				for (int i = 0; i < count; i++) {
					covOption = (CovOption) covoptions.get(i);
					if (covOption != null) {
						aNbaOinkRequest.addValue(covOption.getCovOptionKey());
					}
				}
			}
		} else if (coverageType.equals(NON_RIDER_COV) || coverageType.equals(RIDER)) {
			List coverages = null;
			if (coverageType.equals(NON_RIDER_COV)) {
				coverages = getNonRider();
			} else {
				coverages = getRider();
			}

			if (coverages != null) {
				int count = coverages.size();
				for (int i = 0; i < count; i++) {
					coverage = (Coverage) coverages.get(i);
					if (coverage != null) {
						aNbaOinkRequest.addValue(coverage.getCoverageKey());
					}
				}
			}
		}
		// end NBA104
	}

	/**
	 * Obtain the value for Benefit Rating Factor i.e SubstandardRating.PermPercentageLoading / 100.
	 * Holding.Policy.Life.Coverage.CovOption.SubstandardRating.PermPercentageLoading
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA072 New Method
	public void retrievePermPercentageLoading(NbaOinkRequest aNbaOinkRequest) {
		// NBA104 deleted code
		// begin NBA104
		String covOptionType = aNbaOinkRequest.getQualifier();
		if (covOptionType.equals(BENEFIT) || covOptionType.equals(ACCIDENTAL_DEATH_BENEFIT)) {
			Life life = getLife();
			if (life == null) {
				return;
			}
			CovOption covOption = null;
			SubstandardRating substandardRating = null;
			SubstandardRatingExtension substandardRatingEx = null;
			List covOptionList = getCovOptions();// Begin ALII2073
			if (covOptionList != null) {
				for (int i = 0; i < covOptionList.size(); i++) {
					covOption = (CovOption) covOptionList.get(i);// End ALII207
					if (covOption != null) {
						if (covOptionType.equals(ACCIDENTAL_DEATH_BENEFIT) && covOption.getLifeCovOptTypeCode() != NbaOliConstants.OLI_OPTTYPE_ADB) {
							continue;
						}
						boolean resolved = false;
						int countSR = covOption.getSubstandardRatingCount();
						for (int k = 0; k < countSR && !resolved; k++) {
							substandardRating = covOption.getSubstandardRatingAt(k);
							if (NbaUtils.isValidRating(substandardRating)) { // SPR2590
								// ALS3235 Begin
								if (substandardRating.hasPermTableRating()) {
									aNbaOinkRequest.addValue(substandardRating.getPermTableRating()); // SPR2170
									resolved = true;
								} else {
									substandardRatingEx = NbaUtils.getFirstSubstandardExtension(substandardRating);
									// ALS3235 Begin
									if (substandardRatingEx != null && substandardRatingEx.hasPermTableRating()) {
										aNbaOinkRequest.addValue(substandardRatingEx.getPermTableRating()); // SPR2170 }
										resolved = true;
									}
								}
								// ALS3235 end
							}
						}
						if (!resolved) {
							aNbaOinkRequest.addValue(1L); // defaults to 1, if not specified
						}
					}
				}
			}
		}
		// end NBA104
	}

	/**
	 * Obtain the value for Annual Premium Amount. Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.AnnualPremAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA072 New Method
	public void retrieveAnnualPremiumAmt(NbaOinkRequest aNbaOinkRequest) {
		// NBA104 deleted code
		Coverage coverage = null;
		String coverageType = aNbaOinkRequest.getQualifier();

		// begin NBA104
		if (coverageType.equals(NON_RIDER_COV) || coverageType.equals(RIDER)) {
			List coverages = null;
			if (coverageType.equals(NON_RIDER_COV)) {
				coverages = getNonRider();
			} else {
				coverages = getRider();
			}

			if (coverages == null) {
				return;
			}
			int count = coverages.size();
			for (int i = 0; i < count; i++) {
				coverage = (Coverage) coverages.get(i);
				boolean resolved = false;
				LifeParticipant lifeParticipant = NbaUtils.getInsurableLifeParticipant(coverage);
				if (lifeParticipant != null && !lifeParticipant.isActionDelete()) {
					int countSR = lifeParticipant.getSubstandardRatingCount();
					for (int j = 0; j < countSR && !resolved; j++) {
						SubstandardRating substandardRating = lifeParticipant.getSubstandardRatingAt(j);
						if (NbaUtils.isValidRating(substandardRating)) { //SPR2590
							SubstandardRatingExtension srExt = NbaUtils.getFirstSubstandardExtension(substandardRating);
							if (srExt != null) {
								aNbaOinkRequest.addValue(srExt.getAnnualPremAmt());
								resolved = true;
							}
						}
					}
				}
				if (!resolved) {
					aNbaOinkRequest.addValue(Double.NaN);
				}
			}
		}
		// end NBA104
	}

	/**
	 * Obtain the value for a Coverage ProductCode. Holding.Policy.Life.Coverage.ProductCode is a productCode.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 new method
	public void retrieveCoverageAnnualPremAmt(NbaOinkRequest aNbaOinkRequest) {
		Life aLife = getLife();
		if (aLife == null) {
			;
		} else {
			for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
				int covIndx = getCoverageForParty(aNbaOinkRequest, i);
				if (covIndx == -1) {
					break;
				} else {
					Coverage aCoverage = aLife.getCoverageAt(covIndx); // SPR3290
					aNbaOinkRequest.addValue(aCoverage.getAnnualPremAmt());
				}
			}
		}
	}

	/**
	 * Obtain the value for Perm Table Rating Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.PermTableRating
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA072 New Method
	public void retrievePermTableRating(NbaOinkRequest aNbaOinkRequest) {
		// NBA104 deleted code
		Coverage coverage = null;
		String coverageType = aNbaOinkRequest.getQualifier();
		// NBA104 deleted code
		// begin NBA104
		if (coverageType.equals(NON_RIDER_COV) || coverageType.equals(RIDER)) {
			List coverages = null;
			if (coverageType.equals(NON_RIDER_COV)) {
				coverages = getNonRider();
			} else {
				coverages = getRider();
			}

			if (coverages == null) {
				return;
			}
			int count = coverages.size();
			for (int i = 0; i < count; i++) {
				coverage = (Coverage) coverages.get(i);
				boolean resolved = false;
				LifeParticipant lifeParticipant = NbaUtils.getInsurableLifeParticipant(coverage);
				if (lifeParticipant != null && !lifeParticipant.isActionDelete()) {
					int countSR = lifeParticipant.getSubstandardRatingCount();
					for (int j = 0; j < countSR && !resolved; j++) {
						SubstandardRating substandardRating = lifeParticipant.getSubstandardRatingAt(j);
						if (NbaUtils.isValidRating(substandardRating) && substandardRating.hasPermTableRating()) { //SPR2590
							aNbaOinkRequest.addValue(substandardRating.getPermTableRating());
							resolved = true;
						}
					}
				}
				if (!resolved) {
					aNbaOinkRequest.addValue(-1);
				}
			}
		}
		// end NBA104
	}	

	/**
	 * Obtain the value for Current Number Of Units. Holding.Policy.Life.Coverage.CurrentNumberOfUnits
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA072 New Method
	public void retrieveCurrentNumberOfUnits(NbaOinkRequest aNbaOinkRequest) {
		// NBA104 deleted code
		Coverage coverage = null;
		String coverageType = aNbaOinkRequest.getQualifier();

		// begin NBA104
		if (coverageType.equals(NON_RIDER_COV) || coverageType.equals(RIDER)) {
			List coverages = null;
			if (coverageType.equals(NON_RIDER_COV)) {
				coverages = getNonRider();
			} else {
				coverages = getRider();
			}

			if (coverages != null) {
				int count = coverages.size();
				for (int i = 0; i < count; i++) { // SPR3290
					coverage = (Coverage) coverages.get(i);
					if (coverage != null) {
						aNbaOinkRequest.addValue(coverage.getCurrentNumberOfUnits());
					}
				}
			}
		}
		// end NBA104
	}

	/**
	 * Obtain the value for Coverage Gender. Holding.Policy.Life.Coverage.LifeParticipant.IssueGender
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA072 New Method
	public void retrieveIssueGender(NbaOinkRequest aNbaOinkRequest) {
		// begin NBA100 NBA104
		if (getNbaTXLife().isAnnuity()) {
			long gender = -1;
			Participant participant = getNbaTXLife().getPrimaryAnnuitantParticipant();
			if (participant != null) {
				gender = participant.getIssueGender();
			}
			aNbaOinkRequest.addValue(gender);
		} else {
			List lifeParticipants = getInsurableLifeParticipants(aNbaOinkRequest);
			int next = 0;
			LifeParticipant lifeParticipant;
			while ((lifeParticipant = getNextLifeParticipant(aNbaOinkRequest, lifeParticipants, next++)) != null) {
				if (lifeParticipant.hasIssueGender()) {
					aNbaOinkRequest.addValue(lifeParticipant.getIssueGender());
				} else {
					aNbaOinkRequest.addValue(-1);
				}
			}
		}
		// end NBA100 NBA104
	}

	/**
	 * Obtain the value for a Benefit Type Code. Holding.Policy.Life.Coverage.CovOption.ProductCode is a productCode.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveBenefitType(NbaOinkRequest aNbaOinkRequest) {
		// NBA104 deleted code
		CovOption covOption = null;
		String covOptionType = aNbaOinkRequest.getQualifier();
		// begin NBA104
		List covoptions = null;
		if (covOptionType.equals(ACCIDENTAL_DEATH_BENEFIT)) {
			covoptions = getCovOptions(NbaOliConstants.OLI_OPTTYPE_ADB);
		} else {
			covoptions = getCovOptions();
		}
		if (covoptions != null) {
			int count = covoptions.size();
			for (int i = 0; i < count; i++) {
				covOption = (CovOption) covoptions.get(i);
				if (covOption != null && covOption.hasProductCode()) {
					aNbaOinkRequest.addValue(covOption.getProductCode().substring(0, 1));
				}
			}
		}
		// end NBA104
	}

	/**
	 * Obtain the value for a Benefit Type Code. Holding.Policy.Life.Coverage.CovOption.ProductCode is a productCode.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveBenefitSubType(NbaOinkRequest aNbaOinkRequest) {
		// NBA104 deleted code
		CovOption covOption = null;
		String covOptionType = aNbaOinkRequest.getQualifier();
		// begin NBA104
		List covoptions = null;
		if (covOptionType.equals(ACCIDENTAL_DEATH_BENEFIT)) {
			covoptions = getCovOptions(NbaOliConstants.OLI_OPTTYPE_ADB);
		} else {
			covoptions = getCovOptions();
		}
		if (covoptions != null) {
			int count = covoptions.size();
			for (int i = 0; i < count; i++) {
				covOption = (CovOption) covoptions.get(i);
				if (covOption != null && covOption.hasProductCode()) {
					aNbaOinkRequest.addValue(covOption.getProductCode().substring(1, 2));	// AXAL3.7.56
				}
			}
		}
		// end NBA104
	}

	/**
	 * Obtain the number of years for a coverage/rider or benefit by calculating the difference in years between Holding.Policy.Life.Coverage.TermDate &
	 * Holding.Policy.Life.Coverage.EffDate or Holding.Policy.Life.Coverage.CovOption.TermDate & Holding.Policy.Life.Coverage.CovOption.EffDate
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA104 New Method
	public void retrieveYears(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = null;
		CovOption covOption = null;
		List coverages = getCoveragesOrRiders(aNbaOinkRequest);
		if (coverages != null && coverages.size() > 0) {
			int count = coverages.size();
			for (int i = 0; i < count; i++) {
				coverage = (Coverage) coverages.get(i);
				if (coverage != null && coverage.hasTermDate() && coverage.hasEffDate()) {
					aNbaOinkRequest.addValue(NbaUtils.calcYearsDiff(coverage.getTermDate(), coverage.getEffDate()));
				} else {
					aNbaOinkRequest.addValue(-1);
				}
			}
		} else {
			List covoptions = getCovOptions(aNbaOinkRequest);
			if (covoptions != null) {
				int count = covoptions.size();
				for (int i = 0; i < count; i++) {
					covOption = (CovOption) covoptions.get(i);
					if (covOption != null && covOption.hasTermDate() && covOption.hasEffDate()) {
						aNbaOinkRequest.addValue(NbaUtils.calcYearsDiff(covOption.getTermDate(), covOption.getEffDate()));
					} else {
						aNbaOinkRequest.addValue(-1);
					}
				}
			}
		}
	}

	/**
	 * Obtain the value for a Person RateClass. OLifE.Party.PersonOrOrganization.Person.PersonExtension.RateClass is the rateclass of the person.
	 * @param aNbaOinkRequest - data request container
	 */
	//SPR1778 New Method
	public void retrievePersonRateClass(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				String rateClass = "";
				PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person);
				if (personExtension != null) {
					if (personExtension.hasRateClass()) {
						rateClass = personExtension.getRateClass();
					}
				}
				aNbaOinkRequest.addValue(rateClass, NbaTableConstants.NBA_RATECLASS);
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for a Person RateClassOverideInd. OLifE.Party.PersonOrOrganization.Person.PersonExtension.RateClassOverideInd is the RateClass
	 * Overide Indicator of the person.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP010 New Method
	public void retrieveRateClassOverrideInd(NbaOinkRequest aNbaOinkRequest) {
		boolean rateClassOverideInd = false;
		Person person = getPerson(aNbaOinkRequest, 0);
		if (person != null) {
			PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person);
			if (personExtension != null) {
				if (personExtension.hasRateClassOverrideInd()) {
					rateClassOverideInd = personExtension.getRateClassOverrideInd();
				}
			}
		}

		aNbaOinkRequest.addValue(rateClassOverideInd);
	}

	/**
	 * Obtain the value for a Person RateClassAppliedFor. OLifE.Party.PersonOrOrganization.Person.PersonExtension.RateClassAppliedFor is the applied
	 * for rateclass of the person.
	 * @param aNbaOinkRequest - data request container
	 */
	//SPR1778 New Method
	public void retrieveRateClassAppliedFor(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				String rateClass = "";
				PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person);
				if (personExtension != null) {
					if (personExtension.hasRateClassAppliedFor()) {
						rateClass = personExtension.getRateClassAppliedFor();
					}
				}
				aNbaOinkRequest.addValue(rateClass, NbaTableConstants.NBA_RATECLASS);
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for Ballooning Hours. OLifE.Party.Risk.LifeStyleActivity.AirSportsExp.BalloonExp.NumberHours is the number of hours.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveBallooningHours(NbaOinkRequest aNbaOinkRequest) {
		LifeStyleActivity activity = null;
		// SPR3290 code deleted
		AviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr aviationExp = null;
		AirSportsExp airSportsExp = null;
		int requestCount = aNbaOinkRequest.getCount(); // SPR3290
		for (int i = 0; i < requestCount; i++) {
			activity = getLifeStyleActivity(aNbaOinkRequest, i);
			if (activity != null) {
				if (activity.hasAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr()) {
					aviationExp = activity.getAviationExpOrRacingExpOrUnderwaterDivingExpOrClimbingExpOr();
					if (aviationExp.isAirSportsExp()) {
						airSportsExp = aviationExp.getAirSportsExp();
						if (airSportsExp.hasBallooningExp()) {
							aNbaOinkRequest.addValue(airSportsExp.getBallooningExp().getNumberHours());
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain the value for TobaccoType. OLifE.Party.Risk.SubstanceUsage.TobaccoType is the tobacco type
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveSubstanceTobaccoType(NbaOinkRequest aNbaOinkRequest) {
		Party party = null;
		Risk risk = null;
		party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			if (party.hasRisk()) {
				risk = getRisk(party);
				int count = risk.getSubstanceUsageCount();
				if (count > 0) {
					for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
						if (i <= count) {
							aNbaOinkRequest.addValue(risk.getSubstanceUsageAt(i).getTobaccoType());
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain the value for TobaccoType. OLifE.Party.Risk.SubstanceUsage.TobaccoType is the tobacco type
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveTobaccoInd(NbaOinkRequest aNbaOinkRequest) {
		Party party = null;
		Risk risk = null;
		party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			if (party.hasRisk()) {
				risk = getRisk(party);
				boolean tobInd = risk.getTobaccoInd();
				aNbaOinkRequest.addValue(tobInd);
			}
		}
	}

	/**
	 * Obtain the value for LifeStyleActivityOther. OLifE.Holding.Party.Attachment.AttachmentData is the value of LifeStyleActivityOther.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveLifeStyleActivityOther(NbaOinkRequest aNbaOinkRequest) {
		Party party = null;
		// SPR3290 code deleted
		party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			int count = party.getAttachmentCount();
			if (count > 0) {
				for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
					if (i <= count) {
						aNbaOinkRequest.addValue(party.getAttachmentAt(i).getAttachmentData().getPCDATA());
					}
				}
			}
		}
	}

	/**
	 * Set the value for PorposeHolding. OLifE.Holding.Purpose is the Purpose for Holding .
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrievePurposeHolding(NbaOinkRequest aNbaOinkRequest) {
		Holding aHolding = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE());
		aNbaOinkRequest.addValue(aHolding.getPurpose());
	}

	/**
	 * Set the value for PurposeCoverage. OlifE.Holding.Policy.Life.Coverage.Purpose is the Purpose for Coverage .
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP014 New Method
	public void retrievePurposeCoverage(NbaOinkRequest aNbaOinkRequest) {
		Life aLife = getLife();
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			int covIndx = getCoverageForParty(aNbaOinkRequest, i);
			if (covIndx == -1) {
				break;
			} else {
				Coverage aCoverage = aLife.getCoverageAt(covIndx); // SPR3290
				aNbaOinkRequest.addValue(aCoverage.getPurpose());
			}
		}
	}

	/**
	 * Set the value for ReqCategory. OLifE.Holding.Policy.RequirementInfo.ReqCategory is the Reqirement Category .
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveReqCategory(NbaOinkRequest aNbaOinkRequest) {
		List reqList = getRequirementInfos(aNbaOinkRequest); //SPR3353
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			RequirementInfo aRequirementInfo = getRequirementInfo(reqList, i); //SPR3353
			if (aRequirementInfo != null) {
				aNbaOinkRequest.addValue(aRequirementInfo.getReqCategory());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for AbbrName from OLifE.Party.Organization.AbbrName
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveAbbrName(NbaOinkRequest aNbaOinkRequest) {
		int count = aNbaOinkRequest.getCount();
		for (int i = 0; i < count; i++) {
			Organization organization = getOrganization(aNbaOinkRequest, i);
			if (organization != null) {
				aNbaOinkRequest.addValue(organization.getAbbrName());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for EstabDate from OLifE.Party.Organization.EstabDate
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveEstabDate(NbaOinkRequest aNbaOinkRequest) {
		int count = aNbaOinkRequest.getCount();
		for (int i = 0; i < count; i++) {
			Organization organization = getOrganization(aNbaOinkRequest, i);
			if (organization != null) {
				aNbaOinkRequest.addValue(organization.getEstabDate());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for NumOwners from OLifE.Party.Organization.NumOwners
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveNumOwners(NbaOinkRequest aNbaOinkRequest) {
		int count = aNbaOinkRequest.getCount();
		for (int i = 0; i < count; i++) {
			Organization organization = getOrganization(aNbaOinkRequest, i);
			if (organization != null) {
				aNbaOinkRequest.addValue(organization.getNumOwners());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for OrgForm from OLifE.Party.Organization.OrgForm
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveOrgForm(NbaOinkRequest aNbaOinkRequest) {
		int count = aNbaOinkRequest.getCount();
		for (int i = 0; i < count; i++) {
			Organization organization = getOrganization(aNbaOinkRequest, i);
			if (organization != null) {
				aNbaOinkRequest.addValue(organization.getOrgForm());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for EstGrossAnnualOtherIncome from OLifE.Party.Person.EstGrossAnnualOtherIncome
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP014 New Method
	public void retrieveEstGrossAnnualOtherIncome(NbaOinkRequest aNbaOinkRequest) {
		int count = aNbaOinkRequest.getCount();
		for (int i = 0; i < count; i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				aNbaOinkRequest.addValue(person.getEstGrossAnnualOtherIncome());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for SmokingFrequencyNumber from OLifE.Party.Person.SmokingFrequencyNumber
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveSmokingFrequencyNumber(NbaOinkRequest aNbaOinkRequest) {
		int count = aNbaOinkRequest.getCount();
		for (int i = 0; i < count; i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				aNbaOinkRequest.addValue(person.getSmokingFrequencyNumber());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for CurrentAssetsAmt from OLifE.Party.Organization.OrganizationFinancialData.CurrentAssetsAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveCurrentAssetsAmt(NbaOinkRequest aNbaOinkRequest) {
		int count = aNbaOinkRequest.getCount();
		for (int i = 0; i < count; i++) {
			OrganizationFinancialData orgFinData = getOrganizationFinancialData(aNbaOinkRequest, i);
			if (orgFinData != null) {
				aNbaOinkRequest.addValue(orgFinData.getCurrentAssetsAmt());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for CurrentLiabilitiesAmt from OLifE.Party.Organization.OrganizationFinancialData.CurrentLiabilitiesAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveCurrentLiabilitiesAmt(NbaOinkRequest aNbaOinkRequest) {
		int count = aNbaOinkRequest.getCount();
		for (int i = 0; i < count; i++) {
			OrganizationFinancialData orgFinData = getOrganizationFinancialData(aNbaOinkRequest, i);
			if (orgFinData != null) {
				aNbaOinkRequest.addValue(orgFinData.getCurrentLiabilitiesAmt());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for PrevYrNetIncomeAmt from OLifE.Party.Organization.OrganizationFinancialData.PrevYrNetIncomeAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrievePrevYrNetIncomeAmt(NbaOinkRequest aNbaOinkRequest) {
		int count = aNbaOinkRequest.getCount();
		for (int i = 0; i < count; i++) {
			OrganizationFinancialData orgFinData = getOrganizationFinancialData(aNbaOinkRequest, i);
			if (orgFinData != null) {
				aNbaOinkRequest.addValue(orgFinData.getPrevYrNetIncomeAmt());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for PrevYrTaxableEarningsAmt from OLifE.Party.Organization.OrganizationFinancialData.PrevYrTaxableEarningsAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP014 New Method
	public void retrievePrevYrTaxableEarningsAmt(NbaOinkRequest aNbaOinkRequest) {
		int count = aNbaOinkRequest.getCount();
		for (int i = 0; i < count; i++) {
			OrganizationFinancialData orgFinData = getOrganizationFinancialData(aNbaOinkRequest, i);
			if (orgFinData != null) {
				aNbaOinkRequest.addValue(orgFinData.getPrevYrTaxableEarningsAmt());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for YrEndNetWorthAmt from OLifE.Party.Organization.OrganizationFinancialData.YrEndNetWorthAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP014 New Method
	public void retrieveYrEndNetWorthAmt(NbaOinkRequest aNbaOinkRequest) {
		int count = aNbaOinkRequest.getCount();
		for (int i = 0; i < count; i++) {
			OrganizationFinancialData orgFinData = getOrganizationFinancialData(aNbaOinkRequest, i);
			if (orgFinData != null) {
				aNbaOinkRequest.addValue(orgFinData.getYrEndNetWorthAmt());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for OccupClass from OLifE.Party.Risk.Employment.OccupClass
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP017 New Method
	public void retrieveOccupClass(NbaOinkRequest aNbaOinkRequest) {
		int count = aNbaOinkRequest.getCount();
		for (int i = 0; i < count; i++) {
			Employment employment = getEmploymentForKeyPerson(aNbaOinkRequest, i);
			if (employment != null) {
				aNbaOinkRequest.addValue(employment.getOccupClass());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for AgeAtDeath from OLifE.Party.Risk.FamilyIllness.AgeAtDeath
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP013 New Method
	public void retrieveAgeAtDeath(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null && party.hasRisk()) {
			ArrayList list = getRisk(party).getFamilyIllness();
			int count = list.size();
			for (int i = 0; i < count; i++) {
				FamilyIllness familyIllness = (FamilyIllness) list.get(i);
				if (familyIllness != null) {
					aNbaOinkRequest.addValue(familyIllness.getAgeAtDeath());
				} else {
					break;
				}
			}
		}
	}

	/**
	 * Obtain the value for AgeIfLiving from OLifE.Party.Risk.FamilyIllness.AgeIfLiving
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP013 New Method
	public void retrieveAgeIfLiving(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null && party.hasRisk()) {
			ArrayList list = getRisk(party).getFamilyIllness();
			int count = list.size();
			for (int i = 0; i < count; i++) {
				FamilyIllness familyIllness = (FamilyIllness) list.get(i);
				if (familyIllness != null) {
					aNbaOinkRequest.addValue(familyIllness.getAgeIfLiving());
				} else {
					break;
				}
			}
		}
	}

	/**
	 * Obtain the value for FamilyDiagnosis/Medical Condition. from OLifE.Party.Risk.FamilyIllness.Diagnosis
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP013 New Method
	public void retrieveDiagnosis(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null && party.hasRisk()) {
			ArrayList list = getRisk(party).getFamilyIllness();
			int count = list.size();
			for (int i = 0; i < count; i++) {
				FamilyIllness familyIllness = (FamilyIllness) list.get(i);
				if (familyIllness != null) {
					aNbaOinkRequest.addValue(familyIllness.getDiagnosis());
				} else {
					break;
				}
			}
		}
	}

	/**
	 * Obtain the value for OnsetAge. from OLifE.Party.Risk.FamilyIllness.OnsetAge
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP013 New Method
	public void retrieveOnsetAge(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null && party.hasRisk()) {
			ArrayList list = getRisk(party).getFamilyIllness();
			int count = list.size();
			for (int i = 0; i < count; i++) {
				FamilyIllness familyIllness = (FamilyIllness) list.get(i);
				if (familyIllness != null) {
					aNbaOinkRequest.addValue(familyIllness.getOnsetAge());
				} else {
					break;
				}
			}
		}
	}

	/**
	 * Obtain the value for NumberEmployees from OLifE.Party.Organization.OrganizationFinancialData.NumEmployees
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP017 New Method
	public void retrieveNumEmployees(NbaOinkRequest aNbaOinkRequest) {
		int count = aNbaOinkRequest.getCount();
		for (int i = 0; i < count; i++) {
			OrganizationFinancialData orgFinData = getOrganizationFinancialData(aNbaOinkRequest, i);
			if (orgFinData != null) {
				aNbaOinkRequest.addValue(orgFinData.getNumEmployees());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for YearEndNetProfit from OLifE.Party.Organization.OrganizationFinancialData.YrEndNetProfitAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP017 New Method
	public void retrieveYrEndNetProfitAmt(NbaOinkRequest aNbaOinkRequest) {
		int count = aNbaOinkRequest.getCount();
		for (int i = 0; i < count; i++) {
			OrganizationFinancialData orgFinData = getOrganizationFinancialData(aNbaOinkRequest, i);
			if (orgFinData != null) {
				aNbaOinkRequest.addValue(orgFinData.getYrEndNetProfitAmt());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for a Person BonusCommAmt. OLifE.Party.Person..OLifEExtension.PersonExtension.BonusCommAmt is the BonusCommAmt of the person.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP017 New Method
	public void retrieveBonusCommAmt(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person);
				if (personExtension != null) {
					aNbaOinkRequest.addValue(personExtension.getBonusCommAmt());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for a Person OccupationYrExperience. Olife.Party.Person.OLifEExtension.PersonExtension.OccupationYrExperience is the
	 * OccupationYrExperience of the person.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP017 New Method
	public void retrieveOccupationYrExperience(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person);
				if (personExtension != null) {
					aNbaOinkRequest.addValue(personExtension.getOccupationYrExperience());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for KeyPersonsInsInd from OLifE.Party.Organization.OLifEExtension.OrganizationExtension.KeyPersonsInsInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP017 New Method
	public void retrieveKeyPersonsInsInd(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Organization organization = getOrganization(aNbaOinkRequest, i);
			if (organization != null) {
				OrganizationExtension organizationExtension = NbaUtils.getFirstOrganizationExtension(organization);
				if (organizationExtension != null) {
					aNbaOinkRequest.addValue(organizationExtension.getKeyPersonsInsInd());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for AccumOwnerManagementSalary from
	 * OLifE.Party.Organization.OrganizationFinancialData.OLifEExtension.OrganizationFinancialDataExtension.AccumOwnerManagementSalary
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP017 New Method
	public void retrieveAccumOwnerManagementSalary(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			OrganizationFinancialData organizationFinancialData = getOrganizationFinancialData(aNbaOinkRequest, i);
			if (organizationFinancialData != null) {
				OrganizationFinancialDataExtension organizationFinancialDataExtension = NbaUtils
						.getFirstOrganizationFinancialDataExtension(organizationFinancialData);
				if (organizationFinancialDataExtension != null) {
					aNbaOinkRequest.addValue(organizationFinancialDataExtension.getAccumOwnerManagementSalary());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for FullTimeInd from OLifE.Party.Employment.OLifEExtension.EmploymentExtension.FullTimeInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP017 New Method
	public void retrieveFullTimeInd(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Employment employment = getEmploymentForKeyPerson(aNbaOinkRequest, i);
			if (employment != null) {
				EmploymentExtension employmentExtension = NbaUtils.getFirstEmploymentExtension(employment);
				if (employmentExtension != null) {
					aNbaOinkRequest.addValue(employmentExtension.getFullTimeInd());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for NumYrsToReplace from OLifE.Party.Employment.OLifEExtension.EmploymentExtension.NumYrsToReplace
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP017 New Method
	public void retrieveNumYrsToReplace(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Employment employment = getEmploymentForKeyPerson(aNbaOinkRequest, i);
			if (employment != null) {
				EmploymentExtension employmentExtension = NbaUtils.getFirstEmploymentExtension(employment);
				if (employmentExtension != null) {
					aNbaOinkRequest.addValue(employmentExtension.getNumYrsToReplace());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for PriorBusAmt. OlifE.Party.RiskExtension.PriorBusAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP017 New Method
	public void retrievePriorBusAmt(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk aRisk = getRisk(party);
			if (aRisk != null) {
				int index_extension = getExtensionIndex(aRisk.getOLifEExtension(), RISK_EXTN);
				if (index_extension != -1) {
					RiskExtension extension = aRisk.getOLifEExtensionAt(index_extension).getRiskExtension();
					if (extension != null) {
						aNbaOinkRequest.addValue(extension.getPriorBusAmt());
					}
				}
			}
		}
	}

	/**
	 * Obtain the value for BuySellPersonsInsInd from OLifE.Party.Organization.OLifEExtension.OrganizationExtension.BuySellPersonsInsInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP018 New Method
	public void retrieveBuySellPersonsInsInd(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Organization organization = getOrganization(aNbaOinkRequest, i);
			if (organization != null) {
				OrganizationExtension organizationExtension = NbaUtils.getFirstOrganizationExtension(organization);
				if (organizationExtension != null) {
					aNbaOinkRequest.addValue(organizationExtension.getBuySellPersonsInsInd());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for AccumBuySellCoverage from
	 * OLifE.Party.Organization.OrganizationFinancialData.OLifEExtension.OrganizationFinancialDataExtension.AccumBuySellCoverage
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP018 New Method
	public void retrieveAccumBuySellCoverage(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			OrganizationFinancialData organizationFinancialData = getOrganizationFinancialData(aNbaOinkRequest, i);
			if (organizationFinancialData != null) {
				OrganizationFinancialDataExtension organizationFinancialDataExtension = NbaUtils
						.getFirstOrganizationFinancialDataExtension(organizationFinancialData);
				if (organizationFinancialDataExtension != null) {
					aNbaOinkRequest.addValue(organizationFinancialDataExtension.getAccumBuySellCoverage());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for NetWorthValuationCode from
	 * OLifE.Party.Organization.OrganizationFinancialData.OLifEExtension.OrganizationFinancialDataExtension.NetWorthValuationCode
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP018 New Method
	public void retrieveNetWorthValuationCode(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			OrganizationFinancialData organizationFinancialData = getOrganizationFinancialData(aNbaOinkRequest, i);
			if (organizationFinancialData != null) {
				OrganizationFinancialDataExtension organizationFinancialDataExtension = NbaUtils
						.getFirstOrganizationFinancialDataExtension(organizationFinancialData);
				if (organizationFinancialDataExtension != null) {
					aNbaOinkRequest.addValue(organizationFinancialDataExtension.getNetWorthValuationCode());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for TravelCountry. from OLifE.Party.Risk.LifeStyleActivity.ForeignTravel.TravelCountry
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP022 New Method
	public void retrieveTravelCountry(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null && party.hasRisk()) {
			ArrayList list = getRisk(party).getLifeStyleActivity();
			int count = list.size();
			for (int i = 0; i < count; i++) {
				LifeStyleActivity lStyleActivity = (LifeStyleActivity) list.get(i);
				if (lStyleActivity != null) {
					ForeignTravel foreignTravel = getForeignTravel(lStyleActivity);
					if (foreignTravel != null) {
						aNbaOinkRequest.addValue(foreignTravel.getTravelCountry());
					}
				} else {
					break;
				}
			}
		}
	}

	/**
	 * Obtain the value for TravelMaxTime. from OLifE.Party.Risk.LifeStyleActivity.ForeignTravel.TravelMaxTime
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP022 New Method
	public void retrieveTravelMaxTime(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null && party.hasRisk()) {
			ArrayList list = getRisk(party).getLifeStyleActivity();
			int count = list.size();
			for (int i = 0; i < count; i++) {
				LifeStyleActivity lStyleActivity = (LifeStyleActivity) list.get(i);
				if (lStyleActivity != null) {
					ForeignTravel foreignTravel = getForeignTravel(lStyleActivity);
					if (foreignTravel != null) {
						aNbaOinkRequest.addValue(foreignTravel.getTravelMaxTime());
					}
				} else {
					break;
				}
			}
		}
	}

	/**
	 * Obtain the value for LifeStyleActivityType from OLifE.Party.Risk.LifeStyleActivity.LifeStyleActivityType
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP009 New Method
	public void retrieveLifeStyleActivityType(NbaOinkRequest aNbaOinkRequest) {
		int index = aNbaOinkRequest.getElementIndexFilter();
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk risk = party.getRisk();
			if (risk != null) {
				ArrayList lifeStyleActivityList = risk.getLifeStyleActivity();
				if (index <= lifeStyleActivityList.size() && lifeStyleActivityList.size() > 0) {  //AXAL3.7.07
					aNbaOinkRequest.addValue(((LifeStyleActivity) lifeStyleActivityList.get(index)).getLifeStyleActivityType());
				}
			}
		}
	}

	/**
	 * Obtain the value for SafetyStandardsInd from OLifE.Party.Risk.LifeStyleActivity.AirSportsExp.SafetyStandardsInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveSafetyStandardsInd(NbaOinkRequest aNbaOinkRequest) {
		int count = aNbaOinkRequest.getCount();
		for (int i = 0; i < count; i++) {
			AirSportsExp airSportsExp = getAirSportsExp(aNbaOinkRequest, i);
			if (airSportsExp != null) {
				aNbaOinkRequest.addValue(airSportsExp.getSafetyStandardsInd());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for AnnualSalary. OLifE.Party.Employment.AnnualSalary
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP017 New Method
	public void retrieveAnnualSalary(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Employment anEmployment = getEmploymentForKeyPerson(aNbaOinkRequest, i);
			if (anEmployment != null) {
				aNbaOinkRequest.addValue(anEmployment.getAnnualSalary());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for CorporateStockOwnedPct. OLifE.Party.Employment.CorporateStockOwnedPct
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP014 New Method
	public void retrieveCorporateStockOwnedPct(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		ArrayList employmentList = null;
		int employmentCount = 0;
		Employment employment = null;
		if (party != null) {
			employmentList = party.getEmployment();
			employmentCount = employmentList.size();
			for (int i = 0; i < employmentCount; i++) {
				employment = (Employment) employmentList.get(i);
				//Pick Up the first Employment object that doesn't have a termination date
				if (employment != null && employment.getTerminationDate() == null) {
					aNbaOinkRequest.addValue(employment.getCorporateStockOwnedPct());
					break;
				}
			}
		}
	}

	/**
	 * Obtain the value for HireDate. OLifE.Party.Employment.HireDate
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveHireDate(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Employment anEmployment = getEmployment(aNbaOinkRequest, i);
			if (anEmployment != null) {
				aNbaOinkRequest.addValue(anEmployment.getHireDate());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for TerminationDate. OLifE.Party.Employment.TerminationDate
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveTerminationDate(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Employment anEmployment = getEmployment(aNbaOinkRequest, i);
			if (anEmployment != null) {
				aNbaOinkRequest.addValue(anEmployment.getTerminationDate());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for FirstDiastolicBPReading. OLifE.Party.Risk.MedicalExam.FirstDiastolicBPReading
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP001 New Method
	public void retrieveFirstDiastolicBPReading(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalExam aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				aNbaOinkRequest.addValue(aMedicalExam.getFirstDiastolicBPReading());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for SecondDiastolicBPReading. OLifE.Party.Risk.MedicalExam.SecondDiastolicBPReading
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP001 New Method
	public void retrieveSecondDiastolicBPReading(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalExam aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				aNbaOinkRequest.addValue(aMedicalExam.getSecondDiastolicBPReading());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for ThirdDiastolicBPReading. OLifE.Party.Risk.MedicalExam.ThirdDiastolicBPReading
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP001 New Method
	public void retrieveThirdDiastolicBPReading(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalExam aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				aNbaOinkRequest.addValue(aMedicalExam.getThirdDiastolicBPReading());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for FirstSystolicBPReading. OLifE.Party.Risk.MedicalExam.FirstSystolicBPReading
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP001 New Method
	public void retrieveFirstSystolicBPReading(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalExam aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				aNbaOinkRequest.addValue(aMedicalExam.getFirstSystolicBPReading());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for FirstPulseReading. OLifE.Party.Risk.MedicalExam.FirstPulseReading
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveFirstPulseReading(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalExam aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				aNbaOinkRequest.addValue(aMedicalExam.getFirstPulseReading());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for SecondSystolicBPReading. OLifE.Party.Risk.MedicalExam.SecondSystolicBPReading
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP001 New Method
	public void retrieveSecondSystolicBPReading(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalExam aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				aNbaOinkRequest.addValue(aMedicalExam.getSecondSystolicBPReading());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for ThirdSystolicBPReading. OLifE.Party.Risk.MedicalExam.ThirdSystolicBPReading
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP001 New Method
	public void retrieveThirdSystolicBPReading(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalExam aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				aNbaOinkRequest.addValue(aMedicalExam.getThirdSystolicBPReading());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for PulseIrregularInd. OLifE.Party.Risk.MedicalExam.PulseIrregularInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrievePulseIrregularInd(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalExam aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				boolean ind = aMedicalExam.getPulseIrregularInd();
				aNbaOinkRequest.addValue(ind);
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for PrescriptionDosageStrength. OLifE.Party.Risk.PrescriptionDrug.PrescriptionDosageStrength
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrievePrescriptionDosageStrength(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			PrescriptionDrug aPrescriptionDrug = getPrescriptionDrug(aNbaOinkRequest, i);
			if (aPrescriptionDrug != null) {
				aNbaOinkRequest.addValue(aPrescriptionDrug.getPrescriptionDosageStrength());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for PrescriptionDosageUnit. OLifE.Party.Risk.PrescriptionDrug.PrescriptionDosageUnit
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrievePrescriptionDosageUnit(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			PrescriptionDrug aPrescriptionDrug = getPrescriptionDrug(aNbaOinkRequest, i);
			if (aPrescriptionDrug != null) {
				aNbaOinkRequest.addValue(aPrescriptionDrug.getPrescriptionDosageUnit());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for ConditionType. OLifE.Party.Risk.MedicalCondition.ConditionType
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP007 New Method
	public void retrieveConditionType(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalCondition aMedicalCondition = getMedicalCondition(aNbaOinkRequest, i);
			if (aMedicalCondition != null) {
				aNbaOinkRequest.addValue(aMedicalCondition.getConditionType());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for ConditionID. OLifE.Party.Risk.MedicalCondition.ConditionID
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP007 New Method
	public void retrieveConditionID(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalCondition aMedicalCondition = getMedicalCondition(aNbaOinkRequest, i);
			if (aMedicalCondition != null) {
				aNbaOinkRequest.addValue(aMedicalCondition.getId());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for ConditionDescription. OLifE.Party.Risk.MedicalCondition.ConditionDescription
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP007 New Method
	public void retrieveConditionDescription(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalCondition aMedicalCondition = getMedicalCondition(aNbaOinkRequest, i);
			if (aMedicalCondition != null) {
				aNbaOinkRequest.addValue(aMedicalCondition.getConditionDescription());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for ConditionOnsetDate. OLifE.Party.Risk.MedicalCondition.ConditionOnsetDate
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP007 New Method
	public void retrieveConditionOnsetDate(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalCondition aMedicalCondition = getMedicalCondition(aNbaOinkRequest, i);
			if (aMedicalCondition != null) {
				aNbaOinkRequest.addValue(aMedicalCondition.getConditionOnsetDate());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for ExamineeDesc. OLifE.Party.Risk.MedicalCondition.ExamineeDesc
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP007 New Method
	public void retrieveExamineeDesc(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalCondition aMedicalCondition = getMedicalCondition(aNbaOinkRequest, i);
			if (aMedicalCondition != null) {
				aNbaOinkRequest.addValue(aMedicalCondition.getExamineeDesc());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for PrimaryPhysicianName. OLifE.Party.Risk.MedicalCondition.PrimaryPhysicianName
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP007 New Method
	public void retrievePrimaryPhysicianName(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalCondition aMedicalCondition = getMedicalCondition(aNbaOinkRequest, i);
			if (aMedicalCondition != null) {
				String partyId = aMedicalCondition.getPrimaryPhysicianID();
				NbaParty nbaParty = nbaTXLife.getParty(partyId);
				aNbaOinkRequest.addValue(nbaParty.getDisplayName());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for TestType. OLifE.Party.Risk.MedicalPrevention.TestType
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP007 New Method
	//ACP007 Modified to retrieve List
	public void retrieveTestType(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			ArrayList mpList = getMedicalPreventions(aNbaOinkRequest, i);
			if (mpList != null) {
				for (int j = 0; j < mpList.size(); j++) {
					MedicalPrevention aMedicalPrevention = (MedicalPrevention) mpList.get(j);
					aNbaOinkRequest.addValue(aMedicalPrevention.getTestType());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for TestDate. OLifE.Party.Risk.MedicalPrevention.TestDate
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP007 New Method
	//ACP007 Modified to retrieve List
	public void retrieveTestDate(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			ArrayList mpList = getMedicalPreventions(aNbaOinkRequest, i);
			if (mpList != null) {
				for (int j = 0; j < mpList.size(); j++) {
					MedicalPrevention aMedicalPrevention = (MedicalPrevention) mpList.get(j);
					aNbaOinkRequest.addValue(aMedicalPrevention.getTestDate());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for TreatmentAmt. OLifE.Party.Risk.MedicalCondition.MedicalTreatment.TreatmentAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP007 New Method
	//ACP007 Modified to retrieve List
	public void retrieveTreatmentAmt(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			ArrayList mtList = getMedicalTreatments(aNbaOinkRequest, i);
			if (mtList != null) {
				for (int j = 0; j < mtList.size(); j++) {
					MedicalTreatment aMedicalTreatment = (MedicalTreatment) mtList.get(j);
					aNbaOinkRequest.addValue(aMedicalTreatment.getTreatmentAmt());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for TreatmentType. OLifE.Party.Risk.MedicalCondition.MedicalTreatment.TreatmentType
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP007 New Method
	//ACP007 Modified to retrieve List
	public void retrieveTreatmentType(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			ArrayList mtList = getMedicalTreatments(aNbaOinkRequest, i);
			if (mtList != null) {
				for (int j = 0; j < mtList.size(); j++) {
					MedicalTreatment aMedicalTreatment = (MedicalTreatment) mtList.get(j);
					aNbaOinkRequest.addValue(aMedicalTreatment.getTreatmentType());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for Medication. OLifE.Party.Risk.MedicalCondition.MedicalTreatment.Medication
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP007 New Method
	//ACP007 Modified to retrieve List
	public void retrieveMedication(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			ArrayList mtList = getMedicalTreatments(aNbaOinkRequest, i);
			if (mtList != null) {
				for (int j = 0; j < mtList.size(); j++) {
					MedicalTreatment aMedicalTreatment = (MedicalTreatment) mtList.get(j);
					aNbaOinkRequest.addValue(aMedicalTreatment.getMedication());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for TreatmentFrequencyMode. OLifE.Party.Risk.MedicalCondition.MedicalTreatment.TreatmentFrequencyMode
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP007 New Method
	//ACP007 Modified to retrieve List
	public void retrieveTreatmentFrequencyMode(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			ArrayList mtList = getMedicalTreatments(aNbaOinkRequest, i);
			if (mtList != null) {
				for (int j = 0; j < mtList.size(); j++) {
					MedicalTreatment aMedicalTreatment = (MedicalTreatment) mtList.get(j);
					aNbaOinkRequest.addValue(aMedicalTreatment.getTreatmentFrequencyMode());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for TreatmentStartDate. OLifE.Party.Risk.MedicalCondition.MedicalTreatment.TreatmentStartDate
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP007 New Method
	//ACP007 Modified to retrieve List
	public void retrieveTreatmentStartDate(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			ArrayList mtList = getMedicalTreatments(aNbaOinkRequest, i);
			if (mtList != null) {
				for (int j = 0; j < mtList.size(); j++) {
					MedicalTreatment aMedicalTreatment = (MedicalTreatment) mtList.get(j);
					aNbaOinkRequest.addValue(aMedicalTreatment.getTreatmentStartDate());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for DateLastSeen. OLifE.Party.Risk.MedicalCondition.MedicalTreatment.DateLastSeen
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP007 New Method
	//ACP007 Modified to retrieve List
	public void retrieveDateLastSeen(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			//Begin ALS3030
			MedicalCondition medicalCondition = null;
			Risk risk = null;
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				risk = getRisk(party);
				if (risk != null) {
					medicalCondition = getMedicalCondition(aNbaOinkRequest, i);
					if (medicalCondition != null) {
						aNbaOinkRequest.addValue(medicalCondition.getDateLastSeen());
					}else { //End ALS3030
						break;
					}
				}
			}
		}
	}

	/**
	 * Obtain the value for SubstanceAmt. OLifE.Party.Risk.SubstanceUsage.SubstanceAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveSubstanceAmt(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			SubstanceUsage aSubstanceUsage = getSubstanceUsage(aNbaOinkRequest, i);
			if (aSubstanceUsage != null) {
				aNbaOinkRequest.addValue(aSubstanceUsage.getSubstanceAmt());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for SubstanceDesc. OLifE.Party.Risk.SubstanceUsage.SubstanceDesc
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveSubstanceDesc(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			SubstanceUsage aSubstanceUsage = getSubstanceUsage(aNbaOinkRequest, i);
			if (aSubstanceUsage != null) {
				aNbaOinkRequest.addValue(aSubstanceUsage.getSubstanceDesc());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for SubstanceEndDate. OLifE.Party.Risk.SubstanceUsage.SubstanceEndDate
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveSubstanceEndDate(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			SubstanceUsage aSubstanceUsage = getSubstanceUsage(aNbaOinkRequest, i);
			if (aSubstanceUsage != null) {
				aNbaOinkRequest.addValue(aSubstanceUsage.getSubstanceEndDate());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for SubstanceType. OLifE.Party.Risk.SubstanceUsage.SubstanceType
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP009 New Method
	public void retrieveSubstanceType(NbaOinkRequest aNbaOinkRequest) {
		int index = aNbaOinkRequest.getElementIndexFilter();
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk risk = party.getRisk();
			if (risk != null) {
				ArrayList substanceUsageList = risk.getSubstanceUsage();
				if (index <= substanceUsageList.size() && (substanceUsageList.size() > 0)) {
					aNbaOinkRequest.addValue(((SubstanceUsage) substanceUsageList.get(index)).getSubstanceType());
				}
			}
		}
	}

	/**
	 * Obtain the value for ViolationDate. OLifE.Party.Risk.Violation.ViolationDate
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveViolationDate(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Violation aViolation = getViolation(aNbaOinkRequest, i);
			if (aViolation != null) {
				aNbaOinkRequest.addValue(aViolation.getViolationDate());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for ViolationType. OLifE.Party.Risk.Violation.ViolationType
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP009 New Method
	public void retrieveViolationType(NbaOinkRequest aNbaOinkRequest) {
		int index = aNbaOinkRequest.getElementIndexFilter();
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk risk = party.getRisk();
			if (risk != null) {
				ArrayList violationList = risk.getViolation();
				if (index <= violationList.size()) {
					aNbaOinkRequest.addValue(((Violation) violationList.get(index)).getViolationType());
				}
			}
		}
	}

	/**
	 * Obtain the value for AppOwnerSignatureOK. OLifE.Holding.Policy.ApplicationInfo.AppOwnerSignatureOK
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveAppOwnerSignatureOK(NbaOinkRequest aNbaOinkRequest) {
		//start loop for the no of occurrences
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { //check whether the applicationInfo object exist or not
			if (getApplicationInfo() != null && getApplicationInfo().hasAppOwnerSignatureOK()) {
				//get the value for AppOwnerSignatureOK from the ApplicationInfo object.
				aNbaOinkRequest.addValue(getApplicationInfo().getAppOwnerSignatureOK());
			} else {
				aNbaOinkRequest.addUnknownValue("");
			} //end if
		} //end loop
	} //end method

	/**
	 * Obtain the value for AppProposedInsuredSignatureOK. OLifE.Holding.Policy.ApplicationInfo.AppProposedInsuredSignatureOK
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP004 New Method
	public void retrieveAppProposedInsuredSignatureOK(NbaOinkRequest aNbaOinkRequest) {
		//start loop for the no of occurrences
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { //check whether the applicationInfo object exist or not
			if (getApplicationInfo() != null) { //SPR3047
				//get the value for AppProposedInsuredSignatureOK from the ApplicationInfo object.
				aNbaOinkRequest.addValue(getApplicationInfo().getAppProposedInsuredSignatureOK());
			} else {
				aNbaOinkRequest.addUnknownValue("");
			} //end if
		} //end loop
	} //end method

	/**
	 * Obtain the value for CarrierInputDate. OLifE.Holding.Policy.ApplicationInfo.CarrierInputDate
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveCarrierInputDate(NbaOinkRequest aNbaOinkRequest) {
		//start loop for the no of occurrences
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { //check whether the applicationInfo object exist or not
			if (getApplicationInfo() != null && getApplicationInfo().hasCarrierInputDate()) {
				//get the value for CarrierInputDate from the ApplicationInfo object.
				aNbaOinkRequest.addValue(getApplicationInfo().getCarrierInputDate());
			} else {
				aNbaOinkRequest.addUnknownValue("");
			} //end if
		} //end loop
	} //end method

	/**
	 * Obtain the value for CaseLocationDate. OLifE.Holding.Policy.ApplicationInfo.CaseLocationDate
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveCaseLocationDate(NbaOinkRequest aNbaOinkRequest) {
		//start loop for the no of occurrences
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { //check whether the applicationInfo object exist or not
			if (getApplicationInfo() != null && getApplicationInfo().hasCaseLocationDate()) {
				//get the value for CaseLocationDate from the ApplicationInfo object.
				aNbaOinkRequest.addValue(getApplicationInfo().getCaseLocationDate());
			} else {
				aNbaOinkRequest.addUnknownValue("");
			} //end if
		} //end loop
	} //end method

	/**
	 * Obtain the value for FormalAppInd. OLifE.Holding.Policy.ApplicationInfo.FormalAppInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveFormalAppInd(NbaOinkRequest aNbaOinkRequest) {
		//start loop for the no of occurrences
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { //check whether the applicationInfo object exist or not
			if (getApplicationInfo() != null && getApplicationInfo().hasFormalAppInd()) {
				//get the value for FormalAppInd from the ApplicationInfo object.
				aNbaOinkRequest.addValue(getApplicationInfo().getFormalAppInd());
			} else {
				aNbaOinkRequest.addUnknownValue("");
			} //end if
		} //end loop
	} //end method

	/**
	 * Obtain the value for HOCompletionDate. OLifE.Holding.Policy.ApplicationInfo.HOCompletionDate
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveHOCompletionDate(NbaOinkRequest aNbaOinkRequest) {
		//start loop for the no of occurrences
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { //check whether the applicationInfo object exist or not
			if (getApplicationInfo() != null && getApplicationInfo().hasHOCompletionDate()) {
				//get the value for HOCompletionDate from the ApplicationInfo object.
				aNbaOinkRequest.addValue(getApplicationInfo().getHOCompletionDate());
			} else {
				aNbaOinkRequest.addUnknownValue("");
			} //end if
		} //end loop
	} //end method

	/**
	 * Obtain the value for ReinsuranceInd. OLifE.Holding.Policy.ReinsuranceInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveReinsuranceInd(NbaOinkRequest aNbaOinkRequest) {
		//start loop for the no of occurrences
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { //check whether the policy object exist or not
			if (getPolicy() != null && getPolicy().hasReinsuranceInd()) {
				//get the value for ReinsuranceInd from the policy object.
				aNbaOinkRequest.addValue(getPolicy().getReinsuranceInd());
			} else {
				aNbaOinkRequest.addUnknownValue("");
			} //end if
		} //end loop
	} //end method

	/**
	 * Obtain the value for ReinsuranceType by evaluating the ReinsuranceInfo object in the base coverage. OLifE.Holding.Policy.ReinsuranceInd
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA130 New Method
	public void retrieveReinsuranceType(NbaOinkRequest aNbaOinkRequest) {
		Coverage baseCoverage = getBaseCoverage();
		if (null != baseCoverage) {
			Iterator reinInfoIter = baseCoverage.getReinsuranceInfo().iterator();
			while (reinInfoIter.hasNext()) {
				ReinsuranceInfo reinInfo = (ReinsuranceInfo) reinInfoIter.next();
				if ((null == reinInfo.getCarrierPartyID() || reinInfo.getCarrierPartyID().length() == 0) && reinInfo.hasReinsuranceRiskBasis()) {//ALII151
					aNbaOinkRequest.addValue(reinInfo.getReinsuranceRiskBasis());
					return;
				}
			}
			aNbaOinkRequest.addValue(0); //SPR3290
		}
	}

	/**
	 * Obtain the value for FirstName. Olife.Party.PriorName.FirstName
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP006 New Method
	public void retrievePriorFirstName(NbaOinkRequest aNbaOinkRequest) {
		//start loop for the no of occurrences
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			PriorName priorName = getPriorName(aNbaOinkRequest, i);
			//check whether the policy object exist or not
			if (priorName != null && priorName.hasFirstName()) {
				//get the value for FirstName from the PriorName object.
				aNbaOinkRequest.addValue(priorName.getFirstName());
			} else {
				aNbaOinkRequest.addUnknownValue("");
			} //end if
		} //end loop
	} //end method

	/**
	 * Obtain the value for LastName. Olife.Party.PriorName.LastName
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP006 New Method
	public void retrievePriorLastName(NbaOinkRequest aNbaOinkRequest) {
		//start loop for the no of occurrences
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			PriorName priorName = getPriorName(aNbaOinkRequest, i);
			//check whether the policy object exist or not
			if (priorName != null && priorName.hasLastName()) {
				//get the value for LastName from the PriorName object.
				aNbaOinkRequest.addValue(priorName.getLastName());
			} else {
				aNbaOinkRequest.addUnknownValue("");
			} //end if
		} //end loop
	} //end method

	/**
	 * Set the value for LifeCovOptTypeCode. OLifE.Holding.Policy.Life.Coverage.CovOption.LifeCovOptTypeCode
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveLifeCovOptTypeCode(NbaOinkRequest aNbaOinkRequest) {
		int sizeCov = 0;
		Coverage coverage = null;
		CovOption covOption = null;
		Life life = getLife();
		if (life != null) {
			sizeCov = aNbaOinkRequest.getCount();
			//start loop for the no of occurrences
			for (int i = 0; i < sizeCov; i++) {
				int covIndx = getCoverageForParty(aNbaOinkRequest, i);
				if (covIndx == -1) {
					break;
				} else {
					coverage = life.getCoverageAt(covIndx); // SPR3290
					covOption = getCovOption(coverage, i);
					if (covOption != null && (covOption.hasLifeCovOptTypeCode())) {
						aNbaOinkRequest.addValue(covOption.getLifeCovOptTypeCode());
					} else {
						aNbaOinkRequest.addUnknownValue("");
					} //end if
				}
			} //end for
		} //end if
	} //end method

	/**
	 * Set the value for IndicatorCode. OLifE.Holding.Policy.Life.Coverage.IndicatorCode
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP019 New Method
	public void retrieveIndicatorCode(NbaOinkRequest aNbaOinkRequest) {
		int sizeCov = 0;
		Coverage coverage = null;
		Life life = getLife();
		if (life != null) {
			sizeCov = aNbaOinkRequest.getCount();
			//start loop for the no of occurrences
			for (int i = 0; i < sizeCov; i++) {
				int covIndx = getCoverageForParty(aNbaOinkRequest, i);
				if (covIndx == -1) {
					break;
				} else {
					coverage = getCoverage(life, covIndx);
					if (coverage != null && (coverage.hasIndicatorCode())) {
						aNbaOinkRequest.addValue(coverage.getIndicatorCode());
					} else {
						aNbaOinkRequest.addUnknownValue("");
					} //end if
				}
			} //end for
		} //end if
	} //end method

	/**
	 * Set the value for AnnualPremAmt. OLifE.Holding.Policy.Life.Coverage.AnnualPremAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveAnnualPremAmt(NbaOinkRequest aNbaOinkRequest) {
		retrieveCoverageAnnualPremAmt(aNbaOinkRequest);
	} //end method

	/**
	 * Set the value for PermFlatExtraAmt. OLifE.Holding.Policy.Life.Coverage.LifeParticipant.PermFlatExtraAmt
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 ACP002 New Method, CR1343973 method refactored 
	public void retrievePermFlatExtraAmt(NbaOinkRequest aNbaOinkRequest) {		
		SubstandardRatingExtension substandardRatingExt;
		List ratings = getPermFlatSubstandardRatingsForParty(aNbaOinkRequest);					
		for (int i = 0; i < ratings.size(); i++) {			
			substandardRatingExt = NbaUtils.getFirstSubstandardExtension((SubstandardRating) ratings.get(i));
			aNbaOinkRequest.addValue(substandardRatingExt.getPermFlatExtraAmt());				
		}		
	}	
	
	/**
	 * Set the value for PermTableRating. OLifE.Holding.Policy.Life.Coverage.LifeParticipant.PermTableRating
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrievePermLifeTableRating(NbaOinkRequest aNbaOinkRequest) {
		int sizeCov = 0; // SPR3290
		Coverage coverage = null;
		LifeParticipant lifeParticipant = null;
		Life life = getLife();
		if (life != null) {
			sizeCov = aNbaOinkRequest.getCount();
			//start loop for the no of occurrences
			for (int i = 0; i < sizeCov; i++) {
				int covIndx = getCoverageForParty(aNbaOinkRequest, i);
				if (covIndx == -1) {
					break;
				} else {
					coverage = life.getCoverageAt(covIndx); // SPR3290
					lifeParticipant = getLifeParticipant(coverage, i);
					if (lifeParticipant != null && (lifeParticipant.hasPermTableRating())) {
						aNbaOinkRequest.addValue(lifeParticipant.getPermTableRating());
					} else {
						aNbaOinkRequest.addUnknownValue("");
					} //end if
				}
			} //end for
		} //end if
	} //end method

	/**
	 * Set the value for RatingOverriddenInd. OLifE.Holding.Policy.Life.Coverage.LifeParticipant.RatingOverriddenInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveRatingOverriddenInd(NbaOinkRequest aNbaOinkRequest) {
		int sizeCov = 0; // SPR3290
		Coverage coverage = null;
		LifeParticipant lifeParticipant = null;
		Life life = getLife();
		if (life != null) {
			sizeCov = aNbaOinkRequest.getCount();
			//start loop for the no of occurrences
			for (int i = 0; i < sizeCov; i++) {
				int covIndx = getCoverageForParty(aNbaOinkRequest, i);
				if (covIndx == -1) {
					break;
				} else {
					coverage = life.getCoverageAt(covIndx); // SPR3290
					lifeParticipant = getLifeParticipant(coverage, i);
					if (lifeParticipant != null && (lifeParticipant.hasRatingOverriddenInd())) {
						aNbaOinkRequest.addValue(lifeParticipant.getRatingOverriddenInd());
					} else {
						aNbaOinkRequest.addUnknownValue("");
					} //end if
				}
			} //end for
		} //end if
	} //end method

	/**
	 * Set the value for RatingReason. OLifE.Holding.Policy.Life.Coverage.LifeParticipant.RatingReason
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveRatingReason(NbaOinkRequest aNbaOinkRequest) {
		int sizeCov = 0; // SPR3290
		Coverage coverage = null;
		LifeParticipant lifeParticipant = null;
		Life life = getLife();
		if (life != null) {
			sizeCov = aNbaOinkRequest.getCount();
			//start loop for the no of occurrences
			for (int i = 0; i < sizeCov; i++) {
				int covIndx = getCoverageForParty(aNbaOinkRequest, i);
				if (covIndx == -1) {
					break;
				} else {
					coverage = life.getCoverageAt(covIndx); // SPR3290
					lifeParticipant = getLifeParticipant(coverage, i);
					if (lifeParticipant != null && (lifeParticipant.hasRatingReason())) {
						aNbaOinkRequest.addValue(lifeParticipant.getRatingReason());
					} else {
						aNbaOinkRequest.addUnknownValue("");
					} //end if
				}
			} //end for
		} //end if
	} //end method

	/**
	 * Set the value for TempFlatEndDate. OLifE.Holding.Policy.Life.Coverage.LifeParticipant.TempFlatEndDate
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 ACP002 New Method
	public void retrieveTempFlatEndDate(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = null;
		LifeParticipant lifeParticipant;
		SubstandardRating substandardRating;
		// SPR3290 code deleted
		int count = aNbaOinkRequest.getCount();
		if ((aNbaOinkRequest.getQualifier().length() > 0 && !isCOVOrRDRQualifier(aNbaOinkRequest))
				|| aNbaOinkRequest.getRelationRoleCode() != NbaOinkRequest.noFilterLong
				|| aNbaOinkRequest.getPartyFilter() != NbaOinkRequest.noFilterInt
				|| aNbaOinkRequest.getCoverageFilter() != NbaOinkRequest.noFilterLong) {
			Life life = getLife();
			if (life != null) {
				for (int i = 0; i < count; i++) {
					int covIndx = getCoverageForParty(aNbaOinkRequest, i);
					if (covIndx == -1) {
						break;
					} else {
						coverage = life.getCoverageAt(covIndx);
						Date endDate = null;
						lifeParticipant = getLifeParticipant(coverage, i);
						if (lifeParticipant != null) {
							int countSR = lifeParticipant.getSubstandardRatingCount();
							for (int j = 0; j < countSR; j++) {
								substandardRating = lifeParticipant.getSubstandardRatingAt(j);
								if (NbaUtils.isValidRating(substandardRating) && substandardRating.hasTempFlatExtraAmt()) { //SPR2590
									endDate = substandardRating.getTempFlatEndDate();
									break;
								}
							}
						}
						if (endDate != null) {
							aNbaOinkRequest.addValue(endDate);
						} else {
							aNbaOinkRequest.addUnknownValue(new Date());
						}
					}
				}
			}
		} else {
			List ratings = getTempFlatSubstandardRatings(aNbaOinkRequest);
			if (ratings.size() < count) {
				count = ratings.size();
			}
			for (int i = 0; i < count; i++) {
				if (ratings.get(i) == null) {
					aNbaOinkRequest.addUnknownValue(new Date());
				} else {
					aNbaOinkRequest.addValue(((SubstandardRating) ratings.get(i)).getTempFlatEndDate());
				}
			}
		}
	}

	/**
	 * Set the value for TempFlatExtraAmt. OLifE.Holding.Policy.Life.Coverage.LifeParticipant.TempFlatExtraAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveTempLifeFlatExtraAmt(NbaOinkRequest aNbaOinkRequest) {
		int sizeCov = 0; // SPR3290
		Coverage coverage = null;
		LifeParticipant lifeParticipant = null;
		Life life = getLife();
		if (life != null) {
			sizeCov = aNbaOinkRequest.getCount();
			//start loop for the no of occurrences
			for (int i = 0; i < sizeCov; i++) {
				int covIndx = getCoverageForParty(aNbaOinkRequest, i);
				if (covIndx == -1) {
					break;
				} else {
					coverage = life.getCoverageAt(covIndx); // SPR3290
					lifeParticipant = getLifeParticipant(coverage, i);
					if (lifeParticipant != null && (lifeParticipant.hasTempFlatExtraAmt())) {
						aNbaOinkRequest.addValue(lifeParticipant.getTempFlatExtraAmt());
					} else {
						aNbaOinkRequest.addUnknownValue("");
					} //end if
				}
			} //end for
		} //end if
	} //end method

	/**
	 * Obtain the value for CompletionDate. OlifE.FormInstance.CompletionDate
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveCompletionDate(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			FormInstance formInstance = getFormInstance(aNbaOinkRequest, i);
			if (formInstance != null && formInstance.hasCompletionDate()) {
				aNbaOinkRequest.addValue(formInstance.getCompletionDate());
			} else {
				aNbaOinkRequest.addUnknownValue("");
			}
		}
	}

	/**
	 * Obtain the value for ChestFullExpansionMeasure. OLifE.Party.Risk.MedicalExam.ChestFullMeasure.MeasureValue
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP001 New Method
	public void retrieveChestFullExpansionMeasure(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		ChestFullMeasure chestFullMeasure = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				chestFullMeasure = aMedicalExam.getChestFullMeasure();
				aNbaOinkRequest.addValue(chestFullMeasure.getMeasureValue());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for ChestForceExpirationMeasure. OLifE.Party.Risk.MedicalExam.ChestForceMeasure.MeasureValue
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP001 New Method
	public void retrieveChestForceExpirationMeasure(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		ChestForcedMeasure chestForcedMeasure = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				chestForcedMeasure = aMedicalExam.getChestForcedMeasure();
				aNbaOinkRequest.addValue(chestForcedMeasure.getMeasureValue());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for AbdominalMeasure. OLifE.Party.Risk.MedicalExam.AbdominalMeasureValue.MeasureValue
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP001 New Method
	public void retrieveAbdominalMeasure(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		AbdomenMeasure abdomenMeasure = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				abdomenMeasure = aMedicalExam.getAbdomenMeasure();
				aNbaOinkRequest.addValue(abdomenMeasure.getMeasureValue());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for WeightChgAmt. Olife.Party.Risk.WeightChgAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveWeightChgAmt(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk aRisk = getRisk(party);
			if (aRisk != null) {
				int index_extension = getExtensionIndex(aRisk.getOLifEExtension(), RISK_EXTN);
				if (index_extension != -1) {
					RiskExtension extension = aRisk.getOLifEExtensionAt(index_extension).getRiskExtension();
					if (extension != null) {
						aNbaOinkRequest.addValue(extension.getWeightChgAmt());
					}
				}
			}
		}
	}

	/**
	 * Obtain the value for WeightChgInd. Olife.Party.Risk.WeightChgInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveWeightChgInd(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk aRisk = getRisk(party);
			if (aRisk != null) {
				int index_extension = getExtensionIndex(aRisk.getOLifEExtension(), RISK_EXTN);
				if (index_extension != -1) {
					RiskExtension extension = aRisk.getOLifEExtensionAt(index_extension).getRiskExtension();
					if (extension != null) {
						boolean wtChgInd = extension.getWeightChgInd();
						aNbaOinkRequest.addValue(wtChgInd);
					}
				}
			}
		}
	}

	/**
	 * Obtain the value for HabitUsage. Olife.Party.Risk.SubstanceUsage.HabitUsage.MeasureValue
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveHabitUsage(NbaOinkRequest aNbaOinkRequest) {
		SubstanceUsage aSubstanceUsage = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aSubstanceUsage = getSubstanceUsage(aNbaOinkRequest, i);
			if (aSubstanceUsage != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aSubstanceUsage.getOLifEExtension(), SUBSTANCEUSAGE_EXTN);
				if (index_extension != -1) {
					oli = aSubstanceUsage.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					aNbaOinkRequest.addValue(oli.getSubstanceUsageExtension().getHabitUsage());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for UrineTempValue. Olife.Party.Risk.MedicalExam.UrineTemperature.MeasureValue
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP001 New Method
	public void retrieveUrineTempValue(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		UrineTemperature urineTemperature = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					urineTemperature = oli.getMedicalExamExtension().getUrineTemperature();
					if (urineTemperature != null) { //AXAL3.7.07
						aNbaOinkRequest.addValue(urineTemperature.getMeasureValue());
					} //AXAL3.7.07
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for WeightChgAmt. Olife.Party.Risk.MedicalExam.WeightChgAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveWeightChgAmtOther(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					MedicalExamExtension mee = oli.getMedicalExamExtension();
					aNbaOinkRequest.addValue(mee.getWeightChgAmt());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for WeightChgInd. Olife.Party.Risk.MedicalExam.WeightChgInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveWeightChgIndOther(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					MedicalExamExtension mee = oli.getMedicalExamExtension();
					boolean wtChgInd = mee.getWeightChgInd();
					aNbaOinkRequest.addValue(wtChgInd);
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for Height2. Olife.Party.Risk.MedicalExam.Height2
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveHeight2Other(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					MedicalExamExtension mee = oli.getMedicalExamExtension();
					Height2 ht = mee.getHeight2();
					aNbaOinkRequest.addValue(ht.getMeasureValue());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for Weight2. Olife.Party.Risk.MedicalExam.Weight2
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveWeight2Other(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					MedicalExamExtension mee = oli.getMedicalExamExtension();
					Weight2 ht = mee.getWeight2();
					aNbaOinkRequest.addValue(ht.getMeasureValue());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for AppearanceInd. Olife.Party.Risk.MedicalExam.AppearanceInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP001 New Method
	public void retrieveAppearanceInd(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					MedicalExamExtension mee = oli.getMedicalExamExtension();
					aNbaOinkRequest.addValue(mee.getAppearanceInd());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for ExerciseIrregularInd. Olife.Party.Risk.MedicalExam.ExerciseIrregularInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveExerciseIrregularInd(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					MedicalExamExtension mee = oli.getMedicalExamExtension();
					boolean ind = mee.getExerciseIrregularInd();
					aNbaOinkRequest.addValue(ind);
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for ExercisePulseRate. Olife.Party.Risk.MedicalExam.ExercisePulseRate
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveExercisePulseRate(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					MedicalExamExtension mee = oli.getMedicalExamExtension();
					aNbaOinkRequest.addValue(mee.getExercisePulseRate());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for PostExerciseIrregularInd. Olife.Party.Risk.MedicalExam.PostExerciseIrregularInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrievePostExerciseIrregularInd(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					MedicalExamExtension mee = oli.getMedicalExamExtension();
					boolean ind = mee.getPostExerciseIrregularInd();
					aNbaOinkRequest.addValue(ind);
				}
			} else {
				break;
			}
		}
	}

	/**
	 * 
	 * Obtain the value for PostExercisePulseRate. Olife.Party.Risk.MedicalExam.PostExercisePulseRate
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrievePostExercisePulseRate(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					MedicalExamExtension mee = oli.getMedicalExamExtension();
					aNbaOinkRequest.addValue(mee.getPostExercisePulseRate());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * 
	 * Obtain the value for FVCActual. Olife.Party.Risk.MedicalExam.FVCActual
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveFVCActual(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					MedicalExamExtension mee = oli.getMedicalExamExtension();
					TimedVitals tvc = mee.getTimedVitalsAt(0);
					Previous prev = tvc.getPrevious();
					aNbaOinkRequest.addValue(prev.getFVCActual());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * 
	 * Obtain the value for FEVActualOneSecond. Olife.Party.Risk.MedicalExam.FEVActualOneSecond
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveFEVActualOneSecond(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					MedicalExamExtension mee = oli.getMedicalExamExtension();
					TimedVitals tvc = mee.getTimedVitalsAt(0);
					Previous prev = tvc.getPrevious();
					aNbaOinkRequest.addValue(prev.getFEVActualOneSecond());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * 
	 * Obtain the value for FEVPercentage. Olife.Party.Risk.MedicalExam.FEVPercentage
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveFEVPercentage(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					MedicalExamExtension mee = oli.getMedicalExamExtension();
					TimedVitals tvc = mee.getTimedVitalsAt(0);
					Previous prev = tvc.getPrevious();
					aNbaOinkRequest.addValue(prev.getFEVPercentage());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * 
	 * Obtain the value for FVCAndFEVAssessment. Olife.Party.Risk.MedicalExam.FVCAndFEVAssessment
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveFVCAndFEVAssessment(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					MedicalExamExtension mee = oli.getMedicalExamExtension();
					TimedVitals tvc = mee.getTimedVitalsAt(0);
					Previous prev = tvc.getPrevious();
					aNbaOinkRequest.addValue(prev.getFVCAndFEVAssessment());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * 
	 * Obtain the value for FVCPercentage. Olife.Party.Risk.MedicalExam.FVCPercentage
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveFVCPercentage(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					MedicalExamExtension mee = oli.getMedicalExamExtension();
					TimedVitals tvc = mee.getTimedVitalsAt(0);
					Previous prev = tvc.getPrevious();
					aNbaOinkRequest.addValue(prev.getFVCPercentage());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * 
	 * Obtain the value for FVCActual. Olife.Party.Risk.MedicalExam.FVCActual
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveFVCActualPost(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					MedicalExamExtension mee = oli.getMedicalExamExtension();
					TimedVitals tvc = mee.getTimedVitalsAt(0);
					Post post = tvc.getPost();
					aNbaOinkRequest.addValue(post.getFVCActual());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * 
	 * Obtain the value for FEVActualOneSecond. Olife.Party.Risk.MedicalExam.FEVActualOneSecond
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveFEVActualOneSecondPost(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					MedicalExamExtension mee = oli.getMedicalExamExtension();
					TimedVitals tvc = mee.getTimedVitalsAt(0);
					Post post = tvc.getPost();
					aNbaOinkRequest.addValue(post.getFEVActualOneSecond()); //TODO - change FVC to FEV
				}
			} else {
				break;
			}
		}
	}

	/**
	 * 
	 * Obtain the value for FEVPercentage. Olife.Party.Risk.MedicalExam.FEVPercentage
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveFEVPercentagePost(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					MedicalExamExtension mee = oli.getMedicalExamExtension();
					TimedVitals tvc = mee.getTimedVitalsAt(0);
					Post post = tvc.getPost();
					aNbaOinkRequest.addValue(post.getFEVPercentage());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * 
	 * Obtain the value for FVCAndFEVAssessment. Olife.Party.Risk.MedicalExam.FVCAndFEVAssessment
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveFVCAndFEVAssessmentPost(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					MedicalExamExtension mee = oli.getMedicalExamExtension();
					TimedVitals tvc = mee.getTimedVitalsAt(0);
					Post post = tvc.getPost();
					aNbaOinkRequest.addValue(post.getFVCAndFEVAssessment());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * 
	 * Obtain the value for FVCPercentage. Olife.Party.Risk.MedicalExam.FVCPercentage
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP002 New Method
	public void retrieveFVCPercentagePost(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					MedicalExamExtension mee = oli.getMedicalExamExtension();
					TimedVitals tvc = mee.getTimedVitalsAt(0);
					Post post = tvc.getPost();
					aNbaOinkRequest.addValue(post.getFVCPercentage());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for UrineTempUnits. Olife.Party.Risk.MedicalExam.UrineTemperature.UnitsOfMeasure
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP001 New Method
	public void retrieveUrineTempUnits(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		UrineTemperature urineTemperature = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					urineTemperature = oli.getMedicalExamExtension().getUrineTemperature();
					if (urineTemperature != null) { //AXAL3.7.07
						aNbaOinkRequest.addValue(urineTemperature.getUnitsOfMeasure());
					} //AXAL3.7.07
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for SerumAppearanceCode. Olife.Party.Risk.MedicalExam.SerumAppearanceCode
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP001 New Method
	public void retrieveLabSerumAppearanceCode(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					aNbaOinkRequest.addValue(oli.getMedicalExamExtension().getSerumAppearanceCode());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for AppearanceInd. Olife.Party.Risk.MedicalExam.AppearanceInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP001 New Method
	public void retrieveLabAppearanceInd(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICALEXAM_EXTN);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					aNbaOinkRequest.addValue(oli.getMedicalExamExtension().getAppearanceInd());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for ReportCompleteInd Olife.Party.Risk.LabTesting.ReportCompleteInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP001 New Method
	public void retrieveReportCompleteInd(NbaOinkRequest aNbaOinkRequest) {
		LabTesting labTesting = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			labTesting = getlabTesting(aNbaOinkRequest, i);
			if (labTesting != null) {
				aNbaOinkRequest.addValue(labTesting.getReportCompleteInd());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for SensitiveResultsInd Olife.Party.Risk.LabTesting.SensitiveResultsInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP001 New Method
	public void retrieveSensitiveResultsInd(NbaOinkRequest aNbaOinkRequest) {
		LabTesting labTesting = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			labTesting = getlabTesting(aNbaOinkRequest, i);
			if (labTesting != null) {
				aNbaOinkRequest.addValue(labTesting.getSensitiveResultsInd());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for LabProcessDate Olife.Party.Risk.LabTesting.LabTestResult.ProcessDate
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP001 New Method
	public void retrieveLabProcessDate(NbaOinkRequest aNbaOinkRequest) {
		LabTesting labTesting = null;
		ArrayList labResultList = null;
		LabTestResult labTestResult = null;
		int labResultListLength = 0;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			labTesting = getlabTesting(aNbaOinkRequest, i);
			if (labTesting != null) {
				labResultList = labTesting.getLabTestResult();
				labResultListLength = labResultList.size();
				for (int j = 0; j < labResultListLength; j++) {
					labTestResult = (LabTestResult) labResultList.get(j);
					aNbaOinkRequest.addValue(labTestResult.getProcessDate());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for LabSpecimanType Olife.Party.Risk.LabTesting.LabTestResult.ProcessDate
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP001 New Method
	public void retrieveLabSpecimanType(NbaOinkRequest aNbaOinkRequest) {
		LabTesting labTesting = null;
		ArrayList labResultList = null;
		LabTestResult labTestResult = null;
		int labResultListLength = 0;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			labTesting = getlabTesting(aNbaOinkRequest, i);
			if (labTesting != null) {
				labResultList = labTesting.getLabTestResult();
				labResultListLength = labResultList.size();
				for (int j = 0; j < labResultListLength; j++) {
					labTestResult = (LabTestResult) labResultList.get(j);
					aNbaOinkRequest.addValue(labTestResult.getSpecimanType());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for ReceivedDate. OLifE.Holding.Policy.RequirementInfo.ReceivedDate is the code specifying the underwriting requirement..
	 * @param aNbaOinkRequest - data request container
	 */
	//	ACP002 New Method
	public void retrieveReceivedDate(NbaOinkRequest aNbaOinkRequest) {
		List reqList = getRequirementInfos(aNbaOinkRequest); //SPR3353
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			RequirementInfo aRequirementInfo = getRequirementInfo(reqList, i); //SPR3353
			if (aRequirementInfo != null) {
				aNbaOinkRequest.addValue(aRequirementInfo.getReceivedDate()); // Special Handling for Summary VPMS models
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for ReqStatus. OLifE.Holding.Policy.RequirementInfo.ReqStatus is the code specifying the underwriting requirement..
	 * @param aNbaOinkRequest - data request container
	 */
	//	ACP001 New Method
	public void retrieveReqStatus(NbaOinkRequest aNbaOinkRequest) {
		List reqList = getRequirementInfos(aNbaOinkRequest); //SPR3353
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			RequirementInfo aRequirementInfo = getRequirementInfo(reqList, i); //SPR3353
			if (aRequirementInfo != null) {
				aNbaOinkRequest.addValue(aRequirementInfo.getReqStatus());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain all the values for ReqCode. OLifE.Holding.Policy.RequirementInfo.ReqCode is the code specifying the underwriting requirement..
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP001 New Method
	public void retrieveReqCodeList(NbaOinkRequest aNbaOinkRequest) {
		RequirementInfo reqInfo = null;
		Policy policy = getPolicy();
		int count = policy.getRequirementInfoCount();
		for (int i = 0; i < count; i++) {
			reqInfo = policy.getRequirementInfoAt(i);
			if (reqInfo != null) {
				aNbaOinkRequest.addValue(reqInfo.getReqCode());
			}
		}
	}

	/**
	 * Obtain all the values for ReqStatus. OLifE.Holding.Policy.RequirementInfo.ReqStatus is the code specifying the underwriting requirement..
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP001 New Method
	public void retrieveReqStatusList(NbaOinkRequest aNbaOinkRequest) {
		RequirementInfo reqInfo = null;
		Policy policy = getPolicy();
		int count = policy.getRequirementInfoCount();
		for (int i = 0; i < count; i++) {
			reqInfo = policy.getRequirementInfoAt(i);
			if (reqInfo != null) {
				aNbaOinkRequest.addValue(reqInfo.getReqStatus());
			}
		}
	}

	/**
	 * Obtain all the unique values of LabTestCode for a particular requirement id. Olife.Party.Risk.LabTesting.LabTestResult.TestCode is the code
	 * specifying the underwriting requirement..
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP001 New Method
	public void retrieveLabTestCode(NbaOinkRequest aNbaOinkRequest) {
		String reqIdFilter = aNbaOinkRequest.getRequirementIdFilter();
		LabTesting labTesting = getlabTesting(aNbaOinkRequest, 0);
		if (labTesting != null) {
			ArrayList labTestResultList = labTesting.getLabTestResult();
			LabTestResult labTestResult = null;
			int count = labTestResultList.size();
			String reqId;
			HashMap testCodeMap = new HashMap();
			String testCode = "";
			for (int i = 0; i < count; i++) {
				reqId = "";
				labTestResult = (LabTestResult) labTestResultList.get(i);
				reqId = labTestResult.getRequirementInfoID();
				testCode = String.valueOf(labTestResult.getTestCode());
				if (!reqId.equals("") && reqId.equals(reqIdFilter) && !testCodeMap.containsKey(testCode)) {
					aNbaOinkRequest.addValue(testCode);
					testCodeMap.put(testCode, "");
				}
			}
		}
	}

	/**
	 * Obtain all the values of LabValue for a particular LabTestCode. Olife.Party.Risk.LabTesting.LabTestResult.TestCode is the code specifying the
	 * underwriting requirement..
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP001 New Method
	public void retrieveLabValue(NbaOinkRequest aNbaOinkRequest) {
		String labTestCodeFilter = aNbaOinkRequest.getLabTestCodeFilter();
		LabTesting labTesting = getlabTesting(aNbaOinkRequest, 0);
		List labTestResultList = labTesting.getLabTestResult(); // SPR3290
		LabTestResult labTestResult = null;
		int count = labTestResultList.size();
		String testCode;
		String labValue;
		long valueCode = 0;		
		boolean isConvertToString = false;
		QualitativeResult qual = null;		
		boolean duplicateTestCodeFlag = determineMultipleMatchingTestCode(aNbaOinkRequest);//APSL2819
		// SPR3290 code deleted
		List qualList = null; // SPR3290
		List quantList = null; // SPR3290
		for (int i = 0; i < count; i++) {
			testCode = "";
			labValue = "";
			valueCode = 0;
			labTestResult = (LabTestResult) labTestResultList.get(i);
			testCode = String.valueOf(labTestResult.getTestCode());
			if (testCode != "" && testCode.equals(labTestCodeFilter)) {				
				qualList = labTestResult.getQualitativeResult();
				quantList = labTestResult.getQuantitativeResult();
				
				if ((qualList.size() >= 1 && quantList.size() >= 1) || duplicateTestCodeFlag) {//APSL2819
					isConvertToString = true;//APSL2819
				} 			
				if (quantList != null) {
					for (int j = 0; j < quantList.size(); j++) {
						//Begin APSL2819
						if(isConvertToString) {
							aNbaOinkRequest.addValue(Double.toString(((QuantitativeResult) quantList.get(j)).getMeasureValue()));
							isConvertToString = false;
							//End APSL2819
						} else {
							aNbaOinkRequest.addValue(((QuantitativeResult) quantList.get(j)).getMeasureValue());
						}
						
					}
				}
				if (qualList != null) {
					for (int j = 0; j < qualList.size(); j++) {
						qual = (QualitativeResult) qualList.get(j);
						valueCode = qual.getValueCode();
						if (valueCode != -1) {
							labValue = NbaTransOliCode.lookupText(NbaOliConstants.OLI_LU_QUALVALUE, valueCode);
							if (labValue != "") {
								aNbaOinkRequest.addValue(labValue);
							}
						} else {
							aNbaOinkRequest.addValue(qual.getValueString());
						}
					}
				}
			}
		}
	}
	
	
	
	/**
	 * This method will check if labTestCodeFilter is present in more then one labTestResult object
	 * @param aNbaOinkRequest - data request container
	 * @return True/False
	 */
	 //APSL2819 New Method
	public boolean determineMultipleMatchingTestCode(NbaOinkRequest aNbaOinkRequest) {
		String labTestCodeFilter = aNbaOinkRequest.getLabTestCodeFilter();
		LabTesting labTesting = getlabTesting(aNbaOinkRequest, 0);
		List labTestResultList = labTesting.getLabTestResult();
		int count = labTestResultList.size();
		String testCode;
		int multipleTestCodeFlag = 0;
		LabTestResult labTestResult = null;
		for (int i = 0; i < count; i++) {
			testCode = "";
			labTestResult = (LabTestResult) labTestResultList.get(i);
			testCode = String.valueOf(labTestResult.getTestCode());
			if (testCode != "" && testCode.equals(labTestCodeFilter)) {
				multipleTestCodeFlag++;
			}
		}

		if (multipleTestCodeFlag > 1) {
			return true;
		}

		return false;
	}

	/**
	 * Obtain all the values of FormQuestionNumber for a particular requirement. OlifE.FormInstance.FormResponse.QuestionNumber is the code specifying
	 * the underwriting requirement..
	 * @param aNbaOinkRequest - data request container
	 */
	//	ACP001 New Method
	public void retrieveFormQuestionNumber(NbaOinkRequest aNbaOinkRequest) {
		FormInstance tempFormInstance = null;
		FormInstance formInstance = null;
		//get all the form instances
		ArrayList formInstanceList = getOLifE().getFormInstance();
		int listSize = formInstanceList.size();
		//get the medical exam object for the requirement
		MedicalExam medicalExam = getMedicalExam(aNbaOinkRequest, 0);
		//find the forminstance object for the requiement
		//through the medical exam object
		for (int i = 0; i < listSize; i++) {
			tempFormInstance = (FormInstance) formInstanceList.get(i);
			if (tempFormInstance.getRelatedObjectID() != null) {
				if (tempFormInstance.getRelatedObjectID().equals(medicalExam.getId())) {
					formInstance = tempFormInstance;
					break;
				}
			}
		}
		if (formInstance != null) {
			ArrayList formResponseList = formInstance.getFormResponse();
			for (int i = 0; i < formResponseList.size(); i++) {
				aNbaOinkRequest.addValue(((FormResponse) formResponseList.get(i)).getQuestionNumber());
			}
		}
	}

	/**
	 * Obtain all the values of FormQuestionText for a particular requirement. OlifE.FormInstance.FormResponse.QuestionText is the code specifying the
	 * underwriting requirement..
	 * @param aNbaOinkRequest - data request container
	 */
	//	ACP001 New Method
	public void retrieveFormQuestionText(NbaOinkRequest aNbaOinkRequest) {
		FormInstance tempFormInstance = null;
		FormInstance formInstance = null;
		//get all the form instances
		ArrayList formInstanceList = getOLifE().getFormInstance();
		int listSize = formInstanceList.size();
		//get the medical exam object for the requirement
		MedicalExam medicalExam = getMedicalExam(aNbaOinkRequest, 0);
		//find the forminstance object for the requiement
		//through the medical exam object
		for (int i = 0; i < listSize; i++) {
			tempFormInstance = (FormInstance) formInstanceList.get(i);
			if (tempFormInstance.getRelatedObjectID() != null) {
				if (tempFormInstance.getRelatedObjectID().equals(medicalExam.getId())) {
					formInstance = tempFormInstance;
					break;
				}
			}
		}
		if (formInstance != null) {
			ArrayList formResponseList = formInstance.getFormResponse();
			for (int i = 0; i < formResponseList.size(); i++) {
				aNbaOinkRequest.addValue(((FormResponse) formResponseList.get(i)).getQuestionText());
			}
		}
	}

	/**
	 * Obtain all the values of FormResponseCode for a particular requirement. OlifE.FormInstance.FormResponse.ResponseCode is the code specifying the
	 * underwriting requirement..
	 * @param aNbaOinkRequest - data request container
	 */
	//	ACP001 New Method
	public void retrieveFormResponseCode(NbaOinkRequest aNbaOinkRequest) {
		FormInstance tempFormInstance = null;
		FormInstance formInstance = null;
		//get all the form instances
		ArrayList formInstanceList = getOLifE().getFormInstance();
		int listSize = formInstanceList.size();
		//ACP005 Begin
		String relatedObjectType = aNbaOinkRequest.getRelatedObjectTypeFilter();
		if (relatedObjectType != null && !relatedObjectType.equals("")) {
			formInstance = getFormInstanceByRelatedObjectType(aNbaOinkRequest);
			if (formInstance != null) {
				ArrayList formResponseList = formInstance.getFormResponse();
				for (int i = 0; i < formResponseList.size(); i++) {
					aNbaOinkRequest.addValue(((FormResponse) formResponseList.get(i)).getResponseCode());
				}
			}
			return;
		}
		//ACP005 End
		//get the medical exam object for the requirement
		MedicalExam medicalExam = getMedicalExam(aNbaOinkRequest, 0);
		//find the forminstance object for the requiement
		//through the medical exam object
		if (medicalExam != null) { //ACP005: Added this condition
			for (int i = 0; i < listSize; i++) {
				tempFormInstance = (FormInstance) formInstanceList.get(i);
				if (tempFormInstance.getRelatedObjectID() != null) {
					if (tempFormInstance.getRelatedObjectID().equals(medicalExam.getId())) {
						formInstance = tempFormInstance;
						break;
					}
				}
			}
			if (formInstance != null) {
				ArrayList formResponseList = formInstance.getFormResponse();
				for (int i = 0; i < formResponseList.size(); i++) {
					aNbaOinkRequest.addValue(((FormResponse) formResponseList.get(i)).getResponseCode());
				}
			}
		}
	}

	/**
	 * Obtain the value for DBA. Olife.Party.Organization.DBA is the Age of the person
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP019 New Method
	public void retrieveDBA(NbaOinkRequest aNbaOinkRequest) {
		int count = aNbaOinkRequest.getCount();
		for (int i = 0; i < count; i++) {
			Organization organization = getOrganization(aNbaOinkRequest, i);
			if (organization != null) {
				aNbaOinkRequest.addValue(organization.getDBA());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for a Impairment Debit. OLifE.Party.Person.ImpairmentInfo.Debit is the debit of the impairment for the person.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP007 New Method
	public void retrieveDebit(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			ImpairmentInfo impInfo = getImpairmentInfo(aNbaOinkRequest, i);
			if (impInfo != null) {
				aNbaOinkRequest.addValue(impInfo.getDebit());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for a Impairment Date. OLifE.Party.Person.ImpairmentInfo.ImpairmentDate is the date of the impairment for the person.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACN006 New Method
	public void retrieveImpairmentDate(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			ImpairmentInfo impInfo = getImpairmentInfo(aNbaOinkRequest, i);
			if (impInfo != null) {
				aNbaOinkRequest.addValue(impInfo.getImpairmentDate());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for a Impairment Description. OLifE.Party.Person.ImpairmentInfo.Description is the description of the impairment for the
	 * person.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACN006 New Method
	public void retrieveDescription(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			ImpairmentInfo impInfo = getImpairmentInfo(aNbaOinkRequest, i);
			if (impInfo != null) {
				aNbaOinkRequest.addValue(impInfo.getDescription());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for a Impairment Status. OLifE.Party.Person.ImpairmentInfo.ImpairmentStatus is the status of the impairment for the person.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACN006 New Method
	public void retrieveImpairmentStatus(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			ImpairmentInfo impInfo = getImpairmentInfo(aNbaOinkRequest, i);
			if (impInfo != null) {
				aNbaOinkRequest.addValue(impInfo.getImpairmentStatus());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for Impairment Class. OLifE.Party.Person.ImpairmentInfo.ImpairmentClass is the class of the Impairment
	 * @param aNbaOinkRequest - data request container
	 */
	//ACN006 New Method
	public void retrieveImpairmentClass(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			ImpairmentInfo impInfo = getImpairmentInfo(aNbaOinkRequest, i);
			if (impInfo != null) {
				aNbaOinkRequest.addValue(impInfo.getImpairmentClass(), NbaTableConstants.OLIEXT_LU_IMPAIRMENTCLASS);
			} else {
				break;
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLIEXT_LU_IMPAIRMENTCLASS);
		}
	}

	/**
	 * Obtain the value for a Impairment Type. OLifE.Party.Person.ImpairmentInfo.ImpairmentType is the type of the impairment for the person.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACN006 New Method
	public void retrieveImpairmentType(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			ImpairmentInfo impInfo = getImpairmentInfo(aNbaOinkRequest, i);
			if (impInfo != null) {
				aNbaOinkRequest.addValue(impInfo.getImpairmentType());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for Impairment Source. OLifE.Party.Person.ImpairmentInfo.ImpairmentSource is the Source from where the impairment is added.
	 * @param aNbaOinkRequest - data request container
	 */
	//	ACN006 New Method
	public void retrieveImpairmentSource(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			ImpairmentInfo impInfo = getImpairmentInfo(aNbaOinkRequest, i);
			if (impInfo != null) {
				aNbaOinkRequest.addValue(impInfo.getImpairmentSource(), NbaTableConstants.OLIEXT_LU_IMPAIRMENTSOURCE);
			} else {
				break;
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLIEXT_LU_IMPAIRMENTSOURCE);
		}
	}

	/**
	 * Obtain the value for a Impairment Workup Indicator. OLifE.Party.Person.ImpairmentInfo.ImpWorkupInd is the Workup Indicator of the impairment
	 * for the person.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP019 New Method
	public void retrieveImpWorkupInd(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			ImpairmentInfo impInfo = getImpairmentInfo(aNbaOinkRequest, i);
			if (impInfo != null) {
				aNbaOinkRequest.addValue(impInfo.getImpWorkupInd());
			} else {
				break;
			}
		}
	}

	/**
	 * get the value for TrackingServiceProvider. OLifE.Holding.Policy.RequirementInfo.TrackingInfo.TrackingServiceProvider is the Vendor name of the
	 * requirement
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP019 New Method
	public void retrieveTrackingServiceProvider(NbaOinkRequest aNbaOinkRequest) {
		List reqList = getRequirementInfos(aNbaOinkRequest); //SPR3353
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			TrackingInfo trackInfo = getTrackingInfo(reqList, i); //SPR3353
			if (trackInfo != null)
				aNbaOinkRequest.addValue(trackInfo.getTrackingServiceProvider());
			else
				break;
		}
	}

	/**
	 * Obtain the value for TermDate. Holding.Policy.Life.Coverage.CovOption.TermDate
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA104 New Method
	public void retrieveTermDate(NbaOinkRequest aNbaOinkRequest) {
		CovOption covOption = null;
		String qualifier = aNbaOinkRequest.getQualifier();

		if (BENEFIT.equals(qualifier)) {
			List covoptions = getCovOptions();
			if (covoptions != null) {
				int count = covoptions.size();
				for (int i = 0; i < count; i++) {
					covOption = (CovOption) covoptions.get(i);
					if (covOption != null && covOption.hasTermDate()) {
						aNbaOinkRequest.addValue(covOption.getTermDate());
					} else {
						aNbaOinkRequest.addUnknownValue((Date) null);
					}
				}
			}
			//begin NBA100
		} else {
			List coverages = getCoveragesOrRiders(aNbaOinkRequest);
			int next = 0;
			Coverage coverage;
			while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
				if (coverage.hasTermDate()) {
					aNbaOinkRequest.addValue(coverage.getTermDate());
				} else {
					aNbaOinkRequest.addUnknownValue((Date) null);
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addUnknownValue((Date) null);
		}
		//end NBA100
	}

	/**
	 * Obtain the value for FinalPaymentDate. Default to the TermDate if not present. Holding.Policy.Life.Coverage.FinalPaymentDate
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	public void retrieveFinalPaymentDate(NbaOinkRequest aNbaOinkRequest) {
		// SPR3290 code deleted
		List coverages = getCoveragesOrRiders(aNbaOinkRequest);
		int next = 0;
		Coverage coverage;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			if (coverage.hasFinalPaymentDate()) {
				aNbaOinkRequest.addValue(coverage.getFinalPaymentDate());
			} else if (coverage.hasTermDate()) {
				aNbaOinkRequest.addValue(coverage.getTermDate());
			} else {
				aNbaOinkRequest.addUnknownValue(new Date());
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addUnknownValue(new Date());
		}
	}

	/**
	 * Obtain the value for MaturityAge. Holding.Policy.Life.Coverage.CovOption.TermDate
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	public void retrieveMaturityAge(NbaOinkRequest aNbaOinkRequest) {
		if (getNbaTXLife().isAnnuity()) {
			aNbaOinkRequest.addValue((calcMaturityAge(getNbaTXLife().getAnnuity())));
		} else {
			List coverages = getCoveragesOrRiders(aNbaOinkRequest);
			int next = 0;
			Coverage coverage;
			while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
				aNbaOinkRequest.addValue(calcMaturityAge(coverage));
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1);
		}
	}

	/**
	 * Calculate the Maturity Age for a Coverage
	 * @param coverage
	 * @return
	 */
	// NBA100 New Method
	protected int calcMaturityAge(Coverage coverage) {
		int age = -1;
		if (coverage.hasTermDate() && coverage.hasEffDate()) {
			int years = NbaUtils.calcYearsDiff(coverage.getTermDate(), coverage.getEffDate());
			LifeParticipant lifeParticipant = NbaUtils.getInsurableLifeParticipant(coverage);
			if (lifeParticipant != null && lifeParticipant.hasIssueAge()) {
				age = lifeParticipant.getIssueAge() + years;
			}
		}
		return age;
	}

	/**
	 * Calculate the Maturity Age for a Coverage
	 * @param coverage
	 * @return
	 */
	// NBA100 New Method
	protected int calcMaturityAge(Annuity annuity) {
		int age = -1;
		if (getPolicy().hasTermDate() && getPolicy().hasEffDate()) {
			int years = NbaUtils.calcYearsDiff(getPolicy().getTermDate(), getPolicy().getEffDate());
			Participant participant = getNbaTXLife().getPrimaryAnnuitantParticipant();
			if (participant != null && participant.hasIssueAge())
				age = participant.getIssueAge() + years;
		}
		return age;
	}

	/**
	 * Obtain the value for Table Rating. If the Temporary Table Rating is not found try to return the Permanent Table Rating.
	 * Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.TempTableRating
	 * Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.PermTableRating
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA104 NBA100 New Method
	public void retrieveTableRating(NbaOinkRequest aNbaOinkRequest) {
		List participants = getInsurableLifeParticipants(aNbaOinkRequest);
		int next = 0;
		LifeParticipant lifeParticipant;
		long tempRating = -1;
		long permRating = -1;
		int countSR;
		SubstandardRating substandardRating;
		while ((lifeParticipant = getNextLifeParticipant(aNbaOinkRequest, participants, next++)) != null) {
			countSR = lifeParticipant.getSubstandardRatingCount();
			for (int j = 0; j < countSR; j++) {
				substandardRating = lifeParticipant.getSubstandardRatingAt(j);
				if (NbaUtils.isValidRating(substandardRating)) { //SPR2590
					if (substandardRating.hasTempTableRating() && tempRating == -1) {
						tempRating = substandardRating.getTempTableRating();
					} else if (substandardRating.hasPermTableRating() && permRating == -1) {
						permRating = substandardRating.getPermTableRating();
					}
				}
			}
			if (tempRating != -1) {
				aNbaOinkRequest.addValue(tempRating);
			} else {
				aNbaOinkRequest.addValue(permRating);
			}
		}
	}

	/**
	 * Obtain the value for BandTableIdentity. Holding.Policy.Life.Coverage.BandTableIdentity
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA104 New Method
	public void retrieveBandTableIdentity(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = null;
		CoverageExtension covExt = null;
		List coverages = getCoveragesOrRiders(aNbaOinkRequest);
		if (coverages == null) {
			return;
		}
		int count = coverages.size();
		for (int i = 0; i < count; i++) {
			coverage = (Coverage) coverages.get(i);
			if (coverage != null) {
				covExt = NbaUtils.getFirstCoverageExtension(coverage);
				if (covExt != null && covExt.hasBandTableIdentity()) {
					aNbaOinkRequest.addValue(covExt.getBandTableIdentity());
				} else {
					aNbaOinkRequest.addValue("");
				}
			}
		}
	}

	/**
	 * gets the value for UFSQuestionnaireCode based on the RelatedObjectID OLifE.FormInstance.FormInstanceExtension.UFSQuestionnaireCode is the code
	 * for the UFS Questions.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP005 New Method
	public void retrieveUFSQuestionnaireCode(NbaOinkRequest aNbaOinkRequest) {
		FormInstance formInstance = getFormInstanceByRelatedObjectType(aNbaOinkRequest);
		if (formInstance != null) {
			OLifEExtension oli = null;
			int index_extension = getExtensionIndex(formInstance.getOLifEExtension(), FORMINSTANCE_EXTN);
			if (index_extension != -1) {
				oli = formInstance.getOLifEExtensionAt(index_extension);
			}
			if (oli != null) {
				aNbaOinkRequest.addValue(oli.getFormInstanceExtension().getUFSQuestionnaireCode());
			}
		}
	}

	/**
	 * Obtain the value for GuarIntRate.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	public void retrieveGuarIntRate(NbaOinkRequest aNbaOinkRequest) {
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladh = getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
		if (ladh != null) {
			if (ladh.isAnnuity()) {
				aNbaOinkRequest.addValue(ladh.getAnnuity().getGuarIntRate());
			} else {
				List coverages = getCoveragesOrRiders(aNbaOinkRequest);
				int next = 0;
				Coverage coverage;
				while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
					if (coverage.hasGuarIntRate()) {
						aNbaOinkRequest.addValue(coverage.getGuarIntRate());
					} else {
						aNbaOinkRequest.addValue(0.0);
					}
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(0.0);
		}
	}

	/**
	 * Obtain the value for QuotedPremiumBasisAmt. Holding.Policy.PolicyExtension.QuotedPremiumBasisAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA104 New Method
	public void retrieveQuotedPremiumBasisAmt(NbaOinkRequest aNbaOinkRequest) {
		PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(getPolicy());
		if (policyExt != null) {
			aNbaOinkRequest.addValue(policyExt.getQuotedPremiumBasisAmt());
		}
	}

	/**
	 * Obtain the value for QuotedPremiumBasisFrequency. Holding.Policy.PolicyExtension.QuotedPremiumBasisFrequency
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA104 New Method
	public void retrieveQuotedPremiumBasisFrequency(NbaOinkRequest aNbaOinkRequest) {
		PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(getPolicy());
		if (policyExt != null) {
			aNbaOinkRequest.addValue(policyExt.getQuotedPremiumBasisFrequency());
		}
	}

	/**
	 * Obtain the value for a Coverage LivesType. Holding.Policy.Life.Coverage.LivesType is a livesType.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA104 New Method
	public void retrieveLivesType(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = null;
		String coverageType = aNbaOinkRequest.getQualifier();
		//Begin P2AXAL053
		if (coverageType.equals(NON_RIDER_COV)) {
			List coverages = getCoveragesOrRiders(aNbaOinkRequest);
			if (coverages != null) {
				int sizeCoverage = coverages.size();
				for (int i = 0; i < sizeCoverage; i++) {
					coverage = (Coverage) coverages.get(i);
					if (coverage.hasLivesType()) {
						aNbaOinkRequest.addValue(coverage.getLivesType());
					} else {
						aNbaOinkRequest.addValue(-1);
					}
				}
			}
		} else {
			coverage = getBaseCoverage();
			if (coverage != null && coverage.hasLivesType()) {
				aNbaOinkRequest.addValue(coverage.getLivesType());
		}
	}
		//End P2AXAL053
	}

	/**
	 * Obtain the value for ValuationBaseSeries.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	public void retrieveValuationBaseSeries(NbaOinkRequest aNbaOinkRequest) {
		if (nbaTXLife.isAnnuity()) {
			AnnuityExtension ext = NbaUtils.getFirstAnnuityExtension(getAnnuity());
			if (ext != null) {
				aNbaOinkRequest.addValue(ext.getValuationBaseSeries());
			}
		} else {
			List coverages = getCoveragesOrRiders(aNbaOinkRequest);
			int next = 0;
			Coverage coverage;
			while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
				CoverageExtension ext = NbaUtils.getFirstCoverageExtension(coverage);
				if (ext != null) {
					aNbaOinkRequest.addValue(ext.getValuationBaseSeries());
				} else {
					aNbaOinkRequest.addValue("");
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue("");
		}
	}

	/**
	 * Obtain the value for Temporary Table EffDate. If the Temporary Table Rating is not found attempt to use the Permanent Table Rating.
	 * Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.SubstandardRatingExtension.EffDate
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveTableRatingEffDate(NbaOinkRequest aNbaOinkRequest) {
		List participants = getInsurableLifeParticipants(aNbaOinkRequest);
		int next = 0;
		LifeParticipant lifeParticipant;
		int countSR;
		Date tempRatingEffDate = null;
		Date permRatingEffDate = null;
		SubstandardRatingExtension substandardExtension;
		SubstandardRating substandardRating;
		while ((lifeParticipant = getNextLifeParticipant(aNbaOinkRequest, participants, next++)) != null) {
			countSR = lifeParticipant.getSubstandardRatingCount();
			for (int j = 0; j < countSR; j++) {
				substandardRating = lifeParticipant.getSubstandardRatingAt(j);
				substandardExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
				if (NbaUtils.isValidRating(substandardRating) && substandardExtension != null) { //SPR2590
					if (substandardRating.hasTempTableRating() && tempRatingEffDate == null) {
						tempRatingEffDate = substandardExtension.getEffDate();
					} else if (substandardRating.hasPermTableRating() && permRatingEffDate == null) {
						permRatingEffDate = substandardExtension.getEffDate();
					}
				}
			}
		}
		if (tempRatingEffDate != null) {
			aNbaOinkRequest.addValue(tempRatingEffDate);
		} else if (permRatingEffDate != null) {
			aNbaOinkRequest.addValue(permRatingEffDate);
		} else {
			aNbaOinkRequest.addUnknownValue(new Date());
		}
	}

	/**
	 * Obtain the value for SecondaryDividendType. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.SecondaryDividendType is the Dividend
	 * Option.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveSecondaryDividendType(NbaOinkRequest aNbaOinkRequest) {
		Life life = getLife();
		if (life != null) {
			LifeExtension lifeExtension = NbaUtils.getFirstLifeExtension(life);
			if (lifeExtension != null) {
				aNbaOinkRequest.addValue(lifeExtension.getSecondaryDividendType(), NbaTableConstants.OLI_LU_DIVTYPE);
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0)
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_DIVTYPE);
	}

	/**
	 * Obtain the value for ValuationSubSeries Holding.Policy.Life.Coverage.OLifEExtension.CoverageExtension.ValuationClassType
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveValuationSubSeries(NbaOinkRequest aNbaOinkRequest) {
		CoverageExtension coverageExt;
		Coverage coverage;
		List coverages = getCoveragesOrRiders(aNbaOinkRequest);
		int next = 0;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			coverageExt = NbaUtils.getFirstCoverageExtension(coverage);
			if (coverageExt != null) {
				aNbaOinkRequest.addValue(coverageExt.getValuationSubSeries());
			} else {
				aNbaOinkRequest.addValue("");
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue("");
		}
	}

	/**
	 * Obtain the value for AgeAtPayout AgeAtPayout is the age at maturity for an Annuity. Participant.Issue Age + diffenence in years between
	 * Maturity Date (requested or calculated) and EffDate
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveAgeAtPayout(NbaOinkRequest aNbaOinkRequest) {
		int ageAtPayout = 0;
		if (getNbaTXLife().isAnnuity() && getPolicy().hasEffDate()) {
			Participant participant = getNbaTXLife().getPrimaryAnnuitantParticipant();
			if (participant != null && participant.hasIssueAge()) {
				ageAtPayout = participant.getIssueAge();
				Date maturityDate = getPolicy().getTermDate();
				if (maturityDate != null) {
					ageAtPayout = ageAtPayout + NbaUtils.calcYearsDiff(maturityDate, getPolicy().getEffDate());
				}
			}
		}
		aNbaOinkRequest.addValue(ageAtPayout);
	}

	/**
	 * Obtain the value for PayoutAmt Holding.Policy.Annuity.Payout.PayoutAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrievePayoutAmt(NbaOinkRequest aNbaOinkRequest) {
		double payoutAmt = 0;
		if (getNbaTXLife().isAnnuity()) {
			Payout payout = NbaUtils.getFirstPayout(getAnnuity());
			if (payout != null && payout.hasPayoutAmt()) {
				payoutAmt = payout.getPayoutAmt();
			}
		}
		aNbaOinkRequest.addValue(payoutAmt);
	}

	/**
	 * Obtain the value for PayoutAmt Holding.Policy.Annuity.Payout.PayoutMode
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrievePayoutMode(NbaOinkRequest aNbaOinkRequest) {
		long payoutMode = -1;
		if (getNbaTXLife().isAnnuity()) {
			Payout payout = NbaUtils.getFirstPayout(getAnnuity());
			if (payout != null && payout.hasPayoutMode()) {
				payoutMode = payout.getPayoutMode();
			}
		}
		aNbaOinkRequest.addValue(payoutMode);
	}

	/**
	 * Obtain the value for AnnuityBand Return a constant "1"
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveAnnuityBand(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue("1");
	}

	/**
	 * Obtain the value for AnnuityRateClass Return a constant "N"
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveAnnuityRateClass(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue("N");
	}

	/**
	 * Obtain the value for Phase Code. Holding.Policy.Annuity.AnnuityKey
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveAnnuityKey(NbaOinkRequest aNbaOinkRequest) {
		// SPR3290 code deleted
		String annuityKey = "";
		if (getNbaTXLife().isAnnuity()) {
			annuityKey = getAnnuity().getAnnuityKey();
		}
		aNbaOinkRequest.addValue(annuityKey);
	}

	/**
	 * Obtain the value for InitDepIntRateCurrent. Holding.Policy.Annuity.InitDepIntRateCurrent
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveInitDepIntRateCurrent(NbaOinkRequest aNbaOinkRequest) {
		double initDepIntRateCurrent = 0;
		if (getNbaTXLife().isAnnuity()) {
			initDepIntRateCurrent = getAnnuity().getInitDepIntRateCurrent();
		}
		aNbaOinkRequest.addValue(initDepIntRateCurrent);
	}

	/**
	 * Obtain the value for CurrIntRate. For Life - Holding.Policy.Life.CurrIntRate For Annuity - Holding.Policy.Annuity.InitDepIntRateCurrent
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveCurrIntRate(NbaOinkRequest aNbaOinkRequest) {
		double intRateCurrent = 0;
		if (getNbaTXLife().isAnnuity()) {
			intRateCurrent = getAnnuity().getInitDepIntRateCurrent();
		} else if (getNbaTXLife().isLife()) {
			intRateCurrent = getLife().getCurrIntRate();
		}
		aNbaOinkRequest.addValue(intRateCurrent);
	}

	/**
	 * Obtain the number of years for Temporary Flat Substandard Rating. Calculated as the difference in years between
	 * Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.Duration
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveTempFlatExtraYears(NbaOinkRequest aNbaOinkRequest) {
		List ratings = getTempFlatSubstandardRatingsForParty(aNbaOinkRequest); // CR1343973
		SubstandardRating substandardRating;
		SubstandardRatingExtension substandardRatingExt;
		//if we only have 1 rating, then don't resolve variable as this is reserved for the 2nd temp rating
		// QC1230 code deleted
		// CR1343973 method refactored begin
		if (ratings.size() >= 1 && ratings.get(0) != null) {
			substandardRating = (SubstandardRating) ratings.get(0);
			substandardRatingExt = NbaUtils.getFirstSubstandardExtension(substandardRating);
			if (substandardRatingExt.hasDuration()) {
				aNbaOinkRequest.addValue(substandardRatingExt.getDuration());
			} else {
				aNbaOinkRequest.addUnknownValue("");
			}
		} else {
			aNbaOinkRequest.addUnknownValue("");
		}
		// CR1343973 method refactored end
	}

	/**
	 * Obtain the number of years for Temporary Flat Substandard Rating. Calculated as the difference in years between
	 * Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.Duration
	 * @param aNbaOinkRequest - data request container
	 */
	// CR1343973 New Method
	public void retrieveTempFlatSecondExtraYears(NbaOinkRequest aNbaOinkRequest) {
		List ratings = getTempFlatSubstandardRatingsForParty(aNbaOinkRequest); // CR1343973
		SubstandardRating substandardRating;
		SubstandardRatingExtension substandardRatingExt;
		//if we only have 1 rating, then don't resolve variable as this is reserved for the 2nd temp rating
		// QC1230 code deleted
		// CR1343973 method refactored begin
		if (ratings.size() >= 2 && ratings.get(1) != null) {
			substandardRating = (SubstandardRating) ratings.get(1);
			substandardRatingExt = NbaUtils.getFirstSubstandardExtension(substandardRating);
			if (substandardRatingExt.hasDuration()) {	
				aNbaOinkRequest.addValue(substandardRatingExt.getDuration());
			} else {
				aNbaOinkRequest.addUnknownValue("");
			}
		} else {
			aNbaOinkRequest.addUnknownValue("");
		}
		// CR1343973 method refactored end
	}
	
	/**
	 * Obtain the number of years for Temporary Flat Substandard Rating. Calculated as the difference in years between
	 * Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.Duration
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.13I New Method
	public void retrieveTempFlatThirdExtraYears(NbaOinkRequest aNbaOinkRequest) {
		List ratings = getTempFlatSubstandardRatingsForParty(aNbaOinkRequest); // CR1343973
		SubstandardRating substandardRating;
		SubstandardRatingExtension substandardRatingExt;
		// CR1343973 method refactored begin
		if (ratings.size() >= 3 && ratings.get(2) != null) {
			substandardRating = (SubstandardRating) ratings.get(2);
			substandardRatingExt = NbaUtils.getFirstSubstandardExtension(substandardRating);
			if (substandardRatingExt.hasDuration()) {	
				aNbaOinkRequest.addValue(substandardRatingExt.getDuration());
			} else {
				aNbaOinkRequest.addUnknownValue("");
			}
		} else {
			aNbaOinkRequest.addUnknownValue("");
		}
		// CR1343973 method refactored end
	}

	/**
	 * Obtain the number of years for Permanent Flat Substandard Rating. Calculated as the difference in years between
	 * Holding.Policy.Life.Coverage.TermDate and Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.EffDate
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrievePermFlatExtraYears(NbaOinkRequest aNbaOinkRequest) {
		List ratings = getPermFlatSubstandardRatings(aNbaOinkRequest);
		List coverages = getCoveragesOrRiders(aNbaOinkRequest);
		Coverage coverage;
		SubstandardRatingExtension substandardRatingExt;
		for (int i = 0; i < ratings.size(); i++) {
			if (ratings.get(i) == null) {
				aNbaOinkRequest.addUnknownValue("");
			} else {
				coverage = (Coverage) coverages.get(i);
				substandardRatingExt = NbaUtils.getFirstSubstandardExtension((SubstandardRating) ratings.get(i));
				if (coverage.hasTermDate() && substandardRatingExt.hasEffDate()) {
					aNbaOinkRequest.addValue(NbaUtils.calcYearsDiff(coverage.getTermDate(), substandardRatingExt.getEffDate()));
				} else {
					aNbaOinkRequest.addUnknownValue("");
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addUnknownValue("");
		}
	}

	/**
	 * Obtain the value for CWAAmt. OLifE.Holding.Policy.ApplicationInfo.CWAAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveCWAAmt(NbaOinkRequest aNbaOinkRequest) {
		if (getApplicationInfo() != null) {
			aNbaOinkRequest.addValue(getApplicationInfo().getCWAAmt(), FORMAT_TYPE_CURRENCY);//ALII2039
		} else {
			aNbaOinkRequest.addValue(0L);
		}
	}

	/**
	 * Obtain the value for AssumedInterestRate. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Annuity.Payout.AssumedInterestRate is the
	 * assumed interest rate to be used to calculate the payout for an annuity.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveAssumedInterestRate(NbaOinkRequest aNbaOinkRequest) {
		double rate = 0;
		if (getNbaTXLife().isAnnuity()) {
			Payout payout = NbaUtils.getFirstPayout(getAnnuity());
			if (payout != null && payout.hasAssumedInterestRate()) {
				rate = payout.getAssumedInterestRate();
			}
		}
		aNbaOinkRequest.addValue(rate);
	}

	/**
	 * Obtain the value for NumModalPayouts. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Annuity.Payout.NumModalPayouts is the number of
	 * modal payouts for an annuity.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveNumModalPayouts(NbaOinkRequest aNbaOinkRequest) {
		int numPayouts = 0;
		if (getNbaTXLife().isAnnuity()) {
			Payout payout = NbaUtils.getFirstPayout(getAnnuity());
			if (payout != null && payout.hasNumModalPayouts()) {
				numPayouts = payout.getNumModalPayouts();
			}
		}
		aNbaOinkRequest.addValue(numPayouts);
	}

	/**
	 * Obtain the value for RoleCodeDesc from OLifE.Party.Risk.FamilyIllness.RoleCodeDesc
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP013 New Method
	public void retrieveRoleCodeDesc(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null && party.hasRisk()) {
			ArrayList list = getRisk(party).getFamilyIllness();
			int count = list.size();
			for (int i = 0; i < count; i++) {
				FamilyIllness familyIllness = (FamilyIllness) list.get(i);
				if (familyIllness != null) {
					aNbaOinkRequest.addValue(familyIllness.getRoleCodeDesc());
				} else {
					break;
				}
			}
		}
	}

	/**
	 * Obtain the value for DivOnDepositAmt. For Life - Holding.Policy.Life.DivOnDepositAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveDivOnDepositAmt(NbaOinkRequest aNbaOinkRequest) {
		double amt = 0;
		if (getNbaTXLife().isLife()) {
			amt = getLife().getDivOnDepositAmt();
		}
		aNbaOinkRequest.addValue(amt);
	}

	/**
	 * Obtain the value for OYTPurchaseAmt. For Life - Holding.Policy.Life.OYTPurchaseAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrieveOYTPurchaseAmt(NbaOinkRequest aNbaOinkRequest) {
		double amt = 0;
		if (getNbaTXLife().isLife()) {
			amt = getLife().getOYTPurchaseAmt();
		}
		aNbaOinkRequest.addValue(amt); // SPR3290
	}

	/**
	 * Obtain the value for InitCovAmt. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.Coverage.InitCovAmt is the Initial Face without
	 * options.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	public void retrieveInitCovAmt(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = null;
		List coverages = getCoveragesOrRiders(aNbaOinkRequest);
		int next = 0;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			if (coverage.hasInitCovAmt()) {
				aNbaOinkRequest.addValue(coverage.getInitCovAmt(), FORMAT_TYPE_CURRENCY);
			} else {
				aNbaOinkRequest.addValue(0);
			}
		}
	}

	/**
	 * Obtain the value for GuidelineSinglePrem. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.LifeUSA.GuidelineSinglePrem or
	 * OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.Coverage.GuidelineSinglePrem is the Guideline Single Premium amount.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	public void retrieveGuidelineSinglePrem(NbaOinkRequest aNbaOinkRequest) {
		// SPR3290 code deleted
		if (getNbaTXLife().isLife()) {
			String coverageType = aNbaOinkRequest.getQualifier();
			if (coverageType.length() == 0) {
				Life life = getNbaTXLife().getLife();
				if (life != null) {
					LifeUSA lifeUSA = life.getLifeUSA();
					if (lifeUSA != null) {
						if (lifeUSA.hasGuidelineSinglePrem()) {
							aNbaOinkRequest.addValue(lifeUSA.getGuidelineSinglePrem());
						} else {
							aNbaOinkRequest.addValue(0);
						}
					}
				}
			} else {
				List coverages = getCoveragesOrRiders(aNbaOinkRequest);
				int next = 0;
				Coverage coverage;
				while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
					if (coverage.hasGuidelineSinglePrem()) {
						aNbaOinkRequest.addValue(coverage.getGuidelineSinglePrem(), FORMAT_TYPE_CURRENCY);
					} else {
						aNbaOinkRequest.addValue(0);
					}
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(0);
		}
	}

	/**
	 * Obtain the value for GuidelineAnnPrem. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.LifeUSA.GuidelineAnnPrem or
	 * OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.Coverage.GuidelineSinglePrem is the Guideline Annual Premium amount.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	public void retrieveGuidelineAnnPrem(NbaOinkRequest aNbaOinkRequest) {
		// SPR3290 code deleted
		if (getNbaTXLife().isLife()) {
			String coverageType = aNbaOinkRequest.getQualifier();
			if (coverageType.length() == 0) {
				Life life = getNbaTXLife().getLife();
				if (life != null) {
					LifeUSA lifeUSA = life.getLifeUSA();
					if (lifeUSA != null) {
						if (lifeUSA.hasGuidelineAnnPrem()) {
							aNbaOinkRequest.addValue(lifeUSA.getGuidelineAnnPrem());
						} else {
							aNbaOinkRequest.addValue(0);
						}
					}
				}
			} else {
				List coverages = getCoveragesOrRiders(aNbaOinkRequest);
				int next = 0;
				Coverage coverage;
				while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
					if (coverage.hasGuidelineAnnPrem()) {
						aNbaOinkRequest.addValue(coverage.getGuidelineAnnPrem(), FORMAT_TYPE_CURRENCY);
					} else {
						aNbaOinkRequest.addValue(0);
					}
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(0);
		}
	}

	/**
	 * gets the value for AppPendingInd OlifE.Party.Risk.RiskExtension.AppPendingInd is the Application Pending Indicator for the Insured.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP009 New Method
	public void retrieveAppPendingInd(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk risk = party.getRisk();
			if (risk != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(risk.getOLifEExtension(), RISK_EXTN);
				if (index_extension != -1) {
					oli = risk.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					aNbaOinkRequest.addValue(oli.getRiskExtension().getAppPendingInd());
				}
			}
		}
	}

	/**
	 * gets the value for IndemnityInd OlifE.Party.Risk.RiskExtension.IndemnityInd is the Application Pending Indicator for the Insured.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP009 New Method
	public void retrieveIndemnityInd(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk risk = party.getRisk();
			if (risk != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(risk.getOLifEExtension(), RISK_EXTN);
				if (index_extension != -1) {
					oli = risk.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					aNbaOinkRequest.addValue(oli.getRiskExtension().getIndemnityInd());
				}
			}
		}
	}

	/**
	 * gets the value for RejectionInd OlifE.Party.Risk.RejectionInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP009 New Method
	public void retrieveRejectionInd(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk risk = party.getRisk();
			if (risk != null) {
				aNbaOinkRequest.addValue(risk.getRejectionInd());
			}
		}
	}

	/**
	 * gets the value for ReceivingDisabilityBenefitsInd OlifE.Party.Risk.ReceivingDisabilityBenefitsInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP009 New Method
	public void retrieveReceivingDisabilityBenefitsInd(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk risk = party.getRisk();
			if (risk != null) {
				aNbaOinkRequest.addValue(risk.getReceivingDisabilityBenefitsInd());
			}
		}
	}

	/**
	 * gets the value for InsRatedInd OlifE.Party.Risk.RiskExtension.InsRatedInd is the Application Pending Indicator for the Insured.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP009 New Method
	public void retrieveInsRatedInd(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk risk = party.getRisk();
			if (risk != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(risk.getOLifEExtension(), RISK_EXTN);
				if (index_extension != -1) {
					oli = risk.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					aNbaOinkRequest.addValue(oli.getRiskExtension().getInsRatedInd());
				}
			}
		}
	}

	/**
	 * gets the value for AIDSInd OlifE.Party.Risk.AIDSInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP009 New Method
	public void retrieveAIDSInd(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk risk = party.getRisk();
			if (risk != null) {
				aNbaOinkRequest.addValue(risk.getAIDSInd());
			}
		}
	}

	/**
	 * gets the value for LastActivityDate OlifE.Party.Risk.LastActivityDate
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP009 New Method
	public void retrieveLastActivityDate(NbaOinkRequest aNbaOinkRequest) {
		int index = aNbaOinkRequest.getElementIndexFilter();
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk risk = party.getRisk();
			if (risk != null) {
				ArrayList lifeStyleActivityList = risk.getLifeStyleActivity();
				if (index <= lifeStyleActivityList.size() && (lifeStyleActivityList.size() > 0)) {
					aNbaOinkRequest.addValue(((LifeStyleActivity) lifeStyleActivityList.get(index)).getLastActivityDate());
				}
			}
		}
	}

	/**
	 * gets the value for CrimeDescription OLifE.Party.Risk.CriminalConviction.CrimeDescription
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP009 New Method
	public void retrieveCrimeDescription(NbaOinkRequest aNbaOinkRequest) {
		int index = aNbaOinkRequest.getElementIndexFilter();
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk risk = party.getRisk();
			if (risk != null) {
				ArrayList criminalConvictionList = risk.getCriminalConviction();
				if ((index <= criminalConvictionList.size()) && (criminalConvictionList.size() > 0)) {
					aNbaOinkRequest.addValue(((CriminalConviction) criminalConvictionList.get(index)).getCrimeDescription());
				}
			}
		}
	}

	/**
	 * gets the value for CrimeType OLifE.Party.Risk.CriminalConviction.CrimeType
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP009 New Method
	public void retrieveCrimeType(NbaOinkRequest aNbaOinkRequest) {
		int index = aNbaOinkRequest.getElementIndexFilter();
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk risk = party.getRisk();
			if (risk != null) {
				ArrayList criminalConvictionList = risk.getCriminalConviction();
				if (index <= criminalConvictionList.size() && (criminalConvictionList.size() > 0)) {
					aNbaOinkRequest.addValue(((CriminalConviction) criminalConvictionList.get(index)).getCrimeType());
				}
			}
		}
	}

	/**
	 * Obtain the value for PremLoadTargetAmt. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.PremLoadTargetAmt is the Premium Load target
	 * Amount.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA100 New Method
	public void retrievePremLoadTargetAmt(NbaOinkRequest aNbaOinkRequest) {
		Life life = getLife();
		if (life != null) {
			LifeExtension lifeExtension = NbaUtils.getFirstLifeExtension(life);
			if (lifeExtension != null && lifeExtension.hasPremLoadTargetAmt()) {
				aNbaOinkRequest.addValue(lifeExtension.getPremLoadTargetAmt(), FORMAT_TYPE_CURRENCY);
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0)
			aNbaOinkRequest.addValue(0, FORMAT_TYPE_CURRENCY);
	}

	//
	/**
	 * Obtain the value for DefLifeInsMethod. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.LifeUSA.DefLifeInsMethod is the Cash Value or
	 * Guideline Annual Premiums definition of life insurance test.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	public void retrieveDefLifeInsMethod(NbaOinkRequest aNbaOinkRequest) {
		Life life = getNbaTXLife().getLife();
		if (life != null) {
			LifeUSA lifeUSA = life.getLifeUSA();
			if (lifeUSA != null && lifeUSA.hasDefLifeInsMethod()) {
				aNbaOinkRequest.addValue(lifeUSA.getDefLifeInsMethod());
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1);
		}
	}

	/**
	 * Obtain the value for AnnualizedPaymentAmt. OLifE.Holding.Policy.PaymentAmt is the current modal payment/premium amount multiplied by the number
	 * of payments to make it an annual amount.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	public void retrieveAnnualizedPaymentAmt(NbaOinkRequest aNbaOinkRequest) {
		//NBA117 code deleted
		aNbaOinkRequest.addValue(NbaUtils.getAnnualizedPaymentAmt(getPolicy()), FORMAT_TYPE_CURRENCY); //NBA117
	}

	/**
	 * Obtain the values for Substandard Extra Cease date. For Permanent extras the Cease value is the TermDate of the associated coverage. For
	 * Temprary extras, the value is either SubstandardRating.TempTableRatingEndDate or SubstandardRating.TempFlatEndDate.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	public void retrieveExtraCeaseDate(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		LifeParticipant lifeParticipant;
		SubstandardRating substandardRating;
		Date date;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			int lifeIdx = coverage.getLifeParticipantCount();
			for (int i = 0; i < lifeIdx; i++) {
				lifeParticipant = coverage.getLifeParticipantAt(i);
				if (participants.contains(lifeParticipant)) {
					int ratIdx = lifeParticipant.getSubstandardRatingCount();
					for (int j = 0; j < ratIdx; j++) {
						date = null;
						substandardRating = lifeParticipant.getSubstandardRatingAt(j);
						if (NbaUtils.isValidRating(substandardRating)) { //SPR2590
							String type = NbaUtils.getSubstandardRatingType(substandardRating);
							if (NbaConstants.SUB_STAND_TYPE_PERM_TABLE.equals(type)) {
								date = coverage.getTermDate();
							} else if (NbaConstants.SUB_STAND_TYPE_PERM_FLAT.equals(type)) {
								date = coverage.getTermDate();
							} else if (NbaConstants.SUB_STAND_TYPE_TEMP_TABLE.equals(type)) {
								date = substandardRating.getTempTableRatingEndDate();
							} else if (NbaConstants.SUB_STAND_TYPE_TEMP_FLAT.equals(type)) {
								date = substandardRating.getTempFlatEndDate();
							} else if (NbaConstants.SUB_STAND_TYPE_PERCENT.equals(type)) {
								date = coverage.getTermDate();
							}
							if (date == null) {
								aNbaOinkRequest.addUnknownValue(new Date());
							} else {
								aNbaOinkRequest.addValue(date);
							}
						} //SPR2590
					}
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addUnknownValue(new Date());
		}
	}

	/**
	 * Obtain the values for Substandard Extra Effective date. SubstandardRating.SubstandardRatingExtension.EffDate.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	public void retrieveExtraEffDate(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		LifeParticipant lifeParticipant;
		SubstandardRating substandardRating;
		SubstandardRatingExtension ext;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			int lifeIdx = coverage.getLifeParticipantCount();
			for (int i = 0; i < lifeIdx; i++) {
				lifeParticipant = coverage.getLifeParticipantAt(i);
				if (participants.contains(lifeParticipant)) {
					int ratIdx = lifeParticipant.getSubstandardRatingCount();
					for (int j = 0; j < ratIdx; j++) {
						substandardRating = lifeParticipant.getSubstandardRatingAt(j);
						ext = NbaUtils.getFirstSubstandardExtension(substandardRating);
						if (NbaUtils.isValidRating(substandardRating)) { //SPR2590
							if (ext == null) {
								aNbaOinkRequest.addUnknownValue(new Date());
							} else {
								aNbaOinkRequest.addValue(ext.getEffDate());
							}
						}//SPR2590
					}
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addUnknownValue(new Date());
		}
	}

	/**
	 * Obtain the values for Substandard Extra Flat Amount. For Flat extras the value is SubstandardRating.TempFlatExtraAmt or
	 * SubstandardRating.SubstandardRatingExtension.PermFlatExtraAmt. For other extras, value is zero.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	public void retrieveExtraAmt(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		LifeParticipant lifeParticipant;
		SubstandardRating substandardRating;
		SubstandardRatingExtension ext;
		double amt;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			int lifeIdx = coverage.getLifeParticipantCount();
			for (int i = 0; i < lifeIdx; i++) {
				lifeParticipant = coverage.getLifeParticipantAt(i);
				if (participants.contains(lifeParticipant)) {
					int ratIdx = lifeParticipant.getSubstandardRatingCount();
					for (int j = 0; j < ratIdx; j++) {
						amt = 0;
						substandardRating = lifeParticipant.getSubstandardRatingAt(j);
						if (NbaUtils.isValidRating(substandardRating)) { //SPR2590
							String type = NbaUtils.getSubstandardRatingType(substandardRating);
							if (NbaConstants.SUB_STAND_TYPE_PERM_FLAT.equals(type)) {
								ext = NbaUtils.getFirstSubstandardExtension(substandardRating);
								if (ext != null) {
									amt = ext.getPermFlatExtraAmt();
								}
							} else if (NbaConstants.SUB_STAND_TYPE_TEMP_FLAT.equals(type)) {
								amt = substandardRating.getTempFlatExtraAmt();
							}
							aNbaOinkRequest.addValue(amt);
						} //SPR2590
					}
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(0.0);
		}
	}

	/**
	 * Obtain the values for Substandard Extra Percentage. For Percentage extras the value is
	 * SubstandardRating.SubstandardRatingExtension.PermPercentageLoading. For other extras, value is zero.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	public void retrieveExtraPct(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		LifeParticipant lifeParticipant;
		SubstandardRating substandardRating;
		SubstandardRatingExtension ext;
		double pct;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			int lifeIdx = coverage.getLifeParticipantCount();
			for (int i = 0; i < lifeIdx; i++) {
				lifeParticipant = coverage.getLifeParticipantAt(i);
				if (participants.contains(lifeParticipant)) {
					int ratIdx = lifeParticipant.getSubstandardRatingCount();
					for (int j = 0; j < ratIdx; j++) {
						pct = 0;
						substandardRating = lifeParticipant.getSubstandardRatingAt(j);
						if (NbaUtils.isValidRating(substandardRating)) { //SPR2590
							String type = NbaUtils.getSubstandardRatingType(substandardRating);
							if (NbaConstants.SUB_STAND_TYPE_PERCENT.equals(type)) {
								ext = NbaUtils.getFirstSubstandardExtension(substandardRating);
								if (ext != null) {
									aNbaOinkRequest.addUnknownValue(new Date());
									pct = ext.getPermPercentageLoading();
								}
							}
							aNbaOinkRequest.addValue(pct);
						} //SPR2590
					}
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(0);
		}
	}

	/**
	 * Obtain the values for Substandard Extra RelationRoleCode. This is the RelationRoleCode of the Party associated with the Extra.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	public void retrieveExtraRelationRoleCode(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		LifeParticipant lifeParticipant;
		// SPR3290 code deleted
		long code = -1;
		String partyId;
		Object[] relations = getOLifE().getRelation().toArray();
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			int lifeIdx = coverage.getLifeParticipantCount();
			for (int i = 0; i < lifeIdx; i++) {
				lifeParticipant = coverage.getLifeParticipantAt(i);
				if (participants.contains(lifeParticipant)) {
					partyId = lifeParticipant.getPartyID();
					code = -1;
					if (partyId != null && partyId.length() > 0) {
						Relation relation = NbaUtils.getRelationForParty(partyId, relations);
						if (relation != null) {
							code = relation.getRelationRoleCode();
						}
					}
					int ratIdx = lifeParticipant.getSubstandardRatingCount();
					for (int j = 0; j < ratIdx; j++) {
						aNbaOinkRequest.addValue(code);
					}
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L);
		}
	}

	/**
	 * Obtain the values for Substandard Extra RelatedRefID. This is the RelatedRefID of the Party associated with the Extra.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	public void retrieveExtraRelatedRefID(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		LifeParticipant lifeParticipant;
		// SPR3290 code deleted
		String code = "";
		String partyId;
		Object[] relations = getOLifE().getRelation().toArray();
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			int lifeIdx = coverage.getLifeParticipantCount();
			for (int i = 0; i < lifeIdx; i++) {
				lifeParticipant = coverage.getLifeParticipantAt(i);
				if (participants.contains(lifeParticipant)) {
					partyId = lifeParticipant.getPartyID();
					code = "";
					if (partyId != null && partyId.length() > 0) {
						Relation relation = NbaUtils.getRelationForParty(partyId, relations);
						if (relation != null) {
							code = relation.getRelatedRefID();
						}
					}
					int ratIdx = lifeParticipant.getSubstandardRatingCount();
					for (int j = 0; j < ratIdx; j++) {
						aNbaOinkRequest.addValue(code);
					}
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue("");
		}
	}

	/**
	 * Obtain the values for Substandard Extra Table Rating. For Table extras the value is SubstandardRating.PermTableRating or
	 * SubstandardRating.TempTableRating. For other extras, value is zero.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	public void retrieveExtraTableRating(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		LifeParticipant lifeParticipant;
		SubstandardRating substandardRating;
		long rating;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			int lifeIdx = coverage.getLifeParticipantCount();
			for (int i = 0; i < lifeIdx; i++) {
				lifeParticipant = coverage.getLifeParticipantAt(i);
				if (participants.contains(lifeParticipant)) {
					int ratIdx = lifeParticipant.getSubstandardRatingCount();
					for (int j = 0; j < ratIdx; j++) {
						rating = -1;
						substandardRating = lifeParticipant.getSubstandardRatingAt(j);
						if (NbaUtils.isValidRating(substandardRating)) { //SPR2590
							String type = NbaUtils.getSubstandardRatingType(substandardRating);
							if (NbaConstants.SUB_STAND_TYPE_PERM_TABLE.equals(type)) {
								rating = substandardRating.getPermTableRating();
							} else if (NbaConstants.SUB_STAND_TYPE_TEMP_TABLE.equals(type)) {
								rating = substandardRating.getTempTableRating();
							}
							aNbaOinkRequest.addValue(rating);
						} //SPR2590
					}
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L);
		}
	}

	/**
	 * Obtain the values for Substandard Extra Type.
	 * @see com.csc.fsg.nba.foundation.NbaConstants#SUB_STAND_TYPE_PERCENT SubstandardRating constants
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	public void retrieveExtraType(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		LifeParticipant lifeParticipant;
		SubstandardRating substandardRating;
		String type;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			int lifeIdx = coverage.getLifeParticipantCount();
			for (int i = 0; i < lifeIdx; i++) {
				lifeParticipant = coverage.getLifeParticipantAt(i);
				if (participants.contains(lifeParticipant)) {
					int ratIdx = lifeParticipant.getSubstandardRatingCount();
					for (int j = 0; j < ratIdx; j++) {
						substandardRating = lifeParticipant.getSubstandardRatingAt(j);
						if (NbaUtils.isValidRating(substandardRating)) { //SPR2590
							type = NbaUtils.getSubstandardRatingType(substandardRating);
							aNbaOinkRequest.addValue(type);
						}
					}
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue("");
		}
	}

	/**
	 * Obtain the value for CountExtra - the count of Substandard Extras.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	public void retrieveCountExtra(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		LifeParticipant lifeParticipant;
		SubstandardRating substandardRating = null; //SPR2590
		int ratIdx = 0; //SPR2590
		int count = 0;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			int lifeIdx = coverage.getLifeParticipantCount();
			for (int i = 0; i < lifeIdx; i++) {
				lifeParticipant = coverage.getLifeParticipantAt(i);
				if (participants.contains(lifeParticipant)) {
					//begin SPR2590
					ratIdx = lifeParticipant.getSubstandardRatingCount();
					for (int j = 0; j < ratIdx; j++) {
						substandardRating = lifeParticipant.getSubstandardRatingAt(j);
						if (NbaUtils.isValidRating(substandardRating)) {
							count++;
						}
					}
					//end SPR2590
				}
			}
		}
		aNbaOinkRequest.addValue(count);
	}

	/**
	 * Obtain the End Date for Table Rating. If the Temporary Table Rating is not found try to return the Permanent Table Rating. For Temporary Tables
	 * ratinsg, return Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.TempTableRatingEndDate. For Permanent Table Ratings, return
	 * Holding.Policy.Life.Coverage.TermDate For other extras, there is no value.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	public void retrieveTableRatingEndDate(NbaOinkRequest aNbaOinkRequest) {
		Date tempDate = null;
		Date permDate = null;
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		LifeParticipant lifeParticipant;
		SubstandardRating substandardRating;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			int lifeIdx = coverage.getLifeParticipantCount();
			for (int i = 0; i < lifeIdx; i++) {
				lifeParticipant = coverage.getLifeParticipantAt(i);
				if (participants.contains(lifeParticipant)) {
					int ratIdx = lifeParticipant.getSubstandardRatingCount();
					for (int j = 0; j < ratIdx; j++) {
						substandardRating = lifeParticipant.getSubstandardRatingAt(j);
						if (NbaUtils.isValidRating(substandardRating)) { //SPR2590
							String type = NbaUtils.getSubstandardRatingType(substandardRating);
							if (NbaConstants.SUB_STAND_TYPE_TEMP_TABLE.equals(type) && tempDate == null) {
								tempDate = substandardRating.getTempTableRatingEndDate();
							} else if (NbaConstants.SUB_STAND_TYPE_PERM_TABLE.equals(type) && permDate == null) {
								permDate = coverage.getTermDate();
							}
						} //SPR2590
					}
				}
			}
			if (tempDate != null) {
				aNbaOinkRequest.addValue(tempDate);
			} else if (permDate != null) {
				aNbaOinkRequest.addValue(permDate);
			} else {
				aNbaOinkRequest.addUnknownValue(new Date());
			}
		}
	}

	/**
	 * Obtain the End Date for first Flat Rating. For Temporary Flat ratings, return
	 * Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.TempFlatEndDate. For Permanent Flat ratings, return
	 * Holding.Policy.Life.Coverage.TermDate For other Extras, there is no value.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	//ALII2009 Method refactored
	public void retrieveFlatEndDate(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		SubstandardRating substandardRating;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			Date endDate = null;
			List flatExtraRatings = NbaUtils.getFlatExtraRatings(coverage, participants);
			substandardRating = (SubstandardRating) flatExtraRatings.get(0);
			if(substandardRating != null) {
				if(NbaUtils.isPermFlatExtra(substandardRating)) {
					endDate = coverage.getTermDate();
				}else if(NbaUtils.isTempFlatExtra(substandardRating)){
					endDate = substandardRating.getTempFlatEndDate();
				}
			}
			if (endDate != null) {
				aNbaOinkRequest.addValue(endDate);
			} else {
				aNbaOinkRequest.addUnknownValue(new Date());
			}
		}
	}

	/**
	 * Obtain the Amount for the first Flat Rating. For Temporary Flat ratings, return
	 * Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.TempFlatExtraAmt. For Permanent Flat ratings, return
	 * Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.SubstandardRatingExtension.PermFlatExtraAmt. For other Extras, there is no
	 * value.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	//ALII2009 Method refactored
	public void retrieveFlatExtraAmt(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		SubstandardRating substandardRating;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			double extraAmt = Double.NaN;
			List flatExtraRatings = NbaUtils.getFlatExtraRatings(coverage, participants);
			substandardRating = (SubstandardRating) flatExtraRatings.get(0);
			if(substandardRating != null) {
				if(NbaUtils.isPermFlatExtra(substandardRating)) {
					SubstandardRatingExtension substandardExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
					if(substandardExtension != null) {
						extraAmt = substandardExtension.getPermFlatExtraAmt();	
					}
				}else if(NbaUtils.isTempFlatExtra(substandardRating)){
					extraAmt = substandardRating.getTempFlatExtraAmt();
				}	
			}
			aNbaOinkRequest.addValue(extraAmt);
		}
	}

	/**
	 * Obtain the Effective Date for a Temporary Flat rating. If the Temporary Flat rating is not found attempt to use the Permanent Flat rating.
	 * Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.SubstandardRatingExtension.EffDate
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA104 New Method
	//ALII2009 Method refactored
	public void retrieveFlatExtraEffDate(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		SubstandardRating substandardRating;
		SubstandardRatingExtension substandardExtension;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			Date effDate = null;
			List flatExtraRatings = NbaUtils.getFlatExtraRatings(coverage, participants);
			substandardRating = (SubstandardRating) flatExtraRatings.get(0);
			substandardExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
			if(substandardRating != null && substandardExtension != null) {
				if(NbaUtils.isPermFlatExtra(substandardRating)) {
					effDate = substandardExtension.getEffDate();
				}else if(NbaUtils.isTempFlatExtra(substandardRating)){
					effDate = substandardExtension.getEffDate();
				}	
			}
			if (effDate != null) {
				aNbaOinkRequest.addValue(effDate);
			} else {
				aNbaOinkRequest.addUnknownValue(new Date());
			}
		}
	}

	/**
	 * Obtain the number of years for a Temporary Flat rating. If the Temporary Flat rating is not found attempt to use the Permanent Flat rating. The
	 * Temporary Flat rating, return Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.Duration For a Permanent Flat rating, calculated
	 * as the difference in years between Holding.Policy.Life.Coverage.TermDate and
	 * Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.EffDate
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA104 New Method
	//ALII2009 Method refactored
	public void retrieveFlatExtraYears(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		SubstandardRating substandardRating;
		SubstandardRatingExtension substandardExtension;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			int extraYears = -1;
			List flatExtraRatings = NbaUtils.getFlatExtraRatings(coverage, participants);
			substandardRating = (SubstandardRating) flatExtraRatings.get(0);
			substandardExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
			if(substandardRating != null && substandardExtension != null) {
				if(NbaUtils.isPermFlatExtra(substandardRating)) {
					extraYears = NbaUtils.calcYearsDiff(substandardExtension.getEndDate(), substandardExtension.getEffDate());
				}else if(NbaUtils.isTempFlatExtra(substandardRating)){
					extraYears = substandardExtension.getDuration();
				}	
			}
			if(extraYears != -1){
				aNbaOinkRequest.addValue(extraYears);
			}else {
				aNbaOinkRequest.addUnknownValue("");	
			}
		}
	}


	/**
	 * Obtain the End Date for 2nd Flat Rating. For Temporary Flat ratings, return
	 * Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.TempFlatEndDate. For Permanent Flat ratings, return
	 * Holding.Policy.Life.Coverage.TermDate
	 * 
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	//ALII2009 Method refactored
	public void retrieve2ndFlatEndDate(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		SubstandardRating substandardRating;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			Date endDate = null;
			List flatExtraRatings = NbaUtils.getFlatExtraRatings(coverage, participants);
			substandardRating = (SubstandardRating) flatExtraRatings.get(1);
			if(substandardRating != null) {
				if(NbaUtils.isPermFlatExtra(substandardRating)) {
					endDate = coverage.getTermDate();
				}else if(NbaUtils.isTempFlatExtra(substandardRating)){
					endDate = substandardRating.getTempFlatEndDate();
				}	
			}
			if (endDate != null) {
				aNbaOinkRequest.addValue(endDate);
			} else {
				aNbaOinkRequest.addUnknownValue(new Date());
			}
		}
	}

	/**
	 * Obtain the Amount for 2nd Flat Rating. If a Temporary Flat Rating and a Permanent Flat Rating are present, use the Permanent Flat Rating.
	 * Otherwise there is no value. Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.SubstandardRatingExtension.PermFlatExtraAmt.
	 * @param aNbaOinkRequest - data request container
	 */
	// NBA100 New Method
	//ALII2009 Method refactored
	public void retrieve2ndFlatExtraAmt(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		SubstandardRating substandardRating;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			double extraAmt = Double.NaN;
			List flatExtraRatings = NbaUtils.getFlatExtraRatings(coverage, participants);
			substandardRating = (SubstandardRating) flatExtraRatings.get(1);
			if(substandardRating != null) {
				if(NbaUtils.isPermFlatExtra(substandardRating)) {
					SubstandardRatingExtension substandardExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
					if(substandardExtension != null) {
						extraAmt = substandardExtension.getPermFlatExtraAmt();	
					}
				}else if(NbaUtils.isTempFlatExtra(substandardRating)){
					extraAmt = substandardRating.getTempFlatExtraAmt();
				}	
			}
			aNbaOinkRequest.addValue(extraAmt);
		}
	}

	/**
	 * Obtain the Effective Date for a 2nd Flat rating. If a Temporary Flat Rating and a Permanent Flat Rating are present, use the Permanent Flat
	 * Rating. Otherwise there is no value. Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.SubstandardRatingExtension.EffDate
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA104 New Method
	//ALII2009 Method refactored
	public void retrieve2ndFlatExtraEffDate(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		SubstandardRating substandardRating;
		SubstandardRatingExtension substandardExtension;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			Date effDate = null;
			List flatExtraRatings = NbaUtils.getFlatExtraRatings(coverage, participants);
			substandardRating = (SubstandardRating) flatExtraRatings.get(1);
			substandardExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
			if(substandardRating != null && substandardExtension != null) {
				if(NbaUtils.isPermFlatExtra(substandardRating)) {
					effDate = substandardExtension.getEffDate();
				}else if(NbaUtils.isTempFlatExtra(substandardRating)){
					effDate = substandardExtension.getEffDate();
				}	
			}
			if (effDate != null) {
				aNbaOinkRequest.addValue(effDate);
			} else {
				aNbaOinkRequest.addUnknownValue(new Date());
			}
		}
	}

	/**
	 * Obtain the number of years for a 2nd Flat rating. If a Temporary Flat Rating and a Permanent Flat Rating are present, use the Permanent Flat
	 * Rating. Otherwise there is no value. The Temporary Flat rating, return Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.Duration
	 * For a Permanent Flat rating, calculated as the difference in years between Holding.Policy.Life.Coverage.TermDate and
	 * Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.EffDate
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA104 New Method
	//ALII2009 Method refactored
	public void retrieve2ndFlatExtraYears(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		SubstandardRating substandardRating;
		SubstandardRatingExtension substandardExtension;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {//AXAL3.7.14
			int extraYears = -1;
			List flatExtraRatings = NbaUtils.getFlatExtraRatings(coverage, participants);
			substandardRating = (SubstandardRating) flatExtraRatings.get(1);
			substandardExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
			if(substandardRating != null && substandardExtension != null) {
				if(NbaUtils.isPermFlatExtra(substandardRating)) {
					extraYears = NbaUtils.calcYearsDiff(substandardExtension.getEndDate(), substandardExtension.getEffDate());
				}else if(NbaUtils.isTempFlatExtra(substandardRating)){
					extraYears = substandardExtension.getDuration();
				}	
			}
			if(extraYears != -1){
				aNbaOinkRequest.addValue(extraYears);
			}else {
				aNbaOinkRequest.addUnknownValue("");	
			}
		}
	}
	
	/**
	 * Obtain the number of years for a 3rd Flat rating. If a Temporary Flat Rating and a Permanent Flat Rating are present, use the Permanent Flat
	 * Rating. Otherwise there is no value. The Temporary Flat rating, return Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.Duration
	 * For a Permanent Flat rating, calculated as the difference in years between Holding.Policy.Life.Coverage.TermDate and
	 * Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.EffDate
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.14 New Method
	//ALII2009 Method refactored
	public void retrieve3rdFlatExtraAmt(NbaOinkRequest aNbaOinkRequest) {
		Iterator coveragesItr = getCoveragesOrRiders(aNbaOinkRequest).iterator(); //Coverages related to request
		List participantsList = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		SubstandardRating substandardRating;
		while (coveragesItr.hasNext()) {
			Coverage coverage = (Coverage) coveragesItr.next();
			double extraAmt = Double.NaN;
			List flatExtraRatings = NbaUtils.getFlatExtraRatings(coverage, participantsList);
			substandardRating = (SubstandardRating) flatExtraRatings.get(2);
			if(substandardRating != null) {
				if(NbaUtils.isPermFlatExtra(substandardRating)) {
					SubstandardRatingExtension substandardExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
					if(substandardExtension != null) {
						extraAmt = substandardExtension.getPermFlatExtraAmt();	
					}
				}else if(NbaUtils.isTempFlatExtra(substandardRating)){
					extraAmt = substandardRating.getTempFlatExtraAmt();
				}	
			}
			aNbaOinkRequest.addValue(extraAmt);
		}
	}
	
	/**
	 * Obtain the number of years for a 3rd Flat rating. If a Temporary Flat Rating and a Permanent Flat Rating are present, use the Permanent Flat
	 * Rating. Otherwise there is no value. The Temporary Flat rating, return Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.Duration
	 * For a Permanent Flat rating, calculated as the difference in years between Holding.Policy.Life.Coverage.TermDate and
	 * Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.EffDate
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.14 New Method
	//ALII2009 Method refactored
	public void retrieve3rdFlatExtraYears(NbaOinkRequest aNbaOinkRequest) {
		Iterator coveragesItr = getCoveragesOrRiders(aNbaOinkRequest).iterator(); //Coverages related to request
		List participantsList = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		SubstandardRating substandardRating;
		while (coveragesItr.hasNext()) {
			Coverage coverage = (Coverage) coveragesItr.next();
			int extraYears = -1;
			List flatExtraRatings = NbaUtils.getFlatExtraRatings(coverage, participantsList);
			substandardRating = (SubstandardRating) flatExtraRatings.get(1);
			SubstandardRatingExtension substandardExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
			if(substandardRating != null && substandardExtension != null) {
				if(NbaUtils.isPermFlatExtra(substandardRating)) {
					extraYears = NbaUtils.calcYearsDiff(substandardExtension.getEndDate(), substandardExtension.getEffDate());
				}else if(NbaUtils.isTempFlatExtra(substandardRating)){
					extraYears = substandardExtension.getDuration();
				}	
			}
			if(extraYears != -1){
				aNbaOinkRequest.addValue(extraYears);
			}else {
				aNbaOinkRequest.addUnknownValue("");	
			}
		}
	}
	
	/**
	 * Obtain the value for Table Rating Years. If a Temporary Table Rating is not found try to return the value from a Permanent Table Rating.
	 * Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.TempTableRating
	 * Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.PermTableRating
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA104 NBA100 New Method
	public void retrieveTableRatingYears(NbaOinkRequest aNbaOinkRequest) {
		int tempYears = -1;
		int permYears = -1;
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		LifeParticipant lifeParticipant;
		SubstandardRating substandardRating;
		SubstandardRatingExtension ext;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			int lifeIdx = coverage.getLifeParticipantCount();
			for (int i = 0; i < lifeIdx; i++) {
				lifeParticipant = coverage.getLifeParticipantAt(i);
				if (participants.contains(lifeParticipant)) {
					int ratIdx = lifeParticipant.getSubstandardRatingCount();
					for (int j = 0; j < ratIdx; j++) {
						substandardRating = lifeParticipant.getSubstandardRatingAt(j);
						if (NbaUtils.isValidRating(substandardRating)) { //SPR2590
							ext = NbaUtils.getFirstSubstandardExtension(substandardRating);
							if (ext != null && ext.hasEffDate()) {
								String type = NbaUtils.getSubstandardRatingType(substandardRating);
								if (NbaConstants.SUB_STAND_TYPE_TEMP_TABLE.equals(type) && tempYears == -1) {
									if (substandardRating.hasTempTableRatingEndDate()) {
										tempYears = NbaUtils.calcYearsDiff(substandardRating.getTempTableRatingEndDate(), ext.getEffDate());
									}
								} else if (NbaConstants.SUB_STAND_TYPE_PERM_TABLE.equals(type) && permYears == -1) {
									if (ext.hasEndDate() && ext.hasEffDate()) {
										permYears = NbaUtils.calcYearsDiff(ext.getEndDate(), ext.getEffDate());
									}
								}
							}
						} //SPR2590
					}
				}
			}
			if (tempYears != -1) {
				aNbaOinkRequest.addValue(tempYears);
			} else {
				aNbaOinkRequest.addValue(permYears);
			}
		}
	}

	/**
	 * gets all the ReplacementInd values OlifE.Holding.Policy.ApplicationInfo.ReplacementInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP014 New Method
	public void retrieveReplacementInd(NbaOinkRequest aNbaOinkRequest) {
		ArrayList holdingList = getOLifE().getHolding();
		int count = holdingList.size();
		Policy policy = null;
		ApplicationInfo appInfo = null;
		for (int i = 0; i < count; i++) {
			policy = ((Holding) holdingList.get(i)).getPolicy();
			if (policy != null) {
				appInfo = policy.getApplicationInfo();
				if (appInfo != null) {
					aNbaOinkRequest.addValue(appInfo.getReplacementInd());
				}
			}
		}
	}

	
	/**
	 * gets all the AllChildrenInsuredInd values OLifE.Party.Risk.RiskExtension.AllChildrenInsuredInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP014 New Method
	public void retrieveAllChildrenInsuredInd(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk aRisk = getRisk(party);
			if (aRisk != null) {
				int index_extension = getExtensionIndex(aRisk.getOLifEExtension(), RISK_EXTN);
				if (index_extension != -1) {
					RiskExtension extension = aRisk.getOLifEExtensionAt(index_extension).getRiskExtension();
					if (extension != null) {
						aNbaOinkRequest.addValue(extension.getAllChildrenInsuredInd());
					}
				}
			}
		}
	}

	/**
	 * Retrieve the LifeParticipantRoleCode value. OLifE.Holding.Policy.Life.Coverage.LifeParticipant.LifeParticipantRoleCode
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP014 New Method
	public void retrieveLifeParticipantRoleCode(NbaOinkRequest aNbaOinkRequest) {
		// begin SPR2387
		String qualifier = aNbaOinkRequest.getQualifier();
		if (BENEFIT.equals(qualifier)) {
			Life life = getLife();
			int count = life.getCoverageCount();
			for (int i = 0; i < count; i++) {
				Coverage coverage = life.getCoverageAt(i);
				LifeParticipant lifeParticipant = NbaUtils.getInsurableLifeParticipant(coverage);
				int countCO = coverage.getCovOptionCount();
				for (int j = 0; j < countCO; j++) {
					aNbaOinkRequest.addValue(lifeParticipant.getLifeParticipantRoleCode());
				}
			}
		} else {
			List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
			int count = participants.size();
			for (int i = 0; i < count; i++) {
				LifeParticipant lifeParticipant = (LifeParticipant) participants.get(i);
				aNbaOinkRequest.addValue(lifeParticipant.getLifeParticipantRoleCode());
			}
		}
		// end SPR2387
	}

	/**
	 * get the TotalInforce values OLifE.Party.Risk.RiskExtension.TotalInforce
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP014 New Method
	public void retrieveTotalInforce(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		Risk risk = null;
		if (party != null) {
			risk = party.getRisk();
			if (risk != null) {
				int index_extension = getExtensionIndex(risk.getOLifEExtension(), RISK_EXTN);
				if (index_extension != -1) {
					RiskExtension extension = risk.getOLifEExtensionAt(index_extension).getRiskExtension();
					if (extension != null) {
						aNbaOinkRequest.addValue(extension.getTotalInforce(), FORMAT_TYPE_CURRENCY); //ALII1660
					}
				}
			}
		}
	}

	/**
	 * get the TotalPending values OLifE.Party.Risk.RiskExtension.TotalPending
	 * @param aNbaOinkRequest - data request container
	 */
	//SPR2142 New Method
	public void retrieveTotalPending(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		Risk risk = null;
		if (party != null) {
			risk = party.getRisk();
			if (risk != null) {
				int index_extension = getExtensionIndex(risk.getOLifEExtension(), RISK_EXTN);
				if (index_extension != -1) {
					RiskExtension extension = risk.getOLifEExtensionAt(index_extension).getRiskExtension();
					if (extension != null) {
						aNbaOinkRequest.addValue(extension.getTotalPending());
					}
				}
			}
		}
	}

	/**
	 * gets the ReqSubStatus value based on the requirement id filter OLifE.Holding.Policy.RequirementInfo.ReqSubStatus
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP009 New Method
	public void retrieveReqSubStatus(NbaOinkRequest aNbaOinkRequest) {
		String reqIdFilter = aNbaOinkRequest.getRequirementIdFilter();
		Policy policy = getPolicy();
		ArrayList reqInfoList = new ArrayList();
		int listSize = 0;
		RequirementInfo reqInfo = null;
		if (policy != null) {
			reqInfoList = policy.getRequirementInfo();
			listSize = reqInfoList.size();
			for (int i = 0; i < listSize; i++) {
				reqInfo = (RequirementInfo) reqInfoList.get(i);
				if (reqInfo != null && reqInfo.getId().equals(reqIdFilter)) {
					break;
				}
			}
			if (reqInfo != null) {
				aNbaOinkRequest.addValue(reqInfo.getReqSubStatus());//AXAL3.7.38
			}
		}
	}

	/**
	 * Retrieves the relationrolecode for this party with the subject of inquiry. OLifE.Relation.RelationRoleCode
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP006 New Method
	public void retrieveMIBRelationRoleCode(NbaOinkRequest aNbaOinkRequest) {
		int partyIndex = aNbaOinkRequest.getPartyFilter();
		Party party = oLifE.getPartyAt(partyIndex);
		String partyId = "";
		if (party != null) {
			partyId = party.getId();
		}
		ArrayList relationList = new ArrayList();
		Relation relation = null;
		relationList = oLifE.getRelation();
		for (int j = 0; j < relationList.size(); j++) {
			relation = (Relation) relationList.get(j);
			if (relation.getRelatedObjectID().equalsIgnoreCase(partyId)
					&& (relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_HIT || relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_TRY)) {
				aNbaOinkRequest.addValue(relation.getRelationRoleCode());
			}
		}
	}

	/**
	 * Retrieves lastName of one of the reply party in MIB response. Party.Person.LastName
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP006 New Method
	public void retrieveMIBLastName(NbaOinkRequest aNbaOinkRequest) {
		int partyIndex = aNbaOinkRequest.getPartyFilter();
		Party party = oLifE.getPartyAt(partyIndex);
		if (party != null && party.hasPersonOrOrganization() && party.getPersonOrOrganization().isPerson()) {
			aNbaOinkRequest.addValue(party.getPersonOrOrganization().getPerson().getLastName());
		}
	}

	/**
	 * Retrieves firstName of one of the reply party in MIB response. Party.Person.FirstName
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP006 New Method
	public void retrieveMIBFirstName(NbaOinkRequest aNbaOinkRequest) {
		int partyIndex = aNbaOinkRequest.getPartyFilter();
		Party party = oLifE.getPartyAt(partyIndex);
		if (party != null && party.hasPersonOrOrganization() && party.getPersonOrOrganization().isPerson()) {
			aNbaOinkRequest.addValue(party.getPersonOrOrganization().getPerson().getFirstName());
		}
	}

	/**
	 * Retrieves birthdate of one of the reply party in MIB response. Party.Person.FirstName
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP006 New Method
	public void retrieveMIBBirthDate(NbaOinkRequest aNbaOinkRequest) {
		int partyIndex = aNbaOinkRequest.getPartyFilter();
		Party party = oLifE.getPartyAt(partyIndex);
		if (party != null && party.hasPersonOrOrganization() && party.getPersonOrOrganization().isPerson()) {
			aNbaOinkRequest.addValue(party.getPersonOrOrganization().getPerson().getBirthDate());
		}
	}

	/**
	 * Retrieves birthstate of one of the reply party in MIB response. Party.Person.BirthState
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP006 New Method
	public void retrieveMIBBirthState(NbaOinkRequest aNbaOinkRequest) {
		int partyIndex = aNbaOinkRequest.getPartyFilter();
		Party party = oLifE.getPartyAt(partyIndex);
		if (party != null && party.hasPersonOrOrganization() && party.getPersonOrOrganization().isPerson()) {
			aNbaOinkRequest.addValue(party.getPersonOrOrganization().getPerson().getBirthJurisdictionTC());
		}
	}

	/**
	 * Obtain all the value for ReqCode. OLifE.Holding.Policy.RequirementInfo.ReqCode is the code specifying the underwriting requirement..
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrieveReqCategoryList(NbaOinkRequest aNbaOinkRequest) {
		RequirementInfo reqInfo = null;
		Policy policy = getPolicy();
		int count = policy.getRequirementInfoCount();
		for (int i = 0; i < count; i++) {
			reqInfo = policy.getRequirementInfoAt(i);
			if (reqInfo != null) {
				if (reqInfo.hasReqCategory()) {
					aNbaOinkRequest.addValue(reqInfo.getReqCategory());
				} else {
					aNbaOinkRequest.addValue("-1");
				}

			}
		}
	}

	/**
	 * Obtain all the values for ProfileSysCalcDebits. ProfileInfo.ProfileSysCalcDebits is the Score returned by profile model.
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrieveProfileSysCalcDebits(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		UnderwritingAnalysis uwAnalysis = null;
		if (party != null) {
			OLifEExtension oli = null;
			int index_extension = getExtensionIndex(party.getOLifEExtension(), PARTY_EXTN);
			if (index_extension != -1) {
				oli = party.getOLifEExtensionAt(index_extension);
			} else {
				aNbaOinkRequest.addValue("0");
			}
			if (oli != null && oli.getPartyExtension() != null && oli.getPartyExtension().getUnderwritingAnalysis() != null) {
				uwAnalysis = oli.getPartyExtension().getUnderwritingAnalysis();
				if (uwAnalysis.hasProfileInfo() && uwAnalysis.getProfileInfo().hasProfileSysCalcDebits()) {
					aNbaOinkRequest.addValue(uwAnalysis.getProfileInfo().getProfileSysCalcDebits());
				} else {
					aNbaOinkRequest.addValue("0");
				}
			} else
				aNbaOinkRequest.addValue("0");
		}
	}

	/**
	 * Obtain all the values for ProfileSysCalcCredits ProfileInfo.ProfileSysCalcCredits the Score returned by profile model.
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrieveProfileSysCalcCredits(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		UnderwritingAnalysis uwAnalysis = null;
		if (party != null) {
			OLifEExtension oli = null;
			int index_extension = getExtensionIndex(party.getOLifEExtension(), PARTY_EXTN);
			if (index_extension != -1) {
				oli = party.getOLifEExtensionAt(index_extension);
			} else {
				aNbaOinkRequest.addValue("0");
			}
			if (oli != null && oli.getPartyExtension() != null && oli.getPartyExtension().getUnderwritingAnalysis() != null) {
				uwAnalysis = oli.getPartyExtension().getUnderwritingAnalysis();
				if (uwAnalysis.hasProfileInfo() && uwAnalysis.getProfileInfo().hasProfileSysCalcCredits()) {
					aNbaOinkRequest.addValue(uwAnalysis.getProfileInfo().getProfileSysCalcCredits());
				} else {
					aNbaOinkRequest.addValue("0");
				}
			} else
				aNbaOinkRequest.addValue("0");
		}
	}

	/**
	 * Obtain value for PrfSet PreferredInfo.PrfSet is the code returned by preferred model.
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrievePrfSet(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		UnderwritingAnalysis uwAnalysis = null;
		if (party != null) {
			OLifEExtension oli = null;
			int index_extension = getExtensionIndex(party.getOLifEExtension(), PARTY_EXTN);
			if (index_extension != -1) {
				oli = party.getOLifEExtensionAt(index_extension);
			}
			if (oli != null && oli.getPartyExtension() != null && oli.getPartyExtension().getUnderwritingAnalysis() != null) {
				uwAnalysis = oli.getPartyExtension().getUnderwritingAnalysis();
				if (uwAnalysis.hasProfileInfo() && uwAnalysis.getPreferredInfo().hasPrfSet()) {
					aNbaOinkRequest.addValue(uwAnalysis.getPreferredInfo().getPrfSet());
				} else {
					aNbaOinkRequest.addValue("0");
				}
			}
		}
	}

	/**
	 * Obtain all the values for ImpairmentTempFlatExtraAmt. OLifE.Party.Person.ImpairmentInfo.ImpairmentTempFlatExtraAmt
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrieveImpairmentTempFlatExtraAmtList(NbaOinkRequest aNbaOinkRequest) {
		// SPR3290 code deleted
		Person person = null;
		int requestCount = aNbaOinkRequest.getCount(); // SPR3290
		for (int i = 0; i < requestCount; i++) { // SPR3290
			person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(person.getOLifEExtension(), PERSON_EXTN);
				if (index_extension != -1) {
					oli = person.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					List impInfoList = oli.getPersonExtension().getImpairmentInfo(); // SPR3290
					if (impInfoList != null && impInfoList.size() > 0) {
						for (int impCount = 0; impCount < impInfoList.size(); impCount++) {
							aNbaOinkRequest.addValue(((ImpairmentInfo) impInfoList.get(impCount)).getImpairmentTempFlatExtraAmt());
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain all the values for ImpairmentPermFlatExtraAmt. OLifE.Party.Person.ImpairmentInfo.ImpairmentPermFlatExtraAmt
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrieveImpairmentPermFlatExtraList(NbaOinkRequest aNbaOinkRequest) {
		// SPR3290 code deleted
		Person person = null;
		int requestCount = aNbaOinkRequest.getCount(); // SPR3290
		for (int i = 0; i < requestCount; i++) { // SPR3290
			person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(person.getOLifEExtension(), PERSON_EXTN);
				if (index_extension != -1) {
					oli = person.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					List impInfoList = oli.getPersonExtension().getImpairmentInfo(); // SPR3290
					if (impInfoList != null && impInfoList.size() > 0) {
						for (int impCount = 0; impCount < impInfoList.size(); impCount++) {
							aNbaOinkRequest.addValue(((ImpairmentInfo) impInfoList.get(impCount)).getImpairmentPermFlatExtraAmt());
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain all the values for ImpairmentStatus. OLifE.Party.Person.ImpairmentInfo.ImpairmentPermFlatExtraAmt This code specifies what is the status
	 * of the Impairment.
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrieveImpairmentStatusList(NbaOinkRequest aNbaOinkRequest) {
		// SPR3290 code deleted
		Person person = null;
		int requestCount = aNbaOinkRequest.getCount(); // SPR3290
		for (int i = 0; i < requestCount; i++) { // SPR3290
			person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(person.getOLifEExtension(), PERSON_EXTN);
				if (index_extension != -1) {
					oli = person.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					List impInfoList = oli.getPersonExtension().getImpairmentInfo(); // SPR3290
					if (impInfoList != null && impInfoList.size() > 0) {
						for (int impCount = 0; impCount < impInfoList.size(); impCount++) {
							if (((ImpairmentInfo) impInfoList.get(impCount)).hasImpairmentStatus()) {
								aNbaOinkRequest.addValue(((ImpairmentInfo) impInfoList.get(impCount)).getImpairmentStatus());
							} else {
								break;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain all the values for ImpairmentType. OLifE.Party.Person.ImpairmentInfo.ImpairmentType This code specifies what is the Type of the
	 * Impairment.
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrieveImpairmentTypeList(NbaOinkRequest aNbaOinkRequest) {
		// SPR3290 code deleted
		Person person = null;
		int requestCount = aNbaOinkRequest.getCount(); // SPR3290
		for (int i = 0; i < requestCount; i++) { // SPR3290
			person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(person.getOLifEExtension(), PERSON_EXTN);
				if (index_extension != -1) {
					oli = person.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					List impInfoList = oli.getPersonExtension().getImpairmentInfo(); // SPR3290
					if (impInfoList != null && impInfoList.size() > 0) {
						for (int impCount = 0; impCount < impInfoList.size(); impCount++) {
							aNbaOinkRequest.addValue(((ImpairmentInfo) impInfoList.get(impCount)).getImpairmentType());
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain all the values for ImpairmentClass. OLifE.Party.Person.ImpairmentInfo.ImpairmentClass This code specifies what is the class of the
	 * Impairment.
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrieveImpairmentClassList(NbaOinkRequest aNbaOinkRequest) {
		// SPR3290 code deleted
		Person person = null;
		int requestCount = aNbaOinkRequest.getCount(); // SPR3290
		for (int i = 0; i < requestCount; i++) { // SPR3290
			person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(person.getOLifEExtension(), PERSON_EXTN);
				if (index_extension != -1) {
					oli = person.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					List impInfoList = oli.getPersonExtension().getImpairmentInfo(); // SPR3290
					if (impInfoList != null && impInfoList.size() > 0) {
						for (int impCount = 0; impCount < impInfoList.size(); impCount++) {
							aNbaOinkRequest.addValue(((ImpairmentInfo) impInfoList.get(impCount)).getImpairmentClass());
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain all the values for Debit, OLifE.Party.PersonOrOrganization.Person.PersonExtension.Debit *
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrieveDebitList(NbaOinkRequest aNbaOinkRequest) {
		// SPR3290 code deleted
		Person person = null;
		int requestCount = aNbaOinkRequest.getCount(); // SPR3290
		for (int i = 0; i < requestCount; i++) {// SPR3290
			person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(person.getOLifEExtension(), PERSON_EXTN);
				if (index_extension != -1) {
					oli = person.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					List impInfoList = oli.getPersonExtension().getImpairmentInfo(); // SPR3290
					if (impInfoList != null && impInfoList.size() > 0) {
						for (int impCount = 0; impCount < impInfoList.size(); impCount++) {
							aNbaOinkRequest.addValue(((ImpairmentInfo) impInfoList.get(impCount)).getDebit());
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain all the values for Credit, OLifE.Party.PersonOrOrganization.Person.PersonExtension.Debit *
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrieveCreditList(NbaOinkRequest aNbaOinkRequest) {
		// SPR3290 code deleted
		Person person = null;
		int requestCount = aNbaOinkRequest.getCount(); // SPR3290
		for (int i = 0; i < requestCount; i++) { // SPR3290
			person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(person.getOLifEExtension(), PERSON_EXTN);
				if (index_extension != -1) {
					oli = person.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					List impInfoList = oli.getPersonExtension().getImpairmentInfo(); // SPR3290
					if (impInfoList != null && impInfoList.size() > 0) {
						for (int impCount = 0; impCount < impInfoList.size(); impCount++) {
							aNbaOinkRequest.addValue(((ImpairmentInfo) impInfoList.get(impCount)).getCredit());
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain the value for AgentLicNum, an instance of CompanyProducerID with a carrierApptTypeCode of 1 (Agent).
	 * OLifE().Party().Producer().CarrierAppointment().CompanyProducerID() is the Producer identification number as issued by an insurance company.
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrieveAgentLicNumList(NbaOinkRequest aNbaOinkRequest) {
		CarrierAppointment carrierAppointment = null;
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Producer producer = party.getProducer();
			if (producer != null) {
				int sizeCarrierAppointment = producer.getCarrierAppointmentCount();
				for (int i = 0; i < sizeCarrierAppointment; i++) {
					carrierAppointment = producer.getCarrierAppointmentAt(i);
					if (carrierAppointment != null) {
						aNbaOinkRequest.addValue(carrierAppointment.getCompanyProducerID());
					} else {
						break;
					}
				}
			}
		}
	}

	/**
	 * Obtain all the values for IndicatorCode. OLifE.Holding.Policy.Life.Coverage.IndicatorCode is the code Coverage classication - e.g. base, rider,
	 * etc.
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrieveIndicatorCodeList(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = null;
		// SPR3290 code deleted
		Life life = null;
		life = getLife();
		if (life != null) {
			int countCoverage = life.getCoverageCount();
			for (int i = 0; i < countCoverage; i++) {
				coverage = life.getCoverageAt(i);
				if (coverage != null && coverage.hasIndicatorCode()) {
					aNbaOinkRequest.addValue(coverage.getIndicatorCode());
				} else {
					break;
				}
			}
		}
	}

	/**
	 * Obtain all the values for ProductCode OLifE.Holding.Policy.Life.Coverage.covOption.ProductCode is the code ProductCode.
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrieveProductCodeList(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = null;
		// SPR3290 code deleted
		Life life = null;
		life = getLife();
		if (life != null) {
			for (int i = 0; i < life.getCoverageCount(); i++) {
				coverage = life.getCoverageAt(i);
				if (coverage != null) {
					if (coverage != null && coverage.hasProductCode()) {
						aNbaOinkRequest.addValue(coverage.getProductCode());
					}
				}
			}
		}
	}

	/**
	 * Obtain all the values for ProductCode OLifE.Holding.Policy.Life.Coverage.covOption.LifeCovOptTypeCode is the code specifying ProductCode.
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrieveLifeCovOptTypeCodeList(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = null;
		List covOptionList = null;
		CovOption covOption = null;
		// SPR3290 code deleted
		Life life = null;
		life = getLife();
		if (life != null) {
			for (int i = 0; i < life.getCoverageCount(); i++) {
				coverage = coverage = life.getCoverageAt(i);
				if (coverage != null) {

					if (coverage.hasLifeCovTypeCode()) {
						aNbaOinkRequest.addValue(coverage.getLifeCovTypeCode());
					}
					covOptionList = coverage.getCovOption();
					for (int j = 0; j < covOptionList.size(); j++) {
						covOption = (CovOption) covOptionList.get(j);
						if (covOption.hasLifeCovOptTypeCode()) {
							aNbaOinkRequest.addValue(covOption.getLifeCovOptTypeCode());
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain all the values for ProductCode OLifE.Holding.Policy.Life.Coverage.covOption.OverrideRatingReason is the code specifying ProductCode.
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrieveOverrideRatingReasonList(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = null;
		List covOptionList = null;
		CovOption covOption = null;
		OLifEExtension oli = null;
		// SPR3290 code deleted
		Life life = null;
		life = getLife();
		if (life != null) {
			for (int i = 0; i < life.getCoverageCount(); i++) {
				coverage = coverage = life.getCoverageAt(i);
				if (coverage != null) {
					int index_extension = 0;
					index_extension = getExtensionIndex(coverage.getOLifEExtension(), COVERAGE_EXTN);
					if (index_extension != -1) {
						oli = coverage.getOLifEExtensionAt(index_extension);
						if (oli != null && oli.getCoverageExtension().hasOverrideRatingReason()) {
							aNbaOinkRequest.addValue(oli.getCoverageExtension().getOverrideRatingReason());
						} else {
							aNbaOinkRequest.addValue("");
						}
					}
					covOptionList = coverage.getCovOption();
					for (int j = 0; j < covOptionList.size(); j++) {
						covOption = (CovOption) (covOptionList.get(j));
						index_extension = getExtensionIndex(coverage.getOLifEExtension(), COV_OPTION_EXTN);
						if (index_extension != -1) {
							oli = covOption.getOLifEExtensionAt(index_extension);
							if (oli != null && oli.getCovOptionExtension().hasOverrideRatingReason()) {
								aNbaOinkRequest.addValue(oli.getCovOptionExtension().getOverrideRatingReason());
							} else {
								aNbaOinkRequest.addValue("");
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain the value for PrfApprovedLevel from OLifE.Party.UnderwritingAnalysis.PreferredInfo.PrfApprovedLevel
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrievePrfApprovedLevel(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		UnderwritingAnalysis uwAnalysis = null;
		if (party != null) {
			OLifEExtension oli = null;
			int index_extension = getExtensionIndex(party.getOLifEExtension(), PARTY_EXTN);
			if (index_extension != -1) {
				oli = party.getOLifEExtensionAt(index_extension);
			}
			if (oli != null && oli.getPartyExtension() != null && oli.getPartyExtension().getUnderwritingAnalysis() != null) {
				uwAnalysis = oli.getPartyExtension().getUnderwritingAnalysis();
				if (uwAnalysis.hasPreferredInfo()) {
					aNbaOinkRequest.addValue(uwAnalysis.getPreferredInfo().getPrfApprovedLevel());
				} else {
					aNbaOinkRequest.addValue("");
				}
			}
		}
	}

	/**
	 * Obtain all the values for Duration. OLifE.Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.Duration
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrieveDurationList(NbaOinkRequest aNbaOinkRequest) {
		// SPR3290 code deleted
		Person person = null;
		int requestCount = aNbaOinkRequest.getCount(); // SPR3290
		for (int i = 0; i < requestCount; i++) {
			person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(person.getOLifEExtension(), PERSON_EXTN);
				if (index_extension != -1) {
					oli = person.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					List impInfoList = oli.getPersonExtension().getImpairmentInfo(); // SPR3290
					if (impInfoList != null && impInfoList.size() > 0) {
						for (int impCount = 0; impCount < impInfoList.size(); impCount++) {
							if (((ImpairmentInfo) impInfoList.get(impCount)).hasImpairmentStatus()) {
								aNbaOinkRequest.addValue(((ImpairmentInfo) impInfoList.get(impCount)).getImpairmentDuration());
							} else {
								break;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain all the values for CovOption.ProductCode OLifE.Holding.Policy.Life.Coverage.CovOption.ProductCode
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrieveCovOptionProductCodeList(NbaOinkRequest aNbaOinkRequest) {
		String coverageFilter = aNbaOinkRequest.getCoverageIdFilter();

		Coverage coverage = null;
		List covOptionList = null;
		CovOption covOption = null;
		// SPR3290 code deleted
		Life life = null;
		life = getLife();
		if (life != null) {
			for (int i = 0; i < life.getCoverageCount(); i++) {
				coverage = life.getCoverageAt(i);
				if (coverage != null && coverage.getId().equals(coverageFilter)) {
					covOptionList = coverage.getCovOption();
					for (int j = 0; j < covOptionList.size(); j++) {
						covOption = (CovOption) covOptionList.get(j);
						if (covOption.hasProductCode()) {
							aNbaOinkRequest.addValue(covOption.getProductCode());
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain all the values for CovOption.LifeCovOptTypeCode OLifE.Holding.Policy.Life.Coverage.CovOption.LifeCovOptTypeCode
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrieveCovOptionLifeCovOptTypeCodeList(NbaOinkRequest aNbaOinkRequest) {
		String coverageFilter = aNbaOinkRequest.getCoverageIdFilter();
		Coverage coverage = null;
		List covOptionList = null;
		CovOption covOption = null;
		// SPR3290 code deleted
		Life life = null;
		life = getLife();
		if (life != null) {
			for (int i = 0; i < life.getCoverageCount(); i++) {
				coverage = life.getCoverageAt(i);
				if (coverage != null && coverage.getId().equals(coverageFilter)) {
					covOptionList = coverage.getCovOption();
					for (int j = 0; j < covOptionList.size(); j++) {
						covOption = (CovOption) covOptionList.get(j);
						if (covOption.hasLifeCovOptTypeCode()) {
							aNbaOinkRequest.addValue(covOption.getLifeCovOptTypeCode());
						} else {
							aNbaOinkRequest.addValue(-1);
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain the value for the MAP Target End Date. OLifE.Holding.Policy.Life.MapTargetEndDate
	 * @param aNbaOinkRequest
	 */
	public void retrieveMapTargetEndDate(NbaOinkRequest aNbaOinkRequest) {
		Date mapDate = null;
		Life life = getLife();
		if (life != null) {
			LifeExtension lifeExtension = NbaUtils.getFirstLifeExtension(life);
			if (lifeExtension != null) {
				mapDate = lifeExtension.getMapTargetEndDate();
			}
		}
		if (mapDate != null) {
			aNbaOinkRequest.addValue(mapDate);
		} else {
			aNbaOinkRequest.addUnknownValue(new Date());
		}
	}

	/**
	 * Obtain the value for the MAP Target Amount. OLifE.Holding.Policy.Life.MapTargetEndDate
	 * @param aNbaOinkRequest
	 */
	public void retrieveMinPremAmt(NbaOinkRequest aNbaOinkRequest) {
		Life life = getLife();
		if (life != null) {
			aNbaOinkRequest.addValue(life.getMinPremAmt());
		} else {
			aNbaOinkRequest.addUnknownValue("");
		}
	}

	/**
	 * Obtain the value for ActivityCountLastYear from OLifE.Party.Risk.LifeStyleActivity.ActivityCountLastYear
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveActivityCountLastYear(NbaOinkRequest aNbaOinkRequest) {
		LifeStyleActivity lsActivity = getLifeStyleActivity(aNbaOinkRequest);
		if (lsActivity != null) {
			aNbaOinkRequest.addValue(lsActivity.getActivityCountLastYear());
		}
	}

	/**
	 * Obtain the value for ActivityCountNextYear from OLifE.Party.Risk.LifeStyleActivity.ActivityCountNextYear
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveActivityCountNextYear(NbaOinkRequest aNbaOinkRequest) {
		LifeStyleActivity lsActivity = getLifeStyleActivity(aNbaOinkRequest);
		if (lsActivity != null) {
			aNbaOinkRequest.addValue(lsActivity.getActivityCountNextYear());
		}
	}

	/**
	 * Obtain the value for ActivityCountTotal from OlifE.Party.Risk.LifeStyleActivity.ActivityCountTotal
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveActivityCountTotal(NbaOinkRequest aNbaOinkRequest) {
		LifeStyleActivity lsActivity = getLifeStyleActivity(aNbaOinkRequest);
		if (lsActivity != null) {
			aNbaOinkRequest.addValue(lsActivity.getActivityCountTotal());
		}
	}

	/**
	 * Obtain the value for AirCraftType from OLifE.Party.Risk.LifeStyleActivity.AviationExp.AirCraftType
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveAirCraftType(NbaOinkRequest aNbaOinkRequest) {
		AviationExp aviationExp = getAviationExp(aNbaOinkRequest);
		if (aviationExp != null) {
			aNbaOinkRequest.addValue(aviationExp.getAircraftType());
		}
	}

	/**
	 * Get the AverageHoursPerYear from OLifE.Party.Risk.LifeStyleActivity.LifeStyleActivityExtension.AverageHoursPerYear
	 * @param aNbaOinkRequest
	 */
	//ACP016 New Method
	public void retrieveAverageHoursPerYear(NbaOinkRequest aNbaOinkRequest) {
		LifeStyleActivity lsActivity = getLifeStyleActivity(aNbaOinkRequest);
		if (lsActivity != null) {
			LifeStyleActivityExtension lsActivityExt = NbaUtils.getFirstLifeStyleActivityExtension(lsActivity);
			aNbaOinkRequest.addValue(lsActivityExt.getAverageHoursPerYear());
		}

	}

	/**
	 * Obtain the value for MonthsOfExperience from OLifE.Party.Risk.LifeStyleActivity.AirSportsExp.AirSportsExpExtension.MonthsOfExperience
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveMonthsOfExperience(NbaOinkRequest aNbaOinkRequest) {
		AirSportsExp airSportsExp = getAirSportsExp(aNbaOinkRequest);
		if (airSportsExp != null) {
			AirSportsExpExtension airSportsExpExt = NbaUtils.getFirstAirSportsExpExtension(airSportsExp);
			aNbaOinkRequest.addValue(airSportsExpExt.getMonthsOfExperience());
		}
	}

	/**
	 * Get the ActivityCountPrevOneToTwoYears from OLifE.Party.Risk.LifeStyleActivity.LifeStyleActivityExtension.ActivityCountPrevOneToTwoYears
	 * @param aNbaOinkRequest
	 */
	//ACP016 New Method
	public void retrieveActivityCountPrevOneToTwoYears(NbaOinkRequest aNbaOinkRequest) {
		LifeStyleActivity lsActivity = getLifeStyleActivity(aNbaOinkRequest);
		if (lsActivity != null) {
			LifeStyleActivityExtension lsActivityExt = NbaUtils.getFirstLifeStyleActivityExtension(lsActivity);
			aNbaOinkRequest.addValue(lsActivityExt.getActivityCountPrevOneToTwoYears());
		}

	}

	/**
	 * Obtain the value for AviationType from OLifE.Party.Risk.LifeStyleActivity.AviationExp.AviationType
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveAviationType(NbaOinkRequest aNbaOinkRequest) {
		AviationExp aviationExp = getAviationExp(aNbaOinkRequest);
		if (aviationExp != null) {
			aNbaOinkRequest.addValue(aviationExp.getAviationType());
		}
	}

	/**
	 * Obtain the value for CoPilotInd from OLifE.Party.Risk.LifeStyleActivity.AviationExp.AviationExpExtension.AviationType
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveCoPilotInd(NbaOinkRequest aNbaOinkRequest) {
		AviationExp aviationExp = getAviationExp(aNbaOinkRequest);
		if (aviationExp != null) {
			AviationExpExtension aviationExpExt = NbaUtils.getFirstAviationExpExtension(aviationExp);
			aNbaOinkRequest.addValue(aviationExpExt.getCoPilotInd());
		}
	}

	/**
	 * Obtain the value for FAAViolationCount from OLifE.Party.Risk.LifeStyleActivity.AviationExp.AviationExpExtension.FAAViolationCount
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveFAAViolationCount(NbaOinkRequest aNbaOinkRequest) {
		AviationExp aviationExp = getAviationExp(aNbaOinkRequest);
		if (aviationExp != null) {
			AviationExpExtension aviationExpExt = NbaUtils.getFirstAviationExpExtension(aviationExp);
			aNbaOinkRequest.addValue(aviationExpExt.getFAAViolationCount());
		}
	}

	/**
	 * Obtain the value for FAAViolationType from OLifE.Party.Risk.LifeStyleActivity.AviationExp.AviationExpExtension.FAAViolationType
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveFAAViolationType(NbaOinkRequest aNbaOinkRequest) {
		AviationExp aviationExp = getAviationExp(aNbaOinkRequest);
		if (aviationExp != null) {
			AviationExpExtension aviationExpExt = NbaUtils.getFirstAviationExpExtension(aviationExp);
			aNbaOinkRequest.addValue(aviationExpExt.getFAAViolationType());
		}
	}

	/**
	 * Obtain the value for FlyingPurpose from OLifE.Party.Risk.LifeStyleActivity.AviationExp.FlyingPurpose
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveFlyingPurpose(NbaOinkRequest aNbaOinkRequest) {
		AviationExp aviationExp = getAviationExp(aNbaOinkRequest);
		if (aviationExp != null) {
			aNbaOinkRequest.addValue(aviationExp.getFlyingPurpose());
		}
	}

	/**
	 * Obtain the value for CompeteInd from OLifE.Party.Risk.LifeStyleActivity.AirSportsExp.CompeteInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveCompeteInd(NbaOinkRequest aNbaOinkRequest) {
		AirSportsExp airSportsExp = getAirSportsExp(aNbaOinkRequest);
		if (airSportsExp != null) {
			aNbaOinkRequest.addValue(airSportsExp.getCompeteInd());
		}
	}

	/**
	 * Obtain the value for HazardousAreasInd from OLifE.Party.Risk.LifeStyleActivity.AirSportsExp.AirSportsExpExtension.HazardousAreasInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveHazardousAreasInd(NbaOinkRequest aNbaOinkRequest) {
		AirSportsExp airSportsExp = getAirSportsExp(aNbaOinkRequest);
		if (airSportsExp != null) {
			AirSportsExpExtension airSportsExpExt = NbaUtils.getFirstAirSportsExpExtension(airSportsExp);
			aNbaOinkRequest.addValue(airSportsExpExt.getHazardousAreasInd());
		}
	}

	/**
	 * Obtain the value for RecordAttempts from OLifE.Party.Risk.LifeStyleActivity.AirSportsExp.AirSportsExpExtension.RecordAttempts
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveRecordAttempts(NbaOinkRequest aNbaOinkRequest) {
		AirSportsExp airSportsExp = getAirSportsExp(aNbaOinkRequest);
		if (airSportsExp != null) {
			AirSportsExpExtension airSportsExpExt = NbaUtils.getFirstAirSportsExpExtension(airSportsExp);
			aNbaOinkRequest.addValue(airSportsExpExt.getRecordAttempts());
		}
	}

	/**
	 * Obtain the value for IFRInd from OLifE.Party.Risk.LifeStyleActivity.AviationExp.IFRInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveIFRInd(NbaOinkRequest aNbaOinkRequest) {
		AviationExp aviationExp = getAviationExp(aNbaOinkRequest);
		if (aviationExp != null) {
			aNbaOinkRequest.addValue(aviationExp.getIFRInd());
		}
	}

	/**
	 * Obtain the value for IFRHours from OLifE.Party.Risk.LifeStyleActivity.AviationExp.AviationExpExtension.IFRHours
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveIFRHours(NbaOinkRequest aNbaOinkRequest) {
		AviationExp aviationExp = getAviationExp(aNbaOinkRequest);
		if (aviationExp != null) {
			AviationExpExtension aviationExpExt = NbaUtils.getFirstAviationExpExtension(aviationExp);
			if (aviationExpExt != null) {
				aNbaOinkRequest.addValue(aviationExpExt.getIFRHours());
			}
		}
	}

	/**
	 * Obtain the value for HIghestQualificationLevel from OLifE.Party.Risk.LifeStyleActivity.AviationExp.HighestQualificationLevel
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveHighestQualificationLevel(NbaOinkRequest aNbaOinkRequest) {
		AviationExp aviationExp = getAviationExp(aNbaOinkRequest);
		if (aviationExp != null) {
			aNbaOinkRequest.addValue(aviationExp.getHighestQualificationLevel());
		}
	}

	/**
	 * Obtain the value for TetheredInd from OLifE.Party.Risk.LifeStyleActivity.AirSportsExp.BallooningExp.BallooningExpExtension.TetheredInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveTetheredInd(NbaOinkRequest aNbaOinkRequest) {
		AirSportsExp airSportsExp = getAirSportsExp(aNbaOinkRequest);
		if (airSportsExp != null && airSportsExp.hasBallooningExp()) {
			BallooningExp ballooningExp = airSportsExp.getBallooningExp();
			if (ballooningExp != null) {
				BallooningExpExtension ballooningExpExt = NbaUtils.getBallooningExpExtension(ballooningExp);
				if (ballooningExpExt != null) {
					aNbaOinkRequest.addValue(ballooningExpExt.getTetheredInd());
				}
			}
		}
	}

	/**
	 * Obtain the value for NumberHours from OLifE.Party.Risk.LifeStyleActivity.AirSportsExp.BallooningExp.NumberHours
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016 New Method
	public void retrieveNumberHours(NbaOinkRequest aNbaOinkRequest) {
		AirSportsExp airSportsExp = getAirSportsExp(aNbaOinkRequest);
		if (airSportsExp != null && airSportsExp.hasBallooningExp()) {
			BallooningExp ballooningExp = airSportsExp.getBallooningExp();
			if (ballooningExp != null) {
				aNbaOinkRequest.addValue(ballooningExp.getNumberHours());
			}
		}
	}

	/**
	 * Obtain the value of HazardousActivityInd from OLifE.Party.Risk.RiskExtension.HazardousActivityInd
	 * @param aNbaOinkRequest
	 */
	//ACP016
	public void retrieveHazardousActivityInd(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		Risk risk = getRisk(party);
		if (risk != null) {
			RiskExtension riskExt = NbaUtils.getFirstRiskExtension(risk);
			if (riskExt != null) {
				aNbaOinkRequest.addValue(riskExt.getHazardousActivityInd());
			}
		}
	}

	/**
	 * Obtain the value of LifeStyleActivityInd from OLifE.Party.Risk.RiskExtension.LifeStyleActivityInd
	 * @param aNbaOinkRequest
	 */
	// SPR2396 New Method
	public void retrieveLifeStyleActivityInd(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		Risk risk = getRisk(party);
		if (risk != null) {
			RiskExtension riskExt = NbaUtils.getFirstRiskExtension(risk);
			if (riskExt != null) {
				aNbaOinkRequest.addValue(riskExt.getLifeStyleActivityInd());
			}
		}
	}

	/**
	 * Obtain the value of JobChangeCount from OLifE.Party.Risk.RiskExtension.JobChangeCount
	 * @param aNbaOinkRequest
	 */
	//ACP016
	public void retrieveJobChangeCount(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		Risk risk = getRisk(party);
		if (risk != null) {
			RiskExtension riskExt = NbaUtils.getFirstRiskExtension(risk);
			if (riskExt != null) {
				aNbaOinkRequest.addValue(riskExt.getJobChangeCount());
			}
		}
	}

	/**
	 * Obtain the value for FutureAviationInd from OLifE.Party.Risk.LifeStyleActivity.AviationExp.AviationExpExtension.FutureAviationInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP016
	public void retrieveFutureAviationInd(NbaOinkRequest aNbaOinkRequest) {
		AviationExp aviationExp = getAviationExp(aNbaOinkRequest);
		if (aviationExp != null) {
			AviationExpExtension aviationExpExt = NbaUtils.getFirstAviationExpExtension(aviationExp);
			if (aviationExpExt != null) {
				aNbaOinkRequest.addValue(aviationExpExt.getFutureAviationInd());
			}
		}
	}

	/**
	 * Obtain the value of MovingViolationInd from OLifE.Party.Risk.RiskExtension.MovingViolationInd
	 * @param aNbaOinkRequest
	 */
	//ACP016
	public void retrieveMovingViolationInd(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		Risk risk = getRisk(party);
		if (risk != null) {
			RiskExtension riskExt = NbaUtils.getFirstRiskExtension(risk);
			if (riskExt != null) {
				aNbaOinkRequest.addValue(riskExt.getMovingViolationInd());
			}
		}
	}

	/**
	 * gets all the HypertensiveTherapyInd values OLifE.Party.Risk.RiskExtension.HypertensiveTherapyInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP015 New Method
	public void retrieveHypertensiveTherapyInd(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk aRisk = getRisk(party);
			if (aRisk != null) {
				int index_extension = getExtensionIndex(aRisk.getOLifEExtension(), RISK_EXTN);
				if (index_extension != -1) {
					RiskExtension extension = aRisk.getOLifEExtensionAt(index_extension).getRiskExtension();
					if (extension != null) {
						aNbaOinkRequest.addValue(extension.getHypertensiveTherapyInd());
					}
				}
			}
		}
	}

	/**
	 * gets all the LipidTherapyInd values OLifE.Party.Risk.RiskExtension.LipidTherapyInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP015 New Method
	public void retrieveLipidTherapyInd(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk aRisk = getRisk(party);
			if (aRisk != null) {
				int index_extension = getExtensionIndex(aRisk.getOLifEExtension(), RISK_EXTN);
				if (index_extension != -1) {
					RiskExtension extension = aRisk.getOLifEExtensionAt(index_extension).getRiskExtension();
					if (extension != null) {
						aNbaOinkRequest.addValue(extension.getLipidTherapyInd());
					}
				}
			}
		}
	}

	/**
	 * gets all the PulseTreatmentInd value OLifE.Party.Risk.RiskExtension.PulseTreatmentInd
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP015 New Method
	public void retrievePulseTreatmentInd(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk aRisk = getRisk(party);
			if (aRisk != null) {
				int index_extension = getExtensionIndex(aRisk.getOLifEExtension(), RISK_EXTN);
				if (index_extension != -1) {
					RiskExtension extension = aRisk.getOLifEExtensionAt(index_extension).getRiskExtension();
					if (extension != null) {
						aNbaOinkRequest.addValue(extension.getPulseTreatmentInd());
					}
				}
			}
		}
	}

	/**
	 * Obtain the value for StressECGValue. Olife.Party.Risk.MedicalExam.StressECGValue
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP015 New Method
	public void retrieveStressECGValue(NbaOinkRequest aNbaOinkRequest) {
		MedicalExam aMedicalExam = null;
		MedicalExamExtension mee = null;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(aMedicalExam.getOLifEExtension(), MEDICAL_EXAM_EXTENSION);
				if (index_extension != -1) {
					oli = aMedicalExam.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					mee = oli.getMedicalExamExtension();
					if (mee != null) {
						aNbaOinkRequest.addValue(mee.getStressECGValue());
					}
				}
			} else {
				break;
			}
		}
	}

	/**
	 * get the ProposedUnderwritingClass value OLifE.Holding.Policy.Life.Coverage.LifeParticipant.lifeparticipantExtension.ProposedUnderwritingClass
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP015 New Method
	public void retrieveProposedUnderwritingClass(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		String partyID = null;
		Life life = null;
		ArrayList coverageList = null;
		ArrayList lifeParticipantList = null;
		Coverage coverage = null;
		LifeParticipant lifeParticipant = null;
		LifeParticipantExtension lifeParticipantExtension = null;
		int coverageCount = 0;
		int lifeParticipantCount = 0;
		if (party != null) {
			partyID = party.getId();
		}
		life = getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
		if (life != null) {
			coverageList = life.getCoverage();
			coverageCount = coverageList.size();
			outer: for (int i = 0; i < coverageCount; i++) {
				coverage = (Coverage) coverageList.get(i);
				if (coverage != null) {
					lifeParticipantList = coverage.getLifeParticipant();
					lifeParticipantCount = lifeParticipantList.size();
					for (int j = 0; j < lifeParticipantCount; j++) {
						lifeParticipant = (LifeParticipant) lifeParticipantList.get(j);
						if (lifeParticipant != null && lifeParticipant.getPartyID().equals(partyID)) {
							lifeParticipantExtension = NbaUtils.getFirstLifeParticipantExtension(lifeParticipant);
							if (lifeParticipantExtension != null) {
								aNbaOinkRequest.addValue(lifeParticipantExtension.getProposedUnderwritingClass());
								break outer;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain the value for PrfCalculatedLevel from OLifE.Party.UnderwritingAnalysis.PreferredInfo.PrfCalculatedLevel
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP008 New Method
	public void retrievePrfCalculatedLevel(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		UnderwritingAnalysis uwAnalysis = null;
		if (party != null) {
			OLifEExtension oli = null;
			int index_extension = getExtensionIndex(party.getOLifEExtension(), PARTY_EXTN);
			if (index_extension != -1) {
				oli = party.getOLifEExtensionAt(index_extension);
			}
			if (oli != null && oli.getPartyExtension() != null && oli.getPartyExtension().getUnderwritingAnalysis() != null) {
				uwAnalysis = oli.getPartyExtension().getUnderwritingAnalysis();
				if (uwAnalysis != null && uwAnalysis.hasPreferredInfo()) {
					aNbaOinkRequest.addValue(uwAnalysis.getPreferredInfo().getPrfCalculatedLevel());
				} else {
					aNbaOinkRequest.addValue("-1");
				}
			}
		}
	}

	/**
	 * Obtain the value for PrfUWManualValue from OLifE.Party.UnderwritingAnalysis.PreferredInfo.PrfUWManualValue
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP008 New Method
	public void retrievePrfUWManualValue(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		UnderwritingAnalysis uwAnalysis = null;
		if (party != null) {
			OLifEExtension oli = null;
			int index_extension = getExtensionIndex(party.getOLifEExtension(), PARTY_EXTN);
			if (index_extension != -1) {
				oli = party.getOLifEExtensionAt(index_extension);
			}
			if (oli != null && oli.getPartyExtension() != null && oli.getPartyExtension().getUnderwritingAnalysis() != null) {
				uwAnalysis = oli.getPartyExtension().getUnderwritingAnalysis();
				if (uwAnalysis != null && uwAnalysis.hasPreferredInfo()) {
					aNbaOinkRequest.addValue(uwAnalysis.getPreferredInfo().getPrfUWManualValue());
				} else {
					aNbaOinkRequest.addValue("-1");
				}
			}
		}
	}

	/**
	 * Obtain the value for PrfUWMaxPrefLevel from OLifE.Party.UnderwritingAnalysis.PreferredInfo.PrfUWMaxPrefLevel
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP008 New Method
	public void retrievePrfUWMaxPrefLevel(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		UnderwritingAnalysis uwAnalysis = null;
		if (party != null) {
			OLifEExtension oli = null;
			int index_extension = getExtensionIndex(party.getOLifEExtension(), PARTY_EXTN);
			if (index_extension != -1) {
				oli = party.getOLifEExtensionAt(index_extension);
			}
			if (oli != null && oli.getPartyExtension() != null && oli.getPartyExtension().getUnderwritingAnalysis() != null) {
				uwAnalysis = oli.getPartyExtension().getUnderwritingAnalysis();
				if (uwAnalysis != null && uwAnalysis.hasPreferredInfo()) {
					aNbaOinkRequest.addValue(uwAnalysis.getPreferredInfo().getPrfUWMaxPrefLevel());
				} else {
					aNbaOinkRequest.addValue("-1");
				}
			}
		}
	}

	/**
	 * Obtain the value for PrfTobaccoPremiumBasis from OLifE.Party.UnderwritingAnalysis.PreferredInfo.PrfTobaccoPremiumBasis
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP008 New Method
	public void retrievePrfTobaccoPremiumBasis(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		UnderwritingAnalysis uwAnalysis = null;
		if (party != null) {
			OLifEExtension oli = null;
			int index_extension = getExtensionIndex(party.getOLifEExtension(), PARTY_EXTN);
			if (index_extension != -1) {
				oli = party.getOLifEExtensionAt(index_extension);
			}
			if (oli != null && oli.getPartyExtension() != null && oli.getPartyExtension().getUnderwritingAnalysis() != null) {
				uwAnalysis = oli.getPartyExtension().getUnderwritingAnalysis();
				if (uwAnalysis != null && uwAnalysis.hasPreferredInfo()) {
					aNbaOinkRequest.addValue(uwAnalysis.getPreferredInfo().getPrfTobaccoPremiumBasis());
				} else {
					aNbaOinkRequest.addValue("-1");
				}
			}
		}
	}

	/**
	 * Obtain the value for KnownFamilyHistoryInd from OLifE.Party.Risk.KnownFamilyHistoryInd
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP008 New Method
	public void retrieveKnownFamilyHistoryInd(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		Risk risk = null;
		if (party != null) {
			risk = party.getRisk();
			if (risk != null) {
				aNbaOinkRequest.addValue(risk.getKnownFamilyHistoryInd());
			}
		}
	}

	/**
	 * Obtain the value for PrfUFPResponsesScore from
	 * OLifE.Party.OLifEExtension.PartyExtension.UnderwritingAnalysis.PreferredInfo.PrfUFPResponsesScore
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP008 New Method
	public void retrievePrfUFPResponsesScore(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		UnderwritingAnalysis uwAnalysis = null;
		if (party != null) {
			OLifEExtension oli = null;
			int index_extension = getExtensionIndex(party.getOLifEExtension(), PARTY_EXTN);
			if (index_extension != -1) {
				oli = party.getOLifEExtensionAt(index_extension);
			}
			if (oli != null && oli.getPartyExtension() != null && oli.getPartyExtension().getUnderwritingAnalysis() != null) {
				uwAnalysis = oli.getPartyExtension().getUnderwritingAnalysis();
				if (uwAnalysis.hasPreferredInfo()) {
					aNbaOinkRequest.addValue(uwAnalysis.getPreferredInfo().getPrfUFPResponsesScore());
				}
			}
		}
	}

	/**
	 * gets the value for UFPQuestionnaireCode based on the RelatedObjectID OLifE.FormInstance.FormInstanceExtension.UFPQuestionnaireCode is the code
	 * for the UFS Questions.
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP008 New Method
	public void retrieveUFPQuestionnaireCode(NbaOinkRequest aNbaOinkRequest) {
		FormInstance formInstance = getFormInstanceByRelatedObjectType(aNbaOinkRequest);
		FormInstanceExtension formInstanceExtension = null;
		if (formInstance != null) {
			OLifEExtension oli = null;
			int index_extension = getExtensionIndex(formInstance.getOLifEExtension(), FORMINSTANCE_EXTN);
			if (index_extension != -1) {
				oli = formInstance.getOLifEExtensionAt(index_extension);
			}
			if (oli != null) {
				formInstanceExtension = oli.getFormInstanceExtension();
				if (formInstanceExtension != null) {
					aNbaOinkRequest.addValue(formInstanceExtension.getUFPQuestionnaireCode());
				}
			}
		}
	}

	/**
	 * Retrieve the value for ReplacementFormSignatureOK. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.ReplacementFormSignatureOK.
	 * @param aNbaOinkRequest - data request container
	 */
	// ACN007 new method
	public void retrieveReplacementFormSignatureOK(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null) {
					aNbaOinkRequest.addValue(oli.getApplicationInfoExtension().getReplacementFormSignatureOK());
					return;
				}
			}
		}
		aNbaOinkRequest.addValue(false); //SPR3165
	}

	/**
	 * Retrieve the value for PACValidationOK OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.PACValidationOK
	 * @param aNbaOinkRequest - data request container
	 */
	// ACN007 new method
	public void retrievePACValidationOK(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null) {
					aNbaOinkRequest.addValue(oli.getApplicationInfoExtension().getPACValidationOK());
					return;
				}
			}
		}
		aNbaOinkRequest.addValue(false); //SPR3165
	}

	/**
	 * Retrieve the value for CheckSignedOK OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.CheckSignedOK
	 * @param aNbaOinkRequest - data request container
	 */
	// ACN007 new method
	public void retrieveCheckSignedOK(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null) {
					aNbaOinkRequest.addValue(oli.getApplicationInfoExtension().getCheckSignedOK());
					return;
				}
			}
		}
		aNbaOinkRequest.addValue(false); //SPR3165
	}

	/**
	 * Get the value for a BillControlEffDate. OLifE.Holding.Banking.OLifEExtension.BankingExtension.BillControlEffDate is the effective date credit
	 * card charging
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA115 New Method
	public void retrieveBillControlEffDate(NbaOinkRequest aNbaOinkRequest) {
		Banking banking = null;
		List bankingList = getBankingList(aNbaOinkRequest);
		int bankingCount = bankingList.size();
		int requestCount = aNbaOinkRequest.getCount();
		for (int i = 0; i < requestCount; i++) {
			if (i < bankingCount) {
				banking = (Banking) bankingList.get(i);
				BankingExtension bankingExtn = NbaUtils.getFirstBankingExtension(banking);
				if ((bankingExtn != null) && (bankingExtn.hasBillControlEffDate())) {
					aNbaOinkRequest.addValue(bankingExtn.getBillControlEffDate());
					continue;
				}
			}
			aNbaOinkRequest.addUnknownValue(new Date());
		}
	}

	/**
	 * Get the value for a PaymentChargeAmt. OLifE.Holding.Banking.OLifEExtension.BankingExtension.PaymentChargeAmt is the charge amount for credit
	 * card payments
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA115 New Method
	public void retrievePaymentChargeAmt(NbaOinkRequest aNbaOinkRequest) {
		Banking banking = null;
		List bankingList = getBankingList(aNbaOinkRequest);
		int bankingCount = bankingList.size();
		int requestCount = aNbaOinkRequest.getCount();
		for (int i = 0; i < requestCount; i++) {
			if (i < bankingCount) {
				banking = (Banking) bankingList.get(i);
				BankingExtension bankingExtn = NbaUtils.getFirstBankingExtension(banking);
				if ((bankingExtn != null) && (bankingExtn.hasPaymentChargeAmt())) {
					aNbaOinkRequest.addValue(bankingExtn.getPaymentChargeAmt());
					continue;
				}
			}
			aNbaOinkRequest.addValue(0.0);
		}
	}

	/**
	 * Get the value for a PaymentType. OLifE.Holding.Banking.OLifEExtension.BankingExtension.PaymentType is the payment type for credit card payments
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA115 New Method
	public void retrievePaymentType(NbaOinkRequest aNbaOinkRequest) {
		long paymentType;
		Banking banking = null;
		List bankingList = getBankingList(aNbaOinkRequest);
		int bankingCount = bankingList.size();
		int requestCount = aNbaOinkRequest.getCount();
		for (int i = 0; i < requestCount; i++) {
			paymentType = -1L;
			if (i < bankingCount) {
				banking = (Banking) bankingList.get(i);
				BankingExtension bankingExtn = NbaUtils.getFirstBankingExtension(banking);
				if ((bankingExtn != null) && (bankingExtn.hasPaymentType())) {
					paymentType = bankingExtn.getPaymentType();
				}
			}
			aNbaOinkRequest.addValue(paymentType, NbaTableConstants.OLI_LU_FINACTTYPE);
		}
	}

	/**
	 * Retrieve the value for QualityCheckModelResults, which is an Attachement object of type OLI_ATTACH_WRKSHT
	 * OLifE.Holding.Attachment.AttachmentData
	 * @param aNbaOinkRequest - data request container
	 */
	// ACN007 new method
	public void retrieveQualityCheckModelResults(NbaOinkRequest aNbaOinkRequest) {
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE());
		for (int i = 0; i < holding.getAttachmentCount(); i++) {
			if (holding.getAttachmentAt(i).getAttachmentType() == NbaOliConstants.OLI_ATTACH_WRKSHT) {
				String pureResults = holding.getAttachmentAt(i).getAttachmentData().getPCDATA();
				pureResults = pureResults.replace('\"', '\'');
				aNbaOinkRequest.addValue(pureResults);
				return;
			}
		}
	}

	/**
	 * Obtain the value for MedCondResponse OLifE.Party.Risk.MedicalCondition.MedicalConditionExtension.MedCondResponse
	 * @param aNbaOinkRequest - data request container
	 */
	//SPR2532 New Method
	public void retrieveMedCondResponse(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalCondition aMedicalCondition = getMedicalCondition(aNbaOinkRequest, i);
			if (aMedicalCondition != null) {
				MedicalConditionExtension medicalConditionExtension = NbaUtils.getFirstMedicalConditionExtension(aMedicalCondition);
				if (medicalConditionExtension != null && medicalConditionExtension.hasMedCondResponse()) {
					aNbaOinkRequest.addValue(medicalConditionExtension.getMedCondResponse());
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the boolean indicating if there are any severe Contract Validation errors.
	 * @param aNbaOinkRequest
	 */
	// SPR1753 New Method
	public void retrieveSevereValidationErrorInd(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(getNbaTXLife().hasSevereValidationErrors());
	}

	/**
	 * Obtain the value for CardExpMonth, derived from first two characters of OLifE.Holding.Banking.CreditCardExpDate
	 * OLifE.Holding.Banking.CreditCardExpDate is the expiration date of the associated credit card
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA115 New Method
	public void retrieveCardExpMonth(NbaOinkRequest aNbaOinkRequest) {
		long month;
		Banking banking = null;
		List bankingList = getBankingList(aNbaOinkRequest);
		int bankingCount = bankingList.size();
		int requestCount = aNbaOinkRequest.getCount();
		for (int i = 0; i < requestCount; i++) {
			month = -1L;
			if (i < bankingCount) {
				banking = (Banking) bankingList.get(i);
				if (banking.hasCreditCardExpDate() && banking.getCreditCardExpDate().length() >= 2) {
					month = NbaDataObjectRenderer.convertStringToLong(banking.getCreditCardExpDate().substring(0, 2));
				}
			}
			aNbaOinkRequest.addValue(month, NbaTableConstants.NBA_MONTHS);
		}
	}

	/**
	 * Obtain the value for CardExpMonth, derived from last four characters of OLifE.Holding.Banking.CreditCardExpDate
	 * OLifE.Holding.Banking.CreditCardExpDate is the expiration date of the associated credit card
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA115 New Method
	public void retrieveCardExpYear(NbaOinkRequest aNbaOinkRequest) {
		String year = null;
		Banking banking = null;
		List bankingList = getBankingList(aNbaOinkRequest);
		int bankingCount = bankingList.size();
		int requestCount = aNbaOinkRequest.getCount();
		for (int i = 0; i < requestCount; i++) {
			year = "";
			if (i < bankingCount) {
				banking = (Banking) bankingList.get(i);
				if (banking.hasCreditCardExpDate() && banking.getCreditCardExpDate().length() >= 7) {
					year = banking.getCreditCardExpDate().substring(3, 7);
				}
			}
			aNbaOinkRequest.addValue(year, NbaTableConstants.NBA_CARD_EXP_YEARS);
		}
	}

	/**
	 * Obtain the value for CovOptionPctInd Holding.Policy.Life.Coverage.CovOption.CovOptionPctInd
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA142 New Method
	public void retrieveCovOptionPctInd(NbaOinkRequest aNbaOinkRequest) {
		CovOption covOption = null;
		List covOptions = getCovOptions();
		if (covOptions != null) {
			int count = covOptions.size();
			for (int i = 0; i < count; i++) {
				covOption = (CovOption) covOptions.get(i);
				if (covOption != null) {
					aNbaOinkRequest.addValue(covOption.getCovOptionPctInd());
				}
			}
		}
	}

	/**
	 * Retrieves the ReorderInd value based on the requirement id filter
	 * OLifE.Holding.Policy.RequirementInfo.RequirementInfoExtension.TrackingInfo.ReorderInd
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA192 New Method
	public void retrieveReorderInd(NbaOinkRequest aNbaOinkRequest) {
		String reqIdFilter = aNbaOinkRequest.getRequirementIdFilter();
		if (reqIdFilter != null && reqIdFilter.trim().length() > 0) {
			Policy policy = getPolicy();
			if (policy != null) {
				int listSize = policy.getRequirementInfoCount();
				RequirementInfo reqInfo = null;
				for (int i = 0; i < listSize; i++) {
					reqInfo = policy.getRequirementInfoAt(i);
					if (reqInfo != null && reqIdFilter.equalsIgnoreCase(reqInfo.getId())) {
						break;
					}
				}
				if (reqInfo != null) {
					RequirementInfoExtension extension = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
					if (extension != null && extension.hasTrackingInfo()) {
						aNbaOinkRequest.addValue(extension.getTrackingInfo().getReorderInd());
					}
				}
			}
		}
		aNbaOinkRequest.addValue(false);
	}

	/**
	 * Retrieves the ReviewedInd value based on the requirement id filter OLifE.Holding.Policy.RequirementInfo.RequirementInfoExtension.ReviewedInd
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA192 New Method
	public void retrieveReviewedInd(NbaOinkRequest aNbaOinkRequest) {
		String reqIdFilter = aNbaOinkRequest.getRequirementIdFilter();
		if (reqIdFilter != null && reqIdFilter.trim().length() > 0) {
			Policy policy = getPolicy();
			if (policy != null) {
				int listSize = policy.getRequirementInfoCount();
				RequirementInfo reqInfo = null;
				for (int i = 0; i < listSize; i++) {
					reqInfo = policy.getRequirementInfoAt(i);
					if (reqInfo != null && reqIdFilter.equalsIgnoreCase(reqInfo.getId())) {
						break;
					}
				}
				if (reqInfo != null) {
					RequirementInfoExtension extension = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
					if (extension != null) {
						aNbaOinkRequest.addValue(extension.getReviewedInd());
					}
				}
			}
		}
		aNbaOinkRequest.addValue(false);
	}

	/**
	 * Retrieves the follow up service provider value based on the requirement id filter
	 * OLifE.Holding.Policy.RequirementInfo.RequirementInfoExtension.TrackingInfo.FollowUpServiceProvider
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA192 New Method
	public void retrieveFollowUpServiceProvider(NbaOinkRequest aNbaOinkRequest) {
		String reqIdFilter = aNbaOinkRequest.getRequirementIdFilter();
		if (reqIdFilter != null && reqIdFilter.trim().length() > 0) {
			Policy policy = getPolicy();
			if (policy != null) {
				int listSize = policy.getRequirementInfoCount();
				RequirementInfo reqInfo = null;
				for (int i = 0; i < listSize; i++) {
					reqInfo = policy.getRequirementInfoAt(i);
					if (reqInfo != null && reqIdFilter.equalsIgnoreCase(reqInfo.getId())) {
						break;
					}
				}
				if (reqInfo != null) {
					RequirementInfoExtension extension = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
					if (extension != null && extension.hasTrackingInfo()) {
						aNbaOinkRequest.addValue(extension.getTrackingInfo().getFollowUpServiceProvider());
					}
				}
			}
		}
	}

	/**
	 * Obtain the value for ApplicationOrigin. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.ApplicationOrigin.
	 * 
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA187 New Method
	public void retrieveApplicationOrigin(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
		if (applicationInfoExtension != null) {
			aNbaOinkRequest.addValue(applicationInfoExtension.getApplicationOrigin());
		}
	}

	/**
	 * Obtain the value for InformalAppApproval. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.InformalAppApproval.
	 * 
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA187 New Method
	public void retrieveInformalAppApproval(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
		if (applicationInfoExtension != null) {
			aNbaOinkRequest.addValue(applicationInfoExtension.getInformalAppApproval());
		}
	}

	/**
	 * Obtain the value for GenericPlan in PolicyExtension. OLife.Holding.Policy.PolicyExtension.GenericPlan
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA139 New Method
	public void retrievePolicyGenericPlan(NbaOinkRequest aNbaOinkRequest) {
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(getPolicy());
		if (policyExtension != null) {
			aNbaOinkRequest.addValue(policyExtension.getGenericPlan());
		} else {
			aNbaOinkRequest.addValue("");
		}
	}

	/**
	 * Obtain the value for GenericPlanLevelPeriod. OLife.Holding.Policy.PolicyExtension.GenericPlanLevelPeriod
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA139 New Method
	public void retrieveGenericPlanLevelPeriod(NbaOinkRequest aNbaOinkRequest) {
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(getPolicy());
		if (policyExtension != null) {
			aNbaOinkRequest.addValue(policyExtension.getGenericPlanLevelPeriod());
		} else {
			aNbaOinkRequest.addValue("");
		}
	}

	/**
	 * Obtain the value for GenericPlanCalculationMethod. OLife.Holding.Policy.PolicyExtension.GenericPlanCalculationMethod
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA139 New Method
	public void retrieveGenericPlanCalculationMethod(NbaOinkRequest aNbaOinkRequest) {
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(getPolicy());
		if (policyExtension != null) {
			aNbaOinkRequest.addValue(policyExtension.getGenericPlanCalculationMethod());
		} else {
			aNbaOinkRequest.addValue("");
		}

	}

	/**
	 * Obtain the value for GenericPlanOverrideInd. OLife.Holding.Policy.PolicyExtension.GenericPlanOverrideInd
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA139 New Method
	public void retrieveGenericPlanOverrideInd(NbaOinkRequest aNbaOinkRequest) {
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(getPolicy());
		if (policyExtension != null) {
			aNbaOinkRequest.addValue(policyExtension.getGenericPlanOverrideInd());
		} else {
			aNbaOinkRequest.addValue("");
		}
	}

	/**
	 * Obtain the value for GenericPlan in CovOptionExtension. OLife.Holding.Policy.Life.Coverage.CovOption.CovOptionExtension.GenericPlan
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA139 New Method
	public void retrieveCoverageGenericPlanCOV(NbaOinkRequest aNbaOinkRequest) {
		Life life = getLife();
		int count = life.getCoverageCount();
		for (int i = 0; i < count; i++) {
			Coverage coverage = life.getCoverageAt(i);
			CoverageExtension covergeExtension = NbaUtils.getFirstCoverageExtension(coverage);
			if (covergeExtension != null) {
				aNbaOinkRequest.addValue(covergeExtension.getGenericPlan());
			} else {
				aNbaOinkRequest.addUnknownValue("");
			}
		}
	}

	/**
	 * Obtain the value for GenericPlan in SubAccount. OLife.Holding.Investment.SubAccount.SubAccountExtension.GenericPlan
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA139 New Method
	public void retrieveSubAccountGenericPlan(NbaOinkRequest aNbaOinkRequest) {
		Investment investment = getInvestment();
		int count = investment.getSubAccountCount();
		for (int i = 0; i < count; i++) {
			SubAccount subAccount = investment.getSubAccountAt(i);
			SubAccountExtension subAccountExt = NbaUtils.getFirstSubAccountExtension(subAccount);
			if (subAccountExt != null) {
				aNbaOinkRequest.addValue(subAccountExt.getGenericPlan());
			} else {
				aNbaOinkRequest.addUnknownValue("");
			}
		}
	}

	/**
	 * Obtain the value for PremType in Annuity. OLife.Holding.Holding.Policy.Annuity.PremType
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA139 New Method
	public void retrievePremType(NbaOinkRequest aNbaOinkRequest) {
		Annuity annuity = getAnnuity();
		if (annuity != null) {
			aNbaOinkRequest.addValue(annuity.getPremType());
		} else {
			aNbaOinkRequest.addUnknownValue("");
		}
	}

	/**
	 * Obtain the value for Distribution Channel from Distribution channel Info.
	 * OLifE.Party.Producer.CarrierAppointment.DistributionChannelInfo.DistributionChannel
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.06 New Method
	public void retrieveDistributionChannel(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			CarrierAppointment carrierAppointment = getCarrierAppointment(aNbaOinkRequest, i);
			List distChannelInfoList = carrierAppointment.getDistributionChannelInfo();
			if (distChannelInfoList.size() > 0) {
				for (int j = 0; j < distChannelInfoList.size(); j++) {
					DistributionChannelInfo distChannelInfo = (DistributionChannelInfo) distChannelInfoList.get(j);
					aNbaOinkRequest.addValue(distChannelInfo.getDistributionChannel());
				}
			} else {
				aNbaOinkRequest.addUnknownValue("");
			}
		}
	}

	/**
	 * Obtain the value of ExamDate from Risk. Risk.MedicalExam.ExamDate
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.06 New Method
	public void retrieveExamDate(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalExam aMedicalExam = getMedicalExam(aNbaOinkRequest, i);
			if (aMedicalExam != null) {
				aNbaOinkRequest.addValue(aMedicalExam.getExamDate());
			}
		}
	}

	/**
	 * Obtain the value for HomState, an instance of AddressCountryTc for an Address with type code = 1. OLifE.Party.Address.AddressCountryTc is the
	 * address Country
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.06 New Method
	public void retrieveHomCountry(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_HOME);
				if (address == null) {
					aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_NATION);
				} else {
					aNbaOinkRequest.addValue(address.getAddressCountryTC(), NbaTableConstants.OLI_LU_NATION);
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_NATION);
		}
	}

	/**
	 * Obtain the value for HomState, an instance of AddressCountryTc for an Address with type code = 1. OLifE.Party.Address.AddressCountryTc is the
	 * address Country
	 * @param aNbaOinkRequest - data request container
	 */
	//ALII915 New Method
	public void retrieveHomCountryX(NbaOinkRequest aNbaOinkRequest) {
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		Party party = null;
		aNbaOinkRequest.setParseMultiple(true);
		for (int i = 0; i < partyList.size() ; i++) {
			party = (Party)partyList.get(i);
			if (party != null) {
				Address address = getAddressForType(party, NbaOliConstants.OLI_ADTYPE_HOME);
				if (address == null) {
					aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_NATION);
				} else {
					aNbaOinkRequest.addValue(address.getAddressCountryTC(), NbaTableConstants.OLI_LU_NATION);
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_NATION);
		}
	}
	
	/**
	 * Obtain the value for Mailing, an instance of AddressCountryTc for an Address with type code = 1. OLifE.Party.Address.AddressCountryTc is the
	 * address Country
	 * @param aNbaOinkRequest - data request container
	 */
	//ALII1042 New Method
	public void retrieveMailingCountryX(NbaOinkRequest aNbaOinkRequest) {
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		Party party = null;
		aNbaOinkRequest.setParseMultiple(true);
		for (int i = 0; i < partyList.size() ; i++) {
			party = (Party)partyList.get(i);
			if (party != null) {
				Address address = getAddressForType(party, NbaOliConstants.OLI_ADTYPE_MAILING);
				if (address == null) {
					aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_NATION);
				} else {
					aNbaOinkRequest.addValue(address.getAddressCountryTC(), NbaTableConstants.OLI_LU_NATION);
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_NATION);
		}
	}	
	
	/**
	 * The method will set NbaOinkRequest Value as true if Home Adderss is entered for a Party, false otherwise
	 * @param aNbaOinkRequest
	 */
	// ALII1319 New Method
	public void retrieveHomeAddrPresent(NbaOinkRequest aNbaOinkRequest) {
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		Party party = null;
		for (int i = 0; i < partyList.size() ; i++) {
			party = (Party)partyList.get(i);
			if (party != null) {
				Address address = getAddressForType(party, NbaOliConstants.OLI_ADTYPE_HOME);
				if (address == null) {
					aNbaOinkRequest.addValue(false);
				} else {
					aNbaOinkRequest.addValue(true);
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(false);
		}
	}
	
	/**
	 * The method will set NbaOinkRequest Value as true if Mailing Adderss is entered for a Party, false otherwise
	 * @param aNbaOinkRequest
	 */
	// ALII1319 New Method
	public void retrieveMailingAddrPresent(NbaOinkRequest aNbaOinkRequest) {
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		Party party = null;
		for (int i = 0; i < partyList.size() ; i++) {
			party = (Party)partyList.get(i);
			if (party != null) {
				Address address = getAddressForType(party, NbaOliConstants.OLI_ADTYPE_MAILING);
				if (address == null) {
					aNbaOinkRequest.addValue(false);
				} else {
					aNbaOinkRequest.addValue(true);
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(false);
		}
	}
	
	//ALII1042 New Method
	public void retrievePartyTypeCodeX(NbaOinkRequest aNbaOinkRequest) {
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		Party party = null;
		aNbaOinkRequest.setParseMultiple(true);
		for (int i = 0; i < partyList.size(); i++) {
			party = (Party) partyList.get(i);
			if (party != null) {
				aNbaOinkRequest.addValue(party.getPartyTypeCode());
			} else {
				aNbaOinkRequest.addUnknownValue("");
			}
		}
	}

	
	/**
	 * Obtain the value for Exam date, from OlifE.Party.RiskExtension.MedicalCertification.ExamDate
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.06 New Method
	public void retrieveOtherCoExamDate(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk aRisk = getRisk(party);
			if (aRisk != null) {
				int index_extension = getExtensionIndex(aRisk.getOLifEExtension(), RISK_EXTN);
				if (index_extension != -1) {
					RiskExtension extension = aRisk.getOLifEExtensionAt(index_extension).getRiskExtension();
					if (extension != null) {
						int count = extension.getMedicalCertificationCount();
						for (int i = 0; i < count; i++) {
							MedicalCertification medCert = extension.getMedicalCertificationAt(i);
							if (medCert != null) {
								aNbaOinkRequest.addValue(medCert.getExamDate());
							} else {
								aNbaOinkRequest.addUnknownValue("");
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain the value for Exam date, from OlifE.Party.RiskExtension.MedicalCertification.OtherCompanyExamUsed
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.06 New Method
	public void retrieveOtherCoExamUsed(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk aRisk = getRisk(party);
			if (aRisk != null) {
				int index_extension = getExtensionIndex(aRisk.getOLifEExtension(), RISK_EXTN);
				if (index_extension != -1) {
					RiskExtension extension = aRisk.getOLifEExtensionAt(index_extension).getRiskExtension();
					if (extension != null) {
						int count = extension.getMedicalCertificationCount();
						for (int i = 0; i < count; i++) {
							MedicalCertification medCert = extension.getMedicalCertificationAt(i);
							if (medCert != null) {
								boolean otherCoExamUsedInd = medCert.getOtherCompanyExamUsedInd();
								aNbaOinkRequest.addValue(otherCoExamUsedInd);
							} else {
								aNbaOinkRequest.addUnknownValue("");
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain the value for Exam date, from OLifE.Party.PartyTypeCode
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.06 New Method
	public void retrievePartyTypeCode(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				aNbaOinkRequest.addValue(aParty.getPartyTypeCode());
			} else {
				aNbaOinkRequest.addUnknownValue("");
			}
		}
	}

	/**
	 * Obtain the value for Exam date, from OLifE.Party.Client.PrefLanguage
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.06 New Method
	public void retrievePrefLanguage(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Client client = getClient(aNbaOinkRequest, i);
			if (client != null) {
				aNbaOinkRequest.addValue(client.getPrefLanguage());
			} else {
				aNbaOinkRequest.addUnknownValue("");
			}
		}
	}

	/**
	 * Obtain the value for SelectAgent, from OLifE.Party.Producer.CarrierAppointment.OLifEExtension.CarrierAppointmentExtension.SelectAgentInd
	 * @param aNbaOinkRequest - data request container
	 */
	//	  AXAL3.7.06 New Method
	public void retrieveSelectAgentInd(NbaOinkRequest aNbaOinkRequest) {
		NbaParty nbaParty = nbaTXLife.getWritingAgent();
		if (nbaParty != null) {
			Party party = nbaParty.getParty();
			if (party.hasProducer()) {
				Producer producer = party.getProducer();
				if (producer.getCarrierAppointment().size() > 0) {
					CarrierAppointment carrierAppointment = producer.getCarrierAppointmentAt(0);
					CarrierAppointmentExtension extension = NbaUtils.getFirstCarrierAppointmentExtension(carrierAppointment);
					if (extension != null) {
						/*
						 * Code Start :: SRSR634919/APLS2666
						 */
						if(!extension.getSelectAgentInd() &&  
								((extension.getProducerDesignation()==NbaOliConstants.OLIEXT_PRODUCERDESIGNATION_1009800002)
								||(extension.getProducerDesignation()==NbaOliConstants.OLIEXT_PRODUCERDESIGNATION_1009800003)))
						{
							aNbaOinkRequest.addValue(true);
						}else
						{	
								aNbaOinkRequest.addValue(extension.getSelectAgentInd());
						}
						/*
						 * Code END :: SRSR634919/APLS2666
						 */
						return;
					}
				}
			}
		}
		aNbaOinkRequest.addUnknownValue("");
	}

	/**
	 * Obtain the value for SignatureOKIndCode. Holding. Policy.ApplicationInfo.SignatureInfo.OLifEExtension.SignatureInfoExtension.SignatureOKIndCode
	 * where Signature relation role code of �37� for Primary Writing Agent
	 * 
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.06 New Method
	public void retrieveSignatureOKIndCode(NbaOinkRequest aNbaOinkRequest) {
		int signatureInfoCount = getApplicationInfo().getSignatureInfoCount();
		boolean signatureFound = false;
		for (int i = 0; i < signatureInfoCount; i++) {
			SignatureInfo signInfo = getApplicationInfo().getSignatureInfoAt(i);
			if (signInfo.getSignatureRoleCode() == NbaOliConstants.OLI_PARTICROLE_PRIMAGENT) {
				SignatureInfoExtension signInfoExtension = NbaUtils.getFirstSignatureInfoExtension(signInfo);
				if (signInfoExtension != null && signInfoExtension.hasSignatureOKIndCode()) {
					signatureFound = true;
					aNbaOinkRequest.addValue(signInfoExtension.getSignatureOKIndCode());
					break;
				}
			}
		}
		if (signatureFound == false) {
			aNbaOinkRequest.addUnknownValue("");
		}
	}

	/**
	 * Obtain the value for ApplicationSignatureInd for primary insured.
	 * 
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.06 New Method
	public void retrieveApplicationSignatureInd(NbaOinkRequest aNbaOinkRequest) {
		int signatureInfoCount = getApplicationInfo().getSignatureInfoCount();
		boolean signatureFound = false;
		for (int i = 0; i < signatureInfoCount; i++) {
			SignatureInfo signInfo = getApplicationInfo().getSignatureInfoAt(i);
			if (signInfo.getSignatureRoleCode() == NbaOliConstants.OLI_PARTICROLE_PRIMARY && 
				signInfo.getSignaturePurpose() == NbaOliConstants.OLI_SIGTYPE_APPSIG) {
				signatureFound = true;
				aNbaOinkRequest.addValue(signatureFound);
				break;
			}
		}
		if (signatureFound == false) {
			aNbaOinkRequest.addValue(signatureFound);
		}
	}

	/**
	 * Obtain the value for ModalPremAmt. Holding.Policy.Life.Coverage.ModalPremAmt
	 * 
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.40 New Method
	public void retrieveModalPremAmt(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			String qualifier = aNbaOinkRequest.getQualifier();
			if (qualifier.equals(PARTY_PRIM_INSURED)) {
				List coverages = getCoveragesOrRiders(aNbaOinkRequest);
				int next = 0;
				Coverage coverage;
				while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
					if (coverage.hasModalPremAmt()) {
						aNbaOinkRequest.addValue(coverage.getModalPremAmt());
					}
				}
			} else {
				List coverages = getCoveragesOrRiders(aNbaOinkRequest);
				int next = 0;
				Coverage coverage;
				while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
					if (coverage.hasModalPremAmt()) {
						aNbaOinkRequest.addValue(coverage.getModalPremAmt());
					}
				}
			}
		}
	}

	/**
	 * Obtain the value for Eff Date. Holding.Policy.EffDate
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.40 New Method
	public void retrievePolicyEffDate(NbaOinkRequest aNbaOinkRequest) {
		if (getNbaTXLife().getPolicy().hasEffDate()) {
			aNbaOinkRequest.addValue(getNbaTXLife().getPolicy().getEffDate());
		} else {
			aNbaOinkRequest.addUnknownValue((Date) null);
		}
	}

	/**
	 * Obtain the value for CurrentDate
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.40 New Method
	public void retrieveCurrentDate(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(new Date());
	}

	/**
	 * Obtain the value for GovtImmigrationNo. OLifE.Party.PersonOrOrganization.Person.GovtImmigrationNo is the GovtImmigrationNo of the person
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.40 New Method
	public void retrieveGovtImmigrationNo(NbaOinkRequest aNbaOinkRequest) {
		try {
			for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					aNbaOinkRequest.addValue(person.getGovtImmigrationNo());
				} else {
					break;
				}
			}
		} catch (Exception e) {
			e.toString();
		}
	}

	/**
	 * Obtain the value for PassportNo. OLifE.Party.PersonOrOrganization.Person.PassportNo is the PassportNo of the person
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.40 New Method
	public void retrievePassportNo(NbaOinkRequest aNbaOinkRequest) {
		try {
			for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null) {
					aNbaOinkRequest.addValue(person.getPassportNo());
				} else {
					break;
				}
			}
		} catch (Exception e) {
			e.toString();
		}
	}

	/**
	 * Obtain the value for BirthCountry. OLifE.Party.PersonOrOrganization.Person.BirthCountry is the BirthCountry of the person
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.40 New Method
	public void retrieveBirthCountry(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			//ALII828
			if (person == null) {
				aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_NATION);
			} else {
				aNbaOinkRequest.addValue(person.getBirthCountry(), NbaTableConstants.OLI_LU_NATION);
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_NATION);
		}
	}

	/**
	 * Obtain the value for DeliveryReceiptDate. RequirementInfoExtension.DeliveryReceiptSignDate
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.40 New Method
	public void retrieveDeliveryReceiptDate(NbaOinkRequest aNbaOinkRequest) {
		List requirementInfos = getNbaTXLife().getPolicy().getRequirementInfo();
		Date receiptSignDate = null;//APSL2333
		Date createDate = null;////APSL2333
		for (int i = 0; requirementInfos != null && i < requirementInfos.size(); i++) {
			RequirementInfo requirementInfo = getNbaTXLife().getPolicy().getRequirementInfoAt(i);
			if (requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_POLDELRECEIPT) {
				RequirementInfoExtension reqInfoExtn = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
				if (reqInfoExtn != null && reqInfoExtn.hasDeliveryReceiptSignDate()) {
					//Begin APSL2333
					if (createDate == null) {
						createDate = reqInfoExtn.getCreatedDate();
						receiptSignDate = reqInfoExtn.getDeliveryReceiptSignDate();
					} else if (createDate.compareTo(reqInfoExtn.getCreatedDate()) <= 0) {
						createDate = reqInfoExtn.getCreatedDate();
						receiptSignDate = reqInfoExtn.getDeliveryReceiptSignDate();
					}
				}
			}
		}
		if (receiptSignDate != null) {
			aNbaOinkRequest.addValue(receiptSignDate);
		} else {
			aNbaOinkRequest.addUnknownValue((Date) null);
		}
		//End APSL2333
	}

	/**
	 * Obtain the value for ParamedSignDate.
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.40 New Method
    public void retrieveParamedSignDate(NbaOinkRequest aNbaOinkRequest) {
        Date paramedReqDate = null;// apsl4585
        boolean parmedSignedDateExist = false;// apsl4585
        List requirementInfos = getNbaTXLife().getPolicy().getRequirementInfo();
        for (int i = 0; requirementInfos != null && i < requirementInfos.size(); i++) {
            RequirementInfo requirementInfo = getNbaTXLife().getPolicy().getRequirementInfoAt(i);
            if ((requirementInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_RECEIVED)
                    && (requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MEDEXAMPARAMED || requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MEDEXAMMD)) {
                RequirementInfoExtension reqInfoExtn = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
                if (reqInfoExtn != null && reqInfoExtn.hasParamedSignedDate()) {
                    // Begin:APSL4585 condition added to set latest parmedsigned date
                    if (paramedReqDate == null || reqInfoExtn.getParamedSignedDate().after(paramedReqDate)) {
                        paramedReqDate = reqInfoExtn.getParamedSignedDate();
                        parmedSignedDateExist = true;
                    }
                }

            }
        }
        if (parmedSignedDateExist) {
            aNbaOinkRequest.addValue(paramedReqDate);
        } else {
            aNbaOinkRequest.addUnknownValue((Date) null);
        }
        // End APSL4585
    }

	/**
	 * Obtain the value for RequirementInfoExtension.PremiumDueCarrierReceiptDate
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.40 New Method
	public void retrievePremiumDueCarrierReceiptDate(NbaOinkRequest aNbaOinkRequest) {
		List requirementInfos = getNbaTXLife().getPolicy().getRequirementInfo();
		for (int i = 0; requirementInfos != null && i < requirementInfos.size(); i++) {
			RequirementInfo requirementInfo = getNbaTXLife().getPolicy().getRequirementInfoAt(i);
			if (requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_PREMDUE
					&& requirementInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_RECEIVED) {
				RequirementInfoExtension reqInfoExtn = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
				if (reqInfoExtn != null && reqInfoExtn.hasPremiumDueCarrierReceiptDate()) {
					aNbaOinkRequest.addValue(reqInfoExtn.getPremiumDueCarrierReceiptDate());
				} else {
					aNbaOinkRequest.addUnknownValue((Date) null);
				}
				break;
			}
		}
	}

	/**
	 * Obtain the value for FinancialActivity.FinEffDate
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.40 New Method
	public void retrieveFinEffDate(NbaOinkRequest aNbaOinkRequest) {
		ArrayList finanActivityList = getNbaTXLife().getPolicy().getFinancialActivity();
		Date finEffDate = null;
		Date checkScanDate = null;
		if (finanActivityList != null) {
			for (int i = 0; i < finanActivityList.size(); i++) {
				FinancialActivity financialActivity = (FinancialActivity) finanActivityList.get(i);
				//Begin QC#8467 APSL1995
				FinancialActivityExtension finActExtension=NbaUtils.getFirstFinancialActivityExtension(financialActivity);
				if ((finActExtension.hasCheckScanDate() || financialActivity.hasFinEffDate()) && !skipFinancialActivityType(financialActivity)) {//ALS4938
					
					if (checkScanDate == null) {
						checkScanDate = finActExtension.getCheckScanDate();
					} else if (checkScanDate.before(finActExtension.getCheckScanDate())) {
						checkScanDate = finActExtension.getCheckScanDate();
					}
					if (finEffDate == null) {
						finEffDate = financialActivity.getFinEffDate();
					} else if (finEffDate.before(financialActivity.getFinEffDate())) {
						finEffDate = financialActivity.getFinEffDate();
					}
					//End QC#8467 APSL1995
				}
			}
			if (checkScanDate != null) {//QC#8467 APSL1995
				aNbaOinkRequest.addValue(checkScanDate);
			} else if (finEffDate != null) {
				aNbaOinkRequest.addValue(finEffDate);
			} else {
				aNbaOinkRequest.addUnknownValue((Date) null);
			}
		}
	}		
	/**
	 * This method checks the Financial Activity Sub Type and if it is Reverse,Refund, or Partial 
	 * Refund then returns true. If the disbursement Indicator is true for the financial activity 
	 * then also this method returns true.     
	 * @param FinancialActivity financialActivity - fro which the subactivity type is to be checked
	 */
	//ALS4938 New Method
	public boolean skipFinancialActivityType(FinancialActivity financialActivity) {
		boolean skipFinActivityType = false;
		long finActivitySubType = 0;
		FinancialActivityExtension financialActivityExtension = NbaUtils.getFirstFinancialActivityExtension(financialActivity);
		if (financialActivityExtension != null) {
			if (financialActivityExtension.getDisbursedInd()) {
				skipFinActivityType = true;
			}
		}
		finActivitySubType = financialActivity.getFinActivitySubType();
		if (NbaOliConstants.OLI_FINACTSUB_REV == finActivitySubType || NbaOliConstants.OLI_FINACTSUB_REFUND == finActivitySubType
				|| NbaOliConstants.OLI_FINACTSUB_PARTIALREFUND == finActivitySubType) {
			skipFinActivityType = true;
		}
		return skipFinActivityType;
	}
	

	/**
	 * Obtain the value for rate class. OLifE.Party.PersonOrOrganization.Person.RateClass OLifE.Holding.Policy.Life.Coverage.RateClass
	 * @param aNbaOinkRequest - data request container
	 * @deprecated this method will be removed in a future release. Use {@link #retrieveSmokerStat(NbaOinkRequest)}
	 */
	//New Method AXAL3.7.14
	public void retrieveTobaccoPremiumBasis(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage;
		long tobPremBasis;
		List coverages = getCoveragesOrRiders(aNbaOinkRequest);
		int next = 0;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			tobPremBasis = -1L;
			LifeParticipant insLifeParticipant = NbaUtils.getInsurableLifeParticipant(coverage);
			if (insLifeParticipant.hasTobaccoPremiumBasis()) {
				tobPremBasis = insLifeParticipant.getTobaccoPremiumBasis();
			}
			aNbaOinkRequest.addValue(tobPremBasis);
		}
	}

	/**
	 * Obtain the value for Policy.PaymentDraftDay
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.40 New Method
	public void retrievePaymentDraftDay(NbaOinkRequest aNbaOinkRequest) {
		if (getNbaTXLife().getPolicy().hasPaymentDraftDay()) {
			aNbaOinkRequest.addValue(getNbaTXLife().getPolicy().getPaymentDraftDay());
		}
	}

	/**
	 * Obtain the values for USCitizenshipList for Owners
	 * @param aNbaOinkRequest - data request container
	 */
	//New Method AXAL3.7.07
	public void retrieveUSCitizenshipIndCodeList(NbaOinkRequest aNbaOinkRequest) {

		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(person.getOLifEExtension(), PERSON_EXTN);
				if (index_extension != -1) {
					oli = person.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					PersonExtension personExtension = oli.getPersonExtension();
					if (personExtension != null) {
						aNbaOinkRequest.addValue(personExtension.getUSCitizenIndCode());
					}
				}
			} // end else
		} // end for
	}

	/**
	 * Obtain the value for UserCode. OLifE.Holding.Policy.RequirementInfo.UserCode is the code specifying the underwriting requirement..
	 * @param aNbaOinkRequest - data request container
	 */
	// AXAL3.7.07 New Method
	public void retrieveUserCode(NbaOinkRequest aNbaOinkRequest) {
		List reqList = getRequirementInfos(aNbaOinkRequest); //SPR3353
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			RequirementInfo aRequirementInfo = getRequirementInfo(reqList, i);
			if (aRequirementInfo != null) {
				aNbaOinkRequest.addValue(aRequirementInfo.getUserCode());
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for OLifE.Party.Risk.MedicalCondition.OLifEExtension.MedicalConditionExtension.QuestionNumber.
	 * @param aNbaOinkRequest - data request container
	 */
	// AXAL3.7.07 New Method
	public void retrieveQuestionNumber(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalCondition aMedicalCondition = getMedicalCondition(aNbaOinkRequest, i);
			if (aMedicalCondition != null) {
				MedicalConditionExtension medicalConditionExtension = NbaUtils.getFirstMedicalConditionExtension(aMedicalCondition);
				if (medicalConditionExtension != null) {
					aNbaOinkRequest.addValue(medicalConditionExtension.getQuestionNumber());
				} else {
					aNbaOinkRequest.addValue("");
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for OLifE.Party.Risk.MedicalCondition.OLifEExtension.MedicalConditionExtension.RecommendedTreatmentIndCode
	 * @param aNbaOinkRequest - data request container
	 */
	// ALS2936
	public void retrieveRecommendTreatmentIndCode(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalCondition aMedicalCondition = getMedicalCondition(aNbaOinkRequest, i);
			if (aMedicalCondition != null) {
				MedicalConditionExtension medicalConditionExtension = NbaUtils.getFirstMedicalConditionExtension(aMedicalCondition);
				if (medicalConditionExtension != null) {
					aNbaOinkRequest.addValue(medicalConditionExtension.getRecommendTreatmentIndCode());
				} else {
					aNbaOinkRequest.addValue("2"); //apsl4782
				}
			} else {
				aNbaOinkRequest.addValue("2"); //apsl4782
			}
		}
	}

	/**
	 * Obtain the value for OLifE.Party.Risk.MedicalCondition.OLifEExtension.MedicalConditionExtension.QuestionText.
	 * @param aNbaOinkRequest - data request container
	 */
	// AXAL3.7.07 New Method
	public void retrieveQuestionText(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalCondition aMedicalCondition = getMedicalCondition(aNbaOinkRequest, i);
			if (aMedicalCondition != null) {
				MedicalConditionExtension medicalConditionExtension = NbaUtils.getFirstMedicalConditionExtension(aMedicalCondition);
				if (medicalConditionExtension != null) {
					aNbaOinkRequest.addValue(medicalConditionExtension.getQuestionText());
				} else {
					aNbaOinkRequest.addValue("");
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for OLifE.Party.Risk.MedicalCondition.OLifEExtension.MedicalConditionExtension.QuestionText.
	 * @param aNbaOinkRequest - data request container
	 */
	// AXAL3.7.07 New Method
	public void retrieveRiskType(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			MedicalCondition aMedicalCondition = getMedicalCondition(aNbaOinkRequest, i);
			if (aMedicalCondition != null) {
				MedicalConditionExtension medicalConditionExtension = NbaUtils.getFirstMedicalConditionExtension(aMedicalCondition);
				if (medicalConditionExtension != null) {
					aNbaOinkRequest.addValue(medicalConditionExtension.getRiskType());
				} else {
					aNbaOinkRequest.addValue("");
				}
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain all LabTesting LabTestRemark RemarkCodes for a LabTesting object.
	 * @param aNbaOinkRequest - data request container
	 */
	// AXAL3.7.07 New Method
	public void retrieveLabTestingRemarkCode(NbaOinkRequest aNbaOinkRequest) {
		LabTesting labTesting = getlabTesting(aNbaOinkRequest, 0);
		if (labTesting != null) {
			ArrayList labTestingRemarkList = labTesting.getLabTestRemark();
			LabTestRemark labTestRemark = null;
			int count = labTestingRemarkList.size();
			for (int i = 0; i < count; i++) {
				labTestRemark = (LabTestRemark) labTestingRemarkList.get(i);
				aNbaOinkRequest.addValue(labTestRemark.getRemarkCode());
			}
		}
	}

	/**
	 * Obtain all LabTesting LabTestRemark RemarkSubCodes for a LabTesting object.
	 * @param aNbaOinkRequest - data request container
	 */
	// AXAL3.7.07 New Method
	public void retrieveLabTestingRemarkSubCode(NbaOinkRequest aNbaOinkRequest) {
		LabTesting labTesting = getlabTesting(aNbaOinkRequest, 0);
		if (labTesting != null) {
			ArrayList labTestingRemarkList = labTesting.getLabTestRemark();
			LabTestRemark labTestRemark = null;
			int count = labTestingRemarkList.size();
			for (int i = 0; i < count; i++) {
				labTestRemark = (LabTestRemark) labTestingRemarkList.get(i);
				aNbaOinkRequest.addValue(labTestRemark.getRemarkSubCode());
			}
		}
	}

	/**
	 * Obtain all the values Risk.PrescriptionDrug.PrescriptionLabel.
	 * @param aNbaOinkRequest - data request container
	 */
	// AXAL3.7.07 New Method
	public void retrievePrescriptionLabel(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk risk = getRisk(party);
			PrescriptionDrug prescriptionDrug = null;
			ArrayList prescriptionDrugList = risk.getPrescriptionDrug();
			int count = prescriptionDrugList.size();
			for (int i = 0; i < count; i++) {
				prescriptionDrug = (PrescriptionDrug) prescriptionDrugList.get(i);
				aNbaOinkRequest.addValue(prescriptionDrug.getPrescriptionLabel());
			}
		}
	}

	/**
	 * Obtain all the values Risk.PrescriptionDrug.PrescriptionCode.
	 * @param aNbaOinkRequest - data request container
	 */
	// AXAL3.7.07 New Method
	public void retrievePrescriptionCode(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Risk risk = getRisk(party);
			PrescriptionDrug prescriptionDrug = null;
			ArrayList prescriptionDrugList = risk.getPrescriptionDrug();
			int count = prescriptionDrugList.size();
			for (int i = 0; i < count; i++) {
				prescriptionDrug = (PrescriptionDrug) prescriptionDrugList.get(i);
				aNbaOinkRequest.addValue(prescriptionDrug.getPrescriptionCode());
			}
		}
	}

	/**
	 * Obtain all the values FundingDisclosureTC values from the contract.
	 * @param aNbaOinkRequest - data request container
	 */
	// AXAL3.7.07 New Method
	public void retrieveFundingDisclosureTC(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		FundingDisclosureDetails fundingDisclosureDetails = null;
		int numCount = 0;
		if (party != null) {
			Risk aRisk = getRisk(party);
			if (aRisk != null) {
				int index_extension = getExtensionIndex(aRisk.getOLifEExtension(), RISK_EXTN);
				if (index_extension != -1) {
					RiskExtension riskExtension = aRisk.getOLifEExtensionAt(index_extension).getRiskExtension();
					if (riskExtension != null) {
						ArrayList fundingDisclosureDetailsIndex = riskExtension.getFundingDisclosureDetails();
						numCount = fundingDisclosureDetailsIndex.size();
						for (int i = 0; i < numCount; i++) {
							fundingDisclosureDetails = (FundingDisclosureDetails) fundingDisclosureDetailsIndex.get(i);
							aNbaOinkRequest.addValue(fundingDisclosureDetails.getFundingDisclosureTC());
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain all the values ExpenseNeedTypeCodeCC values from the contract.
	 * @param aNbaOinkRequest - data request container
	 */
	// AXAL3.7.07 New Method
	public void retrieveExpenseNeedTypeCodeCC(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				String partyId = party.getId();
				IntentExtension intentExtension = getPersonalIntentExtension(i, partyId); //Personal

				if (intentExtension != null) {
					ExpenseNeedTypeCodeCC expenseNeedTypeCodeCC = intentExtension.getExpenseNeedTypeCodeCC();
					if (expenseNeedTypeCodeCC != null) {
						String sValue = "";
						ArrayList aList = expenseNeedTypeCodeCC.getExpenseNeedTypeCode();
						for (int k = 0; k < aList.size(); k++) {
							sValue = aList.get(k).toString();
							aNbaOinkRequest.addValue(sValue);
						}
					}
				}

				intentExtension = getBusinessIntentExtension(i, partyId); //Business
				if (intentExtension != null) {
					ExpenseNeedTypeCodeCC expenseNeedTypeCodeCC = intentExtension.getExpenseNeedTypeCodeCC();
					if (expenseNeedTypeCodeCC != null) {
						String sValue = "";
						ArrayList aList = expenseNeedTypeCodeCC.getExpenseNeedTypeCode();
						for (int k = 0; k < aList.size(); k++) {
							sValue = aList.get(k).toString();
							aNbaOinkRequest.addValue(sValue);
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain the value for OLifE.Party.Client.OLifEExtension.ClientExtension.AgentRelationshipDescription 
	 * has a value or not
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.43 New Method
	public void retrieveHasAgentRelationshipDescription(NbaOinkRequest aNbaOinkRequest) {
		boolean found = false;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Client client = aParty.getClient();
				if (client != null) {
					ClientExtension clientExtension = NbaUtils.getClientExtension(client);
					if (clientExtension != null) {
						if (clientExtension.hasAgentRelationshipDescription()) {
							found = true;
						}
					}
				}
			}
			aNbaOinkRequest.addValue(found);
		}
	}

	/**
	 * Obtain the value for VisaExpDate. OLifE.Party.PersonOrOrganization.Person.VisaExpDate of the person
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.40 New Method
	public void retrieveVisaExpDate(NbaOinkRequest aNbaOinkRequest) {
		try {
			for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
				Person person = getPerson(aNbaOinkRequest, i);
				if (person != null && person.hasVisaExpDate()) {
					aNbaOinkRequest.addValue(person.getVisaExpDate());
				} else {
					break;
				}
			}
		} catch (Exception e) {
			e.toString();
		}
	}

	/**
	 * Obtain the value for CheckNameMissingIndCode
	 * 
	 * @param aNbaOinkRequest -
	 *                data request container
	 */
	//	AXAL3.7.40 New Method
	public void retrieveCheckNameMissingInd(NbaOinkRequest aNbaOinkRequest) {
		try {
			boolean checkNameMissingInd = false;
			Banking banking = NbaUtils.getBanking(getOLifE(), NbaOliConstants.OLI_HOLDTYPE_BANKING);
			BankingExtension bankingExt = NbaUtils.getFirstBankingExtension(banking);
			if (bankingExt == null || bankingExt.getAuthorizedSignatoryCount() <= 0) {
				checkNameMissingInd = true;
			} else {
				for (int index = 0; index < bankingExt.getAuthorizedSignatoryCount(); index++) {
					AuthorizedSignatory authSign = bankingExt.getAuthorizedSignatoryAt(index);
					if (authSign == null || NbaUtils.isBlankOrNull(authSign.getSignatoryName())) {
						checkNameMissingInd = true;
						break;
					}
				}
			}
			aNbaOinkRequest.addValue(checkNameMissingInd);
		} catch (Exception e) {
			e.toString();
		}
	}

	/**
	 * Obtain the value for Transit/Routing Number. OLifE.Holding.Banking.Transit/Routing Number is the aTransit/Routing Number for the banking
	 * object.
	 * 
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.40 new method
	public void retrieveRoutingNumber(NbaOinkRequest aNbaOinkRequest) {
		Banking banking = NbaUtils.getBanking(getOLifE(), NbaOliConstants.OLI_HOLDTYPE_BANKING);
		if (banking == null || !banking.hasRoutingNum()) {
			aNbaOinkRequest.addValue("");
		} else {
			aNbaOinkRequest.addValue(banking.getRoutingNum());
		}
	}
	// Begin APSL2735 
	public void retrieveACHRoutingNumber(NbaOinkRequest aNbaOinkRequest) {
		Banking banking = NbaUtils.getBankingByHoldingSubType(getNbaTXLife(), NbaOliConstants.OLI_HOLDSUBTYPE_INITIAL);
		if (banking == null || !banking.hasRoutingNum()) {
			aNbaOinkRequest.addValue("");
		} else {
			aNbaOinkRequest.addValue(banking.getRoutingNum());
		}
	}
	
	public void retrieveACHAccountNumber(NbaOinkRequest aNbaOinkRequest) {
		Banking banking = NbaUtils.getBankingByHoldingSubType(getNbaTXLife(), NbaOliConstants.OLI_HOLDSUBTYPE_INITIAL);
		if (banking == null || !banking.hasAccountNumber()) {
			aNbaOinkRequest.addValue("");
		} else {
			aNbaOinkRequest.addValue(banking.getAccountNumber());
		}
	}

	// End APSL2735
	
	/**
	 * Obtain the value of restrict code for system message with lowest restrict code.
	 * 
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.40 new method
	public void retrieveRestrictCode(NbaOinkRequest aNbaOinkRequest) {
		long temp = NbaConstants.LONG_NULL_VALUE;
		if (!nbaTXLife.isInformalApplication() && !nbaTXLife.isPreSaleApplication()) {//ALS5411
			temp = nbaTXLife.getMostRestrictedCode();
		}
		aNbaOinkRequest.addValue(String.valueOf(temp));
	}

	/**
	 * Obtain the vale for Party.Producer.CarrierAppointment.OLifEExtension.CarrierAppointmentExtension.ASUCode
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.3M1 New method
	public void retrieveASUCode(NbaOinkRequest aNbaOinkRequest) {
		CarrierAppointment carrierAppointment = getCarrierAppointment(aNbaOinkRequest, 0);
		if (carrierAppointment != null) {
			CarrierAppointmentExtension extension = NbaUtils.getFirstCarrierAppointmentExtension(carrierAppointment);
			if (extension != null) {
				aNbaOinkRequest.addValue(extension.getASUCode());
			}
		}
	}

	/**
	 * Obtain the vale for Party.Producer.CarrierAppointment.OLifEExtension.CarrierAppointmentExtension.BGAUWTeam
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.3M1 New method
	public void retrieveBGAUWTeam(NbaOinkRequest aNbaOinkRequest) {
		// APSL3447 Begins
		Relation relation = null;
		if (NbaUtils.isHVTCase(getNbaTXLife())) {
			relation = getNbaTXLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_PROCESSINGFIRM);
		} else {
			relation = getNbaTXLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_GENAGENT);
		}
		// APSL3447 Ends
		if (relation != null) {
			NbaParty bgaParty = getNbaTXLife().getParty(relation.getRelatedObjectID());
			if (!NbaUtils.isDeleted(bgaParty.getParty())) {// ALPC168
				CarrierAppointment carrierAppointment = bgaParty.getParty().getProducer().getCarrierAppointmentAt(0);
				if (carrierAppointment != null) {
					CarrierAppointmentExtension extension = NbaUtils.getFirstCarrierAppointmentExtension(carrierAppointment);
					if (extension != null) {
						aNbaOinkRequest.addValue(extension.getBGAUWTeam());
					}
				}
			}// ALPC168
		}
	}

	/**
	 * Obtain the vale for Party.Producer.CarrierAppointment.CompanyProducerID
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.3M1 New method
	public void retrieveSuperBGANbr(NbaOinkRequest aNbaOinkRequest) {
		Relation relation = getNbaTXLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_SUPERIORAGENT);
		if (relation != null) {
			NbaParty superBgaParty = getNbaTXLife().getParty(relation.getRelatedObjectID());
			if (!NbaUtils.isDeleted(superBgaParty.getParty())) {//ALPC168
				CarrierAppointment carrierAppointment = superBgaParty.getParty().getProducer().getCarrierAppointmentAt(0);
				if (carrierAppointment != null) {
					aNbaOinkRequest.addValue(carrierAppointment.getCompanyProducerID());
				}
			}//ALPC168
		}
	}

	/**
	 * Obtain the value for rate class. OLifE.Party.PersonOrOrganization.Person.RateClass OLifE.Holding.Policy.Life.Coverage.RateClass
	 */
	//AXAL3.7.13I
	public void retrieveRateClassText(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage;
		CoverageExtension coverageExt;
		String rateclass;
		List coverages = getCoveragesOrRiders(aNbaOinkRequest);
		int next = 0;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			rateclass = "";
			coverageExt = NbaUtils.getFirstCoverageExtension(coverage);
			if (coverageExt != null && coverageExt.hasRateClass()) {
				rateclass = coverageExt.getRateClass();
			}
			aNbaOinkRequest.addValue(rateclass, NbaTableConstants.NBA_RATECLASS);
		}
	}

	/**
	 * Obtain the value for Base Second Flat Extra. Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.TempFlatExtraAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA072 New Method modified for AXAL3.7.13I, CR1343973 method refactored
	public void retrieveTempFlatSecondExtraAmt(NbaOinkRequest aNbaOinkRequest) {
		List ratings = getTempFlatSubstandardRatingsForParty(aNbaOinkRequest); 
		SubstandardRating substandardRating;		
		if (ratings.size() >= 2) {
			substandardRating = (SubstandardRating) ratings.get(1);
			aNbaOinkRequest.addValue(substandardRating.getTempFlatExtraAmt());
		} else {
			aNbaOinkRequest.addUnknownValue("");
		}
	}

	/**
	 * Obtain the value for Base Second Flat Extra. Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.TempFlatExtraAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.13I New Method
	public void retrieveTempFlatThirdExtraAmt(NbaOinkRequest aNbaOinkRequest) {
		List ratings = getTempFlatSubstandardRatingsForParty(aNbaOinkRequest); 
		SubstandardRating substandardRating;		
		if (ratings.size() >= 3) {
			substandardRating = (SubstandardRating) ratings.get(2);
			aNbaOinkRequest.addValue(substandardRating.getTempFlatExtraAmt());
		} else {
			aNbaOinkRequest.addUnknownValue("");
		}
	}

	/**
	 * Obtain the value for Perm Table Rating Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.PermTableRating
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.13I New Method
	public void retrievePermTableRatingText(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = null;
		String coverageType = aNbaOinkRequest.getQualifier();
		if (coverageType.equals(NON_RIDER_COV) || coverageType.equals(RIDER)) {
			List coverages = null;
			if (coverageType.equals(NON_RIDER_COV)) {
				coverages = getNonRider();
			} else {
				coverages = getRider();
			}
			if (coverages == null) {
				return;
			}
			int count = coverages.size();
			for (int i = 0; i < count; i++) {
				coverage = (Coverage) coverages.get(i);
				boolean resolved = false;
				LifeParticipant lifeParticipant = NbaUtils.getInsurableLifeParticipant(coverage);
				if (lifeParticipant != null && !lifeParticipant.isActionDelete()) {
					int countSR = lifeParticipant.getSubstandardRatingCount();
					for (int j = 0; j < countSR && !resolved; j++) {
						SubstandardRating substandardRating = lifeParticipant.getSubstandardRatingAt(j);
						if (NbaUtils.isValidRating(substandardRating) && substandardRating.hasPermTableRating()) { //SPR2590
							aNbaOinkRequest.addValue(substandardRating.getPermTableRating());
							resolved = true;
						}
					}
				}
				if (!resolved) {
					aNbaOinkRequest.addValue(-1);
				}
			}
		}
	}

	/**
	 * Set the value for PermFlatExtraAmt. OLifE.Holding.Policy.Life.Coverage.LifeParticipant.PermFlatExtraAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.13I New Method, CR1343973 method refactored
	public void retrievePermFlatSecondExtraAmt(NbaOinkRequest aNbaOinkRequest) {
		SubstandardRating substandardRating;
		SubstandardRatingExtension substandardRatingExt;
		List ratings = getPermFlatSubstandardRatingsForParty(aNbaOinkRequest);
		if (ratings.size() >= 2) {
			substandardRating = (SubstandardRating) ratings.get(1);
			substandardRatingExt = NbaUtils.getFirstSubstandardExtension(substandardRating);
			aNbaOinkRequest.addValue(substandardRatingExt.getPermFlatExtraAmt());			 			
		} else {
			aNbaOinkRequest.addUnknownValue("");
		}		
	}

	/**
	 * Set the value for PermFlatExtraAmt. OLifE.Holding.Policy.Life.Coverage.LifeParticipant.PermFlatExtraAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.13I New Method, CR1343973 method refactored
	public void retrievePermFlatThirdExtraAmt(NbaOinkRequest aNbaOinkRequest) {
		SubstandardRating substandardRating;
		SubstandardRatingExtension substandardRatingExt;
		List ratings = getPermFlatSubstandardRatingsForParty(aNbaOinkRequest);
		if (ratings.size() >= 3) {
			substandardRating = (SubstandardRating) ratings.get(2);
			substandardRatingExt = NbaUtils.getFirstSubstandardExtension(substandardRating);
			aNbaOinkRequest.addValue(substandardRatingExt.getPermFlatExtraAmt());			 			
		} else {
			aNbaOinkRequest.addUnknownValue("");
		}		
	}

	//AXAL3.7.13I New Method
	protected SubstandardRatingExtension getSubstandardRatingExtensionAt(NbaOinkRequest aNbaOinkRequest, int index, String SubstandardExtntype) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		LifeParticipant lifeParticipant;
		SubstandardRating substandardRating;
		SubstandardRatingExtension substandardExtension = null;
		int susbstandPermCount = 0;
		int susbstandTempCount = 0;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			int lifeIdx = coverage.getLifeParticipantCount();
			for (int i = 0; i < lifeIdx; i++) {
				lifeParticipant = coverage.getLifeParticipantAt(i);
				if (participants.contains(lifeParticipant)) {
					int ratIdx = lifeParticipant.getSubstandardRatingCount();
					for (int j = 0; j < ratIdx; j++) {
						substandardRating = lifeParticipant.getSubstandardRatingAt(j);
						if (NbaUtils.isValidRating(substandardRating)) { //SPR2590
							String type = NbaUtils.getSubstandardRatingType(substandardRating);
							if (SubstandardExtntype.equals(type)) {
								substandardExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
								if (substandardExtension != null) {
									susbstandPermCount++;
								}
								if (susbstandPermCount == index) {
									return substandardExtension;
								}

							} else {
								substandardExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
								if (substandardExtension != null) {
									susbstandTempCount++;
								}
								if (susbstandTempCount == index) {
									return substandardExtension;
								}
							}
							substandardExtension = null; //ALS4725
						}
					}
				}
			}
		}
		return substandardExtension;
	}

	/**
	 * Obtain the value for Holding.Policy.ApplicationInfo.PlacementEndDate
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.13I New method
	public void retrievePlacementEndDate(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo applicationInfo = getApplicationInfo();
		if (applicationInfo != null && applicationInfo.hasPlacementEndDate()) {
			aNbaOinkRequest.addValue(applicationInfo.getPlacementEndDate());
		}
	}

	/**
	 * Obtain the value for Holding.Policy.ApplicationInfo.UnderwritingStatus
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.13I New method
	public void retrieveCaseFinalDispstnText(NbaOinkRequest aNbaOinkRequest) {

		ApplicationInfo applicationInfo = getApplicationInfo();
		if (applicationInfo != null) {
			int index_extension = getExtensionIndex(applicationInfo.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_extension != -1) {
				ApplicationInfoExtension extension = applicationInfo.getOLifEExtensionAt(index_extension).getApplicationInfoExtension();
				if (extension != null) {
					aNbaOinkRequest.addValue(extension.getUnderwritingStatus(), NbaTableConstants.NBA_FINAL_DISPOSITION); //ALS2062 QC1215 //ALS2366
				}
			}
		}
	}

	/**
	 * Obtain the value for Holding.Policy.ApplicationInfo.UnderwritingStatus
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.20 New method
	public void retrieveCaseFinalDispstn(NbaOinkRequest aNbaOinkRequest) {

		ApplicationInfo applicationInfo = getApplicationInfo();
		if (applicationInfo != null) {
			int index_extension = getExtensionIndex(applicationInfo.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_extension != -1) {
				ApplicationInfoExtension extension = applicationInfo.getOLifEExtensionAt(index_extension).getApplicationInfoExtension();
				if (extension != null) {
					aNbaOinkRequest.addValue(extension.getUnderwritingStatus());
				}
			}
		}
	}

	/**
	 * Obtain the value for MiddleName(. OLifE.Party.PersonOrOrganization.Person.MiddleName is the middle name of the person
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.13I New method
	public void retrieveMiddleInitial(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				aNbaOinkRequest.addValue(NbaUtils.convertStringInProperCase(person.getMiddleName())); //AXAL3.7.13I
			} else {
				break;
			}
		}
	}

	/**
	 * Obtain the value for Holding.Policy.ApplicationInfo.HOUnderwriterName
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.13I New method
	public void retrieveUndwrtQueue(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo applicationInfo = getApplicationInfo();
		if (applicationInfo != null) {
			aNbaOinkRequest.addValue(applicationInfo.getHOUnderwriterName());
		}
	}

	/**
	 * Obtain the value for Perm Table Rating Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.PermTableRating
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.13I New method
	public void retrievePermLifeTableRatingText(NbaOinkRequest aNbaOinkRequest) {
		//begin ALS2385
		// CR1343973 code deleted
		int next = 0;		
		boolean resolved = false;
		// CR1343973 begin
		LifeParticipant lifeParticipant = null;  
		List lifeParticipants = getLifeParticipants(aNbaOinkRequest);
		while ((lifeParticipant = getNextLifeParticipant(lifeParticipants, next++)) != null) { // CR1343973 end
			if (lifeParticipant != null && !lifeParticipant.isActionDelete()) {
				int countSR = lifeParticipant.getSubstandardRatingCount();
				for (int j = 0; j < countSR && !resolved; j++) {
					SubstandardRating substandardRating = lifeParticipant.getSubstandardRatingAt(j);
					if (NbaUtils.isValidRating(substandardRating) && substandardRating.hasPermTableRating()) { //SPR2590
						aNbaOinkRequest.addValue(substandardRating.getPermTableRating(), NbaTableConstants.OLI_LU_RATINGS);
						resolved = true;
					}
				}
			}
			if (!resolved) {
				aNbaOinkRequest.addValue("");
			}
		}
		//end ALs2385
	}

	/**
	 * Obtain the vale for Party.Producer.CarrierAppointment.OLifEExtension.CarrierAppointmentExtension.ProducerDesignation
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.3M1 New method
	public void retrieveProducerDesignation(NbaOinkRequest aNbaOinkRequest) {
		NbaParty nbaParty = nbaTXLife.getWritingAgent();
		if (nbaParty != null) {
			Party party = nbaParty.getParty();
			if (party.hasProducer()) {
				Producer producer = party.getProducer();
				if (producer.getCarrierAppointment().size() > 0) {
					CarrierAppointment carrierAppointment = producer.getCarrierAppointmentAt(0);
					CarrierAppointmentExtension extension = NbaUtils.getFirstCarrierAppointmentExtension(carrierAppointment);
					if (extension != null) {
						aNbaOinkRequest.addValue(extension.getProducerDesignation());
						return;
					}
				}
			}
		}
		aNbaOinkRequest.addUnknownValue("");
	}

	/**
	 * Obtain the value for Reg60 Review. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.Reg60Review.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA231 new method
	public void retrieveReg60Review(NbaOinkRequest aNbaOinkRequest) {

		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null) {
					aNbaOinkRequest.addValue(oli.getApplicationInfoExtension().getReg60Review());
				}
			}
		}
	}

	/**
	 * Obtain the value for Reg60 PreSale Decision. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.Reg60PSDecision.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA231 new method
	public void retrieveReg60PSDecision(NbaOinkRequest aNbaOinkRequest) {

		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null) {
					aNbaOinkRequest.addValue(oli.getApplicationInfoExtension().getReg60PSDecision());
				}
			}
		}
	}

	/**
	 * Obtain the vale for Party.Producer.CarrierAppointment.OLifEExtension.CarrierAppointmentExtension.SalesRegion
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.20 New method
	public void retrieveSalesRegion(NbaOinkRequest aNbaOinkRequest) {
		// APSL3447 Begins
		Relation relation = null;
		if (NbaUtils.isHVTCase(getNbaTXLife())) {
			relation = getNbaTXLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_PROCESSINGFIRM);
		} else {
			relation = getNbaTXLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_GENAGENT);
		}
		// APSL3447 Ends
		if (relation != null) {
			NbaParty bgaParty = getNbaTXLife().getParty(relation.getRelatedObjectID());
			CarrierAppointment carrierAppointment = bgaParty.getParty().getProducer().getCarrierAppointmentAt(0);
			if (carrierAppointment != null) {
				CarrierAppointmentExtension extension = NbaUtils.getFirstCarrierAppointmentExtension(carrierAppointment);
				if (extension != null) {
					aNbaOinkRequest.addValue(extension.getSalesRegion());
					return;
				}
			}
		}
		aNbaOinkRequest.addUnknownValue("");
	}

	/**
	 * Obtain the vale for Policy.ApplicationInfo.ApplicationInfoExtension.ReopenDate
	 * @param aNbaOinkRequest
	 */
	//NBA254 New method
	public void retrieveReopenDate(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
		if (applicationInfoExtension != null) {
			aNbaOinkRequest.addValue(applicationInfoExtension.getReopenDate());
		}
	}

	/**
	 * Obtain the vale for Policy.ApplicationInfo.LastUnderwritingActivityDate
	 * @param aNbaOinkRequest
	 */
	//NBA254 New method
	public void retrieveInitialOfferDate(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo applicationInfo = getApplicationInfo();
		if (applicationInfo != null) {
			aNbaOinkRequest.addValue(applicationInfo.getLastUnderwritingActivityDate());
		}
	}

	/**
	 * Obtain the value of dispLevel. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.TentativeDisp.dispLevel is the disposition level
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA186 New Method
	public void retrieveDispLevel(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		String uwRole = aNbaOinkRequest.getQualifier();
		int qualifierLevel = Integer.parseInt(uwRole.substring(uwRole.length() - 1));
		if (api != null) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(api);
			if (appInfoExt != null) {
				int count = appInfoExt.getTentativeDispCount();
				for (int i = 0; i < count; i++) {
					TentativeDisp tentativeDisp = appInfoExt.getTentativeDispAt(i);
					if (qualifierLevel == tentativeDisp.getDispLevel()) {
						aNbaOinkRequest.addValue(tentativeDisp.getDispLevel());
						return;
					}
				}
			}
		}
	}

	/**
	 * Obtain the value of dispUndID OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.TentativeDisp.dispUndID is the disposition
	 * underwriter ID
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA186 New Method
	public void retrieveDispUndID(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		String uwRole = aNbaOinkRequest.getQualifier();
		int qualifierLevel = Integer.parseInt(uwRole.substring(uwRole.length() - 1));
		if (api != null) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(api);
			if (appInfoExt != null) {
				int count = appInfoExt.getTentativeDispCount();
				TentativeDisp tentativeDisp = null;
				for (int i = 0; i < count; i++) {
					tentativeDisp = appInfoExt.getTentativeDispAt(i);
					if (qualifierLevel == tentativeDisp.getDispLevel()) {
						aNbaOinkRequest.addValue(tentativeDisp.getDispUndID());
						return;
					}
				}
			}
		}
	}

	/**
	 * Obtain the value of disposition OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.TentativeDisp.disposition is the disposition
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA186 New Method
	public void retrieveDisposition(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		String uwRole = aNbaOinkRequest.getQualifier();
		int qualifierLevel = Integer.parseInt(uwRole.substring(uwRole.length() - 1));
		if (api != null) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(api);
			if (appInfoExt != null) {
				int count = appInfoExt.getTentativeDispCount();
				TentativeDisp tentativeDisp = null;
				for (int i = 0; i < count; i++) {
					tentativeDisp = appInfoExt.getTentativeDispAt(i);
					if (qualifierLevel == tentativeDisp.getDispLevel()) {
						aNbaOinkRequest.addValue(tentativeDisp.getDisposition());
						return;
					}
				}
			}
		}
	}

	/**
	 * Obtain the value of dispDate OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.TentativeDisp.dispDate is the disposition date
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA186 New Method
	public void retrieveDispDate(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		String uwRole = aNbaOinkRequest.getQualifier();
		int qualifierLevel = Integer.parseInt(uwRole.substring(uwRole.length() - 1));
		if (api != null) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(api);
			if (appInfoExt != null) {
				int count = appInfoExt.getTentativeDispCount();
				TentativeDisp tentativeDisp = null;
				for (int i = 0; i < count; i++) {
					tentativeDisp = appInfoExt.getTentativeDispAt(i);
					if (qualifierLevel == tentativeDisp.getDispLevel()) {
						aNbaOinkRequest.addValue(tentativeDisp.getDispDate());
						return;
					}
				}
			}
		}
	}

	/**
	 * Obtain the value of dispReason OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.TentativeDisp.dispReason is the disposition reason
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA186 New Method
	public void retrieveDispReason(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		String uwRole = aNbaOinkRequest.getQualifier();
		int qualifierLevel = Integer.parseInt(uwRole.substring(uwRole.length() - 1));
		if (api != null) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(api);
			if (appInfoExt != null) {
				int count = appInfoExt.getTentativeDispCount();
				TentativeDisp tentativeDisp = null;
				for (int i = 0; i < count; i++) {
					tentativeDisp = appInfoExt.getTentativeDispAt(i);
					if (qualifierLevel == tentativeDisp.getDispLevel()) {
						aNbaOinkRequest.addValue(tentativeDisp.getDispReason());
						return;
					}
				}
			}
		}
	}

	/**
	 * Obtain the value of decisionLevel OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.InitialDecision.decisionLevel is the decision
	 * level
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA186 New Method
	public void retrieveDecisionLevel(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		String uwRole = aNbaOinkRequest.getQualifier();
		int qualifierLevel = Integer.parseInt(uwRole.substring(uwRole.length() - 1));
		if (api != null) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(api);
			if (appInfoExt != null) {
				int count = appInfoExt.getInitialDecisionCount();
				InitialDecision initialDecision = null;
				for (int i = 0; i < count; i++) {
					initialDecision = appInfoExt.getInitialDecisionAt(i);
					if (qualifierLevel == initialDecision.getDecisionLevel()) {
						aNbaOinkRequest.addValue(initialDecision.getDecisionLevel());
						return;
					}
				}
			}
		}
	}

	/**
	 * Obtain the value of decisionUndID OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.InitialDecision.decisionUndID is the decision
	 * underwriter ID
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA186 New Method
	public void retrieveDecisionUndID(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		String uwRole = aNbaOinkRequest.getQualifier();
		int qualifierLevel = Integer.parseInt(uwRole.substring(uwRole.length() - 1));
		if (api != null) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(api);
			if (appInfoExt != null) {
				int count = appInfoExt.getInitialDecisionCount();
				InitialDecision initialDecision = null;
				for (int i = 0; i < count; i++) {
					initialDecision = appInfoExt.getInitialDecisionAt(i);
					if (qualifierLevel == initialDecision.getDecisionLevel()) {
						aNbaOinkRequest.addValue(initialDecision.getDecisionUndID());
						return;
					}
				}
			}
		}
	}

	/**
	 * Obtain the value of UWRole. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.InitialDecision.UWRole is the decision
	 * underwriter Role
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA186 New Method
	public void retrieveDecisionUndRole(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		String uwRole = aNbaOinkRequest.getQualifier();
		int qualifierLevel = Integer.parseInt(uwRole.substring(uwRole.length() - 1));
		if (api != null) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(api);
			if (appInfoExt != null) {
				int count = appInfoExt.getInitialDecisionCount();
				InitialDecision initialDecision = null;
				for (int i = 0; i < count; i++) {
					initialDecision = appInfoExt.getInitialDecisionAt(i);
					if (qualifierLevel == initialDecision.getDecisionLevel()) {
						aNbaOinkRequest.addValue(initialDecision.getUWRole());
						return;
					}
				}
			}
		}
	}

	/**
	 * Obtain the value of decisionDate OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.InitialDecision.decisionDate is the decision
	 * Date
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA186 New Method
	public void retrieveDecisionDate(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		String uwRole = aNbaOinkRequest.getQualifier();
		int qualifierLevel = Integer.parseInt(uwRole.substring(uwRole.length() - 1));
		if (api != null) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(api);
			if (appInfoExt != null) {
				int count = appInfoExt.getInitialDecisionCount();
				InitialDecision initialDecision = null;
				for (int i = 0; i < count; i++) {
					initialDecision = appInfoExt.getInitialDecisionAt(i);
					if (qualifierLevel == initialDecision.getDecisionLevel()) {
						aNbaOinkRequest.addValue(initialDecision.getDecisionDate());
						return;
					}
				}
			}
		}
	}

	/**
	 * Obtain the value of decision OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.InitialDecision.decision is the decision
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA186 New Method
	public void retrieveDecision(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		String uwRole = aNbaOinkRequest.getQualifier();
		int qualifierLevel = Integer.parseInt(uwRole.substring(uwRole.length() - 1));
		if (api != null) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(api);
			if (appInfoExt != null) {
				int count = appInfoExt.getInitialDecisionCount();
				InitialDecision initialDecision = null;
				for (int i = 0; i < count; i++) {
					initialDecision = appInfoExt.getInitialDecisionAt(i);
					if (qualifierLevel == initialDecision.getDecisionLevel()) {
						aNbaOinkRequest.addValue(initialDecision.getDecision());
						return;
					}
				}
			}
		}
	}

	/**
	 * Obtain the values for Substandard Extra Table Rating. For Table extras the value is SubstandardRating.PermTableRating or
	 * SubstandardRating.TempTableRating. For other extras, value is zero.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA186 New Method
	public void retrieveFirstExtraTableRating(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		LifeParticipant lifeParticipant;
		SubstandardRating substandardRating;
		long rating;
		String type;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			int lifeIdx = coverage.getLifeParticipantCount();
			for (int i = 0; i < lifeIdx; i++) {
				lifeParticipant = coverage.getLifeParticipantAt(i);
				if (participants.contains(lifeParticipant) && lifeParticipant.getLifeParticipantRoleCode() == NbaOliConstants.OLI_PARTICROLE_PRIMARY) {
					int ratIdx = lifeParticipant.getSubstandardRatingCount();
					for (int j = 0; j < ratIdx; j++) {
						substandardRating = lifeParticipant.getSubstandardRatingAt(j);
						if (NbaUtils.isValidRating(substandardRating)) {
							type = NbaUtils.getSubstandardRatingType(substandardRating);
							if (NbaConstants.SUB_STAND_TYPE_PERM_TABLE.equals(type)) {
								rating = substandardRating.getPermTableRating();
								aNbaOinkRequest.addValue(rating);
								return;
							} else if (NbaConstants.SUB_STAND_TYPE_TEMP_TABLE.equals(type)) {
								rating = substandardRating.getTempTableRating();
								aNbaOinkRequest.addValue(rating);
								return;
							}
						}
					}
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L);
		}
	}

	/**
	 * Obtain the values for First Substandard Extra Flat Amount. For Flat extras the value is SubstandardRating.TempFlatExtraAmt or
	 * SubstandardRating.SubstandardRatingExtension.PermFlatExtraAmt. For other extras, value is zero.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA186 New Method
	public void retrieveFirstExtraAmt(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest); //Coverages related to request
		List participants = getInsurableLifeParticipants(aNbaOinkRequest); //LifeParticipants related to request
		int next = 0;
		Coverage coverage;
		LifeParticipant lifeParticipant;
		SubstandardRating substandardRating;
		SubstandardRatingExtension ext;
		double amt;
		String type;
		while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
			int lifeIdx = coverage.getLifeParticipantCount();
			for (int i = 0; i < lifeIdx; i++) {
				lifeParticipant = coverage.getLifeParticipantAt(i);
				if (participants.contains(lifeParticipant) && lifeParticipant.getLifeParticipantRoleCode() == NbaOliConstants.OLI_PARTICROLE_PRIMARY) {
					int ratIdx = lifeParticipant.getSubstandardRatingCount();
					for (int j = 0; j < ratIdx; j++) {
						substandardRating = lifeParticipant.getSubstandardRatingAt(j);
						if (NbaUtils.isValidRating(substandardRating)) {
							type = NbaUtils.getSubstandardRatingType(substandardRating);
							if (NbaConstants.SUB_STAND_TYPE_PERM_FLAT.equals(type)) {
								ext = NbaUtils.getFirstSubstandardExtension(substandardRating);
								if (ext != null) {
									amt = ext.getPermFlatExtraAmt();
									aNbaOinkRequest.addValue(amt);
									return;
								}
							} else if (NbaConstants.SUB_STAND_TYPE_TEMP_FLAT.equals(type)) {
								amt = substandardRating.getTempFlatExtraAmt();
								aNbaOinkRequest.addValue(amt);
								return;
							}
						}
					}
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(0.0);
		}
	}

	/**
	 * @param aNbaOinkRequest
	 */
	public void retrieveIsPayorSameAsOwnerOrInsured(NbaOinkRequest aNbaOinkRequest) {
		Relation payerRelation = nbaTXLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_PAYER);
		Relation insuredRelation = nbaTXLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_INSURED);
		Relation ownerRelation = nbaTXLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_OWNER);
		if (payerRelation == null) {
			aNbaOinkRequest.addValue("false");
		} else if (insuredRelation != null && payerRelation.hasRelatedObjectID()
				&& payerRelation.getRelatedObjectID().equalsIgnoreCase(insuredRelation.getRelatedObjectID())) {
			aNbaOinkRequest.addValue("true");
		} else if (ownerRelation != null && payerRelation.hasRelatedObjectID()
				&& payerRelation.getRelatedObjectID().equalsIgnoreCase(ownerRelation.getRelatedObjectID())) {
			aNbaOinkRequest.addValue("true");
		} else {
			aNbaOinkRequest.addValue("false");
		}
	}

	/**
	 * Retrieve Life TotalRiskAmt values (from question 43 of application).
	 * @param aNbaOinkRequest
	 */
	//ALCP161 New method
	public void retrieveTotalRiskAmt(NbaOinkRequest aNbaOinkRequest) {
		Holding aHolding = null;
		Policy aPolicy = null;
		Life aLife = null;
		boolean added = false;//AXAL3.7.10C
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { //ALII1658
			Party party = getParty(aNbaOinkRequest, i); //ALII1658
			if (party != null) { //ALII1658
				List holdings =  NbaUtils.getReplacementHolding(nbaTXLife, party.getId()); //ALII1658
				for (int j = 0; j < holdings.size(); j++) { //ALII1658
					aHolding = (Holding) holdings.get(j); //ALII1658
					if (!aHolding.getId().equals(getHolding().getId())) {
						aPolicy = aHolding.getPolicy();
						if (aPolicy != null) {
							Object obj = aPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty$Contents();
							if (obj instanceof Life) {
								aLife = aPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
								aNbaOinkRequest.addValue(aLife.getTotalRiskAmt());
								added = true;//AXAL3.7.10C
							}
						}
					}
				}
			} //ALII1658
		} //ALII1658
		if(!added) {//AXAL3.7.10C
			aNbaOinkRequest.addValue(0);
		}
	}

	/**
	 * Retrieve ReplacedInd values (from question 43 of application).
	 * @param aNbaOinkRequest
	 */
	//ALCP161 New method
	public void retrieveReplacedInd(NbaOinkRequest aNbaOinkRequest) {
		Holding aHolding = null;
		Policy aPolicy = null;
		boolean replacedInd = false;
		//ALII1658 code deleted 
		int relationCnt = getOLifE().getRelationCount();

		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { //ALII1658
			Party party = getParty(aNbaOinkRequest, i); //ALII1658
			if (party != null) { //ALII1658
				List holdings =  NbaUtils.getReplacementHolding(nbaTXLife, party.getId()); //ALII1658
				for (int j = 0; j < holdings.size(); j++) { //ALII1658
					aHolding = (Holding) holdings.get(j); //ALII1658
					if (!aHolding.getId().equals(getHolding().getId())) {
						aPolicy = aHolding.getPolicy();
						if (aPolicy != null) {
							Object obj = aPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty$Contents();
							if (obj instanceof Life) {
								//for this Holding, determine if there is a Relation Holding to Holding that is a replacement type
								//Order of values added to Oink needed to match TotalRiskAmt values.
								replacedInd = false;
								for (int k = 0; k < relationCnt; k++) { //ALII1658
									Relation aRelation = getOLifE().getRelationAt(k); //ALII1658
									if (aRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_REPLACEDBY) {
										if (aRelation.getOriginatingObjectID().equals(getHolding().getId()) && 
												aRelation.getRelatedObjectID().equals(aHolding.getId())) {
											replacedInd = true;
											break;
										}
									}
								} // end for
								aNbaOinkRequest.addValue(replacedInd);
							}
						}
					}
				} //ALII1658
			} //ALII1658					
		} // end for
	}

	/**
	 * Retrieve HHFamilyInsurance AppliedForInsAmt values (from question 44 of application).
	 * @param aNbaOinkRequest
	 */
	//ALCP161 New method
	public void retrieveAppliedForInsAmt(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				Risk aRisk = getRisk(party);
				if (aRisk != null) {
					int hHcount = aRisk.getHHFamilyInsuranceCount();
					HHFamilyInsurance hHFamilyInsurance = null;
					for (int h = 0; h < hHcount; h++) {
						hHFamilyInsurance = aRisk.getHHFamilyInsuranceAt(h);
						aNbaOinkRequest.addValue(hHFamilyInsurance.getAppliedForInsAmt());
					}
				}
			}
		}
	}

	/**
	 * Return HHFamilyInsurance ReasonForOtherAppl (from question 44 of application)
	 * @param aNbaOinkRequest
	 */
	//ALCP161 New method
	public void retrieveReasonForOtherAppl(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				long aReason = 0L;
				Risk aRisk = getRisk(party);
				if (aRisk != null) {
					int hHcount = aRisk.getHHFamilyInsuranceCount();
					HHFamilyInsurance hHFamilyInsurance = null;
					for (int h = 0; h < hHcount; h++) {
						hHFamilyInsurance = aRisk.getHHFamilyInsuranceAt(h);

						int index_extension = getExtensionIndex(hHFamilyInsurance.getOLifEExtension(), HHFAMILYINSURANCE_EXTN);
						if (index_extension > -1) {
							OLifEExtension oli = hHFamilyInsurance.getOLifEExtensionAt(index_extension);
							aReason = oli.getHHFamilyInsuranceExtension().getReasonForOtherAppl();
							aNbaOinkRequest.addValue(aReason);
						}
					}
				}

			}
		}
	}

	/**
	 * Obtain the value for PremBalDue. OLifE.Holding.Policy.ApplicationInfo.PremBalDue
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.13 New method
	public void retrievePremBalDue(NbaOinkRequest aNbaOinkRequest) {
		if (getApplicationInfo() != null) {
			aNbaOinkRequest.addValue(getApplicationInfo().getPremBalDue());
		}
	}

	/**
	 * Obtain the value for UserLoginName . TXLife.UserAuthRequest.UserLoginName
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.13 New method
	public void retrieveUserLoginName(NbaOinkRequest aNbaOinkRequest) {
		UserAuthRequestAndTXLifeRequest userAuthReqAndTXLifeReq = getNbaTXLife().getTXLife().getUserAuthRequestAndTXLifeRequest();
		if (userAuthReqAndTXLifeReq != null) {
			UserAuthRequest userAuthReq = userAuthReqAndTXLifeReq.getUserAuthRequest();
			if (userAuthReq != null) {
				aNbaOinkRequest.addValue(userAuthReq.getUserLoginNameAndUserPswdOrUserSessionKey().getUserLoginNameAndUserPswd().getUserLoginName());
				return;
			}
		}
		aNbaOinkRequest.addUnknownValue("");
	}

	/**
	 * Obtain the value for UnderwritingResultReason . Holding.Policy.ApplicationInfo.OLifEExtension.ApplicationInfoExtension.UnderwritingResult.UnderwritingResultReason
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.13 New method
	public void retrieveAUDCode(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null) {
					List urList = new ArrayList();
					urList = oli.getApplicationInfoExtension().getUnderwritingResult();
					if (urList.size() > 0) {
						UnderwritingResult uwResult = oli.getApplicationInfoExtension().getUnderwritingResultAt(0);
						if (uwResult.hasUnderwritingResultReason()) {
							aNbaOinkRequest.addValue(uwResult.getUnderwritingResultReason());
							return;
						}
					}
				}
			}
		}
		aNbaOinkRequest.addUnknownValue("");
	}

	/**
	 * Obtain the value FirstYrPremDiscountAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//ALS2658 New method
	public void retrieveYRTAmt(NbaOinkRequest aNbaOinkRequest) {
		Policy policy = getPolicy();
		if (policy != null) {
			PolicyExtension policyextension = NbaUtils.getFirstPolicyExtension(policy);
			if (policyextension != null && policyextension.hasFirstYrPremDiscountAmt()) {
				aNbaOinkRequest.addValue(policyextension.getFirstYrPremDiscountAmt());
			} else {//ALS2730 begin
				aNbaOinkRequest.addValue(0);
			}//ALS2730 end
		}
	}

	/**
	 * Obtain the value for a Plan name. Holding.Policy.Life.Coverage.PlanName
	 * @param aNbaOinkRequest - data request container
	 * @deprecated - use CoverageProductCode (NBA093)
	 */
	//AXAL3.7.13
	public void retrievePlnTxtName(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getCoveragesOrRiders(aNbaOinkRequest);
		Coverage coverage = null;
		if (coverages != null) {
			int next = 0;
			while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
				if (coverage.hasLifeCovTypeCode()) {
					aNbaOinkRequest.addValue(coverage.getPlanName());
				} else {
					aNbaOinkRequest.addValue(-1);
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1);
		}
	}

	/**
	 * Obtain the value for a first UW result reason. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.UnderwritingResult(0).UnderwritingResultReason.
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.13, CR1455063 method refactored
	public void retrieveFirstUnderwritingResultReason(NbaOinkRequest aNbaOinkRequest) {
		boolean isSIApplication = getNbaTXLife().isSIApplication(); //APSL3282(QC12255)	
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
			if (applicationInfoExtension != null && applicationInfoExtension.getUnderwritingResult().size() > 0) {
				// APSL5175 :: AUD Letter Name Should be with MIB (AUD-AG) when MIB reason Available on Case
				// Iterator undResultItr = applicationInfoExtension.getUnderwritingResult().iterator();
				ArrayList urListtemp = applicationInfoExtension.getUnderwritingResult();
				List undResultList = NbaUtils.getUWReasonResultList(urListtemp);
				Iterator undResultItr = undResultList.iterator();
				// APSL 5175 :: END
				while (undResultItr.hasNext()) {
					UnderwritingResult uwResult = (UnderwritingResult) undResultItr.next();
					if ((uwResult.getRelatedObjectID() != null && uwResult.getRelatedObjectID().equalsIgnoreCase(party.getId()))
							|| (uwResult.getRelatedObjectID() == null && party.getId().equalsIgnoreCase(
									getNbaTXLife().getPartyId(NbaOliConstants.OLI_REL_INSURED)))) {
						//	APSL3159(QC11964) Underwriting result reason empty for SI referred case MDFRM. Send next valid underwriting result reason
						if(uwResult.hasUnderwritingResultReason()){ 
							long uwReasonTc = uwResult.getUnderwritingResultReason();
							/*APSL3282(QC12255)
							 * if Application is SI then send first decline underwriting result.
							 */
							if(isSIApplication){
								UnderwritingResultExtension uwResultExt = NbaUtils.getFirstUnderwritingResultExtension(uwResult);
								if(uwResultExt != null && uwResultExt.hasUnderwritingReasonType() && NbaOliConstants.OLI_UWREASON_EXT_FINAL_DISP == uwResultExt.getUnderwritingReasonType()){
									aNbaOinkRequest.addValue(uwReasonTc);
									break;
								} //APSL3282(QC12255) end
							}else{ //APSL3282(QC12255) 
								aNbaOinkRequest.addValue(uwReasonTc);
								break;
							} //APSL3282(QC12255) end
						}

					}
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1);
		}
	}

	/**
	 * Obtain the value of RequirementFollowUpList. OLifE.Holding.Policy.RequirementInfo.RequirementInfoExtension.RequirementFollowUpList
	 * @param aNbaOinkRequest - data request container
	 */
	//ALPC96 New Method
	public void retrieveRequirementFollowUpList(NbaOinkRequest aNbaOinkRequest) {
		String reqIdFilter = aNbaOinkRequest.getRequirementIdFilter();
		Policy policy = getPolicy();
		ArrayList reqInfoList = new ArrayList();
		int listSize = 0;
		RequirementInfo reqInfo = null;
		if (policy != null) {
			reqInfoList = policy.getRequirementInfo();
			listSize = reqInfoList.size();
			for (int i = 0; i < listSize; i++) {
				reqInfo = (RequirementInfo) reqInfoList.get(i);
				if (reqInfo != null && reqInfo.getId().equals(reqIdFilter)) {
					break;
				}
			}
			if (reqInfo != null) {
				int index_extension = getExtensionIndex(reqInfo.getOLifEExtension(), REQUIREMENTINFO_EXTN);
				if (index_extension != -1) {
					RequirementInfoExtension extension = reqInfo.getOLifEExtensionAt(index_extension).getRequirementInfoExtension();
					if (extension != null && extension.hasRequirementFollowUpList()) {//ALS4748
						String processedReqList = "";
						StringBuffer reqStr = new StringBuffer();
						StringTokenizer st = new StringTokenizer(extension.getRequirementFollowUpList(), ",");
						String requirement = "";
						while(st.hasMoreTokens()){
							// SR534920 Begin
							requirement = st.nextToken().trim();
							RequirementInfo followUpReqInfo = getNbaTXLife().getRequirementInfo(requirement);
							String reqType = followUpReqInfo != null ? String.valueOf(followUpReqInfo.getReqCode()) : requirement;							  
							if (followUpReqInfo != null && NbaUtils.isMissingDataRequirement(reqType)) {
								reqStr.append(followUpReqInfo.getRequirementDetails());
							} else {
								reqStr.append(NbaUtils.getRequirementTranslation(reqType, policy));
							}
							// SR534920 End
							reqStr.append(", ");
						}
						if(reqStr.length()>0){ //If there is any requirement in the list
							processedReqList = reqStr.substring(0,reqStr.length()-2); //Remove last comma and space from the list
						}
						aNbaOinkRequest.addValue(processedReqList);
					}
				}
			}
		}
	}
	

	/**
	 * Obtain the value of PendingRequirementList. Iterate on the requirement list to find out requirements that restrict issue and are pending.
	 * @param aNbaOinkRequest - data request container
	 */
	//ALPC96 New Method
	public void retrievePendingRequirementList(NbaOinkRequest aNbaOinkRequest) {
		Policy policy = getPolicy();
		//begin PERF-APSL308
		List reqList = NbaUtils.getPendingRequirementList(policy);
		StringBuffer reqCodeBuffer = new StringBuffer();
		 for(int i=0; i < reqList.size(); i++){
		 	if (i>0) {
		 		reqCodeBuffer.append(", ");
		 	}
		 	reqCodeBuffer.append((String)reqList.get(i));		 	
		 }
		 //end PERF-APSL308
		aNbaOinkRequest.addValue(reqCodeBuffer.toString());
	}

	/**
	 * Obtain the value for a money refunded by the Final disposition process otherwise return zero.
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.13
	public void retrieveNTORefundMoney(NbaOinkRequest aNbaOinkRequest) {
		int finCount = getPolicy().getFinancialActivityCount();
		double totalRefundMoney = 0.0;
		for (int i = 0; i < finCount; i++) {
			FinancialActivity activity = getFinancialActivity(i);
			if  ( activity.getFinActivitySubType()== NbaOliConstants.OLI_FINACTSUB_REFUND && activity.getUserCode().equals(NbaConstants.PROC_FINAL_DISPOSITION)){
				Payment payment = activity.getPaymentAt(0);
				if (payment != null) {
					totalRefundMoney += payment.getPaymentAmt();
				}
			}
		}
		aNbaOinkRequest.addValue(totalRefundMoney);
	}

	//ALPC165 new method added.
	public void retrieveFeeAmtForPayMode(NbaOinkRequest aNbaOinkRequest) {
		long paymentMode = getPolicy().getPaymentMode();
		if (!NbaUtils.isNull(paymentMode)) {
			Iterator paymentFeesItr = NbaUtils.getFirstPolicyExtension(getPolicy()).getPaymentFees().iterator();
			while (paymentFeesItr.hasNext()) {
				PaymentFees paymentFees = (PaymentFees) paymentFeesItr.next();
				if (paymentFees.getFeeMode() == paymentMode && paymentFees.hasFeeAmt()) {
					aNbaOinkRequest.addValue(paymentFees.getFeeAmt());
					break;
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(0);
		}
	}

	//ALPC165 new method added.
	public void retrieveBestClassModalPremAmt(NbaOinkRequest aNbaOinkRequest) {
		PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(getPolicy());
		if (polExt != null && polExt.hasBestClassModalPremAmt()) {
			aNbaOinkRequest.addValue(polExt.getBestClassModalPremAmt());
		} else {
			aNbaOinkRequest.addValue(0);
		}
	}

//	ALPC96 New Method APSL5164
	public void retrieveAmendmentEndorsementCode(NbaOinkRequest aNbaOinkRequest) {
		String endrsmntListStr = amendmentEndorsementCodeInd();
		aNbaOinkRequest.addValue(endrsmntListStr);
	}

	//AXAL3.7.13 new method
	public void retrieveLastStatusChangeDate(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo applicationInfo = getApplicationInfo();
		if (applicationInfo != null) {
			aNbaOinkRequest.addValue(applicationInfo.getHOCompletionDate());
		} else {
			aNbaOinkRequest.addValue(0);
		}
	}

	/**
	 * Obtain the value for UnderwritingResultReason . Holding.Policy.ApplicationInfo.OLifEExtension.ApplicationInfoExtension.UnderwritingResult.Description+SupplementalText (if any)
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.13 New method
	//ALPC195 - Deleted method
	//public void retrieveAUDReason(NbaOinkRequest aNbaOinkRequest) {

	/**
	 * @param aNbaOinkRequest
	 */
	//ALPC075 New Method
	public void retrieveDPWPresent(NbaOinkRequest aNbaOinkRequest) {
		Life life = getNbaTXLife().getLife(); //check if DPW is present on case - if present throw msg
		int covCount = life.getCoverageCount();
		for (int j = 0; j < covCount; j++) {
			Coverage coverage = life.getCoverageAt(j);
			List covOptCount = coverage.getCovOption();
			for (int k = 0; k < covOptCount.size(); k++) {
				CovOption covOption = coverage.getCovOptionAt(k);
				if (covOption.getLifeCovOptTypeCode() == NbaOliConstants.OLI_COVTYPE_DREADDISEASE) {
					if (!NbaUtils.isDeleted(covOption) && NbaOliConstants.OLI_POLSTAT_DECISSUE != covOption.getCovOptionStatus()) {
						aNbaOinkRequest.addValue(true);
					} else {
						aNbaOinkRequest.addValue(false);
					}
				}
			}
		}
	}

	/**
	 * Obtain the value for Policy status.Holding.Policy.PolicyStatus
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.26 new method added.
	public void retrievePolStatus(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(getPolicy().getPolicyStatus());
	}

	/**
	 * Determine if an APS requirement is on the contract
	 * @param aNbaOinkRequest - data request container
	 */
	// ALCP153 New Method
	public void retrieveAPSInd(NbaOinkRequest aNbaOinkRequest) {
		boolean APSInd = false;
		RequirementInfo reqInfo = null;
		Policy policy = getPolicy();
		int count = policy.getRequirementInfoCount();
		for (int i = 0; i < count; i++) {
			reqInfo = policy.getRequirementInfoAt(i);
			if (reqInfo != null) {
				if (reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_PHYSSTMT) {
					APSInd = true;
				}
			}
		}
		aNbaOinkRequest.addValue(APSInd);
	}

	//ALPC168 new method
	public void retrieveSpecialCase(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
		if (applicationInfoExtension != null && applicationInfoExtension.hasSpecialCase()) {
			aNbaOinkRequest.addValue(applicationInfoExtension.getSpecialCase());
			return;
		}
		aNbaOinkRequest.addValue(-1);
	}

	//ALPC168 new method
	public void retrieveNIGOInd(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
		if (applicationInfoExtension != null && applicationInfoExtension.hasNIGOInd()) {
			aNbaOinkRequest.addValue(applicationInfoExtension.getNIGOInd());
			return;
		}
		aNbaOinkRequest.addValue(false);
	}

	/**
	 * Obtain the value for PayUpDate. Holding.Policy.Life.Coverage.PayUpDate
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.14 New Method
	public void retrievePayUpDate(NbaOinkRequest aNbaOinkRequest) {
		CovOption covOption = null;
		String qualifier = aNbaOinkRequest.getQualifier();

		if (BENEFIT.equals(qualifier)) {
			List covoptions = getCovOptions();
			if (covoptions != null) {
				int count = covoptions.size();
				for (int i = 0; i < count; i++) {
					covOption = (CovOption) covoptions.get(i);
					CovOptionExtension covOptionExt = NbaUtils.getFirstCovOptionExtension(covOption);
					if (covOptionExt != null && covOptionExt.hasPayUpDate()) {
						aNbaOinkRequest.addValue(covOptionExt.getPayUpDate());
					} else {
						aNbaOinkRequest.addUnknownValue((Date) null);
					}
				}
			}
		} else {
			List coverages = getCoveragesOrRiders(aNbaOinkRequest);
			int next = 0;
			Coverage coverage;
			while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
				CoverageExtension coverageExt = NbaUtils.getFirstCoverageExtension(coverage);
				if (coverageExt.hasPayUpDate()) {
					aNbaOinkRequest.addValue(coverageExt.getPayUpDate());
				} else {
					aNbaOinkRequest.addUnknownValue((Date) null);
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addUnknownValue((Date) null);
		}
	}
	
	/**
	 * Sets the value as "1" (true) if it is an internal replacement else sets the value as "0" (false)
	 * @param aNbaOinkRequest - data request container
	 */
	//ALPC119 New Method
	public void retrieveInternalReplacementInd(NbaOinkRequest aNbaOinkRequest) {
		boolean internalReplacementInd = false;
		if(NbaOliConstants.OLI_REPTY_INTERNAL == getPolicy().getReplacementType()){
			internalReplacementInd = true;
		}
		aNbaOinkRequest.addValue(internalReplacementInd);
	}
	
	/**
	 * OINKs scan station
	 * @param aNbaOinkRequest
	 */
	//ALS3977 New Method
	public void retrieveScanStation(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfoExtension appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
		if (appInfoExtn != null) {
			aNbaOinkRequest.addValue(appInfoExtn.getScanStation());
		}
	}
	/**
	 * Obtain the value for BillNumber. OLifE.Holding.Policy.BillNumber.
	 * @param aNbaOinkRequest - data request container
	 */
	//ALPC234 new method
	public void retrieveBillNumber(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(getPolicy().getBillNumber());
	}
	
	/**
	 * @param aNbaOinkRequest
	 */
	//ALS4450,ALS4451,ALS4452,ALS4453 New Method
	public void retrieveCTIRPresent(NbaOinkRequest aNbaOinkRequest) {
		Life life = getNbaTXLife().getLife();
		int covCount = life.getCoverageCount();
		for (int j = 0; j < covCount; j++) {
			Coverage coverage = life.getCoverageAt(j);
			if (NbaOliConstants.OLI_COVTYPE_CHILDTERM == coverage.getLifeCovTypeCode()) {
				if (!NbaUtils.isDeleted(coverage) && coverage.getLifeCovStatus() != NbaOliConstants.OLI_POLSTAT_DECISSUE) {
					aNbaOinkRequest.addValue(true);
					break;
				} else {
					aNbaOinkRequest.addValue(false);
					break;
				}
			}
		}
	}
	
	/**
	 * Obtain the value for OLifE.Party.Client.OLifEExtension.ClientExtension.AgentRelationshipDescription 
	 * @param aNbaOinkRequest - data request container
	 */
	//ALS4533 New Method
	public void retrieveAgentRelationshipDescription(NbaOinkRequest aNbaOinkRequest) {
		String description = "";
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Client client = aParty.getClient();
				if (client != null) {
					ClientExtension clientExtension = NbaUtils.getClientExtension(client);
					if (clientExtension != null) {
						if (clientExtension.hasAgentRelationshipDescription()) {
							description = String.valueOf(clientExtension.getAgentRelationshipDescription());
						}
					}
				}
			}
			aNbaOinkRequest.addValue(description);
		}
	}
	
	/**
	 * Obtain the boolean indicating if there are any has Significant unoverridden Contract Validation errors.
	 * @param aNbaOinkRequest
	 */
	// ALS4747 New Method
	public void retrieveSignificantCVErrorInd(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(getNbaTXLife().hasSignificantValidationErrors());
	}
	
	/**
	 * Obtain the vale for OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.InformalOfferDate
	 * @param aNbaOinkRequest
	 */
	// ALS5041 New Method
	public void retrieveInformalOfferDate(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo applicationInfo = getApplicationInfo();
		if (applicationInfo != null) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
			if (appInfoExt != null) {
				aNbaOinkRequest.addValue(appInfoExt.getInformalOfferDate());
			}
		}
	}
	/**
	 * Obtain the vale for OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.SpecialInstructionInd
	 * @param aNbaOinkRequest
	 */
	// ALS3826 New Method
	public void retrieveSpecialInstructionInd(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo applicationInfo = getApplicationInfo();
		if (applicationInfo != null) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
			if (appInfoExt != null) {
				aNbaOinkRequest.addValue(appInfoExt.getSpecialInstructionInd());
			}
		}
	}	
	/**
	 * Obtain the value for UnboundInd. OLifE.Holding.Policy.PolicyExtension.UnboundInd
	 * @param aNbaOinkRequest
	 */
	//ALPC234 new method
	public void retrieveUnboundInd(NbaOinkRequest aNbaOinkRequest) {
		
		Policy policy = getPolicy();
		if (policy != null) {
			PolicyExtension policyextension = NbaUtils.getFirstPolicyExtension(policy);
			if (policyextension != null && policyextension.hasUnboundInd()) {
				aNbaOinkRequest.addValue(policyextension.getUnboundInd());
			}
		}
	}
	/**
	 * Obtain the value for ContractChangeReprintDate. OLifE.Holding.Policy.PolicyExtension.ContractChangeReprintDate
	 * @param aNbaOinkRequest
	 */
	//ALPC234 new method
	public void retrieveContractChangeReprintDate(NbaOinkRequest aNbaOinkRequest) {
		
		Policy policy = getPolicy();
		if (policy != null) {
			PolicyExtension policyextension = NbaUtils.getFirstPolicyExtension(policy);
			if (policyextension != null && policyextension.hasContractChangeReprintDate()) {
				aNbaOinkRequest.addValue(policyextension.getContractChangeReprintDate());
			}
		}
	}
	/**
	 * Obtain the value for ContractChangeReprintInd. OLifE.Holding.Policy.PolicyExtension.ContractChangeReprintInd
	 * @param aNbaOinkRequest
	 */
	//ALPC234 new method
	public void retrieveContractChangeReprintInd(NbaOinkRequest aNbaOinkRequest) {
		
		Policy policy = getPolicy();
		if (policy != null) {
			PolicyExtension policyextension = NbaUtils.getFirstPolicyExtension(policy);
			if (policyextension != null && policyextension.hasContractChangeReprintInd()) {
				aNbaOinkRequest.addValue(policyextension.getContractChangeReprintInd());
			}
		}
	}
	/**
	 * Obtain the value for RequirementInfo.ReceivedDate
	 * @param aNbaOinkRequest - data request container
	 */
	//ALPC234 New Method
	public void retrieveDeliveryReceivedDate(NbaOinkRequest aNbaOinkRequest) {
		Date deliverySignDate = null;
		List requirementInfos = getNbaTXLife().getPolicy().getRequirementInfo();
		for (int i = 0; requirementInfos != null && i < requirementInfos.size(); i++) {
			RequirementInfo requirementInfo = getNbaTXLife().getPolicy().getRequirementInfoAt(i);
			if (requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_POLDELRECEIPT
					&& requirementInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_RECEIVED) {
				if (requirementInfo.hasReceivedDate() && 
						(null == deliverySignDate || 
								(null != deliverySignDate && requirementInfo.getReceivedDate().after(deliverySignDate)))) {
					deliverySignDate = requirementInfo.getReceivedDate();
				}
			}
		}
		if (null != deliverySignDate) {
			aNbaOinkRequest.addValue(deliverySignDate);
		} else {
			aNbaOinkRequest.addUnknownValue((Date) null);
		}

	}
	
	//ALS5687
	public void retrieveIssuedAsAppliedInd(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = getBaseCoverage();
		if (null != coverage) {
			aNbaOinkRequest.addValue(coverage.getIssuedAsAppliedInd());
		}
	}
	//ALS5710 New Method
	public void retrieveBillingControlEffectiveDate(NbaOinkRequest aNbaOinkRequest) {
		Date billingControlEffDate = null;
		int index_Extension = getExtensionIndex(getPolicy().getOLifEExtension(), POLICY_EXTN);
		if (index_Extension != -1) {
			PolicyExtension extension = getPolicy().getOLifEExtensionAt(index_Extension).getPolicyExtension();
			if (extension != null) {
				billingControlEffDate = extension.getBillingControlEffectiveDate();
			}
		}
		if (null != billingControlEffDate) {
			aNbaOinkRequest.addValue(billingControlEffDate);
		} else {
			aNbaOinkRequest.addUnknownValue((Date) null);
		}
	}
	
	//New Method AXAL3.7.32
	public void retrieveAPSDoctorName(NbaOinkRequest aNbaOinkRequest) {
		List relations = getOLifE().getRelation();
		StringBuffer doctorNames = new StringBuffer();
		for(int i = 0;i<relations.size();i++) {
			Relation relation = (Relation) relations.get(i);
			if(relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_PHYSICIAN) {
				String partyId = relation.getRelatedObjectID();
				NbaParty party = getNbaTXLife().getParty(partyId);
				if(party != null) {
					doctorNames.append(party.getFullName() + "#");	
				}
			}
		}
		if(doctorNames.length()>0) {
			if(doctorNames.indexOf("#") > -1) {
				aNbaOinkRequest.addValue(doctorNames.toString().substring(0,doctorNames.length()-2));//Removing the last '#' from the value
			}else{
				aNbaOinkRequest.addValue(doctorNames.toString());
			}
		}
	}
	
	//New Method AXAL3.7.32
	public void retrieveEmpLine1(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { // CR1343973 begin
			Party party = getParty(aNbaOinkRequest, i); 				
			NbaParty employer = getNbaTXLife().getEmployer(party.getId()); // CR1343973 end 
			// CR1343973 code deleted
			if(employer != null) {
				Address address = employer.getParty().getAddressAt(0);
				if (address != null && address.hasLine1()) {
					aNbaOinkRequest.addValue(address.getLine1());
				}	
			}
		}
	}
	
	//New Method AXAL3.7.32
	public void retrieveEmpLine2(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { // CR1343973 begin
			Party party = getParty(aNbaOinkRequest, i); 				
			NbaParty employer = getNbaTXLife().getEmployer(party.getId()); // CR1343973 end
			// CR1343973 code deleted
			if(employer != null) {
				Address address = employer.getParty().getAddressAt(0);
				if (address != null && address.hasLine2()) {
					aNbaOinkRequest.addValue(address.getLine2());
				}	
			}
		}
	}
	
	//New Method AXAL3.7.32
	public void retrieveEmpLine3(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { // CR1343973 begin
			Party party = getParty(aNbaOinkRequest, i); 				
			NbaParty employer = getNbaTXLife().getEmployer(party.getId()); // CR1343973 end
			// CR1343973 code deleted
			if(employer != null) {
				Address address = employer.getParty().getAddressAt(0);
				if (address != null && address.hasLine3()) {
					aNbaOinkRequest.addValue(address.getLine3());
				}	
			}
		}
	}
	
	//New Method AXAL3.7.32
	public void retrieveEmpCity(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { // CR1343973 begin
			Party party = getParty(aNbaOinkRequest, i); 				
			NbaParty employer = getNbaTXLife().getEmployer(party.getId()); // CR1343973 end
			// CR1343973 code deleted
			if(employer != null) {
				Address address = employer.getParty().getAddressAt(0);
				if (address != null && address.hasCity()) {
					aNbaOinkRequest.addValue(address.getCity());
				}	
			}
		}
	}
	
	//New Method AXAL3.7.32
	public void retrieveEmpState(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { // CR1343973 begin
			Party party = getParty(aNbaOinkRequest, i); 				
			NbaParty employer = getNbaTXLife().getEmployer(party.getId()); // CR1343973 end
			// CR1343973 code deleted
			if(employer != null) {
				Address address = employer.getParty().getAddressAt(0);
				if (address != null && address.hasAddressStateTC()) {   
					aNbaOinkRequest.addValue(address.getAddressStateTC(), NbaTableConstants.NBA_STATES); //ALII2014
				} else {
					aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_STATES);
				}
				
			}
		}
	}
	
	//New Method AXAL3.7.32
	public void retrieveEmpZip(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) { // CR1343973 begin
			Party party = getParty(aNbaOinkRequest, i); 				
			NbaParty employer = getNbaTXLife().getEmployer(party.getId()); // CR1343973 end
			// CR1343973 code deleted
			if(employer != null) {
				Address address = employer.getParty().getAddressAt(0);
				if (address != null && address.hasZip()) {
					aNbaOinkRequest.addValue(address.getZip());
				}	
			}
		}
	}

	//New Method P2AXAL028
	public void retrieveExchange1035Ind(NbaOinkRequest aNbaOinkRequest) {
		if(getNbaTXLife() != null){
				aNbaOinkRequest.addValue(getNbaTXLife().is1035Exchange());
		}
	}
	
	/**
	 * Obtain the value for FullNames. OLifE.Party.FullName - When on Party, Client applications should treat FullName as Read-only when
	 * Party.Type='Person' and read/write for all other types. In the case where it is Read-Only, the server will construct the FullName property, and
	 * update it immediately whenever one of those dependent properties is set. When Party.Type='Person', Fullname is formatted '%L, %F %M, %S' where
	 * %L is LastName, %F is FirstName, %M is MiddleName and %S is Suffix.
	 * @param aNbaOinkRequest - data request container
	 */
	//New Method P2AXAL028
	public void retrieveFullNameX(NbaOinkRequest aNbaOinkRequest) {
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		Party party = null;
		String fullName;
		aNbaOinkRequest.setParseMultiple(true);
		for (int i = 0; i < partyList.size() ; i++) {
			//ALII127 Code Moved
			party = (Party)partyList.get(i);
			if (party != null) {
				fullName = party.getFullName();
				if (fullName == null || fullName.length() < 1) {
					Object obj = party.getPersonOrOrganization$Contents();
					if ((obj != null) && (obj instanceof Person)) {
						Person aPerson = party.getPersonOrOrganization().getPerson();
						StringBuffer sb = new StringBuffer();
						if (aPerson.hasLastName()) {
							sb.append(NbaUtils.convertStringInProperCase(aPerson.getLastName()));
							sb.append(", ");
						}
						if (aPerson.hasFirstName()) {
							sb.append(NbaUtils.convertStringInProperCase(aPerson.getFirstName()));
							sb.append(" ");
						}
						if (aPerson.hasMiddleName()) {
							sb.append(NbaUtils.convertStringInProperCase(aPerson.getMiddleName()));
							sb.append(" ");
						}
						if (aPerson.hasSuffix() && aPerson.getSuffix().length() > 0) {
							sb.append(", ");
							sb.append(aPerson.getSuffix());
						}
						fullName = sb.toString();
					}
				}
			} else {
				break;
			}
			aNbaOinkRequest.addValue(fullName);
		}
	}
	//New Method P2AXAL028
	public void retrieveCompanyIDX(NbaOinkRequest aNbaOinkRequest) {
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		Party party = null;
		aNbaOinkRequest.setParseMultiple(true);
		for (int i = 0; i < partyList.size() ; i++) {
			//ALII127 code moved
			party = (Party)partyList.get(i);
			if (party != null && party.hasPartyKey()) {
				aNbaOinkRequest.addValue(party.getPartyKey());
			}
		}
	}
	
    //New Method P2AXAL028
	public void retrieveConvPolicyLostIndCodeX(NbaOinkRequest aNbaOinkRequest){
		ArrayList holdingList = getOLifE().getHolding();
		Policy policy = null;
		aNbaOinkRequest.setParseMultiple(true);
		for(int i=0; i < holdingList.size(); i++){
			//ALII127 code moved
			policy = ((Holding) holdingList.get(i)).getPolicy();
			if (policy != null) {
				PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(policy);
				if (policyExtension != null && policyExtension.hasConvPolicyLostIndCode()) {
					aNbaOinkRequest.addValue(policyExtension.getConvPolicyLostIndCode());
				}
			}
		}
	}
	
    //New Method P2AXAL028
	public void retrieveConvPolicyAttachedIndCodeX(NbaOinkRequest aNbaOinkRequest) {
		ArrayList holdingList = getOLifE().getHolding();
		Policy policy = null;
		aNbaOinkRequest.setParseMultiple(true);
		for (int i = 0; i < holdingList.size(); i++) {
			//ALII127 code moved
			policy = ((Holding) holdingList.get(i)).getPolicy();
			if (policy != null) {
				PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(policy);
				if (policyExtension != null && policyExtension.hasConvPolicyAttachedIndCode()) {
					aNbaOinkRequest.addValue(policyExtension.getConvPolicyAttachedIndCode());
				}
			}
		}
	}

    //New Method P2AXAL028
	public void retrieveNumParty(NbaOinkRequest aNbaOinkRequest){
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		aNbaOinkRequest.addValue(partyList.size());
	}
	
	
	//New Method P2AXAL005
	public void retrieveLegalStopType(NbaOinkRequest aNbaOinkRequest) {
		long legalstoptype = 0;
		int index_Extension = getExtensionIndex(getPolicy().getOLifEExtension(), POLICY_EXTN);
		if (index_Extension != -1) {
			OLifEExtension oLifEExtension = getPolicy().getOLifEExtensionAt(index_Extension);
			if (oLifEExtension != null && oLifEExtension.getPolicyExtension().hasLegalStopType()) {
				legalstoptype = oLifEExtension.getPolicyExtension().getLegalStopType();
			}
		}
		aNbaOinkRequest.addValue(legalstoptype);
	}
	/**
	 * Obtain the values for ReqCode of all the received requirements. OLifE.Holding.Policy.RequirementInfo.ReqCode is the code specifying the underwriting requirement.
	 * @param aNbaOinkRequest - data request container
	 */
	//New method P2AXAL028
	public void retrieveReqReceivedX(NbaOinkRequest aNbaOinkRequest) {
		List reqList = getRequirementInfos(aNbaOinkRequest);
		aNbaOinkRequest.setParseMultiple(true);
		for (int i = 0; i < reqList.size(); i++) {
			//ALII127 Code Moved
			RequirementInfo aRequirementInfo = getRequirementInfo(reqList, i); 
			if (aRequirementInfo != null && aRequirementInfo.getReqStatus()== NbaOliConstants.OLI_REQSTAT_RECEIVED) {
				aNbaOinkRequest.addValue(aRequirementInfo.getReqCode(), NbaTableConstants.NBA_REQUIREMENTS);
			}
		}
	}

	//New Method P2AXAL036
	public void retrieveVolumeSharePct(NbaOinkRequest aNbaOinkRequest) {
		Relation primAgntRel = getNbaTXLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_PRIMAGENT);
		if (primAgntRel != null) {
			aNbaOinkRequest.addValue(primAgntRel.getVolumeSharePct());
		}
	}
   //NA_AXAL004
	public void retrieveHOAppFormNumber(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo applicationInfo = getApplicationInfo();
		if (applicationInfo != null) {
			aNbaOinkRequest.addValue(applicationInfo.getHOAppFormNumber());			
		}
	}
   //AXAL3.7.10B New Method
	public void retrieveCededAmtExceeded(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(NbaUtils.isCededAmtExceeded(getNbaTXLife())? NbaConstants.TRUE_STR : NbaConstants.FALSE_STR);
	}

	//AXAL3.7.10B New Method
	public void retrieveTotalCededAmtExceeded(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(NbaUtils.isTotalCededAmtExceeded(getNbaTXLife()) ? NbaConstants.TRUE_STR : NbaConstants.FALSE_STR);
	}
	// CR58636 New Method ADC Retrofit
	public void retrieveNigoAdcReasons(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		if(getOLifE().getActivity().size()>0)
		{
		Activity activity = getOLifE().getActivityAt(0);
		if (activity != null) {
			OLifEExtension oli = null;
			int index_extension = getExtensionIndex(activity.getOLifEExtension(), ACTIVITY_EXTN);
			if (index_extension != -1) {
				oli = activity.getOLifEExtensionAt(index_extension);
			} else {
				aNbaOinkRequest.addValue("0");
			}
			if (oli != null && oli.getActivityExtension() != null && oli.getActivityExtension().getReason() != null) {
				NbaTableAccessor nta = new NbaTableAccessor();
				Map tableData = new java.util.HashMap();
				tableData.put(NbaTableAccessConstants.C_COVERAGE_KEY, NbaTableAccessConstants.WILDCARD);
				tableData.put(NbaTableAccessConstants.C_COMPANY_CODE, NbaTableAccessConstants.WILDCARD);  
				tableData.put(NbaTableAccessConstants.C_SYSTEM_ID, NbaTableAccessConstants.WILDCARD);
				tableData.put(NbaTableAccessConstants.C_REASON_TYPE,oli.getActivityExtension().getReasonCategory());
				tableData.put(NbaTableAccessConstants.C_INDEX_VALUE,oli.getActivityExtension().getReason());
				NbaReasonsData data =(NbaReasonsData)nta.getDataForOlifeValue(tableData, NbaTableConstants.NBA_REASONS,oli.getActivityExtension().getReason());
				String value="";
				if(data!=null)
				{
					if(data.getIndexTranslation().equalsIgnoreCase("OTHER"))
						{
						value=oli.getActivityExtension().getDescription();
						}
					else
						{
						value=data.getIndexTranslation();
						}
				}
				aNbaOinkRequest.addValue(value);
				
			}
			
		}
		}
	}
	
	// CR58636 New Method ADC Retrofit
	public void retrieveReasonCategory(NbaOinkRequest aNbaOinkRequest) {
		if(getOLifE().getActivity().size()>0)
		{
			Activity activity = getOLifE().getActivityAt(0);
			if (activity != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(activity.getOLifEExtension(), ACTIVITY_EXTN);
				if (index_extension != -1) {
					oli = activity.getOLifEExtensionAt(index_extension);
				} else {
					aNbaOinkRequest.addValue("0");
				}
				if (oli != null && oli.getActivityExtension() != null && oli.getActivityExtension().getReasonCategory() != null) {
					aNbaOinkRequest.addValue(oli.getActivityExtension().getReasonCategory());
				}
				
			}
		}
	}
	
	// CR58636 New Method ADC Retrofit
	public void retrieveMailByDate(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null) {
					aNbaOinkRequest.addValue(oli.getApplicationInfoExtension().getMailedByDate());
					return;
				}
			}
		}
		aNbaOinkRequest.addValue(false); 
	}

	/**
	 * Obtain all the values for IndicatorCode. OLifE.Holding.Policy.Life.Coverage.IndicatorCode is the code Coverage classication - e.g. base, rider,
	 * etc.
	 * @param aNbaOinkRequest - data request container
	 */
	// P2AXAL028 New Method
	public void retrieveIndicatorCodeX(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = null;
		Life life = getLife();
		aNbaOinkRequest.setParseMultiple(true);
		if (life != null) {
			int countCoverage = life.getCoverageCount();
			for (int i = 0; i < countCoverage; i++) {
				//ALII127 Code Moved
				coverage = life.getCoverageAt(i);
				if (coverage != null && coverage.hasIndicatorCode()) {
					aNbaOinkRequest.addValue(coverage.getIndicatorCode());
				}
			}
		}
	}

	/**
	 * Obtain the value for LifeCovTypeCode. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.Coverage.LifeCovTypeCode is the type of
	 * coverage.
	 * @param aNbaOinkRequest - data request container
	 */
//	 P2AXAL028 New Method
	public void retrieveLifeCovTypeCodeX(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = null;
		Life life = getLife();
		aNbaOinkRequest.setParseMultiple(true);
		if (life != null) {
			int countCoverage = life.getCoverageCount();
			for (int i = 0; i < countCoverage; i++) {
				//ALII127 Code Moved
				coverage = life.getCoverageAt(i);
				if (coverage.hasLifeCovTypeCode()) {
					aNbaOinkRequest.addValue(coverage.getLifeCovTypeCode());
				} else {
					aNbaOinkRequest.addValue(-1L);//ALII483
				}
			}
		}
	}

	
	/**
	 * Obtain the value for a Coverage ProductCode. Holding.Policy.Life.Coverage.ProductCode is a productCode.
	 * Holding.Policy.Life.Coverage.CovOption.ProductCode is a productCode.
	 * @param aNbaOinkRequest - data request container
	 */
	//P2AXAL028 new method
	public void retrieveCoverageProductCodeX(NbaOinkRequest aNbaOinkRequest) {
		Life aLife = getLife();
		if (aLife == null) {
			retrievePolicyProductCode(aNbaOinkRequest);
			return;
		}
		int count = 0;
		Coverage coverage = null;
		CovOption covOption = null;
		String coverageType = aNbaOinkRequest.getQualifier();
		if (coverageType.equals(BENEFIT) || coverageType.equals(ACCIDENTAL_DEATH_BENEFIT)) {
			List covoptions = null;
			if (coverageType.equals(ACCIDENTAL_DEATH_BENEFIT)) {
				covoptions = getCovOptions(NbaOliConstants.OLI_OPTTYPE_ADB);
			} else {
				covoptions = getCovOptions();
			}
			if (covoptions != null) {
				count = covoptions.size();
				for (int i = 0; i < count; i++) {
					covOption = (CovOption) covoptions.get(i);
					if (covOption != null) {
						aNbaOinkRequest.addValue(covOption.getProductCode(), NbaTableConstants.NBA_PLANS);
					}
				}
			}
		} else if (coverageType.equals(NON_RIDER_COV) || coverageType.equals(RIDER)) {
			List coverages = null;
			if (coverageType.equals(NON_RIDER_COV)) {
				coverages = getNonRider();
			} else {
				coverages = getRider();
			}

			if (coverages != null) {
				count = coverages.size();
				for (int i = 0; i < count; i++) {
					coverage = (Coverage) coverages.get(i);
					if (aNbaOinkRequest.getCoverageFilter() != -1) {
						if (coverage.getLifeCovTypeCode() == aNbaOinkRequest.getCoverageFilter()) {
							aNbaOinkRequest.addValue(coverage.getProductCode(), NbaTableConstants.NBA_PLANS);
						}
					} else {
						aNbaOinkRequest.addValue(coverage.getProductCode(), NbaTableConstants.NBA_PLANS);
					}
				}
			}
		} else {
			List coverages = aLife.getCoverage();
			for (int i = 0; i < coverages.size(); i++) {
				Coverage aCoverage = aLife.getCoverageAt(i);
				aNbaOinkRequest.addValue(aCoverage.getProductCode(), NbaTableConstants.NBA_PLANS);
			}
		}
	}
	/**
	 * Obtain the value for OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.internationalUWProgInd
	 * @param aNbaOinkRequest
	 */
	// P2AXAL030 New Method
	public void retrieveInternationalUWProgInd(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo applicationInfo = getApplicationInfo();
		if (applicationInfo != null) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
			if (appInfoExt != null) {
				aNbaOinkRequest.addValue(appInfoExt.getInternationalUWProgInd());
			}
		}
	}	
	/**
	 * Obtain the value for OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.IUPQualifiedInd
	 * @param aNbaOinkRequest
	 */
	// APSL2332 New Method
	public void retrieveIUPQualifiedInd(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo applicationInfo = getApplicationInfo();
		if (applicationInfo != null) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
			if (appInfoExt != null) {
				aNbaOinkRequest.addValue(appInfoExt.getIUPQualifiedInd());
			}
		}
	}
	/**
	 * Obtain the value for OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.iupOverrideInd
	 * @param aNbaOinkRequest
	 */
	// P2AXAL030 New Method
	public void retrieveIupOverrideInd(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo applicationInfo = getApplicationInfo();
		if (applicationInfo != null) {
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
			if (appInfoExt != null) {
				aNbaOinkRequest.addValue(appInfoExt.getIUPOverrideInd());
			}
		}
	}	
	/**
	 * @param aNbaOinkRequest
	 */
	//P2AXAL018
	public void retrieveCLRPresent(NbaOinkRequest aNbaOinkRequest) {
		Life life = getNbaTXLife().getLife();
		boolean isCLRPresent = false;
		int covCount = life.getCoverageCount();
		for (int j = 0; j < covCount; j++) {
			Coverage coverage = life.getCoverageAt(j);
			if (NbaOliConstants.OLI_COVIND_RIDER == coverage.getIndicatorCode() && NbaOliConstants.OLI_COVTYPE_CLR == coverage.getLifeCovTypeCode()) {
				if (!NbaUtils.isDeleted(coverage)) {
					isCLRPresent = true;
					break;
				}
			}
		}
		aNbaOinkRequest.addValue(isCLRPresent);
	}
	
	/**
	 *TXLife.TXLifeRequest.OLifE.Holding.Policy.Life.LifeUSA.OLifEExtension.LifeUSAExtension.Exch1035IndCode
	 * @param aNbaOinkRequest
	 */
	// P2AXAL018 New Method
	public void retrieveExch1035IndCode(NbaOinkRequest aNbaOinkRequest) {
		LifeUSA lifeUSA=getLife().getLifeUSA();
		if (lifeUSA != null) {
			LifeUSAExtension lifeUSAExt = NbaUtils.getFirstLifeUSAExtension(lifeUSA);
			if (lifeUSAExt != null) {
				aNbaOinkRequest.addValue(lifeUSAExt.getExch1035IndCode());
			}
		}
	}
	
	//AXAL3.7.10C New Method
	public void retrieveOverrideRetentionLimitsInd(NbaOinkRequest aNbaOinkRequest) {
		ReinsuranceInfo reinsuranceInfo = getNbaTXLife().getDefaultReinsuranceInfo();
		ReinsuranceInfoExtension reinInfoExt = NbaUtils.getFirstReinsuranceInfoExtension(reinsuranceInfo);
		if(reinInfoExt != null) {
				aNbaOinkRequest.addValue(reinInfoExt.getOverrideRetentionLimitsInd());
		}
	}
	
	/**
	 * Retrieve Life TotalRiskAmt values where ReplacementIndCode = "Yes"
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.10C New method
	//P2AXAL056 Refractored adding support for the party
	public void retrieveReplAmount(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				aNbaOinkRequest.addValue(NbaUtils.getTotalReplacementAmt(getNbaTXLife(), party));
			}
		}
	}
	
	/**
	 * Obtain the value for ProductCode for all riders.
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.10C New Method
	public void retrieveCalcRiderProductCodeX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List riderProductCodes = NbaUtils.getRiderProductCodes(getNbaTXLife());
		for (int i = 0; i < riderProductCodes.size(); i++) {
			String productCode = (String) riderProductCodes.get(i);
			aNbaOinkRequest.addValue(productCode);
		}
	}
	
	/**
	 * Obtain the value for ProductCode for all Benefits.
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.10C New Method
	public void retrieveCalcBenefitProductCodeX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List benefitProductCodes = NbaUtils.getBenefitProductCodes(getNbaTXLife());
		for (int i = 0; i < benefitProductCodes.size(); i++) {
			String productCode = (String) benefitProductCodes.get(i);
			aNbaOinkRequest.addValue(productCode);
		}
	}
	
	/**
	 * Obtain the value for total retained amount for Single Life
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.10C New Method
	//P2AXAL056 Refractored for added the support for party
	public void retrieveSRetAmount(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				aNbaOinkRequest.addValue(NbaUtils.getTotalRetainedAmtSingleLife(party));
			}
		}
	}
	
	/**
	 * Obtain the value for Total reinsurance ceded amount for Single Life
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.10C New Method
	//P2AXAL056 Refractored for added the support for party
	public void retrieveSCededAmt(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				aNbaOinkRequest.addValue(NbaUtils.getTotalReinsuranceCededSingleLife(party));
			}
		}
	}
	
	/**
	 * Obtain the value for total retained amount for Joint Life.
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.10C New Method
	//P2AXAL056 Refractored for added the support for party
	public void retrieveJRetAmount(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				aNbaOinkRequest.addValue(NbaUtils.getTotalRetainedAmtJointLife(party));
			}
		}
	}
	
	/**
	 * Obtain the value for Total reinsurance ceded amount for Single Life
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.10C New Method
	//P2AXAL056 Refractored for added the support for party
	public void retrieveJCededAmt(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				aNbaOinkRequest.addValue(NbaUtils.getTotalReinsuranceCededJointLife(party));
			}
		}
	}
	
	/**
	 * Obtain the value for total prior reinsurance face amount.
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.10C New Method
	//P2AXAL056 Refractored for added the support for party
	public void retrievePriorReinsFaceAmt(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				aNbaOinkRequest.addValue(NbaUtils.getTotalPriorReinsuranceFaceAmt(party));
			}
		}
	}
	
	/**
	 * Obtain the value for Max Benefit Amount for ROPR benefit.
	 * @param aNbaOinkRequest - data request container
	 */
	
	// commented code for CR1343972 (remove the current calculation in OINK process for CLR on Prior Reinsurance.)
	//AXAL3.7.10C New Method
	/*public void retrieveTotalMaxBenefitROPRAmt(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(NbaUtils.getTotalMaxBenefitROPRAmt(getNbaTXLife()));
	}*/
	/**
	 * Obtain the Total Max Benefit ROPR Amount available for issuance which is calculated from the reinsurance calculator.
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaBaseException
	 */
	//CR1343972
	//CR735253-735254
	// commented code for changing mapping of TotalMaxBenefitROPRAmt
	//from OLifE.Holding.Policy.Life.Coverage.ReinsuranceInfo.OLifEExtension.ReinsuranceInfoExtension.ReinsuranceCalcInfo. TotalMaxBenefitROPRAmt
	//To TXLife.TXLifeRequest.OLifE.Holding.Policy.Life.Coverage.CovOption.MaxBenefitAmt
	/*public void retrieveTotalMaxBenefitROPRAmt(NbaOinkRequest aNbaOinkRequest) {
		//aNbaOinkRequest.addValue(NbaUtils.getTotalMaxBenefitROPRAmt(getNbaTXLife()));
		ReinsuranceInfo reinsuranceInfo = getNbaTXLife().getDefaultReinsuranceInfo();
		double totalMaxBenefitROPRAmt = 0;
		ReinsuranceInfoExtension reinsuranceInfoExt = NbaUtils.getFirstReinsuranceInfoExtension(reinsuranceInfo);
		if (reinsuranceInfoExt != null && reinsuranceInfoExt.getReinsuranceCalcInfoCount() > 0) {
			ReinsuranceCalcInfo reinCalcInfo = reinsuranceInfoExt.getReinsuranceCalcInfoAt(0);
			if (reinCalcInfo != null) {
				totalMaxBenefitROPRAmt = reinCalcInfo.getTotalMaxBenefitROPRAmt();
			}
		}
		aNbaOinkRequest.addValue(totalMaxBenefitROPRAmt);
	}*/
	/**
	 * Obtain the value of total facultative ceded amount.
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaBaseException
	 */
	//AXAL3.7.10C New Method
	public void retrieveTotalFacultativeCededAmt(NbaOinkRequest aNbaOinkRequest) {
		List coverages = getLife().getCoverage();
		double totalFacultativeCededAmt = 0;
		for (int i = 0; i < coverages.size(); i++) {
			Coverage coverage = (Coverage) coverages.get(i);
			CoverageExtension coverageExt = NbaUtils.getFirstCoverageExtension(coverage);
			if (coverageExt != null) {
				List reinsuranceOffers = coverageExt.getReinsuranceOffer();
				for (int k = 0; k < reinsuranceOffers.size(); k++) {
					ReinsuranceOffer reinOffer = (ReinsuranceOffer) reinsuranceOffers.get(k);
					if (reinOffer.getAcceptInd() &&(reinOffer.getAppliesToPartyID() == null ||
							reinOffer.getAppliesToPartyID().equals(getNbaTXLife().getPartyId(NbaOliConstants.OLI_REL_INSURED)))) {//P2AXAL056
						totalFacultativeCededAmt = totalFacultativeCededAmt + reinOffer.getCededAmt();
					}
				}
			}
		}
		aNbaOinkRequest.addValue(totalFacultativeCededAmt);
	}
	
	/**
	 * Obtain the total amount available for issuance which is calculated from the reinsurance calculator.
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaBaseException
	 */
	//AXAL3.7.10C New Method
	public void retrieveTotalAmtAvailableForIssuance(NbaOinkRequest aNbaOinkRequest) {
		ReinsuranceInfo reinsuranceInfo = getNbaTXLife().getDefaultReinsuranceInfo();
		double totalAmtAvailableForIssuance = 0;
		ReinsuranceInfoExtension reinsuranceInfoExt = NbaUtils.getFirstReinsuranceInfoExtension(reinsuranceInfo);
		if (reinsuranceInfoExt != null && reinsuranceInfoExt.getReinsuranceCalcInfoCount() > 0) {
			ReinsuranceCalcInfo reinCalcInfo = reinsuranceInfoExt.getReinsuranceCalcInfoAt(0);
			if (reinCalcInfo != null) {
				totalAmtAvailableForIssuance = reinCalcInfo.getTotalAmtAvailableForIssuance();
			}
		}
		aNbaOinkRequest.addValue(totalAmtAvailableForIssuance);
	}
	
	/**
	 * Returns true if Good Health Impairment exists on the case. ACORD typecode=1009800003 AND RiskType (or RiskClass) of 9
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaBaseException
	 */
	//APSL4630 - GHCP2.0
    public void retrieveGoodHealthCreditInd(NbaOinkRequest aNbaOinkRequest) {
        for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
            Party party = getParty(aNbaOinkRequest, i);
            if (party != null) {
                PartyExtension partyExtension = NbaUtils.getFirstPartyExtension(party);
                if (partyExtension != null) {
                    UnderwritingAnalysis uwAnalysis = partyExtension.getUnderwritingAnalysis();
                    if(uwAnalysis!= null && uwAnalysis.getGHCPInfo() != null &&   
                            uwAnalysis.getGHCPInfo().getGHCPAppliedInd() == 1 ){ //APSL5187
                        aNbaOinkRequest.addValue("true");
                    }
                }
            }
        }
      //AXAL3.7.10C New Method
        for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
            Person person = getPerson(aNbaOinkRequest, i);
            if (person != null) {
                PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person);
                if (personExtension != null) {
                    for (int k = 0; k < personExtension.getImpairmentInfo().size(); k++) {
                        ImpairmentInfo impairmentInfo = personExtension.getImpairmentInfoAt(k);
                        if (Long.parseLong(impairmentInfo.getImpairmentType()) == 1009800003
                                && Long.parseLong(impairmentInfo.getImpairmentClass()) == 9) {
                            aNbaOinkRequest.addValue("true");
                        }
                    }
                }
            }
        }
    }
	/**
	 * Obtain the value for 1035 Exchange Transfer Form requirement ReqStatus (ReqCode = 134). 
	 * @param aNbaOinkRequest - data request container
	 */
	//ALS3.7.10C New Method
 	public void retrieveReqStatus1035Exch(NbaOinkRequest aNbaOinkRequest) {
		RequirementInfo reqInfo = getNbaTXLife().getRequirementInfo(getNbaTXLife().getPrimaryParty(), NbaOliConstants.OLI_REQCODE_1035EXCFORM);
		if (reqInfo != null) {
			aNbaOinkRequest.addValue(reqInfo.getReqStatus());
		}
	}
 	/**
	 * It will retrieve true if the owner is same as insured else it  will retrieve false. 
	 * @param aNbaOinkRequest - data request container
	 */
	//P2AXAL018 New Method
 	public void retrieveOwnerSameAsInsuredInd(NbaOinkRequest aNbaOinkRequest) {
 		aNbaOinkRequest.addValue(getNbaTXLife().isOwnerSameAsInsured() ? NbaConstants.TRUE_STR : NbaConstants.FALSE_STR);//P2AXAL054
	}
 	
	/**
	 * Obtain all the values for ProductCode OLifE.Holding.Policy.Life.Coverage.covOption.LifeCovOptTypeCode is the code specifying ProductCode.
	 * @param aNbaOinkRequest - data request container
	 */
	// P2AXAL035 New Method
	public void retrieveLifeCovOptTypeCodeX(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = null;
		List covOptionList = null;
		CovOption covOption = null;
		Life life = null;
		aNbaOinkRequest.setParseMultiple(true);
		//Begin P2AXAL024
		coverage = getCoverageByQualifier(aNbaOinkRequest);
		if (coverage != null) {
			covOptionList = coverage.getCovOption();
			for (int j = 0; j < covOptionList.size(); j++) {
				covOption = (CovOption) covOptionList.get(j);
				if (!NbaUtils.isDeleted(covOption) && covOption.getCovOptionStatus() != NbaOliConstants.OLI_POLSTAT_DECISSUE) {
					aNbaOinkRequest.addValue(covOption.getLifeCovOptTypeCode());
				}
			}
		} else { //End P2AXAL024
			life = getLife();
			if (life != null) {
				for (int i = 0; i < life.getCoverageCount(); i++) {
					coverage = coverage = life.getCoverageAt(i);
					if (coverage != null) {
						covOptionList = coverage.getCovOption();
						for (int j = 0; j < covOptionList.size(); j++) {
							//ALII127 Code Moved
							covOption = (CovOption) covOptionList.get(j);
							if (covOption.hasLifeCovOptTypeCode()) {
								aNbaOinkRequest.addValue(covOption.getLifeCovOptTypeCode());
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain all the values for ProductCode OLifE.Holding.Policy.Life.Coverage.covOption.LifeCovOptTypeCode is the code specifying ProductCode.
	 * @param aNbaOinkRequest - data request container
	 */
	// P2AXAL035 New Method
	public void retrieveCovOptionStatusX(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = null;
		List covOptionList = null;
		CovOption covOption = null;
		Life life = null;
		life = getLife();
		aNbaOinkRequest.setParseMultiple(true);
		if (life != null) {
			for (int i = 0; i < life.getCoverageCount(); i++) {
				coverage = coverage = life.getCoverageAt(i);
				if (coverage != null) {
					covOptionList = coverage.getCovOption();
					for (int j = 0; j < covOptionList.size(); j++) {
						covOption = (CovOption) covOptionList.get(j);
						//ALII127 Code Moved
						if (covOption.hasLifeCovOptTypeCode()) {
							aNbaOinkRequest.addValue(covOption.getCovOptionStatus());
						}
					}
				}
			}
		}
	}


	/**
	 * Obtain the value for LifeCovTypeCode. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.Coverage.LifeCovStatus is the status of
	 * coverage.
	 * @param aNbaOinkRequest - data request container
	 */
//	 P2AXAL035 New Method
	public void retrieveLifeCovStatusX(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = null;
		Life life = getLife();
		aNbaOinkRequest.setParseMultiple(true);
		if (life != null) {
			int countCoverage = life.getCoverageCount();
			for (int i = 0; i < countCoverage; i++) {
				//ALII127 Code Moved
				coverage = life.getCoverageAt(i);
				if (coverage.hasLifeCovStatus()) {
					aNbaOinkRequest.addValue(coverage.getLifeCovStatus());
				} else {
					aNbaOinkRequest.addValue(-1L);//ALII483
				}
			}
		}
	}

	
	/**
	 * Obtain the value for MECInd. OLifE.Holding.Policy.LifeOrAnnuityOrDisabilityHealth.Life.LifeUSA.MECInd.
	 * @param aNbaOinkRequest - data request container
	 */
	// P2AXAL035 New Method
	public void retrieveMECInd(NbaOinkRequest aNbaOinkRequest) {
		Life life = getNbaTXLife().getLife();
		if (life != null) {
			LifeUSA lifeUSA = life.getLifeUSA();
			if (lifeUSA != null && lifeUSA.hasMECInd()) {
				aNbaOinkRequest.addValue(lifeUSA.getMECInd());
			}
		}
	}
	


	
	/**
	 * Obtain all the values of FormQuestionNumber for a VUL Form. OlifE.FormInstance.FormResponse.QuestionNumber is the question number
	 * for the VUL Suppliment Form.
	 * @param aNbaOinkRequest - data request container
	 */
	//	P2AXAL035  New Method
	public void retrieveQuestionNumberX(NbaOinkRequest aNbaOinkRequest) {
		FormInstance formInstance = getFormInstanceByQualifier(aNbaOinkRequest);
		aNbaOinkRequest.setParseMultiple(true);
		if (formInstance != null) {
			ArrayList formResponseList = formInstance.getFormResponse();
			//ALII127 Code Moved
			for (int i = 0; i < formResponseList.size(); i++) {
				aNbaOinkRequest.addValue(((FormResponse) formResponseList.get(i)).getQuestionNumber());
			}
		}
	}

	/**
	 * Obtain all the values of FormQuestionText for a VUL Form. OlifE.FormInstance.FormResponse.QuestionText is the question text
	 * for the VUL Suppliment Form.
	 * @param aNbaOinkRequest - data request container
	 */
	//	P2AXAL035 New Method
	public void retrieveQuestionTextX(NbaOinkRequest aNbaOinkRequest) {
		FormInstance formInstance = getFormInstanceByQualifier(aNbaOinkRequest);
		aNbaOinkRequest.setParseMultiple(true);
		if (formInstance != null) {
			ArrayList formResponseList = formInstance.getFormResponse();
			//ALII127 Code Moved
			for (int i = 0; i < formResponseList.size(); i++) {
				aNbaOinkRequest.addValue(((FormResponse) formResponseList.get(i)).getQuestionText());
			}
		}
	}

	/**
	 * Obtain all the values of FormResponseCode for a particular requirement. OlifE.FormInstance.FormResponse.ResponseCode is the Responce Code
	 * for the VUL Suppliment Form.
	 * @param aNbaOinkRequest - data request container
	 */
	//	P2AXAL035 New Method
	public void retrieveResponseCodeX(NbaOinkRequest aNbaOinkRequest) {
		FormInstance formInstance = getFormInstanceByQualifier(aNbaOinkRequest);
		aNbaOinkRequest.setParseMultiple(true);
		if (formInstance != null) {
			ArrayList formResponseList = formInstance.getFormResponse();
			//ALII127 Code Moved
			for (int i = 0; i < formResponseList.size(); i++) {
				aNbaOinkRequest.addValue(((FormResponse) formResponseList.get(i)).getResponseCode());
			}
		}
	}

	//P2AXAL025 New Method
	public void retrieveExch1035LoanCarryoverInd(NbaOinkRequest aNbaOinkRequest) {
		boolean exch1035Ind = false;
		Life life = getNbaTXLife().getLife();
		if (life != null) {
			LifeUSA lifeUSA = life.getLifeUSA();
			LifeUSAExtension lifeusaExt = NbaUtils.getFirstLifeUSAExtension(lifeUSA);
			if (lifeusaExt != null) {
				exch1035Ind = lifeusaExt.getExch1035LoanCarryoverInd();
				} 
			}
		aNbaOinkRequest.addValue(exch1035Ind);
	}

	//P2AXAL025 New Method
	public void retrieveLoanBalance(NbaOinkRequest aNbaOinkRequest) {
		double loanBalance = 0;
		int relationCnt = getOLifE().getRelationCount();
		for (int j = 0; j < relationCnt; j++) {
			Relation aRelation = getOLifE().getRelationAt(j);
			if (aRelation.hasRelationRoleCode() && aRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_REPLACEDBY) {
				Holding holding = nbaTXLife.getHolding(aRelation.getRelatedObjectID());
				loanBalance = getLoanBalance(holding);
				if (loanBalance> 0) {
					break;
				}
			}
		}
		aNbaOinkRequest.addValue(loanBalance);
	}
	//P2AXAL025 New Method
	protected double getLoanBalance(Holding holding) {
		double loanBalance = 0;
		int loanCount = holding.getLoanCount();
		Loan loan = null;
		for(int i=0;i<loanCount;i++) {
			loan = holding.getLoanAt(i); //ALII1082
			if (loan.hasLoanBalance() && loan.getLoanBalance() > 0) {
				loanBalance = loan.getLoanBalance();
				break;
			}
		}
		return loanBalance;
	}
	
	//AXAL3.7.10C New Method
	public void retrieveParentGuardianTotInsAmt(NbaOinkRequest aNbaOinkRequest) {
		NbaParty applicant = getNbaTXLife().getApplicant(); 
		if(applicant == null) {
			applicant = getNbaTXLife().getPrimaryOwner();
		}
		if(applicant != null) {
			Risk risk = applicant.getRisk();
			RiskExtension riskExtension = NbaUtils.getFirstRiskExtension(risk);
			if(riskExtension != null) {
				aNbaOinkRequest.addValue(riskExtension.getParentGuardianTotInsAmt());
			}	
		}
	}

	//P2AXAL018, ALII122
	public void retrieve1035ExchReqInd(NbaOinkRequest aNbaOinkRequest) {
		boolean is1035ExchReqInd = false;
		List replHoldingList = NbaUtils.getHoldingByRoleCode(getNbaTXLife(), NbaOliConstants.OLI_REL_REPLACEDBY);
		for (int i = 0; i < replHoldingList.size(); i++) {
			Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
			PolicyExtension replPolicyExtn = NbaUtils.getFirstPolicyExtension(replPolicy);
			if (replPolicyExtn != null && NbaUtils.isAnsweredYes(replPolicyExtn.getReplacementIndCode())) {
				if (replPolicy.hasLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty()
						&& replPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().isLife()) {
					LifeUSAExtension replLifeUSAExtn = NbaUtils.getFirstLifeUSAExtension(replPolicy
							.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife().getLifeUSA());
					if (replLifeUSAExtn != null && NbaUtils.isUnanswered(replLifeUSAExtn.getExch1035IndCode())) {
						is1035ExchReqInd = true;
						break;
					}
				}
			} 
		}
		aNbaOinkRequest.addValue(is1035ExchReqInd);
	}
	
	/**
	 * Retrieve Life TotalRiskAmt values (from question 43 of application).
	 * @param aNbaOinkRequest
	 */
	//AXAL3.7.10C New method
	//P2AXAL056 Refractored added support for the party
	public void retrieveCalcTotalRiskAmt(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				aNbaOinkRequest.addValue(NbaUtils.getTotalRiskAmt(getNbaTXLife(), party));
			}
		}
	}
	
	/**
	 * Obtain the value for Prior CLR Benefit amount.
	 * @param aNbaOinkRequest - data request container
	 */
	//AXAL3.7.10C New Method
	/*public void retrievePriorCLRBenefitAmt(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(NbaUtils.getPriorCLRBenefitAmt(getNbaTXLife()));
	}*/
	
	/**
	 * Obtain the value for CLR Rider Amount.
	 * @param aNbaOinkRequest - data request container
	 */
	//	CR1343972 new Method
	public void retrieveCLRRiderAmount(NbaOinkRequest aNbaOinkRequest) {
		//aNbaOinkRequest.addValue(NbaUtils.getPriorCLRBenefitAmt(getNbaTXLife()));
		ReinsuranceInfo reinsuranceInfo = getNbaTXLife().getDefaultReinsuranceInfo();
		double clrRiderAmount = 0;
		ReinsuranceInfoExtension reinsuranceInfoExt = NbaUtils.getFirstReinsuranceInfoExtension(reinsuranceInfo);
		if (reinsuranceInfoExt != null && reinsuranceInfoExt.getReinsuranceCalcInfoCount() > 0) {
			ReinsuranceCalcInfo reinCalcInfo = reinsuranceInfoExt.getReinsuranceCalcInfoAt(0);
			if (reinCalcInfo != null) {
				clrRiderAmount = reinCalcInfo.getCLRRiderAmount();
			}
		}
		aNbaOinkRequest.addValue(clrRiderAmount);
	}
		
	
	/**
	 * Obtain the value for ProductCode for Holding.Policy.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA300 New Method
	public void retrieveProductCodeX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List replHoldingList = NbaUtils.getTermConvOPAIHoldings(getNbaTXLife());
		for (int i = 0; i < replHoldingList.size(); i++) {
			Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
			PolicyExtension replPolicyExtn = NbaUtils.getFirstPolicyExtension(replPolicy);
			if (replPolicyExtn != null) {
				if(!("-1").equalsIgnoreCase(replPolicyExtn.getTermConvPlanSeries())){
					aNbaOinkRequest.addValue(replPolicyExtn.getTermConvPlanSeries());
				} else{
					aNbaOinkRequest.addValue("");
				}
			}
		}
	}

	/**
	 * Obtain value for Holding.Policy.OLifEExtension.PolicyExtension.TermConvSubType
	 * where TermConvSubType is not null. Resolve as a List.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA300 New Method
	public void retrieveTermConvSubTypeX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List replHoldingList = NbaUtils.getTermConvOPAIHoldings(getNbaTXLife());
		for (int i = 0; i < replHoldingList.size(); i++) {
			Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
			PolicyExtension replPolicyExtn = NbaUtils.getFirstPolicyExtension(replPolicy);
			if (replPolicyExtn != null) {
				aNbaOinkRequest.addValue(replPolicyExtn.getTermConvSubType());
			}
		}
	}

	/**
	 * Obtain value for Holding.Policy.OLifEExtension.PolicyExtension.TermConvFaceAmt
	 * where TermConvSubType is not null. Resolve as a List.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA300 New Method
	public void retrieveTermConvFaceAmtX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List replHoldingList = NbaUtils.getTermConvOPAIHoldings(getNbaTXLife());
		for (int i = 0; i < replHoldingList.size(); i++) {
			Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
			PolicyExtension replPolicyExtn = NbaUtils.getFirstPolicyExtension(replPolicy);
			if (replPolicyExtn != null) {
				aNbaOinkRequest.addValue(replPolicyExtn.getTermConvFaceAmt());
			}
		}
	}

	/**
	 * Obtain value for Holding.Policy.OLifEExtension.PolicyExtension.TermConvPolicyStatus
	 * where TermConvSubType is not null. Resolve as a List.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA300 New Method
	public void retrieveTermConvPolicyStatusX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List replHoldingList = NbaUtils.getTermConvOPAIHoldings(getNbaTXLife());
		for (int i = 0; i < replHoldingList.size(); i++) {
			Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
			PolicyExtension replPolicyExtn = NbaUtils.getFirstPolicyExtension(replPolicy);
			if (replPolicyExtn != null) {
				aNbaOinkRequest.addValue(replPolicyExtn.getTermConvPolicyStatus());
			}
		}
	}

	/**
	 * Obtain value for Holding.Policy.OLifEExtension.PolicyExtension.TermConvRateClass
	 * where TermConvSubType is not null. Resolve as a List.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA300 New Method
	public void retrieveTermConvRateClassX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List replHoldingList = NbaUtils.getTermConvOPAIHoldings(getNbaTXLife());
		for (int i = 0; i < replHoldingList.size(); i++) {
			Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
			PolicyExtension replPolicyExtn = NbaUtils.getFirstPolicyExtension(replPolicy);
			if (replPolicyExtn != null) {
				if(!("-1").equalsIgnoreCase(replPolicyExtn.getTermConvRateClass())){
					aNbaOinkRequest.addValue(replPolicyExtn.getTermConvRateClass());
				} else{
					aNbaOinkRequest.addValue("");
				}
			}
		}
	}

	//ALII1204 New Method
	public void retrieveTermConvPolNumberX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List replHoldingList = NbaUtils.getTermConvOPAIHoldings(getNbaTXLife());
		for (int i = 0; i < replHoldingList.size(); i++) {
			Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
			if (replPolicy != null) {
				aNbaOinkRequest.addValue(replPolicy.getPolNumber());
			}
		}
	}

	/**
	 * Obtain value for Holding.Policy.OLifEExtension.PolicyExtension.TermConvRidersCC.TermConvRiders
	 * where TermConvSubType is not null. Resolve as a List.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA300 New Method
	public void retrieveTermConvRidersX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List replHoldingList = NbaUtils.getTermConvOPAIHoldings(getNbaTXLife());
		for (int i = 0; i < replHoldingList.size(); i++) {
			Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
			PolicyExtension replPolicyExtn = NbaUtils.getFirstPolicyExtension(replPolicy);
			String riderStr = "";
			if (replPolicyExtn != null) {
				if(replPolicyExtn.hasTermConvRidersCC()){
					TermConvRidersCC tcRiderCC = replPolicyExtn.getTermConvRidersCC();
					ArrayList aList = tcRiderCC.getTermConvRiders();
					if(aList != null && aList.size()>0){
						for (int k = 0; k < aList.size(); k++) {
							if(!"-1".equalsIgnoreCase(aList.get(k).toString())){
								riderStr += aList.get(k).toString();
							}
						}
					}
				}
				aNbaOinkRequest.addValue(riderStr);
			}
		}
	}

	/**
	 * Obtain value for Holding.Policy.ApplicationInfo.ApplicationType
	 * where TermConvSubType is not null. Resolve as a List.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA300 New Method
	public void retrieveApplicationTypeX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List replHoldingList = NbaUtils.getTermConvOPAIHoldings(getNbaTXLife());
		for (int i = 0; i < replHoldingList.size(); i++) {
			Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
			PolicyExtension replPolicyExtn = NbaUtils.getFirstPolicyExtension(replPolicy);
			if (replPolicyExtn != null) {
				ApplicationInfo replAppInfo = replPolicy.getApplicationInfo();
				if (replAppInfo != null) {
					aNbaOinkRequest.addValue(replAppInfo.getApplicationType(), NbaTableConstants.OLI_LU_APPTYPE);
				}
			}
		}
	}
	
	/**
	 * Obtain value for Holding.Policy.OLifEExtension.PolicyExtension.TermConvRatingReductionInd
	 * where TermConvSubType is not null. Resolve as a List.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA300 New Method
	public void retrieveTermConvRatingReductionIndX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List replHoldingList = NbaUtils.getTermConvOPAIHoldings(getNbaTXLife());
		for (int i = 0; i < replHoldingList.size(); i++) {
			Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
			PolicyExtension replPolicyExtn = NbaUtils.getFirstPolicyExtension(replPolicy);
			if (replPolicyExtn != null) {
				aNbaOinkRequest.addValue(replPolicyExtn.getTermConvRatingReductionInd());
			}
		}
	}
	
	/**
	 * Obtain value for Holding.Policy.OLifEExtension.PolicyExtension.TermConvIncreaseUWReqrdInd
	 * where TermConvSubType is not null. Resolve as a List.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA300 New Method
	public void retrieveTermConvIncreaseUWReqrdIndX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List replHoldingList = NbaUtils.getTermConvOPAIHoldings(getNbaTXLife());
		for (int i = 0; i < replHoldingList.size(); i++) {
			Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
			PolicyExtension replPolicyExtn = NbaUtils.getFirstPolicyExtension(replPolicy);
			if (replPolicyExtn != null) {
				aNbaOinkRequest.addValue(replPolicyExtn.getTermConvIncreaseUWReqrdInd());
			}
		}
	}

	/**
	 * Obtain value for Holding.Policy.OLifEExtension.PolicyExtension.TermConvRiderAddUWReqrdInd
	 * where TermConvSubType is not null. Resolve as a List.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA300 New Method
	public void retrieveTermConvRiderAddUWReqrdIndX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List replHoldingList = NbaUtils.getTermConvOPAIHoldings(getNbaTXLife());
		for (int i = 0; i < replHoldingList.size(); i++) {
			Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
			PolicyExtension replPolicyExtn = NbaUtils.getFirstPolicyExtension(replPolicy);
			if (replPolicyExtn != null) {
				aNbaOinkRequest.addValue(replPolicyExtn.getTermConvRiderAddUWReqrdInd());
			}
		}
	}

	/**
	 * Obtain value for Holding.Policy.OLifEExtension.PolicyExtension.TermConvBenefitAddUWReqrdInd
	 * where TermConvSubType is not null. Resolve as a List.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA300 New Method
	public void retrieveTermConvBenefitAddUWReqrdIndX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List replHoldingList = NbaUtils.getTermConvOPAIHoldings(getNbaTXLife());
		for (int i = 0; i < replHoldingList.size(); i++) {
			Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
			PolicyExtension replPolicyExtn = NbaUtils.getFirstPolicyExtension(replPolicy);
			if (replPolicyExtn != null) {
				aNbaOinkRequest.addValue(replPolicyExtn.getTermConvBenefitAddUWReqrdInd());
			}
		}
	}

	/**
	 * Obtain value for Holding.Policy.OLifEExtension.PolicyExtension.RemoveTermConvExclRdrInd
	 * where TermConvSubType is not null. Resolve as a List.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA300 New Method
	public void retrieveRemoveTermConvExclRdrIndX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List replHoldingList = NbaUtils.getTermConvOPAIHoldings(getNbaTXLife());
		for (int i = 0; i < replHoldingList.size(); i++) {
			Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
			PolicyExtension replPolicyExtn = NbaUtils.getFirstPolicyExtension(replPolicy);
			if (replPolicyExtn != null) {
				aNbaOinkRequest.addValue(replPolicyExtn.getRemoveTermConvExclRdrInd());
			}
		}
	}
	
	/**
	 * Obtain value for Holding.Policy.OLifEExtension.PolicyExtension.OptionOffSchedule
	 * where TermConvSubType is not null. Resolve as a List.
	 * @param aNbaOinkRequest - data request container
	 */
	//CR1345266 New Method
	public void retrieveOptionOffScheduleX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List replHoldingList = NbaUtils.getTermConvOPAIHoldings(getNbaTXLife());
		for (int i = 0; i < replHoldingList.size(); i++) {
			Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
			PolicyExtension replPolicyExtn = NbaUtils.getFirstPolicyExtension(replPolicy);
			if (replPolicyExtn != null) {
				aNbaOinkRequest.addValue(replPolicyExtn.getOptionOffSchedule());
			}
		}
	}

	//ALII181 New Method
	public void retrieveROPRPresent(NbaOinkRequest aNbaOinkRequest) {
		boolean ropr = false;
		Coverage coverage = null;
		List covOptionList = null;
		CovOption covOption = null;
		if (getNbaTXLife() != null) {
			Life life = getNbaTXLife().getLife();
			if (life != null) {
				for (int i = 0; i < life.getCoverageCount(); i++) {
					coverage = life.getCoverageAt(i);
					if (coverage != null) {
						covOptionList = coverage.getCovOption();
						for (int j = 0; j < covOptionList.size(); j++) {
							covOption = (CovOption) covOptionList.get(j);
							if (covOption.hasLifeCovOptTypeCode() && covOption.getLifeCovOptTypeCode() == NbaOliConstants.OLI_OPTTYPE_ROPR
									&& covOption.getCovOptionStatus() != NbaOliConstants.OLI_POLSTAT_DECISSUE) {
								ropr = true;
								break;
							}
						}
					}
				}
			}
		}
		aNbaOinkRequest.addValue(ropr);
	}
	
	

	
	/**
	 * Obtain value of Holding.Policy.RequirementInfo.ReqCode as a List.
	 * @param aNbaOinkRequest
	 */
	//	P2AXAL035 New Method
	public void retrieveReqCodeX(NbaOinkRequest aNbaOinkRequest){
		aNbaOinkRequest.setParseMultiple(true);
		List reqInfoList = getNbaTXLife().getPolicy().getRequirementInfo();
		for(int i=0; i<reqInfoList.size(); i++){
			RequirementInfo reqInfo=  (RequirementInfo)reqInfoList.get(i);
			if(reqInfo != null ){
				aNbaOinkRequest.addValue(reqInfo.getReqCode());
			}
		}
	}
	
	
	/**
	 * Obtain value of Holding.Policy.RequirementInfo.ReqStatus as a List.
	 * @param aNbaOinkRequest
	 */
	//P2AXAL035 New Method
	public void retrieveReqStatusX(NbaOinkRequest aNbaOinkRequest){
		aNbaOinkRequest.setParseMultiple(true);
		List reqInfoList = getNbaTXLife().getPolicy().getRequirementInfo();
		for(int i=0; i<reqInfoList.size(); i++){
			RequirementInfo reqInfo=  (RequirementInfo)reqInfoList.get(i);
			if(reqInfo != null ){
				aNbaOinkRequest.addValue(reqInfo.getReqStatus());
			}
		}
	}
	
	/**
	 * Obtain the vale for OLifE.Holding.Party.Client.ClientExtension.ClientAcknowledgeInfo.OwnerQtnreInd
	 * @param aNbaOinkRequest
	 */
    //A2_AXAL003 New Method
	public void retrieveOwnerQtnreInd(NbaOinkRequest aNbaOinkRequest) {		 
		aNbaOinkRequest.addValue(getUWReqFormInd(aNbaOinkRequest, NbaOliConstants.AXA_UWREQFORMS_OWNER_QUEST));		
	}
	
	/**
	 * Obtain the vale for OLifE.Holding.Party.Client.ClientExtension.ClientAcknowledgeInfo.ForeignResTravelInfoQtnreInd
	 * @param aNbaOinkRequest
	 */
    //A2_AXAL003 New Method
	public void retrieveForeignResTravelInfoQtnreInd(NbaOinkRequest aNbaOinkRequest) {		 
		aNbaOinkRequest.addValue(getUWReqFormInd(aNbaOinkRequest, NbaOliConstants.AXA_UWREQFORMS_FRTI_QUEST));
	}
	
	/**
	 * Obtain the vale for OLifE.Holding.Party.Client.ClientExtension.ClientAcknowledgeInfo.MedInfoQtnreInd
	 * @param aNbaOinkRequest
	 */
    //A2_AXAL003 New Method
	public void retrieveMedInfoQtnreInd(NbaOinkRequest aNbaOinkRequest) { 
		aNbaOinkRequest.addValue(getUWReqFormInd(aNbaOinkRequest, NbaOliConstants.AXA_UWREQFORMS_MEDICAL_QUEST));
	}
	
	/**
	 * Obtain the vale for OLifE.Holding.Party.Client.ClientExtension.ClientAcknowledgeInfo.FinancialInfoQtnreInd
	 * @param aNbaOinkRequest
	 */
    //A2_AXAL003 New Method
	public void retrieveFinancialInfoQtnreInd(NbaOinkRequest aNbaOinkRequest) {		 
		aNbaOinkRequest.addValue(getUWReqFormInd(aNbaOinkRequest, NbaOliConstants.AXA_UWREQFORMS_FINANCIAL_QUEST));
	}
	
	/**
	 * Obtain the vale for OLifE.Holding.Party.Client.ClientExtension.ClientAcknowledgeInfo.CTIRQtnreInd
	 * @param aNbaOinkRequest
	 */
    //A2_AXAL003 New Method
	public void retrieveCTIRQtnreInd(NbaOinkRequest aNbaOinkRequest) { 
		aNbaOinkRequest.addValue(getUWReqFormInd(aNbaOinkRequest, NbaOliConstants.AXA_UWREQFORMS_CTIR_QUEST));
	}
	
	/**
	 * Obtain the vale for OLifE.Holding.Party.Client.ClientExtension.ClientAcknowledgeInfo.SubstanceUsageQtnreInd
	 * @param aNbaOinkRequest
	 */
    //A2_AXAL003 New Method
	public void retrieveSubstanceUsageQtnreInd(NbaOinkRequest aNbaOinkRequest) { 
		aNbaOinkRequest.addValue(getUWReqFormInd(aNbaOinkRequest, NbaOliConstants.AXA_UWREQFORMS_SUBSTANCE_QUEST));
	}
	
	/**
	 * Obtain the vale for OLifE.Holding.Party.Client.ClientExtension.ClientAcknowledgeInfo.AviationQtnreInd
	 * @param aNbaOinkRequest
	 */
    //A2_AXAL003 New Method
	public void retrieveAviationQtnreInd(NbaOinkRequest aNbaOinkRequest) { 
		aNbaOinkRequest.addValue(getUWReqFormInd(aNbaOinkRequest, NbaOliConstants.AXA_UWREQFORMS_AVIATION_QUEST));
	}
	
	/**
	 * Obtain the vale for OLifE.Holding.Party.Client.ClientExtension.ClientAcknowledgeInfo.AvocationQtnreInd
	 * @param aNbaOinkRequest
	 */
    //A2_AXAL003 New Method
	public void retrieveAvocationQtnreInd(NbaOinkRequest aNbaOinkRequest) { 
		aNbaOinkRequest.addValue(getUWReqFormInd(aNbaOinkRequest, NbaOliConstants.AXA_UWREQFORMS_AVOCATION_QUEST));
	}
	
	/**
	 * Obtain the vale for OLifE.Holding.Party.Client.ClientExtension.ClientAcknowledgeInfo.AxaTermLifeInd
	 * @param aNbaOinkRequest
	 */
    //A2_AXAL003 New Method	
	public void retrieveAxaTermLifeInd(NbaOinkRequest aNbaOinkRequest) {
		//A3_AXAL005 start code review comment
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				//A3_AXAL005 end
				Client client = aParty.getClient();
				if (client != null) {
					ClientExtension clientExt = NbaUtils.getFirstClientExtension(client);
					if (clientExt != null) {
						if (clientExt.getClientAcknowledgeInfo() != null) {
							aNbaOinkRequest.addValue(clientExt.getClientAcknowledgeInfo().getProductInfoType() == NbaOliConstants.AXA_PRODUCTINFOTYPE_TERM ? NbaConstants.TRUE_STR : NbaConstants.FALSE_STR);
						}
					}
				}
			}
		}
	}
	
 	public void retrieveRequirementInfoCount(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(getNbaTXLife().getPolicy().getRequirementInfo().size());
	}
 	
 	/**
	 * Obtain the value for a UW result reasons. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.UnderwritingResult.UnderwritingResultReason.
	 * @param aNbaOinkRequest - data request container
	 */
	//P2AXAL024
	public void retrieveUnderwritingResultReasonX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List ratings = getPermFlatSubstandardRatings(aNbaOinkRequest);
		SubstandardRatingExtension substandardRatingExt;
		for(int i = 0; i<ratings.size();i++){
			substandardRatingExt = NbaUtils.getFirstSubstandardExtension((SubstandardRating) ratings.get(i));
			if(substandardRatingExt != null){
				List uwResultList = substandardRatingExt.getUnderwritingResult();
				for(int j = 0; j < uwResultList.size();j++){
					UnderwritingResult uwResult = (UnderwritingResult) uwResultList.get(j);
					if(!uwResult.isActionDelete())
						aNbaOinkRequest.addValue(uwResult.getUnderwritingResultReason());
				}
			}
		}
	}
	
	/**
	 * Obtain the value for a MinPremiumInitialAmt. OLifE.Holding.Policy.MinPremiumInitialAmt.
	 * @param aNbaOinkRequest - data request container
	 */
	//P2AXAL013
	public void retrieveMinPremiumInitialAmt(NbaOinkRequest aNbaOinkRequest) {
		Policy pol = getPolicy();
		if(pol != null){
			aNbaOinkRequest.addValue(pol.getMinPremiumInitialAmt());
		}
	}
	
	//Coded for defect ALII333.
	//OINK Attribute A_TermConvPurchOptInd mapped to TXLife.TXLifeRequest.OLifE.Holding.Policy.ApplicationInfo.OLifEExtension.ApplicationInfoExtension.TermConvPurchOptInd
 	public void retrieveTermConvPurchOptInd(NbaOinkRequest aNbaOinkRequest) {
 		ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
		if (applicationInfoExtension != null) {
			aNbaOinkRequest.addValue(applicationInfoExtension.getTermConvPurchOptInd());
		}
	}
 	
 	//NBA297
	public void retrieveReadyForSuitabilityInd(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null) {
					aNbaOinkRequest.addValue(oli.getApplicationInfoExtension().getReadyForSuitabilityInd());
					return;
				}
			}
		}
		aNbaOinkRequest.addValue(false);
	}
 	//NBA297
	public void retrievePriorSuitabilityIGOStatusInd(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null) {
					aNbaOinkRequest.addValue(oli.getApplicationInfoExtension().getPriorSuitabilityIGOStatusInd());
					return;
				}
			}
		}
		aNbaOinkRequest.addValue(false);
	}

 	//NBA297
	public void retrieveQualifyForSuitabilityInd(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null) {
					aNbaOinkRequest.addValue(oli.getApplicationInfoExtension().getQualifyForSuitabilityInd());
					return;
				}
			}
		}
		aNbaOinkRequest.addValue(false);
	}	
	
	//AXAL3.7.43 New Method
	public void retrieveAgeInDays(NbaOinkRequest aNbaOinkRequest){
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				aNbaOinkRequest.addValue(NbaUtils.calcDaysDiff(new Date(System.currentTimeMillis()), person.getBirthDate())); 
			} else {
				break;
			}
		}
	}
	
	/**
	 * Obtain the value for Reg60 PreSale Decision. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.Reg60PSDecision.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBA231 new method
	public void retrieveSuitabilityDecisionStatus(NbaOinkRequest aNbaOinkRequest) {

		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null) {
					aNbaOinkRequest.addValue(oli.getApplicationInfoExtension().getSuitabilityDecisionStatus());
				}
			}
		}
	}	
	
	/**
	 * verify if "ReinsuranceOffer" is present. 
	 * @param aNbaOinkRequest
	 */
    //AXAL3.7.10A New Method	
	public void retrieveReinsuranceOfferPresent(NbaOinkRequest aNbaOinkRequest) {
		Life life = getLife();
		List coverages = life.getCoverage();
		Coverage coverage = null;
		CoverageExtension coverageExt = null; 
		for(int i=0;i<coverages.size();i++) {
			coverage = (Coverage) coverages.get(i);
			coverageExt = NbaUtils.getFirstCoverageExtension(coverage);
			if(coverageExt != null) {
				List reinOffers = coverageExt.getReinsuranceOffer();
				if(reinOffers != null && !reinOffers.isEmpty()) {
					aNbaOinkRequest.addValue(true);
					return;
				}
			}
		}
		aNbaOinkRequest.addValue(false);
	}
	
	//AXAL3.7.10A New Method
	public void retrieveReinsurersTreatyIdentX(NbaOinkRequest aNbaOinkRequest){
		Life life = getLife();
		List coverages = life.getCoverage();
		Coverage coverage = null;
		CoverageExtension coverageExt = null;
		for (int i = 0; i < coverages.size(); i++) {
			coverage = (Coverage) coverages.get(i);
			coverageExt = NbaUtils.getFirstCoverageExtension(coverage);
			if (coverageExt != null) {
				List reinsuranceOfferList = coverageExt.getReinsuranceOffer();
				for (int k = 0; k < reinsuranceOfferList.size(); k++) {
					ReinsuranceOffer reinOffer = (ReinsuranceOffer) reinsuranceOfferList.get(k);
					if (reinOffer != null && reinOffer.hasPartyID()) {
						ReinsuranceInfo reinInfo = NbaUtils.getReinsuranceInfo(coverage, reinOffer.getPartyID());
						if (reinInfo != null) {
							aNbaOinkRequest.addValue(reinInfo.getReinsurersTreatyIdent());
						}
					}
				}
			}
		}
	}
	
	//AXAL3.7.10A New Method
	public void retrieveDBAX(NbaOinkRequest aNbaOinkRequest) {
		Life life = getLife();
		List coverages = life.getCoverage();
		Coverage coverage = null;
		CoverageExtension coverageExt = null;
		for (int i = 0; i < coverages.size(); i++) {
			coverage = (Coverage) coverages.get(i);
			coverageExt = NbaUtils.getFirstCoverageExtension(coverage);
			if(coverageExt != null) {
				List reinsuranceOfferList = coverageExt.getReinsuranceOffer();
				for (int k = 0; k < reinsuranceOfferList.size(); k++) {
					ReinsuranceOffer reinOffer = (ReinsuranceOffer) reinsuranceOfferList.get(k);
					if (reinOffer != null && reinOffer.hasPartyID() && !(NbaConstants.PROC_POSTMANUALAPPROVAL).equalsIgnoreCase(reinOffer.getSourceID())
							&& !(NbaConstants.PROC_FINAL_DISPOSITION).equalsIgnoreCase(reinOffer.getSourceID())) { // NBLXA-2114 allows reinsurance offer having partyId and
																							// not having SourceId=A2PSMNAP
						NbaParty party = getNbaTXLife().getParty(reinOffer.getPartyID());
						if (party != null) {
							aNbaOinkRequest.addValue(party.getDBA());
						}
						break;
					}
					continue;

				}
			}
		}
	}
	
	//AXAL3.7.10A New Method
	public void retrieveAcceptIndX(NbaOinkRequest aNbaOinkRequest) {
		Life life = getLife();
		List coverages = life.getCoverage();
		Coverage coverage = null;
		CoverageExtension coverageExt = null;
		for (int i = 0; i < coverages.size(); i++) {
			coverage = (Coverage) coverages.get(i);
			coverageExt = NbaUtils.getFirstCoverageExtension(coverage);
			if(coverageExt != null) {
				List reinsuranceOfferList = coverageExt.getReinsuranceOffer();
				for (int k = 0; k < reinsuranceOfferList.size(); k++) {
					ReinsuranceOffer reinOffer = (ReinsuranceOffer) reinsuranceOfferList.get(k);
					// Begin NBLXA-1331
					if (reinOffer != null && !(NbaConstants.PROC_POSTMANUALAPPROVAL).equalsIgnoreCase(reinOffer.getSourceID())
							&& !(NbaConstants.PROC_FINAL_DISPOSITION).equalsIgnoreCase(reinOffer.getSourceID())) { // NBLXA-2114
						if ((NbaConfigurationConstants.Negative_Dispose).equalsIgnoreCase(reinOffer.getSourceID())) {
							aNbaOinkRequest.addValue(false);
							break;
						}
						if (reinOffer.getAcceptRejectCode() == NbaConstants.REINSURANCE_REJECT_TRUE_CODE) {
							aNbaOinkRequest.addValue(false);
						} else if (reinOffer.getAcceptRejectCode() == NbaConstants.REINSURANCE_REJECT_FALSE_CODE) {
							aNbaOinkRequest.addValue(true);
						}
						break;
					} // End NBLXA-1331
					continue;

				}
			}
		}
	}
	/**
	 * Obtain the value for ResidenceState of the Party
	 * @param aNbaOinkRequest - data request container
	 */
	//P2AXAL036
	public void retrieveResidenceState(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				if (!aParty.hasResidenceState()) {
					aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_STATES);
				} else {
					aNbaOinkRequest.addValue(aParty.getResidenceState(), NbaTableConstants.NBA_STATES); //NBA093
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_STATES);
		}
	}	
	/**
	 * Obtain the value for MailingState, an instance of AddressStateTc for an Address with type code = 17. OLifE.Party.Address.AddressStateTc is the
	 * address state
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveMailingState(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_MAILING);
				if (address == null) {
					aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_STATES);
				} else {
					aNbaOinkRequest.addValue(address.getAddressStateTC(), NbaTableConstants.NBA_STATES); //NBA093
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.NBA_STATES);
		}
	}
	
	/**
	 * determines the ApplicantDiffFromOwnerIndCode for the applicant relation
	 */
	public void retrieveApplicantDiffFromOwnerIndCode(NbaOinkRequest aNbaOinkRequest) {
		Relation aRelation = NbaUtils.getRelation(getOLifE(), NbaOliConstants.OLI_REL_APPLICANT);
		if (aRelation != null) {
			RelationExtension relationExt = NbaUtils.getFirstRelationExtension(aRelation);
			if(relationExt != null){
				aNbaOinkRequest.addValue(relationExt.getApplicantDiffFromOwnerIndCode());
			}
		}
	}
	
	// CR60956 New Method
	public void retrieveIssuedToAdminSysInd(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
		if (applicationInfoExtension != null) {
			aNbaOinkRequest.addValue(applicationInfoExtension.getIssuedToAdminSysInd());
		}
	}

	//APSL4585
    public void retrieveIsPaidReissue(NbaOinkRequest aNbaOinkRequest) {
        boolean paidReissue = false;
        ApplicationInfoExtension appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
        paidReissue = appInfoExtn != null && appInfoExtn.getReissueType() == NbaOliConstants.AXA_REISSUETYPE_PAID;
        aNbaOinkRequest.addValue(paidReissue);
        
     }
	
	/**
	 * Obtain the vale for TXLife.TXLifeRequest.OLifE.Party.Client.OLifEExtension.ClientExtension.ClientAcknowledgeInfo.ProductInfoType
	 * @param aNbaOinkRequest
	 */
    //A3_AXAL005 New Method	
	public void retrieveProductInfoType(NbaOinkRequest aNbaOinkRequest) {
		//A3_AXAL005 start code review comment
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				//A3_AXAL005 end
				Client client = aParty.getClient();
				if (client != null) {
					ClientExtension clientExt = NbaUtils.getFirstClientExtension(client);
					if (clientExt != null) {
						if (clientExt.getClientAcknowledgeInfo() != null) {
							aNbaOinkRequest.addValue(clientExt.getClientAcknowledgeInfo().getProductInfoType());
						}
					}
				}
			}
		}
	}
	
	/**
	 * Obtain the vale for OLifE.Holding.Party.Client.ClientExtension.ClientAcknowledgeInfo.TermPolicyQtnreInd
	 * @param aNbaOinkRequest    AviationQtnreInd
	 */
    //A3_AXAL005 New Method
	public void retrieveTermConvQtnreInd(NbaOinkRequest aNbaOinkRequest) { 
		aNbaOinkRequest.addValue(getUWReqFormInd(aNbaOinkRequest, NbaOliConstants.AXA_UWREQFORMS_TERM_POLICY_QUEST));
	}
	
	/**
	 * Obtain the value for HIPAAAuthorizationInd for APS and Pharmaceutical.
	 * 
	 * @param aNbaOinkRequest
	 */
//	Start APSL2654
	//Rewriting method for SR675913(APSL2654)
	//APSL1358 New Method
	/*public void retrieveHIPAAAuthorizationInd(NbaOinkRequest aNbaOinkRequest) {
		boolean reqReceiveAndReview = false;
		List requirementInfos = getNbaTXLife().getPolicy().getRequirementInfo();
		for (int i = 0; requirementInfos != null && i < requirementInfos.size(); i++) {
			RequirementInfo requirementInfo = getNbaTXLife().getPolicy().getRequirementInfoAt(i);
			if (requirementInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_RECEIVED
					&& requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_1009800033) {
				RequirementInfoExtension reqInfoExtn = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
				if (reqInfoExtn != null && reqInfoExtn.getReviewedInd()) {
					reqReceiveAndReview = true;
				}
				break;
			}
		}
		
		int signatureInfoCount = getApplicationInfo().getSignatureInfoCount();
		boolean signatureFound = false;
		for (int i = 0; reqReceiveAndReview && i < signatureInfoCount; i++) {
			SignatureInfo signInfo = getApplicationInfo().getSignatureInfoAt(i);
			if (signInfo.getSignatureRoleCode() == NbaOliConstants.OLI_PARTICROLE_PRIMARY && 
				signInfo.getSignaturePurpose() == NbaOliConstants.OLI_SIGTYPE_BLANKETAUTH) {
				signatureFound = true;
				aNbaOinkRequest.addValue(signatureFound);
				break;
			}
		}
		if (signatureFound == false) {
			aNbaOinkRequest.addValue(signatureFound);
		}
	}*/
     
	//APSL4410 Refactoring the code with insured level
		public void retrieveHIPAAAuthorizationInd(NbaOinkRequest aNbaOinkRequest) {		
			boolean signatureFound = false;
			for (int k = 0; k < aNbaOinkRequest.getCount(); k++) { // APSL4410
				boolean reqReceive = false;
				boolean hippaOutstanding = false;
				Party party = getParty(aNbaOinkRequest, k); // APSL4410
				if (party != null) { // APSL4410
					List requirementInfos = getNbaTXLife().getPolicy().getRequirementInfo();
					for (int i = 0; requirementInfos != null && i < requirementInfos.size(); i++) {
						RequirementInfo requirementInfo = getNbaTXLife().getPolicy().getRequirementInfoAt(i);
						if (requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_1009800033 && requirementInfo.hasAppliesToPartyID()
								&& requirementInfo.getAppliesToPartyID().equalsIgnoreCase(party.getId())) { // APSL4410
							if (requirementInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_RECEIVED) {
								// Begin QC15843 (APSL4373)
								RequirementInfoExtension reqInfoExtn = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
								if (reqInfoExtn != null && reqInfoExtn.getReviewedInd()) {
									reqReceive = true;
								} else {
									hippaOutstanding = true;
									break;
								}// End QC15843 (APSL4373)
							} else if (requirementInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_ADD) {
								hippaOutstanding = true;
								break;
							}
						}
					}

				
					Object[] relations = getOLifE().getRelation().toArray(); // APSL4410
					Relation relation = NbaUtils.getRelationForParty(party.getId(), relations); // APSL4410
					long xSignatureRoleCode = relation.getRelationRoleCode() == NbaOliConstants.OLI_PARTICROLE_32 ? NbaOliConstants.OLI_PARTICROLE_PRIMARY
							: NbaOliConstants.OLI_PARTICROLE_JOINT; // APSL4410

					if (hippaOutstanding == false && reqReceive == true) {
						int signatureInfoCount = getApplicationInfo().getSignatureInfoCount();
						for (int i = 0; reqReceive && i < signatureInfoCount; i++) {
							SignatureInfo signInfo = getApplicationInfo().getSignatureInfoAt(i);
							if (signInfo.getSignatureRoleCode() == xSignatureRoleCode
									&& signInfo.getSignaturePurpose() == NbaOliConstants.OLI_SIGTYPE_BLANKETAUTH) { // APSL4410
								signatureFound = true;
								aNbaOinkRequest.addValue(signatureFound);
								break;
							}
						}
					}				
				}
			}
		if (!signatureFound) { //APSL4410
			aNbaOinkRequest.addValue(false);
		}
		}
	/**
	 * Obtain the value for ApplicationSubType. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.ApplicationSubType.
	 * @param aNbaOinkRequest - data request container
	 */
	//ALII1206 New Method
	public void retrieveApplicationSubType(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
		if (applicationInfoExtension != null) {
			aNbaOinkRequest.addValue(applicationInfoExtension.getApplicationSubType());
			return;
		}			
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_APPTYPE);		
		}
	}	
	/**
	 * Obtain the value for CoverageCurrentAmtEPR. Holding.Policy.Life.Coverage.CurrentAmt.
	 * @param aNbaOinkRequest - data request container
	 */
//	CR735253-735254 new method
	public void retrieveCoverageCurrentAmtEPR(NbaOinkRequest aNbaOinkRequest) {
		Life life = getNbaTXLife().getLife();
		int covCount = life.getCoverageCount();
		for (int j = 0; j < covCount; j++) {
			Coverage coverage = life.getCoverageAt(j);
			if (NbaOliConstants.OLI_COVIND_RIDER == coverage.getIndicatorCode() && NbaOliConstants.OLI_COVTYPE_ESTATEPROT == coverage.getLifeCovTypeCode() && coverage.getLifeCovStatus() == NbaOliConstants.OLI_POLSTAT_ACTIVE) {
				aNbaOinkRequest.addValue(String.valueOf(coverage.getCurrentAmt()));
			}
		}
	}
	/**
	 * Obtain the value for CoverageProductCodeEPR. Holding.Policy.Life.Coverage.ProductCode.
	 * @param aNbaOinkRequest - data request container
	 */
//	CR735253-735254 new method
	public void retrieveCoverageProductCodeEPR(NbaOinkRequest aNbaOinkRequest) {
		Life life = getNbaTXLife().getLife();
		int covCount = life.getCoverageCount();
		for (int j = 0; j < covCount; j++) {
			Coverage coverage = life.getCoverageAt(j);
			if (NbaOliConstants.OLI_COVIND_RIDER == coverage.getIndicatorCode() && NbaOliConstants.OLI_COVTYPE_ESTATEPROT == coverage.getLifeCovTypeCode() && coverage.getLifeCovStatus() == NbaOliConstants.OLI_POLSTAT_ACTIVE) {
				aNbaOinkRequest.addValue(coverage.getProductCode());
			}
		}
	}
	/**
	 * Obtain the value for ApplicationSubType. Holding.Policy.Life.Coverage.ProductCode.
	 * @param aNbaOinkRequest - data request container
	 */
//	CR735253-735254 new method
	public void retrieveCoverageProductCodeCLR(NbaOinkRequest aNbaOinkRequest) {
		Life life = getNbaTXLife().getLife();
		int covCount = life.getCoverageCount();
		for (int j = 0; j < covCount; j++) {
			Coverage coverage = life.getCoverageAt(j);
			if (NbaOliConstants.OLI_COVIND_RIDER == coverage.getIndicatorCode() && NbaOliConstants.OLI_COVTYPE_CLR == coverage.getLifeCovTypeCode() && coverage.getLifeCovStatus() == NbaOliConstants.OLI_POLSTAT_ACTIVE) {
				aNbaOinkRequest.addValue(String.valueOf(coverage.getProductCode()));
			}
		}
	}
	/**
	 * Obtain the value for CoverageCurrentAmtCLR. Holding.Policy.Life.Coverage.CurrentAmt.
	 * @param aNbaOinkRequest - data request container
	 */
//	CR735253-735254 new method
	public void retrieveCoverageCurrentAmtCLR(NbaOinkRequest aNbaOinkRequest) {
		Life life = getNbaTXLife().getLife();
		int covCount = life.getCoverageCount();
		for (int j = 0; j < covCount; j++) {
			Coverage coverage = life.getCoverageAt(j);
			if (NbaOliConstants.OLI_COVIND_RIDER == coverage.getIndicatorCode() && NbaOliConstants.OLI_COVTYPE_CLR == coverage.getLifeCovTypeCode() && coverage.getLifeCovStatus() == NbaOliConstants.OLI_POLSTAT_ACTIVE) {
				aNbaOinkRequest.addValue(coverage.getCurrentAmt());
			}
		}
	}
	/**
	 * Obtain the value for CovOptionProductCodeROPR. Holding.Policy.Life.Coverage.CovOption.ProductCode.
	 * @param aNbaOinkRequest - data request container
	 */
//	CR735253-735254 new method
	public void retrieveCovOptionProductCodeROPR(NbaOinkRequest aNbaOinkRequest) {
		Life life = getNbaTXLife().getLife();
		int covCount = life.getCoverageCount();
		for (int j = 0; j < covCount; j++) {
			Coverage coverage = life.getCoverageAt(j);
			if (coverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_BASE) {
				CovOption covOpt = getNbaTXLife().getCovOption(coverage, NbaOliConstants.OLI_OPTTYPE_ROPR);
				if (covOpt != null) {
					aNbaOinkRequest.addValue(covOpt.getProductCode());
				}
			}
		}
	}
	/**
	 * Obtain the value for TotalMaxBenefitROPRAmt. Holding.Policy.Life.Coverage.CovOption.MaxBenefitAmt.
	 * @param aNbaOinkRequest - data request container
	 */
//	CR735253-735254 new method
	public void retrieveTotalMaxBenefitROPRAmt(NbaOinkRequest aNbaOinkRequest) {
		Life life = getNbaTXLife().getLife();
		int covCount = life.getCoverageCount();
		for (int j = 0; j < covCount; j++) {
			Coverage coverage = life.getCoverageAt(j);
			if (coverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_BASE) {
				CovOption covOpt = getNbaTXLife().getCovOption(coverage, NbaOliConstants.OLI_OPTTYPE_ROPR);
				if (covOpt != null && !NbaUtils.isBlankOrNull(covOpt.getMaxBenefitAmt())) {
					aNbaOinkRequest.addValue(covOpt.getMaxBenefitAmt());
				}else{
					aNbaOinkRequest.addValue(0.0);
				}
			}
		}
	}
	/**
	 * Obtain the value for CovOptionProductCodeDPW. Holding.Policy.Life.Coverage.CovOption.ProductCode.
	 * @param aNbaOinkRequest - data request container
	 */
//	CR735253-735254 new method
	public void retrieveCovOptionProductCodeDPW(NbaOinkRequest aNbaOinkRequest) {
		Life life = getNbaTXLife().getLife();
		int covCount = life.getCoverageCount();
		for (int j = 0; j < covCount; j++) {
			Coverage coverage = life.getCoverageAt(j);
			if (coverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_BASE) {
				CovOption covOpt = getNbaTXLife().getCovOption(coverage, NbaOliConstants.OLI_COVTYPE_DREADDISEASE);
				if (covOpt != null) {
					aNbaOinkRequest.addValue(covOpt.getProductCode());
				}
			}
		}
	}
	/**
	 * Obtain the value for CovOptionProductCodeDDW. Holding.Policy.Life.Coverage.CovOption.ProductCode
	 * @param aNbaOinkRequest - data request container
	 */
//	CR735253-735254 new method
	public void retrieveCovOptionProductCodeDDW(NbaOinkRequest aNbaOinkRequest) {
		Life life = getNbaTXLife().getLife();
		int covCount = life.getCoverageCount();
		for (int j = 0; j < covCount; j++) {
			Coverage coverage = life.getCoverageAt(j);
			if (coverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_BASE) {
				CovOption covOpt = getNbaTXLife().getCovOption(coverage, NbaOliConstants.OLI_OPTTYPE_WMD);
				if (covOpt != null) {
					aNbaOinkRequest.addValue(covOpt.getProductCode());
				}
			}
		}
	}
	/**
	 * Obtain the values for TXLife.TXLifeRequest.OLifE.Party.Client.OLifEExtension.ClientExtension.ClientAcknowledgeInfo.ProductInfoType
	 * @param aNbaOinkRequest
	 */
    //A4_AXAL005 New Method	
	public void retrieveProductInfoTypeX(NbaOinkRequest aNbaOinkRequest) {
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		Party party = null;
		aNbaOinkRequest.setParseMultiple(true);
		for (int i = 0; i < partyList.size() ; i++) { 
			party = (Party)partyList.get(i);
			if (party != null) {
				Client client = party.getClient();
				if (client != null) {
					ClientExtension clientExt = NbaUtils.getFirstClientExtension(client);
					if (clientExt != null) {
						if (clientExt.getClientAcknowledgeInfo() != null) {
							aNbaOinkRequest.addValue(clientExt.getClientAcknowledgeInfo().getProductInfoType());
						}
					}
				}
			}
		}
	}

	/**
	 * Obtain the values for TXLife.TXLifeRequest.OLifE.Party.Client.OLifEExtension.ClientExtension.ClientExtension.ClientAcknowledgeInfo.UWReqFormsCC.UWReqFormsTC)
	 * @param aNbaOinkRequest
	 */
    //A4_AXAL005 New Method	
	public void retrieveUWReqFormsX(NbaOinkRequest aNbaOinkRequest) {
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		Party party = null;
		aNbaOinkRequest.setParseMultiple(true);
		for (int i = 0; i < partyList.size() ; i++) { 
			party = (Party)partyList.get(i);
			if (party != null) {
				Client client = party.getClient();
				if (client != null) {
					ClientExtension clientExt = NbaUtils.getFirstClientExtension(client);
					if (clientExt != null) {
						ClientAcknowledgeInfo clientAck = clientExt.getClientAcknowledgeInfo();
						if (clientAck != null && clientAck.hasUWReqFormsCC()) {
							UWReqFormsCC UwReqFrms = clientAck.getUWReqFormsCC();
							if(UwReqFrms != null){
								for(int j=0; j< UwReqFrms.getUWReqFormsTCCount(); j++)
								aNbaOinkRequest.addValue(UwReqFrms.getUWReqFormsTCAt(j));
							}
						}
					}
				}
			}
		}
	}


	/**
	 * Obtain the value for OrgForm from OLifE.Party.Organization.OrgForm
	 * @param aNbaOinkRequest - data request container
	 */
	//P2AXAL053 New Method
	public void retrieveOrgFormX(NbaOinkRequest aNbaOinkRequest) {
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		Party party = null;
		aNbaOinkRequest.setParseMultiple(true);
		for (int i = 0; i < partyList.size(); i++) {
			party = (Party) partyList.get(i);
			if (party != null) {
				Object obj = party.getPersonOrOrganization$Contents();
				if ((obj != null) && (obj instanceof Organization)) {
					Organization aOrg = party.getPersonOrOrganization().getOrganization();
					aNbaOinkRequest.addValue(aOrg.getOrgForm());
				}
			} else {
				break;
			}
		}
	}
	

	/**
	 * Obtain the value for OrgForm from OLifE.Party.Organization.OrgForm
	 * @param aNbaOinkRequest - data request container
	 */
	//P2AXAL053 New Method
	public void retrieveBeneSameAsOwnerIndX(NbaOinkRequest aNbaOinkRequest) {
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		Party party = null;
		aNbaOinkRequest.setParseMultiple(true);
		for (int i = 0; i < partyList.size(); i++) {
			party = (Party) partyList.get(i);
			if (party != null) {
				aNbaOinkRequest.addValue(getNbaTXLife().isSameAsOwner(party));
			} else {
				break;
			}
		}
	}
	
	/**
	 * Obtain the value for OrgForm from OLifE.Party.Organization.OrgForm
	 * @param aNbaOinkRequest - data request container
	 */
	//P2AXAL053 New Method
	public void retrieveIrrevokableIndX(NbaOinkRequest aNbaOinkRequest) {
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		Party party = null;
		aNbaOinkRequest.setParseMultiple(true);
		for (int i = 0; i < partyList.size(); i++) {
			party = (Party) partyList.get(i);
			if (party != null) {
				Relation relation = getRelation(aNbaOinkRequest.getQualifier(), i, getPartyForPrimaryIns().getId(), party.getId());
				if (relation != null) {
					aNbaOinkRequest.addValue(relation.getIrrevokableInd());
				}
			} else {
				break;
			}
		}
	}
	
	/**
	 * Obtain the value for msgCode of all the non delted overidale system messages
	 */
	//ALII1107 New method
	public void retrieveDefaultingMsgCodeX(NbaOinkRequest aNbaOinkRequest) {
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE());
		SystemMessage sysMsg = null;
		SystemMessageExtension sysMsgExt = null;
		aNbaOinkRequest.setParseMultiple(true);
		for (int i = 0; i < holding.getSystemMessageCount(); i++) {
			sysMsg = holding.getSystemMessageAt(i);
			sysMsgExt = NbaUtils.getFirstSystemMessageExtension(sysMsg);
			if (sysMsg != null && !sysMsg.isDeleted() && sysMsgExt !=null 
						&& sysMsgExt.getMsgValidationType()== AxaConstants.DEFAULTING_SUBSET
						&& NbaOliConstants.OLI_MSGSEVERITY_OVERIDABLE == sysMsg.getMessageSeverityCode()) {
					aNbaOinkRequest.addValue(sysMsg.getMessageCode());
			}
		}
	}
	
	/**
	 * Obtain the value amount for EPR from the prior reinsurance
	 */
	//P2AXAL056 New Method
	public void retrievePriorEPRRiderAmt(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(NbaUtils.getPriorEPRRiderAmt(getNbaTXLife()));
	}
	

	//CR61047 Code deleted
		
	/**
	 * Obtain the value for EntryTotalInforceAndAppliedIns for applicant 
	 * @param aNbaOinkRequest - data request container
	 */
	//P2AXAL056 New Method
	public void retrieveEntryTotalInforceAndAppliedIns(NbaOinkRequest aNbaOinkRequest) {
		NbaParty applicant = getNbaTXLife().getApplicant(); 
		if(applicant == null) {
			applicant = getNbaTXLife().getPrimaryOwner();
		}
		if(applicant != null) {
			Risk risk = applicant.getRisk();
			RiskExtension riskExtension = NbaUtils.getFirstRiskExtension(risk);
			if(riskExtension != null) {
				aNbaOinkRequest.addValue(riskExtension.getEntryTotalInforceAndAppliedIns());
			}	
		}
	}
	
	/**
	 * Obtain the value for a EmploymentExtension attributes 
	 * @param aNbaOinkRequest -  data request container
	 */
	// P2AXAL066
	public void retrieveEmploymentExtensionQuestions(NbaOinkRequest aNbaOinkRequest) throws InvocationTargetException, IllegalAccessException {
		Object methodObject = EmploymentExtensionVariables.get(aNbaOinkRequest.getRootVariable().toUpperCase());
		Class aClass;
		Method aMethod;
		if (methodObject != null) {
			aMethod = (Method) methodObject;
			aClass = aMethod.getReturnType();
		} else {
			if (getLogger().isWarnEnabled()) {
				getLogger().logWarn("Retrieve variable name is invalid: " + aNbaOinkRequest.getVariable());
			}
			return;
		}
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				if (party.getEmploymentCount() > 0) {
					EmploymentExtension extension = NbaUtils.getFirstEmploymentExtension(party.getEmploymentAt(0));
					if (extension != null) {
						aNbaOinkRequest.addValueForType(((Method) methodObject).invoke(extension, null), aClass);
					} else {
						aNbaOinkRequest.addUnknownValueForType(aClass);
					}
				}
			} else {
				break;
			}
		}
	}
	
	//CR61047
	public void retrieveTermConvTableRatingIndX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List replHoldingList = NbaUtils.getTermConvOPAIHoldings(getNbaTXLife());
		for (int i = 0; i < replHoldingList.size(); i++) {
			Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
			PolicyExtension replPolicyExtn = NbaUtils.getFirstPolicyExtension(replPolicy);
			if (replPolicyExtn != null) {
				aNbaOinkRequest.addValue(replPolicyExtn.getTermConvTableRatingInd());
			}
		}
	}

	//CR61047
	public void retrieveTermConvFlatExtraIndX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List replHoldingList = NbaUtils.getTermConvOPAIHoldings(getNbaTXLife());
		for (int i = 0; i < replHoldingList.size(); i++) {
			Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
			PolicyExtension replPolicyExtn = NbaUtils.getFirstPolicyExtension(replPolicy);
			if (replPolicyExtn != null) {
				aNbaOinkRequest.addValue(replPolicyExtn.getTermConvFlatExtraInd());
			}
		}
	}

	//CR61047
	public void retrieveIssueDateX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List replHoldingList = NbaUtils.getTermConvOPAIHoldings(getNbaTXLife());
		for (int i = 0; i < replHoldingList.size(); i++) {
			Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
			if (replPolicy != null && replPolicy.hasIssueDate()) {
				aNbaOinkRequest.addValue(replPolicy.getIssueDate());
			}
		}
	}

	/**
	 * Obtain the value for LifeCovOptTypeCode and CovOptionStatus for Prior Insurance. Holding.Policy.Life.Coverage.CovOption.LifeCovOptTypeCode and
	 * Holding.Policy.Life.Coverage.CovOption.CovOptionStatus
	 * @param aNbaOinkRequest - data request container
	 */
	//CR735251 new method
	public void retrieveLifeCovOptTypeCodePI(NbaOinkRequest aNbaOinkRequest) throws Exception {
		boolean flag = false;
		boolean statusFlag = false;
		int partyCount = getOLifE().getPartyCount();
		for (int k = 0; k < partyCount; k++) {
			Party party = getOLifE().getPartyAt(k);
			int attachmentCount = party.getAttachmentCount();
			for (int j = 0; j < attachmentCount; j++) {
				if (party.getAttachmentAt(j).getAttachmentType() == NbaOliConstants.OLI_ATTACH_PRIORINS) {
					String pureResults = party.getAttachmentAt(j).getAttachmentData().getPCDATA();
					NbaTXLife attachmentTxLife = new NbaTXLife(pureResults);
					Policy policy = attachmentTxLife.getPrimaryHolding().getPolicy();//ALII1530 
					if (! getPolicy().getPolNumber().equals(policy.getPolNumber())) {//ALII1530
						Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();//ALII1530
						List coverages = life.getCoverage();
						Coverage coverage = null;
						if (coverages != null) {
							int next = 0;
							while ((coverage = getNextCoverage(aNbaOinkRequest, coverages, next++)) != null) {
								int covOptionCount = coverage.getCovOptionCount();
								for (int i = 0; i < covOptionCount; i++) {
									if (coverage.getCovOptionAt(i).getLifeCovOptTypeCode() == NbaOliConstants.OLI_OPTTYPE_LTCABO) {
										if (coverage.getCovOptionAt(i).getCovOptionStatus() == NbaOliConstants.OLI_COVOPTION_ACTIVE
												|| coverage.getCovOptionAt(i).getCovOptionStatus() == NbaOliConstants.OLI_COVOPTION_PENDING) {
											statusFlag = true;
										} else {
											flag = true;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		if (statusFlag) {
			aNbaOinkRequest.addValue(2);
		} else if (flag) {
			aNbaOinkRequest.addValue(1);
		} else {
			aNbaOinkRequest.addValue(0);
		}
	}
	//	Begin QC9347/ALII1411
	public void retrieve1035FundReceived(NbaOinkRequest aNbaOinkRequest) {
		boolean found = true;
		List requirementInfos = getNbaTXLife().getPolicy().getRequirementInfo();
		for (int i = 0; requirementInfos != null && i < requirementInfos.size(); i++) {
			RequirementInfo requirementInfo = getNbaTXLife().getPolicy().getRequirementInfoAt(i);
			if ((requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_1009800090
					|| requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_676 
					|| requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_1000500044) && 
					(requirementInfo.getReqStatus() != NbaOliConstants.OLI_REQSTAT_RECEIVED
							&& requirementInfo.getReqStatus() != NbaOliConstants.OLI_REQSTAT_CANCELLED
							&& requirementInfo.getReqStatus() != NbaOliConstants.OLI_REQSTAT_WAIVED))  { //ALII1437
				found = false;
				break;

			}
		}
		aNbaOinkRequest.addValue(found);
	}
	//End QC9347/ALII1411
	//New Method: QC9244
//	public void retrieveRetainedAmt(NbaOinkRequest aNbaOinkRequest) {
//		ReinsuranceInfo reinsuranceInfo = getNbaTXLife().getDefaultReinsuranceInfo();
//		if (reinsuranceInfo != null) {
//			BigDecimal bd = new BigDecimal(reinsuranceInfo.getRetentionAmt());
//			if (!NbaUtils.isBlankOrNull(bd)) {
//				aNbaOinkRequest.addValue(NbaUtils.setScaleTo2(bd).toString());
//			}
//		}
//
//	}
	
	/**
	 * Obtain the value for PermissionCode from OLifE.Party.Organization.OLifEExtension.OrganizationExtension.PermissionCode
	 * @param aNbaOinkRequest - data request container
	 */
	
	//SR641590 (APSL2012) SUB-BGA	
	public void retrievePermissionCode(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Organization organization = getOrganization(aNbaOinkRequest, i);
			if (organization != null) {
				OrganizationExtension organizationExtension = NbaUtils.getFirstOrganizationExtension(organization);
				if (organizationExtension != null) {
					aNbaOinkRequest.addValue(organizationExtension.getPermissionCode());
				}
			} else {
				break;
			}
		}
	}
	
	//ALNA621 modified for APSL2536
	public void retrieveEPRAmount(NbaOinkRequest aNbaOinkRequest) {
		Life life = getNbaTXLife().getLife();
		double eprRiderAmount = 0;
		int covCount = life.getCoverageCount();
		for (int j = 0; j < covCount; j++) {
			Coverage coverage = life.getCoverageAt(j);
			if (NbaOliConstants.OLI_COVTYPE_ESTATEPROT == coverage.getLifeCovTypeCode()) {
				if (!NbaUtils.isDeleted(coverage) && coverage.getLifeCovStatus() != NbaOliConstants.OLI_POLSTAT_DECISSUE) {
					eprRiderAmount = (!NbaUtils.isBlankOrNull(coverage.getCurrentAmt())) ? coverage.getCurrentAmt() : 0.0;
					break;
				}
			}
		}
		aNbaOinkRequest.addValue(eprRiderAmount);
	}
	
	/**
	 * Obtain the value for a Person ApprovedRateClass. OLifE.Party.PersonOrOrganization.Person.PersonExtension.ApprovedRateClass is the approved at
	 * rateclass of the person.
	 * 
	 * @param aNbaOinkRequest -
	 *            data request container
	 */
	//SR657319 New Method
	public void retrieveApprovedRateClass(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Person person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				String approvedRateClass = "";
				PersonExtension personExtension = NbaUtils.getFirstPersonExtension(person);
				if (personExtension != null) {
					if (personExtension.hasApprovedRateClass()) {
						approvedRateClass = personExtension.getApprovedRateClass();
					}
				}
				aNbaOinkRequest.addValue(approvedRateClass, NbaTableConstants.NBA_RATECLASS);
			} else {
				break;
			}
		}
	}
//SR564247(APSL2525) New Method
	public void retrieveIssueType(NbaOinkRequest aNbaOinkRequest) {
		if (getPolicy() != null && getPolicy().hasIssueDate()) {
			aNbaOinkRequest.addValue(getPolicy().getIssueType());
		} else {
			aNbaOinkRequest.addUnknownValue("");
		}
	}
	
//SR564247(APSL2525) New Method
	public void retrievePredictiveInd(NbaOinkRequest aNbaOinkRequest) {
		Policy policy = getPolicy();
		if (policy != null) {
			PolicyExtension policyextension = NbaUtils.getFirstPolicyExtension(policy);
			if (policyextension != null) { //CR1663222(APSL2857)/ALII1806
				aNbaOinkRequest.addValue(policyextension.getPredictiveInd());
			}
		}
	}
	//SR564247(APSL2525) New Method
	public void retrievePredRateClassVerificationInd(NbaOinkRequest aNbaOinkRequest) {
		Policy policy = getPolicy();
		if (policy != null) {
			PolicyExtension policyextension = NbaUtils.getFirstPolicyExtension(policy);
			if (policyextension != null && policyextension.hasPredRateClassVerifiyInd()) {
				aNbaOinkRequest.addValue(policyextension.getPredRateClassVerifiyInd());
			}
		}
	}	
	
	//SR564247(APSL2525) New Method
	public void retrievePredMissingDataInd(NbaOinkRequest aNbaOinkRequest) {
		Policy policy = getPolicy();
		if (policy != null) {
			PolicyExtension policyextension = NbaUtils.getFirstPolicyExtension(policy);
			if (policyextension != null && policyextension.hasPredMissingDataInd()) {
				aNbaOinkRequest.addValue(policyextension.getPredMissingDataInd());
			}
		}
	}
	
	//SR564247(APSL2525) New Method
	public void retrievePredApplicationReviewInd(NbaOinkRequest aNbaOinkRequest) {
		Policy policy = getPolicy();
		if (policy != null) {
			PolicyExtension policyextension = NbaUtils.getFirstPolicyExtension(policy);
			if (policyextension != null && policyextension.hasPredApplicationReviewInd()) {
				aNbaOinkRequest.addValue(policyextension.getPredApplicationReviewInd());
			}
		}
	}
	
	//SR564247(APSL2525) New Method
	public void retrievePredDisabilityWaiverReviewInd(NbaOinkRequest aNbaOinkRequest) {
		Policy policy = getPolicy();
		if (policy != null) {
			PolicyExtension policyextension = NbaUtils.getFirstPolicyExtension(policy);
			if (policyextension != null && policyextension.hasPredDisabilityWaiverReviewInd()) {
				aNbaOinkRequest.addValue(policyextension.getPredDisabilityWaiverReviewInd());
			}
		}
	}
	
	//SR564247(APSL2525) New Method
	public void retrievePredDoctorVisitInd(NbaOinkRequest aNbaOinkRequest) {
		Policy policy = getPolicy();
		if (policy != null) {
			PolicyExtension policyextension = NbaUtils.getFirstPolicyExtension(policy);
			if (policyextension != null && policyextension.hasPredDoctorVisitInd()) {
				aNbaOinkRequest.addValue(policyextension.getPredDoctorVisitInd());
			}
		}
	}
	
	//SR564247(APSL2525) New Method
	public void retrievePredConcurrentAppInd(NbaOinkRequest aNbaOinkRequest) {
		Policy policy = getPolicy();
		if (policy != null) {
			PolicyExtension policyextension = NbaUtils.getFirstPolicyExtension(policy);
			if (policyextension != null && policyextension.hasPredConcurrentAppInd()) {
				aNbaOinkRequest.addValue(policyextension.getPredConcurrentAppInd());
			}
		}
	}
	//CR1346706/APSL2724 - New Method //APSL4657 
	public void retrieveAnnualPaymentAmt(NbaOinkRequest aNbaOinkRequest) {
		if (getPolicy().hasAnnualPaymentAmt()) {
			DecimalFormat format = new DecimalFormat("0.00");
	        String annualPaymentAmt = format.format(getPolicy().getAnnualPaymentAmt());
			aNbaOinkRequest.addValue(annualPaymentAmt);
			return;
		}
		aNbaOinkRequest.addValue("0");
	}
	
	
	/**
	 * Obtain the value for ResidenceCountry of the Party
	 * @param aNbaOinkRequest - data request container
	 */
	//CR1346706/APSL2724 - New Method
	public void retrieveResidenceCountry(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				if (!aParty.hasResidenceCountry()) {
					aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_NATION);
				} else {
					aNbaOinkRequest.addValue(aParty.getResidenceCountry(),NbaTableConstants.OLI_LU_NATION);
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_NATION);
		}
	}
	
	/**
	 * Obtain the value for MailingCountry of the Party address
	 * @param aNbaOinkRequest - data request container
	 */
	//CR1346706/APSL2724 - New Method
	public void retrieveMailingCountry(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_MAILING);
				if (address == null) {
					aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_NATION);
				} else {
					aNbaOinkRequest.addValue(address.getAddressCountryTC(),NbaTableConstants.OLI_LU_NATION); 
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1L, NbaTableConstants.OLI_LU_NATION);
		}
	}
	
	/**
	 * It will retrieve true if the owner is same as primary insured else it  will retrieve false. 
	 * @param aNbaOinkRequest - data request container
	 */
	//ALII1770 New Method
 	public void retrieveOwnerSameAsPrimaryInsuredInd(NbaOinkRequest aNbaOinkRequest) {
 		aNbaOinkRequest.addValue(getNbaTXLife().isOwnerSameAsPrimaryIns() ? NbaConstants.TRUE_STR : NbaConstants.FALSE_STR);
	}
 	
 	/**
	 * It will retrieve true if the owner is same as joint insured else it  will retrieve false. 
	 * @param aNbaOinkRequest - data request container
	 */
	//ALII1770 New Method
 	public void retrieveOwnerSameAsJointInsuredInd(NbaOinkRequest aNbaOinkRequest) {
 		aNbaOinkRequest.addValue(getNbaTXLife().isOwnerSameAsJointIns() ? NbaConstants.TRUE_STR : NbaConstants.FALSE_STR);
	}
 	

	/**
	 * Sets the value as "1" (true) if it is an internal Term to Term replacement else sets the value as "0" (false)
	 * 
	 * @param aNbaOinkRequest -
	 *            data request container
	 */
	//QC#10404, APSL2719 New Method
	public void retrieveTermToTermIntReplInd(NbaOinkRequest aNbaOinkRequest) {
		boolean termToTermintReplInd = false;
		if (!NbaUtils.isBlankOrNull(getPolicy())
				&& (NbaOliConstants.OLI_PRODTYPE_TERM == getPolicy().getProductType() || NbaOliConstants.OLI_PRODTYPE_INDETERPREM == getPolicy()
						.getProductType()) && NbaOliConstants.OLI_REPTY_INTERNAL == getPolicy().getReplacementType()) {
			termToTermintReplInd = NbaUtils.isTermToTermIntReplInd(getNbaTXLife());
		}
		aNbaOinkRequest.addValue(termToTermintReplInd);
	}
	
	
	/**
	 * Sends the value of replacement policy numbers in the form of a list if it is an internal Term to Term replacement
	 * 
	 * @param aNbaOinkRequest -
	 *            data request container
	 */
	//SR552543#, APSL3214 New Method
	public void retrieveTermToTermInternalReplPolNumX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List termToTermInternalReplPolicies = NbaUtils.getTermToTermInternalReplPolicies(getNbaTXLife());
		for (int i = 0; i < termToTermInternalReplPolicies.size(); i++) {
			aNbaOinkRequest.addValue(((Policy)termToTermInternalReplPolicies.get(i)).getPolNumber());
		}
	}
	
	
	/**
	 * Obtain the value for a UW result reasons. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.UnderwritingResult.UnderwritingResultReason.
	 * @param aNbaOinkRequest - data request container
	 */
	//APSL2808
	public void retrieveAppUnderwritingResultReasonX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		
		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null && oli.getApplicationInfoExtension() != null){
					ApplicationInfoExtension appInfoExtension = oli.getApplicationInfoExtension();
					List uwResultList = appInfoExtension.getUnderwritingResult();
					for(int i = 0; i < uwResultList.size();i++){
						UnderwritingResult uwResult = (UnderwritingResult) uwResultList.get(i);		
						if(uwResult != null && uwResult.getUnderwritingResultReason() != -1L){
							aNbaOinkRequest.addValue(uwResult.getUnderwritingResultReason());
						}
					}
				}
			}
		}
	}
	
	
 	/**
	 * Obtain the value for a scor Underwriting decision. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.UnderwritingResult.UnderwritingResultReason.
	 * @param aNbaOinkRequest - data request container
	 */
	//APSL2808
	/*public void retrieveSCORUnderWritingDecision(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null && oli.getApplicationInfoExtension() != null){
					ApplicationInfoExtension appInfoExtension = oli.getApplicationInfoExtension();
					aNbaOinkRequest.addValue(appInfoExtension.getSCORUnderWritingDecision());
				}
			}
		}
	}*/
	
 	/**
	 * Obtain the value for a UW result reasons. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.UnderwritingResult.UnderwritingResultReason.
	 * @param aNbaOinkRequest - data request container
	 */
	//APSL2808
	public void retrieveAppUnderwritingResultReasonXCount(NbaOinkRequest aNbaOinkRequest) {
			
		ApplicationInfo api = getApplicationInfo();
		int count = 0;
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null && oli.getApplicationInfoExtension() != null){
					ApplicationInfoExtension appInfoExtension = oli.getApplicationInfoExtension();
					List uwResultList = appInfoExtension.getUnderwritingResult();	
					for(int i = 0; i < uwResultList.size();i++){
						UnderwritingResult uwResult = (UnderwritingResult) uwResultList.get(i);		
						if(uwResult != null && uwResult.getUnderwritingResultReason() != -1L){
							count++;
						}
					}
					aNbaOinkRequest.addValue(count);					
				}
			}
		}
	}
	
	
 	/**
	 * Obtain the value for a UW result reasons source. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.UnderwritingResult.UnderwritingResultReasonSource.
	 * @param aNbaOinkRequest - data request container
	 */
	//APSL2808
	public void retrieveAppUnderwritingReasonSrcX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		
		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null && oli.getApplicationInfoExtension() != null){
					ApplicationInfoExtension appInfoExtension = oli.getApplicationInfoExtension();
					List uwResultList = appInfoExtension.getUnderwritingResult();
					for(int i = 0; i < uwResultList.size();i++){
						UnderwritingResult uwResult = (UnderwritingResult) uwResultList.get(i);		
						if(uwResult != null && uwResult.getUnderwritingResultReason() != -1L){
							aNbaOinkRequest.addValue(uwResult.getUnderwritingReasonSource());
						}						
					}
				}
			}
		}
	}
	
 	/**
	 * Obtain the value for a UW result company assigned code. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.UnderwritingResult.CompanyAssignedCode.
	 * @param aNbaOinkRequest - data request container
	 */
	//APSL2808
	public void retrieveAppUnderwritingCompanyAssignedCodeX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		
		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null && oli.getApplicationInfoExtension() != null){
					ApplicationInfoExtension appInfoExtension = oli.getApplicationInfoExtension();
					List uwResultList = appInfoExtension.getUnderwritingResult();
					for(int i = 0; i < uwResultList.size();i++){
						UnderwritingResult uwResult = (UnderwritingResult) uwResultList.get(i);	
						if(uwResult != null && uwResult.getCompanyAssignedCode() != null && uwResult.getCompanyAssignedCode().trim().length() >0){
							aNbaOinkRequest.addValue(uwResult.getCompanyAssignedCode());
						}
					}
				}
			}
		}
	}
	
 	/**
	 * Obtain the value for a UW result company assigned code source. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.UnderwritingResult.UnderwritingResultReasonSource.
	 * @param aNbaOinkRequest - data request container
	 */
	//APSL2808
	public void retrieveAppUnderwritingCompanyAssignedCodeSrcX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		
		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null && oli.getApplicationInfoExtension() != null){
					ApplicationInfoExtension appInfoExtension = oli.getApplicationInfoExtension();
					List uwResultList = appInfoExtension.getUnderwritingResult();
					for(int i = 0; i < uwResultList.size();i++){
						UnderwritingResult uwResult = (UnderwritingResult) uwResultList.get(i);	
						if(uwResult != null && uwResult.getCompanyAssignedCode() != null && uwResult.getCompanyAssignedCode().trim().length() >0){
							aNbaOinkRequest.addValue(uwResult.getUnderwritingReasonSource());
						}
					}
				}
			}
		}
	}
	
	
 	/**
	 * Obtain the value for a UW result company assigned code count. OLifE.Holding.Policy.ApplicationInfo.ApplicationInfoExtension.UnderwritingResult.UnderwritingResultReasonSource.
	 * @param aNbaOinkRequest - data request container
	 */
	//APSL2808
	public void retrieveAppUnderwritingCompanyAssignedCodeXCount(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		int count = 0;
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null && oli.getApplicationInfoExtension() != null){
					ApplicationInfoExtension appInfoExtension = oli.getApplicationInfoExtension();
					List uwResultList = appInfoExtension.getUnderwritingResult();
					for(int i = 0; i < uwResultList.size();i++){
						UnderwritingResult uwResult = (UnderwritingResult) uwResultList.get(i);	
						if(uwResult != null && uwResult.getCompanyAssignedCode() != null && uwResult.getCompanyAssignedCode().trim().length() >0){
							count++;
							
						}
					}
					aNbaOinkRequest.addValue(count);
				}
			}
		}
	}
	
	/**
	 * Obtain the value for Medical Condition Type OLifE.Party.Risk.MedicalCondition.ConditionType
	 * @param aNbaOinkRequest - data request container
	 */
	//APSL2808 New Method
	public void retrieveMedicalConditionX(NbaOinkRequest aNbaOinkRequest) {
		
		aNbaOinkRequest.setParseMultiple(true);			
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				
				Risk aRisk = getRisk(party);
				if (aRisk != null) {					
					for (int j = 0; i < aRisk.getMedicalConditionCount(); j++) {
						MedicalCondition condition = aRisk.getMedicalConditionAt(j);
						if(condition != null && condition.getConditionType() != -1L){
							aNbaOinkRequest.addValue(condition.getConditionType());
						}
					}				
				}
			}
		}
	}
	
	/**
	 * Obtain the value for MedCond Type Count OLifE.Party.Risk.MedicalCondition.ConditionType
	 * @param aNbaOinkRequest - data request container
	 */
	//APSL2808 New Method
	public void retrieveMedicalConditionXCount(NbaOinkRequest aNbaOinkRequest) {
		int count = 0;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				
				Risk aRisk = getRisk(party);
				if (aRisk != null) {	
					for (int j = 0; i < aRisk.getMedicalConditionCount(); j++) {
						MedicalCondition condition = aRisk.getMedicalConditionAt(j);
						if(condition != null && condition.getConditionType() != -1L){
							count++;
						}
					}
					aNbaOinkRequest.addValue(count);					
				}
			}
		}
	}

	// Begin APSL2735
	public void retrieveACHFirstName(NbaOinkRequest aNbaOinkRequest) {

		NbaParty party = NbaUtils.getPayerParty(getNbaTXLife());
		if (party == null) {
			aNbaOinkRequest.addValue("");
		} else {
			aNbaOinkRequest.addValue(party.getFirstName());
		}
	}

	public void retrieveACHLastName(NbaOinkRequest aNbaOinkRequest) {

		NbaParty party = NbaUtils.getPayerParty(getNbaTXLife());
		if (party == null) {
			aNbaOinkRequest.addValue("");
		} else {
			aNbaOinkRequest.addValue(party.getLastName());
		}
	}

	public void retrieveACHBankName(NbaOinkRequest aNbaOinkRequest) {

		Banking achBanking = NbaUtils.getBankingByHoldingSubType(getNbaTXLife(), NbaOliConstants.OLI_HOLDSUBTYPE_INITIAL);
		if (achBanking != null) {
			BankingExtension aBankingExt = NbaUtils.getFirstBankingExtension(achBanking);
			if (aBankingExt != null) {
				aNbaOinkRequest.addValue(aBankingExt.getBankName());
			} else {
				aNbaOinkRequest.addValue("");
			}

		} else {
			aNbaOinkRequest.addValue("");
		}
	}

	public void retrieveACHOwnerType(NbaOinkRequest aNbaOinkRequest) {

		NbaParty party = NbaUtils.getPayerParty(getNbaTXLife());
		if (party == null) {
			aNbaOinkRequest.addValue("");
		} else {
			aNbaOinkRequest.addValue(party.getPartyTypeCode());
		}

	}

	public void retrieveACHInstitutionalName(NbaOinkRequest aNbaOinkRequest) {

		NbaParty party = NbaUtils.getPayerParty(getNbaTXLife());
		if (party == null) {
			aNbaOinkRequest.addValue("");
		} else {
			aNbaOinkRequest.addValue(party.getDBA());
		}

	}
   // End APSL2735
	
	//APSL2808
	/**
	 * Set PHI CV Indicator
	 */
	public void retrievePHICVInd(NbaOinkRequest aNbaOinkRequest) {
		
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE());		
		ArrayList messages =  holding.getSystemMessage();
		if (NbaUtils.hasSignificantValErrors(messages, NbaConstants.SUBSET_SI_PHI)) {
			aNbaOinkRequest.addValue("true");
		}
	}
	
	//APSL2808
	/**
	 * Set Significant Validation Error Indicator if CV's other that SI Agent CV's are present
	 * APSL3382 CDN letters for overridable CV's
	 */
	public void retrieveNonAgentSignificantCVInd(NbaOinkRequest aNbaOinkRequest) {		
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE());		
		ArrayList messages =  holding.getSystemMessage();
		if (NbaUtils.hasSignificantCVOtherThanSubset(messages, NbaConstants.SUBSET_SI_AGENT)) {
			aNbaOinkRequest.addValue("true");
		}else{
			aNbaOinkRequest.addValue("false"); 
		}
	}
	
	//APSL2808 , APSL3195(QC12002)
	/**
	 * Set PWA Agent Phone 
	 */
	
	public void retrieveAgentPhone(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Phone offPhone = getPhoneForType(aParty, NbaOliConstants.OLI_PHONETYPE_BUS);
				Phone homePhone = getPhoneForType(aParty, NbaOliConstants.OLI_PHONETYPE_HOME);
				Phone returnPhone = null;
				if (offPhone != null && offPhone.getPrefPhone()) {
					returnPhone = offPhone;
				}else if(homePhone != null && homePhone.getPrefPhone()) {
					returnPhone = homePhone;
				}else if(offPhone != null) {
					returnPhone = offPhone;
				}else if(homePhone != null) {
					returnPhone = homePhone;
				}
				StringBuffer phoneNumber = new StringBuffer();
				if(returnPhone != null) {
					if (returnPhone.hasAreaCode()) {
						phoneNumber.append(returnPhone.getAreaCode());
					}
					if (returnPhone.hasDialNumber()) {
						phoneNumber.append(returnPhone.getDialNumber());
					}
				}
				aNbaOinkRequest.addValue(phoneNumber.toString(), FORMAT_TYPE_PHONE);
			} 
		}
	}
	
	//APSL2808
	/**
	 * Set PHI CV Indicator for TRY cases
	 */
	public void retrievePHITryCVInd(NbaOinkRequest aNbaOinkRequest) {
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE());		
		ArrayList messages =  holding.getSystemMessage();		
		if (NbaUtils.hasSignificantValErrors(messages, NbaConstants.SUBSET_SI_PHI)) {			
			NbaParty primaryParty = getNbaTXLife().getPrimaryParty();
			if(primaryParty != null ){
				List priorInsContractList = NbaUtils.getPriorInsuranceContracts(primaryParty.getParty());
				for(int listSize = 0; listSize < priorInsContractList.size();listSize++){
					NbaTXLife priorInsTXLife = (NbaTXLife) priorInsContractList.get(listSize);
					OLifE oLife = priorInsTXLife.getOLifE();
					List relationList = oLife.getRelation();
					if (relationList != null) {
						for (int i = 0; i < relationList.size(); i++) {
							Relation relation = (Relation) relationList.get(i);
							if (relation != null && relation.hasRelationRoleCode()
									&& (relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_TRY)) {
								RelationExtension relationExtension = NbaUtils.getFirstRelationExtension(relation);
								if (relationExtension != null && relationExtension.hasUWResolution()){
									aNbaOinkRequest.addValue("false");
									return;
								}else{
									aNbaOinkRequest.addValue("true");
									return;
								}
							}
						}
					}
				}
			}			
		}
	}
	//SR741618 APSL3204, APSL4105(QC14542)
	public void retrieveisEarcAgent(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException
	{
		String producerid ="";
		Relation bgaRelation = getNbaTXLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_GENAGENT);
		Relation relation = getNbaTXLife().getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_PRIMAGENT);
		NbaParty party = null;
		if(relation != null){
			party = getNbaTXLife().getParty(relation.getRelatedObjectID());
		}else if (bgaRelation != null){
			party = getNbaTXLife().getParty(bgaRelation.getRelatedObjectID());
		}
		if (party != null) {
			CarrierAppointment carrierAppointment = party.getParty().getProducer().getCarrierAppointmentAt(0);
			if (carrierAppointment != null) {
				producerid=carrierAppointment.getCompanyProducerID();
			}
		}
		if(NbaUtils.isEarcAgent(producerid))
			aNbaOinkRequest.addValue(NbaConstants.TRUE_STR);
		else
			aNbaOinkRequest.addValue(NbaConstants.FALSE_STR);
	}

	// Begin APSL3262
	public void retrieveACHEmailId(NbaOinkRequest aNbaOinkRequest) {
		NbaParty party = NbaUtils.getPayerParty(getNbaTXLife());
		if (party != null) {
			EMailAddress anEMailAddress = party.getEmailAddress(NbaOliConstants.OLI_EMAIL_BUSINESS);
			if (anEMailAddress == null) {
				aNbaOinkRequest.addUnknownValue("");
			} else {
				aNbaOinkRequest.addValue(anEMailAddress.getAddrLine());
			}
		}
	}
  // End APSL3262
	
	//APSL3258 New Method
	public void retrieveForeignNationalityInd(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			PartyExtension partyextension = NbaUtils.getFirstPartyExtension(party);
			if (partyextension != null) {
				aNbaOinkRequest.addValue(partyextension.getForeignNationalityInd());
			}
		}
	}
	
	//New Method ALII2019
	public void retrievePaymentDraftFailureReason(NbaOinkRequest aNbaOinkRequest) {
		FinancialActivity financialActivity = (FinancialActivity) getNbaTXLife().getPolicy().getFinancialActivity().get(0);
		FinancialActivityExtension financialActivityExtension = NbaUtils.getFirstFinancialActivityExtension(financialActivity);
		if (financialActivityExtension != null) {
			aNbaOinkRequest.addValue(financialActivityExtension.getPaymentDraftFailureReason());
		}
	}
	
	// Begin APSL3388
	
	/**
	 * Obtain the value for MailingLine1, an instance of Line1 for an Address with type code = 17. OLifE.Party.Address.Line1 is the first line of the
	 * address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveMailingLine1(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_MAILING);
				if (address == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(address.getLine1());
				}
			} else {
				break;
			}
		}
	}
	
	/**
	 * Obtain the value for MailingLine2, an instance of Line2 for an Address with type code = 17. OLifE.Party.Address.Line2 is the second line of the
	 * address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveMailingLine2(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_MAILING);
				if (address == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(address.getLine2());
				}
			} else {
				break;
			}
		}
	}
	/**
	 * Obtain the value for MailingZip, an instance of Zip for an Address with type code = 17. OLifE.Party.Address.Zip is the zip code, postal code, etc.
	 * (country dependent)
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveMailingZip(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_MAILING);
				if (address == null) {
					aNbaOinkRequest.addValue("", FORMAT_TYPE_ZIP);
				} else {
					aNbaOinkRequest.addValue(address.getZip(), FORMAT_TYPE_ZIP);
				}
			} else {
				break;
			}
		}
	}
	
	/**
	 * Obtain the value for MailingCity, an instance of City for an Address with type code = 17. OLifE.Party.Address.City is the city of the address.
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveMailingCity(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_MAILING);
				if (address == null) {
					aNbaOinkRequest.addUnknownValue("");
				} else {
					aNbaOinkRequest.addValue(address.getCity());
				}
			} else {
				break;
			}
		}
	}
	//End APSL3388	
	
	/**
	 * Obtain the value for PredManualTriggerInd Holding.Policy.PolicyExtension.PredManualTriggerInd
	 */
	//ALII1981(QC11754)
	public void retrievePredManualTriggerInd(NbaOinkRequest aNbaOinkRequest) {
		Policy policy = getPolicy();
		if (policy != null) {
			PolicyExtension policyextension = NbaUtils.getFirstPolicyExtension(policy);
			if (policyextension != null && policyextension.hasPredManualTriggerInd()) {
				aNbaOinkRequest.addValue(policyextension.getPredManualTriggerInd());
			}
		}
	}
	
	/**
	 * Obtain the value for ProcessFirmAXAInd Holding.Policy.ApplicationInfo.ApplicationInfoExtension.ProcessFirmAXAInd
	 */
	//APSL3447
	public void retrieveProcessFirmAXAInd(NbaOinkRequest aNbaOinkRequest) {
		Policy policy = getPolicy();
		if (policy != null) {
			ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(policy.getApplicationInfo());
			if (applicationInfoExtension != null && applicationInfoExtension.hasProcessFirmAXAInd()) { 
				aNbaOinkRequest.addValue(applicationInfoExtension.getProcessFirmAXAInd());
			}
		}
	}
	/**
	 * Identify the RelationRoleCode of Pending Information Recipient.
	 */
	//APSL3447
	public void retrievePIRExistsInd(NbaOinkRequest aNbaOinkRequest) {
		int relationCnt = getOLifE().getRelationCount();
		boolean pirExists = false;
		for (int j = 0; j < relationCnt; j++) {
			Relation aRelation = getOLifE().getRelationAt(j);
			if (aRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_PEND_INFO_RECIPIENT) {
				pirExists = true;
				break;
			}
		}
		aNbaOinkRequest.addValue(pirExists);
	}
	/**
	 * Identify the RelationRoleCode of Processing Firm.
	 */
	//APSL3447
	public void retrieveProcessFirmExistsInd(NbaOinkRequest aNbaOinkRequest) {
		int relationCnt = getOLifE().getRelationCount();
		boolean procFirmExists = false;
		for (int j = 0; j < relationCnt; j++) {
			Relation aRelation = getOLifE().getRelationAt(j);
			if (aRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_PROCESSINGFIRM) {
				procFirmExists = true;
				break;
			}
		}
		aNbaOinkRequest.addValue(procFirmExists);
	}
	/**
	 * Identify the RelationRoleCode of Contract Firm.
	 */
	//APSL3447
	public void retrieveContractFirmExistInd(NbaOinkRequest aNbaOinkRequest) {
		int relationCnt = getOLifE().getRelationCount();
		boolean contractFirmExists = false;
		for (int j = 0; j < relationCnt; j++) {
			Relation aRelation = getOLifE().getRelationAt(j);
			if (aRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_CONTRACTINGFIRM) {
				contractFirmExists = true;
				break;
			}
		}
		aNbaOinkRequest.addValue(contractFirmExists);
	}
	
	//APSL3619 New Method
	// APSL 4410 modified the whole method
	//APSL3619 New Method
	// APSL 4410 modified the whole method
	public void retrieveHIPAAIgoInd(NbaOinkRequest aNbaOinkRequest) {
		boolean igoInd = false;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party party = getParty(aNbaOinkRequest, i);
			if (party != null) {
				Object[] relations = getOLifE().getRelation().toArray();
				Relation relation = NbaUtils.getRelationForParty(party.getId(), relations);
				boolean xsameAsOwner = relation.getRelationRoleCode() == NbaOliConstants.OLI_PARTICROLE_32 ? getNbaTXLife().isOwnerSameAsPrimaryIns()
						: getNbaTXLife().isOwnerSameAsJointIns();
				long xSignatureRoleCode = relation.getRelationRoleCode() == NbaOliConstants.OLI_PARTICROLE_32 ? NbaOliConstants.OLI_PARTICROLE_PRIMARY
						: NbaOliConstants.OLI_PARTICROLE_JOINT;
				Date appSignedDate = getApplicationInfo().getSignedDate();
				SignatureInfo signInfo = NbaUtils.getSignatureInfo(getApplicationInfo(), xSignatureRoleCode, NbaOliConstants.OLI_SIGTYPE_BLANKETAUTH);
				if (!NbaUtils.isBlankOrNull(signInfo.getSignatureDate())  //APSL4741 removed stat and city blank check
						&& (!NbaUtils.isBlankOrNull(appSignedDate) && !signInfo.getSignatureDate().after(new Date()) && NbaUtils.calcMonthsDiff(
								appSignedDate, signInfo.getSignatureDate()) <= 24) && (xsameAsOwner)) { // APSL4273
					if (!nbaTXLife.isSIApplication()) { // APSL4464(QC15850)
						if (matchWithHipaaParty(party)) {// APSL4464(QC15850)
							igoInd = true;
						}
					} else {
						igoInd = true;
					}
				}
			}
		}
		aNbaOinkRequest.addValue(igoInd);
	}
	
	
	//APSL3530
	public void retrieveFinActivityDateRolloverEXT1035(NbaOinkRequest aNbaOinkRequest) {
		Date finEffDate=null;
		int i = 0;
		while (true) {
			FinancialActivity activity = getFinancialActivity(i++);
			if (activity == null) {
				break;
			} else {
				if (activity.getFinActivityType() == NbaOliConstants.OLI_FINACT_ROLLOVEREXT1035) { // ALII1113
					finEffDate = activity.getFinEffDate();
					break;
				}
			}
		}
		aNbaOinkRequest.addValue(finEffDate);
	}
	
	/**
	 * Obtain the value for ReceivedDate. OLifE.Holding.Policy.RequirementInfo.ReceivedDate as a List.
	 * @param aNbaOinkRequest
	 */
	// APSL3525 New Method
	public void retrieveReceivedDateX(NbaOinkRequest aNbaOinkRequest){
		aNbaOinkRequest.setParseMultiple(true);
		List reqInfoList = getNbaTXLife().getPolicy().getRequirementInfo();
		for(int i=0; i<reqInfoList.size(); i++){
			RequirementInfo reqInfo = (RequirementInfo)reqInfoList.get(i);
			if(reqInfo != null ){
				aNbaOinkRequest.addValue(reqInfo.getReceivedDate());
			}
		}
	}
	
	//SR777850(APSL3719)
	public void retrieve1035FundsNotRcvdOrRcvdWithoutMIP(NbaOinkRequest aNbaOinkRequest){		
		int finActCount = 0;
		double grossAmount = 0;
		Policy policy = getPolicy();
		if (policy != null) {
			finActCount = policy.getFinancialActivityCount() ;			
			for(int i=0;i<finActCount;i++){
				FinancialActivity activity = policy.getFinancialActivityAt(i);
				if(activity !=null && activity.getFinActivityType() == NbaOliConstants.OLI_FINACT_ROLLOVEREXT1035 || activity.getFinActivityType() == NbaOliConstants.OLI_FINACT_1035INIT || activity.getFinActivityType() == NbaOliConstants.OLI_FINACT_1035SUBS) { 
					grossAmount += activity.getFinActivityGrossAmt();							
				}
			}			
			if(policy.hasMinPremiumInitialAmt() && (policy.getMinPremiumInitialAmt() > 0)&& (grossAmount - policy.getMinPremiumInitialAmt() < 0)){ //APSL3811(QC13733)
				aNbaOinkRequest.addValue("true");
			}else{
				aNbaOinkRequest.addValue("false");
			}		
		}
	}
	
	//APSL3619 New Method
	public void retrieveReplacementNYFormIgo(NbaOinkRequest aNbaOinkRequest) {
        boolean flag = false;
        if (getApplicationInfo().getApplicationJurisdiction() == 37) {
            Party party = getPartyForPrimaryIns();
            if (null != party && party.getClient() != null && NbaUtils.getFirstClientExtension(party.getClient()) != null) {
                ClientExtension clientExe = NbaUtils.getFirstClientExtension(party.getClient());
                if (clientExe.hasOtherInsAXACompanyIndCode() && NbaUtils.isAnsweredNo(clientExe.getOtherInsAXACompanyIndCode())
                        && clientExe.hasOtherCarrierApplPendIndCode() && NbaUtils.isAnsweredNo(clientExe.getOtherCarrierApplPendIndCode())
                        && clientExe.hasEffectOnExistingInsIndCode() && NbaUtils.isAnsweredNo(clientExe.getEffectOnExistingInsIndCode())) {
                    flag = true;
                }
            }
            if (flag) {
                ArrayList formInstanceList = getOLifE().getFormInstance();
                int formInstanceListSize = formInstanceList.size();
                for (int i = 0; i < formInstanceListSize; i++) {
                    FormInstance formInstance = (FormInstance) formInstanceList.get(i);
                    if (NbaConstants.FORM_NAME_REPLNY.equalsIgnoreCase(formInstance.getFormName()) && formInstance.getFormResponseCount() > 0) {
                        Iterator formResItr = formInstance.getFormResponse().iterator();
                        while (formResItr.hasNext()) {
                            FormResponse formResponse = (FormResponse) formResItr.next();
                            if (!NbaUtils.isAnsweredNo(formResponse.getResponseCode())) {
                                flag = false;
                                break;
                            }
                        }
                        break;
                    }
                }

            }
        }
        aNbaOinkRequest.addValue(flag);
    }
	//APSL3619 New Method
	public void retrieveRequirementInAddStatus(NbaOinkRequest aNbaOinkRequest){
		boolean flag = false; 
		List reqList = getRequirementInfosByReqCode(aNbaOinkRequest);
		int count =0;
		for(int i = 0; i < reqList.size();i++){
			RequirementInfo requireInfo = (RequirementInfo)reqList.get(i);
			if(requireInfo != null && requireInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_ADD){
				count++;
			}
		}
		if(count > 0){
			flag = true;
		}
		aNbaOinkRequest.addValue(flag);
	}
	
	
	//APSL3818(SR805869) LTCSR CA New Method
	public void retrieveAckCheckboxUncheckedX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);		
		boolean flag = false;
		
		//Check if LTC CA form is present 
		if (getApplicationInfo().getApplicationJurisdiction() == NbaOliConstants.OLI_USA_CA) {			
			RequirementInfo rInfo = getNbaTXLife().getRequirementInfo(getNbaTXLife().getPrimaryParty(),NbaOliConstants.OLI_REQCODE_1009800084);
			if(rInfo!=null){
				if (NbaConstants.FORM_AXA_LTC_2012_CA.equalsIgnoreCase(rInfo.getFormNo())){
					flag = true;
				}
			}

		}
		//If LTC CA form is present check if acknowledgement checkboxes are checked. 
		if(flag){
			Party aParty = getPartyForPrimaryIns();
			if (aParty != null) {				
				Client client = aParty.getClient();
				if (client != null) {
					ClientExtension clientExt = NbaUtils.getFirstClientExtension(client);
					if (clientExt != null) {
						if (clientExt.getClientAcknowledgeInfo() != null && clientExt.getClientAcknowledgeInfo().getLTCReqFormsCC() != null) {							
							if(clientExt.getClientAcknowledgeInfo().getLTCReqFormsCC().getLTCReqFormsTC() != null && clientExt.getClientAcknowledgeInfo().getLTCReqFormsCC().getLTCReqFormsTCCount() > 0){								
								for(int i=0;i< clientExt.getClientAcknowledgeInfo().getLTCReqFormsCC().getLTCReqFormsTCCount();i++){
									long ltcReqFormTCValue = clientExt.getClientAcknowledgeInfo().getLTCReqFormsCC().getLTCReqFormsTCAt(i);									
									aNbaOinkRequest.addValue(ltcReqFormTCValue);
								}								
							}
						}
					}
				}
			}
		}
	}
	
	//APSL3818(SR805869) LTCSR CA New Method
	public void retrieveLTCCAFormPresent(NbaOinkRequest aNbaOinkRequest) {
		//Check if LTC CA form is present 
		boolean flag = false;
		if (getApplicationInfo().getApplicationJurisdiction() == NbaOliConstants.OLI_USA_CA) {
			RequirementInfo rInfo = getNbaTXLife().getRequirementInfo(getNbaTXLife().getPrimaryParty(),NbaOliConstants.OLI_REQCODE_1009800084);
			if(rInfo!=null){
				if (NbaConstants.FORM_AXA_LTC_2012_CA.equalsIgnoreCase(rInfo.getFormNo())){
					flag = true;
				}
			}
		}
		
		if(flag){
			aNbaOinkRequest.addValue("true");
		}else{
			aNbaOinkRequest.addValue("false");
		}
	}
	
	// APSL4070 New method
	public void retrieveCaseManagerQueue(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo applicationInfo = getApplicationInfo();
		if (applicationInfo != null) {
			aNbaOinkRequest.addValue(applicationInfo.getNBContactName());
		}
	}
	
	// APSL4036 New Method
	/**
	 * gets the LTCReplacementInd value from LTC Replacement Question Mapping:
	 * FormInstance details within TXLife.TXLifeRequest.OLifE.FormInstance where FormName = LTCSupp
	 * TXLife.TXLifeRequest.OLifE.FormInstance.FormResponse.OLifEExtension.FormResponseExtension.QuestionTypeAbbr = replaceOrIntentLTC
	 * TXLife.TXLifeRequest.OLifE.FormInstance.FormResponse.ResponseCode = 1
	 * @param aNbaOinkRequest
	 *            - data request container
	 */
	
	public void retrieveLTCReplResponseCode(NbaOinkRequest aNbaOinkRequest) {
		FormInstance formInstance = null;
		ArrayList formResponseList = null;
		FormResponse formResponse = null;
		FormResponseExtension formResponseExtension = null;
		ArrayList formInstanceList = getOLifE().getFormInstance();
		int count = formInstanceList.size();
		for (int i = 0; i < count; i++) {
			formInstance = (FormInstance) formInstanceList.get(i);
			if (formInstance != null && (formInstance.hasFormName()) && (NbaConstants.FORM_NAME_LTCSUPP.equals(formInstance.getFormName()))) {
				formResponseList = formInstance.getFormResponse();
				for (int j = 0; j < formResponseList.size(); j++) {
					formResponse = (FormResponse) formResponseList.get(j);
					formResponseExtension = NbaUtils.getFirstFormResponseExtension(formResponse);
					if (formResponseExtension != null && formResponseExtension.hasQuestionTypeAbbr()
							&& formResponseExtension.getQuestionTypeAbbr().equalsIgnoreCase(NbaConstants.LTCS_REPLACEORINTENTLTC)) {
						aNbaOinkRequest.addValue(formResponse.getResponseCode());
					}
				}
			}
		}
	}	
	
	/**
	 * Retrieves the CreatedDate value based on the requirement id filter
	 * OLifE.Holding.Policy.RequirementInfo.RequirementInfoExtension.CreatedDate
	 * @param aNbaOinkRequest - data request container
	 */
	// APSL4087-SR835575 New Method
	public void retrieveCreatedDate(NbaOinkRequest aNbaOinkRequest) {
		String reqIdFilter = aNbaOinkRequest.getRequirementIdFilter();
		Policy policy = getPolicy();
		ArrayList reqInfoList = new ArrayList();
		int listSize = 0;
		RequirementInfo reqInfo = null;
		if (policy != null) {
			reqInfoList = policy.getRequirementInfo();
			listSize = reqInfoList.size();
			for (int i = 0; i < listSize; i++) {
				reqInfo = (RequirementInfo) reqInfoList.get(i);
				if (reqInfo != null && reqInfo.getId().equals(reqIdFilter)) {
					break;
				}
			}
			if (reqInfo != null) {
				int index_extension = getExtensionIndex(reqInfo.getOLifEExtension(), REQUIREMENTINFO_EXTN);
				if (index_extension != -1) {
					RequirementInfoExtension extension = reqInfo.getOLifEExtensionAt(index_extension).getRequirementInfoExtension();
					if (extension != null) {
						aNbaOinkRequest.addValue(extension.getCreatedDate());
					}
				}
			}
		}
	}
	
	/**
	 * Obtain the value for CWA or Initial premium amount value. OLifE.Holding.Policy.ApplicationInfo.CWAAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//APSL4287 New Method
	public void retrieveCWAAndInitPremiumAmt(NbaOinkRequest aNbaOinkRequest) {
		double totalCWA = 0L;
		if (getPolicy() != null) {
			int finCount = getPolicy().getFinancialActivityCount();
			long finActivitySubType = 0;
			long finActivityType = 0;
			for (int i = 0; i < finCount; i++) {
				FinancialActivity financialActivity = getPolicy().getFinancialActivityAt(i);
				finActivityType = financialActivity.getFinActivityType();
				finActivitySubType = financialActivity.getFinActivitySubType();
				boolean paymentAddition = false;
				if (finActivityType == NbaOliConstants.OLI_FINACT_CWA) { // APSL4585
					paymentAddition = true;
					FinancialActivityExtension financialActivityExtension = NbaUtils.getFirstFinancialActivityExtension(financialActivity);
					if (financialActivityExtension != null) {
						if (financialActivityExtension.getDisbursedInd()) {
							paymentAddition = false;
						}
						if (NbaOliConstants.OLI_FINACTSUB_REV == finActivitySubType || NbaOliConstants.OLI_FINACTSUB_REFUND == finActivitySubType
								|| NbaOliConstants.OLI_FINACTSUB_PARTIALREFUND == finActivitySubType) {
							paymentAddition = false;
						}
					}
					if (paymentAddition) {
						totalCWA = (double) Math.round((totalCWA + financialActivity.getFinActivityGrossAmt()) * 100) / 100; 
					}
				}
			}
		}
		aNbaOinkRequest.addValue(totalCWA, FORMAT_TYPE_CURRENCY);
	}
	
	/**
	 * Obtain the value for CWA or Initial premium amount value. OLifE.Holding.Policy.ApplicationInfo.CWAAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//APSL4287 New Method
	public void retrieveCWAAndInitPremiumAnd1035Amt(NbaOinkRequest aNbaOinkRequest) {
		double totalCWA = 0L;
		if (getPolicy() != null) {
			int finCount = getPolicy().getFinancialActivityCount();
			long finActivitySubType = 0;
			long finActivityType = 0;
			for (int i = 0; i < finCount; i++) {
				FinancialActivity financialActivity = getPolicy().getFinancialActivityAt(i);
				finActivityType = financialActivity.getFinActivityType();
				finActivitySubType = financialActivity.getFinActivitySubType();
				boolean paymentAddition = false;
				if (finActivityType == NbaOliConstants.OLI_FINACT_CWA || finActivityType == NbaOliConstants.OLI_FINACT_PREMIUMINIT  || finActivityType == NbaOliConstants.OLI_FINACT_ROLLOVEREXT1035) {
					paymentAddition = true;
					FinancialActivityExtension financialActivityExtension = NbaUtils.getFirstFinancialActivityExtension(financialActivity);
					if (financialActivityExtension != null) {
						if (financialActivityExtension.getDisbursedInd()) {
							paymentAddition = false;
						}
						if (NbaOliConstants.OLI_FINACTSUB_REV == finActivitySubType || NbaOliConstants.OLI_FINACTSUB_REFUND == finActivitySubType
								|| NbaOliConstants.OLI_FINACTSUB_PARTIALREFUND == finActivitySubType) {
							paymentAddition = false;
						}
					}
					if (paymentAddition) {
						totalCWA = (double) Math.round((totalCWA + financialActivity.getFinActivityGrossAmt()) * 100) / 100; 
					}
				}
			}
		}
		aNbaOinkRequest.addValue(totalCWA, FORMAT_TYPE_CURRENCY);
	}
	

	/**
	 * Obtain the value for CWA or Initial premium amount value. OLifE.Holding.Policy.ApplicationInfo.CWAAmt
	 * @param aNbaOinkRequest - data request container
	 */
	//APSL4287 New Method
	public void retrieveCWAFinAmt(NbaOinkRequest aNbaOinkRequest) {
		double totalCWA = 0L;
		if (getPolicy() != null) {
			int finCount = getPolicy().getFinancialActivityCount();
			long finActivitySubType = 0;
			long finActivityType = 0;
			for (int i = 0; i < finCount; i++) {
				FinancialActivity financialActivity = getPolicy().getFinancialActivityAt(i);
				finActivityType = financialActivity.getFinActivityType();
				finActivitySubType = financialActivity.getFinActivitySubType();
				boolean paymentAddition = false;
				if (finActivityType == NbaOliConstants.OLI_FINACT_CWA) {
					paymentAddition = true;
					FinancialActivityExtension financialActivityExtension = NbaUtils.getFirstFinancialActivityExtension(financialActivity);
					if (financialActivityExtension != null) {
						if (financialActivityExtension.getDisbursedInd()) {
							paymentAddition = false;
						}
						if (NbaOliConstants.OLI_FINACTSUB_REV == finActivitySubType || NbaOliConstants.OLI_FINACTSUB_REFUND == finActivitySubType
								|| NbaOliConstants.OLI_FINACTSUB_PARTIALREFUND == finActivitySubType) {
							paymentAddition = false;
						}
					}
					if (paymentAddition) {
						totalCWA = (double) Math.round((totalCWA + financialActivity.getFinActivityGrossAmt()) * 100) / 100; 
					}
				}
			}
		}
		aNbaOinkRequest.addValue(totalCWA, FORMAT_TYPE_CURRENCY);
	}
	
	/**
	 * Obtain the financial activity effective date when Carry Over Loan was applied
	 * @param aNbaOinkRequest - data request container
	 */
	//APSL4287 New Method
	public void retrieveCarryOverLoanAppliedDate(NbaOinkRequest aNbaOinkRequest) {
		Date finEffDate = null;
		int i = 0;
		FinancialActivity activity =null;
		FinancialActivityExtension finActivityExtension = null;
		boolean isDisbursed = false;
		while (true) {
			activity = getFinancialActivity(i++);
			if (activity == null) {
                break;
            }
			finActivityExtension = NbaUtils.getFirstFinancialActivityExtension(activity);
			isDisbursed = false;
			if(finActivityExtension != null){
				isDisbursed = finActivityExtension.getDisbursedInd();
			}			
			if(activity.getFinActivitySubType() != NbaOliConstants.OLI_FINACTSUB_REV &&
					activity.getFinActivitySubType() != NbaOliConstants.OLI_FINACTSUB_REFUND &&
					activity.getFinActivitySubType() != NbaOliConstants.OLI_FINACTSUB_PARTIALREFUND && isDisbursed  != true){
				if (activity.getFinActivityType() == NbaOliConstants.OLI_FINACT_CARRYOVERLOAN) {
					finEffDate = activity.getFinEffDate();
					break;
				}
			}
		}
		aNbaOinkRequest.addValue(finEffDate);
	}
	
	//APSL4287 New Method
	public void retrieveCWAAppliedDate(NbaOinkRequest aNbaOinkRequest) {
		Date finEffDate = null;
		int i = 0;
		while (true) {
			FinancialActivity activity = getFinancialActivity(i++);
			if (activity == null) {
				break;
			}
			if (activity.getFinActivityType() == NbaOliConstants.OLI_FINACT_CWA) {
				finEffDate = activity.getFinEffDate();
				break;
			}
		}
		aNbaOinkRequest.addValue(finEffDate);
	}
	
	//APSL4287 New Method
	public void retrieveInitialPremAppliedDate(NbaOinkRequest aNbaOinkRequest) {
		Date finEffDate = null;
		int i = 0;
		while (true) {
			FinancialActivity activity = getFinancialActivity(i++);
			if (activity == null) {
				break;
			}
			if (activity.getFinActivityType() == NbaOliConstants.OLI_FINACT_PREMIUMINIT) {
				finEffDate = activity.getFinEffDate();
				break;
			}
		}
		aNbaOinkRequest.addValue(finEffDate);
	}
	/**
	 * Check if it is 1035 combo case
	 * @param aNbaOinkRequest - data request container
	 */
	//APSL4287 New Method
	public void retrieveExch1035ComboInd(NbaOinkRequest aNbaOinkRequest) {
		boolean isInternal = false, isExternal = false;
		List releationList = getNbaTXLife().getOLifE().getRelation();
		Relation relation = null;
		if (getNbaTXLife().is1035Exchange()) {
			for (int i = 0; i < releationList.size(); i++) {
				relation = (Relation) releationList.get(i);
				if (NbaOliConstants.OLI_REL_HOLDINGCO == relation.getRelationRoleCode()) {
					NbaParty nbaParty = nbaTXLife.getParty(relation.getRelatedObjectID());
					String replacedPartyKey = nbaParty.getParty().getPartyKey();
					if (replacedPartyKey != null) {
						if (!NbaUtils.isInternalReplacementCompany(replacedPartyKey)) {
							isExternal = true;
						} else {
							isInternal = true;
						}
					}
				}
			}
		}
		if (isInternal && isExternal){
			aNbaOinkRequest.addValue(true);
		} else {
			aNbaOinkRequest.addValue(false);
		}
	}
	
	   /**
     * Check if it is 1035 combo case
     * @param aNbaOinkRequest - data request container
     */
    //APSL4585 New Method
	public void retrieveExch1035ComboIndForPED(NbaOinkRequest aNbaOinkRequest) {
		boolean isInternal = false, isExternal = false;
		List releationList = getNbaTXLife().getOLifE().getRelation();
		Relation relation = null;
		if (releationList != null) {
			for (int i = 0; i < releationList.size(); i++) {
				relation = (Relation) releationList.get(i);
				if (NbaOliConstants.OLI_REL_HOLDINGCO == relation.getRelationRoleCode() && NbaOliConstants.OLI_HOLDING==relation.getOriginatingObjectType() ) {
					Holding holding = nbaTXLife.getHolding(relation.getOriginatingObjectID());
					Policy replPolicy =holding.getPolicy();
					LifeUSAExtension lifeUSAextn = NbaUtils.getFirstLifeUSAExtension(replPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty()
							.getLife().getLifeUSA());
					if (lifeUSAextn != null) {
						long exch1035IndCode = lifeUSAextn.getExch1035IndCode();
						if (exch1035IndCode == NbaOliConstants.NBA_ANSWERS_YES){
							NbaParty nbaParty = nbaTXLife.getParty(relation.getRelatedObjectID());
							String replacedPartyKey = nbaParty.getParty().getPartyKey();
							if (replacedPartyKey != null) {
								if (!NbaUtils.isInternalReplacementCompany(replacedPartyKey)
										|| NbaConstants.AXA_COMPANY_MONY002.equalsIgnoreCase(replacedPartyKey)) {
									isExternal = true;
								} else {
									isInternal = true;
								}
							}

						}
					}
				}
			}
		}
		if (isInternal && isExternal) {
			aNbaOinkRequest.addValue(true);
		} else {
			aNbaOinkRequest.addValue(false);	
		}
	}

	
   
    
    /**
     * Check if it is 1035  case
     * @param aNbaOinkRequest - data request container
     */
    //APSL4585 New Method
    public void retrieveExch1035IndCodeForPED(NbaOinkRequest aNbaOinkRequest) {
        boolean is1035ExchCase = false;
        List replHoldingList = NbaUtils.getHoldingByRoleCode(getNbaTXLife(), NbaOliConstants.OLI_REL_REPLACEDBY);
        if (null != replHoldingList) {
            int replHoldingCount = replHoldingList.size();
            for (int i = 0; i < replHoldingCount; i++) {
                Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
                LifeUSAExtension lifeUSAextn = NbaUtils.getFirstLifeUSAExtension(replPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty()
                        .getLife().getLifeUSA());
                if (lifeUSAextn != null) {
                    long exch1035IndCode = lifeUSAextn.getExch1035IndCode();
                    if (exch1035IndCode == NbaOliConstants.NBA_ANSWERS_YES) {
                        is1035ExchCase = true;
                        break;
                    }
                }
            }
        }
        aNbaOinkRequest.addValue(is1035ExchCase);
    }
    
    
   
    /**
     * Check if it is 1035  case
     * @param aNbaOinkRequest - data request container
     */
	 
    //APSL4585 New Method
    public void retrieveInt1035IndCode(NbaOinkRequest aNbaOinkRequest) {
    	 boolean isInternal = false;
    		List releationList = getNbaTXLife().getOLifE().getRelation();
    		Relation relation = null;
    		if (releationList != null) {
    			for (int i = 0; i < releationList.size(); i++) {
    				relation = (Relation) releationList.get(i);
    				if (NbaOliConstants.OLI_REL_HOLDINGCO == relation.getRelationRoleCode() && NbaOliConstants.OLI_HOLDING==relation.getOriginatingObjectType()) {
    					Holding holding = nbaTXLife.getHolding(relation.getOriginatingObjectID());
    					Policy replPolicy =holding.getPolicy();
    					LifeUSAExtension lifeUSAextn = NbaUtils.getFirstLifeUSAExtension(replPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty()
    							.getLife().getLifeUSA());
    					if (lifeUSAextn != null) {
    						long exch1035IndCode = lifeUSAextn.getExch1035IndCode();
    						if (exch1035IndCode == NbaOliConstants.NBA_ANSWERS_YES){
    							NbaParty nbaParty = nbaTXLife.getParty(relation.getRelatedObjectID());
    							String replacedPartyKey = nbaParty.getParty().getPartyKey();
    							if (replacedPartyKey != null) {
    								if (NbaUtils.isInternalReplacementCompany(replacedPartyKey) 
    							             && !NbaConstants.AXA_COMPANY_MONY002.equalsIgnoreCase(replacedPartyKey)) {
    							         isInternal = true;
    							     } 
    							}

    						}
    					}
    				}
    			}
    		}
        aNbaOinkRequest.addValue(isInternal);
    }
    
    /**
     * Check if it is 1035  case
     * @param aNbaOinkRequest - data request container
     */
    
    //APSL4585 New Method
   public void retrieveExt1035IndCode(NbaOinkRequest aNbaOinkRequest) {
	   boolean  isExternal = false;
		List releationList = getNbaTXLife().getOLifE().getRelation();
		Relation relation = null;
		if (releationList != null) {
			for (int i = 0; i < releationList.size(); i++) {
				relation = (Relation) releationList.get(i);
				if (NbaOliConstants.OLI_REL_HOLDINGCO == relation.getRelationRoleCode() && NbaOliConstants.OLI_HOLDING==relation.getOriginatingObjectType()) {
					Holding holding = nbaTXLife.getHolding(relation.getOriginatingObjectID());
					Policy replPolicy =holding.getPolicy();
					LifeUSAExtension lifeUSAextn = NbaUtils.getFirstLifeUSAExtension(replPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty()
							.getLife().getLifeUSA());
					if (lifeUSAextn != null) {
						long exch1035IndCode = lifeUSAextn.getExch1035IndCode();
						if (exch1035IndCode == NbaOliConstants.NBA_ANSWERS_YES){
							NbaParty nbaParty = nbaTXLife.getParty(relation.getRelatedObjectID());
							String replacedPartyKey = nbaParty.getParty().getPartyKey();
							if (replacedPartyKey != null) {
								if (!NbaUtils.isInternalReplacementCompany(replacedPartyKey) 
							            || NbaConstants.AXA_COMPANY_MONY002.equalsIgnoreCase(replacedPartyKey)) {
							        isExternal = true;
							    } 
							}

						}
					}
				}
			}
		}
        aNbaOinkRequest.addValue(isExternal);
    }
	
	
	/**
	 * Obtain the financial activity effective date when payment type is internal 1035 Exchange
	 * @param aNbaOinkRequest - data request container
	 */
	//APSL4287 New Method
	public void retrieveFirstInternal1035ExchFundDate(NbaOinkRequest aNbaOinkRequest) {
		Date finEffDate = null;
		int i = 0;
		while (true) {
			FinancialActivity activity = getFinancialActivity(i++);
			if (activity == null) {
				break;
			}
			if (activity.getFinActivityType() == NbaOliConstants.OLI_FINACT_1035INIT || activity.getFinActivityType() == NbaOliConstants.OLI_FINACT_1035SUBS) {
				if(finEffDate == null || finEffDate.after(activity.getFinEffDate())){
					finEffDate = activity.getFinEffDate();
				}
			}
		}
		aNbaOinkRequest.addValue(finEffDate);
	}
	
	/**
	 * Obtain the financial activity effective date when payment type is external 1035 Exchange
	 * @param aNbaOinkRequest - data request container
	 */
	//APSL4287 New Method
	public void retrieveFirstExternal1035ExchFundDate(NbaOinkRequest aNbaOinkRequest) {
		Date finEffDate = null;
		int i = 0;
		while (true) {
			FinancialActivity activity = getFinancialActivity(i++);
			if (activity == null) {
				break;
			}
			if (activity.getFinActivityType() == NbaOliConstants.OLI_FINACT_ROLLOVEREXT1035) {
				if(finEffDate == null || finEffDate.after(activity.getFinEffDate())){
					finEffDate = activity.getFinEffDate();
				}
			}
		}
		aNbaOinkRequest.addValue(finEffDate);
	}
	
	//APSL4287 New Method - ###Method Modified APSL4585###
	public void retrieveExch1035LastFundAppliedDate(NbaOinkRequest aNbaOinkRequest) {
		Date finEffDate = null;
		int i = 0;
		double minPremium = getPolicy().getMinPremiumInitialAmt();
		FinancialActivity activity =null;
        FinancialActivityExtension finActivityExtension = null;
        boolean isDisbursed = false;
		while (true) {
			activity = getFinancialActivity(i++);
			if (activity == null) {
				break;
			}
			isDisbursed = false;
			finActivityExtension = NbaUtils.getFirstFinancialActivityExtension(activity);
            if(finActivityExtension != null){
                isDisbursed = finActivityExtension.getDisbursedInd();
            }           
            if(activity.getFinActivitySubType() != NbaOliConstants.OLI_FINACTSUB_REV &&
                    activity.getFinActivitySubType() != NbaOliConstants.OLI_FINACTSUB_REFUND &&
                    activity.getFinActivitySubType() != NbaOliConstants.OLI_FINACTSUB_PARTIALREFUND && isDisbursed  != true){
                if (activity.getFinActivityType() == NbaOliConstants.OLI_FINACT_ROLLOVEREXT1035 || activity.getFinActivityType() == NbaOliConstants.OLI_FINACT_CWA || activity.getFinActivityType() == NbaOliConstants.OLI_FINACT_PREMIUMINIT) {
                    if(activity.getFinActivityGrossAmt() >= (minPremium-10)){
                        finEffDate = activity.getFinEffDate();
                        break;
                    }
                }
            }
		}
		aNbaOinkRequest.addValue(finEffDate);
	}
	
	// APSL4278 new method
	public void retrieveHighestSingleScore(NbaOinkRequest aNbaOinkRequest) {
		ProfileResultInfo profileResultInfo = null;
		Life life = getLife();
		if (life != null) {
			ArrayList covList = life.getCoverage();
			int covListsize = covList.size();
			for (int j = 0; j < covListsize; j++) {
				Coverage coverage = (Coverage) covList.get(j);
				if (coverage != null) {
					LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(0);
					if (lifeParticipant != null) {
						OLifEExtension oLifEExt = lifeParticipant.getOLifEExtensionAt(0);
						if (oLifEExt != null) {
							LifeParticipantExtension lifeParticipantExtension = oLifEExt.getLifeParticipantExtension();
							if (lifeParticipantExtension != null) {
								ArrayList underwritingResultList = lifeParticipantExtension.getUnderwritingResult();
								if (underwritingResultList != null) {
									for (int k = 0; k < underwritingResultList.size(); k++) {
										UnderwritingResult underwritingResult = (UnderwritingResult) underwritingResultList.get(k);
										ArrayList profileResultInfoList = underwritingResult.getProfileResultInfo();
										if (profileResultInfoList != null) {
											int profileResultInfoListSize = profileResultInfoList.size();
											for (int l = 0; l < profileResultInfoListSize; l++) {
												profileResultInfo = (ProfileResultInfo) profileResultInfoList.get(l);
												if (profileResultInfo != null) {
													if (profileResultInfo.getDescription() != null
															&& profileResultInfo.getProfileResultSubType() == (NbaOliConstants.OLI_PROFRESULTSUBTYPE_SINGLE)
															&& profileResultInfo.getProfileResultType() == (NbaOliConstants.OLI_PROFRESULTTYPE_SCORE)) {
														aNbaOinkRequest.addValue(profileResultInfo.getProfileResultValue());
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	// APSL4278 new method
	public void retrieveOverallRiskScore(NbaOinkRequest aNbaOinkRequest) {
		ProfileResultInfo profileResultInfo = null;
		Life life = getLife();
		if (life != null) {
			ArrayList covList = life.getCoverage();
			int covListsize = covList.size();
			for (int j = 0; j < covListsize; j++) {
				Coverage coverage = (Coverage) covList.get(j);
				if (coverage != null) {
					LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(0);
					if (lifeParticipant != null) {
						OLifEExtension oLifEExt = lifeParticipant.getOLifEExtensionAt(0);
						if (oLifEExt != null) {
							LifeParticipantExtension lifeParticipantExtension = oLifEExt.getLifeParticipantExtension();
							if (lifeParticipantExtension != null) {
								ArrayList underwritingResultList = lifeParticipantExtension.getUnderwritingResult();
								if (underwritingResultList != null) {
									for (int k = 0; k < underwritingResultList.size(); k++) {
										UnderwritingResult underwritingResult = (UnderwritingResult) underwritingResultList.get(k);
										ArrayList profileResultInfoList = underwritingResult.getProfileResultInfo();
										if (profileResultInfoList != null) {
											int profileResultInfoListSize = profileResultInfoList.size();
											for (int l = 0; l < profileResultInfoListSize; l++) {
												profileResultInfo = (ProfileResultInfo) profileResultInfoList.get(l);
												if (profileResultInfo != null) {
													if (profileResultInfo.getDescription() != null
															&& profileResultInfo.getProfileResultSubType() == (NbaOliConstants.OLI_PROFRESULTSUBTYPE_COMP)
															&& profileResultInfo.getProfileResultType() == (NbaOliConstants.OLI_PROFRESULTTYPE_SCORE)) {
														aNbaOinkRequest.addValue(profileResultInfo.getProfileResultValue());
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	//APSL4414 New Method
	public void retrieveTeleUndewriting(NbaOinkRequest aNbaOinkRequest) {
		boolean isTeleUndCase = false; 
        ArrayList formInstanceList = getOLifE().getFormInstance();
		int formInstanceListSize = formInstanceList.size();
		for (int i = 0; i < formInstanceListSize; i++) {
			FormInstance formInstance = (FormInstance) formInstanceList.get(i);
			if (formInstance.hasProviderFormNumber() && NbaConstants.FORM_NAME_COVERSHEET.equalsIgnoreCase(formInstance.getProviderFormNumber())) {
				isTeleUndCase = true;
				break;
			}
		}
		aNbaOinkRequest.addValue(isTeleUndCase);
	}
	
	/**	 
	 * Check if LTCSR is present on case
	 * @param aNbaOinkRequest - data request container
	 */
	//APSLAPSL4420 New Method
	public void retrieveLTCSRPresent(NbaOinkRequest aNbaOinkRequest) {
		Life life = getNbaTXLife().getLife(); 
		int covCount = life.getCoverageCount();
		for (int j = 0; j < covCount; j++) {
			Coverage coverage = life.getCoverageAt(j);
			List covOptCount = coverage.getCovOption();
			for (int k = 0; k < covOptCount.size(); k++) {
				CovOption covOption = coverage.getCovOptionAt(k);
				if (covOption.getLifeCovOptTypeCode() == NbaOliConstants.OLI_OPTTYPE_LTCABO) {
					if (!NbaUtils.isDeleted(covOption) && NbaOliConstants.OLI_POLSTAT_DECISSUE != covOption.getCovOptionStatus()) {
						aNbaOinkRequest.addValue(true);
					} else {
						aNbaOinkRequest.addValue(false);
					}
				}
			}
		}
	}
	
	/**	 
	 * Check For replacement and Term conversion policy to generate NBRPLNOTIF WI on Term Conversion case. 
	 * @param aNbaOinkRequest - data request container
	 */
	//APSL4449 New Method
	public void retrieveReplNotifIND(NbaOinkRequest aNbaOinkRequest) {
		if (getNbaTXLife() != null) {
			List tconvHoldingList = NbaUtils.getTermConvHoldingList(getNbaTXLife());
			List replHoldingList = NbaUtils.getReplacementHolding(getNbaTXLife());
			int tconvHoldingCount = tconvHoldingList.size();
			int replHoldingCount = replHoldingList.size();
			boolean generateRplNotif = false;
			long appState = getPolicy().getJurisdiction();
			if (getPolicy().getApplicationInfo().getReplacementInd()) {
				if (appState == NbaOliConstants.OLI_USA_GA || appState == NbaOliConstants.OLI_USA_IL || appState == NbaOliConstants.OLI_USA_MA
						|| appState == NbaOliConstants.OLI_USA_MI || appState == NbaOliConstants.OLI_USA_PR || appState == NbaOliConstants.OLI_USA_NY) {
					generateRplNotif = true;
				} else {
					for (int i = 0; i < replHoldingCount; i++) {
						Holding replHolding = (Holding) replHoldingList.get(i);
						String replPolNumber = replHolding.getPolicy().getPolNumber();
						if (!isReplPresentForConversionPol(replPolNumber, tconvHoldingList)) {
							generateRplNotif = true;
							break;
						} else {
							generateRplNotif = false;
						}
					}
				}
			}
			//Start APSL5167
			else if (NbaUtils.isTermConvOPAICase(getNbaTXLife()) && (appState == NbaOliConstants.OLI_USA_GA || appState == NbaOliConstants.OLI_USA_IL || appState == NbaOliConstants.OLI_USA_MA
					|| appState == NbaOliConstants.OLI_USA_MI || appState == NbaOliConstants.OLI_USA_PR)){
				generateRplNotif = true;
			} 
			//End APSL5167
			aNbaOinkRequest.addValue(generateRplNotif);
		}
	}	
	
	//APSL4449 New Method
	public boolean isReplPresentForConversionPol(String tconvPolNumber, List tconvHoldingList){
		boolean policyPresent = false;
		for (int i = 0; i < tconvHoldingList.size(); i++) {
			Holding replHolding = (Holding) tconvHoldingList.get(i);
			if (tconvPolNumber.equals(replHolding.getPolicy().getPolNumber())) {
				policyPresent = true;		
				break;
			}
		}
		return policyPresent;
	}
	
	// New method APSL4464(QC15850)
	public boolean matchWithHipaaParty(Party party) {
		Relation relation = nbaTXLife.getRelationForRoleAndOriginatingID(party.getId(), NbaOliConstants.OLI_REL_168);
		if (relation != null) {
			NbaParty nbaParty = nbaTXLife.getParty(relation.getRelatedObjectID());
			if (nbaParty != null) {
				Person hipaaPerson = nbaParty.getPerson();
				Person insuredPerson = null;
				if (party.hasPersonOrOrganization()) {
					insuredPerson = party.getPersonOrOrganization().getPerson();
				}
				if (hipaaPerson != null && insuredPerson != null) {
					// APSL4875 added null check for DOB
					if (hipaaPerson.getBirthDate() != null
							&& insuredPerson.getBirthDate() != null	
							&& (NbaUtils.compare(hipaaPerson.getBirthDate(), insuredPerson.getBirthDate()) == 0)
							&& NbaUtils.isEqual(hipaaPerson.getFirstName(), insuredPerson.getFirstName())
							&& NbaUtils.isEqual(hipaaPerson.getLastName(), insuredPerson.getLastName()) && compare(hipaaPerson, insuredPerson)) {
						return true;
					}
				}
			}
		}
		return false;
	}
    
	// New method APSL4464(QC15850)
	public boolean compare(Person hipaaPerson, Person insuredPerson) {
		if ((hipaaPerson.hasPrefix() || insuredPerson.hasPrefix()) && !NbaUtils.isEqual(hipaaPerson.getPrefix(), insuredPerson.getPrefix())) {
			return false;
		}
		if ((hipaaPerson.hasSuffix() || insuredPerson.hasSuffix()) && !NbaUtils.isEqual(hipaaPerson.getSuffix(), insuredPerson.getSuffix())) {
			return false;
		}
		/* APSL5344
		 * if ((!NbaUtils.isBlankOrNull(hipaaPerson.getMiddleName()) || !NbaUtils.isBlankOrNull(insuredPerson.getMiddleName()))//APSL4584
				&& !NbaUtils.isEqual(hipaaPerson.getMiddleName(), insuredPerson.getMiddleName())) {
			return false;
		}*/
		return true;
	}
	
	//APSL4502 New Method
	public void retrieveJetRecommendStatus(NbaOinkRequest aNbaOinkRequest) {
		Policy policy = getNbaTXLife().getPolicy();
		PolicyExtension policyExtn = NbaUtils.getFirstPolicyExtension(policy);
		if (policyExtn != null) {
			List presultList = policyExtn.getPredictiveResult();
			if (presultList != null) {
				int presultSize = presultList.size();
				PredictiveResult predictiveResult = (PredictiveResult) presultList.get(presultSize - 1);
				if (predictiveResult.hasPolicyStatus()) {
					aNbaOinkRequest.addValue(predictiveResult.getPolicyStatus());
				}
			}
		}
	}
	
	//APSL4601 New Method
	public void retrieveRCMTeam(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		aNbaOinkRequest.addValue(NbaUtils.getRCMTeam(NbaUtils.getAsuCodeForRetail(getNbaTXLife()), NbaUtils.getEPGInd(getNbaTXLife())));
	}
	
	/**
     * Obtain MIP Date
     * @param aNbaOinkRequest - data request container
     */
    //APSL4585 New Method
    public void retrieveMIPDate(NbaOinkRequest aNbaOinkRequest) {
        double totalCWA = 0;
        double requiredPayment = 0;
        Date mipDate = null;
        
        if (getPolicy() != null) { 
            if (AxaUtils.isPermProduct(getPolicy().getProductType())) {
                requiredPayment = getPolicy().getMinPremiumInitialAmt();
            } else {
                requiredPayment = getPolicy().getPaymentAmt();
            }
            long finActivitySubType = 0; //SPR1876
            // Begin NBLXA-1279
            List <FinancialActivity> finActivity=getPolicy().getFinancialActivity();
            if(finActivity!=null && finActivity.size()>0){
              	SortingHelper.sortData(finActivity, true, NbaConstants.FINEFFDATE);
			}
            for (FinancialActivity financialActivity:finActivity) { // End NBLXA-1279
                String finActivityType = Long.toString(financialActivity.getFinActivityType());
                finActivitySubType = financialActivity.getFinActivitySubType(); //SPR1876
                boolean paymentAddition = false;
                int arrayLength = NbaConstants.CWA_PAYMENT_ADDITIONS.length;
                for (int j = 0; j < arrayLength ; j++) {
                    if (finActivityType.equals(NbaConstants.CWA_PAYMENT_ADDITIONS[j])) {
                        paymentAddition = true;
                        break;
                    }
                }
                if (paymentAddition) {
                    FinancialActivityExtension financialActivityExtension = NbaUtils.getFirstFinancialActivityExtension(financialActivity);
                    if (financialActivityExtension != null) {
                        if (financialActivityExtension.getDisbursedInd()) {
                            paymentAddition = false;
                        }
                    }
                    if (NbaOliConstants.OLI_FINACTSUB_REV == finActivitySubType
                        || NbaOliConstants.OLI_FINACTSUB_REFUND == finActivitySubType
                        || NbaOliConstants.OLI_FINACTSUB_PARTIALREFUND == finActivitySubType) { //SPR1876
                        paymentAddition = false; //SPR1876
                    } //SPR1876
                }
                if (paymentAddition) {
                    totalCWA = (double) Math.round((totalCWA + financialActivity.getFinActivityGrossAmt()) * 100) / 100; // SPR1876,ALII2039, APSL3543
                    if(totalCWA>=requiredPayment-10) {
                        mipDate = financialActivity.getFinEffDate(); //APSL5005
                        break;
                    }

                }
            }
        }
        aNbaOinkRequest.addValue(mipDate);
    }   
    
    //New Method - APSL4635
    public void retrieveTermConvRegisterDateX(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.setParseMultiple(true);
		List replHoldingList = NbaUtils.getTermConvOPAIHoldings(getNbaTXLife());
		for (int i = 0; i < replHoldingList.size(); i++) {
			Policy replPolicy = ((Holding) replHoldingList.get(i)).getPolicy();
			PolicyExtension replPolicyExtn = NbaUtils.getFirstPolicyExtension(replPolicy);
			if (replPolicyExtn != null) {
				if(replPolicyExtn.hasTermConvRegisterDate()){
					aNbaOinkRequest.addValue(replPolicyExtn.getTermConvRegisterDate());
				} else{
					aNbaOinkRequest.addValue("");
				}
			}
		}
	}
// APSL4759 New Method    
    public void retrieveDDWPresent(NbaOinkRequest aNbaOinkRequest) {
		Life life = getNbaTXLife().getLife(); //check if DDW is present on case - if present throw msg
		int covCount = life.getCoverageCount();
		for (int j = 0; j < covCount; j++) {
			Coverage coverage = life.getCoverageAt(j);
			List covOptCount = coverage.getCovOption();
			for (int k = 0; k < covOptCount.size(); k++) {
				CovOption covOption = coverage.getCovOptionAt(k);
				if (covOption.getLifeCovOptTypeCode() == NbaOliConstants.OLI_COVTYPE_LTC) {
					if (!NbaUtils.isDeleted(covOption) && NbaOliConstants.OLI_POLSTAT_DECISSUE != covOption.getCovOptionStatus()) {
						aNbaOinkRequest.addValue(true);
					} else {
						aNbaOinkRequest.addValue(false);
					}
				}
			}
		}
	}
    
     // APSL4756 New Method
	public void retrieveChkLstCompletedIndForUW(NbaOinkRequest aNbaOinkRequest) throws NbaBaseException {
		String polNumber = getPolicy().getPolNumber();
		if (!NbaUtils.isBlankOrNull(polNumber)) {
			aNbaOinkRequest.addValue(NbaSystemDataProcessor.selectChkLstCompletedIndFromDB(polNumber, NbaOliConstants.OLI_CHECKTYPE));
		}

	}
	// APSL4756 New Method
	public void retrieveGHCPAppliedInd(NbaOinkRequest aNbaOinkRequest) {
		Party party = getParty(aNbaOinkRequest, 0);
		UnderwritingAnalysis undAnalysis = null;
		if (party != null) {
			OLifEExtension oli = null;
			int index_extension = getExtensionIndex(party.getOLifEExtension(), PARTY_EXTN);
			if (index_extension != -1) {
				oli = party.getOLifEExtensionAt(index_extension);
			}
			if (oli != null && oli.getPartyExtension() != null && oli.getPartyExtension().getUnderwritingAnalysis() != null) {
				undAnalysis = oli.getPartyExtension().getUnderwritingAnalysis();
				if (undAnalysis.hasGHCPInfo()) {
					GHCPInfo ghcpInfo = undAnalysis.getGHCPInfo();
					if (ghcpInfo.hasGHCPAppliedInd()) {
						aNbaOinkRequest.addValue(ghcpInfo.getGHCPAppliedInd());
					}
				}
			}
		}
	}
	
	// New method APSL4800
	public void retrieveIsOutStandReq(NbaOinkRequest aNbaOinkRequest) {
		boolean reqOutstanding = false;
		List reqList = getRequirementInfos(aNbaOinkRequest);
		for (int i = 0; i < reqList.size(); i++) {
			RequirementInfo aRequirementInfo = getRequirementInfo(reqList, i);
			if (aRequirementInfo != null && NbaUtils.isRequirementOutstanding(aRequirementInfo.getReqStatus())) {
				reqOutstanding = true;
				break;
			}
		}
		if (reqOutstanding) {
			aNbaOinkRequest.addValue(true);
		} else {
			aNbaOinkRequest.addValue(false);
		}
	}	
	
	
	/**
	 * Returns UW Program Indicator.
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaBaseException
	 */
	//APSL4756 - UW Automation 'Decision Journey'
	public void retrieveUwProgramInd(NbaOinkRequest aNbaOinkRequest) {
		List uwProgramList = new ArrayList();
		LifeParticipant lifeParticipant;
		int next = 0;
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			List lifeParticipantList = getLifeParticipants(aNbaOinkRequest);
			while ((lifeParticipant = getNextLifeParticipant(lifeParticipantList, next++)) != null) {
			if (lifeParticipant != null) {
				LifeParticipantExtension lifeParticipantExtension = NbaUtils.getFirstLifeParticipantExtension(lifeParticipant);
				if (lifeParticipantExtension != null) {
					uwProgramList = lifeParticipantExtension.getUWProgram();
					for (int k = 0; k < uwProgramList.size(); k++) {
						UWProgram uwProgram = (UWProgram) uwProgramList.get(k);
						if(NbaUtils.isAdditionalConsidrationNeeded(uwProgram)){
							aNbaOinkRequest.addValue("true");
							break;
						}
					}
				}
			}
		}
		}
	}
	

/**
 * Return UnderwritingResultReason For genetic info
 * @param aNbaOinkRequest
 */
//APSL4817 New method
public void retrieveGeneticInfoCode(NbaOinkRequest aNbaOinkRequest) {
	for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
		Party party = getParty(aNbaOinkRequest, i);
		if (party != null) {
			UnderwritingResult uwResult = getNbaTXLife().getGeneticInfo(party.getId());
			if (uwResult != null && uwResult.hasUnderwritingResultReason()) {
				aNbaOinkRequest.addValue(uwResult.getUnderwritingResultReason());
				return;
			}
		}
	}
}

//APSL4766 New Method
public void retrieveACHMissingInfoPresent(NbaOinkRequest aNbaOinkRequest) {
	ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
	if(appInfoExt != null && appInfoExt.getInitialPremiumPaymentForm() == NbaOliConstants.OLI_PAYFORM_EFT){
		boolean depositorSignatureFound = false;
		boolean ownerSignatureFound = false;
		FormInstance formInstance = NbaUtils.getFormInstance(getNbaTXLife(), NbaConstants.FORM_NAME_SYSPAY);
		if (formInstance != null) {
			List signInfoList = formInstance.getSignatureInfo();
			if (signInfoList != null) {			
				for (int i = 0; i < signInfoList.size(); i++) {
					SignatureInfo signInfo = (SignatureInfo) signInfoList.get(i);
					if (signInfo != null && signInfo.getSignatureRoleCode() == NbaOliConstants.OLI_PARTICROLE_DEPOSITOR 
							&& signInfo.getSignaturePurpose() == NbaOliConstants.OLI_SIGTYPE_INITIAL
							&& signInfo.getSignatureCode() != null && signInfo.getSignatureCode().equalsIgnoreCase(NbaConstants.FORM_NAME_ELECTRONIC)){
						depositorSignatureFound = true;
					    break;
					}
				}
				
				long partyRole;
				if (!getNbaTXLife().isOwnerSameAsPrimaryIns()) {
					partyRole = NbaOliConstants.OLI_PARTICROLE_OWNER;
				} else {
					partyRole = NbaOliConstants.OLI_PARTICROLE_PRIMARY;
				}
				
				for (int i = 0; i < signInfoList.size(); i++) {
					SignatureInfo signInfo = (SignatureInfo) signInfoList.get(i);
					if (signInfo != null && signInfo.getSignatureRoleCode() == partyRole 
							&& signInfo.getSignaturePurpose() == NbaOliConstants.OLI_SIGTYPE_INITIAL
							&& signInfo.getSignatureCode() != null && signInfo.getSignatureCode().equalsIgnoreCase(NbaConstants.FORM_NAME_ELECTRONIC)){
						ownerSignatureFound = true;
					    break;
					}
				}
			}		
		}
		
		if(!ownerSignatureFound || !depositorSignatureFound){
			aNbaOinkRequest.addValue("true");
			return;
		}
	}
	aNbaOinkRequest.addValue("false");
}

	// New mettod APSL4763
	public void retrieveIsOutStandProducerReq(NbaOinkRequest aNbaOinkRequest) {
		List pendReqList = NbaUtils.getPendingRequirementList(getNbaTXLife().getPolicy());
		if (pendReqList.size() > 0) {
			aNbaOinkRequest.addValue(true);
		} else {
			aNbaOinkRequest.addValue(false);
		}
	}
	

	   /**
		 * Obtain the value for ReqSignedDate.
		 * @param aNbaOinkRequest - data request container
		 */
	//APSL4872 New method
	public void retrieveReqSignedDate(NbaOinkRequest aNbaOinkRequest) {
		Date reqSignDate = null;
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		Party party = null;
		String reqCode = aNbaOinkRequest.getQualifier2();
		for (int i = 0; i < partyList.size(); i++) {
			party = (Party) partyList.get(i);
			List requirementInfos = getNbaTXLife().getRequirementInfoList(party.getId(), Long.parseLong(reqCode));
			for (int j = 0; requirementInfos != null && j < requirementInfos.size(); j++) {
				RequirementInfo requirementInfo=(RequirementInfo) requirementInfos.get(i);
				if ((requirementInfo != null) &&(requirementInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_RECEIVED)) {
					RequirementInfoExtension reqInfoExtn = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
					if (reqInfoExtn != null && reqInfoExtn.hasSignDate()) {
						if (reqSignDate == null || reqInfoExtn.getSignDate().after(reqSignDate)) {
							reqSignDate = reqInfoExtn.getSignDate();
						}
					}

				}
			}

			if (!NbaUtils.isBlankOrNull(reqSignDate)) {
				aNbaOinkRequest.addValue(reqSignDate);
			} else {
				aNbaOinkRequest.addUnknownValue((Date) null);
			}
		}
	}
	
	// New method APSL4916
	public void retrieveCoverageUnderwritingResultMissing(NbaOinkRequest aNbaOinkRequest) {
		boolean coverageUWReasonAbsent = false;
		Life life = getNbaTXLife().getLife(); 
		int covCount = life.getCoverageCount();
		for (int j = 0; j < covCount; j++) {
			Coverage coverage = life.getCoverageAt(j);
			CoverageExtension coverageExt = NbaUtils.getFirstCoverageExtension(coverage);
			if (!NbaUtils.isDeleted(coverage) && coverageExt != null && coverage.getLifeCovStatus() == NbaOliConstants.OLI_POLSTAT_DECISSUE) {
				List uwResultList = coverageExt.getUnderwritingResult();
				if (uwResultList == null || uwResultList.size() == 0) {
					coverageUWReasonAbsent = true;
				}
			}
		}
		if (coverageUWReasonAbsent) {
			aNbaOinkRequest.addValue(true);
		} else {
			aNbaOinkRequest.addValue(false);
		}
	}
	
	// New method APSL4916
	public void retrieveCovOptionUnderwritingResultMissing(NbaOinkRequest aNbaOinkRequest) {
		boolean covOptionUWReasonAbsent = false;
		Life life = getNbaTXLife().getLife(); 
		int covCount = life.getCoverageCount();
		for (int j = 0; j < covCount; j++) {
			Coverage coverage = life.getCoverageAt(j);
			List covOptCount = coverage.getCovOption();
			for (int k = 0; k < covOptCount.size(); k++) {
				CovOption covOption = coverage.getCovOptionAt(k);
				CovOptionExtension covOptionExt = NbaUtils.getFirstCovOptionExtension(covOption);
				if (!NbaUtils.isDeleted(covOption) && covOptionExt != null && NbaOliConstants.OLI_POLSTAT_DECISSUE == covOption.getCovOptionStatus()) {
					List uwResultList = covOptionExt.getUnderwritingResult();
					if (uwResultList == null || uwResultList.size() == 0) {
						covOptionUWReasonAbsent = true;
					}
				}
			}
		}
		if (covOptionUWReasonAbsent) {
			aNbaOinkRequest.addValue(true);
		} else {
			aNbaOinkRequest.addValue(false);
		}
	}
	
	/**
	 * Obtain the value for ReqParamedSignDate.
	 * @param aNbaOinkRequest - data request container
	 */
	//APSL4872 New method
	public void retrieveReqParamedSignDate(NbaOinkRequest aNbaOinkRequest) {
		Date paramedReqDate = null;
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		Party party = null;
		boolean parmedSignedDateExist = false;
		List requirementInfos = getNbaTXLife().getPolicy().getRequirementInfo();
		for (int i = 0; i < partyList.size(); i++) {
			party = (Party) partyList.get(i);
			for (int j = 0; requirementInfos != null && j < requirementInfos.size(); j++) {
				RequirementInfo requirementInfo = getNbaTXLife().getPolicy().getRequirementInfoAt(j);
				if ((requirementInfo != null)&&(requirementInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_RECEIVED)
						&& (requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MEDEXAMPARAMED || requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_MEDEXAMMD)
						&& party.getId().equalsIgnoreCase(requirementInfo.getAppliesToPartyID())) {
					RequirementInfoExtension reqInfoExtn = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
					if (reqInfoExtn != null && reqInfoExtn.hasParamedSignedDate()) {
						if (paramedReqDate == null || reqInfoExtn.getParamedSignedDate().after(paramedReqDate)) {
							paramedReqDate = reqInfoExtn.getParamedSignedDate();
 							parmedSignedDateExist = true;
						}
					}

				}
			}
		}
		if (parmedSignedDateExist) {
			aNbaOinkRequest.addValue(paramedReqDate);
		} else {
			aNbaOinkRequest.addUnknownValue((Date) null);
		}
	}

		
		// New method APSL5100
		public void retrieveRCMNIGOCondition(NbaOinkRequest aNbaOinkRequest) {
			boolean rcmNIGOConditionsPresent = false;
			List<Activity> activityList = getNbaTXLife().getOLifE().getActivity();
			for(Activity activity : activityList){
				if(activity != null && activity.getActivityTypeCode() == NbaOliConstants.OLI_ACTTYPE_1009800006 && activity.getActivityStatus() == NbaOliConstants.OLI_ACTSTAT_ACTIVE){
					ActivityExtension activityExtn = NbaUtils.getFirstActivityExtension(activity);
					if(activityExtn != null && activityExtn.getConditions() == NbaOliConstants.OLI_PRINT_PREVIEW_CONDITIONS_NIGO_RCM){
						rcmNIGOConditionsPresent = true;
					}
				}
			}
			aNbaOinkRequest.addValue(rcmNIGOConditionsPresent);
		}
		
		// New method APSL5100
		public void retrieveIGOCondition(NbaOinkRequest aNbaOinkRequest) {
			boolean igoConditionsPresent = false;
			List<Activity> activityList = getNbaTXLife().getOLifE().getActivity();
			for(Activity activity : activityList){
				if(activity != null && activity.getActivityTypeCode() == NbaOliConstants.OLI_ACTTYPE_1009800006 && activity.getActivityStatus() == NbaOliConstants.OLI_ACTSTAT_ACTIVE){
					ActivityExtension activityExtn = NbaUtils.getFirstActivityExtension(activity);
					if(activityExtn != null && activityExtn.getConditions() == NbaOliConstants.OLI_PRINT_PREVIEW_CONDITIONS_IGO){
						igoConditionsPresent = true;
					}
				}
			}
			aNbaOinkRequest.addValue(igoConditionsPresent);
		}
		
		// New method APSL5100
		public void retrieveIGOCorrectedInEpolicyCondition(NbaOinkRequest aNbaOinkRequest) {
			boolean igoCorrectedInEpolicyPresent = false;
			List<Activity> activityList = getNbaTXLife().getOLifE().getActivity();
			for(Activity activity : activityList){
				if(activity != null && activity.getActivityTypeCode() == NbaOliConstants.OLI_ACTTYPE_1009800006 && activity.getActivityStatus() == NbaOliConstants.OLI_ACTSTAT_ACTIVE){
					ActivityExtension activityExtn = NbaUtils.getFirstActivityExtension(activity);
					if(activityExtn != null && activityExtn.getConditions() == NbaOliConstants.OLI_PRINT_PREVIEW_CONDITIONS_IGO_CORRECTED_IN_EPOLICY){
						igoCorrectedInEpolicyPresent = true;
					}
				}
			}
			aNbaOinkRequest.addValue(igoCorrectedInEpolicyPresent);
		}
		
		// New method APSL5100
		public void retrieveCorrectedPrintPreviewNeededCondition(NbaOinkRequest aNbaOinkRequest) {
			boolean correctedPrintPreviewNeededPresent = false;
			List<Activity> activityList = getNbaTXLife().getOLifE().getActivity();
			for(Activity activity : activityList){
				if(activity != null && activity.getActivityTypeCode() == NbaOliConstants.OLI_ACTTYPE_1009800006 && activity.getActivityStatus() == NbaOliConstants.OLI_ACTSTAT_ACTIVE){
					ActivityExtension activityExtn = NbaUtils.getFirstActivityExtension(activity);
					if(activityExtn != null && activityExtn.getConditions() == NbaOliConstants.OLI_PRINT_PREVIEW_CONDITIONS_CORRECTED_PREVIEW_NEEDED){
						correctedPrintPreviewNeededPresent = true;
					}
				}
			}
			aNbaOinkRequest.addValue(correctedPrintPreviewNeededPresent);
		}
		
		// New method APSL5100
		public void retrieveCorrectedPrintNoPreviewNeededCondition(NbaOinkRequest aNbaOinkRequest) {
			boolean correctedPrintNoPreviewNeededPresent = false;
			List<Activity> activityList = getNbaTXLife().getOLifE().getActivity();
			for(Activity activity : activityList){
				if(activity != null && activity.getActivityTypeCode() == NbaOliConstants.OLI_ACTTYPE_1009800006 && activity.getActivityStatus() == NbaOliConstants.OLI_ACTSTAT_ACTIVE){
					ActivityExtension activityExtn = NbaUtils.getFirstActivityExtension(activity);
					if(activityExtn != null && activityExtn.getConditions() == NbaOliConstants.OLI_PRINT_PREVIEW_CONDITIONS_CORRECTED_NO_PREVIEW_NEEDED){
						correctedPrintNoPreviewNeededPresent = true;
					}
				}
			}
			aNbaOinkRequest.addValue(correctedPrintNoPreviewNeededPresent);
		}
		
		// New method APSL5100
		public void retrieveUWNIGOCondition(NbaOinkRequest aNbaOinkRequest) {
			boolean uwNIGOConditionsPresent = false;
			List<Activity> activityList = getNbaTXLife().getOLifE().getActivity();
			for(Activity activity : activityList){
				if(activity != null && activity.getActivityTypeCode() == NbaOliConstants.OLI_ACTTYPE_1009800006 && activity.getActivityStatus() == NbaOliConstants.OLI_ACTSTAT_ACTIVE){
					ActivityExtension activityExtn = NbaUtils.getFirstActivityExtension(activity);
					if(activityExtn != null && activityExtn.getConditions() == NbaOliConstants.OLI_PRINT_PREVIEW_CONDITIONS_NIGO_UW){
						uwNIGOConditionsPresent = true;
					}
				}
			}
			aNbaOinkRequest.addValue(uwNIGOConditionsPresent);
		}
		// New method APSL5100
		public void retrieveUWUnapproveApproveCondition(NbaOinkRequest aNbaOinkRequest) {
			boolean uwUnapproveApproveNIGOReasonPresent = false;
			List<Activity> activityList = getNbaTXLife().getOLifE().getActivity();
			for(Activity activity : activityList){
				if(activity != null && activity.getActivityTypeCode() == NbaOliConstants.OLI_ACTTYPE_1009800006 && activity.getActivityStatus() == NbaOliConstants.OLI_ACTSTAT_ACTIVE){
					ActivityExtension activityExtn = NbaUtils.getFirstActivityExtension(activity);
					if(activityExtn != null && activityExtn.getConditions() == NbaOliConstants.OLI_PRINT_PREVIEW_CONDITIONS_NIGO_UW_UNAPPROVE_APPROVE){
						uwUnapproveApproveNIGOReasonPresent = true;
					}
				}
			}
			aNbaOinkRequest.addValue(uwUnapproveApproveNIGOReasonPresent);
		}
		
	//New method APSL5108 PRE BIC- oink variable for qualified funds 
	public void retrievequalifiedFundsInd(NbaOinkRequest aNbaOinkRequest) {
		try {
			aNbaOinkRequest.addValue(NbaUtils.isQualifiedMoneyForPreBIC(nbaTXLife));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	//New method APSL5108 PRE BIC- oink variable for qualified plans 
	public void retrievequalifiedPlanforBICInd(NbaOinkRequest aNbaOinkRequest) {
		try {
			aNbaOinkRequest.addValue(NbaUtils.isQualifiedPlanForPreBIC(nbaTXLife));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	// New method APSL5108 PRE BIC- oink variable for switch
	public void retrieveAmendEndorseBICInd(NbaOinkRequest aNbaOinkRequest) {
		try {
			String AmendEndorseApplicable = null;
			AmendEndorseApplicable = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.DOL_AmendEndorseSwitch);
			if (!NbaUtils.isBlankOrNull(AmendEndorseApplicable) && NbaConstants.TRUE_STR.equalsIgnoreCase(AmendEndorseApplicable)) {
				aNbaOinkRequest.addValue(true);
			}
		} catch (NbaBaseException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Return Endorsement Code List.
	 * 
	 * @return
	 */
	// APSL 5164 New method
	public String amendmentEndorsementCodeInd() {
		Policy policy = getPolicy();
		ArrayList endorsementList = new ArrayList();
		Endorsement endorsement = null;
		StringBuffer endrsmntList = new StringBuffer();
		if (policy != null) {
			endorsementList = policy.getEndorsement();
			for (int i = 0; i < endorsementList.size(); i++) {
				endorsement = (Endorsement) endorsementList.get(i);
				if (endorsement != null) {
					EndorsementExtension endorsementExtension = NbaUtils.getFirstEndorseExtension(endorsement);
					if (endorsementExtension != null) {
						endrsmntList.append(endorsementExtension.getEndorsementCodeContent());
						endrsmntList.append(", ");
					}
				}
			}
		}
		String endrsmntListStr = endrsmntList.toString();
		if (endrsmntList.length() > 0) {
			endrsmntListStr = endrsmntList.substring(0, endrsmntList.length() - 2); // Remove last comma and space from the list
		}
		return endrsmntListStr;
	}

	// APSL 5164 New method
	public void retrieveAmendmentEndorsementCodeCIPE(NbaOinkRequest aNbaOinkRequest) {
		String endrsmntListStr = amendmentEndorsementCodeInd();
		int i = 0;
		for (i = 0; i < NbaConstants.AXA_EXCLUSION_LIST.length; i++) {
			if (endrsmntListStr.contains(NbaConstants.AXA_EXCLUSION_LIST[i])) {
				aNbaOinkRequest.addValue(true);
				break;
			}
		}
	}

	// APSL 5164 New method
	public void retrieveFaceAmountAmendmentEndorsement(NbaOinkRequest aNbaOinkRequest) {
		Policy policy = getPolicy();
		ArrayList endorsementList = new ArrayList();
		Endorsement endorsement = null;
		if (policy != null) {
			endorsementList = policy.getEndorsement();
			for (int i = 0; i < endorsementList.size(); i++) {
				endorsement = (Endorsement) endorsementList.get(i);
				if (endorsement != null) {
					EndorsementExtension endorsementExtension = NbaUtils.getFirstEndorseExtension(endorsement);
					if (endorsementExtension != null) {
						if (("Face Amount").equalsIgnoreCase(endorsementExtension.getQuestionNumber())) {
							aNbaOinkRequest.addValue(true);
							break;
						}

					}
				}
			}
		}
	}

	/**
	 * This method find out any Substandard Rating on case for each Coverage or Rider.
	 * 
	 * @param aNbaOinkRequest
	 *            - data request container
	 */
	// APSL5164 New Method
	protected void retrieveSubstandardRatings(NbaOinkRequest aNbaOinkRequest) {
		boolean participantrateddeny = false;
		List lifeParticipants = getInsurableLifeParticipants(aNbaOinkRequest);
		int next = 0;
		LifeParticipant lifeParticipant;
		SubstandardRating substandardRating;
		while ((lifeParticipant = getNextLifeParticipant(lifeParticipants, next++)) != null) {// P2AXAL035 call to getNextLifeParticipant Modified
			int countSR = lifeParticipant.getSubstandardRatingCount();
			for (int j = 0; j < countSR; j++) {
				substandardRating = lifeParticipant.getSubstandardRatingAt(j);
				if (NbaUtils.isValidRating(substandardRating)) {
					participantrateddeny = true;
				}
			}
		}
		if(!participantrateddeny){
			participantrateddeny = covOptionsDeniedInd();
		}
		aNbaOinkRequest.addValue(participantrateddeny);
	}

	// APSL 5164 New method
		public void retrieveBenefitTableRating(NbaOinkRequest aNbaOinkRequest) {
			CovOption covOption = null;
			SubstandardRating substandardRating = null;
			List covOptionList = getCovOptions();
			if (covOptionList != null && covOptionList.size() > 0) {
				for (int i = 0; i < covOptionList.size(); i++) {
					covOption = (CovOption) covOptionList.get(i);
					if (covOption != null) {
						int countSR = covOption.getSubstandardRatingCount();
						for (int k = 0; k < countSR; k++) {
							substandardRating = covOption.getSubstandardRatingAt(k);
							if (NbaUtils.isValidRating(substandardRating)) {
								aNbaOinkRequest.addValue(true);
								return;
							}
						}
					}
				}
			}
		}

	// APSL 5164 New method
	public void retrieveCovOptionDenyInd(NbaOinkRequest aNbaOinkRequest) {
		Coverage baseCoverage = getNbaTXLife().getPrimaryCoverage();
		boolean uWReasonType = false;
		List covOptCount = baseCoverage.getCovOption();
		for (int k = 0; k < covOptCount.size(); k++) {
			CovOption covOption = baseCoverage.getCovOptionAt(k);
			CovOptionExtension covOptionExt = NbaUtils.getFirstCovOptionExtension(covOption);
			if (!NbaUtils.isDeleted(covOption) && covOptionExt != null && NbaOliConstants.OLI_POLSTAT_DECISSUE == covOption.getCovOptionStatus()) {
				uWReasonType = true;
				break;
			}
		}
		aNbaOinkRequest.addValue(uWReasonType);
	}

	// APSL 5164 New method
	public void retrieveAnyCoverageDenyInd(NbaOinkRequest aNbaOinkRequest) {
		Life life = getNbaTXLife().getLife();
		boolean uWReasonType = false;
		int covCount = life.getCoverageCount();
		for (int j = 0; j < covCount; j++) {
			Coverage coverage = life.getCoverageAt(j);
			CoverageExtension coverageExt = NbaUtils.getFirstCoverageExtension(coverage);
			if (!NbaUtils.isDeleted(coverage) && coverageExt != null && coverage.getLifeCovStatus() == NbaOliConstants.OLI_POLSTAT_DECISSUE) {
				uWReasonType = true;
				break;
			}
		}
		aNbaOinkRequest.addValue(uWReasonType);
	}

	/**
	 * Obtain the value for UnderwritingResultReason.
	 * Holding.Policy.ApplicationInfo.OLifEExtension.ApplicationInfoExtension.UnderwritingResult.UnderwritingResultReason
	 * 
	 * @param aNbaOinkRequest
	 *            - data request container
	 */
	// APSL 5164 New method
	public void retrieveAUDInd(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo api = getApplicationInfo();
		if (api != null) {
			int index_Extension = getExtensionIndex(api.getOLifEExtension(), APPLICATIONINFO_EXTN);
			if (index_Extension != -1) {
				OLifEExtension oli = api.getOLifEExtensionAt(index_Extension);
				if (oli != null) {
					List urList = new ArrayList();
					urList = oli.getApplicationInfoExtension().getUnderwritingResult();
					if (urList.size() > 0) {
						UnderwritingResult uwResult = oli.getApplicationInfoExtension().getUnderwritingResultAt(0);
						if (uwResult.hasUnderwritingResultReason()) {
							aNbaOinkRequest.addValue(true);
							return;
						}
					}
				}
			}
		}
	}
	
	// New method APSL5173 Market Stabilizer Option
	public void retrieveGrowthCapRate(NbaOinkRequest aNbaOinkRequest) {
		ArrangementExtension arrExtn = NbaUtils.getArrangementExtention(getNbaTXLife(), NbaOliConstants.OLI_ARRTYPE_AA,
				NbaOliConstants.OLI_ARRSUBTYPE_FULLREBALFT);
		if (arrExtn != null && arrExtn.hasGrowthCapRate()) {
			aNbaOinkRequest.addValue(arrExtn.getGrowthCapRate());
		}
	}

	// New method APSL5173 Pro-rata transfers GIA
	public void retrieveProRateGIAFundSupplInd(NbaOinkRequest aNbaOinkRequest) {
		ArrangementExtension arrExtn = NbaUtils.getArrangementExtention(getNbaTXLife(), NbaOliConstants.OLI_ARRTYPE_AA,
				NbaOliConstants.OLI_ARRSUBTYPE_FULLREBALFT);
		if (arrExtn != null) {
			aNbaOinkRequest.addValue(arrExtn.getProRateGIAFundSupplInd());
		}
	}

	// New method APSL5173
	public void retrieveLTC3rdPartyElectionIndCode(NbaOinkRequest aNbaOinkRequest) {
		String covOptionType = aNbaOinkRequest.getQualifier();
		long benefitTypeCode = NbaOliConstants.OLI_COVTYPE_LTCRIDER;
		Coverage coverage = getNbaTXLife().getPrimaryCoverage();
		if (coverage != null) {
			if (covOptionType != null && covOptionType.equals(LTC_BENEFIT)) {
				benefitTypeCode = NbaOliConstants.OLI_COVTYPE_LTCRIDER;
			}
			CovOption covOption = getNbaTXLife().getCovOption(coverage, benefitTypeCode);
			if (covOption != null) {
				CovOptionExtension aNewCovoptExtn = NbaUtils.getFirstCovOptionExtension(covOption);
				if (aNewCovoptExtn != null && aNewCovoptExtn.hasLTC3rdPartyElectionIndCode()) {
					aNbaOinkRequest.addValue(aNewCovoptExtn.getLTC3rdPartyElectionIndCode());
				}
			}
		}
	}

	// New method APSL5173
	public void retrieveNonFortProvisionIndCode(NbaOinkRequest aNbaOinkRequest) {
		String covOptionType = aNbaOinkRequest.getQualifier();
		long benefitTypeCode = NbaOliConstants.OLI_COVTYPE_LTCRIDER;
		Coverage coverage = getNbaTXLife().getPrimaryCoverage();
		if (coverage != null) {
			if (covOptionType != null && covOptionType.equals(LTC_BENEFIT)) {
				benefitTypeCode = NbaOliConstants.OLI_COVTYPE_LTCRIDER;
			}
			CovOption covOption = getNbaTXLife().getCovOption(coverage, benefitTypeCode);
			if (covOption != null) {
				CovOptionExtension aNewCovoptExtn = NbaUtils.getFirstCovOptionExtension(covOption);
				if (aNewCovoptExtn != null && aNewCovoptExtn.hasNonFortProvisionIndCode()) {
					aNbaOinkRequest.addValue(aNewCovoptExtn.getNonFortProvisionIndCode());
				}
			}
		}
	}

	// New method APSL5173
	public void retrieveOptionPct(NbaOinkRequest aNbaOinkRequest) {
		String covOptionType = aNbaOinkRequest.getQualifier();
		long benefitTypeCode = NbaOliConstants.OLI_COVTYPE_LTCRIDER;
		Coverage coverage = getNbaTXLife().getPrimaryCoverage();
		if (coverage != null) {
			if (covOptionType != null && covOptionType.equals(LTC_BENEFIT)) {
				benefitTypeCode = NbaOliConstants.OLI_COVTYPE_LTCRIDER;
			}
			CovOption covOption = getNbaTXLife().getCovOption(coverage, benefitTypeCode);
			if (covOption != null && covOption.hasOptionPct()) {
				aNbaOinkRequest.addValue(covOption.getOptionPct());
			}
		}
	}

	// New method APSL5173
	public void retrieveOPAIExerciseDate(NbaOinkRequest aNbaOinkRequest) {
		Coverage coverage = getNbaTXLife().getPrimaryCoverage();
		if (coverage != null) {
			CovOption covOption = getNbaTXLife().getCovOption(coverage, NbaOliConstants.OLI_OPTTYPE_OPAI);
			if (covOption != null && covOption.hasExerciseDate()) {
				aNbaOinkRequest.addValue(covOption.getExerciseDate());
			}
		}
	}

	// New method APSL5173
	public void retrieveAccelerationPct(NbaOinkRequest aNbaOinkRequest) {
		String covOptionType = aNbaOinkRequest.getQualifier();
		long benefitTypeCode = NbaOliConstants.OLI_COVTYPE_LTCRIDER;
		Coverage coverage = getNbaTXLife().getPrimaryCoverage();
		if (coverage != null) {
			if (covOptionType != null && covOptionType.equals(LTC_BENEFIT)) {
				benefitTypeCode = NbaOliConstants.OLI_COVTYPE_LTCRIDER;
			}
			CovOption covOption = getNbaTXLife().getCovOption(coverage, benefitTypeCode);
			if (covOption != null) {
				CovOptionExtension aNewCovoptExtn = NbaUtils.getFirstCovOptionExtension(covOption);
				if (aNewCovoptExtn != null && aNewCovoptExtn.hasAccelerationPct()) {
					aNbaOinkRequest.addValue(aNewCovoptExtn.getAccelerationPct());
				}
			}
		}
	}
	/**
	 * Obtain all the values for ImpairmentFlatExtra. OLifE.Party.Person.ImpairmentInfo.ImpairmentPermFlatExtraAmt
	 * @param aNbaOinkRequest - data request container
	 */
	// ACP010 New Method
	public void retrieveImpairmentFlatExtraInd(NbaOinkRequest aNbaOinkRequest) {
		Person person = null;
		int requestCount = aNbaOinkRequest.getCount(); 
		for (int i = 0; i < requestCount; i++) { 
			person = getPerson(aNbaOinkRequest, i);
			if (person != null) {
				OLifEExtension oli = null;
				int index_extension = getExtensionIndex(person.getOLifEExtension(), PERSON_EXTN);
				if (index_extension != -1) {
					oli = person.getOLifEExtensionAt(index_extension);
				}
				if (oli != null) {
					List impInfoList = oli.getPersonExtension().getImpairmentInfo(); 
					if (impInfoList != null && impInfoList.size() > 0) {
						for (int impCount = 0; impCount < impInfoList.size(); impCount++) {
							String TempFlatExtra=((ImpairmentInfo) impInfoList.get(impCount)).getImpairmentTempFlatExtraAmt();
							String PermFlatExtra=((ImpairmentInfo) impInfoList.get(impCount)).getImpairmentPermFlatExtraAmt();
							//Checking $0.0 in ImpairmentTempFlatExtraAmt and ImpairmentPermFlatExtraAmt bcoz $0 is acceptable
							if((TempFlatExtra!=null && !EMPTY_STRING.equalsIgnoreCase(TempFlatExtra) && !ZERO_AMT.equalsIgnoreCase(TempFlatExtra)) ||
									(PermFlatExtra!=null && !EMPTY_STRING.equalsIgnoreCase(PermFlatExtra) && !ZERO_AMT.equalsIgnoreCase(PermFlatExtra)) ){
								aNbaOinkRequest.addValue(true);
								return;
							}
						}
					}
				}
			}
		}
	}
	
	public void retrieveAssetRebalancingFrequency(NbaOinkRequest aNbaOinkRequest) {
        List arrList = getHolding().getArrangement();
        Iterator itr = arrList.iterator();
        while (itr.hasNext()) {
            Arrangement arrng = (Arrangement) itr.next();
            if (arrng.getArrType() == NbaOliConstants.OLI_ARRTYPE_ASSALLO && arrng.getOLifEExtensionCount() == 0) {
                aNbaOinkRequest.addValue(arrng.getArrMode());
                return;
            }
        }
    }    
    
    
    public void retrieveProRateGIAInd(NbaOinkRequest aNbaOinkRequest) {
        List arrList = getHolding().getArrangement();
        Iterator itr = arrList.iterator();
        while (itr.hasNext()) {
            Arrangement arrng = (Arrangement) itr.next();
            if (arrng.getArrType() == NbaOliConstants.OLI_ARRTYPE_CHARGEDEDUCTION && arrng.getOLifEExtensionCount() != 0) {
                ArrangementExtension arrExt = NbaUtils.getArrangementExtensionForSubType(arrng, NbaOliConstants.OLI_ARRSUBTYPE_FUNDBALFT);
                if (arrExt != null && arrExt.hasProRateGIAFundSupplInd()) {
                    aNbaOinkRequest.addValue(arrExt.getProRateGIAFundSupplInd());
                    return;
                }
            }
        }
    }   

//	APSL5221 New Method
	public void retrievePartyZipCode(NbaOinkRequest aNbaOinkRequest) {
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		Party party = null;
		String addressTypeCode = aNbaOinkRequest.getQualifier2();
		for (int i = 0; i < partyList.size(); i++) {
			party = (Party) partyList.get(i);
			List addressList = party.getAddress();
			for (int j = 0; addressList != null && j < addressList.size(); j++) {
				Address address = (Address) addressList.get(j);
				if (addressTypeCode.equalsIgnoreCase(Long.toString(address.getAddressTypeCode()))) {
					aNbaOinkRequest.addValue(address.getZip(), FORMAT_TYPE_ZIP);
					break;
				}
			}
			aNbaOinkRequest.addValue("", FORMAT_TYPE_ZIP);
		}
	}

	// APSL5164
	public boolean covOptionsDeniedInd() {
		Life life = getLife();
		List<Coverage> coverages = life.getCoverage();
		for (Coverage coverage : coverages) {
			List lifeParticipants = coverage.getLifeParticipant();
			Iterator itetr = lifeParticipants.iterator();
			while (itetr.hasNext()) {
				LifeParticipant lifeParticipant = (LifeParticipant) itetr.next();
				LifeParticipantExtension lifePatcntExtn = NbaUtils.getFirstLifeParticipantExtension(lifeParticipant);
				if (lifePatcntExtn != null && lifePatcntExtn.hasParticipantStatus()
						&& lifePatcntExtn.getParticipantStatus() == NbaOliConstants.OLI_CLISTAT_DECISSUE) {
					List uWResultlist = lifePatcntExtn.getUnderwritingResult();
					if (!NbaUtils.isBlankOrNull(uWResultlist)) {
						Iterator<UnderwritingResult> resultItr = uWResultlist.iterator();
						UnderwritingResult uWResult = null;
						while (resultItr.hasNext()) {
							uWResult = resultItr.next();
							UnderwritingResultExtension uWResultExt = NbaUtils.getFirstUnderwritingResultExtension(uWResult);
							if (uWResultExt.getUnderwritingReasonType() == NbaOliConstants.OLI_UWREASON_EXT_DENY) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Obtain the value for HomState, an instance of CountyName for an Address with type code = 1. OLifE.Party.Address.AddressExtention.CountyName is the
	 * address County
	 * @param aNbaOinkRequest - data request container
	 */
	//APSL5173 QC18415
	public void retrieveHomCounty(NbaOinkRequest aNbaOinkRequest) {
		for (int i = 0; i < aNbaOinkRequest.getCount(); i++) {
			Party aParty = getParty(aNbaOinkRequest, i);
			if (aParty != null) {
				Address address = getAddressForType(aParty, NbaOliConstants.OLI_ADTYPE_HOME);
				AddressExtension addExt = NbaUtils.getAddressExtension(address);
				if (addExt == null) {
					aNbaOinkRequest.addValue("");
				} else {
					aNbaOinkRequest.addValue(addExt.getCountyName());
				}
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue("");
		}
	}
	
	// APSL5128
		public void retrieveReqReceivedDate(NbaOinkRequest aNbaOinkRequest) {
			Date reqReceivedDate = null;
			ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
			Party party = null;
			String reqCode = aNbaOinkRequest.getQualifier2();
			for (int i = 0; i < partyList.size(); i++) {
				party = (Party) partyList.get(i);
				List requirementInfos = getNbaTXLife().getRequirementInfoList(party.getId(), Long.parseLong(reqCode));
				for (int j = 0; requirementInfos != null && j < requirementInfos.size(); j++) {
					RequirementInfo requirementInfo = (RequirementInfo) requirementInfos.get(j);
					if ((requirementInfo != null) && (requirementInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_RECEIVED)) {
						if (reqReceivedDate == null){
							reqReceivedDate =requirementInfo.getReceivedDate();
						}
						if(requirementInfo.getReceivedDate().after(reqReceivedDate)) {
							reqReceivedDate = requirementInfo.getReceivedDate();
						}
					}
				}

				if (!NbaUtils.isBlankOrNull(reqReceivedDate)) {
					aNbaOinkRequest.addValue(reqReceivedDate);
				} else {
					aNbaOinkRequest.addUnknownValue(new Date());
				}
			}
		}
		
		//APSL4856
		public void retrievePortalCreated(NbaOinkRequest aNbaOinkRequest) {
			ApplicationInfo applicationInfo = getApplicationInfo();
			if (applicationInfo != null && applicationInfo.getSubmissionType() == NbaOliConstants.OLI_APPSUBMITTYPE_ELECTRONIC) {
				aNbaOinkRequest.addValue(NbaOliConstants.OLI_APPSUBMITTYPE_ELECTRONIC);
			}
		}
		
	//APSL5173 
	public void retrieveDuplicateAmendEndorse(NbaOinkRequest aNbaOinkRequest) {
		aNbaOinkRequest.addValue(NbaUtils.isDuplicateAmendEndorse(getNbaTXLife(), null));
	}

	// New Method APSL5334  // Modified for APSL5391/QC-18810
	public void retrieveTermExpInd(NbaOinkRequest aNbaOinkRequest) {
		NbaTXLife nbaTXLife = getNbaTXLife();
		ApplicationInfo appInfo = getApplicationInfo();
		String termExpressInd = null;
		if (appInfo != null) {
			ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(appInfo);
			if (applicationInfoExtension != null && applicationInfoExtension.getTermExpressInd()) {
				// charles bailey case no
				try {
					if (isValidAgent(NbaUtils.getProducerID(nbaTXLife))) {
						termExpressInd = NbaConstants.NO_VALUE;
					} else {
						termExpressInd = NbaConstants.YES_VALUE;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (applicationInfoExtension != null && !applicationInfoExtension.hasTermExpressInd()) {
				termExpressInd = NbaConstants.NO_VALUE;
			}
		}
		aNbaOinkRequest.addValue(termExpressInd);
	}
				
	
	// New method NBLXA-1288
			public void retrievePreventProcessActivity(NbaOinkRequest aNbaOinkRequest) {
				boolean preventProcessActivity = false;
				List<Activity> activityList = getNbaTXLife().getOLifE().getActivity();
				for(Activity activity : activityList){
					if(activity != null && activity.getActivityTypeCode() == NbaOliConstants.OLI_ACTTYPE_PREVENT_PROCESS && activity.getActivityStatus() == NbaOliConstants.OLI_ACTSTAT_COMPLETE){
						preventProcessActivity=true;
					}
				}
				aNbaOinkRequest.addValue(preventProcessActivity);
			}
			
			
			//New Method NBLXA186-NBLXA1272 //APSL5391/QC-18810  
	protected boolean isValidAgent(String producerid) {
		boolean isCharlesBaileyAgentID = false;
		NbaVpmsResultsData agentData = getDataFromVPMSForIdentifyingAgent(NbaVpmsConstants.CONTRACTVALIDATIONCALCULATIONS,
				NbaVpmsConstants.EP_AGENTVALIDATION, producerid);
		if (agentData != null && agentData.getResultsData() != null) {
			isCharlesBaileyAgentID = Boolean.parseBoolean(((String) agentData.getResultsData().get(0)));
		}
		return isCharlesBaileyAgentID;
	}

	// New Method NBLXA186-NBLXA1272 //APSL5391/QC-18810
	public NbaVpmsResultsData getDataFromVPMSForIdentifyingAgent(String modelName, String entryPoint, String producerID) {
		NbaVpmsAdaptor adapter = null;
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(new NbaLob());
			adapter = new NbaVpmsAdaptor(oinkData, modelName);
			Map deOinkMap = new HashMap();
			deOinkMap.put("A_PRODUCERID", producerID);
			adapter.setSkipAttributesMap(deOinkMap);
			adapter.setVpmsEntryPoint(entryPoint);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(adapter.getResults());
			return vpmsResultsData;
		} catch (Exception e) {
			getLogger().logDebug("Problem in getting valid agent from VPMS" + e.getMessage());
		} finally {
			try {
				if (adapter != null) {
					adapter.remove();
				}
			} catch (Throwable th) {
				// ignore, nothing can be done
				th.printStackTrace();
			}
		}
		return null;
	}
	
	public void retrieveACHPayerTypePartCorpLLCInd(NbaOinkRequest aNbaOinkRequest) {
		NbaParty party = NbaUtils.getPayerParty(getNbaTXLife());
		PartyExtension achPayerPartyExtension = NbaUtils.getFirstPartyExtension(party.getParty());
		if (achPayerPartyExtension == null) {
			aNbaOinkRequest.addValue("");
		} else {			
			aNbaOinkRequest.addValue(achPayerPartyExtension.getPayerTypePartCorpLLCInd());
		}
	}
	
	//NBLXA-1651 new method
	public void retrieveManualReinTypeActivity(NbaOinkRequest aNbaOinkRequest) {
		ReinsuranceInfo reinInfo = getNbaTXLife().getDefaultReinsuranceInfo();
		if (reinInfo !=null && !reinInfo.hasReinsuranceRiskBasis()) {
			List activityList = NbaUtils.getActivityByTypeCode(getNbaTXLife().getOLifE().getActivity(),
					NbaOliConstants.OLI_ACTTYPE_1009900004);
			if (activityList.size() > 0) {
				aNbaOinkRequest.addValue(true);
			}else {
				aNbaOinkRequest.addValue(false);
			}
		}
	}
	
	//NBLXA-1632
	public void retrieveTxlifeDefLifeInsMethod(NbaOinkRequest aNbaOinkRequest) {
		Life life = getNbaTXLife().getLife();
		if (life != null) {
			LifeUSA lifeUSA = life.getLifeUSA();
			if (lifeUSA != null && lifeUSA.hasDefLifeInsMethod()) {
				aNbaOinkRequest.addValue(lifeUSA.getDefLifeInsMethod());
			}
		}
		if (aNbaOinkRequest.getValue().size() == 0) {
			aNbaOinkRequest.addValue(-1);
		}
	}
	
	/**
	 * Obtain the value for IsHepatitisImpEff based on SubmissionDate. OLifE.Holding.Policy.ApplicationInfo.SubmissionDate is the date application was signed.
	 * @param aNbaOinkRequest - data request container
	 */
	//NBLXA-1740 New Method
	public void retrieveIsHepatitisImpEff(NbaOinkRequest aNbaOinkRequest) {
		Date submissionDate = null;
		if(getApplicationInfo() != null){
			submissionDate = getApplicationInfo().getSubmissionDate();
		}
		Date impEffectiveDate = NbaUtils.getDateFromStringInUSFormat(NbaConstants.NBLXA1740DEFFDATE);
		if (NbaUtils.compare(submissionDate, impEffectiveDate) >= 0) {
			aNbaOinkRequest.addValue(true);
		} else {
			aNbaOinkRequest.addValue(false);
		}
	}
	
	// Begin NBLXA-1722
		public void retrievePreviousStatus(NbaOinkRequest aNbaOinkRequest) {
			String previoursStatus= "";
			List<Activity> activityList = getNbaTXLife().getOLifE().getActivity();
			for(Activity activity : activityList){
				if(activity != null && activity.getActivityTypeCode() == NbaOliConstants.OLI_ACTTYPE_CLOSURE_CHECK && activity.getActivityStatus() == NbaOliConstants.OLI_ACTSTAT_COMPLETE){
					ActivityExtension activityExtn = NbaUtils.getFirstActivityExtension(activity);
					if(activityExtn != null && !NbaUtils.isBlankOrNull(activityExtn.getUserQueue())){
						previoursStatus = activityExtn.getUserQueue();
					}
				}
			}
			aNbaOinkRequest.addValue(previoursStatus);
		}
		
		//End NBLXA-1722
		
	    //Begin NBLXA-1799 New Method
	    public void retrieveCIPEPaymentInd(NbaOinkRequest aNbaOinkRequest) {
	        double totalCWA = 0;
	        double requiredPayment = 0;
	        boolean cipePayment = false;
	        
	        if (getPolicy() != null) { 
	            if (AxaUtils.isPermProduct(getPolicy().getProductType())) {
	                requiredPayment = getPolicy().getMinPremiumInitialAmt();
	            } else {
	                requiredPayment = getPolicy().getPaymentAmt();
	            }
	            long finActivitySubType = 0; 
	            List <FinancialActivity> finActivity=getPolicy().getFinancialActivity();
	            if(finActivity!=null && finActivity.size()>0){
	              	SortingHelper.sortData(finActivity, true, NbaConstants.FINEFFDATE);
				}
	            for (FinancialActivity financialActivity:finActivity) {
	                String finActivityType = Long.toString(financialActivity.getFinActivityType());
	                finActivitySubType = financialActivity.getFinActivitySubType(); 
	                boolean paymentAddition = false;
	                int arrayLength = NbaConstants.CWA_PAYMENT_ADDITIONS.length;
	                for (int j = 0; j < arrayLength ; j++) {
	                    if (finActivityType.equals(NbaConstants.CWA_PAYMENT_ADDITIONS[j])) {
	                        paymentAddition = true;
	                        break;
	                    }
	                }
	                if (paymentAddition) {
	                    FinancialActivityExtension financialActivityExtension = NbaUtils.getFirstFinancialActivityExtension(financialActivity);
	                    if (financialActivityExtension != null) {
	                        if (financialActivityExtension.getDisbursedInd()) {
	                            paymentAddition = false;
	                        }
	                    }
	                    if (NbaOliConstants.OLI_FINACTSUB_REV == finActivitySubType
	                        || NbaOliConstants.OLI_FINACTSUB_REFUND == finActivitySubType
	                        || NbaOliConstants.OLI_FINACTSUB_PARTIALREFUND == finActivitySubType) { 
	                        paymentAddition = false; 
	                    } 
	                }
	                if (paymentAddition) {
	                    totalCWA = (double) Math.round((totalCWA + financialActivity.getFinActivityGrossAmt()) * 100) / 100;
	                    if(totalCWA>=requiredPayment-10) {
	                    	FinancialActivityExtension financialActivityExtension = NbaUtils.getFirstFinancialActivityExtension(financialActivity);
		                    if (financialActivityExtension != null) {
		                        if (financialActivityExtension.hasCipePaymentType()) {
		                        	cipePayment = true;
		                        	break;
		                        }
		                    }
	                        
	                    }

	                }
	            }
	        }
	        aNbaOinkRequest.addValue(cipePayment);
	    }  
	    //END NBLXA-1799
	
	    //Start NBLXA-1794
	    public void retrieveReqLabCollectedDate(NbaOinkRequest aNbaOinkRequest) {
	    	Date labCollectedDate = null;
	    	ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
	    	Party party = null;
	    	String reqCode = aNbaOinkRequest.getQualifier2();
	    	for (int i = 0; i < partyList.size(); i++) {
	    		party = (Party) partyList.get(i);
	    		List requirementInfos = getNbaTXLife().getRequirementInfoList(party.getId(), Long.parseLong(reqCode));
	    		for (int j = 0; requirementInfos != null && j < requirementInfos.size(); j++) {
	    			RequirementInfo requirementInfo = (RequirementInfo) requirementInfos.get(j);
	    			RequirementInfoExtension reqInfoExtn = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
	    			if (reqInfoExtn != null && reqInfoExtn.hasLabCollectedDate()) {
	    				if ((requirementInfo != null) && (requirementInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_RECEIVED)) {
	    					if (labCollectedDate == null || reqInfoExtn.getLabCollectedDate().after(labCollectedDate)) {
	    						labCollectedDate = reqInfoExtn.getLabCollectedDate();
	    					}
	    				}
	    			}
	    		}
	    		if (!NbaUtils.isBlankOrNull(labCollectedDate)) {
	    			aNbaOinkRequest.addValue(labCollectedDate);
	    		} else {
	    			aNbaOinkRequest.addUnknownValue(new Date());
	    		}
	    	}		
	    }
	    //END NBLXA-1794
	    
	// NBLXA-1554(NBLXA-2042) new method
	public void retrieveReplacmentTypeAsExternal(NbaOinkRequest aNbaOinkRequest) {
		List<Holding> replHoldingList = AxaValueObjectUtils.getOthInsCompanyHoldingListForIns(getOLifE());
		boolean isExternal = false;
		if (!NbaUtils.isBlankOrNull(replHoldingList)) {
			for (Holding holding : replHoldingList) {
				if (!NbaUtils.isBlankOrNull(holding) && !holding.isActionDelete() && !NbaUtils.isBlankOrNull(holding.getPolicy())) {
					PolicyExtension polExtn = NbaUtils.getFirstPolicyExtension(holding.getPolicy());
					if (!NbaUtils.isBlankOrNull(polExtn) && polExtn.getReplacementIndCode() == NbaOliConstants.NBA_ANSWERS_YES) {
						NbaParty party = nbaTXLife.getParty(NbaUtils.getPartyIdFromOriginatingParty(nbaTXLife, NbaOliConstants.OLI_REL_HOLDINGCO,
								holding.getId()));
						String replacedPartyKey = party.getParty().getPartyKey();
						if (replacedPartyKey != null) {
							if (!NbaUtils.isInternalReplacementCompanyForMI(replacedPartyKey)) {
								isExternal = true;
							}
						}
					}
				}
			}
		}
		if (isExternal) {
			aNbaOinkRequest.addValue(true);
		} else {
			aNbaOinkRequest.addValue(false);
		}
	}

	// NBLXA-2155(NBLXA-2194) new method
	public void retrieveIllustrationStatus(NbaOinkRequest aNbaOinkRequest) {
		PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(getPolicy());
		if (policyExt != null) {
			aNbaOinkRequest.addValue(policyExt.getIllustrationStatus());
		}
	}
	
	/**
	 * Obtain the value for ApplicationState
	 * @param aNbaOinkRequest - data request container
	 */
	// NBLXA-2155(NBLXA-2200) new method
	public void retrieveApplicationState(NbaOinkRequest aNbaOinkRequest) {
		if (!NbaUtils.isBlankOrNull(getPolicy())){
			ApplicationInfo appInfo = getPolicy().getApplicationInfo();
			if (!NbaUtils.isBlankOrNull(appInfo)){
				aNbaOinkRequest.addValue(appInfo.getApplicationJurisdiction());
			}
		}
	}
	
	// NBLXA-2177 new method
	public void retrieveInternalReplacement(NbaOinkRequest aNbaOinkRequest) {
		List releationList = getNbaTXLife().getOLifE().getRelation();
		List releationListHoldingCo = getNbaTXLife().getOLifE().getRelation();
		Relation relation = null;
		boolean internalReplacement = false;
		if (releationList != null) {
			for (int i = 0; i < releationList.size(); i++) {
				relation = (Relation) releationList.get(i);
				if (NbaOliConstants.OLI_REL_REPLACEDBY == relation.getRelationRoleCode()) {
					if (releationListHoldingCo != null) {
						for (int j = 0; j < releationListHoldingCo.size(); j++) {
							Relation repRelation = (Relation) releationListHoldingCo.get(j);
							if (NbaOliConstants.OLI_REL_HOLDINGCO == repRelation.getRelationRoleCode()
									&& relation.getRelatedObjectID().equals(repRelation.getOriginatingObjectID())) {
								NbaParty nbaParty = nbaTXLife.getParty(repRelation.getRelatedObjectID());
								String replacedPartyKey = nbaParty.getParty().getPartyKey();
								if (replacedPartyKey != null) {
									if (NbaUtils.isInternalReplacementCompany(replacedPartyKey)) {
										internalReplacement = true;
										break;
									}
								}
							}
						}
						if(internalReplacement){
							aNbaOinkRequest.addValue(true);
							break;
						}
					}
				}
			}
		}
	}
	
	//Changes for user story 268964 - Start
	public void retrieveLexisNexisRef(NbaOinkRequest aNbaOinkRequest) {
		String productReference = null;
		Date reqReceivedDate = null;
		ArrayList partyList = getPartiesByQualifier(aNbaOinkRequest);
		Party party = null;
		for (int i = 0; i < partyList.size(); i++) {
			party = (Party) partyList.get(i);
			List requirementInfos = getNbaTXLife().getRequirementInfoList();
			for (int j = 0; requirementInfos != null && j < requirementInfos.size(); j++) {
				RequirementInfo requirementInfo = (RequirementInfo) requirementInfos.get(j);
				if (requirementInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_RISKCLASSIFIER) {
					RequirementInfoExtension reqInfoExtn = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
					if (reqInfoExtn != null && reqInfoExtn.hasProductReference()) {
						if ((requirementInfo != null) && (requirementInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_RECEIVED)) {
							if (reqReceivedDate == null) {
								reqReceivedDate = requirementInfo.getReceivedDate();
								productReference = reqInfoExtn.getProductReference();
							}
							if (requirementInfo.getReceivedDate().after(reqReceivedDate)) {
								reqReceivedDate = requirementInfo.getReceivedDate();
								productReference = reqInfoExtn.getProductReference();
							}

						}
					}
				}
			}
			// to do check for latest date
			if (!NbaUtils.isBlankOrNull(productReference)) {
				aNbaOinkRequest.addValue(productReference);
			} else {
				aNbaOinkRequest.addUnknownValue("");
			}
		}
	}
	//Changes for user story 268964 - End
	
	// NBLXA-2380
	public void retrieveisFarmerAgent(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {

		if (NbaUtils.isFarmerAgent(getNbaTXLife()))
			aNbaOinkRequest.addValue(NbaConstants.TRUE_STR);
		else
			aNbaOinkRequest.addValue(NbaConstants.FALSE_STR);
	}
	
	/**
	 * Retrieves HOLD_FOR_ALERT from AxaGIAppOnboardingDataAccessor
	 */
	//NBLXA-2299 New Method
	public void retrieveHoldPolicyForHighAlert(NbaOinkRequest aNbaOinkRequest) {
		ApplicationInfo appInfo = getNbaTXLife().getPolicy().getApplicationInfo();
		if(appInfo!=null && appInfo.getApplicationType()==NbaOliConstants.OLI_APPTYPE_GROUPAPP){						
			PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(getPolicy());			
			aNbaOinkRequest.addValue(NbaUtils.isHoldPolicyForHighAlert(policyExt.getGuarIssOfferNumber(), getPolicy().getPolNumber()));			
		}
	}
	
	// NBLXA-2328[NBLXA-2496]
	public void retrieveisCV1918Present(NbaOinkRequest aNbaOinkRequest) {
		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(getOLifE());
		SystemMessage sysMsg = null;
		SystemMessageExtension sysMsgExt = null;
		for (int i = 0; i < holding.getSystemMessageCount(); i++) {
			sysMsg = holding.getSystemMessageAt(i);
			if (!NbaUtils.isBlankOrNull(sysMsg) && sysMsg.getMessageCode() == NbaConstants.CV_1918) {
				sysMsgExt = NbaUtils.getFirstSystemMessageExtension(sysMsg);
				if (!sysMsg.isDeleted() && !NbaUtils.isBlankOrNull(sysMsgExt) && !sysMsgExt.getMsgOverrideInd()) {
					aNbaOinkRequest.addValue(NbaConstants.TRUE_STR);
				} else {
					aNbaOinkRequest.addValue(NbaConstants.FALSE_STR);
				}
			}
		}
	}
	
	// NBLXA-2572[NBLXA2328]
	public void retrieveAdditionalReplacement(NbaOinkRequest aNbaOinkRequest) {
		boolean additionalReplFlag = NbaUtils.isAdditionalReplacement(getNbaTXLife());
		if (additionalReplFlag) {
			aNbaOinkRequest.addValue(NbaConstants.TRUE_STR);
		} else {
			aNbaOinkRequest.addValue(NbaConstants.FALSE_STR);
		}
	}


}
