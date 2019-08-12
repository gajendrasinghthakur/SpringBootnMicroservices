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
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.cash.CheckAllocationVO;

/**
 * The RetrieveCheckAllocationsDisAssembler expects an NbaSource value object from which
 * it pulls the work/source item's ID and returns a CheckAllocation data object populated
 * with the item's ID.
 * <p>
 * The assembler processes zero to many CheckAllocation data objects and transforms the
 * data into CheckAllocationVO value objects. 
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

public class RetrieveCheckAllocationsAssembler extends AccelTransformation {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelTransformation#assemble(com.csc.fs.Result)
	 */
	@Override
	public Result assemble(Result request) {
		Result result = new AccelResult();
		if (request != null) {
			for (CheckAllocation allocation : (List<CheckAllocation>)request.getData()) {
				result.addResult(createCheckAllocationVO(allocation));
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelTransformation#disassemble(java.lang.Object)
	 */
	@Override
	public Result disassemble(Object request) {
		Result result = new AccelResult();
		CheckAllocation allocation = new CheckAllocation();
		if (request instanceof NbaSource) {
			NbaSource source = (NbaSource) request;
			allocation.setItemID(source.getID());
		}
		result.addResult(allocation);
		return result;
	}

	/**
	 * Creates a CheckAllocationVO and populates it from data in the CheckAllocation
	 * data object. 
	 * @param allocationDO
	 * @return
	 */
	protected CheckAllocationVO createCheckAllocationVO(CheckAllocation allocationDO) {
		CheckAllocationVO allocation = createCheckAllocation();
		allocation.setItemID(allocationDO.getItemID());
		allocation.setSequence(allocationDO.getSequence());
		allocation.setCompany(allocationDO.getCompany());
		allocation.setPolicyNumber(allocationDO.getPolicyNumber());
		allocation.setCwaAmount(allocationDO.getCwaAmount());
		if (allocationDO.getPaymentMoneySource() != null) {
			allocation.setPaymentMoneySource(allocationDO.getPaymentMoneySource());
		}
		if (allocationDO.getInforcePaymentType() != null) {
			allocation.setInforcePaymentType(allocationDO.getInforcePaymentType());
			allocation.setInforcePaymentManual(allocationDO.getInforcePaymentManInd() != null);
		} else if (allocationDO.getPendingPaymentType() != null) {
			allocation.setPendingPaymentType(allocationDO.getPendingPaymentType());
		}
		allocation.setPaymentDate(allocationDO.getInforcePaymentDate());
		allocation.setCostBasis(allocationDO.getCostBasis());
		allocation.setPreviousTaxYear(allocationDO.getPreviousTaxYear() != null);
		return allocation;
	}

	/**
	 * Returns a CheckAllocationVO value object instance.
	 * @return
	 */
	protected CheckAllocationVO createCheckAllocation() {
		return new CheckAllocationVO();
	}
}
