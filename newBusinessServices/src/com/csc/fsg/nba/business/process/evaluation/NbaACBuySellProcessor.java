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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Class that will take care of the processing once ACKeyPerson model is invoked 
 * from NBCTEVAL process.
 * <p>Implements NbaVpmsModelProcessor 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACP018</td><td>Version 4</td><td>Buy Sell</td></tr>
 * <tr><td>ACN016</td><td>Version 4</td><td>PnR MB2</td></tr>
 * <tr><td>SPR2652</td><td>Version 5</td><td>APCTEVAL process getting error stopped with Run time error occured message</td><tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */

public class NbaACBuySellProcessor extends NbaVpmsModelProcessor{
	
	protected ArrayList buySellImpairments = new ArrayList();
	/**
	 * Overridden method, calls the model and 
	 * updates the contract with impairments.
	 * @throws NbaBaseException
	 */
	public void execute() throws NbaBaseException {
		boolean isSuccess = false;
		impSrc = NbaConstants.BUYSELL_SRC; //ACN016
		if(performingContractEvaluation()){	//SPR2652
			int partyIndex = 0;
			List al = getAllInsuredIndexes(); // SPR3290
			OLifE oLifE = nbaTxLife.getOLifE();
			int insListCount = al.size();
			// SPR3290 code deleted
			for(int i=0;i<insListCount;i++){
				partyIndex = ((Integer)al.get(i)).intValue();
				String partyId = oLifE.getPartyAt(partyIndex).getId();
				partyID = partyId;
				// SPR3290 code deleted
				buySellImpairments.clear();	  				
										
				isSuccess = callBuySellModel(partyId, i);
				if (!isSuccess){
					throw new NbaVpmsException(NbaVpmsException.VPMS_RESULTS_ERROR + NbaVpmsAdaptor.AC_BUYSELL);	//SPR2652 
				}
				getContractImpairments(partyId);
				ArrayList[] mergedLists = mergeImpairments(contractImpairments, buySellImpairments, new ArrayList(), new ArrayList());
				ArrayList arrMerged = mergedLists[0];
				addImpairmentInfo(partyId, arrMerged);						
			}
		}
	}
	/**
	* This method is used to call the ACKeyPerson model	 
	* @param  partyIndex: Index of the insured
	* @return boolean : Returns true if the call is successful
	* 					 Else returns false 	
	* @throws NbaBaseException
	*/	
    //ACP018 New method.
    public boolean callBuySellModel(String partyId, int insuredIndex) throws NbaBaseException{
        boolean success = false;	//SPR2652
		ArrayList results = null;
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(nbaTxLife);
		oinkData.setAcdbSource(new NbaAcdb(), nbaTxLife);
        oinkData.setLobSource(work.getNbaLob());
		partyID = partyId;			
		//SPR2652 code deleted
		NbaOinkRequest oinkRequest = new NbaOinkRequest();

		oinkRequest.setArgs(getKeys());
		Map deOink = new HashMap();

		NbaACUtils.getFirstPartyDeOinkValues(oinkData, "AccumBuySellCoverage_OWN", deOink);
		NbaACUtils.getFirstPartyDeOinkValues(oinkData, "BuySellPersonsInsInd_OWN", deOink);
		NbaACUtils.getFirstPartyDeOinkValues(oinkData, "CurrentAssetsAmt_OWN", deOink);
		NbaACUtils.getFirstPartyDeOinkValues(oinkData, "CurrentLiabilitiesAmt_OWN", deOink);
		NbaACUtils.getFirstPartyDeOinkValues(oinkData, "NetWorthValuationCode_OWN", deOink);
		NbaACUtils.getFirstPartyDeOinkValues(oinkData, "OrgForm_OWN", deOink);
		NbaACUtils.getFirstPartyDeOinkValues(oinkData, "ReltoAnnOrIns_BEN", deOink);
		NbaACUtils.getFirstPartyDeOinkValues(oinkData, "ReltoAnnOrIns_OWN", deOink);
		NbaACUtils.getFirstPartyDeOinkValues(oinkData, "YrEndNetWorthAmt_OWN", deOink);
		// SPR2652 code deleted
		//begin SPR3362
        NbaVpmsAdaptor vpmsProxy = null;
        try {
            if (updatePartyFilterInRequest(oinkRequest, partyId)) { //SPR2652
                vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.AC_BUYSELL);//SPR2652
                vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_RESULT);//SPR2652
                vpmsProxy.setSkipAttributesMap(deOink);//SPR2652
                vpmsProxy.setANbaOinkRequest(oinkRequest);
                try {
                    NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
                    results = vpmsResultsData.getResultsData();
                    //Resulting string will be the zeroth element.
                    if (results == null) {
                        throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.AC_BUYSELL); //SPR2652
                    }
                    // SPR2652 code deleted
                    vpmsResult = (String) results.get(0);
                    NbaVpmsModelResult vpmsOutput = new NbaVpmsModelResult(vpmsResult);
                    VpmsModelResult vpmModelResult = vpmsOutput.getVpmsModelResult();
                    buySellImpairments = vpmModelResult.getImpairmentInfo(); //ACN024
                    success = true;
                    // SPR2652 Code Deleted
                } catch (RemoteException re) {
                    handleRemoteException(re, NbaVpmsAdaptor.AC_BUYSELL); //SPR2652
                    // SPR2652 Code Deleted
                }
            }
        } finally {
            if (vpmsProxy != null) {
                try {
                    vpmsProxy.remove();
                } catch (Exception e) {
                    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
                }
            }
        }
        //end SPR3362
		return success;								
     }     
}
