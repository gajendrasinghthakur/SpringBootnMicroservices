package com.csc.fsg.nba.correspondence;

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
 * 
 */
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNoValueException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;

/** 
 * 
 * This class is responsible for creating an XML extract for Document Solutions.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA013</td><td>Version 2</td><td>Correspondence System</td></tr> 
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA129</td><td>Version 5</td><td>xPression Integration</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */
public class NbaDocumentSolutionsExtract {
	protected final static String TAG_FORMS = "Forms"; //Root tag
	protected final static String TAG_COMPANY = "Company";
	protected final static String TAG_LOB = "LOB";
	protected final static String TAG_POLICYNUMBER = "PolicyNum";
	protected final static String TAG_SYSTEMDATE = "SystemDate";
	protected final static String TAG_LETTERS = "Form"; //NBA129
	protected final static String TAG_LETTERNAME = "Name";
	protected final static String TAG_LETTERVARIABLES = "Data";
	protected final static String EXTRACT_REQUIREDFIELD_PREFIX = "ER";

	protected Map letterVariables;
	protected String company;
	protected String lob;
	protected String policyNumber;
	protected String letter;
	private static NbaLogger logger = null; //NBA044
/**
 * The NbaDocumentSolutionsExtract constructor initializes all member variables.
 */
public NbaDocumentSolutionsExtract() {
    letterVariables = new HashMap();
}
/**
 * This method returns the Company value.
 * @return java.lang.String
 */
public String getCompany() {
    return company;
}
/**
 * This method returns the Letter name.
 * @return java.lang.String
 */
public String getLetter() {
    return letter;
}
/**
 * This method returns the Letter Lob value.
 * @return java.lang.String
 */
public String getLob() {
    return lob;
}
/**
 * This method returns the Policy Number.
 * @return java.lang.String
 */
public String getPolicyNumber() {
    return policyNumber;
}
/**
 * This method returns all the Letter variables.
 * @return java.util.Map
 */
public Map getVariables() {
    return letterVariables;
}
/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
    // NBA044 New Method
	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaDocumentSolutionsExtract.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaDocumentSolutionsExtract could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
/**
 * This method returns a value for a letter variable.
 * @return java.lang.String
 * @param variable A letter variable name.
 */
public String getVariableValue(String variable) {
    return (String) letterVariables.get(variable);
}
/**
 * This method is required only for testing <code>NbaDocumentSolutionsExtract</code>
 * 
 * @param args java.lang.String[]
 */
public static void main(String[] args) {
    try {
        NbaDocumentSolutionsExtract extractBuilder = new NbaDocumentSolutionsExtract();
        extractBuilder.setCompany("CSC");
        extractBuilder.setLob("UL");
        extractBuilder.setPolicyNumber("1023456789");
        extractBuilder.setLetter("Decline");
        extractBuilder.setVariableValue("FirstName", "Ryan");
        extractBuilder.setVariableValue("MiddleName", "Manny");
        extractBuilder.setVariableValue("LastName", "Jones");
        //NBA044 start
        if(getLogger().isDebugEnabled()){
        	getLogger().logDebug(extractBuilder.marshal());
        }
        //NBA044 end
    } catch (NbaBaseException e) {
        getLogger().logFatal(e); //NBA044
    }
}
/**
 * This method creates an XML extract specific to Document Solutions.
 * @return java.lang.String
 * @exception com.csc.fsg.nba.exception.NbaNoValueException This exception is thrown whenever required fields are
 * missing, or when an <code>IOException</code> has occcured.
 */
public String marshal() throws NbaBaseException {
    String marshalledXML = null;
    validateHeaderInformation();

    try {
        Element e = null;
        Element p = null;

        // Document (Xerces implementation only).
        Document xmldoc = new DocumentImpl();
        //Define a Root element.
        Element root = xmldoc.createElement(TAG_FORMS);

        e = xmldoc.createElementNS(null, TAG_COMPANY);
        e.appendChild(xmldoc.createTextNode(getCompany()));
        root.appendChild(e);

        e = xmldoc.createElementNS(null, TAG_LOB);
        e.appendChild(xmldoc.createTextNode(getLob()));
        root.appendChild(e);

        e = xmldoc.createElementNS(null, TAG_POLICYNUMBER);
        e.appendChild(xmldoc.createTextNode(getPolicyNumber()));
        root.appendChild(e);

        e = xmldoc.createElementNS(null, TAG_SYSTEMDATE);
        e.appendChild(xmldoc.createTextNode(null));
        root.appendChild(e);

        p = xmldoc.createElementNS(null, TAG_LETTERS);
        e = xmldoc.createElementNS(null, TAG_LETTERNAME);
        e.appendChild(xmldoc.createTextNode(getLetter()));
        p.appendChild(e);
        root.appendChild(p);

        p = xmldoc.createElementNS(null, TAG_LETTERVARIABLES);
        Object[] variables = letterVariables.keySet().toArray();
        for (int i = 0; i < variables.length; i++) {
            validateField((String) variables[i], (String) letterVariables.get(variables[i]));
            e = xmldoc.createElementNS(null, (String) variables[i]);
            e.appendChild(xmldoc.createTextNode((String) letterVariables.get(variables[i])));
            p.appendChild(e);
        }
        root.appendChild(p);

        xmldoc.appendChild(root);

        OutputFormat of = new OutputFormat("XML", "UTF-8", true);
        of.setIndenting(true);
        of.setIndent(1);

        StringWriter sw = new StringWriter();
        XMLSerializer serializer = new XMLSerializer(of);
        serializer.setOutputCharStream(sw);
        serializer.asDOMSerializer();
        serializer.serialize(xmldoc.getDocumentElement());

        marshalledXML = sw.getBuffer().toString();
    } catch (IOException e) {
        throw new NbaBaseException(e);
    }
    return marshalledXML;
}
/**
 * This method is used to set the company value.
 * @param newCompany java.lang.String
 */
public void setCompany(String newCompany) {
    company = newCompany;
}
/**
 * This method is used to the Letter name.
 * @param newLetter java.lang.String
 */
public void setLetter(String newLetter) {
    letter = newLetter;
}
/**
 * This method is used to set the Letter LOB value.
 * @param newLob java.lang.String
 */
public void setLob(String newLob) {
    lob = newLob;
}
/**
 * This method is used to set the Policy Number.
 * @param newPolicyNumber A policy Number.
 */
public void setPolicyNumber(String newPolicyNumber) {
    policyNumber = newPolicyNumber;
}
/**
 * This method is used to set the Letter variables.
 * @param variables java.util.Map
 */
public void setVariables(Map variables) {
    letterVariables = variables;
}
/**
 * This method is used to set a Letter variable value.
 * @param variable A letter Variable.
 * @param value A letter variable value.
 */
public void setVariableValue(String variable, String value) {
    letterVariables.put(variable, value);
}
/**
 * This method validates if the field is required. If so, it throws an exception
 * if the field value is null or invalid.
 * @param aField A field to validate
 * @param aValue A field value
 * @exception com.csc.fsg.nba.exception.NbaNoValueException Throw this exception if a required field's value is missing.
 */
protected void validateField(String aField, String aValue) throws com.csc.fsg.nba.exception.NbaNoValueException {
    if (aField.startsWith(EXTRACT_REQUIREDFIELD_PREFIX) && aValue.length() == 0) {
        throw new NbaNoValueException("Required field " + aField.substring(4) + " missing!");	//NBA050
    }
}
/**
 * This method is used validates requires fields.
 * @exception com.csc.fsg.nba.exception.NbaNoValueException This exception is thrown whenever required fields are
 * missing.
 */
protected void validateHeaderInformation() throws NbaNoValueException {
    if (company == null) {
        throw new NbaNoValueException("Required field Company missing!");
    }
    if (letter == null) {
        throw new NbaNoValueException("Required field Letter name missing!");
    }
    if (lob == null) {
        throw new NbaNoValueException("Required field Lob missing!");
    }
    if (policyNumber == null) {
        throw new NbaNoValueException("Required field Policy Number missing!");
    }
}
}
