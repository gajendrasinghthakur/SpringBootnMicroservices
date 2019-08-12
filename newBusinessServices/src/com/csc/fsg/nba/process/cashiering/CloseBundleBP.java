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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.tableaccess.NbaDepositTicketData;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaCashBundleVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * Closing the Bundle
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

public class CloseBundleBP extends NewBusinessAccelBP {

    public static final BigDecimal ZERO = new BigDecimal("0");

    /**
     * Called to close the selected bundles.
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */

    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            if (input != null) {
                List selectedBundle = (List) input;
                result.addResult(closeBundle(selectedBundle));
                
            }

        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

    /**
     * Called to close the selected Bundles.
     * @param bundleList list of bundles selected from bundle summary table
     * @throws NbaBaseException
     */

    protected List closeBundle(List selectedBundle) throws NbaBaseException {
        NbaCashBundleVO bundleVO = null;
        NbaCashieringTable cashTable = new NbaCashieringTable();        
        List updatedBundles = new ArrayList();
        cashTable.startTransaction();

        Date closeTime = new Date(); // set close time stamp
        boolean allRejected = false;

        try {
            for (int i = 0; i < selectedBundle.size(); i++) {
                bundleVO = (NbaCashBundleVO) selectedBundle.get(i);
                String company = bundleVO.getCompany();
                String bundleID = bundleVO.getBundleID();
                String userID = bundleVO.getUserVO().getUserID();
                //Begin NBA228
                if(bundleVO.isCloseBundleInd())
                {
                	cashTable.closeBundle(company, bundleID);
                	// Begin APSL4513
                	if (NbaConstants.SCAN_STATION_EAPPACH.equals(bundleVO.getScanStationId())) {
                		String newBundleId = cashTable.getBundleID(bundleVO.getScanStationId(), bundleVO.getCompany());                	
                		updateCWABundleId(newBundleId, bundleID, cashTable.selectUpdateBundle(bundleID), bundleVO.getUserVO()); // APSL4590
                		cashTable.updateBundle(newBundleId, bundleID);
                	}
                	// End APSL4513
                	continue;
                }
                //End NBA228
                
                allRejected = cashTable.verifyChecks(bundleVO);
                if (allRejected) {
                    //close the bundle to new checks
                    cashTable.closeBundle(company, bundleID);

                    //set rejected checks as closed	
                    cashTable.closeChecks(company, bundleID, closeTime, bundleVO.getScanStationId()); // APSL4624
                    // create deposit ticket entry
                    NbaDepositTicketData ticketData = new NbaDepositTicketData();
                    ticketData.setCompany(company);
                    ticketData.setBundleID(bundleID);
                    ticketData.setDepositTime(closeTime);
                    ticketData.setDepositUser(userID);
                    ticketData.setTotalAmount(bundleVO.getTotalAmount());
                    ticketData.setCloseTime(closeTime);
                    ticketData.insert();
                }else{
                    bundleVO.setEligibleForClose(false);
                }
                updatedBundles.add(bundleVO);
            }
        } catch (NbaBaseException nbe) {
            cashTable.rollbackTransaction();
            throw nbe;
        }
        cashTable.commitTransaction();
        return updatedBundles;

    }
    
    // APSL4590 New method
    public NbaSearchVO searchCWA(String contractKey) {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(NbaConstants.A_WT_CWA);
		searchVO.setContractNumber(contractKey);
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		AccelResult result = (AccelResult)  callBusinessService("SearchWorkflowBP", searchVO);
		searchVO = (NbaSearchVO) result.getFirst();
		return searchVO;
	}
	
    // APSL4590 New method
	public void updateCWABundleId(String bundleId, String oldBundleId, List contractKeyList, NbaUserVO nbaUserVO) throws NbaBaseException {
		for (int j = 0; j < contractKeyList.size(); j++) {
			String contractKey = (String) contractKeyList.get(j);
			NbaSearchVO searchResultVO = searchCWA(contractKey);
			if (searchResultVO != null && searchResultVO.getSearchResults() != null && !(searchResultVO.getSearchResults().isEmpty())) {
				List searchResultList = searchResultVO.getSearchResults();			
				for (int i = 0; i < searchResultList.size(); i++) {
					NbaSearchResultVO searchResultVo = (NbaSearchResultVO) searchResultList.get(i);
					if (searchResultVo!=null && searchResultVo.getNbaLob() != null 
							&& searchResultVo.getNbaLob().getBundleNumber() != null &&  
							searchResultVo.getNbaLob().getBundleNumber().equals(oldBundleId)) //NBLXA-2525
						{
						NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
						retOpt.setWorkItem(searchResultVo.getWorkItemID(), true);
						NbaDst nbaDst = retrieveWorkItem(nbaUserVO, retOpt);
						nbaDst.getNbaLob().setBundleNumber(bundleId);
						nbaDst.setNbaUserVO(nbaUserVO);
						nbaDst.setUpdate();						
						AccelResult result = (AccelResult) callBusinessService("NbaUpdateWorkBP", nbaDst);
						if (result.hasErrors()) {
							throw new NbaBaseException(NbaBaseException.UPDATE_WORK);
						}
						unlockWork(nbaUserVO, nbaDst);
					}
				}
			}
		}
	}

	// APSL4590 New method
	protected NbaDst retrieveWorkItem(NbaUserVO nbaUserVO, NbaAwdRetrieveOptionsVO retOpt) throws NbaBaseException {
		retOpt.setNbaUserVO(nbaUserVO);
		retOpt.setLockWorkItem();
		AccelResult accelResult = (AccelResult) callBusinessService("NbaRetrieveWorkBP", retOpt);
		NewBusinessAccelBP.processResult(accelResult);
		return (NbaDst) accelResult.getFirst();
	}
	
	// APSL4590 New method
	protected void unlockWork(NbaUserVO nbaUserVO, NbaDst item) throws NbaBaseException {
		item.setNbaUserVO(nbaUserVO);
		AccelResult accelResult = new AccelResult();
		accelResult.merge(callBusinessService("NbaUnlockWorkBP", item));
		NewBusinessAccelBP.processResult(accelResult);
	}
}
