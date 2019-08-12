package com.csc.fs.dataobject.accel.FA;

/*
 *******************************************************************************<BR>
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
 * 
 *******************************************************************************<BR>
 */
import com.csc.fs.dataobject.accel.AccelDataObject;
/**
 * FileMessageDO data object contains a file i/o response message.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA188</td><td>Version 7</td><td>XML sources to NBAAUXILIARY</td></tr>
 * </table>
 * <p>
 */
public class FileMessageDO extends AccelDataObject {
    private String msg;
    private String severity;

    /**
     * Retrieve the message contents
     * @return the msg.
     */
    public String getMsg() {
        return msg;
    }

    /**
     * Retrieve the message error severity
     * @return the severity.
     */
    public String getSeverity() {
        return severity;
    }

    /**
     * Set the value of the message.
     * @param msg The message to set.
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * Set the error severity.
     * @param severity The severity to set.
     */
    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
 
