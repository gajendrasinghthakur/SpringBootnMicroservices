package com.csc.fsg.nba.process.index;

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

import java.rmi.RemoteException;
import java.util.List;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.Result;
import com.csc.fs.accel.constants.newBusiness.ServiceCatalog;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.index.IndexCommitRequest;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

/**
 * Commit changes to the work item and sources to the workflow system. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA173</td><td>Version 7</td><td>nbA Indexing UI Rewrite Project</td></tr>
 * <tr><td>SPR3423</td><td>Version 8</td><td>Index Should Not Automatically Route Corrected Items from Error Queue - Such As Unmatched Requirement Result</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */

public class UpdateIndexWorkBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	protected static final String NBCREATE = "NBCREATE";
    protected static final String NBAUPDATEWORKBP = "NbaUpdateWorkBP";
    
    public Result process(Object input) {
    	Result result = null;
		try {
			//begin NBA331.1, APSL5055
			IndexCommitRequest request = (IndexCommitRequest) input;
			if (request.getCheckAllocations() != null) {
				result = callService(ServiceCatalog.COMMIT_CHECK_ALLOC_DISASSEMBLER, request.getCheckAllocations());
				if (!result.hasErrors()) {
					result = invoke(ServiceCatalog.COMMIT_CHECK_ALLOCATIONS, result.getData());
				}
			}
			if (result == null || !result.hasErrors()) {
				result = update(request.getNbaDst());
			}
			//end NBA331.1, APSL5055
			
		} catch (Exception e) {
			result = new AccelResult();
			addExceptionMessage(result, e);
		}
		return result;
	}

	 
	/**
	 * Determine the work item's next status and commit the work item to the workflow system.
	 * @param workDst
	 * @return
	 * @throws Exception
	 */
	protected AccelResult update(NbaDst workDst) throws Exception {
		if(isWorkInIndexingQueue(workDst)){ //SPR3423
			setStatus(workDst);
		}else{//SPR3423//ALPC168
			NbaProcessStatusProvider provider = new NbaProcessStatusProvider(new NbaUserVO(NBCREATE, ""), workDst);//ALPC168
			workDst.getWork().increasePriority(provider.getCaseAction(),provider.getCasePriority());//ALPC168
		}//ALPC168
        Result result = callService(NBAUPDATEWORKBP, workDst);
        return AccelResult.buildResult(result);
	}
	
	/**
	 * Use the <code>NbaProcessStatusProvider</code> to determine the work item's next
	 * status and priority.
	 * @param work
	 * @throws NbaBaseException
	 */
	protected void setStatus(NbaDst work) throws NbaBaseException {
        NbaProcessStatusProvider provider = new NbaProcessStatusProvider(new NbaUserVO(NBCREATE, ""), work);
        work.setStatus(provider.getPassStatus());  
        work.getWork().increasePriority(provider.getCaseAction(),provider.getCasePriority());//ALPc168
        
	}
	/**
     * Calls VPMS model to check whether the current work item is in a Indexing queue.
     * @param  workDst the Work DST object
     * @return boolean true if the current work item is in a Indexing queue, false otherwise.
     * @throws NbaBaseException
     */
	//SPR3423 New Method
    protected boolean isWorkInIndexingQueue(NbaDst workDst) throws NbaBaseException {
		NbaVpmsAdaptor rulesProxy = null;
		boolean isIndexingQueue = false;
		try {
			NbaOinkDataAccess data = new NbaOinkDataAccess();
			data.setLobSource(workDst.getNbaLob());
			rulesProxy = new NbaVpmsAdaptor(data, NbaVpmsConstants.INDEX);
			rulesProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_CHECK_INDEXING_QUEUE);
			VpmsComputeResult rulesProxyResult = rulesProxy.getResults();
			if (!rulesProxyResult.isError()) {
				NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(rulesProxyResult);
				List rulesList = vpmsResultsData.getResultsData();
				if (!rulesList.isEmpty()) {
					int result = NbaUtils.convertStringToInt((String) rulesList.get(0));
					isIndexingQueue = (result == 1);
				}
			}
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} finally {
			if (rulesProxy != null) {
				try {
					rulesProxy.remove();
				} catch (RemoteException re) {
					LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
				}
			}
		}
		return isIndexingQueue;
	}
}
