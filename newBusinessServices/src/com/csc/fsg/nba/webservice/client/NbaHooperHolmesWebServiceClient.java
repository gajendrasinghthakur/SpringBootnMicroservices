package com.csc.fsg.nba.webservice.client;

/*
 * **************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group®.  The use,<BR>
 * reproduction, distribution or disclosure of this program, in whole or in<BR>
 * part, without the express written permission of CSC Financial Services<BR>
 * Group is prohibited.  This program is also an unpublished work protected<BR>
 * under the copyright laws of the United States of America and other<BR>
 * countries.  If this program becomes published, the following notice shall<BR>
 * apply:
 *     Property of Computer Sciences Corporation.<BR>
 *     Confidential. Not for publication.<BR>
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************************<BR>
 */
 
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.soap.Body;
import org.apache.soap.Constants;
import org.apache.soap.Envelope;
import org.apache.soap.Header;
import org.apache.soap.SOAPException;
import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.messaging.Message;
import org.apache.soap.rpc.Call;
import org.apache.soap.transport.http.SOAPHTTPConnection;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.csc.fsg.nba.vo.configuration.Provider;

/**
 * WebService client for Hooper Holmes. This class invokes the HPH Web Service to pass the 
 * ACCORD 121 request to Hooper Holmes.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA081</td><td>Version 3</td><td>Hooper Holmes Requirement Ordering and Receipting</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */

public class NbaHooperHolmesWebServiceClient
{
  private Call call;
  private URL url = null;
  private String stringURL = null;//"https://hhws.portamedic.com/DocumentService/ORDER.asmx";
  private java.lang.reflect.Method setTcpNoDelayMethod;
  private Provider provider = null; //ACN012
  
  /**
   * NbaHooperHolmesWebServiceClient constructor.
   * @param newProvider
   */
  //ACN012 CHANGED SIGNATURE
  public NbaHooperHolmesWebServiceClient(Provider newProvider )
  {
	if (newProvider == null) {
		provider = new Provider(); //ACN012
	}else{
		provider = newProvider;
	}
	try
	{
	  setTcpNoDelayMethod = SOAPHTTPConnection.class.getMethod("setTcpNoDelay", new Class[]{Boolean.class});
	}
	catch (Exception e)
	{
	}
	call = createCall();
  }
/**
 * This method sets the target URL 
 * @param url URL of the webservice provider
 */
  public synchronized void setEndPoint(URL url)
  {
	this.url = url;
  }
  /**
   * This method retrieves the target URL
   * @return URL of the webservice provider 
   */
  public synchronized URL getEndPoint() throws MalformedURLException
  {
	return getURL();
  }
	
 /**
 * This method retrieves the target URL
 * @return URL of the webservice provider 
 */
  private URL getURL() throws MalformedURLException
  {
	stringURL = provider.getUrl();
	return new URL(stringURL);
  }
	
  /**
   * This method submits request to web service
   * @param paramaeters org.w3c.dom.Element object which contains the web service request
   * @return response received from web service 
   */
  public synchronized String SubmitOrder_(org.w3c.dom.Element parameters) throws Exception
  {
	String targetObjectURI = "null";
	String SOAPActionURI = "https://hhws.portamedic.com/DocumentService/SubmitOrder";

	if(getURL() == null)
	{
	  throw new SOAPException(Constants.FAULT_CODE_CLIENT,
	  "A URL must be specified via OrderProxy.setEndPoint(URL).");
	}

	//Create an XML Document
	DocumentBuilderFactory dbFactory = DocumentBuilderFactoryImpl.newInstance();
	DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
	Document xmlDoc = docBuilder.newDocument();

	Element wSSecurity = xmlDoc.createElement("WSSecurity");
	wSSecurity.setAttribute("xmlns","https://hhws.portamedic.com/DocumentService/");

	Element credentials = xmlDoc.createElement("Credentials");
	wSSecurity.appendChild(credentials);

	Element username = xmlDoc.createElement("Username");
	username.appendChild(xmlDoc.createTextNode(provider.getUser()));

	Element password = xmlDoc.createElement("Password");
	password.appendChild(xmlDoc.createTextNode(provider.getPassword()));
	
	credentials.appendChild(username);
	credentials.appendChild(password);	
		
	// create message envelope, header and body
	Envelope msgEnv = new Envelope();
		
	// header
	Header msgHeader = new Header();
	Vector headVect = new Vector();
	headVect.add(wSSecurity);
	msgHeader.setHeaderEntries(headVect);	
	msgEnv.setHeader(msgHeader);

	//body
	Body msgBody = new Body();
	Vector bodyVect = new Vector();
    
	String ownerStr = DOM2String(parameters.getOwnerDocument());
	Element submitOrder = parameters.getOwnerDocument().createElement("SubmitOrder");
	submitOrder.setAttribute("xmlns","https://hhws.portamedic.com/DocumentService/");
	Element orderData = parameters.getOwnerDocument().createElement("OrderData");	
	
	orderData.appendChild(parameters.getOwnerDocument().createTextNode(ownerStr));
	submitOrder.appendChild(orderData);
	bodyVect.add(submitOrder);
	msgBody.setBodyEntries(bodyVect);
	msgEnv.setBody(msgBody);

	// create and send message
	Message msg = new Message();
	SOAPHTTPConnection soapHTTPConnection = new SOAPHTTPConnection();    
	soapHTTPConnection.setTimeout(90000000);
	msg.setSOAPTransport(soapHTTPConnection);
	msg.send(getURL(),SOAPActionURI, msgEnv);

	// receive response envelope
	Envelope env = msg.receiveEnvelope();
	Body retbody = env.getBody();
	java.util.Vector v = retbody.getBodyEntries();
	
	return domWriter((Element)v.firstElement(), new java.lang.StringBuffer());
    
  }

