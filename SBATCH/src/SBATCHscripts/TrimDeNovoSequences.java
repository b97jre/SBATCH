package SBATCHscripts;

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
import java.util.Hashtable;

public class TrimDeNovoSequences {

	String suffix;
	int length;
	float c; 

	public TrimDeNovoSequences() {}


	public boolean addParameters(Hashtable<String, String> T) {

		String inDir, outDir, logDir;
		inDir = outDir = logDir = null;
		boolean allPresent = true;


		if(!T.containsKey("-suffix")) return false;
	
		suffix = Functions.getValue(T, "-suffix", "fa");
		length =  Functions.getInt(T, "-length", 350);
		c =  (float)Functions.getDouble(T, "-c", 1.00);
		return true;
	}
	

	public void trimDeNovoFile(ExtendedWriter EW, String inDir, String fileName) {


			EW.println();
			EW.println("cd " + inDir);
			
			String filebase = Functions.getFileWithoutSuffix(fileName, suffix);
		
			filebase = extractAboveCommand(EW,filebase,350,suffix);
			filebase = CDHitEST(EW,filebase,1,suffix);
			
			findORFCommand(EW, filebase+"."+suffix);

			EW.println();
			EW.println();

			String peptideFileName = fileName.substring(0,
					fileName.lastIndexOf(suffix))
					+ "peptide.fa";
			String pfamFileName = fileName.substring(0,
					fileName.lastIndexOf(suffix))
					+ "peptide.pfam";
			runPfamCommand(EW, peptideFileName, pfamFileName);

			EW.println();
			EW.println();

			EW.flush();

	}

	public static void findORFCommand(ExtendedWriter EW, String inFile) {

		EW.println("module load java");
		EW.println();
		EW.println();
		EW.println("echo RUNNING findORFs");
		EW.println();
		EW.println("java  -Xmx2G -jar /glob/johanr/bin/HTStools.jar -p sequenceHandling ORFs -i "
				+ inFile);
		EW.println();
		EW.println("echo DONE");
	}

	
	public static String extractAboveCommand(ExtendedWriter EW, String inBase,int length,String suffix) {

		EW.println("module load java");
		EW.println();
		EW.println();
		EW.println("echo RUNNING extractAbove");
		EW.println();
		EW.println("java  -Xmx2G -jar /glob/johanr/bin/HTStools.jar -p sequenceHandling EXTRACTABOVE"
				+" -i "+ inBase+"."+suffix
				+" -length "+ length
				+" -suffix "+ suffix
				);
		EW.println();
		EW.println("echo DONE");
		return inBase+"."+length;
	}
	
	
	public static String CDHitEST(ExtendedWriter EW, String inBase,float C,String suffix) {

		EW.println("echo RUNNING cdHit-EST");
		EW.println();
		EW.println("/glob/johanr/bin/cd-hit-est"+
				" -i "+ inBase+"."+suffix +
				" -o "+ inBase+".cdHitEst_"+C+"."+suffix +
				" -c "+C
				);
		EW.println();
		EW.println("echo DONE");
		return inBase+".cdHitEst_"+C;
	}
	
	
	public static void runPfamCommand(ExtendedWriter EW, String inFile,
			String outFile) {

		EW.println("module load bioinfo-tools");
		EW.println("module load hmmer/3.0");
		EW.println("module load BioPerl/1.6.1_PERL5.10.1");
		EW.println("module load pfam_scan/1.3");

		// The pfam files are stored in
		// /bubo/nobackup/uppnex/pfam2011

		EW.println("echo RUNNING PFAM");
		EW.println();
		EW.println("pfam_scan.pl -fasta " + inFile
				+ " -dir /bubo/nobackup/uppnex/pfam2011 -outfile " + outFile);
		EW.println();
		EW.println("echo DONE");
	}

	public static void runPANTHERCommand(ExtendedWriter EW, String inFile,
			String outFile) {

		EW.println("module load bioinfo-tools");
		EW.println("module load blast/2.2.24");
		EW.println("PERL5LIB=/bubo/home/h17/johanr/src/pantherScore1.03/lib:$PERL5LIB");
		EW.println("export PERL5LIB");

		EW.println("echo RUNNING PANTHER");
		EW.println();
		EW.println("perl /bubo/home/h17/johanr/src/pantherScore1.03/pantherScore.pl  -l db -D B -V  -B /bubo/sw/apps/bioinfo/blast/2.2.24/bin/blastall  -i "
				+ inFile + " -o " + outFile);
		EW.println();
		EW.println("echo DONE");

	}

}
