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
import java.text.SimpleDateFormat;

import com.csc.fsg.nba.exception.NbaDataAccessException;

/**
 *  This is the interface class for Oink formatting.
 *  <p>
 *  <b>Modifications:</b><br>
 *  <table border=0 cellspacing=5 cellpadding=5>
 *  <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 *  </thead>
 *  <tr><td>NBA021</td><td>Version 2</td><td>Object Interactive Name Keeper</td></tr>
 * 	<tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 *  </table>
 *  <p>
 *  @author CSC FSG Developer
 * @version 7.0.0
 *  @since New Business Accelerator - Version 2
 * 
 */
public interface NbaOinkFormatter {
    static final int DATE_FORMAT_YYYYMMDD = 0;
    static final int DATE_FORMAT_MMDDYYYY = 1;
    static final int DATE_FORMAT_MMDDYY = 2;
    static final SimpleDateFormat sdf_iso_with_dash = new SimpleDateFormat("yyyy-MM-dd"); // case matters - see the javadoc
    static final SimpleDateFormat sdf_us_with_dash = new SimpleDateFormat("MM-dd-yyyy"); // case matters - see the javadoc
    static final SimpleDateFormat sdf_us_abbrv_with_dash = new SimpleDateFormat("MM-dd-yy"); // case matters - see the javadoc
    static final SimpleDateFormat sdf_iso = new SimpleDateFormat("yyyyMMdd"); // case matters - see the javadoc
    static final SimpleDateFormat sdf_us = new SimpleDateFormat("MMddyyyy"); // case matters - see the javadoc
    static final SimpleDateFormat sdf_us_abbrv = new SimpleDateFormat("MMddyy"); // case matters - see the javadoc
    static final SimpleDateFormat sdf_iso_with_slash = new SimpleDateFormat("yyyy/MM/dd"); // case matters - see the javadoc
    static final SimpleDateFormat sdf_us_with_slash = new SimpleDateFormat("MM/dd/yyyy"); // case matters - see the javadoc
    static final SimpleDateFormat sdf_us_abbrv_with_slash = new SimpleDateFormat("MM/dd/yy"); // case matters - see the javadoc
    static final int DATE_SEPARATOR_NONE = 0;
    static final int DATE_SEPARATOR_DASH = 1;
    static final int DATE_SEPARATOR_SLASH = 2;

    static final int TIME_FORMAT_HHMMSS = 0;
    static final int TIME_SEPARATOR_NONE = 0;
    static final int TIME_SEPARATOR_COLON = 1;
/**
 * Answer a String data value for the variable contained in the NbaOinkRequest.
 * @param aNbaOinkRequest - data request container
 */
public String getStringValueFor(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException ;
/**
 * Answer a String [] containing data value for the variable contained in the NbaOinkRequest.
 * @param aNbaOinkRequest - data request container
 */
public String[] getStringValuesFor(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException;
/**
 * Set the date format
 * @param newDateFormat int
 */
public void setDateFormat(int newDateFormat);
/**
 * Set the date separator type
 * @param newDateSeparator int
 */
public void setDateSeparator(int newDateSeparator);
/**
 * Set a reference to the NbaOinkDataAccess object 
 * @param newNbaOinkDataAccess com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess
 */
void setNbaOinkDataAccess(NbaOinkDataAccess newNbaOinkDataAccess);
}
