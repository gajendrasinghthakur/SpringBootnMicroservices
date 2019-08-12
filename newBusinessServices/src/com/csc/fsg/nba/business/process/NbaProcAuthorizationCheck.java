package com.csc.fsg.nba.business.process;

/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which are
 * proprietary to CSC Financial Services Group®.  The use, reproduction,
 * distribution or disclosure of this program, in whole or in part, without
 * the express written permission of CSC Financial Services Group is prohibited.
 * This program is also an unpublished work protected under the copyright laws
 * of the United States of America and other countries.
 *
 * If this program becomes published, the following notice shall apply:
 *    Property of Computer Sciences Corporation.<BR>
 *    Confidential. Not for publication.<BR>
 *    Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.accel.valueobject.RetrieveWorkItemsRequest;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaXMLDecorator;
import com.csc.fsg.nba.vo.nbaschema.AutomatedProcess;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.SignatureInfo;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;
import com.csc.fsg.nba.vpms.NbaVPMSHelper;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;



/**
 * NbaProcAuthorizationCheck is the class that processes nbAccelerator work items
 * found on the AWD authorization check queue (NBATHCHK). The process determines what
 * authorization is required for each requirement type and then checks if 
 * it has been received.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td></tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture Changes</td></tr>
 * <tr><td>SPR1261</td><td>Version 3</td><td>ATHCHK process does not add comments when it processes a work item</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>SPR1359</td><td>Version 3</td><td>Automated processes stop poller when unable to lock supplementary work items</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>Requirement Control Source migration Changes</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR2380</td><td>Version 5</td><td>Cleanup</td></tr>
 * <tr><td>NBA119</td><td>Version 5</td><td>Automated Process Suspend</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr> 
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirement/Reinsurance Changes</td></tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>General Code Clean Up Issues for Version 6</td></tr>
 * <tr><td>SPR2933</td><td>Version 6</td><td>Mispelling of "authorization" in AWD manual comment message when a requirement is suspended in NBATHCHK</td></tr>
 * <tr><td>NBA136</td><td>Version 6</td><td>In Tray and Search Rewrite</td></tr>
 * <tr><td>NBA138</td><td>Version 6</td><td>Requirements Override Settings Project</td></tr>
 * <tr><td>NBA212</td><td>Version 7</td><td>Content Services</td></tr>
 * <tr><td>SPR3150</td><td>Version 7</td><td>Authorization Check Not Finding Authorization Form</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR3611</td><td>Version 8</td><td>The Aggregate process is not setting LOBs on a Case correctly</td></tr>
 * <tr><td>ALS4228</td><td>AXA Life Phase 1</td><td>QC #3129 - 3.7.6: MVR Order should occur when Application is received</td></tr>
 * <tr><td>PERF-APSL601</td><td>AXA Life Phase 1</td><td>PERF - View/Update Requirement optimization</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaProcAuthorizationCheck extends NbaAutomatedProcess {
	//NBA008 begin added
	protected NbaOinkDataAccess oinkData = null;
	//NBA119 Code deleted
	protected NbaDst parentCase = null;
	protected NbaSource aSource = null;
	private AuthorizationParty authorizationParty;//APSL4846
	long partyRoleCode = -1L; //NBLXA-1790
	String signatureId = null; //NBLXA-1790
	//NBA008 end added
	//SPR2380 removed logger
/**
 * NbaProcAuthorizationCheck constructor comment.
 */
public NbaProcAuthorizationCheck() {
	super();
}

//NBA103 - removed method

/**
 * This method drives the Authorization Check Process.
 * This method calls the VPMS model to get the Authorization needed for this
 * requirement. If Authorization is not required, the process will change the
 * status of work item to a pass status and will update the work item.
 * If Authorization is required, the process will execute verifySources() to retrieve 
 * the parent case, sibling transactions and associated sources to check 
 * whether the required authorization is present or not. If the required source is 
 * present, it will change the status of work item to a pass status.
 * If authorization is not found, it will call wasSuspended() method, to check
 * whether the source was previously suspended or not. If the source was previously
 * suspended, the status of work item will be changed to the fail status.
 * If not previously suspended, the process will suspend this work item to allow time
 * for the authorization to be received.
 * @param user the user for whom the process is being executed
 * @param work a DST value object for which the process is to occur
 * @return NbaAutomatedProcessResult containing information about
 *         the success or failure of the process
 * @throws NbaBaseException
 */
