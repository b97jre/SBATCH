package SBATCHscripts;

import java.util.EnumSet;
import java.util.Hashtable;

import general.ExtendedWriter;
import general.Functions;
import general.IOTools;

public class SBATCHinfo {

	private String projectNumber;
	private String email;
	private String[] module;
	private String time;
	private int node;
	private boolean core;
	private int memory;
	public String timeStamp;
	
	public boolean interactive; 
	

	public int getNrOfCores(){
		if(core) return 1;
		else return node;
	}
	
	
	public static void main(String[] args) {
		int length = args.length;

		for (int i = 0; i < length; i++) {
			args[i] = args[i].trim();
			System.out.print(args[i] + " ");
		}
		System.out.println();

		Hashtable<String, String> T = Functions.parseCommandLine(args);
		SBATCHinfo SBATCH = new SBATCHinfo();

		if (!SBATCH.run(T)) {
			System.out
			.println("script did not run properly becuase some input was missing.");
		}
	}



	public enum Programs {

		STAR,
		PICARD,
		GATK,
		BWA,
		DENOVO,
		DENOVOANALYSIS,
		EXTENSION,
		FASTQC,
		FILTER,
		GZIP,
		CUTADAPT,
		SHRIMP,
		TOPHAT,
		CUFFLINKS,
		SAMTOOLS,
		BOWTIE2,
		SEQPREP,
		TRINITY,
		DIGINORM,
		SCRIPT,
		MERGE,
		BLAST,
		BLAT,
		PANTHER,
		PFAM,
		HELP
	}





	public SBATCHinfo() {
	}


	
	
	public boolean run(Hashtable<String, String> T) {

		if (!T.containsKey("-interactive")) {
			if (!T.containsKey("-pNr") || !T.containsKey("-email") || !T.containsKey("-time")) {
				System.out.println("must contain project number (-pNr), email(-email) and likely time (-time) or be interactive (-interactive)");
			help();
			return false;
			}
		}
		
		
		if(T.containsKey("-node")){
			core =false;
			node = Functions.getInt(T, "-node", 8);
			memory = Functions.getInt(T,"-memory", 24);
		}else 
			core =true;
		projectNumber = Functions.getValue(T, "-pNr", "b2010035");
		email = Functions.getValue(T, "-email", "johan.reimegard@scilifelab.se");
		time = Functions.getValue(T, "-time");
		timeStamp = Functions.getDateTime();


		module = null;

		if (T.containsKey("-modules")) {
			String modules = Functions.getValue(T, "-modules");
			System.out.println("modules found");
			if (modules.indexOf(" ") > -1)
				module = modules.split(" ");
			else {
				module = new String[1];
				module[0] = modules;
			}
		}

		String programName = Functions.getValue(T, "-program",Functions.getValue(T, "-p","help"));
		Programs program = Programs.valueOf(programName.toUpperCase());

		switch (program) {
		case STAR:
			GeneralPairFile star = new GeneralPairFile();
			star.run(T,this);
			break;
		case PICARD:
			Picard picard = new Picard();
			picard.run(T);
			break;
		case GATK:
			GATK gatk = new GATK();
			gatk.run(T);
			break;
		case BWA:
			BWA bwa = new BWA();
			bwa.run(T);
			break;
		case DENOVO:
			deNovoAssembly deNovoAssembly = new deNovoAssembly();
			deNovoAssembly.run(T);
			break;
		case DENOVOANALYSIS:
			analyseDeNovoTranscripts analyze = new analyseDeNovoTranscripts();
			analyze.run(T);
			break;
		case EXTENSION:
			deNovoExtension deNovoExtension = new deNovoExtension();
			deNovoExtension.run(T);
			break;
		case FASTQC:
			FastQCSBATCH FastQC = new FastQCSBATCH();
			FastQC.run(T);
			break;
		case FILTER:
			FilterFastqSBATCH FilterFastq = new FilterFastqSBATCH();
			FilterFastq.run(T);
			break;
		case GZIP:
			gunzipSBATCH gunzip = new gunzipSBATCH();
			gunzip.run(T);
			break;
		case CUTADAPT:
			WriteTocutAdaptoSBATCH cutadapt = new WriteTocutAdaptoSBATCH();
			cutadapt.run(T);
			break;
		case SHRIMP:
			ShrimpSBATCH shrimp = new ShrimpSBATCH();
			shrimp.run(T);
			break;
		case TOPHAT:
			TopHatSBATCH tophat = new TopHatSBATCH();
			tophat.run(T);
			break;
		case CUFFLINKS:
			GeneralOneFile cufflinks = new GeneralOneFile();
			cufflinks.run(T,this);
			break;
		case SAMTOOLS:
			SamtoolsSBATCH samtools = new SamtoolsSBATCH();
			samtools.run(T);
			break;
		case BOWTIE2:
			bowtie2SBATCH bowtie2 = new bowtie2SBATCH();
			bowtie2.run(T);
			break;
		case SEQPREP:
			SeqPrep seqprep = new SeqPrep();
			seqprep.run(T);
			break;
		case TRINITY:
			Trinity trinity = new Trinity();
			trinity.run(T);
			break;
		case DIGINORM:
			DigiNorm diginorm = new DigiNorm();
			diginorm.run(T);
			break;
		case SCRIPT:
			Script script = new Script();
			script.run(T);
			break;
		case MERGE:
			Merge merge = new Merge();
			merge.run(T);
			break;
		case BLAST:
			GeneralOneFile general = new GeneralOneFile();
			general.run(T,this);
			break;
		case BLAT:
			GeneralOneFile blat = new GeneralOneFile();
			blat.run(T,this);
			break;
		case PANTHER:
			GeneralOneFile panther = new GeneralOneFile();
			panther.run(T,this);
			break;
		case PFAM:
			GeneralOneFile pfam = new GeneralOneFile();
			pfam.run(T,this);
			break;
		default: help();	

		}			
		return true;

	}
	
	
	public void help(){
		
		System.out.println("To start any program you have to specify the -program <program> flag. Available programs are listed below.");
		
		
		System.out.println("To start any program you have to specify the -program <program> flag. Available programs are listed below.");
		for (Programs info : EnumSet.allOf(Programs.class)) {
			System.out.println(info);
		}
		
		System.out.println();
		
	}
	
	

