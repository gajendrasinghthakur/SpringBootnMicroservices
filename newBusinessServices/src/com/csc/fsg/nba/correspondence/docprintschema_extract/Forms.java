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
package com.csc.fsg.nba.correspondence.docprintschema_extract;


import com.tbf.xml.*;

/**
 * Forms class.
 *
 */
public class Forms
	implements com.tbf.xml.XmlObject,
	com.tbf.xml.Validateable,
	java.io.Serializable {

	/**
	 * Constant for "SystemDate" node name.
	 */
	public static final String $SYSTEM_DATE = "SystemDate";

	/**
	 * Constant for "Form" node name.
	 */
	public static final String $FORM = "Form";

	/**
	 * Constant for "Data" node name.
	 */
	public static final String $DATA = "Data";

	/**
	 * Constant for "PolicyNum" node name.
	 */
	public static final String $POLICY_NUM = "PolicyNum";

	/**
	 * Constant for "LOB" node name.
	 */
	public static final String $LOB = "LOB";

	/**
	 * Constant for "Forms" node name.
	 */
	public static final String $FORMS = "Forms";

	/**
	 * Constant for "Company" node name.
	 */
	public static final String $COMPANY = "Company";


	/**
	 * Declarations for the XML related fields.
	 */
	protected String _Company = null;
	protected String _LOB = null;
	protected String _PolicyNum = null;
	protected String _SystemDate = null;
	protected Form _Form = null;
	protected Data _Data = null;


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
	public Forms () {
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 */
	public Forms (XmlElement xml) {
		unmarshal(xml);
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 * @param  parent  the containibg XmlObject
	 */
	public Forms (XmlElement xml, XmlObject parent) {
		_parent_ = parent;
		unmarshal(xml);
	}


	/**
	 * Get the Company property.
	 */
	public String getCompany () {
		return (_Company);
	}


	/**
	 * Set the Company property.
	 */
	public void setCompany (String newValue) {
		_Company = newValue;
	}


	/**
	 * Checks for whether Company is set or not.
	 *
	 * @return true if Company is set, false if not
	 */
	 public boolean hasCompany () {
		return (_Company != null);
	}


	/**
	 * Discards Company's value.
	 */
	 public void deleteCompany () {
		_Company = null;
	}


	/**
	 * Get the LOB property.
	 */
	public String getLOB () {
		return (_LOB);
	}


	/**
	 * Set the LOB property.
	 */
	public void setLOB (String newValue) {
		_LOB = newValue;
	}


	/**
	 * Checks for whether LOB is set or not.
	 *
	 * @return true if LOB is set, false if not
	 */
	 public boolean hasLOB () {
		return (_LOB != null);
	}


	/**
	 * Discards LOB's value.
	 */
	 public void deleteLOB () {
		_LOB = null;
	}


	/**
	 * Get the PolicyNum property.
	 */
	public String getPolicyNum () {
		return (_PolicyNum);
	}


	/**
	 * Set the PolicyNum property.
	 */
	public void setPolicyNum (String newValue) {
		_PolicyNum = newValue;
	}


	/**
	 * Checks for whether PolicyNum is set or not.
	 *
	 * @return true if PolicyNum is set, false if not
	 */
	 public boolean hasPolicyNum () {
		return (_PolicyNum != null);
	}


	/**
	 * Discards PolicyNum's value.
	 */
	 public void deletePolicyNum () {
		_PolicyNum = null;
	}


	/**
	 * Get the SystemDate property.
	 */
	public String getSystemDate () {
		return (_SystemDate);
	}


	/**
	 * Set the SystemDate property.
	 */
	public void setSystemDate (String newValue) {
		_SystemDate = newValue;
	}


	/**
	 * Checks for whether SystemDate is set or not.
	 *
	 * @return true if SystemDate is set, false if not
	 */
	 public boolean hasSystemDate () {
		return (_SystemDate != null);
	}


	/**
	 * Discards SystemDate's value.
	 */
	 public void deleteSystemDate () {
		_SystemDate = null;
	}


	/**
	 * Get the Form property.
	 */
	public Form getForm () {
		return (_Form);
	}


	/**
	 * Set the Form property.
	 */
	public void setForm (Form obj) {
		_Form = obj;
	}


	private void setForm (XmlElement xml) {

		_Form =
			new Form(xml, this);
	}




	/**
	 * Checks for whether Form is set or not.
	 *
	 * @return true if Form is set, false if not
	 */
	 public boolean hasForm () {
		return (_Form != null);
	}


	/**
	 * Discards Form's value.
	 */
	 public void deleteForm () {
		_Form = null;
	}


	/**
	 * Get the Data property.
	 */
	public Data getData () {
		return (_Data);
	}


	/**
	 * Set the Data property.
	 */
	public void setData (Data obj) {
		_Data = obj;
	}


	private void setData (XmlElement xml) {

		_Data =
			new Data(xml, this);
	}




	/**
	 * Checks for whether Data is set or not.
	 *
	 * @return true if Data is set, false if not
	 */
	 public boolean hasData () {
		return (_Data != null);
	}


	/**
	 * Discards Data's value.
	 */
	 public void deleteData () {
		_Data = null;
	}


	protected String _node_name_ = $FORMS;

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
		return ($FORMS);
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
		_Company_validator_ = null;
	transient protected static XmlStringValidator 
		_PolicyNum_validator_ = null;
	transient protected static XmlValidator 
		_Form_validator_ = null;
	transient protected static XmlValidator 
		_Data_validator_ = null;

	/**
	 * Create the validators for this class.
	 */
	protected static synchronized void createValidators () {

		if (_validators_created) {
			return;
		}

		_Company_validator_ = new XmlStringValidator(
			"Forms.Company", "Element", 
			"Forms/Company", -1, -1, 1, 1);

		_PolicyNum_validator_ = new XmlStringValidator(
			"Forms.PolicyNum", "Element", 
			"Forms/PolicyNum", -1, -1, 1, 1);

		_Form_validator_ = new XmlValidator(
			"Forms.Form", "Element", 
			"Forms/Form", 1, 1);

		_Data_validator_ = new XmlValidator(
			"Forms.Data", "Element", 
			"Forms/Data", 1, 1);

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
		e = _Company_validator_.validate(
			_Company);
		if (e != null) {
			errors.addElement(e);
			if (return_on_error) {
				return (errors);
			}
		}

		e = _PolicyNum_validator_.validate(
			_PolicyNum);
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

		is_valid = _Form_validator_.isValid(
			_Form,
			errors, return_on_error, traverse);
		if (!is_valid && return_on_error) {
			return (errors);
		}

		is_valid = _Data_validator_.isValid(
			_Data,
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

		return (xml.matches($FORMS, nsm, parent));
	}


	/**
	 * This method unmarshals an XML document instance
	 * into an instance of this class.
	 */
	public static Forms unmarshal (
			java.io.InputStream in) throws Exception {

		Forms obj = new Forms();
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

		if (!xml.matches($FORMS, this)) {
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

		if (xml.matches($COMPANY, this)) {
			setCompany(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($LOB, this)) {
			setLOB(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($POLICY_NUM, this)) {
			setPolicyNum(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($SYSTEM_DATE, this)) {
			setSystemDate(xml.getData());
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($FORM,
			Form.nsm, this)) {

			setForm(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($DATA,
			Data.nsm, this)) {

			setData(xml);
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

		out.write($COMPANY,
			_Company);
		out.write($LOB,
			_LOB);
		out.write($POLICY_NUM,
			_PolicyNum);
		out.write($SYSTEM_DATE,
			_SystemDate);
		out.write(null,
			getForm());
		out.write(null,
			getData());

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
