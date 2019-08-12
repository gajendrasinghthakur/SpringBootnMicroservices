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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.tableaccess.NbaDepositTicketData;
import com.csc.fsg.nba.vo.NbaCashBundleVO;


/**
 * Retrieve checks for selected bundles
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

public class RetrieveChecksBP extends NewBusinessAccelBP {

    /**
     * Called to retrieve the list of checks to be shown in the check summary table.
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            if (input != null) {
                List selectedItems = (List) input;
                if(selectedItems.get(0) instanceof NbaCashBundleVO){
                    result.addResult(retrieveCheckSummaryforBundles(selectedItems));
                }else if(selectedItems.get(0) instanceof NbaDepositTicketData){
                    result.addResult(retrieveCheckSummaryforDeposit(selectedItems));
                }
                
            }

        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

    /**
     * @param selectedItems
     * @return
     */
    private List retrieveCheckSummaryforDeposit(List tickets) throws NbaBaseException {
        
        List checkSummary = null;
        List updatedBundles = new ArrayList(10);
        HashMap company = new HashMap();
        HashMap bundlesUsed = new HashMap();
        NbaCashieringTable cashTable = new NbaCashieringTable();
        Map tblCompany = cashTable.loadCompanies();
        Map returnedMap = getSelectedDepositTicketsFromParameter(tickets); 
        Map bundleToDepositTimeMap = (Map) returnedMap.get("bundleToDepositTimeMap");
		NbaCashBundleVO[] depositBundles = (NbaCashBundleVO[]) returnedMap.get("bundlesArray");
		for (int i = 0; i < depositBundles.length; i++){
		    NbaCashBundleVO bundle = depositBundles[i];
			if (company.containsKey(bundle.getCompany())) {
				continue; //skip it, it has already been processed
			} else{
				company.put(bundle.getCompany(), null);
			}
			
			// translate the company code to name
			String companyName = (String) tblCompany.get(bundle.getCompany());
			if (companyName == null) { // if no translation is available
				companyName = bundle.getCompany(); // use company code
			}
			for (int j = i; j < depositBundles.length; j++) {
				NbaCashBundleVO currentBundle = depositBundles[j];
				if (!bundle.getCompany().equals(currentBundle.getCompany())) {
					continue;
				}
				if (bundlesUsed.containsKey(currentBundle.getBundleID())) {
					continue;
				} else{ 
					bundlesUsed.put(currentBundle.getBundleID(),currentBundle.getBundleID());
				}
				
				
				checkSummary =
					cashTable.getDepositedBundleDetails(
						currentBundle.getCompany(),
						currentBundle.getBundleID(),
						(ArrayList) bundleToDepositTimeMap.get(currentBundle.getBundleID()));
				currentBundle.setChecks(checkSummary);
				updatedBundles.add(currentBundle);
			}
			
				
		}
        return updatedBundles;

    }
    
    /**
	 * Parses a tokenized string to retrieve the Deposit Time and Deposit User for bundles
	 * selected on the Deposit Ticket Correction view.
	 * @param bundlesSelected java.lang.String
	 * @return com.csc.fsg.nba.vo.NbaDepositTicketVO[]
	 */
	protected Map getSelectedDepositTicketsFromParameter(List tickets) throws NbaBaseException{
	    
	    int count = tickets.size();
	    
		Map bundleToDepositTimeMap = new HashMap();
		ArrayList depositedTime = null;
		List tblBundles = new ArrayList(10);
		
		
		for(int i=0;i<count;i++){
		    NbaDepositTicketData ticketVO = new NbaDepositTicketData();
		    NbaCashBundleVO bundle = new NbaCashBundleVO();
		    ticketVO = (NbaDepositTicketData)tickets.get(i);
			bundle.setCompany(ticketVO.getCompany());
			bundle.setCompanyName(ticketVO.getCompanyName());
			bundle.setBundleID(ticketVO.getBundleID());
			bundle.setTotalAmount(ticketVO.getTotalAmount());
			//long timeMilliSeconds = (ticketVO.getDepositTime().getTime());
			bundle.setInfo(ticketVO.getUser());
			if (bundleToDepositTimeMap.containsKey(bundle.getBundleID())) {
				depositedTime = (ArrayList) bundleToDepositTimeMap.get(bundle.getBundleID());
				depositedTime.add(ticketVO.getDepositTime());
			} else {
				depositedTime = new ArrayList(10);
				depositedTime.add(ticketVO.getDepositTime());
				bundleToDepositTimeMap.put(bundle.getBundleID(), depositedTime);
			}
			// get Bundle IDs for this deposit ticket.		
			tblBundles.add(bundle);
		}
			
		
		NbaCashBundleVO[] bundlesArray = (NbaCashBundleVO[]) tblBundles.toArray(new NbaCashBundleVO[tblBundles.size()]);
		Map returnMap = new HashMap();
		returnMap.put("bundleToDepositTimeMap", bundleToDepositTimeMap);
		returnMap.put("bundlesArray", bundlesArray);
		return returnMap;
	}

    /**
     * Called to retrieve a List of checks for the selected bundles
     * @param bundleList list of bundle selected from bundle summary table
     * @return checkSummary the list of checks
     * @throws NbaBaseException
     */
    protected List retrieveCheckSummaryforBundles(List bundleList) throws NbaBaseException {

        List updatedBundles = null;

        NbaCashieringTable cashTable = new NbaCashieringTable();
        updatedBundles = cashTable.getChecksforBundles(bundleList);

        return updatedBundles;
    }
    
    

}
