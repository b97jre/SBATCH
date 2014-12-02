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

public class Bowtie2 {

	String referenceFile;
	String extra;
	//	String time;
	//	String projectDir;
	//	String suffix;
	//	String split;
	//	String[] sep;
	//	boolean files;
	//	String forward;
	//	String reverse;

	//	int missmatch;
	//	int seedLength;
	//	int nrOfHits;
	//	int percentage;

	//	boolean normal;
	//	boolean strict;
	//	boolean sensitive;
	//	boolean superStrict;
	//	boolean ends;

	public Bowtie2() {
		this.referenceFile = "Database";
		this.extra = null;

	}

	public static void main(String[] args) {

		int length = args.length;
		for (int i = 0; i < length; i++) {
			args[i] = args[i].trim();
			System.out.print(args[i] + " ");
		}
		System.out.println();
		Hashtable<String, String> T = Functions.parseCommandLine(args);
		Bowtie2 bowtie2 = new Bowtie2();
		//bowtie2.run(T);
	}



	public static void help(){
		System.out.println("");
		System.out.println("Bowtie2 specific flags");
		System.out.println("Mandatory");
		System.out.println(Functions.fixedLength("-refBase bowtie2File",50)+"The reference that you want to align against");
		System.out.println("");
		System.out.println("Optional");
		System.out.println(Functions.fixedLength("-programSpecific [bowtieSpecific]",50)+"Here you can add any flags for Bowtie2");
		System.out.println(Functions.fixedLength("",50)+"Remember to add the [ ]  around the flags");
		System.out.println(Functions.fixedLength("",50)+"e.g. [--local]  for local instead of end to end");


	}



	public boolean addParameters(Hashtable<String, String> T) {
		boolean allPresent = true;

		if (T.containsKey("-refBase")){
			String referenceBase = Functions.getValue(T, "-refBase", ".");
			if(IOTools.fileExists(referenceBase+".1.bt2")){
				this.referenceFile = new File(referenceBase+".1.bt2").getAbsolutePath();
				this.referenceFile = this.referenceFile.substring(0,this.referenceFile.indexOf(".1.bt2"));

			}else{
				System.out.println("Could not find bowtie2 reference files based on: "+referenceBase);

				allPresent = false;
			}
		}
		else {
			System.out.println("must contain referenceBase -refBase");
			allPresent = false;
		}
		if (T.containsKey("-programSpecific")){
			this.extra = Functions.getValue(T, "-programSpecific");
			System.out.println("These bowtie2 specific flags will be added");
			System.out.println(this.extra);
			System.out.println();

		}




		return allPresent;

	}

	public void Bowtie2File(ExtendedWriter generalSbatchScript, SBATCHinfo sbatch, String inDir, String outDir, String forward, String reverse) {

		String commonName = forward;
		if(reverse!= null)
			commonName = Functions.getCommonPrefix(forward, reverse);

		outDir = outDir+"/"+commonName;
		if (!IOTools.isDir(outDir))
			IOTools.mkDirs(outDir);
		if (!IOTools.isDir(outDir + "/reports"))
			IOTools.mkDir(outDir + "/reports");
		if (!IOTools.isDir(outDir + "/scripts"))
			IOTools.mkDir(outDir + "/scripts");

		try {

			String sbatchFileName = outDir + "/scripts/" + sbatch.timeStamp+"_"+commonName
					+ "_BOWTIE2.sbatch";
			if (!sbatch.interactive)
				generalSbatchScript.println("sbatch " + sbatchFileName);
			else {
				sbatchFileName = outDir + "/scripts/"+commonName+"STAR.sh";
				generalSbatchScript.println("sh " + sbatchFileName);
			}
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(sbatchFileName));
			sbatch.printSBATCHinfo(EW, outDir, sbatch.timeStamp, commonName, "BOWTIE2");

			bowtie2File(EW,inDir,outDir, forward, reverse,sbatch.getNrOfCores() );

			EW.flush();
			EW.close();

		} catch (Exception E) {
			E.printStackTrace();
		}
	}	

	public void bowtie2File(ExtendedWriter EW, String inDir, String outDir,
			String forward,String reverse, int nrOfThreads) {


		EW.println();
		EW.println("module load bioinfo-tools");
		EW.println("module load bowtie2/2.2.3");
		EW.println();

		EW.println("cd " + inDir);


		String query=IOTools.getFileBase(new File(inDir+"/"+forward).getName(),null);
		String forwardFile = inDir + "/" + forward;
		String reverseFile = null;
		if(reverse != null)reverseFile = inDir + "/" + reverse;
		if(reverse != null){
			String reverseName = new File(reverse).getName();
			String forwardName = new File(forward).getName();
			query = Functions.getCommonPrefix(reverseName, forwardName);
		}

		String refName = referenceFile;

		String [] temp = refName.split("/");
		if (temp.length > 1)
			refName = temp[temp.length - 1];


		String samFile = outDir + "/" + query + "_"
				+ refName + ".sam";

		bowtie2File(EW, referenceFile, forwardFile,
				reverseFile, samFile, nrOfThreads,extra);

		EW.println();
		EW.println();

		Samtools.sam2bam(EW, samFile,
				-1, -1,-1, true, true, true, true, true);


	}



	public static void bowtie2File(ExtendedWriter EW, String indexFile,
			String inFile1, String inFile2, String outFile,  int nrOfThreads, String extra) {

		// bowtie2 --local -M 3 -N 2 -L 25 -i S,1,0.50 -x bowtie2Ref/GCAT -1
		// blend/blend.1.fastq -2 blend/blend.2.fastq -S bowtie2/blend.sam -t
		// --score-min L,0,0.50
		String bowtiecommand = "bowtie2 ";
		bowtiecommand += " --threads " + nrOfThreads;
		bowtiecommand += " -x " + indexFile;
		if (inFile2 != null) {
			bowtiecommand += " -1 " + inFile1;
			bowtiecommand += " -2 " + inFile2;
			bowtiecommand += " --un-conc " + outFile + ".noHit.fastq";
		} else {
			bowtiecommand += " -U " + inFile1;
			bowtiecommand += " --un-gz " + outFile + ".noHit.fastq.gz";
		}

		bowtiecommand += " -S " + outFile ;

		if(extra != null)
			bowtiecommand += " "+extra;

		EW.println("echo START");
		EW.println();
		EW.println("echo \"" + bowtiecommand + "\" 1>&2");
		EW.println(bowtiecommand);
		EW.println();
		EW.println("echo DONE");

	}



}
