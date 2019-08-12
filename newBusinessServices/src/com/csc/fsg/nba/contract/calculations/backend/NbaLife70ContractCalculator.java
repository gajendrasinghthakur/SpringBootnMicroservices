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
import com.csc.fsg.nba.contract.calculations.NbaContractCalculationsConstants;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;
/**
 * NbaLife70ContractCalculator is the adapter class to process calculations for the Life 70 backend system. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>P2AXAL016CV</td><td>AXA Life Phase 2</td><td>Product Val - Life 70 calculations</td></tr>
 * <tr><td>P2AXAL029</td><td>AXA Life Phase 2</td><td>Contract Print</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class NbaLife70ContractCalculator extends NbaBackendBaseContractCalculator {
    private static NbaLogger logger = null;
    private String calculationType = null;
 
	public NbaLife70ContractCalculator() {
	}
	
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() {
        if (logger == null) {
            try {
                logger = NbaLogFactory.getLogger(NbaLife70ContractCalculator.class.getName());
            } catch (Exception e) {
                NbaBootLogger.log("NbaLife70ContractCalculator could not get a logger from the factory.");
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
        return -1;
    }
	
	/**
     * This method makes a call to the Life 70 contract calculation interface and returns the response txlife.
     * @param calcType the calculation type
     * @param request the input request txlife
     * @return response nbaTXLife with calculated values
     * @throws NbaBaseException
     */    
    public NbaTXLife calculate(String calcType, NbaTXLife request) throws NbaBaseException {
    	setCalculationType(calcType);
    	NbaTXLife response = invokeCalculationService(request);
        return response;
    }
   
    /**
     * Find and invoke calculation service
     * @param request the request object
     * @return the response from calculation service
     * @throws NbaBaseException
     */
    public NbaTXLife invokeCalculationService(NbaTXLife request) throws NbaBaseException {
    	//Call the Calculation webservice to retrive calculations based on the calculation type
		AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(getOperationName(), getNbaUserVO(),
				request, null, getTransSubType());//P2AXAL029
		return (NbaTXLife) webServiceInvoker.execute();
    }    
    
    /**
     * Returns false as policy product information need not be added to the request
     * @return false as policy product information need not be added to the request
     */ 
    public boolean addPolicyProductToRequest() {
        return false;
    }
    
	/**
	 * @return Returns the transaction sub type based on the calculation type if CV or PRINT.
	 */
	private Long getTransSubType() {
		long transSubType = -1;
		if(NbaContractCalculationsConstants.CALC_TYPE_CV_CALC.equalsIgnoreCase(getCalculationType())) {
			transSubType = NbaOliConstants.TC_SUBTYPE_BACKEND_CALCULATIONS;
		} else if (NbaContractCalculationsConstants.CALC_TYPE_PRINT_CALC.equalsIgnoreCase(getCalculationType())) {
			transSubType = NbaOliConstants.TC_SUBTYPE_BACKEND_PRINT_CALCULATIONS;
		}
		return new Long(transSubType);
	}
    
	/**
	 * @return Returns the calculationType.
	 */
	public String getCalculationType() {
		return calculationType;
	}
	/**
	 * @param calculationType The calculationType to set.
	 */
	public void setCalculationType(String calculationType) {
		this.calculationType = calculationType;
	}
	
	/**
	 * @return Returns the transaction sub type based on the calculation type if CV or PRINT.
	 */
	//New Method P2AXAL029
	private String getOperationName() {
		String operationName = null;
		if(NbaContractCalculationsConstants.CALC_TYPE_CV_CALC.equalsIgnoreCase(getCalculationType())) {
			operationName = AxaWSConstants.WS_OP_L70_CALCULATIONS;
		} else if (NbaContractCalculationsConstants.CALC_TYPE_PRINT_CALC.equalsIgnoreCase(getCalculationType())) {
			operationName = AxaWSConstants.WS_OP_L70_PRINT_CALCULATIONS;
		}
		return operationName;
	}
}
