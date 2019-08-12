package com.csc.fsg.nba.process.correspondence;

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

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.correspondence.NbaCorrespondenceAdapter;
import com.csc.fsg.nba.correspondence.NbaCorrespondenceAdapterFactory;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaCorrespondenceRequestVO;

/**
 * Creates the correspondence letter and return the letter in the form of PDF or HTML as per request
 * Accepts a <code>NbaCorrespondenceRequestVO</code> as input to create a correspondence extract
 * The dst, letter, and NbaUserVO are required on the value object.
 * Returns a <code>NbaCorrspondenceResponseVO </code> with the extract and keys poulated
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA184</td><td>Version 7</td><td>Correspondence Rewrite</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class CreateCorrespondenceBP extends NewBusinessAccelBP {

    /* (non-Javadoc)
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            NbaCorrespondenceRequestVO corrRequestVO = (NbaCorrespondenceRequestVO) input;
            NbaCorrespondenceAdapter adapter = new NbaCorrespondenceAdapterFactory().getAdapterInstance();
            adapter.initializeObjects(corrRequestVO.getDst(), corrRequestVO.getNbaUserVO());
            if (NbaCorrespondenceRequestVO.FORMAT_PDF.equals(corrRequestVO.getAction())) {
                result.addResult(adapter.getLetterAsPDF(corrRequestVO.getLetter(), corrRequestVO.getXmlExtract()));
            } else if (NbaCorrespondenceRequestVO.FORMAT_HTML.equals(corrRequestVO.getAction())) {
                result.addResult(adapter.getLetterAsHTML(corrRequestVO.getLetter()));
            }
        } catch (Exception e) {
            result = new AccelResult();
            addExceptionMessage(result, e);
        }
        return result;
    }
}
