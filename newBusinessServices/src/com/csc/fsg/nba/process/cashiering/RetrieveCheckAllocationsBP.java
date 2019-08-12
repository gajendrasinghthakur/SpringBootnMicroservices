package com.csc.fsg.nba.process.cashiering;

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

import com.csc.fs.Result;
import com.csc.fs.accel.constants.newBusiness.ServiceCatalog;
import com.csc.fsg.nba.process.NewBusinessAccelBP;

/**
 * Retrieves the check allocations for an work/source item.
 * The RetrieveCheckAllocationsDisassembler handles transforming the input value object
 * into the expected data object input for the system access service call,
 * cash/retrieveCheckAllocations.  The assembler transforms the data object results
 * into value objects.
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

public class RetrieveCheckAllocationsBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	@Override
	public Result process(Object input) {
		Result result = callService(ServiceCatalog.RETRIEVE_CHECK_ALLOC_DISASSEMBLER, input);
		if (!result.hasErrors()) {
			result = invoke(ServiceCatalog.RETRIEVE_CHECK_ALLOCATIONS, result.getData());
			if (!result.hasErrors()) {
				result = callService(ServiceCatalog.RETRIEVE_CHECK_ALLOC_ASSEMBLER, result);
			}
		}
		return result;
	}

}
