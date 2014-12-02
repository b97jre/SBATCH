// Asumes that the non-GATK steps have been made. For those steps please see Picard.java.
package SBATCHscripts;

import general.ExtendedReader;
import general.ExtendedWriter;
import general.Functions;
import general.IOTools;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import Sequence.FastaSequences;


public class GATKphaseSNPs {

	String time;
	String projectDir;
	String suffix;
	String GATKdir;
	String vcfFile;
	String pedigreeFile;
	int cutoff;





	boolean respectAlreadyPhased;

	String directory;


	String Reference;



	public void help(){



		System.out.println("Mandatory values for GATK phaseSNPs:");
		System.out.println(Functions.fixedLength("-i <inDirectory> ", 50)+"Directory where all files with suffix under will be used for one vcf file");
		System.out.println(Functions.fixedLength("-R <ReferenceFile.fa>", 50)+"Reference fasta file");
		System.out.println(Functions.fixedLength("-DNAPhasedVcfFile <vcfFile.vcf>", 50)+"Vcf file with phased SNPs");
//		System.out.println(Functions.fixedLength("-BedFile <vcfFile.bed>", 50)+"Vcf file with phased SNPs");
		
		System.out.println();


		System.out.println();
		System.out.println();

	}

	public boolean addParameters(Hashtable<String, String> T) {

	
		this.directory = Functions.getValue(T, "-i");
		File ref = new File(this.directory);
		this.directory = ref.getAbsolutePath();

		this.Reference = Functions.getValue(T, "-R");
		ref = new File(this.Reference);
		this.Reference = ref.getAbsolutePath();

		vcfFile = Functions.getValue(T, "-DNAPhasedVcfFile");
		ref = new File(this.vcfFile);
		vcfFile = ref.getAbsolutePath();
		
		suffix =  Functions.getValue(T, "-suffix",null);
		pedigreeFile =  Functions.getValue(T, "-ped",null);


		respectAlreadyPhased = false;
		if (T.containsKey("-respectPhaseInInput"))
			respectAlreadyPhased = true;


		this.cutoff = Functions.getInt(T, "-cutoff", -1);

		return true;
	}

	public boolean checkParameters(Hashtable<String, String> T) {
		boolean allPresent = true;

		GATKdir = Functions.getValue(T, "-GATKDir",
				"/bubo/sw/apps/bioinfo/GATK/2.5.2/");
		if (!T.containsKey("-i")){System.out.println("-i is missing" ); allPresent = false;}
		if (!T.containsKey("-R")){System.out.println("-R is missing" ); allPresent = false;}
		if(!T.containsKey("-suffix")){System.out.println("-suffix is missing" ); allPresent = false;}		
		if(!T.containsKey("-DNAPhasedVcfFile")){System.out.println("-DNAPhasedVcfFile is missing" ); allPresent = false;}


		if(allPresent) return true;

		help();
		return false;
	}





	public GATKphaseSNPs() {
		projectDir = time = null;
	}









	public ArrayList<String> getFilesWithSuffix(String dir) {
		ArrayList<String> fileNames = new ArrayList<String>();
		ArrayList<String> subDirs = IOTools.getDirectories(dir);
		for (int i = 0; i < subDirs.size(); i++) {
			ArrayList<String> newFileNames = getFilesWithSuffix(dir + "/"
					+ subDirs.get(i));
			if (!newFileNames.isEmpty()) {
				fileNames = Functions.mergeLists(fileNames, newFileNames);
			}
		}
		ArrayList<String> newFileNames = IOTools.getSequenceFilesFullPath(dir,
				suffix);
		if (!newFileNames.isEmpty()) {
			fileNames = Functions.mergeLists(fileNames, newFileNames);
		}

		return fileNames;
	}







