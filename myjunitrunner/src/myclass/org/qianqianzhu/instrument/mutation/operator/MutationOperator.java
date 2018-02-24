package org.qianqianzhu.instrument.mutation.operator;

import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.qianqianzhu.instrument.mutation.Mutation;

public interface MutationOperator {
	
	public boolean isApplicable(AbstractInsnNode abstractInsnNode);

	public List<Mutation> apply(MethodNode mn, String classname, String methodname,
			AbstractInsnNode insn, Frame<BasicValue> frame, int lineNo);

}
