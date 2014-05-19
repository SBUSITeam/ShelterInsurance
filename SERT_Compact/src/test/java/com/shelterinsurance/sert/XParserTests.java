package com.shelterinsurance.sert;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class XParserTests {

	//Tests a pair of sibling data elements, and asserts that their properties are being correctly parsed.
	@Test
	public void testParseSingleLayer(){
		XParser xp = new XParser();
		//Parse some XML data.
		DataElement root = xp.parseDocument("<Data><Tag id=\"id\" type=\"type\">innerHTML</Tag><Tag2 id=\"id2\" type=\"type2\">innerHTML2</Tag2></Data>");
		//Get the first Element, and assert accuracy of properties.
		DataElement leaf = root.getElement(0);
		assertEquals("Tag",leaf.getName());
		assertEquals("innerHTML",leaf.getValue());
		assertEquals("id",leaf.getID());
		assertEquals("type",leaf.getType());
		//Get the second Element, and assert accuracy of properties.
		leaf = root.getElement(1);
		assertEquals("Tag2",leaf.getName());
		assertEquals("innerHTML2",leaf.getValue());
		assertEquals("id2",leaf.getID());
		assertEquals("type2",leaf.getType());
	}
	
	//Tests a parent and child pair of data elements, and asserts that their properties are being correctly parsed.
	@Test
	public void testParseMultiLayer(){
		XParser xp = new XParser();
		//Parse some XML data.
		DataElement root = xp.parseDocument("<Data><Tag id=\"id\" type=\"type\"><Tag2 id=\"id2\" type=\"type2\">innerHTML2</Tag2></Tag></Data>");
		//Get the parent Element, and assert accuracy of properties.
		DataElement leaf = root.getElement(0);
		assertEquals("Tag",leaf.getName());
		assertEquals("!Group!",leaf.getValue());
		assertEquals("id",leaf.getID());
		assertEquals("type",leaf.getType());
		//Get the child Element, and assert accuracy of properties.
		leaf = root.getElement(0).getElement(0);
		assertEquals("Tag2",leaf.getName());
		assertEquals("innerHTML2",leaf.getValue());
		assertEquals("id2",leaf.getID());
		assertEquals("type2",leaf.getType());
	}
}
