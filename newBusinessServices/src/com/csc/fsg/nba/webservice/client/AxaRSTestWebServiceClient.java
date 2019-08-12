package com.csc.fsg.nba.webservice.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaTXLife;

public class AxaRSTestWebServiceClient extends NbaWebServiceAdapterBase {

	@Override
	public NbaTXLife invokeWebService(NbaTXLife nbATxLife) throws NbaBaseException {
		return null;
	}

	public Map<String, Object> invokeAxaWebService(Map parameters) throws NbaBaseException {
		try {
			Map<String, Object> outputParams = new HashMap<String, Object>();
			StringBuffer qualifiedFile = new StringBuffer();
			qualifiedFile.append(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.TEST_WS_FOLDER));
			qualifiedFile.append(getTestWsFileName());
			qualifiedFile.append(".txt");
			BufferedReader bfr = new BufferedReader(new FileReader(new File(qualifiedFile.toString())));
			String line;
			while ((line = bfr.readLine()) != null) {
				if (!line.isEmpty()) {
					String[] pair = line.trim().split("=");
					if (pair[0].trim().equalsIgnoreCase("responseCode")) {
						outputParams.put(pair[0].trim(), Integer.valueOf(pair[1].trim()));
					} else {
						outputParams.put(pair[0].trim(), pair[1].trim());
					}
				}
			}
			bfr.close();
			return outputParams;
		} catch (Exception ex) {
			throw new NbaBaseException(ex);
		}
	}
	
}
