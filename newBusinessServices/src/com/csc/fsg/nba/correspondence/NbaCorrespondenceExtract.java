package com.csc.fsg.nba.correspondence;

/**
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
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.csc.fsg.nba.correspondence.docprintschema_extract.Correspondence;
import com.csc.fsg.nba.correspondence.docprintschema_extract.Data;
import com.csc.fsg.nba.correspondence.docprintschema_extract.Extract;
import com.csc.fsg.nba.correspondence.docprintschema_extract.Form;
import com.csc.fsg.nba.correspondence.docprintschema_extract.Forms;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNoValueException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;

/** 
 * 
 * This class is responsible for creating an XML extract for a correspondence system.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA129</td><td>Version 5</td><td>xPression Integration</td></tr> 
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.13I</td><td>AXA Life Phase 1</td><td>Informal Correspondence</td></tr>
 * <tr><td>AXAL3.7.32</td><td>AXA Life Phase 2</td><td>Reinsurer Interface</td></tr> 
 * <tr><td>CR58636</td><td>Discretionary</td><td>ADC Retrofit</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 5
 */
public class NbaCorrespondenceExtract {
	protected final static String EXTRACT_LANGUAGE = "English";
	protected final static String EXTRACT_REQUIREDFIELD_PREFIX = "ER";
	private static NbaLogger logger = null;
	//	Begin CR58636 ADC Retrofit 
	protected final static String TAG_ATTACHMENT = "Attachment";
	protected List  attachedImages=new ArrayList();
	//  End CR58636 
	protected final static String TAG_LETTERVARIABLES = "Data";
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */

	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger =
					NbaLogFactory.getLogger(
						NbaCorrespondenceExtract.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log(
					"NbaCorrespondenceExtract could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * This method is required only for testing <code>NbaDocumentSolutionsExtract</code>
	 * 
	 * @param args java.lang.String[]
	 */
	public static void main(String[] args) {
		try {

			NbaxPressionAdapter adapter = new NbaxPressionAdapter();
			//adapter.initializeObjects();
			adapter.getCategoryNames();

			NbaCorrespondenceExtract extractBuilder =
				new NbaCorrespondenceExtract();
			extractBuilder.setCompany("CSC");
			extractBuilder.setLob("UL");
			extractBuilder.setPolicyNumber("1023456789");
			extractBuilder.setLetter("Decline");
			extractBuilder.setVariableValue("EON_FirstName", "Ryan");
			extractBuilder.setVariableValue("EON_MiddleName", "Manny");
			extractBuilder.setVariableValue("EON_LastName", "Jones");
			extractBuilder.setVariableValue("ERN_AgentName", "Joe Smith");
			extractBuilder.setVariableValue("ERV_PolicyNumber", "1111111111");
			extractBuilder.setVariableValue(
				"ERN_HomeAddressLine1",
				"808 Cypress Dr");
			extractBuilder.setVariableValue(
				"ERV_HomeAddressLine2",
				"Chatham IL 62629");

			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug(extractBuilder.marshal());
			}

		} catch (NbaBaseException e) {
			getLogger().logFatal(e);
		}
	}
	protected String company;
	protected String effectiveDate;
	protected String language;
	protected String letter;
	protected String letterType;

	protected List letterVariables;
	protected String lob;
	protected String policyNumber;
	/**
	 * The NbaCorrespondenceExtract constructor initializes all member variables.
	 */
	public NbaCorrespondenceExtract() {
		letterVariables = new ArrayList();
	}
	/**
	 * This method returns the Company value.
	 * @return java.lang.String
	 */
	public String getCompany() {
		return company;
	}
	/**
	 * This method returns the effectiveDate.
	 * @return java.lang.String
	 */
	public String getEffectiveDate() {
		return effectiveDate;
	}

	/**
	 * This method returns the Letter name.
	 * @return java.lang.String
	 */
	public String getLetter() {
		return letter;
	}

	/**
	 * This method is used to get the letter type
	 * @return
	 */
	public String getLetterType() {
		return letterType;
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
	public String getVariable(int i) {
		String[] varArray = new String[2];
		varArray = (String[]) letterVariables.get(i);
		return varArray[0];
	}
	/**
	 * This method returns a value for a letter variable.
	 * @return java.lang.String
	 * @param variable A letter variable name.
	 */
	public String getVariableValue(int i) {
		String[] varArray = new String[2];
		varArray = (String[]) letterVariables.get(i);
		return varArray[1];
	}
	/**
	 * This method creates an XML extract for a correspondence system.
	 * @return java.lang.String
	 * @exception com.csc.fsg.nba.exception.NbaNoValueException This exception is thrown whenever required fields are
	 * missing, or when an <code>IOException</code> has occcured.
	 */
	public String marshal() throws NbaBaseException {
		String marshalledXML = null;
		validateHeaderInformation();

		Correspondence correspondence = new Correspondence();
		Extract extract = new Extract();
		Forms forms = new Forms();
		Form form = new Form();
		Data data = new Data();
		
		//Make sure a language has been set for the correspondence
		if (getLanguage().length() == 0){
			setLanguage(EXTRACT_LANGUAGE);
		}
		String  var = null;
		String val = null;
		ByteArrayOutputStream stream = null;
		String xmlString = null;
		Element root = null;
		try {
			correspondence.setCompany(getCompany());
			correspondence.setLetterName(getLetter());
			correspondence.setLetterType(getLetterType());
			correspondence.setLob(getLob());
			correspondence.setPolicyNumber(getPolicyNumber());
			correspondence.setLanguage(getLanguage());
			correspondence.setEffective_Date(getEffectiveDate());

			// build forms
			forms.setCompany(getCompany());
			forms.setLOB(getLob());
			forms.setPolicyNum(getPolicyNumber());

			//build form
			form.setName(getLetter());

			forms.setForm(form);
			forms.setData(data);
			extract.setForms(forms);
			correspondence.setExtract(extract);
			stream = new ByteArrayOutputStream();
			correspondence.marshal(stream);
			xmlString = stream.toString();
			Element e = null;
			// SPR3290 code deleted

			// create the varialbes used in the letter
			Document xmldoc = new DocumentImpl();
			root = xmldoc.createElement(TAG_LETTERVARIABLES);
			
			for (int i = 0; i < letterVariables.size(); i++) {
				var =getVariable(i);
				val = getVariableValue(i);
				validateField(var, val);
				if ( val != null && ! val.equals("")){
					e = xmldoc.createElementNS(null, getVariable(i));
					Text txt = xmldoc.createTextNode(getVariableValue(i));
					e.appendChild(txt);
					root.appendChild(e);
				}
			}

			//Begin CR58636 ADC Retrofit 
			if(attachedImages.size()>0)
			{
			createAttachment(xmldoc,root);
			}
			//End CR58636		
			xmldoc.appendChild(root);

			OutputFormat of = new OutputFormat();
			of.setIndenting(true);
			of.setIndent(1);

			StringWriter sw = new StringWriter();
			XMLSerializer serializer = new XMLSerializer(of);
			serializer.setOutputCharStream(sw);
			serializer.asDOMSerializer();
			serializer.serialize(xmldoc.getDocumentElement());
			// Remove the <XML tag before inserting into the Correspondence XML
			String dataXml = sw.getBuffer().toString().substring(39);
			marshalledXML = xmlString.replaceAll("<Data></Data>", quoteReplacement(dataXml));	//APSL438
		} catch (Exception e) {
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
	 * This method is used to set the effective date
	 * @param string
	 */
	public void setEffectiveDate(String string) {
		effectiveDate = string;
	}
	/**
	 * This method is used to the Letter name.
	 * @param newLetter java.lang.String
	 */
	public void setLetter(String newLetter) {
		letter = newLetter;
	}

	/**
	 * This method is used to set the letter type
	 * @param string
	 */
	public void setLetterType(String string) {
		letterType = string;
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
	public void setVariables(List variables) {
		letterVariables = variables;
	}

	
	/**
	 * @return Returns the letterVariables.
	 */
	public String getLetterVariablesString() {
		return letterVariables.toString();
	}
	/**
	 * This method is used to set a Letter variable value.
	 * @param variable A letter Variable.
	 * @param value A letter variable value.
	 */
	public void setVariableValue(String variable, String value) {
		String[] varArray = new String[2];
		varArray[0] = variable;
		varArray[1] = value;
		letterVariables.add(varArray);
	}
	/**
	 * This method is used to set a Letter variable value.
	 * @param variable A letter Variable.
	 * @param value A letter variable value.
	 */
	 //AXAL3.7.10A New Method
	public void setVariableValue(String variable, String[] value) {
		if (value.length > 0 && variable.split("_")[0].endsWith("X")) { //ALII899
			for (int i = 0; i < value.length; i++) {
				setVariableValue(variable + "_" + String.valueOf(i + 1), value[i]);
			}
		} else if (value.length > 0) {
			setVariableValue(variable, value[0]);
		} else {
			setVariableValue(variable, "");
		}
	}
	/**
	 * This method validates if the field is required. If so, it throws an exception
	 * if the field value is null or invalid.
	 * @param aField A field to validate
	 * @param aValue A field value
	 * @exception com.csc.fsg.nba.exception.NbaNoValueException Throw this exception if a required field's value is missing.
	 */
	protected void validateField(String aField, String aValue)	throws com.csc.fsg.nba.exception.NbaNoValueException {
		if (aField.startsWith(EXTRACT_REQUIREDFIELD_PREFIX) && aValue.length() == 0) {
			throw new NbaNoValueException(
				"Required field " + aField.substring(4) + " missing");
		}
	}
	/**
	 * This method is used validates requires fields.
	 * @exception com.csc.fsg.nba.exception.NbaNoValueException This exception is thrown whenever required fields are
	 * missing.
	 */
	protected void validateHeaderInformation() throws NbaNoValueException {
		if (company == null) {
			throw new NbaNoValueException("Required field Company missing");
		}
		if (letter == null) {
			throw new NbaNoValueException("Required field Letter name missing");
		}
		if (lob == null) {
			throw new NbaNoValueException("Required field Lob missing");
		}
		if (policyNumber == null) {
			throw new NbaNoValueException("Required field Policy Number missing");
		}
	}

	/**
	 * @return
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @param string
	 */
	public void setLanguage(String string) {
		language = string;
	}

//APSL438 New Method to handle $
	public static String quoteReplacement(String s) {
        if ((s.indexOf('\\') == -1) && (s.indexOf('$') == -1))
            return s;
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                sb.append('\\'); sb.append('\\');
            } else if (c == '$') {
                sb.append('\\'); sb.append('$');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
	
//	CR58636 new Method ADC Retrofit
	/**
	 * @return Returns the attachedImages.
	 */
	public List getAttachedImages() {
		return attachedImages;
	}
	
//	CR58636 new Method ADC Retrofit
	/**
	 * @param attachedImages The attachedImages to set.
	 */
	public void setAttachedImages(List attachedImages) {
		this.attachedImages = attachedImages;
	}
	
//	CR58636 new Method ADC Retrofit
	public void createAttachment(Document xmldoc,Element root)
	{
		Iterator i=attachedImages.iterator();
		int index=0;
		while(i.hasNext())
		{  
			index++;
		        String image=(String)i.next();
		        Element imageElement= xmldoc.createElementNS(null,TAG_ATTACHMENT+"_"+index);
		    	Text imageNode = xmldoc.createTextNode(image);
		    	imageElement.appendChild(imageNode);		
 			    root.appendChild(imageElement);
				
		}
	}
	/**
	 * This method is used to check if the variable already exist or not.
	 * @param variable A letter Variable.
	 * @return boolean A letter variable exists or not.
	 */
	//New Method AXAL3.7.32
	public boolean hasVariable(String variable) {
		Iterator letterIterator = letterVariables.iterator();
		String[] varArray = new String[2];
		while (letterIterator.hasNext()) {
			varArray = (String[]) letterIterator.next();
			if (variable.equalsIgnoreCase(varArray[0])) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This method is used to remove the variable
	 * 
	 * @param variable A letter Variable.
	 */
	//New Method AXAL3.7.32
	public void removeVariable(String variable) {
		Iterator letterIterator = letterVariables.iterator();
		String[] varArray = new String[2];
		while (letterIterator.hasNext()) {
			varArray = (String[]) letterIterator.next();
			if (variable.equalsIgnoreCase(varArray[0])) {
				letterIterator.remove();
			}
		}
	}
}
