package com.csc.fsg.nba.business.process.evaluation;

/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.business.transaction.NbaRequirementUtils;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaXMLDecorator;
import com.csc.fsg.nba.vo.nbaschema.Requirement;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.ImpairmentInfo;
import com.csc.fsg.nba.vo.txlife.ImpairmentMessages;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PredictiveResult;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * Class that will take care of the processing once acMVR model is invoked 
 * from NBCTEVAL and NBRQEVAL process.
 * <p>Implements NbaVpmsModelProcessor 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *   <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACN024</td><td>Version 4</td><td>CTEVAL/RQEVAL restructuring</td></tr>
 * <tr><td>ACN016</td><td>Version 4</td><td>PnR MB2</td></tr>
 * <tr><td>SPR2652</td><td>Version 5</td><td>APCTEVAL process getting error stopped with Run time error occured message</td><tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>AXAL3.7.07</td><td>AXA Life Phase 1</td><td>Auto Underwriting</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */

public class NbaACMVRProcessor extends NbaVpmsModelProcessor {

	protected ArrayList acMVRImpairments = new ArrayList();//ACN024
	protected ArrayList acMVRAccepImpairments = null;	

	/**
	 * Overridden method, calls the model and 
	 * updates the contract with impairments.
	 * @throws NbaBaseException
	 */
	public void execute() throws NbaBaseException {
		boolean isSuccess = false;
		impSrc = NbaConstants.MVR_SRC; //ACN016
		if (performingRequirementsEvaluation()) { //SPR2652
			setPartyID(work); //ACN024
			isSuccess = callMVRModel();
			if (!isSuccess) {
				throw new NbaVpmsException(NbaVpmsException.VPMS_RESULTS_ERROR + NbaVpmsAdaptor.AC_MVR);	//SPR2652
			}
			//Do the Impairments Merging //ACN024
			mergeImpairmentsAndAccep(acMVRImpairments,acMVRAccepImpairments); //ACN016							
		}
	}
	
