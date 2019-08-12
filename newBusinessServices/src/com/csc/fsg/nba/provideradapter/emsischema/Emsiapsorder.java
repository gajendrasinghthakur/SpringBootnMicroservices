/*
 * DO NOT EDIT!
 *
 * This file was generated by the Breeze XML Studio code generator.
 *
 *        Project: file:/C:/nbA/Requirement/EMSIAPS.xsd Binding
 *     Class Name: Emsiapsorder
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
import com.tbf.xml.XmlValidationError;
import com.tbf.xml.XmlValidator;

/**
 * Emsiapsorder class.
 *
 */
public class Emsiapsorder
	implements com.tbf.xml.XmlObject,
	com.tbf.xml.Validateable,
	java.io.Serializable {

	/**
	 * Constant for "Agent" node name.
	 */
	public static final String $AGENT = "Agent";

	/**
	 * Constant for "EMSIAPSOrder" node name.
	 */
	public static final String $EMSIAPSORDER = "EMSIAPSOrder";

	/**
	 * Constant for "APS" node name.
	 */
	public static final String $APS = "APS";

	/**
	 * Constant for "SourceInfo" node name.
	 */
	public static final String $SOURCE_INFO = "SourceInfo";

	/**
	 * Constant for "Carrier" node name.
	 */
	public static final String $CARRIER = "Carrier";

	/**
	 * Constant for "Applicant" node name.
	 */
	public static final String $APPLICANT = "Applicant";


	/**
	 * Declarations for the XML related fields.
	 */
	protected SourceInfo _SourceInfo = null;
	protected Carrier _Carrier = null;
	protected Applicant _Applicant = null;
	protected Agent _Agent = null;
	protected Aps _Aps = null;


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
	public Emsiapsorder () {
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 */
	public Emsiapsorder (XmlElement xml) {
		unmarshal(xml);
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 * @param  parent  the containibg XmlObject
	 */
	public Emsiapsorder (XmlElement xml, XmlObject parent) {
		_parent_ = parent;
		unmarshal(xml);
	}


	/**
	 * Get the SourceInfo property.
	 */
	public SourceInfo getSourceInfo () {
		return (_SourceInfo);
	}


	/**
	 * Set the SourceInfo property.
	 */
	public void setSourceInfo (SourceInfo obj) {
		_SourceInfo = obj;
	}


	private void setSourceInfo (XmlElement xml) {

		_SourceInfo =
			new SourceInfo(xml, this);
	}




	/**
	 * Checks for whether SourceInfo is set or not.
	 *
	 * @return true if SourceInfo is set, false if not
	 */
	 public boolean hasSourceInfo () {
		return (_SourceInfo != null);
	}


	/**
	 * Discards SourceInfo's value.
	 */
	 public void deleteSourceInfo () {
		_SourceInfo = null;
	}


	/**
	 * Get the Carrier property.
	 */
	public Carrier getCarrier () {
		return (_Carrier);
	}


	/**
	 * Set the Carrier property.
	 */
	public void setCarrier (Carrier obj) {
		_Carrier = obj;
	}


	private void setCarrier (XmlElement xml) {

		_Carrier =
			new Carrier(xml, this);
	}




	/**
	 * Checks for whether Carrier is set or not.
	 *
	 * @return true if Carrier is set, false if not
	 */
	 public boolean hasCarrier () {
		return (_Carrier != null);
	}


	/**
	 * Discards Carrier's value.
	 */
	 public void deleteCarrier () {
		_Carrier = null;
	}


	/**
	 * Get the Applicant property.
	 */
	public Applicant getApplicant () {
		return (_Applicant);
	}


	/**
	 * Set the Applicant property.
	 */
	public void setApplicant (Applicant obj) {
		_Applicant = obj;
	}


	private void setApplicant (XmlElement xml) {

		_Applicant =
			new Applicant(xml, this);
	}




	/**
	 * Checks for whether Applicant is set or not.
	 *
	 * @return true if Applicant is set, false if not
	 */
	 public boolean hasApplicant () {
		return (_Applicant != null);
	}


	/**
	 * Discards Applicant's value.
	 */
	 public void deleteApplicant () {
		_Applicant = null;
	}


	/**
	 * Get the Agent property.
	 */
	public Agent getAgent () {
		return (_Agent);
	}


	/**
	 * Set the Agent property.
	 */
	public void setAgent (Agent obj) {
		_Agent = obj;
	}


	private void setAgent (XmlElement xml) {

		_Agent =
			new Agent(xml, this);
	}




	/**
	 * Checks for whether Agent is set or not.
	 *
	 * @return true if Agent is set, false if not
	 */
	 public boolean hasAgent () {
		return (_Agent != null);
	}


	/**
	 * Discards Agent's value.
	 */
	 public void deleteAgent () {
		_Agent = null;
	}


	/**
	 * Get the Aps property.
	 */
	public Aps getAps () {
		return (_Aps);
	}


	/**
	 * Set the Aps property.
	 */
	public void setAps (Aps obj) {
		_Aps = obj;
	}


	private void setAps (XmlElement xml) {

		_Aps =
			new Aps(xml, this);
	}




	/**
	 * Checks for whether Aps is set or not.
	 *
	 * @return true if Aps is set, false if not
	 */
	 public boolean hasAps () {
		return (_Aps != null);
	}


	/**
	 * Discards Aps's value.
	 */
	 public void deleteAps () {
		_Aps = null;
	}


	protected String _node_name_ = $EMSIAPSORDER;

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
		return ($EMSIAPSORDER);
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
		_SourceInfo_validator_ = null;
	transient protected static XmlValidator 
		_Carrier_validator_ = null;
	transient protected static XmlValidator 
		_Applicant_validator_ = null;
	transient protected static XmlValidator 
		_Agent_validator_ = null;
	transient protected static XmlValidator 
		_Aps_validator_ = null;

	/**
	 * Create the validators for this class.
	 */
	protected static synchronized void createValidators () {

		if (_validators_created) {
			return;
		}

		_SourceInfo_validator_ = new XmlValidator(
			"Emsiapsorder.SourceInfo", "Element", 
			"EMSIAPSOrder/SourceInfo", 1, 1);

		_Carrier_validator_ = new XmlValidator(
			"Emsiapsorder.Carrier", "Element", 
			"EMSIAPSOrder/Carrier", 1, 1);

		_Applicant_validator_ = new XmlValidator(
			"Emsiapsorder.Applicant", "Element", 
			"EMSIAPSOrder/Applicant", 1, 1);

		_Agent_validator_ = new XmlValidator(
			"Emsiapsorder.Agent", "Element", 
			"EMSIAPSOrder/Agent", 1, 1);

		_Aps_validator_ = new XmlValidator(
			"Emsiapsorder.Aps", "Element", 
			"EMSIAPSOrder/APS", 1, 1);

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

		is_valid = _SourceInfo_validator_.isValid(
			_SourceInfo,
			errors, return_on_error, traverse);
		if (!is_valid && return_on_error) {
			return (errors);
		}

		is_valid = _Carrier_validator_.isValid(
			_Carrier,
			errors, return_on_error, traverse);
		if (!is_valid && return_on_error) {
			return (errors);
		}

		is_valid = _Applicant_validator_.isValid(
			_Applicant,
			errors, return_on_error, traverse);
		if (!is_valid && return_on_error) {
			return (errors);
		}

		is_valid = _Agent_validator_.isValid(
			_Agent,
			errors, return_on_error, traverse);
		if (!is_valid && return_on_error) {
			return (errors);
		}

		is_valid = _Aps_validator_.isValid(
			_Aps,
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

		return (xml.matches($EMSIAPSORDER, nsm, parent));
	}


	/**
	 * This method unmarshals an XML document instance
	 * into an instance of this class.
	 */
	public static Emsiapsorder unmarshal (
			InputStream in) throws Exception {

		Emsiapsorder obj = new Emsiapsorder();
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

		if (!xml.matches($EMSIAPSORDER, this)) {
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

		if (xml.matches($SOURCE_INFO,
			SourceInfo.nsm, this)) {

			setSourceInfo(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($CARRIER,
			Carrier.nsm, this)) {

			setCarrier(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($APPLICANT,
			Applicant.nsm, this)) {

			setApplicant(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($AGENT,
			Agent.nsm, this)) {

			setAgent(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($APS,
			Aps.nsm, this)) {

			setAps(xml);
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
			getSourceInfo());
		out.write(null,
			getCarrier());
		out.write(null,
			getApplicant());
		out.write(null,
			getAgent());
		out.write(null,
			getAps());

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