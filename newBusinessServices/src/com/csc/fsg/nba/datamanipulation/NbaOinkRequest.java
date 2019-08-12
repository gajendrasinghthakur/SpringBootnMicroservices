package com.csc.fsg.nba.datamanipulation; //NBA201

/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 *  are proprietary to CSC Financial Services Group�.  The use,<BR>
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
import java.util.Date;
import java.util.Vector;

import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaTime;

/**
 *  NbaOinkRequest provides a container for the variable name and values. The 
 *  requesting method sets the variable name and, when storing values, the new value(s).  
 *  It then messages an instance of NbaOinkDataAccess with the NbaOinkRequest as an 
 *  argument. Filter and count values may be set in the NbaOinkRequest to control the 
 *  values to be accessed. Filter values are used to control the occurrences of a 
 *  value are to be accessed. A filter for specific Party occurrences may be set 
 *  either by setting a specific occurrence number, or by identifying a RelatedRefID 
 *  and RelationRoleCode. A coverage filter may be used to identify a specific 
 *  occurrence of Coverage for a Party.  A covOption filter may be used to specify 
 *  a specific occurrence of CovOption for a Coverage. All occurrences are relative 
 *  to zero (0 = the first occurrence, 1 = the second, etc.).  The count is used to 
 *  determine the maximum number of occurrences to be accessed. If a count value is 
 *  not set, 1 occurrence is assumed.
 *  <p>
 *  <b>Modifications:</b><br>
 *  <table border=0 cellspacing=5 cellpadding=5>
 *  <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 *  </thead>
 *  <tr><td>NBA021</td><td>Version 2</td><td>Object Interactive Name Keeper</td></tr>
 *  <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 *  <tr><td>NBA061</td><td>Version 3</td><td>Adopt Struts Framework</td></tr>
 *  <tr><td>NBA072</td><td>Version 3</td><td>Calculations</td></tr>
 *  <tr><td>NBP001</td><td>Version 3</td><td>nb Producer</td></tr>
 *  <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 *  <tr><td>ACP001</td><td>Version 4</td><td>IU-Lab Result Processing</td></tr>
 * 	<tr><td>ACP002</td><td>Version 4</td><td>AC Summary</td></tr>
 *  <tr><td>ACP005</td><td>Version 4</td><td>IU-UFS</td></tr>
 *  <tr><td>ACP007</td><td>Version 4</td><td>Medical Screening</td></tr>
 * 	<tr><td>NBA115</td><td>Version 5</td><td>Credit Card Payment and Authorization</td></tr>
 *  <tr><td>SPR2722</td><td>Version 6</td><td>Application Entry Creates Multiple Primary Producer Relations Instead of One Primary and Others Additional Writing.</td></tr>
 * 	<tr><td>NBA132</td><td>Version 6</td><td>Equitable Distribution of Work</td></tr>
 *  <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * <tr><td>AXAL3.7.07</td><td>AXA Life Phase 1</td><td>Automated Underwriting</td></tr>
 * <tr><td>AXAL3.7.06</td><td>AXA Life Phase 1</td><td>Requirement Determination</td></tr>
 * <tr><td>AXAL3.7.13I</td><td>AXA Life Phase 1</td><td>Informal Corresponsence</td></tr>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * <tr><td>P2AXAL005</td><td>AXA Life Phase 2</td><td>Legal Policy Stop</td></tr>
 * <tr><td>P2AXAL035</td><td>AXA Life Phase 2</td><td>Amendment / Endorsement / Delivery Instructions</td></tr>
 * <tr><td>P2AXAL016</td><td>AXA Life Phase 2</td><td>Product Validation</td></tr>
 * <tr><td>SR641590(APSL2012)</td><td>Discretionary</td><td>SUB-BGA SR</td></tr>
 * <tr><td>APSL3447</td><td>Discretionary</td><td>HVT</td></tr>
 * <tr><td>APSL4872</td><td>Discretionary</td><td>Requirement As Data</td></tr>
 *  </table>
 *  <p>
 *  @author CSC FSG Developer
 * @version 7.0.0
 *  @see com.csc.fsg.nba.datamanipulation.NbaRetrieveContractData
 *  @see com.csc.fsg.nba.datamanipulation.NbaUpdateContract
 *  @see com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess
 *  @see com.csc.fsg.nba.datamanipulation.NbaOinkDefaultFormatter
 *  @see com.csc.fsg.nba.datamanipulation.NbaOinkHTMLFormatter
 *  @see com.csc.fsg.nba.datamanipulation.NbaRetrieveLOB
 *  @see com.csc.fsg.nba.datamanipulation.NbaUpdateLOB
 *  @since New Business Accelerator - Version 2
 * 
 */
