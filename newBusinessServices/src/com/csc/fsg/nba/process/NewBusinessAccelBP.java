package com.csc.fsg.nba.process;

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
 * *******************************************************************************<BR>
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.Message;
import com.csc.fs.accel.WorkFlowMessages;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.logging.LogHandler;
import com.csc.fs.sa.SystemDefinition;
import com.csc.fs.sa.SystemDefinitionHandler;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.workflow.NbaWorkflowDistribution;

/**
 * Class Description.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA181</td><td>Version 7</td><td>Contract Plan Change Rewrite</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public abstract class NewBusinessAccelBP extends com.csc.fs.accel.AccelBP {
	
    protected final static String A_JOB_GETWORK = "NBAGetWork";
    protected final static String A_JOB_AUTOPROCESSGETWORK = "NBAAutoPGetWork";
    
    protected final static String A_ERR_NOWORK = "SYS0070";
    protected final static String A_ERR_CANT_LOCK = "SYS0091";
    protected final static String A_ERR_CANT_LOCK_REST = WorkFlowMessages.ERR_MSG_LOCK_FAILED.toString(); //APSL5055-NBA331.1
    protected final static String A_ERR_NO_SUSPENSION = "SYS0116";	
    protected final static String A_ERR_STATUS_NOT_FOUND = "SYS0161";
    protected final static String A_ERR_AWD_TIMEOUT = "SYS0100"; //ALII2032
    protected final static String A_ERR_APP_HLD_LOCKED_BY_ANOTHER_USER = "Case locked by another user";//NBLXA-2619
    protected final static String CASERECORDTYPE = "C";
    protected final static String TRANSACTIONRECORDTYPE = "T";
    protected final static String SOURCERECORDTYPE = "O";
    
    protected static final String EMPTYSTRING = "";
    protected static final String NOINCREASE = "000";
    // APSL5055-NBA331 code deleted    
    protected final static String RESULT = "result"; //NBA212
    
    // APSL5055-NBA331 code deleted
    
    protected static String getWorkflowSystemName() throws NbaBaseException {
        return NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.DEFAULT_SYSTEM);
    }
    
    protected boolean isCreateFlagOn(NbaDst nbaDst) throws NbaBaseException {
        return (nbaDst.isTransaction() && NbaConstants.YES_VALUE.equalsIgnoreCase(nbaDst.getTransaction().getCreate()))
                || (nbaDst.isCase() && NbaConstants.YES_VALUE.equalsIgnoreCase(nbaDst.getCase().getCreate()));
    }

    /**
     * Determine if the LobData object is a duplicate based on its name and sequence number values.
     * @param lobs - map containing keys of previous LobData objects
     * @param lobData - the LobData objects
     * @return true if the LobData object is not a duplicate
     */
    protected  boolean isNotDuplicate(Map lobs, LobData lobData) {
        String key = (new StringBuffer().append(lobData.getDataName()).append("_").append(lobData.getSequenceNmbr())).toString();
        if (lobs.containsKey(key)) {
            return false;
        }
        lobs.put(key, EMPTYSTRING);
        return true;
    }
    
    //NBA208-32 code deleted
    
   
    /**
     * @param lobList
     * @return
     */
    protected LobData[] getFormattedLobList(NbaLob nbALob) {
        // SPR3290 code deleted
        //NBA208-32
        if (nbALob != null && nbALob.getLobs() != null) {
            return (LobData[]) nbALob.getLobs().toArray(new LobData[nbALob.getLobs().size()]);
        }
        return null;
    }

    //P2AXAL040 changed the method to introduce status in the logging
	public static boolean processResult(AccelResult value) throws NbaBaseException {
		String status = null;
		if (value.getFirst() instanceof NbaDst) {
			status = ((NbaDst) value.getFirst()).getStatus();
		}
		return processResult(value, status);
	}

	//P2AXAL040 updated existing method with new signature for status
	public static boolean processResult(AccelResult value, String status) throws NbaBaseException {
		boolean containsError = value.isErrors();
		Message messages[] = value.getMessages();
		List currentErrors = new ArrayList();
		if (messages != null && messages.length > 0) {
			for (int i = 0; i < messages.length; i++) {
				Message message = messages[i];
				String errorVal = "";
				try {
					errorVal = message.format();
				} catch (Exception ex) {
					//leave msg null
				}
				if (errorVal == null || errorVal.equals(Message.ERR_MESSAGE_MISSING)) {
					List data = message.getData();
					if (data != null) {
						errorVal += " data[" + data.toString() + "]";
					}
					//begin	P2AXAL040
				} else if (errorVal == null || errorVal.startsWith(A_ERR_STATUS_NOT_FOUND)) {
					if (!NbaUtils.isBlankOrNull(status)) {
						errorVal += "( For status: " + status + " ) ";
					} else {
						errorVal +="( For status: NullORBlank  )";
					}//end P2AXAL040
				}
				currentErrors.add(message);
			}
		}
		if (containsError) {
			StringBuffer errors = new StringBuffer();
			String msg = "";
			boolean lockException = false;
			Iterator iter = currentErrors.iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof Message) {
					Message message = (Message) obj;
					try {
						msg = message.format();
					} catch (Exception ex) {
						//leave msg null
					}
					if (msg == null || msg.equals(Message.ERR_MESSAGE_MISSING)) {
						List data = message.getData();
						if (data != null) {
							msg += " data[" + data.toString() + "]";
						}
						//begin	P2AXAL040
					} else if (msg == null || msg.startsWith(A_ERR_STATUS_NOT_FOUND)) {
						if (!NbaUtils.isBlankOrNull(status)) {
							msg += "( For status: " + status + " ) ";
						} else {
							msg +="( For status: NullORBlank )";
						}//end P2AXAL040
					}
				} else {
					msg = obj.toString();
				}
				if (msg.equals(A_ERR_CANT_LOCK_REST)|| msg.startsWith(A_ERR_CANT_LOCK)) { // APSL5055-NBA331.1
					lockException = true;
				}
				errors.append(msg).append(" ");
			}
			if (lockException) {
				throw new NbaAWDLockedException(NbaAWDLockedException.LOCKED_BY_USER);
			}
			NbaBaseException ex = new NbaBaseException(errors.toString());
			//Begin ALS5535
			boolean fatalInd = false;
			if (value.getData() != null && value.getData().size() != 0) {
				Object firstData = value.getData().get(0);
				if (firstData instanceof NbaBaseException) {
					fatalInd = ((NbaBaseException) firstData).isFatal();
				}
			}
			if (fatalInd) {
				ex.forceFatalExceptionType();
			}
			//End ALS5535
			ex.markAsLogged();
			throw ex;
		}
		return containsError;
	}

    /**
	 * Returns a string representing the current time stamp from the workflow system.
	 * 
	 * @param nbaUserVO
	 * @return
	 * @throws NbaBaseException
	 */
	//NBA181 New Method
    protected String getTimeStamp(NbaUserVO nbaUserVO) throws NbaBaseException {
        AccelResult res = (AccelResult)callBusinessService("NbaRetrieveTimeStampBP", nbaUserVO);
    	processResult(res);
        NbaDst timeStamp = (NbaDst)res.getFirst();
        //NBA208-32
        return timeStamp.getTimestamp();
    }
    
    //ALS4381 New Method, ALII1187 method signature changed
    protected String determineEquitableQueue(NbaLob lob, List queues, String queueLOB) throws NbaBaseException {
	    String equitableStatus = null;
	    String queue = null;
	    int workCount = 0;
	    int equitableCount = 0;
		int count = queues.size();
		NbaWorkflowDistribution distribution = new NbaWorkflowDistribution(lob);
		for (int i = 0; i < count; i++) {
		    queue = (String) queues.get(i);
		    workCount = distribution.getAssignedWorkCountByQueue(queue, queueLOB); // ALII1187
		    if (i == 0 || workCount < equitableCount) {
		        equitableCount = workCount;
		        equitableStatus = queue;
		    }
		}
		return equitableStatus;
	}
    //ALS4381 New Method
    protected String getListOfCMQueues(String resultQueues, String underwriterLOB) {
		String token = null;
		String underwriterToken = null;
		NbaStringTokenizer tokens = new NbaStringTokenizer(resultQueues, "|");
		while (tokens.hasMoreTokens()) {
			token = tokens.nextToken();
			underwriterToken = token.substring(0, token.indexOf("="));
			if (underwriterToken.equalsIgnoreCase(underwriterLOB)) {
				return token.substring(token.indexOf("=") + 1);
			}
		}
		return "-";
	}
}
