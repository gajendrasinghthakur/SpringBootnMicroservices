package com.csc.fsg.nba.business.process;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaUserVO;

public class NbaProcRequirementOrdered4 extends NbaProcRequirementOrdered {
	
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		return super.executeProcess(user, work);
	}

}
