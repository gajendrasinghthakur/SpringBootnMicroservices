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
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.provideradapter.AxaEibProviderAdapter;
import com.csc.fsg.nba.provideradapter.NbaProviderAdapter;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;

/**
 * <code>NbaProcProviderSourceAccess</code> handles communications between
 * nbAccelerator and SourceAccess. It extends the NbaProcProviderCommunications class,
 * which drives the process, and supplies SourceAccess specific functionality.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.31</td><td>AXA Life Phase 2</td><td>Provider Interface</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */
public class NbaProcProviderSourceAccess extends NbaProcProviderCommunications {
	/**
	 * NbaProcProviderEmsi constructor comment.
	 */
	private final String NAMESPACE = " xmlns=\"http://ACORD.org/Standards/Life/2\">";

	public NbaProcProviderSourceAccess() {
		super();
	}

	/**
	 * Answers the result of evaluating the response from the FTP process.
	 * 
	 * @param response
	 *            the response from the sendMessageToProvider method
	 * @return <code>true</code> if the result is not null or empty;
	 *         otherwise, <code>false</code> is returned
	 * @throws NbaBaseException
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
	 * For LabOne processing, this will sets the URL (path) from the
	 * NbaConfiguration file.
	 * 
	 * @throws NbaBaseException
	 */
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
		return addNameSpace(outPutXml, NAMESPACE);
	}
	
	//CR60669-APS Order Authorization method override 
	protected Object addAuthorizations(Object data) throws NbaBaseException {
		try {
			NbaTXLife reqst121TxLife = new NbaTXLife((String) data);
			setNbaOLifEId(new NbaOLifEId(reqst121TxLife));
			if (NbaOliConstants.OLI_REQCODE_PHYSSTMT == getWorkLobs().getReqType()) {
				RequirementInfo reqInfo = reqst121TxLife.getRequirementInfo(getWorkLobs().getReqUniqueID());
				List sources = retrieveWorkItem(getWork()).getNbaSources();
				for (int i = 0; i < sources.size(); i++) {
					NbaSource nbASource = (NbaSource) sources.get(i);
					if (nbASource.isImageFormat()) {
						reqInfo.getAttachment().addAll(createAttachmentsForImages(nbASource, LONG_NULL_VALUE));
					}
				}
				getNbaOLifEId().assureId(reqst121TxLife);
			}
			return reqst121TxLife.toXmlString();
		} catch (Exception whoops) {
			throw new NbaBaseException("Unable to add Authorizations for SourceAccess(SA)/APS", whoops, NbaExceptionType.FATAL);
		}
	}

}
