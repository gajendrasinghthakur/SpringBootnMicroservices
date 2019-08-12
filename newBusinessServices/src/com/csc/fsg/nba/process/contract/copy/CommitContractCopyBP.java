package com.csc.fsg.nba.process.contract.copy;

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

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fs.svcloc.ServiceLocator;
import com.csc.fsg.nba.bean.accessors.NbaCompanionCaseFacadeBean;
import com.csc.fsg.nba.business.process.AutoContractNumber;
import com.csc.fsg.nba.business.process.NbaAutoContractNumber;
import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.process.contract.planchange.CommitPlanChangeBP;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.tableaccess.NbaCompanionCaseControlData;
import com.csc.fsg.nba.transaction.validation.NbaTransactionValidationFacade;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaCase;
import com.csc.fsg.nba.vo.NbaCompanionCaseVO;
import com.csc.fsg.nba.vo.NbaContractCopyRequest;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaOverrideContractUpdateVO;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;

/**
 * Commits a copy of a contract to the workflow system.  Depending upon user selection,
 * the copied case can be opened immediately or allowed to process thru the automated
 * workflow.  If the base plan is changed, the contract will be modified to remove
 * contract information that is no longer applicable due to the plan change.  A comment
 * will also be added which audits this plan change.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA180</td><td>Version 7</td><td>Contract Copy Rewrite</td></tr>
 * <tr><td>NBA139</td><td>Version 7</td><td>Plan Code Determination</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPR3569</td><td>Version 8</td><td>Contract Copy Should Not Copy Financial Activity Objects to New Contract</td></tr>
 * <tr><td>AXAL3.7.34</td><td>AXA Life Phase 1</td><td>Contract Services</td></tr>
 * <tr><td>ALS3828</td><td>AXA Life Phase 1</td><td>QC # 2551  - Ad hoc: Copy feature</td></tr>
 * <tr><td>ALS4283</td><td>AXA Life Phase 1</td><td>QC # 3190 - Adhoc: Contract Copy functionality is not copying over the Images from the original application being copied</td></tr>
 * <tr><td>P2AXAL016</td><td>AXA Life Phase 2</td><td>Product Validation</td></tr>
 * <tr><td>SPR3614</td><td>AXA Life Phase 1</td><td>JVPMS Memory leak in Auto Contract Numbering logic</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 * @see CommitPlanChangeBP
 */
public class CommitContractCopyBP extends CommitPlanChangeBP {

    protected static final String ACTION_LOCK = "L";
    protected static final String POLICY_NO_LOB ="POLN"; //QC10954/APSL2852

