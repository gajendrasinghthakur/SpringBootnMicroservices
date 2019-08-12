package com.csc.fsg.nba.bean.accessors;
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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import com.csc.fsg.nba.foundation.NbaServiceLocator;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.configuration.DatabaseConnection;



public class NbaAuditWebServiceBean {
	public Element getHoldingInquiry(String backendKey, String companyKey, String policyNo, String auditNo, String check) {
		Element rootElement = null;
		if (null == check)
			check = "";
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.newDocument();
			rootElement = dom.createElement("Tables");
			dom.appendChild(rootElement);
			DatabaseConnection configDB = NbaConfiguration.getInstance().getDatabaseConnection(NbaConfiguration.AUDITING_CONTRACT); // NBA050, ACN012
			NbaServiceLocator sl = NbaServiceLocator.getInstance();
			javax.sql.DataSource ds = (DataSource) sl.lookup(configDB.getDataSource());
			Connection conn = ds.getConnection(configDB.getUserId(), configDB.getPassword());
			Statement stmt = conn.createStatement();
			if (check.equals("")) {
				ResultSet allTables = stmt
						.executeQuery("select table_name from user_tables WHERE TABLE_name NOT IN ('NBA_TXN_HISTORY') ORDER by table_name ");
				while (allTables.next()) {
					String tableName = allTables.getString(1).toUpperCase();
					Statement stm = conn.createStatement();
					ResultSet checkRecord = stm.executeQuery("select count(*) from " + tableName + " where BACKENDKEY='" + backendKey.toUpperCase()
							+ "'" + " and COMPANYKEY='" + companyKey.toUpperCase() + "'" + " and CONTRACTKEY='" + policyNo + "'"
							+ " AND AUDITVERSION='" + auditNo + "'");
					int recordCount = 0;
					if (checkRecord.next()) {
						recordCount = checkRecord.getInt(1);
						//For each Table object create element and attach it to root
						if (recordCount > 0) {
							Table t = new Table(tableName, "", "", "");
							Element tableElement = createTableElement(t, dom);
							rootElement.appendChild(tableElement);
						}
					}
					checkRecord.close();
					stm.close();
				}
				allTables.close();
			}
			stmt.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rootElement;
	}

	private Element createTableElement(Table t, Document dom) {
		Element tableEle = null;
		if (!t.getTableName().equals("")) {
			tableEle = dom.createElement("Table");
			tableEle.setAttribute("Name", t.getTableName());
		}
		if (!t.getColumnName().equals("")) {
			Element columEle = dom.createElement("Column");
			columEle.setAttribute("Name", t.getColumnName());
			Element prevValueEle = dom.createElement("PrevValue");
			Text prevValueText = dom.createTextNode(t.getPrevValue());
			prevValueEle.appendChild(prevValueText);
			columEle.appendChild(prevValueEle);
			Element currentValueEle = dom.createElement("CurrentValue");
			Text currentValueText = dom.createTextNode(t.getCurrentValue());
			currentValueEle.appendChild(currentValueText);
			columEle.appendChild(currentValueEle);
			tableEle.appendChild(columEle);
		}
		return tableEle;
	}


public class Table {
	String tableName;
	String columnName;
	String prevValue;
	String currentValue;
	
	public Table(String tableName,String columnName,String prevValue,String currentValue){
		this.tableName=tableName;
		this.columnName=columnName;
		this.prevValue=prevValue;
		this.currentValue=currentValue;
	}
	

	/**
	 * @return Returns the columnName.
	 */
	public String getColumnName() {
		return columnName;
	}
	/**
	 * @param columnName The columnName to set.
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	/**
	 * @return Returns the currentValue.
	 */
	public String getCurrentValue() {
		return currentValue;
	}
	/**
	 * @param currentValue The currentValue to set.
	 */
	public void setCurrentValue(String currentValue) {
		this.currentValue = currentValue;
	}
	/**
	 * @return Returns the prevValue.
	 */
	public String getPrevValue() {
		return prevValue;
	}
	/**
	 * @param prevValue The prevValue to set.
	 */
	public void setPrevValue(String prevValue) {
		this.prevValue = prevValue;
	}
	/**
	 * @return Returns the tableName.
	 */
	public String getTableName() {
		return tableName;
	}
	/**
	 * @param tableName The tableName to set.
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
}
