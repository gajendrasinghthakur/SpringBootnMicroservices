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
 * This class is used as Data Object class for Manager reports.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * <tr><td>NBA228</td><td>Version 8</td><td>Cash Management Enhancement</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class GenerateManagerReportsRequest extends AccelDataObject {

    private String fromDate;
    private String toDate;
    private String reportName;
    private String userID;
    private String cashActRptSubReport;  //NBA228
    
    /**
     * @return Returns the userID.
     */
    public String getUserID() {
        return userID;
    }
    /**
     * @param userID The userID to set.
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }
    /**
     * @return Returns the fromTentativeDisp.
     */
    public String getFromDate() {
        return fromDate;
    }
    /**
     * @param fromTentativeDisp The fromTentativeDisp to set.
     */
    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }
    /**
     * @return Returns the reportName.
     */
    public String getReportName() {
        return reportName;
    }
    /**
     * @param reportName The reportName to set.
     */
    public void setReportName(String reportName) {
        this.reportName = reportName;
    }
    /**
     * @return Returns the toTentativeDisp.
     */
    public String getToDate() {
        return toDate;
    }
    /**
     * @param toTentativeDisp The toTentativeDisp to set.
     */
    public void setToDate(String toDate) {
        this.toDate = toDate;
    }
    /**
     * @return Returns the cashActRptSubReport.
     */
    //NBA228 new method
    public String getCashActRptSubReport() {
        return cashActRptSubReport;
    }
    /**
     * @param cashActRptSubReport The cashActRptSubReport to set.
     */
    //NBA228 new method    
    public void setCashActRptSubReport(String cashActRptSubReport) {
        this.cashActRptSubReport = cashActRptSubReport;
    }    
}
