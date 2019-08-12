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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.tableaccess.NbaCheckData;
import com.csc.fsg.nba.tableaccess.NbaDepositTicketData;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDepositSearchVO;
import com.csc.fsg.nba.vo.configuration.Cashiering;



/**
 * Retrieve deposits tickets on the basis of search criteria
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA182</td><td>Version 7</td><td>Cashiering Rewrite</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class SearchDepositsBP extends NewBusinessAccelBP {

    /**
     * Called to retrieve the list of Deposit Tickets .
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            List deposits = new ArrayList(); 
            if (input != null && input instanceof NbaDepositSearchVO) {
                NbaDepositSearchVO searchVO = (NbaDepositSearchVO)input;
                deposits = searchDepositTickets(searchVO);
            } 
            result.addResult(deposits);
        } catch (NbaBaseException e){
            if (NbaBaseException.SEARCH_RESULT_BEYOND_TIME_LIMIT.equalsIgnoreCase(e.getMessage())
                    || NbaBaseException.SEARCH_NO_VALID_RECORD.equalsIgnoreCase(e.getMessage())) {
                result.addResult(e.getMessage());	
            } else {
                addExceptionMessage(result, e);
            }
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

    /**
     * Retrieve Deposit tickets from cashiering database based on input parameters .
     * @param depositDate
     * @param bundleID
     * @param message
     * @return
     * @throws NbaBaseException
     */
    protected List retrieveDepositTickets(Date depositDate, String bundleID, String message) throws NbaBaseException {
        boolean depositFound = false;
        Cashiering config = NbaConfiguration.getInstance().getCashiering();
        int days = config.getDepositCorrectionDays();
        NbaCashieringTable cashTable = new NbaCashieringTable();
        List depositTickets = cashTable.getDepositTicketData(days);
        int ticketCount = depositTickets.size();
        NbaDepositTicketData currentTicket = null;
        if (depositDate != null && bundleID != null) {
            depositFound = false;
            for (int i = 0; i < ticketCount; i++) {
                currentTicket = (NbaDepositTicketData) depositTickets.get(i);
                if (depositDate.equals(currentTicket.getDepositTime()) && currentTicket.getBundleID().equals(bundleID)) {
                    currentTicket.setSelected(true);
                    depositFound = true;
                }
            }
            
        } else if (depositDate != null && bundleID == null) {
            for (int i = 0; i < ticketCount; i++) {
                currentTicket = (NbaDepositTicketData) depositTickets.get(i);
                if (NbaUtils.getStringInUSFormatFromDate(currentTicket.getDepositTime())
                        .equals(NbaUtils.getStringInUSFormatFromDate(depositDate))) {
                    currentTicket.setSelected(true);
                    depositFound = true;
                }
            }
            
        }
        if(depositDate != null){
            Date calculatedDate = NbaUtils.addDaysToDate(depositDate, days);
            int requestDateDifference = NbaUtils.compare(calculatedDate, new Date());
            if(!depositFound){
                throw new NbaBaseException(NbaBaseException.SEARCH_NO_VALID_RECORD);
            } else if (requestDateDifference < 0) {
                throw new NbaBaseException(NbaBaseException.SEARCH_RESULT_BEYOND_TIME_LIMIT);
            }
        }
        return depositTickets;
    }
    
	/**
	 * Search deposit tickets based on entered criteria
	 * @param searchVO Deposit Search Value Object 
	 * @return List of instances of <code>NbaDepositTicketData</code> objects each representing a deposit ticket
	 * @throws NbaBaseException, NbaDataAccessException throw exception on event of known overflow or unknown errors.
	 */
	protected List searchDepositTickets(NbaDepositSearchVO searchVO) throws NbaBaseException, NbaDataAccessException {
	    List depositSearchResult = new ArrayList();
        String inputDepositDate = NbaUtils.getStringInUSFormatFromDate(searchVO.getDepositDate());
        String contractNumber = searchVO.getContractNumber();
        String checkNumber = searchVO.getCheckNumber();
        String lastName = searchVO.getLastName();
        double checkAmount = searchVO.getCheckAmount() != null ? searchVO.getCheckAmount().doubleValue() : 0.0;
        String bundleID = null;
        Date depositDate = null;
        
        if (!contractNumber.equals("") || !checkNumber.equals("") || !lastName.equals("") || checkAmount > 0.0) {
            //if the search criteria is one or more of above, perform DB lookup to get deposit dates and bundle numbers
            List depositTimeAndBundles = new NbaCheckData().getDepositDateAndBundles(contractNumber, lastName, checkNumber, checkAmount);
            int depositCount = 0;
            if (depositTimeAndBundles != null) {
                depositCount = depositTimeAndBundles.size();
            }
            if (depositCount == 0) {
                //if no deposits found, message out - There is no valid record for the search criteria
                throw new NbaBaseException(NbaBaseException.SEARCH_NO_VALID_RECORD);
            } else if (NbaBaseException.SEARCH_RESULT_BEYOND_TIME_LIMIT.equalsIgnoreCase((String)depositTimeAndBundles.get(0))) {
                //if deposits found are out of time range, message out - Matching result is beyond the specified time limit.
                throw new NbaBaseException(NbaBaseException.SEARCH_RESULT_BEYOND_TIME_LIMIT);
            } else {
                //if deposits found and are within time range
                for(int i = 0; i < depositCount; i++){
                    String currentDeposit = (String) depositTimeAndBundles.get(i);
                    int index = currentDeposit.indexOf("|");
                    depositDate = new Date(Long.parseLong(currentDeposit.substring(0, index)));
                    bundleID = currentDeposit.substring(index + 1);
                    if (!inputDepositDate.equals("")) {
                        if (inputDepositDate.equals(NbaUtils.getStringInUSFormatFromDate(depositDate))) {
                            depositSearchResult.addAll(retrieveDepositTickets(depositDate, bundleID, null));
                        } else {
                            depositSearchResult.addAll(retrieveDepositTickets(null, null, NbaBaseException.SEARCH_NO_VALID_RECORD));
                        }
                    } else {
                        depositSearchResult.addAll(retrieveDepositTickets(depositDate, bundleID, null));
                    }
                }
            }
        } else {
            //if search criteria is deposit date, lookup deposits simply based on the deposit date.
            depositSearchResult.addAll(retrieveDepositTickets(NbaUtils.getDateFromStringInUSFormat(inputDepositDate), null, null));
        }
        return depositSearchResult;
    }
}
