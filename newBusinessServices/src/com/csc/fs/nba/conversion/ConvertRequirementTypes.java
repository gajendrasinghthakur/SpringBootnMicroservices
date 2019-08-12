package com.csc.fs.nba.conversion;

/*
 * *******************************************************************************<BR>
 * Copyright 2014, Computer Sciences Corporation. All Rights Reserved.<BR>
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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.csc.fs.dataobject.awd.W01U999S;
import com.csc.fs.dataobject.nba.auxiliary.UwReqTypesReceived;
import com.csc.fs.nba.utility.Nba331Conversion;
import com.csc.fsg.nba.foundation.NbaConstants;

/**
 * Converts Requirement Type LOBs attached to the NBAPPLCTN Case Work items in the W01U999S
 * table into the UW_REQTYPES_RECEIVED table in the NBAAUXILIARY schema.  The requirement
 * types are grouped by work item ID and concatenated together in a comma delimited String
 * to be stored in a single column.
 * <p>
 * Selection criteria for the WO1U999S table
 * <ul>
 * <li>RecordCd = "C"</li>
 * <li>CrNode = "01"</li>
 * <li>DataName = "RQTP"</li>
 * </ul>
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL5055, NBA331.1</td><td>Version NB-1402</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @see 
 * @since New Business Accelerator - Version NB-1402
 */

public class ConvertRequirementTypes {

	private static Logger log = Logger.getLogger(ConvertRequirementTypes.class);

	public void convert(Nba331Conversion conversion) throws Exception {
		System.out.println("Converting requirement types");
		List<W01U999S> criteriaResult = load(conversion.getAwdSessionFactory());
		Map<String, UwReqTypesReceived> reqTypesReceived = processRows(criteriaResult);
		save(conversion.getAuxSessionFactory(), reqTypesReceived.values());
	}

	/**
	 * Loads all the requirement types attached to a case work item from the W01U999S
	 * table.
	 * @param session
	 * @return
	 */
	protected List<W01U999S> load(SessionFactory awdSF) throws HibernateException {
		Session session = null;
		try {
			session = awdSF.openSession();
			Query query= session.createQuery("select new W01U999S (W01.crDatTim, W01.recordCd, W01.crNode, W01.seqNbr, W01.dataName, W01.dataValue) from W01U999S W01, W03U999S W03 where W01.recordCd in ('C', 'T') and W01.crNode = '01' and W01.dataName = 'RQTP' and W01.crDatTim = W03.crDatTim and W03.untCd = 'NBALIFE' and W03.wrkType in ('NBAPPLCTN', 'NBAGGCNT')");
	        List list = query.list();	
	        System.out.format("\tLoaded %d requirement types\n", list.size());
	        return list;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	

	
	/**
	 * Converts the list of W01U999S rows into a collection of UwReqTypesReceived instances.
	 * @param rows
	 * @return
	 */
	protected Map<String, UwReqTypesReceived> processRows(List<W01U999S> rows) {
		Map<String, UwReqTypesReceived> reqTypesReceived = new HashMap<String, UwReqTypesReceived>();
		for (W01U999S w01 : rows) {
			String itemID = getID(w01);
			UwReqTypesReceived reqTypes = reqTypesReceived.get(itemID);
			if (reqTypes == null) {
				reqTypes = new UwReqTypesReceived();
				reqTypes.setItemID(itemID);
				reqTypes.setMigratedInd(NbaConstants.TRUE);
				reqTypesReceived.put(itemID, reqTypes);
			}
			reqTypes.addReqType(w01.getDataValue());
		}
		System.out.format("\tProcessed %d requirement type lobs into %d rows by work item\n", rows.size(), reqTypesReceived.size());
		return reqTypesReceived;
	}

	/**
	 * Persists the UwReqTypesReceived into the UW_REQTYPES_RECEIVED table.
	 * @param auxSF
	 * @param rows
	 * @throws HibernateException
	 */
	protected void save(SessionFactory auxSF, Collection<UwReqTypesReceived> rows) throws HibernateException {
		Session session = null;
		Transaction trx = null;
		try {
			session = auxSF.openSession();
			trx = session.beginTransaction();
			// remove all rows from the table just in case this utility is run multiple times
			int deleteCount = session.createQuery("delete from UwReqTypesReceived").executeUpdate();
			System.out.println("rows deleted");
			if (deleteCount > 0) {
				System.out.format("\tClearing table UW_REQTYPES_RECEIVED (removed %d rows)\n", deleteCount);
			}

			if (log.isDebugEnabled()) {
				log.debug("            Count      Item ID                      \t Requirement Types");
				log.debug("          =========   ============================= \t============================");
			}
			int count = 1;
			for (UwReqTypesReceived reqTypes : rows) {
				if (log.isDebugEnabled()) {
					log.debug(String.format("Inserting  %7d  - %s \t%s", count++, reqTypes.getItemID(), reqTypes.getReqTypes()));
				}
				session.save(reqTypes);
			}
			trx.commit();
			System.out.format("\tCommitted %d rows by work item into UW_REQTYPES_RECEIVED\n", rows.size());
		} catch (HibernateException he) {
			if (session != null && session.isOpen()) {
				if (trx != null) {
					trx.rollback();
				}
			}
			throw he;
		}
	}

	/**
	 * Returns the ID from the W01U999S.  This is a concatenation of the CrDatTim, RecordCd,
	 * and CrNode columns.
	 * @param w01
	 * @return
	 */
	protected String getID(W01U999S w01) {
		StringBuilder sb = new StringBuilder();
		sb.append(w01.getCrDatTim());
		sb.append(w01.getRecordCd());
		sb.append(w01.getCrNode());
		return sb.toString();
	}
}
