package com.csc.fsg.nba.process.product;

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
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fs.dataobject.accel.product.PolicyProduct;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaHolding;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.Policy;

/**
 * ProductCache provides cache for caching product data. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ALII767</td><td>PPFL Cache Enhancement</td><td>Performance Tuning</td></tr>
 * <tr><td>ALII2057-1</td><td>Axa Life Phase 2</td><td>Performance PPfL Cache enhancement</td></tr>
 * </table>
 * </p>
 * @author 	CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.configuration.NbaConfiguration
 * @since New Business Automation - Version 1
 */
public class ProductCache {
    private static ProductCache ref = new ProductCache();
    private Map ppflCache =  Collections.synchronizedMap(new LRUCache(NbaUtils.getNbaProductCacheSize())); 
    
    private static com.csc.fsg.nba.foundation.NbaLogger logger;
    private boolean termProductsCached = false;  //ALII2057-1
    private ProductCache() {
    	termProductsCached = NbaUtils.isCacheTermProducts(); //ALII2057-1
    }

    public static ProductCache getInstance() {
        return ref;
    }
    
    /**
     * 
     * @param policyProduct
     * @return ProductKey
     * Create product keys based on PolicyProduct
     */
	public ProductKey createProductKey(PolicyProduct policyProduct) {	
		ProductKey productKey = new ProductKey();
		productKey.setCompanyKey(policyProduct.getCompanyKey());
		productKey.setProductKey(policyProduct.getProductKey());
		productKey.setBackendKey(policyProduct.getBackendKey());
		productKey.setEffDateKey(policyProduct.getEffDateKey());
		productKey.setExpDateKey(policyProduct.getExpDateKey());
		return productKey;			
	}
	
	/**
	 * 
	 * @param nbaTxLife
	 * @return ProductKey
	 * Create product keys based on NbaTXLife
	 */
	public ProductKey createProductKey(NbaTXLife nbaTxLife){
		NbaHolding nbaHolding = nbaTxLife.getNbaHolding();		
		ProductKey productKey = new ProductKey();
		productKey.setCompanyKey(nbaTxLife.getCarrierCode());
		productKey.setProductKey(nbaTxLife.getProductCode());
		productKey.setBackendKey(nbaTxLife.getOLifE().getSourceInfo().getFileControlID());
		productKey.setEffDateKey(nbaHolding.getApplicationInfo().getSignedDate());
		productKey.setExpDateKey(nbaHolding.getApplicationInfo().getSignedDate());
		return productKey;
	}
		
	/**
	 * 
	 * @param nbaTxLife
	 * @return List
	 * Prepare the list of Product Keys based on NbaTXLife
	 */	
	public List prepareProductKeys(NbaTXLife nbaTxLife) {		
		Life life = null;
		List productkeysList = new ArrayList();
		if(nbaTxLife != null){
			//Prepare the base product Key
			ProductKey productKey = createProductKey(nbaTxLife);
			if(productKey !=null ){
				productkeysList.add(productKey);
				//Prepare the Coverage product Key
				Policy policy = nbaTxLife.getPrimaryHolding().getPolicy();
				if ((policy != null) && (policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() != null)) {
					life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
				}
				if (life != null) {
					int count = life.getCoverageCount();
					for (int i = 0; i < count; i++) {
						Coverage coverage = life.getCoverageAt(i);
						if ((coverage != null)
								&& (coverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_RIDER || coverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_INTEGRATED)) {
							ProductKey covProductKey = createProductKey(nbaTxLife);							
							covProductKey.setProductKey(coverage.getProductCode());
							if (coverage.hasEffDate()) {
								covProductKey.setEffDateKey(coverage.getEffDate());
								covProductKey.setExpDateKey(coverage.getEffDate());
							} 							
							productkeysList.add(covProductKey);	
						}
					}
				}
			}
		}
		return productkeysList;
	}
	/**
	 * 
	 * @param nbaTXLifeRequest
	 * @return AccelProduct
	 * Method to Fetch the product Data from product Cache 
	 */
    public AccelProduct getProductData(NbaTXLife nbaTXLifeRequest) {		
    	List productKeyList = prepareProductKeys(nbaTXLifeRequest);
    	Iterator itrProductKeyList = productKeyList.iterator();
    	List policyProductList = new ArrayList();
    	PolicyProduct policyProduct = null;
    	ProductKey productKey = null;
    	if (getLogger().isInfoEnabled()) {
            getLogger().logInfo("PPfl Cache size: " + ppflCache.size());
        }
    	while(itrProductKeyList.hasNext()){
    		productKey = (ProductKey)itrProductKeyList.next();
    		 SoftReference reference = (SoftReference) ppflCache.get(productKey);
    		 if (reference != null) { // softreference is not garbage collected 
    		 	policyProduct = (PolicyProduct) reference.get(); // getting data out of softreference
                if (policyProduct != null) {
                    if (getLogger().isInfoEnabled()) {
                        getLogger().logInfo("Hit for key : " + productKey.toString());
                    }
                    policyProductList.add(policyProduct);
                    continue;
                }
            }  
		 	// softreference is garbage collected or was never there
        	ppflCache.remove(productKey); // remove the entry from the cache if exists
                if (getLogger().isInfoEnabled()) {
                    getLogger().logInfo("Cache Entry garbage collected for key : " + productKey.toString());
                }
                return new AccelProduct();
           
    	}
    	return new AccelProduct(policyProductList);
    }
    
