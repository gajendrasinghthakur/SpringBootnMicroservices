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
import javax.ejb.SessionBean;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
/**
 * This is web service to accept the ACCORD 1123 result from Hooper Holmes
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA081</td><td>Version 3</td><td>Hooper Holmes</td></tr>
 * <tr><td>SPR1863</td><td>Version 3</td><td>Hooper Holmes Requirement TIF images are not displaying in the UW Workbench</td></tr> 
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.support.NbaUnderwriterWorkbenchViewHelper
 * @since New Business Accelerator - Version 3
 */
public class NbaHooperWebServiceBean implements SessionBean {
	private javax.ejb.SessionContext mySessionCtx;
	/**
	 * getSessionContext
	 * @return javax.ejb.SessionContext Session context
	 */
	public javax.ejb.SessionContext getSessionContext() {
		return mySessionCtx;
	}
	/**
	 * setSessionContext
	 * @param ctx Session context   
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
	 * Accepts the ACCORD 1123 result from Hooper Holmes and creates an XML to AWD_RIP.
	 * @param ele an Element object containing result from Hooper Holmes 
	 */
	//NBA103
	public void submitResult(Element ele) {
		try {//NBA103
			String path = NbaConfiguration.getInstance().getFileLocation("hooperRip");
			String resultXml = convertDOM2String(ele.getOwnerDocument());
			File xmlFile = new File(path + NbaUtils.getGUID() + ".xml"); //SPR1863
			OutputStream fileOut = new FileOutputStream(xmlFile);
			fileOut.write(resultXml.getBytes());
			fileOut.close();
		} catch (Throwable t) {//NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(t);//NBA103
		}
	}
	/**
	 * This method is used to convert the Document type value to String.
	 * @param doc Document object
	 * @return String a String representation of the Document object
	 */
	protected static String convertDOM2String(Document doc) throws java.io.IOException {
		StringWriter sw = new StringWriter();
		OutputFormat oFormatter = new OutputFormat("XML", null, false);
		XMLSerializer oSerializer = new XMLSerializer(sw, oFormatter);
		oSerializer.serialize(doc);
		return sw.toString();
	}
}
