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
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.StandardAttr;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * Busniess Process class to Call Informal Indexing default VP/MS model 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA187</td><td>Version 7</td><td>nbA Trial Application Project</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class ProcessInformalIndexingDefaultsBP extends NewBusinessAccelBP {

    /*
     * (non-Javadoc)
     * 
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result;
        try {
            result = setDefaults((NbaLob) input);
        } catch (Exception e) {
            result = new AccelResult();
            addExceptionMessage(result, e);
        }
        return result;
    }

    /**
     * Set the LOB values to the default values if no data was entered for them
     * 
     * @param indexVO NbaIndexingVO object
     * @return AccelResult object
     * @throws Exception
     */
    protected AccelResult setDefaults(NbaLob sourceLob) throws Exception {
        VpmsModelResult vpmsModelResult = processRules(sourceLob);
        
        if (vpmsModelResult != null) {
            List strAttrs = vpmsModelResult.getStandardAttr();
            Iterator itr = strAttrs.iterator();
            NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess();
            nbaOinkDataAccess.setLobDest(sourceLob);
            NbaOinkRequest aNbaOinkRequest;
            StandardAttr standardAttr;
            //update the lobs with default values
            while (itr.hasNext()) {
                aNbaOinkRequest = new NbaOinkRequest();
                standardAttr = (StandardAttr) itr.next();
                aNbaOinkRequest.setVariable(standardAttr.getAttrName().substring(2));
                aNbaOinkRequest.setValue(standardAttr.getAttrValue());
                nbaOinkDataAccess.updateValue(aNbaOinkRequest);
            }
        }
        AccelResult result = new AccelResult();
        result.addResult(sourceLob);
        return AccelResult.buildResult(result);
    }

    /**
     * Call Indexing VP/MS model for trial defaults
     * 
     * @param lobData Lob data which from which OINK auto populated the input vpms values
     * @return VpmsModelResult
     * @throws NbaBaseException
     */
    protected VpmsModelResult processRules(NbaLob sourceLob) throws NbaBaseException {
        NbaVpmsAdaptor proxy = null;
        try {
            NbaOinkDataAccess data = new NbaOinkDataAccess(sourceLob);
            proxy = new NbaVpmsAdaptor(data, NbaVpmsConstants.INDEX);
            proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_TRIAL_APP_DEFAULTS);

            // get the string out of XML returned by VP / MS Model and parse it to create the object structure
            VpmsComputeResult proxyResult = proxy.getResults();
            if (!proxyResult.isError()) {
                NbaVpmsResultsData resultsData = new NbaVpmsResultsData(proxyResult);
                List rulesList = resultsData.getResultsData();
                if (!rulesList.isEmpty()) {
                    String xmlString = (String) rulesList.get(0);
                    NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
                    return nbaVpmsModelResult.getVpmsModelResult();
                }
            }
            return null;
        } catch (RemoteException t) {
            throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
        } finally {
            if (proxy != null) {
                try {
                    proxy.remove();
                } catch (RemoteException re) {
                    LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
                }
            }
        }
    }
}
