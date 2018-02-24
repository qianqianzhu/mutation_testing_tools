package org.qianqianzhu.instrument.util;

import java.util.Map;
import java.util.Set;

import org.qianqianzhu.instrument.mutation.Mutation;

public class MutationExecutionResult {
	private Map<String,TestExecutionResult> testExecutionResults;
	private Map<Integer, Map<String,Double>> weakMatrix;
	private Map<Integer, Map<String,Boolean>> strongMatrix;
	private Set<Mutation> killedMutants;
	
	public MutationExecutionResult(Map<String,TestExecutionResult> testExecutionResults,
			                       Map<Integer, Map<String,Double>> weakMatrix,  
			                       Map<Integer, Map<String,Boolean>> strongMatrix,
			                       Set<Mutation> killedMutants){
		this.testExecutionResults = testExecutionResults;
		this.weakMatrix = weakMatrix;
		this.strongMatrix = strongMatrix;
		this.killedMutants = killedMutants;
	}
	
	public Map<String, TestExecutionResult> getTestExecutionResults(){
		return testExecutionResults;
	}
	
	public Map<Integer, Map<String,Double>> getWeakMutationResults(){
		return weakMatrix;
	}
	
	public Map<Integer, Map<String,Boolean>> getStrongMutationResults(){
		return strongMatrix;
	}
	
	public Set<Mutation> getKilledMutation(){
		return killedMutants;
	}
}
