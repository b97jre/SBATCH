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
		BOWTIE2,
		BWA,
		TRIMFASTQFILES,
		CUTADAPT,
		HELP
	}	



	public void run(Hashtable<String, String> T,SBATCHinfo sbatch ) {

		boolean allPresent = true;
		String programName = Functions.getValue(T, "-program",Functions.getValue(T, "-p","help"));


		if (!T.containsKey("-i")){
			if(!T.containsKey("-f1") &&!T.containsKey("-f2")){
				help();
				return;
			}
		}
		
		allPresent = testProgramParamters(T,sbatch);

		inDir = Functions.getValue(T, "-i", null);
		inDir = new File(inDir).getAbsolutePath();
		forward = Functions.getValue(T, "-f1", null);
		reverse = Functions.getValue(T, "-f2", null);
		outDir = Functions.getValue(T, "-o", inDir);
		outDir = new File(outDir).getAbsolutePath();
		
		projectDir = Functions.getValue(T, "-pDir", IOTools.getCurrentPath());
		suffix = Functions.getValue(T, "-suffix", "fastq");
		String seperator = Functions.getValue(T, "-sep", "1." + suffix + " 2."+ suffix);
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
			String program = Functions.getValue(T, "-program", "");
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(projectDir
					+ "/scripts/" + sbatch.timeStamp + "_start_"+program+"_SBATCHScript.sh"));
			if (this.inDir != null){				
				generalDir(T, EW, sbatch,  inDir, outDir);
			}

			else if (this.forward != null && this.reverse != null) {
				String folder = this.projectDir;
				generalFileInitial(T, EW, sbatch,  folder, folder,
						this.forward, this.reverse);
			}
			EW.flush();
			EW.close();
			
			System.out.println("All things seems to have run properly!");
			System.out.println("To start all the scripts write ");
			System.out.println("sh "+projectDir + "/scripts/" + sbatch.timeStamp	
					+ "_start_"+program+"_SBATCHScript.sh");

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

		System.out.println("Finding files with suffix "+suffix+" in folder "+inDir );

		ArrayList<String> fileNames = IOTools.getSequenceFiles(inDir, suffix);
		if(fileNames == null){
			System.out.println("Something very wrong with this file path");
			System.out.println(inDir);
			return;
			
		}
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
					star.STARFile(generalSbatchScript, sbatch, new File(inDir).getAbsolutePath(), outDir,
							fileName1,fileName2);
				}
				break;
			case TRIMFASTQFILES:
				TrimFastqFiles TFF = new TrimFastqFiles();
				if(TFF.addParameters(T)){
					TFF.trimFastQFiles(sbatch, generalSbatchScript, inDir, fileName1, fileName2);
				}
				break;
				
			case CUTADAPT:
				CutAdapt ca = new CutAdapt();
				ca.addParameters(T);
				ca.trimFastQFiles(sbatch, generalSbatchScript, inDir, fileName1, fileName2);
				break;
			case BOWTIE2:
				Bowtie2 bt2 = new Bowtie2();
				bt2.addParameters(T);
				bt2.Bowtie2File(generalSbatchScript, sbatch, new File(inDir).getAbsolutePath(), outDir,
						fileName1,fileName2);
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


		public boolean testProgramParamters(Hashtable<String, String> T,
				SBATCHinfo sbatch) {

			String programName = Functions.getValue(T, "-program",Functions.getValue(T, "-p","help"));
			Programs program = Programs.valueOf(programName.toUpperCase());

			switch (program) {
			case STAR:
				STAR star = new STAR();
				return star.addParameters(T,sbatch);
			case TRIMFASTQFILES:
				TrimFastqFiles TFF = new TrimFastqFiles();
				return TFF.addParameters(T);
			case BWA:
				BWA bwa = new BWA();
				return bwa.checkParameters(T);
			case CUTADAPT:
				CutAdapt ca = new CutAdapt();
				return ca.checkParameters(T);
			case BOWTIE2:
				Bowtie2 bt2 = new Bowtie2();
				return bt2.addParameters(T);
			case TOPHAT:
				System.out.println("Not yet implemented");
				return true;
			case TOPHAT2:
				System.out.println("Not yet implemented");
				return true;
			default: 
				System.out.println(programName+" is not yet implemented");
				return false;	

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


			for (Programs info : EnumSet.allOf(Programs.class)) {
				System.out.println(info);
			}

			System.out.println();

		}

	}

