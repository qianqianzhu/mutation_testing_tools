package org.qianqianzhu.instrument.mutation;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;

public class Mutation {
	
	private final int id;

	private final String className;

	private final String methodName;

	private final String mutationName;

	private final AbstractInsnNode original;

	private final InsnList mutation;

	private final InsnList infection;

	private final int lineNo;

	public Mutation(String className, String methodName, String mutationName, int id, AbstractInsnNode original,
			AbstractInsnNode mutation, InsnList distance, int lineNo) {
		this.className = className;
		this.methodName = methodName;
		this.mutationName = mutationName;
		this.id = id;
		this.original = original;
		this.mutation = new InsnList();
		this.mutation.add(mutation);
		this.infection = distance;
		this.lineNo = lineNo;
	}
		

	public Mutation(String classname, String methodname, String mutationname, int id, AbstractInsnNode original,
			InsnList mutation, InsnList distance, int lineNo) {
		this.className = classname;
		this.methodName = methodname;
		this.mutationName = mutationname;
		this.id = id;
		this.original = original;
		this.mutation = mutation;
		this.infection = distance;
		this.lineNo = lineNo;
	}


	public InsnList getInfectionDistance() {
		return infection;
	}

	public int getId() {
		return id;
	}

	public InsnList getMutation() {
		return mutation;
	}
	
	public int getLineNumber(){
		return lineNo;
	}
	
	public String getMutationType(){
		return mutationName.substring(0,mutationName.indexOf(" "));
		
	}
	
	public String getMutationInfo(){
		return className.replace("/", ".") + "." + methodName + ":" + lineNo
		        + " - " + mutationName;
		
	}

	public static InsnList getDefaultInfectionDistance() {
		InsnList defaultDistance = new InsnList();
		defaultDistance.add(new LdcInsnNode(0.0));
		return defaultDistance;
	}

}
