package com.csc.fsg.nba.correspondence;

/*
 * ************************************************************** <BR>
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
 * ************************************************************** <BR>
 * 
 */
import java.io.ByteArrayOutputStream;
import java.util.List;
import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.nbaschema.Correspondence;
import com.csc.fsg.nba.vpms.CopyLobsTaskConstants;
import com.csc.fsg.nba.vpms.NbaVPMSHelper;

/** 
 * 
 * This class provides a easy way to create Correspondence Work Items and Sources
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA013</td><td>Version 2</td><td>Correspondence System</td></tr>  
 * <tr><td>NBA062</td><td>Version 3</td><td>EnCorr Web Service</td></tr>  
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * <tr><td>NBA097</td><td>Version 4</td><td>Work Routing Reason Displayed</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA146</td><td>Version 6</td><td>Workflow integration</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>AXAL3.7.13I</td><td>AXA Life Phase 1</td><td>Informal correspondence</td></tr>
 * <tr><td>ALPC96</td><td>AXA Life Phase 1</td><td>xPression OutBound Email</td></tr>
 * <tr><td>NBA239</td><td>Version 8</td><td>Improving Approve Transaction timing to <=3 seconds</td></tr>
 * <tr><td>ALII1334</td><td>AXA Life Phase 2</td><td>Event Driven Correspondence causing AWD Unlock issue</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */
public class NbaCorrespondenceUtils {
    protected final static String CASE_PREFIX = "C";
    protected final static String CORRESPONDENCE_AUTOPROC = "NBAPROCCORRESPONDENCE";
    public final static String LETTER_ONDEMAND = "ONDEMAND";
    public final static String LETTER_EVENTDRIVEN = "EVENTDRIVEN";
    protected boolean needSiblingOrChildren;
    protected byte[] image;
    protected String status;
    protected String transactionID;
    protected NbaDst workItem;
    protected com.csc.fsg.nba.vo.nbaschema.Correspondence sourceXML;  //ACN012
    protected NbaLogger logger;
	//NBA213 deleted code
    protected NbaUserVO user;
    protected NbaDst parentCase; //NBA239
	protected String letterType;	//NBA062

