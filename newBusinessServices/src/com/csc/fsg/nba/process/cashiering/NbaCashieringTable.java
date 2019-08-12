package com.csc.fsg.nba.process.cashiering;

/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which are
 * proprietary to CSC Financial Services Group�.  The use, reproduction,
 * distribution or disclosure of this program, in whole or in part, without
 * the express written permission of CSC Financial Services Group is prohibited.
 * This program is also an unpublished work protected under the copyright laws
 * of the United States of America and other countries.
 *
 * If this program becomes published, the following notice shall apply:
 *    Property of Computer Sciences Corporation.<BR>
 *    Confidential. Not for publication.<BR>
 *    Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.csc.fsg.nba.database.NbaConnectionManager;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaServiceLocator;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaBundleData;
import com.csc.fsg.nba.tableaccess.NbaCheckData;
import com.csc.fsg.nba.tableaccess.NbaContractCheckData;
import com.csc.fsg.nba.tableaccess.NbaContractsWireTransferData;
import com.csc.fsg.nba.tableaccess.NbaCreditCardData;
import com.csc.fsg.nba.tableaccess.NbaDepositTicketData;
import com.csc.fsg.nba.tableaccess.NbaTable;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.NbaCashBundleVO;
import com.csc.fsg.nba.vo.NbaCashCheckVO;
import com.csc.fsg.nba.vo.NbaCashCompanyVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaLockBundleVO;
import com.csc.fsg.nba.vo.configuration.Cashiering;
import com.csc.fsg.nba.vo.configuration.DatabaseConnection;

/**
 * This class controls the connections and other database related functionality for the nbA Cashiering database tables.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA009</td><td>Version 2</td><td>Cashiering</td></tr>
 * <tr><td>SPR1191</td><td>Version 2</td><td>Closing Rejected Bundles</td></tr>
 * <tr><td>SPR1171</td><td>Version 2</td><td>Secondary Contracts for Wire Transfers</td></tr>
 * <tr><td>NBA027</td><td>Version 3</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>NBA069</td><td>Version 3</td><td>Cashiering Enhancement</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>NBA068</td><td>Version 3</td><td>Inforce Payment</td></tr>
 * <tr><td>SPR1779</td><td>Version 4</td><td>Correct parsing of database type</td></tr>
 * <tr><td>SPR1726</td><td>Version 4</td><td>NBA_CHECK_DEPOSIT_ACCOUNTING extracts are not updated after Deposit Ticket Correction</td></tr>
 * <tr><td>SPR1808</td><td>Version 4</td><td>Create new database NBAAUXILIARY</td></tr>
 * <tr><td>ACN012</td><td>Version 3</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA115</td><td>Version 5</td><td>Credit card payment and authorization</td></tr>
 * <tr><td>NBA182</td><td>Version 7</td><td>Cashiering Credit card Rewrite</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>NBA228</td><td>Version 8</td><td>Cash Management Enhancement</td></tr>  
 * <tr><td>AXAL3.7.23</td><td>AXA Life phase 1</td><td>Accounting Interface</td></tr>  
 * <tr><td>APSL634</td><td>AXA Life phase 1</td><td>PERF - Database connection leak in Cashiering Workbench</td></tr>
 * <tr><td>SR615900</td><td>Discretionary</td><td>Prevent Checks From Being Deposited When CWA Not Applied</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */
public class NbaCashieringTable extends NbaTableHelper { //NBA044

    private static DataSource ds = null; //SPR1808


    private static NbaLogger logger;

    //NBA044 code moved to <code>NbaTableHelper</code>

    private String depositInd = NbaConfigurationConstants.DEPOSIT_INCLUDE; //default - use Include indicator, ACN012

    // configuration
    public final static String CASH_TECHNICAL_KEY = "CASH_TECHNICAL_KEY";

    public final static String CASH_BUNDLE_KEY = "CASH_BUNDLE_KEY";

    // column names
    public final static String AMOUNT = "AMOUNT";

    public final static String AMOUNT_APPLIED = "AMOUNT_APPLIED";

    public final static String APPLIED_IND = "APPLIED_IND";

    public final static String BACKEND_SYSTEM = "BACKEND_SYSTEM";

    public final static String BUNDLE_ID = "BUNDLE_ID";

    public final static String BUNDLES = "BUNDLES"; //NBA182

    public final static String CHECK_AMT = "CHECK_AMT";

    public final static String CHECK_DATE = "CHECK_DATE";

    public final static String CHECK_COUNT = "CHECK_COUNT";

    public final static String CHECK_NUMBER = "CHECK_NUMBER";

    public final static String CLOSE_TIME = "CLOSE_TIME"; //SPR1191

    public final static String COMPANY = "COMPANY";
    
    public final static String LOCATION_ID = "LOCATION_ID";//NBA228
    
    public final static String CONTRACT_NUMBER = "CONTRACT_NUMBER";

    public final static String CREDIT_CARD_COMPANY = "CREDIT_CARD_COMPANY"; //NBA115

    public final static String DEPOSIT_TIME = "DEPOSIT_TIME";
    public final static String DRAFT_TIME = "DEPOSIT_TIMEDRAFT";//NBA228   

    public final static String DEPOSIT_USER = "DEPOSIT_USER";

    public final static String INCLUDE_IND = "INCLUDE_IND";

    public final static String INSTITUTION_ID = "INSTITUTION_ID";

    public final static String PRIMARY_CONTRACT_IND = "PRIMARY_CONTRACT_IND";

    public final static String PRIMARY_INSURED_NAME = "PRIMARY_INSURED_NAME";

    public final static String PRODUCT_TYPE = "PRODUCT_TYPE";

    public final static String REFUSED_IND = "REFUSED_IND"; //NBA115

    public final static String REFUND_IND = "REFUND_IND"; //NBA115

    public final static String REFUND_FAIL_IND = "REFUND_FAIL_IND"; //NBA115

    public final static String REJECT_IND = "REJECT_IND";

    public final static String REPORT_IND = "REPORT_IND";

    public final static String RETURNED_IND = "RETURNED_IND";

    public final static String SCAN_STATION_ID = "SCAN_STATION_ID";

    public final static String SEED_NUMBER = "SEED_NUMBER";

    public final static String SEQUENCE = "SEQUENCE";

    public final static String SEQUENCE_NAME = "SEQUENCE_NAME";

    public final static String SOURCE_CREATE_TIME = "SOURCE_CREATE_TIME";

    public final static String TECHNICAL_KEY = "TECHNICAL_KEY";

    public final static String TOTAL_AMT = "TOTAL_AMT";

    public final static String WIRE_TRANSFER_EFFDATE = "WIRE_TRANSFER_EFFDATE";

    //Begin NBA115
    public final static String TRANSACTION_ID = "TRANSACTION_ID";

    public final static String CREDIT_CARD_NUMBER = "CREDIT_CARD_NUMBER";

    public final static String CHARGE_DATE = "CHARGE_DATE";

    public final static String INFORCE_IND = "INFORCE_IND";

    public final static String ACCOUNTING_IND = "ACCOUNTING_IND";

    public final static String DEPOSIT_DATE = "DEPOSIT_DATE";

    //End NBA115

    //Begin NBA069
    public final static String CORRECTION_IND = "CORRECTION_IND";

    public final static String REVISED_CHECK_AMT = "REVISED_CHECK_AMT";

    public final static String CORRECTION_TIME = "CORRECTION_TIME";

    public final static String USER_ID = "USER_ID";

    public final static String REVISED_DEPOSIT_TIME = "REVISED_DEPOSIT_TIME";

    public final static String REVISED_DEPOSIT_BUNDLE_TOTAL = "REVISED_DEPOSIT_BUNDLE_TOTAL";
    public final static String DISTRIBUTION_CHANNEL = "DISTRIBUTIONCHANNEL"; //APSL3410

    //End NBA069
    public final static String INFORCE_PAYMENT_IND = "INFORCE_PAYMENT_IND";//NBA068
    public final static String CLOSE_BUNDLE = "CLOSE_BUNDLE"; //NBA228 
    public final static String TRANSLATION = "TRANSLATION"; //NBA228
    public final static String CHECK_LAST_NAME = "CHECK_LAST_NAME"; //NBA228
    public final static String CHECK_PAYMENT_FORM = "PAYMENT_FORM"; //AXAL3.7.23
    public final static String PRODUCT_CODE= "PRODUCT_CODE"; //AXAL3.7.23
    public final static String SYSTEM_APPLIED_IND = "SYSTEM_APPLIED_IND"; // SR615900
    public final static String RESCANNED_IND = "RESCANNED_IND"; // APSL4513
    public final static String BUNDLE_CREATE_TIME = "BUNDLE_CREATE_TIME"; // APSL4513

    //NBA044 code deleted
    /**
     * NbaCashieringTable constructor.
     */
    public NbaCashieringTable() throws NbaBaseException {
        super();

        Cashiering config = NbaConfiguration.getInstance().getCashiering(); // ACN012
        DatabaseConnection configDB = NbaConfiguration.getInstance().getDatabaseConnection(NbaConfiguration.CASHIERING); // NBA050, ACN012

        depositInd = config.getDepositEligibility();
        setUser(configDB.getUserId()); //NBA044, ACN012
        setPassword(configDB.getPassword()); //NBA044

        if (getDataSource() == null) { //NBA044
            NbaServiceLocator sl = NbaServiceLocator.getInstance();
            setDataSource((DataSource) sl.lookup(configDB.getDataSource())); //NBA044
        }
        //begin NBA093
        String driverClass = configDB.getDriverClass().toUpperCase();
        if (driverClass.indexOf("ORACLE") >= 0) { // SPR1779
            setDbType(ORACLE);
        } else if (driverClass.indexOf("SQLSERVER") >= 0) { // SPR1779
            setDbType(SQL_SERVER);
        }
        //end NBA093
    }

    /**
     * Removes the specified bundle so that no new checks can be assigned to it.
     * @param company company code
     * @param bundleID bundle identification
     * @throws NbaDataAccessException          
     */
    public void closeBundle(String company, String bundleID) throws NbaDataAccessException {

        StringBuffer query = new StringBuffer("DELETE FROM NBA_BUNDLES");

        query.append(" WHERE " + formatSQLWhereCriterion(COMPANY, company));
        query.append(" AND " + formatSQLWhereCriterion(BUNDLE_ID, bundleID));

        executeDelete(query.toString());
    }

    /**
     * Close all rejected checks in a bundle.
     * @param company company code
     * @param bundleID bundle identification
     * @param closeTime bundle close timestamp
     * @throws NbaDataAccessException          
     */
    public void closeChecks(String company, String bundleID, java.util.Date closeTime, String scanStationId) throws NbaDataAccessException { // APSL4624

        StringBuffer query = new StringBuffer("UPDATE NBA_CHECKS SET ");

        query.append(formatSQLUpdateValue(DEPOSIT_TIME, closeTime));
        query.append(" WHERE " + formatSQLWhereCriterion(DEPOSIT_TIME, (java.util.Date) null));
        query.append(" AND " + TECHNICAL_KEY);
        query.append(" IN (SELECT DISTINCT TECHNICAL_KEY FROM NBA_CONTRACTS_CHECKS WHERE ");
        query.append(formatSQLWhereCriterion(COMPANY, company));
        query.append(" AND " + formatSQLWhereCriterion(BUNDLE_ID, bundleID));
        // APSL4624 Begin
        query.append(" AND (" + formatSQLWhereCriterion(REJECT_IND, true));
        query.append(" OR " + formatSQLWhereCriterion(RETURNED_IND, true));
        if (!NbaConstants.SCAN_STATION_EAPPACH.equals(scanStationId)) {
        	query.append(" OR " + formatSQLWhereCriterion(RESCANNED_IND, true));        	
        }
        query.append("))");
        // APSL4624 End

        executeUpdate(query.toString());
    }

