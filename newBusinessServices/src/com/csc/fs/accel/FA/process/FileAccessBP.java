package com.csc.fs.accel.FA.process;

/*
*
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

import com.csc.fs.Result;
import com.csc.fs.accel.AccelBP;
import com.csc.fs.accel.constants.FA.ServiceCatalog;

/**
 * File Access Business Process.
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
public class FileAccessBP extends AccelBP {
    /**
     * Perform the document input Process by invoking the dis-assembler, service, and assembler.
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object requestVO) {
        Result result = callService(ServiceCatalog.FILEACCESSDISASSEMBLER, requestVO); 
        if (!result.hasErrors()) {
            result = callService(ServiceCatalog.FILEACCESS, result);
            if (!result.hasErrors()) {
                result = callService(ServiceCatalog.FILEACCESSASSEMBLER, result);
            }
        }
        return result;
    }
}
