package com.csc.fsg.nba.process.tx302;

/*
 * *******************************************************************************<BR>
 * Copyright 2014, Computer Sciences Corporation. All Rights Reserved.<BR>
 *
 * CSC, the CSC logo, nbAccelerator, and csc.com are trademarks or registered
 * trademarks of Computer Sciences Corporation, registered in the United States
 * and other jurisdictions worldwide. Other product and service names might be
 * trademarks of CSC or other companies.<BR>
 *
 * Warning: This computer program is protected by copyright law and international
 * treaties. Unauthorized reproduction or distribution of this program, or any
 * portion of it, may result in severe civil and criminal penalties, and will be
 * prosecuted to the maximum extent possible under the law.<BR>
 * *******************************************************************************<BR>
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.bean.accessors.NbaRetrievePolicy;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponse;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;

/**
 * TX302WebServiceBP is used for retrieving Policy Details.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead> <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th> </thead>
 * <tr>
 * <td>APSL4508 Websphere 8.5.5 Upgrade</td>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 */

public class TX302WebServiceBP extends NewBusinessAccelBP {
     
	protected static NbaLogger logger = null;
    /**
     * This method supports do contract inquiry and call assembler & return result.
     * 
     * @param input requires a TXLife request object
     * @return the TXLife response is returned in the Resu-=========lt.
     */
	public Result process(Object input) {
		Result result = new AccelResult();
		String namespaceStr = null;
		String guid = null;
		NbaTXLife failureResponse = null;
		StringBuffer TXLifeStr = null;
		try {
			NbaTXLife aTXLife = (NbaTXLife) input;
			guid = aTXLife.getTransRefGuid();
			failureResponse = createResponse(guid, NbaOliConstants.TC_RESCODE_FAILURE);
			TXLifeStr = new StringBuffer(aTXLife.toXmlString());
			// Remove the EIB headers and trailers
			TXLifeStr.delete(0, TXLifeStr.indexOf("<TXLife"));
			TXLifeStr.delete(TXLifeStr.toString().indexOf("</TXLife>") + 9, TXLifeStr.length());
			// Remove the namespace definition from the result xml
			String start = "<TXLife";
			String end = ">";
			String xmlns = " xmlns=\"http://ACORD.org/Standards/Life/2\"";
			namespaceStr = substringBetween(TXLifeStr.toString(), start, end);
			int length = namespaceStr.length();
			TXLifeStr = new StringBuffer(start + xmlns + TXLifeStr.substring(start.length() + length).trim());
			
			NbaTXLife aTXLifeNew = new NbaTXLife(TXLifeStr.toString());
            //begin PERF-APSL479
            if (getLogger().isDebugEnabled()) {
            	getLogger().logDebug("Incoming 302 after parse");
            	getLogger().logDebug(aTXLifeNew.toXmlString());
            }
            //end PERF-APSL479
            NbaTXLife nbaTXLife = null;
            long transSubType = aTXLifeNew.getTransSubType();
            //Check for the transSubType to identify formal/Informal or Agent search.
            try {
                if (transSubType == 1009830201) {
                    nbaTXLife = new NbaRetrievePolicy().retrieveAgentFormalInformalPolicy(aTXLifeNew);
                } else if (transSubType == 1009830203) {
                    nbaTXLife = new NbaRetrievePolicy().retrieveFormalInformalPolicy(aTXLifeNew);
                }
                else {
                	nbaTXLife = createResponse(guid, NbaOliConstants.TC_RESCODE_SUCCESS);
                }
                //begin PERF-APSL479
                if (getLogger().isDebugEnabled()) {
                	getLogger().logDebug("Outgoing 302 response");
                	getLogger().logDebug(nbaTXLife.toXmlString());
                }
                //end PERF-APSL479
            } catch (Throwable e) {
                NbaLogFactory.getLogger(this.getClass()).logException(e);
                Element exceptionElement = createExceptionResponse(e,aTXLifeNew);
                return result.addResult(exceptionElement);
            }
			if(nbaTXLife==null)
			{
				nbaTXLife = failureResponse;
			}
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Response: " + failureResponse.toXmlString());
			}
			result.addResult(nbaTXLife);

		} catch (Throwable t) {
			NbaLogFactory.getLogger(this.getClass()).logException(t);
			t.printStackTrace();
			failureResponse = handleException(t, guid);
			result.addResult(failureResponse);
			return result;
		}
		return result;
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
	
	
	protected NbaTXLife createResponse(String guid, Long resultCode) {
		NbaTXLife nbaTXLife = new NbaTXLife();
		nbaTXLife.setTXLife(new TXLife());
		UserAuthResponseAndTXLifeResponseAndTXLifeNotify ua = new UserAuthResponseAndTXLifeResponseAndTXLifeNotify();
		nbaTXLife.getTXLife().setUserAuthResponseAndTXLifeResponseAndTXLifeNotify(ua);
		ua.setUserAuthResponse(new UserAuthResponse());
		ua.getUserAuthResponse().setSvrDate(new Date());
		ua.getUserAuthResponse().setSvrTime(new NbaTime());
		TXLifeResponse tXLifeResponse = new TXLifeResponse();
		ua.addTXLifeResponse(tXLifeResponse);
		tXLifeResponse.setTransRefGUID(guid);
		tXLifeResponse.setTransType(NbaOliConstants.TC_TYPE_HOLDINGSRCH);
		tXLifeResponse.setTransExeDate(new Date());
		tXLifeResponse.setTransExeTime(new NbaTime());
		tXLifeResponse.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		TransResult transResult = new TransResult();
		transResult.setResultCode(resultCode);
		tXLifeResponse.setTransResult(transResult);
		return nbaTXLife;

	}
	
