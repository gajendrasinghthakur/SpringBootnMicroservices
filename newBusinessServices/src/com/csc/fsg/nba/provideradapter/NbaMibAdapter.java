package com.csc.fsg.nba.provideradapter;

/*
 * ************************************************************** <BR>
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
 * ************************************************************** <BR>
 */
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.business.process.NbaAutoProcessProviderProxy;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaDataException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaStatesData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.Provider;
import com.csc.fsg.nba.vo.configuration.Request;
import com.csc.fsg.nba.vo.txlife.AssociatedResponseData;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.FormInstanceExtension;
import com.csc.fsg.nba.vo.txlife.FormResponse;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Relation;

/**
 * NbaMibAdapter provides support for converting NbaTXLife requirement requests
 * into the transactions required by MIB.  In addition, it parses the results received
 * from MIB or request rejected by MIBLINK-PLUS software, updates AWD work items for 
 * those results or requests.
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td><tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture changes</td></tr>
 * <tr><td>SPR1312</td><td>Version 3</td><td>MIB Requirement is going to provider error in Order Requirement Process</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>121/1122 Migration</td></tr>
 * <tr><td>SPR1486</td><td>Version 4</td><td>MIB Codes not transmitted on clicking MIB Transmit Button</td></tr>
 * <tr><td>ACN009</td><td>Version 4</td><td>MIB 401/402 Migration</td></tr>
 * <tr><td>SPR1346</td><td>Version 5</td><td>Displaying State Drop-down list in Alphabetical order by country/ACORD State Code Change</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see NbaProviderAdapterFacade
 * @since New Business Accelerator - Version 2
 */
//ACN014 Changed to extend NbaProviderAdapter
public class NbaMibAdapter extends NbaProviderAdapter {
	// logger reference
	protected NbaLogger logger = null;
	protected NbaTableAccessor ntsAccess = null;
	protected Provider provider = null; //ACN012
	protected int dataMapNumer = 0;

	protected static final int REQUEST_KEIGHLEY_FIELD_LENGTH = 13;

	protected static final String BINARY_IND = "00";
	protected static final String TEXT_IND = "01";
	protected static final String MIB_LOB = "LOB ";
	protected static final String MIB_DELAYED_DATE = "DEDT";
	protected static final String MIB_BOP_UNKNOWN = "UNKNOW";
	protected static final String MIB_OCCUPATION_UNKNOWN = "UNKNOWN";
	protected static final String MIB_LOB_LIFE = "LD ";
	protected static final String MIB_DATE_UNKNOWN = "------";

	protected static final String MIB_NFD = "NFD";
	protected static final String MIB_HIT = "HIT";
	protected static final String MIB_TRY = "TRY";

	protected static final int CYBERLIFE_CODE = 1;
	protected static final int VANTAGE_CODE = 2;

	protected static final String MIB_RAW_RESPONSE = "MIB_RAW_RESPONSE";
	protected static final String APPL_PREFIX = "APPLICATION_PREFIX";
	protected static final String FORMAT = "FORMAT_INDICATOR";
	protected static final String RECORD_TYPE = "REC_TYPE";
	protected static final String KEIGHLEY_FIELD = "KEIGHLEY_FIELD";
	protected static final String MSG_TYPE = "MSG_TYPE";
	protected static final String COMPANY_SYMBOL = "CSY";
	protected static final String DESTINATION_CODE = "DCODE";
	protected static final String BATCH_NO = "BATCH_NO";
	protected static final String INQ_TYPE = "INQ_TYPE";
	protected static final String LAST_NAME = "SN_20";
	protected static final String FIRST_NAME = "GN_20";
	protected static final String MIDDLE_NAME = "MI_1";
	protected static final String DATE_OF_BIRTH = "DOB";
	protected static final String PLACE_OF_BIRTH = "POB";
	protected static final String TERRITORY_CODE = "TCODE";
	protected static final String POLICY_NO = "PN";
	protected static final String LINE_OF_BUSINESS = "LOB";
	protected static final String TRANSMIT_DATE = "XMIT_DATE";
	protected static final String REPLY_TYPE = "REPLY_TYPE";
	protected static final String FIELD_ID = "FIELD_ID";
	protected static final String DATA_ITEM = "DATA_ITEM";
	protected static final String INQUIRY_NO = "INQ_NO";
	protected static final String CURRENT_DATE = "CURRENT_DATE";
	protected static final String OCCUPATION = "OCCUPATION";
	protected static final String UPDATE_CODE_DSPL = "UPDATE_CODE_DSPL";
	protected static final String UPDATE_CODE = "UPDATE_CODE";
	protected static final String DELAYED_DATE = "DELAYED_DATE";
	protected static final String ALT_LAST_NAME = "ALT_SN_20";
	protected static final String ALT_FIRST_NAME = "ALT_GN_20";
	protected static final String ALT_MIDDLE_NAME = "ALT_MI_1";
	protected static final String ALT_DATE_OF_BIRTH = "ALT_DOB";
	protected static final String ALT_PLACE_OF_BIRTH = "ALT_POB";
	protected static final String TOTAL_RECORD = "TOTAL_RECORD";
	protected static final String MISCELLANEOUS_NOTES = "MISC_NOTES";
	
	protected String mibVendorCode = "12";//ACN009

	//for outside use
	public static final String ERROR_MSG = "ERROR_MSG";
	//ACN014 Deleted Lines
	protected static final HashMap mibMonth = new HashMap();
	static {
		mibMonth.put("1", "JA");
		mibMonth.put("2", "FB");
		mibMonth.put("3", "MR");
		mibMonth.put("4", "AP");
		mibMonth.put("5", "MY");
		mibMonth.put("6", "JU");
		mibMonth.put("7", "JL");
		mibMonth.put("8", "AG");
		mibMonth.put("9", "SP");
		mibMonth.put("10", "OC");
		mibMonth.put("11", "NV");
		mibMonth.put("12", "DC");
	}

/**
 * Default NbaMibAdapter constructor.
 */
public NbaMibAdapter() throws NbaBaseException {
	ntsAccess = new NbaTableAccessor();
}
/**
 * This method returns a string filled with char passed as parameter. 
 * @return the filler pad string
 * @param chr filler char
 * @param length string length
 */
protected String addFiller(char chr, int length) {
	StringBuffer padd = new StringBuffer();
	for (int i = 0; i < length; i++) {
		padd.append(chr);
	}
	return padd.toString();
}
/**
 * Creates FormInstance object and adds to the OLifE object. * 
 * @param olife the OLifE object
 * @param map the mapping hashmap
 */
protected void addFormInstance(OLifE olife, Map map) throws NbaBaseException {
	OLifEExtension olifeExt = null;
	FormInstance form = null;
	FormInstanceExtension formExt = null;
	//NBA093 code deleted
	FormResponse formResponse = null;
	AssociatedResponseData data = null;
	//ACN009 code deleted
	int count = 1;
	int countAssociateId = 0;
	NbaOLifEId nbaOLifEId = new NbaOLifEId(olife);	//NBA093

	int loop = Integer.parseInt(map.get(TOTAL_RECORD).toString());
	for (int i = 1; i <= loop; i++) {
		form = new FormInstance();
		nbaOLifEId.setId(form);	//NBA093
		form.setFormInstanceKey(map.get(INQUIRY_NO + i).toString());
		form.setFormName(map.get(RECORD_TYPE + i).toString());
		form.setCompletionDate(convertMIBFormatToDate(map.get(TRANSMIT_DATE + i).toString()));
		
		olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_FORMINSTANCE);	//NBA093
		formExt = olifeExt.getFormInstanceExtension();	//NBA093
		formExt.setInquiryType(map.get(INQ_TYPE + i).toString());
		//ACN009 CODE DELETED
		formExt.setBatchNumber(map.get(BATCH_NO + i).toString());	//NBA093
		form.addOLifEExtension(olifeExt);
		//NBA093 code deleted
		formResponse = new FormResponse();
		formResponse.setId("FormResponse_" + i);
		form.addFormResponse(formResponse);
		count = 1;

		while (map.containsKey(FIELD_ID + i + count)) {
			data = new AssociatedResponseData();
			data.setId("AssociatedResponseData_" + (countAssociateId + count));
			data.setAssociatedResponseDataKey(map.get(FIELD_ID + i + count).toString());

			//ACN009 CODE DELETED
			//NBA093 code deleted
			//ACN009 CODE DELETED

			formResponse.addAssociatedResponseData(data);
			count = count + 1; //don't forget else system will hang up
		}
		olife.addFormInstance(form);
		countAssociateId = countAssociateId + count;
	}
}
/**
 * Creates Holding object and adds to the OLifE object. 
 * @param olife the OLifE object
 * @param map the mapping hashmap
 */
