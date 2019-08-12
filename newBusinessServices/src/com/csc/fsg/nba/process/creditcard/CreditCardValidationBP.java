package com.csc.fsg.nba.process.creditcard;

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

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.Message;
import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkFormatter;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.StandardAttr;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * Performs credit card validation on a given work item represented by an <code>NbaDst</code>.
 * <p>
 * The <code>NbaDst</code> input for this process can be either a case or transaction work
 * item.  If the input is a case work item, the process will search for the first <b>NBPAYMENT</b>
 * transaction that is marked for creation.  If the input is a transaction, the process
 * expects it to be an <b>NBPAYMENT</b> transaction.
 * <p>
 * Credit card validation is performed by making a call to the <b>CreditCard</b> VP/MS model.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA172</td><td>Version 7</td><td>Credit Card Payment Rewrite</td></tr> 
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class CreditCardValidationBP extends NewBusinessAccelBP {

    protected static final String ATTRIBUTE_INFORCE_PAYMENT = "A_InforcePayment";

    protected static final String ATTRIBUTE_RETURN_TYPE = "A_ReturnType";

    protected static final String RETURN_TYPE_VALUE = "value";

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            String error = doCreditCardValidation((NbaDst) input);
            if (error != null && error.length() > 0) {
            	// [TODO] replace w/ addErrorMessage(result, error);
                Message msg = new Message();
                String[] messages = new String[1];
                messages[0] = error;
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
     * Performs credit card validation on a NBPAYMENT transaction. If the incoming work item is a case,
     * the child transactions are iterated thru looking for the first NBPAYMENT that is newly created.
     * It returns an empty string if validation succeeds, or an error describing why validation failed.
     * @param nbaDst - Work Item
     * @return String - comma delimited error message(s)
     * @throws NbaBaseException
     */
    protected String doCreditCardValidation(NbaDst nbaDst) throws NbaBaseException {
        NbaLob paymentLob = null;
        if (nbaDst.isCase()) {
            List transList = nbaDst.getNbaTransactions();
            NbaTransaction transaction = null;
            for (int i = transList.size() - 1; i >= 0; i--) {
                transaction = (NbaTransaction) transList.get(i);
                if (NbaConstants.A_WT_PAYMENT.equalsIgnoreCase(transaction.getWorkType()) &&
                	(transaction.getTransaction().getCreate() != null && transaction.getTransaction().getCreate().equals("Y"))) {  //NBA208-32
                    paymentLob = transaction.getNbaLob();
                    break;
                }
            }
        } else {
            paymentLob = nbaDst.getNbaLob();
        }
        Map deOinkMap = new HashMap();
        deOinkMap.put(ATTRIBUTE_RETURN_TYPE, RETURN_TYPE_VALUE);
        if (paymentLob.getInforcePaymentType() > 0) {
            deOinkMap.put(ATTRIBUTE_INFORCE_PAYMENT, NbaConstants.INFORCE_PAYMENT);
        }
        return processVpms(paymentLob, deOinkMap);
    }

    /**
     * Invokes the <b>CreditCard</b> VP/MS model using the <b>P_GetCCValidationXML</b> entry point
     * to validate the credit card information.  It returns an empty string if no validation errors
     * are found or a comma delimited string of validation errors.
     * @param paymentLob - the credit card transaction lobs
     * @param deOinkMap - additional information required by model
     * @throws NbaBaseException
     */
    protected String processVpms(NbaLob paymentLob, Map deOinkMap) throws NbaBaseException {
        NbaVpmsAdaptor vpmsProxy = null;
        try {
            NbaOinkDataAccess data = new NbaOinkDataAccess(paymentLob);
            NbaOinkFormatter dataFormatter = data.getFormatter();
            dataFormatter.setDateFormat(NbaOinkFormatter.DATE_FORMAT_MMDDYYYY);
            dataFormatter.setDateSeparator(NbaOinkFormatter.DATE_SEPARATOR_SLASH);
            vpmsProxy = new NbaVpmsAdaptor(data, NbaVpmsConstants.CREDIT_CARD_VALIDATION);
            vpmsProxy.setSkipAttributesMap(deOinkMap);
            vpmsProxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_CCVALIDATION_XML);
            String returnStr = "";
            VpmsComputeResult result = vpmsProxy.getResults();
            if (!result.isError()) {
                NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(result);
                List rulesList = vpmsResultsData.getResultsData();
                if (!rulesList.isEmpty()) {
                    String xmlString = (String) rulesList.get(0);
		            NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
		            VpmsModelResult vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
		            List strAttrs = vpmsModelResult.getStandardAttr();
		            //Generate delimited string if there are more than one parameters returned
		            Iterator itr = strAttrs.iterator();
		            if (itr.hasNext())
		                returnStr += ((StandardAttr) itr.next()).getAttrValue();
		            while (itr.hasNext()) {
		                returnStr += NbaConstants.DELIMITER_COMMA_STRING;
		                returnStr += ((StandardAttr) itr.next()).getAttrValue();
                    }
                }
            }
            return returnStr;
        } catch (RemoteException t) {
            throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
        } finally {
            if (vpmsProxy != null) {
	            try {
	                vpmsProxy.remove();
	            } catch (RemoteException re) {
	                LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED); //SPR3362
	            }
            }
        }
    }
}
