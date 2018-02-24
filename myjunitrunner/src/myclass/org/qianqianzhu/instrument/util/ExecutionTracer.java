package org.qianqianzhu.instrument.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExecutionTracer {
	
	private static ExecutionTracer instance = null;
	private ExecutionTrace trace;
	
	public Set<Integer> touchedMutants = new HashSet<Integer>();
	public Map<Integer, Double> mutantDistances = new HashMap<Integer, Double>();
	
	private ExecutionTracer() {
		trace = new ExecutionTrace();
	}
	
	public static ExecutionTracer getExecutionTracer() {
		if (instance == null) {
			instance = new ExecutionTracer();
		}
		return instance;
	}
	
	public static void passedLine(String className, String methodName, int line) {
		ExecutionTracer tracer = getExecutionTracer();

		tracer.trace.linePassed(className, methodName, line);
	}
	
	
	public static void passedMutation(double distance, int mutationId) {
		ExecutionTracer tracer = getExecutionTracer();

		tracer.trace.mutationPassed(mutationId, distance);
	}
	
	public ExecutionTrace getTrace() {
		return trace;
	}

	public void clear() {
		trace = new ExecutionTrace();
	}
}
