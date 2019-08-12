
package com.csc.fsg.nba.bean.accessors;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.csc.fsg.nba.database.NbaConnectionManager;
import com.csc.fsg.nba.database.NbaDatabaseUtils;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.Criteria;
import com.csc.fsg.nba.vo.txlife.CriteriaExpression;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;

/**
 * Retrieve the contract data for formal and Informal policies based on search criteria.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.35</td><td>AXA Life Phase 1</td><td>nbA Web Services</td></tr>
 * <tr><td>ALPC7</td><td>Version 7</td><td>Schema migration from 2.8.90 to 2.9.03</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 */

public class NbaRetrievePolicy {
    
    
    protected static NbaLogger logger = null;
    
    

    
    /**
     * Retrieves the list of agent formal policy based on search criteria. Search Criteria is populated by iterating through TXLife.
     * 
     * @param nbaTXLife
     * @return NbaTXLife
     * @throws NbaBaseException
     */
    public NbaTXLife retrieveAgentFormalInformalPolicy(NbaTXLife nbaTXLife) throws NbaBaseException {
        Date startTime = Calendar.getInstance().getTime();
        Connection conn = null;
        PreparedStatement stmt = null;
        NbaTXLife life = null;
        try {
            conn = NbaConnectionManager.borrowConnection(NbaConfigurationConstants.NBAPEND);
            conn.setAutoCommit(true); // APSL4508
            StringBuffer searchQuery = new StringBuffer();
            String searchCriteria = getSearchCriteria(nbaTXLife);
            List webserviceData = new ArrayList();
            List companyProducerIdList = new ArrayList();
            searchQuery.append(getQueryString());
            searchQuery.append(searchCriteria);
            searchQuery.append(" ORDER BY P.CONTRACTKEY ");
            getLogger().logDebug(" SQL Query : Agent Informal Search : " + searchQuery.toString());
            stmt = conn.prepareStatement(searchQuery.toString());
            ResultSet rs = stmt.executeQuery();
            Nba302WebServiceData data = null;
            while (rs.next()) {
                data = new Nba302WebServiceData();
				data.setFormaAppliedInd(rs.getLong(1));
				data.setApplicationType(rs.getLong(2));
				data.setInformalApproval(rs.getLong(3));
				companyProducerIdList.add(rs.getString(4));
				data.setCarrierAdminSystem(rs.getString(5));
				data.setPolicyNumber(rs.getString(6));
				data.setCarrierCode(rs.getString(7));
				data.setPlanName(rs.getString(8));
				data.setPolicyStatus(rs.getLong(9));
				data.setProductCode(rs.getString(10));
				
				if (webserviceData.size() > 0) {
					Nba302WebServiceData lastInsertedData = (Nba302WebServiceData) webserviceData.get(webserviceData.size() - 1);
					if (lastInsertedData.getPolicyNumber().equals(data.getPolicyNumber())) {
						lastInsertedData.getCompanyProducerIdList().add(rs.getString(4));
					} else {
						data.setCompanyProducerIdList(companyProducerIdList);
						webserviceData.add(data);
						companyProducerIdList = new ArrayList();
					}
				} else {
					data.setCompanyProducerIdList(companyProducerIdList);
					webserviceData.add(data);
					companyProducerIdList = new ArrayList();
								
				}
                

            }
            life = createTXLife302Response(webserviceData, getMaximumRecordCount(nbaTXLife), getStartRecordNumber(nbaTXLife, webserviceData.size()),
                    NbaOliConstants.TC_SUBTYPE_AGENT_SEARCH);
        } catch (Exception ex) {
            getLogger().logException("During search for Agent Formal/Informal policices in pending database ", ex);
            throw new NbaBaseException(ex);
        } finally {
            NbaDatabaseUtils.closeStatement(stmt, startTime, "SELECT for Agent Formal/Informal Policies ");
            NbaDatabaseUtils.logElapsedTime(startTime);
            NbaDatabaseUtils.returnDBconnection(conn, NbaConfigurationConstants.NBAAUXILIARY);
        }

        return life;

    }

