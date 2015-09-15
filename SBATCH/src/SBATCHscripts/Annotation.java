package SBATCHscripts;

import general.ExtendedWriter;
import general.Functions;
import general.IOTools;
import general.RNAfunctions;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;


public class Annotation {

	String inFile;
	String inDir;
	int length;
	boolean rfam;
	boolean hmmer;

	HMMER hmmerProg;
	RFAM RfamProg;
	
	
	boolean RNA;



	public Annotation() {
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

		
		if (T.containsKey("-cmsearch")){
			this.rfam = true;
			this.hmmer = false;
		}else if(T.containsKey("-hmmer")){
			this.rfam = false;
			this.hmmer = true;
		}else{
			this.rfam = false;
			this.hmmer = false;
			
		}
		if(hmmer){
			hmmerProg = new HMMER(T);
		}
		if(rfam){
			RfamProg = new RFAM(T);
		}

		if (allPresent) {
			annotateSequences(sbatch,T);
		} else
			System.out
			.println("\n\nAborting run because of missing arguments.");
	}

	public void annotateSequences(SBATCHinfo sbatch,Hashtable<String, String> T) {
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
				System.out.println("Merging hmmer jobs files have jobid "+ mergeNumber);  

			}
			if(this.rfam){
				if (!IOTools.isDir(inDir + "/RFAM")){
					IOTools.mkDir(inDir + "/RFAM");
					IOTools.mkDir(inDir + "/RFAM/scripts");
					IOTools.mkDir(inDir + "/RFAM/reports");
				}
				ExtendedWriter EW = ExtendedWriter.getFileWriter(inDir
						+ "/scripts/" + sbatch.getTimeStamp() + ".Annotation.RFAM.sh");
				for(int i = 0; i < fileNames.size();i++){
					RfamProg.runRfamFile(EW, sbatch,  inDir+"/tmp", inDir+"/RFAM", fileNames.get(i));
				}
				EW.flush();
				EW.close();
				ArrayList <Integer> Numbers = sbatch.startSbatchScripts(inDir
						+ "/scripts/" + sbatch.getTimeStamp() + ".Annotation.RFAM.sh");
				System.out.println();
				System.out.println();
//				String sbatchScriptName = RfamProg.getMergesbatchScript(sbatch,Numbers,inDir+"/blast",inDir,this.inFile+".blast.tab" );
//				Integer mergeNumber  = sbatch.startSbatchScript(sbatchScriptName);
//				System.out.println("Merging blast files have jobid "+ mergeNumber);  

			}
		}catch(Exception E){
			E.printStackTrace();
		}
	}
}

