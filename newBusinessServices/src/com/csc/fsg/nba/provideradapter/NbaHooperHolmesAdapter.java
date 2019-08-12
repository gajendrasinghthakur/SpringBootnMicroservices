package com.csc.fsg.nba.provideradapter;

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
 */
import java.io.File;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.communication.NbaCommunicator;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.provideradapter.xmlschema.NbaProviderAdapterSchema;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.Provider;
import com.csc.fsg.nba.vo.configuration.Request;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.MedicalExam;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.Phone;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RiskExtension;
import com.csc.fsg.nba.vo.txlife.SourceInfo;
import com.csc.fsg.nba.vo.txlife.StatusEvent;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsModelResult;
import com.csc.fsg.nba.vpms.results.ResultData;

/**
 * NbaHooperHolmesAdapter provides support for converting NbaTXLife requirement requests
 * into the transactions required by Hooper Holmes.  In addition, it parses the results received
 * from Hooper Holmes, updates AWD work items for those results and adds any additional sources
 * that might have been received for this work item.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA081</td><td>Version 3</td><td>Hooper Holmes Requirement Ordering and Receipting</td><tr> 
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>SPR1863</td><td>Version 3</td><td>Hooper Holmes Requirement TIF images are not displaying in the UW Workbench</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACN014</td><td>Version 4</td><td>121/1122 Migration</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>ACN009</td><td>Version 4</td><td>ACORD 401/402 MIB Inquiry and Update Migration</td></tr>
 * <tr><td>SPR1601</td><td>Version 4</td><td>Update OLI_LU_REQSTAT to 2.8.90</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see NbaProviderAdapterFacade
 * @since New Business Accelerator - Version 3
 */
//ACN014 Changed to extend NbaProviderAdapter
public class NbaHooperHolmesAdapter extends NbaProviderAdapter {

	private NbaLogger logger = null;
	public java.lang.StringBuffer errorMsg = new StringBuffer();
	private java.lang.StringBuffer providerTransaction = null;
	private Provider provider = null; //ACN012
	protected NbaTXLife hHrequest = null;
	private NbaDst dst = null;
	protected List filenames = new ArrayList(); //SPR1863
	
	public NbaHooperHolmesAdapter() {} //ACN014
	/**
	 * NbaHooperHolmesAdapter constructor.
	 * @param newProvider
	 */
	//ACN012 CHANGED SIGNATURE
	public NbaHooperHolmesAdapter(Provider newProvider) {
		super();
	    if (newProvider == null) {
	        provider = new Provider(); //ACN012
	    }else{
	    	provider = newProvider;
	    }
	}
	
	/**
	 * This method converts the XML Requirement transaction(s) into a format
	 * that is understandable by Hooper Holmes.
	 * @param aList array list of NbaTXLife requirement transactions
	 * @return Map
	 * @exception NbaBaseException thrown if an error occurs.
	 */
	public Map convertXmlToProviderFormat(List aList) throws NbaBaseException {
	    Map aMap = new HashMap();
	    if (aList.size() == 0) {
	        throw new NbaBaseException("XMLife is required");
	    }
	    for (int i = 0; i < aList.size(); i++) {
	        NbaTXLife txlife = (NbaTXLife) aList.get(i);
	        if (getLogger().isDebugEnabled()) { 
	        	getLogger().logDebug("Hooper Holmes Adapter Request for transformation:\n" + txlife.toXmlString());
	        }
	        setHHrequest(txlife);
	        if(getHHrequest() != null){
			   	formatRequest(getHHrequest());
		    }
	       	validateXmlRequest(getHHrequest());
	    	if (errorMsg.length() > 0) {
	            aMap.put(getHHrequest().getTransRefGuid(), errorMsg.toString());
	        }       
	        errorMsg.delete(0, errorMsg.length());
	    }
	    
	    providerTransaction = new StringBuffer(getHHrequest().toXmlString());
	    aMap.put(TRANSACTION, providerTransaction.toString());
	    return aMap;
	}
	/**
	 * Answers an instance of NbaLogger.
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected NbaLogger getLogger() {
	    if (logger == null) {
	        try {
	            logger = NbaLogFactory.getLogger(NbaHooperHolmesAdapter.class.getName());
	        } catch (Exception e) {
	            NbaBootLogger.log("NbaHooperHolmesAdapter could not get a logger from the factory.");
	            e.printStackTrace(System.out);
	        }
	    }
	    return logger;
	}
	/**
	 * Answer provider information in the configuration file.
	 * 
	 * @return NbaConfigProvider provider name of the provider
	 */
	//ACN012 CHANGED SIGNATURE
	protected Provider getProvider() throws NbaBaseException {
		if (provider == null) {
			provider = NbaConfiguration.getInstance().getProvider(NbaConstants.PROVIDER_HOOPERHOLMES);
		}
		return provider;
	}
	
