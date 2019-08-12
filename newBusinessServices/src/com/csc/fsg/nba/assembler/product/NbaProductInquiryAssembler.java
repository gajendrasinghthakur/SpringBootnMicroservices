/*******************************************************************************
 * 
 * Copyright Notice (2006) (c) CSC Financial Services Limited 1996-2006. All
 * rights reserved. The software and associated documentation supplied hereunder
 * are the confidential and proprietary information of CSC Financial Services
 * Limited, Austin, Texas, USA and are supplied subject to licence terms. In no
 * event may the Licensee reverse engineer, decompile, or otherwise attempt to
 * discover the underlying source code or confidential information herein.
 *  
 ******************************************************************************/
package com.csc.fsg.nba.assembler.product;

import java.util.HashMap;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.product.assembler.ProductInquiryAssembler;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.dataobject.accel.product.AccelProductDataObject;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaHolding;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Rider;

public class NbaProductInquiryAssembler extends ProductInquiryAssembler {
	Map plans = new HashMap(10);
	public Result disassemble(Object input) {
		NbaTXLife nbaTxLife = (NbaTXLife) input;
		AccelProductDataObject accelProdDO = createAccelProductDO(nbaTxLife);
		Result result = new AccelResult();
		getPlans().put(accelProdDO.getProductKey(), accelProdDO.getProductKey());
		result.addResult(accelProdDO);
		if (nbaTxLife.isLife()) {
			addCoverageProducts(result, nbaTxLife);
		} else if (nbaTxLife.isAnnuity()) {
			addRiderProducts(result, nbaTxLife);
		}
		
		
		return result;

	}
	
	/**
	 * @param nbaTxLife
	 * @return
	 */
	private AccelProductDataObject createAccelProductDO(NbaTXLife nbaTxLife) {
		NbaHolding nbaHolding = nbaTxLife.getNbaHolding();
		AccelProductDataObject accelProdDO = new AccelProductDataObject();
		accelProdDO.setCompanyKey(nbaTxLife.getCarrierCode());
		accelProdDO.setProductKey(nbaTxLife.getProductCode());
		accelProdDO.setBackendKey(nbaTxLife.getOLifE().getSourceInfo().getFileControlID());
		accelProdDO.setEffDateKey(nbaHolding.getApplicationInfo().getSignedDate());
		accelProdDO.setExpDateKey(nbaHolding.getApplicationInfo().getSignedDate());
		return accelProdDO;
	}

	protected void addCoverageProducts(Result result, NbaTXLife nbaTXLife) { //NBA237
		Life life = null;
		Policy policy = nbaTXLife.getPrimaryHolding().getPolicy();
		if ((policy != null) && (policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() != null)) {
			life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
		}
		if (life != null) {
			int count = life.getCoverageCount();
			for (int i = 0; i < count; i++) {
				Coverage coverage = life.getCoverageAt(i);
				if ((coverage != null)
						&& (coverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_RIDER || coverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_INTEGRATED)) {
					// if the PolicyProduct is already loaded, no need to reload
					if (getPlans().get(coverage.getProductCode()) != null) {
						continue;
					}
					AccelProductDataObject accelProdDO = createAccelProductDO(nbaTXLife);
					accelProdDO.setProductKey(coverage.getProductCode());
					getPlans().put(accelProdDO.getProductKey(), accelProdDO.getProductKey());
					if (coverage.hasEffDate()) {
						accelProdDO.setEffDateKey(coverage.getEffDate());
						accelProdDO.setExpDateKey(coverage.getEffDate());
					} 
					result.addResult(accelProdDO);
						
				}
			}
		}
	}
	
	protected void addRiderProducts(Result result, NbaTXLife nbaTXLife) { 
		Annuity annuity = null;
		Policy policy = nbaTXLife.getPrimaryHolding().getPolicy();
		if ((policy != null) && (policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() != null)) {
			annuity = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity();
		}
		if (annuity != null) {
			int count = annuity.getRiderCount();
			for (int i = 0; i < count; i++) {
				Rider rider = annuity.getRiderAt(i);
				if (rider != null) {
					// begin SPR1817
					// if the PolicyProduct is already loaded, no need to reload
					if (getPlans().get(rider.getRiderCode()) != null) {  
						continue;
					}
					AccelProductDataObject accelProdDO = createAccelProductDO(nbaTXLife);
					accelProdDO.setProductKey(rider.getRiderCode());  
					if (rider.hasEffDate()) {
						accelProdDO.setEffDateKey(rider.getEffDate());
						accelProdDO.setExpDateKey(rider.getEffDate());
					}
					getPlans().put(accelProdDO.getProductKey(), accelProdDO.getProductKey());
				}
			}
		}
		
	}
	public Map getPlans() {
		return plans;
	}
	public void setPlans(Map plans) {
		this.plans = plans;
	}
}
