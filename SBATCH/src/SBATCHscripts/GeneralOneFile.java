package SBATCHscripts;

import general.ExtendedWriter;
import general.Functions;
import general.IOTools;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;

public class GeneralOneFile {

	String time;
	String projectDir;
	String inDir;
	String outDir;
	String suffix;

	public GeneralOneFile() {

	}

	public static void main(String[] args) {

		int length = args.length;
		for (int i = 0; i < length; i++) {
			args[i] = args[i].trim();
			System.out.print(args[i] + " ");
		}
		System.out.println();
		Hashtable<String, String> T = Functions.parseCommandLine(args);
		GeneralOneFile general = new GeneralOneFile();

		SBATCHinfo sbatch = new SBATCHinfo();
		sbatch.addSBATCHinfo(T);
		general.run(T,sbatch);
	}



	public enum Programs {

		BLAST,
		BLAT,
		BOWTIE2,
		PANTHER,
		PFAM,
		STAR,
		TRIMFASTQFILES,
		TRIMDENOVOFILES,
		GETCOVERAGE,
		SAMTOOLS,
		GATK,
		CUTADAPT,
		ONEFILE,
		BAM2WIG,
		HTSEQ,
		HELP
	}	



	public ArrayList<Integer> run(Hashtable<String, String> T,SBATCHinfo sbatch) {

		boolean allPresent = true;


		if (T.containsKey("-i")){
			inDir = Functions.getValue(T, "-i");
			//System.out.println(inDir);
			inDir = new File(inDir).getAbsolutePath();
			//System.out.println(inDir);
		}else if(T.containsKey("-inDir")){
			inDir = Functions.getValue(T, "-inDir");
			//System.out.println(inDir);
			inDir = new File(inDir).getAbsolutePath();
		}
		else {
			help(T);
			return null;

		}	
		projectDir = Functions.getValue(T, "-pDir", IOTools.getCurrentPath());
		outDir = Functions.getValue(T, "-o", inDir);
		if(!IOTools.isDir(inDir)){
			IOTools.mkDirs(inDir); 
		}
		outDir = new File(outDir).getAbsolutePath();

		if (T.containsKey("-suffix"))
			suffix = Functions.getValue(T, "-suffix");
		else if(IOTools.isDir(inDir)){
			allPresent = false;
		}
		if(!checkParameters(T)) return null;

		if (allPresent)
			return generalStart(T, sbatch, sbatch.timeStamp);
		else{
			System.out.println("\n\nAborting run because of missing arguments.");
			help(T);
		}
		return null;
	}

	public ArrayList<Integer> generalStart(Hashtable<String, String> T, SBATCHinfo sbatch,
			String timeStamp) {
		try {
			if (!IOTools.isDir(projectDir + "/scripts"))
				IOTools.mkDir(projectDir + "/scripts");


			String program = Functions.getValue(T, "-program", "");

			String ShellScriptFileName  = projectDir
					+ "/scripts/" + timeStamp + "_start_"+program+"_SBATCHScript.sh";

			ExtendedWriter EW = ExtendedWriter.getFileWriter(ShellScriptFileName);


			if (IOTools.isDir(inDir)){
				generalDir(T, EW, sbatch, timeStamp, inDir, outDir);
			}
			else if (IOTools.fileExists(inDir)) {
				File file = new File(inDir);
				String folder = file.getParent();
				String fileName = file.getName();
				System.out.println("folder: " + folder);
				System.out.println("fileName: " + fileName);

				generalFileInitial(T, EW, sbatch, timeStamp, folder, folder,
						fileName);
			}

			EW.flush();
			EW.close();

			//sbatch.startSbatchScripts(ShellScriptFileName);
			if(sbatch.isInteractive()){
					System.out.println("All the shell scripts that have been created has been gathered here");
					System.out.println(ShellScriptFileName);
					System.out.println("Type line below to start the programs:");
					System.out.println("sh "+ShellScriptFileName);
					
			}
			else{
				System.out.println("All the sbatch scripts runned have been gathered here");
				System.out.println(ShellScriptFileName);

				if(!T.containsKey("-q")){	
					System.out.println("All scripts have been started. If you want to start them your self add extra flag -q");
					ArrayList<Integer> processes = sbatch.startSbatchScripts(ShellScriptFileName);
					return processes;
				}else{
					System.out.println("Type line below to initiate sbatch scripts:");
					System.out.println("sh "+ShellScriptFileName);
					
				}
			}
			return null;		

		} catch (Exception E) {
			E.printStackTrace();
		}
		return null;
	}

