package com.csc.fsg.nba.workflow.api.rest;

/*
 * *******************************************************************************<BR>
 * Copyright 2015, Computer Sciences Corporation. All Rights Reserved.<BR>
 *
 * CSC, the CSC logo, nbAccelerator, and csc.com are trademarks or registered
 * trademarks of Computer Sciences Corporation, registered in the United States
 * and other jurisdictions worldwide. Other product and service names might be
 * trademarks of CSC or other companies.<BR>
 *
 * Warning: This computer program is protected by copyright law and international
 * treaties. Unauthorized reproduction or distribution of this program, or any
 * portion of it, may result in severe civil and criminal penalties, and will be
 * prosecuted to the maximum extent possible under the law.<BR>
 * *******************************************************************************<BR>
 */

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.helpers.DefaultHandler;

import com.csc.fs.dataobject.accel.workflow.Field;
import com.csc.fs.dataobject.accel.workflow.LOB;
import com.csc.fs.dataobject.accel.workflow.LookupRequest;
import com.csc.fs.logging.LogHandler;
import com.csc.fs.svcloc.ServiceLocator;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.AWD.rest.GenericWorkflowLookupDefinition;
import com.tbf.xml.XmlString;

/**
 * WorkflowLookupRequestFormatter formats the query to be sent to the AWD 10 Stored Procedure
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @since New Business Accelerator - Version NB-1401
 */

public class WorkflowLookupRequestFormatter extends DefaultHandler {
    protected static final String POUND_SIGN = "#";
    protected static final String EXCLAMATION = "!";
    protected static final String X = "X";
    protected static final String ONE = "1";

    /**
     * FieldDefinition contains the Field definition values retrieve from AWD 10 
     */
    protected class FieldDefinition {
        private String decimals;
        private String formatType;
        private String length;
        private String mask;
        private String name;

        /**
         * Create a FieldDefinition instance from a Field
         * @param field
         */
        public FieldDefinition(Field field) {
            setName(field.getName());
            setFormatType(field.getFormatType());
            setLength(field.getLength());
            setDecimals(field.getDecimals());
            setMask(field.getMask());
            LogHandler.Factory.LogLowLevelDebug(this, toString());
        }

        /**
         * @return the decimals
         */
        protected String getDecimals() {
            return decimals;
        }

        /**
         * @return the formatType
         */
        protected String getFormatType() {
            return formatType;
        }

        /**
         * @return the length
         */
        protected String getLength() {
            return length;
        }

        /**
         * @return the mask
         */
        protected String getMask() {
            return mask;
        }

        /**
         * @return the name
         */
        protected String getName() {
            return name;
        }

        /**
         * @param value the decimals to set
         */
        protected void setDecimals(String value) {
            this.decimals = value;
        }

        /**
         * @param value the formatType to set
         */
        protected void setFormatType(String value) {
            this.formatType = value;
        }

        /**
         * @param value the length to set
         */
        protected void setLength(String value) {
            this.length = value;
        }

        /**
         * @param value the mask to set
         */
        protected void setMask(String value) {
            this.mask = value;
        }

        /**
         * @param value the name to set
         */
        protected void setName(String value) {
            this.name = value;
        }

