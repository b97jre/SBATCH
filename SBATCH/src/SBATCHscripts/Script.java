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

public class Script {

	String inFile;
	String projectDir;
	String time;
	boolean core;
	String parameters;

	public Script() {
		inFile = projectDir = time = null;
		core = true;
	}

	public static void main(String[] args) {

		int length = args.length;
		for (int i = 0; i < length; i++) {
			args[i] = args[i].trim();
			System.out.print(args[i] + " ");
		}
		System.out.println();
		Hashtable<String, String> T = Functions.parseCommandLine(args);
		FilterFastqSBATCH filter = new FilterFastqSBATCH();
		filter.run(T);
	}


	public void run(SBATCHinfo sbatch,Hashtable<String, String> T) {
		projectDir = Functions.getValue(T, "-wd", IOTools.getCurrentPath());
		if (T.containsKey("-i"))
			inFile = Functions.getValue(T, "-i");
		else{ 
			System.out.println("must contain script file");
			System.out.println("\n\nAborting run because of missing arguments for script.");
			return;
		}

		try {
			String info1 = null;
			if (inFile.indexOf("/") > -1) {
				String[] info = inFile.split("/");
				info1 = info[info.length - 1];
			} else {
				info1 = inFile;
			}
			ExtendedWriter EW = sbatch.printSBATCHInfoSTART(info1);
			ExtendedReader ER  = ExtendedReader.getFileReader(inFile);
			while(ER.more()){
				EW.println(ER.readLine());
			}
			ER.close();
			EW.println();
			EW.println();
			EW.println("Shell script copied from "+new File(inFile).getAbsolutePath());
			
			EW.flush();
			EW.close();
			String sbatchFileName = sbatch.getSbatchFileName(info1);
			
			System.out.println("Information writtend to "+sbatchFileName);
		} catch (Exception E) {
			E.printStackTrace();
		}
		
	}


}
