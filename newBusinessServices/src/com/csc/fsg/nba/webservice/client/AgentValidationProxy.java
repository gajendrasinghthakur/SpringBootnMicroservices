package com.csc.fsg.nba.webservice.client;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.apache.soap.Body;
import org.apache.soap.Constants;
import org.apache.soap.Envelope;
import org.apache.soap.SOAPException;
import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.messaging.Message;
import org.apache.soap.rpc.Call;
import org.apache.soap.transport.http.SOAPHTTPConnection;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.csc.fsg.life.tools.xml.XmlWriter;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaTXLife;

public class AgentValidationProxy extends NbaWebServiceAdapterBase 
{
  private Call call;
  private URL url = null;
  private String stringURL = "http://ehrpsnt01:9082/lifews/services/AgentValidation";
  private java.lang.reflect.Method setTcpNoDelayMethod;
  private NbaTXLife agentValResponse = null;
  private static NbaLogger logger = null;

  public AgentValidationProxy()
  {
    try
    {
      setTcpNoDelayMethod = SOAPHTTPConnection.class.getMethod("setTcpNoDelay", new Class[]{Boolean.class});
    }
    catch (Exception e)
    {
    }
    call = createCall();
  }

  public synchronized void setEndPoint(URL url)
  {
    this.url = url;
  }

  public synchronized URL getEndPoint() throws MalformedURLException
  {
    return getURL();
  }

  private URL getURL() throws MalformedURLException
  {
    if (url == null && stringURL != null && stringURL.length() > 0)
    {
      url = new URL(stringURL);
    }
    return url;
  }

  /**
   * After getting the instance of this client class using Factory classes, this 
   * invokeWebService method will be used to call the Agent Validation WebService. 
   * @param nbATxLife NbaTXLife
   * @param obj Object
   * @return NbaTXLife
   */
  public NbaTXLife invokeWebService(NbaTXLife nbATxLife) { // SPR2968
	  try {
		  StringReader stringReader = new java.io.StringReader(nbATxLife.toXmlString());

		  InputSource inputSource = new org.xml.sax.InputSource(stringReader);
		  DOMParser domParser = new org.apache.xerces.parsers.DOMParser();
		  domParser.parse(inputSource);
		  Document document = domParser.getDocument();
		  Element passedEle = document.getDocumentElement();

		  try {
			  setTcpNoDelayMethod = SOAPHTTPConnection.class.getMethod("setTcpNoDelay", new Class[] { Boolean.class });
		  } catch (Exception e) {
		  }

		 // call = createCall();
		  Element responseEle = service(passedEle);
		  XmlWriter xmlwriter = new XmlWriter();
		  StringBuffer sb = new StringBuffer();
		  xmlwriter.printDOM(responseEle, sb);
		agentValResponse = new NbaTXLife(Element2String(responseEle));
		  
	  } catch (Exception e) {
		if (getLogger().isErrorEnabled())
			   getLogger().logError(e);
	  }
	  return agentValResponse;
  }

  public synchronized org.w3c.dom.Element service(org.w3c.dom.Element part) throws Exception
  {
    String targetObjectURI = getTargetUri();
    String SOAPActionURI = "";

    if(getURL() == null)
    {
      throw new SOAPException(Constants.FAULT_CODE_CLIENT,
      "A URL must be specified via AgentValidationProxy.setEndPoint(URL).");
    }

    // create message envelope and body
    Envelope msgEnv = new Envelope();
    Body msgBody = new Body();
    Vector vect = new Vector();

    vect.add(part);
    msgBody.setBodyEntries(vect);
    msgEnv.setBody(msgBody);

	java.net.URL url = new java.net.URL(getWsdlUrl());
    // create and send message
    Message msg = new Message();
    msg.send(url, SOAPActionURI, msgEnv);

    // receive response envelope
    Envelope env = msg.receiveEnvelope();
    Body retbody = env.getBody();
    java.util.Vector v = retbody.getBodyEntries();

    return (Element)v.firstElement();
    
  }

  protected Call createCall()
  {
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
	* Receives an Element and converts it into a String
	* @param ele Element which contains the element to be converted into String
	* @return String 
	*/
   protected static String Element2String(Element ele) throws java.io.IOException {
	   StringWriter sw = new StringWriter();
	   OutputFormat oFormatter = new OutputFormat("XML", null, false);
	   XMLSerializer oSerializer = new XMLSerializer(sw, oFormatter);
	   oSerializer.serialize(ele);
	   return sw.toString();
   }  

   /**
	* Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	* @return the logger implementation
	*/
   private static NbaLogger getLogger() {
	   if (logger == null) {
		   try {
			   logger = NbaLogFactory.getLogger(AgentValidationProxy.class.getName()); //NBA103
		   } catch (Exception e) {
			   NbaBootLogger.log("AccountEntry could not get a logger from the factory.");
			   e.printStackTrace(System.out);
		   }
	   }
	   return logger;
   }
  
}
