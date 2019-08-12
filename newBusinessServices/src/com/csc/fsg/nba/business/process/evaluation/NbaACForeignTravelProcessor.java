package com.csc.fsg.nba.business.process.evaluation;

/*
 * **************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group�.  The use,<BR>
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
 * Class that will take care of the processing once ACForeignTravel model is invoked 
 * from NBCTEVAL or NBRQEVAL process.
 * <p>Implements NbaVpmsModelProcessor 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACP022</td><td>Version 4</td><td>Foreign Travel</td></tr>
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

public class NbaACForeignTravelProcessor extends NbaVpmsModelProcessor {

	//Impairments generated by this class will be stored in this instance. 
	//Such an instance will be in each class that invokes the model.
	protected ArrayList foreignTravelImpairments =new ArrayList();//ACN024
	protected ArrayList accepImpairments = null;	
	
	/**
	 * Overridden method, calls the model and 
	 * updates the contract with impairments.
	 * @throws NbaBaseException
	 */
	public void execute() throws NbaBaseException {
		boolean isSuccess = false;
		impSrc = NbaConstants.FOREIGNTRAVEL_SRC;//ACN016
		if(performingContractEvaluation()){	//SPR2652
			int partyIndex = 0;
			List al = getAllInsuredIndexes(); // SPR3290
			OLifE oLifE = nbaTxLife.getOLifE();
			// SPR3290 code deleted
			int insuredCount = al.size();
			foreignTravelImpairments = new ArrayList();//ACP022	
			for(int i=0;i<insuredCount;i++){
				isSuccess = false;
				partyIndex = ((Integer)al.get(i)).intValue();
				String partyId = oLifE.getPartyAt(partyIndex).getId();
				partyID = partyId;							
				// SPR3290 code deleted
				foreignTravelImpairments.clear();
				isSuccess = callForeignTravelModel(i);
				if (!isSuccess){
					throw new NbaVpmsException(NbaVpmsException.VPMS_RESULTS_ERROR + NbaVpmsAdaptor.AC_FOREIGNTRAVEL);	//SPR2652
				}
				getContractImpairments(partyId);
				ArrayList[] mergedLists = mergeImpairments(contractImpairments, foreignTravelImpairments, new ArrayList(), new ArrayList());
				ArrayList arrMerged = mergedLists[0];								
				addImpairmentInfo(partyId, arrMerged);	

			}
		} else if (performingRequirementsEvaluation()) { //SPR2652
			setPartyID(work); //ACN024
			isSuccess = callForeignTravelModel();
			if (!isSuccess) {
				throw new NbaVpmsException(NbaVpmsException.VPMS_RESULTS_ERROR + NbaVpmsAdaptor.AC_FOREIGNTRAVEL);	//SPR2652
			}
			   //Do the Impairments Merging //ACN024
			  mergeImpairmentsAndAccep(foreignTravelImpairments,accepImpairments); //ACN016							
		}
	}
	
	/**
	 * This method is used to call the AcForeignTravel model.
	 * @param 
	 * @return boolean : Returns true if the call is successful
	 * 					 Else returns false 	
	 * @throws NbaBaseException
	 */	
	//ACP022 new method.
	public boolean callForeignTravelModel(int partyIndex) throws NbaBaseException{
		boolean success = true;
		ArrayList results = null;
		NbaVpmsAdaptor vpmsProxy = null; //SPR3362
		try{		 
			NbaOinkDataAccess accessContract = new NbaOinkDataAccess(nbaTxLife);
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			accessContract.setLobSource(work.getNbaLob());
			accessContract.setAcdbSource(new NbaAcdb(), nbaTxLife);
			vpmsProxy = new NbaVpmsAdaptor(accessContract, NbaVpmsAdaptor.AC_FOREIGNTRAVEL); //SPR3362
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_RESULTXML);
			vpmsProxy.setANbaOinkRequest(oinkRequest);
			Map deOink = getForeignTravelDeOINKValues(partyIndex);
			vpmsProxy.setSkipAttributesMap(deOink);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
			vpmsResultsData.displayResultsData();
			results = vpmsResultsData.getResultsData();
			
			if (results == null) {
				//SPR3362 code deleted
				throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.AC_FOREIGNTRAVEL); //SPR2652			
			}
			//SPR2652
			vpmsResult = (String)results.get(0);
			NbaVpmsModelResult vpmsOutput = new NbaVpmsModelResult(vpmsResult);
			if (getLogger().isDebugEnabled()) { 
				getLogger().logDebug(vpmsResult);
			}
			VpmsModelResult vpmModelResult = vpmsOutput.getVpmsModelResult();
			foreignTravelImpairments = vpmModelResult.getImpairmentInfo();
			success = true;
			// SPR2652 Code Deleted
			//SP3362 code deleted
		}
		// SPR2652 Code Deleted
		catch(RemoteException e){	//SPR2652 
			handleRemoteException(e, NbaVpmsAdaptor.AC_FOREIGNTRAVEL); //SPR2652 
		//begin SPR3362
		} finally {
		    if(vpmsProxy != null){
		        try {
		            vpmsProxy.remove();
                } catch (Exception e) {
                    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
                }
		    }
		//end SPR3362
		}		
		return success;
	}
	
	/**
	 * This method gets all the deOink variables for ACForeignTravel model	 
	 * @param 
	 * @return java.util.Map : The Hash Map containing all the deOink variables 	
	 * @throws NbaBaseException
	 */
	//ACP022 new method.
	public HashMap getForeignTravelDeOINKValues(int partyIndex) throws NbaBaseException{
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(nbaTxLife);
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		HashMap deOink = new HashMap();	//SPR2652
		if (updatePartyFilterInRequest(oinkRequest, partyID)) { //SPR2652
			String strValue = "";
			String[] country = null;
			String[] maxTime = null;
			strValue = "TravelCountry_INS";
			// SPR2652 code deleted
			oinkRequest.setVariable(strValue);
			country = oinkData.getStringValuesFor(oinkRequest);
			strValue = "TravelMaxTime_INS";
			// SPR2652 code deleted
			oinkRequest.setVariable(strValue);
			maxTime = oinkData.getStringValuesFor(oinkRequest);

			int countCountry = country.length;
			// SPR3290 code deleted

			// SPR2652 code deleted
			deOink.put("A_NO_OF_TRAVELCOUNTRY", String.valueOf(countCountry));

			for (int i = 0; i < countCountry; i++) {
				if (i == 0) {
					deOink.put("A_TRAVELCOUNTRY", country[i]);
					deOink.put("A_TRAVELMAXTIME", maxTime[i]);
				} else {
					deOink.put("A_TRAVELCOUNTRY[" + i + "]", country[i]);
					deOink.put("A_TRAVELMAXTIME[" + i + "]", maxTime[i]);
				}
			}
		}
		return deOink;
	}
	
	/**
	* This method is used to call the AcForeignTravel model.
	* @param 
	* @return boolean : Returns true if the call is successful
	* 					 Else returns false 	
	* @throws NbaBaseException
	*/
	//ACP022 new method.
	public boolean callForeignTravelModel() throws NbaBaseException {
		boolean success = false;
		ArrayList tempImpairmentList = null;
		ArrayList results = null;
		NbaVpmsAdaptor vpmsProxy = null; //SPR3362
		try {
			NbaOinkDataAccess accessContract = new NbaOinkDataAccess(txLifeReqResult); //ACN009
			accessContract.setLobSource(work.getNbaLob());
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			accessContract.setAcdbSource(new NbaAcdb(), nbaTxLife);
			vpmsProxy = new NbaVpmsAdaptor(accessContract, NbaVpmsAdaptor.AC_FOREIGNTRAVEL); //SPR3362
			vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_RESULTXML);
			vpmsProxy.setANbaOinkRequest(oinkRequest);
			Map deOink = getForeignTravelDeOINKValues();
			vpmsProxy.setSkipAttributesMap(deOink);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
			vpmsResultsData.displayResultsData();
			results = vpmsResultsData.getResultsData();

			if (results == null) {
				//SPR3362 code deleted
				throw new NbaVpmsException(NbaVpmsException.VPMS_NO_RESULTS + NbaVpmsAdaptor.AC_FOREIGNTRAVEL); //SPR2652
			} //SPR2652 
			vpmsResult = (String) results.get(0);
			NbaVpmsModelResult vpmsOutput = new NbaVpmsModelResult(vpmsResult);
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug(vpmsResult);
			}
			VpmsModelResult vpmsModelResult = vpmsOutput.getVpmsModelResult();
			foreignTravelImpairments = vpmsModelResult.getImpairmentInfo(); //ACN024
			if (tempImpairmentList != null && tempImpairmentList.size() != 0) {
				//listImpairmentInfo.addAll(tempImpairmentList); //ACN024
			}
			success = true;
			// SPR2652 code deleted
			//SPR3362 code deleted
			// SPR2652 code deleted
		} catch (RemoteException e) {	//SPR2652
			handleRemoteException(e, NbaVpmsAdaptor.AC_FOREIGNTRAVEL); //SPR2652
		//begin SPR3362
		} finally {
		    if(vpmsProxy != null){
		        try {
		            vpmsProxy.remove();
                } catch (Exception e) {
                    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
                }
		    }
		//end SPR3362
		}
		return success;
	}
	
	/**
	 * This method gets all the deOink variables for ACForeignTravel model	 
	 * @param 
	 * @return java.util.Map : The Hash Map containing all the deOink variables 	
	 * @throws NbaBaseException
	 */
	//ACP022 new method.
	public HashMap getForeignTravelDeOINKValues() throws NbaBaseException {
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess(txLifeReqResult); //ACN009
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		String strValue = "";
		String[] country = null;
		String[] maxTime = null;
		strValue = "TravelCountry_INS";

		oinkRequest.setVariable(strValue);
		country = oinkData.getStringValuesFor(oinkRequest);
		strValue = "TravelMaxTime_INS";
		oinkRequest.setVariable(strValue);
		maxTime = oinkData.getStringValuesFor(oinkRequest);

		int countCountry = country.length;
		// SPR3290 code deleted

		HashMap deOink = new HashMap();
		deOink.put("A_NO_OF_TRAVELCOUNTRY", String.valueOf(countCountry));
		// SPR3290 code deleted
		for (int i = 0; i < countCountry; i++) {
			if (i == 0) {
				deOink.put("A_TRAVELCOUNTRY", country[i]);
				deOink.put("A_TRAVELMAXTIME", maxTime[i]);
			} else {
				deOink.put("A_TRAVELCOUNTRY[" + i + "]", country[i]);
				deOink.put("A_TRAVELMAXTIME[" + i + "]", maxTime[i]);
			}
		}
		return deOink;
	}	
}
