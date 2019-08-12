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

import java.util.ArrayList;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.AccelTransformation;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.dataobject.nba.cash.CheckAllocation;
import com.csc.fsg.nba.vo.NbaCashieringAdditionalContractVO;
import com.csc.fsg.nba.vo.cash.CheckCorrectionCommitRequest;

/**
 * The CommitCheckCorrectionAddlContractsDisAssembler processes a CheckCorrectionCommitRequest
 * value object and transforms the data into a collection of CheckAllocation data
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

public class CommitCheckCorrectionAddlContractsAssembler extends AccelTransformation {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelTransformation#assemble(com.csc.fs.Result)
	 */
	@Override
	public Result assemble(Result request) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelTransformation#disassemble(java.lang.Object)
	 */
	@Override
	public Result disassemble(Object request) {
		Result result = new AccelResult();
		result.addResults(transformRequest((CheckCorrectionCommitRequest) request));
		return result;
	}

	/**
	 * Transforms the CheckCorrectionCommitRequest into a collection of CheckAllocation
	 * data objects.
	 * @param additionalContracts
	 * @param parentCase
	 */
	protected List<CheckAllocation> transformRequest(CheckCorrectionCommitRequest request) {
		if (request.getAdditionalContracts() == null || request.getCorrectionWorkItemID() == null) {
			//no additional contracts
			return new ArrayList(1);
		}
		List<CheckAllocation> checkAllocations = new ArrayList<CheckAllocation>(request.getAdditionalContracts().size());
		int sequence = 2;
		for (NbaCashieringAdditionalContractVO contract : request.getAdditionalContracts()) {
			CheckAllocation allocation = transformCorrection(contract);
			allocation.setItemID(request.getCorrectionWorkItemID());
			allocation.setSequence(sequence++);
			checkAllocations.add(allocation);
		}
		return checkAllocations;
	}

	/**
	 * Creates and returns a CheckAllocationDO data object populated with data from the
	 * NbaCashieringAdditionalContractVO value object.
	 * @param contract
	 * @return
	 */
	protected CheckAllocation transformCorrection(NbaCashieringAdditionalContractVO contract) {
		CheckAllocation allocation = createAllocationDO();
		allocation.setPolicyNumber(contract.getContractNumber());
		allocation.setCwaAmount(contract.getAppliedAmount());
		allocation.setCostBasis(contract.getCostBasis());
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
