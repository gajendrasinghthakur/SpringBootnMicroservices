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
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
/**
 * NbaBackendContractCalculator is the interface class to process back end system calculations. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA133</td><td>Version 6</td><td>nbA CyberLife Interface for Calculations and Contract Print</td></tr>
 * <tr><td>P2AXAL016CV</td><td>AXA Life Phase 2</td><td>Life 70 Calculations</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
public interface NbaBackendContractCalculator {
    
	/**
	 * This method will be implemented by each NbaBackendContractCalculator to peform specific processing needed 
	 * to obtain calculated value from backend. This method assumes that data in incoming request will be converted
	 * into out response with updated values for calculation fields.
	 * @param calcType the calculation type
	 * @param holding the calculation request
	 * @return the NbaTXLife response with calculated values
	 * @throws NbaBaseException
	 */
	public NbaTXLife calculate(String calcType, NbaTXLife holding) throws NbaBaseException;	

	/**
	 * Sets the NbaUserVO
	 * @param userVO the NbaUserVO
	 * @throws NbaBaseException
	 */
	//P2AXAL016CV new method
	public void setNbaUserVO(NbaUserVO userVO) throws NbaBaseException;	
}
