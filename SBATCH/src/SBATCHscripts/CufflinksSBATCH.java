package SBATCHscripts;

import general.ExtendedWriter;
import java.io.FileWriter;
import general.Functions;
import general.IOTools;

import java.util.Hashtable;

public class CufflinksSBATCH {


	String GTF;
	String gtf;
	String suffix;

	public CufflinksSBATCH() {

	}


	public CufflinksSBATCH(Hashtable<String, String> T) {
		GTF = Functions.getValue(T, "-G",null);
		gtf = Functions.getValue(T, "-g",null);
		suffix = Functions.getValue(T, "-suffix");

	}


	public void run(Hashtable<String, String> T,
			ExtendedWriter generalSbatchScript, SBATCHinfo sbatch,
			String timestamp, String inDir, String outDir, String fileName,
			String suffix){
		if (!IOTools.isDir(outDir))
			IOTools.mkDir(outDir);
		if (!IOTools.isDir(outDir + "/reports"))
			IOTools.mkDir(outDir + "/reports");
		if (!IOTools.isDir(outDir + "/scripts"))
			IOTools.mkDir(outDir + "/scripts");
		try {
			String sbatchFileName = outDir + "/scripts/" + timestamp
					+ "_" + fileName + "_cufflinks.sbatch";
			generalSbatchScript.println("sbatch " + sbatchFileName);

			ExtendedWriter EW = new ExtendedWriter(new FileWriter(
					sbatchFileName));
			String[] split1 = outDir.split("/");
			sbatch.printSBATCHinfo(EW, outDir, timestamp,fileName,"cufflinks");

			EW.println("cd " + inDir);
			EW.println("export LD_LIBRARY_PATH=/home/delhomme/lib/");

			EW.println();
			EW.println();
			String fileBase = fileName.substring(0,
					fileName.indexOf(suffix) - 1);
			if (!IOTools.isDir(outDir + "/" + fileBase))
				IOTools.mkDir(outDir + "/" + fileBase);
			if (this.GTF == null)
				EW.println("cufflinks  -o " + outDir + "/" + fileBase
						+ " " + fileName);
			else
				EW.println("cufflinks -G " + this.GTF + " -o " + outDir
						+ "/" + fileBase + " " + fileName);

			EW.println();
			EW.println();
			EW.println("wait");
			EW.flush();
			EW.close();
		} catch (Exception E) {
			E.printStackTrace();
		}
	}



	public void cufflinksFile(ExtendedWriter EW, String inDir,String outDir,String fileName){			

		EW.println("cd " + inDir);
		EW.println("export LD_LIBRARY_PATH=/home/delhomme/lib/");

		EW.println();
		EW.println();
		if (!IOTools.isDir(outDir))
			IOTools.mkDir(outDir);
		if (this.GTF == null)
			EW.println("cufflinks  -o " + outDir 
					+ " " + fileName);
		else
			EW.println("cufflinks -G " + this.GTF + " -o " + outDir
					+ " " + fileName);

		EW.println();
		EW.println();
		EW.println("wait");

	}




}
