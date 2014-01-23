package SBATCHscripts;

import general.ExtendedReader;

import java.util.ArrayList;
import java.util.Hashtable;




public class FASTQCmodule {
	public String Name;
	public String Value;
	public String[] ColNames;
	public Hashtable<String,String[]> StringInfo;
	
	
	FASTQCmodule(String Name,String Value){
		this.Name = Name;
		this.Value = Value;
		StringInfo =null;
		
	}
	
	public void parseModule(ExtendedReader ER){
		
		String info = ER.readLine();

		info = ER.readLine();
		// Read the columnNames in the module 
		this.ColNames = ER.readLine().split("\t");
		
		
		info = ER.readLine();
		while(info.compareTo(">>END_MODULE") != 0){
			this.StringInfo.put(info.split("\t")[0],info.split("\t"));
			info = ER.readLine();
		 }			
	}
	
	
	public String[] getColNames(){
		return ColNames;
	}
	
	public String[] getValues(String rowName){
		if(this.StringInfo.containsKey(rowName))
			return this.StringInfo.get(rowName);
		return null;
	}
	
	

	
	
}



