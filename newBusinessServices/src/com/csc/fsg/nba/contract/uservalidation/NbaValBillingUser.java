package com.csc.fsg.nba.contract.uservalidation;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fsg.nba.contract.validation.NbaContractValidationCommon;
import com.csc.fsg.nba.contract.validation.NbaContractValidationImpl;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.ValProc;
/**
 * NbaValBillingUser demonstrates a user implementation for Billing validation.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA064</td><td>Version 3</td><td>Contract Validation</td></tr>
 * <tr><td>SPR1994</td><td>Version 4</td><td>Correct user validation example </td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>SPR1234</td><td>Version 4</td><td>General source code clean up </td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.18</td><td>AXA Life Phase 1</td><td>Producer Interfaces</td></tr> 
 * <tr><td>NBA237</td><td>Version 8</td><td>Migrate Policy Product Transmittal XML1201 to 2.15.00</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaValBillingUser extends NbaContractValidationCommon implements NbaContractValidationImpl {	//SPR1234
	// SPR1994 code deleted 
	//NBA237 changed method signature
	public void initialze(NbaDst nbaDst, NbaTXLife nbaTXLife, Integer subset, NbaOLifEId nbaOLifEId, AccelProduct nbaProduct, NbaUserVO userVo) { //AXAL3.7.18
		super.initialze(nbaDst, nbaTXLife, subset, nbaOLifEId, nbaProduct, userVo); //AXAL3.7.18
		initProcesses();
		// SPR1994 code deleted 
		Method[] allMethods = this.getClass().getDeclaredMethods();
		for (int i = 0; i < allMethods.length; i++) {
			Method aMethod = allMethods[i];
			String aMethodName = aMethod.getName();
			if (aMethodName.startsWith("process_")) {
				// SPR3290 code deleted
				processes.put(aMethodName.substring(8).toUpperCase(), aMethod);
			}
		}
		// SPR1994 code deleted 
	}
	/**
	 * Override parent implemenation. Do not throw exception if process not defined.
	 * @see com.csc.fsg.nba.contract.validation.NbaContractValidationImpl#validate()
	 */
	// ACN012 changed signature
	public void validate(ValProc valProc, ArrayList objects) {
		//begin SPR1994
		if (getProcesses().containsKey(valProc.getId())) { //ACN012
			try {
				setObjects(objects);
				setNbaConfigValProc(valProc); //ACN012
				Method process = (Method) getProcesses().get(valProc.getId()); //ACN012
				process.invoke(this, null);
			} catch (IllegalAccessException e) {
				addNewSystemMessage(
					VALIDATION_PROCESSING,
					concat("Process id: ", valProc.getId(), ", IllegalAccessException: ", e.toString()), //ACN012
					"");
			} catch (InvocationTargetException e) {
				addNewSystemMessage(
					VALIDATION_PROCESSING,
					concat("Process id: ", valProc.getId(), ", InvocationTargetException: ", e.getTargetException().getMessage()), //ACN012
					"");
			}
		}
		//end SPR1994
	}
	/**   
	 *  Outline for example user implementation of process P002   
	 */
	// SPR1994 New Method
	public void process_P002() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBillingUser.process_P002()"); //NBA103
			if (false) {  //Example only. Do not generate System Message
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Payment Mode: ", "X"), getIdOf(getPolicy()));
			}

		}
	}
}