public class NbaOinkRequest implements NbaDataFormatConstants, NbaContractDataAccessConstants {
    protected int count;
    protected int partyFilter = noFilterInt;
    protected int coverageFilter = noFilterInt;
	protected int covTypeCodeFilter = noFilterInt; //ACP002
	protected String coverageIdFilter = noFilterString; //ACP002
	protected int medConditionFilter = noFilterInt; //ACP007
    protected int covOptionFilter = noFilterInt;
	protected int lifeParticipantFilter = noFilterInt;
    protected java.lang.String variable;
    protected java.util.Vector value;
    final static int noFilterInt = -1;
    final static String noFilterString = "";
    final static long noFilterLong = -1L;
    final static Vector qualifierTypes = new Vector();
    final static Vector formattingTypes = new Vector();
    final static Vector dataGroupTypes = new Vector();
    final static Vector covOptionFilterTypes = new Vector();//P2AXAL016
    final static Vector qualifier2Types = new Vector(); //APSL4872
    protected java.lang.String rootVariable;
    static {
        dataGroupTypes.add("MED");
        dataGroupTypes.add("RIS");
        dataGroupTypes.add("RIX");
        //Begin AXAL3.7.07
        dataGroupTypes.add("AIX");  //ApplicationInfoExtension
        dataGroupTypes.add("PAX");  //PartyExtension
        dataGroupTypes.add("PEX");  //PersonExtension
        dataGroupTypes.add("CLX");  //ClientExtension
        dataGroupTypes.add("MEX");  //MedicalExamExtension        
        dataGroupTypes.add("INX");  //IntentExtension
        dataGroupTypes.add("EMP");  //Employment
        dataGroupTypes.add("TIA");  //TempInsAgreementInfo
        //End AXAL3.7.07
        //Begin AXAL3.7.06
        dataGroupTypes.add("POX");	//PolicyExtension
        //End AXAL3.7.06
        dataGroupTypes.add("TAD");	//TempInsAgreementDetails A4_AXAL001
        dataGroupTypes.add("EIX");	//EmploymentExtension P2AXAL066
        qualifierTypes.add(PARTY_BENEFICIARY);
        qualifierTypes.add(PARTY_COBENEFICIARY);
        qualifierTypes.add(PARTY_INSURED);
        qualifierTypes.add(PARTY_JOINT_INSURED);
        qualifierTypes.add(PARTY_OTHER_INSURED);
        qualifierTypes.add(PARTY_OWNER);
        qualifierTypes.add(PARTY_PAYOR);
        qualifierTypes.add(PARTY_REQUESTEDBY);
        qualifierTypes.add(PARTY_PRIM_INSURED);
		qualifierTypes.add(PARTY_PRIMARY_AND_JOINT_INSURED);	//NBA100
        qualifierTypes.add(PARTY_PRIM_OWNER);
        qualifierTypes.add(PARTY_PRIWRITINGAGENT);
        qualifierTypes.add(PARTY_PRIWRITINGAGENTAGENCY);  //NBA132
        qualifierTypes.add(PARTY_ADDWRITINGAGENT);//SPR2722
		qualifierTypes.add(PARTY_SAG);	//ACP001
        qualifierTypes.add(PARTY_SPOUSE);
        qualifierTypes.add(PARTY_CARRIER);
        qualifierTypes.add(BASE_COV);
		qualifierTypes.add(RIDER);
		qualifierTypes.add(BENEFIT);
		qualifierTypes.add(NON_RIDER_COV);
		qualifierTypes.add(ACCIDENTAL_DEATH_BENEFIT);		        
		qualifierTypes.add(WAIVER_OF_PREMIUM); //ACP002
		qualifierTypes.add(GUARANTEED_INSURABILITY_RIDER); //ACP002
		qualifierTypes.add(PLAN);	
		qualifierTypes.add(PARTY_ANNUITANT); //NBP001	        
		qualifierTypes.add(CREDIT_CARD_PAYMENT);//NBA115
		qualifierTypes.add(PARTY_MULTIPLEASSIGNEE);//P2AXAL005
		//Begin NBA186
        qualifierTypes.add(UNDERWRITER_LEVEL1);
        qualifierTypes.add(UNDERWRITER_LEVEL2);
        qualifierTypes.add(UNDERWRITER_LEVEL3);
        //End NBA186
		qualifierTypes.add(PARTY_BGA);	//AXAL3.7.13I		
		qualifierTypes.add(PARTY_SBGA);	//SR641590  SUB-BGA	
		qualifierTypes.add(PARTY_BCM); //AXAL3.7.13
		qualifierTypes.add(PARTY_SBCM);//SR641590 SUB-BGA
		qualifierTypes.add(PARTY_REPLCOMP); //P2AXAL028
		qualifierTypes.add(PARTY_APPLCNT);//ALII104
		qualifierTypes.add(FORM_VUL); //P2AXAL035
		qualifierTypes.add(CONV); //NBA300
		qualifierTypes.add(PARTY_AGENT);//P2AXAL028
		qualifierTypes.add(CTIR_COV); //P2AXAL024
        qualifierTypes.add(CLR_COV); //P2AXAL024
        qualifierTypes.add(ROPR_BENEFIT); //ALII1012, P2AXAL040
        qualifierTypes.add(LTC_BENEFIT); //ALII1012, P2AXAL040
        qualifierTypes.add(PARTY_PIR);	//APSL3447
        qualifierTypes.add(PARTY_PROCESSFIRM);	//APSL3447
        qualifierTypes.add(PARTY_CONTRACTFIRM);	//APSL3447
        qualifierTypes.add(PARTY_PROCESSFIRMCM);	//APSL3447
        qualifierTypes.add(Long.toString(NbaOliConstants.OLI_REQCODE_REPFORM)); //APSL3619
        qualifierTypes.add(Long.toString(NbaOliConstants.OLI_REQCODE_1009800033)); //APSL3619
        formattingTypes.add("EF");
        formattingTypes.add("DD");
        formattingTypes.add("RB");
        formattingTypes.add("CB");
        formattingTypes.add("MMDDYYY");
        formattingTypes.add("NUM");
        formattingTypes.add("CUR");
        formattingTypes.add("LFM");

        covOptionFilterTypes.add(ACCIDENTAL_DEATH_BENEFIT);	//P2AXAL016	        
        covOptionFilterTypes.add(WAIVER_OF_PREMIUM);//P2AXAL016	
        covOptionFilterTypes.add(GUARANTEED_INSURABILITY_RIDER); //P2AXAL016
        qualifier2Types.add(Long.toString(NbaOliConstants.OLI_REQCODE_MEDFME)); 
         //APSL4872 Start
        qualifier2Types.add(Long.toString(NbaOliConstants.OLI_REQCODE_SIGNEDAPP)); 
        qualifier2Types.add(Long.toString(NbaOliConstants.OLI_REQCODE_DVR)); 
        qualifier2Types.add(Long.toString(NbaOliConstants.OLI_REQCODE_INSPRPTQUES)); 
        qualifier2Types.add(Long.toString(NbaOliConstants.OLI_REQCODE_PPR)); 
        qualifier2Types.add(Long.toString(NbaOliConstants.OLI_REQCODE_MVRPT)); 
        qualifier2Types.add(Long.toString(NbaOliConstants.OLI_REQCODE_EKGTREAD)); 
        qualifier2Types.add(Long.toString(NbaOliConstants.OLI_REQCODE_EKGREST)); 
        qualifier2Types.add(Long.toString(NbaOliConstants.OLI_REQCODE_URINE)); 
        qualifier2Types.add(Long.toString(NbaOliConstants.OLI_REQCODE_BLOOD)); 
        qualifier2Types.add(Long.toString(NbaOliConstants.OLI_REQCODE_MEDEXAMPARAMED)); 
        //APSL4872 End
        //APSL5221 : start
        qualifier2Types.add(Long.toString(NbaOliConstants.OLI_ADTYPE_HOME));
        qualifier2Types.add(Long.toString(NbaOliConstants.OLI_ADTYPE_BUS));
        qualifier2Types.add(Long.toString(NbaOliConstants.OLI_ADTYPE_VAC));
//      APSL5221 : End

    }
    protected java.lang.String qualifier;
    protected java.lang.String qualifier2; //APSL4872
    protected java.lang.String dataGroup;
    protected int formatting;
    protected boolean enabled;
    protected int valueType;
    static final int VALUE_STRING = 1;
    static final int VALUE_INT = 5;
    static String PRODUCT_ANNUITY = "ANN";
    static final int VALUE_DATE = 2;
    static final int VALUE_TIME = 6;
    static final int VALUE_LONG = 3;
    static final int VALUE_DOUBLE = 4;
    static final int VALUE_BOOLEAN = 0;
    protected java.lang.String table = null;
    protected int contentType;
    protected long relationRoleCode = noFilterLong;
    protected java.lang.String relatedRefID = noFilterString;
    protected boolean tableTranslations = false;
    protected String besTranslate = "";
    protected String requirementIdFilter = ""; //ACP001
	protected String labTestCodeFilter = ""; //ACP001 
	protected Object[] args = null;//ACP002
	protected String relatedObjectTypeFilter = ""; //ACP005
	protected int elementIndexFilter = 0; //ACP005
	protected boolean parseMultiple = false;//P2AXAL028
/**
 * NbaDataRequest constructor comment.
 */
public NbaOinkRequest() {
	super();
}
/**
 * Add an unknown NbaTime value to the Map containing the resolved values.
 * @param newValue 
 */
void addUnknownValue(NbaTime newValue) {
    setValueType(VALUE_TIME);
    setTable(null);
    setContentType(FORMAT_TYPE_TEXT);
    getValue().add(null);
}
/**
 * Add an unknown String value to the Map containing the resolved values.
 * @param newValue 
 */
void addUnknownValue(String newValue) {
	setValueType(VALUE_STRING);
	setTable(null);
	setContentType(FORMAT_TYPE_TEXT);
	getValue().add(null);
}
/**
 * Add an unknown Date value to the Map containing the resolved values.
 * @param newValue 
 */
void addUnknownValue(Date newValue) {
	setValueType(VALUE_DATE);
	setTable(null);
	setContentType(FORMAT_TYPE_TEXT);
	getValue().add(null);
}
/**
 * Add a value to the Map containing the resolved values.
 * @param newValue 
 */
void addUnknownValueForType(Class type) {
    String className = type.getName().toUpperCase();
    if (className.endsWith("DATE")) {
        addUnknownValue(new Date());
    } else if (className.toUpperCase().endsWith("NBATIME")) {
        addUnknownValue(new NbaTime());
    } else if (className.endsWith("STRING")) {
        addUnknownValue("");
    } else if (className.endsWith("DOUBLE")) {
        addValue(0.0);
    } else if (className.endsWith("LONG")) {
        addValue(-1L);
    } else if (className.endsWith("BOOLEAN")) {
        setValueType(VALUE_BOOLEAN);
        setTable(null);
        setContentType(FORMAT_TYPE_TEXT);
        getValue().add(null);
    }
}
/**
 * Add a Double value to the Map containing the resolved values
 * and set the translation table.
 * @param newValue 
 */
void addValue(double newValue) {
	setValueType(VALUE_DOUBLE);
	setTable(null);
	setContentType(FORMAT_TYPE_TEXT);
	getValue().add(new Double(newValue));
}
/**
 * Add a Double value to the Map containing the resolved values
 * and set the translation table.
 * @param newValue 
 */
void addValue(double newValue, int aContentType) {
	setValueType(VALUE_DOUBLE);
	setTable(null);
	setContentType(aContentType);
	getValue().add(new Double(newValue));
}
/**
 * Add a Long value to the Map containing the resolved values.
 * @param newValue 
 */
void addValue(int newValue) {
	setValueType(VALUE_INT);
	setTable(null);
	setContentType(FORMAT_TYPE_TEXT);
	getValue().add(new Integer(newValue));
}
/**
 * Add a Long value to the Map containing the resolved values.
 * @param newValue 
 */
void addValue(long newValue) {
	setValueType(VALUE_LONG);
	setTable(null);
	setContentType(FORMAT_TYPE_TEXT);
	getValue().add(new Long(newValue));
}
/**
 * Add a Long value to the Map containing the resolved values
 * and set the translation table.
 * @param newValue 
 */
void addValue(long newValue, String aTable) {
	setValueType(VALUE_LONG);
	setTable(aTable);
	setContentType(FORMAT_TYPE_TEXT);
	getValue().add(new Long(newValue));
}
/**
 * Add a NbaTime value to the Map containing the resolved values.
 * @param newValue 
 */
void addValue(NbaTime newValue) {
    setValueType(VALUE_TIME);
    setTable(null);
    setContentType(FORMAT_TYPE_TEXT);
    getValue().add(newValue);
}
/**
 * Add a String value to the Map containing the resolved values.
 * @param newValue 
 */
void addValue(String newValue) {
	setValueType(VALUE_STRING);
	setTable(null);
	setContentType(FORMAT_TYPE_TEXT);
	getValue().add(newValue);
}
/**
 * Add a String value to the Map containing the resolved values.
 * @param newValue 
 */
void addValue(String newValue, int aFormat) {
	setValueType(VALUE_STRING);
	setTable(null);
	setContentType(aFormat);
	getValue().add(newValue);
}
/**
 * Add a String value to the Map containing the resolved values.
 * @param newValue 
 */
void addValue(String newValue, String aTable) {
	setValueType(VALUE_STRING);
	setTable(aTable);
	setContentType(FORMAT_TYPE_TEXT);
	getValue().add(newValue);
}
/**
 * Add a Date value to the Map containing the resolved values.
 * @param newValue 
 */
void addValue(Date newValue) {
	setValueType(VALUE_DATE);
	setTable(null);
	setContentType(FORMAT_TYPE_TEXT);
	getValue().add(newValue);
}
/**
 * Add a Boolean value to the Map containing the resolved values.
 * @param newValue 
 */
void addValue(boolean newValue) {
	setValueType(VALUE_BOOLEAN);
	setTable(null);
	setContentType(FORMAT_TYPE_TEXT);
	getValue().add(new Boolean(newValue));
}
/**
 * Add a value to the Map containing the resolved values.
 * @param newValue 
 */
void addValueForType(Object newValue, Class type) {
    if (type.getName().endsWith("Date")) {
        addValue((Date) newValue);
    } else if (type.getName().toUpperCase().endsWith("NBATIME")) {
        addValue((NbaTime) newValue);
    } else if (type.getName().toUpperCase().endsWith("STRING")) {
        addValue((String) newValue);
    } else if (type.getName().toUpperCase().endsWith("DOUBLE")) {
        addValue(((Double) newValue).doubleValue());
    } else if (type.getName().toUpperCase().endsWith("LONG")) {
        addValue(((Long) newValue).longValue());
    } else if (type.getName().toUpperCase().endsWith("BOOLEAN")) {
        addValue(((Boolean) newValue).booleanValue());
    }
    //  begin AXAL3.7.07
    else if (type.getName().toUpperCase().endsWith("INT")) {
    addValue(((Integer) newValue).intValue());
    }
    // end AXAL3.7.07
}
/**
 * Return a Back End System id String if values are to be translated into 
 * Back End System values. If the String is empty, Back End System Translate
 * are not performed.
 * @return java.lang.String
 */
public java.lang.String getBesTranslate() {
	return besTranslate;
}
/**
 * Answer the value for content type.
 * @return int
 */
protected int getContentType() {
	return contentType;
}
/**
 * Answer the number of occurrences to be returned for the variable.
 * @return count
 */
public int getCount() {
	return count;
}
/**
 * Answer the filter value for coverages.
 * @return coverageFilter
 */
public int getCoverageFilter() {
	return coverageFilter;
}
/**
 * Answer the filter value for coverages.
 * @return covTypeCodeFilter
 */
// ACP002 New Method
public int getCovTypeCodeFilter() {
	return covTypeCodeFilter;
}
/**
 * Answer the filter value for coverages.
 * @return coverageIdFilter
 */
// ACP002 New Method
public String getCoverageIdFilter() {
	return coverageIdFilter;
}
/**
 * Answer the filter value for Medical Conditions.
 * @return medConditionFilter
 */
// ACP007 new method
public int getMedConditionFilter() {
	return medConditionFilter;
}
/**
 * Answer the filter value for CovOptions.
 * @return covOptionFilter
 */
public int getCovOptionFilter() {
	return covOptionFilter;
}
/**
 * Answer the filter value for  Participant.
 * @return lifeParticipantFilter
 */
//nba072
public int getLifeParticipantFilter() {
	return lifeParticipantFilter;
}

/**
 * Answer the data group value. 
 * @return dataGroup
 */
protected java.lang.String getDataGroup() {
	return dataGroup;
}
/**
 * Answer the value formatting value. 
 * @return formatting
 */
public int getFormatting() {
	return formatting;
}
/**
 * Answer the filter value for parties.
 * @return partyFilter
 */
public int getPartyFilter() {
	return partyFilter;
}
/**
 * Answer the qualifier value. 
 * @return qualifier
 */
public java.lang.String getQualifier() {
	return qualifier;
}
/**
 * Answer the relatedRefID value. 
 * @return java.lang.String
 */
protected java.lang.String getRelatedRefID() {
	return relatedRefID;
}
/**
 * Answer the relationRoleCode value. 
 * @return long
 */
protected long getRelationRoleCode() {
	return relationRoleCode;
}
/**
 * Answer the root variable.
 * @return rootVariable
 */
public java.lang.String getRootVariable() {
	return rootVariable;
}
/**
 * Answer the string for the first value.
 * @return value
 */
public String getStringValue() {
	return (String) value.get(0);
}
/**
 * Answer a String [] for the values.
 * @return value
 */
public String [] getStringValues() {
	String [] stringArray = new String[value.size()];
	for (int i = 0; i < value.size(); i++){
		stringArray[i] = (String) value.get(i);
	}
	return stringArray;
}
/**
 * Answer the table name. 
 * @return java.lang.String
 */
public java.lang.String getTable() {
	return table;
}
/**
 * Answer the value to be returned for a variable.
 * @return value
 */
public Vector getValue() {
	return value;
}
/**
 * Answer the value type.
 * @return int
 */
public int getValueType() {
	return valueType;
}
/**
 * Answer the variable identifier of the value to be returned.
 * @return variable
 */
public java.lang.String getVariable() {
	return variable;
}
/**
 * Convenience method to initialize all filters.
 */
public void initFilters() {
	setCoverageFilter(noFilterInt);
	setCovOptionFilter(noFilterInt);
	setLifeParticipantFilter(noFilterInt);
	setPartyFilter(noFilterInt);
	setRelatedRefID(noFilterString);
	setRelationRoleCode(noFilterLong);
}
/**
 * Return true if values are to be translated into 
 * Back End System values. 
 * @param newBesTranslate java.lang.String
 */
public boolean isBesTranslate() {
	return getBesTranslate().length() > 0;
}
/**
 * Answer the value for enabled
 * @return boolean
 */
protected boolean isEnabled() {
	return enabled;
}
/**
 * Return true if values are to be translated using the applicable table.
 * @return boolean
 */
public boolean isTableTranslations() {
	return tableTranslations;
}
/**
 * Parse the variable identifier.
 * @return true if the variable could be parsed, false otherwise.
 */
protected boolean parseVariable() {
	int nodeCount = 1;
	int startIndex = 0;
	int lastIndex = getVariable().length() - 1;
	if (lastIndex <= 0) {  //NBA104
		return false;
	}
	while (startIndex < lastIndex) {
		int endIndex = getVariable().indexOf("_", startIndex);
		if (endIndex < 0) {
			endIndex = ++lastIndex;
		}
		String nodeValue = getVariable().substring(startIndex, endIndex);
		boolean mapped = false;
		while (!mapped && nodeCount < 5) {
			mapped = saveNodeValue(nodeCount, nodeValue.toUpperCase());
			nodeCount++;
		}
		if (!mapped && nodeCount > 4) {
			return false;
		}
		startIndex = ++endIndex;
	}
	return true;
}
/**
 * Determine if the next nodeValue is applicable to the current node.
 * If it it, save the value.
 * @return true if the variable is applicable, false otherwise.
 */
protected boolean saveNodeValue(int nodeCount, String nodeValue) {
	switch (nodeCount) {
		case 1 :
			setRootVariable(nodeValue);
			setQualifier("");
			setDataGroup("");
			setFormatting(FIELD_TYPE_TEXT);
			return true;
		
		case 2 : //APSL4872 :: Start
			if (qualifier2Types.contains(nodeValue)) { 
				setQualifier2(nodeValue);
				return true;
			} 
			return false;
			//APSL4872 :: End
		case 3 :
			if (qualifierTypes.contains(nodeValue)) {
				setQualifier(nodeValue);
				return true;
			} 
			return false;
		case 4 :
			if (dataGroupTypes.contains(nodeValue)) {
				setDataGroup(nodeValue);
				return true;
				//Begin P2AXAL016
			} else if(covOptionFilterTypes.contains(nodeValue)){
		     	if (nodeValue.equals(ACCIDENTAL_DEATH_BENEFIT)){
					setCovOptionFilter((int)NbaOliConstants.OLI_OPTTYPE_ADB);
					return true;
				} else if (nodeValue.equals(WAIVER_OF_PREMIUM)){
					setCovOptionFilter((int)NbaOliConstants.OLI_OPTTYPE_WP);
					return true;
				} else if (nodeValue.equals(GUARANTEED_INSURABILITY_RIDER)){
					setCovOptionFilter((int)NbaOliConstants.OLI_OPTTYPE_GIR);
					return true;
				}
			} //End P2AXAL016 
			return false;
		case 5 :
			if (formattingTypes.contains(nodeValue)) {
				if (nodeValue.equals("DD")) {
					setFormatting(FIELD_TYPE_DROPDOWN);
				} else if (nodeValue.equals("EF")) {
					setFormatting(FIELD_TYPE_TEXT);
				} else if (nodeValue.equals("CB")) {
					setFormatting(FIELD_TYPE_CHECKBOX);
				} else if (nodeValue.startsWith("RB")) {
					setFormatting(FIELD_TYPE_RADIOBUTTON);
					if (nodeValue.length() > 2) {
						setPartyFilter(Integer.parseInt(nodeValue.substring(2))); //NBA027
					}
				}
				return true;
			} 
			return false;
		default :
			return false;
	}
}
/**
 * Set a Back End System id String to translate values into 
 * Back End System values. If the String is empty, Back End System Translate
 * are not performed.
 * @param newBesTranslate java.lang.String
 */
public void setBesTranslate(java.lang.String newBesTranslate) {
	besTranslate = newBesTranslate;
	tableTranslations = isBesTranslate();
}
/**
 * Set the content type.
 * @param newContentType int
 */
protected void setContentType(int newContentType) {
	contentType = newContentType;
}
/**
 * Set the number of occurrences to be returned for the variable.
 * @param newCount
 */
public void setCount(int newCount) {
	count = newCount;
}
/**
 * Set the filter value for coverages.
 * @param newCoverageFilter
 */
public void setCoverageFilter(int newCoverageFilter) {
	coverageFilter = newCoverageFilter;
}
/**
 * Set the filter value for coverages.
 * @param newCovTypeCodeFilter
 */
// ACP002 New Method
public void setCovTypeCodeFilter(int newCovTypeCodeFilter) {
	covTypeCodeFilter = newCovTypeCodeFilter;
}
/**
 * Set the filter value for coverages.
 * @param id
 */
// ACP002 New Method
public void setCoverageIdFilter(String id) {
	coverageIdFilter = id;
}
/**
 * Set the filter value for Medical Conditions.
 * @param index
 */
// ACP007 new method
public void setMedConditionFilter(int index) {
	medConditionFilter = index;
}
/**
 * Set the filter value for CovOptions.
 * @param newCovOptionFiler
 */
public void setCovOptionFilter(int newCovOptionFilter) {
	covOptionFilter = newCovOptionFilter;
}
/**
 * Set the filter value for lifeParticipant.
 * @param newLifeParticipantFilter
 */
//nba072
public void setLifeParticipantFilter(int newLifeParticipantFilter) {
	lifeParticipantFilter = newLifeParticipantFilter;
}
/**
 * Set the data group value. 
 * @param newDataGroup
 */
protected void setDataGroup(java.lang.String newDataGroup) {
	dataGroup = newDataGroup;
}
/**
 * Set the enabled value. Enabled is only applicable to HTML.
 * @param newEnabled
 */
protected void setEnabled(boolean newEnabled) {
	enabled = newEnabled;
}
/**
 * Set the value formatting value. 
 * @param newFormatting
 */
protected void setFormatting(int newFormatting) {
	formatting = newFormatting;
}
/**
 * Set the filter value for parties.
 * @param newPartyFilter
 */
public void setPartyFilter(int newPartyFilter) {
	partyFilter = newPartyFilter;
}
/**
 * Set the party filter based on a relationRoleCode and relatedRefID.
 * @param relationRoleCode
 * @param relatedRefID
 */
public void setPartyFilter(long relationRoleCode, String relatedRefID) {
	setRelationRoleCode(relationRoleCode);
	setRelatedRefID (relatedRefID);
}
/**
 * Set the qualifier value.
 * @param newQualifier
 */
protected void setQualifier(java.lang.String newQualifier) {
	qualifier = newQualifier;
}
/**
 * Set the relatedRefID value. 
 * @param newRelatedRefID java.lang.String
 */
protected void setRelatedRefID(java.lang.String newRelatedRefID) {
	relatedRefID = newRelatedRefID;
}
/**
 * Set the relationRoleCode value. 
 * @param newRelationRoleCode long
 */
protected void setRelationRoleCode(long newRelationRoleCode) {
	relationRoleCode = newRelationRoleCode;
}
/**
 * Set the root variable.
 * @param newRootVariable
 */
protected void setRootVariable(java.lang.String newRootVariable) {
	rootVariable = newRootVariable;
}
/**
 * Set the table name.
 * @param newTable java.lang.String
 */
protected void setTable(java.lang.String newTable) {
	table = newTable;
}
/**
 * Turn on or off table translation.  The default value is off.
 * @param newTableTranslations boolean
 */
public void setTableTranslations(boolean newTableTranslations) {
	tableTranslations = newTableTranslations;
	besTranslate = "";
}
/**
 * Set a single value to be returned for a variable.
 * @param newValue
 */
public void setValue(String newValue) {
	//begin NBA061
	if (value == null) {
		setValue(new Vector());
		value.add(newValue);
		setCount(1);
	} else {
		//end NBA061
		value.set(0, newValue);
	} //NBA061
}
/**
 * Set the value to be returned for a variable.
 * @param newValue
 */
public void setValue(Vector newValue) {
	value = newValue;
}
/**
 * Set the values for a variable.
 * @param newValues
 */
public void setValues(String[] newValues) {
	setCount(newValues.length);
	setValue(new Vector());
	for (int i = 0; i < getCount(); i++) {
		value.add(newValues[i]);
	}
}
/**
 * Set the value type.
 * @param newValueType int
 */
protected void setValueType(int newValueType) {
	valueType = newValueType;
}
/**
 * Set the variable identifier of the value to be returned.
 * @param newVariable
 */
public void setVariable(java.lang.String newVariable) {
	variable = newVariable;
}


/**
 *	gets the requirementIdFilter
 *  @return requirementIdFilter
 */
//ACP001 New Method
public String getRequirementIdFilter() {
	return requirementIdFilter;
}
/**
 * sets the requirementIdFilter
 * @param requirementIdFilter
 */
// ACP001 New Method
public void setRequirementIdFilter(String requirementIdFilter) {
	this.requirementIdFilter = requirementIdFilter;
}

/**
 * gets the labTestCodeFilter
 * @return labTestCodeFilter
 */
// ACP001 New Method
public String getLabTestCodeFilter() {
	return labTestCodeFilter;
}

/**
 * sets the labTestCodeFilter
 * @param labTestCodeFilter
 */
// ACP001 New Method
public void setLabTestCodeFilter(String labTestCodeFilter) {
	this.labTestCodeFilter = labTestCodeFilter;
}

/**
 * gets the args[] array
 * @return args[]
 */
//ACP002 new method
public Object[] getArgs() {
	return args;
}

/**
 * set the args[] array
 * @param objects[]
 */
//ACP002 new method
public void setArgs(Object[] objects) {
	args = objects;
}
/**
 * Gets the element Index Filter
 * @return int
 */
//ACP005 new method
public int getElementIndexFilter() {
	return elementIndexFilter;
}

/**
 * Sets the element Index Filter
 * @param i
 */
//ACP005 new method
public void setElementIndexFilter(int i) {
	elementIndexFilter = i;
}

/**
 * Gets the Related Object Type
 * @return String
 */
//ACP005 new method
public String getRelatedObjectTypeFilter() {
	return relatedObjectTypeFilter;
}

/**
 * Sets the related Object Type
 * @param string
 */
//ACP005 new method
public void setRelatedObjectTypeFilter(String string) {
	relatedObjectTypeFilter = string;
}
	/**
	 * @return Returns the parseMultiple.
	 */
	 //New Method P2AXAL028
	public boolean isParseMultiple() {
		return parseMultiple;
	}
	/**
	 * @param parseMultiple The parseMultiple to set.
	 */
	 //New Method P2AXAL028	 
	public void setParseMultiple(boolean parseMultiple) {
		this.parseMultiple = parseMultiple;
	}
	
	/**
	 * @return the qualifier2
	 */
	//New Method APSL4872	 
	public java.lang.String getQualifier2() {
		return qualifier2;
	}
	/**
	 * @param qualifier2 the qualifier2 to set
	 */
	//New Method APSL4872	 
	public void setQualifier2(java.lang.String qualifier2) {
		this.qualifier2 = qualifier2;
	}
}