    /**
     * Retrieves the list of formal and informal policies based on search criteria. Search Criteria is populated by iterating through TXLife.
     * 
     * @param nbaTXLife
     * @return NbaTXLife
     * @throws NbaBaseException
     */
    public NbaTXLife retrieveFormalInformalPolicy(NbaTXLife nbaTXLife) throws NbaBaseException {
        Date startTime = Calendar.getInstance().getTime();
        Connection conn = null;
        PreparedStatement stmt = null;
        NbaTXLife life = null;
        try {
            conn = NbaConnectionManager.borrowConnection(NbaConfigurationConstants.NBAPEND);
            conn.setAutoCommit(true);// APSL4508
            StringBuffer searchQuery = new StringBuffer();
            String searchCriteria = getSearchCriteria(nbaTXLife);
            List webserviceData = new ArrayList();
            List companyProducerIdList = new ArrayList();
            searchQuery.append(getFormalInformalQueryString());
            searchQuery.append(searchCriteria);
            searchQuery.append(" ORDER BY P.CONTRACTKEY");
            getLogger().logDebug(" SQL Query : Formal/Informal Search : " + searchQuery.toString());
            stmt = conn.prepareStatement(searchQuery.toString());
            ResultSet rs = stmt.executeQuery();
            Nba302WebServiceData data = null;
            while (rs.next()) {
				data = new Nba302WebServiceData();
				data.setFormaAppliedInd(rs.getLong(1));
				data.setApplicationType(rs.getLong(2));
				data.setInformalApproval(rs.getLong(3));
				companyProducerIdList.add(rs.getString(4));
				data.setCarrierAdminSystem(rs.getString(5));
				data.setPolicyNumber(rs.getString(6));
				data.setCarrierCode(rs.getString(7));
				data.setPlanName(rs.getString(8));
				data.setPolicyStatus(rs.getLong(9));
				data.setProductCode(rs.getString(10));
				
				if (webserviceData.size() > 0) {
					Nba302WebServiceData lastInsertedData = (Nba302WebServiceData) webserviceData.get(webserviceData.size() - 1);
					if (lastInsertedData.getPolicyNumber().equals(data.getPolicyNumber())) {
						lastInsertedData.getCompanyProducerIdList().add(rs.getString(4));
					} else {
						data.setCompanyProducerIdList(companyProducerIdList);
						webserviceData.add(data);
					}
				} else {
					data.setCompanyProducerIdList(companyProducerIdList);
					webserviceData.add(data);
				}
				companyProducerIdList = new ArrayList();
            }
            life = createTXLife302Response(webserviceData, getMaximumRecordCount(nbaTXLife), getStartRecordNumber(nbaTXLife, webserviceData.size()),
                    NbaOliConstants.TC_SUBTYPE_STATUS_DATE_SEARCH);
        } catch (Exception ex) {
            getLogger().logException("During search for Formal/Informal policices in pending database ", ex);
            throw new NbaBaseException(ex);
        } finally {
            NbaDatabaseUtils.closeStatement(stmt, startTime, "SELECT for Formal/Informal Policies ");
            NbaDatabaseUtils.logElapsedTime(startTime);
            NbaDatabaseUtils.returnDBconnection(conn, NbaConfigurationConstants.NBAAUXILIARY);
        }

        return life;
    }

  
    /**
     * Return <code>NbaLogger</code> implementation (e.g. NbaLogService).
     * 
     * @return com.csc.fsg.nba.foundation.NbaLogger
     */
    protected static NbaLogger getLogger() {
        if (logger == null) {
            try {
                logger = NbaLogFactory.getLogger(NbaRetrievePolicy.class.getName());
            } catch (Exception e) {
                NbaBootLogger.log(NbaRetrievePolicy.class.getName() + " could not get a logger from the factory.");
                e.printStackTrace(System.out);
            }
        }
        return logger;
    }
    
