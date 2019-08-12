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
 *     Copyright (c) 2002-2009 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************************<BR>
 */
package com.csc.fsg.nba.business.process;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.StandardAttr;

/**
 * NbaProcPMFormal is the class that processes cases in N2PMFRML queue. Permanent formal cases not originating from informal are routed by N2FORMAL to
 * this queue. This process creates a new transaction and sends it to LIFENB business area. It also sends the case to END queue.
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.03</td><td>AXA Life Phase 1</td><td>Informals</td></tr>
 * <tr><td>ALPC166</td><td>AXA Life Phase 1</td><td>LOB Mapping to AWD P5</td></tr>
 * <tr><td>ALS2220</td><td>AXA Life Phase 1</td><td>QC# 1018  - Line 2 of the source tagline is missing when formal moves from nbA to AWD</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 */
public class NbaProcPMFormal extends NbaAutomatedProcess {

	protected NbaDst lifeNBTransaction = new NbaDst();

	/**
	 * This is the entry point of the automated process.
	 * @param user
	 * @param work
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException, NbaAWDLockedException {
		// Initialization
		if (!initialize(user, work)) {
			return getResult();
		}

		doProcess();
		changeStatus(getResult().getStatus());
		// Create LIFENB transaction and unlock it
		lifeNBTransaction = update(lifeNBTransaction);
		unlockWork(getUser(), lifeNBTransaction);
		// Update case
		doUpdateWorkItem();

		return getResult();
	}

	/**
	 * Determine new business area, work type and status for the case.
	 * @throws NbaBaseException
	 */
	protected void doProcess() throws NbaBaseException {
		setWork(retrieveWorkItem(getWork()));

		NbaVpmsAdaptor vpmsProxy = null;
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
			// Check if NBCWACHECK source is there with this case.
			List sources = getWork().getNbaSources();
			NbaSource source = null;
			if (sources != null) {
				for (int i = 0; i < sources.size(); i++) {
					source = (NbaSource) sources.get(i);
					if (source != null && source.getSourceType().equals(A_ST_CWA_CHECK)) {
						break;
					}
					source = null;
				}
			}
			// Call InformalToFormal VP/MS model. The entry point used here gives the values for business area and work type. Work type is determined
			// by the values of DIST, DSNG, CKAM, RPLC and APST LOBs.
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsConstants.INFORMALTOFORMAL);
			vpmsProxy.setVpmsEntryPoint(EP_ROUTE_PERM_FORMAL);
			// If NBCWACHECK source is present, deoink its CKAM LOB value.
			if (source != null && source.getSourceType().equals(A_ST_CWA_CHECK)) {
				Map deOink = new HashMap();
				deOink.put("A_CHECKAMOUNTLOB", Double.toString(source.getNbaLob().getCheckAmount()));
				vpmsProxy.setSkipAttributesMap(deOink);
			}

			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			List resultsData = data.getResultsData();
			// The entry point in VP/MS is configured to return business area, work type and status.
			if (resultsData != null && resultsData.size() >= 3) {
				createLifeNBTransaction((String) resultsData.get(0), (String) resultsData.get(1), (String) resultsData.get(2));
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			} else {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getVpmsErrorStatus()));
			}
		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException("APPMFRML" + NbaVpmsException.VPMS_EXCEPTION, re);
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}
	}

	/**
	 * Retrieve the case and its sources.
	 * @param nbaDst Case
	 * @return Case with sources
	 * @throws NbaBaseException
	 */
	protected NbaDst retrieveWorkItem(NbaDst nbaDst) throws NbaBaseException {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("APPMFRML Starting retrieveWorkItem for " + nbaDst.getID());
		}
		NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
		retrieveOptionsValueObject.setWorkItem(nbaDst.getID(), true); //ALS2741
		retrieveOptionsValueObject.requestTransactionAsChild(); // ALS2741
		retrieveOptionsValueObject.requestSources();
		retrieveOptionsValueObject.setLockTransaction();
		return retrieveWorkItem(getUser(), retrieveOptionsValueObject);
	}

	/**
	 * This method creates a new work item for LIFENB business area corresponding to this case in NBALIFE
	 * @param workType
	 * @param businessArea
	 * @param status
	 * @return
	 * @throws NbaNetServerDataNotFoundException
	 * @throws NbaBaseException
	 */
	protected void createLifeNBTransaction(String businessArea, String workType, String status) throws NbaBaseException {
		try {
			//ALPC166 Code Deleted
			WorkItem workItem = new WorkItem();
			// Set AWD fields
			workItem.setCreate("Y");
			workItem.setWorkType(workType);
			workItem.setStatus(status);
			workItem.setBusinessArea(businessArea);
			workItem.setRecordType("T");
			setLIFENBTransactionLOBs(workItem); //ALPC166

			lifeNBTransaction.setNbaUserVO(getWork().getNbaUserVO());
			lifeNBTransaction.addTransaction(workItem);
			NbaTransaction transaction = lifeNBTransaction.getNbaTransaction();
			// Copy sources of the case to the transaction
			List sources = getWork().getNbaSources();
			NbaSource source = null;
			if (sources != null) {
				//Begin APSL429 Remove ALS2220 changes
				for (int i = 0; i < sources.size(); i++) {
					source = (NbaSource) sources.get(i);
					if (source != null) {
						transaction.addNbaSource(source);
					}
				}
			}//End APSL429
			// Begin ALS2741
			List childTrans = getWork().getNbaTransactions();
			if (childTrans != null) {
				NbaTransaction childTran = null;
				sources = null;
				for (int i = 0; i < childTrans.size(); i++) {
					childTran = (NbaTransaction) childTrans.get(i);
					sources = childTran.getNbaSources();
					for (int j = 0; j < sources.size(); j++) {
						source = (NbaSource) sources.get(j);
						if (source != null) {
							transaction.addNbaSource(source);
						}
					}
				}
			}
			// End ALS2741
		} catch (Exception ex) {
			throw new NbaBaseException(ex);
		}
	}
	/**
	 * This methods sets the LOBs configured in VP/MS for LIFENB transaction.
	 * @param workItem
	 * @throws NbaBaseException
	 */
	//ALPC166 New Method
	protected void setLIFENBTransactionLOBs(WorkItem workItem) throws NbaBaseException {
		NbaVpmsAdaptor vpmsProxy = null; //ALS5009
		try{
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsConstants.INFORMALTOFORMAL); //ALS5009
			vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_LIFENB_TRANSACTION_LOBS);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
			String resultXml = (String) vpmsResultsData.getResultsData().get(0);
			NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(resultXml);
			List strAttrs = nbaVpmsModelResult.getVpmsModelResult().getStandardAttr();
	        Iterator itr = strAttrs.iterator();
	        List newLobs = workItem.getLobData();
			NbaOinkRequest aNbaOinkRequest = new NbaOinkRequest();
	        while (itr.hasNext()) {
				LobData newLob = new LobData();
				StandardAttr standardAttr = (StandardAttr) itr.next();
				newLob.setDataName(standardAttr.getAttrValue()); //VP/MS returns LIFENB LOB as attribute value
				aNbaOinkRequest.setVariable(standardAttr.getAttrName());
				String lobValue = oinkData.getStringValueFor(aNbaOinkRequest);
				if (! NbaUtils.isBlankOrNull(lobValue)) {
					newLob.setDataValue(lobValue);
					newLobs.add(newLob);
				}
			}
		} catch (Exception ex) {
			throw new NbaBaseException(ex);
		//begin ALS5009
		} finally {
			try {
			    if (vpmsProxy != null) {
					vpmsProxy.remove();					
				}
			} catch (RemoteException re) {
			    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); 
			}
		}
		//end ALS5009
	}

}