	protected String getFullPathToCopy() throws NbaBaseException{
		return NbaConfiguration.getInstance().getFileLocation("predictive");
	}	
	
	protected NbaTXLife handleException(Throwable ex, String guid) {
		Document xmlDoc = null;
		Element txLife = null;
		NbaTXLife aTXLife=null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactoryImpl.newInstance();
			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
			xmlDoc = docBuilder.newDocument();
			txLife = xmlDoc.createElement("TXLife");
			Element txLifeResponse = xmlDoc.createElement("TXLifeResponse");
			txLife.appendChild(txLifeResponse);
			Element transResult = xmlDoc.createElement("TransResult");
			Date today = new Date();
			Element resultCode = xmlDoc.createElement("ResultCode");
			transResult.appendChild(resultCode);
			resultCode.setAttribute("tc", "5");
			resultCode.appendChild(xmlDoc.createTextNode("Failure"));
			Element transRefGUID = xmlDoc.createElement("TransRefGUID");
			Element transType = xmlDoc.createElement("TransType");
			Element transExeDate = xmlDoc.createElement("TransExeDate");
			Element transExeTime = xmlDoc.createElement("TransExeTime");
			Element transMode = xmlDoc.createElement("TransMode");
			Element transResult1 = xmlDoc.createElement("TransResult");
			txLifeResponse.appendChild(transRefGUID);
			txLifeResponse.appendChild(transType);
			txLifeResponse.appendChild(transExeDate);
			txLifeResponse.appendChild(transExeTime);
			txLifeResponse.appendChild(transMode);
			txLifeResponse.appendChild(transResult1);
			transRefGUID.appendChild(xmlDoc.createTextNode(guid));
			transType.setAttribute("tc", Long.toString(NbaOliConstants.TC_TYPE_CWA));
			transExeDate.appendChild(xmlDoc.createTextNode(formatDate(today)));
			transExeTime.appendChild(xmlDoc.createTextNode(formatTime(today)));
			transMode.setAttribute("tc", Long.toString(NbaOliConstants.TC_MODE_ORIGINAL));
			transMode.appendChild(xmlDoc.createTextNode("Original"));
			Element resultCode1 = xmlDoc.createElement("ResultCode");
			transResult1.appendChild(resultCode1);
			resultCode1.setAttribute("tc", "5");
			resultCode1.appendChild(xmlDoc.createTextNode("Failure"));
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
			formatStackTrace(ex, sb);
			sb.append("]]>");
			resultInfoDesc.appendChild(xmlDoc.createTextNode(sb.toString()));
			String txLifeStr = DOM2String(txLife.getOwnerDocument());
			StringBuffer txLifeStringBuffer = new StringBuffer(txLifeStr);
			String txLifeString = txLifeStringBuffer.toString();
			txLifeString = txLifeString.substring(txLifeString.indexOf("<TXLife"),txLifeString.indexOf("</TXLife>")+9);
            aTXLife = new NbaTXLife(txLifeString.toString());
           
			
		} catch (Throwable t) {
			NbaLogFactory.getLogger(this.getClass()).logException(t);
		}
		return aTXLife;

	}
	
	protected String formatDate(Date rawDate) {
		SimpleDateFormat dateFormat;
		dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String formattedDate = dateFormat.format(rawDate);
		return formattedDate;
	}	
	
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
	
	protected String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }
	
	protected void createFile(String path, StringBuffer txLife, Document doc) throws Exception{
		String GUID = null;
		GUID = getNodeValue(doc, "TransRefGUID");
		SimpleDateFormat GUID_SDF = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String datetime = GUID_SDF.format(new Date());
		System.out.println("Path==="+path+GUID);
		File xmlFile = new File(path, GUID + ".xml");
		if (xmlFile.exists()) {
			System.out.println("File exist.....");
			xmlFile = new File(path, GUID + "_" + datetime + ".xml");
		}
		getLogger().logDebug("NbaProviderIndexWebService Writing to file: " + xmlFile.getPath());
		OutputStream fileOut = new FileOutputStream(xmlFile);
		fileOut.write(txLife.toString().getBytes());
		fileOut.close();
	} 
	
	protected String getNodeValue(Document ele, String tagName) throws Exception{
		NodeList nodes = ele.getElementsByTagName(tagName);
		Node node = nodes.item(0);
		return node.getFirstChild().getNodeValue();
    }
	
	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaConfiguration.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaConfiguration could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}	
	
	protected static String DOM2String(Document doc) throws java.io.IOException {
		StringWriter sw = new StringWriter();
		OutputFormat oFormatter = new OutputFormat("XML", null, false);
		XMLSerializer oSerializer = new XMLSerializer(sw, oFormatter);
		oSerializer.serialize(doc);
		return sw.toString();
	}
	
	
    
}
