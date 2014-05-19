package com.shelterinsurance.sert;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Node;

@Controller
public class HomeController{
	
	//The UserID is used to uniquely identify the current user.
	private String userID = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789000";
	
	//Each user has an associated userName, used for personalization.
	private String userName = "John Doe";

	//This is the list of host names.
	private String hostNames = "";
	
	//This is the list of host IP addresses.
	private String hostIPs = "";

	//This is the number of host names;
	private int numHostNames = 0;
	
	//This is the file stored with the server that holds system environment data for a user.
	private File dataFile = new File(userID+"_data_input.in");
	
	//This is the file stored with the server that holds system environment data for a user.
	private File hostNameFile = new File("host_name_list.in");

	//This is the full hierarchical data structure.
	private ArrayList<DataElement> data = new ArrayList<DataElement>();
	
	//This data structure houses references to on the elements currently visible, allowing for easy access when recieving click events.
	private ArrayList<ArrayList<DataElement>> visibleElements = new ArrayList<ArrayList<DataElement>>();
	
	//This is the custom XML parsing class, for parsing the system data document.
	private XParser xp = new XParser();
	
	//This class should contain methods used to filter sensitive data from getting to the wrong user.
	private Security security = new Security();

	//This method is called whenever http://localhost:8081/sert/ is accessed. It should be used to perform a secure login. It also initially loads a user's specific data file.
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String login(Locale locale, Model model){
		
		/**
		 * TODO: Shelter Insurance -
		 * Implement authentication, such that the result is a 64 character userID,
		 * which can be referenced throughout the rest of the program to securely
		 * and uniquely identify a particular user.
		 */
		
		//--Sample high-level authentication (Security class can be implemented by Shelter Insurance)--//
		//
		//userID = security.getUserID();
		//

		//Declare the data file, using a user's id to identify their unique data file.
		dataFile = new File(userID+"_data_input.in");
		
		//Parse the data in the data file, creating all the DataElements.
		parseDataFile();

		//Parse the data from the host names file.
		parseHostNames();
		
		return "login";
	}
	
	//This method is called whenever http://localhost:8081/sert/display is accessed. It displays the data trees, and saves changes made to data.
	@RequestMapping(value = "/display", method = RequestMethod.GET)
	public String display(Locale locale, Model model){
		
		//Make the number of data trees accessible to the JavaScript code.
		model.addAttribute("numTrees",data.size());

		//Make the number of data trees accessible to the JavaScript code.
		model.addAttribute("numHostNames",numHostNames);
		
		//Reset the visible elements.
		visibleElements = new ArrayList<ArrayList<DataElement>>();
		
		//Create the data strings used to define the visible data trees.
		String dataString = "";
		for(int i=0;i<data.size();i++){
			//Root nodes are always added to the Visible Nodes.
			visibleElements.add(new ArrayList<DataElement>());
			//For each root node, recursively detect and cache which sub nodes are visible.
			cacheVisibleElements(i,data.get(i),0,visibleElements);
			//If a tree is being searched, a prefix will be added that contains search results.
			if(!data.get(i).getType().equals("")){
				dataString+="<p>======== Search Results ========</p>";
				//Recursively search each data element, and find any that have a match to the search phrase (which is a root node's "type" variable).
				for(int j=0;j<data.get(i).getSize();j++){
					dataString+=doSearchTree(data.get(i).getType(),data.get(i).getElement(j));
				}
				dataString+="<p>==========================\n</p>";
			}
			//Recursively create XML elements representing data elements in a hierarchical structure.
			dataString+=createXMLFromElements(i,visibleElements);
			//Add an identifyer between each tree, so the JavaScript can explode the full data into individual trees.
			if(i<data.size()-1){
				dataString+="!!!\n";
			}
		}
		//Make the full data available for use in the JavaScript code.
		model.addAttribute("full_data",dataString);
		
		//Make the host names available for use in the JavaScripted code.
		model.addAttribute("host_names",hostNames);
		
		//Make the host names available for use in the JavaScripted code.
		model.addAttribute("host_ips",hostIPs);
		
		//If the user is an Admin
		if(security.isAdmin()){
			model.addAttribute("admin_link","<a id=\"titlebar_adminlink\" href=\"admin\"><img src=\"resources/TitleBar_AdminLink.png\" alt=\"\"></a>");
		}
		
		//Save the data to the Data File.
		saveDataFile();
		
		return "display";
	}
	
