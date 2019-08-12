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

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import com.csc.fs.ServiceHandler;
import com.csc.fs.UserSessionKey;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.AxaReassignDataVO;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaProcessingErrorComment;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.NbaLob;

/**
 * 
 * This class encapsulates checks whenever following changes are made on the policy.
 * - Insured's Name. - Insured's SSN
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>NBLXA-2162</td><td>AXA Life Phase 2</td>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaUpdateInsuredDataTransaction extends AxaDataChangeTransaction implements NbaConstants{
	protected NbaLogger logger = null;
	
	protected static long[] changeTypes = {
		DC_INSURED_FIRSTNAME,
		DC_INSURED_MIDDLENAME,
		DC_INSURED_LASTNAME,
		DC_JNT_INSURED_FIRSTNAME,
		DC_JNT_INSURED_MIDDLENAME,
		DC_JNT_INSURED_LASTNAME,
		DC_INSURED_SSN,
		DC_JNT_INSURED_SSN,
		DC_INSURED_DOB,
		DC_JNT_INSURED_DOB
	};

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
	

	/* (non-Javadoc)
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#callInterface(com.csc.fsg.nba.vo.NbaTXLife)
	 */
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		String msg = "";
		String crda = ""; //NBLXA-2498
		if (isCallNeeded()) {
			if (hasChangeSubType(DC_INSURED_FIRSTNAME)||hasChangeSubType(DC_INSURED_MIDDLENAME)
					||hasChangeSubType(DC_INSURED_LASTNAME)||hasChangeSubType(DC_INSURED_SSN)
					||hasChangeSubType(DC_INSURED_DOB)){
				//Begin NBLXA-2498
				if (nbaDst.isCase()) {
					crda = nbaDst.getID();
					getLogger().logDebug("crda of the case == " + crda);
				} else { //boolean isTransaction = work.isTransaction();
					NbaDst dst = getParentCase(user, nbaDst);
					if (!NbaUtils.isBlankOrNull(dst)) {
						//crda = nbaDst.getWorkItem().getParentWorkItemID();
						crda = dst.getID();
						getLogger().logDebug("crda of the case for the transaction == " + crda + "  trnsaction == " + nbaDst.getID());
					} else {
						crda = nbaDst.getID();
						getLogger().logDebug("crda of the transaction but case== " + crda);
					}
				}
				//End NBLXA-2498
				AxaReassignDataVO reassignVo = new AxaReassignDataVO();
				Policy policy = nbaTxLife.getPolicy();
				NbaParty primInsParty = nbaTxLife.getPrimaryParty();
				Person primInsPerson = primInsParty.getPerson();
                // Begin NBLXA-2529
				reassignVo.setCreateDateTime(crda);
				reassignVo.setPolicynumber(policy.getPolNumber());
				// End NBLXA-2529
				reassignVo.setCompanyKey(policy.getCompanyKey());
				reassignVo.setBackendKey(policy.getBackendKey());
				reassignVo.setUserCode(user.getUserID());
				reassignVo.setStatus("Active");
				if(hasChangeSubType(DC_INSURED_FIRSTNAME)||hasChangeSubType(DC_INSURED_MIDDLENAME)
						||hasChangeSubType(DC_INSURED_LASTNAME)){
					reassignVo.setChangedType(NbaConstants.JOB_PRIMARY_INS_NAME_UPDATE);
					reassignVo.setChangedValue(primInsPerson.getFirstName()+","+primInsPerson.getMiddleName()+","+primInsPerson.getLastName());
					msg = "Name change initiated for Primary Insured.";
					NbaSystemDataDatabaseAccessor.insertReassingmentProcessing(reassignVo);
					addComments(user, nbaDst, msg);
				}
				if(hasChangeSubType(DC_INSURED_SSN)&& !NbaUtils.isBlankOrNull(primInsParty.getSSN()) ){
					reassignVo.setChangedType(NbaConstants.JOB_PRIMARY_INS_SSN_UPDATE);
					reassignVo.setChangedValue(primInsParty.getSSN());
					msg = "SSN change initiated for Primary Insured.";
					NbaSystemDataDatabaseAccessor.insertReassingmentProcessing(reassignVo);
					addComments(user, nbaDst, msg);
				}
				if(hasChangeSubType(DC_INSURED_DOB)){
					reassignVo.setChangedType(NbaConstants.JOB_PRIMARY_INS_DOB_UPDATE);
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
					String birthDate = simpleDateFormat.format(primInsPerson.getBirthDate());
					reassignVo.setChangedValue(birthDate);					
					msg = "DOB change initiated for Primary Insured.";
					NbaSystemDataDatabaseAccessor.insertReassingmentProcessing(reassignVo);
					addComments(user, nbaDst, msg);
				}				
			}
			
			if (hasChangeSubType(DC_JNT_INSURED_FIRSTNAME)||hasChangeSubType(DC_JNT_INSURED_MIDDLENAME)
					||hasChangeSubType(DC_JNT_INSURED_LASTNAME)||hasChangeSubType(DC_JNT_INSURED_SSN)
					||hasChangeSubType(DC_JNT_INSURED_DOB)){
				//Begin NBLXA-2498
				if (nbaDst.isCase()) {
					crda = nbaDst.getID();
					getLogger().logDebug("crda of the case == " + crda);
				} else { //boolean isTransaction = work.isTransaction();
					NbaDst dst = getParentCase(user, nbaDst);
					if (!NbaUtils.isBlankOrNull(dst)) {
						//crda = nbaDst.getWorkItem().getParentWorkItemID();
						crda = dst.getID();
						getLogger().logDebug("crda of the case for the transaction == " + crda + "  trnsaction == " + nbaDst.getID());
					} else {
						crda = nbaDst.getID();
						getLogger().logDebug("crda of the transaction but case== " + crda);
					}
				}
				//End NBLXA-2498
				AxaReassignDataVO reassignVo = new AxaReassignDataVO();
				Policy policy = nbaTxLife.getPolicy();
				NbaParty jntInsParty = nbaTxLife.getJointParty();
				Person jntInsPerson = jntInsParty.getPerson();
				// Begin NBLXA-2529
				reassignVo.setCreateDateTime(crda);
				reassignVo.setPolicynumber(policy.getPolNumber());
				// End NBLXA-2529
				reassignVo.setCompanyKey(policy.getCompanyKey());
				reassignVo.setBackendKey(policy.getBackendKey());
				reassignVo.setUserCode(user.getUserID());
				reassignVo.setStatus("Active");
				if(hasChangeSubType(DC_JNT_INSURED_FIRSTNAME)||hasChangeSubType(DC_JNT_INSURED_MIDDLENAME)
						||hasChangeSubType(DC_JNT_INSURED_LASTNAME)){
					reassignVo.setChangedType(NbaConstants.JOB_JOINT_INS_NAME_UPDATE);
					reassignVo.setChangedValue(jntInsPerson.getFirstName()+","+jntInsPerson.getMiddleName()+","+jntInsPerson.getLastName());
					msg = "Name change initiated for Joint Insured.";
					NbaSystemDataDatabaseAccessor.insertReassingmentProcessing(reassignVo);
					addComments(user, nbaDst, msg);
				}
				if(hasChangeSubType(DC_JNT_INSURED_SSN) && !NbaUtils.isBlankOrNull(jntInsParty.getSSN()) ){
					reassignVo.setChangedType(NbaConstants.JOB_JOINT_INS_SSN_UPDATE);
					reassignVo.setChangedValue(jntInsParty.getSSN());
					msg = "SSN change initiated for Joint Insured.";
					NbaSystemDataDatabaseAccessor.insertReassingmentProcessing(reassignVo);
					addComments(user, nbaDst, msg);
				}
				if(hasChangeSubType(DC_JNT_INSURED_DOB)){
					reassignVo.setChangedType(NbaConstants.JOB_JOINT_INS_DOB_UPDATE);
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
					String birthDate = simpleDateFormat.format(jntInsPerson.getBirthDate());
					reassignVo.setChangedValue(birthDate);					
					msg = "DOB change initiated for Joint Insured.";
					NbaSystemDataDatabaseAccessor.insertReassingmentProcessing(reassignVo);
					addComments(user, nbaDst, msg);
				}
			}
		}
		return nbaDst;
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

	protected void addComments(NbaUserVO user, NbaDst nbaDst,String msg) {
		NbaProcessingErrorComment comment = new NbaProcessingErrorComment();
		comment.setText(msg);
		comment.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
		comment.setOriginator(user.getUserID());
		comment.setUserNameEntered(user.getUserID());
		comment.setActionAdd();
		nbaDst.addManualComment(comment.convertToManualComment());
	}
	
	//New Method NBLXA-2498
	protected NbaDst getParentCase(NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		NbaDst parentCase = null;
		if (parentCase == null) {
			//NBA213 deleted code
			//create and set parent case retrieve option
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(nbaDst.getID(), false);
			retOpt.requestCaseAsParent();
			retOpt.requestSources();
			retOpt.requestTransactionAsSibling();//SPR2544
			retOpt.setLockWorkItem();
			retOpt.setLockParentCase();
			retOpt.setAutoSuspend();
			//get case from awd
			parentCase = WorkflowServiceHelper.retrieveWorkItem(user, retOpt); //NBA213
			//NBA213 deleted code
		}
		return parentCase;
	}



}