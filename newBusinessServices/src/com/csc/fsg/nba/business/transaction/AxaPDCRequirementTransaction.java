/*
 * ************************************************************** <BR>
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
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */

package com.csc.fsg.nba.business.transaction;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkFormatter;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;


/**
 * 
 * This class encapsulates checks whenever Payment Amount is changed, Order message for PDC requirement is updated.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>ALS4633</td><td>AXA Life Phase 1</td><td>Compensation Interface</td>
 * </tr>
 * <tr><td>APSL4112</td><td>AXA Life</td><td>Reissue processed reducing the face amount and premium due requirement was not updated to reflect new premium calc.</td></tr>
 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaPDCRequirementTransaction extends AxaDataChangeTransaction implements NbaOliConstants {
	protected NbaLogger logger = null;

	protected static long[] changeTypes = { 
	    DC_PAYMENT_AMOUNT,DC_CWA_RECEIVED,DC_CWA_Shortage_CV_EXIST};//NBLXA-1896


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#callInterface(com.csc.fsg.nba.vo.NbaTXLife)
	 */
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		//NBLXA-2205 : removed if condition introduced under NBLXA-1896 as below function needs to be called everytime irrespective of data change
	    setPremiumDueRequirementDetails(nbaTxLife,nbaDst);

		//NBLXA-1896 Begins
		if(hasChangeSubType(DC_CWA_RECEIVED)|| hasChangeSubType(DC_CWA_Shortage_CV_EXIST)){
			updatePrintWI(nbaTxLife,user,nbaDst);
		}
		//NBLXA-1896 Ends
		return nbaDst;
	}

	   /**
     * Get the Requirement Details to be set on outstanding Premium Due Carrier Requirement  
     */
    protected void setPremiumDueRequirementDetails(NbaTXLife nbaTxLife, NbaDst nbaDst) throws NbaBaseException{
        NbaVpmsAdaptor proxy = null;
        try {
            String reqDetails = null;
            NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess(nbaDst.getNbaLob());
            nbaOinkDataAccess.setContractSource(nbaTxLife);
            nbaOinkDataAccess.getFormatter().setDateSeparator(NbaOinkFormatter.DATE_SEPARATOR_DASH);
            proxy = new NbaVpmsAdaptor(nbaOinkDataAccess, NbaVpmsConstants.CONTRACTVALIDATIONCOMPANYCONSTANTS);
            proxy.setVpmsEntryPoint("P_GetPremiumDueRequirementDetails");
            NbaVpmsResultsData nbaVpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
            if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
                if (nbaVpmsResultsData.getResultsData().size() == 1) {                  
                    reqDetails = (String) nbaVpmsResultsData.getResultsData().get(0);                   
                }
            }
            List reqInfoList = nbaTxLife.getRequirementInfoList(nbaTxLife.getPrimaryParty(),OLI_REQCODE_PREMDUE);
            if (reqInfoList != null) {
                int count = reqInfoList.size();
                RequirementInfo reqInfo = null;
                for (int i = 0; i < count; i++) {
                    reqInfo = (RequirementInfo) reqInfoList.get(i);
                    if (reqInfo != null && !NbaUtils.isRequirementFulfilled(String.valueOf(reqInfo.getReqStatus()))) {
                        reqInfo.setRequirementDetails(reqDetails);
                        reqInfo.setActionUpdate();
                    }
                }
            }            
        } catch (RemoteException t) {
            throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
        } catch (NbaBaseException e) {
            throw new NbaBaseException("NbaBaseException Exception occured while processing VP/MS request", e);
        } finally {
            if (proxy != null) {
                try {
                    proxy.remove();
                } catch (RemoteException t) {
                    getLogger().logError(t);
                }
            }
        }
    }
    
	// NBLXA-1896 New Method
	protected void updatePrintWI(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		String contractKey = null;
		if (nbaTxLife != null && nbaTxLife.getPolicy() != null && nbaTxLife.getPolicy().getPolNumber() != null) {
			contractKey = nbaTxLife.getPolicy().getPolNumber();
		}
		if (!NbaUtils.isBlankOrNull(contractKey)) {
			NbaSearchVO searchVO = searchPrint(user, contractKey);
			if (!NbaUtils.isBlankOrNull(searchVO)) {
				List printWIsList = searchVO.getSearchResults();
				if (!NbaUtils.isBlankOrNull(printWIsList)) {
					List printWIs = new ArrayList();

					for (int i = 0; i < printWIsList.size(); i++) {
						NbaSearchResultVO printVO = (NbaSearchResultVO) printWIsList.get(i);

						NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
						retOpt.setWorkItem(printVO.getWorkItemID(), false);
						retOpt.setLockWorkItem();
						NbaDst aWorkItem = WorkflowServiceHelper.retrieveWorkItem(user, retOpt);
						printWIs.add(aWorkItem);
					}
					Iterator it = printWIs.iterator();
					if (hasChangeSubType(DC_CWA_RECEIVED)) {
						while (it.hasNext()) {
							NbaDst printItem = (NbaDst) it.next();
							if (!NbaUtils.isBlankOrNull(printItem) && printItem.getNbaLob() != null) {
								printItem.getNbaLob().setCheckAmount(NbaConstants.TRUE);
								printItem.setUpdate();
								WorkflowServiceHelper.updateWork(user, printItem);
							}
							WorkflowServiceHelper.unlockWork(user, printItem);
						}
					} else if (hasChangeSubType(DC_CWA_Shortage_CV_EXIST)) {
						while (it.hasNext()) {
							NbaDst printItem = (NbaDst) it.next();
							if (!NbaUtils.isBlankOrNull(printItem) && printItem.getNbaLob() != null) {
								printItem.getNbaLob().setCheckAmount(NbaConstants.FALSE);
								printItem.setUpdate();
								WorkflowServiceHelper.updateWork(user, printItem);
							}
							WorkflowServiceHelper.unlockWork(user, printItem);
						}
					}
				}
			}
		}
	}

	//NBLXA-1896 New Method
    public NbaSearchVO searchPrint(NbaUserVO user, String contractKey) throws NbaBaseException {
		NbaSearchVO searchVO = new NbaSearchVO();
		searchVO.setWorkType(NbaConstants.A_WT_CONT_PRINT_EXTRACT);
		searchVO.setContractNumber(contractKey);
		searchVO.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchVO = WorkflowServiceHelper.lookupWork(user, searchVO);
		return searchVO;
	}
    
    
	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#getDataChangeTypes()
	 */
	protected long[] getDataChangeTypes() {
		return changeTypes;
	}

	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaDataChangeTransaction#isTransactionAlive()
	 */
	protected boolean isTransactionAlive() {
		return true;
	}
	
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * 
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass());
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	
}