public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
	//NBA008 code deleted
	//NBA008 begin added
	// NBA027 - logging code deleted
	if (!initialize(user, work)) {
		return getResult(); // NBA050
	}
	List formNumber = null; //NBA119	//ALS4490
	try {
		oinkData = new NbaOinkDataAccess(getWorkLobs());  //SPR2992
		oinkData.setContractSource(getNbaTxLife());	//AXAL3.7.06
		initializeAuthParty();//APSL4846
		boolean updateContract = false; //NBA130

		//Update RQFF(Requirement Follow-Up Frequency) LOB
		NbaVpmsResultsData data = null;
		//begin NBA138
		if(!hasFollowupFrequency()){
			data = getDataFromVpms(NbaVpmsAdaptor.EP_GET_FOLLOWUP_DAYS); //NBA130
			if (data.getResultsData() != null && data.getResultsData().size() > 0) {
				//BEGIN NBA130
				updateRequirementInfoExtension(data);
				updateContract = true;
				//END NBA130
			}
		}
		//end NBA138
		//begin NBA119
		data = getDataFromVpms(NbaVpmsAdaptor.EP_GET_AUTH_SOURCES);
		ArrayList resultDataSources = null;
		if (data.wasSuccessful() && data.getResultsData().size() > 0) {
			resultDataSources = data.getResultsData(); //resultDataSources contains source
			data = getDataFromVpms(NbaVpmsAdaptor.EP_GET_AUTH_FORMNUMBER);
			if (data.wasSuccessful() && data.getResultsData().size() > 0) {
				formNumber = data.getResultsData(); //form Number contains form Number.	//ALS4490
			}

		}
		
		//begin ALS4490
		List emptyResults = new ArrayList();
		emptyResults.add("");
		formNumber.removeAll(emptyResults);
		//end ALS4490
		//Start NBLXA-1504
		NbaLob lob = getWorkLobs();
		setParentCase(retrieveParentCaseWithSources());
		if (!NbaUtils.isBlankOrNull(getParentCase().getNbaLob().getCaseManagerQueue())) {
			lob.setCaseManagerQueue(getParentCase().getNbaLob().getCaseManagerQueue());
		}
		boolean apsFlage=true;
		if (lob != null && lob.getReqType() == NbaOliConstants.OLI_REQCODE_PHYSSTMT && ! NbaUtils.isBlankOrNull(lob.getReqStatus())
				&& Integer.valueOf(lob.getReqStatus()) == NbaOliConstants.OLI_REQSTAT_ADD) {
			apsFlage = false;
		}
		//End NBLXA-1504
		
		if (apsFlage && //NBLXA-1504 
				null != resultDataSources && resultDataSources.size() > 0 && null != formNumber && formNumber.size() > 0) { //NBA119 this means authorization source is required //ALS4490
			//end NBA119
			//retrieve the parent case and work items sources if authorization source is required
			//setParentCase(retrieveParentCaseWithSources());  //NBA136
			resetLastNonReviewedRequirementReceived();  //SPR3611
			//get original workitem with sources			
			getWork().getTransaction().getSourceChildren().addAll(((NbaTransaction) getParentCase().getNbaCase().getNbaTransactions().get(0)).getSources());  //NBA136 NBA208-32 SPR3290
			//NBA119 Code deleted.
			//begin NBA119
			//APSL5007
				Date signatureDate = null;
				ApplicationInfo applicationInfo = getNbaTxLife().getPolicy().getApplicationInfo();
				int signatureInfoCount = applicationInfo.getSignatureInfoCount();
				for (int i = 0; i < signatureInfoCount; i++) {
					SignatureInfo signInfo = applicationInfo.getSignatureInfoAt(i);
					if (signInfo != null
							&& (signInfo.hasSignatureRoleCode() && signInfo.getSignatureRoleCode() == NbaOliConstants.OLI_PARTICROLE_PRIMARY)
							&& signInfo.hasSignatureDate()) {
						signatureDate = signInfo.getSignatureDate();
						break;
					}
				}
				// Start NBLXA-1790
				boolean suspendReq = false;
				if (! NbaVPMSHelper.isTConvFormNumber(getNbaTxLife()) && ! NbaVPMSHelper.isGIFormNumber(getNbaTxLife())) {
					if (lob != null && lob.getReqType() == NbaOliConstants.OLI_REQCODE_PHYSSTMT
							|| lob.getReqType() == NbaOliConstants.OLI_REQCODE_PPR || lob.getReqType() == NbaOliConstants.OLI_REQCODE_DVR
							|| lob.getReqType() == NbaOliConstants.OLI_REQCODE_INSPRPTQUES
							|| lob.getReqType() == NbaOliConstants.OLI_REQCODE_INSPRPTQUES) {
						suspendReq = isHipaaSigExpired() || isHipaaCVPresent();
					}
				}
				// End NBLXA-1790
			if (!isAuthSourcePresent(formNumber, resultDataSources) || (signatureDate == null && getNbaTxLife().isInformalApp() && getWorkLobs().getReqType() == NbaOliConstants.OLI_REQCODE_MIBCHECK) || suspendReq) { //NBLXA-1790 //source not present on either parent case or sibling transactions //ACN014 NBA212 SPR3150 
					if (!isAllowableSuspendDaysOver(lob)) {
						data = getDataFromVpms(NbaVpmsAdaptor.EP_GET_SUSPEND_ACTIVATE_DATE);
						//end NBA119
						if (data.getResultsData() != null && data.getResultsData().size() > 0){
							String activateDate = (String) data.getResultsData().get(0); //NBA119
							NbaSuspendVO suspendVO = new NbaSuspendVO();
							suspendVO.setTransactionID(getWork().getID());
							// NBA119 code deleted
						    suspendVO.setActivationDate(NbaUtils.getDateFromStringInAWDFormat(activateDate)); //NBA119                   
						    //NBA119 code deleted
						    //Begin NBA130
                            if (updateContract) {
                                handleHostResponse(doContractUpdate());
                            }
                            if (getResult() == null) {
                                addComment("Suspended awaiting authorization"); //SPR1261 SPR2933
	                            //activation of work item.
	                            updateWork(getUser(), getWork());  //NBA213
	                            suspendWork(getUser(), suspendVO);  //NBA213
	                            unlockWork(getUser(), getWork());  //NBA213
	                            setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "Suspended"));
                            } else {
                                updateWork(getUser(), getWork());  //NBA213
                                unlockWork(getUser(), getWork());  //NBA213
                            }
                            //End NBA130 
                            return getResult();
						}
					}
					lob.deleteAppHoldSuspDate(); //NBA119
					//APSL4142 code deleted
					throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_FUNC_AUTH);//APSL4142
			} //APSL4142
			aSource = getRecentReceivedHippa(getParentCase().getNbaSources()); // NBLXA-2603[NBLXa-2626]			
			//if authorization source is present.update form number lob
			// NBA119, SPR2992
			//AXAL3165 code deleted
			//Authorization source will be attached with work item
			getWork().addNbaSource(aSource);
			// BEGIN NBA130
				try {
				    requirementInfo.getAttachment().addAll(createAttachments(aSource, NbaOliConstants.OLI_ATTACH_ACORD751, false));	//NBA212
				    updateContract = true;
				    getWork().setUpdate();
				} catch (NbaBaseException nnse) {	//NBA212
					addComment("Unable to retrieve image for source " + aSource.getSource().getDocument());//NBA208-32
				    setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Image retrieval failed", getAwdErrorStatus()));
				}
				// END NBA130
				//APSL4142 code deleted
		} else if(isMVRDriverLicenseAndStateMissing(lob)){ //Begin NBLXA-2072
			//Suspend MVR for missing Driving License info
			if (!isAllowableSuspendDaysOver(lob)) {
				data = getDataFromVpms(NbaVpmsAdaptor.EP_GET_SUSPEND_ACTIVATE_DATE);				
				if (data.getResultsData() != null && data.getResultsData().size() > 0){
					String activateDate = (String) data.getResultsData().get(0); 
					NbaSuspendVO suspendVO = new NbaSuspendVO();
					suspendVO.setTransactionID(getWork().getID());
				    suspendVO.setActivationDate(NbaUtils.getDateFromStringInAWDFormat(activateDate));                   
                    if (updateContract) {
                        handleHostResponse(doContractUpdate());
                    }
                    if (getResult() == null) {
                        addComment("Suspended due to missing driver license Number and State"); 
                        //activation of work item.
                        updateWork(getUser(), getWork());  
                        suspendWork(getUser(), suspendVO);  
                        unlockWork(getUser(), getWork());  
                        setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "Suspended"));
                    } else {
                        updateWork(getUser(), getWork());  
                        unlockWork(getUser(), getWork());  
                    }                    
                    return getResult();
				}
			}
			lob.deleteAppHoldSuspDate();			
			throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_FUNC_AUTH);//APSL4142	
			//End NBLXA-2072
			} else if (isResidenceAddressMissing(lob)) { //NBLXA-2184 | US 302047 - Start
				//Suspend LNRC for missing Address for Insured
				if (!isAllowableSuspendDaysOver(lob)) {
					data = getDataFromVpms(NbaVpmsAdaptor.EP_GET_SUSPEND_ACTIVATE_DATE);
					if (data.getResultsData() != null && data.getResultsData().size() > 0) {
						String activateDate = (String) data.getResultsData().get(0);
						NbaSuspendVO suspendVO = new NbaSuspendVO();
						suspendVO.setTransactionID(getWork().getID());
						suspendVO.setActivationDate(NbaUtils.getDateFromStringInAWDFormat(activateDate));
						if (updateContract) {
							handleHostResponse(doContractUpdate());
						}
						if (getResult() == null) {
							addComment("LNRC suspended awaiting missing residence address for insured");
							// activation of work item.
							updateWork(getUser(), getWork());
							suspendWork(getUser(), suspendVO);
							unlockWork(getUser(), getWork());
							setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "Suspended"));
						} else {
							updateWork(getUser(), getWork());
							unlockWork(getUser(), getWork());
						}
						return getResult();
					}
				}

				lob.deleteAppHoldSuspDate();
				throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_FUNC_AUTH);
				 //NBLXA-2184 | US 302047 - End
		} else {	//SPR3611
			setParentCase(retrieveParentCase());	//SPR3611
			resetLastNonReviewedRequirementReceived();  //SPR3611
		}
		

		resetSignificantRequirementReceivedLOB(); //APSL1526
		updateRequirementInfo(getWork().getNbaTransaction(),requirementInfo); //PERF-APSL601
		updateContract = true; //PERF-APSL601
        //Begin NBA130
        if (updateContract) {
            handleHostResponse(doContractUpdate());
        }
        //End NBA130
        
		if (getResult() == null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Authorization Done.", getPassStatus()));
			//NBA208-32 code deleted
		}
		//SPR3611 code deleted
		//NBA130 code deleted
		changeStatus(getResult().getStatus()); //NBA208-32
		doUpdateWorkItem();
		return getResult();
	} catch (java.rmi.RemoteException re) {
		throw new NbaBaseException("Authorization Check Problems: " + re, re); //SPR1359
	}
	//NBA008 end added
}
/**
 * //APSL4846 New Method
 * Initialize applies to party details for current WI into AuthorizationParty object
 */
	
