package com.csc.fsg.nba.webservice.invoke.magnum;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.swissre.magnum.client.BulkSubmitObject;

//NBLXA-2402(NBLXA-2534)
public interface AxaMagnumBulkBuildable {
	void constructReqData(RequirementInfo aReqInfo, BulkSubmitObject bulkSubmitObj, int partyIndex) throws NbaBaseException;
}
