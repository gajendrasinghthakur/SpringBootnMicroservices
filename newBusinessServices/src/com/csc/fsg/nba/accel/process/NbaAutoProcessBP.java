/*************************************************************************
*
* Copyright Notice (2005)
* (c) CSC Financial Services Limited 1996-2005.
* All rights reserved. The software and associated documentation
* supplied hereunder are the confidential and proprietary information
* of CSC Financial Services Limited, Austin, Texas, USA and
* are supplied subject to licence terms. In no event may the Licensee
* reverse engineer, decompile, or otherwise attempt to discover the
* underlying source code or confidential information herein.
*
*************************************************************************/


package com.csc.fsg.nba.accel.process;

import com.csc.fs.Result;
import com.csc.fs.accel.console.result.AutoProcessResult;
import com.axa.fs.accel.console.valueobject.ExecuteAutoProcessVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * Business Process to execute an Automated Process.
 * 
 * <br><b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead> 
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>AXAL3.7.68</td><td>AXA Life Phase 1</td><td>LDAP Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class NbaAutoProcessBP extends NbaAutoProcessAccelBP {
    protected static final String DOCUMENT_INPUT = "documentInput";
	public Result process(Object request) {
        ExecuteAutoProcessVO autoProcessVO = (ExecuteAutoProcessVO) request;
        NbaUserVO userVO = createUserVO(autoProcessVO);
 //       NbaUserVO userVO = createUserVO(autoProcessVO.getUserId(), autoProcessVO.getPassword(), autoProcessVO.getSessionKey(),
  //              autoProcessVO.getNetServerHost());
            
        if (DOCUMENT_INPUT.equalsIgnoreCase(autoProcessVO.getGetWorkAlternate())) {
            return processDocumentInput(userVO);
        }
        return processGetWork(userVO);
    }
    /**
     * Invoke the Document Input automated process. The Document Input process
     * determines, based on configuration information, if File System records are 
     * present for which Workflow system objects should be created.
     * @param userVO
     * @param dst
     * @return Result
     */
	protected Result processDocumentInput(NbaUserVO userVO) {
	    return executeAutoProcess(userVO, new NbaDst());
    }
    /**
     * Perform a workflow system getWork() to determine if an eligible work item is 
     * ready for processing. If so, invoke the appropriate automated process.
     * @param userVO
     * @param dst
     * @return Result
     */
    protected Result processGetWork(NbaUserVO userVO) {
        NbaDst dst = null;
        AutoProcessResult autoProcessResult = new AutoProcessResult();
        //Look for work. If no work, return, if error during getwork, remove
        // the lock and stop the process. If error during
        //removing the lock, log it.
        getWork(userVO, autoProcessResult);
        if (autoProcessResult.getStatus() == AutoProcessResult.EXCEPTION || autoProcessResult.getStatus() == AutoProcessResult.NOWORK) {
            return autoProcessResult;
        }
        if (autoProcessResult.getFirst() instanceof NbaDst) {
            //if dst is returned, invoke the autoprocess
            dst = (NbaDst) autoProcessResult.getFirst();
            return executeAutoProcess(userVO, dst);
        }
        autoProcessResult.setStatus(AutoProcessResult.EXCEPTION);
        autoProcessResult.setErrorMessage("Invalid response from get work");
        return autoProcessResult;
    }
}
 
