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

public class GATKphase1 {

	String time;
	String projectDir;
	String suffix;
	String prefix;
	String picardDir;
	String GATKdir;
	String knownSNPVCF;
	String knownIndelVCF;
	String targetIntervals;
	String nucleicAcid;
	boolean ReassignOneMappingQuality;

	int cutoff;
	int padding;
	

	boolean rerun;
	String Reference;

	boolean fullDir;
	boolean fullDirSplit;
	boolean sub;
	

	public void help(){



		System.out.println("Setable values for GATK phase1:");
		System.out.println(Functions.fixedLength("-picardDir <dir>", 30)+"Default is  /bubo/sw/apps/bioinfo/picard/1.92/kalkyl/");
		System.out.println(Functions.fixedLength("-GATKDir <dir>", 30)+"Default is  /bubo/sw/apps/bioinfo/GATK/2.5.2/");
		System.out.println(Functions.fixedLength("-R <ReferenceFile.fa>", 30)+"Reference fasta file");
		System.out.println(Functions.fixedLength("-knownSNPs <KnownSNP.vcf>", 30)+"Vcf file with known SNPs");
		System.out.println(Functions.fixedLength("-knownIndels <KnownIndels.vcf>", 30)+"Vcf file with known indels");
		System.out.println(Functions.fixedLength("-targetIntervals <targetIndelsFile>", 30)+"targetIntervals file in format according to GATK");
		System.out.println(Functions.fixedLength("-ReAlign ", 30)+"substep in phase 1 of GATK see GATK for more info");
		System.out.println(Functions.fixedLength("-AddOrReplaceReadGroups ", 30)+"substep in phase 1 of GATK see GATK for more info");
		System.out.println(Functions.fixedLength("-BQSR ", 30)+"substep in phase 1 of GATK see GATK for more info");
		System.out.println(Functions.fixedLength("-BQSRprint ", 30)+"substep in phase 1 of GATK see GATK for more info");
		System.out.println(Functions.fixedLength("-ReduceReads ", 30)+"substep in phase 1 of GATK see GATK for more info");
		System.out.println(Functions.fixedLength("-ReassignOneMappingQuality ", 30)+"substep in phase 1 of GATK see GATK for more info. ATTENTION this must be set for STAR alignments to work");
		System.out.println(Functions.fixedLength("", 30)+"ATTENTION!!! this must be set for STAR alignments to work");
		

			
		
		

		System.out.println();

	}





	public GATKphase1() {
		projectDir = time = null;
		
		
	}

