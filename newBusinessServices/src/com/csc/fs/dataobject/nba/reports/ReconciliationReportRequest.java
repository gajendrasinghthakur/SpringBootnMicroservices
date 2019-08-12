
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

public class ReconciliationReportRequest extends AccelDataObject {

    private String depositTime;

    private String reportName;

    private String subReportOpenBundles;

    private String subReportClosedAndRejected;

    private String userID;
    private String locationID;//NBA228

    private String closeTime;

    /**
     * getter for closeTime
     * 
     * @return Returns the closeTime.
     */
    public String getCloseTime() {
        return closeTime;
    }

    /**
     * setter for closeTime
     * 
     * @param closeTime
     *            The closeTime to set.
     */
    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    /**
     * getter for depositTime
     * 
     * @return Returns the depositTime.
     */
    public String getDepositTime() {
        return depositTime;
    }

    /**
     * setter for depositTime
     * 
     * @param depositTime
     *            The depositTime to set.
     */
    public void setDepositTime(String depositTime) {
        this.depositTime = depositTime;
    }

    /**
     * getter for reportName
     * 
     * @return Returns the reportName.
     */
    public String getReportName() {
        return reportName;
    }

    /**
     * setter for reportName
     * 
     * @param reportName
     *            The reportName to set.
     */
    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    /**
     * getter for userID
     * 
     * @return Returns the userID.
     */
    public String getUserID() {
        return userID;
    }

    /**
     *  setter for userID
     * 
     * @param userID
     *            The userID to set.
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * getter for subReportClosedAndRejected
     * 
     * @return Returns the subReportClosedAndRejected.
     */
    public String getSubReportClosedAndRejected() {
        return subReportClosedAndRejected;
    }

    /**
     *  setter for subReportClosedAndRejected 
     * 
     * @param subReportClosedAndRejected The subReportClosedAndRejected to set.
     */
    public void setSubReportClosedAndRejected(String subReportClosedAndRejected) {
        this.subReportClosedAndRejected = subReportClosedAndRejected;
    }

    /**
     * getter for subReportOpenBundles
     * 
     * @return Returns the subReportOpenBundles.
     */
    public String getSubReportOpenBundles() {
        return subReportOpenBundles;
    }

    /**
     * setter for subReportOpenBundles
     * 
     * @param subReportOpenBundles The subReportOpenBundles to set.
     */
    public void setSubReportOpenBundles(String subReportOpenBundles) {
        this.subReportOpenBundles = subReportOpenBundles;
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
}
