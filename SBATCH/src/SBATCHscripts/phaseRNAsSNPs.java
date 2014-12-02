// Asumes that the non-GATK steps have been made. For those steps please see Picard.java.
package SBATCHscripts;

import general.ExtendedWriter;
import general.Functions;
import general.IOTools;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;



public class phaseRNAsSNPs {

	String time;
	String projectDir;
	String suffix;


	String directory;
	String HTStoolsPath; 

	String Reference;
	String vcfFile;
	String RNAvcfFile;
	String AnnotationFile;
	String sample;

	boolean readPhased;

	public void help(){

		System.out.println("Mandatory values for phasing RNA reads based on DNA SNPs:");
		System.out.println(Functions.fixedLength("-R <ReferenceFile.fa>", 50)+"Reference fasta file");
		System.out.println(Functions.fixedLength("-PhasedVcfFile <vcfFile.vcf>", 50)+"Vcf file with phased SNPs");
		System.out.println(Functions.fixedLength("-annotation <annotation.bed>", 50)+"Bed file with annotation");
		System.out.println(Functions.fixedLength("-sample <Sample name>", 50)+"Has to be the same in both the RNA and the DNA VCFfile");
		System.out.println();
		System.out.println(Functions.fixedLength("-RNAVcfFile <vcfFile.vcf>", 50)+"Vcf file with SNPs");
		System.out.println(Functions.fixedLength("OR", 50)+"if RNAVcfFile is not set then identify all files with suffix in  folder and run analysis on them");	
		System.out.println(Functions.fixedLength("-i <inDirectory> ", 50)+"Directory where all files with suffix under will be used for one vcf file");
		System.out.println(Functions.fixedLength("-suffix <SUFFIX>", 50)+"suffix");

		System.out.println();

		System.out.println(Functions.fixedLength("-readPhased", 50)+"(default False)If phased VCF file is readPhased");
		System.out.println(Functions.fixedLength("-transmissionPhased", 50)+"(default True)If phased VCF file is transmission phased ");
		System.out.println(Functions.fixedLength("", 50)+"transmissionPhased != readPhased");

		System.out.println();
		System.out.println();

	}

	public boolean addParameters(Hashtable<String, String> T) {


		this.HTStoolsPath =Functions.getValue(T, "-HTStoolsPath", "/glob/johanr/bin/HTStools.jar");


		this.RNAvcfFile = Functions.getValue(T, "-RNAVcfFile", null);

		this.directory = Functions.getValue(T, "-i", IOTools.getCurrentPath());
		File ref = new File(this.directory);
		this.directory = ref.getAbsolutePath();


		this.suffix = Functions.getValue(T, "-suffix", IOTools.getCurrentPath());



		this.Reference = Functions.getValue(T, "-R");
		ref = new File(this.Reference);
		this.Reference = ref.getAbsolutePath();

		vcfFile = Functions.getValue(T, "-PhasedVcfFile");
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

		if (!T.containsKey("-RNAVcfFile") &&  !T.containsKey("-i")){System.out.println("either -i and -suffix or -RNAVcfFile must be present"); allPresent = false;}
		if (!T.containsKey("-R")){System.out.println("-R is missing" ); allPresent = false;}
		if(!T.containsKey("-PhasedVcfFile")){System.out.println("-PhasedVcfFile" ); allPresent = false;}
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

	public phaseRNAsSNPs() {
		projectDir = time = null;
	}

	public ArrayList<String> getFilesWithSuffix(String dir) {
		ArrayList<String> fileNames = new ArrayList<String>();
		ArrayList<String> subDirs = IOTools.getDirectories(dir);

		if (!subDirs.isEmpty()) {
			for (int i = 0; i < subDirs.size(); i++) {
				ArrayList<String> newFileNames = getFilesWithSuffix(dir + "/"
						+ subDirs.get(i));
				if (!newFileNames.isEmpty()) {
					fileNames = Functions.mergeLists(fileNames, newFileNames);
				}
			}
		}
		ArrayList<String> newFileNames = IOTools.getSequenceFilesFullPath(dir,suffix);
		if (!newFileNames.isEmpty()) {
			fileNames = Functions.mergeLists(fileNames, newFileNames);
		}

		return fileNames;
	}



	public void phaseRNAs(SBATCHinfo sbatch) {

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

			if(this.RNAvcfFile != null){
				String sbatchFile =  phaseRNA(sbatch, this.RNAvcfFile, 0);
				System.out.println("Information written to this file");
				System.out.println(sbatchFile);
				//sbatch.startSbatchScript(sbatchFile);
			}else{

				String shellScriptFile = phaseRNAfiles(sbatch);

				ArrayList<Integer> jobids = sbatch.startSbatchScripts(shellScriptFile);
			}
		} catch (Exception E) {
			E.printStackTrace();
		}
	}


