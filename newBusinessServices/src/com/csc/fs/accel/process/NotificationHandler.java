package com.csc.fs.accel.process;

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

import java.util.Iterator;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.AccelService;
import com.csc.fs.accel.notification.NotificationEvent;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.util.NotificationConfigurationLoader;
import com.csc.fs.accel.valueobject.notification.configuration.EventType;
import com.csc.fs.accel.valueobject.notification.configuration.NotificationTarget;
import com.csc.fs.logging.LogHandler;

/**
 * This service will be invoked on certain events that require a notification to be sent. The name of the event is passed to this service.
 * The service reads the notification configuration to get the notification types attached with that event. Then it retrieves the information
 * about service to be invoked for different notification types and calls those services.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>NBA123</td><td>Version 6</td><td>Administrator Console Rewrite</td></tr>
 * <tr><td>NBA210</td><td>Version 7</td><td>Admin Console Notification</td></tr>
 * <tr><td>NBA196</td><td>Version 7</td><td>JCA Adapter for Email</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */

public class NotificationHandler extends AccelService {

	/**
     * This method is executed by the framework.It receives the event type in the NotificationEvent object and retrives the map containing 
     * notification types attached with that event type from the configuration file.
     * @param request - the input object for use by this service
     * @return Result - the resulting outcome from this service execution
     */
    public Result execute(Result request){
        List inputData = request.getData(); //NBA196
		Iterator inputObjects = inputData.iterator();
		Result result = Result.Factory.create();		
		try{
			while (inputObjects.hasNext()) {
				Object obj = inputObjects.next();
				if (obj instanceof NotificationEvent) {
			        NotificationEvent eventDO = (NotificationEvent) obj;
			        String event = eventDO.getEventType();
			        EventType eventType = null;
			        if (NotificationConfigurationLoader.getEventMap().containsKey(event)){
			            eventType = (EventType)NotificationConfigurationLoader.getEventMap().get(event);
			        }else {
			            result = new AccelResult();
			            result.setErrors(true);
			            addErrorMessage(result, "Event type not defined in the configuration file");
			            return result;
			        }
			        callServicesForEvent(eventDO, eventType, result); //NBA210 NBA196
				}
			}
		}catch (Throwable t){
		    LogHandler.Factory.LogError("Exception in NotificationHandler ", t.getMessage());
		    result = new AccelResult();
		    result.setErrors(true);
		    addExceptionMessage(result, t);
      }

		return result;
    }

	/**
     * Retrieves the map containing the type of notifications associated with an event. It then retrieves the services associated with that
     * notification type and invokes the servies. It passes back the success / fail message to the calling code, for each service.
     * @param request - the input object for use
     * @param result - the object containing the result
     * @param eventType - the event type   
     * @return Result - the resulting outcome from this service execution
     */
     //NBA196 changed method signature
    private void callServicesForEvent(NotificationEvent notificationEvent, EventType eventType, Result result) { //NBA210 added inputData
        AccelResult tempRequest ; //NBA196       
        //NBA196 code deleted 
        if (eventType != null){
            if (eventType.isNotification()){
                //NBA196 code deleted
                //get the list of notifications to be sent on this event 
                List notificationTargetList = eventType.getNotificationTypeList();
                if (notificationTargetList != null && notificationTargetList.size() > 0){
                    String serviceName = null;
                    String notificationType = null;
                    NotificationTarget target;
					//NBA196 code deleted
                    for (int i=0; i < notificationTargetList.size(); i++){
                        //get the notificationtype to get the notification target that would contain the information for the service 
                        notificationType = (String)notificationTargetList.get(i);
                        try{
                            tempRequest = new AccelResult(); //Create a new accelResult for each target //NBA196
                            tempRequest.addResult(notificationEvent);	//NBA196
	                        //get the notification target for the notification type 
                            target = (NotificationTarget)NotificationConfigurationLoader.getNotificationMap().get(notificationType);
                            tempRequest.addResult(target); // NBA210 NBA196
                            //read the service name from target tag in xml
                            serviceName = target.getServiceName();
                            
                            result.merge(callService(serviceName, tempRequest));	//NBA196
							//NBA196 code deleted
	                    }catch(Exception exp){
	                        LogHandler.Factory.LogError("Error in invoking the " + notificationType + " service", exp.getMessage()); 
	                        addExceptionMessage(result, exp);	//NBA196
	                    }
	                }
                }
            }
        }
		//NBA196 code deleted
    }
}
