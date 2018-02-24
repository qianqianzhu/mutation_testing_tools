package org.qianqianzhu.instrument.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExecutionTrace {
	
	public Set<Integer> touchedMutants = Collections.synchronizedSet(new HashSet<Integer>());
	public Map<Integer, Double> mutantDistances = Collections.synchronizedMap(new HashMap<Integer, Double>());
	public Map<String, Map<String, Map<Integer, Integer>>> coverage = Collections.synchronizedMap(new HashMap<String, Map<String, Map<Integer, Integer>>>());

	public void mutationPassed(int mutationId, double distance) {

		touchedMutants.add(mutationId);
		if (!mutantDistances.containsKey(mutationId)) {
			mutantDistances.put(mutationId, distance);
		} else {
			mutantDistances.put(mutationId, Math.min(distance, mutantDistances.get(mutationId)));
		}
	}

	public Set<Integer> getTouchedMutants() {
		return touchedMutants;
	}
	
	public Map<Integer, Double> getMutantDistances(){
		return mutantDistances;
	}

	public Map<String, Map<String, Map<Integer, Integer>>> getLineCoverage(){
		return coverage;
	}
	
	public void linePassed(String className, String methodName, int line) {

		if (!coverage.containsKey(className)) {
			coverage.put(className, new HashMap<String, Map<Integer, Integer>>());
		}

		if (!coverage.get(className).containsKey(methodName)) {
			coverage.get(className).put(methodName, new HashMap<Integer, Integer>());
		}

		if (!coverage.get(className).get(methodName).containsKey(line)) {
			coverage.get(className).get(methodName).put(line, 1);
		} else {
			coverage.get(className).get(methodName).put(line,
					coverage.get(className).get(methodName).get(line) + 1);
		}
		
	}

}
