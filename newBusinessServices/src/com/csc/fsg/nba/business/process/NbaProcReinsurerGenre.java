package com.csc.fsg.nba.business.process;

/*
 * ************************************************************** <BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group?.  The use,<BR>
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
import java.io.DataInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.reinsurance.rgaschema.AttachedFile;
import com.csc.fsg.nba.reinsurance.rgaschema.Case;
import com.csc.fsg.nba.reinsurance.rgaschema.Cases;
import com.csc.fsg.nba.reinsurance.rgaschema.Document;
import com.csc.fsg.nba.reinsurance.rgaschema.Documents;
import com.csc.fsg.nba.reinsurance.rgaschema.NbaRgaRequest;
import com.csc.fsg.nba.reinsurance.rgaschema.ReinsuranceCases;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaSource;

/**
 * <code>NbaProcReinsurerGenRe</code> handles communications between nbAccelerator
 * and AXA EIB for GenRE reinsurance.  It extends the NbaProcReinsurerCommunications class, which drives the process,
 * and supplies Gen Re specific functionality.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.32</td><td>Axa Life Phase 2</td><td>Reinsurance Interface</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaProcReinsurerGenre extends NbaProcReinsurerCommunications {
	/**
	 * NbaProcReinsurerGenRe default constructor.
	 */
	public NbaProcReinsurerGenre() {
		super();
	}

	/**
	 * For Swiss Re processing,  this will sets the URL (path) from the NbaConfiguration file.
	 */
	public void initializeTarget() throws NbaBaseException {
		setTarget(getConfigRien().getUrl());
	}
	// New Method to return file type when requested for GenRe (NBLXA-2231)
	public Object doReinsurerSpecificProcessing(NbaSource aSource) throws NbaBaseException {
		NbaRgaRequest nbaRgaRequest = new NbaRgaRequest(aSource.getText());
		ReinsuranceCases reinsuranceCases = nbaRgaRequest.getReinsuranceCases();
		// Add all the images in the resinurance request.
		List images = getDocumentsToCopy(reinsuranceCases.getCases());
		for (int i = 0; i < images.size(); i++) {
			byte[] byteArr = NbaBase64.decode((String) ((Map) images.get(i)).get(DATA));
			Snow.Snowbnd snbd = new Snow.Snowbnd();
			DataInputStream imageDataInputStream;
			imageDataInputStream = new DataInputStream(new java.io.ByteArrayInputStream(byteArr));
			int filetypecode = snbd.IMGLOW_get_filetype(imageDataInputStream);   // Will return FileType code defined in snowbound
			String filetype = decodeFileType(filetypecode);
			AttachedFile attachedFile = new AttachedFile();
			attachedFile.setFileName((String) ((Map) images.get(i)).get(FILENAME));
			attachedFile.setFile(((String) ((Map) images.get(i)).get(DATA)));
			attachedFile.setFileType(filetype);
			reinsuranceCases.addAttachedFile(attachedFile);
		}
		// Create attachedFile object for Correspondence Letter
		AttachedFile attachedCorrespondenceLetter = new AttachedFile();
		String letterName = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.REINSURANCE_LETTER_NAME);
		attachedCorrespondenceLetter.setFileName(letterName);
		byte[] letter = generateCorrespondenceLetter(letterName);
		saveReinsuranceLetterAsSource(work, letter, A_ST_CORRESPONDENCE_LETTER);// NBLXA-1331 //LOB values to be set on source can be passed as
																				// argument.
		attachedCorrespondenceLetter.setFile(NbaBase64.encodeBytes(letter));
		attachedCorrespondenceLetter.setFileType("TIFF");
		reinsuranceCases.addAttachedFile(attachedCorrespondenceLetter);
		Cases cases = reinsuranceCases.getCases();
		Case aCase = cases.getCaseAt(0);
		if (aCase != null) {
			Documents documents = aCase.getDocuments();
			if (documents == null) {// ALII377
				documents = new Documents();
				documents.setCount("0");
				aCase.setDocuments(documents);
			}
			Document document = new Document();
			documents.addDocument(document);
			long count = documents.getCount().longValue() + 1;
			document.setID(String.valueOf(count));
			document.setFilename(letterName);
			documents.setCount(String.valueOf(count));
		}

		return nbaRgaRequest.toXmlString();
	}

	//New Method to render ImageType NBLXA-2231
	private String decodeFileType(int filetypecode) {

		String fileType = null;
		Set pdfSet = new HashSet();
		pdfSet.add(59);
		pdfSet.add(79);
		pdfSet.add(92);

		if (pdfSet.contains(filetypecode)) {
			fileType = "PDF";
		} else {
			fileType = "TIFF";
		}
		return fileType;
	}
}
