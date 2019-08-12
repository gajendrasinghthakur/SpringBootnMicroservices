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

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaConfiguration;

public class NbaProviderCommServiceProxy {
  private Call call;
  private URL url = null;
  private String stringURL = "http://localhost:8080/nba_webservice_WAR/servlet/rpcrouter";
  private java.lang.reflect.Method setTcpNoDelayMethod;

  public NbaProviderCommServiceProxy()
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

  public synchronized URL getEndPoint() throws MalformedURLException,NbaBaseException
  {
    return getURL();
  }

  private URL getURL() throws MalformedURLException, NbaBaseException
  {
    if (url == null && stringURL != null && stringURL.length() > 0)
    {
		if( stringURL != null && stringURL.length() > 0) {
			url = new URL(stringURL);
		} else {
			url = new URL(NbaConfiguration.getInstance().getProviders().getProviderAt(0).getUrl());
		}
    }
    return url;
  }

  public synchronized java.lang.String submitProviderRequest(java.lang.String Request) throws Exception
  {
    String targetObjectURI = "http://tempuri.org/com.csc.fsg.nba.access.contract.NbaProviderCommService";
    String SOAPActionURI = "";

    if(getURL() == null)
    {
      throw new SOAPException(Constants.FAULT_CODE_CLIENT,
      "A URL must be specified via NbaProviderCommServiceProxy.setEndPoint(URL).");
    }

    call.setMethodName("submitProviderRequest");
    call.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
    call.setTargetObjectURI(targetObjectURI);
    Vector params = new Vector();
    Parameter RequestParam = new Parameter("Request", java.lang.String.class, Request, Constants.NS_URI_SOAP_ENC);
    params.addElement(RequestParam);
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
/**
 * @return
 */
public String getStringURL() {
	return stringURL;
}

/**
 * @param string
 */
public void setStringURL(String string) {
	stringURL = string;
}

}
