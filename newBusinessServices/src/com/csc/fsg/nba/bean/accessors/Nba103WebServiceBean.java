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
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import javax.ejb.SessionBean;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaValidator;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaLob;
import java.util.List;
import com.csc.fs.svcloc.ServiceLocator;
import com.csc.fsg.nba.business.process.AutoContractNumber;
/**
 * This is a stateless Session Bean that is used for submitting a new 
 * application request for a policy in NBA. It uses XPathAPI to check the
 * existence of a policy number in the incoming XML. If a policy number 
 * is not attached with the XML with the request, then it retrieves a 
 * policy number nbA system and assigns it to the XML. Then 
 * it creates a file in the AWD_RIP folder and creates the response using the Xerces API.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBP001</td><td>Version 3</td><td>nbProducer Initial Development</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR3614</td><td>AXA Life Phase 1</td><td>JVPMS Memory leak in Auto Contract Numbering logic</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class Nba103WebServiceBean implements SessionBean {
	protected static NbaLogger logger = null;
	private javax.ejb.SessionContext mySessionCtx;
	/**
	 * getSessionContext
	 */
	public javax.ejb.SessionContext getSessionContext() {
		return mySessionCtx;
	}
	/**
	 * setSessionContext
	 */
	public void setSessionContext(javax.ejb.SessionContext ctx) {
		mySessionCtx = ctx;
	}
	/**
	 * ejbCreate
	 */
	public void ejbCreate() throws javax.ejb.CreateException {
	}
	/**
	 * ejbActivate
	 */
	public void ejbActivate() {
	}
	/**
	 * ejbPassivate
	 */
	public void ejbPassivate() {
	}
	/**
	 * ejbRemove
	 */
	public void ejbRemove() {
	}
	/**
	 * Checks for existence of a policy number in the incoming XML,
	 * retrieves a policy number from system in case it is not 
	 * there and creates an XML file in the AWD_RIP folder.
	 * @param ele Element with the New Application submission data
	 * @return Element entire TXLife response, including policy number
	 */
	public Element submitApplication(Element ele) {
		String policyNumber = null;
		String errorCode = null;
		Node node;
		String GUID = null;
		NbaLob nbaLob = new NbaLob();
		Element txLife = null;
		int fileNamingLoopInfo;
		try {//NBA103
			//Do schema validation
			StringBuffer txLifeStrBuf = null;
			try {
				String txLifeStr = DOM2String(ele.getOwnerDocument());
				txLifeStrBuf = new StringBuffer(txLifeStr);
				txLifeStrBuf.delete(txLifeStr.indexOf("<SOAP-ENV"), txLifeStr.indexOf("<TXLife") - 1); //NBA093
				txLifeStrBuf.delete(txLifeStrBuf.toString().indexOf("</ele>"), txLifeStrBuf.length());
			} catch (Exception exp) {
				getLogger().logException(exp); //NBA103
				errorCode = "ERR003";
			}
			String dtdPath = NbaConfiguration.getInstance().getFileLocation("acordDTD");
			// Insert the schema against which the request will be validated		
			//begin NBA093
			if (txLifeStrBuf != null) {
				String txLifeStr = txLifeStrBuf.toString();
				if (txLifeStr.indexOf("<TXLife") > 0) {
					int start = txLifeStr.indexOf("<TXLife");
					int end = txLifeStr.indexOf(">", start) + 1;
					txLifeStrBuf.replace(
						start,
						end,
						"<TXLife xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
							+ "xsi:noNamespaceSchemaLocation=\""
							+ dtdPath
							+ "\">");
				}
			}
			//end NBA093
			// If block for testing against schema
			if (NbaConfiguration.getInstance().isWebServiceDTDValidationON("NBP103WEBSERVICE")) {
				NbaValidator validator = new NbaValidator();
				validator.validate(txLifeStrBuf.toString());
				ArrayList expList = validator.getExpList();
				int size = expList.size();
				if (size > 0) {
					txLife = createDTDErrorResponse(expList, ele);
					return txLife;
				}
			}
			node = getNode(ele, "TXLifeRequest/TransRefGUID");
			GUID = node.getFirstChild().getNodeValue();
			try {
				node = getNode(ele, "TXLifeRequest/OLifE/Holding/Policy/PolNumber");
				if (!(node.hasChildNodes())) {
					nbaLob.setCompany(
						getNode(ele, "TXLifeRequest/OLifE/Holding/Policy/CarrierCode").getFirstChild().getNodeValue());
					nbaLob.setProductTypSubtyp(
						getNode(ele, "TXLifeRequest/OLifE/Holding/Policy/ProductType /@tc")
							.getFirstChild()
							.getNodeValue());
					nbaLob.setPlan(
						getNode(ele, "TXLifeRequest/OLifE/Holding/Policy/ProductCode").getFirstChild().getNodeValue());
					NbaTableAccessor nbaTableAccessor = new NbaTableAccessor();
					NbaPlansData planData =
						nbaTableAccessor.getPlanData(setupTableMap(nbaLob.getCompany(), nbaLob.getPlan()));
					if (planData != null) {
						nbaLob.setBackendSystem(planData.getSystemId());
					}
					//begin SPR3614
					AutoContractNumber autoContractNumbering = (AutoContractNumber) ServiceLocator.lookup(AutoContractNumber.AUTO_CONTRACT_NUMBERING);
					List inputParam = new ArrayList(2);
					inputParam.add(new NbaOinkDataAccess(nbaLob));
					policyNumber = autoContractNumbering.generateContractNumber(inputParam);
					//end SPR3614
					node.appendChild((ele.getOwnerDocument()).createTextNode(policyNumber));
				} else {
					policyNumber = node.getFirstChild().getNodeValue();
				}
			} catch (Exception exp) { //NBA103
				getLogger().logException(exp); //NBA103
				errorCode = "ERR001";
			}
			try {
				String txLifeStr = DOM2String(ele.getOwnerDocument());
				txLifeStrBuf = new StringBuffer(txLifeStr);
				txLifeStrBuf.delete(txLifeStr.indexOf("<SOAP-ENV"), txLifeStr.indexOf("<TXLife>") - 1);
				txLifeStrBuf.delete(txLifeStrBuf.toString().indexOf("</ele>"), txLifeStrBuf.length());
			} catch (Exception exp) {
				getLogger().logException(exp); //NBA103
				errorCode = "ERR003";
			}
			String path = null;
			try {
				path = NbaConfiguration.getInstance().getNbProducerRippath().getPath(); //ACN012
			} catch (Exception exp) { //NBA103
				getLogger().logException(exp);//NBA103
				errorCode = "ERR002";
			}
			try {
				fileNamingLoopInfo = NbaConfiguration.getInstance().getNbProducerFileNamingLoop().getNumber(); //ACN012
			} catch (Exception exp) { //NBA103
				//initialize n with a default value if n is not retrieved from the NbaConfiguration.xml.
				getLogger().logException(exp);//NBA103
				fileNamingLoopInfo = 3;
			}
			//if there is no exception above or the policy number could not be generated, then print the file
			File xmlFile;
			if (errorCode == null || (errorCode != null && (errorCode.equals("ERR001")))) {
				xmlFile = new File(path, GUID + ".xml");
				for (int j = 0; j <= fileNamingLoopInfo; j++) {
					if (xmlFile.exists()) {
						//if loop iterator is equal to fileNamingLoopInfo, then dont create another File object.
						if (j != fileNamingLoopInfo) {
							xmlFile = new File(path, GUID + "_" + (j + 1) + ".xml");
						}
					} else {
						break;
					}
					//break the loop if the loop iterator is equal to fileNamingLoopInfo. The file is not to be created in this condition.
					if (j == fileNamingLoopInfo) {
						errorCode = "ERR002";
					}
				}
				if (errorCode == null || (errorCode != null && !(errorCode.equals("ERR002")))) {
					try {
						OutputStream fileOut = new FileOutputStream(xmlFile);
						fileOut.write(txLifeStrBuf.toString().getBytes());
						fileOut.close();
					} catch (Exception exp) { //NBA103
						getLogger().logException(exp);//NBA103
						errorCode = "ERR002";
					}
				}
			}
			// begin SPR3290
			if (txLife == null) {
				txLife = createResponse(policyNumber, ele, GUID, errorCode);
			}
			return txLife;
			// end SPR3290
		} catch (Exception exp) {//NBA103
			getLogger().logException(exp); //NBA103
			errorCode = "ERR002";//NBA103
			// begin SPR3290
			if (txLife == null) {
				txLife = createResponse(policyNumber, ele, GUID, errorCode);
			}
			return txLife;
			// end SPR3290
		}
		// SPR3290 code deleted
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
	 * Uses the XPathAPI to retrieve the value of a node from the root node.
	 * @param root Node that is the root node 
	 * @param pattern String that defines the pattern which has to be traversed using XPathAPI
	 * @return Node containing the node	 
	 */
	protected static Node getNode(Node root, String pattern) throws Exception {
		return XPathAPI.selectSingleNode(root, pattern);
	}
	/**
	 * Uses the Xerces API create a response from the class.
	 * @param policyNumber String contains the policy number. 
	 * @param ele Element that is the incoming XML
	 * @param GUID String that is retrieved from the incoming XML
	 * @param errorCode
	 * @return Element containing the response	 
	 */
	protected Element createResponse(String policyNumber, Element ele, String GUID, String errorCode) {
		Document xmlDoc = null;
		Element txLife = null;
		try {
			//Create an XML Document
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
			if (errorCode == null || (errorCode != null && errorCode.equals("ERR001"))) {
				resultCode.setAttribute("tc", "1");
				resultCode.appendChild(xmlDoc.createTextNode("Success"));
			} else {
				resultCode.setAttribute("tc", "5");
				resultCode.appendChild(xmlDoc.createTextNode("Failure"));
			}
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
			transRefGUID.appendChild(xmlDoc.createTextNode(GUID));
			transType.setAttribute("tc", "103");
			transType.appendChild(xmlDoc.createTextNode("New Business Application"));
			transExeDate.appendChild(xmlDoc.createTextNode(formatDate(today)));
			//extract date from request using XPathAPI
			transExeTime.appendChild(xmlDoc.createTextNode(formatTime(today)));
			transMode.setAttribute("tc", getNode(ele, "TXLifeRequest/TransMode/@tc").getFirstChild().getNodeValue());
			transMode.appendChild(
				xmlDoc.createTextNode(getNode(ele, "TXLifeRequest/TransMode").getFirstChild().getNodeValue()));
			//Create and add first level elements for TransResult in TXLifeResponse
			Element resultCode1 = xmlDoc.createElement("ResultCode");
			transResult1.appendChild(resultCode1);
			if (errorCode == null || (errorCode != null && errorCode.equals("ERR001"))) {
				resultCode1.setAttribute("tc", "1");
				resultCode1.appendChild(xmlDoc.createTextNode("Success"));
			} else {
				resultCode1.setAttribute("tc", "5");
				resultCode1.appendChild(xmlDoc.createTextNode("Failure"));
			}
			//in case of exception, these elements are generated
			if (errorCode != null) {
				Element resultInfo = xmlDoc.createElement("ResultInfo");
				transResult1.appendChild(resultInfo);
				Element resultInfoCode = xmlDoc.createElement("ResultInfoCode");
				Element resultInfoDesc = xmlDoc.createElement("ResultInfoDesc");
				resultInfo.appendChild(resultInfoCode);
				resultInfo.appendChild(resultInfoDesc);
				if (errorCode.equals("ERR002") || errorCode.equals("ERR003")) {
					resultInfoCode.setAttribute("tc", "600");
					resultInfoCode.appendChild(xmlDoc.createTextNode("Unable to Process Transaction at this Time"));
					resultInfoDesc.appendChild(xmlDoc.createTextNode("The File could not be created"));
				} else if (errorCode.equals("ERR001")) {
					resultInfoDesc.appendChild(xmlDoc.createTextNode("The contract number could not be generated"));
				}
			}
			if (errorCode == null) {
				//create olife elements
				Element holding = xmlDoc.createElement("Holding");
				oLifE.appendChild(holding);
				holding.setAttribute("id", "XMHoldingDO_1");
				//create and Add elemetns for holding
				Element holdingTypeCode = xmlDoc.createElement("HoldingTypeCode");
				Element policy = xmlDoc.createElement("Policy");
				holding.appendChild(holdingTypeCode);
				holding.appendChild(policy);
				holdingTypeCode.setAttribute("tc", "2");
				holdingTypeCode.appendChild(xmlDoc.createTextNode("Policy"));
				//create and append elements for policy
				Element polNumber = xmlDoc.createElement("PolNumber");
				policy.appendChild(polNumber);
				polNumber.appendChild(xmlDoc.createTextNode(policyNumber));
			}
		} catch (Exception e) {
			getLogger().logException(e);//NBA103
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
	 * Builds the map for retrieving the table values.
	 * Since there is no case at this time, so cannot use 
	 * @param company the company code
	 * @param coverageKey the coverage key
	 * @return <code>Map</code> contains fields and values for retrieving data
	 * @exception NbaBaseException If errors occur while trying to create the HashMap. This
	 *                             exception is thrown for <code>InvocationTargetException</code>, 
	 *                             </code>IllegalArgumentException</code> and <code>IllegalAccessException</code>. 
	 */
	protected Map setupTableMap(String company, String coverageKey) throws NbaBaseException {
		NbaTableAccessor nbaTableAccessor = new NbaTableAccessor();
		Map myCaseMap = nbaTableAccessor.createDefaultHashMap("*");
		if (company != null) {
			myCaseMap.put(NbaTableAccessConstants.C_COMPANY_CODE, company);
		}
		if (coverageKey != null) {
			myCaseMap.put(NbaTableAccessConstants.C_COVERAGE_KEY, coverageKey);
		}
		return myCaseMap;
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(Nba103WebServiceBean.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("Nba103WebServiceBean could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * Creates a failure response when a DTD validation error has occured
	 * @param expList Exception list 
	 * @param ele
	 * @return Element containing the response
	 */
	protected Element createDTDErrorResponse(ArrayList expList, Element ele) {
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
			transRefGUID.appendChild(
				xmlDoc.createTextNode(getNode(ele, "TXLifeRequest/TransRefGUID").getFirstChild().getNodeValue()));
			transType.setAttribute("tc", getNode(ele, "TXLifeRequest/TransType/@tc").getFirstChild().getNodeValue());
			transType.appendChild(
				xmlDoc.createTextNode(getNode(ele, "TXLifeRequest/TransType").getFirstChild().getNodeValue()));
			//extract date from request using XPathAPI
			transExeDate.appendChild(xmlDoc.createTextNode(formatDate(today)));
			//extract date from request using XPathAPI
			transExeTime.appendChild(xmlDoc.createTextNode(formatTime(today)));
			transMode.setAttribute("tc", getNode(ele, "TXLifeRequest/TransMode/@tc").getFirstChild().getNodeValue());
			transMode.appendChild(
				xmlDoc.createTextNode(getNode(ele, "TXLifeRequest/TransMode").getFirstChild().getNodeValue()));
			//Create and add first level elements for TransResult in TXLifeResponse
			Element resultCode1 = xmlDoc.createElement("ResultCode");
			transResult1.appendChild(resultCode1);
			resultCode1.setAttribute("tc", "5");
			resultCode1.appendChild(xmlDoc.createTextNode("Failure"));
			Element resultInfo;
			Element resultInfoCode;
			Element resultInfoDesc;
			int size = expList.size();
			Exception tempError;
			for (int i = 0; i < size; i++) {
				tempError = (Exception) expList.get(i);
				resultInfo = xmlDoc.createElement("ResultInfo");
				transResult1.appendChild(resultInfo);
				resultInfoCode = xmlDoc.createElement("ResultInfoCode");
				resultInfoDesc = xmlDoc.createElement("ResultInfoDesc");
				resultInfo.appendChild(resultInfoCode);
				resultInfo.appendChild(resultInfoDesc);
				resultInfoCode.setAttribute("tc", "200");
				resultInfoCode.appendChild(xmlDoc.createTextNode("General Data Error"));
				resultInfoDesc.appendChild(xmlDoc.createTextNode("DTD validation error : " + tempError.getMessage()));
			}
		} catch (Exception e) {
			getLogger().logException(e);//NBA103
		}
		return txLife;
	}
}
