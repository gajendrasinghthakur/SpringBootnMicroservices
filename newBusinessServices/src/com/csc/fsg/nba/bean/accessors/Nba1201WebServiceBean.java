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
import java.util.Calendar;
import java.util.Date;

import javax.ejb.SessionBean;

import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaNDC;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fs.dataobject.accel.product.AnnuityProduct;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fs.dataobject.accel.product.LifeProduct;
import com.csc.fs.dataobject.accel.product.LifeProductExtension;
import com.csc.fs.dataobject.accel.product.LifeProductOrAnnuityProduct;
import com.csc.fs.dataobject.accel.product.OLifE;
import com.csc.fs.dataobject.accel.product.OLifEExtension;
import com.csc.fs.dataobject.accel.product.PaymentAssembly;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fs.dataobject.accel.product.PolicyProduct;
import com.csc.fs.dataobject.accel.product.PolicyProductExtension;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.SourceInfo;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
import com.tbf.xml.XmlValidationError;

/**
 * The Nba1201WebServiceBean stateless session bean provides a Web Service interface 
 * to nbA for transmitting product data in an ACORD 1201 PolicyProduct Transmittal
 * transaction.  
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA070</td><td>Version 3</td><td>Plans, Rates, and Funds I/O</td></tr>
 * <tr><td>SPR1817</td><td>Version 4</td><td>Provide additional support for the 1201 webservice</td></tr>
 * <tr><td>SPR1911</td><td>Version 4</td><td>Allow a product to be delivered multiple times</td></tr>
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA103</td><td>Version 3</td><td>Logging</td></tr>
 * <tr><td>SPR1915</td><td>Version 4</td><td>Vantage AnnuityProduct object must be present in order to perform validation on PremType</td></tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>General Code Clean Up Issues for Version 6</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>NBA237</td><td>Version 8</td><td>Migrate Policy Product Transmittal XML1201 to 2.15.00</td></tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class Nba1201WebServiceBean implements SessionBean, NbaOliConstants {
	protected static NbaLogger logger = null;
	// SPR3290 code deleted

	private static String MODE_PREM_TABLE_DEFAULT = "000";  //NBA104

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
	 * Returns an instance of the NbaProductAccessFacade. 
	 */
	// SPR1911 New Method
	protected NbaProductAccessFacadeBean getProductEJB() throws Exception {
		return new NbaProductAccessFacadeBean();
	}

	/**
	 * The web services primary entry point for receiving an ACORD 1201 transaction.
	 * 
	 * @param str an ACORD 1201 request
	 * @return String an ACORD 1201 response
	 */
	// SPR1817 New Method
	public String productUpdate(String str) {
		NbaNDC.setNDC("Nba1201WebServiceBean", "productUpdate"); //NBA103	
		try{	//NBA103
			Element ele = productUpdateElement(str);
			str = DOM2String(ele);
		} catch (Throwable t){//NBA103
			getLogger().logException(t);//NBA103
		}
		NbaNDC.removeNDC(); //NBA103
		return str;
	}
	
	/**
	 * This method validates the incoming request, updates the nbA product information,
	 * and creates a response. 
	 * 
	 * @param txLifeStr an ACORD 1201 request
	 * @return Element an ACORD 1201 response
	 */
	// SPR1817 method name and parameter change
	protected Element productUpdateElement(String txLifeStr){
		// SPR1817 code deleted
		Element ele = null;
/*		NbaTXLife aTXLife = null;
		UserAuthRequestAndTXLifeRequest uatxlreq = null;

		// Verify that we got an TXLife object as input
		// If not, try to give the caller an intelligible response.
		try {
			// SPR1817 code deleted
			StringBuffer txLifeString = new StringBuffer(txLifeStr);
	
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Incoming 1201 PolicyProduct transmittal:\n" + txLifeString.toString());
			}
			
			int firstIndex = 0;  //SPR1817
			int lastIndex = txLifeStr.indexOf("<TXLife");
			if (lastIndex < 0) {  //SPR1817
				throw new NbaBaseException("Invalid or missing TXLife element: could not find starting TXLife tag");
			} else if (lastIndex > 0) {  //SPR1817
				txLifeString.delete(firstIndex, lastIndex - 1);  //SPR1817
			}
			// SPR1817 code deleted
			
			firstIndex = txLifeString.toString().indexOf("</TXLife>");
			lastIndex = txLifeString.length();
			if (firstIndex < 0 || lastIndex < 0 || firstIndex > lastIndex) {
				throw new NbaBaseException("Invalid or missing TXLife element: could not find ending TXLife tag");
			}
			txLifeString.delete(firstIndex + 9, lastIndex);

			aTXLife = new NbaTXLife(txLifeString.toString());
			uatxlreq = aTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest();
			if (uatxlreq == null) {
				throw new NbaBaseException("TXLife contents missing or invalid.");
			}
		} catch(Exception exp) {
			getLogger().logException(exp); //NBA103
			NbaTXLife response = new NbaTXLife();
			response.setTXLife(createTXLifeResponseAsError(exp));  //SPR1915
			String txLifeResponse = response.toXmlString();
			try {
				Document doc1 = (DocumentBuilderFactoryImpl.newInstance()).newDocumentBuilder().parse(new ByteArrayInputStream(txLifeResponse.getBytes()));
				ele = doc1.getDocumentElement();
			} catch (Exception e) {
				getLogger().logException("Fatal error during a 1201 PolicyProduct transmittal.  Could not return information back to caller.", e); //NBA103
			}
			return ele;
		}

		// Start building the response
		NbaTXLife nbaTXLife = null;
		TXLifeRequest txrequest = null;
		TXLifeResponse txresponse = null;
		TransResult transResult = null;
					
		int transCount = uatxlreq.getTXLifeRequestCount();
		for (int i=0; i<transCount; i++) {
			txrequest = uatxlreq.getTXLifeRequestAt(i);
			//NBA237 Code commented
			//transResult = performValidationAndMarkup(txrequest);

			// is this the first txLifeRequest?			
			if (nbaTXLife == null) {
				nbaTXLife = new NbaTXLife();
				nbaTXLife.setTXLife(NbaTXLife.createTXLifeResponse(aTXLife.getTXLife()));
				txresponse = nbaTXLife.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0); 
			} else {
				txresponse = NbaTXLife.createTXLifeResponse(txrequest);
				nbaTXLife.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().addTXLifeResponse(txresponse);
			}

			if (transResult != null) {
				txresponse.setTransResult(transResult);
				continue;
			}
			//NBA237- replaced NbaProduct by AccelProduct
			AccelProduct product = new AccelProduct(txrequest.getOLifE());

			// Now try to save the product.
			try {
				// SPR1911 deleted code
				//product = getProductEJB().doProductUpdate(product);  // SPR1911

				// fill in the results for the response
				txresponse.setTransResult(createTransResult(product));
			} catch(Exception exp) {
				getLogger().logException(exp); //NBA103
				// fill in the results with the exception
				txresponse.setTransResult(createTransResult(exp));
			}			
		}

		// Pass the response back to the caller
		try {
			String txLifeResponse = nbaTXLife.toXmlString();
			if (getLogger().isDebugEnabled()) {  //SPR2992
				getLogger().logDebug("Response 1201 PolicyProduct transmittal:\n" + txLifeResponse);  //SPR2992
			}  //SPR2992
			Document doc1 = (DocumentBuilderFactoryImpl.newInstance()).newDocumentBuilder().parse(new ByteArrayInputStream(txLifeResponse.getBytes()));
			ele = doc1.getDocumentElement();
		} catch (Exception e) {
			getLogger().logException("Fatal error during a 1201 PolicyProduct transmittal.  Could not return information back to caller.", e); //NBA103
		}*/
		return ele;
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(Nba1201WebServiceBean.class.getName());  //SPR1817
			} catch (Exception e) {
				NbaBootLogger.log("Nba1201WebServiceBean could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
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
	 * Creates a new <code>TransResult</code> object with the appropriate
	 * error information retrieved from the <code>NbaProduct</code>.
	 * 
	 * @param product an nbA wrapper for the PolicyProduct
	 * @return com.csc.fsg.nba.vo.txlife.TransResult
	 */
	protected TransResult createTransResult(AccelProduct product) {
		TransResult transResult = new TransResult();

		if (product.getErrorCode() == TC_RESCODE_SUCCESS) {
			transResult.setResultCode(TC_RESCODE_SUCCESS);
		} else {
			transResult.setResultCode(TC_RESCODE_FAILURE);
			int count = product.getErrorMessageCount();
			for (int i=0; i<count; i++) {
				ResultInfo resultInfo = new ResultInfo();
				resultInfo.setResultInfoCode(product.getErrorCode());
				resultInfo.setResultInfoDesc(product.getErrorMessageAt(i));
				transResult.addResultInfo(resultInfo);
			}
		}
		
		return transResult;
	}

	/**
	 * Creates a new <code>TransResult</code> object with the appropriate
	 * error information retrieved from an exception.
	 * 
	 * @param e an exception for an unexpected problem
	 * @return com.csc.fsg.nba.vo.txlife.TransResult
	 */
	protected TransResult createTransResult(Exception e) {
		return createTransResult(null, TC_RESINFO_GENERALERROR, e.getMessage());
	}

	/**
	 * Creates a new <code>TransResult</code> object or appends to an existing
	 * one.  A new <code>ResultInfo</code> is created and applied with the
	 * appropriate error information received from the parameters.
	 * 
	 * @param transResult existing result to append to
	 * @param resultCode error code
	 * @param resultDesc description of error
	 * @return com.csc.fsg.nba.vo.txlife.TransResult
	 */
	protected TransResult createTransResult(TransResult transResult, long resultCode, String resultDesc) {
		if (transResult == null) {
			transResult = new TransResult();
			transResult.setResultCode(TC_RESCODE_FAILURE);
		}
		
		ResultInfo resultInfo = new ResultInfo();
		resultInfo.setResultInfoCode(resultCode);
		resultInfo.setResultInfoDesc(resultDesc);
		transResult.addResultInfo(resultInfo);
		
		return transResult;
	}

	/**
	 * Creates a new <code>TXLife</code> object and initializes the
	 * <code>TXLifeResponse</code> objects with the exception information.
	 * 
	 * @param e an exception for an unexpected problem
	 * @return com.csc.fsg.nba.vo.txlife.TXLife
	 */
	protected TXLife createTXLifeResponseAsError(Exception e) { //SPR1915

		UserAuthResponseAndTXLifeResponseAndTXLifeNotify response = new UserAuthResponseAndTXLifeResponseAndTXLifeNotify();
		if (response == null) {
			return (null);
		}

		TXLifeResponse txResponse = new TXLifeResponse();
		txResponse.setTransRefGUID(NbaUtils.getGUID());
		txResponse.setTransType(TC_TYPE_POLICYPRODUCTTRANS);
		Date serverDate = Calendar.getInstance().getTime();
		txResponse.setTransExeDate(serverDate);
		txResponse.setTransExeTime(new NbaTime(serverDate));
		response.addTXLifeResponse(txResponse);

		TransResult transResult = new TransResult();
		transResult.setResultCode(TC_RESCODE_FAILURE);
		ResultInfo resultInfo = new ResultInfo();
		resultInfo.setResultInfoCode(TC_RESINFO_ELEMENTINVALID);
		resultInfo.setResultInfoDesc(e.getMessage());
		transResult.addResultInfo(resultInfo);
		txResponse.setTransResult(transResult);

		TXLife txLifeResponse = new TXLife();
		txLifeResponse.setUserAuthResponseAndTXLifeResponseAndTXLifeNotify(response);

		return (txLifeResponse);
	}
	
	/**
	 * Perform validation and markup to the incoming transaction.
	 * 
	 * Validation performed:
	 *    - Only one PolicyProduct per request transaction
	 *    - PolicyProduct.CarrierCode is required
	 * 	  - PolicyProduct.ProductCode is required
	 *    - Backend system is required
	 *        * Preferred location is PolicyProductExtension.CarrierAdminSystem
	 *        * Alternate location is SourceInfo.FileControlID
	 *    - AnnuityProduct/LifeProduct is required
	 * 
	 * Markup performed:
	 *    - PolicyProduct.SaleEffectiveDate/SaleExpirationDate
	 *        * SaleEffectiveDate defaults to 1900-01-01 if not present
	 *        * SaleExpirationDate defaults to 9999-12-31 if not present
	 *    - If Backend system is only found in SourceInfo.FileControlID, then it
	 *        is duplicated in the PolicyProductExtension.CarrierAdminSystem.
	 *    - PremType is assumed to be fixed for Life Products if not present
	 *    - PremType is assumed to be flexible for Annuity Products if not present 
	 * 
	 * @param request a 1201 request transaction 
	 * @return null if successful, otherwise a TransResult with errors
	 */
	protected TransResult performValidationAndMarkup(com.csc.fs.dataobject.accel.product.TXLifeRequest request){
		
		// check transaction type
		if (request.getTransType() != TC_TYPE_POLICYPRODUCTTRANS) {
			return createTransResult(null, TC_RESINFO_UNSUPPSERVICE, "This WebService only supports a PolicyProduct Transmittal (1201) transaction.");
		}

		TransResult transResult = null;
		OLifE olife = request.getOLifE();
		// begin SPR1817
		if (olife == null) {
			return createTransResult(null, TC_RESINFO_GENERALDATAERR, "Transaction does not include any OLifE data.");
		}
		// end SPR1817
		
		if (olife.getPolicyProductCount() != 1) {
			transResult = createTransResult(transResult, TC_RESINFO_GENERALDATAERR, "Only one PolicyProduct instance is accepted in a transaction.");  //SPR1817
		}
		PolicyProduct policyproduct = olife.getPolicyProductAt(0);
		if (!policyproduct.hasCarrierCode()) {
			transResult = createTransResult(transResult, TC_RESINFO_GENERALDATAERR, "PolicyProduct.CarrierCode is required for this transaction.");  //SPR1817
		}
		if (!policyproduct.hasProductCode()) {
			transResult = createTransResult(transResult, TC_RESINFO_GENERALDATAERR, "PolicyProduct.ProductCode is required for this transaction.");  //SPR1817
		}

		// default the effective and expiration dates if not previously set
		if (!policyproduct.hasSaleEffectiveDate()) {
			policyproduct.setSaleEffectiveDate("1900-01-01");
		}
		if (!policyproduct.hasSaleExpirationDate()) {
			policyproduct.setSaleExpirationDate("9999-12-31");
		}
		
		// check for backend system id
		//NBA237 Changed reference from NBAUtils to AccelProduct
		PolicyProductExtension ppext = AccelProduct.getFirstPolicyProductExtension(policyproduct);
		if ((ppext != null) && (ppext.hasCarrierAdminSystem())) {
			if (ppext.getCarrierAdminSystem().equalsIgnoreCase("CSC_CLife")) {
				ppext.setCarrierAdminSystem(NbaConstants.SYST_CYBERLIFE);
			} else if (ppext.getCarrierAdminSystem().equalsIgnoreCase("CSC_Vantage")) {
				ppext.setCarrierAdminSystem(NbaConstants.SYST_VANTAGE);
			}
		} else {			// if no backend system, check the previous location for backend system
			SourceInfo info = null; 
			//NBA237 Commented Code
			//info = olife.getSourceInfo();
			if ((info != null) && (info.hasFileControlID())) {
				// do we have an existing extension to update the backend system
				if (ppext != null) {
					ppext.setCarrierAdminSystem(info.getFileControlID());
				} else {	// no existing extension found so create one
					//NBA237 Code commented
					/*com.csc.fs.dataobject.accel.product.OLifEExtension olifeext = NbaTXLife.createOLifEExtension(EXTCODE_POLICYPRODUCT);
					
					if (olifeext != null && olifeext.getPolicyProductExtension() != null) {
						olifeext.getPolicyProductExtension().setCarrierAdminSystem(info.getFileControlID());
						policyproduct.addOLifEExtension(olifeext);
					}*/
				}
			} else {
				transResult = createTransResult(transResult, TC_RESINFO_GENERALDATAERR, "The Carrier Admin System is required for this transaction.");  //SPR1817
			}
		}

		transResult = establishPremType(transResult, policyproduct); //SPR1915

		// begin NBA104
		// for CyberLife plans - default the PaymentAssembly.ModePremTableIdentity if not present
		if (ppext != null && NbaConstants.SYST_CYBERLIFE.equalsIgnoreCase(ppext.getCarrierAdminSystem())) {
			int count = ppext.getPaymentAssemblyCount();
			for (int i=0; i < count; i++) {
				PaymentAssembly paymentAssembly = ppext.getPaymentAssemblyAt(i);
				if (paymentAssembly != null && !paymentAssembly.hasModePremTableIdentity()) {
					paymentAssembly.setModePremTableIdentity(MODE_PREM_TABLE_DEFAULT);
				}
			}
			// create a PaymentAssembly if necessary
			if (count == 0) {
				PaymentAssembly paymentAssembly = new PaymentAssembly();
				paymentAssembly.setModePremTableIdentity(MODE_PREM_TABLE_DEFAULT);
				paymentAssembly.setId("PaymentAssembly_1");
				ppext.addPaymentAssembly(paymentAssembly);
			}
		} 
		// end NBA104

		// begin SPR1817
		// perform XML validation
		try {
			if (NbaConfiguration.getInstance().isWebServiceDTDValidationON(NbaConfigurationConstants.NBA1201WEBSERVICE)) { //ACN012
				//NBA237 Code commented
				//java.util.Vector v = olife.getValidationErrors();
				java.util.Vector v = null;
				if (v != null) {
					for (int ndx = 0; ndx < v.size(); ndx++)
					{
						XmlValidationError error = (XmlValidationError)v.get(ndx);
						String errMsg = null;
						if (error != null) {
							errMsg = error.getErrorMessage();
							transResult = createTransResult(transResult, TC_RESINFO_GENERALDATAERR, errMsg);
						} else {
							errMsg = "A problem occurred retrieving the validation error.";
						}
						if (getLogger().isDebugEnabled()) {
							StringBuffer sb = new StringBuffer("\tError(");
							sb.append(ndx);
							sb.append("): ");
							sb.append(errMsg);
							sb.append("\n");
							getLogger().logDebug(sb.toString());
						}
					}
				}
			}
		} catch (NbaBaseException nbe) {
			getLogger().logException("NbaConfiguration error, XML validation not performed on 1201 transaction", nbe); //NBA103
		}
		// end SPR1817
		
		// begin SPR1911
		// if we don't have any errors, remove the existing product if necessary
		if (transResult == null) {
			transResult = removeExistingProduct(policyproduct);
		}
		// end SPR1911
		
		return transResult;
	}
	
	/**
	 * Establish PremType
	 * @param transResult transResult to udpate if errors need to be returned
	 * @param policyproduct product info to update
	 * @return supplied TransResult if successful, otherwise a TransResult with errors
	 */
	//SPR1915 - new method
	private TransResult establishPremType(TransResult transResult, PolicyProduct policyproduct) {
		LifeProductOrAnnuityProduct product = policyproduct.getLifeProductOrAnnuityProduct();
		if (product == null || !product.hasContents()) {
			product = new LifeProductOrAnnuityProduct();
			if (policyproduct.getLineOfBusiness() == OLI_LINEBUS_LIFE) {
				product.setLifeProduct(new LifeProduct());
			} else if (policyproduct.getLineOfBusiness() == OLI_LINEBUS_ANNUITY) {
				product.setAnnuityProduct(new AnnuityProduct());
			} else {
				transResult = createTransResult(transResult, TC_RESINFO_GENERALDATAERR, "Unsupported line of business: " + policyproduct.getLineOfBusiness());
			}
			policyproduct.setLifeProductOrAnnuityProduct(product);
		}
		
		if (policyproduct.getLineOfBusiness() == OLI_LINEBUS_LIFE) {
			LifeProductExtension lifeExt = AccelProduct.getFirstLifeProductExtension(product.getLifeProduct()); //NBA237
			if (lifeExt != null) {
				if (lifeExt.hasPremType() == false) {
					lifeExt.setPremType(OLI_ANNPREM_FIXED);
				}
			} else {
				//NBA237 Changed reference from NbaTXlife to AccelProduct
				OLifEExtension olifeext = AccelProduct.createOLifEExtension(EXTCODE_LIFEPRODUCT);
				if (olifeext != null && olifeext.getLifeProductExtension() != null) {
					olifeext.getLifeProductExtension().setPremType(OLI_ANNPREM_FIXED);
					product.getLifeProduct().addOLifEExtension(olifeext);
				} else {
					transResult = createTransResult(transResult, TC_RESINFO_GENERALDATAERR, "Could not create OlifeExtension."); 
				}
			}
		} else if (policyproduct.getLineOfBusiness() == OLI_LINEBUS_ANNUITY) {
			if (policyproduct.getLifeProductOrAnnuityProduct().getAnnuityProduct().hasPremType() == false) {
				policyproduct.getLifeProductOrAnnuityProduct().getAnnuityProduct().setPremType(OLI_ANNPREM_FLEX);
			}
		} else {
			transResult = createTransResult(transResult, TC_RESINFO_GENERALDATAERR, "Unsupported line of business: " + policyproduct.getLineOfBusiness()); 
		}
		return transResult;
	}

	/**
	 * Receives a document element and converts it into a String
	 * @param doc Document which contains the element to be converted into String
	 * @return String the string form of Element recieved as parameter
	 */
	protected static String DOM2String(Element ele) throws java.io.IOException {

		StringWriter sw = new StringWriter();
		OutputFormat oFormatter = new OutputFormat("XML", null, false);

		XMLSerializer oSerializer = new XMLSerializer(sw, oFormatter);
		oSerializer.serialize(ele);
		return sw.toString();
	}

	/**
	 * Removes an existing product from the database if it already exists.
	 * @param policyproduct product info to remove
	 */
	// SPR1911 New Method
	protected TransResult removeExistingProduct(PolicyProduct policyproduct) {

		try {
			TransResult transResult = null;		
			NbaTXLife nbaTXLife = new NbaTXLife();
			
			nbaTXLife.setTXLife(createTXLifeResponse(policyproduct));
			AccelProduct nbaprod = getProductEJB().doProductInquiry(nbaTXLife);
			if (nbaprod.getErrorCode() == TC_RESCODE_SUCCESS) {
				nbaprod.toXmlString();  // Load complete product
				nbaprod.getOLifE().setActionDelete();
				nbaprod.setErrorCode(TC_RESCODE_FAILURE);
				//NBA237 Code commented
				//nbaprod = getProductEJB().doProductUpdate(nbaprod);
				if (nbaprod.getErrorCode() != TC_RESCODE_SUCCESS) {
					int count = nbaprod.getErrorMessageCount();
					for (int i=0; i<count; i++) {
						transResult = createTransResult(transResult, TC_RESINFO_DUPLICATEOBJ, nbaprod.getErrorMessageAt(i));
					}
				}
			}
	
			return transResult;
		} catch(Exception exp) {
			getLogger().logException(exp); //nba103
			return createTransResult(exp);
		}			
	}
	
	/**
	 * Creates a TXLifeResponse for a PolicyProduct inquiry.
	 * @param policyproduct product info to remove
	 */
	// SPR1911 New Method
	protected TXLife createTXLifeResponse(PolicyProduct policyproduct) {
		TXLife txlife = new TXLife();
		UserAuthResponseAndTXLifeResponseAndTXLifeNotify response = new UserAuthResponseAndTXLifeResponseAndTXLifeNotify();
		TXLifeResponse txResponse = new TXLifeResponse();
		OLifE olife = new OLifE();
		//NBA237 Changed from NBAUtils to AccelProduct
		PolicyProductExtension ppext = AccelProduct.getFirstPolicyProductExtension(policyproduct);
		SourceInfo srcInfo = new SourceInfo();
		srcInfo.setFileControlID(ppext.getCarrierAdminSystem());
		//NBA237 Code commented
		//olife.setSourceInfo(srcInfo);
		
		Holding holding = new Holding();
		holding.setId("Holding_1");
		Policy policy = new Policy();
		policy.setCarrierCode(policyproduct.getCarrierCode());
		policy.setProductCode(policyproduct.getProductCode());
		ApplicationInfo appInfo = new ApplicationInfo();
		appInfo.setSignedDate(policyproduct.getSaleEffectiveDate());

		policy.setApplicationInfo(appInfo);
		holding.setPolicy(policy);
		//NBA27 Code commented
		//olife.addHolding(holding);
		//txResponse.setOLifE(olife);
		response.addTXLifeResponse(txResponse);
		txlife.setUserAuthResponseAndTXLifeResponseAndTXLifeNotify(response);
		return txlife;
	}
}
