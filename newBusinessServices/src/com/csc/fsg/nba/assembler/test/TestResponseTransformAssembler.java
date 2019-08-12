package com.csc.fsg.nba.assembler.test;

/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which are
 * proprietary to CSC Financial Services Group®.  The use, reproduction,
 * distribution or disclosure of this program, in whole or in part, without
 * the express written permission of CSC Financial Services Group is prohibited.
 * This program is also an unpublished work protected under the copyright laws
 * of the United States of America and other countries.
 *
 * If this program becomes published, the following notice shall apply:
 *    Property of Computer Sciences Corporation.<BR>
 *    Confidential. Not for publication.<BR>
 *    Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.AccelTransformation;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.WebServiceCall;

/**
 * Transformation services to create a <code>WebServiceCall</code> value object
 * populated with an XML response.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA234</td><td>Version 8</td><td>ACORD Transformation Service</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */

public class TestResponseTransformAssembler extends AccelTransformation {

	/**
	 * Populates a <code>Result</code> from an incoming <code>List</code> containing a
	 * single XML response string. The string is pulled from the list and placed in the
	 * result directly.
	 * @param input
	 * @return
	 */
	public Result disassemble(Object input) {
		Result result = new AccelResult();
		List data = (List) input;
		result.addResult(data.get(0));
		return result;
	}

	/**
	 * Returns a <code>Result</code> with a <code>WebServiceCall</code> populated with
	 * an response message.
	 * @param request
	 * @return
	 */
	public Result assemble(Result request) {
		WebServiceCall wsCall = new WebServiceCall();
		wsCall.setResponse((String) request.getFirst());

		Result result = new AccelResult();
		result.addResult(wsCall);
		return result;
	}
}
