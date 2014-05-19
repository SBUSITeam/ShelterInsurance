package com.shelterinsurance.sert;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

//The XParser is a class that was created to easily parse XML documents into a DataElement tree.
public class XParser{

	public XParser(){}
	
	//This method is responsible for creating the XML document objects and initiating the recursive method to parse each element.
	public DataElement parseDocument(String doc){
		//Get the source from the input data.
		InputSource source = new InputSource(new StringReader(doc));
		try{
			//Initialize the Document from the source.
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(source);
			//Create the base root node, into which is added all data as children elements.
			DataElement d = new DataElement("FullData","!RootNode!");
			//Grab the primary document XML Element.
			Element e = document.getDocumentElement();
			//Get the XML Element's children.
			NodeList nl = e.getChildNodes();
			//Recursively parse each child.
			for(int i=0;i<nl.getLength();i++){
				DataElement d2 = parseXMLElement(nl.item(i));
				//Only add a child if the parsed element has data.
				if(!d2.getName().equals("!Blank!")){
					d.addElement(d2);
				}
			}
			
			return parseXMLElement(e);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	//This method recursively parses XML Elements, and all of their children, returning them as children to the previous recusive step.
	private DataElement parseXMLElement(Node n){
		//Initialize a blank DataElement
		DataElement d = new DataElement("!Blank!","!Blank!");
		//Check if the XML Node is an Element Node.
		if(n.getNodeType()==Node.ELEMENT_NODE){
			//If the XML Node is an Element Node, reference it as one.
			Element e = (Element)n;
			//Get the data for the new DataElement.
			d = new DataElement(e.getTagName(),e.getTextContent(),e.getAttribute("id"),e.getAttribute("type"));
			//Get all the current XML Element's children nodes.
			NodeList nl = n.getChildNodes();
			//A check, used to deturmine if an element is a group.
			boolean check = false;
			//Recursively parse each child.
			for(int i=0;i<nl.getLength();i++){
				DataElement d2 = parseXMLElement(nl.item(i));
				//Only add a child if the parsed element has data.
				if(!d2.getName().equals("!Blank!")){
					d.addElement(d2);
					check = true;
				}
			}
			//If the Element is a group, update it's value to reflect tha.
			if(check){
				d.setValue("!Group!");
			}
		}
		return d;
	}
}
