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
 *     Copyright (c) 2002-2013 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import com.csc.fsg.nba.database.NbaConnectionManager;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.AxaUnderWriterPODVO;
/**
 * This is a stateless Session Bean that is used for doing an Inquiry 
 * for a policy from the backend. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>CR57873</td><td>Discretionary</td><td>Under Writer Weighted Assignment</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaRulesServiceBean {
	protected static NbaLogger logger = null;
	
	public ArrayList displayTableResult(String tableName) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = NbaConnectionManager.borrowConnection(NbaConfiguration.NBA_RULES);
			ArrayList results = new ArrayList();
			if (tableName.equalsIgnoreCase("UW_WORKLOAD")) {
				stmt = conn.createStatement();
				String query = "SELECT UW_QUEUE,CURR_SCORE,CURR_COUNT,MAX_SCORE,ASSIGN_TYPE,YTD_SCORE,YTD_COUNT FROM UW_WORKLOAD";
				rs = stmt.executeQuery(query);
				while (rs.next()) {
					AxaUnderWriterPODVO uwpod = new AxaUnderWriterPODVO();
					uwpod.setUwQueue(rs.getString("UW_QUEUE"));
					uwpod.setCurrentScore(rs.getDouble("CURR_SCORE"));
					uwpod.setCurrentCount(rs.getInt("CURR_COUNT"));
					uwpod.setMaxScore(Double.parseDouble(rs.getString("MAX_SCORE")));
					uwpod.setAssignType(Integer.parseInt(rs.getString("ASSIGN_TYPE")));
					uwpod.setYtdScore(rs.getDouble("YTD_SCORE"));
					uwpod.setYtdCount(rs.getInt("YTD_COUNT"));
					results.add(uwpod);
				}
			}
			return results;
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (Throwable t) {
				return null;
			}
		}
	}	
}