package com.csc.fsg.nba.backendadapter.cyberlifeprint;
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
import com.csc.fs.sa.accel.interaction.services.AccelCyberLifeDXEDataTransformationIntf;
import com.csc.fsg.nba.backendadapter.cyberlife.NbaCyberRequests;
import com.csc.fsg.nba.backendadapter.cyberlifeInforce.NbaCyberInforceAppAdapter;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.KeyedValue;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;
/**
 * This class create the data streams(DXE) for cyberlife backend calculations.
 * <p>
 * <b>Modifications:</b><br>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA133</td><td>Version 6</td><td>nbA CyberLife Interface for Calculations and Contract Print</td></tr>
 * <tr><td>NBA195</td><td>Version 7</td><td>JCA Adapter for DXE Interface to CyberLife</td></tr>
 * </table>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
// NBA195 signature modified
public class NbaCyberPrintRequests extends NbaCyberRequests implements AccelCyberLifeDXEDataTransformationIntf {
    public static final String POLICY_PAGE_AGENT_CARD = "AGNTCD=1;";
    public static final String POLICY_PAGE_POLICY_SUMMARY = "POLSUM=1;";
    public static final String POLICY_PAGE_SCHEDULE_OR_VALUE = "SCHDPG=1;";
    private static NbaLogger logger = null; // NBA195
    
    /**
	 * NbaCyberInforceRequests constructor.
	 */
	public NbaCyberPrintRequests() {
		super();
	}
	
	// NBA195 changed signature to protected
	protected String createPrintRequest(NbaTXLife nbaTXLife) throws NbaBaseException {
        StringBuffer requestDXE = new StringBuffer();
        requestDXE.append(getISPNRequestDXE(nbaTXLife)); //ISPN transaction
        requestDXE.append(getPolicyPagesRequest(nbaTXLife));
        return requestDXE.toString();
    }

    protected String getISPNRequestDXE(NbaTXLife nbaTXLife) throws NbaBaseException {
        return new NbaCyberInforceAppAdapter().createIS00Request(nbaTXLife.getTXLife()).replaceFirst("IS00", "ISPN");
    }
    
    protected String getPolicyPagesRequest(NbaTXLife nbaTXLife) {
        Policy policy = nbaTXLife.getPolicy();
        StringBuffer buffer = new StringBuffer();
        int keyedValueCount = policy.getKeyedValueCount();
        if (keyedValueCount > 0) {
            KeyedValue keyedValue = null;
            for (int i = 0; i < keyedValueCount; i++) {
                keyedValue = policy.getKeyedValueAt(i);
                if (keyedValue.getKeyValueCount() > 0) {
                    long value = NbaUtils.convertStringToLong(keyedValue.getKeyValueAt(0).getPCDATA());
                    if (value == NbaOliConstants.OLI_ATTACH_AGTCARD) {
                        buffer.append(POLICY_PAGE_AGENT_CARD);
                    } else if (value == NbaOliConstants.OLI_ATTACH_POLSUM) {
                        buffer.append(POLICY_PAGE_POLICY_SUMMARY);
                    } else if (value == NbaOliConstants.OLI_ATTACH_POLSCHED || value == NbaOliConstants.OLI_ATTACH_POLVAL) {
                        buffer.append(POLICY_PAGE_SCHEDULE_OR_VALUE);
                    }
                }
            }
        } else {
            buffer.append(POLICY_PAGE_AGENT_CARD);
            buffer.append(POLICY_PAGE_POLICY_SUMMARY);
            buffer.append(POLICY_PAGE_SCHEDULE_OR_VALUE);
        }
        return buffer.toString();
    }

    /**
	 * Parse the calculation results from host response. It creates XMLIfe resposne from incoming request and update reponse with backend print
	 * results.
	 * 
	 * @param hostResponse the DXE response from host
	 * @param request the NbaTXLife request for print
	 * @return the NbaTXLife response from print
	 * @see com.csc.fsg.nba.backendadapter.cyberlifeprint.NbaCyberPrintParser#createXmlResponse(NbaTXLife txLifeRequest)
	 */
	// NBA195 new method
	public Object parseHostResponse(Object request, String hostResponse) {
		if (hostResponse == null || hostResponse.length() < 2
				|| hostResponse.substring(0, 2).compareTo(String.valueOf(NbaOliConstants.OLI_TC_NULL)) == 0) {
			throw new RuntimeException(new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_HOST_UNAVAILABLE + "  " + hostResponse,
					NbaExceptionType.FATAL));
		}
		try {
			//create the XML response
			NbaCyberPrintParser cyberParser = new NbaCyberPrintParser();
			cyberParser.setHostResponse(hostResponse);
			return cyberParser.createXmlResponse((NbaTXLife) request);
		} catch (NbaBaseException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * It creates DXE string to be sent to host from NbaTXLife.
	 * 
	 * @param request the NbaTXLife request for print
	 * @return the NbaTXLife response updated with calculation results.
	 */
	// NBA195 new method
	public String prepareRequestToHost(Object request) {
		String clRequest = null;
		UserAuthRequestAndTXLifeRequest userRequest = ((NbaTXLife) request).getTXLife().getUserAuthRequestAndTXLifeRequest();
		if (userRequest != null && userRequest.getTXLifeRequestCount() > 0) {
			TXLifeRequest txRequest = userRequest.getTXLifeRequestAt(0);
			long transType = txRequest.getTransType();
			long transSubType = txRequest.getTransSubType();
			if (transType == NbaOliConstants.TC_TYPE_NEWBUSSUBMISSION && transSubType == NbaOliConstants.TC_SUBTYPE_BACKEND_PRINT) {
				// TODO determine why a new instance of this same class is needed
				NbaCyberPrintRequests cyberRequest = new NbaCyberPrintRequests();
				try {
					clRequest = cyberRequest.createPrintRequest((NbaTXLife) request);
				} catch (NbaBaseException e) {
					throw new RuntimeException(e);
				}
			} else {
				throw new RuntimeException(new NbaBaseException("Invalid Print Type Requested"));
			}
			getLogger().logDebug(clRequest);
		}
		return clRequest;
	}

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * 
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	// NBA195 new method
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaCyberPrintRequests.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaCyberPrintRequests could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
}
