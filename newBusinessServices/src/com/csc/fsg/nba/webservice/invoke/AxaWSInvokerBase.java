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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */
package com.csc.fsg.nba.webservice.invoke;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.business.transaction.NbaAxaServiceResponse;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTransOliCode;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.CarrierAppointmentExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.SourceInfo;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.webservice.client.NbaAxaServiceRequestor;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapter;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapterFactory;

/**
 * This class is an abstract class that implements the AxaWSInvoker
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>AXAL3.7.18</td>
 * <td>Version 7</td>
 * <td>Producer Interface</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public abstract class AxaWSInvokerBase implements AxaWSInvoker, AxaWSConstants {

    private NbaUserVO userVO;

    private NbaTXLife nbaTXLife;

    private NbaDst nbaDst;

    private Object object;

    private String backEnd;

    private String category;

    private String functionId;

    private String operation;

    private NbaTXLife nbaTxLifeRequest;
    
    private Object webserviceResponse;

    private static final String minFaceAmount100KAgentCodes[] = {"362903"}; // NBLXA-2553 
    
    /**
     *  
     */
    private AxaWSInvokerBase() {
        super();

    }

    /**
     * @param userVO
     * @param nbaTXLife
     * @param nbaDst
     * @param object
     */
    public AxaWSInvokerBase(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
        super();
        setOperation(operation);
        setUserVO(userVO);
        setNbaTXLife(nbaTXLife);
        setNbaDst(nbaDst);
        setObject(object);
    }

    /**
     * @return
     * @throws NbaBaseException
     */
    public abstract Object createRequest() throws NbaBaseException; //NBLXA-2402(NBLXA-2476)

    /**
	 * This method would check whether a call is to be made to the web service.Should be overridden for some specefic implementation.
	 * @param txLife203
	 * @return
	 * @throws NbaBaseException
	 */
    //AXAL3.7.22 new method
    protected boolean isCallNeeded(){
		return true; // should not be changed. If required override.
	}
    
    /**
     * Validate txLife request and return true as default
     * Need to override this method for specific validate implementation.
     * @param txLife203
     * @return
     * @throws NbaBaseException
     */
    protected boolean validate() throws NbaBaseException {//AXAL3.7.22 method signature changed
		return true; // should not be changed. If required override.
	}

	/**
	 * Impletemented method which validate txLife request and perform other processing.
	 * @return TxLife as response
	 */
    //ALII677 redone
	public Object execute() throws NbaBaseException {
		if (isCallNeeded()) {//AXAL3.7.22
			nbaTxLifeRequest = (NbaTXLife) createRequest(); //NBLXA-2402(NBLXA-2476)
			if (validate()) {
				cleanRequest();
				invoke();
				handleResponse();
			}
		}
		return webserviceResponse;
	}

    /**
	 * Crealte a general TxLife request object
	 * 
	 * @param transType
	 * @param transSubType
	 * @param businessUser
	 * @return
	 */
    protected NbaTXLife createTXLifeRequest(long transType, long transSubType, String businessUser) {
        NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
        nbaTXRequest.setTransType(transType);
        nbaTXRequest.setTransSubType(transSubType);
        nbaTXRequest.setBusinessProcess(businessUser);
        nbaTXRequest.setNbaUser(getUserVO());
        NbaTXLife txLife = new NbaTXLife(nbaTXRequest);
        return txLife;
    }

    /**
     * Creat SourceInfo object
     * 
     * @return
     */

    protected SourceInfo createSoureInfo() {
        SourceInfo sourceInfo = new SourceInfo();
        sourceInfo.setSourceInfoName(SOURCEINFO);
        sourceInfo.setFileControlID(getBackEnd());	//P2AXAL008
        return sourceInfo;
    }

    /**
     * @return
     * @throws NbaBaseException
     */
    public void invoke() throws NbaBaseException {

        NbaWebServiceAdapter service = NbaWebServiceAdapterFactory.createWebServiceAdapter(getBackEnd(), getCategory(), getFunctionId());
        Map params = new HashMap();
        params.put(NbaAxaServiceRequestor.PARAM_SERVICEOPERATION, getOperation());
        params.put(NbaAxaServiceRequestor.PARAM_NBATXLIFE, getNbaTxLifeRequest().toXmlString());
        if (userVO != null && userVO.getToken() != null) {
            params.put(NbaAxaServiceRequestor.PARAM_TOKEN, userVO.getToken());
        }
        params.put(NbaAxaServiceRequestor.PARAM_UDDIKEY, "ToBeDetermined");
        Map results = null;
        
        try {//This try catch should be removed and the below two loggers should be moved to execute() method once all the code from NbaAxaServiceRequester
			 // is moved into this class.
        	results = service.invokeAxaWebService(params);
        } catch (NbaBaseException nbe){
        	
        	throw nbe;//AXAL3.7.22
        } finally {
        	if (getLogger().isDebugEnabled()) {
            	getLogger().logDebug("Axa webservice request for  " + getOperation() + ":" + params.get(NbaAxaServiceRequestor.PARAM_SOAPENVELOP));
            }
        }
        
        if(!NbaUtils.isBlankOrNull(results.get(NbaAxaServiceResponse.ERRORMSG_ELEMENT))) {
            String errorText = (String) results.get(NbaAxaServiceResponse.ERRORMSG_ELEMENT);
            getLogger().logError("Error Messages in Response for " + getOperation() + ":" + errorText);
            throw new NbaBaseException("Failure response for " + getOperation() + ": " + errorText); //APSL3874

        } 
        
        setWebserviceResponse(results.get(NbaAxaServiceResponse.NBATXLIFE_ELEMENT));
        if (getLogger().isDebugEnabled()) {
        	getLogger().logDebug("Axa webservice response for " + getOperation() + ":" + ((NbaTXLife)getWebserviceResponse()).toXmlString());
        }
    }

    /**
     * @param nbaTXLife
     * @throws NbaBaseException
     */
    protected void handleResponse() throws NbaBaseException {
        TransResult transResult = ((NbaTXLife) webserviceResponse).getTransResult();
        if (transResult != null && transResult.getResultCode() != NbaOliConstants.TC_RESCODE_SUCCESS) {
            StringBuffer errorString = new StringBuffer();
            List resultInfoList = transResult.getResultInfo();
            if (resultInfoList != null && resultInfoList.size() > 0) {
                for (int i = 0; i < resultInfoList.size(); i++) {
                    ResultInfo resultInfo = (ResultInfo) resultInfoList.get(i);
                    if(i>0)
                        errorString.append(" Error count : " + i );
                    long resultInfoCode = resultInfo.getResultInfoCode();
                    if (!NbaUtils.isBlankOrNull(resultInfoCode))
                        errorString.append(" Error Code : (" + resultInfoCode + ") "
                                + NbaTransOliCode.lookupText(NbaOliConstants.RESULT_INFO_CODES, resultInfoCode) + "\n");
                    if (!NbaUtils.isBlankOrNull(resultInfo.getResultInfoDesc()))
                        errorString.append(" Error Desc : " + resultInfo.getResultInfoDesc());

                    errorString.append("\n");
                }
                getLogger().logError("Error Messages in Response for " + getOperation() + ":" + errorString.toString());
                throw new NbaBaseException("Failure response for " + getOperation() + ": " + errorString.toString()); //APSL3874
            }
        }

    }

    /**
     *  
     */
    protected void cleanRequest() throws NbaBaseException { //APSL371 APSL372
        NbaUtils.prepareCoverageForSubStandardRating(getNbaTxLifeRequest()); //ALS2688
    }

    /**
     * @return Returns the nbaTXLife.
     */
    public NbaTXLife getNbaTXLife() {
        return nbaTXLife;
    }

    /**
     * @param nbaTXLife
     *            The nbaTXLife to set.
     */
    public void setNbaTXLife(NbaTXLife nbaTXLife) {
        this.nbaTXLife = nbaTXLife;
    }

    /**
     * @return Returns the userVO.
     */
    public NbaUserVO getUserVO() {
        return userVO;
    }

    /**
     * @param userVO
     *            The userVO to set.
     */
    public void setUserVO(NbaUserVO userVO) {
        this.userVO = userVO;
    }

    /**
     * @return Returns the nbaDst.
     */
    public NbaDst getNbaDst() {
        return nbaDst;
    }

    /**
     * @param nbaDst
     *            The nbaDst to set.
     */
    public void setNbaDst(NbaDst nbaDst) {
        this.nbaDst = nbaDst;
    }

    /**
     * @return Returns the backEnd.
     */
    //P2AXAL008 Refactored
    public String getBackEnd() {
		if (nbaTXLife != null && !NbaUtils.isBlankOrNull(nbaTXLife.getBackendSystem())) {
			return nbaTXLife.getBackendSystem();
		} else if (nbaDst != null && !NbaUtils.isBlankOrNull(nbaDst.getNbaLob().getBackendSystem())) {
			return nbaDst.getNbaLob().getBackendSystem();
		}
		//Needs to be revisited if wrong backend
		return backEnd;
	}

    /**
	 * @param backEnd The backEnd to set.
	 */
    public void setBackEnd(String backEnd) {
        this.backEnd = backEnd;
    }

    /**
     * @return Returns the category.
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category
     *            The category to set.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return Returns the functionId.
     */
    public String getFunctionId() {
        return functionId;
    }

    /**
     * @param functionId
     *            The functionId to set.
     */
    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    /**
     * @return Returns the operation.
     */
    public String getOperation() {
        return operation;
    }

    /**
     * @param operation
     *            The operation to set.
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * @return Returns the object.
     */
    public Object getObject() {
        return object;
    }

    /**
     * @param object
     *            The object to set.
     */
    public void setObject(Object object) {
        this.object = object;
    }



    /**
     * @return Returns the nbaTxLifeRequest.
     */
    public NbaTXLife getNbaTxLifeRequest() {
        return nbaTxLifeRequest;
    }

    /**
     * @param nbaTxLifeRequest
     *            The nbaTxLifeRequest to set.
     */
    public void setNbaTxLifeRequest(NbaTXLife nbaTxLifeRequest) {
        this.nbaTxLifeRequest = nbaTxLifeRequest;
    }

    /**
     * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
     * 
     * @return the logger implementation
     */
    protected  NbaLogger getLogger() {
        NbaLogger logger =null;
        try {
            logger = NbaLogFactory.getLogger(this.getClass().getName());
        } catch (Exception e) {
            NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
            e.printStackTrace(System.out);
        }

        return logger;
    }
    
    
	
	/**
	 * @return Returns the webserviceResponse.
	 */
	public Object getWebserviceResponse() {
		return webserviceResponse;
	}
	/**
	 * @param webserviceResponse The webserviceResponse to set.
	 */
	public void setWebserviceResponse(Object webserviceResponse) {
		this.webserviceResponse = webserviceResponse;
	}

	/**
     * Override method for handling agent webservice response. Set the DTP tag for 100K min face amount for Charles Bailey agent 
     * 
     * @param nbaTXLife
     * @throws NbaBaseException
     */
	// NBLXA-2553 New method
    protected void handleAgentWebserviceResponse() throws NbaBaseException {
    	NbaTXLife nbaTXLifeResponse = (NbaTXLife) getWebserviceResponse();
    	if (nbaTXLifeResponse != null) {
    		for (int i = 0; i < nbaTXLifeResponse.getOLifE().getRelationCount(); i++) {
				Relation currentRelation = nbaTXLifeResponse.getOLifE().getRelationAt(i);
				if (NbaUtils.isAgentRelation(currentRelation)) {
					NbaParty party = nbaTXLifeResponse.getParty(currentRelation.getRelatedObjectID());
					if (party == null) {
						party = nbaTXLifeResponse.getParty(currentRelation.getOriginatingObjectID());
					}
					CarrierAppointment carrAppt = AxaUtils.getFirstCarrierAppointment(party.getParty().getProducer());
					CarrierAppointmentExtension carrApptExtn = NbaUtils.getFirstCarrierAppointmentExtension(carrAppt);
					if (carrAppt != null && carrApptExtn != null && isMinFaceAmount100KAgentCodePresent(carrAppt.getCompanyProducerID())) {
						carrApptExtn.setDtp(NbaConstants.DTP_AXADISTRIBUTOTSDTP100K);
					}
				}
			}
    	}
    }
    
	// NBLXA-2553 New method
  	protected boolean isMinFaceAmount100KAgentCodePresent(String agentCode) {
  		boolean flag = false;
  		for (int i = 0; i < minFaceAmount100KAgentCodes.length; i++) {
  			if (minFaceAmount100KAgentCodes[i].equals(agentCode)) {
  				flag = true;
  				break;
  			}
  		}
  		return flag;
  	}
}
