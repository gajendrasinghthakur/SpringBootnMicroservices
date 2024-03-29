/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which are
 * proprietary to CSC Financial Services Group�.  The use, reproduction,
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

import com.tbf.xml.XmlDbObject;
import com.tbf.xml.XmlElement;
import com.tbf.xml.XmlObject;
import com.tbf.xml.XmlObjectFactory;

/**
 * This class provides methods to create instances of
 * the objects in this class library (project).
 *
 * @see com.tbf.xml.XmlObjectFactory
 */
public class ObjectFactory extends XmlObjectFactory {

	protected static java.util.Hashtable _default_map;
	static {
		/**
		 * Populate the default XML node name to class name map.
		 */
		_default_map = new java.util.Hashtable();
		_default_map.put("Correspondence",
			"com.csc.fsg.nba.correspondence.docprintschema_extract.Correspondence");
		_default_map.put("Extract",
			"com.csc.fsg.nba.correspondence.docprintschema_extract.Extract");
		_default_map.put("Forms",
			"com.csc.fsg.nba.correspondence.docprintschema_extract.Forms");
		_default_map.put("Form",
			"com.csc.fsg.nba.correspondence.docprintschema_extract.Form");
		_default_map.put("Data",
			"com.csc.fsg.nba.correspondence.docprintschema_extract.Data");
	}


	protected static java.util.Hashtable _default_dbmap;
	static {
		/**
		 * Populate the default DB table name to class name map.
		 */
		_default_dbmap = new java.util.Hashtable();
	}



	/*
	 * Create the default ObjectFactory
	 */
	protected static XmlObjectFactory _default_factory = getInstance();


	/**
	 * Get the default XmlObjectFactory instance.
	 */
	public static XmlObjectFactory getDefaultFactory () {
		return (_default_factory);
	}


	/**
	 * Set the default XmlObjectFactory instance.
	 */
	public static void setDefaultFactory (XmlObjectFactory factory) {

		if (factory != null) {
			_default_factory = factory;
		} else {
			_default_factory = getInstance();
		}
	}


	/**
	 * Create an instance of this ObjectFactory with no bindings set.
	 */
	protected ObjectFactory () {
	}


	/**
	 * Creates an instance of this ObjectFactory and populates its
	 * node to class name and column to class name maps with the
	 * defaults.
	 */
	public static ObjectFactory getInstance () {

		ObjectFactory factory = new ObjectFactory();
		factory.setXmlMap(_default_map);
		factory.setDbMap(_default_dbmap);

		return (factory);
	}


	/**
	 * Get last error reported by the default ObjectFactory 
	 * instance for this particular ObjectFactory. The default
	 * factory is used by all the static createObject methods
	 * in this class.
	 *
	 * @return  the last error reported by the default ObjectFactory 
	 *          instance for this particular ObjectFactory
	 * @see #getDefaultFactoryLastException()
	 * @deprecated  will be removed in a future release. 
	 * Use com.tbf.xml.XmlObjectFactory.getLastError().
	 */
	public static String getDefaultFactoryLastError () {
		return (_default_factory.getLastError());
	}


	/**
	 * Clear the default factory last error message.
	 *
	 * @deprecated  will be removed in a future release. 
	 * Use com.tbf.xml.XmlObjectFactory.clearLastError().
	 */
	public static void clearDefaultFactoryLastError () {
		_default_factory.clearLastError();
	}


	/**
	 * Get last exception reported by the default ObjectFactory 
	 * instance for this particular ObjectFactory. The default
	 * factory is used by all the static createObject methods
	 * in this class.
	 *
	 * @return  the last exception reported by the default ObjectFactory 
	 *          instance for this particular ObjectFactory
	 * @see #getDefaultFactoryLastError()
	 * @deprecated  will be removed in a future release. 
	 * Use com.tbf.xml.XmlObjectFactory.getLastException().
	 */
	public static Exception getDefaultFactoryLastException () {
		return (_default_factory.getLastException());
	}


	/**
	 * Clear the default factory last exception.
	 *
	 * @deprecated  will be removed in a future release. 
	 * Use com.tbf.xml.XmlObjectFactory.clearLastException().
	 */
	public static void clearDefaultFactoryLastException () {
		_default_factory.setLastException(null);
	}


	/**
	 * Set the XML name to class name binding on the
	 * default ObjectFactory instance.
	 *
	 * @param  name  the name of the binding to get
	 * @param  class_name the class name that the XML name is to be bound to
	 * @deprecated  will be removed in a future release. 
	 * Use com.tbf.xml.XmlObjectFactory.setXmlBinding(String, String).
	 */
	public static void setDefaultFactoryXmlBinding (
			String name, String class_name) {

		_default_factory.setXmlBinding(name, class_name);
		_default_map.put(name, class_name);
	}


	/**
	 * Set the XML name to XmlObjectFactory binding on the
	 * default ObjectFactory instance.
	 *
	 * @param  name  the name of the binding to get
	 * @param  factory  the XmlObjectFactory that the XML name
	 *                  is to be bound to
	 * @deprecated  will be removed in a future release. 
	 * Use com.tbf.xml.XmlObjectFactory.setXmlBinding(String, XmlObjectFactory).
	 */
	public static void setDefaultFactoryXmlBinding (
			String name, XmlObjectFactory factory) {

		_default_factory.setXmlBinding(name, factory);
	}


