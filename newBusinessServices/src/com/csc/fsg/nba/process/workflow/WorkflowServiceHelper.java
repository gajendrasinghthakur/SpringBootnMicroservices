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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.csc.fs.UserSessionController;
import com.csc.fs.Message;
import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.accel.WorkFlowMessages;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.result.CreateWorkResult;
import com.csc.fs.accel.result.WorkflowSourceResult;
import com.csc.fs.accel.valueobject.File;
import com.csc.fs.accel.valueobject.SourceItemRequest;
import com.csc.fs.accel.valueobject.TimeStamp;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemRequest;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fs.sa.SystemDefinition;
import com.csc.fs.sa.SystemDefinitionHandler;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fs.Result;

/**
 * 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-2</td><td>Version 7</td><td>Performance Tuning and Testing</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class WorkflowServiceHelper {
	
    protected final static String A_ERR_CANT_LOCK = "SYS0091";
    protected final static String A_ERR_CANT_LOCK_REST = WorkFlowMessages.ERR_MSG_LOCK_FAILED.toString(); //APSL5055-NBA331.1
    protected static final String EMPTYSTRING = ""; 
    protected final static String CASERECORDTYPE = "C";
    protected final static String TRANSACTIONRECORDTYPE = "T";
    protected final static String SOURCERECORDTYPE = "O";
    protected static NbaLogger logger = null;
    // APSL5055-NBA331 code deleted
    protected static final String CONTENT_SERVICES = "ContentServices";	//NBA212
    
    //NBA331 code deleted


    public static void checkOutcome(AccelResult value) throws NbaBaseException {
    	if(value.hasErrors()){
	        StringBuffer errors = new StringBuffer();
	        boolean lockException = false;
	        //begin P2AXAL040
	        NbaDst dst = null;
	        Object dstObj = value.getFirst();
	        if (dstObj instanceof NbaDst) {
	        	dst = (NbaDst) dstObj;
			}
			//end P2AXAL040
			// assume its an object check for existance of error indicator field
			Message messages[] = value.getMessages();
			if (messages != null && messages.length > 0) {
				for (int i = 0; i < messages.length; i++) {
					Message message = messages[i];
					String errorVal = "";
					try{
					    errorVal = message.format();
					}catch(Exception ex){
					    //Ignore. Null check will handle
					}
					if (errorVal == null || errorVal.equals(Message.ERR_MESSAGE_MISSING)) {
						if(dst != null){//P2AXAL040
	                		errorVal += "( For status: " + dst.getStatus() + " ) ";//P2AXAL040
	                	}
						List data = message.getData();
						if(data != null){
							errorVal += " data[" + data.toString() + "]";
						}
					}
					if (errorVal.equals(A_ERR_CANT_LOCK_REST) || errorVal.startsWith(A_ERR_CANT_LOCK)) {   //APSL5055-NBA331
	                    lockException = true;
	                }
	                errors.append(errorVal).append(" ");
				}
			}
	        NbaBaseException ex;
	        if (lockException) {
	            ex = new NbaAWDLockedException(NbaAWDLockedException.LOCKED_BY_USER);
	        } else {
	            ex = new NbaBaseException(errors.toString());
	            ex.markAsLogged();
	        }
	        throw ex;
    	}
    }

	
	public static NbaDst updateWork(NbaUserVO user, NbaDst workItem)throws NbaBaseException{
		workItem.setNbaUserVO(user);
        AccelResult workResult = (AccelResult)ServiceHandler.invoke("NbaUpdateWorkBP", ServiceContext.currentContext(), workItem);
        checkOutcome(workResult);
    	Object obj = workResult.getFirst();
    	if(obj != null && obj instanceof NbaDst){
    		workItem = (NbaDst)obj;
    	}
		return workItem;
	}
	/**
	 * commits comments note that this will only be used when WF is not set as comment source
	 * @param user
	 * @param workItem
	 * @return
	 * @throws NbaBaseException
	 */
	//AXAL3.7.07
	public static NbaDst commitComments(NbaUserVO user, NbaDst workItem) throws NbaBaseException {
		workItem.setNbaUserVO(user);
		if (!NbaUtils.isAdditionalCommentsStoreWF()) {
			AccelResult workResult = (AccelResult) ServiceHandler.invoke("CommitCommentsBP", ServiceContext.currentContext(), workItem);
			checkOutcome(workResult);
			Object obj = workResult.getFirst();
			if (obj != null && obj instanceof NbaDst) {
				workItem = (NbaDst) obj;
			}
		}
		return workItem;
	}

	public static NbaDst update(NbaUserVO user, NbaDst workItem)throws NbaBaseException{
		return updateWork(user, workItem);
	}
	
	
	public static void suspendWork(NbaUserVO user, NbaSuspendVO suspendVo)throws NbaBaseException{
		suspendVo.setNbaUserVO(user);
        AccelResult workResult = (AccelResult)ServiceHandler.invoke("NbaSuspendWorkBP", ServiceContext.currentContext(), suspendVo);
        checkOutcome(workResult);
	}
	

	public static void unlockWork(NbaUserVO user, NbaDst workItem)throws NbaBaseException{
		workItem.setNbaUserVO(user);
		AccelResult workResult = new AccelResult();
        workResult.merge(ServiceHandler.invoke("NbaUnlockWorkBP", ServiceContext.currentContext(), workItem));
        checkOutcome(workResult);
	}

	public static void unsuspendWork(NbaUserVO user, NbaSuspendVO suspendVo)throws NbaBaseException{
		suspendVo.setNbaUserVO(user);
        AccelResult workResult = (AccelResult)ServiceHandler.invoke("NbaUnsuspendWorkBP", ServiceContext.currentContext(), suspendVo);
        checkOutcome(workResult);
	}
	
	public static void activateWork(NbaUserVO user, NbaSuspendVO suspendVo)throws NbaBaseException{
		suspendVo.setNbaUserVO(user);
        AccelResult workResult = (AccelResult)ServiceHandler.invoke("NbaActivateWorkBP", ServiceContext.currentContext(), suspendVo);
        checkOutcome(workResult);
	}
	
	public static NbaDst retrieveWork(NbaUserVO user,  NbaAwdRetrieveOptionsVO options) throws NbaBaseException{
		if(options != null){
			options.setNbaUserVO(user);
		}
        AccelResult workResult = (AccelResult)ServiceHandler.invoke("NbaRetrieveWorkBP", ServiceContext.currentContext(), options);
        checkOutcome(workResult);
        NbaDst workItem = null;
    	Object obj = workResult.getFirst();
    	if(obj != null && obj instanceof NbaDst){
    		workItem = (NbaDst)obj;
    	}
		return workItem;
	}

	public static NbaSearchVO lookupWork(NbaUserVO userVO, NbaSearchVO searchVO) throws NbaBaseException{
		searchVO.setNbaUserVO(userVO);
        AccelResult workResult = (AccelResult)ServiceHandler.invoke("SearchWorkflowBP", ServiceContext.currentContext(), searchVO);
        checkOutcome(workResult);
        NbaSearchVO workItem = null;
    	Object obj = workResult.getFirst();
    	if(obj != null && obj instanceof NbaSearchVO){
    		workItem = (NbaSearchVO)obj;
    	}
		return workItem;
	}
	
	
	public static NbaDst retrieveWorkItem(NbaUserVO user,  NbaAwdRetrieveOptionsVO options)throws NbaBaseException{
		return retrieveWork(user,options);
	}

	
	public static NbaDst retrieveWorkItem(NbaUserVO user,  NbaAwdRetrieveOptionsVO options, boolean lock)throws NbaBaseException{
		if(options != null){
			options.setNbaUserVO(user);
		}
		if(lock){
			options.setLockWorkItem();
		}
        AccelResult workResult = (AccelResult)ServiceHandler.invoke("NbaRetrieveWorkBP", ServiceContext.currentContext(), options);
        checkOutcome(workResult);
        NbaDst workItem = null;
    	Object obj = workResult.getFirst();
    	if(obj != null && obj instanceof NbaDst){
    		workItem = (NbaDst)obj;
    	}
		return workItem;
	}
	
    public static NbaDst getTimeStamp(NbaUserVO userVO) throws NbaBaseException {        
        //SPR208-32 code deleted
        TimeStamp timeStamp = new TimeStamp();
        timeStamp.setUserId(userVO.getUserID());
        //SPR208-32 code deleted
        AccelResult workResult = (AccelResult)ServiceHandler.invoke("RetrieveCurrentDateTimeBP", ServiceContext.currentContext(), timeStamp);  //SPR208-32
        checkOutcome(workResult);
        NbaDst nbaDst = new NbaDst();
        //NBA208-32 code deleted
        timeStamp = (TimeStamp)workResult.getFirst();
        //NBA208-32
        nbaDst.setTimestamp(timeStamp.getTimeStamp());
       return nbaDst;
    }
	
    
    /**
     * Retrieve the designated source image from NetServer
     * @param userVO User Value Object containing userID.
     * @param source the source
     * @return the source image
     */
    //NBA212 changed return type
    public static List getBase64SourceImage(NbaUserVO userVO, NbaSource source) throws NbaBaseException {
        List responses = null;	//NBA212 SPR3290       
        SourceItemRequest request = new SourceItemRequest();
        WorkflowSourceResult workResult = new WorkflowSourceResult(); 	 
        //begin NBA212
        // APSL5055-NBA331 code deleted
		request.setSystemName(getSystemName());
		// NBA208-32
        request.setSourceItemID(source.getID()); //APSL5055-NBA331        
        
        //begin NBA331.1
        if (NbaConstants.AWDREST.equals(request.getSystemName())) {
            request.setCollectionID(source.getSource().getFile());
            request.setRot13Password(userVO.getPassword()); 
            workResult.merge(ServiceHandler.invoke("GetImageSourceRestBP", ServiceContext.currentContext(), request));
        } else {
            request.setCollectionID(source.getSource().getObjectID());
            request.setRot13Password(NbaUtils.applyROT13(userVO.getPassword()));
            workResult.merge(ServiceHandler.invoke("GetImageSourceCSBP", ServiceContext.currentContext(), request));
        }
        //end NBA331.1        	 
        checkOutcome(workResult);
		List files = workResult.getData();
		File aFile;
		int fileCount = files.size(); // SPR3290
		responses = new ArrayList(fileCount + 1); // SPR3290
		for (int i = 0; i < fileCount; i++) {// SPR3290
			aFile = (File) files.get(i);
			if (aFile.getImage() != null) {
				responses.add(aFile.getImage());
			} else if (aFile.getText() != null) {
				responses.add(NbaBase64.encodeString(aFile.getText()));
			}
		}
         
        // APSL5055-NBA331 code deleted
        return responses;
        //end NBA212
    }

    public static String getSystemName(){ // APSL5055-NBA331 Changed visibility
        
    	 //begin APSL5055-NBA331
        UserSessionController userSession = ServiceContext.currentContext().getUserSession();
        if (userSession != null) {
            Result systemSessionResult = userSession.getSystem(NbaConstants.AWDNETSERVER);
            if (!systemSessionResult.hasErrors()) {
                return NbaConstants.AWDNETSERVER;
            }
            systemSessionResult = userSession.getSystem(NbaConstants.WORK_TRACKING);
            if (!systemSessionResult.hasErrors()) {
                return NbaConstants.WORK_TRACKING;
            }
        }
        return NbaConstants.AWDREST;
        //end APSL5055-NBA331
    	
    
        
    }
    
    public static NbaDst createCase(NbaUserVO userVO, String businessArea, String workType, String status, NbaLob nbaLob) throws NbaBaseException {       
        WorkItemRequest workItemRequest = new WorkItemRequest();
        String objectType = CASERECORDTYPE;
        if (!nbaLob.isTypeCase()) {
            objectType = TRANSACTIONRECORDTYPE;
        }
        workItemRequest.setSystemName(getSystemName());
        WorkItem workItem = new WorkItem();
        workItemRequest.getWorkItems().add(workItem);
        workItem.setRecordType(objectType);
        workItem.setBusinessArea(businessArea);
        workItem.setWorkType(workType);
        workItem.setStatus(status);
        workItem.setLockStatus(userVO.getUserID());
        workItem.setIgnoreLock(true);
        //NBA208-32 - code deleted
        //NBA208-32
        //SPR3290 code deleted
        workItem.getLobData().addAll(nbaLob.getLobs());
        AccelResult workResult = (AccelResult)ServiceHandler.invoke("CreateFullWorkBP", ServiceContext.currentContext(), workItemRequest);
        checkOutcome(workResult);
        CreateWorkResult createWorkResult = (CreateWorkResult)workResult;
        WorkItem workitem = createWorkResult.getWorkItem();
        //begin APSL5055-NBA331.1
        if (NbaConstants.AWDREST.equals(workitem.getSystemName())) {
        	NbaDst nbaDst = new NbaDst();        	
            if (workitem.getRecordType().equals(CASERECORDTYPE)) {
            	nbaDst.addCase(workitem);    
            } else {
            	nbaDst.addTransaction(workitem);
            }
            nbaDst.setUserID(userVO.getUserID());
            return nbaDst;
        }
        //end APSL5055-NBA331.1
        NbaAwdRetrieveOptionsVO nbaAwdRetrieveOptionsVO = new NbaAwdRetrieveOptionsVO();
        nbaAwdRetrieveOptionsVO.setWorkItem(workitem.getItemID(), workitem.getRecordType().equals(CASERECORDTYPE));
        nbaAwdRetrieveOptionsVO.setSystemName(workitem.getSystemName());  //APSL5055-NBA331
        return retrieveWorkItem(userVO, nbaAwdRetrieveOptionsVO); //do a retrieve because the response from AWDCreate does not contain LOBs
    }
    
    protected static void retrieveTextSources(NbaDst nbaDst, boolean isNbaSystemDataRequested) throws NbaBaseException {
        NbaSource nbaSource;
        //check sources for top level work item
        Iterator allSources = nbaDst.getSources().iterator();
        while (allSources.hasNext()) {
        	//NBA208-32
            nbaSource = new NbaSource((WorkItemSource) allSources.next());
            if (nbaSource.isTextFormat()) {
                nbaSource.setText(getTextImage(nbaSource.getID(),nbaSource.getText()));
            }
        } //top level work item has more sources
        //if top level work item is a Case, check case's transactions
        if (nbaDst.isCase()) {
            Iterator allTransactions = nbaDst.getTransactions().iterator();
            NbaTransaction nbaTransaction;
            while (allTransactions.hasNext()) {
            	//NBA208-32
                nbaTransaction = new NbaTransaction((WorkItem) allTransactions.next());
                allSources = nbaTransaction.getSources().iterator();
                while (allSources.hasNext()) {
                	//NBA208-32
                    nbaSource = new NbaSource((WorkItemSource) allSources.next());
                    if (nbaSource.isTextFormat()) {
                        nbaSource.setText(getTextImage(nbaSource.getID(), nbaSource.getText()));
                    }
                } //while transaction has more sources
            } //while case has more transactions
        } //if top level work item is a Case
        if (isNbaSystemDataRequested) {
            retrieveNbaSystemData(nbaDst);
        }
    }

    protected static String getTextImage(String sourceKey, String text) throws NbaBaseException {
        String response = text; 
        SourceItemRequest request = new SourceItemRequest();
        request.setSystemName(getSystemName());
        request.setSourceItemID(sourceKey); 
        AccelResult workResult = (AccelResult)ServiceHandler.invoke("GetTextSourceBP", ServiceContext.currentContext(), request);
        checkOutcome(workResult);
        WorkflowSourceResult retrieveSourceResult = (WorkflowSourceResult)workResult;
        //if the result is empty, we are going to assume it was not found in work flow and that it is in the DB
        //so don't replace.
        if (retrieveSourceResult != null && retrieveSourceResult.getSourceStream() != null && !EMPTYSTRING.equals(retrieveSourceResult.getSourceStream())) { //APSL5055-NBA331
            response = (String) retrieveSourceResult.getSourceStream();
        }
        return response;
    }
    
    /**
     * This method populates the NbaSystemData for the case and transactions. It first goes to the
     * database to get the NbaSystemData based on work item id. Then it attaches those NbaSystemData with the case and/or transaction.
     * @param nbaDst The Dst object containing the case and/or transaction for which the Requirement control source is to be populated.
     */
    //NBA188 New Method
    protected static void retrieveNbaSystemData(NbaDst nbaDst) throws NbaBaseException {
        NbaSource nbaSource = null;
        Iterator nbaSources = null;
        //NBA208-32
        WorkItemSource source = null;
        //NBA208-2 begin
    	Map systemDataMap = NbaSystemDataProcessor.getSystemData(prepareSystemDataRequest(nbaDst));
    	if(systemDataMap.size() == 0) {
    	    return;
    	}
    	//NBA208-2 end
        if (nbaDst.isCase() || nbaDst.isTransaction()) {
        	List dstSourceList;
        	//begin NBA208-2
            List sourceList =(List)systemDataMap.get(nbaDst.getID());
            if(sourceList != null) { 
            	dstSourceList = nbaDst.getSources();
                nbaSources = sourceList.iterator(); 
                //end NBA208-2
                while (nbaSources.hasNext()) {
                    nbaSource = (NbaSource) nbaSources.next();
                    if (sourceNotRetrieved(nbaSource, dstSourceList)) {
	                    source = nbaSource.getSource();
	                    source.setBusinessArea(nbaDst.getBusinessArea());
	                    source.setSystemName(nbaDst.getSystemName()); //APSL5055-NBA331
	                    source.setCreate("");
	                    nbaSource = nbaDst.addNbaSource(nbaSource, false);
	                    nbaSource.getSource().setRelate("");
	                    nbaSource.getSource().setDisplayable(false);	//NBA208-32
                    }
                }
            } //NBA208-2
            //if top level work item is a Case, check case's transactions
            if (nbaDst.isCase()) {
                Iterator allTransactions = nbaDst.getTransactions().iterator();
                NbaTransaction nbaTransaction;
                while (allTransactions.hasNext()) {
                	//NBA208-32
                    nbaTransaction = new NbaTransaction((WorkItem) allTransactions.next());
                    //begin NBA208-2
                    dstSourceList = nbaTransaction.getSources();
                    sourceList =(List)systemDataMap.get(nbaTransaction .getID()); 
                    if (sourceList != null) {
                        nbaSources = sourceList.iterator(); 
                        //end NBA208-2
                        while (nbaSources.hasNext()) {
                            nbaSource = (NbaSource) nbaSources.next();
                            source = nbaSource.getSource();
                            if (sourceNotRetrieved(nbaSource, dstSourceList)) {
								source.setBusinessArea(nbaDst.getBusinessArea());
								source.setCreate("");
								nbaSource = nbaTransaction.addNbaSource(nbaSource, false);
								nbaSource.getSource().setRelate("");
								nbaSource.getSource().setDisplayable(false); //NBA208-32
							}
                        }
                    }//NBA208-2
                }
            }
        }
    }
    /**
	 * This method prepares the list of work item id's for which System Data is required to be retrieved
	 * @param nbaDst the nbA Dst instance
	 */
    // NBA208-2 New Method
    protected static List prepareSystemDataRequest(NbaDst nbaDst) throws NbaBaseException {
    	List requestList = new ArrayList();
        if (nbaDst.isCase() || nbaDst.isTransaction()) {
            requestList.add(nbaDst.getID());
            //if top level work item is a Case, check case's transactions
            if (nbaDst.isCase()) {
                Iterator allTransactions = nbaDst.getTransactions().iterator();
                NbaTransaction nbaTransaction;
                while (allTransactions.hasNext()) {
                	//NBA208-32
                    nbaTransaction = new NbaTransaction((WorkItem) allTransactions.next());                    
                    requestList.add(nbaTransaction.getID());
                }
            }
        }
    	return requestList;
    }

    // APSL5055-NBA331 code deleted

    /**
     * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
     * @return com.csc.fsg.nba.foundation.NbaLogger
     */
    protected static NbaLogger getLogger() {
        if (logger == null) {
            try {
                logger = NbaLogFactory.getLogger(WorkflowServiceHelper.class.getName());
            } catch (Exception e) {
                NbaBootLogger.log(WorkflowServiceHelper.class.getName() + " could not get a logger from the factory.");
                e.printStackTrace(System.out);
            }
        }
        return logger;
    }
    /**
     * Check and see if we have the source already added to the DST 
     * @param nbaSource
     * @param sourceList
     * @return
     */
    protected static boolean sourceNotRetrieved(NbaSource nbaSource, List sourceList) {
		int size = sourceList.size();
		for (int i = 0; i < size; i++) {
			WorkItemSource dstSource = (WorkItemSource) sourceList.get(i);
			//TODO NBA213-- why is nbaSource.getID null when adding a new source such as on reinsurance
			if (dstSource.getItemID() != null && dstSource.getItemID().equalsIgnoreCase(nbaSource.getID())) {
				return false;
			}
		}
		return true;
	}
}
