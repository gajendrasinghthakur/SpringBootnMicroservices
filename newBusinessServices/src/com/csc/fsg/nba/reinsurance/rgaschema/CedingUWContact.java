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
import com.tbf.xml.XmlAttributeList;
import com.tbf.xml.XmlElement;
import com.tbf.xml.XmlNamespaceManager;
import com.tbf.xml.XmlObject;
import com.tbf.xml.XmlOutputStream;

/**
 * CedingUWContact class.
 *
 */
public class CedingUWContact
	implements com.tbf.xml.XmlObject,
	com.tbf.xml.Validateable,
	java.io.Serializable {

	/**
	 * Constant for "Type" node name.
	 */
	public static final String $TYPE = "Type";

	/**
	 * Constant for "Fax" node name.
	 */
	public static final String $FAX = "Fax";

	/**
	 * Constant for "FirstName" node name.
	 */
	public static final String $FIRST_NAME = "FirstName";

	/**
	 * Constant for "LastName" node name.
	 */
	public static final String $LAST_NAME = "LastName";

	/**
	 * Constant for "Phone" node name.
	 */
	public static final String $PHONE = "Phone";

	/**
	 * Constant for "CedingUWContact" node name.
	 */
	public static final String $CEDING_UWCONTACT = "CedingUWContact";

	/**
	 * Constant for "Email" node name.
	 */
	public static final String $EMAIL = "Email";

	/**
	 * Constant for "CedingContactID" node name.
	 */
	public static final String $CEDING_CONTACT_ID = "CedingContactID";


	/**
	 * Declarations for the XML related fields.
	 */
	protected String _CedingContactID = null;
	protected String _Email = null;
	protected String _Fax = null;
	protected String _FirstName = null;
	protected String _LastName = null;
	protected String _Phone = null;
	protected String _Type = null;


	/**
	 * Holds the parent object of this object.
	 */
	protected transient XmlObject _parent_ = null;


	/**
	 * Default no args constructor.
	 */
	public CedingUWContact () {
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 */
	public CedingUWContact (XmlElement xml) {
		unmarshal(xml);
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 * @param  parent  the containibg XmlObject
	 */
	public CedingUWContact (XmlElement xml, XmlObject parent) {
		_parent_ = parent;
		unmarshal(xml);
	}


	/**
	 * Get the CedingContactID property.
	 */
	public String getCedingContactID () {
		return (_CedingContactID);
	}


	/**
	 * Set the CedingContactID property.
	 */
	public void setCedingContactID (String newValue) {
		_CedingContactID = newValue;
	}


	/**
	 * Checks for whether CedingContactID is set or not.
	 *
	 * @return true if CedingContactID is set, false if not
	 */
	 public boolean hasCedingContactID () {
		return (_CedingContactID != null);
	}


	/**
	 * Discards CedingContactID's value.
	 */
	 public void deleteCedingContactID () {
		_CedingContactID = null;
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
	 * Get the Type property.
	 */
	public String getType () {
		return (_Type);
	}


	/**
	 * Set the Type property.
	 */
	public void setType (String newValue) {
		_Type = newValue;
	}


	/**
	 * Checks for whether Type is set or not.
	 *
	 * @return true if Type is set, false if not
	 */
	 public boolean hasType () {
		return (_Type != null);
	}


	/**
	 * Discards Type's value.
	 */
	 public void deleteType () {
		_Type = null;
	}


	protected String _node_name_ = $CEDING_UWCONTACT;

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
		return ($CEDING_UWCONTACT);
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

		return (xml.matches($CEDING_UWCONTACT, nsm, parent));
	}


	/**
	 * This method unmarshals an XML document instance
	 * into an instance of this class.
	 */
	public static CedingUWContact unmarshal (
			java.io.InputStream in) throws Exception {

		CedingUWContact obj = new CedingUWContact();
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

		if (!xml.matches($CEDING_UWCONTACT, this)) {
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
		setCedingContactID(xml.getAttribute($CEDING_CONTACT_ID, this, false));
		setEmail(xml.getAttribute($EMAIL, this, false));
		setFax(xml.getAttribute($FAX, this, false));
		setFirstName(xml.getAttribute($FIRST_NAME, this, false));
		setLastName(xml.getAttribute($LAST_NAME, this, false));
		setPhone(xml.getAttribute($PHONE, this, false));
		setType(xml.getAttribute($TYPE, this, false));
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

		attrs.add($CEDING_CONTACT_ID, _CedingContactID);
		attrs.add($EMAIL, _Email);
		attrs.add($FAX, _Fax);
		attrs.add($FIRST_NAME, _FirstName);
		attrs.add($LAST_NAME, _LastName);
		attrs.add($PHONE, _Phone);
		attrs.add($TYPE, _Type);

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
