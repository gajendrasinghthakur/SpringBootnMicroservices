package com.csc.fsg.nba.business.process;

/*
 * **************************************************************<BR>
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
 * **************************************************************<BR>
 */
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import com.csc.fs.NbaBase64;
import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.SystemAccess;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.FileAccess;
import com.csc.fs.accel.valueobject.FileItem;
import com.csc.fs.accel.valueobject.FileMessage;
import com.csc.fs.accel.valueobject.FolderAccessItem;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.svcloc.ServiceLocator;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaConfigurationException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaCase;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.AutomatedProcess;
import com.csc.fsg.nba.vo.configuration.DocumentDescription;
import com.csc.fsg.nba.vo.configuration.DocumentInputDefinition;
import com.csc.fsg.nba.vo.configuration.DocumentInputDefinitions;
import com.csc.fsg.nba.vo.configuration.DocumentSource;
import com.csc.fsg.nba.vo.txlife.Attachment;

/**
* NbaProcDocumentInput performs 
* <p>
* <b>Modifications:</b><br>
* <table border=0 cellspacing=5 cellpadding=5>
* <thead>
* <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
* </thead>
* <tr><td>NBA188</td><td>Version 7</td><td>nbA XML Sources to Auxiliary</td></tr>
* <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
* <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
* <tr><td>SR564247(APSL2525)</td><td>Discretionary</td><td>Predictive Full Implementation</td></tr>
* </table>
* <p>
* @author CSC FSG Developer
 * @version 7.0.0
* @since New Business Accelerator - Version 7
*/
public class NbaProcDocumentInput extends com.csc.fsg.nba.business.process.NbaAutomatedProcess {
    private static final String NOT_LOGGED_ON_ERROR = "SYS0111";
    protected static final String ACTION_LOCK = "L";
    protected static final String DELETE = "DELETE";
    protected static final String DOCUMENTINPUTDEFINITION_ACCESS_ERROR = "Unable to access DocumentInputDefinition configuration information for user: ";
    protected static final String FILEACCESS = "FileAccessBP";  //NBA213
    protected static final String FILENAMES = "FILENAMES";
    protected static final String NO_DOCUMENTINPUTDEFINITIONS = "No DocumentInputDefinitions defined for user";
    protected static final String NO_LOGGER = "NbaProcDocumentInput could not get a logger from the factory.";
    protected static final String READ_REQUEST = "READ";
    protected static final String RESULT = "result";
    protected static final String WT_CASE = "C";
    protected static final String WT_TRANSACTION = "T";

    protected DocumentDescription documentDescription;
    protected Map documentPaths;
    protected String fileContents;
    protected FileItem fileItem;
    protected FolderAccessItem folderAccessItem;

    /**
     * NbaProcAppSubmit default constructor.
     */
    public NbaProcDocumentInput() {
        super();
    }

    /**
     * Check the outcome of a Service Action. If any accelerator errors are present wrapper the errors in a fatal NbaBaseException.
     * If any FileMessage messages are present in the FileAccess response, log any informatial, warning, or error severity 
     * messages. Wrapper any fatal severity messages in a fatal NbaBaseException.
     * @param resultParams
     * @param outcome
     * @throws NbaBaseException
     */
    protected void checkOutcome(AccelResult accelResult) throws NbaBaseException {
    	if (accelResult.hasErrors()) {  //NBA213
    	    WorkflowServiceHelper.checkOutcome(accelResult);  //NBA213
    	} else {  //NBA213
            Object obj = accelResult.getFirst();
            if (obj != null) {
                FileAccess fileAccess = (FileAccess) obj;
                FileMessage fileMessage;
                int msgCnt = fileAccess.getMessages().size();
                for (int i = 0; i < msgCnt; i++) { //Look for FileMessage in the FileAccess response
                    fileMessage = (FileMessage) fileAccess.getMessages().get(i);
                    String severity = fileMessage.getSeverity();
                    if (FileMessage.SEVERITY_INFO.equals(severity)) {
                        getLogger().logInfo(fileMessage.getMsg());
                    } else if (FileMessage.SEVERITY_WARNING.equals(severity)) {
                        getLogger().logWarn(fileMessage.getMsg());
                    } else if (FileMessage.SEVERITY_ERROR.equals(severity)) {
                        getLogger().logError(fileMessage.getMsg());
                    } else if (FileMessage.SEVERITY_FATAL.equals(severity)) { //Throw a fatal exception
                        throw new NbaBaseException(fileMessage.getMsg(), NbaExceptionType.FATAL);
                    }
                }
            }
        }
    }

