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

public class GeneralOneFile {

	String time;
	String projectDir;
	String inDir;
	String outDir;
	String suffix;

	public GeneralOneFile() {

	}

	public static void main(String[] args) {

		int length = args.length;
		for (int i = 0; i < length; i++) {
			args[i] = args[i].trim();
			System.out.print(args[i] + " ");
		}
		System.out.println();
		Hashtable<String, String> T = Functions.parseCommandLine(args);
		GeneralOneFile general = new GeneralOneFile();

		SBATCHinfo sbatch = new SBATCHinfo();
		sbatch.addSBATCHinfo(T);
		general.run(T,sbatch);
	}



	public enum Programs {

		BLAST,
		BLAT,
		PANTHER,
		PFAM,
		TRIMFASTQFILES,
		SAMTOOLS,
		GATK,
		HELP
	}	



	public void run(Hashtable<String, String> T,SBATCHinfo sbatch) {

		boolean allPresent = true;


		if (T.containsKey("-i")){
			inDir = Functions.getValue(T, "-i");
			//System.out.println(inDir);
			inDir = new File(inDir).getAbsolutePath();
			//System.out.println(inDir);
		}
		else {
			help(T);
			return;

		}
		projectDir = Functions.getValue(T, "-pDir", IOTools.getCurrentPath());
		outDir = Functions.getValue(T, "-o", inDir);
		outDir = new File(outDir).getAbsolutePath();

		if (T.containsKey("-suffix"))
			suffix = Functions.getValue(T, "-suffix");
		else if(IOTools.isDir(inDir)){
			allPresent = false;
		}
		if(!checkParameters(T)) return;

		if (allPresent)
			generalStart(T, sbatch, sbatch.timeStamp);
		else{
			System.out.println("\n\nAborting run because of missing arguments.");
			help(T);
		}
	}

	public void generalStart(Hashtable<String, String> T, SBATCHinfo sbatch,
			String timeStamp) {
		try {
			if (!IOTools.isDir(projectDir + "/scripts"))
				IOTools.mkDir(projectDir + "/scripts");


			String program = Functions.getValue(T, "-program", "");

			ExtendedWriter EW = new ExtendedWriter(new FileWriter(projectDir
					+ "/scripts/" + timeStamp + "_start"+program+"SBATCHScript.sh"));


			if (IOTools.isDir(inDir)){
				generalDir(T, EW, sbatch, timeStamp, inDir, outDir);
			}
			else if (IOTools.fileExists(inDir)) {
				File file = new File(inDir);
				String folder = file.getParent();
				String fileName = file.getName();
				System.out.println("folder: " + folder);
				System.out.println("fileName: " + fileName);

				generalFileInitial(T, EW, sbatch, timeStamp, folder, folder,
						fileName);
			}

			EW.flush();
			EW.close();
			System.out.println("To start all the scripts write ");
			System.out.println(projectDir + "/scripts/" + timeStamp
					+ "_startSBATCHScript.sh");

		} catch (Exception E) {
			E.printStackTrace();
		}
	}

	public void generalFileInitial(Hashtable<String, String> T,
			ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
			String timestamp, String inDir, String outDir, String inFile) {
		if (!IOTools.isDir(outDir))
			IOTools.mkDir(outDir);
		if (!IOTools.isDir(outDir + "/reports"))
			IOTools.mkDir(outDir + "/reports");
		if (!IOTools.isDir(outDir + "/scripts"))
			IOTools.mkDir(outDir + "/scripts");

		generalFile(T, generalSbatchScript, sbatch, timestamp, inDir, outDir,
				inFile);
	}

	public void generalDir(Hashtable<String, String> T,
			ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
			String timestamp, String inDir, String outDir) {

		ArrayList<String> fileNames = IOTools.getSequenceFiles(inDir, suffix);

		if (fileNames.isEmpty()) {
			if (!IOTools.isDir(outDir))
				IOTools.mkDir(outDir);
		} else {
			if (!IOTools.isDir(outDir))
				IOTools.mkDir(outDir);
			if (!IOTools.isDir(outDir + "/reports"))
				IOTools.mkDir(outDir + "/reports");
			if (!IOTools.isDir(outDir + "/scripts"))
				IOTools.mkDir(outDir + "/scripts");
			try {
				for (int i = 0; i < fileNames.size(); i++) {
					generalFile(T, generalSbatchScript, sbatch, timestamp,
							inDir, outDir, fileNames.get(i));
				}
			} catch (Exception E) {
				E.printStackTrace();
			}
		}
		ArrayList<String> subDirs = IOTools.getDirectories(inDir);
		for (int i = 0; i < subDirs.size(); i++) {
			generalDir(T, generalSbatchScript, sbatch, timestamp, inDir + "/"
					+ subDirs.get(i), outDir + "/" + subDirs.get(i));
		}
	}



