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

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.business.process.NbaRequirementMerger;
import com.csc.fsg.nba.business.transaction.NbaRequirementUtils;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaXMLDecorator;
import com.csc.fsg.nba.vo.nbaschema.Requirement;
import com.csc.fsg.nba.vo.txlife.ImpairmentInfo;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

/**
 * Class that will take care of the processing once ACReqImpWorkup model is invoked 
 * from NBRQEVAL process.
 * <p>Implements NbaVpmsModelProcessor 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACP007</td><td>Version 4</td><td>Medical Screening</td></tr>
 * <tr><td>SPR2652</td><td>Version 5</td><td>APCTEVAL process getting error stopped with Run time error occured message</td><tr>
 * <tr><td>NBA122</td><td>Version 5</td><td>Underwriter Workbench Rewrite</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>SPR2199</td><td>Version 6</td><td>P&R Requirements Merging Logic Needs to Change to Not Discard some Requirements</td></tr> 
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7<td><tr> 
 * <tr><td>AXAL3.7.07</td><td>AXA Life Phase 1</td><td>Auto Underwriting</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */

public class NbaACReqImpWorkupProcessor extends NbaVpmsModelProcessor {
	protected List contractRequirements = new ArrayList(); // All requirements for the insured on contract  //NBA122 SPR2199
	protected List workupRequirements = new ArrayList(); // All workup requirements - Not merged	// SPR2199
	protected List newWorkupRequirements = new ArrayList(); // All unique & new workup requirements	// SPR2199
	
	/**
	 * Overridden method, calls the model and 
	 * updates the contract with impairments.
	 * @throws NbaBaseException
	 */
	public void execute() throws NbaBaseException {
		// SPR3290 code deleted
		
		if (performingRequirementsEvaluation()) { //SPR2652
			setPartyID(work); //ACN024
			setContractRequirements(nbaTxLife.getRequirementInfos(partyID));	//SPR2199
			getImpairmentWorkupRequirements();
			determineNewRequirements();	// SPR2199
			addRequirementWorkItems(getNewWorkupRequirements());  //AXAL3.7.07
		}
	}

	/**
	 * Add the Requirement Work Items to AWD 
	 * Uses the List of Requirments to be added from Instance variable - newWorkupRequirments
	 * List newRequirements,
	 * @throws NbaBaseException
	 */
	 //SPR2199 changed method visibility
	protected void addRequirementWorkItems(List newRequirements) throws NbaBaseException {
			// SPR3290 code deleted
			NbaOinkDataAccess oinkDataAccess = new NbaOinkDataAccess(work.getNbaLob());
			if (newRequirements.size() > 0) {	//SPR2199, AXAL3.7.07
				Set requirementsId = new HashSet(); //ACN014

				ListIterator li = newRequirements.listIterator();	//SPR2199, AXA3.7.07
				boolean requirementsGenerated = false;
				//NBA093 code deleted
				NbaLob tempLob = work.getNbaLob(); //NBA093
				String tempReqStatus = tempLob.getReqStatus(); //NBA093
				tempLob.setReqStatus(String.valueOf(NbaOliConstants.OLI_REQSTAT_ORDER));
				NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(user, tempLob);
				NbaRequirementUtils reqUtils = new NbaRequirementUtils();
				reqUtils.setHoldingInquiry(nbaTxLife);
				reqUtils.setAutoGeneratedInd(true);
				reqUtils.setEmployeeId(user.getUserID());
				reqUtils.setReqPersonCodeAndSeq(tempLob.getReqPersonCode(), tempLob.getReqPersonSeq());
				tempLob.setReqStatus(tempReqStatus); //NBA093
				NbaDst parentCase = null;
				while (li.hasNext()) {
					if (parentCase == null) {
						NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
						retrieveOptionsValueObject.setWorkItem(work.getID(), false);
						retrieveOptionsValueObject.requestCaseAsParent();
						retrieveOptionsValueObject.setLockParentCase();
						retrieveOptionsValueObject.requestSources();
						retrieveOptionsValueObject.requestTransactionAsSibling();
						retrieveOptionsValueObject.setLockSiblingTransaction();
						retrieveOptionsValueObject.setLockWorkItem();
						parentCase = retrieveWorkItem(user, retrieveOptionsValueObject);	//NBA213

						List exstingWorkItems = parentCase.getNbaTransactions();
						for (int t = 0; t < exstingWorkItems.size(); t++) {
							NbaTransaction requirement = (NbaTransaction)exstingWorkItems.get(t);
							if (requirement.getID() != null) {
								requirementsId.add(requirement.getID());
							}
						}
					}
					
					NbaTransaction nbaTransaction =
						parentCase.addTransaction(provider.getWorkType(), provider.getInitialStatus());
					nbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
					RequirementInfo reqInfo = (RequirementInfo)li.next();
					NbaLob lob = nbaTransaction.getNbaLob();

					lob.setReqType((int)reqInfo.getReqCode()); // Req Type
					lob.setAgency(work.getNbaLob().getAgency()); //agency
					lob.setAgentID(work.getNbaLob().getAgentID()); //agent
					lob.setCompany(work.getNbaLob().getCompany());
					lob.setAppState(work.getNbaLob().getAppState());
					lob.setPlan(work.getNbaLob().getPlan());
					lob.setProductTypSubtyp(work.getNbaLob().getProductTypSubtyp());
					lob.setFaceAmount(work.getNbaLob().getFaceAmount());
					lob.setReqStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_ORDER));
					oinkDataAccess.setLobSource(lob);
					reqUtils.setReqType((int)reqInfo.getReqCode());
					reqUtils.processRequirementWorkItem(parentCase, nbaTransaction);
					//update LOBs and add requirement control source
					requirementsGenerated = true;
					addRequirementInfoObject(reqUtils, lob); //NBA130
				}
				parentCase = updateWork(user, parentCase); //SPR1851	//NBA213

