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
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.tbf.xml.XmlValidationError;
/**
 * This class is used for doing an Inquiry for a policy from the backend.
 * The retrievePolicy method is exposed as webservice operation. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.35</td><td>AXA Life Phase 1</td><td>nbA Web Services</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class Nba302WebService {
	protected static NbaLogger logger = null;
	/**
     * Recieves a 302 request as an XML element, checks for DTD validations, performs a formal informal policy search for the search criterai supplied
     * in the request xml stream and returns it.
     * 
     * @param ele Element with the search criteria
     * @return Element containing the policy data
     */
    public Element retrievePolicy(Element ele) {
        Element txLife = null;
        try {

            String txLifeStr = DOM2String(ele.getOwnerDocument());
            StringBuffer txLifeStringBuffer = new StringBuffer(txLifeStr);
            String txLifeString = txLifeStringBuffer.toString();
            //begin PERF-APSL479
            if (getLogger().isDebugEnabled()) {
            	getLogger().logDebug("Incoming 302 before parse");
            	getLogger().logDebug(txLifeString);
            }
            //end PERF-APSL479
            txLifeString = txLifeString.substring(txLifeString.indexOf("<TXLife"),txLifeString.indexOf("</TXLife>")+9);
            txLifeStringBuffer.delete(txLifeStr.indexOf("<SOAP-ENV"), txLifeStr.indexOf("<TXLife") - 1); 
            //txLifeString.delete(txLifeString.toString().indexOf("</TXLife>") + 9, txLifeString.length());
            NbaLogFactory.getLogger(this.getClass()).logDebug("Incoming 302 after string parse");
            NbaLogFactory.getLogger(this.getClass()).logDebug(txLifeString);
            //DTD validations using Breeze
            NbaTXLife aTXLife = new NbaTXLife(txLifeString.toString());
            //begin PERF-APSL479
            if (getLogger().isDebugEnabled()) {
            	getLogger().logDebug("Incoming 302 after parse");
            	getLogger().logDebug(aTXLife.toXmlString());
            }
            //end PERF-APSL479
            NbaTXLife nbaTXLife = null;
            long transSubType = aTXLife.getTransSubType();
            //Check for the transSubType to identify formal/Informal or Agent search.
            try {
                if (transSubType == 1009830201) {
                    nbaTXLife = new NbaRetrievePolicy().retrieveAgentFormalInformalPolicy(aTXLife);
                } else if (transSubType == 1009830203) {
                    nbaTXLife = new NbaRetrievePolicy().retrieveFormalInformalPolicy(aTXLife);
                }
                //begin PERF-APSL479
                if (getLogger().isDebugEnabled()) {
                	getLogger().logDebug("Outgoing 302 response");
                	getLogger().logDebug(nbaTXLife.toXmlString());
                }
                //end PERF-APSL479
            } catch (Throwable e) {
                NbaLogFactory.getLogger(this.getClass()).logException(e);
                return createExceptionResponse(e,aTXLife);
            }

            //
            String TXLife = nbaTXLife.toXmlString();
            Document doc1 = (DocumentBuilderFactoryImpl.newInstance()).newDocumentBuilder().parse(new ByteArrayInputStream(TXLife.getBytes()));
            txLife = doc1.getDocumentElement();

        } catch (NbaBaseException e) { 
            e.forceFatalExceptionType();
            NbaLogFactory.getLogger(this.getClass()).logException(e);
            return createExceptionResponse(e);
        } catch (Throwable t) { 
            NbaLogFactory.getLogger(this.getClass()).logException(t);
            return createExceptionResponse(t);
           		
        }
        return txLife; 
    }
	
	/**
	 * Receives a document and converts it into a String
	 * @param doc Document which contains the element to be converted into String
	 * @return String 
	 */
	protected static String DOM2String(Document doc) throws java.io.IOException {
		StringWriter sw = new StringWriter();
		OutputFormat oFormatter = new OutputFormat("XML", null, false);
		XMLSerializer oSerializer = new XMLSerializer(sw, oFormatter);
		oSerializer.serialize(doc);
		return sw.toString();
	}
	/**
	 * Creates a failure response for 302 transaction in case of DTD error.
	 * @param ele Element with the New Application submission data
	 * @param vctrErrors Vector containing the validation errors.
	 * @param aTXLife NbaTXLife value object containing the data recieved in the request
	 * @return Element containing the Error message	 
	 */
	protected Element createDTDErrorResponse(String guid, String typeTrans, String modeTrans, Throwable exp) {
		Document xmlDoc = null;
		Element txLife = null;
		try {
			//Create a XML Document
			DocumentBuilderFactory dbFactory = DocumentBuilderFactoryImpl.newInstance();
			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
			xmlDoc = docBuilder.newDocument();
			txLife = xmlDoc.createElement("TXLife");
			Element userAuthResponse = xmlDoc.createElement("UserAuthResponse");
			Element txLifeResponse = xmlDoc.createElement("TXLifeResponse");
			txLife.appendChild(userAuthResponse);
			txLife.appendChild(txLifeResponse);
			//Add code for UserAuthResponse contents
			Element transResult = xmlDoc.createElement("TransResult");
			Element svrDate = xmlDoc.createElement("svrDate");
			Element svrTime = xmlDoc.createElement("svrTime");
			userAuthResponse.appendChild(transResult);
			userAuthResponse.appendChild(svrDate);
			userAuthResponse.appendChild(svrTime);
			//Set the values in server date and server time
			// Build SvrDate
			Date today = new Date();
			svrDate.appendChild(xmlDoc.createTextNode(formatDate(today)));
			// Build SvrTime
			svrTime.appendChild(xmlDoc.createTextNode(formatTime(today)));
			// create and set the value for result code		
			Element resultCode = xmlDoc.createElement("ResultCode");
			transResult.appendChild(resultCode);
			resultCode.setAttribute("tc", "5");
			resultCode.appendChild(xmlDoc.createTextNode("Failure"));
			//Add code for TXLifeResponse contents
			//Create first level element for TXLifeResponse
			Element transRefGUID = xmlDoc.createElement("TransRefGUID");
			Element transType = xmlDoc.createElement("TransType");
			Element transExeDate = xmlDoc.createElement("TransExeDate");
			Element transExeTime = xmlDoc.createElement("TransExeTime");
			Element transMode = xmlDoc.createElement("TransMode");
			Element transResult1 = xmlDoc.createElement("TransResult");
			Element oLifE = xmlDoc.createElement("OLifE");
			//Add first level element for TXLifeResponse
			txLifeResponse.appendChild(transRefGUID);
			txLifeResponse.appendChild(transType);
			txLifeResponse.appendChild(transExeDate);
			txLifeResponse.appendChild(transExeTime);
			txLifeResponse.appendChild(transMode);
			txLifeResponse.appendChild(transResult1);
			txLifeResponse.appendChild(oLifE);
			//set the values for trans ref guid, trans type, trans exe date, trans exe time and trans mode 
			transRefGUID.appendChild(xmlDoc.createTextNode(guid));
			transType.setAttribute("tc", typeTrans);
			transExeDate.appendChild(xmlDoc.createTextNode(formatDate(today)));
			//extract date from
			transExeTime.appendChild(xmlDoc.createTextNode(formatTime(today)));
			transMode.setAttribute("tc", modeTrans);
			//Create and add first level elements for TransResult in TXLifeResponse
			Element resultCode1 = xmlDoc.createElement("ResultCode");
			transResult1.appendChild(resultCode1);
			resultCode1.setAttribute("tc", "5");
			resultCode1.appendChild(xmlDoc.createTextNode("Failure"));
			//in case of exception, these elements are generated
//			in case of exception, these elements are generated
            Element resultInfo;
            Element resultInfoCode;
            Element resultInfoDesc;
            resultInfo = xmlDoc.createElement("ResultInfo");
            transResult1.appendChild(resultInfo);
            resultInfoCode = xmlDoc.createElement("ResultInfoCode");
            resultInfoDesc = xmlDoc.createElement("ResultInfoDesc");
            resultInfo.appendChild(resultInfoCode);
            resultInfo.appendChild(resultInfoDesc);
            resultInfoCode.setAttribute("tc", "200");
            resultInfoCode.appendChild(xmlDoc.createTextNode("General Data Error"));
            StringBuffer sb = new StringBuffer();
            sb.append("<![CDATA[\n");
            formatStackTrace(exp, sb);
            sb.append("]]>");
            resultInfoDesc.appendChild(xmlDoc.createTextNode(sb.toString()));
		} catch (Throwable t) { //NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(t);
		}
		return txLife;
	}
 
	/**
	 * Formats a Date's date for use in the response node.
	 * @param  rawDate  the date/time in Java internal format.
	 * @return String   the date as a properly formatted String.
	 */
	protected String formatDate(Date rawDate) {
		SimpleDateFormat dateFormat;
		dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String formattedDate = dateFormat.format(rawDate);
		return formattedDate;
	}
	/**
	 * Formats a Date's time for use in the response node.
	 * @param rawTime   the date/time in Java internal format.
	 * @return String   the time as a properly formatted String.
	 */
	protected String formatTime(Date rawTime) {
		SimpleDateFormat dateFormat;
		dateFormat = new SimpleDateFormat("HH:mm:ss");
		long zoneOffset = (new GregorianCalendar()).get(Calendar.ZONE_OFFSET);
		DecimalFormat hoursFormat = new DecimalFormat("+00;-00");
		DecimalFormat minutesFormat = new DecimalFormat("00");
		String formattedTime =
			dateFormat.format(rawTime)
				+ hoursFormat.format(zoneOffset / 3600000)
				+ ":"
				+ minutesFormat.format((zoneOffset / 60000) % 60);
		return formattedTime;
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(Nba302WebService.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("Nba203WebServiceBean could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * This method retrieves the backend id for a policy using Company code and plan 
	 * @param companyCode company code
	 * @param plan plan
	 * @return String backend id
	 * @exception Exception
	 */
	protected String getBackendId(String companyCode, String plan) throws Exception {
		NbaTableAccessor tableAccessor = new NbaTableAccessor();
		Map tblKeys = tableAccessor.createDefaultHashMap("*"); //Wild card the backend id to retrieve plan data
		if (companyCode != null) {
			tblKeys.put(NbaTableAccessConstants.C_COMPANY_CODE, companyCode);
		}
		if (plan != null) {
			tblKeys.put(NbaTableAccessConstants.C_COVERAGE_KEY, plan);
		}
		NbaPlansData planData = tableAccessor.getPlanData(tblKeys);
		if (planData == null) {
			throw new NbaBaseException("Plan is invalid.");
		} else {
			return planData.getSystemId();
		}
	}
    /**
     * Creates a failure response for 302 transaction when an Exception occurs.
     * @param ele Element with the New Application submission data
     * @param vctrErrors Vector containing the validation errors.
     * @param aTXLife NbaTXLife value object containing the data recieved in the request
     * @return Element containing the Error message	 
     */
	
    protected Element createExceptionResponse(Throwable exp,NbaTXLife aTXLife) {
    	String guid = aTXLife.getTransRefGuid();
    	String typeTrans = new Long(aTXLife.getTransType()).toString();
    	String modeTrans = new Long(aTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getTransMode()).toString();
    	try {
    		return createDTDErrorResponse(guid, typeTrans, modeTrans, exp);
            //Create a XML Document
            
        } catch (Throwable t) { //NBA103
            NbaLogFactory.getLogger(this.getClass()).logException(t);
        }
        return null;
    }
    protected Element createExceptionResponse(Throwable exp) {
    	try {
    		return createDTDErrorResponse(NbaUtils.getGUID(), "302", "2", exp);
            //Create a XML Document
            
        } catch (Throwable t) { //NBA103
            NbaLogFactory.getLogger(this.getClass()).logException(t);
        }
        return null;
    }
    
    /**
     * Format a Stack Trace for an Exception 
     * @param exp
     * @param sb
     */
    protected void formatStackTrace(Throwable exp, StringBuffer sb) {
        sb.append(exp.toString());
        sb.append("\n");
        StackTraceElement trace[] = exp.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            StackTraceElement frame = trace[i];
            sb.append(frame.getClassName());
            sb.append(" ");
            sb.append(frame.getMethodName());
            sb.append(" ");
            if (frame.getLineNumber() >= 0) {
                sb.append(frame.getLineNumber());
            }
            sb.append("\n");
        }
        if (exp.getCause() != null) {
            sb.append("Previous throwable\n");
            formatStackTrace(exp.getCause(), sb);
        }
    }
 	
}
