package com.csc.fsg.nba.transaction.validation;
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
import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
/**
 * Interface for Transaction Validation. This interface is implemented by the Transaction
 * validation implementation classes. In nba configuration file the each business process 
 * has one or more subset values. Transaction validation is applicable for the business 
 * functions with subset value greater than or equal to 900. For each subset value there is an
 * implementation class which implements this interface. The NbaTransactionValidationFactory
 * instantiates  
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA094</td><td>Version 3</td><td>Transaction Validation</td></tr>
 * <tr><td>P2AXAL016</td><td>AXA Life Phase 2</td><td>Product Validation</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public interface NbaTransactionValidation {
	/**
	 * Perform the validation process. The implementation class will define this abstract method.
	 * @param nbaTXLife the NbaTXLife instance
	 * @param nbaDst the NbaDst object
	 * @param user the NbaUserVO object
	 * @throws NbaBaseException 
	*/
	void validate(NbaTXLife txLife, NbaDst nbaDst, NbaUserVO userVO, AccelProduct nbaProduct) throws NbaBaseException;//P2AXAL016
}
