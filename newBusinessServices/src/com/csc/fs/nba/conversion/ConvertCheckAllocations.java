package com.csc.fs.nba.conversion;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
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
import com.csc.fs.dataobject.nba.cash.CheckAllocation;
import com.csc.fs.nba.utility.Nba331Conversion;
import com.csc.fsg.nba.foundation.NbaConstants;

/**
 * Converts the Check Allocation LOBs in the W01U999S table into the CHECK_ALLOCATIONS
 * table in the NBACASH schema.  The LOBs are grouped together by work item ID and sequence
 * numbers, then inserted as a single row into the CHECK_ALLOCATIONS table. 
 * <p>
 * Selection criteria for the WO1U999S table
 * <ul>
 * <li>RecordCd != "C"</li>
 * <li>SeqNbr > 1</li>
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

public class ConvertCheckAllocations {

	public static final String COMPANY = "COID";
	public static final String POLICYNUMBER = "POLN";
	public static final String CWAAMOUNT = "CWAM";
	public static final String INFORCEPAYMENTTYPE = "IFPT";
	public static final String INFORCEPAYMENTDATE = "IFPD";
	public static final String INFORCEPAYMENTMANIND = "IFMP";
	public static final String PENDINGPAYMENTTYPE = "PNPT";
	public static final String COSTBASIS = "CBAS";
	public static final String PAYMENTMONEYSOURCE = "CWOR";
	public static final String PREVIOUSTAXYEAR = "PRTX";
	
	private static Logger log = Logger.getLogger(ConvertCheckAllocations.class);

	
	public void convert(Nba331Conversion conversion) throws Exception {
		System.out.println("Converting check allocations");
		List<W01U999S> criteriaResult = load(conversion.getAwdSessionFactory());
		Map<String, CheckAllocation> checkAllocations = processRows(criteriaResult);
		save(conversion.getCashSessionFactory(), checkAllocations.values());
	}

	/**
	 * Loads all the LOBs with a sequence number > 1 and a work item that is not a case
	 * work item (record code not equal to "C").
	 * @param session
	 * @return
	 */
	protected List<W01U999S> load(SessionFactory awdSF) throws HibernateException {
		Session session = null;
		try {
			session = awdSF.openSession();
			//@SuppressWarnings("unchecked")
			Query query= session.createQuery("select new W01U999S (W01.crDatTim, W01.recordCd, W01.crNode, W01.seqNbr, W01.dataName, W01.dataValue) from W01U999S W01, W03U999S W03 where W01.recordCd <> 'C' and W01.seqNbr > 1 and W01.crDatTim = W03.crDatTim and W03.untCd = 'NBALIFE'");
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
	 * Converts the list of W01U999S rows into a collection of CheckAllocation instances.
	 * @param rows
	 * @return
	 */
	protected Map<String, CheckAllocation> processRows(List<W01U999S> rows) throws ParseException {
		Map<String, CheckAllocation> checkAllocations = new HashMap<String, CheckAllocation>();
	//To read only first 10 records
	//	for(int i=0;i<10;i++){
		for (W01U999S w01 : rows) {
		//	W01U999S w01 = rows.get(i);
			String ID = getID(w01);
			CheckAllocation checkAlloc = checkAllocations.get(ID);
			if (checkAlloc == null) {
				checkAlloc = new CheckAllocation();
				checkAlloc.setItemID(getItemID(w01));
				checkAlloc.setSequence(w01.getSeqNbr());
				checkAllocations.put(ID, checkAlloc);
			}
			if (COMPANY.equals(w01.getDataName())) {
				checkAlloc.setCompany(w01.getDataValue());
			} else if (COSTBASIS.equals(w01.getDataName())) {
				checkAlloc.setCostBasis(convertToDouble(w01.getDataValue()));
			} else if (CWAAMOUNT.equals(w01.getDataName())) {
				checkAlloc.setCwaAmount(convertToDouble(w01.getDataValue()));
			} else if (INFORCEPAYMENTDATE.equals(w01.getDataName())) {
				checkAlloc.setInforcePaymentDate(convertToDate(w01.getDataValue()));
			} else if (INFORCEPAYMENTMANIND.equals(w01.getDataName())) {
				checkAlloc.setInforcePaymentManInd(Integer.valueOf(w01.getDataValue()));
			} else if (INFORCEPAYMENTTYPE.equals(w01.getDataName())) {
				checkAlloc.setInforcePaymentType(Long.valueOf(w01.getDataValue()));
			} else if (PAYMENTMONEYSOURCE.equals(w01.getDataName())) {
				checkAlloc.setPaymentMoneySource(Long.valueOf(w01.getDataValue()));
			} else if (PENDINGPAYMENTTYPE.equals(w01.getDataName())) {
				checkAlloc.setPendingPaymentType(Long.valueOf(w01.getDataValue()));
			} else if (POLICYNUMBER.equals(w01.getDataName())) {
				checkAlloc.setPolicyNumber(w01.getDataValue());
			} else if (PREVIOUSTAXYEAR.equals(w01.getDataName())) {
				checkAlloc.setPreviousTaxYear(Integer.valueOf(w01.getDataValue()));
			}
			checkAlloc.setMigratedInd(NbaConstants.TRUE);
		}
		System.out.format("\tProcessed %d check sequenced lobs into %d rows by work item and sequence\n", rows.size(), checkAllocations.size());
		return checkAllocations;
	}

	/**
	 * Persists the CheckAllocation instances into the CHECK_ALLOCATIONS table.
	 * @param cashSF
	 * @param rows
	 * @throws HibernateException
	 */
	protected void save(SessionFactory cashSF, Collection<CheckAllocation> rows) throws HibernateException {
		Session session = null;
		Transaction trx = null;
		try {
			session = cashSF.openSession();
			trx = session.beginTransaction();
			// remove all rows from the table just in case this utility is run multiple times
			int deleteCount = session.createQuery("delete from CheckAllocation").executeUpdate();
			if (deleteCount > 0) {
				System.out.format("\tClearing table CHECK_ALLOCATIONS (removed %d rows)\n", deleteCount);
			}

			if (log.isDebugEnabled()) {
				log.debug("                                                                                                        Pending      Inforce Payment     ");
				log.debug("                                                                                                Payment Payment /-----------------------\\            Previous");
				log.debug("            Count      Item ID                      Sequence Company Policy Number   CWA Amount Source    Type    Type     Date    Manual Cost Basis Tax Year");
				log.debug("          =========   ============================= ======== ======= =============== ========== ======= ======= ======= ========== ====== ========== ========");
			}
			int count = 1;
			for (CheckAllocation checkAlloc : rows) {
				if (log.isDebugEnabled()) {
					log.debug(String.format("Inserting  %7d    %s", count++, toString(checkAlloc)));
				}
				session.save(checkAlloc);
			}
			trx.commit();
			System.out.format("\tCommitted %d check allocations by work item into CHECK_ALLOCATIONS\n", rows.size());
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
	 * CrNode, and SeqNbr columns.
	 * @param w01
	 * @return
	 */
	protected String getID(W01U999S w01) {
		StringBuilder sb = new StringBuilder();
		sb.append(w01.getCrDatTim());
		sb.append(w01.getRecordCd());
		sb.append(w01.getCrNode());
		sb.append("_");
		sb.append(w01.getSeqNbr());
		return sb.toString();
	}

	/**
	 * Returns the work item ID from the W01U999S.  This is a concatenation of the CrDatTim,
	 * RecordCd, and CrNode columns.
	 * @param w01
	 * @return
	 */
	protected String getItemID(W01U999S w01) {
		StringBuilder sb = new StringBuilder();
		sb.append(w01.getCrDatTim());
		sb.append(w01.getRecordCd());
		sb.append(w01.getCrNode());
		return sb.toString();
	}

	/**
	 * Converts a String value into a Double.  It assumes the value is a currency value and
	 * if no decimal point is found in the value, it will be inserted prior to the last two
	 * digits.
	 * @param str
	 * @return
	 */
	protected Double convertToDouble(String str) {
		Double dbl = null;
		if (str.indexOf(".") == -1) {
			StringBuilder sb = new StringBuilder(str);
			sb.insert(str.length() - 2, '.');
			str = sb.toString();
		}
		dbl = Double.valueOf(str);
		return dbl;
	}

	/**
	 * Converts a String representation of a date into a Date.  It assumes the string value
	 * is formatted as YYYY-MM-DD.
	 * @param strDate
	 * @return
	 * @throws ParseException
	 */
	protected Date convertToDate(String strDate) throws ParseException {
		Date date = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-DD");
		date = format.parse(strDate);
		return date;
	}

	/**
	 * This method creates a String representation of the CheckAllocation instance for
	 * logging purposes.
	 * @param checkAlloc
	 * @return
	 */
	protected String toString(CheckAllocation checkAlloc) {
		StringBuilder sb = new StringBuilder(150);
		sb.append(checkAlloc.getItemID());
		sb.append(String.format("    %2d", checkAlloc.getSequence()));
		sb.append(String.format("    %-7s", checkAlloc.getCompany()));
		sb.append(String.format(" %-15s", checkAlloc.getPolicyNumber()));
		if (checkAlloc.getCwaAmount() == null) {
			sb.append("    null   ");
		} else {
			sb.append(String.format(" %10.2f", checkAlloc.getCwaAmount()));
		}
		if (checkAlloc.getPaymentMoneySource() == null) {
			sb.append("    null");
		} else if (checkAlloc.getPaymentMoneySource() < 0) {
			sb.append(String.format(" %+7d", checkAlloc.getPaymentMoneySource()));
		} else {
			sb.append(String.format(" %7d", checkAlloc.getPaymentMoneySource()));
		}
		if (checkAlloc.getPendingPaymentType() == null) {
			sb.append("    null");
		} else if (checkAlloc.getPendingPaymentType() < 0) {
			sb.append(String.format(" %+7d", checkAlloc.getPendingPaymentType()));
		} else {
			sb.append(String.format(" %7d", checkAlloc.getPendingPaymentType()));
		}
		if (checkAlloc.getInforcePaymentType() == null) {
			sb.append("    null");
		} else if (checkAlloc.getInforcePaymentType() < 0) {
			sb.append(String.format(" %+7d", checkAlloc.getInforcePaymentType()));
		} else {
			sb.append(String.format(" %7d", checkAlloc.getInforcePaymentType()));
		}
		if (checkAlloc.getInforcePaymentDate() == null) {
			sb.append("    null   ");
		} else {
			sb.append(new SimpleDateFormat(" yyyy-MM-DD").format(checkAlloc.getInforcePaymentDate()));
		}
		if (checkAlloc.getInforcePaymentManInd() == null) {
			sb.append("   null");
		} else {
			sb.append(String.format(" %6d", checkAlloc.getInforcePaymentManInd()));
		}
		if (checkAlloc.getCostBasis() == null) {
			sb.append("    null   ");
		} else {
			sb.append(String.format(" %10.2f", checkAlloc.getCostBasis()));
		}
		if (checkAlloc.getPreviousTaxYear() == null) {
			sb.append("   null");
		} else {
			sb.append(String.format(" %4d", checkAlloc.getPreviousTaxYear()));
		}
		return sb.toString();
	}
}