	//
	//		if (T.containsKey("-STAR")) {
	//			STAR sbatch = new STAR();
	//			sbatch.run(T);
	//			break;
	//		}
	//		if (T.containsKey("-picard")) {
	//
	//		}
	//		if (T.containsKey("-GATK")) {
	//		}
	//		if (T.containsKey("-BWA")) {
	//			BWA sbatch = new BWA();
	//			sbatch.run(T);
	//			break;
	//		}
	//		if (T.containsKey("-deNovo")) {
	//			deNovoAssembly sbatch = new deNovoAssembly();
	//			sbatch.run(T);
	//			break;
	//		}
	//		if (T.containsKey("-deNovoAnalysis")) {
	//			analyseDeNovoTranscripts sbatch = new analyseDeNovoTranscripts();
	//			sbatch.run(T);
	//			break;
	//		}
	//
	//		if (T.containsKey("-extension")) {
	//			deNovoExtension sbatch = new deNovoExtension();
	//			sbatch.run(T);
	//			break;
	//		}
	//
	//		if (T.containsKey("-fastQC")) {
	//			FastQCSBATCH sbatch = new FastQCSBATCH();
	//			sbatch.run(T);
	//			break;
	//		}
	//		if (T.containsKey("-filter")) {
	//			FilterFastqSBATCH sbatch = new FilterFastqSBATCH();
	//			sbatch.run(T);
	//			break;
	//		}
	//
	//		if (T.containsKey("-gzip")) {
	//			gunzipSBATCH sbatch = new gunzipSBATCH();
	//			sbatch.run(T);
	//			break;
	//		}
	//
	//		if (T.containsKey("-cutAdapt")) {
	//			WriteTocutAdaptoSBATCH sbatch = new WriteTocutAdaptoSBATCH();
	//			sbatch.run(T);
	//			break;
	//		}
	//
	//		if (T.containsKey("-shrimp")) {
	//			ShrimpSBATCH sbatch = new ShrimpSBATCH();
	//			sbatch.run(T);
	//			break;
	//		}
	//
	//		if (T.containsKey("-tophat")) {
	//			TopHatSBATCH sbatch = new TopHatSBATCH();
	//			sbatch.run(T);
	//			break;
	//		}
	//
	//		if (T.containsKey("-cufflinks")) {
	//			CufflinksSBATCH sbatch = new CufflinksSBATCH();
	//			sbatch.run(T);
	//			break;
	//		}
	//
	//		if (T.containsKey("-samtools")) {
	//			SamtoolsSBATCH sbatch = new SamtoolsSBATCH();
	//			sbatch.run(T);
	//			break;
	//		}
	//
	//		if (T.containsKey("-bowtie2")) {
	//			bowtie2SBATCH sbatch = new bowtie2SBATCH();
	//			sbatch.run(T);
	//			break;
	//		}
	//
	//		if (T.containsKey("-SeqPrep")) {
	//			SeqPrep sbatch = new SeqPrep();
	//			sbatch.run(T);
	//			break;
	//		}
	//		if (T.containsKey("-trinity")) {
	//			Trinity sbatch = new Trinity();
	//			sbatch.run(T);
	//			break;
	//		}
	//		if (T.containsKey("-DigiNorm")) {
	//			DigiNorm sbatch = new DigiNorm();
	//			sbatch.run(T);
	//			break;
	//		}
	//
	//		if (T.containsKey("-script")) {
	//			Script script = new Script();
	//			script.run(T);
	//			break;
	//
	//		}
	//		if (T.containsKey("-merge")) {
	//			Merge script = new Merge();
	//			script.run(T);
	//			break;
	//
	//		} else {
	//			GeneralOneFile general = new GeneralOneFile();
	//			general.run(T);
	//			break;
	//		}


