package com.csc.fsg.nba.assembler.nbascorfeed;

import java.util.Iterator;

import javax.resource.cci.MappedRecord;

import com.csc.fs.Result;
import com.csc.fs.accel.AccelTransformation;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.dataobject.nba.nbascorfeed.NbaScorFeedDO;

/**
 * NbaDataFeedService - This services invokes the
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>APSL2808-NBA-SCOR</td><td>AXA Life</td><td>Simplified Issue</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 8.0.0
 *  
 */
public class CommitNbaScorFeedAssembler extends AccelTransformation {

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
				//TODO - @@ASSEMBLE_CONTENT@@
			}
		}
		return returnResult;
	}

	public Result disassemble(Object request) {
		MappedRecord record = (MappedRecord) request;
		NbaScorFeedDO nbaScorFeedDO = new NbaScorFeedDO();
		nbaScorFeedDO.setToken((String) record.get("Token"));
		nbaScorFeedDO.setPassword((String) record.get("Password"));
		nbaScorFeedDO.setUserName((String) record.get("UserName"));
		nbaScorFeedDO.setWorkitemID((String) record.get("WorkitemID"));
		nbaScorFeedDO.setContractNumber((String) record.get("ContractNumber"));		
		nbaScorFeedDO.setUserSessionKey((String) record.get("UserSessionKey"));
		Result result = new AccelResult();
		result.addResult(nbaScorFeedDO);
		return result;
	}

}