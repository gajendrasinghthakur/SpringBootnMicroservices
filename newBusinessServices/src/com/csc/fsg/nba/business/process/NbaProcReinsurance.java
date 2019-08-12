package com.csc.fsg.nba.business.process;

/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which are
 * proprietary to CSC Financial Services Group®.  The use, reproduction,
 * distribution or disclosure of this program, in whole or in part, without
 * the express written permission of CSC Financial Services Group is prohibited.
 * This program is also an unpublished work protected under the copyright laws
 * of the United States of America and other countries.
 *
 * If this program becomes published, the following notice shall apply:
 *    Property of Computer Sciences Corporation.<BR>
 *    Confidential. Not for publication.<BR>
 *    Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */
 
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * NbaProcReinsurance will be used to determine if automatic or facultative 
 * work items will be generated to be routed to the reinsurance administration queue.  
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA038</td><td>Version 3</td><td>Reinsurance</td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 * @see NbaAutomatedProcess
 */

public class NbaProcReinsurance extends NbaAutomatedProcess {

	/**
	 * This process will call the VP/MS model to determine if automatic or facultative 
 	 * work items will be generated.
	 * @param user the user for whom the process is being executed
	 * @param work a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about
	 *         the success or failure of the process
	 */
	//SPR1851 Remove NbaAutoProcessNotLockedException
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user,NbaDst work)throws NbaBaseException {
		if (!initialize(user, work)) {
			return getResult();
		}
		
		//Get work item type and initial status from VP/MS		
		NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork());
		if(provider.getWorkType() != null && provider.getWorkType().trim().length() > 0){
			NbaTransaction nbaTrans = getWork().addTransaction(provider.getWorkType(), provider.getInitialStatus()); 
			nbaTrans.increasePriority(provider.getWIAction(), provider.getWIPriority());
			NbaLob transLob = nbaTrans.getNbaLob();
			NbaLob caseLob = getWork().getNbaLob();
			transLob.setFaceAmount(caseLob.getFaceAmount());
			
		} 
		result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus());
		changeStatus(getPassStatus());
		doUpdateWorkItem(); // also unlocks the case		
		return result;
	}
}