    /* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            result = commit((NbaContractCopyRequest) input);
        } catch (Exception e) {
            result = new AccelResult();
            addExceptionMessage(result, e);
			NbaLogFactory.getLogger(this.getClass()).logError(e);
        }
        return result;
    }

	/**
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	protected AccelResult commit(NbaContractCopyRequest request) throws Exception {

		AccelResult accelResult = new AccelResult();
		request.getNbaTXLife().setBusinessProcess(NbaConstants.MENU_BUS_FUNC_CONTRACT_COPY);
		NbaTransactionValidationFacade.validateBusinessProcess(request.getNbaTXLife(), request.getNbaDst(), request.getNbaUserVO(), null);//P2AXAL016

		//copy the contract
		setContractCopy(request.isRequestForContractCopy());
		if (request.getSelectedPlan() != null) {
			setSelectedProduct(request.getNbaDst(), request.getSelectedPlan(), request.isOverriden()); //NBA139
		}
		NbaTXLife contract = getContract(request);
		removeExtraneousData(contract); //SPR3569
		if (request.getSelectedPlan() != null) {
			markupPlanChange(contract);
		}

		//create a new work item
		NbaDst copiedWork = createWorkItem(request);
		//begin AXAL3.7.34
		NbaTXLife xml103 = getXML103(contract, copiedWork, request); 		
		copiedWork.setNbaUserVO(request.getNbaUserVO());
		if (request.getContractNumber() == null) { //ALII53
			//setup EIB ContractNumber
			//begin SPR361
			//Begin QC10954/APSL2852
			String polNumber = null;
			if (!NbaUtils.isReg60PreSale(copiedWork.getNbaLob().getAppOriginType())) {
				polNumber = NbaAutoContractNumber.getInstance().generateEIBContractNumber(xml103, copiedWork, request.getNbaUserVO());
			} else { //End QC10954/APSL2852
				AutoContractNumber autoContractNumbering = (AutoContractNumber) ServiceLocator.lookup(AutoContractNumber.AUTO_CONTRACT_NUMBERING);
				List inputParam = new ArrayList(2);
				inputParam.add(new NbaOinkDataAccess(copiedWork));
				polNumber = autoContractNumbering.generateContractNumber(inputParam);
			}
			// End SPR361
			xml103.getPolicy().setPolNumber(polNumber);
			copiedWork.getNbaLob().setPolicyNumber(polNumber);		
			//ALS3828 begin
			List srcList = copiedWork.getNbaSources();
			for(int i=0;i<srcList.size();i++){
			  NbaSource source = (NbaSource) srcList.get(i);
			  NbaLob srcLob = source.getNbaLob();
			  if(srcLob!=null){
			      srcLob.setPolicyNumber(polNumber);
			  }   
			}
			//ALS3828 end
		}
		copiedWork.addXML103Source(xml103);
		//end AXAL3.7.34
		//update workflow system with new copied work
		Result res = callService("NbaUpdateWorkBP", copiedWork);
		if (res.hasErrors()) {
			accelResult.setErrors(res.hasErrors());
			accelResult.addMessages(res.getMessages());
		} else {
			copiedWork = (NbaDst) res.getFirst();

			// create a cross reference link for the cases, requires the work item ID from above update first
			createCrossReferenceLink(request, copiedWork);

			if (request.isOpenCase()) {
				request.setCopiedWork(copiedWork); //NBA208-36
			} else {
				// unlock the copied work item
				copiedWork.setNbaUserVO(request.getNbaUserVO());
				res = callService("NbaUnlockWorkBP", copiedWork);
				if (res.hasErrors()) {
					accelResult.setErrors(res.hasErrors());
					accelResult.addMessages(res.getMessages());
				}
			}

			// if we are unlocking the current work or modified the current work item, then update the workflow system
			//NBA208-32
			if (request.isOpenCase() || "Y".equals(request.getNbaDst().getCase().getUpdate())) {
				res = updateCurrentWork(request); 
				if (res.hasErrors()) {
					accelResult.setErrors(res.hasErrors());
					accelResult.addMessages(res.getMessages());
				}
			}
		}
		if (!res.hasErrors()) {
			accelResult.addResult(request);
		}
		return accelResult;
	}

	/**
	 * Creates a new work item, updates the lobs, creates the companion case
	 * cross reference and adds a manual comment for the copied case.
	 * @param request
	 * @return
	 * @throws Exception
	 */
	protected NbaDst createWorkItem(NbaContractCopyRequest request) throws Exception {
		NbaDst newDst = new NbaDst();
		//NBA208-32 code deleted
		newDst.setNbaUserVO(request.getNbaUserVO());
		newDst.addCase(createCase(request, newDst)); //ALS4283
		updateLobs(newDst, request);
		//addition of general comment if the plan code changed
		String productCode = request.getNbaTXLife().getProductCode();
		if (request.getSelectedPlan() != null && !request.getSelectedPlan().equalsIgnoreCase(productCode)) {
			newDst.addManualComment(getGeneralComment(request.getNbaTXLife().getProductCode(), request.getSelectedPlan(), request.getNbaUserVO()));
		}
		return newDst;
	}

