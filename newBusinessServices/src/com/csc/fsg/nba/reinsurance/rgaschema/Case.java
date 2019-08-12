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
package com.csc.fsg.nba.reinsurance.rgaschema;


import java.io.OutputStream;

import com.tbf.xml.FormattedOutputStream;
import com.tbf.xml.Validateable;
import com.tbf.xml.XmlAttributeList;
import com.tbf.xml.XmlElement;
import com.tbf.xml.XmlNamespaceManager;
import com.tbf.xml.XmlNumberValidator;
import com.tbf.xml.XmlObject;
import com.tbf.xml.XmlOutputStream;
import com.tbf.xml.XmlValidationError;
import com.tbf.xml.XmlValidator;

/**
 * Case class.
 *
 */
public class Case
	implements com.tbf.xml.XmlObject,
	com.tbf.xml.Validateable,
	java.io.Serializable {

	/**
	 * Constant for "DateEntered" node name.
	 */
	public static final String $DATE_ENTERED = "DateEntered";

	/**
	 * Constant for "FaceAmount" node name.
	 */
	public static final String $FACE_AMOUNT = "FaceAmount";

	/**
	 * Constant for "Comment" node name.
	 */
	public static final String $COMMENT = "Comment";

	/**
	 * Constant for "ApplicationID" node name.
	 */
	public static final String $APPLICATION_ID = "ApplicationID";

	/**
	 * Constant for "Case" node name.
	 */
	public static final String $CASE = "Case";

	/**
	 * Constant for "CedingCompany" node name.
	 */
	public static final String $CEDING_COMPANY = "CedingCompany";

	/**
	 * Constant for "Applicants" node name.
	 */
	public static final String $APPLICANTS = "Applicants";

	/**
	 * Constant for "CaseID" node name.
	 */
	public static final String $CASE_ID = "CaseID";

	/**
	 * Constant for "Requests" node name.
	 */
	public static final String $REQUESTS = "Requests";

	/**
	 * Constant for "JointCase" node name.
	 */
	public static final String $JOINT_CASE = "JointCase";

	/**
	 * Constant for "Documents" node name.
	 */
	public static final String $DOCUMENTS = "Documents";


	/**
	 * Declarations for the XML related fields.
	 */
	protected CedingCompany _CedingCompany = null;
	protected Applicants _Applicants = null;
	protected Documents _Documents = null;
	protected Requests _Requests = null;
	protected String _ApplicationID = null;
	protected java.math.BigInteger _CaseID = null;
	protected String _Comment = null;
	protected String _DateEntered = null;
	protected java.math.BigDecimal _FaceAmount = null;
	protected String _JointCase = null;


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
	public Case () {
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 */
	public Case (XmlElement xml) {
		unmarshal(xml);
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 * @param  parent  the containibg XmlObject
	 */
	public Case (XmlElement xml, XmlObject parent) {
		_parent_ = parent;
		unmarshal(xml);
	}


	/**
	 * Get the CedingCompany property.
	 */
	public CedingCompany getCedingCompany () {
		return (_CedingCompany);
	}


	/**
	 * Set the CedingCompany property.
	 */
	public void setCedingCompany (CedingCompany obj) {
		_CedingCompany = obj;
	}


	private void setCedingCompany (XmlElement xml) {

		_CedingCompany =
			new CedingCompany(xml, this);
	}




	/**
	 * Checks for whether CedingCompany is set or not.
	 *
	 * @return true if CedingCompany is set, false if not
	 */
	 public boolean hasCedingCompany () {
		return (_CedingCompany != null);
	}


	/**
	 * Discards CedingCompany's value.
	 */
	 public void deleteCedingCompany () {
		_CedingCompany = null;
	}


	/**
	 * Get the Applicants property.
	 */
	public Applicants getApplicants () {
		return (_Applicants);
	}


	/**
	 * Set the Applicants property.
	 */
	public void setApplicants (Applicants obj) {
		_Applicants = obj;
	}


	private void setApplicants (XmlElement xml) {

		_Applicants =
			new Applicants(xml, this);
	}




	/**
	 * Checks for whether Applicants is set or not.
	 *
	 * @return true if Applicants is set, false if not
	 */
	 public boolean hasApplicants () {
		return (_Applicants != null);
	}


	/**
	 * Discards Applicants's value.
	 */
	 public void deleteApplicants () {
		_Applicants = null;
	}


	/**
	 * Get the Documents property.
	 */
	public Documents getDocuments () {
		return (_Documents);
	}


	/**
	 * Set the Documents property.
	 */
	public void setDocuments (Documents obj) {
		_Documents = obj;
	}


	private void setDocuments (XmlElement xml) {

		_Documents =
			new Documents(xml, this);
	}




	/**
	 * Checks for whether Documents is set or not.
	 *
	 * @return true if Documents is set, false if not
	 */
	 public boolean hasDocuments () {
		return (_Documents != null);
	}


	/**
	 * Discards Documents's value.
	 */
	 public void deleteDocuments () {
		_Documents = null;
	}


	/**
	 * Get the Requests property.
	 */
	public Requests getRequests () {
		return (_Requests);
	}


	/**
	 * Set the Requests property.
	 */
	public void setRequests (Requests obj) {
		_Requests = obj;
	}


	private void setRequests (XmlElement xml) {

		_Requests =
			new Requests(xml, this);
	}




	/**
	 * Checks for whether Requests is set or not.
	 *
	 * @return true if Requests is set, false if not
	 */
	 public boolean hasRequests () {
		return (_Requests != null);
	}


	/**
	 * Discards Requests's value.
	 */
	 public void deleteRequests () {
		_Requests = null;
	}


	/**
	 * Get the ApplicationID property.
	 */
	public String getApplicationID () {
		return (_ApplicationID);
	}


	/**
	 * Set the ApplicationID property.
	 */
	public void setApplicationID (String newValue) {
		_ApplicationID = newValue;
	}


	/**
	 * Checks for whether ApplicationID is set or not.
	 *
	 * @return true if ApplicationID is set, false if not
	 */
	 public boolean hasApplicationID () {
		return (_ApplicationID != null);
	}


	/**
	 * Discards ApplicationID's value.
	 */
	 public void deleteApplicationID () {
		_ApplicationID = null;
	}


	/**
	 * Get the CaseID property.
	 */
	public java.math.BigInteger getCaseID () {
		return (_CaseID);
	}


	/**
	 * Set the CaseID property.
	 */
	public void setCaseID (java.math.BigInteger new_value) {
		_CaseID = new_value;
		_format_errors.remove("Case.CaseID");
	}


	public void setCaseID (String new_value) {

		if (new_value == null) {
			_CaseID = null;
			return;
		}

		try {
			_CaseID = new java.math.BigInteger(new_value);
			_format_errors.remove("Case.CaseID");
		} catch (NumberFormatException nfe) {
			_CaseID = null;
			XmlValidationError.addValidityFormatError(
				_format_errors, "Case.CaseID", "Attribute",
				"Case/CaseID", new_value);
		}
	}


	/**
	 * Checks for whether CaseID is set or not.
	 *
	 * @return true if CaseID is set, false if not
	 */
	 public boolean hasCaseID () {
		return (_CaseID != null);
	}


	/**
	 * Discards CaseID's value.
	 */
	 public void deleteCaseID () {
		_CaseID = null;
		_format_errors.remove("Case.CaseID");
	}


	/**
	 * Get the Comment property.
	 */
	public String getComment () {
		return (_Comment);
	}


	/**
	 * Set the Comment property.
	 */
	public void setComment (String newValue) {
		_Comment = newValue;
	}


	/**
	 * Checks for whether Comment is set or not.
	 *
	 * @return true if Comment is set, false if not
	 */
	 public boolean hasComment () {
		return (_Comment != null);
	}


	/**
	 * Discards Comment's value.
	 */
	 public void deleteComment () {
		_Comment = null;
	}


	/**
	 * Get the DateEntered property.
	 */
	public String getDateEntered () {
		return (_DateEntered);
	}


	/**
	 * Set the DateEntered property.
	 */
	public void setDateEntered (String newValue) {
		_DateEntered = newValue;
	}


	/**
	 * Checks for whether DateEntered is set or not.
	 *
	 * @return true if DateEntered is set, false if not
	 */
	 public boolean hasDateEntered () {
		return (_DateEntered != null);
	}


	/**
	 * Discards DateEntered's value.
	 */
	 public void deleteDateEntered () {
		_DateEntered = null;
	}


	/**
	 * Get the FaceAmount property.
	 */
	public java.math.BigDecimal getFaceAmount () {
		return (_FaceAmount);
	}


	/**
	 * Set the FaceAmount property.
	 */
	public void setFaceAmount (java.math.BigDecimal new_value) {
		_FaceAmount = new_value;
		_format_errors.remove("Case.FaceAmount");
	}


	public void setFaceAmount (String new_value) {

		if (new_value == null) {
			_FaceAmount = null;
			return;
		}

		try {
			_FaceAmount = new java.math.BigDecimal(new_value);
			_format_errors.remove("Case.FaceAmount");
		} catch (NumberFormatException nfe) {
			_FaceAmount = null;
			XmlValidationError.addValidityFormatError(
				_format_errors, "Case.FaceAmount", "Attribute",
				"Case/FaceAmount", new_value);
		}
	}


	/**
	 * Checks for whether FaceAmount is set or not.
	 *
	 * @return true if FaceAmount is set, false if not
	 */
	 public boolean hasFaceAmount () {
		return (_FaceAmount != null);
	}


	/**
	 * Discards FaceAmount's value.
	 */
	 public void deleteFaceAmount () {
		_FaceAmount = null;
		_format_errors.remove("Case.FaceAmount");
	}


	/**
	 * Get the JointCase property.
	 */
	public String getJointCase () {
		return (_JointCase);
	}


	/**
	 * Set the JointCase property.
	 */
	public void setJointCase (String newValue) {
		_JointCase = newValue;
	}


	/**
	 * Checks for whether JointCase is set or not.
	 *
	 * @return true if JointCase is set, false if not
	 */
	 public boolean hasJointCase () {
		return (_JointCase != null);
	}


	/**
	 * Discards JointCase's value.
	 */
	 public void deleteJointCase () {
		_JointCase = null;
	}


	protected String _node_name_ = $CASE;

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
		return ($CASE);
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
	transient protected static XmlValidator 
		_CedingCompany_validator_ = null;
	transient protected static XmlValidator 
		_Applicants_validator_ = null;
	transient protected static XmlValidator 
		_Documents_validator_ = null;
	transient protected static XmlValidator 
		_Requests_validator_ = null;
	transient protected static XmlNumberValidator
		_CaseID_validator_ = null;
	transient protected static XmlNumberValidator
		_FaceAmount_validator_ = null;

	/**
	 * Create the validators for this class.
	 */
	protected static synchronized void createValidators () {

		if (_validators_created) {
			return;
		}

		_CedingCompany_validator_ = new XmlValidator(
			"Case.CedingCompany", "Element", 
			"Case/CedingCompany", 1, 1);

		_Applicants_validator_ = new XmlValidator(
			"Case.Applicants", "Element", 
			"Case/Applicants", 1, 1);

		_Documents_validator_ = new XmlValidator(
			"Case.Documents", "Element", 
			"Case/Documents", 1, 1);

		_Requests_validator_ = new XmlValidator(
			"Case.Requests", "Element", 
			"Case/Requests", 1, 1);

		_CaseID_validator_ = new XmlNumberValidator(
			"Case.CaseID", "Attribute",
			"Case/CaseID",
			null, XmlValidator.NOT_USED,
			null, XmlValidator.NOT_USED, 0, 1);

		_FaceAmount_validator_ = new XmlNumberValidator(
			"Case.FaceAmount", "Attribute",
			"Case/FaceAmount",
			null, XmlValidator.NOT_USED,
			null, XmlValidator.NOT_USED, 0, 1);

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
		e = _CaseID_validator_.validate(
			_CaseID,
			_format_errors);
		if (e != null) {
			errors.addElement(e);
			if (return_on_error) {
				return (errors);
			}
		}

		e = _FaceAmount_validator_.validate(
			_FaceAmount,
			_format_errors);
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

		is_valid = _CedingCompany_validator_.isValid(
			_CedingCompany,
			errors, return_on_error, traverse);
		if (!is_valid && return_on_error) {
			return (errors);
		}

		is_valid = _Applicants_validator_.isValid(
			_Applicants,
			errors, return_on_error, traverse);
		if (!is_valid && return_on_error) {
			return (errors);
		}

		is_valid = _Documents_validator_.isValid(
			_Documents,
			errors, return_on_error, traverse);
		if (!is_valid && return_on_error) {
			return (errors);
		}

		is_valid = _Requests_validator_.isValid(
			_Requests,
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

		return (xml.matches($CASE, nsm, parent));
	}


	/**
	 * This method unmarshals an XML document instance
	 * into an instance of this class.
	 */
	public static Case unmarshal (
			java.io.InputStream in) throws Exception {

		Case obj = new Case();
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

		if (!xml.matches($CASE, this)) {
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

		if (xml.matches($CEDING_COMPANY,
			CedingCompany.nsm, this)) {

			setCedingCompany(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($APPLICANTS,
			Applicants.nsm, this)) {

			setApplicants(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($DOCUMENTS,
			Documents.nsm, this)) {

			setDocuments(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($REQUESTS,
			Requests.nsm, this)) {

			setRequests(xml);
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

		/*
		 * Set the attribute based field(s)
		 */
		setApplicationID(xml.getAttribute($APPLICATION_ID, this, false));
		setCaseID(xml.getAttribute($CASE_ID, this, false));
		setComment(xml.getAttribute($COMMENT, this, false));
		setDateEntered(xml.getAttribute($DATE_ENTERED, this, false));
		setFaceAmount(xml.getAttribute($FACE_AMOUNT, this, false));
		setJointCase(xml.getAttribute($JOINT_CASE, this, false));
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

		out.write(null,
			getCedingCompany());
		out.write(null,
			getApplicants());
		out.write(null,
			getDocuments());
		out.write(null,
			getRequests());

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

		if (attrs == null) {
			attrs = new XmlAttributeList();
		}

		attrs.add($APPLICATION_ID, _ApplicationID);
		attrs.add($CASE_ID, _CaseID);
		attrs.add($COMMENT, _Comment);
		attrs.add($DATE_ENTERED, _DateEntered);
		attrs.add($FACE_AMOUNT, _FaceAmount);
		attrs.add($JOINT_CASE, _JointCase);

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
