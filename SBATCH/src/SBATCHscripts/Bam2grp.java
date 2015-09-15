package SBATCHscripts;

import general.ExtendedWriter;
import general.Functions;

import java.io.File;
import java.util.Hashtable;

public class Bam2grp {

	private String reference;

	public Bam2grp() {
	}

	public static void help(){
		System.out.println();
		System.out.println();
		System.out.println("Mandatory values for Bam2Wig:");
		System.out.println(Functions.fixedLength("-ref <pathToReference>", 50)+"The reference sequences that the reads were mapped against");
	}


	public void setParameters(Hashtable<String, String> T) {
		this.reference = Functions.getValue(T, "-ref");
	}
	
	
	public static boolean checkParameters(Hashtable<String, String> T) {
		if(!T.containsKey("-ref")){
			help();
			return false;
		}
		return true;
	}
	
	
	
	
	
	

	public void bam2wigFile(ExtendedWriter EW,String jarFile,  String bamFileName,
			String finalOutDir) {

			
			File temp = new File(bamFileName);
			String fileNameString = temp.getName();

			EW.println("module load bioinfo-tools");
			EW.println("module load samtools");
			EW.println("cd " + finalOutDir);

			EW.println("samtools view "+bamFileName +"| java -jar "+jarFile+"  -p genomeCov -ref "+this.reference+" -o "+ fileNameString);
			EW.flush();
			EW.close();

	}

	

}