	//This method is called whenever http://localhost:8081/sert/display is accessed. It displays the data trees, and saves changes made to data.
	@RequestMapping(value = "/admin", method = RequestMethod.GET)
	public String admin(Locale locale, Model model){

		//Make the number of data trees accessible to the JavaScript code.
		model.addAttribute("numHostNames",numHostNames);
		
		//Make the host names available for use in the JavaScripted code.
		model.addAttribute("host_names",hostNames);
		
		//Make the host names available for use in the JavaScripted code.
		model.addAttribute("host_ips",hostIPs);
		
		return "admin";
	}
	
	//The method that is called when a collapsible tree element is clicked. It is responsible for toggling an element's "openness" state.
	@RequestMapping("/clickElement")
	@ResponseBody
	public String clickElement(@ModelAttribute(value = "treeNum") String streeNum,@ModelAttribute(value = "elementNum") String selementNum,BindingResult result,Model model){
		System.out.println("Fired: /clickElement");
		
		//Convert input from text to integer.
		int treeNum = Integer.parseInt(streeNum);
		int elementNum = Integer.parseInt(selementNum);
		
		String returnText;
		if(!result.hasErrors()){
			//Get the clicked element, and toggle its openness.
			visibleElements.get(treeNum).get(elementNum).toggleOpenness();
			returnText = "Element Openness Changed";
		}else{
			returnText = "An Error Has Occurred";
		}
		return returnText;
	}
	
	//The method that is called when a tree's refresh button is pressed, or when a tree is being added. It is responsible for initiating a recursive data refresh.
	@RequestMapping("/refreshTree")
	@ResponseBody
	public String refreshTree(@ModelAttribute(value = "treeNum") String streeNum,BindingResult result,Model model){
		System.out.println("Fired: /refreshTree");
		
		//Convert input from text to integer.
		int treeNum = Integer.parseInt(streeNum);
		
		String returnText;
		if(!result.hasErrors()){
			//Initiate recursive data refresh.
			doRefreshTree(data.get(treeNum),visibleElements.get(treeNum).get(0).getName());
			returnText = "Tree Refreshed";
		}else{
			returnText = "An Error Has Occurred";
		}
		return returnText;
	}
	
	//The method that is called when a tree's delete button is pressed. It is responsible for removing a complete tree from the full data hierarchy.
	@RequestMapping("/deleteTree")
	@ResponseBody
	public String deleteTree(@ModelAttribute(value = "treeNum") String streeNum,BindingResult result,Model model){
		System.out.println("Fired: /deleteTree");
		
		//Convert input from text to integer.
		int treeNum = Integer.parseInt(streeNum);
		
		String returnText;
		if(!result.hasErrors()){
			//Remove the tree's reference from the full data hierarchy, allowing garbage collection to dispose of it.
			data.remove(visibleElements.get(treeNum).get(0));
			returnText = "Tree Removed";
		}else{
			returnText = "An Error Has Occurred";
		}
		return returnText;
	}
	
	//The method that is called when the "add new tree" button is pressed. It is responsible for creating a new tree, and initiating a refresh of that tree.
	@RequestMapping("/addTree")
	@ResponseBody
	public String addTree(@ModelAttribute(value = "ipaddress") String ipaddress,BindingResult result,Model model){
		System.out.println("Fired: /addTree");
		
		String returnText;
		if(!result.hasErrors()){
			//Create a new root element.
			DataElement d = new DataElement(ipaddress,"!RootNode!");
			//Refresh the element, giving it data from a system.
			if(doRefreshTree(d,ipaddress)){
				//If the data was successfully loaded, add the tree to the hierarchy, otherwise ignore this command.
				data.add(d);
			}
			returnText = "Tree Added";
		}else{
			returnText = "An Error Has Occurred";
		}
		return returnText;
	}
	
