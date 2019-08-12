package com.csc.fsg.nba.provideradapter;

/*
 * ************************************************************** <BR>
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
 * ************************************************************** <BR>
 */
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.dom4j.io.XMLResult;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.csc.fsg.nba.business.process.NbaAutomatedProcess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataException;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.tbf.xml.XmlValidationError;

/**
 * NbaGenericProviderAdapter parses the results received from a provider, updates AWD LOB fields based
 * on those results and add additional sources, as required.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACN014</td><td>Version 4</td><td>121/1122 Migration</td></tr>
 * <tr><td>ACN009</td><td>Version 4</td><td>MIB 401/402 Migration</td></tr>
 * <tr><td>SPR2580</td>Version 5</td><td>Incorrect Error message in AWD when an MIB response is recieved.</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR3311</td><td>Version 8</td><td>Pre-existing temporary Requirements are not matched to a new Requirement</td></tr>
 * <tr><td>AXAL3.7.31</td><td>Axa Life Phase 1</td><td>Provider Interface - MIB</td></tr>
 * <tr><td>SPRNBA-597</td><td>Version NB-1301</td><td> Image data should not be stored in Attachments</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see NbaProviderAdapterFacade
 * @since New Business Accelerator - Version 4
 */

public class NbaGenericProviderAdapter extends NbaProviderAdapter implements NbaConstants {
	public java.lang.StringBuffer errorMsg = new StringBuffer();
	private com.csc.fsg.nba.foundation.NbaLogger logger = null;
	protected static final String IMAGE_DATA_REMOVED = "Image Data removed.";		//SPRNBA-597
	/**
	 * NbaGenericProviderAdapter constructor comment.
	 */
	public NbaGenericProviderAdapter() {
	
	}
/**
 * This method converts the XML Requirement transactions into a format
 * that is understandable by the provider.
 * @param aList array list of requirement transactions
 * @return a provider ready message.
 * @exception NbaBaseException thrown if an error occurs.
 */
public Map convertXmlToProviderFormat(List aList)throws NbaBaseException {
	Map aMap = new HashMap();
	if (aList.size() == 0) {
		throw new NbaBaseException("XMLife is required");
	}
	// SPR3290 code deleted
	NbaTXLife txlife = null;
	NbaTXLife cmpTxlife = null; //ACN014
	for (int i = 0; i < aList.size(); i++) {
		txlife = (NbaTXLife) aList.get(i);
		if (getLogger().isDebugEnabled()) { // NBA027
			getLogger().logDebug("Generic Adapter Request for transformation:\n" + txlife.toXmlString());
		} // NBA027
		//BEGIN ACN014
		validate(txlife);
		cmpTxlife = createTXLife(txlife, cmpTxlife);
		//END ACN014
		if (errorMsg.length() > 0) {
			aMap.put(txlife.getTransRefGuid(), errorMsg.toString());
		}
		errorMsg.delete(0, errorMsg.length());
	} 
	aMap.put(NbaConstants.TRANSACTION, cmpTxlife.toXmlString()); //ACN014
	return aMap;
}
	/**
	 * This method creates a combined (or bundled) XML requirement request
	 * by adding the RequirementInfo object from the txlife object to the
	 * cmpTxlife object and then returning the updated cmpTxlife object.
	 * If the cmpTxlife object is null, it simply returns the txlife object
	 * passed in.
	 * @param txlife The NbaTXLife object containing the RequiementInfo object
	 * to be added
	 * @param cmpTxlife The NbaTXLife object to which a RequirementInfo object
	 * it to be added
	 * @return an updated NbaTXLife object
	 */
	//ACN014 New Method
	private NbaTXLife createTXLife(NbaTXLife txlife, NbaTXLife cmpTxlife) {
		if (cmpTxlife == null) {
			return txlife;
		}
		Policy cmpPolicy = cmpTxlife.getPolicy();
		int count = txlife.getPolicy().getRequirementInfoCount();
		for (int i = 0; i < count; i++) {
			cmpPolicy.addRequirementInfo(txlife.getPolicy().getRequirementInfoAt(i));
		}
		return cmpTxlife;
	}
/**
 * This method updates the provider result source on the workitem with generated
 * XMLife message. It also updates the workitem LOBs.
 * 
 * @param work - the work item.
 * @param map - the mapping hash map
 * @param txLife - the XMLife response message
 */
protected int determineRequirementType(String results) throws NbaBaseException {
	return 0;
}
/**
 * returns instance of NbaLogger.
 * 
 * @return com.csc.fsg.nba.foundation.NbaLogger
 */
protected NbaLogger getLogger() {
    if (logger == null) {
        try {
            logger = NbaLogFactory.getLogger(NbaGenericProviderAdapter.class.getName());
        } catch (Exception e) {
            NbaBootLogger.log("NbaGenericProviderAdapter could not get a logger from the factory.");
            e.printStackTrace(System.out);
        }
    }
    return logger;
}
/**
 * This method parse the batch response from MIB into indiviaual response streams. 
 * @return the map with all individual response streams
 * @param the batch response string
 */
protected Map parseBatch(String response) throws NbaBaseException {
	try {
		NbaTXLife nbaTxLife = new NbaTXLife(response);
		// SPR3290 code deleted
		Map tempMap = new HashMap();
		Policy policy = nbaTxLife.getPolicy();
		ArrayList list = new ArrayList();
		if (policy != null) {
			int count = policy.getRequirementInfoCount();
			for (int i = 0; i < count; i++) {
				list.add(nbaTxLife);
			}
			tempMap.put(RESPONSE_LIST, list);
		} else {
			tempMap.put("ERROR_MSG", "No Requirments found");
		}
		return tempMap;
	} catch (Exception e) {
		throw new NbaBaseException(e);
	}
}
/**
 * This method converts the Provider's response into XML transaction.It 
 * also updates required LOBs and result source with converted XMLife.
 * @param work the requirement work item.
 * @return the requirement work item with formated source.
 * @exception NbaBaseException thrown if an error occurs.
 */
public ArrayList processResponseFromProvider(NbaDst work, NbaUserVO user) throws NbaBaseException, NbaDataException {
	String response = getDataFromSource(work);
	ArrayList aList = new ArrayList();
	try {
		NbaTXLife nbaTxLife = new NbaTXLife(response);
		Vector vctrErrors = nbaTxLife.getTXLife().getValidationErrors(false);
		if (vctrErrors != null && vctrErrors.size() > 0) {
			int count = vctrErrors.size();
			StringBuffer errorString = new StringBuffer();
			for( int ndx = 0; ndx < count; ndx++) {
				XmlValidationError error = (XmlValidationError)vctrErrors.get(ndx);
				if (error != null) {
					errorString.append("Error(" + ndx + "): " + error.getErrorMessage() + "\n");
				} else {
					errorString.append("A problem occurred retrieving the validation error.");
				}
			}
			throw new NbaDataException("Provider Validation failed; response invalid.\nValidation Error(s):\n" + errorString.toString()); //SPR2580 
		} 
		Policy policy = nbaTxLife.getPolicy();
		if (policy == null || policy.getRequirementInfoCount() == 0) {
			throw new NbaDataException("Provider response requirement info is missing or invalid");//SPR2580
		}
		//begin SPRNBA-597
		List requirementInfoImagesList = stripImagesFromAttachments(policy); // Get a List of images from the <Attachments> and removed the image bytes from the 1122
		response = nbaTxLife.toXmlString(); // Save the 1122 with images removed.
		getProviderSupplementSource().setText(response);  //Update the original NBPROVSUPP Source 
		getProviderSupplementSource().setUpdate();
		//end SPRNBA-597
		//Begin NBA130
		NbaLob workLob = work.getNbaLob();
		//ACN009 begin
		if (!policy.hasPolNumber() || policy.getPolNumber().indexOf(NbaAutomatedProcess.CONTRACT_DELIMITER) == -1) { //SPR3311
			workLob.setCompany(policy.getCarrierCode());
			if (nbaTxLife.getOLifE().getSourceInfo() != null) {
				workLob.setBackendSystem(nbaTxLife.getOLifE().getSourceInfo().getFileControlID());
			}
			if (NbaUtils.isBlankOrNull(workLob.getPolicyNumber())) { //APSL2808
				workLob.setPolicyNumber(policy.getPolNumber());
			}
		} else {
			NbaStringTokenizer tokens = new NbaStringTokenizer(policy.getPolNumber(), NbaAutomatedProcess.CONTRACT_DELIMITER);
			if (tokens.hasMoreTokens()) {
				workLob.setCompany(tokens.nextToken());
				workLob.setBackendSystem(tokens.nextToken());
				if (NbaUtils.isBlankOrNull(workLob.getPolicyNumber())) { //APSL2808
					workLob.setPolicyNumber(policy.getPolNumber());
				}
			}
		}
		workLob.setReqReceiptDate(new Date());
		workLob.setReqReceiptDateTime(NbaUtils.getStringFromDateAndTime(new Date()));//QC20240
		//End NBA130
		//ACN009 code deleted
		//ACN009 end
		work = updateWorkItem(work, nbaTxLife.getPolicy().getRequirementInfoAt(0), (List) requirementInfoImagesList.get(0), nbaTxLife); //ACN014 SPRNBA-597
		aList.add(work);
		int count = policy.getRequirementInfoCount();
		for (int i = 1; i < count; i++) {
			// create transaction
			NbaDst tempTrans = createTransaction(user, work);
			//Begin NBA130
			NbaLob tempLob = tempTrans.getNbaLob();
			//ACN014 begin
			tempLob.setPolicyNumber(workLob.getPolicyNumber());
			tempLob.setBackendSystem(workLob.getBackendSystem());
			tempLob.setCompany(workLob.getCompany());
			//ACN014 end
			tempLob.setReqVendor(workLob.getReqVendor());
			tempLob.setReqReceiptDate(workLob.getReqReceiptDate());
			tempLob.setReqReceiptDateTime(workLob.getReqReceiptDateTime());//QC20240
			tempTrans.addNbaSource(new NbaSource(NbaConstants.A_BA_NBA, NbaConstants.A_ST_PROVIDER_SUPPLEMENT, response));
			tempTrans = updateWorkItem(tempTrans, nbaTxLife.getPolicy().getRequirementInfoAt(i), (List) requirementInfoImagesList.get(i), nbaTxLife); //ACN014  SPRNBA-597
			aList.add(tempTrans);
			//End NB130
		}
		return aList;
	} catch (SAXParseException spe) {
		throw new NbaDataException("Provider Validation failed; response invalid."); //SPR2580
	} catch (NbaDataException nde) {
		throw nde;
	} catch (Exception e) {
		throw new NbaBaseException("Provider Validation failed\n" + e.toString(), e);	//SPR3311
	}
}
/**
 * This method validates the NbaTXLife object by processing it against
 * an XSLT stylesheet.  The XSLT stylesheet to be used for validation
 * is obtained from the FileLocation section of the NbaConfiguration.xml
 * file using the keyword NbaConfigurationConstants.XSL_REQUIREMENT_VALIDATOR.
 * 
 * @param nbaTxLife The NbaTXLife object to be validated
 */
//ACN014 New Method
private void validate(NbaTXLife nbaTxLife) throws NbaBaseException {
	try {
		TransformerFactory tfactory = TransformerFactory.newInstance();
		if (tfactory.getFeature(SAXSource.FEATURE)) {
			SAXParserFactory pfactory = SAXParserFactory.newInstance();
			pfactory.setNamespaceAware(true);
			pfactory.setValidating(false); // Turn off validation.
			XMLReader reader = pfactory.newSAXParser().getXMLReader();
			Handler handler = new Handler();
			reader.setErrorHandler(handler);

			Transformer t = tfactory.newTransformer(new StreamSource(
				NbaConfiguration.getInstance().getFileLocation(
				NbaConfigurationConstants.XSL_REQUIREMENT_VALIDATOR)));
			NbaValidateXMLErrorListener myListener = new NbaValidateXMLErrorListener();
			t.setErrorListener(myListener);
			InputSource input = new InputSource();
			InputStream in = null;
			try {
				in = new ByteArrayInputStream(nbaTxLife.toXmlString().getBytes());
			} catch (Exception ex) {
				getLogger().logDebug("Unable to generate bytestream for NbaTXLife object that follows: " + nbaTxLife.toXmlString());
				throw new NbaBaseException("Unable to generate bytestream for NbaTXLife object", ex);
			}
			input.setByteStream(in);
			SAXSource source = new SAXSource(reader, input);

			try {
				// SPR3290 code deleted
				XMLResult result = new XMLResult();
				t.transform(source, result);
			} catch (TransformerException te) {
				throw new NbaBaseException("Not a SAXParseException warning or error: " + te.getMessage(), te);
			}
			if (myListener.getBuffer().length() > 0) {
				errorMsg.append(myListener.getBuffer().toString());
				getLogger().logDebug(nbaTxLife.toXmlString());
			}
		} else
			throw new NbaBaseException("tfactory does not support SAX features!");
	} catch (ParserConfigurationException cfe) {
		throw new NbaBaseException(cfe);
	} catch (SAXException se) {
		throw new NbaBaseException(se);
	} catch (TransformerConfigurationException tce) {
		throw new NbaBaseException(tce);
	}
}
/**
 * For each  RequirementInfo in the 1122,  store a List containing the image data contained in its <Attachment>s into a List. The list is indexed by the RequirementInfo occurrence number.
 * Replace the image data with a String indicating that the image data has been removed.
 * @param policy - the Policy 
 * @return a List containing the image data. Each entry corresponds to a RequirementInfo. Each entry contains another List containing the images for that RequirementInfo.  
 * @throws NbaBaseException 
 */
//SPRNBA-597 New Method
protected List stripImagesFromAttachments(Policy policy) throws NbaBaseException {
    int requirementInfoCount = policy.getRequirementInfoCount();
    List requirementInfoList = new ArrayList(requirementInfoCount);
    for (int i = 0; i < requirementInfoCount; i++) {
        RequirementInfo requirementInfo = policy.getRequirementInfoAt(i);
        requirementInfoList.add(i, getImagesList(requirementInfo));
    }
    return requirementInfoList;
}

/**
 * For each  <Attachment> for the RequirementInfo,  store the image data contained into a List.
 * Replace the image data with a String indicating that the image data has been removed.
 * @param requirementInfo - the RequirementInfo
 * @return the list
 * @throws NbaBaseException 
 */
//SPRNBA-597 New Method
protected List getImagesList(RequirementInfo requirementInfo) throws NbaBaseException {
    int attachmentCount = requirementInfo.getAttachmentCount();
    List imagesList = new ArrayList(attachmentCount);
    for (int i = 0; i < attachmentCount; i++) {
        Attachment attachment = requirementInfo.getAttachmentAt(i);
        if (attachment.getAttachmentBasicType() == NbaOliConstants.OLI_LU_BASICATTMNTTY_IMAGE) {
            byte[] data;
            if (attachment.getAttachmentLocation() == NbaOliConstants.OLI_INLINE) { // data is included in attachment
                data = NbaBase64.decode(attachment.getAttachmentData().getPCDATA());
            } else {
                data = getImageFromExternalFile(attachment.getAttachmentData().getPCDATA());
            }
            if (data != null) {
                resetAttachmentData(attachment);
                imagesList.add(data);
            }
        } else {
            String data;
            if (attachment.getAttachmentLocation() == NbaOliConstants.OLI_INLINE) { // data is included in attachment
                data = attachment.getAttachmentData().getPCDATA();
            } else {
                data = String.valueOf(getDataFromExternalFile(attachment.getAttachmentData().getPCDATA()));
            }
            if (data != null) {
                resetAttachmentData(attachment);
                imagesList.add(data);
            }
        }
    }
    return imagesList;
}
/**
 * Reset values in the <Attachment> to indicate that the image has been removed.
 * @param attachment
 */
//SPRNBA-597 New Method
protected void resetAttachmentData(Attachment attachment) {
    attachment.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_TEXT);
    attachment.getAttachmentData().setPCDATA(IMAGE_DATA_REMOVED);
    attachment.setAttachmentLocation(NbaOliConstants.OLI_INLINE);
    attachment.deleteMimeTypeTC();
    attachment.deleteTransferEncodingTypeTC();
}
// Catch any errors or warnings from the XMLReader.
//ACN014 new class
class Handler extends DefaultHandler
{
  public void warning (SAXParseException spe)
	   throws SAXException
  {
	System.out.println("SAXParseException warning: " + spe.getMessage());
  }    

  public void error (SAXParseException spe)
	  throws SAXException
  {
	System.out.println("SAXParseException error dumbass: " + spe.getMessage());
  }     
}
}
