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
import java.util.Date;

import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaObjectPrinter;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.html.support.NbaHTMLHelper;
import com.csc.fsg.nba.tableaccess.NbaTableData;

/**
 *  NbaOinkHTMLFormatter converts retrieved values into strings and wrappers the 
 *  strings with the JavaScript necessary to present the values in an HTML page.
 *  For variables that have a formatting identifier of "EF", table translated values
 *  are returned if applicable. For variables that have a formatting identifier of "DD", 
 *  JavaScript is generated to populate the dropdown list.  An instance of NbaHTMLHelper 
 *  is used to perform the JavaScript formatting.  Unlike the NbaOinkDefaultFormatter, 
 *  NbaOinkHTMLFormatter does not return a formatted response for each variable.  
 *  Instead, the NbaHTMLHelper instance holds the results until all variables have 
 *  been processed, and returns the entire formatted JavaScript result as a String.
 *  <p>
 *  <b>Modifications:</b><br>
 *  <table border=0 cellspacing=5 cellpadding=5>
 *  <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 *  </thead>
 *  <tr><td>NBA021</td><td>Version 2</td><td>Object Interactive Name Keeper</td></tr>
 *  <tr><td>NBA031</td><td>Version 3</td><td>Rewrite logon/menu/status bar in HTML</td></tr>
 *  <tr><td>SPR2149</td><td>Version 4</td><td>Face amount LOB is not set when Current amount has 9 or more digits</td></tr> 
 *  <tr><td>ACN007</td><td>Version 4</td><td>Reflexive Questioning</td></tr>
 *  <tr><td>NBA212</td><td>Version 7</td><td>Content Services</td></tr>
 * 	<tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 *  </table>
 *  <p>
 *  @author CSC FSG Developer
 * @version 7.0.0
 *  @since New Business Accelerator - Version 2
 * 
 */
