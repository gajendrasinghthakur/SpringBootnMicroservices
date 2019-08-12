package com.csc.fs.nba.utility;

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

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.csc.fs.nba.conversion.ConvertCheckAllocations;
import com.csc.fs.nba.conversion.ConvertRequirementTypes;

/**
 * The conversion utility for AWD LOBs with sequence numbers greater than 1 (one).
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA331.1</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @see 
 * @since New Business Accelerator - Version NB-1402
 */

public class Nba331Conversion {

	public static Nba331Conversion conversion;
	private SessionFactory auxSessFactory;
	private SessionFactory awdSessFactory;
	private SessionFactory cashSessFactory;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			getInstance().setup();
			new ConvertCheckAllocations().convert(getInstance());
			new ConvertRequirementTypes().convert(getInstance());
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			getInstance().tearDown();
		}
	}

	/**
	 * Returns a singleton instance of this class.
	 * @return
	 */
	public static Nba331Conversion getInstance() {
		if (conversion == null) {
			conversion = new Nba331Conversion();
		}
		return conversion;
	}

	/**
	 * Setup the Hibernate environment session factories for the AUXILIARY, AWD, & CASH
	 * data sources.
	 */
	protected void setup() {
		auxSessFactory = new Configuration().configure("hibernateAuxiliary.cfg.xml").buildSessionFactory();
		awdSessFactory = new Configuration().configure("hibernateAWD.cfg.xml").buildSessionFactory();
		cashSessFactory = new Configuration().configure("hibernateCash.cfg.xml").buildSessionFactory();
	}

	/**
	 * Shutdown the Hibernate environment session factories.
	 */
	public void tearDown() {
		if (auxSessFactory != null) {
			try {
				auxSessFactory.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (awdSessFactory != null) {
			try {
				awdSessFactory.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (cashSessFactory != null) {
			try {
				cashSessFactory.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Return the SessionFactory for the AUXILIARY data source.
	 * @return
	 */
	public SessionFactory getAuxSessionFactory() {
		return auxSessFactory;
	}

	/**
	 * Return the SessionFactory for the AWD data source.
	 * @return
	 */
	public SessionFactory getAwdSessionFactory() {
		return awdSessFactory;
	}

	/**
	 * Return the SessionFactory for the CASH data source.
	 * @return
	 */
	public SessionFactory getCashSessionFactory() {
		return cashSessFactory;
	}
	
	public void convertRequirementType() {
		try {
			setup();
			new ConvertRequirementTypes().convert(Nba331Conversion.getInstance());
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			tearDown();
		}
	
	}
	
	public void convertCheckAllocation() {
		try {
			setup();
			new ConvertCheckAllocations().convert(Nba331Conversion.getInstance());
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			tearDown();
		}
	
	}
}
