package com.csc.fsg.nba.assembler.datafeed;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.AccelTransformation;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.TimeStamp;
import com.csc.fs.dataobject.nba.datafeed.NbaDataFeedDO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.UserAuthResponse;
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
 * NbaDataFeedAssembler pulls data from the NbaTXLife object that is needed for the 
 * data feed process.
 * 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA232</td><td>Version 8</td><td>nbA Feed for a Customer's Web Site</td></tr>
 * <tr><td>AXAL3.7.54</td><td>AXA Life Phase 1</td><td>AXAOnline / AXA Distributors Service</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 8.0.0
 *
 */
public class NbaDataFeedAssembler extends AccelTransformation {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelTransformation#assemble(com.csc.fs.Result)
	 */
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

	/* Pulls values from the NbaTXLife object and places them in a NbaDataFeedDO object.
	 * @see com.csc.fs.accel.AccelTransformation#disassemble(java.lang.Object)ler
	 */
	public Result disassemble(Object request) {
		List dataFeedList = (ArrayList)request;
		Iterator iter = dataFeedList.iterator();
		NbaDataFeedDO dataFeedDO = new NbaDataFeedDO();
		dataFeedDO.setUserSessionKey(srvcCtxt.getUserSession().getUserSessionKey().asString());
		while (iter.hasNext()) {
			Object o = iter.next();
			if( o instanceof NbaTXLife) {
				NbaTXLife nbATXLife = (NbaTXLife)o;
				dataFeedDO.setCompanyCode(nbATXLife.getPolicy().getCarrierCode());
				dataFeedDO.setContractNumber(nbATXLife.getPolicy().getPolNumber());
				dataFeedDO.setBackendSystem(nbATXLife.getOLifE().getSourceInfo().getFileControlID());
				try {
					TXLifeResponse resp = nbATXLife.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0);
					if ( null != resp ) {
						dataFeedDO.setFeedTime(resp.getTransExeTime());
						dataFeedDO.setFeedDate(resp.getTransExeDate());
					}
				} catch (Throwable t) {
					NbaTime nt = new NbaTime();
					dataFeedDO.setFeedDate(nt.getTime());
					dataFeedDO.setFeedTime(nt);
				}
			} else if( o instanceof NbaUserVO) {
				NbaUserVO userVO = (NbaUserVO)o;
				dataFeedDO.setUserID(userVO.getUserID());
				dataFeedDO.setToken(userVO.getToken()); //AXAL3.7.54
			} else if( o instanceof String) {
				dataFeedDO.setOperatingMode((String)o);
			}
		}
		Result result = new AccelResult();
		result.addResult(dataFeedDO);
		return result;
	}

}