        /**
         * Display myself
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return (new StringBuffer().append("name=").append(name).append(" formatType=").append(formatType).append(" length=").append(length)
                    .append(" decimals=").append(decimals).append("mask").append(mask)).toString();
        }
    }
    protected static final String ALPHANUMERIC = "Alphanumeric";   
    //NBA331.1 code deleted
    protected static final String DATE = "Date";
    //NBA331.1 code deleted
    protected static final String DOT = ".";
    protected static final String EMPTY_STRING = "";
    protected final static String EQUAL_OPERATOR = "=";
    //NBA331.1 code deleted
    protected static Map<String, FieldDefinition> fieldDefinitions = Collections.synchronizedMap(new HashMap<String, FieldDefinition>());
    protected static boolean fieldValuesInitialized = false;
    protected static final String FORMAT_1$ = "%1$";
    protected static final String FORMAT_1$S_2$S_3$S = "%1$s-%2$s-%3$s";
    protected static final String FORMAT_1$S_2$S_3$S_NODASHES = "%1$s%2$s%3$s";
    protected static final String FORMAT_1$S_SLASH_2$S = "%1$s/%2$s";
    protected static GenericWorkflowLookupDefinition genericWorkflowLookupDefinition = null;
    protected static final String GT_SYMBOL = ">";   
    protected static WorkflowLookupRequestFormatter instance = null;
    //NBA331.1 code deleted
    protected static final String IVDT = "IVDT";
    protected final static String LIKE_OPERATOR = "like";
    protected static final String LOCKSTAT = "LOCKSTAT";
    protected static final String LT_SYMBOL = "<";
    protected final static String MM_YYYY = "MM-YYYY";
    protected static final String NUMERIC = "Numeric";
    protected static final String NUMERIC_TEXT = "Numeric Text";
    protected static String procedureFormName;
    //NBA331.1 code deleted
    protected static final String QUEUECD = "QUEUECD";
    protected static final String SPACE = " ";
    protected static final String STATCD = "STATCD";
    protected static final String STRING_0 = "0";
    protected static final String STRING_9 = "9";
    protected static final String STRING_LC_S = "s";
    protected static final String TMST = "TMST";
    protected static final String UNITCD = "UNITCD";
    protected static String wildcardChars = null;
    protected final static String WORKFLOW_LOOKUP_DEFINITIONS = "/rest/GenericWorkflowLookupDefinition.xml";
    protected static final String WRKTYPE = "WRKTYPE";
    protected final static String YYYY_MM_DD = "YYYY-MM-DD";
    protected static final String CDATA = "<![CDATA[";
    protected static final String CDATA_END = "]]>";
    //begin NBA331.1 
    protected static final String DATAVALUE = "DATAVALUE";
    protected static final String DATANAME = "DATANAME";
    protected static final String CREATE_DATE_TIME = "CRDATTIM";
    protected static final String SUSPFLAG = "SUSPFLAG";
    protected static final String SUSP = "SUSP";    
    protected static String workItemTableAlias;
    protected static List<String> businessEntityColumnNames;
    protected static String businessEntityTableAlias;
    protected static String fieldValuesTableAlias;    
    protected static String delimiter;
    
    protected static final String RECORDCD = "RECORDCD";
    protected static final String RECO = "RECO";
    protected static final String CRNODE = "CRNODE";
    protected static final String CRNO = "CRNO";
    protected static final String OWNERNODE = "OWNERNODE";
    protected static final String OWNE = "OWNE";
    protected static final String PRTY = "PRTY";    
    protected static final String INCREASE = "INCREASE";
    protected static final String INCR = "INCR";
    protected static final String AMOUNTTYPE = "AMOUNTTYPE";
    protected static final String AMTT = "AMTT";
    protected static final String AMOUNT = "AMOUNT";
    protected static final String AMTV = "AMTV";  
    protected static final String VIFLAG = "VIFLAG";
    protected static final String VIFL = "VIFL";  
    protected static final String CRDA = "CRDA";
    protected static final String UNIT = "UNIT";
    protected static final String WRKT = "WRKT";
    protected static final String STAT = "STAT";
    protected static final String QUEU = "QUEU";
    protected static final String LOCK = "LOCK";

    
    
    
    
    
    //end NBA331.1    

    /**
     * @return the fieldDefinitions
     */
    protected static Map<String, FieldDefinition> getFieldDefinitions() {
        return fieldDefinitions;
    }

    /**
     * @return the genericWorkflowLookupDefinition
     */
    protected static GenericWorkflowLookupDefinition getGenericWorkflowLookupDefinition() {
        return genericWorkflowLookupDefinition;
    }

    /**
     * Answer the singleton reference to this class.
     * @return com.csc.fs.accel.workflow.api.rest.WorkflowLookupRequestFormatter
     */
    public static WorkflowLookupRequestFormatter getInstance() {
        if (instance == null) {
            instance = new WorkflowLookupRequestFormatter();
            try {
                wildcardChars = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.WILDCARD_CHARACTERS);
            } catch (NbaBaseException e) {
                LogHandler.Factory.LogError("WorkflowLookupRequestFormatter", NbaBaseException.CONFIGURATION_MISSING);
            }
        }
        return instance;
    }

 
 
    /**
     * @return Returns the wildcardChars.
     */
    protected static String getWildcardChars() {
        return wildcardChars;
    }

    /**
     * @return the wORKFLOW_LOOKUP_DEFINITIONS
     */
    protected static String getWORKFLOW_LOOKUP_DEFINITIONS() {
        return WORKFLOW_LOOKUP_DEFINITIONS;
    }

 
 
    /**
     * @param fieldDefinitions the fieldDefinitions to set
     */
    protected static void setFieldDefinitions(Map<String, FieldDefinition> map) {
        WorkflowLookupRequestFormatter.fieldDefinitions = map;
    }

    /**
     * @param value the genericWorkflowLookupDefinition to set
     */
    protected static void setGenericWorkflowLookupDefinition(GenericWorkflowLookupDefinition value) {
        WorkflowLookupRequestFormatter.genericWorkflowLookupDefinition = value;
    }

    /**
     * @param value the instance to set
     */
    protected static void setInstance(WorkflowLookupRequestFormatter value) {
        WorkflowLookupRequestFormatter.instance = value;
    }

 

    /**
     * WorkflowLookupRequestFormatter constructor.
     */
    WorkflowLookupRequestFormatter() {
        initialize();
    }

