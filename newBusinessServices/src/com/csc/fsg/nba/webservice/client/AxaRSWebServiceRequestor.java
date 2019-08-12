package com.csc.fsg.nba.webservice.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaTXLife;

public class AxaRSWebServiceRequestor extends NbaWebServiceAdapterBase {

	@Override
	public NbaTXLife invokeWebService(NbaTXLife nbATxLife) throws NbaBaseException {
		throw new NbaBaseException("SOAP webservice is not valid for " + this.getClass().getName());
	}

	@Override
	public Object invokeCorrespondenceWebService(String user, String password, String categoryLetter, String type, String xml, String fileName,
			String token, Map keys) throws NbaBaseException {
		throw new NbaBaseException("Correspondence webservice is not valid for " + this.getClass().getName());
	}

	@Override
	public Map<String, Object> invokeAxaWebService(Map parameters) throws NbaBaseException {
		HttpURLConnection connection = null;
		Map<String, Object> outputParams = new HashMap<String, Object>();
		try {
			constructURI();
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("URL: " + getTargetUri());
			}
			URL url = new URL(getTargetUri());
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(getAccess());
			Iterator<Map.Entry<String, String>> itr = parameters.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<String, String> entry = itr.next();
				connection.setRequestProperty(entry.getKey(), entry.getValue());
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Request parameter: " + entry.getKey() + " Value: " + entry.getValue());
				}
			}
			if (((String) parameters.get("body")) != null) {
				connection.setDoOutput(true);
				OutputStream out = connection.getOutputStream();
				out.write(((String) parameters.get("body")).getBytes());
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Body: " + parameters.get("body"));
				}
				out.flush();
				out.close();
			}
			long before = System.currentTimeMillis();
			connection.connect();
			outputParams.put("responseCode", connection.getResponseCode());
			InputStreamReader in = new InputStreamReader(connection.getInputStream());
			BufferedReader br = new BufferedReader(in);
			StringBuffer response = new StringBuffer();
			String output;
			while ((output = br.readLine()) != null) {
				response.append(output);
			}
			long after = System.currentTimeMillis();
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("Time taken to invoke webservice: " + (after - before) + " milliseconds.");
			}
			outputParams.put("responseObject", response.toString());
			br.close();
			in.close();
		} catch (Exception ex) {
			getLogger().logFatalException(ex);
			try {
				InputStreamReader in = new InputStreamReader(connection.getErrorStream());
				BufferedReader br = new BufferedReader(in);
				StringBuffer response = new StringBuffer();
				String output;
				while ((output = br.readLine()) != null) {
					response.append(output);
				}
				outputParams.put("responseObject", response.toString());
				br.close();
				in.close();
			} catch (Exception ex1) {
				ex1.printStackTrace();
			}
//			throw new NbaBaseException("Error invoking webservice: " + ex.getMessage());
		} finally {
			connection.disconnect();
		}
		return outputParams;
	}

	protected void constructURI() {
		String uri = getTargetUri();
		setTargetUri(MessageFormat.format(uri, uriParamsList.toArray()));
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

}