protected void initializeAuthParty() {
	NbaLob workLobs = getWorkLobs();
	String uniqueID = workLobs.getReqUniqueID();
	RequirementInfo reqInfo = getNbaTxLife().getRequirementInfo(uniqueID);
	if(reqInfo!=null){
		String partyID = reqInfo.getAppliesToPartyID();
		NbaParty reqParty = getNbaTxLife().getParty(partyID);
		this.authorizationParty = new AuthorizationParty(reqParty.getFirstName(),reqParty.getLastName()); // APSL5165 removed SSN check
	}
}

/**
 * Updates the RequirementInfoExtension for the RequirementInfo object
 * @param data Contains the follow up frequency for the requirement
 */
private void updateRequirementInfoExtension(NbaVpmsResultsData data) {
    RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
    if( reqInfoExt == null ) {
    	OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO); //SPR2992
    	reqInfoExt = oliExt.getRequirementInfoExtension();
    	reqInfoExt.setActionAdd();
    	getRequirementInfo().addOLifEExtension(oliExt);
    	getRequirementInfo().setActionUpdate();
    } else {
    	reqInfoExt.setActionUpdate();
    }
    reqInfoExt.setFollowUpFreq(Integer.parseInt((String) data.getResultsData().get(0)));
    reqInfoExt.setWorkitemID(getWork().getID());
}