	public static void main(String[] args) {

		int length = args.length;
		for (int i = 0; i < length; i++) {
			args[i] = args[i].trim();
			System.out.print(args[i] + " ");
		}
		System.out.println();
		Hashtable<String, String> T = Functions.parseCommandLine(args);
		GATKphase1 GATK = new GATKphase1();
		//GATK.run(T);
	}

	
	
	
	
	
	public boolean addParameters(Hashtable<String, String> T) {

		picardDir = Functions.getValue(T, "-picardDir",
				"/sw/apps/bioinfo/picard/1.92/milou/");
		GATKdir = Functions.getValue(T, "-GATKDir",
				"/sw/apps/bioinfo/GATK/2.5.2/");
		this.suffix = Functions.getValue(T,"-suffix");
		boolean allPresent = true;
		this.Reference = Functions.getValue(T, "-R");
		File ref = new File(this.Reference);
		this.Reference = ref.getAbsolutePath();
		
		if (T.containsKey("-RNA"))
			this.nucleicAcid = "RNA";
		else
			this.nucleicAcid = "DNA";

		knownSNPVCF = Functions.getValue(T, "-knownSNPs", null);
		if (IOTools.fileExists(IOTools.getCurrentPath() + "/" + knownSNPVCF))
			knownSNPVCF = IOTools.getCurrentPath() + "/" + knownSNPVCF;

		knownIndelVCF = Functions.getValue(T, "-knownIndels", null);
		if (IOTools.fileExists(IOTools.getCurrentPath() + "/" + knownIndelVCF))
			knownIndelVCF = IOTools.getCurrentPath() + "/" + knownIndelVCF;
		targetIntervals = Functions.getValue(T, "-targetIntervals", null);
		suffix = Functions.getValue(T, "-suffix", "bam");
		prefix = Functions.getValue(T, "-prefix");
		if (suffix.indexOf('.') == 0)
			suffix = suffix.substring(1);


		if (T.containsKey("-ReAlign")
				|| T.containsKey("-AddOrReplaceReadGroups")
				|| T.containsKey("-BQSR") || T.containsKey("-BQSRprint")
				|| T.containsKey("-ReduceReads")
				|| T.containsKey("-sub")) sub = true;
		
		else if (T.containsKey("-single")) fullDirSplit = true;		
		else fullDir= true; 
			
		if (T.containsKey("-ReAlign") || T.containsKey("-BQSR") || T.containsKey("-BQSRprint")) {
			
			if (T.containsKey("-ReAlign")){
				if (!T.containsKey("-R")){
					System.out.println("must contain ReferenceFile");
					help();
					return false;
				}
			}
			if (T.containsKey("-BQSR")){
				if (!T.containsKey("-R")){
					System.out.println("must contain ReferenceFile");
					help();
					return false;
				}
			}
			if (T.containsKey("-BQSRprint")){
				if (!T.containsKey("-R")){
					System.out.println("must contain ReferenceFile");
					help();
					return false;
				}
			}
			return true;
		}
		if (!T.containsKey("-R")){
			System.out.println("must contain ReferenceFile");
			help();
			return false;
		}
		
		return true;
	}
		
