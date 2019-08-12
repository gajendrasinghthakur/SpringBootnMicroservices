package com.csc.fsg.nba.business.process.evaluation;

/*
 * **************************************************************************<BR>
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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************************<BR>
 */

import java.rmi.RemoteException;
import java.util.ArrayList;

import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * Class that will take care of the processing once ACOccupation model is invoked 
 * from NBCTEVAL and NBRQEVAL process.
 * <p>Implements NbaVpmsModelProcessor 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *   <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACN024</td><td>Version 4</td><td>CTEVAL/RQEVAL restructuring</td></tr>
 * <tr><td>ACN016</td><td>Version 4</td><td>PnR MB2</td></tr>
 * <tr><td>SPR2652</td><td>Version 5</td><td>APCTEVAL process getting error stopped with Run time error occured message</td><tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */

public class NbaACOccupationProcessor extends NbaVpmsModelProcessor {	
	protected ArrayList occupationImpairments = new ArrayList();
	protected ArrayList accepImpairments = null;

	/**
	 * Overridden method, calls the model and 
	 * updates the contract with impairments.
	 * @throws NbaBaseException
	 */
	public void execute() throws NbaBaseException {
		boolean isSuccess = false;
		impSrc = NbaConstants.OCCUPATION_SRC; //ACN016
		if (performingContractEvaluation()) { //SPR2652
			int partyIndex = 0;
			ArrayList al = getAllInsuredIndexes();
			OLifE oLifE = nbaTxLife.getOLifE();			
			int insuredCount = al.size();
			for(int i=0;i<insuredCount;i++){
				isSuccess = false;
				partyIndex = ((Integer)al.get(i)).intValue();
				partyID = oLifE.getPartyAt(partyIndex).getId();				
				occupationImpairments.clear();		
				isSuccess = callOccupationModel(i);
				if (!isSuccess){					 
					throw new NbaVpmsException(NbaVpmsException.VPMS_RESULTS_ERROR + NbaVpmsAdaptor.AC_OCCUPATION);	//SPR2652
				}
				getContractImpairments(partyID);
				ArrayList[] mergedLists = mergeImpairments(contractImpairments, occupationImpairments, new ArrayList(), new ArrayList());												
				addImpairmentInfo(partyID, mergedLists[0]);
			}
		}
	}
	/**
	 * This function calls the ACOccupation model	 
	 * @param partyIndex
	 * @return boolean : Returns true if the call is successful
	 * 					 Else returns false 	
	 * @throws NbaBaseException
	 */
	//ACP011 new method.
	public boolean callOccupationModel(int partyIndex) throws NbaBaseException{		
		boolean success = false;	//SPR2652
		NbaOinkRequest oinkRequest = new NbaOinkRequest();	//SPR2652	
		if (updatePartyFilterInRequest(oinkRequest, partyID)) { //SPR2652
		    NbaVpmsAdaptor vpmsProxy = null; //SPR3362
			try {
				NbaOinkDataAccess accessContract = new NbaOinkDataAccess(nbaTxLife);
				accessContract.setLobSource(work.getNbaLob());
				accessContract.setAcdbSource(new NbaAcdb(), nbaTxLife);
				vpmsProxy = new NbaVpmsAdaptor(accessContract, NbaVpmsAdaptor.AC_OCCUPATION); //SPR3362
				vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_RESULT);
				// SPR2652 code deleted
				oinkRequest.setArgs(getKeys());
				// SPR2652 code deleted
				vpmsProxy.setANbaOinkRequest(oinkRequest);
				NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
				if (vpmsResultsData.getResult().isError()) {
					//SPR3362 code deleted
					throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.AC_OCCUPATION); //SPR2652
				}
				// SPR2652 code deleted
				String xmlString = (String) vpmsResultsData.getResultsData().get(0);
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Results from VPMS Model: " + NbaVpmsAdaptor.AC_OCCUPATION);
					getLogger().logDebug(xmlString);
				}
				NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
				VpmsModelResult vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
				occupationImpairments = vpmsModelResult.getImpairmentInfo();
				success = true;
				// SPR2652 code deleted
				//SPR3362 code deleted
			}
			// SPR2652 code deleted
			catch (RemoteException e) { //SPR2652
				handleRemoteException(e, NbaVpmsAdaptor.AC_OCCUPATION); //SPR2652
			//begin SPR3362
			} finally {
			    if(vpmsProxy != null){
			        try {
	                    vpmsProxy.remove();
	                } catch (RemoteException e) {
	                    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
	                }
			    }
			//end SPR3362
			}
		}
		return success;
	}
}
