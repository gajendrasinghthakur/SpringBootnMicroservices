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
 * Applicant class.
 *
 */
public class Applicant
	implements com.tbf.xml.XmlObject,
	com.tbf.xml.Validateable,
	java.io.Serializable {

	/**
	 * Constant for "Occupation" node name.
	 */
	public static final String $OCCUPATION = "Occupation";

	/**
	 * Constant for "MiddleName" node name.
	 */
	public static final String $MIDDLE_NAME = "MiddleName";

	/**
	 * Constant for "Smoker" node name.
	 */
	public static final String $SMOKER = "Smoker";

	/**
	 * Constant for "NationalIdentifier" node name.
	 */
	public static final String $NATIONAL_IDENTIFIER = "NationalIdentifier";

	/**
	 * Constant for "ResidenceCountryCode" node name.
	 */
	public static final String $RESIDENCE_COUNTRY_CODE = "ResidenceCountryCode";

	/**
	 * Constant for "BirthStateCode" node name.
	 */
	public static final String $BIRTH_STATE_CODE = "BirthStateCode";

	/**
	 * Constant for "BirthCountryCode" node name.
	 */
	public static final String $BIRTH_COUNTRY_CODE = "BirthCountryCode";

	/**
	 * Constant for "DOB" node name.
	 */
	public static final String $DOB = "DOB";

	/**
	 * Constant for "Applicant" node name.
	 */
	public static final String $APPLICANT = "Applicant";

	/**
	 * Constant for "LastName" node name.
	 */
	public static final String $LAST_NAME = "LastName";

	/**
	 * Constant for "ID" node name.
	 */
	public static final String $ID = "ID";

	/**
	 * Constant for "Benefits" node name.
	 */
	public static final String $BENEFITS = "Benefits";

	/**
	 * Constant for "FirstName" node name.
	 */
	public static final String $FIRST_NAME = "FirstName";

	/**
	 * Constant for "Gender" node name.
	 */
	public static final String $GENDER = "Gender";

	/**
	 * Constant for "ResidenceStateCode" node name.
	 */
	public static final String $RESIDENCE_STATE_CODE = "ResidenceStateCode";

	/**
	 * Constant for "Primary" node name.
	 */
	public static final String $PRIMARY = "Primary";


	/**
	 * Declarations for the XML related fields.
	 */
	protected Benefits _Benefits = null;
	protected String _BirthCountryCode = null;
	protected String _BirthStateCode = null;
	protected String _DOB = null;
	protected String _FirstName = null;
	protected String _Gender = null;
	protected java.math.BigInteger _ID = null;
	protected String _LastName = null;
	protected String _MiddleName = null;
	protected String _NationalIdentifier = null;
	protected String _Occupation = null;
	protected String _Primary = null;
	protected String _ResidenceCountryCode = null;
	protected String _ResidenceStateCode = null;
	protected String _Smoker = null;


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
	public Applicant () {
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 */
	public Applicant (XmlElement xml) {
		unmarshal(xml);
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 * @param  parent  the containibg XmlObject
	 */
	public Applicant (XmlElement xml, XmlObject parent) {
		_parent_ = parent;
		unmarshal(xml);
	}


	/**
	 * Get the Benefits property.
	 */
	public Benefits getBenefits () {
		return (_Benefits);
	}


	/**
	 * Set the Benefits property.
	 */
	public void setBenefits (Benefits obj) {
		_Benefits = obj;
	}


	private void setBenefits (XmlElement xml) {

		_Benefits =
			new Benefits(xml, this);
	}




	/**
	 * Checks for whether Benefits is set or not.
	 *
	 * @return true if Benefits is set, false if not
	 */
	 public boolean hasBenefits () {
		return (_Benefits != null);
	}


	/**
	 * Discards Benefits's value.
	 */
	 public void deleteBenefits () {
		_Benefits = null;
	}


	/**
	 * Get the BirthCountryCode property.
	 */
	public String getBirthCountryCode () {
		return (_BirthCountryCode);
	}


	/**
	 * Set the BirthCountryCode property.
	 */
	public void setBirthCountryCode (String newValue) {
		_BirthCountryCode = newValue;
	}


	/**
	 * Checks for whether BirthCountryCode is set or not.
	 *
	 * @return true if BirthCountryCode is set, false if not
	 */
	 public boolean hasBirthCountryCode () {
		return (_BirthCountryCode != null);
	}


	/**
	 * Discards BirthCountryCode's value.
	 */
	 public void deleteBirthCountryCode () {
		_BirthCountryCode = null;
	}


	/**
	 * Get the BirthStateCode property.
	 */
	public String getBirthStateCode () {
		return (_BirthStateCode);
	}


	/**
	 * Set the BirthStateCode property.
	 */
	public void setBirthStateCode (String newValue) {
		_BirthStateCode = newValue;
	}


	/**
	 * Checks for whether BirthStateCode is set or not.
	 *
	 * @return true if BirthStateCode is set, false if not
	 */
	 public boolean hasBirthStateCode () {
		return (_BirthStateCode != null);
	}


	/**
	 * Discards BirthStateCode's value.
	 */
	 public void deleteBirthStateCode () {
		_BirthStateCode = null;
	}


	/**
	 * Get the DOB property.
	 */
	public String getDOB () {
		return (_DOB);
	}


	/**
	 * Set the DOB property.
	 */
	public void setDOB (String newValue) {
		_DOB = newValue;
	}


	/**
	 * Checks for whether DOB is set or not.
	 *
	 * @return true if DOB is set, false if not
	 */
	 public boolean hasDOB () {
		return (_DOB != null);
	}


	/**
	 * Discards DOB's value.
	 */
	 public void deleteDOB () {
		_DOB = null;
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
	 * Get the Gender property.
	 */
	public String getGender () {
		return (_Gender);
	}


	/**
	 * Set the Gender property.
	 */
	public void setGender (String newValue) {
		_Gender = newValue;
	}


	/**
	 * Checks for whether Gender is set or not.
	 *
	 * @return true if Gender is set, false if not
	 */
	 public boolean hasGender () {
		return (_Gender != null);
	}


	/**
	 * Discards Gender's value.
	 */
	 public void deleteGender () {
		_Gender = null;
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
		_format_errors.remove("Applicant.ID");
	}


	public void setID (String new_value) {

		if (new_value == null) {
			_ID = null;
			return;
		}

		try {
			_ID = new java.math.BigInteger(new_value);
			_format_errors.remove("Applicant.ID");
		} catch (NumberFormatException nfe) {
			_ID = null;
			XmlValidationError.addValidityFormatError(
				_format_errors, "Applicant.ID", "Attribute",
				"Applicant/ID", new_value);
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
		_format_errors.remove("Applicant.ID");
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
	 * Get the NationalIdentifier property.
	 */
	public String getNationalIdentifier () {
		return (_NationalIdentifier);
	}


	/**
	 * Set the NationalIdentifier property.
	 */
	public void setNationalIdentifier (String newValue) {
		_NationalIdentifier = newValue;
	}


	/**
	 * Checks for whether NationalIdentifier is set or not.
	 *
	 * @return true if NationalIdentifier is set, false if not
	 */
	 public boolean hasNationalIdentifier () {
		return (_NationalIdentifier != null);
	}


	/**
	 * Discards NationalIdentifier's value.
	 */
	 public void deleteNationalIdentifier () {
		_NationalIdentifier = null;
	}


	/**
	 * Get the Occupation property.
	 */
	public String getOccupation () {
		return (_Occupation);
	}


	/**
	 * Set the Occupation property.
	 */
	public void setOccupation (String newValue) {
		_Occupation = newValue;
	}


	/**
	 * Checks for whether Occupation is set or not.
	 *
	 * @return true if Occupation is set, false if not
	 */
	 public boolean hasOccupation () {
		return (_Occupation != null);
	}


	/**
	 * Discards Occupation's value.
	 */
	 public void deleteOccupation () {
		_Occupation = null;
	}


	/**
	 * Get the Primary property.
	 */
	public String getPrimary () {
		return (_Primary);
	}


	/**
	 * Set the Primary property.
	 */
	public void setPrimary (String newValue) {
		_Primary = newValue;
	}


	/**
	 * Checks for whether Primary is set or not.
	 *
	 * @return true if Primary is set, false if not
	 */
	 public boolean hasPrimary () {
		return (_Primary != null);
	}


	/**
	 * Discards Primary's value.
	 */
	 public void deletePrimary () {
		_Primary = null;
	}


	/**
	 * Get the ResidenceCountryCode property.
	 */
	public String getResidenceCountryCode () {
		return (_ResidenceCountryCode);
	}


	/**
	 * Set the ResidenceCountryCode property.
	 */
	public void setResidenceCountryCode (String newValue) {
		_ResidenceCountryCode = newValue;
	}


	/**
	 * Checks for whether ResidenceCountryCode is set or not.
	 *
	 * @return true if ResidenceCountryCode is set, false if not
	 */
	 public boolean hasResidenceCountryCode () {
		return (_ResidenceCountryCode != null);
	}


	/**
	 * Discards ResidenceCountryCode's value.
	 */
	 public void deleteResidenceCountryCode () {
		_ResidenceCountryCode = null;
	}


	/**
	 * Get the ResidenceStateCode property.
	 */
	public String getResidenceStateCode () {
		return (_ResidenceStateCode);
	}


	/**
	 * Set the ResidenceStateCode property.
	 */
	public void setResidenceStateCode (String newValue) {
		_ResidenceStateCode = newValue;
	}


	/**
	 * Checks for whether ResidenceStateCode is set or not.
	 *
	 * @return true if ResidenceStateCode is set, false if not
	 */
	 public boolean hasResidenceStateCode () {
		return (_ResidenceStateCode != null);
	}


	/**
	 * Discards ResidenceStateCode's value.
	 */
	 public void deleteResidenceStateCode () {
		_ResidenceStateCode = null;
	}


	/**
	 * Get the Smoker property.
	 */
	public String getSmoker () {
		return (_Smoker);
	}


	/**
	 * Set the Smoker property.
	 */
	public void setSmoker (String newValue) {
		_Smoker = newValue;
	}


	/**
	 * Checks for whether Smoker is set or not.
	 *
	 * @return true if Smoker is set, false if not
	 */
	 public boolean hasSmoker () {
		return (_Smoker != null);
	}


	/**
	 * Discards Smoker's value.
	 */
	 public void deleteSmoker () {
		_Smoker = null;
	}


	protected String _node_name_ = $APPLICANT;

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
		return ($APPLICANT);
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
		_Benefits_validator_ = null;
	transient protected static XmlNumberValidator
		_ID_validator_ = null;

	/**
	 * Create the validators for this class.
	 */
	protected static synchronized void createValidators () {

		if (_validators_created) {
			return;
		}

		_Benefits_validator_ = new XmlValidator(
			"Applicant.Benefits", "Element", 
			"Applicant/Benefits", 1, 1);

		_ID_validator_ = new XmlNumberValidator(
			"Applicant.ID", "Attribute",
			"Applicant/ID",
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
		e = _ID_validator_.validate(
			_ID,
			_format_errors);
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

		is_valid = _Benefits_validator_.isValid(
			_Benefits,
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

		return (xml.matches($APPLICANT, nsm, parent));
	}


	/**
	 * This method unmarshals an XML document instance
	 * into an instance of this class.
	 */
	public static Applicant unmarshal (
			java.io.InputStream in) throws Exception {

		Applicant obj = new Applicant();
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

		if (!xml.matches($APPLICANT, this)) {
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

		if (xml.matches($BENEFITS,
			Benefits.nsm, this)) {

			setBenefits(xml);
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

		/*
		 * Set the attribute based field(s)
		 */
		setBirthCountryCode(xml.getAttribute($BIRTH_COUNTRY_CODE, this, false));
		setBirthStateCode(xml.getAttribute($BIRTH_STATE_CODE, this, false));
		setDOB(xml.getAttribute($DOB, this, false));
		setFirstName(xml.getAttribute($FIRST_NAME, this, false));
		setGender(xml.getAttribute($GENDER, this, false));
		setID(xml.getAttribute($ID, this, false));
		setLastName(xml.getAttribute($LAST_NAME, this, false));
		setMiddleName(xml.getAttribute($MIDDLE_NAME, this, false));
		setNationalIdentifier(xml.getAttribute($NATIONAL_IDENTIFIER, this, false));
		setOccupation(xml.getAttribute($OCCUPATION, this, false));
		setPrimary(xml.getAttribute($PRIMARY, this, false));
		setResidenceCountryCode(xml.getAttribute($RESIDENCE_COUNTRY_CODE, this, false));
		setResidenceStateCode(xml.getAttribute($RESIDENCE_STATE_CODE, this, false));
		setSmoker(xml.getAttribute($SMOKER, this, false));
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
			getBenefits());

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

		if (attrs == null) {
			attrs = new XmlAttributeList();
		}

		attrs.add($BIRTH_COUNTRY_CODE, _BirthCountryCode);
		attrs.add($BIRTH_STATE_CODE, _BirthStateCode);
		attrs.add($DOB, _DOB);
		attrs.add($FIRST_NAME, _FirstName);
		attrs.add($GENDER, _Gender);
		attrs.add($ID, _ID);
		attrs.add($LAST_NAME, _LastName);
		attrs.add($MIDDLE_NAME, _MiddleName);
		attrs.add($NATIONAL_IDENTIFIER, _NationalIdentifier);
		attrs.add($OCCUPATION, _Occupation);
		attrs.add($PRIMARY, _Primary);
		attrs.add($RESIDENCE_COUNTRY_CODE, _ResidenceCountryCode);
		attrs.add($RESIDENCE_STATE_CODE, _ResidenceStateCode);
		attrs.add($SMOKER, _Smoker);

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
