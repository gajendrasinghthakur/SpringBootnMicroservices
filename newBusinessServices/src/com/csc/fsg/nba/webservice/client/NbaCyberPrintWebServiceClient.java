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
import com.csc.fsg.nba.backendadapter.cyberlifeprint.NbaCyberPrintAdapter;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaWebServerFaultException;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaTXLife;

/**
 * NbaCyberPrintWebServiceClient is the client class to call nbA hosted CyberLife adapter for contract printing.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA133</td><td>Version 6</td><td>nbA CyberLife Interface for Calculations and Contract Print</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
public class NbaCyberPrintWebServiceClient extends NbaWebServiceAdapterBase {
    private NbaLogger logger;
	/**
	 * Constructor for NbaCyberPrintWebServiceClient.
	 */
	public NbaCyberPrintWebServiceClient() {
        super();
    }
	/**
	 * Calls cyberlife calcuation adapter to perform requested calculation.
	 * @param nbATxLife the calculation request
	 * @return the updated NbaTXLife object with calc values
	 */
	public NbaTXLife invokeWebService(NbaTXLife nbATxLife) throws NbaBaseException {
	    NbaTXLife nbaTxlifeResponse = null;
	    try {
            boolean isDebugEnable = getLogger().isDebugEnabled();
            if (isDebugEnable) {
                getLogger().logDebug(nbATxLife.toXmlString());
            }

            NbaCyberPrintAdapter adapter = new NbaCyberPrintAdapter();
            nbaTxlifeResponse = adapter.print(nbATxLife);

            if (isDebugEnable) {
                getLogger().logDebug(nbaTxlifeResponse.toXmlString());
            }
        } catch (NbaBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new NbaWebServerFaultException("Unexpected Error Backend Adapter Exception", e);
        }
        return nbaTxlifeResponse;
    }
	/**
	 * Returns my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	private NbaLogger getLogger() {
        if (logger == null) {
            logger = NbaLogFactory.getLogger(this.getClass());
        }
        return logger;
    }
}
