package SBATCHscripts;

import general.ExtendedReader;
import general.ExtendedWriter;
import general.Functions;
import general.IOTools;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import Sequence.FastaSequences;

public class Blast {

	String time;
	String projectDir;

	String suffix;
	String split;
	String[] sep; 


	boolean pairedEnd;
	int insertLength;

	int maxKmer;
	int minKmer;
	int step;
	int mergeKmer;
	boolean strandSpecific;

	public Blast(){
		this.split = ".";
		projectDir = time =  null;

	}


	

	public static void run(Hashtable<String,String> T, ExtendedWriter generalSbatchScript, SBATCHinfo sbatch ,String timestamp,String inDir, String outDir,String fileName,String suffix, String time){
		String blastProgram = "blastn";
		if(T.containsKey("-blastp") || T.containsKey("-p")){
			System.out.println("running blastp based on -blastp or -p flag");
			blastProgram = "blastp";
		}
		else{
			System.out.println("running blastn based on -blastn or -n flag (Also Default)");
			blastProgram = "blastn";
		}
		boolean protein = false;
		if(blastProgram.compareTo("blastp") == 0) protein = true;
		
		String blastDB = null;
		if(T.containsKey("-blastDB"))
			blastDB = Functions.getValue(T, "-blastDB","/bubo/nobackup/uppnex/blast_databases/nt");
		else{
			if(protein)
				blastDB= "/bubo/nobackup/uppnex/blast_databases/nr";
			else
				blastDB= "/bubo/nobackup/uppnex/blast_databases/nt";
			System.out.println("blastDB is "+blastDB+ " to change add flag -blastDB <yourBlastDB>");
			
		}
		

		
		if(T.containsKey("-split")){
			int peptidedPerFile = Functions.getInt(T, "-split", 1000);
			ArrayList<String> fileNames = FastaSequences.split	(inDir,fileName, peptidedPerFile, suffix);
			for(int i = 0; i < fileNames.size();i++){
				runBlastFile(T, generalSbatchScript,sbatch, timestamp, inDir, outDir,fileNames.get(i), suffix,time,protein,blastDB);
			}

			try{
				ExtendedWriter EW = new ExtendedWriter(new FileWriter(outDir+"/scripts/"+timestamp+"_"+fileName+"_blast_removeTemp.sh"));
				EW.println("This file will remove all the temporary files generated to run pfam in parallel");
				for(int i = 0; i < fileNames.size();i++){
					String inFileBase = IOTools.getFileBase(fileNames.get(i), suffix);

					EW.println("rm "+fileNames.get(i));
					EW.println("rm "+inFileBase+".blast");
					EW.println();
				}
				EW.flush();
				EW.close();
			}
			catch(Exception E){E.printStackTrace();}

		}
		else{
			runBlastFile(T, generalSbatchScript,sbatch, timestamp, inDir, outDir,fileName, suffix,time,protein,blastDB);

		}
	}

	public static void runBlastFile(Hashtable<String,String> T, ExtendedWriter generalSbatchScript, SBATCHinfo sbatch ,String timestamp,String inDir, 
			String outDir,String fileName,String suffix, String time, boolean protein, String blastDB){
		try{
			String inFileBase = IOTools.getFileBase(fileName, suffix);
			String 	sbatchFileName = outDir+"/scripts/"+timestamp+"_"+fileName+"_blast.sbatch";
			generalSbatchScript.println("sbatch "+ sbatchFileName);

			ExtendedWriter EW = new ExtendedWriter(new FileWriter(sbatchFileName));
			
			sbatch.printSBATCHinfoCore(EW,outDir,timestamp,0,inFileBase+".blast", time);

			double Evalue = Functions.getDouble(T, "-evalue", 0.000000000000001);
			
			EW.println("# loading prerequistit modules at uppmax");
			EW.println("module load bioinfo-tools");
			EW.println("module load blast/2.2.28+");

			EW.println("# going to correct directory");
			EW.println("cd "+inDir);
			if(protein)
				EW.println(Blast.BlastPcommand(fileName,blastDB,inFileBase,Evalue));
			else
				EW.println(Blast.BlastNcommand(fileName,blastDB,inFileBase));
			EW.flush();
			EW.close();

		}
		catch(Exception E){E.printStackTrace();}
	}


	public static void merge(ArrayList<String> sequenceFiles, String dir, String outFile){

		System.out.println("merging blast files in folder "+dir+" to file "+ outFile);
		
		
		try{
			ExtendedWriter EW = new ExtendedWriter(new FileWriter(dir+"/"+outFile));
			ExtendedReader ER = new ExtendedReader(new FileReader(dir+"/"+sequenceFiles.get(0)));
			while(ER.more()){
				EW.println(ER.readLine());
			}
			ER.close();
			for(int i = 1; i < sequenceFiles.size();i++){	
				System.out.println(" now adding file "+ sequenceFiles.get(i));
				ER = new ExtendedReader(new FileReader(dir+"/"+sequenceFiles.get(i)));
				while(ER.more()){
					if(ER.lookAhead() != '#')
						EW.println(ER.readLine());
					else
						ER.skipLine();
				}
				ER.close();
			}
			EW.flush();
			EW.close();
		}
		catch(Exception E){E.printStackTrace();}
	}



	
	public static String BlastPcommand(  String inFile, String blastDB,String inBase, double Evalue ){

			String refName = blastDB;
			String [] temp =  blastDB.split("/");
			if(temp.length > 1)
				refName = temp[temp.length-1];


			return "blastp -query "+inFile+" -db "+blastDB+" -outfmt 6 -evalue "+ Evalue+" -out "+inBase+"."+refName+".blast";

	}


	public static String BlastNcommand( String inFile, String blastDB, String outDir ){

		String refName = blastDB;
		String [] temp =  blastDB.split("/");
		if(temp.length > 1)
			refName = temp[temp.length-1];


		return "blastn -d "+blastDB+" -i "+ inFile +" -m 8 -out "+inFile+"."+refName+".blast";

	}


	public static void buildBD(){
		
	}
	
	
	
}