    /**
     * Add FieldDefinitions
     * @param fields
     */
    public void addFieldDefinition(List<Field> fields) {
        Iterator<Field> it = fields.iterator();
        while (it.hasNext()) {
            FieldDefinition def = new FieldDefinition(it.next());
            if (isNeeded(def)) {
                getFieldDefinitions().put(def.getName(), def);
            }
        }
    }

    /**
     * @return the fieldValuesInitialized
     */
    public boolean areFieldValuesInitialized() {
        return fieldValuesInitialized;
    }

    //NBA331.1 code deleted

    /**
     * Format the value.
     * @param name
     * @param value
     * @return
     */
    protected String formatValue(String name, String value) {
        String returnValue = value;
        if (getFieldDefinitions().containsKey(name)) {
            FieldDefinition def = getFieldDefinitions().get(name);
            try {
                if (DATE.equals(def.getFormatType())) {
                    if (YYYY_MM_DD.equals(def.getMask()) && value.length() == 8) {
                        returnValue = String.format(FORMAT_1$S_2$S_3$S, returnValue.substring(0, 4), returnValue.substring(4, 6), returnValue
                                .substring(6, 8));
                    }
                } else if (NUMERIC.equals(def.getFormatType())) {
                    int decimals = 0;
                    if (!def.getDecimals().isEmpty()) {
                        decimals = Integer.parseInt(def.getDecimals());
                    }
                    int decimalPointIdx = returnValue.indexOf(DOT);
                    returnValue = returnValue.replaceAll("\\.", "").replaceAll(",", "");
                    int rightPad = decimals;
                    String prefix = EMPTY_STRING;
                    String suffix = EMPTY_STRING;
                    if (decimalPointIdx < 0) {
                        rightPad = decimals;
                    } else {
                        rightPad = decimals - (returnValue.length() - decimalPointIdx);
                    }
                    if (rightPad > 0) {
                        suffix = String.format(FORMAT_1$ + rightPad + STRING_LC_S, STRING_0).replace(SPACE, STRING_0);
                    }
                    int leftPad = Integer.valueOf(def.getLength()) - returnValue.length() - suffix.length();
                    if (leftPad > 0) {
                        prefix = String.format(FORMAT_1$ + leftPad + STRING_LC_S, STRING_0).replace(SPACE, STRING_0);
                    }
                    returnValue = String.format(FORMAT_1$S_2$S_3$S_NODASHES, prefix, returnValue, suffix);
                } else if (NUMERIC_TEXT.equals(def.getFormatType())) {
                    if (def.getMask().indexOf(STRING_9) > -1) {
                        int leftPad = StringUtils.countMatches(def.getMask(), STRING_9);
                        returnValue = String.format(FORMAT_1$ + leftPad + STRING_LC_S, returnValue).replace(SPACE, STRING_0);
                    }
                } else if (ALPHANUMERIC.equals(def.getFormatType())) {
                    if (MM_YYYY.equals(def.getMask()) && returnValue.length() > 5) {
                        returnValue = String.format(FORMAT_1$S_SLASH_2$S, returnValue.substring(4, 6), returnValue.substring(0, 4));
                    }
                }
            } catch (Throwable e) {
                LogHandler.Factory.LogError(this, "Unable to format " + name + " value " + value, e);
            }
        }
        return returnValue;
    }

    //NBA331.1 code deleted

    /**
     *  Load and parse the  WorkflowLookupRequestDefinitions.xml
     *  Initialize variables from the Objects created from parsing.
     */
    protected void initialize() {
        try {
        	//begin NBA331.1
            setGenericWorkflowLookupDefinition(GenericWorkflowLookupDefinition.unmarshal(loadConfigurationFile()));            
            setProcedureFormName(getGenericWorkflowLookupDefinition().getProcedureFormName());
            setDelimiter(getGenericWorkflowLookupDefinition().getDelimiter()); 
            setBusinessEntityTableAlias(getGenericWorkflowLookupDefinition().getBusinessEntityTableAlias());
            setWorkItemTableAlias(getGenericWorkflowLookupDefinition().getWorkItemTableAlias());
            setFieldValuesTableAlias(getGenericWorkflowLookupDefinition().getFieldValuesTableAlias());
            List<XmlString> beColumns = getGenericWorkflowLookupDefinition().getBusinesEntityColumns().getBusinesEntityColumn();
            List<String> beColumnList = new ArrayList(beColumns.size());
            Iterator<XmlString> bit = beColumns.iterator();
            while (bit.hasNext()) {
                beColumnList.add(bit.next().getValue());
            }
            setBusinessEntityColumnNames(beColumnList);   
            //end NBA331.1          
        } catch (Throwable e) {
            LogHandler.Factory.LogError("WorkflowLookupRequestFormatter", "Initialization Failed", e);
        }
    }

