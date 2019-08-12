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
 * Reinsurer class.
 *
 */
public class Reinsurer
	implements com.tbf.xml.XmlObject,
	com.tbf.xml.Validateable,
	java.io.Serializable {

	/**
	 * Constant for "MiddleName" node name.
	 */
	public static final String $MIDDLE_NAME = "MiddleName";

	/**
	 * Constant for "Reinsurer" node name.
	 */
	public static final String $REINSURER = "Reinsurer";

	/**
	 * Constant for "Fax" node name.
	 */
	public static final String $FAX = "Fax";

	/**
	 * Constant for "NetworkFaxAddress" node name.
	 */
	public static final String $NETWORK_FAX_ADDRESS = "NetworkFaxAddress";

	/**
	 * Constant for "Zipcode" node name.
	 */
	public static final String $ZIPCODE = "Zipcode";

	/**
	 * Constant for "Phone" node name.
	 */
	public static final String $PHONE = "Phone";

	/**
	 * Constant for "Email" node name.
	 */
	public static final String $EMAIL = "Email";

	/**
	 * Constant for "ReinsurerName" node name.
	 */
	public static final String $REINSURER_NAME = "ReinsurerName";

	/**
	 * Constant for "LastName" node name.
	 */
	public static final String $LAST_NAME = "LastName";

	/**
	 * Constant for "City" node name.
	 */
	public static final String $CITY = "City";

	/**
	 * Constant for "FirstName" node name.
	 */
	public static final String $FIRST_NAME = "FirstName";

	/**
	 * Constant for "Address2" node name.
	 */
	public static final String $ADDRESS2 = "Address2";

	/**
	 * Constant for "State" node name.
	 */
	public static final String $STATE = "State";

	/**
	 * Constant for "Address1" node name.
	 */
	public static final String $ADDRESS1 = "Address1";

	/**
	 * Constant for "ContactID" node name.
	 */
	public static final String $CONTACT_ID = "ContactID";

	/**
	 * Constant for "ReceiveMethodID" node name.
	 */
	public static final String $RECEIVE_METHOD_ID = "ReceiveMethodID";


	/**
	 * Declarations for the XML related fields.
	 */
	protected String _Address1 = null;
	protected String _Address2 = null;
	protected String _City = null;
	protected java.math.BigInteger _ContactID = null;
	protected String _Email = null;
	protected String _Fax = null;
	protected String _FirstName = null;
	protected String _LastName = null;
	protected String _MiddleName = null;
	protected String _NetworkFaxAddress = null;
	protected String _Phone = null;
	protected String _ReceiveMethodID = null;
	protected String _ReinsurerName = null;
	protected String _State = null;
	protected String _Zipcode = null;


	/**
	 * Holds the parent object of this object.
	 */
	protected transient XmlObject _parent_ = null;


	/**
	 * Default no args constructor.
	 */
	public Reinsurer () {
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 */
	public Reinsurer (XmlElement xml) {
		unmarshal(xml);
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 * @param  parent  the containibg XmlObject
	 */
	public Reinsurer (XmlElement xml, XmlObject parent) {
		_parent_ = parent;
		unmarshal(xml);
	}


	/**
	 * Get the Address1 property.
	 */
	public String getAddress1 () {
		return (_Address1);
	}


	/**
	 * Set the Address1 property.
	 */
	public void setAddress1 (String newValue) {
		_Address1 = newValue;
	}


	/**
	 * Checks for whether Address1 is set or not.
	 *
	 * @return true if Address1 is set, false if not
	 */
	 public boolean hasAddress1 () {
		return (_Address1 != null);
	}


	/**
	 * Discards Address1's value.
	 */
	 public void deleteAddress1 () {
		_Address1 = null;
	}


	/**
	 * Get the Address2 property.
	 */
	public String getAddress2 () {
		return (_Address2);
	}


	/**
	 * Set the Address2 property.
	 */
	public void setAddress2 (String newValue) {
		_Address2 = newValue;
	}


	/**
	 * Checks for whether Address2 is set or not.
	 *
	 * @return true if Address2 is set, false if not
	 */
	 public boolean hasAddress2 () {
		return (_Address2 != null);
	}


	/**
	 * Discards Address2's value.
	 */
	 public void deleteAddress2 () {
		_Address2 = null;
	}


	/**
	 * Get the City property.
	 */
	public String getCity () {
		return (_City);
	}


	/**
	 * Set the City property.
	 */
	public void setCity (String newValue) {
		_City = newValue;
	}


	/**
	 * Checks for whether City is set or not.
	 *
	 * @return true if City is set, false if not
	 */
	 public boolean hasCity () {
		return (_City != null);
	}


	/**
	 * Discards City's value.
	 */
	 public void deleteCity () {
		_City = null;
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
		_format_errors.remove("Reinsurer.ContactID");
	}


	public void setContactID (String new_value) {

		if (new_value == null) {
			_ContactID = null;
			return;
		}

		try {
			_ContactID = new java.math.BigInteger(new_value);
			_format_errors.remove("Reinsurer.ContactID");
		} catch (NumberFormatException nfe) {
			_ContactID = null;
			XmlValidationError.addValidityFormatError(
				_format_errors, "Reinsurer.ContactID", "Attribute",
				"Reinsurer/ContactID", new_value);
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
		_format_errors.remove("Reinsurer.ContactID");
	}


	/**
	 * Get the Email property.
	 */
	public String getEmail () {
		return (_Email);
	}


	/**
	 * Set the Email property.
	 */
	public void setEmail (String newValue) {
		_Email = newValue;
	}


	/**
	 * Checks for whether Email is set or not.
	 *
	 * @return true if Email is set, false if not
	 */
	 public boolean hasEmail () {
		return (_Email != null);
	}


	/**
	 * Discards Email's value.
	 */
	 public void deleteEmail () {
		_Email = null;
	}


	/**
	 * Get the Fax property.
	 */
	public String getFax () {
		return (_Fax);
	}


	/**
	 * Set the Fax property.
	 */
	public void setFax (String newValue) {
		_Fax = newValue;
	}


	/**
	 * Checks for whether Fax is set or not.
	 *
	 * @return true if Fax is set, false if not
	 */
	 public boolean hasFax () {
		return (_Fax != null);
	}


	/**
	 * Discards Fax's value.
	 */
	 public void deleteFax () {
		_Fax = null;
	}


	/**
	 * Get the FirstName property.
	 */
	public String getFirstName () {
		return (_FirstName);
	}


	/**
	 * Set the FirstName property.
	 */
	public void setFirstName (String newValue) {
		_FirstName = newValue;
	}


	/**
	 * Checks for whether FirstName is set or not.
	 *
	 * @return true if FirstName is set, false if not
	 */
	 public boolean hasFirstName () {
		return (_FirstName != null);
	}


	/**
	 * Discards FirstName's value.
	 */
	 public void deleteFirstName () {
		_FirstName = null;
	}


	/**
	 * Get the LastName property.
	 */
	public String getLastName () {
		return (_LastName);
	}


	/**
	 * Set the LastName property.
	 */
	public void setLastName (String newValue) {
		_LastName = newValue;
	}


	/**
	 * Checks for whether LastName is set or not.
	 *
	 * @return true if LastName is set, false if not
	 */
	 public boolean hasLastName () {
		return (_LastName != null);
	}


	/**
	 * Discards LastName's value.
	 */
	 public void deleteLastName () {
		_LastName = null;
	}


	/**
	 * Get the MiddleName property.
	 */
	public String getMiddleName () {
		return (_MiddleName);
	}


	/**
	 * Set the MiddleName property.
	 */
	public void setMiddleName (String newValue) {
		_MiddleName = newValue;
	}


	/**
	 * Checks for whether MiddleName is set or not.
	 *
	 * @return true if MiddleName is set, false if not
	 */
	 public boolean hasMiddleName () {
		return (_MiddleName != null);
	}


	/**
	 * Discards MiddleName's value.
	 */
	 public void deleteMiddleName () {
		_MiddleName = null;
	}


	/**
	 * Get the NetworkFaxAddress property.
	 */
	public String getNetworkFaxAddress () {
		return (_NetworkFaxAddress);
	}


	/**
	 * Set the NetworkFaxAddress property.
	 */
	public void setNetworkFaxAddress (String newValue) {
		_NetworkFaxAddress = newValue;
	}


	/**
	 * Checks for whether NetworkFaxAddress is set or not.
	 *
	 * @return true if NetworkFaxAddress is set, false if not
	 */
	 public boolean hasNetworkFaxAddress () {
		return (_NetworkFaxAddress != null);
	}


	/**
	 * Discards NetworkFaxAddress's value.
	 */
	 public void deleteNetworkFaxAddress () {
		_NetworkFaxAddress = null;
	}


	/**
	 * Get the Phone property.
	 */
	public String getPhone () {
		return (_Phone);
	}


	/**
	 * Set the Phone property.
	 */
	public void setPhone (String newValue) {
		_Phone = newValue;
	}


	/**
	 * Checks for whether Phone is set or not.
	 *
	 * @return true if Phone is set, false if not
	 */
	 public boolean hasPhone () {
		return (_Phone != null);
	}


	/**
	 * Discards Phone's value.
	 */
	 public void deletePhone () {
		_Phone = null;
	}


	/**
	 * Get the ReceiveMethodID property.
	 */
	public String getReceiveMethodID () {
		return (_ReceiveMethodID);
	}


	/**
	 * Set the ReceiveMethodID property.
	 */
	public void setReceiveMethodID (String newValue) {
		_ReceiveMethodID = newValue;
	}


	/**
	 * Checks for whether ReceiveMethodID is set or not.
	 *
	 * @return true if ReceiveMethodID is set, false if not
	 */
	 public boolean hasReceiveMethodID () {
		return (_ReceiveMethodID != null);
	}


	/**
	 * Discards ReceiveMethodID's value.
	 */
	 public void deleteReceiveMethodID () {
		_ReceiveMethodID = null;
	}


	/**
	 * Get the ReinsurerName property.
	 */
	public String getReinsurerName () {
		return (_ReinsurerName);
	}


	/**
	 * Set the ReinsurerName property.
	 */
	public void setReinsurerName (String newValue) {
		_ReinsurerName = newValue;
	}


	/**
	 * Checks for whether ReinsurerName is set or not.
	 *
	 * @return true if ReinsurerName is set, false if not
	 */
	 public boolean hasReinsurerName () {
		return (_ReinsurerName != null);
	}


	/**
	 * Discards ReinsurerName's value.
	 */
	 public void deleteReinsurerName () {
		_ReinsurerName = null;
	}


	/**
	 * Get the State property.
	 */
	public String getState () {
		return (_State);
	}


	/**
	 * Set the State property.
	 */
	public void setState (String newValue) {
		_State = newValue;
	}


	/**
	 * Checks for whether State is set or not.
	 *
	 * @return true if State is set, false if not
	 */
	 public boolean hasState () {
		return (_State != null);
	}


	/**
	 * Discards State's value.
	 */
	 public void deleteState () {
		_State = null;
	}


	/**
	 * Get the Zipcode property.
	 */
	public String getZipcode () {
		return (_Zipcode);
	}


	/**
	 * Set the Zipcode property.
	 */
	public void setZipcode (String newValue) {
		_Zipcode = newValue;
	}


	/**
	 * Checks for whether Zipcode is set or not.
	 *
	 * @return true if Zipcode is set, false if not
	 */
	 public boolean hasZipcode () {
		return (_Zipcode != null);
	}


	/**
	 * Discards Zipcode's value.
	 */
	 public void deleteZipcode () {
		_Zipcode = null;
	}


	protected String _node_name_ = $REINSURER;

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
		return ($REINSURER);
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
		_ContactID_validator_ = null;

	/**
	 * Create the validators for this class.
	 */
	protected static synchronized void createValidators () {

		if (_validators_created) {
			return;
		}

		_ContactID_validator_ = new XmlNumberValidator(
			"Reinsurer.ContactID", "Attribute",
			"Reinsurer/ContactID",
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
		e = _ContactID_validator_.validate(
			_ContactID,
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

		return (xml.matches($REINSURER, nsm, parent));
	}


	/**
	 * This method unmarshals an XML document instance
	 * into an instance of this class.
	 */
	public static Reinsurer unmarshal (
			java.io.InputStream in) throws Exception {

		Reinsurer obj = new Reinsurer();
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

		if (!xml.matches($REINSURER, this)) {
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
		setAddress1(xml.getAttribute($ADDRESS1, this, false));
		setAddress2(xml.getAttribute($ADDRESS2, this, false));
		setCity(xml.getAttribute($CITY, this, false));
		setContactID(xml.getAttribute($CONTACT_ID, this, false));
		setEmail(xml.getAttribute($EMAIL, this, false));
		setFax(xml.getAttribute($FAX, this, false));
		setFirstName(xml.getAttribute($FIRST_NAME, this, false));
		setLastName(xml.getAttribute($LAST_NAME, this, false));
		setMiddleName(xml.getAttribute($MIDDLE_NAME, this, false));
		setNetworkFaxAddress(xml.getAttribute($NETWORK_FAX_ADDRESS, this, false));
		setPhone(xml.getAttribute($PHONE, this, false));
		setReceiveMethodID(xml.getAttribute($RECEIVE_METHOD_ID, this, false));
		setReinsurerName(xml.getAttribute($REINSURER_NAME, this, false));
		setState(xml.getAttribute($STATE, this, false));
		setZipcode(xml.getAttribute($ZIPCODE, this, false));
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

		attrs.add($ADDRESS1, _Address1);
		attrs.add($ADDRESS2, _Address2);
		attrs.add($CITY, _City);
		attrs.add($CONTACT_ID, _ContactID);
		attrs.add($EMAIL, _Email);
		attrs.add($FAX, _Fax);
		attrs.add($FIRST_NAME, _FirstName);
		attrs.add($LAST_NAME, _LastName);
		attrs.add($MIDDLE_NAME, _MiddleName);
		attrs.add($NETWORK_FAX_ADDRESS, _NetworkFaxAddress);
		attrs.add($PHONE, _Phone);
		attrs.add($RECEIVE_METHOD_ID, _ReceiveMethodID);
		attrs.add($REINSURER_NAME, _ReinsurerName);
		attrs.add($STATE, _State);
		attrs.add($ZIPCODE, _Zipcode);

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