    /**
     * Transverse through the TXLife and retrieve the search parameters. The method loop through each criteria expression and identify the operator,
     * Criteria and operation.
     * 
     * @param nbaTXLife
     * @return String
     */
    protected String getSearchCriteria(NbaTXLife nbaTXLife) throws NbaBaseException {
        StringBuffer searchCriteria = new StringBuffer();
        if (nbaTXLife != null) {
            if (nbaTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest() != null) {
                CriteriaExpression criteriaExpression = nbaTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0)
                        .getCriteriaExpression();
                if (criteriaExpression != null) {
                    if (criteriaExpression.isCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension()) {//ALPC7
                        for (int i = 0; i < criteriaExpression.getCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension()//ALPC7
                                .getCriteriaOrCriteriaExpressionCount(); i++) {
                            if (criteriaExpression.getCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension().getCriteriaOrCriteriaExpressionAt(i)
                                    .isCriteriaExpression()) {//ALPC7
                                CriteriaExpression expression = criteriaExpression.getCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension()
                                        .getCriteriaOrCriteriaExpressionAt(i).getCriteriaExpression();//ALPC7
                                for (int j = 0; j < expression.getCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension()
                                        .getCriteriaOrCriteriaExpressionCount(); j++) {//ALPC7
                                    Criteria criteria = expression.getCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension()
                                            .getCriteriaOrCriteriaExpressionAt(j).getCriteria();//ALPC7
                                    searchCriteria.append(getTableName((int) criteria.getObjectType()));
                                    searchCriteria.append(criteria.getPropertyName());
                                    searchCriteria.append(getOperation((int) criteria.getOperation()));
                                    if (criteria.getPropertyName().toUpperCase().indexOf("DATE")>0) {
                                        searchCriteria.append(" TO_DATE(");
                                        searchCriteria.append("'");
                                        searchCriteria.append(criteria.getPropertyValue().getPCDATA());
                                        searchCriteria.append("'");
                                        searchCriteria.append(" , 'YYYY-MM-DD')");
                                    } else {
                                        searchCriteria.append("'");
                                        searchCriteria.append(criteria.getPropertyValue().getPCDATA());
                                        searchCriteria.append("'");

                                    }

                                    if (j < (expression.getCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension().getCriteriaOrCriteriaExpressionCount() - 1)) {//ALPC7
                                        searchCriteria.append(getOperator((int) expression.getCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension()
                                                .getCriteriaOperator())
                                                + " ");//ALPC7
                                    }
                                }
                                if (i < (criteriaExpression.getCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension()
                                        .getCriteriaOrCriteriaExpressionCount() - 1)) {//ALPC7
                                    searchCriteria.append(getOperator((int) criteriaExpression.getCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension()
                                            .getCriteriaOperator())
                                            + " ");//ALPC7
                                }
                            } else if (criteriaExpression.getCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension().getCriteriaOrCriteriaExpressionAt(i)
                                    .isCriteria()) {//ALPC7
                                Criteria criteria = criteriaExpression.getCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension()
                                        .getCriteriaOrCriteriaExpressionAt(i).getCriteria();//ALPC7
                                searchCriteria.append(getTableName((int) criteria.getObjectType()));
                                searchCriteria.append(criteria.getPropertyName());
                                searchCriteria.append(getOperation((int) criteria.getOperation()));
                                searchCriteria.append("'");
                                searchCriteria.append(criteria.getPropertyValue().getPCDATA());
                                searchCriteria.append("'");
                                if (i < (criteriaExpression.getCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension()
                                        .getCriteriaOrCriteriaExpressionCount() - 1)) {//ALPC7
                                    searchCriteria.append(getOperator((int) criteriaExpression.getCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension()
                                            .getCriteriaOperator())
                                            + " ");//ALPC7
                                }
                            }
                        }
                    }
                }
            }
        }

