package com.csc.fsg.nba.process.email;

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

import com.csc.fs.Message;
import com.csc.fs.Result;
import com.csc.fs.ResultImpl;
import com.csc.fs.accel.constants.ServiceCatalog;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.EmailRequest;
import com.csc.fs.dataobject.accel.EmailResponse;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaServiceLocator;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaEmailVO;

/**
 * SendEmailBP sends an email notification using the EMAIL system service.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA196</td><td>Version 7</td><td>JCA Adapter for Email</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class SendEmailBP extends NewBusinessAccelBP {
    protected NbaServiceLocator svcLoc = null;

    /**
     * Sends an email notification using the EMAIL system service. The EmailRequest value/data object is constructed using the values
     * from the NbaEmailVO object in the request.
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        //begin NBA196
        try {            
            if (input instanceof NbaEmailVO) {                
                NbaEmailVO nbaEmailVO = (NbaEmailVO) input;
                EmailRequest emailVO = new EmailRequest(nbaEmailVO.getTo(), nbaEmailVO.getSubject(), nbaEmailVO.getReplyTo(), nbaEmailVO
                        .getBody());
                Result re = callService(ServiceCatalog.EMAIL_SERVICE, (new ResultImpl()).addResult(emailVO));
                getMessages(result, re);
                return result;                
            }
        //end NBA196
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

    /**
     * Get any messages from the EmailResponse in the serviceResult and add them
     * to the result
     * @param result
     * @param serviceResult
     */
    //NBA196 New Method
    protected void getMessages(Result result, Result serviceResult) {
        List data = serviceResult.getData();
        Iterator it = data.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof EmailResponse) {
                EmailResponse emailResponse = (EmailResponse) obj;
                if (!NbaConstants.TRUE_STR.equalsIgnoreCase(emailResponse.getSuccess())) {
                    result.setErrors(true);
                    Message msg = new Message();
                    String msgdata[] = new String[] { emailResponse.getMessage() };
                    msg = msg.setVariableData(msgdata);
                    result.addMessage(msg);
                }
            }
        }
    }
}
