package SBATCHscripts;

import general.ExtendedWriter;
import general.Functions;
import general.IOTools;
import general.RNAfunctions;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;


public class Argot2 {

	String inFile;
	String inDir;
	int length;
	boolean blast;
	boolean hmmer;

	HMMER hmmerProg;
	Blast blastProgram;
	boolean RNA;



	public Argot2() {
	}


	public void run(Hashtable<String, String> T, SBATCHinfo sbatch) {

		boolean allPresent = true;

		if (T.containsKey("-i"))
			inFile = Functions.getValue(T, "-i", ".");
		else {
			System.out.println("must contain inFile -i");
			return;
		}
		inFile = new File(inFile).getAbsolutePath();
		inDir = new File(inFile).getParent();
		inFile = new File(inFile).getName();
		this.length = Functions.getInt(T, "-length", 10000);

		if (T.containsKey("-RNA"))this.RNA = true;
		else this.RNA =false;
		
		
		if (T.containsKey("-blast")){
			this.blast = true;
			this.hmmer = false;
		}else if(T.containsKey("-hmmer")){
			this.blast = false;
			this.hmmer = true;
		}else{
			this.blast = true;
			this.hmmer = true;
			
		}
		if(blast){
			T.put("-outfmt", "6 qseqid sseqid evalue ");
			T.put("-evalue", "0.001");
			T.put("-max_target_seqs", "20");
			T.put("-blastprogram","blastp");
			T.put("-blastDB","/sw/data/uppnex/blast_databases/uniprot_all.fasta");
			blastProgram = new Blast(T);
		}
		if(hmmer){
			hmmerProg = new HMMER(T);
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

			if(this.hmmer){
				if (!IOTools.isDir(inDir + "/HMMER")){
					IOTools.mkDir(inDir + "/HMMER");
					IOTools.mkDir(inDir + "/HMMER/scripts");
					IOTools.mkDir(inDir + "/HMMER/reports");

				}
				ExtendedWriter EW = ExtendedWriter.getFileWriter(inDir
						+ "/scripts/" + sbatch.getTimeStamp() + ".ARGOT2.HMMERscan.sh");
				for(int i = 0; i < fileNames.size();i++){
					hmmerProg.runHmmerFile(EW, sbatch, inDir+"/tmp", inDir+"/HMMER", fileNames.get(i));
				}
				EW.flush();
				EW.close();
				ArrayList <Integer> Numbers = sbatch.startSbatchScripts(inDir
						+ "/scripts/" + sbatch.getTimeStamp() + ".ARGOT2.HMMERscan.sh");
				System.out.println();
				System.out.println();

				String sbatchScriptName = hmmerProg.getMergesbatchScript(sbatch, Numbers, inDir+"/HMMER",inDir,this.inFile+"."+hmmerProg.refName+".hmmer");
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
						+ "/scripts/" + sbatch.getTimeStamp() + ".ARGOT2.Blast.sh");
				for(int i = 0; i < fileNames.size();i++){
					blastProgram.runBlastFile(EW, sbatch, sbatch.getTimeStamp(), inDir+"/tmp", inDir+"/blast", fileNames.get(i), this.inFile.substring(this.inFile.lastIndexOf(".")+1));
				}
				EW.flush();
				EW.close();
				ArrayList <Integer> Numbers = sbatch.startSbatchScripts(inDir
						+ "/scripts/" + sbatch.getTimeStamp() + ".ARGOT2.Blast.sh");
				System.out.println();
				System.out.println();
				String sbatchScriptName = blastProgram.getMergesbatchScript(sbatch,Numbers,inDir+"/blast",inDir,this.inFile+".blast.tab" );
				Integer mergeNumber  = sbatch.startSbatchScript(sbatchScriptName);
				System.out.println("Merging blast files have jobid "+ mergeNumber);  

			}
		}catch(Exception E){
			E.printStackTrace();
		}
	}
}