	public void generalFileInitial(Hashtable<String, String> T,
			ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
			String timestamp, String inDir, String outDir, String inFile) {
		if (!IOTools.isDir(outDir))
			IOTools.mkDir(outDir);
		if (!IOTools.isDir(outDir + "/reports"))
			IOTools.mkDir(outDir + "/reports");
		if (!IOTools.isDir(outDir + "/scripts"))
			IOTools.mkDir(outDir + "/scripts");

		generalFile(T, generalSbatchScript, sbatch, timestamp, inDir, outDir,
				inFile);
	}

	public void generalDir(Hashtable<String, String> T,
			ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
			String timestamp, String inDir, String outDir) {

		ArrayList<String> fileNames = IOTools.getSequenceFiles(inDir, suffix);

		if (fileNames != null && !fileNames.isEmpty()){
			if (!IOTools.isDir(outDir))
				IOTools.mkDirs(outDir);
			if (!IOTools.isDir(outDir + "/reports"))
				IOTools.mkDir(outDir + "/reports");
			if (!IOTools.isDir(outDir + "/scripts"))
				IOTools.mkDir(outDir + "/scripts");
			try {
				for (int i = 0; i < fileNames.size(); i++) {
					generalFile(T, generalSbatchScript, sbatch, timestamp,
							inDir, outDir, fileNames.get(i));
				}
			} catch (Exception E) {
				E.printStackTrace();
			}
		}
		ArrayList<String> subDirs = IOTools.getDirectories(inDir);
		if(subDirs != null){
			for (int i = 0; i < subDirs.size(); i++) {
				generalDir(T, generalSbatchScript, sbatch, timestamp, inDir + "/"
						+ subDirs.get(i), outDir + "/" + subDirs.get(i));
			}
		}
	}



