package org.qianqianzhu.instrument.util;

import java.util.Set;

public class FindTargetClasses {
	public static void main(String[] args){
		String projectDir = args[0];
		Properties.REPORT_DIR = projectDir;
		Set<String> classes = JunitHelper.findClasses(false,projectDir+"/classes",projectDir+"/classes");
		if(classes.isEmpty())
			System.out.println("No classes");
		else{
			for(String className : classes){
				//System.out.println(className);
				JunitHelper.writeMyData("targetClasses.txt",className+"\n");
			}
			System.out.println("Finished!");
		}
	}

}
