package com.csc.fsg.nba.bean.accessors;

/*
 * **************************************************************************<BR>
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
 *     Copyright (c) 2002-2012 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************************<BR>
 */

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
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
import org.xml.sax.InputSource;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.database.NbaSuitabilityDecisionAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.configuration.DocumentInputDefinition;
import com.csc.fsg.nba.vo.configuration.DocumentInputDefinitions;
import com.csc.fsg.nba.vo.configuration.DocumentSource;
import com.csc.fsg.nba.vo.txlife.Activity;
import com.csc.fsg.nba.vo.txlife.ActivityEvent;
import com.csc.fsg.nba.vo.txlife.ActivityExtension;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponse;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;

/**
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL2735-EIP</td><td>Version 1</td><td>ACH</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 1
 * @since New Business Accelerator - Version 1201
 */

public class Nba508WebServiceBean{

	protected NbaLogger logger = null;
	protected static final String BUS_FUNCTION = "OTHERDATA";
	
	
	public Element submitPayment(Element ele) {

		StringBuffer TXLifeStr = null;
		String namespaceStr = null;
		Element responseTxLife = null;
		String guid = null;
		
		try {
			guid = getNodeValue(ele.getOwnerDocument(),"TransRefGUID");
			NbaTXLife response = createResponse(guid);
			String path = getFullPathToCopy();
			String txLife = DOM2String(ele.getOwnerDocument());
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("ProcessACH508 Request: " + txLife);
			}
			TXLifeStr = new StringBuffer(txLife);
			//Remove the EIB headers and trailers
			TXLifeStr.delete(0, TXLifeStr.indexOf("<TXLife"));
			TXLifeStr.delete(TXLifeStr.toString().indexOf("</TXLife>") + 9, TXLifeStr.length());
			//Remove the namespace definition from the result xml
			String start = "<TXLife";
			String end = ">";
			String xmlns =" xmlns=\"http://ACORD.org/Standards/Life/2\"";
			namespaceStr = substringBetween(TXLifeStr.toString(), start, end);
			int length = namespaceStr.length();
			TXLifeStr = new StringBuffer(start + xmlns + TXLifeStr.substring(start.length() + length).trim());
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(TXLifeStr.toString())));
			createFile(path, TXLifeStr, doc);
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Response: " + response.toXmlString());
			}
			Document doc1 = (DocumentBuilderFactoryImpl.newInstance()).newDocumentBuilder().parse(
					new ByteArrayInputStream(response.toXmlString().getBytes()));
			responseTxLife = doc1.getDocumentElement();

		} catch (Throwable t) {
			NbaLogFactory.getLogger(this.getClass()).logException(t);
			t.printStackTrace();
			responseTxLife = handleException(t,guid);
		}

		return responseTxLife;
	}
	/**
	 * Receives a document and converts it into a String
	 * 
	 * @param doc
	 *                 Document which contains the element to be converted into String
	 * @return String
	 */
	protected static String DOM2String(Document doc) throws java.io.IOException {
		StringWriter sw = new StringWriter();
		OutputFormat oFormatter = new OutputFormat("XML", null, false);
		XMLSerializer oSerializer = new XMLSerializer(sw, oFormatter);
		oSerializer.serialize(doc);
		return sw.toString();
	}
	protected void createFile(String path, StringBuffer txLife, Document doc) throws Exception{
		String GUID = null;
		GUID = getNodeValue(doc, "TransRefGUID");
		SimpleDateFormat GUID_SDF = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String datetime = GUID_SDF.format(new Date());
		File xmlFile = new File(path, GUID + ".xml");
		if (xmlFile.exists()) {
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
	protected String getFullPathToCopy() throws NbaBaseException{
		return NbaConfiguration.getInstance().getFileLocation("ACH");
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
	protected NbaTXLife createResponse(String guid) {
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
		tXLifeResponse.setTransType(NbaOliConstants.TC_TYPE_CWA);
		tXLifeResponse.setTransExeDate(new Date());
		tXLifeResponse.setTransExeTime(new NbaTime());
		tXLifeResponse.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		TransResult transResult = new TransResult();
		transResult.setResultCode(NbaOliConstants.TC_RESCODE_SUCCESS);
		tXLifeResponse.setTransResult(transResult);
		return nbaTXLife;

	}
	
	protected Element handleException(Throwable ex, String guid) {
		Document xmlDoc = null;
		Element txLife = null;
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
		} catch (Throwable t) {
			NbaLogFactory.getLogger(this.getClass()).logException(t);
		}
		return txLife;

	}
	
    /**
	 * Format a Stack Trace for an Exception
	 * 
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
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass());
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
}
