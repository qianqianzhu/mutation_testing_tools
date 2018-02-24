package org.qianqianzhu.junit;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.qianqianzhu.instrument.mutation.Mutation;
import org.qianqianzhu.instrument.mutation.MutationPool;
import org.qianqianzhu.instrument.util.JunitHelper;
import org.qianqianzhu.instrument.util.MutationExecutionResult;
import org.qianqianzhu.instrument.util.Properties;
import org.qianqianzhu.junit.JUnitExecutionListener;

public class MyJunitRunner {

	public static void main(String[] args) throws InitializationError {

		String target_project_dir = args[0];
		String target_test_dir = args[1];
		String repo_dir = args[2];
		boolean isStrong = args[3].equalsIgnoreCase("true");

		Properties.setProperties(target_project_dir, //target_project_dir
				target_test_dir,//target_test_dir, 
				repo_dir //repo_dir
				); 

		runMutationTesting(isStrong);
		//runTest();
		//runMutationAnalysis();
		System.exit(0);

	}

	static void runTest(){
		long startTime = System.currentTimeMillis();
		Iterator<String> iter = Properties.targetTest.iterator();
		while(iter.hasNext()){
			String oneTest = iter.next();
			Class<?> klass;
			try
			{
				klass = Class.forName(oneTest);
				//Request request = Request.aClass(test);
				Runner r;
				try {
					r = new BlockJUnit4ClassRunner(klass);
					JUnitCore junit = new JUnitCore();
					//junit.addListener(new TextListener(System.out));
					junit.addListener(new JUnitExecutionListener());
					junit.run(Request.runner(r));
				} catch (InitializationError e) {
					e.printStackTrace();
				}

			} catch(ClassNotFoundException e)
			{
				e.printStackTrace();
			} catch(RuntimeException e)
			{
				e.printStackTrace();
			} catch(LinkageError e)
			{
				e.printStackTrace();
			} 
		}
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);
		System.out.println("Total execution time: "+duration + " ms");
		System.exit(0);
	}

	static void runMutationAnalysis(){
		long startTime = System.currentTimeMillis();
		Iterator<String> iter = Properties.targetTest.iterator();
		while(iter.hasNext()){
			String oneTest = iter.next();
			Class<?> klass;
			try
			{
				klass = Class.forName(oneTest);
				//Request request = Request.aClass(test);
				Runner r;
				try {
					r = new MutationAnalysisRunner(klass);
					JUnitCore junit = new JUnitCore();
					//junit.addListener(new TextListener(System.out));
					junit.addListener(new JUnitExecutionListener());
					junit.run(Request.runner(r));
				} catch (InitializationError e) {
					e.printStackTrace();
				}

			} catch(ClassNotFoundException e)
			{
				e.printStackTrace();
			} catch(RuntimeException e)
			{
				e.printStackTrace();
			} catch(LinkageError e)
			{
				e.printStackTrace();
			} 
		}
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);
		System.out.println("Total execution time: "+duration + " ms");
		System.exit(0);
		
	}
	
	static MutationExecutionResult runTestWithMatrixCollector(Class<?> test, boolean isStrongMutation){

		try {
			MutantMatrixCollector runner = new MutantMatrixCollector(test,isStrongMutation);
			System.out.println("Running test class:"+test.getName());
			runner.run(new RunNotifier());
			return runner.getMutationExecutionResults();

		} catch (InitializationError e) {
			e.printStackTrace();
			return null;
		}

	}

	static void runMutationTesting(boolean isStrong){

		int totalTestNo = Properties.targetTest.size();
		Set<Mutation> killedMutants = new HashSet<Mutation>();
		System.out.println("Total test class:"+totalTestNo);
		int executedTestNo = 0;
		long startTime = System.currentTimeMillis();
		Iterator<String> iter = Properties.targetTest.iterator();
		//for(String oneTest:testClasses){
		while(iter.hasNext()){
			String oneTest = iter.next();
			System.out.println("Loading test class:"+oneTest);
			executedTestNo += 1;
			//String oneTest = target_test_class; //"org.apache.commons.math3.analysis.function.GaussianTest";
			Class<?> klass;

			try
			{
				klass = Class.forName(oneTest);
				MutationExecutionResult mutRes = runTestWithMatrixCollector(klass,isStrong);
				if(mutRes!=null){
					//JunitHelper.wrtieMutationExecutionData(mutRes);
					killedMutants.addAll(mutRes.getKilledMutation());
					//totalKilledMutants = + mutRes.getStrongMutationResults().size();
				}
			} catch(ClassNotFoundException e)
			{
				e.printStackTrace();
			} 
			catch(LinkageError e)
			{
				e.printStackTrace();
			}
			catch(Exception e){
				e.printStackTrace();
			}

			System.out.println("test class left:"+(totalTestNo-executedTestNo));
		}

		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);

		// write mutation info into file 
		for(Mutation mutant:MutationPool.getMutants())
			JunitHelper.writeMyData("mutants_info.csv", mutant.getId()+"\t"+mutant.getMutationType()+"\t"+mutant.getMutationInfo()+"\n");

		System.out.println("Total execution time: "+duration + " ms, mutationNo:" + MutationPool.getMutationSize()+ " killed mutationNo:"+killedMutants.size());
		System.exit(0);
	}


}