protected void addHolding(OLifE olife, Map map) throws NbaBaseException {

	Holding holding = new Holding();
	holding.setId("Holding_1");
	holding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);
	Attachment attach = new Attachment();
	attach.setAttachmentType(1); //should be 1 'Document'
	AttachmentData attachData = new AttachmentData();
	attachData.setPCDATA(map.get(MIB_RAW_RESPONSE).toString());
	attach.setAttachmentData(attachData);
	holding.addAttachment(attach);

	Policy policy = new Policy();
	policy.setPolicyStatus(NbaOliConstants.OLI_POLSTAT_PENDING);
	if (map.get(POLICY_NO) != null) {
		policy.setPolNumber(map.get(POLICY_NO).toString());
	}
	holding.setPolicy(policy);

	olife.addHolding(holding);
}
/**
 * Creates Perosn or Organization party object and adds to the OLifE. 
 * @param olife the OLifE object
 * @param map the mapping hashmap
 */
protected void addParty(OLifE olife, Map map) throws NbaBaseException {
	// SPR3290 code deleted
	PersonOrOrganization perOrg = null;
	Person person = null;
	// NBA093 deleted line
	Party perParty = null;

	int loop = Integer.parseInt(map.get(TOTAL_RECORD).toString());
	for (int i = 1; i <= loop; i++) {
		perParty = new Party();
		perParty.setId("Party_" + i);
		perOrg = new PersonOrOrganization();
		person = new Person();
		if (map.get(FIRST_NAME + i) != null && map.get(FIRST_NAME + i).toString().trim().length() > 0) {
			person.setFirstName(map.get(FIRST_NAME + i).toString().trim());
		}
		if (map.get(LAST_NAME + i) != null && map.get(LAST_NAME + i).toString().trim().length() > 0) {
			person.setLastName(map.get(LAST_NAME + i).toString().trim());
		}
		if (map.get(MIDDLE_NAME + i) != null && map.get(MIDDLE_NAME + i).toString().trim().length() > 0) {
			person.setMiddleName(map.get(MIDDLE_NAME + i).toString().trim());
		}
		if (map.get(DATE_OF_BIRTH + i) != null && (!map.get(DATE_OF_BIRTH + i).toString().equals(MIB_DATE_UNKNOWN))) {
			person.setBirthDate(convertMIBFormatToDate(map.get(DATE_OF_BIRTH + i).toString()));
		}
		perOrg.setPerson(person);
		perParty.setPersonOrOrganization(perOrg);

		// NBA093 deleted 2 lines
		if (map.get(PLACE_OF_BIRTH + i) != null && map.get(PLACE_OF_BIRTH + i).toString().trim().length() > 0) {
			person.setBirthJurisdictionTC(map.get(PLACE_OF_BIRTH + i).toString());  //NBA093
		}
		// NBA093 deleted 2 lines
		olife.addParty(perParty);
	}

	//make sure that requested party is the last party object added to the olife
	if (loop > 0) {
		Party orgParty = new Party();
		orgParty.setId("Party_" + (loop + 1));
		perOrg = new PersonOrOrganization();
		Organization org = new Organization();
		if (map.get(COMPANY_SYMBOL) != null && map.get(COMPANY_SYMBOL).toString().trim().length() > 0) {
			org.setAbbrName(map.get(COMPANY_SYMBOL).toString());
		} else {
			org.setAbbrName("");
		}

		perOrg.setOrganization(org);
		orgParty.setPersonOrOrganization(perOrg);

		//ACN009 CODE DELETED
		olife.addParty(orgParty);
	}
}
/**
 * Create Relation objects and add into the OLifE object.
 * 
 * @param olife the OLifE object
 * @param map the mapping hashmap
 */
protected void addRelations(OLifE olife, Map map) throws NbaBaseException {

	int loop = Integer.parseInt(map.get(TOTAL_RECORD).toString());
	long relationRoleCode = 0;
	String userDefine = map.get(KEIGHLEY_FIELD).toString(); //SPR1312
	long personRoleCode = Long.parseLong(userDefine.substring(0, userDefine.length() - 11)); //SPR1312

	for (int i = 0; i < loop; i++) {
		olife.addRelation(
			createRelation(
				olife.getRelationCount(),
				NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(),
				olife.getPartyAt(i).getId(),
				NbaOliConstants.OLI_HOLDING,
				NbaOliConstants.OLI_PARTY,
				personRoleCode)); //NBA044

		olife.addRelation(
			createRelation(
				olife.getRelationCount(),
				NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(),
				olife.getFormInstanceAt(i).getId(),
				NbaOliConstants.OLI_HOLDING,
				NbaOliConstants.OLI_FORMINSTANCE,
				NbaOliConstants.OLI_REL_ORIGINATOR)); //NBA044

		if (map.get(REPLY_TYPE + (i + 1)).toString().equals(MIB_HIT)) {
			relationRoleCode = NbaOliConstants.OLI_REL_HIT;
		} else if (map.get(REPLY_TYPE + (i + 1)).toString().equals(MIB_TRY)) {
			relationRoleCode = NbaOliConstants.OLI_REL_TRY;
		} else if (map.get(REPLY_TYPE + (i + 1)).toString().equals(MIB_NFD)) {
			relationRoleCode = NbaOliConstants.OLI_REL_NFD;
		} else {
			throw new NbaBaseException("Invalid Reply Type");
		}
		olife.addRelation(
			createRelation(
				olife.getRelationCount(),
				olife.getFormInstanceAt(i).getId(),
				olife.getPartyAt(i).getId(),
				NbaOliConstants.OLI_FORMINSTANCE,
				NbaOliConstants.OLI_PARTY,
				relationRoleCode));
	}
	olife.addRelation(
		createRelation(
			olife.getRelationCount(),
			NbaTXLife.getPrimaryHoldingFromOLifE(olife).getId(),
			olife.getPartyAt(olife.getPartyCount() - 1).getId(),
			NbaOliConstants.OLI_HOLDING,
			NbaOliConstants.OLI_PARTY,
			NbaOliConstants.OLI_REL_REQUESTEDBY)); //NBA044

}
/**
 * Convert 2 byte ascii value into equivalent integer value.
 * It uses following formula to convert into int value.
 * intvalue = secondbyte * 256 + firstbye
 * @return converted int value
 * @param str the two bytes ascii in the form of string.
 */
