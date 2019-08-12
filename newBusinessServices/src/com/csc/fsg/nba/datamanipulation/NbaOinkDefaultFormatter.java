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
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.vo.NbaTime;

/**
*  NbaOinkDefaultFormatter converts retrieved values into strings.  Raw (no table 
*  translation) values are returned.  For dates, the formatting and date separator
*  character may be specified. Date formatting options are: YYYYMMDD and MMDDYYYY.  
*  Date separator options are: none, "/" and "-".
*  <p>
*  <b>Modifications:</b><br>
*  <table border=0 cellspacing=5 cellpadding=5>
*  <thead>
*  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
*  </thead>
*  <tr><td>NBA021</td><td>Version 2</td><td>Object Interactive Name Keeper</td></tr>
*  <tr><td>SPR2030</td><td>Version 4</td><td>NbaOinkDefaultFormatter.getStringValueFor() concatenates multiple results into one string</td></tr>
*  <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
*  </table>
*  <p>
*  @author CSC FSG Developer
 * @version 7.0.0
*  @since New Business Accelerator - Version 2
* 
*/
public class NbaOinkDefaultFormatter implements NbaOinkFormatter {
    protected int dateFormat = DATE_FORMAT_YYYYMMDD;
    protected int dateSeparator = DATE_SEPARATOR_NONE;
    protected NbaOinkRequest nbaOinkRequest;
    protected NbaOinkDataAccess nbaOinkDataAccess;

