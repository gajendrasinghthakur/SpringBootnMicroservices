package com.csc.fsg.nba.business.process;

/*
 * **************************************************************<BR>
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
 * **************************************************************<BR>
 */
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.csc.fsg.nba.business.transaction.NbaAxaServiceResponse;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.webservice.client.NbaAxaServiceRequestor;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapter;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapterFactory;

/**
* This job reads the reporting output directory and sends files to AXA via SFTP process
* <p>
* <b>Modifications:</b><br>
* <table border=0 cellspacing=5 cellpadding=5>
* <thead>
* <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
* </thead>
* <tr><td>ALS3400</td><td>AXA Life Phase 1</td><td>Cashiering Reports Firewall Issue</td></tr>
* <tr><td>ALS4376</td><td>AXA Life Phase 1</td><td>QC # 3301  - Not receiving correct Deposit Report</td></tr>
* <tr><td>ALS4597</td><td>AXA Life Phase 1</td><td>QC # 3676 - nbA Deposit Ticket Report did not automatically generate </td></tr>
* </table>
* <p>
* @author CSC FSG Developer
 * @version 7.0.0
* @since New Business Accelerator - Version 7
*/
public class NbaProcReportProcess extends NbaAutomatedProcess {
  
	private String reportDirectory;
	private String backupDirectory;
	private String ftpUser = null;
	private String ftpPassword;
	private String url;
	private String reportType;
	private String ftpDirectory;
	private int successCount = 0;
	private int failCount = 0;
	private String sftpClientLocation;
    private static final String SFTP_CLIENT_LOCATION = "stclient.exe";
	protected static final String NO_LOGGER = "NbaProcReportProcess could not get a logger from the factory.";
	private static final String ERROR_MESSAGE = "Unable to FTP file: ";
	private String sftpCommands = null;
	private FileFilter ff = null; //ALS4376
	private static final int BATCH_SIZE = 5; //APSL753
	private static int REPORT_COUNTER = 0; //APSL753
    /**
     * NbaProcReportProcess default constructor.
     */
    public NbaProcReportProcess() {
        super();
    }
      
