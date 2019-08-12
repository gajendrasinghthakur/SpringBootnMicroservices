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
import java.util.ArrayList;
import java.util.HashMap;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaSource;

/**
 * <code>NbaProcReinsurerMunich</code> handles communications between nbAccelerator
 * and Munich.  It extends the NbaProcReinsurerCommunications class, which drives the process,
 * and supplies Munich specific functionality.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA038</td><td>Version 3</td><td>Reinsurance</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaProcReinsurerMunich extends NbaProcReinsurerCommunications {
	/**
	 * NbaProcReinsurerMunich default constructor.
	 */
	public NbaProcReinsurerMunich() {
		super();
	}
	/**
	 * This method performs reinsurer specific processing as required for Munich. 
	 * @param aSource the <code>NbaSource</code> containing the provider-ready transaction from the work item
	 * @return an ArrayList containing HashMap(s) with filename and data
	 */
	public Object doReinsurerSpecificProcessing(NbaSource aSource) throws NbaBaseException {

		ArrayList aList = new ArrayList();
		HashMap aMap = new HashMap();
		aMap.put(FILENAME, getWork().getNbaLob().getPolicyNumber() + "_" + System.currentTimeMillis() + ".xml");
		aMap.put(DATA, aSource.getText());
		aList.add(aMap);
		return aList;
	}
	/**
	 * For Munich processing,  this will sets the URL (path) from the NbaConfiguration file.
	 */
	public void initializeTarget() throws NbaBaseException {
		setTarget(getConfigRien().getUrl());
	}
}
