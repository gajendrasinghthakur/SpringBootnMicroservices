package com.csc.fsg.nba.business.process;

/*
 * *******************************************************************************<BR>
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
 * 
 * *******************************************************************************<BR>
 */

import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkDefaultFormatter;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataStoreModeNotFoundException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaCase;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.StandardAttr;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * This class calls the presubmit Validate model to check for default values 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA187</td><td>Version 7</td><td>nbA Trial Application</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>  
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>AXAL3.7.03</td><td>AXA Life Phase 1</td><td>Informals</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */

public class NbaXML103SetDefaultValues implements NbaTableAccessConstants {

    protected NbaTXLife objNbaTXLife = null;
    protected NbaCase aCase = null; 
    protected NbaUserVO userVO = null; 
    protected NbaDst nbaDst = null;
    /**
     * NbaXML103Validation constructor.
     */
    public NbaXML103SetDefaultValues() {
        super();
    }

    /**
     * Return NbaCase object
     * 
     * @return NbaCase
     */

    protected NbaCase getCase() {
        return aCase;
    }

    /**
     * Gets NbaTXLife object
     * 
     * @return com.csc.fsg.nba.vo.NbaTXLife
     */
    protected NbaTXLife getNbaTXLife() {
        return objNbaTXLife;
    }

    /**
     * This method does the initialization of member objects. Since the member objects are used in other methods, the initialization needs to be done
     * before any other methods are called.
     * 
     * @exception com.csc.fsg.nba.exception.NbaBaseException
     */

    protected void initializeData(NbaDst nbaDst) throws NbaBaseException {
        setCase(nbaDst.getNbaCase());
        setNbaTXLife(getCase().getXML103Source());
        setUserVO(nbaDst.getNbaUserVO());
        setNbaDst(nbaDst);
    }

    /**
     * Set NbaCase
     * 
     * @param newACase
     */

    protected void setCase(NbaCase newACase) {
        aCase = newACase;
    }

    /**
     * Set NbaTXLife object to the member variable.
     * 
     * @param aNbaTXLife The instance of XML103
     */
    protected void setNbaTXLife(NbaTXLife aNbaTXLife) {
        if (objNbaTXLife == null)
            objNbaTXLife = aNbaTXLife;
    }

    /**
     * Entry to set the presubmit default values
     * 
     * @param lobData Lob data which from which OINK auto populated the input vpms values
     * @param deOinkMap map containing overrided or non lob data
     * @param entryPoint Entry point which needs to be called
     * @return VpmsModelResult result
     * @throws NbaBaseException
     */

    public NbaDst setPresubmitDefaultValues(NbaDst nbaDst) throws NbaBaseException {

        initializeData(nbaDst);
        setDefaultValues();
        return nbaDst;
    }

