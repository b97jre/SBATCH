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
import java.util.Hashtable;

public class GeneralOneFile {

	String time;
	String projectDir;
	String inDir;
	String outDir;
	String suffix;

	public GeneralOneFile(){

	}

	public static void main(String []args){

		int length = args.length;
		for (int i = 0; i < length; i++){
			args[i] = args[i].trim();
			System.out.print(args[i]+" ");
		}
		System.out.println();
		Hashtable<String,String> T = Functions.parseCommandLine(args);
		GeneralOneFile general = new GeneralOneFile();
		general.run(T);
	}

	public void run(Hashtable<String,String> T){

		boolean allPresent = true;

		String timeStamp = Functions.getDateTime();
		SBATCHinfo sbatch = new SBATCHinfo();
		if(!sbatch.addSBATCHinfo(T)) allPresent = false;

		if(T.containsKey("-i"))
			inDir= Functions.getValue(T, "-i", ".");
		else{
			System.out.println("must contain inDirectory -i");
			allPresent = false;
		}

		projectDir= Functions.getValue(T, "-pDir", IOTools.getCurrentPath());
		outDir= Functions.getValue(T, "-o", inDir);

		if(T.containsKey("-time"))
			time = Functions.getValue(T, "-time", ".");
		else{
			System.out.println("must contain likely time -time");
			allPresent = false;
		}

		if(T.containsKey("-suffix"))
			suffix = Functions.getValue(T,"-suffix","sam");
		else{
			System.out.println("must contain likely time -suffix");
			allPresent = false;
		}


		if(allPresent)
			generalStart(T,sbatch, timeStamp);
		else
			System.out.println("\n\nAborting run because of missing arguments for cufflinks.");
	}


	public void generalStart(Hashtable<String,String> T,SBATCHinfo sbatch, String timeStamp){
		try{
			if(!IOTools.isDir(projectDir+"/scripts"))
				IOTools.mkDir(projectDir+"/scripts");
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(projectDir+"/scripts/"+timeStamp+"_startSBATCHScript.sh"));
			if(IOTools.isDir(projectDir+"/"+inDir))
				generalDir(T, EW,sbatch, timeStamp, inDir, projectDir+"/"+outDir);
			else if(IOTools.fileExists(inDir)){
				File file = new File(projectDir+"/"+inDir);
				String folder = file.getParent();
				String fileName = file.getName();
				System.out.println("folder: "+folder);
				System.out.println("fileName: "+fileName);
				
				generalFileInitial(T, EW,sbatch, timeStamp, folder, folder, fileName);
			}
				
			EW.flush();
			EW.close();
			System.out.println("To start all the scripts write ");
			System.out.println(projectDir+"/scripts/"+timeStamp+"_startSBATCHScript.sh");

		}catch(Exception E){E.printStackTrace();}
	} 


	public void generalFileInitial(Hashtable<String,String> T, ExtendedWriter generalSbatchScript, SBATCHinfo sbatch ,String timestamp,String inDir, String outDir, String inFile){
			if(!IOTools.isDir(outDir))
				IOTools.mkDir(outDir);
			if(!IOTools.isDir(outDir+"/reports"))
				IOTools.mkDir(outDir+"/reports");
			if(!IOTools.isDir(outDir+"/scripts"))
				IOTools.mkDir(outDir+"/scripts");
				
			generalFile(T, generalSbatchScript, sbatch ,timestamp,inDir, outDir, inFile,suffix,time);
	}
	
	
	public void generalDir(Hashtable<String,String> T, ExtendedWriter generalSbatchScript, SBATCHinfo sbatch ,String timestamp,String inDir, String outDir){


		ArrayList <String> fileNames = IOTools.getSequenceFiles(inDir,suffix);

		if(fileNames.isEmpty()){
			if(!IOTools.isDir(outDir))
				IOTools.mkDir(outDir);
		}
		else{
			if(!IOTools.isDir(outDir))
				IOTools.mkDir(outDir);
			if(!IOTools.isDir(outDir+"/reports"))
				IOTools.mkDir(outDir+"/reports");
			if(!IOTools.isDir(outDir+"/scripts"))
				IOTools.mkDir(outDir+"/scripts");
			try{

				for(int i = 0; i < fileNames.size(); i++){	
					generalFile(T, generalSbatchScript, sbatch ,timestamp,inDir, outDir, fileNames.get(i),suffix,time);
				}
			}catch(Exception E){E.printStackTrace();}
		}
		ArrayList <String> subDirs = IOTools.getDirectories(inDir);
		for(int i = 0; i < subDirs.size(); i++){
			generalDir(T, generalSbatchScript,sbatch,timestamp,inDir+"/"+subDirs.get(i),outDir+"/"+subDirs.get(i));
		}
	}

	public static void generalFile(Hashtable<String,String> T, ExtendedWriter generalSbatchScript, SBATCHinfo sbatch ,String timestamp,String inDir
			, String outDir,String fileName, String suffix, String time){
		if(T.containsKey("-blast"))
			Blast.run(T, generalSbatchScript, sbatch, timestamp, inDir, outDir, fileName, suffix, time);
		if(T.containsKey("-PFAM"))
			PFAM.run(T,generalSbatchScript, sbatch, timestamp, inDir, outDir,fileName,suffix,time);
		if(T.containsKey("-Panther"))
			inDir= Functions.getValue(T, "-i", ".");

	}



}

