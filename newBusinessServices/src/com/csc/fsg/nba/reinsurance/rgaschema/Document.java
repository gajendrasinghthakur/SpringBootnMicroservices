/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which are
 * proprietary to CSC Financial Services Group�.  The use, reproduction,
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
 * Document class.
 *
 */
public class Document
	implements com.tbf.xml.XmlObject,
	com.tbf.xml.Validateable,
	java.io.Serializable {

	/**
	 * Constant for "ID" node name.
	 */
	public static final String $ID = "ID";

	/**
	 * Constant for "Document" node name.
	 */
	public static final String $DOCUMENT = "Document";

	/**
	 * Constant for "Filename" node name.
	 */
	public static final String $FILENAME = "Filename";

	/**
	 * Constant for "Path" node name.
	 */
	public static final String $PATH = "Path";

	/**
	 * Constant for "Comment" node name.
	 */
	public static final String $COMMENT = "Comment";


	/**
	 * Declarations for the XML related fields.
	 */
	protected String _Comment = null;
	protected String _Filename = null;
	protected java.math.BigInteger _ID = null;
	protected String _Path = null;


	/**
	 * Holds the parent object of this object.
	 */
	protected transient XmlObject _parent_ = null;


	/**
	 * Default no args constructor.
	 */
	public Document () {
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 */
	public Document (XmlElement xml) {
		unmarshal(xml);
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 * @param  parent  the containibg XmlObject
	 */
	public Document (XmlElement xml, XmlObject parent) {
		_parent_ = parent;
		unmarshal(xml);
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
	 * Get the Filename property.
	 */
	public String getFilename () {
		return (_Filename);
	}


	/**
	 * Set the Filename property.
	 */
	public void setFilename (String newValue) {
		_Filename = newValue;
	}


	/**
	 * Checks for whether Filename is set or not.
	 *
	 * @return true if Filename is set, false if not
	 */
	 public boolean hasFilename () {
		return (_Filename != null);
	}


	/**
	 * Discards Filename's value.
	 */
	 public void deleteFilename () {
		_Filename = null;
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
		_format_errors.remove("Document.ID");
	}


	public void setID (String new_value) {

		if (new_value == null) {
			_ID = null;
			return;
		}

		try {
			_ID = new java.math.BigInteger(new_value);
			_format_errors.remove("Document.ID");
		} catch (NumberFormatException nfe) {
			_ID = null;
			XmlValidationError.addValidityFormatError(
				_format_errors, "Document.ID", "Attribute",
				"Document/ID", new_value);
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
		_format_errors.remove("Document.ID");
	}


	/**
	 * Get the Path property.
	 */
	public String getPath () {
		return (_Path);
	}


	/**
	 * Set the Path property.
	 */
	public void setPath (String newValue) {
		_Path = newValue;
	}


	/**
	 * Checks for whether Path is set or not.
	 *
	 * @return true if Path is set, false if not
	 */
	 public boolean hasPath () {
		return (_Path != null);
	}


	/**
	 * Discards Path's value.
	 */
	 public void deletePath () {
		_Path = null;
	}


	protected String _node_name_ = $DOCUMENT;

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
		return ($DOCUMENT);
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
	transient protected static XmlNumberValidator
		_ID_validator_ = null;

	/**
	 * Create the validators for this class.
	 */
	protected static synchronized void createValidators () {

		if (_validators_created) {
			return;
		}

		_ID_validator_ = new XmlNumberValidator(
			"Document.ID", "Attribute",
			"Document/ID",
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

		java.util.Vector errors = new java.util.Vector(4, 4);
		XmlValidationError e;
		e = _ID_validator_.validate(
			_ID,
			_format_errors);
		if (e != null) {
			errors.addElement(e);
			if (return_on_error) {
				return (errors);
			}
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

		return (xml.matches($DOCUMENT, nsm, parent));
	}


	/**
	 * This method unmarshals an XML document instance
	 * into an instance of this class.
	 */
	public static Document unmarshal (
			java.io.InputStream in) throws Exception {

		Document obj = new Document();
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

		if (!xml.matches($DOCUMENT, this)) {
			return;
		}

		java.util.Vector doc_namespaces = xml.getDeclaredNamespaces();
		if (doc_namespaces != null) {
			_doc_declared_namespaces_ = 
				(java.util.Vector)doc_namespaces.clone();
		}

		unmarshalAttributes(xml);
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
		setComment(xml.getAttribute($COMMENT, this, false));
		setFilename(xml.getAttribute($FILENAME, this, false));
		setID(xml.getAttribute($ID, this, false));
		setPath(xml.getAttribute($PATH, this, false));
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
		out.writeStartTag(getXmlTagName(), attrs, true);


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

		attrs.add($COMMENT, _Comment);
		attrs.add($FILENAME, _Filename);
		attrs.add($ID, _ID);
		attrs.add($PATH, _Path);

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