    /**
     * Call the VP/MS model for presubmit validation defaults
     * @throws NbaVpmsException
     */
    protected void setDefaultValues() throws NbaBaseException, NbaVpmsException {
        NbaVpmsAdaptor vpmsProxy = null;//SPR3362
        try {
            NbaOinkDataAccess oinkDataAccess = new NbaOinkDataAccess(getNbaTXLife());
            //begin AXAL3.7.03
            if (getCase().getApplicationSource() != null) {
            	oinkDataAccess.setLobSource(getCase().getApplicationSource().getNbaLob());
            }
            //end AXAL3.7.03
            Map deOink = new HashMap();
            deOink.put("A_OperatingModeLOB", getDataStoreModeFromVpms());
            vpmsProxy = new NbaVpmsAdaptor(oinkDataAccess, NbaVpmsAdaptor.PRESUBMITVALIDATION); //SPR3362
            vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_PRESUBMIT_APP_DEFAULTS);
            vpmsProxy.setSkipAttributesMap(deOink);

            // get the string out of XML returned by VP / MS Model and parse it to create the object structure
            NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
            //if all LOBs have values return null
            if (vpmsResultsData.getResultsData() == null) {
                return;
            }
            String xmlString = (String) vpmsResultsData.getResultsData().get(0);
            NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
            if (nbaVpmsModelResult.getVpmsModelResult() != null) {
                setDefaultInformal(nbaVpmsModelResult.getVpmsModelResult());
            }
            //SPR3362 code deleted

        } catch (RemoteException t) {
            throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
        //begin SPR3362
        } finally {
               try {
                   if (vpmsProxy != null) {
                       vpmsProxy.remove();
                   }
               } catch (Throwable th) {
                   LogHandler.Factory.LogError(this,  NbaBaseException.VPMS_REMOVAL_FAILED);
               }
        }
        //end SPR3362
    }

    /**
     * Store the default values
     * 
     * @param sourceList sources supported for this work indexing
     * @param sourceType current source type
     * @return boolean true if passed source is suppported false otherwise
     */
    protected void setDefaultInformal(VpmsModelResult vpmsModelResult) throws NbaBaseException {
        List strAttrs = vpmsModelResult.getStandardAttr();
        //Generate delimited string if there are more than one parameters returned
        Iterator itr = strAttrs.iterator();
        NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess();
        nbaOinkDataAccess.setContractDest(getNbaTXLife());
        while (itr.hasNext()) {
            NbaOinkRequest aNbaOinkRequest = new NbaOinkRequest();
            StandardAttr standardAttr = (StandardAttr) itr.next();
            aNbaOinkRequest.setVariable(standardAttr.getAttrName().substring(7));
            aNbaOinkRequest.setValue(checkForDate(standardAttr.getAttrValue()));
            nbaOinkDataAccess.updateValue(aNbaOinkRequest);
        }
        checkProductCode();
        getCase().updateXML103Source(getNbaTXLife());
        getCase().updateLobFromNbaTxLife(getNbaTXLife());
    }

    /**
     * This method calls a VP/MS model that returns 'S' if datastore mode is stand alone or 'W' if datastore mode is wrappered.
     * 
     * @param lob the lob fields
     * @return data store source from VP/MS model
     */
    protected String getDataStoreModeFromVpms() throws NbaBaseException {
        NbaVpmsAdaptor vpmsProxy = null;
        try {
            NbaLob nbaLob = getCase().getNbaLob();
            if (nbaLob.getPortalCreated()) {
                nbaLob.updateLobFromNbaTxLife(getNbaTXLife()); //make sure we have LOBs populated from the XML103
                nbaLob.setBackendSystem(getBackEndSystemId());
            }
            NbaOinkDataAccess oinkData = new NbaOinkDataAccess(nbaLob);
            oinkData.getFormatter().setDateSeparator(NbaOinkDefaultFormatter.DATE_SEPARATOR_DASH);
            vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.DETERMINE_DATA_STORE_MODE);
            vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_PRESUBMIT_DATASTOREMODE);
            NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
            if (data.wasSuccessful()) {
                if (data.getResultsData() != null && data.getResultsData().size() > 0) {
                    return (String) data.getResultsData().get(0);
                }
            }
            throw new NbaDataStoreModeNotFoundException("DataStore not found for Backend, Plan Or SignedDate");	//ALS4055 exception messaging.
        } catch( NbaDataStoreModeNotFoundException e ){		
        	throw e ;
        } catch (Throwable th) {
            throw new NbaBaseException("Problem retrieving PreSubmit Data Store Mode from VP/MS", th);
        } finally {
            try {
                if (vpmsProxy != null) {
                    vpmsProxy.remove();
                }
            } catch (Throwable th) {
                LogHandler.Factory.LogError(this,  NbaBaseException.VPMS_REMOVAL_FAILED); //SPR3362
            }
        }
    }
    /**
     * Check to see if today's date needs to be calculated for the default
     * 
     * @param lobData Lob data which from which OINK auto populated the input vpms values
     * @param deOinkMap map containing overrided or non lob data
     * @param entryPoint Entry point which needs to be called
     * @return VpmsModelResult result
     * @throws NbaBaseException
     */
    protected String checkForDate(String value) throws NbaBaseException {
        if ("todayDate".equals(value)) {
            Result result = ServiceHandler.invoke("NbaRetrieveTimeStampBP", ServiceContext.currentContext(), getUserVO());
            if (!result.hasErrors()) {
                NbaDst nbADstObj = (NbaDst) result.getFirst();
                if (nbADstObj != null) {
                	//NBA208-32
                    Date date = NbaUtils.getDateFromStringInAWDFormat(nbADstObj.getTimestamp());
                    return NbaUtils.getStringInUSFormatFromDate(date);
                }
            } 
            return "";
        }         
        return value;
    }
    /**
	 * Set the user value object
     * @return Returns the userVO.
     */
    protected NbaUserVO getUserVO() {
        return userVO;
    }
    /**
     * Return the user value object
     * @param userVO The userVO to set.
     */
    protected void setUserVO(NbaUserVO userVO) {
        this.userVO = userVO;
    }
    /**
     * Reset the policy.productCode if different than the base coverage due to default value
     */
    protected void checkProductCode(){
    	Coverage baseCoverage =  getNbaTXLife().getPrimaryCoverage();
    	if  (baseCoverage != null) {
        	String baseProductCode = baseCoverage.getProductCode();
        	if (baseProductCode != null && !baseProductCode.equals(getNbaTXLife().getProductCode())) {
            	getNbaTXLife().getPolicy().setProductCode(baseProductCode);
            }
        }
    }
    /**
     * Get the back end system id based on company and plan.
     * @return returns the back end system id
     */
    protected String getBackEndSystemId() throws NbaBaseException {
        NbaTableAccessor nta = new NbaTableAccessor();
        Map myCaseMap = nta.setupTableMap(getNbaDst());
        String strPlan = null;
        String strCompany = null;
        Policy policy = getNbaTXLife().getPrimaryHolding().getPolicy();
        if (policy != null) {
            strPlan = policy.getProductCode();
        	strCompany = policy.getCarrierCode();
        } 	
        if (strPlan != null && strCompany != null) {
            myCaseMap.put("company", strCompany);
            myCaseMap.put("plan", strPlan);
            NbaPlansData planData = nta.getPlanData(myCaseMap);
            if (planData != null) {
                return planData.getSystemId();
            }
        }
        return "";

    }
    /**
     * Get the nbaDst
     * @return Returns the nbaDst.
     */
    protected NbaDst getNbaDst() {
        return nbaDst;
    }
    /**
     * Set the nbaDst
     * @param nbaDst The nbaDst to set.
     */
    protected void setNbaDst(NbaDst nbaDst) {
        this.nbaDst = nbaDst;
    }
}
