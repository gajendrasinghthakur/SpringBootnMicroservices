package com.csc.fsg.nba.business.transaction.datachange;
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
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;

/**
 * 
 * Helper classes to determine Data change 
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>AXAL3.7.07</td><td>AXA Life Phase 2</td><td>Data Change Architecture</td>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaDataChangeSubstandardRatingComparator extends AxaDataChangeComparator {
	protected SubstandardRating oldSubstandardRating;

	protected SubstandardRating newSubstandardRating;

	protected SubstandardRatingExtension newSubstandardRatingExtension;

	protected SubstandardRatingExtension oldSubstandardRatingExtension;

	/**
	 * @param oldSubstandardRating
	 * @param newSubstandardRating
	 */
	public AxaDataChangeSubstandardRatingComparator(SubstandardRating newSubstandardRating, SubstandardRating oldSubstandardRating) {
		super();
		this.oldSubstandardRating = oldSubstandardRating;
		this.newSubstandardRating = newSubstandardRating;
		oldSubstandardRatingExtension = NbaUtils.getFirstSubstandardExtension(oldSubstandardRating);
		newSubstandardRatingExtension = NbaUtils.getFirstSubstandardExtension(newSubstandardRating);
	}

	/**
	 * 
	 * @return
	 */
	public boolean isNewSubstandardRatingExtension() {
		return (newSubstandardRatingExtension != null && newSubstandardRatingExtension.isActionAdd() && oldSubstandardRatingExtension == null);
	}

	/**
	 * 
	 * @return
	 */
	public boolean isNewSubstandardRating() {
		return (newSubstandardRating != null && newSubstandardRating.isActionAdd() && oldSubstandardRating == null);
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSubstandardRatingDeleted() {
		return newSubstandardRating != null && oldSubstandardRating != null && NbaUtils.isDeletedOnly(newSubstandardRating);//ALS3680 changed the NbaUtils method called
	}

	/**
	 * 
	 * @return
	 */
	public boolean isPermFlatExtraRatingAdded() {
		return isNewSubstandardRating() && isNewSubstandardRatingExtension() && newSubstandardRatingExtension.hasPermFlatExtraAmt();
	}

	/**
	 * 
	 * @return
	 */
	public boolean isPermFlatExtraRatingDeleted() {
		return isSubstandardRatingDeleted() && newSubstandardRatingExtension.hasPermFlatExtraAmt();
	}

	/**
	 * 
	 * @return
	 */
	public boolean isTempFlatExtraRatingAdded() {
		return isNewSubstandardRating() && newSubstandardRating.hasTempFlatExtraAmt();
	}

	/**
	 * 
	 * @return
	 */
	public boolean isTempFlatExtraRatingDeleted() {
		return isSubstandardRatingDeleted() && newSubstandardRating.hasTempFlatExtraAmt();
	}

	/**
	 * 
	 * @return
	 */
	public boolean isPermTableRatingAdded() {
		return isNewSubstandardRating() && newSubstandardRating.hasPermTableRating();
	}

	/**
	 * 
	 * @return
	 */
	public boolean isPermTableRatingDeleted() {
		return isSubstandardRatingDeleted() && newSubstandardRating.hasPermTableRating();
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSubstandardRatingChanged() {
		return isNewSubstandardRating() || isSubstandardRatingDeleted() || isPermFlatExtraRatingAdded() || isPermFlatExtraRatingDeleted()
				|| isTempFlatExtraRatingAdded() || isTempFlatExtraRatingDeleted() || isPermFlatExtraRatingAdded() || isPermFlatExtraRatingDeleted();
	}

}
