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
package com.csc.fsg.nba.correspondence.docprintschema;


import com.tbf.xml.*;

/**
 * NameAndDescAndCategoryAndDefaultDataSourceName class.
 *
 */
public class NameAndDescAndCategoryAndDefaultDataSourceName
	implements com.tbf.xml.XmlObject,
	com.tbf.xml.Validateable,
	java.io.Serializable {

	/**
	 * Constant for "Name" node name.
	 */
	public static final String $NAME = "Name";

	/**
	 * Constant for "Desc" node name.
	 */
	public static final String $DESC = "Desc";

	/**
	 * Constant for "Category" node name.
	 */
	public static final String $CATEGORY = "Category";

	/**
	 * Constant for "DefaultDataSourceName" node name.
	 */
	public static final String $DEFAULT_DATA_SOURCE_NAME = "DefaultDataSourceName";


	/**
	 * Declarations for the XML related fields.
	 */
	protected String _Name = null;
	protected String _Desc = null;
	protected String _Category = null;
	protected String _DefaultDataSourceName = null;


	/**
	 * Holds the parent object of this object.
	 */
	protected transient XmlObject _parent_ = null;


	/**
	 * Default no args constructor.
	 */
	public NameAndDescAndCategoryAndDefaultDataSourceName () {
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 */
	public NameAndDescAndCategoryAndDefaultDataSourceName (XmlElement xml) {
		unmarshal(xml);
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 * @param  parent  the containibg XmlObject
	 */
	public NameAndDescAndCategoryAndDefaultDataSourceName (XmlElement xml, XmlObject parent) {
		_parent_ = parent;
		unmarshal(xml);
	}


	/**
	 * Get the Name property.
	 */
	public String getName () {
		return (_Name);
	}


	/**
	 * Set the Name property.
	 */
	public void setName (String newValue) {
		_Name = newValue;
	}


	/**
	 * Checks for whether Name is set or not.
	 *
	 * @return true if Name is set, false if not
	 */
	 public boolean hasName () {
		return (_Name != null);
	}


	/**
	 * Discards Name's value.
	 */
	 public void deleteName () {
		_Name = null;
	}


	/**
	 * Get the Desc property.
	 */
	public String getDesc () {
		return (_Desc);
	}


	/**
	 * Set the Desc property.
	 */
	public void setDesc (String newValue) {
		_Desc = newValue;
	}


	/**
	 * Checks for whether Desc is set or not.
	 *
	 * @return true if Desc is set, false if not
	 */
	 public boolean hasDesc () {
		return (_Desc != null);
	}


	/**
	 * Discards Desc's value.
	 */
	 public void deleteDesc () {
		_Desc = null;
	}


	/**
	 * Get the Category property.
	 */
	public String getCategory () {
		return (_Category);
	}


	/**
	 * Set the Category property.
	 */
	public void setCategory (String newValue) {
		_Category = newValue;
	}


	/**
	 * Checks for whether Category is set or not.
	 *
	 * @return true if Category is set, false if not
	 */
	 public boolean hasCategory () {
		return (_Category != null);
	}


	/**
	 * Discards Category's value.
	 */
	 public void deleteCategory () {
		_Category = null;
	}


	/**
	 * Get the DefaultDataSourceName property.
	 */
	public String getDefaultDataSourceName () {
		return (_DefaultDataSourceName);
	}


	/**
	 * Set the DefaultDataSourceName property.
	 */
	public void setDefaultDataSourceName (String newValue) {
		_DefaultDataSourceName = newValue;
	}


	/**
	 * Checks for whether DefaultDataSourceName is set or not.
	 *
	 * @return true if DefaultDataSourceName is set, false if not
	 */
	 public boolean hasDefaultDataSourceName () {
		return (_DefaultDataSourceName != null);
	}


	/**
	 * Discards DefaultDataSourceName's value.
	 */
	 public void deleteDefaultDataSourceName () {
		_DefaultDataSourceName = null;
	}


	protected String _node_name_ = null;

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
		return (null);
	}


	/**
	 * Checks this object to see if it will produce valid XML.
	 */
	public boolean isValid () {
		return (true);
	}


	/**
	 * Checks each field on the object for validity and
	 * returns a Vector holding the validation errors.
	 */
	public java.util.Vector getValidationErrors () {
		return (null);
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
		return (null);
	}


	/**
	 * Checks the XML to see whether it matches the
	 * XML contents of this class.
	 */
	public static boolean matches (XmlElement xml, XmlObject parent) {

		if (xml == null) {
			return (false);
		}

		if (xml.matches($NAME, parent)) {
			return (true);
		}

		if (xml.matches($DESC, parent)) {
			return (true);
		}

		if (xml.matches($CATEGORY, parent)) {
			return (true);
		}

		if (xml.matches($DEFAULT_DATA_SOURCE_NAME, parent)) {
			return (true);
		}

		return (false);
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


		/*
		 * Save the passed in XmlElement, we use it later
		 */
		XmlElement saved_xml = xml;

		if (xml.matches($NAME, this)) {
			setName(xml.getData());
			saved_xml.setLastProcessed(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($DESC, this)) {
			setDesc(xml.getData());
			saved_xml.setLastProcessed(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($CATEGORY, this)) {
			setCategory(xml.getData());
			saved_xml.setLastProcessed(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($DEFAULT_DATA_SOURCE_NAME, this)) {
			setDefaultDataSourceName(xml.getData());
			saved_xml.setLastProcessed(xml);
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

		if (_node_name_ != null) {
			out.pushScope();
			out.writeStartTag(_node_name_, marshalAttributes(null), false);
			out.incrementIndent();
		}

		out.write($NAME,
			_Name);
		out.write($DESC,
			_Desc);
		out.write($CATEGORY,
			_Category);
		out.write($DEFAULT_DATA_SOURCE_NAME,
			_DefaultDataSourceName);

		if (_node_name_ != null) {
			out.decrementIndent();
			out.writeEndTag(_node_name_);
			out.popScope();
		}
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
}