/**
 * This method calls a VPMS model which responds with the
 * authorization needed for this requirement.
 * @return NbaVpmsResultsData containing the source and form number, separated by dash(-).
 * @param entryPoint is the entryPoint for VPMS model.
 * @throws NbaBaseException
 */
//NBA008 New Method
public NbaVpmsResultsData getDataFromVpms(String entryPoint) throws com.csc.fsg.nba.exception.NbaBaseException {
    NbaVpmsAdaptor vpmsProxy = null; //SPR3362
    try {    	
    	getLogger().logDebug("Starting Retrieval of data from VPMS model"); //NBA044
		//second parameter is the name of the model to be executed.
		vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaConfiguration.REQUIREMENTS); //SPR3362
		Map deOinkMap = new HashMap();
		deOinkMap.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser())); //SPR2639
		//Start NBLXA-1790
		long signedState = getSignedState();
		if (!NbaUtils.isNull(signedState)) {
			deOinkMap.put(NbaVpmsAdaptor.A_SIGNEDSTATE, String.valueOf(getSignedState())); 
		}
		//End NBLXA-1790
		vpmsProxy.setSkipAttributesMap(deOinkMap);
		vpmsProxy.setVpmsEntryPoint(entryPoint);
		NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
		//SPR3362 code deleted
		return data;
	} catch (java.rmi.RemoteException re) {
		throw new NbaBaseException("Authorization Check Problem", re);
		//begin SPR3362
	} finally {
		try {
		    if (vpmsProxy != null) {
				vpmsProxy.remove();					
			}
		} catch (Exception e) {
			getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); 
		}
	}
	//end SPR3362
}

	// NBLXA-1790 new method
 	public long getSignedState() throws NbaBaseException {
		SignatureInfo signInfo = null;
		long contractSignedState = -1L;
		if (nbaTxLife != null) {
			int personCode = getWorkLobs().getReqPersonCode();
			Relation relation = NbaUtils.getRelation(getNbaTxLife().getOLifE(), personCode);
			if (relation != null) {
				if (relation.getRelationRoleCode() == NbaOliConstants.OLI_PARTICROLE_32) {
					signInfo = NbaUtils.findSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_PRIMARY, NbaOliConstants.OLI_SIGTYPE_APPSIG,
							nbaTxLife);
					if (signInfo != null) {
						contractSignedState = signInfo.getSignatureState();
					}
				} else if (relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_JOINTINSURED) {
					signInfo = NbaUtils.findSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_JOINT, NbaOliConstants.OLI_SIGTYPE_APPSIG,
							nbaTxLife);
					if (signInfo != null) {
						contractSignedState = signInfo.getSignatureState();
					}
				}
			}
		}
		return contractSignedState;
	}
		//End NBLXA-1790
//NBA212 code deleted
/**
 * This method checks whether this work item was previously suspended or not.
 * @return boolean true indicates that this work item was previously suspended.
 *         false indicates that the work item was not previously suspended.
 * @throws NbaBaseException
 */
//NBA008 New Method
public boolean wasSuspended() throws com.csc.fsg.nba.exception.NbaBaseException {
	NbaXMLDecorator xmlDecorator = new NbaXMLDecorator(getWork().getRequirementControlSource().getText());
	AutomatedProcess process = xmlDecorator.getAutomatedProcess(NbaUtils.getBusinessProcessId(getUser())); //SPR2639
	if (process != null && process.hasSuspendDate()) {
		return true;
	}
	return false;
}

/**
 * Checks if the requirement authorization source is present on either case or child transactions.
 * @param formNumber the form number required on the authorization source
 * @param sourceTypeList the list of sources types that can be used as authorization.
 * @return true if requirement authorization source is found else return false.
 */
//SPR3150 New Method
protected boolean isAuthSourcePresent(List formNumber, List sourceTypeList) throws NbaBaseException {	//ALS4490
    boolean isSourcePresent = isAuthSourcePresent(getParentCase().getNbaSources(), formNumber, sourceTypeList);
    if (!isSourcePresent) {
        List trans = getParentCase().getNbaTransactions();
        int size = trans.size();
        for (int i = 0; i < size; i++) {
           if (isAuthSourcePresent(((NbaTransaction) trans.get(i)).getNbaSources(), formNumber, sourceTypeList)) {
                isSourcePresent = true;
                break;
            }
        }
    }
    return isSourcePresent;
}

