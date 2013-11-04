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
import java.util.Hashtable;

public class Merge {

	String time;
	String projectDir;
	String inDir;
	String outDir;
	String suffix;
	String prefix;

	boolean interactive;

	public Merge() {

	}

	public static void main(String[] args) {

		int length = args.length;
		for (int i = 0; i < length; i++) {
			args[i] = args[i].trim();
			System.out.print(args[i] + " ");
		}
		System.out.println();
		Hashtable<String, String> T = Functions.parseCommandLine(args);
		Merge general = new Merge();
		general.run(T);
	}

	public void run(Hashtable<String, String> T) {

		boolean allPresent = true;

		String timeStamp = Functions.getDateTime();
		SBATCHinfo sbatch = new SBATCHinfo();
		if (!sbatch.addSBATCHinfo(T))
			allPresent = false;

		if (T.containsKey("-i"))
			inDir = Functions.getValue(T, "-i", ".");
		else {
			System.out.println("must contain inDirectory -i");
			allPresent = false;
		}

		projectDir = Functions.getValue(T, "-pDir", IOTools.getCurrentPath());
		outDir = Functions.getValue(T, "-o", inDir);

		if (T.containsKey("-interactive")) {
			this.interactive = true;
		} else if (T.containsKey("-time"))
			time = Functions.getValue(T, "-time", ".");
		else {
			System.out
					.println("must contain likely time -time or interactive -interactive");
			allPresent = false;
		}

		if (T.containsKey("-prefix"))
			prefix = Functions.getValue(T, "-prefix", null);

		if (T.containsKey("-suffix"))
			suffix = Functions.getValue(T, "-suffix", null);

		if (allPresent)
			generalStart(T, sbatch, timeStamp);
		else
			System.out
					.println("\n\nAborting run because of missing arguments for cufflinks.");
	}

	public void generalStart(Hashtable<String, String> T, SBATCHinfo sbatch,
			String timeStamp) {
		if (!this.interactive) {
			try {
				if (!IOTools.isDir(projectDir + "/scripts"))
					IOTools.mkDir(projectDir + "/scripts");
				ExtendedWriter EW = new ExtendedWriter(new FileWriter(
						projectDir + "/scripts/" + timeStamp
								+ "_startSBATCHScript.sh"));

				generalDir(T, EW, sbatch, timeStamp, projectDir + "/" + inDir,
						projectDir + "/" + outDir);

				EW.flush();
				EW.close();

				System.out.println("To start all the scripts write ");
				System.out.println(projectDir + "/scripts/" + timeStamp
						+ "_startSBATCHScript.sh");

			} catch (Exception E) {
				E.printStackTrace();
			}
		} else {
			ExtendedWriter EW = null;
			generalDir(T, EW, sbatch, timeStamp, projectDir + "/" + inDir,
					projectDir + "/" + outDir);

		}

	}

	public void generalDir(Hashtable<String, String> T,
			ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
			String timestamp, String inDir, String outDir) {

		ArrayList<String> fileNamesSuffix = IOTools.getFilesSuffix(inDir,
				suffix);
		ArrayList<String> fileNamesPrefix = IOTools.getFilesPrefix(inDir,
				prefix);
		ArrayList<String> fileNames = (ArrayList<String>) IOTools.intersection(
				fileNamesSuffix, fileNamesPrefix);

		if (fileNames.isEmpty()) {
			if (!IOTools.isDir(outDir))
				IOTools.mkDir(outDir);
		} else {
			if (!IOTools.isDir(outDir))
				IOTools.mkDir(outDir);
			if (!IOTools.isDir(outDir + "/reports"))
				IOTools.mkDir(outDir + "/reports");
			if (!IOTools.isDir(outDir + "/scripts"))
				IOTools.mkDir(outDir + "/scripts");
			try {
				if (T.containsKey("-PFAM") || T.containsKey("-pfam"))
					PFAM.merge(fileNames, inDir,
							IOTools.longestCommonPrefix(fileNames)
									+ "merged.pfam");
			} catch (Exception E) {
				E.printStackTrace();
			}
		}
		ArrayList<String> subDirs = IOTools.getDirectories(inDir);
		for (int i = 0; i < subDirs.size(); i++) {
			generalDir(T, generalSbatchScript, sbatch, timestamp, inDir + "/"
					+ subDirs.get(i), outDir + "/" + subDirs.get(i));
		}
	}


}
