// Asumes that the non-GATK steps have been made. For those steps please see Picard.java.
package SBATCHscripts;

import general.ExtendedWriter;
import general.Functions;
import general.IOTools;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;



public class PersonalizedSkellyFormat {


	String time;
	String projectDir;
	String suffix;


	String directory;
	String HTStoolsPath; 

	String Reference;
	String personalizedReference;
	String vcfFile;
	String AnnotationFile;
	String sample;
	String bamFile;
	String motherSep;
	String fatherSep;


	public void help(){

		System.out.println("Mandatory values for phasing RNA reads based on DNA SNPs:");
		System.out.println(Functions.fixedLength("-R <ReferenceFile.fa>", 100)+"non-personalized Reference fasta file");
		System.out.println(Functions.fixedLength("-phasedVcfFile <vcfFile.vcf>", 100)+"Vcf file with phased SNPs and ReferenceFile annotation");
		System.out.println(Functions.fixedLength("-personalizedReference <Personalized ReferenceFile.fa>", 100)+"personalized Reference fasta file based on phasedVcfFile (two merged Haplo Genomes)");
		System.out.println(Functions.fixedLength("-bam <mappedReads.bam>", 100)+"bamFile with reads mapped to personalized Reference");
		System.out.println(Functions.fixedLength("-sample <Sample name>", 100)+"SampleName in VCF file that created personalized vcf File");
		System.out.println(Functions.fixedLength("-fatherSuffix <Suffix of contigNames for father in personalized Genome>", 100)+"Only get skelly for this sample");
		System.out.println(Functions.fixedLength("-motherSuffix <Suffix of contigNames for mother in personalized Genome>", 100)+"Only get skelly for this sample");
		
		System.out.println(Functions.fixedLength("-annotation <annotation.bed>", 100)+"Bed file with annotation");


		System.out.println();
		System.out.println();

	}

	public boolean addParameters(Hashtable<String, String> T) {


		this.HTStoolsPath =Functions.getValue(T, "-HTStoolsPath", "/glob/johanr/bin/HTStools.jar");


		this.directory = Functions.getValue(T, "-i", IOTools.getCurrentPath());


		this.Reference = Functions.getValue(T, "-R");
		this.personalizedReference = Functions.getValue(T, "-personalizedReference");
		File ref = new File(this.Reference);
		this.Reference = ref.getAbsolutePath();
		this.bamFile = Functions.getValue(T, "-bam");

		vcfFile = Functions.getValue(T, "-phasedVcfFile");
		ref = new File(this.vcfFile);
		vcfFile = ref.getAbsolutePath();

		this.AnnotationFile = Functions.getValue(T,"-annotation");
		ref = new File(this.AnnotationFile);
		AnnotationFile = ref.getAbsolutePath();


		this.sample = 	Functions.getValue(T, "-sample", null);
		this.fatherSep = Functions.getValue(T,"-fatherSuffix");
		this.motherSep = Functions.getValue(T,"-motherSuffix");

		
		
		
		return true;
	}

	public boolean checkParameters(Hashtable<String, String> T) {
		boolean allPresent = true;
		System.out.println();
		System.out.println();
		System.out.println("Checking parameters");
		System.out.println();

		if (!T.containsKey("-phasedVcfFile")){System.out.println("-phasedVcfFile is missing"); allPresent = false;}
		if (!T.containsKey("-R")){System.out.println("-R is missing" ); allPresent = false;}
		if (!T.containsKey("-personalizedReference")){System.out.println("-personalizedReference is missing" ); allPresent = false;}
		if(!T.containsKey("-annotation")){System.out.println("-annotation is missing" ); allPresent = false;}
		if(!T.containsKey("-sample")){System.out.println("-sample is missing" ); allPresent = false;}
		if(!T.containsKey("-fatherSuffix")){System.out.println("-fatherSuffix is missing" ); allPresent = false;}
		if(!T.containsKey("-motherSuffix")){System.out.println("-motherSuffix is missing" ); allPresent = false;}

		
		
		System.out.println();
		System.out.println();

		if(allPresent){
			System.out.println("All parameters are set");

			return true;
		}
		System.out.println("Some parameters are missing se below for all potential parameters");
		System.out.println();
		System.out.println();
		System.out.println();

		help();
		return false;
	}

