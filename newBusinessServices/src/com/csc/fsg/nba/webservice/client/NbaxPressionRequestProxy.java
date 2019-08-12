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

public class NbaxPressionRequestProxy
{
  private Call call;
  private URL url = null;
  private String stringURL = "http://danbant06:9082/xPressionAdapter/webservices/xPressionRequest.jws";
  private java.lang.reflect.Method setTcpNoDelayMethod;

  public NbaxPressionRequestProxy()
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

  public synchronized java.lang.String[] categoriesForUser(java.lang.String userName,java.lang.String password) throws Exception
  {
    String targetObjectURI = "http://DefaultNamespace";
    String SOAPActionURI = "";

    if(getURL() == null)
    {
      throw new SOAPException(Constants.FAULT_CODE_CLIENT,
      "A URL must be specified via xPressionRequestProxy.setEndPoint(URL).");
    }

    call.setMethodName("categoriesForUser");
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
      return ((java.lang.String[])refValue.getValue());
    }
  }

  public synchronized java.lang.String[] documentsForCategory(java.lang.String userName,java.lang.String password,java.lang.String documentCategory) throws Exception
  {
    String targetObjectURI = "http://DefaultNamespace";
    String SOAPActionURI = "";

    if(getURL() == null)
    {
      throw new SOAPException(Constants.FAULT_CODE_CLIENT,
      "A URL must be specified via xPressionRequestProxy.setEndPoint(URL).");
    }

    call.setMethodName("documentsForCategory");
    call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
    call.setTargetObjectURI(targetObjectURI);
    Vector params = new Vector(3); // SPR3290
    Parameter userNameParam = new Parameter("userName", java.lang.String.class, userName, Constants.NS_URI_SOAP_ENC);
    params.addElement(userNameParam);
    Parameter passwordParam = new Parameter("password", java.lang.String.class, password, Constants.NS_URI_SOAP_ENC);
    params.addElement(passwordParam);
    Parameter documentCategoryParam = new Parameter("documentCategory", java.lang.String.class, documentCategory, Constants.NS_URI_SOAP_ENC);
    params.addElement(documentCategoryParam);
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
      return ((java.lang.String[])refValue.getValue());
    }
  }

  public synchronized java.lang.String[] outputProfilesForDocument(java.lang.String documentName,java.lang.String userName,java.lang.String password) throws Exception
  {
    String targetObjectURI = "http://DefaultNamespace";
    String SOAPActionURI = "";

    if(getURL() == null)
    {
      throw new SOAPException(Constants.FAULT_CODE_CLIENT,
      "A URL must be specified via xPressionRequestProxy.setEndPoint(URL).");
    }

    call.setMethodName("outputProfilesForDocument");
    call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
    call.setTargetObjectURI(targetObjectURI);
    Vector params = new Vector(3); // SPR3290
    Parameter documentNameParam = new Parameter("documentName", java.lang.String.class, documentName, Constants.NS_URI_SOAP_ENC);
    params.addElement(documentNameParam);
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
      return ((java.lang.String[])refValue.getValue());
    }
  }

  public synchronized java.lang.String postForBatch(java.lang.String batchDocumentType,java.lang.String userName,java.lang.String password,java.lang.String customerData) throws Exception
  {
    String targetObjectURI = "http://DefaultNamespace";
    String SOAPActionURI = "";

    if(getURL() == null)
    {
      throw new SOAPException(Constants.FAULT_CODE_CLIENT,
      "A URL must be specified via xPressionRequestProxy.setEndPoint(URL).");
    }

    call.setMethodName("postForBatch");
    call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
    call.setTargetObjectURI(targetObjectURI);
    Vector params = new Vector(4); // SPR3290
    Parameter batchDocumentTypeParam = new Parameter("batchDocumentType", java.lang.String.class, batchDocumentType, Constants.NS_URI_SOAP_ENC);
    params.addElement(batchDocumentTypeParam);
    Parameter userNameParam = new Parameter("userName", java.lang.String.class, userName, Constants.NS_URI_SOAP_ENC);
    params.addElement(userNameParam);
    Parameter passwordParam = new Parameter("password", java.lang.String.class, password, Constants.NS_URI_SOAP_ENC);
    params.addElement(passwordParam);
    Parameter customerDataParam = new Parameter("customerData", java.lang.String.class, customerData, Constants.NS_URI_SOAP_ENC);
    params.addElement(customerDataParam);
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

  public synchronized byte[] previewPDF(java.lang.String documentName,java.lang.String userName,java.lang.String password,java.lang.String outputProfile,java.lang.String transformation,java.lang.String customerData) throws Exception
  {
    String targetObjectURI = "http://DefaultNamespace";
    String SOAPActionURI = "";

    if(getURL() == null)
    {
      throw new SOAPException(Constants.FAULT_CODE_CLIENT,
      "A URL must be specified via xPressionRequestProxy.setEndPoint(URL).");
    }

    call.setMethodName("previewPDF");
    call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
    call.setTargetObjectURI(targetObjectURI);
    Vector params = new Vector(6); // SPR3290
    Parameter documentNameParam = new Parameter("documentName", java.lang.String.class, documentName, Constants.NS_URI_SOAP_ENC);
    params.addElement(documentNameParam);
    Parameter userNameParam = new Parameter("userName", java.lang.String.class, userName, Constants.NS_URI_SOAP_ENC);
    params.addElement(userNameParam);
    Parameter passwordParam = new Parameter("password", java.lang.String.class, password, Constants.NS_URI_SOAP_ENC);
    params.addElement(passwordParam);
    Parameter outputProfileParam = new Parameter("outputProfile", java.lang.String.class, outputProfile, Constants.NS_URI_SOAP_ENC);
    params.addElement(outputProfileParam);
    Parameter transformationParam = new Parameter("transformation", java.lang.String.class, transformation, Constants.NS_URI_SOAP_ENC);
    params.addElement(transformationParam);
    Parameter customerDataParam = new Parameter("customerData", java.lang.String.class, customerData, Constants.NS_URI_SOAP_ENC);
    params.addElement(customerDataParam);
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
      return ((byte[])refValue.getValue());
    }
  }

  public synchronized java.lang.String publishDocument(java.lang.String documentName,java.lang.String userName,java.lang.String password,java.lang.String outputProfile,java.lang.String transformation,java.lang.String customerData) throws Exception
  {
    String targetObjectURI = "http://DefaultNamespace";
    String SOAPActionURI = "";

    if(getURL() == null)
    {
      throw new SOAPException(Constants.FAULT_CODE_CLIENT,
      "A URL must be specified via xPressionRequestProxy.setEndPoint(URL).");
    }

    call.setMethodName("publishDocument");
    call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
    call.setTargetObjectURI(targetObjectURI);
    Vector params = new Vector(6); // SPR3290
    Parameter documentNameParam = new Parameter("documentName", java.lang.String.class, documentName, Constants.NS_URI_SOAP_ENC);
    params.addElement(documentNameParam);
    Parameter userNameParam = new Parameter("userName", java.lang.String.class, userName, Constants.NS_URI_SOAP_ENC);
    params.addElement(userNameParam);
    Parameter passwordParam = new Parameter("password", java.lang.String.class, password, Constants.NS_URI_SOAP_ENC);
    params.addElement(passwordParam);
    Parameter outputProfileParam = new Parameter("outputProfile", java.lang.String.class, outputProfile, Constants.NS_URI_SOAP_ENC);
    params.addElement(outputProfileParam);
    Parameter transformationParam = new Parameter("transformation", java.lang.String.class, transformation, Constants.NS_URI_SOAP_ENC);
    params.addElement(transformationParam);
    Parameter customerDataParam = new Parameter("customerData", java.lang.String.class, customerData, Constants.NS_URI_SOAP_ENC);
    params.addElement(customerDataParam);
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

  public synchronized java.lang.String[] variablesForDocument(java.lang.String documentName,java.lang.String userName,java.lang.String password) throws Exception
  {
    String targetObjectURI = "http://DefaultNamespace";
    String SOAPActionURI = "";

    if(getURL() == null)
    {
      throw new SOAPException(Constants.FAULT_CODE_CLIENT,
      "A URL must be specified via xPressionRequestProxy.setEndPoint(URL).");
    }

    call.setMethodName("variablesForDocument");
    call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
    call.setTargetObjectURI(targetObjectURI);
    Vector params = new Vector(3); // SPR3290
    Parameter documentNameParam = new Parameter("documentName", java.lang.String.class, documentName, Constants.NS_URI_SOAP_ENC);
    params.addElement(documentNameParam);
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
      return ((java.lang.String[])refValue.getValue());
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
