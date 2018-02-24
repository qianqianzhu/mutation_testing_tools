package org.qianqianzhu.instrument.simple;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


public class SimpleMutationClassAdapter extends ClassVisitor {

	private final String className;
	private final ClassLoader classLoader;

	public SimpleMutationClassAdapter(ClassLoader classLoader, ClassVisitor visitor, String className) {
		super(Opcodes.ASM5, visitor);
		this.className = className;
		this.classLoader = classLoader;
		System.out.println("class:"+className);
	}

	@Override
	public MethodVisitor visitMethod(int access, String methodName, String desc,
			String signature, String[] exceptions){
		MethodVisitor mv = super.visitMethod(access, methodName, desc, signature, exceptions);
		System.out.println("class:"+className+" method:"+methodName);
		return new SimpleMutationMethodAdapter(classLoader, className, access, methodName, desc, signature, exceptions, mv);
	}

}
