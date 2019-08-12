package com.csc.fsg.nba.process.search;

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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.database.NbaSearchFavoriteAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaSearchFavoriteCriteria;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * Class Description.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA136</td><td>Version 6</td><td>In Tray and Search Rewrite</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */
//NBA213 extends NewBusinessAccelBP
public class RetrieveSearchCriteriaBP extends NewBusinessAccelBP {

	/* (non-Javadoc)
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			NbaSearchFavoriteCriteria criteria = (NbaSearchFavoriteCriteria) input;
			if (criteria.isPrimary()) {
				result.addResult(loadPrimaryCriteria(criteria));
			} else {
				result.addResult(loadCriteria(criteria));
			}
		} catch (Exception e) {
			addExceptionMessage(result, e);
		}
		return result;
	}

	/**
	 * Returns a <code>NbaSearchFavoriteCriteria</code> populated with the primary 
	 * search criteria for a user.  Also included is a list of all saved favorite
	 * search criteria for a user.
	 * @param favoriteCriteria
	 * @return
	 * @throws NbaBaseException
	 */
	protected NbaSearchFavoriteCriteria loadPrimaryCriteria(NbaSearchFavoriteCriteria favoriteCriteria) throws NbaBaseException {
		NbaUserVO user = favoriteCriteria.getNbaUserVO();
		List criterias = NbaSearchFavoriteAccessor.selectSearchFavorites(user);
		NbaSearchFavoriteCriteria criteria = findPrimary(criterias);
		if (criteria != null) {
			criteria.setFavoritesList(criterias);
			criteria.setNbaUserVO(user);
			return loadCriteria(criteria);
		}
		return favoriteCriteria;
	}

	/**
	 * Returns the primary search criteria <code>NbaSearchFavoriteCriteria</code> from a
	 * list of search criterias.  If a primary search criteria is not selected, the first
	 * search criteria in the list is returned.
	 * @param criterias
	 * @return
	 */
	protected NbaSearchFavoriteCriteria findPrimary(List criterias) {
		NbaSearchFavoriteCriteria criteria = null;
		int count = criterias.size();
		for (int i = 0; i < count; i++) {
			criteria = (NbaSearchFavoriteCriteria) criterias.get(i);
			if (criteria.isPrimary()) {
				return criteria;
			}
		}
		criteria = null;
		if (count > 0) {
			criteria = (NbaSearchFavoriteCriteria) criterias.get(0);
		}
		return criteria;
	}

	/**
	 * Returns a <code>NbaSearchFavoriteCriteria</code> populated with the search
	 * criteria.  
	 * @param criteria
	 * @return
	 * @throws NbaBaseException
	 */
	protected NbaSearchFavoriteCriteria loadCriteria(NbaSearchFavoriteCriteria criteria) throws NbaBaseException {
		return NbaSearchFavoriteAccessor.loadSearchCriteria(criteria, criteria.getNbaUserVO());
	}
}