/**
 * Checks if requirement authorization source is present in the source list. 
 * @param sources the workitem source list
 * @param formNumber the form number required on the authorization source
 * @param sourceTypeList the list of sources types that can be used as authorization.
 * @return true if authorization is found in the source list else return false.
 */
    //NBA119 New Method
    protected boolean isAuthSourcePresent(List sources, List formNumber, List sourceTypeList) throws NbaBaseException { //ALS4490
        int sourceTypeCount = sourceTypeList.size();
        int sourceCount = sources.size();
        NbaLob workLob = getWork().getNbaLob();//ALII1388
        for (int i = 0; i < sourceCount; i++) {
            aSource = (NbaSource) sources.get(i);
            for (int j = 0; j < sourceTypeCount; j++) {
                NbaLob lob = aSource.getNbaLob();
                String caseSourceType = aSource.getSource().getSourceType();
                if (lob.getReqType() == 11 || lob.getReqType() == 348 || workLob.getReqType() == 11 || workLob.getReqType() == 348
							|| lob.getReqType() == NbaOliConstants.OLI_REQCODE_DVR || workLob.getReqType() == NbaOliConstants.OLI_REQCODE_DVR
							|| lob.getReqType() == NbaOliConstants.OLI_REQCODE_INSPRPTQUES || workLob.getReqType() == NbaOliConstants.OLI_REQCODE_INSPRPTQUES) {  // QC7486 ALII1388 QC9787 ALII2088
						if (caseSourceType.equals(sourceTypeList.get(j)) // SPR3290
								&& ((null != lob.getFormNumber() && formNumber.contains(lob.getFormNumber())) //ALS4490
								&& checkAuthorizationOverride(caseSourceType) && isSourceForSameParty(lob))) { //ALS4228, APSL1358,APSL4846
							return true;
						}
					} else {
						if (caseSourceType.equals(sourceTypeList.get(j)) // QC7486
								&& ((null != lob.getFormNumber() && formNumber.contains(lob.getFormNumber())) 
								|| checkAuthorizationOverride(caseSourceType))) { 
							return true;
						}
					}
	            }
        }
        return false;
    }
    
    /**
     * APSL4846 New Method
     * @param sourceLob
     * @return
     */
	protected boolean isSourceForSameParty(NbaLob sourceLob) {
		AuthorizationParty sourceAuthorizationParty = new AuthorizationParty(sourceLob.getFirstName(),sourceLob.getLastName()); // APSL5165 removed SSN check
		return authorizationParty.equals(sourceAuthorizationParty);
	}

