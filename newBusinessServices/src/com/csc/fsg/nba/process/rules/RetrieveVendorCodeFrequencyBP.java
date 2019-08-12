package com.csc.fsg.nba.process.rules;

/* 
 * *******************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.business.transaction.NbaRequirementUtils;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.tableaccess.NbaRequirementsData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;

/**
 * Retrieves the default vendor code and followup frequency for given requirement type.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA138</td><td>Version 6</td><td>Override Requirements Settings Project</td></tr>
 * <tr><td>NBA192</td><td>Version 7</td><td>Requirement Management Enhancement</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>AXAL3.7.06</td><td>AxaLife Phase 1</td><td>Requirement Management</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 extends NewBusinessAccelBP
public class RetrieveVendorCodeFrequencyBP extends NewBusinessAccelBP {

    /**
     * Called to retrieve the default vendor code and followup frequency for given requirement type.
     * @param an array list object containing following
     * 	- Requirement type
     *  - an instance of NbaLob
     *  - Contract's holding inquiry 
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            if (input instanceof List) {
                List inputList = (List) input;
                //Begin ALS3573 
                String reqType = (String) inputList.get(0);
                String reqStatus = (String)inputList.get(1);
                NbaLob nbaLob = (NbaLob) inputList.get(2);
                NbaTXLife holdingInq = (NbaTXLife) inputList.get(3);
                //end ALS3573 
                String model = null;
                String entryPoint = null;
                //vendor code model and entry point details are cached in utils class to cater later calls. 
                if (NbaRequirementUtils.getReqVendorModel() != null) {
                    model = NbaRequirementUtils.getReqVendorModel();
                    entryPoint = NbaRequirementUtils.getReqVendorEntryPoint();
                } else {
                    model = getVpmsDataFromRulesControl(NbaVpmsAdaptor.EP_GET_MODEL, holdingInq);
                    entryPoint = getVpmsDataFromRulesControl(NbaVpmsAdaptor.EP_GET_ENTRY_POINT, holdingInq);
                    NbaRequirementUtils.setReqVendorModel(model);
                    NbaRequirementUtils.setReqVendorEntryPoint(entryPoint);
                }
                Map deoinkMap = new HashMap(5,(float)0.9); //SPR3290 //ALS3573
                List resultList = new ArrayList();
                deoinkMap.put(NbaVpmsConstants.A_REQ_CODE, reqType);
                deoinkMap.put(NbaVpmsConstants.A_REQ_TYPE_LOB, reqType);
                deoinkMap.put(NbaVpmsConstants.A_ReqStatusLOB, reqStatus);//ALS3573 
                VpmsComputeResult aResult = getDataFromVpms(model, entryPoint, deoinkMap, holdingInq, nbaLob);
                String vendor = aResult.getResult();
                //add vendor code at first position in response arraylist
                resultList.add(vendor);
                deoinkMap.clear(); //SPR3290
                deoinkMap.put(NbaVpmsConstants.A_REQ_TYPE_LOB, reqType);
                deoinkMap.put(NbaVpmsConstants.A_REQ_VENDOR_LOB, vendor);
                deoinkMap.put(NbaVpmsConstants.A_ReqStatusLOB, reqStatus);//ALS3573
                aResult = getDataFromVpms(NbaVpmsConstants.REQUIREMENTS, NbaVpmsConstants.EP_GET_FOLLOWUP_DAYS, deoinkMap, holdingInq, nbaLob);
                //add follow up frequency at second position in response arraylist
                resultList.add(aResult.getResult());
                //add follow up provider at third position in response arraylist
                resultList.add(getFollowUpVendor(nbaLob, holdingInq, deoinkMap)); //NBA192
                //Begin AXAL3.7.06
                deoinkMap.clear();
                deoinkMap.put(NbaVpmsConstants.A_REQ_TYPE_LOB, reqType);  
                deoinkMap.put(NbaVpmsConstants.A_ReqStatusLOB, reqStatus);//ALS3573
                NbaRequirementUtils.deOinkEndorsementValues(deoinkMap, holdingInq);//ALS4322
                NbaTableAccessor tableAccessor = new NbaTableAccessor();
                NbaRequirementsData requirementsData = (NbaRequirementsData) tableAccessor.getDataForOlifeValue(tableAccessor.setupTableMap(nbaLob),
        				NbaTableConstants.NBA_REQUIREMENTS, String.valueOf(reqType));
        		if (requirementsData == null) {
        			throw new NbaDataAccessException("No data found for requirement " + reqType);
        		}
        	    aResult = getDataFromVpms(NbaVpmsConstants.REQUIREMENTS, NbaVpmsConstants.EP_REQ_OVERRIDE_SETTINGS, deoinkMap, holdingInq, nbaLob);
                NbaVpmsModelResult nbaVpmsModelReqOverrideResult = new NbaVpmsModelResult(aResult.getResult());
                if (nbaVpmsModelReqOverrideResult.getVpmsModelResult() != null) {
        			RequirementInfo overrideReqInfo = nbaVpmsModelReqOverrideResult.getVpmsModelResult().getRequirementInfoAt(0);
        			if (overrideReqInfo.hasRestrictIssueCode()) {
        				resultList.add(String.valueOf(overrideReqInfo.getRestrictIssueCode()));
        			} else {
        				resultList.add(requirementsData.getRestrictionCode());
        			}
        			RequirementInfoExtension overrideReqInfoExtn = overrideReqInfo.getOLifEExtensionAt(0).getRequirementInfoExtension();
        			if (overrideReqInfoExtn.hasMedicalIndicator()) {
        				resultList.add(String.valueOf(overrideReqInfoExtn.getMedicalIndicator()));
        			} else {
        				resultList.add(String.valueOf(requirementsData.getMedicalTypeIndicator() == 1 ? true : false));
        			}
        			if (overrideReqInfoExtn.hasReviewCode()) {
        				resultList.add(String.valueOf(overrideReqInfoExtn.getReviewCode()));
        			} else {
        				resultList.add(Long.toString(requirementsData.getReviewIndicator()));
        			}
        		}                       
                //End AXAL3.7.06                
                result.addResult(resultList);
            }
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

    /**
     * Retreives the follow-up vendor ID from requirement VP/MS model.
     * @param nbaLob the Nba Lob
     * @param holdingInq the holding inquiry
     * @param deoinkMap the de-oink map
     * @return the follow-up vendor ID
     * @throws NbaBaseException
     */
    //NBA192 New Method
    protected String getFollowUpVendor(NbaLob nbaLob, NbaTXLife holdingInq, Map deoinkMap) throws NbaBaseException {
        String followUpVendor = "";
        VpmsComputeResult aResult = getDataFromVpms(NbaVpmsConstants.REQUIREMENTS, NbaVpmsConstants.EP_GET_FOLLOWUP_PROVIDER, deoinkMap, holdingInq,
                nbaLob);
        NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(aResult.getResult());
        if (nbaVpmsModelResult.getVpmsModelResult() != null && nbaVpmsModelResult.getVpmsModelResult().getResultCount() > 0) {
            followUpVendor = nbaVpmsModelResult.getVpmsModelResult().getResultAt(0);
        }
        return followUpVendor;
    }

    /**
     * Create and initialize an <code>NbaVpmsAutoUnderwritingData</code> object to find matching work items.
     * @param entryPoint the VP/MS model's entry point
     * @return NbaVpmsAutoUnderwritingData the VP/MS results
     * @throws NbaBaseException
     */
    protected VpmsComputeResult getDataFromVpms(String model, String entryPoint, Map deoinkMap, NbaTXLife holdingInq, NbaLob nbaLob)
            throws NbaBaseException {
        NbaVpmsAdaptor vpmsAdaptor = null; //SPR3362
        try {
            NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
            if (deoinkMap == null) {
                deoinkMap = new HashMap(2, 0.9f);
            }
            if (holdingInq != null) {
                oinkData.setContractSource(holdingInq);
            }
            if (nbaLob != null) {
                oinkData.setLobSource(nbaLob);
            }
            deoinkMap.put(NbaVpmsConstants.A_PROCESS_ID, holdingInq.getBusinessProcess());
            vpmsAdaptor = new NbaVpmsAdaptor(oinkData, model); //SPR3362
            vpmsAdaptor.setVpmsEntryPoint(entryPoint);
            vpmsAdaptor.setSkipAttributesMap(deoinkMap);
            return vpmsAdaptor.getResults();
        } catch (java.rmi.RemoteException re) {
            throw new NbaBaseException("Problem retrieving requirement's default vendor/frequency ", re);
        //begin SPR3362
        } finally {
                try {
                    if (vpmsAdaptor != null) {
                        vpmsAdaptor.remove();
                    }
                } catch (Throwable th) {
                    LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
                }
            }
        //end SPR3362
    }
    
    /** 
     * This method calls the NbaRulesControlModel to determine the
     * target VPMS model node to be called for the given process and model name.
     * @return String containing the VPMS model node name to execute
     * @throws NbaBaseException
     */
    public String getVpmsDataFromRulesControl(String entryPoint, NbaTXLife holdingInq) throws NbaBaseException {
        HashMap deoinkMap = new HashMap();
        deoinkMap.put(NbaVpmsAdaptor.A_INSTALLATION, String.valueOf(NbaConfiguration.getInstance().isAcNba()));
        VpmsComputeResult aResult = getDataFromVpms(NbaVpmsConstants.NBARULESCONTROLMODEL, entryPoint, deoinkMap, holdingInq, null);
        NbaVpmsModelResult modresult = new NbaVpmsModelResult(aResult.getResult());
        if (NbaVpmsConstants.EP_GET_MODEL.equalsIgnoreCase(entryPoint)) {
            return modresult.getModelName();
        } else {
            return modresult.getVpmsModelResult().getResultAt(0);
        }

    }
}
