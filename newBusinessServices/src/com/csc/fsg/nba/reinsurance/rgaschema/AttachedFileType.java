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
import com.tbf.xml.XmlObject;
import com.tbf.xml.XmlOutputStream;
import com.tbf.xml.XmlStringValidator;
import com.tbf.xml.XmlValidationError;
import com.tbf.xml.XmlValidator;

/**
 * AttachedFileType class.
 *
 *
 * Elements for Attached File (Type)
 */
public class AttachedFileType
	implements com.tbf.xml.XmlObject,
	com.tbf.xml.Validateable,
	java.io.Serializable {

	/**
	 * Constant for "FileName" node name.
	 */
	public static final String $FILE_NAME = "FileName";

	/**
	 * Constant for "File" node name.
	 */
	public static final String $FILE = "File";


	/**
	 * Declarations for the XML related fields.
	 */
	protected String _FileName = null;
	protected byte [] _File = null;
	protected int _FileLength = -1;


	/**
	 * Holds the parent object of this object.
	 */
	protected transient XmlObject _parent_ = null;


	/**
	 * Default no args constructor.
	 */
	public AttachedFileType () {
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 */
	public AttachedFileType (XmlElement xml) {
		unmarshal(xml);
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 * @param  parent  the containibg XmlObject
	 */
	public AttachedFileType (XmlElement xml, XmlObject parent) {
		_parent_ = parent;
		unmarshal(xml);
	}


	/**
	 * Get the FileName property.
	 */
	public String getFileName () {
		return (_FileName);
	}


	/**
	 * Set the FileName property.
	 */
	public void setFileName (String newValue) {
		_FileName = newValue;
	}


	/**
	 * Checks for whether FileName is set or not.
	 *
	 * @return true if FileName is set, false if not
	 */
	 public boolean hasFileName () {
		return (_FileName != null);
	}


	/**
	 * Discards FileName's value.
	 */
	 public void deleteFileName () {
		_FileName = null;
	}


	/**
	 * Get the File property.
	 */
	public byte[] getFile () {
		return (_File);
	}


	/**
	 * Get the length of the dynamic data in the File property.
	 */
	public int getFileLength () {
		return (_FileLength);
	}


	/**
	 * Set the File property.
	 */
	public void setFile (byte buf[]) {
		_File = buf;
		if (buf != null) {
			_FileLength = buf.length;
		}
	}


	/**
	 * Checks for whether File is set or not.
	 *
	 * @return true if File is set, false if not
	 */
	 public boolean hasFile () {
		return (_File != null);
	}


	/**
	 * Discards File's value.
	 */
	 public void deleteFile () {
		_File = null;
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
	 * This flag is used to used to check whether
	 * the validators have been created.
	 */
	transient protected static boolean _validators_created = false;

	/*
	 * XML Validators
	 */
	transient protected static XmlStringValidator 
		_FileName_validator_ = null;
	transient protected static XmlValidator 
		_File_validator_ = null;

	/**
	 * Create the validators for this class.
	 */
	protected static synchronized void createValidators () {

		if (_validators_created) {
			return;
		}

		_FileName_validator_ = new XmlStringValidator(
			"AttachedFileType.FileName", "Element", 
			"FileName", -1, -1, 1, 1);

		_File_validator_ = new XmlValidator(
			"AttachedFileType.File", "Element", 
			"File", 1, 1);

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
		e = _FileName_validator_.validate(
			_FileName);
		if (e != null) {
			errors.addElement(e);
			if (return_on_error) {
				return (errors);
			}
		}

		e = _File_validator_.validate(
			_File);
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

		return (xml.matches($FILE_NAME, parent));
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

		if (xml.matches($FILE_NAME, this)) {
			setFileName(xml.getData());
			saved_xml.setLastProcessed(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($FILE, this)) {
			byte _File_buf_[] = xml.getDdatFromXml(-1);
			setFile(_File_buf_);
			saved_xml.setLastProcessed(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
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

		out.write($FILE_NAME,
			_FileName);
		out.write(
			$FILE, getFile(), getFileLength());

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