	//NBA146
	public NbaCorrespondenceUtils(NbaUserVO userVO){
	    user = userVO;
	}
	
/**
 * This method is returns Image bytes.
 * @return byte[]
 */
public byte[] getImage() {
    return image;
}
/**
 * This method returns an instance of <code>NbaLogger</code>. 
 * If an instance is not available it creates one.
 * @return com.csc.fsg.nba.foundation.NbaLogger
 */
protected NbaLogger getLogger() {
    if (logger == null) {
        try {
            logger = NbaLogFactory.getLogger(this.getClass().getName());
        } catch (Exception e) {
            NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
            e.printStackTrace(System.out);
        }
    }
    return logger;
}
//NBA213 deleted code
/**
 * This method returns Source XML.
 * @return com.csc.fsg.nba.vo.nbaschema.Correspondence
 */
public Correspondence getSourceXML() {
    return sourceXML;
}
/**
 * This method returns a status.
 * @return java.lang.String
 */
public String getStatus() {
    return status;
}
/**
 * This method returns a Transaction ID.
 * @return java.lang.String
 */
public String getTransactionID() {
    return transactionID;
}
/**
 * This method returns an instance of <code>NbaUserVO</code>.
 * @return com.csc.fsg.nba.vo.NbaUserVO
 */
public com.csc.fsg.nba.vo.NbaUserVO getUser() throws NbaBaseException {
	//NBA146 code deleted
	return user;
}
/**
 * This method returns an instance of <code>NbaDst</code>.
 * @return com.csc.fsg.nba.vo.NbaDst
 */
public NbaDst getWorkItem() {
    return workItem;
}
/**
 * This method retrieves a Case and/or Transactions based on a Transaction ID.
 * @return com.csc.fsg.nba.vo.NbaDst
 * @exception com.csc.fsg.nba.exception.NbaBaseException Thsi exception is throuwn whenever an error occurs.
 */
public NbaDst retrieveCase() throws NbaBaseException {
	//NBA213 deleted code
        boolean isCase = (transactionID.substring(transactionID.length() - 3).startsWith(CASE_PREFIX) ? true : false);
        NbaAwdRetrieveOptionsVO vo = new NbaAwdRetrieveOptionsVO();
        vo.requestCaseAsParent();
        if (needSiblingOrChildren) {
            if (isCase) {
                vo.requestTransactionAsChild();
            } else {
                vo.requestTransactionAsSibling();
            }
        }
        vo.requestSources();
        vo.setWorkItem(transactionID, isCase);
        //This is the Parent Case
        parentCase = WorkflowServiceHelper.retrieveWorkItem(getUser(), vo);  //NBA213, NBA239
	//NBA213 deleted code
    return parentCase;  //NBA239
}
/**
 * This method retrieves a Case and Transactions based on a Transaction ID.
 * @return com.csc.fsg.nba.vo.NbaDst
 * @exception com.csc.fsg.nba.exception.NbaBaseException Thsi exception is throuwn whenever an error occurs.
 */
public NbaDst retrieveCaseAndTransactions() throws NbaBaseException {
    needSiblingOrChildren = true;
    return retrieveCase();
}
/**
 * This method is used to set image bytes.
 * @param newImage These are the image bytes
 */
public void setImage(byte[] newImage) {
    image = newImage;
}
/**
 * This method is used set the source xml.
 * @param newSourceXML A <code>Correspondence</code> object representing the source xml.
 */
public void setSourceXML(Correspondence newSourceXML) {
    sourceXML = newSourceXML;
}
/**
 * This method is used to set the status.
 * @param newStatus a valid AWD status
 */
public void setStatus(String newStatus) {
    status = newStatus;
}
/**
 * This method is used to set a transaction id.
 * @param newTransactionID A Case or Work iTem Id
 */
public void setTransactionID(String newTransactionID) {
    transactionID = newTransactionID;
}
/**
 * This method is used to set the <code>NbaUserVO</code>.
 * @param newUser an <code>NbaUserVO</code> instance
 */
public void setUser(NbaUserVO newUser) {
    user = newUser;
}
/**
 * This method is used to set the <code>NbaDst</code> object if it refers to a Correspondence or requirement
 * Work Item.
 * @param newWorkItem A <code>NbaDst</code> instance
 * @exception com.csc.fsg.nba.exception.NbaBaseException This Exception is thrown if the Work Item is not a Correspondence or Requirement Work Item.
 */
public void setWorkItem(NbaDst newWorkItem) throws NbaBaseException {
    if (newWorkItem.isCase() || ! (newWorkItem.getTransaction().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_CORRESPONDENCE)
    		|| newWorkItem.getTransaction().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_REQUIREMENT))) { //ALPC96
        throw new NbaBaseException("Can not set Work Item!");
    }
    setTransactionID(newWorkItem.getID());
    workItem = newWorkItem;
}
/**
 * This method is responsible for creating a source, creating a work item if required and updating it to AWD with a valid status.
 * @exception com.csc.fsg.nba.exception.NbaBaseException This Exception is thrown if any error occurs.
 */
	public void updateWorkItem() throws NbaBaseException {
		updateWorkItem(true); //ALII1334
	}
