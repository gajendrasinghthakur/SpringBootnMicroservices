package com.csc.fsg.nba.bean.accessors;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.csc.fs.CloneObject;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.business.model.NbaCreditCardPaymentRelease;
import com.csc.fsg.nba.business.process.NbaAutoProcessProxy;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.business.transaction.NbaApproveContract;
import com.csc.fsg.nba.business.transaction.NbaMIBReportUtils;
import com.csc.fsg.nba.business.transaction.NbaReinsuranceUtils;
import com.csc.fsg.nba.business.transaction.NbaRequirementUtils;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNetServerDataNotFoundException;
import com.csc.fsg.nba.exception.NbaTransactionValidationException;
import com.csc.fsg.nba.foundation.NbaActionIndicator;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaPrintLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUnderwriterWorkbenchVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;

/**
 * Send Underwriter Workbench transactions to back-end systems and AWD.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA004</td><td>Version 2</td><td>Automated Process Model Support for Work Items</td></tr>
 * <tr><td>NBA007</td><td>Version 2</td><td>Adding Relation and Party object for Approval/Denial of Coverage/Benefits for Vantage contracts</td></tr>
 * <tr><td>SPR1062</td><td>Version 2</td><td>Person-level SubstandardRatings are not displayed correctly in hierarchy</td></tr>
 * <tr><td>SPR1018</td><td>Version 2</td><td>General code clean-up</td></tr>
 * <tr><td>NBA012</td><td>Version 2</td><td>Contract Extract Print</td></tr>
 * <tr><td>NBA013</td><td>Version 2</td><td>Correspondence System</td></tr>
 * <tr><td>NBA006</td><td>Version 2</td><td>Annuity support</td></tr>
 * <tr><td>NBA020</td><td>Version 2</td><td>AWD Priority</td></tr>
 * <tr><td>SPR1005</td><td>Version 2</td><td>After a adding requirements to AWD, you can not update any requirements until after refreshing the view</td></tr>
 * <tr><td>SPR1098</td><td>Version 2</td><td>Waive Requirements is Unsuccessful Without Provider Result Source</td></tr>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirement Ordering and Receipting</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database Changes</td></tr>
 * <tr><td>NBA036</td><td>Version 3</td><td>Underwriter Workbench Trx to DB</td></tr>
 * <tr><td>NBA010</td><td>Version 3</td><td>Iterative Underwriter Workbench</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>NBA079</td><td>Version 3</td><td>Underwriter Workbench Enhancement</td></tr>
 * <tr><td>NBA064</td><td>Version 3</td><td>Contract Validation</td></tr>
 * <tr><td>NBA038</td><td>Version 3</td><td>Reinsurance</td></tr>
 * <tr><td>SPR1629</td><td>Version 4</td><td>Transaction Validation</td></tr>
 * <tr><td>SPR1573</td><td>Version 4</td><td>Work item is not unsuspended on satisfying from the UW Workbench</td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * <tr><td>SPR1715</td><td>Version 4</td><td>Wrappered/Standalone Mode Should Be By BackEnd System and by Plan</td></tr>
 * <tr><td>NBA097</td><td>Version 4</td><td>Work Routing Reason Displayed</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>121/1122 Migration</td></tr>
 * <tr><td>SPR1482</td><td>Version 4</td><td>MIB Code added to the Impairment was not getting displayed in the MIB Report Tab</td></tr>
 * <tr><td>SPR1486</td><td>Version 4</td><td>MIB Codes not transmitted on clicking MIB Transmit Button</td></tr>
 * <tr><td>ACN010</td><td>Version 4</td><td>Evaluation Control Model</td></tr>
 * <tr><td>ACN022</td><td>Version 5</td><td>Re-Underwrite Project</td></tr>
 * <tr><td>ACN023</td><td>Version 5</td><td>Automatic MIB Update</td></tr>
 * <tr><td>NBA115</td><td>Version 5</td><td>Credit card payment and authorization</td></tr>
 * <tr><td>SPR2434</td><td>Version 5</td><td>Memory leak</td></tr>
 * <tr><td>SPR2636</td><td>Version 5</td><td>MISCMAIL Not Setting SYST LOB - Comments Will Not Open</td></tr>
 * <tr><td>NBA121</td><td>Version 4</td><td>Case Comments and Images on Individual Work Item</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>NBA122</td><td>Version 5</td><td>Underwriter Workbench Rewrite</td></tr>
 * <tr><td>SPR2977</td><td>Version 6</td><td>APAPPRVL autoprocess is error stopping with message "NetServer error: NBSuspend SYS0003 - FILE = SQLCODE = MISC = (SYS0003)"</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirement/Reinsurance Changes</td></tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>General Code Clean Up Issues for Version 6</td></tr>
 * <tr><td>NBA208-2</td><td>Version 7</td><td>Performance Tuning and Testing - Incremental change 2</td></tr>
 * <tr><td>NBA208-4</td><td>Version 7</td><td>Performance Tuning and Testing - Incremental change 4</td></tr>
 * <tr><td>NBA208-15</td><td>Version 7</td><td>Performance Tuning and Testing - Incremental change 15 - Avoid holding inquiry during Contract approve decline</td></tr>   
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-36</td><td>Version 7</td><td>Deferred Work Item Retrieval</td></tr>
 * <tr><td>NBA186</td><td>Version 8</td><td>nbA Underwriter Additional Approval and Referral Project</td></tr>
 * <tr><td>AXAL3.7.62</td><td>AXALife Phase 1</td><td>Amendments Endorsements</td></tr>
 * <tr><td>ALS4875</td><td>AXA Life Phase 1</td><td>QC # 4027 - Contract Validation Message appearing on the Contract Messages tab for unknown reason</td></tr>
 * <tr><td>ALS5017</td><td>AXA Life Phase 1</td><td>QC # 4181  - 3.7.31 MIB - reported codes did not transmit to MIB upon final disposition</td></tr>
 * <tr><td>ALS5344</td><td>AXA Life Phase 1</td><td>QC #4526 - MIB reported codes not being sent on final dispositionfor informals</td></tr>
 * <tr><td>AXAL3.7.10B</td><td>AXA Life Phase 2</td><td>Reinsurance</td></tr>
 * <tr><td>FNB020</td><td>AXA Life Phase 1</td><td>Performance Tuning</td></tr>
 * <tr><td>PERF-APSL410</td><td>AXA Life Phase 1</td><td>Optimize Commit Requirements</td></tr>
 * <tr><td>SR534655</td><td>Discretionary</td><td>nbA ReStart – Underwriter Approval</td></tr>
 * <tr><td>APSL692</td><td>QC#4630 Satisfy message not saving in nbA requirements</td></tr>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.support.NbaUnderwriterWorkbenchViewHelper
 * @since New Business Accelerator - Version 1
 */
