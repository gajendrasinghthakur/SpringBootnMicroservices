/*
 * DO NOT EDIT!
 *
 * This file was generated by the Breeze XML Studio code generator.
 *
 *        Project: file:/C:/nbA/Requirement/EMSIAPS.xsd Binding
 *     Class Name: SourceInfo
 *           Date: Mon Nov 11 14:43:37 CST 2002
 * Breeze Version: 3.0.0 build 382
 *
 * IMPORTANT: Please see your Breeze license for more information on
 *            where and how this generated code may be used.
 *
 */

package com.csc.fsg.nba.provideradapter.emsischema;


import java.io.InputStream;
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
 * SourceInfo class.
 *
 */
public class SourceInfo
	implements com.tbf.xml.XmlObject,
	com.tbf.xml.Validateable,
	java.io.Serializable {

	/**
	 * Constant for "SourceInfoComment" node name.
	 */
	public static final String $SOURCE_INFO_COMMENT = "SourceInfoComment";

	/**
	 * Constant for "CreationDate" node name.
	 */
	public static final String $CREATION_DATE = "CreationDate";

	/**
	 * Constant for "SourceInfo" node name.
	 */
	public static final String $SOURCE_INFO = "SourceInfo";

	/**
	 * Constant for "Password" node name.
	 */
	public static final String $PASSWORD = "Password";

	/**
	 * Constant for "SourceInfoDescription" node name.
	 */
	public static final String $SOURCE_INFO_DESCRIPTION = "SourceInfoDescription";

	/**
	 * Constant for "CreationTime" node name.
	 */
	public static final String $CREATION_TIME = "CreationTime";

	/**
	 * Constant for "UserName" node name.
	 */
	public static final String $USER_NAME = "UserName";

	/**
	 * Constant for "SourceInfoName" node name.
	 */
	public static final String $SOURCE_INFO_NAME = "SourceInfoName";


	/**
	 * Declarations for the XML related fields.
	 */
	protected java.util.Date _CreationDate = null;
	protected java.util.Date _CreationTime = null;
	protected String _SourceInfoName = null;
	protected String _SourceInfoDescription = null;
	protected String _SourceInfoComment = null;
	protected String _UserName = null;
	protected String _Password = null;


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
	public SourceInfo () {
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 */
	public SourceInfo (XmlElement xml) {
		unmarshal(xml);
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 * @param  parent  the containibg XmlObject
	 */
	public SourceInfo (XmlElement xml, XmlObject parent) {
		_parent_ = parent;
		unmarshal(xml);
	}


	/**
	 * Get the CreationDate property.
	 */
	public java.util.Date getCreationDate () {
		return (_CreationDate);
	}


	/**
	 * Set the CreationDate property.
	 */
	public void setCreationDate (java.util.Date timedate) {
		_CreationDate = timedate;
		_format_errors.remove("SourceInfo.CreationDate");
	}


	public void setCreationDate (String str) {

		if (str == null) {
			_CreationDate = null;
			return;
		}

		_CreationDate = com.tbf.util.DateUtilities.stringToDate(str, 
			com.tbf.util.ISO8601DateFormat.EXTENDED_DATE);
		if (_CreationDate == null) {
			XmlValidationError.addValidityFormatError(
				_format_errors, "SourceInfo.CreationDate", "Element",
				"SourceInfo/CreationDate", str);
		} else {
			_format_errors.remove("SourceInfo.CreationDate");
		}
	}


	/**
	 * Checks for whether CreationDate is set or not.
	 *
	 * @return true if CreationDate is set, false if not
	 */
	 public boolean hasCreationDate () {
		return (_CreationDate != null);
	}


	/**
	 * Discards CreationDate's value.
	 */
	 public void deleteCreationDate () {
		_CreationDate = null;
		_format_errors.remove("SourceInfo.CreationDate");
	}


	/**
	 * Get the CreationTime property.
	 */
	public java.util.Date getCreationTime () {
		return (_CreationTime);
	}


	/**
	 * Set the CreationTime property.
	 */
	public void setCreationTime (java.util.Date timedate) {
		_CreationTime = timedate;
		_format_errors.remove("SourceInfo.CreationTime");
	}


	public void setCreationTime (String str) {

		if (str == null) {
			_CreationTime = null;
			return;
		}

		_CreationTime = com.tbf.util.DateUtilities.stringToDate(str, 
			com.tbf.util.ISO8601DateFormat.EXTENDED_TIME);
		if (_CreationTime == null) {
			XmlValidationError.addValidityFormatError(
				_format_errors, "SourceInfo.CreationTime", "Element",
				"SourceInfo/CreationTime", str);
		} else {
			_format_errors.remove("SourceInfo.CreationTime");
		}
	}


	/**
	 * Checks for whether CreationTime is set or not.
	 *
	 * @return true if CreationTime is set, false if not
	 */
	 public boolean hasCreationTime () {
		return (_CreationTime != null);
	}


	/**
	 * Discards CreationTime's value.
	 */
	 public void deleteCreationTime () {
		_CreationTime = null;
		_format_errors.remove("SourceInfo.CreationTime");
	}


	/**
	 * Get the SourceInfoName property.
	 */
	public String getSourceInfoName () {
		return (_SourceInfoName);
	}


	/**
	 * Set the SourceInfoName property.
	 */
	public void setSourceInfoName (String newValue) {
		_SourceInfoName = newValue;
	}


	/**
	 * Checks for whether SourceInfoName is set or not.
	 *
	 * @return true if SourceInfoName is set, false if not
	 */
	 public boolean hasSourceInfoName () {
		return (_SourceInfoName != null);
	}


	/**
	 * Discards SourceInfoName's value.
	 */
	 public void deleteSourceInfoName () {
		_SourceInfoName = null;
	}


	/**
	 * Get the SourceInfoDescription property.
	 */
	public String getSourceInfoDescription () {
		return (_SourceInfoDescription);
	}


	/**
	 * Set the SourceInfoDescription property.
	 */
	public void setSourceInfoDescription (String newValue) {
		_SourceInfoDescription = newValue;
	}


	/**
	 * Checks for whether SourceInfoDescription is set or not.
	 *
	 * @return true if SourceInfoDescription is set, false if not
	 */
	 public boolean hasSourceInfoDescription () {
		return (_SourceInfoDescription != null);
	}


	/**
	 * Discards SourceInfoDescription's value.
	 */
	 public void deleteSourceInfoDescription () {
		_SourceInfoDescription = null;
	}


	/**
	 * Get the SourceInfoComment property.
	 */
	public String getSourceInfoComment () {
		return (_SourceInfoComment);
	}


	/**
	 * Set the SourceInfoComment property.
	 */
	public void setSourceInfoComment (String newValue) {
		_SourceInfoComment = newValue;
	}


	/**
	 * Checks for whether SourceInfoComment is set or not.
	 *
	 * @return true if SourceInfoComment is set, false if not
	 */
	 public boolean hasSourceInfoComment () {
		return (_SourceInfoComment != null);
	}


	/**
	 * Discards SourceInfoComment's value.
	 */
	 public void deleteSourceInfoComment () {
		_SourceInfoComment = null;
	}


	/**
	 * Get the UserName property.
	 */
	public String getUserName () {
		return (_UserName);
	}


	/**
	 * Set the UserName property.
	 */
	public void setUserName (String newValue) {
		_UserName = newValue;
	}


	/**
	 * Checks for whether UserName is set or not.
	 *
	 * @return true if UserName is set, false if not
	 */
	 public boolean hasUserName () {
		return (_UserName != null);
	}


	/**
	 * Discards UserName's value.
	 */
	 public void deleteUserName () {
		_UserName = null;
	}


	/**
	 * Get the Password property.
	 */
	public String getPassword () {
		return (_Password);
	}


	/**
	 * Set the Password property.
	 */
	public void setPassword (String newValue) {
		_Password = newValue;
	}


	/**
	 * Checks for whether Password is set or not.
	 *
	 * @return true if Password is set, false if not
	 */
	 public boolean hasPassword () {
		return (_Password != null);
	}


	/**
	 * Discards Password's value.
	 */
	 public void deletePassword () {
		_Password = null;
	}


	protected String _node_name_ = $SOURCE_INFO;

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
		return ($SOURCE_INFO);
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
		_CreationDate_validator_ = null;
	transient protected static XmlValidator 
		_CreationTime_validator_ = null;
	transient protected static XmlStringValidator 
		_SourceInfoName_validator_ = null;
	transient protected static XmlStringValidator 
		_SourceInfoDescription_validator_ = null;
	transient protected static XmlStringValidator 
		_SourceInfoComment_validator_ = null;
	transient protected static XmlStringValidator 
		_UserName_validator_ = null;
	transient protected static XmlStringValidator 
		_Password_validator_ = null;

	/**
	 * Create the validators for this class.
	 */
	protected static synchronized void createValidators () {

		if (_validators_created) {
			return;
		}

		_CreationDate_validator_ = new XmlValidator(
			"SourceInfo.CreationDate", "Element", 
			"SourceInfo/CreationDate", 1, 1);

		_CreationTime_validator_ = new XmlValidator(
			"SourceInfo.CreationTime", "Element", 
			"SourceInfo/CreationTime", 1, 1);

		_SourceInfoName_validator_ = new XmlStringValidator(
			"SourceInfo.SourceInfoName", "Element", 
			"SourceInfo/SourceInfoName", -1, -1, 1, 1);

		_SourceInfoDescription_validator_ = new XmlStringValidator(
			"SourceInfo.SourceInfoDescription", "Element", 
			"SourceInfo/SourceInfoDescription", -1, -1, 1, 1);

		_SourceInfoComment_validator_ = new XmlStringValidator(
			"SourceInfo.SourceInfoComment", "Element", 
			"SourceInfo/SourceInfoComment", -1, -1, 1, 1);

		_UserName_validator_ = new XmlStringValidator(
			"SourceInfo.UserName", "Element", 
			"SourceInfo/UserName", -1, -1, 1, 1);

		_Password_validator_ = new XmlStringValidator(
			"SourceInfo.Password", "Element", 
			"SourceInfo/Password", -1, -1, 1, 1);

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
		e = _CreationDate_validator_.validate(
			_CreationDate, _format_errors);
		if (e != null) {
			errors.addElement(e);
			if (return_on_error) {
				return (errors);
			}
		}

		e = _CreationTime_validator_.validate(
			_CreationTime, _format_errors);
		if (e != null) {
			errors.addElement(e);
			if (return_on_error) {
				return (errors);
			}
		}

		e = _SourceInfoName_validator_.validate(
			_SourceInfoName);
		if (e != null) {
			errors.addElement(e);
			if (return_on_error) {
				return (errors);
			}
		}

		e = _SourceInfoDescription_validator_.validate(
			_SourceInfoDescription);
		if (e != null) {
			errors.addElement(e);
			if (return_on_error) {
				return (errors);
			}
		}

		e = _SourceInfoComment_validator_.validate(
			_SourceInfoComment);
		if (e != null) {
			errors.addElement(e);
			if (return_on_error) {
				return (errors);
			}
		}

		e = _UserName_validator_.validate(
			_UserName);
		if (e != null) {
			errors.addElement(e);
			if (return_on_error) {
				return (errors);
			}
		}

		e = _Password_validator_.validate(
			_Password);
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

		return (xml.matches($SOURCE_INFO, nsm, parent));
	}


	/**
	 * This method unmarshals an XML document instance
	 * into an instance of this class.
	 */
	public static SourceInfo unmarshal (
			InputStream in) throws Exception {

		SourceInfo obj = new SourceInfo();
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

		if (!xml.matches($SOURCE_INFO, this)) {
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

		if (xml.matches($CREATION_DATE, this)) {
			setCreationDate(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($CREATION_TIME, this)) {
			setCreationTime(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($SOURCE_INFO_NAME, this)) {
			setSourceInfoName(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($SOURCE_INFO_DESCRIPTION, this)) {
			setSourceInfoDescription(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($SOURCE_INFO_COMMENT, this)) {
			setSourceInfoComment(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($USER_NAME, this)) {
			setUserName(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($PASSWORD, this)) {
			setPassword(xml.getData());
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
	public void toXml (OutputStream stream) {
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
	public void toXml (OutputStream stream, boolean embed_files) {

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
			OutputStream stream, String indent, boolean embed_files)	{

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
	public void marshal (OutputStream stream) {

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

		out.write($CREATION_DATE,
			com.tbf.util.DateUtilities.getFormattedDate(getCreationDate(),
			com.tbf.util.ISO8601DateFormat.EXTENDED_DATE));
		out.write($CREATION_TIME,
			com.tbf.util.DateUtilities.getFormattedDate(getCreationTime(),
			com.tbf.util.ISO8601DateFormat.EXTENDED_TIME));
		out.write($SOURCE_INFO_NAME,
			_SourceInfoName);
		out.write($SOURCE_INFO_DESCRIPTION,
			_SourceInfoDescription);
		out.write($SOURCE_INFO_COMMENT,
			_SourceInfoComment);
		out.write($USER_NAME,
			_UserName);
		out.write($PASSWORD,
			_Password);

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