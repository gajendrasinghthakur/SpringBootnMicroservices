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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

//import javax.ejb.SessionBean;

import com.csc.fs.Message;
import com.csc.fs.Result;
import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;

import com.csc.fsg.nba.process.product.ProductCache;
import com.csc.fsg.nba.vo.NbaTXLife;

/**
 * The NbaProductAccessFacade stateless session bean provides an interface 
 *  to the datastore to retrieve, insert, update and delete product data.  
 *  This class will, based on the primary datastore, invoke other classes to provide
 *  services.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA070</td><td>Version 3</td><td>Plans, Rates, and Funds I/O</td></tr>
 * <tr><td>SPR1706</td><td>Version 4</td><td>Sever Errors pertaining to Plan and Rates are generated</td></tr>
 * <tr><td>SPR1817</td><td>Version 4</td><td>Provide additional support for the 1201 webservice</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 * <tr><td>NBA208-26</td><td>Version 7</td><td>Remove synchronized keyword from getLogger() methods</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>NBA237</td><td>Version 8</td><td>Migrate Policy Product Transmittal XML1201 to 2.15.00</td></tr>
 * <tr><td>ALII767</td><td>PPFL Cache Enhancement</td><td>Performance Tuning</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaProductAccessFacadeBean implements NbaOliConstants {
	protected static NbaLogger logger = null;
	
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() { // NBA208-26
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaProductAccessFacadeBean.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaProductAccessFacadeBean could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * This method retrieves product data from the datastore.
	 * @param nbaTXLife the contract object containing the carrier code, policy product code,
	 * and an effective date to locate the product data.
	 * @return an NbaProduct with the newly retrieved data and values
	 */
	//NBA103
	//NBA237 changed method signature
	public AccelProduct doProductInquiry(NbaTXLife nbaTXLifeRequest) throws NbaBaseException { 
		try {//NBA103
			AccelProduct nbaProduct = new AccelProduct(); //NBA237
			// SPR3290 code deleted
	
			// sanity check that we received an NbaTXLife
			if (nbaTXLifeRequest == null || nbaTXLifeRequest.getTXLife() == null) {
				StringBuffer sb = new StringBuffer();
				sb.append("Information required to perform the product inquiry was missing: CarrierCode ProductCode");
				nbaProduct.setErrorCode(TC_RESINFO_ELEMENTMISSING);
				nbaProduct.addErrorMessage(sb.toString());
				return nbaProduct;
			}
	
			// validation check for required fields
			if (nbaTXLifeRequest.getCarrierCode() == null  || nbaTXLifeRequest.getProductCode() == null) {
				StringBuffer sb = new StringBuffer();
				sb.append("Information required to perform the product inquiry was missing:");
				if (nbaTXLifeRequest.getCarrierCode() == null) {
					sb.append(" CarrierCode");
				}
				if (nbaTXLifeRequest.getProductCode() == null) {
					sb.append(" ProductCode");
				}
				nbaProduct.setErrorCode(TC_RESINFO_ELEMENTMISSING);
				nbaProduct.addErrorMessage(sb.toString());
				return nbaProduct;
			}
			//begin NBA237
			if (NbaUtils.isNbaProductCacheSwitch()) {
				nbaProduct =	ProductCache.getInstance().getProductData(nbaTXLifeRequest); //ALII767
			}
			if (nbaProduct.getOLifE() == null ) {//ALII767			
				Result result = (AccelResult) ServiceHandler.invoke("NbaProductInquiryBP", ServiceContext.currentContext(), nbaTXLifeRequest);
				if (result.hasErrors()) {
					handleErrors(nbaProduct, result);
				} else {
					nbaProduct = (AccelProduct) result.getFirst();
				}
				if (nbaProduct.getOLifE() != null && NbaUtils.isNbaProductCacheSwitch()) { 
					ProductCache.getInstance().cacheProductData(nbaProduct);//ALII767
				}				
			}
			//end NBA237	
			return nbaProduct;
		} catch (Throwable t) {//NBA103
			NbaBaseException e = new NbaBaseException(t);//NBA103
			getLogger().logException(e);//NBA103
			throw e;//NBA103
		}
	}
	/**
	 * Handle errors
	 * @param nbaProduct
	 * @param result
	 */
	//NBA237 New Method
	protected void handleErrors(AccelProduct nbaProduct, Result result) {
		List list = result.getMessagesList();
		Message aMessage;
		List varList;
		String varText;
		for (int i = 0; i < list.size(); i++) {
			aMessage = (Message) list.get(i);
			varList = aMessage.getMessageVariableData();
			for (int j = 0; j < varList.size(); j++) {
				varText = (String) varList.get(j);
				nbaProduct.addErrorMessage(varText);
			}
		}
		nbaProduct.setErrorCode(TC_RESINFO_OBJECTNOTFOUND);
	}

	//NBA237 code deleted
}
