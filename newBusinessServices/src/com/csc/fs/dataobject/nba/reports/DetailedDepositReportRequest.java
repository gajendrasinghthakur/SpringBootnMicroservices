
package com.csc.fs.dataobject.nba.reports;

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

import com.csc.fs.dataobject.accel.AccelDataObject;

/**
 * This class is used to transfer data from calling class to system access.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA198</td><td>Version 7</td><td>Wrapper for Crystal or Pure Java</td></tr>
 * <tr><td>NBA228</td><td>Version 8</td><td>Cash Management Enhancement</td></tr>  
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class DetailedDepositReportRequest extends AccelDataObject {

    private String depositTime;

    private String reportName;

    private String userID;
    private String locationID;//NBA228
    private String draft;//NBA228

    /**
     * @return Returns the depositTime.
     */
    public String getDepositTime() {
        return depositTime;
    }

    /**
     * @param depositTime
     *            The depositTime to set.
     */
    public void setDepositTime(String depositTime) {
        this.depositTime = depositTime;
    }

    /**
     * @return Returns the reportName.
     */
    public String getReportName() {
        return reportName;
    }

    /**
     * @param reportName
     *            The reportName to set.
     */
    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    /**
     * @return Returns the userID.
     */
    public String getUserID() {
        return userID;
    }

    /**
     * @param userID
     *            The userID to set.
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }
	/**
	 * @return Returns the locationID.
	 */
    //NBA228 New Method
	public String getLocationID() {
		return locationID;
	}
	/**
	 * @param locationID The locationID to set.
	 */
    //NBA228 New Method
	public void setLocationID(String locationID) {
		this.locationID = locationID;
	}

	/**
	 * @return Returns the draft.
	 */
    //NBA228 New Method
	public String getDraft() {
		return draft;
	}
	/**
	 * @param draft The draft to set.
	 */
    //NBA228 New Method
	public void setDraft(String draft) {
		this.draft = draft;
	}	
}
