package com.csc.fsg.nba.process.search;

/* 
 * *******************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.result.RetrieveWorkResult;
import com.csc.fs.accel.valueobject.SearchWorkRequest;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fsg.nba.exception.NbaNetServerException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaCase;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaLookupResultVO;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaWorkItem;

/**
 * Performs a workflow search with criteria specified in a <code>NbaSearchVO</code> value object.
 * <p>
 * The <code>AccelResult</code> returned includes the input <code>NbaSearchVO</code> with a
 * populated list of <code>NbaSearchResultVO</code> value objects for each work item.  If no work
 * items match the criteria, the SearchResults property will include an empty list.
 * <p>
 * The maximum number of work items returned is configurable thru the NbaConfiguration.xml
 * MaxRecords.NetServerMaxRecords element.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA136</td><td>Version 6</td><td>In Tray and Search Rewrite</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-17</td><td>Version 7</td><td>Performance Tuning and Testing - Incremental change 17</td></tr> 
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPR3344</td><td>Version 8</td><td>Searches Using the Suspend Indicator Not Allowed</td></tr>
 * <tr><td>ALS4729</td><td>AXALife Phase 1</td><td>QC # 3860 - "No Match Found" message gets displayed for the policy where the data is available in nbA</td></tr>
 * <tr><td>APSL5055-NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 extends NewBusinessAccelBP
public class SearchWorkflowBP extends NewBusinessAccelBP {

    protected final static String DEFAULT_SEARCH_RESULT_CLASS = NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS;  //NBA213
    protected final static String VO_PACKAGE = "com.csc.fsg.nba.vo.";  //NBA213

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
		//begin NBA213
			NbaSearchVO searchVO = (NbaSearchVO) input;
			//NBA213 code deleted
	        int maxRecord = NbaConfiguration.getInstance().getMaxRecords().getNetServerMaxRecords();
	        SearchWorkRequest serviceObject = new com.csc.fs.accel.valueobject.SearchWorkRequest();
	        NbaLob nbaLobTemp = searchVO.getNbaLob();
	        serviceObject.setBusinessArea(searchVO.getBusinessArea());
	        serviceObject.setOperand(searchVO.getOperand()); //NBLXA-1379
	        serviceObject.setQueueID(searchVO.getQueue());
	        serviceObject.setStatus(searchVO.getStatus());
	        serviceObject.setWorkType(searchVO.getWorkType());
	        serviceObject.setSourceType(searchVO.getSourceType());
	        serviceObject.setBeginDateTime(searchVO.getFromDateTimeStamp());
	        serviceObject.setEndDateTime(searchVO.getToDateTimeStamp());
	        serviceObject.setMaxRecords(maxRecord);
	        serviceObject.setPageNumber("1");  //APSL5055-NBA331
	        
	        if (nbaLobTemp == null) {
	            nbaLobTemp = new NbaLob();
	        }
	        if (searchVO.getCompanyCode() != null) {
	            nbaLobTemp.setCompany(searchVO.getCompanyCode());
	        }
	        if (searchVO.getContractNumber() != null) {
	            nbaLobTemp.setPolicyNumber(searchVO.getContractNumber());
	        }
	        if (searchVO.getGovtID() != null) {
	            nbaLobTemp.setSsnTin(searchVO.getGovtID());
	        }
	        if (searchVO.getLastName() != null) {
	            nbaLobTemp.setLastName(searchVO.getLastName());
	        }
	        if (searchVO.getFirstName() != null) {
	            nbaLobTemp.setFirstName(searchVO.getFirstName());
	        }
	        if (searchVO.getMiddleInitial() != null) {
	            nbaLobTemp.setMiddleInitial(searchVO.getMiddleInitial());
	        }
	        if(searchVO.getSuspendedFlag()!= null) { //SPR3344
	        	nbaLobTemp.setSuspendedFlag(searchVO.getSuspendedFlag()); //SPR3344
	        } //SPR3344
	        serviceObject.setLobData(getFormattedLobList(nbaLobTemp));
	        AccelResult wfsearchresult = (AccelResult)callBusinessService("SearchFullWorkBP", serviceObject);
	        if (wfsearchresult.hasErrors()) {  //ALS4729
	        	return wfsearchresult;  //ALS4729
	        }  //ALS4729
	        
            if (!wfsearchresult.hasErrors() && wfsearchresult instanceof RetrieveWorkResult) {
                RetrieveWorkResult retrieveWorkResult = (RetrieveWorkResult) wfsearchresult;
                List workItems = retrieveWorkResult.getWorkItems();
                List list = new ArrayList();
                searchVO.setSearchResults(list);
                NbaLookupResultVO resultVO;
                //	setup so can use reflection to get the result class
                String resultClassName = searchVO.getResultClassName();
                if (resultClassName == null) {
                    resultClassName = DEFAULT_SEARCH_RESULT_CLASS;
                }
                resultClassName = VO_PACKAGE + resultClassName;
                Constructor srchRsltConstructor;
                Object[] srchRsltArgs;
                Object object;
                try {
                    Class srchRsltClass = NbaUtils.classForName(resultClassName);
                    Class[] srchRsltArgsClass = null;
                    if (searchVO.getSourceType() != null) {
                        srchRsltArgsClass = new Class[] { NbaSource.class };
                    } else {
                        srchRsltArgsClass = new Class[] { NbaWorkItem.class };
                    }
                    srchRsltConstructor = srchRsltClass.getConstructor(srchRsltArgsClass);
                } catch (Throwable e) {
                    throw new NbaNetServerException(NbaNetServerException.LOOKUP, e);
                }
                int workItemsSize = workItems.size();
                searchVO.setMaxResultsExceeded(workItemsSize > maxRecord);
                for (int i = 0; i < workItemsSize; i++) {
                    WorkItem workitem = (WorkItem) workItems.get(i);
                    if (CASERECORDTYPE.equals(workitem.getRecordType())) { // process Cases
                        NbaCase nbaCase = new NbaCase((WorkItem) workItems.get(i));  //NBA208-32
                        try {
                            //create the search result from the Case
                            srchRsltArgs = new Object[] { nbaCase };
                            object = srchRsltConstructor.newInstance(srchRsltArgs);
                            resultVO = (NbaLookupResultVO) object;
                        } catch (Throwable e) {
                            throw new NbaNetServerException(NbaNetServerException.LOOKUP, e);
                        }
                        if (resultVO.isActionAdd()) { //valid result: reset the action indicator and add to the return list
                            resultVO.getActionIndicator().setAction(null);
                            //NBA208-36
                            if(resultVO instanceof NbaSearchResultVO){
                            	((NbaSearchResultVO)resultVO).setWorkItem(workitem);
                            }
                            list.add(resultVO);
                        }
                    } else if (TRANSACTIONRECORDTYPE.equals(workitem.getRecordType())) { // process Transactions
                        NbaTransaction nbaTransaction = new NbaTransaction((WorkItem) workItems.get(i));  //NBA208-32
                        try {
                            //create the search result from the Transaction
                            srchRsltArgs = new Object[] { nbaTransaction };
                            object = srchRsltConstructor.newInstance(srchRsltArgs);
                            resultVO = (NbaLookupResultVO) object;
                        } catch (Throwable e) {
                            throw new NbaNetServerException(NbaNetServerException.LOOKUP, e);
                        }
                        if (resultVO.isActionAdd()) { //valid result: reset the action indicator and add to the return list
                            resultVO.getActionIndicator().setAction(null);
							//NBA208-36
                            if(resultVO instanceof NbaSearchResultVO){
                            	((NbaSearchResultVO)resultVO).setWorkItem(workitem);
                            }
                            list.add(resultVO);
                        }
                    } else if (SOURCERECORDTYPE.equals(workitem.getRecordType())) { // process Sources
                        Iterator sourcesItr = workitem.getSourceChildren().iterator();  //NBA208-32
                        while (sourcesItr.hasNext()) {
                            WorkItemSource source = (WorkItemSource) sourcesItr.next();  //NBA208-32
                            NbaSource nbaSource = new NbaSource(source);
                            try {
                                //create the search result from the Source
                                srchRsltArgs = new Object[] { nbaSource };
                                object = srchRsltConstructor.newInstance(srchRsltArgs);
                                resultVO = (NbaLookupResultVO) object;
                            } catch (Throwable e) {
                                throw new NbaNetServerException(NbaNetServerException.LOOKUP, e);
                            }
                            if (resultVO.isActionAdd()) {//valid result: reset the action indicator and add to the return list
                                resultVO.getActionIndicator().setAction(null);
                                list.add(resultVO);
                            }
                        }
                    }
                }
                if (searchVO.isWorkItemIdentificationNeeded()) {
                    List results = searchVO.getSearchResults();
                    //begin NBA208-17
                    NbaSearchResultVO[] searchResults = new NbaSearchResultVO[results.size()];
                    searchResults = (NbaSearchResultVO[])results.toArray(searchResults);
                    Result wiresult = callBusinessService("WorkItemIdentificationBP", searchResults);
                    Map wiMap = null;
                    if (!wiresult.hasErrors()) {
                        wiMap = ((Map) wiresult.getFirst());
                    }
                    int count = results.size();
                    for (int i = 0; i < count; i++) {
                        NbaSearchResultVO searchResult = (NbaSearchResultVO) results.get(i);
                        if (!wiresult.hasErrors()) {
                            searchResult.setRouteReason((String)wiMap.get(searchResult.getWorkItemID()));
                        }
                    }
                    //end NBA208-17
                }
                result.addResult(searchVO);
            }
		} catch (Exception e) {
			addExceptionMessage(result, e);
		}
		//end NBA213
		return result;
	}

	//NBA213 deleted code
}
