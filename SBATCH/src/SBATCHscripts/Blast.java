package SBATCHscripts;

import general.ExtendedReader;
import general.ExtendedWriter;
import general.Functions;
import general.IOTools;
import general.RNAfunctions;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;


public class Blast {

	double Evalue;
	String blastDB;
	String refName;
	String blastProgram;
	boolean protein;
	String outputFormat;
	int max_target_seqs;
	
	String extra;


	ArrayList<String> tmpFiles;
	String finalFinalName;


	public Blast(double Evalue,String blastDB, String blastProgram){
		this.Evalue = Evalue;
		this.blastDB = blastDB;
		this.blastProgram = blastProgram;
	}

	public Blast(){}

	public void addMergeInfo(Hashtable<String, String> T){
		this.outputFormat = Functions.getValue(T,"-outfmt", "6");
	}
	
	public Blast(Hashtable<String, String> T) {
		this.outputFormat = Functions.getValue(T,"-outfmt", "6");

		this.Evalue = Functions.getDouble(T, "-evalue", 0.001);
		this.max_target_seqs = Functions.getInt(T, "-max_target_seqs", 20);

		this.blastProgram = Functions.getValue(T, "-blastprogram", "blastn");

		if (blastProgram.compareTo("blastp") == 0) {
			System.out.println("running blastp");
			protein = true;
		}else if (blastProgram.compareTo("blastx") == 0){
			System.out.println("running blastx");
			protein = true;
		} else if (blastProgram.compareTo("blastn") == 0){
			System.out.println("running blastn");
			protein = false;
		} 
		else if (blastProgram.compareTo("tblastn") == 0){
			System.out.println("running tblastn");
			protein = false;
		} 
		if (T.containsKey("-blastDB")){
			blastDB = Functions.getValue(T, "-blastDB");
			
			System.out.println("blastDB is " + blastDB);
			blastDB = new File(blastDB).getAbsolutePath();
		}else {
			if (protein){
				blastDB = "/sw/data/uppnex/blast_databases/nr";
				System.out.println("Default blastDB for proteins are " + blastDB);
				System.out.println("To change add flag -blastDB <yourBlastDB>");
			}
			else{
				blastDB = "/sw/data/uppnex/blast_databases/nt";
				System.out.println("Default blastDB for nucleotides are " + blastDB);
				System.out.println("To change add flag -blastDB <yourBlastDB>");
			}
			
		}
		this.refName = blastDB;
		String[] temp = blastDB.split("/");
		if (temp.length > 1)
			refName = temp[temp.length - 1];


		this.tmpFiles = new ArrayList<String>(); 

		if (T.containsKey("-programSpecific")){
			this.extra = Functions.getValue(T, "-programSpecific");
			System.out.println("These blast specific flags will be added");
			System.out.println(this.extra);
			System.out.println();
			
		}
		
		
	}
	
	public static boolean checkParameters(Hashtable<String, String> T){
		if(!T.containsKey("-blastprogram")) {
			System.out.println("Mandatory blast flag is missing");
			help();
			return false;
		}
		return true;
		
	}

	public static void help(){
		System.out.println("");
		System.out.println("Blast specific flags");
		System.out.println("Mandatory");
		System.out.println(Functions.fixedLength("-blastprogram <blastn|blastp|blastx>",50)+"blastn is default");
		System.out.println("");
		System.out.println("Optional");
		System.out.println(Functions.fixedLength("-blastDB <pathToBlastDB>",50)+"nt is default for nucleotide searches ");
		System.out.println(Functions.fixedLength(" ",50)+"nr is default for protein searches");
		System.out.println(Functions.fixedLength("-outfmt",50)+"See blast manual for info");
		System.out.println(Functions.fixedLength("-evalue",50)+"See blast manual for info");
		System.out.println(Functions.fixedLength("-max_target_seqs",50)+"See blast manual for info");
		System.out.println(Functions.fixedLength("-length",50)+"Will split up file to be blasted in smaller files with ");
		System.out.println(Functions.fixedLength("",50)+"total sequence length that is close to given length and run them in parallel.");
		System.out.println(Functions.fixedLength("-programSpecific [bowtieSpecific]",50)+"Here you can add any Blast specific fla	gs");
		System.out.println(Functions.fixedLength("",50)+"Remember to add the [ ]  around the flags");
			

	}




