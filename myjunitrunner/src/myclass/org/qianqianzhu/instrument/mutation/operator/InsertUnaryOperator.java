package org.qianqianzhu.instrument.mutation.operator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.qianqianzhu.instrument.mutation.Mutation;
import org.qianqianzhu.instrument.mutation.MutationPool;
import org.qianqianzhu.instrument.mutation.operator.MutationOperator;
import org.qianqianzhu.instrument.util.VariableNotFoundException;

public class InsertUnaryOperator implements MutationOperator {

	public static final String NAME = "InsertUnaryOp";
	
	public boolean isApplicable(AbstractInsnNode node) {
		switch (node.getOpcode()) {
		case Opcodes.ILOAD:
		case Opcodes.LLOAD:
		case Opcodes.FLOAD:
		case Opcodes.DLOAD:
			return true;
		case Opcodes.GETFIELD:
		case Opcodes.GETSTATIC:
			FieldInsnNode fieldNode = (FieldInsnNode) node;
			Type type = Type.getType(fieldNode.desc);
			if (type == Type.BYTE_TYPE || type == Type.SHORT_TYPE
			        || type == Type.LONG_TYPE || type == Type.FLOAT_TYPE
			        || type == Type.DOUBLE_TYPE || type == Type.BOOLEAN_TYPE
			        || type == Type.INT_TYPE) {
				return true;
			}
		default:
			return false;
		}
	}

	public List<Mutation> apply(MethodNode mn, String classname, String methodname, AbstractInsnNode insn,
			Frame<BasicValue> frame, int lineNo) {
		// Mutation: Insert an INEG _after_ an iload 
		List<Mutation> mutations = new LinkedList<Mutation>();
		List<InsnList> mutationCode = new LinkedList<InsnList>();
		List<String> descriptions = new LinkedList<String>();

		if (insn instanceof VarInsnNode) {
			try {
				InsnList mutation = new InsnList();
				VarInsnNode node = (VarInsnNode) insn;

				// insert mutation into bytecode with conditional
				mutation.add(new VarInsnNode(node.getOpcode(), node.var));
				mutation.add(new InsnNode(getNegation(node.getOpcode())));
				mutationCode.add(mutation);

				if (!mn.localVariables.isEmpty())
					descriptions.add("Negation of " + getName(mn, node));
				else
					descriptions.add("Negation");

				if (node.getOpcode() == Opcodes.ILOAD) {
					if (frame.getStack(frame.getStackSize() - 1) != new BasicValue(Type.BOOLEAN_TYPE)) {
						mutation = new InsnList();
						mutation.add(new IincInsnNode(node.var, 1));
						mutation.add(new VarInsnNode(node.getOpcode(), node.var));
						if (!mn.localVariables.isEmpty())
							descriptions.add("IINC 1 " + getName(mn, node));
						else
							descriptions.add("IINC 1");
						mutationCode.add(mutation);

						mutation = new InsnList();
						mutation.add(new IincInsnNode(node.var, -1));
						mutation.add(new VarInsnNode(node.getOpcode(), node.var));
						if (!mn.localVariables.isEmpty())
							descriptions.add("IINC -1 " + getName(mn, node));
						else
							descriptions.add("IINC -1");
						mutationCode.add(mutation);
					}
				}
			} catch (VariableNotFoundException e) {
				//System.out.println("Could not find variable: " + e);
				return new ArrayList<Mutation>();
			}
		} else {
			InsnList mutation = new InsnList();
			FieldInsnNode node = (FieldInsnNode) insn;
			Type type = Type.getType(node.desc);
			mutation.add(new FieldInsnNode(node.getOpcode(), node.owner, node.name,
			        node.desc));
			mutation.add(new InsnNode(getNegation(type)));
			descriptions.add("Negation");
			mutationCode.add(mutation);

			if (type == Type.INT_TYPE) {
				mutation = new InsnList();
				mutation.add(new FieldInsnNode(node.getOpcode(), node.owner, node.name,
				        node.desc));
				mutation.add(new InsnNode(Opcodes.ICONST_1));
				mutation.add(new InsnNode(Opcodes.IADD));
				descriptions.add("+1");
				mutationCode.add(mutation);

				mutation = new InsnList();
				mutation.add(new FieldInsnNode(node.getOpcode(), node.owner, node.name,
				        node.desc));
				mutation.add(new InsnNode(Opcodes.ICONST_M1));
				mutation.add(new InsnNode(Opcodes.IADD));
				descriptions.add("-1");
				mutationCode.add(mutation);
			}
		}

		int i = 0;
		for (InsnList mutation : mutationCode) {
			// insert mutation into pool
			Mutation mutationObject = MutationPool.addMutation(classname,
			                                                   methodname,
			                                                   NAME + " "
			                                                           + descriptions.get(i++),
			                                                   insn,
			                                                   mutation,
			                                                   Mutation.getDefaultInfectionDistance(),
			                                                   lineNo);

			mutations.add(mutationObject);
		}
		return mutations;
	}
	
	private int getNegation(Type type) {
		if (type.equals(Type.BYTE_TYPE)) {
			return Opcodes.INEG;
		} else if (type == Type.SHORT_TYPE) {
			return Opcodes.INEG;
		} else if (type == Type.LONG_TYPE) {
			return Opcodes.LNEG;
		} else if (type == Type.FLOAT_TYPE) {
			return Opcodes.FNEG;
		} else if (type == Type.DOUBLE_TYPE) {
			return Opcodes.DNEG;
		} else if (type == Type.BOOLEAN_TYPE) {
			return Opcodes.INEG;
		} else if (type == Type.INT_TYPE) {
			return Opcodes.INEG;
		} else {
			throw new RuntimeException("Don't know how to negate type " + type);
		}
	}
	

	private int getNegation(int opcode) {
		switch (opcode) {
		case Opcodes.ILOAD:
			return Opcodes.INEG;
		case Opcodes.LLOAD:
			return Opcodes.LNEG;
		case Opcodes.FLOAD:
			return Opcodes.FNEG;
		case Opcodes.DLOAD:
			return Opcodes.DNEG;
		default:
			throw new RuntimeException("Invalid opcode for negation: " + opcode);
		}
	}
	
	private String getName(MethodNode mn, AbstractInsnNode node)
	        throws VariableNotFoundException {
		if (node instanceof VarInsnNode) {
			LocalVariableNode var = getLocal(mn, node, ((VarInsnNode) node).var);
			return var.name;
		} else if (node instanceof FieldInsnNode) {
			return ((FieldInsnNode) node).name;
		} else if (node instanceof IincInsnNode) {
			IincInsnNode incNode = (IincInsnNode) node;
			LocalVariableNode var = getLocal(mn, node, incNode.var);
			return var.name;

		} else {
			throw new RuntimeException("Unknown variable node: " + node);
		}
	}

	
	private LocalVariableNode getLocal(MethodNode mn, AbstractInsnNode node, int index)
	        throws VariableNotFoundException {
		int currentId = mn.instructions.indexOf(node);
		for (Object v : mn.localVariables) {
			LocalVariableNode localVar = (LocalVariableNode) v;
			int startId = mn.instructions.indexOf(localVar.start);
			int endId = mn.instructions.indexOf(localVar.end);
			if (currentId >= startId && currentId <= endId && localVar.index == index)
				return localVar;
		}

		throw new VariableNotFoundException("Could not find local variable " + index
		        + " at position " + currentId + ", have variables: "
		        + mn.localVariables.size());
	}
}