    //NBA044 generic methods moved to <code>NbaTableHelper</code>
    /**
     * Retrieve check information for checks in a bundle that are available for deposit.
     * 
     * @param bundleID
     *            bundle identification
     * @return com.csc.fsg.nba.tableaccess.NbaCheckData[]
     */
    public NbaCheckData[] getBundleCheckDataOpenForDeposit(String bundleID) throws NbaBaseException, NbaDataAccessException {

        StringBuffer query = new StringBuffer(
                "SELECT TECHNICAL_KEY, BUNDLE_ID, INCLUDE_IND, DEPOSIT_TIME, CHECK_AMT, CHECK_DATE, CHECK_NUMBER, SCAN_STATION_ID, SOURCE_CREATE_TIME FROM NBA_CHECKS, NBA_CONTRACTS_CHECKS");

        query.append(" WHERE " + formatSQLWhereCriterion("nba_checks.BUNDLE_ID", bundleID));
        query.append(" AND " + formatSQLWhereCriterion("nba_contracts_checks.reject_ind", false));
        if (getDepositInd().compareToIgnoreCase(NbaConfigurationConstants.DEPOSIT_APPLY) == 0) { // ACN012
            query.append(" AND " + formatSQLWhereCriterion("nba_contracts_checks.applied_ind", true));
        } else {
            query.append(" AND " + formatSQLWhereCriterion("nba_checks.include_ind", true));
        }
        query.append(" AND nba_checks.deposit_time is null");

        List aList = new ArrayList(10); // SPR3290
        try {
            ResultSet rs = executeQuery(query.toString());
            while (rs.next()) {
                aList.add(new NbaCheckData(rs));
            }
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally {
            close();
        }

        return ((NbaCheckData[]) aList.toArray(new NbaCheckData[aList.size()]));
    }

    /**
     * Return a bundle ID. If one does not already exist, a new one will be created.
     * 
     * @param scanStationID
     *            scan station identification
     * @param company
     *            company
     * @return java.lang.String
     */
    public String getBundleID(String scanStationID, String company) throws NbaBaseException, NbaDataAccessException {

        StringBuffer query = new StringBuffer("SELECT BUNDLE_ID FROM NBA_BUNDLES WHERE ");
        query.append(formatSQLWhereCriterion(SCAN_STATION_ID, scanStationID));
        query.append(" AND " + formatSQLWhereCriterion(COMPANY, company));

        try {
            ResultSet rs = executeQuery(query.toString());
            if (rs.next()) {
                return (rs.getString(BUNDLE_ID).trim());
            } else {
                long seedNbr = getSeedNumber(CASH_BUNDLE_KEY);

                DecimalFormat dFormat = new DecimalFormat("0000000000");
                String bundleID = dFormat.format(seedNbr);

                // APSL4513 Begin Code refactored 
                StringBuffer insertQuery = new StringBuffer("INSERT INTO NBA_BUNDLES (SCAN_STATION_ID, COMPANY, BUNDLE_ID, BUNDLE_CREATE_TIME) VALUES (");
                insertQuery.append(formatSQLValue(scanStationID));
                insertQuery.append(", " + formatSQLValue(company));
                insertQuery.append(", " + formatSQLValue(bundleID));
                insertQuery.append(", " + formatSQLValue(new Date(), getDbType()));
                insertQuery.append(")");
                executeInsert(insertQuery.toString());
                // APSL4513 End
                
                return (bundleID);
            }
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally { //PERF-APSL634
        	close(); //PERF-APSL634
        }  //PERF-APSL634
    }

    /**
     * Returns an array of Bundles that are available for deposit.
     * 
     * @return com.csc.fsg.nba.vo.NbaCashCheckVO
     */
    public NbaCashCheckVO[] getBundlesOpenForDeposit() throws NbaBaseException, NbaDataAccessException {

        StringBuffer query = new StringBuffer(
                "SELECT nba_checks.technical_key, nba_checks.bundle_id, nba_checks.include_ind, nba_checks.check_amt, nba_checks.revised_check_amt, nba_contracts_checks.company, nba_contracts_checks.applied_ind from nba_checks, nba_contracts_checks");//NBA069

        query.append(" WHERE nba_checks.technical_key = nba_contracts_checks.technical_key AND nba_checks.deposit_time is null");
        //SPR1191 lines deleted
        List aList = new ArrayList(10); // SPR3290
        try {
            ResultSet rs = executeQuery(query.toString());
            while (rs.next()) {
                NbaCashCheckVO cashCheck = new NbaCashCheckVO();
                cashCheck.setTechnicalKey(rs.getLong(TECHNICAL_KEY));
                cashCheck.setBundleID(NbaUtils.trim(rs.getString(BUNDLE_ID)));
                cashCheck.setIncludedInd(stringToBoolean(rs.getString(INCLUDE_IND)));
                cashCheck.setCheckAmount(rs.getBigDecimal(CHECK_AMT));
                cashCheck.setCompany(NbaUtils.trim(rs.getString(COMPANY)));
                cashCheck.setAppliedInd(stringToBoolean(rs.getString(APPLIED_IND)));
                cashCheck.setRevisedCheckAmt(rs.getBigDecimal(REVISED_CHECK_AMT));//NBA069
                aList.add(cashCheck);
            }
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally {
            close();
        }

        return ((NbaCashCheckVO[]) aList.toArray(new NbaCashCheckVO[aList.size()]));
    }

    /**
     * Retrieve the check information.
     * 
     * @param checkAmt
     *            check amount
     * @param checkDate
     *            date check was written
     * @param checkNumber
     *            check number
     * @param scanStationID
     *            scan station identification
     * @param sourceCreateTime
     *            timestamp when check was scanned into nbA
     * @return com.csc.fsg.nba.tableaccess.NbaCheckData
     */
    public NbaCheckData getCheckData(double checkAmt, java.util.Date checkDate, String checkNumber, String scanStationID,
            java.util.Date sourceCreateTime) throws NbaBaseException, NbaDataAccessException {

        NbaCheckData nbaCheckData = null;
        StringBuffer query = new StringBuffer(
				"SELECT TECHNICAL_KEY, BUNDLE_ID, INCLUDE_IND, DEPOSIT_TIME, CHECK_AMT, CHECK_DATE, CHECK_NUMBER, SCAN_STATION_ID, SOURCE_CREATE_TIME, PAYMENT_FORM FROM NBA_CHECKS"); //AXAL3.7.23
        query.append(" WHERE " + formatSQLWhereCriterion(CHECK_AMT, new BigDecimal(Double.toString(checkAmt))));
        query.append(" AND " + formatSQLWhereCriterion(CHECK_DATE, checkDate));
        query.append(" AND " + formatSQLWhereCriterion(CHECK_NUMBER, checkNumber));
        query.append(" AND " + formatSQLWhereCriterion(SCAN_STATION_ID, scanStationID));
        query.append(" AND " + formatSQLWhereCriterion(SOURCE_CREATE_TIME, sourceCreateTime));

        try {
            ResultSet rs = executeQuery(query.toString());
            if (rs.next()) {
                nbaCheckData = new NbaCheckData(rs);
            }
           //PERF-APSL634 code deleted
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally { //PERF-APSL634
        	close(); //PERF-APSL634
        }  //PERF-APSL634
        return (nbaCheckData);
    }

    /**
     * Retrieve the check information.
     * 
     * @param technicalKey
     *            long
     * @return com.csc.fsg.nba.tableaccess.NbaCheckData
     */
    public NbaCheckData getCheckData(long technicalKey) throws NbaBaseException, NbaDataAccessException {

        NbaCheckData nbaCheckData = null;
        StringBuffer query = new StringBuffer(
                "SELECT TECHNICAL_KEY, BUNDLE_ID, INCLUDE_IND, DEPOSIT_TIME, CHECK_AMT, CHECK_DATE, CHECK_NUMBER, SCAN_STATION_ID, SOURCE_CREATE_TIME, PAYMENT_FORM  FROM NBA_CHECKS");  //AXAL3.7.23
        query.append(" WHERE " + formatSQLWhereCriterion(TECHNICAL_KEY, technicalKey));

        try {
            ResultSet rs = executeQuery(query.toString());
            if (rs.next()) {
                nbaCheckData = new NbaCheckData(rs);
            }
            //PERF-APSL634 code deleted
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally { //PERF-APSL634
        	close(); //PERF-APSL634
        }  //PERF-APSL634
        return (nbaCheckData);
    }

    /**
     * Retrieve a collection of contract information for a check.
     * 
     * @param technicalKey
     *            long
     * @return com.csc.fsg.nba.tableaccess.NbaContractCheckData[]
     */
    public NbaContractCheckData[] getContractCheckData(long technicalKey) throws NbaBaseException, NbaDataAccessException {

        // NBA182 deleted code
        StringBuffer query = new StringBuffer(
                "SELECT BUNDLE_ID, COMPANY, CONTRACT_NUMBER, TECHNICAL_KEY, PRIMARY_CONTRACT_IND, PRODUCT_TYPE, APPLIED_IND, REJECT_IND, AMOUNT_APPLIED, PRIMARY_INSURED_NAME, BACKEND_SYSTEM FROM NBA_CONTRACTS_CHECKS");

        query.append(" WHERE " + formatSQLWhereCriterion(TECHNICAL_KEY, technicalKey));

        List aList = new ArrayList(10); // SPR3290
        try {
            ResultSet rs = executeQuery(query.toString());
            while (rs.next()) {
                aList.add(new NbaContractCheckData(rs));
            }
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally {
            close();
        }

        return ((NbaContractCheckData[]) aList.toArray(new NbaContractCheckData[aList.size()]));
    }

    /**
     * Retrieve the contract information for a check from the database.
     * 
     * @param bundleID
     *            Bundle Identification
     * @param company
     *            Company Code
     * @param contractNumber
     *            Contract Number
     * @param checkAmt
     *            Check Amount
     * @param checkNumber
     *            Check Number
     * @param applyAmt
     *            Apply Amount
     * @return com.csc.fsg.nba.tableaccess.NbaContractCheckData
     */
    public NbaContractCheckData getContractCheckData(String bundleID, String company, String contractNumber, double checkAmt, String checkNumber,
            double applyAmt) throws NbaBaseException, NbaDataAccessException {

        NbaContractCheckData nbaContractCheckData = null;
        StringBuffer query = new StringBuffer(
                "SELECT NBA_CONTRACTS_CHECKS.BUNDLE_ID, COMPANY, CONTRACT_NUMBER, NBA_CONTRACTS_CHECKS.TECHNICAL_KEY, PRIMARY_CONTRACT_IND, PRODUCT_TYPE, APPLIED_IND, REJECT_IND, AMOUNT_APPLIED, PRIMARY_INSURED_NAME, BACKEND_SYSTEM FROM NBA_CONTRACTS_CHECKS, NBA_CHECKS");

        query.append(" WHERE " + formatSQLWhereCriterion("NBA_CONTRACTS_CHECKS.BUNDLE_ID", bundleID));
        query.append(" AND " + formatSQLWhereCriterion(COMPANY, company));
        query.append(" AND " + formatSQLWhereCriterion(CONTRACT_NUMBER, contractNumber));
        query.append(" AND " + formatSQLWhereCriterion(CHECK_AMT, new BigDecimal(Double.toString(checkAmt))));
        query.append(" AND " + formatSQLWhereCriterion(CHECK_NUMBER, checkNumber));
        query.append(" AND " + formatSQLWhereCriterion(AMOUNT_APPLIED, new BigDecimal(Double.toString(applyAmt))));
        query.append(" AND NBA_CONTRACTS_CHECKS.TECHNICAL_KEY = NBA_CHECKS.TECHNICAL_KEY");

        try {
            ResultSet rs = executeQuery(query.toString());
            if (rs.next()) {
                nbaContractCheckData = new NbaContractCheckData(rs);
            }
            //PERF-APSL634 code deleted
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally { //PERF-APSL634
        	close(); //PERF-APSL634
        }  //PERF-APSL634

        return (nbaContractCheckData);
    }

    /**
     * Retrieve the contract information for a check from the database.
     * 
     * @param bundleID
     *            Bundle Identification
     * @param company
     *            Company Code
     * @param contractNumber
     *            Contract Number
     * @param technicalKey
     *            Technical Key
     * @return com.csc.fsg.nba.tableaccess.NbaContractCheckData
     */
    public NbaContractCheckData getContractCheckData(String bundleID, String company, String contractNumber, long technicalKey)
            throws NbaBaseException, NbaDataAccessException {

        NbaContractCheckData nbaContractCheckData = null;
        StringBuffer query = new StringBuffer(
                "SELECT BUNDLE_ID, COMPANY, CONTRACT_NUMBER, TECHNICAL_KEY, PRIMARY_CONTRACT_IND, PRODUCT_TYPE, APPLIED_IND, REJECT_IND, AMOUNT_APPLIED, PRIMARY_INSURED_NAME, BACKEND_SYSTEM FROM NBA_CONTRACTS_CHECKS");

        query.append(" WHERE " + formatSQLWhereCriterion(BUNDLE_ID, bundleID));
        query.append(" AND " + formatSQLWhereCriterion(COMPANY, company));
        query.append(" AND " + formatSQLWhereCriterion(CONTRACT_NUMBER, contractNumber));
        query.append(" AND " + formatSQLWhereCriterion(TECHNICAL_KEY, technicalKey));

        try {
            ResultSet rs = executeQuery(query.toString());
            if (rs.next()) {
                nbaContractCheckData = new NbaContractCheckData(rs);
            }
            //PERF-APSL634 code deleted
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally { //PERF-APSL634
        	close(); //PERF-APSL634
        }  //PERF-APSL634

        return (nbaContractCheckData);
    }

    /**
     * Returns the configured deposit indicator. Should be a value of 'Include' or 'Apply'. The default deposit indicator is 'Include'.
     * 
     * @return java.lang.String
     */
    public String getDepositInd() {
        return depositInd;
    }

    /**
     * Retrieve a collection of deposit ticket information.
     * 
     * @param depositTime
     *            deposit time
     * @param depositUser
     *            deposit user
     * @return com.csc.fsg.nba.tableaccess.NbaDepositTicketData[]
     */
    //NBA069 Method Signature changed, added a String argument depositUser
    public NbaDepositTicketData[] getDepositTicketData(java.util.Date depositTime, String depositUser) throws NbaBaseException,
            NbaDataAccessException {

        StringBuffer query = new StringBuffer("SELECT DEPOSIT_TIME, DEPOSIT_USER, BUNDLE_ID, COMPANY, TOTAL_AMT, CLOSE_TIME FROM NBA_DEPOSIT_TICKETS"); //SPR1191
        query.append(" WHERE " + formatSQLWhereCriterion(DEPOSIT_TIME, depositTime) + " AND " + formatSQLWhereCriterion(DEPOSIT_USER, depositUser));//NBA069

        List aList = new ArrayList(10); // SPR3290
        try {
            ResultSet rs = executeQuery(query.toString());
            while (rs.next()) {
                aList.add(new NbaDepositTicketData(rs));
            }
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally {
            close();
        }

        return ((NbaDepositTicketData[]) aList.toArray(new NbaDepositTicketData[aList.size()]));
    }

    /**
     * Retrieve a List of <code>NbaDepositTicketData</code> object containing deposit ticket data information.
     * 
     * @param depositTime
     *            deposit time
     * @return List of com.csc.fsg.nba.tableaccess.NbaDepositTicketData objects
     */
    //NBA069 New Method
    //NBA182 changed method return type
    public List getDepositTicketData(int days) throws NbaBaseException, NbaDataAccessException {
        Map tblCompany = loadCompanies();
        NbaDepositTicketData depositTicket = null;
        // Deposit Tickets less than "days" days old.
        //Begin NBA228        
        StringBuffer query = new StringBuffer("SELECT T1.DEPOSIT_TIME, T1.DEPOSIT_USER, T1.BUNDLE_ID, T1.COMPANY, T1.TOTAL_AMT, ");
        query.append("T1.REVISED_DEPOSIT_BUNDLE_TOTAL,T1.REVISED_DEPOSIT_TIME, T1.CLOSE_TIME, NBA_BUNDLE_LOCK.USER_ID ");
        query.append("FROM NBA_DEPOSIT_TICKETS T1 LEFT OUTER JOIN NBA_BUNDLE_LOCK ON T1.BUNDLE_ID = NBA_BUNDLE_LOCK.BUNDLE_ID ");
        query.append(" WHERE (" + formatSQLValue(new java.util.Date(), getDbType()) + " - DEPOSIT_TIME) <= " + days); //[CURRENT_TIME - DEPOSIT_TIME]
        //End NBA228 
        // difference of dates gives the
        // number of days NBA093
        query.append(" ORDER BY DEPOSIT_TIME, DEPOSIT_USER");
        List aList = new ArrayList(10); // SPR3290
        try {
            ResultSet rs = executeQuery(query.toString());
            while (rs.next()) {
                depositTicket = new NbaDepositTicketData(rs);
                String companyCode = NbaUtils.trim(rs.getString(COMPANY));
                String companyName = (String) tblCompany.get(companyCode);

                if (companyName == null) {
                    companyName = companyCode;
                }
                depositTicket.setCompanyName(companyName);
                aList.add(depositTicket);
            }
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally {
            close();
        }
        return aList; //NBA182

    }
    
    
    /**
     * Retrieve a List of Location Id for the current deposit based on deposit time.
     * 
     * @param depositTime
     *            deposit time
     * @return List of String objects
     */
    //NBA228 New Method

    public List getLocations(java.util.Date depositTime) throws NbaBaseException, NbaDataAccessException {

        StringBuffer query = new StringBuffer(" SELECT DISTINCT SCAN_STATION.LOCATION_ID ");
        query.append(" FROM NBA_CHECKS, NBA_DEPOSIT_TICKETS, NBADATA.SCAN_STATION SCAN_STATION ");
        query.append(" WHERE NBA_CHECKS.DEPOSIT_TIME = NBA_DEPOSIT_TICKETS.DEPOSIT_TIME AND ");
        query.append(" NBA_CHECKS.BUNDLE_ID = NBA_DEPOSIT_TICKETS.BUNDLE_ID AND "); 
        query.append(" (NBA_CHECKS.SCAN_STATION_ID = SCAN_STATION.SCAN_STATION_ID OR ");
        query.append(" (SCAN_STATION.SCAN_STATION_ID = '*' AND (SELECT COUNT(T.SCAN_STATION_ID) ");
        query.append(" FROM NBADATA.SCAN_STATION T WHERE T.SCAN_STATION_ID = NBA_CHECKS.SCAN_STATION_ID) = 0)) AND ");         
        query.append(formatSQLWhereCriterion("NBA_DEPOSIT_TICKETS.DEPOSIT_TIME", depositTime));
        List aList = new ArrayList(5); 
        try {
            ResultSet rs = executeQuery(query.toString());
            while (rs.next()) {
                String locationId = NbaUtils.trim(rs.getString(LOCATION_ID));
                aList.add(locationId);
            }
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally {
            close();
        }
        return aList; 
    }    

    /**
     * Retrieve a List of Location Id for the current deposit based on deposit time.
     * 
     * @param depositTime
     *            deposit time
     * @return List of String objects
     */
    //NBA228 New Method

    public List getLocationsForDraft(java.util.Date depositTime) throws NbaBaseException, NbaDataAccessException {

        StringBuffer query = new StringBuffer(" SELECT DISTINCT SCAN_STATION.LOCATION_ID ");
        query.append(" FROM NBA_CHECKS, NBA_DEPOSIT_TICKETSDRAFT NBA_DEPOSIT_TICKETS, NBADATA.SCAN_STATION SCAN_STATION ");
        query.append(" WHERE NBA_CHECKS.DEPOSIT_TIMEDRAFT = NBA_DEPOSIT_TICKETS.DEPOSIT_TIME AND ");
        query.append(" NBA_CHECKS.BUNDLE_ID = NBA_DEPOSIT_TICKETS.BUNDLE_ID AND "); 
        query.append(" (NBA_CHECKS.SCAN_STATION_ID = SCAN_STATION.SCAN_STATION_ID OR ");
        query.append(" (SCAN_STATION.SCAN_STATION_ID = '*' AND (SELECT COUNT(T.SCAN_STATION_ID) ");
        query.append(" FROM NBADATA.SCAN_STATION T WHERE T.SCAN_STATION_ID = NBA_CHECKS.SCAN_STATION_ID) = 0)) AND ");         
        query.append(formatSQLWhereCriterion("NBA_DEPOSIT_TICKETS.DEPOSIT_TIME", depositTime));
        List aList = new ArrayList(5); 
        try {
            ResultSet rs = executeQuery(query.toString());
            while (rs.next()) {
                String locationId = NbaUtils.trim(rs.getString(LOCATION_ID));
                aList.add(locationId);
            }
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally {
            close();
        }
        return aList; 
    } 

    /**
     * Retrieve credit card data for a specified transactionId.
     * 
     * @param transactionId
     *            the credit card payment transactionId
     * @return com.csc.fsg.nba.tableaccess.NbaCreditCardData[]
     */
    //NBA115 New Method
    public NbaCreditCardData getCreditCardData(String transactionId) throws NbaBaseException, NbaDataAccessException {

        StringBuffer query = new StringBuffer(
                "SELECT TRANSACTION_ID, COMPANY, CONTRACT_NUMBER, CREDIT_CARD_COMPANY, CREDIT_CARD_NUMBER, INFORCE_IND, APPLIED_IND, REJECT_IND, REFUSED_IND, AMOUNT, REPORT_IND, CHARGE_DATE, DEPOSIT_DATE FROM NBA_CREDIT_CARD");
        query.append(" WHERE " + formatSQLWhereCriterion(TRANSACTION_ID, transactionId));

        List aList = new ArrayList(10); // SPR3290
        try {
            ResultSet rs = executeQuery(query.toString());
            while (rs.next()) {
                aList.add(new NbaCreditCardData(rs));
            }
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally {
            close();
        }
        return (NbaCreditCardData) aList.get(0);

    }

    /**
     * Retrieve credit card data for the specified data
     * 
     * @param contract
     * @param ccNumber
     * @param ccType
     * @return com.csc.fsg.nba.tableaccess.NbaCreditCardData[]
     */
    //NBA115 New Method
    public NbaCreditCardData getCreditCardData(String contract, String ccNumber, String ccType) throws NbaBaseException, NbaDataAccessException {

        NbaCreditCardData nbaCreditCardData = null;
        StringBuffer query = new StringBuffer(
                "SELECT TRANSACTION_ID, COMPANY, CONTRACT_NUMBER, CREDIT_CARD_COMPANY, INFORCE_IND, APPLIED_IND, REJECT_IND, REFUSED_IND, AMOUNT, REPORT_IND FROM NBA_CREDIT_CARD");
        query.append(" WHERE " + formatSQLWhereCriterion(CONTRACT_NUMBER, contract));
        query.append(" AND " + formatSQLWhereCriterion(CREDIT_CARD_NUMBER, ccNumber));
        query.append(" AND " + formatSQLWhereCriterion(CREDIT_CARD_COMPANY, ccType));

        try {
            ResultSet rs = executeQuery(query.toString());
            if (rs.next()) {
                nbaCreditCardData = new NbaCreditCardData(rs);
            }
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally {
            close();
        }
        return nbaCreditCardData;

    }

    /**
     * Retrieve credit card data for transactions that have an accounting indicator set to true.
     * 
     * @return com.csc.fsg.nba.tableaccess.NbaCreditCardData[]
     */
    //NBA115 New Method
    public NbaCreditCardData[] getCreditCardExtractData() throws NbaBaseException, NbaDataAccessException {

        List extractList = new ArrayList(10); // SPR3290
        StringBuffer query = new StringBuffer(
                "SELECT TRANSACTION_ID, COMPANY, CONTRACT_NUMBER, CREDIT_CARD_COMPANY, CREDIT_CARD_NUMBER, INFORCE_IND, APPLIED_IND, REJECT_IND, REFUSED_IND, AMOUNT, REPORT_IND, CHARGE_DATE, DEPOSIT_DATE FROM NBA_CREDIT_CARD");
        query.append(" WHERE " + formatSQLWhereCriterion(APPLIED_IND, true));
        query.append(" AND " + formatSQLWhereCriterion(REPORT_IND, true));

        try {
            ResultSet rs = executeQuery(query.toString());
            while (rs.next()) {
                extractList.add(new NbaCreditCardData(rs));
            }
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally {
            close();
        }
        return ((NbaCreditCardData[]) extractList.toArray(new NbaCreditCardData[extractList.size()]));
    }

    /**
     * The credit card payment has been approved by the clearing house. Set the accounting and report indicators so the report will reflect these
     * results.
     * 
     * @param aTransactionID
     *            report id
     */
    //NBA115 New Method
    public void updateCCChargeDate(String aTransactionID, Date aDate) throws NbaBaseException, NbaDataAccessException {

        NbaCashieringTable table = new NbaCashieringTable();
        StringBuffer query = new StringBuffer("UPDATE NBA_CREDIT_CARD SET ");
        query.append(formatSQLUpdateValue(CHARGE_DATE, aDate));
        query.append(" WHERE " + formatSQLWhereCriterion(TRANSACTION_ID, aTransactionID));
        table.executeUpdate(query.toString());
    }

    /**
     * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
     * 
     * @return com.csc.fsg.nba.foundation.NbaLogger
     */
    protected static NbaLogger getLogger() {
        if (logger == null) {
            try {
                logger = NbaLogFactory.getLogger(NbaCashieringTable.class.getName());
            } catch (Exception e) {
                NbaBootLogger.log("NbaCashieringTable could not get a logger from the factory.");

            }
        }
        return logger;
    }

    /**
     * Retrieves the next technical key available.
     * 
     * @return long
     */
    public long getNextTechnicalKey() throws NbaBaseException, NbaDataAccessException {

        return (getSeedNumber(CASH_TECHNICAL_KEY));
    }

    /**
     * Retrieve a collection of bundle details that are open for deposit.
     * 
     * @param company
     *            company
     * @param bundleID
     *            bundle identification
     * @return com.csc.fsg.nba.vo.NbaCashCheckVO[]
     */
    public NbaCashCheckVO[] getOpenBundleDetails(String company, String bundleID) throws NbaBaseException, NbaDataAccessException {

        StringBuffer query = new StringBuffer(
                "SELECT NBA_CHECKS.TECHNICAL_KEY, NBA_CHECKS.BUNDLE_ID, NBA_CONTRACTS_CHECKS.COMPANY, NBA_CONTRACTS_CHECKS.CONTRACT_NUMBER, NBA_CHECKS.INCLUDE_IND, NBA_CONTRACTS_CHECKS.APPLIED_IND, NBA_CONTRACTS_CHECKS.REJECT_IND, NBA_CONTRACTS_CHECKS.PRIMARY_CONTRACT_IND, NBA_CONTRACTS_CHECKS.PRIMARY_INSURED_NAME, NBA_CONTRACTS_CHECKS.AMOUNT_APPLIED, NBA_CHECKS.CHECK_NUMBER, NBA_CHECKS.CHECK_AMT, NBA_CHECKS.REVISED_CHECK_AMT FROM NBA_CHECKS, NBA_CONTRACTS_CHECKS");//NBA069

        query.append(" WHERE nba_checks.technical_key = nba_contracts_checks.technical_key AND nba_contracts_checks.technical_key IN");
        query.append("(SELECT nba_checks.technical_key FROM nba_checks, nba_contracts_checks WHERE nba_checks.deposit_time is NULL");
        // begin SPR3290
        query.append(" AND ");
        query.append(formatSQLWhereCriterion(COMPANY, company));
        query.append(" AND ");
        query.append(formatSQLWhereCriterion("NBA_CHECKS.BUNDLE_ID", bundleID));
        query.append(" AND ");
        query.append(formatSQLWhereCriterion(PRIMARY_CONTRACT_IND, true));
        // end SPR3290
        query.append(") ORDER BY nba_checks.technical_key");

        List aList = new ArrayList(10); // SPR3290
        try {
            ResultSet rs = executeQuery(query.toString());
            NbaCashCheckVO cashCheck = null; // SPR3290
            while (rs.next()) {
                cashCheck = new NbaCashCheckVO(); // SPR3290
                cashCheck.setTechnicalKey(rs.getLong(TECHNICAL_KEY));
                cashCheck.setCompany(NbaUtils.trim(rs.getString(COMPANY)));
                cashCheck.setBundleID(NbaUtils.trim(rs.getString(BUNDLE_ID)));
                cashCheck.setContractNumber(rs.getString(CONTRACT_NUMBER));
                cashCheck.setIncludedInd(stringToBoolean(rs.getString(INCLUDE_IND)));
                cashCheck.setAppliedInd(stringToBoolean(rs.getString(APPLIED_IND)));
                cashCheck.setRejectedInd(stringToBoolean(rs.getString(REJECT_IND)));
                cashCheck.setPrimaryContractInd(stringToBoolean(rs.getString(PRIMARY_CONTRACT_IND)));
                cashCheck.setPrimaryInsuredName(NbaUtils.trim(rs.getString(PRIMARY_INSURED_NAME)));
                cashCheck.setAppliedAmount(rs.getBigDecimal(AMOUNT_APPLIED));
                cashCheck.setCheckNumber(rs.getString(CHECK_NUMBER));
                cashCheck.setCheckAmount(rs.getBigDecimal(CHECK_AMT));
                cashCheck.setRevisedCheckAmt(rs.getBigDecimal(REVISED_CHECK_AMT));//NBA069
                aList.add(cashCheck);
            }
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally {
            close();
        }

        return ((NbaCashCheckVO[]) aList.toArray(new NbaCashCheckVO[aList.size()]));
    }

    //NBA044 generic method moved to <code>NbaTableHelper</code>
    /**
     * Retrieve the seed number from the cashiering sequence table. If the seed does not exist, create it. Otherwise, increment the value and update
     * the table.
     * 
     * @param seedKey
     *            java.lang.String
     * @return long
     */
    protected long getSeedNumber(String seedKey) throws NbaBaseException, NbaDataAccessException {

        long seedNbr = NbaConfiguration.getInstance().getCashiering().getInitialSeed(); // ACN012

        StringBuffer query = new StringBuffer("SELECT SEED_NUMBER FROM NBA_CASH_SEQUENCE");
        query.append(" WHERE " + formatSQLWhereCriterion(SEQUENCE_NAME, seedKey));

        try {
            ResultSet rs = executeQuery(query.toString());
            if (rs.next()) {
                seedNbr = rs.getLong(SEED_NUMBER);

                query = new StringBuffer("UPDATE NBA_CASH_SEQUENCE SET ");
                query.append(formatSQLUpdateValue(SEED_NUMBER, seedNbr + 1));
                query.append(" WHERE " + formatSQLWhereCriterion(SEQUENCE_NAME, seedKey));
                executeUpdate(query.toString());
            } else {
                query = new StringBuffer("INSERT INTO NBA_CASH_SEQUENCE (SEQUENCE_NAME, SEED_NUMBER) VALUES (");
                query.append(formatSQLValue(seedKey));
                query.append(", " + formatSQLValue(seedNbr + 1));
                query.append(")");
                executeInsert(query.toString());
            }

            close();
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        }

        return (seedNbr);
    }

    //NBA044 generic methods moved to <code>NbaTableHelper</code>
    /**
     * Retrieve the contract information for a select wire transfer.
     * 
     * @param effDate
     *            date of the wire transfer
     * @param company
     *            company ID
     * @param contractNumber
     *            contract number
     * @param sequence
     *            sequence number
     * @return com.csc.fsg.nba.tableaccess.NbaContractsWireTransferData
     */
    // SPR1171 added new parameter, sequence
    public NbaContractsWireTransferData getWireTransferContractData(java.util.Date effDate, String company, String contractNumber, long sequence)
            throws NbaBaseException, NbaDataAccessException {

        StringBuffer query = new StringBuffer(
                "SELECT NBA_CONTRACTS_WIRE_TRANSFER.TECHNICAL_KEY, SEQUENCE, COMPANY, CONTRACT_NUMBER, APPLIED_IND, RETURNED_IND, AMOUNT, REPORT_IND FROM NBA_WIRE_TRANSFER, NBA_CONTRACTS_WIRE_TRANSFER");
        query.append(" WHERE " + formatSQLWhereCriterion("NBA_WIRE_TRANSFER.WIRE_TRANSFER_EFFDATE", effDate));
        query.append(" AND " + formatSQLWhereCriterion(COMPANY, company));
        query.append(" AND " + formatSQLWhereCriterion(CONTRACT_NUMBER, contractNumber));
        query.append(" AND " + formatSQLWhereCriterion(SEQUENCE, sequence)); //SPR1171
        query.append(" AND NBA_WIRE_TRANSFER.TECHNICAL_KEY = NBA_CONTRACTS_WIRE_TRANSFER.TECHNICAL_KEY");

        NbaContractsWireTransferData contractWireTransfer = null;
        try {
            ResultSet rs = executeQuery(query.toString());
            if (rs.next()) {
                contractWireTransfer = new NbaContractsWireTransferData(rs);
            }
           //PERF-APSL634 code deleted
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally { //PERF-APSL634
        	close(); //PERF-APSL634
        }  //PERF-APSL634

        return (contractWireTransfer);
    }

    /**
     * Retrieve the next sequence number for a wire transfer.
     * 
     * @param effDate
     *            java.util.Date
     * @param institutionID
     *            java.lang.String
     * @return long
     */
    // SPR1171 new method
    public long getWireTransferSequence(java.util.Date effDate, String institutionID) throws NbaDataAccessException {

        StringBuffer query = new StringBuffer("SELECT MAX(NBA_CONTRACTS_WIRE_TRANSFER.SEQUENCE) FROM NBA_WIRE_TRANSFER, NBA_CONTRACTS_WIRE_TRANSFER");
        query.append(" WHERE " + formatSQLWhereCriterion(WIRE_TRANSFER_EFFDATE, effDate));
        query.append(" AND " + formatSQLWhereCriterion(INSTITUTION_ID, institutionID));
        query.append(" AND NBA_WIRE_TRANSFER.TECHNICAL_KEY = NBA_CONTRACTS_WIRE_TRANSFER.TECHNICAL_KEY");

        long sequence = 0;

        try {
            ResultSet rs = executeQuery(query.toString());
            if (rs.next()) {
                sequence = rs.getLong(1);
            }

           //PERF-APSL634 code deleted
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally { //PERF-APSL634
        	close(); //PERF-APSL634
        }  //PERF-APSL634

        return (++sequence);
    }

    //NBA044 generic method moved to <code>NbaTableHelper</code>
    /**
     * Initializes the logger for the class
     * 
     * @param newLogger
     */
    public static void setLogger(NbaLogger newLogger) {
        logger = newLogger;
    }

    /**
     * Update the check's information as being deposited.
     * @param company company code
     * @param bundleID bundle identification
     * @param depositTime deposit time stamp
     * @return java.math.BigDecimal total amount deposited for this bundle
     * @throws NbaDataAccessException
     */
    public java.math.BigDecimal updateChecksAsDeposited(String company, String bundleID, java.util.Date depositTime) throws NbaDataAccessException {

        StringBuffer query = new StringBuffer("UPDATE NBA_CHECKS SET ");

        query.append(formatSQLUpdateValue(DEPOSIT_TIME, depositTime));
        query.append(" WHERE " + formatSQLWhereCriterion(DEPOSIT_TIME, (java.util.Date) null));
        if (getDepositInd().compareToIgnoreCase(NbaConfigurationConstants.DEPOSIT_APPLY) != 0) { // ACN012
            query.append(" AND " + formatSQLWhereCriterion(INCLUDE_IND, true));
        }
        query.append(" AND " + TECHNICAL_KEY);
        query.append(" IN (SELECT DISTINCT TECHNICAL_KEY FROM NBA_CONTRACTS_CHECKS WHERE ");
        query.append(formatSQLWhereCriterion(COMPANY, company));
        query.append(" AND " + formatSQLWhereCriterion(BUNDLE_ID, bundleID));
        if (getDepositInd().compareToIgnoreCase(NbaConfigurationConstants.DEPOSIT_APPLY) == 0) { // ACN012
            query.append(" AND " + formatSQLWhereCriterion(APPLIED_IND, true));
        }
        query.append(" AND " + formatSQLWhereCriterion(SYSTEM_APPLIED_IND, true)); // SR615900
        query.append(")");

        executeUpdate(query.toString());

        // calculate the total deposit amount for this bundle
        query = new StringBuffer("SELECT SUM(" + CHECK_AMT + ") FROM NBA_CHECKS WHERE ");
        query.append(formatSQLWhereCriterion(DEPOSIT_TIME, depositTime));
        query.append(" AND " + formatSQLWhereCriterion(BUNDLE_ID, bundleID));
        if (getDepositInd().compareToIgnoreCase(NbaConfigurationConstants.DEPOSIT_APPLY) != 0) { //ALS4610 
            query.append(" AND " + formatSQLWhereCriterion(INCLUDE_IND, true));
        }
        try { //PERF-APSL634
        	ResultSet rs = executeQuery(query.toString());
        //PERF-APSL634 code deleted
            if (rs.next()) {
                return rs.getBigDecimal(1);
            } else {
                throw new NbaDataAccessException("Could not calculate updated check total for bundle: " + bundleID);
            }
        } catch (SQLException se) {
            throw new NbaDataAccessException(NbaTable.SQL_ERROR, se);
        } finally { //PERF-APSL634
        	close(); //PERF-APSL634
        }  //PERF-APSL634
    }

    /**
     * Update the check's information as being un deposited by setting the deposit time to null.
     * @param company company code
     * @param bundleID bundle identification
     * @param depositTime deposit time stamp
     * @return java.math.BigDecimal total amount deposited for this bundle
     * @throws NbaDataAccessException
     */
    //AXAL3.7.23 - new method 
    //APSL276 Changed Signature
    public void updateChecksAsUnDeposited(Date depositDate) throws NbaDataAccessException {

        StringBuffer query = new StringBuffer("UPDATE NBA_CHECKS SET DEPOSIT_TIME = null ");
        query.append(" WHERE " + formatSQLWhereCriterion(DEPOSIT_TIME, depositDate)); //APSL276
        //APSL276 Code Deleted

        executeUpdate(query.toString());
    }
    

    /**
     * Update the check's information as being deposited.
     * @param company company code
     * @param bundleID bundle identification
     * @param depositTime deposit time stamp
     * @return java.math.BigDecimal total amount deposited for this bundle
     * @throws NbaDataAccessException
     */
    //NBA228 New method
    public java.math.BigDecimal updateChecksAsDraftDeposited(String company, String bundleID, java.util.Date draftTime) throws NbaDataAccessException {

        StringBuffer query = new StringBuffer("UPDATE NBA_CHECKS SET ");

        query.append(formatSQLUpdateValue(DRAFT_TIME, draftTime));
        query.append(" WHERE " + formatSQLWhereCriterion(DEPOSIT_TIME, (java.util.Date) null));
        if (getDepositInd().compareToIgnoreCase(NbaConfigurationConstants.DEPOSIT_APPLY) != 0) { 
            query.append(" AND " + formatSQLWhereCriterion(INCLUDE_IND, true));
        }
        query.append(" AND " + TECHNICAL_KEY);
        query.append(" IN (SELECT DISTINCT TECHNICAL_KEY FROM NBA_CONTRACTS_CHECKS WHERE ");
        query.append(formatSQLWhereCriterion(COMPANY, company));
        query.append(" AND " + formatSQLWhereCriterion(BUNDLE_ID, bundleID));
        if (getDepositInd().compareToIgnoreCase(NbaConfigurationConstants.DEPOSIT_APPLY) == 0) { 
            query.append(" AND " + formatSQLWhereCriterion(APPLIED_IND, true));
        }
        query.append(")");

        executeUpdate(query.toString());

        // calculate the total deposit amount for this bundle
        query = new StringBuffer("SELECT SUM(" + CHECK_AMT + ") FROM NBA_CHECKS WHERE ");
        query.append(formatSQLWhereCriterion(DRAFT_TIME, draftTime));
        query.append(" AND " + formatSQLWhereCriterion(BUNDLE_ID, bundleID));

        //PERF-APSL634 code deleted..moved inside try statement
        try {
        	ResultSet rs = executeQuery(query.toString()); //PERF-APSL634
            if (rs.next()) {
                return rs.getBigDecimal(1);
            } else {
                throw new NbaDataAccessException("Could not calculate updated check total for bundle: " + bundleID);
            }
        } catch (SQLException se) {
            throw new NbaDataAccessException(NbaTable.SQL_ERROR, se);
        } finally { //PERF-APSL634
        	close(); //PERF-APSL634
        }  //PERF-APSL634
    }

    
    /**
     * Update the check's deposit timestamp for all recently closed bundles, so they are reported correctly in the generated reports.
     * 
     * @param depositTime
     *            deposit time stamp
     */
    // SPR1191 new method
    public void updateClosedBundles(java.util.Date depositTime) throws NbaDataAccessException {

        StringBuffer query = new StringBuffer("UPDATE NBA_DEPOSIT_TICKETS SET ");

        query.append(formatSQLUpdateValue(DEPOSIT_TIME, depositTime));
        query.append(" WHERE DEPOSIT_TIME = CLOSE_TIME");

        executeUpdate(query.toString());
    }

    /**
     * Determine if any checks on the deposit ticket are for variable products.
     * 
     * @param depositTime
     *            java.util.Date
     * @return boolean
     */
    public boolean variableProductInDeposit(java.util.Date depositTime) throws NbaBaseException, NbaDataAccessException {

        StringBuffer query = new StringBuffer("SELECT COUNT(NBA_CHECKS.TECHNICAL_KEY) FROM NBA_CHECKS, NBA_CONTRACTS_CHECKS");
        query.append(" WHERE " + formatSQLWhereCriterion(DEPOSIT_TIME, depositTime));
        query.append(" AND NBA_CHECKS.TECHNICAL_KEY = NBA_CONTRACTS_CHECKS.TECHNICAL_KEY AND (");
        query.append(formatSQLWhereCriterion(PRODUCT_TYPE, Long.toString(NbaOliConstants.OLI_PRODTYPE_VUL)));
        query.append(" OR " + formatSQLWhereCriterion(PRODUCT_TYPE, Long.toString(NbaOliConstants.OLI_PRODTYPE_VAR)));
        query.append(")");

        boolean inDeposit = false;
        try {
            ResultSet rs = executeQuery(query.toString());
            if (rs.next()) {
                if (rs.getInt(1) > 0) {
                    inDeposit = true;
                }
            }
            //PERF-APSL634 code deleted
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally { //PERF-APSL634
        	close(); //PERF-APSL634
        }  //PERF-APSL634

        return (inDeposit);
    }

    /**
     * Retrieve a collection of bundle details for deposit correction.
     * 
     * @param company
     *            company
     * @param bundleID
     *            bundle identification
     * @param dateList
     *            date list
     * @return com.csc.fsg.nba.vo.NbaCashCheckVO[]
     */
    //NBA069 New Method
    public List getDepositedBundleDetails(String company, String bundleID, ArrayList dateList) throws NbaBaseException, NbaDataAccessException {

        Map tblCompany = loadCompanies(); 
        StringBuffer query = new StringBuffer("SELECT NBA_CHECKS.TECHNICAL_KEY, NBA_CHECKS.BUNDLE_ID, NBA_CHECKS.DEPOSIT_TIME, ");
        query.append("NBA_CHECKS.CORRECTION_IND, NBA_CONTRACTS_CHECKS.COMPANY, NBA_CONTRACTS_CHECKS.CONTRACT_NUMBER, NBA_CHECKS.INCLUDE_IND, ");
        query.append("NBA_CONTRACTS_CHECKS.APPLIED_IND, NBA_CONTRACTS_CHECKS.REJECT_IND, NBA_CONTRACTS_CHECKS.PRIMARY_CONTRACT_IND, ");
        query.append("NBA_CONTRACTS_CHECKS.PRIMARY_INSURED_NAME, NBA_CONTRACTS_CHECKS.AMOUNT_APPLIED, NBA_CHECKS.CHECK_NUMBER, ");
        query.append("NBA_CHECKS.CHECK_AMT,  NBA_CHECKS.REVISED_CHECK_AMT, NBA_CHECKS.CHECK_LAST_NAME "); //NBA228
        query.append("FROM NBA_CHECKS, NBA_CONTRACTS_CHECKS ");
        query.append("WHERE nba_checks.technical_key = nba_contracts_checks.technical_key AND nba_contracts_checks.technical_key IN");
        query.append("(SELECT nba_checks.technical_key FROM nba_checks, nba_contracts_checks WHERE nba_checks.deposit_time is NOT NULL");
        query.append(" AND (" + formatSQLWhereCriterion("NBA_CHECKS.DEPOSIT_TIME", (java.util.Date) dateList.get(0)));
        int size = dateList.size();
        for (int i = 1; i < size; i++) {
            query.append(" OR ");
            query.append(formatSQLWhereCriterion("NBA_CHECKS.DEPOSIT_TIME", (java.util.Date) dateList.get(i)));
        }
        //query.append(" AND " + formatSQLWhereCriterion(COMPANY, company));
        query.append(") AND " + formatSQLWhereCriterion("NBA_CHECKS.BUNDLE_ID", bundleID));
        query.append(" AND " + formatSQLWhereCriterion(PRIMARY_CONTRACT_IND, true));
        query.append(") ORDER BY nba_checks.technical_key , PRIMARY_CONTRACT_IND desc");

        List aList = new ArrayList(10); // SPR3290
        try {
            ResultSet rs = executeQuery(query.toString());
            while (rs.next()) {
                NbaCashCheckVO cashCheck = new NbaCashCheckVO();
                String companyName = (String) tblCompany.get(NbaUtils.trim(rs.getString(COMPANY)));
                cashCheck.setTechnicalKey(rs.getLong(TECHNICAL_KEY));
                cashCheck.setCompany(NbaUtils.trim(rs.getString(COMPANY)));
                cashCheck.setCompanyName(companyName);
                cashCheck.setBundleID(NbaUtils.trim(rs.getString(BUNDLE_ID)));
                cashCheck.setContractNumber(rs.getString(CONTRACT_NUMBER));
                cashCheck.setIncludedInd(stringToBoolean(rs.getString(INCLUDE_IND)));
                cashCheck.setAppliedInd(stringToBoolean(rs.getString(APPLIED_IND)));
                cashCheck.setDisableAppliedInd(true);
                cashCheck.setRejectedInd(stringToBoolean(rs.getString(REJECT_IND)));
                cashCheck.setDisableRejectInd(true);
                cashCheck.setPrimaryContractInd(stringToBoolean(rs.getString(PRIMARY_CONTRACT_IND)));
                cashCheck.setPrimaryInsuredName(NbaUtils.trim(rs.getString(PRIMARY_INSURED_NAME)));
                cashCheck.setAppliedAmount(rs.getBigDecimal(AMOUNT_APPLIED));
                cashCheck.setOldappliedAmount(rs.getBigDecimal(AMOUNT_APPLIED));
                cashCheck.setCheckNumber(rs.getString(CHECK_NUMBER));
                if (rs.getString(CORRECTION_IND) != null && rs.getString(CORRECTION_IND).equalsIgnoreCase("Y")
                        && rs.getBigDecimal(REVISED_CHECK_AMT) != null) {
                    cashCheck.setCheckAmount(rs.getBigDecimal(REVISED_CHECK_AMT));
                    cashCheck.setOldcheckAmount(rs.getBigDecimal(REVISED_CHECK_AMT));
                } else {
                    cashCheck.setCheckAmount(rs.getBigDecimal(CHECK_AMT));
                    cashCheck.setOldcheckAmount(rs.getBigDecimal(CHECK_AMT));
                }
                cashCheck.setDepositTimeMilliSeconds(rs.getDate(DEPOSIT_TIME).getTime());
                cashCheck.setCheckName(rs.getString(CHECK_LAST_NAME)); //NBA228
                aList.add(cashCheck);
            }
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally {
            close();
        }

        //return ((NbaCashCheckVO[]) aList.toArray(new NbaCashCheckVO[aList.size()]));
        return aList;

    }

    /**
     * Update the check's information as being part of a corrected deposit.
     * 
     * @param userID
     *            user identification
     * @param bundleID
     *            bundle identification
     * @param originalDepositTime
     *            original deposit time stamp
     * @param newDepositTime
     *            new deposit time stamp
     * @return BigDecimal total amount deposited for this bundle
     */
    //NBA069 New Method
    public java.math.BigDecimal updateChecksAsReDeposited(String userID, String bundleID, java.util.Date originalDepositTime,
            java.util.Date newDepositTime) throws NbaDataAccessException {

        StringBuffer query = new StringBuffer("UPDATE NBA_CHECKS SET ");

        query.append(formatSQLUpdateValue(CORRECTION_TIME, newDepositTime));
        query.append(", " + formatSQLUpdateValue(USER_ID, userID));
        query.append(" WHERE " + formatSQLWhereCriterion(DEPOSIT_TIME, originalDepositTime));
        query.append(" AND " + formatSQLWhereCriterion(BUNDLE_ID, bundleID));

        executeUpdate(query.toString());

        // calculate the total deposit amount for this bundle
        query = new StringBuffer("SELECT SUM(" + CHECK_AMT + ") FROM NBA_CHECKS WHERE ");
        query.append(formatSQLWhereCriterion(DEPOSIT_TIME, originalDepositTime));
        query.append(" AND " + formatSQLWhereCriterion(CORRECTION_TIME, newDepositTime));
        query.append(" AND " + formatSQLWhereCriterion(BUNDLE_ID, bundleID));
        query.append(" AND " + REVISED_CHECK_AMT + " is NULL");
        try { //PERF-APSL634
        	ResultSet rs1 = executeQuery(query.toString());

        	query = new StringBuffer("SELECT SUM(" + REVISED_CHECK_AMT + ") FROM NBA_CHECKS WHERE ");
        	query.append(formatSQLWhereCriterion(DEPOSIT_TIME, originalDepositTime));
        	query.append(" AND " + formatSQLWhereCriterion(BUNDLE_ID, bundleID));
        	query.append(" AND " + REVISED_CHECK_AMT + " is NOT NULL");

        	ResultSet rs2 = executeQuery(query.toString());

        	BigDecimal totalOriginalAmount = null;
        	BigDecimal totalRevisedAmount = null;
        	boolean rs1True = false;
        	boolean rs2True = false;

        //PERF-APSL634 code deleted
            rs1True = rs1.next();
            rs2True = rs2.next();
            if (rs1True || rs2True) {
                if (rs1True) {
                    totalOriginalAmount = rs1.getBigDecimal(1);
                }
                if (rs2True) {
                    totalRevisedAmount = rs2.getBigDecimal(1);
                }
                if (totalOriginalAmount != null && totalRevisedAmount != null) {
                    return totalOriginalAmount.add(totalRevisedAmount);
                } else if (totalRevisedAmount != null) {
                    return totalRevisedAmount;
                } else {
                    return totalOriginalAmount;
                }
            } else {
                throw new NbaDataAccessException("Could not calculate updated check total for bundle: " + bundleID);
            }
        } catch (SQLException se) {
            throw new NbaDataAccessException(NbaTable.SQL_ERROR, se);
        } finally { //PERF-APSL634
        	close(); //PERF-APSL634
        }  //PERF-APSL634
    }

    /**
     * Update the deposit ticket with the revised deposit total.
     * 
     * @param bundleID
     *            bundle identification
     * @param originalDepositTime
     *            original deposit time stamp
     */
    //NBA069 New Method
    public void updateDepositTicketWithRevisedDepositTotal(String bundleID, java.util.Date originalDepositTime) throws NbaDataAccessException {
        updateDepositTicketWithRevisedDepositTotal(bundleID, originalDepositTime, false);
    }

    /**
     * Update the deposit ticket with the revised deposit total.
     * 
     * @param bundleID
     *            bundle identification
     * @param originalDepositTime
     *            original deposit time stamp
     * @param excludeCheck
     *            true when check has to be excluded
     */
    //NBA069 New Method
    public void updateDepositTicketWithRevisedDepositTotal(String bundleID, java.util.Date originalDepositTime, boolean excludeCheck)
			throws NbaDataAccessException {
		StringBuffer query = null;
		// calculate the total deposit amount for this bundle
		query = new StringBuffer("SELECT SUM(" + CHECK_AMT + ") FROM NBA_CHECKS WHERE ");
		query.append(formatSQLWhereCriterion(DEPOSIT_TIME, originalDepositTime));
		query.append(" AND " + formatSQLWhereCriterion(BUNDLE_ID, bundleID));
		query.append(" AND " + REVISED_CHECK_AMT + " is NULL");
		BigDecimal originalPlusRevised = null; //PERF-APSL634
		try { //PERF-APSL634
			ResultSet rs1 = executeQuery(query.toString());

			query = new StringBuffer("SELECT SUM(" + REVISED_CHECK_AMT + ") FROM NBA_CHECKS WHERE ");
			query.append(formatSQLWhereCriterion(DEPOSIT_TIME, originalDepositTime));
			query.append(" AND " + formatSQLWhereCriterion(BUNDLE_ID, bundleID));
			query.append(" AND " + REVISED_CHECK_AMT + " is NOT NULL");

			ResultSet rs2 = executeQuery(query.toString());

			BigDecimal totalOriginalAmount = null;
			BigDecimal totalRevisedAmount = null;
			//PERF-APSL634 code deleted
			boolean rs1True = false;
			boolean rs2True = false;

			//PERF-APSL634 code deleted
			rs1True = rs1.next();
			rs2True = rs2.next();
			if (rs1True || rs2True) {
				if (rs1True) {
					totalOriginalAmount = rs1.getBigDecimal(1);
				}
				if (rs2True) {
					totalRevisedAmount = rs2.getBigDecimal(1);
				}
				if (totalOriginalAmount != null && totalRevisedAmount != null) {
					originalPlusRevised = totalOriginalAmount.add(totalRevisedAmount);
				} else if (totalRevisedAmount != null) {
					originalPlusRevised = totalRevisedAmount;
				} else {
					originalPlusRevised = totalOriginalAmount;
				}
				if (excludeCheck && originalPlusRevised == null) {
					originalPlusRevised = new BigDecimal(0.00);
				}
			} else {
				throw new NbaDataAccessException("Could not calculate updated check total for bundle: " + bundleID);
			}

		} catch (SQLException se) {
			throw new NbaDataAccessException(NbaTable.SQL_ERROR, se);
		} finally { //PERF-APSL634
			close(); //PERF-APSL634
		} //PERF-APSL634
		try {
			query = new StringBuffer("UPDATE NBA_DEPOSIT_TICKETS SET ");
			query.append(formatSQLUpdateValue(REVISED_DEPOSIT_BUNDLE_TOTAL, originalPlusRevised));
			query.append(" WHERE " + formatSQLWhereCriterion(DEPOSIT_TIME, originalDepositTime));
			query.append(" AND " + formatSQLWhereCriterion(BUNDLE_ID, bundleID));
			executeUpdate(query.toString());
		} catch (Exception se) {
			throw new NbaDataAccessException("Could not update revised total for bundle: ", se);
		}

	}

    /**
	 * Update the deposit ticket with the revised deposit total for a bundle.
	 * 
	 * @param bundleID
	 *            bundle identification
	 */
    //NBA069 New Method
    public void updateDepositTicketWithRevisedDepositTotalForBundle(String bundleID) throws NbaDataAccessException {
        Set hashSet = new HashSet(10);
        java.util.Date depositTime = null;

        StringBuffer query = new StringBuffer("SELECT  DEPOSIT_TIME FROM NBA_CHECKS ");
        query.append(" WHERE " + formatSQLWhereCriterion(BUNDLE_ID, bundleID));
        query.append(" AND DEPOSIT_TIME is not NULL");
        ResultSet rs = executeQuery(query.toString());
        try {
            while (rs.next()) {
                depositTime = rs.getTimestamp(DEPOSIT_TIME);
                hashSet.add(depositTime);
            }
        } catch (SQLException sqe) {
            throw new NbaDataAccessException("Unable to update revised deposit total for bundle:" + sqe);
        }
        Iterator iterator = hashSet.iterator();
        while (iterator.hasNext()) {
            updateDepositTicketWithRevisedDepositTotal(bundleID, (java.util.Date) iterator.next());
        }
    }

    /**
     * Retrieve the information from NBA_CHECKS table based on bundleID and includeInd passed in this method as parameters.
     * @param bundleId bundle Identification
     * @param includeInd Include Indicator
     * @return NbaCheckData[]
     */
    //SPR1726 new method
    public NbaCheckData[] getCheckData(String bundleID, String includeInd, java.util.Date originalDepositTime) throws NbaBaseException,
            NbaDataAccessException {

        List checkData = new ArrayList(10); // SPR3290
        StringBuffer query = new StringBuffer(
                "SELECT INCLUDE_IND, TECHNICAL_KEY,BUNDLE_ID,CHECK_NUMBER, CHECK_DATE, DEPOSIT_TIME, USER_ID, CHECK_AMT, CORRECTION_IND, REVISED_CHECK_AMT, LOCATION_ID, PAYMENT_FORM, NBA_CHECKS.SCAN_STATION_ID FROM NBA_CHECKS, NBADATA.SCAN_STATION SCAN_STATION"); //NBA228 AXAL3.7.23
        query.append(" WHERE " + formatSQLWhereCriterion(BUNDLE_ID, bundleID));     
        //ALS4610 code deleted
        query.append(" AND " + formatSQLWhereCriterion(DEPOSIT_TIME, originalDepositTime));
		query.append(" AND (NBA_CHECKS.SCAN_STATION_ID = SCAN_STATION.SCAN_STATION_ID OR (SCAN_STATION.SCAN_STATION_ID = '*' AND "); //NBA228
		query.append(" (SELECT COUNT(T.SCAN_STATION_ID) FROM NBADATA.SCAN_STATION T WHERE T.SCAN_STATION_ID = NBA_CHECKS.SCAN_STATION_ID) = 0))"); //NBA228
		query.append(" AND " + formatSQLWhereCriterion(INCLUDE_IND, includeInd));	//APSL341
		try {
            ResultSet rs = executeQuery(query.toString());
            while (rs.next()) {
                NbaCheckData nbaCheckData = new NbaCheckData();
                nbaCheckData.setTechnicalKey(rs.getLong(TECHNICAL_KEY));
                nbaCheckData.setBundleID(NbaUtils.trim(rs.getString(BUNDLE_ID)));
                nbaCheckData.setIncludeInd(stringToBoolean(rs.getString(INCLUDE_IND)));
                nbaCheckData.setDepositTimeStamp(rs.getTimestamp(DEPOSIT_TIME));
                nbaCheckData.setCheckAmount(rs.getDouble(CHECK_AMT));
                nbaCheckData.setCheckDate(rs.getDate(CHECK_DATE));
                nbaCheckData.setCheckNumber(rs.getString(CHECK_NUMBER));
                nbaCheckData.setCorrection_Ind(rs.getString(CORRECTION_IND));
                nbaCheckData.setRevisedCheckAmt(rs.getString(REVISED_CHECK_AMT));
                nbaCheckData.setUserId(rs.getString(USER_ID));
				nbaCheckData.setLocationId(rs.getString(LOCATION_ID)); //NBA228
				nbaCheckData.setScanStationID(rs.getString(SCAN_STATION_ID));//AXAL3.7.23
				nbaCheckData.setPaymentForm(rs.getInt(CHECK_PAYMENT_FORM)); //AXAL3.7.23
                checkData.add(nbaCheckData);
            }
            //PERF-APSL634 code deleted
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
		} finally { //PERF-APSL634
			close(); //PERF-APSL634
		} //PERF-APSL634

        return ((NbaCheckData[]) checkData.toArray(new NbaCheckData[checkData.size()]));
    }

    /**
     * Retrieve the information from NBA_CONTRACTS_CHECKS table based on bundleID and techincalKey passed in this method as parameters.
     * 
     * @param bundleId
     *            String
     * @param technicalKey
     *            String
     * @return NbaContractCheckData[]
     */
    //SPR1726 new method
    public NbaContractCheckData[] getContractCheckData(String bundleID, long technicalKey) throws NbaBaseException, NbaDataAccessException {

        List contractCheckData = new ArrayList(10); // SPR3290
        StringBuffer query = new StringBuffer("SELECT CONTRACT_NUMBER,PRIMARY_INSURED_NAME, COMPANY, AMOUNT_APPLIED, PRODUCT_CODE, BACKEND_SYSTEM, DISTRIBUTIONCHANNEL FROM NBA_CONTRACTS_CHECKS"); //ALII1895, APSL3410

        query.append(" WHERE " + formatSQLWhereCriterion(BUNDLE_ID, bundleID));
        query.append(" AND " + formatSQLWhereCriterion(TECHNICAL_KEY, technicalKey));

        try {
            ResultSet rs = executeQuery(query.toString());
            while (rs.next()) {
                NbaContractCheckData nbaContractCheckData = new NbaContractCheckData();
                nbaContractCheckData.setContractNumber(rs.getString(CONTRACT_NUMBER));
                nbaContractCheckData.setPrimaryInsuredName(rs.getString(PRIMARY_INSURED_NAME));
                nbaContractCheckData.setCompany(rs.getString(COMPANY));	//AXAL3.7.23
                nbaContractCheckData.setAmountApplied(rs.getBigDecimal(AMOUNT_APPLIED)); //AXAL3.7.23
                nbaContractCheckData.setProductCode(rs.getString(PRODUCT_CODE));	//AXAL3.7.23
                nbaContractCheckData.setBackendSystem(rs.getString(BACKEND_SYSTEM));	//ALII1895
                nbaContractCheckData.setDistributionChannel(rs.getLong(DISTRIBUTION_CHANNEL));    //APSL3410
                contractCheckData.add(nbaContractCheckData);
            }
            //PERF-APSL634 code deleted
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
		} finally { //PERF-APSL634
			close(); //PERF-APSL634
		} //PERF-APSL634

        return ((NbaContractCheckData[]) contractCheckData.toArray(new NbaContractCheckData[contractCheckData.size()]));
    }

    /**
     * Returns the ds.
     * 
     * @return DataSource
     */
    //SPR1808 New Method
    public DataSource getDataSource() {
        return ds;
    }

    /**
     * Sets the ds.
     * 
     * @param DataSource
     */
    //SPR1808 New Method
    public void setDataSource(DataSource newDs) {
        ds = newDs;
    }

    /**
     * Retrieve a collection of deposit ticket information.
     * 
     * @param bundleId
     *            String
     * @return com.csc.fsg.nba.tableaccess.NbaDepositTicketData[]
     */
    //SPR1726 new method
    public NbaDepositTicketData[] getDepositTicketData(String bundleId, java.util.Date originalDepositTime) throws NbaBaseException,
            NbaDataAccessException {
        StringBuffer query = new StringBuffer(
                "SELECT DEPOSIT_TIME, DEPOSIT_USER, BUNDLE_ID, COMPANY, TOTAL_AMT, REVISED_DEPOSIT_BUNDLE_TOTAL, REVISED_DEPOSIT_TIME, CORRECTION_IND FROM NBA_DEPOSIT_TICKETS");
        query.append(" WHERE " + formatSQLWhereCriterion(BUNDLE_ID, bundleId));
        query.append(" AND " + formatSQLWhereCriterion(DEPOSIT_TIME, originalDepositTime));
        List aList = new ArrayList(10); // SPR3290
        try {
            ResultSet rs = executeQuery(query.toString());
            while (rs.next()) {
                NbaDepositTicketData nbaDepositTicketData = new NbaDepositTicketData();
                nbaDepositTicketData.setDepositTime(rs.getTimestamp(DEPOSIT_TIME));
                nbaDepositTicketData.setDepositUser(NbaUtils.trim(rs.getString(DEPOSIT_USER)));
                nbaDepositTicketData.setBundleID(NbaUtils.trim(rs.getString(BUNDLE_ID)));
                nbaDepositTicketData.setCompany(NbaUtils.trim(rs.getString(COMPANY)));
                nbaDepositTicketData.setTotalAmount(rs.getBigDecimal(TOTAL_AMT));
                nbaDepositTicketData.setRevisedDepositTime(rs.getTimestamp(REVISED_DEPOSIT_TIME));
                nbaDepositTicketData.setRevisedTotalAmount(rs.getBigDecimal(REVISED_DEPOSIT_BUNDLE_TOTAL));
                aList.add(nbaDepositTicketData);
            }
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally {
            close();
        }
        return ((NbaDepositTicketData[]) aList.toArray(new NbaDepositTicketData[aList.size()]));

    }

    /**
     * Retrieve a list of companies and number of open bundles that are open for deposit.
     * @return the list of companies
     * @throws NbaBaseException
     * @throws NbaDataAccessException
     */
    public List getOpenBundleDetails() throws NbaBaseException, NbaDataAccessException {

        Map tblCompany = loadCompanies();

        //StringBuffer query = new StringBuffer("select company,COUNT(bundle_id)AS BUNDLES from nba_bundles GROUP BY COMPANY ");

        StringBuffer query1 = new StringBuffer(
                "SELECT nba_contracts_checks.company, count(distinct(nba_checks.bundle_id)) as BUNDLES from nba_checks,");
        query1.append("nba_contracts_checks WHERE nba_checks.technical_key = nba_contracts_checks.technical_key AND nba_checks.deposit_time is null");
        query1.append(" And nba_contracts_checks.primary_contract_ind = 'Y'"); // NBA182 
        query1.append(" And nba_contracts_checks.backend_system <> 'ADS'"); //NBLXA-1908
        query1.append(" group by nba_contracts_checks.company");
        // NBA182 deleted code

       List companies = new ArrayList(10); // SPR3290

        //PERF-APSL634 code deleted

        NbaCashCompanyVO cashCompany = null;

        try {
            conn = NbaConnectionManager.borrowConnection(NbaConfigurationConstants.CASHIERING);
            ResultSet rs = executeQuery(query1.toString(),conn);
            while (rs.next()) {
                String companyCode = NbaUtils.trim(rs.getString(COMPANY));
                String companyName = (String) tblCompany.get(companyCode);

                if (companyName == null) {
                    companyName = companyCode;
                }
                cashCompany = new NbaCashCompanyVO();
                cashCompany.setCompanyCode(companyCode);
                cashCompany.setCompanyName(companyName);
                cashCompany.setNumberOfBundles(rs.getInt(BUNDLES));
                companies.add(cashCompany);

            }
            //Start : NBLXA-1908 Coil changes
            StringBuffer query2 = new StringBuffer(
                    "SELECT nba_contracts_checks.company, count(distinct(nba_checks.bundle_id)) as BUNDLES from nba_checks,");
            query2.append("nba_contracts_checks WHERE nba_checks.technical_key = nba_contracts_checks.technical_key AND nba_checks.deposit_time is null");
            query2.append(" And nba_contracts_checks.primary_contract_ind = 'Y'");
            query2.append(" And nba_contracts_checks.backend_system = 'ADS'");
            query2.append(" group by nba_contracts_checks.company");
            
            ResultSet rs1 = executeQuery(query2.toString(),conn);
            if (rs1.next()) {
                String companyCode = NbaConstants.Product_COIL;
                String companyName = (String) tblCompany.get(companyCode);
               
                cashCompany = new NbaCashCompanyVO();
                cashCompany.setCompanyCode(companyCode);
                cashCompany.setCompanyName(companyName);
                cashCompany.setNumberOfBundles(rs1.getInt(BUNDLES));
                companies.add(cashCompany); 

            }
            //End : NBLXA-1908 Coil changes

        } catch (Exception se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally {
            close();
        }

        return companies;

    }

    /**
     * Retrieves all the company code's and corresponding name for the tables.
     * @return Map of companies by company code
     */
    protected Map loadCompanies() {
        Map tblCompany = new HashMap();
        Map tblKeys = new HashMap();
        NbaTableData[] xlatBackend = null;
        NbaTableData[] xlatCompany = null;
        NbaTableAccessor nta = new NbaTableAccessor();
        try {
            if (tblKeys != null) {
                tblKeys.put(NbaTableAccessConstants.C_SYSTEM_ID, NbaTableAccessConstants.WILDCARD);
                tblKeys.put(NbaTableAccessConstants.C_COMPANY_CODE, NbaTableAccessConstants.WILDCARD);
                tblKeys.put(NbaTableAccessConstants.C_COVERAGE_KEY, NbaTableAccessConstants.WILDCARD);
                xlatBackend = nta.getDisplayData(tblKeys, NbaTableConstants.NBA_BACK_END_SYSTEM);
                for (int i = 0; i < xlatBackend.length; i++) {
                    tblKeys.put(NbaTableAccessConstants.C_SYSTEM_ID, xlatBackend[i].code());
                    xlatCompany = nta.getDisplayData(tblKeys, NbaTableConstants.NBA_CASHIERING_COMPANY); //NBLXA-1908
                    for (int j = 0; j < xlatCompany.length; j++) {
                        tblCompany.put(xlatCompany[j].code(), xlatCompany[j].text());
                    }
                }
            }
        } catch (Exception e) {
            getLogger().logDebug("Couldn't retrieve any companies: " + e.getMessage());
        }
        return tblCompany;
    }

    /**
     * Executes a Select query on a new database connection.
     * @param query Select query to be executed
     * @param conn Connection object          
     * @return java.sql.ResultSet Results of the query
     * @throws NbaDataAccessException
     */
    public ResultSet executeQuery(String query, Connection conn) throws NbaDataAccessException {

        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("executing SQL query: " + query);
        }

       

        try {
            stmt = conn.createStatement();
            return executeQuery(stmt,query);
        } catch (Exception se) {
            getLogger().logError("SQLException thrown executing SQL query: " + query);
            throw new NbaDataAccessException(SQL_ERROR, se);
        }

    }

    public ResultSet executeQuery(Statement statement, String query) throws NbaDataAccessException {
    	 ResultSet rs = null;
    	try {
            rs = statement.executeQuery(query);
            return rs;
        } catch (Exception se) {
            getLogger().logError("SQLException thrown executing SQL query: " + query);
            throw new NbaDataAccessException(SQL_ERROR, se);
        }
        
    }
    /**
     * Retrieve a list of bundle that are open for deposit.
     * @param companyList list of companies
     * @return bundles the list of bundles
     * @throws NbaBaseException
     * @throws NbaDataAccessException
     */
    public List getOpenBundles(List companyList) throws NbaBaseException, NbaDataAccessException {

        int count = companyList.size();

        NbaCashCompanyVO companyVO = null;

        List checkList = new ArrayList(10); // SPR3290

        Map tblCompany = loadCompanies();

        //Connection conn = null; PERF-APSL634

        try {
        	conn = NbaConnectionManager.borrowConnection(NbaConfigurationConstants.CASHIERING); //PERF-APSL634
        	stmt = conn.createStatement(); //PERF-APSL634
            for (int i = 0; i < count; i++) {

                companyVO = new NbaCashCompanyVO();

                companyVO = (NbaCashCompanyVO) companyList.get(i);

                //Begin NBA228
                StringBuffer query = new StringBuffer("SELECT T1.TECHNICAL_KEY, T1.BUNDLE_ID, T1.INCLUDE_IND, T1.CHECK_AMT, ");//NBA069
                query.append("T1.REVISED_CHECK_AMT, T1.SCAN_STATION_ID, NBA_CONTRACTS_CHECKS.COMPANY, ");
                query.append("NBA_CONTRACTS_CHECKS.APPLIED_IND, NBA_CONTRACTS_CHECKS.REJECT_IND, ");
                query.append("NBA_CONTRACTS_CHECKS.RETURNED_IND, NBA_CONTRACTS_CHECKS.RESCANNED_IND, "); // APSL4624
                query.append("CASE NVL(NBA_BUNDLES.BUNDLE_ID, 'Y') WHEN 'Y' THEN 'Y' ELSE 'N' END CLOSE_BUNDLE, ");
                query.append("NBA_BUNDLE_LOCK.USER_ID, T3.TRANSLATION, NBA_BUNDLES.BUNDLE_CREATE_TIME, "); // APSL4513
                query.append("NBA_CONTRACTS_CHECKS.BACKEND_SYSTEM "); // NBLXA-1908
                query.append("FROM NBA_CHECKS T1 LEFT OUTER JOIN NBA_BUNDLES ON T1.BUNDLE_ID = NBA_BUNDLES.BUNDLE_ID, ");
                query.append("NBA_CHECKS T2 LEFT OUTER JOIN NBA_BUNDLE_LOCK ON T2.BUNDLE_ID = NBA_BUNDLE_LOCK.BUNDLE_ID, ");
                query.append("NBA_CONTRACTS_CHECKS, NBADATA.SCAN_STATION T3 "); 
                query.append("WHERE T1.TECHNICAL_KEY = NBA_CONTRACTS_CHECKS.TECHNICAL_KEY AND T1.DEPOSIT_TIME IS NULL ");
                query.append("AND T2.TECHNICAL_KEY = NBA_CONTRACTS_CHECKS.TECHNICAL_KEY AND T2.DEPOSIT_TIME IS NULL ");
                query.append("AND NBA_CONTRACTS_CHECKS.PRIMARY_CONTRACT_IND = 'Y' "); // NBA182
                if(companyVO.getCompanyCode().equalsIgnoreCase(NbaConstants.Product_COIL)){ //NBLXA-1908
                    query.append(" And nba_contracts_checks.backend_system = 'ADS'");                	
                }else{
                    query.append(" And nba_contracts_checks.backend_system <> 'ADS'");
                    query.append("AND " + formatSQLWhereCriterion("NBA_CONTRACTS_CHECKS.COMPANY", companyVO.getCompanyCode()));
                }
                query.append(" AND ( T1.SCAN_STATION_ID = T3.SCAN_STATION_ID OR (T3.SCAN_STATION_ID = '*' AND ");
                query.append(" (SELECT COUNT(T4.SCAN_STATION_ID) FROM NBADATA.SCAN_STATION T4 WHERE T4.SCAN_STATION_ID = T1.SCAN_STATION_ID) = 0)) ");
                //End NBA228

                //PERf-APSL634 code deleted
                ResultSet rs = executeQuery(stmt, query.toString()); //PERF-APS634
                NbaCashCheckVO cashCheck = null; // SPR3290
                String companyName = ""; // SPR3290
                while (rs.next()) {

                    cashCheck = new NbaCashCheckVO(); // SPR3290
                    companyName = (String) tblCompany.get(NbaUtils.trim(rs.getString(COMPANY))); // SPR3290
                    cashCheck.setTechnicalKey(rs.getLong(TECHNICAL_KEY));
                    cashCheck.setBundleID(NbaUtils.trim(rs.getString(BUNDLE_ID)));
                    cashCheck.setIncludedInd(stringToBoolean(rs.getString(INCLUDE_IND)));
                    cashCheck.setRejectedInd(stringToBoolean(rs.getString(REJECT_IND)));
                    cashCheck.setCheckAmount(rs.getBigDecimal(CHECK_AMT));
                    cashCheck.setCompany(NbaUtils.trim(rs.getString(COMPANY)));
                    if(companyVO.getCompanyCode().equalsIgnoreCase(NbaConstants.Product_COIL)){//NBLXA-1908
                    	cashCheck.setCompanyName(companyVO.getCompanyName());
                    } else {
                    	cashCheck.setCompanyName(companyName);
                    }
                    cashCheck.setScanStation(rs.getString(TRANSLATION)); //NBA228
                    cashCheck.setAppliedInd(stringToBoolean(rs.getString(APPLIED_IND)));
                    cashCheck.setRevisedCheckAmt(rs.getBigDecimal(REVISED_CHECK_AMT));//NBA069
                    cashCheck.setCloseBundleInd(stringToBoolean(rs.getString(CLOSE_BUNDLE)));//NBA228
                    cashCheck.setLockedUser(rs.getString(USER_ID)); //NBA228
                    cashCheck.setBundleCreateTime(rs.getTimestamp(BUNDLE_CREATE_TIME)); // APSL4513
                    cashCheck.setScanStationId(rs.getString(SCAN_STATION_ID)); //APSL4513
                    cashCheck.setReturnedInd(stringToBoolean(rs.getString(RETURNED_IND))); //APSL4624
                    cashCheck.setRescannedInd(stringToBoolean(rs.getString(RESCANNED_IND))); //APSL4624
                    cashCheck.setBackendSystem(rs.getString(BACKEND_SYSTEM)); //NBLXA-1908
                    checkList.add(cashCheck);
                }

            }
        } catch (Exception se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally {
            close();
        }

        // return the list
        return checkList;

    }

    /**
     * Retrieves all the company code's and corresponding name for the tables.
     * @return Map of companies by company code
     */
    protected Map loadScanStation() {
        Map tblCompany = new HashMap();
        Map tblKeys = new HashMap();
        NbaTableData[] xlatBackend = null;
        NbaTableData[] xlatCompany = null;
        NbaTableAccessor nta = new NbaTableAccessor();
        try {
            if (tblKeys != null) {
                tblKeys.put(NbaTableAccessConstants.C_SYSTEM_ID, NbaTableAccessConstants.WILDCARD);
                tblKeys.put(NbaTableAccessConstants.C_COMPANY_CODE, NbaTableAccessConstants.WILDCARD);
                tblKeys.put(NbaTableAccessConstants.C_COVERAGE_KEY, NbaTableAccessConstants.WILDCARD);
                xlatBackend = nta.getDisplayData(tblKeys, NbaTableConstants.NBA_BACK_END_SYSTEM);
                for (int i = 0; i < xlatBackend.length; i++) {
                    tblKeys.put(NbaTableAccessConstants.C_SYSTEM_ID, xlatBackend[i].code());
                    xlatCompany = nta.getDisplayData(tblKeys, NbaTableConstants.NBA_COMPANY);
                    for (int j = 0; j < xlatCompany.length; j++) {
                        tblCompany.put(xlatCompany[j].code(), xlatCompany[j].text());
                    }
                }
            }
        } catch (Exception e) {
            getLogger().logDebug("Couldn't retrieve any companies: " + e.getMessage());
        }
        return tblCompany;
    }

    /**
     * This method is used to get the number of checks for the guven bundle
     * @param bundleID bundleID for which the number of checks to be determined
     * @param conn Connection Object
     * @return checkCount number of checks
     * @throws NbaBaseException
     * @throws NbaDataAccessException
     */
    private int getCheckCount(String bundleID, Connection conn) throws NbaBaseException, NbaDataAccessException {
        int checkCount = 0;
        StringBuffer query = new StringBuffer("select count(*) as CHECK_COUNT from nba_checks ");//NBA069
        query.append(" where " + formatSQLWhereCriterion(BUNDLE_ID, bundleID));
        query.append(" and deposit_time is null ");
        query.append(" and include_ind='Y' ");

        try {

            ResultSet rs = executeQuery(query.toString(), conn);
            while (rs.next()) {
                checkCount = rs.getInt(CHECK_COUNT);
            }
        } catch (Exception se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally {
            close();
        }
        return checkCount;
    }

    /**
     * This method is used to get the list of checks for the bundles selected from the bundle summary table
     * @param bundleList list of selected bundles
     * @return updatedBundleList list of checks
     * @throws NbaBaseException
     * @throws NbaDataAccessException
     */
    public List getChecksforBundles(List bundleList) throws NbaBaseException, NbaDataAccessException {

        int count = bundleList.size();

        List updatedBundleList = new ArrayList(10);

        NbaCashCheckVO cashCheck = null;

        NbaCashBundleVO cashBundle = null;

        List checkList = null; // SPR3290

        Map tblCompany = loadCompanies();

        //Connection conn = null; PERF-APSL634
        try {
        	conn = NbaConnectionManager.borrowConnection(NbaConfigurationConstants.CASHIERING); //PERF-APSL634
        	stmt = conn.createStatement(); //PERF-APSL634
            for (int i = 0; i < count; i++) {
                cashBundle = new NbaCashBundleVO();
                checkList = new ArrayList(10);
                cashBundle = (NbaCashBundleVO) bundleList.get(i);
                StringBuffer query = new StringBuffer("SELECT NBA_CHECKS.TECHNICAL_KEY, NBA_CHECKS.BUNDLE_ID, NBA_CONTRACTS_CHECKS.COMPANY, ");
                query.append("NBA_CONTRACTS_CHECKS.CONTRACT_NUMBER, NBA_CHECKS.INCLUDE_IND, NBA_CONTRACTS_CHECKS.APPLIED_IND, NBA_CONTRACTS_CHECKS.SYSTEM_APPLIED_IND, "); // SR615900
                query.append("NBA_CONTRACTS_CHECKS.REJECT_IND,NBA_CONTRACTS_CHECKS.PRIMARY_CONTRACT_IND,NBA_CONTRACTS_CHECKS.PRIMARY_INSURED_NAME,");
                query.append("NBA_CONTRACTS_CHECKS.AMOUNT_APPLIED, NBA_CHECKS.CHECK_NUMBER, NBA_CHECKS.CHECK_AMT, NBA_CHECKS.REVISED_CHECK_AMT, ");
                query.append("NBA_CHECKS.CHECK_LAST_NAME, NBA_CONTRACTS_CHECKS.RESCANNED_IND, NBA_CONTRACTS_CHECKS.RETURNED_IND, NBA_CONTRACTS_CHECKS.BACKEND_SYSTEM "); // APSL4513,NBLXA-1908
                query.append("FROM NBA_CHECKS, NBA_CONTRACTS_CHECKS");

                query.append(" WHERE nba_checks.technical_key = nba_contracts_checks.technical_key AND nba_contracts_checks.technical_key IN");
                query.append("(SELECT nba_checks.technical_key FROM nba_checks, nba_contracts_checks WHERE nba_checks.deposit_time is NULL");
                query.append(" AND ");
                query.append(formatSQLWhereCriterion(COMPANY, cashBundle.getCompany()));
                query.append(" AND ");
                query.append(formatSQLWhereCriterion("NBA_CHECKS.BUNDLE_ID", cashBundle.getBundleID()));
                query.append(" AND ");
                query.append(formatSQLWhereCriterion(PRIMARY_CONTRACT_IND, true));
                query.append(" AND ");
                if(cashBundle.getBackendSystem().equalsIgnoreCase("ADS")){//NBLXA-1908
                	query.append(formatSQLWhereCriterion(BACKEND_SYSTEM, cashBundle.getBackendSystem()));
                } else{
                	query.append(formatSQLWhereNotCriterion(BACKEND_SYSTEM, cashBundle.getBackendSystem()));
                }
                query.append(") ORDER BY NBA_CHECKS.CHECK_AMT , NBA_CHECKS.TECHNICAL_KEY, PRIMARY_CONTRACT_IND desc");//ALS4817, APSL359

                //PERF-APSL634 code deleted
                
                ResultSet rs = executeQuery(stmt, query.toString()); //PERF-APSL634
                while (rs.next()) {
                    cashCheck = new NbaCashCheckVO();
                    String companyName = (String) tblCompany.get(NbaUtils.trim(rs.getString(COMPANY)));
                    cashCheck.setTechnicalKey(rs.getLong(TECHNICAL_KEY));
                    cashCheck.setCompany(NbaUtils.trim(rs.getString(COMPANY)));
                    cashCheck.setCompanyName(companyName);
                    cashCheck.setBundleID(NbaUtils.trim(rs.getString(BUNDLE_ID)));
                    cashCheck.setContractNumber(rs.getString(CONTRACT_NUMBER));
                    cashCheck.setIncludedInd(stringToBoolean(rs.getString(INCLUDE_IND)));
                    cashCheck.setOldincludedInd(stringToBoolean(rs.getString(INCLUDE_IND)));
                    cashCheck.setAppliedInd(stringToBoolean(rs.getString(APPLIED_IND)));
                    cashCheck.setOldappliedInd(stringToBoolean(rs.getString(APPLIED_IND)));
                    cashCheck.setRejectedInd(stringToBoolean(rs.getString(REJECT_IND)));
                    cashCheck.setOldrejectedInd(stringToBoolean(rs.getString(REJECT_IND)));
                    cashCheck.setPrimaryContractInd(stringToBoolean(rs.getString(PRIMARY_CONTRACT_IND)));
                    cashCheck.setPrimaryInsuredName(NbaUtils.trim(rs.getString(PRIMARY_INSURED_NAME)));
                    cashCheck.setAppliedAmount(rs.getBigDecimal(AMOUNT_APPLIED));
                    cashCheck.setOldappliedAmount(rs.getBigDecimal(AMOUNT_APPLIED));
                    cashCheck.setCheckNumber(rs.getString(CHECK_NUMBER));
                    cashCheck.setCheckAmount(rs.getBigDecimal(CHECK_AMT));
                    cashCheck.setOldcheckAmount(rs.getBigDecimal(CHECK_AMT));
                    cashCheck.setRevisedCheckAmt(rs.getBigDecimal(REVISED_CHECK_AMT));//NBA069
                    cashCheck.setCheckName(rs.getString(CHECK_LAST_NAME));
                    cashCheck.setSystemAppliedInd(stringToBoolean(rs.getString(SYSTEM_APPLIED_IND))); // SR615900
                    // APSL4513 Begin
                    cashCheck.setRescannedInd(stringToBoolean(rs.getString(RESCANNED_IND))); 
                    cashCheck.setOldRescannedInd(stringToBoolean(rs.getString(RESCANNED_IND)));
                    cashCheck.setReturnedInd(stringToBoolean(rs.getString(RETURNED_IND)));
                    cashCheck.setOldReturnedInd(stringToBoolean(rs.getString(RETURNED_IND)));
                    // APSL4513 End
                    cashCheck.setBackendSystem(rs.getString(BACKEND_SYSTEM));//NBLXA-1908
                    updateIndicators(cashCheck);

                    checkList.add(cashCheck);
                    //}

                }
                cashBundle.setChecks(checkList);
                updatedBundleList.add(cashBundle);

            }
        } catch (Exception se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally { //PERF-APSL634
        	close(); //PERF-APSL634
        } //PERF-APSL634

        return updatedBundleList;
    }

    /**
     * @param cashCheck
     */
    private void updateIndicators(NbaCashCheckVO cashCheck) {
		if (cashCheck.isPrimaryContractInd()) {
			if (cashCheck.isExcludeInd() && cashCheck.isRejectedInd()) {
				cashCheck.setDisableAppliedInd(true);
				cashCheck.setDisableExcludeInd(false);
				cashCheck.setDisableRejectInd(false);
			}
			if (cashCheck.isAppliedInd()) {
				cashCheck.setDisableAppliedInd(false);
				cashCheck.setDisableExcludeInd(true);
				cashCheck.setDisableRejectInd(true);
			} else {
				cashCheck.setDisableAppliedInd(false);
				cashCheck.setDisableExcludeInd(false);
				cashCheck.setDisableRejectInd(false);
			}
		} else {
			if (cashCheck.isAppliedInd()) {
				cashCheck.setDisableAppliedInd(false);
				cashCheck.setDisableRejectInd(true);
			} else if (cashCheck.isRejectedInd()) {
				cashCheck.setDisableAppliedInd(true);
				cashCheck.setDisableRejectInd(false);
			} else {
				cashCheck.setDisableAppliedInd(false);
				cashCheck.setDisableRejectInd(false);
			}

		}

    }

    /**
	 * @param bundleVO
	 * @return
	 * @throws NbaDataAccessException
	 */
    public boolean verifyChecks(NbaCashBundleVO bundleVO) throws NbaDataAccessException {
        String company = bundleVO.getCompany();
        String bundleID = bundleVO.getBundleID();

        StringBuffer query = new StringBuffer(
                "SELECT DISTINCT NBA_CONTRACTS_CHECKS.TECHNICAL_KEY FROM NBA_CONTRACTS_CHECKS,NBA_CHECKS WHERE NBA_CONTRACTS_CHECKS.TECHNICAL_KEY IN");

        query.append("(SELECT technical_key FROM nba_checks WHERE deposit_time is NULL)");
        query.append(" AND " + formatSQLWhereCriterion("NBA_CONTRACTS_CHECKS.company", company));
        query.append(" AND " + formatSQLWhereCriterion("NBA_CONTRACTS_CHECKS.bundle_id", bundleID));
        query.append(" AND " + formatSQLWhereCriterion("NBA_CONTRACTS_CHECKS.reject_ind", false));
        // APSL4624 Begin
        query.append(" AND (" + formatSQLWhereCriterion("NBA_CONTRACTS_CHECKS.RETURNED_IND", false));        
        query.append(" OR " + formatSQLWhereCriterion("NBA_CONTRACTS_CHECKS.RETURNED_IND", (String)null));
        query.append(") ");
        if (!NbaConstants.SCAN_STATION_EAPPACH.equals(bundleVO.getScanStationId())) {
        	query.append(" AND (" + formatSQLWhereCriterion("NBA_CONTRACTS_CHECKS.RESCANNED_IND", false));
        	query.append(" OR " + formatSQLWhereCriterion("NBA_CONTRACTS_CHECKS.RESCANNED_IND", (String)null));
            query.append(") ");
        }
        // APSL4624 End

        try {
            conn = NbaConnectionManager.borrowConnection(NbaConfigurationConstants.CASHIERING);
            ResultSet rs = executeQuery(query.toString(), conn);
            return (!rs.next());

        } catch (Exception se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally {
            close();
        }

        /*
         * SELECT DISTINCT NBA_CONTRACTS_CHECKS.TECHNICAL_KEY FROM NBA_CONTRACTS_CHECKS,NBA_CHECKS WHERE NBA_CONTRACTS_CHECKS.TECHNICAL_KEY in 
         * (select technical_key from nba_checks where deposit_time is null) and NBA_CONTRACTS_CHECKS.company ='00' and 
         * NBA_CONTRACTS_CHECKS.bundle_id='0000000002' and NBA_CONTRACTS_CHECKS.reject_ind='N';
         */

        /*StringBuffer query = new StringBuffer(
         "SELECT NBA_CHECKS.TECHNICAL_KEY, NBA_CHECKS.BUNDLE_ID, NBA_CONTRACTS_CHECKS.COMPANY, NBA_CONTRACTS_CHECKS.CONTRACT_NUMBER, NBA_CHECKS.INCLUDE_IND, NBA_CONTRACTS_CHECKS.APPLIED_IND, NBA_CONTRACTS_CHECKS.REJECT_IND, NBA_CONTRACTS_CHECKS.PRIMARY_CONTRACT_IND, NBA_CONTRACTS_CHECKS.PRIMARY_INSURED_NAME, NBA_CONTRACTS_CHECKS.AMOUNT_APPLIED, NBA_CHECKS.CHECK_NUMBER, NBA_CHECKS.CHECK_AMT, NBA_CHECKS.REVISED_CHECK_AMT FROM NBA_CHECKS, NBA_CONTRACTS_CHECKS");//NBA069

         query.append(" WHERE nba_checks.technical_key = nba_contracts_checks.technical_key AND nba_contracts_checks.technical_key IN");
         query.append("(SELECT nba_checks.technical_key FROM nba_checks, nba_contracts_checks WHERE nba_checks.deposit_time is NULL");
         query.append(" AND " + formatSQLWhereCriterion(COMPANY, cashBundle.getCompany()));
         query.append(" AND " + formatSQLWhereCriterion("NBA_CHECKS.BUNDLE_ID", cashBundle.getBundleID()));
         query.append(" AND " + formatSQLWhereCriterion(PRIMARY_CONTRACT_IND, true));
         query.append(") ORDER BY nba_checks.technical_key");
         }*/
    }

    /**
     * @return
     */
    public List getCashieringData() throws NbaDataAccessException {
       
        
        //StringBuffer query = new StringBuffer("select company,COUNT(bundle_id)AS BUNDLES from nba_bundles GROUP BY COMPANY ");

        StringBuffer query1 = new StringBuffer(
                "SELECT nba_checks.technical_key, nba_checks.bundle_id, nba_checks.include_ind, nba_checks.check_amt, ");

        query1.append("nba_checks.revised_check_amt, nba_checks.SCAN_STATION_ID,nba_contracts_checks.company,");

        query1.append("nba_contracts_checks.applied_ind from nba_checks, nba_contracts_checks WHERE nba_checks.technical_key =");

        query1.append("nba_contracts_checks.technical_key AND nba_checks.deposit_time is null");

        List companies = new ArrayList(10); // SPR3290

        //PERF-APSL634 code deleted

        try {
            conn = NbaConnectionManager.borrowConnection(NbaConfigurationConstants.CASHIERING);
            ResultSet rs = executeQuery(query1.toString(), conn);
            while (rs.next()) {
                NbaCashCompanyVO cashCompany = new NbaCashCompanyVO();
                companies.add(cashCompany);
                break;

            }

        } catch (Exception se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally {
            close();
        }

        return companies;
    }

	/**
	 * Determine if a bundle is locked by another user.
	 * @param bundleData - bundle values
	 * @return true if there is a matching row in the NBA_BUNDLE_LOCK table
	 * @throws NbaDataAccessException
	 */
    //NBA228 new method
	public boolean isLocked(NbaLockBundleVO bundleData) throws NbaDataAccessException {

		StringBuffer query = new StringBuffer("SELECT BUNDLE_ID, USER_ID FROM NBA_BUNDLE_LOCK WHERE "
				+ formatSQLWhereCriterion("BUNDLE_ID", bundleData.getBundleId()));

		//PERF-APSL634 code deleted

		try {
			conn = NbaConnectionManager.borrowConnection(NbaConfigurationConstants.CASHIERING);
			ResultSet rs = executeQuery(query.toString(), conn);
			if (rs.next()) {
				if (bundleData.getUserId().equalsIgnoreCase(rs.getString("USER_ID"))) {
					bundleData.setLockedForUser(true);
				} else {
					return true;
				}
			}

		} catch (Exception ex) {
			getLogger().logException(ex);
			throw new NbaDataAccessException(SQL_ERROR, ex);
		} finally {
			close();
		}
		return false;
	}
    /**
     * This method locks the specified bundle so that any other user is not able to work on this bundle. It adds a row into NBA_BUNDLE_LOCK table.
     * @param bundle the bundle details
     * @throws NbaDataAccessException          
     */
     //NBA228 new method
    public void lockBundle(NbaLockBundleVO bundle) throws NbaDataAccessException {

        StringBuffer query = new StringBuffer("INSERT INTO NBA_BUNDLE_LOCK (BUNDLE_ID, USER_ID) VALUES (");
        query.append(formatSQLValue(bundle.getBundleId()));
        query.append("," + formatSQLValue(bundle.getUserId()) + ")");

        executeInsert(query.toString());
    }	

    /**
     * This method unlocks the specified bundle locked by a user. It removes the row for the bundle from NBA_BUNDLE_LOCK table.  
     * @param bundle the bundle details
     * @throws NbaDataAccessException          
     */
    //NBA228 new method
    public void unlockBundle(NbaLockBundleVO bundle) throws NbaDataAccessException {

        StringBuffer query = new StringBuffer("DELETE FROM NBA_BUNDLE_LOCK");
        query.append(" WHERE " + formatSQLWhereCriterion(BUNDLE_ID, bundle.getBundleId()));
        query.append(" AND " + formatSQLWhereCriterion(USER_ID, bundle.getUserId()));

        executeDelete(query.toString());
    }
    
	/**
	 * Determine if a bundle is locked by current user.
	 * @param bundleData - bundle values
	 * @return true if there is a matching row in the NBA_BUNDLE_LOCK table
	 * @throws NbaDataAccessException
	 */
    //NBA228 new method
	public boolean determineUserLock(NbaLockBundleVO bundleData) throws NbaDataAccessException {

		StringBuffer query = new StringBuffer("SELECT BUNDLE_ID, USER_ID FROM NBA_BUNDLE_LOCK");
		query.append(" WHERE "+ formatSQLWhereCriterion("BUNDLE_ID", bundleData.getBundleId()));
		query.append(" AND "+ formatSQLWhereCriterion("USER_ID", bundleData.getUserId()));

		//PERF-APSL634 code deleted

		try {
			conn = NbaConnectionManager.borrowConnection(NbaConfigurationConstants.CASHIERING);
			ResultSet rs = executeQuery(query.toString(), conn);
			if (rs.next()) {
				return true;
			}			
		} catch (Exception ex) {
			getLogger().logException(ex);
			throw new NbaDataAccessException(SQL_ERROR, ex);
		} finally {
			close();
		}
		return false;
	}
	
	// APSL4513 New method
    public void updateBundle(String bundleId, String oldBundleId) throws NbaBaseException, NbaDataAccessException {    	
		StringBuffer query = new StringBuffer();
		query.append(formatSQLUpdateValue(BUNDLE_ID, bundleId));
		query.append(" WHERE " + TECHNICAL_KEY);
		query.append(" IN (SELECT DISTINCT TECHNICAL_KEY FROM NBA_CONTRACTS_CHECKS WHERE ");
		query.append(formatSQLWhereCriterion(BUNDLE_ID, oldBundleId));
		query.append(" AND (");
		query.append(formatSQLWhereCriterion(APPLIED_IND, false));
		query.append(" OR ");
		query.append(formatSQLWhereCriterion(APPLIED_IND, (String)null));		
		query.append(") AND (");
		query.append(formatSQLWhereCriterion(RETURNED_IND, false));
		query.append(" OR ");
		query.append(formatSQLWhereCriterion(RETURNED_IND, (String)null));
		query.append("))");
		StringBuffer query1 = new StringBuffer("UPDATE NBA_CHECKS SET ");
		query1.append(query);
		executeUpdate(query1.toString());
		
		StringBuffer query2 = new StringBuffer("UPDATE NBA_CONTRACTS_CHECKS SET ");
		query2.append(query);
		executeUpdate(query2.toString());
	}
	
    // APSL4590 New method
    public List selectUpdateBundle(String bundleId) throws NbaBaseException, NbaDataAccessException {    	
		StringBuffer query = new StringBuffer();		
		query.append("SELECT DISTINCT CONTRACT_NUMBER FROM NBA_CONTRACTS_CHECKS WHERE ");
		query.append(formatSQLWhereCriterion(BUNDLE_ID, bundleId));
		query.append(" AND (");
		query.append(formatSQLWhereCriterion(APPLIED_IND, false));
		query.append(" OR ");
		query.append(formatSQLWhereCriterion(APPLIED_IND, (String)null));		
		query.append(") AND (");
		query.append(formatSQLWhereCriterion(RETURNED_IND, false));
		query.append(" OR ");
		query.append(formatSQLWhereCriterion(RETURNED_IND, (String)null));
		query.append(")");
		
		List aList = new ArrayList();
        try {
            ResultSet rs = executeQuery(query.toString());
            while (rs.next()) {
                aList.add(rs.getString(CONTRACT_NUMBER));
            }
        } catch (SQLException se) {
            throw new NbaDataAccessException(SQL_ERROR, se);
        } finally {
            close();
        }
        return aList;
	}
}
