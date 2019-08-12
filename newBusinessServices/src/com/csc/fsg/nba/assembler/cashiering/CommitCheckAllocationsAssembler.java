package com.csc.fsg.nba.assembler.cashiering;

/*
 * *******************************************************************************<BR>
 * Copyright 2015, Computer Sciences Corporation. All Rights Reserved.<BR>
 *
 * CSC, the CSC logo, nbAccelerator, and csc.com are trademarks or registered
 * trademarks of Computer Sciences Corporation, registered in the United States
 * and other jurisdictions worldwide. Other product and service names might be
 * trademarks of CSC or other companies.<BR>
 *
 * Warning: This computer program is protected by copyright law and international
 * treaties. Unauthorized reproduction or distribution of this program, or any
 * portion of it, may result in severe civil and criminal penalties, and will be
 * prosecuted to the maximum extent possible under the law.<BR>
 * *******************************************************************************<BR>
 */

import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.AccelTransformation;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.dataobject.nba.cash.CheckAllocation;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.cash.CheckAllocationVO;

/**
 * The CommitCheckAllocationsDisAssembler processes a collection of CheckAllocationVO
 * value objects and transforms them into a collection of CheckAllocation data
 * objects for processing in the services tier.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA331</td><td>Version NB-1402</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @see 
 * @since New Business Accelerator - Version NB-1402
 */

public class CommitCheckAllocationsAssembler extends AccelTransformation {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelTransformation#assemble(com.csc.fs.Result)
	 */
	@Override
	public Result assemble(Result request) {
		Result result = new AccelResult();
		return result;
	}

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelTransformation#disassemble(java.lang.Object)
	 */
	@Override
	public Result disassemble(Object request) {
		Result result = new AccelResult();
		if (request != null) {
			int sequence = 2;
			for (CheckAllocationVO allocation : (List<CheckAllocationVO>) request) {
				allocation.setSequence(sequence++);
				result.addResult(transformAllocation(allocation));
			}
		}
		return result;
	}

	/**
	 * Creates and returns a new CheckAllocation populated with the relevant data
	 * from the CheckAllocationVO value object.
	 * @param allocation
	 * @return
	 */
	protected CheckAllocation transformAllocation(CheckAllocationVO vo) {
		CheckAllocation allocation = createAllocationDO();
		allocation.setItemID(vo.getItemID());
		allocation.setSequence(vo.getSequence());
		allocation.setCompany(vo.getCompany());
		allocation.setPolicyNumber(vo.getPolicyNumber());
		allocation.setCwaAmount(vo.getCwaAmount());
		if (vo.getPaymentMoneySource() != NbaOliConstants.OLI_TC_NULL) {
			allocation.setPaymentMoneySource(vo.getPaymentMoneySource());
		}
		if (vo.getInforcePaymentType() != NbaOliConstants.OLI_TC_NULL) {
			allocation.setInforcePaymentType(vo.getInforcePaymentType());
			if (vo.isInforcePaymentManual()) {
				allocation.setInforcePaymentManInd(NbaConstants.TRUE);
			}
		} else if (vo.getPendingPaymentType() != NbaOliConstants.OLI_TC_NULL) {
			allocation.setPendingPaymentType(vo.getPendingPaymentType());
		}
		allocation.setInforcePaymentDate(vo.getPaymentDate());
		allocation.setCostBasis(vo.getCostBasis());
		if (vo.getPreviousTaxYear()) {
			allocation.setPreviousTaxYear(NbaConstants.TRUE);
		}
		return allocation;
	}

	/**
	 * Creates a CheckAllocation data object instance.
	 * @return
	 */
	protected CheckAllocation createAllocationDO() {
		return new CheckAllocation();
	}
}
