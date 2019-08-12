package com.csc.fsg.nba.business.process;

/*
 * ************************************************************** <BR>
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
 * ************************************************************** <BR>
 */
import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.contract.validation.NbaContractValidation;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaRequirementInfoException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaFundsData;
import com.csc.fsg.nba.vo.AxaReinsuranceCalcVO;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaHolding;
import com.csc.fsg.nba.vo.NbaManualCommentType;
import com.csc.fsg.nba.vo.NbaNoteComment;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.nbaschema.SecureComment;
import com.csc.fsg.nba.vo.txlife.Activity;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.CovOptionExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.ImpairmentInfo;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.LifeParticipantExtension;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.ReinsuranceInfo;
import com.csc.fsg.nba.vo.txlife.ReinsuranceInfoExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vo.txlife.SuspendInfo;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;
import com.csc.fsg.nba.vo.txlife.TentativeDisp;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.Messages;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * NbaProcAutoUnderwriting is the class that processes nbAccelerator cases found
 * on the AWD automated underwriting queue (NBAUUND). It invokes the VP/MS
 * Automated Underwriting model to determine if the case can be automatically
 * underwritten.
 * <p>The NbaProcAutoUnderwriting class extends the NbaAutomatedProcess class.  
 * Although this class may be instantiated by any module, the NBA polling class 
 * will be the primary creator of objects of this type.
 * <p>When the polling process finds a case on the Automated Underwriting queue, 
 * it will create an object of this instance and call the object's 
 * executeProcess(NbaUserVO, NbaDst) method.  This method will manage the steps 
 * necessary to submit a case to a VP/MS model to determine if the case is 
 * eligible for automated underwriting.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>SPR1018</td><td>Version 2</td><td>JavaDoc, comments and minor source code changes.</td></tr>
 * <tr><td>SPR1050</td><td>Version 2</td><td>Route case to error queue if severity error not allowed to send case for auto underwriting</td></tr>
 * <tr><td>NBA021</td><td>Version 2</td><td>Data Resolver</td></tr> 
 * <tr><td>NBA004</td><td>Version 2</td><td>VP/MS Model Support for Work Items Project</td></tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA035</td><td>Version 3</td><td>Application Submit to nba Pending DB</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>NBA059</td><td>Version 3</td><td>Jet Suitability</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture changes</td></tr>
 * <tr><td>NBA010</td><td>Version 3</td><td>Hooks for Iterative Underwriting</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>SPR1752</td><td>Version 4</td><td>Remove Check for Severe Errors from NBAUUND</td></tr>
 * <tr><td>SPR1778</td><td>Version 4</td><td>Correct problems with SmokerStatus and RateClass in Automated Underwriting, and Validation.</td></tr>
 * <tr><td>NBA106</td><td>Version 4</td><td>Storage of sensitive vpms result data changed</td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * <tr><td>NBA095</td><td>Version 4</td><td>Queues Accept Any Work Type</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACP010</td><td>Version 4</td><td>ac Automated Underwriting</td></tr>
 * <tr><td>NBA110</td><td>Version 4</td><td>Vntg issue to admin</td></tr>
 * <tr><td>ACN008</td><td>Version 4</td><td>Underwriting Workflow Changes</td></tr>
 * <tr><td>SPR2557</td><td>Version 5</td><td>the message is being added to the attachment data table again each time the case is unsuspended</td></tr>
 * <tr><td>SPR2619</td><td>Version 5</td><td>Automated underwriting failing for wrong reason for Annuity </td></tr>
 * <tr><td>SPR1753</td><td>Version 5</td><td>Automated Underwriting and Requirements Determination Should Detect Severe Errors for Both AC and Non - AC</td></tr>
 * <tr><td>SPR2699</td><td>Version 5</td><td>Cases are being suspended erroneously.</td></tr>
 * <tr><td>NBA122</td><td>Version 5</td><td>Underwriter Workbench Rewrite</td></tr>
 * <tr><td>SPR2758</td><td>Version 6</td><td>Auto Underwriting process is suspending the work item for 2 days irrespective of the maximum suspend days value in the model.</td></tr>
 * <tr><td>NBA132</td><td>Version 6</td><td>Equitable Distribution of Work</td></tr>
 * <tr><td>SPR3014</td><td>Version 6</td><td>Substandard Extra Ratings Deleted by Auto Underwriting After Retrieved from Backend System</td></tr>
 * <tr><td>SPR2689</td><td>Version 6</td><td>Underwriter Workbench, Client, Rate Classification values displayed incorrectlys</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR3229</td><td>Version 8</td><td>Proposed substandard ratings are not getting stored for Covoption by NBAUUND process in AC Environment</td></tr>
 * <tr><td>AXAL3.7.07</td><td>AXA Life Phase 1</td><td>Auto Underwriting</td></tr>
 * <tr><td>NBA251</td><td>Version 8</td><td>nbA Case Manager and Companion Case Assignment</td></tr>
 * <tr><td>NBA224</td><td>Version 8</td><td>nbA Underwriter Workbench Requirements and Impairments Enhancement<td><tr>
 * <tr><td>NBA225</td><td>Version 8</td><td>nbA Comments<td><tr>
 * <tr><td>ALPC19AA</td><td>AXA Life Phase 1</td><td>Auto Approval<td><tr>
 * <tr><td>NBA223</td><td>Version 8</td><td>Underwriter Final Disposition</td></tr>
 * <tr><td>NBA300</td><td>AXA Life Phase 2</td><td>Term Conversion</td></tr>
 * <tr><td>SR494086.5</td><td>Discretionary</td><td>ADC Retrofit</td></tr>
 * <tr><td>P2AXAL053</td><td>AXA Life Phase 2</td><td>R2 Auto Underwriting</td></tr>
 * <tr><td>SR657319</td><td>Discretionary</td><td>Manual selection of Rate Class</td></tr>
 * <tr><td>SR564247(APSL2525)</td><td>Discretionary</td><td>Predictive Full Implementation</td></tr>
 * <tr><td>NBA323</td><td>Version NB-1301</td><td>nbA Comments Improvements Retrofit from Base</td></tr>
 * <tr><td>APSL4635</td><td>Discretionary</td><td>Term Conversion Underwriting rate conversion </td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 * @see NbaAutomatedProcess
 */

public class NbaProcAutoUnderwriting extends NbaAutomatedProcess {
	private NbaOinkDataAccess oinkData;
	// ACP010 starts 
	public NbaSuspendVO suspendVO;
	private NbaOinkRequest oinkRequestGlobal = null;
	private String backEndKey = "";
	private String companyKey = "";
	private String contractNo = "";
	private String PartyID = "";
	boolean passIndicator = true;
	boolean outstandingImpairmentsIndicator = false;
	boolean outstandingRequirementsIndicator = false;
	//NBA132 code deleted
	private final static String SUSPENDED = "SUSPENDED";
	VpmsModelResult vpmsModelResultGlobal = new VpmsModelResult();
	HashMap coverageExtensionMap = new HashMap();
	HashMap personExtensionMap = new HashMap();
	HashMap coverageExtensionProposedRateClassMap = new HashMap();
	//SPR2758 code deleted
	private int maximumSuspendDuration = 0; //SPR2758
	private Date suspendActivationDate = null; //SPR2758
	String reason = "";
	protected NbaDst parentCase; //AXAL3.7.07
	boolean reEvalTransaction; //ALS5260
	boolean caseSuspended; //ALS5260
	//ALII1981 - Removed Code //ALS5260
	// ACP010 Ends 
	boolean removeTconvMsg = false; //APSL4635
	private final static int TCONV_MSG_CODE = 1790;
	boolean giApplication = false; //NBLXA-188
	
		/**
	 * This constructor calls the superclass constructor which will set
	 * the appropriate statues for the process.
	 */
	public NbaProcAutoUnderwriting() {
		super();
	}

