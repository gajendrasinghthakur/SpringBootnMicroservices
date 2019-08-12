package com.csc.fsg.nba.reinsuranceadapter;

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
import java.util.HashMap;
import java.util.Map;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;
/**
 * NbaReinsuranceAdapter is the interface for connecting to a reinsurer for the purpose
 * of ordering bids and receiving offer from reinsurer vendors.
 * <p>This interface defines the methods the different subclasses must implement 
 * in order to create a commonality of function between the classes that create,
 * send, receive and process messages to/from reinsurer.
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
public abstract class NbaReinsuranceAdapter {
	public static String TRANSACTION = "TRANSACTION";

	/**
	 * This method converts the XML 552 transactions into a format that is understandable by the reinsurer.
	 * @param txLife the 552 XML transaction
	 * @param user the user value object
	 * @return a reinsurer ready message in a HashMap which includes any errors that might have occurred.
	 * @exception NbaBaseException thrown if an error occurs.
	 */
	public java.util.Map convertXmlToReinsurerFormat(NbaTXLife txLife, NbaUserVO user, NbaDst work) throws NbaBaseException {
		Map map = new HashMap();
		map.put(TRANSACTION, txLife.toXmlString());
		return map;
	}

	/**
	 * This method converts the reinsurer response/offer into XML transaction.It also updates required LOBs and result source with converted XMLife.
	 * @param work the reinsurance work item.
	 * @param user the user value object
	 * @return the requirement work item with formated source.
	 * @exception NbaBaseException thrown if an error occurs.
	 */
	public NbaDst processResponseFromReinsurer(NbaDst work, NbaUserVO user) throws NbaBaseException {
		return work;
	}

	/**
	 * This method provides the means by which a message representing a request for a requirement is submitted to the provider.
	 * <p>
	 * The means of communication varies and may include many different methods: HTTP Post, writing the message to a folder on a server, or others.
	 * The <code>NbaConfigReinsurer</code> that contains information on how to communicate with the reinsurer is passed to the
	 * <code>NbaProviderCommunicator</code> object. The <code>NbaProviderCommunicator</code> then sends the message to the provider.
	 * @param aTarget the destinatin of the message
	 * @param message the message to be sent to the provider
	 * @return an Object that must be evaluated by the calling process
	 */
	//AXAL3.7.32 New Method
	public Object sendMessageToProvider(String aTarget, Object aMessage, NbaUserVO userVO) throws NbaBaseException {
		return aMessage;
	}

}
