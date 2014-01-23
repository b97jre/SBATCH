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

public class GeneralDir {

	String time;
	String projectDir;
	String inDir;
	String outDir;
	String suffix;

	public GeneralDir() {

	}

	public static void main(String[] args) {

		int length = args.length;
		for (int i = 0; i < length; i++) {
			args[i] = args[i].trim();
			System.out.print(args[i] + " ");
		}
		System.out.println();
		Hashtable<String, String> T = Functions.parseCommandLine(args);
		GeneralDir general = new GeneralDir();

		SBATCHinfo sbatch = new SBATCHinfo();
		sbatch.addSBATCHinfo(T);
		//general.run(T,sbatch);
	}



	public enum Programs {

		GATK,
		HELP
	}	
 


	public boolean addParameters(Hashtable<String, String> T,SBATCHinfo sbatch) {

		boolean allPresent = true;


		if (T.containsKey("-i")){
			inDir = Functions.getValue(T, "-i");
			//System.out.println(inDir);
			inDir = new File(inDir).getAbsolutePath();
			//System.out.println(inDir);
			if(!IOTools.isDir(inDir)) return false;
		}
		else {
			help(T);
			return false;
		}
		
		projectDir = Functions.getValue(T, "-pDir", IOTools.getCurrentPath());
		outDir = Functions.getValue(T, "-o", inDir);
		outDir = new File(outDir).getAbsolutePath();

		if (T.containsKey("-suffix"))
			suffix = Functions.getValue(T, "-suffix");
		else {
			allPresent = false;
		}
		
		String programName = Functions.getValue(T, "-program",Functions.getValue(T, "-p","help"));
		Programs program = Programs.valueOf(programName.toUpperCase());

		switch (program) {
		case GATK:
			GATK gatk = new GATK();
			if(!gatk.addParameters(T)) return false;
			if(gatk.phase1){
				GATKphase1 phase1 = new GATKphase1();
				if(!phase1.checkParameters(T)) return false;
				
			}
			break;
		default: help(T);	
		}
		
		if (allPresent) return true;

		System.out.println("\n\nAborting run because of missing arguments.");
		help(T);
		return false;
		
	}

	public void generalStart(Hashtable<String, String> T, SBATCHinfo sbatch) {
		try {
			if (!IOTools.isDir(projectDir + "/scripts"))
				IOTools.mkDir(projectDir + "/scripts");

			System.out.println(projectDir);
			String program = Functions.getValue(T, "-program", "");

			ExtendedWriter EW = new ExtendedWriter(new FileWriter(projectDir
					+ "/scripts/" + sbatch.getTimeStamp() + "_start"+program+"_SBATCHScript.sh"));


			generalDir(T, EW, sbatch,  inDir, outDir);

			EW.flush();
			EW.close();
			System.out.println("All things seems to have run properly!");
			System.out.println("To start all the scripts write ");
			System.out.println("sh "+projectDir
					+ "/scripts/" +  sbatch.getTimeStamp() + "_start"+program+"_SBATCHScript.sh");
			
			

		} catch (Exception E) {
			E.printStackTrace();
		}
	}

	public void generalDir(Hashtable<String, String> T,
			ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
			 String inDir, String outDir) {

		ArrayList<String> fileNames = IOTools.getSequenceFiles(inDir, suffix);

		if (fileNames == null) {
			System.out.println("Something is problematic with this folder :");
			System.out.println(inDir);
			System.out.println();
		}else if (fileNames.isEmpty()) {
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
				generalFiles(T, generalSbatchScript, sbatch, 
						inDir, outDir, fileNames);
			}
			catch (Exception E) {
				E.printStackTrace();
			}
		}
		ArrayList<String> subDirs = IOTools.getDirectories(inDir);
		for (int i = 0; i < subDirs.size(); i++) {
			generalDir(T, generalSbatchScript, sbatch, inDir + "/"
					+ subDirs.get(i), outDir + "/" + subDirs.get(i));
		}
	}

	public void generalFiles(Hashtable<String, String> T,
			ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,String inDir, String outDir, ArrayList<String> fileNames) {

		String programName = Functions.getValue(T, "-program",Functions.getValue(T, "-p","help"));
		Programs program = Programs.valueOf(programName.toUpperCase());

		switch (program) {
		case GATK:
			GATK gatk = new GATK();
			gatk.addParameters(T);
			if(gatk.phase1){
				GATKphase1 phase1 = new GATKphase1();
				if(phase1.addParameters(T))
					phase1.GATKDirPhase1(T, generalSbatchScript, sbatch,inDir,fileNames);
				
			}
			break;
		default: help(T);	

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
		case GATK:
			GATK.help();
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

