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
 * Request class.
 *
 */
public class Request
	implements com.tbf.xml.XmlObject,
	com.tbf.xml.Validateable,
	java.io.Serializable {

	/**
	 * Constant for "ID" node name.
	 */
	public static final String $ID = "ID";

	/**
	 * Constant for "RequestDate" node name.
	 */
	public static final String $REQUEST_DATE = "RequestDate";

	/**
	 * Constant for "ReinsurerID" node name.
	 */
	public static final String $REINSURER_ID = "ReinsurerID";

	/**
	 * Constant for "Request" node name.
	 */
	public static final String $REQUEST = "Request";

	/**
	 * Constant for "ContactID" node name.
	 */
	public static final String $CONTACT_ID = "ContactID";

	/**
	 * Constant for "Reinsurer" node name.
	 */
	public static final String $REINSURER = "Reinsurer";

	/**
	 * Constant for "Comments" node name.
	 */
	public static final String $COMMENTS = "Comments";


	/**
	 * Declarations for the XML related fields.
	 */
	protected Reinsurer _Reinsurer = null;
	protected String _Comments = null;
	protected java.math.BigInteger _ContactID = null;
	protected java.math.BigInteger _ID = null;
	protected java.math.BigInteger _ReinsurerID = null;
	protected String _RequestDate = null;


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
	public Request () {
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 */
	public Request (XmlElement xml) {
		unmarshal(xml);
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 * @param  parent  the containibg XmlObject
	 */
	public Request (XmlElement xml, XmlObject parent) {
		_parent_ = parent;
		unmarshal(xml);
	}


	/**
	 * Get the Reinsurer property.
	 */
	public Reinsurer getReinsurer () {
		return (_Reinsurer);
	}


	/**
	 * Set the Reinsurer property.
	 */
	public void setReinsurer (Reinsurer obj) {
		_Reinsurer = obj;
	}


	private void setReinsurer (XmlElement xml) {

		_Reinsurer =
			new Reinsurer(xml, this);
	}




	/**
	 * Checks for whether Reinsurer is set or not.
	 *
	 * @return true if Reinsurer is set, false if not
	 */
	 public boolean hasReinsurer () {
		return (_Reinsurer != null);
	}


	/**
	 * Discards Reinsurer's value.
	 */
	 public void deleteReinsurer () {
		_Reinsurer = null;
	}


	/**
	 * Get the Comments property.
	 */
	public String getComments () {
		return (_Comments);
	}


	/**
	 * Set the Comments property.
	 */
	public void setComments (String newValue) {
		_Comments = newValue;
	}


	/**
	 * Checks for whether Comments is set or not.
	 *
	 * @return true if Comments is set, false if not
	 */
	 public boolean hasComments () {
		return (_Comments != null);
	}


	/**
	 * Discards Comments's value.
	 */
	 public void deleteComments () {
		_Comments = null;
	}


	/**
	 * Get the ContactID property.
	 */
	public java.math.BigInteger getContactID () {
		return (_ContactID);
	}


	/**
	 * Set the ContactID property.
	 */
	public void setContactID (java.math.BigInteger new_value) {
		_ContactID = new_value;
		_format_errors.remove("Request.ContactID");
	}


	public void setContactID (String new_value) {

		if (new_value == null) {
			_ContactID = null;
			return;
		}

		try {
			_ContactID = new java.math.BigInteger(new_value);
			_format_errors.remove("Request.ContactID");
		} catch (NumberFormatException nfe) {
			_ContactID = null;
			XmlValidationError.addValidityFormatError(
				_format_errors, "Request.ContactID", "Attribute",
				"Request/ContactID", new_value);
		}
	}


	/**
	 * Checks for whether ContactID is set or not.
	 *
	 * @return true if ContactID is set, false if not
	 */
	 public boolean hasContactID () {
		return (_ContactID != null);
	}


	/**
	 * Discards ContactID's value.
	 */
	 public void deleteContactID () {
		_ContactID = null;
		_format_errors.remove("Request.ContactID");
	}


	/**
	 * Get the ID property.
	 */
	public java.math.BigInteger getID () {
		return (_ID);
	}


	/**
	 * Set the ID property.
	 */
	public void setID (java.math.BigInteger new_value) {
		_ID = new_value;
		_format_errors.remove("Request.ID");
	}


	public void setID (String new_value) {

		if (new_value == null) {
			_ID = null;
			return;
		}

		try {
			_ID = new java.math.BigInteger(new_value);
			_format_errors.remove("Request.ID");
		} catch (NumberFormatException nfe) {
			_ID = null;
			XmlValidationError.addValidityFormatError(
				_format_errors, "Request.ID", "Attribute",
				"Request/ID", new_value);
		}
	}


	/**
	 * Checks for whether ID is set or not.
	 *
	 * @return true if ID is set, false if not
	 */
	 public boolean hasID () {
		return (_ID != null);
	}


	/**
	 * Discards ID's value.
	 */
	 public void deleteID () {
		_ID = null;
		_format_errors.remove("Request.ID");
	}


	/**
	 * Get the ReinsurerID property.
	 */
	public java.math.BigInteger getReinsurerID () {
		return (_ReinsurerID);
	}


	/**
	 * Set the ReinsurerID property.
	 */
	public void setReinsurerID (java.math.BigInteger new_value) {
		_ReinsurerID = new_value;
		_format_errors.remove("Request.ReinsurerID");
	}


	public void setReinsurerID (String new_value) {

		if (new_value == null) {
			_ReinsurerID = null;
			return;
		}

		try {
			_ReinsurerID = new java.math.BigInteger(new_value);
			_format_errors.remove("Request.ReinsurerID");
		} catch (NumberFormatException nfe) {
			_ReinsurerID = null;
			XmlValidationError.addValidityFormatError(
				_format_errors, "Request.ReinsurerID", "Attribute",
				"Request/ReinsurerID", new_value);
		}
	}


	/**
	 * Checks for whether ReinsurerID is set or not.
	 *
	 * @return true if ReinsurerID is set, false if not
	 */
	 public boolean hasReinsurerID () {
		return (_ReinsurerID != null);
	}


	/**
	 * Discards ReinsurerID's value.
	 */
	 public void deleteReinsurerID () {
		_ReinsurerID = null;
		_format_errors.remove("Request.ReinsurerID");
	}


	/**
	 * Get the RequestDate property.
	 */
	public String getRequestDate () {
		return (_RequestDate);
	}


	/**
	 * Set the RequestDate property.
	 */
	public void setRequestDate (String newValue) {
		_RequestDate = newValue;
	}


	/**
	 * Checks for whether RequestDate is set or not.
	 *
	 * @return true if RequestDate is set, false if not
	 */
	 public boolean hasRequestDate () {
		return (_RequestDate != null);
	}


	/**
	 * Discards RequestDate's value.
	 */
	 public void deleteRequestDate () {
		_RequestDate = null;
	}


	protected String _node_name_ = $REQUEST;

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
		return ($REQUEST);
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
		_Reinsurer_validator_ = null;
	transient protected static XmlNumberValidator
		_ContactID_validator_ = null;
	transient protected static XmlNumberValidator
		_ID_validator_ = null;
	transient protected static XmlNumberValidator
		_ReinsurerID_validator_ = null;

	/**
	 * Create the validators for this class.
	 */
	protected static synchronized void createValidators () {

		if (_validators_created) {
			return;
		}

		_Reinsurer_validator_ = new XmlValidator(
			"Request.Reinsurer", "Element", 
			"Request/Reinsurer", 1, 1);

		_ContactID_validator_ = new XmlNumberValidator(
			"Request.ContactID", "Attribute",
			"Request/ContactID",
			null, XmlValidator.NOT_USED,
			null, XmlValidator.NOT_USED, 0, 1);

		_ID_validator_ = new XmlNumberValidator(
			"Request.ID", "Attribute",
			"Request/ID",
			null, XmlValidator.NOT_USED,
			null, XmlValidator.NOT_USED, 0, 1);

		_ReinsurerID_validator_ = new XmlNumberValidator(
			"Request.ReinsurerID", "Attribute",
			"Request/ReinsurerID",
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
		e = _ContactID_validator_.validate(
			_ContactID,
			_format_errors);
		if (e != null) {
			errors.addElement(e);
			if (return_on_error) {
				return (errors);
			}
		}

		e = _ID_validator_.validate(
			_ID,
			_format_errors);
		if (e != null) {
			errors.addElement(e);
			if (return_on_error) {
				return (errors);
			}
		}

		e = _ReinsurerID_validator_.validate(
			_ReinsurerID,
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

		is_valid = _Reinsurer_validator_.isValid(
			_Reinsurer,
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

		return (xml.matches($REQUEST, nsm, parent));
	}


	/**
	 * This method unmarshals an XML document instance
	 * into an instance of this class.
	 */
	public static Request unmarshal (
			java.io.InputStream in) throws Exception {

		Request obj = new Request();
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

		if (!xml.matches($REQUEST, this)) {
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

		if (xml.matches($REINSURER,
			Reinsurer.nsm, this)) {

			setReinsurer(xml);
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
		setComments(xml.getAttribute($COMMENTS, this, false));
		setContactID(xml.getAttribute($CONTACT_ID, this, false));
		setID(xml.getAttribute($ID, this, false));
		setReinsurerID(xml.getAttribute($REINSURER_ID, this, false));
		setRequestDate(xml.getAttribute($REQUEST_DATE, this, false));
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
			getReinsurer());

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

		attrs.add($COMMENTS, _Comments);
		attrs.add($CONTACT_ID, _ContactID);
		attrs.add($ID, _ID);
		attrs.add($REINSURER_ID, _ReinsurerID);
		attrs.add($REQUEST_DATE, _RequestDate);

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
