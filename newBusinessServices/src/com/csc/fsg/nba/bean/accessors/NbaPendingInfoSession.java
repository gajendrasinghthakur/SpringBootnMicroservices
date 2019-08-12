package com.csc.fsg.nba.bean.accessors;

/*
 *
 *******************************************************************************<BR>
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
 * 
 *******************************************************************************<BR>
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.tableaccess.NbaNbpTable;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.nbaschema.NbpAgents;
import com.csc.fsg.nba.vo.nbaschema.NbpInsureds;
import com.csc.fsg.nba.vo.nbaschema.NbpParties;
import com.csc.fsg.nba.vo.nbaschema.PendingInfo;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.Relation;


/**
 * This is a stateless Session Bean that is used by nbA processes to update nbProducer tables.
 * it uses OINK classes to retrieve data from TXLiferesponse object
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBP001</td><td>Version 3</td><td>nbProducer Initial Development</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR1882</td><td>Version 5</td><td>Plan name is being removed from nbP pending index display</td></tr>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr> 
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */

public class NbaPendingInfoSession{

	// SPR3290 code deleted

	// code deleted NBA201  
	//	private static final java.util.Vector insurableRoles = new Vector();

	/**
	 * This method is for updating the nbPendingInfo database
	 * @param nbaHolding com.csc.fsg.nba.vo.NbaTXLife
	 * @param workflowQueue the queue name
	 */
	//NBA103
	public void update(NbaTXLife holdingInq, String workflowQueue) throws NbaBaseException {
		try {//NBA103
		    // begin NBA201
			// SPR3290 code deleted
			
			List doList = new ArrayList();
	
			// retrieve data for nbp_pendinginfo from holding 
			NbaOinkDataAccess aNbaOinkDataAccess = new NbaOinkDataAccess(holdingInq);
			processPendingInfo(holdingInq, aNbaOinkDataAccess, doList, workflowQueue);
		
			try {
			    processParties(holdingInq, aNbaOinkDataAccess, doList);
			} catch (Exception e) {
				throw new NbaBaseException("Error in extracting parties data from TXLife response", e);
			}
			
			NbaNbpTable nbpTable = new NbaNbpTable();
			nbpTable.update(doList);
			// end NBA201
		} catch (NbaBaseException e) {//NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
			throw e;//NBA103
		} catch (Throwable t) {//NBA103
			NbaBaseException e = new NbaBaseException(t);//NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
			throw e;//NBA103
		}
	}
	// code deleted NBA201
	/**
     * process the Pending Info VO. Using OINK it fetches information to populate PendingInfo object 
     * @param holdingInq
     * @param nbaOinkDataAccess
     * @param doList
     * @param workflowQueue
     */
	// New Method NBA201
    private void processPendingInfo(NbaTXLife holdingInq, NbaOinkDataAccess aNbaOinkDataAccess, List doList, String workflowQueue) throws NbaBaseException {
		NbaOinkRequest aNbaOinkRequest = new NbaOinkRequest();
		PendingInfo pendingInfo = new PendingInfo();
		
		String value = null;
		aNbaOinkRequest.setVariable("PolNumber");
		value = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
        pendingInfo.setPolicyNumber(value);

		aNbaOinkRequest.setVariable("CarrierCode");
		value = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
        pendingInfo.setCarrierCode(value);

		aNbaOinkRequest.setVariable("ProductType");
		value = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
		if (Long.parseLong(value) == NbaOliConstants.OLI_PRODTYPE_ANN || Long.parseLong(value) == NbaOliConstants.OLI_PRODTYPE_VAR) {
		// its Annuity 
			aNbaOinkRequest.setVariable("PolicyValue"); //This value is not required for Annuity
			value = "";
		} else { // if its non-Annuity
			aNbaOinkRequest.setVariable("FaceAmt");
			value = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
		}
		if (value == null || (value.trim()).equals("")) {
			value = "0";
		} 
		pendingInfo.setPolicyValue(new Double(value).doubleValue());

		aNbaOinkRequest.setVariable("PendingContractStatus");
		value = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
		pendingInfo.setPendingContractStatusCode(value);
		
		aNbaOinkRequest.setTableTranslations(true);
		value = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
		pendingInfo.setPendingContractStatusDescription(value);

		aNbaOinkRequest.setTableTranslations(false);
		aNbaOinkRequest.setVariable("LineOfBusiness");
		value = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
		if (value == null || (value.trim()).equals("")) {
			value = "";
		}
		pendingInfo.setLineOfBusiness(value);

		pendingInfo.setLastUnderwritingActivityDate(new java.sql.Date(System.currentTimeMillis()));

		aNbaOinkRequest.setVariable("PolicyProductCode"); 
		value = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
		pendingInfo.setPlanNameCode(value);

		aNbaOinkRequest.setTableTranslations(true);
		value = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
		pendingInfo.setPlanNameDescription(value);

		pendingInfo.setWorkflowStatusCode(workflowQueue);
		
		NbaTableAccessor tableAccessor = new NbaTableAccessor();
		value = tableAccessor.getTranslationString(NbaTableAccessConstants.AWD_QUEUE_TYPE, workflowQueue);
		pendingInfo.setWorkflowStatusDescription(value);

		value = holdingInq.getOLifE().getSourceInfo().getFileControlID();
		if (value == null) {
			value = "";
		}
		pendingInfo.setSystemId(value);
        doList.add(pendingInfo);
    }


