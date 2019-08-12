package com.csc.fsg.nba.correspondence;

/*
 * **************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group�.  The use,<BR>
 * reproduction, distribution or disclosure of this program, in whole or in<BR>
 * part, without the express written permission of CSC Financial Services<BR>
 * Group is prohibited.  This program is also an unpublished work protected<BR>
 * under the copyright laws of the United States of America and other<BR>
 * countries.  If this program becomes published, the following notice shall<BR>
 * apply:
 *     Property of Computer Sciences Corporation.<BR>
 *     Confidential. Not for publication.<BR>
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * **************************************************************************<BR>
 */
import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaCorrespondenceRequestVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * This interface exposes methods that all nbA Correspondence Adapters much provide.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA013</td><td>Version 2</td><td>Correspondence System</td></tr>
 * <tr><td>NBA129</td><td>Version 5</td><td>xPression Integration</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unfied User Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */

public interface NbaCorrespondenceAdapter {
/**
 * This method frees up all resources used by the correspondence system.
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception
 * whenever an error occurs.
 */
void freeResources() throws com.csc.fsg.nba.exception.NbaBaseException;
/**
 * This method returns the extract data used for Letter generation.
 * @return java.lang.String
 */
String getExtract();
/**
 * This method returns a List off applicable correspondence categories.
 * @return java.util.List
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception
 * whenever an error occurs. 
 */
//NBA129 new method
List getCategoryNames() throws NbaBaseException;
/**
 * This method returns the extract data used for Letter generation.
 * @return java.lang.String
 */
//NBA129 new method
String createExtract(String strLetterName)throws NbaBaseException;
/**
 * This method returns the Letter in HTML format, which basically represents a preview of the letter.
 * The PDF letter generation request requires the name of the letter to be generated.
 * @return byte[]
 * @param strLetterName A letter Name
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception
 * whenever an error occurs. 
 */
byte[] getLetterAsHTML(String strLetterName) throws NbaBaseException;
/**
 * This method returns the Letter in PDF format. The PDF letter generation request requires
 * the name of the letter to be generated, and the variable XML representing the changed letter contents
 * after a preview.
 * @return byte[]
 * @param strLetterName Name of the letter to be gererated.
 * @param strChangedXml Changed letter contents obtained from the HTML preview of the letter
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception
 * whenever an error occurs. 
 */
byte[] getLetterAsPDF(String strLetterName, String strChangedXml) throws NbaBaseException;
/**
 * This method returns a List off applicable correspondence letters.
 * @param categoryName paramter for xPression to only return documents ina specific category
 * @return java.util.List
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception
 * whenever an error occurs. 
 */
//NBA129 changed method signature
List getLetterNames(String categoryName) throws NbaBaseException; 
/**
 * Intialize all objects needed by the Correspondence system.
 * @param dst A <code>NbaDst</code> object representing the contract for which a letter needs to be generated.
 * @param UserID The user to log on to the correspondence system
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception
 * whenever an error occurs.
 */
//NBA129, NBA213 Changed method signature
void initializeObjects(NbaDst dst, NbaUserVO user) throws NbaBaseException; 
/**
 * This method marks the letter for batch printing. 
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception
 * whenever an error occurs.
 */
void markForBatchPrint() throws com.csc.fsg.nba.exception.NbaBaseException;
/**
 * This method can be used to provide the extract for the letter to be generated.
 * @param extract java.lang.String
 */
void setExtract(String extract);

/**
 * This method can be used to set the Parent DST of Correspondecne WI .
 * @param dst A <code>NbaDst</code> object representing the contract for which a letter needs to be generated.
 */
void setParentDst(NbaDst dst); //ALS4476

void setLetterType(String string);//AXAL3.7.32

String getLetterNameFromSource() throws com.csc.fsg.nba.exception.NbaBaseException; //APSL4270
void initializeObjects(NbaCorrespondenceRequestVO correspondenceRequestVO ) throws NbaBaseException;//APSL5200

//NBLXA-2114 new method with changed method signature
void initializeObjects(NbaDst dst, NbaUserVO user,NbaTXLife nbaTxLife) throws NbaBaseException; 

}