	private ExtendedWriter printSBATCHinfoSTART(SBATCHinfo sbatch,
			ExtendedWriter generalSbatchScript, String inDir, String forward, String program) {
		try {
			if (!IOTools.isDir(inDir + "/reports"))
				IOTools.mkDir(inDir + "/reports");
			if (!IOTools.isDir(inDir + "/scripts"))
				IOTools.mkDir(inDir + "/scripts");

			String fileWithoutSuffix = Functions.getFileWithoutSuffix(forward,this.suffix);
			String sbatchFile = inDir + "/scripts/" + sbatch.timeStamp
					+ "_" +fileWithoutSuffix+ "_"+ program+".sbatch";
			generalSbatchScript.println("sbatch " + sbatchFile);
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(
					sbatchFile));

			sbatch.printSBATCHinfo(EW, inDir, sbatch.timeStamp, fileWithoutSuffix, program);

			return EW;
		}
		catch(Exception E){
			E.printStackTrace();
		}
			return null;
	}

	private void printSBATCHinfoEND(
			ExtendedWriter EW, String program) {
		try {
			EW.println("echo "+program+" finished running");
			EW.flush();
			EW.close();
		}
		catch(Exception E){
			E.printStackTrace();
		}
	}
	
	

		public void generalFile(Hashtable<String, String> T,
				ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
				String timestamp, String inDir, String outDir, String fileName) {

			String programName = Functions.getValue(T, "-program",Functions.getValue(T, "-p","help"));
			Programs program = Programs.valueOf(programName.toUpperCase());
			try{
				ExtendedWriter EW = null;
			switch (program) {
			case BLAST:
				Blast blast = new Blast(T); 
				blast.run(T, generalSbatchScript, sbatch, timestamp, inDir, outDir,
						fileName,suffix);
				break;
			case BLAT:
				System.out.println("Not yet implemented");
				break;
			case PANTHER:
				System.out.println("Not yet implemented");
				break;
			case PFAM:
				PFAM.run(T, generalSbatchScript, sbatch, timestamp, inDir, outDir,
						fileName, suffix);
				break;
			case SAMTOOLS:
				EW = printSBATCHinfoSTART(sbatch,generalSbatchScript,inDir,fileName, "samtools");
				Samtools st = new Samtools();
				st.setParameters(T);
				st.samtoolsFile(EW, sbatch, fileName, inDir, suffix);
				printSBATCHinfoEND(EW,"samtools");
				break;
			case TRIMFASTQFILES:
				EW = printSBATCHinfoSTART(sbatch,generalSbatchScript,inDir,fileName, "TrimFastqFiles");
				TrimFastqFiles TFF = new TrimFastqFiles();
				if(TFF.addParameters(T))
					TFF.trimFastQFile(EW,sbatch, generalSbatchScript, inDir, fileName);
				printSBATCHinfoEND(EW,"TrimFastqFiles");
				break;
			case GATK:
				EW = printSBATCHinfoSTART(sbatch,generalSbatchScript,inDir,fileName, "GATKgenotype");
				GATKgenotype GATKgt = new GATKgenotype();
				if(GATKgt.addParameters(T))
					GATKgt.GATKGenotypeSample(EW,sbatch, inDir, fileName);
				printSBATCHinfoEND(EW,"TrimFastqFiles");
				break;
			default: help(T);	

			}	
			}catch(Exception E){
				E.printStackTrace();
			}

		}



		public boolean checkParameters(Hashtable<String, String> T) {

			String programName = Functions.getValue(T, "-program",Functions.getValue(T, "-p","help"));
			Programs program = Programs.valueOf(programName.toUpperCase());

			switch (program) {
			case BLAST:
				System.out.println("Not yet implemented");
				return true;
			case BLAT:
				System.out.println("Not yet implemented");
				return false;
			case PANTHER:
				System.out.println("Not yet implemented");
				return false;
			case PFAM:
				System.out.println("Not yet implemented");
				return true;
			case SAMTOOLS:
				Samtools st = new Samtools();
				return(st.checkParameters(T));
			case TRIMFASTQFILES:
				TrimFastqFiles TFF = new TrimFastqFiles();
				return TFF.addParameters(T);
			case GATK:
				GATKgenotype GATKgt = new GATKgenotype();
				return GATKgt.checkParameters(T);
			default: help(T);
			return false;

			}			

		}


		public void help(Hashtable<String, String> T){

			System.out.println("Required flags:");
			System.out.println(Functions.fixedLength("-program <program>",30)+"Available programs are listed below.(-program blast)");
			System.out.println(Functions.fixedLength("-i <file|dir>", 30)+"InDirectory or inFile (-i fastaDir)");
			System.out.println();
			System.out.println("Required values if working on directory:");
			System.out.println(Functions.fixedLength("-suffix <yourSuffix>", 30)+" suffix of files (-suffix fa)");

			System.out.println();
			System.out.println();
			System.out.println("Other flags:");
			System.out.println(Functions.fixedLength("-o <dir>", 30)+" if you want the files to end up in another folder system");
			System.out.println();
			System.out.println();
			System.out.println("Program specific flags");

			String programName = Functions.getValue(T, "-program",Functions.getValue(T, "-p","help"));
			Programs program = Programs.valueOf(programName.toUpperCase());

			switch (program) {
			case BLAST:
				Blast.help();
				break;
			case BLAT:
				System.out.println("Blat help not yet implemented see source code for proper flags");
				break;
			case PANTHER:
				System.out.println("Panther help not yet implemented see source code for proper flags");
				break;
			case PFAM:
				PFAM.help();
				break;
			case TRIMFASTQFILES:
				PFAM.help();
				break;
			default: 
				System.out.println("This program is not available as a single file program. Available programs are");
				for (Programs info : EnumSet.allOf(Programs.class)) {
					System.out.println(info);
				}
				break;

			}
		}

	}

