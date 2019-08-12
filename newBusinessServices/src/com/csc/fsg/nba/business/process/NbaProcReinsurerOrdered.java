package com.csc.fsg.nba.business.process;

/*
 * ************************************************************** <BR>
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
 * ************************************************************** <BR>
 */
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaTransactionSearchResultVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

/**
 * NbaProcReinsurerOrdered attempts to match ordered reinsurer bids with reinsurer
 * offer received directly from the reinsurer or from a manual source.
 * <p>Two types of reinsurance work items are handled by this process:
 * <ul><li>Reinsurance work items that have been ordered but have no source
 * <li>Temporary Reinsurance work items whose source needs to be associated with a 
 * Reinsurance work item.
 * </ul>
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA038</td><td>Version 3</td><td>Reinsurance</td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * <tr><td>SPR1715</td><td>Version 4</td><td>Wrappered/Standalone Mode Should Be By BackEnd System and by Plan</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR2380</td><td>Version 5</td><td>Cleanup</td></tr> 
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>SPR2806</td><td>Version 5</td><td>Reinsurance work item is getting matched even though the result is not receipted</td></tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>General Code Clean Up Issues for Version 6</td></tr>
 * <tr><td>SPR3009</td><td>Version 6</td><td>When a reinsurance result is received, it should bring the case out of underwriter hold queue.</td></tr>
 * <tr><td>NBA146</td><td>Version 6</td><td>Workflow integration</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaProcReinsurerOrdered extends NbaAutomatedProcess {
	protected ArrayList matchingWorkItems;
	protected NbaProcessStatusProvider wiStatus = null;
	protected java.util.ArrayList tempWorkItems;
	protected java.util.ArrayList permWorkItems;
	protected Map caseWorkItems = new Hashtable();	//SPR3009
	/** Minimum data for AWD lookup presence indicator **/
	private boolean lookupDataFound = false; //SPR2806	SPR2992
	//SPR2380 removed logger	
	private boolean isAdditionalInfo = false; //ALS5494
	/**
	 * NbaProcReinsurerOrdered default constructor.
	 */
	public NbaProcReinsurerOrdered() {
		super();
	}