	public boolean addSBATCHinfo(Hashtable<String, String> T) {

		if (!T.containsKey("-interactive")) {
			if (!T.containsKey("-pNr") || !T.containsKey("-email") || !T.containsKey("-time")) {
				System.out.println("must contain project number (-pNr), email(-email) and likely time (-time) or be interactive (-interactive)");
			help();
			return false;
			}
		}
		
		
		if(T.containsKey("-node")){
			core =false;
			node = Functions.getInt(T, "-node", 8);
			memory = Functions.getInt(T,"-memory", 24);
		}else 
			core =true;
		
		projectNumber = Functions.getValue(T, "-pNr", "b2010035");
		email = Functions.getValue(T, "-email", "johan.reimegard@scilifelab.se");
		time = Functions.getValue(T, "-time");
		

		module = null;

		if (T.containsKey("-modules")) {
			String modules = Functions.getValue(T, "-modules");
			if (modules.indexOf(" ") > -1)
				module = modules.split(" ");
			else {
				module = new String[1];
				module[0] = modules;
			}
		}
		return false;

	}

	
	public void printSBATCHinfo(ExtendedWriter EW, String directory,
			String timestamp, int ID, String program) {
		if(core){
			printSBATCHinfoCore(EW,directory,timestamp, String.valueOf(ID), program);
		}else{
			if(memory <= 72)
				printSBATCHinfoNode(EW,directory,timestamp, String.valueOf(ID), program);
			else
				printSBATCHinfohalvan(EW,directory,timestamp, String.valueOf(ID), program);
		}
	}

	
	public void printSBATCHinfo(ExtendedWriter EW, String directory,
			String timestamp, String ID, String program) {
		if(core){
			printSBATCHinfoCore(EW,directory,timestamp, ID, program);
		}else{
			if(memory <= 72)
				printSBATCHinfoNode(EW,directory,timestamp, ID, program);
			else
				printSBATCHinfohalvan(EW,directory,timestamp, ID, program);
		}
	}
	
	
	public void printSBATCHinfoNode(ExtendedWriter EW, String directory,
			String timestamp, String ID, String program) {
		if (!IOTools.isDir(directory + "/reports")) {
			IOTools.mkDir(directory + "/reports");
		}

		if (program.indexOf("/") > -1) {
			program = program.substring(program.indexOf("/"));
		}
		String jobName = ID + "_" + program + "_" + timestamp;

		EW.println("#! /bin/bash -l");
		EW.println("#SBATCH -A " + projectNumber);
		EW.println("#SBATCH -p node -n "+this.node+" ");
		if(memory <= 24)
			EW.println("#SBATCH -C thin");
		else if(memory <= 36)
			EW.println("#SBATCH -C Fat");
		else if(memory <= 72)
			EW.println("#SBATCH -C mem72GB");
			
		EW.println("#SBATCH -t " + time);
		EW.println("#SBATCH -J " + jobName);
		EW.println("#SBATCH -e " + directory + "/reports/" + jobName
				+ "_SLURM_Job_id=%j.stderr.txt");
		EW.println("#SBATCH -o " + directory + "/reports/" + jobName
				+ "_SLURM_Job_id=%j.stdout.txt");
		if (email != null) {
			EW.println("#SBATCH --mail-type=All");
			EW.println("#SBATCH --mail-user=" + email);
		}

		if (module != null) {
			EW.println();
			EW.println();
			EW.println("module load bioinfo-tools");
			for (int i = 0; i < module.length; i++) {
				EW.println("module load " + module[i]);
			}
		}

		EW.println();
		EW.println();
	}

	
	
	
	public void printSBATCHinfo(ExtendedWriter EW, String directory,
			String timestamp, int ID, String program, String time) {
		if (!IOTools.isDir(directory + "/reports")) {
			IOTools.mkDir(directory + "/reports");
		}

		if (program.indexOf("/") > -1) {
			program = program.substring(program.indexOf("/"));
		}
		String jobName = ID + "_" + program + "_" + timestamp;

		EW.println("#! /bin/bash -l");
		EW.println("#SBATCH -A " + projectNumber);
		EW.println("#SBATCH -p node -n 8 ");
		EW.println("#SBATCH -C thin");
		EW.println("#SBATCH -t " + time);
		EW.println("#SBATCH -J " + jobName);
		EW.println("#SBATCH -e " + directory + "/reports/" + jobName
				+ "_SLURM_Job_id=%j.stderr.txt");
		EW.println("#SBATCH -o " + directory + "/reports/" + jobName
				+ "_SLURM_Job_id=%j.stdout.txt");
		if (email != null) {
			EW.println("#SBATCH --mail-type=All");
			EW.println("#SBATCH --mail-user=" + email);
		}

		if (module != null) {
			EW.println();
			EW.println();
			EW.println("module load bioinfo-tools");
			for (int i = 0; i < module.length; i++) {
				EW.println("module load " + module[i]);
			}
		}

		EW.println();
		EW.println();
	}

