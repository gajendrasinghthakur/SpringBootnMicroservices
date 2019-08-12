package com.csc.fsg.nba.provideradapter;

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
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Snow.Snowbnd;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.business.process.NbaAutomatedProcess;
import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaObjectPrinter;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaPerformanceLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Participant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Payout;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

/**
 * NbaProviderAdapter is the interface for connecting to a provider for the purpose
 * of ordering and receiving requirements.
 * <p>This interface defines the methods the different subclasses must implement 
 * in order to create a commonality of function between the classes that create,
 * send, receive and process messages to/from providers.
 * <p>For purposes of nbAccelerator, a Provider may be defined as an entity that 
 * receives a request for an insurance requirement of some type (MIB report, 
 * Motor Vehicle Report (MVR), Attending Physician's Statement (APS), paramed, etc),
 * arranges for the requirement to be fulfilled and provides the results of that 
 * requirement to the requesting party.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td><tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>121/1122 Migration</td></tr>
 * <tr><td>ACN009</td><td>Version 4</td><td>ACORD 401/402 MIB Inquiry and Update Migration</td></tr>
 * <tr><td>SPR1486</td><td>Version 4</td><td>MIB Codes not transmitted on clicking MIB Transmit Button</td></tr>
 * <tr><td>SPR2427</td><td>Version 5</td><td>TempReq work item going to error queue in the Provider Index process for MIB requirement, if the 401 response xml contains <RequirementInfo> object without <AppliestoParty> attribute.</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>SPR2697</td><td>Version 6</td><td>Requirement Matching Criteria Needs to Be Expanded</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7<td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPRNBA-597</td><td>Version NB-1301</td><td> Image data should not be stored in Attachments</td></tr>
 * <tr><td>APSL5085 </td><td>Discretionary</td><td>Requirement As Data</td></tr>
 * <tr><td>NBLXA-1656</td><td>Version NB-1501</td><td>nbA Requirement Order Statuses from Third Party Providers</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see NbaProviderAdapterFacade
 * @since New Business Accelerator - Version 2
 */
//ACN014 Changed from interface to class
public abstract class NbaProviderAdapter implements NbaConstants {
	public final static java.lang.String TRANSACTION = "TRANSACTION"; //SPR1486
	java.lang.String RESPONSE_LIST = "RESPONSE_LIST"; //ACN008
	private final static String CDATA_BEGIN = "<![CDATA["; //ACN014
	private final static String CDATA_END = "]]>"; //ACN014
	protected NbaSource providerSupplementSource = null;      //SPRNBA-597

	/**
	 * This method converts the XML Requirement transactions into a format
	 * that is understandable by the provider.
	 * @param aList array list of requirement transactions
	 * @return a provider ready message in a HashMap which includes any errors that might have occurred.
	 * @exception NbaBaseException thrown if an error occurs.
	 */
	//ACN014 Changed signature
	abstract Map convertXmlToProviderFormat(List aList)throws NbaBaseException;
	/**
	 * This method searches for a provider supplement source and returns
	 * the data within that source.
	 * @param work the requirement work item.
	 * @return String
	 * @exception NbaBaseException thrown if an error occurs.
	 */
	//ACN014 New Method
	public String getDataFromSource(NbaDst work)throws NbaBaseException{

		NbaSource source = getProviderSupplement(work); //ALS4880
		String response = null;

		//ALS4880 code deleted

		// check if response source has valid data
		if (source != null) {
			response = source.getText();
			if (response == null || response.trim().length() == 0) {
				throw new NbaBaseException("Provider response source is missing or invalid"); // SPR1234 - correct spelling
			}
			setProviderSupplementSource(source); //SPRNBA-597
		} else {
			throw new NbaBaseException("Provider response source is missing or invalid"); // SPR1234 - correct spelling
		}
		return response;
	}

	//ALS4880 new method
	public NbaSource getProviderSupplement(NbaDst work) {
		List list = work.getNbaSources();
		NbaSource source = null;

		// get provider response source
		for (int i = 0; i < list.size(); i++) {
			source = (NbaSource) list.get(i);
			if (source.getSource().getSourceType().equals(NbaConstants.A_ST_PROVIDER_SUPPLEMENT)) {
				return source;
			} 
		}
		return null;
	}

