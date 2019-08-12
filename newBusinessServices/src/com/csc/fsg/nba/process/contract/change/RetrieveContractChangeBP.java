package com.csc.fsg.nba.process.contract.change;

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

import java.util.HashMap;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.AccelBP;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.access.contract.NbaServerUtility;
import com.csc.fsg.nba.business.transaction.NbaHoldingInqTransaction;
import com.csc.fsg.nba.datamanipulation.NbaRetrievePlanData;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaContractChangeDataTable;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.NbaContractChangeVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * Processes the contract change data retrieval.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA185</td><td>Version 7</td><td>Contract Change Rewrite</td></tr>
 * <tr><td>NBA187</td><td>Version 7</td><td>nbA Trial Application Project</td></tr>
 * <tr><td>AXAL3.7.04</td><td>Axa Life Phase 1</td><td>Paid Changes</td></tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class RetrieveContractChangeBP extends AccelBP {
    
    protected static NbaLogger logger = null;

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			if (input instanceof NbaContractChangeVO) {
				NbaContractChangeVO ccVO = (NbaContractChangeVO) input;
				NbaUserVO nbaUserVO = ccVO.getNbaUserVO();
				NbaLob lob = ccVO.getDefaultLobs();
				NbaDst nbaDst = ccVO.getOriginalWork();
				ccVO.setAdminContract(getAdminRecord(nbaUserVO, lob));
				ccVO.setCcSeqViews(getViewSequence(lob, nbaUserVO));
				if (nbaDst != null) {
					String dbData = getDataFromDatabase(nbaDst);
					if (dbData != null) {
						ccVO.setTempContract(new NbaTXLife(dbData));
					}
				}
				ccVO.setOpMode(lob.getOperatingMode());
				ccVO.setBackendSystem(lob.getBackendSystem());
				result.addResult(ccVO);
			} else {
				throw new IllegalArgumentException("Invalid arguments");
			}
		} catch (Exception e) {
			addExceptionMessage(result, e);
		}
		return result;
	}

	/**
	 * Retrieves the contract. The contract is obtained either from the administration system or the new business system depending on the operating
	 * mode from the data store VPMS model. For wrappered mode contracts, the contract is obtained from the new business system. For stand-alone
	 * contracts, the contract is obtained from the administration system.
	 * @param nbaUserVO The user value object
	 * @param lob the LOBs to retrieve data from admin system
	 * @return the admin record from backend
	 * @throws NbaBaseException
	 */
	protected NbaTXLife getAdminRecord(NbaUserVO nbaUserVO, NbaLob lob) throws NbaBaseException {
		NbaHoldingInqTransaction holdingTrx = new NbaHoldingInqTransaction(); //NBA187
		NbaTXRequestVO nbaTXRequest = holdingTrx.createRequestTransaction(lob, NbaConstants.READ, nbaUserVO.getUserID(), nbaUserVO); //NBA187
        NbaTXLife response = null;
        //NBA187 code deleted
        if (lob.getBackendSystem() == null) {
            NbaPlansData planData = NbaUtils.getNbaPlansData(lob);
            lob.setBackendSystem(planData.getSystemId());
        }
        if (NbaServerUtility.isDataStoreDB(lob, nbaUserVO)) {
           response = holdingTrx.processInforceTransaction(lob, nbaUserVO); //NBA187
        } else {
            response = NbaContractAccess.doContractInquiry(nbaTXRequest);
        }
        if (getLogger().isDebugEnabled()) {           
            getLogger().logDebug("XML203 responce from Webservice: " + response == null?null:response.toXmlString());                    
        }
        if (response != null && !response.isTransactionError()) {
            if (NbaConstants.SYST_VANTAGE.equalsIgnoreCase(lob.getBackendSystem())) {
                response.doXMLMarkUp();
            }
        }else {//If contract not found in CAPS, return null
        	return null;
        }//AXAL3.7.04
        return response;
    }	
	
	/**
     * Retrieves view sequence data from database for contract change.
     * @param lob The NbaLob
     * @param userVO The user value object
     * @return the array of sequence views
     * @throws NbaBaseException
     */
	protected NbaTableData[] getViewSequence(NbaLob lob, NbaUserVO userVO) throws NbaBaseException {
        NbaTableAccessor table = new NbaTableAccessor();
        Map defaultMap = getDefaultMap(lob, userVO);
        return table.getDisplayData(defaultMap, NbaTableConstants.NBA_CHANGETYPE_VIEWS);
    }
	
	/**
	 * Creates default Map to retrieve data from database
	 * @param lob The NbaLob
	 * @param userVO The user value object
	 * @return the default Map
	 * @throws NbaBaseException
	 */
	protected Map getDefaultMap(NbaLob lob, NbaUserVO userVO) throws NbaBaseException {
        NbaTableAccessor table = new NbaTableAccessor();
        HashMap map = table.createDefaultHashMap(NbaTableAccessConstants.WILDCARD);
        map.put(NbaTableAccessConstants.C_COMPANY_CODE, lob.getCompany());
        map.put(NbaTableAccessConstants.C_COVERAGE_KEY, lob.getPlan());
        map.put(NbaTableAccessConstants.C_SYSTEM_ID, lob.getBackendSystem());
        map.put(NbaTableAccessConstants.CHANGE_TYPE, lob.getContractChgType());
        //determine operating model if not known
        if (lob.getOperatingMode() == null || lob.getOperatingMode().trim().length() == 0) {
            if (NbaServerUtility.isDataStoreDB(lob, userVO)) {
                lob.setOperatingMode(NbaConstants.STANDALONE);
            } else {
                lob.setOperatingMode(NbaConstants.WRAPPERED);
            }
        }
        map.put(NbaTableAccessConstants.OPERATING_MODE, lob.getOperatingMode());
        return map;
    }
	
	/**
	 * Retrieve temporary contract data from database for a contract change.
	 * @param nbaDst The original workitem
	 * @return the temporary contract data from database
	 * @throws Exception
	 */
	protected String getDataFromDatabase(NbaDst nbaDst) throws NbaBaseException {
        String dataFromDatabase = null;
        if (nbaDst != null) {
            NbaContractChangeDataTable tableData = new NbaContractChangeDataTable();
            tableData.setWorkItemId(nbaDst.getID());
            tableData.setChangeType(NbaUtils.convertStringToLong(nbaDst.getNbaLob().getContractChgType()));
            tableData.retrieveData();
            dataFromDatabase = tableData.getTempContract();
        }
        return dataFromDatabase;
    }
	
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaRetrievePlanData.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log(" could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
}
