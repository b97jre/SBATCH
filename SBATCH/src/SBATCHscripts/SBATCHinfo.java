package SBATCHscripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;

import general.ExtendedReader;
import general.ExtendedWriter;
import general.Functions;
import general.IOTools;

public class SBATCHinfo {

	private String projectNumber;
	private String email;
	private String[] module;
	private ArrayList<Integer> afterany;
	private String time;
	private int core;
	private int memory;
	public String timeStamp;

	public boolean interactive; 
	boolean milou;

	String jarPath;
	String fileParserJarFile;
	String HTStoolsJarFile;
	String programName;




	public static void main(String[] args) {

		int length = args.length;


		System.out.println("Current flags:");
		//		for (int i = 0; i < length; i++) {
		//			args[i] = args[i].trim();
		//			System.out.print(args[i] + " ");
		//		}
		//		System.out.println();
		//		System.out.println();

		Hashtable<String, String> T = Functions.parseCommandLine(args);
		SBATCHinfo SBATCH = new SBATCHinfo();

		SBATCH.jarPath = SBATCHinfo.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		SBATCH.fileParserJarFile = Functions.getValue(T, "-fileParserJarFile","/glob/johanr/bin/FileParser.jar");
		SBATCH.HTStoolsJarFile = Functions.getValue(T, "-HTStoolsJarFile","/glob/johanr/bin/HTStools.jar");

		//System.out.println("The path to the jar file is : "+SBATCH.jarPath);
		if (!SBATCH.run(T)) {
			System.out
			.println("script did not run properly becuase some input was missing.");
		}
	}



	public enum Programs {

		ANNOTATION,
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
		TRIMDENOVOFILES,
		ONELINE,
		PHASERNA,
		SKELLYFORMAT,
		ANNOTATEPROTEIN,
		BLAST2GO,
		ARGOT2,
		PERSONALIZEDSKELLYFORMAT,
		BAM2WIG,
		HTSEQ,
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
		}else{
			this.interactive = true;
			System.out.println("You have set the interactive flag.");
			System.out.println("This means that the programs will not run as sbatch scripts but instead they will be created as shell scripts");
			System.out.println("As a consecuense no modules will be loaded and that you will have to make sure that the program that you run are already installed in the PATH of your computer");
			System.out.println("This is still under development and may not work for all programs.");
			
		}
		core = Functions.getInt(T,"-core", 1);

		memory = Functions.getInt(T,"-memory",8);


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

		if (T.containsKey("-afterany")) {
			String modules = Functions.getValue(T, "-afterany");
			this.afterany = new ArrayList<Integer>();
			if (modules.indexOf(" ") > -1){
				String[] temp = modules.split(" ");
				for(int i = 0; i< temp.length; i++){
					afterany.add(Integer.parseInt(temp[i]));
				}
			}else {
				afterany.add(Integer.parseInt(modules));
			}
		}

		if (T.containsKey("-node")) {
			core = Functions.getInt(T,"-node", 16);
		}


		programName = Functions.getValue(T, "-program",Functions.getValue(T, "-p","help"));
		System.out.println("programName "+programName);
		Programs program = Programs.valueOf(programName.toUpperCase());

