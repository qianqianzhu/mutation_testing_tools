package org.qianqianzhu.instrument.mutation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

public class MutationMethodAdapter extends MethodVisitor {

	MethodVisitor next;
	String className;
	String methodName;
	int access;
	ClassLoader classloader;
	
	public MutationMethodAdapter(ClassLoader classLoader, String className, int access,
	        String methodName, String desc, String signature, String[] exceptions,
	        MethodVisitor mv){
		super(Opcodes.ASM5, new MethodNode(access, methodName, desc, signature, exceptions));
		next = mv;
		this.className = className; 
		this.methodName = methodName + desc;
		this.access=access;
		
	}
	@Override
	public void visitEnd(){
		MethodNode mn = (MethodNode) mv;
		// transformation code
		//System.out.println("Transforming code..."+methodName);
		MutationInstrumentation mutInst = new MutationInstrumentation();
		
		mutInst.analyse( mn,className, methodName, access);
		mn.accept(next);
	}
	
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		int maxNum = 7;
		super.visitMaxs(Math.max(maxNum, maxStack), maxLocals);
	}
}
