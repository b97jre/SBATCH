package SBATCHscripts;

import general.ExtendedReader;
import general.ExtendedWriter;
import general.Functions;
import general.IOTools;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;


public class RFAM {

	String cmsearch;
	double Evalue;
	String rfamDB;
	String refName;
	
	
	ArrayList<String> tmpFiles;
	String finalFinalName;
	
	

	public RFAM(Hashtable<String, String> T) {
		
		cmsearch = Functions.getValue(T, "-cmsearch","/glob/johanr/bin/cmsearch");
		rfamDB = Functions.getValue(T, "-pfamDB","/pica/v3/b2011098_nobackup/private/deNovoAnnotation/references/RFAM/Rfam.cm");
		refName = new File("pfamDB").getName();
		this.tmpFiles = new ArrayList<String>(); 
	}
	
	public static void help(){
		System.out.println("");
		System.out.println("RFAM specific flags");
		System.out.println(Functions.fixedLength("-cmsearch <pathTocmsearch>",30)+"/glob/johanr/bin/cmsearch is default");
		System.out.println(Functions.fixedLength("-pfamDB <pathTopfamDB>",30)+"/glob/johanr/references/Pfam/PfamAB.hmm is default");
		System.out.println("");
		
	}

	
	
	
	public  String runRfamFile(ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
			String inDir, String outDir, String fileName){
		try {
			String inFileBase = IOTools.getFileBase(fileName, null);

			String sbatchFileName = outDir + "/scripts/" + sbatch.getTimeStamp() + "_"
					+ inFileBase + "_RFAM.sbatch";
			generalSbatchScript.println("sbatch " + sbatchFileName);

			ExtendedWriter EW = new ExtendedWriter(new FileWriter(
					sbatchFileName));

			sbatch.printSBATCHinfo(EW, outDir, sbatch.getTimeStamp(), 0, inFileBase
					+ ".RFAM");


			EW.println("# going to correct directory");
			EW.println("cd " + inDir);
			EW.println(RFAMCommand(cmsearch, fileName, rfamDB));

			String outFileName = getOutFileName(fileName, rfamDB);
			EW.println("mv "+outFileName+".tmp "+outDir+"/"+outFileName);
			this.tmpFiles.add(outFileName);
			EW.flush();
			EW.close();
			
			return sbatchFileName;
			
		} catch (Exception E) {
			E.printStackTrace();
		}
		return null;
	}
	

	public static String RFAMCommand(String cmsearch, String inFile, String pfamDB) {
		System.out.println(pfamDB);
		String refName = new File(pfamDB).getName();
		String inBase = IOTools.getFileBase(new File(inFile).getName(),null);
   
		return cmsearch+" "+ 
				"--cut_ga "+  
				" --tblout " + inBase + "."+ refName + ".tbl.RFAM.tmp"+
				" "+pfamDB+" "+inFile;
	
	}
	
	public static String getOutFileName(String inFile, String pfamDB) {
		String refName = new File(pfamDB).getName();
		String inBase = IOTools.getFileBase(new File(inFile).getName(),null);
		return inBase + "."+ refName + ".tbl.RFAM";
	}

}
