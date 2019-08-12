package com.csc.fsg.nba.process.contract;

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

import java.util.ArrayList;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.business.rule.NbaDeterminePlan;
import com.csc.fsg.nba.contract.validation.NbaContractValidation;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaContractMessage;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaValidateContractRequest;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;

/**
 * Validates a contract using the <code>NbaContractValidation</code> service.  The
 * process accepts an <code>NbaValidateContractRequest</code> as input. It returns
 * the same object with a list of <code>NbaContractMessages</code> populated for
 * each contract validation message.  The list of messages is kept as a local class
 * variable so other processes can update the list of messages prior to calling 
 * the contract validation service.   
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA181</td><td>Version 7</td><td>Contract Plan Change Rewrite</td></tr>
 * <tr><td>NBA139</td><td>Version 7</td><td>Plan Code Determination</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class ValidateContractBP extends NewBusinessAccelBP implements NbaConstants {
	private List messages = new ArrayList();

	public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
    		NbaValidateContractRequest req = (NbaValidateContractRequest)input;
			validate(req.getNbaTXLife(), req.getNbaDst(), req.getNbaUserVO());
			req.setContractMessages(getMessages());
			result.addResult(req);
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

	/**
	 * Calls the <code>NbaContractValidation</code> service to perform contract validation
	 * on a contract.  The generated validation messages will then be extracted to a
	 * collection for display.
	 * @param contract
	 * @param work
	 * @param user
	 * @throws NbaBaseException
	 */
    protected void validate(NbaTXLife contract, NbaDst work, NbaUserVO user) throws NbaBaseException {
        contract = determinePlanCode(contract, work, user);//NBA139
		new NbaContractValidation().validate(contract, work, user);
		populateContractMessages(contract.getPrimaryHolding());
    }

    /**
     * Retrieves all contract messages from a contract holding.  Each contract message
     * is represented by a <code>NbaContractMessage</code> value object.  Only include
     * messages that have not been deleted.
     * @param holding 
     * @return all system messages
     */
    protected void populateContractMessages(Holding holding) {
        SystemMessage sysMessage = null;
        SystemMessageExtension messageExt = null;
        NbaContractMessage message = null;

        int msgSize = holding.getSystemMessageCount();       
        for (int i = 0; i < msgSize; i++) {
            sysMessage = holding.getSystemMessageAt(i);
            // do not include deleted messages
			if (!sysMessage.isActionDelete()) {
	        	message = new NbaContractMessage();
	            message.setID(sysMessage.getId());
	            message.setSeverity(sysMessage.getMessageSeverityCode());
	            message.setCode(sysMessage.getMessageCode());
	            message.setDescription(sysMessage.getMessageDescription());

	            messageExt = NbaUtils.getFirstSystemMessageExtension(sysMessage);
	            if (messageExt != null) {
	                message.setType(messageExt.getMsgValidationType());
	                message.setRestrictCode(messageExt.getMsgRestrictCode());//AXAL3.7.40
	            }
	            getMessages().add(message);
			}
        }
    }

	/**
	 * Returns a list of contract validation error messages <code>NbaMessagesTableVO</code>.
	 * @return
	 */
	protected List getMessages() {
		return messages;
	}
	

	/**
     * This method is called to validate the Coverage, Benefits and funds for the newly selected plan from the Plan Change or Copy Contract view
     * @param nbaTXLife NbaTXLifeobject to be processed
     * @param dst NbaDst Object to be processed
     * @param user NbaUserVO object to be processed
     * @throws NbaBaseException
     */
	//New method NBA139
	protected NbaTXLife determinePlanCode(NbaTXLife nbaTXLife, NbaDst dst, NbaUserVO user) throws NbaBaseException {
        NbaLob aNbaLob = dst.getNbaLob();
        try {
            if (NbaConfiguration.getInstance().isGenericPlanImplementation()) {
                if ((aNbaLob.getContractChgType() == null) && (aNbaLob.getAppOriginType() != 0)
                        && !(NbaUtils.getGenericPlanOverrideInd(nbaTXLife.getPolicy()))) {
                    NbaDeterminePlan determinePlan = new NbaDeterminePlan();
                    if (aNbaLob.getOperatingMode().equalsIgnoreCase("S")) {
                        nbaTXLife = determinePlan.determinePlanCode(dst, nbaTXLife, false);//false here means that we don't want to addComments while validation
                    } else {
                        nbaTXLife = determinePlan.calculateBasePlan(dst, nbaTXLife, false);
                    }
                }
            }
            return nbaTXLife;
        } catch (NbaVpmsException e) {
            e.forceFatalExceptionType();
            throw e;
        } catch (NbaBaseException e) {
            e.forceFatalExceptionType();
            throw e;
        }
    }
}
