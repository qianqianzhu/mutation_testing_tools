package org.qianqianzhu.instrument.mutation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.qianqianzhu.instrument.mutation.Mutation;

public class MutationPool {
	
	// maps className -> method inside that class -> list of branches inside that method 
	private static Map<String, Map<String, List<Mutation>>> mutationMap = new HashMap<String, Map<String, List<Mutation>>>();

	// maps the mutationIDs assigned by this pool to their respective Mutations
	private static Map<Integer, Mutation> mutationIdMap = new HashMap<Integer, Mutation>();

	private static int numMutations = 0;

	public static Mutation addMutation(String classname, String methodname, String mutationname, AbstractInsnNode insn,
			AbstractInsnNode mutation, InsnList distance, int lineNo) {
		if (!mutationMap.containsKey(classname))
			mutationMap.put(classname, new HashMap<String, List<Mutation>>());

		if (!mutationMap.get(classname).containsKey(methodname))
			mutationMap.get(classname).put(methodname, new ArrayList<Mutation>());

		Mutation mutationObject = new Mutation(classname, methodname, mutationname,
		        numMutations++, insn, mutation, distance, lineNo);
		mutationMap.get(classname).get(methodname).add(mutationObject);
		mutationIdMap.put(mutationObject.getId(), mutationObject);

		return mutationObject;
	}
	
	public static Mutation addMutation(String classname, String methodname, String mutationname, AbstractInsnNode insn,
			InsnList mutation, InsnList distance, int lineNo) {
		if (!mutationMap.containsKey(classname))
			mutationMap.put(classname, new HashMap<String, List<Mutation>>());

		if (!mutationMap.get(classname).containsKey(methodname))
			mutationMap.get(classname).put(methodname, new ArrayList<Mutation>());

		Mutation mutationObject = new Mutation(classname, methodname, mutationname,
		        numMutations++, insn, mutation, distance,lineNo);
		mutationMap.get(classname).get(methodname).add(mutationObject);

		mutationIdMap.put(mutationObject.getId(), mutationObject);

		return mutationObject;
	}
	
	public static Map<Integer, Mutation> getMutationIdMap(){
		return mutationIdMap;
	}
	
	public static List<Mutation> getMutants() {
		return new ArrayList<Mutation>(mutationIdMap.values());
	}
	
	public static Mutation getMutant(int id) {
		return mutationIdMap.get(id);
	}
	
	public static int getMutationSize(){
		return mutationIdMap.size();
	}


}
