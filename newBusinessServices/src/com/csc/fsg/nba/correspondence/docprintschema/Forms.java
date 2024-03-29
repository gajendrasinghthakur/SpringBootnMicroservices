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
package com.csc.fsg.nba.correspondence.docprintschema;


import com.tbf.xml.*;

/**
 * Forms class.
 *
 */
public class Forms
	implements com.tbf.xml.XmlObject,
	com.tbf.xml.Validateable,
	java.io.Serializable {

	/**
	 * Constant for "Forms" node name.
	 */
	public static final String $FORMS = "Forms";


	/**
	 * Declarations for the XML related fields.
	 */
	protected java.util.Vector _NameAndDescAndCategoryAndDefaultDataSourceName = new java.util.Vector(4, 0);


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
	public Forms () {
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 */
	public Forms (XmlElement xml) {
		unmarshal(xml);
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 * @param  parent  the containibg XmlObject
	 */
	public Forms (XmlElement xml, XmlObject parent) {
		_parent_ = parent;
		unmarshal(xml);
	}


	/**
	 * Get the NameAndDescAndCategoryAndDefaultDataSourceName property.
	 */
	public java.util.Vector getNameAndDescAndCategoryAndDefaultDataSourceName () {
		return (_NameAndDescAndCategoryAndDefaultDataSourceName);
	}


	public NameAndDescAndCategoryAndDefaultDataSourceName getNameAndDescAndCategoryAndDefaultDataSourceNameAt (int index)
			throws IndexOutOfBoundsException {
		return ((NameAndDescAndCategoryAndDefaultDataSourceName)_NameAndDescAndCategoryAndDefaultDataSourceName.elementAt(index));
	}


	/**
	 * Get the count of elements in the NameAndDescAndCategoryAndDefaultDataSourceName property.
	 */
	public int getNameAndDescAndCategoryAndDefaultDataSourceNameCount () {
		if (_NameAndDescAndCategoryAndDefaultDataSourceName == null) {
			return (0);
		}

		return (_NameAndDescAndCategoryAndDefaultDataSourceName.size());
	}


	/**
	 * Set the NameAndDescAndCategoryAndDefaultDataSourceName property.
	 */
	public void setNameAndDescAndCategoryAndDefaultDataSourceName (java.util.Vector newList) {

		if (newList == null) {
			_NameAndDescAndCategoryAndDefaultDataSourceName.removeAllElements();
		} else {
			_NameAndDescAndCategoryAndDefaultDataSourceName = (java.util.Vector)newList.clone();
		}
	}


	public void addNameAndDescAndCategoryAndDefaultDataSourceName (NameAndDescAndCategoryAndDefaultDataSourceName obj) {
		if (obj == null) {
			return;
		}

		_NameAndDescAndCategoryAndDefaultDataSourceName.addElement(obj);
	}


	public void setNameAndDescAndCategoryAndDefaultDataSourceNameAt (NameAndDescAndCategoryAndDefaultDataSourceName obj, int index)
			throws IndexOutOfBoundsException {
		if (obj == null) {
			return;
		}

		_NameAndDescAndCategoryAndDefaultDataSourceName.setElementAt(obj, index);
	}


	public void removeNameAndDescAndCategoryAndDefaultDataSourceName (NameAndDescAndCategoryAndDefaultDataSourceName obj) {
		if (obj == null) {
			return;
		}

		_NameAndDescAndCategoryAndDefaultDataSourceName.removeElement(obj);
	}


	public void removeNameAndDescAndCategoryAndDefaultDataSourceNameAt (int index)
			throws IndexOutOfBoundsException {
		_NameAndDescAndCategoryAndDefaultDataSourceName.removeElementAt(index);
	}


	private void setNameAndDescAndCategoryAndDefaultDataSourceName (XmlElement xml) {

		_NameAndDescAndCategoryAndDefaultDataSourceName.removeAllElements();
		XmlElement saved_xml = xml;
		boolean first = true;

		while (xml != null &&
			NameAndDescAndCategoryAndDefaultDataSourceName.matches(xml, this)) {

			Object obj = new NameAndDescAndCategoryAndDefaultDataSourceName(xml, this);
			_NameAndDescAndCategoryAndDefaultDataSourceName.addElement(obj);

			if (first) {
				first = false;
				XmlElement last_processed = xml.last_processed;
				xml = xml.next();
				saved_xml.setLastProcessed(last_processed);
			} else {
				saved_xml.setLastProcessed(xml);
				xml = xml.next();
			}
		}
	}


	protected String _node_name_ = $FORMS;

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
		return ($FORMS);
	}


	/**
	 * This flag is used to used to check whether
	 * the validators have been created.
	 */
	transient protected static boolean _validators_created = false;

	/*
	 * XML Validators
	 */
	transient protected static XmlValidator 
		_NameAndDescAndCategoryAndDefaultDataSourceName_validator_ = null;

	/**
	 * Create the validators for this class.
	 */
	protected static synchronized void createValidators () {

		if (_validators_created) {
			return;
		}

		_NameAndDescAndCategoryAndDefaultDataSourceName_validator_ = new XmlValidator(
			"Forms.NameAndDescAndCategoryAndDefaultDataSourceName", "Element", 
			"", 0, -1);

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

		if (!traverse) {
			if (errors.size() < 1) {
				return (null);
			}

			return (errors);
		}


		boolean is_valid;

		is_valid = _NameAndDescAndCategoryAndDefaultDataSourceName_validator_.isValid(
			_NameAndDescAndCategoryAndDefaultDataSourceName,
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

		return (xml.matches($FORMS, nsm, parent));
	}


	/**
	 * This method unmarshals an XML document instance
	 * into an instance of this class.
	 */
	public static Forms unmarshal (
			java.io.InputStream in) throws Exception {

		Forms obj = new Forms();
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

		if (!xml.matches($FORMS, this)) {
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

		if (NameAndDescAndCategoryAndDefaultDataSourceName.matches(xml, this)) {
			setNameAndDescAndCategoryAndDefaultDataSourceName(xml);
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

		out.write(null,
			getNameAndDescAndCategoryAndDefaultDataSourceName());

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