	/**
     * Creates a new case work item for an application. Sets the work type and initial status as calculated from a rule. It updates most details from
     * the original case and attaches the application source if it is available.
     * 
     * @param request
     * @return
     */
    //NBA208-32
	//ALS4283 override method signature
    protected WorkItem createCase(NbaContractCopyRequest request,NbaDst newDst) throws NbaBaseException {
        WorkItem newCase = new WorkItem();
        newCase.setCreate("Y");
        if (request.isOpenCase()) {
            //NBA208-32
            newCase.setLock("Y");
            newCase.setAction(ACTION_LOCK);
        }

        // set worktype and initial status
        NbaProcessWorkItemProvider provider = getWorkItemProvider(request);
        newCase.setWorkType(provider.getWorkType());
        newCase.setStatus(provider.getInitialStatus());

        // update details from original case
        NbaCase srcCase = request.getNbaDst().getNbaCase();
        newCase.setBusinessArea(srcCase.getBusinessArea());
        //NBA208-32
        List lobs = srcCase.getCase().getLobData();
        List newlobs = newCase.getLobData();
        Iterator iter = lobs.iterator();
        while (iter.hasNext()) {
			LobData lobitem = (LobData) iter.next();
			if (!lobitem.getDataName().equals(POLICY_NO_LOB)) { //QC10954/APSL2852
				LobData newLob = new LobData();
				newLob.setDataName(lobitem.getDataName());
				newLob.setDataValue(lobitem.getDataValue());
				newLob.setSequenceNmbr(lobitem.getSequenceNmbr());
				newlobs.add(newLob);
			}
		}
		newCase.setRecordType(srcCase.getCase().getRecordType());
        //Retrieval of Application source attached with existing case
        //ALS3828 begin
        List sourceList = srcCase.getNbaSources();
        for (int i = 0; i < sourceList.size(); i++) {
            NbaSource source = (NbaSource) sourceList.get(i);
            if (source.isApplication() || source.isMiscMail() || source.isProviderResult()) { //NA_AXAL004 
                WorkItemSource workItemSource = source.getSource();
                //begin ALS4283 override base process and create a new WorkItemSource so we don't get duplicate sources
                WorkItemSource newWorkItemSource = new WorkItemSource();
            	newWorkItemSource.setCreate("Y");
            	newWorkItemSource.setRelate("Y");
            	newWorkItemSource.setLobData(workItemSource.getLobData());
            	newWorkItemSource.setBusinessArea(workItemSource.getBusinessArea());
            	newWorkItemSource.setSourceType(workItemSource.getSourceType());
            	newWorkItemSource.setSystemName(workItemSource.getSystemName());
                List list = WorkflowServiceHelper.getBase64SourceImage(request.getNbaDst().getNbaUserVO(), source);
                if (list != null && list.size() > 0) {
                	newWorkItemSource.setFormat(NbaConstants.A_SOURCE_IMAGE);
                	String guid = NbaUtils.getGUID(); //Get new GUID
                	workItemSource.setSize(0); //required as parameter, but value ignored by AWD
                	workItemSource.setPages(1); //required as parameter, but value ignored by AWD
                	newWorkItemSource.setText(guid);//(String) list.get(0));
                	newWorkItemSource.setFileName(null);
                	//ALS4283 already have a base64 encoded image returned from DST.  No need to encode again or do anything else
                	//NbaUpdateWorkBP will properly set image.
                    //Begin APSL497 Contract Copy process coping all the pages  in the source
					try {
						if (list.size() == 1) {
							newWorkItemSource.setSourceStream(list.get(0));
						} else {
							newWorkItemSource.setSourceStream(generateMultiPageTiff(list, guid));//if List size>1 , generate Multi Page Tiff image from List of images.
						}
					} catch (Exception exe) {
						throw new NbaBaseException("Error in Processing image files ", exe);
					}//End APSL497           
                }
                //end ALS4283
                newCase.getSourceChildren().add(newWorkItemSource);//ALS4283
            }
        }
        //ALS3828 end
        return newCase;
    }

    /**
     * Generate Multi Page Tiff image from List of images.
     * 
     * @param List imageStreamList
     * @param String newFileName
     * @return String
     *APSL497 New Method
     */
    private String generateMultiPageTiff(List imageStreamList, String newFileName) throws NbaBaseException {
		String encodeFinalImage = null;
		ImageWriter writer = null;
		ImageOutputStream ios = null;
		BufferedInputStream bis = null;
		try {
			//Create the temporary output file on the disk
			File tempFile = File.createTempFile(newFileName, ".tif");
			ios = ImageIO.createImageOutputStream(tempFile);
			//Get the appropriate Tiff Image Writer
			Iterator writers = ImageIO.getImageWritersByFormatName("tiff");
			if (writers != null && writers.hasNext()) {
				writer = (ImageWriter) ImageIO.getImageWritersByFormatName("tiff").next();
				writer.setOutput(ios);
				ImageWriteParam param = writer.getDefaultWriteParam();
				//Loop through all image files and write them to output tiff image
				for (int i = 0; i < imageStreamList.size(); i++) {
					InputStream fis = null;
					try {
						byte[] decodeImage = NbaBase64.decode((String) imageStreamList.get(i));
						fis = new BufferedInputStream(new ByteArrayInputStream(decodeImage));
						BufferedImage image = ImageIO.read(fis);
						IIOImage img = new IIOImage(image, null, null);
						if (i == 0) {
							writer.write(null, img, param);
						} else {
							writer.writeInsert(-1, img, param);
						}
						image.flush();
					} finally {
						if (fis != null) {
							fis.close();
						}
					}
				}
			}
			ios.flush();
			//decode the image into AWD acceptable format
			FileInputStream fis = new FileInputStream(tempFile.getAbsolutePath());	//APSL497
			byte finalImage[] = new byte[fis.available()];	//APSL497
			fis.read(finalImage);	//APSL497
			encodeFinalImage = NbaBase64.encodeBytes(finalImage);
		} catch (Exception ex) {
			throw new NbaBaseException("Error in Processing image files ", ex);
		} finally {
			if (null != writer)
				writer.dispose();
			if (null != ios)
				try {
					ios.close();
				} catch (IOException ioe) {
					throw new NbaBaseException("Error in closing ios", ioe);
				}
		}
		return encodeFinalImage;
	}
    //	NBA213 New Method
	protected NbaDst retrieveWorkItem(NbaUserVO nbaUserVO, NbaAwdRetrieveOptionsVO retOpt) throws NbaBaseException {
		retOpt.setNbaUserVO(nbaUserVO);
		AccelResult accelResult = (AccelResult) callBusinessService("NbaRetrieveWorkBP", retOpt);
		NewBusinessAccelBP.processResult(accelResult);
		return (NbaDst) accelResult.getFirst();
	}

