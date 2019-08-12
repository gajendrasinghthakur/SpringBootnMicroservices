package com.csc.fsg.nba.business.process;

/*
 * ************************************************************** <BR>
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
 *     Copyright (c) 2002-2012 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.csc.fsg.nba.database.NbaSuitabilityDecisionAccessor;
import com.csc.fsg.nba.database.NbaSuitabilityProcessingAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSuitabilityDecisionContract;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;

/**
 * NbaProcSuitabilityDecision 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA297</td><td>Version ?</td><td>Suitability</td></tr>
 * <tr><td>CR1345559</td><td>AXA Life Phase2</td><td>Suitability</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 1201
 * @see NbaAutomatedProcess
 */

public class NbaProcSuitabilityDecision extends NbaProcSuitabilityBase {

	public NbaProcSuitabilityDecision() {
		super();
	}

	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		NbaSuitabilityDecisionContract decisionCandidate = null;

		setUser(user);

		List decisionCandidates = NbaSuitabilityDecisionAccessor.selectContractsForSuitabilityDecisionProcessing();
		int candidateSize = decisionCandidates.size(); //
		for (Iterator iter = decisionCandidates.iterator(); iter.hasNext();){
			try {
			decisionCandidate =  (NbaSuitabilityDecisionContract)iter.next();
			try{
	           	setWork(retrieveCaseWorkItem(user, decisionCandidate.getContractNumber(), decisionCandidate.getCompanyCode()));
	        } catch (NbaLockedException exception) {
	        	getLogger().logError("Locked workitem being bypassed.  ContractNumber:" + decisionCandidate.getContractNumber());
	        	NbaSuitabilityDecisionAccessor.suspend(decisionCandidate);
	        	iter.remove();
	           	continue;
	        } catch (NbaBaseException nbe) {
	        	nbe.printStackTrace();//APSL4558
	        	getLogger().logError("Exception " + nbe.getMessage() + " occurred while processing decision for " + decisionCandidate.getContractNumber());//APSL4558
	        	getLogger().logError("Workitem not found! Record will be delete for ContractNumber: " + decisionCandidate.getContractNumber());
	        	NbaSuitabilityDecisionAccessor.delete(decisionCandidate);
	        	iter.remove();
	           	continue;
	        }
	        //try..catch the holding inquiry so the suitability record can be suspended.
	        try {
	        	setNbaTxLife(doHoldingInquiry());
	        } catch (Exception nbe) {
	        	getLogger().logError("Unable to retrieve contract for: " + decisionCandidate.getContractNumber());
	        	NbaSuitabilityDecisionAccessor.suspend(decisionCandidate); //TODO  should we delete instead?
	        	iter.remove();
	           	continue;
	        }
	        updateApplicationInfoExtension(decisionCandidate.getSuitabilityDecision(), decisionCandidate.getDecisonDate(), decisionCandidate.getDecisionTime());
	        receiptOutstandingSuitability(decisionCandidate.getSuitabilityDecision());  //CR1345559
	        setContractAccess(UPDATE); //CR1345559 
	    	doContractUpdate(); //CR1345559
	        NbaSuitabilityDecisionAccessor.delete(decisionCandidate);
	        if (NbaOliConstants.NBA_SUITABILITYDECISIONSTATUS_PASS == decisionCandidate.getSuitabilityDecision()) { //APSL2893
	        	NbaSuitabilityProcessingAccessor.resetProcessingIndicators(decisionCandidate.getCompanyCode(), decisionCandidate.getContractNumber()); //APSL2893
	        } //APSL2893
	        //unlockCase(); 
			} catch (NbaDataAccessException ndae) {
				getLogger().logException(ndae);
				ndae.forceFatalExceptionType();
				throw ndae;
			} catch (NbaBaseException nbe) {
	        	getLogger().logException(nbe);
	        	getLogger().logError("Unable to process contract: " + decisionCandidate.getContractNumber());
	        } catch (Throwable e) {
				getLogger().logException(e);
				getLogger().logError("Exception occurred while processing Suitability Decision for contract -" + decisionCandidate.getContractNumber());
			} finally {
				if (null != getWork() && getWork().isLocked(user.getUserID())) {
					try {
						unlockCase();
					} catch (NbaBaseException nbe) {
						getLogger().logError("Error unlocking work for contract: " + decisionCandidate.getContractNumber());
					}
				}
			}
		}
		if (candidateSize > 0) {
			return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", "");
		}
		return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", "");
	}	
	
	/*
	 * Retrieves the outstanding Suitability Decision requirement and will receipt/route it
	 * provided the Suiability decision is fass or fail
	 */
	//CR1345559 New Method
	private void receiptOutstandingSuitability(long decision) {
		//Only route the requirement if Suitability Decision is  NOT 'Additional Review Required'
		if (NbaOliConstants.NBA_SUITABILITYDECISIONSTATUS_ADDLRVWREQ == decision) {
			return;
		}
		//
		RequirementInfo reqInfo = getRequirement();
		if (null == reqInfo) {
			return;
		}
		try {
		NbaDst nbaDst = retrieveWorkItem(reqInfo.getRequirementInfoUniqueID());
		reqInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_RECEIVED);
		reqInfo.setReceivedDate(new Date()); //APSL3473
		reqInfo.setActionUpdate();
		RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
		if (null != reqInfoExt && NbaOliConstants.NBA_SUITABILITYDECISIONSTATUS_PASS == decision) {
			reqInfoExt.setReviewedInd(true);
			reqInfoExt.setReviewID(getUser().getUserID());
			reqInfoExt.setReviewDate(new Date());
			reqInfoExt.setReceivedDateTime(NbaUtils.getStringFromDateAndTime(new Date()));//QC20240
			reqInfoExt.setActionUpdate();
		}
		nbaDst.getNbaLob().setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_RECEIVED));
		nbaDst.getNbaLob().setReqReceiptDate(reqInfo.getReceivedDate());//APSL3473
		nbaDst.getNbaLob().setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(reqInfo.getReceivedDate()));//QC20240
		NbaProcessStatusProvider statProvider = getStatusProvider(nbaDst);
		nbaDst.setStatus(statProvider.getPassStatus());		
		nbaDst.setUpdate();
		update(nbaDst);
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		if(nbaDst != null && nbaDst.isSuspended()){
			suspendVO.setTransactionID(nbaDst.getID());
			unsuspendWork(suspendVO);
		}
		unlockWork(nbaDst);
		} catch (Exception e) {
			getLogger().logError(e.getMessage());
			addComment("Unable to receipt Suitability Decision Outstanding requirement", getUser().getUserID());
		}
		
	}

	//CR1345559 New Method
	private NbaProcessStatusProvider getStatusProvider(NbaDst nbaReq) throws NbaBaseException {
	    	return new NbaProcessStatusProvider(getUser(), nbaReq, getNbaTxLife());
	}
	//CR1345559 New Method
	private NbaDst retrieveWorkItem(String rqui) throws NbaBaseException {
		NbaTransaction nbaTrans = null;
		
			List transactions = getWork().getNbaTransactions();
			int count = transactions.size();
			
			for (int i = 0; i < count; i++) {
				nbaTrans = (NbaTransaction) transactions.get(i);
				if (A_WT_REQUIREMENT.equals(nbaTrans.getWorkType()) && rqui.equalsIgnoreCase(nbaTrans.getNbaLob().getReqUniqueID())) {
					break;
				}
				nbaTrans = null;
			}
		
		if (null != nbaTrans) {
			NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
			retrieveOptionsValueObject.setWorkItem(nbaTrans.getID(), false);
			retrieveOptionsValueObject.setLockWorkItem();
			return retrieveWorkItem(getUser(), retrieveOptionsValueObject); //NBA213
		}
		return null;
	}
	/**
	 * Find the RequirementInfo object for Suitability Decision Outstanding
	 * Only return the object if it's not in a received status.
	 */
	//CR1345559 New Method
	private RequirementInfo getRequirement() {
		// TODO Auto-generated method stub
		int reqInfoCount = getNbaTxLife().getPolicy().getRequirementInfoCount();
		List reqInfoList = getNbaTxLife().getPolicy().getRequirementInfo();
		RequirementInfo reqInfo = null;
		for (int x=0;x<reqInfoCount;x++) {
			reqInfo = (RequirementInfo) reqInfoList.get(x);
			if (NbaOliConstants.OLI_REQCODE_SUITOUTSTANDING == reqInfo.getReqCode() && !(NbaOliConstants.OLI_REQSTAT_RECEIVED ==reqInfo.getReqStatus())) {
				return reqInfo;
			}
			reqInfo = null;
		}
		return reqInfo;
	}
}
