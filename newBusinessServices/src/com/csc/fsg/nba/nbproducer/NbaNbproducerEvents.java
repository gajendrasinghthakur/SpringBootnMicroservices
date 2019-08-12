package com.csc.fsg.nba.nbproducer;

/**
 * ************************************************************** <BR>
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
 * ************************************************************** <BR>
 * 
 */

import java.util.List;

import com.csc.fsg.nba.bean.accessors.NbaPendingInfoSession;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
// ACN012 code deleted

/** 
 * 
 * This class is used to update nbProducer PendingInfo database in case of AWD status change
 * nbA PendingInfo database change and Host event.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBP001</td><td>Version 3</td><td>nbProducer Initial Development</td></tr>   
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaNbproducerEvents {
	
	protected NbaLogger logger;

/**
 * This method returns an instance of <code>NbaLogger</code>.
 * @return com.csc.fsg.nba.foundation.NbaLogger
 */
protected NbaLogger getLogger() {
    if (logger == null) {
        try {
            logger = NbaLogFactory.getLogger(this.getClass().getName());
        } catch (Exception e) {
            NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
            e.printStackTrace(System.out);
        }
    }
    return logger;
}

/**
 * This method verifies if the Host response is valid.
 * @return boolean
 */
protected boolean isValidHostResponse(NbaTXLife nbaTxlifeResponse) {
    boolean returnValue = true;
    try {
        List responses = nbaTxlifeResponse.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponse();
        TXLifeResponse theResponse = (TXLifeResponse) responses.get(0);

        if (theResponse.getTransResult().getResultCode() > 1) {
            getLogger().logError("Invalid host response. Correspondence event can not be evaluated!");
            returnValue = false;
        }
    } catch (Exception e) {
        getLogger().logError(e);
        returnValue = false;
    }
    return returnValue;
}
/**
 * This method identifies if the Host transaction is valid. If the Transaction is a Holding
 * Inquiry it returns false;
 * @param nbaTxlifeRequest com.csc.fsg.nba.vo.NbaTXLife
 * @param workItemId java.lang.String
 * @return boolean
 */
protected boolean isValidTransactionRequest(NbaTXLife nbaTxlifeRequest) {
    boolean returnValue = true;
    try {
       	 if (nbaTxlifeRequest.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getTransType() == NbaOliConstants.TC_TYPE_HOLDINGINQ) {
            returnValue = false;
        }
    } catch (Exception e) {
        getLogger().logError("Invalid Transaction Error");
        returnValue = false;
    }
    return returnValue;
}



/**
 * This method determines if a status change has occurred for a case.
 * If a status change has occurred, then nbP PendingInfo database is updated.
 * @param nbaDst An <code>NbaDst</code> instance.
 */
public void updateNbpendingInfoDatabaseForAwdStatusChangeEvent(NbaDst nbaDst) throws NbaBaseException{
    NbaLob nbaLob = nbaDst.getNbaLob();
    if (NbaConfiguration.getInstance().getNbpDatabaseUpdate().equalsIgnoreCase("ON") && nbaLob.getPolicyNumber() != null) {
		NbaPendingInfoSession pif = new NbaPendingInfoSession();  //NBA213
		pif.updateWorkflowStatus(nbaLob.getPolicyNumber(),nbaLob.getBackendSystem(),nbaLob.getCompany(), nbaDst.getQueue());
		//NBA213 deleted code
	}

}
/**
 * This method updates nbP PendingInfo database for a host event
 * @param txlifeRequest A Host Transaction request
 * @param txlifeResponse A Host Transaction response 
 * @param nbaDst An <code>NbaDst</code> instance.
 */
public void updateNbpendingInfoDatabaseForHostEvent(NbaTXLife nbaTxlifeRequest, NbaTXLife nbaTxlifeResponse, NbaDst nbaDst) throws NbaBaseException {
    if (isValidTransactionRequest(nbaTxlifeRequest) && isValidHostResponse(nbaTxlifeResponse)) {
		//NBA213 deleted code
		if (NbaConfiguration.getInstance().getNbpDatabaseUpdate().equalsIgnoreCase("ON")  && nbaDst.getNbaLob().getPolicyNumber() != null) {
			NbaPendingInfoSession pif = new NbaPendingInfoSession();  //NBA213
			pif.update(nbaTxlifeResponse, nbaDst.getQueue());
		}
		//NBA213 deleted code
	}
}
/**
 * This method updates nbP PendingInfo database for a host event
 * @param txlifeRequest A Host Transaction request
 * @param txlifeResponse A Host Transaction response 
 * @param nbaDst An <code>NbaDst</code> instance.
 */
public void updateNbpendingInfoDatabaseForHostEvent(String txlifeRequest, String txlifeResponse, NbaDst nbaDst) {
    try {
        updateNbpendingInfoDatabaseForHostEvent(new NbaTXLife(txlifeRequest), new NbaTXLife(txlifeResponse), nbaDst);
    } catch (Exception e) {
        getLogger().logError(e);
    }
}

/**
 * This method updates nbP PendingInfo database for a nbA PendingInfo database change event
 * @param nbaTxlife a NbaTxlife containing the updated contract in its TXLifeResponse object
 * @param nbaDst An <code>NbaDst</code> instance.
 */
public void updateNbpendingInfoDatabaseForDatabaseEvent(NbaTXLife nbaTxlife, NbaDst nbaDst) throws NbaBaseException {
	if (NbaConfiguration.getInstance().getNbpDatabaseUpdate().equalsIgnoreCase("ON") && nbaDst.getNbaLob().getPolicyNumber() != null) {
		//NBA213 deleted code
		NbaPendingInfoSession pif = new NbaPendingInfoSession();  //NBA213
		pif.update(nbaTxlife, nbaDst.getQueue());
		//NBA213 deleted code
	}
}


}
