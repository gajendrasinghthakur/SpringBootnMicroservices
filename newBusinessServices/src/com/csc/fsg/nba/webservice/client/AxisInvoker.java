/*
 * This software and/or documentation contains trade secrets and
 * confidential information, which are proprietary to
 * Computer Sciences Corporation.
 * The use, reproduction, distribution, or disclosure of this
 * software and/or documentation, in whole or in part, without the express
 * written permission of Computer Sciences Corporation is prohibited.
 * This software and/or documentation is also an unpublished work protected
 * under the copyright laws of the United States of America and other countries.
 * If this software and/or documentation becomes published, the following
 * notice shall apply:
 *
 * Copyright © 2004 Computer Sciences Corporation. All Rights Reserved.
 */
package com.csc.fsg.nba.webservice.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.encoding.TypeMapping;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.utils.Options;
import org.apache.wsif.WSIFException;
import org.apache.wsif.util.WSIFUtils;
import org.w3c.dom.Element;

/** 
 * 
 * This class communicates a transaction over an HTTPS SSL connection.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.68</td><td>Version 6</td><td>LDAP Interface</td></tr>
 * <tr><td>AXAL3.7.22</td><td>AXALife Phase 1</td><td>Compensation Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 6.0.0
 * @since New Business Accelerator - Version 2
 */

public final class AxisInvoker {
	
	private static String URI = "urn:EMPTY";

	private Map callcoll = new HashMap();

	protected WsdlCache wsc = null;

	private String user;
	private String token;
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	
	public String invoke(Map prop, SOAPEnvelope rqstEnv) throws Exception {
		SOAPEnvelope respEnv = null; 
		try {
			String url = (String)prop.get("url");			
			String port = (String)prop.get("port");
			String operationName = (String)prop.get("operationName");
			String soapAction = (String)prop.get("soapAction");
			String errorNS = (String)prop.get("SoapFaultNS");
			String errorClassName = (String)prop.get("SoapFaultClass");
			String errorNode = (String)prop.get("SoapFaultNode");
			String timeOutStr = (String)prop.get("timeout");
			String wsdl = (String)prop.get("wsdl");
			int timeout = 0;
			try {
				timeout = Integer.parseInt(timeOutStr);
			} catch (Throwable th) {}
			if (timeout < 0) {
				timeout = 0;
			}
			
			CallDetails calld = getCallDetails(getUser(), getToken(), timeout, wsdl, operationName, soapAction);
			if (calld != null) {
				List reqParts = calld.getRequestParts();
				List resParts = calld.getResponseParts();
				Call call = calld.getCall();
				call.setTargetEndpointAddress(url);
				if (errorClassName != null && errorClassName.length() > 0) {
			        TypeMappingRegistry registry = call.getService().getTypeMappingRegistry();
			        TypeMapping map = registry.getDefaultTypeMapping();
					Class errorClass = Thread.currentThread().getContextClassLoader().loadClass(errorClassName);
			        QName faultQName = new QName(errorNS, errorNode);
			        map.register(errorClass, faultQName, new BeanSerializerFactory(errorClass, faultQName), new BeanDeserializerFactory(errorClass, faultQName));					
				}
		        respEnv = call.invoke(rqstEnv);
			}
		} catch (AxisFault af) {
			if (af.getFaultDetails().length == 0) {
				throw af;
			}
			//an AXA SOAPException is wrapped in an AxisFault so extract details and send the SOAPException back to the requestor
			try {
				Element[] details = af.getFaultDetails();
				org.apache.xml.serialize.OutputFormat format = new org.apache.xml.serialize.OutputFormat(details[0].getOwnerDocument());
				java.io.StringWriter stringOut = new java.io.StringWriter();
				org.apache.xml.serialize.XMLSerializer serial = new org.apache.xml.serialize.XMLSerializer(stringOut, format);
				serial.asDOMSerializer();
				serial.serialize(details[0].getOwnerDocument().getDocumentElement());
				//AXAL3.7.22 code deleted
				//AXAL3.7.22 begin
				throw new AxisFault(stringOut.toString());
			} catch (AxisFault newAf) {
				throw newAf;//AXAL3.7.22 end
			} catch (Exception e) {
				//throw the original exception if we cannot parse the details
				throw af;
			}
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}
		return respEnv.toString();
	}
	
	public void setWsdlCache(WsdlCache wsc) {
		this.wsc = wsc;
	}
	
	private ExtensibilityElement getExtension(Port port, String name) {
		ExtensibilityElement retval = null;
		List exelms = port.getExtensibilityElements();
		for (int i = 0; i < exelms.size(); i++) {
			ExtensibilityElement exelm = (ExtensibilityElement) exelms.get(i);
			if (exelm.getElementType().getLocalPart().equalsIgnoreCase(name)) {
				return exelm;
			}
		}
		return retval;
	}