    protected int timeFormat = TIME_FORMAT_HHMMSS;
    protected int timeSeparator = TIME_SEPARATOR_NONE;
/**
 * NbaHTMLDataFormatter constructor.
 */
public NbaOinkDefaultFormatter() {
	super();
}
/**
 * Construct the JavaScript to wrapper the the value for the variable identified in the request.
 * By default, do nothing.
 * @param aNbaOinkRequest - data request container
 */
public void generateJavaScript(NbaOinkRequest aNbaOinkRequest) {
}
/**
 * Get the date format
 * @return int
 */
public int getDateFormat() {
	return dateFormat;
}
/**
 * Get the date separator type
 * @return int
 */
public int getDateSeparator() {
	return dateSeparator;
}
/**
 * Get the nbaOinkDataAccess object
 * @return com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess
 */
protected NbaOinkDataAccess getNbaOinkDataAccess() {
	return nbaOinkDataAccess;
}
/**
 * Get the nbaOinkDataAccess nbaOinkRequest
 * @return com.csc.fsg.nba.datamanipulation.NbaOinkRequest
 */
protected NbaOinkRequest getNbaOinkRequest() {
	return nbaOinkRequest;
}
/**
 * Answer the formatted data for the variable contained in the NbaOinkRequest.
 * @param aNbaOinkRequest - data request container
 */
public String getStringValueFor(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
    setNbaOinkRequest(aNbaOinkRequest);
    String returnValue = "";	//SPR2030
    if (aNbaOinkRequest.getValue().size() > 0) {	//SPR2030
        switch (getNbaOinkRequest().getValueType()) {
            case NbaOinkRequest.VALUE_BOOLEAN :
                {
                    returnValue = getStringValueFor((Boolean) aNbaOinkRequest.getValue().elementAt(0));	//SPR2030
                    break;
                }
            case NbaOinkRequest.VALUE_STRING :
                {
                    returnValue = getStringValueFor((String) aNbaOinkRequest.getValue().elementAt(0));	//SPR2030
                    break;
                }
            case NbaOinkRequest.VALUE_DATE :
                {
                    returnValue = getStringValueFor((Date) aNbaOinkRequest.getValue().elementAt(0));	//SPR2030
                    break;
                }
            case NbaOinkRequest.VALUE_LONG :
                {
                    returnValue = getStringValueFor((Long) aNbaOinkRequest.getValue().elementAt(0));	//SPR2030
                    break;
                }
            case NbaOinkRequest.VALUE_DOUBLE :
                {
                    returnValue = getStringValueFor((Double) aNbaOinkRequest.getValue().elementAt(0));	//SPR2030
                    break;
                }
            case NbaOinkRequest.VALUE_INT :
                {
                    returnValue = getStringValueFor((Integer) aNbaOinkRequest.getValue().elementAt(0));	//SPR2030
                    break;
                }
            case NbaOinkRequest.VALUE_TIME :
                {
                    returnValue = getStringValueFor((NbaTime) aNbaOinkRequest.getValue().elementAt(0));	//SPR2030
                    break;
                }
        }
    }
    return returnValue;	//SPR2030
}
/**
 * Answer the String value for a <code>NbaTime</code>.
 * @param value
 * @return String
 */
protected String getStringValueFor(NbaTime value) {
    StringBuffer timeString = new StringBuffer();

    if (value != null && !value.equals("")) {
        String separator = "";
        switch (getTimeSeparator()) {
            case TIME_SEPARATOR_COLON :
                {
                    separator = ":";
                    break;
                }
        }

        switch (getTimeFormat()) {
            case TIME_FORMAT_HHMMSS :
                {
                    Calendar c = Calendar.getInstance();
                    c.setTime(value.getTime());
                    if(c.get(Calendar.HOUR) < 10) timeString.append("0");                    
                    timeString.append(c.get(Calendar.HOUR));
                    timeString.append(separator);
                    if(c.get(Calendar.MINUTE) < 10) timeString.append("0");                    
                    timeString.append(c.get(Calendar.MINUTE));
                    timeString.append(separator);
                    if(c.get(Calendar.SECOND) < 10) timeString.append("0");                    
                    timeString.append(c.get(Calendar.SECOND));
                    break;
                }
        }

    }
    return timeString.toString();
}
/**
 * Answer the String value for a Boolean.
 * @param value
 * @return String
 */
protected String getStringValueFor(Boolean value) throws NbaDataAccessException {
	if (value != null) {
		return getTranslatedValue(value.toString());
	}
	return "false";
}
/**
 * Answer the String value for a Double.
 * @param value
 * @return String
 */
protected String getStringValueFor(Double value) throws NbaDataAccessException {
	if (value != null && !value.isNaN()) {
		double valueWholePart = Math.floor(value.doubleValue()); //ALII1660
		if(value.doubleValue() - valueWholePart > 0) { //ALII1660
			return getTranslatedValue((new BigDecimal(value.toString())).toString()); //ALII841, ALII1660
		} 
		return getTranslatedValue((new BigDecimal(Double.parseDouble(value.toString()))).toString()); //ALII1660
	}
	return "";
}
/**
 * Answer the String value for a Integer.
 * @param value
 * @return String
 */
protected String getStringValueFor(Integer value) throws NbaDataAccessException {
	if (value != null) {
		return getTranslatedValue(value.toString());
	}
	return "";
}
/**
 * Answer the String value for a Long.
 * @param value
 * @return String
 */
protected String getStringValueFor(Long value) throws NbaDataAccessException {
	if (value != null && !value.equals(new Long(-1L))) {
		return getTranslatedValue(value.toString());
	}
	return "";
}
/**
 * Answer the String value for a String.
 * @param value
 * @return String
 */
protected String getStringValueFor(String value)throws NbaDataAccessException {
    if (value != null) {
        return getTranslatedValue(value);
    }
    return "";
}
/**
 * Answer the String value for a Date.
 * @param value
 * @return String
 */
protected String getStringValueFor(Date value) {
    if (value != null && !value.equals("")) {
        switch (getDateFormat()) {
            case DATE_FORMAT_MMDDYYYY :
                {
                    switch (getDateSeparator()) {
                        case DATE_SEPARATOR_NONE :
                            {
                                return sdf_us.format(value);
                            }
                        case DATE_SEPARATOR_DASH :
                            {
                                return sdf_us_with_dash.format(value);
                            }
                        case DATE_SEPARATOR_SLASH :
                            {
                                return sdf_us_with_slash.format(value);
                            }
                    }
                    break;
                }
            case DATE_FORMAT_YYYYMMDD :
                {
                    switch (getDateSeparator()) {
                        case DATE_SEPARATOR_NONE :
                            {
                                return sdf_iso.format(value);
                            }
                        case DATE_SEPARATOR_DASH :
                            {
                                return sdf_iso_with_dash.format(value);
                            }
                        case DATE_SEPARATOR_SLASH :
                            {
                                return sdf_iso_with_slash.format(value);
                            }
                    }
                    break;
                }
            case DATE_FORMAT_MMDDYY :
                {
                    switch (getDateSeparator()) {
                        case DATE_SEPARATOR_NONE :
                            {
                                return sdf_us_abbrv.format(value);
                            }
                        case DATE_SEPARATOR_DASH :
                            {
                                return sdf_us_abbrv_with_dash.format(value);
                            }
                        case DATE_SEPARATOR_SLASH :
                            {
                                return sdf_us_abbrv_with_slash.format(value);
                            }
                    }
                    break;
                }
        }
    }
    return "";
}
/**
 * Answer a String [] containing the formatted data for the variable contained
 * in the NbaOinkRequest.
 * @param aNbaOinkRequest - data request container
 */
public String[] getStringValuesFor(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
    setNbaOinkRequest(aNbaOinkRequest);
    String[] returnArray = new String[aNbaOinkRequest.getValue().size()];
    for (int valueIndx = 0; valueIndx < aNbaOinkRequest.getValue().size(); valueIndx++) {
        switch (aNbaOinkRequest.getValueType()) {
            case NbaOinkRequest.VALUE_BOOLEAN :
                {
                    returnArray[valueIndx] = getStringValueFor((Boolean) aNbaOinkRequest.getValue().elementAt(valueIndx));
                    break;
                }
            case NbaOinkRequest.VALUE_STRING :
                {
                    returnArray[valueIndx] = getStringValueFor((String) aNbaOinkRequest.getValue().elementAt(valueIndx));
                    break;
                }
            case NbaOinkRequest.VALUE_DATE :
                {
                    returnArray[valueIndx] = getStringValueFor((Date) aNbaOinkRequest.getValue().elementAt(valueIndx));
                    break;
                }
            case NbaOinkRequest.VALUE_LONG :
                {
                    returnArray[valueIndx] = getStringValueFor((Long) aNbaOinkRequest.getValue().elementAt(valueIndx));
                    break;
                }
            case NbaOinkRequest.VALUE_DOUBLE :
                {
                    returnArray[valueIndx] = getStringValueFor((Double) aNbaOinkRequest.getValue().elementAt(valueIndx));
                    break;
                }
            case NbaOinkRequest.VALUE_INT :
                {
                    returnArray[valueIndx] = getStringValueFor((Integer) aNbaOinkRequest.getValue().elementAt(valueIndx));
                    break;
                }
            case NbaOinkRequest.VALUE_TIME :
                {
                    returnArray[valueIndx] = getStringValueFor((NbaTime) aNbaOinkRequest.getValue().elementAt(valueIndx));
                    break;
                }
        }
    }
    return returnArray;
}
/**
 * Insert the method's description here.
 * Creation date: (12/3/2002 1:23:58 PM)
 * @return int
 */
public int getTimeFormat() {
	return timeFormat;
}
/**
 * Insert the method's description here.
 * Creation date: (12/3/2002 1:29:32 PM)
 * @return int
 */
public int getTimeSeparator() {
	return timeSeparator;
}
/**
 * Get the translated value if applicable.
 * @return the translated value if tranlsation has been requested and there
 * is a translation. Otherwise, return the original value.
 */
protected String getTranslatedValue(String aValue) throws NbaDataAccessException {
	if (getNbaOinkRequest().isTableTranslations()) {
		String tableName = getNbaOinkRequest().getTable();
		if (tableName != null && tableName.length() > 0) {
			if (getNbaOinkRequest().isBesTranslate()) {
				return getNbaOinkDataAccess().getBesTranslation(tableName, aValue);
			} else {
				return getNbaOinkDataAccess().getTableTranslation(tableName, aValue);
			}
		}
	}
	return aValue;
}
/**
 * Set the date format
 * @param newDateFormat int
 */
public void setDateFormat(int newDateFormat) {
	dateFormat = newDateFormat;
}
/**
 * Set the date separator type
 * @param newDateSeparator int
 */
public void setDateSeparator(int newDateSeparator) {
	dateSeparator = newDateSeparator;
}
/**
 * Set a reference to the NbaOinkDataAccess object 
 * @param newNbaOinkDataAccess com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess
 */
public void setNbaOinkDataAccess(NbaOinkDataAccess newNbaOinkDataAccess) {
	nbaOinkDataAccess = newNbaOinkDataAccess;
}
/**
 * Set a reference to the NbaOinkRequest object
 * @param newNbaOinkRequest com.csc.fsg.nba.datamanipulation.NbaOinkRequest
 */
protected void setNbaOinkRequest(NbaOinkRequest newNbaOinkRequest) {
	nbaOinkRequest = newNbaOinkRequest;
}
/**
 * Insert the method's description here.
 * Creation date: (12/3/2002 1:23:58 PM)
 * @param newTimeFormat int
 */
public void setTimeFormat(int newTimeFormat) {
	timeFormat = newTimeFormat;
}
/**
 * Insert the method's description here.
 * Creation date: (12/3/2002 1:29:32 PM)
 * @param newTimeSeparator int
 */
public void setTimeSeparator(int newTimeSeparator) {
	timeSeparator = newTimeSeparator;
}
}