	/**
	 * This function is used to call the ACMVR model
	 * @return boolean : Returns true if the call is successful
	 * 					 Else returns false 	
	 * @throws NbaBaseException
	 */
	//ACP009 New Method
	public boolean callMVRModel() throws NbaBaseException{
		VpmsModelResult vpmsModelResult = null;
		boolean success = false;
		ArrayList tempImpairmentList = null;
		NbaVpmsAdaptor vpmsProxy = null; //SPR3362 
		ArrayList reqInfoList = new ArrayList();
		RequirementInfo reqInfo = null;
		try {
			Policy policy = null;
			Map deOink = new HashMap();
			NbaOinkDataAccess accessContract =	new NbaOinkDataAccess(txLifeReqResult);
			accessContract.setLobSource(work.getNbaLob());
			accessContract.setAcdbSource(new NbaAcdb(), nbaTxLife);
			vpmsProxy = new NbaVpmsAdaptor(accessContract, NbaVpmsAdaptor.AC_MVR); //SPR3362
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_RESULT);
			//Begin NBLXA-2184 US 269721
			if (txLifeReqResult != null && txLifeReqResult.getTransType() == NbaOliConstants.TC_TYPE_GENREQUIRERESTRN){
				policy = txLifeReqResult.getPolicy();
			}

			if (policy != null) {
				reqInfoList = policy.getRequirementInfo();
				for (int i = 0; i < reqInfoList.size(); i++) {
					reqInfo = (RequirementInfo) reqInfoList.get(i);
					if (reqInfo != null && reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_RISKCLASSIFIER) {
							deOinkLNRCImpairmentFields(txLifeReqResult,deOink);
						}
					}
			}
			//End NBLXA-2184 US 269721 
			deOinkContractFieldsForRequirement(deOink);
			Object [] args = getKeys();
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			oinkRequest.setArgs(args);
			oinkRequest.setRequirementIdFilter(reqId);
					
			vpmsProxy.setANbaOinkRequest(oinkRequest);
			vpmsProxy.setSkipAttributesMap(deOink);
			NbaVpmsResultsData vpmsResultsData;
			vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
			if (vpmsResultsData == null) {
				//SPR3362 code deleted
				throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.AC_MVR); //SPR2652
			} //SPR2652
			String xmlString = (String) vpmsResultsData.getResultsData().get(0);
			if(getLogger().isDebugEnabled()){
				getLogger().logDebug("Results from VPMS Model: "+NbaVpmsAdaptor.AC_MVR);
				getLogger().logDebug(xmlString);
			}
			NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
			vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
			//begin AXAL3.7.07
			List reqList = vpmsModelResult.getRequirementInfo();
			if (reqList != null && reqList.size() > 0) {
				addRequirementWorkItems(reqList);
			}
			//end AXAL3.7.07
			//NBLXA-2184 Start
			if(vpmsModelResult.getImpairmentInfo()!=null && vpmsModelResult.getImpairmentInfo().size()>0) {
				ArrayList tempACMVRImpairments = vpmsModelResult.getImpairmentInfo();
				ImpairmentInfo info  = null;
				for(int i=0;i<tempACMVRImpairments.size();i++) {
					info  = (ImpairmentInfo)tempACMVRImpairments.get(i);
					if(info !=null && info.getImpairmentSource()!=null && info.getImpairmentSource().equalsIgnoreCase(NbaConstants.LEXISNEXIS_RISK_CLASSIFIER)) {
						ArrayList impairmentMsgList = info.getImpairmentMessages();
						ArrayList msgRemoveList = new ArrayList();
						for(int j=0;j<impairmentMsgList.size();j++) {
							ImpairmentMessages msg = (ImpairmentMessages)impairmentMsgList.get(j);
							if(NbaUtils.isBlankOrNull(msg.getImpairmentMessageText())) {
								msgRemoveList.add(msg);
							}
						}
						if(msgRemoveList.size()>0) {
							for(int k=0;k<msgRemoveList.size();k++) {
								ImpairmentMessages impMsg = (ImpairmentMessages)msgRemoveList.get(k);
								info.removeImpairmentMessages(impMsg);
							}
						}
						vpmsModelResult.setImpairmentInfo(tempACMVRImpairments);
					}
				}
			}
			//NBLXA-2184 End
			acMVRImpairments = vpmsModelResult.getImpairmentInfo(); //ACN024
			if(tempImpairmentList!=null && tempImpairmentList.size() != 0){
				//listImpairmentInfo.addAll(tempImpairmentList);  //ACN024
			}
			success = true;			
			// SPR2652 Code Deleted
			//SPR3362 code deleted
		// SPR2652 Code Deleted
		} catch (RemoteException e) {	//SPR2652
			throw new NbaBaseException("NbaVpmsException Exception occured in callMVRModel()",e);
		//begin SPR3362
		} finally {
		    if(vpmsProxy != null){
		        try {
                    vpmsProxy.remove();
                } catch (RemoteException e) {
                    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
                }
		    }
		//end SPR3362
		}
		return success;
	}	

	/**
	 * Add the new Requirement Work Items to AWD 
	 * Uses the List of Requirments to be add to contract.
	 * This method does not lock sibling transactions.  
	 * The contract is locked from RQEVAL preventing other processes or users from adding requirments to contract. 
	 * Unlock the new transaction once it is added.
	 * @throws NbaBaseException
	 */
	 //AXAL3.7.07 new method
	protected void addRequirementWorkItems(List newRequirements) throws NbaBaseException {
		NbaACReqImpWorkupProcessor nbaACReqImpWorkupProcessor = new NbaACReqImpWorkupProcessor();
		nbaACReqImpWorkupProcessor.initialize(nbaTxLife, user, work);
		nbaACReqImpWorkupProcessor.setPartyID(work);
		
		if (newRequirements.size() > 0) {
			Set requirementsId = new HashSet();
			ListIterator li = newRequirements.listIterator();
			boolean requirementsGenerated = false;

			NbaRequirementUtils reqUtils = new NbaRequirementUtils();
			reqUtils.setHoldingInquiry(nbaTxLife);
			reqUtils.setAutoGeneratedInd(true);
			reqUtils.setEmployeeId(user.getUserID());
			reqUtils.setReqPersonCodeAndSeq(work.getNbaLob().getReqPersonCode(), work.getNbaLob().getReqPersonSeq());
			NbaDst parentCase = null;
			while (li.hasNext()) {  //add the requirements
				if (parentCase == null) {
					NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
					retrieveOptionsValueObject.setWorkItem(work.getID(), false);
					retrieveOptionsValueObject.requestCaseAsParent();
					//retrieveOptionsValueObject.setLockParentCase();
					retrieveOptionsValueObject.requestSources();
					retrieveOptionsValueObject.requestTransactionAsSibling();
					//retrieveOptionsValueObject.setLockSiblingTransaction();
					//retrieveOptionsValueObject.setLockWorkItem();
					parentCase = nbaACReqImpWorkupProcessor.retrieveWorkItem(user, retrieveOptionsValueObject);
					List exstingWorkItems = parentCase.getNbaTransactions();
					for (int t = 0; t < exstingWorkItems.size(); t++) {
						NbaTransaction requirement = (NbaTransaction)exstingWorkItems.get(t);
						if (requirement.getID() != null) {
							requirementsId.add(requirement.getID());
						}
					}
				}
				RequirementInfo reqInfo = (RequirementInfo)li.next();

				// 1st Get the ReqStatus to be used to create the WI from APS.
				NbaLob tempLob = new NbaLob();
				tempLob.setReqType((int)reqInfo.getReqCode());
				NbaVpmsResultsData vpmsResultsData = getNbaVpmsResultsDataFromVpmsModelRequirements(NbaVpmsAdaptor.EP_GET_REQUIREMENT_INITIAL_STATUS, tempLob.getReqType());
				
				if (vpmsResultsData.wasSuccessful()) {
					String newValue = (String) vpmsResultsData.getResultsData().get(0);
					tempLob.setReqStatus( newValue); // Requirement Status
				}
				else {
					tempLob.setReqStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_ADD));
				}	
				if (parentCase != null) {
					tempLob.setCaseManagerQueue(parentCase.getNbaLob().getCaseManagerQueue());
				}				// 2nd create the wi and get the lob.Status value
				NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(user, tempLob);
				NbaTransaction nbaTransaction =
					parentCase.addTransaction(provider.getWorkType(), provider.getInitialStatus());
				nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
				NbaLob lob = nbaTransaction.getNbaLob();
				nbaTransaction.getTransaction().setLock("N");
				
				// 3rd assign new values to the new work item lobs
				lob.setReqType((int)reqInfo.getReqCode()); // Req Type
				lob.setAgency(work.getNbaLob().getAgency()); //agency
				lob.setAgentID(work.getNbaLob().getAgentID()); //agent
				lob.setCompany(work.getNbaLob().getCompany());
				lob.setAppState(work.getNbaLob().getAppState());
				lob.setPlan(work.getNbaLob().getPlan());
				lob.setProductTypSubtyp(work.getNbaLob().getProductTypSubtyp());
				lob.setFaceAmount(work.getNbaLob().getFaceAmount());
				lob.setReqStatus(tempLob.getReqStatus());
				if (parentCase != null) {
					lob.setCaseManagerQueue(parentCase.getNbaLob().getCaseManagerQueue());
				}	
				reqUtils.setReqType(lob.getReqType());
				reqUtils.processRequirementWorkItem(parentCase, nbaTransaction);

				requirementsGenerated = true;
				nbaACReqImpWorkupProcessor.addRequirementInfoObject(reqUtils, lob, reqInfo.getRequirementDetails());
			}  //end while
			parentCase = nbaACReqImpWorkupProcessor.updateWork(user, parentCase);
			if (requirementsGenerated) {
				List nbaTransactions = parentCase.getNbaTransactions();
				NbaRequirementUtils nbaReqUtils = new NbaRequirementUtils();
				NbaSource nbaSource = parentCase.getRequirementControlSource();
				NbaTransaction originalTrans = null;   	            
				for (int j = 0; j < nbaTransactions.size(); j++) {
					NbaTransaction nbaTransaction = (NbaTransaction)nbaTransactions.get(j);
					if (nbaTransaction.getTransaction().getWorkType().equals(NbaConstants.A_WT_REQUIREMENT)) {
						if (!requirementsId.contains(nbaTransaction.getID())) {
							NbaLob lob = nbaTransaction.getNbaLob();
							reqUtils.setAutoGeneratedInd(true);
							reqUtils.setEmployeeId(nbaTransaction.getTransaction().getWorkType());
							reqUtils.setReqPersonCodeAndSeq(lob.getReqPersonCode(), lob.getReqPersonSeq());
							reqUtils.setReqType(lob.getReqType());
							reqUtils.addRequirementControlSource(nbaTransaction);
							unlockRequirement(nbaACReqImpWorkupProcessor, nbaTransaction.getID());
						}
					}
				}   // end for
				for (int i = 0; i < nbaTransactions.size(); i++) {
					NbaTransaction nbaTransaction = (NbaTransaction)nbaTransactions.get(i);
					
					if (nbaTransaction.isSelected()) {
						originalTrans = nbaTransaction;
					}
					
					int personCode = nbaTransaction.getNbaLob().getReqPersonCode();
					int personSeq = nbaTransaction.getNbaLob().getReqPersonSeq();
					if(getLogger().isDebugEnabled()) { 
						getLogger().logDebug("About to add master requirement control source");
					} 
					NbaXMLDecorator sourceDecorator = new NbaXMLDecorator(nbaSource.getText());
					if (sourceDecorator.getInsurableParty(personSeq, personCode) != null) {
						ArrayList requirements =
							sourceDecorator.getInsurableParty(personSeq, personCode).getRequirement();
						Requirement requirement = null;
						if (requirements.size() > 0) {
							boolean found = false;
							for (int z = 0, count = requirements.size(); z < count; z++) {
								requirement = (Requirement)requirements.get(z);
								if (nbaTransaction.getID().equals(requirement.getAwdId())
										&& nbaTransaction.getNbaLob().getReqType() == requirement.getCode()) {
									found = true;
									break;
								}
							}
							if (!found) {
								nbaReqUtils.addMasterRequirementControlSource(parentCase, nbaTransaction);
							}
						}
					}
				}  // end for
				nbaTransactions.remove(originalTrans);
				parentCase.setUpdate();
			}
		}
	}
    /**
     * Create and initialize an <code>NbaVpmsResultsData</code> object to find matching work items.
     * @param entryPoint the VP/MS model's entry point
     * @return NbaVpmsResultsData the VP/MS results
     * @throws NbaBaseException
     */
    //     AXAL3.7.07 New Method
    protected NbaVpmsResultsData getNbaVpmsResultsDataFromVpmsModelRequirements(String entryPoint, int newReqType) throws NbaBaseException {
     	NbaVpmsAdaptor vpmsProxy = null; //APSL588
        try {
    		NbaOinkDataAccess accessContract =	new NbaOinkDataAccess(txLifeReqResult);
    		vpmsProxy = new NbaVpmsAdaptor(accessContract, NbaVpmsAdaptor.REQUIREMENTS); //APSL588
    		vpmsProxy.setVpmsEntryPoint(entryPoint);
    		Map deOink = new HashMap();
    		deOink.put(NbaVpmsConstants.A_PROCESS_ID, "DUMMY");
    		deOink.put("A_ReqTypeLOB", new Integer(newReqType).toString());  //reqtype for new requirement
    		deOink.put("A_XMLResponse", "false");
    		vpmsProxy.setSkipAttributesMap(deOink);
    		NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
    		vpmsProxy.remove();
    		return vpmsResultsData;
    	} catch (java.rmi.RemoteException re) {
    		throw new NbaBaseException("Requirements problem", re);
        //Begin APSL588	
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		//End APSL588	
    	}
    }

    // AXAL3.7.07 New Method
    protected void unlockRequirement(NbaACReqImpWorkupProcessor nbaACReqImpWorkupProcessor, String id) throws NbaBaseException {
	    NbaDst workItem = null;
		NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
		retrieveOptionsValueObject.setWorkItem(id, false);
		workItem = nbaACReqImpWorkupProcessor.retrieveWorkItem(user, retrieveOptionsValueObject);
		workItem.setUpdate();
		nbaACReqImpWorkupProcessor.updateWork(user, workItem);
		nbaACReqImpWorkupProcessor.unlockWork(user, workItem);//ALS5738
    }
    
    //NBLXA-2184 US 269721 New Method
    protected void deOinkLNRCImpairmentFields(NbaTXLife txLifeReqResult,Map deOink){
    	int score;
    	NbaParty respInsuredParty = txLifeReqResult.getParty(txLifeReqResult.getPartyId(NbaOliConstants.OLI_REL_INSURED));
		if (respInsuredParty != null) {
			PartyExtension respInsuredPartyExt = NbaUtils.getFirstPartyExtension(respInsuredParty.getParty());
			if (respInsuredPartyExt != null) {
				List respPredictiveResult = respInsuredPartyExt.getPredictiveResult();
				if (respPredictiveResult != null && respPredictiveResult.size() > 0) {
					score = ((PredictiveResult) respPredictiveResult.get(0)).getScore();
						deOink.put("A_LNRCScore", String.valueOf(score));
				}
			}
		}
	    //NBLXA-2184 Start
		Holding holding = txLifeReqResult.getPrimaryHolding();
		List systemMessageList = new ArrayList();
		String msgCodeText = "";
		String messageLNProcessingCode="LNRC not found/Paramed and labs required.";
		List lnrcMsgCodeLst = new ArrayList();
		List lnrcMsgDescLst = new ArrayList();
		
		List rskClassMsgCodeLst = new ArrayList();
		List rskClassMsgDescLst = new ArrayList();
		
		if(holding!=null) {
			systemMessageList = holding.getSystemMessage();
			if(systemMessageList!=null && systemMessageList.size()>0) {
				for(int i=0;i<systemMessageList.size();i++) {
					SystemMessage sysMsg = (SystemMessage) systemMessageList.get(i);
					if(sysMsg.getCarrierAdminSystem().equalsIgnoreCase(NbaConstants.PROVIDER_LEXISNEXIS)) {
						msgCodeText = NbaUtils.getFirstSystemMessageExtension(sysMsg).getMsgCodeText();
						lnrcMsgCodeLst.add(msgCodeText);
						//lnrcMsgDescLst.add(sysMsg.getMessageDescription());
						lnrcMsgDescLst.add(messageLNProcessingCode);//US 317664
					}else if(sysMsg.getCarrierAdminSystem().equalsIgnoreCase(NbaConstants.RSKCLASS)) {
						msgCodeText = NbaUtils.getFirstSystemMessageExtension(sysMsg).getMsgCodeText();
						rskClassMsgCodeLst.add(msgCodeText);
						rskClassMsgDescLst.add(sysMsg.getMessageDescription());
					}
				}	
			}
		}
		if(lnrcMsgCodeLst!=null && lnrcMsgCodeLst.size()>0) {
			deOink.put("A_LNRCErrorCodeList", lnrcMsgCodeLst.toArray(new String[lnrcMsgCodeLst.size()]));
			deOink.put("C_ErrorCodeCount", String.valueOf(lnrcMsgCodeLst.size()));
		}
		if(lnrcMsgDescLst!=null && lnrcMsgDescLst.size()>0) {
			deOink.put("A_LNRCErrorCodeMsgList", lnrcMsgDescLst.toArray(new String[lnrcMsgDescLst.size()]));
			deOink.put("C_ErrorCodeMsgCount", String.valueOf(lnrcMsgDescLst.size()));
		}
		if(rskClassMsgCodeLst!=null && rskClassMsgCodeLst.size()>0) {
			deOink.put("A_LNRCReasonCodeList", rskClassMsgCodeLst.toArray(new String[rskClassMsgCodeLst.size()]));
			deOink.put("C_ReasonCodeCount", String.valueOf(rskClassMsgCodeLst.size()));
		}
		if(rskClassMsgDescLst!=null && rskClassMsgDescLst.size()>0) {
			deOink.put("A_LNRCReasonCodeMsgList", rskClassMsgDescLst.toArray(new String[rskClassMsgDescLst.size()]));
			deOink.put("C_ReasonCodeMsgCount", String.valueOf(rskClassMsgDescLst.size()));
		}
    }
    //NBLXA-2184 End


}
