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
 * FileAccessDO Domain Object contains the request code for a file access operation.
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
public class FileAccessDO extends AccelDataObject {
    private String requestCode;

    /**
     * Retrieve the value of the requestCode.requestCode
     * @return the requestCode.
     */
    public String getRequestCode() {
        return requestCode;
    }

    /**
     * Set the value of requestCode.
     * @param requestCode The requestCode to set.
     */
    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }
}
