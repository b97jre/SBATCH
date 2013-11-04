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
		HELP
	}	



	public void run(Hashtable<String, String> T,SBATCHinfo sbatch) {

		boolean allPresent = true;

		String timeStamp = Functions.getDateTime();
		if (!sbatch.addSBATCHinfo(T))
			allPresent = false;

		if (T.containsKey("-i"))
			inDir = Functions.getValue(T, "-i", ".");
		else {
			allPresent = false;
		}
		projectDir = Functions.getValue(T, "-pDir", IOTools.getCurrentPath());
		outDir = Functions.getValue(T, "-o", inDir);
		
		
		if (T.containsKey("-suffix"))
			suffix = Functions.getValue(T, "-suffix");
		else if(IOTools.isDir(inDir)){
			allPresent = false;
		}
		

		if (allPresent)
			generalStart(T, sbatch, timeStamp);
		else
			System.out
			.println("\n\nAborting run because of missing arguments.");
	}

	public void generalStart(Hashtable<String, String> T, SBATCHinfo sbatch,
			String timeStamp) {
		try {
			if (!IOTools.isDir(projectDir + "/scripts"))
				IOTools.mkDir(projectDir + "/scripts");
			
			
			
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(projectDir
					+ "/scripts/" + timeStamp + "_startSBATCHScript.sh"));
			
			
			if (IOTools.isDir(projectDir + "/" + inDir)){
				generalDir(T, EW, sbatch, timeStamp, inDir, projectDir + "/"
						+ outDir);
			}
			else if (IOTools.fileExists(inDir)) {
				File file = new File(projectDir + "/" + inDir);
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

	public void generalFile(Hashtable<String, String> T,
			ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
			String timestamp, String inDir, String outDir, String fileName) {

		String programName = Functions.getValue(T, "-program",Functions.getValue(T, "-p","help"));
		Programs program = Programs.valueOf(programName.toUpperCase());

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
		default: help();	

		}			

	}


	public void help(){

		System.out.println("Mandatory values:");
		System.out.println(Functions.fixedLength("-i <file|dir>", 30)+"inDirectory or inFile (-i fastaDir)");
		System.out.println();
		System.out.println("Mandatory values if working on directory:");
		System.out.println(Functions.fixedLength("-suffix <yourSuffix>", 30)+" suffix of files (-suffix fa)");

		System.out.println();
		System.out.println();
		System.out.println("Mandatory values:");
		System.out.println(Functions.fixedLength("-o <dir>", 30)+" if you want the files to end up in another folder system");
		System.out.println();
		System.out.println();
		
		System.out.println("To start any program you have to specify the -program <program> flag. Available programs are listed below.");

		for (Programs info : EnumSet.allOf(Programs.class)) {
			System.out.println(info);
		}

		System.out.println();

	}

}