	public boolean checkParameters(Hashtable<String, String> T) {

		picardDir = Functions.getValue(T, "-picardDir",
				"/bubo/sw/apps/bioinfo/picard/1.92/kalkyl/");
		GATKdir = Functions.getValue(T, "-GATKDir",
				"/bubo/sw/apps/bioinfo/GATK/2.5.2/");
		this.suffix = Functions.getValue(T,"-suffix");
		boolean allPresent = true;
		
		if (T.containsKey("-ReAlign")
				|| T.containsKey("-AddOrReplaceReadGroups")
				|| T.containsKey("-BQSR") || T.containsKey("-BQSRprint")
				|| T.containsKey("-ReduceReads")
				|| T.containsKey("-sub")) sub = true;
		
		
		else if (T.containsKey("-single")) fullDirSplit = true;		
		else fullDir= true; 
			
		if (T.containsKey("-ReAlign") || T.containsKey("-BQSR") || T.containsKey("-BQSRprint")) {
			
			if (T.containsKey("-ReAlign")){
				if (!T.containsKey("-R")){
					System.out.println("must contain ReferenceFile");
					help();
					return false;
				}
			}
			if (T.containsKey("-BQSR")){
				if (!T.containsKey("-R")){
					System.out.println("must contain ReferenceFile");
					help();
					return false;
				}
			}
			if (T.containsKey("-BQSRprint")){
				if (!T.containsKey("-R")){
					System.out.println("must contain ReferenceFile");
					help();
					return false;
				}
			}
			return true;
		}
		if (!T.containsKey("-R")){
			System.out.println("must contain ReferenceFile");
			help();
			return false;
		}
		
		return true;
	}
	
	
	
//	
//	public void run(Hashtable<String, String> T) {
//
//		time = Functions.getValue(T, "-time", "3:00:00");
//
//		if (T.containsKey("-i"))
//			inDir = Functions.getValue(T, "-i", ".");
//		else {
//			System.out.println("must contain inDirectory -i ");
//			allPresent = false;
//		}
//
//		if (T.containsKey("-R"))
//			this.Reference = Functions.getValue(T, "-R");
//		else {
//			System.out.println("must contain ReferenceFile -R ");
//			allPresent = false;
//		}
//
//		if (T.containsKey("-RNA"))
//			this.nucleicAcid = "RNA";
//		else
//			this.nucleicAcid = "DNA";
//
//		if (!T.containsKey("-time"))
//			System.out
//			.println("must contain likely time -time now set to default 3:00:00");
//		if (T.containsKey("-UnifiedGenotyper"))
//			this.HaploTypeCaller = false;
//		else
//			this.HaploTypeCaller = true;
//
//		cutoff = Functions.getInt(T, "-L", -1);
//
//		knownSNPVCF = Functions.getValue(T, "-knownSNPs", null);
//		if (IOTools.fileExists(IOTools.getCurrentPath() + "/" + knownSNPVCF))
//			knownSNPVCF = IOTools.getCurrentPath() + "/" + knownSNPVCF;
//
//		knownIndelVCF = Functions.getValue(T, "-knownIndels", null);
//		if (IOTools.fileExists(IOTools.getCurrentPath() + "/" + knownIndelVCF))
//			knownIndelVCF = IOTools.getCurrentPath() + "/" + knownIndelVCF;
//
//		targetIntervals = Functions.getValue(T, "-targetIntervals", null);
//
//		this.PRIORITIZE = true;
//		if (T.containsKey("-UNIQUIFY"))
//			PRIORITIZE = false;
//
//		if (T.containsKey("-rerun"))
//			rerun = true;
//
//		this.padding = 100;
//
//		suffix = Functions.getValue(T, "-suffix", "bam");
//		prefix = Functions.getValue(T, "-prefix");
//		if (suffix.indexOf('.') == 0)
//			suffix = suffix.substring(1);
//
//		projectDir = Functions.getValue(T, "-projectDir",
//				IOTools.getCurrentPath());
//
//		if (allPresent) {
//			if (IOTools.fileExists(projectDir + "/" + this.Reference)) {
//				this.Reference = projectDir + "/" + this.Reference;
//			}
//			if (IOTools.isDir(projectDir + "/" + inDir)) {
//				inDir = projectDir + "/" + inDir;
//			} else if (!IOTools.isDir(inDir)) {
//				System.out.println("Neither " + inDir + " nor " + projectDir
//						+ "/" + inDir + "was found");
//				return;
//			}
//			GATKInitial(sbatch, timeStamp, inDir, T);
//		} else {
//			System.out
//			.println("\n\nAborting run because of missing arguments for GATK.");
//			System.out.println("\n\nTypical line should be something like:");
//			System.out.println("\n\nThis is the current :");
//			System.out
//			.println("HTStools -p sbatch -GATK -phase1 -ReassignOneMappingQuality  -ReAlign -BQSR -BQSRprint -ReduceReads -suffix markDuplicates.bam -i bamFiles -R /bubo/home/h17/johanr/capsellaDir/nobackup/Cr_reference/assembly/Crubella_183.fa -time 40:00:00 -knownSNPs /bubo/home/h17/johanr/capsellaDir/nobackup/knownSNPs/Cg/GATK_13samps.flt_excluded_sites.vcf -targetIntervals /bubo/home/h17/johanr/capsellaDir/nobackup/Cr_reference/annotation/Crubella_183_only_exons_unique.bed");
//			System.out
//			.println("HTStools -p sbatch -GATK -phase1  -i INDIR -R REFERENCEFILE -time 4:00:00 -knownSNPs knownSNPsFile  [-knownIndels knownIndelsFile] [-targetIntervals bedFile] -suffix bam [ -preGATK -RGPU Barcode] [-RGSM Name]] ");
//			System.out
//			.println("HTStools -p sbatch -GATK -phase2 -i INDIR -R REFERENCEFILE -time 4:00:00 -L 100000 [-rerun] -X 2 -suffix reduced.bam");
//			System.out
//			.println("HTStools -p sbatch -GATK -merge -i INDIR -R REFERENCEFILE -time 6-00:00:00 -X 23 -suffix vcf -prefix output.raw.snps.indels");
//			System.out
//			.println("HTStools -p sbatch -GATK -phaseSNPs -i INDIR -R REFERENCEFILE -time 6-00:00:00 -X 23 -knownSNPs knownSNPsFile -suffix ENDOFBAMfiles -respectPhase ");
//		}
//	}
//
//	public void GATKInitial(SBATCHinfo sbatch, String timeStamp, String inDir,
//			Hashtable<String, String> T) {
//		try {
//			if (!IOTools.isDir(projectDir + "/scripts"))
//				IOTools.mkDir(projectDir + "/scripts");
//			String lastDir = inDir.substring(inDir.lastIndexOf("/") + 1);
//			if (lastDir.length() < 1) {
//				lastDir = inDir.substring(0, inDir.lastIndexOf("/"));
//				lastDir = lastDir.substring(lastDir.lastIndexOf("/") + 1);
//			}
//			if (T.containsKey("-phase1")) {
//				ExtendedWriter EW = new ExtendedWriter(new FileWriter(
//						projectDir + "/scripts/" + timeStamp + "." + lastDir
//						+ ".GATK.phase1.sh"));
//				GATKDirPhase1(EW, sbatch, timeStamp, inDir, T);
//				EW.flush();
//				EW.close();
//			}
//		} catch (Exception E) {
//			E.printStackTrace();
//		}
//	}

