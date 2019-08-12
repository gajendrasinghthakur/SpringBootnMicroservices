package com.csc.fsg.nba.business.process;
/*
 * ************************************************************** <BR>
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
 * ************************************************************** <BR>
 */
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.reinsuranceadapter.NbaReinsuranceAdapterFacade;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * <code>NbaProcReinsurerIndexing</code> calls provider adapter to update LOBs. When reinsurance 
 * request are ordered by the system, the results of those request may be received from the reinsurer
 * electronically (HTTPS, FTP, dial-up service), via FAX or scanned into the system.  The results will be 
 * RIPed into the system as a temporary work item (NBTEMPREN) with the result source attached to it.
 * For results received electronically, this automated process will, if necessary, call the reinsurer adapter 
 * to transform the received message into another message format and then update the LOB fields 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA038</td><td>Version 3</td><td>Reinsurance</td></tr>
 * <tr><td>NBA095</td><td>Version 4</td><td>Queues Accept Any Work Type</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaProcReinsurerIndexing extends NbaAutomatedProcess {
	/**
	 * NbaProcReinsurerIndexing constructor comment.
	 */
	public NbaProcReinsurerIndexing() {
		super();
	}
	/** 
	 * This automated process calls the reinsurer adapter to update the LOB fields 
	 * so that the reinsurer Ordered process can match the temporary work item
	 * to a permanent work item.  In addition to updating the LOB fields, additional
	 * sources may be added to the work item by the reinsurer adapter. 
	 * @param user the user/process for whom the process is being executed
	 * @param work a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {

		// NBA095 - block move begin
		if (!initialize(user, work)) {
			return statusProcessFailed();
		}
		// NBA095 - block move end

		// NBA103 - removed try catch
		retrieveWork();
		NbaReinsuranceAdapterFacade reinAdapter = new NbaReinsuranceAdapterFacade(getWork(), getUser());
		NbaDst revWork = reinAdapter.processResponseFromReinsurer(getWork(), getUser());
		if (revWork == null) {
			addComment("Unable to process response from Renisurer");
			result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Failed", getFailStatus());
		} else {
			setWork(revWork);
			result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus());
		}
		changeStatus(getResult().getStatus());
		doUpdateWorkItem();
		return result;
	}
	/**
	 * This method retrieves the work item along with all of its associated sources. 
	 */
	public void retrieveWork() throws NbaBaseException {
		//NBA213 deleted code
		NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
		retrieveOptionsValueObject.setWorkItem(getWork().getID(), false);
		retrieveOptionsValueObject.requestSources();
		retrieveOptionsValueObject.setLockWorkItem();
		setWork(retrieveWorkItem(getUser(), retrieveOptionsValueObject));  //NBA213
		//NBA213 deleted code
	}
}
