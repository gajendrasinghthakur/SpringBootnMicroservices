package com.csc.fsg.nba.business.process;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.SuspendInfo;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

public class NbaProcGIHold extends NbaAutomatedProcess {

	private NbaDst lockedMatchingWork;	

	private final static String SUSPENDED = "SUSPENDED";
	
	private boolean isHoldForAlert = false; //NBLXA-2299
	
	boolean censusMatchedInd = false;	

	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {

		if (!initialize(user, work)) {
			return getResult();
		}
		doProcess(work.getNbaLob());
		return getResult();
	}

	protected void doProcess(NbaLob nbaLob) throws NbaBaseException, NbaVpmsException {
		//boolean censusMatchedInd = false;		NBLXA-2299 Declared as class variable
		// Begin NBLXA-2206
		if (NbaUtils.isBlankOrNull(nbaLob.getEmployerName())) {
			addComment("Employer Name is missing on Indexing");
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "FAILED", getHostFailStatus()));
			changeStatus(getHostFailStatus());
			doUpdateWorkItem();
			if (getLockedMatchingWork() != null) {
				NbaContractLock.removeLock(getUser());
				unlockWork(getLockedMatchingWork());
			}
		} else { // END NBLXA-2206
			if (NbaSystemDataDatabaseAccessor.isGIAppMatchedFromGICaseTemplate(nbaLob.getDOB(), nbaLob.getSsnTin(), nbaLob.getEmployerName())) {
				censusMatchedInd = true;
			} else {
				getLogger().logDebug("Census data not found for GI Application");
			}
			Policy policy = nbaTxLife.getPolicy();
			ApplicationInfo appInfo = policy.getApplicationInfo();
			ApplicationInfoExtension appInfoExtension = NbaUtils.getFirstApplicationInfoExtension(appInfo);
			//NBLXA-2299
			PolicyExtension policyExt = NbaUtils.getFirstPolicyExtension(policy);
			if(policyExt!=null){
				isHoldForAlert = NbaUtils.isHoldPolicyForHighAlert(policyExt.getGuarIssOfferNumber(),policy.getPolNumber());
			}
			
			if (appInfoExtension != null) {
				if (censusMatchedInd) {
					appInfoExtension.setGiAppMatchingInd(censusMatchedInd); 
					appInfoExtension.setActionUpdate();
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus()));
					//changeStatus(getResult().getStatus(), getRouteReason());	Code commented - NBLXA-2299				
				} 
				if(!isHoldForAlert){ //NBLXA-2299
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus()));
					//changeStatus(getResult().getStatus(), getRouteReason()); Code commented - NBLXA-2299	
					
				}
				if(!censusMatchedInd || isHoldForAlert){  //NBLXA-2299		
					suspendCase(policy);
				}								
				changeStatus(getResult().getStatus(), getRouteReason());
				doContractUpdate(nbaTxLife);
				handleHostResponse(nbaTxLife);
				doUpdateWorkItem();
				if (getLockedMatchingWork() != null) {
					NbaContractLock.removeLock(getUser());
					unlockWork(getLockedMatchingWork());
				}
			}			
		}// NBLXA-2206
	}

	/**
	 * When auto underwriting fails and Suspend processing is needed, this method adds suspends the case creates a new
	 * <code>NbaAutomatedProcessResult</code> to return to the polling program. A null value may be passed to indicate failure prior to the executing
	 * the VPMS model.
	 * 
	 * @param data
	 *            null or the results of the VPMS processing
	 * @return an NbaAutomatedProcessResult containing the reason for failure
	 * @throws NbaBaseException
	 */
	protected void suspendCase(Policy policy) throws NbaBaseException {
		int maximumSuspendDuration = 0, suspendActivationDuration = 0;
		String routeReason = "";
		ArrayList suspendInfoList = getSuspendedInfo();
		if (!suspendInfoList.isEmpty()) {
			maximumSuspendDuration = Integer.parseInt(suspendInfoList.get(0).toString());
			suspendActivationDuration = Integer.parseInt(suspendInfoList.get(1).toString());
			routeReason = (String) suspendInfoList.get(2);
		}
		SuspendInfo suspendInfo = null;
		Date initialSuspendDate = new Date();
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(policy);
		if (policyExtension == null) {
			OLifEExtension olife = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
			olife.setActionAdd();
			policy.addOLifEExtension(olife);
			policyExtension = olife.getPolicyExtension();
			policyExtension.setActionAdd();
			policy.setActionUpdate();

		}
		if (!policyExtension.isActionAdd()) {
			policyExtension.setActionUpdate();
		}
		if (policyExtension.hasSuspendInfo()) {
			suspendInfo = policyExtension.getSuspendInfo();
			suspendInfo.setActionUpdate();
		} else {
			suspendInfo = new SuspendInfo();
			suspendInfo.setActionAdd();
			suspendInfo.setSuspendDate(initialSuspendDate);
			suspendInfo.setUserCode(getUser().getUserID());
			policyExtension.setSuspendInfo(suspendInfo);
			policyExtension.setActionUpdate();
		}
		if (suspendInfo.hasSuspendDate() && suspendInfo.getUserCode().equals(getUser().getUserID())) {
			initialSuspendDate = policyExtension.getSuspendInfo().getSuspendDate();
		}
		if (!censusMatchedInd  || isHoldForAlert) { //NBLXA-2299
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(initialSuspendDate);
			Date currentDate = new Date();
			calendar.add(Calendar.DAY_OF_WEEK, maximumSuspendDuration);
			Date maxSuspendDurationDate = calendar.getTime();
			try {
				if (currentDate.after(maxSuspendDurationDate)) {
					addComment("Case routed to UW because the maximum suspend duration has been exceeded");
					policyExtension.getSuspendInfo().setActionDelete();
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus()));
					changeStatus(getResult().getStatus(), routeReason);
				} else {
					if(!censusMatchedInd){ //NBLXA-2299
						addComment("Census data not matched, case suspended for next day");
					}else{
						addComment("Lexis Nexis/BAE High/Failure alert present, case suspended for next day");						
					}
					NbaSuspendVO tempsuspendVO = new NbaSuspendVO();
					tempsuspendVO.setCaseID(getWork().getID());
					tempsuspendVO.setActivationDate(addDays(currentDate, suspendActivationDuration));
					setSuspendVO(tempsuspendVO);
					updateForSuspend();
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", SUSPENDED));
				}
			} catch (NbaBaseException e) {
				e.printStackTrace();
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Failed", getFailStatus()));
			}
		}
	}

	protected static Date addDays(Date date, int days) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}

	public NbaDst getLockedMatchingWork() {
		return lockedMatchingWork;
	}

	public void setLockedMatchingWork(NbaDst lockedMatchingWork) {
		this.lockedMatchingWork = lockedMatchingWork;
	}

	/**
	 * @purpose new method to get Suspended Info for GI Hold
	 * @throws NbaBaseException
	 */
	protected ArrayList getSuspendedInfo() throws NbaBaseException {
		NbaVpmsAdaptor proxy = null;
		ArrayList resultData = null;
		try {
			Map deOink = new HashMap();
			if (nbaTxLife != null) {
				NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess(nbaTxLife);
				deOink.put(A_DELIMITER, NbaVpmsConstants.VPMS_DELIMITER[0]);
				proxy = new NbaVpmsAdaptor(nbaOinkDataAccess, NbaVpmsConstants.AUTO_PROCESS_STATUS);
				proxy.setSkipAttributesMap(deOink);
				proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GICASE_SUSPEND_DAYS_INFO);
				NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
				if (vpmsResultsData != null) {
					resultData = vpmsResultsData.getResultsData();
					if (!resultData.isEmpty()) {
						return resultData;
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
				} catch (RemoteException re) {
					NbaLogFactory.getLogger("NbaProcPredictiveAnalysis").logError(re);
				}
			}
		}
		return null;
	}	
	

}
