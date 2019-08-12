package com.csc.fsg.nba.business.process;
/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which are
 * proprietary to CSC Financial Services Group�.  The use, reproduction,
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
import java.util.ArrayList;
import java.util.HashMap;

import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.access.contract.NbaServerUtility;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaNetServerException;
import com.csc.fsg.nba.provideradapter.NbaProviderAdapterFacade;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
/**
 * <code>NbaProcProviderIndex</code> calls provider adapter to update LOBs. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td></tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>SPR1359</td><td>Version 3</td><td>Automated processes stop poller when unable to lock supplementary work items</td></tr>
 * <tr><td>SPR1312</td><td>Version 3</td><td>MIB Requirement is going to provider error in Order Requirement Process</td></tr>
 * <tr><td>SPR1273</td><td>Version 3</td><td>RIP does not update RQVN LOB field when ripping in temporary work items</td></tr>
 * <tr><td>NBA097</td><td>Version 4</td><td>Work Routing Reason Displayed</td></tr>
 * <tr><td>NBA095</td><td>Version 4</td><td>Queues Accept Any Work Type</td></tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>121/1122 Migration</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPR3311</td><td>Version 8</td><td>Pre-existing temporary Requirements are not matched to a new Requirement</td></tr>
 * <tr><td>AXAL3.7.31</td><td>AXA Life Phase 1</td><td>Provider Interfaces</td></tr>
 * <tr><td>ALS4966</td><td>AXA Life Phase 1</td><td>QC#4129-AXAL03.07.31 Provider Results for "Joint Life #2" error when Requirements are not posted</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */
public class NbaProcProviderIndex extends NbaAutomatedProcess {

	protected static final String OLI_REQSTAT_RECEIVED = String.valueOf(NbaOliConstants.OLI_REQSTAT_RECEIVED); //SPR3311
	