	/**
	 * This method drives the automated underwriting process.
	 * <P>After obtaining a reference to the <code>NbaNetServerAccessor</code> EJB, 
	 * it retrieves the Holding Inquiry and uses it to update the case's 
	 * LOB fields. Those LOB fields are then used to instantiate the
	 * <code>NbaVpmsVO<code>. Then the XML103 object is retrieved and it 
	 * and the holding inquiry are linked to the NbaVpmsVO object.  The
	 * NbaVpmsVO object is passed to the passed to the underwriteCase(NbaVpmsVO)
	 * method that handles setting up and executing the call to the VP/MS model.
	 * The result of that call is an NbaVpmsAutoUnderwritingData object that 
	 * contains the results of the VP/MS invocation.
	 * <P>Prior to instantiating the NbaVpmsVO object, the method will 
	 * interrogate the Holding Inquiry to determine if it is eligible for 
	 * automated processing.  If it finds that the error severity is too 
	 * severe, then the case will fail automated underwriting and failure 
	 * processing will occur.  Otherwise, the case will be sent into VP/MS 
	 * to determine if it can be automatically underwritten.
	 * <P>If the case passes automated underwriting, the status is changed to 
	 * the pass status to route the case to the next queue. If automated 
	 * underwriting fails, errors in the form of AWD comments indicating the 
	 * reason for failure are generated and added to AWD.  The status is 
	 * changed to the fail status to route the case to the next queue.
	 * <P>In either case, the process creates an NbaAutomatedProcessResult object 
	 * that will be returned to the polling process to indicate success or 
	 * failure of the Automated Underwriting process.  Finally, the changes are 
	 * committed to AWD.
	 * @param user  the user for whom the work was retrieved
	 * @param work  the AWD case to be reviewed
	 * @return NbaAutomatedProcessResult containing the results of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
		boolean debugLogging = getLogger().isDebugEnabled(); // NBA027
		boolean routeNeeded = true; //APSL3782
		//NBA132 code deleted
		// NBA095 code deleted
		if (!initializeWithoutStatus(user, work)) { //NBA132
			return getResult(); // NBA050
		}

		setBackEndKey(getWorkLobs().getBackendSystem()); //NBA132
		setCompanyKey(getWorkLobs().getCompany()); //NBA132
		setContractNo(getWorkLobs().getPolicyNumber()); //NBA132
		try { //NBA095
			if (debugLogging) { // NBA027
				getLogger().logDebug("AutoUnderwriting contract " + getContractNo()); //NBA132
			} // NBA027
			// begin AXAL3.7.07
			boolean isTransaction = work.isTransaction();
			//APSL3782 start
			if (!isTransaction) {
			    AxaPreventProcessVpmsData preventProcessData = new AxaPreventProcessVpmsData(user, getNbaTxLife(), getVpmsModelToExecute());
			    if (preventProcessData.isPreventsProcess()) {
			        routeNeeded = false;
			    }
            }
			//APSL3782 END
			//Start NBLXA-188
			if(NbaOliConstants.OLI_APPTYPE_GROUPAPP == getNbaTxLife().getPolicy().getApplicationInfo().getApplicationType()){
				giApplication = true;
			}
			//End NBLXA-188
			
			setReEvalTransaction(isTransaction && A_WT_REEVALUATE.equals(work.getTransaction().getWorkType()));//ALS5260
			//ALS5428 begin
			if(isReEvalTransaction()){	
				removeErrorMessage();
			}	
			//end ALS5428
			retreiveWorkFromAWD(); //NBA224
			// code deleted NBA224
			if (!giApplication && routeNeeded && NbaConstants.STANDALONE.equals(getWorkLobs().getOperatingMode())) { //NBA132//APSL3782
				new NbaContractValidation().validate(getNbaTxLife(), work, user); //NBA132
			} //NBA132
			//end AXAL3.7.07
			initializeStatusFields(); //NBA132
			// NBA050 CODE DELETED
			//NBA224 CODE DELETED
			//Begin AXAL3.7.40G
			if (!isTransaction) {//ALS1758
				NbaAutomatedProcessResult preventProcessingResult = doPreventProcessing(routeNeeded);//APSL3782
				if (preventProcessingResult != null) {
					return preventProcessingResult;
				}
			}
			//End AXAL3.7.40G
			// NBA050 CODE DELETED
			VpmsModelResult vpmsModelResult = null;
			if (!giApplication) {//NBLXA-188
			oinkData = new NbaOinkDataAccess();
			oinkData.setAcdbSource(new NbaAcdb(), nbaTxLife);
			oinkData.setLobSource(work.getNbaLob());
			oinkData.setContractSource(nbaTxLife, getWork().getNbaLob()); //NBA035 set oink with 203, not merged 203/103 & NBA050
			// SPR1752 code deleted
			// ACP010 Begins
			nbaOLifEId = new NbaOLifEId(nbaTxLife);
			// Begin NBA130
				
			try {
				vpmsModelResult = underwriteCase();
			} catch (NbaBaseException nbe) {
				if (nbe instanceof NbaRequirementInfoException) {
					addComment(nbe.getMessage());
					setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Automated Underwriting Failed", NbaConfiguration.getInstance().getNetserverDefaultStatus("HostError")));
				} else {
					throw nbe;
				}
			}
			// End NBA130
			if (getResult() == null) { // not a database update error //SPR2399, AXAL3.7.07, NBA224
				//Start APSL2536
				if(!NbaUtils.isAdcApplication(work) && passIndicator == true){
					passIndicator = updateAndCheckReinsuranceCalculcationLimits(nbaTxLife);	
				} 
				//End APSL2536
                if (passIndicator == true) {
                    if (debugLogging) { // NBA027
                        getLogger().logDebug("Passed Underwriting: change status to " + getPassStatus());
                    } // NBA027
                    //ALS5415 code deleted/moved
                    if (nbaTxLife.getPolicy().getIssueType() == NbaOliConstants.OLI_COVISSU_REDUCEDUNDERWRITING) { //SR564247(APSL2525),ALII1605                                           	
                    	updateFieldsForJetPredictiveAutoApprovedCase(); // SR657319
						createActivityForInitialReview(); // APSL4980
                    }
                    //begin ALS5415
                    if (isTransaction) {
                    	unsuspendCase();
                    } else {
                    	getWork().getNbaLob().setPassedAutoUnd(true);
                        if (getWork().getNbaLob().getFailedAutoUnd()) { //ALS5428
                            getWork().getNbaLob().setFailedAutoUnd(false);//ALS5428
                        }
                        if(!NbaUtils.isTermConvOPAICase(nbaTxLife) && NbaUtils.isAdcApplication(work)) { //ALII1288,ALII1304, For TermConvOPAI case, PassInd will be returned as TRUE from VPMS model and we don't want to set UnderwritingApproval=2 at this point, this should get set in NbaProcApproval 
                        	//Begin SR494086.5
                        	ApplicationInfoExtension appext = NbaUtils.getFirstApplicationInfoExtension(nbaTxLife.getNbaHolding().getApplicationInfo());
							appext.setUnderwritingApproval(NbaOliConstants.NBA_TENTATIVEDISPOSITION_APPROVED);
							appext.setActionUpdate();
							nbaTxLife.getNbaHolding().getApplicationInfo().setHOCompletionDate(new Date());
							//End SR494086.5
                        }										// SR564247(APSL2525)
                        createTentativeDisp(); //ALPC19AA
                        addReinsuranceInfo();//ALS5273
                    }
                    //end ALS5415
                    //ALS5084 code deleted
                    result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus());
                    //ALII1981 - Removed Code //ALS5260
                } else {
                    getWork().getNbaLob().setFailedAutoUnd(true);
                    getWork().getNbaLob().setPassedAutoUnd(false); //ALII1785
                     //SR564247,Deleted Code(ALII1605)
                    updateFields(vpmsModelResult, personExtensionMap, coverageExtensionMap); //ALS4884                                      
                    if (outstandingImpairmentsIndicator == false && outstandingRequirementsIndicator == true) { //ALS5592

                        //Begin ALS5260, ALS5415, ALS5428
                        if (!isTransaction ) { //If ReEval WI
                            result = suspendCase(vpmsModelResult); 
                            if (result != null && SUSPENDED.equals(result.getStatus())) { //SPR2399
                                return result;
                            }
                        }
                        //End ALS5260, ALS5415, ALS5428
					
                    }
                    doFailProcessing(vpmsModelResult, reason); //SPR2557
                    //ALS5084 code deleted
                    result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Failed", getFailStatus()); //NBA132//NBA251
                    //ALII1981 - Removed code //ALS5260 

                }
                //Start ALII1981
                Map deOinkMap = new HashMap();               
                deOinkMap.put("A_PassInd",String.valueOf(passIndicator));
                NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(getUser(), getWork(),getNbaTxLife(),deOinkMap);
                setReason(statusProvider.getReason());
                //End ALII1981
				}
			}else{// Start NBLXA-188
				Map deOinkMap = new HashMap();               
                NbaProcessStatusProvider statusProvider = new NbaProcessStatusProvider(getUser(), getWork(),getNbaTxLife(),deOinkMap);
                setReason(statusProvider.getReason());
				result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Failed", getFailStatus());
            }//End NBLXA-188
			//SPR1851 code deleted
			//End NBA106
			// SPR1752 code deleted
			// ACP010 Code deleted.
			// ACP010 Ends
			// begin AXAL3.7.07
			
			//ALS4314 code deleted
			
			//begin ALS5084
			if(isReEvalTransaction()){//ALS5260 removed code	
				removeTconvSystemMessage(); //APSL4635
				updateReEvaluateWorkItem(vpmsModelResult); //NBA224
			}
			
			//ALS5260 removed code	
			nbaTxLife = doContractUpdate();
			handleHostResponse(nbaTxLife);
			//end ALS5084 
			
			//ALS5260 removed code	
			if (updateCase(isTransaction)) {  //ALS5415
				changeStatus(result.getStatus(), getReason());//ALS5260 ALS5415
			} //ALS5415
			//ALS5260 removed code	
			
			// NBA224 code delted
			//NBA251 Code deleted
			
			doUpdateWorkItem(); //SPR2699
			
			//NBA251 Code deleted
			// end AXAL3.7.07
			if (debugLogging) { // NBA027
				getLogger().logDebug("AutoUnderwriting for contract " + getWork().getNbaLob().getPolicyNumber() + " completed");
			} // NBA027
			return result;
		} catch (java.rmi.RemoteException re) {
			NbaBaseException nbe = new NbaBaseException("AutoUnderwriting problem", re);
			throw nbe;
		}
		 finally {
				setWork(getOrigWorkItem()); // Begin APSL4376
			}
	}

	/**
	 * update fields for an auto approved jet recommended predicitive case 
	 */
	// SR657319 New method
	private void updateFieldsForJetPredictiveAutoApprovedCase() {
		int partyCount = nbaTxLife.getOLifE().getPartyCount();
		for (int k = 0; k < partyCount; k++) {
			Party party = nbaTxLife.getOLifE().getPartyAt(k);
			if (nbaTxLife.isInsured(party.getId())) {
				Person person = party.getPersonOrOrganization().getPerson();
				PersonExtension personExt = NbaUtils.getFirstPersonExtension(person);
				if (personExt != null) {
					personExt.setApprovedRateClass(personExt.getRateClass());
					personExt.setActionUpdate();
					//BEGIN: APSL4845
					if(!NbaUtils.isBlankOrNull(personExt.getRateClass()) &&
							!NbaUtils.isBlankOrNull(personExt.getRateClassAppliedFor())){
						if (!personExt.getRateClass().equalsIgnoreCase(personExt.getRateClassAppliedFor())) {
							NbaDst work = getWork();
							work.getNbaLob().setIssueOthrApplied(true);
						}
					}
					//END: APSL4845
				}
			}
		}
	}

