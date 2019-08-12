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

import com.csc.fsg.nba.business.transaction.NbaRequirementUtils;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaXMLDecorator;
import com.csc.fsg.nba.vo.nbaschema.Requirement;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;

/**
 * This is the class to process transactions found in NBPSTREQ queue.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA020</td><td>Version 2</td><td>AWD Priority</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>NBP001</td><td>Version 3</td><td>nbProducer Initial Development</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>121/1122 Migration</td></tr>
 * <tr><td>SPR2811</td><td>Version 5</td><td>Doctor information on Requirements is being inserted into the NBA Pending Database</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>General Code Clean Up Issues for Version 6</td></tr>
 * <tr><td>SPR3185</td><td>Version 6</td><td>Error from Host when submitting a receipted duplicate temp requirement.</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess
 * @since New Business Accelerator - Version 1
 */
public class NbaProcPostRequirement extends NbaAutomatedProcess {
	
    protected static final String OLI_REQSTAT_RECEIVED = String.valueOf(NbaOliConstants.OLI_REQSTAT_RECEIVED); //SPR3185
    /**
	 * NbaProcPostOrderRequirement constructor comment.
	 */
	public NbaProcPostRequirement() {
		super();
		//SPR1851 code deleted
	}
	/**
	 * Do a holding inquiry and update the Database with the data retrieved from the host
	 * back-end system for the newly added requirement.
	 * @throws NbaBaseException
	 */
	//SPR3185 change method signature
	protected void doAndProcessHoldingInquiry(boolean receipt) throws NbaBaseException {
		NbaTXLife holdingInq = doHoldingInquiry();
		//This is a 2nd holding inquiry that will only update the database. No trasaction is set 
		//to the BES.
		handleHostResponse(holdingInq);
		//begin NBA130
		if (getResult() == null) { // ensure no failure on holding inquiry
			RequirementInfo reqInfo = holdingInq.getRequirementInfo(requirementInfo.getRequirementInfoUniqueID());
			//begin SPR3185
			//If this is a duplicate requirement that was receipted in the ordered AP
			//set it back to received before leaving POST
			if (receipt){
			    reqInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_RECEIVED); 
			}
			//end SPR3185
			reqInfo.setActionUpdate(); 
			handleHostResponse(doContractUpdate(holdingInq)); 
		}
		if (getResult() == null) { // ensure no failure the DB update from the holding Inq 
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", getPassStatus()));
		}
		//end NBA130
	}

	/**
	 * Post an add or order requirement to the back-end system.
	 * Update the LOB fields of the requirement with key values from the 
	 * back-end system in order to tie the AWD and back-end data.
	 * @param user the AWD automated process user that the work was returned for
	 * @param work a DST value object for which the process is to occur
	 * @return information about the success or failure of the process
	 * @throws NbaBaseException
	 */
	public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
	    
		// Initialization
		if (!initialize(user, work)) {
			return getResult(); //NBA050
		}
		//begin SPR3185 
		//Check to see if the requirement coming in is a duplicate and should be receipted
		//If it is received, change the status so that the requirement can be ordered before 
		//sending it on to the Receipt AP
		boolean receipted = false;
	    if (OLI_REQSTAT_RECEIVED.equals(getWorkLobs().getReqStatus())){
	        receipted = true;
	        getRequirementInfo().setReqStatus(NbaOliConstants.OLI_REQSTAT_ORDER);
	    }
	    //end SPR3185
		//Begin ACN014
		//This code will retrieve the sources attached with the workitem.
		//NBA213 deleted code
		NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
		retOpt.setWorkItem(getWork().getID(), false);
		retOpt.requestSources();
		retOpt.setLockWorkItem();
		setWork(retrieveWorkItem(getUser(), retOpt));  //NBA213
		//NBA213 deleted code
		//end ACN014
		//ACN014 begin
		requirementInfo.setActionUpdate(); //NBA130
		//NBA130 code deleted
		if( !updateRequirementControlSource(requirementInfo.getId())) { //NBA130
			getWork().setStatus(getHostErrorStatus());
			setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, getHostErrorStatus(), getHostErrorStatus()));
			doUpdateWorkItem();
			return getResult();
		}
	   	//end NBP001
		
		// NBA050 CODE DELETED
		handleHostResponse(doContractUpdate(nbaTxLife)); // NBA050
		// NBA050 CODE DELETED
		if (getResult() == null) {
		    doAndProcessHoldingInquiry(receipted); //SPR3185
		}
		changeStatus(getResult().getStatus()); // gets pass or fail status
		doUpdateWorkItem();
		//NBA020 code deleted
		return getResult();
	}
    /**
	 * Return requirement occurrence.
	 * 
	 * @return long
	 * @param reqInfo RequirementInfo value object
	 */
	public int getReqOccurrence(RequirementInfo reqInfo) {
		return reqInfo.getSequence();  //NBA093
	}
	//NBA130 Code Deleted
	/**
	 * This method updates the Requirement Control Source to include the
	 * ID from the RequirementInfo object.
	 * @return <code>true</code> if the RequirementControlSource is located and updated;
	 * <code>false</code> is returned otherwise.
	 */
	// ACN014 New Method
	public boolean updateRequirementControlSource(String reqId) {
		//Update control source with redundancy check section
		try {
			NbaSource source = getWork().getRequirementControlSource();
			if (source == null) {
				return false;
			}
			NbaXMLDecorator xmlDecorator = new NbaXMLDecorator(source.getText());
			Requirement req = xmlDecorator.getRequirement();
			req.setRequirementInfoId(reqId);
			source.updateText(xmlDecorator.toXmlString());
			NbaRequirementUtils reqUtils = new NbaRequirementUtils(); 
			reqUtils.updateRequirementControlSource(null,getWork().getNbaTransaction(),xmlDecorator.toXmlString(),NbaRequirementUtils.actionUpdate); //SPR2992
			return true;
		} catch (NbaBaseException nbe) {
			addComment(nbe.getMessage());
			return false;
		}
	}
}