	/**
	 * NbaProcProviderIndex constructor comment.
	 */
	public NbaProcProviderIndex() {
		super();
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (12/12/2002 4:21:06 PM)
	 * @return com.csc.fsg.nba.vo.NbaDst
	 * @param businessArea java.lang.String
	 * @param workType java.lang.String
	 * @param status java.lang.String
	 */
	public NbaDst createTransaction(String workType, String status) {
		//NBA208-32
		WorkItem transaction = new WorkItem();
		NbaDst nbaDst = new NbaDst();
		//NBA208-32
		nbaDst.setUserID(getUser().getUserID());
		nbaDst.setPassword(getUser().getPassword());
		try{
			nbaDst.addTransaction(transaction);
		}catch(Exception ex){
		}
		//set Business Area, Work type and Status
		transaction.setBusinessArea(getWork().getBusinessArea());
		transaction.setWorkType(workType);
		transaction.setStatus(status);
		transaction.setSystemName(getWork().getSystemName()); //APSL5055-NBA331
		//NBA208-32		
		transaction.setCreate("Y");
		return nbaDst;
	}
	/** 
	 * This automated process calls the provider adapter to update the LOB fields 
	 * so that the Requirement Ordered process can match the temporary work item
	 * to a permanent work item.  In addition to updating the LOB fields, additional
	 * sources may be added to the work item by the provider adapter. 
	 * @param user the user/process for whom the process is being executed
	 * @param work a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about
	 *         the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		// NBA095 - block move begin
		if (!initialize(user, work)) {
			return statusProcessFailed();
		}
		try { //NBA095
			// NBA027 - logging code deleted
			retrieveWork();
			// SPR1273 BEGIN
			//AXAL3.7.31 begin
            HashMap skipMap = new HashMap();
            skipMap.put("A_XmlVendorCode", "");
			NbaVpmsResultsData resultsData =
				new NbaVpmsResultsData(
					getDataFromVpms(
						NbaVpmsAdaptor.REQUIREMENTS,
						NbaVpmsAdaptor.EP_GET_PROVIDER_FOR_RESULT,
						null,
						skipMap,
						null));
            //AXAL3.7.31 end
			//ACN014
			if (resultsData == null) {
				result =
					new NbaAutomatedProcessResult(
						NbaAutomatedProcessResult.SUCCESSFUL,
						"Vpms Error",
						getVpmsErrorStatus());
				addComment("Unable to determine Provider"); //NBA103						
			}
			getWorkLobs().setReqVendor((String) resultsData.getResultsData().get(0));	//SPR3311
			getWorkLobs().setReqStatus(OLI_REQSTAT_RECEIVED);	//SPR3311
			updateLobsForJoint(getWorkLobs()); //ALS4966
			// begin ACN014
			NbaProviderAdapterFacade providerAdapter = new NbaProviderAdapterFacade(getWork(), getUser());
			ArrayList aList = null;
			NbaDst mywork = null;	//AXAL3.7.31			
			try {
				//NBA208-32
				mywork = getWork();	//AXAL3.7.31
				work.setUserID(user.getUserID());
				work.setPassword(user.getPassword());				
				aList = providerAdapter.processResponseFromProvider(mywork, getUser());
			} catch (NbaBaseException nbe) {
				//US-283120
				throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH, nbe.getMessage()); // APSL4165
				// NBA103 - removed conditional
				// addComment(nbe.getMessage());
			}
			if (aList == null || aList.size() == 0) {
				throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH); // APSL4165
			} // APSL4165 code deleted
				for (int i = 0; i < aList.size(); i++) {
					NbaDst revWork = (NbaDst) aList.get(i);
					reinitializeFields(user, revWork);// ALS5718
					if (revWork.getID() != null && revWork.getID().equalsIgnoreCase(work.getID())) {
						setWork(revWork);
						increasePriority();// ALS5718
					} else {
						updateTempWorkItem(revWork);
					}
					// SR787006-APSL3702 Begin
					if (revWork.getNbaLob().getWorkSubType() == NbaOliConstants.TC_SUBTYPE_PAPERDELIVERY_REQUEST) {
						NbaTXLife pendingTxLife = getHoldingFromPending(user, revWork);
						if (getResult() == null) {
							ApplicationInfo appInfo = pendingTxLife.getPolicy().getApplicationInfo();
							ApplicationInfoExtension appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(appInfo);
							if (appInfoExtn != null) {
								appInfoExtn.setReqPolicyDeliverMethod(NbaOliConstants.OLI_POLDELMETHOD_REGULARMAIL);
								appInfoExtn.setActionUpdate();
							}
							pendingTxLife = doContractUpdate(pendingTxLife);
							handleHostResponse(pendingTxLife);
							NbaContractLock.removeLock(revWork, getUser());// APSL3603(QC13073)
						}
					}
					// SR787006-APSL3702 End
				}
				if (getResult() == null) { // SR787006-APSL3702 End
					result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus());
				}
				// APSL4165 code deleted
			// end ACN014
			
			changeStatus(getResult().getStatus());
			doUpdateWorkItem();
			return result;
		} catch (NbaBaseException nbe) { //SPR1359
			throw nbe; //SPR1359
		} catch (Exception e) {
			e.printStackTrace();
			NbaBaseException nbe = new NbaBaseException("An exception occured during Provider Indexing process", e);
			//ACN014
			throw nbe;
		}
	}
	/**
	 * Updates the Joint insured LOB fields
	 * @param workLobs
	 * @throws NbaBaseException
	 */
	//ALS4966 New Method
	private void updateLobsForJoint(NbaLob nbaLob) throws NbaBaseException {
		nbaLob.setJointDOB(nbaLob.getDOB());
		nbaLob.setJointFirstName(nbaLob.getFirstName());
		nbaLob.setJointLastName(nbaLob.getLastName());
		nbaLob.setJointSsnTin(nbaLob.getSsnTin());
	}
	/**
	 * This method retrieves the work item along with all of its associated sources.
	 * Creation date: (11/5/2002 4:58:47 PM)
	 * @throws NbaBaseException
	 */
	public void retrieveWork() throws NbaBaseException {
		//NBA213 deleted code
		NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
		retrieveOptionsValueObject.setWorkItem(getWork().getID(), false);
		retrieveOptionsValueObject.requestSources();
		retrieveOptionsValueObject.setLockWorkItem();
		setWork(retrieveWorkItem(getUser(), retrieveOptionsValueObject));  //NBA213
		//NBA213 deleted code
	}
	/**
	 * This method retrieves the work item along with all of its associated sources.
	 * Creation date: (11/5/2002 4:58:47 PM)
	 * @throws NbaBaseException
	 */
	public void updateTempWorkItem(NbaDst tempTrans) throws NbaBaseException {
		//NBA213 deleted code
		increasePriority();
		updateWork(getUser(), tempTrans);  //NBA213
		unlockWork(getUser(), tempTrans);	//AXAL3.7.31
		//NBA213 deleted code
	}
	
	//New Method ALS5718
	protected void reinitializeFields(NbaUserVO newUser, NbaDst newWork)
	throws NbaBaseException, NbaNetServerDataNotFoundException, NbaNetServerException {
 		setStatusProvider(new NbaProcessStatusProvider(newUser, newWork));
 	}
	
	/**
	 * Retrieve a holding inquiry from pending backend syatem. 
	 * @param nbaUserVO the NbaUser for whom the process is being executed
	 * @param nbaDst a NbaDst value object 
	 * @return a NbaTXLife object with holding from pending system.
	 * @throws NbaBaseException
	 */
	// SR787006-APSL3702 New method
	protected NbaTXLife getHoldingFromPending(NbaUserVO nbaUserVO, NbaDst nbaDst) throws NbaBaseException {
		NbaTXLife response = null;
		NbaTXRequestVO requestVO = createRequestObject(NbaConstants.READ, nbaUserVO.getUserID());
		if (NbaServerUtility.isDataStoreDB(nbaDst.getNbaLob(), getUser())) {
			requestVO.setOverrideDataSource(STANDALONE);
		} else {
			requestVO.setOverrideDataSource(WRAPPERED);
		}

		// do not copy locking logic from here. Autoprocess automatically lock a database
		setContractAccess(UPDATE);
		NbaContractLock.requestLock(nbaDst, nbaUserVO);
		try {
			response = NbaContractAccess.doContractInquiry(requestVO);  //NBA213
		} catch (NbaDataAccessException de) {
			getLogger().logException("Error while doing holding retrieval from pending system", de);
			throw de;
		}		
		handleHostResponse(response, true);
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Completed holding retrieval from pending system");
		}
		return response;
	}
	
}
