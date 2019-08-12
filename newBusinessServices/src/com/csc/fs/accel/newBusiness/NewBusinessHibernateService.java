package com.csc.fs.accel.newBusiness;

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

import java.util.List;

import com.csc.fs.Message;
import com.csc.fs.Result;
import com.csc.fs.accel.AccelService;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;

/**
 * New Business Service provides an interface to invoke the accel service.
 * This class implements the abstract method execute of Accel Service
 * so that any class which extends this class will not have to implement the
 * execute method.    
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class NewBusinessHibernateService extends AccelService {
    protected static NbaLogger logger = null;

    /* (non-Javadoc)
     * @see com.csc.fs.accel.AccelService#execute(com.csc.fs.Result)
     */
    public Result execute(Result request) {
        return null;
    }
    
    /**
     * Calls the @see super.invoke() of AccelService class to invoke the service.
     *  
     * @param serviceName the service (System API or System Service) to invoke
     * @param objects the list of input AccelValueDataObjects
     * @return Result
     */
    public final Result invokeService(String serviceName, List objects) {
        return super.invoke(serviceName, objects, true);
    }

    /**
     * Process the error messages returned by the hibernate layer.
     * And returns a string of Concatenated Error Messages.
     * @param result
     * @return
     */
    public String processErrors(Result result) {
        StringBuffer errorVal = new StringBuffer();
        Message messages[] = result.getMessages();
        if (messages != null) {
            int size = messages.length;
            try {
                for (int i = 0; i < size; i++) {
                    Message message = messages[i];
                    errorVal.append(message.format());
                    if ("".equals(errorVal.toString()) || Message.ERR_MESSAGE_MISSING.equals(errorVal.toString())) {
                        List data = message.getData();
                        if (data != null && data.get(0) != null){
                            String messageStr = data.get(0).toString();
                            errorVal.append(" data[" + messageStr + "]");
                        }
                    }
                }
            } catch(Exception ex) {
                getLogger().logError(NewBusinessHibernateService.class + "Failed to obtain text from message with exception [" + ex.getMessage() + "]");
            }
            if (result.isErrors()) {
                getLogger().logError(NewBusinessHibernateService.class + "Message Returned in Result Object[" + errorVal.toString() + "]");    
            }
        }
        return errorVal.toString();
    }
    
    /**
    * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
    * @return com.csc.fsg.nba.foundation.NbaLogger
    */
    protected static synchronized NbaLogger getLogger() {
        if (logger == null) {
            try {
                logger = NbaLogFactory.getLogger(NewBusinessHibernateService.class.getName());
            } catch (Exception e) {
                NbaBootLogger.log("NewBusinessHibernateService could not get a logger from the factory.");
                e.printStackTrace(System.out);
            }
        }
        return logger;
    }

}
