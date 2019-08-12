package com.csc.fsg.nba.business.process;

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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************************<BR>
 */
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.business.transaction.NbaRequirementUtils;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaMagnumUtils;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.AxaMagnumDecisionServiceVO;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransactionSearchResultVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaXMLDecorator;
import com.csc.fsg.nba.vo.nbaschema.AutomatedProcess;
import com.csc.fsg.nba.vo.nbaschema.CrossReference;
import com.csc.fsg.nba.vo.nbaschema.ReqItem;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vpms.NbaVPMSHelper;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
import com.csc.fsg.nba.vpms.results.ResultData;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

/**
 * NbaProcRedundancyCheck is the class that processes work items found on the NBRDNCHK queue. Redundancy Check reduces the number of ordered
 * requirements by searching for matching requirements for an insured. When matching requirements are found, it determines if those requirements may
 * be used to help satisfy the requirement on which it is operating and, if so, links the two requirements so that each may be processed when the
 * provider response is received.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td></tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * <tr><td>SPR1715</td><td>Version 4</td><td>Wrappered/Standalone Mode Should Be By BackEnd System and by Plan</td></tr>
 * <tr><td>NBA097</td><td>Version 4</td><td>Work Routing Reason Displayed</td></tr>
 * <tr><td>SPR1463</td><td>Version 4</td><td>The Redundancy Process sets the incorrect status in the Requirement Control Source</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>121/1122 Migration</td></tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>Requirement Control Source migration Changes</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR2380</td><td>Version 5</td><td>Cleanup</td></tr>
 * <tr><td>SPR2037</td><td>Version 5</td><td>Requirement receipt date is not set on the redundant requirement</td></tr>
 * <tr><td>SPR2198</td><td>Version 5</td><td>While ordering a requirement which was earlier waived for any other contract, redundancy check process moves the waived requirement work item to error queue</td>
 * </tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>SPR3083</td><td>Version 6</td><td>Change AWDERROR to HOSTERROR in Apply Money and Redundancy Check</td>
 * </tr>
 * <tr><td>SPR3114</td><td>Version 6</td><td>Redundancy Check Should Be Corrected to Only Reference Other Contracts for Redundant Requirements</td>
 * </tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>General Code Clean Up Issues for Version 6</td></tr>
 * <tr><td>NBA146</td><td>Version 6</td><td>Workflow integration</td></tr>
 * <tr><td>NBA192</td><td>Version 7</td><td>Requirement Management Enhancement</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td> </tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.31</td><td>AXA Life Phase 1</td><td>Provider Interfaces</td></tr>
 * </table><p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaProcRedundancyCheck extends NbaAutomatedProcess {
    /** The NbaVpmsVO object which provides data for vpms models */
    protected com.csc.fsg.nba.vpms.NbaVpmsVO vpmsVO = null; //NBA008

    /** An array representing matching work items */
    protected ArrayList matchingWorkItems = new ArrayList(); //NBA008

    /** A boolean variable representing whether an xml is attached or not */
    private boolean xmlAttached = false;

    /** A boolean variable representing whether the provider result is attached or not */
    private boolean provRsltAttached = false;

    //SPR2380 removed logger
    private boolean reqResultAttached = false; //NBA130

    private boolean reqRequestAttached = false; //NBA130
    private boolean beenReceipted = false; //APSL866

    /**
     * NbaProcRedundancyCheck default constructor.
     */
    public NbaProcRedundancyCheck() {
        super();
    }

    //NBA103 - removed method

    /**
     * Links the provider source(s) of the matching work item to the original work item. There may be multiple provider result sources (NBPROVRSLT and
     * NBPROVSUPP).
     * 
     * @param nbaDst
     *            the work item to which the source should be linked
     * @throws NbaBaseException
     */
    //NBA008 New Method
    protected void associateProviderSource(NbaDst nbaDst) throws NbaBaseException {
        NbaSource source = null;
        for (int i = 0; i < nbaDst.getNbaSources().size(); i++) {
            source = (NbaSource) nbaDst.getNbaSources().get(i);
            if (source.isProviderResult() || source.isProviderResultSupplement()) {
                getWork().getNbaTransaction().addNbaSource(source);
                NbaRequirementUtils.markSourceDisplayable(getRequirementInfo(), source, getWork()); // ALS2544//ALS4420
                setProvRsltAttached(true);

            }
        }
    }

    /**
     * This method determines if a requirement XML transaction (NBREQRXML) is associated with the nbaDst work item. If not, it links the XML
     * transaction from the current work item to the nbaDst work item.
     * 
     * @param nbaDst
     *            the work item to which the XML transaction is to be linked
     * @throws NbaBaseException
     */
    //NBA008 New Method
    public void associateXMLSource(NbaDst nbaDst) throws NbaBaseException {
        for (int i = 0; i < nbaDst.getNbaSources().size(); i++) {
            NbaSource nbaSource = (NbaSource) nbaDst.getNbaSources().get(i);
            if (nbaSource.getSource().getSourceType().equals(NbaConstants.A_ST_REQUIREMENT_XML_TRANSACTION)) {
                for (int j = 0; j < getWork().getNbaSources().size(); j++) {
                    NbaSource mySource = (NbaSource) getWork().getNbaSources().get(j);
                    if (mySource.getSource().getSourceType().equals(NbaConstants.A_ST_REQUIREMENT_XML_TRANSACTION)
                            && !(mySource.getID().equals(nbaSource.getID()))) {
                        getWork().getNbaTransaction().addNbaSource(nbaSource);
                        setXmlAttached(true);
                        return;
                    }
                }
            }
        }
    }

    /**
     * This method drives the Redundancy Check process by retrieving the work item from AWD and then searching for matching work items. If none are
     * found, the requirement control source is updated to indicate that Redundancy Check has occurred and the work item moves to the next queue. If
     * matching work items are found, the process checks to see if they have been through this process and, if not, they are removed from the list as
     * they will be processed later. Those that remain are cross-referenced with this work item and all are updated so that their requirement control
     * source contains information about each of the other cross-refernced work items.
     * 
     * @param user
     *            the user for whom the process is being executed
     * @param work
     *            a DST value object for which the process is to occur
     * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
     * @throws NbaBaseException
     */

    public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
        NbaDst nbaDst = null;
        // Initialization
        if (!initialize(user, work)) {
            return getResult();//NBA050
        }
        
         boolean bypassRedundancyProcess=NbaRequirementUtils.isBypassRedundancyCheck(getNbaTxLife(),work);//QC16338/APSL4619
        //NBA008 code deleted - previous code was a pass through without processing
        //NBA008 code added

        // ALS4647 begin
        // From ALS4181 - VPMS code to change P_GetReceiptAllowableDays entry point to return values based on processID. e.g. When REQDET is calling
        // the model, thisvalue must return 365+ to indicate the requirement NEVER expires.
        // Similarly when RDNCHK is calling the same entry point then return with a lower value preferably 0. 
        // The 0 values will ensure that on requirement on different case do not get cross referenced and ordered ALWAYS
        int allowableDays = getAllowableDaysToReceive();
        if (!bypassRedundancyProcess && allowableDays > 0) {
            //ALS4647 end
            //Get requirement control source
            setWork(retrieveWorkItem(getWork()));

            getLogger().logDebug("Getting matching work items");
            //Get matching work items
            NbaSearchVO searchVO = lookupWork();
            if (searchVO.getSearchResults() == null || searchVO.getSearchResults().isEmpty()) {
                //NBA192 no matching work items
                return moveToNextQueue();//ALS4647 code refacotring
            } 
            retrieveMatchingWorkItems(searchVO.getSearchResults().listIterator());
            
            //Update control source with redundancy check section
            NbaSource source = getWork().getRequirementControlSource();
            if (source == null) {
                return moveToErrorQueue();//ALS4647 code rafactoring
            }
            updateRequirementControlSource(source);//ALS4647 code refactoring
            
            getLogger().logDebug("Processing matching work items");

            //Process matching work items
            ArrayList unlockList = new ArrayList(); //no awd updates for these items
            //APSL866 Code deleted
            boolean updateContract = false; //NBA130
            NbaLob lob = getWork().getNbaLob(); //SPR2037
            String polNumber = lob.getPolicyNumber(); //SPR3114
            for (int i = 0; i < getMatchingWorkItems().size(); i++) {
                nbaDst = (NbaDst) getMatchingWorkItems().get(i);
                NbaLob matchingLob = nbaDst.getNbaLob(); //SPR2198 LOB on matching workitems
                if(isValidProviderResult(nbaDst, allowableDays)) { // APSL3807
                //look at maching work items for other contracts and not self
                if (!polNumber.equals(matchingLob.getPolicyNumber()) && isNullOrSameFormNumber(matchingLob, lob)) { //SPR3114 //ALNA571
                    //Need to check if the matching requirement ever passed through this process so that cross reference should be updated
                    //However if the mathing requirement workitem is cancelled or waived, we don't want to touch that workitem.
                    // Begin AXAL3.7.31
                    // Since agent-ordered, case matched and "Add"-ed requirements bypass Redundancy check, we should allow the requirement
                    // to proceed in the logic as long as its status indicates that it is not outstanding
                    if ((hasRedundancyCheck(nbaDst) || NbaUtils.isRequirementFulfilled(matchingLob.getReqStatus()))//ALS4892
                            // End AXAL3.7.31
                            && !(matchingLob.getReqStatus().equals(String.valueOf(NbaOliConstants.OLI_REQSTAT_CANCELLED)) || matchingLob
                                    .getReqStatus().equals(String.valueOf(NbaOliConstants.OLI_REQSTAT_WAIVED)))) { //APSL4109 Code Reverted//SPR2198
							if (beenReceipted == false) {
								// SPR2198 code deleted
								// BEGIN NBA130
								NbaTXLife parentTXLife = doHoldingInquiry(nbaDst, READ, NbaUtils.getBusinessProcessId(getUser()));
								RequirementInfo matchingReqInfo = parentTXLife.getRequirementInfo(matchingLob.getReqUniqueID());
								if (!isReqRequestAttached()) {
									attachRequirementRequest(matchingReqInfo);
									updateContract = true;
								}
								// END NBA130
								// ALS5677 Code Moved

								// ALS4647 begin
								if (matchingLob.getReqReceiptDate() == null && matchingLob.getParamedSignDate() == null) {
									lob.setReqStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_ADD)); // SPR2037
									requirementInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_ADD); // NBA130
									requirementInfo.setActionUpdate(); // NBA130
									if (!isXmlAttached()) {
										associateXMLSource(nbaDst);
									}
								} else if (isValidProviderResult(nbaDst, allowableDays)) {// ALS4892 ALS5677
									// ALS5643 Code moved from above
									// Begin AXAL3.7.31
									// Because of partial results, we need to transfer results independent of the status
									if (hasProviderSource(nbaDst)) {// ALS5643
										if (!isProvRsltAttached()) {
											associateProviderSource(nbaDst);
										}
										attachRequirementResult(matchingReqInfo);
									} // ALS4647 else block deleted
									if (NbaUtils.isRequirementFulfilled(matchingLob.getReqStatus())) {// ALS5677
										lob.setParamedSignDate(matchingLob.getParamedSignDate());
										lob.setReqStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_RECEIVED)); // SPR2037
										lob.setReqReceiptDate(matchingLob.getReqReceiptDate()); // SPR2037
										lob.setReqReceiptDateTime(matchingLob.getReqReceiptDateTime()); // QC20240
										lob.setLabCollectionDate(matchingLob.getLabCollectionDate()); // NBLXA-1794
										requirementInfo.setReceivedDate(matchingLob.getReqReceiptDate());
										requirementInfo.setFulfilledDate(matchingLob.getReqReceiptDate());
										requirementInfo.setReqStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_RECEIVED));
										requirementInfo.setActionUpdate();
										// NBLXA-2408 Start
										List sources = getWork().getNbaSources();
										if (sources.size() > 0 && NbaVPMSHelper.isSupplementTabForm(lob)) {
											NbaDst parentCase = retrieveParentWork(getWork(), true, false);
											if (parentCase != null) {
												Iterator<NbaSource> sourceIterator = sources.iterator();
												NbaSource currentSource = null;
												while (sourceIterator.hasNext()) {
													currentSource = sourceIterator.next();
													if (currentSource.getNbaLob()!=null && currentSource.getSourceType().equalsIgnoreCase(NbaConstants.A_ST_PROVIDER_RESULT)) {
														if ((lob.getReqType() == NbaOliConstants.OLI_REQCODE_1009800033
																&& currentSource.getNbaLob().getReqType() == NbaOliConstants.OLI_REQCODE_1009800033)
																|| (lob.getReqType() != NbaOliConstants.OLI_REQCODE_1009800033 && currentSource
																		.getNbaLob().getReqType() != NbaOliConstants.OLI_REQCODE_1009800033))
														parentCase.addNbaSource(currentSource);
														updateJointInsuredLOB(currentSource, getNbaTxLife());
														updateWork(getUser(), parentCase);
														break;
													}
												}

											}
										}
										// End NBLXA-2408
										// ALS5343 Begin
										RequirementInfoExtension reqInfoExtn = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
										if (reqInfoExtn != null) {
											reqInfoExtn.setParamedSignedDate(matchingLob.getParamedSignDate());// ALS5533
											reqInfoExtn.setLabCollectedDate(matchingLob.getLabCollectionDate());// NBLXA-1794
											reqInfoExtn.setReceivedDateTime(matchingLob.getReqReceiptDateTime());// QC20240
											reqInfoExtn.setActionUpdate();
										} else {
											OLifEExtension oExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
											if (oExt != null) {
												reqInfoExtn = oExt.getRequirementInfoExtension();
												reqInfoExtn.setParamedSignedDate(matchingLob.getParamedSignDate());// ALS5533
												reqInfoExtn.setLabCollectedDate(matchingLob.getLabCollectionDate());// NBLXA-1794
												reqInfoExtn.setReceivedDateTime(matchingLob.getReqReceiptDateTime());// QC20240
												reqInfoExtn.setActionAdd();
												requirementInfo.addOLifEExtension(oExt);
											}
										}
										// ALS5343 End
										beenReceipted = true;
									}
								}
								// ALS4647 end
								// NBLXA-2402 (NBLXA-2564) US 323027 | Start
								if(NbaOliConstants.OLI_REQCODE_MEDEXAMPARAMED == requirementInfo.getReqCode() && beenReceipted){
									
									adoptMagnumParamedCaseId(matchingReqInfo, parentTXLife);
									
								}
								// NBLXA-2402 (NBLXA-2564) US 323027 | End
							}
                        // End AXAL3.7.31
                        updateCrossReference(nbaDst);
                        updateContract = true; //NBA130
                    } else {
                        unlockList.add(nbaDst);
                    }
                }//SPR3114
            } else { //APSL3807
                  unlockList.add(nbaDst); //APSL3807
            }
            }
            //Begin NBA130
            if (updateContract) {
                nbaTxLife.getPolicy().setActionUpdate();
                doContractUpdate();
            }
            //End NBA130
            getLogger().logDebug("Remove unlocklist items from further processing");
            //Remove unlocklist items from further processing
            for (int i = 0; i < unlockList.size(); i++) {
                getMatchingWorkItems().remove(unlockList.get(i));
                unlockWorkItem((NbaDst) unlockList.get(i));
            }

            //Update matching work items on AWD
            updateMatchinWorkitems(); //ALS4647 code refactoring
            
        }//ALS4647 end of allowable check
        
        return moveToNextQueue();//ALS4647 Code refactoring
    }

    /**
     * NBLXA-2402 (NBLXA-2564) US 323027 | New Method
     * @param matchingReqInfo
     * @param parentTXLife
     * @param holdingInquiry
     */
	private void adoptMagnumParamedCaseId(RequirementInfo matchingReqInfo, NbaTXLife parentTXLife) throws NbaBaseException {

		String matchingreqInfoId = matchingReqInfo.getId();
		Relation parentMagnumRelation = parentTXLife.getRelationByRelatedIdAndRelationRoleCode(matchingreqInfoId, NbaOliConstants.OLI_REL_MAGNUM_CASE);
		
		if (!NbaUtils.isBlankOrNull(parentMagnumRelation)) {
			
			String holdingId = parentMagnumRelation.getOriginatingObjectID();
			Holding parentMagnumHolding = parentTXLife.getHolding(holdingId);

			if (!NbaUtils.isBlankOrNull(parentMagnumHolding)) {
				Holding magnumHolding = AxaMagnumUtils.createHoldingAndRelation(getNbaTxLife(), parentMagnumHolding, requirementInfo);
				//Starts NBLXA-2402 (NBLXA-2586)
				AxaMagnumDecisionServiceVO magnumDecisionServiceVO = new AxaMagnumDecisionServiceVO(); 
				magnumDecisionServiceVO.setMagnumHolding(magnumHolding);
				magnumDecisionServiceVO.setMagnumProcess(AxaMagnumUtils.getMagnumProcessForRequirement(requirementInfo.getReqCode(),getUser().getUserID()));
				//Ends NBLXA-2402 (NBLXA-2586)	
				// NBLXA-2402 (NBLXA-2565)(NBLXA-2586) US 342800 | Start,(NBLXA-2586)
				AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.RS_MAGNUM_DETAILED_DECISION, getUser(),
						getNbaTxLife(), null, magnumDecisionServiceVO);
				webServiceInvoker.execute();
				// NBLXA-2402 (NBLXA-2565) US 342800 | End
			}
		}
	}
    
    
    /**
     * This method searches the Attachment objects for the Requirement Request, and if located, attaches it to the RequirementInfo object. It also
     * sets the reqRequestAttached to true if the RequirementRequest is found and attached.
     * 
     * @param matchingReqInfo
     *            the RequirementInfo object that potentially has the RequirementRequest
     * @throws NbaBaseException
     */
    //NBA130 NEW METHOD
    private void attachRequirementRequest(RequirementInfo matchingReqInfo) throws NbaBaseException {
        if (matchingReqInfo == null || matchingReqInfo.getAttachmentCount() == 0) { //NBA208-32
            return;
        }
        Iterator attachments = matchingReqInfo.getAttachment().iterator();
        while (attachments.hasNext()) {
            Attachment attach = (Attachment) attachments.next();
            long attachType = attach.getAttachmentType();
            if ((NbaOliConstants.OLI_REQCODE_MIBCHECK != getWork().getNbaLob().getReqType() && NbaOliConstants.OLI_ATTACH_REQUIREREQUEST == attachType)
                    || (NbaOliConstants.OLI_REQCODE_MIBCHECK == getWork().getNbaLob().getReqType() && NbaOliConstants.OLI_ATTACH_MIB401 == attachType)) {
                requirementInfo.addAttachment(createNewAttachment(attach));
                requirementInfo.setActionUpdate();
                setReqRequestAttached(true);
            }
        }
    }

    /**
     * This method attaches the RequirementResult to the RequirementInfo object
     * 
     * @param matchingReqInfo
     * @throws NbaBaseException
     */
    // NBA130 NEW METHOD
    private void attachRequirementResult(RequirementInfo matchingReqInfo) throws NbaBaseException {
        if (matchingReqInfo.getAttachmentCount() == 0) {
            return;
        }
        Iterator attachments = matchingReqInfo.getAttachment().iterator();
        while (attachments.hasNext()) {
            Attachment attach = (Attachment) attachments.next();
            long attachType = attach.getAttachmentType();
            if ((NbaOliConstants.OLI_REQCODE_MIBCHECK != getWork().getNbaLob().getReqType() && NbaOliConstants.OLI_ATTACH_REQUIRERESULTS == attachType)
                    || (NbaOliConstants.OLI_REQCODE_MIBCHECK == getWork().getNbaLob().getReqType() && (NbaOliConstants.OLI_ATTACH_MIB_SERVRESP == attachType || NbaOliConstants.OLI_ATTACH_MIB401 == attachType))) {
                requirementInfo.addAttachment(createNewAttachment(attach));
                requirementInfo.setActionUpdate();
            }
        }
    }

    /**
     * @param attach
     * @return
     */
    private Attachment createNewAttachment(Attachment attach) {
        Attachment newAttach = attach.clone(false);
        newAttach.setId(null);
        getNbaOLifEId().setId(newAttach);
        newAttach.setActionAdd();
        if (attach.hasAttachmentData()) {
            AttachmentData newAttachData = attach.getAttachmentData().clone(false);
            newAttachData.setActionAdd();
            newAttach.setAttachmentData(newAttachData);
        }
        return newAttach;
    }

    /**
     * Answer the value of "Received within allowable days" from the VP/MS model.
     * 
     * @return number of allowable days.
     * @throws NbaBaseException
     */
    //NBA008 New Method
    protected int getAllowableDaysToReceive() throws NbaBaseException {
        int days = 0;
        List daysList = getDataFromVpms(NbaVpmsAdaptor.EP_RECEIPT_ALLOWABLE_DAYS).getResultsData();
        if (daysList != null && daysList.size() > 0) {
            days = Integer.parseInt(daysList.get(0).toString());
        }

        return days;
    }

    /**
     * This method retreives data from the Requirements VP/MS model based on the entryPoint passed in.
     * 
     * @param entryPoint
     *            the entry point to be executed in the VP/MS model.
     * @return the results of the call to the VP/MS model
     * @NbaVpmsException
     */
    //NBA008 New Method
    public NbaVpmsResultsData getDataFromVpms(String entryPoint) throws NbaBaseException, NbaVpmsException {
        NbaVpmsAdaptor vpmsProxy = null; //SPR3362
        try {
            NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
            oinkData.setContractSource(getNbaTxLife());//ALS4366
            Map deOink = new HashMap();
            deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(getUser())); //SPR2639
            vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REQUIREMENTS); //SPR3362
            vpmsProxy.setVpmsEntryPoint(entryPoint);
            vpmsProxy.setSkipAttributesMap(deOink);
            NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
            //SPR3362 code deleted
            return data;
        } catch (java.rmi.RemoteException re) {
            throw new NbaVpmsException("Redundancy Check problem" + NbaVpmsException.VPMS_EXCEPTION, re);
            //begin SPR3362
        } finally {
            try {
                if (vpmsProxy != null) {
                    vpmsProxy.remove();
                }
            } catch (RemoteException re) {
                getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
            }
        }
        //end SPR3362
    }

    /**
     * Answer the matching work items.
     * 
     * @return java.util.ArrayList
     */
    //NBA008 New Method
    protected java.util.ArrayList getMatchingWorkItems() {
        return matchingWorkItems;
    }

    /**
     * Answer the order requirement queue suspend minutes
     * 
     * @return the number of minutes which a work item in the order requirement queue may be suspeneded
     * @throws NbaBaseException
     */
    //NBA008 New Method
    protected int getSuspendMinutes() throws NbaBaseException {
        String suspendMinutes = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
                NbaConfigurationConstants.ORDER_REQUIREMENT_SUSPEND_MINUTES); //ACN012
        if (suspendMinutes != null) { //ACN012
            return Integer.parseInt(suspendMinutes); //ACN012
        }
        return 0;

    }

    /**
     * Answer true if the work item has a provider result source or a provider supplement source.
     * 
     * @param nbaDst
     *            a matching work item
     * @return <code>true</code> if a source exists; otherwise, <code>false</code>
     * @throws NbaBaseException
     */
    //NBA008 New Method
    protected boolean hasProviderSource(NbaDst nbaDst) throws NbaBaseException {
        if (getLogger().isDebugEnabled()) { // NBA027
            getLogger().logDebug("APRDNCHK Starting hasProviderSource for " + nbaDst.getID());
        } // NBA027
        for (int i = 0; i < nbaDst.getNbaSources().size(); i++) {
            NbaSource nbaSource = (NbaSource) nbaDst.getNbaSources().get(i);
            if (nbaSource.isProviderResult() || nbaSource.isProviderResultSupplement()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Answer true if a redundancy check section exists (i.e., processId = APRDNCHK)
     * 
     * @param nbaDst
     *            a work item
     * @return <code>true</code> if redundancy check section exists; otherwise, <code>false</code>
     * @throws NbaBaseException
     */
    //NBA008 New Method
    protected boolean hasRedundancyCheck(NbaDst nbaDst) throws NbaBaseException {
        if (getLogger().isDebugEnabled()) { // NBA027
            getLogger().logDebug("APRDNCHK Starting hasRedundancyCheck for " + nbaDst.getID());
        } // NBA027
        if (nbaDst.getRequirementControlSource() == null) {
            return false;
        }
        NbaXMLDecorator rcs = new NbaXMLDecorator(nbaDst.getRequirementControlSource().getText());
        AutomatedProcess ap = rcs.getAutomatedProcess(NbaUtils.getBusinessProcessId(getUser())); //SPR2639
        if (ap == null) {
            return false;
        }
        return true;

    }

    /**
     * Answers if a provider result is attached
     * 
     * @return boolean
     */
    public boolean isProvRsltAttached() {
        return provRsltAttached;
    }

    /**
     * If the provider result for a matching work item has been received, this task first computes the earliest valid receipt date by subtracting the
     * Requirement Received Allowable Days from the current system date. To be considered redundant, the matching requirement must have either no
     * receipt date or the receipt date must fall between the calculated date and the current date.
     * 
     * @param nbaDst
     *            a work item
     * @param <code>true<code> if provider result is valid; else, <code>false</code>
     * @return Boolean variable representing whether the provider result for a matching work item is valid or not
     * @throws NbaBaseException
     */
    //NBA008 New Method
    protected boolean isValidProviderResult(NbaDst nbaDst, int getAllowableDaysToReceive) throws NbaBaseException { //ALS4647
        if (getLogger().isDebugEnabled()) { // NBA027
            getLogger().logDebug("APRDNCHK Starting isValidProviderResult for " + nbaDst.getID());
        } // NBA027
        GregorianCalendar calendar = new GregorianCalendar();
        Date currentDate = new Date();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_YEAR, -getAllowableDaysToReceive);
        Date earliestValidDate = calendar.getTime();

        //AXAL3.7.31 begin
        // If Paramed Sign Date is present, use that for the comparison instead of the Requirement Receipt Date
        if (nbaDst.getNbaLob().getParamedSignDate() != null) {
            if (earliestValidDate.compareTo(nbaDst.getNbaLob().getParamedSignDate()) > 0) {
                return false; //receipt date is before the earliest valid date
            }
            return true;
        }
        //AXAL3.7.31 end
        
        if (nbaDst.getNbaLob().getReqReceiptDate() == null) {//ALS5643
            return false; //no receipt date
        }
        if (earliestValidDate.compareTo(nbaDst.getNbaLob().getReqReceiptDate()) > 0) {
            return false; //receipt date is before the earliest valid date
        }
        return true;

    }

    /**
     * Answers the xmlAttached member
     * 
     * @return boolean
     */
    public boolean isXmlAttached() {
        return xmlAttached;
    }

    /**
     * Search for matching work items. Iterate over the criteria (sets of LOB fields) to be used in the search until a successfull search is
     * performed. For each search, the LOB values identifed in the criteria set are copied from the work item to the SearchVo. Then the LOB values are
     * examined to verify that values for all LOBs were present on the work item. If not, the set is bypassed.
     * 
     * @return the search value object containing the result of the search
     * @throws NbaBaseException
     */
    //NBA008 New Method
    public NbaSearchVO lookupWork() throws NbaBaseException {
        //begin NBA192
        List criteriaSets = getVpmsLookupCriteria();
        NbaSearchVO searchVO = new NbaSearchVO();
        searchVO.setResultClassName(NbaSearchVO.TRANSACTION_SEARCH_RESULT_CLASS);
        searchVO.setWorkType(A_WT_REQUIREMENT);
        int setCount = criteriaSets.size();
        List searchLobs = null;
        for (int i = 0; i < setCount; i++) {
            searchLobs = (List) criteriaSets.get(i);
            if (searchLobs.size() > 0 && checkLobPresence(getWorkLobs(), searchLobs)) { //Perform a search only if all LOB values are present on
                                                                                        // work.
                searchVO.setNbaLob(getNbaLobForLookup((ArrayList) searchLobs));
                searchVO = lookupWork(getUser(), searchVO); //NBA213
                if (searchVO.getSearchResults().size() > 0) {
                    //Begin ALS4237
                    for (int j = 0; j < searchVO.getSearchResults().size(); j++) {
                        NbaTransactionSearchResultVO resultVO = (NbaTransactionSearchResultVO) searchVO.getSearchResults().get(j);
                        if (!(resultVO.getTransactionID().equals(getWork().getID()))) {
                            return searchVO;
                        }
                    }
                    //End ALS4237
                }
            }
        }
        return (new NbaSearchVO()); //ALS4237
        //end NBA192
    }

    /**
     * Call a model to retrieve the search criteria sets (LOBs to be compared). The results are returned as an array with an entry for each set.
     * 
     * @return a List containing the search criteria sets
     * @throws NbaBaseException
     */
    //NBA192 New Method
    protected List getVpmsLookupCriteria() throws NbaBaseException {
        NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWorkLobs());
        oinkData.setContractSource(getNbaTxLife());
        VpmsComputeResult computeResult = getDataFromVpms(NbaVpmsAdaptor.REQUIREMENTS, NbaVpmsAdaptor.EP_GET_MATCHING_SEARCH_CRITERIA, oinkData,
                null, null);
        NbaVpmsModelResult modelResult = new NbaVpmsModelResult(computeResult.getResult());

        List criteriaSets = new ArrayList();
        List lobList = null;
        if (modelResult.getVpmsModelResult() != null && modelResult.getVpmsModelResult().getResultDataCount() > 0) {
            int criteriaCount = modelResult.getVpmsModelResult().getResultDataCount();
            ResultData resultData = null;
            for (int i = 0; i < criteriaCount; i++) {
                resultData = modelResult.getVpmsModelResult().getResultDataAt(i); //Next criteria
                int resultCount = resultData.getResultCount();
                if (resultCount > 0) {
                    lobList = new ArrayList();
                    for (int j = 0; j < resultCount; j++) {
                        lobList.add(resultData.getResultAt(j));
                    }
                    criteriaSets.add(lobList);
                }
            }
        }
        return criteriaSets;
    }

    /**
     * This method retrieves the work items referenced in the NbaSearchResultVO object.
     * 
     * @param results
     *            a list of matching work items
     * @throws NbaBaseException
     */
    //NBA008 New Method
    public void retrieveMatchingWorkItems(ListIterator results) throws NbaBaseException {
        // SPR3290 code deleted
        while (results.hasNext()) {
            NbaTransactionSearchResultVO resultVO = (NbaTransactionSearchResultVO) results.next();
            if (!(resultVO.getTransactionID().equals(getWork().getID()))) {
                retrieveWorkItem(resultVO);
            }
        }
    }

    /**
     * This method retrieves a work item from AWD.
     * 
     * @param nbaDst
     *            a work item to be retrieved
     * @return the retrieved work item
     * @throws NbaBaseException
     */
    //NBA008 New Method
    public NbaDst retrieveWorkItem(NbaDst nbaDst) throws NbaBaseException {
        if (getLogger().isDebugEnabled()) { // NBA027
            getLogger().logDebug("APRDNCHK Starting retrieveWorkItem for " + nbaDst.getID());
        } // NBA027
        //NBA213 deleted code
        NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
        retrieveOptionsValueObject.setWorkItem(nbaDst.getID(), false);
        retrieveOptionsValueObject.requestSources();
        return retrieveWorkItem(getUser(), retrieveOptionsValueObject); //NBA213
    }

    /**
     * This method retrieves the work item referenced in the NbaSearchResultVO object.
     * 
     * @param resultVO
     *            the work item to be retrieved; may be a requirement work item or a temporary work item
     * @return the retrieved work item
     * @throws NbaBaseException
     */
    //NBA008 New Method
    public void retrieveWorkItem(NbaTransactionSearchResultVO resultVO) throws NbaBaseException {
		if (getLogger().isDebugEnabled()) { // NBA027
			getLogger().logDebug("APRDNCHK Starting retrieveWorkItem");
		} // NBA027
		//NBA213 deleted code
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(resultVO.getTransactionID(), false);
		retOpt.requestSources();
		retOpt.setLockTransaction();
		retOpt.setAutoSuspend();
		NbaDst aWorkItem = retrieveWorkItem(getUser(), retOpt); //NBA213
		if (!NbaUtils.isBlankOrNull(aWorkItem.getNbaLob().getPolicyNumber())) { //APSL2198
			getMatchingWorkItems().add(aWorkItem); //APSL2198 To prevent miscmail without pol no to be added in matching list.
			//NBA213 deleted code
		}
	}

    /**
	 * Initializes the provRsltAttached member
	 * 
	 * @param newProvRsltAttached
	 *            boolean
	 */
    public void setProvRsltAttached(boolean newProvRsltAttached) {
        provRsltAttached = newProvRsltAttached;
    }

    /**
     * Initializes the xmlAttached member
     * 
     * @param newXmlAttached
     *            boolean
     */
    public void setXmlAttached(boolean newXmlAttached) {
        xmlAttached = newXmlAttached;
    }

    /**
     * This method suspends a work item by calculating the activation date based on the number of minutes specified in the NbaConfiguration file.
     * 
     * @throws NbaBaseException
     */
    //NBA008 New Method
    public void suspendTransaction() throws NbaBaseException {
        getLogger().logDebug("Starting suspendTransaction"); //NBA044
        NbaSuspendVO suspendVO = new NbaSuspendVO();
        suspendVO.setTransactionID(getWork().getID());
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, getSuspendMinutes());
        Date reqSusDate = (calendar.getTime());
        suspendVO.setActivationDate(reqSusDate);
        updateForSuspend(suspendVO);
    }

    /**
     * This method unlocks the original work item and all matching work items.
     * 
     * @throws NbaBaseException
     */
    //NBA008 New Method
    public void unlockAllWorkItems() throws NbaBaseException {
        getLogger().logDebug("APRDNCHK Starting unlockAllWorkItems");
        unlockWorkItem(getWork()); //unlock original
        for (int i = 0; i < getMatchingWorkItems().size(); i++) {
            unlockWorkItem((NbaDst) getMatchingWorkItems().get(i));
        }

    }

    /**
     * Unlocks the work item by calling the NbaNetServerAccessor EJB.
     * 
     * @param nbaDst
     *            the work item to be unlocked
     * @throws NbaBaseException
     */
    //NBA008 New Method
    protected void unlockWorkItem(NbaDst nbaDst) throws NbaBaseException {
        if (getLogger().isDebugEnabled()) { // NBA027
            getLogger().logDebug("APRDNCHK Starting unlockWorkItem for " + nbaDst.getID());
        } // NBA027
        unlockWork(getUser(), nbaDst); //NBA213
    }

    /**
     * Updates the work item in AWD.
     * 
     * @param workItem
     *            a work item to be updated
     * @throws NbaBaseException
     */
    //NBA008 New Method
    protected void updateAWD(NbaDst workItem) throws NbaBaseException {
        if (getLogger().isDebugEnabled()) { // NBA027
            getLogger().logDebug("APRDNCHK Starting updateAWD for " + workItem.getID());
        } // NBA027
        updateWork(getUser(), workItem); //NBA213
    }

    /**
     * This method updates the cross reference section of the Requirement Control Source of both the original work item and the matching work item.
     * 
     * @param nbaDst
     *            a matching work item
     * @throws NbaBaseException
     */
    //NBA008 New Method
    public void updateCrossReference(NbaDst nbaDst) throws NbaBaseException {
        if (getLogger().isDebugEnabled()) { // NBA027
            getLogger().logDebug("APRDNCHK Starting updateCrossReference for " + nbaDst.getID());
        } // NBA027
        NbaSource source = null;
        NbaXMLDecorator xmlDecorator = null;
        AutomatedProcess originalAp, matchingAp = null;

        //add cross reference to original
        source = getWork().getRequirementControlSource();
        xmlDecorator = new NbaXMLDecorator(source.getText());
        originalAp = xmlDecorator.getAutomatedProcess(NbaUtils.getBusinessProcessId(getUser())); //SPR2639
        ReqItem matchingReqItem = new ReqItem();
        matchingReqItem.setAwdId(nbaDst.getID());
        matchingReqItem.setPolicyNumber(nbaDst.getNbaLob().getPolicyNumber());
        if (originalAp.hasCrossReference()) {
            originalAp.getCrossReference().addReqItem(matchingReqItem);
        } else {
            CrossReference cr = new CrossReference();
            originalAp.setCrossReference(cr);
            originalAp.getCrossReference().addReqItem(matchingReqItem);
        }
        //BEGIN NBA130 add CrossReference
        com.csc.fsg.nba.vo.txlife.CrossReference xRef = new com.csc.fsg.nba.vo.txlife.CrossReference();
        xRef.setActionAdd();
        xRef.setKeys(requirementInfo.getKeys());
        xRef.setPolNumber(nbaDst.getNbaLob().getPolicyNumber());
        //NBA208-32
        xRef.setWorkitemID(nbaDst.getTransaction().getItemID());
        xRef.setRelatedRefID(nbaDst.getNbaLob().getReqUniqueID());
        getNbaOLifEId().setId(xRef);
        xRef.setActionAdd();
        RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
        if (reqInfoExt == null) {
            OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO); //SPR2992
            reqInfoExt = olifeExt.getRequirementInfoExtension();
            reqInfoExt.setActionAdd();
            requirementInfo.addOLifEExtension(olifeExt);
        } else {
            reqInfoExt.setActionUpdate();
        }
        reqInfoExt.setCrossReference(xRef);
        requirementInfo.setActionUpdate();
        //END NBA130
        // Begin AXAL3.7.31
        // Don't set the original status here - it was set earlier.
        //originalAp.setOriginalStatus(Long.toString(NbaOliConstants.OLI_REQSTAT_ADD));//SPR1463
        // End AXAL3.7.31
        source.updateText(xmlDecorator.toXmlString());
        NbaRequirementUtils reqUtils = new NbaRequirementUtils(); //ACN014
        reqUtils.updateRequirementControlSource(getWork(), null, xmlDecorator.toXmlString(), NbaRequirementUtils.actionUpdate);//ACN014 SPR2992

        //add cross reference to matching
        source = nbaDst.getRequirementControlSource();
        xmlDecorator = new NbaXMLDecorator(source.getText());
        //NBA208-32
        matchingAp = xmlDecorator.getAutomatedProcess(NbaUtils.getBusinessProcessId(nbaDst.getUserID())); //SPR2639
        //Begin AXAL3.7.31
        if (matchingAp == null) {
            matchingAp = new AutomatedProcess();
            matchingAp.setProcessId(NbaUtils.getBusinessProcessId(getUser()));
            matchingAp.setOriginalStatus(getWork().getNbaLob().getReqStatus());
            xmlDecorator.getRequirement().addAutomatedProcess(matchingAp);
            source.updateText(xmlDecorator.toXmlString());
        }
        // End AXAL3.7.31
        ReqItem originalReqItem = new ReqItem();
        originalReqItem.setAwdId(getWork().getID());
        originalReqItem.setPolicyNumber(getWork().getNbaLob().getPolicyNumber());
        if (matchingAp.hasCrossReference()) {
            matchingAp.getCrossReference().addReqItem(originalReqItem);
        } else {
            CrossReference cr = new CrossReference();
            matchingAp.setCrossReference(cr);
            matchingAp.getCrossReference().addReqItem(originalReqItem);
        }
        reqUtils.updateRequirementControlSource(nbaDst, null, xmlDecorator.toXmlString(), NbaRequirementUtils.actionUpdate);//ACN014 SPR2992

    }

    /**
     * Since the work item must be suspended before it can be unlocked, this method is used instead of the superclass method to update AWD.
     * <P>
     * This method updates the work item in the AWD system, suspends the work item using the supsendVO, and then unlocks the work item.
     * 
     * @param suspendVO
     *            the suspend value object used to suspend the work item
     * @throws NbaBaseException
     */
    //NBA008 New Method
    public void updateForSuspend(NbaSuspendVO suspendVO) throws NbaBaseException {
        getLogger().logDebug("APRDNCHK Starting updateForSuspend");
        updateWork(getUser(), getWork()); //NBA213
        suspendWork(getUser(), suspendVO); //NBA213
        unlockWork(getUser(), getWork()); //NBA213
    }

    /**
     * Answers the reqRequestAttached
     * 
     * @return the reqRequestAttached.
     */
    //NBA130 New Method
    public boolean isReqRequestAttached() {
        return reqRequestAttached;
    }

    /**
     * Sets the reqRequestAttached variable
     * 
     * @param the
     *            reqRequestAttached to set.
     */
    //NBA130 New Method
    public void setReqRequestAttached(boolean reqRequestAttached) {
        this.reqRequestAttached = reqRequestAttached;
    }

    /**
     * Answers the reqResultAttached
     * @return the reqResultAttached.
     */
    //NBA130 New Method
    public boolean isReqResultAttached() {
        return reqResultAttached;
    }

    /**
     * Sets the reqResultAttached variable
     * @param the reqResultAttached to set.
     */
    //NBA130 New Method
    public void setReqResultAttached(boolean reqResultAttached) {
        this.reqResultAttached = reqResultAttached;
    }

    //ALS4892 code moved to Utils

    //ALS4647 Code refacotring
    private void updateRequirementControlSource(NbaSource source) throws NbaBaseException {
        NbaXMLDecorator xmlDecorator = new NbaXMLDecorator(source.getText());
        AutomatedProcess ap = xmlDecorator.getAutomatedProcess(NbaUtils.getBusinessProcessId(getUser())); //SPR2639
        if (ap == null) {
            ap = new AutomatedProcess();
            ap.setProcessId(NbaUtils.getBusinessProcessId(getUser())); //SPR2639
            ap.setOriginalStatus(getWork().getNbaLob().getReqStatus());
            xmlDecorator.getRequirement().addAutomatedProcess(ap);
            source.updateText(xmlDecorator.toXmlString());
            NbaRequirementUtils reqUtils = new NbaRequirementUtils();//ACN014
            reqUtils.updateRequirementControlSource(getWork(), null, xmlDecorator.toXmlString(), NbaRequirementUtils.actionUpdate); //ACN014 SPR2992
        }
    }

    //ALS4647 code refactoring
    private NbaAutomatedProcessResult moveToErrorQueue() throws NbaBaseException {
        changeStatus(getAwdErrorStatus()); //NBA097 SPR3083 NBA213
        doUpdateWorkItem();//SPR3083
        setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, getHostErrorStatus(), getHostErrorStatus())); //SPR3083
        return getResult();
    }

    //ALS4647 code refactoring
    private NbaAutomatedProcessResult moveToNextQueue() throws NbaBaseException {
		Map deOinkMap = new HashMap();	//APSL866
		deOinkMap.put("A_IsRedundantReq", beenReceipted ? "true" : "false");	//APSL866
		NbaProcessStatusProvider wiStatus = new NbaProcessStatusProvider(getUser(), getWork(), nbaTxLife, deOinkMap); //SPR1715, APSL866
        changeStatus(wiStatus.getPassStatus()); //NBA097
        doUpdateWorkItem();
        setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
        return getResult();
    }
    
    //ALS4647 code refactoring
    private void updateMatchinWorkitems() throws NbaBaseException{
        getLogger().logDebug("Updating matching work items on AWD");
        boolean debugLogging = getLogger().isDebugEnabled(); // NBA027
        NbaDst nbaDst = null;
        for (int i = 0; i < getMatchingWorkItems().size(); i++) {
            nbaDst = (NbaDst) getMatchingWorkItems().get(i);
            if (debugLogging) { // NBA027
                getLogger().logDebug("Updating work item " + nbaDst.getID());
            } // NBA027
            updateAWD(nbaDst);
            unlockWorkItem(nbaDst);
        }
    }
    //ALNA571 new method
    private boolean isNullOrSameFormNumber(NbaLob matchingLob, NbaLob lob){
    	if(!NbaUtils.isBlankOrNull(lob.getFormNumber())){
    		return lob.getFormNumber().equalsIgnoreCase(matchingLob.getFormNumber());
    	} 
    	return true;
    }
    //NBLXA-2408 new method
   	protected void updateJointInsuredLOB(NbaSource source,NbaTXLife nTxLife) throws NbaBaseException {
   		String validReq = "";
   		NbaLob lob = source.getNbaLob();
   		NbaVpmsAdaptor proxy = null;
   		Relation rel = null;
   		if (nTxLife != null) { //ALII1674
   			rel = NbaUtils.getRelation(nTxLife.getOLifE(), NbaOliConstants.OLI_REL_JOINTINSURED);
   		} else {
   			rel = NbaUtils.getRelation(nbaTxLife.getOLifE(), NbaOliConstants.OLI_REL_JOINTINSURED);
   		}
   		if (rel != null) {
   			try {
   				NbaOinkDataAccess data = new NbaOinkDataAccess(lob);
   				Map deOinkMap = new HashMap();
   				proxy = new NbaVpmsAdaptor(data, NbaVpmsConstants.REQUIREMENTS);
   				proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_ORD_REQ_FOR_JINS);
   				deOinkMap.put(NbaVpmsConstants.A_REQ_CODE, String.valueOf(lob.getReqType()));
   				proxy.setSkipAttributesMap(deOinkMap);
   				VpmsComputeResult rulesProxyResult = proxy.getResults();
   				if (!rulesProxyResult.isError()) {
   					validReq = rulesProxyResult.getResult();
   				}
   			} catch (RemoteException t) {
   				throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
   			} finally {
   				if (proxy != null) {
   					try {
   						proxy.remove();
   					} catch (RemoteException re) {
   						LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
   					}
   				}
   			}
   			if (validReq.equals("1")) {
   				String partyId = null;
   				Party party = null;
   				if (nTxLife != null) {
   					partyId = nTxLife.getPartyId(NbaOliConstants.OLI_REL_JOINTINSURED, rel.getRelatedRefID());
   					party = nTxLife.getParty(partyId).getParty();
   				} else {
   					partyId = nbaTxLife.getPartyId(NbaOliConstants.OLI_REL_JOINTINSURED, rel.getRelatedRefID());
   					party = nbaTxLife.getParty(partyId).getParty();
   				}				
   				Person person = party.getPersonOrOrganization().getPerson();
   				if (lob.getFirstName().equalsIgnoreCase(person.getFirstName()) && lob.getLastName().equalsIgnoreCase(person.getLastName())) { //ALII1545
   					lob.setJointInsured(true);
   				}
   			}
   		}
   	}
}
