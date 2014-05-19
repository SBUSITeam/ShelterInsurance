package com.shelterinsurance.sert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Scanner;

import org.junit.Test;

public class HomeControllerTests{
	
	//This test tests the controller's ability to search through some elements and compile the results into XML.
	@Test
	public void testDoSearchTree(){
		//Set up a tree, and several searchable children.
		DataElement e = new DataElement("Full Data","!RootNode!","someID","searchableValue");
		e.addElement(new DataElement("someSearchableValue","something","somethingElse","somethingElse"));
		e.addElement(new DataElement("something","someOtherSEARCHABLEValue","somethingElse","somethingElse"));
		e.addElement(new DataElement("something","somethingNOTactuallysearchable","somethingElse","somethingElse"));
		e.addElement(new DataElement("something","somethingElse","someOtherSEARCHABLEVALUE","somethingElse"));
		e.addElement(new DataElement("something","somethingElse","somethingElse","someOTHERSEARCHABLEVALUE"));
		try{
			//Set up a private method to be fired.
			HomeController h = new HomeController();
			Class[] cparams = {String.class,DataElement.class};
			Method doSearchTree = HomeController.class.getDeclaredMethod("doSearchTree", cparams);
			doSearchTree.setAccessible(true);
			//Fire doSearchTree directly.
			String returnXML = "";
			for(int i=0;i<e.getSize();i++){
				Object[] oparams = {e.getType(),e.getElement(i)};
				returnXML += (String) doSearchTree.invoke(h,oparams);
			}
			//Verify correctness of the search.
			assertEquals(returnXML,"<p>someSearchableValue (somethingElse) [somethingElse]: something</p>\n<p>something (somethingElse) [somethingElse]: someOtherSEARCHABLEValue</p>\n<p>something (someOtherSEARCHABLEVALUE) [somethingElse]: somethingElse</p>\n<p>something (somethingElse) [someOTHERSEARCHABLEVALUE]: somethingElse</p>\n");
		}catch(Exception ex){
			ex.printStackTrace();
			assertFalse(true);
		}
	}
	
	//This test performs a parse of element data from a data file string, and verifies the correct creation of data elements.
	@Test
	public void testParseElementData(){
		//Create a mock data file.
		Scanner scan = new Scanner("Data 3\n!Group!\n\n\n2\nData 3_1\nHello again\nrandomID\n\n0\nData 3_2\n100000\n\nrandomType\n0");
		try{
			//Set up a private method to be fired.
			HomeController h = new HomeController();
			Class[] cparams = {Scanner.class};
			Method parseElementData = HomeController.class.getDeclaredMethod("parseElementData", cparams);
			parseElementData.setAccessible(true);
			//Fire parseElementData directly.
			Object[] oparams = {scan};
			DataElement e = (DataElement) parseElementData.invoke(h,oparams);
			//Verify the parsed parent's properties.
			assertEquals(e.getName(),"Data 3");
			assertEquals(e.getValue(),"!Group!");
			assertEquals(e.getID(),"");
			assertEquals(e.getType(),"");
			//Verify the parsed properties of the first child.
			assertEquals(e.getElement(0).getName(),"Data 3_1");
			assertEquals(e.getElement(0).getValue(),"Hello again");
			assertEquals(e.getElement(0).getID(),"randomID");
			assertEquals(e.getElement(0).getType(),"");
			//Verify the parsed properties of the second child.
			assertEquals(e.getElement(1).getName(),"Data 3_2");
			assertEquals(e.getElement(1).getValue(),"100000");
			assertEquals(e.getElement(1).getID(),"");
			assertEquals(e.getElement(1).getType(),"randomType");
		}catch(Exception ex){
			ex.printStackTrace();
			assertFalse(true);
		}
	}
	
