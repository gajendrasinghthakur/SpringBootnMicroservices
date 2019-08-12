package com.csc.fsg.nba.process.tx203;

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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.csc.fs.Result;
import com.csc.fs.accel.newBusiness.markup.ContractMarkup;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.svcloc.ServiceLocator;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.assembler.tx203.TX203WebServiceAssembler;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.SourceInfo;
import com.csc.fsg.nba.vo.txlife.SourceInfoExtension;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.tbf.xml.XmlValidationError;

/**
 * TX203WebServiceBP is used for do Contract Inquiry.
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
 * 
 * @author CSC FSG Developer
 * @since New Business Accelerator - APSL4508
 */

public class TX203WebServiceBP extends NewBusinessAccelBP {

	protected static NbaLogger logger = null;

	/**
	 * This method supports do contract inquiry and call assembler & return result.
	 * 
	 * @param input
	 *            requires a TXLife request object
	 * @return the TXLife response is returned in the Result.
	 */

	public Result process(Object input) {

		Result result = new AccelResult();
		NbaTXLife aTXLife = (NbaTXLife) input;
		try {
			SourceInfo sourceInfo = aTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getOLifE().getSourceInfo();
			String backendId = null;

			if (sourceInfo != null) {
				backendId = sourceInfo.getFileControlID();
			}
			if (backendId == null || backendId.equals("null")) {
				String plan = aTXLife.getPrimaryHolding().getPolicy().getProductCode(); // NBA213
				if (plan == null || plan.equals("null")) {
					throw new NbaBaseException("Backend ID and Plan both are missing");
				}
				// Find the file control id
				String companyCode = aTXLife.getPrimaryHolding().getPolicy().getCarrierCode();
				backendId = getBackendId(companyCode, plan);
				if (sourceInfo == null) {
					sourceInfo = new SourceInfo();
				}
				sourceInfo.setFileControlID(backendId);
				aTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getOLifE().setSourceInfo(sourceInfo);
			}
			aTXLife.setBusinessProcess(NbaConstants.PROC_NBP); // SPR2639
			Vector vctrErrors = null;
			if (NbaConfiguration.getInstance().isWebServiceDTDValidationON("NBP203WEBSERVICE")) {
				vctrErrors = aTXLife.getTXLife().getValidationErrors(false);
			}
			if (vctrErrors != null && vctrErrors.size() > 0) {
				return result.addResult(createDTDErrorResponse(vctrErrors, aTXLife));
			}
			String auditversion = "0";
			// If there is validation error, no need to process further
			if (null != aTXLife.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify()) {
				return result;
			}
			OLifE olife = aTXLife.getOLifE();
			SourceInfo srcInfo = olife.getSourceInfo();

			if (srcInfo.getOLifEExtensionCount() > 0 && srcInfo.getOLifEExtensionAt(0) != null) {
				SourceInfoExtension ext = srcInfo.getOLifEExtensionAt(0).getSourceInfoExtension();
				auditversion = ext.getHoldingVersion();
			}
			aTXLife.setAccessIntent(NbaConstants.READ);
			aTXLife.setBusinessProcess(NbaConstants.PROC_NBP);

			// Code for checking Backend Id.
			defaultBackendSystem(aTXLife);
			// Call doContractInquiry for contract.
			aTXLife.setCaseInd(true);
			NbaTXLife aTXLife_new;
			if (Integer.parseInt(auditversion) > 0) {
				aTXLife_new = NbaContractAccess.doAuditInquiry(aTXLife, Integer.parseInt(auditversion));
			} else {
				aTXLife_new = NbaContractAccess.doContractInquiry(aTXLife);
			}
			ContractMarkup contractMarkup = (ContractMarkup) ServiceLocator.lookup(ContractMarkup.NBA_HOLDING_INQUIRY_MARKUP);
			aTXLife_new = contractMarkup.markup(aTXLife_new);
			String returnString = aTXLife_new.toXmlString();
			StringBuffer TXLifeStr = new StringBuffer(returnString);
			TXLifeStr.delete(TXLifeStr.indexOf("<?xml"), TXLifeStr.indexOf("<TXLife"));
			returnString=TXLifeStr.toString();
			// APSL5255 :: tx203 will not generate when special char (non UTF-8) available in 203, Replacing Special Char
			// With Space 
			//Pattern pattern = Pattern.compile("[^\\x00-\\x7F]");
			//Matcher matcher = pattern.matcher(returnString);
			//String returnUpdatedString = matcher.replaceAll("");
			// String returnUpdatedString = returnStringTemp.replace("¿", " ");
			String returnUpdatedString = returnString.replaceAll("[^\\x00-\\x7F]", " ");
			// APSL5255 :: END 
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("TX203WebServiceBP Response: " + returnUpdatedString);
			}
			result.addResult(returnUpdatedString);
		} catch (Throwable e) {
			return result.addResult(createExceptionResponse(e, aTXLife));
		}
		// Build TXLife response. This will add errors to the TXLife if they exist
		return result;
	}

	/**
	 * @param aTXLife
	 * @throws NbaBaseException
	 * @throws Exception
	 */

	protected void defaultBackendSystem(NbaTXLife aTXLife) throws Exception {
		String backendId = null;
		OLifE olife = aTXLife.getOLifE();
		SourceInfo sourceInfo = olife.getSourceInfo();
		if (sourceInfo == null) {
			sourceInfo = new SourceInfo();
			olife.setSourceInfo(sourceInfo);
		} else {
			backendId = sourceInfo.getFileControlID();
		}
		Policy policy = aTXLife.getPolicy();
		if (backendId == null) {
			if (policy.getProductCode() == null) {
				throw new NbaBaseException("Backend ID and Plan both are missing");
			}

			sourceInfo.setFileControlID(getBackendId(policy));
		}
	}

	/**
	 * Method for getting Backend ID.
	 * 
	 * @param policy
	 * @return String SystemId
	 */
	protected String getBackendId(Policy policy) throws NbaBaseException {
		NbaTableAccessor tableAccessor = new NbaTableAccessor();
		Map tblKeys = tableAccessor.createDefaultHashMap("*");
		tblKeys.put(NbaTableAccessConstants.C_COMPANY_CODE, policy.getCarrierCode()); // NBA330
		tblKeys.put(NbaTableAccessConstants.C_COVERAGE_KEY, policy.getProductCode()); // NBA330
		NbaPlansData planData = tableAccessor.getPlanData(tblKeys);
		if (planData == null) {
			throw new NbaBaseException("Not able to find the plan information for plan code " + policy.getProductCode());
		}
		return planData.getSystemId();
	}

	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(TX203WebServiceAssembler.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("TX203WebServiceBP could not get a logger from the factory.");
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

	/**
	 * Creates a failure response for 203 transaction in case of 'other' error.
	 * 
	 * @param ele
	 *            Element with the New Application submission data
	 * @param vctrErrors
	 *            Vector containing the validation errors.
	 * @param aTXLife
	 *            NbaTXLife value object containing the data recieved in the request
	 * @return Element containing the Error message
	 */
	// AXAL3.7.35 new method
	protected Element createExceptionResponse(Throwable exp) {
		String guid = NbaUtils.getGUID();
		Document xmlDoc = null;
		Element txLife = null;
		try {
			// Create a XML Document
			DocumentBuilderFactory dbFactory = DocumentBuilderFactoryImpl.newInstance();
			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
			xmlDoc = docBuilder.newDocument();
			txLife = xmlDoc.createElement("TXLife");
			Element userAuthResponse = xmlDoc.createElement("UserAuthResponse");
			Element txLifeResponse = xmlDoc.createElement("TXLifeResponse");
			txLife.appendChild(userAuthResponse);
			txLife.appendChild(txLifeResponse);
			// Add code for UserAuthResponse contents
			Element transResult = xmlDoc.createElement("TransResult");
			Element svrDate = xmlDoc.createElement("svrDate");
			Element svrTime = xmlDoc.createElement("svrTime");
			userAuthResponse.appendChild(transResult);
			userAuthResponse.appendChild(svrDate);
			userAuthResponse.appendChild(svrTime);
			// Set the values in server date and server time
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
			// Add code for TXLifeResponse contents
			// Create first level element for TXLifeResponse
			Element transRefGUID = xmlDoc.createElement("TransRefGUID");
			Element transType = xmlDoc.createElement("TransType");
			Element transExeDate = xmlDoc.createElement("TransExeDate");
			Element transExeTime = xmlDoc.createElement("TransExeTime");
			Element transMode = xmlDoc.createElement("TransMode");
			Element transResult1 = xmlDoc.createElement("TransResult");
			Element oLifE = xmlDoc.createElement("OLifE");
			// Add first level element for TXLifeResponse
			txLifeResponse.appendChild(transRefGUID);
			txLifeResponse.appendChild(transType);
			txLifeResponse.appendChild(transExeDate);
			txLifeResponse.appendChild(transExeTime);
			txLifeResponse.appendChild(transMode);
			txLifeResponse.appendChild(transResult1);
			txLifeResponse.appendChild(oLifE);
			// set the values for trans ref guid, trans type, trans exe date, trans exe time and trans mode
			transRefGUID.appendChild(xmlDoc.createTextNode(guid));
			transType.setAttribute("tc", "203");
			transExeDate.appendChild(xmlDoc.createTextNode(formatDate(today)));
			// extract date from
			transExeTime.appendChild(xmlDoc.createTextNode(formatTime(today)));
			transMode.setAttribute("tc", "2");
			// Create and add first level elements for TransResult in TXLifeResponse
			Element resultCode1 = xmlDoc.createElement("ResultCode");
			transResult1.appendChild(resultCode1);
			resultCode1.setAttribute("tc", "5");
			resultCode1.appendChild(xmlDoc.createTextNode("Failure"));
			// in case of exception, these elements are generated
			// in case of exception, these elements are generated
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
		} catch (Throwable t) { // NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(t);
		}
		return txLife;
	}

	/**
	 * Creates a failure response for 203 transaction in case of DTD error.
	 * 
	 * @param ele
	 *            Element with the New Application submission data
	 * @param vctrErrors
	 *            Vector containing the validation errors.
	 * @param aTXLife
	 *            NbaTXLife value object containing the data recieved in the request
	 * @return Element containing the Error message
	 */
	protected String createDTDErrorResponse(Vector vctrErrors, NbaTXLife aTXLife) {
		Document xmlDoc = null;
		Element txLife = null;
		String returnString = null;
		try {
			// Create a XML Document
			DocumentBuilderFactory dbFactory = DocumentBuilderFactoryImpl.newInstance();
			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
			xmlDoc = docBuilder.newDocument();
			txLife = xmlDoc.createElement("TXLife");
			Element userAuthResponse = xmlDoc.createElement("UserAuthResponse");
			Element txLifeResponse = xmlDoc.createElement("TXLifeResponse");
			txLife.appendChild(userAuthResponse);
			txLife.appendChild(txLifeResponse);
			// Add code for UserAuthResponse contents
			Element transResult = xmlDoc.createElement("TransResult");
			Element svrDate = xmlDoc.createElement("svrDate");
			Element svrTime = xmlDoc.createElement("svrTime");
			userAuthResponse.appendChild(transResult);
			userAuthResponse.appendChild(svrDate);
			userAuthResponse.appendChild(svrTime);
			// Set the values in server date and server time
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
			// Add code for TXLifeResponse contents
			// Create first level element for TXLifeResponse
			Element transRefGUID = xmlDoc.createElement("TransRefGUID");
			Element transType = xmlDoc.createElement("TransType");
			Element transExeDate = xmlDoc.createElement("TransExeDate");
			Element transExeTime = xmlDoc.createElement("TransExeTime");
			Element transMode = xmlDoc.createElement("TransMode");
			Element transResult1 = xmlDoc.createElement("TransResult");
			Element oLifE = xmlDoc.createElement("OLifE");
			// Add first level element for TXLifeResponse
			txLifeResponse.appendChild(transRefGUID);
			txLifeResponse.appendChild(transType);
			txLifeResponse.appendChild(transExeDate);
			txLifeResponse.appendChild(transExeTime);
			txLifeResponse.appendChild(transMode);
			txLifeResponse.appendChild(transResult1);
			txLifeResponse.appendChild(oLifE);
			TXLifeRequest txLifeRequest =
					aTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0);
			// set the values for trans ref guid, trans type, trans exe date, trans exe time and trans mode
			transRefGUID.appendChild(xmlDoc.createTextNode(aTXLife.getTransRefGuid()));
			transType.setAttribute("tc", new Long(txLifeRequest.getTransType()).toString());
			transExeDate.appendChild(xmlDoc.createTextNode(formatDate(today)));
			// extract date from
			transExeTime.appendChild(xmlDoc.createTextNode(formatTime(today)));
			transMode.setAttribute("tc", new Long(txLifeRequest.getTransMode()).toString());
			// Create and add first level elements for TransResult in TXLifeResponse
			Element resultCode1 = xmlDoc.createElement("ResultCode");
			transResult1.appendChild(resultCode1);
			resultCode1.setAttribute("tc", "5");
			resultCode1.appendChild(xmlDoc.createTextNode("Failure"));
			// in case of exception, these elements are generated
			Element resultInfo;
			Element resultInfoCode;
			Element resultInfoDesc;
			XmlValidationError tempError;
			for (int i = 0; i < vctrErrors.size(); i++) {
				tempError = (XmlValidationError) vctrErrors.get(i);
				resultInfo = xmlDoc.createElement("ResultInfo");
				transResult1.appendChild(resultInfo);
				resultInfoCode = xmlDoc.createElement("ResultInfoCode");
				resultInfoDesc = xmlDoc.createElement("ResultInfoDesc");
				resultInfo.appendChild(resultInfoCode);
				resultInfo.appendChild(resultInfoDesc);
				resultInfoCode.setAttribute("tc", "200");
				resultInfoCode.appendChild(xmlDoc.createTextNode("General Data Error"));
				resultInfoDesc.appendChild(
						xmlDoc.createTextNode("DTD validation error : " + tempError.getErrorMessage()));
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				StreamResult result = new StreamResult(new StringWriter());
				DOMSource source = new DOMSource(txLife);
				transformer.transform(source, result);
				returnString = result.getWriter().toString();
				StringBuffer TXLifeStr = new StringBuffer(returnString);
				TXLifeStr.delete(TXLifeStr.indexOf("<?xml"), TXLifeStr.indexOf("<TXLife"));
				returnString = TXLifeStr.toString();
			}
		} catch (Throwable t) { // NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(t);
		}
		return returnString;
	}

	/**
	 * Creates a failure response for 203 transaction when an Exception occurs.
	 * 
	 * @param ele
	 *            Element with the New Application submission data
	 * @param vctrErrors
	 *            Vector containing the validation errors.
	 * @param aTXLife
	 *            NbaTXLife value object containing the data recieved in the request
	 * @return Element containing the Error message
	 */
	// NBA213 New Method
	protected String createExceptionResponse(Throwable exp, NbaTXLife aTXLife) {
		Document xmlDoc = null;
		String returnString = null;
		Element txLife = null;
		try {
			// Create a XML Document
			DocumentBuilderFactory dbFactory = DocumentBuilderFactoryImpl.newInstance();
			DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
			xmlDoc = docBuilder.newDocument();
			txLife = xmlDoc.createElement("TXLife");
			Element userAuthResponse = xmlDoc.createElement("UserAuthResponse");
			Element txLifeResponse = xmlDoc.createElement("TXLifeResponse");
			txLife.appendChild(userAuthResponse);
			txLife.appendChild(txLifeResponse);
			// Add code for UserAuthResponse contents
			Element transResult = xmlDoc.createElement("TransResult");
			Element svrDate = xmlDoc.createElement("svrDate");
			Element svrTime = xmlDoc.createElement("svrTime");
			userAuthResponse.appendChild(transResult);
			userAuthResponse.appendChild(svrDate);
			userAuthResponse.appendChild(svrTime);
			// Set the values in server date and server time
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
			// Add code for TXLifeResponse contents
			// Create first level element for TXLifeResponse
			Element transRefGUID = xmlDoc.createElement("TransRefGUID");
			Element transType = xmlDoc.createElement("TransType");
			Element transExeDate = xmlDoc.createElement("TransExeDate");
			Element transExeTime = xmlDoc.createElement("TransExeTime");
			Element transMode = xmlDoc.createElement("TransMode");
			Element transResult1 = xmlDoc.createElement("TransResult");
			Element oLifE = xmlDoc.createElement("OLifE");
			// Add first level element for TXLifeResponse
			txLifeResponse.appendChild(transRefGUID);
			txLifeResponse.appendChild(transType);
			txLifeResponse.appendChild(transExeDate);
			txLifeResponse.appendChild(transExeTime);
			txLifeResponse.appendChild(transMode);
			txLifeResponse.appendChild(transResult1);
			txLifeResponse.appendChild(oLifE);
			TXLifeRequest txLifeRequest = aTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0);
			// set the values for trans ref guid, trans type, trans exe date, trans exe time and trans mode
			transRefGUID.appendChild(xmlDoc.createTextNode(aTXLife.getTransRefGuid()));
			transType.setAttribute("tc", new Long(txLifeRequest.getTransType()).toString());
			transExeDate.appendChild(xmlDoc.createTextNode(formatDate(today)));
			// extract date from
			transExeTime.appendChild(xmlDoc.createTextNode(formatTime(today)));
			transMode.setAttribute("tc", new Long(txLifeRequest.getTransMode()).toString());
			// Create and add first level elements for TransResult in TXLifeResponse
			Element resultCode1 = xmlDoc.createElement("ResultCode");
			transResult1.appendChild(resultCode1);
			resultCode1.setAttribute("tc", "5");
			resultCode1.appendChild(xmlDoc.createTextNode("Failure"));
			// in case of exception, these elements are generated
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
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(txLife);
			transformer.transform(source, result);
			returnString = result.getWriter().toString();
			StringBuffer TXLifeStr = new StringBuffer(returnString);
			TXLifeStr.delete(TXLifeStr.indexOf("<?xml"), TXLifeStr.indexOf("<TXLife"));
			returnString = TXLifeStr.toString();
		} catch (Throwable t) { // NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(t);
		}
		return returnString;
	}

	/**
	 * Format a Stack Trace for an Exception
	 * 
	 * @param exp
	 * @param sb
	 */
	// NBA213 New Method
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
	 * 
	 * @param rawDate
	 *            the date/time in Java internal format.
	 * @return String the date as a properly formatted String.
	 */
	protected String formatDate(Date rawDate) {
		SimpleDateFormat dateFormat;
		dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String formattedDate = dateFormat.format(rawDate);
		return formattedDate;
	}

	/**
	 * Formats a Date's time for use in the response node.
	 * 
	 * @param rawTime
	 *            the date/time in Java internal format.
	 * @return String the time as a properly formatted String.
	 */
	protected String formatTime(Date rawTime) {
		SimpleDateFormat dateFormat;
		dateFormat = new SimpleDateFormat("HH:mm:ss");
		long zoneOffset = (new GregorianCalendar()).get(Calendar.ZONE_OFFSET);
		DecimalFormat hoursFormat = new DecimalFormat("+00;-00");
		DecimalFormat minutesFormat = new DecimalFormat("00");
		String formattedTime = dateFormat.format(rawTime) + hoursFormat.format(zoneOffset / 3600000) + ":"
				+ minutesFormat.format((zoneOffset / 60000) % 60);
		return formattedTime;
	}

	/**
	 * This method retrieves the backend id for a policy using Company code and plan
	 * 
	 * @param companyCode
	 *            company code
	 * @param plan
	 *            plan
	 * @return String backend id
	 * @exception Exception
	 */
	protected String getBackendId(String companyCode, String plan) throws Exception {
		NbaTableAccessor tableAccessor = new NbaTableAccessor();
		Map tblKeys = tableAccessor.createDefaultHashMap("*"); // Wild card the backend id to retrieve plan data
		if (companyCode != null) {
			tblKeys.put(NbaTableAccessConstants.C_COMPANY_CODE, companyCode);
		}
		if (plan != null) {
			tblKeys.put(NbaTableAccessConstants.C_COVERAGE_KEY, plan);
		}
		NbaPlansData planData = tableAccessor.getPlanData(tblKeys);
		if (planData == null) {
			throw new NbaBaseException("Plan is invalid.");
		}
		return planData.getSystemId();
	}

}