	/**
	 * @return Returns the reportType.
	 */
	public String getReportType() {
		return reportType;
	}
	/**
	 * @param reportType The reportType to set.
	 */
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}
	/**
	 * @return Returns the sftpClientLocation.
	 */
	public String getSftpClientLocation() {
		return sftpClientLocation;
	}
	/**
	 * @param sftpClientLocation The sftpClientLocation to set.
	 */
	public void setSftpClientLocation(String sftpClientLocation) {
		this.sftpClientLocation = sftpClientLocation;
	}
	/**
	 * @return Returns the reportDirectory.
	 */
	public String getReportDirectory() {
		return reportDirectory;
	}
	/**
	 * @param reportDirectory The reportDirectory to set.
	 */
	public void setReportDirectory(String reportDirectory) {
		this.reportDirectory = reportDirectory;
	}
	/**
	 * @return Returns the backupDirectory.
	 */
	public String getBackupDirectory() {
		return backupDirectory;
	}
	/**
	 * @param backupDirectory The backupDirectory to set.
	 */
	public void setBackupDirectory(String backupDirectory) {
		this.backupDirectory = backupDirectory;
	}
	/**
	 * @return Returns the ftpDirectory.
	 */
	public String getFtpDirectory() {
		return ftpDirectory;
	}
	/**
	 * @param ftpDirectory The ftpDirectory to set.
	 */
	public void setFtpDirectory(String ftpDirectory) {
		this.ftpDirectory = ftpDirectory;
	}
	/**
	 * @return Returns the ftpPassword.
	 */
	public String getFtpPassword() {
		return ftpPassword;
	}
	/**
	 * @param ftpPassword The ftpPassword to set.
	 */
	public void setFtpPassword(String ftpPassword) {
		this.ftpPassword = ftpPassword;
	}
	/**
	 * @return Returns the ftpUser.
	 */
	public String getFtpUser() {
		return ftpUser;
	}
	/**
	 * @param ftpUser The ftpUser to set.
	 */
	public void setFtpUser(String ftpUser) {
		this.ftpUser = ftpUser;
	}
	/**
	 * @return Returns the url.
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url The url to set.
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * @return Returns the sftpCommands.
	 */
	public String getSftpCommands() {
		return sftpCommands;
	}
	/**
	 * @param sftpCommands The sftpCommands to set.
	 */
	public void setSftpCommands(String sftpCommands) {
		this.sftpCommands = sftpCommands;
	}
    /**
     * SFTP file to configured server
     * Create a dummy StatusProvider. The status is obtained from the DocumentDescription
     * @throws NbaBaseException
     */
	//ALS4376 new method
	protected void sftpFiles(Runtime rt, String directory) throws NbaBaseException {
		File[] listOfFiles = new File(directory).listFiles(ff);
        
        String fileToSend = null;
        String DATE_FORMAT = "yyyyMMdd";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Calendar c1 = Calendar.getInstance(); // today
        StringBuffer command  = new StringBuffer();
        try {
			for (int i = 0; i < listOfFiles.length && REPORT_COUNTER < BATCH_SIZE; i++) { //APSL753
			if (listOfFiles[i].isDirectory()) {
				sftpFiles(rt,listOfFiles[i].getAbsolutePath());
			} 
			if (listOfFiles[i].isFile()) {
				getSFTPCredentials();
				copyFile(listOfFiles[i], sdf.format(c1.getTime()));
				fileToSend = listOfFiles[i].getPath();
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Sending report ["+ fileToSend + "] via FTP" );
				}
				command.setLength(0); //reset buffer;
				command.append("\"" + getSftpClientLocation()  + "\"");
				command.append(" httpsu://");
				command.append(getFtpUser());
				command.append(":");
				command.append(getFtpPassword());
				command.append("@");
				command.append(getUrl());
				command.append(getFtpDirectory());
				command.append(" "+ fileToSend);
				command.append(" " + getSftpCommands()); 
				successCount++;
					REPORT_COUNTER++;//APSL753
				if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Executing command = " + command.toString());
				}
                Process p = rt.exec(command.toString()) ;
                if (getLogger().isDebugEnabled()) {
					getLogger().logDebug("Command Result = " + p.waitFor());
				}
				}
			}
        } catch (NbaBaseException nbe) {
        	throw nbe; //throw the base exception so poller fails
        } catch(Exception e) {
        	failCount++;
        	getLogger().logError(ERROR_MESSAGE.concat(fileToSend));
        	e.printStackTrace();
        }
	}
	/**
	 * browse through directories and determine if SFTP processing is needed
	 * @throws NbaBaseException
	 */
	//ALS4376 new method
    protected void readDirectory() throws NbaBaseException {
    	 
	        Runtime rt = Runtime.getRuntime() ;
	        sftpFiles(rt,getReportDirectory());
/*	        try {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					copyFile(listOfFiles[i], sdf.format(c1.getTime()));
					fileToSend = listOfFiles[i].getPath();
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("Sending report ["+ fileToSend + "] via FTP" );
					}
					command.setLength(0); //reset buffer;
					command.append("\"" + getSftpClientLocation()  + "\"");
					command.append(" httpsu://");
					command.append(getFtpUser());
					command.append(":");
					command.append(getFtpPassword());
					command.append("@");
					command.append(getUrl());
					command.append(getFtpDirectory());
					command.append(" "+ fileToSend);
					command.append(" " + getSftpCommands()); 
					successCount++;
					if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("Executing command = " + command.toString());
					}
	                Process p = rt.exec(command.toString()) ;
	                if (getLogger().isDebugEnabled()) {
						getLogger().logDebug("Command Result = " + p.waitFor());
					}
					}
				}
	        } catch(Exception e) {
	        	failCount++;
	        	getLogger().logError(ERROR_MESSAGE.concat(fileToSend));
	        	e.printStackTrace();
	        }
    */     
    }
    /**
     * takes existing file and backs it up to backup directory defined in NbaConfiguration.xml
     * @param file
     * @param dateStamp
     */
    private void copyFile(File file, String dateStamp) {
    	if (getLogger().isDebugEnabled()) {
    		getLogger().logDebug("backing up report: " + file.getPath());
    	}
    	StringBuffer backupBuffer = new StringBuffer();
    	backupBuffer.append(file.getName());
    	backupBuffer.append("_");
    	backupBuffer.append(dateStamp);
    	File backupFile = new File(getReportDirectory(), backupBuffer.toString());
    	
    	if (getLogger().isDebugEnabled()) {
    		getLogger().logDebug("backing up report as: " + backupBuffer.toString());
    	}
    	InputStream in = null;
    	OutputStream out = null;
    	try {
    		if (!backupFile.exists()) {
        		backupFile.createNewFile();
        	}
       	 	in = new FileInputStream(file);
       	 	out = new FileOutputStream(backupFile);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0){
              out.write(buf, 0, len);
            }

            if (getLogger().isDebugEnabled()) {
        		getLogger().logDebug("backing up sucessful for report: " + file.getName());
        	}
       	} catch (Exception e) {
       		getLogger().logError("Unable to backup report: " + file.getName());
       		e.printStackTrace();
       	} finally {
       		try {
       			if (null != in) {
       				in.close();
       			}
       			if (null != out) {
       				out.close();
       			}
       		} catch (IOException ioe) {
       			getLogger().logError(ioe.getMessage());
       		}
       	}
    	
    }
   

    /**
     * Perform SFTP processing:
     * - obtain list of all files 
     * - get the file name and contents of the next eligible file to process in the paths.
     * - if there are no eligible files:
     *      return an NbaAutomatedProcessResult with a return code of "2" (no work)
     * - if an eligible file is found:
     * -    backup the file
     * -    SFTP the file
     * -    delete the file (via /move command)
     * -    return an NbaAutomatedProcessResult with a return code of "0" (successful)
     * @param user the NbaUser for whom the process is being executed
     * @param work - null
     * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
     * @throws NbaBaseException
     */
    public NbaAutomatedProcessResult executeProcess(NbaUserVO nbaUserVO, NbaDst nbaDst) throws NbaBaseException {
        try {
        	REPORT_COUNTER=0;//APSL753--Reset Report Counter to 0;
            long startTime = System.currentTimeMillis(); 
            initialize(nbaUserVO, nbaDst);  //ALS4376
            readDirectory();
            //if everything was succesful, send 'success' to Admin Console
            if (successCount> 0 && failCount == 0) {
            	setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESS_NOWORK, "", ""));  //success..let it ry again
            } else if (successCount == 0 && failCount == 0) {
            	setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", ""));  //reply no work to wait designated time for retry
            } else {
            	setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "", ""));
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
	 * Call the AXA Controller Single SignOn Web Service using AXA's EIB to get the SFTP credentials 
	 * @return HashMap  Four entries: uploadfolder, userpassword, uid and url 
	 * @throws NbaBaseException
	 */

	public void getSFTPCredentials() throws NbaBaseException {
		//don't invoke again if we already have the information
		if (null != getFtpUser() ) { //ALS4376
			return; //ALS4376
		} //ALS4376
		try {
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("getSFTPCredentials() - Invoking the AXA Controller Single SignOn Web Service to get SFTP credentials");
			}
			// Begin ALII53
			String systemId = NbaConstants.SYST_CAPS;
			if(getWork() != null && getWork().getNbaLob() != null && !NbaUtils.isBlankOrNull(getWork().getNbaLob().getBackendSystem())){
				systemId = getWork().getNbaLob().getBackendSystem();
			}
			//Web service call
			NbaWebServiceAdapter service = NbaWebServiceAdapterFactory.createWebServiceAdapter(systemId, "Controller", "ControllerSingleSignOn");
			Map params = new HashMap();
	        params.put(NbaAxaServiceRequestor.PARAM_SERVICEOPERATION, NbaAxaServiceRequestor.OPERATION_SECURE_FILE_TRANSFER);
	        Map results = service.invokeAxaWebService(params);

		    // Process Response
    		if (results == null || results.isEmpty()) {
    		    throw new Exception("NULL element in the web service response");
    		}
    		if (getLogger().isDebugEnabled()) {
    			getLogger().logDebug("getSFTPCredentials() - Response received from Web Service = "  + results.toString());
    		}
            setFtpPassword((String) results.get(NbaAxaServiceResponse.PASSWORD_ELEMENT));
            setFtpUser((String) results.get(NbaAxaServiceResponse.USERID_ELEMENT));
            setUrl((String) results.get(NbaAxaServiceResponse.URL_ELEMENT));
            setFtpDirectory((String) results.get(NbaAxaServiceResponse.UPLOAD_FOLDER_ELEMENT));
    		
		}
		catch (Exception e) {
			NbaBaseException nbe = new NbaBaseException("Error invoking the AXA Controller Single SignOn Web Service to get SFTP credentials " + e.getMessage(), e);
			nbe.forceFatalExceptionType();
			throw nbe;
		}
		
	} 
   
    

    /**
     * Read NbaConfiguration to get SFTP parameters. If any are missing, or sftpDirectory is not a dir, force a fatal exception to error stop the poller
     * value.
     * @throws NbaBaseException
     * @throws NbaBaseException
     */
    public boolean initialize(NbaUserVO nbaUserVO, NbaDst nbaDst) throws NbaBaseException {
        setUser(nbaUserVO);
        setWork(nbaDst);
        try {
        setReportDirectory(NbaConfiguration.getInstance().getBusinessRulesAttributeValue("reportPath"));
        setSftpCommands(NbaConfiguration.getInstance().getBusinessRulesAttributeValue("sftpCommands"));
        setReportType(NbaConfiguration.getInstance().getBusinessRulesAttributeValue("reportType"));
        setBackupDirectory(" " +  NbaConfiguration.getInstance().getBusinessRulesAttributeValue("reportBackupPath"));;
        setSftpClientLocation(NbaConfiguration.getInstance().getBusinessRulesAttributeValue("sftpClientLocation"));
        //begin ALS4376
    	if ("PDF".equalsIgnoreCase(getReportType())) { 
    		ff = new PDFFileFilter();
    	} else {
    		ff = new HTMLFileFilter();
    	}
    	//end ALS4376
        File folder = new File(getReportDirectory());
		if (!folder.isDirectory()) { 
			throw new NbaBaseException("Configured report path is not a directory!", NbaExceptionType.FATAL);
		}

        } catch (NbaBaseException nbe) {
        	if (!nbe.isFatal()) {
        		nbe.forceFatalExceptionType();
        	}
        	throw nbe;
        }
        return true;
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
            case NbaAutomatedProcessResult.SUCCESS_NOWORK:
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
            buf.append("ReportProcess was ").append(System.currentTimeMillis() - startTime).append(" milliseconds");
            getLogger().logInfo(buf.toString());
        }
    }

    private final class PDFFileFilter implements java.io.FileFilter {
		public boolean accept(File file) {
            return file.isDirectory() || (file.isFile() && file.getName().toUpperCase().endsWith("PDF"));  //ALS4597
        }
	}
	private final class HTMLFileFilter implements java.io.FileFilter {
		public boolean accept(File file) {
            return file.isDirectory() || (file.isFile() && file.getName().toUpperCase().endsWith("HTML"));  //ALS4597
        }
	}	   
 
}
