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

public class bowtie2SBATCH {

	String referenceFile;
	String time;
	String projectDir;
	String suffix;
	String split;
	String[] sep;
	boolean files;
	String forward;
	String reverse;

	int missmatch;
	int seedLength;
	int nrOfHits;
	int percentage;

	int M;
	int N;
	int L;
	int percent;
	int X;

	boolean normal;
	boolean strict;
	boolean sensitive;
	boolean superStrict;
	boolean ends;

	public bowtie2SBATCH() {
		this.referenceFile = "Database";
		this.split = ".";

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
		bowtie2SBATCH bowtie2 = new bowtie2SBATCH();
		//bowtie2.run(T);
	}

	public boolean checkParameters(Hashtable<String, String> T) {

		String inDir, outDir, logDir;
		inDir = outDir = logDir = null;
		boolean allPresent = true;
		this.normal = this.strict = this.superStrict = this.ends = this.sensitive = false;
		this.strict = true;

		if (T.containsKey("-normal")) {
			this.normal = true;
			this.sensitive = false;
		}

		if (T.containsKey("-strict")) {
			this.strict = true;
			this.sensitive = false;
		}

		if (T.containsKey("-superStrict")) {
			this.superStrict = true;
			this.sensitive = false;
		}

		if (T.containsKey("-ends")) {
			this.ends = true;
			this.sensitive = false;
		}

		if (T.containsKey("-sensitive")) {
			this.strict = false;
			this.sensitive = true;
		}


		if (T.containsKey("-o"))
			outDir = Functions.getValue(T, "-o", ".");
		else {
			System.out.println("must contain outDirectory -o");
			allPresent = false;
		}

		if (T.containsKey("-refFile"))
			referenceFile = Functions.getValue(T, "-refFile", ".");
		else {
			System.out.println("must contain referenceFile -refFile");
			allPresent = false;
		}


		L = Integer.parseInt(Functions.getValue(T, "-L", "22"));
		M = Integer.parseInt(Functions.getValue(T, "-M", "8"));
		N = Integer.parseInt(Functions.getValue(T, "-N", "1"));
		percent = Integer.parseInt(Functions.getValue(T, "-percent", "35"));
		X = Integer.parseInt(Functions.getValue(T, "-X", "1000"));

		return allPresent;
	}
	
	
	public void addParameters(Hashtable<String, String> T) {
		String inDir, outDir, logDir;
		inDir = outDir = logDir = null;
		boolean allPresent = true;
		this.normal = this.strict = this.superStrict = this.ends = this.sensitive = false;
		this.strict = true;

		if (T.containsKey("-normal")) {
			this.normal = true;
			this.sensitive = false;
		}

		if (T.containsKey("-strict")) {
			this.strict = true;
			this.sensitive = false;
		}

		if (T.containsKey("-superStrict")) {
			this.superStrict = true;
			this.sensitive = false;
		}

		if (T.containsKey("-ends")) {
			this.ends = true;
			this.sensitive = false;
		}

		if (T.containsKey("-sensitive")) {
			this.strict = false;
			this.sensitive = true;
		}


		if (T.containsKey("-o"))
			outDir = Functions.getValue(T, "-o", ".");
		else {
			System.out.println("must contain outDirectory -o");
			allPresent = false;
		}

		if (T.containsKey("-refFile"))
			referenceFile = Functions.getValue(T, "-refFile", ".");
		else {
			System.out.println("must contain referenceFile -refFile");
			allPresent = false;
		}


		L = Integer.parseInt(Functions.getValue(T, "-L", "22"));
		M = Integer.parseInt(Functions.getValue(T, "-M", "8"));
		N = Integer.parseInt(Functions.getValue(T, "-N", "1"));
		percent = Integer.parseInt(Functions.getValue(T, "-percent", "35"));
		X = Integer.parseInt(Functions.getValue(T, "-X", "1000"));

	}

