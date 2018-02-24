package org.qianqianzhu.junit;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.qianqianzhu.instrument.mutation.Mutation;
import org.qianqianzhu.instrument.mutation.MutationObserver;
import org.qianqianzhu.instrument.mutation.MutationPool;
import org.qianqianzhu.instrument.util.ExecutionTracer;
import org.qianqianzhu.instrument.util.JunitHelper;
import org.qianqianzhu.instrument.util.MutationExecutionResult;
import org.qianqianzhu.instrument.util.TestExecutionResult;

public class MutantMatrixCollector extends BlockJUnit4ClassRunner {

	private final String className;
	private Set<Mutation> killedMutants = new LinkedHashSet<Mutation>();
	private Set<Mutation> liveMutants;
	private final boolean isStrongMutation;

	// store the test execution results without mutation
	private Map<String,TestExecutionResult> testExecutionResults = new HashMap<String,TestExecutionResult>();

	// store the weak mutation mutant-by-test matrix: mutation id -> testMethod -> weak mutation result (infection distance)
	private Map<Integer, Map<String, Double>> weakMatrix = new HashMap<Integer,Map<String,Double>>();

	// store the strong mutation mutant-by-test matrix: mutation id -> testMethod -> strong mutation result
	private Map<Integer, Map<String, Boolean>> strongMatrix = new HashMap<Integer,Map<String,Boolean>>();

	public MutantMatrixCollector(Class<?> klass, Collection<Mutation> allMutants, boolean isStrongMutation) throws InitializationError {
		super(klass);
		this.liveMutants = new LinkedHashSet<Mutation>(allMutants);
		this.className = klass.getName();
		this.isStrongMutation = isStrongMutation;
		// TODO: fix this: test execution will go wrong
		//super.setScheduler(new ParallelScheduler());   // run different methods in a test class in parallel
	}

	public MutantMatrixCollector(Class<?> klass, boolean isStrongMutation) throws InitializationError {
		this(klass, MutationPool.getMutants(),isStrongMutation);

	}

	public Set<Mutation> getLiveMutants() {
		return liveMutants;
	}

	public Set<Mutation> getKilledMutants() {
		return killedMutants;
	}

	public MutationExecutionResult getMutationExecutionResults(){
		MutationExecutionResult mRes = new MutationExecutionResult(testExecutionResults,weakMatrix,strongMatrix, killedMutants);
		return mRes;

	}

	@Override
	protected List<TestRule> getTestRules(Object target) {
		//		long timeout = 10000;
		List<TestRule> testRules = super.getTestRules(target);
		//		String targetClass = target.getClass().getName();
		//		if(execTime.get(targetClass)!=null)
		//			timeout = execTime.get(targetClass);
		//		if(timeout==0)
		//			timeout=1;

		//		System.out.println(target.getClass().getName()+" timeout: "+timeout);
		testRules.add(new Timeout(10000,TimeUnit.MILLISECONDS));   // adding timeout control (10s) for each test method
		//System.out.println(testRules);
		return testRules;
	}


	private static class SimpleRunListener extends RunListener {
		public boolean hasFailure = false;
		public Failure lastFailure = null;
		@Override
		public void testFailure(Failure failure) throws Exception {
			hasFailure = true;
			lastFailure = failure;
			super.testFailure(failure);
			//System.out.println("Failure");
		}
	}