	public PersonalizedSkellyFormat() {
		projectDir = time = null;
	}




	public void getSkellyFormat(SBATCHinfo sbatch) {

		/*
		 * for each sample (asumed to be in same dir)
		 * 
		 * lanes.bam <- merged lane.bams for sample dedup.bam <-
		 * MarkDuplicates(lanes.bam) realigned.bam <- realign(dedup.bam) [with
		 * known sites included if available] recal.bam <- recal(realigned.bam)
		 * sample.bam <- recal.bam
		 */

		String outDir = this.directory;

		if (!IOTools.isDir(outDir + "/reports"))
			IOTools.mkDir(outDir + "/reports");
		if (!IOTools.isDir(outDir + "/scripts"))
			IOTools.mkDir(outDir + "/scripts");
		try {

			if(this.vcfFile != null){
				String sbatchFile =  getSkellyFormat(sbatch, 0);
				System.out.println("Information written to this file");
				System.out.println(sbatchFile);
				System.out.println("The sbatchscript will  be submitted to Uppmax");
				sbatch.startSbatchScript(sbatchFile);
			}
		} catch (Exception E) {
			E.printStackTrace();
		}
	}


	public String getSkellyFormat(SBATCHinfo sbatch,  int count) {
		try{
			String sbatchFileName = sbatch.getSbatchFileName("personalizedSkellyFormat", this.directory,count);
			ExtendedWriter EW = sbatch.printSBATCHInfoSTART("personalizedSkellyFormat", this.directory,count); 
			EW.println("cd "+this.directory);
			EW.println();
			String bamFile_unique = Samtools.bam2bam(EW, bamFile, -1, -1, 250, true, true, true, false, true);
			EW.println();
			String mpileupFile = Samtools.mpileUp(EW, this.personalizedReference, bamFile_unique, "-BQ0 -d10000000 ");
			EW.println();
			String personalizedVCFfile = parseMpileUpToVCF(EW,sbatch.getMemoryPadded(), this.HTStoolsPath,
					this.vcfFile,this.Reference,mpileupFile,
					this.sample, this.motherSep,this.fatherSep);
			EW.println(SkellyFormat.SkellyFormatFile(sbatch.getMemoryPadded(), this.HTStoolsPath,personalizedVCFfile , this.Reference, this.AnnotationFile, this.sample, false));  
			EW.println();
			EW.println();
			EW.flush();
			EW.close();
			return sbatchFileName;
		} catch(Exception E){E.printStackTrace();}

		return null;


	}
	
	
	public static String parseMpileUpToVCF(ExtendedWriter EW,int memory, String HTStoolsPath,
			String phasedVCF, String reference, String mpileupFile,
			String sample, String mother, String father ){
		EW.println(parseMpileUpToVCF(memory, HTStoolsPath,
				phasedVCF, reference,mpileupFile,
				sample, mother,father));
		String mpName = new File(mpileupFile).getName();
		String vcfFileName = new File(phasedVCF).getName();
		
		return mpName+"_"+vcfFileName;
		
		
		
	}

	public static String parseMpileUpToVCF(int memory, String HTStoolsPath,
			String phasedVCF, String reference, String mpileupFile,
			String sample, String mother, String father) {
		String commandLine = "java -Xmx" + memory + "g -jar " + HTStoolsPath
				+ " -p databases -parseMpileUpToVCF "

				+ "-R " + reference + " "
				+ "-sample "+ sample+" "
				+ "-transmissionPhasedVCF  " + phasedVCF+" "
				+ "-mpileupFile  " + mpileupFile+" "
				+ "-mother  " + mother+" "
				+ "-father  " + father;
		return commandLine;
		
	}

	
}