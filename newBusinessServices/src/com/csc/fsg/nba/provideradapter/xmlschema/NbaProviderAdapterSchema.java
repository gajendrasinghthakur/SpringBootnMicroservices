package com.csc.fsg.nba.provideradapter.xmlschema;

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
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.csc.fsg.nba.communication.NbaCommunicator;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.tableaccess.NbaStatesData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.tableaccess.NbaUctData;
import com.csc.fsg.nba.vo.NbaTime;

/**
 * The NbaProviderAdapterSchema class provides support to NbaProviderAdapter classes by handling
 * the creation of fixed length records using an XML file as a model.  
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td></tr>
 * <tr><td>SPR1208</td><td>Version 3</td><td>Requirement work item is sent to error queue when RQTP 147 is ordered for Annuitant</td><tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>SPR1135</td><td>Version 3</td><td>A default agent id should be provided in the configuration file to send to a provider when an agent is not included on the case</td><tr>
 * <tr><td>SPR1207</td><td>Version 3</td><td>The EMSI format does not contain the required information</td><tr>
 * <tr><td>ACN009</td><td>Version 4</td><td>ACORD 401/402 MIB Inquiry and Update Migration</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */

public class NbaProviderAdapterSchema {
	private NbaOinkRequest requester;
	private NbaTableAccessor tableAccessor = null;
	private NbaOinkDataAccess oinkData = null;
	private java.lang.StringBuffer errorMsg = new StringBuffer();
	public final static char CONSTANT = 'c';
	public final static char TRANSLATE = 't';
	public final static char FINALIZE = 'f';
	public final static char PROCESS = 'p';
	public final static char ZERO_PAD = 'z';
	public final static String OR01_FINAL_RECORD = "TOTL";
	public final static String NAILBA_TRAILER = "TRAILER";
	public final static String NAILBA_HEADER = "HEADER";
	public final static String RECORD_MISSING = "\nRequired record missing: ";
	public final static String FIELD_MISSING = "\nRequired field missing: ";
	public final static char OINK = 'o';
	private char filler = ' ';
	private NbaLogger logger = null;
	private java.lang.String provider;
/**
 * NbaProviderAdapterSchema constructor creates a new NbaTableAccessor to handle
 * translations and initializes the provider.
 * @param aProvider the provider who will be receiving the new fixed length record
 */
public NbaProviderAdapterSchema(String aProvider) {
    super();
	tableAccessor = new NbaTableAccessor();
    setProvider(aProvider);
}
/**
 * NbaProviderAdapterSchema constructor creates a new NbaTableAccessor to handle
 * translations and initializes the provider and OINK data members.
 * @param aProvider the provider who will be receiving the new fixed length record
 * @param anOink an NbaOinkDataAccess object that identifies the data to be used to 
 *               populate the record
 * @param aRequest an NbaOinkRequest object used to identify the filter for the OINK object.
 * @param fillChr filler character
 */
public NbaProviderAdapterSchema(String aProvider, NbaOinkDataAccess anOink, NbaOinkRequest aRequest, char fillChr)
    throws NbaBaseException {
    super();
    provider = aProvider;
    oinkData = anOink;
    requester = aRequest;
    tableAccessor = new NbaTableAccessor();
    filler = fillChr;
}
/**
 * This method populates a string, up to the specified length, with the character passed 
 * as a parameter.  In effect, it concatenates multiple characters to the end of the string.
 * @param chr filler character
 * @param length length of the string to be filled 
 * @return String a String padded with chr
 */
protected String addFiller(char chr, int length) {
	StringBuffer padd = new StringBuffer();
	for (int i = 0; i < length; i++) {
		padd.append(chr);
	}
	return padd.toString();
}
/**
 * This method creates the Map needed by the tableAccessor.
 * @return Map a Map that may be used by the NbaTableAccessor
 */
protected Map createTableMap() {
    HashMap aMap = new HashMap();
    aMap.put(NbaTableAccessConstants.C_COMPANY_CODE, "*");
    aMap.put(NbaTableAccessConstants.C_COVERAGE_KEY, "*");
    aMap.put(NbaTableAccessConstants.C_SYSTEM_ID, provider);
    return aMap;
}
/**
 * This method returns a fixed length string with the contents left aligned.  If the msg does
 * not completely fill the return String, the method the remaining length of the return String 
 * with fillerChr.
 * @param msg the message
 * @param fixedLength the length of return String
 * @param fillerChr the filler char
 * @return String a String filled with message and padded with fillerChr
 */
public String fixedFormat(String msg, int fixedLength, char fillerChr) {
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
 * Answers today's date in MMddyy format.
 * @return String a String representing today's date
 */
public String getDate() {
    Date date = new Date();
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMddyy");
    return dateFormat.format(date);
}
/**
 * Answers any errorMsg created by the process
 * @return java.lang.StringBuffer a StringBuffer containing the error message
 */
public java.lang.StringBuffer getErrorMsg() {
	return errorMsg;
}
/**
 * Answers the filler character for the class
 * @return char the filler char
 */
public char getFiller() {
	return filler;
}
/**
 * This method calls the <code>NbaFileServlet</code> to get the contents of the
 * XML file specified by the fileName.
 * @param fileName the name of the XML file that is to be used as the model for creating
 *               the fixed length record
 * @return String a String containing the contents of the file specified by fileName
 */
public String getFormatFile(String fileName) throws Exception {
    try {
        Serializable parms[] = { NbaConstants.S_FUNC_FILE_READ, fileName };
        NbaCommunicator proComm = new NbaCommunicator();
        ObjectInputStream in = proComm.postObjectsToServlet(parms); // Execute the servlet transaction
        Object retObj = in.readObject(); // Read and use the result value
        if (retObj instanceof NbaBaseException) {
            in.close();
            throw (NbaBaseException) retObj;
        } else if (retObj instanceof Throwable) {
            in.close();
            throw new NbaBaseException(NbaBaseException.INVOKE_SERVER, (Throwable) retObj);
        }
        in.close();
        String results = new String((byte[])retObj);
        return results;
    } catch (NbaBaseException nbe) {
        throw nbe;
    } catch (Throwable t) {
        throw new NbaBaseException(NbaBaseException.INVOKE_SERVER, t);
    }
}
/**
 * Returns instance of NbaLogger.
 * @return NbaLogger my NbaLogger
 */
protected NbaLogger getLogger() {
    if (logger == null) {
        try {
            logger = NbaLogFactory.getLogger(NbaProviderAdapterSchema.class.getName());
        } catch (Exception e) {
            NbaBootLogger.log("NbaProviderAdapterSchema could not get a logger from the factory.");
            e.printStackTrace(System.out);
        }
    }
    return logger;
}
/**
 * Answers the oinkData member
 * @return NbaOinkDataAccess the NbaOinkDataAccess member
 */
public NbaOinkDataAccess getOinkData() {
	return oinkData;
}
/**
 * Answers the provider 
 * @return java.lang.String name of the provider
 */
public java.lang.String getProvider() {
	return provider;
}
/**
 * Answers the NbaOinkRequest member
 * @return NbaOinkRequest the NbaOinkRequest member
 */
public NbaOinkRequest getRequester() {
	return requester;
}
/**
 * Answers the current time as a string in HHmmss format
 * @return String the current time
 */
public String getTime() {
	NbaTime time = new NbaTime();
	return time.format("HHmmss", time.getTimeZone());
}
/**
 * Answers the time zone as a string
 * @return String the time zone
 */
public String getTimeZone() {
    NbaTime time = new NbaTime();
 	return time.format("z", time.getTimeZone());
}
/**
 * This method parses the Field Breeze object to determine the type of data for 
 * this field.  That data is then retrieved and returned for inclusion in the fixed 
 * length record.
 * @param aField a Field Breeze object 
 * @return String a String containing the retrieved data
 */
public String processField(com.csc.fsg.nba.provideradapter.xmlschema.Field aField) throws NbaBaseException {
	try {
		String data = new String();
		if (aField.hasValueType()) {
			switch (aField.getValueType().charAt(0)) {
				case CONSTANT : // constant value
					{
						data = aField.getValue();
						break;
					}
				case OINK : // OINK data field
					{
						requester = new NbaOinkRequest(); //SPR1208
						requester.setVariable(aField.getValue());
						data = oinkData.getStringValueFor(requester);
						break;
					}
				case TRANSLATE : // translation required
					{
						requester.setVariable(aField.getValue());
						NbaTableData value =
							(NbaTableData) tableAccessor.getDataForOlifeValue(
								createTableMap(),
								aField.getTable(),
								oinkData.getStringValueFor(requester)); //SPR1207
						if (value == null) {
							data = "";
						} else {
							//begin SPR1207
							if(value instanceof NbaUctData){
								data = ((NbaUctData)value).getBesValue();
							}else if(value instanceof NbaStatesData){
								data = ((NbaStatesData)value).getStateCodeTrans();
							}else{
								data = "";
							}
							//end SPR1207
						}
						break;
					}
				case PROCESS : // process "value" is the method to execute - method must exist in this class
					{
						try {
							String methodCall = aField.getValue();
							Method method = this.getClass().getMethod(methodCall, null);
							data = (String) method.invoke(this, null);

						} catch (InvocationTargetException e) {
							throw new NbaBaseException("Exception during PROCESS - invalid method", e);
						} catch (Exception e) {
							throw new NbaBaseException("Exception during PROCESS - invalid method", e);
						}
						break;
					}
				case ZERO_PAD : // pad "value" with zero for "size"
					{
						data = addFiller('0', aField.getSize() - aField.getValue().length());
						data = data + aField.getValue();
						break;
					}
				case FINALIZE : // Total Record Count
					{
						//                        data = String.valueOf(recordCount + 1);
						break;
					}
				default :
					{
						errorMsg.append("An unknown valueType (" + aField.getValueType() + ") was encountered for " + aField.getName() + ".");
						break;
					}
			}
		}
		if (data.length() == 0 && aField.getRequired() == true) {
			//begin SPR1135
			if (aField.getDefault() != null && aField.getDefault().length() > 0) {
				data = aField.getDefault();
			} else {
				errorMsg.append(FIELD_MISSING + aField.getName());
				return "";
			}
			//end SPR1135
		}
		if (data.length() > aField.getSize()) {
			data = data.substring(0, aField.getSize());
		}
		return fixedFormat(data, aField.getSize(), filler);
	} catch (NbaDataAccessException ndae) {
		throw new NbaBaseException(ndae);
	} catch (NbaBaseException nbe) {
		errorMsg.append(FIELD_MISSING + aField.getName());
	}
	return "";
}
/**
 * This method reads the Record Breeze object and passes each of the Fields within that Record
 * to get their associated data.  The resulting data is accumulated in a buffer for return to
 * the caller.
 * @param aRecord a Record Breeze object 
 * @return String[] An array of strings containing the data retrieved from all the Fields and any errors
 *               that may have occurred
 */
public String[] processRecord(Record aRecord) throws NbaBaseException {
    StringBuffer data = new StringBuffer();
    errorMsg.delete(0, errorMsg.length());
    boolean debugLogging = getLogger().isDebugEnabled(); // NBA027
    for (int i = 0; i < aRecord.getFieldCount(); i++) {
        com.csc.fsg.nba.provideradapter.xmlschema.Field aField = aRecord.getFieldAt(i);
        if (debugLogging) { // NBA027
        	getLogger().logDebug("Processing " + aField.getName());
        } // NBA027
        data.append(processField(aField));
    }
    String results[] = new String[2];
    results[0] = data.toString();
    results[1] = errorMsg.toString();
    return results;
}
/**
 * Initializes the errorMsg member
 * @param newErrorMsg new error message
 */
public void setErrorMsg(java.lang.StringBuffer newErrorMsg) {
	errorMsg = newErrorMsg;
}
/**
 * Sets the class' filler character.
 * @param newFiller new filler
 */
public void setFiller(char newFiller) {
	filler = newFiller;
}
/**
 * Initializes the oinkData member.
 * @param newOinkData
 */
public void setOinkData(NbaOinkDataAccess newOinkData) {
	oinkData = newOinkData;
}
/**
 * Initializes the provider.
 * @param newProvider new provider
 */
public void setProvider(java.lang.String newProvider) {
	provider = newProvider;
}
/**
 * Initializes the requester object
 * @param newRequester
 */
public void setRequester(NbaOinkRequest newRequester) {
	requester = newRequester;
}
}
