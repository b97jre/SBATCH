package SBATCHscripts;


import java.io.File;

import general.ExtendedWriter;

import general.Functions;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;



public class HTseqCount {

	String gffFile;
	String programSpecific;
	String newSuffix;
	boolean countAll;

	public HTseqCount(){
	}

	public static boolean checkParameters(Hashtable<String, String> T){
		if(!T.containsKey("-gff")) {
			System.out.println("Mandatory htseq flag is missing");
			help();
			return false;
		}
		return true;
	}


	
	public boolean addParameters(Hashtable<String, String> T){
		this.gffFile = Functions.getValue(T,"-gff");
		
		if(T.containsKey("-programSpecific")) this.programSpecific = Functions.getValue(T, "-programSpecific");
		this.newSuffix = Functions.getValue(T, "-newSuffix", "htseqCount");
		this.countAll = T.containsKey("-countAll");
	
		return true;
	}

	
	public static void help(){
		System.out.println("");
		System.out.println("HTseq specific flags");
		System.out.println("Mandatory:");
		System.out.println(Functions.fixedLength("-gff gffFile",50)+"Location to the gff file that is used for this program");
		System.out.println("");
		System.out.println("Optional:");
		System.out.println(Functions.fixedLength("-countAll ",50)+"If you add this all reads will be counted for independent if they are mapped to multiple locations.");
		System.out.println(Functions.fixedLength("",50)+"This should only be used if you want to count things you know are mapped to multiple locaitons like rRNAs and not used for diff exp analysis");
		System.out.println("");
		System.out.println(Functions.fixedLength("-programSpecific [htseq specific]",50)+"");
		System.out.println(Functions.fixedLength("",50)+"For htseq-count specific flags go to http://www-huber.embl.de/users/anders/HTSeq/doc/count.html#count");
		System.out.println(Functions.fixedLength("",50)+"Remember to add the [ ]  around the flags");
		System.out.println("");
		System.out.println("A typical sbatchsctips line looks like this");
		System.out.println("java -jar /glob/johanr/bin/SBATCH.jar -pNr b2013265 -email johan.reimegard@scilifelab.se -time 2:00:00 -program htseq   -i STARmappings -suffix sorted.bam -gff ");
		System.out.println("");
		System.out.println(Functions.fixedLength("",50)+"To get htseq specific flags go to webpage ");
		try{	
			Runtime.getRuntime().exec("module load bioinfo-tools htseq/0.6.1");
			Runtime.getRuntime().exec("htseq-count");
		}
		catch(Exception E){
			E.printStackTrace();
		}
	}




	public  String runHTseqFile(ExtendedWriter EW, SBATCHinfo sbatch,
			String inDir, String bamFile,String suffix) {
		try {

			String outFile = Functions.getFileWithoutSuffix(bamFile, suffix)+"."+newSuffix;
			
			EW.println("# loading prerequistit modules at uppmax");
			EW.println("module load bioinfo-tools");
			EW.println("module load htseq/0.6.1");
			if(this.countAll)EW.println("module load samtools");

			EW.println("# going to correct directory");
			EW.println("cd " + inDir);
			EW.println("# Running program");
			if(!this.countAll)
				EW.println(HTseqCount.HTseqCommand(bamFile, this.gffFile, this.programSpecific, outFile+".tmp"));
			else{
				EW.println(HTseqCount.HTseqCommandCountAll(bamFile, this.gffFile, this.programSpecific, outFile+".tmp"));
			}
			EW.println("# moving from tmp to final file ");
			EW.println("mv "+outFile+".tmp "+outFile);
			EW.flush();
			EW.close();

		} catch (Exception E) {
			E.printStackTrace();
		}
		return null;
	}



	public static String HTseqCommand(String bamFile, String gffFile,
			String programSpecific, String outFile) {

		String Command = "htseq-count " + programSpecific+ " "+bamFile+" "+ gffFile+ " > "+outFile;
		return Command;
	}

	
	public static String HTseqCommandCountAll(String bamFile, String gffFile,
			String programSpecific, String outFile) {
		Hashtable<String, String> T = Functions.parseCommandLine(programSpecific.split(" "), false);

		T.put("-f", "sam");
		T.put("-a", "0");
		String parameters = "";
		
		System.out.println("Getting here HTseqCommansdfdCountAll");
		
		
		
		for (Entry<String, String> entry : T.entrySet())
		{
		    parameters += " "+entry.getKey() + " " + entry.getValue();
		}
		String Command1 = "samtools view -F 256 "+bamFile;
		String Command2 = "awk 'BEGIN {FS=\"\\t\"} {for (i = 1 ;i < 12 ; i++) printf \"%s\\t\",$i }{print \"NH:i:1\\tHI:i:1\\tAS:i:44\\tnM:i:0\"}'"; 
		String Command3 = "htseq-count " + parameters+ " - "+ gffFile+ " > "+outFile;
		String totalCommand =  Command1+" | "+Command2+" | "+Command3;
		
		return totalCommand;
	
	}

	
	public static String mergeHTseqSbatchScript(SBATCHinfo sbatch, ArrayList <Integer> Numbers, String FileParserJarFile,  String inDir,String suffix, String outFile){
		
		inDir = new File(inDir).getAbsolutePath();
		ExtendedWriter MergeScript = sbatch.printSBATCHInfoSTART("mergeHTseq",inDir, Numbers);
		MergeScript.print("java -jar "+FileParserJarFile+" -program HTSEQCOUNT " +
				" -i "+inDir+
				" -suffix "+suffix+
				" -o "+outFile);
		
		MergeScript.println();
		MergeScript.flush();
		MergeScript.close();
		
		return sbatch.getSBATCHfileName("mergeHTseq",inDir);
	}
	
	
}
