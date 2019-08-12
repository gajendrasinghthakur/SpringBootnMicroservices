
package com.csc.fsg.nba.bean.accessors;

import java.util.List;

/**
 * Plane Java class to populate the policy search result data.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.35</td><td>AXA Life Phase 1</td><td>nbA Web Services</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 */
public class Nba302WebServiceData {
    
    private long formaAppliedInd;
    private long applicationType;
    private long informalApproval;
    private List companyProducerIdList;
    private String carrierAdminSystem;
    private String policyNumber;
    private String carrierCode;
    private String planName;
    private long policyStatus;
    private String primaryWritingAgentID;
    private String productCode;

    /**
     * @return Returns the primaryWritingAgentID.
     */
    public String getPrimaryWritingAgentID() {
        return primaryWritingAgentID;
    }
    /**
     * @param primaryWritingAgentID The primaryWritingAgentID to set.
     */
    public void setPrimaryWritingAgentID(String primaryWritingAgentID) {
        this.primaryWritingAgentID = primaryWritingAgentID;
    }
    /**
     * @return Returns the applicationType.
     */
    public long getApplicationType() {
        return applicationType;
    }
    /**
     * @param applicationType The applicationType to set.
     */
    public void setApplicationType(long applicationType) {
        this.applicationType = applicationType;
    }
    /**
     * @return Returns the carrierAdminSystem.
     */
    public String getCarrierAdminSystem() {
        return carrierAdminSystem;
    }
    /**
     * @param carrierAdminSystem The carrierAdminSystem to set.
     */
    public void setCarrierAdminSystem(String carrierAdminSystem) {
        this.carrierAdminSystem = carrierAdminSystem;
    }
    /**
     * @return Returns the carrierCode.
     */
    public String getCarrierCode() {
        return carrierCode;
    }
    /**
     * @param carrierCode The carrierCode to set.
     */
    public void setCarrierCode(String carrierCode) {
        this.carrierCode = carrierCode;
    }
  
    
	/**
	 * @return Returns the companyProducerIdList.
	 */
	public List getCompanyProducerIdList() {
		return companyProducerIdList;
	}
	/**
	 * @param companyProducerIdList The companyProducerIdList to set.
	 */
	public void setCompanyProducerIdList(List companyProducerIdList) {
		this.companyProducerIdList = companyProducerIdList;
	}
    /**
     * @return Returns the formaAppliedInd.
     */
    public long getFormaAppliedInd() {
        return formaAppliedInd;
    }
    /**
     * @param formaAppliedInd The formaAppliedInd to set.
     */
    public void setFormaAppliedInd(long formaAppliedInd) {
        this.formaAppliedInd = formaAppliedInd;
    }
    /**
     * @return Returns the informalApproval.
     */
    public long getInformalApproval() {
        return informalApproval;
    }
    /**
     * @param informalApproval The informalApproval to set.
     */
    public void setInformalApproval(long informalApproval) {
        this.informalApproval = informalApproval;
    }
    /**
     * @return Returns the planName.
     */
    public String getPlanName() {
        return planName;
    }
    /**
     * @param planName The planName to set.
     */
    public void setPlanName(String planName) {
        this.planName = planName;
    }
    /**
     * @return Returns the policyNumber.
     */
    public String getPolicyNumber() {
        return policyNumber;
    }
    /**
     * @param policyNumber The policyNumber to set.
     */
    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }
    /**
     * @return Returns the policyStatus.
     */
    public long getPolicyStatus() {
        return policyStatus;
    }
    /**
     * @param policyStatus The policyStatus to set.
     */
    public void setPolicyStatus(long policyStatus) {
        this.policyStatus = policyStatus;
    }
    
  
    /**
     * @return Returns the productCode.
     */
    public String getProductCode() {
        return productCode;
    }
    /**
     * @param productCode The productCode to set.
     */
    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }
}
