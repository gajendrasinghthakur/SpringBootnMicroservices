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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Vector;
import javax.ejb.SessionBean;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.csc.fs.Result;
import com.csc.fs.ServiceContext;
import com.csc.fs.UserCredential;
import com.csc.fs.UserSessionController;
import com.csc.fs.session.UserCredentialBase;
import com.csc.fs.session.UserSessionControllerFactory;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.SourceInfo;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.tbf.xml.XmlValidationError;
/**
 * This is a stateless Session Bean that is used for doing an Inquiry 
 * for a policy from the backend. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBP001</td><td>Version 3</td><td>nbProducer Initial Development</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA103</td><td>Version 3</td><td>Logging</td></tr>
 * <tr><td>SPR2066</td><td>Version 4</td><td>Remove underwriter notes</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr> 
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class Nba203WebServiceBean implements SessionBean {
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
	 * Recieves a 203 request as an XML element, checks for DTD validations,
	 * performs a holding inquiry for the policy number supplied in 
	 * the request xml stream and returns it.
	 * @param ele Element with the New Application submission data
	 * @return Element containing the policy number	 
	 */
	public Element getHoldingInquiry(Element ele) {
		Element txLife = null;
		try {//NBA103
			String txLifeStr = DOM2String(ele.getOwnerDocument());
			//begin AXAL3.7.35
			StringBuffer txLifeStringBuffer = new StringBuffer(txLifeStr);
			String txLifeString = txLifeStringBuffer.toString();
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Incoming 203 before parse");
				getLogger().logDebug(txLifeString);
			}
            txLifeString = txLifeString.substring(txLifeString.indexOf("<TXLife"),txLifeString.indexOf("</TXLife>")+9);
            //PERF-APSL479 wrap in debug check
            if (getLogger().isDebugEnabled()) {
            	getLogger().logDebug("Incoming 203 after string parse");
            	getLogger().logDebug(txLifeString);
            }
			//DTD validations using Breeze			
			NbaTXLife aTXLife = new NbaTXLife(txLifeString.toString());
            //PERF-APSL479 wrap in debug check
            if (getLogger().isDebugEnabled()) {
            	getLogger().logDebug("Incoming 203 after XML parse");
            	getLogger().logDebug(aTXLife.toXmlString());
            }
            //end AXAL3.7.35
			SourceInfo sourceInfo =
				aTXLife
					.getTXLife()
					.getUserAuthRequestAndTXLifeRequest()
					.getTXLifeRequestAt(0)
					.getOLifE()
					.getSourceInfo();
			String backendId = null;
			if (sourceInfo != null) {
				backendId = sourceInfo.getFileControlID();
			}
			if (backendId == null) {
				String plan = aTXLife.getPrimaryHolding().getPolicy().getProductCode();	//NBA213
				if (plan == null) {
					throw new NbaBaseException("Backend ID and Plan both are missing");
				} else {
					//Find the file control id
					String companyCode = aTXLife.getPrimaryHolding().getPolicy().getCarrierCode();
					backendId = getBackendId(companyCode, plan);
					if (sourceInfo == null) {
						sourceInfo = new SourceInfo();
					}
					sourceInfo.setFileControlID(backendId);
					aTXLife
						.getTXLife()
						.getUserAuthRequestAndTXLifeRequest()
						.getTXLifeRequestAt(0)
						.getOLifE()
						.setSourceInfo(
						sourceInfo);
				}
			}
			aTXLife.setBusinessProcess(NbaConstants.PROC_NBP); //SPR2639
			Vector vctrErrors = null;
			if (NbaConfiguration.getInstance().isWebServiceDTDValidationON("NBP203WEBSERVICE")) {
				vctrErrors = aTXLife.getTXLife().getValidationErrors(false);
			}
			if (vctrErrors != null && vctrErrors.size() > 0) {
				txLife = createDTDErrorResponse(ele, vctrErrors, aTXLife);
			} else {
				//begin NBA050				
				aTXLife.setAccessIntent(NbaConstants.READ);
				//begin NBA213
				NbaUserVO userVo = aTXLife.getUser();
                UserCredential credential = new UserCredentialBase();
                credential.setPassword(userVo.getPassword());
                credential.setUserId(userVo.getUserID());
                Result result = UserSessionControllerFactory.create(credential);
                UserSessionController ctlr = (UserSessionController) result.getFirst();
                ServiceContext.create(ctlr);                
                NbaTXLife nbaTXLife;
                try {
                    nbaTXLife = NbaContractAccess.doContractInquiry(aTXLife);
                } catch (Throwable e) {
                    NbaLogFactory.getLogger(this.getClass()).logException(e);
                    return createExceptionResponse(e, aTXLife);
                }
                ServiceContext.dispose();	
				//end NBA213
				removeSensitiveData(nbaTXLife); //SPR2066
				
				//end NBA050 
				String TXLife = nbaTXLife.toXmlString();
				Document doc1 =
					(DocumentBuilderFactoryImpl.newInstance()).newDocumentBuilder().parse(
						new ByteArrayInputStream(TXLife.getBytes()));
				txLife = doc1.getDocumentElement();
			}
		} catch (NbaBaseException e) { //NBA103			
			e.forceFatalExceptionType();//NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103 
			return createExceptionResponse(e);  //AXAL3.7.35
		} catch (Throwable t) { //NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(t);//NBA103
			return createExceptionResponse(t); //AXAL3.7.35
		//NBA213 code deleted			
		}
		return txLife;	//NBA213
	}
	//SPR2066
	private void removeSensitiveData(NbaTXLife nbaTXLife) {
		removeUnderwriterNotes(nbaTXLife);
	}
	//SPR2066
	private void removeUnderwriterNotes(NbaTXLife nbaTXLife) {
		ArrayList notes = new ArrayList();
		for (int x = 0;x < nbaTXLife.getPrimaryHolding().getAttachmentCount(); x++) {
			Attachment attachment = nbaTXLife.getPrimaryHolding().getAttachmentAt(x);
			if (attachment != null && attachment.getAttachmentType() != NbaOliConstants.OLI_ATTACH_UNDRWRTNOTE) {
				notes.add(nbaTXLife.getPrimaryHolding().getAttachmentAt(x));
			}
		}
		nbaTXLife.getPrimaryHolding().setAttachment(notes);
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
	 * Creates a failure response for 203 transaction in case of DTD error.
	 * @param ele Element with the New Application submission data
	 * @param vctrErrors Vector containing the validation errors.
	 * @param aTXLife NbaTXLife value object containing the data recieved in the request
	 * @return Element containing the Error message	 
	 */
	protected Element createDTDErrorResponse(Element ele, Vector vctrErrors, NbaTXLife aTXLife) {
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
			TXLifeRequest txLifeRequest =
				aTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0);
			//set the values for trans ref guid, trans type, trans exe date, trans exe time and trans mode 
			transRefGUID.appendChild(xmlDoc.createTextNode(aTXLife.getTransRefGuid()));
			transType.setAttribute("tc", new Long(txLifeRequest.getTransType()).toString());
			transExeDate.appendChild(xmlDoc.createTextNode(formatDate(today)));
			//extract date from
			transExeTime.appendChild(xmlDoc.createTextNode(formatTime(today)));
			transMode.setAttribute("tc", new Long(txLifeRequest.getTransMode()).toString());
			//Create and add first level elements for TransResult in TXLifeResponse
			Element resultCode1 = xmlDoc.createElement("ResultCode");
			transResult1.appendChild(resultCode1);
			resultCode1.setAttribute("tc", "5");
			resultCode1.appendChild(xmlDoc.createTextNode("Failure"));
			//in case of exception, these elements are generated
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
			}
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
				logger = NbaLogFactory.getLogger(Nba203WebServiceBean.class.getName());
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
     * Creates a failure response for 203 transaction when an Exception occurs.
     * @param ele Element with the New Application submission data
     * @param vctrErrors Vector containing the validation errors.
     * @param aTXLife NbaTXLife value object containing the data recieved in the request
     * @return Element containing the Error message	 
     */
	//NBA213 New Method
    protected Element createExceptionResponse(Throwable exp, NbaTXLife aTXLife) {
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
            TXLifeRequest txLifeRequest = aTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0);
            //set the values for trans ref guid, trans type, trans exe date, trans exe time and trans mode 
            transRefGUID.appendChild(xmlDoc.createTextNode(aTXLife.getTransRefGuid()));
            transType.setAttribute("tc", new Long(txLifeRequest.getTransType()).toString());
            transExeDate.appendChild(xmlDoc.createTextNode(formatDate(today)));
            //extract date from
            transExeTime.appendChild(xmlDoc.createTextNode(formatTime(today)));
            transMode.setAttribute("tc", new Long(txLifeRequest.getTransMode()).toString());
            //Create and add first level elements for TransResult in TXLifeResponse
            Element resultCode1 = xmlDoc.createElement("ResultCode");
            transResult1.appendChild(resultCode1);
            resultCode1.setAttribute("tc", "5");
            resultCode1.appendChild(xmlDoc.createTextNode("Failure"));
            //in case of exception, these elements are generated
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
     * Format a Stack Trace for an Exception 
     * @param exp
     * @param sb
     */
    //NBA213 New Method
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
	 * Creates a failure response for 203 transaction in case of 'other' error.
	 * @param ele Element with the New Application submission data
	 * @param vctrErrors Vector containing the validation errors.
	 * @param aTXLife NbaTXLife value object containing the data recieved in the request
	 * @return Element containing the Error message	 
	 */
    //AXAL3.7.35 new method
    protected Element createExceptionResponse(Throwable exp) {
    	String guid = NbaUtils.getGUID();
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
			transType.setAttribute("tc", "203");
			transExeDate.appendChild(xmlDoc.createTextNode(formatDate(today)));
			//extract date from
			transExeTime.appendChild(xmlDoc.createTextNode(formatTime(today)));
			transMode.setAttribute("tc", "2");
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
    
    //APSL3313 New Method
	public Element getAuditInquiry(Element ele, String auditVersion) {
		Element txLife = null;
		try {
			String txLifeStr = DOM2String(ele.getOwnerDocument());
			StringBuffer txLifeStringBuffer = new StringBuffer(txLifeStr);
			String txLifeString = txLifeStringBuffer.toString();
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Incoming 203 before parse");
				getLogger().logDebug(txLifeString);
			}
            txLifeString = txLifeString.substring(txLifeString.indexOf("<TXLife"),txLifeString.indexOf("</TXLife>")+9);
            if (getLogger().isDebugEnabled()) {
            	getLogger().logDebug("Incoming 203 after string parse");
            	getLogger().logDebug(txLifeString);
            }
			NbaTXLife aTXLife = new NbaTXLife(txLifeString.toString());
            if (getLogger().isDebugEnabled()) {
            	getLogger().logDebug("Incoming 203 after XML parse");
            	getLogger().logDebug(aTXLife.toXmlString());
            }
			SourceInfo sourceInfo =
				aTXLife
					.getTXLife()
					.getUserAuthRequestAndTXLifeRequest()
					.getTXLifeRequestAt(0)
					.getOLifE()
					.getSourceInfo();
			String backendId = null;
			if (sourceInfo != null) {
				backendId = sourceInfo.getFileControlID();
			}
			if (backendId == null) {
				String plan = aTXLife.getPrimaryHolding().getPolicy().getProductCode();
				if (plan == null) {
					throw new NbaBaseException("Backend ID and Plan both are missing");
				} else {
					//Find the file control id
					String companyCode = aTXLife.getPrimaryHolding().getPolicy().getCarrierCode();
					backendId = getBackendId(companyCode, plan);
					if (sourceInfo == null) {
						sourceInfo = new SourceInfo();
					}
					sourceInfo.setFileControlID(backendId);
					aTXLife
						.getTXLife()
						.getUserAuthRequestAndTXLifeRequest()
						.getTXLifeRequestAt(0)
						.getOLifE()
						.setSourceInfo(
						sourceInfo);
				}
			}
			aTXLife.setBusinessProcess(NbaConstants.PROC_NBP);
			Vector vctrErrors = null;
			if (NbaConfiguration.getInstance().isWebServiceDTDValidationON("NBP203WEBSERVICE")) {
				vctrErrors = aTXLife.getTXLife().getValidationErrors(false);
			}
			if (vctrErrors != null && vctrErrors.size() > 0) {
				txLife = createDTDErrorResponse(ele, vctrErrors, aTXLife);
			} else {
				aTXLife.setAccessIntent(NbaConstants.READ);
				NbaUserVO userVo = aTXLife.getUser();
                UserCredential credential = new UserCredentialBase();
                credential.setPassword(userVo.getPassword());
                credential.setUserId(userVo.getUserID());
                Result result = UserSessionControllerFactory.create(credential);
                UserSessionController ctlr = (UserSessionController) result.getFirst();
                ServiceContext.create(ctlr);                
                NbaTXLife nbaTXLife;
                try {
                    nbaTXLife = NbaContractAccess.doAuditInquiry(aTXLife, Integer.parseInt(auditVersion));
                } catch (Throwable e) {
                    NbaLogFactory.getLogger(this.getClass()).logException(e);
                    return createExceptionResponse(e, aTXLife);
                }
                ServiceContext.dispose();	
				removeSensitiveData(nbaTXLife);
				String TXLife = nbaTXLife.toXmlString();
				Document doc1 =
					(DocumentBuilderFactoryImpl.newInstance()).newDocumentBuilder().parse(
						new ByteArrayInputStream(TXLife.getBytes()));
				txLife = doc1.getDocumentElement();
			}
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

    	
}