	/**
	 * This method processes the parties for populating the NbpAgents, NbpParties and NbpInsureds list 
	 * @param txlife
	 * @param aNbaOinkDataAccess
	 * @param doList
	 * @throws NbaBaseException
	 */
//  New Method NBA201
    protected void processParties(NbaTXLife txlife, NbaOinkDataAccess aNbaOinkDataAccess, List doList) throws NbaBaseException {
        int i = 0;
        Map totalDataMap = new HashMap();
        // process Agents
        processAgents(txlife, doList);        
        long id = System.currentTimeMillis();
        
        NbpParties nbpParties = null;
        NbpInsureds nbpInsureds = null;
        int count = getPartyCount("INS", aNbaOinkDataAccess);
        for (i = 0; i < count; i++) {
            id = id + 1;            
            nbpInsureds = (NbpInsureds) getNbpPartyValues("INS", aNbaOinkDataAccess, i, totalDataMap, id);
            if (nbpInsureds != null) {
                doList.add(nbpInsureds);
            }
        }

        count = getPartyCount("ANN", aNbaOinkDataAccess);
        for (i = 0; i < count; i++) {
            id = id + 1;
            nbpInsureds = (NbpInsureds) getNbpPartyValues("ANN", aNbaOinkDataAccess, i, totalDataMap, id);
            if (nbpInsureds != null) {
                doList.add(nbpInsureds);
            }
        }

        count = getPartyCount("BEN", aNbaOinkDataAccess);
        for (i = 0; i < count; i++) {
            id = id + 1; 
            nbpParties = getNbpPartyValues("BEN", aNbaOinkDataAccess, i, totalDataMap, id);
            if (nbpParties != null) {
                doList.add(nbpParties);
            }
        }

        count = getPartyCount("OWN", aNbaOinkDataAccess);
        for (i = 0; i < count; i++) {
            id = id + 1; 
            nbpParties = getNbpPartyValues("OWN", aNbaOinkDataAccess, i, totalDataMap, id);
            if (nbpParties != null) {
                doList.add(nbpParties);
            }
        }

        count = getPartyCount("PYR", aNbaOinkDataAccess);
        for (i = 0; i < count; i++) {
            id = id + 1; 
            nbpParties = getNbpPartyValues("PYR", aNbaOinkDataAccess, i, totalDataMap, id);
            if (nbpParties != null) {
                doList.add(nbpParties);
            }
        }
        
        count = getPartyCount("SPS", aNbaOinkDataAccess);
        for (i = 0; i < count; i++) {
            id = id + 1; 
            nbpParties = getNbpPartyValues("SPS", aNbaOinkDataAccess, i, totalDataMap, id);
            if (nbpParties != null) {
                doList.add(nbpParties);
            }
        }
    }

