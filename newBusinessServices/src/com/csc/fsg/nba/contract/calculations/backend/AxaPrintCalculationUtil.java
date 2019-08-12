package com.csc.fsg.nba.contract.calculations.backend;

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
 * 
 * *******************************************************************************<BR>
 */
import com.csc.fsg.nba.business.contract.merge.CopyBox;
import com.csc.fsg.nba.business.contract.merge.CopyManager;
import com.csc.fsg.nba.business.transaction.NbaContractChangeUtils;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.CovOptionExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeUSA;
import com.csc.fsg.nba.vo.txlife.LifeUSAExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;

/**
 * NbaLife70CalculatorUtil is the utility class to process merging the calculated values. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>P2AXAL029</td><td>AXA Life Phase 2</td><td>Contract Print</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaPrintCalculationUtil extends NbaContractChangeUtils {

	public AxaPrintCalculationUtil(NbaTXLife firstTXLife, NbaTXLife secondTXLife, String calcType) {
		nbAContract = firstTXLife;
		beContract = secondTXLife;
	}

	public void performCalculationMerge() throws NbaBaseException {
		mergeHolding(nbAContract.getPrimaryHolding(), beContract.getPrimaryHolding());
	}

	public void mergeHolding(Holding firstHolding, Holding secondHolding) throws NbaBaseException {
		super.mergeHolding(firstHolding, secondHolding);
	}
	
	/**
	 * Merges 2 Policy Extnsion  Objects. <BR>
	 * This method merges the details from secondPolicy extension into the reference of firstPolicy. This method then returns firstPolicy's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if secondPolicy extension is null, remove policy extension from firstPolicy
	 * 		if firstPolicy extsnsion is null, set policy extension from secondPolicy
	
	 * </code>
	 * @param firstPolicy a Policy  object
	 * @param secondPolicy a Policy  object 
	 */
	protected void mergePolicyExtension(Policy firstPolicy, Policy secondPolicy) throws NbaBaseException {
		super.mergePolicyExtension(firstPolicy,secondPolicy);
		PolicyExtension firstPolicyExt = NbaUtils.getFirstPolicyExtension(firstPolicy);
		PolicyExtension secondPolicyExt = NbaUtils.getFirstPolicyExtension(secondPolicy);
		if(firstPolicyExt != null && secondPolicyExt != null) {
			firstPolicyExt.setExtractIllusSummaryInfo(secondPolicyExt.getExtractIllusSummaryInfo());
			firstPolicyExt.setExtractScheduleInfo(secondPolicyExt.getExtractScheduleInfo());
			firstPolicyExt.setExtractSummaryProjections(secondPolicyExt.getExtractSummaryProjections());
		}
	}
	
	/**
	 * Merges 2 Life Objects and its children <BR>
	 * This method merges the details from beLife into the reference of nbALife. This method then returns firstLife's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if beLife is null, return nbALife
	 * 		if nbALife is null, return beLife. (CAUTION: Any changes to nbALife member objects references will affect the state of beLife.)
	 * 	  </code>
	 * 
	 * @param nbALife a Life object 
	 * @param beLife a Life object
	 */
	protected void mergeLife(Life nbALife, Life beLife) throws NbaBaseException {
		super.mergeLife(nbALife,beLife);
		if(nbALife != null && beLife != null) {
			LifeUSA firstLifeUSA = nbALife.getLifeUSA();
			LifeUSA secondLifeUSA = beLife.getLifeUSA();
			if(firstLifeUSA != null) {
				mergeLifeUSA(firstLifeUSA, secondLifeUSA);	
			}else {
				nbALife.setLifeUSA(secondLifeUSA);
			}
		}
	}
	
	/**
	 * Merges 2 LifeUSA Objects and its children <BR>
	 * This method merges the details from secondLifeUSA into the reference of finrstLifeUSA. This method then returns firstLife's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * 
	 * @param firstLifeUSA a LifeUSA object 
	 * @param secondLifeUSA a LifeUSA object
	 */
	protected void mergeLifeUSA(LifeUSA firstLifeUSA, LifeUSA secondLifeUSA) throws NbaBaseException {
		if(firstLifeUSA != null && secondLifeUSA != null) {
			CopyBox copyBox = CopyManager.getCopyBox(OLI_LIFEUSA);
			copyBox.copy(firstLifeUSA, secondLifeUSA);
			mergeLifeUSAExtension(firstLifeUSA, secondLifeUSA);
		}
	}
	
	/**
	 * Merges 2 LifeUSAExtension Objects and its children <BR>
	 * This method merges the details from firstLifeUSAExtension into the reference of secondLifeUSAExtension. This method then returns firstLifeExt's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * @param firstLifeUSA a LifeUSA object 
	 * @param secondLifeUSA a LifeUSA object
	 */
	protected void mergeLifeUSAExtension(LifeUSA firstLifeUSA, LifeUSA secondLifeUSA) throws NbaBaseException {
		LifeUSAExtension firstLifeUSAExt = NbaUtils.getFirstLifeUSAExtension(firstLifeUSA);
		LifeUSAExtension secondLifeUSAExt = NbaUtils.getFirstLifeUSAExtension(secondLifeUSA);
		if (firstLifeUSAExt != null && secondLifeUSAExt != null) {
			CopyBox copyBox = CopyManager.getCopyBox(EXTCODE_LIFEUSA);
			copyBox.copy(firstLifeUSAExt, secondLifeUSAExt);	
		}
	}
	
	/**
	 * Merges 2 CoverageExtension Objects and its children <BR>
	 * This method merges the details from secondCoverageExt into the reference of firstCoverageExt. This method then returns nbACoverageExt's updated reference.
	 * 
	 * @param firstCoverage a Coverage object 
	 * @param secondCoverage a Coverage object
	 */
	protected void mergeCoverageExtension(Coverage firstCoverage, Coverage secondCoverage) throws NbaBaseException {
		super.mergeCoverageExtension(firstCoverage, secondCoverage);
		CoverageExtension firstCoverageExt = NbaUtils.getFirstCoverageExtension(firstCoverage);
		CoverageExtension secondCoverageExt = NbaUtils.getFirstCoverageExtension(secondCoverage);
		if(firstCoverageExt != null && secondCoverageExt != null) {
			firstCoverageExt.setExtractScheduleInfo(secondCoverageExt.getExtractScheduleInfo());
			firstCoverageExt.setPremiumDetailInfo(secondCoverageExt.getPremiumDetailInfo());
		}
	}
	
	/**
	 * Merges 2 CovOptionExtension Objects and its children <BR>
	 * This method merges the details from secondCovOptionExt into the reference of firstCovOptionExt. This method then returns nbACovOptionExt's updated reference.
	 * 
	 * @param firstCovOption a CovOption object 
	 * @param secondCovOption a CovOption object
	 */
	protected void mergeCovOptionExtension(CovOption firstCovOption, CovOption secondCovOption) throws NbaBaseException {
		super.mergeCovOptionExtension(firstCovOption, secondCovOption);
		CovOptionExtension firstCovOptionExt = NbaUtils.getFirstCovOptionExtension(firstCovOption);
		CovOptionExtension secondCovOptionExt = NbaUtils.getFirstCovOptionExtension(secondCovOption);
		if(firstCovOptionExt != null && secondCovOptionExt != null) {
			firstCovOptionExt.setExtractScheduleInfo(secondCovOptionExt.getExtractScheduleInfo());
			firstCovOptionExt.setPremiumDetailInfo(secondCovOptionExt.getPremiumDetailInfo());
		}
		
	}
}
