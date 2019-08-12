package com.csc.fsg.nba.process.images;

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
 * *******************************************************************************<BR>
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.result.RetrieveWorkResult;
import com.csc.fs.accel.valueobject.RetrieveWorkItemsRequest;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fs.accel.workflow.process.RetrieveHierarchyBP;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaViewImagesVO;

/**
 * Calls the RetrieveHierarchyBP to retrieve the hierarchy of workitem. Then calls the ImageIdentificationBP 
 * to get the descriptive information about the workitems.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA227</td><td>Version 8</td><td>Selection List of Images to Display</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
/*
 * All AXA Code has been moved to com.axa.fsg.nba.process.images.ListImagesBP
 */
public class ListImagesBP extends RetrieveHierarchyBP {
    
    protected final Object CASERECORDTYPE = "C";
	protected final Object TRANSACTIONRECORDTYPE = "T";

	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			NbaViewImagesVO viewImages = (NbaViewImagesVO) input;
			AccelResult hierarchyresult = retrieveWorkItems(viewImages);
			if(hierarchyresult.hasErrors()) {
				return hierarchyresult;
			}
			organizeAllSources(viewImages, (WorkItem)hierarchyresult.getFirst());
			result.addResult(viewImages);
		} catch (Exception e) {
			addExceptionMessage(result, e);
		}
		return result;
	}    

    /**
     * Retrieve the sources and children sources if requested bye calling the process method of RetrieveHierarchyBP
     * @param viewImages - value object
     * @return - AccelResult 
     */
    protected AccelResult retrieveWorkItems(NbaViewImagesVO viewImages) {
    	AccelResult hierarchyresult;
    	WorkItem workItem = viewImages.getWorkItem();
		//only process cases that need their children
		if ((workItem.isCase() && !viewImages.isIncludeTransactions()) || workItem.isTransaction()) {
			hierarchyresult = new AccelResult();
			hierarchyresult.addResult(workItem);
			return hierarchyresult;
		}
		RetrieveWorkItemsRequest request = new RetrieveWorkItemsRequest();
		request.setUserID(getCurrentUserId());
		request.setWorkItemID(workItem.getItemID());
		request.setSourcesIndicator(true);
		request.setTransactionAsChildIndicator(true);
		hierarchyresult = (AccelResult) super.process(request);
		if (hierarchyresult.hasErrors()) {
			return hierarchyresult;
		}
		RetrieveWorkResult retrieveWorkResult = (RetrieveWorkResult) hierarchyresult;
		List transactions = new ArrayList();
		List items = retrieveWorkResult.getWorkItems();
		WorkItem tempWorkItem = null;
		WorkItem wi;
		for (int i = 0; i < items.size(); i++) {
			wi = (WorkItem) items.get(i);
			if (wi.getRecordType().equals(CASERECORDTYPE)) {
				tempWorkItem = wi;
			} else if (wi.getRecordType().equals(TRANSACTIONRECORDTYPE)) {
				transactions.add(wi);
			}
		}
		if (tempWorkItem != null) {
			tempWorkItem.getWorkItemChildren().addAll(transactions);
		} else if (!transactions.isEmpty()) {
			tempWorkItem = ((WorkItem) transactions.get(0));
		}
		hierarchyresult.addResult(tempWorkItem);
		return hierarchyresult;
	}

	/**
	 * Retrieve Sources for the Case and its Transactions. This function then calls ImageIdentificationBP to
	 * get the descriptive information about the workitems.
	 * @param viewImages - value object
	 * @param workItem - workitem object
	 * @throws NbaBaseException
	 */
	protected void organizeAllSources(NbaViewImagesVO viewImages, WorkItem workItem) throws NbaBaseException {
		Map primaryInsuredSourceMap = new HashMap();
		NbaViewImagesVO viewImagesVo;
		List transactionSourceList = new ArrayList();
		Map transactionSourceMap = new HashMap();
		if (workItem.isCase()) {
			List caseSources = workItem.getSourceChildren();
			if (viewImages.isIncludeTransactions()) {
				List transactions = workItem.getWorkItemChildren();
				int transCount = transactions.size();
				WorkItem currentTransaction;
				for (int i = 0; i < transCount; i++) {
					currentTransaction = (WorkItem) transactions.get(i);
					addSourcesFromTransaction(currentTransaction, caseSources, viewImages, primaryInsuredSourceMap, transactionSourceList,
							transactionSourceMap);
				}
			}
			viewImagesVo = prepareRequest(caseSources, primaryInsuredSourceMap, transactionSourceList, transactionSourceMap, workItem);
		} else {
			List transactionSource = addSourcesFromTransaction(workItem, viewImages.isIncludeSensitiveImages());
			viewImagesVo = prepareRequest(transactionSource, primaryInsuredSourceMap, transactionSourceList, transactionSourceMap, workItem);
		}
		AccelResult hierarchyResult = (AccelResult) callBusinessService("ImageIdentificationBP", viewImagesVo);
		if (!hierarchyResult.hasErrors()) {
			viewImagesVo = (NbaViewImagesVO) hierarchyResult.getFirst();
			viewImages.setSourcesList(viewImagesVo.getSourcesList());
			viewImages.setWorkItemDescMap(viewImagesVo.getWorkItemDescMap());
		}		
	}
	
	/**
	 * Retrieves sources which are not present in the case. It also retrieves their associated workitem. if the same source is present in more than
	 * one transaction workitem then the workitem retrieved will be the one which is associated with primary insured.
	 * @param workItem - transaction Workitem
	 * @param caseSources - List of sources which are present in case.
	 * @param viewImages - value object.
	 * @param primaryInsuredSourceMap - map of sourceids and workitems which are associated with primary insured
	 * @param transactionSourceList - List of sources associated with transaction workitems
	 * @param transactionSourceMap - map of sourceids and workitems which are not associated with primary insured
	 * @throws NbaBaseException
	 */
	protected void addSourcesFromTransaction(WorkItem workItem, List caseSources, NbaViewImagesVO viewImages, Map primaryInsuredSourceMap,
			List transactionSourceList, Map transactionSourceMap) throws NbaBaseException {
		List transSources = workItem.getSourceChildren();
		int transSourceCount = transSources.size();
		String sourceId = null;
		NbaSource currentNbaSource;
		WorkItemSource currentSource;
		NbaLob nbaLob = new NbaLob(workItem.getLobData());
		for (int j = 0; j < transSourceCount; j++) {
			currentSource = (WorkItemSource) transSources.get(j);
			sourceId = currentSource.getItemID();
			currentNbaSource = new NbaSource(currentSource);
			if (viewImages.isIncludeSensitiveImages() || (!(nbaLob.getReqMedicalType() && currentNbaSource.isProviderResult()))) {
				if (!isSourcePresent(caseSources, sourceId)) {
					if (isSourcePresent(transactionSourceList, sourceId) && isPrimaryInsuredTransaction(nbaLob, viewImages.getPrimaryInsuredSSN())) {
						//if source is present in the list and the workitem is assoicated with primary insured.
						// Then check if the source is present in the primaryInsuredSourceMap. if not then update the primaryInsuredSourceMap with
						// source id and workitem id
						if (!primaryInsuredSourceMap.containsKey(sourceId)) {
							primaryInsuredSourceMap.put(sourceId, workItem);
							transactionSourceMap.remove(sourceId);
						}
					} else if (!isSourcePresent(transactionSourceList, sourceId)) {
						transactionSourceList.add(currentSource);
						if (isPrimaryInsuredTransaction(nbaLob, viewImages.getPrimaryInsuredSSN())) {
							//if source is not present in the transactionSourceList and the workitem is assoicated with primary insured.
							// then sourceId and workitemid to map.							
							primaryInsuredSourceMap.put(sourceId, workItem);
						} else {
							transactionSourceMap.put(sourceId, workItem);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Prepares the request to be sent to ImageIdentificationBP.
	 * @param caseSources - List of sources assoicated with case.
	 * @param primaryInsuredSourceMap - map of sourceids and workitems which are associated with primary insured
	 * @param transactionSourceList - List of sources associated with transaction workitems
	 * @param transactionSourceMap -  map of sourceids and workitems which are not associated with primary insured
	 * @param workItem - case workitem
	 */
	protected NbaViewImagesVO prepareRequest(List caseSources, Map primaryInsuredSourceMap, List transactionSourceList, Map transactionSourceMap,
			WorkItem workItem) {
		WorkItemSource source;
		if (!caseSources.isEmpty()) {
			int count = caseSources.size();
			for (int i = 0; i < count; i++) {
				source = (WorkItemSource) caseSources.get(i);
				primaryInsuredSourceMap.put(source.getItemID(), workItem);
			}
		}
		if (!transactionSourceList.isEmpty()) {
			caseSources.addAll(transactionSourceList);
			primaryInsuredSourceMap.putAll(transactionSourceMap);
		}
		NbaViewImagesVO viewImagesVO = new NbaViewImagesVO();
		viewImagesVO.setSourcesList(caseSources);
		viewImagesVO.setWorkItemMap(primaryInsuredSourceMap);
		return viewImagesVO;
	}
	
	/**
	 * This function checks if the workitem is associated with primary insured
	 * @param nbaLob - lobs of transaction workitem.
	 * @param govtId - govtId of primary insured.
	 * @return
	 */
	protected boolean isPrimaryInsuredTransaction(NbaLob nbaLob, String govtId) {
		String ssn = nbaLob.getSsnTin();
		if (ssn != null) {
			if (ssn.equals(govtId)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if source is present in the list.
	 * @param sources - the Sources list
	 * @param sourceId - Source id.
	 */
	protected boolean isSourcePresent(List sources, String sourceId) {
		WorkItemSource source;
		if (!sources.isEmpty()) {
			int count = sources.size();
			for (int i = 0; i < count; i++) {
				source = (WorkItemSource) sources.get(i);
				if (sourceId.equals(source.getItemID())) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Add Sources for Transaction. Sensitive provider results are only included if requested. These
	 * are determined by interrogating the medical indicator (LOB RQMD) on an NBPROVRSLT work item.
	 * If RQMD = 1 (true) this is a medical requirement and if RQMD = 0 (false) this is not a medical
	 * requirement. 
	 * @param nbaWorkItem - the Transaction work item
	 * @param includeSensitiveSources - boolean value indicating whether to include sensitive sources
	 * @throws NbaBaseException
	 */
	protected List addSourcesFromTransaction(WorkItem workItem, boolean includeSensitiveSources) throws NbaBaseException {
		List transSources = workItem.getSourceChildren();
		int transSourceCount = transSources.size();
		List sources = new ArrayList(transSourceCount);
		NbaSource currentNbaSource;
		WorkItemSource currentSource;
		NbaLob nbaLob = new NbaLob(workItem.getLobData());
		for (int j = 0; j < transSourceCount; j++) {
			currentSource = (WorkItemSource) transSources.get(j);
			currentNbaSource = new NbaSource(currentSource);
			if (includeSensitiveSources) {
				sources.add(currentSource);
			} else if (!(nbaLob.getReqMedicalType() && currentNbaSource.isProviderResult())) { //Skip medical provider results
				sources.add(currentSource);
			}
		}
		return sources;
	}
}