//	NBA103 - removed method

	/**
	 * This method associates the Reinsurer Offer source(s) attached to the
	 * temporary work item to the reinsurance work item.  There may be both
	 * NBREINRSLT and NBREINSUPP sources associated with a work item.
	 * @param permWorkItem the permanent work item to which the source(s) is(are) to be added
	 * @param tempWorkItem the temporary work item that contains the source(s)
	 */
	protected void associateSource(NbaDst permWorkItem, NbaDst tempWorkItem) throws NbaBaseException {	//SPR2992 changed method signature
		for (int i = 0; i < tempWorkItem.getNbaSources().size(); i++) {
			NbaSource aSource = (NbaSource) tempWorkItem.getNbaSources().get(i);
			permWorkItem.getNbaTransaction().addNbaSource(aSource);
		}
	}
	/**
	 * This method drives the ReinsurerOrdered process.  It process both permanent requirement 
	 * and temporary requirement work items.
	 * It starts by retrieving all the sources for the work item being processed and then looks up 
	 * any matching work items.  If none are found, the work item will be sent to 
	 * an error queue.
	 * When matching work items are located, they are updated by moving the sources associated
	 * with the temporary work item to the permanent work item.  Then all work items are updated
	 * moved to an appropriate queue.
	 * @param user the user/process for whom the process is being executed
	 * @param work a requirement work item, either permanent or temporary, to be matched
	 * @return an NbaAutomatedProcessResult containing information about
	 *         the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException   {
		if (!initialize(user, work)) {
			return getResult();
		}
		if (getLogger().isDebugEnabled()) {  
			getLogger().logDebug("RequirementOrdered for contract " + getWork().getNbaLob().getPolicyNumber());
		}

		//retrieve the sources for this work item
		//NBA213 deleted code
		NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
		retrieveOptionsValueObject.setWorkItem(getWork().getID(), false);
		retrieveOptionsValueObject.requestSources();
		// SPR3009 code deleted
		setWork(retrieveWorkItem(getUser(), retrieveOptionsValueObject));	//SPR3009, NBA213
		//NBA213 deleted code
		isAdditionalInfo = checkIfAdditionalInfo(getWork());//ALS5494 Checks if the WI is additional Info WI
		// call awd to lookup matching items	
		NbaSearchVO searchVO = null;
		searchVO = lookupWork();
		copyCreateStation(getWork());//AXAL3.7.20 ALS5191
		reinitializeFields();//AXAL3.7.20
		// any work items found?
		// begin SPR2806
		if(isAdditionalInfo) {//ALS5494
			processAdditionalInfo(searchVO);
		}else {
			if (isLookupDataFound()) { // if there are no matching work items found		//SPR2992
				if (searchVO.getSearchResults() != null && searchVO.getSearchResults().isEmpty()) {
					processUnmatchedWorkitem();
				}else {
				    //begin SPR3009
					try {
	                    // retrieve and lock matching work items
	                    processMatchingWorkItems(searchVO.getSearchResults());
	                } catch (Exception e) {
	                    handleProcessingException(e);
	                }
	                try {
	                    updateCaseWorkItems();
	                    updatePermanentWorkItems();
	                    updateTempWorkItems();
	                } catch (NbaBaseException e) {
	                    handleUpdateException(e);
	                }
	                if (result == null) {
	                    setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
	                }
	                //end SPR3009                
				}
				if (result == null) {
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "SUSPENDED", "SUSPENDED"));
				}
			} else {
			    setWork(getOrigWorkItem()); //SPR3009
				changeStatus(getFailStatus());
				addComment("Minimum data for AWD lookup not present");
				doUpdateWorkItem();
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, getFailStatus(), getFailStatus()));
			}
			//end SPR2806
		}
		return result;
	}
	
	/**
	 * Check if the work is the TEMPREN WI created corresponding to an Additional Info
	 */
	//ALS5494 New Method
	protected boolean checkIfAdditionalInfo(NbaDst work) {
		if (A_WT_TEMP_REINSURANCE.equals(work.getWorkType()) && !work.getNbaSources().isEmpty()) {
			List sources = work.getNbaSources();
			for(int i=0; i<sources.size();i++) {
				NbaSource nbaSource = (NbaSource) work.getNbaSources().get(i);
				if(A_ST_REINSURANCE_XML_TRANSACTION.equals(nbaSource.getSourceType())) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Process Additional Info WI
	 */
	//ALS5494 New Method
	protected void processAdditionalInfo(NbaSearchVO searchVO) throws NbaBaseException {
		try {
			retrieveMatchingWorkItems(searchVO.getSearchResults());
		} catch (Exception e) {
			handleProcessingException(e);
		}
		try {
			setWiStatus(new NbaProcessStatusProvider(getUser(), getWork()));
			getWork().setStatus(getWiStatus().getPassStatus());
			updateWork(getUser(), getWork());
			unlockWork(getUser(), getWork());

			if (!getMatchingWorkItems().isEmpty()) {
				NbaDst matchingWorkItem = (NbaDst) getMatchingWorkItems().get(0);
				matchingWorkItem.setStatus(getWiStatus().getAlternateStatus());
				matchingWorkItem.getNbaLob().deleteAppHoldSuspDate();
				matchingWorkItem.getNbaLob().setAdditionalInfo(true);//NBLXA -1983 
				updateWork(getUser(), matchingWorkItem);
				unlockWork(getUser(), matchingWorkItem);
			}
		} catch (NbaBaseException e) {
			handleUpdateException(e);
		}
		if (result == null) {
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getPassStatus(), getPassStatus()));
		}
	}
	
	/**
	 * copy createStation from source to work
	 */
	//New Method AXAL3.7.20 //ALS5191 Signature Changed
	protected void copyCreateStation(NbaDst work) {
		if (!work.getNbaSources().isEmpty()) {
			NbaSource nbaSource = (NbaSource) work.getNbaSources().get(0);
			NbaLob sourceLob = nbaSource.getNbaLob();
			work.getWorkItem().setCreateNode(sourceLob.getCreateStation());
		}
	}
	
	/**
	 * Reset the auto process status fields
	 */
	//AXAL3.7.20
	protected void reinitializeFields()
		throws NbaBaseException{
	 	setStatusProvider(new NbaProcessStatusProvider(user, getWork()));
	}
	
    /**
	 * This method creates and initializes an <code>NbaVpmsAdaptor</code> object to
	 * call VP/MS to execute the supplied entryPoint.
	 * @param entryPoint the entry point to be called in the VP/MS model 
	 * @return the results from the VP/MS call in the form of an <code>NbaVpmsResultsData</code> object
	 */
	protected NbaVpmsResultsData getDataFromVpms(String entryPoint) throws NbaBaseException, NbaVpmsException {	//SPR2992 changed method signature
	    NbaVpmsAdaptor vpmsProxy = null; //SPR3362
	    try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
			Map deOink = new HashMap();
			deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser())); //SPR2639
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REINSURANCE); //SPR3362
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			vpmsProxy.setSkipAttributesMap(deOink);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			//SPR3362 code deleted
			return data;
		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException(NbaVpmsException.VPMS_EXCEPTION, re);
        //begin SPR3362 
		} finally {
			try {
			    if (vpmsProxy != null) {
					vpmsProxy.remove();					
				}
			} catch (RemoteException re) {
			    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); 
			}
		}
		//end SPR3362
	}
	/**
	 * Answers the work item status member
	 * @return the work item statuses in the form of an NbaProcessStatusProvider object
	 */
	protected NbaProcessStatusProvider getWiStatus() {		//SPR2992 changed method signature
		return wiStatus;
	}
	/**
	 * This method creates and initializes an <code>NbaSearchVO</code> object to
	 * find any matching work items. It calls the Reinsurance VP/MS model to
	 * get the LOB fields to be used in the search.  If the first set finds no
	 * match, then a second set is used.
	 * @return the search value object containing the results of the search
	 */
	protected NbaSearchVO lookupWork() throws NbaBaseException {	//SPR2992 changed method signature
		//NBA213 deleted code
			NbaSearchVO searchVO = new NbaSearchVO();
			String entryPoints[] = { NbaVpmsAdaptor.EP_REIN_PRIMARY_SEARCH_FIELDS, NbaVpmsAdaptor.EP_REIN_SECONDARY_SEARCH_FIELDS };
			for (int i = 0; i < entryPoints.length; i++) {
				NbaVpmsResultsData data = getDataFromVpms(entryPoints[i]);
				//begin SPR2806
				ArrayList aList = data.getResultsData();
				//check if sufficient lob data is present on work, if not just skip this matching criteria
				if (!checkLobPresence(getWork().getNbaLob(), aList)) {
					continue;
				}
				setLookupDataFound(true);	//SPR2992					
				searchVO.setNbaLob(getNbaLobForLookup(aList));
				//Code moved ALS5171
				// end SPR2806
				if (getWork().getTransaction().getWorkType().equalsIgnoreCase(A_WT_REINSURANCE)) {
					searchVO.setWorkType(A_WT_TEMP_REINSURANCE);
					searchVO.setStatus(A_STATUS_REINSURANCE_INDEXED);//ALS5171
				} else {
					searchVO.setWorkType(A_WT_REINSURANCE);
					if(isAdditionalInfo) {//ALS5494
						searchVO.setQueue(END_QUEUE);//ALS5494
					}else {
						searchVO.setStatus(A_STATUS_REINSURANCE_ORDERED);//ALS5171	
					}
				}
				searchVO.setResultClassName("NbaTransactionSearchResultVO");
				searchVO = lookupWork(getUser(), searchVO);	//SPR3009, NBA213
		        if (searchVO.isMaxResultsExceeded()){	//NBA146
		            throw new NbaBaseException(NbaBaseException.SEARCH_RESULT_EXCEEDED_SIZE, NbaExceptionType.FATAL);	//NBA146
		        }	//NBA146
				if (!(searchVO.getSearchResults().isEmpty())) {
					break;
				}
			}
			return searchVO;
		//NBA213 deleted code
	}
	/**
	 * This method retrieves the work item referenced in the NbaSearchResultVO object.  It then
	 * process each work item by associating the source from the temp work item to the
	 * permanent work item and then updating any cross-referenced work items.
	 * @param searchResults the list of results found by the AWD lookup
	 */
	//SPR1851 remove NbaAutoProcessNotLockedException
	protected void processMatchingWorkItems(List searchResults) throws NbaBaseException {	//SPR2992 changed method signature
		retrieveMatchingWorkItems(searchResults);
		// all matching work items have been retrieved
		setPermWorkItems(new ArrayList());	//SPR2992
		setTempWorkItems(new ArrayList());	//SPR2992
		if (getWork().getTransaction().getWorkType().equals(A_WT_REINSURANCE)) {
			getPermWorkItems().add(getWork());	//SPR2992
			setTempWorkItems((ArrayList) getMatchingWorkItems().clone());	//SPR2992
		} else {
			getTempWorkItems().add(getWork());	//SPR2992
			setPermWorkItems((ArrayList) getMatchingWorkItems().clone());	//SPR2992
		}
		retrieveCaseWorkItems(getPermWorkItems());	//SPR3009
		for (int i = 0; i < getPermWorkItems().size(); i++) {	//SPR2992
			NbaDst perm = (NbaDst) getPermWorkItems().get(i);	//SPR2992
			for (int j = 0; j < getTempWorkItems().size(); j++) {	//SPR2992
				NbaDst temp = (NbaDst) getTempWorkItems().get(j);	//SPR2992
				associateSource(perm, temp);
			}
		}
	}
    /**
	 * This method retrieves the matching work items found in the NbaSearchResultVO object.
	 * Each work item is retrieved and locked.  If unable to retrieve with lock, this work item
	 * will be suspended for a brief period of time due to the auto suspend flag being set.
	 * @param searchResults the results of the previous AWD lookup
	 * @throws NbaBaseException
	 */
	//SPR1851 remove NbaLockException
	protected void retrieveMatchingWorkItems(List searchResults) throws NbaBaseException  { 	//SPR2992 changed method signature
		//NBA213 deleted code
		ListIterator results = searchResults.listIterator();
		setMatchingWorkItems(new ArrayList());		//SPR2992
		while (results.hasNext()) {
			NbaTransactionSearchResultVO resultVO = (NbaTransactionSearchResultVO) results.next();
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(resultVO.getTransactionID(), false);
			retOpt.requestSources();				 
			retOpt.setLockWorkItem();
			NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);	//SPR3009, NBA213
			getMatchingWorkItems().add(aWorkItem);	//SPR2992
		}
		//NBA213 deleted code
	}
    /**
	 * Sets the work item status member
	 * @param newWiStatus
	 */
	protected void setWiStatus(NbaProcessStatusProvider newWiStatus) {		//SPR2992 changed method signature
		wiStatus = newWiStatus;
	}
	/**
	 * This method updates the work item so that the source will
	 * be added and the status updated.
	 */
	protected void updatePermanentWorkItems() throws NbaBaseException {	//SPR2992 changed method signature
		getLogger().logDebug("Starting updatePermanentWorkItems");
		// get the receipt date from the temporary work item
		// NBA3290 code deleted
		for (int i = 0; i < getPermWorkItems().size(); i++) {	//SPR2992
			NbaDst perm = (NbaDst) getPermWorkItems().get(i);	//SPR2992
			//NBA213 deleted code
			setWiStatus(new NbaProcessStatusProvider(getUser(), perm)); //SPR1715 SPR2992
			perm.setStatus(getWiStatus().getPassStatus());	//SPR2992
			NbaUtils.setRouteReason(perm,perm.getStatus());//APSL462
			perm.increasePriority(getWiStatus().getWIAction(), getWiStatus().getWIPriority());	//SPR2992
			copyCreateStation(perm);//ALS5191
			updateWork(getUser(), perm);	//SPR3009, NBA213
			if (perm.isSuspended()) {
				NbaSuspendVO suspendVO = new NbaSuspendVO();
				suspendVO.setTransactionID(perm.getID());
				unsuspendWork(getUser(), suspendVO);	//SPR3009, NBA213
			}
			if (!perm.getID().equals(getWork().getID())) { // APSL5055-NBA331.1
				unlockWork(getUser(), perm);	//SPR3009, NBA213				
			} // APSL5055-NBA331.1  
			//NBA213 deleted code
		}
	}
	/**
	 * This method breaks the association between the temporary work item
	 * and it's source and then changes the status of this work item to send
	 * it to the end queue.
	 */
	protected void updateTempWorkItems() throws NbaBaseException {	//SPR2992 changed method signature
		getLogger().logDebug("Starting updateTempWorkItems"); 
		// break association
		for (int i = 0; i < getTempWorkItems().size(); i++) {	//SPR2992
			NbaDst temp = (NbaDst) getTempWorkItems().get(i);	//SPR2992
			copyCreateStation(temp);//ALS5191
			ListIterator sources = temp.getNbaSources().listIterator();
			if (sources.hasNext()) {
				NbaSource source = (NbaSource) sources.next();
				source.setBreakRelation();
			}
			setWiStatus(new NbaProcessStatusProvider(getUser(), temp)); //SPR1715 SPR2992
			temp.setStatus(getWiStatus().getPassStatus());	//SPR2992
			//NBA213 deleted code
			updateWork(getUser(), temp);	//SPR3009, NBA213
			if (temp.isSuspended()) {
				NbaSuspendVO suspendVO = new NbaSuspendVO();
				suspendVO.setTransactionID(temp.getID());
				unsuspendWork(getUser(), suspendVO);	//SPR3009, NBA213
			}
			if (!temp.getID().equals(getWork().getID())) { // APSL5055-NBA331.1
				unlockWork(getUser(), temp);	//SPR3009, NBA213				
			} // APSL5055-NBA331.1
			//NBA213 deleted code
		}
	}	
	/**
	 * Check if NbaLob object passed contains a non null values for all the lobs mentioned in lobList.
	 * If any of the LOBs mentioned in the list is null or empty string, return false 
	 * indicating that this AWD work item search criteria cannot be used, otherwise return true.  
	 * @param workLob NbaLob object
	 * @param lobList arraylist of LOB names 
	 * @return boolean indicating that this search criteria can/cannot be used for AWD search
	 */
	//SPR2806 New Method
	protected boolean checkLobPresence(NbaLob workLob, ArrayList lobList) {
		int size = lobList.size();
		String lobName = null;
		String lobValue = null;
		NbaOinkDataAccess aNbaOinkDataAccess = new NbaOinkDataAccess(workLob);
		NbaOinkRequest aNbaOinkRequest = new NbaOinkRequest();
		for (int i = 0; i < size; i++) {
			lobName = (String) lobList.get(i);
			if (lobName.startsWith("A_")) {
				aNbaOinkRequest.setVariable(lobName.substring(2));
			} else {
				aNbaOinkRequest.setVariable(lobName);
			}
			try {
				lobValue = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
				if (lobValue == null
					|| lobValue.trim().length() == 0
					|| NbaAutomatedProcess.LOB_NOT_AVAILABLE.equals(lobValue)
					|| "0".equals(lobValue)) {
					return false;
				}
			} catch (NbaBaseException e) {
				getLogger().logInfo("Could not resolve:" + lobName);
				return false;
			}
		}
		return true;

	}

	/**
	 * This method looks at the ApHoldSuspend Date LOB value 
	 * for the work item to see if the work item has been previously suspended.
	 * If not, it calculates the activation date by making a call to the Requirements VP/MS
	 * model to get the number of suspend days and then suspends the work item.
	 * If previously suspended, the process sets the work item's status to
	 * the fail status to move it to either the Follow up queue (if an NBREINSURE
	 * work item) or the Error queue (if an NBTEMPREN work item). 
	 * @throws NbaBaseException
	 */
	//SPR2806 New Method
	protected void processUnmatchedWorkitem() throws NbaBaseException {
		if (getWork().getNbaLob().getAppHoldSuspDate() == null) { // if it hasn't been suspended NbaVpmsResultsData
			NbaVpmsResultsData data = getDataFromVpms(EP_REIN_MATCHING_SUSPEND_DAYS); 
			String suspendDays = getFirstResult(data); 
			if (suspendDays.length() == 0) { 
			    setWork(getOrigWorkItem()); //SPR3009
				changeStatus(getVpmsErrorStatus()); 
				addComment("Suspend Days not found in VPMS model.");
				doUpdateWorkItem();
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getVpmsErrorStatus(), getVpmsErrorStatus()));
			} else if(Integer.parseInt(suspendDays) > 0) {  //ALS5028
				GregorianCalendar calendar = new GregorianCalendar();
				calendar.setTime(new Date());
				calendar.add(Calendar.DAY_OF_WEEK, Integer.parseInt(suspendDays));
				Date reqSusDate = (calendar.getTime());
				setWork(getOrigWorkItem()); //SPR3009
				getWork().getNbaLob().setAppHoldSuspDate(reqSusDate);
				addComment("Suspended awaiting matching work item");
				work.setUpdate();
				suspendTransaction(reqSusDate);
			} else {
				updateUnmatchedWorkitem(); //ALS5028
			}
		} else {
			//Code Deleted ALS5028
			updateUnmatchedWorkitem(); //ALS5028
		}
	}
	
	/**
	 * This method sets the work item's status to FailStatus. 
	 */
	//New Method ALS5028
	protected void updateUnmatchedWorkitem() throws NbaBaseException {
		setWork(getOrigWorkItem());
		changeStatus(getFailStatus());
		addComment("Matching work item not found");
		doUpdateWorkItem();
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, getFailStatus(), getFailStatus()));
	}
	
	/**
	 * This method locates the first Result within the ResultData(s)
	 * contained in the VpmsModelResult and returns the string at that
	 * location. If any of the fields are null or their count is 0, it
	 * returns an empty string.
	 * @param VpmsModelResult contains the result from the VPMS call
	 * @return String containing the value from the first Result
	 */
	//SPR2806 New Method
	protected String getFirstResult(NbaVpmsResultsData vpmsResult) {
		String result = "";
		if (vpmsResult == null || vpmsResult.getResultsData() == null || vpmsResult.getResultsData().size() == 0) {
			return result;
		}
		if (vpmsResult.getResultsData().get(0) == null) {
			return result;
		}
		return (String) (vpmsResult.getResultsData()).get(0);
	}

	/**
	 * This method suspends a work item by using the work item information and
	 * the supplied suspend date to populate the suspendVO.
	 * @param suspendDays the number of days to suspend
	 * @throws NbaBaseException
	 */
	//SPR2806 New Method
	protected void suspendTransaction(Date reqSusDate) throws NbaBaseException {
		getLogger().logDebug("Starting suspendTransaction");
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		suspendVO.setTransactionID(getWork().getID());
		suspendVO.setActivationDate(reqSusDate);
		updateForSuspend(suspendVO);
	}

	/**
	 * Since the work item must be suspended before it can be unlocked, this method
	 * is used instead of the superclass method to update AWD.
	 * <P>This method updates the work item in the AWD system, suspends the 
	 * work item using the supsendVO, and then unlocks the work item.
	 * @param suspendVO the suspend value object created by the process to be used
	 *  in suspending the work item.
	 * @throws NbaBaseException
	 */
	//SPR2806 New Method
	protected void updateForSuspend(NbaSuspendVO suspendVO) throws NbaBaseException {
		getLogger().logDebug("Starting updateForSuspend");
		updateWork(getUser(), getWork());	//SPR3009, NBA213
		suspendWork(getUser(), suspendVO);	//SPR3009, NBA213
		unlockWork(getUser(), getWork());	//SPR3009, NBA213
		}

    /**
     * Return true if an Lookup was performed
     * @return lookupDataFound.
     */
	// SPR2992 New Method
    protected boolean isLookupDataFound() {
        return lookupDataFound;
    }
    /**
     * Set the Lookup was performed indicator
     * @param lookupDataFound The lookupDataFound to set.
     */
    // SPR2992 New Method
    protected void setLookupDataFound(boolean lookupDataFound) {
        this.lookupDataFound = lookupDataFound;
    }
    /**
     * Return the matching Work Items.
     * @return matchingWorkItems.
     */
    // SPR2992 New Method    
    protected ArrayList getMatchingWorkItems() {
        return matchingWorkItems;
    }
    /**
     * Set the matching Work Items
     * @param matchingWorkItems The matchingWorkItems to set.
     */
    // SPR2992 New Method    
    protected void setMatchingWorkItems(ArrayList matchingWorkItems) {
        this.matchingWorkItems = matchingWorkItems;
    }
    /**
     * Return the permanent Work Items.
     * @return permWorkItems.
     */
    // SPR2992 New Method    
    protected java.util.ArrayList getPermWorkItems() {
        return permWorkItems;
    }
    /**
     * Set the permanent Work Items.
     * @param permWorkItems The permWorkItems to set.
     */
    // SPR2992 New Method    
    protected void setPermWorkItems(java.util.ArrayList permWorkItems) {
        this.permWorkItems = permWorkItems;
    }
    /**
     * Return the temporary Work Items.
     * @return tempWorkItems.
     */
    // SPR2992 New Method    
    protected java.util.ArrayList getTempWorkItems() {
        return tempWorkItems;
    }
    /**
     * Set the temporary Work Items.
     * @param tempWorkItems The tempWorkItems to set.
     */
    // SPR2992 New Method    
    protected void setTempWorkItems(java.util.ArrayList tempWorkItems) {
        this.tempWorkItems = tempWorkItems;
    }
    /**
     * Return the caseWorkItems 
     * @return caseWorkItems.
     */
    // SPR3009 New Method
    protected Map getCaseWorkItems() {
        return caseWorkItems;
    }

    /**
     * Set the caseWorkItems
     * @param caseWorkItems the caseWorkItems to set.
     */
    // SPR3009 New Method
    protected void setCaseWorkItems(Map caseWorkItems) {
        this.caseWorkItems = caseWorkItems;
    }

    /**
     * Update Case work items if their statuses have changed. Unlock the Case work items. 
     * @throws NbaBaseException
     */
    // SPR3009 New Method
    protected void updateCaseWorkItems() throws NbaBaseException {
        NbaDst nbaDst;
        Iterator it = it = getCaseWorkItems().values().iterator();
        while (it.hasNext()) {
            nbaDst = (NbaDst) it.next();
            //NBA208-32
            if ("Y".equals(nbaDst.getCase().getUpdate())) {
				updateWork(getUser(), nbaDst);  //NBA213
            }
			unlockWork(getUser(), nbaDst);  //NBA213
        }
    }

    /**
     * Unlock any locked work items. 
     * @throws NbaBaseException
     */
    // SPR3009 New Method
    protected void unLockItems() throws NbaBaseException {
        Iterator it = getMatchingWorkItems().iterator();
        NbaDst nbaDst; 
		//NBA213 deleted code
		//process transactions
		while (it.hasNext()) {
			nbaDst = (NbaDst) it.next();
			unlockWork(getUser(), nbaDst);  //NBA213
		}
		it = getCaseWorkItems().values().iterator();
		//process cases
		while (it.hasNext()) {
			nbaDst = (NbaDst) it.next();
			unlockWork(getUser(), nbaDst);  //NBA213
		}
		//NBA213 deleted code
    }

    /**
     * Retrieve and lock the Case work items associated with the permanent Transaction work items.
     * @param permWorkItems
     * @throws NbaAWDLockedException if the case work item cannot be locked.
     * @throws NbaBaseException with the text "Unable to route parent case to underwriter queue, reason: " if any other error occurs to cause the
     *         original work item to be routed to an error queue or to cause the automated process to stop if the error is fatal
     */
    // SPR3009 New Method
    protected void retrieveCaseWorkItems(ArrayList permWorkItems) throws NbaBaseException {
        ListIterator it = permWorkItems.listIterator();
        NbaDst tranDst;
        NbaDst caseDst;
        NbaAwdRetrieveOptionsVO retOpt;
        while (it.hasNext()) {
            tranDst = (NbaDst) it.next();   
            retOpt = new NbaAwdRetrieveOptionsVO();
            retOpt.setWorkItem(tranDst.getID(), false);
            retOpt.requestCaseAsParent();
            retOpt.setLockParentCase();
            retOpt.setLockTransaction();
            try {
                caseDst = retrieveWorkItem(getUser(), retOpt);  //NBA213
            } catch (NbaAWDLockedException e) {
                throw e;	//caller will handle case lock error
            } catch (NbaBaseException e) {
                if (e.isFatal()) {
                    throw e;
                }
                getLogger().logException(e);
                throw new NbaBaseException("Unable to retrieve case work item, reason: " + e.getMessage(), e);
			//NBA213 deleted code
            }
            if (!getCaseWorkItems().containsKey(caseDst.getID())) { 
                caseDst.getWorkItem().setWorkItemChildren(new ArrayList());	//Clear children so unlock does not unlock them when it unlocks the Case //NBA213 
                getCaseWorkItems().put(caseDst.getID(), caseDst);
                updateCaseWithNewStatus(caseDst,tranDst); //NBLXA -1983
            }
        }
    }

    /**
     * If the case is in the underwriter hold queue, update the status to route it back to the underwriter. 
     * @param caseDst
     * @throws NbaBaseException with the text "Unable to retrieve new status for case work item, reason: " if an error occurs. This causes the
     *         original work item to be routed to an error queue or the automated process to stop if the error is fatal
     */
    // SPR3009 New Method
    protected void updateCaseWithNewStatus(NbaDst caseDst,NbaDst tranDst) throws NbaBaseException { //NBLXA -1983
    	try {
    		if (NbaConstants.A_QUEUE_UNDERWRITER_HOLD.equals(caseDst.getQueue())) {
    			//Start NBLXA -1983
    			/* Reinsurance Responses are bundled unless and until all the responses are received on the case. But if response for additional info is received case route to UW */
    			boolean isFinalReplRecieve= true; 
    			if(((!NbaUtils.isBlankOrNull(tranDst.getNbaLob().hasAdditionalInfo()) && (!tranDst.getNbaLob().getAdditionalInfo()) ) ||
    					NbaUtils.isBlankOrNull(tranDst.getNbaLob().hasAdditionalInfo()))){
    				setNbaTxLife(doHoldingInquiry(caseDst));
    				if(!NbaUtils.isBlankOrNull(getNbaTxLife())){
    					Long appType=getNbaTxLife().getPolicy().getApplicationInfo().getApplicationType();
    					ApplicationInfo appInfo = getNbaTxLife().getPolicy().getApplicationInfo();
    					ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
    					if( NbaOliConstants.OLI_ACTTYPE_1000500003 !=appType && NbaOliConstants.OLI_APPTYPE_SIMPLIFIEDISSUE !=appType 
    							&& (!NbaUtils.isBlankOrNull(appInfoExt) && !NbaUtils.isContractChange(getNbaTxLife()) && NbaUtils.isBlankOrNull(appInfoExt.getReopenDate()))){
    						NbaDst parentCase = retrieveParentWork(tranDst, true,true);
    						List transactions = parentCase.getNbaTransactions();
    						NbaTransaction transaction = null;  		
    						for (int i = 0; i < transactions.size(); i++) {
    							transaction = (NbaTransaction) transactions.get(i);
    							if (!tranDst.getID().equalsIgnoreCase( transaction.getID()) && NbaConstants.A_WT_REINSURANCE.equals(transaction.getWorkType()) && !transaction.isInEndQueue()) {
    								isFinalReplRecieve =false;
    								break;
    							}
    						}
    					}
    				}
    			}
    			if(isFinalReplRecieve){
    				//End NBLXA -1983
    				updateCaseStatus(caseDst);
    			}
    			//NBA208-32
    			caseDst.getCase().getWorkItemChildren().clear(); //clear child transaction
    			caseDst.setUpdate();
    		}
    	}catch (NbaBaseException e) {
    		if (e.isFatal()) {
    			throw e;
    		}
    		getLogger().logException(e);
    		throw new NbaBaseException("Unable to retrieve new status for case work item, reason: " + e.getMessage(), e);
    	}
    }

    /**
     * Call NbaWorkCompleteStatusProvider which invokes a VPMS model to calculate a case status and the priority based on the UNDQ LOB value of case.
     * The case status and priority is then updated with the status and priority.
     * @param nbaDst
     * @throws NbaBaseException
     */
    // SPR3009 New Method
    protected void updateCaseStatus(NbaDst nbaDst) throws NbaBaseException {
        NbaProcessWorkCompleteStatusProvider workCompleteStatusProvider = new NbaProcessWorkCompleteStatusProvider(getUser(), nbaDst.getNbaLob(),
                new HashMap());
        nbaDst.setStatus(workCompleteStatusProvider.getStatus());
        setRouteReason(nbaDst, nbaDst.getStatus(), workCompleteStatusProvider.getReason());//ALS5260
        nbaDst.getNbaLob().setUnderwriterActionLob(NbaOliConstants.OLI_UW_SUBSEQUENT_ACTION);//APSL4981
		NbaUtils.addGeneralComment(nbaDst,getUser(),workCompleteStatusProvider.getReason());//ALS5260,ALS5337
        //Increase the priority of case.
        String action = workCompleteStatusProvider.getPriorityAction();
        String value = workCompleteStatusProvider.getPriorityValue();
        if (action != null && action.trim().length() > 0 && value != null && value.trim().length() > 0) {
            nbaDst.increasePriority(workCompleteStatusProvider.priorityAction, workCompleteStatusProvider.priorityValue);
        }
    }
    /**
     * Handle an exception which occurred while retrieving or processing the work items. 
     * (1) Unlock any locked work items retrieved after the getwork(). 
     * (2) If a lock exception occurred, exit. The caller will suspend the getwork() item. 
     * (3) If a fatal exception occurred, exit. The caller will stop the automated process. 
     * (4) If a result has not been created, add a comment containing the exception message, and set the work
     * item status to the host errored status. If a result is already present, the exception has already been intercepted and the work item has been
     * updated with an appriopriate status and comment. 
     * (5) Update the work item. If this fails, throw a fatal exception to cause the caller to stop
     * the automated process.
     * @param e
     * @throws NbaBaseException
     */
    // SPR3009 New Method    
    protected void handleProcessingException(Exception e) throws NbaBaseException {
        unLockItems();
        if (e instanceof NbaBaseException) {
            NbaBaseException eb = (NbaBaseException) e;
            if (eb.isFatal() || eb instanceof NbaLockedException) {
                throw eb;
            }
        }
        if (result == null) {
            setWork(getOrigWorkItem());
            addComment(e.getMessage());
            changeStatus(getHostErrorStatus());
            setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, getHostErrorStatus(), getHostErrorStatus()));
        }
        try {
            doUpdateWorkItem();
        } catch (NbaBaseException e1) {
            e1.forceFatalExceptionType();
            throw e1;
        }
    }
    /**
     * Handle an exception which occurred while updating the work items. 
     * (1) Add a comment containing "An error occurred while comitting workflow changes" and the exception message to the original work item, 
     * and set the work item status to the AWD errored status.
     * (2) Update the original work item. If this fails, throw a fatal exception to cause the caller to stop the automated process.
     * @param e
     * @throws NbaBaseException
     */
    // SPR3009 New Method    
    protected void handleUpdateException(Exception e) throws NbaBaseException {
        setWork(getOrigWorkItem());
        addComment("An error occurred while committing workflow changes " + e.getMessage());
        changeStatus(getAwdErrorStatus());
        try {
            doUpdateWorkItem();
        } catch (NbaBaseException e1) {
            e1.forceFatalExceptionType();
            throw e1;
        }
        setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, getAwdErrorStatus(), getAwdErrorStatus()));
    }

}