    /**
     * Determine if a FieldDefinition is needed
     * @param def
     * @return
     */
    protected boolean isNeeded(FieldDefinition def) {
        if (DATE.equals(def.getFormatType())) {
            return true;
        }
        if (NUMERIC.equals(def.getFormatType())) {
            return true;
        }
        if (NUMERIC_TEXT.equals(def.getFormatType())) {
            return !(ONE.equals(def.getLength()));
        }
        if (ALPHANUMERIC.equals(def.getFormatType())) {
            if (ONE.equals(def.getLength())) {
                return false;
            }
            String mask = def.getMask().replaceAll(X, EMPTY_STRING).replaceAll(EXCLAMATION, EMPTY_STRING).replaceAll(POUND_SIGN, EMPTY_STRING);
            return mask.trim().length() > 0;
        }
        return false;
    }

    /**
     * Load WorkflowLookupRequestDefinitions.xml
     * @exception java.lang.Exception 
     */
    protected InputStream loadConfigurationFile() throws Exception {
        URL url = ServiceLocator.getConfigurationURL(WORKFLOW_LOOKUP_DEFINITIONS);
        if (url == null) {
            throw new NbaBaseException(NbaBaseException.CONFIGURATION_PARSE + SPACE + WORKFLOW_LOOKUP_DEFINITIONS + "  not found");
        }
        try {
            return url.openStream();
        } catch (Throwable t) {
            throw new NbaBaseException(NbaBaseException.CONFIGURATION_PARSE + " unable to load  " + WORKFLOW_LOOKUP_DEFINITIONS, t);
        }
    }

    /**
     * @param value the fieldValuesInitialized to set
     */
    protected void setFieldValuesInitialized(boolean value) {
        WorkflowLookupRequestFormatter.fieldValuesInitialized = value;
    }
 
	//NBA331.1 code deleted

    /**
     * @return the procedureFormName
     */
    protected  String getProcedureFormName() {  //NBA331.1
        return procedureFormName;
    }

    /**
     * @param procedureFormName the procedureFormName to set
     */
    protected static void setProcedureFormName(String value) {
        procedureFormName = value;
    }

    //NBA331.1 code deleted
    
    /**
     * Return businessEntityTableAlias
     */
    //NBA331.1 New Method
    protected static String getBusinessEntityTableAlias() {
        return businessEntityTableAlias;
    }

    /**
     * Set businessEntityTableAlias
     */
    //NBA331.1 New Method
    protected static void setBusinessEntityTableAlias(String value) {
        businessEntityTableAlias = value;
    }

    /**
     * Return workItemTableAlias
     */
    //NBA331.1 New Method
    protected static String getWorkItemTableAlias() {
        return workItemTableAlias;
    }

    /**
     * Set workItemTableAlias
     */
    //NBA331.1 New Method
    protected static void setWorkItemTableAlias(String value) {
        workItemTableAlias = value;
    }

    /**
     * Return fieldValuesTableAlias
     */
    //NBA331.1 New Method
    protected static String getFieldValuesTableAlias() {
        return fieldValuesTableAlias;
    }

    /**
     * Set fieldValuesTableAlias
     */
//    /NBA331.1 New Method
    protected static void setFieldValuesTableAlias(String value) {
        fieldValuesTableAlias = value;
    }

    /**
     * Return delimiter
     */
    //NBA331.1 New Method
    protected static String getDelimiter() {
        return delimiter;
    }

    /**
     * Set delimiter
     */
    //NBA331.1 New Method
    protected static void setDelimiter(String value) {
        delimiter = value;
    }
 
    /**
     * Set businessEntityColumnNames
     */
    //NBA331.1 New Method
    protected static void setBusinessEntityColumnNames(List<String> values) {
        businessEntityColumnNames = values;        
    }

    /**
     * Return businessEntityColumnNames
     */
    //NBA331.1 New Method    
    protected static List<String> getBusinessEntityColumnNames() {
        return businessEntityColumnNames;
    }
    
    /**
     * Update the LookupRequest with Strings containing the Table Names,
     * Column Names, Conditions, and Column Values to be used to 
     * locate the work items.
     * @param lookupReq
     * @return
     */
    //NBA331.1 New Method
    public void generateArgumentValues(LookupRequest lookupReq) {
        List<String> tableNames = new ArrayList<String>();
        List<String> columnNames = new ArrayList<String>();
        List<String> conditions = new ArrayList<String>();
        List<String> columnValues = new ArrayList<String>();
        lookupReq.setSql(getProcedureFormName());
        processNonFieldValues(lookupReq, tableNames, columnNames, conditions, columnValues);
        processBEAndFieldValues(lookupReq, tableNames, columnNames, conditions, columnValues);
        lookupReq.setTableNames(formatAsArgument(tableNames));
        lookupReq.setColumnNames(formatAsArgument(columnNames));
        lookupReq.setConditions(formatAsArgument(conditions));
        lookupReq.setColumnValues(formatAsArgument(columnValues));        
        lookupReq.setMaxRecords(lookupReq.getMaxRecords() + 1);
    }

