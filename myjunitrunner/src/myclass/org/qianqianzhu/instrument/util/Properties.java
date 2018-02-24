package org.qianqianzhu.instrument.util;

import java.util.HashSet;
import java.util.Set;

public class Properties {

	public static String CLASS_PREFIX = "";
	public static String REPORT_DIR;
	public static String TARGET_PROJECT_DIR;
	public static String TARGET_TEST_DIR;
	public static Set<String> classNeedInstrumentation = new HashSet<String>();
	public static Set<String> targetTest = new HashSet<String>();
	public static Set<String> methodNeedInstrumentation = new HashSet<String>();
		
	public static void setClassPrefix(String class_prefix){
		CLASS_PREFIX = class_prefix;
	}
	
	public static void setProperties(String target_project_dir,String target_test_dir,String repo_dir){
		TARGET_PROJECT_DIR = target_project_dir;
		TARGET_TEST_DIR = target_test_dir;
		REPORT_DIR = repo_dir;
		
		// set test classes
		if(target_test_dir.contains("/")){
			Set<String> testClasses = JunitHelper.findClasses(false,
					//"/Users/zhuqianqian/workspace/mutation/LunchEvosuite/sut/jpacman-framework/target/test-classes",
					//"/Users/zhuqianqian/workspace/mutation/LunchEvosuite/sut/jpacman-framework/target/test-classes");
					target_test_dir,target_test_dir);
			targetTest.addAll(testClasses);
		}else{
			targetTest.add(target_test_dir);
		}
		
		// set classes for instrumentation
		if(target_project_dir.contains("/")){
			Set<String> targetClasses = JunitHelper.findClasses(true,
					target_project_dir,
					target_project_dir);
			classNeedInstrumentation.addAll(targetClasses);
		}else{
			classNeedInstrumentation.add(target_project_dir.replace(".", "/"));
		}
		// remove test cases for instrumentation which added accidently
		classNeedInstrumentation.removeAll(targetTest);
		
		System.out.println("class need instrumentation:"+classNeedInstrumentation);
		
		// set methods for instrumentation
//		if(methodNeedInstrumentation.isEmpty())
		
		
	}

}