	private Port getPort(Service service) {
		Port ret = null;
		Map ports = service.getPorts();
		Iterator itr = ports.values().iterator();
		if (itr.hasNext()) {
			return (Port) itr.next();
		}
		return ret;
	}

	private CallDetails getCallDetails(String username, String token, int timeout, String wsdl, String operationName, String soapAction) 
	throws ServiceException, MalformedURLException, WSIFException, Exception {
		CallDetails calld = null;
		if (wsdl != null && operationName != null) {
			String key = wsdl + ":" + operationName;
			calld = (CallDetails) callcoll.get(key);
			if (calld == null) {
					Definition def = getDefinition(wsdl);
					List parts = null;
					List oparts = null;
					SOAPAddress sa = null;
					String locationURI = null;
					if (def != null) {
						PortType portType =
							WSIFUtils.selectPortType(def, null, null);
						if (portType != null) {
							List operationList = portType.getOperations();
							Operation op = null;
							for (Iterator i = operationList.iterator();
								i.hasNext();
								) {
								op = (Operation) i.next();
								String name = op.getName();
								if (name.equals(operationName)) {
									break;
								}
							}
							Input opInput = op.getInput();
							parts = opInput.getMessage().getOrderedParts(null);
							Output out = op.getOutput();
							oparts = out.getMessage().getOrderedParts(null);
							Service service = WSIFUtils.selectService(def, null, null);
							Port port = getPort(service);
							ExtensibilityElement exelm = getExtension(port, "address");
							sa = (SOAPAddress) exelm;
							locationURI = sa.getLocationURI();
						}
					}
					if (locationURI != null) {
						org.apache.axis.client.Service as = new org.apache.axis.client.Service();
						Call call = null;
						call = (Call) as.createCall();
						Options opts = new Options(new String[2]);
						opts.setDefaultURL(locationURI);
						call.setUsername(username);
						call.setPassword(token);
						call.setTargetEndpointAddress(new URL(opts.getURL()));
						call.setOperation(operationName);
						if(soapAction != null && !soapAction.equals("")){
							call.setSOAPActionURI(soapAction);
						}
						call.setTimeout(new Integer(timeout));
						calld = new CallDetails();
						calld.setCall(call);
						calld.setRequestParts(parts);
						calld.setResponseParts(oparts);
						callcoll.put(key, calld);
					}
			}
		}
		return calld;
	}

	private CallDetails getCallDetails(String username, byte[] password, int timeout, String url, String port, String operation_ns, String operationName, String soapAction) 
		throws ServiceException, MalformedURLException {
		CallDetails calld = null;
		if (url != null && operationName != null) {
			String key = url + ":" + operationName;
			calld = (CallDetails) callcoll.get(key);
			if (calld == null) {
					org.apache.axis.client.Service as = new org.apache.axis.client.Service();
					Call call = null;
					call = (Call) as.createCall();
					Options opts = new Options(new String[2]);
					opts.setDefaultURL(url);
					call.setUsername(username);
					call.setPassword(new String(password));
					call.setTargetEndpointAddress(new URL(opts.getURL()));
					if(operation_ns != null && !operation_ns.equals("")){
						call.setOperationName(new QName(operation_ns,operationName));
					} else {
						call.setOperation(operationName);
					}
					if(soapAction != null && !soapAction.equals("")){
						call.setSOAPActionURI(soapAction);
					}
					call.setTimeout(new Integer(timeout));
					calld = new CallDetails();
					calld.setCall(call);
					callcoll.put(key, calld);
			}
		}
		return calld;
	}
	
	public Definition getDefinition(String wsdlLocation) throws Exception {
		Definition def = null;
		if (wsc != null) {
			def = (Definition) wsc.getWsdl(wsdlLocation);
		}
		if (def == null) {
			def = WSIFUtils.readWSDL(null, wsdlLocation);
			if (wsc != null) {
				wsc.addWsdl(wsdlLocation, def);
			}
		}
		return def;
	}

	private class CallDetails {

		private Service service;
		
		private Call call = null;

		private List requestParts;

		private List responseParts;

		/**
		 * @return
		 */
		public Call getCall() {
			return call;
		}

		/**
		 * @return
		 */
		public List getRequestParts() {
			return requestParts;
		}

		/**
		 * @return
		 */
		public List getResponseParts() {
			return responseParts;
		}

		/**
		 * @param call
		 */
		public void setCall(Call call) {
			this.call = call;
		}

		/**
		 * @param list
		 */
		public void setRequestParts(List list) {
			requestParts = list;
		}

		/**
		 * @param list
		 */
		public void setResponseParts(List list) {
			responseParts = list;
		}
		
		

		/**
		 * @return Returns the service.
		 */
		public Service getService() {
			return service;
		}
		/**
		 * @param servoce The servoce to set.
		 */
		public void setService(Service service) {
			this.service = service;
		}
	}
}