	public void GATKPhaseSNPs(SBATCHinfo sbatch) {

		/*
		 * for each sample (asumed to be in same dir)
		 * 
		 * lanes.bam <- merged lane.bams for sample dedup.bam <-
		 * MarkDuplicates(lanes.bam) realigned.bam <- realign(dedup.bam) [with
		 * known sites included if available] recal.bam <- recal(realigned.bam)
		 * sample.bam <- recal.bam
		 */


		if(this.suffix == null && this.pedigreeFile == null){
			System.out.println("Either -suffix or -ped must be set");
			help();
			return;
		}


		String outDir = this.directory;

		if (!IOTools.isDir(outDir + "/reports"))
			IOTools.mkDir(outDir + "/reports");
		if (!IOTools.isDir(outDir + "/scripts"))
			IOTools.mkDir(outDir + "/scripts");
		try {



			if(this.pedigreeFile != null){
				String sbatchFileName = outDir + "/scripts/" + sbatch.getTimeStamp() + "_"
						+"phaseSNPs_GATK.sbatch";
				ExtendedWriter EW = new ExtendedWriter(new FileWriter(
						sbatchFileName));
				sbatch.printSBATCHinfo(EW, outDir, sbatch.getTimeStamp(), 0, "GATK_phaseSNPs");

				EW.println("cd " + outDir);

				String VCFfile = this.vcfFile.substring(0,this.vcfFile.indexOf(".vcf"));

				if(VCFfile.lastIndexOf(".") == VCFfile.length()){
					VCFfile = VCFfile.substring(0,VCFfile.length()-1);
				}

				String commandLine = PhaseByTransmission(
						sbatch.getMemoryPadded(), this.GATKdir,
						this.pedigreeFile, this.Reference ,
						VCFfile+".phaseByTransmission.vcf",VCFfile+".vcf",
						respectAlreadyPhased);
				System.out.println(commandLine);
				EW.println(commandLine);
				VCFfile = VCFfile+".phaseByTransmission";
				this.respectAlreadyPhased = true;

				EW.flush();
				EW.close();


			}
			else{
				GATKPhaseReadBackedPhasing(sbatch);
			}

		} catch (Exception E) {
			E.printStackTrace();
		}
	}



