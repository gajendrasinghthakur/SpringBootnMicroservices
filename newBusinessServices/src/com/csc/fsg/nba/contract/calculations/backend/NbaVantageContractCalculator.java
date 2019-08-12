package com.csc.fsg.nba.contract.calculations.backend;
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
 * 
 * *******************************************************************************<BR>
 */
import com.csc.fsg.nba.contract.calculations.NbaContractCalculationsConstants;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapter;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapterFactory;
/**
 * NbaVantageContractCalculator is the adapter class to process calculations for the Vantage backend system. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA117</td><td>Version 7</td><td>Pending VANTAGE-ONE Calculations </td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
public class NbaVantageContractCalculator extends NbaBackendBaseContractCalculator {
    private static NbaLogger logger = null;
 
	public NbaVantageContractCalculator(){
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() {
        if (logger == null) {
            try {
                logger = NbaLogFactory.getLogger(NbaVantageContractCalculator.class.getName());
            } catch (Exception e) {
                NbaBootLogger.log("NbaVantageContractCalculator could not get a logger from the factory.");
                e.printStackTrace(System.out);
            }
        }
        return logger;
    }

    /**
     * Answers change sub type for a calculation type
     * @param calcType the calculation type
     * @return the change sub type for a calculation type
     * @throws NbaBaseException
     */
    public long getChangeSubType(String calcType) throws NbaBaseException {
        long changeSubType = -1;
        if (NbaContractCalculationsConstants.CALC_TYPE_ALL_STD_MODES_PREMIUM.equalsIgnoreCase(calcType)) {
            changeSubType = NbaOliConstants.OLIX_CHANGETYPE_STDMODEPREMIUM;
        } else if (NbaContractCalculationsConstants.CALC_TYPE_NON_STANDARD_MODE_PREMIUM.equalsIgnoreCase(calcType)) {
            changeSubType = NbaOliConstants.OLIX_CHANGETYPE_NONSTDMODEPREMIUM;
        } else if (NbaContractCalculationsConstants.CALC_TYPE_COMMISSION_TARGET.equalsIgnoreCase(calcType)) {
            changeSubType = NbaOliConstants.OLIX_CHANGETYPE_COMMISSIONTARGET;
        } else if (NbaContractCalculationsConstants.CALC_TYPE_GUIDELINE_PREMIUM.equalsIgnoreCase(calcType)) {
            changeSubType = NbaOliConstants.OLIX_CHANGETYPE_GUIDELINEPREMIUM;
        } else if (NbaContractCalculationsConstants.CALC_TYPE_JOINT_EQUAL_AGE.equalsIgnoreCase(calcType)) {
            changeSubType = NbaOliConstants.OLIX_CHANGETYPE_JOINTEQUALAGE;
        } else if (NbaContractCalculationsConstants.CALC_TYPE_MIN_NO_LAPSE_PREMIUM.equalsIgnoreCase(calcType)) {
            changeSubType = NbaOliConstants.OLIX_CHANGETYPE_MAPTARGET;
        } else if (NbaContractCalculationsConstants.CALC_TYPE_LIFE_COVERAGE_PREMIUM.equalsIgnoreCase(calcType)) {
            changeSubType = NbaOliConstants.OLIX_CHANGETYPE_LIFECOVERAGE;
        } else if (NbaContractCalculationsConstants.CALC_TYPE_LIFE_COVERAGE_OPTION_PREMIUM.equalsIgnoreCase(calcType)) {
            changeSubType = NbaOliConstants.OLIX_CHANGETYPE_LIFECOVOPTION;
        } else if (NbaContractCalculationsConstants.CALC_TYPE_ANNUITY_RIDER_COVERAGE_OPTION_PREMIUM.equalsIgnoreCase(calcType)) {
            changeSubType = NbaOliConstants.OLIX_CHANGETYPE_RIDERCOVOPTION;
        } else if (NbaContractCalculationsConstants.CALC_TYPE_LIFE_COVERAGE_SUBRATING_PREMIUM.equalsIgnoreCase(calcType)) {
            changeSubType = NbaOliConstants.OLIX_CHANGETYPE_COVERAGESUBRATING;
        } else if (NbaContractCalculationsConstants.CALC_TYPE_SURRENDER_CHARGE.equalsIgnoreCase(calcType)) {
            changeSubType = NbaOliConstants.OLIX_CHANGETYPE_SURRENDERTARGET;
        } else if (NbaContractCalculationsConstants.CALC_TYPE_7PAY_PREMIUM.equalsIgnoreCase(calcType)) {
            changeSubType = NbaOliConstants.OLIX_CHANGETYPE_7PAY;   
        } else if (NbaContractCalculationsConstants.CALC_TYPE_PREMIUM_LOAD.equalsIgnoreCase(calcType)) {            
            changeSubType = NbaOliConstants.OLIX_CHANGETYPE_PREMIUMLOADTARGET; 
        } else {
            throw new NbaBaseException("Calculation is not supported for " + calcType);
        }
        return changeSubType;
    }

    /**
     * Find and invoke calculation service
     * @param request the request object
     * @return the response from calculation service
     * @throws NbaBaseException
     */
    public NbaTXLife invokeCalculationService(NbaTXLife request) throws NbaBaseException {
		NbaWebServiceAdapter service = NbaWebServiceAdapterFactory.createWebServiceAdapter(request.getBackendSystem(),
                NbaConfiguration.WEBSERVICE_CATEGORY_HOLDING, NbaConfiguration.WEBSERVICE_FUNCTION_HOLDING);
		return service.invokeWebService(request);
    }    
    
    /**
     * Returns false as policy product information need not be added to the request
     * @return false as policy product information need not be added to the request
     */ 
    public boolean addPolicyProductToRequest() {
        return false;
    }
}
