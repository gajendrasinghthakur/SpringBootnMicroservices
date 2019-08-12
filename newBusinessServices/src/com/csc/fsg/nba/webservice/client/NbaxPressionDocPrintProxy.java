package com.csc.fsg.nba.webservice.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import org.apache.soap.Constants;
import org.apache.soap.Fault;
import org.apache.soap.SOAPException;
import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.rpc.Call;
import org.apache.soap.rpc.Parameter;
import org.apache.soap.rpc.Response;
import org.apache.soap.transport.http.SOAPHTTPConnection;

public class NbaxPressionDocPrintProxy
{
  private Call call;
  private URL url = null;
  private String stringURL = "http://danbant06:9082/xPressionAdapter/webservices/docPrint/DocPrint.jws";
  private java.lang.reflect.Method setTcpNoDelayMethod;

  public NbaxPressionDocPrintProxy()
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

  public synchronized java.lang.String getDocumentVariables(java.lang.String userName,java.lang.String password,java.lang.String docName) throws Exception
  {
    String targetObjectURI = "http://DefaultNamespace";
    String SOAPActionURI = "";

    if(getURL() == null)
    {
      throw new SOAPException(Constants.FAULT_CODE_CLIENT,
      "A URL must be specified via docPrint.setEndPoint(URL).");
    }

    call.setMethodName("getDocumentVariables");
    call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
    call.setTargetObjectURI(targetObjectURI);
    Vector params = new Vector(3); // SPR3290
    Parameter userNameParam = new Parameter("userName", java.lang.String.class, userName, Constants.NS_URI_SOAP_ENC);
    params.addElement(userNameParam);
    Parameter passwordParam = new Parameter("password", java.lang.String.class, password, Constants.NS_URI_SOAP_ENC);
    params.addElement(passwordParam);
    Parameter docNameParam = new Parameter("docName", java.lang.String.class, docName, Constants.NS_URI_SOAP_ENC);
    params.addElement(docNameParam);
    call.setParams(params);
    Response resp = call.invoke(getURL(), SOAPActionURI);

    //Check the response.
    if (resp.generatedFault())
    {
      Fault fault = resp.getFault();
      call.setFullTargetObjectURI(targetObjectURI);
      throw new SOAPException(fault.getFaultCode(), fault.getFaultString());
    }
    else
    {
      Parameter refValue = resp.getReturnValue();
      return ((java.lang.String)refValue.getValue());
    }
  }

  public synchronized java.lang.String getListOfCategories(java.lang.String userName,java.lang.String password) throws Exception
  {
    String targetObjectURI = "http://DefaultNamespace";
    String SOAPActionURI = "";

    if(getURL() == null)
    {
      throw new SOAPException(Constants.FAULT_CODE_CLIENT,
      "A URL must be specified via docPrint.setEndPoint(URL).");
    }

    call.setMethodName("getListOfCategories");
    call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
    call.setTargetObjectURI(targetObjectURI);
    Vector params = new Vector(2); // SPR3290
    Parameter userNameParam = new Parameter("userName", java.lang.String.class, userName, Constants.NS_URI_SOAP_ENC);
    params.addElement(userNameParam);
    Parameter passwordParam = new Parameter("password", java.lang.String.class, password, Constants.NS_URI_SOAP_ENC);
    params.addElement(passwordParam);
    call.setParams(params);
    Response resp = call.invoke(getURL(), SOAPActionURI);

    //Check the response.
    if (resp.generatedFault())
    {
      Fault fault = resp.getFault();
      call.setFullTargetObjectURI(targetObjectURI);
      throw new SOAPException(fault.getFaultCode(), fault.getFaultString());
    }
    else
    {
      Parameter refValue = resp.getReturnValue();
      return ((java.lang.String)refValue.getValue());
    }
  }

  public synchronized java.lang.String getListOfDocuments(java.lang.String userName,java.lang.String password,java.lang.String categoryName) throws Exception
  {
    String targetObjectURI = "http://DefaultNamespace";
    String SOAPActionURI = "";

    if(getURL() == null)
    {
      throw new SOAPException(Constants.FAULT_CODE_CLIENT,
      "A URL must be specified via docPrint.setEndPoint(URL).");
    }

    call.setMethodName("getListOfDocuments");
    call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
    call.setTargetObjectURI(targetObjectURI);
    Vector params = new Vector(3); // SPR3290
    Parameter userNameParam = new Parameter("userName", java.lang.String.class, userName, Constants.NS_URI_SOAP_ENC);
    params.addElement(userNameParam);
    Parameter passwordParam = new Parameter("password", java.lang.String.class, password, Constants.NS_URI_SOAP_ENC);
    params.addElement(passwordParam);
    Parameter categoryNameParam = new Parameter("categoryName", java.lang.String.class, categoryName, Constants.NS_URI_SOAP_ENC);
    params.addElement(categoryNameParam);
    call.setParams(params);
    Response resp = call.invoke(getURL(), SOAPActionURI);

    //Check the response.
    if (resp.generatedFault())
    {
      Fault fault = resp.getFault();
      call.setFullTargetObjectURI(targetObjectURI);
      throw new SOAPException(fault.getFaultCode(), fault.getFaultString());
    }
    else
    {
      Parameter refValue = resp.getReturnValue();
      return ((java.lang.String)refValue.getValue());
    }
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
}
