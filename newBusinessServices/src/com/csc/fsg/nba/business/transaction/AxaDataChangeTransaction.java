package com.csc.fsg.nba.business.transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.csc.fs.accel.valueobject.Comment;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeConstants;
import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeEntry;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;

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

/**
 * This class will be used as super class for all the WebService transaction classes. The common functionality for all the sub classes will be added
 * in this.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <td>AXAL3.7.07</td>
 * <td>AXA Life Phase 2</td>
 * <td>Data Change Architecture</td>
 * <td>ALS3374</td><td>AXA Life Phase 2</td><td>QC # 2032  - Wholesale case with "BGA agent" is going to error queue after Application submit</td>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */

public abstract class AxaDataChangeTransaction implements AxaDataChangeConstants {
	private boolean callNeeded = false;
	private long datachangeType; //ALS3374
	protected List registeredChanges = new ArrayList();

	protected abstract NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException;

	protected abstract long[] getDataChangeTypes();

	protected abstract boolean isTransactionAlive();

	protected void sendEvent(AxaDataChangeEntry dataChangeEntry) {
		int noOfDatachangeTypes = (getDataChangeTypes() != null) ? getDataChangeTypes().length : 0;
		long changeType = -1L;
		setChangeType(dataChangeEntry.getChangeType()); //ALS3374
		for (int i = 0; i < noOfDatachangeTypes; i++) {
			changeType = getDataChangeTypes()[i];
			if (changeType == dataChangeEntry.getChangeType()) {
				registerChange(dataChangeEntry);
				break;
			}
		}
	}
	/**
	 * @return Returns the changeType.
	 */
	//ALS3374 new method
	public long getChangeType() {
		return datachangeType;
	}
	/**
	 * @param changeType The changeType to set.
	 */
	//ALS3374 new method
	public void setChangeType(long changeType) {
		this.datachangeType = changeType;
	}
	protected void registerChange(AxaDataChangeEntry dataChangeEntry) {
		registeredChanges.add(dataChangeEntry);
		callNeeded = true;
	}

	/**
	 * @return Returns the callNeeded.
	 */
	public boolean isCallNeeded() {
		return callNeeded;
	}

	/**
	 * @param callNeeded
	 *            The callNeeded to set.
	 */
	public void setCallNeeded(boolean callNeeded) {
		this.callNeeded = callNeeded;
	}

	public void verifyTxLifeResponse(NbaTXLife response, NbaDst work, NbaUserVO userVo) throws NbaBaseException {
		UserAuthResponseAndTXLifeResponseAndTXLifeNotify txlifeResponseParent = response.getTXLife()
				.getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
		if (txlifeResponseParent.getTXLifeResponseCount() > 0 && txlifeResponseParent.getTXLifeResponseAt(0).hasTransResult()) {
			TXLifeResponse txLifeResponse = txlifeResponseParent.getTXLifeResponseAt(0);
			TransResult transResult = txLifeResponse.getTransResult();
			long resultCode = transResult.getResultCode();
			if ((NbaOliConstants.TC_RESCODE_FAILURE == resultCode)) {
				ArrayList results = transResult.getResultInfo();
				int resultInfoCount = results.size();
				ResultInfo resultInfo = null;
				for (int i = 0; i < resultInfoCount; i++) {
					resultInfo = (ResultInfo) results.get(i);
					if (errorStop(resultInfo.getResultInfoCode())) {
						String msgDesc = "";
						if(resultInfo.hasResultInfoDesc()){
							msgDesc =	resultInfo.getResultInfoDesc();
						}
						 
						throw new NbaBaseException("Error accessing WebService : FAULTSTRING : " + msgDesc  +" "+ this.getClass().getName() + " ", NbaExceptionType.FATAL);
					}
					if (resultInfo.hasResultInfoDesc()) {
						work = updateAsComment(userVo, work, resultInfo.getResultInfoDesc());

					}
				}
			}
		} else {
			throw new NbaBaseException(this.getClass().getName() + " WebService returned invalid response.", NbaExceptionType.FATAL);
		}
	}

	/**
	 * Adds a new comment to the AWD system.
	 * 
	 * @param aComment
	 *            the comment to be added to the AWD system.
	 * @param aProcess
	 *            the process that added the comment.
	 */
	public void addComment(NbaUserVO userVo, NbaDst work, String acommentText) {
		NbaProcessingErrorComment npec = new NbaProcessingErrorComment();
		npec.setActionAdd();
		npec.setOriginator(userVo.getUserID());
		npec.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
		npec.setProcess(userVo.getUserID());
		npec.setText(acommentText);
		work.addManualComment(npec.convertToManualComment());
	}

	/**
	 * 
	 * @param userVo
	 * @param work
	 * @param acommentText
	 * @throws NbaBaseException
	 */
	public NbaDst updateAsComment(NbaUserVO userVo, NbaDst work, String acommentText) throws NbaBaseException {
		addComment(userVo, work, acommentText);
		if (!NbaUtils.isAdditionalCommentsStoreWF()) {
			work = WorkflowServiceHelper.commitComments(userVo, work);
			resetCommentsFlag(work);
		}
		return work;
	}

	/**
	 * Resets the update flag after sucessful completion of commit
	 * 
	 * @param nbaDst
	 * @throws NbaNetServerDataNotFoundException
	 */
	protected void resetCommentsFlag(NbaDst nbaDst) throws NbaNetServerDataNotFoundException {
		if (nbaDst.isCase()) {
			updateDstComments(nbaDst.getCase().getComments());
			List trans = nbaDst.getTransactions();
			int size = trans.size();
			for (int i = 0; i < size; i++) {
				WorkItem wi = (WorkItem) trans.get(i);
				updateDstComments(wi.getComments());
			}
		} else {
			updateDstComments(nbaDst.getTransaction().getComments());
		}

	}

	/**
	 * Resets the update flag after sucessful completion of commit
	 * 
	 * @param nbaDst
	 * @throws NbaNetServerDataNotFoundException
	 */
	protected void updateDstComments(List comments) {
		int size = comments.size();
		Comment comment;
		for (int i = 0; i < size; i++) {
			comment = (Comment) comments.get(i);
			if (Comment.COMMENT_ACTION_ADD.equals(comment.getAction())) {
				comment.setAction("");
			}
		}
	}

	/**
	 * 
	 * @param resultCode
	 * @return
	 */
	protected boolean errorStop(long resultCode) {
		if (NbaOliConstants.TC_RESINFO_SYSTEMNOTAVAIL == resultCode || NbaOliConstants.TC_RESINFO_SECVIOLATION == resultCode
				|| NbaOliConstants.TC_RESINFO_UNABLETOPROCESS == resultCode) {
			return true;
		}
		return false;
	}
	
	//ALS4633 new method
	public boolean hasChangeSubType(long changeType) {
		Iterator registerChangesItr = registeredChanges.iterator();
		while (registerChangesItr.hasNext()) {
			AxaDataChangeEntry changeSubType = (AxaDataChangeEntry) registerChangesItr.next();
			if(changeType == changeSubType.getChangeType()){
				return true;
			}
		}
		return false;
	}	
		
}