				if (requirementsGenerated) {
					//SPR1851 code deleted			            
					List nbaTransactions = parentCase.getNbaTransactions();
					NbaRequirementUtils nbaReqUtils = new NbaRequirementUtils();
					NbaSource nbaSource = parentCase.getRequirementControlSource();
					NbaTransaction originalTrans = null; //SPR1359	            	            

					for (int j = 0; j < nbaTransactions.size(); j++) {
						NbaTransaction nbaTransaction = (NbaTransaction)nbaTransactions.get(j);
						if (nbaTransaction.getTransaction().getWorkType().equals(NbaConstants.A_WT_REQUIREMENT)) {
							if (!requirementsId.contains(nbaTransaction.getID())) {
								//always true for req determination
								NbaLob lob = nbaTransaction.getNbaLob();
								reqUtils.setAutoGeneratedInd(true);
								reqUtils.setEmployeeId(nbaTransaction.getTransaction().getWorkType());
								reqUtils.setReqPersonCodeAndSeq(lob.getReqPersonCode(), lob.getReqPersonSeq());
								reqUtils.setReqType(lob.getReqType());
								reqUtils.addRequirementControlSource(nbaTransaction);
							}
						}
					} //ACN014 ends		

					for (int i = 0; i < nbaTransactions.size(); i++) {
						NbaTransaction nbaTransaction = (NbaTransaction)nbaTransactions.get(i);
						//SPR1359 begin
						if (nbaTransaction.isSelected()) {
							originalTrans = nbaTransaction;
						}
						//SPR1359 end
						int personCode = nbaTransaction.getNbaLob().getReqPersonCode();
						int personSeq = nbaTransaction.getNbaLob().getReqPersonSeq();
						if(getLogger().isDebugEnabled()) { //SPR3290
						    getLogger().logDebug("About to add master requirement control source");
						} //SPR3290
						NbaXMLDecorator sourceDecorator = new NbaXMLDecorator(nbaSource.getText());
						if (sourceDecorator.getInsurableParty(personSeq, personCode) != null) {
							ArrayList requirements =
								sourceDecorator.getInsurableParty(personSeq, personCode).getRequirement();
							Requirement requirement;
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
					}
					nbaTransactions.remove(originalTrans);
					parentCase.setUpdate();
					parentCase = updateWork(user, parentCase); //SPR1851
					unlockWork(user, parentCase);	//NBA213
				}
			}
	}

	/**
	 * Add the requirementInfo object for the database
     * @param reqUtils
     * @param lob
     * @param requirementDetails
     * @throws NbaBaseException
     */
	//AXAL3.7.07 New Method
    protected void addRequirementInfoObject(NbaRequirementUtils reqUtils, NbaLob lob, String requirementDetails) throws NbaBaseException {
        RequirementInfo aReqInfo = reqUtils.createNewRequirementInfoObject(nbaTxLife, partyID, getUser(), lob); 
        aReqInfo.setRequirementDetails(requirementDetails);
        Policy policy = nbaTxLife.getPolicy();
        policy.addRequirementInfo(aReqInfo);
        policy.setActionUpdate();
        lob.setReqUniqueID(aReqInfo.getRequirementInfoUniqueID());
    }

	/**
	 * Add the requirementInfo object for the database
     * @param reqUtils
     * @param lob
     * @throws NbaBaseException
     */
	//NBA130 New Method
    protected void addRequirementInfoObject(NbaRequirementUtils reqUtils, NbaLob lob) throws NbaBaseException {
        RequirementInfo aReqInfo = reqUtils.createNewRequirementInfoObject(nbaTxLife, partyID, getUser(), lob); 
        Policy policy = nbaTxLife.getPolicy();
        policy.addRequirementInfo(aReqInfo);
        policy.setActionUpdate();
        lob.setReqUniqueID(aReqInfo.getRequirementInfoUniqueID());
    }

    
    /**
	 * Get a List of Impairment Workup Requirements, by looping over all Impairments for the insured party.
	 * This list is set in the instance variable - workupRequirements
	 * @throws NbaBaseException
	 */
	protected void getImpairmentWorkupRequirements() throws NbaBaseException {
		ArrayList impairments = nbaTxLife.getImpairments(partyID);
		int listSize = impairments.size();
		ImpairmentInfo aImpairment = null;
		ArrayList tempRequirments = null;
		for (int i = 0; i < listSize; i++) {
			aImpairment = (ImpairmentInfo)impairments.get(i);
			if (!("true".equalsIgnoreCase(aImpairment.getImpWorkupInd()))) {
				tempRequirments = callReqImpWorkupModel(aImpairment);
				if (tempRequirments != null) {
					getWorkupRequirements().addAll(tempRequirments);	//SPR2199
				}
				// update Indicator to true
				aImpairment.setImpWorkupInd("true");
				aImpairment.setActionUpdate();
			}
		}
	}
	
	//SPR2199 code deleted
	/**
	 * Get a List of Impairment Workup Requirements from VPMS model
	 * @param impairment the ImpairmentInfo object on which workup has to be done
	 * @return ArrayList - List of RequirementInfo objects
	 * @throws NbaBaseException
	 */
	 //SPR2199 changed method visibility
	protected ArrayList callReqImpWorkupModel(ImpairmentInfo impairment) throws NbaBaseException {
	    NbaVpmsAdaptor vpmsProxy = null;	//SPR2199
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(nbaTxLife); //ACN009
			oinkData.setAcdbSource(new NbaAcdb(), nbaTxLife);
			oinkData.setLobSource(work.getNbaLob());
			//begin SPR2199
			if (getLogger().isDebugEnabled()) {  
                getLogger().logDebug("Performing " + NbaVpmsAdaptor.ACREQIMPWORKUP + " entrypoint " + NbaVpmsAdaptor.EP_RESULTXML); 
            } 
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.ACREQIMPWORKUP);
			//end SPR2199
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_RESULTXML);
			Object[] args = getKeys();
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			oinkRequest.setArgs(args);			
			Map deOink = new HashMap();
			//			######## DEOINK
			deOink.put(NbaVpmsConstants.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser())); //SPR2639
			deOinkImpairmentFields(deOink, impairment);
			deOinkMiscFields(deOink);

			vpmsProxy.setANbaOinkRequest(oinkRequest);
			vpmsProxy.setSkipAttributesMap(deOink);
			VpmsComputeResult vcr = vpmsProxy.getResults();
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vcr);
			ArrayList results = vpmsResultsData.getResultsData();
			results = vpmsResultsData.getResultsData();
			//Resulting string will be the zeroth element.
			if (results == null) {
			    //SPR2199 code deleted
				throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.ACREQIMPWORKUP); //SPR2652
			}
			vpmsResult = (String)results.get(0);
			NbaVpmsModelResult vpmsOutput = new NbaVpmsModelResult(vpmsResult);
			ArrayList reqList = vpmsOutput.getVpmsModelResult().getRequirementInfo();
			updateWithVendor(reqList);	//SPR2199			
			//SPR2199 code deleted
			return reqList;
		} catch (RemoteException re) {
			throw new NbaBaseException("Remote Exception occured in callReqImpWorkupModel", re);
		//begin SPR2199
		} finally {
            if (vpmsProxy != null) {
                try {
                    vpmsProxy.remove();
                } catch (Exception e) {
                    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); //SPR3362
                }
            }
            //end SPR2199
            // SPR2652 Code Deleted
        }
	}

    /**
	 * deOink the Impairment fields
	 * @param deOink - deoink Map
	 * @param impairment - ImpairmentInfo objects
	 * @throws NbaBaseException
	 */
	private void deOinkImpairmentFields(Map deOink, ImpairmentInfo impairment) throws NbaBaseException {
		deOink.put("A_ImpairmentDate_INS", convertToDefault(NbaUtils.getStringInISOFormatFromDate(impairment.getImpairmentDate())));
		deOink.put("A_ImpairmentStatus_INS", convertToDefault(String.valueOf(impairment.getImpairmentStatus())));
		deOink.put("A_ImpairmentType_INS", convertToDefault(impairment.getImpairmentType()));
		deOink.put("A_Debit_INS", convertToDefault(String.valueOf(impairment.getDebit())));
		deOink.put("A_Description_INS", convertToDefault(impairment.getDescription()));
		deOink.put("A_ImpWorkupInd_INS", convertToDefault(impairment.getImpWorkupInd()));
	}
	
	private String convertToDefault(String str) {
		if (str == null || str.equalsIgnoreCase("null")) {
			return "";
		}
		else if (str.equalsIgnoreCase("NaN")) {
			return "-1";
		}
		return str;
	}
		
	/**
	 * deOink Miscalleneous fields
	 * @param deOink - deoink Map
	 * @throws NbaBaseException
	 */
	private void deOinkMiscFields(Map deOink) throws NbaBaseException {
		NbaOinkDataAccess oinkDataAccess = new NbaOinkDataAccess(nbaTxLife);
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		oinkRequest.setVariable("AgentLicNum_SAG"); // get the Agent Lic No
		String var =oinkDataAccess.getStringValueFor(oinkRequest);		
		deOink.put("A_AgentLicNum_SAG", var);
					
		oinkRequest.setVariable("DBA_SAG"); // get the Servicing Agency Name
		var = oinkDataAccess.getStringValueFor(oinkRequest);			
		deOink.put("A_DBA_SAG", var);
		
		//deOINK reqCode
		deOink.put("A_ReqCode", String.valueOf(work.getNbaLob().getReqType()));
	}

	/**
	 * Create a list containing the requirements in WorkupRequirements which are new requirements. 
	 * @throws NbaBaseException
	 */	
	//SPR2199 New Method
	protected void determineNewRequirements() throws NbaBaseException {
        setNewWorkupRequirements(new NbaRequirementMerger(getUser(), nbaTxLife).determineNewRequirements(getContractRequirements(), getWorkupRequirements())); //SPR2199 //ALS4366
    }
    /**
     * Returns the contractRequirements.
     * @return Returns the contractRequirements.
     */
	//SPR2199 New Method
    protected List getContractRequirements() {
        return contractRequirements;
    }
    /**
     * Store the RequiremntInfo objects from the Map in an Array
     * @param contractRequirements. The map containing the RequiremntInfo objects
     */
    //SPR2199 New Method
    protected void setContractRequirements(Map requirementsMap) {        
        contractRequirements.addAll(requirementsMap.values());
    }
    /**
     * @return Returns the newWorkupRequirements.
     */
    //SPR2199 New Method
    protected List getNewWorkupRequirements() {
        return newWorkupRequirements;
    }
    /**
     * @return Returns the workupRequirements.
     */
    //SPR2199 New Method
    protected List getWorkupRequirements() {
        return workupRequirements;
    }
    /**
     * @param newWorkupRequirements The newWorkupRequirements to set.
     */
    //SPR2199 New Method
    protected void setNewWorkupRequirements(List newWorkupRequirements) {
        this.newWorkupRequirements = newWorkupRequirements;
    }

	/**
	 * Update a List of RequirementInfo objects with Vendor information.
     * @param reqList
	 * @throws NbaBaseException
     */
    //SPR2199 New Method
    protected void updateWithVendor(ArrayList reqList) throws NbaBaseException {
        int cnt = reqList.size();
        for (int i = 0; i < cnt; i++) {
            updateWithVendor((RequirementInfo) reqList.get(i));
        }
    }  
	/**
	 * Update a RequirementInfo with Vendor information.
     * @param requirementInfo
	 * @throws NbaBaseException
     */
    //SPR2199 New Method
    protected void updateWithVendor(RequirementInfo requirementInfo) throws NbaBaseException {
        NbaLob lob = work.getNbaLob().clone(true);
        lob.setReqType((int) requirementInfo.getReqCode());
        NbaOinkDataAccess oinkDataAccess = new NbaOinkDataAccess(lob);
        NbaVpmsModelResult nbaVpmsModelResult = getDataFromVpmsModelRequirements(NbaVpmsAdaptor.EP_GET_PROVIDER, oinkDataAccess);
        if (nbaVpmsModelResult != null) { 
            String vendor = nbaVpmsModelResult.getVpmsModelResult().getResultAt(0);
            requirementInfo.addOLifEExtension(NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO));  
            RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
            NbaRequirementUtils.addProviderInfo(vendor, reqInfoExt);
        }
    } 
    /**
     * Create and initialize an <code>NbaVpmsResultsData</code> object to find matching work items.
     * @param entryPoint the VP/MS model's entry point
     * @return NbaVpmsResultsData the VP/MS results
     */
    //SPR2199 New Method
    protected NbaVpmsModelResult getDataFromVpmsModelRequirements(String entryPoint, NbaOinkDataAccess oinkDataAccess) throws NbaBaseException {
        NbaVpmsAdaptor vpmsProxy = null;
        try {
            vpmsProxy = new NbaVpmsAdaptor(oinkDataAccess, NbaVpmsAdaptor.REQUIREMENTS);
            vpmsProxy.setVpmsEntryPoint(entryPoint);
            Map deOink = new HashMap();
            deOink.put(NbaVpmsConstants.A_PROCESS_ID, "DUMMY");
            deOink.put("A_XmlResponse", "true");
            vpmsProxy.setSkipAttributesMap(deOink);
            VpmsComputeResult compResult = vpmsProxy.getResults();
            if (compResult.getReturnCode() != 0) {
                return null;
            }
            NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(compResult.getResult());  
            return nbaVpmsModelResult;  
        } catch (java.rmi.RemoteException re) {
            throw new NbaBaseException("Remote Exception occured in " + NbaVpmsAdaptor.REQUIREMENTS + " entrypoint " + entryPoint, re);
        } finally {
            if (vpmsProxy != null) {
                try {
                    vpmsProxy.remove();
                } catch (Exception e) {
                    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); //SPR3362
                }
            }
        }
    }
    //NBA213 New Method
    protected NbaDst retrieveWorkItem(NbaUserVO nbaUserVO, NbaAwdRetrieveOptionsVO retOpt) throws NbaBaseException {
		retOpt.setNbaUserVO(nbaUserVO);
		AccelResult result = (AccelResult)ServiceHandler.invoke("NbaRetrieveWorkBP", ServiceContext.currentContext(), retOpt);
		NewBusinessAccelBP.processResult(result);
        return (NbaDst)result.getFirst();
    }
    //NBA213 New Method
    protected void unlockWork(NbaUserVO nbaUserVO, NbaDst item) throws NbaBaseException{
		item.setNbaUserVO(nbaUserVO);
		AccelResult result = new AccelResult();
		result.merge(ServiceHandler.invoke("NbaUnlockWorkBP",ServiceContext.currentContext(), item));
		NewBusinessAccelBP.processResult(result);
    }
    //NBA213 New Method
    protected NbaDst updateWork(NbaUserVO nbaUserVO, NbaDst item) throws NbaBaseException{
		item.setNbaUserVO(nbaUserVO);
		AccelResult result = (AccelResult)ServiceHandler.invoke("NbaUpdateWorkBP",ServiceContext.currentContext(), item);
		NewBusinessAccelBP.processResult(result);
        return (NbaDst)result.getFirst();
    }

}
