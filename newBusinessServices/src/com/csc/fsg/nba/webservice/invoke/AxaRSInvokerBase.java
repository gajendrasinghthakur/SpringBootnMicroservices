package com.csc.fsg.nba.webservice.invoke;

import java.util.HashMap;
import java.util.Map;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapter;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapterFactory;

public abstract class AxaRSInvokerBase extends AxaWSInvokerBase {
	
	private String contentType;
	private Map<String, Object> respMap;
	private Object webserviceRequest;
	private NbaWebServiceAdapter service;
	
	protected Map<String, Object> getRespMap() {
		return respMap;
	}

	protected Object getWebserviceRequest() {
		return webserviceRequest;
	}

	protected NbaWebServiceAdapter getService() {
		return service;
	}
	
	protected NbaLogger getLogger() {
		NbaLogger logger = null;
		try {
			logger = NbaLogFactory.getLogger(this.getClass().getName());
		} catch (Exception e) {
			NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
			e.printStackTrace(System.out);
		}

		return logger;
	}

	public Object execute() throws NbaBaseException {
		try {
			if (isCallNeeded()) {
				if (getLogger().isDebugEnabled()) {
					String polNumber = "";
					if (getNbaTXLife() != null) {
						polNumber = getNbaTXLife().getPolicy().getPolNumber();
					}
					getLogger().logDebug(
							"Webservice request for " + getOperation() + (!NbaUtils.isBlankOrNull(polNumber) ? ", policy number: " + polNumber : ""));
					getLogger().logDebug("token: " + getUserVO().getToken());
				}
				service = NbaWebServiceAdapterFactory.createWebServiceAdapter(getBackEnd(), getCategory(), getFunctionId());
				constructURI();
				webserviceRequest = createRequest();
				if (validate()) {
					prepareRequest();
					invoke();
					handleResponse();
				}
			}
		} catch (NbaBaseException ex) {
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug(ex);
			}
			throw ex;
		}
		return getWebserviceResponse();
	}
       
	protected void prepareRequest() throws NbaBaseException {

	}

	protected void constructURI() throws NbaBaseException {

	}

	protected void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	protected void setWebserviceRequest(Object webserviceRequest) {
		this.webserviceRequest = webserviceRequest;
	}

	public AxaRSInvokerBase(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
	}
    
	public final void invoke() throws NbaBaseException {
		Map<String, String> inputParams = new HashMap<String, String>();
		inputParams.put("Content-Type", contentType);
		inputParams.put("X-IBM-Client-Id", System.getProperty("X-IBM-Client-Id"));
		inputParams.put("X-IBM-Client-Secret", System.getProperty("X-IBM-Client-Secret"));
		inputParams.put("token", getUserVO().getToken());
		inputParams.put("body", (String) webserviceRequest);
		respMap = service.invokeAxaWebService(inputParams);
	}
    
	protected void handleResponse() throws NbaBaseException {
		Integer responseCode = (Integer) respMap.get("responseCode");
		Object responseObj = respMap.get("responseObject");
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Response code: " + respMap.get("responseCode"));
			getLogger().logDebug("Response object: " + respMap.get("responseObject"));
		}
		if (responseCode >= 200 && responseCode < 300) { // success codes
			setWebserviceResponse(responseObj);
		} else if (responseCode >= 400 && responseCode < 500) { // soft errors
			throw new NbaBaseException("Webservice failure for category " + getCategory() + " functionId " + getFunctionId() + " while executing "
					+ this.getClass().getName() + " Response Code: " + responseCode);
		} else if (responseCode >= 500 && responseCode < 600) { // hard errors
			throw new NbaBaseException("Error accessing webservice for category " + getCategory() + " functionId " + getFunctionId()
					+ " while executing " + this.getClass().getName() + " Response Code: " + responseCode, NbaExceptionType.FATAL);
		} else { // soft errors
			throw new NbaBaseException("Unexpected webservice response for category " + getCategory() + " functionId " + getFunctionId()
					+ " while executing " + this.getClass().getName() + " Response Code: " + responseCode);
		}
	}
}
