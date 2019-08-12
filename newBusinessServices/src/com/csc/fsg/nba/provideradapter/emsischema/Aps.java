/*
 * DO NOT EDIT!
 *
 * This file was generated by the Breeze XML Studio code generator.
 *
 *        Project: file:/C:/nbA/Requirement/EMSIAPS.xsd Binding
 *     Class Name: Aps
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
 * Aps class.
 *
 */
public class Aps
	implements com.tbf.xml.XmlObject,
	com.tbf.xml.Validateable,
	java.io.Serializable {

	/**
	 * Constant for "Location" node name.
	 */
	public static final String $LOCATION = "Location";

	/**
	 * Constant for "APS" node name.
	 */
	public static final String $APS = "APS";

	/**
	 * Constant for "AgentID" node name.
	 */
	public static final String $AGENT_ID = "AgentID";

	/**
	 * Constant for "Account" node name.
	 */
	public static final String $ACCOUNT = "Account";

	/**
	 * Constant for "ScannedAuthorization" node name.
	 */
	public static final String $SCANNED_AUTHORIZATION = "ScannedAuthorization";

	/**
	 * Constant for "SpecialInstructions" node name.
	 */
	public static final String $SPECIAL_INSTRUCTIONS = "SpecialInstructions";


	/**
	 * Declarations for the XML related fields.
	 */
	protected String _Account = null;
	protected String _AgentId = null;
	protected String _SpecialInstructions = null;
	protected ScannedAuthorization _ScannedAuthorization = null;
	protected Location _Location = null;


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
	public Aps () {
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 */
	public Aps (XmlElement xml) {
		unmarshal(xml);
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 * @param  parent  the containibg XmlObject
	 */
	public Aps (XmlElement xml, XmlObject parent) {
		_parent_ = parent;
		unmarshal(xml);
	}


	/**
	 * Get the Account property.
	 */
	public String getAccount () {
		return (_Account);
	}


	/**
	 * Set the Account property.
	 */
	public void setAccount (String newValue) {
		_Account = newValue;
	}


	/**
	 * Checks for whether Account is set or not.
	 *
	 * @return true if Account is set, false if not
	 */
	 public boolean hasAccount () {
		return (_Account != null);
	}


	/**
	 * Discards Account's value.
	 */
	 public void deleteAccount () {
		_Account = null;
	}


	/**
	 * Get the AgentId property.
	 */
	public String getAgentId () {
		return (_AgentId);
	}


	/**
	 * Set the AgentId property.
	 */
	public void setAgentId (String newValue) {
		_AgentId = newValue;
	}


	/**
	 * Checks for whether AgentId is set or not.
	 *
	 * @return true if AgentId is set, false if not
	 */
	 public boolean hasAgentId () {
		return (_AgentId != null);
	}


	/**
	 * Discards AgentId's value.
	 */
	 public void deleteAgentId () {
		_AgentId = null;
	}


	/**
	 * Get the SpecialInstructions property.
	 */
	public String getSpecialInstructions () {
		return (_SpecialInstructions);
	}


	/**
	 * Set the SpecialInstructions property.
	 */
	public void setSpecialInstructions (String newValue) {
		_SpecialInstructions = newValue;
	}


	/**
	 * Checks for whether SpecialInstructions is set or not.
	 *
	 * @return true if SpecialInstructions is set, false if not
	 */
	 public boolean hasSpecialInstructions () {
		return (_SpecialInstructions != null);
	}


	/**
	 * Discards SpecialInstructions's value.
	 */
	 public void deleteSpecialInstructions () {
		_SpecialInstructions = null;
	}


	/**
	 * Get the ScannedAuthorization property.
	 */
	public ScannedAuthorization getScannedAuthorization () {
		return (_ScannedAuthorization);
	}


	/**
	 * Set the ScannedAuthorization property.
	 */
	public void setScannedAuthorization (ScannedAuthorization obj) {
		_ScannedAuthorization = obj;
	}


	private void setScannedAuthorization (XmlElement xml) {

		_ScannedAuthorization =
			new ScannedAuthorization(xml, this);
	}




	/**
	 * Checks for whether ScannedAuthorization is set or not.
	 *
	 * @return true if ScannedAuthorization is set, false if not
	 */
	 public boolean hasScannedAuthorization () {
		return (_ScannedAuthorization != null);
	}


	/**
	 * Discards ScannedAuthorization's value.
	 */
	 public void deleteScannedAuthorization () {
		_ScannedAuthorization = null;
	}


	/**
	 * Get the Location property.
	 */
	public Location getLocation () {
		return (_Location);
	}


	/**
	 * Set the Location property.
	 */
	public void setLocation (Location obj) {
		_Location = obj;
	}


	private void setLocation (XmlElement xml) {

		_Location =
			new Location(xml, this);
	}




	/**
	 * Checks for whether Location is set or not.
	 *
	 * @return true if Location is set, false if not
	 */
	 public boolean hasLocation () {
		return (_Location != null);
	}


	/**
	 * Discards Location's value.
	 */
	 public void deleteLocation () {
		_Location = null;
	}


	protected String _node_name_ = $APS;

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
		return ($APS);
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
		_Account_validator_ = null;
	transient protected static XmlStringValidator 
		_AgentId_validator_ = null;
	transient protected static XmlStringValidator 
		_SpecialInstructions_validator_ = null;
	transient protected static XmlValidator 
		_ScannedAuthorization_validator_ = null;
	transient protected static XmlValidator 
		_Location_validator_ = null;

	/**
	 * Create the validators for this class.
	 */
	protected static synchronized void createValidators () {

		if (_validators_created) {
			return;
		}

		_Account_validator_ = new XmlStringValidator(
			"Aps.Account", "Element", 
			"APS/Account", -1, -1, 1, 1);

		_AgentId_validator_ = new XmlStringValidator(
			"Aps.AgentId", "Element", 
			"APS/AgentID", -1, -1, 1, 1);

		_SpecialInstructions_validator_ = new XmlStringValidator(
			"Aps.SpecialInstructions", "Element", 
			"APS/SpecialInstructions", -1, -1, 1, 1);

		_ScannedAuthorization_validator_ = new XmlValidator(
			"Aps.ScannedAuthorization", "Element", 
			"APS/ScannedAuthorization", 1, 1);

		_Location_validator_ = new XmlValidator(
			"Aps.Location", "Element", 
			"APS/Location", 1, 1);

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
		e = _Account_validator_.validate(
			_Account);
		if (e != null) {
			errors.addElement(e);
			if (return_on_error) {
				return (errors);
			}
		}

		e = _AgentId_validator_.validate(
			_AgentId);
		if (e != null) {
			errors.addElement(e);
			if (return_on_error) {
				return (errors);
			}
		}

		e = _SpecialInstructions_validator_.validate(
			_SpecialInstructions);
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

		is_valid = _ScannedAuthorization_validator_.isValid(
			_ScannedAuthorization,
			errors, return_on_error, traverse);
		if (!is_valid && return_on_error) {
			return (errors);
		}

		is_valid = _Location_validator_.isValid(
			_Location,
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

		return (xml.matches($APS, nsm, parent));
	}


	/**
	 * This method unmarshals an XML document instance
	 * into an instance of this class.
	 */
	public static Aps unmarshal (
			InputStream in) throws Exception {

		Aps obj = new Aps();
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

		if (!xml.matches($APS, this)) {
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

		if (xml.matches($ACCOUNT, this)) {
			setAccount(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($AGENT_ID, this)) {
			setAgentId(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($SPECIAL_INSTRUCTIONS, this)) {
			setSpecialInstructions(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($SCANNED_AUTHORIZATION,
			ScannedAuthorization.nsm, this)) {

			setScannedAuthorization(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($LOCATION,
			Location.nsm, this)) {

			setLocation(xml);
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

		out.write($ACCOUNT,
			_Account);
		out.write($AGENT_ID,
			_AgentId);
		out.write($SPECIAL_INSTRUCTIONS,
			_SpecialInstructions);
		out.write(null,
			getScannedAuthorization());
		out.write(null,
			getLocation());

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