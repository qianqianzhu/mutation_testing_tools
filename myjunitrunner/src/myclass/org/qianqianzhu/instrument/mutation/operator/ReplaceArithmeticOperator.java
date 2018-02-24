package org.qianqianzhu.instrument.mutation.operator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.qianqianzhu.instrument.mutation.Mutation;
import org.qianqianzhu.instrument.mutation.MutationPool;
import org.qianqianzhu.instrument.util.PackageInfo;

public class ReplaceArithmeticOperator implements MutationOperator{
	
	public static final String NAME = "ReplaceArithmeticOperator";
	
	private static Set<Integer> opcodesInt = new HashSet<Integer>();
	private static Set<Integer> opcodesLong = new HashSet<Integer>();
	private static Set<Integer> opcodesFloat = new HashSet<Integer>();
	private static Set<Integer> opcodesDouble = new HashSet<Integer>();

	private int numVariable = 0;

	static {
		opcodesInt.addAll(Arrays.asList(new Integer[] { Opcodes.IADD, Opcodes.ISUB,
		        Opcodes.IMUL, Opcodes.IDIV, Opcodes.IREM }));
		opcodesLong.addAll(Arrays.asList(new Integer[] { Opcodes.LADD, Opcodes.LSUB,
		        Opcodes.LMUL, Opcodes.LDIV, Opcodes.LREM }));
		opcodesFloat.addAll(Arrays.asList(new Integer[] { Opcodes.FADD, Opcodes.FSUB,
		        Opcodes.FMUL, Opcodes.FDIV, Opcodes.FREM }));
		opcodesDouble.addAll(Arrays.asList(new Integer[] { Opcodes.DADD, Opcodes.DSUB,
		        Opcodes.DMUL, Opcodes.DDIV, Opcodes.DREM }));
	}

	private String getOp(int opcode) {
		switch (opcode) {
		case Opcodes.IADD:
		case Opcodes.LADD:
		case Opcodes.FADD:
		case Opcodes.DADD:
			return "+";
		case Opcodes.ISUB:
		case Opcodes.LSUB:
		case Opcodes.FSUB:
		case Opcodes.DSUB:
			return "-";
		case Opcodes.IMUL:
		case Opcodes.LMUL:
		case Opcodes.FMUL:
		case Opcodes.DMUL:
			return "*";
		case Opcodes.IDIV:
		case Opcodes.LDIV:
		case Opcodes.FDIV:
		case Opcodes.DDIV:
			return "/";
		case Opcodes.IREM:
		case Opcodes.LREM:
		case Opcodes.FREM:
		case Opcodes.DREM:
			return "%";
		}
		throw new RuntimeException("Unknown opcode: " + opcode);
	}

	/**
	 * <p>getNextIndex</p>
	 *
	 * @param mn a {@link org.objectweb.asm.tree.MethodNode} object.
	 * @return a int.
	 */
	@SuppressWarnings("rawtypes")
	public static int getNextIndex(MethodNode mn) {
		Iterator it = mn.localVariables.iterator();
		int max = 0;
		int next = 0;
		while (it.hasNext()) {
			LocalVariableNode var = (LocalVariableNode) it.next();
			int index = var.index;
			if (index >= max) {
				max = index;
				next = max + Type.getType(var.desc).getSize();
			}
		}
		if (next == 0)
			next = getNextIndexFromLoad(mn);
		return next;
	}

	@SuppressWarnings("rawtypes")
	private static int getNextIndexFromLoad(MethodNode mn) {
		Iterator it = mn.instructions.iterator();
		int index = 0;
		while (it.hasNext()) {
			AbstractInsnNode node = (AbstractInsnNode) it.next();
			if (node instanceof VarInsnNode) {
				VarInsnNode varNode = (VarInsnNode) node;
				int varIndex = varNode.var;
				switch (varNode.getOpcode()) {
				case Opcodes.ALOAD:
				case Opcodes.ILOAD:
				case Opcodes.FLOAD:
				case Opcodes.IALOAD:
				case Opcodes.BALOAD:
				case Opcodes.CALOAD:
				case Opcodes.AALOAD:
				case Opcodes.ASTORE:
				case Opcodes.ISTORE:
				case Opcodes.FSTORE:
				case Opcodes.IASTORE:
				case Opcodes.BASTORE:
				case Opcodes.CASTORE:
				case Opcodes.AASTORE:
					index = Math.max(index, varIndex + 1);
					break;
				case Opcodes.DLOAD:
				case Opcodes.DSTORE:
				case Opcodes.LLOAD:
				case Opcodes.LSTORE:
				case Opcodes.DALOAD:
				case Opcodes.DASTORE:
				case Opcodes.LALOAD:
				case Opcodes.LASTORE:
					index = Math.max(index, varIndex + 2);
					break;
				}
			}
		}

		return index;
	}

	public boolean isApplicable(AbstractInsnNode insn) {
		int opcode = insn.getOpcode();
		if (opcodesInt.contains(opcode))
			return true;
		else if (opcodesLong.contains(opcode))
			return true;
		else if (opcodesFloat.contains(opcode))
			return true;
		else if (opcodesDouble.contains(opcode))
			return true;

		return false;
	}

