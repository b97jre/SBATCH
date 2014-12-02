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

	
	
	
	public static boolean checkParameters(Hashtable<String, String> T){
		boolean allPresent = true;

		if (T.containsKey("-refDir")) {
			String referenceDir = Functions.getValue(T, "-refDir", ".");
			File test = new File(referenceDir);
			if(test.isDirectory())
				referenceDir = test.getAbsolutePath();
			else{
				allPresent = false;
			}
				
		} else {
			allPresent = false;
		}
		if (!T.containsKey("-o")) {
			allPresent = false;
		}
		if (!T.containsKey("-suffix"))
			allPresent = false;
		
		if (!T.containsKey("-parameterFile")) {
			allPresent = false;
		}
		if(allPresent) return true;
		help();
		return false;
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
		if (!T.containsKey("-o")) {
			allPresent = false;
		}
		
		

		this.sam2bam = true;

		this.nrOfThreads = sbatch.getNrOfCores();
		this.suffix = Functions.getValue(T, "-suffix", "fastq.gz");

		if (T.containsKey("-parameterFile")) {
			this.parameterFile = Functions.getValue(T, "-parameterFile");
		}else{
			allPresent = false;
		}
		if(allPresent) return true;
		help();
		return false;
	}
	
	

	public void STARFile(ExtendedWriter generalSbatchScript, SBATCHinfo sbatch, String inDir, String outDir, String forward, String reverse) {

		String commonName = forward;
		if(reverse!= null)
			commonName = Functions.getCommonPrefix(forward, reverse);

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
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(sbatchFileName));
			sbatch.printSBATCHinfo(EW, outDir, sbatch.timeStamp, commonName, "STAR");
			
			
			
			EW.println();
			EW.println("cd " + outDir);
			if(reverse != null){
				STARCommand(EW, inDir + "/" + forward,
						inDir + "/" + reverse);
			}else{
				STARCommand(EW, inDir + "/" + forward,null);
			}
			if (sam2bam) {
				Samtools.sam2bam(EW, outDir + "/Aligned.out.sam", -1, -1, -1, 
						true, true, true, true, true);
			}


			EW.flush();
			EW.close();

		} catch (Exception E) {
			E.printStackTrace();
		}
	}


	
	public void STARFile(ExtendedWriter EW, String inDir, String outDir, String forward, String reverse) {

		String commonName = forward;
		if(reverse!= null)
			commonName = Functions.getCommonPrefix(forward, reverse);

		File refDir = new File(referenceDir);
		String refName = refDir.getName();
		
		outDir = outDir+"/"+commonName+"_"+refName;
		if(!IOTools.isDir(outDir))
			IOTools.mkDir(outDir);
		
		EW.println();
		EW.println("cd " + outDir);
			
		if(reverse != null){
				STARCommand(EW, inDir + "/" + forward,
						inDir + "/" + reverse);
		}else{
			STARCommand(EW, inDir + "/" + forward,null);
		}
		if (sam2bam) {
			Samtools.sam2bam(EW, outDir + "/Aligned.out.sam", -1, -1, -1,
					true, true, true, true, true);
		}
	}
	
	
	public void STARCommand(ExtendedWriter EW,
			 String inFile1, String inFile2) {

		String STARcommand = "/sw/apps/bioinfo/star/2.3.1o/milou/bin/STAR ";
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
				else
					ER.skipLine();
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

	
	public static void help(){

		System.out.println();
		System.out.println();
		System.out.println("Mandatory values for STAR:");
		System.out.println(Functions.fixedLength("-refDir <pathToSTARref>", 50)+"The reference directory generated by STAR");
		System.out.println(Functions.fixedLength("-parameterFile <pathToParamterFile>", 50)+"File containing all the extra inormation regarding parameters");
		System.out.println(Functions.fixedLength("-o <outDir>", 50)+"The path to the where the outFiles should end up");

		System.out.println("Mandatory values if working on directory:");
		System.out.println();

	}

}





