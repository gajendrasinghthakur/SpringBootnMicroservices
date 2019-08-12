package com.csc.fsg.nba.contract.calculations.backend;
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
import java.util.Iterator;
import java.util.List;

import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fsg.nba.bean.accessors.NbaProductAccessFacadeBean;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
/**
 * NbaBackendBaseContractCalculator is the base adapter class to process backedn calculations. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA117</td><td>Version 7</td><td>Pending VANTAGE-ONE Calculations</td></tr>
 * <tr><td>NBA237</td><td>Version 8</td><td>Migrate Policy Product Transmittal XML1201 to 2.15.00</td></tr>
 * <tr><td>P2AXAL016CV</td><td>AXA Life Phase 2</td><td>Product Val - Life 70 Calculations</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
public abstract class NbaBackendBaseContractCalculator implements NbaBackendContractCalculator {
    private static NbaLogger logger = null;
    protected NbaUserVO nbaUserVO = null; //P2AXAL016CV
    
	public NbaBackendBaseContractCalculator(){
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() {
        if (logger == null) {
            try {
                logger = NbaLogFactory.getLogger(NbaBackendBaseContractCalculator.class.getName());
            } catch (Exception e) {
                NbaBootLogger.log("NbaBackendBaseContractCalculator could not get a logger from the factory.");
                e.printStackTrace(System.out);
            }
        }
        return logger;
    }	

    /* (non-Javadoc)
     * @see com.csc.fsg.nba.contract.calculations.backend.NbaBackendContractCalculator#calculate(java.lang.String, com.csc.fsg.nba.vo.NbaTXLife)
     */
    public NbaTXLife calculate(String calcType, NbaTXLife holding) throws NbaBaseException {
        NbaTXLife request = createCalcRequest(calcType, holding);        
        getLogger().logDebug("Backend Calculation Request : " + (null != request ? request.toXmlString() : "")); //P2AXAL016CV
        NbaTXLife response = invokeCalculationService(request);        
        getLogger().logDebug("Backend Calculation Response : " + (null != response ? response.toXmlString() : "")); //P2AXAL016CV
        return response;
    }
    
    /**
     * Creates calculation request
     * @param calcType the calculation type
     * @param holding the holding inquiry object
     * @return the calculation request for a calculation type
     * @throws NbaBaseException
     */
    protected NbaTXLife createCalcRequest(String calcType, NbaTXLife holding) throws NbaBaseException {
        NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
        nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
        nbaTXRequest.setTransSubType(NbaOliConstants.TC_SUBTYPE_BACKEND_CALCULATIONS);
        nbaTXRequest.setChangeSubType(getChangeSubType(calcType));
        nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
        nbaTXRequest.setBusinessProcess(holding.getBusinessProcess());

        //create txlife with default request fields
        NbaTXLife request = new NbaTXLife(nbaTXRequest);
        request.setOLifE(holding.getOLifE());
        request = removeIgnoredObjects(request); //remove unwanted objects
        if (addPolicyProductToRequest()) {
            addPolicyProduct(request); //add policy product information
        }
        return request;
    }

    /**
     * Answers change sub type for a calculation type
     * @param calcType the calculation type
     * @return the change sub type for a calculation type
     * @throws NbaBaseException
     */
    public abstract long getChangeSubType(String calcType) throws NbaBaseException;

    /**
     * Find and invoke calculation service
     * @param request the request object
     * @return the response from calculation service
     * @throws NbaBaseException
     */
    public abstract NbaTXLife invokeCalculationService(NbaTXLife request) throws NbaBaseException;
    
    /**
     * Returns true if policy product information to be added to the request
     * @return true if policy product information to be added to the request else return false
     */
    public abstract boolean addPolicyProductToRequest();
            
    /**
     * Removed ignored or un-wanted objects from txlife. 
     * @param txlife the request 
     * @return the modified request with ignored objects removed.
     * @throws NbaBaseException
     */
    protected NbaTXLife removeIgnoredObjects(NbaTXLife txlife) throws NbaBaseException {
        //remove deleted objects
        NbaTXLife modifiedLife; 
        try {
            modifiedLife = new NbaTXLife(txlife.toXmlString()); //toXMLString will remove deleted objects
        } catch (Exception e) {
            throw new NbaBaseException("Could not clone calculation request", e);
        }

        //remove Denied, Terminated or Invalid Coverage Options
        removeInvalidCovOptions(modifiedLife);
        //remove proposed sub standard ratings
        removeProposedRatings(modifiedLife);
        return modifiedLife;
    }
    
    /**
     * Remove invalid cov options from request
     * @param txlife the request object
     */
    protected void removeInvalidCovOptions(NbaTXLife txlife) {
        if (txlife.isLife()) {
            Life life = txlife.getLife();
            int count = life.getCoverageCount();
            for (int i = 0; i < count; i++) {
                removeInvalidCovOptions(life.getCoverageAt(i).getCovOption());
            }
        } else {
            Annuity annuity = txlife.getAnnuity();
            int count = annuity.getRiderCount();
            for (int i = 0; i < count; i++) {
                removeInvalidCovOptions(annuity.getRiderAt(i).getCovOption());
            }
        }
    }
    
    /**
     * Remove invalid cov options
     * @param covOptions the cov options list
     */
    protected void removeInvalidCovOptions(List covOptions){
        Iterator iterator = covOptions.iterator();
        CovOption covOption = null;
        while (iterator.hasNext()) {
            covOption = (CovOption) iterator.next();
            long covOptionStatus = covOption.getCovOptionStatus();
            if (covOptionStatus == NbaOliConstants.OLI_POLSTAT_TERMINATE || covOptionStatus == NbaOliConstants.OLI_POLSTAT_DECISSUE
                    || covOptionStatus == NbaOliConstants.OLI_POLSTAT_INVALID) {
                iterator.remove();
            }
        }
    }
    
    
    /**
     * Remove proposed rating from request
     * @param txlife the request object
     */
    protected void removeProposedRatings(NbaTXLife txlife) {
        if (txlife.isLife()) {
            Life life = txlife.getLife();
            int count = life.getCoverageCount();
            for (int i = 0; i < count; i++) {
                removeProposedRatings(life.getCoverageAt(i));
            }
        } else {
            Annuity annuity = txlife.getAnnuity();
            int count = annuity.getRiderCount();
            for (int i = 0; i < count; i++) {
                removeProposedRatingsOnCovOptions(annuity.getRiderAt(i).getCovOption());
            }
        }
    }
    
    /**
     * Remove proposed rating from a coverage. It will remove proposed rating for both life partificants and
     * covoptions for this coverage.
     * @param coverage the coverage object
     */
    protected void removeProposedRatings(Coverage coverage) {
        int size = coverage.getLifeParticipantCount();
        Iterator iterator = null;
        for (int i = 0; i < size; i++) {
            iterator = coverage.getLifeParticipantAt(i).getSubstandardRating().iterator();
            while (iterator.hasNext()) {
                if (!NbaUtils.isValidRating((SubstandardRating) iterator.next())) { //if not valid, remove it
                    iterator.remove();
                }
            }
        }
        removeProposedRatingsOnCovOptions(coverage.getCovOption());
    }
    
    /**
     * Remove proposed rating from cov options
     * @param covOptions the cov options list
     */
    protected void removeProposedRatingsOnCovOptions(List covOptions) {
        int size = covOptions.size();
        Iterator iterator = null;
        for (int i = 0; i < size; i++) {
            iterator = ((CovOption)covOptions.get(i)).getSubstandardRating().iterator();
            while (iterator.hasNext()) {
                if (!NbaUtils.isValidRating((SubstandardRating) iterator.next())) { //if not valid, remove it
                    iterator.remove();
                }
            }
        }
    }
    
	/**
	 * Add the PolicyProduct information for the Contract to the OLifE.
	 * @param calcOlife
	 * @param holdingIssue
	 * @throws NbaBaseException when unable to retrieve the PolicyProduct for the Contract
	 */
	protected void addPolicyProduct(NbaTXLife holding) throws NbaBaseException {
		try {
            NbaProductAccessFacadeBean npa = new NbaProductAccessFacadeBean();  //NBA213
            AccelProduct nbaprod = npa.doProductInquiry(holding); //NBA237
            holding.getOLifE().setPolicyProduct(nbaprod.getOLifE().getPolicyProduct());
        } catch (Exception e) {
            throw new NbaBaseException("Unable to load Policy Product information for " + holding.getPolicy().getProductCode(), e);
        }
    }
	
	/**
	 * Returns nbaUserVO
	 */
	//P2AXAL016CV new method
	public NbaUserVO getNbaUserVO() {
    	return nbaUserVO;
    }
	
	/**
	 * @param nbaUserVO The nbaUserVO to set.
	 */
	//P2AXAL016CV new method
	public void setNbaUserVO(NbaUserVO nbaUserVO) {
		this.nbaUserVO = nbaUserVO;
	}
}
