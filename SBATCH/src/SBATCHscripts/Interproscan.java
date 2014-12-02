package SBATCHscripts;

import general.ExtendedReader;
import general.ExtendedWriter;
import general.Functions;
import general.IOTools;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import Sequence.FastaSequences;

public class Interproscan {

	String InterproscanLocation;
	
	
	ArrayList<String> tmpFiles;
	String finalFinalName;
	
	
	public Interproscan(boolean protein, String InterproscanLocation){
	this.InterproscanLocation =InterproscanLocation;
	}

	public Interproscan() {
		
	}
	public Interproscan(Hashtable<String, String> T) {
		this.InterproscanLocation = Functions.getValue(T, "-interproscanJar","/home/johanr/glob/bin/interProScan/interproscan-5.3-46.0/interproscan-5.jar" );
		this.tmpFiles = new ArrayList<String>(); 
		
	}
	
	public static void help(){
		System.out.println("");
		System.out.println("Interproscan specific flags");
		System.out.println(Functions.fixedLength("-blastp or -blastn",30)+"blastn is default");
		System.out.println(Functions.fixedLength("-blastDB <pathToBlastDB>",30)+"nt is default for blastn");
		System.out.println(Functions.fixedLength(" ",30)+"nr is default for blastp");
		System.out.println("");
		
		
		
		
	}
	public  void run(Hashtable<String, String> T,SBATCHinfo sbatch){
		
		
		
		
		
	}
			

//	public  void run(Hashtable<String, String> T,
//			ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
//			String timestamp, String inDir, String outDir, String fileName,
//			String suffix) {
//
//
//		if (T.containsKey("-split")) {
//			int peptidedPerFile = Functions.getInt(T, "-split", 1000);
//			ArrayList<String> fileNames = FastaSequences.split(inDir, fileName,peptidedPerFile, suffix);
//			for (int i = 0; i < fileNames.size(); i++) {
//				
//				runBlastFile(generalSbatchScript, sbatch, timestamp, inDir,
//						outDir, fileNames.get(i), suffix);
//			}
//
//			try {
//				ExtendedWriter EW = new ExtendedWriter(new FileWriter(outDir
//						+ "/scripts/" + timestamp + "_" + fileName
//						+ "_blast_removeTemp.sh"));
//				EW.println("#This file will remove all the temporary files generated to run blast in parallel");
//				for (int i = 0; i < fileNames.size(); i++) {
//					String inFileBase = IOTools.getFileBase(fileNames.get(i),
//							suffix);
//
//					EW.println("rm " + fileNames.get(i));
//					EW.println("rm " + inFileBase + ".blast");
//					EW.println();
//				}
//				EW.flush();
//				EW.close();
//			} catch (Exception E) {
//				E.printStackTrace();
//			}
//
//		} else {
//			runBlastFile(generalSbatchScript, sbatch, timestamp, inDir,
//					outDir, fileName, suffix);
//
//		}
//	}

	
	
	
	
