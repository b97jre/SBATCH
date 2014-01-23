// Asumes that the non-GATK steps have been made. For those steps please see Picard.java.
package SBATCHscripts;

import general.ExtendedReader;
import general.ExtendedWriter;
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
import java.util.Map.Entry;

import SBATCHscripts.GeneralPairFile.Programs;
import Sequence.FastaSequences;

public class GATKphase2 {

	String time;
	String projectDir;
	String suffix;
	String prefix;
	String GATKdir;
	String knownSNPVCF;
	String knownIndelVCF;

	int cutoff;
	int padding;
	String job;
	
	String directory;

	boolean PRIORITIZE;
	boolean HaploTypeCaller;
	
	boolean rerun;
	boolean merge;
	String Reference;

	
	
	public void help(){



		System.out.println("Mandatory values for GATK phase2:");
		System.out.println(Functions.fixedLength("-i <inDirectory> ", 50)+"Directory where all files with suffix under will be used for one vcf file");
		System.out.println(Functions.fixedLength("-R <ReferenceFile.fa>", 50)+"Reference fasta file");
		System.out.println(Functions.fixedLength("-T <UnifiedGenotyper|HaplotypeCaller>", 50)+"Which program to use. Has to be either UnifiedGenotyper or HaplotypeCaller see GATK for more info");
		System.out.println(Functions.fixedLength("-suffix", 50)+"End of bamfiles that you will process");
		System.out.println(Functions.fixedLength("-prefix", 50)+"Prefix of vcf files when merging many files ONLY mandatory when -merge flag is called");
		System.out.println();

		
		
		System.out.println("Optional values:");
		
		System.out.println(Functions.fixedLength("-knownSNPs <KnownSNP.vcf>", 50)+"Vcf file with known SNPs");
		System.out.println(Functions.fixedLength("-knownIndels <KnownIndels.vcf>", 50)+"Vcf file with known indels");
		System.out.println(Functions.fixedLength("-targetIntervals <targetIndelsFile>", 50)+"targetIntervals file in format according to GATK");
		System.out.println(Functions.fixedLength("-split <size>", 50)+"Split up reference so that they can be runned in parrallel.");
		System.out.println(Functions.fixedLength("", 50)+"Recomended size is 100 000 where each run takes less than an hour.");
		System.out.println(Functions.fixedLength("", 50)+"Default is -1,i.e. it is not split up");
		System.out.println();
		System.out.println(Functions.fixedLength("-padding <size>", 50)+"If split then this is the extra nucleotides added on each side when doing the comparision. Default is 100");
		System.out.println(Functions.fixedLength("-merge", 50)+"post processing after the files have been split into different bins ");
			
		System.out.println();
		System.out.println();

	}

	public boolean addParameters(Hashtable<String, String> T) {

		boolean allPresent = true;
		GATKdir = Functions.getValue(T, "-GATKDir",
				"/sw/apps/bioinfo/GATK/2.5.2/");
		this.suffix = Functions.getValue(T,"-suffix");
		if (suffix.indexOf('.') == 0)
			suffix = suffix.substring(1);

		
		
		this.directory = Functions.getValue(T, "-i");
		File ref = new File(this.directory);
		this.directory = ref.getAbsolutePath();
		
		this.Reference = Functions.getValue(T, "-R");
		ref = new File(this.Reference);
		this.Reference = ref.getAbsolutePath();
		
		knownSNPVCF = Functions.getValue(T, "-knownSNPs");
		if(knownSNPVCF != null){
			ref = new File(this.knownSNPVCF);
			knownSNPVCF = ref.getAbsolutePath();
		}
		knownIndelVCF = Functions.getValue(T, "-knownIndels", null);
		if (IOTools.fileExists(IOTools.getCurrentPath() + "/" + knownIndelVCF))
			knownIndelVCF = IOTools.getCurrentPath() + "/" + knownIndelVCF;
		
		String caller = Functions.getValue(T, "-T");
		if(caller != null && caller.toUpperCase().compareTo("HaplotypeCaller".toUpperCase()) == 0)this.HaploTypeCaller = true;
		else this.HaploTypeCaller = false;
				
		
		if(T.containsKey("-merge")) this.merge = true;
		prefix = Functions.getValue(T, "-prefix");
		cutoff = Functions.getInt(T, "-split",- 1);
		padding = Functions.getInt(T, "-padding",100);

		return true;
	}
		
