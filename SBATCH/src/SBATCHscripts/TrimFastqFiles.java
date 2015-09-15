package SBATCHscripts;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import general.ExtendedReader;
import general.ExtendedWriter;
import general.Functions;
import general.IOTools;

public class TrimFastqFiles {

	CutAdapt CA;
	SeqPrep SP;
	String suffix;
	boolean hiseq;

	public TrimFastqFiles() {
		hiseq = false;
		CA=null;
		SP = null;
	}

	public void help(){

		System.out.println("Required flag for TrimFastqFiles:");
		System.out.println(Functions.fixedLength("-phred <33|64>", 30)+"Depending on ilumina version.");
		System.out.println("");
		

	}

	public boolean addParameters(Hashtable<String, String> T){
		boolean allParameters = true;
		if(Functions.getValue(T, "-cutAdapt", "yes").toUpperCase().compareTo("YES") == 0){
			CA = new CutAdapt();
			if(!CA.addParameters(T)) allParameters = false;
		}
		if(Functions.getValue(T, "-SeqPrep", "yes").toUpperCase().compareTo("YES") == 0){
			SP = new SeqPrep();
			if(!SP.addParameters(T)) allParameters = false;
		}

		int phred = Functions.getInt(T, "-phred", -1);
		if (phred == 33){
			hiseq = false;
		}else if(phred == 64){
			hiseq = true;
		}else{
			allParameters = false;
		}
		suffix = Functions.getValue(T, "-suffix", "fastq");
		
		if(!allParameters){
			help();
			return false;
		}
		return true;

	}


