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
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.Hashtable;

import SBATCHscripts.GeneralOneFile.Programs;

public class Merge {

	String time;
	String inDir;
	String outDir;
	String suffix;
	String prefix;
	ArrayList<String> fileNames;
	String type ;
	String outFile;


	public Merge() {

	}

	public enum Programs {

		BLAST,
		INTERPROSCAN,
		HMMER,
		BLAT,
		PANTHER,
		PFAM,
		VCF,
		GATK,
		HELP
	}	

	public void run(Hashtable<String, String> T,SBATCHinfo sbatch) {

		if (T.containsKey("-fileNames"))
			fileNames = new ArrayList<String>(Arrays.asList(Functions.getValue(T, "-fileNames", ".").split(" ")));
		else {
			fileNames = null;
		}
		if (T.containsKey("-tmpFiles"))
			fileNames = new ArrayList<String>(Arrays.asList(Functions.getValue(T, "-tmpFiles", ".").split(",")));
		
		outFile = Functions.getValue(T, "-outFile");
		inDir = Functions.getValue(T, "-i", Functions.getValue(T, "-inDir", IOTools.getCurrentPath()));		
		outDir = Functions.getValue(T, "-o", Functions.getValue(T, "-outDir",inDir));
		prefix = Functions.getValue(T, "-prefix", null);
		suffix = Functions.getValue(T, "-suffix", null);
		if(T.containsKey("-type")){
			this.type = Functions.getValue(T,"-type","HELP");
		}
		else if (T.containsKey("-GATK") || T.containsKey("-gatk") ||T.containsKey("-vcf")){
			this.type = "GATK";
		} 
		else if (T.containsKey("-PFAM") || T.containsKey("-PFAM")){
			this.type = "PFAM";
		} 
		
		

		if (fileNames != null)
			generalStart(T, sbatch);
		else if(suffix != null || prefix != null)
			generalStart(T, sbatch);
		else
			System.out
			.println("\n\nAborting run because of missing arguments.");
	}

	public void generalStart(Hashtable<String, String> T,SBATCHinfo sbatch) {
		if (!sbatch.interactive){
			if (!IOTools.isDir(inDir + "/scripts"))
				IOTools.mkDir(inDir + "/scripts");

			if(this.fileNames == null){				
				try {
					ExtendedWriter EW = new ExtendedWriter(new FileWriter(
							inDir + "/scripts/" + sbatch.getTimeStamp()
							+ "_startSBATCHScript.sh"));

					generalDir(EW, sbatch, inDir,outDir);

					EW.flush();
					EW.close();

					System.out.println("All the sbatchscriptfiles locations have been written to :");
					System.out.println(inDir + "/scripts/" + sbatch.getTimeStamp()+ "_startSBATCHScript.sh");
					sbatch.startSbatchScripts(inDir + "/scripts/" + sbatch.getTimeStamp()+ "_startSBATCHScript.sh");

				} catch (Exception E) {
					E.printStackTrace();
				}
			}else{
				String sbatchFile = Merge.getSBATCHscript(sbatch, inDir, outDir, fileNames, type, 0);
				System.out.println("The sbatchScript has been written to  :");
				System.out.println(sbatchFile);
				
			}

		} else {
			ExtendedWriter EW = null;
			if(this.fileNames == null)
				generalDir(EW, sbatch, inDir,outDir);
			else generalFilesInteractive(T, inDir,outDir,fileNames,outFile);

		}

	}