	/**
	 * Validate the mandatory Party fields required to send request transaction to HooperHolmes.
	 * @param aParty a party object to be validated
	 * @return String a String containing any missing fields
	 */
	protected String validatePartyInfo(Party aParty) throws NbaBaseException {
	    try {
	        try {
	            if (aParty.getPersonOrOrganization().getPerson().getFirstName().length() == 0) {
	                errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Person.First Name");
	            }
	        } catch (Exception e) {
	            errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Person.First Name");
	        }
	        try {
	            if (aParty.getPersonOrOrganization().getPerson().getLastName().length() == 0) {
	                errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Person.Last Name");
	            }
	        } catch (Exception e) {
	            errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Person.Last Name");
	        }
	        try{
		        if ( aParty.getAddressAt(0).getLine1().length() == 0) { 
					errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Party.Address Line 1 ");
			    }
			} catch (Exception e) {
	            errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Party.Address Line 1 ");
	        }
	        try {
			    if ( aParty.getAddressAt(0).getCity().length() == 0) { 
					errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Party.Address City ");
			    }
		    } catch (Exception e) {
	            errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Party.Address City ");
	        }
			try {
			    if ( aParty.getAddressAt(0).getAddressStateTC() == -1) {  //NBA093
					errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Party.Address AddressStateTc ");
			    }	
			} catch (Exception e) {
	            errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Party.Address AddressStateTc ");
	        }
	        try {
			    if ( aParty.getAddressAt(0).getZip().length() == 0) { 
					errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Party.Address Zip ");
			    }
	        } catch (Exception e) {
	            errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Party.Address Zip ");
	        }
	    } catch (Exception e) {
	        errorMsg.append("\nParty");
	    }
	   
	    return errorMsg.length() > 0 ? errorMsg.toString() : "";
	}
	
