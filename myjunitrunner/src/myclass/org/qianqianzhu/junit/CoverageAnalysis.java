package org.qianqianzhu.junit;

import java.util.Iterator;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runners.model.InitializationError;
import org.qianqianzhu.instrument.line.LinePool;
import org.qianqianzhu.instrument.util.Properties;

public class CoverageAnalysis {

	public static void main(String args[]) throws ClassNotFoundException{

		Properties.setProperties("nl.tudelft.jpacman.board.BoardFactory", //target_project_dir
				"/Users/zhuqianqian/workspace/mutation/LunchEvosuite/sut/jpacman-framework/target/test-classes",//target_test_dir, 
				"/Users/zhuqianqian/workspace/mutation/report/jpacman-framework(BoardFactory)" //repo_dir
				); 

		int totalTestNo = Properties.targetTest.size();
		System.out.println("Total test class:"+totalTestNo);
		int executedTestNo = 0;
		long startTime = System.currentTimeMillis();
		Iterator<String> iter = Properties.targetTest.iterator();
		while(iter.hasNext()){
			String oneTest = iter.next();
			System.out.println("Loading test class:"+oneTest);
			executedTestNo += 1;
			//String oneTest = target_test_class; //"org.apache.commons.math3.analysis.function.GaussianTest";
			Class<?> klass;
			klass = Class.forName(oneTest);
			runTest(klass);
			System.out.println("test class left:"+(totalTestNo-executedTestNo));
			//System.out.println(LinePool.getLineMap().toString());
			//LinePool.reset();
		}
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);
		System.out.println("Total execution time: "+duration + " ms" );

	}

	static void runTest(Class<?> test){
		try {
			//Request request = Request.aClass(test);
			Runner r = new CoverageMatrixCollector(test);
			JUnitCore junit = new JUnitCore();
			//junit.addListener(new TextListener(System.out));
			junit.addListener(new JUnitExecutionListener());
			junit.run(Request.runner(r));
		} catch (InitializationError e) {
			e.printStackTrace();
		}

	}

}
