package SBATCHscripts;

import java.io.FileWriter;
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
	boolean milou;





	public static void main(String[] args) {
		int length = args.length;

		System.out.println("Current flags:");
		for (int i = 0; i < length; i++) {
			args[i] = args[i].trim();
			System.out.print(args[i] + " ");
		}
		System.out.println();
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
		TRIMFASTQFILES,
		HELP
	}





	public SBATCHinfo() {
		
	}




	public boolean run(Hashtable<String, String> T) {

		if (!T.containsKey("-interactive")) {
			if (!T.containsKey("-pNr") || !T.containsKey("-email") || !T.containsKey("-time")) {
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
		memory = Functions.getInt(T,"-memory", 24);
	
		
		milou = true;
		projectNumber = Functions.getValue(T, "-pNr", "b2010035");
		email = Functions.getValue(T, "-email", "johan.reimegard@scilifelab.se");
		time = Functions.getValue(T, "-time");
		timeStamp = Functions.getDateTime();


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
			if(gatk.addParameters(T)){
				if(gatk.phase1){
					GeneralDir general = new GeneralDir();
					if(general.addParameters(T,this))
						general.generalStart(T, this);
				}
			}
			
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
			if(T.containsKey("-sep")){
				GeneralPairFile CutAdapt = new GeneralPairFile();
				CutAdapt.run(T,this);
			}else if(T.containsKey("-f2")){
				GeneralPairFile CutAdapt = new GeneralPairFile();
				CutAdapt.run(T,this);
			}else{
				GeneralOneFile CutAdapt = new GeneralOneFile();
				CutAdapt.run(T,this);
			}
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
			GeneralOneFile samtools = new GeneralOneFile();
			samtools.run(T,this);
			break;
		case BOWTIE2:
			if(T.containsKey("-sep")){
				GeneralPairFile bowtie2 = new GeneralPairFile();
				bowtie2.run(T,this);
			}else if(T.containsKey("-f2")){
				GeneralPairFile bowtie2 = new GeneralPairFile();
				bowtie2.run(T,this);
			}else{
				GeneralOneFile bowtie2 = new GeneralOneFile();
				bowtie2.run(T,this);
			}
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
		case TRIMFASTQFILES:
			if(T.containsKey("-sep")){
				GeneralPairFile trimFastqFiles = new GeneralPairFile();
				trimFastqFiles.run(T,this);
			}else if(T.containsKey("-f2")){
				GeneralPairFile trimFastqFiles = new GeneralPairFile();
				trimFastqFiles.run(T,this);
			}else{
				GeneralOneFile trimFastqFiles = new GeneralOneFile();
				trimFastqFiles.run(T,this);
			}
			break;
		default: help();	

		}			
		return true;

	}


	public void help(){

		System.out.println("Mandatory flags for SBATCHinfo:");
		System.out.println(Functions.fixedLength("-pNr <projrctNumber>", 30)+"The project which you want the time to be taken from (-pNr b2011098)");
		System.out.println(Functions.fixedLength("-email <your@email>", 30)+"Your email (-email john.doe@scilifelab.se)");
		System.out.println(Functions.fixedLength("-time <time>", 30)+"The time you will (-time 1-2:00:00)");
		System.out.println(Functions.fixedLength("-program <program>",30)+"Available programs are listed below.(-program blast)");
		System.out.println("");
		System.out.println("Other flags for SBATCHinfo:");
		System.out.println(Functions.fixedLength("-core", 30)+" This is default, i.e. one core 3 GB RAM. (-core)");
		System.out.println(Functions.fixedLength("-node <numberOfCores>", 30)+" Number of cores. Default is 8, i.e one node. (-node 8)");
		System.out.println(Functions.fixedLength("-memory <NrOfGB>", 30)+" Memory of nodes. Default is 3, i.e a core. (-memory 24)");
		System.out.println(Functions.fixedLength("-milou", 30)+" If you want to run on the milou cluster. (-memory 24)");
		System.out.println("");
		

		for (Programs info : EnumSet.allOf(Programs.class)) {
			System.out.println(info);
		}

		System.out.println();

	}





	public boolean addSBATCHinfo(Hashtable<String, String> T) {

		if (!T.containsKey("-interactive")) {
			if (!T.containsKey("-pNr") || !T.containsKey("-email") || !T.containsKey("-time")) {
				System.out.println("must contain project number (-pNr), email(-email) and likely time (-time) or be interactive (-interactive)");
				help();
				return false;
			}
		}

		if(T.containsKey("-core")){
			core =true;
			memory = Functions.getInt(T,"-memory", 8);
			
			
		}
		else if(T.containsKey("-node")){
			core =false;
			node = Functions.getInt(T, "-node", 16);
			memory = Functions.getInt(T,"-memory", 128);
		}else{ 
			core =true;
			memory = Functions.getInt(T,"-memory", 8);
		}

		
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
			if(memory <= 512)
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
			if(memory <= 512)
				printSBATCHinfoNode(EW,directory,timestamp, ID, program);
			else
				printSBATCHinfohalvan(EW,directory,timestamp, ID, program);
		}
	}
	public int getNrOfCores(){
		if(core) return memory/8+1;
		else return node;
	}
	
	private void printSBATCHinfoCore(ExtendedWriter EW, String directory,
			String timestamp, String ID, String program) {

		if (!IOTools.isDir(directory + "/reports")) {
			IOTools.mkDir(directory + "/reports");
		}

		String jobName = ID + "_" + program + "_" + timestamp;

		EW.println("#! /bin/bash -l");
		EW.println("#SBATCH -A " + projectNumber);
		if(milou)
		EW.println("#SBATCH -M milou");
		
		EW.println("#SBATCH -p core -n "+getNrOfCores());
		
		
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

	private void printSBATCHinfoNode(ExtendedWriter EW, String directory,
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
		EW.println("#SBATCH -M milou");
		if(memory >= 128){
			if(memory <= 256){
				EW.println("#SBATCH -C mem256GB");
			}
			else if(memory <= 512){
				EW.println("#SBATCH -C mem512GB");
			}
		}
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


	
	
	
	


/*	public void printSBATCHinfo(ExtendedWriter EW, String directory,
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
*/
	private void printSBATCHinfo72GB(ExtendedWriter EW, String directory,
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

	private void printSBATCHinfoCore(ExtendedWriter EW, String directory,
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



	private void printSBATCHinfoFat(ExtendedWriter EW, String directory,
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

	private void printSBATCHinfohalvan(ExtendedWriter EW, String directory,
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

	private void printSBATCHinfohalvan(ExtendedWriter EW, String directory,
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


	public String getProjectNumber() {
		return projectNumber;
	}


	public void setProjectNumber(String projectNumber) {
		this.projectNumber = projectNumber;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String[] getModule() {
		return module;
	}


	public void setModule(String[] module) {
		this.module = module;
	}


	public String getTime() {
		return time;
	}


	public void setTime(String time) {
		this.time = time;
	}


	public int getNode() {
		return node;
	}


	public void setNode(int node) {
		this.node = node;
	}


	public boolean isCore() {
		return core;
	}


	public void setCore(boolean core) {
		this.core = core;
	}


	public int getMemory() {
		return memory;
	}

	public int getMemoryPadded() {
		if(memory < 10)
			return memory-1;
		return memory-3;
	}
	
	

	public void setMemory(int memory) {
		if(memory <= 128)
			this.memory = (int)Math.ceil((double)memory/(double)8);
		else if(memory <= 256)
			this.memory = (int)Math.ceil((double)memory/(double)16);
		else if(memory <= 512)
			this.memory = (int)Math.ceil((double)memory/(double)16);
		else
			this.memory = (int)Math.ceil((double)memory/(double)32);
	}


	public String getTimeStamp() {
		return timeStamp;
	}


	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}


	public boolean isInteractive() {
		return interactive;
	}


	public void setInteractive(boolean interactive) {
		this.interactive = interactive;
	}


	public boolean isMilou() {
		return milou;
	}


	public void setMilou(boolean milou) {
		this.milou = milou;
	}



}