	/**
	 * Returns a <code>NbaProcessWorkItemProvider</code> to set the copied case's new
	 * work type and initial status.
	 * @param request
	 * @return
	 * @throws NbaBaseException
	 */
	protected NbaProcessWorkItemProvider getWorkItemProvider(NbaContractCopyRequest request) throws NbaBaseException {
        Map deOink = new HashMap();
        deOink.put("A_OpenCase", request.isOpenCase() ? NbaConstants.TRUE_STR : NbaConstants.FALSE_STR);
        NbaUserVO copyUser = new NbaUserVO(NbaConstants.PROC_VIEW_CONTRACT_COPY, "");
        NbaProcessWorkItemProvider workItemProvider = new NbaProcessWorkItemProvider(copyUser, request.getNbaDst(), request.getNbaTXLife(), deOink);
        return workItemProvider;
	}

	/**
	 * Returns the copied <code>NbaLob</code> after they have been updated.  The lobs
	 * are returned for performance reasons so they can be passed to create a cross
	 * reference link.  
	 * @param newDst
	 * @param request
	 * @return
	 * @throws RemoteException
	 * @throws NbaBaseException
	 */
	protected void updateLobs(NbaDst newDst, NbaContractCopyRequest request) throws NbaBaseException {
		NbaLob lob = newDst.getNbaLob();
		lob.setCompanionType(request.getCompanionType());		
		if (request.getSelectedPlan() != null) {
			lob.setPlan(request.getSelectedPlan());
			lob.setProductTypSubtyp(String.valueOf(getSelectedProduct()));
		}
		//ALII53 code deleted
		lob.setPolicyNumber(request.getContractNumber());
		
		
		lob.deleteCwaTotal(); //SPR3569
		if (lob.getCwaSameSource()) {
			lob.setCwaSameSource(false);
		}
	}	
	