	public List<Mutation> apply(MethodNode mn, String classname, String methodname, AbstractInsnNode insn,
			Frame<BasicValue> frame, int lineNo) {
		
		numVariable = getNextIndex(mn);  // qianqian's question: what does this function do?
		List<Mutation> mutations = new LinkedList<Mutation>();

		for (int opcode : getMutations(insn.getOpcode())) {
			InsnNode mutation = new InsnNode(opcode);
			// insert mutation into pool
			Mutation mutationObject = MutationPool.addMutation(classname,
			                                                   methodname,
			                                                   NAME + " "
			                                                           + getOp(insn.getOpcode())
			                                                           + " -> "
			                                                           + getOp(opcode),
			                                                   insn,
			                                                   mutation,
			                                                   getInfectionDistance(insn.getOpcode(),
			                                                                        opcode),
			                                                   lineNo);
			mutations.add(mutationObject);
		}

		return mutations;
	}

	private Set<Integer> getMutations(int opcode) {
		Set<Integer> replacement = new HashSet<Integer>();
		if (opcodesInt.contains(opcode))
			replacement.addAll(opcodesInt);
		else if (opcodesLong.contains(opcode))
			replacement.addAll(opcodesLong);
		else if (opcodesFloat.contains(opcode))
			replacement.addAll(opcodesFloat);
		else if (opcodesDouble.contains(opcode))
			replacement.addAll(opcodesDouble);

		replacement.remove(opcode);
		return replacement;
	}

	private InsnList getInfectionDistance(int opcodeOrig, int opcodeNew) {
		InsnList distance = new InsnList();

		if (opcodesInt.contains(opcodeOrig)) {
			distance.add(new InsnNode(Opcodes.DUP2));
			distance.add(new LdcInsnNode(opcodeOrig));
			distance.add(new LdcInsnNode(opcodeNew));
			distance.add(new MethodInsnNode(
			        Opcodes.INVOKESTATIC,
					PackageInfo.getNameWithSlash(ReplaceArithmeticOperator.class),
			        "getInfectionDistanceInt", "(IIII)D", false));
		} else if (opcodesLong.contains(opcodeOrig)) {
			distance.add(new VarInsnNode(Opcodes.LSTORE, numVariable));
			distance.add(new InsnNode(Opcodes.DUP2));
			distance.add(new VarInsnNode(Opcodes.LLOAD, numVariable));
			distance.add(new InsnNode(Opcodes.DUP2_X2));
			distance.add(new LdcInsnNode(opcodeOrig));
			distance.add(new LdcInsnNode(opcodeNew));
			distance.add(new MethodInsnNode(
			        Opcodes.INVOKESTATIC,
					PackageInfo.getNameWithSlash(ReplaceArithmeticOperator.class),
			        "getInfectionDistanceLong", "(JJII)D", false));
			numVariable += 2;
		} else if (opcodesFloat.contains(opcodeOrig)) {
			distance.add(new InsnNode(Opcodes.DUP2));
			distance.add(new LdcInsnNode(opcodeOrig));
			distance.add(new LdcInsnNode(opcodeNew));
			distance.add(new MethodInsnNode(
			        Opcodes.INVOKESTATIC,
					PackageInfo.getNameWithSlash(ReplaceArithmeticOperator.class),
			        "getInfectionDistanceFloat", "(FFII)D", false));
		} else if (opcodesDouble.contains(opcodeOrig)) {
			distance.add(new VarInsnNode(Opcodes.DSTORE, numVariable));
			distance.add(new InsnNode(Opcodes.DUP2));
			distance.add(new VarInsnNode(Opcodes.DLOAD, numVariable));
			distance.add(new InsnNode(Opcodes.DUP2_X2));
			distance.add(new LdcInsnNode(opcodeOrig));
			distance.add(new LdcInsnNode(opcodeNew));
			distance.add(new MethodInsnNode(
			        Opcodes.INVOKESTATIC,
					PackageInfo.getNameWithSlash(ReplaceArithmeticOperator.class),
			        "getInfectionDistanceDouble", "(DDII)D", false));
			numVariable += 2;
		}

		return distance;
	}

	private static boolean hasDivZeroError(int opcode) {
		switch (opcode) {
		case Opcodes.IDIV:
		case Opcodes.IREM:
		case Opcodes.FDIV:
		case Opcodes.FREM:
		case Opcodes.LDIV:
		case Opcodes.LREM:
		case Opcodes.DDIV:
		case Opcodes.DREM:
			return true;
		default:
			return false;
		}
	}

	/**
	 * <p>getInfectionDistanceInt</p>
	 *
	 * @param x a int.
	 * @param y a int.
	 * @param opcodeOrig a int.
	 * @param opcodeNew a int.
	 * @return a double.
	 */
	public static double getInfectionDistanceInt(int x, int y, int opcodeOrig,
	        int opcodeNew) {
		if (y == 0) {
			return hasDivZeroError(opcodeOrig) == hasDivZeroError(opcodeNew) ? 1.0 : 0.0;
		}
		int origValue = calculate(x, y, opcodeOrig);
		int newValue = calculate(x, y, opcodeNew);
		return origValue == newValue ? 1.0 : 0.0;
	}
	
