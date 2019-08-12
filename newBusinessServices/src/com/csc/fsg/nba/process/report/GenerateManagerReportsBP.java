package com.csc.fsg.nba.process.report;

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

import java.util.ArrayList;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.dataobject.nba.reports.GenerateManagerReportsRequest;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.GenerateManagerReportsRequestVO;

/**
 * Creates the Manager reports 
 * Accepts a <code>GenerateManagerReportsRequestVO</code> as input to create report
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

public class GenerateManagerReportsBP extends NewBusinessAccelBP {
    protected NbaLogger logger = null;
    public static final String REPORT_SUCCESS = "REPORT GENERATED SUCCESSFULLY: ";
    public static final String REPORT_FAILED = "REPORT FAILED: ";
    public static final String CASH_ACTIVITY_REPORT = "CashActRpt"; //NBA228
    public static final String CASH_ACTIVITY_SUBREPORT = "NBDLYACT_SUB.RPT"; //NBA228
    
    /* (non-Javadoc)
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            GenerateManagerReportsRequestVO generateManagerReportsRequestVO = (GenerateManagerReportsRequestVO) input;
            GenerateManagerReportsRequest doRequest = new GenerateManagerReportsRequest();
            List request = new ArrayList(2);
            String reportName = generateManagerReportsRequestVO.getSelectedReport();
            doRequest.setReportName(reportName);
            doRequest.setFromDate(NbaUtils.getStringFromDate(generateManagerReportsRequestVO.getFromDate()));
            doRequest.setToDate(NbaUtils.getStringFromDate(generateManagerReportsRequestVO.getToDate()));
            doRequest.setUserID(generateManagerReportsRequestVO.getUserID());
            //Begin NBA228
            if(reportName.equalsIgnoreCase(CASH_ACTIVITY_REPORT)) {
                doRequest.setCashActRptSubReport(CASH_ACTIVITY_SUBREPORT); 
            }
	    	//End NBA228	
            request.add(doRequest);
            Result reportResult = invoke("Reports/" + reportName, request, true);
            if (!reportResult.hasErrors()) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().logDebug(REPORT_SUCCESS + reportName);
                }
            } else {
                getLogger().logError(REPORT_FAILED + reportName + " - " + reportResult.getMessagesList().get(0));
            }
            request.clear();
        } catch (Exception e) {
            result = new AccelResult();
            addExceptionMessage(result, e);
        }
        return result;
    }
    /**
     * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
     * @return the logger implementation
     */
    protected NbaLogger getLogger() {
        if (logger == null) {
            try {
                logger = NbaLogFactory.getLogger(this.getClass());
            } catch (Exception e) {
                NbaBootLogger.log("NbaServlet could not get a logger from the factory.");
                e.printStackTrace(System.out);
            }
        }
        return logger;
    }
    
}
