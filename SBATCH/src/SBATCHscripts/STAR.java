package SBATCHscripts;

import general.ExtendedWriter;
import general.ExtendedReader;
import general.Functions;
import general.IOTools;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.Hashtable;

import SBATCHscripts.GeneralPairFile.Programs;

public class STAR {

	String referenceDir;
	String suffix;
	int nrOfThreads;

	String parameterFile;	
	
	boolean strandSpecifik;
	boolean interactive;
	boolean sam2bam;

	public STAR(){
	}

	
	
	public boolean addParameters(Hashtable<String, String> T, SBATCHinfo sbatch){
		boolean allPresent = true;

		if (T.containsKey("-refDir")) {
			referenceDir = Functions.getValue(T, "-refDir", ".");
			File test = new File(referenceDir);
			if(test.isDirectory())
				referenceDir = test.getAbsolutePath();
			else{
				allPresent = false;
			}
				
		} else {
			allPresent = false;
		}

		this.sam2bam = true;

		this.nrOfThreads = sbatch.getNrOfCores();

		if (T.containsKey("-parameterFile")) {
			this.parameterFile = Functions.getValue(T, "-parameterFile");
		}else{
			allPresent = false;
		}
		if(allPresent) return true;
		help();
		return false;
	}
	
	
	
//	
//	public void run(Hashtable<String, String> T, SBATCHinfo sbatch) {
//
//		String inDir, outDir, logDir;
//		inDir = outDir = logDir = null;
//		boolean allPresent = true;
//		this.interactive = false;
//
//
//
//		// if(T.containsKey(key))
//
//		// if(T.containsKey("-searchspace"))
//		// findOptimalValues(T,sbatch, timeStamp, outDir);
//		// else
//			
//			STAR(sbatch, inDir, projectDir + "/" + outDir);
//		} else
//			System.out
//					.println("\n\nAborting run because of missing arguments for STAR.");
//	}
//
//	//
//	// public void findOptimalValues(Hashtable<String,String> T,SBATCHinfo
//	// sbatch, String timeStamp, String outDir){
//	// String forward,reverse;
//	// forward = reverse = null;
//	// boolean allPresent = true;
//	// if(T.containsKey("-1") && T.containsKey("-2")){
//	// forward = Functions.getValue(T, "-1", ".");
//	// reverse = Functions.getValue(T, "-2", ".");
//	// }
//	// else if(T.containsKey("-U") ){
//	// forward = Functions.getValue(T, "-U", ".");
//	// }
//	// else{
//	// System.out.println("must contain sequence file");
//	// allPresent = false;
//	// }
//	// if(allPresent){
//	// try{
//	// if(!IOTools.isDir(projectDir+"/scripts"))
//	// IOTools.mkDir(projectDir+"/scripts");
//	// ExtendedWriter EW = new ExtendedWriter(new
//	// FileWriter(projectDir+"/scripts/"+timeStamp+"_STAR.sh"));
//	// searchSpace(EW,sbatch, forward, reverse, outDir, timeStamp,8);
//	// EW.flush();
//	// EW.close();
//	// }catch(Exception E){E.printStackTrace();}
//	// }
//	// }
//	//
//
//	public void STAR(SBATCHinfo sbatch,String inDir,
//			String outDir) {
//		try {
//			if (!IOTools.isDir(projectDir + "/scripts"))
//				IOTools.mkDir(projectDir + "/scripts");
//			ExtendedWriter EW = new ExtendedWriter(new FileWriter(projectDir
//					+ "/scripts/" + timeStamp + ".STAR.sh"));
//			if (interactive)
//				EW = new ExtendedWriter(new FileWriter(projectDir
//						+ "/scripts/STAR.sh"));
//			if (interactive)
//				STAR.STARCommandLoadGenome(EW, referenceDir);
//			if (files) {
//				STARFile(EW, sbatch, timeStamp, outDir, forward, reverse);
//			} else {
//				STARDir(EW, sbatch, timeStamp, inDir, outDir);
//			}
//			if (interactive)
//				STAR.STARCommandRemoveGenome(EW, referenceDir);
//
//			EW.flush();
//			EW.close();
//
//			System.out
//					.println("Execute the following command to start all the runs:");
//			if (interactive)
//				System.out.println("sh " + projectDir + "/scripts/STAR.sh >"
//						+ projectDir + "/scripts/STAR.sh.out ");
//			else
//				System.out.println("sh " + projectDir + "/scripts/" + timeStamp
//						+ ".STAR.sh >&" + projectDir + "/scripts/" + timeStamp
//						+ ".STAR.sh.out ");
//		} catch (Exception E) {
//			E.printStackTrace();
//		}
//	}
//
//	public void STARDir(ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
//			String inDir, String outDir, String fileName1, String fileName2) {
//
//		ArrayList<String> fileNames = IOTools.getSequenceFiles(inDir, suffix);
//		if (!fileNames.isEmpty()) {
//			if (!IOTools.isDir(outDir))
//				IOTools.mkDirs(outDir);
//			try {
//				ArrayList<String[]> pairs = IOTools.findPairs(fileNames, sep);
//				if (pairs.size() != 0) {
//					for (int i = 0; i < pairs.size(); i++) {
//						String[] temp = referenceDir.split("/");
//						String refName = referenceDir;
//						if (temp.length > 1)
//							refName = temp[temp.length - 1];
//
//						String readsName = pairs.get(i)[0].substring(0,
//								pairs.get(i)[0].indexOf(sep[0]));
//
//						String newOutDir = outDir + "/" + readsName + "_"
//								+ refName;
//
//						STARFile(generalSbatchScript, sbatch, timestamp,
//								newOutDir, inDir + "/" + pairs.get(i)[0], inDir
//										+ "/" + pairs.get(i)[1]);
//					}
//				} else {
//					// System.out.println("Something wrong with the seperators? Asuming that these are single end reads");
//					// System.out.println(sep[0]);
//					// System.out.println(sep[1]);
//					for (int i = 0; i < fileNames.size(); i++) {
//						System.out.println(fileNames.get(i));
//
//						String[] temp = referenceDir.split("/");
//						String refName = referenceDir;
//						if (temp.length > 1)
//							refName = temp[temp.length - 1];
//
//						String fileName = fileNames.get(i).substring(0,
//								fileNames.get(i).indexOf(this.suffix));
//						if (fileName.lastIndexOf(".") == fileName.length())
//							fileName.subSequence(0, fileName.length() - 1);
//
//						String newOutDir = outDir + "/" + fileName + "_"
//								+ refName;
//						STARFile(generalSbatchScript, sbatch, timestamp,
//								newOutDir, inDir + "/" + fileNames.get(i), null);
//					}
//				}
//			} catch (Exception E) {
//				E.printStackTrace();
//			}
//		}
//		ArrayList<String> subDirs = IOTools.getDirectories(inDir);
//		for (int i = 0; i < subDirs.size(); i++) {
//			STARDir(generalSbatchScript, sbatch, timestamp, inDir + "/"
//					+ subDirs.get(i), outDir + "/" + subDirs.get(i));
//		}
//
//	}
	
	

