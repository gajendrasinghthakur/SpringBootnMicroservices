package com.csc.fs.accel.newBusiness.cash.api;

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

import com.csc.fs.ComponentBase;
import com.csc.fs.ObjectRepository;
import com.csc.fs.Result;
import com.csc.fs.dataobject.nba.cash.CheckAllocation;
import com.csc.fs.sa.PreProcess;
import com.csc.fs.sa.SystemAPI;
import com.csc.fs.sa.SystemService;

/**
 * Insert a new <code>CheckAllocation</code> at the beginning of the input list and
 * mark it for delete so that all check allocations for the specified work items
 * will be deleted.  A post process will be run to remove the newly created data object.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA331.1</td><td>Version NB-1402</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @see 
 * @since New Business Accelerator - Version NB-1402
 */

public class DeleteCheckAllocationsPreProcess extends ComponentBase implements PreProcess {

	/* (non-Javadoc)
	 * @see com.csc.fs.sa.PreProcess#systemApi(java.util.List, com.csc.fs.sa.SystemService, com.csc.fs.sa.SystemAPI, com.csc.fs.ObjectRepository)
	 */
	@Override
	public Result systemApi(List input, SystemService service, SystemAPI api, ObjectRepository or) {
		if (!input.isEmpty()) {
			CheckAllocation allocation = (CheckAllocation) input.get(0);
			CheckAllocation deleteAllocation = new CheckAllocation();
			deleteAllocation.setItemID(allocation.getItemID());
			deleteAllocation.markDeleted();
			input.add(0, deleteAllocation);
		}

		return Result.Factory.create();
	}

	/* (non-Javadoc)
	 * @see com.csc.fs.sa.PreProcess#systemService(java.util.List, com.csc.fs.sa.SystemService, com.csc.fs.ObjectRepository)
	 */
	@Override
	public Result systemService(List input, SystemService service, ObjectRepository or) {
		// TODO Auto-generated method stub
		return null;
	}
}
