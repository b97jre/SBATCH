package SBATCHscripts;

import general.ExtendedWriter;
import general.Functions;
import general.IOTools;
import general.RNAfunctions;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;


public class Blast2GO {

	String inFile;
	String inDir;
	int length;
	boolean blast;
	boolean interproscan;
	boolean protein;

	Blast blastProgram;
	Interproscan IPS;



	public Blast2GO() {
	}


	public void run(Hashtable<String, String> T, SBATCHinfo sbatch) {

		boolean allPresent = true;

		if (T.containsKey("-i"))
			inFile = Functions.getValue(T, "-i", ".");
		else {
			System.out.println("must contain inFile -i");
			return;
		}
		if (T.containsKey("-type")){
			String kind = Functions.getValue(T, "-type");
			if(kind.compareTo("p") == 0) this.protein = true;
			else if (kind.compareTo("n") == 0)this.protein = false;
			else{
				System.out.println("-type can be either p for protein or n for nucleotide");
				return;
			}
		}
		else {
			System.out.println("must contain what kind of sequences the file contains -type <p|n>");
			return;
		}
		
		inFile = new File(inFile).getAbsolutePath();
		inDir = new File(inFile).getParent();
		inFile = new File(inFile).getName();
		this.length = Functions.getInt(T, "-length", 10000);

		if (T.containsKey("-blast")){
			this.blast = true;
			T.put("-outfmt", "5");
			T.put("-evalue", "0.001");
			T.put("-max_target_seqs", "20");
			if(this.protein)
				T.put("-blastprogram","blastp");
			else
				T.put("-blastprogram","blastx");
			blastProgram = new Blast(T);
			this.interproscan = false;

		}
		else if(T.containsKey("-interproscan")){
			this.blast = false;
			this.interproscan = true;
			IPS = new Interproscan(T);

		}
		else{
			this.blast = true;
			T.put("-outfmt", "5");
			T.put("-evalue", "0.001");
			T.put("-max_target_seqs", "20");
			if(this.protein)
				T.put("-blastprogram","blastp");
			else
				T.put("-blastprogram","blastx");
			blastProgram = new Blast(T);
			this.interproscan = true;
			IPS = new Interproscan(T);
		}

		if (allPresent) {
			annotateAAsequences(sbatch,T);
		} else
			System.out
			.println("\n\nAborting run because of missing arguments.");
	}

	public void annotateAAsequences(SBATCHinfo sbatch,Hashtable<String, String> T) {
		try {
			if (!IOTools.isDir(inDir + "/scripts"))
				IOTools.mkDir(inDir + "/scripts");
			ArrayList <String> fileNames = RNAfunctions.splitSize(inDir, this.inFile ,length, this.inFile.substring(this.inFile.lastIndexOf(".")+1));

			if(this.interproscan){
				if (!IOTools.isDir(inDir + "/InterproScan")){
					IOTools.mkDir(inDir + "/InterproScan");
					IOTools.mkDir(inDir + "/InterproScan/scripts");
					IOTools.mkDir(inDir + "/InterproScan/reports");
					
				}
				
				ExtendedWriter EW = ExtendedWriter.getFileWriter(inDir
						+ "/scripts/" + sbatch.getTimeStamp() + ".blast2GO.InterproScan.sh");
				for(int i = 0; i < fileNames.size();i++){
					IPS.runInterproScanFile(EW, sbatch, inDir+"/tmp",inDir+"/InterproScan", fileNames.get(i), this.protein);
				}
				EW.flush();
				EW.close();
				ArrayList <Integer> Numbers = sbatch.startSbatchScripts(inDir
						+ "/scripts/" + sbatch.getTimeStamp() + ".blast2GO.InterproScan.sh");
				System.out.println();
				System.out.println();
				
				String sbatchScriptName = IPS.mergeXMLsbatchScript(sbatch, Numbers, inDir+"/InterproScan",inDir,this.inFile+".interproscan.xml" );
				
				Integer mergeNumber  = sbatch.startSbatchScript(sbatchScriptName);
				System.out.println("Merging Interproscan files have jobid "+ mergeNumber);  
				
			}
			
			
			
			
			
			if(this.blast){
				if (!IOTools.isDir(inDir + "/blast")){
					IOTools.mkDir(inDir + "/blast");
					IOTools.mkDir(inDir + "/blast/scripts");
					IOTools.mkDir(inDir + "/blast/reports");
				}
				ExtendedWriter EW = ExtendedWriter.getFileWriter(inDir
						+ "/scripts/" + sbatch.getTimeStamp() + ".blast2GO.Blast.sh");
				for(int i = 0; i < fileNames.size();i++){
					blastProgram.runBlastFile(EW, sbatch, sbatch.getTimeStamp(), inDir+"/tmp", inDir+"/blast", fileNames.get(i), this.inFile.substring(this.inFile.lastIndexOf(".")+1));
				}
				EW.flush();
				EW.close();
				ArrayList <Integer> Numbers = sbatch.startSbatchScripts(inDir
						+ "/scripts/" + sbatch.getTimeStamp() + ".blast2GO.Blast.sh");
				System.out.println();
				System.out.println();
				String sbatchScriptName = blastProgram.getMergesbatchScript(sbatch,Numbers,inDir+"/blast",inDir,this.inFile+".blast.xml" );
				Integer mergeNumber  = sbatch.startSbatchScript(sbatchScriptName);
				System.out.println("Merging blast files have jobid "+ mergeNumber);  
				
				
				
			}
		} catch (Exception E) {
			E.printStackTrace();
		}
	}





}
