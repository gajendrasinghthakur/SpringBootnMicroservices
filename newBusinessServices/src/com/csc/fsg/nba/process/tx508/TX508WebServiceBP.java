package com.csc.fsg.nba.process.tx508;

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
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.assembler.tx508.TX508Assembler;
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
 * TX508WebServiceBP is used Submit Payment.
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

public class TX508WebServiceBP extends NewBusinessAccelBP {
     
	protected static NbaLogger logger = null;
    /**
     * This method supports do contract inquiry and call assembler & return result.
     * 
     * @param input requires a TXLife request object
     * @return the TXLife response is returned in the Resu-=========lt.
     */
	public Result process(Object input) {
		Result result = new AccelResult();
		String guid = null;
		NbaTXLife response = null;
		StringBuffer TXLifeStr = null;
		try {
			NbaTXLife aTXLife = (NbaTXLife) input;
			guid = aTXLife.getTransRefGuid();
			response = createResponse(guid);
			String path = getFullPathToCopy();
			TXLifeStr = new StringBuffer(aTXLife.toXmlString());
			// Remove the Extra data
			TXLifeStr.delete(0, TXLifeStr.indexOf("<TXLife"));
			TXLifeStr.delete(TXLifeStr.toString().indexOf("</TXLife>") + 9, TXLifeStr.length());
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("ProcessACH508 Request: " + TXLifeStr.toString());
			}
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(TXLifeStr.toString())));
			createFile(path, TXLifeStr, doc);
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("ProcessACH508 Response: " + response.toXmlString());
			}
			result.addResult(response);

		} catch (Exception t) {
			NbaLogFactory.getLogger(this.getClass()).logException(t);
			t.printStackTrace();
			response = handleException(t, guid);
			result.addResult(response);
		}
		
		return result;
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
	
	protected String getFullPathToCopy() throws NbaBaseException{
		return NbaConfiguration.getInstance().getFileLocation("ACH");
	}	
	
	protected NbaTXLife handleException(Exception ex, String guid) {
		Document xmlDoc = null;
		Element txLife = null;
		NbaTXLife aTXLife=null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
			xmlDoc = docBuilder.newDocument();
			txLife = xmlDoc.createElement("TXLife");
			txLife.setAttribute("xmlns", "http://ACORD.org/Standards/Life/2");
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
			sb.append(ex.getMessage());
			sb.append("]]>");
			resultInfoDesc.appendChild(xmlDoc.createTextNode(sb.toString()));
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			StringWriter buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(txLife),
			      new StreamResult(buffer));
			String str = buffer.toString();
			aTXLife = new NbaTXLife(str);
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
	
     protected void createFile(String path, StringBuffer txLife, Document doc) throws Exception{
		String GUID = null;
		GUID = getNodeValue(doc, "TransRefGUID");
		SimpleDateFormat GUID_SDF = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String datetime = GUID_SDF.format(new Date());
		File xmlFile = new File(path, GUID + ".xml");
		if (xmlFile.exists()) {
			xmlFile = new File(path, GUID + "_" + datetime + ".xml");
		}
		
		if (getLogger().isDebugEnabled()) {
		getLogger().logDebug("TX508WebServiceBP Writing to file: " + xmlFile.getPath());
		}
	
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
				logger = NbaLogFactory.getLogger(TX508Assembler.class.getName());
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