/**
 * This method is responsible for creating a source, creating a work item if required and updating it to AWD with a valid status.
 * @exception com.csc.fsg.nba.exception.NbaBaseException This Exception is thrown if any error occurs.
 */
	//ALII1334 refactored to use boolean to determine if work should be unlocked
	public void updateWorkItem(boolean unlockWork) throws NbaBaseException {
		// ensure that all minimum fields are present
		validateFields();
		if (workItem != null && workItem.isTransaction() && (workItem.getTransaction().getWorkType().equals(NbaConstants.A_WT_CORRESPONDENCE) 
						|| workItem.getTransaction().getWorkType().equals(NbaConstants.A_WT_REQUIREMENT))) {// ALPC96
			workItem.addImageSource(workItem.getNbaTransaction(),NbaConstants.A_ST_CORRESPONDENCE_LETTER, image);
			workItem.setStatus(status);
		} else {
			if (parentCase == null || !parentCase.isCase()) { // NBA239
				retrieveCase();
			}
			workItem = parentCase; // NBA239
			// Use a VP/MS model to recover initial status + work item type along with AWD priorities
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider();
			// NBA146 begin
			com.csc.fsg.nba.vo.configuration.Correspondence config = NbaConfiguration.getInstance().getCorrespondence(); // ACN012
			NbaUserVO tempUserVO = new NbaUserVO(config.getEventUserName(),config.getEventPassword()); // AXAL3.7.13I
			provider.initializeFields(tempUserVO, workItem, sourceXML);
			// NBA146 end
			NbaTransaction aNbaTransaction = workItem.addTransaction(provider.getWorkType(), provider.getInitialStatus());
			aNbaTransaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
			// Begin ALII811
			List lobList = new NbaVPMSHelper().getLOBsToCopy(CopyLobsTaskConstants.N2CORREQ_CREATE_CORREQ);
			NbaLob transactionLOBs = aNbaTransaction.getNbaLob();
			workItem.getNbaLob().copyLOBsTo(transactionLOBs, lobList);
			// End ALII811

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			sourceXML.marshal(stream);
			// Begin ALII811
			NbaSource corrSource = new NbaSource(workItem.getBusinessArea(),NbaConstants.A_ST_CORRESPONDENCE_XML, stream.toString());
			aNbaTransaction.addNbaSource(corrSource);
			NbaLob sourceLobs = corrSource.getNbaLob();
			workItem.getNbaLob().copyLOBsTo(sourceLobs, lobList);
			// End ALII811
			aNbaTransaction.getNbaLob().setLetterType(letterType); // NBA062
			if (image != null) {
				workItem.addImageSource(aNbaTransaction,
						NbaConstants.A_ST_CORRESPONDENCE_LETTER, image);
			}
		}
		// NBA213 deleted code
		NbaUtils.setRouteReason(workItem, workItem.getStatus()); // NBA097
		workItem = WorkflowServiceHelper.update(getUser(), workItem); // SPR1851,NBA213
		// SPR1851 code deleted
		if (unlockWork && (workItem.isCase() || !workItem.getTransaction().getWorkType().equals(NbaConstants.A_WT_REQUIREMENT))) {// ALPC96  ALII1334
			WorkflowServiceHelper.unlockWork(getUser(), workItem);
		}
		// NBA213 deleted code
	}
/**
 * This method is used to valid required fields. It also initialized some fields if not present.
 * @exception com.csc.fsg.nba.exception.NbaBaseException This Exception is thrown if any of the required fields is missing.
 */
protected void validateFields() throws NbaBaseException {
    if (sourceXML == null && image == null) {
        throw new NbaBaseException(this.getClass().getName() + ": Source not set!");
    }
    if (transactionID == null) {
        throw new NbaBaseException(this.getClass().getName() + ": Transaction or Transaction Id not set!");
    }
}
	/**
	 * Returns the letterType.
	 * @return String
	 */
	//NBA062 new method
	public String getLetterType() {
		return letterType;
	}

	/**
	 * Sets the letterType.
	 * @param letterType The letterType to set
	 */
	//NBA062 new method	
	public void setLetterType(String letterType) {
		this.letterType = letterType;
	}
	//NBA239 New Method
	public NbaDst getParentCase() {
		return parentCase;
	}
	//NBA239 New Method
	public void setParentCase(NbaDst parentCase) {
		this.parentCase = parentCase;
	}
}