	/**
	 * Find and update pass status on Re-Evaluate Work Item
	 * VpmsModelResult vpmsModelResult
	 * boolean debugLogging
	 * @throws NbaBaseException
	 */
	//AXAL3.7.07 New Method, NBA224 Method signature changed
	protected void updateReEvaluateWorkItem(VpmsModelResult vpmsModelResult) throws NbaBaseException {
		// code deleted NBA224
		// Code deleted NBA224
		//ALS5084 code deleted
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Automate underwriting for Re-Evaluate WorkItem completed");
		}
		ListIterator transList = getWork().getNbaTransactions().listIterator();
		NbaTransaction nbaTrans = null;
		String origWorkItemId = getOrigWorkItem().getID();
		NbaDst parentCase = getWork();
		setWork(getOrigWorkItem());
		initializeStatusFields();
		while (transList.hasNext()) {
			nbaTrans = (NbaTransaction) transList.next();
			if (A_WT_REEVALUATE.equals(nbaTrans.getTransaction().getWorkType()) && origWorkItemId.equals(nbaTrans.getID())) {
				nbaTrans.setStatus(getPassStatus());
				if(!parentCase.getNbaLob().getQueue().equalsIgnoreCase(getOrigWorkItem().getNbaLob().getQueue())){ //ALS5428
				    setRouteReason(nbaTrans, getPassStatus());
				    //ALS5260 removed result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getPassStatus());//ALS3710
				    setReason(getRouteReason());//ALS5260
				}    
				break;
			}
		}
		setWork(parentCase);
	}

	//ALS5252 code refactored to parent class

	/**
	 * Answer the awd case and sources 
	 * @return NbaDst which represent a awd case
	 */
	// AXAL3.7.07 New Method
	protected NbaDst getParentCase() throws NbaBaseException {
		if (parentCase == null) {
			//NBA213 deleted code
			//create and set parent case retrieve option
			NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
			retOpt.setWorkItem(getWork().getID(), false);
			retOpt.requestCaseAsParent();
			retOpt.requestSources();
			retOpt.requestTransactionAsSibling();//SPR2544
			retOpt.setLockWorkItem();
			retOpt.setLockParentCase();
			retOpt.setAutoSuspend();
			//get case from awd
			parentCase = retrieveWorkItem(getUser(), retOpt); //NBA213
			//NBA213 deleted code
		}
		return parentCase;
	}

	/**
	 * This Method Updates all the Fields in the Database Returned by Model, This Method gets called only one time and 
	 * updates all the coverages on the contract.
	 * @param  VpmsModelResult vpms containing coverage related values for all the coverages on the contract.
	 * @param  HashMap coverageExtMap Map Containing all the CovOption Objects Returned by Model , Key is coverage_id
	 * @param  HashMap personExtensionMap Map Containg Rate Class values , Key is partyId,
	 * @return void
	 * @throws NbaBaseException
	 */
	public void updateFields(VpmsModelResult vpms, HashMap personExtensionMap, HashMap coverageExtMap) throws NbaBaseException {
		OLifE olife = nbaTxLife.getOLifE();
		Party party = null;
		//NBA132 code deleted
		int partyCount = 0;
		//NBA132 code deleted
		Person person = null;
		int MAX_CASE_AGE_SEVENTEEN = 17; //APSL5143
		if (olife != null) {
			partyCount = olife.getPartyCount();
			for (int k = 0; k < partyCount; k++) {
				party = olife.getPartyAt(k);
				if (nbaTxLife.isInsured(party.getId())) {
					Object obj = party.getPersonOrOrganization$Contents();
					if ((obj == null) || (obj instanceof Organization)) {
						createPerson(party);
						person.setActionAdd();
					}
					if (!party.hasPartyTypeCode()) {
						party.setPartyTypeCode(NbaOliConstants.OLI_PT_PERSON);
					}
					person = party.getPersonOrOrganization().getPerson();
					PersonExtension personExt, personExtResult = null;
					personExt = NbaUtils.getFirstPersonExtension(person);
					personExtResult = (PersonExtension) personExtensionMap.get(party.getId());
					if (personExt != null && personExtResult != null) {
						personExt.setActionUpdate();
						if (!personExt.getRateClassOverrideInd() && personExtResult.hasRateClass()) { //SPR2689, AXA3.7.07
								personExt.setRateClass(personExtResult.getRateClass()); //SPR2689
							
						} //SPR2689			
						personExt.setProposedRateClass(personExtResult.getProposedRateClass());
						
						// SR657319 Begin 
						if (passIndicator) {
								personExt.setApprovedRateClass(personExtResult.getProposedRateClass());
						}
						// SR657319 End
					}
				}
			}
			CoverageExtension covExt = null;
			if (nbaTxLife.getLife() != null) {
				int coverageCount = nbaTxLife.getLife().getCoverageCount();				
				for (int k = 0; k < coverageCount; k++) {
					Coverage coverage = nbaTxLife.getLife().getCoverageAt(k);
					coverage.setActionUpdate();
					if (coverage != null) {
						covExt = NbaUtils.getFirstCoverageExtension(coverage);
						if (covExt != null) {
							covExt.setActionUpdate();
						} else {
							OLifEExtension oli = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_COVERAGE);
							covExt = oli.getCoverageExtension();
							oli.setActionAdd();
						}
						if (covExt != null) {
							CoverageExtension covExtResult = (CoverageExtension) coverageExtMap.get(coverage.getId());
							LifeParticipant lifeParticipant = NbaUtils.getLifeParticipantFor(nbaTxLife.getPrimaryCoverage(),nbaTxLife.getPrimaryParty().getID());
							int issueAge = lifeParticipant.getIssueAge();
							if (covExtResult != null) {
								if (covExtResult.hasRateClass()) { // AXAL3.7.07
									// Start APSL5143
									if (issueAge <= MAX_CASE_AGE_SEVENTEEN) {
										if (!NbaUtils.isPlanNotApplicatbleForSPClass(nbaTxLife.getPolicy().getProductCode())) {
											covExt.setRateClass(NbaOliConstants.RATE_CLASS_STANDARD_PLUS);
										} else if (PLAN_ISWL.equalsIgnoreCase(nbaTxLife.getPolicy().getProductCode())) {
											covExt.setRateClass(NbaConstants.RATE_CALSS_PREFERRED_NONTOBACCO);
										} else {
											covExt.setRateClass(covExtResult.getRateClass());
										} // End APSL5143
									} else {
										covExt.setRateClass(covExtResult.getRateClass());
									}
								}
								if (NbaUtils.isTermConvOPAICase(nbaTxLife) && covExtResult.hasRateClass() && covExtResult.getRateClass().equals("")
										&& nbaTxLife.getSystemMessage(TCONV_MSG_CODE) == null
										&& nbaTxLife.getSystemMessage(NbaConstants.INVALID_RATE_CLASS) == null) { // APSL4635
									addTconvSystemMessage();
								} else {
									removeTconvMsg = true;
								}
								// Start APSL5143
								if (issueAge <= MAX_CASE_AGE_SEVENTEEN) {
									if (!NbaUtils.isPlanNotApplicatbleForSPClass(nbaTxLife.getPolicy().getProductCode())) {
										covExt.setProposedRateClass(NbaOliConstants.RATE_CLASS_STANDARD_PLUS);
									} else if (PLAN_ISWL.equalsIgnoreCase(nbaTxLife.getPolicy().getProductCode())) {
										covExt.setProposedRateClass(NbaConstants.RATE_CALSS_PREFERRED_NONTOBACCO);
									} else {
										covExt.setProposedRateClass(covExtResult.getProposedRateClass());
									}// End APSL5143
								} else {
									covExt.setProposedRateClass(covExtResult.getProposedRateClass());
								}
							}
						}
					}
				}
			}
			int resultCovCount = vpms.getCoverageCount();
			int resultcovOptionCount = 0;
			int conCovCount = 0;
			if (nbaTxLife.isLife()) {
				conCovCount = nbaTxLife.getLife().getCoverageCount();
			}
			int conCovOptionCount = 0;
			Coverage coverage;
			for (int i = 0; i < resultCovCount; i++) { //loop for all coverages returned by the model.
				Coverage vpmsCoverage = vpms.getCoverageAt(i);
				for (int j = 0; j < conCovCount; j++) {
					coverage = nbaTxLife.getLife().getCoverageAt(j);
					if (coverage.getId().equals(vpmsCoverage.getId().trim())) {
						coverage.setActionUpdate();
						coverage.setTobaccoPremiumBasis(vpmsCoverage.getTobaccoPremiumBasis());
						resultcovOptionCount = vpmsCoverage.getCovOptionCount();
						conCovOptionCount = coverage.getCovOptionCount();
						for (int a = 0; a < resultcovOptionCount; a++) {
							CovOption vpmsCovOption = vpmsCoverage.getCovOptionAt(a);
							for (int b = 0; b < conCovOptionCount; b++) {
								CovOption covOption = null;
								covOption = coverage.getCovOptionAt(b);
								if (covOption.getId().equalsIgnoreCase(vpmsCovOption.getId().trim())) {
									covOption.setActionUpdate();
									covOption.setUnderwritingClass(vpmsCovOption.getUnderwritingClass());
									CovOptionExtension covExtCon, covExtVpms = null;
									covExtCon = NbaUtils.getFirstCovOptionExtension(covOption);
									covExtVpms = NbaUtils.getFirstCovOptionExtension(vpmsCovOption);
									if (covExtCon == null) {
										OLifEExtension oli = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_COVOPTION);
										oli.setActionAdd();
										covExtCon = oli.getCovOptionExtension();
										covOption.addOLifEExtension(oli);
									}
									if (covExtCon != null && covExtVpms != null) {
										covExtCon.setActionUpdate();
										covExtCon.setProposedUnderwritingClass(covExtVpms.getProposedUnderwritingClass());
									}
									handleSubstandardRating(covOption, vpmsCovOption);
								}
							}
						}
						int resultLifePartiCount = vpmsCoverage.getLifeParticipantCount();
						int currLifePartiCount = coverage.getLifeParticipantCount();
						LifeParticipant currLifeParti;
						//ALS4731 Code Deleted
						for (int d = 0; d < resultLifePartiCount; d++) {
							LifeParticipant vpmsLifeParti = vpmsCoverage.getLifeParticipantAt(d);
							for (int k = 0; k < currLifePartiCount; k++) {
								currLifeParti = coverage.getLifeParticipantAt(k);
								if (currLifeParti.getId().equalsIgnoreCase(vpmsLifeParti.getId().trim())) {
									if (vpmsLifeParti.hasTobaccoPremiumBasis()) {
										currLifeParti.setActionUpdate();
										currLifeParti.setTobaccoPremiumBasis(vpmsLifeParti.getTobaccoPremiumBasis());
									}
									LifeParticipantExtension lifeExt = NbaUtils.getFirstLifeParticipantExtension(currLifeParti);
									if (lifeExt == null) {
										OLifEExtension olife1 = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_LIFEPARTICIPANT);
										olife1.setActionAdd();
										currLifeParti.addOLifEExtension(olife1);
										lifeExt = olife1.getLifeParticipantExtension();
									}
									LifeParticipantExtension lifeExtResult = NbaUtils.getFirstLifeParticipantExtension(vpmsCoverage
											.getLifeParticipantAt(d));
									if (lifeExtResult != null) {
										lifeExt.setProposedUnderwritingClass(lifeExtResult.getProposedUnderwritingClass());
										lifeExt.setProposedTobaccoPremiumBasis(lifeExtResult.getProposedTobaccoPremiumBasis());
										lifeExt.setActionUpdate();
									}
									//ALS4731 Code Deleted
									handleSubstandardRating(currLifeParti, vpmsLifeParti);//ALS4731
									break;
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * When auto underwriting fails, this method adds comments indicating why
	 * the process failed and creates a new <code>NbaAutomatedProcessResult</code> to return
	 * to the polling program.
	 * A null value may be passed to indicate failure prior to the executing
	 * the VPMS model.
	 * @param data   null or the results of the VPMS processing
	 * @param reason the reason the process failed
	 * @return an NbaAutomatedProcessResult containing the reason for failure
	 */
	public void doFailProcessing(VpmsModelResult data, String reason) throws NbaBaseException {
		List msgList = new ArrayList(); //ALS4381
		if (data == null) {
			getLogger().logDebug(reason);
			addComment("AutoUnderwriting failed: " + reason);
		} else {
			if (data.getMessages() != null) {
				for (int i = 0; i < data.getMessages().size(); i++) {
					Messages msg = data.getMessagesAt(i);
					if (!msgList.contains(msg.getMessageText())) {
						if (data.getMessagesAt(i).getPrivacyInd() == false) {
							addComment(msg.getMessageText()); //NBA132
						} else {
						    if(!duplicateSecureMessage(msg.getMessageText())){ //ALS5527
								//NBA132 code deleted
								Attachment attachment = new Attachment(); 
								nbaOLifEId.setId(attachment);
								attachment.setAttachmentType(NbaOliConstants.OLI_ATTACH_UNDRWRTNOTE);
								attachment.setActionAdd();
								attachment.setDateCreated(new Date());
								attachment.setUserCode(getUser().getUserID());
								AttachmentData attachmentData = new AttachmentData();
								attachmentData.setTc("8");
								// Begin NBA225
								SecureComment secure = new SecureComment();
								secure.setComment(msg.getMessageText());
								secure.setUserNameEntered(getUser().getUserID());
								secure.setAutoInd(true);// NBA323 - Retrofit from Base(APSL3520)
								attachmentData.setPCDATA(toXmlString(secure)); //NBA132
								// End NBA225
								attachmentData.setActionAdd();
								attachment.setAttachmentData(attachmentData);
								nbaTxLife.getPrimaryHolding().addAttachment(attachment);
						    }	
						}
					}
					msgList.add(msg.getMessageText());
				}
			}
		}
		//SR564247(APSL2525),Deleted Code(ALII1605)
		updateFields(data, personExtensionMap, coverageExtensionMap);		
	}	
	
	/**
	 * Suspend the ReEval WI
	 *  @return
	 * @throws NbaBaseException
	 */
	//ALS5260 New Method
	protected NbaAutomatedProcessResult suspendReEvalWI()throws NbaBaseException {
		NbaTransaction nbaTrans = null;
		ListIterator transList = getWork().getNbaTransactions().listIterator();
		while (transList.hasNext()) {
			nbaTrans = (NbaTransaction) transList.next();
			if (A_WT_REEVALUATE.equals(nbaTrans.getTransaction().getWorkType()) && getOrigWorkItem().getID().equals(nbaTrans.getID())) {
				break;
			}
		}
		NbaSuspendVO suspendVO = new NbaSuspendVO();
		if(nbaTrans!= null){
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(new Date());
			calendar.add(Calendar.DAY_OF_WEEK, 1);//Suspend for 1 day
			suspendVO.setActivationDate(calendar.getTime());
			suspendVO.setTransactionID(nbaTrans.getID());
			setSuspendVO(suspendVO);
			addComment("ReEvaluation suspended, all requirements have not been receipted and/or evaluated");
			updateForSuspend();
			return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", SUSPENDED);
		}
		return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Failed", getFailStatus()); 			
					
	}
	/**
	 * When auto underwriting fails and Suspend processing is needed, this method adds suspends the case 
	 * creates a new <code>NbaAutomatedProcessResult</code> to return
	 * to the polling program.
	 * A null value may be passed to indicate failure prior to the executing
	 * the VPMS model.
	 * @param data   null or the results of the VPMS processing
	 * @return an NbaAutomatedProcessResult containing the reason for failure
	 */
	// ACP010 New Method
	protected NbaAutomatedProcessResult suspendCase(VpmsModelResult vpms) throws NbaBaseException { //SPR2699 change methhod visibility
		//NBA132 code deleted
		SuspendInfo suspendInfo = null;
		Policy policy = nbaTxLife.getPolicy();
		//NBA132 code deleted
		Date initialSuspendDate = new Date();
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(policy);
		if (policyExtension == null) {
			OLifEExtension olife = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
			olife.setActionAdd();
			policy.addOLifEExtension(olife);
			policyExtension = olife.getPolicyExtension();
			policyExtension.setActionAdd();

		}
		if (policyExtension.hasSuspendInfo()) {
			suspendInfo = policyExtension.getSuspendInfo();
			suspendInfo.setActionUpdate();
		} else {
			suspendInfo = new SuspendInfo();
			suspendInfo.setActionAdd();
			suspendInfo.setSuspendDate(initialSuspendDate);
			suspendInfo.setUserCode(getUser().getUserID());
			policyExtension.setSuspendInfo(suspendInfo);
		}
		if (suspendInfo.hasSuspendDate() && suspendInfo.getUserCode().equals(getUser().getUserID())) {
			initialSuspendDate = policyExtension.getSuspendInfo().getSuspendDate();
			//NBA132 code deleted
		}
		policy.setActionUpdate();
		if(isReEvalTransaction()){//ALS5260
			removeErrorMessage();//ALS5260
		}//ALS5260
		nbaTxLife = doContractUpdate();
		handleHostResponse(nbaTxLife);
		if (getResult() == null) { // not a database update error		//SPR2699	
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(initialSuspendDate);
			Date currentDate = new Date();
			calendar.add(Calendar.DAY_OF_WEEK, maximumSuspendDuration);
			Date maxSuspendDurationDate = calendar.getTime();
			if (currentDate.after(maxSuspendDurationDate)) {
				addComment("Case cannot be suspended because the maximum suspend duration has been exceeded"); //SPR2699
			} else {
				addComment("All Requirements have not been receipted and/or evaluated"); //SPR2699
				//begin SPR2758
				NbaSuspendVO tempsuspendVO = new NbaSuspendVO();
				tempsuspendVO.setCaseID(getWork().getID());
				tempsuspendVO.setActivationDate(suspendActivationDate);
				//end SPR2758
				setSuspendVO(tempsuspendVO);
				updateForSuspend();
				caseSuspended = true;//ALS5260
				return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", SUSPENDED);
			}
			return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Failed", getFailStatus()); //NBA132//NBA251
		}
		return getResult();
	}

	/**
	 * This method is invoked when automated underwriting is needed for a case.
	 * It instantiates an NbaVpmsAdaptor object using the input parameter and 
	 * the VP/MS model name.  It then executes the NbaVpmsAdaptor method 
	 * getResults() and, using the results from the method call, instantiates 
	 * and returns an NbaVpmsAutoUnderwritingData object.
	 * @return NbaVpmsAutoUnderwritingData object that contains results of the call
	 * to the VPMS model.
	 * @throws NbaBaseException java.rmi.RemoteException
	 */
	protected VpmsModelResult underwriteCase() throws java.rmi.RemoteException, NbaBaseException { //SPR1778
		NbaVpmsAdaptor vpmsProxy = null; //SPR3362
		try { //SPR3362
			oinkRequestGlobal = new NbaOinkRequest(); // set up the NbaOinkRequest object
			vpmsProxy = new NbaVpmsAdaptor(oinkData, getVpmsModelToExecute()); //SPR3362
			vpmsProxy.setVpmsEntryPoint("P_ResultXml");
			//begin NBA059
			Map deOink = new HashMap();
			//P2AXAL006 Code deleted
			deOinkTermConvData(deOink); //NBA300
			//ACP010 Begins
			vpmsProxy.setSkipAttributesMap(deOink);
			// AC010 Ends
			//end NBA059
			vpmsProxy.setANbaOinkRequest(oinkRequestGlobal);
			oinkRequestGlobal.setCount(1);
			VpmsModelResult vpmsModelResult = underwriteInsureds(vpmsProxy); //SPR1778
			//SPR3362 code deleted
			return vpmsModelResult;
			//begin SPR3362
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (RemoteException e) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}
		//end SPR3362
	}
	
	/**
	 * Invoke the model for each unsured or annuitant until underwriting fails or all parties have been processed.
	 * @param nbaVpmsAdaptor
	 * @return NbaVpmsAutoUnderwritingData
	 */
	//SPR1778 New Method
	protected VpmsModelResult underwriteInsureds(NbaVpmsAdaptor nbaVpmsAdaptor) throws java.rmi.RemoteException, NbaBaseException {
		OLifE oLifE = nbaTxLife.getOLifE();
		NbaVpmsResultsData vpmsResultsData;
		NbaVpmsModelResult nbaVpmsModelResult = null;
		VpmsModelResult vpmsModelResult = null;
		int loopCount = 1;
		String nonacFlag = getloopingFlagForAutomatedProcess();
		//NBA132 code deleted
		int insuredIndex = 0;
		int iterationCount = 0; //SPR1753
		for (int i = 0; i < oLifE.getPartyCount(); i++) {
			String partyId = oLifE.getPartyAt(i).getId();
			setPartyID(partyId);
			oinkRequestGlobal.setArgs(getKeys());
			nbaVpmsAdaptor.setANbaOinkRequest(oinkRequestGlobal);
			if (nbaTxLife.isInsured(partyId) || nbaTxLife.isAnnuitant(partyId)) {
				Relation relation = NbaUtils.getRelationForParty(partyId, oLifE.getRelation().toArray()); //SPR2619
				if (relation != null) { //SPR2619
					nbaVpmsAdaptor.getANbaOinkRequest().setPartyFilter(relation.getRelationRoleCode(), relation.getRelatedRefID()); //SPR2619
					insuredIndex++;
					getAcAutoIssueDeOINKValues(nbaVpmsAdaptor);
					ArrayList coverageList = getAcCoverageListForInsured();
					if (nonacFlag.equals("TRUE") || nbaTxLife.isAnnuitant(partyId)) { // In base environMent //SPR1753
						loopCount = 1;
					} else {
						loopCount = coverageList.size();
					}
					for (int a = 0; a < loopCount; a++) {
						String coverageId = "";
						if (nonacFlag.equals("FALSE") && nbaTxLife.isInsured(partyId)) { //SPR1753
							Coverage currentCoverage = (Coverage) coverageList.get(a);
							coverageId = currentCoverage.getId();
							getDeoinkValuesforCoverage(nbaVpmsAdaptor, currentCoverage, partyId);//P2AXAL053
						}
						nbaVpmsAdaptor.getSkipAttributesMap().put("A_IterationCount", String.valueOf(++iterationCount)); //SPR1753
						vpmsResultsData = new NbaVpmsResultsData(nbaVpmsAdaptor.getResults());
						String xmlString = (String) vpmsResultsData.getResultsData().get(0);
						nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
						vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
						if (vpmsModelResult.getAutoUnderwritingModelResultsAt(0).getPassInd() == false)
							passIndicator = false;
						if (vpmsModelResult.getAutoUnderwritingModelResultsAt(0).getOutstandingImpairmentsInd() == true)
							outstandingImpairmentsIndicator = true;
						if (vpmsModelResult.getAutoUnderwritingModelResultsAt(0).getOutstandingRequirementsInd() == true)
							outstandingRequirementsIndicator = true;
						if (vpmsModelResult.getCoverageCount() > 0) {
							vpmsModelResultGlobal.addCoverage(vpmsModelResult.getCoverageAt(0));
						}
						if (vpmsModelResult.getPersonExtensionCount() > 0) {
							personExtensionMap.put(PartyID, vpmsModelResult.getPersonExtensionAt(0));
						}
						if (vpmsModelResult.getCoverageExtensionCount() > 0) {
							coverageExtensionMap.put(coverageId, vpmsModelResult.getCoverageExtensionAt(0));
						}
						if (vpmsModelResult.getMessagesCount() > 0) {
							for (int k = 0; k < vpmsModelResult.getMessagesCount(); k++) {
								vpmsModelResultGlobal.addMessages(vpmsModelResult.getMessagesAt(k));
							}
						}
					}
				} //SPR2619
				if (passIndicator == true) {
				} else {
					reason = "FailedAutoUnderwriting";
					if (outstandingImpairmentsIndicator == true) {
						reason = "OutstandingImpairments";
					}
					if (outstandingRequirementsIndicator == true) {
						reason = "OutstandingRequirements";
					}
				}
				if (vpmsModelResult != null && vpmsModelResult.getAutoUnderwritingModelResultsCount() > 0) {
					//SPR2758 code deleted
					maximumSuspendDuration = Integer.parseInt(String.valueOf(vpmsModelResult.getAutoUnderwritingModelResultsAt(0)
							.getMaximumSuspendDuration()));
					suspendActivationDate = NbaUtils.getDateFromStringInAWDFormat(vpmsModelResult.getAutoUnderwritingModelResultsAt(0)
							.getSuspendActivateDate()); //SPR2758
				}
				// ACP010 code deleted	
			}
		}

		return vpmsModelResultGlobal;
	}

	/**
	 * This method returns the Objective Index value
	 * @param fund is the Fund Index for which Objective Index is required
	 * @return objIndex
	 */
	//NBA059 new method
	protected String getObjectiveValue(String fund, NbaDst dst, NbaFundsData[] dataObj) {
		String objIndex = null;
		try {
			for (int i = 0; i < dataObj.length; i++) {
				if ((dataObj[i].getIndexValue()).equals(fund)) {
					objIndex = dataObj[i].getIndexTranslation();
					break;
				}
			}
		} catch (Exception e) {
		}
		return objIndex;
	}

	/**
     * This method sets all the Deoink values Required for the AcAutoUnderwriting Model To Run for a coverage
     * 
     * @param NbaVpmsAdapter
     * @return Coverage coverage forwhich model neds to run.
     */
	//P2AXAL053 signature modified
    public void getDeoinkValuesforCoverage(NbaVpmsAdaptor nbaVpmsAdaptor, Coverage coverage, String partyId) throws NbaBaseException {
        String indCode = "";
        String prodCode = "";
        String overrideRatingReason = "";
        String coverageId = "";
        NbaOinkRequest oinkRequest = nbaVpmsAdaptor.getANbaOinkRequest();
        NbaOinkDataAccess accessContract = nbaVpmsAdaptor.getOinkSurrogate();
        //NBA132 code deleted
        if (coverage != null) {
            indCode = coverage.hasIndicatorCode() ? String.valueOf(coverage.getIndicatorCode()) : "";
            prodCode = coverage.hasProductCode() ? coverage.getProductCode() : "";
            overrideRatingReason = getOverideRatingIndicatorForCoverage(coverage);
            coverageId = coverage.getId();
            addToDeOinkMap(nbaVpmsAdaptor.getSkipAttributesMap(), "A_IndicatorCode_INS", indCode, true);
            addToDeOinkMap(nbaVpmsAdaptor.getSkipAttributesMap(), "A_CoverageId", coverageId, true);
            addToDeOinkMap(nbaVpmsAdaptor.getSkipAttributesMap(), "A_ProductCode_INS", prodCode, true);
            addToDeOinkMap(nbaVpmsAdaptor.getSkipAttributesMap(), "A_OverrideRatingReason", overrideRatingReason, true);
            oinkRequest.setVariable("CovOptionLifeCovOptTypeCodeList");
            oinkRequest.setCoverageIdFilter(coverage.getId());
            String[] covOptionTypeCodeList = accessContract.getStringValuesFor(oinkRequest);
            String covOptionProductCode = "";

            int countCovOption = covOptionTypeCodeList.length;
            ArrayList covOptionOverrideRatingReasonList = getOverrideRatingReasonForCovOption(coverage);
            addToDeOinkMap(nbaVpmsAdaptor.getSkipAttributesMap(), "A_no_of_CovOptionProductCode", String.valueOf(countCovOption), true);
            for (int j = 0; j < countCovOption; j++) {
                covOptionProductCode = covOptionTypeCodeList[j];
                overrideRatingReason = (String) covOptionOverrideRatingReasonList.get(j);
                addToDeOinkMap(nbaVpmsAdaptor.getSkipAttributesMap(), "A_LifeCovOptTypeCovOption", j, covOptionProductCode, true);
                addToDeOinkMap(nbaVpmsAdaptor.getSkipAttributesMap(), "A_CovOptionId", j, coverage.getCovOptionAt(j).getId(), true); //ACP010
                addToDeOinkMap(nbaVpmsAdaptor.getSkipAttributesMap(), "A_CovOptionOverrideRatingReason", j, overrideRatingReason, true);
            }
            //begin ACP010
            LifeParticipant lifeParticipant = NbaUtils.getLifeParticipantFor(coverage, partyId);//P2AXAL053
            if (lifeParticipant != null) {
                addToDeOinkMap(nbaVpmsAdaptor.getSkipAttributesMap(), "A_LifeParticipantId", lifeParticipant.getId(), true);
                // Begin NBA223-01
                LifeParticipantExtension lifePartExtn = NbaUtils.getFirstLifeParticipantExtension(lifeParticipant);
                if (lifePartExtn != null) {
                    addToDeOinkMap(nbaVpmsAdaptor.getSkipAttributesMap(), "A_LifeParticipantOverrideRatingReason", lifePartExtn
                            .getOverrideRatingReason(), true);
                }
                int subStdRatingCnt = lifeParticipant.getSubstandardRatingCount();
                SubstandardRatingExtension subStdRatExtn = null;
                for (int i = 0; i < subStdRatingCnt; i++) {
                    subStdRatExtn = NbaUtils.getFirstSubstandardExtension(lifeParticipant.getSubstandardRatingAt(i));
                    if (subStdRatExtn != null) {
                        if ((subStdRatExtn.hasProposedInd() && !subStdRatExtn.getProposedInd()) || !subStdRatExtn.hasProposedInd()) {
                            if (subStdRatExtn.hasInsRatedInd()) {
                                addToDeOinkMap(nbaVpmsAdaptor.getSkipAttributesMap(), "A_InsRatedInd",
                                        String.valueOf(subStdRatExtn.getInsRatedInd()), true);
                            }
                        }
                    }
                }
                // End NBA223-01
            }

            //end ACP010
        }
    }

	/**This method add values to skip map of vpms Proxy.
	 * @param Map in which values needs to be added.
	 * @param String variable whose value needs to be added.
	 * @param String value which needs to added.
	 * @param boolean flag whether default value needs to added or not.
	 * @return void
	 */
	private void addToDeOinkMap(Map deoink, String variable, String value, boolean useDefault) {
		String newVariable = variable;
		String newValue = value;
		if (useDefault) {
			newValue = value == null || value.length() == 0 ? "" : value;
		}
		deoink.put(newVariable, newValue);
	}

	/**
	 * This method gets all the CovOption.OverideRatingReason Values for a Coverage
	 * @param Coverage coverage
	 * @return ArrayList Containing OverideRatingReason Values for Coverage
	 */
	// ACP010 New Method
	public ArrayList getOverrideRatingReasonForCovOption(Coverage coverage) {
		ArrayList overideRatingRaesonCovoption = new ArrayList();
		ArrayList covOptionList = coverage.getCovOption();
		if (covOptionList != null && covOptionList.size() > 0) {
			for (int a = 0; a < covOptionList.size(); a++) {
				CovOption covOption = (CovOption) covOptionList.get(a);
				if (covOption != null) {
					CovOptionExtension covOptionExtension = NbaUtils.getFirstCovOptionExtension(covOption);
					if (covOptionExtension != null) {
						overideRatingRaesonCovoption.add(a, covOptionExtension.getOverrideRatingReason());
					} else {
						overideRatingRaesonCovoption.add(a, "");
					}
				}
			}
		}
		return overideRatingRaesonCovoption;
	}

	/**
	 * This method gets all the OverideRatingReason Values for a Coverage
	 * @param Coverage coverage
	 * @return ArrayList Containing OverideRatingReason Values for Coverage
	 */
	// ACP010 New Method
	public String getOverideRatingIndicatorForCoverage(Coverage coverage) {
		String overideRatingRaeson = "";
		CoverageExtension coverageExtension = NbaUtils.getFirstCoverageExtension(coverage);
		if (coverageExtension != null) {
			overideRatingRaeson = coverageExtension.getOverrideRatingReason();
		}
		return overideRatingRaeson;
	}

	//ACP010 New Method
	private Object[] getKeys() {
		Object[] keys = new Object[4];
		keys[0] = getPartyID();//Parentid key.
		keys[1] = getContractNo();//contract key
		keys[2] = getCompanyKey();//company key
		keys[3] = getBackEndKey();//backend key
		return keys;
	}

	/**
	 * @return
	 */
	//	ACP010 New Method
	public String getContractNo() {
		return contractNo;
	}

	/**
	 * @param string
	 */
	//	ACP010 New Method
	public void setContractNo(String string) {
		contractNo = string;
	}

	/**
	 * @return
	 */
	//	ACP010 New Method
	public String getBackEndKey() {
		return backEndKey;
	}

	/**
	 * @return
	 */
	//	ACP010 New Method
	public String getCompanyKey() {
		return companyKey;
	}

	/**
	 * @param string
	 */
	//	ACP010 New Method
	public void setBackEndKey(String string) {
		backEndKey = string;
	}

	/**
	 * @param string
	 */
	//	ACP010 New Method
	public void setCompanyKey(String string) {
		companyKey = string;
	}

	/**
	 * @return
	 */
	//	ACP010 New Method
	public String getPartyID() {
		return PartyID;
	}

	/**
	 * @param string
	 */
	//	ACP010 New Method
	public void setPartyID(String string) {
		PartyID = string;
	}

	/**This Method sets all The DeOink Variable's Value in a Hash Map.
	 * This methods gets all the values for the count retreived and sets the values -1 if there is a null value that has come in. 
	 * @param Hash map That contains the values of variables.
	 * @return void
	 */
	//	ACP010 New Method  
	public void getAcAutoIssueDeOINKValues(NbaVpmsAdaptor nbaVpmsAdaptor) throws NbaBaseException {
		Map deOInkMap = nbaVpmsAdaptor.getSkipAttributesMap();
		NbaOinkRequest oinkRequest = new NbaOinkRequest();
		NbaOinkDataAccess accessContract = nbaVpmsAdaptor.getOinkSurrogate();
		// SPR3290 code deleted

		List impairments = nbaTxLife.getImpairments(PartyID); // SPR3290
		int impCount = impairments.size(); // SPR3290
		int permFlatCount = 0;//ALS5638
		int tempFlatCount = 0;//ALS5638
		deOInkMap.put("A_no_of_Impairments", String.valueOf(impCount));
		for (int i = 0; i < impCount; i++) {
			ImpairmentInfo impairmentInfo = (ImpairmentInfo) impairments.get(i); // SPR3290
			String credit = String.valueOf(impairmentInfo.getCredit());
			String debit = String.valueOf(impairmentInfo.getDebit());
			String impairmentStatus = String.valueOf(impairmentInfo.getImpairmentStatus());
			String impairmentPermFlatExtraAmt = impairmentInfo.getImpairmentPermFlatExtraAmt();
			String impairmentTempFlatExtraAmt = impairmentInfo.getImpairmentTempFlatExtraAmt();
			String impairmentClass = impairmentInfo.getImpairmentClass();
			String impairmentType = impairmentInfo.getImpairmentType();
			String impairmentDuration = impairmentInfo.getImpairmentDuration();
			if (i == 0) {
				deOInkMap.put("A_Debit", convertToDefault(debit));
				deOInkMap.put("A_Credit", convertToDefault(credit));
				deOInkMap.put("A_ImpairmentStatus", convertToDefault(impairmentStatus));
				deOInkMap.put("A_ImpairmentPermFlatExtraAmt", convertToDefault(impairmentPermFlatExtraAmt));
				deOInkMap.put("A_ImpairmentTempFlatExtraAmt", convertToDefault(impairmentTempFlatExtraAmt));
				deOInkMap.put("A_ImpairmentClass", convertToDefault(impairmentClass));
				deOInkMap.put("A_ImpairmentType", convertToDefault(impairmentType));
				deOInkMap.put("A_ImpairmentDuration", convertToDefault(impairmentDuration));
				deOInkMap.put("A_ImpairmentDescription", impairmentInfo.getDescription()); //AXAL3.7.07
			} else {
				deOInkMap.put("A_Debit[" + i + "]", convertToDefault(debit));
				deOInkMap.put("A_Credit[" + i + "]", convertToDefault(credit));
				deOInkMap.put("A_ImpairmentStatus[" + i + "]", convertToDefault(impairmentStatus));
				deOInkMap.put("A_ImpairmentPermFlatExtraAmt[" + i + "]", convertToDefault(impairmentPermFlatExtraAmt));
				deOInkMap.put("A_ImpairmentTempFlatExtraAmt[" + i + "]", convertToDefault(impairmentTempFlatExtraAmt));
				deOInkMap.put("A_ImpairmentClass[" + i + "]", convertToDefault(impairmentClass));
				deOInkMap.put("A_ImpairmentType[" + i + "]", convertToDefault(impairmentType));
				deOInkMap.put("A_ImpairmentDuration[" + i + "]", convertToDefault(impairmentDuration));
				deOInkMap.put("A_ImpairmentDescription[" + i + "]", impairmentInfo.getDescription()); //AXAL3.7.07
			}
			//begin ALS5638
			if (!NbaUtils.isBlankOrNull(impairmentPermFlatExtraAmt)) {
				if (permFlatCount == 0) {
					deOInkMap.put("A_PermFlatExtraAmt", convertToDefault(impairmentPermFlatExtraAmt));
					permFlatCount++;
				} else {
					deOInkMap.put("A_PermFlatExtraAmt[" + permFlatCount + "]", convertToDefault(impairmentPermFlatExtraAmt));
					permFlatCount++;
				}
			}
			if (!NbaUtils.isBlankOrNull(impairmentTempFlatExtraAmt)) {
				if (tempFlatCount == 0) {
					deOInkMap.put("A_TempFlatExtraAmt", convertToDefault(impairmentTempFlatExtraAmt));
					deOInkMap.put("A_TempFlatExtraAmtDuration", convertToDefault(impairmentDuration));
					tempFlatCount++;
				} else {
					deOInkMap.put("A_TempFlatExtraAmt[" + tempFlatCount + "]", convertToDefault(impairmentTempFlatExtraAmt));
					deOInkMap.put("A_TempFlatExtraAmtDuration[" + tempFlatCount + "]", convertToDefault(impairmentDuration));
					tempFlatCount++;
				}
			}
		}
		deOInkMap.put("A_NoofPermFlatExtra", String.valueOf(permFlatCount));
		deOInkMap.put("A_NoofTempFlatExtra", String.valueOf(tempFlatCount));
		//end ALS5638
		//AXAL3.7.07 code deleted
		//begin AXAL3.7.07
		Map reqMap = nbaTxLife.getRequirementInfos(getPartyID());
		Collection reqCollection = reqMap.values();
		Iterator reqIterator = reqCollection.iterator();
		//end AXAL3.7.07

		//NBA132 code deleted
		int countReq = 0;
		//AXAL3.7.07 code deleted
		NbaHolding nbaHolding = nbaTxLife.getNbaHolding(); //NBA122
		deOInkMap.put(A_UNDERWRITER_WORKBENCH_APPLET, Boolean.toString(NbaConfiguration.getInstance().isUnderwriterWorkbenchApplet())); //NBA122
		while (reqIterator.hasNext()) { //AXAL3.7.07

			//AXAL3.7.07 Code Deleted
			//begin AXAL3.7.07, use RequirementInfo, instead of AWD work items
			RequirementInfo reqInfo = (RequirementInfo) reqIterator.next();
			if (null == reqInfo) {
				throw new NbaRequirementInfoException("Unable to retrieve RequirementInfo for party " + getPartyID() + " .");
			}
			RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
			addToDeOinkMap(deOInkMap, "A_ReqStatus", countReq, String.valueOf(reqInfo.getReqStatus()), false);
			addToDeOinkMap(deOInkMap, "A_ReqCode", countReq, String.valueOf(reqInfo.getReqCode()), false);
			addToDeOinkMap(deOInkMap, "A_ReqMedicalType", countReq, String.valueOf(reqInfoExt.getMedicalIndicator()), false);
			//end AXAL3.7.07
			addToDeOinkMap(deOInkMap, "A_ReqRestriction", countReq, String.valueOf(reqInfo.getRestrictIssueCode()), false);
			if (reqInfoExt != null) {
				addToDeOinkMap(deOInkMap, "A_ReqReview", countReq, String.valueOf(reqInfoExt.getReviewCode()), false);
				addToDeOinkMap(deOInkMap, "A_ReviewedInd", countReq, String.valueOf(reqInfoExt.getReviewedInd()), false);
			} else {
				addToDeOinkMap(deOInkMap, "A_ReqReview", countReq, "0", false);
				addToDeOinkMap(deOInkMap, "A_ReviewedInd", countReq, String.valueOf(false), false);
			}
			//End NBA130
			//end NBA122
			countReq++;
			//AXAL3.7.07 code deleted
		}
		deOInkMap.put("A_no_of_Requirements", String.valueOf(countReq));
		if (countReq == 0) {
			deOInkMap.put("A_ReqCode", "");
			deOInkMap.put("A_ReqStatus", "");
			deOInkMap.put("A_ReqMedicalType", "");
			deOInkMap.put("A_ReqRestriction", ""); //NBA122
			deOInkMap.put("A_ReqReview", ""); //NBA122
			deOInkMap.put("A_ReviewedInd", ""); //NBA122
		}

		oinkRequest.setVariable("AgentLicNumList_PWA");
		String[] codeList = accessContract.getStringValuesFor(oinkRequest);
		int count = codeList.length;
		deOInkMap.put("A_no_of_Agents", new Integer(count).toString());
		if (count == 0) {
			deOInkMap.put("A_AgentLicNum", "");
		} else {
			for (int i = 0; i < count; i++) {
				addToDeOinkMap(deOInkMap, "A_AgentLicNum", i, codeList[i], true);
			}
		}

	}

	/**This Method Convert String values to Default
	 * @param String
	 * @return String
	 *
	 */
	//ACP010 New Method 
	private String convertToDefault(String str) {
		if (str == null || str.equalsIgnoreCase("null")) {

			return "";
		} else if (str.equalsIgnoreCase("NaN")) {
			return "-1";
		}
		return str;
	}

	/**This Method Updates the Case for getting suspend in the AWD and 
	 *if there is some error in case getting suspended it throws the Exception
	 * 
	 */
	// ACP010 New Method
	public void updateForSuspend() throws NbaBaseException {
		getLogger().logDebug("Starting updateForSuspend");
		updateWork(getUser(), getWork()); //NBA213
		suspendWork(getUser(), getSuspendVO()); //NBA213
		unlockWork(getUser(), getWork()); //NBA213
	}

	/**This Methods Returns the Suspend VO
	 * 
	 */
	//ACP010 New Method 
	public NbaSuspendVO getSuspendVO() {
		return suspendVO;
	}

	/**This Method Sets the Suspend VO. 
	 * 
	 */
	// ACP010 New Method.
	public void setSuspendVO(NbaSuspendVO newSuspendVO) {
		suspendVO = newSuspendVO;
	}

	protected void createPerson(Party party) {
		//create a new PO object
		PersonOrOrganization po = new PersonOrOrganization();
		po.setPerson(new Person());
		party.setPersonOrOrganization(po);
	}

	/** This method formats an attribute and adds it to the 
	 * skipAttributesMap for the NbaVpmsAdaptor.
	 * @param deoink the Map for the NbaVpmsAdaptor
	 * @param variable the name of the attribute to be added
	 * @param index an integer value indicating the index
	 * @param value the value for the attribute
	 * @boolean useDefault indicates if a default value of -1 should be
	 * used if no value present
	 * @return void
	 */
	//	ACP010 New Method  
	private void addToDeOinkMap(Map deoink, String variable, int index, String value, boolean useDefault) {
		String newVariable = variable;
		if (index > 0) {
			newVariable = variable + "[" + index + "]";
		}
		String newValue = value;
		if (useDefault) {
			newValue = value == null || value.length() == 0 ? "" : value;
		}
		deoink.put(newVariable, newValue);
	}

	/* This method gets all the Coverages in which insured party is Lifeparticipant 
	 * @return ArrayList containing all the coverages for party.
	 */
	private ArrayList getAcCoverageListForInsured() {
		ArrayList coverageList = new ArrayList();
		coverageList = nbaTxLife.getCoveragesFor(PartyID);
		return coverageList;
	}

	/** 
	 * Handle CovOption Substandard Rating objects returned by model.
	 * @param CovOption - pre-existing CovOptions
	 * @param CovOptionResult - model CovOptions
	 * @return void
	 */
	// ACP010 New Method
	protected void handleSubstandardRating(CovOption covOption, CovOption covOptionResult) {
		//ALS4731 Code Deleted
		if(covOption.getCovOptionStatus() != NbaOliConstants.OLI_POLSTAT_DECISSUE){//ALS5506 ALS5507
			covOption.setSubstandardRating(mergeSubstandardRatings(covOption.getSubstandardRating(), covOptionResult.getSubstandardRating(),false));//ALS4731 ALS4904
		}
	}

	/** 
	 * Handle Life Participant Substandard Rating objects returned by model.
	 * @param lifePartiCon - pre-existing LifeParticipants
	 * @param lifePartiRes - model LifeParticipants
	 * @return void
	 */
	// ACP010 New Method
	//ALS4731 Method Signature Changed
	protected void handleSubstandardRating(LifeParticipant lifePartiCon, LifeParticipant lifePartiRes) {
		lifePartiCon.setSubstandardRating(mergeSubstandardRatings(lifePartiCon.getSubstandardRating(), lifePartiRes.getSubstandardRating(),true));//ALS4731 ALS4904
	}

	/**
	 * Merge SubstandardRatings for a LifeParticipant or CovOption.
	 * If the model only returns proposed substandard rating objects, delete ALL proposed objects 
	 * for the parent object and add the new object(s).
	 * If the model returns both proposed and 'actual' delete ALL proposed and actual 
	 * objects for the parent object and add the new proposed and 'actual'.
	 * There is a situation where there could be proposed and actual objects in the dbase but the model now returns 
	 * that NO ratings are required.  In this situation, ALL proposed objects should be deleted.  However, the actual 
	 * object should be deleted ONLY if the CovOption or Coverage (whichever is the parent object in question) does 
	 * NOT have the OverrideRatingReason field populated.  If this field is populated only the proposed should be deleted.  
	 * The actuals should remain.
	 * @param currSubstandardRatings
	 * @param modelSubstandardRatings
	 * @return
	 */
	// ACP010 New Method
	//ALS4731 Method Signature Changed
	protected ArrayList mergeSubstandardRatings(ArrayList currSubstandardRatings, ArrayList modelSubstandardRatings, boolean isLafeParticipantRating) {//ALS4904
		SubstandardRating newModelSubstandardRating;//ALS3680
		SubstandardRatingExtension modelSubstandardRatingExtension;
		SubstandardRating currSubstandardRating;
		SubstandardRatingExtension currSubstandardRatingExtension;
		List newModelSubstandardRatings = new ArrayList();//ALS3680
		//Code Deleted ALS4731
		boolean deleteProposed = modelSubstandardRatings.size() == 0; //delete proposed if no new ratings
		boolean deleteActual = false; //ALS4731 Don't delete actual ratings if no ratings are returned from the model 

		//Determine what to do based on new SubstandardRatings
		for (int i = 0; i < modelSubstandardRatings.size(); i++) {
			//ALS3680 begin
			newModelSubstandardRating = ((SubstandardRating) modelSubstandardRatings.get(i)).clone(false);
			newModelSubstandardRating.setId(null);
			newModelSubstandardRating.setAction(null);
			nbaOLifEId.setId(newModelSubstandardRating);
			//ALS3680 end
			newModelSubstandardRating.setActionAdd(); //Add new objects
			modelSubstandardRatingExtension = NbaUtils.getFirstSubstandardExtension(newModelSubstandardRating);
			if (modelSubstandardRatingExtension != null) {
				if(isLafeParticipantRating) {//ALS4904
					modelSubstandardRatingExtension.setInsRatedInd(true);	//ALS3973	
				}
				if (modelSubstandardRatingExtension.getProposedInd()) {
					deleteProposed = true;
				} else {
					deleteActual = true;
				}
			}
			newModelSubstandardRatings.add(newModelSubstandardRating);//ALS3680
		}
		//Begin ALS5448 - Commented SPR3014 logic
		//Begin SPR3014
		//Do not delete the actual ratings on the contract for contract changes 
		//if (getWork().getNbaLob().getContractChgType() != null) {
		//	deleteActual = false;
		//}
		//End ALS5448
		
		//End SPR3014
		//Delete pre-existing if necessary
		for (int i = 0; i < currSubstandardRatings.size(); i++) {
			currSubstandardRating = (SubstandardRating) currSubstandardRatings.get(i);
			currSubstandardRatingExtension = NbaUtils.getFirstSubstandardExtension(currSubstandardRating);
			if (currSubstandardRatingExtension != null) {
				if (currSubstandardRatingExtension.getProposedInd() && deleteProposed) {
					currSubstandardRating.setActionDelete();
				} else if (!currSubstandardRatingExtension.getProposedInd() && deleteActual) {
					currSubstandardRating.setActionDelete();
				}
			}
		}
		currSubstandardRatings.addAll(newModelSubstandardRatings);//ALS3680 adding the newly created substandardrating list.
		return currSubstandardRatings; //Merged results
	}

	/**
	 * Answer true if the contract is CyberLife back-end system; false otherwise.
	 * @param nbaDst
	 * @return boolean  
	 */
	// ACP010 New Method
	protected boolean isCyberLifeBackEndSystem() {
		return getWork().getNbaLob().getBackendSystem().equals(NbaConstants.SYST_CYBERLIFE);
	}

	//NBA251 Methods deleted
	/**
	 * @return
	 * @throws NbaBaseException
	 */
	//new method AXAL3.7.40G
	//APSL3782 Signature of the method is change, Parameter Introduced.
	protected NbaAutomatedProcessResult doPreventProcessing(boolean routeNeeded) throws NbaBaseException {
		NbaAutomatedProcessResult result = null;
		AxaPreventProcessVpmsData preventProcessData = new AxaPreventProcessVpmsData(user, getNbaTxLife(), getVpmsModelToExecute());
		PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(nbaTxLife.getPolicy());
		boolean routetoAlternateStatus = false;
		// Begin NBLXA-1288
		Activity activity=null;
		boolean activtyPreventProcess=false;
		List<Activity> activityList = NbaUtils.getActivityByTypeCodeAndStatus(getNbaTxLife(), NbaOliConstants.OLI_ACTTYPE_PREVENT_PROCESS,
				NbaOliConstants.OLI_ACTSTAT_COMPLETE);
		if (activityList.size() > 0) {
			activtyPreventProcess = true;
			for (Activity activityObj : activityList) {
				activity = activityObj;
			}
		}
		// End NBLXA-1288
		if (preventProcessData.isPreventsProcess()) {
			if (preventProcessData.isNextOptSuspend()) {
				//check if case can be suspended suspendCase() will return a boolean if case has been suspended before return false else
				// return true
				//if it is to be suspended set suspendReason ->prevent Process
			    checkForMaxSuspendDays(preventProcessData);
				if (suspendCase(preventProcessData, NBA_ROUTEREASON_RESTAUTOUND)) {
					// process NBAVALDERR WI . If it is in End queue and case is suspended then Route WI with REVWVLERR status.
					processNbaValdErrWorkItem();
					result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Suspended", SUSPENDED);
				} else {
					routetoAlternateStatus = true;
					result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "Maximum suspend duration elapsed.", getAlternateStatus());
				}
			}
		} else {
			//This will execute in two cases
			//1 case never had CVs with prevent code 1
			//2 Case had CVs with prevent code 1 previously, that are resolved and case should be routed to alternate status in this case(Risk
			// reevaluated)
			if (routeNeeded && ( (policyExtension != null && policyExtension.hasSuspendInfo()) ||activtyPreventProcess )) { //NBLXA-1288
				SuspendInfo suspendInfo = policyExtension.getSuspendInfo();
				//Check if this has been suspended before because 1 were present, now that 1s have resolved (prevent process=true)
				if ((suspendInfo!=null && NBA_ROUTEREASON_RESTAUTOUND == suspendInfo.getSuspendReason()) || activtyPreventProcess) { // NBLXA-1288
					routetoAlternateStatus = true;
					result = new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "Successful", getAlternateStatus());
					//Route to underwriter risk now.
				}
			}
		}
		if (routetoAlternateStatus && (policyExtension != null ||activtyPreventProcess)) { // NBLXA-1288
			if(policyExtension.hasSuspendInfo()){ // NBLXA-1288
				policyExtension.getSuspendInfo().setActionDelete();
			}
			//Begin QC10953/APSL2851
			List reinInfoList = nbaTxLife.getPrimaryCoverage().getReinsuranceInfo();
			// Begin NBLXA-1288
			if(activity!=null){
				activity.setActionDelete();
			}
			// End NBLXA-1288
			if((NbaUtils.isAdcApplication(getWork())) && (reinInfoList.size()==0)){
				addReinsuranceInfo();
			}
			//End QC10953/APSL2851
			nbaTxLife = doContractUpdate();
			handleHostResponse(nbaTxLife);
			changeStatus(getAlternateStatus());
			doUpdateWorkItem();
		}
		return result;
	}

	/**
	 * @throws NbaBaseException
	 */
	//AXAL3.7.40G new method
	private void processNbaValdErrWorkItem() throws NbaBaseException {
		if (getWork().isCase()) {
			List transactions = getWork().getTransactions(); // get all the transactions
			Iterator transactionItr = transactions.iterator();
			WorkItem workItem;
			while (transactionItr.hasNext()) {
				workItem = (WorkItem) transactionItr.next();
				if (A_WT_NBVALDERR.equals(workItem.getWorkType()) && END_QUEUE.equals(workItem.getQueueID())) {
					NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
					retOpt.setWorkItem(workItem.getItemID(), false);
					retOpt.requestSources();
					retOpt.setLockWorkItem();
					NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt);
					NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), aWorkItem, A_ST_NO_SOURCE);
					if (provider != null && provider.getWorkType() != null && provider.getInitialStatus() != null) {
						aWorkItem.setStatus(provider.getInitialStatus());
						updateWork(getUser(), aWorkItem);
					}
					unlockWork(getUser(), aWorkItem);
				}
			}
		}
	}

	/**
	 * Retreive workitems and sources from AWD and set retreived case to the work object. 
	 * @throws NbaBaseException if RemoteException is thrown by netserver.
	 */
	//NBA224 New Method
	protected void retreiveWorkFromAWD() throws NbaBaseException {
		if (work.isCase()) {
			NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
			retrieveOptionsValueObject.setWorkItem(getWork().getID(), true);
			retrieveOptionsValueObject.requestSources();
			retrieveOptionsValueObject.requestTransactionAsChild();
			retrieveOptionsValueObject.setLockWorkItem();
			setWork(retrieveWorkItem(getUser(), retrieveOptionsValueObject));
		} else {
			setWork(getParentCase());
		}
	}

	/**
	 * Convert the SecureComment to xml string
	 * @return java.lang.String
	 */
	//NBA225 New Method
	protected String toXmlString(SecureComment secureComment) {
		String xml = "";
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		if (stream != null) {
			secureComment.marshal(stream);
			xml = stream.toString();
			try {
				stream.close();
			} catch (java.io.IOException e) {
			}
		}
		return (xml);
	}
	
	
	/**
	 * This methods create a Tentative Disposition object
	 */
	//ALPC19AA New Method
	protected void createTentativeDisp() throws NbaBaseException{
	    TentativeDisp tentativeDisp = new TentativeDisp();
		tentativeDisp.setDisposition(NbaOliConstants.NBA_TENTATIVEDISPOSITION_APPROVED);
		tentativeDisp.setDispLevel(NbaConstants.TENT_DISP_LEVEL_ONE);
		tentativeDisp.setDispUndID(getUser().getUserID());
		tentativeDisp.setDispDate(getCurrentDateFromWorkflow(getUser()));
		tentativeDisp.setDispReason("");
		tentativeDisp.setUWRole(getUser().getUserID());
		tentativeDisp.setUWRoleLevel(getUnderwriterLevel(getUser().getUserID(), NbaTableAccessConstants.WILDCARD,
				NbaTableAccessConstants.WILDCARD));
		tentativeDisp.setActionAdd();
		ApplicationInfoExtension appInfoExt = NbaUtils.getAppInfoExtension(getNbaTxLife().getPolicy().getApplicationInfo());
		appInfoExt.addTentativeDisp(tentativeDisp);
	}	
	
	
	/**
	 * @return Returns the reEvalTransaction.
	 */
	//ALS5260 New Method
	public boolean isReEvalTransaction() {
		return reEvalTransaction;
	}
	/**
	 * @param reEvalTransaction The reEvalTransaction to set.
	 */
	//ALS5260 New Method
	public void setReEvalTransaction(boolean reEvalTransaction) {
		this.reEvalTransaction = reEvalTransaction;
	}
	
	/**
	 * Adds default Reinsurance Info for auto approved cases.
	 * @throws NbaBaseException
	 */
	//ALS5273 New Method
	protected void addReinsuranceInfo() throws NbaBaseException{
		List reinInfoList = nbaTxLife.getPrimaryCoverage().getReinsuranceInfo();
		ReinsuranceInfo reinInfo = null;
		if (reinInfoList != null && reinInfoList.size() >= 1) {
			for (int i = 0; i < reinInfoList.size(); i++) {
				reinInfo = (ReinsuranceInfo) reinInfoList.get(i);
				if (!reinInfo.hasCarrierPartyID()) {//If it is a default reinsurance info tag
					reinInfo.deleteReinsuredAmt();
					reinInfo.deleteRetentionAmt();
					reinInfo.setActionUpdate();
					break;
				}
			}
		} else {
			reinInfo = new ReinsuranceInfo();
			nbaTxLife.getPrimaryCoverage().addReinsuranceInfo(reinInfo);
			reinInfo.setActionAdd();
		}
		reinInfo.setReinsuranceRiskBasis(NbaOliConstants.OLI_REINRISKBASE_NONE);//Set reinsurance risk basis as 'TAI Determined'

		//Begin SR564247(APSL2525)
		ReinsuranceInfoExtension reinInfoExtsn = NbaUtils.getFirstReinsuranceInfoExtension(reinInfo);
		if(reinInfoExtsn == null){
			OLifEExtension olifeExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REINSURANCEINFO);
			reinInfo.addOLifEExtension(olifeExtension);
			reinInfoExtsn = olifeExtension.getReinsuranceInfoExtension();
			reinInfoExtsn.setActionAdd();
		} else {
			reinInfoExtsn.setActionUpdate();
		}
				
		NbaVpmsAdaptor proxy = null;
		try {
			Map deOink = new HashMap();
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess(nbaTxLife);
			deOink.put(A_DELIMITER, NbaVpmsConstants.VPMS_DELIMITER[0]);
			proxy = new NbaVpmsAdaptor(nbaOinkDataAccess, NbaVpmsConstants.ACAUTOUNDERWRITING);
			proxy.setSkipAttributesMap(deOink);
			proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_REINSURANCE_DATA);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
			if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {
				// APSL2179 begin
				reinInfo.setReinsuranceRiskBasis((String)vpmsResultsData.getResultsData().get(0));				
				reinInfoExtsn.setRetentionReason((String)vpmsResultsData.getResultsData().get(1));
				// APSL2179 end
			}
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} catch (NbaBaseException e) {
			throw new NbaBaseException("NbaBaseException Exception occured while processing VP/MS request", e);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException re) {
					NbaLogFactory.getLogger("NbaProcPredictiveAnalysis").logError(re);
				}
			}
		}
		//End SR564247(APSL2525)		
		
		Policy policy = nbaTxLife.getPrimaryHolding().getPolicy();
		policy.setReinsuranceInd(false);
		policy.setActionUpdate();
	}
	
	/**
	 * Returns a string representing the current date from the workflow system. 
	 * @param nbaUserVO NbaUserVO object
	 * @return currentDate Date object
	 * @throws NbaBaseException
	 */
	//ALS5428 new method
	protected Date getCurrentDateFromWorkflow(NbaUserVO nbaUserVO) throws NbaBaseException {
		Date currentDate = null;
		String timeStamp = getTimeStamp(nbaUserVO);
		if (timeStamp != null) {
			currentDate = NbaUtils.getDateFromStringInAWDFormat(timeStamp);
		}
		return currentDate;
	}
	
	
	/**
     * @param message -
     *            secure message text
     * @return boolean
     */
    //ALS5527 - check for multiple automated secure comments
    private boolean duplicateSecureMessage(String message) {
        //Retrieve comments from NbaTXLife object
        List attachmentList = nbaTxLife != null ? nbaTxLife.getPrimaryHolding().getAttachment() : null; //NBA208-12 AXAL3390 null check
        NbaManualCommentType manCommType = null; // added.
        Attachment attachment = null;
        if (null != attachmentList && attachmentList.size() > 0) {
            for (int i = 0; i < attachmentList.size(); i++) {
                attachment = (Attachment) attachmentList.get(i);
                if (NbaOliConstants.OLI_ATTACH_UNDRWRTNOTE == attachment.getAttachmentType()) {
                    //all comments from nbatxlife will be of type notes
                    manCommType = new NbaNoteComment(attachment);
                    if (!NbaUtils.isBlankOrNull(manCommType.getText()) && manCommType.getText().equalsIgnoreCase(message)) {
                        return true;
                    }    
                }
            }
        }

        return false;
    }
    /*
     * If the original work is a tranaction and the case in in N2UNDHLD, release it
     */
    //ALS5415 new method
    private boolean updateCase(boolean isTransaction) throws NbaBaseException {
    	if (!isTransaction) {
    		return true;
    	}
    	//begin ALS3972 
    	if(getWork().getQueue().equalsIgnoreCase(NbaConstants.A_QUEUE_UNDERWRITER_HOLD)){
    		return (getOrigWorkItem().getNbaLob().getUWWBValue() == null) || (!getOrigWorkItem().getNbaLob().getUnderwritingWB());
    	}
    	return false;
    	//end ALS3972
    }
    /*
     * if case is suspended in auto underwriting, unsuspend it
     */
    //ALS5415 new method
    private void unsuspendCase() throws NbaBaseException {
    	if (getWork().isSuspended() && NbaConstants.A_QUEUE_AUTO_UNDERWRITING.equalsIgnoreCase(getWork().getQueue())) {
    		NbaSuspendVO suspendVO = new NbaSuspendVO();
            suspendVO.setCaseID(getWork().getID());
            unsuspendWork(suspendVO);
    	}
    }
    //APSL2536 New Method
    protected boolean updateAndCheckReinsuranceCalculcationLimits(NbaTXLife nbaTXLife) throws NbaBaseException {
		boolean isAmtOfRetentionExceeded = true;
		AxaReinsuranceCalcVO reinCalcVO = new AxaReinsuranceCalcVO();
		reinCalcVO.setNbaTXLife(nbaTXLife);
		reinCalcVO.setAutoRun(true);
		AccelResult result = (AccelResult) currentBP.callBusinessService("CalculateReinsuranceBP", reinCalcVO);
		if (result.hasErrors()) {
			throw new NbaBaseException("Error in calculating the reinsurance retention limits");
		}
		setNbaTxLife(reinCalcVO.getNbaTXLife());
		if(reinCalcVO.getTotalAmtForIssuance() < reinCalcVO.getRetentionAmtAvailable()){
			isAmtOfRetentionExceeded = false;
			addComment("Total Amount available for Issuance is less than the retained amount");
		}
		return isAmtOfRetentionExceeded;
	} 
    
  //APSL4635 - New Method
	protected void addTconvSystemMessage() {
		Holding holding = nbaTxLife.getPrimaryHolding();
		SystemMessage msg = new SystemMessage();
		NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
		nbaOLifEId.setId(msg);
		msg.setMessageCode(NbaConstants.INVALID_RATE_CLASS);
		msg.setRelatedObjectID(holding.getId());
		msg.setSequence("0");
		msg.setMessageSeverityCode(NbaOliConstants.OLI_MSGSEVERITY_INFO);
		msg.setMessageStartDate(new Date(System.currentTimeMillis()));
		msg.setMessageDescription("Multiple policies being converted system cannot calculate new policy rate class.");
		msg.setActionAdd();

		OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_SYSTEMMESSAGE);
		msg.addOLifEExtension(olifeExt);
		SystemMessageExtension systemMessageExtension = olifeExt.getSystemMessageExtension();
		systemMessageExtension.setMsgOverrideInd(false);		
		holding.addSystemMessage(msg);
		holding.setActionUpdate();
	}
    
    //APSL4635 - New Method
    protected void removeTconvSystemMessage() {    	
		if (removeTconvMsg) {
			Holding primaryHolding = nbaTxLife.getPrimaryHolding();
			int count = primaryHolding.getSystemMessageCount();
			for (int i = 0; i < count; i++) {
				SystemMessage sysMsg = primaryHolding.getSystemMessageAt(i);
				if (sysMsg.getMessageCode() == NbaConstants.INVALID_RATE_CLASS) {
					sysMsg.setActionDelete();
					break;
				}
			}
		}
	} 
    
	// APSL4980 New Method
	private void createActivityForInitialReview() throws NbaBaseException {
		List activityList = nbaTxLife.getOLifE().getActivity();
		String userRole = getUser().getUserID();
		if (userRole != null) {
			if (!NbaUtils.isActivityExistForUserRole(activityList, NbaOliConstants.OLI_ACTTYPE_INITIALINDICATOR, userRole)) {
				Activity newActivity = new Activity();
				newActivity.setLastUpdate(new java.sql.Timestamp(System.currentTimeMillis()));
				newActivity.setStartTime(new NbaTime(new java.sql.Timestamp(System.currentTimeMillis())));
				newActivity.setUserCode(userRole);
				newActivity.setActivityStatus(NbaOliConstants.OLI_ACTSTAT_ACTIVE);
				newActivity.setActivityTypeCode(NbaOliConstants.OLI_ACTTYPE_INITIALINDICATOR);
				newActivity.setActionAdd();
				nbaTxLife.getOLifE().setActionUpdate();
				nbaTxLife.getOLifE().addActivity(newActivity);
			}
		}
	}
   
	//NBLXA-2155[NBLXA-2505]
	protected void checkForMaxSuspendDays(AxaPreventProcessVpmsData preventProcessData) {
		Holding primaryHolding = nbaTxLife.getPrimaryHolding();
		Policy policy = primaryHolding.getPolicy();
		if (!NbaUtils.isBlankOrNull(policy) &&
				!NbaUtils.isBlankOrNull(NbaUtils.getPolicyExtension(policy)) &&
				!NbaUtils.isBlankOrNull(policy.getApplicationInfo())) {
			PolicyExtension policyExtension = NbaUtils.getPolicyExtension(policy);
			long IllustrationStatus = policyExtension.getIllustrationStatus();
			ApplicationInfo appInfo = policy.getApplicationInfo();
			long appState = appInfo.getApplicationJurisdiction();
			if (NbaOliConstants.OLIEXT_LU_ILLUSSTAT_NCONF == IllustrationStatus &&
					(appState == NbaOliConstants.OLI_USA_TX || appState == NbaOliConstants.OLI_USA_NY)) {
				Date placementEndDate = appInfo.getPlacementEndDate();
				if (!NbaUtils.isBlankOrNull(placementEndDate)) {
					Date initialSuspendDate = new Date();
					if (policyExtension.hasSuspendInfo()) {
						SuspendInfo suspendInfo = policyExtension.getSuspendInfo();
						if (suspendInfo != null && suspendInfo.hasSuspendDate() && suspendInfo.getUserCode().equals(getUser().getUserID())) {
							initialSuspendDate = policyExtension.getSuspendInfo().getSuspendDate();
						}
					}	
					long diffdays = NbaUtils.getDaysDiff(placementEndDate, initialSuspendDate);
					preventProcessData.setMaxSuspendTime(String.valueOf(diffdays));
				}
			}
		}
	}
}