	public String phaseRNAfiles(SBATCHinfo sbatch) {


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

			ExtendedWriter superShellScript = sbatch.printShellInfoSTART("phaseRNAs",outDir);
			ArrayList<String> fileNames = getFilesWithSuffix(outDir);

			System.out.println("Files that will be phased:");
			for (int i = 0; i < fileNames.size(); i++) {
				System.out.println(fileNames.get(i));
			}
			System.out.println();
			System.out.println();
			for (int i = 0; i < fileNames.size(); i++) {
				superShellScript.println(phaseRNA(sbatch, fileNames.get(i), i) );
			}
			String shellScriptFile = sbatch.printShellInfoSTOP(superShellScript,"phaseRNAs",outDir);

			return shellScriptFile;
		} catch (Exception E) {
			E.printStackTrace();
		}

		return null;
	}







	public String phaseRNA(SBATCHinfo sbatch, String RNAVCFfile, int count) {
		try{
			String sbatchFileName = sbatch.getSbatchFileName("RNAreadPhasing", this.directory,count);
			ExtendedWriter EW = sbatch.printSBATCHInfoSTART("RNAreadPhasing", this.directory,count); 
			EW.println("cd "+this.directory);
			EW.println();
			EW.println();
			EW.println(phaseRNAsSNPs.phaseRNA(sbatch.getMemoryPadded(), this.HTStoolsPath, RNAVCFfile, this.Reference, this.vcfFile, this.sample, this.readPhased)); 
			File ref = new File(RNAVCFfile.substring(0,RNAVCFfile.length()-3)+"phased.vcf");

			EW.println();
			EW.println();
			EW.println(SkellyFormat.SkellyFormatFile(sbatch.getMemoryPadded(), this.HTStoolsPath, ref.getName(), this.Reference, this.AnnotationFile, this.sample, this.readPhased));  
			EW.println();
			EW.println();
			EW.flush();
			EW.close();
			return sbatchFileName;
		} catch(Exception E){E.printStackTrace();}

		return null;


	}


	public static String phaseRNA(int memory, String HTStoolsPath,
			String RNAVCFFile, String reference, String phasedVCF,
			String sample, boolean readPhased) {

		/*
		 * how to write the call is taken from
		 * java -Xmx6g -jar /glob/johanr/bin/HTStools.jar  
		 * -p databases -phaseVCFfile 
		 * -R /proj/b2012122/private/Cr_reference/assembly/Crubella_183.fa 
		 * -VCF Intra8-2_DNA.ReadPhased.heterozygous.filteredSNPs.intersect.vcf 
		 * -unphased RNAsamples/Intra8_2/Intra8_2.1.L.AGTCAA.L007.Cr_reference.AddOrReplaceReadGroups.markDuplicates.ReassignOneMappingQuality.bam.Genotype.vcf 
		 * -readPhased
		 */

		String commandLine = "java -Xmx" + memory + "g -jar " + HTStoolsPath
				+ " -p databases -phaseVCFfile "
				+ "-R " + reference + " "
				+ "-VCF  " + phasedVCF + " "
				+ "-unphased " + RNAVCFFile;
		if (readPhased){
			commandLine += " -readPhased ";
		}else{
			commandLine += " -transmissionPhased ";
		}
		if (sample != null){
			commandLine += " -sample "+ sample;
		}

		return commandLine;

	}



}