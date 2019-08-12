package com.csc.fsg.nba.process.contract;

import java.util.Iterator;
import java.util.Map;

import com.axa.fs.accel.console.valueobject.ExecuteUtilityProcessVO;

/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group�.  The use,<BR>
 * reproduction, distribution or disclosure of this program, in whole or in<BR>
 * part, without the express written permission of CSC Financial Services<BR>
 * Group is prohibited.  This program is also an unpublished work protected<BR>
 * under the copyright laws of the United States of America and other<BR>
 * countries.  If this program becomes published, the following notice shall<BR>
 * apply:
 *     Property of Computer Sciences Corporation.<BR>
 *     Confidential. Not for publication.<BR>
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.configuration.Function;


/**
 * Commits a base plan change request for a contract.  The contract will be modified
 * to remove contract information that is no longer applicable due to the plan change.
 * A comment will also be added which audits this plan change.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <tr><td>NBLXA-1538</td><td>Version 7</td><td>Distribution Channel Update for TconV</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class ModifyNbaConfiguration extends NewBusinessAccelBP {

  	@Override
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			ExecuteUtilityProcessVO vo = (ExecuteUtilityProcessVO) input;
			Map webservice = vo.getValueToModify();
			Iterator entries = webservice.entrySet().iterator();
			String operation = null;
			String[] value = null;
			String webServiceName="";
			while (entries.hasNext()) {
				Map.Entry entry = (Map.Entry) entries.next();
				operation = (String) entry.getKey();
				value = (String[]) entry.getValue();
				webServiceName=value[0];
				System.out.println("operation = " + operation + ", Value = " + value[0]);
			}

			String[] backEnd = new String[] { NbaConstants.SYST_CAPS, NbaConstants.SYST_LIFE70, NbaConstants.SYST_ANDESA };
			for (int i = 0; i < backEnd.length; i++) {
				if (operation.equals("Unstub")) {
					if (i == 0) {
						NbaConfiguration.getInstance().reinitialize();
					}
					if (webServiceName.equalsIgnoreCase("MIB")) {
						Function function = NbaConfiguration.getInstance().getIntegrationCategoryByFunction(backEnd[i], webServiceName);
						if (function != null) {
							function.setWebService("AxaEibProviderServices");
						}
					}
					if (webServiceName.equalsIgnoreCase("MIBIND")) {
					NbaConfiguration.getInstance().setBusinessRulesAttributeValue(NbaConfigurationConstants.MIB_EVT_SUPPRESS_TRANSLATIONS_INDICATOR,"true");
					}
				}
				if (operation.equals("Stub")) {
					Function function = NbaConfiguration.getInstance().getIntegrationCategoryByFunction(backEnd[i], webServiceName);
					if(function!=null) {
					function.setWebService("TestService");
					}
					if(webServiceName.equals("retrieveClientRiskScore") || webServiceName.equals("searchCustomer") || webServiceName.equals("bridgerRetrieveCIData") || webServiceName.equals("CIFServiceTransmit")) {
						NbaConfiguration.getInstance().setBusinessRulesAttributeValue(NbaConfigurationConstants.ENABLE_CLIENT_INTERFACE_CALL,"false");
					}
					if (webServiceName.equalsIgnoreCase("MIBIND")) {
					NbaConfiguration.getInstance().setBusinessRulesAttributeValue(NbaConfigurationConstants.MIB_EVT_SUPPRESS_TRANSLATIONS_INDICATOR,"false");
					}

				}
			}
			result.setErrors(false);
		} catch (Exception e) {
			result = new AccelResult();
			result.setErrors(true);
			addExceptionMessage(result, e);
		}
		return result;
	}

	public boolean tableValueFound(String key, String[] table) {
		for (String str : table) {
			if (key.contains(str))
				return true;
		}
		return false;
	}

}