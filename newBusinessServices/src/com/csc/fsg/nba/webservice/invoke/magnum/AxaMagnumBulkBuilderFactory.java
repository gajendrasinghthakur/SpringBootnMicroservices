package com.csc.fsg.nba.webservice.invoke.magnum;

import com.csc.fsg.nba.foundation.NbaOliConstants;

//NBLXA-2402(NBLXA-2534)
public class AxaMagnumBulkBuilderFactory implements NbaOliConstants {
	public static AxaMagnumBulkBuildable getBuilder(long reqCode) {
		if (reqCode == OLI_REQCODE_MEDEXAMPARAMED) {
			return new AxaMagnumBulkParamedBuilder();
		}
		return null;
	}
}
