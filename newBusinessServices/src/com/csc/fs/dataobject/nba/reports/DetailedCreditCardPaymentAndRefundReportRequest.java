
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

public class DetailedCreditCardPaymentAndRefundReportRequest extends AccelDataObject {

    private String reportName;

    private String userID;
    private String draft;//NBA228

    private String subReportCreditCardPasses;

    private String subReportCreditCardFailed;

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
     * @param reportName The reportName to set.
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
     * setter for userID
     * 
     * @param userID The userID to set.
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * getter for subReportCreditCardFailed
     * 
     * @return Returns the subReportCreditCardFailed.
     */

    public String getSubReportCreditCardFailed() {
        return subReportCreditCardFailed;
    }

    /**
     * setter for subReportCreditCardFailed
     * 
     * @param subReportCreditCardFailed The subReportCreditCardFailed to set.
     */
    public void setSubReportCreditCardFailed(String subReportCreditCardFailed) {
        this.subReportCreditCardFailed = subReportCreditCardFailed;
    }

    /**
     * getter for subReportCreditCardPasses
     * 
     * @return Returns the subReportCreditCardPasses.
     */
    public String getSubReportCreditCardPasses() {
        return subReportCreditCardPasses;
    }

    /**
     * setter for subReportCreditCardPasses
     * 
     * @param subReportCreditCardPasses The subReportCreditCardPasses to set.
     */
    public void setSubReportCreditCardPasses(String subReportCreditCardPasses) {
        this.subReportCreditCardPasses = subReportCreditCardPasses;
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
