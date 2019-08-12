package com.csc.fsg.nba.business.transaction;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.vo.NbaConfiguration;

public class NbaRemovingXMLNodesUtils {

	private String strResult = null;

	/**
	 * 
	 * NbaRemovingXMLNodesUtils constructor comment.
	 */
	public NbaRemovingXMLNodesUtils() {
		super();
	}

	/**
	 * @purpose This method will remove the unwanted nodes from the TXLife and return the remaining into the String.
	 * @param txLife
	 * @return
	 * @throws NbaBaseException
	 */
	public String removeNodesFromTXLife(String txLife) throws NbaBaseException {
		try {
			Document doc = convertStringToDocument(txLife);
			String removeNodeCommaSeprated = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
					NbaConfigurationConstants.REMOVE_NODES_FROM_XML);
			String[] removeNodes = removeNodeCommaSeprated.split("\\s*,\\s*");
			// This code will delete the nodes from the TXLife
			NodeList nodeList = doc.getElementsByTagName("*");
			// This code will delete the Party node for the RelationCode 239 from the TXLife
			deletePartyOfRelactionCode(doc);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				NodeList childNodeList = node.getChildNodes();
				for (String tagName : removeNodes) {
					for (int j = 0; j < childNodeList.getLength(); j++) {
						Node childNode = childNodeList.item(j);
						if (childNode.getNodeName().equalsIgnoreCase(tagName)) {
							System.out.println("Removing Child Node == " + tagName);
							node.removeChild(childNode);
						}
					}
				}
			}
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			transformer.transform(source, result);
			strResult = writer.toString();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return strResult;
	}

	/**
	 * @purpose This method will delete the part ID associate to the RelationCode = 239
	 * @param doc
	 * @throws XPathExpressionException
	 */
	protected static void deletePartyOfRelactionCode(Document doc) throws XPathExpressionException {
		String deleteRelationCode239 = "NBContact - Case Managed By";
		String party = searchReference(deleteRelationCode239, doc);
		Node oLife = doc.getElementsByTagName("OLifE").item(0);
		NodeList OlifeList = oLife.getChildNodes();
		for (int i = 0; i < OlifeList.getLength(); i++) {
			Node node = OlifeList.item(i);
			if (node.getNodeName().equals("Party") && node.getAttributes().getNamedItem("id").getNodeValue().equalsIgnoreCase(party)) {
				oLife.removeChild(node);
			}
		}
	}

	/**
	 * @purpose This method will ne used to search the reference of the text into the Document
	 * @param textToFind
	 * @param doc
	 * @return
	 * @throws XPathExpressionException
	 */
	private static String searchReference(String textToFind, Document doc) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		// change ELEMENTS
		String partyNode = null;

		String xPathExpression = "//*[text()='" + textToFind + "']";
		NodeList nodes = (NodeList) xpath.evaluate(xPathExpression, doc, XPathConstants.NODESET);

		for (int idx = 0; idx < nodes.getLength(); idx++) {
			String party = nodes.item(idx).getParentNode().getAttributes().getNamedItem("RelatedObjectID").getNodeValue();
			System.out.println("nodes == " + party);
			partyNode = party;
		}
		return partyNode;
	}

	/**
	 * @purpose This method will convert read the String object and parse it into the Document Type
	 * @param xmlStr
	 * @return
	 */
	private static Document convertStringToDocument(String xmlStr) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