		switch (program) {
		case STAR:
			if(T.containsKey("-sep")){
				GeneralPairFile star = new GeneralPairFile();
				star.run(T,this);
			}
			else{
				System.out.println("Did not contain flag -sep to identify paired end reads. ");
				System.out.println("Assuming single end reads. ");
				GeneralOneFile star = new GeneralOneFile();
				star.run(T, this);
			}
			break;
		case BAM2WIG:
			GeneralOneFile bam2wig = new GeneralOneFile();
			bam2wig.run(T, this);
		
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
				else if(gatk.phase2){	
					GATKphase2 phase2 = new GATKphase2();
					if(phase2.checkParameters(T)){
						phase2.addParameters(T);
						phase2.GATKPhase2(this);
					}
				}
				else if(gatk.phase){	
					GATKphaseSNPs phaseSNPs = new GATKphaseSNPs();
					if(phaseSNPs.checkParameters(T)){
						phaseSNPs.addParameters(T);
						phaseSNPs.GATKPhaseSNPs(this);
					}
				}
				else if(gatk.genotype){	
					GeneralOneFile genotypeSample = new GeneralOneFile();
					genotypeSample.run(T,this);
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
			AnnotateRNA analyze = new AnnotateRNA(T);
//			analyze.run(T,this);
			break;
		case EXTENSION:
			deNovoExtension deNovoExtension = new deNovoExtension();
			deNovoExtension.run(T);
			break;
		case FASTQC:
			FastQC FastQC = new FastQC();
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
		case HTSEQ:
			GeneralOneFile htseq = new GeneralOneFile();
			ArrayList<Integer> jobIds = htseq.run(T,this);
			if(jobIds != null){
				String suffix = Functions.getValue(T, "-newSuffix", "htseqCount");
				String script = HTseqCount.mergeHTseqSbatchScript(this, jobIds, this.fileParserJarFile, 
						Functions.getValue(T, "-i"), suffix, "htseqCount.merged."+suffix+".table.tab.txt") ;
				startSbatchScript(script);
			}
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
			merge.run(T,this);
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
		case PHASERNA:
			phaseRNAsSNPs phaseRNAs = new phaseRNAsSNPs();
			if(phaseRNAs.checkParameters(T)){
				phaseRNAs.addParameters(T);
				phaseRNAs.phaseRNAs(this);
			}
			break;
		case SKELLYFORMAT:
			SkellyFormat SF = new SkellyFormat();
			if(SF.checkParameters(T)){
				SF.addParameters(T);
				SF.getSkellyFormat(this);
			}
			break;
		case PERSONALIZEDSKELLYFORMAT:
			PersonalizedSkellyFormat PSF = new PersonalizedSkellyFormat();
			if(PSF.checkParameters(T)){
				PSF.addParameters(T);
				PSF.getSkellyFormat(this);
			}
			break;
		case BLAST2GO:
			Blast2GO blast2GO = new Blast2GO();
			blast2GO.run(T,this);
			break;
		case ARGOT2:
			Argot2 argot2 = new Argot2();
			argot2.run(T,this);
			break;
		case ANNOTATION:
			Annotation annotation = new Annotation();
			annotation.run(T,this);
			break;
		case ANNOTATEPROTEIN:
			AnnotateAASequences annotateAA = new AnnotateAASequences();
			annotateAA.run(T,this);
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
		case TRIMDENOVOFILES:
			GeneralOneFile trimDeNovoFiles = new GeneralOneFile();
			trimDeNovoFiles.run(T,this);
			break;
		case ONELINE:
			System.out.println("One line it is");
			if(T.containsKey("-i")){
				oneLine(T);
			}
			break;
		default: help();	

		}			
		return true;

	}


