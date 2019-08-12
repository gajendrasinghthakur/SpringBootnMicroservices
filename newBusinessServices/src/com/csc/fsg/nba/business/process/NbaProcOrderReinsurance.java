package com.csc.fsg.nba.business.process;
/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */
import java.util.HashMap;
import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.reinsuranceadapter.NbaReinsuranceAdapter;
import com.csc.fsg.nba.reinsuranceadapter.NbaReinsuranceAdapterFacade;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * NbaProcOrderReinsurance is the class that processes nbAccelerator work items
 * found on the AWD order reinsurance queue (NBORDREN). This process will generate 
 * an Reinsurer-Ready  message and associate that message with the work item.  
 * When this work item moves to its next queue, the reinsurer-ready transaction generated 
 * by this process will be sent to the reinsurer.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA038</td><td>Version 3</td><td>Reinsurance</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaProcOrderReinsurance extends NbaAutomatedProcess {

	/**
	 * Defaut NbaProcOrderReinsurance constructor.
	 */
	public NbaProcOrderReinsurance() {
		super();
	}
	/**
	 * This process will generate an Reinsurer-Ready  message and associate that message with the work item.  
 	 * When this work item moves to its next queue, the reinsurer-ready transaction generated 
 	 * by this process will be sent to the reinsurer.
	 * @param user the user for whom the process is being executed
	 * @param work a DST value object for which the process is to occur
	 * @return an NbaAutomatedProcessResult containing information about
	 *         the success or failure of the process
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		// Initialization
		if (!initialize(user, work)) {
			return getResult(); //NBA050
		}
		//retrieve sources
		retrieveWork();
		orderReinsurance();
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		changeStatus(getPassStatus());
		doUpdateWorkItem();
		return getResult();
	}
	
	/**
	 * This method first creates and adds XMLife message to the each work item. Then
	 * It call reinsurance adapter to transform XMLife message to the reinsurance ready message.
	 * When reinsurance ready message is receipt, it associates it to workitem as a source.
	 */
	protected void orderReinsurance() throws NbaBaseException {

		HashMap reqMsg = null;
		//call provider to get provider ready message
		NbaReinsuranceAdapterFacade facade = new NbaReinsuranceAdapterFacade(getWork(), getUser());
		NbaTXLife xml552 = getXMLTrnsaction();
		reqMsg = (HashMap) facade.convertXmlToReinsurerFormat(xml552, getUser());
		//if original work item has error return here and set original work item status to fail status
		if (reqMsg.get(xml552.getTransRefGuid()) != null) {
			addComment((String) reqMsg.get(xml552.getTransRefGuid()));
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, getFailStatus(), getFailStatus()));
			return;
		}
		NbaSource source =
			new NbaSource(
				getWork().getBusinessArea(),
				NbaConstants.A_ST_REINSURANCE_TRANSACTION,
				(String) reqMsg.get(NbaReinsuranceAdapter.TRANSACTION));
		getWork().addNbaSource(source);
	}
	/**
	 * Retrieve the original work item with sources
	 * 
	 */
	protected void retrieveWork() throws NbaBaseException {
		//NBA213 deleted code
		//create and set retrieve option
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false);
		retOpt.setLockWorkItem();
		retOpt.requestSources();
		setWork(retrieveWorkItem(getUser(), retOpt));  //NBA213
		//NBA213 deleted code
	}

	/**
	 * Returns XML 552 transaction from workitem.
	 * @return the XML 552 transaction
	 */
	protected NbaTXLife getXMLTrnsaction() {
		List list = getWork().getNbaSources();
		for (int i = 0; i < list.size(); i++) {
			NbaSource source = (NbaSource) list.get(i);
			if (NbaConstants.A_ST_REINSURANCE_XML_TRANSACTION.equals(source.getSource().getSourceType())) {
				try {
					return new NbaTXLife(source.getText());
				} catch (Exception e) {
					return null;
				}
			}
		}
		return null;
	}
}