	public boolean checkParameters(Hashtable<String, String> T) {
		boolean allPresent = true;

		GATKdir = Functions.getValue(T, "-GATKDir",
				"/bubo/sw/apps/bioinfo/GATK/2.5.2/");
		if(!T.containsKey("-merge")){
			if (!T.containsKey("-i")){System.out.println("-i is missing" ); allPresent = false;}
			if (!T.containsKey("-R")){System.out.println("-R is missing" ); allPresent = false;}
			if(!T.containsKey("-suffix")){System.out.println("-suffix is missing" ); allPresent = false;}		
			if(!T.containsKey("-T")){System.out.println("-T is missing" ); allPresent = false;}
		}
		else{
			if (!T.containsKey("-i")){System.out.println("-i is missing" ); allPresent = false;}
			if (!T.containsKey("-R")){System.out.println("-R is missing" ); allPresent = false;}
			if(!T.containsKey("-suffix")){System.out.println("-suffix is missing" ); allPresent = false;}		
			if(!T.containsKey("-prefix")){System.out.println("-prefix is missing" ); allPresent = false;}		
			if(!T.containsKey("-split")){System.out.println("-cutoff is missing" ); allPresent = false;}		
		}
		
		
		if(allPresent) return true;
		
		help();
		return false;
	}

		
	
	
	
	public GATKphase2() {
		projectDir = time = null;
		merge = false;
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


	public void GATKMerge2() {
		String outDir = this.directory;

		/*
		 * for each sample (asumed to be in same dir)
		 * 
		 * lanes.bam <- merged lane.bams for sample dedup.bam <-
		 * MarkDuplicates(lanes.bam) realigned.bam <- realign(dedup.bam) [with
		 * known sites included if available] recal.bam <- recal(realigned.bam)
		 * sample.bam <- recal.bam
		 */

		if (!IOTools.isDir(outDir + "/reports"))
			IOTools.mkDir(outDir + "/reports");
		if (!IOTools.isDir(outDir + "/scripts"))
			IOTools.mkDir(outDir + "/scripts");
		try {

			ExtendedWriter EW = new ExtendedWriter(new FileWriter(outDir + "/"
					+ prefix + ".Final.vcf"));
			ExtendedReader ER2 = new ExtendedReader(new FileReader(outDir + "/"
					+ prefix + ".part0." + suffix));
			while (ER2.lookAhead() == '#')
				EW.println(ER2.readLine());
			ER2.close();
			// Write header info once

			ArrayList<Integer> lengths = FastaSequences.getLengths(Reference);
			ArrayList<String> names = FastaSequences.getNames(Reference);
			int count = 0;
			int pointer = 0;
			while (cutoff != -1 && pointer < names.size()) {
				// ...

				int size = lengths.get(pointer);
				if (size > cutoff + padding) {
					int start = 1;
					int stop = cutoff + padding;
					int first = 1;
					int last = cutoff;
					while (stop < size) {

						ExtendedReader ER = new ExtendedReader(new FileReader(
								outDir + "/" + prefix + ".part" + count + "."
										+ suffix));
						while (ER.lookAhead() == '#')
							ER.readLine();
						while (ER.more()) {
							String infoLine = ER.readLine();
							String[] info = infoLine.split("\t");
							int pos = Integer.parseInt(info[1]);
							if (pos >= first && pos < last)
								EW.println(infoLine);
						}
						ER.close();
						start = stop - 2 * padding;
						stop = stop + cutoff;
						first = last;
						last = first + cutoff;
						count++;
						System.out.println(count);
					}
					stop = size;
					last = size + 1;
					ExtendedReader ER = new ExtendedReader(new FileReader(
							outDir + "/" + prefix + ".part" + count + "."
									+ suffix));
					while (ER.lookAhead() == '#')
						ER.readLine();
					while (ER.more()) {
						String infoLine = ER.readLine();
						String[] info = infoLine.split("\t");
						int pos = Integer.parseInt(info[1]);
						if (pos >= first && pos < last)
							EW.println(infoLine);
					}
					ER.close();
					count++;
					System.out.println(count);
					pointer++;

				} else {
					pointer++;
					while (size < cutoff && pointer < names.size()) {
						size += lengths.get(pointer);
						pointer++;
					}
					ExtendedReader ER = new ExtendedReader(new FileReader(
							outDir + "/" + prefix + ".part" + count + "."
									+ suffix));
					while (ER.lookAhead() == '#')
						ER.readLine();
					while (ER.more()) {
						String infoLine = ER.readLine();
						EW.println(infoLine);
					}
					ER.close();
					count++;
					System.out.println(count);
				}

			}
			EW.flush();
			EW.close();
			ER2.close();

		} catch (Exception E) {
			E.printStackTrace();
		}
	}

	public void printSBATCHscript(ExtendedWriter masterShellScript, 
	SBATCHinfo sbatch, String outDir,String commandLine, String outFile, String function) {
		try {
			String sbatchFileName = outDir + "/scripts/" + sbatch.getTimeStamp() + "_"
			+ outFile + "_" + function + ".sbatch";

			
			masterShellScript.println("sbatch "+sbatchFileName);
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(sbatchFileName));
			sbatch.printSBATCHinfo(EW, outDir, sbatch.getTimeStamp(), outFile, function);
			EW.println();
			EW.println();
			EW.println("cd " + outDir);
			EW.println();
			EW.println();
			EW.println(commandLine);
			EW.println();
			EW.println("mv " + outFile + ".temp " + outFile);
			EW.println("mv " + outFile + ".temp.idx " + outFile + ".idx");
			EW.println();
			EW.println("wait");

			EW.flush();
			EW.close();

		} catch (Exception E) {
			E.printStackTrace();
		}
	}

	