	public void generalFile(Hashtable<String, String> T,
			ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
			String timestamp, String inDir, String outDir, String fileName) {

		String programName = Functions.getValue(T, "-program",Functions.getValue(T, "-p","help"));
		Programs program = Programs.valueOf(programName.toUpperCase());
		try{
			ExtendedWriter EW = null;
			switch (program) {

			case BLAST:
				Blast blast = new Blast(T); 
				blast.run(T, generalSbatchScript, sbatch, timestamp, inDir, outDir,
						fileName,suffix);
				break;
			case CUTADAPT:
				CutAdapt CA = new CutAdapt();
				if(CA.addParameters(T)){
					CA.trimFastQFile(sbatch,generalSbatchScript, inDir, outDir,
							fileName,suffix);
				}
				break;
			case BOWTIE2:
				EW = printSBATCHinfoSTART(sbatch,generalSbatchScript,outDir,fileName, "Bowtie2");
				Bowtie2 BT2 = new Bowtie2();
				BT2.addParameters(T);
				BT2.bowtie2File(EW, inDir, outDir,
						fileName,null, sbatch.getNrOfCores());	
				printSBATCHinfoEND(EW,"Bowtie2");
				break;
			case BAM2WIG:
				EW = printSBATCHinfoSTART(sbatch,generalSbatchScript,outDir,fileName, programName);
				Bam2grp B2G = new Bam2grp();
				B2G.setParameters(T);
				B2G.bam2wigFile(EW,sbatch.HTStoolsJarFile ,fileName, outDir);	
				printSBATCHinfoEND(EW,programName);
				break;
			case STAR:
				EW = printSBATCHinfoSTART(sbatch,generalSbatchScript,outDir,fileName, "STAR");
				STAR star = new STAR();
				star.addParameters(T,sbatch);
				star.STARFile(EW, inDir, outDir,fileName,null);	
				printSBATCHinfoEND(EW,"STAR");
				break;
			case BLAT:
				System.out.println("Not yet implemented");
				break;
			case PANTHER:
				System.out.println("Not yet implemented");
				break;
			case PFAM:
				PFAM.run(T, generalSbatchScript, sbatch, timestamp, inDir, outDir,
						fileName, suffix);
				break;
			case GETCOVERAGE:
				EW = printSBATCHinfoSTART(sbatch,generalSbatchScript,outDir,fileName, "BAMTOOLS_COVERGAE");
				break;
			case SAMTOOLS:
				EW = printSBATCHinfoSTART(sbatch,generalSbatchScript,inDir,fileName, "samtools");
				Samtools st = new Samtools();
				st.setParameters(T);
				st.samtoolsFile(EW, sbatch, fileName, inDir, suffix);
				printSBATCHinfoEND(EW,"samtools");
				break;
			case TRIMFASTQFILES:

				EW = printSBATCHinfoSTART(sbatch,generalSbatchScript,inDir,fileName, "TrimFastqFiles");
				TrimFastqFiles TFF = new TrimFastqFiles();
				if(TFF.addParameters(T))
					TFF.trimFastQFile(EW,sbatch, generalSbatchScript, inDir, fileName);
				printSBATCHinfoEND(EW,"TrimFastqFiles");
				break;
			case TRIMDENOVOFILES:
				EW = printSBATCHinfoSTART(sbatch,generalSbatchScript,inDir,fileName, programName);
				TrimDeNovoSequences TDNS = new TrimDeNovoSequences();
				if(TDNS.addParameters(T))
					TDNS.trimDeNovoFile(EW, inDir, fileName);
				printSBATCHinfoEND(EW,programName);
				break;
			case GATK:
				EW = printSBATCHinfoSTART(sbatch,generalSbatchScript,inDir,fileName, "GATKgenotype");
				GATKgenotype GATKgt = new GATKgenotype();
				if(GATKgt.addParameters(T))
					GATKgt.GATKGenotypeSample(EW,sbatch, inDir, fileName);
				printSBATCHinfoEND(EW,"GATKgenotype");
				break;
			case HTSEQ:
				EW = printSBATCHinfoSTART(sbatch,generalSbatchScript,inDir,fileName, "HTSEQ");
				HTseqCount HTseq = new HTseqCount();
				if(HTseq.addParameters(T))
					HTseq.runHTseqFile(EW, sbatch, inDir, fileName,suffix);
				printSBATCHinfoEND(EW,"HTSEQ");
				break;


			default: help(T);	

			}	
		}catch(Exception E){
			E.printStackTrace();
		}

	}




