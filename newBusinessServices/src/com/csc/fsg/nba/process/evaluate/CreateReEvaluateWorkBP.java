package com.csc.fsg.nba.process.evaluate;

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

import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.LockRetrieveWorkRequest;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaContractUpdateVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * Create re-evaluate work item
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA211</td><td>Version 7</td><td>Partial Application</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 deleted code
public class CreateReEvaluateWorkBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
		    result.addResult(generateEvaluateWorkItem((NbaContractUpdateVO) input));
		} catch (Exception e) {
			addExceptionMessage(result, e);
		}
		return result;
	}

	

    /**
     * Check to see if a re-evaluate work item needs to be created
     * 
     * @param contractUpdateVO
     * @exception com.csc.fsg.nba.exception.NbaBaseException
     * @return NbaDst
     */
    public NbaDst generateEvaluateWorkItem(NbaContractUpdateVO contractUpdateVO) throws NbaBaseException {
        NbaDst nbaDst = contractUpdateVO.getNbaDst();
        NbaUserVO tempUserVO = new NbaUserVO(contractUpdateVO.getUserID(), "");
        NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(tempUserVO, nbaDst);
        getEvaluateTransactions(nbaDst, provider.getWorkType());  //NBA208-36
        List aList = nbaDst.getNbaTransactions();
        NbaTransaction evalTransaction = null;
        String evalWorkType = provider.getWorkType();
        int count = aList.size();
        for (int i = 0; i < count; i++) {
            evalTransaction = (NbaTransaction) aList.get(i);
            if (evalTransaction.getTransaction().getWorkType().equalsIgnoreCase(evalWorkType)) {
                if (!evalTransaction.isInEndQueue()) {
                    return nbaDst;
                }
            }
        }

        evalTransaction = createReEvalTransaction(nbaDst, provider);
        //TODO do I really need to set this. Try to redo without
        evalTransaction.setUpdate();
        nbaDst.setUpdate();

        //Call netserver update method
        nbaDst = WorkflowServiceHelper.update(contractUpdateVO.getNbaUserVO(), nbaDst);

        //return the updated nbaDST
        return nbaDst;
    }

    /**
     * Creates a re-evaluate work items
     * 
     * @param nbaDst
     * @param provider
     * @param evalTransaction
     * @return
     */
    protected NbaTransaction createReEvalTransaction(NbaDst nbaDst, NbaProcessWorkItemProvider provider)  throws NbaBaseException {
        NbaTransaction evalTransaction = nbaDst.addTransaction(provider.getWorkType(), provider.getInitialStatus());
        evalTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
        evalTransaction.getTransaction().setWorkType(provider.getWorkType());

        evalTransaction.setStatus(provider.getInitialStatus());
        //Copy lobs from the case to the new transaction
        NbaLob caseLob = nbaDst.getNbaLob();
        NbaLob reEvalLob = evalTransaction.getNbaLob();

        reEvalLob.setPolicyNumber(caseLob.getPolicyNumber());
        reEvalLob.setCompany(caseLob.getCompany());
        reEvalLob.setLastName(caseLob.getLastName());
        reEvalLob.setFirstName(caseLob.getFirstName());
        reEvalLob.setSsnTin(caseLob.getSsnTin());
        reEvalLob.setTaxIdType(caseLob.getTaxIdType());
        evalTransaction.getTransaction().setLock("Y");  //NBA208-32
        return evalTransaction;
    }
	
	/**
	 * Retrieves a list of evaluate transactions based on worktype.
	 * @param work
	 * @param workType
	 * @return
	 */
	//NBA208-36 New Method
	protected void getEvaluateTransactions(NbaDst work, String workType) throws NbaBaseException {
		LockRetrieveWorkRequest request = new LockRetrieveWorkRequest();
		//setup the search lobs
		NbaLob workLob = work.getNbaLob();
		NbaLob tempLob = new NbaLob();
		tempLob.setCompany(workLob.getCompany());
		tempLob.setPolicyNumber(workLob.getPolicyNumber());

		request.setBusinessArea(workLob.getBusinessArea());
		request.setWorkType(workType);
		request.setPageNumber("1"); //APSL5055-NBA331
		request.setLobData((LobData[]) tempLob.getLobs().toArray(new LobData[tempLob.getLobs().size()]));
		request.setWorkItem(work.getCase());
		request.setRetrieveWorkLocked(true);

		AccelResult result = (AccelResult) callService("LockRetrieveWorkBP", request);
		if (!result.hasErrors()) {
			work.addCase((WorkItem) result.getFirst());
		}
	}
}
