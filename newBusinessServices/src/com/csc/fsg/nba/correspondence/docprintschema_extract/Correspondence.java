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
package com.csc.fsg.nba.correspondence.docprintschema_extract;


import com.tbf.xml.*;

/**
 * Correspondence class.
 *
 */
public class Correspondence
	implements com.tbf.xml.XmlObject,
	com.tbf.xml.Validateable,
	java.io.Serializable {

	/**
	 * Constant for "PolicyNumber" node name.
	 */
	public static final String $POLICY_NUMBER = "PolicyNumber";

	/**
	 * Constant for "Extract" node name.
	 */
	public static final String $EXTRACT = "Extract";

	/**
	 * Constant for "LetterType" node name.
	 */
	public static final String $LETTER_TYPE = "LetterType";

	/**
	 * Constant for "Language" node name.
	 */
	public static final String $LANGUAGE = "Language";

	/**
	 * Constant for "Effective_Date" node name.
	 */
	public static final String $EFFECTIVE_DATE = "Effective_Date";

	/**
	 * Constant for "Company" node name.
	 */
	public static final String $COMPANY = "Company";

	/**
	 * Constant for "ImagingCompany" node name.
	 */
	public static final String $IMAGING_COMPANY = "ImagingCompany";

	/**
	 * Constant for "ImagingPolicyNumber" node name.
	 */
	public static final String $IMAGING_POLICY_NUMBER = "ImagingPolicyNumber";

	/**
	 * Constant for "ObjectRef" node name.
	 */
	public static final String $OBJECT_REF = "ObjectRef";

	/**
	 * Constant for "LetterName" node name.
	 */
	public static final String $LETTER_NAME = "LetterName";

	/**
	 * Constant for "Correspondence" node name.
	 */
	public static final String $CORRESPONDENCE = "Correspondence";

	/**
	 * Constant for "Lob" node name.
	 */
	public static final String $LOB = "Lob";


	/**
	 * Declarations for the XML related fields.
	 */
	protected String _PolicyNumber = null;
	protected String _LetterName = null;
	protected String _LetterType = null;
	protected String _Company = null;
	protected String _Lob = null;
	protected String _ObjectRef = null;
	protected java.util.Date _Effective_Date = null;
	protected String _Language = null;
	protected String _ImagingCompany = null;
	protected String _ImagingPolicyNumber = null;
	protected Extract _Extract = null;


	/**
	 * Holds the parent object of this object.
	 */
	protected transient XmlObject _parent_ = null;


	/**
	 * Storage for UNEXPECTED_XML errors.
	 */
	protected transient java.util.Vector _unexpected_xml_errors_ = null;


	/**
	 * Default no args constructor.
	 */
	public Correspondence () {
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 */
	public Correspondence (XmlElement xml) {
		unmarshal(xml);
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 * @param  parent  the containibg XmlObject
	 */
	public Correspondence (XmlElement xml, XmlObject parent) {
		_parent_ = parent;
		unmarshal(xml);
	}


	/**
	 * Get the PolicyNumber property.
	 */
	public String getPolicyNumber () {
		return (_PolicyNumber);
	}


	/**
	 * Set the PolicyNumber property.
	 */
	public void setPolicyNumber (String newValue) {
		_PolicyNumber = newValue;
	}


	/**
	 * Checks for whether PolicyNumber is set or not.
	 *
	 * @return true if PolicyNumber is set, false if not
	 */
	 public boolean hasPolicyNumber () {
		return (_PolicyNumber != null);
	}


	/**
	 * Discards PolicyNumber's value.
	 */
	 public void deletePolicyNumber () {
		_PolicyNumber = null;
	}


	/**
	 * Get the LetterName property.
	 */
	public String getLetterName () {
		return (_LetterName);
	}


	/**
	 * Set the LetterName property.
	 */
	public void setLetterName (String newValue) {
		_LetterName = newValue;
	}


	/**
	 * Checks for whether LetterName is set or not.
	 *
	 * @return true if LetterName is set, false if not
	 */
	 public boolean hasLetterName () {
		return (_LetterName != null);
	}


	/**
	 * Discards LetterName's value.
	 */
	 public void deleteLetterName () {
		_LetterName = null;
	}


	/**
	 * Get the LetterType property.
	 */
	public String getLetterType () {
		return (_LetterType);
	}


	/**
	 * Set the LetterType property.
	 */
	public void setLetterType (String newValue) {
		_LetterType = newValue;
	}


	/**
	 * Checks for whether LetterType is set or not.
	 *
	 * @return true if LetterType is set, false if not
	 */
	 public boolean hasLetterType () {
		return (_LetterType != null);
	}


	/**
	 * Discards LetterType's value.
	 */
	 public void deleteLetterType () {
		_LetterType = null;
	}


	/**
	 * Get the Company property.
	 */
	public String getCompany () {
		return (_Company);
	}


	/**
	 * Set the Company property.
	 */
	public void setCompany (String newValue) {
		_Company = newValue;
	}


	/**
	 * Checks for whether Company is set or not.
	 *
	 * @return true if Company is set, false if not
	 */
	 public boolean hasCompany () {
		return (_Company != null);
	}


	/**
	 * Discards Company's value.
	 */
	 public void deleteCompany () {
		_Company = null;
	}


	/**
	 * Get the Lob property.
	 */
	public String getLob () {
		return (_Lob);
	}


	/**
	 * Set the Lob property.
	 */
	public void setLob (String newValue) {
		_Lob = newValue;
	}


	/**
	 * Checks for whether Lob is set or not.
	 *
	 * @return true if Lob is set, false if not
	 */
	 public boolean hasLob () {
		return (_Lob != null);
	}


	/**
	 * Discards Lob's value.
	 */
	 public void deleteLob () {
		_Lob = null;
	}


	/**
	 * Get the ObjectRef property.
	 */
	public String getObjectRef () {
		return (_ObjectRef);
	}


	/**
	 * Set the ObjectRef property.
	 */
	public void setObjectRef (String newValue) {
		_ObjectRef = newValue;
	}


	/**
	 * Checks for whether ObjectRef is set or not.
	 *
	 * @return true if ObjectRef is set, false if not
	 */
	 public boolean hasObjectRef () {
		return (_ObjectRef != null);
	}


	/**
	 * Discards ObjectRef's value.
	 */
	 public void deleteObjectRef () {
		_ObjectRef = null;
	}


	/**
	 * Get the Effective_Date property.
	 */
	public java.util.Date getEffective_Date () {
		return (_Effective_Date);
	}


	/**
	 * Set the Effective_Date property.
	 */
	public void setEffective_Date (java.util.Date timedate) {
		_Effective_Date = timedate;
		_format_errors.remove("Correspondence.Effective_Date");
	}


	public void setEffective_Date (String str) {

		if (str == null) {
			_Effective_Date = null;
			return;
		}

		_Effective_Date = com.tbf.util.DateUtilities.stringToDate(str, 
			com.tbf.util.ISO8601DateFormat.EXTENDED_DATE);
		if (_Effective_Date == null) {
			XmlValidationError.addValidityFormatError(
				_format_errors, "Correspondence.Effective_Date", "Element",
				"Correspondence/Effective_Date", str);
		} else {
			_format_errors.remove("Correspondence.Effective_Date");
		}
	}


	/**
	 * Checks for whether Effective_Date is set or not.
	 *
	 * @return true if Effective_Date is set, false if not
	 */
	 public boolean hasEffective_Date () {
		return (_Effective_Date != null);
	}


	/**
	 * Discards Effective_Date's value.
	 */
	 public void deleteEffective_Date () {
		_Effective_Date = null;
		_format_errors.remove("Correspondence.Effective_Date");
	}


	/**
	 * Get the Language property.
	 */
	public String getLanguage () {
		return (_Language);
	}


	/**
	 * Set the Language property.
	 */
	public void setLanguage (String newValue) {
		_Language = newValue;
	}


	/**
	 * Checks for whether Language is set or not.
	 *
	 * @return true if Language is set, false if not
	 */
	 public boolean hasLanguage () {
		return (_Language != null);
	}


	/**
	 * Discards Language's value.
	 */
	 public void deleteLanguage () {
		_Language = null;
	}


	/**
	 * Get the ImagingCompany property.
	 */
	public String getImagingCompany () {
		return (_ImagingCompany);
	}


	/**
	 * Set the ImagingCompany property.
	 */
	public void setImagingCompany (String newValue) {
		_ImagingCompany = newValue;
	}


	/**
	 * Checks for whether ImagingCompany is set or not.
	 *
	 * @return true if ImagingCompany is set, false if not
	 */
	 public boolean hasImagingCompany () {
		return (_ImagingCompany != null);
	}


	/**
	 * Discards ImagingCompany's value.
	 */
	 public void deleteImagingCompany () {
		_ImagingCompany = null;
	}


	/**
	 * Get the ImagingPolicyNumber property.
	 */
	public String getImagingPolicyNumber () {
		return (_ImagingPolicyNumber);
	}


	/**
	 * Set the ImagingPolicyNumber property.
	 */
	public void setImagingPolicyNumber (String newValue) {
		_ImagingPolicyNumber = newValue;
	}


	/**
	 * Checks for whether ImagingPolicyNumber is set or not.
	 *
	 * @return true if ImagingPolicyNumber is set, false if not
	 */
	 public boolean hasImagingPolicyNumber () {
		return (_ImagingPolicyNumber != null);
	}


	/**
	 * Discards ImagingPolicyNumber's value.
	 */
	 public void deleteImagingPolicyNumber () {
		_ImagingPolicyNumber = null;
	}


	/**
	 * Get the Extract property.
	 */
	public Extract getExtract () {
		return (_Extract);
	}


	/**
	 * Set the Extract property.
	 */
	public void setExtract (Extract obj) {
		_Extract = obj;
	}


	private void setExtract (XmlElement xml) {

		_Extract =
			new Extract(xml, this);
	}




	/**
	 * Checks for whether Extract is set or not.
	 *
	 * @return true if Extract is set, false if not
	 */
	 public boolean hasExtract () {
		return (_Extract != null);
	}


	/**
	 * Discards Extract's value.
	 */
	 public void deleteExtract () {
		_Extract = null;
	}


	protected String _node_name_ = $CORRESPONDENCE;

	/**
	 * Get the XML tag name for this instance.
	 */
	public String getXmlTagName () {
		return (_node_name_);
	}


	/**
	 * Set the XML tag name for this instance.
	 */
	public void setXmlTagName (String node_name) {
		_node_name_ = node_name;
	}


	/**
	 * Gets the XML tag name for this class.
	 */
	public static String getClassXmlTagName () {
		return ($CORRESPONDENCE);
	}


	/**
	 * Keeps track of format errors that are
	 * thrown in the numeric setXXX() methods().
	 */
	protected java.util.Hashtable _format_errors =
		new java.util.Hashtable();


	/**
	 * This flag is used to used to check whether
	 * the validators have been created.
	 */
	transient protected static boolean _validators_created = false;

	/*
	 * XML Validators
	 */
	transient protected static XmlStringValidator 
		_PolicyNumber_validator_ = null;
	transient protected static XmlValidator 
		_Effective_Date_validator_ = null;
	transient protected static XmlValidator 
		_Extract_validator_ = null;

	/**
	 * Create the validators for this class.
	 */
	protected static synchronized void createValidators () {

		if (_validators_created) {
			return;
		}

		_PolicyNumber_validator_ = new XmlStringValidator(
			"Correspondence.PolicyNumber", "Element", 
			"Correspondence/PolicyNumber", -1, -1, 1, 1);

		_Effective_Date_validator_ = new XmlValidator(
			"Correspondence.Effective_Date", "Element", 
			"Correspondence/Effective_Date", 0, 1);

		_Extract_validator_ = new XmlValidator(
			"Correspondence.Extract", "Element", 
			"Correspondence/Extract", 0, 1);

		_validators_created = true;
	}


	/**
	 * Checks this object to see if it will produce valid XML.
	 */
	public boolean isValid () {

		if (!(this instanceof Validateable)) {
			return (true);
		}

		java.util.Vector errors = getValidationErrors(true);
		if (errors == null || errors.size() < 1) {
			return (true);
		}

		return (false);
	}


	/**
	 * Checks each field on the object for validity and
	 * returns a Vector holding the validation errors.
	 */
	public java.util.Vector getValidationErrors () {
		return (getValidationErrors(false));
	}


	/**
	 * Checks each field on the object for validity and
	 * returns a java.util.Vector holding the validation errors.
	 *
	 * @return  a Vector containing the validation errors
	 */
	public java.util.Vector getValidationErrors (boolean return_on_error) {
		return (getValidationErrors(return_on_error, true));
	}


	/**
	 * Checks each field on the object for validity and
	 * returns a Vector holding the validation errors.
	 *
	 * @return  a Vector containing the validation errors
	 */
	public java.util.Vector getValidationErrors (
		boolean return_on_error, boolean traverse) {

		createValidators();

		java.util.Vector errors;
		if (_unexpected_xml_errors_ != null &&
				_unexpected_xml_errors_.size() > 0) {
			errors = (java.util.Vector)_unexpected_xml_errors_.clone();
			if (return_on_error) {
				return (errors);
			}
		} else {
			errors = new java.util.Vector(4, 4);
		}

		XmlValidationError e;
		e = _PolicyNumber_validator_.validate(
			_PolicyNumber);
		if (e != null) {
			errors.addElement(e);
			if (return_on_error) {
				return (errors);
			}
		}

		e = _Effective_Date_validator_.validate(
			_Effective_Date, _format_errors);
		if (e != null) {
			errors.addElement(e);
			if (return_on_error) {
				return (errors);
			}
		}


		if (!traverse) {
			if (errors.size() < 1) {
				return (null);
			}

			return (errors);
		}


		boolean is_valid;

		is_valid = _Extract_validator_.isValid(
			_Extract,
			errors, return_on_error, traverse);
		if (!is_valid && return_on_error) {
			return (errors);
		}



		if (errors.size() < 1) {
			return (null);
		}

		return (errors);
	}


	/**
	 * Checks the XML to see whether it matches the
	 * XML contents of this class.
	 */
	public static boolean matches (XmlElement xml, XmlObject parent) {

		if (xml == null) {
			return (false);
		}

		return (xml.matches($CORRESPONDENCE, nsm, parent));
	}


	/**
	 * This method unmarshals an XML document instance
	 * into an instance of this class.
	 */
	public static Correspondence unmarshal (
			java.io.InputStream in) throws Exception {

		Correspondence obj = new Correspondence();
		ObjectFactory.unmarshal(obj, in);
		return (obj);
	}


	/**
	 * Populates this object with the values from the 
	 * parsed XML.
	 * @deprecated  will be removed in a future release.
	 * Use {@link #unmarshal(XmlElement)}.
	 */
	public void fromXml (XmlElement xml) {
		unmarshal(xml);
	}


	/**
	 * Populates this object with the values from the 
	 * parsed XML.
	 *
	 * @since 2.5
	 */
	public void unmarshal (XmlElement xml) {

		if (xml == null) {
			return;
		}

		if (!xml.matches($CORRESPONDENCE, this)) {
			return;
		}

		java.util.Vector doc_namespaces = xml.getDeclaredNamespaces();
		if (doc_namespaces != null) {
			_doc_declared_namespaces_ = 
				(java.util.Vector)doc_namespaces.clone();
		}

		unmarshalAttributes(xml);

		/*
		 * Get the contained XmlElement, this is what we process
		 */
		xml = xml.getChildAt(0);
		if (xml == null) {
			return;
		}

		if (xml.matches($POLICY_NUMBER, this)) {
			setPolicyNumber(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($LETTER_NAME, this)) {
			setLetterName(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($LETTER_TYPE, this)) {
			setLetterType(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($COMPANY, this)) {
			setCompany(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($LOB, this)) {
			setLob(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($OBJECT_REF, this)) {
			setObjectRef(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($EFFECTIVE_DATE, this)) {
			setEffective_Date(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($LANGUAGE, this)) {
			setLanguage(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($IMAGING_COMPANY, this)) {
			setImagingCompany(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($IMAGING_POLICY_NUMBER, this)) {
			setImagingPolicyNumber(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($EXTRACT,
			Extract.nsm, this)) {

			setExtract(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml != null) {

			_unexpected_xml_errors_ =
				XmlValidationError.addUnexpectedXmlError(
					this, _unexpected_xml_errors_, xml);
		}
	}


	/**
	 * Unmarshal any attributes.
	 *
	 * @param xml the XmlElement holding the parsed XML
	 * @since 2.5
	 */
	protected void unmarshalAttributes (XmlElement xml) {
	}


	/**
	 * Writes this instance to a stream. 
	 *
	 * @param  stream  the OutputStream to write the XML object to
	 * @deprecated  This method will be removed in a future release.
	 * Use {@link #marshal(XmlOutputStream)} or {@link #marshal(OutputStream)}.
	 */
	public void toXml (java.io.OutputStream stream) {
		marshal(stream);
	}


	/**
	 * Writes this instance to a stream. 
	 *
	 * @param  stream  the OutputStream to write the XML object to
	 * @param  embed_files  set to true to embed files in the XML
	 * @deprecated  This method will be removed in a future release.
	 * Use {@link #marshal(XmlOutputStream)} or {@link #marshal(OutputStream)}.
	 */
	public void toXml (java.io.OutputStream stream, boolean embed_files) {

		XmlOutputStream out = new FormattedOutputStream(stream);
		out.setEmbedFiles(embed_files);
		marshal(out);
	}


	/**
	 * Writes this instance to a stream. 
	 *
	 * @param  stream  the OutputStream to write the XML object to
	 * @param  embed_files  set to true to embed files in the XML
	 * @deprecated  This method will be removed in a future release.
	 * Use {@link #marshal(XmlOutputStream)} or {@link #marshal(OutputStream)}.
	 */
	public void toXml (
			java.io.OutputStream stream, String indent, boolean embed_files) { 

		FormattedOutputStream out = new FormattedOutputStream(stream);
		out.setIndentString(indent);
		out.setEmbedFiles(embed_files);
		marshal(out);
	}


	/**
	 * Writes this instance to a stream. If the OutputStream is not an
	 * instance of XmlOutputStream then a FormattedOutputStream
	 * will be created which wraps the OutputStream.
	 *
	 * @param  stream  the OutputStream to write the XML object to
	 * @see #marshal(XmlOutputStream)
	 * @since 2.5
	 */
	public void marshal (java.io.OutputStream stream) {

		XmlOutputStream out = new FormattedOutputStream(stream);
		marshal(out);
	}


	/**
	 * Writes this instance to an XmlOutputStream.
	 *
	 * @param  out  the XmlOutputStream to write the XML object to
	 * @see #marshal(OutputStream)
	 * @since 2.5
	 */
	public void marshal (XmlOutputStream out) {

		XmlAttributeList attrs = new XmlAttributeList(nsm);
		attrs = marshalAttributes(attrs);
		out.pushScope();
		out.writeStartTag(getXmlTagName(), attrs, false);
		out.incrementIndent();

		out.write($POLICY_NUMBER,
			_PolicyNumber);
		out.write($LETTER_NAME,
			_LetterName);
		out.write($LETTER_TYPE,
			_LetterType);
		out.write($COMPANY,
			_Company);
		out.write($LOB,
			_Lob);
		out.write($OBJECT_REF,
			_ObjectRef);
		out.write($EFFECTIVE_DATE,
			com.tbf.util.DateUtilities.getFormattedDate(getEffective_Date(),
			com.tbf.util.ISO8601DateFormat.EXTENDED_DATE));
		out.write($LANGUAGE,
			_Language);
		out.write($IMAGING_COMPANY,
			_ImagingCompany);
		out.write($IMAGING_POLICY_NUMBER,
			_ImagingPolicyNumber);
		out.write(null,
			getExtract());

		out.decrementIndent();
		out.writeEndTag(getXmlTagName());
		out.popScope();
	}


	/**
	 * Get the XmlAttributeList for marshalling.
	 *
	 * @param attrs  the currently populated XmlAttributeList.
	 * @return  a populated XmlAttributeList
	 * @since 2.5
	 */
	protected XmlAttributeList marshalAttributes (XmlAttributeList attrs) {

		return (attrs);
	}


	/**
	 * Get this object's parent object.
	 */
	public XmlObject get$Parent () {
		return (_parent_);
	}


	/**
	 * Set this object's parent object.
	 */
	public void set$Parent (XmlObject parent) {
		_parent_ = parent;
	}


	/**
	 * The default <code>XmlNamespaceManager</code> for this class.
	 * @since 2.5
	 */
	public static XmlNamespaceManager nsm = null;


	/**
	 * Get the <code>XmlNamespaceManager</code> for this class.
	 * This will be null if no namespaces on this class or if
	 * namespace support is disabled during code generation.
	 *
	 * @since 2.5
	 */
	public XmlNamespaceManager get$NamespaceManager () {
		return (nsm);
	}


	/**
	 * Storage for namespaces declared in the input document.
	 * @since 2.5
	 */
	protected java.util.Vector _doc_declared_namespaces_ = null;


	/**
	 * Get the Vector holding the namespaces declared in the element
	 * that this instance was unmarshalled from.
	 *
	 * @since 2.5
	 */
	public java.util.Vector get$DocumentNamespaces () {
		return (_doc_declared_namespaces_);
	}
}
