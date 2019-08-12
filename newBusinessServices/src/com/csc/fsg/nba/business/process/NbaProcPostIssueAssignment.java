package com.csc.fsg.nba.business.process;

/*
 * **************************************************************************<BR>
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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************************<BR>
 */
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.csc.fsg.nba.business.uwAssignment.AxaUnderwriterAssignmentEngine;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.AxaUWAssignmentEngineVO;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vpms.NbaVPMSHelper;

/**
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead> <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th> </thead>
 * 
 * <tr>
 * <td>APSL4685</td>
 * <td>Discretionary</td>Post Issue Assignment
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class NbaProcPostIssueAssignment extends NbaAutomatedProcess {
	protected List unlockList = new ArrayList();

	private NbaLob parentCaseLobs = null;

	protected NbaDst parentCase = null;

	/**
	 * NbaProcPostIssueAssignment constructor comment.
	 */
	public NbaProcPostIssueAssignment() {
		super();
	}

	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		if (!initialize(user, work)) {
			return getResult();
		}
		try {
			boolean isWIForPICMPrintNigo = NbaVPMSHelper.isWIForPICMPrintNigo(getWork());
			boolean suspendInd = false;
			if (!isWIForPICMPrintNigo) {
				retrieveParentCase();
				if (getParentCase() != null) {
					if (getParentCaseLobs().getPostIssueCMQueue() == null || getParentCaseLobs().getPostIssueCMQueue().trim().length() <= 0) {
						AxaUWAssignmentEngineVO picmAssignment = new AxaUWAssignmentEngineVO();
						picmAssignment.setTxLife(nbaTxLife);
						picmAssignment.setNbaDst(getWork());
						picmAssignment.setPostIssueCaseManagerRequired(true);
						picmAssignment.setWIForPICMPrintNigo(isWIForPICMPrintNigo);
						new AxaUnderwriterAssignmentEngine().execute(picmAssignment);
						if (getWork().getNbaLob().getPostIssueCMQueue() != null && getWork().getNbaLob().getPostIssueCMQueue().trim().length() != 0) {
							getParentCaseLobs().setPostIssueCMQueue(getWork().getNbaLob().getPostIssueCMQueue());
							getParentCase().setUpdate();
							updateWork(getUser(), getParentCase());
						}
					} else {
						getWork().getNbaLob().setPostIssueCMQueue(getParentCaseLobs().getPostIssueCMQueue());
					}
					// APSL4967 begin
					try {
						if (getWork().getNbaLob().getReceivedPDRInd() == true) {
							boolean isPDRCVExists = false;
							isPDRCVExists = NbaUtils.validatePDRCVOnCase(nbaTxLife);
							if (isPDRCVExists) {
								Calendar calendar = new GregorianCalendar();
								Date currentdate = new Date();
								calendar.setTime(NbaUtils.getDateFromStringInAWDFormat(getWork().getNbaLob().getCreateDate()));
								calendar.add(Calendar.DATE, 1);
								if (NbaUtils.compare(currentdate, calendar.getTime()) < 0) {
									NbaSuspendVO suspendVO = new NbaSuspendVO();
									suspendVO.setCaseID(getWork().getID());
									suspendVO.setActivationDate(calendar.getTime());
									suspendWork(suspendVO);
									updateForSuspend();
									suspendInd = true;
									setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", "SUSPENDED"));
								}

							}

						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					// APSL4967 End
					if (getWork().getNbaLob().getPostIssueCMQueue() != null && getWork().getNbaLob().getPostIssueCMQueue().trim().length() != 0) {
						setStatusProvider(getProcesStatusProvider(getWork().getNbaLob(), nbaTxLife));//modified for APSL4967
					}
				}
			} else {
				AxaUWAssignmentEngineVO picmAssignment = new AxaUWAssignmentEngineVO();
				picmAssignment.setTxLife(nbaTxLife);
				picmAssignment.setNbaDst(getWork());
				picmAssignment.setPostIssueCaseManagerRequired(true);
				picmAssignment.setWIForPICMPrintNigo(isWIForPICMPrintNigo);
				new AxaUnderwriterAssignmentEngine().execute(picmAssignment);
				if (getWork().getNbaLob().getPostIssueCMQueue() != null && getWork().getNbaLob().getPostIssueCMQueue().trim().length() != 0) {
					getStatusProvider().setPassStatus(getProcesStatusProvider(getWork().getNbaLob(), nbaTxLife).getPassStatus());
				}
			}
			// changeStatus(getStatusProvider().getPassStatus());//commented for APSL4967
			//Added for APSL4967
			if (!suspendInd) {
				changeStatus(getStatusProvider().getPassStatus(), getStatusProvider().getReason());
			}
			// Update the Work Item with it's new status and reason and update the work item in AWD
			doUpdateWorkItem();
			if (getResult() == null) {
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
			}
		} catch (NbaBaseException e) {
			if (e.isFatal() || e instanceof NbaAWDLockedException) {
				throw e;
			} else {
				e.printStackTrace();
				handleNbaBaseException(e);
			}
		} finally {
			unlockAWD();
		}
		return getResult();

	}

	protected void retrieveParentCase() throws NbaBaseException {
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false);
		retOpt.requestCaseAsParent();
		retOpt.setLockParentCase();
		retOpt.requestSources();
		this.parentCase = retrieveWorkItem(getUser(), retOpt); // Retrieve the Case, Sources and sibling Transactions
		setParentCaseLobs(this.parentCase.getNbaLob());
		getUnlockList().add(this.parentCase);
	}

	protected void unlockAWD() throws NbaBaseException {
		// unlock all work items
		for (int i = 0; i < unlockList.size(); i++) {
			unlockWork(getUser(), (NbaDst) unlockList.get(i));
		}
	}

	protected void handleNbaBaseException(NbaBaseException e) throws NbaBaseException {
		if (e.isFatal()) {
			throw e;
		}
		setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, getHostErrorStatus(), getHostErrorStatus()));
		addComment(e.getMessage());
	}

	/**
	 * @return the unlockList
	 */
	public List getUnlockList() {
		return unlockList;
	}

	/**
	 * @param unlockList
	 *            the unlockList to set
	 */
	public void setUnlockList(List unlockList) {
		this.unlockList = unlockList;
	}

	/**
	 * @return the parentCaseLobs
	 */
	public NbaLob getParentCaseLobs() {
		return parentCaseLobs;
	}

	/**
	 * @param parentCaseLobs
	 *            the parentCaseLobs to set
	 */
	public void setParentCaseLobs(NbaLob parentCaseLobs) {
		this.parentCaseLobs = parentCaseLobs;
	}

	/**
	 * @return the parentCase
	 */
	public NbaDst getParentCase() {
		return parentCase;
	}

	/**
	 * @param parentCase
	 *            the parentCase to set
	 */
	public void setParentCase(NbaDst parentCase) {
		this.parentCase = parentCase;
	}

}
