package SBATCHscripts;

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
import Sequence.FastaSequences;

import general.ExtendedReader;
import general.ExtendedWriter;
import general.Functions;
import general.IOTools;

public class CutAdapt {

	ArrayList<String> threeAdapters;
	ArrayList<String> otherAdapters;

	int cutoff;
	String suffix;
	String[] sep;
	int length;
	int overlap;
	boolean hiseq;
	boolean split;
	String untrimmedFileName;

	public static void main(String[] args) {

		int length = args.length;
		for (int i = 0; i < length; i++) {
			args[i] = args[i].trim();
			System.out.print(args[i] + " ");
		}
		System.out.println();
		Hashtable<String, String> T = Functions.parseCommandLine(args);


	}

	public CutAdapt() {
		hiseq = false;
		threeAdapters = null;
		untrimmedFileName = null;

	}

	public static void help(){

		System.out.println("At least one flag for cutAdapt has to be present:");
		System.out.println(Functions.fixedLength("-a <3PrimeAdapterSequenceFile>", 30)+"This is the location of the file that contains all the adaptersequences");
		System.out.println(Functions.fixedLength("-b <AdapterSequenceFile>", 30)+"This is the location of the file that contains all the adaptersequences");
		System.out.println(Functions.fixedLength("-q <PhredScoreQualtiy>", 30)+"");
		System.out.println(Functions.fixedLength("-m <MinimumLength>",30)+"");
		System.out.println(Functions.fixedLength("-untrimmed-output <untrimedFileName>",30)+"");
		
		
		System.out.println("");

		System.out.println("For more details concerning the cutadapt flags see cutadapt manual");
		System.out.println("");

	}





	public boolean addParameters(Hashtable<String, String> T){
		String threePrimeAdaptersFile = Functions.getValue(T, "-a", null);
		if(threePrimeAdaptersFile != null)
			threeAdapters = getAdapters(threePrimeAdaptersFile);
		String allAdaptersFile = Functions.getValue(T, "-b",  null);
		if(allAdaptersFile != null)
			otherAdapters = getAdapters(allAdaptersFile);
		cutoff = Functions.getInt(T, "-q", -1);
		length = Functions.getInt(T, "-m", -1);
		overlap = Functions.getInt(T, "-overlap", 3);
		
		if(T.containsKey("-untrimmedFileName")){
			this.split = true;
			this.untrimmedFileName = Functions.getValue(T, "-untrimmedFileName");
		}
		

		if(length >-1 || cutoff > -1 || threeAdapters != null || otherAdapters != null) return true;
		help();
		return false;

	}

