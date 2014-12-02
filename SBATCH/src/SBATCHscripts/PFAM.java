package SBATCHscripts;

import general.ExtendedWriter;
import general.ExtendedReader;
import general.Functions;
import general.IOTools;

import java.io.FileWriter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;

import Sequence.FastaSequences;

public class PFAM {

	String pfamDB;
	ArrayList<String> tmpFiles;

	public PFAM() {

	}

	public PFAM(Hashtable<String, String> T) {
		pfamDB = Functions.getValue(T, "-pfamDatabase",
					"/bubo/nobackup/uppnex/pfam2011");

	}
	
	

	public static void help(){
		System.out.println("");
		System.out.println("PFAM specific flags");
		System.out.println(Functions.fixedLength("-pfamDatabase <PFAMDB>",30)+"Database that PFAM will run against. Default is /bubo/nobackup/uppnex/pfam2011");
		System.out.println(Functions.fixedLength("-split <nrOfSequencesPerRun>",30)+"will split up file for parralel runs");
		System.out.println("");
	}

	
	public static void run(Hashtable<String, String> T,
			ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
			String timestamp, String inDir, String outDir, String fileName,
			String suffix) {
		
		PFAM A = new PFAM(T);

		if (T.containsKey("-split")) {
			int peptidedPerFile = Functions.getInt(T, "-split", 1000);
			ArrayList<String> fileNames = FastaSequences.split(inDir, fileName,
					peptidedPerFile, suffix);
			for (int i = 0; i < fileNames.size(); i++) {
				A.runPfamFile(generalSbatchScript, sbatch, timestamp, inDir,
						outDir, fileNames.get(i));
			}
			try {
				ExtendedWriter EW = new ExtendedWriter(new FileWriter(outDir
						+ "/scripts/" + timestamp + "_" + fileName
						+ "_pfam_removeTemp.sh"));
				EW.println("#This file will remove all the temporary files generated to run pfam in parallel");
				for (int i = 0; i < fileNames.size(); i++) {
					String inFileBase = IOTools.getFileBase(fileNames.get(i),
							suffix);

					EW.println("rm " + fileNames.get(i));
					EW.println("rm " + inFileBase + ".pfam");
					EW.println();
				}
				EW.flush();
				EW.close();
			} catch (Exception E) {
				E.printStackTrace();
			}

		} else {
			A.runPfamFile(generalSbatchScript, sbatch, timestamp, inDir,
					outDir, fileName);

		}
	}

	public  void runPfamFile(ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
			String timestamp, String inDir, String outDir, String fileName) {

		try {

			String sbatchFileName = outDir + "/scripts/" + timestamp + "_"
					+ fileName + "_pfam.sbatch";
			generalSbatchScript.println("sbatch " + sbatchFileName);

			ExtendedWriter EW = new ExtendedWriter(new FileWriter(
					sbatchFileName));

			sbatch.printSBATCHinfo(EW, outDir, timestamp,fileName, "pfam");

			EW.println("# loading prerequistit modules at uppmax");
			EW.println("module load bioinfo-tools");
			EW.println("module load BioPerl/1.6.1_PERL5.10.1");
			EW.println("module load hmmer/3.0");
			EW.println("module load pfam_scan/1.3");

			EW.println("# going to correct directory");
			EW.println("cd " + inDir);


			EW.println(PfamCommand(fileName, pfamDB, "tmp."+fileName+".pfam"));
			EW.println("mv tmp."+fileName+".pfam "+fileName+".pfam");
			this.tmpFiles.add(fileName+".pfam");
			
			EW.flush();
			EW.close();

		} catch (Exception E) {
			E.printStackTrace();
		}
	}

	
	public  void getPfamFileName( String fileName) {
			this.tmpFiles.add(fileName+".pfam");
	}
	
	public static void merge(ArrayList<String> sequenceFiles, String dir,
			String outFile) {

		System.out.println("merging pfam files in folder " + dir + " to file "
				+ outFile);

		try {
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(dir + "/"
					+ outFile));
			ExtendedReader ER = new ExtendedReader(new FileReader(dir + "/"
					+ sequenceFiles.get(0)));
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

	public void merge( String inDir, String outDir,
			String outFile) {

		System.out.println("merging pfam files in folder " + inDir + " to file "
				+ outDir+"/"+ outFile);

		try {
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(outDir + "/"
					+ outFile));
			ExtendedReader ER = new ExtendedReader(new FileReader(inDir + "/"
					+ tmpFiles.get(0)));
			while (ER.more()) {
				EW.println(ER.readLine());
			}
			ER.close();
			for (int i = 1; i < tmpFiles.size(); i++) {
				System.out.println(" now adding file " + tmpFiles.get(i));
				ER = new ExtendedReader(new FileReader(inDir + "/"
						+ tmpFiles.get(i)));
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
	
	
	
	public static String PfamCommand(String fasta_file, String pfamDB, String pfamFile) {

		// more information can be added right now only default works

		/*
		 * Usage: pfam_scan.pl -fasta <fasta_file> -dir <directory location of
		 * Pfam files>
		 * 
		 * Additonal options:
		 * 
		 * -h : show this help -outfile <file> : output file, otherwise send to
		 * STDOUT -clan_overlap : show overlapping hits within clan member
		 * families (applies to Pfam-A families only) -align : show the
		 * HMM-sequence alignment for each match -e_seq <n> : specify hmmscan
		 * evalue sequence cutoff for Pfam-A searches (default Pfam defined)
		 * -e_dom <n> : specify hmmscan evalue domain cutoff for Pfam-A searches
		 * (default Pfam defined) -b_seq <n> : specify hmmscan bit score
		 * sequence cutoff for Pfam-A searches (default Pfam defined) -b_dom <n>
		 * : specify hmmscan bit score domain cutoff for Pfam-A searches
		 * (default Pfam defined) -pfamB : search against Pfam-B* HMMs (uses
		 * E-value sequence and domain cutoff 0.001), in addition to searching
		 * Pfam-A HMMs -only_pfamB : search against Pfam-B* HMMs only (uses
		 * E-value sequence and domain cutoff 0.001) -as : predict active site
		 * residues for Pfam-A matches -json [pretty] : write results in JSON
		 * format. If the optional value "pretty" is given, the JSON output will
		 * be formatted using the "pretty" option in the JSON module -cpu <n> :
		 * number of parallel CPU workers to use for multithreads (default all)
		 */

		return "pfam_scan.pl -fasta " + fasta_file + " -dir " + pfamDB
				+ " -outfile " + pfamFile;
	}

}
