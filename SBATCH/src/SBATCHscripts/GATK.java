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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Map.Entry;

import SBATCHscripts.GeneralPairFile.Programs;
import Sequence.FastaSequences;

public class GATK {

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

	int memory;
	int cutoff;
	int padding;
	String job;

	boolean PRIORITIZE;
	boolean HaploTypeCaller;
	boolean rerun;
	String Reference;

	
	boolean phase1;
	boolean phase2;
	boolean genotype;
	boolean phase;
	
	
		public static void help(){
			
			

			System.out.println("Please select one of the following values for GATK:");
			System.out.println(Functions.fixedLength("-phase1", 30)+"All the steps for phase 1 in GATK pipeline");
			System.out.println(Functions.fixedLength("-phase2", 30)+"All the steps for phase 2 in GATK pipeline");
			System.out.println(Functions.fixedLength("-phaseSNPs", 30)+"All the steps for phasing of SNPs in GATK pipeline");
			System.out.println(Functions.fixedLength("-Genotype", 30)+"All the steps for Genotyping a sample in the GATK pipeline");

	System.out.println("\n\nTypical line should be something like:");
	System.out.println("\n\nThis is the current :");
	System.out
			.println("HTStools -p sbatch -GATK -phase1 -ReassignOneMappingQuality  -ReAlign -BQSR -BQSRprint -ReduceReads -suffix markDuplicates.bam -i bamFiles -R /bubo/home/h17/johanr/capsellaDir/nobackup/Cr_reference/assembly/Crubella_183.fa -time 40:00:00 -knownSNPs /bubo/home/h17/johanr/capsellaDir/nobackup/knownSNPs/Cg/GATK_13samps.flt_excluded_sites.vcf -targetIntervals /bubo/home/h17/johanr/capsellaDir/nobackup/Cr_reference/annotation/Crubella_183_only_exons_unique.bed");
	System.out
			.println("HTStools -p sbatch -GATK -phase1  -i INDIR -R REFERENCEFILE -time 4:00:00 -knownSNPs knownSNPsFile  [-knownIndels knownIndelsFile] [-targetIntervals bedFile] -suffix bam [ -preGATK -RGPU Barcode] [-RGSM Name]] ");
	System.out
			.println("HTStools -p sbatch -GATK -phase2 -i INDIR -R REFERENCEFILE -time 4:00:00 -L 100000 [-rerun] -X 2 -suffix reduced.bam");
	System.out
			.println("HTStools -p sbatch -GATK -phase2 -merge -i INDIR -R REFERENCEFILE -time 6-00:00:00 -X 23 -suffix vcf -prefix output.raw.snps.indels");
	System.out
			.println("HTStools -p sbatch -GATK -phaseSNPs -i INDIR -R REFERENCEFILE -time 6-00:00:00 -X 23 -knownSNPs knownSNPsFile -suffix ENDOFBAMfiles -respectPhase ");

			for (Programs info : EnumSet.allOf(Programs.class)) {
				System.out.println(info);
			}

			System.out.println();

		}
		
		
	
	
	
	public GATK() {
		projectDir = time = null;
		phase1=phase2=genotype=phase=false;
	}

	public static void main(String[] args) {

		int length = args.length;
		for (int i = 0; i < length; i++) {
			args[i] = args[i].trim();
			System.out.print(args[i] + " ");
		}
		System.out.println();
		Hashtable<String, String> T = Functions.parseCommandLine(args);
		GATK GATK = new GATK();
		GATK.run(T);
	}
	
	
	public boolean addParameters(Hashtable<String, String> T){

		String inDir, logDir;
		inDir = logDir = null;
		boolean allPresent = true;

		if (T.containsKey("-phase1")){
			phase1 = true;
			GATKphase1 test = new GATKphase1();
			return test.addParameters(T);
			
		}else if(T.containsKey("-phase2")){
			phase2 = true;
			return true;
			
		} else if(T.containsKey("-phaseSNPs")){
				phase = true;
				return true;
		} else if (T.containsKey("-Genotype")){ 
			this.genotype = true;
			return true;
		}
		else{ 
			help();
			return false;
		}
		
	}

	

	
	

