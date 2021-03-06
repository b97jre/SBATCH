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

public class findORFs {

	String time;
	String projectDir;
	String suffix;

	public findORFs() {

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
		findORFs findORFs = new findORFs();
		findORFs.run(T);
	}

	public void run(Hashtable<String, String> T) {

		String inDir, outDir, logDir;
		inDir = outDir = logDir = null;
		boolean allPresent = true;

		String timeStamp = Functions
				.getValue(T, "-TS", Functions.getDateTime());
		SBATCHinfo sbatch = new SBATCHinfo();
		if (!sbatch.addSBATCHinfo(T)) {
			allPresent = false;
			return;
		}

		projectDir = Functions.getValue(T, "-pDir", IOTools.getCurrentPath());
		if (T.containsKey("-i"))
			inDir = Functions.getValue(T, "-i", ".");
		else {
			System.out.println("must contain inDirectory -i");
			allPresent = false;
		}
		if (!inDir.startsWith("/"))
			inDir = projectDir + "/" + inDir;

		outDir = Functions.getValue(T, "-o", inDir);
		if (!outDir.startsWith("/"))
			outDir = projectDir + "/" + outDir;

		if (T.containsKey("-time"))
			time = Functions.getValue(T, "-time", "");
		else {
			System.out
					.println("must contain likely time -time now set to 5:00:00 hours");
			allPresent = false;
		}

		suffix = Functions.getValue(T, "-suffix", "fa");
		if (allPresent) {
			findORFsInitiate(sbatch, timeStamp, inDir, outDir);
		} else
			System.out
					.println("\n\nAborting run because of missing arguments.");
	}

	public void findORFsInitiate(SBATCHinfo sbatch, String timeStamp,
			String inDir, String outDir) {
		try {
			if (!IOTools.isDir(projectDir + "/scripts"))
				IOTools.mkDir(projectDir + "/scripts");
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(projectDir
					+ "/scripts/" + timeStamp + ".findORFs.sh"));
			findORFsDir(EW, sbatch, timeStamp, inDir, outDir);

			EW.flush();
			EW.close();
		} catch (Exception E) {
			E.printStackTrace();
		}
	}

	public void findORFsDir(ExtendedWriter generalSbatchScript,
			SBATCHinfo sbatch, String timestamp, String inDir, String outDir) {

		ArrayList<String> fileNames = IOTools.getSequenceFiles(inDir, suffix);
		if (!fileNames.isEmpty()) {
			if (!IOTools.isDir(outDir))
				IOTools.mkDirs(outDir);
			try {
				for (int i = 0; i < fileNames.size(); i++)
					findORFsFile(generalSbatchScript, sbatch, timestamp,
							outDir, inDir, fileNames.get(i));
			} catch (Exception E) {
				E.printStackTrace();
			}
		}
		ArrayList<String> subDirs = IOTools.getDirectories(inDir);
		for (int i = 0; i < subDirs.size(); i++) {
			findORFsDir(generalSbatchScript, sbatch, timestamp, inDir + "/"
					+ subDirs.get(i), outDir + "/" + subDirs.get(i));
		}

	}

	public void findORFsFile(ExtendedWriter generalSbatchScript,
			SBATCHinfo sbatch, String timestamp, String inDir, String fileName,
			String suffix) {

		if (!IOTools.isDir(inDir))
			IOTools.mkDirs(inDir);
		if (!IOTools.isDir(inDir + "/reports"))
			IOTools.mkDir(inDir + "/reports");
		if (!IOTools.isDir(inDir + "/scripts"))
			IOTools.mkDir(inDir + "/scripts");
		try {

			String sbatchFileName = inDir + "/scripts/" + timestamp + "_"
					+ fileName + "_findORF.sbatch";
			generalSbatchScript.println("sbatch " + sbatchFileName);

			ExtendedWriter EW = new ExtendedWriter(new FileWriter(
					sbatchFileName));
			sbatch.printSBATCHinfo(EW, inDir, timestamp, fileName, "findORF");

			EW.println();
			EW.println("cd " + inDir);
			
			String filebase = Functions.getFileWithoutSuffix(fileName, suffix);
			
		
			filebase = extractAboveCommand(EW,filebase,350,suffix);
			filebase = CDHitEST(EW,filebase,1,suffix);
			

			findORFCommand(EW, fileName);

			EW.println();
			EW.println();

			String peptideFileName = fileName.substring(0,
					fileName.lastIndexOf(suffix))
					+ ".peptide.fa";
			String pfamFileName = fileName.substring(0,
					fileName.lastIndexOf(suffix))
					+ ".peptide.pfam";
			runPfamCommand(EW, peptideFileName, pfamFileName);

			EW.println();
			EW.println();

			String PANTHERFileName = fileName.substring(0,
					fileName.lastIndexOf(suffix))
					+ ".peptide.pfam";
			runPfamCommand(EW, peptideFileName, PANTHERFileName);

			EW.println();
			EW.println();

			EW.flush();
			EW.close();

		} catch (Exception E) {
			E.printStackTrace();
		}
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
		return inBase+"."+length+"."+suffix;
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
