package com.csc.fsg.nba.assembler.identification;

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
import com.csc.fs.accel.AccelTransformation;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.dataobject.nba.auxiliary.UwReqTypesReceived;

/**
 * The UnderwritingReqTypesReceivedDisAssembler assumes the input will be a String
 * containing a work/source item ID.  It returns a Result containing a UwReqTypesReceived
 * data object populated with the item ID.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA331</td><td>Version NB-1402</td><td>AWD REST</td></tr>
 * <tr><td>APSL5055</td><td>Version</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @see 
 * @since New Business Accelerator - Version NB-1402
 */

public class UnderwritingReqTypesReceivedAssembler extends AccelTransformation {

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
		UwReqTypesReceived reqTypes = new UwReqTypesReceived();
		reqTypes.setItemID((String)request);
		result.addResult(reqTypes);
		return result;
	}
}
