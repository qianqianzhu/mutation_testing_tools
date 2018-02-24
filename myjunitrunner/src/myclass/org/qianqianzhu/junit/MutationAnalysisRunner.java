package org.qianqianzhu.junit;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
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


public class MutationAnalysisRunner extends BlockJUnit4ClassRunner {


	private Set<Mutation> killedMutants = new LinkedHashSet<Mutation>();

	private Set<Mutation> liveMutants;

	public MutationAnalysisRunner(Class<?> klass, Collection<Mutation> allMutants) throws InitializationError {
		super(klass);
		this.liveMutants = new LinkedHashSet<Mutation>(allMutants);
	}

	public MutationAnalysisRunner(Class<?> klass) throws InitializationError {
		this(klass, MutationPool.getMutants());
	}

	public Set<Mutation> getLiveMutants() {
		return liveMutants;
	}

	public Set<Mutation> getKilledMutants() {
		return killedMutants;
	}

	@Override
	protected List<TestRule> getTestRules(Object target) {
		List<TestRule> testRules = super.getTestRules(target);
		testRules.add(new Timeout(10000,TimeUnit.MILLISECONDS));   // adding timeout control (10s) for each test method
		System.out.println(testRules);
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
		}		
	}

	@Override
	protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
		System.out.println("Running method "+method.getName());
		SimpleRunListener resultListener = new SimpleRunListener();
		notifier.addListener(resultListener);

		// First run without mutants
		//ExecutionTracer.enable();
		boolean result;
		try{
			super.runChild(method, notifier);
		} catch(RuntimeException e){
			e.printStackTrace();
			result = true;
		}
		
		result = resultListener.hasFailure;
		System.out.println("Result without mutant: "+result);
		if(result) {
			System.out.println("Failure: "+resultListener.lastFailure.getMessage());
		}

		Set<Integer> touchedMutants = ExecutionTracer.getExecutionTracer().getTrace().getTouchedMutants();
		System.out.println("Touched mutants: "+touchedMutants.size());
		// Now run it for all touched mutants
		for(Integer mutantID : touchedMutants) {
			System.out.println("Current mutant: "+mutantID);
			Mutation m = MutationPool.getMutant(mutantID);
			if(killedMutants.contains(m)) {
				// System.out.println("Already dead: "+mutantID);
				continue;
			}

			ExecutionTracer.getExecutionTracer().clear();
			resultListener.hasFailure = false;
			MutationObserver.activateMutation(m);
			try{
				super.runChild(method, notifier);
			}catch(RuntimeException e){
				e.printStackTrace();
				resultListener.hasFailure = true;
			}
			MutationObserver.deactivateMutation(m);

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
				} catch(Throwable t) {
					System.out.println("Error: "+t);
					t.printStackTrace();
				}

				//} else {
				//	System.out.println("Remains live: "+mutantID);
			}
		}
		notifier.removeListener(resultListener);
		System.out.println("Done with "+method.getName());
	}
	
	
}
