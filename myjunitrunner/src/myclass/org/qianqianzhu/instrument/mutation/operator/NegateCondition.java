package org.qianqianzhu.instrument.mutation.operator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.qianqianzhu.instrument.mutation.Mutation;
import org.qianqianzhu.instrument.mutation.MutationPool;
import org.qianqianzhu.instrument.mutation.operator.MutationOperator;

public class NegateCondition implements MutationOperator {
	
	private static Map<Integer, Integer> opcodeMap = new HashMap<Integer, Integer>();

	public static final String NAME = "NegateCondition";
	
	static {
		opcodeMap.put(Opcodes.IF_ACMPEQ, Opcodes.IF_ACMPNE);
		opcodeMap.put(Opcodes.IF_ACMPNE, Opcodes.IF_ACMPEQ);
		opcodeMap.put(Opcodes.IF_ICMPEQ, Opcodes.IF_ICMPNE);
		opcodeMap.put(Opcodes.IF_ICMPGE, Opcodes.IF_ICMPLT);
		opcodeMap.put(Opcodes.IF_ICMPGT, Opcodes.IF_ICMPLE);
		opcodeMap.put(Opcodes.IF_ICMPLE, Opcodes.IF_ICMPGT);
		opcodeMap.put(Opcodes.IF_ICMPLT, Opcodes.IF_ICMPGE);
		opcodeMap.put(Opcodes.IF_ICMPNE, Opcodes.IF_ICMPEQ);
		opcodeMap.put(Opcodes.IFEQ, Opcodes.IFNE);
		opcodeMap.put(Opcodes.IFGE, Opcodes.IFLT);
		opcodeMap.put(Opcodes.IFGT, Opcodes.IFLE);
		opcodeMap.put(Opcodes.IFLE, Opcodes.IFGT);
		opcodeMap.put(Opcodes.IFLT, Opcodes.IFGE);
		opcodeMap.put(Opcodes.IFNE, Opcodes.IFEQ);
		opcodeMap.put(Opcodes.IFNONNULL, Opcodes.IFNULL);
		opcodeMap.put(Opcodes.IFNULL, Opcodes.IFNONNULL);
	}

	public boolean isApplicable(AbstractInsnNode insn) {
		// isBranch(): (isJump() && !isGoto());
		return ((insn instanceof JumpInsnNode) && !(insn.getOpcode()==Opcodes.GOTO));		
	}

	public List<Mutation> apply(MethodNode mn, String classname, String methodname, AbstractInsnNode insn,
			Frame<BasicValue> frame, int lineNo) {
		List<Mutation> mutations = new LinkedList<Mutation>();

		JumpInsnNode node = (JumpInsnNode) insn;
		LabelNode target = node.label;

		// insert mutation into bytecode with conditional
		JumpInsnNode mutation = new JumpInsnNode(getOpposite(node.getOpcode()), target);
		// insert mutation into pool
		Mutation mutationObject = MutationPool.addMutation(classname,
		                                                   methodname,
		                                                   NAME,
		                                                   insn,
		                                                   mutation,
		                                                   Mutation.getDefaultInfectionDistance(),
		                                                   lineNo);

		mutations.add(mutationObject);
		return mutations;
	}
	
	private static int getOpposite(int opcode) {
		return opcodeMap.get(opcode);
	}

}