	@Override
	protected void runChild(final FrameworkMethod method, final RunNotifier notifier){
		String fullMethodName = className + ":"+method.getName();
		System.out.println("Running method "+fullMethodName);
		SimpleRunListener resultListener = new SimpleRunListener();
		notifier.addListener(resultListener);
		//notifier.addListener(new TextListener(System.out));
		//notifier.addListener(new JUnitExecutionListener());

		// First run without mutants
		boolean result;
		long timer;  // timer for test method execution
		timer = System.currentTimeMillis();
		try{
			super.runChild(method, notifier);
		}catch(RuntimeException e){
			//e.printStackTrace();
			result = true;
		}

		timer = System.currentTimeMillis() - timer;
		if(timer == 0) // to avoid no time control
			timer = 1;
		result = resultListener.hasFailure;

		System.out.println("Result without mutant: "+result);

		if(result) {
			System.out.println("Failure: "+resultListener.lastFailure.getMessage());
			//return;  // skip mutation testing for failed tests
		}

		Set<Integer> touchedMutants = ExecutionTracer.getExecutionTracer().getTrace().getTouchedMutants();
		Map<Integer, Double> mutantDistances = ExecutionTracer.getExecutionTracer().getTrace().getMutantDistances();
		System.out.println("Touched mutants: "+touchedMutants.size());
		// add test execution results to testExecutionResults
		JunitHelper.writeMyData("test_sum.csv",
				fullMethodName+","
						+!result+"," 
						+timer+","
						+touchedMutants.size()+"\n");

		// Now run it for all touched mutants
		Iterator<Integer> itr = touchedMutants.iterator();		
		while(itr.hasNext()){
			int mutantID = itr.next();
			final Mutation m = MutationPool.getMutant(mutantID);
			System.out.println("activate: "+MutationPool.getMutant(mutantID).getMutationInfo());

			// adding touched mutation results to touchMatrix
			JunitHelper.writeMyData("touched_mutation_sum.csv",
					mutantID+","
							+m.getLineNumber()+","
							+fullMethodName+"\n"
							//+mutantDistances.get(m.getId())+"\n"
					);

			// adding weak mutation results to weakMatrix
			if(mutantDistances.get(mutantID)==0.0){
				JunitHelper.writeMyData("weak_mutation_sum.csv",
						mutantID+","
								+fullMethodName+"\n"
								//+mutantDistances.get(m.getId())+"\n"
						);
			}

			ExecutionTracer.getExecutionTracer().clear();

			// since we need to know each pair's result, comment these lines
//			if(killedMutants.contains(m)) {
//				System.out.println("Already dead: "+mutantID);
//				continue;
//			}

			// strong mutation session: load mutation by id
			if(isStrongMutation){
				System.out.println("Current mutant: "+mutantID);
				resultListener.hasFailure = false;
				// execute mutation only when it is weakly killed
				System.out.println(mutantDistances.get(mutantID));
				if(mutantDistances.get(mutantID)==0.0){
					// old approach
//					try{
//						MutationObserver.activateMutation(m);
//						//System.out.println(method.getName());
//						super.runChild(method, notifier);
//						MutationObserver.deactivateMutation(m);
//					}catch(Exception e){
//						e.printStackTrace();
//						resultListener.hasFailure = true;
//					}					

					// improved methods to void getting stuck in infinite loops without timeout
					MutationObserver.activateMutation(m);
					Thread strongTest = new Thread(new Runnable() {
						public void run() {
							//System.out.println(method.getName());
							MutantMatrixCollector.super.runChild(method, notifier);
						}
					});
					try {
						strongTest.start();
						strongTest.join(timer+10,10);
					} catch (Exception e){
						e.printStackTrace();	
						resultListener.hasFailure = true;
					}
					MutationObserver.deactivateMutation(m);
					// try to avoid memory leak
					strongTest=null;
				}

				// If killed
				if(resultListener.hasFailure != result) {
					System.out.println("Now killed: "+mutantID);
					try {
						liveMutants.remove(m);
					} catch(Throwable t) {
						System.out.println("Error: "+t);
						t.printStackTrace();
					}
					try {
						killedMutants.add(m);
						// adding strong mutation results to strongMatrix
						JunitHelper.writeMyData("strong_mutation_sum.csv",
								mutantID+","
										+fullMethodName+"\n"
								);
					} catch(Throwable t) {
						System.out.println("Error: "+t);
						t.printStackTrace();
					}
				}
			}
		}
		notifier.removeListener(resultListener);
		System.out.println("Done with "+method.getName());
	}

}
