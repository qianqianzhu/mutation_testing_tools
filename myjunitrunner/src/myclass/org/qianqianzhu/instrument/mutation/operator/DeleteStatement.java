package org.qianqianzhu.instrument.mutation.operator;

import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.qianqianzhu.instrument.mutation.Mutation;
import org.qianqianzhu.instrument.mutation.MutationPool;
import org.qianqianzhu.instrument.mutation.operator.MutationOperator;

public class DeleteStatement implements MutationOperator {
	
	public static final String NAME = "DeleteStatement";

	public boolean isApplicable(AbstractInsnNode asmNode) {
		return  asmNode instanceof MethodInsnNode  //isMethodCall()
		        && asmNode.getOpcode() != Opcodes.INVOKESPECIAL;
	}

	public List<Mutation> apply(MethodNode mn, String classname, String methodname, AbstractInsnNode insn,
			Frame<BasicValue> frame, int lineNo) {
		List<Mutation> mutations = new LinkedList<Mutation>();

		MethodInsnNode node = (MethodInsnNode) insn;
		Type returnType = Type.getReturnType(node.desc);

		// insert mutation into bytecode with conditional
		InsnList mutation = new InsnList();
		//System.out.println("Mutation deletestatement for statement " + node.name + node.desc);
		for (Type argType : Type.getArgumentTypes(node.desc)) {
			if (argType.getSize() == 0){
				//System.out.println("Ignoring parameter of type " + argType);
			}
			else if (argType.getSize() == 2) {
				mutation.insert(new InsnNode(Opcodes.POP2));
				//System.out.println("Deleting parameter of 2 type " + argType);
			} else {
				//System.out.println("Deleting parameter of 1 type " + argType);
				mutation.insert(new InsnNode(Opcodes.POP));
			}
		}
		if (node.getOpcode() == Opcodes.INVOKEVIRTUAL) {
			//System.out.println("Deleting callee of type " + node.owner);
			mutation.add(new InsnNode(Opcodes.POP));
		} else if (node.getOpcode() == Opcodes.INVOKEINTERFACE) {
			boolean isStatic = false;
			try {
				Class<?> clazz = Class.forName(node.owner.replace('/', '.'), false, DeleteStatement.class.getClassLoader());
				for (java.lang.reflect.Method method : clazz.getMethods()) {
					if (method.getName().equals(node.name)) {
						if (Type.getMethodDescriptor(method).equals(node.desc)) {
							if (Modifier.isStatic(method.getModifiers()))
								isStatic = true;
						}
					}
				}
			} catch (ClassNotFoundException e) {
				//System.out.println("Could not find class: " + node.owner+ ", this is likely a severe problem");
			}
			if (!isStatic) {
				//System.out.println("Deleting callee of type " + node.owner);
				mutation.add(new InsnNode(Opcodes.POP));
			}
		}
		mutation.add(getDefault(returnType));

		// insert mutation into pool
		Mutation mutationObject = MutationPool.addMutation(classname,
		                                                   methodname,
		                                                   NAME + " "
		                                                           + node.name
		                                                           + node.desc,
		                                                   insn,
		                                                   mutation,
		                                                   Mutation.getDefaultInfectionDistance(),
		                                                   lineNo);

		mutations.add(mutationObject);
		return mutations;
	}
	

	private static AbstractInsnNode getDefault(Type type) {
		if (type.equals(Type.BOOLEAN_TYPE)) {
			return new LdcInsnNode(0);
		} else if (type.equals(Type.INT_TYPE)) {
			return new LdcInsnNode(0);
		} else if (type.equals(Type.BYTE_TYPE)) {
			return new LdcInsnNode(0);
		} else if (type.equals(Type.CHAR_TYPE)) {
			return new LdcInsnNode(0);
		} else if (type.equals(Type.DOUBLE_TYPE)) {
			return new LdcInsnNode(0.0);
		} else if (type.equals(Type.FLOAT_TYPE)) {
			return new LdcInsnNode(0.0F);
		} else if (type.equals(Type.INT_TYPE)) {
			return new LdcInsnNode(0);
		} else if (type.equals(Type.LONG_TYPE)) {
			return new LdcInsnNode(0L);
		} else if (type.equals(Type.SHORT_TYPE)) {
			return new LdcInsnNode(0);
		} else if (type.equals(Type.VOID_TYPE)) {
			return new LabelNode();
		} else {
			return new InsnNode(Opcodes.ACONST_NULL);
		}
	}

}
