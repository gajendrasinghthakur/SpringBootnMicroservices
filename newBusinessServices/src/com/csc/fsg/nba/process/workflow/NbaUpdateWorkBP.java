package com.csc.fsg.nba.process.workflow;

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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.result.RetrieveWorkResult;
import com.csc.fs.accel.valueobject.Comment;
import com.csc.fs.accel.valueobject.CompleteWorkRequest;
import com.csc.fs.accel.valueobject.SourceItem;
import com.csc.fs.accel.valueobject.SourceItemRequest;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemRequest;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.correspondence.NbaCorrespondenceEvents;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaNetServerException;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaPrintLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.nbproducer.NbaNbproducerEvents;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaCase;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * Updates a work item using the <code>NbaNetServerAccessor</code>.  Requires an <code>NbaDst</code>
 * as input.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>SPR3304</td><td>Version 7</td><td>NBREQRCTL Sources are duplicated in the response from a workflow update</td></tr>
 * <tr><td>NBA188</td><td>Version 7</td><td>nbA XML Sources to Auxiliary</td></tr>
 * <tr><td>SPR3313</td><td>Version 7</td><td>Priority Selected on Original Create Work View Does Not Set Work Item Prirority Value in</td></tr> 
 * <tr><td>SPR3323</td><td>Version 7</td><td>Create Relationship does not work for a Case</td></tr>
 * <tr><td>SPR3332</td><td>Version 7</td><td>Images for Sources are not being stored correctly in AWD</td></tr>
 * <tr><td>NBA212</td><td>Version 7</td><td>Content Services</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-1 </td><td>Version 7</td><td>Performance Tuning and Testing - Deferred History Retrieval</td></tr>
 * <tr><td>NBA180</td><td>Version 7</td><td>Contract Copy Rewrite</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>NBA208-38</td><td>Version 7</td><td>Performance Tuning and Testing - Comments in Database</td></tr> 
 * <tr><td>NBA229</td><td>Version 8</td><td>nbA Work List and Search Results Enhancement</td></tr>
 * <tr><td>AXAL3.7.20</td><td>AXA Life Phase 1</td><td>Workflow</td></tr>
 * <tr><td>ALPC137</td><td>AXA Life Phase 1</td><td>Miscellaneous Mail</td></tr>
 * <tr><td>ALPC185</td><td>AXA Life Phase 1</td><td>RGA ASAP</td></tr>
 * <tr><td>AXAL3.7.20R</td><td>AXA Life Phase 1</td><td>Replacement Workflow</td>
 * <tr><td>ALS4283</td><td>AXA Life Phase 1</td><td>QC # 3190 - Adhoc: Contract Copy functionality is not copying over the Images from the original application being copied</td></tr>
 * <tr><td>SR494086.5</td><td>Discretionary</td><td>ADC Workflow</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class NbaUpdateWorkBP extends NewBusinessAccelBP {
    boolean restoreHistory = false; //NBA208-1
    
	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			NbaDst dst = (NbaDst) input;
			result.addResult(update(dst.getNbaUserVO(), dst));
		} catch (Exception e) {
			addExceptionMessage(result, e);
			result.addResult(e);
			return result;
		}
		return result;
	}

	protected NbaDst update(NbaUserVO userVO, NbaDst nbaDst) throws NbaBaseException {
        try {        
       	    NbaTransaction pushTransaction=null; //NBLXA-1954 and NBLXA-2620
            setRestoreHistory(nbaDst.isHistoryRetrieved());	//NBA208-1   
            if (isCreateFlagOn(nbaDst)) {
                NbaDst tempNbaDst = WorkflowServiceHelper.createCase(userVO, nbaDst.getBusinessArea(), nbaDst.getWorkType(), nbaDst.getStatus(), nbaDst.getNbaLob());
                //begin NBA208-32
                WorkItem origWorkitem;
                WorkItem tempWorkitem;
                if (nbaDst.isCase()) {
                    origWorkitem = nbaDst.getCase();
                    tempWorkitem = tempNbaDst.getCase();
                } else {
                    origWorkitem = nbaDst.getTransaction();
                    tempWorkitem = tempNbaDst.getTransaction();
                }
                origWorkitem.setCreate(null);
                origWorkitem.setItemID(tempNbaDst.getID());
                origWorkitem.setLockedByMe(tempWorkitem.isLockedByMe());
                origWorkitem.setLockStatus(tempWorkitem.getLockStatus());
                origWorkitem.setQueueID(tempWorkitem.getQueueID());
                //end NBA208-32
            }
            //Check whether it is a case and status change has occurred
            boolean statusChange = false;
            if (nbaDst.isCase() && nbaDst.hasNewStatus()) {
                statusChange = true;
            }
    		//Begin AXAL3.7.20
    		if ((! userVO.isAutomatedProcess() && nbaDst.hasNewStatus())) {
    			nbaDst.getNbaLob().setUserId(userVO.getUserID());
    		}
    		//End AXAL3.7.20
            Map historyMap = new HashMap();	//NBA208-1   
            Map commentsMap = new HashMap();  //NBA208-32
            if (isRestoreHistory()){	//NBA208-1   
                cacheCommittedHistory(nbaDst, historyMap, commentsMap);  //NBA208-1, NBA208-32   
            }	//NBA208-1   
            NbaCorrespondenceEvents events = new NbaCorrespondenceEvents(userVO); //NBA146
            events.validateAwdEvent(nbaDst);
            //Copy imageMap to temp map. Original NbaDst object will be replaced after Netserver call(runJob).
            Map imageMap = nbaDst.getImageSourceMap();
            Map updateMap = new HashMap(); // NBA208-32 - keep track of original objects linked to update requests so we can apply changes directly with reponse
            List listOfNbaTransactions = null;
            List updateList = new ArrayList();
            NbaAwdRetrieveOptionsVO retOptVO = new NbaAwdRetrieveOptionsVO();
            WorkItemRequest workItemRequest = new WorkItemRequest();//NBA188
            //begin NBA188
            if (nbaDst.getSources() != null && nbaDst.getSources().size() != 0) {
                retOptVO.requestSources();
            }
            //Cache the requirement control source from nbadst as that is not to be updated in AWD           
            Map nbaSystemDataMap = NbaSystemDataProcessor.cacheSystemData(nbaDst);	//SPR3304
            //end NBA188
            if (nbaDst.isCase()) {
				if (NbaUtils.isBlankOrNull(nbaDst.getNbaLob().getQueueEntryDate())) {//Begin APSL505 added if condition
					nbaDst.getNbaLob().setQueueEntryDate(new Date());
				} else {
					if (nbaDst.hasNewStatus()) { //NBA229
						nbaDst.getNbaLob().setQueueEntryDate(new Date()); //NBA229
					} //NBA229
				}//End APSL505
                NbaCase nbaCase = nbaDst.getNbaCase();
                listOfNbaTransactions = nbaDst.getNbaTransactions();
                // prepare retrieve option vo and retrieve the complete DST.                
                workItemRequest.setWorkItemID(nbaCase.getID());
                retOptVO.setWorkItem(workItemRequest.getWorkItemID(), true);
                updateList = update(nbaCase, updateMap, updateList, imageMap, userVO.getPassword(),statusChange );	//NBA212, NBA208-32, ALS5337
                Iterator itr = listOfNbaTransactions.iterator();
                NbaTransaction transaction = null; //NBA229
                while (itr.hasNext()) {
                	// Begin NBA229
                	transaction = (NbaTransaction) itr.next();
                	//Begin NBLXA-1954 and NBLXA-2620
                	if(!NbaUtils.isBlankOrNull(transaction.getNbaLob().getUnsuspendWorkItem()) && transaction.hasNewStatus()){
                		pushTransaction=transaction;
                	}
                	//End NBLXA-1954
                	if (NbaUtils.isBlankOrNull(transaction.getNbaLob().getQueueEntryDate())) {//APSL505 added if condition
                		transaction.getNbaLob().setQueueEntryDate(new Date());
					}
                	if (transaction.hasNewStatus()) {
		
                		transaction.getNbaLob().setQueueEntryDate(new Date());
			    		//Begin AXAL3.7.20
			    		if (! userVO.isAutomatedProcess()) {
			    			transaction.getNbaLob().setUserId(userVO.getUserID());
			    		}
			    		//End AXAL3.7.20
					}
					 transaction.getNbaLob().setAppProdType(nbaDst.getNbaLob().getAppProdType()); //SR494086.5 ADC Retrofit
                	//Begin AXAL3.7.20R
		    		if (NbaUtils.isBlankOrNull(transaction.getNbaLob().getReg60CseMgrQueue())) {
		    			transaction.getNbaLob().setReg60CseMgrQueue(nbaDst.getNbaLob().getReg60CseMgrQueue());
		    		}
		    		//End AXAL3.7.20R
                    updateList = update(nbaCase, transaction, userVO, updateMap, updateList, imageMap);	//NBA212, NBA208-32
                   // End NBA229   
                    retOptVO.requestTransactionAsChild();
                }
                //NBA188 code deleted
            } else if (nbaDst.isTransaction()) {   
				if (NbaUtils.isBlankOrNull(nbaDst.getNbaLob().getQueueEntryDate())) {//Begin APSL505 added if condition
					nbaDst.getNbaLob().setQueueEntryDate(new Date());
				} else {
					if (nbaDst.hasNewStatus()) { //NBA229
						nbaDst.getNbaLob().setQueueEntryDate(new Date()); //NBA229
					} //NBA229
				}//End APSL505

				//Begin NBLXA-1954 and NBLXA-2620
				if(!NbaUtils.isBlankOrNull(nbaDst.getNbaTransaction().getNbaLob().getUnsuspendWorkItem()) && nbaDst.getNbaTransaction().hasNewStatus()){
					pushTransaction=nbaDst.getNbaTransaction();
		      	  }
				//End NBLXA-1954
        		workItemRequest.setWorkItemID(nbaDst.getNbaTransaction().getID());
                retOptVO.setWorkItem(workItemRequest.getWorkItemID(), false);                
                updateList = update(null, nbaDst.getNbaTransaction(), userVO, updateMap, updateList, imageMap);	//NBA212, NBA208-32
                //NBA188 code deleted                                
            }
            //NBA208-1 code deleted
            Iterator updateListItr = updateList.iterator();
            //Begin NBA188
            Object obj = null;
            List systemData = null;
          //APSL5055-NBA331.1 code deleted
            Map sourceToImageMap = new HashMap();	//SPR3332
          //APSL5055-NBA331.1 code deleted
            while (updateListItr.hasNext()) {
                obj = updateListItr.next();
                if (obj instanceof WorkItemRequest){
                    systemData = NbaSystemDataProcessor.getSystemDataOfNewWorkitem(obj, nbaSystemDataMap);
                }
              //APSL5055-NBA331.1 code deleted
                // begin NBA208-32
                AccelResult res = (AccelResult)callBusinessService("StandardFullCompleteWorkBP", obj);  //NBA213
                try {
                	//Begin P2AXAL040
                	String status = nbaDst.getStatus();
                	if (obj instanceof CompleteWorkRequest) {
                		status = ((CompleteWorkRequest)obj).getStatus();
                	}
                	processResult(res, status);
                	//End P2AXAL040
                	if(res instanceof RetrieveWorkResult){
                		RetrieveWorkResult result = (RetrieveWorkResult)res;
                		//APSL5055-NBA331.1 code deleted
	                    Object item = updateMap.get(obj);
	                    if (obj instanceof WorkItemRequest){
	                        NbaSystemDataProcessor.addSystemDataWithNewId(result, nbaSystemDataMap, systemData); //NBA213
	                    }
	                    
	                    // merge the work/source item returned with original...
	                    if(item instanceof NbaTransaction && result.getWorkItems() != null && !result.getWorkItems().isEmpty()){
							Iterator iter = result.getWorkItems().iterator();
							while(iter.hasNext()){
								Object retitem = iter.next();
								if(retitem instanceof WorkItem){
									String create = ((NbaTransaction)item).getTransaction().getCreate();//ALPC185
									assembleWorkItemFields((WorkItem)retitem, ((NbaTransaction)item).getTransaction());
									((NbaTransaction)item).setAction(null);
									//Begin ALPC185
									if(create != null && create.equals(NbaConstants.YES_VALUE)) {
										assembleTransactionSources((WorkItem) retitem, ((NbaTransaction)item).getTransaction());
									}
									//End ALPC185
								}
							}
	                    } else if(item instanceof NbaCase && result.getWorkItems() != null && !result.getWorkItems().isEmpty()){
							Iterator iter = result.getWorkItems().iterator();
							while(iter.hasNext()){
								Object retitem = iter.next();
								if(retitem instanceof WorkItem){
									assembleWorkItemFields((WorkItem)retitem, ((NbaCase)item).getCase());
									((NbaCase)item).setAction(null);
								}
							}
						} else if(item instanceof NbaSource && result.getSourceItems() != null && !result.getSourceItems().isEmpty()){
							Iterator iter = result.getSourceItems().iterator();
							while(iter.hasNext()){
								Object retitem = iter.next();
								if(retitem instanceof WorkItemSource){
									assembleSourceFields((WorkItemSource)retitem, ((NbaSource)item).getSource());
									((NbaSource)item).setAction(null);
								}
							}
						} else if(item instanceof WorkItem && result.getWorkItems() != null && !result.getWorkItems().isEmpty()){
							Iterator iter = result.getWorkItems().iterator();
							while(iter.hasNext()){
								Object retitem = iter.next();
								if(retitem instanceof WorkItem){
									assembleWorkItemFields((WorkItem)retitem, (WorkItem)item);
								}
							}
						} else if(item instanceof WorkItemSource && result.getSourceItems() != null && !result.getSourceItems().isEmpty()){
							Iterator iter = result.getSourceItems().iterator();
							while(iter.hasNext()){
								Object retitem = iter.next();
								if(retitem instanceof WorkItemSource){
									assembleSourceFields((WorkItemSource)retitem, (WorkItemSource)item);
								}
							}
						}
                	// end NBA208-32
	                //End NBA188
                	}
                } catch (NbaBaseException e) {
                    if (e.getMessage().startsWith(A_ERR_NOWORK)) {
                        return nbaDst;
                    }
                    throw e;
                }
            }
            updateImageSources(userVO, imageMap, sourceToImageMap);	//Save the Images in the Workflow system  //SPR3332
            if (nbaSystemDataMap.size() > 0) { //NBA188
                NbaSystemDataProcessor.updateAuxilliaryDatabaseForSystemData(nbaSystemDataMap);//NBA188 
            }//NBA188            
            retOptVO.setNbaUserVO(userVO);
            // NBA208-32 - code deleted
            //SPR3332 code deleted
            //SPR3304 code deleted
            events.refreshNbaDst(nbaDst); //provide the latest snapshot of the NbaDst object
            events.createWorkItems(); //create Correspondence Work items if necessary
            //Update nbProducer PendingInfo Database
            if (statusChange) {
                NbaNbproducerEvents nbPevents = new NbaNbproducerEvents();
                nbPevents.updateNbpendingInfoDatabaseForAwdStatusChangeEvent(nbaDst);
            }
            //TODO: Use the comments in the results from the hibernate service to update nbaDst
            //begin NBA208-38
            if(nbaDst != null && nbaDst.getWork() != null){
            	List commentLst =  nbaDst.getWork().getManualComments();
            	System.out.println("Total Comment on the Workitem with Id "+nbaDst.getWork().getID() +" is "+commentLst.size());
            	Iterator itr = commentLst.iterator();
            	while(itr.hasNext()){
            		 Comment comment = (Comment) itr.next();
            		 System.out.println(comment.getText());
            	}
            }
            Result commentResult = callBusinessService("CommitCommentsBP",nbaDst); 
            if (!commentResult.hasErrors()) { 
            	updateDst(nbaDst);
            }
            //end NBA208-38
            WorkflowServiceHelper.retrieveNbaSystemData(nbaDst);  //NBA208-32
            mergeCommittedHistory(nbaDst, historyMap, commentsMap);  //NBA208-32
            
            //NBLXA-1954 and NBLXA-2620
    		if (pushTransaction != null && pushTransaction.getNbaLob()!=null && !NbaUtils.isBlankOrNull(pushTransaction.getNbaLob().getQueue()) && pushTransaction.getNbaLob().getQueue().equalsIgnoreCase(NbaConstants.END)) {
    			unSuspendPushWork(pushTransaction, nbaDst);
    		}
    		//NBLXA-1954 and NBLXA-2620
       
    		return nbaDst;
        } catch (NbaBaseException e) {
	        LogHandler.Factory.LogError(this, e.getMessage());
            e.forceFatalExceptionType();	//SPR3332
            throw e;
        } catch (Throwable t) {             
            NbaBaseException e = new NbaBaseException(NbaBaseException.UPDATE_WORK + " " + t.getClass().getName(), t, NbaExceptionType.FATAL);	//SPR3332 NBA213
         	LogHandler.Factory.LogError(this, e.getMessage());
            throw e;
        	//SPR3332 code deleted 
        }
        
      
    }
    
	//NBA212 added imageMap to parameters
	// NBA208-32 - added updateMap to parameters
    protected List update(NbaCase caseObj, NbaTransaction transactionObj, NbaUserVO userVO, Map updateMap, List updateList, Map imageMap) throws NbaBaseException { 
        String update = transactionObj.getTransaction().getUpdate();
        String create = transactionObj.getTransaction().getCreate();
        boolean unlock = NbaConstants.YES_VALUE.equals(transactionObj.getTransaction().getUnlock());  //NBA180
        String systemName = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.DEFAULT_SYSTEM);
        if (create != null && create.equals(NbaConstants.YES_VALUE)) {
            WorkItemRequest workItemRequest = new WorkItemRequest();
            workItemRequest.setSystemName(getWorkflowSystemName());
            //begin ALS3319 if new source to be created and attached
            List sources = transactionObj.getSources();
            Iterator sourceItr = sources.iterator();
            List sourceItem = new ArrayList();
            while (sourceItr.hasNext()) {
            	WorkItemSource sourceObj = (WorkItemSource) sourceItr.next();
            	sourceObj.setSystemName(systemName); //APSL5055-NBA331
            	if (NbaConstants.YES_VALUE.equalsIgnoreCase(sourceObj.getCreate())){
            		sourceObj = prepareSourceVO(sourceObj, imageMap, userVO.getPassword()); //ALS3702
            	}
            	sourceItem.add(sourceObj);//ALS3639
            }
            transactionObj.getTransaction().setSourceChildren(sourceItem);
            //end ALS3319 
            workItemRequest.getWorkItems().add(transactionObj.getTransaction());  //NBA208-32
            //NBA208-32 code deleted
            workItemRequest.setWorkItemID(caseObj.getID());
            // if new source to be created and attached
            //NBA208-32 code deleted
            updateList.add(workItemRequest);
            // NBA208-32
            updateMap.put(workItemRequest, transactionObj);
            //APSL613 begin
    		if(NbaPrintLogger.getLogger().isDebugEnabled()){
    			if(transactionObj.getWorkType() != null && transactionObj.getWorkType().equals(NbaConstants.A_WT_CONT_PRINT_EXTRACT)){ //A2ISSUE error stop
    				NbaPrintLogger.getLogger().logDebug("Print work item is added to updateList NbaUpdateWorkBP - "+transactionObj.getNbaLob().getPolicyNumber());
    			}
    		}
    		//APSL613 end
            return updateList;
        }
        CompleteWorkRequest completeWorkRequest = new CompleteWorkRequest();
        completeWorkRequest.setSystemName(getWorkflowSystemName());
        completeWorkRequest.setBusinessArea(transactionObj.getBusinessArea()); // APSL5055-NBA331
        completeWorkRequest.setLockedBy(transactionObj.getTransaction().getLockStatus()); // APSL5055-NBA331
        completeWorkRequest.setWorkItemID(transactionObj.getID());
        completeWorkRequest.setLobData(getFormattedLobList(transactionObj.getNbaLob()));
        completeWorkRequest.setBreakRelation(transactionObj.getTransaction().getBreakRelation());
        completeWorkRequest.setRecordType(TRANSACTIONRECORDTYPE);
        completeWorkRequest.setWorkType(transactionObj.getTransaction().getWorkType());
        completeWorkRequest.setStatus(transactionObj.getStatus());
        completeWorkRequest.setPriority(transactionObj.getTransaction().getPriority());//ALS5718
        completeWorkRequest.setPriorityIncrease(transactionObj.getTransaction().getPriorityIncrease());//ALS5718
        completeWorkRequest.setIgnoreLock(!unlock);  //NBA180
        if (caseObj != null && caseObj.getCase() != null) {
        	//NBA208-32
            completeWorkRequest.setParentCreateDateTime(caseObj.getCase().getCreateDateTime());
            completeWorkRequest.setParentCreateNode(caseObj.getCase().getCreateNode());
            //NBA208-32
            completeWorkRequest.setParentID(caseObj.getCase().getItemID());
            completeWorkRequest.setParentRecordCode(caseObj.getCase().getRecordType());
        }
        completeWorkRequest.setCreateRelation(transactionObj.getTransaction().getRelate());
        completeWorkRequest.setBusinessArea(transactionObj.getTransaction().getBusinessArea());	//ALPC137
        //if comments needs to be stored in workflow, 
        if (NbaUtils.isAdditionalCommentsStoreWF()) { //NBA208-38
           // get the list of manual comments
			List listOfComments = transactionObj.getManualComments();
			// loop through all the comments
			Iterator itr = listOfComments.iterator();
			List commentLst = new ArrayList();
			while (itr.hasNext()) {
				//NBA208-32
				Comment comment = (Comment) itr.next();
				// if comment has Action "A" then convert the comment to AWD Format and add it to 
				//commentLst
				if (Comment.COMMENT_ACTION_ADD.equalsIgnoreCase(comment.getAction())) { //NBA208-38
					    commentLst.add(comment.getCommentsInAwdFormat()); //NBA208-38
						update = NbaConstants.YES_VALUE;
				}
			}
			//set the list in the request
			completeWorkRequest.setCommentsLst(commentLst);
		} //NBA208-38
        if (transactionObj.getTransaction().getBreakRelation() != null && transactionObj.getTransaction().getBreakRelation().equalsIgnoreCase(NbaConstants.YES_VALUE)) {
            update = NbaConstants.YES_VALUE;
        }
        if (transactionObj.getTransaction().getRelate() != null && transactionObj.getTransaction().getRelate().equalsIgnoreCase(NbaConstants.YES_VALUE)) {
            update = NbaConstants.YES_VALUE;
        }
        //NBA208-38 code deleted
        if (NbaConstants.YES_VALUE.equalsIgnoreCase(update) || unlock) {  //NBA180
            updateList.add(completeWorkRequest);
            // NBA208-32
            updateMap.put(completeWorkRequest, transactionObj);
        }
        List listOfSources = transactionObj.getSources();
        if (listOfSources != null) {
            Iterator sourceItr = listOfSources.iterator();
            while (sourceItr.hasNext()) {
            	//NBA208-32
                WorkItemSource sourceObj = (WorkItemSource) sourceItr.next();
                create = sourceObj.getCreate();
                update = sourceObj.getUpdate();
                sourceObj.setSystemName(systemName); // APSL5055-NBA331
                //begin SPR3332
                if (NbaConstants.YES_VALUE.equals(create)) { 
                    sourceObj.setBreakRelation(NbaConstants.NO_VALUE);
                    // NBA208-32
                    createSourceRequest(transactionObj, updateMap, updateList, systemName, completeWorkRequest, sourceObj, imageMap, userVO.getPassword()); //NBA212
                    //sourceObj.setCreate(NbaConstants.NO_VALUE);//ALPC185
                } else if (NbaConstants.YES_VALUE.equalsIgnoreCase(sourceObj.getBreakRelation())) { 
                	// NBA208-32
                    createSourceRequest(transactionObj, updateMap, updateList, systemName, completeWorkRequest, sourceObj, imageMap, userVO.getPassword()); //NBA212
                } else if (NbaConstants.YES_VALUE.equals(update) || NbaConstants.YES_VALUE.equals(sourceObj.getRelate())) { 
                	// NBA208-32
                    createSourceRequest(transactionObj, updateMap,updateList, systemName, completeWorkRequest, sourceObj, imageMap, userVO.getPassword()); //NBA212
                //end SPR3332
                }
            }
        }
        return updateList;
    }    
    
    /**
     * Browse through the NbaDst object looking for all Sources. For each Source, check if update attribute is set. If set, update Source via
     * NetServer.
     * @param userVO user value object containing userID used to access AWD system
     * @param nbaDst the NbaDst object to update
     */
    protected void processSourcesForUpdate(NbaUserVO userVO, NbaDst nbaDst, Map imageMap) throws RemoteException, NbaNetServerException,
            NbaNetServerDataNotFoundException, Exception {
        NbaSource nbaSource;
        //check sources for top level work item
        Iterator allSources = nbaDst.getNbaSources().iterator();
        while (allSources.hasNext()) {
            nbaSource = (NbaSource) allSources.next();
            if (nbaSource.isUpdateSet()) {
                if (nbaSource.isTextFormat()) {
                    updateSource(userVO, nbaSource.getID(), nbaSource.getText().getBytes());
                } else if (nbaSource.isImageFormat()) {
                    updateSource(userVO, nbaSource.getID(), (byte[]) imageMap.get(nbaSource.getText()));
                }
            }
        } //top level work item has more sources
        //if top level work item is a Case, check case's transactions
        if (nbaDst.isCase()) {
            Iterator allTransactions = nbaDst.getNbaTransactions().iterator();
            NbaTransaction nbaTransaction;
            while (allTransactions.hasNext()) {
                nbaTransaction = (NbaTransaction) allTransactions.next();
                allSources = nbaTransaction.getNbaSources().iterator();
                while (allSources.hasNext()) {
                    nbaSource = (NbaSource) allSources.next();
                    if (nbaSource.isUpdateSet()) {
                        if (nbaSource.isTextFormat()) { // Text source
                            updateSource(userVO, nbaSource.getID(), nbaSource.getText().getBytes());
                        } else if (nbaSource.isImageFormat()) { //Image source
                            updateSource(userVO, nbaSource.getID(), (byte[]) imageMap.get(nbaSource.getText()));
                        }
                    }
                } //while transaction has more sources
            } //while case has more transactions
        } //if top level work item is a Case
    }
    
    protected void updateSource(NbaUserVO userVO, String sourceID, byte[] data) throws  NbaNetServerException, Exception {     //SPR3332 removed unnecessary RemoteException
        //if there is no data to update simply return.
        if (data != null) {	//NBA188  
            SourceItemRequest request = new SourceItemRequest();
            request.setSystemName(getWorkflowSystemName());
            request.setSourceItemID(sourceID);
            request.setSourceStream(data);           
            AccelResult res = (AccelResult)callBusinessService("UploadSourceBP", request);
            processResult(res);
        //NBA188 code deleted
        }  
    }
       
	//NBA212 added imageMap, passWord to parameters
	// NBA208-32 - added updateMap to parameters
    protected List update(NbaCase caseObj, Map updateMap, List updateList, Map imageMap, String passWord, boolean statusChange) throws NbaBaseException { //ALS5337
        String update = caseObj.getCase().getUpdate();
        boolean unlock = NbaConstants.YES_VALUE.equals(caseObj.getCase().getUnlock());  //NBA180
        String systemName = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.DEFAULT_SYSTEM);
        CompleteWorkRequest completeWorkRequest = new CompleteWorkRequest();
        completeWorkRequest.setSystemName(getWorkflowSystemName());
        completeWorkRequest.setBusinessArea(caseObj.getBusinessArea()); //APSL5055-NBA331
        completeWorkRequest.setLockedBy(caseObj.getCase().getLockStatus()); //APSL5055-NBA331
        completeWorkRequest.setWorkItemID(caseObj.getID());
        completeWorkRequest.setLobData(getFormattedLobList(caseObj.getNbaLob()));
        completeWorkRequest.setRecordType(CASERECORDTYPE);
        completeWorkRequest.setStatus(caseObj.getStatus());
        completeWorkRequest.setIgnoreLock(!unlock);  //NBA180
        //begin ALS2798, AXAL3.7.20
        if (caseObj.getCase() != null) {
        	completeWorkRequest.setPriority(caseObj.getCase().getPriority());
        	completeWorkRequest.setPriorityIncrease(caseObj.getCase().getPriorityIncrease());
        }
        //end ALS2798, AXAL3.7.20
        
        // if comments to be stored in workflow
        if (NbaUtils.isAdditionalCommentsStoreWF() || statusChange) { //NBA208-38 , ALS5337
           // get the manual comments on case
			List listOfComments = caseObj.getManualComments();
			Iterator itr = listOfComments.iterator();
			List commentLst = new ArrayList();
			while (itr.hasNext()) {
				// loop through all the comments
				//NBA208-32
				Comment comment = (Comment) itr.next();
				// if comment has action "A" set
				if (Comment.COMMENT_ACTION_ADD.equalsIgnoreCase(comment.getAction())) { //NBA208-38
				    // convert the comment to AWD Format
				    //ALS5337 begin
				    if(NbaUtils.isAdditionalCommentsStoreWF()) {
				        commentLst.add(comment.getCommentsInAwdFormat()); //NBA208-38
				        update = NbaConstants.YES_VALUE;
				    } else if (!NbaUtils.isBlankOrNull(comment.getText()) && !NbaUtils.isBlankOrNull(caseObj.getNbaLob())) {
                        if (comment.getText().equalsIgnoreCase(caseObj.getNbaLob().getRouteReason())) {
                            commentLst.add(comment.getCommentsInAwdFormat()); //NBA208-38
                            update = NbaConstants.YES_VALUE;
                        }
                    }
					//ALS5337 end
				}
			}
			completeWorkRequest.setCommentsLst(commentLst);
		} //NBA208-38
        if (NbaConstants.YES_VALUE.equals(update) || unlock) {  //NBA180
            updateList.add(completeWorkRequest);
            // NBA208-32
            updateMap.put(completeWorkRequest, caseObj);
        }
        List listOfSources = caseObj.getSources();
        if (listOfSources != null) {
            Iterator sourceItr = listOfSources.iterator();
            while (sourceItr.hasNext()) {
            	//NBA208-32
                WorkItemSource sourceObj = (WorkItemSource) sourceItr.next();
                String create = sourceObj.getCreate();
                update = sourceObj.getUpdate();
                sourceObj.setSystemName(systemName); // APSL5055-NBA331
                //begin SPR3332
                if (NbaConstants.YES_VALUE.equals(create)) {
                    sourceObj.setBreakRelation(NbaConstants.NO_VALUE);
                    // NBA208-32
                    createSourceRequest(caseObj, updateMap, updateList, systemName, completeWorkRequest, sourceObj, imageMap, passWord);	//NBA212
                } else if (NbaConstants.YES_VALUE.equals(update)) {
                	// NBA208-32
                    createSourceRequest(caseObj, updateMap, updateList, systemName, completeWorkRequest, sourceObj, imageMap, passWord);	//NBA212
                } else if (NbaConstants.YES_VALUE.equalsIgnoreCase(sourceObj.getBreakRelation()) || NbaConstants.YES_VALUE.equalsIgnoreCase(sourceObj.getRelate())) { //SPR3323
                	// NBA208-32
                    createSourceRequest(caseObj, updateMap, updateList, systemName, completeWorkRequest, sourceObj, imageMap, passWord);	//NBA212
                //end SPR3332
                }
            }
        }
        return updateList;
    }
    
	//NBA212 added imageMap, passWord to parameters
	//NBA208-32
    protected CompleteWorkRequest prepareSourceRequest(WorkItemSource sourceObj, String parentWorkItemId, String systemName, String status,
            String parentCreateNode, String parentCreateTime, String parentRecordType, String parentWorkType, Map imageMap, String passWord) {
            //NBA208-32
        WorkItemSource[] sources = new WorkItemSource[1];
        CompleteWorkRequest sourceUpdatereq = new CompleteWorkRequest();
        sourceUpdatereq.setWorkItemID(parentWorkItemId);
        sourceUpdatereq.setSystemName(systemName);
        sourceUpdatereq.setWorkType(parentWorkType);
        sourceUpdatereq.setStatus(status);
        sourceUpdatereq.setIgnoreLock(true);
        sourceUpdatereq.setParentCreateDateTime(parentCreateTime);
        sourceUpdatereq.setParentID(parentWorkItemId);
        sourceUpdatereq.setParentCreateNode(parentCreateNode);
        sourceUpdatereq.setParentRecordCode(parentRecordType);
        sources[0] = prepareSourceVO(sourceObj, imageMap, passWord);	//NBA212, NBA208-32
        sourceUpdatereq.setSources(sources);
        sourceUpdatereq.setIgnoreLock(true);
        sourceUpdatereq.setBusinessArea(sourceObj.getBusinessArea());	//ALPC137
        return sourceUpdatereq;
    }

	//NBA212 added imageMap, passWord to parameters
	//NBA208-32 removed systemName parameter
    protected WorkItemSource prepareSourceVO(WorkItemSource sourceObj, Map imageMap, String passWord) {
	    //NBA208-32 code deleted
        sourceObj.setPassWord(passWord);	//NBA212 
        //begin NBA212       
        if (null == sourceObj.getSourceStream()) { //ALS4283
        	if (sourceObj.getText() != null) {
				if (sourceObj.getFormat().equals(NbaConstants.A_SOURCE_IMAGE)) {
					// APSL5055-NBA331 code deleted
					// begin APSL5055-NBA331.1
					if (NbaConstants.AWDREST.equals(sourceObj.getSystemName())) {
						sourceObj.setSourceStream(imageMap.get(sourceObj.getText())); // Sent as a base64 decoded byte[]
						sourceObj.setText(null); // Do not need the link any more
					} else {
						sourceObj.setSourceStream(NbaBase64.encodeBytes((byte[]) imageMap.get(sourceObj.getText()))); // Sent as a base64 encoded
																														// String
					}
					// end APSL5055-NBA331.1
					// NBA208-32 code deleted
				} else {
                	sourceObj.setText("<![CDATA[" + sourceObj.getText() + "]]>");
            	}
            	//end NBA212
        	}
        }
		//NBA208-32 code deleted          
        return sourceObj;
    }
    /**
     * Create a CompleteWorkRequest for a Source attached to a Case and add it to the updateList.
     * @param caseObj the parent NbaCase of the Source
     * @param updateList the list of request objects
     * @param systemName the System Name
     * @param completeWorkRequest the CompleteWorkRequest for the parent
     * @param sourceObj the Source
     */
    //SPR3332 New Method
    //NBA212 added imageMap, passWord to parameters
    // NBA208-32 - added updateMap to parameters
    protected void createSourceRequest(NbaCase caseObj,Map updateMap, List updateList, String systemName, CompleteWorkRequest completeWorkRequest,
            WorkItemSource sourceObj, Map imageMap, String passWord) {
    	try{
	        WorkItem aCase = caseObj.getCase();
	        CompleteWorkRequest sourceUpdatereq = prepareSourceRequest(sourceObj, aCase.getItemID(), systemName, completeWorkRequest.getStatus(), aCase
	                .getCreateNode(), aCase.getCreateDateTime(), aCase.getRecordType(), aCase.getWorkType(), imageMap, passWord);	//NBA212
	        updateList.add(sourceUpdatereq);
	        // NBA208-32
	        updateMap.put(sourceUpdatereq, sourceObj);
    	}catch(Exception ex){
    	}
    }
    /**
     * Create a CompleteWorkRequest for a Source attached to a Transaction and add it to the updateList.
     * @param transactionObj the parent NbaTransaction of the Source
     * @param updateList the list of request objects
     * @param systemName the System Name
     * @param completeWorkRequest the CompleteWorkRequest for the parent
     * @param sourceObj the Source
     * @throws NbaNetServerDataNotFoundException
     */
    //SPR3332 new method
    //NBA212 added imageMap, passWord to parameters
    // NBA208-32 - added updateMap to parameters
    protected void createSourceRequest(NbaTransaction transactionObj, Map updateMap, List updateList, String systemName,
            CompleteWorkRequest completeWorkRequest, WorkItemSource sourceObj, Map imageMap, String passWord) throws NbaNetServerDataNotFoundException {
        WorkItem tran = transactionObj.getTransaction();
        CompleteWorkRequest sourceUpdatereq = prepareSourceRequest(sourceObj, tran.getItemID(), systemName, completeWorkRequest.getStatus(), tran
                .getCreateNode(), tran.getCreateDateTime(), tran.getRecordType(), tran.getWorkType(), imageMap, passWord);	//NBA212
        updateList.add(sourceUpdatereq);
        // NBA208-32
        updateMap.put(sourceUpdatereq, sourceObj);
    }
     
    		// APSL5055-NBA331.1 code deleted
    
    
    /**
     * If the sourceVO.text contains a GUID which is a key to an entry in the in the imageMap, add the sourceVO
     * to the sourcesWithImages List so that it can matched against the results from the Workflow Update to determine the 
     * Source.id of the new Source. 
     * Otherise, add the id of the sourceVO to the sourceIDsToIgnore to cause it to be ignored when determining the new ids.
     * @param imageMap map containing GUIDs and their image data
     * @param sourceIDsToIgnore the ids of the Sources which are not Image updates
     * @param sourcesWithImages the SourceVOs of the Sources which are Image updates
     * @param sourceVO the SourceVO
     */
    //SPR3332 New Method
    //NBA208-32
    protected void findSourcesWithImages(Map imageMap, Map sourceIDsToIgnore, List sourcesWithImages, WorkItemSource sourceVO) {
        if (sourceVO.getText() != null && imageMap.containsKey(sourceVO.getText())) {
            sourcesWithImages.add(sourceVO);
        } else {
            if (sourceVO.getWorkItemID() != null){
                sourceIDsToIgnore.put(sourceVO.getWorkItemID(), null); //Just need the id to comapre against the results
            }
        }
    } 
    	// APSL5055-NBA331.1 code deleted
    
    /**
     * Apply the data for the Images to the Workflow system.
     * @param userVO the User value object
     * @param imageMap map containing GUIDs and their image data
     * @param sourceToImageMap contains entries which map the Source.id values to the GUIDs in the Image Map
     * @throws Exception
     * @throws NbaNetServerException 
     */
    //SPR3332 New Method
    protected void updateImageSources(NbaUserVO userVO, Map imageMap, Map sourceToImageMap) throws NbaNetServerException, Exception {
        Iterator it = sourceToImageMap.keySet().iterator();
        String id;
        String guid;
        while (it.hasNext()) {
            id = (String) it.next();
            guid = (String) sourceToImageMap.get(id);
            updateSource(userVO, id, (byte[]) imageMap.get(guid));
        }
    }

    		// APSL5055-NBA331.1 code deleted
    /**
     * Cache the committed history information for passed NbaDst object. It traverses complete object graph under DST and prepares a HashMap of vectors
     * where each vector contains committed history of specific item like case, transaction or source.
     * @param nbaDst
     * @return a map of vectors which contains cached history of all items under current DST object graph.
     * @throws NbaNetServerDataNotFoundException
     */
    //NBA208-1 New Method 
    //NBA208-32
    protected void cacheCommittedHistory(NbaDst nbaDst, Map historyMap, Map commentsMap) throws NbaNetServerDataNotFoundException {
        if (nbaDst.isCase()) {
        	//NBA208-32
            cacheCommittedHistoryForWork(nbaDst.getCase(), historyMap, commentsMap);
            List origTransList = nbaDst.getTransactions();
            int origTransCount = origTransList.size();
            for (int i = 0; i < origTransCount; i++) {
            	//NBA208-32
            	cacheCommittedHistoryForWork((WorkItem) origTransList.get(i), historyMap, commentsMap);
            }
        } else {
        	//NBA208-32
        	cacheCommittedHistoryForWork(nbaDst.getTransaction(), historyMap, commentsMap);
        }
    }
    
    //NBA208-32 code deleted
    
    /**
     * Cache all the committed history for the passed transaction object and its children sources. A vector for the original transaction and each child
     * source history will be formed to keep its committed history and same will be stored in historyMap
     * @param transaction
     * @param historyMap
     */
    //NBA208-1 New Method
    //NBA208-32 (consolidated cache for transaction and case into single method)
    protected void cacheCommittedHistoryForWork(WorkItem transaction, Map historyMap, Map commentsMap) {
        if (transaction.getHistory() != null && transaction.getHistory().isEmpty()) {
            historyMap.put(transaction.getItemID(), cacheCommittedHistoryItems(transaction.getHistory()));
        }
        //NBA208-32
        if (transaction.getComments() != null && transaction.getComments().isEmpty()) {
            commentsMap.put(transaction.getItemID(), cacheCommittedHistoryItems(transaction.getComments()));
        }
        int sourceCount = transaction.getSourceChildren().size();
        WorkItemSource origSource = null;
        for (int i = 0; i < sourceCount; i++) {
            if (isRestoreHistory()) {  //Stop if a new comment was found
            	//NBA208-32
                origSource = (WorkItemSource)transaction.getSourceChildren().get(i);
                if (origSource.getHistory() != null && !origSource.getHistory().isEmpty()) {
                    historyMap.put(origSource.getItemID(), cacheCommittedHistoryItems(origSource.getHistory()));
                }
            } else {
                break;
            }
        }
    }
    /**
     * Determine all the history items being added/updated in the passed History object and cache them in a Vector. Remove the cached items from
     * original History object.
     * @param currentHistory - the History object to be processed.
     * @return a Vector of all items to be cached.
     */
    //NBA208-1 New Method
    //NBA208-32
    protected Vector cacheCommittedHistoryItems(List currentHistory) {
        int historyCount = currentHistory.size();
        Vector historyCacheVector = new Vector(10, 10);
        Comment currentHistoryItem = null;  //NBA208-32
        for (int i = historyCount - 1; i >= 0; i--) {
        	//begin NBA208-32
        	if (currentHistory.get(i) instanceof Comment) {
	            currentHistoryItem = (Comment)currentHistory.get(i);
	            String type = currentHistoryItem.getCommentType();
	            if ("M".equals(type) && "Y".equals(currentHistoryItem.getCreate())) {
	                setRestoreHistory(false); //Stop if a new comment was found
	                break;
	            }
        	}
            historyCacheVector.add(currentHistory.get(i));
            //end NBA208-32
            currentHistory.remove(i);
        }
        return historyCacheVector;
    }
    /**
     * Merge the committed history information for passed NbaDst object. It traverses complete object graph under DST and retrieves history for each
     * specific item like case, transaction or source from the cache and restores it back to original hierarchy.
     * @param nbaDst
     * @param historyMap
     * @throws NbaNetServerDataNotFoundException
     */
    //NBA208-1 New Method
    //NBA208-32
    protected void mergeCommittedHistory(NbaDst nbaDst, Map historyMap, Map commentsMap) throws NbaNetServerDataNotFoundException {
        nbaDst.setHistoryRetrieved(isRestoreHistory());
        if (isRestoreHistory()) { //Continue if the original had History and no new History was found
            if (nbaDst.isCase()) {
            	//NBA208-32
                mergeCommittedHistoryForWork(nbaDst.getCase(), historyMap, commentsMap);
                List origTransList = nbaDst.getTransactions();
                int origTransCount = origTransList.size();
                for (int i = 0; i < origTransCount; i++) {
                	//NBA208-32
                    mergeCommittedHistoryForWork((WorkItem) origTransList.get(i), historyMap, commentsMap);
                }
            } else {
            	//NBA208-32
                mergeCommittedHistoryForWork(nbaDst.getTransaction(), historyMap, commentsMap);
            }
        }
    }
    //NBA208-32 code deleted
    
    /**
     * Merge cached history from historyMap into a Transaction object.
     * @param transaction the Transaction objected to be updated
     * @param historyMap history Map cache
     */
    //NBA208-1 New Method
    //NBA208-32
    protected void mergeCommittedHistoryForWork(WorkItem transaction, Map historyMap, Map commentsMap) {
        if (historyMap.containsKey(transaction.getItemID())) {
            transaction.getHistory().addAll((List)historyMap.get(transaction.getItemID()));
        }
        //NBA208-32
        if (commentsMap.containsKey(transaction.getItemID())) {
            transaction.getComments().addAll((List)commentsMap.get(transaction.getItemID()));
        }
        int sourceCount = transaction.getSourceChildren().size();
        WorkItemSource source = null;
        for (int i = 0; i < sourceCount; i++) {
        	//NBA208-32
            source = (WorkItemSource)transaction.getSourceChildren().get(i);
            mergeCommittedHistoryForSource(source, historyMap, commentsMap);
        }
    }
    /**
     * Merge cached history from historyMap into a Source object.
     * @param historyMap
     * @param source
     */
    //NBA208-1 New Method
    //NBA208-32
    protected void mergeCommittedHistoryForSource(WorkItemSource source, Map historyMap, Map commentsMap) {
        if (historyMap.containsKey(source.getItemID())) {
            source.getHistory().addAll((List)historyMap.get(source.getItemID()));
        }
        //NBA208-32
        if (commentsMap.containsKey(source.getItemID())) {
            source.getComments().addAll((List)commentsMap.get(source.getItemID()));
        }
    }

    /**
     * Returns the restoreHistory. The value is true if if the original had History and no new History was found
     * @return restoreHistory
     */
    //NBA208-1 New Method
    protected boolean isRestoreHistory() {
        return restoreHistory;
    }
    /**
     * Set the value of restoreHistory
     * @param restoreHistory value to set.
     */
    //NBA208-1 New Method
    protected void setRestoreHistory(boolean restore) {
        restoreHistory = restore;
    }
    
    //NBA208-32 New Method
    protected void assembleWorkItemFields (com.csc.fs.accel.valueobject.WorkItem  source, com.csc.fs.accel.valueobject.WorkItem workItemVO) {
	    //long start = System.currentTimeMillis();
		workItemVO.setIdentifier(source.getIdentifier());
		workItemVO.setItemID(source.getItemID());
		workItemVO.setBusinessArea(source.getBusinessArea());
		workItemVO.setWorkType(source.getWorkType());
		workItemVO.setStatus(source.getStatus());
		workItemVO.setQueueID(source.getQueueID());
		workItemVO.setLockStatus(source.getLockStatus());
		workItemVO.setCreateDateTime(source.getCreateDateTime());
		workItemVO.setRecordType(source.getRecordType());
		workItemVO.setCreateNode(source.getCreateNode());
		workItemVO.setPriority(source.getPriority());
		workItemVO.setSuspendFlag(source.getSuspendFlag());
		workItemVO.setSuspendDateTime(source.getSuspendDateTime());	//NBA146
		workItemVO.setActivationDateTime(source.getActivationDateTime());	//NBA146
		workItemVO.setActivationDateTimeStr(source.getActivationDateTimeStr());	//NBA146
		workItemVO.setActivationStatus(source.getActivationStatus());	//NBA146
		workItemVO.setIndex_1(source.getIndex_1());
		workItemVO.setIndex_2(source.getIndex_2());
		workItemVO.setIndex_3(source.getIndex_3());
		workItemVO.setIndex_4(source.getIndex_4());
		workItemVO.setLockedByMe(source.isLockedByMe());
		workItemVO.setSystemName(source.getSystemName());
		workItemVO.setAction(source.getAction());
		workItemVO.setCreate(source.getCreate());
		workItemVO.setRelate(source.getRelate());
		workItemVO.setUpdate(source.getUpdate());
		workItemVO.setUnlock(source.getUnlock());
		//long end = System.currentTimeMillis();
	}
    
    //ALPC185 New Method
	protected void assembleTransactionSources(WorkItem retItem, WorkItem itemVO) {
		if (itemVO.getSourceChildren() != null && !itemVO.getSourceChildren().isEmpty()) {
			Map retItemMap = new HashMap();
			if (retItem != null && !retItem.getSourceChildren().isEmpty()) {
				Iterator retItemItr = retItem.getSourceChildren().iterator();
				while (retItemItr.hasNext()) {
					WorkItemSource retItemSource = (WorkItemSource) retItemItr.next();
					retItemMap.put(retItemSource.getSourceType(), retItemSource);
				}
			}
			Iterator itemItr = itemVO.getSourceChildren().iterator();
			while (itemItr.hasNext()) {
				WorkItemSource itemSource = (WorkItemSource) itemItr.next();
				if (retItemMap.get(itemSource.getSourceType()) != null) {
					assembleSourceFields((WorkItemSource) retItemMap.get(itemSource.getSourceType()), itemSource);
				}
			}
		}
	}

	//NBA208-32 - New Method
	protected void assembleSourceFields (WorkItemSource source, WorkItemSource sourceVO) {
		sourceVO.setArchiveBox(source.getArchiveBox());
		sourceVO.setItemID(source.getItemID());
		sourceVO.setBusinessArea(source.getBusinessArea());
		sourceVO.setCreateStation(source.getCreateStation());
		sourceVO.setCreateDateTime(source.getCreateDateTime());
		sourceVO.setRecordType(source.getRecordType());
		sourceVO.setCreateNode(source.getCreateNode());
		sourceVO.setCreateUser(source.getCreateUserID());
		sourceVO.setCustomScreen(source.getCustomScreen());
		sourceVO.setFormat(source.getFormat());
		sourceVO.setIdentifier(source.getIdentifier());
		sourceVO.setLockStatus(source.getLockStatus());
		sourceVO.setMailType(source.getMailType());
		sourceVO.setObjectID(source.getFileName());
		sourceVO.setOpticalStatus(source.getOpticalStatus());
		sourceVO.setRevisable(source.getRevise());
		sourceVO.setSecurityLevel(source.getSecurityLevel());	
		sourceVO.setReceiveTime(source.getReceived());
		sourceVO.setSourceType(source.getSourceType());
		sourceVO.setLockedByMe(source.isLockedByMe());
		sourceVO.setPageCount(source.getPageCount());
		sourceVO.setSystemID(source.getSystemID());
		sourceVO.setFolderName(source.getFolderName());
		sourceVO.setSubDirectory(source.getSubDirectory());
		sourceVO.setCreate(source.getCreate());
		sourceVO.setUpdate(source.getUpdate());
	}
	/**
	 * Resets the update flag after sucessful completion of commit
	 * @param nbaDst
	 * @throws NbaNetServerDataNotFoundException
	 */
	//NBA208-38 New Method
	protected void updateDst (NbaDst nbaDst) throws NbaNetServerDataNotFoundException{	
		if (nbaDst.isCase()) {
			updateDstComments(nbaDst.getCase().getComments());
			List trans = nbaDst.getTransactions();
			int size = trans.size();
			for (int i = 0; i < size; i++) {
				WorkItem wi = (WorkItem) trans.get(i);
				updateDstComments(wi.getComments());
			}
		} else {
			updateDstComments(nbaDst.getTransaction().getComments());
		}

	}
	/**
	 * Resets the update flag after sucessful completion of commit
	 * @param nbaDst
	 * @throws NbaNetServerDataNotFoundException
	 */
	//NBA208-38 New Method
	protected void updateDstComments (List comments) {	
		int size = comments.size();
		Comment comment;
		for (int i = 0; i < size; i++) {
			comment = (Comment) comments.get(i);
			if (Comment.COMMENT_ACTION_ADD.equals(comment.getAction())) {
				comment.setAction("");
			}
		}
	}
	// Begin NBLXA-1954 and NBLXA-2620
	protected void unSuspendPushWork(NbaTransaction pushTransaction, NbaDst dst) {
		String crda = pushTransaction.getNbaLob().getUnsuspendWorkItem();
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		if(crda.contains("C01")){
			suspendVO.setCaseID(crda);
			System.out.println("unSuspendPushWork Id  crda of parentcase " + crda);
		}else{
			System.out.println("unSuspendPushWork Id  crda of trnsaction " + crda);
			suspendVO.setTransactionID(crda);
		}
		
		suspendVO.setNbaUserVO(dst.getNbaUserVO());
		callBusinessService("NbaUnsuspendWorkBP", suspendVO);
	}
	// End NBLXA-1954 and NBLXA-2620
	
}
