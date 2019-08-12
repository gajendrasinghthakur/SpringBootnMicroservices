package com.csc.fsg.nba.business.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.axa.fsg.nba.fileGen.AxaFileGenerateProcessor;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.business.transaction.NbaRemovingXMLNodesUtils;
import com.csc.fsg.nba.business.transaction.NbaSFTPClientUtils;
import com.csc.fsg.nba.database.NbaContractDataBaseAccessor;
import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.database.NbaSystemDataDatabaseAccessor;
import com.csc.fsg.nba.exception.NbaAWDLockedException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaConfigurationException;
import com.csc.fsg.nba.exception.NbaContractAccessException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaCOILAndessaDataVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.AutomatedProcess;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;

public class NbaProcGenerateFileRequest extends NbaAutomatedProcess {

	private static final String NOT_FOUND = "Not Found";
	private String filepath = null;
	private String fileName = null;
	private NbaTXLife jointTXLife = null;
	private static final String CDATA = "<![CDATA[";
	private static final String CDATA_END = "]]>";
	private static final String POLICY_TO_ANDESA = "<PoliciesToAndessa>";
	private static final String POLICY_TO_ANDESA_END = "</PoliciesToAndessa>";
	private static final String NEXT_LINE = "\n";
	private static final String POLICY_DETAILS = "<PolicyDetails>";
	private static final String POLICY_DETAILS_END = "</PolicyDetails>";
	private static final String XML_VERSION = "<?xml version=" + "\"1.0\" " + "encoding= " + "\"UTF-8\"" + "?>";
	private static final String HOLDING_ID = "Holding_1";
	private static final String ATTACHMENT_ID = "Attachment_1";
	private static final String COIL_NBA = "COIL_nbA_ADS";
	private static final String XML = "xml";
	private static final String TIMESTAMP = "HH.mm.ss";
	private String andessaFileFormat = null;

	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException, NbaAWDLockedException {
		 boolean sftpFlag = false;
		// Process initialization
		if (!initialize(user, work)) {
			return getResult();
		}
		List<String> employerNameList = NbaSystemDataDatabaseAccessor.getEmployerListForAndessa();
		List<NbaCOILAndessaDataVO> policyNumberList = null;
		
		if (employerNameList.size() <= 0) {
			return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", "");
		}
		for (int i = 0; i < employerNameList.size(); i++) {
			String employerName = employerNameList.get(i);
			policyNumberList = NbaSystemDataDatabaseAccessor.getPolicyNumberListForAndessa(employerName);
			filepath = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConstants.UPLOADED_FILEPATH_FOR_GI);// For Local use this
			// filepath = "C://work//myWork//Legacy Decommission//COIL//";//filepath = "C://";
			andessaFileFormat = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.ANDESSA_FILE_FORMAT);
			if(andessaFileFormat.equalsIgnoreCase("REQ")){ //REQ stands to generate the XML request.
			Iterator iterator = policyNumberList.iterator();
			StringBuilder eibXML = new StringBuilder();
			eibXML.append(POLICY_TO_ANDESA);
			eibXML.append(NEXT_LINE);
			NbaRemovingXMLNodesUtils nodesUtils = new NbaRemovingXMLNodesUtils();
			NbaCOILAndessaDataVO andessaDataVO = new NbaCOILAndessaDataVO();
			NbaTXLife aTXLife = null;
			String content = null;
			while (iterator.hasNext()) {
				andessaDataVO = (NbaCOILAndessaDataVO) iterator.next();
				String policyNumber = andessaDataVO.getPolicynumber();
				String carrierCode = andessaDataVO.getCarrierCode();
				String backendKey = andessaDataVO.getBackendKey();
				try {
					aTXLife = retrieveTxlifeForWI(policyNumber, carrierCode, backendKey, getUser());
					// //Call utility to prepare xml for retrieved txLife List.
					content = nodesUtils.removeNodesFromTXLife(aTXLife.toXmlString());
					eibXML.append(NEXT_LINE);
					eibXML.append(POLICY_DETAILS);
					eibXML.append(NEXT_LINE);
					eibXML.append(content);
					eibXML.append(NEXT_LINE);
					eibXML.append(POLICY_DETAILS_END);
					eibXML.append(NEXT_LINE);
				} catch (Exception e) {
					e.printStackTrace();
				}
				// Add in nbaTxLifeList
				// nbaTxLifeList.add(aTXLife);
			}
			eibXML.append(POLICY_TO_ANDESA_END);
			jointTXLife = prepareXMLToSendEIB(eibXML.toString());
			fileName=	uploadXML(jointTXLife.toXmlString());
			}else{
				try{
					fileName=new AxaFileGenerateProcessor().generateFileForPolicies(policyNumberList,user, employerName,"");
				}catch(Exception e){
					
				}
			}
			sftpFlag=uploadFileOnAXAServer(fileName);
			System.out.println("sftpFlag::::"+sftpFlag);
			// Call EIB WebService,send xml ,
			// if get successful response, Move retrieved policies to END queue.
			if(null != policyNumberList && policyNumberList.size() > 0){
				for (NbaCOILAndessaDataVO policyNumber : policyNumberList) {
						NbaSystemDataDatabaseAccessor.updateProcessIndOfAndesaData(policyNumber.getPolicynumber(),sftpFlag);
				}
			}
		}
		// if file doesnt exists, then create it

		// Set Process_Ind to 1 for processed policies.
		List<NbaCOILAndessaDataVO> polNumberList = NbaSystemDataDatabaseAccessor.getPolicyNumberListForRoute();
		routeWorkItemAndUpdateAndesaData(polNumberList);
		if (getResult() == null) {
			return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", "");
		}
		return getResult();
	}

	//Begin NBLXA-1632
	protected String uploadXML(String eibXML) {
		fileName = filepath + getFileName();
		File file = new File(fileName);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			// Write in file
			bw.write(eibXML);
			bw.flush();
			bw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileName;
	}

	private NbaTXLife retrieveTxlifeForWI(String policyNumber, String companyKey, String backendSys, NbaUserVO user) throws Exception {
		NbaTXLife txLife203 = null;
		try {
			String[] args = new String[4];
			args[0] = "NO_ID";
			args[1] = policyNumber;
			args[2] = companyKey;
			args[3] = backendSys;

			StringBuffer txLifeStringBuffer = new StringBuffer("<TXLife xmlns=\"http://ACORD.org/Standards/Life/2\" >");
			txLifeStringBuffer.append("<UserAuthRequest><UserLoginName>" + user.getUserID()
					+ "</UserLoginName><UserPswd><CryptType>NONE</CryptType><Pswd>" + user.getPassword() + "</Pswd></UserPswd></UserAuthRequest>");
			txLifeStringBuffer.append("<TXLifeRequest><TransRefGUID>303721ZQ-9DFB-11D4-AF00-00D0B781A9F9</TransRefGUID></TXLifeRequest></TXLife>");

			NbaTXLife nbatxLife = new NbaTXLife(txLifeStringBuffer.toString());
			nbatxLife.setBusinessProcess(PROC_NBP);
			nbatxLife.setAccessIntent(READ);
			ArrayList pendResults = retrievePendEnquire(args);
			txLife203 = new NbaTXLife(nbatxLife);
			TXLife newTXLife = txLife203.getTXLife();
			UserAuthResponseAndTXLifeResponseAndTXLifeNotify userAuthResponse = newTXLife.getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
			TXLifeResponse txLifeResponse = userAuthResponse.getTXLifeResponseAt(0);
			txLifeResponse.setOLifE((OLifE) pendResults.get(0));

		} catch (SQLException sqle) {
			NbaLogFactory.getLogger(this.getClass()).logException(sqle);
			throw new NbaBaseException("An error occured while performing DB operation.", sqle);
		} catch (NbaBaseException nbe) {
			nbe.forceFatalExceptionType();
			NbaLogFactory.getLogger(this.getClass()).logException(nbe);
			throw new NbaBaseException("An error occured while setting Bean details.", nbe);
		}
		return txLife203;
	}

	private ArrayList retrievePendEnquire(String[] args) throws NbaContractAccessException {
		ArrayList results = NbaContractDataBaseAccessor.getInstance().selectOLifE(args);
		if (results == null || results.size() < 1) {
			throw new NbaContractAccessException("Unable to retrieve information from Contract Database for " + args[1]);
		}
		return results;
	}

	protected void routeWorkItemAndUpdateAndesaData(List policyNumberList) {
		Iterator newIterator = policyNumberList.iterator();
		boolean statusInitialize = false;
		while (newIterator.hasNext()) {
			NbaCOILAndessaDataVO andessaDataVO = new NbaCOILAndessaDataVO();
			andessaDataVO = (NbaCOILAndessaDataVO) newIterator.next();
			String policyNum = andessaDataVO.getPolicynumber();
			int processInd = andessaDataVO.getProcess_Ind();
			NbaDst parentWorkItem;
			try {
				if(processInd != 0){
				parentWorkItem = getParentWorkItem(policyNum, user);
				unsuspendParentCase(getUser(), parentWorkItem);
				setWork(parentWorkItem);
					if(!statusInitialize){
				initializeStatusFields();
						statusInitialize = true;
					}
					if(processInd == 1 ){
						setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
					}else if(processInd == 2){
						setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", getFailStatus()));
					}
					changeStatus(parentWorkItem, getResult().getStatus());
				doUpdateWorkItem();
				NbaContractLock.removeLock(getUser());
				unlockWork(parentWorkItem);
				// unsuspendParentCase(getUser(), parentWorkItem);
				NbaSystemDataDatabaseAccessor.updateRouteIndForAndessa(policyNum);
				}
			} catch (NbaBaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public NbaDst getParentWorkItem(String polNumber, NbaUserVO userVo) throws NbaBaseException {
		NbaSearchVO searchParentWI = new NbaSearchVO();
		searchParentWI.setContractNumber(polNumber);
		searchParentWI.setResultClassName(NbaSearchVO.DEFAULT_SEARCH_RESULT_CLASS);
		searchParentWI.setWorkType(NbaConstants.A_WT_APPLICATION);
		searchParentWI = WorkflowServiceHelper.lookupWork(userVo, searchParentWI);
		NbaDst aWorkItem = null;
		if (searchParentWI != null && searchParentWI.getSearchResults().size() > 0) {
			aWorkItem = retrieveWorkItem((NbaSearchResultVO) searchParentWI.getSearchResults().get(0));
		}
		return aWorkItem;
	}

	public NbaDst retrieveWorkItem(NbaSearchResultVO resultVO) throws NbaBaseException {
		NbaDst aWorkItem = null;
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setNbaUserVO(getUser());
		retOpt.setWorkItem(resultVO.getWorkItemID(), true);
		retOpt.setLockWorkItem();
		aWorkItem = retrieveWorkItem(getUser(), retOpt);
		return aWorkItem;
	}

	protected void unsuspendParentCase(NbaUserVO user, NbaDst work) throws NbaBaseException {
		if (work.isSuspended()) {
			work.setNbaUserVO(user);
			NbaSuspendVO suspendVO = new NbaSuspendVO();
			suspendVO.setCaseID(work.getID());
			suspendVO.setNbaUserVO(user);
			unsuspendWork(user, suspendVO);
		}
	}

	public boolean initialize(NbaUserVO nbaUserVO, NbaDst nbaDst) throws NbaBaseException {
		setUser(nbaUserVO);
		setWork(nbaDst);

		AutomatedProcess automatedProcess = null;
		String userID;
		if (nbaUserVO != null) {
			userID = nbaUserVO.getUserID();
			automatedProcess = NbaConfiguration.getInstance().getAutomatedProcessConfigEntry(userID/* "A2GNFLRQ" */);
			if (automatedProcess == null) {
				// Necessary configuration could not be found so raise exception
				setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
				writeToLogFile(userID + NOT_FOUND);
				throw new NbaConfigurationException(userID + NOT_FOUND);
			}
			return true;
		}
		return false;
	}

	protected void writeToLogFile(String entry) {
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug(entry);
		}
	}

	protected NbaTXLife prepareXMLToSendEIB(String xml) {
		xml.replace(XML_VERSION, "");
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQTRANS); // We will update these fields after discussion
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setNbaUser(getUser());
		NbaTXLife nbaReqTXLife = new NbaTXLife(nbaTXRequest);
		OLifE olifE = new OLifE();
		olifE.setVersion(NbaOliConstants.OLIFE_VERSION);
		Holding holding = new Holding();
		holding.setId(HOLDING_ID);
		holding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);
		Attachment attachment = new Attachment();
		attachment.setId(ATTACHMENT_ID);
		attachment.setDateCreated(new Date());
		attachment.setUserCode(getUser().getUserID());
		AttachmentData attachmentData = new AttachmentData();
		StringBuilder sb = new StringBuilder();
		sb.append(CDATA).append(XML_VERSION).append(NEXT_LINE).append(xml).append(CDATA_END);
		attachmentData.setPCDATA(sb.toString());
		attachment.setAttachmentData(attachmentData);
		attachment.setAttachmentType(NbaOliConstants.OLI_ATTACH_ANDESSA);
		holding.addAttachment(attachment);
		olifE.addHolding(holding);

		nbaReqTXLife.setOLifE(olifE);
		return nbaReqTXLife;
	}

	/**
	 * @purpose This method will return the fileName
	 * @param fieldName
	 * @param fileName
	 * @return
	 */
	private String getFileName() {
		return COIL_NBA + "_" + NbaUtils.getDateWithoutSeparator(new Date()) + "_" + getTimeStampFromStringInAWDFormat(new Date())
				+ "." + XML;
	}

	private String getTimeStampFromStringInAWDFormat(Date date) {
		String aDate = null;
		aDate = new java.text.SimpleDateFormat(TIMESTAMP).format(date);
		return aDate;
	}
	
	/**
	 * @purpose This method will Upload the file(XLS/XML) onto the AXA server
	 * @throws NbaBaseException
	 */
	private boolean uploadFileOnAXAServer(String fileNameWithPath) throws NbaBaseException{
		try {
			String destinationFile = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.SFTP_AXA_PATH);// For
			NbaSFTPClientUtils sftpClientUtils = new NbaSFTPClientUtils();
			sftpClientUtils.uploadFile(fileNameWithPath, destinationFile);
			if (!NbaUtils.isBlankOrNull(fileNameWithPath)) {
				File file = new File(fileNameWithPath);
				if (file.exists()) {
					file.delete();
				}
			}
			return true;
		} catch (NbaBaseException e) {	
			NbaLogFactory.getLogger(this.getClass()).logException(e);
			return false;
		}
	}
}