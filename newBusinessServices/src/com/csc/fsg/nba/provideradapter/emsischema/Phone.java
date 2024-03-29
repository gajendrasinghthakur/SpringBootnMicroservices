/*
 * DO NOT EDIT!
 *
 * This file was generated by the Breeze XML Studio code generator.
 *
 *        Project: file:/C:/nbA/Requirement/EMSIAPS.xsd Binding
 *     Class Name: Phone
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
import com.tbf.xml.XmlIntegerValidator;
import com.tbf.xml.XmlNamespaceManager;
import com.tbf.xml.XmlObject;
import com.tbf.xml.XmlOutputStream;
import com.tbf.xml.XmlValidationError;
import com.tbf.xml.XmlValidator;

/**
 * Phone class.
 *
 */
public class Phone
	implements com.tbf.xml.XmlObject,
	com.tbf.xml.Validateable,
	java.io.Serializable {

	/**
	 * Constant for "AreaCode" node name.
	 */
	public static final String $AREA_CODE = "AreaCode";

	/**
	 * Constant for "Prefix" node name.
	 */
	public static final String $PREFIX = "Prefix";

	/**
	 * Constant for "Home" node name.
	 */
	public static final String $HOME = "Home";

	/**
	 * Constant for "Suffix" node name.
	 */
	public static final String $SUFFIX = "Suffix";

	/**
	 * Constant for "Phone" node name.
	 */
	public static final String $PHONE = "Phone";

	/**
	 * Constant for "Work" node name.
	 */
	public static final String $WORK = "Work";

	/**
	 * Constant for "Extension" node name.
	 */
	public static final String $EXTENSION = "Extension";


	/**
	 * Declarations for the XML related fields.
	 */
	protected Work _Work = null;
	protected Home _Home = null;
	protected int _AreaCode = -1;
	protected boolean _has_AreaCode_ = false;
	protected int _Prefix = -1;
	protected boolean _has_Prefix_ = false;
	protected int _Suffix = -1;
	protected boolean _has_Suffix_ = false;
	protected int _Extension = -1;
	protected boolean _has_Extension_ = false;


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
	public Phone () {
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 */
	public Phone (XmlElement xml) {
		unmarshal(xml);
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 * @param  parent  the containibg XmlObject
	 */
	public Phone (XmlElement xml, XmlObject parent) {
		_parent_ = parent;
		unmarshal(xml);
	}


	/**
	 * Get the Work property.
	 */
	public Work getWork () {
		return (_Work);
	}


	/**
	 * Set the Work property.
	 */
	public void setWork (Work obj) {
		_Work = obj;
	}


	private void setWork (XmlElement xml) {

		_Work =
			new Work(xml, this);
	}




	/**
	 * Checks for whether Work is set or not.
	 *
	 * @return true if Work is set, false if not
	 */
	 public boolean hasWork () {
		return (_Work != null);
	}


	/**
	 * Discards Work's value.
	 */
	 public void deleteWork () {
		_Work = null;
	}


	/**
	 * Get the Home property.
	 */
	public Home getHome () {
		return (_Home);
	}


	/**
	 * Set the Home property.
	 */
	public void setHome (Home obj) {
		_Home = obj;
	}


	private void setHome (XmlElement xml) {

		_Home =
			new Home(xml, this);
	}




	/**
	 * Checks for whether Home is set or not.
	 *
	 * @return true if Home is set, false if not
	 */
	 public boolean hasHome () {
		return (_Home != null);
	}


	/**
	 * Discards Home's value.
	 */
	 public void deleteHome () {
		_Home = null;
	}


	/**
	 * Get the AreaCode property.
	 */
	public int getAreaCode () {
		if (_has_AreaCode_) {
			return (_AreaCode);
		}

		return (-1);
	}


	/**
	 * Set the AreaCode property.
	 */
	public void setAreaCode (int new_value) {
		_AreaCode = new_value;
		_has_AreaCode_ = true;
		_format_errors.remove("Phone.AreaCode");
	}


	public void setAreaCode (String new_value) {

		if (new_value == null) {
			_AreaCode = -1;
			_has_AreaCode_ = false;
			return;
		}

		try {
			_AreaCode = Integer.parseInt(new_value);
			_has_AreaCode_ = true;
			_format_errors.remove("Phone.AreaCode");
		} catch (NumberFormatException nfe) {
			_has_AreaCode_ = false;
			XmlValidationError.addValidityFormatError(
				_format_errors, "Phone.AreaCode", "Element",
				"Phone/AreaCode", new_value);
		}
	}


	/**
	 * Checks for whether AreaCode is set or not.
	 *
	 * @return true if AreaCode is set, false if not
	 */
	public boolean hasAreaCode () {
		return (_has_AreaCode_);
	}


	/**
	 * Discards AreaCode's value.
	 */
	public void deleteAreaCode () {
		_has_AreaCode_ = false;
		_format_errors.remove("Phone.AreaCode");
	}


	/**
	 * Get the Prefix property.
	 */
	public int getPrefix () {
		if (_has_Prefix_) {
			return (_Prefix);
		}

		return (-1);
	}


	/**
	 * Set the Prefix property.
	 */
	public void setPrefix (int new_value) {
		_Prefix = new_value;
		_has_Prefix_ = true;
		_format_errors.remove("Phone.Prefix");
	}


	public void setPrefix (String new_value) {

		if (new_value == null) {
			_Prefix = -1;
			_has_Prefix_ = false;
			return;
		}

		try {
			_Prefix = Integer.parseInt(new_value);
			_has_Prefix_ = true;
			_format_errors.remove("Phone.Prefix");
		} catch (NumberFormatException nfe) {
			_has_Prefix_ = false;
			XmlValidationError.addValidityFormatError(
				_format_errors, "Phone.Prefix", "Element",
				"Phone/Prefix", new_value);
		}
	}


	/**
	 * Checks for whether Prefix is set or not.
	 *
	 * @return true if Prefix is set, false if not
	 */
	public boolean hasPrefix () {
		return (_has_Prefix_);
	}


	/**
	 * Discards Prefix's value.
	 */
	public void deletePrefix () {
		_has_Prefix_ = false;
		_format_errors.remove("Phone.Prefix");
	}


	/**
	 * Get the Suffix property.
	 */
	public int getSuffix () {
		if (_has_Suffix_) {
			return (_Suffix);
		}

		return (-1);
	}


	/**
	 * Set the Suffix property.
	 */
	public void setSuffix (int new_value) {
		_Suffix = new_value;
		_has_Suffix_ = true;
		_format_errors.remove("Phone.Suffix");
	}


	public void setSuffix (String new_value) {

		if (new_value == null) {
			_Suffix = -1;
			_has_Suffix_ = false;
			return;
		}

		try {
			_Suffix = Integer.parseInt(new_value);
			_has_Suffix_ = true;
			_format_errors.remove("Phone.Suffix");
		} catch (NumberFormatException nfe) {
			_has_Suffix_ = false;
			XmlValidationError.addValidityFormatError(
				_format_errors, "Phone.Suffix", "Element",
				"Phone/Suffix", new_value);
		}
	}


	/**
	 * Checks for whether Suffix is set or not.
	 *
	 * @return true if Suffix is set, false if not
	 */
	public boolean hasSuffix () {
		return (_has_Suffix_);
	}


	/**
	 * Discards Suffix's value.
	 */
	public void deleteSuffix () {
		_has_Suffix_ = false;
		_format_errors.remove("Phone.Suffix");
	}


	/**
	 * Get the Extension property.
	 */
	public int getExtension () {
		if (_has_Extension_) {
			return (_Extension);
		}

		return (-1);
	}


	/**
	 * Set the Extension property.
	 */
	public void setExtension (int new_value) {
		_Extension = new_value;
		_has_Extension_ = true;
		_format_errors.remove("Phone.Extension");
	}


	public void setExtension (String new_value) {

		if (new_value == null) {
			_Extension = -1;
			_has_Extension_ = false;
			return;
		}

		try {
			_Extension = Integer.parseInt(new_value);
			_has_Extension_ = true;
			_format_errors.remove("Phone.Extension");
		} catch (NumberFormatException nfe) {
			_has_Extension_ = false;
			XmlValidationError.addValidityFormatError(
				_format_errors, "Phone.Extension", "Element",
				"Phone/Extension", new_value);
		}
	}


	/**
	 * Checks for whether Extension is set or not.
	 *
	 * @return true if Extension is set, false if not
	 */
	public boolean hasExtension () {
		return (_has_Extension_);
	}


	/**
	 * Discards Extension's value.
	 */
	public void deleteExtension () {
		_has_Extension_ = false;
		_format_errors.remove("Phone.Extension");
	}


	protected String _node_name_ = $PHONE;

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
		return ($PHONE);
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
		_Work_validator_ = null;
	transient protected static XmlValidator 
		_Home_validator_ = null;
	transient protected static XmlIntegerValidator
		_AreaCode_validator_ = null;
	transient protected static XmlIntegerValidator
		_Prefix_validator_ = null;
	transient protected static XmlIntegerValidator
		_Suffix_validator_ = null;
	transient protected static XmlIntegerValidator
		_Extension_validator_ = null;

	/**
	 * Create the validators for this class.
	 */
	protected static synchronized void createValidators () {

		if (_validators_created) {
			return;
		}

		_Work_validator_ = new XmlValidator(
			"Phone.Work", "Element", 
			"Phone/Work", 1, 1);

		_Home_validator_ = new XmlValidator(
			"Phone.Home", "Element", 
			"Phone/Home", 1, 1);

		_AreaCode_validator_ = new XmlIntegerValidator(
			"Phone.AreaCode", "Element",
			"Phone/AreaCode",
			0, XmlValidator.NOT_USED,
			0, XmlValidator.NOT_USED, 1, 1);

		_Prefix_validator_ = new XmlIntegerValidator(
			"Phone.Prefix", "Element",
			"Phone/Prefix",
			0, XmlValidator.NOT_USED,
			0, XmlValidator.NOT_USED, 1, 1);

		_Suffix_validator_ = new XmlIntegerValidator(
			"Phone.Suffix", "Element",
			"Phone/Suffix",
			0, XmlValidator.NOT_USED,
			0, XmlValidator.NOT_USED, 1, 1);

		_Extension_validator_ = new XmlIntegerValidator(
			"Phone.Extension", "Element",
			"Phone/Extension",
			0, XmlValidator.NOT_USED,
			0, XmlValidator.NOT_USED, 1, 1);

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
		e = _AreaCode_validator_.validate(
			_AreaCode,
			_has_AreaCode_, _format_errors);
		if (e != null) {
			errors.addElement(e);
			if (return_on_error) {
				return (errors);
			}
		}

		e = _Prefix_validator_.validate(
			_Prefix,
			_has_Prefix_, _format_errors);
		if (e != null) {
			errors.addElement(e);
			if (return_on_error) {
				return (errors);
			}
		}

		e = _Suffix_validator_.validate(
			_Suffix,
			_has_Suffix_, _format_errors);
		if (e != null) {
			errors.addElement(e);
			if (return_on_error) {
				return (errors);
			}
		}

		e = _Extension_validator_.validate(
			_Extension,
			_has_Extension_, _format_errors);
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

		is_valid = _Work_validator_.isValid(
			_Work,
			errors, return_on_error, traverse);
		if (!is_valid && return_on_error) {
			return (errors);
		}

		is_valid = _Home_validator_.isValid(
			_Home,
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

		return (xml.matches($PHONE, nsm, parent));
	}


	/**
	 * This method unmarshals an XML document instance
	 * into an instance of this class.
	 */
	public static Phone unmarshal (
			InputStream in) throws Exception {

		Phone obj = new Phone();
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

		if (!xml.matches($PHONE, this)) {
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

		if (xml.matches($WORK,
			Work.nsm, this)) {

			setWork(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($HOME,
			Home.nsm, this)) {

			setHome(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($AREA_CODE, this)) {
			setAreaCode(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($PREFIX, this)) {
			setPrefix(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($SUFFIX, this)) {
			setSuffix(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($EXTENSION, this)) {
			setExtension(xml.getData());
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

		out.write(null,
			getWork());
		out.write(null,
			getHome());
		out.write($AREA_CODE,
			_AreaCode, _has_AreaCode_);
		out.write($PREFIX,
			_Prefix, _has_Prefix_);
		out.write($SUFFIX,
			_Suffix, _has_Suffix_);
		out.write($EXTENSION,
			_Extension, _has_Extension_);

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