	private ExtendedWriter printSBATCHinfoSTART(SBATCHinfo sbatch,
			ExtendedWriter generalSbatchScript, String inDir, String forward, String program) {
		try {
			if(!IOTools.isDir(inDir)){
				IOTools.mkDirs(inDir); 
			}
			if (!IOTools.isDir(inDir + "/reports"))
				IOTools.mkDir(inDir + "/reports");
			if (!IOTools.isDir(inDir + "/scripts"))
				IOTools.mkDir(inDir + "/scripts");

			String fileWithoutSuffix = Functions.getFileWithoutSuffix(forward,this.suffix);
			String sbatchFile = inDir + "/scripts/" + sbatch.timeStamp
					+ "_" +fileWithoutSuffix+ "_"+ program+".sbatch";
			if(!sbatch.isInteractive()){
				generalSbatchScript.println("sbatch " + sbatchFile);
			}
				else{
				generalSbatchScript.println("sh " + sbatchFile);
				
			}
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(
					sbatchFile));

			sbatch.printSBATCHinfo(EW, inDir, sbatch.timeStamp, fileWithoutSuffix, program);

			return EW;
		}
		catch(Exception E){
			E.printStackTrace();
		}
		return null;
	}

	private void printSBATCHinfoEND(
			ExtendedWriter EW, String program) {
		try {
			EW.println("echo "+program+" finished running");
			EW.flush();
			EW.close();
		}
		catch(Exception E){
			E.printStackTrace();
		}
	}





	public boolean checkParameters(Hashtable<String, String> T) {

		String programName = Functions.getValue(T, "-program",Functions.getValue(T, "-p","help"));
		Programs program = Programs.valueOf(programName.toUpperCase());

		switch (program) {
		case BLAST:
			return Blast.checkParameters(T);
		case BAM2WIG:
			return Bam2grp.checkParameters(T);
		case CUTADAPT:
			return CutAdapt.checkParameters(T);
		case STAR:
			return STAR.checkParameters(T);
		case BLAT:
			System.out.println("Not yet implemented");
			return false;
		case PANTHER:
			System.out.println("Not yet implemented");
			return false;
		case PFAM:
			System.out.println("Not yet implemented");
			return true;
		case SAMTOOLS:
			Samtools st = new Samtools();
			return(st.checkParameters(T));
		case TRIMFASTQFILES:
			TrimFastqFiles TFF = new TrimFastqFiles();
			return TFF.addParameters(T);
		case TRIMDENOVOFILES:
			TrimDeNovoSequences TDNS = new TrimDeNovoSequences();
			return TDNS.addParameters(T);
		case BOWTIE2:
			Bowtie2 BT2 = new Bowtie2();
			return BT2.addParameters(T);
		case HTSEQ:
			return HTseqCount.checkParameters(T);
		case GATK:
			GATKgenotype GATKgt = new GATKgenotype();
			return GATKgt.checkParameters(T);
		default: help(T);
		return false;

		}			
	}


	public void help(Hashtable<String, String> T){

		System.out.println("Required flags:");
		System.out.println(Functions.fixedLength("-program <program>",30)+"Available programs are listed below.(-program blast)");
		System.out.println(Functions.fixedLength("-i <file|dir>", 30)+"InDirectory or inFile (-i fastaDir)");
		System.out.println(Functions.fixedLength("or", 30));
		System.out.println(Functions.fixedLength("-inDir <dir>", 30)+"InDirectory (-inDir fastaDir)");
		System.out.println();
		System.out.println("Required values if working on directory:");
		System.out.println(Functions.fixedLength("-suffix <yourSuffix>", 30)+" suffix of files (-suffix fa)");

		System.out.println();
		System.out.println();
		System.out.println("Other flags:");
		System.out.println(Functions.fixedLength("-o <dir>", 30)+" if you want the files to end up in another folder");
		System.out.println();
		System.out.println();
		System.out.println("Program specific flags");

		String programName = Functions.getValue(T, "-program",Functions.getValue(T, "-p","help"));
		Programs program = Programs.valueOf(programName.toUpperCase());

		switch (program) {
		case BLAST:
			Blast.help();
			break;
		case BOWTIE2:
			Bowtie2.help();
			break;
		case BLAT:
			System.out.println("Blat help not yet implemented see source code for proper flags");
			break;
		case PANTHER:
			System.out.println("Panther help not yet implemented see source code for proper flags");
			break;
		case PFAM:
			PFAM.help();
			break;
		case TRIMFASTQFILES:
			PFAM.help();
			break;
		default: 
			System.out.println("This program is not available as a single file program. Available programs are");
			for (Programs info : EnumSet.allOf(Programs.class)) {
				System.out.println(info);
			}
			break;

		}
	}

}

