package com.csc.fs.dataobject.nba.cash;

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

import java.util.Date;

import com.csc.fs.PrimaryKey;
import com.csc.fs.dataobject.accel.AccelDataObject;
import com.csc.fsg.nba.foundation.NbaConstants;

/**
 * CheckAllocation is a data object representing a single row from the CHECK_ALLOCATIONS
 * table in the NBACASH database schema.  This class is used to query and maintain data in
 * the CHECK_ALLOCATIONS table.
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

public class CheckAllocation extends AccelDataObject {

	private static final long serialVersionUID = -1971214096157351461L;

	private String itemID = null;
	private int sequence = 0;
	private String company = null;
	private String policyNumber = null;
	private Double cwaAmount = null;
	private Long infPaymentType = null;
	private Date infPaymentDate = null;
	private Integer infPaymentManInd = null;
	private Long pendPaymentType = null;
	private Double costBasis = null;
	private Long paymentMoneySource = null;
	private Integer previousTaxYear = null;
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
		key.addKey("sequence", getSequence());

		StringBuilder sb = new StringBuilder(100);
		sb.append(getItemID()).append("_");
		sb.append(getSequence());
		setPk(sb.toString());
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
	 * @return the sequence
	 */
	public int getSequence() {
		return sequence;
	}
	/**
	 * @param sequence the sequence to set
	 */
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	/**
	 * @return the company
	 */
	public String getCompany() {
		return company;
	}
	/**
	 * @param company the company to set
	 */
	public void setCompany(String company) {
		this.company = company;
	}

	/**
	 * @return the policyNumber
	 */
	public String getPolicyNumber() {
		return policyNumber;
	}
	/**
	 * @param policyNumber the policyNumber to set
	 */
	public void setPolicyNumber(String policyNumber) {
		this.policyNumber = policyNumber;
	}

	/**
	 * @return the cwaAmount
	 */
	public Double getCwaAmount() {
		return cwaAmount;
	}
	/**
	 * @param cwaAmount the cwaAmount to set
	 */
	public void setCwaAmount(Double cwaAmount) {
		this.cwaAmount = cwaAmount;
	}

	/**
	 * @return the inforce payment type
	 */
	public Long getInforcePaymentType() {
		return infPaymentType;
	}
	/**
	 * @param paymentType the inforce payment type to set
	 */
	public void setInforcePaymentType(Long paymentType) {
		this.infPaymentType = paymentType;
	}

	/**
	 * @return the inforce payment date
	 */
	public Date getInforcePaymentDate() {
		return infPaymentDate;
	}
	/**
	 * @param paymentDate the inforce payment date to set
	 */
	public void setInforcePaymentDate(Date paymentDate) {
		this.infPaymentDate = paymentDate;
	}

	/**
	 * @return the inforce payment manual indicator
	 */
	public Integer getInforcePaymentManInd() {
		return infPaymentManInd;
	}
	/**
	 * @param indicator the inforce payment manual indicator to set
	 */
	public void setInforcePaymentManInd(Integer indicator) {
		this.infPaymentManInd = indicator;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isInforcePaymentManual() {
		return getInforcePaymentManInd() != null && getInforcePaymentManInd() == NbaConstants.TRUE;
	}
	
	/**
	 * @return the pending payment type
	 */
	public Long getPendingPaymentType() {
		return pendPaymentType;
	}
	/**
	 * @param paymentType the pending payment type to set
	 */
	public void setPendingPaymentType(Long paymentType) {
		this.pendPaymentType = paymentType;
	}

	/**
	 * @return the cost basis
	 */
	public Double getCostBasis() {
		return costBasis;
	}
	/**
	 * @param costBasis the cost basis to set
	 */
	public void setCostBasis(Double costBasis) {
		this.costBasis = costBasis;
	}

	/**
	 * @return the payment money source
	 */
	public Long getPaymentMoneySource() {
		return paymentMoneySource;
	}
	/**
	 * @param paymentMoneySource the payment money source to set
	 */
	public void setPaymentMoneySource(Long paymentMoneySource) {
		this.paymentMoneySource = paymentMoneySource;
	}

	/**
	 * @return the previousTaxYear
	 */
	public Integer getPreviousTaxYear() {
		return previousTaxYear;
	}
	/**
	 * @param previousTaxYear the previousTaxYear to set
	 */
	public void setPreviousTaxYear(Integer previousTaxYear) {
		this.previousTaxYear = previousTaxYear;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isPreviousTaxYear() {
		return getPreviousTaxYear() != null && getPreviousTaxYear() == NbaConstants.TRUE;
	}

	public Integer getMigratedInd() {
		return migratedInd;
	}

	public void setMigratedInd(Integer migratedInd) {
		this.migratedInd = migratedInd;
	}
}
