package SBATCHscripts;

import general.ExtendedWriter;
import general.Functions;
import general.IOTools;
import general.RNAfunctions;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;


public class AnnotateAASequences {

	String inFile;
	String inDir;
	int length;
	boolean blast;
	boolean interproscan;
	
	

	public AnnotateAASequences() {
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

		if (T.containsKey("-blast")){
			this.blast = true;
			this.interproscan = false;
		}
		else if(T.containsKey("-interproscan")){
			this.blast = false;
			this.interproscan = true;
		}
		else{
			this.blast = true;
			this.interproscan = true;
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
			ExtendedWriter EW = ExtendedWriter.getFileWriter(inDir
					+ "/scripts/" + sbatch.getTimeStamp() + ".annotateAAs.sh");
			ArrayList <String> fileNames = RNAfunctions.splitSize(inDir, this.inFile ,length, this.inFile.substring(this.inFile.lastIndexOf(".")+1));

			for(int i = 0; i < fileNames.size();i++){
				annotateAAfiles(EW, sbatch, inDir+"/tmp",fileNames.get(i),T,this.inFile.substring(this.inFile.lastIndexOf(".")+1));
			}
			
			EW.flush();
			EW.close();
			
			
			System.out.println();
			System.out.println();
			System.out
					.println("Everything seemed to work correct. All paths to all sbatch scripts that has been started can be found here :");
			System.out.println("sh " + inDir + "/scripts/" + sbatch.getTimeStamp() +".annotateAAs.sh");
			System.out.println();
			System.out.println();

		} catch (Exception E) {
			E.printStackTrace();
		}
	}

	

	public void annotateAAfiles(ExtendedWriter generalSbatchScript,
			SBATCHinfo sbatch,String inDir, String fileName,Hashtable<String, String> T,
			String suffix) {
		try {
			if(this.interproscan){
				Interproscan IPS = new Interproscan(T);
				IPS.runInterproScanFile(generalSbatchScript, sbatch, inDir,inDir, fileName, true);
			}
			if(this.blast){
				T.put("-outfmt", "5");
				T.put("-evalue", "0.001");
				T.put("-max_target_seqs", "20");
				T.put("-blastp","");
				Blast blast = new Blast(T);
				blast.runBlastFile(generalSbatchScript, sbatch, sbatch.getTimeStamp(), inDir, inDir, fileName, suffix);
			}

		} catch (Exception E) {
			E.printStackTrace();
		}
	}


}