	public void STARFile(ExtendedWriter generalSbatchScript, SBATCHinfo sbatch, String inDir, String outDir, String forward, String reverse) {

		String commonName = Functions.getCommonPrefix(forward, reverse);
		String[] temp = referenceDir.split("/");
		File refDir = new File(referenceDir);
		String refName = refDir.getName();
		
		outDir = outDir+"/"+commonName+"_"+refName;
		if (!IOTools.isDir(outDir))
			IOTools.mkDirs(outDir);
		if (!IOTools.isDir(outDir + "/reports"))
			IOTools.mkDir(outDir + "/reports");
		if (!IOTools.isDir(outDir + "/scripts"))
			IOTools.mkDir(outDir + "/scripts");
			
		try {
			
			String sbatchFileName = outDir + "/scripts/" + sbatch.timeStamp+"_"+commonName+"_"+refName
					+ "_STAR.sbatch";
			if (!sbatch.interactive)
				generalSbatchScript.println("sbatch " + sbatchFileName);
			else {
				sbatchFileName = outDir + "/scripts/"+commonName+"STAR.sh";
				generalSbatchScript.println("sh " + sbatchFileName);
			}
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(
					sbatchFileName));
			sbatch.printSBATCHinfo(EW, outDir, sbatch.timeStamp, commonName, "STAR");
			EW.println();
			EW.println("cd " + outDir);
			STARCommand(EW, inDir + "/" + forward,
						inDir + "/" + reverse);

			if (sam2bam) {
				SamtoolsSBATCH.sam2bam(EW, outDir + "/Aligned.out.sam", -1, -1,
						true, true, true, true, true);
			}


			EW.flush();
			EW.close();

		} catch (Exception E) {
			E.printStackTrace();
		}
	}


	public void STARCommand(ExtendedWriter EW,
			 String inFile1, String inFile2) {

		String STARcommand = "STAR ";
		STARcommand += " --genomeDir " + this.referenceDir;
		STARcommand += " --readFilesIn " + inFile1;
		if (inFile2 != null) {
			STARcommand += " " + inFile2;
		}
		STARcommand += " --runThreadN " + nrOfThreads;
		// if(!strandSpecifik) STARcommand +=
		// " --outSAMstrandField intronMotif ";
		if (suffix.indexOf("gz") > 0)
			STARcommand += " --readFilesCommand zcat ";
		if (suffix.indexOf("bz2") > 0)
			STARcommand += " --readFilesCommand bzcat ";

		try {
			ExtendedReader ER = new ExtendedReader(
					new FileReader(parameterFile));
			while (ER.more()) {
				if (ER.lookAhead() != '#') {
					String line = ER.readLine();
					String[] command = line.split("\t");
					if (command.length != 2) {
						System.out.println("Somehting wrong with line ");
						System.out.println(line);
						System.out
								.println("Ignoring the command given in this line");
					} else {
						STARcommand += " --" + command[0] + " " + command[1];
					}
				}
			}
			ER.close();
		} catch (Exception E) {
			E.printStackTrace();
		}

		EW.println("echo START");
		EW.println("echo using parameters from file " + parameterFile);
		EW.println();
		EW.println("echo \"" + STARcommand + "\" 1>&2");
		EW.println(STARcommand);
		EW.println();
		EW.println("echo DONE");

	}

	public static void STARCommandLoadGenome(ExtendedWriter EW, String refDir) {

		String STARcommand = "STAR ";
		STARcommand += " --genomeDir " + refDir;
		STARcommand += " --genomeLoad LoadAndExit";

		EW.println("echo START");
		EW.println();
		EW.println("echo \"" + STARcommand + "\" 1>&2");
		EW.println(STARcommand);
		EW.println();
		EW.println("echo DONE");

	}

	public static void STARCommandRemoveGenome(ExtendedWriter EW, String refDir) {

		String STARcommand = "STAR ";
		STARcommand += " --genomeDir " + refDir;
		STARcommand += " --genomeLoad remove";

		EW.println("echo START");
		EW.println();
		EW.println("echo \"" + STARcommand + "\" 1>&2");
		EW.println(STARcommand);
		EW.println();
		EW.println("echo DONE");

	}

	
	public void help(){

		System.out.println();
		System.out.println();
		System.out.println("Mandatory values for STAR:");
		System.out.println(Functions.fixedLength("-refDir <pathToSTARref>", 30)+"The reference directory generated by STAR");
		System.out.println("or");
		System.out.println(Functions.fixedLength("-parameterFile <pathToParamterFile>", 30)+"File containing all the extra inormation regarding parameters");

		System.out.println("Mandatory values if working on directory:");
		System.out.println();

	}

}





