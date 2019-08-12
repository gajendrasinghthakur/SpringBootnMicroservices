package com.csc.fsg.nba.bean.accessors;
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
import org.w3c.dom.Element;

/**
 * This is a stateless Session Bean that is used for submitting a new 
 * application request for a policy in NBA. It uses XPathAPI to check the
 * existence of a policy number in the incoming XML. If a policy number 
 * is not attached with the XML with the request, then it retrieves a 
 * policy number nbA system and assigns it to the XML. Then 
 * it creates a file in the AWD_RIP folder and creates the response using the Xerces API.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.31</td><td>AXA Life</td><td>AXAL3.7.31 Provider Interfaces</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public interface NbaProviderIndexWebService {
	abstract public Element submitRequirementResult(Element ele);
}