    /**
     * Update the argument Lists for criteria in the W03U999S table.
     * Assure that the request contains at least the default Business Area. This value 
     * is required to allow the SP to fetch the Business Entity table name from WE5U999S table.
     * @param lookupReq 
     */
    //NBA331.1 New Method
    protected void processNonFieldValues(LookupRequest lookupReq, List tableNames, List columnNames, List conditions, List columnValues) {
        processNonFieldValues(getWorkItemTableAlias(), UNITCD, null, lookupReq.getBusinessArea(), tableNames, columnNames, conditions, columnValues);
        //begin NBA331.1
        if (tableNames.isEmpty()) { // Assure that the request contains at least the default Business Area.
            String businessArea = EMPTY_STRING;
            try {
                businessArea = NbaConfiguration.getInstance().getNetserverBusinessArea(NbaConstants.A_BA_NBA).getValue();
            } catch (NbaBaseException e) {
                e.printStackTrace();
            }
            processNonFieldValues(getWorkItemTableAlias(), UNITCD, null, businessArea, tableNames, columnNames, conditions, columnValues);
        }
        //end NBA331.1
        processNonFieldValues(getWorkItemTableAlias(), WRKTYPE, null, lookupReq.getWorkType(), tableNames, columnNames, conditions, columnValues);
        processNonFieldValues(getWorkItemTableAlias(), STATCD, null, lookupReq.getWorkStatus(), tableNames, columnNames, conditions, columnValues);
        processNonFieldValues(getWorkItemTableAlias(), QUEUECD, lookupReq.getQueueOperand(), lookupReq.getQueue(), tableNames, columnNames, conditions, columnValues); //Passed QueueOperand to fix issue for 'Active Only' option on search screen.
        processNonFieldValues(getWorkItemTableAlias(), LOCKSTAT, null, lookupReq.getLockStat(), tableNames, columnNames, conditions, columnValues);
        if (lookupReq.getBeginDateTime() != null) {
            processNonFieldValues(getWorkItemTableAlias(), CREATE_DATE_TIME, GT_SYMBOL, lookupReq.getBeginDateTime(), tableNames, columnNames,
                    conditions, columnValues);
        }
        if (lookupReq.getEndDateTime() != null) {
            processNonFieldValues(getWorkItemTableAlias(), CREATE_DATE_TIME, LT_SYMBOL, lookupReq.getEndDateTime(), tableNames, columnNames,
                    conditions, columnValues);
        }
        
        String suspendVlaue = handleSUSP(lookupReq); // NBA331.1
        processNonFieldValues(getWorkItemTableAlias(), SUSPFLAG, null, suspendVlaue, tableNames, columnNames, conditions, columnValues); // NBA331.1
                
      //NBA331 Start of Additional Lobs Code
        
        String crdattimValue = hanldeCRDA(lookupReq);
               
        if(crdattimValue != null)
        {
        	StringBuilder crdattimVal = new StringBuilder(crdattimValue);
        	crdattimVal.setCharAt(10, ' ');
        	crdattimVal.setCharAt(13, ':');
        	crdattimVal.setCharAt(16, ':');
        	processNonFieldValues(getWorkItemTableAlias(), CREATE_DATE_TIME, null ,crdattimVal.toString(), tableNames, columnNames, conditions, columnValues); // NBA331.1
        }
        String recordcdValue = hanldeRECORDCD(lookupReq);
        processNonFieldValues(getWorkItemTableAlias(), RECORDCD, null, recordcdValue, tableNames, columnNames, conditions, columnValues); // NBA331.1
        
        String crnodeValue = hanldeCRNODE(lookupReq);
        processNonFieldValues(getWorkItemTableAlias(), CRNODE, null, crnodeValue, tableNames, columnNames, conditions, columnValues); // NBA331.1
        
        String owneValue = hanldeOWNERNODE(lookupReq);
        processNonFieldValues(getWorkItemTableAlias(), OWNERNODE, null, owneValue, tableNames, columnNames, conditions, columnValues); // NBA331.1
   
        if(lookupReq.getBusinessArea() == null)
        {
        	String unitcdValue = hanldeUNITCD(lookupReq);
        	processNonFieldValues(getWorkItemTableAlias(), UNITCD, null, unitcdValue, tableNames, columnNames, conditions, columnValues); // NBA331.1
        }
        
        if(lookupReq.getWorkType() == null)
        {
        	String wrktypeValue = hanldeWRKTYPE(lookupReq);
        	processNonFieldValues(getWorkItemTableAlias(), WRKTYPE, null, wrktypeValue, tableNames, columnNames, conditions, columnValues); // NBA331.1
        }
        
        if(lookupReq.getWorkStatus() == null)
        {
        	String statcdValue = hanldeSTATCD(lookupReq);
        	processNonFieldValues(getWorkItemTableAlias(), STATCD, null, statcdValue, tableNames, columnNames, conditions, columnValues); // NBA331.1
        }
        if(lookupReq.getQueue() == null)
        {
        	String queuecdValue = hanldeQUEUECD(lookupReq);
        	processNonFieldValues(getWorkItemTableAlias(), QUEUECD, null, queuecdValue, tableNames, columnNames, conditions, columnValues); // NBA331.1
        }
        
        String prtyValue = hanldePRTY(lookupReq);
        processNonFieldValues(getWorkItemTableAlias(), PRTY, null, prtyValue, tableNames, columnNames, conditions, columnValues); // NBA331.1
        
        String incrValue = hanldeINCR(lookupReq);
        processNonFieldValues(getWorkItemTableAlias(), INCREASE, null, incrValue, tableNames, columnNames, conditions, columnValues); // NBA331.1
        
        if(lookupReq.getLockStat() == null)
        {
        	String lockValue = hanldeLOCKSTAT(lookupReq);
        	processNonFieldValues(getWorkItemTableAlias(), LOCKSTAT, null, lockValue, tableNames, columnNames, conditions, columnValues); // NBA331.1
        }
        
        String amttValue = hanldeAMOUNTTYPE(lookupReq);
        processNonFieldValues(getWorkItemTableAlias(), AMOUNTTYPE, null, amttValue, tableNames, columnNames, conditions, columnValues); // NBA331.1
        
        String amountValue = hanldeAMOUNT(lookupReq);
        processNonFieldValues(getWorkItemTableAlias(), AMOUNT, null, amountValue, tableNames, columnNames, conditions, columnValues); // NBA331.1
        
        String viflagValue = hanldeVIFLAG(lookupReq);
        processNonFieldValues(getWorkItemTableAlias(), VIFLAG, null, viflagValue, tableNames, columnNames, conditions, columnValues); // NBA331.1
      
        
        //NBA331.1 End
        
        
    }
    /**
     * Update the argument Lists for criteria which are Business Entities or Field Values
     */
    //NBA331.1 New Method
    protected void processBEAndFieldValues(LookupRequest lookupReq, List tableNames, List columnNames, List conditions, List columnValues) {
        if (lookupReq.getLobData() != null) {
            List<LOB> fieldValues = lookupReq.getLobData();
            Iterator<LOB> it = fieldValues.iterator();
            while (it.hasNext()) {
                LOB aLob = it.next();
                String name = aLob.getName();
                String value = formatValue(name, aLob.getValue());
                if (getBusinessEntityColumnNames().contains(name)) {
                    processNonFieldValues(getBusinessEntityTableAlias(), name, null, value, tableNames, columnNames, conditions, columnValues);
                } else {
                    processFieldValues(getFieldValuesTableAlias(), name, null, value, tableNames, columnNames, conditions, columnValues);
                }
            }
            if (lookupReq.getInterviewFromDate() != null) {
                String value = formatValue(IVDT, lookupReq.getInterviewFromDate());
                processFieldValues(getFieldValuesTableAlias(), IVDT, GT_SYMBOL, value, tableNames, columnNames, conditions, columnValues);
            }
            if (lookupReq.getInterviewToDate() != null) {
                String value = formatValue(IVDT, lookupReq.getInterviewToDate());
                processFieldValues(getFieldValuesTableAlias(), IVDT, LT_SYMBOL, value, tableNames, columnNames, conditions, columnValues);
            }
            if (lookupReq.getTimestampFromDate() != null) {
                String value = formatValue(TMST, lookupReq.getTimestampFromDate());
                processFieldValues(getFieldValuesTableAlias(), TMST, GT_SYMBOL, value, tableNames, columnNames, conditions, columnValues);
            }
            if (lookupReq.getTimestampToDate() != null) {
                String value = formatValue(TMST, lookupReq.getTimestampToDate());
                processFieldValues(getFieldValuesTableAlias(), TMST, LT_SYMBOL, value, tableNames, columnNames, conditions, columnValues);
            }
        }
    }