protected int convertByteToInt(String str) {
	int first = (int) str.charAt(0);
	int second = (int) str.charAt(1);
	return second * 256 + first;
}
/**
 * Converts date into string format that is understood by MIB 
 * @return MIB date formated string
 * @param date the standatrd date object
 */
protected String convertDateToMIBFormat(Date date) {
	if (date == null) {
		return MIB_DATE_UNKNOWN;
	}
	GregorianCalendar calendar = new GregorianCalendar();
	calendar.setTime(date);
	StringBuffer mibDate = new StringBuffer();

	if (calendar.get(GregorianCalendar.DAY_OF_MONTH) > 0) {
		mibDate.append(fxdRgtAlnFormat(Integer.toString(calendar.get(GregorianCalendar.DAY_OF_MONTH)), 2, '0'));
	} else {
		mibDate.append("--");
	}

	if (mibMonth.get(Integer.toString(calendar.get(GregorianCalendar.MONTH) + 1)) != null) {
		mibDate.append((String) mibMonth.get(Integer.toString(calendar.get(GregorianCalendar.MONTH) + 1)));
	} else {
		mibDate.append("--");
	}
	mibDate.append((Integer.toString(calendar.get(GregorianCalendar.YEAR))).substring(2));
	return mibDate.toString();
}
/**
 * Convert the integer value in equivalent 2 byte ascii value
 * It uses following formula to convert into byte array.
 * firstbyte = intval%256 secondbyte= intval/256 
 * @return acsii bytes in the form of string
 * @param value the int value
 */
protected String convertIntToByte(int value) {
	char[] str = {(char) (value % 256), (char) (value / 256)};
	return new String(str);
}
/**
 * Convert MIB specific date format to standard date object. 
 * @return the standard date object
 * @param mibDate MIB formated date
 */
protected Date convertMIBFormatToDate(String mibDate) {
	if (mibDate.equals(MIB_DATE_UNKNOWN)) {
		return null;
	}
	int year = Integer.parseInt(mibDate.substring(4, 6));
	int day = Integer.parseInt(mibDate.substring(0, 2));
	int month = 0;

	Iterator iterate = mibMonth.keySet().iterator();
	while (iterate.hasNext()) {
		String str = (String) iterate.next();
		if (mibDate.substring(2, 4).equals(mibMonth.get(str))) {
			month = Integer.parseInt(str);
			break;
		}
	}
	StringBuffer buf = new StringBuffer();
	buf.append(month);
	buf.append("/");
	buf.append(day);
	buf.append("/");
	if (year < 10) {
		buf.append("0");
	}
	buf.append(year);
	Date date = null;
	try {
		date = new java.text.SimpleDateFormat("M/d/y").parse(buf.toString());
	} catch (Exception e) {
		new NbaBaseException("Error parsing MIB date");
	}
	return date;
	//GregorianCalendar calendar = new GregorianCalendar(year, month - 1, day);
	//return calendar.getTime();
}
/**
 * This method converts the XML Requirement transactions into a format
 * that is understandable by the MIB.
 * @param aList array list of requirement transactions
 * @return a MIB ready message.
 * @exception NbaBaseException thrown if an error occurs.
 */
public Map convertXmlToProviderFormat(List aList) throws NbaBaseException {
	Map result = new HashMap();

	if (aList.size() == 1) {
		//ACN009 begin
		NbaTXLife txlife = null;
		try{
			txlife = new NbaTXLife((String) aList.get(0));
		}catch(Exception exp){
			getLogger().logError(exp.getMessage());
		}
		//ACN009 end
		if (getLogger().isDebugEnabled()) { // NBA027
			getLogger().logDebug("Request for transform : " + txlife.toXmlString());
		} // NBA027
		//ACN009 code deleted
		if (txlife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getTransType() == NbaOliConstants.TC_TYPE_MIBUPDATE) {//ACN009
			try {
				txlife = createUpdateMapping(txlife);//ACN009
				//ACN009 code deleted 
			} catch (NbaDataException e) {
				result.put(txlife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getTransRefGUID(), e.getMessage());  //NBA093
			}
		} // ACN009 code deleted
		result.put(TRANSACTION, txlife);//ACN009
		return result;

	} else if (aList.size() == 0) {
		throw new NbaBaseException("XMLife is required");
	} else {
		throw new NbaBaseException("More than one XMLife messages");
	}

}
/**
 * Creates a new HashMap and maps all fields for batch processing.
 * @param response the provider response message
 * @return the mapped response hashmap
 */
protected Map createBatchResponseMapping(String response) throws NbaBaseException {
	HashMap map = new HashMap();
	boolean textFormat = false;
	map.put(FORMAT, response.substring(98, 100));
	map.put(ERROR_MSG, response.substring(15, 75).trim());
	if (map.get(FORMAT).toString().equals(TEXT_IND)) {
		textFormat = true;
	} else if (map.get(FORMAT).toString().equals(BINARY_IND)) {
		textFormat = false;
	} else {
		throw new NbaBaseException("Invalid Format Indicator");
	}
	int read = 100;
	int length = 0;
	String subStr = null;
	if (textFormat) {
		length = Integer.parseInt(response.substring(read, 8 + read).trim());
		subStr = response.substring(read + 8, read + length);
	} else {
		length = convertByteToInt(response.substring(read, 2 + read));
		subStr = response.substring(read + 2, read + length);
	}

	map.put(KEIGHLEY_FIELD, subStr.substring(8, 28).trim());
	map.put(POLICY_NO, subStr.substring(88, 108).trim());
	return map;
}
/**
 * Creates a hashmap and maps all ields for rejected request.
 * @param response the provider request message
 * @return the mapped request hashmap
 */
