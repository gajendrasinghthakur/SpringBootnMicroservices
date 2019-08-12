


package com.csc.fsg.nba.assembler.datafeed;

import java.util.Date;
import java.util.Iterator;

import javax.resource.cci.MappedRecord;

import com.csc.fs.Result;
import com.csc.fs.UserSessionKey;
import com.csc.fs.accel.AccelTransformation;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.dataobject.nba.datafeed.NbaDataFeedDO;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;


/**
 * This class is used for data feed assembling and disassembling.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <tr><td>AXAL3.7.54</td><td>AXA Life Phase 1</td><td>AXAOnline / AXA Distributors Service</td></tr>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class CommitDataFeedAssembler extends AccelTransformation {

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
				//TO DO - @@ASSEMBLE_CONTENT@@
			}
		}
		return returnResult;
	}

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelTransformation#disassemble(java.lang.Object)
	 */
	public Result disassemble(Object request) {
		MappedRecord record = (MappedRecord) request;
		NbaDataFeedDO dataFeedDO = new NbaDataFeedDO();
		// Get contract keys from the record
		MappedRecord contractKeys = (MappedRecord)record.get("ContractKeys");
		dataFeedDO.setCompanyCode((String)contractKeys.get("CompanyCode"));
		dataFeedDO.setContractNumber((String)contractKeys.get("ContractNumber"));
		dataFeedDO.setBackendSystem((String)contractKeys.get("BackendSystem"));
		// get UserSessionKey for user id
		try {
			dataFeedDO.setUserID((UserSessionKey.create((String)record.get("UserSessionKey"))).getUserId());
		} catch (Throwable t) {
			// if user id not available, use process name user id
			dataFeedDO.setUserID((String)record.get("UserName"));			
		}
		// get date and time 
		dataFeedDO.setFeedDate((Date)record.get("FeedDate"));
		dataFeedDO.setFeedTime((NbaTime)record.get("FeedTime"));
		// get username and password from the record
		dataFeedDO.setProcessName((String)record.get("UserName"));
		dataFeedDO.setProcessPwd((String)record.get("Password"));
		dataFeedDO.setOperatingMode((String)record.get("OperatingMode"));
		dataFeedDO.setToken((String)record.get("Token")); //AXAL3.7.54
		Result result = new AccelResult();
		result.addResult(dataFeedDO);
		return result;
	}

}
