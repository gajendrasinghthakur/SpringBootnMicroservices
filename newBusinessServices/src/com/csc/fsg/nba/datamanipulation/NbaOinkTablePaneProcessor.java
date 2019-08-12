package com.csc.fsg.nba.datamanipulation; //NBA201
/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 *  are proprietary to CSC Financial Services Group®.  The use,<BR>
 *  reproduction, distribution or disclosure of this program, in whole or in<BR>
 *  part, without the express written permission of CSC Financial Services<BR>
 *  Group is prohibited.  This program is also an unpublished work protected<BR>
 *  under the copyright laws of the United States of America and other<BR>
 *  countries.  If this program becomes published, the following notice shall<BR>
 *  apply:
 *      Property of Computer Sciences Corporation.<BR>
 *      Confidential. Not for publication.<BR>
 *      Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.html.support.NbaTableColumn;
import com.csc.fsg.nba.html.support.NbaTablePane;
import com.csc.fsg.nba.tableaccess.NbaTableData;


/**
 * This class provides Application Entry screens an ability to resolve data for
 * table panes using OINK. It extracts details of table panes defined on the JSPs from 
 * the HTTPRequest object, unmarshals columns information, and maps them to the XML Source/Destination
 * using OINK. OINK will successfully map all table columns to the right XML tags only if
 * all the columns names are a subset of OINK's data dictionary.
 *  <p>
 *  <b>Modifications:</b><br>
 *  <table border=0 cellspacing=5 cellpadding=5>
 *  <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 *  </thead>
 *  <tr><td>SPR1337</td><td>Version 3</td><td>Table pane support for App Entry</td></tr>
 *  <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 *  <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 *  </table>
 *  <p>
 *  @author CSC FSG Developer
 * @version 7.0.0
 *  @since New Business Accelerator - Version 3
 * 
 */
public class NbaOinkTablePaneProcessor {
	protected final String TABLE_DEF_FIELD = "Nba_TP_Info";
	protected final String NBA_OBJECT_PREFIX = "NBA_TP";
	
	/*
	 * This is an internal storage variable for table pane information.
	 */	
	protected Map tablePanes;
	/*
	 * Stores definitions of columns with translation tables.
	 */
	protected Map clmnsWithTables;
	/*
	 * Stores JavaScript that represents table pane data.
	 */
	protected StringBuffer scriptBuffer;
	
	
	/**
	 * Constructor for NbaOinkTablePaneProcessor.
	 * @param request Represents the HTTP Request object.
	 */
	public NbaOinkTablePaneProcessor(HttpServletRequest request) {
		tablePanes = new HashMap();
		clmnsWithTables = new HashMap();
		scriptBuffer = new StringBuffer();
		
		if (null != request.getParameter(TABLE_DEF_FIELD)) {
			NbaStringTokenizer tokens = new NbaStringTokenizer(request.getParameter(TABLE_DEF_FIELD), ";");
			while (tokens.hasMoreTokens()) {
				String token = tokens.nextToken(); // SPR3290
				NbaStringTokenizer nvPair = new NbaStringTokenizer(token, "=");
				String key = nvPair.nextToken();
				String value = nvPair.nextToken();
				if (key.toUpperCase().startsWith(NBA_OBJECT_PREFIX)) {
					tablePanes.put(key, value);
				} else {
					clmnsWithTables.put(key, value);
				}
			}
		}
	}
	
