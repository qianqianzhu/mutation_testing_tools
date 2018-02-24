package org.qianqianzhu.instrument.mutation.operator;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.qianqianzhu.instrument.mutation.Mutation;
import org.qianqianzhu.instrument.mutation.MutationPool;
import org.qianqianzhu.instrument.mutation.operator.MutationOperator;

public class ReplaceConstant implements MutationOperator {
	
	public static final String NAME = "ReplaceConstant";

	public boolean isApplicable(AbstractInsnNode node) {
		//isConstant()
		switch (node.getOpcode()) {
		case Opcodes.LDC:
		case Opcodes.ICONST_0:
		case Opcodes.ICONST_1:
		case Opcodes.ICONST_2:
		case Opcodes.ICONST_3:
		case Opcodes.ICONST_4:
		case Opcodes.ICONST_5:
		case Opcodes.ICONST_M1:
		case Opcodes.LCONST_0:
		case Opcodes.LCONST_1:
		case Opcodes.DCONST_0:
		case Opcodes.DCONST_1:
		case Opcodes.FCONST_0:
		case Opcodes.FCONST_1:
		case Opcodes.FCONST_2:
		case Opcodes.BIPUSH:
		case Opcodes.SIPUSH:
			return true;
		default:
			return false;
		}
	}

	public List<Mutation> apply(MethodNode mn, String classname, String methodname, AbstractInsnNode insn,
			Frame<BasicValue> frame, int lineNo) {
		List<Mutation> mutations = new LinkedList<Mutation>();
		Object value = getValue(insn);

		for (Object replacement : getReplacement(value)) {
			// insert mutation into bytecode with conditional
			LdcInsnNode mutation = new LdcInsnNode(replacement);
			// insert mutation into pool
			String summary = NAME + " - " + value + " -> " + replacement;
			if (replacement instanceof String) {
				summary = summary.replace("*/", "*_/");
			}
			Mutation mutationObject = MutationPool.addMutation(classname,
			                                                   methodname,
			                                                   summary,
			                                                   insn,
			                                                   mutation,
			                                                   Mutation.getDefaultInfectionDistance(),
			                                                   lineNo);
			mutations.add(mutationObject);
		}

		return mutations;
	}

	private Object getValue(AbstractInsnNode constant) {
		switch (constant.getOpcode()) {
		case Opcodes.LDC:
			return ((LdcInsnNode) constant).cst;
		case Opcodes.ICONST_0:
			return 0;
		case Opcodes.ICONST_1:
			return 1;
		case Opcodes.ICONST_2:
			return 2;
		case Opcodes.ICONST_3:
			return 3;
		case Opcodes.ICONST_4:
			return 4;
		case Opcodes.ICONST_5:
			return 5;
		case Opcodes.ICONST_M1:
			return -1;
		case Opcodes.LCONST_0:
			return 0L;
		case Opcodes.LCONST_1:
			return 1L;
		case Opcodes.DCONST_0:
			return 0.0;
		case Opcodes.DCONST_1:
			return 1.0;
		case Opcodes.FCONST_0:
			return 0.0F;
		case Opcodes.FCONST_1:
			return 1.0F;
		case Opcodes.FCONST_2:
			return 2.0F;
		case Opcodes.SIPUSH:
			return ((IntInsnNode) constant).operand;
		case Opcodes.BIPUSH:
			return ((IntInsnNode) constant).operand;
		default:
			throw new RuntimeException("Unknown constant: " + constant.getOpcode());
		}
	}

	// Integer, a Float, a Long, a Double, a String or a Type.
	private Object[] getReplacement(Object value) {
		if (value instanceof Integer)
			return getReplacement(((Integer) value).intValue());
		else if (value instanceof Float)
			return getReplacement(((Float) value).floatValue());
		else if (value instanceof Double)
			return getReplacement(((Double) value).doubleValue());
		else if (value instanceof Long)
			return getReplacement(((Long) value).longValue());
		else if (value instanceof String)
			return getReplacement((String) value);
		else if (value instanceof Type)
			return new Object[0];
		else
			throw new RuntimeException("Unknown type of constant: " + value);
	}

	private Object[] getReplacement(int value) {
		List<Object> values = new LinkedList<Object>();
		// TODO: Should be replaced with a proper check for booleans
		if (value == 0)
			values.add(1);
		else if (value == 1)
			values.add(0);
		else {
			if (value != 0)
				values.add(0);
			if (value != 1)
				values.add(1);
			if (value != -1)
				values.add(-1);
			if (!values.contains(value - 1))
				values.add(value - 1);
			if (!values.contains(value + 1))
				values.add(value + 1);
		}

		return values.toArray();
	}

	private Object[] getReplacement(long value) {
		List<Object> values = new LinkedList<Object>();
		if (value != 0L)
			values.add(0L);
		if (value != 1L)
			values.add(1L);
		if (value != -1L)
			values.add(-1L);
		if (!values.contains(value - 1L))
			values.add(value - 1L);
		if (!values.contains(value + 1L))
			values.add(value + 1L);

		return values.toArray();
	}

	private Object[] getReplacement(float value) {
		List<Object> values = new LinkedList<Object>();
		if (value != 0.0F)
			values.add(0.0F);
		if (value != 1.0F)
			values.add(1.0F);
		if (value != -1.0F)
			values.add(-1.0F);
		if (!values.contains(value - 1.0F))
			values.add(value - 1.0F);
		if (!values.contains(value + 1.0F))
			values.add(value + 1.0F);

		return values.toArray();
	}

	private Object[] getReplacement(double value) {
		List<Object> values = new LinkedList<Object>();
		if (value != 0.0)
			values.add(0.0);
		if (value != 1.0)
			values.add(1.0);
		if (value != -1.0)
			values.add(-1.0);
		if (!values.contains(value - 1.0))
			values.add(value - 1.0);
		if (!values.contains(value + 1.0))
			values.add(value + 1.0);

		return values.toArray();
	}

	private Object[] getReplacement(String value) {
		List<Object> values = new LinkedList<Object>();
		if (!value.equals(""))
			values.add("");

		return values.toArray();
	}

}
