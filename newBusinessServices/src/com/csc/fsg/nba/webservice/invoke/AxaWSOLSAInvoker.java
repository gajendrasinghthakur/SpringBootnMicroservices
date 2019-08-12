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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */

package com.csc.fsg.nba.webservice.invoke;

import java.util.Iterator;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.SourceInfo;
import com.csc.fsg.nba.vo.txlife.VendorApp;
import com.csc.fsg.nba.vo.txlife.VendorName;

/**
 * This class is responsible for creating request for RTS webservice .
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.26</td><td>AXA Life Phase 1</td><td>OLSA Interface</td></tr>
 * <tr><td>SR494086.6</td><td>Discretionary</td><td>ADC Retrofit</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaWSOLSAInvoker extends AxaWSInvokerBase {
	
	private static final String CATEGORY = "OLSA";

	private static final String FUNCTIONID = "initialPremiumInfo";

    /**
     * constructor from superclass
     * @param userVO
     * @param nbaTXLife
     */
    public AxaWSOLSAInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
        super(operation, userVO, nbaTXLife, nbaDst, object);
        setBackEnd(ADMIN_ID);
        setCategory(CATEGORY);
        setFunctionId(FUNCTIONID);
    }
	/**
     * Create webservice request for Agent validation
     */
    public NbaTXLife createRequest() throws NbaBaseException {
    	NbaTXLife nba1213ReqTXLife = createTXLifeRequest(NbaOliConstants.TC_TYPE_OLSA, NbaOliConstants.OLI_TC_NULL, getUserVO().getUserID());
    	OLifE olife1213 = new OLifE();
		setSourceVendorInfo1213(nba1213ReqTXLife,olife1213);
		addHoldingInfo1213(olife1213, getNbaTXLife());
		addPrimWritAgentPartyRelation1213(olife1213, getNbaTXLife());
		nba1213ReqTXLife.setOLifE(olife1213);
		return nba1213ReqTXLife;
    }
    
    /**
	 * @param userVO
	 * @return
	 */
    public NbaTXLife createTXLifeRequest(long transType, long transSubType, String businessUser) { 
		NbaTXLife nbaReqTXLife = super.createTXLifeRequest(transType, transSubType, businessUser);
		nbaReqTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaReqTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransMode(NbaOliConstants.TC_CONTENT_UPDATE);
		return nbaReqTXLife;
	}
	
	/**
	 * @param nba1213ReqTXLife
	 * @param txLife
	 */
	protected void addPrimWritAgentPartyRelation1213(OLifE olife1213, NbaTXLife txLife) {
		Relation relationPrimWritAgent = txLife.getRelationForRelationRoleCode(NbaOliConstants.OLI_REL_PRIMAGENT).clone(true);
		if(relationPrimWritAgent != null){
			olife1213.addRelation(relationPrimWritAgent);
			Party partyPrimWritAgent = txLife.getParty(relationPrimWritAgent.getRelatedObjectID()).getParty();
			olife1213.addParty(partyPrimWritAgent);
		}
	}

	/**
	 * @param olifeFor1213
	 */
	protected void setSourceVendorInfo1213(NbaTXLife nba1213ReqTXLife, OLifE olifeFor1213) {
		VendorApp vendorApp1213 = new VendorApp();
		VendorName vendorName = new VendorName();
		vendorName.setPCDATA(NbaOliConstants.VENDOR_APP_1213_VENNAME);
		vendorName.setVendorCode(NbaOliConstants.VENDOR_APP_1213_VENCODE);
		vendorApp1213.setVendorName(vendorName);
		vendorApp1213.setAppName(NbaOliConstants.SOURCE_INFO_FILECTRLID_1213);
		vendorApp1213.setAppVer(NbaOliConstants.VENDOR_APP_1213_APPVER);
		nba1213ReqTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getUserAuthRequest().setVendorApp(vendorApp1213);
		SourceInfo sourceInfo = new SourceInfo();
		sourceInfo.setSourceInfoDescription(NbaOliConstants.SOURCE_INFO_SOURCEINFODESC_1213);
		sourceInfo.setSourceInfoComment(NbaOliConstants.SOURCE_FUNDS_DETAILS_1213);
		sourceInfo.setFileControlID(NbaOliConstants.SOURCE_INFO_FILECTRLID_1213);
		olifeFor1213.setSourceInfo(sourceInfo);
	}

	/**
	 * @param nba1213ReqTXLife
	 * @param txLife
	 */
	protected void addHoldingInfo1213(OLifE olife1213, NbaTXLife txLife) {
		Holding holding1213 = new Holding();
		Policy policy1213 = new Policy();
		Policy policy = txLife.getPolicy();
		policy1213.setId("Policy_1");
		policy1213.setPolNumber(policy.getPolNumber());
		policy1213.setCarrierCode(policy.getCarrierCode());
		policy1213.setPolicyStatus(policy.getPolicyStatus());
		policy1213.setPaymentMethod(policy.getPaymentMethod());
		Iterator finActivityItr = policy.getFinancialActivity().iterator();
		while(finActivityItr.hasNext()){
			FinancialActivity finActivity = (FinancialActivity) finActivityItr.next();
			FinancialActivityExtension finActExt = NbaUtils.getFirstFinancialActivityExtension(finActivity);
			if ((finActivity.getPaymentCount() > 0) && !(NbaOliConstants.SOURCE_FUNDS_DETAILS_1213.equalsIgnoreCase(finActivity.getPaymentAt(0).getSourceOfFundsDetails())
					|| finActExt.getDisbursedInd() || NbaOliConstants.OLI_FINACTSUB_REFUND == finActivity.getFinActivitySubType())) {
				policy1213.addFinancialActivity(finActivity);
			}
		}
		holding1213.setId(HOLDING_ID);
		holding1213.setPolicy(policy1213);
		olife1213.addHolding(holding1213);
	}
	//SR494086.6 New Method ADC Retrofit
	public boolean isCallNeeded() {
		if (NbaUtils.isAdcApplication(getNbaDst())) {
			return false;
		}
		return true;
	}
}