    /**
     * Fetch the PolicyProduct Object from AccelProduct
 	   Prepare product key from Policy product 
 	   Push the (productkey, policyproduct) object into cache map
     */
    public void cacheProductData(AccelProduct nbaProduct) {         
    	List policyProductList = nbaProduct.getAllPolicyProducts();
    	Iterator itrpolicyProductList = policyProductList.iterator();
    	while(itrpolicyProductList.hasNext()){
    		PolicyProduct policyProduct = (PolicyProduct)itrpolicyProductList.next();
    		if (excludeProduct(policyProduct.getPolicyProductTypeCode())) {  //ALII2057-1, APSL4903
    			continue;			//ALII2057-1
    		}  //ALII2057-1
    		ProductKey productKey = createProductKey(policyProduct);
    		SoftReference sr = new SoftReference(policyProduct); 
    		synchronized (ppflCache) {
    			ppflCache.put(productKey, sr);
    		}
            if (getLogger().isInfoEnabled()) {
                getLogger().logInfo("Product Cached for key : " + productKey.toString());
            }	
    	}    	
	}
    /*
     * determine if term should be cached
     */
    //ALII2057-1
    private boolean excludeProduct(long productTypeCode) {
    	return !termProductsCached && (NbaOliConstants.OLI_PRODTYPE_INDETERPREM == productTypeCode || NbaOliConstants.OLI_PRODTYPE_TERM == productTypeCode); //APSL4903
    }
    /**
     	Code to clear the cache 
     */
    public void clear(){
    	if(ppflCache !=null ){
    		ppflCache.clear();
//    		ppflCache = null;
    	}
    	if (getLogger().isInfoEnabled()) {
            getLogger().logInfo("Product Cached Cleared ");
        }	
    }
    
    /**
     * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
     * @return com.csc.fsg.nba.foundation.NbaLogger
     */
    private static NbaLogger getLogger() {
    	if (logger == null) {
    		try {
    			logger = NbaLogFactory.getLogger(ProductCache.class.getName());
    		} catch (Exception e) {
    			NbaBootLogger.log("ProductCache could not get a logger from the factory.");
    			e.printStackTrace(System.out);
    		}
    	}
    	return logger;
    }
    
    class LRUCache extends LinkedHashMap {
        public LRUCache(int maxsize) {
    	super(maxsize*4/3 + 1, 0.75f, true);
    	this.maxsize = maxsize;
        }
        protected int maxsize;
        // This method gets called everytime when an entry is added in Cache. see java documentation
        // this default implementation of this method always returns false
        // configured this method to return true when max size is reached. this will evict the least recently used entry. 
        protected boolean removeEldestEntry(Map.Entry eldest) { return size() > maxsize; }
    }    
}
