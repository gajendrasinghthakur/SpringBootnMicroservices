package com.csc.fsg.nba.accel.process;

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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import com.axa.fs.accel.console.valueobject.ExecuteAutoProcessVO;
import com.csc.fs.Message;
import com.csc.fs.Result;
import com.csc.fs.ServiceContext;
import com.csc.fs.accel.console.result.AutoProcessResult;
import com.csc.fs.accel.constants.ServiceCatalog;
import com.csc.fs.accel.notification.NotificationEvent;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.ui.AxaStatusDefinitionLoader;
import com.csc.fsg.nba.business.process.NbaAutoProcessProxy;
import com.csc.fsg.nba.business.process.NbaAutomatedProcess;
import com.csc.fsg.nba.business.process.NbaAutomatedProcessResult;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.database.NbaDatabaseUtils;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.statusDefinitions.Status;

/**
 * Business Process to execute an Automated Process.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA196</td><td>Version 7</td><td>JCA Adapter for Email</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7<td><tr>  
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>AXAL3.7.68</td><td>AXA Life Phase 1</td><td>LDAP Interface</td></tr>
 * <tr><td>ALNA212</td><td>AXA Life Phase 2</td><td>Performance Improvement</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
public class NbaAutoProcessAccelBP extends NewBusinessAccelBP{
    private static NbaLogger logger = null;
  //Begin NBLXA-2048
    static int  smartErrorStopThresholdValue = 0;
    static ArrayList skipPollerToSuspendList = new ArrayList();
    
    static{
    	ResourceBundle applicationMessages = ResourceBundle.getBundle("com.csc.fs.accel.ui.config.ApplicationMessages");//NBLXA-2048
    	 try{
         	smartErrorStopThresholdValue = Integer.parseInt(applicationMessages.getString("smart_error_stop_threshold"));
         	String skipPollerToSuspendString = applicationMessages.getString("skipPollerToSuspend");
         	if(!NbaUtils.isBlankOrNull(skipPollerToSuspendString)){
         		String[] skipPollerArray = skipPollerToSuspendString.split(",");
         		if(skipPollerArray != null){
         			Collections.addAll(skipPollerToSuspendList, skipPollerArray);
         		}
         	}
         }catch(Exception e){
         }
    }
    //End NBLXA-2048
    /**
     * This method creates a NbaUserVO object that will be used for processing of workitems within nbA
     * @param userID The user id of current user.
     * @param password The password of current user
     * @param userSessionKey The user session key
     * @param netServerHost Netserver Host name
     */
    protected NbaUserVO createUserVO(String userID, String password, String userSessionKey, String netServerHost) {
        NbaUserVO userVO = new NbaUserVO(userID, password, true, netServerHost);
        userVO.setSessionKey(userSessionKey);
        return userVO;
    }

    /**
     * This method calls the NbaNetserverAccessorBean to retrieve the case or workitem from AWD. If there is any error in retrieving the workitem,
     * then the method will send back the error message in result object
     * @param userVO the user VO used by nbA.
     * @param accelResult the result object
     * @return accelResult the result object containing NbaDst or error message
     */
    protected void getWork(NbaUserVO userVO, AutoProcessResult autoProcessResult) {
        NbaDst dst = null;
        long startTime = -1L;
        Date beginTime = null;
        if (getLogger().isDebugEnabled()){
            beginTime = new Date();
            startTime = System.currentTimeMillis();
        }
        try {
            AccelResult result = (AccelResult)callBusinessService("NbaGetWorkBP", userVO);
            processResult(result);
            dst = (NbaDst)result.getFirst();
            autoProcessResult.addResult(dst);
        } catch (Throwable exp) {
            //if there is an exception in getWork, then NbaWorkflowAccessor will unlock the case. log the error and send back the error message
            setException(exp, autoProcessResult, userVO.getSessionKey(), userVO.getUserID());            
        }
        if (getLogger().isDebugEnabled()){
            getLogger().logDebug(
                    "getWork," + userVO.getUserID() + ",returned[" + (dst == null ? "0" : "1") + "],DURATION"
                            + String.valueOf(System.currentTimeMillis() - startTime) + ",BeginTime:" + beginTime + ",EndTime:" + new Date());
        }
    }

    protected AccelResult executeAutoProcess(NbaUserVO userVO, NbaDst dst) {
        AutoProcessResult autoProcessResult = new AutoProcessResult();
        NbaAutomatedProcess autoProcess = null;
        // Lookup Autoprocess Implementation from process Name
        ResourceBundle props = ResourceBundle.getBundle("com.csc.fsg.nba.accel.process.AutoProcessHandlers");
        if(props != null){
            try{
                String handlerName = props.getString(ServiceContext.currentContext().getServiceName());
                if(handlerName != null){
                    Class processClass = Thread.currentThread().getContextClassLoader().loadClass(handlerName);
                    Thread.currentThread().setName(handlerName.substring(handlerName.lastIndexOf('.') + 1)); // APSL3860
                    autoProcess = (NbaAutomatedProcess)processClass.newInstance();
                    autoProcess.setCurrentBP(this); // Set current BP (this) to provide access to services framework if needed
                    return executeAutoProcess(userVO, dst, autoProcess);
                }  
                addErrorMessage(autoProcessResult, "Autoprocess Handler configuration missing for business process + [" + ServiceContext.currentContext().getServiceName() + "]");               
            }catch(Exception ex){
                addExceptionMessage(autoProcessResult, ex);
            }
        } else {
            addErrorMessage(autoProcessResult, "Unable to load Autoprocess Handler configuration");
        }
        return autoProcessResult;
    }
    

    /**
     * This method calls the executeProcess method on the class that implements NbaAutomatedProcess. In case of NbaLockedException, the workitem
     * is suspended. In case of a fatal exception, the process is stopped. In case of any other exception, the work item is sent to error queue.
     * @param userVO the user VO used by nbA.
     * @param dst The workitem to work on.
     * @param autoProcess an instance of the auto process class
     * @return accelResult the result object containing the status or error message
     */
    //APSL3874 added throws clause
    protected AccelResult executeAutoProcess(NbaUserVO userVO, NbaDst dst, NbaAutomatedProcess autoProcess) throws Exception {
        AutoProcessResult autoProcessResult = new AutoProcessResult();
        NbaAutomatedProcessResult processResult = null;
        try {
            long startTime = -1L;
            Date beginTime = null;
            if (getLogger().isDebugEnabled()){
                beginTime = new Date();
                startTime = System.currentTimeMillis();
            }
            processResult = autoProcess.executeProcess(userVO, dst);
            if (getLogger().isDebugEnabled()){
                getLogger().logDebug("executeProcess," + userVO.getUserID() + ",DURATION"
                        + String.valueOf(System.currentTimeMillis() - startTime) + ",BeginTime:" + beginTime + ",EndTime:" + new Date());

            }
            autoProcessResult.setStatus(processResult.getReturnCode());
            autoProcessResult.setUserSessionKey(userVO.getSessionKey()); //return user session key to the job.
        } catch (NbaLockedException lockedExp) {
            doNbaLockedProcessing(lockedExp, userVO, dst, autoProcessResult);
        } catch (AxaErrorStatusException exp) { //APSL3874
            processErrorStatusException(autoProcess, autoProcessResult, exp); //APSL3874
            autoProcess.handleErrorStatus(); //APSL3874
        } catch (NbaBaseException exp) {
        	processBaseException(autoProcess, autoProcessResult, exp, userVO, dst); //APSL3874,NBLXA-2048
            autoProcess.handleErrorStatus(); //APSL3874
        } catch (Throwable t) {
            try {
                routeToErrorStatus(autoProcess, autoProcessResult, t, userVO, dst); //APSL3874,NBLXA-2048
            } catch (Exception exp2) {
                //if not able to route the case to error queue, stop the process
                setException(exp2, autoProcessResult, userVO.getSessionKey(), autoProcess.getClass().getName());
            }
        } finally {
			// Begin NBLXA-2048
			try {
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("autoProcessResult.getStatus() : " + autoProcessResult.getStatus());
				}
				if (autoProcessResult.getStatus() != AutoProcessResult.NOWORK && autoProcessResult.getStatus() != AutoProcessResult.SUCCESSFUL
						&& autoProcessResult.getStatus() != AutoProcessResult.RETRY
						&& autoProcessResult.getStatus() != AutoProcessResult.SUCCESS_NOWORK
						&& autoProcessResult.getStatus() != AutoProcessResult.FAILURE) {
					String procUserId = null;
					if (smartErrorStopThresholdValue > 0) {
						routeToErrorStatus(autoProcess, autoProcessResult, procUserId, userVO, dst);
					}
				}
			} catch (Exception e) {
				getLogger().logException(e);
			}
			// End NBLXA-2048
            try {
                if (dst != null && userVO != null) {
                    //unlock the case and call remove lock from table
                    unlockWork(autoProcess.getWork(), userVO); //APSL3874
                }
            } catch (Exception exp) {
                // Do not overwrite the exception that is already set.
                getLogger().logException(exp);
            }
        }
        //Begin NBLXA-2497
        if(!NbaUtils.isBlankOrNull(autoProcessResult.getErrorMessage())){
        	StringBuilder errMsg = new StringBuilder(autoProcessResult.getErrorMessage());
        	if (!NbaUtils.isBlankOrNull(NbaDatabaseUtils.getProcessingInstance())) {
        		errMsg.append(", Processing Instance - "+NbaDatabaseUtils.getProcessingInstance());
        	}
        	if(autoProcess.getWork().getNbaLob() != null) {
        		if(!NbaUtils.isBlankOrNull(autoProcess.getWork().getNbaLob().getPolicyNumber())){
        			errMsg.append(", Policy Number - "+autoProcess.getWork().getNbaLob().getPolicyNumber());
        		} else {
        			errMsg.append(", CRDA - "+autoProcess.getWork().getID());
        		}
        	}
        	autoProcessResult.setErrorMessage(errMsg.toString());
        }
        //End NBLXA-2497
        return autoProcessResult;
    }

    /**
     * This method will process the NbaBaseException thrown by the automated process. If the exception is fatal, it will stop the autoprocess.
     * 
     * If it is non fatal, it will send the work item to error queue unless the message is a 
     * <code> SYS0116 - No suspension record was found for the case/transaction specified </code> error. 
     * 
     * SYS0116 errors are returned from the workflow system when a retrieve is performed by an automated procees for a 
     * related work item while that work item is being unsuspended in the workflow system by other processing. Once the 
     * workflow system completes the unsuspension, retrieval of the work item processes correctly. Therefore this processing 
     * ignores the SYS0116 error and the original getwork work item is simply unlocked. A subsequent getwork will re-retrieve 
     * the original work item and processing will continue normally.
     * @param userVO 
     * @param dst
     * @param accelResult
     * @param exp
     */
    //APSL3874,NBLXA-2048 Changed Signature
    protected void processBaseException(NbaAutomatedProcess autoProcess, AutoProcessResult autoProcessResult, NbaBaseException exp, NbaUserVO userVO, NbaDst dst) {
        if (exp.isFatal()) {
            setException(exp, autoProcessResult, autoProcess.getUser().getSessionKey(), autoProcess.getClass().getName()); //APSL3874
        } else {
            if (exp.getMessage() != null
                    && ((exp.getMessage().indexOf(A_ERR_NO_SUSPENSION) > -1) || (exp.getMessage().indexOf(A_ERR_AWD_TIMEOUT) > -1))) { // AXAL3.7.68,
                                                                                                                                       // ALII2032 //APSL3874
                // Status of other will indicate the job that it has to do a getwork again.
                autoProcessResult.setStatus(AutoProcessResult.RETRY); // NBA213
                autoProcessResult.setUserSessionKey(autoProcess.getUser().getSessionKey());// NBA213
            } else {
                try {
                	if (exp.getMessage() != null
                            && ((exp.getMessage().indexOf(A_ERR_APP_HLD_LOCKED_BY_ANOTHER_USER) == -1) )) {//NBLXA-2619
                		routeToErrorStatus(autoProcess, autoProcessResult, exp, userVO, dst); //APSL3874,NBLXA-2048
                	} 
                	//Begin NBLXA-2619
                	else {
                		addComment(dst, userVO, A_ERR_APP_HLD_LOCKED_BY_ANOTHER_USER , "AppHold");
                		doNbaLockedProcessing(exp, userVO, dst, autoProcessResult);//NBLXA-2619
                	}
                	//End NBLXA-2619
                } catch (Exception exp2) {
                    // if not able to route the case to error queue, stop the process
                	setException(exp2, autoProcessResult, autoProcess.getUser().getSessionKey(), autoProcess.getClass().getName()); //APSL3874
                }
            }
        }
    }
    


    /**
     * This method suspends the workitem.
     * @param nbaUserVO the user VO used by nbA.
     * @param nbaDst The workitem to suspend.
     */
    protected void suspendWorkItem(NbaUserVO nbaUserVO, NbaDst nbaDst) throws NbaBaseException {
        NbaSuspendVO suspendVO = new NbaSuspendVO();
        try {
            AccelResult res = (AccelResult)callBusinessService("NbaRetrieveTimeStampBP", nbaUserVO);
            processResult(res);
            NbaDst timeStamp = (NbaDst)res.getFirst();
            //NBA208-32
            java.util.Date activateDateTime = NbaUtils.getDateFromStringInAWDFormat(timeStamp.getTimestamp());

            //get LockedChildSuspend value from Configuration file
            String sSuspend = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.LOCKED_CHILD_SUSPEND);
            int suspendSeconds = Integer.parseInt(sSuspend);
            activateDateTime.setTime(activateDateTime.getTime() + (suspendSeconds * 1000));

            suspendVO.setActivationDate(activateDateTime);
            suspendVO.setSuspendCode(NbaConstants.AWD_SUSPEND_REASON_NOWORK);
            //set the work item id
            if (nbaDst.isCase()) {
                suspendVO.setCaseID(nbaDst.getID());
            } else {
                suspendVO.setTransactionID(nbaDst.getID());
            }
            
            //ALNA212 commented
            /*String notLockedSuspendReason = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
                    NbaConfigurationConstants.NOT_LOCKED_SUSPEND_REASON_MSG); 
            if (notLockedSuspendReason == null){
                notLockedSuspendReason = "Portions of this case are currently unavailable and cannot be processed at this time";
            } 
            addComment(nbaDst, nbaUserVO, notLockedSuspendReason , "GetWork");*/
 
            long startTime = -1L;
            Date beginTime = null;
            if (getLogger().isDebugEnabled()){
                beginTime = new Date();
                startTime = System.currentTimeMillis();
            }
            
            nbaDst.setNbaUserVO(nbaUserVO);
            AccelResult result = (AccelResult)callBusinessService("NbaUpdateWorkBP", nbaDst);
            processResult(result);
            
            // Now suspend
            suspendVO.setNbaUserVO(nbaUserVO);
            result = (AccelResult)callBusinessService("NbaSuspendWorkBP", suspendVO);
            processResult(result);
            
            if (getLogger().isDebugEnabled()){
                getLogger().logDebug("suspendWork," + nbaUserVO.getUserID() + ",DURATION"
                        + String.valueOf(System.currentTimeMillis() - startTime) + ",BeginTime:" + beginTime + ",EndTime:" + new Date());
            }

           //AXAL3.7.68 code deleted.  RETRY from the AutoProcessJob will force a re-logon there.
        } catch (NbaBaseException e) {
            getLogger().logException(e);
            throw e;
        } catch (Throwable t) {
            getLogger().logException(t);
            NbaBaseException e = new NbaBaseException("Error suspending work item", t);
            throw e;
        }
    }

    /**
     * This method unlocks the work and removes the entry from NBA_CONTRACT_LOCK table in the database.
     * @param dst The workitem to suspend.
     * @param userVO the user VO used by nbA.
     */
    protected void unlockWork(NbaDst dst, NbaUserVO userVO) throws Exception {
        if (dst.getID() != null) {
            try { // APSL3160
                dst.setNbaUserVO(userVO);
                AccelResult result = new AccelResult();
                result.merge(callBusinessService("NbaUnlockWorkBP", dst));
                processResult(result);
            } catch (Exception ex) { // APSL3160
                getLogger().logException(ex);
                throw ex;
            } finally {// APSL3160
                NbaContractLock.removeLock(dst, userVO);
            }
        }
    }

    /**
     * If the exception represents an unsuccessfull getwork(), set the status on the result object to NOWORK to cause the auto process to go into sleep mode.
     * Otherwise set the status on the result object to EXCEPTION. This indicates that the automated process should be error stopped.
     * @param exp the user VO used by nbA.
     * @param result The result object that takes back the status and error message.
     * @param accelResult The result object that takes back AutoProcessResult object.
     */
    protected void setException(Throwable exp, AutoProcessResult autoProcessResult, String sessionKey, String originatorName) {
        if (exp.getMessage().startsWith(A_ERR_NOWORK)){
            autoProcessResult.setStatus(AutoProcessResult.NOWORK);
            return;
        }        
        getLogger().logException(exp);
        autoProcessResult.setStatus(AutoProcessResult.EXCEPTION);
        autoProcessResult.setErrorMessage(exp.getMessage());
        autoProcessResult.setUserSessionKey(sessionKey); 
        try{
            
            NotificationEvent eventDO = new NotificationEvent();
            eventDO.setEventType(NotificationEvent.PROCESS_ERROR_STOP);

            StringBuffer summary = new StringBuffer("nbA : ").append(originatorName).append(" error stopped on ").append(new Date())
                    .append(" due to ").append(exp.getMessage());
            eventDO.setSummary(summary.toString());
            eventDO.setOriginator(originatorName);
    
            StringWriter sw = new StringWriter();
            exp.printStackTrace(new PrintWriter(sw));
            eventDO.setDescription(summary + "\r\n\r\n" + sw.toString());
            
            Result serviceResult = new AccelResult();
            serviceResult.addResult(eventDO);   //NBA196
            
            if (getLogger().isDebugEnabled()){
                getLogger().logDebug("Calling notification service for automated process " + originatorName);
            }
            
            //begin NBA196
            serviceResult = callService(ServiceCatalog.NOTIFICATION_HANDLER_SERVICE,serviceResult);
            if (serviceResult.hasErrors()) {
                List list = serviceResult.getMessagesList();
                Iterator it = list.iterator();
                StringBuffer buff = new StringBuffer(autoProcessResult.getErrorMessage());
                buff.append("\nNotification processing failed:");
                while (it.hasNext()) {
                    buff.append("\n").append(((Message) it.next()).formatMessageText());
                }
                autoProcessResult.setErrorMessage(buff.toString());
            }
            //end NBA196 
        }catch(Throwable t){
            getLogger().logError(t);
        }
    }
    
    /**
    * This methods routes the work item to error queue an sets the error message on the workitem.
    * @param userVO the user VO used by nbA
    * @param dst The workitem to be routed to error queue
    * @param accelResult takes back the result values
    */
    //APSL3874,NBLXA-2048 Changed Signature
    protected void routeToErrorStatus(NbaAutomatedProcess autoProcess, AutoProcessResult autoProcessResult, Throwable exp, NbaUserVO userVO, NbaDst dst) throws NbaBaseException { //NBA196
        getLogger().logException("Error while processing work item " + autoProcess.getWork().getID(), exp);
        boolean isLockException=false;
        if (autoProcess.getWork().getID() != null) {
            //APSL3874 code deleted
            Status err = AxaStatusDefinitionLoader.determineError(autoProcess.getUser().getUserID(), "TECH"); //APSL3874
            NbaProcessingErrorComment npec = new NbaProcessingErrorComment();
            //Begin APSL3874
            if (!NbaUtils.isBlankOrNull(err.getRoutingReason())) {
                autoProcess.getWork().getNbaLob().setRouteReason(AxaStatusDefinitionHelper.determineRoutingReason(err, autoProcess)); //APSL3874
            } else {
                NbaUtils.setRouteReason(autoProcess.getWork(), err.getStatusCode()); //APSL3874
            }
            //End APSL3874
            npec.setActionAdd();
            npec.setOriginator(autoProcess.getUser().getUserID()); //APSL3874
            npec.setEnterDate(NbaUtils.getStringFromDate(new Date()));
            npec.setProcess(autoProcess.getUser().getUserID()); //APSL3874
            //APSL3874 code deleted
            npec.setText(AxaStatusDefinitionHelper.determineComment(err, autoProcess, exp) + exp.getClass().getName() + ": "
                    + exp.getStackTrace()[0].toString()); // APSL3874
            autoProcess.getWork().setStatus(AxaStatusDefinitionHelper.determineStatus(err, autoProcess)); //APSL3874
            autoProcess.getWork().addManualComment(npec.convertToManualComment()); //APSL3874
            autoProcess.getWork().setUpdate(); //APSL3874
            long startTime = -1L;
            Date beginTime = null;
            if (getLogger().isDebugEnabled()) {
                beginTime = new Date();
                startTime = System.currentTimeMillis();
            }
            autoProcess.getWork().setNbaUserVO(autoProcess.getUser()); //APSL3874
            //Begin NBLXA-2048
            try{
 	            AccelResult updateresult = (AccelResult) callBusinessService("NbaUpdateWorkBP", autoProcess.getWork());
 	            processResult(updateresult);
             } catch(NbaBaseException nbaBaseException){
            	 if (getLogger().isDebugEnabled()) {
                     getLogger().logDebug("autoProcess.getUser().getUserID() :"+autoProcess.getUser().getUserID());
                 }
             	if(nbaBaseException.getMessage()!=null && nbaBaseException.getMessage().toUpperCase().indexOf("[OBJECT NOT LOCKED") != -1
             		&& !skipPollerToSuspendList.contains(autoProcess.getUser().getUserID())	
             			){
             		isLockException=true;
             		doNbaLockedProcessing(nbaBaseException, userVO, dst, autoProcessResult);
             	} else{
             		throw nbaBaseException;
             	}
             }
            //End NBLXA-2048
            if (getLogger().isDebugEnabled()) {
                getLogger().logDebug(
                        "routeToErrorQueue," + autoProcess.getUser().getUserID() + ",DURATION" + String.valueOf(System.currentTimeMillis() - startTime)
                                + ",BeginTime:" + beginTime + ",EndTime:" + new Date()); //APSL3874
            }
        }
        if(!isLockException){
        	autoProcessResult.setStatus(AutoProcessResult.FAILURE); //NBA196
        }
        autoProcessResult.setUserSessionKey(autoProcess.getUser().getSessionKey());    //NBA196 APSL3874
    }

    
    /**
     * This methods routes the work item to error queue an sets the error message on the workitem.
     * @param userVO the user VO used by nbA
     * @param dst The workitem to be routed to error queue
     * @param accelResult takes back the result values
     */
     //NBLXA-2048 overload method for smart poller
	protected void routeToErrorStatus(NbaAutomatedProcess autoProcess, AutoProcessResult autoProcessResult, String procUserId, NbaUserVO userVO,
			NbaDst dst) throws NbaBaseException { 
		if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Error while processing work item " + autoProcess.getWork().getID());
            getLogger().logDebug("smartErrorStopThresholdValue ::" + smartErrorStopThresholdValue);
        }
		
		if (autoProcess.getWork().getNbaLob() != null) {
			if (getLogger().isDebugEnabled()) {
	            getLogger().logDebug("routeToErrorStatus PolicyNumber : " + autoProcess.getWork().getNbaLob().getPolicyNumber());
	        }
		}
		boolean isLockException = false;
		if (autoProcess.getWork().getID() != null) {
			Status err = AxaStatusDefinitionLoader.determineError(procUserId, "SMARTTECH"); // APSL3874
			NbaProcessingErrorComment npec = new NbaProcessingErrorComment();
			if (!NbaUtils.isBlankOrNull(err.getRoutingReason())) {
				autoProcess.getWork().getNbaLob().setRouteReason(AxaStatusDefinitionHelper.determineRoutingReason(err, autoProcess)); // APSL3874
			} else {
				NbaUtils.setRouteReason(autoProcess.getWork(), err.getStatusCode()); // APSL3874
			}
			npec.setActionAdd();
			npec.setOriginator(autoProcess.getUser().getUserID()); // APSL3874
			npec.setEnterDate(NbaUtils.getStringFromDate(new Date()));
			npec.setProcess(autoProcess.getUser().getUserID()); // APSL3874
			npec.setText(autoProcessResult.getErrorMessage());
			autoProcess.getWork().setStatus(AxaStatusDefinitionHelper.determineStatus(err, autoProcess)); // APSL3874
			autoProcess.getWork().addManualComment(npec.convertToManualComment()); // APSL3874
			autoProcess.getWork().setUpdate(); // APSL3874
			long startTime = -1L;
			Date beginTime = null;
			if (getLogger().isDebugEnabled()) {
				beginTime = new Date();
				startTime = System.currentTimeMillis();
			}
			autoProcess.getWork().setNbaUserVO(autoProcess.getUser()); // APSL3874
			try {
				AccelResult updateresult = (AccelResult) callBusinessService("NbaUpdateWorkBP", autoProcess.getWork());
				processResult(updateresult);
			} catch (NbaBaseException nbaBaseException) {
				if (nbaBaseException.getMessage() != null 
						&& nbaBaseException.getMessage().toUpperCase().indexOf("[OBJECT NOT LOCKED") != -1
						&& !skipPollerToSuspendList.contains(autoProcess.getUser().getUserID())) {
					doNbaLockedProcessing(nbaBaseException, userVO, dst, autoProcessResult);
					isLockException = true;
				} else {
					throw nbaBaseException;
				}
			}
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug(
						"routeToErrorQueue," + autoProcess.getUser().getUserID() + ",DURATION"
								+ String.valueOf(System.currentTimeMillis() - startTime) + ",BeginTime:" + beginTime + ",EndTime:" + new Date()); // APSL3874
			}
		}
		if (!isLockException) {
			autoProcessResult.setStatus(AutoProcessResult.SMART_FAILURE); // NBA196
		}
		autoProcessResult.setUserSessionKey(autoProcess.getUser().getSessionKey()); // NBA196 APSL3874
	}


    /**
     * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
     * 
     * @return the logger implementation
     */
    protected NbaLogger getLogger() {
        if (logger == null) {
            try {
                logger = NbaLogFactory.getLogger(this.getClass());
            } catch (Exception e) {
                NbaBootLogger.log("NbaAutoProcessUtils could not get a logger from the factory.");
                e.printStackTrace(System.out);
            }
        }
        return logger;
    }

    /**
     * This method will be called when an automated process tries to lock a matching workitem from a different case and that workitem is already 
     * locked by another user.
     * @param lockedExp
     * @param userVO
     * @param dst
     * @param accelResult
     * @return the logger implementation
     */
    protected void doNbaLockedProcessing(Exception lockedExp, NbaUserVO userVO, NbaDst dst, AutoProcessResult autoProcessResult) { //NBA213,NBLXA-2048
        //suspend the case and increase failure count by 1
        getLogger().logException(lockedExp);
        try {
            suspendWorkItem(userVO, dst);
        } catch (Exception exp) {
            //if problem in suspending the case or unlocking the work, log and continue
            getLogger().logException(exp);
        }
        //Status of other will indicate the job that it has to do a getwork again.
        autoProcessResult.setStatus(AutoProcessResult.RETRY); //NBA213
        autoProcessResult.setUserSessionKey(userVO.getSessionKey());//NBA213
    }

    /**
     * This method will be overridden by the autoprocessBP classes that will be extending this class in future.
     */
    public Result process(Object input) {
        return new AccelResult();
    }
    
    /**
     * Adds new error comments to the AWD system.
     * @param user the AWD user 
     * @param workItem the AWD work item, typically a case, to add the error comments to
     * @param process the process which produced the errors
     * @param comments the errors to be added to the AWD system.
     */
    protected void addErrorComments(NbaUserVO user, NbaDst workItem, String process, String[] comments) {
        try {
            NbaAutoProcessProxy apProxy = new NbaAutoProcessProxy(user, workItem, false); // NBA035
            for (int i = 0; i < comments.length; i++) {
                apProxy.addComment(comments[i], process);
            }
        } catch (Exception e) {
            // suppress the exception because the calling code has no good way to handle it
        }
    }

    /**
     * Adds a new comment to the AWD system.
     * @param work the NbaDst workitem
     * @param user the user value object
     * @param aComment the comment to be added to the AWD system.
     * @param aProcess the process that added the comment.
     */ 
    protected void addComment(NbaDst work, NbaUserVO user, String aComment, String aProcess) {
        NbaProcessingErrorComment npec = new NbaProcessingErrorComment();
        npec.setActionAdd();
        npec.setOriginator(user.getUserID());
        npec.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
        npec.setProcess(aProcess);
        npec.setText(aComment);
        work.addManualComment(npec.convertToManualComment());
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Comment added: " + aComment);
        }
    }

    /**
     * This method creates a NbaUserVO object that will be used for processing of workitems within nbA
     * @param userID The user id of current user.
     * @param password The password of current user
     * @param userSessionKey The user session key
     * @param netServerHost Netserver Host name
     */
    // AXAL3.7.68 - Added
    public NbaUserVO createUserVO(ExecuteAutoProcessVO vo) {
        NbaUserVO userVO = new NbaUserVO(vo.getUserId(), vo.getPassword(), true, vo.getNetServerHost());
        userVO.setSessionKey(vo.getSessionKey());
        userVO.setTokens(vo.getSecurityTokens());
        return userVO;
    }
    
    //APSL3874 New Method
    protected void processErrorStatusException(NbaAutomatedProcess autoProcess, AutoProcessResult autoProcessResult, AxaErrorStatusException exp) {
        if (exp.isFatal()) {
            setException(exp, autoProcessResult, autoProcess.getUser().getSessionKey(), autoProcess.getClass().getName());
        } else {
            if (exp.getMessage() != null
                    && ((exp.getMessage().indexOf(A_ERR_NO_SUSPENSION) > -1) || (exp.getMessage().indexOf(A_ERR_AWD_TIMEOUT) > -1))) {
                // Status of other will indicate the job that it has to do a getwork again.
                autoProcessResult.setStatus(AutoProcessResult.RETRY);
                autoProcessResult.setUserSessionKey(autoProcess.getUser().getSessionKey());
            } else {
                try {
                    determineError(autoProcess, exp);
                    autoProcessResult.setStatus(AutoProcessResult.FAILURE);
                    autoProcessResult.setUserSessionKey(autoProcess.getUser().getSessionKey());
                } catch (Exception exp2) {
                    // if not able to route the case to error queue, stop the process
                    setException(exp2, autoProcessResult, autoProcess.getUser().getSessionKey(), autoProcess.getClass().getName());
                }
            }
        }
    }
    
    //APSL3874 New Method
    protected void determineError(NbaAutomatedProcess autoProcess, AxaErrorStatusException exp) throws NbaBaseException {
        if (autoProcess.getWork().getID() != null) {
            Status err = AxaStatusDefinitionLoader.determineError(autoProcess.getUser().getUserID(), exp.getVariance());
            NbaProcessingErrorComment npec = new NbaProcessingErrorComment();
            if (!NbaUtils.isBlankOrNull(err.getRoutingReason())) {
                autoProcess.getWork().getNbaLob().setRouteReason(AxaStatusDefinitionHelper.determineRoutingReason(err, autoProcess));
            } else {
                NbaUtils.setRouteReason(autoProcess.getWork(), err.getStatusCode());
            }
            npec.setActionAdd();
            npec.setOriginator(autoProcess.getUser().getUserID());
            npec.setEnterDate(NbaUtils.getStringFromDate(new Date()));
            npec.setProcess(autoProcess.getUser().getUserID());
            npec.setText(AxaStatusDefinitionHelper.determineComment(err, autoProcess, exp));
            autoProcess.getWork().setStatus(AxaStatusDefinitionHelper.determineStatus(err, autoProcess));
            autoProcess.getWork().addManualComment(npec.convertToManualComment());
            autoProcess.getWork().setUpdate();
            long startTime = -1L;
            Date beginTime = null;
            if (getLogger().isDebugEnabled()) {
                beginTime = new Date();
                startTime = System.currentTimeMillis();
            }
            autoProcess.getWork().setNbaUserVO(autoProcess.getUser());
            AccelResult updateresult = (AccelResult) callBusinessService("NbaUpdateWorkBP", autoProcess.getWork());
            processResult(updateresult);
            if (getLogger().isDebugEnabled()) {
                getLogger().logDebug(
                        "determineError," + autoProcess.getUser().getUserID() + ",DURATION" + String.valueOf(System.currentTimeMillis() - startTime)
                                + ",BeginTime:" + beginTime + ",EndTime:" + new Date());
            }
        }
    }
}
