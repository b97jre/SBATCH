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


public class HMMER {

	double Evalue;
	String pfamDB;
	String refName;
	
	
	ArrayList<String> tmpFiles;
	String finalFinalName;
	
	

	public HMMER(Hashtable<String, String> T) {
		
		pfamDB = Functions.getValue(T, "-pfamDB","/glob/johanr/references/Pfam/PfamAB.hmm");
		refName = new File("pfamDB").getName();
		this.tmpFiles = new ArrayList<String>(); 
	}
	
	public static void help(){
		System.out.println("");
		System.out.println("HMMER specific flags");
		System.out.println(Functions.fixedLength("--pfamDB <pathTopfamDB>",30)+"/glob/johanr/references/Pfam/PfamAB.hmm is default");
		System.out.println("");
		
	}

	
	
	
	public  String runHmmerFile(ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
			String inDir, String outDir, String fileName){
		try {
			String inFileBase = IOTools.getFileBase(fileName, null);

			String sbatchFileName = outDir + "/scripts/" + sbatch.getTimeStamp() + "_"
					+ inFileBase + "_HMMER.sbatch";
			generalSbatchScript.println("sbatch " + sbatchFileName);

			ExtendedWriter EW = new ExtendedWriter(new FileWriter(
					sbatchFileName));

			sbatch.printSBATCHinfo(EW, outDir, sbatch.getTimeStamp(), 0, inFileBase
					+ ".HMMER");


			EW.println("# loading prerequistit modules at uppmax");
			EW.println("module load bioinfo-tools");
			EW.println("module load hmmer/3.1b1-gcc");

			EW.println("# going to correct directory");
			EW.println("cd " + inDir);
			EW.println(HMMER.HmmerCommand(fileName, pfamDB));

			String outFileName = HMMER.getOutFileName(fileName, pfamDB);
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
	
	
	
	public String getMergesbatchScript(SBATCHinfo sbatch,ArrayList <Integer> Numbers, String inDir,String outDir,String outFile){
		ExtendedWriter MergeScript = sbatch.printSBATCHInfoSTART("HmmerMerge",inDir, Numbers);
		MergeScript.print("java -Xmx4G -jar "+sbatch.jarPath+" -interactive -program MERGE -type HMMER " +
				" -inDir "+inDir+
				" -outDir "+outDir+
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

		return sbatch.getSBATCHfileName("HmmerMerge",inDir);
	}


	public static void merge(String inDir, String outDir,ArrayList<String> sequenceFiles,
			String outFile) {

		System.out.println("merging Hmmer files in folder " + inDir + " to file "+
				outDir+"/"+ outFile);

		try {
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(outDir + "/"
					+ outFile));


			
			ExtendedReader ER = new ExtendedReader(new FileReader(inDir + "/"
					+ sequenceFiles.get(0)));
			for(int i = 0; i< 3; i++){ 
				EW.println(ER.readLine());
			}
			ER.close();
			
			for (int i = 0; i < sequenceFiles.size(); i++) {
				System.out.println(" now adding file " + sequenceFiles.get(i));
				ER = new ExtendedReader(new FileReader(inDir + "/"
						+ sequenceFiles.get(i)));
				while (ER.more()) {
					if (ER.lookAhead() != '#'){
						String line = ER.readLine();
						EW.println(line);
						System.out.println(line);
					}
					else
						ER.skipLine();
				}
				ER.close();
			}
			EW.println("#");
			ER = new ExtendedReader(new FileReader(inDir + "/"
					+ sequenceFiles.get(0)));
			boolean print = false;
			while (ER.more()) {
				String line = ER.readLine();
				if(!print && line.indexOf("# Program") == 0)print = true;
				if(print) EW.println(line);
			}
			ER.close();
			
			EW.flush();
			EW.close();
		} catch (Exception E) {
			E.printStackTrace();
		}
	}
	
	
	public void merge(String inDir, String outDir, String outFile) {

		System.out.println("merging Hmmer files in folder " + inDir + " to file "
				+ outFile);

		try {
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(outDir + "/"
					+ outFile));
			System.out.println(" now adding file " + tmpFiles.get(0));
			ExtendedReader ER = new ExtendedReader(new FileReader(inDir + "/"
					+ tmpFiles.get(0)));
			while (ER.more() && ER.lookAhead() == '#') {
				EW.println(ER.readLine());
			}
			while (ER.more() && ER.lookAhead() != '#') {
				EW.println(ER.readLine());
			}
			ER.close();
			for (int i = 1; i < tmpFiles.size()-1; i++) {
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
			System.out.println(" now adding file " + tmpFiles.get(tmpFiles.size()-1));
			ER = new ExtendedReader(new FileReader(inDir + "/"
					+ tmpFiles.get(tmpFiles.size()-1)));
			while (ER.more() && ER.lookAhead() == '#') {
				ER.skipLine();
			}
			while (ER.more()){
				EW.println(ER.readLine());
			}
			ER.close();
			EW.flush();
			EW.close();
		} catch (Exception E) {
			E.printStackTrace();
		}

	}

	public static String HmmerCommand(String inFile, String pfamDB) {

		String refName = new File(pfamDB).getName();
		String inBase = IOTools.getFileBase(new File(inFile).getName(),null);
   
		return "hmmscan" 
				+" --tblout " + inBase + "."+ refName + ".hmmer.tmp"
				+" "+pfamDB+" "+inFile;
	
	}
	
	public static String getOutFileName(String inFile, String pfamDB) {
		String refName = new File(pfamDB).getName();
		String inBase = IOTools.getFileBase(new File(inFile).getName(),null);
		return inBase + "."+ refName + ".hmmer";
	}

}
