package com.csc.fsg.nba.business.process;
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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.bean.accessors.NbaCompanionCaseFacadeBean;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaCompanionCaseVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsCompanionCaseData;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
/**
 * This class exracts the rules for a companion case. It has the ability to retrieve all linked cases
 * and the suspend information based on the rules applicable for the companion case.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA033</td><td>Version 3</td><td>Companion Case and HTML Indexing Views</td></tr>
 * <tr><td>SPR1410</td><td>Version 4</td><td>Companion cases are wrongly suspended at NBAPPHLD</td></tr>
 * <tr><td>NBA101</td><td>Version 4</td><td>Companion cases override</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>SPR2670</td><td>Version 6</td><td>Correction needed in Companion Case VP/MS model </td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7<td><tr> 
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPR3198</td><td>AXA Life Phase 1</td><td>Companion Case is not stopping for the other Companion Cases if one companion case is overridden</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaCompanionCaseRules {
	protected NbaUserVO user;
	protected NbaDst workItem;
	protected List companionCases;
	protected NbaSuspendVO suspendVO;
	protected NbaVpmsCompanionCaseData result;
	private static NbaLogger logger = null;
	/**
	 * Constructor for NbaCompanionCaseRules.
	 */
	public NbaCompanionCaseRules() {
		super();
	}
	/**
	 * Constructor for NbaCompanionCaseRules.
	 * @param user the user value object
	 * @param aNbaDst the work item
	 */
	public NbaCompanionCaseRules(NbaUserVO user, NbaDst aNbaDst) {
		super();
		setUser(user);
		setWorkItem(aNbaDst);
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaCompanionCaseRules.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaCompanionCaseRules could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	//NBA213 deleted code
	
	/**
	 * Returns the user.
	 * @return NbaUserVO
	 */
	public NbaUserVO getUser() {
		return user;
	}

	/**
	 * Sets the user.
	 * @param user <code>NbaUserVO</code> object to set
	 */
	public void setUser(NbaUserVO user) {
		this.user = user;
	}

	/**
	 * Returns the workItem.
	 * @return NbaDst
	 */
	public NbaDst getWorkItem() {
		return workItem;
	}

	/**
	 * Sets the workItem (Case only).
	 * @param workItem The workItem to set
	 */
	public void setWorkItem(NbaDst workItem) {
		this.workItem = workItem;
	}	
	/**
	 * Returns the workItem as a Case.
	 * @throws NbaBaseException
	 * @return NbaDst
	 */
	public NbaDst getWorkItemAsCase() throws NbaBaseException{			
		try {
			NbaAwdRetrieveOptionsVO options = new NbaAwdRetrieveOptionsVO();
			options.requestCaseAsParent();			
			options.setWorkItem(getWorkItem().getID(), false);
			return retrieveWorkItem(getUser(), options);  //NBA213
		} catch (Exception e) {
			getLogger().logError(e);
			throw new NbaBaseException(e);
		}		
	}	
	/**
	 * Returns the lazy initialized companionCases.
	 * @throws NbaBaseException
	 * @return List collection of <code>NbaCompanionCaseVO</code> objects
	 */
	public List getCompanionCases() throws NbaBaseException {
		if (companionCases == null) {
			try {				
				boolean isCase = (getWorkItem().getID().substring(getWorkItem().getID().length() - 3).startsWith("C") ? true : false);
				if (isCase) {
					NbaCompanionCaseFacadeBean bean = new NbaCompanionCaseFacadeBean();  //NBA213
					companionCases = bean.retrieveCompanionCases(getUser(), getWorkItem());  //NBA213
				} else {
					NbaDst parentCase = getWorkItemAsCase();
					NbaCompanionCaseFacadeBean bean = new NbaCompanionCaseFacadeBean();  //NBA213
					companionCases = bean.retrieveCompanionCases(getUser(), parentCase);  //NBA213

					if (workItem.getTransaction().getWorkType().equals(NbaConstants.A_WT_CONT_PRINT_EXTRACT)) {
						NbaCompanionCaseVO vo = null;
						NbaDst aCase = null;
						for (Iterator iter = companionCases.iterator(); iter.hasNext();) {
							vo = (NbaCompanionCaseVO) iter.next();
							if (vo.getWorkItemID().equals(parentCase.getID())) {
								vo.getNbaLob().setStatus(getWorkItem().getStatus());
							} else {
								vo.getNbaLob().setStatus(""); //reset the status
								NbaAwdRetrieveOptionsVO options = new NbaAwdRetrieveOptionsVO();								
								options.requestTransactionAsChild();
								options.setWorkItem(vo.getWorkItemID(), true);								
								aCase = retrieveWorkItem(getUser(), options);  //NBA213
								for (int i = 0; i < aCase.getTransactions().size(); i++) {
									//NBA208-32
									WorkItem tx = (WorkItem) aCase.getTransactions().get(i);
									if (tx.getWorkType().equals(NbaConstants.A_WT_CONT_PRINT_EXTRACT)) {
										vo.getNbaLob().setStatus(tx.getStatus());
										break;
									}
								}
							}

						}
					}
				}
			} catch (Exception e) {
				throw new NbaBaseException(e);
			}
		}
		return companionCases;
	}
	/**
	 * Returns true if suspended is needed, else returns false.
	 * @param vpmsModel the name of VP/MS model
	 * @return true if suspend needed
	 * @throws NbaBaseException
	 */
	//SPR2670 added parameter vpmsModel
	public boolean isSuspendNeeded(String vpmsModel) throws NbaBaseException {
	    NbaVpmsAdaptor vpms = null; //SPR3362
		try {
			vpms = new NbaVpmsAdaptor(null, vpmsModel); //SPR2670 SPR3362
			Map deOinkMap = new HashMap();
			NbaCompanionCaseVO vo = null;
			deOinkMap.put(NbaVpmsConstants.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser())); //SPR2639
			String workItemId = getWorkItem().isCase()? getWorkItem().getID() : getWorkItemAsCase().getID();
			
			for (Iterator iter = getCompanionCases().iterator(); iter.hasNext();) {
				vo = (NbaCompanionCaseVO) iter.next(); //find the case that has the CMCS LOB
				
				//begin  NBA101
				if (workItemId.equals(vo.getWorkItemID())){
					deOinkMap.put("A_OriginalWorkCCOverrideInd", vo.getOverride() != null ? vo.getOverride(): "N");
				} 
				//end NBA101
				
				//SPR3198 Code Deleted
			}
			vpms.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_SUSPEND_DATA);			
			//NBA101 code deleted

			for (Iterator iter = getCompanionCases().iterator(); iter.hasNext();) {
				vo = (NbaCompanionCaseVO) iter.next();
				
				deOinkMap.put("A_OtherWorkCCOverrideInd", vo.getOverride() != null ? vo.getOverride(): "N");//NBA101
				vpms.setSkipAttributesMap(deOinkMap);//NBA101
				
				vpms.setOinkSurrogate(new NbaOinkDataAccess(vo.getNbaLob()));
				result = new NbaVpmsCompanionCaseData(vpms.getResults());
				if (result.getApplicableSuspendTime() > 0) {
				//SPR3198 Code Deleted
					break;
				}
			}
		} catch (Exception e) {
			getLogger().logError(e);
			throw new NbaBaseException(e);
		//begin SPR3362
        } finally {
            if (vpms != null) {
                try {
                    vpms.remove();
                } catch (RemoteException e) {
                    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
                }
            }
        //end SPR3362
		}
		return (result != null && result.getApplicableSuspendTime() > 0);		
	}
	/**
	 * Returns a boolean to indicate whether a suspend for the work item is possible or not.
	 * @return boolean
	 * @throws NbaBaseException
	 */
	public boolean isSuspendDurationWithinLimits() throws NbaBaseException{
		boolean withinLimits = false;		
		if(result != null){
			NbaLob aNbaLob = getWorkItem().getNbaLob();	
			int unit = result.getUnit().equals("Days") ? Calendar.DAY_OF_WEEK : Calendar.MINUTE;
			if(getLogger().isDebugEnabled()) { //SPR3290
			    getLogger().logDebug("Starting Suspend calculation for " + aNbaLob.getPolicyNumber());
			} //SPR3290
			Date appHoldSusDate = aNbaLob.getAppHoldSuspDate(); //change this
			if (appHoldSusDate == null) {
				appHoldSusDate = new Date();
				aNbaLob.setAppHoldSuspDate(appHoldSusDate);
				getWorkItem().setUpdate();
			}
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(appHoldSusDate);
			calendar.add(unit, result.getMaxSuspendTime());
			Date maxSuspendDate = (calendar.getTime());
			if (!maxSuspendDate.before(new Date())) {				
				suspendVO = new NbaSuspendVO();
				suspendVO.setCaseID(getWorkItem().getID());
				calendar.setTime(new Date());
				calendar.add(unit, result.getApplicableSuspendTime());
				Date activationDate = calendar.getTime(); //save off activationDate for lookup test
				if (activationDate.after(maxSuspendDate)) {
					suspendVO.setActivationDate(maxSuspendDate);
				} else {
					suspendVO.setActivationDate(activationDate);
				}
				if(getLogger().isDebugEnabled()) { //SPR3290
				    getLogger().logDebug("Ending Suspend calculation for " + aNbaLob.getPolicyNumber());
				} //SPR3290
				withinLimits = true;						
			}
		}	
		return withinLimits;
	}	
	/**
	 * Returns the suspendVO.
	 * @return NbaSuspendVO
	 */
	public NbaSuspendVO getSuspendVO() {
		return suspendVO;
	}
	
	//NBA213 New Method
    protected NbaDst retrieveWorkItem(NbaUserVO user, NbaAwdRetrieveOptionsVO retOpt) throws NbaBaseException {
		retOpt.setNbaUserVO(user);
		AccelResult result = (AccelResult)ServiceHandler.invoke("NbaRetrieveWorkBP", ServiceContext.currentContext(), retOpt);
		NewBusinessAccelBP.processResult(result);
        return (NbaDst)result.getFirst();
    }

}
