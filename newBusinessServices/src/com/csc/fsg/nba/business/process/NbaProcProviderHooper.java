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
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.provideradapter.AxaEibProviderAdapter;
import com.csc.fsg.nba.provideradapter.NbaProviderAdapter;
import com.csc.fsg.nba.vo.NbaTXLife;


/**
 * <code>NbaProcProviderHooper</code> handles communications between nbAccelerator
 * and Hooper Holmes.  It extends the NbaProcProviderCommunications class, which drives the process,
 * and supplies Hooper Holmes specific functionality.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA081</td><td>Version 3</td><td>Requirements Ordering and Receipting</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>AXAL3.7.31</td><td>AXA Life Phase 2</td><td>Provider Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */

public class NbaProcProviderHooper extends NbaProcProviderCommunications {

/**
 * NbaProcProviderHooper default constructor.
 * 
 */
public NbaProcProviderHooper() {
	super();
}

//ACN014 - Removed doProviderSpecificProcessing method

/**
 * Answers the result of evaluating the response from Hooper Holmes web service.
 * @param response the response from the sendMessageToProvider method
 * @return boolean <code>true</code> if the result is not null or empty and does not contain "ERROR";
 *              otherwise, <code>false</code> is returned
 */
public boolean evaluateResponse(String response) throws NbaBaseException {
	//AXAL3.7.31 - rewrote method to handle transaction errors
    boolean success = false;
    if (response != null && response.trim().length() > 0) {
        NbaTXLife life;
        try {
            life = new NbaTXLife(response);
        } catch (Exception e) {
            throw new NbaBaseException(NbaBaseException.INVALID_RESPONSE, e);
        }
        if (isTransactionError(life)) {
            handleProviderWebServiceFailure(life);
        } else {
            success = true;
        }
    }
    return success;
}
/**
 * For Hooper processing, this will find the URL or path for the requirement based
 * on the requirement type.
 */
public void initializeTarget() throws NbaBaseException {
    setTarget(getProvider().getUrl());
}	

//AXAL3.7.31 New Method
public Object doProviderSpecificProcessing(Object data)
		throws NbaBaseException {
	List alist = new ArrayList();
	alist.add(removeNameSpace((String) data));
	alist.add(getProvider().getName());
	alist.add(NbaUtils.XSL_REQUIREMENT_REQUEST);
	AxaEibProviderAdapter eibAdapter = new AxaEibProviderAdapter();
	Map map = eibAdapter.convertXmlToProviderFormat(alist);
	String outputXml = (String) map.get(NbaProviderAdapter.TRANSACTION);
	// Moved namespace definitions to the XSL transform
	return outputXml;
}

}