	public void printSBATCHinfo72GB(ExtendedWriter EW, String directory,
			String timestamp, int ID, String program, String time) {
		if (!IOTools.isDir(directory + "/reports")) {
			IOTools.mkDir(directory + "/reports");
		}

		String jobName = ID + "_" + program + "_" + timestamp;

		EW.println("#! /bin/bash -l");
		EW.println("#SBATCH -A " + projectNumber);
		EW.println("#SBATCH -p node");
		EW.println("#SBATCH -C mem72GB");
		EW.println("#SBATCH -t " + time);
		EW.println("#SBATCH -J " + jobName);
		EW.println("#SBATCH -e " + directory + "/reports/" + jobName
				+ "_SLURM_Job_id=%j.stderr.txt");
		EW.println("#SBATCH -o " + directory + "/reports/" + jobName
				+ "_SLURM_Job_id=%j.stdout.txt");

		if (email != null) {
			EW.println("#SBATCH --mail-type=All");
			EW.println("#SBATCH --mail-user=" + email);
		}

		if (module != null) {
			EW.println();
			EW.println();
			EW.println("module load bioinfo-tools");
			for (int i = 0; i < module.length; i++) {
				EW.println("module load " + module[i]);
			}
		}

		EW.println();
		EW.println();
	}

	public void printSBATCHinfoCore(ExtendedWriter EW, String directory,
			String timestamp, int ID, String program, String time) {

		if (!IOTools.isDir(directory + "/reports")) {
			IOTools.mkDir(directory + "/reports");
		}

		String jobName = ID + "_" + program + "_" + timestamp;

		EW.println("#! /bin/bash -l");
		EW.println("#SBATCH -A " + projectNumber);
		EW.println("#SBATCH -p core");
		EW.println("#SBATCH -t " + time);
		EW.println("#SBATCH -J " + jobName);
		EW.println("#SBATCH -e " + directory + "/reports/" + jobName
				+ "_SLURM_Job_id=%j.stderr.txt");
		EW.println("#SBATCH -o " + directory + "/reports/" + jobName
				+ "_SLURM_Job_id=%j.stdout.txt");

		if (email != null) {
			EW.println("#SBATCH --mail-type=All");
			EW.println("#SBATCH --mail-user=" + email);
		}
		if (module != null) {
			EW.println();
			EW.println();
			EW.println("module load bioinfo-tools");
			for (int i = 0; i < module.length; i++) {
				EW.println("module load " + module[i]);
			}
		}

		EW.println();
		EW.println();
	}