	/**
	 * Retrieves translation table values for columns that need translation values.
	 * @param oink
	 * @throws NbaBaseException
	 */
	public void retrieveTableData(NbaOinkDataAccess oink) throws NbaBaseException{
		Object[] tableNames = clmnsWithTables.values().toArray();
		
		Map tableData = new HashMap();
		for (int i = 0; i < tableNames.length; i++) {
			//tablename - data pair	
			tableData.put((String) tableNames[i], oink.getNbaTableData((String) tableNames[i]));
		}
		Object[] clmnsNames = clmnsWithTables.keySet().toArray();
		for (int j = 0; j < clmnsNames.length; j++) {
			//clmn name - data pair replaces the clmn name - table name pair
			clmnsWithTables.put(clmnsNames[j], tableData.get(clmnsWithTables.get(clmnsNames[j])));	
		}
	}
	/**
	 * Extracts data from a predefined XML source and converts it into JavaScript
	 * suitable for display on HTML table panes.
	 * @param request
	 * @param oink
	 * @throws NbaBaseException
	 */
	public void buildData(HttpServletRequest request, NbaOinkDataAccess oink) throws NbaBaseException{
		StringBuffer scriptBuffer = new StringBuffer();
		Object[] tableNames = tablePanes.keySet().toArray();
		
		for (int i = 0; i < tableNames.length; i++) {
			NbaTablePane pane = new NbaTablePane();
			pane.setDomElement((String) tableNames[i]);
			pane.unmarshal(request);
			int rowCount = pane.getRowCount();
			for (int j = 0; j < pane.columns.size(); j++) {
				oink.getFormatter().setDateFormat(NbaOinkFormatter.DATE_FORMAT_MMDDYYYY);
				NbaOinkRequest oinkRequest = new NbaOinkRequest();
				oinkRequest.setCount(rowCount);
				String clmnName = ((NbaTableColumn) pane.columns.get(j)).getName();
				if (!clmnName.equalsIgnoreCase("row_status")) {
					oinkRequest.setVariable(clmnName);
					String[] values = oink.getStringValuesFor(oinkRequest);
					for (int k = 0; k < values.length; k++) {
						scriptBuffer.setLength(0);						
						scriptBuffer.append("parent.");
						scriptBuffer.append(tablePanes.get(tableNames[i]).toString());
						scriptBuffer.append(".setCellValue(\"");
						scriptBuffer.append(clmnName);
						scriptBuffer.append("\", ");
						scriptBuffer.append(k);
						scriptBuffer.append(", \"");
						scriptBuffer.append(values[k]);
						scriptBuffer.append("\");\n");
						oink.getNbaOinkHTMLFormatter().getNbaHTMLHelper().sendScript(scriptBuffer.toString());						
					}
					
					if (clmnsWithTables.containsKey(clmnName)) {
						oink.getNbaOinkHTMLFormatter().getNbaHTMLHelper().createArrays(clmnName, (NbaTableData[]) clmnsWithTables.get(clmnName));
						scriptBuffer.setLength(0);						
						scriptBuffer.append(clmnName);
						scriptBuffer.append("Codes = ");
						scriptBuffer.append(clmnName);
						scriptBuffer.append("Codes.split(\"|\");");
						
						scriptBuffer.append(clmnName);
						scriptBuffer.append("Text = ");
						scriptBuffer.append(clmnName);
						scriptBuffer.append("Text.split(\"|\");");
						
						scriptBuffer.append("parent.");
						scriptBuffer.append(tablePanes.get(tableNames[i]).toString());
						scriptBuffer.append(".findColumnByName(\"");
						scriptBuffer.append(clmnName);
						scriptBuffer.append("\").ddValue = ");
						scriptBuffer.append(clmnName);
						scriptBuffer.append("Codes ;\n");
	
						scriptBuffer.append("parent.");
						scriptBuffer.append(tablePanes.get(tableNames[i]).toString());
						scriptBuffer.append(".findColumnByName(\"");
						scriptBuffer.append(clmnName);
						scriptBuffer.append("\").ddText = ");
						scriptBuffer.append(clmnName);
						scriptBuffer.append("Text ;\n");
						oink.getNbaOinkHTMLFormatter().getNbaHTMLHelper().sendScript(scriptBuffer.toString());
					}
				}
				
			}
		}

	}
	/**
	 * Extracts data from a HTML table pane and converts it into XML data.
	 * @param request
	 * @param oink
	 * @throws NbaBaseException
	 */
	public void saveData(HttpServletRequest request, NbaOinkDataAccess oink) throws NbaBaseException {
		// SPR3290 code deleted
		Object[] tableNames = tablePanes.keySet().toArray();
		for (int i = 0; i < tableNames.length; i++) {
			NbaTablePane pane = new NbaTablePane();
			pane.setDomElement((String) tableNames[i]);
			pane.unmarshal(request);			
			for (int j = 0; j < pane.columns.size(); j++) {
				NbaOinkRequest oinkRequest = new NbaOinkRequest();
				NbaTableColumn clmn = (NbaTableColumn) pane.columns.get(j);
				if (!"row_status".equalsIgnoreCase(clmn.getName())) {					
					oinkRequest.setVariable(clmn.getName());
					oinkRequest.setCount(clmn.getData().size());
					oinkRequest.setValue(new Vector(clmn.getData()));
					oink.updateValue(oinkRequest);
				}
			}
		}
	}
}