/**
	 * This method checks whether this work item has reached the maximum allowable suspend days limit.
	 * @param lob the NbaLob object 
	 * @return true if max suspend days limit is over else return false. 
	 */
	//NBA119 New Method
	public boolean isAllowableSuspendDaysOver(NbaLob lob) throws NbaBaseException {
		Date appHoldSusDate = lob.getAppHoldSuspDate();
		if (null == appHoldSusDate) {
			appHoldSusDate = new Date();
			lob.setAppHoldSuspDate(appHoldSusDate);
			getWork().setUpdate();
		}
		int maxSuspendDaysNum = lob.getMaxNumSuspDays();
		if (-1 == maxSuspendDaysNum) {
			NbaVpmsResultsData suspendData = getDataFromVpms(NbaVpmsAdaptor.EP_GET_MAX_SUSPEND_DAYS);
			if (null != suspendData.getResultsData() && suspendData.getResultsData().size() > 0) {
				maxSuspendDaysNum = NbaUtils.convertStringToInt((String) suspendData.getResultsData().get(0));
			}
			lob.setMaxNumSuspDays(maxSuspendDaysNum);
			getWork().setUpdate();
		}
   
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(appHoldSusDate);
		calendar.add(Calendar.DAY_OF_WEEK, maxSuspendDaysNum);
		Date maxSuspendDate = (calendar.getTime());
		if (maxSuspendDate.before(new Date())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * If the last non-reviewed requirement received LOB is set to true on the case work
	 * item, then reset it to false.  If the parent case has not been retrieved from the
	 * workflow system, then retrieve it now.  If the LOB is reset, then update the case.
	 * @throws RemoteException
	 * @throws NbaBaseException
	 */
	//NBA136 New Method
	protected void resetLastNonReviewedRequirementReceived() throws RemoteException, NbaBaseException {
 		//SPR3611 code deleted
		NbaLob lobs = getParentCase().getNbaLob();
		if (lobs.getLstNonRevReqRec()) {
			//begin SPR3611
			List transactions = new ArrayList();
			WorkItem aCase = getParentCase().getCase(); 
			transactions.addAll(aCase.getWorkItemChildren());		//Save the origional Transactions
			aCase.getWorkItemChildren().clear();	//Prevent current Transaction from being unlocked
			
			lockParentCase();	 
			//end SPR3611
			lobs.setLstNonRevReqRec(false);
			getParentCase().setUpdate();	//SPR3611
			updateWork(getUser(), getParentCase());  //NBA213
			unlockWork(getParentCase());	//SPR3611	
			aCase.setWorkItemChildren(transactions);	//Restore the original Transactions	//SPR3611
		}
	}
	/**
	 * Returns the parent case work item.
	 * @return
	 */
	// NBA136 New Method
	protected NbaDst getParentCase() {
		return parentCase;
	}

	/**
	 * Sets the parent case work item.
	 * @param dst
	 */
	// NBA136 New Method
	protected void setParentCase(NbaDst dst) {
		parentCase = dst;
	}

	/**
	 * Retrieves the parent case with a lock from the workflow system including all sources.
	 * @return
	 * @throws RemoteException
	 * @throws NbaBaseException
	 */
	//NBA136 New Method
	protected NbaDst retrieveParentCaseWithSources() throws RemoteException, NbaBaseException {
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false);
		retOpt.requestSources();
		retOpt.requestCaseAsParent();
		retOpt.requestTransactionAsSibling(); //SPR3150
		//SPR3611 code deleted
		return retrieveWorkItem(getUser(), retOpt);  //NBA213
	}

	/**
	 * Retrieves the parent case with a lock from the workflow system.
	 * @return
	 * @throws RemoteException
	 * @throws NbaBaseException
	 */
	//NBA136 New Method
	protected NbaDst retrieveParentCase() throws RemoteException, NbaBaseException {
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false);
		retOpt.requestCaseAsParent();
		//SPR3611 code deleted
		return retrieveWorkItem(getUser(), retOpt);  //NBA213
	}
	
	/**
	 * Determine whether followup frequency is present on current RequirementInfo object
	 * @return true if followup frequency is present, false otherwise
	 */
	//NBA138 New Method
	protected boolean hasFollowupFrequency(){
	    RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(getRequirementInfo());
	    return reqInfoExt.hasFollowUpFreq();
	}
	/**
	 * Lock the Case
	 * @throws NbaNetServerDataNotFoundException
	 * @throws NbaBaseException
	 */
	//SPR3611 New Method
	protected void lockParentCase() throws NbaNetServerDataNotFoundException, NbaBaseException {
		RetrieveWorkItemsRequest retrieveWorkItemsRequest = new RetrieveWorkItemsRequest();
		retrieveWorkItemsRequest.setUserID(getUser().getUserID());
		String id = getParentCase().getCase().getItemID();
		retrieveWorkItemsRequest.setWorkItemID(id);
		retrieveWorkItemsRequest.setRecordType(id.substring(26, 27));
		retrieveWorkItemsRequest.setLockWorkItem();
		lockWorkItem(retrieveWorkItemsRequest);		 //Lock the Case
		getParentCase().getCase().setLockStatus(getUser().getUserID());		 
	}
	/**
	 * Call VP/MS model to check if we can override authorization forms
	 * Returns true if conditions are met
	 * @param String sourceType
	 * @return true if conditions are met
	 */
	//ALS4228 New Method
	protected boolean checkAuthorizationOverride(String sourceType) {
		try {
			VpmsModelResult data = getDataFromVpms(NbaVpmsAdaptor.EP_GET_AUTH_OVERRIDE, sourceType);
			if(YES_VALUE.equalsIgnoreCase(getFirstResult(data))) {
				return true;
			}
		}
		catch (NbaBaseException nbe) {
			getLogger().logError(nbe.getMessage());
		}
		return false;
	}
	/**
	 * This method creates and initializes an <code>NbaVpmsAdaptor</code> object to
	 * call VP/MS to execute the supplied entryPoint.
	 * @param entryPoint the entry point to be called in the VP/MS model 
	 * @return the results from the VP/MS call in the form of an <code>VpmsModelResult</code> object
	 * @throws NbaBaseException
	 */
	//ALS4228 New Method
	public VpmsModelResult getDataFromVpms(String entryPoint, String sourceType) throws NbaBaseException, NbaVpmsException {
	    NbaVpmsAdaptor vpmsProxy = null;
	    try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
			oinkData.setContractSource(getNbaTxLife());
			Map deOink = new HashMap();
			deOink.put("A_SourceType", sourceType);
			NbaOinkRequest oinkRequest = getOinkRequestForPartyFilter();//APSL4410
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REQUIREMENTS); 
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			vpmsProxy.setSkipAttributesMap(deOink);
			if(oinkRequest != null){ //APSL4410
				vpmsProxy.setANbaOinkRequest(oinkRequest); //APSL4410
			}
			VpmsComputeResult compResult = vpmsProxy.getResults();
			NbaVpmsModelResult data = new NbaVpmsModelResult(compResult.getResult()); 

			return data.getVpmsModelResult();
		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException(NbaVpmsException.VPMS_EXCEPTION, re);
		} finally {
			try {
	            if (vpmsProxy != null) {
	                vpmsProxy.remove();
	            }
	        } catch (RemoteException re) {
	            getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
	        }
		}
	}
	/**
	 * This method locates the first Result within the ResultData(s)
	 * contained in the VpmsModelResult and returns the string at that
	 * location. If any of the fields are null or their count is 0, it
	 * returns an empty string.
	 * @param VpmsModelResult contains the result from the VPMS call
	 * @return String containing the value from the first Result
	 */
	//ALS4228 New Method
	protected String getFirstResult(VpmsModelResult modResult ) {
		String result = "";
		if( modResult == null || modResult.getResultData() == null || modResult.getResultDataCount() == 0 ) {
			return result;
		}
		if( modResult.getResultDataAt(0).getResult() == null || modResult.getResultDataAt(0).getResultCount() == 0) {
			return result;
		}
		return modResult.getResultDataAt(0).getResultAt(0);
	}
	
	//APSL4410 New Method To set up the NbaOinkRequest object
	protected NbaOinkRequest getOinkRequestForPartyFilter() throws NbaBaseException {
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		int personCode = getWorkLobs().getReqPersonCode();
		Relation relation = NbaUtils.getRelation(getNbaTxLife().getOLifE(), personCode);
		if (relation != null) {
			long roleCode = relation.getRelationRoleCode();
			String relRefId = relation.getRelatedRefID();
			oinkRequest.setPartyFilter(roleCode, relRefId);
			return oinkRequest;
		}
		return null;
	}
	
	/**
	 * APSL4846, APSL5165 (Removed SSN check)
	 * 
	 * @author ytyagi2
	 *
	 */
	class AuthorizationParty{
		private String firstName;
		private String lastName;
		
		public AuthorizationParty(String firstName,String lastName){
			this.firstName = firstName;
			this.lastName = lastName;
		}
		
		
		public boolean equals(Object anObject){
			boolean returnValue = false;
			AuthorizationParty aParty = null;
			if(anObject == null || anObject.getClass()!= this.getClass()){
				returnValue = false;
			}
			if(anObject instanceof AuthorizationParty){
				aParty  = (AuthorizationParty)anObject;
				returnValue =  firstName.equalsIgnoreCase(aParty.firstName) && lastName.equalsIgnoreCase(aParty.lastName);
			}
			return returnValue;
		}
		
		public String toString(){
			return "AuthorizationParty[ firstName = "+firstName+" , lastName = "+lastName+" ]";
		}
	}

	// NBLXA-1790 new method --Returns true if Hipaa CV 1787 OR 1926 PRESENT
	protected boolean isHipaaCVPresent() {
		Holding holding = null;
		holding = getNbaTxLife().getPrimaryHolding();
		if (holding != null && requirementInfo != null) {
			int msgSize = getNbaTxLife().getPrimaryHolding().getSystemMessageCount();
			for (int i = 0; i < msgSize; i++) {
				SystemMessageExtension systemMessageExtension = null;
				SystemMessage sysMessage = holding.getSystemMessageAt(i);
				if (sysMessage != null && (sysMessage.getMessageCode() == HIPAA_NIGO_CV)) {
					if (sysMessage.getRelatedObjectID().equals(requirementInfo.getAppliesToPartyID())) {
						systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(sysMessage);
					}
				} else if (sysMessage != null && (sysMessage.getMessageCode() == HIPAA_SIG_MISSING_CV)) {
					if (sysMessage.getRelatedObjectID().equals(signatureId)) {
						systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(sysMessage);
					}
				}// Begin NBLXA-2033
				else if (sysMessage != null && (sysMessage.getMessageCode() == HIPAA_NIGO_CV_SEVERE)) {
					if (sysMessage.getRelatedObjectID().equals(signatureId) || sysMessage.getRelatedObjectID().equals(requirementInfo.getAppliesToPartyID())) {
						systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(sysMessage);
					}
				}
				else if (sysMessage != null && (sysMessage.getMessageCode() == HIPAA_SIG_MISSING_CV_1925)) {
					if((partyRoleCode == NbaOliConstants.OLI_REL_JOINTINSURED && sysMessage.getRelatedObjectID().equals(requirementInfo.getAppliesToPartyID())) 
							|| (partyRoleCode == NbaOliConstants.OLI_PARTICROLE_32 && sysMessage.getRelatedObjectID().equals(requirementInfo.getAppliesToPartyID()))){
						systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(sysMessage);
					}
				}
				// End NBLXA-2033
				//BEGIN NBLXA-2328[NBLXA-2606]
				else if (sysMessage != null && (sysMessage.getMessageCode() == HIPAA_INVLPAGE_COUNT_CV)) {
					if((partyRoleCode == NbaOliConstants.OLI_REL_JOINTINSURED && sysMessage.getRelatedObjectID().equals(requirementInfo.getAppliesToPartyID())) 
							|| (partyRoleCode == NbaOliConstants.OLI_PARTICROLE_32 && sysMessage.getRelatedObjectID().equals(requirementInfo.getAppliesToPartyID()))){
						systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(sysMessage);
					}
				}
				//END NBLXA-2328[NBLXA-2606]
				if (systemMessageExtension != null && !systemMessageExtension.getMsgOverrideInd()) {
					String msg = null;
					if (partyRoleCode == NbaOliConstants.OLI_PARTICROLE_32) {
						msg = "Primary Insured";
					} else if (partyRoleCode == NbaOliConstants.OLI_REL_JOINTINSURED) {
						msg = "Joint Insured";
					}
					if (!NbaUtils.isNull(requirementInfo.getReqCode())) {
						addComment(NbaUtils.getRequirementTranslation(String.valueOf(requirementInfo.getReqCode()), getNbaTxLife().getPolicy())
								+ " requirement can not be ordered due to HIPAA CV Present - " + msg);
						return true;
					}
				}
			}
		}
		return false;
	}

	// NBLXA-1790 new method---Returns True if Hipaa Signed before 2 years
	protected boolean isHipaaSigExpired() throws NbaBaseException {
		if (getNbaTxLife() != null && getNbaTxLife().getPolicy() != null && getWorkLobs() != null) {
			int personCode = getWorkLobs().getReqPersonCode();
			Relation relation = NbaUtils.getRelation(getNbaTxLife().getOLifE(), personCode);
			if (relation != null) {
				partyRoleCode = relation.getRelationRoleCode();
				SignatureInfo signInfo = null;
				String msg = null;
				if (partyRoleCode == NbaOliConstants.OLI_PARTICROLE_32) {
					signInfo = NbaUtils.findSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_PRIMARY,
							NbaOliConstants.OLI_SIGTYPE_BLANKETAUTH, getNbaTxLife());
					msg = "- Primary Insured";
				} else if (partyRoleCode == NbaOliConstants.OLI_REL_JOINTINSURED) {
					signInfo = NbaUtils.findSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_JOINT,
							NbaOliConstants.OLI_SIGTYPE_BLANKETAUTH, getNbaTxLife());
					msg = "- Joint Insured";
				}
				if (signInfo != null) {
					signatureId = signInfo.getId();
					if (signInfo.hasSignatureDate()) {
						if (validateSignatureDate(new Date(), signInfo.getSignatureDate())) {
							addComment("HIPAA Expired - Signed before 2 years, please order a new HIPAA to proceed " + msg);
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	// NBLXA-1790 new method :: Check when no Of Days will greater then 730 Days.
	protected boolean validateSignatureDate(Date date1, Date date2) {
		double dateDiff = 730;
		if (date1 != null) {
			double days = (date1.getTime() - date2.getTime()) / (1000D * 60 * 60 * 24);
			if (days > dateDiff) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Check for Driver License Number and State missing  
	 */
	//NBLXA-2072 New Method
	private boolean isMVRDriverLicenseAndStateMissing(NbaLob lob) throws NbaBaseException{		
		if (isMVRRequirement(lob)) {
			RequirementInfo mvrReqInfo = getNbaTxLife().getRequirementInfo(lob.getReqUniqueID());
			NbaParty nbAParty = null;
			if (mvrReqInfo != null && mvrReqInfo.hasAppliesToPartyID()) {
				nbAParty = getNbaTxLife().getParty(mvrReqInfo.getAppliesToPartyID());
			} else {
				nbAParty = getNbaTxLife().getPrimaryParty();
			}
			if (nbAParty != null && nbAParty.getPerson() != null) {
				if (NbaUtils.isBlankOrNull(nbAParty.getPerson().getDriversLicenseNum()) || !nbAParty.getPerson().hasDriversLicenseState()) {
					return true;
				}
			}
		}
		return false;
	}
	
	/*
	 * Check for MVR requirement  
	 */
	//NBLXA-2072 New Method
	private boolean isMVRRequirement(NbaLob lob) throws NbaBaseException{
		if(lob != null && lob.getWorkType().equals(A_WT_REQUIREMENT)
					&& lob.getReqType() == NbaOliConstants.OLI_REQCODE_MVRPT){					 
				return true;
		}	
		return false;
	}	
	
	
	/*
	 * Check if Residence Address is missing  
	 */
	//NBLXA-2184 New Method | US 302047
	private boolean isResidenceAddressMissing(NbaLob lob) throws NbaBaseException{		
		if (isLNRCRequirement(lob)) {
			ArrayList systemMessages = getNbaTxLife().getPrimaryHolding().getSystemMessage();
			Iterator <SystemMessage> systemMessageIterator = systemMessages.iterator();
			while(systemMessageIterator.hasNext()){
				SystemMessage currentSystemMessage = (SystemMessage) systemMessageIterator.next();
				int messageCode = currentSystemMessage.getMessageCode();
				boolean isMsgOverriden = currentSystemMessage.getOLifEExtensionAt(0).getSystemMessageExtension().getMsgOverrideInd();
				if(NbaConstants.MISSING_RRESIDENCE_ADDRESS_CV == messageCode && !isMsgOverriden){
					return true;
				}
			}
		}
		return false;
	}
	
	
	/*
	 * Check for LNRC requirement  
	 */
	//NBLXA-2184 New Method  | US 302047
	private boolean isLNRCRequirement(NbaLob lob) throws NbaBaseException{
		if(lob != null && lob.getWorkType().equals(A_WT_REQUIREMENT)
					&& lob.getReqType() == NbaOliConstants.OLI_REQCODE_RISKCLASSIFIER){					 
				return true;
		}	
		return false;
	}
	
	// NBLXA-2603[NBLXa-2626] New Method
	private NbaSource getRecentReceivedHippa(List sources) throws NbaBaseException {
			Date recentHipaaDate = null;
			Map<Date, String> hipaaSrcMap = new HashMap<Date, String>();
			SignatureInfo signatureInfo = null;
			NbaSource lastetHipaaSrc = null;
			if (getNbaTxLife() != null && getNbaTxLife().getPolicy() != null && getWorkLobs() != null) {
				int personCode = getWorkLobs().getReqPersonCode();
				Relation relation = NbaUtils.getRelation(getNbaTxLife().getOLifE(), personCode);
				if (relation != null) {
					partyRoleCode = relation.getRelationRoleCode();
					if (partyRoleCode == NbaOliConstants.OLI_PARTICROLE_32) {
						signatureInfo = NbaUtils.findSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_PRIMARY,
								NbaOliConstants.OLI_SIGTYPE_BLANKETAUTH, getNbaTxLife());
					} else if (partyRoleCode == NbaOliConstants.OLI_REL_JOINTINSURED) {
						signatureInfo = NbaUtils.findSignatureInfo(LIFE_APP_FORM, NbaOliConstants.OLI_PARTICROLE_JOINT,
								NbaOliConstants.OLI_SIGTYPE_BLANKETAUTH, getNbaTxLife());
					}
					if (!NbaUtils.isBlankOrNull(signatureInfo)) {
						List<RequirementInfo> HippareqInfoList = getNbaTxLife().getRequirementInfoListByStatus(signatureInfo.getSignaturePartyID(),
								NbaOliConstants.OLI_REQCODE_1009800033, NbaOliConstants.OLI_REQSTAT_RECEIVED);
						RequirementInfoExtension reqInfoExtn = null;
						for (RequirementInfo reqinfo : HippareqInfoList) {
							reqInfoExtn = NbaUtils.getFirstRequirementInfoExtension(reqinfo);
							if (reqinfo.hasReceivedDate() && !NbaUtils.isBlankOrNull(reqInfoExtn) && reqInfoExtn.getReviewedInd() == true) {
								recentHipaaDate = NbaUtils.validateReceivedDate(recentHipaaDate, reqinfo.getReceivedDate());
								hipaaSrcMap.put(recentHipaaDate, reqinfo.getRequirementInfoUniqueID());
							}
						}
						for (int i = 0; i < sources.size(); i++) {
							lastetHipaaSrc = (NbaSource) sources.get(i);
							NbaLob srclob = lastetHipaaSrc.getNbaLob();
							if (srclob.getReqType() == NbaOliConstants.OLI_REQCODE_1009800033
									&& srclob.getReqUniqueID().equalsIgnoreCase(hipaaSrcMap.get(recentHipaaDate))) {
								return lastetHipaaSrc;
							}
						}
					}
				}
		}
		return aSource;
	}
}