    /**
     * Depending on the type of party it creates a different Party object and populates the fields of these 
     * objects using the OINK variables. All the individual Party objects are added to the totalDataMap passed.
     * It calls the isPresent method to check if the same party is not already present in the totalDataMap and if
     * not it adds the party to the map.
     * as an input.
     * @param persString
     * @param aNbaOinkDataAccess
     * @param partyFilter
     * @param totalDataMap
     * @param id
     * @return NbpParties
     * @throws NbaBaseException
     */
//  New Method NBA201    
    protected NbpParties getNbpPartyValues(String persString, NbaOinkDataAccess aNbaOinkDataAccess, int partyFilter, Map totalDataMap, long id) throws NbaBaseException {
        NbpParties nbpParties = null;
        NbpInsureds nbpInsureds = null;
        NbaOinkRequest aNbaOinkRequest = new NbaOinkRequest();
        String value = null;
        aNbaOinkRequest.setPartyFilter(partyFilter);
        
        if (!isPresent(persString, aNbaOinkDataAccess, totalDataMap, aNbaOinkRequest)) {
            if (persString.equalsIgnoreCase("INS") || persString.equalsIgnoreCase("ANN")) {
                nbpInsureds = new NbpInsureds();
                nbpParties = nbpInsureds;
            } else {
                nbpParties = new NbpParties();    
            }
	        aNbaOinkRequest.setVariable("GovtID_" + persString);
			value = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
			if (value == null || value.length() == 0) {
			    value = "0";
			}
			nbpParties.setGovtId(value);
			nbpParties.setId(id);
			
	        aNbaOinkRequest.setVariable("CompanyProducerID_" + persString);
			value = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
			if (value == null || value.length() == 0) {
			    value = "0";
			}
			nbpParties.setCompanyProducerID(value);

			aNbaOinkRequest.setVariable("RelationRoleCode_" + persString);
			value = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
			if (value == null || value.length() == 0) {
			    value = "0";
			}
			nbpParties.setRelationRoleCode(new Integer(value));
	
			aNbaOinkRequest.setVariable("FullName_" + persString);
			value = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
			if (value == null) {
			    value = "";
			}
			nbpParties.setFullName(value);
			
			aNbaOinkRequest.setVariable("FirstName_" + persString);
			value = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
			if (value == null) {
			    value = "";
			}
			nbpParties.setFirstName(value);
	
			aNbaOinkRequest.setVariable("MiddleName_" + persString);
			value = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
			if (value == null) {
			    value = "";
			}
			nbpParties.setMiddleName(value);
			
			aNbaOinkRequest.setVariable("LastName_" + persString);
			value = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
			if (value == null) {
			    value = "";
			}
			nbpParties.setLastName(value);
			totalDataMap.put(nbpParties.getGovtId(), nbpParties);
        }        
        return nbpParties;
    }

    /**
     * This method checks if the person/party is present or not in the totalDataMap passed as an input.
     * It first checks on the basis of GovtId and if it is null it accesses it on the basis of 
     * CompanyProducerId. If either of these ids are present in the map then it returns true else false
     * @param persString
     * @param nbaOinkDataAccess
     * @param totalDataMap
     * @param nbaOinkRequest
     * @return
     */
//  New Method NBA201
    private boolean isPresent(String persString, NbaOinkDataAccess aNbaOinkDataAccess, Map totalDataMap, NbaOinkRequest aNbaOinkRequest) throws NbaBaseException {
        boolean present = false;
		if (persString.equalsIgnoreCase("BEN") || persString.equalsIgnoreCase("CBN") || persString.equalsIgnoreCase("OWN") 
				|| persString.equalsIgnoreCase("POWN") || persString.equalsIgnoreCase("PYR") || persString.equalsIgnoreCase("SPS")) {
		    aNbaOinkRequest.setVariable("GovtID_" + persString);
			String id = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
			if (totalDataMap.containsKey(id)) {
			    present = true;
			} else {
			    aNbaOinkRequest.setVariable("CompanyProducerID_" + persString);
				id = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
				if (totalDataMap.containsKey(id)) {
				    present = true;
				}
			} 
		}
        return present;
    }

