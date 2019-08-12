package com.csc.fsg.nba.business.process;

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
 *     Copyright (c) 2002-2012 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */
import java.util.Iterator;
import java.util.List;

import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.database.NbaWebServiceProcessAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaWebServiceProcessorVO;
import com.csc.fsg.nba.vo.txlife.EPolicyData;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;

/**
 * NbaProcWebServiceResProcesser
 
 * <td>APSL5100</td>
 * <td>Print Preview</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 1201
 * @see NbaAutomatedProcess
 */

public class NbaProcWebServiceResProcesser extends NbaAutomatedProcess {

	protected static final String PRINT_PREVIEW = "PrintPreview";

	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {

		NbaWebServiceProcessorVO webServiceRes = null;
		String workItemId = null;
		String printGuid = null;
		boolean errorFlag = true;
		PolicyExtension txLifePolExtn = null;
		EPolicyData ePolicyData = null;
		NbaDst parentWork = null;

		setUser(user);
		List webServiceResults = NbaWebServiceProcessAccessor.selectContractsForResponseProcessing();
		int resultSize = webServiceResults.size();
		try {

			for (Iterator iter = webServiceResults.iterator(); iter.hasNext();) {
				webServiceRes = (NbaWebServiceProcessorVO) iter.next();
				if (webServiceRes.getWebServiceType() != null && webServiceRes.getWebServiceType().equalsIgnoreCase(PRINT_PREVIEW)) {
					try {
						NbaTXLife request = new NbaTXLife(webServiceRes.getWebServiceResponse());

						/* Retrieving print CRDA and print GUID from Response. */

						if (request.getPolicy().getOLifEExtensionCount() > 0) {
							for (int i = 0, j = request.getPolicy().getOLifEExtensionCount(); i < j; i++) {
								if (request.getPolicy().getOLifEExtensionAt(i).isPolicyExtension()) {
									PolicyExtension policyExtn = request.getPolicy().getOLifEExtensionAt(i).getPolicyExtension();
									if (policyExtn != null && policyExtn.getEPolicyData().size() > 0) {
										workItemId = policyExtn.getEPolicyDataAt(0).getPrintCRDA();
										printGuid = policyExtn.getEPolicyDataAt(0).getEPolicyPrintID();
										break;
									}
								}
							}
						}
						NbaAwdRetrieveOptionsVO retOptVO = new NbaAwdRetrieveOptionsVO();
						retOptVO.setWorkItem(workItemId, false);
						retOptVO.setNbaUserVO(user);
						retOptVO.setLockWorkItem();
						setWork(retrieveWorkItem(user, retOptVO));
						parentWork=retrieveParentWork(getWork(),true,false,false);
					} catch (NbaLockedException exception) {
						getLogger().logError("Locked workitem being bypassed.  ContractNumber:" + webServiceRes.getContractNumber());
						iter.remove();
						NbaWebServiceProcessAccessor.suspend(webServiceRes.getContractNumber(),webServiceRes.getItemId());
						continue;
					} catch (NbaBaseException nbe) {
						nbe.printStackTrace();// APSL4558
						getLogger().logError(
								"Exception " + nbe.getMessage() + " occurred while processing response for " + webServiceRes.getContractNumber());// APSL4558
						iter.remove();
						NbaWebServiceProcessAccessor.suspend(webServiceRes.getContractNumber(),webServiceRes.getItemId());
						continue;
					}
					// try..catch the holding inquiry .
					try {
						setNbaTxLife(doHoldingInquiry());
					} catch (Exception nbe) {
						getLogger().logError("Unable to retrieve contract for: " + webServiceRes.getContractNumber());
						iter.remove();
						NbaWebServiceProcessAccessor.suspend(webServiceRes.getContractNumber(),webServiceRes.getItemId());
						continue;
					}

					if (getNbaTxLife().getPolicy().getOLifEExtensionCount() > 0) {
						for (int i = 0, j = getNbaTxLife().getPolicy().getOLifEExtensionCount(); i < j; i++) {
							if (getNbaTxLife().getPolicy().getOLifEExtensionAt(i).isPolicyExtension()) {
								txLifePolExtn = getNbaTxLife().getPolicy().getOLifEExtensionAt(i).getPolicyExtension();
								break;
							}
						}
					}
					if (txLifePolExtn != null && txLifePolExtn.getEPolicyData().size() > 0) {
						for (int i = 0, j = txLifePolExtn.getEPolicyDataCount(); i < j; i++) {
							ePolicyData = txLifePolExtn.getEPolicyDataAt(i);
							if (ePolicyData != null && ePolicyData.getPrintCRDA().equals(workItemId) && ePolicyData.getActive()) {
								errorFlag = false;
								break;
							}
						}
						if (errorFlag) {
							getLogger().logError("Unable to retrieve EPolicyData for workItemId: " + webServiceRes.getContractNumber() + workItemId );
							iter.remove();
							NbaWebServiceProcessAccessor.suspend(webServiceRes.getContractNumber(),webServiceRes.getItemId());
							continue;
						}
						ePolicyData.setEPolicyPrintID(printGuid);
						ePolicyData.setActionUpdate();
						txLifePolExtn.setUpdatedPreviewRecievedInd(true);
						txLifePolExtn.setActionUpdate();
						setContractAccess(UPDATE);
						doContractUpdate();
						NbaContractLock.removeLock(getWork(), user);
						NbaWebServiceProcessAccessor.resetProcessingIndicators(webServiceRes.getContractNumber(),webServiceRes.getItemId());
						NbaSuspendVO suspendVo = new NbaSuspendVO();
						suspendVo.setNbaUserVO(user);
						suspendVo.setTransactionID(workItemId);
						unsuspendWork(user, suspendVo);
					}
					unlockCase(parentWork);
				}
			}
		} catch (NbaBaseException nbe) {
			getLogger().logException(nbe);
			if(null !=webServiceRes){
			getLogger().logError("Unable to process contract: " + webServiceRes.getContractNumber());
			NbaWebServiceProcessAccessor.suspend(webServiceRes.getContractNumber(),webServiceRes.getItemId());
			}
			
		} catch (Exception nbe) {
			getLogger().logException(nbe);
			if(null !=webServiceRes){
			getLogger().logError("Unable to process contract: " + webServiceRes.getContractNumber());
			NbaWebServiceProcessAccessor.suspend(webServiceRes.getContractNumber(),webServiceRes.getItemId());
			}
			
		} finally {
			if (null != getWork() && getWork().isLocked(user.getUserID())) {
				try {
					unlockCase(parentWork);
				} catch (NbaBaseException nbe) {
					getLogger().logException(nbe);
					if(null !=webServiceRes){
					getLogger().logError("Error unlocking work for contract: " + webServiceRes.getContractNumber());
					NbaWebServiceProcessAccessor.suspend(webServiceRes.getContractNumber(),webServiceRes.getItemId());
					}
				}
			}
		}

		if (resultSize > 0) {
			return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", "");
		}
		return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", "");
	}

	protected void unlockCase(NbaDst parentWork) throws NbaBaseException {
		unlockWork(getWork());
		if (null != parentWork &&  parentWork.isLocked(user.getUserID())) {
			unlockWork(parentWork);
		}
		NbaContractLock.removeLock(getWork(), getUser());
	}

}