	public void oneLine(Hashtable<String, String> T){
		String oneLine  = T.get("-i");
		System.out.println("The line to be runned is :");
		System.out.println(oneLine);

		ExtendedWriter EW = printSBATCHInfoSTART("oneLine");
		EW.println();
		EW.println();
		if(T.containsKey("-o")){
			String out = T.get("-o");
			EW.println(oneLine +"> tmp."+out);
			EW.println("mv tmp."+out+" "+out);

		}else
			EW.println(oneLine);
		EW.println();
		EW.println();
		EW.flush();
		EW.close();

		try {
			String line;
			Process p = Runtime.getRuntime().exec("sbatch "+getSbatchFileName("oneLine"));
			BufferedReader in = new BufferedReader(
					new InputStreamReader(p.getInputStream()) );
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
			in.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Integer> startSbatchScripts(String shellScript){

		try {
			ArrayList<Integer> jobids = new ArrayList<Integer>();
			ExtendedReader ER = ExtendedReader.getFileReader(shellScript);
			while(ER.more()){
				String sbatchScript = ER.readLine();
				System.out.println("Starting sbatchscript "+ sbatchScript);
				Integer A = null;
				if(sbatchScript.indexOf("sbatch ") == 0){
					A = startSbatchScript(sbatchScript.split(" ")[1]);
				}else{
					A = startSbatchScript(sbatchScript);
				}
				if(A!= null)
					jobids.add(A);
			}
			ER.close();
			return jobids;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	public ArrayList<Integer> startSbatchScripts(ArrayList<String> sbatchScripts){

		try {
			ArrayList<Integer> jobids = new ArrayList<Integer>();
			for(int i = 0; i< sbatchScripts.size();i++){
				System.out.println("Starting sbatchscript "+ sbatchScripts.get(i));
				Integer A = startSbatchScript(sbatchScripts.get(i));
				if(A!= null)
					jobids.add(A);
			}
			return jobids;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	public Integer startSbatchScript(String sbatchScript){
		Integer I = null;
		try {
			String line;
			Process p = Runtime.getRuntime().exec("sbatch "+sbatchScript);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(p.getInputStream()) );
			while ((line = in.readLine()) != null) {
				System.out.println(line);
				// Submitted batch job 906647 on cluster milou
				String[] info = line.split(" ");
				I = Integer.parseInt(info[3]);
			}
			in.close();
			in = new BufferedReader(
					new InputStreamReader(p.getErrorStream()) );
			while ((line = in.readLine()) != null) {
				System.out.println(line);
				// Submitted batch job 906647 on cluster milou
			}
			if(I == null){
				System.out.println("ATTENTION!! sbatchScript "+sbatchScript+" did not start properly");
			}
			else{
				if(memory <= 512)
					System.out.println("Submitted batch job id is "+I+" on cluster milou");
				else
					System.out.println("Submitted batch job id is "+I+" on Halvan");
				return I;
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}



	public String writeSbatchScript( ArrayList<Integer> afterAny,ArrayList<String> Executions, String programName){
		System.out.println("The lines to be runned is :");
		for(int i = 0; i < Executions.size();i++){
			System.out.println(Executions.get(i));
		}
		if(afterAny != null){
			System.out.println("The program will be running after  line to be runned is :");
			this.afterany = afterAny;
		}

		ExtendedWriter EW = printSBATCHInfoSTART(programName);
		EW.println();
		EW.println();
		for(int i = 0; i < Executions.size();i++){
			EW.println(Executions.get(i));
		}
		EW.println();
		EW.println();
		EW.flush();
		EW.close();
		return getSbatchFileName(programName);
	}

	public Integer submitSBATCHScript(String sbatchFileName){
		try {
			String line;
			Process p = Runtime.getRuntime().exec("sbatch "+sbatchFileName);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(p.getInputStream()) );
			while ((line = in.readLine()) != null) {
				System.out.println(line);

			}
			in.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}



	public String getSbatchFileName(String program) {
		String cDir = IOTools.getCurrentPath();
		String sbatchFile = cDir + "/scripts/" +program 
				+ "_" +new File(cDir).getName()+ "_"+timeStamp +".sbatch";
		return sbatchFile;

	}

	public String getSbatchFileName(String program, String cDir) {
		String sbatchFile = cDir + "/scripts/" +program 
				+ "_" +new File(cDir).getName()+ "_"+timeStamp +".sbatch";
		return sbatchFile;

	}


	public ExtendedWriter printSBATCHInfoSTART(String program, String inDir,  ArrayList<Integer> afterAny){
		ArrayList<Integer> temp = this.afterany;
		this.afterany=afterAny;
		ExtendedWriter EW = printSBATCHInfoSTART(program,inDir);
		this.afterany = temp;
		return EW;
	}

	public ExtendedWriter printSBATCHInfoSTART(String program, ArrayList<Integer> afterAny){
		String cp = IOTools.getCurrentPath();
		return printSBATCHInfoSTART(program,cp,afterAny);
	}

	public ExtendedWriter printSBATCHInfoSTART(String program) {
		String cp = IOTools.getCurrentPath();
		return printSBATCHInfoSTART(program,cp);
	}

	public ExtendedWriter printSBATCHInfoSTART(String program, String inDir) {
		try {
			if (!IOTools.isDir(inDir + "/reports"))
				IOTools.mkDir(inDir + "/reports");
			if (!IOTools.isDir(inDir + "/scripts"))
				IOTools.mkDir(inDir + "/scripts");
			String sbatchFile = inDir + "/scripts/" +program 
					+ "_" +new File(inDir).getName()+ "_"+timeStamp +".sbatch";			
			System.out.println("Creating sbatch_file :"+sbatchFile);
			ExtendedWriter EW = ExtendedWriter.getFileWriter(sbatchFile);
			this.printSBATCHinfo(EW, inDir, timeStamp, 0, program);

			EW.println("cd "+inDir);
			return EW;
		}
		catch(Exception E){
			E.printStackTrace();
		}
		return null;
	}

	public String getSBATCHfileName(String program, String inDir) {
		String sbatchFile = inDir + "/scripts/" +program 
				+ "_" +new File(inDir).getName()+ "_"+timeStamp +".sbatch";			
		return sbatchFile;
	}

	public ExtendedWriter printShellInfoSTART(String program) {
		try {
			String cDir = IOTools.getCurrentPath();
			if (!IOTools.isDir(cDir + "/reports"))
				IOTools.mkDir(cDir + "/reports");
			if (!IOTools.isDir(cDir + "/scripts"))
				IOTools.mkDir(cDir + "/scripts");
			String sbatchFile = cDir + "/scripts/" + timeStamp
					+ "_" +new File(cDir).getName()+ "_"+ program+".sh";
			ExtendedWriter EW = ExtendedWriter.getFileWriter(sbatchFile);
			return EW;
		}
		catch(Exception E){
			E.printStackTrace();
		}
		return null;
	}




	public ExtendedWriter printShellInfoSTART(String program, String inDir ) {
		try {
			String cDir = IOTools.getCurrentPath();
			if (!IOTools.isDir(cDir + "/reports"))
				IOTools.mkDir(cDir + "/reports");
			if (!IOTools.isDir(cDir + "/scripts"))
				IOTools.mkDir(cDir + "/scripts");
			String sbatchFile = cDir + "/scripts/" + timeStamp
					+ "_" +new File(inDir).getName()+ "_"+ program+".sh";
			ExtendedWriter EW = ExtendedWriter.getFileWriter(sbatchFile);
			return EW;
		}
		catch(Exception E){
			E.printStackTrace();
		}
		return null;
	}

	public String getSbatchFileName(String program, String inDir, int count ) {
		return inDir + "/scripts/" + timeStamp
				+ "_" +new File(inDir).getName()+ "_"+ program+"_"+count+".sbatch";
	}


	public ExtendedWriter printSBATCHInfoSTART(String program, String inDir, int count ) {
		try {
			if (!IOTools.isDir(inDir + "/reports"))
				IOTools.mkDir(inDir + "/reports");
			if (!IOTools.isDir(inDir + "/scripts"))
				IOTools.mkDir(inDir + "/scripts");
			String sbatchFile = getSbatchFileName(program,inDir,count);
			ExtendedWriter EW = ExtendedWriter.getFileWriter(sbatchFile);

			printSBATCHinfo(EW, inDir, getTimeStamp(), count, program);

			return EW;
		}
		catch(Exception E){
			E.printStackTrace();
		}
		return null;
	}


	public ExtendedWriter printSBATCHInfoSTART(String program, String inDir, int count, ArrayList<Integer> afterAny ) {
		try {
			if (!IOTools.isDir(inDir + "/reports"))
				IOTools.mkDir(inDir + "/reports");
			if (!IOTools.isDir(inDir + "/scripts"))
				IOTools.mkDir(inDir + "/scripts");
			ArrayList<Integer> temp = this.afterany;
			this.afterany = afterAny;
			String sbatchFile = getSbatchFileName(program,inDir,count);
			ExtendedWriter EW = ExtendedWriter.getFileWriter(sbatchFile);

			printSBATCHinfo(EW, inDir, getTimeStamp(), count, program);

			EW.println();
			EW.println("cd "+inDir);
			this.afterany = temp;
			return EW;
		}
		catch(Exception E){
			E.printStackTrace();
		}
		return null;
	}


	public String printShellInfoSTOP(ExtendedWriter EW, String program, String inDir ) {
		try {
			EW.flush();
			EW.close();

			String cDir = IOTools.getCurrentPath();
			String sbatchFile = cDir + "/scripts/" + timeStamp
					+ "_" +new File(inDir).getName()+ "_"+ program+".sh";

			System.out.println("All things seems to have run properly!");
			System.out.println("To start all the scripts write  :");
			System.out.println("sh "+sbatchFile);

			return sbatchFile;
		}
		catch(Exception E){
			E.printStackTrace();
		}
		return null;
	}


	public void printInteractiveStart(ExtendedWriter EW, String programName) {
		EW.print("java -Xmx"+this.getMemoryPadded()+"G -jar "+this.jarPath+" -interactive -program "+programName+" " );
	}



	public void mvTempFiles(ExtendedWriter EW, String dir, ArrayList<String> temp, ArrayList<String> finalFile){
		for(int i = 0; i < temp.size();i++){
			EW.println("mv "+dir+"/"+temp.get(i)+" "+dir+"/"+finalFile.get(i));
		}
	}

	public void mvTempFiles(ExtendedWriter EW,  ArrayList<String> temp, ArrayList<String> finalFile){
		for(int i = 0; i < temp.size();i++){
			EW.println("mv "+temp.get(i)+" "+finalFile.get(i));
		}
	}



	public void help(){

		System.out.println("Mandatory flags for SBATCHinfo:");
		System.out.println(Functions.fixedLength("-pNr <projrctNumber>", 30)+"The project which you want the time to be taken from (-pNr b2011098)");
		System.out.println(Functions.fixedLength("-email <your@email>", 30)+"Your email (-email john.doe@scilifelab.se)");
		System.out.println(Functions.fixedLength("-time <time>", 30)+"The time you will (-time 1-2:00:00)");
		System.out.println(Functions.fixedLength("-program <program>",30)+"Available programs are listed below.(-program blast)");
		System.out.println("");
		System.out.println("Other flags for SBATCHinfo:");
		System.out.println(Functions.fixedLength("-core <numberOfCores>", 30)+" Default is one core, i.e. 8 GB RAM.");
		System.out.println(Functions.fixedLength("-memory <NrOfGB>", 30)+" Allocated memory . Default is 8, i.e a core. (-memory 24)");
		System.out.println(Functions.fixedLength("-milou", 30)+" Run on the milou cluster (Default");
		System.out.println(Functions.fixedLength("-kalkyl", 30)+" Run on the kalkyl cluster");

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

		core = Functions.getInt(T,"-core", 1);
		memory = Functions.getInt(T,"-memory", 8);
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

		if(memory <= 512)
			printSBATCHinfoCore(EW,directory,timestamp, String.valueOf(ID), program);
		else
			printSBATCHinfohalvan(EW,directory,timestamp, String.valueOf(ID), program);
	}



	public void printSBATCHinfo(ExtendedWriter EW, String directory,
			String timestamp, String ID, String program) {
		if(memory <= 512)
			printSBATCHinfoCore(EW,directory,timestamp, ID, program);
		else
			printSBATCHinfohalvan(EW,directory,timestamp, ID, program);
	}

	public int getNrOfCores(){
		if(milou)
			return Math.max(core, ((this.memory-1)/8)+1);
		else{
			System.out.println("no System have been called on uppmax " );
			return 1;
		}


	}

	
	public void printSHELLinfo(ExtendedWriter EW) {


		EW.println("#! /bin/bash -l");
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
			EW.println("#SBATCH --mail-type=FAIL");
			EW.println("#SBATCH --mail-user=" + email);
		}
		if(this.afterany != null){
			EW.print("#SBATCH -d afterany");
			if(this.afterany.size() < 80){ 
				for(int i = 0; i < this.afterany.size(); i++){
					EW.print(":"+afterany.get(i));
				}
				EW.println();
			}else{
				for(int i = this.afterany.size()-80; i < this.afterany.size(); i++){
					EW.print(":"+afterany.get(i));
				}
				EW.println();
			}
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

		int nrofnodes = (memory-1) / 32 + 1;
		System.out.println("Work will be placed on halvan");
		System.out.println("Memory allocated will be " +nrofnodes * 32 + " GB");
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
			EW.println("#SBATCH --mail-type=FAIL");
			EW.println("#SBATCH --mail-user=" + email);
		}
		if(this.afterany != null){
			EW.print("#SBATCH -d afterany");
			for(int i = 0; i < this.afterany.size(); i++){
				EW.print(":"+afterany.get(i));
			}
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


	public void printSBATCHscript(ExtendedWriter masterShellScript, 
			String outDir,String commandLine, ArrayList<String> tempOutFiles, ArrayList<String> finalOutFiles, String function) {
		try {
			String sbatchFileName = outDir + "/scripts/" + getTimeStamp() + "_"
					+ finalOutFiles.get(0) + "_" + function + ".sbatch";

			masterShellScript.println("sbatch "+sbatchFileName);
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(sbatchFileName));
			printSBATCHinfo(EW, outDir, getTimeStamp(), finalOutFiles.get(0), function);
			EW.println();
			EW.println();
			EW.println("cd " + outDir);
			EW.println();
			EW.println();
			EW.println(commandLine);
			EW.println();
			for(int i = 0; i < tempOutFiles.size(); i++){
				EW.println("mv " + tempOutFiles.get(i) + " "+ finalOutFiles.get(i));
			}
			EW.println();
			EW.println("wait");

			EW.flush();
			EW.close();

		} catch (Exception E) {
			E.printStackTrace();
		}
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



	public int getMemory() {
		if(milou)
			return Math.max(core*8-1, this.memory);
		else{
			System.out.println("no System have been called on uppmax " );
			return 8;
		}
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
			this.memory = (int)Math.ceil((double)memory/(double)32);
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


	public void addAfterAny(ArrayList<Integer>projNumbers){
		this.afterany = projNumbers;
	}




}
