package com.csc.fsg.nba.business.process.formal;
/*
 * **************************************************************************<BR>
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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************************<BR>
 */

import java.util.ArrayList;
import java.util.List;

import com.csc.fsg.nba.business.process.NbaAutomatedProcessResult;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;

/**
 * This class executes APFORMAL for a Reg60 case.
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>ALS3091</td><td>AXA Life Phase 1</td><td>General code clean up of NbaProcFormal</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaReg60Proxy extends NbaFormalFromInformalProxy {
	
	//ALS3778 New Method
	//NBLXA-1955 deleted overriden method findDuplicateWork()

	public void doProcess() throws NbaBaseException {
		List informalCases = findMatchingInformalCases(); //ALS4150
		List preSaleCases = findMatchingPreSaleCases();
		getWorkLobs().deletePolicyNumber();
		processMatchingPreSaleCases(preSaleCases, informalCases); //ALS4150
	}

	protected List findMatchingPreSaleCases() throws NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Find Reg60 Case work items");
		}
		NbaSearchVO searchVO = lookupMatchingWork("P_Reg60SearchKey", NbaConstants.PROC_REPL_PROCESSING, PRIMARY_SEARCH,
				NbaOliConstants.OLI_APPORIGIN_REPLACEMENT);
		if (searchVO.getSearchResults().isEmpty()) {
			searchVO = lookupMatchingWork("P_Reg60SearchKey", NbaConstants.PROC_REPL_PROCESSING, SECONDRY_SEARCH,
					NbaOliConstants.OLI_APPORIGIN_REPLACEMENT);
		}
		List preSaleCases = searchVO.getSearchResults();
		return preSaleCases;
	}
	
	//ALS4150 Modified method signature - Added parameter 'informalCases'
	protected void processMatchingPreSaleCases(List preSaleCases, List informalCases) throws NbaBaseException {
		// //APSL3518 CHAUG007 Deleted If Clause
		if (preSaleCases.size() > 1) {
			addComment("Multiple Reg 60 matches found");
		} else if (informalCases.size() > 0) {
			addComment("Matching informal and Reg60 found"); // ALS4150
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getAlternateStatus())); // APSL3518 CHAUG007 //APSL4226
			return;
		} else if (preSaleCases.size() == 1) {
			copyReg60Case((NbaSearchResultVO) preSaleCases.get(0));
			// ALII826 copyComment call moved from here
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			return;
		}
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getFailStatus()));//APSL4226
	}

	//ALS5081 - New method
	protected void copyComments(NbaSearchResultVO resultVO) throws NbaBaseException, NbaVpmsException {
		NbaDst matchingCase = retrieveCaseWithTransactionsAndSources(resultVO.getWorkItemID());
		//Clone matchingCase to retrieve comments - RetrieveComments does not retrieves sources
		NbaDst matchingClone = (NbaDst) matchingCase.clone();
		matchingClone = retrieveWorkItemComments(matchingClone);
		NbaProcFormalUtils.mergeMatchingCaseComments(getWork().getNbaCase(), matchingClone.getNbaCase());
		setLockedMatchingWork(matchingCase);
	}
	
	protected void copyReg60Case(NbaSearchResultVO resultVO) throws NbaBaseException {
		NbaDst preSaleCase = retrieveCaseWithTransactionsAndSources(resultVO.getWorkItemID());
		preSaleCase.setNbaUserVO(getUser());
		NbaTXLife reg60TXLife = doHoldingInquiry(preSaleCase, NbaConstants.READ, null);
		//Begin ALII826
		reInitializeStatusFields(reg60TXLife);
		boolean errorQueue = getPassStatus().equalsIgnoreCase("RGMERGERR") || getPassStatus().equalsIgnoreCase("RGMERGERRW");//ALII826
		if (!errorQueue) {
			NbaProcFormalUtils.mergeMatchingCase(getWork().getNbaCase(), preSaleCase.getNbaCase(), false);
			createXML103(reg60TXLife);
			copyComments(resultVO);
		} else {
			addComment("Reg 60 Pre-sale Contract is not in Goodorder or is not approved");
		}
		setLockedMatchingWork(preSaleCase);
		//End ALII826
	}

	protected void createPolicyInfo(Policy reg60Policy, Policy informalPolicy) throws NbaBaseException {//APSL703 
		reg60Policy.setProductType(getWorkLobs().getProductTypSubtyp());
		reg60Policy.setJurisdiction(getWorkLobs().getAppState());
		reg60Policy.setProductCode(getWorkLobs().getPlan());
		reg60Policy.setRequirementInfo(informalPolicy.getRequirementInfo());
		reg60Policy.setEndorsement(informalPolicy.getEndorsement());
        //Begin APSL703
		OLifEExtension olifeExtn = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
		PolicyExtension policyExtension = olifeExtn.getPolicyExtension();
		reg60Policy.addOLifEExtension(olifeExtn);
		policyExtension.setDistributionChannel(getWorkLobs().getDistChannel());
		policyExtension.setProductSuite(getWorkLobs().getPlanType());
		setExpireMIBCheckReq(informalPolicy.getRequirementInfo());
		//End APSL703
	}

	protected void createCoverage(NbaTXLife informalTXLife, Policy policy) {
		Life life = new Life();
		getNbaOLifEId().setId(life);
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladhpc = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
		policy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(ladhpc);
		ladhpc.setLife(life);
		life.setFaceAmt(informalTXLife.getFaceAmount()); //ALNA447
		life.setCoverage(informalTXLife.getLife().getCoverage());

	}

	protected void createRelations(OLifE olife, ArrayList relations) {
		olife.setRelation(relations);
	}

	protected void createPrimaryInsuredInfo(NbaParty nbaParty) throws NbaBaseException {

	}

	protected void reInitializeStatusFields(NbaTXLife reg60Txlife) throws NbaBaseException {
		//ALS3928 commented Cache logic
//		String businessProcess = NbaUtils.getBusinessProcessId(getUser());
//		statusProvider = NbaProcessStatusCache.getInstance().getStatusProvider(businessProcess);
//		if (statusProvider != null) {
//			return;
//		}

		statusProvider = new NbaProcessStatusProvider(getUser(), getWork(), reg60Txlife);
	}
}
