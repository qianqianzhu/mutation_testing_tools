package org.qianqianzhu.instrument.simple;

import java.util.Iterator;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.qianqianzhu.instrument.mutation.MutationObserver;
import org.qianqianzhu.instrument.mutation.operator.ReplaceArithmeticOperator;
import org.qianqianzhu.instrument.util.PackageInfo;


public class SimpleMutationMethodAdapter extends MethodVisitor {

	MethodVisitor next;
	String className;
	String methodName;
	int access;
	ClassLoader classloader;

	public SimpleMutationMethodAdapter(ClassLoader classLoader, String className, int access,
			String methodName, String desc, String signature, String[] exceptions,
			MethodVisitor mv){
		super(Opcodes.ASM5, new MethodNode(access, methodName, desc, signature, exceptions));
		next = mv;
		this.className = className; 
		this.methodName = methodName;
		this.access=access;
	}

	@Override
	public void visitEnd(){
		MethodNode mn = (MethodNode) mv;
		// transformation code
		System.out.println("Transforming code..."+className + methodName);

		InsnList insns = mn.instructions;
		Iterator<AbstractInsnNode> i = insns.iterator();
		while(i.hasNext()){
			 AbstractInsnNode in = i.next();
			 System.out.println("insnNode:"+in.getOpcode());
			 if(in.getOpcode()==Opcodes.IADD){
				 //System.out.println("!!! IADD !!!");
				 
				 // add infection distance instruments
				 InsnList il = new InsnList();
//				 il.add(new LdcInsnNode("!!!!!!IADD insn!!!!!!!!"));
//				 il.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out",
//			                "Ljava/io/PrintStream;"));
				 il.add(new InsnNode(Opcodes.DUP2));
				 il.add(new LdcInsnNode(Opcodes.IADD));
				 il.add(new LdcInsnNode(Opcodes.ISUB));
				 il.add(new MethodInsnNode(
						 Opcodes.INVOKESTATIC,
							PackageInfo.getNameWithSlash(ReplaceArithmeticOperator.class),
					        "printInfectionDistanceInt", "(IIII)V", false));	
				 insns.insertBefore(in,il);
			 }
			 
			 
		}
		mn.accept(next);
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		int maxNum = 7;
		super.visitMaxs(Math.max(maxNum, maxStack), maxLocals);
	}
	
	//	@Override 
	//	public void visitCode(){
	//		mv.visitCode();
	////		mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
	////                "Ljava/io/PrintStream;");
	////        mv.visitLdcInsn("Hello Test");
	////        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
	////                "println", "(Ljava/lang/String;)V",false);
	//	}
	//	
	//	@Override
	//	public void visitInsn(int op) {
	//		switch (op) {
	//		case Opcodes.IADD:
	//			// (int)i1 + (int)i2 -> (int)i1 - (int)i2
	//			System.out.println("(int)i1 + (int)i2 -> (int)i1 - (int)i2");
	//			mv.visitInsn(Opcodes.ISUB);
	//			break;
	//		case Opcodes.LADD:
	//			// (long)i1 + (long)i2 -> (long)i1 - (long)i2
	//			System.out.println("(long)i1 + (long)i2 -> (long)i1 - (long)i2");
	//			mv.visitInsn(Opcodes.LSUB);
	//			break;
	//		case Opcodes.DADD:
	//			// (double)d1 + (double)d2 -> (double)d1 - (double)d2
	//			System.out.println("(double)d1 + (double)d2 -> (double)d1 - (double)d2");
	//			mv.visitInsn(Opcodes.DSUB);
	//			break;
	//		case Opcodes.FADD:
	//			// (float)g1 + (float)f2 -> (float)f1 - (float)f2
	//			System.out.println("(float)g1 + (float)f2 -> (float)f1 - (float)f2");
	//			super.visitInsn(Opcodes.FSUB);
	//			break;
	//		default:
	//			System.out.println("No mutating");
	//			mv.visitInsn(op);
	//		}
	//	}
}