	public void printSBATCHinfoCore(ExtendedWriter EW, String directory,
			String timestamp, String ID, String program) {

		if (!IOTools.isDir(directory + "/reports")) {
			IOTools.mkDir(directory + "/reports");
		}

		String jobName = ID + "_" + program + "_" + timestamp;

		EW.println("#! /bin/bash -l");
		EW.println("#SBATCH -A " + projectNumber);
		EW.println("#SBATCH -p core");
		EW.println("#SBATCH -t " + time);
		EW.println("#SBATCH -J " + jobName);
		EW.println("#SBATCH -e " + directory + "/reports/" + jobName
				+ "_SLURM_Job_id=%j.stderr.txt");
		EW.println("#SBATCH -o " + directory + "/reports/" + jobName
				+ "_SLURM_Job_id=%j.stdout.txt");

		if (email != null) {
			EW.println("#SBATCH --mail-type=All");
			EW.println("#SBATCH --mail-user=" + email);
		}
		if (module != null) {
			EW.println();
			EW.println();
			EW.println("module load bioinfo-tools");
			for (int i = 0; i < module.length; i++) {
				EW.println("module load " + module[i]);
			}
		}

		EW.println();
		EW.println();
	}

	
	
	public void printSBATCHinfoFat(ExtendedWriter EW, String directory,
			String timestamp, int ID, String program, String time) {

		if (!IOTools.isDir(directory + "/reports")) {
			IOTools.mkDir(directory + "/reports");
		}
		String jobName = ID + "_" + program + "_" + timestamp;

		EW.println("#! /bin/bash -l");
		EW.println("#SBATCH -A " + projectNumber);
		EW.println("#SBATCH -p node");
		EW.println("#SBATCH -C fat");
		EW.println("#SBATCH -t " + time);
		EW.println("#SBATCH -J " + jobName);
		EW.println("#SBATCH -e " + directory + "/reports/" + jobName
				+ "_SLURM_Job_id=%j.stderr.txt");
		EW.println("#SBATCH -o " + directory + "/reports/" + jobName
				+ "_SLURM_Job_id=%j.stdout.txt");

		if (email != null) {
			EW.println("#SBATCH --mail-type=All");
			EW.println("#SBATCH --mail-user=" + email);
		}
		if (module != null) {
			EW.println();
			EW.println();
			EW.println("module load bioinfo-tools");
			for (int i = 0; i < module.length; i++) {
				EW.println("module load " + module[i]);
			}
		}

		EW.println();
		EW.println();
	}

