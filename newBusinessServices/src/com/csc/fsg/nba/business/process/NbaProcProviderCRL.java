package com.csc.fsg.nba.business.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.provideradapter.AxaEibProviderAdapter;
import com.csc.fsg.nba.provideradapter.NbaProviderAdapter;
import com.csc.fsg.nba.vo.NbaTXLife;


/**
 * <code>NbaProcProviderCRL </code> handles communications between
 * nbAccelerator and CRL. It extends the NbaProcProviderCommunications class,
 * which drives the process, and supplies CRL specific functionality.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>ACORD 121/1122 General Requirement Request Migration</td></tr>
 * <tr><td>AXAL3.7.31</td><td>AXA Life Phase 2</td><td>Provider Interface</td></tr>
 * <tr><td>APSL4278</td><td>Rx Score</td><td>Provider Interface</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */

public class NbaProcProviderCRL extends NbaProcProviderCommunications {

	public NbaProcProviderCRL() {
		super();
	}

	public boolean evaluateResponse(String response) throws NbaBaseException {
		// AXAL3.7.31 - rewrote method to handle transaction errors
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

	public void initializeTarget() throws NbaBaseException {
		setTarget(getProvider().getUrl());
	}
	public Object doProviderSpecificProcessing(Object data) throws NbaBaseException {
		List alist = new ArrayList();
		alist.add(removeNameSpace((String) data));
		alist.add(getProvider().getName());
		alist.add(NbaUtils.XSL_REQUIREMENT_REQUEST);
		AxaEibProviderAdapter eibAdapter = new AxaEibProviderAdapter();
		Map map = eibAdapter.convertXmlToProviderFormat(alist);
		String outPutXml = (String) map.get(NbaProviderAdapter.TRANSACTION);
		// Moved namespace definitions to the XSL transform
		return outPutXml;
	}
}
