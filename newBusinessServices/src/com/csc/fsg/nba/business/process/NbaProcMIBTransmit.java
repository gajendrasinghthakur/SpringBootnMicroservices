package com.csc.fsg.nba.business.process;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.csc.fsg.nba.business.transaction.NbaAxaServiceResponse;
import com.csc.fsg.nba.database.NbaConnectionManager;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.webservice.client.NbaAxaServiceRequestor;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapter;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapterFactory;

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

/**
 * Bulk MIB Transmit of failed 402 requests to MIB
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ALS5366</td><td>Version 7</td><td>Bulk MIB Transmission</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaProcMIBTransmit extends NbaAutomatedProcess implements NbaOliConstants {

	private final String stringToken = ";";
	
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException, NbaAWDLockedException {
		getLogger().logDebug("***MIB Bulk Transmit Starting***");
		setUser(user);
		ArrayList contractList = getContractsWithFailedMIB();
		String resultString = null;
		ArrayList pcDataList;
		String pcData;
		boolean successful;
		for (int i=0;i<contractList.size();i++) {
			getLogger().logDebug("*************************");
			resultString = (String) contractList.get(i);
			getLogger().logDebug("Begin process of keys: " + resultString);
			pcDataList = getPCData(resultString);
			if (pcDataList.size() > 0) {
				pcData = (String) pcDataList.get(0); //should only have 1
				try {
					successful = false;
					NbaTXLife tx402 = new NbaTXLife(pcData);
					if (sendMIBRequest(tx402)) {
						getLogger().logDebug("MIB Transmit Successful ");
						successful = true;
					} 
					updateAttachmentDescription(resultString,successful);
				} catch (NbaBaseException nbe) {
					if (nbe.isFatal()) {
						throw nbe;
					}
					updateAttachmentDescription(resultString,false);
					getLogger().logError("Error on MIB Transmit: " + nbe.getMessage());
					nbe.printStackTrace();
				} catch (Exception e) {
					getLogger().logError("Unable to create TX402 request for keys: " + pcDataList);
					e.printStackTrace();
				}
			}
			getLogger().logDebug("End process for keys: " + resultString);
			getLogger().logDebug("*************************");
		}
		getLogger().logDebug("***MIB Bulk Transmit Complete***");
		return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESS_NOWORK, "", "");
	}
	private void updateAttachmentDescription(String attachmentKeys,boolean successful) throws NbaDataAccessException {
		String systemKey = null;
		String contractKey = null;
		String companyKey = null;
		String backendKey = null;
		String id = null;
		StringTokenizer st = new StringTokenizer(attachmentKeys,stringToken);
		if (st.hasMoreTokens()) {
			systemKey = st.nextToken();
			contractKey = st.nextToken();
			companyKey = st.nextToken();
			backendKey = st.nextToken();
			id = st.nextToken();
		}
		getLogger().logDebug("Preparing to execute Update for Keys:" + attachmentKeys);
		
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = NbaConnectionManager.borrowConnection(NbaConfiguration.NBAPEND);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			stmt = conn.prepareStatement("UPDATE ATTACHMENT SET DESCRIPTION = ? WHERE PARENTIDKEY = ? AND CONTRACTKEY = ? AND COMPANYKEY = ? AND BACKENDKEY = ? AND ID = ?");
			if (successful) {
				stmt.setString(1,"TRANSMITTED");
			} else {
				stmt.setString(1,"FAILED");
			}
			stmt.setString(2,systemKey);
			stmt.setString(3,contractKey);
			stmt.setString(4,companyKey);
			stmt.setString(5,backendKey);
			stmt.setString(6,id);
			if (stmt.executeUpdate() > 0) {
				getLogger().logDebug("Successful Attachment update for Keys:" + attachmentKeys);
			}
		} catch (Throwable t) {
			throw new NbaDataAccessException("Error on Update of Keys:  " + attachmentKeys, t);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				NbaConnectionManager.returnConnection(conn);
			} catch (Throwable t) {
				getLogger().logException("Error Closing connection for Attachment Update on: " + attachmentKeys, t);
			}
		}
	}
	private boolean sendMIBRequest(NbaTXLife tx402) throws NbaBaseException {
		
		Policy policy = tx402.getPolicy();
		int size = 	policy.getRequirementInfoCount();
		RequirementInfo reqInfo = null;
		NbaParty nbaParty = null;
		for (int i=0; i<size; i++) {
			reqInfo = policy.getRequirementInfoAt(i);
			nbaParty = tx402.getParty(reqInfo.getAppliesToPartyID());
			if (null == nbaParty.getPerson().getOccupation() || nbaParty.getPerson().getOccupation().length() <= 0) {
			nbaParty.getPerson().setOccupation("UNKNOWN");
			}
		}
		NbaTXLife tx402Result = invokeWebservice(tx402.toXmlString());
		return evaluateResponse(tx402Result);
	
	}
	
	//evaluate the response..on success return true, otherwise return false
	private boolean evaluateResponse(NbaTXLife life) throws NbaBaseException {
		if (!life.isTransactionError()) {
            return true;
        } 
		
        handleWebServiceFailure(life);
        return false;
	}
	//if failure (5) result code with info code of 300 is received, then throw a fatal exception to stop poller
	protected void handleWebServiceFailure(NbaTXLife nbaTXLifeResponse) throws NbaBaseException {
	    TransResult transResult = nbaTXLifeResponse.getTransResult();
	    int resultInfoCount = transResult.getResultInfoCount();
	    for (int i = 0; i < resultInfoCount; i++) {
	        if (NbaOliConstants.TC_RESINFO_SYSTEMNOTAVAIL == transResult.getResultInfoAt(i).getResultInfoCode()) {
	            throw new NbaBaseException(NbaBaseException.WEBSERVICE_NOT_AVAILABLE, NbaExceptionType.FATAL);
	        } else {
	        	getLogger().logDebug("MIB Transmit Failure: " + transResult.getResultInfoAt(i).getResultInfoDesc());
	        }
	    } 
	}	
	private NbaTXLife invokeWebservice(String xml) throws NbaBaseException {
        Map params = new HashMap();
        try {
        	params.put(NbaAxaServiceRequestor.PARAM_SERVICEOPERATION, NbaAxaServiceRequestor.OPERATION_MIB_RETRIEVEMEDICALINFO_SERVICE);
        	params.put(NbaAxaServiceRequestor.PARAM_NBATXLIFE, xml);
            params.put(NbaAxaServiceRequestor.PARAM_TOKEN, getUser().getToken() );
            params.put(NbaAxaServiceRequestor.PARAM_UDDIKEY, "ToBeDetermined");
        } catch (Exception e) {
            throw new NbaBaseException(NbaBaseException.INVALID_REQUEST, e);
        }
        String defaultIntegration = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.DEFAULT_INTEGRATION);
        NbaWebServiceAdapter service = NbaWebServiceAdapterFactory.createWebServiceAdapter(defaultIntegration, 
        		NbaConfigurationConstants.WEBSERVICE_CATEGORY_PROVIDER_COMMUNICATION, 
        		NbaConfigurationConstants.WEBSERVICE_FUNCTION_PROVIDER_MIB);
   	    Map results = service.invokeAxaWebService(params);
   	    NbaTXLife txLifeResult = (NbaTXLife) results.get(NbaAxaServiceResponse.NBATXLIFE_ELEMENT);
   	    if (txLifeResult == null) {
   	    	String error = "Error (" + results.get(NbaAxaServiceResponse.ERRORCODE_ELEMENT) + ") " + results.get(NbaAxaServiceResponse.ERRORMSG_ELEMENT);
   	   	    if(getLogger().isDebugEnabled()) { 
   			    getLogger().logDebug("invokeWebservice : Response received from webservice: " + error);
   			} 
   	    	throw new NbaBaseException(error);
   	    }
   	     	    
   	    return txLifeResult;
	}
	private ArrayList getContractsWithFailedMIB() throws NbaBaseException {
		
		
		getLogger().logDebug("Preparing to execute SELECT Query for MIB Batch Process");
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList results = new ArrayList();
		try {
			conn = NbaConnectionManager.borrowConnection(NbaConfiguration.NBAPEND);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			stmt = conn.prepareStatement("select a.parentidkey as parentidkey, a.contractkey as contractkey, a.companykey as companykey, a.backendkey as backendkey, a.id as id" +
					" from attachment a, applicationinfoextension b where a.attachmenttype = '244' and " + "(" + "a.description != ? or a.description is null" + ")" +  
					"and (a.contractkey = b.contractkey and b.applicationsubtype = 1009800001 and (b.informalappapproval != -1 or b.underwritingstatus != -1))");
			stmt.setString(1,"TRANSMITTED");
			rs = stmt.executeQuery();
			StringBuffer sb = new StringBuffer();
			while (rs.next()) {
				sb.setLength(0); //resize to 0
				sb.append(rs.getString("parentidkey").trim());
				sb.append(stringToken);
				sb.append(rs.getString("contractkey").trim());
				sb.append(stringToken);
				sb.append(rs.getString("companykey").trim());
				sb.append(stringToken);
				sb.append(rs.getString("backendkey").trim());
				sb.append(stringToken);
				sb.append(rs.getString("id").trim());
				results.add(sb.toString());
			} 
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Returned " + results.size() + " rows");
			}
		} catch (Throwable t) {
			throw new NbaDataAccessException("MIB Batch Select Failed ", t);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				NbaConnectionManager.returnConnection(conn);
			} catch (Throwable t) {
				getLogger().logException("Failed to close connection on MIB Batch select", t);
			}
		}
		return results;
	}
	private ArrayList getPCData(String attacmentString) throws NbaBaseException{
		
		String systemKey = null;
		String contractKey = null;
		String companyKey = null;
		String backendKey = null;
		String id = null;
		StringTokenizer st = new StringTokenizer(attacmentString,stringToken);
		if (st.hasMoreTokens()) {
			systemKey = st.nextToken();
			contractKey = st.nextToken();
			companyKey = st.nextToken();
			backendKey = st.nextToken();
			id = st.nextToken();
		}
		getLogger().logDebug("Preparing to execute SELECT on AttachmentData: " + attacmentString);
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList results = new ArrayList();
		try {
			conn = NbaConnectionManager.borrowConnection(NbaConfiguration.NBAPEND);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			stmt = conn.prepareStatement("select PCDATA from ATTACHMENTDATA WHERE PARENTIDKEY = ? AND CONTRACTKEY = ? AND COMPANYKEY = ? AND BACKENDKEY = ?");
			stmt.setString(1,id);
			stmt.setString(2,contractKey);
			stmt.setString(3,companyKey);
			stmt.setString(4,backendKey);
			rs = stmt.executeQuery();
			while (rs.next()) {
				results.add(getStringForLongData(rs.getAsciiStream("PCDATA")));
			} 
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Select Successful");
			}
		} catch (Throwable t) {
			throw new NbaDataAccessException("Select failed for AttachmentData: " + attacmentString, t);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				NbaConnectionManager.returnConnection(conn);
			} catch (Throwable t) {
				getLogger().logException("Failed to close connection on select for AttachmentData: " + attacmentString, t);
			}
		}
		return results;
	}
	public String getStringForLongData(InputStream in) {
		try {
			int count;
			StringBuffer dataBuffer = new StringBuffer();
			while((count = in.read()) != -1) {
				dataBuffer.append((char)count);
			}
			return dataBuffer.toString();
		} catch (IOException ioe) {
			getLogger().logError(ioe.getMessage() + " during getStringForLongData");
			return null;
		}
	}
}