    /**
     * For a Field Value, add 2 entries. The first for the "DATANAME" column, the second for the "DATAVALUE" column.
     * @param tableAlias
     * @param name
     * @param condition
     * @param agrValue
     */
    //NBA331.1 New Method
    protected void processFieldValues(String tableAlias, String name, String condition, String agrValue, List tableNames, List columnNames, List conditions, List columnValues) {
        if (agrValue != null && agrValue.trim().length() > 0) {
            tableNames.add(tableAlias);
            columnNames.add(DATANAME);
            conditions.add(EQUAL_OPERATOR);
            columnValues.add(name);
            String val = agrValue.trim();
            tableNames.add(tableAlias);
            columnNames.add(DATAVALUE);
            if (condition != null) {
                conditions.add(condition);
            } else {
                conditions.add(getCondition(val));
            }
            columnValues.add(val);
        }
    }

    /**
     * Add entries to the Lists for arguments which are not Field Values.
     * @param tableAlias
     * @param argName
     * @param condition
     * @param agrValue
     */
    //NBA331.1 New Method
    protected void processNonFieldValues(String tableAlias, String argName, String condition, String agrValue, List tableNames, List columnNames, List conditions, List columnValues) {
        if (agrValue != null && agrValue.trim().length() > 0) {
            String val = agrValue.trim();
            tableNames.add(tableAlias);
            columnNames.add(argName);
            if (condition != null) {
                conditions.add(condition);
            } else {
                conditions.add(getCondition(val));
            }
            columnValues.add(val);
        }
    }