	public void trimFastQFile(SBATCHinfo sbatch,
			ExtendedWriter generalSbatchScript, String inDir, String forward) {
		String finalDir = inDir;
		String inDirName = new File(inDir).getName();
		try {
			if (!IOTools.isDir(finalDir + "/reports"))
				IOTools.mkDir(finalDir + "/reports");
			if (!IOTools.isDir(finalDir + "/scripts"))
				IOTools.mkDir(finalDir + "/scripts");

			String fileWithoutSuffix = Functions.getFileWithoutSuffix(forward,this.suffix);
			String sbatchFile = null;
			if(sbatch.interactive){
				sbatchFile = finalDir + "/scripts/" + sbatch.timeStamp
					+ "_" + inDirName +"_"+fileWithoutSuffix+ "_TrimFastqFiles.sh";
				generalSbatchScript.println("sh " + sbatchFile);
			}
			else{
				sbatchFile = finalDir + "/scripts/" + sbatch.timeStamp
						+ "_" + inDirName +"_"+fileWithoutSuffix+ "_TrimFastqFiles.sbatch";
				generalSbatchScript.println("sbatch " + sbatchFile);
			}
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(
					sbatchFile));

			if(!sbatch.interactive)
				sbatch.printSBATCHinfo(EW, inDirName, sbatch.timeStamp, 0, fileWithoutSuffix
					+ "_trimFastqFiles");

			// SeqPrep step merging sequences
			if (CA!= null) {
					CA.addCutAdaptStep(EW, 
							inDir, 
							forward, 
							fileWithoutSuffix+".cutAdapt.fastq.gz");
			}

			FastQC FQC = new FastQC();
			// FastQCstep
			FQC.FastQCSample(EW, inDir,forward);
			FQC.FastQCSample(EW, inDir, fileWithoutSuffix+".cutAdapt.fastq.gz");

			EW.println();
			EW.flush();
			EW.close();
		}
		catch(Exception E){
			E.printStackTrace();
		}
	}	

	public void trimFastQFile(ExtendedWriter EW,SBATCHinfo sbatch,
			ExtendedWriter generalSbatchScript, String inDir, String forward) {
			String fileWithoutSuffix = Functions.getFileWithoutSuffix(forward,this.suffix);

			// SeqPrep step merging sequences
			if (CA!= null) {
				if(CA.split)
					CA.addCutAdaptStep(EW, 
							inDir, inDir,
							forward, 
							fileWithoutSuffix+".cutAdapt.fastq.gz",fileWithoutSuffix+".untrimmed.cutAdapt.fastq.gz");
				else
					CA.addCutAdaptStep(EW, 
							inDir, 
							forward, 
							fileWithoutSuffix+".cutAdapt.fastq.gz");
			}

			FastQC FQC = new FastQC();
			// FastQCstep
			FQC.FastQCSample(EW, inDir,forward);
			FQC.FastQCSample(EW, inDir, fileWithoutSuffix+".cutAdapt.fastq.gz");
			if(CA.split)
				FQC.FastQCSample(EW, inDir, fileWithoutSuffix+".untrimmed.cutAdapt.fastq.gz");
				

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
			String sbatchFile = null;
			ExtendedWriter EW = null;
			
			if(sbatch.interactive){
				sbatchFile = finalDir + "/scripts/" + sbatch.timeStamp
						+ "_" + inDirName +"_"+commonName+ "_TrimFastqFiles.sh";
				generalSbatchScript.println("sh " + sbatchFile);
				EW = new ExtendedWriter(new FileWriter(
						sbatchFile));
				sbatch.printSHELLinfo(EW);
			}
			else{
				sbatchFile = finalDir + "/scripts/" + sbatch.timeStamp
						+ "_" + inDirName +"_"+commonName+ "_TrimFastqFiles.sbatch";
				generalSbatchScript.println("sbatch " + sbatchFile);
				EW = new ExtendedWriter(new FileWriter(
						sbatchFile));
				sbatch.printSBATCHinfo(EW, inDir, sbatch.timeStamp, 0, commonName
						+ "_trimFastqFiles");
			}
				
			
		

			// SeqPrep step merging sequences
			if (SP != null) {
				if (hiseq)
					SP.hiseq = true;
				SP.filter_fastqSample(EW, inDir,forward, reverse, commonName);
			}
			if (CA!= null) {
				if(SP!= null){
					CA.addCutAdaptPairedReadsStep(EW, inDir, 
							commonName+".SeqPrep.1.fastq.gz", 
							commonName+".SeqPrep.2.fastq.gz",
							commonName+".SeqPrep.cutAdapt.1.fastq.gz",
							commonName+".SeqPrep.cutAdapt.2.fastq.gz");

					CA.addCutAdaptStep(EW, 
							inDir, 
							commonName+".SeqPrep.merged.fastq.gz", 
							commonName+".SeqPrep.cutAdapt.merged.fastq.gz");
				}else{
					CA.addCutAdaptPairedReadsStep(EW, 
							inDir, 
							forward, 
							reverse, 
							commonName+".cutAdapt.1.fastq.gz", 
							commonName+".cutAdapt.2.fastq.gz");
				}
			}

			FastQC FQC = new FastQC();
			// FastQCstep
			FQC.FastQCSample(EW, inDir,forward);
			FQC.FastQCSample(EW, inDir, reverse);
			if(SP != null){
				FQC.FastQCSample(EW, inDir, commonName+".SeqPrep.1.fastq.gz");
				FQC.FastQCSample(EW, inDir, commonName+".SeqPrep.2.fastq.gz");
				FQC.FastQCSample(EW, inDir, commonName+".SeqPrep.merged.fastq.gz");
				if(CA!= null){
					FQC.FastQCSample(EW, inDir, commonName+".SeqPrep.cutAdapt.1.fastq.gz");
					FQC.FastQCSample(EW, inDir, commonName+".SeqPrep.cutAdapt.2.fastq.gz");
					FQC.FastQCSample(EW, inDir, commonName+".SeqPrep.cutAdapt.merged.fastq.gz");
				}
			}else{
				if(SP != null){
					FQC.FastQCSample(EW, inDir, commonName+".cutAdapt.1.fastq.gz");
					FQC.FastQCSample(EW, inDir, commonName+".cutAdapt.2.fastq.gz");
				}
			}
			
			EW.println();
			EW.flush();
			EW.close();
		}
		catch(Exception E){
			E.printStackTrace();
		}
	}	
	





}
