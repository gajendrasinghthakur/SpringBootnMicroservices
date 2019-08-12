package com.csc.fsg.nba.process.tx1122;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.assembler.tx1122.TX1122Assembler;
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
 * TX1122WebServiceBP is used for SubmitRequirementResult.
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

public class TX1122WebServiceBP extends NewBusinessAccelBP {
     
	protected static NbaLogger logger = null;
	protected static NbaLogger failureLogger = null; //NBLXA-1884

	/**
     * This method supports do contract inquiry and call assembler & return result.
     * 
     * @param input requires a TXLife request object
     * @return the TXLife response is returned in the Resu-=========lt.
     */
	public Result process(Object input) {
		Result result = new AccelResult();
		String GUID = null;
		NbaTXLife aTXLife = (NbaTXLife) input;
		String providerID = aTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getUserAuthRequest().getVendorApp().getVendorName().getPCDATA();
    	
		// Intercept the incoming requirement result
		Vector errors = null;
		try {
			if (NbaConfiguration.getInstance().isWebServiceDTDValidationON("NBAPROVIDERINDEXWEBSERVICE")) {
				errors = aTXLife.getTXLife().getValidationErrors(false);
			}
			if (errors != null && errors.size() > 0) {
				NbaTXLife txlife=createResponse(aTXLife, errors); //NBLXA-1884
				getWebServiceFailureLogger().logDebug("TX1122WebServiceBP Finished Request: " + aTXLife.toXmlString()); //NBLXA-1884
				getWebServiceFailureLogger().logDebug("TX1122WebServiceBP Response: " + txlife.toXmlString()); //NBLXA-1884
				return result.addResult(txlife); //NBLXA-1884
			}
			String path = null;
			try {
				path = NbaConfiguration.getInstance().getFileLocation(providerID + "Rip");
			} catch (Exception exp) {
				getLogger().logException(exp);
				errors = new Vector();
				errors.add(buildExceptionMessage(exp));
				return result.addResult(createResponse(aTXLife, errors));
			}
			// Deliver the transformed provider requirement result to DocumentInput
			GUID = aTXLife.getTransRefGuid();
			SimpleDateFormat GUID_SDF = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			String datetime = GUID_SDF.format(new Date());
			File xmlFile = new File(path, GUID + ".xml");
			if (xmlFile.exists()) {
				xmlFile = new File(path, GUID + "_" + datetime + ".xml");
			}
			OutputStream fileOut = new FileOutputStream(xmlFile);
			String output=aTXLife.toXmlString();
			  if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("TX1122WebServiceBP Finished Request: " + output);
				}
			fileOut.write(output.getBytes());
			fileOut.close();
		} catch (Exception exp) {
			NbaLogFactory.getLogger(this.getClass()).logException(exp);
			exp.printStackTrace();
			errors = new Vector();
			errors.add(buildExceptionMessage(exp));
			return result.addResult(createResponse(aTXLife, errors));
		}
		// return createResponse(ele, null);
		return result.addResult(createResponse(aTXLife, null));
	} 
	
	
	
	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(TX1122Assembler.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("TX1122Assembler could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}	
	
   
    /**
	 * Uses the Xerces API create a response.
	 * @param ele Element that is the incoming XML
	 * @param txLife NbaTXLife that is the formatted incoming XML
	 * @param errors Vector containing error messages encountered while processing the incoming XML
	 * @return Element containing the response	 
	 */
      
	protected NbaTXLife createResponse(NbaTXLife atxLife, Vector errors) {
		NbaTXLife nbaTXLife = new NbaTXLife();
		try {
			nbaTXLife.setTXLife(new TXLife());
			UserAuthResponseAndTXLifeResponseAndTXLifeNotify ua = new UserAuthResponseAndTXLifeResponseAndTXLifeNotify();
			nbaTXLife.getTXLife().setUserAuthResponseAndTXLifeResponseAndTXLifeNotify(ua);
			ua.setUserAuthResponse(new UserAuthResponse());
			ua.getUserAuthResponse().setSvrDate(new Date());
			ua.getUserAuthResponse().setSvrTime(new NbaTime());
			TXLifeResponse tXLifeResponse = new TXLifeResponse();
			ua.addTXLifeResponse(tXLifeResponse);
			tXLifeResponse.setTransRefGUID(atxLife.getTransRefGuid());
			tXLifeResponse.setTransType(atxLife.getTransType());
			tXLifeResponse.setTransExeDate(new Date());
			tXLifeResponse.setTransExeTime(new NbaTime());
			tXLifeResponse.setTransMode(atxLife.getTransMode());
			TransResult transResult = new TransResult();
			tXLifeResponse.setTransResult(transResult);
			if (errors != null && errors.size() > 0) {
				transResult.setResultCode(200);
				ResultInfo reInfo = new ResultInfo();
				reInfo.setResultInfoCode(200);
				ArrayList aList = new ArrayList();
				XmlValidationError xmlError;
				for (int i = 0; i < errors.size(); i++) {
					if (errors.get(i) instanceof XmlValidationError) {
						xmlError = (XmlValidationError) errors.get(i);
						reInfo.setResultInfoDesc("DTD validation error : " + xmlError.getErrorMessage());
						aList.add(reInfo);
					} else if (errors.get(i) instanceof String) {
						reInfo.setResultInfoDesc((String) errors.get(i));
						aList.add(reInfo);
					}
				}
				transResult.setResultInfo(aList);
			} else {
				transResult.setResultCode(NbaOliConstants.TC_RESCODE_SUCCESS);
			
			}
			return nbaTXLife;
		} catch (Exception exp) {
          System.out.println("Exception="+exp);
		}

		return nbaTXLife;
	}
    
    protected String buildExceptionMessage(Exception exp) {
		if (exp.getMessage() != null) {
			return exp.getMessage();
		}
		StackTraceElement trace = exp.getStackTrace()[0];
		StringBuffer sb = new StringBuffer();
		sb.append(exp.getClass().getName());
		sb.append(" ");
		sb.append(trace.toString());
		return sb.toString();
    }    
     // Begin NBLXA-1884
    private static NbaLogger getWebServiceFailureLogger() {
		if (failureLogger == null) {
			try {
				failureLogger = NbaLogFactory.getLogger(TX1122WebServiceBP.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("TX1122WebServiceBP could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return failureLogger;
	}
    // End NBLXA-1884
    
}