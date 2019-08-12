package com.csc.fsg.nba.process.ghcp;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.process.contract.CommitContractBP;
import com.csc.fsg.nba.vo.GHCPCriteriaInfoVO;
import com.csc.fsg.nba.vo.NbaContractUpdateVO;
import com.csc.fsg.nba.vo.NbaDst;

public class CommitGhcpCriteriaBP extends CommitContractBP {

	public Result process(Object input) {
		// TODO Auto-generated method stub
		AccelResult result = new AccelResult();
		GHCPCriteriaInfoVO ghcpCriteriaInfoVo = (GHCPCriteriaInfoVO) input;
		NbaDst work = ghcpCriteriaInfoVo.getNbaDst();
		// need to code here

		NbaContractUpdateVO request = new NbaContractUpdateVO();
		request.setStatus(work.getStatus());
		request.setUpdateWork(true);
		request.setNbaUserVO(ghcpCriteriaInfoVo.getNbaUserVO());
		request.setNbaDst(work);
		request.setNbaTXLife(ghcpCriteriaInfoVo.getTxLife());
		try {
			result = persistContract(request);
		} catch (NbaBaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

}