	public void GATKPhaseReadBackedPhasing(SBATCHinfo sbatch) {

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

			ExtendedWriter superShellScript = sbatch.printShellInfoSTART("GATK_SNPphase",outDir);


			String VCFfile = new File(this.vcfFile).getName();
			VCFfile = VCFfile.substring(0,VCFfile.indexOf(".vcf"));

			if(VCFfile.lastIndexOf(".") == VCFfile.length()){
				VCFfile = VCFfile.substring(0,VCFfile.length()-1);
			}
			String VCFfolder = new File(this.vcfFile).getParent();



			ArrayList<Integer> lengths = FastaSequences.getLengths(Reference);
			ArrayList<String> names = FastaSequences.getNames(Reference);
			ArrayList<String> fileNames = getFilesWithSuffix(outDir);

			System.out.println("Files considered for final vcf file:");
			for (int i = 0; i < fileNames.size(); i++) {
				System.out.println(fileNames.get(i));
			}
			System.out.println();
			System.out.println();
			int count = 0;
			int pointer = 0;
			ArrayList<String> VCFfiles = new ArrayList<String>();

			while (pointer < names.size()){

				int size = lengths.get(pointer);
				ArrayList<String> SubNames = new ArrayList<String>();
				String interval = names.get(pointer) + ":" + 1
						+ "-" + size;
				SubNames.add(interval);					
				pointer++;
				while (pointer < lengths.size() && size < cutoff ) {
					size += lengths.get(pointer);
					interval = names.get(pointer) + ":" + 1
							+ "-" + lengths.get(pointer);
					SubNames.add(interval);					
					pointer++;
				}




				ArrayList<String> tempFiles = new ArrayList<String>();
				ArrayList<String> finalFiles = new ArrayList<String>();

				ArrayList<String> tempIdxFiles = new ArrayList<String>();
				ArrayList<String> finalIdxFiles = new ArrayList<String>();

				tempFiles.add(outDir+"/"+VCFfile
						+ ".ReadPhased."+count+".vcf.tmp");
				finalFiles.add(outDir+"/"+VCFfile
						+ ".ReadPhased."+count+".vcf");
				VCFfiles.add(VCFfile+ ".ReadPhased."+count+".vcf");
				tempIdxFiles.add(outDir+"/"+VCFfile
						+ ".ReadPhased."+count+".vcf.tmp.idx");
				finalIdxFiles.add(outDir+"/"+VCFfile
						+ ".ReadPhased."+count+".vcf.idx");



				superShellScript.println("sbatch "+sbatch.getSbatchFileName("GATK_ReadBacked_Phasing", outDir,count));

				ExtendedWriter EW = sbatch.printSBATCHInfoSTART("GATK_ReadBacked_Phasing", outDir,count); 

				EW.println("cd "+outDir);

				if(cutoff == -1 ){
					SubNames = null;
					pointer = lengths.size(); 
				}

				String commandLine = ReadBackedPhasing(sbatch.getMemoryPadded(),
						this.GATKdir, fileNames, this.Reference, tempFiles.get(0), VCFfolder+"/"+VCFfile+ ".vcf", 10.0,
						respectAlreadyPhased,SubNames);
				EW.println(commandLine);
				EW.println();
				EW.println("# Moving vcf files");
				sbatch.mvTempFiles(EW, tempFiles,finalFiles);
				EW.println();
				EW.println("# Moving idx files");
				sbatch.mvTempFiles(EW, tempIdxFiles,finalIdxFiles);



				EW.flush();
				EW.close();
				//System.out.println(commandLine);
				count++;
			}
			
			String shellScriptFile = sbatch.printShellInfoSTOP(superShellScript,"GATK_SNPphase",outDir);
			
			ArrayList<Integer> jobids = sbatch.startSbatchScripts(shellScriptFile);
			sbatch.addAfterAny(jobids);
			String MergeSbatchScriptFile = Merge.getSBATCHscript(sbatch, outDir, outDir, VCFfiles, "GATK", 0);
			Integer jobid = sbatch.startSbatchScript(MergeSbatchScriptFile);

		} catch (Exception E) {
			E.printStackTrace();
		}
	}

	
	





	public static String ReadBackedPhasing(int memory, String GATKDir,
			ArrayList<String> bamFiles, String reference, String outFileVCF,
			String knownSNPVCF, double phaseQualityThresh,
			boolean respectAlreadyPhased, ArrayList<String> contigs) {

		/*
		 * how to write the call is taken from
		 * http://www.broadinstitute.org/gatk
		 * /gatkdocs/org_broadinstitute_sting_gatk_walkers_phasing_ReadBackedPhasing
		 * .html#--respectPhaseInInput
		 * 
		 * -jar GenomeAnalysisTK.jar -T ReadBackedPhasing -R reference.fasta -I
		 * reads.bam --variant SNPs.vcf -L SNPs.vcf -o phased_SNPs.vcf
		 * --phaseQualityThresh 20.0
		 */

		String commandLine = "java -Xmx" + memory + "g -jar " + GATKDir
				+ "/GenomeAnalysisTK.jar " 
				+ "-R " + reference + " "
				+ "-T ReadBackedPhasing " 
				+ "--variant  " + knownSNPVCF + " "
				+ "-o " + outFileVCF
				+ " --phaseQualityThresh " + phaseQualityThresh;
		for(int i = 0; i < bamFiles.size();i++){
			commandLine += " -I " + bamFiles.get(i);
		}
		if (respectAlreadyPhased)
			commandLine += " -respectPhaseInInput ";
		if (contigs != null) {
			for (int i = 0; i < contigs.size(); i++) {
				commandLine += " -L " + contigs.get(i);
			}
		}
		return commandLine;

	}

	public static String PhaseByTransmission(int memory, String GATKDir,
			String pedigreeFile, String reference, String outFileVCF,
			String knownSNPVCF,
			boolean respectAlreadyPhased) {

		/*
		 * how to write the call is taken from
		 * http://www.broadinstitute.org/gatk
		 * /gatkdocs/org_broadinstitute_sting_gatk_walkers_phasing_ReadBackedPhasing
		 * .html#--respectPhaseInInput
		 * 
	 java -Xmx2g -jar GenomeAnalysisTK.jar \
	   -R ref.fasta \
	   -T PhaseByTransmission \
	   -V input.vcf \
	   -ped input.ped \
	   -o output.vcf

		 */

		String commandLine = "java -Xmx" + memory + "g -jar " + GATKDir
				+ "/GenomeAnalysisTK.jar " + "-R " + reference + " "
				+ "-T PhaseByTransmission " + "-V  " + knownSNPVCF + " "
				+ "-o " + outFileVCF
				+ " -ped " + pedigreeFile;
		if (respectAlreadyPhased)
			commandLine += " -respectPhaseInInput ";

		return commandLine;

	}



}
