package com.csc.fsg.nba.process.cashiering;

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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.dataobject.nba.reports.ChecksDepositReportRequest;
import com.csc.fs.dataobject.nba.reports.DepositTicketReportRequest;
import com.csc.fs.dataobject.nba.reports.DetailedCreditCardPaymentAndRefundReportRequest;
import com.csc.fs.dataobject.nba.reports.DetailedDepositReportRequest;
import com.csc.fs.dataobject.nba.reports.DetailedExcludedFromDepositReportRequest;
import com.csc.fs.dataobject.nba.reports.DetailedWireTransferReportRequest;
import com.csc.fs.dataobject.nba.reports.InterCompanyJournalReportRequest;
import com.csc.fs.dataobject.nba.reports.ReconciliationReportRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.tableaccess.NbaCheckData;
import com.csc.fsg.nba.tableaccess.NbaContractCheckData;
import com.csc.fsg.nba.tableaccess.NbaContractsWireTransferData;
import com.csc.fsg.nba.tableaccess.NbaCreditCardData;
import com.csc.fsg.nba.tableaccess.NbaDepositTicketData;
import com.csc.fsg.nba.vo.NbaCashBundleVO;
import com.csc.fsg.nba.vo.NbaCheckDepositAccountingExtractVO;

/**
 * Create Deposit Ticket
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA182</td><td>Version 7</td><td>Cashiering Rewrite</td></tr>
 * <tr><td>NBA228</td><td>Version 8</td><td>Cash Management Enhancement</td></tr>  
 * <tr><td>AXAL3.7.23</td><td>AXA Life phase 1</td><td>Accounting interface</td></tr>  
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class CreateDepositBP extends NewBusinessAccelBP {

    public static final BigDecimal ZERO = new BigDecimal("0");

    //report date format
    public final static String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.00";

    public final static String TIME_FORMAT_DETAIL = "MM-dd-yyyy HH:mm:ss";

    //report constants
    public static final String SUCCESS = "Success";

    public static final String REPORT_SUCCESS = "REPORT GENERATED SUCCESSFULLY: ";

    public static final String REPORT_FAILED = "REPORT FAILED: ";

    //begin NBA186
    public static final String DEPOSIT_TICKET = "cashiering/NBDEPTKT.rpt";
    
    public static final String VARIABLE_PRODUCT_RPT = "cashiering/NBCHKDEP.rpt";

    public static final String DEPOSIT_RPT = "cashiering/NBDETDEP.rpt";

    public static final String EXCLUDED_RPT = "cashiering/NBEXCDEP.rpt";

    public static final String WIRE_TRANSFER_RPT = "cashiering/NBWIRTRN.rpt";

    public static final String INTER_COMPANY_RPT = "cashiering/NBINCOJN.rpt";

    public static final String RECONCILIATION_RPT = "cashiering/NBRECNCL.rpt";
    
    public static final String RECONCILIATION_OPEN_BUNDLES_RPT = "OPEN_BUNDLES.rpt";
    
    public static final String RECONCILIATION_CLOSED_AND_REJECTED_RPT = "CLOSED_AND_REJECTED.rpt";
    
    public static final String CREDIT_CARD_SUB_PASSED_RPT = "NBCCPRPT_passed.rpt";
    
    public static final String CREDIT_CARD_SUB_FAILED_RPT = "NBCCPRPT_failed.rpt";

    public static final String CREDIT_CARD_RPT = "cashiering/NBCCPRPT.rpt";
    
    //end NBA186

    protected NbaLogger logger = null;

    /**
     * Called to create the deposit ticket.
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            if (input != null) {
                List selectedItems = (List) input;

                if (selectedItems.get(0) instanceof NbaCashBundleVO) {
                    result.addResult(createDeposit(selectedItems));

                } else if (selectedItems.get(0) instanceof NbaDepositTicketData) {
                    result.addResult(reCreateDeposit(selectedItems));
                }

            }

        } catch (Exception e) {
            addExceptionMessage(result, e);
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Updates the deposit information and generates the corresponding output.
     * @param depositTickets list of selected Deposits
     * @param request Object that encapsulates the request to the servlet
     * @return java.lang.String
     * @throws NbaBaseException, NbaDataAccessException
     */
    protected List reCreateDeposit(List depositTickets) throws NbaBaseException {  //AXAL3.7.23 - updated visibility

        NbaDepositTicketData depositTicketsVO = null;
        NbaCashieringTable cashTable = new NbaCashieringTable();
        cashTable.startTransaction();
        Date newDepositTime = new Date();
        Date originalDepositTime = null;
        boolean deposited = false;
        String userID = null;
        ArrayList depositExtractsList = new ArrayList();
        ArrayList depositList = new ArrayList();

        try {

            for (int i = 0; i < depositTickets.size(); i++) {

                depositTicketsVO = (NbaDepositTicketData) depositTickets.get(i);
                // if there is nothing to deposit, skip this selected bundle
                String bundleID = depositTicketsVO.getBundleID();
                originalDepositTime = depositTicketsVO.getDepositTime();
                userID = depositTicketsVO.getDepositUser();
                BigDecimal totalAmt = cashTable.updateChecksAsReDeposited(userID, bundleID, originalDepositTime, newDepositTime);
                NbaDepositTicketData[] depositTicketDataList = cashTable.getDepositTicketData(bundleID, originalDepositTime);
                NbaDepositTicketData ticketData = new NbaDepositTicketData();
                ticketData.setDepositTime(originalDepositTime);
                ticketData.setBundleID(bundleID);
                ticketData.setDepositUser(userID);
                ticketData.setRevisedTotalAmount(totalAmt);
                ticketData.setRevisedDepositTime(newDepositTime);
                ticketData.setCorrectionInd(NbaConstants.YES_VALUE);
                ticketData.setTotalAmount(depositTicketDataList[0].getTotalAmount());
                cashTable.executeUpdate(ticketData.getUpdateQuery());

                deposited = true;

                depositList.add(ticketData);
                ArrayList depositExtractVOlist = new ArrayList();
                createCorrectedCheckDepositAccExtractVOs(depositTicketDataList, depositExtractVOlist);
                depositExtractsList.add(depositExtractVOlist);

            }
            //createAccountingExtracts(request.getSession(),depositExtractsList, true, true); 
            //processCreditCardExtracts(request.getSession());
        } catch (NbaBaseException nbe) {
            cashTable.rollbackTransaction();
            throw nbe;
        }
        cashTable.commitTransaction();
        //NbaHTMLHelper helper = new NbaHTMLHelper();
        if (deposited) {
            generateReports(userID, originalDepositTime, cashTable);

            correctDepositTicketDataList(depositList);

        }
        return depositExtractsList;
    }

    /**
     * Called to create the Deposit Ticket.
     * @param bundleList list of bundles selected from bundle summary table
     * @throws NbaBaseException
     */

    protected List createDeposit(List bundleList) throws NbaBaseException {
        NbaCashBundleVO bundleVO = null;
        NbaCashieringTable cashTable = new NbaCashieringTable();
        ArrayList depositExtractsList = new ArrayList();
        String userID = null;
        cashTable.startTransaction();
        Date depositTime = new Date(); // set deposit time stamp
        boolean deposited = false; // flag to track whether we deposit anything
        try {

            for (int i = 0; i < bundleList.size(); i++) {
                // if there is nothing to deposit, skip this selected bundle
                bundleVO = new NbaCashBundleVO();
                bundleVO = (NbaCashBundleVO) bundleList.get(i);
                String company = bundleVO.getCompany();
                String bundleID = bundleVO.getBundleID();
                userID = bundleVO.getUserVO().getUserID();
                String scanStation = bundleVO.getScanStation(); //APSL3460
                if (!bundleVO.isDraft()) { //NBA228
                	// close the bundle to new checks and set current checks as deposited	
	                cashTable.closeBundle(company, bundleID);
	                BigDecimal totalAmt = cashTable.updateChecksAsDeposited(company, bundleID, depositTime);
	                // create deposit ticket entry
	                NbaDepositTicketData ticketData = new NbaDepositTicketData();
	                ticketData.setCompany(company);
	                ticketData.setBundleID(bundleID);
	                ticketData.setDepositTime(depositTime);
	                ticketData.setDepositUser(userID);
	                ticketData.setTotalAmount(totalAmt);
	                ticketData.insert();
	                deposited = true;
	                NbaCheckData[] checkDataList = cashTable.getCheckData(bundleID, NbaConstants.YES_VALUE, depositTime);
	                for (int j = 0; j < checkDataList.length; j++) {
	                    NbaContractCheckData[] contractCheckData = cashTable.getContractCheckData(bundleID, checkDataList[j].getTechnicalKey());
	                    ArrayList depositExtractVOlist = new ArrayList();
	                    if(null != scanStation && !scanStation.equalsIgnoreCase(NbaOliConstants.AXA_SCANSTATION_BNKLKBOX) ){ //APSL3460
	                        createCheckDepositAccExtractVOs(checkDataList[j], contractCheckData, company, bundleID, depositExtractVOlist);
	                    }
	                    depositExtractsList.add(depositExtractVOlist);
	                }
	            //Begin NBA228
                } else {
	                BigDecimal totalAmt = cashTable.updateChecksAsDraftDeposited(company, bundleID, depositTime);
	                // create deposit ticket entry
	                NbaDepositTicketData ticketData = new NbaDepositTicketData();
	                ticketData.setCompany(company);
	                ticketData.setBundleID(bundleID);
	                ticketData.setDepositTime(depositTime);
	                ticketData.setDepositUser(userID);
	                ticketData.setTotalAmount(totalAmt);
	                ticketData.insertDraft();
                }
                //End NBA228
            }

        } catch (NbaBaseException nbe) {
            cashTable.rollbackTransaction();
            throw nbe;
        }
        cashTable.commitTransaction();
        if (deposited) {
            try {
                cashTable.updateClosedBundles(depositTime);//SPR1191 - update closed bundles for reporting
            } catch (Exception e) {
                e.printStackTrace();
            }
            generateReports(userID, depositTime, cashTable);
        }
        if(bundleVO.isDraft()) //NBA228 
        	generateReports(userID, depositTime, cashTable, true); //NBA228
        	
        return depositExtractsList;
    }


    /**
     * Generate the cashiering reports.
     * 
     * @param userID
     *            userID of the person
     * @param depositTime
     *            java.util.Date
     * @param cashTable
     *            NbaCashieringTable
     * @throws NbaBaseException
     */
    //NBA228 New Method 
    protected void generateReports(String userId, Date depositTime, NbaCashieringTable cashTable) throws NbaBaseException {
    	generateReports(userId, depositTime, cashTable, false);
    }
   
    
    /**
     * Generate the cashiering reports.
     * 
     * @param userID
     *            userID of the person
     * @param depositTime
     *            java.util.Date
     * @param cashTable
     *            NbaCashieringTable
     * @param isDraft
     *            boolean
     * @throws NbaBaseException
     */
    //NBA228 method signature changed
    protected void generateReports(String userId, Date depositTime, NbaCashieringTable cashTable, boolean isDraft) throws NbaBaseException {//NBA228
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
        String depositTimeStamp = dateFormat.format(depositTime);
        
        

        //For DetailDepositReport
        //Begin NBA228
        List locationList = isDraft ? cashTable.getLocationsForDraft(depositTime) : cashTable.getLocations(depositTime);
        
	    for (int i = 0; i < locationList.size(); i++) {
	        String locationId = (String) locationList.get(i);
	        if(NbaUtils.isBlankOrNull(locationId)) {
	        	continue;
	        }
	        //End NBA228
	        SimpleDateFormat detailReportDateFormat = new SimpleDateFormat(TIME_FORMAT_DETAIL);
	        String detailDepositTimeStamp = detailReportDateFormat.format(depositTime);
	        boolean debugLogging = getLogger().isDebugEnabled();
	        if (debugLogging) {
	            getLogger().logDebug("Generating reports for deposit on " + depositTimeStamp);
	        }
	
	        List requestArray = new ArrayList();
	
	        //Invoking Cashiering Reports
	        // NBA198 Begin
	        if(!isDraft){ //NBA228
		        DepositTicketReportRequest doRequest = new DepositTicketReportRequest();
		        doRequest.setReportName(DEPOSIT_TICKET);
		        doRequest.setDepositTime(depositTimeStamp);
		        doRequest.setUserID(userId);
		        doRequest.setLocationID(locationId); //NBA228
		        requestArray.add(doRequest);
		        Result reportResult = invoke("Reports/DepositTicketReport", requestArray, true);
		        if (!reportResult.hasErrors()) {
		            if (debugLogging) {
		                getLogger().logDebug(REPORT_SUCCESS + DEPOSIT_TICKET);
		            }
		        } else {
		            getLogger().logError(REPORT_FAILED + DEPOSIT_TICKET + " - " + reportResult.getMessagesList().get(0));
		        }
		        requestArray.clear();
	        } //NBA228
	
	        ChecksDepositReportRequest doRequest1 = new ChecksDepositReportRequest();
	        doRequest1.setReportName(VARIABLE_PRODUCT_RPT);
	        doRequest1.setDepositTime(depositTimeStamp);
	        doRequest1.setUserID(userId);
	        doRequest1.setLocationID(locationId); //NBA228
	        doRequest1.setDraft(isDraft ? "Draft" : ""); //NBA228
	        requestArray.add(doRequest1);
	        Result reportResult1 = invoke("Reports/ChecksDepositReport", requestArray, true);
	        if (!reportResult1.hasErrors()) {
	            if (debugLogging) {
	                getLogger().logDebug(REPORT_SUCCESS + VARIABLE_PRODUCT_RPT);
	            }
	        } else {
	            getLogger().logError(REPORT_FAILED + VARIABLE_PRODUCT_RPT + " - " + reportResult1.getMessagesList().get(0));
	        }
	
	        DetailedDepositReportRequest doRequest2 = new DetailedDepositReportRequest();
	        doRequest2.setReportName(DEPOSIT_RPT);
	        doRequest2.setDepositTime(detailDepositTimeStamp);
	        doRequest2.setUserID(userId);
	        doRequest2.setLocationID(locationId); //NBA228
	        doRequest2.setDraft(isDraft ? "Draft" : ""); //NBA228
	        requestArray.add(doRequest2);
	        Result reportResult2 = invoke("Reports/DetailedDepositReport", requestArray, true);
	        if (!reportResult2.hasErrors()) {
	            if (debugLogging) {
	                getLogger().logDebug(REPORT_SUCCESS + DEPOSIT_RPT);
	            }
	        } else {
	            getLogger().logError(REPORT_FAILED + DEPOSIT_RPT + " - " + reportResult2.getMessagesList().get(0));
	        }
	
	        DetailedExcludedFromDepositReportRequest doRequest3 = new DetailedExcludedFromDepositReportRequest();
	        doRequest3.setReportName(EXCLUDED_RPT);
	        doRequest3.setDepositTime(depositTimeStamp);
	        doRequest3.setUserID(userId);
	        doRequest3.setLocationID(locationId); //NBA228
	        doRequest3.setDraft(isDraft ? "Draft" : ""); //NBA228
	        requestArray.add(doRequest3);
	        Result reportResult3 = invoke("Reports/DetailedExcludedFromDepositReport", requestArray, true);
	        if (!reportResult3.hasErrors()) {
	            if (debugLogging) {
	                getLogger().logDebug(REPORT_SUCCESS + EXCLUDED_RPT);
	            }
	        } else {
	            getLogger().logError(REPORT_FAILED + EXCLUDED_RPT + " - " + reportResult3.getMessagesList().get(0));
	        }
	
	        InterCompanyJournalReportRequest doRequest4 = new InterCompanyJournalReportRequest();
	        doRequest4.setReportName(INTER_COMPANY_RPT);
	        doRequest4.setDepositTime(depositTimeStamp);
	        doRequest4.setUserID(userId);
	        doRequest4.setLocationID(locationId); //NBA228
	        doRequest4.setDraft(isDraft ? "Draft" : ""); //NBA228
	        requestArray.add(doRequest4);
	        Result reportResult4 = invoke("Reports/InterCompanyJournalReport", requestArray, true);
	        if (!reportResult4.hasErrors()) {
	            if (debugLogging) {
	                getLogger().logDebug(REPORT_SUCCESS + INTER_COMPANY_RPT);
	            }
	        } else {
	            getLogger().logError(REPORT_FAILED + INTER_COMPANY_RPT + " - " + reportResult4.getMessagesList().get(0));
	        }
	        
	        if(i==0){//NBA228
		        DetailedWireTransferReportRequest doRequest5 = new DetailedWireTransferReportRequest();
		        doRequest5.setReportName(WIRE_TRANSFER_RPT);
		        doRequest5.setUserID(userId);
		        doRequest5.setDraft(isDraft ? "Draft" : ""); //NBA228
		        requestArray.add(doRequest5);
		        Result reportResult5 = invoke("Reports/DetailedWireTransferReport", requestArray, true);
		        if (!reportResult5.hasErrors()) {
		            if (debugLogging) {
		                getLogger().logDebug(REPORT_SUCCESS + WIRE_TRANSFER_RPT);
		            }
		        } else {
		            getLogger().logError(REPORT_FAILED + WIRE_TRANSFER_RPT + " - " + reportResult5.getMessagesList().get(0));
		        }
	        } //NBA228
	
	        if(!isDraft){ //NBA228
		        ReconciliationReportRequest doRequest6 = new ReconciliationReportRequest();
		        doRequest6.setReportName(RECONCILIATION_RPT);
		        doRequest6.setSubReportOpenBundles(RECONCILIATION_OPEN_BUNDLES_RPT);
		        doRequest6.setSubReportClosedAndRejected(RECONCILIATION_CLOSED_AND_REJECTED_RPT);       
		        doRequest6.setDepositTime(depositTimeStamp);
		        doRequest6.setCloseTime(depositTimeStamp);
		        doRequest6.setUserID(userId);
		        doRequest6.setLocationID(locationId); //NBA228	        
		        requestArray.add(doRequest6);
		        Result reportResult6 = invoke("Reports/ReconciliationReport", requestArray, true);
		        if (!reportResult6.hasErrors()) {
		            if (debugLogging) {
		                getLogger().logDebug(REPORT_SUCCESS + RECONCILIATION_RPT);
		            }
		        } else {
		            getLogger().logError(REPORT_FAILED + RECONCILIATION_RPT + " - " + reportResult6.getMessagesList().get(0));
		        }
	        } //NBA228

	        if(i==0){ //NBA228
		        DetailedCreditCardPaymentAndRefundReportRequest doRequest7 = new DetailedCreditCardPaymentAndRefundReportRequest();
		        doRequest7.setReportName(CREDIT_CARD_RPT);
		        doRequest7.setSubReportCreditCardPasses(CREDIT_CARD_SUB_PASSED_RPT);
		        doRequest7.setSubReportCreditCardFailed(CREDIT_CARD_SUB_FAILED_RPT);
		        doRequest7.setUserID(userId);
		        doRequest7.setDraft(isDraft ? "Draft" : ""); //NBA228
		        requestArray.add(doRequest7);
		        Result reportResult7 = invoke("Reports/DetailedCreditCardPaymentAndRefundReport", requestArray, true);
		        if (!reportResult7.hasErrors()) {
		            if (debugLogging) {
		                getLogger().logDebug(REPORT_SUCCESS + CREDIT_CARD_RPT);
		            }
		        } else {
		            getLogger().logError(REPORT_FAILED + CREDIT_CARD_RPT + " - " + reportResult7.getMessagesList().get(0));
		        }
	        }//NBA228
	    }//NBA228

        // NBA198 End

        NbaContractsWireTransferData.updateAllReportedInd();
        NbaCreditCardData.updateCCAllReported();
    }
   
    /**
     * This method creates NbaCheckDepositAccountingExtractVOs which will be used to create Deposit accounting 
     * extracts in the database.
     * @param checkData NbaCheckData. 
     * @param contractCheckDataList Array of NbaContractCheckData 
     * @param company String
     * @param bundleId String
     * @param depositExtractVOlist ArrayList used to store the NbaCheckDepositAccountingExtract value objects
     */

    protected void createCheckDepositAccExtractVOs(NbaCheckData checkData, NbaContractCheckData[] contractCheckDataList, String company,
            String bundleID, ArrayList depositExtractVOlist) {
        for (int i = 0; i < contractCheckDataList.length; i++) {
            NbaCheckDepositAccountingExtractVO checkDepositAccountingExtractVO = new NbaCheckDepositAccountingExtractVO();
            checkDepositAccountingExtractVO.setCompanyCode(company);
            checkDepositAccountingExtractVO.setBundleNumber(bundleID);
            checkDepositAccountingExtractVO.setCheckNumber(checkData.getCheckNumber());
            checkDepositAccountingExtractVO.setCheckDate(checkData.getCheckDate());
            checkDepositAccountingExtractVO.setDepositDate(checkData.getDepositTimeStamp());
            checkDepositAccountingExtractVO.setUserID(checkData.getUser());
            checkDepositAccountingExtractVO.setExtractCreate(new java.util.Date());
            checkDepositAccountingExtractVO.setCorrection_Ind(checkData.getCorrection_Ind());
            checkDepositAccountingExtractVO.setCheck_Amt(checkData.getCheckAmount());
            checkDepositAccountingExtractVO.setRevisedAmount(checkData.getRevisedCheckAmt());
            checkDepositAccountingExtractVO.setContractNumber(contractCheckDataList[i].getContractNumber());
            checkDepositAccountingExtractVO.setPrimaryInsured(contractCheckDataList[i].getPrimaryInsuredName());
			checkDepositAccountingExtractVO.setLocationId(checkData.getLocationId()); // NBA228
            depositExtractVOlist.add(checkDepositAccountingExtractVO);
        }
    }

    /**
     * This method creates NbaCheckDepositAccountingExtractVOs which will be used to create Deposit accounting 
     * extracts in the database for corrected bundles.
     * @param nbaDepositTicketData Array of NbaDepositTicketData
     * @param depositExtractVOlist ArrayList used to store the NbaCheckDepositAccountingExtract value objects
     */

    protected void createCorrectedCheckDepositAccExtractVOs(NbaDepositTicketData[] nbaDepositTicketData, ArrayList depositExtractVOlist) {
        for (int i = 0; i < nbaDepositTicketData.length; i++) {
            NbaDepositTicketData depositTicketData = nbaDepositTicketData[i];
            if (depositTicketData.getRevisedTotalAmount() == null) {
                continue;
            }
            NbaCheckDepositAccountingExtractVO checkDepositAccountingExtractVO = new NbaCheckDepositAccountingExtractVO();
            checkDepositAccountingExtractVO.setCompanyCode(depositTicketData.getCompany());
            checkDepositAccountingExtractVO.setBundleNumber(depositTicketData.getBundleID());
            checkDepositAccountingExtractVO.setDepositDate(depositTicketData.getDepositTime());
            checkDepositAccountingExtractVO.setUserID(depositTicketData.getUser());
            checkDepositAccountingExtractVO.setExtractCreate(new java.util.Date());
            checkDepositAccountingExtractVO.setCorrection_Ind(NbaConstants.YES_VALUE);
            checkDepositAccountingExtractVO.setCheck_Amt(depositTicketData.getTotalAmount());
            checkDepositAccountingExtractVO.setRevisedAmount(depositTicketData.getRevisedTotalAmount().toString());
            depositExtractVOlist.add(checkDepositAccountingExtractVO);
        }
    }

    /**
     * Moves the value of REVISED_DEPOSIT_BUNDLE_TOTAL column to TOTAL_AMT column of NBA_DEPOSIT_TICKETS
     * table after the extracts have been created on correction tab and we have received the message 
     * "deposit created successfully"
     * @param depositList ArrayList
     * @throws NbaBaseException
     */

    protected void correctDepositTicketDataList(ArrayList depositList) throws NbaBaseException {
        NbaCashieringTable cashTable = new NbaCashieringTable();
        cashTable.startTransaction();
        for (int i = 0; i < depositList.size(); i++) {
            NbaDepositTicketData ticketData = (NbaDepositTicketData) depositList.get(i);
            ticketData.setTotalAmount(ticketData.getRevisedTotalAmount());
            ticketData.setRevisedTotalAmount(null);
            ticketData.setCorrectionInd(NbaConstants.NO_VALUE);
            cashTable.executeUpdate(ticketData.getUpdateQuery());
        }
        cashTable.commitTransaction();
    }

    /**
     * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
     * @return the logger implementation
     */
    protected NbaLogger getLogger() { //NBA103
        if (logger == null) {
            try {
                logger = NbaLogFactory.getLogger(this.getClass()); //NBA103
            } catch (Exception e) {
                NbaBootLogger.log("NbaServlet could not get a logger from the factory.");
                e.printStackTrace(System.out);
            }
        }
        return logger;
    }
}