	public void findOptimalValues(Hashtable<String, String> T,
			SBATCHinfo sbatch, String timeStamp, String outDir) {
		String forward, reverse;
		forward = reverse = null;
		boolean allPresent = true;
		if (T.containsKey("-1") && T.containsKey("-2")) {
			forward = Functions.getValue(T, "-1", ".");
			reverse = Functions.getValue(T, "-2", ".");
		} else if (T.containsKey("-U")) {
			forward = Functions.getValue(T, "-U", ".");
		} else {
			System.out.println("must contain sequence file");
			allPresent = false;
		}
		if (allPresent) {
			try {
				if (!IOTools.isDir(projectDir + "/scripts"))
					IOTools.mkDir(projectDir + "/scripts");
				ExtendedWriter EW = new ExtendedWriter(new FileWriter(
						projectDir + "/scripts/" + timeStamp + "_bowtie2.sh"));
				searchSpace(EW, sbatch, forward, reverse, outDir, timeStamp, 8);
				EW.flush();
				EW.close();
			} catch (Exception E) {
				E.printStackTrace();
			}
		}
	}


	public void searchSpace(ExtendedWriter generalSbatchScript,
			SBATCHinfo sbatch, String inFile1, String inFile2, String outDir,
			String timestamp, int M) {
		String refName = referenceFile;
		String[] temp = referenceFile.split("/");
		if (temp.length > 1)
			refName = temp[temp.length - 1];
		String f1 = inFile1;
		temp = f1.split("/");
		if (temp.length > 1)
			f1 = temp[temp.length - 1];
		String f2 = inFile2;
		temp = f2.split("/");
		if (temp.length > 1)
			f2 = temp[temp.length - 1];

		if (!IOTools.isDir(outDir + "/scripts"))
			IOTools.mkDir(outDir + "/scripts");
		int count = 0;
		for (int N = 0; N < 3; N++) {
			for (int L = 15; L < 25; L++) {
				try {
					String sbatchFileName = outDir + "/scripts/bowtie"
							+ refName + "_" + timestamp + "_" + count
							+ ".sbatch";
					generalSbatchScript.println("sbatch " + sbatchFileName);

					ExtendedWriter EW = new ExtendedWriter(new FileWriter(
							sbatchFileName));
					sbatch.printSBATCHinfo(EW, outDir, timestamp, count,
							"bowtie2");

					EW.println("cd " + projectDir);

					EW.println();
					EW.println();
					for (int percent = 30; percent < 66; percent = percent + 5) {
						bowtie2File(EW, referenceFile, inFile1, inFile2, outDir
								+ "/" + f1 + "." + f2 + "." + refName + "."
								+ count + ".sam", M, N, L, percent, 1000, 1);
					}
					count++;
					EW.flush();
					EW.close();
				} catch (Exception E) {
					E.printStackTrace();
				}

			}
		}
	}

	public static void bowtie2File(ExtendedWriter EW, String indexFile,
			String inFile1, String inFile2, String outFile, int M, int N,
			int L, int percent, int X, int nrOfThreads) {

		// bowtie2 --local -M 3 -N 2 -L 25 -i S,1,0.50 -x bowtie2Ref/GCAT -1
		// blend/blend.1.fastq -2 blend/blend.2.fastq -S bowtie2/blend.sam -t
		// --score-min L,0,0.50
		String bowtiecommand = "bowtie2 --local -M " + M + " -N " + N + " -L "
				+ L + " -X " + X + "  -t --score-min L,0,0." + percent;
		bowtiecommand += " -x " + indexFile;
		if (inFile2 != null) {
			bowtiecommand += " -1 " + inFile1;
			bowtiecommand += " -2 " + inFile2;
		} else {
			bowtiecommand += " -U " + inFile1;
		}
		bowtiecommand += " -S " + outFile + " &";
		EW.println("echo START");
		EW.println();
		EW.println("echo \"" + bowtiecommand + "\" 1>&2");
		EW.println(bowtiecommand);
		EW.println();
		EW.println("echo DONE");

	}