	/**
	 * Create an instance of the specified class name and
	 * populate it with the specified XML data.
	 *
	 * @param  name  the class name of the object to create
	 * @param  xml   the XmlElement (tree) containg the data for the 
	 *               created object
	 * @return the created and populated object
	 * @deprecated  will be removed in a future release. 
	 * Use com.tbf.xml.XmlObjectFactory.getInstance(String, XmlElement).
	 */
	public static XmlObject createObject (String name, XmlElement xml) {

		XmlObjectFactory factory = xml.getObjectFactory();
		if (factory != null) {
			return (factory.getInstance(name, xml));
		} else {
			return (_default_factory.getInstance(name, xml));
		}
	}


	/**
	 * Create an instance of the class specified in the InputStream
	 * populate it with the XML data on that stream.
	 *
	 * @param  in  the InputStream containg the data to create and populate
	 *             an XmlObject
	 * @return the created and populated object
	 * @deprecated  will be removed in a future release. 
	 * Use com.tbf.xml.XmlObjectFactory.getInstance(java.io.InputStream).
	 */
	public static XmlObject createObject (java.io.InputStream in) {
		return (_default_factory.getInstance(in));
	}


	/**
	 * Create an instance of the class from the XML string.
	 *
	 * @param  xml  the String containing the XML data to create and
	 *              populate an XmlObject
	 * @return the created and populated object
	 * @deprecated  will be removed in a future release. 
	 * Use com.tbf.xml.XmlObjectFactory.getInstanceFromString(String).
	 */
	public static XmlObject createObjectFromString (String xml) {
		return (_default_factory.getInstanceFromString(xml));
	}


	/**
	 * Create an instance of the class from the XML file pointed
	 * to by the provided URL string.
	 *
	 * @param  url  the URL to open and read to create and
	 *                   populate an XmlObject
	 * @return the created and populated object
	 * @deprecated  will be removed in a future release. 
	 * Use com.tbf.xml.XmlObjectFactory.getInstanceFromUrl(String).
	 */
	public static XmlObject createObjectFromUrl (String url) {
		return (_default_factory.getInstanceFromUrl(url));
	}


	/**
	 * Create an instance of the class specified in the XML
	 * and populate it with that XML data.
	 *
	 * @param  xml   the XmlElement (tree) containg the data for the 
	 *               created object
	 * @return the created and populated object
	 * @deprecated  will be removed in a future release. 
	 * Use com.tbf.xml.XmlObjectFactory.getInstance(XmlElement).
	 */
	public static XmlObject createObject (XmlElement xml) {

		XmlObjectFactory factory = xml.getObjectFactory();
		if (factory != null) {
			return (factory.getInstance(xml));
		} else {
			return (_default_factory.getInstance(xml));
		}
	}


	/**
	 * Create and populate an instance of the class specified in the
	 * XML document file.
	 *
	 * @param  f  the File to open and use to create and
	 *                   populate an XmlObject
	 * @return the created and populated object
	 * @deprecated  will be removed in a future release. 
	 * Use com.tbf.xml.XmlObjectFactory.getInstance(java.io.File).
	 */
	public static XmlObject createObject (java.io.File f) {
		return (_default_factory.getInstance(f));
	}


	/**
	 * Create and populate an instance of the class specified in
	 * the XML filename.
	 *
	 * @param  filename  the filename to open and use to create and
	 *                   populate an XmlObject
	 * @return the created and populated object
	 * @deprecated  will be removed in a future release. 
	 * Use com.tbf.xml.XmlObjectFactory.getInstance(String).
	 */
	public static XmlObject createObject (String filename) {
		return (_default_factory.getInstance(filename));
	}


	/**
	 * Create and populate a Vector of objects from
	 * the passed in XML tree.
	 *
	 * @param  xml   the XmlElement (tree) containg the data for the 
	 *               created list.
	 * @return the created and populated list (Vector)
	 * @deprecated  will be removed in a future release. 
	 * Use com.tbf.xml.XmlObjectFactory.getList(XmlElement).
	 */
	public static java.util.Vector createList (XmlElement xml) {

		XmlObjectFactory factory = xml.getObjectFactory();
		if (factory != null) {
			return (factory.getList(xml));
		} else {
			return (_default_factory.getList(xml));
		}
	}


	/**
	 * Create and populate an instance of a XmlDbObject based on
	 * the table name contained in the ResultSet.
	 *
	 * @param  rs  the ResultSet containg the data to create and
	 *             populate an XmlDbObject
	 * @return the created and populated object
	 * @deprecated  will be removed in a future release. 
	 * Use com.tbf.xml.XmlObjectFactory.getInstance(java.sql.ResultSet).
	 */
	public static XmlDbObject createObject (java.sql.ResultSet rs) {
		return (_default_factory.getInstance(rs));
	}


	/**
	 * Create a Vector and populate it with instances of XmlDbObjects
	 * based on the table name contained in the ResultSet.
	 *
	 * @param  rs  the ResultSet containg the data to create and
	 *             populate an XmlDbObject
	 * @return a Vector containg the created and populated XmlDbObjects
	 * @deprecated  will be removed in a future release. 
	 * Use com.tbf.xml.XmlObjectFactory.getList(java.sql.ResultSet).
	 */
	public static java.util.Vector createList (java.sql.ResultSet rs) {
		return (_default_factory.getList(rs));
	}
}
