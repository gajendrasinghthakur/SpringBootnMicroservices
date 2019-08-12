package com.csc.fsg.nba.bean.accessors;

/*
 * ******************************************************************************* <BR> 
 * This program contains trade secrets and confidential
 * information which <BR> are proprietary to CSC Financial Services Group®. The use, <BR> reproduction, distribution or disclosure of this program, in
 * whole or in <BR> part, without the express written permission of CSC Financial Services <BR> Group is prohibited. This program is also an
 * unpublished work protected <BR> under the copyright laws of the United States of America and other <BR> countries. If this program becomes
 * published, the following notice shall <BR> apply: Property of Computer Sciences Corporation. <BR> Confidential. Not for publication. <BR> Copyright
 * (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved. <BR>
 * ******************************************************************************* <BR>
 */
import java.util.Vector;
import com.csc.fsg.nba.utility.XmlXsdValidator;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.tbf.xml.XmlValidationError;

public class NbaXmlValidatorWebServiceBean {

	/**
	 * This method validates the incoming XML using XSD and Breeze and 
	 * creates the response
	 * @param xmlString an XML
	 * @return String response
	*/
	public String validateXml(String xmlString) throws Exception {
		StringBuffer response = new StringBuffer();
		try {
			//--------------------Using XSD--------------------------------
			XmlXsdValidator validator = new XmlXsdValidator();
			response = validator.validateSchema(xmlString);
			//--------------------Using BREEZE-----------------------------
			response.append("\n<b>OTHER BREEZE VALIDATIONS</b>\n\n");
			NbaTXLife out = null;
			out = new NbaTXLife(xmlString.toString());
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
		} catch (Exception e) {
			response.append("An unhandled exception occurred:\n");
			response.append("\t" + e.getMessage() + "\n");
		} finally {
			if (response.length() == 0) {
				response.append("Validation successful.\n");
			}
		}
		return response.toString();
	}

}