public class NbaUnderwriterWorkbenchFacadeBean  {	//NBA213
    //NBA213 code deleted

    // NBA050 code deleted - method addEndorsement() deleted
    // NBA050 code deleted - method addSubstandardRatingToBenefit() deleted
    // NBA050 code deleted -  method addSubstandardRatingToCoverage() deleted
    // NBA050 code deleted -  method addSubstandardRatingToPerson() deleted
	protected NbaDst nbaDstWithAllTransactions = null; //ALS5017
	protected  NbaLogger logger = null; //APSL3028

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	//APSL3028 New Method
	protected  synchronized NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaUnderwriterWorkbenchFacadeBean.class);
			} catch (Exception e) {
				NbaBootLogger.log("NbaUnderwriterWorkbenchFacadeBean  could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
     * Apply the contract approval and final disposition to the host.
     * @param userVO the nbA user
     * @param originalHolding the holding inquiry as retrieved from a back-end system
     * @param changedHolding the holding inquiry with changes
     * @param awdDst the AWD case
     * @param newStatusKey the logical "user" key to the auto process status VP/MS model
	 * @param informalApp a boolean indiating if this is an informal application
     */
	
	
    // NBA022 - new method to add newStatusKey to the parameters
    //NBA208-4 modified return type
    //NBA208-15 removed a parameter (NbaTXLife)	
	//NBA186 - added new parameter String
	//ALS5344 - added new parameter boolean
	 public ArrayList applyApproveAndDisposition(NbaUserVO userVO, NbaTXLife changedHolding, NbaDst awdDst, String newStatusKey, String newSecondLvlDecisionQueue, boolean informalApp)
     	throws NbaBaseException {
        ArrayList result = new ArrayList(2); //NBA208-4
        try {//NBA103
            //NBA012
        	//NBA186 code deleted
            //begin ACN023
            try {
            	//retain original policy indicator as it is used by approval transaction to validate. Assuming we will not get Action Add here
                String originalPolicyActionInd = changedHolding.getPolicy().getActionIndicatorCode();
                NbaMIBReportUtils mibUtils = new NbaMIBReportUtils(changedHolding, userVO);
                mibUtils.setNbaDstWithAllTransactions(getNbaDstWithAllTransactions());//ALS5017
                awdDst = mibUtils.processMIBReportsForAContract(awdDst, true); //assume next tasks will update holding and dst
                changedHolding = mibUtils.getNbaTxLife();
                changedHolding.getPolicy().setAction(originalPolicyActionInd);
                result.add(changedHolding);//NBA208-4
                //Begin ALS5344
                if (informalApp) {
                	return result;
                }
                //End ALS5344
            } catch (Throwable t) {
                //convert all ecxeption or error including a NbaBaseException into a new NbaBaseException 
                throw new NbaBaseException("MIB report transmission cannot be created.", t);
            }
            //end ACN023
            //begin NBA186
        	
           // QC7949 code moved below
			//end NBA186
            //ALII1075 code moved below
            //APSL3491 - Code Deleted
            // TODO - NBA186 look to remove the call to NbaApproveContract.checkReinsurance
            if(! NbaUtils.isNegativeDisposition(awdDst) && !changedHolding.isSIApplication()){//ALS4417, APSL2808
	            NbaApproveContract nac = new NbaApproveContract(userVO, changedHolding, awdDst, newStatusKey, newSecondLvlDecisionQueue); // NBA022 NBA208-15 NBA186
	            //Code Deleted AXAL3.7.10B
	            nac.checkReinsurance(changedHolding, awdDst); //NBA038 //NBA208-15	            
	            nac.checkEndorsement(changedHolding);  //AXAL3.7.62
	            //APSL613 begin				
				//APSL613 end
            }
            //NBA186 code deleted
            if (!changedHolding.isSIApplication()) { // APSL2808
            	NbaCreditCardPaymentRelease.releasePayments(awdDst, userVO);//NBA115 SPR2977            	
            }
            //NBA186 code deleted	
            //NBA186 code deleted	
            //Begin ALII1075 code moved here
            //begin NBA186
        	//QC7949 start
            ApplicationInfo appInfo = changedHolding.getPolicy().getApplicationInfo();
            ApplicationInfoExtension appInfoExtension = NbaUtils.getFirstApplicationInfoExtension(appInfo);
			if (isPrintRequired() && appInfoExtension != null && appInfoExtension.getUnderwritingApproval() == NbaOliConstants.OLIX_UNDAPPROVAL_UNDERWRITER && !NbaUtils.isNegativeDisposition(awdDst)) {//ALS5552
				NbaUserVO tempUserVO = new NbaUserVO(newStatusKey, "");
				NbaAutoProcessProxy vbaAutoProcessProxy = new NbaAutoProcessProxy(awdDst); // SR534655,changed parameter of NbaAutoProcessProxy();
				vbaAutoProcessProxy.addPrintExtractTransaction(tempUserVO, awdDst, NbaConstants.PROC_APPROVAL, changedHolding); //Add Print Extract Work item
				//APSL613 begin
				if(NbaPrintLogger.getLogger().isDebugEnabled()){
					if(NbaUtils.isPrintAttachedToDst(awdDst)){
						NbaPrintLogger.getLogger().logDebug("Print attached to DST after addPrintExtractTransaction() - "+awdDst.getNbaLob().getPolicyNumber());
					}
				}
				//APSL613 end
			}			
			//QC7949 End
			//end NBA186
            //End ALII1075
        } catch (NbaBaseException e) { //NBA103
            NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
            throw e;//NBA103
        } catch (Throwable t) {//NBA103
            NbaBaseException e = new NbaBaseException(t);//NBA103
            NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
            throw e;//NBA103
        }
        return result; //NBA208-4
    }
	 
	 
	 public boolean isPrintRequired() throws NbaBaseException {
		List transactionList = getNbaDstWithAllTransactions().getNbaTransactions();
		for (int i = 1; i < transactionList.size(); i++) {
			NbaTransaction transaction = (NbaTransaction) transactionList.get(i);
			if (transaction != null && transaction.getWorkType().equalsIgnoreCase(NbaConstants.A_WT_CONT_PRINT_EXTRACT)
					&& !transaction.getNbaLob().getQueue().equalsIgnoreCase(NbaConstants.END_QUEUE) && !transaction.getNbaLob().getContractPrinted()) {
				return false;
			}
		}
		return true;
	}

    // NBA050 code deleted -  method applyClient() deleted
    // NBA050 code deleted -  method applyCoverageBenefits() methods
    // NBA050 code deleted -  method applyCWA() deleted
    //NBA213 code deleted
    /**
     * Apply the requirements to AWD.
     * @param userVO the nbA user
     * @param txLife the NbaTXlife object
     * @param nbaDst the AWD case
     * @return List contains both NbaDST object and NbaTXLife with updated action indicators
     */
    //NBA213 changed return type to List, removed impairmentsAdded, throws NbaTransactionValidationException
    public List applyRequirements(NbaUserVO userVO, NbaTXLife txLife, NbaDst nbaDst) throws NbaTransactionValidationException, NbaBaseException {
    	List results = new ArrayList(); //SPR1629, NBA213	
    	boolean dbUpdateFlag=false;//APSL3028
    	boolean awdUpdateFlag=false;//APSL3028
        try {//NBA103
            //NBA208-2 code deleted	
        	//begin PERF-APSL410
        	//revertd APSL2160 as it causes duplicate calls to update work in AWD.
            if (txLife != null) {
            	//FNB020 code deleted
                txLife = NbaContractAccess.doContractUpdate(txLife, nbaDst, userVO); //SPR1851
                
//              Begin APSL1427
                if (txLife.isTransactionError()) {
					results.add(nbaDst);
					results.add(txLife);
					return results;
				}
               //End APSL1427
            }
            //end PERF-APSL410
            dbUpdateFlag=true;//APSL3028
            // NBA004 begin
            // replace the work type and status for an add or order requirement
            java.util.List requirements = nbaDst.getNbaTransactions();
           //NBA208-4 code deleted
            HashSet unsuspendSet = new HashSet(); //SPR1851
            //begin NBA008
            boolean isAddOrOrder = false;
            NbaRequirementUtils reqUtils = new NbaRequirementUtils();
            reqUtils.setHoldingInquiry(txLife);
            reqUtils.setAutoGeneratedInd(false); //always false
            reqUtils.setEmployeeId(userVO.getUserID());
            Set requirementsId = new HashSet(); //ACN014
            //end NBA008
            for (int t = 0; t < requirements.size(); t++) {
                NbaTransaction requirement = (NbaTransaction) requirements.get(t);
                if (requirement.getID() != null) { //ACN014
                    requirementsId.add(requirement.getID()); //ACN014
                } //ACN014
                NbaLob lob = requirement.getNbaLob(); //NBA008
                RequirementInfo reqInfo = txLife.getRequirementInfo(lob.getReqUniqueID()); //NBA130 //ALS4081
                if (requirement.isAction(NbaActionIndicator.ACTION_ADD) || requirement.isAction(NbaActionIndicator.ACTION_ORDER)) {
                    WorkItem transaction = requirement.getTransaction();  //NBA208-32
                    // create a user value object and set the original work type as the user id
                    NbaUserVO user = new NbaUserVO();
                    user.setUserID(transaction.getWorkType());
                    // retrieve the work type and initial status from VP/MS and then
                    // replace the original values for the transaction
                    NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(user, nbaDst, txLife, new HashMap()); //NBA008	NBA208-4
                    transaction.setWorkType(provider.getWorkType());
                    transaction.setStatus(provider.getInitialStatus());
                    NbaUtils.setRouteReason(requirement, requirement.getStatus());//NBA097
                    requirement.increasePriority(provider.getWIAction(), provider.getWIPriority()); //NBA020 AWD Priority
                    //begin NBA008				
                    reqUtils.setReqPersonCodeAndSeq(lob.getReqPersonCode(), lob.getReqPersonSeq());
                    reqUtils.setReqType(lob.getReqType());
                    //update LOBs
                    reqUtils.processRequirementWorkItem(nbaDst, requirement); //ACN014  
                    isAddOrOrder = true;
                    requirement.getTransaction().setLock("Y");  //NBA208-32
                    //NBA208-4 code deleted				
                    //begin SPR1098		
                } else if (requirement.isAction(NbaActionIndicator.ACTION_UPDATE) && lob.getReqStatus() != null) {
                    if (lob.getReqStatus().equals(Long.toString(NbaOliConstants.OLI_REQSTAT_WAIVED))) { //NBA079
                    	NbaProcessStatusProvider provider = new NbaProcessStatusProvider(new NbaUserVO(NbaConstants.PROC_VIEW_REQUIREMENT_CANCEL, ""), nbaDst, txLife); //SPR1715 //SPR2639
                    	//ALS4081 code moved outside the block
                        if (null != reqInfo && reqInfo.hasRequestedDate()) { //NBA130
                            requirement.setStatus(provider.getPassStatus());
                        } else {
                            requirement.setStatus(provider.getOtherStatus());
                        }
                        requirement.increasePriority(provider.getWIAction(), provider.getWIPriority());
                        //end SPR1098	
                        //SPR1851 code deleted
                        NbaUtils.setRouteReason(requirement, requirement.getStatus());//NBA097					
                        unsuspendSet.add(requirement.getID()); //SPR1851
                     } else if (lob.getReqStatus().equals(Long.toString(NbaOliConstants.OLI_REQSTAT_RECEIVED))//NBA079
                            || lob.getReqStatus().equals(Long.toString(NbaOliConstants.OLI_REQSTAT_COMPLETED))) { //ACN010
                        //ACN010 Begin					
                        NbaProcessStatusProvider provider;
                        NbaDst tempDst = null;
                        NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO(); //NBA208-2
                        retOpt.setWorkItem(requirement.getID(), false); //NBA208-2
                        tempDst = WorkflowServiceHelper.retrieveWorkItem(userVO, retOpt); //NBA208-2
                        if (lob.getReqStatus().equals(Long.toString(NbaOliConstants.OLI_REQSTAT_RECEIVED))) {
                            provider = new NbaProcessStatusProvider(new NbaUserVO(NbaConstants.PROC_VIEW_REQUIREMENT_SATISFY, ""), tempDst, txLife, reqInfo); //SPR2639 NBA130 //ALS4081
                        } else {
                            //NBA130 code deleted 
                            provider = new NbaProcessStatusProvider(new NbaUserVO(NbaConstants.PROC_VIEW_REQUIREMENT_COMPLETED, ""), tempDst, txLife, reqInfo); //SPR2639 //ALS4081
                        }
                        //ACN010 end
                        requirement.setStatus(provider.getPassStatus()); //NBA079					
                        requirement.increasePriority(provider.getWIAction(), provider.getWIPriority()); //NBA079
                        //APSL692 code deleted.
                        //SPR1851 code deleted
                        unsuspendSet.add(requirement.getID()); //SPR1851
                    } 
                    NbaUtils.setRouteReason(requirement, requirement.getStatus());//NBA097
                   
                }
            }
            //ACN010 Code deleted		
            //SPR1851 code deleted
            NbaDst retCase = WorkflowServiceHelper.update(userVO, nbaDst); //SPR1851
            awdUpdateFlag=true;//APSL3028
            // NBA004 deleted line
            //NBA208-4 code deleted
            List requirementsTransactions = retCase.getNbaTransactions(); //NBA208-4
              
            boolean isUpdateNeeded = false;//NBA208-4
            if (isAddOrOrder) { //NBA208-4
                for (int j = 0; j < requirementsTransactions.size(); j++) {
                    NbaTransaction nbaTransaction = (NbaTransaction) requirementsTransactions.get(j);
                    if (nbaTransaction.getTransaction().getWorkType().equals(NbaConstants.A_WT_REQUIREMENT)) {
                        if (!requirementsId.contains(nbaTransaction.getID())) {
                            //always true for req determination
                            NbaLob lob = nbaTransaction.getNbaLob();
                            reqUtils.setAutoGeneratedInd(true);
                            reqUtils.setEmployeeId(nbaTransaction.getTransaction().getWorkType());
                            reqUtils.setReqPersonCodeAndSeq(lob.getReqPersonCode(), lob.getReqPersonSeq());
                            reqUtils.setReqType(lob.getReqType());
                            reqUtils.addRequirementControlSource(nbaTransaction);
                            reqUtils.addMasterRequirementControlSource(retCase, nbaTransaction);//NBA208-4
                            isUpdateNeeded = true;//NBA208-4
                            awdUpdateFlag=false;//APSL3028
                        }
                    }
                }//ACN014 ends
            }
            //Begin ALS2611
            //PERF-APSL410 code reverted to original place
            if (isUpdateNeeded) {
                retCase = WorkflowServiceHelper.update(userVO, retCase);//NBA208-4 PERF-APSL410
                awdUpdateFlag=true;//APSL3028
            }
            //End ALS2611
            //End ALS2611
            results.add(retCase); //NBA208-4
            results.add(txLife);
            //NBA213 code deleted		
            results.add(unsuspendSet); //SPR1851
        } catch (NbaTransactionValidationException ntve) {  //NBA213
        	throw ntve;  //NBA213
        } catch (Exception e) {
            NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
            try {
                java.util.List requirements = nbaDst.getNbaTransactions();
                for (int t = 0; t < requirements.size(); t++) {
                    NbaTransaction requirement = (NbaTransaction) requirements.get(t);
                    if (!requirement.isActionDisplay()) {
                        requirement.setActionFailed();
                    }
                }
                //NBA213 deleted code
            } catch (Throwable t) {
                NbaLogFactory.getLogger(this.getClass()).logException(t);//NBA103
                throw new NbaBaseException("Exception while applying requirement");//ALS2160
            }
            getLogger().logDebug(e.getMessage());//APSL3028
            throw new NbaBaseException("Exception while applying requirement:" + e.getMessage());//ALS2160
        }
        //Begin APSL3028
        finally{
        	if(!(dbUpdateFlag && awdUpdateFlag)){
        		if(txLife!=null){
        			getLogger().logDebug("PolicyNo:"+txLife.getPolicy().getContractKey());
        		}
        		if(dbUpdateFlag==false){
        			getLogger().logDebug("-----------Database update failed----------");
        		}else if(awdUpdateFlag==false){
        			getLogger().logDebug("-----------Awd update failed---------------");
        		}
        	}
        	
        }
        //End APSL3028
        return results; //NBA208-4
    }

    //SPR1486 code deleted
    /**
     * Transmit MIB reports to the MIB.
     * @param userVO the nbA user
     * @param uwVo the underwriter workbench value object.
     * @return the underwrite workbech value object with updated components
     */
    //SPR1486 New Method
    public NbaUnderwriterWorkbenchVO transmitMIBReports(NbaUserVO userVO, NbaUnderwriterWorkbenchVO uwVo) throws NbaBaseException {
        try {
            NbaTXLife nbaTxLife = uwVo.getNbATXLife();
            nbaTxLife.setBusinessProcess(NbaConstants.PROC_UW); //ALS4875
            NbaDst nbaDst = uwVo.getNbaDst();
            if (NbaConfiguration.getInstance().isUnderwriterWorkbenchApplet()) { //NBA122
                nbaTxLife = NbaContractAccess.doContractUpdate(nbaTxLife, nbaDst, userVO);
            } //NBA122
            NbaTransaction mibReq = uwVo.getNbaTransaction(); //NBA208-36
            NbaMIBReportUtils mibUtils = new NbaMIBReportUtils(nbaTxLife, userVO); //ACN023	
            //ACN009 code deleted if (!validateTransaction(mibReq)) {
            //	throw new NbaBaseException("Could not transmit reports. MIB Requirement is not originally ordered");
            //}
            //ACN023 code deleted	
            NbaDst clonedDst = getClonedDst(nbaDst); // ACN023 NBA208-36 Only the Case 
            mibUtils.processMIBReportsForAMIBTransaction(clonedDst, mibReq, false); //ACN023
            commitTransactions(userVO, clonedDst); //NBA208-36
            //ACN023 code deleted
            nbaTxLife = NbaContractAccess.doContractUpdate(mibUtils.getNbaTxLife(), nbaDst, userVO); //ACN023
            uwVo.setNbaDst(nbaDst);
            uwVo.setNbATXLife(nbaTxLife);
            return uwVo;
        } catch (NbaBaseException e) {
            NbaLogFactory.getLogger(this.getClass()).logException(e);
            throw e;
        } catch (Throwable t) {
            NbaBaseException e = new NbaBaseException(t);
            NbaLogFactory.getLogger(this.getClass()).logException(e);
            throw e;
        }
    }

    //ACN023 moved getReportsToTransmit, processMIBReportForImpairmentMIBCodes and prepareReportToTransmit to NbaMIBReportUtils
 	//NBA208-36 code deleted

    /**
     * This method validate the transaction. Return false if it failed validation
     * else retiurn true.
     * @param transaction the transaction object
     * @return true for successful validation.
     */
    //SPR1486 New Method
    protected boolean validateTransaction(NbaTransaction transaction) {
        List sources = transaction.getNbaSources();
        for (int r = 0; r < sources.size(); r++) {
            NbaSource suppSource = (NbaSource) sources.get(r);
            if (suppSource.getSource().getSourceType().equals(NbaConstants.A_ST_PROVIDER_TRANSACTION)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Search and return a transaction that matched the transactionId.
     * @param nbaDst the nbadst value object
     * @param transactionId the transaction id to match
     * @return the matched transaction object.
     * @throws NbaNetServerDataNotFoundException
     */
    //SPR1486 New Method
    protected NbaTransaction getNbaTransaction(NbaDst nbaDst, String transactionId) throws NbaNetServerDataNotFoundException {
        if (transactionId != null && nbaDst != null) {
            List transactions = nbaDst.getNbaTransactions();
            for (int t = 0; t < transactions.size(); t++) {
                NbaTransaction transaction = (NbaTransaction) transactions.get(t);
                if (transactionId.equals(transaction.getID())) {
                    return transaction;
                }
            }
        }
        return null;
    }

    //NBA208-36 code deleted

    /**
     * This method will update original dst from cloned dst
     * with information updated during trasmitting mib reports.
     * @param nbaDst the original dst
     * @param mibReq the requirement work
     * @param the cloned dst
     * @throws NbaBaseException
     */
    //SPR1486 New Method
    protected void updateOriginalDstFromClone(NbaDst nbaDst, NbaTransaction mibReq, NbaDst clonedDst) throws NbaBaseException {
		//begin NBA208-32
        WorkItem caseDst = nbaDst.getCase();
        WorkItem clonedCaseDst = clonedDst.getCase();
        for (int i = caseDst.getWorkItemChildren().size() - 1; i >= 0; i--) {
            if (mibReq.getID().equals(((WorkItem) caseDst.getWorkItemChildren().get(i)).getItemID())) {
                for (int j = clonedCaseDst.getWorkItemChildren().size() - 1; j >= 0; j--) {
                    if (mibReq.getID().equals(((WorkItem) clonedCaseDst.getWorkItemChildren().get(i)).getItemID())) {
					((WorkItem)caseDst.getWorkItemChildren().get(i)).setSourceChildren(((WorkItem)clonedCaseDst.getWorkItemChildren().get(j)).getSourceChildren());
		//end NBA208-32
                        break;
                    }
                }
                break;
            }
        }
    }

    //NBA213 code deleted

    /**
     * Commit the new Transactions to the workflow system
     * @param userVO the user VO
     * @param clonedDst the DST containing the original Case and new Transactions
     * @throws NbaBaseException
     */
    //NBA208-36 New Method
    protected void commitTransactions(NbaUserVO userVO, NbaDst clonedDst) throws NbaBaseException {
        Iterator it = clonedDst.getNbaTransactions().iterator();
        NbaTransaction nbaTransaction;
        NbaLob nbaLob;
        NbaDst newWorkNbaDst;
        while (it.hasNext()){
            nbaTransaction = (NbaTransaction) it.next();
            nbaLob = nbaTransaction.getNbaLob();
            nbaLob.setTypeCase(false);
            newWorkNbaDst = WorkflowServiceHelper.createCase(userVO, nbaTransaction.getBusinessArea(), nbaTransaction.getWorkType(), nbaTransaction.getStatus(), nbaLob);
            WorkflowServiceHelper.unlockWork(userVO, newWorkNbaDst);
        }
    }
    
    /**
     * Create a clone dst of original dst containing only the Case
     * @param nbaDst the original dst
     * @return the cloned dst
     * @throws NbaBaseException
     */
    //NBA208-36 New Method
    protected NbaDst getClonedDst(NbaDst nbaDst) throws NbaBaseException {
        NbaDst clonedDst = null;
        try {
            clonedDst = new NbaDst();
            CloneObject.clone(nbaDst, clonedDst);
        } catch (Exception e) {
            throw new NbaBaseException("Invalid Dst", e);
        }
        clonedDst.getTransactions().clear();
        return clonedDst;
    }
    /**
	 * @return Returns the nbaDstWithAllTransactions.
	 */
	//ALS5017 New Method
	public NbaDst getNbaDstWithAllTransactions() {
		return nbaDstWithAllTransactions;
	}
	/**
	 * @param nbaDstWithAllTransactions The nbaDstWithAllTransactions to set.
	 */
	//ALS5017 New Method
	public void setNbaDstWithAllTransactions(NbaDst nbaDstWithAllTransactions) {
		this.nbaDstWithAllTransactions = nbaDstWithAllTransactions;
	}

}
