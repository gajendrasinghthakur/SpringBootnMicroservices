package com.csc.fsg.nba.process.tx151;

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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.assembler.tx151.TX151Assembler;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponse;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
import com.tbf.xml.XmlValidationError;

/**
 * TX151WebServiceBP is used for Jet Result.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th> </thead>
 * <tr>
 * <td>APSL4508 Websphere 8.5.5 Upgrade</td>
 * </tr>
 * </table>
 * <p>
 */

public class TX151WebServiceBP extends NewBusinessAccelBP {

	protected static NbaLogger logger = null;

	/**
	 * @param input
	 *            requires a TXLife request object
	 * @return the TXLife response is returned in the Resu-=========lt.
	 */
	public Result process(Object input) {
		Result result = new AccelResult();
		String response = null;
		StringBuffer txLifeBuffer = null;
		NbaTXLife aTXLife = null;
		String txLifeStr = null;
		try {
			aTXLife = (NbaTXLife) input;
			txLifeStr = aTXLife.toXmlString();
			txLifeBuffer = new StringBuffer(txLifeStr);
			// Remove the EIB headers and trailers
			// Check for namespace prefixes
			if (txLifeStr.indexOf("<TXLife") == -1) {
				String prefix = "";
				for (int i = txLifeStr.indexOf("TXLife"); i > 0; i--) {
					if (txLifeBuffer.charAt(i) == '<')
						break;
					prefix = txLifeStr.substring(i, txLifeStr.indexOf("TXLife"));
				}
				if (prefix.length() > 0) {
					txLifeBuffer = new StringBuffer(txLifeBuffer.toString().replaceAll("<" + prefix, "<"));
					txLifeBuffer = new StringBuffer(txLifeBuffer.toString().replaceAll("</" + prefix, "</"));
				}
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("TX151WebServiceBP Removed namespace prefix: " + txLifeBuffer.toString());
				}
			}

			txLifeBuffer.delete(0, txLifeBuffer.indexOf("<TXLife"));
			txLifeBuffer.delete(txLifeBuffer.toString().indexOf("</TXLife>") + 9, txLifeBuffer.length());
			Vector errors = null;
			errors = validateRequest(txLifeBuffer.toString());
			if (errors != null && errors.size() > 0) {
				result.addResult(createResponse(aTXLife, errors));
				return result;
			}

			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("TX151WebServiceBP Request: " + txLifeBuffer.toString());
			}

			// Deliver the transformed provider requirement result to DocumentInput
			String GUID = null;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(txLifeBuffer.toString())));
			GUID = getNodeValue(document, "TransRefGUID");
			TransformerFactory tranFactory = TransformerFactory.newInstance();
			Transformer aTransformer = tranFactory.newTransformer();
			Source src = new DOMSource(document);
			String path = getFullPathToCopy();
			StreamResult dest = new StreamResult(new File(path + GUID + ".xml"));
			aTransformer.transform(src, dest);
			response = createResponse(aTXLife, null);
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("TX151WebServiceBP Response: " + response);
			}
			result.addResult(response);

		} catch (Exception t) {
			NbaLogFactory.getLogger(this.getClass()).logException(t);
			t.printStackTrace();
			Vector<String> vec = new Vector<String>(1);
			StringBuffer sb = new StringBuffer();
			sb.append("<![CDATA[\n");
			sb.append(t.getMessage());
			sb.append("]]>");
			vec.addElement(sb.toString());
			response = createResponse(aTXLife, vec);
			result.addResult(response);
		}

		return result;
	}

	protected String createResponse(NbaTXLife txlife, Vector errors) {
		NbaTXLife nbaTXLife = new NbaTXLife();
		nbaTXLife.setTXLife(new TXLife());
		UserAuthResponseAndTXLifeResponseAndTXLifeNotify ua = new UserAuthResponseAndTXLifeResponseAndTXLifeNotify();
		nbaTXLife.getTXLife().setUserAuthResponseAndTXLifeResponseAndTXLifeNotify(ua);
		ua.setUserAuthResponse(new UserAuthResponse());
		ua.getUserAuthResponse().setSvrDate(new Date());
		ua.getUserAuthResponse().setSvrTime(new NbaTime());
		TXLifeResponse tXLifeResponse = new TXLifeResponse();
		ua.addTXLifeResponse(tXLifeResponse);
		tXLifeResponse.setTransRefGUID(txlife.getTransRefGuid());
		tXLifeResponse.setTransType(txlife.getTransType());
		tXLifeResponse.setTransExeDate(new Date());
		tXLifeResponse.setTransExeTime(new NbaTime());
		tXLifeResponse.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		TransResult transResult = new TransResult();
		if (errors != null && errors.size() > 0) {
			transResult.setResultCode(NbaOliConstants.TC_RESCODE_FAILURE);
			ResultInfo reInfo = new ResultInfo();
			reInfo.setResultInfoCode(NbaOliConstants.TC_RESINFO_GENERALDATAERR);
			XmlValidationError xmlError;
			ArrayList error=new ArrayList();
			for (int i = 0; i < errors.size(); i++) {
				if (errors.get(i) instanceof XmlValidationError) {
					xmlError = (XmlValidationError) errors.get(i);
					reInfo.setResultInfoDesc("DTD validation error : " + xmlError.getErrorMessage());
					error.add(reInfo);
				} else if (errors.get(i) instanceof String) {
					reInfo.setResultInfoDesc((String) errors.get(i));
					error.add(reInfo);
				}
			}
			transResult.setResultInfo(error);
		} else {
			transResult.setResultCode(NbaOliConstants.TC_RESCODE_SUCCESS);
		}
		tXLifeResponse.setTransResult(transResult);
		String returnVal=nbaTXLife.toXmlString();
		returnVal=returnVal.substring(returnVal.indexOf("<TXLife"));
		return returnVal;
	}

	protected String getFullPathToCopy() throws NbaBaseException {
		return NbaConfiguration.getInstance().getFileLocation("predictive");
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

	protected String getNodeValue(Document ele, String tagName) throws Exception {
		NodeList nodes = ele.getElementsByTagName(tagName);
		Node node = nodes.item(0);
		return node.getFirstChild().getNodeValue();
	}

	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(TX151Assembler.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("TX151WebServiceBP could not get a logger from the factory.");
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
	
	protected Vector validateRequest(String txLife) throws NbaBaseException, Exception {
		Vector errors = null;
		if (txLife != null) {
			if (NbaConfiguration.getInstance().isWebServiceDTDValidationON("nbA151WebService")) {
				NbaTXLife nbaTXLife = new NbaTXLife(txLife);
				Vector vctrErrors = nbaTXLife.getTXLife().getValidationErrors(false);
				if (vctrErrors != null && vctrErrors.size() > 0) {
					errors = new Vector();
					for (int i = 0; i < vctrErrors.size(); i++) {
						XmlValidationError temp = (XmlValidationError) vctrErrors.get(i);
						errors.add(temp.getErrorMessage());
					}
				}
			}
		}
		return errors;
	}		

}