	public void bowtie2FileNormal(ExtendedWriter EW, String indexFile,
			String inFile1, String inFile2, String outFile, int nrOfThreads) {

		// bowtie2 --local -M 3 -N 2 -L 25 -i S,1,0.50 -x bowtie2Ref/GCAT -1
		// blend/blend.1.fastq -2 blend/blend.2.fastq -S bowtie2/blend.sam -t
		// --score-min L,0,0.50
		String bowtiecommand = "bowtie2 ";
		bowtiecommand += " --threads " + nrOfThreads;
		bowtiecommand += " -x " + indexFile;
		if (inFile2 != null) {
			bowtiecommand += " -1 " + inFile1;
			bowtiecommand += " -2 " + inFile2;
		} else {
			bowtiecommand += " -U " + inFile1;
		}

		bowtiecommand += " -S " + outFile + " &";
		EW.println("echo START");
		EW.println();
		EW.println("echo \"" + bowtiecommand + "\" 1>&2");
		EW.println(bowtiecommand);
		EW.println();
		EW.println("echo DONE");

	}

	public void bowtie2FileStrict(ExtendedWriter EW, String indexFile,
			String inFile1, String inFile2, String outFile,
			String notPairedFile, int nrOfThreads) {

		// bowtie2 --local -M 3 -N 2 -L 25 -i S,1,0.50 -x bowtie2Ref/GCAT -1
		// blend/blend.1.fastq -2 blend/blend.2.fastq -S bowtie2/blend.sam -t
		// --score-min L,0,0.50
		String bowtiecommand = "bowtie2";
		bowtiecommand += " -p " + nrOfThreads;
		bowtiecommand += " -x " + indexFile;
		if (inFile2 != null) {
			bowtiecommand += " -1 " + inFile1;
			bowtiecommand += " -2 " + inFile2;
		} else {
			bowtiecommand += " -U " + inFile1;
		}
		bowtiecommand += " --un-conc " + outFile + ".noHit.fastq";
		bowtiecommand += " -S " + outFile + " &";
		EW.println("echo START");
		EW.println();
		EW.println("echo \"" + bowtiecommand + "\" 1>&2");
		EW.println(bowtiecommand);
		EW.println();
		EW.println("echo DONE");

	}

	public void bowtie2FileSuperStrict(ExtendedWriter EW, String indexFile,
			String inFile1, String inFile2, String outFile,
			String notPairedFile, int nrOfThreads) {

		// bowtie2 --local -M 3 -N 2 -L 25 -i S,1,0.50 -x bowtie2Ref/GCAT -1
		// blend/blend.1.fastq -2 blend/blend.2.fastq -S bowtie2/blend.sam -t
		// --score-min L,0,0.50
		String bowtiecommand = "bowtie2 ";
		bowtiecommand += "--score-min L,0,-0.15 -M 10";
		bowtiecommand += " -p " + nrOfThreads;
		bowtiecommand += " -x " + indexFile;
		if (inFile2 != null) {
			bowtiecommand += " -1 " + inFile1;
			bowtiecommand += " -2 " + inFile2;
		} else {
			bowtiecommand += " -U " + inFile1;
		}
		bowtiecommand += " --un-conc " + outFile + ".noHit.fastq";
		bowtiecommand += " -S " + outFile + " &";
		EW.println("echo START");
		EW.println();
		EW.println("echo \"" + bowtiecommand + "\" 1>&2");
		EW.println(bowtiecommand);
		EW.println();
		EW.println("echo DONE");

	}

