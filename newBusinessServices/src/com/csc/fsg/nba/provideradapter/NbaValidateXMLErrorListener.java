package com.csc.fsg.nba.provideradapter;

/*
 * ************************************************************** <BR>
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
 * ************************************************************** <BR>
 */
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * NbaValidateXMLErrorListener catches errors and adds them to a buffer so they
 * can be reported.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACN014</td><td>Version 4</td><td>121/1122 Migration</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see javax.xml.transform.ErrorListener#warning(javax.xml.transform.TransformerException)
 * @since New Business Accelerator - Version 4
 */
public class NbaValidateXMLErrorListener implements ErrorListener {
	public StringBuffer buffer = new StringBuffer();
	// Catch any errors or warnings from the XMLReader.
	class Handler extends DefaultHandler {
		public void warning(SAXParseException spe) throws SAXException {
			System.out.println("SAXParseException warning: " + spe.getMessage());
		}
		public void error(SAXParseException spe) throws SAXException {
			System.out.println("SAXParseException error: " + spe.getMessage());
		}
	}
	/** Catches warnings and adds them to the StringBuffer.
	 * @param TransformerException
	 */
	public void warning(TransformerException arg0)
		throws TransformerException {
		String msg = arg0.getMessage();
		buffer.append("FIELD MISSING: " + msg + "\n");
	}
	/** Catches errors and adds them to the StringBuffer.
	 * @param TransformerException
	 */
	public void error(TransformerException arg0) throws TransformerException {
		String msg = arg0.getMessage();
		buffer.append("ERROR: " + msg + "\n");

	}
	/** Catches fatalErrors and adds them to the StringBuffer.
	 * @param TransformerException
	 */
	public void fatalError(TransformerException arg0)
		throws TransformerException {
		String msg = arg0.getMessage();
		buffer.append("FATAL ERROR: " + msg + "\n");
	}
	/** Answers the StringBuffer 
	 * @return StringBuffer
	 */
	public StringBuffer getBuffer() {
		return buffer;
	}

	/** Sets the StringBuffer
	 * @param buffer 
	 */
	public void setBuffer(StringBuffer buffer) {
		this.buffer = buffer;
	}

}
