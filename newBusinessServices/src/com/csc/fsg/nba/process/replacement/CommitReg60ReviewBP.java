package com.csc.fsg.nba.process.replacement;

import java.util.HashMap;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.vo.NbaContractUpdateVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;



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

/**
 * BP for Reg60 processing.  Will attempt to create a new NBREPLNOTIF work item if necessary, then perform updates.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA231</td><td>Version 8</td><td>Replacement Processing</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 1
 */
public class CommitReg60ReviewBP extends com.csc.fsg.nba.process.contract.CommitContractBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result;
		try {
			result = generateReplacement((NbaContractUpdateVO) input);
		} catch (Exception e) {
			result = new AccelResult();
			addExceptionMessage(result, e);
		}
		return result;
	}
	private AccelResult generateReplacement(NbaContractUpdateVO input) throws NbaBaseException {
		
		NbaUserVO user = new NbaUserVO(input.getNbaTXLife().getBusinessProcess(),"");
		if (!input.getNbaDst().getWorkType().equalsIgnoreCase("NBRPLNOTIF")) {
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(user, input.getNbaDst(), input.getNbaTXLife(), new HashMap());
		    createReg60Transaction(input.getNbaDst(), provider);
		}
		AccelResult result = persistContract(input);
		processResult(result);
		return result;
		
		
	}
	/**
	 * Creates a re-evaluate work item setting the initial status, priority, and
	 * appropriate lobs.
	 * @param work
	 * @param provider
	 * @return
	 */
	protected void createReg60Transaction(NbaDst work, NbaProcessWorkItemProvider provider) throws NbaBaseException {
		NbaTransaction reg60Transaction = work.addTransaction(provider.getWorkType(), provider.getInitialStatus());
		reg60Transaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
		reg60Transaction.getTransaction().setWorkType(provider.getWorkType());
		reg60Transaction.getTransaction().setLock("Y");
		reg60Transaction.setStatus(provider.getInitialStatus());
        reg60Transaction.getTransaction().setUpdate("Y");
		//Copy lobs from the case to the new transaction
		NbaLob caseLob = work.getNbaLob();
		NbaLob reg60Lob = reg60Transaction.getNbaLob();

		reg60Lob.setPolicyNumber(caseLob.getPolicyNumber());
		reg60Lob.setCompany(caseLob.getCompany());
		reg60Lob.setLastName(caseLob.getLastName());
		reg60Lob.setFirstName(caseLob.getFirstName());
		reg60Lob.setSsnTin(caseLob.getSsnTin());
		reg60Lob.setTaxIdType(caseLob.getTaxIdType());

	}
}
