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
import com.csc.fsg.nba.vo.NbaCashBundleVO;
import com.csc.fsg.nba.vo.NbaCashCheckVO;
import com.csc.fsg.nba.vo.NbaCashieringAdditionalContractVO;
import com.csc.fsg.nba.vo.NbaCheckCorrectionWorkItemVO;

/**
 * Commit Changes made to the checks associated with the Deposit Ticket 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA182</td><td>Version 7</td><td>Cashiering Rewrite</td></tr>
 * <tr><td>SPR1567</td><td>Version 8</td><td>Check Correction Work Item NBCHKCRCTD Attached to Multiple Contracts in Bundle</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class CommitDepositDetailBP extends NewBusinessAccelBP {

	public static final BigDecimal ZERO = new BigDecimal("0");

	/**
	 * Called to commit the changes to the database.
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */

	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			if (input != null) {
				List bundleList = (List) input;
				result.addResult(commitBundle(bundleList));
			}

		} catch (Exception e) {
			addExceptionMessage(result, e);
		}
		return result;
	}

	/**
	 * Called to commit the changes to the database.
	 * @param bundleList list of bundles selected from bundle summary table
	 * @throws NbaBaseException
	 */

	protected List commitBundle(List bundleList) throws NbaBaseException {
		NbaCashBundleVO bundleVO = null;
		NbaCashCheckVO checkVO = null;
		NbaCashieringTable cashTable = new NbaCashieringTable();

		cashTable.startTransaction();

		int bundlesCount = bundleList.size();
		List checkCorrWIList = new ArrayList();
		List checkNumbers = null;//SPR1567
		try {
			for (int i = 0; i < bundlesCount; i++) {
				checkNumbers = new ArrayList();//SPR1567
				bundleVO = new NbaCashBundleVO();
				bundleVO = (NbaCashBundleVO) bundleList.get(i);
				List checkSummary = bundleVO.getChecks();
				int checksCount = checkSummary.size();
				double checkAmt = 0.0;
				double revisedCheckAmt = 0.0;
				double applyAmt = 0.0;
				double revisedApplyAmt = 0.0;
				HashMap checkAmtMap = new HashMap();
				HashMap applyAmtMap = new HashMap();
				String primaryContractNumber = null;//SPR1567
				List secondaryContracts = null;//SPR1567
				boolean correctionRequired = false;//SPR1567

				NbaCheckCorrectionWorkItemVO checkCorrVO = null;
				// look for any changes within the bundle
				for (int j = 0; j < checksCount; j++) {
					checkVO = new NbaCashCheckVO();
					checkVO = (NbaCashCheckVO) checkSummary.get(j);
					long technicalKey = checkVO.getTechnicalKey();
					String techkey = (new Long(technicalKey)).toString();
					applyAmt = checkVO.getOldappliedAmount().doubleValue();//SPR1567
					revisedApplyAmt = checkVO.getAppliedAmount().doubleValue();//SPR1567
					if (checkVO.isPrimaryContractInd()) {
						correctionRequired = false;//SPR1567
						secondaryContracts = new ArrayList();//SPR1567
						primaryContractNumber = checkVO.getContractNumber();//SPR1567
						if (checkVO.isExcludeInd()) {
							NbaCheckData.excludeDepositedCheck(technicalKey, bundleVO.getBundleID());
						}

						checkAmt = checkVO.getOldcheckAmount().doubleValue();

						revisedCheckAmt = checkVO.getCheckAmount().doubleValue();
						if (checkAmt != revisedCheckAmt) {
							checkNumbers.add(techkey);//SPR1567
							checkAmtMap.put(techkey, new Double(revisedCheckAmt));
							//If the check correction value object already exists add this to check correction List 
							//Create a new correction value object for the current check.
							checkCorrVO = new NbaCheckCorrectionWorkItemVO();
							checkCorrVO.setUserVO(bundleVO.getUserVO());
							checkCorrVO.setCheckNumber(checkVO.getCheckNumber());
							checkCorrVO.setOriginalCheckAmount(checkAmt);
							checkCorrVO.setCorrectedCheckAmount(revisedCheckAmt);
							checkCorrWIList.add(checkCorrVO);
						}

					//begin SPR1567
				} else {
					//update the checkCorrVO with additional contract details if checkCorrVO is not null and check Amount is not equal to revised check amount
					// or update checkCorrVO if correctionRequired boolean is true which means checkCorrVO has alread been created for this check when the apply amount has
					// been changed in such a way that check amount has not changed
					if ((checkCorrVO != null && checkAmt != revisedCheckAmt) || correctionRequired) {
						NbaCashieringAdditionalContractVO addlContractVO = new NbaCashieringAdditionalContractVO();
						addlContractVO.setContractNumber(checkVO.getContractNumber());
						addlContractVO.setAppliedAmount(revisedApplyAmt);
						checkCorrVO.addCashieringAdditionalContractVO(addlContractVO);
					} else {
						//keep a list having all additional contracts and updated checkCorrVo with this list if required. 
						NbaCashieringAdditionalContractVO addlContractCashVO = new NbaCashieringAdditionalContractVO();
						addlContractCashVO.setContractNumber(checkVO.getContractNumber());
						addlContractCashVO.setAppliedAmount(revisedApplyAmt);
						secondaryContracts.add(addlContractCashVO);
						//set correctionRequired boolean to true if apply amount has been changed
						if (applyAmt != revisedApplyAmt) {
							correctionRequired = true;
							checkNumbers.add(techkey);//SPR1567
						}
						//if apply amount has been changed , create correction work item if it has not been created already. 
						if (correctionRequired) {
							if (checkCorrVO == null || checkAmt == revisedCheckAmt) {
								checkCorrVO = new NbaCheckCorrectionWorkItemVO();
							}
							//updated the correction work item with details of Primary contract and Seondary Contract
							updateCorrectionWorkItem(secondaryContracts, checkCorrVO, bundleVO, checkVO, primaryContractNumber, checkCorrWIList);
						}

					}

				}
				updateApplyAmountMap(applyAmt,revisedApplyAmt,applyAmtMap,techkey,checkVO);
				//End SPR1567
			}

			if (checkAmtMap.size() > 0 || applyAmtMap.size() > 0) {//SPR1567
				NbaCheckData.updateCheckAmtAndApplyAmount(checkAmtMap, applyAmtMap, bundleVO, checkNumbers);//SPR1567
			}
			}
			//         
		} catch (NbaBaseException nbe) {
			cashTable.rollbackTransaction();
			throw nbe;
		}
		cashTable.commitTransaction();

		return checkCorrWIList;

	}
	
	/**
	 * This method is used to update the Correction work item with Primary Contract details
	 * @param checkCorrVO check Correction work item
	 * @param bundleVO bundle Value Object
	 * @param checkVO  Value Object identifying each instance of check appplied to a contract
	 * @param primaryContractNumber Primary Contract Number to which the check is applied
	 */
	 //SPR1567 New Method
	protected void updateCheckCorrVO(NbaCheckCorrectionWorkItemVO checkCorrVO, NbaCashBundleVO bundleVO, NbaCashCheckVO checkVO, String primaryContractNumber) {
		checkCorrVO.setUserVO(bundleVO.getUserVO());
		checkCorrVO.setCheckNumber(checkVO.getCheckNumber());
		checkCorrVO.setOriginalCheckAmount(checkVO.getOldcheckAmount().doubleValue());
		checkCorrVO.setCorrectedCheckAmount(checkVO.getCheckAmount().doubleValue());
		checkCorrVO.setPrimaryContractNumber(primaryContractNumber);
		checkCorrVO.setAppliedAmount(checkVO.getAppliedAmount().doubleValue());
		checkCorrVO.setCompanyCode(bundleVO.getCompany());
		
	}

	/**
	 * Update the ApplyAmount details for the contracts for different checks.
	 * @param applyAmt original apply amount
	 * @param revisedApplyAmt re4vised apply amount
	 * @param techkey checknumber of the check
	 * @param checkVO Value Object identifying each instance of check appplied to a contract
	 */
	//SPR1567 New Method
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
	
	/**
	 * updated the Correction work item wirh additional  contract and LOB's for Primary Contarct if required
	 * @param secondaryContracts list of Secondary Contracts
	 * @param checkCorrVO Correction Work Item
	 * @param bundleVO Current bundle
	 * @param checkVO Current Check 
	 * @param primaryContractNumber Primary Contract Number to which the check is applied
	 */
	//SPR1567 New Method 
	protected void updateCorrectionWorkItem(List secondaryContracts, NbaCheckCorrectionWorkItemVO checkCorrVO, NbaCashBundleVO bundleVO,
			NbaCashCheckVO checkVO, String primaryContractNumber, List checkCorrWIList) {
		// Check if Corection Work Item has not been updated with the Primary Contract details.if not, then update it
		updateCheckCorrVO(checkCorrVO, bundleVO, checkVO, primaryContractNumber);
		int count = secondaryContracts.size();
		// Update correction work item with Secondary Contract details
		for (int i = 0; i < count; i++) {
			NbaCashieringAdditionalContractVO addlContractVO = (NbaCashieringAdditionalContractVO) secondaryContracts.get(i);
			checkCorrVO.addCashieringAdditionalContractVO(addlContractVO);
		}
		checkCorrWIList.add(checkCorrVO);
	}


}