protected Map createRejectedRequestMapping(String response) throws NbaBaseException {
	HashMap map = new HashMap();
	boolean textFormat = false;
	map.put(FORMAT, response.substring(98, 100));
	map.put(ERROR_MSG, response.substring(15, 75).trim());
	if (map.get(FORMAT).toString().equals(TEXT_IND)) {
		textFormat = true;
	} else if (map.get(FORMAT).toString().equals(BINARY_IND)) {
		textFormat = false;
	} else {
		throw new NbaBaseException("Invalid Format Indicator");
	}
	int read = 100;
	int length = 0;
	String subStr = null;
	if (textFormat) {
		length = Integer.parseInt(response.substring(read, 8 + read).trim());
		subStr = response.substring(read + 8, read + length);
	} else {
		length = convertByteToInt(response.substring(read, 2 + read));
		subStr = response.substring(read + 2, read + length);
	}

	map.put(KEIGHLEY_FIELD, subStr.substring(8, 28).trim());
	map.put(POLICY_NO, subStr.substring(128, 148).trim());
	return map;
}
/**
 * Creates the relation object. 
 * @return  the created relation object
 * @param relCount the count of relation on the olife.
 * @param orgObjId the originating object id
 * @param relObjId the relation object id
 * @param orgType the originating object type
 * @param relType the relation object type
 * @param roleCode the relation role code
 */
protected Relation createRelation(int relCount, String orgObjId, String relObjId, long orgType, long relType, long roleCode)
	throws NbaBaseException {
	Relation rel = new Relation();
	rel.setId("Relation_" + (relCount + 1));
	rel.setOriginatingObjectID(orgObjId);
	rel.setRelatedObjectID(relObjId);
	rel.setOriginatingObjectType(orgType);
	rel.setRelatedObjectType(relType);
	rel.setRelationRoleCode(roleCode);

	return rel;
}
/**
 * Creates a hashmap and maps all the data required for request transaction.
 * @param life a NbaTXLife object
 * @return the mapped request data hashmap
 */
protected Map createRequestMapping(NbaTXLife life) throws NbaBaseException {
	HashMap map = new HashMap();
	OLifE olife = life.getOLifE();
	Party party = olife.getPartyAt(0);
	Person person = party.getPersonOrOrganization().getPerson();
	FormInstance form = olife.getFormInstanceAt(0);

	String tempStr = null; // use to store string temporarily

	Request providerRequest = NbaConfiguration.getInstance().getProviderRequest("MIB",Long.toString(NbaOliConstants.TC_TYPE_MIBINQUIRY)); //ACN012
	map.put(APPL_PREFIX, providerRequest.getApplPrefix());
	map.put(FORMAT, providerRequest.getFormat());

	tempStr = form.getAttachmentAt(0).getAttachmentData().getPCDATA();
	map.put(KEIGHLEY_FIELD, tempStr); // user define field contains person role code,seq and ssn

	//ACN009 CODE DELETED

	map.put(COMPANY_SYMBOL, providerRequest.getCompany());
	map.put(DESTINATION_CODE, providerRequest.getDestination());
	map.put(BATCH_NO, ""); //nba does not send batch no but change if required

	tempStr = Long.toString(form.getOLifEExtensionAt(0).getFormInstanceExtension().getInquiryType());
	map.put(INQ_TYPE, translateOlifeValue(NbaTableConstants.OLIEXT_LU_MIBINQTYPE, tempStr));

	tempStr = person.getLastName();
	if (tempStr != null && tempStr.trim().length() > 0) {
		tempStr = tempStr.trim() + ","; //comma or a space required after last name
	}
	map.put(LAST_NAME, tempStr);
	map.put(FIRST_NAME, person.getFirstName());
	map.put(MIDDLE_NAME, person.getMiddleName());

	if (person.hasBirthDate()) {
		Date date = person.getBirthDate();
		map.put(DATE_OF_BIRTH, convertDateToMIBFormat(date));
	} else {
		map.put(DATE_OF_BIRTH, null);
	}

	if (person.hasBirthJurisdictionTC()) {  //NBA093
		tempStr = Long.toString(person.getBirthJurisdictionTC());  //NBA093
		map.put(PLACE_OF_BIRTH, translateMIBState(tempStr));
	} else {
		map.put(PLACE_OF_BIRTH, MIB_BOP_UNKNOWN);
	}

	//ACN009 CODE DELETED

	StringBuffer buf = new StringBuffer();
	if (olife.getSourceInfo().getFileControlID().equals(NbaConstants.SYST_CYBERLIFE)) {
		buf.append(CYBERLIFE_CODE);
		buf.append(fxdRgtAlnFormat(NbaTXLife.getPrimaryHoldingFromOLifE(olife).getPolicy().getCarrierCode(), 2, '0')); //NBA044
	} else if (olife.getSourceInfo().getFileControlID().equals(NbaConstants.SYST_VANTAGE)) {
		buf.append(VANTAGE_CODE);
		buf.append(fxdRgtAlnFormat(NbaTXLife.getPrimaryHoldingFromOLifE(olife).getPolicy().getCarrierCode(), 3, '0')); //NBA044
	} else {
		throw new NbaBaseException("Invalid Backend System");
	}
	buf.append(life.getNbaHolding().getPolicyNumber());
	map.put(POLICY_NO, buf.toString());

	
	//tempStr = Long.toString(NbaTXLife.getPrimaryHoldingFromOLifE(olife).getPolicy().getLineOfBusiness()); //NBA044
	//map.put(LINE_OF_BUSINESS, translateOlifeValue("OLI_LU_LINEBUS", tempStr));
	map.put(LINE_OF_BUSINESS, MIB_LOB_LIFE);

	return map;
}
/**
 * Creates a hashmap and maps all the response fields.
 * @param response the provider response message
 * @return the mapped response hashmap
 */