	public  void run(Hashtable<String, String> T,
			ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
			String timestamp, String inDir, String outDir, String fileName,
			String suffix) {

		if(suffix == null ){
			suffix = fileName.substring(fileName.lastIndexOf(".")+1);
		}
		
		
		if (T.containsKey("-length")) {
			ArrayList <String> sbatchScripts = new ArrayList<String>();
			
			int peptidedPerFile = Functions.getInt(T, "-length", 20000);
			ArrayList<String> fileNames = RNAfunctions.splitSize(outDir,inDir, fileName, peptidedPerFile, suffix);
			
			for (int i = 0; i < fileNames.size(); i++) {

				sbatchScripts.add(runBlastFile(generalSbatchScript, sbatch, timestamp, outDir+"/tmp",
						outDir+"/tmp", fileNames.get(i), suffix));
			}
			ArrayList<Integer> jobIDs = sbatch.startSbatchScripts(sbatchScripts);
			String file = this.getMergesbatchScript(sbatch, jobIDs, outDir+"/tmp", outDir, null);
			sbatch.startSbatchScript(file);

			try {
				ExtendedWriter EW = new ExtendedWriter(new FileWriter(outDir
						+ "/scripts/" + timestamp + "_" + fileName
						+ "_blast_removeTemp.sh"));
				
				EW.println("#This file will remove all the temporary files generated to run blast in parallel");
				EW.println("cd "+inDir+"/tmp");
				for (int i = 0; i < fileNames.size(); i++) {
					String inFileBase = IOTools.getFileBase(fileNames.get(i),
							suffix);

					EW.println("rm " + fileNames.get(i));
					EW.println("rm " + inFileBase + ".blast");
					EW.println();
				}
				EW.flush();
				EW.close();
				System.out.println("To remove all temporary files start this shell script");
				System.out.println(outDir
						+ "/scripts/" + timestamp + "_" + fileName
						+ "_blast_removeTemp.sh");
			} catch (Exception E) {
				E.printStackTrace();
			}
		} else {
			sbatch.startSbatchScript(runBlastFile(generalSbatchScript, sbatch, timestamp, inDir,
					outDir, fileName, suffix));

		}
	}


	public String getMergesbatchScript(SBATCHinfo sbatch,ArrayList <Integer> Numbers, String inDir,String outDir,String outFile){
		ExtendedWriter MergeScript = sbatch.printSBATCHInfoSTART("blastMerge",inDir, Numbers);
		MergeScript.print("java -Xmx4G -jar "+sbatch.jarPath+" -interactive -program MERGE -type blast " +
				" -inDir "+inDir+
				" -outDir "+outDir+
				" -outfmt \""+this.outputFormat +"\""+
				" -tmpFiles "+tmpFiles.get(0));
		for(int i = 1; i < tmpFiles.size();i++){
			MergeScript.print(","+tmpFiles.get(i));
		}
		if(outFile != null){
			MergeScript.print(" -outFile "+outFile);
		}

		MergeScript.println();
		MergeScript.flush();
		MergeScript.close();

		return sbatch.getSBATCHfileName("blastMerge",inDir);
	}





	public  String runBlastFile(ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
			String timestamp, String inDir, String outDir, String fileName,
			String suffix) {
		try {
			if(!IOTools.isDir(outDir + "/scripts/"))
					IOTools.mkDirs(outDir + "/scripts/");
			String outFile = IOTools.getFileBase(fileName, suffix)+ "." + refName + ".blast";
			String sbatchFileName = outDir + "/scripts/" + timestamp + "_"
					+ IOTools.getFileBase(fileName, suffix) + "_blast.sbatch";
			generalSbatchScript.println("sbatch " + sbatchFileName);

			ExtendedWriter EW = new ExtendedWriter(new FileWriter(
					sbatchFileName));

			sbatch.printSBATCHinfo(EW, outDir, timestamp, 0, IOTools.getFileBase(fileName, suffix)
					+ ".blast");


			EW.println("# loading prerequistit modules at uppmax");
			EW.println("module load bioinfo-tools");
			EW.println("module load blast/2.2.31+");

			EW.println("# going to correct directory");
			EW.println("cd " + inDir);
			EW.println(Blast.BlastCommand(blastProgram,fileName, blastDB, outFile,
					Evalue,this.outputFormat,this.max_target_seqs,this.extra,sbatch.getNrOfCores()));
			if(this.outputFormat.contains("5")){
				EW.println("mv "+outFile+".tmp "+outDir+"/"+outFile+".xml");
				this.tmpFiles.add(outFile+".xml");
			}
			else if(this.outputFormat.contains("6")){
				EW.println("mv "+outFile+".tmp "+outDir+"/"+outFile+".tab");
				this.tmpFiles.add(outFile+".tab");
			}else{
				EW.println("mv "+outFile+".tmp "+outDir+"/"+outFile+"");
				this.tmpFiles.add(outFile+"");
			}
			EW.flush();
			EW.close();
			return sbatchFileName;

		} catch (Exception E) {
			E.printStackTrace();
		}
		return null;
	}

	public  void getBlastFileName( String fileName,String suffix) {
		try {
			String inFileBase = IOTools.getFileBase(fileName, suffix);
			this.tmpFiles.add(inFileBase + "." + refName + ".blast");

		} catch (Exception E) {
			E.printStackTrace();
		}
	}


