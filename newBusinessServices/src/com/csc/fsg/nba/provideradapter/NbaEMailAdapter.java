package com.csc.fsg.nba.provideradapter;

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
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
/**
 * Insert description here.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td><tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>121/1122 Migration</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see NbaProviderAdapterFacade
 * @since New Business Accelerator - Version 2
 */
// ACN014 CHANGED TO EXTEND NbaProviderAdapter class
public class NbaEMailAdapter extends NbaProviderAdapter  {
	/**
 * NbaCrlAdapter constructor comment.
 */
public NbaEMailAdapter() {
	super();
}
/**
 * This method converts the Provider's response into XML transaction.It 
 * also updates required LOBs and result source with converted XMLife.
 * @param work the requirement work item.
 * @return an ArrayList containing requirement work items with formated source.
 * @exception NbaBaseException thrown if an error occurs.
 */
// ACN014 Changed signature
public ArrayList processResponseFromProvider(NbaDst work, NbaUserVO user)throws NbaBaseException{
	//ACN014 BEGIN
	ArrayList aList = new ArrayList();
	aList.add(work);
	return aList;
	//ACN014 END
}

/**
 * This method converts the XML Requirement transactions into a format
 * that is understandable by the provider.
 * @param aList array list of requirement transactions
 * @return a provider ready message.
 * @exception NbaBaseException thrown if an error occurs.
 */
public Map convertXmlToProviderFormat(List aList)throws NbaBaseException{
	StringBuffer buf = new StringBuffer();
	buf.append("Provider Ready Transaction For : EMail \n");
	if (aList.size() > 0) {
		NbaTXLife txlife = (NbaTXLife) aList.get(0);

		buf.append("Contract No. : ");
		buf.append(txlife.getPrimaryHolding().getPolicy().getPolNumber());
		buf.append("\n \n");
		for (int i = 0; i < aList.size(); i++) {
			txlife = (NbaTXLife) aList.get(i);
			buf.append("Requirement Info \n");
			buf.append("Requirement Code : ");
			buf.append(txlife.getPrimaryHolding().getPolicy().getRequirementInfoAt(0).getReqCode());
			buf.append("\n");
			buf.append("Requirement Detail : ");
			buf.append(txlife.getPrimaryHolding().getPolicy().getRequirementInfoAt(0).getRequirementDetails());
			buf.append("\n \n");
		}
	} else {
		throw new NbaBaseException("XMLife is required for transform");
	}
	Map aMap = new HashMap();
	aMap.put(TRANSACTION, buf.toString());
	return aMap;
}

}