protected Map createResponseMapping(String response) throws NbaBaseException {
	HashMap map = new HashMap();
	boolean textFormat = false;
	map.put(MIB_RAW_RESPONSE, new String(response));
	map.put(APPL_PREFIX, response.substring(0, 3));
	map.put(FORMAT, response.substring(98, 100));

	int loop = 0; // no of record

	if (map.get(FORMAT).toString().equals(TEXT_IND)) {
		textFormat = true;
	} else if (map.get(FORMAT).toString().equals(BINARY_IND)) {
		textFormat = false;
	} else {
		throw new NbaBaseException("Invalid Format Indicator");
	}
	String responseDelimiter = ((String) map.get(APPL_PREFIX)) + addFiller(' ', 12);
	NbaStringTokenizer ResponseTokens = new NbaStringTokenizer(response, responseDelimiter);
	//response from multiple databases

	if (ResponseTokens.hasMoreTokens()) { // first taken is null
		ResponseTokens.nextToken();
	}

	while (ResponseTokens.hasMoreTokens()) {
		int length = 0; // record length
		int read = 0; // total bytes read
		String subStr = null; // one record string

		String recordStr = new String(ResponseTokens.nextToken().substring(100 - responseDelimiter.length())); // all records 
		// while record exists
		while (recordStr.length() > read) {

			//get length of next record
			if (textFormat) {
				length = Integer.parseInt(recordStr.substring(read, 8 + read).trim());
				subStr = recordStr.substring(read + 8, read + length);
			} else {
				length = convertByteToInt(recordStr.substring(read, 2 + read));
				subStr = recordStr.substring(read + 2, read + length);
			}
			read = read + length;
			loop++;

			map.put(RECORD_TYPE + loop, subStr.substring(0, 8).trim());
			map.put(KEIGHLEY_FIELD, subStr.substring(8, 28).trim()); // without loop perfix
			map.put(MSG_TYPE + loop, translateMIBValue(NbaTableConstants.OLIEXT_LU_MIBMESSAGETYPE, subStr.substring(28, 30).trim()));
			map.put(COMPANY_SYMBOL, subStr.substring(32, 35).trim()); // without loop perfix
			map.put(DESTINATION_CODE, subStr.substring(37, 42).trim()); // without loop perfix
			map.put(BATCH_NO + loop, subStr.substring(44, 47).trim());
			map.put(TRANSMIT_DATE + loop, subStr.substring(48, 54).trim());
			map.put(INQUIRY_NO + loop, subStr.substring(73, 76).trim());
			map.put(INQ_TYPE + loop, translateMIBValue(NbaTableConstants.OLIEXT_LU_MIBINQTYPE, subStr.substring(78, 79).trim()));
			map.put(REPLY_TYPE + loop, subStr.substring(81, 84).trim());
			map.put(POLICY_NO, subStr.substring(88, 108).trim()); // without loop perfix
			map.put(LAST_NAME + loop, subStr.substring(108, 133).trim());
			map.put(FIRST_NAME + loop, subStr.substring(133, 158).trim());
			map.put(MIDDLE_NAME + loop, subStr.substring(158, 183).trim());
			map.put(DATE_OF_BIRTH + loop, subStr.substring(187, 193).trim());
			if (!subStr.substring(194, 200).equals(MIB_BOP_UNKNOWN)) {
				map.put(PLACE_OF_BIRTH + loop, translateOlifeState(subStr.substring(194, 200).trim()));
			}

			//calculate variable record segment length
			int dataLength = subStr.length() - 203; //203 is the total length of fixed record segment.
			if (!map.get(REPLY_TYPE + loop).toString().equals(MIB_NFD)) {
				for (int i = 0; i * 25 < dataLength; i++) {
					map.put(FIELD_ID + loop + (i + 1), subStr.substring(203 + i * 25, 207 + i * 25).trim());
					map.put(DATA_ITEM + loop + (i + 1), subStr.substring(208 + i * 25, 228 + i * 25).trim());
				}
			}

		}
	}
	map.put(TOTAL_RECORD, Integer.toString(loop));
	return map;
}
/**
 * Creates a hashmap and maps all the data required for update transaction.
 * @param life a NbaTXLife object
 * @return life a NbaTXLife object
 */
protected NbaTXLife createUpdateMapping(NbaTXLife life) throws NbaBaseException { //ACN009 changed return type to NbaTXLife
	//ACN009 Code deleted
	OLifE olife = life.getOLifE();
	//ACN009 code deleted
	//Begin ACN009
	FormInstance formInstance = olife.getFormInstanceAt(0);
	List olifeExtList = formInstance.getOLifEExtension();
	OLifEExtension olifeExt = null;
	for (int i = 0; i < olifeExtList.size(); i++){
		olifeExt = (OLifEExtension)olifeExtList.get(i);
		olifeExt.setVendorCode(mibVendorCode);
		if (olifeExt.getExtensionCode().equals("MIBSameAs")){
			olifeExt.addAnyContent(olifeExt.getFormInstanceExtension().getPartyAt(0));
		}else if (olifeExt.getExtensionCode().equals("MIBNotSameAs")){
			olifeExt.setPCDATA("<"+FormInstanceExtension.$SUBMIT_DATE+">"+NbaUtils.getStringFromDate(olifeExt.getFormInstanceExtension().getSubmitDate())+"</"+FormInstanceExtension.$SUBMIT_DATE+">");
		}
	}
	List partyList = olife.getParty();
	Party party = null;
	for (int i = 0; i < partyList.size(); i++){
		if (((Party)partyList.get(i)).getKeyedValueCount() > 0){
			party = (Party)partyList.get(i);
			for (int j = 0; j < party.getKeyedValueCount(); j++){
				party.getKeyedValueAt(j).setVendorCode(mibVendorCode);
			}
		}
	}
	if (olife.getFormInstanceAt(0).getKeyedValueCount() > 0){
		for (int j = 0; j < olife.getFormInstanceAt(0).getKeyedValueCount(); j++){
			olife.getFormInstanceAt(0).getKeyedValueAt(j).setVendorCode(mibVendorCode);
		}
	}
	return life;
	//End ACN009

//ACN009 code deleted
}
/**
 * This method creates the fixed length MIB request. 
 * @return the provider ready message
 * @param requestMap the request hashmap with all mapping
 */
protected String formatRequest(Map requestMap) throws NbaBaseException {
	StringBuffer request = new StringBuffer();
	StringBuffer cntrlData = new StringBuffer();
	StringBuffer rcrdData = new StringBuffer();

	//***** Control Segment ****** Start
	cntrlData.append(fxdLftAlnFormat(requestMap.get(APPL_PREFIX), 3, ' '));
	cntrlData.append(addFiller(' ', 2)); //function code not supported
	cntrlData.append(addFiller(' ', 2)); //return code not supported
	cntrlData.append(addFiller(' ', 8)); //message id not supported
	cntrlData.append(addFiller(' ', 60)); //messgae text not supported
	cntrlData.append(addFiller(' ', 23)); //filler
	cntrlData.append(fxdRgtAlnFormat(requestMap.get(FORMAT), 2, '0'));
	//***** Control Segment ****** End

	//***** Record Segment ****** Start
	//Length will be added at the end of process
	rcrdData.append(addFiller(' ', 8));
	rcrdData.append(fxdLftAlnFormat(requestMap.get(KEIGHLEY_FIELD), 20, ' '));
	rcrdData.append(fxdLftAlnFormat(requestMap.get(MSG_TYPE), 2, ' '));
	rcrdData.append(addFiller(' ', 2));
	rcrdData.append(fxdLftAlnFormat(requestMap.get(COMPANY_SYMBOL), 3, ' '));
	rcrdData.append(addFiller(' ', 2));
	rcrdData.append(fxdLftAlnFormat(requestMap.get(DESTINATION_CODE), 5, ' '));
	rcrdData.append(addFiller(' ', 2));
	rcrdData.append(fxdLftAlnFormat(requestMap.get(BATCH_NO), 3, ' '));
	rcrdData.append(addFiller(' ', 21));
	rcrdData.append(fxdLftAlnFormat(requestMap.get(INQ_TYPE), 1, ' '));
	rcrdData.append(addFiller(' ', 1));
	rcrdData.append(fxdLftAlnFormat(requestMap.get(LAST_NAME), 20, ' '));
	rcrdData.append(fxdLftAlnFormat(requestMap.get(FIRST_NAME), 20, ' '));
	rcrdData.append(fxdLftAlnFormat(requestMap.get(MIDDLE_NAME), 1, ' '));
	rcrdData.append(addFiller(' ', 1));
	rcrdData.append(fxdLftAlnFormat(requestMap.get(DATE_OF_BIRTH), 6, ' '));
	rcrdData.append(addFiller(' ', 1));
	rcrdData.append(fxdLftAlnFormat(requestMap.get(PLACE_OF_BIRTH), 6, ' '));
	rcrdData.append(addFiller(' ', 1));
	rcrdData.append(fxdLftAlnFormat(requestMap.get(TERRITORY_CODE), 1, ' '));
	rcrdData.append(addFiller(' ', 1));
	rcrdData.append(fxdLftAlnFormat(requestMap.get(POLICY_NO), 20, ' '));
	rcrdData.append(MIB_LOB); //'LOB '
	rcrdData.append(addFiller('=', 1)); //filler
	rcrdData.append(fxdLftAlnFormat(requestMap.get(LINE_OF_BUSINESS), 2, ' '));
	rcrdData.append(addFiller(' ', 13));

	if (requestMap.get(FORMAT).toString().equals(TEXT_IND)) {
		int length = rcrdData.toString().length() + 8;
		rcrdData.insert(0, fxdRgtAlnFormat(Integer.toString(length), 8, '0'));
	} else if (requestMap.get(FORMAT).toString().equals(BINARY_IND)) {
		int length = rcrdData.toString().length() + 2;
		rcrdData.insert(0, convertIntToByte(length));
	} else {
		throw new NbaBaseException("");
	}

	//***** Record Segment ****** End
	request.append(cntrlData.toString().toUpperCase());
	request.append(rcrdData.toString().toUpperCase());

	if (getLogger().isDebugEnabled()) { // NBA027
		getLogger().logDebug("Transform message : " + request.toString());
	} // NBA027
	return request.toString();
}
/**
 * This method creates XMLife from the fixed length response message 
 * @return the updated XMLife 
 * @param responseMap the response hashmap with all mapping
 */
