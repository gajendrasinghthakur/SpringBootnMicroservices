/*
 * DO NOT EDIT!
 *
 * This file was generated by the Breeze XML Studio code generator.
 *
 *        Project: file:/C:/nbA/Requirement/EMSIPHIOrder.xsd Binding
 *     Class Name: Phi
 *           Date: Mon Nov 11 14:48:46 CST 2002
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
 * Phi class.
 *
 */
public class Phi
	implements com.tbf.xml.XmlObject,
	com.tbf.xml.Validateable,
	java.io.Serializable {

	/**
	 * Constant for "Report" node name.
	 */
	public static final String $REPORT = "Report";

	/**
	 * Constant for "Questionnaire" node name.
	 */
	public static final String $QUESTIONNAIRE = "Questionnaire";

	/**
	 * Constant for "PHI" node name.
	 */
	public static final String $PHI = "PHI";


	/**
	 * Declarations for the XML related fields.
	 */
	protected Report _Report = null;
	protected Questionnaire _Questionnaire = null;


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
	public Phi () {
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 */
	public Phi (XmlElement xml) {
		unmarshal(xml);
	}


	/**
	 * Creates and populates an instance from the provided parse tree.
	 *
	 * @param  xml  the parse tree
	 * @param  parent  the containibg XmlObject
	 */
	public Phi (XmlElement xml, XmlObject parent) {
		_parent_ = parent;
		unmarshal(xml);
	}


	/**
	 * Get the Report property.
	 */
	public Report getReport () {
		return (_Report);
	}


	/**
	 * Set the Report property.
	 */
	public void setReport (Report obj) {
		_Report = obj;
	}


	private void setReport (XmlElement xml) {

		_Report =
			new Report(xml, this);
	}




	/**
	 * Checks for whether Report is set or not.
	 *
	 * @return true if Report is set, false if not
	 */
	 public boolean hasReport () {
		return (_Report != null);
	}


	/**
	 * Discards Report's value.
	 */
	 public void deleteReport () {
		_Report = null;
	}


	/**
	 * Get the Questionnaire property.
	 */
	public Questionnaire getQuestionnaire () {
		return (_Questionnaire);
	}


	/**
	 * Set the Questionnaire property.
	 */
	public void setQuestionnaire (Questionnaire obj) {
		_Questionnaire = obj;
	}


	private void setQuestionnaire (XmlElement xml) {

		_Questionnaire =
			new Questionnaire(xml, this);
	}




	/**
	 * Checks for whether Questionnaire is set or not.
	 *
	 * @return true if Questionnaire is set, false if not
	 */
	 public boolean hasQuestionnaire () {
		return (_Questionnaire != null);
	}


	/**
	 * Discards Questionnaire's value.
	 */
	 public void deleteQuestionnaire () {
		_Questionnaire = null;
	}


	protected String _node_name_ = $PHI;

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
		return ($PHI);
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
		_Report_validator_ = null;
	transient protected static XmlValidator 
		_Questionnaire_validator_ = null;

	/**
	 * Create the validators for this class.
	 */
	protected static synchronized void createValidators () {

		if (_validators_created) {
			return;
		}

		_Report_validator_ = new XmlValidator(
			"Phi.Report", "Element", 
			"PHI/Report", 1, 1);

		_Questionnaire_validator_ = new XmlValidator(
			"Phi.Questionnaire", "Element", 
			"PHI/Questionnaire", 1, 1);

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

		is_valid = _Report_validator_.isValid(
			_Report,
			errors, return_on_error, traverse);
		if (!is_valid && return_on_error) {
			return (errors);
		}

		is_valid = _Questionnaire_validator_.isValid(
			_Questionnaire,
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

		return (xml.matches($PHI, nsm, parent));
	}


	/**
	 * This method unmarshals an XML document instance
	 * into an instance of this class.
	 */
	public static Phi unmarshal (
			InputStream in) throws Exception {

		Phi obj = new Phi();
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

		if (!xml.matches($PHI, this)) {
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

		if (xml.matches($REPORT,
			Report.nsm, this)) {

			setReport(xml);
			xml = xml.next();
			if (xml == null) {
				return;
			}
		}

		if (xml.matches($QUESTIONNAIRE,
			Questionnaire.nsm, this)) {

			setQuestionnaire(xml);
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
			getReport());
		out.write(null,
			getQuestionnaire());

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