	/**
	 * This method creates XML103 to be attached with new case
	 * @param contract An instance of <code>NbaTxLife</code>
	 * @param newDst An instance of <code>NbaDst</code>
	 * @return NbaTxLife
	 * @throws NbaBaseException	 
	 */
	protected NbaTXLife getXML103(NbaTXLife contract, NbaDst newDst, NbaContractCopyRequest request){
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_NEWBUSSUBMISSION);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		//NBA208-32
		nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(newDst.getUserID()));
		nbaTXRequest.setNbaLob(newDst.getNbaLob());
		nbaTXRequest.setNbaUser(newDst.getNbaUserVO()); //NBA208-32

		new NbaOLifEId(new NbaTXLife(nbaTXRequest));

		//create txlife with default request fields
		NbaTXLife txLife103 = new NbaTXLife(nbaTXRequest);
		txLife103.setOLifE(contract.getOLifE());		

		Holding holding = txLife103.getPrimaryHolding();
		holding.setSystemMessage(null);
		Policy policy = holding.getPolicy();
		if (policy != null) {
			policy.setPolNumber(request.getContractNumber());
			if (policy.hasApplicationInfo()) {
				ApplicationInfo appInfo = policy.getApplicationInfo();
				appInfo.deleteCWAAmt(); //SPR3569
				ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
				if (appInfoExt != null){
					appInfoExt.setUnderwritingApproval(NbaOliConstants.OLI_TC_NULL);
				}
			}
		}
		setBaseCoverageType(txLife103);
		return txLife103;
	}

	/**
	 * This method sets the LifeCovTypeCode on the base coverage in new case XML103
	 * @param contract An instance of <code>NbaTxLife</code>
	 * @return NbaTxLife
	 */
	protected void setBaseCoverageType(NbaTXLife contract) {
		if (contract.isLife()) {
			Life life = contract.getLife();
			Coverage coverage = null;
			int count = life.getCoverageCount();
			for (int i = 0; i < count; i++) {
				coverage = life.getCoverageAt(i);
				if (coverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_BASE) {
					coverage.setLifeCovTypeCode(getSelectedCovType());
					return;
				}
			}
		}
	}

	/**
	 * This method creates a companion case cross reference link between the original
	 * and the copied case.  The original case will be updated with the companion type
	 * of the copied case if it doesn't have a companion type.  A link is not created
	 * if the companion case types of the original and copied cases are different. 
	 * @param request
	 * @param copiedCaseDst An instance of <code>NbaDst</code>
	 * @throws NbaBaseException
	 */
	protected void createCrossReferenceLink(NbaContractCopyRequest request, NbaDst copiedCaseDst) throws NbaBaseException {
		NbaDst origCase = request.getNbaDst();
		NbaLob origLobs = origCase.getNbaLob();
		String origCompanionType = origLobs.getCompanionType();

		//Do not create a relationship if types differ
		if (origCompanionType != null && !origCompanionType.equals(request.getCompanionType())) {
			return;
		} else if (origCompanionType == null) {
			origLobs.setCompanionType(request.getCompanionType());
			origCase.setUpdate();
		}

		String referenceId = null;
		NbaCompanionCaseFacadeBean ccFacade = new NbaCompanionCaseFacadeBean();
		List existingCompCaseVOs = ccFacade.retrieveCompanionCases(request.getNbaUserVO(), origCase);
		if (existingCompCaseVOs.size() > 0) {
			referenceId = ((NbaCompanionCaseVO) existingCompCaseVOs.get(0)).getCompanionReferenceID();
		}

		ArrayList companionCases = new ArrayList();
		NbaCompanionCaseVO companionCaseVO = null;

		if (referenceId == null) {
			companionCaseVO = new NbaCompanionCaseVO(origCase.getNbaCase());
			companionCaseVO.setActionAdd();
			companionCases.add(companionCaseVO);

			companionCaseVO = new NbaCompanionCaseVO(copiedCaseDst.getNbaCase());
			companionCaseVO.setActionAdd();
			companionCases.add(companionCaseVO);
		} else {
			companionCaseVO = new NbaCompanionCaseVO(copiedCaseDst.getNbaCase());
			companionCaseVO.setCompanionReferenceID(referenceId);
			companionCaseVO.setActionAdd();
			companionCases.add(companionCaseVO);
		}
		new NbaCompanionCaseControlData().insert(companionCases); //Insert the companion cases
	}

	/**
	 * Updates the current work item.  If the user wants to open the copied case,
	 * then the current case and contract lock will be unlocked.
	 * @param request
	 * @return
	 */
	protected Result updateCurrentWork(NbaContractCopyRequest request) {
		NbaOverrideContractUpdateVO updateVO = new NbaOverrideContractUpdateVO();  //NBA139
		updateVO.setNbaDst(request.getNbaDst());
		updateVO.setNbaUserVO(request.getNbaUserVO());
		updateVO.setNbaTXLife(request.getNbaTXLife());  //NBA139
		updateVO.setOverriden(request.isOverriden());  //NBA139
		//unlock current work item
		if (request.isOpenCase()) {
			updateVO.setUnlockWork(true);
		}
		return callService("DeterminePlanCommitContractBP", updateVO);  //NBA139
	}

	/**
	 * Removes the existing RequirementInfo and Financial Activity information from the contract. They will be determined later in the system for new
	 * Contract 
	 * @param contract
	 */
	//New Method SPR3569
	protected void removeExtraneousData(NbaTXLife contract) {
		Policy policy = contract.getPolicy();
		if (policy != null) {
			//Begin ALS5662
			List reqInfoList = policy.getRequirementInfo();
			for (int i = 0; i < reqInfoList.size(); i++) {
				RequirementInfo reqInfo = (RequirementInfo) reqInfoList.get(i);
				removeExtraneousRelations(contract, reqInfo.getId());
			}
			//End ALS5662
			policy.getRequirementInfo().clear();
			policy.getFinancialActivity().clear();
		}
	}
	//ALS5662 New Method
	protected void removeExtraneousRelations(NbaTXLife txLife, String objectId) {
		List relList = txLife.getOLifE().getRelation();
		Iterator itr = relList.iterator();
		while (itr.hasNext()) {
			Relation relation = (Relation) itr.next();
			if (relation.getOriginatingObjectID().equalsIgnoreCase(objectId) || relation.getRelatedObjectID().equalsIgnoreCase(objectId)) {
				txLife.getOLifE().getParty().remove(txLife.getParty(relation.getRelatedObjectID()).getParty());
				itr.remove();
			}
		}
	}
}