protected NbaTXLife formatResponse(Map responseMap) throws NbaBaseException {
	// SPR3290 code deleted
	NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
	nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_MIBINQUIRY); //401
	nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL); //2
	nbaTXRequest.setNbaUser(new NbaUserVO(NbaConfiguration.getInstance().getProvider("MIB").getDefaultUserID(),NbaConfiguration.getInstance().getProvider("MIB").getDefaultUserID())); //SPR1312, ACN012
	NbaTXLife txLife = new NbaTXLife(nbaTXRequest);
	OLifE olife = txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getOLifE();
	//Should be in order Holding,FormInstance,Party and Relation
	addHolding(olife, responseMap);
	addFormInstance(olife, responseMap);
	addParty(olife, responseMap);
	addRelations(olife, responseMap);

	return txLife;
}
//ACN009 code deleted
/**
 * This method retuns a fixed length string with left allign message. it will filled the
 * remaining length with filler string.
 * @return the fixed length message
 * @param msg the message
 * @param fixedLength the length of return string
 * @param fillerChr the filler char
 */
protected String fxdLftAlnFormat(Object msg, int fixedLength, char fillerChr) {
	String temp = null;
	if (msg == null) {
		temp = "";
	} else {
		temp = msg.toString().trim();
	}
	if (temp.length() > fixedLength) {
		return temp.substring(0, fixedLength);
	}
	return temp + addFiller(fillerChr, fixedLength - temp.length());
}
/**
 * This method retuns a fixed length string with right allign message. it will filled the
 * remaining length with filler char.
 * @return the fixed length message
 * @param msg the original message
 * @param fixedLength the length of return string
 * @param fillerChr the filler char
 */
protected String fxdRgtAlnFormat(Object msg, int fixedLength, char fillerChr) {
	String temp = null;
	if (msg == null) {
		temp = "";
	} else {
		temp = msg.toString().trim();
	}
	if (temp.length() > fixedLength) {
		return temp.substring(0, fixedLength);
	}
	return addFiller(fillerChr, fixedLength - temp.length()) + temp;
}
/**
 * Answers the default table map for MIB translation 
 * @return the default table map
 */
//NBA008 New Method
protected Map getDefaultTableMap() {
	Map aCase = new HashMap();
	aCase.put(NbaTableAccessConstants.C_SYSTEM_ID, "MIB");
	aCase.put(NbaTableAccessConstants.C_COMPANY_CODE, "*");
	aCase.put(NbaTableAccessConstants.C_COVERAGE_KEY, "*");
	return aCase;
}
/**
 * returns instance of NbaLogger. 
 * @return com.csc.fsg.nba.foundation.NbaLogger
 */
protected NbaLogger getLogger() {
	if (logger == null) {
		try {
			logger = NbaLogFactory.getLogger(NbaMibAdapter.class.getName());
		} catch (Exception e) {
			NbaBootLogger.log("NbaServiceLocator could not get a logger from the factory.");
			e.printStackTrace(System.out);
		}
	}
	return logger;
}
/**
 * Answers provider information in the configuration file. 
 * @return provider configuration
 */
//ACN012 CHANGED SIGNATURE
protected Provider getProvider() throws NbaBaseException {
	if (provider == null) {
		provider = NbaConfiguration.getInstance().getProvider(NbaConstants.PROVIDER_MIB);
	}
	return provider;
}
/**
 * Answers relation object that matched relation type,person code 
 * and person sequence number for a XMLife.
 * @param life the XMLife object
 * @param relType the relation object type
 * @param roleCode the person relation role code
 * @return the relation object
 */
//NBA008 New Method
protected Relation getRelation(NbaTXLife life, long relType, long roleCode) throws NbaBaseException {
	List list = life.getOLifE().getRelation();
	Relation rel = null;
	for (int i = 0; i < list.size(); i++) {
		rel = (Relation) list.get(i);
		if (rel.getRelatedObjectType() == relType && rel.getRelationRoleCode() == roleCode) {
			return rel;
		}
	}
	throw new NbaBaseException("Invalid Relation");
}
/**
 * This method parse the batch response from MIB into indiviaual response streams. 
 * @return the map with all individual response streams
 * @param the batch response string
 */
protected Map parseBatch(String response) throws NbaBaseException {
	// SPR3290 code deleted
	String applPrifix = response.substring(0, 3).trim();
	String responseDelimiter = applPrifix + addFiller(' ', 12);
	String errMsg = null;
	Map tempList = new HashMap();
	NbaStringTokenizer tokens = new NbaStringTokenizer(response, responseDelimiter);
	if (tokens.hasMoreTokens()) {
		tokens.nextToken(); //this is null value
	}
	while (tokens.hasMoreTokens()) {
		String token = tokens.nextToken();
		String parseResponse = responseDelimiter + token;
		Map mapping = null;
		if (parseResponse.substring(15, 75).trim().length() > 0) {
			errMsg = parseResponse.substring(15, 75).trim();
			mapping = createRejectedRequestMapping(parseResponse);
		} else {
			mapping = createBatchResponseMapping(parseResponse);
		}

		String key = (String) mapping.get(POLICY_NO) + (String) mapping.get(KEIGHLEY_FIELD);
		if (tempList.get(key) != null) {
			tempList.put(key, tempList.get(key) + parseResponse);
		} else {
			tempList.put(key, parseResponse);
		}
	}

	List list = new ArrayList();
	Iterator iterate = tempList.values().iterator();
	while (iterate.hasNext()) {
		list.add(iterate.next()); // SPR3290
	}
	Map map = new HashMap();
	map.put(ERROR_MSG, errMsg);
	map.put(RESPONSE_LIST, list); //ACN014
	return map;
}
/**
 * This method get the reponse text source from temporary workitem and parse the batch response 
 * from into indiviaual response streams. 
 * @return the map with all individual response streams
 * @param work the temporary requirement work item
 */