 /**
  * Creates an object of org.apache.soap.rpc.Call
  * @return object of Call
  */
  protected Call createCall(){
	SOAPHTTPConnection soapHTTPConnection = new SOAPHTTPConnection();
	if ( setTcpNoDelayMethod != null)
	{
	  try
	  {
		setTcpNoDelayMethod.invoke(soapHTTPConnection, new Object[]{Boolean.TRUE});
	  }
	  catch (Exception ex)
	  {
	  }
	}
	Call call = new Call();
	call.setSOAPTransport(soapHTTPConnection);
	SOAPMappingRegistry smr = call.getSOAPMappingRegistry();
	return call;
  }
  /**
  * Receives a document and converts it into a String
  * @param doc Document which contains the element to be converted into String
  * @return String 
  */
	public static String DOM2String(Document doc) throws java.io.IOException {

		StringWriter sw = new StringWriter();
		OutputFormat oFormatter = new OutputFormat("XML", null, false);
		XMLSerializer oSerializer = new XMLSerializer(sw, oFormatter);
		oSerializer.serialize(doc);
		return sw.toString();
	}
/**
 * Replaces <, >, &, ", with equivalent chars
 * @param text Original string
 * @return String The string with replaced values
 */
public static String markup(String text) {
	if (text == null) {
		return null;
	}

	StringBuffer buffer = new StringBuffer();
	for (int i = 0; i < text.length(); i++) {
		char c = text.charAt(i);
		switch (c) {
			case '<':
				buffer.append("&lt;");
				break;
			case '&':
				buffer.append("&amp;");
				break;
			case '>':
				buffer.append("&gt;");
				break;
			case '"':
				buffer.append("&quot;");
				break;
			default:
				buffer.append(c);
				break;
		}
	}
	return buffer.toString();
}

/**
 * Converts org.w3c.dom.Node to String
 * @param node The node to be converted
 * @param buffer String buffer
 * @return String Converted string
 */
public static java.lang.String domWriter(org.w3c.dom.Node node,java.lang.StringBuffer buffer)
{
	if ( node == null ) {
		return "";
	}
	int type = node.getNodeType();
	switch ( type ) {
		case org.w3c.dom.Node.DOCUMENT_NODE: {
			buffer.append(markup("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") + "<br>");
			domWriter(((org.w3c.dom.Document)node).getDocumentElement(),buffer);
			break;
		}
		case org.w3c.dom.Node.ELEMENT_NODE: {
			 buffer.append(markup("<" + node.getNodeName()));
			org.w3c.dom.Attr attrs[] = sortAttributes(node.getAttributes());
			for ( int i = 0; i < attrs.length; i++ ) {
				org.w3c.dom.Attr attr = attrs[i];
				buffer.append(" " + attr.getNodeName() + "=\"" + markup(attr.getNodeValue()) + "\"");
			}
			 buffer.append(markup(">"));
			org.w3c.dom.NodeList children = node.getChildNodes();
			if ( children != null ) {
				int len = children.getLength();
				for ( int i = 0; i < len; i++ ) {
				if(((org.w3c.dom.Node)children.item(i)).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
				buffer.append("<br>");
				domWriter(children.item(i),buffer);
				}
			}
			buffer.append(markup("</" + node.getNodeName() + ">"));
			break;
		}
		case org.w3c.dom.Node.ENTITY_REFERENCE_NODE: {
			org.w3c.dom.NodeList children = node.getChildNodes();
			if ( children != null ) {
				int len = children.getLength();
				for ( int i = 0; i < len; i++ )
				{
				buffer.append(children.item(i));
				}
			}
			break;
		}
		case org.w3c.dom.Node.CDATA_SECTION_NODE: {
			buffer.append(markup(node.getNodeValue()));
			break;
		}
		case org.w3c.dom.Node.TEXT_NODE:{
			buffer.append(markup(node.getNodeValue()));
			break;
		}
		case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:{
			buffer.append(markup("<?"));
			buffer.append(node.getNodeName());
			String data = node.getNodeValue();
			if ( data != null && data.length() > 0 ){
				buffer.append(" ");
				buffer.append(data);
			}
			buffer.append(markup("?>"));
			break;
		}
		}
	return buffer.toString();
}

/**
 * Sorts the attributes
 * @param attrs An instance of org.w3c.dom.NamedNodeMap
 * @return An array of org.w3c.dom.Attr objects
 */
public static org.w3c.dom.Attr[] sortAttributes(org.w3c.dom.NamedNodeMap attrs)
{
	int len = (attrs != null) ? attrs.getLength() : 0;
	org.w3c.dom.Attr array[] = new org.w3c.dom.Attr[len];
	for ( int i = 0; i < len; i++ ){
		array[i] = (org.w3c.dom.Attr)attrs.item(i);
	}
	for ( int i = 0; i < len - 1; i++ ) {
		String name  = array[i].getNodeName();
		int    index = i;
		for ( int j = i + 1; j < len; j++ ) {
			String curName = array[j].getNodeName();
			if ( curName.compareTo(name) < 0 ) {
				name  = curName;
				index = j;
			}
		}
		if ( index != i ) {
			org.w3c.dom.Attr temp    = array[i];
			array[i]     = array[index];
			array[index] = temp;
		}
	}
	return (array);
}
}