    /**
     * Convert the values List into a delimited String surrounded by <![CDATA[ ]]>
     * @param values List of values
     * @return the String
     */
    //NBA331.1 New Method
    protected String formatAsArgument(List<String> values) {
        StringBuffer buff = new StringBuffer();
        Iterator it = values.iterator();
        while (it.hasNext()) {
            if (buff.length() > 0) {
                buff.append(getDelimiter());
            }
            buff.append(it.next());
        }
        return new StringBuffer().append(CDATA).append(buff).append(CDATA_END).toString();
    }   
    
    /**
     * Handle the wild card characters and set the appropriate condition operand      
     */
    //NBA331.1 New Method
    protected String getCondition(String value) {
        String operand = EQUAL_OPERATOR;
        for (int i = 0; i < getWildcardChars().length(); i++) {
            if (value.indexOf(getWildcardChars().charAt(i)) != -1) {
                operand = LIKE_OPERATOR;
                break;
            }
        }
        return operand;
    }
    /**
     * Remove "SUSP" from the field values and return its value      
     */    
    //NBA331.1 New Method
    protected String handleSUSP(LookupRequest lookupReq) {
        String value = null;
        if (lookupReq.getLobData() != null) {
            List<LOB> fieldValues = lookupReq.getLobData();
            Iterator<LOB> it = fieldValues.iterator();
            while (it.hasNext()) {
                LOB aLob = it.next();
                String name = aLob.getName();
                if (SUSP.equals(name)) {
                    value = formatValue(name, aLob.getValue());
                    it.remove();
                    break;
                }
            }
        }
        return value;
    }
    
  //NBA331 Start of Additional LOB code
    protected String hanldeCRDA(LookupRequest lookupReq) {
        String value = null;
        if (lookupReq.getLobData() != null) {
            List<LOB> fieldValues = lookupReq.getLobData();
            Iterator<LOB> it = fieldValues.iterator();
            while (it.hasNext()) {
                LOB aLob = it.next();
                String name = aLob.getName();
                if (CRDA.equals(name)) {
                    value = formatValue(name, aLob.getValue());
                    it.remove();
                    break;
                }
            }
        }
        return value;
    }


    protected String hanldeRECORDCD(LookupRequest lookupReq) {
        String value = null;
        if (lookupReq.getLobData() != null) {
            List<LOB> fieldValues = lookupReq.getLobData();
            Iterator<LOB> it = fieldValues.iterator();
            while (it.hasNext()) {
                LOB aLob = it.next();
                String name = aLob.getName();
                if (RECO.equals(name)) {
                    value = formatValue(name, aLob.getValue());
                    it.remove();
                    break;
                }
            }
        }
        return value;
    }
    
    
    protected String hanldeCRNODE(LookupRequest lookupReq) {
        String value = null;
        if (lookupReq.getLobData() != null) {
            List<LOB> fieldValues = lookupReq.getLobData();
            Iterator<LOB> it = fieldValues.iterator();
            while (it.hasNext()) {
                LOB aLob = it.next();
                String name = aLob.getName();
                if (CRNO.equals(name)) {
                    value = formatValue(name, aLob.getValue());
                    it.remove();
                    break;
                }
            }
        }
        return value;
    }
    
    protected String hanldeOWNERNODE(LookupRequest lookupReq) {
        String value = null;
        if (lookupReq.getLobData() != null) {
            List<LOB> fieldValues = lookupReq.getLobData();
            Iterator<LOB> it = fieldValues.iterator();
            while (it.hasNext()) {
                LOB aLob = it.next();
                String name = aLob.getName();
                if (OWNE.equals(name)) {
                    value = formatValue(name, aLob.getValue());
                    it.remove();
                    break;
                }
            }
        }
        return value;
    }
    
    protected String hanldeUNITCD(LookupRequest lookupReq) {
        String value = null;
        if (lookupReq.getLobData() != null) {
            List<LOB> fieldValues = lookupReq.getLobData();
            Iterator<LOB> it = fieldValues.iterator();
            while (it.hasNext()) {
                LOB aLob = it.next();
                String name = aLob.getName();
                if (UNIT.equals(name)) {
                    value = formatValue(name, aLob.getValue());
                    it.remove();
                    break;
                }
            }
        }
        return value;
    }
    
