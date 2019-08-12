package com.csc.fsg.nba.reinsurance.rgaschema;

/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which are
 * proprietary to CSC Financial Services Group®.  The use, reproduction,
 * distribution or disclosure of this program, in whole or in part, without
 * the express written permission of CSC Financial Services Group is prohibited.
 * This program is also an unpublished work protected under the copyright laws
 * of the United States of America and other countries.
 *
 * If this program becomes published, the following notice shall apply:
 *    Property of Computer Sciences Corporation.<BR>
 *    Confidential. Not for publication.<BR>
 *    Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;

/**
 * This is a wrapper class for the Cases XML classes for RGA.
 * It provides common functionality and convenience methods.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA038</td><td>Version 3</td><td>Reinsurance</td></tr>
 * <tr><td>AXAL3.7.32</td><td>Axa Life Phase 2</td><td>Reinsurance Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaRgaRequest implements NbaOliConstants, Serializable {
	protected ReinsuranceCases reinsuranceCases = null;//AXAL3.7.32
	/**
	 * Default NbaRgaRequest constructor.
	 */
	public NbaRgaRequest() {
		super();
		reinsuranceCases = new ReinsuranceCases();//AXAL3.7.32
	}
	/**
	 * NbaRgaRequest constructor which accepts Cases and creates
	 * new clone Cases. 
	 * @param cases the Cases object
	 */
	//AXAL3.7.32 Modified
	public NbaRgaRequest(ReinsuranceCases reinsuranceCases) throws NbaBaseException {
		try {
			String xml = null;

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			if (stream != null) {
				reinsuranceCases.marshal(stream);
				xml = stream.toString();
				stream.close();
				reinsuranceCases = ReinsuranceCases.unmarshal(new ByteArrayInputStream(xml.getBytes()));
			}
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.SOURCE_XML, e);
		}
	}
	/**
	 * NbaRgaRequest constructor which accepts an InputStream to be
	 * unmarshalled into a Cases object.
	 * @param inStream java.io.InputStream
	 */
	//AXAL3.7.32 Modified
	public NbaRgaRequest(InputStream inStream) throws NbaBaseException {
		try {
			reinsuranceCases = ReinsuranceCases.unmarshal(inStream);
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.SOURCE_XML, e);
		}
	}
	/**
	 * NbaRgaRequest constructor which accepts an XML string to be
	 * unmarshalled into a Cases object. 
	 * @param xml String
	 */
	//AXAL3.7.32 Modified
	public NbaRgaRequest(String xml) throws NbaBaseException {
		try {
			reinsuranceCases = ReinsuranceCases.unmarshal(new ByteArrayInputStream(xml.getBytes()));
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.SOURCE_XML, e);
		}
	}
	/**
	 * Convert the Cases objects to a string of xml.
	 * @return java.lang.String
	 */
	//AXAL3.7.32 Modified
	public String toXmlString() {
		String xml = null;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		if (stream != null) {
			reinsuranceCases.marshal(stream);
			xml = stream.toString();
			try {
				stream.close();
			} catch (java.io.IOException e) {
			}
		}
		return (xml);
	}
	/**
	 * Answers Cases object
	 * @return cases
	 */
	//AXAL3.7.32 Modified
	public ReinsuranceCases getReinsuranceCases() {
		return reinsuranceCases;
	}

	/**
	 * Sets Cases object
	 * @param cases
	 */
	//AXAL3.7.32 Modified
	public void setReinsuranceCases(ReinsuranceCases cases) {
		this.reinsuranceCases = cases;
	}

}
