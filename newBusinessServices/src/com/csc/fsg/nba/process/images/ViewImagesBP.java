package com.csc.fsg.nba.process.images;

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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.result.RetrieveWorkResult;
import com.csc.fs.logging.LogHandler;
import com.csc.fs.accel.valueobject.RetrieveWorkItemsRequest;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fs.sa.SystemDefinition;
import com.csc.fs.sa.SystemDefinitionHandler;
import com.csc.fs.session.SystemSessionBase;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.foundation.NbaWebConstants;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.tableaccess.NbaUctData;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaViewImagesVO;
import com.csc.fsg.nba.vo.configuration.Host;

/**
 * Process request for View Images. Organize all applicable images in based on desired order
 * and medical indicator code. Construct the request XML for image view API to process 
 * opening of required images.  
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>NBA208-36</td><td>Version 7</td><td>Deferred Work Item retrieval</td></tr>
 * <tr><td>AXAL3.7.68</td><td>AXA Life Phase 1</td><td>LDAP Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class ViewImagesBP extends NewBusinessAccelBP {
    protected static boolean contentServicesUsed = false;	 
    protected static final String CONTENT_SERVICES = "ContentServices";	
    
	static {
        SystemDefinition sd = null;
        try {
            sd = SystemDefinitionHandler.load(getSystemName());
        } catch (NbaBaseException whoops) {            
            LogHandler.Factory.LogError("ViewImagesBP", "Unable to determine default AWD system name. Defaulting to SMC image processing.",  whoops);
        }
        if (sd != null) {
            Map connectionDetails = sd.getConnectionDetails();
            if (connectionDetails.containsKey(CONTENT_SERVICES) && connectionDetails.get(CONTENT_SERVICES) != null) {
                String value = ((String) connectionDetails.get(CONTENT_SERVICES)).toUpperCase();
                contentServicesUsed = value.startsWith(NbaConstants.YES_VALUE);
            } else {
                LogHandler.Factory.LogError("ViewImagesBP", "Unable to determine default AWD system name. Defaulting to SMC image processing.");
            }
        }
	}
	
    /**
     * Get the value of the default system from the configuration
     * @return the value
     * @throws NbaBaseException
     */    
    protected static String getSystemName() throws NbaBaseException {
        return NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.DEFAULT_SYSTEM);
    }
    /**
     * Return the value of contentServicesUsed set by the static initializer.
     * @return the contentServicesUsed.
     */ 
    protected static boolean isContentServicesUsed() {
        return contentServicesUsed;
    }
    
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            NbaViewImagesVO viewImages = (NbaViewImagesVO) input;
            //begin NBA208-36
            result = retrieveWorkItems(viewImages);
            if (result.hasErrors()){
            	return result;
            }
            //end NBA208-36
            String imagesXML = generateImageXML(viewImages);
            viewImages.setImageXML(imagesXML);
            result.addResult(viewImages);
        } catch (Exception e) {
            addExceptionMessage(result, e);
        }
        return result;
    }

    /**
     * Organize all applicable images in based on desired order and medical indicator code. 
     * Construct the request XML for image view API to process opening of required images. 
     * @param viewImages
     * @return
     * @throws NbaBaseException
     */
    protected String generateImageXML(NbaViewImagesVO viewImages) throws NbaBaseException {
        List sourceList = organizeAllSources(viewImages);
        if (sourceList == null || sourceList.size() == 0) {
        	return "";
        }
        return getAttributeForSourceViewer(sourceList, viewImages.getNbaUserVO());
    }

	/**
	 * Retrieve and organize the Sources for the Case and if requested, its Transactions.
	 * These Sources represent all images attached to the Case and associated 
	 * work items.  Sensitive provider results will only be included if requested and
	 * are determined by interrogating the medical indicator (LOB RQMD) on an NBPROVRSLT
	 * work item.  If RQMD = 1 (true) this is a medical requirement and if RQMD = 0 (false)
	 * then this is not a medical requirement.  Only the Sources in NBA_DisplaySource will
	 * be displayed and the order of display is also determined by NBA_DisplaySource. 
	 * @param nbaDst - the NbaDst for the Case or Transaction
	 * @param includeTransactions - boolean value indicating whether to include Sources from Transactions if nbaDst is a Case
	 * @param includeSensitiveSources - boolean value indicating whether to include sensitive sources
	 * @return a List containing Strings which identify the id's of the Source items 
	 */
	protected List organizeAllSources(NbaViewImagesVO viewImages) throws NbaBaseException {
		List sources = new ArrayList();
		WorkItem workItem = viewImages.getWorkItem(); 
		if (viewImages.getSourcesList() == null || viewImages.getSourcesList().isEmpty()) { //NBA208-36
			if (workItem.isCase()) {
				sources = workItem.getSourceChildren();
				if (viewImages.isIncludeTransactions()) {
					List transactions = workItem.getWorkItemChildren();
					int transCount = transactions.size();
					WorkItem currentTransaction;
					for (int i = 0; i < transCount; i++) {
						currentTransaction = (WorkItem) transactions.get(i);
						addSourcesFromTransaction(currentTransaction, currentTransaction.getSourceChildren(), sources, viewImages
								.isIncludeSensitiveImages());
					}
				}
			} else if (workItem.isTransaction()) {
				addSourcesFromTransaction(workItem, workItem.getSourceChildren(), sources, viewImages.isIncludeSensitiveImages());
			}
		//begin NBA208-36
		} else {
			sources = viewImages.getSourcesList();
		}
		//end NBA208-36
		return arrangeSources(workItem, sources);
	}

	/**
	 * Add Sources for Transactions. Sensitive provider results are only included if requested. These
	 * are determined by interrogating the medical indicator (LOB RQMD) on an NBPROVRSLT work item.
	 * If RQMD = 1 (true) this is a medical requirement and if RQMD = 0 (false) this is not a medical
	 * requirement. 
	 * @param nbaWorkItem - the Transaction work item
	 * @param transSources - the Sources for the Transaction
	 * @param sources - the Sources list to be added to.
	 * @param includeSensitiveSources - boolean value indicating whether to include sensitive sources
	 * @throws NbaBaseException
	 */
	protected void addSourcesFromTransaction(WorkItem workItem, List transSources, List sources, boolean includeSensitiveSources)
			throws NbaBaseException {
		int transSourceCount = transSources.size();
		NbaSource currentNbaSource;
		WorkItemSource currentSource;
		NbaLob nbaLob = new NbaLob(workItem.getLobData());
		for (int j = 0; j < transSourceCount; j++) {
			boolean sourceAlreadyPresent = false;//ALS2584
			currentSource = (WorkItemSource) transSources.get(j);
			currentNbaSource = new NbaSource(currentSource);
			sourceAlreadyPresent = isSourcePresent(currentSource, sources);//ALS2584
			if (!sourceAlreadyPresent) {//ALS2584
				if (includeSensitiveSources) {
					sources.add(currentSource);
				} else if (!(nbaLob.getReqMedicalType() && currentNbaSource.isProviderResult())) { //Skip medical provider results
					sources.add(currentSource);
				}
			}//ALS2584
		}
	}

	/**checks if the passed in source is present in sources list
	 * @param aSource
	 * @param sources
	 * @return
	 */
	////ALS2584 new method
	protected boolean isSourcePresent(WorkItemSource aSource, List sources) {
		Iterator sourceIterator = sources.iterator();
		WorkItemSource tempWorkitemSource = null;
		while (sourceIterator.hasNext()) {
			tempWorkitemSource = (WorkItemSource) sourceIterator.next();
			if (tempWorkitemSource.getItemID().equalsIgnoreCase(aSource.getItemID())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Arrange the Sources, including only the Sources identified in the NBA_DisplaySource table. The order of 
	 * the Sources is also determined by NBA_DisplaySource. Return a List containing the arranged sources.  
	 * @param nbaDst - the NbaDst for the Case or Transaction
	 * @param sources - the Sources eligible for inclusion
	 * @return a List containing Source items
	 * @throws NbaDataAccessException
	 */
	protected List arrangeSources(WorkItem workItem, List sources) throws NbaDataAccessException {
		List dispSource = new ArrayList();
		if (sources.size() > 0) { //Skip if there's nothing to arrange
			NbaTableAccessor tableAccessor = new NbaTableAccessor();
			NbaDst nbaDst = new NbaDst();
			nbaDst.setWorkItem(workItem); 
			NbaTableData[] displaySourceTable = (NbaUctData[]) tableAccessor.getDisplayData(nbaDst, NbaTableConstants.NBA_DISPLAY_SOURCE);
			//Initialize the display order list with null
			List displayOrder = new ArrayList();
			for (int i = 0; i < displaySourceTable.length; i++) {
				displayOrder.add(i, null);
			}
			// Update the display order list with the Source types based on display order		
			// BesValue identifies the display order
			// IndexValue identifies the Source type
			NbaUctData uctData;
			for (int i = 0; i < displaySourceTable.length; i++) {
				uctData = (NbaUctData) displaySourceTable[i];
				displayOrder.set(Integer.parseInt(uctData.getBesValue()) - 1, uctData.getIndexValue());
			}
			int sourceCtr = 0;
			int sourcesSize = sources.size();
			int dispSize = displayOrder.size();
			//Add Sources id's based on the index value of their source type.
			//Only Source types contained in displaySourceTable are included 
			WorkItemSource source;
			for (int dispIdx = dispSize - 1; dispIdx >= 0; dispIdx--) {
				for (int srcIdx = 0; srcIdx < sourcesSize; srcIdx++) {
					source = (WorkItemSource) sources.get(srcIdx);
					if (displayOrder.get(dispIdx).equals(source.getSourceType())) {
						dispSource.add(source);
						sourceCtr++;
					}
				}
			}
		}
		return dispSource;
	}
	
    /**
     * Method to create xml that will be passed to the Image viewer API for opening the images in image viewer.
     * @return String
     */
    protected String getAttributeForSourceViewer(List sourceList, NbaUserVO userVO) throws NbaBaseException {
        String defaultSystem = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.DEFAULT_SYSTEM);
        Result systemResult = getServiceContext().getUserSession().getSystem(defaultSystem);
        SystemSessionBase systemSessionBase = (SystemSessionBase) systemResult.getFirst();
        String userid = systemSessionBase.getUserId();
        if (userid == null) {
            userid = userVO.getUserID();
        }
        String password = systemSessionBase.getPassword();
        if (password == null) {
            password = userVO.getPassword();
        }
        
        StringBuffer sb = new StringBuffer();
        Host imgViewerNsHost = NbaConfiguration.getInstance().getNetserverHost(NbaWebConstants.IMAGE_VIEWER_NETSERVER_HOST);
		// Begin AXAL3.7.68
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><DST xml:lang=\"en-US\"><AWD>")
		.append("<userID>")
		.append(userid)
		.append("</userID>")
		.append("<securityLevel>000</securityLevel>")			
		.append(getSourceNodeForImageViewer(sourceList))
		.append("<aftHttpHeaders>")
		.append("<aftHttpHeader name=\"Cookie\">JSESSIONID=")
		.append(userVO.getToken(NbaUserVO.JSESSION_ID))
		.append("</aftHttpHeader>")
		.append("</aftHttpHeaders>")
		.append("<aftHttpUrl>")
		.append(userVO.getHostData().get(NbaUserVO.AFT_URL))
		.append("</aftHttpUrl>")
		.append("<httpHeaders>")
		.append("<httpHeader name=\"Cookie\">JSESSIONID=")
		.append(userVO.getToken(NbaUserVO.JSESSION_ID))
		.append("</httpHeader>")
		.append("</httpHeaders>")
		.append("<httpUrl>")
		.append(userVO.getHostData().get(NbaUserVO.HTTP_URL))
		.append("</httpUrl>");
		// End AXAL3.7.68
        sb.append("</AWD></DST>");
        String sourceXML = sb.toString();
        return sourceXML;
    }

    /**
     * Creates the XML containing the source node with the id and name of the source to be opened
     * @param action open or close
     * @return String
     */
    protected String getSourceNodeForImageViewer(List sourceList) {
        StringBuffer sourceBuffer = new StringBuffer();
        if (sourceList != null) {
            int sourceCount = sourceList.size();
            //NBA208-32
            WorkItemSource source;
            String sourceName; 
            for (int i = 0; i < sourceCount; i++) {
	            //NBA208-32
                source = (WorkItemSource) sourceList.get(i);
			    if (isContentServicesUsed()){
			        sourceName = source.getObjectID();
			    } else {
			        sourceName = source.getFile();
			    }
                sourceBuffer.append("<source action=\"");
                sourceBuffer.append("open");
                sourceBuffer.append("\" id=\"");
                //NBA208-32
                sourceBuffer.append(source.getItemID());
                sourceBuffer.append("\" order=\"0\" selected=\"Y\">");
                sourceBuffer.append("<name>");
                //NBA208-32
                sourceBuffer.append(sourceName);
                sourceBuffer.append("</name>");
                sourceBuffer.append("</source>");
            }
        }
        return sourceBuffer.toString();
    }
    /**
     * Retrieve the sources and children sources if requested
     * @param nbaDst
     * @param includeTransactions
     */
    protected AccelResult retrieveWorkItems(NbaViewImagesVO viewImages) {
    	WorkItem workItem = viewImages.getWorkItem();
    	//only process cases that need their children
		if ((workItem.isCase() && !viewImages.isIncludeTransactions())|| workItem.isTransaction()) { 
			return new AccelResult();
		}
		RetrieveWorkItemsRequest request = new RetrieveWorkItemsRequest();
		request.setUserID(getCurrentUserId());
		request.setWorkItemID(workItem.getItemID());	
		request.setSourcesIndicator(true);
		request.setTransactionAsChildIndicator(true);
		
		AccelResult hierarchyresult = (AccelResult) callBusinessService("RetrieveHierarchyBP", request);
		if (hierarchyresult.hasErrors()) {
			return hierarchyresult;
		}

		RetrieveWorkResult retrieveWorkResult = (RetrieveWorkResult) hierarchyresult;
		List transactions = new ArrayList();
		List items = retrieveWorkResult.getWorkItems();
		WorkItem tempWorkItem = null;
		WorkItem wi;
		for (int i = 0; i < items.size(); i++) {
			wi = (WorkItem) items.get(i);
			if (wi.getRecordType().equals(CASERECORDTYPE)) {
				tempWorkItem = wi;
			} else if (wi.getRecordType().equals(TRANSACTIONRECORDTYPE)) {
				transactions.add(wi);
			}
		}
		if (tempWorkItem != null) {
			tempWorkItem.getWorkItemChildren().addAll(transactions);
		} else if (!transactions.isEmpty()) {
			tempWorkItem = ((WorkItem) transactions.get(0));
		}
		viewImages.setWorkItem(tempWorkItem);
		return hierarchyresult;
	}
}
