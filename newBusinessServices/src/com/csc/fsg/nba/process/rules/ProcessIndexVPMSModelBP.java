package com.csc.fsg.nba.process.rules;

/* 
 * *******************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaIndexingVO;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.StandardAttr;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * Busniess Process class to Call Indexing VP/MS model at given entry point  
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA173</td><td>Version 7</td><td>nbA Indexing UI Rewrite Project</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */

public class ProcessIndexVPMSModelBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result;
		try {
			result = validate((NbaIndexingVO) input);
		} catch (Exception e) {
			result = new AccelResult();
			addExceptionMessage(result, e);
		}
		return result;
	}

	/**
	 * Call Indexing VP/MS model to perform validation and return result value object 
	 * @param indexVO NbaIndexingVO object
	 * @return AccelResult object
	 * @throws Exception
	 */
	protected AccelResult validate(NbaIndexingVO indexVO) throws Exception {
	    AccelResult result = new AccelResult();
        result.addResult(processRules(indexVO));
        return AccelResult.buildResult(result);
	}

	/**
	 * Call Indexing VP/MS model at given entry point 
	 * @param lobData Lob data which from which OINK auto populated the input vpms values 
	 * @param deOinkMap map containing overrided or non lob data
	 * @param entryPoint Entry point which needs to be called
	 * @return String result string
	 * @throws NbaBaseException
	 */
	protected String processRules(NbaIndexingVO indexVO) throws NbaBaseException {
	    NbaVpmsAdaptor rulesProxy = null;
	    String returnStr = "";
		try {
			NbaOinkDataAccess data = new NbaOinkDataAccess(indexVO.getNbaLob());
			rulesProxy = new NbaVpmsAdaptor(data, indexVO.getModelName());
			rulesProxy.setSkipAttributesMap(indexVO.getDeOinkMap());
			rulesProxy.setVpmsEntryPoint(indexVO.getVpmsEntryPoint());
			
			// get the string out of XML returned by VP / MS Model and parse it to create the object structure
			VpmsComputeResult rulesProxyResult = rulesProxy.getResults();
			if (!rulesProxyResult.isError()) {
			    NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(rulesProxyResult);
			    List rulesList =  vpmsResultsData.getResultsData();
			    if (!rulesList.isEmpty()) {
					String xmlString = (String) rulesList.get(0);
					NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
					VpmsModelResult vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
					List strAttrs = vpmsModelResult.getStandardAttr();
					
					//Generate delimited string if there are more than one parameters returned
					returnStr = generateDelimitedString(strAttrs);
			    }
			}
			return returnStr;
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} finally {
			if (rulesProxy != null) {
				try {
					rulesProxy.remove();
				} catch (RemoteException re) {
				    LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED); //SPR3362
				}
			}
		}
	}

    /**
     * Generate delimited string if there are more than one parameters returned in the List strAttrs
     * @param returnStr the 
     * @param strAttrs attribute list
     */
    protected String generateDelimitedString(List strAttrs) {
        StringBuffer returnStr = new StringBuffer();
        StandardAttr attr = null;
        int i = 0;
        for (Iterator itr = strAttrs.iterator(); itr.hasNext(); i++) {
            attr = (StandardAttr) itr.next();
            if (i > 0) {
                returnStr.append(NbaVpmsAdaptor.VPMS_DELIMITER[0]);
            }
            returnStr.append(attr.getAttrValue());
        }
        return returnStr.toString();
    }

}