	//The method that is called when a tree's seach button is pressed. It is responsible for updating a tree's search value to the desired phrase.
	@RequestMapping("/searchTree")
	@ResponseBody
	public String searchTree(@ModelAttribute(value = "phrase") String phrase,@ModelAttribute(value = "treeNum") String streeNum,BindingResult result,Model model){
		System.out.println("Fired: /searchTree");

		//Convert input from text to integer.
		int treeNum = Integer.parseInt(streeNum);
		
		String returnText;
		if(!result.hasErrors()){
			//Update a tree's search value, which is the root node's "type" value.
			data.get(treeNum).setType(phrase);
			returnText = "Tree Searched";
		}else{
			returnText = "An Error Has Occurred";
		}
		return returnText;
	}
	
	//The method that is called when a tree's delete button is pressed. It is responsible for removing a complete tree from the full data hierarchy.
	@RequestMapping("/deleteHostName")
	@ResponseBody
	public String deleteHostName(@ModelAttribute(value = "hostNum") String shostNum,BindingResult result,Model model){
		String returnText = "Not Admin!";
		if(security.isAdmin()){
			System.out.println("Fired: /deleteHostName");
			
			//Convert input from text to integer.
			int hostNum = Integer.parseInt(shostNum);
			
			if(!result.hasErrors()){
				//Remove the host name and host ip.
				String[] hn = hostNames.split("!!!");
				String[] hi = hostIPs.split("!!!");
				ArrayList<String> hna = new ArrayList<String>();
				ArrayList<String> hia = new ArrayList<String>();
				for(int i=0;i<hn.length;i++){
					hna.add(hn[i]);
					hia.add(hi[i]);
				}
				hna.remove(hostNum);
				hia.remove(hostNum);
				hostNames = "";
				hostIPs = "";
				for(int i=0;i<hna.size();i++){
					if(i>0){
						hostNames+="!!!";
						hostIPs+="!!!";
					}
					hostNames+=hna.get(i);
					hostIPs+=hia.get(i);
				}
				//Update the number of host names.
				numHostNames = hna.size();
				
				//Save changes.
				saveHostNames();
				
				returnText = "Host Name Removed";
			}else{
				returnText = "An Error Has Occurred";
			}
		}
		return returnText;
	}
	
	//The method that is called when the "add new tree" button is pressed. It is responsible for creating a new tree, and initiating a refresh of that tree.
	@RequestMapping("/addHostName")
	@ResponseBody
	public String addHostName(@ModelAttribute(value = "hostName") String hostName,@ModelAttribute(value = "hostIP") String hostIP,BindingResult result,Model model){
		String returnText = "Not Admin!";
		
		if(security.isAdmin()){
			System.out.println("Fired: /addHostName");
			
			if(!result.hasErrors()){
				//Add new host name and ip.
				if(hostNames.length()>0){
					hostNames+="!!!";
				}
				hostNames+=hostName;
				if(hostIPs.length()>0){
					hostIPs+="!!!";
				}
				hostIPs+=hostIP;
				//Update the number of host names.
				numHostNames = hostNames.split("!!!").length;
				//Save changes.
				saveHostNames();
				returnText = "Host Name Added";
			}else{
				returnText = "An Error Has Occurred";
			}
		}
		return returnText;
	}
	
