// Asumes that the non-GATK steps have been made. For those steps please see Picard.java.
package SBATCHscripts;

import general.ExtendedWriter;
import general.Functions;
import general.IOTools;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;



public class SkellyFormat {

	String time;
	String projectDir;
	String suffix;


	String directory;
	String HTStoolsPath; 

	String Reference;
	String vcfFile;
	String AnnotationFile;
	String sample;

	boolean readPhased;

	public void help(){

		System.out.println("Mandatory values for phasing RNA reads based on DNA SNPs:");
		System.out.println(Functions.fixedLength("-R <ReferenceFile.fa>", 50)+"Reference fasta file");
		System.out.println(Functions.fixedLength("-phasedVcfFile <vcfFile.vcf>", 50)+"Vcf file with phased SNPs");
		System.out.println(Functions.fixedLength("-annotation <annotation.bed>", 50)+"Bed file with annotation");
		System.out.println();
		System.out.println("Optional values:");
		System.out.println(Functions.fixedLength("-sample <Sample name>", 50)+"Only get skelly for this sample");
		System.out.println(Functions.fixedLength("-readPhased", 50)+"(default False)If phased VCF file is readPhased");
		System.out.println(Functions.fixedLength("-transmissionPhased", 50)+"(default True)If phased VCF file is readPhased ");
		System.out.println(Functions.fixedLength("", 50)+"transmissionPhased != readPhased");

		System.out.println();
		System.out.println();

	}

	public boolean addParameters(Hashtable<String, String> T) {


		this.HTStoolsPath =Functions.getValue(T, "-HTStoolsPath", "/glob/johanr/bin/HTStools.jar");
		

		this.directory = Functions.getValue(T, "-i", IOTools.getCurrentPath());
		
		
		this.Reference = Functions.getValue(T, "-R");
		File ref = new File(this.Reference);
		this.Reference = ref.getAbsolutePath();

		vcfFile = Functions.getValue(T, "-phasedVcfFile");
		ref = new File(this.vcfFile);
		vcfFile = ref.getAbsolutePath();

		this.AnnotationFile = Functions.getValue(T,"-annotation");
		ref = new File(this.AnnotationFile);
		AnnotationFile = ref.getAbsolutePath();


		this.sample = 	Functions.getValue(T, "-sample", null);
		if(T.containsKey("-readPhased")) this.readPhased = true;
		else this.readPhased = false;

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
		if(!T.containsKey("-annotation")){System.out.println("-annotation is missing" ); allPresent = false;}
		if (!T.containsKey("-transmissionPhased") &&  !T.containsKey("-readPhased")){ System.out.println("either -transmissionPhased or -readPhased must be present");allPresent = false;}
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

	public SkellyFormat() {
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
				String sbatchFile =  getSkellyFormat(sbatch, this.vcfFile, 0);
				System.out.println("Information written to this file");
				System.out.println(sbatchFile);
				System.out.println("The sbatchscript will  be submitted to Uppmax");
				sbatch.startSbatchScript(sbatchFile);
			}
		} catch (Exception E) {
			E.printStackTrace();
		}
	}


	public String getSkellyFormat(SBATCHinfo sbatch, String phasedVCFfile, int count) {
		try{
			String sbatchFileName = sbatch.getSbatchFileName("RNAreadPhasing", this.directory,count);
			ExtendedWriter EW = sbatch.printSBATCHInfoSTART("RNAreadPhasing", this.directory,count); 
			EW.println("cd "+this.directory);
			EW.println();
			EW.println();
			EW.println(SkellyFormat.SkellyFormatFile(sbatch.getMemoryPadded(), this.HTStoolsPath,phasedVCFfile , this.Reference, this.AnnotationFile, this.sample, this.readPhased));  
			EW.println();
			EW.println();
			EW.flush();
			EW.close();
			return sbatchFileName;
		} catch(Exception E){E.printStackTrace();}

		return null;


	}


	public static String SkellyFormatFile(int memory, String HTStoolsPath,
			String phasedVCF, String reference, String AnnotationFile,
			String sample, boolean readPhased) {
		String commandLine = "java -Xmx" + memory + "g -jar " + HTStoolsPath
				+ " -p databases -SkellyFormat "
				+ "-R " + reference + " "
				+ "-BED "+ AnnotationFile+" "
				+ "-VCF  " + phasedVCF;
		if (readPhased){
			commandLine += " -readPhased ";
		}else{
			commandLine += " -transmissionPhased ";
		}


		return commandLine;
	}

}