	public void GATKPhase2(SBATCHinfo sbatch) {

		if(this.merge){
			GATKMerge2();
			return;
		}
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
			
			ExtendedWriter superShellScript = sbatch.printShellInfoSTART("GATK_phase2",outDir);
			
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
			while (cutoff != -1 && pointer < names.size()) {
				// ...

				int size = lengths.get(pointer);
				if (size > cutoff + padding) {
					int start = 1;
					int stop = cutoff + padding;
					while (stop < size) {
						ArrayList<String> SubNames = new ArrayList<String>();
						String interval = names.get(pointer) + ":" + start
								+ "-" + stop;
						SubNames.add(interval);
						String commandLine = "";
						String outFile = "";
						if (!HaploTypeCaller) {
							outFile = "Unified.output.raw.snps.indels.part"
									+ count + ".vcf";
							commandLine = UnifiedGenotypeCaller(sbatch.getMemoryPadded(),
									GATKdir, fileNames, Reference, outFile
											+ ".temp", knownSNPVCF, SubNames);
						} else {
							outFile = "Haplotype.output.raw.snps.indels.part"
									+ count + ".vcf";
							commandLine = HaplotypeCaller(sbatch.getMemoryPadded(), GATKdir,
									fileNames, Reference, outFile + ".temp",
									knownSNPVCF, SubNames);

						}
						if (!rerun
								|| !IOTools.fileExists(outDir
										+ "/output.raw.snps.indels.part"
										+ count + ".vcf.idx"))
							printSBATCHscript(superShellScript,sbatch,
									 outDir, commandLine, 
									outFile, "GATK_phase2");
						start = stop - 2 * padding;
						stop = stop + cutoff;
						count++;
					}

					ArrayList<String> SubNames = new ArrayList<String>();
					stop = size;
					String interval = names.get(pointer) + ":" + start + "-"
							+ stop;
					SubNames.add(interval);
					String commandLine = "";
					String outFile = "";
					if (!HaploTypeCaller) {
						outFile = "Unified.output.raw.snps.indels.part" + count
								+ ".vcf";
						commandLine = UnifiedGenotypeCaller(sbatch.getMemoryPadded(), GATKdir,
								fileNames, Reference, outFile + ".temp",
								knownSNPVCF, SubNames);
					} else {
						outFile = "Haplotype.output.raw.snps.indels.part"
								+ count + ".vcf";
						commandLine = HaplotypeCaller(sbatch.getMemoryPadded(), GATKdir,
								fileNames, Reference, outFile + ".temp",
								knownSNPVCF, SubNames);

					}
					if (!rerun || !IOTools.fileExists(outDir + "/" + outFile))
						printSBATCHscript(superShellScript, sbatch,
								 outDir, commandLine, 
								outFile, "GATK_phase2");
					count++;
					pointer++;

				} else {
					ArrayList<String> SubNames = new ArrayList<String>();
					SubNames.add(names.get(pointer));
					pointer++;
					while (size < cutoff && pointer < names.size()) {
						SubNames.add(names.get(pointer));
						size += lengths.get(pointer);
						pointer++;
					}
					String commandLine = "";
					String outFile = "";
					if (!HaploTypeCaller) {
						outFile = "Unified.output.raw.snps.indels.part" + count
								+ ".vcf";
						commandLine = UnifiedGenotypeCaller(sbatch.getMemoryPadded(), GATKdir,
								fileNames, Reference, outFile + ".temp",
								knownSNPVCF, SubNames);
					} else {
						outFile = "Haplotype.output.raw.snps.indels.part"
								+ count + ".vcf";
						commandLine = HaplotypeCaller(sbatch.getMemoryPadded(), GATKdir,
								fileNames, Reference, outFile + ".temp",
								knownSNPVCF, SubNames);

					}
					if (!rerun
							|| !IOTools.fileExists(outDir
									+ "/output.raw.snps.indels.part" + count
									+ ".vcf.idx"))
						printSBATCHscript(superShellScript, sbatch,
								 outDir, commandLine, 
								outFile, "GATK_phase2");
					count++;
				}

			}

			if (cutoff == -1) {
				String sbatchFileName = outDir + "/scripts/" + sbatch.getTimeStamp() + "_"
						+ count + "phase2_GATK.sbatch";
				System.out.println("sbatch " + sbatchFileName);
				ExtendedWriter EW = new ExtendedWriter(new FileWriter(
						sbatchFileName));
				sbatch.printSBATCHinfo(EW, outDir, sbatch.getTimeStamp(), 0, "GATK_phase2");

				EW.println("cd " + outDir);

				if (HaploTypeCaller) {
					String commandLine = HaplotypeCaller(sbatch.getMemoryPadded(), GATKdir,
							fileNames, Reference, "output.raw.snps.indels.vcf",
							knownSNPVCF, null);
					System.out.println(commandLine);
					EW.println(commandLine);
					count++;
				} else {
					System.out
							.println("I have not implemented UniGenotypeCaller");
				}
				EW.println();
				EW.println();

				EW.flush();
				EW.close();

			}
			sbatch.printShellInfoSTOP(superShellScript,"GATK_phase2",outDir);

		} catch (Exception E) {
			E.printStackTrace();
		}
	}

	
	
	
