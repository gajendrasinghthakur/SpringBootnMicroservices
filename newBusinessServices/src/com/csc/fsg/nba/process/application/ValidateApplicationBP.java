package com.csc.fsg.nba.process.application;

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
 * *******************************************************************************<BR>
 */

import java.util.List;

import com.csc.fs.Message;
import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.business.process.NbaXML103SetDefaultValues;
import com.csc.fsg.nba.business.process.NbaXML103Validation;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.transaction.validation.NbaTransactionValidationFacade;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;

/**
 * Performs application and transaction validation on the current case's application
 * data source.  Required input is an <code>NbaDst</code> with an XML 103 transaction
 * as an applicaton data source.  The current user instance <code>NbaUserVO</code>
 * should also be set on the NbaDst. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA151</td><td>Version 6</td><td>UL and VUL Application Entry Rewrite</td></tr>
 * <tr><td>NBA187</td><td>Version 7</td><td>nbA Trial Application Project</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>P2AXAL016</td><td>AXA Life Phase 2</td><td>Product Validation</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 extends NewBusinessAccelBP
public class ValidateApplicationBP extends NewBusinessAccelBP {

    /*
     * (non-Javadoc)
     * 
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			//begin NBA187
		    NbaDst nbaDst = setApplicationdefaults((NbaDst) input);
			List errors = validateApplication(nbaDst);
			result.addResult(nbaDst);
			//end NBA187
			if (errors != null && errors.size() > 0) {
				Message msg = new Message();
				String[] messages = new String[errors.size()];
				for (int i = 0; i < errors.size(); i++) {
					messages[i] = (String) errors.get(i);
				}
				msg = msg.setVariableData(messages);
				result.setErrors(true);
				result.addMessage(msg);
			}
		} catch (Exception e) {
			addExceptionMessage(result, e);
			return result;
		}
		return result;
	}

	/**
	 * Performs application and transaction validation on the current case's
	 * application data source.
	 * @param work
	 * @return
	 * @throws NbaBaseException
	 */
	protected List validateApplication(NbaDst work) throws NbaBaseException {
		List errors = doApplicationValidation(work);
		String error = doTransactionValidation(work);
		if (error != null) {
			errors.add(error);
		}
		return errors;
	}

    /**
     * Performs application validation on the application data source attached
     * to the work item.
     * @param work
     * @return
     */
    private List doApplicationValidation(NbaDst work) throws NbaBaseException {
        NbaXML103Validation xmlValidation = new NbaXML103Validation();
        return xmlValidation.doXML103Validation(work.getNbaCase());
    }

    /**
     * Performs transaction validation.  If an error occurs, the error message is 
     * returned.  Otherwise, the method returns null.
     * @param work
     * @return
     */
	protected String doTransactionValidation(NbaDst work) {
		try {
			NbaTXLife txLife = work.getNbaCase().getXML103Source();
			txLife.setBusinessProcess(NbaConstants.PROC_VIEW_APPLICATION_ENTRY);
			NbaTransactionValidationFacade.validateBusinessProcess(txLife, work, work.getNbaUserVO(), null);//P2AXAL016
		} catch (NbaBaseException nbe) {
			return nbe.getMessage();
		}
		return null;
    }
	 /**
     * Sets the default values for a non-formal application
     * @param work
     * @return updated work
     */
     //NBA187 New Method
    private NbaDst setApplicationdefaults(NbaDst work) throws NbaBaseException {
        NbaXML103SetDefaultValues xmlDefaults = new NbaXML103SetDefaultValues();
        return xmlDefaults.setPresubmitDefaultValues(work);
    }

}
