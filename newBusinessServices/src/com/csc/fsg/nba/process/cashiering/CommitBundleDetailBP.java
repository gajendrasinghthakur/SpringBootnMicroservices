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
import java.util.HashMap;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.tableaccess.NbaCheckData;
import com.csc.fsg.nba.tableaccess.NbaContractCheckData;
import com.csc.fsg.nba.vo.NbaCashBundleVO;
import com.csc.fsg.nba.vo.NbaCashCheckVO;
import com.csc.fsg.nba.vo.NbaCashieringAdditionalContractVO;
import com.csc.fsg.nba.vo.NbaCheckCorrectionWorkItemVO;

/**
 * Commit Changes made to the checks associated with the Bundle
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

public class CommitBundleDetailBP extends NewBusinessAccelBP {

    public static final BigDecimal ZERO = new BigDecimal("0");

    /**
     * Called to close the selected bundles.
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */

    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            if (input != null) {
                List bundleList = (List) input;
                result.addResult(commitBundle(bundleList));//ALS4266
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
    //ALS4266 Refactored
    protected List commitBundle(List bundleList) throws NbaBaseException {
		NbaCashBundleVO bundleVO = null;
		NbaCashieringTable cashTable = new NbaCashieringTable();
		cashTable.startTransaction();
		int bundlesCount = bundleList.size();
		List checkCorrWIList = new ArrayList();
		try {
			for (int i = 0; i < bundlesCount; i++) {
				bundleVO = (NbaCashBundleVO) bundleList.get(i);
				
				updateIndicators(bundleVO);
				
				createCorrectionWIList(bundleVO, checkCorrWIList);
			}
		} catch (NbaBaseException nbe) {
			cashTable.rollbackTransaction();
			throw nbe;
		}
		cashTable.commitTransaction();
		return checkCorrWIList;
	}
    
    //ALS4266 New Method
    protected void updateIndicators(NbaCashBundleVO bundleVO) throws NbaBaseException {
		List checkSummary = bundleVO.getChecks();
		for (int j = 0; j < checkSummary.size(); j++) {
			NbaCashCheckVO checkVO = (NbaCashCheckVO) checkSummary.get(j);
			if (checkVO.isExcludeInd() && !checkVO.isOldexcludeInd()) {
				NbaCheckData.updateIncludeInd(false, checkVO.getTechnicalKey());
			} else if (!checkVO.isExcludeInd() && checkVO.isOldexcludeInd()) {
				NbaCheckData.updateIncludeInd(true, checkVO.getTechnicalKey());
			}
			// APSL4513 Begin
			if (checkVO.isReturnedInd() != checkVO.isOldReturnedInd() || checkVO.isRescannedInd() != checkVO.isOldRescannedInd()) {
				NbaContractCheckData.updateReturnedRescannedInd(checkVO.isReturnedInd(), checkVO.isRescannedInd(), checkVO.getTechnicalKey());
			}
			// APSL4513 End
			if (checkVO.isAppliedInd() != checkVO.isOldappliedInd()) {
				NbaContractCheckData.updateAppliedInd(checkVO.isAppliedInd(), bundleVO.getBundleID(), bundleVO.getCompany(), checkVO
						.getContractNumber(), checkVO.getCheckAmount().doubleValue(), checkVO.getCheckNumber(), checkVO.getAppliedAmount()
						.doubleValue());
			}
			if (checkVO.isRejectedInd() != checkVO.isOldrejectedInd()) {
				NbaContractCheckData.updateRejectedInd(checkVO.isRejectedInd(), bundleVO.getBundleID(), bundleVO.getCompany(), checkVO
						.getContractNumber(), checkVO.getCheckAmount().doubleValue(), checkVO.getCheckNumber(), checkVO.getAppliedAmount()
						.doubleValue());
			}
		}
	}
    
	/**
	 * Called to commit the changes to the database.
	 * @param bundleList list of bundles selected from bundle summary table
	 * @throws NbaBaseException
	 */
    //ALS4266 New Method
	protected List createCorrectionWIList(NbaCashBundleVO bundleVO, List checkCorrWIList) throws NbaBaseException {
		List checkNumbers = new ArrayList();
		List checkSummary = bundleVO.getChecks();
		HashMap checkAmtMap = new HashMap();
		HashMap applyAmtMap = new HashMap();

		NbaCheckCorrectionWorkItemVO checkCorrVO = null;
		// look for any changes within the bundle
		List secondaryContracts = null;
		NbaCashCheckVO primaryCheckVO = null;
		
		for (int j = 0; j < checkSummary.size(); j++) {
			NbaCashCheckVO checkVO = (NbaCashCheckVO) checkSummary.get(j);
			
			long technicalKey = checkVO.getTechnicalKey();
			String techkey = (new Long(technicalKey)).toString();
			double applyAmt = checkVO.getOldappliedAmount().doubleValue();
			double revisedApplyAmt = checkVO.getAppliedAmount().doubleValue();
			
			if (checkVO.isPrimaryContractInd()) {
				checkCorrVO = null;
				primaryCheckVO = checkVO;
				secondaryContracts = new ArrayList();
				double checkAmt = checkVO.getOldcheckAmount().doubleValue();
				double revisedCheckAmt = checkVO.getCheckAmount().doubleValue();
				if (checkAmt != revisedCheckAmt) {
					checkNumbers.add(techkey);
					checkAmtMap.put(techkey, new Double(revisedCheckAmt));
				}
			} else {
				NbaCashieringAdditionalContractVO addlContractVO = new NbaCashieringAdditionalContractVO();
				addlContractVO.setContractNumber(checkVO.getContractNumber());
				addlContractVO.setAppliedAmount(revisedApplyAmt);
				if (checkCorrVO != null) {
					checkCorrVO.addCashieringAdditionalContractVO(addlContractVO);
				} else {
					secondaryContracts.add(addlContractVO);
				}
			}
			if (applyAmt != revisedApplyAmt) {
				if (checkCorrVO == null) {
					checkCorrVO = new NbaCheckCorrectionWorkItemVO();
					checkCorrVO.setUserVO(bundleVO.getUserVO());
					checkCorrVO.setCheckNumber(checkVO.getCheckNumber());
					checkCorrVO.setOriginalCheckAmount(checkVO.getOldcheckAmount().doubleValue());
					checkCorrVO.setCorrectedCheckAmount(checkVO.getCheckAmount().doubleValue());
					checkCorrVO.setPrimaryContractNumber(primaryCheckVO.getContractNumber());
					checkCorrVO.setCompanyCode(bundleVO.getCompany());
					checkCorrVO.setAppliedAmount(primaryCheckVO.getAppliedAmount().doubleValue());
					checkCorrVO.setAdditionalContracts(secondaryContracts);
					checkCorrWIList.add(checkCorrVO);
				}
				if (!checkNumbers.contains(techkey)) {
					checkNumbers.add(techkey);
				}
				updateApplyAmountMap(applyAmt, revisedApplyAmt, applyAmtMap, techkey, checkVO);
			}
			
		}
		if (checkAmtMap.size() > 0 || applyAmtMap.size() > 0) {
			NbaCheckData.updateCheckAmtAndApplyAmount(checkAmtMap, applyAmtMap, bundleVO, checkNumbers, false);
		}
		return checkCorrWIList;
	}

	/**
	 * Update the ApplyAmount details for the contracts for different checks.
	 * @param applyAmt original apply amount
	 * @param revisedApplyAmt re4vised apply amount
	 * @param techkey checknumber of the check
	 * @param checkVO Value Object identifying each instance of check appplied to a contract
	 */
	//ALS4266 New Method
	protected void updateApplyAmountMap(double applyAmt, double revisedApplyAmt, HashMap applyAmtMap, String techkey, NbaCashCheckVO checkVO) {
		if (applyAmt != revisedApplyAmt) {
			HashMap contractNumberToAmtMap = null;
			//check if the Map applyAmtMap already contains the key for the check .If yes then , fetch the Map having the contract number and the apply amount
			//details corresponding to that check and update contractNumberToAmtMap with the current contract details.
			if (applyAmtMap.containsKey(techkey)) {
				contractNumberToAmtMap = (HashMap) applyAmtMap.get(techkey);
				contractNumberToAmtMap.put(checkVO.getContractNumber(), new Double(revisedApplyAmt));
				applyAmtMap.put(techkey, contractNumberToAmtMap);
			} else {
				contractNumberToAmtMap = new HashMap();
				contractNumberToAmtMap.put(checkVO.getContractNumber(), new Double(revisedApplyAmt));
				applyAmtMap.put(techkey, contractNumberToAmtMap);
			}
		}
	}
    
}