	public void run(Hashtable<String, String> T){

		String inDir, logDir;
		inDir = logDir = null;
		boolean allPresent = true;

		SBATCHinfo sbatch = new SBATCHinfo();
		if (!sbatch.addSBATCHinfo(T)) {
			allPresent = false;
			return;
		}
		if (T.containsKey("-phase1") || T.containsKey("-phase2") || T.containsKey("-merge") || T.containsKey("-phase1")) {
			if (IOTools.fileExists(projectDir + "/" + this.Reference)) {
				this.Reference = projectDir + "/" + this.Reference;
			}
			if (IOTools.isDir(projectDir + "/" + inDir)) {
				inDir = projectDir + "/" + inDir;
			} else if (!IOTools.isDir(inDir)) {
				System.out.println("Neither " + inDir + " nor " + projectDir
						+ "/" + inDir + "was found");
				return;
			}
			//GATKInitial(sbatch, inDir, T);
		} else {
			System.out
					.println("\n\nAborting run because of missing arguments for GATK.");
			System.out.println("\n\nTypical line should be something like:");
			System.out.println("\n\nThis is the current :");
			System.out
					.println("HTStools -p sbatch -GATK -phase1 -ReassignOneMappingQuality  -ReAlign -BQSR -BQSRprint -ReduceReads -suffix markDuplicates.bam -i bamFiles -R /bubo/home/h17/johanr/capsellaDir/nobackup/Cr_reference/assembly/Crubella_183.fa -time 40:00:00 -knownSNPs /bubo/home/h17/johanr/capsellaDir/nobackup/knownSNPs/Cg/GATK_13samps.flt_excluded_sites.vcf -targetIntervals /bubo/home/h17/johanr/capsellaDir/nobackup/Cr_reference/annotation/Crubella_183_only_exons_unique.bed");
			System.out
					.println("HTStools -p sbatch -GATK -phase1  -i INDIR -R REFERENCEFILE -time 4:00:00 -knownSNPs knownSNPsFile  [-knownIndels knownIndelsFile] [-targetIntervals bedFile] -suffix bam [ -preGATK -RGPU Barcode] [-RGSM Name]] ");
			System.out
					.println("HTStools -p sbatch -GATK -phase2 -i INDIR -R REFERENCEFILE -time 4:00:00 -L 100000 [-rerun] -X 2 -suffix reduced.bam");
			System.out
					.println("HTStools -p sbatch -GATK -merge -i INDIR -R REFERENCEFILE -time 6-00:00:00 -X 23 -suffix vcf -prefix output.raw.snps.indels");
			System.out
					.println("HTStools -p sbatch -GATK -phaseSNPs -i INDIR -R REFERENCEFILE -time 6-00:00:00 -X 23 -knownSNPs knownSNPsFile -suffix ENDOFBAMfiles -respectPhase ");
		}
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

	
	public static void merge(ArrayList<String> sequenceFiles, String dir,String outFile){
		
		Collections.sort(sequenceFiles, new Comparator<String> () {
			@Override
			public int compare(String o1, String o2) {
				String prefix = IOTools.longestCommonPrefix(o1,o2);
				
				o1 = o1.substring(prefix.length());
				o2 = o2.substring(prefix.length());
				String suffix = IOTools.longestCommonSuffix(o1,o2);
				o1 = o1.substring(0,o1.indexOf(suffix));
				o2 = o2.substring(0,o2.indexOf(suffix));
				
				return Integer.parseInt(o1)-Integer.parseInt(o2);
			}
		});
		
		System.out.println("merging VCF files in folder " + dir + " to file "
				+ outFile);

		try {
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(dir + "/"
					+ outFile));
			ExtendedReader ER = new ExtendedReader(new FileReader(dir + "/"
					+ sequenceFiles.get(0)));
			System.out.println(" now adding file " + sequenceFiles.get(0));
			while (ER.more()) {
				EW.println(ER.readLine());
			}
			ER.close();
			for (int i = 1; i < sequenceFiles.size(); i++) {
				System.out.println(" now adding file " + sequenceFiles.get(i));
				ER = new ExtendedReader(new FileReader(dir + "/"
						+ sequenceFiles.get(i)));
				while (ER.more()) {
					if (ER.lookAhead() != '#')
						EW.println(ER.readLine());
					else
						ER.skipLine();
				}
				ER.close();
			}
			EW.flush();
			EW.close();
		} catch (Exception E) {
			E.printStackTrace();
		}
	}
	

}