	//for test
	public static void printInfectionDistanceInt(int x, int y, int opcodeOrig,
	        int opcodeNew) {
		double distance;
		if (y == 0) {
			distance = hasDivZeroError(opcodeOrig) == hasDivZeroError(opcodeNew) ? 1.0 : 0.0;
		}
		int origValue = calculate(x, y, opcodeOrig);
		int newValue = calculate(x, y, opcodeNew);
		distance = origValue == newValue ? 1.0 : 0.0;
		System.out.println(distance);
	}

	/**
	 * <p>getInfectionDistanceLong</p>
	 *
	 * @param x a long.
	 * @param y a long.
	 * @param opcodeOrig a int.
	 * @param opcodeNew a int.
	 * @return a double.
	 */
	public static double getInfectionDistanceLong(long x, long y, int opcodeOrig,
	        int opcodeNew) {
		if (y == 0L) {
			return hasDivZeroError(opcodeOrig) == hasDivZeroError(opcodeNew) ? 1.0 : 0.0;
		}
		long origValue = calculate(x, y, opcodeOrig);
		long newValue = calculate(x, y, opcodeNew);
		return origValue == newValue ? 1.0 : 0.0;
	}

	/**
	 * <p>getInfectionDistanceFloat</p>
	 *
	 * @param x a float.
	 * @param y a float.
	 * @param opcodeOrig a int.
	 * @param opcodeNew a int.
	 * @return a double.
	 */
	public static double getInfectionDistanceFloat(float x, float y, int opcodeOrig,
	        int opcodeNew) {
		if (y == 0.0F) {
			return hasDivZeroError(opcodeOrig) == hasDivZeroError(opcodeNew) ? 1.0 : 0.0;
		}
		float origValue = calculate(x, y, opcodeOrig);
		float newValue = calculate(x, y, opcodeNew);
		return origValue == newValue ? 1.0 : 0.0;
	}

	/**
	 * <p>getInfectionDistanceDouble</p>
	 *
	 * @param x a double.
	 * @param y a double.
	 * @param opcodeOrig a int.
	 * @param opcodeNew a int.
	 * @return a double.
	 */
	public static double getInfectionDistanceDouble(double x, double y, int opcodeOrig,
	        int opcodeNew) {
		if (y == 0.0) {
			return hasDivZeroError(opcodeOrig) == hasDivZeroError(opcodeNew) ? 1.0 : 0.0;
		}
		double origValue = calculate(x, y, opcodeOrig);
		double newValue = calculate(x, y, opcodeNew);
		return origValue == newValue ? 1.0 : 0.0;
	}

	/**
	 * <p>calculate</p>
	 *
	 * @param x a int.
	 * @param y a int.
	 * @param opcode a int.
	 * @return a int.
	 */
	public static int calculate(int x, int y, int opcode) {
		switch (opcode) {
		case Opcodes.IADD:
			return x + y;
		case Opcodes.ISUB:
			return x - y;
		case Opcodes.IMUL:
			return x * y;
		case Opcodes.IDIV:
			return x / y;
		case Opcodes.IREM:
			return x % y;
		}
		throw new RuntimeException("Unknown integer opcode: " + opcode);
	}

	/**
	 * <p>calculate</p>
	 *
	 * @param x a long.
	 * @param y a long.
	 * @param opcode a int.
	 * @return a long.
	 */
	public static long calculate(long x, long y, int opcode) {
		switch (opcode) {
		case Opcodes.LADD:
			return x + y;
		case Opcodes.LSUB:
			return x - y;
		case Opcodes.LMUL:
			return x * y;
		case Opcodes.LDIV:
			return x / y;
		case Opcodes.LREM:
			return x % y;
		}
		throw new RuntimeException("Unknown integer opcode: " + opcode);
	}

	/**
	 * <p>calculate</p>
	 *
	 * @param x a float.
	 * @param y a float.
	 * @param opcode a int.
	 * @return a float.
	 */
	public static float calculate(float x, float y, int opcode) {
		switch (opcode) {
		case Opcodes.FADD:
			return x + y;
		case Opcodes.FSUB:
			return x - y;
		case Opcodes.FMUL:
			return x * y;
		case Opcodes.FDIV:
			return x / y;
		case Opcodes.FREM:
			return x % y;
		}
		throw new RuntimeException("Unknown integer opcode: " + opcode);
	}

	/**
	 * <p>calculate</p>
	 *
	 * @param x a double.
	 * @param y a double.
	 * @param opcode a int.
	 * @return a double.
	 */
	public static double calculate(double x, double y, int opcode) {
		switch (opcode) {
		case Opcodes.DADD:
			return x + y;
		case Opcodes.DSUB:
			return x - y;
		case Opcodes.DMUL:
			return x * y;
		case Opcodes.DDIV:
			return x / y;
		case Opcodes.DREM:
			return x % y;
		}
		throw new RuntimeException("Unknown integer opcode: " + opcode);
	}


}