	/**
	 * Validate the mandatory Phone fields required to send request transaction to HooperHolmes.
	 * @param aParty a Party object to be validated
	 * @return String a String containing any missing fields
	 */
	protected String validatePhoneInfo(Party aParty) throws NbaBaseException {
	    // SPR3290 code deleted
	    try {
	        for (int i = 0; i < aParty.getPhoneCount(); i++) {
	            Phone aPhone = aParty.getPhoneAt(i);
	            try {
	                if (aPhone.getPhoneTypeCode()  == -1) {
	                	errorMsg.append("\nPhone.Type Code");
	            	}
	            } catch (Exception e) {
	                errorMsg.append("\nPhone.Type Code");
	            }
	            
	            try {
	                if (aPhone.getAreaCode().length() == 0) {
	                    errorMsg.append("\nPhone.Area Code");
	                }
	            } catch (Exception e) {
	                errorMsg.append("\nPhone.Area Code");
	            }
	            try {
	                if (aPhone.getDialNumber().length() == 0) {
	                    errorMsg.append("\nPhone.DialNumber");
	                }
	            } catch (Exception e) {
	                errorMsg.append("\nPhone.DialNumber");
	            }
	        }
	    } catch (Exception e) {
	        errorMsg.append("\nPhone");
	    }
	    return errorMsg.length() > 0 ? errorMsg.toString() : "";
	}
	/**
	 * Validate the mandatory Relation fields for various Party, required to send request transaction to HooperHolmes.
	 * @param olife an OLifE object to be validated
	 * @return String a String containing any missing fields
	 */
	protected String validateRelationPartyInfo(OLifE olife) throws NbaBaseException {
	    String errors = new String();
	    try {
	        for (int i = 0; i < olife.getRelationCount(); i++) {
	            Relation aRelation = olife.getRelationAt(i);
	            // SPR3290 code deleted
	            try {
	                ArrayList aPartyList = olife.getParty();
	                Party aParty = null;
	                boolean servicingAgencyPresent = false;
	                for (int j = 0; j < aPartyList.size(); j++) {
	                    aParty = (Party) aPartyList.get(j);
	                    if (aParty.getId().equals(aRelation.getRelatedObjectID())) {
	                        if (aRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_SERVAGENT) { // Servicing Agent
	                            errors += validatePartyInfo(aParty);
	                            errors += validatePhoneInfo(aParty);
	                        } else if (aRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_SERVAGENCY) { // Servicing Agency
	                        	servicingAgencyPresent = true;
	                            errors += validatePartyInfo(aParty);
	                            errors += validatePhoneInfo(aParty);
	                        } else if (aRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_PHYSICIAN) { // Physician
	                            errors += validatePartyInfo(aParty);
	                            errors += validatePhoneInfo(aParty);
	                        }
	                        if (errors.length() > 0) {
	                            errorMsg.append(errors);
	                            errors = "";
	                        }
	                    }
	                }
	                if(!servicingAgencyPresent){
	                	for (int j = 0; j < aPartyList.size(); j++) {
	                    aParty = (Party) aPartyList.get(j);
	                    if (aParty.getId().equals(aRelation.getRelatedObjectID())) {
	                        if (aRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_PRIMAGENT) { // Writing Agent
	                            errors += validatePartyInfo(aParty);
	                            errors += validatePhoneInfo(aParty);
	                        } 
	                        if (errors.length() > 0) {
	                            errorMsg.append(errors);
	                            errors = "";
	                        }
	                    }
	                }
	                }
	            } catch (Exception e) {
	                errorMsg.append("\nParty");
	            }
	        }
	    } catch (Exception e) {
	        errorMsg.append("\nRelation");
	    }
	    return errorMsg.length() > 0 ? errorMsg.toString() : "";
	}
	/**
	 * Validate the mandatory Person, Address, Phone fields for Insured, required to send request transaction to HooperHolmes.
	 * @param olife an OLifE object to be validated
	 * @return String a String containing name of any missing fields
	 */
	protected String validateInsuredInfo(OLifE olife) throws NbaBaseException {
	    // SPR3290 code deleted
		try {
	        ArrayList aReqInfoList = NbaTXLife.getPrimaryHoldingFromOLifE(olife).getPolicy().getRequirementInfo();
	        
	        ArrayList aPartyList = olife.getParty();
	        Party aParty = null;
	        for (int i = 0; i < aReqInfoList.size(); i++) {
	        	RequirementInfo aReqInfo = (RequirementInfo) aReqInfoList.get(i);
	        	boolean physicianPartyObjectIsPresent = false;
	        	boolean isAPS = false;
	        	if(aReqInfo.getReqCode() == 11){
	        		isAPS = true;
	        	}
	        	for (int j = 0; j < aPartyList.size(); j++) {
	        		aParty = (Party) aPartyList.get(i);
					
	        		if(aReqInfo.getAppliesToPartyID().equals(aParty.getId())){ // This is the party for whom this  requirement is for (e.g. the insured).
		        		if(aParty.getPersonOrOrganization().isPerson()){
		        			Person person = aParty.getPersonOrOrganization().getPerson();
		        			if ( person.getFirstName().length() == 0) { 
	    						errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Insured.FirstName ");
						    }
						    if ( person.getLastName().length() == 0) { 
	    						errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Insured.LastName ");
						    }
						    if ( person.getBirthDate() == null) { 
	    						errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Insured.BirthDate");
						    }
		        		}
						// Address
						if ( aParty.getAddressAt(0).getLine1().length() == 0) { 
							errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Insured.Address Line 1 ");
					    }
					    if ( aParty.getAddressAt(0).getCity().length() == 0) { 
							errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Insured.Address City ");
					    }
					    if ( aParty.getAddressAt(0).getAddressStateTC() == -1) {  //NBA093
							errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Insured.Address AddressStateTc ");
					    }	
					    if ( aParty.getAddressAt(0).getZip().length() == 0) { 
							errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Insured.Address Zip ");
					    }
						    
					    // Phone
					    for (int k = 0; k < aParty.getPhoneCount(); k++) {
				            Phone aPhone = aParty.getPhoneAt(k);
			        	    if (aPhone.getPhoneTypeCode() == 1) {
	        			        if (aPhone.getAreaCode().length() == 0) {
	                        		errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Phone.Area Code");
			                	}
						        if (aPhone.getDialNumber().length() == 0) {
		        		            errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Phone.DialNumber");
			                    }
				            }
				            else{
				            	errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Phone");			
				            }
	    				}
	        			
		        	}
		        			        		
	    	    }
	
		        if(isAPS){
		    	   	 for (int j = 0; j < olife.getRelationCount(); j++) {
				           Relation aRelation = olife.getRelationAt(j);
				           if(aRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_PHYSICIAN){
				           		physicianPartyObjectIsPresent = true;
				           		//break;
				           }
		    	   	 }
		    	   	 if(!physicianPartyObjectIsPresent){
		    	 		errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Physician Party Object ");  	 
		    	   	 }	
		    	}
	    	}
	    	} catch (Exception e) {
	    	    errorMsg.append("\nInsured");
		    }
			return errorMsg.length() > 0 ? errorMsg.toString() : "";
	}
	/**
	 * Controls the validation of the XML request to ensure that all required fields are present.
	 * @param txlife An instance of <code>NbaTXLife</code> object to be validated
	 * @throws an NbaBaseException if any fields are missing or invalid
	 */
	protected void validateXmlRequest(NbaTXLife txlife) throws NbaBaseException {
	 
	    	OLifE olife = txlife.getOLifE();
			try{
			    if (NbaTXLife.getPrimaryHoldingFromOLifE(olife).getPolicy().getProductType() < 0) { 
			    	errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Product Type");
			    }
			} catch (Exception e) {
	    	    errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Product Type");
		    }
		    try{
			    if (NbaTXLife.getPrimaryHoldingFromOLifE(olife).getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife().getFaceAmt() == 0) {  //NBA093
			    	errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "FaceAmt");
			    }
			} catch (Exception e) {
	    	    errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "FaceAmt");
		    }
			// RequirementInfo
			ArrayList reqInfoList = NbaTXLife.getPrimaryHoldingFromOLifE(olife).getPolicy().getRequirementInfo();
			if (reqInfoList == null || reqInfoList.size() == 0) { //NBA103 
				errorMsg.append("\nRequirementInfo"); //NBA103
			} //NBA103
			for(int i=0; i<reqInfoList.size(); i++){
				RequirementInfo requirementInfo =  (RequirementInfo)reqInfoList.get(i);
				try{
				    if (requirementInfo.getReqCode() == 0) { 
			    		errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Requirement Code ");
				    }
				} catch (Exception e) {
		    	    errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Requirement Code ");
			    }
				try{
			    	if (requirementInfo.getReferredPartyOrgCode().length() == 0) {
			    		errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Referred Party Org Code ");
				    }
				} catch (Exception e) {
	    		    errorMsg.append(NbaProviderAdapterSchema.FIELD_MISSING + "Referred Party Org Code ");
		    	}
			}
			validateInsuredInfo(olife);
		    //validateRelationPartyInfo(olife);
	}

	/**
	 * Updates the NbaTXLife object with Hooper Holmes required fields
	 * @param txlife An instance of <code>NbaTXLife</code> object to be updated
	 * @throws an NbaBaseException if any error occurs
	 */
	protected void formatRequest(NbaTXLife txlife) throws NbaBaseException{
		OLifE olife = txlife.getOLifE();
		Holding holding = null;
		SourceInfo sourceInfo = null;
		Policy policy = null;
		if(olife != null){
			holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife);
			sourceInfo = olife.getSourceInfo();
		}
		Request request = NbaConfiguration.getInstance().getProviderRequest("HPH",(Long.toString(NbaOliConstants.TC_TYPE_GENREQUIREORDREQ))); //ACN012
		if(sourceInfo != null){
			sourceInfo.setSourceInfoName(request.getSourceInfoName());
		}
		if(holding != null){
			policy = holding.getPolicy();
		}
		if(policy != null){
			for(int i= 0; i<policy.getRequirementInfoCount(); i++){
				RequirementInfo reqInfo = policy.getRequirementInfoAt(i);
				if(reqInfo.getReqStatus() < 0){
					reqInfo.setReqStatus(NbaOliConstants.OLI_REQSTAT_SUBMITTED);	//SPR1601
				}
				if(reqInfo.getRequestedDate() == null){
					reqInfo.setRequestedDate(new SimpleDateFormat("MMddyy").format(new Long(System.currentTimeMillis())));
				}
				if(reqInfo.getReleasePartyOrgCode() == null || reqInfo.getReleasePartyOrgCode().equals("")){
					reqInfo.setReleasePartyOrgCode(request.getConfirmationID()); //ACN012
				}
				if(reqInfo.getReferredPartyOrgCode() == null || reqInfo.getReferredPartyOrgCode().equals("")){
					reqInfo.setReferredPartyOrgCode(getHPHAccountTranslation(String.valueOf(reqInfo.getReqCode()), dst)); 
				}
				
			}		
		}
			
	}	

	/**
	 * Returns the hHrequest.
	 * @return NbaTXLife
	 */
	protected NbaTXLife getHHrequest() {
		return hHrequest;
	}

	/**
	 * Sets the hHrequest.
	 * @param hHrequest The hHrequest to set
	 */
	public void setHHrequest(NbaTXLife newHHrequest) throws NbaBaseException{
		if(hHrequest == null){
			hHrequest = newHHrequest;			
		}else{
			hHrequest.getPrimaryHolding().getPolicy().getRequirementInfo().add(newHHrequest.getPrimaryHolding().getPolicy().getRequirementInfoAt(0));
			// Additional document to be send as Attachment
//NBA103 removed try catch
				File file = new File(NbaConfiguration.getInstance().getFileLocation("hooperAttachmentFile"));
				if (file.exists()) {
					String encodedAttachment = NbaBase64.encodeBytes(NbaUtils.getBytesArrayFromFile(file));
					
					int partyCount = hHrequest.getOLifE().getPartyCount();
					for (int i = 0; i < partyCount; i++) {
						Party aParty = hHrequest.getOLifE().getPartyAt(i);
						if (aParty.getPartyTypeCode() == NbaOliConstants.OLI_PT_PERSON) {
							Attachment aAttachment = new Attachment();
							AttachmentData aAttachmentData = new AttachmentData();
							aAttachmentData.setPCDATA(encodedAttachment);
							aAttachment.setAttachmentData(aAttachmentData);
							aParty.addAttachment(aAttachment);
						}
					}
			}
		}		
	}
	/**
	 * This method returns the Translation of Account passed as parameter
	 * @param account Account code
	 * @param dst NbaDst Object
	 * @return String accountTranslation
	 */
	protected String getHPHAccountTranslation(String account, NbaDst dst) {
		String accountTranslation = null;
		NbaTableAccessor tableAccessor = getTableAccessor();
		try {
			Map hashMap = tableAccessor.createDefaultHashMap(NbaConstants.PROVIDER_HOOPERHOLMES);
			NbaTableData[] tableData = tableAccessor.getDisplayData(hashMap, NbaTableConstants.NBA_REQUIREMENT_ACCOUNT); //ACN014
			for (int i = 0; i < tableData.length; i++) {
				if ((tableData[i].code()).equals(account)) {
					accountTranslation = tableData[i].text();
					return accountTranslation; //ACN014
				}
			}
		} catch (Exception e) {
			getLogger().logException(e); //NBA103
		}
		return null; //ACN014
	}
	/**
	 * Returns a new NbaTableAccessor.
	 * @return A new NbaTableAccessor
	 * @see com.csc.fsg.nba.tableaccess.NbaTableAccessor
	 */
	protected NbaTableAccessor getTableAccessor() {
		return new NbaTableAccessor(); 
	}
	/**
	 * This method controls the processing of the Provider's response and causes the work item
	 * to be updated with data from that response.
	 * @param work the requirement work item
	 * @return the updated requirement work item with formated source and modified LOB fields.
	 * @exception NbaBaseException thrown if an error occurs.
	 */
	//ACN014 Changed signature
	public ArrayList processResponseFromProvider(NbaDst work, NbaUserVO user) throws NbaBaseException {
		List list = work.getNbaSources();
		NbaSource source = null;
		String response = null;
		// get provider response source
		for (int i = 0; i < list.size(); i++) {
			source = (NbaSource) list.get(i);
			if (source.getSource().getSourceType().equals(NbaConstants.A_ST_PROVIDER_SUPPLEMENT)) {
				break;
			} else {
				source = null;
			}
		}
		// check if response source has valid data
		if (source != null) {
			response = source.getText();
			if (response == null || response.trim().length() == 0) {
				throw new NbaBaseException("Provider response source is missing or invalid");
			}
		} else {
			throw new NbaBaseException("Provider response source is missing or invalid");
		}
		//process source
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Response from HOOPER HOLMES : " + response);
		} 
		if (response.trim().length() != 0) { //no error message
			NbaTXLife txLife = null;
			try{
				txLife = new NbaTXLife(response);
			}catch(Exception e){
				throw new NbaBaseException("Could not get NbaTxLife");
			}
			if (getLogger().isDebugEnabled()) {
				getLogger().logDebug("XMLife translation  : " + txLife.toXmlString());
			}
			
			updateWorkItemFromResponse(work, txLife, user); //QC7600 HPH Retrofit
			//begin SPR1863
			for(int i=0; i<getFilenames().size();i++){ 
				byte[] sourceData = getSourceData((String)(getFilenames().get(i)));
				work.addImageSource(work.getNbaTransaction(), NbaConstants.A_ST_PROVIDER_RESULT, sourceData);				
			}
			//end SPR1863
		} 
		//ACN014 Begin
		ArrayList aList = new ArrayList();
		aList.add(work);
		return aList;
		//ACN014 End
	}
	/**
	 * This method updates the provider result source on the workitem with generated
	 * XMLife message. It also updates the workitem LOBs.
	 * 
	 * @param work The work item to be updated
	 * @param txLife an instance of <code>nbaTXLife</code> containing XMLife response message
	 */
	//QC7600 Changed Method Signature HPH retrofit
	protected void updateWorkItemFromResponse(NbaDst work, NbaTXLife txLife, NbaUserVO user) throws NbaBaseException {
		
		NbaLob lob = work.getNbaLob();
		lob.updateLobFromNbaTxLife(txLife);
		OLifE olife = txLife.getOLifE();
		Holding holding = null;
		// SPR3290 code deleted
		Policy policy = null;
		String appliesToPartyId = null;
		if(olife != null){
			holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife);
			//sourceInfo = olife.getSourceInfo();
		}
		if(holding != null){
			policy = holding.getPolicy();
		}
		if(policy != null){
			for(int i= 0; i<policy.getRequirementInfoCount(); i++){
				RequirementInfo reqInfo = policy.getRequirementInfoAt(i);
				if(reqInfo.getReqCode() > 0){
					appliesToPartyId = reqInfo.getAppliesToPartyID();
					lob.setReqType(Integer.parseInt(String.valueOf(reqInfo.getReqCode())));
					lob.setReqReceiptDate(new Date()); //NBA130
					lob.setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(new Date())); //QC20240
				}
			}
			if(lob.getReqType() < 0){
				throw new NbaBaseException("Could not get req code information from response"); //SPR1863
			}
		}else{
			throw new NbaBaseException("Could not get Policy information from response"); //SPR1863
		}
		Relation aRelation = null;
		int relationCount = olife.getRelationCount();
		for(int i=0; i<relationCount; i++){
			if(olife.getRelationAt(i).getRelatedObjectID().equals(appliesToPartyId)){
				aRelation = olife.getRelationAt(i);
				break;
			}
		}
		if(aRelation != null){
			lob.setReqPersonCode(Integer.parseInt(String.valueOf(aRelation.getRelationRoleCode())));
			if(aRelation.getRelatedRefID() != null){
				lob.setReqPersonSeq(Integer.parseInt(aRelation.getRelatedRefID()));
			}
		}else{
			throw new NbaBaseException("Could not get Relation information from response"); //SPR1863
		}
		
		//begin SPR1863
		NbaParty nbaParty = txLife.getParty(appliesToPartyId);
		if (nbaParty != null) {
			lob.setFirstName(nbaParty.getFirstName());
			lob.setLastName(nbaParty.getLastName());
			lob.setMiddleInitial(nbaParty.getMiddleInitial());
			lob.setSsnTin(nbaParty.getSSN());
			Long temp = nbaParty.getTaxId();
			if (temp != null) {
				lob.setTaxIdType(temp.intValue());
			}
		}else{
			throw new NbaBaseException("Could not get Party information from response");
		}
		if(lob.getReqType() == NbaOliConstants.OLI_REQCODE_MEDEXAMPARAMED ||
				lob.getReqType() == NbaOliConstants.OLI_REQCODE_MEDEXAMMD ||
				lob.getReqType() == NbaOliConstants.OLI_REQCODE_803){ //ALII2006
			lob.setParamedSignDate(getParamedSignDate(txLife, lob, txLife.getPolicy().getRequirementInfoAt(0), user)); //QC7600 HPH retrofit
		}
		Party party = nbaParty.getParty();
		for(int i=0; i<party.getAttachmentCount();i++){
			Attachment attachment = party.getAttachmentAt(i);
			if(attachment.getAttachmentType() == NbaOliConstants.OLI_ATTACH_DOC){
				addFilename(attachment.getDescription());
			}
		}		
		//end SPR1863
	}
	
	/**
	 * This method parse the batch response from MIB into individual response stream.
	 * 
	 * @param work com.csc.fsg.nba.vo.NbaDst
	 * @return java.util.Map
	 */
	public Map parseBatchResponse(NbaDst work) throws NbaBaseException {
		//SPR1863 comments deleted	
		return null;
	}	
	
	/**
	 * This method retrieves the data from the file found in the EMSI index file.
	 * @param The file name 
	 * @return the data from the file
	 */
	//SPR1863 New Method
	protected byte[] getSourceData(String filename) throws NbaBaseException {
		try {
			String path = NbaConfiguration.getInstance().getFileLocation("HOOPERRIP");
			Serializable parms[] = { NbaConstants.S_FUNC_FILE_READ, path + filename };
			NbaCommunicator proComm = new NbaCommunicator();
			ObjectInputStream in = proComm.postObjectsToServlet(parms); // Execute the servlet transaction
			Object retObj = in.readObject(); // Read and use the result value
			if (retObj instanceof NbaBaseException) {
				in.close();
				throw (NbaBaseException) retObj;
			} else if (retObj instanceof Throwable) {
				in.close();
				throw new NbaBaseException(NbaBaseException.INVOKE_SERVER, (Throwable) retObj);
			}
			in.close();
			byte[] results = (byte[]) retObj;
			return results;
		} catch (NbaBaseException nbe) {
			throw nbe;
		} catch (Throwable t) {
			throw new NbaBaseException(NbaBaseException.INVOKE_SERVER, t);
		}
	}

	/**
	 * Get attachment file name list
	 * @return
	 */
	//SPR1863 New Method
	public List getFilenames() {
		return filenames;
	}

	/**
	 * Add the attachment file name to the list
	 * @param string
	 */
	//SPR1863 New Method
	public void addFilename(String string) {
		filenames.add(string);
	}

	//QC7600 New Method HPH Retrofit
	protected Date getParamedSignDate(NbaTXLife txLife, NbaLob lob, RequirementInfo reqInfo, NbaUserVO user) throws NbaBaseException {
		if (txLife != null && txLife.getOLifE() != null) {
			HashMap skipMap = new HashMap();
			skipMap.put(NbaVpmsConstants.A_PROCESS_ID, NbaUtils.getBusinessProcessId(user));
			skipMap.put(NbaVpmsConstants.A_REQUEST, NbaVpmsAdaptor.EP_GET_COMPLETION_DATE_LOCATION);
			skipMap.put("A_ReqTypeLOB", String.valueOf(reqInfo.getReqCode()));
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(lob);
			oinkData.setContractSource(txLife);
			NbaOinkRequest oinkRequest = new NbaOinkRequest();
			oinkRequest.setRequirementIdFilter(reqInfo.getId());
			NbaVpmsAdaptor vpmsAdaptor = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REQUIREMENTS);
			vpmsAdaptor.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_COMPLETION_DATE_LOCATION);
			vpmsAdaptor.setANbaOinkRequest(oinkRequest);
			vpmsAdaptor.setSkipAttributesMap(skipMap);
			try {
				VpmsComputeResult computeResult = vpmsAdaptor.getResults();
				NbaVpmsModelResult resultsData = new NbaVpmsModelResult(computeResult.getResult());
				String location = null;
				ResultData resultData;
				for (int i = 0; i < resultsData.getVpmsModelResult().getResultDataCount(); i++) {
					resultData = resultsData.getVpmsModelResult().getResultDataAt(i);
					for (int j = 0; j < resultData.getResultCount(); j++) {
						location = resultData.getResultAt(j);
						if (location.equals("RequirementInfo.RequestedScheduleDate")) {
							if (reqInfo.hasRequestedScheduleDate()) {
								return reqInfo.getRequestedScheduleDate();
							}
						} else if (location.equals("RequirementInfo.StatusEvent.StatusEventDate")) {
							StatusEvent event = null;
							for (int k = 0; k < reqInfo.getStatusEventCount(); k++) {
								event = reqInfo.getStatusEventAt(k);
								if (event.getStatusEventCode() == NbaOliConstants.OLI_STATEVENTCD_COMPLETEASSCHED && event.hasStatusEventDate()) {
									return event.getStatusEventDate();
								}
							}
						} else if (location.equals("Party.Risk.MedicalExam.ExamDate")) {
							Party party = txLife.getPrimaryParty().getParty();
							MedicalExam medExam = null;
							for (int k = 0; party.hasRisk() && k < party.getRisk().getMedicalExamCount(); k++) {
								medExam = party.getRisk().getMedicalExamAt(k);
								if (medExam.hasExamDate()) {
									return medExam.getExamDate();
								}
							}
						} else if (location.equals("Party.Risk.LabTesting.TestUpdateDate")) {
							Party party = txLife.getPrimaryParty().getParty();
							if (party.hasRisk()) {
								RiskExtension riskExt = NbaUtils.getFirstRiskExtension(party.getRisk());
								if (riskExt != null && riskExt.hasLabTesting() && riskExt.getLabTesting().hasTestUpdateDate()) {
									return riskExt.getLabTesting().getTestUpdateDate();
								}
							}
						}
					}
				}
			} catch (java.rmi.RemoteException re) {
				throw new NbaBaseException("VPMS problem", re);
			} finally {
				if (vpmsAdaptor != null) {
					try {
						vpmsAdaptor.remove();
					} catch (RemoteException e) {
						getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
					}
				}
			}
		}
		return null;
	}
}
