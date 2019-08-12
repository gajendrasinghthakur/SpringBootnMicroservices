package com.csc.fsg.nba.assembler.nbascorfeed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.AccelTransformation;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.dataobject.nba.nbascorfeed.NbaScorFeedDO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaUserVO;
/*
 * ************************************************************** <BR>
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
 *     Copyright (c) 2002-2009 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */

/**
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>NBA-SCOR</td></tr>
 *  </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */
public class NbaScorFeedAssembler extends AccelTransformation {

	public Result assemble(Result result) {
		Result returnResult = new AccelResult();
		if (result.hasErrors()) {
			returnResult.merge(result);
			return returnResult;
		}
		if (result.getData() != null) {
			Iterator iter = result.getData().iterator();
			while (iter.hasNext()) {
				Object o = iter.next();
				returnResult.addResult(o);
			}
		}
		return returnResult;
	}

	public Result disassemble(Object request) {
		List nbaScorFeedList = (ArrayList) request;
		Iterator iter = nbaScorFeedList.iterator();
		NbaScorFeedDO nbaScorFeedDO = new NbaScorFeedDO();
		//nbaScorFeedDO.setUserSessionKey(srvcCtxt.getUserSession().getUserSessionKey().asString());
		while (iter.hasNext()) {
			Object o = iter.next();
			if (o instanceof NbaUserVO) {
				NbaUserVO userVO = (NbaUserVO) o;
				nbaScorFeedDO.setToken(userVO.getToken());
				nbaScorFeedDO.setUserSessionKey(userVO.getSessionKey());
			} else if (o instanceof NbaDst) {
				NbaDst dst = (NbaDst) o;
				nbaScorFeedDO.setWorkitemID(dst.getID());
				nbaScorFeedDO.setContractNumber(dst.getNbaLob().getPolicyNumber());
			}
		}
		Result result = new AccelResult();
		result.addResult(nbaScorFeedDO);
		return result;
	}

}