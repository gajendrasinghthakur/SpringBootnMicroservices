package com.csc.fs.dataobject.nba.auxiliary;

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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.csc.fs.PrimaryKey;
import com.csc.fs.accel.valueobject.AccelValueDataObject;
import com.csc.fs.dataobject.accel.AccelDataObject;

/**
 * UwReqTypesReceived is a data object representing a single row from the UW_REQTYPES_RECEIVED
 * table in the NBAAUXILIARY database schema.  This class is used to query and maintain data in
 * the UW_REQTYPES_RECEIVED table.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA331</td><td>Version NB-1402</td><td>AWD REST</td></tr>
 * <tr><td>APSL5055</td><td>Version</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @see 
 * @since New Business Accelerator - Version NB-1402
 */

public class UwReqTypesReceived extends AccelDataObject {

	private static final long serialVersionUID = 8840989829294596935L;

	protected static final String DELIMITER = ",";

	private String itemID = null;
	private String reqTypes = null;
	private List<String> listOfReqTypes = null;
	private Integer migratedInd = null;


	/* (non-Javadoc)
	 * @see com.csc.fs.DataObject#getPrimaryKey()
	 */
	public PrimaryKey getPrimaryKey() {
		if (key.getContent().isEmpty()) {
			constructPrimaryKey();
		}
		return super.getPrimaryKey();
	}

	/**
	 * Constructs a primary key and adds additional key information to the primary key
	 * for use with the NbaHibernatePersist logic.  This method assumes all the key
	 * data has been applied to this object instance prior to being called.  If any of
	 * the key data changes, this method should be called again.
	 */
	public void constructPrimaryKey() {
		key.addKey("itemID", getItemID());

		setPk(getItemID());
	}

	/**
	 * @return the itemID
	 */
	public String getItemID() {
		return itemID;
	}
	/**
	 * @param itemID the itemID to set
	 */
	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	/**
	 * @return the reqtypes
	 */
	public String getReqTypes() {
		if (reqTypes == null) {
			if (listOfReqTypes != null) {
				StringBuilder sb = new StringBuilder();
				for (String type : listOfReqTypes) {
					if (sb.length() > 0) {
						sb.append(DELIMITER);
					}
					sb.append(type);
				}
				reqTypes = sb.toString();
			}
		}
		return reqTypes;
	}
	/**
	 * @param reqtypes the reqtypes to set
	 */
	public void setReqTypes(String reqtypes) {
		this.reqTypes = reqtypes;
		listOfReqTypes = null;
	}

	/**
	 * 
	 * @return
	 */
	public List<String> getListOfReqTypes() {
		if (listOfReqTypes == null) {
			if (reqTypes == null) {
				listOfReqTypes = new ArrayList<String>();
			} else {
				StringTokenizer tokenizer = new StringTokenizer(reqTypes, DELIMITER);
				listOfReqTypes = new ArrayList<String>(tokenizer.countTokens());
				while (tokenizer.hasMoreElements()) {
					listOfReqTypes.add((String)tokenizer.nextElement());
				}
			}
		}
		return listOfReqTypes;
	}

	/**
	 * 
	 * @param reqType
	 */
	public void addReqType(String reqType) {
		getListOfReqTypes().add(reqType);
		reqTypes = null;
	}

	/**
	 * 
	 * @param list
	 */
	public void setListOfReqTypes(List<String> list) {
		listOfReqTypes = list;
		reqTypes = null;
	}

	/**
	 * 
	 */
//	public boolean getNew() {
//		return isNew();
//	}
	
	public Integer getMigratedInd() {
		return migratedInd;
	}

	public void setMigratedInd(Integer migratedInd) {
		this.migratedInd = migratedInd;
	}
}
