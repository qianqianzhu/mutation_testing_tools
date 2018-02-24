package org.qianqianzhu.instrument.mutation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.qianqianzhu.instrument.mutation.operator.DeleteField;
import org.qianqianzhu.instrument.mutation.operator.DeleteStatement;
import org.qianqianzhu.instrument.mutation.operator.InsertUnaryOperator;
import org.qianqianzhu.instrument.mutation.operator.MutationOperator;
import org.qianqianzhu.instrument.mutation.operator.NegateCondition;
import org.qianqianzhu.instrument.mutation.operator.ReplaceArithmeticOperator;
import org.qianqianzhu.instrument.mutation.operator.ReplaceBitwiseOperator;
import org.qianqianzhu.instrument.mutation.operator.ReplaceComparisonOperator;
import org.qianqianzhu.instrument.mutation.operator.ReplaceConstant;
import org.qianqianzhu.instrument.mutation.operator.ReplaceVariable;
import org.qianqianzhu.instrument.util.ExecutionTracer;
import org.qianqianzhu.instrument.util.Properties;

public class MutationInstrumentation {
	private final List<MutationOperator> mutationOperators;

	public MutationInstrumentation(){
		mutationOperators = new ArrayList<MutationOperator>();
		
		mutationOperators.add(new ReplaceArithmeticOperator());
		//mutationOperators.add(new NegateCondition());
		mutationOperators.add(new InsertUnaryOperator());
		mutationOperators.add(new ReplaceComparisonOperator());
		mutationOperators.add(new ReplaceBitwiseOperator());
		mutationOperators.add(new ReplaceConstant());
		mutationOperators.add(new ReplaceVariable());
		//mutationOperators.add(new DeleteField());
		//mutationOperators.add(new DeleteStatement());

	}

	public void analyse(MethodNode mn, String classname, String methodname, int access) {
		//System.out.println("Trying to mutate:"+methodname);
		
		if (methodname.startsWith("<clinit>"))
			return;

		if (methodname.startsWith("__STATIC_RESE")) 
			return;
		
//		if(!Properties.methodNeedInstrumentation.contains(methodname))
//			return;
		
		Analyzer<BasicValue> a = new Analyzer<BasicValue>(new BasicInterpreter());
		try {
			a.analyze(classname, mn);
		} catch (AnalyzerException e) {
			e.printStackTrace();
		}
		// get computed frames
		Frame<BasicValue>[] frames = a.getFrames();

		// get all instructions
		AbstractInsnNode[] insns = mn.instructions.toArray();
		//System.out.println("frames.length:"+frames.length);
		//System.out.println("insns.length:"+insns.length);
		int lastLineNumber = -1;
		
		for(int i=0;i<frames.length;i++)
		{
			// register line number
			if (insns[i] instanceof LineNumberNode)
				lastLineNumber = ((LineNumberNode) insns[i]).line;
			
			// apply mutation operators
			List<Mutation> mutations = new LinkedList<Mutation>();
			//System.out.println("mutation operators:"+mutationOperators.size());

			for (MutationOperator mutationOperator : mutationOperators) {
				//System.out.println("insn:"+insns[i].getOpcode());
				
				if (mutationOperator.isApplicable(insns[i])) {
					//System.out.println("Applying mutation operator "+ mutationOperator.getClass().getSimpleName());
					mutations.addAll(mutationOperator.apply(mn, classname,
							methodname, insns[i],frames[i],lastLineNumber));
				}
			}
			
			//add mutation instrumentation
			if (!mutations.isEmpty()) {
				//System.out.println("Adding instrumentation for mutation:"+mutations.size());
				addInstrumentation(mn, insns[i], mutations);
			}
			//else
				//System.out.println("Mutation is empty");
		}

	}

	private void addInstrumentation(MethodNode mn, AbstractInsnNode original,
			List<Mutation> mutations) {

		InsnList instructions = new InsnList();

		// insert instrumentation for calculating infection distance
		for (Mutation mutation : mutations) {
			//System.out.println("Mutation "+mutation.getId()+":"+mutation.getInfectionDistance());
			instructions.add(mutation.getInfectionDistance());
			instructions.add(new LdcInsnNode(mutation.getId()));
			MethodInsnNode touched = new MethodInsnNode(Opcodes.INVOKESTATIC,
					Type.getInternalName(ExecutionTracer.class), "passedMutation",
					Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] {
							Type.DOUBLE_TYPE, Type.INT_TYPE }), false);
			instructions.add(touched);
		}

		// insert instrumentation for mutation schemata
		LabelNode endLabel = new LabelNode();
		for (Mutation mutation : mutations) {
			
			LabelNode nextLabel = new LabelNode();
			LdcInsnNode mutationId = new LdcInsnNode(mutation.getId());
			instructions.add(mutationId);
			FieldInsnNode activeId = new FieldInsnNode(Opcodes.GETSTATIC,
					Type.getInternalName(MutationObserver.class), "activeMutation", "I");
			instructions.add(activeId);
			instructions.add(new JumpInsnNode(Opcodes.IF_ICMPNE, nextLabel));
			instructions.add(mutation.getMutation());
			instructions.add(new JumpInsnNode(Opcodes.GOTO, endLabel));
			instructions.add(nextLabel);
		}

		mn.instructions.insertBefore(original, instructions);
		mn.instructions.insert(original, endLabel);

	}


}