    /**
     * Create a Case work item from the information in the DocumentDescription.
     */
    protected void constructCase() {
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug(
                    "Creating Case: Business Area=" + getDocumentDescription().getBusinessArea() + ", Work Type="
                            + getDocumentDescription().getWorkType() + ", Status=" + getDocumentDescription().getStatus() + ", Source Type="
                            + getDocumentDescription().getSourceType());
        }
        //NBA208-32
        WorkItem awdCase = new WorkItem();
        //set Business Area, Work type and Status
        awdCase.setBusinessArea(getDocumentDescription().getBusinessArea());
        awdCase.setWorkType(getDocumentDescription().getWorkType());
        awdCase.setStatus(getDocumentDescription().getStatus());
        //NBA208-32
        awdCase.setLock("Y");
        awdCase.setAction(ACTION_LOCK);
        awdCase.setRecordType(WT_CASE);
        //NBA208-32
        awdCase.setCreate("Y");
        getWork().addCase(awdCase);
        getWork().setWork(new NbaCase(awdCase));
    }

    /**
     * Create a File Delete FileAccess request value object. A File Delete request is used to delete a file
     * identifed in the FileAccess.FolderAccessItem.FileItem.filename. 
     * for the specified set of directory names. 
     * @return FileAccess
     */
    protected FileAccess constructDeleteFileRequest() {
        FileAccess deleteFileRequest = new FileAccess();
        deleteFileRequest.setRequestCode(DELETE);
        FolderAccessItem deleteFolderAccessItem = new FolderAccessItem();
        deleteFolderAccessItem.setFoldername(getFolderAccessItem().getFoldername());
        deleteFolderAccessItem.getFileItems().add(getFileItem());	//Put the current file item in the list to be deleted 
        deleteFileRequest.getFolderAccessItems().add(deleteFolderAccessItem);
        return deleteFileRequest;
    }

    /**
     * Create a Document Names FileAccess request value object. A document names request is used to get a list of file names 
     * for the specified set of directory names.
     * @return a FileAccess which has been intitialized as a document names request
     */
    protected FileAccess constructDocumentNamesRequest() {
        FileAccess documentNamesRequest = new FileAccess();
        documentNamesRequest.setRequestCode(FILENAMES);
        FolderAccessItem documentNamesFolderAccessItem;
        Iterator it = getDocumentPaths().keySet().iterator(); //directory names are the keys to the MAP
        while (it.hasNext()) {
            documentNamesFolderAccessItem = new FolderAccessItem();
            documentNamesFolderAccessItem.setFoldername((String) it.next()); //Set the directory name
            documentNamesRequest.getFolderAccessItems().add(documentNamesFolderAccessItem);
        }
        return documentNamesRequest;
    }

    /**
     * Create a file read FileAccess request value object. A file read request is used to retrieve the contents of a file.
     * @return FileAccess
     */
    protected FileAccess constructReadFileRequest() {
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Reading file contents of " + getFileItem().getFilename());
        }
        FileAccess readFileRequest = new FileAccess();
        readFileRequest.setRequestCode(READ_REQUEST);
        FolderAccessItem readFolderAccessItem = new FolderAccessItem();
        readFolderAccessItem.setFoldername(getFolderAccessItem().getFoldername());
        readFolderAccessItem.getFileItems().add(getFileItem());
        readFileRequest.getFolderAccessItems().add(readFolderAccessItem);
        return readFileRequest;
    }

    /**
     * Create a Transaction work item from the information in the DocumentDescription. 
     */
    protected void constructTransaction() {
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug(
                    "Creating Transaction: Business Area=" + getDocumentDescription().getBusinessArea() + ", Work Type="
                            + getDocumentDescription().getWorkType() + ", Status=" + getDocumentDescription().getStatus() + ", Source Type="
                            + getDocumentDescription().getSourceType());
        }
        //NBA208-32
        WorkItem awdTransaction = new WorkItem();
        //set Business Area, Work type and Status
        awdTransaction.setBusinessArea(getDocumentDescription().getBusinessArea());
        awdTransaction.setWorkType(getDocumentDescription().getWorkType());
        awdTransaction.setStatus(getDocumentDescription().getStatus());
        //NBA208-32
        awdTransaction.setLock("Y");
        awdTransaction.setAction(ACTION_LOCK);
        awdTransaction.setRecordType(WT_TRANSACTION);
        //NBA208-32
        awdTransaction.setCreate("Y");
        try{
        	getWork().addTransaction(awdTransaction);
        }catch(Exception ex){
        }
        getWork().setWork(new NbaTransaction(awdTransaction));        
    }

    /**
     * Create the Work Item and attach the Source to it.
     * Create a dummy StatusProvider. The status is obtained from the DocumentDescription
     * @throws NbaBaseException
     */
    protected void constructWorkItem() throws NbaBaseException {
        //NBA208-32
        getWork().setUserID(getUser().getUserID());
        getWork().setPassword(getUser().getPassword());
        if (YES_VALUE.equalsIgnoreCase(getDocumentDescription().getCase())) {
            constructCase();
        } else {
            constructTransaction();
            if (getDocumentDescription().hasWorkSubType()) { // SR787006-APSL3702
            	getWork().getNbaLob().setWorkSubType(getDocumentDescription().getWorkSubType());//SR564247(APSL2525)
            }
        }
        getWork()
                .addNbaSource(new NbaSource(getDocumentDescription().getBusinessArea(), getDocumentDescription().getSourceType(), getFileContents()));
        setStatusProvider(new NbaProcessStatusProvider(getUser(), getWork()));	//Create a dummy StatusProvider. 
    }

    /**
     * Invoke the file access service action to delete a file.
     */
    protected void deleteFile() throws NbaBaseException {
        AccelResult workResult = (AccelResult) ServiceHandler.invoke(FILEACCESS, ServiceContext.currentContext(), constructDeleteFileRequest()); //NBA213
        checkOutcome(workResult);	//NBA213
    }

    /**
     * Perform document input processing:
     * - initialize by constructing a Map containing the document documentPaths and the configuration 
     *   information for the <DocumentSource>s applicable to the paths. 
     * - get the file name and contents of the next eligible file to process in the paths.
     * - if there are no eligible files:
     *      return an NbaAutomatedProcessResult with a return code of "2" (no work)
     * - if an eligible file is found:
     * -    create linked work and source items and commit them
     * -    delete the file
     * -    return an NbaAutomatedProcessResult with a return code of "0" (successful)
     * @param user the NbaUser for whom the process is being executed
     * @param work - null
     * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
     * @throws NbaBaseException
     */
    public NbaAutomatedProcessResult executeProcess(NbaUserVO nbaUserVO, NbaDst nbaDst) throws NbaBaseException {
        try {
            long startTime = System.currentTimeMillis(); 
            initialize(nbaUserVO, nbaDst);
            findDocumentToProcess();
            if (getResult() == null) { //Null indicates that a work item should be created
                constructWorkItem();
                if (getResult() == null) {
                    try {
                        long startCommit = System.currentTimeMillis();
                        updateWork(); //Commit the new work item //NBA208-32
                        logCommitTime(startCommit);
                        deleteFile(); //Delete the file
                    } catch (NbaBaseException whoops) {
                        if (isNotLoggedOn(whoops)) { 
                            //assume that the user has been automatically logged of workflow system because of inactivity. 
                            logon();	//NBA213
                            nbaUserVO.setSessionKey(getUser().getSessionKey());
                            setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.RETRY, "", "")); //and retry
                        } else {
                            whoops.forceFatalExceptionType(); //Any errors in commit are fatal
                            throw whoops;
                        }
                    }
                    if (getResult() == null) {
                        setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
                    }
                }
            }
            logElapsed(startTime);
           
            return getResult();
        } catch (NbaBaseException whoops) {
            whoops.forceFatalExceptionType();
            throw whoops;
        } catch (Throwable t) {
            throw new NbaBaseException("An unhandled error has occured: " + t.toString(), NbaExceptionType.FATAL);	//NBA213
        }
    }

    /**
     * Determine if the error is a not logged on condition
     * @param whoops
     * @return
     */
    private boolean isNotLoggedOn(NbaBaseException whoops) {
        return whoops.getMessage() != null && whoops.getMessage().indexOf(NOT_LOGGED_ON_ERROR) > -1;
    }

    /**
     * Find an eligible input document to process.
     * - get lists of the file names found within the folders associated with the current business process.
     * - iterate over the the list of file names for each folder
     * - for each file attempt to locate a <DocumentDescription> configuration entry which matches the file name suffix.
     * - if a match is found, attempt to read the file contents. If the file is not locked and has data in it, 
     *   it is elgible for processing. Return.
     * - otherwise, continue looping 
     * If there are no eligible files create an NbaAutomatedProcessResult with a return code of "2" (no work).
     * @param resultParams
     * @throws NbaBaseException
     */
    protected void findDocumentToProcess() throws Exception {//NBLXA-2059
        //NBA213 deleted
        boolean documentFound = false;
        AccelResult accelResult = retrieveFileNames(); //Get the file names  NBA213
        if (accelResult != null) {
            Object obj = accelResult.getFirst();
            if (obj != null) {
                FileAccess fileAccess = (FileAccess) obj;
                int folderAccessCnt = fileAccess.getFolderAccessItems().size();
                int fileItemCnt;
                main: for (int i = 0; i < folderAccessCnt; i++) { //Iterate over the folders
                    setFolderAccessItem((FolderAccessItem) fileAccess.getFolderAccessItems().get(i));
                    fileItemCnt = getFolderAccessItem().getFileItems().size();
                    for (int j = 0; j < fileItemCnt; j++) { //Iterate over each file name within the folder
                        setFileItem((FileItem) getFolderAccessItem().getFileItems().get(j));
                        if (matchsCachedDocumentDescription()) {
                            //A document description was found so it's eligible for processing
                            getFileContentsFromDisk(); //Read the file contents
                            if (getFileContents() != null) { //eligible for processing
                                documentFound = true;
                                break main;
                            }
                        }
                    }
                }
            }
        }
        if (!documentFound) {
            setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", ""));
        }
    }

    /**
     * Return the <DocumentDescription> configuration entry to be used.
     * @return the documentDescription.
     */
    protected DocumentDescription getDocumentDescription() {
        return documentDescription;
    }

    /**
     * Return the Map which contains the document documentPaths and the DocumentSources applicable to the documentPaths
     * @return Map
     */
    protected Map getDocumentPaths() {
        return documentPaths;
    }

    /**
     * Return the contents of a file
     * @return the contents.
     */
    protected String getFileContents() {
        return fileContents;
    }

    /**
     * Read the file contents from disk for the file name identified in the fileItem.
     * @return String
     * @throws NbaBaseException
     */
	protected void getFileContentsFromDisk() throws NbaBaseException, Exception {//NBLXA-2059
        AccelResult accelResult = (AccelResult) ServiceHandler.invoke(FILEACCESS, ServiceContext.currentContext(), constructReadFileRequest());  //NBA213
        checkOutcome(accelResult);  //NBA213
        if (accelResult != null) {
            Object obj = accelResult.getFirst();
            if (obj != null) {
                FileAccess fileAccess = (FileAccess) obj;
                int folderAccessCnt = fileAccess.getFolderAccessItems().size();
                int fileItemCnt;
                FolderAccessItem readFolderAccessItem;
                FileItem readFileItem;
                //Loop throug the results for find the file contents
                main: for (int i = 0; i < folderAccessCnt; i++) {
                    readFolderAccessItem = (FolderAccessItem) fileAccess.getFolderAccessItems().get(i);
                    fileItemCnt = readFolderAccessItem.getFileItems().size();
                    for (int j = 0; j < fileItemCnt; j++) {
                        readFileItem = (FileItem) readFolderAccessItem.getFileItems().get(j);
                        if (readFileItem.getFileContents() != null) {
							generateTiffByteArray(readFileItem); //NBLXA-2059
						} //NBLXA-2059
                            setFileContents(readFileItem.getFileContents());
                            if (getLogger().isDebugEnabled() && getFileContents() != null) {
                                getLogger().logDebug(getFileContents().length() + " bytes read from " + readFileItem.getFilename());
                            }
                            break main;
                        }
                    }
                }
            }
        }
	//NBLXA-2059 Starts
	private void generateTiffByteArray(FileItem readFileItem) throws Exception, InvalidPasswordException, IOException {
		nbaTxLife = new NbaTXLife(readFileItem.getFileContents());
		printJavaMemory("In the starting of method");
		if (!NbaUtils.isBlankOrNull(nbaTxLife.getUserAuthRequest())
				&& !NbaUtils.isBlankOrNull(nbaTxLife.getUserAuthRequest().getVendorApp().getVendorName())
				&& NbaConstants.PROVIDER_PRODUCER.equalsIgnoreCase(nbaTxLife.getUserAuthRequest().getVendorApp().getVendorName().getPCDATA())) {
			if (!NbaUtils.isBlankOrNull(nbaTxLife.getPolicy())) {
				if (nbaTxLife.getPolicy().getRequirementInfoCount() > 0 && !NbaUtils.isBlankOrNull(nbaTxLife.getPolicy().getRequirementInfoAt(0))) {
					if (nbaTxLife.getPolicy().getRequirementInfoAt(0).getAttachmentCount() > 0
							&& !NbaUtils.isBlankOrNull(nbaTxLife.getPolicy().getRequirementInfoAt(0).getAttachmentAt(0))) {
						if (!NbaUtils.isBlankOrNull(nbaTxLife.getPolicy().getRequirementInfoAt(0).getAttachmentAt(0).getMimeTypeTC())
								&& NbaOliConstants.OLI_LU_MIMETYPE_PDF == nbaTxLife.getPolicy().getRequirementInfoAt(0).getAttachmentAt(0)
										.getMimeTypeTC()) {
							List images = new ArrayList();
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							Attachment attach = nbaTxLife.getPolicy().getRequirementInfoAt(0).getAttachmentAt(0);
							byte[] imageByteArray = NbaBase64.decode(attach.getAttachmentData().getPCDATA());
							PDDocument document = null;
							BufferedImage image = null;
							document = PDDocument.load(imageByteArray);
							if (ImageIO.getImageWritersByFormatName("TIFF") != null) {
								ImageIO.scanForPlugins();
							}
							ImageOutputStream outputStream = ImageIO.createImageOutputStream(baos);
							ImageWriter tiffWriter = ImageIO.getImageWritersByFormatName("TIFF").next(); // Assumes TIFF plugin installed
							if (tiffWriter != null) {
								tiffWriter.setOutput(outputStream);
								tiffWriter.prepareWriteSequence(null);
								ImageWriteParam jpgWriteParam = tiffWriter.getDefaultWriteParam();
								jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
								jpgWriteParam.setCompressionType("CCITT T.6");
								jpgWriteParam.setCompressionQuality(1f);
								if (document != null) {
									PDFRenderer renderer = new PDFRenderer(document);
									imageByteArray = null;
									for (int page = 0; page < document.getNumberOfPages(); page++) {

										// image = renderer.renderImage(page);
										image = renderer.renderImageWithDPI(page, 200, ImageType.BINARY);
										tiffWriter.writeToSequence(new IIOImage(image, null, null), jpgWriteParam);
										image.flush();
									}
								}
							}
							tiffWriter.endWriteSequence();
							document.close();
							tiffWriter.dispose();
							outputStream.flush();
							outputStream.close();
							String encodeFinalImage = NbaBase64.encodeBytes(baos.toByteArray());
							baos.flush();
							baos.close();
							attach.getAttachmentData().setPCDATA(encodeFinalImage);
							nbaTxLife.getPolicy().getRequirementInfoAt(0).setAttachmentAt(attach, 0);

							readFileItem.setFileContents(nbaTxLife.toXmlString());
						}
					}
				}
			}
		}
	}


	protected void printJavaMemory(String str) {
		System.out.println(str + " : ");
		System.out.println(Runtime.getRuntime().freeMemory() / 1024);
	}
	// NBLXA-2059 Ends
    /**
     * Retrieve the FileItem being processed
     * @return the fileItem.
     */
    protected FileItem getFileItem() {
        return fileItem;
    }

    /**
     * Retrieve the FolderAccessItem being processed
     * @return the folderAccessItem.
     */
    protected FolderAccessItem getFolderAccessItem() {
        return folderAccessItem;
    }

    /**
     * Return my <code>NbaLogger</code> implementation.
     * @return com.csc.fsg.nba.foundation.NbaLogger
     */
    protected NbaLogger getLogger() {
        if (logger == null) {
            try {
                logger = NbaLogFactory.getLogger(NbaProcDocumentInput.class.getName());
            } catch (Exception e) {
                NbaBootLogger.log(NO_LOGGER);
                e.printStackTrace(System.out);
            }
        }
        return logger;
    }

    /**
     * Construct a Map containing the document documentPaths and the configuration information for the <DocumentSource>s applicable to the path. The
     * configuration entries for <DocumentInputDefinition>s are examined. For those whose business function matches the businesss function of the
     * user, create a Map entry with the full path of the document folder as the key and the configuration information for <DocumentSource>s as the
     * value.
     * @throws NbaBaseException
     * @throws NbaBaseException
     */
    public boolean initialize(NbaUserVO nbaUserVO, NbaDst nbaDst) throws NbaBaseException {
        setUser(nbaUserVO);
        setWork(nbaDst);
        //NBA213 deleted
        setDocumentPaths(new HashMap());
        //Get the <AutomatedProcess> entry
        AutomatedProcess automatedProcess = null;
        String userID;
        if (nbaUserVO != null) {
            userID = nbaUserVO.getUserID();
            automatedProcess = NbaConfiguration.getInstance().getAutomatedProcessConfigEntry(userID);
            if (automatedProcess == null) {
                // Necessary configuration could not be found so raise exception
                throw new NbaConfigurationException(DOCUMENTINPUTDEFINITION_ACCESS_ERROR + userID);
            }
        }
        //Get the <DocumentInputDefinitions>
        DocumentInputDefinitions documentInputDefinitions = NbaConfiguration.getInstance().getDocumentInputDefinitions();
        DocumentInputDefinition documentInputDefinition;
        DocumentSource documentSource;
        int cnt = documentInputDefinitions.getDocumentInputDefinitionCount();
        for (int i = 0; i < cnt; i++) {
            documentInputDefinition = documentInputDefinitions.getDocumentInputDefinitionAt(i);
            //For each documentInputDefinition whose business process matches that of the user, add the path to the map.
            if (documentInputDefinition.getBusfunc().equals(automatedProcess.getBusfunc())) {
                int cntPaths = documentInputDefinition.getDocumentSourceCount();
                for (int j = 0; j < cntPaths; j++) {
                    documentSource = documentInputDefinition.getDocumentSourceAt(j);
                    getDocumentPaths().put(documentInputDefinition.getDocumentRootPath() + documentSource.getPath(), documentSource);
                }
            }
        }
        if (getDocumentPaths().keySet().size() < 1) {
            throw new NbaBaseException(NO_DOCUMENTINPUTDEFINITIONS, NbaExceptionType.FATAL);
        }
        return true;
    }
    //NBA213 deleted    
    /**
     * Concatenate a List of messages into a String with new line delimiters between the messages.
     * @param inMessages java.util.List
     * @return java.lang.String
     */
    protected String listToString(java.util.List inMessages) {
        if (inMessages != null && inMessages.size() > 0) {
            StringBuffer msg = new StringBuffer();
            for (int i = 0; i < inMessages.size(); i++) {
                msg.append("\n");
                msg.append(inMessages.get(i).toString());
            }
            return msg.toString();
        }
        return "";
    }

    /**
     * Log the elapsed time for committing a work item.
     * @param startTimeWorkFlow
     */
    protected void logCommitTime(long startTimeWorkFlow) {
        if (getLogger().isInfoEnabled()) {
            StringBuffer buf = new StringBuffer();
            buf.append("Elapsed time to commit new  was ").append(System.currentTimeMillis() - startTimeWorkFlow).append(
                    " milliseconds");
            getLogger().logInfo(buf.toString());
        }
    }

    /**
     * Log the elapsed time between the current time and the start time for the process id.
     * @param id - the process id
     * @param startTime
     */
    protected void logElapsed(long startTime) {
        if (getLogger().isInfoEnabled()) {
            StringBuffer buf = new StringBuffer();
            buf.append("Elapsed time for a");
            switch (getResult().getReturnCode()) {
            case NbaAutomatedProcessResult.SUCCESSFUL:
                buf.append(" successful ");
                break;
            case NbaAutomatedProcessResult.FAILED:
                buf.append(" failed ");
                break;
            case NbaAutomatedProcessResult.NOWORK:
                buf.append(" no documents found ");
                break;
            default:
                buf.append("n unknown ");
                break;
            }
            buf.append("NbaProcDocumentInput was ").append(System.currentTimeMillis() - startTime).append(" milliseconds");
            getLogger().logInfo(buf.toString());
        }
    }

    /**
     * Locate the cached DocumentDescription configuration entry for the file "type". The entry is 
     * located by locating the cached DocumentSource entry matching the path (Foldername) of the file 
     * and then matching the file name suffix with the "type" value in the collection of
     * DocumentDescription configuration entries within the DocumentSource.
     * @return true if located. Otherwise return false.
     */
    protected boolean matchsCachedDocumentDescription() {
        //Find the DocumentSource by matching the path
        DocumentSource documentSource = (DocumentSource) getDocumentPaths().get(getFolderAccessItem().getFoldername());
        int descriptionCnt = documentSource.getDocumentDescriptionCount();
        String fileName = getFileItem().getFilename();
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Searching document descriptions to determine if " + fileName + " should be processed");
        }
        //Examine the DocumentDescription entries to find a match on "type"
        for (int i = 0; i < descriptionCnt; i++) {
            setDocumentDescription(documentSource.getDocumentDescriptionAt(i));
            if (fileName.toUpperCase().endsWith(getDocumentDescription().getType().toUpperCase())) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().logDebug("A matching document description was found for " + fileName);
                }
                return true;
            }
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("No matching document description was found for " + fileName);
        }
        return false;
    }

    /**
     * Invoke the file access service action to retrieve lists of file names for a set of folders.
     * @return
     * @throws NbaBaseException
     */
    //NBA213 changed return type
    protected AccelResult retrieveFileNames() throws NbaBaseException {
        //begin NBA213
        AccelResult accelResult = new AccelResult();
        accelResult.merge(ServiceHandler.invoke(FILEACCESS, ServiceContext.currentContext(), constructDocumentNamesRequest())); 
        checkOutcome(accelResult);          
        return accelResult;
        //end NBA213
    }

    /**
     * Set the current DocumentDescription.
     * 
     * @param documentDescription
     *            The documentDescription to set.
     */
    protected void setDocumentDescription(DocumentDescription documentDescription) {
        this.documentDescription = documentDescription;
    }

    /**
     * Save the Map which contains the document documentPaths and the DocumentSources applicable to the documentPaths
     * @param documentPaths The documentPaths to set.
     */
    protected void setDocumentPaths(Map paths) {
        this.documentPaths = paths;
    }

    /**
     * Store the file contents.
     * @param contents The contents to set.
     */
    protected void setFileContents(String fileContents) {
        this.fileContents = fileContents;
    }

    /**
     * Set the FileItem being processed.
     * @param fileItem The fileItem to set.
     */
    protected void setFileItem(FileItem fileItem) {
        this.fileItem = fileItem;
    }

    /**
     * Set the FolderAccessItem being processed.
     * @param folderAccessItem The folderAccessItem to set.
     */
    protected void setFolderAccessItem(FolderAccessItem folderAccessItem) {
        this.folderAccessItem = folderAccessItem;
    }
    
    /**
     * Logon the user. First logoff the user to clear any session data, then logon.
     */
    //NBA213 New Method
    protected void logon() {
        SystemAccess sysAccess = (SystemAccess) ServiceLocator.lookup(SystemAccess.SERVICENAME);
        String system = null;
        try {
            system = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.DEFAULT_SYSTEM);
        } catch (NbaBaseException exp) {
            getLogger().logException("Exception occured while reading AWD external system from configuration : ", exp);
            system = NbaConstants.AWD_EXTERNAL_SYSTEM;
        }
        sysAccess.logout(system);
        sysAccess.login(system);
    }
 
}