    /**
     * Processes the Agents to populate the NbpAgents object using OINK
     * @param txlife
     * @param totalDataMap
     */
//  New Method NBA201
    protected void processAgents(NbaTXLife txlife, List doList) {
        OLifE olife = txlife.getOLifE();
		int relCount = olife.getRelationCount();
		int count = 0;
		NbpAgents agents = null;
		for (int rel = 0; rel < relCount; rel++) {
			Relation relation = olife.getRelationAt(rel);
			if (relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_PRIMAGENT
				|| relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_ADDWRITINGAGENT
				|| relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_ASSIGNAGENT
				|| relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_SUPERIORAGENT) {
					
				count = count + 1;
				
				agents = new NbpAgents();
				Party aParty = txlife.getParty(relation.getRelatedObjectID()).getParty();
				if (aParty.getProducer().getCarrierAppointmentAt(0).hasCompanyProducerID()) {
					agents.setCompanyProducerID(aParty.getProducer().getCarrierAppointmentAt(0).getCompanyProducerID());
				} else {
				    agents.setCompanyProducerID("");
				}
				Object obj = aParty.getPersonOrOrganization$Contents();
				String fullName = "";
				
				if ((obj != null) && (obj instanceof Person)) {
					Person aPerson = aParty.getPersonOrOrganization().getPerson();
					StringBuffer sb = new StringBuffer();
					if (aPerson.hasLastName()) {
						sb.append(aPerson.getLastName());
						agents.setLastName(aPerson.getLastName());
						sb.append(", ");
					} else {
					    agents.setLastName("");
					}
					if (aPerson.hasFirstName()) {
						sb.append(aPerson.getFirstName());
						agents.setFirstName(aPerson.getFirstName());
						sb.append(" ");
					} else {
						agents.setFirstName("");
					}
					if (aPerson.hasMiddleName()) {
						sb.append(aPerson.getMiddleName());
						agents.setMiddleName(aPerson.getMiddleName());
						sb.append(" ");
					} else {
						agents.setMiddleName("");
					}
					if (aPerson.hasSuffix() && aPerson.getSuffix().length() > 0) {
						sb.append(", ");
						sb.append(aPerson.getSuffix());
					}
					fullName = sb.toString();
					agents.setFullName(fullName);
				} 
				
				if (relation.hasInterestPercent()) {
					agents.setInterestPercent(new Float(relation.getInterestPercent()).floatValue());
				} else {
					agents.setInterestPercent(0);
				}
				
				agents.setRelationRoleCode(new Integer(String.valueOf(relation.getRelationRoleCode())));
				doList.add(agents);			
			}
			
		}
    }

	
	/**
	 * Updates  nbp pendingInfo database with the work flow status.
	 * @param polNumber policy number
	 * @param backendID system ID
	 * @param companyCode company Code
	 * @param workflowStatus queue Name
	 * @exception NbaBaseException
	 */
	//NBA103
	public void updateWorkflowStatus(String polNumber, String backendId, String companyCode, String workflowQueue) throws NbaBaseException {
		try {//NBA103
			String workflowQueueDesc=null;
			try {
				NbaTableAccessor tableAccessor = new NbaTableAccessor();
				workflowQueueDesc = tableAccessor.getTranslationString(NbaTableAccessConstants.AWD_QUEUE_TYPE, workflowQueue);
			} catch (Exception exp) {
				workflowQueueDesc = workflowQueue;
			}
			NbaNbpTable nbaNbpTable = new NbaNbpTable();
	        nbaNbpTable.updateWorkflowStatus(polNumber,backendId,companyCode, workflowQueue, workflowQueueDesc, new java.sql.Date(System.currentTimeMillis()));
		} catch (NbaBaseException e) {//NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
			throw e;//NBA103
		} catch (Throwable t) {//NBA103
			NbaBaseException e = new NbaBaseException(t);//NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
			throw e;//NBA103
		}
	}
	
	/**
	 * This method calls getValues method to fetch agents, insureds and other parties 
	 * data from TXLife object
	 * @param persString String 
	 * @param aNbaOinkDataAccess com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess
	 * @return java.util.HashMap
	 */
	protected int getPartyCount(String persString, NbaOinkDataAccess aNbaOinkDataAccess) throws NbaBaseException {
		int count = 0;
		try {
			NbaOinkRequest aNbaOinkRequest = new NbaOinkRequest();
			aNbaOinkRequest.setVariable("Count_" + persString);
			
			String countString = aNbaOinkDataAccess.getStringValueFor(aNbaOinkRequest);
			count = (new Integer(countString)).intValue();
			
			
		} catch (NbaBaseException e) {
			throw new NbaBaseException("ERROR IN RETRIEVING PARTY COUNT", e);
		}
		return count;
		
	}

}