	//This test makes sure that data is being saved in the proper format to the data files.
	@Test
	public void testSaveElementData(){
		DataElement e = new DataElement("Data 3","!Group!","A","B");
		e.addElement(new DataElement("Data 3_1","Hello again","randomID","B"));
		e.addElement(new DataElement("Data 3_2","100000","A","randomType"));
		try{
			File temp = new File("tempTestFile.tst");
			temp.createNewFile();
			PrintWriter p = new PrintWriter(temp);
			//Set up a private method to be fired.
			HomeController h = new HomeController();
			Class[] cparams = {DataElement.class,PrintWriter.class};
			Method saveElementData = HomeController.class.getDeclaredMethod("saveElementData", cparams);
			saveElementData.setAccessible(true);
			//Fire saveElementData directly.
			Object[] oparams = {e,p};
			saveElementData.invoke(h,oparams);
			//Close PrintWriter
			p.close();
			//Verify the output is correctly formatted.
			Scanner scan = new Scanner(temp);
			scan.useDelimiter("\\Z");
			assertEquals("Data 3\n!Group!\nA\nB\n2\nData 3_1\nHello again\nrandomID\nB\n0\nData 3_2\n100000\nA\nrandomType\n0\n",scan.next());
			scan.close();
			//Remove the temporary file.
			temp.delete();
		}catch(Exception ex){
			ex.printStackTrace();
			assertFalse(true);
		}
	}
	
	//This test makes sure that visible elements are cached correctly.
	@Test
	public void testCacheVisibleElements(){
		DataElement e = new DataElement("Data 3","!Group!","A","B");
		e.addElement(new DataElement("Data 3_1","!Group!","randomID","B"));
		e.getElement(0).addElement(new DataElement("Data 3_2","100000","A","randomType"));
		e.toggleOpenness();
		ArrayList<ArrayList<DataElement>> vElements = new ArrayList<ArrayList<DataElement>>();
		vElements.add(new ArrayList<DataElement>());
		vElements.get(0).add(e);
		try{
			//Set up a private method to be fired.
			HomeController h = new HomeController();
			Class[] cparams = {int.class,DataElement.class,int.class,ArrayList.class};
			Method cacheVisibleElements = HomeController.class.getDeclaredMethod("cacheVisibleElements", cparams);
			cacheVisibleElements.setAccessible(true);
			//Fire cacheVisibleElements directly.
			Object[] oparams = {0,e,0,vElements};
			cacheVisibleElements.invoke(h,oparams);
			//Verify the output is correct.
			assertEquals(2,vElements.get(0).size());
		}catch(Exception ex){
			ex.printStackTrace();
			assertFalse(true);
		}
	}
	
	//This test makes sure that visible elements are correctly turned into XML.
	@Test
	public void testCreateXMLFromElements(){
		DataElement e = new DataElement("Data 3","!Group!","A","B");
		e.addElement(new DataElement("Data 3_1","!Group!","randomID","B"));
		e.addElement(new DataElement("Data 3_2","100000","A","randomType"));
		ArrayList<ArrayList<DataElement>> vElements = new ArrayList<ArrayList<DataElement>>();
		vElements.add(new ArrayList<DataElement>());
		vElements.get(0).add(e);
		vElements.get(0).add(e.getElement(0));
		vElements.get(0).add(e.getElement(1));
		try{
			//Set up a private method to be fired.
			HomeController h = new HomeController();
			Class[] cparams = {int.class,ArrayList.class};
			Method createXMLFromElements = HomeController.class.getDeclaredMethod("createXMLFromElements", cparams);
			createXMLFromElements.setAccessible(true);
			//Fire createXMLFromElements directly.
			Object[] oparams = {0,vElements};
			String results = (String)createXMLFromElements.invoke(h,oparams);
			//Verify the output is correct.
			assertEquals("<p style=\"font-weight:bold; padding-left: 0px;\" id=\"0\" onClick=\"elementClicked(this)\">- Data 3 (A) [B]</p>\n<p style=\"padding-left: 0px;\" value=\"1\">-- Data 3_1 (randomID) [B]: !Group!</p>\n<p style=\"padding-left: 0px;\" value=\"2\">-- Data 3_2 (A) [randomType]: 100000</p>\n",results);
		}catch(Exception ex){
			ex.printStackTrace();
			assertFalse(true);
		}
	}
}
