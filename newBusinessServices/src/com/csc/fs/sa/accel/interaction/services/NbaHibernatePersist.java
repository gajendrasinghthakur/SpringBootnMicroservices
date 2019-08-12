package com.csc.fs.sa.accel.interaction.services;
/* 
 * *******************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.FlushMode;
import org.hibernate.Query;

import com.csc.fs.DataObject;
import com.csc.fs.Message;
import com.csc.fs.Result;
import com.csc.fs.ResultImpl;
import com.csc.fs.accel.NbaMessages;
import com.csc.fs.accel.valueobject.AccelValueDataObject;
import com.csc.fs.accel.valueobject.Inquiry;
import com.csc.fs.dataobject.accel.AccelDataObject;
import com.csc.fs.dataobject.accel.AccelPK;
import com.csc.fs.logging.Level;
import com.csc.fs.logging.LogHandler;
import com.csc.fs.logging.Logger;
import com.csc.fs.sa.accel.interaction.HibernateInteractionBase;
import com.csc.fs.sa.accel.interaction.InteractionService;
import com.csc.fs.util.GUIDFactory;

/**
 * NbaHibernatePersist is the Hibernate Service used to invoke all 
 * Hibernate transactions. It dynamically generates the query using the
 * HQL and Hibernate session's methods. For Select Queries it uses the 
 * Inquiry object and its getPrimaryKey implementation to generate the where clause.
 * The input object class and fields are taken from the objlist referenced in the 
 * System API.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * <tr><td>NBA208-38</td><td>Version 7</td><td>Performance Tuning and Testing - Comments in Database</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class NbaHibernatePersist extends HibernateInteractionBase implements InteractionService{

	private static Logger logger = Logger.getLogger(NbaHibernatePersist.class);
	private static final String INSERT = "INSERT";//NBA208-38

	/*
	 * (non-Javadoc)
	 * @see com.csc.fs.sa.accel.interaction.InteractionService#invoke(java.util.List)
	 */
	public Result invoke(List dataObjects) {
		logger.log(Level.DEBUG, "NbaHibernatePersist invoke()");
		Result result = new ResultImpl();
		try {
			if (dataObjects != null) {
				Iterator iter = dataObjects.iterator();
				DataObject dataObject = null;  //NBA330, APSL5055
				while (iter.hasNext()) {
					dataObject = (DataObject) iter.next();  //NBA330, APSL5055
					if (configuration.getMessage().equals(RETRIEVE)) {
			            Query query = getQuery(dataObject);
			            
			            List results = query.list();

			            Iterator iterator = results.iterator();
						int count = 0;
						while (iterator.hasNext()) {
							DataObject acceldo = (DataObject) iterator.next();  //NBA330, APSL5055
							if(acceldo != null){
								acceldo.markOriginal();
								acceldo.setIdentifier(GUIDFactory.create().getKeyString());
								acceldo.getPrimaryKey();
								result.addResult(acceldo);
							}
							count ++;
						}
					    // clear hibernate session to prevent an immediate persit of retrieved objects on
					    // session close
						session.clear(); 

					} else {
				        String objectKeyField =
							(String) configuration.getInteractionSpecProperties().get("objectField");
				        Query query = null;
						try{
                            //NBA208-38 code deleted	
						  	if ((configuration.getMessage().equals(SAVE) || configuration.getMessage().equals(UPDATE))) {
								session.setFlushMode(FlushMode.ALWAYS);//NBA208-38
							    if(!dataObject.isDeleted()){
							        if (LogHandler.Factory.isLogging(LogHandler.LOG_DEBUG)) {
							            LogHandler.Factory.LogDebug(this, "Saving Object in API [{0}] - {1} PK: {2}", 
							            			new Object[] { configuration.getName(), dataObject.getClass().getName(), getPk(dataObject) });  //NBA330, APSL5055
							        }
						            session.saveOrUpdate(dataObject);    
							        result.addResult(dataObject);
							    } else {
							        if (LogHandler.Factory.isLogging(LogHandler.LOG_DEBUG)) {
							            LogHandler.Factory.LogDebug(this, "Deleting Object in API [{0}] - {1} PK: {2}",
							            		new Object[] {configuration.getName(), dataObject.getClass().getName(), getPk(dataObject) });  //NBA330, APSL5055
							        }
							        if (objectKeyField != null) {
							            query = getQuery(dataObject);
							            query.executeUpdate();
							        } else {
							            session.delete(dataObject);    
							        }
							    }
							    session.flush();//NBA208-38
							}
							//Begin NBA208-38
							else if (configuration.getMessage().equals(INSERT)) {
								if (LogHandler.Factory.isLogging(LogHandler.LOG_DEBUG)) {
									LogHandler.Factory.LogDebug(this, "Saving Object in API [{0}] - {1} PK: {2}",
											new Object[] {configuration.getName(), dataObject.getClass().getName(), getPk(dataObject)});  //NBA330, APSL5055
								}
								session.save(dataObject);
								result.addResult(dataObject);
							}
                           //End NBA208-38
							else if (configuration.getMessage().equals(DELETE) && dataObject.isDeleted()) {
								session.setFlushMode(FlushMode.ALWAYS);//NBA208-38
						        if (LogHandler.Factory.isLogging(LogHandler.LOG_DEBUG)) {
						            LogHandler.Factory.LogDebug(this, "Deleting Object in API [{0}] - {1} PK: {2}",
						            		new Object[] {configuration.getName(), dataObject.getClass().getName(), getPk(dataObject)});  //NBA330, APSL5055
						        }
						        if (objectKeyField != null) {
						            query = getQuery(dataObject);
						            query.executeUpdate();
						        } else {
						            session.delete(dataObject);   
						       }
						        session.flush();//NBA208-38
							}
							//NBA208-38 code deleted
							String currentIdentifier = dataObject.getIdentifier();
							if (currentIdentifier == null || currentIdentifier.trim().equals("")) {
								dataObject.setIdentifier(GUIDFactory.create().getKeyString());
							}
						} catch(Exception ex){
							if(!ex.toString().startsWith("org.hibernate.MappingException: Unknown entity:")){
					            LogHandler.Factory.LogError(this, ex.toString());  //NBA330

								throw ex;
							}
						}
			        }
				}
			} else {
				//begin NBA330
				Message errMsg = NbaMessages.ERR_MSG_HBRNATE_INV_INPUT.setVariableData(new Object[] { configuration.getName() });
				result.addMessage(errMsg);
				LogHandler.Factory.LogError(this, errMsg.format());
				//end NBA330
			}
		} catch (Exception ex) {
			//begin NBA330
			Message errMsg = NbaMessages.ERR_MSG_HBRNATE_EXCEPTION.setVariableData(new Object[] { configuration.getName(), ex.getMessage() });
			result.addMessage(errMsg);
			LogHandler.Factory.LogError(this, errMsg.format());
			//end NBA330
	        }
		return result;
	}
	
	/**
     * @param result
     * @param data
     */
    protected void addMessage(Result result, String[] data) {
        result.setErrors(true);
        Message newMsg = new Message();
        newMsg = newMsg.setVariableData(((Object[]) data));
        result.addMessage(newMsg);
    }

    /**
     * Constructs the Query using the getPrimaryKey information from the
     * AccelValueDataObject passed as an input parameter. 
     * @param dataObject
     * @return
     */
    //NBA330 changed parameter to accept the DataObject interface
    protected Query getQuery(DataObject dataObject) {
		Inquiry inquiry = null;
		if (dataObject instanceof Inquiry) {
		    inquiry = (Inquiry) dataObject;
		} else {
		    inquiry = new Inquiry(dataObject);
		}

		String objectClassName = (String) configuration.getInteractionSpecProperties().get("objectClass");
		if (objectClassName == null || objectClassName.length() == 0) {
		    objectClassName = dataObject.getClassName().substring(dataObject.getClassName().lastIndexOf(".") + 1);
		}
		String objectKeyField = (String) configuration.getInteractionSpecProperties().get("objectField");
		String[] fields = null;
		if (objectKeyField != null) {
		    fields = objectKeyField.split(",");    
		}
		String queryString = constructQuery(objectClassName, inquiry, fields, dataObject); 
		Query query = session.createQuery(queryString);
		populateQueryParams(inquiry, fields, query);
        if (LogHandler.Factory.isLogging(LogHandler.LOG_DEBUG)) {
            LogHandler.Factory.LogDebug("NbaHibernateInteraction", query.getQueryString());
        }
		return query;
	}

    /**
     * Populates the Query Parameters using the values in the Inquiry Object
     * @param inquiry
     * @param fields
     * @param query
     */
    protected void populateQueryParams(Inquiry inquiry, String[] fields, Query query) {
        if (fields == null) {
            return;
        }
		
		// get the primary key of the Inquiry Object and search for the keys in System API
		AccelPK key = (AccelPK) inquiry.getPrimaryKey();
		Map content = key.getContent();
		
		for (int i = 0; i < fields.length; i++) {
		    if(content.containsKey(fields[i])){
		        // see if we have a named value in the content map
				Object idValue = content.get(fields[i]);
				if (idValue instanceof Integer) {
					query.setInteger(i, ((Integer) idValue).intValue());
				} else if(idValue instanceof Date){
					query.setDate(i, (Date)idValue);
				} else if(idValue instanceof BigDecimal){
					query.setBigDecimal(i, (BigDecimal)idValue);
				} else {
				    query.setString(i, idValue.toString());
				}
		    }
		}
    }

    /**
     * This method constructs the Query String.
     * @param objectClassName
     * @param inquiry
     * @param fields
     * @return
     */
    //NBA330 change scope to protected and used the DataObject interface
    protected String constructQuery(String objectClassName, Inquiry inquiry, String[] fields, DataObject dataObject) {
        String whereClause = constructWhereClause(inquiry, fields);
        StringBuffer queryStr = new StringBuffer();
        if (dataObject.isDeleted()) {
            queryStr.append("delete ");  //NBA330
        }
        queryStr.append("from ");  //NBA330
        queryStr.append(objectClassName);
        if (!whereClause.equals("")) {
            queryStr.append(" where ");  //NBA330
            queryStr.append(whereClause);
        }
        
        return queryStr.toString();
    }


    /**
     * This method constructs the where clause on the basis of the fields array.
     * @param inquiry
     * @param fields
     * @return
     */
    protected String constructWhereClause(Inquiry inquiry, String[] fields) {
        if (fields == null) {
            return "";
        }
        StringBuffer whereClause = new StringBuffer();
		for (int i = 0; i < fields.length; i++) {
		    if(inquiry.getIDCount()+1 > i){
				if (!whereClause.toString().equals("")) {
					whereClause.append(" and ");
				}
				whereClause.append(fields[i]);
				whereClause.append(" = ? ");
		    }
		}
		String sortByField =
			(String) configuration.getInteractionSpecProperties().get("sortField");

		if(sortByField != null && !sortByField.equals("")){
			String[] sortFields = sortByField.split(",");
			StringBuffer orderClause = new StringBuffer();
			for (int i = 0; i < sortFields.length; i++) {
				if (!orderClause.toString().equals("")) {
				    orderClause.append(" , ");
				}
				orderClause.append(" ");
				orderClause.append(sortFields[i]);
			}
			if(!orderClause.equals("")){
			    whereClause.append(" order by ");
			    whereClause.append(orderClause.toString());
			}
		}
        return whereClause.toString();
    }
	
	/**
	 * Returns the primary key value from a data object.
	 * @param dataObject
	 * @return
	 */
    //NBA330, APSL5055 New Method
	protected String getPk(DataObject dataObject) {
		String pk = null;
		if (dataObject instanceof AccelDataObject) {
			pk = ((AccelDataObject) dataObject).getPk();
		} else if (dataObject instanceof AccelValueDataObject) {
			pk = ((AccelValueDataObject) dataObject).getPk();
		}
		return pk;
	}
	
}
