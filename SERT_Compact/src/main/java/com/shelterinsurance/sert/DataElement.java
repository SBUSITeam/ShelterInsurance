package com.shelterinsurance.sert;

import java.util.ArrayList;

public class DataElement{
	
	private String name,value,id="",type="";
	private long timestamp = -1;
	private int level = 0;
	private ArrayList<DataElement> Elements = new ArrayList<DataElement>();
	private boolean isOpen = true;

	public DataElement(String Name,String Value){
		name=Name;
		value=Value;
	}
	
	public DataElement(String Name,String Value,String ID){
		name=Name;
		value=Value;
		id=ID;
	}
	
	public DataElement(String Name,String Value,String ID,String Type){
		name=Name;
		value=Value;
		id=ID;
		type=Type;
	}
	
	public void setTimestamp(long Timestamp){
		timestamp = Timestamp;
	}
	
	public String getName(){
		return name;
	}
	
	public String getValue(){
		return value;
	}
	
	public String getID(){
		return id;
	}
	
	public String getType(){
		return type;
	}
	
	public long getTimestamp(){
		return timestamp;
	}
	
	public int getLevel(){
		return level;
	}
	
	public void setName(String Name){
		name = Name;
	}
	
	public void setValue(String Value){
		value = Value;
	}
	
	public void setLevel(int Level){
		level = Level;
	}
	
	public void setType(String Type){
		type = Type;
	}
	
	public void addElement(DataElement e){
		Elements.add(e);
	}
	
	public void addAllElements(ArrayList<DataElement> es){
		Elements.addAll(es);
	}
	
	public void removeElement(DataElement e){
		Elements.remove(e);
	}
	
	public void removeElement(int i){
		Elements.remove(i);
	}

	public DataElement getElement(int i){
		return Elements.get(i);
	}
	
	public ArrayList<DataElement> getElements(){
		return Elements;
	}
	
	public void clearElements(){
		Elements.clear();
	}
	
	public int getSize(){
		return Elements.size();
	}
	
	public boolean isGroup(){
		return !Elements.isEmpty();
	}
	
	public void toggleOpenness(){
		isOpen=!isOpen;
	}
	
	public boolean isOpen(){
		return isOpen;
	}
	
	public String toString(){
		return toString("");
	}
	public String toString(String prefix){
		String s = prefix+name+": "+value;
		for(int i=0;i<Elements.size();i++){
			s+="\n"+Elements.get(i).toString(prefix+"-");
		}
		return s;
	}
}
