package com.csc.fsg.nba.process.product;

import java.util.Date;

/*
 * ************************************************************** <BR>
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
 *     Copyright (c) 2002-2012 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */

/**
 * ProductCache provides cache for caching product data. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ALII767</td><td>PPFL Cache Enhancement</td><td>Performance Tuning</td></tr>
 * </table>
 * </p>
 * @author 	CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.configuration.NbaConfiguration
 * @since New Business Automation - Version 1
 */
public class ProductKey {
		
	protected String backendKey;
	protected String companyKey;
	protected String productKey;
	protected Date effDateKey;
	protected Date expDateKey;
	
	/**
	 * 
	 */
	public ProductKey() {
		super();
	}
	
	/**
	 * @param parentIdKey
	 * @param backendKey
	 * @param companyKey
	 * @param productKey
	 * @param effDateKey
	 * @param expDateKey
	 */
	public ProductKey(String backendKey, String companyKey, String productKey, Date effDateKey, Date expDateKey) {
		super();
		this.backendKey = backendKey;
		this.companyKey = companyKey;
		this.productKey = productKey;
		this.effDateKey = effDateKey;
		this.expDateKey = expDateKey;
	}
	
	/**
	 * @return Returns the backendKey.
	 */
	public String getBackendKey() {
		return backendKey;
	}
	/**
	 * @param backendKey The backendKey to set.
	 */
	public void setBackendKey(String backendKey) {
		this.backendKey = backendKey;
	}
	/**
	 * @return Returns the companyKey.
	 */
	public String getCompanyKey() {
		return companyKey;
	}
	/**
	 * @param companyKey The companyKey to set.
	 */
	public void setCompanyKey(String companyKey) {
		this.companyKey = companyKey;
	}
	/**
	 * @return Returns the productKey.
	 */
	public String getProductKey() {
		return productKey;
	}
	/**
	 * @param productKey The productKey to set.
	 */
	public void setProductKey(String productKey) {
		this.productKey = productKey;
	}
	/**
	 * @return Returns the effDateKey.
	 */
	public Date getEffDateKey() {
		return effDateKey;
	}
	/**
	 * @param effDateKey The effDateKey to set.
	 */
	public void setEffDateKey(Date effDateKey) {
		this.effDateKey = effDateKey;
	}
	/**
	 * @return Returns the expDateKey.
	 */
	public Date getExpDateKey() {
		return expDateKey;
	}
	/**
	 * @param expDateKey The expDateKey to set.
	 */
	public void setExpDateKey(Date expDateKey) {
		this.expDateKey = expDateKey;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj instanceof ProductKey) {
			ProductKey other = (ProductKey) obj;
			return (
				this.productKey.equalsIgnoreCase(other.productKey)
                && this.backendKey.equalsIgnoreCase(other.backendKey) 
				&& this.companyKey.equalsIgnoreCase(other.companyKey)
				&& isBetween(this.expDateKey, other.effDateKey, other.expDateKey )
				&& isBetween(this.effDateKey, other.effDateKey, other.expDateKey ));
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return productKey.hashCode() + companyKey.hashCode() + backendKey.hashCode() ;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "\nProductkey:         " + getProductKey() + "\nCompanyKey:      "
		+ getCompanyKey() + "\nBackendKey :      " + getBackendKey() + "\nEffDateKey:       "
		+ getEffDateKey() + "\nExpDateKey :      " + getExpDateKey();		
	}	
	
	private boolean isBetween(Date d1, Date d2, Date d3) {		
		if (d1 == null || d2==null || d3==null) {
			return false;
		} else {
			return d3.after(d1) && d2.before(d1);
		}
	}
	
	
}