public Map parseBatchResponse(NbaDst work) throws NbaBaseException {
	List list = work.getNbaSources();
	NbaSource source = null;
	String response = null;

	// get provider response source
	for (int i = 0; i < list.size(); i++) {
		source = (NbaSource) list.get(i);
		if (source.getSource().getSourceType().equals(NbaConstants.A_ST_PROVIDER_SUPPLEMENT)) {
			break;
		} else {
			source = null;
		}
	}

	// check if response source has valid data
	if (source != null) {
		response = source.getText();
		if (response == null || response.trim().length() == 0) {
			throw new NbaBaseException("Provider response source is missing ot invalid");
		}
	} else {
		throw new NbaBaseException("Provider response source is missing ot invalid");
	}

	return parseBatch(response);
}
/**
 * This method converts the MIB response into XML transaction.It 
 * also updates required LOBs and result source from response transaction.
 * @param work the requirement work item.
 * @return an ArrayList containing the updated requirement work items.
 * @exception NbaBaseException thrown if an error occurs.
 */
//ACN014 Changed signature
public ArrayList processResponseFromProvider(NbaDst work, NbaUserVO user) throws NbaBaseException {
	List list = work.getNbaSources();
	NbaSource source = null;
	String response = null;

	// get provider response source
	for (int i = 0; i < list.size(); i++) {
		source = (NbaSource) list.get(i);
		if (source.getSource().getSourceType().equals(NbaConstants.A_ST_PROVIDER_SUPPLEMENT)) {
			break;
		} else {
			source = null;
		}
	}

	// check if response source has valid data
	if (source != null) {
		response = source.getText();
		if (response == null || response.trim().length() == 0) {
			throw new NbaBaseException("Provider response source is missing ot invalid");
		}
	} else {
		throw new NbaBaseException("Provider response source is missing ot invalid");
	}

	//process source
	if (getLogger().isDebugEnabled()) { // NBA027
		getLogger().logDebug("Response from MIB : " + response);
	} // NBA027
	if (response.substring(15, 75).trim().length() == 0) { //no error message
		Map mapping = createResponseMapping(response);
		NbaTXLife txLife = formatResponse(mapping);
		if (getLogger().isDebugEnabled()) { // NBA027
			getLogger().logDebug("XMLife translation  : " + txLife.toXmlString());
		} // NBA027
		updateWorkItemFromResponse(work, mapping, txLife);
	} else {
		Map mapping = createRejectedRequestMapping(response);
		updateWorkItemForRejectedRequest(work, mapping);
	}
	//ACN014 Begin
	ArrayList aList = new ArrayList();
	aList.add(work);
	return aList;
	//ACN014 End
}
/**
 * This method accepts the provider ready message it will transmit the message to the provider.
 * @param request a provider ready message
 * @return a response message if the process completed successfully
 * @exception NbaBaseException thrown if an error occurs.
 */
public String submitRequestToProvider(String request) throws NbaBaseException {
	return null;
}
/**
 * Find MIB State for an olife value
 * @param olifeValue Olife value to translate
 * @param table State Table data to search through
 * @return Matched State Data
 */
protected String translateMIBState(String olifeValue) throws NbaBaseException {
	
	NbaStatesData[] table = (NbaStatesData[])ntsAccess.getDisplayData(getDefaultTableMap(),NbaTableConstants.NBA_STATES);
	if (table != null) {
		if (olifeValue != null && olifeValue.length() > 0) {
			for (int i = 0; i < table.length; i++) {
				if (String.valueOf(table[i].getStateCode()).compareToIgnoreCase(olifeValue) == 0) {//SPR1346
					return table[i].getCybAlphaStateCodeTrans();
				}
			}
		}
	}
	return null;
}
/**
 * For a given MIB value in a tableName, it return the olife translation value
 * @param tableName      the table from which the data is retrieved
 * @param mibValue     the value to be translated
 * @return <code>java.lang.String</code> the translated value
 * @exception NbaDataAccessException if errors occur in <code>NbaTable.translateOlifeValue</code> method
 */
protected String translateMIBValue(String tableName, String mibValue) throws NbaBaseException {
	return ntsAccess.translateBackEndValue(getDefaultTableMap(), tableName, mibValue, NbaTableAccessConstants.MIB);
}
/**
 * Find olife State for an MIB value
 * @param mibValue MIB value to translate
 * @return Matched State Data
 */
protected String translateOlifeState(String mibValue) throws NbaBaseException {
	
	NbaStatesData[] table = (NbaStatesData[])ntsAccess.getDisplayData(getDefaultTableMap(),NbaTableConstants.NBA_STATES);
	if (table != null) {
		if (mibValue != null && mibValue.length() > 0) {
			for (int i = 0; i < table.length; i++) {
				if (table[i].getCybAlphaStateCodeTrans().compareToIgnoreCase(mibValue) == 0) {
					return String.valueOf(table[i].getStateCode());//SPR1346
				}
			}
		}
	}
	return null;
}
/**
 * For a given olifeValue in a tableName, it return the MIB translation value
 * @param tableName      the table from which the data is retrieved
 * @param olifeValue     the value to be translated
 * @return <code>java.lang.String</code> the translated value
 * @exception NbaDataAccessException if errors occur in <code>NbaTable.translateOlifeValue</code> method
 */
protected String translateOlifeValue(String tableName, String olifeValue) throws NbaBaseException {
	return ntsAccess.translateOlifeValue(getDefaultTableMap(), tableName, olifeValue, NbaTableAccessConstants.MIB);
}
/**
 * This method updates the work item for rejected request 
 * @param work the work item.
 * @param map the mapping hash map 
 */