	/**
	 * This method converts the Provider's response into XML transaction.It 
	 * also updates required LOBs and result source with converted XMLife.
	 * @param work the requirement work item.
	 * @return an ArrayList containing the requirement work items with formated source.
	 * @exception NbaBaseException thrown if an error occurs.
	 */
	//ACN014 Changed signature
	abstract ArrayList processResponseFromProvider(NbaDst work, NbaUserVO user) throws NbaBaseException;

	/**
	 * Create and initialize an <code>NbaVpmsResultsData</code> object to compute the entry
	 * point for the model specified.
	 * @param model the name of a VPMS model to be invoked
	 * @param entryPoint the entryPoint in the model to be computed
	 * @return NbaVpmsResultsData the VP/MS results
	 * @throws NbaBaseException
	 */
	// ACN014 New method
	protected NbaVpmsResultsData getDataFromVpms(String model, String entryPoint, NbaUserVO user, NbaDst work) throws NbaBaseException {
		NbaVpmsAdaptor vpmsProxy = null; //SPR3362
		try {
			NbaOinkDataAccess oinkDataAccess = new NbaOinkDataAccess(work.getNbaLob());
			vpmsProxy = new NbaVpmsAdaptor(oinkDataAccess, model); //SPR3362
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			Map deOink = new HashMap();
			deOink.put(NbaVpmsConstants.A_PROCESS_ID, NbaUtils.getBusinessProcessId(user)); //SPR2639
			vpmsProxy.setSkipAttributesMap(deOink);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsProxy.getResults());
			//SPR3362  code deleted
			return vpmsResultsData;
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException("NbaProcProviderIndex-VPMS Remote Exception occured", re);
			//begin SPR3362
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
				LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}
		//end SPR3362
	}
	/**
	 * This method updates NbaDst work item LOB fields and adds sources found withing
	 * the Attachment object(s).
	 * @param work an NbaDst temporary requirement work item to be updated/indexed
	 * @param reqInfo a RequirementInfo object from the provider results that may contain Attachment objects
	 * @param nbaTxLife the XMLife provider result message
	 */
	//ACN014 new method
	//SPRNBA-597 added images parameter
	protected NbaDst updateWorkItem(NbaDst work, RequirementInfo reqInfo, List images, NbaTXLife nbaTxLife) throws NbaBaseException {
		Party party = getParty(reqInfo, nbaTxLife);
		NbaLob lob = work.getNbaLob(); //SPR2427
		long transType = 0;
		//ACN009 begin
		try {
			if( nbaTxLife.getTXLife().getUserAuthRequestAndTXLifeRequest() != null) {
				transType  = nbaTxLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getTransType(); 
				if( transType == NbaOliConstants.TC_TYPE_GENREQUIRERESTRN || transType == NbaOliConstants.TC_TYPE_MIBINQUIRY || 
						transType == NbaOliConstants.TC_TYPE_MIBUPDATE || transType == NbaOliConstants.TC_TYPE_ADD_UPDATE_MESSAGE) {
					lob.setReqUniqueID(getValue(reqInfo.getRequirementInfoUniqueID())); //ACN014 //SPR2427
				} else {
					lob.setReqUniqueID(nbaTxLife.getPrimaryHolding().getPolicy().getApplicationInfo().getTrackingID()); //SPR2427
				}
			} else if( nbaTxLife.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify() != null) {
				transType = nbaTxLife.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0).getTransType();  
				if( transType == NbaOliConstants.TC_TYPE_GENREQUIRERESTRN || transType == NbaOliConstants.TC_TYPE_MIBINQUIRY || 
						transType == NbaOliConstants.TC_TYPE_MIBUPDATE ) {
					lob.setReqUniqueID(getValue(reqInfo.getRequirementInfoUniqueID())); //ACN014 //SPR2427
				} else {
					lob.setReqUniqueID(nbaTxLife.getPrimaryHolding().getPolicy().getApplicationInfo().getTrackingID()); //SPR2427
				}
			}
		} catch (Exception e) {
			lob.setReqUniqueID(NbaAutomatedProcess.LOB_NOT_AVAILABLE); //SPR2427
		}
		//ACN009 end
		//Begin SPR2427
		if(!( transType == NbaOliConstants.TC_TYPE_MIBINQUIRY ||  transType == NbaOliConstants.TC_TYPE_MIBUPDATE) ) {

			Person person = party.getPersonOrOrganization() != null ? party.getPersonOrOrganization().getPerson() : null;
			if (person != null) {
				lob.setLastName(getValue(person.getLastName())); //ACN014
				lob.setFirstName(getValue(person.getFirstName())); //ACN014
				//begin SPR2697
				lob.setMiddleInitial(getValue(person.getMiddleName()));
				if (person.hasBirthDate()) {
					lob.setDOB(person.getBirthDate());
				}
				if (person.hasGender()) {
					lob.setGender(getValue(person.getGender()));
				}
				//end SPR2697
			}
			lob.setSsnTin(getValue(party.getGovtID())); //ACN014
			lob.setReqVendor(getValue(lob.getReqVendor())); //ACN014
			//End SPR2427
			lob.setReqType((new Long(reqInfo.getReqCode())).intValue());
			//NBLXA-1656 Starts
			//if (!NbaUtils.isBlankOrNull(reqInfo.getReqCode()) && reqInfo.getReqCode() == NbaOliConstants.OLI_REQCODE_PHYSSTMT&& //NBLXA-1777
			if(!NbaUtils.isBlankOrNull(nbaTxLife.getUserAuthRequest()) && !NbaUtils.isBlankOrNull(nbaTxLife.getTransSubType()) //NBLXA-1777
					&& nbaTxLife.getTransSubType() == NbaOliConstants.OLI_TRANSSUB_INQREQUIREMENT) // QC-19653
			{
				lob.setReqSubStatus(NbaConstants.SUB_STATUS); 
			}
			//NBLXA-1656 Ends
			// NBLXA-1822 Starts
			if ((!NbaUtils.isBlankOrNull(nbaTxLife.getTXLife().getUserAuthRequestAndTXLifeRequest()))
					&& (nbaTxLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getTransType() == NbaOliConstants.TC_TYPE_ADD_UPDATE_MESSAGE)) {
				lob.setReqSubStatus(NbaConstants.REQ_AGENT_CODE);
			}
			// NBLXA-1822 Ends
			
			lob.setReqDrName(getDoctorName(reqInfo, nbaTxLife)); //SPR2697
		}
		addSourcesForImages(work, images); //SPRNBA-597
		//ACN014 END
		return work;
	}
	/** 
	 * This method extracts AttachmentData and adds it as a Source to the
	 * NbaDst work item. The type of source added, Image or Data, is determined
	 * by the AttachmentBasicType within the Attachment object.
	 * @param work an NbaDst work item to which the new source will be added
	 * @param anAttachment the Attachment that contains the data which will
	 * populate the source
	 */
	//ACN014 new method
	protected void addNewSource(NbaDst work, Attachment anAttachment) throws NbaBaseException {
		String info = "AttachmentData removed and placed in AWD source";
		if( anAttachment.getAttachmentBasicType() == NbaOliConstants.OLI_LU_BASICATTMNTTY_IMAGE) {
			byte[] data;
			if(anAttachment.getAttachmentLocation() == NbaOliConstants.OLI_INLINE) { // data is included in attachment
				data = NbaBase64.decode(anAttachment.getAttachmentData().getPCDATA());
			} else {
				data = getImageFromExternalFile(anAttachment.getAttachmentData().getPCDATA());
			}
			if (data != null) {
				anAttachment.getAttachmentData().setPCDATA(info);
				work.addImageSource(work.getNbaTransaction(), A_ST_PROVIDER_RESULT, data);
			}
		} else {
			String data;
			if( anAttachment.getAttachmentLocation() == NbaOliConstants.OLI_INLINE) {
				data = anAttachment.getAttachmentData().getPCDATA();
			} else {
				data = String.valueOf(getDataFromExternalFile(anAttachment.getAttachmentData().getPCDATA()));
			}  
			if (data != null) {
				anAttachment.getAttachmentData().setPCDATA(info);
				work.addNbaSource(new NbaSource(A_BA_NBA, A_ST_PROVIDER_RESULT, data));
			}
		}
	}

	/**
	 * This method evalutes value and if it is not empty, returns the value;
	 * otherwise, it returns "N/A". It is used to update LOB fields so that
	 * empty fields will not return too many results.
	 * @param value a value to be evaluated
	 * @return a String containing the value or NbaAutomatedProcess.LOB_NOT_AVAILABLE ("N/A")
	 */
	//ACN014 new method
	protected String getValue(String value) {
		if( value != null && value.length() > 0) {
			return value;		
		} else {
			return NbaAutomatedProcess.LOB_NOT_AVAILABLE;
		}
	}
	/**
	 * This method will retrieve the data from an external file specified by
	 * fileName.
	 * Currently, this method returns null as implementation is dependent on
	 * interaction with provider.
	 * This method must be written at implementation time if the feature is
	 * required.
	 * @param fileName a string containing the URL or path/name of the file
	 * @return a byte array containing the image from the file
	 */
	protected byte[] getImageFromExternalFile(String fileName) throws NbaBaseException {
		return null;
	}
	/**
	 * This method will retrieve the data from an external file specified by
	 * fileName.
	 * Currently, this method returns null as implementation is dependent on
	 * interaction with provider.
	 * This method must be written at implementation time if the feature is
	 * required.
	 * @param fileName a string containing the URL or path/name of the file
	 * @return a byte array containing the image from the file
	 */
	protected String getDataFromExternalFile(String fileName) throws NbaBaseException {
		return null;
	}
	/**
	 * This method finds the Party associated with the requirement.
	 * If RequirementInfo.AppliesToParty is not present,it assumes that 
	 * there is atleast one coverage or payout with one lifeParticipant present on the TxLife. 
	 * @param RequirementInfo object Value from MIB Response XML
	 * @param NbaTXLife  the XMLife provider result message
	 * @return the located Party ojbect
	 */
	//ACN014 new method
	protected Party getParty(RequirementInfo reqInfo, NbaTXLife nbaTxLife) throws NbaBaseException {
		OLifE olife = nbaTxLife.getOLifE();
		if( olife == null) {
			throw new NbaBaseException("No olife found");
		}
		//SPR2427 Code Deleted
		//Begin SPR2427
		//If the AppliesToParty is present,get the Party using RequirementInfo.AppliesToPartyID
		String partyID = reqInfo.getAppliesToPartyID();
		NbaParty party = nbaTxLife.getParty(partyID);
		if (party != null) {
			return party.getParty();
		}
		//
		//Checks if we can get the Party using Life.Coverage.LifeParticipant PartyID
		Life life = nbaTxLife.getLife();
		if (life != null) {
			Coverage coverage = (life.getCoverageCount() > 0) ? life.getCoverageAt(0) : null;
			if (coverage != null) {
				LifeParticipant lifeParticipant = coverage.getLifeParticipantCount() > 0 ? coverage.getLifeParticipantAt(0) : null;
				partyID = lifeParticipant != null ? lifeParticipant.getPartyID() : null;
				party = nbaTxLife.getParty(partyID);
				if (party != null) {
					return party.getParty();
				}
			}
		}
		//Checks if we can get the Party using Annuity.Payout.Participant.PartyID
		Annuity annuity = nbaTxLife.getAnnuity();
		if (annuity != null) {
			Payout payout = (annuity.getPayoutCount() > 0) ? annuity.getPayoutAt(0) : null;
			if (payout != null) {
				Participant participant = payout.getParticipantCount() > 0 ? payout.getParticipantAt(0) : null;
				partyID = participant != null ? participant.getPartyID() : null;
				party = nbaTxLife.getParty(partyID);
				if (party != null) {
					return party.getParty();
				}
			}
		}
		//End SPR2427
		throw new NbaBaseException("Party not found for RequirementInfo object " + reqInfo.getId());
	}

	/**
	 * Creates an AWD transaction
	 * @param NbaUserVO the user value object
	 * @param NbaDst the workitem object
	 * @return com.csc.fsg.nba.vo.NbaDst
	 */
	// ACN014 new method
	public NbaDst createTransaction(NbaUserVO user, NbaDst work) throws NbaBaseException {
		NbaProcessWorkItemProvider workProvider = new NbaProcessWorkItemProvider(user,work); //SPR2639
		WorkItem transaction = new WorkItem();  //NBA208-32
		NbaDst nbaDst = new NbaDst();
		nbaDst.setUserId(user.getUserID());
		nbaDst.setPassword(user.getPassword());  //NBA208-32
		nbaDst.addTransaction(transaction);  //NBA208-32
		//set Business Area, Work type and Status
		transaction.setBusinessArea(work.getBusinessArea());
		transaction.setWorkType(workProvider.getWorkType()); //SPR2639
		transaction.setStatus(workProvider.getInitialStatus()); //SPR2639
		transaction.setCreate("Y");  //NBA208-32
		return nbaDst;//SPR3290	
	}
	/**
	 * Return the doctor name from the response. RequirementInfoExtension.PhysicianPartyID contains the id 
	 * of the Party object for the Physician. Locate that Party and return Party.FullName 
	 * @param requirementInfo - the RequirementInfo containing the RequirementInfoExtension
	 * @param nbaTxLife - the NbaTXLife encapsulating the response
	 * @return Party.FullName from the party object if the Physician Party can be located. Otherwise retturn an empty string.
	 */
	// SPR2697 New Method
	protected String getDoctorName(RequirementInfo requirementInfo, NbaTXLife nbaTxLife) {
		String name = "";
		RequirementInfoExtension ext = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
		if (ext != null) {
			String partyId = ext.getPhysicianPartyID();
			if (partyId != null) {
				NbaParty nbaParty = nbaTxLife.getParty(partyId);
				if (nbaParty != null) {
					Party party = nbaParty.getParty();
					if (party != null) {
						Person person = party.getPersonOrOrganization() != null ? party.getPersonOrOrganization().getPerson() : null;
						if (person != null) {
							name = NbaObjectPrinter.formatFullName(person.getFirstName(), person.getMiddleName(), person.getLastName());
						}                        
					}
				}
			}
		}
		return name;
	}
	/**
	 * Return the String value for a long.
	 * @param value a the long field
	 * @return a String containing the String value 
	 */
	//SPR2697 new method
	protected String getValue(long value) {
		return Long.toString(value);
	}
	//SPRNBA-597 New Method	
	protected void addSourcesForImages(NbaDst work, List images) throws NbaBaseException {
		int count = images.size();
		for (int i = 0; i < count; i++) {
			Object image = images.get(i);
			if (image instanceof String) {
				work.addNbaSource(new NbaSource(A_BA_NBA, A_ST_PROVIDER_RESULT, (String) image));
			} else {
				work.addImageSource(work.getNbaTransaction(), A_ST_PROVIDER_RESULT, (byte[]) image);
			}
		}
	}
	/**
	 * Return the NbaSource for the Provider Supplement.
	 */
	//SPRNBA-597 New Method
	protected NbaSource getProviderSupplementSource() {
		return providerSupplementSource;
	}
	/**
	 * Store the NbaSource for the Provider Supplement.
	 */
	//SPRNBA-597 New Method
	protected void setProviderSupplementSource(NbaSource source) {
		this.providerSupplementSource = source;
	} 
	//APSL5085: New Method
	protected void addSourcesForImages(NbaDst work, List images, boolean needToSetPGRGLob) throws NbaBaseException {
		NbaPerformanceLogger.initMethod("addSourcesForImages");
		int count = images.size();
		for (int i = 0; i < count; i++) {
			Object image = images.get(i);
			if (image instanceof String) {
				work.addNbaSource(new NbaSource(A_BA_NBA, A_ST_PROVIDER_RESULT, (String) image));
			} else {
				work.addImageSource(work.getNbaTransaction(), A_ST_PROVIDER_RESULT, (byte[]) image);
			}
			if (needToSetPGRGLob && (work.getNbaLob() != null)) {
				byte[] byteArr = null;
				if (image instanceof String) {
					byteArr = NbaBase64.decode((String) image);
				} else {
					byteArr = (byte[]) image;
				}
				if (byteArr != null) {
					DataInputStream di;
					di = new DataInputStream(new java.io.ByteArrayInputStream(byteArr));
					Snowbnd snbd = new Snowbnd();
					NbaPerformanceLogger.logElapsed("[Retrieving tiff image pages] Start Time:", System.currentTimeMillis());
					int pages = snbd.IMGLOW_get_pages(di);
					NbaPerformanceLogger.logElapsed("[Retrieved tiff image pages]:" + pages, System.currentTimeMillis());
					if (pages>1){
						if (pages < 4) {
							work.getNbaLob().setPageRange("1"); // For old forms setting page range 1 to print only one page.
						} else if (pages >= 6) {
							work.getNbaLob().setPageRange("1-" + (pages - 2)); // If count is >=6,means received new Paramed Form,removing last 2 pages
							// from the count to prevent Paramed Report pages to print.
						} else {
							work.getNbaLob().setPageRange("1-" + pages); // Printing all pages,if new form received without Paramed
							// Report pages.
						}
					}
				}
			}
		}
	}
}
