/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */
package com.csc.fsg.nba.webservice.client;

import java.util.HashMap;
import java.util.Map;

import com.csc.fsg.nba.bean.accessors.Nba302WebService;
import com.csc.fsg.nba.business.transaction.NbaAxaServiceResponse;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;

/**
 * This class is used invoking all the AXA Webservices.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>AXAL3.7.16</td><td>AXA Life Phase 1</td><td>TAI Interface</td></tr>
 * <tr><td>AXAL3.7.21</td><td>AXA Life Phase 1</td><td>Prior Insurance Interface</td></tr>
 * <tr><td>AXAL3.7.17</td><td>AXA Life Phase 1</td><td>CAPS Interface</td></tr> 
 * <tr><td>AXAL3.7.23</td><td>AXA Life Phase 1</td><td>Accounting Interface</td></tr>
 * <tr><td>AXAL3.7.28</td><td>AXA Life Phase 1</td><td>Checkwriting Interface</td></tr>
 * <tr><td>AXAL3.7.25</td><td>AXA Life Phase 2</td><td>Client Interface</td></tr>
 * <tr><td>AXAL3.7.22</td><td>AXA Life Phase 1</td><td>Compensation Interface</td></tr>
 * <tr><td>AXAL3.7.54</td><td>AXA Life Phase 1</td><td>AXAOnline / AXA Distributors Service</td></tr>
 * <tr><td>AXAL3.7.17</td><td>AXA Life Phase 1</td><td>Paid Change Interface</td></tr>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class AxaInvokeWebservice {
    protected static NbaLogger logger = null;

    /**
     * Invoke AXAOnline / AXA Distributors Service
     * @param Holding Request
     * @return
     * @throws NbaBaseException
     */
    //  New Method AXAL3.7.54
    public NbaTXLife invokeAXAOnlineDistrWS(NbaTXLife request, NbaUserVO userVO) throws NbaBaseException {
    	//Begin ALII53
    	String backEnd = !NbaUtils.isBlankOrNull(request.getBackendSystem()) ? request.getBackendSystem() : NbaConstants.SYST_CAPS;
        NbaTXLife nbaTXLifeResponse = invokeAxaWS(request, backEnd, "AxaOnlineDistributorService", "AxaOnlineDistributorService",
                NbaAxaServiceRequestor.OPERATION_SEND_POLICY_NOTIFICATIONS, userVO);
        //End ALII53
        if (getLogger().isDebugEnabled()) {
            getLogger().logInfo("InvokeAXAProducerSearchWS() : Agent Validation Response - " + nbaTXLifeResponse.toXmlString());
        }
        return nbaTXLifeResponse;
    }

    /**
     * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
     * @return com.csc.fsg.nba.foundation.NbaLogger
     */
    protected static NbaLogger getLogger() {
        if (logger == null) {
            try {
                logger = NbaLogFactory.getLogger(Nba302WebService.class.getName());
            } catch (Exception e) {
                NbaBootLogger.log("AXAInvokeWebservice could not get a logger from the factory.");
                e.printStackTrace(System.out);
            }
        }
        return logger;
    }

    /**
     * @param request
     * @param backEnd
     * @param category
     * @param functionId
     * @param operation
     * @return
     * @throws NbaBaseException
     */
    public NbaTXLife invokeAxaWS(NbaTXLife request, String backEnd, String category, String functionId, String operation, NbaUserVO userVO)
            throws NbaBaseException {
        NbaWebServiceAdapter service = NbaWebServiceAdapterFactory.createWebServiceAdapter(backEnd, category, functionId);
        Map params = new HashMap();
        params.put(NbaAxaServiceRequestor.PARAM_SERVICEOPERATION, operation);
        NbaUtils.prepareCoverageForSubStandardRating(request); //ALS2688
        params.put(NbaAxaServiceRequestor.PARAM_NBATXLIFE, request.toXmlString());
        if (userVO != null && userVO.getToken() != null) {
            params.put(NbaAxaServiceRequestor.PARAM_TOKEN, userVO.getToken());
        }
        params.put(NbaAxaServiceRequestor.PARAM_UDDIKEY, "ToBeDetermined");
        NbaTXLife nbaTXLifeResponse = null;
        Map results = service.invokeAxaWebService(params);
        nbaTXLifeResponse = (NbaTXLife) results.get(NbaAxaServiceResponse.NBATXLIFE_ELEMENT);
        return nbaTXLifeResponse;
    }
}