public class NbaOinkHTMLFormatter implements NbaDataFormatConstants{
	protected java.util.Map tables = new java.util.HashMap();
	protected NbaOinkRequest nbaOinkRequest;
	protected NbaOinkDataAccess nbaOinkDataAccess;
	protected NbaHTMLHelper nbaHTMLHelper = new NbaHTMLHelper();;
/**
 * NbaHTMLDataFormatter constructor.
 */
public NbaOinkHTMLFormatter() {
	super();
}
/**
 * NbaHTMLDataFormatter constructor.
 */
public NbaOinkHTMLFormatter(NbaOinkDataAccess newNbaOinkDataAccess) {
    super();
    setNbaOinkDataAccess(newNbaOinkDataAccess);
}
/**
 * Format the value for an HTML checkbox.
 */
protected void formatCB() {
	switch (getNbaOinkRequest().getValueType()) {
		case NbaOinkRequest.VALUE_BOOLEAN :
			{
				boolean[] values = new boolean[getNbaOinkRequest().getCount()];
				for (int i = 0; i < getNbaOinkRequest().getValue().size(); i++) {
					Boolean value = (Boolean) getNbaOinkRequest().getValue().elementAt(i);
					values[i] = value.booleanValue();
				}
				getNbaHTMLHelper().setNonEFValues(getNbaOinkRequest().getVariable(), values, getNbaOinkRequest().getFormatting(), false);
			}
	}
}
/**
 * Format the value for an HTML dropdown.
 */
protected void formatDD() throws NbaDataAccessException {
	generateTableData();
	switch (getNbaOinkRequest().getValueType()) {
		case NbaOinkRequest.VALUE_STRING :
			{
				String[] values = new String[getNbaOinkRequest().getCount()];
				for (int i = 0; i < getNbaOinkRequest().getValue().size(); i++) {
					String value = ((String) getNbaOinkRequest().getValue().elementAt(i));
					if (value == null) {
						values[i] = "";
					} else {
						values[i] = value;
					}
				}
				getNbaHTMLHelper().addOptions(getNbaOinkRequest().getVariable(), values, getNbaOinkRequest().getTable(), false, true);
				break;
			}
		case NbaOinkRequest.VALUE_LONG :
			{
				long[] values = new long[getNbaOinkRequest().getCount()];
				for (int i = 0; i < getNbaOinkRequest().getValue().size(); i++) {
					long value = ((Long) getNbaOinkRequest().getValue().elementAt(i)).longValue();
					values[i] = value;
				}
				getNbaHTMLHelper().addOptions(getNbaOinkRequest().getVariable(), values, getNbaOinkRequest().getTable(), false, true);
				break;
			}
	}
}
/**
 * Format the value for an HTML Entry Field.
 */
protected void formatEF() {
	switch (getNbaOinkRequest().getValueType()) {
		case NbaOinkRequest.VALUE_BOOLEAN :
			{
				String[] values = new String[getNbaOinkRequest().getCount()];
				for (int i = 0; i < getNbaOinkRequest().getValue().size(); i++) {
					Boolean value = (Boolean) getNbaOinkRequest().getValue().elementAt(i);
					values[i] = String.valueOf(value);
				}
				getNbaHTMLHelper().setEFValues(getNbaOinkRequest().getVariable(), values, getNbaOinkRequest().getContentType(), false);
				break;
			}
		case NbaOinkRequest.VALUE_STRING :
			{
				String[] values = new String[getNbaOinkRequest().getCount()];
				for (int i = 0; i < getNbaOinkRequest().getValue().size(); i++) {
					String value = getTableValue((String) getNbaOinkRequest().getValue().elementAt(i));
					values[i] = value;
				}
				getNbaHTMLHelper().setEFValues(getNbaOinkRequest().getVariable(), values, getNbaOinkRequest().getContentType(), false);
				break;
			}
		case NbaOinkRequest.VALUE_DATE :
			{
				Date[] values = new Date[getNbaOinkRequest().getCount()];
				for (int i = 0; i < getNbaOinkRequest().getValue().size(); i++) {
					Date value = (Date) getNbaOinkRequest().getValue().elementAt(i);
					values[i] = value;
				}
				getNbaHTMLHelper().setEFValues(getNbaOinkRequest().getVariable(), values, false);
				break;
			}
		case NbaOinkRequest.VALUE_LONG :
			{
				String[] values = new String[getNbaOinkRequest().getCount()];
				for (int i = 0; i < getNbaOinkRequest().getValue().size(); i++) {
					Long value = (Long) getNbaOinkRequest().getValue().elementAt(i);
					values[i] = String.valueOf(value);
				}
				getNbaHTMLHelper().setEFValues(getNbaOinkRequest().getVariable(), values, getNbaOinkRequest().getContentType(), false);
				break;
			}
		case NbaOinkRequest.VALUE_DOUBLE :
			{
				String[] values = new String[getNbaOinkRequest().getCount()];
				for (int i = 0; i < getNbaOinkRequest().getValue().size(); i++) {
					Double value = (Double) getNbaOinkRequest().getValue().elementAt(i);
					if (value.equals(new Double(Double.NaN))) {
						values[i] = null;
					} else if (value.doubleValue() > 9999999.99) {	//SPR2149
						values[i] = NbaObjectPrinter.localeUnformattedDecimal(value.doubleValue());	//SPR2149
					} else {
						values[i] = String.valueOf(value);
					}
				}
				getNbaHTMLHelper().setEFValues(getNbaOinkRequest().getVariable(), values, getNbaOinkRequest().getContentType(), false);
				break;
			}
	}
}
/**
 * Format the value for an HTML radio button.
 */
protected void formatRB() {
	switch (getNbaOinkRequest().getValueType()) {
		case NbaOinkRequest.VALUE_BOOLEAN :
			{
				int valueSize = getNbaOinkRequest().getCount();
				if (valueSize == 1) {
					Boolean aBoolean = (Boolean) getNbaOinkRequest().getValue().elementAt(0);
					if (aBoolean != null) {
						long value = (aBoolean.equals(Boolean.FALSE) ? 0 : 1);
						getNbaHTMLHelper().setNonEFValue(getNbaOinkRequest().getVariable(), value, FIELD_TYPE_RADIOBUTTON, false);
					}
				} else {
					long[] values = new long[valueSize];
					for (int i = 0; i < getNbaOinkRequest().getValue().size(); i++) {
						Boolean value = (Boolean) getNbaOinkRequest().getValue().elementAt(i);
						if (value != null) {
							values[i] = (value.equals(Boolean.FALSE) ? 0 : 1);
						}
					}
					getNbaHTMLHelper().setNonEFValues(getNbaOinkRequest().getVariable(), values, FIELD_TYPE_RADIOBUTTON, false);
				}
				break;
			}
		case NbaOinkRequest.VALUE_LONG :
			{
				int valueSize = getNbaOinkRequest().getCount();
				if (valueSize == 1) {
					Long value = (Long) getNbaOinkRequest().getValue().elementAt(0);
					if (value != null) {
						getNbaHTMLHelper().setNonEFValue(getNbaOinkRequest().getVariable(), value.longValue(), FIELD_TYPE_RADIOBUTTON, false);
					}
				} else {
					long[] values = new long[valueSize];
					for (int i = 0; i < getNbaOinkRequest().getValue().size(); i++) {
						Long value = (Long) getNbaOinkRequest().getValue().elementAt(i);
						if (value != null) {
							values[i] = value.longValue();
						}
					}
					getNbaHTMLHelper().setNonEFValues(getNbaOinkRequest().getVariable(), values, FIELD_TYPE_RADIOBUTTON, false);
				}
			}
			break;
	}
}
/**
 * Construct the JavaScript to wrapper the the value for the variable identified in the request.
 * @param aNbaOinkRequest - data request container
 */
public void generateJavaScript(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
	setNbaOinkRequest(aNbaOinkRequest);
	switch (aNbaOinkRequest.getFormatting()) {
		case FIELD_TYPE_TEXT :
			{
				formatEF();
				break;
			}
		case FIELD_TYPE_DROPDOWN :
			{
				formatDD();
				break;
			}
		case FIELD_TYPE_RADIOBUTTON :
			{
				formatRB();
				break;
			}
		case FIELD_TYPE_CHECKBOX :
			{
				formatCB();
				break;
			}
	}
}
/**
 * If the table identified in tableName has not been 
 * previously processed, generate the HTML representing 
 * the table name, codes, and values for the table entries.
 * @param tableName - the name of the table
 * @return boolean <code>true</code> if this first time 
 * the table has has been encountered.
 */
protected void generateTableData() throws NbaDataAccessException {
    String tableName = getNbaOinkRequest().getTable();
    if (tableName != null && tableName.length() > 0) {
        if (!getTables().containsKey(tableName)) {
            getTables().put(tableName, "");
            NbaTableData[] data = getNbaOinkDataAccess().getNbaTableData(tableName);
            getNbaHTMLHelper().createArrays(tableName, data);
        }
    }
}
/**
 * Return the generated JavaScript
 * @return String
 */
public String getJavaScript(String sourceId) {
	 //begin NBA212
	 StringBuffer str = new StringBuffer();
	 str.append("parent.imageCallBack(\"");
	 str.append(sourceId);
	 str.append("\",false);\n"); 
     getNbaHTMLHelper().setEFValue("SID", sourceId, FIELD_TYPE_TEXT, false);
     getNbaHTMLHelper().sendScript(str.toString()); 
     //end NBA212
     return getNbaHTMLHelper().getData();
}
/**
 * Return the value for the NbaHTMLHelper object
 * @return com.csc.fsg.nba.html.support.NbaHTMLHelper
 */
protected com.csc.fsg.nba.html.support.NbaHTMLHelper getNbaHTMLHelper() {
	return nbaHTMLHelper;
}
/**
 * Return the value for the NbaOinkDataAccess object
 * @return com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess
 */
protected NbaOinkDataAccess getNbaOinkDataAccess() {
	return nbaOinkDataAccess;
}
/**
 * Answer the NbaOinkRequest object
 * @return com.csc.fsg.nba.datamanipulation.NbaOinkRequest
 */
protected NbaOinkRequest getNbaOinkRequest() {
	return nbaOinkRequest;
}
/**
 * Answer the map containing the tables previously encountered
 * @return java.util.Map
 */
protected java.util.Map getTables() {
	return tables;
}
/**
 * Get the translated table value for the value.
 * @return the translated table value
 */
protected String getTableValue(String aValue) {
	if (aValue == null || aValue.length() < 0) {
		return aValue;
	}
	String tableName = getNbaOinkRequest().getTable();
	if (tableName == null) {
		return aValue;
	}
	if (tableName.equals(NbaTableConstants.NBA_PLANS)) {
		try {
			return getNbaOinkDataAccess().getPlanRiderKeyTranslation(aValue);
		} catch (NbaDataAccessException e) {
			return aValue;
		}
	}
	return aValue;
}
/**
 * Set the value for the NbaHTMLHelper object
 * @param newNbaHTMLHelper com.csc.fsg.nba.html.support.NbaHTMLHelper
 */
protected void setNbaHTMLHelper(com.csc.fsg.nba.html.support.NbaHTMLHelper newNbaHTMLHelper) {
	nbaHTMLHelper = newNbaHTMLHelper;
}
/**
 * Set the value for the NbaOinkDataAccess object
 * @param newNbaOinkDataAccess com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess
 */
void setNbaOinkDataAccess(NbaOinkDataAccess newNbaOinkDataAccess) {
	nbaOinkDataAccess = newNbaOinkDataAccess;
}
/**
 * Set the value for the NbaOinkRequest object
 * @param newNbaOinkRequest com.csc.fsg.nba.datamanipulation.NbaOinkRequest
 */
protected void setNbaOinkRequest(NbaOinkRequest newNbaOinkRequest) {
	nbaOinkRequest = newNbaOinkRequest;
}
}
