package org.qianqianzhu.instrument.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class JunitHelper {

	public static Set<String> findClasses(boolean withSlash,String origpath, String path){
		Set<String> res = new HashSet<String>();
		File dir = new File(path);
		File[] files = dir.listFiles();
		for(File file:files){
			if(file.isDirectory()){
				res.addAll(findClasses(withSlash,origpath,file.getAbsolutePath()));
				//System.out.println("Dir:"+file.getCanonicalPath());
			}
			else{
				String classname =file.getPath();
				int start= file.getPath().indexOf(origpath);
				if(start!=-1 && classname.endsWith(".class")){
					classname = classname.substring(start+origpath.length()+1);
					classname = classname.substring(0, classname.length()-6);
					if(!withSlash)
						classname = classname.replaceAll("/", ".");
					res.add(classname);
					//System.out.println("File:"+classname);
				}				
			}
		}
		return res;
	}

	public static void writeMyData(String file, String row){
		// Write to evosuite-report/mutant_statistics.csv
		try {
			File dir = new File(Properties.REPORT_DIR);
			if(!dir.exists()){
				boolean created = dir.mkdirs();
				if(!created){
					String msg = "Cannot create report dir: "+Properties.REPORT_DIR;
					System.err.println(msg);
					throw new RuntimeException(msg);
				}
				//logger.error(dir.getAbsolutePath());
				//System.err.println("creating directory for mutant_statistics: ");
			}

			File f = new File(Properties.REPORT_DIR+ File.separator + file);
			//logger.error("mutant_statistics.csv path is:"+Properties.REPORT_DIR+ File.separator +"mutant_statistics.csv");
			//System.err.println("mutant_statistics.csv path is:"+dir.getAbsolutePath()+ File.separator +"mutant_statistics.csv");
			BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
			out.write(row);
			out.flush();
			out.close();

		} catch (IOException e) {
			System.err.println("Error while writing mutant_statistics: " + e.getMessage());
		}
	}


	public static void wrtieMutationExecutionData(MutationExecutionResult mRes){
		Map<String, TestExecutionResult> testRes = mRes.getTestExecutionResults();
		Map<Integer, Map<String,Double>> weakMatrix = mRes.getWeakMutationResults();
		Map<Integer, Map<String,Boolean>> strongMatrix = mRes.getStrongMutationResults();

		if(!testRes.isEmpty()){
			for(String testName : testRes.keySet()){
				writeMyData("test_sum.csv",
						testName+","
								+testRes.get(testName).getExecutionTime()+"," 
								+testRes.get(testName).getTestResult()+"\n");
			}
		}
		if(!weakMatrix.isEmpty()){
			for(int mutationID : weakMatrix.keySet()){
				Map<String,Double> weakRes = weakMatrix.get(mutationID);
				for(String testName:weakRes.keySet()){
					writeMyData("weak_mutation_sum.csv",
							mutationID+","
									+testName+","
									+weakRes.get(testName)+"\n"
							);
				}
			}
		}
		if(!strongMatrix.isEmpty()){
			for(int mutationID : strongMatrix.keySet()){
				Map<String,Boolean> strongRes = strongMatrix.get(mutationID);
				for(String testName:strongRes.keySet()){
					writeMyData("strong_mutation_sum.csv",
							mutationID+","
									+testName+","
									+strongRes.get(testName)+"\n"
							);
				}

			}
		}

	}


}