        return searchCriteria.toString();

    }
        

   
   /**
    * Update the TXlife response for the maximum number of records in the response.
    * 
    * @param nbaTXLife
    * @param webserviceData
    * @param maxRecord
    * @param startRecord
    */
    private void updateTXLifeResponse(NbaTXLife nbaTXLife, List webserviceData, int maxRecord, int startRecord, long transSubType) {
        TXLifeResponse txLifeResp = nbaTXLife.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0);
        txLifeResp.setTransSubType(transSubType);
        txLifeResp.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
        txLifeResp.setInquiryLevel(1);
        TransResult transResult = txLifeResp.getTransResult();
        if (transResult == null)
            transResult = new TransResult();
        transResult.setResultCode(2);
        OLifE olifE = new OLifE();
        int size = webserviceData.size();
        transResult.setRecordsFound(size);
        if (maxRecord > 0 && maxRecord < size) { //Check for correct MaxRecord value
            if (startRecord == 0) {
                txLifeResp.setNextRecord(maxRecord + 1);
                size = maxRecord;
            } else if ((maxRecord + startRecord) <= size) {
                txLifeResp.setNextRecord(((maxRecord + startRecord + 1) <= size) ? maxRecord + startRecord + 1 : 1);
                size = maxRecord + startRecord;
            } else {
                txLifeResp.setNextRecord(1);
            }
        } else {
            txLifeResp.setNextRecord(1);
        }

        for (int i = startRecord; i < size; i++) {
            Nba302WebServiceData data = (Nba302WebServiceData) webserviceData.get(i);
            olifE.addHolding(addHolding(data, i));
            olifE.addParty(addParty(data, i));
            olifE.addRelation(addRelation(data, i));
        }

        txLifeResp.setOLifE(olifE);

    }
   
   /**
    * Creating Holding object
    * 
    * @param webserviceData
    * @param i
    * @return
    */
    private Holding addHolding(Nba302WebServiceData webserviceData, int i) {
        Holding holding = new Holding();
        holding.setId("Holding_" + (i + 1));
        holding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);

        Policy policy = new Policy();
        policy.setId("Policy_" + (i + 1));
        policy.setCarrierAdminSystem(webserviceData.getCarrierAdminSystem());
        policy.setPolNumber(webserviceData.getPolicyNumber());
        policy.setCarrierCode(webserviceData.getCarrierCode());
        policy.setPlanName(webserviceData.getPlanName());
        policy.setPolicyStatus(webserviceData.getPolicyStatus());
        policy.setProductCode(webserviceData.getProductCode());

        ApplicationInfo applInfo = new ApplicationInfo();
        applInfo.setApplicationType(webserviceData.getApplicationType());
        applInfo.setFormalAppInd(webserviceData.getFormaAppliedInd() == 0 ? false : true);

        ArrayList oLifeExtList = new ArrayList();
        OLifEExtension olifeExt = new OLifEExtension();
        olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_APPLICATIONINFO);
        ApplicationInfoExtension applicationInfoExtension = new ApplicationInfoExtension();
        applicationInfoExtension.setInformalAppApproval(webserviceData.getInformalApproval());
        olifeExt.setApplicationInfoExtension(applicationInfoExtension);
        oLifeExtList.add(olifeExt);

        applInfo.setOLifEExtension(oLifeExtList);
        policy.setApplicationInfo(applInfo);
        holding.setPolicy(policy);
        return holding;
    }
        
   /**
    * Creating party object
    * 
    * @param webserviceData
    * @param i
    * @return
    */
	private Party addParty(Nba302WebServiceData webserviceData, int i) {
		Party party = new Party();
		party.setId("Party_" + (i + 1));
		party.setPartyTypeCode(1);
		Person person = new Person();
		PersonOrOrganization personOrOrganization = new PersonOrOrganization();
		personOrOrganization.setPerson(person);
		party.setPersonOrOrganization(personOrOrganization);

		Producer producer = new Producer();
		CarrierAppointment appointment = null;
		for (int j = 0; j < webserviceData.getCompanyProducerIdList().size(); j++) {
			appointment = new CarrierAppointment();
			appointment.setId("CarrierAppointment_" + (j + 1));
			appointment.setPartyID(party.getId());
			appointment.setCompanyProducerID((String) webserviceData.getCompanyProducerIdList().get(j));
			appointment.setCarrierCode(webserviceData.getCarrierCode());
			producer.addCarrierAppointment(appointment);
		}


		party.setProducer(producer);
		return party;
	}

    /**
	 * Creating Relation object
	 * 
	 * @param webserviceData
	 * @param i
	 * @return
	 */
    private Relation addRelation(Nba302WebServiceData webserviceData, int i) {
        Relation relation = null;
        List agentIdlist = webserviceData.getCompanyProducerIdList();
        if (agentIdlist != null && agentIdlist.size() > 0 && !NbaUtils.isBlankOrNull(agentIdlist.get(0)) ) {
            relation = new Relation();
            relation.setId("Relation_" + (i + 1));
            relation.setOriginatingObjectID("Holding_" + (i + 1));
            relation.setRelatedObjectID("Party_" + (i + 1));
            relation.setRelationRoleCode(NbaOliConstants.OLI_REL_PRIMAGENT);// role code for primary writing agent.
        }
        return relation;
    }

    /**
     * Create the TxLife reponse from search results.
     * @param webserviceData
     * @param maxRecord
     * @param startRecord
     * @return
     */
    private NbaTXLife createTXLife302Response(List webserviceData, int maxRecord, int startRecord,long transSubType) throws Exception {
        NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
        nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGSRCH);
        nbaTXRequest.setBusinessProcess("NBWSUSER");
        NbaTXLife txLife = new NbaTXLife(nbaTXRequest);
        txLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
        txLife = new NbaTXLife(txLife);
        updateTXLifeResponse(txLife, webserviceData, maxRecord, startRecord,transSubType);
        txLife = new NbaTXLife(NbaUtils.getTxlifeWithEmptyTagsRem(txLife.toXmlString()));
        return txLife;
    }

    /**
     * Retrieve the Maximum record count from input TxLife request.
     * @param nbaTXLife
     * @return
     */
    private int getMaximumRecordCount(NbaTXLife nbaTXLife) {
        if (nbaTXLife != null) {
            if (nbaTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest() != null
                    && nbaTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequest().size() > 0) {
                int maxRecord = nbaTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getMaxRecords();
                return (maxRecord > 0) ? maxRecord : 0; //Default max record value is 0
            }
        }
        return 0;

    }

    /**
     * Retrieve the Start Record value from input TxLife request.
     * @param nbaTXLife
     * @param resultDataSize
     * @return
     */
    private int getStartRecordNumber(NbaTXLife nbaTXLife, int resultDataSize) {
        if (nbaTXLife != null) {
            if (nbaTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest() != null
                    && nbaTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequest().size() > 0) {
                int startRecord = nbaTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getStartRecord();
                if (startRecord < resultDataSize) {
                    return (startRecord > 1) ? (startRecord - 1) : 0; //Default Start Record value is 0
                }
            }
        }
        return 0;

    }

    /**
     * Formats a Date's date for use in the response node.
     * 
     * @param rawDate
     *            the date/time in Java internal format.
     * @return String the date as a properly formatted String.
     */
    protected String formatDate(Date rawDate) {
        SimpleDateFormat dateFormat;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(rawDate);
        return formattedDate;
    }

    /**
     * Formats a Date's time for use in the response node.
     * 
     * @param rawTime
     *            the date/time in Java internal format.
     * @return String the time as a properly formatted String.
     */
    protected String formatTime(Date rawTime) {
        SimpleDateFormat dateFormat;
        dateFormat = new SimpleDateFormat("HH:mm:ss");
        long zoneOffset = (new GregorianCalendar()).get(Calendar.ZONE_OFFSET);
        DecimalFormat hoursFormat = new DecimalFormat("+00;-00");
        DecimalFormat minutesFormat = new DecimalFormat("00");
        String formattedTime = dateFormat.format(rawTime) + hoursFormat.format(zoneOffset / 3600000) + ":"
                + minutesFormat.format((zoneOffset / 60000) % 60);
        return formattedTime;
    }

    /**
     * Convert the operation value to corresponsding operator.
     * @param operation
     * @return String
     */
    protected String getOperation(int operation) {
        String operator = "";
        switch (operation) {
        case 1:
            operator = " = ";
            break;
        case 2:
            operator = " != ";
            break;
        case 3:
            operator = " < ";
            break;
        case 4:
            operator = " > ";
            break;
        case 5:
            operator = " <= ";
            break;
        case 6:
            operator = " >= ";
            break;
        case 7:
            operator = " LIKE ";
            break;
        case 8:
            operator = " NOT LIKE ";
            break;
        }

        return operator;
    }

    /**
     * Convert the operation code to database operator
     * @param operation
     * @return String
     */
    protected String getOperator(int operation) {
        String operator = "";
        switch (operation) {
        case 1:
            operator = " OR ";
            break;
        case 2:
            operator = " AND ";
            break;
        case 3:
            operator = " NOT ";
            break;

        }

        return operator;
    }

    /**
     * Convert table Code to table name alias
     * @param tableCode
     * @return String
     */
    protected String getTableName(int tableCode) {
        String tableName = "";
        switch (tableCode) {
        case 58:
            tableName = " AI.";
            break;
        case 18:
            tableName = " P.";
            break;
        case 11:
            tableName = " CA.";
            break;
        case 8:
            tableName = " R.";
            break;
        case 4:
            tableName = " H.";
            break;

        case 1000500058:
            tableName = " AIX.";
            break;
        }

        return tableName;
    }
    
    /**
     * Default query String
     * @return String
     */
    private String getQueryString (){
        StringBuffer searchQuery = new StringBuffer();
        searchQuery.append("SELECT DISTINCT AI.FORMALAPPIND,AI.APPLICATIONTYPE, AIX.INFORMALAPPAPPROVAL, CA.COMPANYPRODUCERID, P.CARRIERADMINSYSTEM, P.CONTRACTKEY, P.CARRIERCODE, P.PLANNAME, P.POLICYSTATUS, P.PRODUCTCODE, R.RELATIONROLECODE "); 
        searchQuery.append("FROM HOLDING H , POLICY P, PARTY PR, APPLICATIONINFO AI, APPLICATIONINFOEXTENSION AIX, CARRIERAPPOINTMENT CA, RELATION R WHERE  ");
        searchQuery.append("H.ID = P.PARENTIDKEY AND H.CONTRACTKEY = P.CONTRACTKEY AND H.COMPANYKEY = P.COMPANYKEY AND H.BACKENDKEY = P.BACKENDKEY AND ");
        searchQuery.append("P.ID = AI.PARENTIDKEY AND P.CONTRACTKEY = AI.CONTRACTKEY AND P.COMPANYKEY = AI.COMPANYKEY AND P.BACKENDKEY = AI.BACKENDKEY AND "); 
        searchQuery.append("P.ID = AIX.PARENTIDKEY AND P.CONTRACTKEY = AIX.CONTRACTKEY AND P.COMPANYKEY = AIX.COMPANYKEY AND P.BACKENDKEY = AIX.BACKENDKEY AND "); 
   	    searchQuery.append("PR.ID = CA.PARENTIDKEY AND PR.CONTRACTKEY = CA.CONTRACTKEY AND PR.COMPANYKEY = CA.COMPANYKEY AND PR.BACKENDKEY = CA.BACKENDKEY AND ");
   	    searchQuery.append("R.CONTRACTKEY = PR.CONTRACTKEY AND R.COMPANYKEY = PR.COMPANYKEY AND R.BACKENDKEY = PR.BACKENDKEY AND R.RELATEDOBJECTID = PR.ID AND ");
   	    searchQuery.append("R.CONTRACTKEY = H.CONTRACTKEY AND R.COMPANYKEY = H.COMPANYKEY AND R.BACKENDKEY = H.BACKENDKEY AND R.ORIGINATINGOBJECTID = H.ID AND  ");
   	    return searchQuery.toString();
        
    }

    /**
     * Default query String
     * @return String
     */
    private String getFormalInformalQueryString (){
        StringBuffer searchQuery = new StringBuffer();
        searchQuery.append("SELECT DISTINCT AI.FORMALAPPIND,AI.APPLICATIONTYPE, AIX.INFORMALAPPAPPROVAL, PA.COMPANYPRODUCERID, P.CARRIERADMINSYSTEM, ");
        searchQuery.append("P.CONTRACTKEY, P.CARRIERCODE, P.PLANNAME, P.POLICYSTATUS, P.PRODUCTCODE ");
        searchQuery.append("FROM HOLDING H , POLICY P, PARTY PR, APPLICATIONINFO AI, APPLICATIONINFOEXTENSION AIX, "); 
        searchQuery.append(" (SELECT DISTINCT CA.PARENTIDKEY PARENTIDKEY, CA.CONTRACTKEY CONTRACTKEY, ");
        searchQuery.append("  CA.COMPANYKEY COMPANYKEY, CA.BACKENDKEY BACKENDKEY, CA.COMPANYPRODUCERID COMPANYPRODUCERID "); 
        searchQuery.append("FROM CARRIERAPPOINTMENT CA, RELATION R ");
        searchQuery.append("WHERE R.CONTRACTKEY = CA.CONTRACTKEY  AND R.COMPANYKEY = CA.COMPANYKEY  AND R.BACKENDKEY = CA.BACKENDKEY AND ");
        searchQuery.append("	  R.RELATIONROLECODE = 37) PA ");
        searchQuery.append("WHERE H.ID = P.PARENTIDKEY AND H.CONTRACTKEY = P.CONTRACTKEY AND H.COMPANYKEY = P.COMPANYKEY AND "); 
        searchQuery.append("H.BACKENDKEY = P.BACKENDKEY AND P.ID = AI.PARENTIDKEY AND P.CONTRACTKEY = AI.CONTRACTKEY AND "); 
        searchQuery.append("P.COMPANYKEY = AI.COMPANYKEY AND P.BACKENDKEY = AI.BACKENDKEY AND P.ID = AIX.PARENTIDKEY AND ");
        searchQuery.append("P.CONTRACTKEY = AIX.CONTRACTKEY AND P.COMPANYKEY = AIX.COMPANYKEY AND P.BACKENDKEY = AIX.BACKENDKEY AND "); 
        searchQuery.append("P.CONTRACTKEY  = PA.CONTRACTKEY (+) AND ");
        searchQuery.append("P.COMPANYKEY = PA.COMPANYKEY (+) AND P.BACKENDKEY = PA.BACKENDKEY (+) AND ");
   	    return searchQuery.toString();
        
    }
    
}
