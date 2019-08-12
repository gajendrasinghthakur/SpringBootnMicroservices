package com.csc.fsg.nba.business.process.formal;
/*
 * **************************************************************************<BR>
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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************************<BR>
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fs.accel.valueobject.Comment;
import com.csc.fsg.nba.business.process.NbaAutomatedProcess;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaCase;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaGeneralComment;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

/**
 * This class provides utility methods for NbaProcFormal and its proxy classes.
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>ALS3091</td><td>AXA Life Phase 1</td><td>General code clean up of NbaProcFormal</td></tr>
 * <tr><td>ALS5881</td><td>AXA Life Phase 1</td><td>QC #5053 - Prod issue #3222: Comments different based on work item selected, dates on some comments are changed based on work item selected</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaProcFormalUtils {

	/**
	 * This method determines if the given work is Reg60
	 * @param work
	 * @param user
	 * @return
	 * @throws NbaBaseException
	 */
	protected static boolean isReg60Case(NbaDst work, NbaUserVO user) throws NbaBaseException {
		NbaVpmsAdaptor vpmsProxy = null;
		try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(work.getNbaLob());
			Map deOink = new HashMap();
			deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(user));
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsConstants.REPLACEMENTS_PROCESSING);
			vpmsProxy.setVpmsEntryPoint("P_GetReg60");
			vpmsProxy.setSkipAttributesMap(deOink);
			NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
			String result = (String) data.getResultsData().get(0);
			if (NbaAutomatedProcess.TRUE_STR.equalsIgnoreCase(result)) {
				return true;
			}
		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException("InformalToFormal" + NbaVpmsException.VPMS_EXCEPTION, re);
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
			}
		}
		return false;
	}

	protected static void mergeMatchingCase(NbaCase originalCase, NbaCase matchingCase, boolean breakRelation) throws NbaBaseException,
            NbaVpmsException {
        //Remove policy number from all sources of original case
        List originalSources = originalCase.getNbaSources();
        NbaSource originalSource = null;
        for (int i = 0; i < originalSources.size(); i++) {
            originalSource = (NbaSource) originalSources.get(i);
            originalSource.getNbaLob().deletePolicyNumber();
            originalSource.setUpdate();
        }

        //Move the transactions of matching work to original work
        List transactions = matchingCase.getNbaTransactions();
        List excludedTransactions = getExcludedTransactions(matchingCase); //ALS5199
        NbaTransaction sourceTxn = null;
        NbaTransaction newTxn = null;
        for (int i = 0; i < transactions.size(); i++) {
            sourceTxn = (NbaTransaction) transactions.get(i);
            //Begin ALS5199
            boolean flag = true;
            for (int j = 0; j < excludedTransactions.size(); j++) {
            	NbaTransaction excludedWorkItem = (NbaTransaction) excludedTransactions.get(j);
        		if (sourceTxn.getID().equalsIgnoreCase(excludedWorkItem.getID())) {
        			flag = false;
        			break;
        		}
        	}
            //End ALS5199
            if (flag) { //ALS5199
	            newTxn = sourceTxn.clone(false);
	            copyLobs(originalCase.getNbaLob(), newTxn.getNbaLob());
	            originalCase.addNbaTransaction(newTxn);
	            if (breakRelation) {
	                sourceTxn.setBreakRelation();
	                sourceTxn.setUpdate();
	            } else {
	                newTxn.getTransaction().setCreate(NbaConstants.YES_VALUE);
	            }
            } //ALS5199
        }

        //Move sources
        List sources = matchingCase.getNbaSources();
        NbaSource matchingSource = null;
        NbaSource newSource = null;
        for (int i = 0; i < sources.size(); i++) {
            matchingSource = (NbaSource) sources.get(i);
            if(!(originalCase.getNbaLob().getPortalCreated()&& matchingSource.isXML103())){ //QC8401(APSL1988)
            	newSource = matchingSource.clone(false);
                copyLobs(originalCase.getNbaLob(), newSource.getNbaLob());
                originalCase.addNbaSource(newSource);
                if (breakRelation) {
                    matchingSource.setBreakRelation();
                    matchingSource.setUpdate();
                }
            }
        }

    }

	public static void copyLobs(NbaLob sourceLobs, NbaLob destLobs) {
		destLobs.setBackendSystem(sourceLobs.getBackendSystem());
		destLobs.setCompany(sourceLobs.getCompany());
		destLobs.setOperatingMode(sourceLobs.getOperatingMode());
		//begin ALS5901
		if (sourceLobs.getPolicyNumber() == null) {
			destLobs.deletePolicyNumber();
		} else {
			destLobs.setPolicyNumber(sourceLobs.getPolicyNumber());
		}//end ALS5901
		destLobs.setPlan(sourceLobs.getPlan());
		destLobs.setProductTypSubtyp(sourceLobs.getProductTypSubtyp());
		destLobs.setLastName(sourceLobs.getLastName());
		destLobs.setFirstName(sourceLobs.getFirstName());
		destLobs.setMiddleInitial(sourceLobs.getMiddleInitial());
		destLobs.setAppState(sourceLobs.getAppState());
		destLobs.setSsnTin(sourceLobs.getSsnTin());
	}
	
	//ALS4752 New Method
	public static void mergeMatchingCaseComments(NbaCase originalCase, NbaCase matchingCase) throws NbaBaseException {
		List matchingCaseComments = matchingCase.getCase().getComments();
        for (int i = 0; matchingCaseComments != null && i < matchingCaseComments.size(); i++) {
			Comment caseComment = (Comment) matchingCaseComments.get(i);
			if (NbaConstants.COMMENTS_TYPE_GENERAL.equalsIgnoreCase(caseComment.getType())) {
				NbaGeneralComment gc = new NbaGeneralComment(caseComment);
				gc.setActionAdd();
				gc.setEnterDate(NbaUtils.getStringFromDate(new java.util.Date()));
				//Begin ALS5881
				Comment comment = gc.convertToManualComment();
				comment.setDateEntered(caseComment.getDateEntered());
				originalCase.addManualComment(comment);
				//End ALS5881
			}
		}
	}
	//ALS5199 New Method
	protected static List getExcludedTransactions(NbaCase matchingCase) throws NbaBaseException {
		List excludedTransactions = new ArrayList();
		if (!(matchingCase.getNbaLob().getProductTypSubtyp().equalsIgnoreCase("4") || matchingCase.getNbaLob().getProductTypSubtyp().equalsIgnoreCase("106"))) {
			List transactions = matchingCase.getNbaTransactions();
			for (int i = 0; i< transactions.size(); i++) {
				NbaTransaction transaction = (NbaTransaction) transactions.get(i);
					//Exclude Proposed Insured 2 NBREQRMNT transaction
					if (transaction.getNbaLob().getReqPersonCode() == NbaOliConstants.OLI_REL_COVINSURED) {
						excludedTransactions.add(transaction);
					}
			}
		}
		return excludedTransactions;
	}
	
}
