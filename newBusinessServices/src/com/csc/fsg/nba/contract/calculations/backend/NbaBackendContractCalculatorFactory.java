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

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.vo.NbaTXLife;

/**
 * NbaBackendContractCalculatorFactory
 * <p>
 * Implements a factory pattern for performing backend contract calculations.  The appropriate
 * calculator is created based on the specified backend type.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA133</td><td>Version 6</td><td>nbA CyberLife Interface for Calculations and Contract Print</td></tr>
 * <tr><td>NBA117</td><td>Version 7</td><td>Pending VANTAGE-ONE Calculations </td></tr>
 * <tr><td>P2AXAL016CV</td><td>AXA Life Phase 2</td><td>Product Val - Life 70 Calculations</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */

public class NbaBackendContractCalculatorFactory {
	
    /**
     * Return specific backend calculator for a backend system id. 
     * @param backEndSystem the backend system id
     * @return the backend system specific calculator
     * @throws NbaBaseException if a system id is not supported.
     */
    public static NbaBackendContractCalculator getCalculator(String backEndSystem) throws NbaBaseException {
        if (NbaConstants.SYST_CYBERLIFE.equalsIgnoreCase(backEndSystem)) {
            return new NbaCyberlifeContractCalculator();
        } else if (NbaConstants.SYST_VANTAGE.equalsIgnoreCase(backEndSystem)) { //NBA117
            return new NbaVantageContractCalculator(); //NBA117
        } else if (NbaConstants.SYST_LIFE70.equalsIgnoreCase(backEndSystem)) { //P2AXAL016CV
            return new NbaLife70ContractCalculator(); //P2AXAL016CV
        }
        throw new NbaBaseException("Backend calculator is not supported for backend " + backEndSystem);
    }

    /**
     * Gets the system specific backend calculator for a susyem id. It then call calculate method on
     * backend calculator to process specific calculation.
     * @param backEndSystem the backend system id
     * @param calcType the calculation type
     * @param request the calculation request
     * @return the NbaTXLife response with calculated values
     * @throws NbaBaseException
     */
    public static NbaTXLife calculate(String backEndSystem, String calcType, NbaTXLife request) throws NbaBaseException {
        return getCalculator(backEndSystem).calculate(calcType, request);
    }
}
