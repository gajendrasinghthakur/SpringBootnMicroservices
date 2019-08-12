/*
 * DO NOT EDIT!
 *
 * This file was generated by the Breeze XML Studio code generator.
 *
 *        Project: file:/C:/nbA/Requirement/EMSIAPS.xsd Binding
 *     Class Name: Applicant
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
 * Applicant class.
 *
 */
public class Applicant
	implements com.tbf.xml.XmlObject,
	com.tbf.xml.Validateable,
	java.io.Serializable {

	/**
	 * Constant for "Person" node name.
	 */
	public static final String $PERSON = "Person";

	/**
	 * Constant for "Applicant" node name.
	 */
	public static final String $APPLICANT = "Applicant";

	/**
	 * Constant for "Phone" node name.
	 */
	public static final String $PHONE = "Phone";

	/**
	 * Constant for "Policy" node name.
	 */
	public static final String $POLICY = "Policy";

	/**
	 * Constant for "Address" node name.
	 */
	public static final String $ADDRESS = "Address";


	/**
	 * Declarations for the XML related fields.
	 */
	protected Person _Person = null;
	protected Address _Address = null;
	protected Phone _Phone = null;
	protected Policy _Policy = null;


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
	 * Get the Person property.
	 */
	public Person getPerson () {
		return (_Person);
	}


	/**
	 * Set the Person property.
	 */
	public void setPerson (Person obj) {
		_Person = obj;
	}


	private void setPerson (XmlElement xml) {

		_Person =
			new Person(xml, this);
	}




	/**
	 * Checks for whether Person is set or not.
	 *
	 * @return true if Person is set, false if not
	 */
	 public boolean hasPerson () {
		return (_Person != null);
	}


	/**
	 * Discards Person's value.
	 */
	 public void deletePerson () {
		_Person = null;
	}


	/**
	 * Get the Address property.
	 */
	public Address getAddress () {
		return (_Address);
	}


	/**
	 * Set the Address property.
	 */
	public void setAddress (Address obj) {
		_Address = obj;
	}


	private void setAddress (XmlElement xml) {

		_Address =
			new Address(xml, this);
	}




	/**
	 * Checks for whether Address is set or not.
	 *
	 * @return true if Address is set, false if not
	 */
	 public boolean hasAddress () {
		return (_Address != null);
	}


	/**
	 * Discards Address's value.
	 */
	 public void deleteAddress () {
		_Address = null;
	}


	/**
	 * Get the Phone property.
	 */
	public Phone getPhone () {
		return (_Phone);
	}


	/**
	 * Set the Phone property.
	 */
	public void setPhone (Phone obj) {
		_Phone = obj;
	}


	private void setPhone (XmlElement xml) {

		_Phone =
			new Phone(xml, this);
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
	 * Get the Policy property.
	 */
	public Policy getPolicy () {
		return (_Policy);
	}


	/**
	 * Set the Policy property.
	 */
	public void setPolicy (Policy obj) {
		_Policy = obj;
	}


	private void setPolicy (XmlElement xml) {

		_Policy =
			new Policy(xml, this);
	}




	/**
	 * Checks for whether Policy is set or not.
	 *
	 * @return true if Policy is set, false if not
	 */
	 public boolean hasPolicy () {
		return (_Policy != null);
	}


	/**
	 * Discards Policy's value.
	 */
	 public void deletePolicy () {
		_Policy = null;
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
	 * This flag is used to used to check whether
	 * the validators have been created.
	 */
	transient protected static boolean _validators_created = false;

	/*
	 * XML Validators
	 */
	transient protected static XmlValidator 
		_Person_validator_ = null;
	transient protected static XmlValidator 
		_Address_validator_ = null;
	transient protected static XmlValidator 
		_Phone_validator_ = null;
	transient protected static XmlValidator 
		_Policy_validator_ = null;

	/**
	 * Create the validators for this class.
	 */
	protected static synchronized void createValidators () {

		if (_validators_created) {
			return;
		}

		_Person_validator_ = new XmlValidator(
			"Applicant.Person", "Element", 
			"Applicant/Person", 1, 1);

		_Address_validator_ = new XmlValidator(
			"Applicant.Address", "Element", 
			"Applicant/Address", 1, 1);

		_Phone_validator_ = new XmlValidator(
			"Applicant.Phone", "Element", 
			"Applicant/Phone", 1, 1);

		_Policy_validator_ = new XmlValidator(
			"Applicant.Policy", "Element", 
			"Applicant/Policy", 1, 1);

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

		is_valid = _Person_validator_.isValid(
			_Person,
			errors, return_on_error, traverse);
		if (!is_valid && return_on_error) {
			return (errors);
		}

		is_valid = _Address_validator_.isValid(
			_Address,
			errors, return_on_error, traverse);
		if (!is_valid && return_on_error) {
			return (errors);
		}

		is_valid = _Phone_validator_.isValid(
			_Phone,
			errors, return_on_error, traverse);
		if (!is_valid && return_on_error) {
			return (errors);
		}

		is_valid = _Policy_validator_.isValid(
			_Policy,
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
			InputStream in) throws Exception {

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

		if (xml.matches($PERSON,
			Person.nsm, this)) {

			setPerson(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($ADDRESS,
			Address.nsm, this)) {

			setAddress(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($PHONE,
			Phone.nsm, this)) {

			setPhone(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($POLICY,
			Policy.nsm, this)) {

			setPolicy(xml);
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
			getPerson());
		out.write(null,
			getAddress());
		out.write(null,
			getPhone());
		out.write(null,
			getPolicy());

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