	public static void mergeTab(ArrayList<String> sequenceFiles, String dir,
			String outFile) {

		System.out.println("merging blast files in folder " + dir + " to file "
				+ outFile);

		try {
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(dir + "/"
					+ outFile));
			ExtendedReader ER = new ExtendedReader(new FileReader(dir + "/"
					+ sequenceFiles.get(0)));
			while (ER.more()) {
				EW.println(ER.readLine());
			}
			ER.close();
			for (int i = 1; i < sequenceFiles.size(); i++) {
				System.out.println(" now adding file " + sequenceFiles.get(i));
				ER = new ExtendedReader(new FileReader(dir + "/"
						+ sequenceFiles.get(i)));
				while (ER.more()) {
					if (ER.lookAhead() != '#')
						EW.println(ER.readLine());
					else
						ER.skipLine();
				}
				ER.close();
			}
			EW.flush();
			EW.close();
		} catch (Exception E) {
			E.printStackTrace();
		}
	}


	public void merge(String inDir, String outDir,ArrayList<String> tmpFiles, String outFile){
		this.tmpFiles = tmpFiles;
		if(this.outputFormat.compareTo("5") == 0){
			if(outFile == null)
				outFile = IOTools.longestCommonPrefix(tmpFiles)+".fa.blast.xml";
			this.mergeXML(inDir, outDir, outFile);

		}else if (this.outputFormat.contains("6")){
			if(outFile == null)
				outFile = IOTools.longestCommonPrefix(tmpFiles)+".fa.blast.tab";
			this.mergeTab(inDir, outDir, outFile);
		}else{

			System.out.println("Can only process outformat 5 (xml) and 6 (tab delimented) will only concatenate your files");
			if(outFile == null)
				outFile = IOTools.longestCommonPrefix(tmpFiles)+".blast";
			this.mergeTab(inDir, outDir, outFile);

		}
	}

	private void mergeTab(String inDir, String outDir, String outFile) {

		System.out.println("merging blast files in folder " + inDir + " to file "
				+ outDir+"/"+outFile);

		try {
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(outDir + "/"
					+ outFile));
			ExtendedReader ER = new ExtendedReader(new FileReader(inDir + "/"
					+ tmpFiles.get(0)));
			while (ER.more()) {
				EW.println(ER.readLine());
			}
			ER.close();
			for (int i = 1; i < tmpFiles.size(); i++) {
				System.out.println(" now adding file " + tmpFiles.get(i));
				ER = new ExtendedReader(new FileReader(inDir + "/"
						+ tmpFiles.get(i)));
				while (ER.more()) {
					if (ER.lookAhead() != '#')
						EW.println(ER.readLine());
					else
						ER.skipLine();
				}
				ER.close();
			}
			EW.flush();
			EW.close();
		} catch (Exception E) {
			E.printStackTrace();
		}
	}

//
//	private void merge(String inDir, String outDir, String outFile) {
//
//		System.out.println("merging blast files in folder " + inDir + " to file "
//				+ outDir+"/"+outFile);
//
//		try {
//			ExtendedWriter EW = new ExtendedWriter(new FileWriter(outDir + "/"
//					+ outFile));
//			for (int i = 0; i < tmpFiles.size(); i++) {
//				System.out.println(" now adding file " + tmpFiles.get(i));
//				ExtendedReader ER = new ExtendedReader(new FileReader(inDir + "/"
//						+ tmpFiles.get(i)));
//				while (ER.more()) {
//					EW.println(ER.readLine());
//				}
//				ER.close();
//			}
//			EW.flush();
//			EW.close();
//		} catch (Exception E) {
//			E.printStackTrace();
//		}
//	}


	public void mergeXML(String inDir, String outDir, String outFile) {

		System.out.println("merging blast files in folder " + inDir + " to file "
				+ outDir+"/"+outFile);

		try {
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(outDir + "/"
					+ outFile));
			ExtendedReader ER = new ExtendedReader(new FileReader(inDir + "/"
					+ tmpFiles.get(0)));
			String Line = ER.readLine();
			while (ER.more() && Line.compareTo("</BlastOutput_iterations>") != 0) {
				EW.println(Line);
				Line = ER.readLine();
			}
			ER.close();
			for (int i = 1; i < tmpFiles.size(); i++) {
				System.out.println(" now adding file " + tmpFiles.get(i));
				ER = new ExtendedReader(new FileReader(inDir + "/"
						+ tmpFiles.get(i)));

				Line = ER.readLine();
				while (ER.more() && Line.compareTo("<BlastOutput_iterations>") != 0) {
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

	public static String BlastCommand(String blastProgram,String inFile, String blastDB,
			String outFile, double Evalue, String outFormat, int max_target_seqs, String extra) {
		return BlastCommand(blastProgram,inFile, blastDB,
				outFile, Evalue, outFormat, max_target_seqs, extra, 1);
	}
	public static String BlastCommand(String blastProgram,String inFile, String blastDB,
			String outFile, double Evalue, String outFormat, int max_target_seqs, String extra,int nrOfThreads) {

		String blastCommand = 
		blastProgram
				+" -query " + inFile  
				+" -db " + blastDB
				+" -outfmt \""+outFormat+"\""
				+" -out " +outFile +".tmp"
				+" -num_threads "+ nrOfThreads;

		if(extra != null)
			blastCommand = blastCommand+" "+extra;
		else{
				blastCommand = blastCommand+" -evalue " + Evalue
				+" -max_target_seqs " + max_target_seqs;
		}
				
		return blastCommand;

	}


	public static void buildBD() {

	}

}