    protected String hanldeWRKTYPE(LookupRequest lookupReq) {
        String value = null;
        if (lookupReq.getLobData() != null) {
            List<LOB> fieldValues = lookupReq.getLobData();
            Iterator<LOB> it = fieldValues.iterator();
            while (it.hasNext()) {
                LOB aLob = it.next();
                String name = aLob.getName();
                if (WRKT.equals(name)) {
                    value = formatValue(name, aLob.getValue());
                    it.remove();
                    break;
                }
            }
        }
        return value;
    }
    
    protected String hanldeSTATCD(LookupRequest lookupReq) {
        String value = null;
        if (lookupReq.getLobData() != null) {
            List<LOB> fieldValues = lookupReq.getLobData();
            Iterator<LOB> it = fieldValues.iterator();
            while (it.hasNext()) {
                LOB aLob = it.next();
                String name = aLob.getName();
                if (STAT.equals(name)) {
                    value = formatValue(name, aLob.getValue());
                    it.remove();
                    break;
                }
            }
        }
        return value;
    }
    
    protected String hanldeQUEUECD(LookupRequest lookupReq) {
        String value = null;
        if (lookupReq.getLobData() != null) {
            List<LOB> fieldValues = lookupReq.getLobData();
            Iterator<LOB> it = fieldValues.iterator();
            while (it.hasNext()) {
                LOB aLob = it.next();
                String name = aLob.getName();
                if (QUEU.equals(name)) {
                    value = formatValue(name, aLob.getValue());
                    it.remove();
                    break;
                }
            }
        }
        return value;
    }
    
    protected String hanldePRTY(LookupRequest lookupReq) {
        String value = null;
        if (lookupReq.getLobData() != null) {
            List<LOB> fieldValues = lookupReq.getLobData();
            Iterator<LOB> it = fieldValues.iterator();
            while (it.hasNext()) {
                LOB aLob = it.next();
                String name = aLob.getName();
                if (PRTY.equals(name)) {
                    value = formatValue(name, aLob.getValue());
                    it.remove();
                    break;
                }
            }
        }
        return value;
    }
    
    protected String hanldeINCR(LookupRequest lookupReq) {
        String value = null;
        if (lookupReq.getLobData() != null) {
            List<LOB> fieldValues = lookupReq.getLobData();
            Iterator<LOB> it = fieldValues.iterator();
            while (it.hasNext()) {
                LOB aLob = it.next();
                String name = aLob.getName();
                if (INCR.equals(name)) {
                    value = formatValue(name, aLob.getValue());
                    it.remove();
                    break;
                }
            }
        }
        return value;
    }
    
    protected String hanldeLOCKSTAT(LookupRequest lookupReq) {
        String value = null;
        if (lookupReq.getLobData() != null) {
            List<LOB> fieldValues = lookupReq.getLobData();
            Iterator<LOB> it = fieldValues.iterator();
            while (it.hasNext()) {
                LOB aLob = it.next();
                String name = aLob.getName();
                if (LOCK.equals(name)) {
                    value = formatValue(name, aLob.getValue());
                    it.remove();
                    break;
                }
            }
        }
        return value;
    }
    
    protected String hanldeAMOUNTTYPE(LookupRequest lookupReq) {
        String value = null;
        if (lookupReq.getLobData() != null) {
            List<LOB> fieldValues = lookupReq.getLobData();
            Iterator<LOB> it = fieldValues.iterator();
            while (it.hasNext()) {
                LOB aLob = it.next();
                String name = aLob.getName();
                if (AMTT.equals(name)) {
                    value = formatValue(name, aLob.getValue());
                    it.remove();
                    break;
                }
            }
        }
        return value;
    }
    
    protected String hanldeAMOUNT(LookupRequest lookupReq) {
        String value = null;
        if (lookupReq.getLobData() != null) {
            List<LOB> fieldValues = lookupReq.getLobData();
            Iterator<LOB> it = fieldValues.iterator();
            while (it.hasNext()) {
                LOB aLob = it.next();
                String name = aLob.getName();
                if (AMTV.equals(name)) {
                    value = formatValue(name, aLob.getValue());
                    it.remove();
                    break;
                }
            }
        }
        return value;
    }

    
    protected String hanldeVIFLAG(LookupRequest lookupReq) {
        String value = null;
        if (lookupReq.getLobData() != null) {
            List<LOB> fieldValues = lookupReq.getLobData();
            Iterator<LOB> it = fieldValues.iterator();
            while (it.hasNext()) {
                LOB aLob = it.next();
                String name = aLob.getName();
                if (VIFL.equals(name)) {
                    value = formatValue(name, aLob.getValue());
                    it.remove();
                    break;
                }
            }
        }
        return value;
    }

    
}
