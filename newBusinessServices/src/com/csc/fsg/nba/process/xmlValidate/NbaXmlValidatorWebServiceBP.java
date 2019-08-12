package com.csc.fsg.nba.process.xmlValidate;

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

import java.util.Vector;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.utility.XmlXsdValidator;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.webservice.client.NbaAxaServiceRequestor;
import com.tbf.xml.XmlValidationError;

/**
 * NbaXmlValidatorWebServiceBP is used for validate the xml.
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

public class NbaXmlValidatorWebServiceBP extends NewBusinessAccelBP {
     
	protected static NbaLogger logger = null;
	//NBLXA-2077 START
    static Source xsl = null;
    static TransformerFactory factory =null;
    static Transformer x = null;
	static {
		try {
			factory = TransformerFactory.newInstance();
			xsl = new StreamSource(NbaAxaServiceRequestor.class.getClassLoader().getResourceAsStream("XslRequirementValidateForValidation.xsl"));
			x = factory.newTransformer(xsl);
		} catch (Exception ex) {
			System.out.println("unable to load XslRequirementValidateForValidation XSLT");
		}
	}
    //NBLXA-2077 END
    /**
     * This class supports XmlValidation & return result.
     * 
     * @param input requires a TXLife request object
     * @return the string response.
     */
	public Result process(Object input) {
		Result result = new AccelResult();
		StringBuffer response = new StringBuffer();

		StringBuffer aTXLife = (StringBuffer) input;
		String nbaTXLifeString = null; //NBLXA-2077
		try {
                       //NBLXA-2077 BEGIN
			if (!NbaUtils.isBlankOrNull(aTXLife)
					&& NbaConstants.TRUE_STR.equalsIgnoreCase(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
							NbaConfigurationConstants.EMPTY_TAG_REMOVAL_SWITCH))) {
				nbaTXLifeString = aTXLife.toString();
				nbaTXLifeString = NbaUtils.getTxlifeWithEmptyTagRemXSLT(x, nbaTXLifeString);
				System.out.println("converted nbaTXLifeString >>>  "+nbaTXLifeString);
				//NBLXA-2077 END
				// --------------------Using XSD--------------------------------
				XmlXsdValidator validator = new XmlXsdValidator();
				response = validator.validateSchema(nbaTXLifeString);
				// --------------------Using BREEZE-----------------------------
				response.append("\n<b>OTHER BREEZE VALIDATIONS</b>\n\n");
				NbaTXLife out = null;
				out = new NbaTXLife(nbaTXLifeString);
				Vector errors = out.getTXLife().getValidationErrors();
				if (errors != null && errors.size() > 0) {
					for (int ndx = 0; ndx < errors.size(); ndx++) {
						XmlValidationError error = (XmlValidationError) errors.get(ndx);
						response.append("Error(" + ndx + "): ");
						if (error != null) {
							response.append(error.getErrorMessage());
						} else {
							response.append("A problem occurred retrieving the validation error.");
						}
						response.append("\n");
					}
				}
			}
		} catch (Exception e) {
			response.append("An unhandled exception occurred:\n");
			response.append("\t" + e.getMessage() + "\n");
		} finally {
			if (response.length() == 0) {
				response.append("Validation successful.\n");
			}
		} 
		return result.addResult(response.toString());
		
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
}