	//This method is responsible for grabbing a system's data, then initiating the parsing of the system data, and updating a tree's elements to the results of the parse.
	private boolean doRefreshTree(DataElement e,String ipaddress){
		//Grab a remote system data XML file.
		/**Requires Authentication! TODO for Shelter Insurance!**/
		//File fXmlFile = new File("\\\\"+ipaddress+"\\C$\\data\\systemdata.xml");
		File fXmlFile = new File("C:\\data\\systemdata.xml");
		//Make sure the file exists and is reachable. If not, return from the method and do nothing.
		if(!fXmlFile.exists()){
			System.out.println("Could not find file at: "+fXmlFile.getPath());
			return false;
		}
		//Assuming the file was found, clear the target tree's elements, readying them to be replaced by the file's contents.
		e.clearElements();
		//Update the tree's latest refresh timestamp.
		e.setTimestamp(System.currentTimeMillis());
		try{
			//Scan the file and grab all its contents.
			Scanner scan = new Scanner(fXmlFile);
			scan.useDelimiter("\\Z");
			//Parse the XML file, and use the returned ArrayList<DataElement> as the new children of our root element.
			e.addAllElements(xp.parseDocument(scan.next()).getElements());
			scan.close();
		}catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	
	//This method will perform a recursive search through d, and all it's sub-elements, for any properties that contain the search phrase.
	//It also returns an XML version of all results as paragraph elements representing the element's data.
	private String doSearchTree(String phrase,DataElement d){
		String s = "";
		//Check for any properties containing the search phrase.
		if(d.getName().toLowerCase().contains(phrase.toLowerCase())||d.getValue().toLowerCase().contains(phrase.toLowerCase())||d.getID().toLowerCase().contains(phrase.toLowerCase())||d.getType().toLowerCase().contains(phrase.toLowerCase())){
			//Apply the start tag.
			s+="<p>";
			//Show all properties of a particular element.
			s+=d.getName();
			if(!d.getID().equals("")){
				s+=" ("+d.getID()+")";
			}
			if(!d.getType().equals("")){
				s+=" ["+d.getType()+"]";
			}
			s+=": "+d.getValue();
			//Apply the end tag.
			s+="</p>\n";
		}
		//Recurse through all the element's children.
		for(int i=0;i<d.getSize();i++){
			s+=doSearchTree(phrase,d.getElement(i));
		}
		return s;
	}
	
	//This method is responsible for loading the saved data file, or creating it if none exists. It initiates a recursive load, after loading the initial user data.
	private void parseDataFile(){
		Scanner scan = null;
		try{
			//See if the data file exists or not.
			if(!dataFile.exists()){
				//If the data file does not exist, create it.
				dataFile.createNewFile();
				//Pre-load the data file with a default tree.
				PrintWriter p = new PrintWriter(dataFile);
				p.println("John Doe");
				p.println("1");
				p.println("localhost");
				p.println("!RootNode!");
				p.println("");
				p.println("");
				p.println(System.currentTimeMillis());
				p.println("0");
				p.close();
			}
			//Read the data from the data file.
			scan = new Scanner(dataFile);
			//Read the user's name, always at the top of the data file.
			userName = scan.nextLine();
			//Get the number of trees that the data file contains currently.
			int numTrees = Integer.parseInt(scan.nextLine());
			//Re-initialize the data hierarchy, readying it for new data.
			data = new ArrayList<DataElement>();
			//For each tree, recursively parse each element.
			for(int j=0;j<numTrees;j++){
				data.add(parseElementData(scan));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		scan.close();
	}
	
	//This method is responsible for loading the host names file, or creating it if none exists.
	private void parseHostNames(){
		Scanner scan = null;
		try{
			//See if the host names file exists or not.
			if(!hostNameFile.exists()){
				//If the host names file does not exist, create it.
				hostNameFile.createNewFile();
				//Add default data to the host names list.
				PrintWriter p = new PrintWriter(hostNameFile);
				p.println("0");
				p.close();
			}
			//Read the host names from the host names file.
			scan = new Scanner(hostNameFile);
			//Get the number of host names
			numHostNames = Integer.parseInt(scan.nextLine());
			//Reset the host names.
			hostNames = "";
			hostIPs = "";
			//Add each host name to the list.
			for(int j=0;j<numHostNames;j++){
				if(j>0){
					hostNames+="!!!";
					hostIPs+="!!!";
				}
				String[] values = scan.nextLine().split("---");
				hostNames+=values[0];
				hostIPs+=values[1];
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		scan.close();
	}
	
	//This method recursively parses each element, as it is formated in the data file.
	//The element is then returned as a child to the previous recursive call, creating the hierarchical structure.
	private DataElement parseElementData(Scanner scan){
		//Read in a particular element's data.
		DataElement e = new DataElement(scan.nextLine(),scan.nextLine(),scan.nextLine(),scan.nextLine());
		//If the newly read element is a root element, read in its timestamp.
		if(e.getValue().equals("!RootNode!")){
			e.setTimestamp(Long.parseLong(scan.nextLine()));
		}
		//Read how many children the element should have.
		int numElements = Integer.parseInt(scan.nextLine());
		//For that number of elements, read in the children recursively.
		for(int i=0;i<numElements;i++){
			e.addElement(parseElementData(scan));
		}
		return e;
	}
	
	//This method will save the data to the data file. This happens by initiating a recursive call to save each root element and its children.
	public void saveDataFile(){
		PrintWriter p = null;
		try{
			//Open an output stream to the data file.
			p = new PrintWriter(dataFile);
			//Print out the user data.
			p.println(userName);
			//Print the number of trees int the hierarchy.
			p.println(data.size());
			//For each tree, recursively save all of its child elements.
			for(int i=0;i<data.size();i++){
				saveElementData(data.get(i),p);
			}
			p.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		p.close();
	}
	
	//This method will save the host names to a file.
	public void saveHostNames(){
		PrintWriter p = null;
		try{
			//Open an output stream to the host names file.
			p = new PrintWriter(hostNameFile);
			//Print out the number of host names.
			p.println(numHostNames);
			//Save each host name.
			String[] hn = hostNames.split("!!!");
			String[] hi = hostIPs.split("!!!");
			for(int i=0;i<hn.length;i++){
				p.println(hn[i]+"---"+hi[i]);
			}
			p.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		p.close();
	}
	
	//This method is responsible for recursively saving each element and its child elements to the data file.
	public void saveElementData(DataElement d,PrintWriter p){
		//Print each property of the given element to the data file.
		p.println(d.getName());
		p.println(d.getValue());
		p.println(d.getID());
		p.println(d.getType());
		//If the element is a root element, save its timestamp.
		if(d.getValue().equals("!RootNode!")){
			p.println(d.getTimestamp());
		}
		//Save the number of children this element should have.
		p.println(d.getSize());
		//For each child, recursively save each one.
		for(int i=0;i<d.getSize();i++){
			saveElementData(d.getElement(i),p);
		}
	}
	
	//This method recursively detects which elements in the data hierarchy are currently open and visible, and adds them to the visible elements.
	public void cacheVisibleElements(int tree,DataElement e,int level,ArrayList<ArrayList<DataElement>> vElements){
		//Set the element's hierarchical depth.
		e.setLevel(level);
		//Add the element to the visible elements
		vElements.get(tree).add(e);
		//If the element is open and a group, recursively check the visibility of its children.
		if(e.isOpen() && e.isGroup()){
			for(int i=0;i<e.getSize();i++){
				cacheVisibleElements(tree,e.getElement(i),level+1,vElements);
			}
		}
	}
	
	//This method is responsible for taking a list of visible elements, and creating a series of XML paragraphs that will display the tree in the data display.
	public String createXMLFromElements(int n,ArrayList<ArrayList<DataElement>> vElements){
		String s = "";
		//For each visible element in the desired tree...
		for(int i=0;i<vElements.get(n).size();i++){
			DataElement e = vElements.get(n).get(i);
			if(e.getValue().equals("!RootNode!")){
				//If the element is a root element, format it to only show its name, and openness.
				s+="<p style=\"font-weight:bold; padding-left: "+(e.getLevel()*30)+"px;\" id=\""+i+"\" onClick=\"elementClicked(this)\">";
				if(e.isOpen()){
					s+="- ";
				}else{
					s+="+ ";
				}
				s+=e.getName();
			}else{
				if(e.isGroup()){
					//If the element is a group, format it to show its name, and openness, id, and type.
					s+="<p style=\"font-weight:bold; padding-left: "+(e.getLevel()*30)+"px;\" id=\""+i+"\" onClick=\"elementClicked(this)\">";
					if(e.isOpen()){
						s+="- ";
					}else{
						s+="+ ";
					}
					s+=e.getName();
					if(!e.getID().equals("")){
						s+=" ("+e.getID()+")";
					}
					if(!e.getType().equals("")){
						s+=" ["+e.getType()+"]";
					}
				}else{
					//If the element is a data element, format it to show all its properties, except for openness.
					s+="<p style=\"padding-left: "+(e.getLevel()*30)+"px;\" value=\""+i+"\">-- ";
					s+=e.getName();
					if(!e.getID().equals("")){
						s+=" ("+e.getID()+")";
					}
					if(!e.getType().equals("")){
						s+=" ["+e.getType()+"]";
					}
					s+=": "+e.getValue();
				}
			}
			s+="</p>\n";
		}
		return s;
	}
}
