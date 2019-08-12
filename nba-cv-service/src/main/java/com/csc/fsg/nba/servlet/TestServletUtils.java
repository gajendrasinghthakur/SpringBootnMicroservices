/*
 * Created on Mar 24, 2011
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.csc.fsg.nba.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * @author tbagga
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestServletUtils {
	/*
	 * This method puts attachment data inside a CDATA block. It also replaces 'Height & Weight' by 'Height And Weight'. The objective is to delete
	 * prologue <? ... ?> in AttachmentData and put its contents inside <![CDATA[ ... ]]>
	 */
	public static String editXML(String fileName) throws Exception {
		byte[] buffer = new byte[(int) new File(fileName).length()];
		BufferedInputStream f = new BufferedInputStream(new FileInputStream(fileName));
		f.read(buffer);
		String originalXml = new String(buffer);
		f.close();

		String modifiedXml = "";
		int beginIndex = 0;
		//Find index of prologue
		int attXmlPrologueIndex = originalXml.indexOf("<?xml");
		while (attXmlPrologueIndex != -1) {
			//Copy XML contents from last begin point to one place earlier than prologue
			modifiedXml += originalXml.substring(beginIndex, attXmlPrologueIndex);
			//Insert <![CDATA[ and skip all characters upto end of prologue
			modifiedXml += "<![CDATA[";
			int attXmlDataBeginIndex = originalXml.indexOf("?>", attXmlPrologueIndex) + 2;
			//Recalculate the position to determine where the actaul attachment data starts from, we are not interested in white spaces.
			attXmlDataBeginIndex = originalXml.indexOf("<", attXmlDataBeginIndex);
			//Need to determine which character is more close to starting char of attachment data ('<') - '>' or ' '
			//Attachment data can start from tags like <TXLife ...> or <SpecialInstruction>. Based on that we will construct the end tag on fly.
			int closingCharDist = originalXml.indexOf(">", attXmlDataBeginIndex);
			int spaceCharDist = originalXml.indexOf(" ", attXmlDataBeginIndex);
			String attXmlDataBeginTag = originalXml.substring(attXmlDataBeginIndex, ((closingCharDist > spaceCharDist) ? spaceCharDist
					: closingCharDist));
			String attXmlDataBeginTagName = attXmlDataBeginTag.substring(1, attXmlDataBeginTag.length());
			String attXmlDataEndTag = ("</" + attXmlDataBeginTagName + ">");
			//Copy attachment data from attachment begin index to attachment end index and close CDATA block.
			int attEndIndex = originalXml.indexOf(attXmlDataEndTag, attXmlDataBeginIndex) + attXmlDataEndTag.length();
			modifiedXml += originalXml.substring(attXmlDataBeginIndex, attEndIndex);
			modifiedXml += "]]>";
			//Determine new indices for the loop
			beginIndex = attEndIndex + 1;
			attXmlPrologueIndex = originalXml.indexOf("<?xml", beginIndex);
		}
		//Append rest of the string.
		modifiedXml += originalXml.substring(beginIndex);

		modifiedXml = modifiedXml.replaceAll("Height & Weight", "Height And Weight");
		System.out.println(modifiedXml);
		return modifiedXml;
	}
	
}
