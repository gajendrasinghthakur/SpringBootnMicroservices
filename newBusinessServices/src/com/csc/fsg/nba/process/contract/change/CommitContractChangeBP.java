package com.csc.fsg.nba.process.contract.change;

/* 
 * *******************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */

import com.csc.fs.Result;
import com.csc.fs.accel.AccelBP;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaContractChangeDataTable;
import com.csc.fsg.nba.vo.NbaContractChangeVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fs.ServiceContext;

/**
 * Processes the contract change data retrieval.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA185</td><td>Version 7</td><td>Contract Change Rewrite</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>AXAL3.7.04</td><td>Axa Life Phase 1</td><td>Paid Changes</td></tr>
 * <tr><td>APSL5055-NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class CommitContractChangeBP extends AccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            if (input instanceof NbaContractChangeVO) {
                NbaContractChangeVO ccVO = (NbaContractChangeVO) input;
                saveData(ccVO);
                if (ccVO.isCommitRequested()) {
                    performCommit(ccVO);
                }
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
     * Saves data to the workflow and database.
     * 
     * @param ccVO
     *            the contract change value object
     */
	protected void saveData(NbaContractChangeVO ccVO) throws Exception {
		NbaDst nbaDst = ccVO.getOriginalWork();
		if (ccVO.getOriginalWork() == null) {
			nbaDst = createTransaction();
		}
		updateLOBs(nbaDst.getNbaLob(), ccVO);
		nbaDst.setUpdate();
		nbaDst = WorkflowServiceHelper.updateWork(ccVO.getNbaUserVO(), nbaDst);
		ccVO.setOriginalWork(nbaDst);
		NbaTXLife tempData = ccVO.getTempContract();
		if (tempData != null) {//AXAL3.7.04
			NbaOLifEId nbaOLifEId = new NbaOLifEId(tempData);
			nbaOLifEId.assureId(tempData);
			saveDataIntoDatabase(ccVO); //save change contract data into database
		}
	}
	
	
	/**
	 * Call VP/MS model to get pass status.
	 * @param ccVO the contract change value object
	 */
	protected void performCommit(NbaContractChangeVO ccVO) throws NbaBaseException {
        NbaUserVO user = new NbaUserVO(NbaConstants.PROC_VIEW_CONTRACT_CHANGE, "");
        NbaDst nbaDst = ccVO.getOriginalWork();
        NbaProcessStatusProvider provider = new NbaProcessStatusProvider(user, nbaDst);
        nbaDst.setStatus(provider.getPassStatus());
        nbaDst = WorkflowServiceHelper.updateWork(ccVO.getNbaUserVO(), nbaDst);
        ccVO.setOriginalWork(nbaDst);
        WorkflowServiceHelper.unlockWork(ccVO.getNbaUserVO(), nbaDst);
    }
	
    /**
	 * Creates contract change workitem in memory.
	 * @return the new contract change workitem
	 * @throws NbaBaseException
	 */
	protected NbaDst createTransaction() throws NbaBaseException {
        NbaUserVO user = new NbaUserVO(NbaConstants.PROC_VIEW_CONTRACT_CHANGE, "");
        NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(user, new NbaLob());
        NbaDst nbaDst = null;
        if (provider != null && provider.getWorkType() != null && provider.getInitialStatus() != null) {
        	//NBA208-32
            WorkItem transaction = new WorkItem();
            nbaDst = new NbaDst();
            //NBA208-32
            nbaDst.setUserID(user.getUserID());
            nbaDst.setPassword(user.getPassword());
            nbaDst.addTransaction(transaction);
            transaction.setBusinessArea(NbaConstants.A_BA_NBA);
            //NBA208-32
            transaction.setLock("Y");
            transaction.setAction("L");
            transaction.setWorkType(provider.getWorkType());
            transaction.setStatus(provider.getInitialStatus());
            //NBA208-32
            ServiceContext ctx = ServiceContext.currentContext();   //APSL5055-NBA331
            transaction.setSystemName(NbaUtils.getSystemForUser(ctx));  //APSL5055-NBA331
            transaction.setCreate("Y");
            nbaDst.increasePriority(provider.getWIAction(), provider.getWIPriority());
        } else {
            throw new NbaBaseException("Invalid Work Type or Initial Status");
        }
        return nbaDst;
    }
	
	/**
	 * Save temporary contract change data on the database
	 * @param nbaDst The original workitem
	 * @param holding the contract data
	 */
	protected void saveDataIntoDatabase(NbaContractChangeVO ccVO) throws NbaBaseException {
        String xmlContract = ccVO.getTempContract().toXmlString();
        NbaContractChangeDataTable tableData = new NbaContractChangeDataTable();
        tableData.setWorkItemId(ccVO.getOriginalWork().getID());
        tableData.setChangeType(NbaUtils.convertStringToLong(ccVO.getDefaultLobs().getContractChgType()));
        tableData.retrieveData();
        if (tableData.getTempContract() != null && tableData.getTempContract().trim().length() > 0) {
            tableData.setTempContract(xmlContract);
            tableData.update();
        } else {
            tableData.setTempContract(xmlContract);
            tableData.insert();
        }
    }
	
	/**
	 * Converts contract change bean fields to equivalent NbaLob fields 
	 * @param contractChange the contract change bean
	 * @return the converted NbaLob
	 */
	protected void updateLOBs(NbaLob origLob, NbaContractChangeVO ccVO) throws NbaBaseException {
        NbaLob lob = ccVO.getDefaultLobs();
        origLob.setCompany(lob.getCompany());
        origLob.setPlan(lob.getPlan());
        origLob.setPolicyNumber(lob.getPolicyNumber());
        origLob.setAppDate(lob.getAppDate());
        origLob.setContractChgType(lob.getContractChgType());
        origLob.setNoChngFrmReqd(lob.getNoChngFrmReqd());
        origLob.setRequiresUnderwriting(lob.getRequiresUnderwriting());//ALS5351
        origLob.setBackendSystem(lob.getBackendSystem());
        origLob.setOperatingMode(lob.getOperatingMode());
        origLob.setIFRxPaidDate(lob.getIFRxPaidDate());
        origLob.setIFRxDueDate(lob.getIFRxDueDate());
    }
}
