package com.csc.fsg.nba.webservice.client;
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
 * 
 * *******************************************************************************<BR>
 */
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.vo.NbaTXLife;
/**
 * Wrapper HoldingInquiry WebService.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA117</td><td>Version 7</td><td>Pending VANTAGE-ONE Calculations</td></tr>
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 5
 */
public class NbaHoldingInquiryClient extends NbaWebServiceAdapterBase {
	public NbaTXLife invokeWebService(NbaTXLife nbATxLife) { // SPR2968
		try {
			HoldingInquiryProxy proxyClient = new HoldingInquiryProxy();
			proxyClient.setAccess(getAccess());
			proxyClient.setTargetUri(getTargetUri());
			proxyClient.setWsdlUrl(getWsdlUrl());
			return proxyClient.invokeWebService(nbATxLife); // SPR2968
		} catch (Throwable t) {
			NbaLogFactory.getLogger(this.getClass()).logException(t); 
		}
		return null;
	}
}
