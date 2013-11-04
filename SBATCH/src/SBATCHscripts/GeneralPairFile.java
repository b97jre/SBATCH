package SBATCHscripts;

import general.ExtendedReader;
import general.ExtendedWriter;
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

import SBATCHscripts.SBATCHinfo.Programs;

public class GeneralPairFile {

	String time;
	String projectDir;
	String inDir;
	String forward;
	String reverse;
	String outDir;
	String suffix;
	String[] sep;

	public GeneralPairFile() {

	}

//	public static void main(String[] args) {
//
//		int length = args.length;
//		for (int i = 0; i < length; i++) {
//			args[i] = args[i].trim();
//			System.out.print(args[i] + " ");
//		}
//		System.out.println();
//		Hashtable<String, String> T = Functions.parseCommandLine(args);
//		GeneralPairFile general = new GeneralPairFile();
//		general.run(T);
//	}
//
//

	public enum Programs {
		STAR,
		TOPHAT,
		TOPHAT2,
		BWA,
		HELP
	}	



	public void run(Hashtable<String, String> T,SBATCHinfo sbatch ) {

		boolean allPresent = true;
		String programName = Functions.getValue(T, "-program",Functions.getValue(T, "-p","help"));


		if (!T.containsKey("-i") ||(!T.containsKey("-f1") &&!T.containsKey("-f2"))){
			help();
			return;
		}

		inDir = Functions.getValue(T, "-i", null);
		forward = Functions.getValue(T, "-f1", null);
		reverse = Functions.getValue(T, "-f2", null);
		outDir = Functions.getValue(T, "-o", inDir + "_"+programName);
		projectDir = Functions.getValue(T, "-pDir", IOTools.getCurrentPath());
		suffix = Functions.getValue(T, "-suffix", IOTools.getCurrentPath());
		suffix = Functions.getValue(T, "-suffix", "fastq");
		String seperator = Functions.getValue(T, "-sep", "1." + suffix + " 2."
				+ suffix);
		this.sep = seperator.split(" ");


		if (allPresent)
			generalStart(T, sbatch);
		else
			System.out
			.println("\n\nAborting run because of missing arguments.");
	}

	public void generalStart(Hashtable<String, String> T, SBATCHinfo sbatch) {
		try {
			if (!IOTools.isDir(projectDir + "/scripts"))
				IOTools.mkDir(projectDir + "/scripts");
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(projectDir
					+ "/scripts/" + sbatch.timeStamp + "_startSBATCHScript.sh"));
			if (this.inDir != null){				
				generalDir(T, EW, sbatch,  inDir, projectDir + "/"
						+ outDir);

			}
			else if (this.forward != null && this.reverse != null) {
				String folder = this.projectDir;
				generalFileInitial(T, EW, sbatch,  folder, folder,
						this.forward, this.reverse);
			}

			EW.flush();
			EW.close();
			System.out.println("To start all the scripts write ");
			System.out.println(projectDir + "/scripts/" + sbatch.timeStamp
					+ "_startSBATCHScript.sh");

		} catch (Exception E) {
			E.printStackTrace();
		}
	}

	public void generalFileInitial(Hashtable<String, String> T,
			ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
			String inDir, String outDir, String inFile1, String inFile2) {
		if (!IOTools.isDir(outDir))
			IOTools.mkDir(outDir);
		if (!IOTools.isDir(outDir + "/reports"))
			IOTools.mkDir(outDir + "/reports");
		if (!IOTools.isDir(outDir + "/scripts"))
			IOTools.mkDir(outDir + "/scripts");

		generalFile(T, generalSbatchScript, sbatch, inDir, outDir,
				inFile1, inFile2);
	}

	public void generalDir(Hashtable<String, String> T,
			ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
			String inDir, String outDir) {

		ArrayList<String> fileNames = IOTools.getSequenceFiles(inDir, suffix);
		ArrayList<String[]> pairs = IOTools.findPairs(fileNames, sep);
		if (!pairs.isEmpty()) {
			if (!IOTools.isDir(outDir))
				IOTools.mkDirs(outDir);
			if (!IOTools.isDir(outDir + "/reports"))
				IOTools.mkDir(outDir + "/reports");
			if (!IOTools.isDir(outDir + "/scripts"))
				IOTools.mkDir(outDir + "/scripts");
			try {
				if (pairs.size() != 0) {
					for (int i = 0; i < pairs.size(); i++){
						generalFile(T, generalSbatchScript, sbatch,
								inDir, outDir, pairs.get(i)[0],pairs.get(i)[1]);
					}
				}
			}catch (Exception E) {
					E.printStackTrace();
				}
			}
			ArrayList<String> subDirs = IOTools.getDirectories(inDir);
			for (int i = 0; i < subDirs.size(); i++) {
				generalDir(T, generalSbatchScript, sbatch, inDir + "/"
						+ subDirs.get(i), outDir + "/" + subDirs.get(i));
			}
		}

		public void generalFile(Hashtable<String, String> T,
				ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
				String inDir, String outDir, String fileName1, String fileName2) {

			String programName = Functions.getValue(T, "-program",Functions.getValue(T, "-p","help"));
			Programs program = Programs.valueOf(programName.toUpperCase());

			switch (program) {
			case STAR:
				STAR star = new STAR();
				if(star.addParameters(T,sbatch)){
					star.STARFile(generalSbatchScript, sbatch, inDir, outDir,
							fileName1,fileName2);
				}
				break;
			case TOPHAT:
				System.out.println("Not yet implemented");
				break;
			case TOPHAT2:
				System.out.println("Not yet implemented");
				break;
			default: help();	

			}			

		}


		public void help(){

			System.out.println("Mandatory values for paired sequences:");
			System.out.println(Functions.fixedLength("-i <dir>", 30)+"inDirectory or inFile (-i fastqDir)");
			System.out.println("or");
			System.out.println(Functions.fixedLength("-f1 <file1>", 30)+"First file (-i file.1.fastq)");
			System.out.println(Functions.fixedLength("-f2 <file2>", 30)+"First file (-i file.2.fastq)");

			System.out.println("Mandatory values if working on directory:");
			System.out.println(Functions.fixedLength("-suffix <yourSuffix>", 30)+" suffix of files (-suffix fastq)");
			System.out.println(Functions.fixedLength("-sep <sep1> <sep2>", 30)+" program assumens pair of files have name unique<sep[1|2]><suffix> (-sep 1 2)");
			System.out.println(Functions.fixedLength("", 30)+"e.g  file.1.fastq file.2.fastq");

			System.out.println("To start any program you have to specify the -program <program> flag. Available programs are listed below.");

			for (Programs info : EnumSet.allOf(Programs.class)) {
				System.out.println(info);
			}

			System.out.println();

		}

	}