	public void generalDir(
			ExtendedWriter generalSbatchScript, SBATCHinfo sbatch, String inDir, String outDir) {

		ArrayList<String> fileNamesSuffix = IOTools.getFilesSuffix(inDir,
				suffix);
		ArrayList<String> fileNamesPrefix = IOTools.getFilesPrefix(inDir,
				prefix);
		ArrayList<String> fileNames = (ArrayList<String>) IOTools.intersection(
				fileNamesSuffix, fileNamesPrefix);

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
				
				generalSbatchScript.println(Merge.getSBATCHscript(sbatch,  inDir,outDir,fileNames,type,0));
			} catch (Exception E) {
				E.printStackTrace();
			}
		}
		ArrayList<String> subDirs = IOTools.getDirectories(inDir);
		for (int i = 0; i < subDirs.size(); i++) {
			generalDir(generalSbatchScript, sbatch,inDir + "/"
					+ subDirs.get(i), outDir + "/" + subDirs.get(i));
		}
	}

	public static String getSBATCHscript(SBATCHinfo sbatch,
			String inDir, String outDir,ArrayList<String> fileNames,String type, int count) {
		try {
			String sbatchFileName = sbatch.getSbatchFileName("Merge", inDir,count);
			ExtendedWriter EW = sbatch.printSBATCHInfoSTART("Merge", inDir,count);
			EW.println();
			sbatch.printInteractiveStart(EW,"Merge");
			EW.print(" -i "+inDir+" -o "+outDir+" -fileNames");
			for(int i = 0; i< fileNames.size();i++){
				EW.print(" "+fileNames.get(i));
			}
			if(type != null){
				System.out.println("type: " +type);
				if (type.toUpperCase().equals("PFAM"))
					EW.print(" -PFAM");
				else if (type.toUpperCase().equals("GATK"))
					EW.print(" -GATK");

			}
			EW.println();
			EW.println();
			EW.flush();
			EW.close();
			return sbatchFileName;

		} catch (Exception E) {
			E.printStackTrace();
		}
		return null;
	}


	public void generalDirInteractive(Hashtable<String,String> T, 
			String inDir, String outDir) {

		ArrayList<String> fileNamesSuffix = IOTools.getFilesSuffix(inDir,
				suffix);
		ArrayList<String> fileNamesPrefix = IOTools.getFilesPrefix(inDir,
				prefix);
		ArrayList<String> fileNames = (ArrayList<String>) IOTools.intersection(
				fileNamesSuffix, fileNamesPrefix);

		if (!fileNames.isEmpty()) {
			if (!IOTools.isDir(outDir))
				IOTools.mkDir(outDir);
			generalFilesInteractive(T,inDir,outDir,fileNames,null);
		}
		ArrayList<String> subDirs = IOTools.getDirectories(inDir);
		for (int i = 0; i < subDirs.size(); i++) {
			generalDirInteractive(T,inDir + "/"+ subDirs.get(i), outDir + "/" + subDirs.get(i));
		}
	}	

	public void generalFilesInteractive(Hashtable<String,String> T, 
			String inDir, String outDir,ArrayList<String> fileNames, String outFile) {
		System.out.println("Merging "+fileNames.size()+" files into one");
		try {
			Programs program = Programs.valueOf(type.toUpperCase());
			switch (program) {
			case BLAST:
				Blast blast = new Blast();
				blast.addMergeInfo(T);
				blast.merge(inDir, outDir, this.fileNames, outFile);
				break;
			case INTERPROSCAN:
				Interproscan IPS = new Interproscan();
				IPS.merge(inDir, outDir, this.fileNames, outFile);
				break;
			case HMMER:
				HMMER.merge(inDir, outDir, this.fileNames, outFile);
				break;
					
			case BLAT:
				System.out.println("Not yet implemented");
				break;
			case PANTHER:
				System.out.println("Not yet implemented");
				break;
			case PFAM:
				PFAM.merge(fileNames, outDir,
						IOTools.longestCommonPrefix(fileNames)
						+ "merged.pfam");
				break;
			case GATK:
				GATK.merge(fileNames, outDir,
						IOTools.removeLastDot(IOTools.longestCommonPrefix(fileNames))
						+ "."+general.IOTools.removeFirstDot(IOTools.longestCommonSuffix(fileNames)));
				break;
			case VCF:
				GATK.merge(fileNames, outDir,
						IOTools.removeLastDot(IOTools.longestCommonPrefix(fileNames))
						+ "."+general.IOTools.removeFirstDot(IOTools.longestCommonSuffix(fileNames)));
				break;
			default: help();	
			
			}
		}catch (Exception E) {
			E.printStackTrace();
		}
	}
	
	
	public void help(){

		System.out.println("Required flags:");
		System.out.println(Functions.fixedLength("-type <program>",30)+"Available programs are listed below.(-type blast)");
		System.out.println(Functions.fixedLength("-i <dir>", 30)+"InDirectory or inFile (-i fastaDir)");
		System.out.println(Functions.fixedLength("or", 30));
		System.out.println(Functions.fixedLength("-inDir <dir>", 30)+"InDirectory or inFile (-inDir fastaDir)");
		System.out.println();
		System.out.println("Required values if working on directory:");
		System.out.println(Functions.fixedLength("-suffix <yourSuffix>", 30)+" suffix of files (-suffix fa)");

		System.out.println();
		System.out.println("Required values if working on known files:");
		System.out.println(Functions.fixedLength("-suffix <yourSuffix>", 30)+" suffix of files (-suffix fa)");
		
		System.out.println();
		System.out.println();
		System.out.println("Other flags:");
		System.out.println(Functions.fixedLength("-o <dir>", 30)+" if you want the files to end up in another folder system");
		System.out.println();
		System.out.println();
		System.out.println("Program specific flags");

		Programs program = Programs.valueOf(type.toUpperCase());

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
		default: 
			System.out.println("This program is not available as a single file program. Available programs are");
			for (Programs info : EnumSet.allOf(Programs.class)) {
				System.out.println(info);
			}
			break;

		}
	}



}