protected void updateWorkItemForRejectedRequest(NbaDst work, Map map) throws NbaBaseException {
	NbaLob lob = work.getNbaLob();
	String userDefine = map.get(KEIGHLEY_FIELD).toString();
	//begin SPR1312
	lob.setReqPersonCode(Integer.parseInt(userDefine.substring(0, userDefine.length() - 11)));
	lob.setReqPersonSeq(Integer.parseInt(userDefine.substring(userDefine.length() - 11, userDefine.length() - 9)));
	lob.setSsnTin(userDefine.substring(userDefine.length() - 9, userDefine.length()));
	//begin SPR1312

	String policyInfo = map.get(POLICY_NO).toString();
	if (Integer.parseInt(policyInfo.substring(0, 1)) == CYBERLIFE_CODE) {
		lob.setBackendSystem(NbaConstants.SYST_CYBERLIFE);
		lob.setCompany(policyInfo.substring(1, 3));
		lob.setPolicyNumber(policyInfo.substring(3).trim());
	} else if (Integer.parseInt(policyInfo.substring(0, 1)) == VANTAGE_CODE) {
		lob.setBackendSystem(NbaConstants.SYST_VANTAGE);
		lob.setCompany(policyInfo.substring(1, 4));
		lob.setPolicyNumber(policyInfo.substring(4).trim());
	} else {
		throw new NbaBaseException("Invalid backend system");
	}

	//SPR1312
	NbaAutoProcessProviderProxy proxy = new NbaAutoProcessProviderProxy(new NbaUserVO(work.getUserID(), work.getPassword()), work, true);  //NBA208-32
	// SPR1312 BEGIN
	NbaTXLife holdingInq = null;
	try {
		holdingInq = proxy.doHoldingInquiry();
	} catch (NbaDataAccessException dae) {
		throw dae;
	}
	// SPR1312 END
	proxy.handleHostResponse(holdingInq);
	if (proxy.getResult() != null) {
		throw new NbaBaseException("Contract Error");
	}
	proxy.addComment((String) map.get(ERROR_MSG));
	work = proxy.getWork();
	//get party information (from holding inquiry)
	Relation partyRel = getRelation(holdingInq, NbaOliConstants.OLI_PARTY, lob.getReqPersonCode());
	NbaParty holdingParty = holdingInq.getParty(partyRel.getRelatedObjectID());
	if (holdingParty == null) {
		throw new NbaBaseException("Could not getparty information from holding inquiry");
	}

	lob.setLastName(holdingParty.getLastName());
	lob.setFirstName(holdingParty.getFirstName());
	lob.setMiddleInitial(holdingParty.getMiddleInitial());
	lob.setReqType(Integer.parseInt(String.valueOf(NbaOliConstants.OLI_REQCODE_MIBCHECK)));
}
/**
 * This method updates the provider result source on the workitem with generated
 * XMLife message. It also updates the workitem LOBs. 
 * @param work the work item.
 * @param map the mapping hash map
 * @param txLife the XMLife response message
 */
protected void updateWorkItemFromResponse(NbaDst work, Map map, NbaTXLife txLife) throws NbaBaseException {
	NbaLob lob = work.getNbaLob();
	String userDefine = map.get(KEIGHLEY_FIELD).toString();
	//begin SPR1312
	lob.setReqPersonCode(Integer.parseInt(userDefine.substring(0, userDefine.length() - 11)));
	lob.setReqPersonSeq(Integer.parseInt(userDefine.substring(userDefine.length() - 11, userDefine.length() - 9)));
	lob.setSsnTin(userDefine.substring(userDefine.length() - 9, userDefine.length()));
	//begin SPR1312

	String policyInfo = map.get(POLICY_NO).toString();
	if (Integer.parseInt(policyInfo.substring(0, 1)) == CYBERLIFE_CODE) {
		lob.setBackendSystem(NbaConstants.SYST_CYBERLIFE);
		lob.setCompany(policyInfo.substring(1, 3));
		lob.setPolicyNumber(policyInfo.substring(3).trim());
	} else if (Integer.parseInt(policyInfo.substring(0, 1)) == VANTAGE_CODE) {
		lob.setBackendSystem(NbaConstants.SYST_VANTAGE);
		lob.setCompany(policyInfo.substring(1, 4));
		lob.setPolicyNumber(policyInfo.substring(4).trim());
	} else {
		throw new NbaBaseException("Invalid backend system");
	}

	//SPR1312
	NbaAutoProcessProviderProxy proxy = new NbaAutoProcessProviderProxy(new NbaUserVO(work.getUserID(), work.getPassword()), work, true);  //NBA208-32
	// SPR1312 BEGIN
	NbaTXLife holdingInq = null;
	try {
		holdingInq = proxy.doHoldingInquiry();
	} catch (NbaDataAccessException dae) {
		throw dae;
	}
	//SPR1312 END
	proxy.handleHostResponse(holdingInq);
	if (proxy.getResult() != null) {
		throw new NbaBaseException("Contract Error");
	}

	//get party information (from holding inquiry)
	Relation partyRel = getRelation(holdingInq, NbaOliConstants.OLI_PARTY, lob.getReqPersonCode());
	NbaParty holdingParty = holdingInq.getParty(partyRel.getRelatedObjectID());
	if (holdingParty == null) {
		throw new NbaBaseException("Could not getparty information from holding inquiry");
	}

	lob.setLastName(holdingParty.getLastName());
	lob.setFirstName(holdingParty.getFirstName());
	lob.setMiddleInitial(holdingParty.getMiddleInitial());
	lob.setReqType(Integer.parseInt(String.valueOf(NbaOliConstants.OLI_REQCODE_MIBCHECK)));
	lob.setReqReceiptDate(new Date()); //NBA130
	lob.setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(new Date())); //QC20240
	
	List list = work.getNbaSources();
	NbaSource source = null;
	for (int i = 0; i < list.size(); i++) {
		source = (NbaSource) list.get(i);
		if (source.getSource().getSourceType().equals(NbaConstants.A_ST_PROVIDER_SUPPLEMENT)) {
			break;
		}
		throw new NbaBaseException("Provider Supplement source is missing");
	}

	source.setText(txLife.toXmlString());
	source.setUpdate();

}
/**
 * Validate the mandatory fields required to send request transaction to MIB
 * throws NbaBaseException if one or more mandatory fields are missing or invalid.
 * @param map - the mapping hashmap
 */
protected void validateRequestMapping(Map map) throws NbaBaseException {
	List errorList = new ArrayList();
	if (map.get(APPL_PREFIX) == null || map.get(APPL_PREFIX).toString().length() > 3) {
		errorList.add("\n Application Prefix");
	}
	if (map.get(FORMAT) == null || map.get(FORMAT).toString().length() > 2) {
		errorList.add("\n Format Indicator");
	}
	if (map.get(KEIGHLEY_FIELD) == null ) { //SPR1312
		errorList.add("\n User Define Field");
	}
	if (map.get(MSG_TYPE) == null || map.get(MSG_TYPE).toString().length() > 2) {
		errorList.add("\n Message Type");
	}
	if (map.get(COMPANY_SYMBOL) == null || map.get(COMPANY_SYMBOL).toString().length() > 3) {
		errorList.add("\n Company Symbol");
	}
	if (map.get(DESTINATION_CODE) == null || map.get(DESTINATION_CODE).toString().length() > 5) {
		errorList.add("\n Destination Code");
	}
	if (map.get(INQ_TYPE) == null || map.get(INQ_TYPE).toString().length() > 1) {
		errorList.add("\n Inquiry Type");
	}
	if (map.get(LAST_NAME) == null || map.get(LAST_NAME).toString().length() == 0 || map.get(LAST_NAME).toString().length() > 20) {
		errorList.add("\n Last Name");
	}
	if (map.get(FIRST_NAME) == null || map.get(FIRST_NAME).toString().length() == 0 || map.get(FIRST_NAME).toString().length() > 20) {
		errorList.add("\n First Name");
	}
	if (map.get(DATE_OF_BIRTH) == null) {
		errorList.add("\n Birth Date");
	}
	if (map.get(TERRITORY_CODE) == null || map.get(TERRITORY_CODE).toString().length() == 0 || map.get(TERRITORY_CODE).toString().length() > 1) {
		errorList.add("\n Territory Code");
	}

	if (errorList.size() > 0) {
		throw new NbaDataException(errorList);
	}
}
//ACN009 code deleted
}
