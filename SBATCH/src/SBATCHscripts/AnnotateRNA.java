package SBATCHscripts;

import general.ExtendedReader;
import general.ExtendedWriter;
import general.Functions;
import general.IOTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;

import Sequence.FastaSequences;
import Sequence.Generate;

public class AnnotateRNA {

	int peptidedPerFile;

	Blast blastProgram;
	PFAM PFAMprogram;


	String fileBase;
	ArrayList<String> fileNames;
	String inDir;
	String fileName;
	String suffix;

	int kmer;


	public AnnotateRNA(Hashtable<String, String> T) {
		//blastProgram = new Blast(T);
		//PFAMprogram = new PFAM();

		peptidedPerFile = Functions.getInt(T, "-split", 20000);
		String inFile = Functions.getValue(T, "-i");
		suffix = Functions.getValue(T, "-suffix", inFile.substring((inFile.lastIndexOf(".")+1)));
		kmer = Functions.getInt(T, "-kmer", 3);

		File inFile2 = new File(inFile);
		inDir = new File(inFile2.getAbsolutePath()).getParent();
		fileName = inFile2.getName();
		fileBase = IOTools.getFileBase(fileName, suffix);

	}

	public void help(){


		System.out.println("");
		System.out.println("Blast specific flags");
		Blast.help();
		System.out.println("");
		System.out.println("PFAM specific flags");
		PFAM.help();
		System.out.println("");
	}


	public boolean hasSufficientInfo(){
		// needs to be filled in for more proper controll
		return true;
	}



	public void run(Hashtable<String, String> T,SBATCHinfo sbatch,String inFileBody){

			ExtendedWriter annotateSbatchScript = sbatch.printSBATCHInfoSTART("annotateRNA",inDir,0,null);
			String sbatchScriptFile = sbatch.getSbatchFileName("annotateRNA",inDir,0);
			
			// Remove duplicates 
			annotateSbatchScript.println();
			annotateSbatchScript.println("cd-hit-est -i "+inFileBody+"."+suffix+
					" -o "+inFileBody+".c1."+suffix +" -c 1");
			annotateSbatchScript.println();
			inFileBody = inFileBody+".c1"; 
			
			annotateSbatchScript.println("java -Xmx"+sbatch.getMemoryPadded()+"G -jar "+sbatch.jarPath+" -email "+sbatch.getEmail()+" -pNr "+sbatch.getProjectNumber()+" -time 4:00:00 -program BLAST2GO -length 1000000  -interproscan -type p -i "+this.fileName);
		
			
			
			annotateSbatchScript.println("java -Xmx"+sbatch.getMemoryPadded()+"G -jar "+sbatch.jarPath+" -email "+sbatch.getEmail()+" -pNr "+sbatch.getProjectNumber()+" -time 4:00:00 -program BLAST2GO -length 1000000  -interproscan -type p -i "+this.fileName);

			sbatch.printShellInfoSTOP(annotateSbatchScript, "findORFs",inDir);
//			runningFile.add(sbatch.startSbatchScript(sbatchScriptFile));
			
			
		}
		//		if(T.containsKey("-merge")){
		//			try{
		//				this.InitiatePrograms(initiateSbatchScript, sbatch, sbatch.timeStamp);
		//			}catch(Exception E){
		//			E.printStackTrace();
		//			}
		//		}
//	}
//
//	public void findORFs() {
//
//		String fileBase = IOTools.getFileBase(fileName, suffix);
//		FastaSequences.getORFs(inDir,fileName);
//		Generate.calculateKmerDistribution(inDir, fileBase+".subset."+suffix, fileBase+".subset.kmers", kmer);		
//		Generate.generateSequenceFile(inDir, fileName, fileBase+".subset.kmers", fileBase+".generated.training.fa", kmer);
//		Generate.generateSequenceFile(inDir, fileName, fileBase+".subset.kmers", fileBase+".generated.evaluation.fa", kmer);
//
//		fileNames = FastaSequences.splitSize(inDir, fileName,peptidedPerFile, suffix);
//		// "file_nr.fa"
//
//		for (int i = 0; i < fileNames.size(); i++) {
//			blastProgram.runBlastFile(generalSbatchScript, sbatch, timestamp, inDir+"/tmp",
//					inDir+"/tmp", fileNames.get(i), suffix);
//			PFAMprogram.runPfamFile(generalSbatchScript, sbatch, timestamp, inDir+"/tmp", inDir+"/tmp", fileName);
//		}
//	}
//
//
//	public void InitiatePrograms(
//			ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
//			String timestamp) {
//
//		fileNames = FastaSequences.splitSize(inDir, fileName,peptidedPerFile, suffix);
//
//		for (int i = 0; i < fileNames.size(); i++) {
//			blastProgram.runBlastFile(generalSbatchScript, sbatch, timestamp, inDir+"/tmp",
//					inDir+"/tmp", fileNames.get(i), suffix);
//			PFAMprogram.runPfamFile(generalSbatchScript, sbatch, timestamp, inDir+"/tmp", inDir+"/tmp", fileName);
//		}
//	}
//
//	public void mergeTmpFiles(String inDir,String outDir, String fileName){
//		fileNames = FastaSequences.splitSizefileNames(inDir, fileName,peptidedPerFile, suffix);
//		for (int i = 0; i < fileNames.size(); i++) {
//			blastProgram.getBlastFileName(fileNames.get(i), suffix);
//			PFAMprogram.getPfamFileName(fileNames.get(i));
//		}
//		boolean AllPresent;
//		blastProgram.merge(inDir, outDir,this.fileBase+".blast");
//		PFAMprogram.merge(inDir, outDir,this.fileBase+".pfam");
//		this.mergeTmpFiles(inDir+"/tmp", inDir, fileName);
//	}
//
//
//
//	public void removeTmpFiles(ExtendedWriter EW){
//
//
//		for (int i = 0; i < fileNames.size(); i++) {
//			EW.println("rm " + fileNames.get(i));
//			EW.println();
//		}
//
//	}
//	public void removeTmpFiles(String tmpDir){
//		for (int i = 0; i < fileNames.size(); i++) {
//			if(IOTools.fileExists(tmpDir+"/"+fileNames.get(i)){
//				File tempFile = new File(tmpDir+"/"+fileNames.get(i));
//				if(!tempFile.delete())
//					System.out.println(tmpDir+"/"+fileNames.get(i)+" was not succesfully deleted");
//
//			}
//			else
//				System.out.println(tmpDir+"/"+fileNames.get(i)+" was not found");
//		}
//	} 
//
//



	public static void merge(ArrayList<String> sequenceFiles, String dir,
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

	public static String BlastPcommand(String inFile, String blastDB,
			String inBase, double Evalue) {

		String refName = blastDB;
		String[] temp = blastDB.split("/");
		if (temp.length > 1)
			refName = temp[temp.length - 1];

		return "blastp -query " + inFile + " -db " + blastDB
				+ " -outfmt 6 -evalue " + Evalue + " -out " + inBase + "."
				+ refName + ".blast";

	}

	public static String BlastNcommand(String inFile, String blastDB,
			String outDir) {

		String refName = blastDB;
		String[] temp = blastDB.split("/");
		if (temp.length > 1)
			refName = temp[temp.length - 1];

		return "blastn -d " + blastDB + " -i " + inFile + " -m 8 -out "
		+ inFile + "." + refName + ".blast";

	}

	public static void buildBD() {

	}

}