	public void GATKDirPhase1(Hashtable<String, String> T ,ExtendedWriter generalSbatchScript,
			SBATCHinfo sbatch, String inDir,ArrayList<String> fileNames) {

		if (!fileNames.isEmpty()) {
			try {
				if(this.fullDir)
					GATKPhase1SampleFull(generalSbatchScript, sbatch,  inDir,
							fileNames, T);
				else if(this.fullDirSplit)
						GATKPhase1SampleFullSplit(generalSbatchScript, sbatch, inDir,
								fileNames, T);
				else
					this.GATKPhase1Sub(generalSbatchScript, sbatch, inDir, fileNames, T);
				
			} catch (Exception E) {
				E.printStackTrace();
			}
		}
	}




	public void GATKPhase1Sub(ExtendedWriter generalSbatchScript,
			SBATCHinfo sbatch,  String outDir,
			ArrayList<String> fileNames, Hashtable<String, String> T) {

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

			boolean phase1 = true;
			if (T.containsKey("-ReAlign")
					|| T.containsKey("-ReassignOneMappingQuality")
					|| T.containsKey("-AddOrReplaceReadGroups")
					|| T.containsKey("-BQSR") || T.containsKey("-BQSRprint")
					|| T.containsKey("-ReduceReads")) {
				phase1 = false;
				for (int i = 0; i < fileNames.size(); i++) {
					String bamFile = fileNames.get(i);

					String sbatchFileName = outDir + "/scripts/" + sbatch.getTimeStamp()
							+ "_" + bamFile + "_phase1_GATK.sbatch";
					generalSbatchScript.println("sbatch " + sbatchFileName);
					ExtendedWriter EW = new ExtendedWriter(new FileWriter(
							sbatchFileName));
					sbatch.printSBATCHinfo(EW, outDir, sbatch.getTimeStamp(), bamFile,
							"GATK_phase1_part");

					EW.println();
					EW.println("cd " + outDir);

					if (!IOTools.fileExists(outDir + "/" + bamFile + ".bai"))
						Samtools.indexBamFile(bamFile, EW, true);

					if (T.containsKey("-AddOrReplaceReadGroups")) {
						String[] info = bamFile.split("_");
						String Name, Nucleotide, Barcode, Lane;
						Name = Nucleotide = Barcode = Lane = null;
						if (info.length > 3) {
							Name = info[0];
							Nucleotide = info[1];
							Barcode = info[2];
							Lane = info[3];
						}
						String RGLB = Functions.getValue(T, "-RGLB", Lane);
						String RGPL = Functions.getValue(T, "-RGPL", "illumina");
						String RGPU = Functions.getValue(T, "-RGPU", Barcode);
						String RGSM = Functions.getValue(T, "-RGSM", Name);
						String RGID = Functions
								.getValue(T, "-RGID", RGSM + "_" + RGPU + "_"
										+ RGLB + "_" + Nucleotide + "_" + Lane);

						bamFile = Picard.AddOrReplaceReadGroups(EW, bamFile, sbatch.getMemoryPadded(), picardDir, suffix, RGID, RGLB, RGPL, RGPU, RGSM, "coordinate")+"."+suffix;
					}

					if (T.containsKey("-ReassignOneMappingQuality")) {
						bamFile = ReassignOneMappingQuality(EW, bamFile,
								sbatch.getMemoryPadded(), this.GATKdir, this.Reference);
					}
					if (T.containsKey("-ReAlign"))
						bamFile = ReAlign(EW, bamFile, sbatch.getMemoryPadded(), this.GATKdir,
								this.Reference, knownSNPVCF,
								knownIndelVCF, targetIntervals);
					if (T.containsKey("-BQSR"))
						bamFile = BQSR(EW, bamFile, sbatch.getMemoryPadded(), this.GATKdir,
								this.Reference, knownSNPVCF,
								knownIndelVCF);
					if (T.containsKey("-BQSRprint"))
						bamFile = BQSRprint(EW, bamFile, sbatch.getMemoryPadded(),
								this.GATKdir, this.Reference);
					if (T.containsKey("-ReduceReads"))
						ReduceReads(EW, bamFile, sbatch.getMemoryPadded(), this.GATKdir,
								this.Reference);
					EW.println();
					EW.println();
					EW.flush();
					EW.close();
				}
			}

		} catch (Exception E) {
			E.printStackTrace();
		}
	}




	public void GATKPhase1SampleFull(ExtendedWriter generalSbatchScript,
			SBATCHinfo sbatch, String outDir,
			ArrayList<String> fileNames, Hashtable<String, String> T) {

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
			String sbatchFileName = outDir + "/scripts/" + sbatch.getTimeStamp()
					+ "_phase1_GATK.sbatch";
			generalSbatchScript.println("sbatch " + sbatchFileName);
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(
					sbatchFileName));
			sbatch.printSBATCHinfo(EW, outDir, sbatch.getTimeStamp(),
					"GATK_phase1", time);
			EW.println();
			EW.println("cd " + outDir);

			// takes care of merging, correct naming of bam files and
			// marking duplicates
			String bamFile = Picard.preGATK(EW, sbatch, sbatch.getTimeStamp(),
					outDir, fileNames, T, this.picardDir, sbatch.getMemoryPadded(),
					this.suffix, this.nucleicAcid);

			// check if it is RNA or DNA and change score scheme
			// accordingly
			if(T.containsKey("-ReassignOneMappingQuality"))
				ReassignOneMappingQuality = true;
			else
				ReassignOneMappingQuality = false;

			// takes care of realining and recalibration
			phase1(EW, bamFile, sbatch.getMemoryPadded(),  this.GATKdir,
					this.Reference, suffix, knownSNPVCF, knownIndelVCF,
					targetIntervals, ReassignOneMappingQuality);
			EW.println();
			EW.println();
			EW.println("wait");

			EW.flush();
			EW.close();
		}catch (Exception E) {
			E.printStackTrace();
		}
	}



	public void GATKPhase1SampleFullSplit(ExtendedWriter generalSbatchScript,
				SBATCHinfo sbatch, String outDir,
				ArrayList<String> fileNames, Hashtable<String, String> T) {

		if (!IOTools.isDir(outDir + "/reports"))
			IOTools.mkDir(outDir + "/reports");
		if (!IOTools.isDir(outDir + "/scripts"))
			IOTools.mkDir(outDir + "/scripts");
		try {
				// expects that all the files are in one bam file
				for (int i = 0; i < fileNames.size(); i++) {
					String sbatchFileName = outDir + "/scripts/"
							+ sbatch.getTimeStamp() + "_" + i + "_phase1_GATK.sbatch";
					generalSbatchScript.println("sbatch " + sbatchFileName);
					ExtendedWriter EW = new ExtendedWriter(new FileWriter(
							sbatchFileName));
					sbatch.printSBATCHinfo(EW, outDir, sbatch.getTimeStamp(),
							"GATK_phase1", time);
					EW.println();
					EW.println("cd " + outDir);

					String bamFile = fileNames.get(i);
					// takes care of realining and recalibration
					boolean ReassignOneMappingQuality = false;
					if (this.nucleicAcid.compareTo("RNA") == 0)
						ReassignOneMappingQuality = true;

					phase1(EW, bamFile, sbatch.getMemoryPadded(), 
							this.GATKdir, this.Reference, suffix,
							knownSNPVCF, knownIndelVCF, targetIntervals,
							ReassignOneMappingQuality);
					EW.println();
					EW.println();
					EW.println("wait");

					EW.flush();
					EW.close();
				}
			}catch (Exception E) {
				E.printStackTrace();
			}
		
		}


		public static String phase1(ExtendedWriter EW, String bamFile, int memory,
				String GATKDir, String Reference, String suffix,
				String knownSNPVCF, String knownIndelsVCF, String targetIntervals,
				boolean ReassignOneMappingQuality) {
			// if(IOTools.fileExists(bamFile)){

			if (!IOTools.fileExists(bamFile + ".bai")) {
				EW.println("# index bam files  ");
				Samtools.indexBamFile(bamFile, EW, true);
				EW.println();
				EW.println();
			}

			if (ReassignOneMappingQuality)
				bamFile = ReassignOneMappingQuality(EW, bamFile, memory, GATKDir,
						Reference);
			bamFile = ReAlign(EW, bamFile, memory, GATKDir, Reference,
					knownSNPVCF, knownIndelsVCF, targetIntervals);
			bamFile = BQSR(EW, bamFile, memory, GATKDir, Reference,
					knownSNPVCF, knownIndelsVCF);
			bamFile = ReduceReads(EW, bamFile, memory, GATKDir, Reference);

			return bamFile;
			// }
			// return null;
		}

		public static String ReassignOneMappingQuality(ExtendedWriter EW,
				String bamFile, int memory, String GATKDir, String Reference) {

			String baseName = bamFile.substring(0, bamFile.indexOf(".bam"));
			String commandLine = ReassignOneMappingQuality(memory, GATKDir,
					bamFile, Reference, baseName);

			EW.println("# ReassignOneMappingQuality  ");
			EW.println();
			EW.println(commandLine);
			EW.println();
			EW.println("echo ReassignOneMappingQuality finished");
			EW.println();

			return baseName + ".ReassignOneMappingQuality.bam";
		}

		public static String ReAlign(ExtendedWriter EW, String bamFile, int memory,
				String GATKDir, String Reference,
				String knownSNPVCF, String knownIndelsVCF, String targetIntervals) {

			String baseName = bamFile.substring(0, bamFile.lastIndexOf(".bam"));
			if (targetIntervals == null) {

				String commandLine = createIntervals(memory, GATKDir, bamFile,
						Reference, baseName, knownIndelsVCF);

				EW.println("# createIntervals  ");
				EW.println(commandLine);
				EW.println();
				EW.println("echo Create Intervals finished");
				EW.println();
				targetIntervals = baseName + ".intervals";
			}

			String commandLine = LocalRealignment(memory, GATKDir, bamFile,
					Reference, baseName, knownIndelsVCF, targetIntervals);
			EW.println("# LocalRealignment  ");
			EW.println(commandLine);
			EW.println();

			EW.println("echo Local Realignment finished");
			EW.println();

			return baseName + ".real.bam";
		}

		public static String BQSR(ExtendedWriter EW, String bamFile, int memory,
				String GATKDir, String Reference, 
				String knownSNPVCF, String knownIndelsVCF) {
			String baseName = bamFile.substring(0, bamFile.indexOf(".bam"));
			EW.println("# BaseQualityRecalibration  ");

			String commandLine = BaseQualityReport(memory, GATKDir, baseName
					+ ".bam", Reference, baseName, knownSNPVCF, knownIndelsVCF);
			EW.println(commandLine);
			EW.println();
			EW.println("echo Base Quality Report finished");
			EW.println();

			String commandLine2 = BaseQualityRecalibration(memory, GATKDir,
					baseName + ".bam", Reference, baseName, baseName + ".grp");
			EW.println(commandLine2);
			EW.println();
			EW.println("echo Base Quality Recalibration finished");
			EW.println();

			return baseName + ".BQSR.bam";
		}

		public static String BQSRprint(ExtendedWriter EW, String bamFile,
				int memory, String GATKDir, String Reference) {
			String baseName = bamFile.substring(0, bamFile.indexOf(".bam"));
			EW.println("# BaseQualityRecalibration  ");

			String commandLine2 = BaseQualityRecalibration(memory, GATKDir,
					baseName + ".bam", Reference, baseName, baseName + ".grp");
			EW.println(commandLine2);
			EW.println();
			EW.println("echo Base Quality Recalibration finished");
			EW.println();

			return baseName + ".BQSR.bam";
		}

		public static String ReduceReads(ExtendedWriter EW, String bamFile,
				int memory, String GATKDir, String Reference) {
			String baseName = bamFile.substring(0, bamFile.indexOf(".bam"));
			EW.println("# ReduceReads shell script  ");

			String commandLine2 = ReduceReads(memory, GATKDir, baseName, Reference);
			EW.println(commandLine2);
			EW.println();
			EW.println("echo ReduceReads finished");
			EW.println();

			return baseName + ".reduced.bam";
		}

	

		public static String BaseQualityReport(int memory, String GATKDir,
				String bamFile, String reference, String baseName,
				String knownSNPVCF, String knownIndelsVCF) {

			/*
			 * java -Xmx4g -jar GenomeAnalysisTK.jar \ -T BaseRecalibrator \ -I
			 * my_reads.bam \ -R resources/Homo_sapiens_assembly18.fasta \
			 * -knownSites bundle/hg18/dbsnp_132.hg18.vcf \ -knownSites
			 * another/optional/setOfSitesToMask.vcf \ -o recal_data.grp
			 */

			String commandLine = "java -Xmx" + memory + "g -jar " + GATKDir
					+ "/GenomeAnalysisTK.jar " + "-T BaseRecalibrator " + "-I "
					+ bamFile + " " + "-R " + reference + " " + "-o " + baseName
					+ ".grp";
			if (knownSNPVCF != null)
				commandLine += " -knownSites " + knownSNPVCF;
			if (knownIndelsVCF != null)
				commandLine += " -knownSites " + knownIndelsVCF;

			return commandLine;

		}

		public static String ReduceReads(int memory, String GATKDir,
				String baseName, String reference) {

			/*
			 * java -Xmx4g -jar GenomeAnalysisTK.jar \ -R ref.fasta \ -T ReduceReads
			 * \ -I myData.bam \ -o myData.reduced.bam
			 */

			String commandLine = "java -Xmx" + memory + "g -jar " + GATKDir
					+ "/GenomeAnalysisTK.jar " + "-T ReduceReads " + "-I "
					+ baseName + ".bam " + "-R " + reference + " " + "-o "
					+ baseName + ".reduced.bam";

			return commandLine;

		}

		public static String BaseQualityRecalibration(int memory, String GATKDir,
				String bamFile, String reference, String baseName, String BSQRfile) {

			/*
			 * java -jar GenomeAnalysisTK.jar \ -T PrintReads \ -R reference.fasta \
			 * -I input.bam \ -BQSR recalibration_report.grp \ -o output.bam - See
			 * more at:
			 * http://gatkforums.broadinstitute.org/discussion/44/base-quality
			 * -score-recalibration-bqsr#sthash.8xVFUaHp.dpuf
			 */

			String commandLine = "java -Xmx" + memory + "g -jar " + GATKDir
					+ "/GenomeAnalysisTK.jar " + "-T PrintReads " + "-I " + bamFile
					+ " " + "-R " + reference + " " + "-BQSR " + BSQRfile + " "
					+ "-o " + baseName + ".BQSR.bam";

			return commandLine;

		}

		public static String ReassignOneMappingQuality(int memory, String GATKDir,
				String bamFile, String reference, String baseName) {

			/*
			 * java -jar GenomeAnalysisTK.jar \ -R ref.fasta \ -T PrintReads \ -o
			 * output.bam \ -I input1.bam \ -I input2.bam \ -rf
			 * ReassignOneMappingQuality -RMQF 255 -RMQT 60 - See more at:
			 * http://gatkforums
			 * .broadinstitute.org/discussion/44/base-quality-score-
			 * recalibration-bqsr#sthash.8xVFUaHp.dpuf
			 */

			String commandLine = "java -Xmx" + memory + "g -jar " + GATKDir
					+ "/GenomeAnalysisTK.jar " + "-I " + bamFile + " " + "-R "
					+ reference + " " + "-T PrintReads " + "-o " + baseName
					+ ".ReassignOneMappingQuality.bam"
					+ " -rf ReassignOneMappingQuality -RMQF 255 -RMQT 60 ";

			return commandLine;

		}

		public static String createIntervals(int memory, String GATKDir,
				String bamFile, String reference, String baseName,
				String knownIndelsVCF) {
			/*
			 * java -Xmx2g -jar GenomeAnalysisTK.jar \ -I input.bam \ -R ref.fasta \
			 * -T RealignerTargetCreator \ -o forIndelRealigner.intervals \ [--known
			 * /path/to/indels.vcf]
			 */
			String commandLine = "java -Xmx" + memory + "g -jar " + GATKDir
					+ "/GenomeAnalysisTK.jar " + "-I " + bamFile + " " + "-R "
					+ reference + " " + "-T RealignerTargetCreator " + "-o "
					+ baseName + ".intervals";
			if (knownIndelsVCF != null)
				commandLine += " -known " + knownIndelsVCF;

			return commandLine;

		}

		public static String ReassignOneMappingQuality(String commandLine) {
			/*
			 * 
			 * java -jar GenomeAnalysisTK.jar -rf ReassignOneMappingQuality -RMQF
			 * 255 -RMQT 60
			 */

			commandLine += " -rf ReassignOneMappingQuality";
			return commandLine;

		}

		public static String LocalRealignment(int memory, String GATKDir,
				String bamFile, String reference, String baseName,
				String knownSNPVCF, String targetIntervals) {

			/*
			 * java -Xmx4g -jar GenomeAnalysisTK.jar \ -I input.bam \ -R ref.fasta \
			 * -T IndelRealigner \ -targetIntervals intervalListFromRTC.intervals \
			 * -o realignedBam.bam \ [-known /path/to/indels.vcf] \ [-compress 0]
			 * (this argument recommended to speed up the process *if* this is only
			 * a temporary file; otherwise, use the default value)
			 */

			String commandLine = "java -Xmx" + memory + "g -jar " + GATKDir
					+ "/GenomeAnalysisTK.jar " + "-I " + bamFile + " " + "-R "
					+ reference + " " + "-T IndelRealigner " + "-targetIntervals "
					+ targetIntervals + " " + "-o " + baseName + ".real.bam";
			if (knownSNPVCF != null)
				commandLine += " -known " + knownSNPVCF;

			return commandLine;

		}

	}
