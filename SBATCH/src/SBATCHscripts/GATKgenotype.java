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

public class GATKgenotype {


	public String GATKdir;
	public String vcfFile;
	public String Reference;
	

	public void help(){



		System.out.println("Mandatory values for GATK genotype:");
		System.out.println(Functions.fixedLength("-i <inDirectory> ", 50)+"Directory where all files with suffix under will be used for one vcf file");
		System.out.println(Functions.fixedLength("-R <ReferenceFile.fa>", 50)+"Reference fasta file");
		System.out.println(Functions.fixedLength("-vcfFile <vcfFile.vcf>", 50)+"Vcf file with known SNPs");
		System.out.println();


		System.out.println();
		System.out.println();

	}

	public boolean addParameters(Hashtable<String, String> T) {

		GATKdir = Functions.getValue(T, "-GATKDir",
				"/sw/apps/bioinfo/GATK/2.5.2/");


		this.Reference = Functions.getValue(T, "-R");
		File ref = new File(this.Reference);
		this.Reference = ref.getAbsolutePath();

		vcfFile = Functions.getValue(T, "-vcfFile");
		ref = new File(this.vcfFile);
		vcfFile = ref.getAbsolutePath();

		return true;
	}

	public boolean checkParameters(Hashtable<String, String> T) {
		boolean allPresent = true;
		if (!T.containsKey("-R")){System.out.println("-R is missing" ); allPresent = false;}
		if (!T.containsKey("-vcfFile")){System.out.println("-vcfFile is missing" ); allPresent = false;}


		if(allPresent) return true;

		help();
		return false;
	}


			

	
	
	public void GATKGenotypeSample(ExtendedWriter EW,
			SBATCHinfo sbatch, String outDir, String bamFile){


				EW.println("cd " + outDir);

				String commandLine = Genotype(sbatch.getMemory(), this.GATKdir, bamFile,
						this.Reference, bamFile + ".Genotype.vcf",
						this.vcfFile, sbatch.getNrOfCores());
				EW.println(commandLine);
				System.out.println(commandLine);
				EW.println();
	}


	public static String Genotype(int memory, String GATKDir, String bamFile,
			String reference, String baseName, String knownSNPVCF, int nrOfThreads) {

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
				+ "-nt "+nrOfThreads+" "
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

}
