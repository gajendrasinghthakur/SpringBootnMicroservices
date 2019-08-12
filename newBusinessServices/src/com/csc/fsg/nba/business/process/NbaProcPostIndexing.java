package com.csc.fsg.nba.business.process;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.StandardAttr;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * Perform the Post indexing business process:   processes the SBQTMAIL work item and sources
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>CRSBQTMAIL</td><td>Version 8</td><td>subsequent mail WI to be split into individual work items</td></tr>
 *  </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class NbaProcPostIndexing extends NbaAutomatedProcess {
	private List unlockWorkItems = new ArrayList();
	private boolean invalidSourceAttached; //APSL3825
	
	/**
	 * NbaProcPostIndexing constructor comment.
	 */
	public NbaProcPostIndexing() {
		super();
	}

	/**
	 * Perform the Post indexing business process: - Retrieves the SBQTMAIL work item and sources. - creates new work items 
	 * for each source attached to the work item. 
	 * @param user the NbaUser for whom the process is being executed
	 * @param work a NbaDst value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		// Initialization
		if (!initialize(user, work)) {
			return getResult();
		}
		List sources = getWorkItemWithSources();
		int sourceCount = sources.size();
		setInvalidSourceAttached(false); //APSL3825
		for (int i = 0; i < sourceCount; i++) {
			NbaSource source = (NbaSource) sources.get(i);
			createNewTransactionFromSource(source);
		}
		unlockAllWorkItems();
		//Begin APSL3825
		if(isInvalidSourceAttached()){
			updateWork(getUser(), getWork());
			//APSL3874 code deleted
	    	throw new AxaErrorStatusException(AxaStatusDefinitionConstants. VARIANCE_KEY_TECH_INVALID_AWD); 
		}
		//End APSL3825
		changeStatus(getPassStatus());
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		doUpdateWorkItem(); 
		return getResult();
	}

	/**
	 * returns new transaction with the source passed to it. 
	 * @throws NbaBaseException
	 */
	protected void createNewTransactionFromSource(NbaSource nbaSrc) throws NbaBaseException {
		NbaDst nbaTransaction = createTransaction(nbaSrc);
		if (nbaTransaction != null){ //APSL3825
			NbaSource newSource = nbaTransaction.addNbaSource(nbaSrc);
			newSource.setUpdate();
			nbaSrc.setBreakRelation();
			nbaTransaction = WorkflowServiceHelper.updateWork(getUser(), nbaTransaction);
			getUnlockWorkItems().add(nbaTransaction);
		}
	}

	/**
	 * returns new transaction with the source passed to it. 
	 * @return NbaDst
	 */
	protected NbaDst createTransaction(NbaSource nbaSrc) throws NbaBaseException {
		NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), nbaSrc.getNbaLob(), nbaSrc.getSource().getSourceType()); 
		NbaDst nbaDst = null;
		if (provider != null && provider.getWorkType() != null && provider.getInitialStatus() != null) {
			WorkItem transaction = new WorkItem();
			transaction.setBusinessArea(NbaConstants.A_BA_NBA);
			transaction.setLock("Y");
			transaction.setAction("L");
			transaction.setWorkType(provider.getWorkType());
			transaction.setStatus(provider.getInitialStatus());
			transaction.setCreate("Y");
			transaction.setUpdate("Y");
			nbaDst = new NbaDst();
			nbaDst.setUserID(user.getUserID());
			nbaDst.setPassword(user.getPassword());
			nbaDst.addTransaction(transaction);
			nbaDst.increasePriority(provider.getWIAction(), provider.getWIPriority());
			List lobList = getLobsToCopy(provider.getWorkType(), nbaSrc);
			nbaSrc.getNbaLob().copyLOBsTo(nbaDst.getNbaLob(), lobList);
		} else {
			setInvalidSourceAttached(true); //APSL3825
			//APSL3825 code moved in calling method
		}
		return nbaDst;
	}
	
	/**
	 * returns list of sources with the workitem passed 
	 * @return list
	 */
	protected List getWorkItemWithSources() throws NbaBaseException{
		NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
		retrieveOptionsValueObject.setWorkItem(getWork().getID(), false);
		retrieveOptionsValueObject.requestSources();
		retrieveOptionsValueObject.setLockWorkItem();
		setWork(retrieveWorkItem(getUser(), retrieveOptionsValueObject)); 
		return getWork().getNbaSources();
	}

	/**
	 * Creates a list of work items to be unlocked, and then calls NbaUnlockWorkBP 
	 * business service to unlock the work item(s).
	 * @return
	 */
	protected void unlockAllWorkItems(){
		if(! NbaUtils.isBlankOrNull(getUnlockWorkItems())){
			currentBP.callBusinessService("NbaUnlockWorkBP", getUnlockWorkItems());
		}
	}
	
	/**
	 * @return Returns the unlockWorkItems.
	 */
	public List getUnlockWorkItems() {
		return unlockWorkItems;
	}
	/**
	 * @param unlockWorkItems The unlockWorkItems to set.
	 */
	public void setUnlockWorkItems(List unlockWorkItems) {
		this.unlockWorkItems = unlockWorkItems;
	}
	
    /**
     * This method returns data elements to be copied 
     * @param workType the work type of the work DST
     * @param sourceAssociatedWithWork LOB data of current source   
     * @param workLob LOB data of original work item
     * @throws NbaBaseException
     */
    protected List getLobsToCopy(String workType, NbaSource source) throws NbaBaseException {
		List lobList = new ArrayList();
		String lobs = processRules(workType, source);
		NbaStringTokenizer lobNames = null;
		if (!NbaUtils.isBlankOrNull(lobs)) {
			lobNames = new NbaStringTokenizer(lobs, NbaVpmsConstants.VPMS_DELIMITER[0]);
			int nextToken = 0;
			while (lobNames.hasMoreTokens()) {
				lobList.add(nextToken, lobNames.nextToken());
				nextToken++;
			}
		}
		return lobList;
	}


	/**
	 * Call Indexing VP/MS model
	 * @param workType
	 * @param NbaSource
	 * @param entryPoint
	 * @return String result string
	 * @throws NbaBaseException
	 */
	protected String processRules(String workType, NbaSource source) throws NbaBaseException {
	    NbaVpmsAdaptor rulesProxy = null;
	    String returnStr = "";
		try {
			Map deOinkMap = new HashMap(2, 1);
	        deOinkMap.put("A_SourceTypeLOB", source.getSourceType());
	        deOinkMap.put("A_WorkTypeLOB", workType);
			NbaOinkDataAccess data = new NbaOinkDataAccess(source.getNbaLob());
			rulesProxy = new NbaVpmsAdaptor(data, NbaVpmsConstants.INDEX);
			rulesProxy.setSkipAttributesMap(deOinkMap);
			rulesProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_INDEX_PARENT_WORK_ITEM);
			
			// get the string out of XML returned by VP / MS Model and parse it to create the object structure
			VpmsComputeResult rulesProxyResult = rulesProxy.getResults();
			if (!rulesProxyResult.isError()) {
			    NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(rulesProxyResult);
			    List rulesList =  vpmsResultsData.getResultsData();
			    if (!rulesList.isEmpty()) {
					String xmlString = (String) rulesList.get(0);
					NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
					VpmsModelResult vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
					List strAttrs = vpmsModelResult.getStandardAttr();
					
					//Generate delimited string if there are more than one parameters returned
					returnStr = generateDelimitedString(strAttrs);
			    }
			}
			return returnStr;
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} finally {
			if (rulesProxy != null) {
				try {
					rulesProxy.remove();
				} catch (RemoteException re) {
				    LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
				}
			}
		}
	}    
    
    /**
     * Generate delimited string if there are more than one parameters returned in the List strAttrs
     * @param returnStr the 
     * @param strAttrs attribute list
     */
    protected String generateDelimitedString(List strAttrs) {
        StringBuffer returnStr = new StringBuffer();
        StandardAttr attr = null;
        int i = 0;
        for (Iterator itr = strAttrs.iterator(); itr.hasNext(); i++) {
            attr = (StandardAttr) itr.next();
            if (i > 0) {
                returnStr.append(NbaVpmsAdaptor.VPMS_DELIMITER[0]);
            }
            returnStr.append(attr.getAttrValue());
        }
        return returnStr.toString();
    }

	/**
	 * @return the invalidSourceAttached
	 */
    //APSL3825 New Method
	public boolean isInvalidSourceAttached() {
		return invalidSourceAttached;
	}

	/**
	 * @param invalidSourceAttached
	 */
	//APSL3825 New Method
	public void setInvalidSourceAttached(boolean invalidSourceAttached) {
		this.invalidSourceAttached = invalidSourceAttached;
	} 
    
    
}

