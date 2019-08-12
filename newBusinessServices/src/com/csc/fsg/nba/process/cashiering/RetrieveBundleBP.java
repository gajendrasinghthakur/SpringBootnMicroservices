package com.csc.fsg.nba.process.cashiering;

/* 
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group�.  The use,<BR>
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
import java.util.HashMap;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaCashBundleVO;
import com.csc.fsg.nba.vo.NbaCashCheckVO;

/**
 * Retrieve open bundle details
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA182</td><td>Version 7</td><td>Cashiering Rewrite</td></tr>
 * <tr><td>NBA228</td><td>Version 8</td><td>Cash Management Enhancement</td></tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class RetrieveBundleBP extends NewBusinessAccelBP {

    public static final BigDecimal ZERO = new BigDecimal("0");

    /**
     * Called to retrieve the list of bundle to be shown in the bundle summary table.
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            if (input != null) {
                List companyList = (List) input;
                result.addResult(retrieveBundleSummary(companyList));
            }

        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

    /**
     * Called to retrieve a List of bundles that are open for deposit.
     * @param companyList list of companies selected from company summary table
     * @return bundleSummary the list of open Bundles
     * @throws NbaBaseException
     */
    protected List retrieveBundleSummary(List companyList) throws NbaBaseException {

        List bundleSummary = null;
        List checkSummary = null;
        NbaCashieringTable cashTable = new NbaCashieringTable();
        checkSummary = cashTable.getOpenBundles(companyList);
        bundleSummary = getBundleDetails(checkSummary);
        return bundleSummary;
    }

    /**
     * Determine if all contracts have been applied.
     * @param checks com.csc.fsg.nba.vo.NbaCashCheckVO[]
     * @param currentIndex int
     * @return boolean
     */
    protected boolean allContractsApplied(List checkSummary, int currentIndex) {
        NbaCashCheckVO check = (NbaCashCheckVO) checkSummary.get(currentIndex);
        int size = checkSummary.size();
        for (int j = currentIndex + 1; j < size ; j++) {
            NbaCashCheckVO nextCheck = (NbaCashCheckVO) checkSummary.get(j);
            if (check.getTechnicalKey() == nextCheck.getTechnicalKey()) {
                if (nextCheck.isAppliedInd() && !nextCheck.isRejectedInd()) {
                    continue;
                } else {
                    return (false);
                }
            }
        }
        return (true);
    }

    /**
     * Called to populate the Bundle information based on the checks retrieved
     * @param companyList list of companies selected from company summary table
     * @return bundleSummary the list of open Bundles
     * @throws NbaBaseException
     */
    private List getBundleDetails(List checkSummary) throws NbaBaseException {
        List bundleSummary = new ArrayList(10);
        List keyList = new ArrayList(10);
        int checkCount = 0;
        HashMap companyMap = new HashMap();
        ArrayList checkList = new ArrayList(10);
        int count = checkSummary.size();
        NbaCashieringTable cashTable = new NbaCashieringTable();
        NbaCashCheckVO checkVO = null;
        boolean useAppliedInd = cashTable.getDepositInd().equals(NbaConfigurationConstants.DEPOSIT_APPLY);
        for (int i = 0; i < count; i++) {
            checkVO = (NbaCashCheckVO) checkSummary.get(i);
            String key = checkVO.getCompany() + "-" + checkVO.getBundleID();
            //StringBuffer key = new StringBuffer();
            //key = key.append(checkVO.getCompany()).append("-").append(checkVO.getBundleID());
            if (checkList.contains(new Long(checkVO.getTechnicalKey()))) {
                continue; // yes, next check please
            } else {
                checkList.add(new Long(checkVO.getTechnicalKey())); //add to our check list
            }
            NbaCashBundleVO companyBundle = (NbaCashBundleVO) companyMap.get(key);
            if (companyBundle != null) {
                checkCount = companyBundle.getCheckCount() + 1;
                companyBundle.setCheckCount(checkCount);
                if (companyBundle.isAllRejected()) {
                	// APSL4624 Begin
                	if (NbaConstants.SCAN_STATION_EAPPACH.equals(checkVO.getScanStationId())) {
                		if (!(checkVO.isRejectedInd() || checkVO.isReturnedInd()))
                            companyBundle.setAllRejected(false);
                	} else {
                		if (!(checkVO.isRejectedInd() || checkVO.isRescannedInd() || checkVO.isReturnedInd()))
                            companyBundle.setAllRejected(false);
                	}
                	// APSL4624 End
                }
                if (useAppliedInd) {
                    if ((checkVO.isAppliedInd()) && (allContractsApplied(checkSummary, i))) {
                        companyBundle.setTotalAmount(companyBundle.getTotalAmount().add(
                                (checkVO.getRevisedCheckAmt() != null) ? checkVO.getRevisedCheckAmt() : checkVO.getCheckAmount()));

                    }
                } else if (checkVO.isIncludedInd() && !checkVO.isRejectedInd()) {
                    companyBundle.setTotalAmount(companyBundle.getTotalAmount().add(
                            (checkVO.getRevisedCheckAmt() != null) ? checkVO.getRevisedCheckAmt() : checkVO.getCheckAmount()));

                }
                companyBundle.setBundleCreateTime(checkVO.getBundleCreateTime()); // APSL4513
                companyBundle.setScanStationId(checkVO.getScanStationId()); // APSL4513
            } else {
                companyBundle = new NbaCashBundleVO();
                keyList.add(key);
                // this is the first check of the bundle
                checkCount = 1;

                companyBundle.setCompany(checkVO.getCompany());
                companyBundle.setCompanyName(checkVO.getCompanyName());
                companyBundle.setBundleID(checkVO.getBundleID());
                companyBundle.setScanStation(checkVO.getScanStation());
                companyBundle.setCheckCount(checkCount);
                companyBundle.setCloseBundleInd(checkVO.isCloseBundleInd());// NBA228
                companyBundle.setLockedUser(checkVO.getLockedUser()); //NBA228
                companyBundle.setBundleCreateTime(checkVO.getBundleCreateTime()); // APSL4513
                companyBundle.setScanStationId(checkVO.getScanStationId()); // APSL4513
                companyBundle.setBackendSystem(checkVO.getBackendSystem()); //NBLXA-1908
                
                if (useAppliedInd) {
                    if ((checkVO.isAppliedInd()) && (allContractsApplied(checkSummary, i))) {
                        companyBundle
                                .setTotalAmount((checkVO.getRevisedCheckAmt() != null) ? checkVO.getRevisedCheckAmt() : checkVO.getCheckAmount());

                    } else {
                        companyBundle.setTotalAmount(ZERO);
                    }
                } else {
                    if (checkVO.isIncludedInd() && !checkVO.isRejectedInd()) {
                        companyBundle
                                .setTotalAmount((checkVO.getRevisedCheckAmt() != null) ? checkVO.getRevisedCheckAmt() : checkVO.getCheckAmount());

                    } else {
                        companyBundle.setTotalAmount(ZERO);
                    }
                }
                if (companyBundle.isAllRejected()) {
                	// APSL4624 Begin
                	if (NbaConstants.SCAN_STATION_EAPPACH.equals(checkVO.getScanStationId())) {
                		if (!(checkVO.isRejectedInd() || checkVO.isReturnedInd()))
                            companyBundle.setAllRejected(false);
                	} else {
                		if (!(checkVO.isRejectedInd() || checkVO.isRescannedInd() || checkVO.isReturnedInd()))
                            companyBundle.setAllRejected(false);
                	}
                	// APSL4624 End
                }

                companyMap.put(key, companyBundle); //add bundle

            }

        }
        int size = keyList.size();
        for (int i = 0; i < size; i++) {

            NbaCashBundleVO companyBundle = (NbaCashBundleVO) companyMap.get(keyList.get(i));
            bundleSummary.add(companyBundle);
        }
        return bundleSummary;
    }

}