	public void printSBATCHinfohalvan(ExtendedWriter EW, String directory,
			String timestamp, int ID, String program, String time, int MB) {
		if (!IOTools.isDir(directory + "/reports")) {
			IOTools.mkDir(directory + "/reports");
		}

		int nrofnodes = MB / 32 + 1;
		if (nrofnodes < 4) {
			nrofnodes = 4;
			System.out.println("Memory allocated will be " + 4 * 32 + " GB");
		} else if (nrofnodes < 8) {
			nrofnodes = 8;
			System.out.println("Memory allocated will be " + 8 * 32 + " GB");
		} else if (nrofnodes < 16) {
			nrofnodes = 16;
			System.out.println("Memory allocated will be " + 8 * 32 + " GB");
		} else if (nrofnodes < 24) {
			nrofnodes = 24;
			System.out.println("Memory allocated will be " + 8 * 32 + " GB");
		} else if (nrofnodes < 32) {
			nrofnodes = 32;
			System.out.println("Memory allocated will be " + 8 * 32 + " GB");
		} else if (nrofnodes < 40) {
			nrofnodes = 40;
			System.out.println("Memory allocated will be " + 8 * 32 + " GB");
		} else if (nrofnodes < 48) {
			nrofnodes = 48;
			System.out.println("Memory allocated will be " + 8 * 32 + " GB");
		} else if (nrofnodes < 56) {
			nrofnodes = 56;
			System.out.println("Memory allocated will be " + 8 * 32 + " GB");
		} else
			nrofnodes = 64;

		String jobName = ID + "_" + program + "_" + timestamp;

		EW.println("#! /bin/bash -l");
		EW.println("#SBATCH -A " + projectNumber);
		EW.println("#SBATCH -M halvan");
		EW.println("#SBATCH -p halvan");
		EW.println("#SBATCH -n " + nrofnodes);
		EW.println("#SBATCH -t " + time);
		EW.println("#SBATCH -J " + jobName);
		EW.println("#SBATCH -e " + directory + "/reports/" + jobName
				+ "_SLURM_Job_id=%j.stderr.txt");
		EW.println("#SBATCH -o " + directory + "/reports/" + jobName
				+ "_SLURM_Job_id=%j.stdout.txt");

		if (email != null) {
			EW.println("#SBATCH --mail-type=All");
			EW.println("#SBATCH --mail-user=" + email);
		}

		if (module != null) {
			EW.println();
			EW.println();
			EW.println("module load bioinfo-tools");
			for (int i = 0; i < module.length; i++) {
				EW.println("module load " + module[i]);
			}
		}

		EW.println();
		EW.println();
	}

	public void printSBATCHinfohalvan(ExtendedWriter EW, String directory,
			String timestamp, String ID, String program) {
		if (!IOTools.isDir(directory + "/reports")) {
			IOTools.mkDir(directory + "/reports");
		}

		int nrofnodes = memory / 32 + 1;
		if (nrofnodes < 4) {
			nrofnodes = 4;
			System.out.println("Memory allocated will be " + 4 * 32 + " GB");
		} else if (nrofnodes < 8) {
			nrofnodes = 8;
			System.out.println("Memory allocated will be " + 8 * 32 + " GB");
		} else if (nrofnodes < 16) {
			nrofnodes = 16;
			System.out.println("Memory allocated will be " + 8 * 32 + " GB");
		} else if (nrofnodes < 24) {
			nrofnodes = 24;
			System.out.println("Memory allocated will be " + 8 * 32 + " GB");
		} else if (nrofnodes < 32) {
			nrofnodes = 32;
			System.out.println("Memory allocated will be " + 8 * 32 + " GB");
		} else if (nrofnodes < 40) {
			nrofnodes = 40;
			System.out.println("Memory allocated will be " + 8 * 32 + " GB");
		} else if (nrofnodes < 48) {
			nrofnodes = 48;
			System.out.println("Memory allocated will be " + 8 * 32 + " GB");
		} else if (nrofnodes < 56) {
			nrofnodes = 56;
			System.out.println("Memory allocated will be " + 8 * 32 + " GB");
		} else
			nrofnodes = 64;

		String jobName = ID + "_" + program + "_" + timestamp;

		EW.println("#! /bin/bash -l");
		EW.println("#SBATCH -A " + projectNumber);
		EW.println("#SBATCH -M halvan");
		EW.println("#SBATCH -p halvan");
		EW.println("#SBATCH -n " + nrofnodes);
		EW.println("#SBATCH -t " + time);
		EW.println("#SBATCH -J " + jobName);
		EW.println("#SBATCH -e " + directory + "/reports/" + jobName
				+ "_SLURM_Job_id=%j.stderr.txt");
		EW.println("#SBATCH -o " + directory + "/reports/" + jobName
				+ "_SLURM_Job_id=%j.stdout.txt");

		if (email != null) {
			EW.println("#SBATCH --mail-type=All");
			EW.println("#SBATCH --mail-user=" + email);
		}

		if (module != null) {
			EW.println();
			EW.println();
			EW.println("module load bioinfo-tools");
			for (int i = 0; i < module.length; i++) {
				EW.println("module load " + module[i]);
			}
		}

		EW.println();
		EW.println();
	}
	
	
	
}