	public static boolean checkParameters(Hashtable<String, String> T){
		String threePrimeAdaptersFile = Functions.getValue(T, "-a", null);
	
		String allAdaptersFile = Functions.getValue(T, "-b",  null);
		int cutoff = Functions.getInt(T, "-q", -1);
		int length = Functions.getInt(T, "-m", -1);

		if(length >-1 || cutoff > -1 || threePrimeAdaptersFile != null || allAdaptersFile != null) return true;
		help();
		return false;


	}

	
	public void trimFastQFile(SBATCHinfo sbatch,
			ExtendedWriter generalSbatchScript, String inDir, String outDir, String inFile, String suffix) {
		String finalDir = inDir;
		String inDirName = new File(inDir).getName();
		
		String fileBase = Functions.getFileWithoutSuffix(inFile, suffix);
		String outFile = fileBase+".cutAdapt."+suffix;
		if(split && this.untrimmedFileName == null){	
			this.untrimmedFileName=fileBase+".cutAdapt.Untrimmed."+suffix;
		}
		try {
			if (!IOTools.isDir(finalDir + "/reports"))
				IOTools.mkDir(finalDir + "/reports");
			if (!IOTools.isDir(finalDir + "/scripts"))
				IOTools.mkDir(finalDir + "/scripts");

			String sbatchFile = finalDir + "/scripts/" + sbatch.timeStamp
					+ "_" + inDirName +"_"+inFile+ "_Cutadapt.sbatch";
			generalSbatchScript.println("sbatch " + sbatchFile);
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(
					sbatchFile));

			sbatch.printSBATCHinfo(EW, inDir, sbatch.timeStamp,inFile, "CutAdapt");
			
			if(split)
				addCutAdaptStep(EW, 
						inDir,
						outDir,
						inFile, 
						outFile,
						this.untrimmedFileName);
			else
				addCutAdaptStep(EW, 
						inDir,
						outDir,
						inFile, 
						outFile);
			

			FastQC FQC = new FastQC();
				// FastQCstep
			FQC.FastQCSample(EW, inDir,inFile);
			FQC.FastQCSample(EW, inDir, fileBase+".cutAdapt."+suffix);
			if(split)
			FQC.FastQCSample(EW, inDir, fileBase+".cutAdapt.Untrimmed."+suffix);
			

			EW.println();
			EW.flush();
			EW.close();
		}
		catch(Exception E){
			E.printStackTrace();
		}
	}	
	
	
	public void trimFastQFile(SBATCHinfo sbatch,
			ExtendedWriter generalSbatchScript, String inDir, String outDir, String inFile, String outFile, String untrimmedFile) {
		String finalDir = inDir;
		String inDirName = new File(inDir).getName();
		try {
			if (!IOTools.isDir(finalDir + "/reports"))
				IOTools.mkDir(finalDir + "/reports");
			if (!IOTools.isDir(finalDir + "/scripts"))
				IOTools.mkDir(finalDir + "/scripts");

			String sbatchFile = finalDir + "/scripts/" + sbatch.timeStamp
					+ "_" + inDirName +"_"+inFile+ "_Cutadapt.sbatch";
			generalSbatchScript.println("sbatch " + sbatchFile);
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(
					sbatchFile));

			sbatch.printSBATCHinfo(EW, inDir, sbatch.timeStamp,inFile, "CutAdapt");

			addCutAdaptStep(EW, 
							inDir,
							outDir,
							inFile, 
							outFile,
							untrimmedFile);

			FastQC FQC = new FastQC();
				// FastQCstep
			FQC.FastQCSample(EW, inDir,inFile);
			FQC.FastQCSample(EW, inDir, outFile);
			if(untrimmedFile != null){
				FQC.FastQCSample(EW, inDir, untrimmedFile);
			}

			EW.println();
			EW.flush();
			EW.close();
		}
		catch(Exception E){
			E.printStackTrace();
		}
	}	
	
	
	public void trimFastQFiles(SBATCHinfo sbatch,
			ExtendedWriter generalSbatchScript, String inDir, String forward, String reverse) {
		String finalDir = inDir;
		String inDirName = new File(inDir).getName();
		try {
			if (!IOTools.isDir(finalDir + "/reports"))
				IOTools.mkDir(finalDir + "/reports");
			if (!IOTools.isDir(finalDir + "/scripts"))
				IOTools.mkDir(finalDir + "/scripts");

			String commonName = Functions.getCommonPrefix(forward, reverse);
			String sbatchFile = finalDir + "/scripts/" + sbatch.timeStamp
					+ "_" + inDirName +"_"+commonName+ "_Cutadapt.sbatch";
			generalSbatchScript.println("sbatch " + sbatchFile);
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(
					sbatchFile));

			sbatch.printSBATCHinfo(EW, inDir, sbatch.timeStamp,commonName, "CutAdapt");

			addCutAdaptPairedReadsStep(EW, 
							inDir, 
							forward, 
							reverse, 
							commonName+".cutAdapt.1.fastq.gz", 
							commonName+".cutAdapt.2.fastq.gz");

			FastQC FQC = new FastQC();
				// FastQCstep
			FQC.FastQCSample(EW, inDir,forward);
			FQC.FastQCSample(EW, inDir, reverse);
			FQC.FastQCSample(EW, inDir, commonName+".cutAdapt.1.fastq.gz");
			FQC.FastQCSample(EW, inDir, commonName+".cutAdapt.2.fastq.gz");

			EW.println();
			EW.flush();
			EW.close();
		}
		catch(Exception E){
			E.printStackTrace();
		}
	}	
	
	
	public void addCutAdaptStep(ExtendedWriter EW, String inDir,String outDir,
			String inFile, String outFile,String untrimmedFile) {
		EW.println();
		EW.println();
		EW.println("#############################################################################################################");
		EW.println("## Running cutadapt 1.3 START");
		EW.println("#############################################################################################################");
		EW.println();
		EW.println();
		EW.println();
		EW.print("/sw/apps/bioinfo/cutadapt/1.3/kalkyl/bin/cutadapt");

		if (cutoff > -1)
			EW.print(" -q " + cutoff + " ");
		if (length > -1)
			EW.print(" --minimum-length " + length + " ");
		if (threeAdapters != null ||  otherAdapters != null){
			EW.print(" --overlap=" + this.overlap + " ");
			if (threeAdapters != null) {
				for (int j = 0; j < threeAdapters.size(); j++) {
					EW.print(" -a " + threeAdapters.get(j));
				}
			}
			if (otherAdapters != null) {
				for (int j = 0; j < otherAdapters.size(); j++) {
					EW.print(" -b " + otherAdapters.get(j));
				}
			}
		}
		EW.print(" --untrimmed-output=" + outDir + "/" + untrimmedFile);
		EW.print(" -o " + outDir + "/" + outFile);
		
		EW.println(" " + inDir + "/" + inFile);
		EW.println();
		EW.println("#############################################################################################################");
		EW.println("## running cutadapt DONE");
		EW.println("#############################################################################################################");
		EW.println();
		EW.println();

	}
	
	public void addCutAdaptStep(ExtendedWriter EW, String inDir,String outDir,
			String inFile, String outFile) {
		EW.println();
		EW.println();
		EW.println("#############################################################################################################");
		EW.println("## Running cutadapt 1.3 START");
		EW.println("#############################################################################################################");
		EW.println();
		EW.println();
		EW.println();
		EW.print("/sw/apps/bioinfo/cutadapt/1.3/kalkyl/bin/cutadapt");

		if (cutoff > -1)
			EW.print(" -q " + cutoff + " ");
		if (length > -1)
			EW.print(" --minimum-length " + length + " ");
		if (threeAdapters != null ||  otherAdapters != null){
			EW.print(" --overlap=" + this.overlap + " ");
			if (threeAdapters != null) {
				for (int j = 0; j < threeAdapters.size(); j++) {
					EW.print(" -a " + threeAdapters.get(j));
				}
			}
			if (otherAdapters != null) {
				for (int j = 0; j < otherAdapters.size(); j++) {
					EW.print(" -b " + otherAdapters.get(j));
				}
			}
		}
		EW.print(" -o " + inDir + "/" + outFile);
		EW.println(" " + inDir + "/" + inFile);
		EW.println();
		EW.println("#############################################################################################################");
		EW.println("## running cutadapt DONE");
		EW.println("#############################################################################################################");
		EW.println();
		EW.println();

	}
	


	
	
	public void addCutAdaptStep(ExtendedWriter EW, String inDir,
			String inFile, String outFile) {
		EW.println();
		EW.println();
		EW.println("#############################################################################################################");
		EW.println("## Running cutadapt 1.3 START");
		EW.println("#############################################################################################################");
		EW.println();
		EW.println();
		EW.println();
		EW.print("/sw/apps/bioinfo/cutadapt/1.3/kalkyl/bin/cutadapt");

		if (cutoff > -1)
			EW.print(" -q " + cutoff + " ");
		if (length > -1)
			EW.print(" --minimum-length " + length + " ");
		if (threeAdapters != null ||  otherAdapters != null){
			EW.print(" --overlap=" + this.overlap + " ");
			if (threeAdapters != null) {
				for (int j = 0; j < threeAdapters.size(); j++) {
					EW.print(" -a " + threeAdapters.get(j));
				}
			}
			if (otherAdapters != null) {
				for (int j = 0; j < otherAdapters.size(); j++) {
					EW.print(" -b " + otherAdapters.get(j));
				}
			}
		}
		EW.print(" -o " + inDir + "/" + outFile);
		EW.println(" " + inDir + "/" + inFile);
		EW.println();
		EW.println("#############################################################################################################");
		EW.println("## running cutadapt DONE");
		EW.println("#############################################################################################################");
		EW.println();
		EW.println();

	}




	public void addCutAdaptPairedReadsStep(ExtendedWriter EW, String inDir,String forward, String reverse, String outForward, String outReverse) {

		/*		 according to steps 
		If you use one of the read-discarding options, then the --paired-output option is needed to keep the two files synchronized. First trim the forward read, writing output to temporary files:

			cutadapt -a ADAPTER_FWD --minimum-length 20 --paired-output tmp.2.fastq -o tmp.1.fastq reads.1.fastq reads.2.fastq
			Then trim the reverse read, using the temporary files as input:

			cutadapt -a ADAPTER_REV --minimum-length 20 --paired-output trimmed.1.fastq -o trimmed.2.fastq tmp.2.fastq tmp.1.fastq
			Finally, remove the temporary files:

			rm tmp.1.fastq tmp.2.fastq
		 */



		EW.println();
		EW.println();
		EW.println("#############################################################################################################");
		EW.println("## Running cutadapt 1.3 START");
		EW.println("#############################################################################################################");
		EW.println();
		EW.println();
		EW.println("module load bioinfo-tools");
		EW.println();
		EW.println("cd "+inDir);

		EW.println();
		// First run cutadapt -a ADAPTER_FWD --minimum-length 20 --paired-output tmp.2.fastq -o tmp.1.fastq reads.1.fastq reads.2.fastq
		EW.print("/sw/apps/bioinfo/cutadapt/1.3/kalkyl/bin/cutadapt ");
		if (cutoff > -1)
			EW.print(" -q " + cutoff + " ");
		if (length > -1)
			EW.print(" --minimum-length " + length + " ");
		if (threeAdapters != null ||  otherAdapters != null){
			EW.print(" --overlap=" + this.overlap + " ");

			if (threeAdapters != null) {
				for (int j = 0; j < threeAdapters.size(); j++) {
					EW.print(" -a " + threeAdapters.get(j));
				}
			}
			if (otherAdapters != null) {
				for (int j = 0; j < otherAdapters.size(); j++) {
					EW.print(" -b " + otherAdapters.get(j));
				}
			}
		}
		EW.print(" --paired-output tmp."+reverse);
		EW.print(" -o tmp." +forward);
		EW.println(" " + forward + " " + reverse);


		EW.println();
		// Second run cutadapt -a ADAPTER_REV --minimum-length 20 --paired-output trimmed.1.fastq -o trimmed.2.fastq tmp.2.fastq tmp.1.fastq
		EW.print("/sw/apps/bioinfo/cutadapt/1.3/kalkyl/bin/cutadapt ");
		if (cutoff > -1)
			EW.print(" -q " + cutoff + " ");
		if (length > -1)
			EW.print(" --minimum-length " + length + " ");
		if (threeAdapters != null ||  otherAdapters != null){
			EW.print(" --overlap=" + this.overlap + " ");
			if (threeAdapters != null) {
				for (int j = 0; j < threeAdapters.size(); j++) {
					EW.print(" -a " + threeAdapters.get(j));
				}
			}
			if (otherAdapters != null) {
				for (int j = 0; j < otherAdapters.size(); j++) {
					EW.print(" -b " + otherAdapters.get(j));
				}
			}
		}
		EW.print(" --paired-output "+outForward);
		EW.print(" -o " +outReverse);
		EW.println("  tmp." +reverse + " tmp." +forward);
		EW.println();
		EW.println();
		EW.println("rm  tmp." +reverse);
		EW.println("rm   tmp." +forward); 
		EW.println();
		EW.println("#############################################################################################################");
		EW.println("## Running cutadapt DONE");
		EW.println("#############################################################################################################");
		EW.println();
		EW.println();

	}





	public static ArrayList<String> getAdapters(String file) {

		ArrayList<String> SequenceFiles = new ArrayList<String>();
		if (file != null) {
			try {
				FastaSequences FS = new FastaSequences(file);
				for(int i = 0; i < FS.size();i++){
					SequenceFiles.add(FS.get(i).getStringSequence());
				} 
			}catch (Exception E) {
				E.printStackTrace();
			}
		}
		return SequenceFiles;
	}
}
 