	public  void runInterproScanFile(ExtendedWriter generalSbatchScript, SBATCHinfo sbatch, 
			String inDir, String outDir, String fileName,boolean AA) {
		try {
			
			if (!IOTools.isDir(outDir + "/scripts"))
				IOTools.mkDir(outDir + "/scripts");
			if (!IOTools.isDir(outDir + "/reports"))
				IOTools.mkDir(outDir + "/reports");
			
			String sbatchFileName = outDir + "/scripts/" + sbatch.getTimeStamp() + "_"
					+ fileName + "_InterproScan.sbatch";
			generalSbatchScript.println("sbatch " + sbatchFileName);
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(
					sbatchFileName));
			sbatch.printSBATCHinfo(EW, inDir, sbatch.getTimeStamp(), 0, fileName
					+ ".InterproScan");
			


			EW.println("# going to correct directory");
			
			EW.println("cd " + new File(this.InterproscanLocation).getParent());
			
			if(IOTools.fileExists(inDir,fileName+".xml"))
				EW.println("rm " +inDir+"/"+fileName+".xml" );
			if(IOTools.fileExists(inDir,fileName+".tsv"))
				EW.println("rm " +inDir+"/"+fileName+".tsv" );
			if(IOTools.fileExists(inDir,fileName+".gff3"))
				EW.println("rm " +inDir+"/"+fileName+".gff3" );
			
			if (AA)
				EW.println(Interproscan.InterproScanProteinCommand(fileName, InterproscanLocation, inDir,sbatch.getMemoryPadded()));
			else
				EW.println(Interproscan.InterproScanNucleotideCommand(fileName, InterproscanLocation, inDir,sbatch.getMemoryPadded()));
			
			EW.println("mv " +inDir+"/"+fileName+".xml "+outDir+"/"+fileName+".xml");
			EW.println("mv " +inDir+"/"+fileName+".tsv "+outDir+"/"+fileName+".tsv");
			EW.println("mv " +inDir+"/"+fileName+".gff3 "+outDir+"/"+fileName+".gff3");
			

			this.tmpFiles.add(fileName + ".xml");
			
			EW.flush();
			EW.close();

		} catch (Exception E) {
			E.printStackTrace();
		}
	}
	
	
	


	public static String InterproScanNucleotideCommand(String inFile, String InterproScanLocation,
			String inDir, int memoryGB) {


		return "sh interproscan.sh"+
				" -i "+ inFile+
				" -t n "+
				" -u "+ inDir;
		
	}

	public static String InterproScanProteinCommand(String inFile, String InterproScanLocation,
			String inDir, int memoryGB) {
		
		return "sh interproscan.sh"+
				" -i "+ inFile+
				" -t p "+
				" -u "+ inDir;
	}

	
	
		
	public String mergeXMLsbatchScript(SBATCHinfo sbatch, ArrayList <Integer> Numbers, String inDir,String outDir ,String outFile){
		
		ExtendedWriter MergeScript = sbatch.printSBATCHInfoSTART("interproscanMerge",outDir, Numbers);
		MergeScript.print("java -Xmx4G -jar "+sbatch.jarPath+" -interactive -program MERGE -type INTERPROSCAN"
		+" -inDir "+inDir
		+" -outDir "+ outDir
		+" -tmpFiles "+tmpFiles.get(0));
		for(int i = 1; i < tmpFiles.size();i++){
			MergeScript.print(","+tmpFiles.get(i));
		}
		if(outFile != null){
			MergeScript.print(" -outFile "+outFile);
		}
		
		MergeScript.println();
		MergeScript.flush();
		MergeScript.close();
		
		return sbatch.getSBATCHfileName("interproscanMerge",outDir);
	}
	
	
	public void merge(String inDir, String outDir, ArrayList<String> tmpFiles, String outFile) {
		if(outFile == null){
			outFile = IOTools.longestCommonPrefix(tmpFiles)+".xml";
		}
		
		System.out.println("merging Interproscan files in folder " + inDir + " to file "
				+ outDir+"/"+outFile);

		
		try {
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(outDir + "/"
					+ outFile));
			ExtendedReader ER = new ExtendedReader(new FileReader(inDir + "/"
					+ tmpFiles.get(0)));
			String Line = ER.readLine();
			while (ER.more() && Line.compareTo("</protein-matches>") != 0) {
				EW.println(Line);
				Line = ER.readLine();
			}
			ER.close();
			for (int i = 1; i < tmpFiles.size(); i++) {
				System.out.println(" now adding file " + tmpFiles.get(i));
				ER = new ExtendedReader(new FileReader(inDir + "/"
						+ tmpFiles.get(i)));
				
				Line = ER.readLine();
				while (ER.more() && Line.compareTo("<protein-matches xmlns=\"http://www.ebi.ac.uk/interpro/resources/schemas/interproscan5\">") != 0) {
					Line = ER.readLine();
				}
				Line = ER.readLine();
				while (ER.more() && Line.compareTo("</BlastOutput_iterations>") != 0) {
						EW.println(Line);
						Line = ER.readLine();
				}
				ER.close();
			}
			
			EW.println("</BlastOutput_iterations>");
			EW.println("</BlastOutput>");

			EW.flush();
			EW.close();
		} catch (Exception E) {
			E.printStackTrace();
		}
	}
	


}