//	
//	public void GATKGenotypeSample(ExtendedWriter generalSbatchScript,
//			SBATCHinfo sbatch, String timestamp, String outDir,
//			ArrayList<String> fileNames, Hashtable<String, String> T) {
//		System.out.println("running GATKGenotype");
//
//		/*
//		 * for each sample (asumed to be in same dir)
//		 * 
//		 * lanes.bam <- merged lane.bams for sample dedup.bam <-
//		 * MarkDuplicates(lanes.bam) realigned.bam <- realign(dedup.bam) [with
//		 * known sites included if available] recal.bam <- recal(realigned.bam)
//		 * sample.bam <- recal.bam
//		 */
//
//		if (!IOTools.isDir(outDir + "/reports"))
//			IOTools.mkDir(outDir + "/reports");
//		if (!IOTools.isDir(outDir + "/scripts"))
//			IOTools.mkDir(outDir + "/scripts");
//		try {
//
//			for (int i = 0; i < fileNames.size(); i++) {
//				String bamFile = fileNames.get(i);
//
//				String sbatchFileName = outDir + "/scripts/" + timestamp + "_"
//						+ i + "_Genotype_GATK.sbatch";
//				generalSbatchScript.println("sbatch " + sbatchFileName);
//				ExtendedWriter EW = new ExtendedWriter(new FileWriter(
//						sbatchFileName));
//				sbatch.printSBATCHinfo(EW, outDir, timestamp, i,
//						"GATK_Genotype");
//				EW.println();
//				EW.println("cd " + outDir);
//
//				String commandLine = Genotype(23, this.GATKdir, bamFile,
//						this.Reference, bamFile + ".Genotype.vcf",
//						this.knownSNPVCF);
//				EW.println(commandLine);
//				System.out.println(commandLine);
//
//				EW.println();
//				EW.println();
//				EW.flush();
//				EW.close();
//			}
//
//		} catch (Exception E) {
//			E.printStackTrace();
//		}
//	}
//
//	public void GATKPhaseSNPsSample(ExtendedWriter generalSbatchScript,
//			SBATCHinfo sbatch, String timestamp, String outDir,
//			ArrayList<String> fileNames, Hashtable<String, String> T) {
//		System.out.println("running GATKPhaseSNPsSample");
//
//		/*
//		 * for each sample (asumed to be in same dir)
//		 * 
//		 * lanes.bam <- merged lane.bams for sample dedup.bam <-
//		 * MarkDuplicates(lanes.bam) realigned.bam <- realign(dedup.bam) [with
//		 * known sites included if available] recal.bam <- recal(realigned.bam)
//		 * sample.bam <- recal.bam
//		 */
//
//		boolean respectAlreadyPhased = false;
//		if (T.containsKey("-respectPhaseInInput"))
//			respectAlreadyPhased = true;
//		if (!IOTools.isDir(outDir + "/reports"))
//			IOTools.mkDir(outDir + "/reports");
//		if (!IOTools.isDir(outDir + "/scripts"))
//			IOTools.mkDir(outDir + "/scripts");
//		try {
//			for (int i = 0; i < fileNames.size(); i++) {
//				String bamFile = fileNames.get(i);
//
//				String sbatchFileName = outDir + "/scripts/" + timestamp + "_"
//						+ i + "_phaseSNPs_GATK.sbatch";
//				generalSbatchScript.println("sbatch " + sbatchFileName);
//				ExtendedWriter EW = new ExtendedWriter(new FileWriter(
//						sbatchFileName));
//				sbatch.printSBATCHinfo(EW, outDir, timestamp, i,
//						"GATK_phaseSNPs");
//				EW.println();
//				EW.println("cd " + outDir);
//
//				String commandLine = ReadBackedPhasing(this.memory,
//						this.GATKdir, bamFile, this.Reference, bamFile
//								+ ".phased.vcf", this.knownSNPVCF, 10.0,
//						respectAlreadyPhased);
//				EW.println(commandLine);
//				System.out.println(commandLine);
//
//				EW.println();
//				EW.println();
//				EW.flush();
//				EW.close();
//			}
//
//		} catch (Exception E) {
//			E.printStackTrace();
//		}
//	}
//

	public static String Genotype(int memory, String GATKDir, String bamFile,
			String reference, String baseName, String knownSNPVCF) {

		/*
		 * java -Xmx23g -jar
		 * /bubo/sw/apps/bioinfo/GATK/1.2.12/GenomeAnalysisTK.jar -R
		 * /bubo/home/h12
		 * /pallol/glob/projects/b2010010/alignment/bwa/variation/assembly
		 * /fAlb13.masked.fa -T UnifiedGenotyper -nt 12 -I
		 * /bubo/home/h12/pallol/
		 * glob/projects/b2010010/alignment/bwa/variation/low_coverage
		 * /mult_genome_20101112
		 * /raw/vs_fAlb13rm/s_5.fastq.bwa.q33.sorted.real.md.recal.bam -o
		 * COLL_34.UNIONSITES.ALLLIBS.SNP.vs_fAlb13rm.vcf -alleles:masterAlleles
		 * /proj/b2010010/private/variation/vs_fAlb13rm/COLL-PIED.UNION.vcf
		 * -gt_mode GENOTYPE_GIVEN_ALLELES -out_mode EMIT_ALL_SITES -BTI
		 * masterAlleles -stand_call_conf 0.0 -G none
		 * 
		 * 
		 * 
		 * java -jar /GenomeAnalysisTK.jar -T GenotypeAndValidate -R
		 * human_g1k_v37.fasta -I myNewTechReads.bam -alleles
		 * handAnnotatedVCF.vcf -L handAnnotatedVCF.vcf
		 */
		String commandLine = "java -Xmx"
				+ memory
				+ "g -jar "
				+ GATKDir
				+ "/GenomeAnalysisTK.jar "
				+ "-T UnifiedGenotyper "
				+ "-nt 7 "
				+ "-I "
				+ bamFile
				+ " "
				+ "-R "
				+ reference
				+ " "
				+ "-o "
				+ bamFile
				+ ".Genotype.vcf "
				+ "-alleles  "
				+ knownSNPVCF
				+ " "
				+ "-gt_mode GENOTYPE_GIVEN_ALLELES --output_mode EMIT_ALL_SITES "
				+ "-L  " + knownSNPVCF;

		return commandLine;

	}

	public static String HaplotypeCaller(int memory, String GATKDir,
			ArrayList<String> bamFiles, String reference, String outFileVCF,
			String knownSNPVCF, ArrayList<String> contigs) {

		/*
		 * java -jar GenomeAnalysisTK.jar -T HaplotypeCaller -R
		 * reference/human_g1k_v37.fasta -I sample1.bam [-I sample2.bam ...] \
		 * --dbsnp dbSNP.vcf \ -stand_call_conf [50.0] \ -stand_emit_conf 10.0 \
		 * [-L targets.interval_list] -o output.raw.snps.indels.vcf
		 */

		String commandLine = "java -Xmx" + memory + "g -jar " + GATKDir
				+ "/GenomeAnalysisTK.jar " + "-R " + reference + " "
				+ "-T HaplotypeCaller " + "-o " + outFileVCF;
		for (int i = 0; i < bamFiles.size(); i++) {
			commandLine += " -I " + bamFiles.get(i);
		}
		if (contigs != null) {
			for (int i = 0; i < contigs.size(); i++) {
				commandLine += " -L " + contigs.get(i);
			}
		}

		if (knownSNPVCF != null)
			commandLine += " --dbsnp " + knownSNPVCF;

		return commandLine;

	}

	public static String UnifiedGenotypeCaller(int memory, String GATKDir,
			ArrayList<String> bamFiles, String reference, String outFileVCF,
			String knownSNPVCF, ArrayList<String> contigs) {

		/*
		 * java -jar GenomeAnalysisTK.jar -T HaplotypeCaller -R
		 * reference/human_g1k_v37.fasta -I sample1.bam [-I sample2.bam ...] \
		 * --dbsnp dbSNP.vcf \ -stand_call_conf [50.0] \ -stand_emit_conf 10.0 \
		 * [-L targets.interval_list] -o output.raw.snps.indels.vcf
		 */

		String commandLine = "java -Xmx" + memory + "g -jar " + GATKDir
				+ "/GenomeAnalysisTK.jar " + "-R " + reference + " "
				+ "-T UnifiedGenotyper " + "-dcov 200 " + "-o " + outFileVCF;
		for (int i = 0; i < bamFiles.size(); i++) {
			commandLine += " -I " + bamFiles.get(i);
		}
		if (contigs != null) {
			for (int i = 0; i < contigs.size(); i++) {
				commandLine += " -L " + contigs.get(i);
			}
		}

		if (knownSNPVCF != null)
			commandLine += " --dbsnp " + knownSNPVCF;

		return commandLine;

	}


}