	public void bowtie2FileSuperStrictEnds(ExtendedWriter EW, String indexFile,
			String inFile1, String inFile2, String outFile,
			String notPairedFile, int nrOfThreads) {

		// bowtie2 --local -M 3 -N 2 -L 25 -i S,1,0.50 -x bowtie2Ref/GCAT -1
		// blend/blend.1.fastq -2 blend/blend.2.fastq -S bowtie2/blend.sam -t
		// --score-min L,0,0.50
		String bowtiecommand = "bowtie2 ";
		bowtiecommand += "--score-min L,0,-0.15 -M " + 5;
		bowtiecommand += " --threads " + nrOfThreads;
		bowtiecommand += " -x " + indexFile;
		if (inFile2 != null) {
			bowtiecommand += " -1 " + inFile1;
			bowtiecommand += " -2 " + inFile2;
		} else {
			bowtiecommand += " -U " + inFile1;
		}
		bowtiecommand += " --un-conc " + outFile + ".noHit.fastq";
		bowtiecommand += " -S " + outFile + " &";
		EW.println("echo START");
		EW.println();
		EW.println("echo \"" + bowtiecommand + "\" 1>&2");
		EW.println(bowtiecommand);
		EW.println();
		EW.println("echo DONE");

	}

	public void bowtie2File(ExtendedWriter generalSbatchScript,
			SBATCHinfo sbatch, String timestamp, String outDir, String forward,
			String reverse) {

		if (!IOTools.isDir(outDir))
			IOTools.mkDir(outDir);
		if (!IOTools.isDir(outDir + "/reports"))
			IOTools.mkDir(outDir + "/reports");
		if (!IOTools.isDir(outDir + "/scripts"))
			IOTools.mkDir(outDir + "/scripts");
		try {

			String sbatchFileName = outDir + "/scripts/" + timestamp
					+ "_bowtie2.sbatch";
			generalSbatchScript.println("sbatch " + sbatchFileName);

			ExtendedWriter EW = new ExtendedWriter(new FileWriter(
					sbatchFileName));

			sbatch.printSBATCHinfo(EW, outDir, timestamp, 0, "bowtie2");

			EW.println("module load bioinfo-tools");
			EW.println("module load bowtie2/2.0.0-beta6");
			EW.println();

			EW.println("cd " + this.projectDir);

			String[] temp = forward.split("/");
			String[] temp2 = reverse.split("/");
			String fileName1 = temp[temp.length - 1];
			String fileName2 = temp2[temp2.length - 1];
			temp = fileName1.split("\\.");
			temp2 = fileName2.split("\\.");
			String query = temp[0];
			for (int j = 1; j < temp2.length - 1; j++) {
				if (temp[j].compareTo(temp2[j]) == 0)
					query = query + "_" + temp[j];
			}

			int nrOfThreads = sbatch.getNrOfCores();
			temp = referenceFile.split("/");
			String refName = referenceFile;
			if (temp.length > 1)
				refName = temp[temp.length - 1];
			if (sensitive)
				bowtie2File(EW, referenceFile, projectDir + "/" + forward,
						projectDir + "/" + reverse, outDir + "/" + query + "_"
								+ refName + ".sam", M, N, L, percent, X, nrOfThreads);
			else if (normal)
				bowtie2FileNormal(EW, referenceFile,
						projectDir + "/" + forward, projectDir + "/" + reverse,
						outDir + "/" + query + "_" + refName + ".normal.sam", nrOfThreads);
			else if (strict)
				bowtie2FileStrict(EW, referenceFile,
						projectDir + "/" + forward, projectDir + "/" + reverse,
						outDir + "/" + query + "_" + refName + ".strict.sam",
						outDir + "/" + query + "_" + refName
								+ ".not_paired.fastq", nrOfThreads);
			else if (superStrict)
				bowtie2FileSuperStrict(EW, referenceFile, projectDir + "/"
						+ forward, projectDir + "/" + reverse, outDir + "/"
						+ query + "_" + refName + ".strict.sam", outDir + "/"
						+ query + "_" + refName + ".not_paired.fastq", nrOfThreads);
			else if (ends)
				bowtie2FileSuperStrictEnds(EW, referenceFile, projectDir + "/"
						+ forward, projectDir + "/" + reverse, outDir + "/"
						+ query + "_" + refName + ".strict.sam", outDir + "/"
						+ query + "_" + refName + ".not_paired.fastq", nrOfThreads);

			EW.println();
			EW.println();
			EW.println("wait");

			EW.flush();
			EW.close();

		} catch (Exception E) {
			E.printStackTrace();
		}
	}

}
