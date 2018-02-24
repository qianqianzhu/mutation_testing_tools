package org.qianqianzhu.instrument.mutation;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;

public class MutationClassAdapter extends ClassVisitor{

	private String className;
	private ClassLoader classLoader;
	/** Skip methods on enums - at least some */
	private boolean isEnum = false;
	/** Skip final class */
	private boolean isFinal = false;
	
	public MutationClassAdapter(ClassVisitor visitor,String className){
		super(Opcodes.ASM5, visitor);
		this.className = className;
		
	}
	public MutationClassAdapter(ClassLoader classLoader, ClassVisitor visitor, String className) {
		super(Opcodes.ASM5, visitor);
		this.className = className;
		this.classLoader = classLoader;
		//System.out.println("class:"+className);
	}
	
	/**
	 * Remove "final" accessor from class definition
	 */
	@Override
	public void visit(int version, int access, String name, String signature,
	        String superName, String[] interfaces) {
		
		if((access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL) {
			isFinal =true;
		}

		// We are removing final access to allow mocking
		super.visit(version, access, name, signature, superName, interfaces);
		if (superName.equals("java/lang/Enum"))
			isEnum = true;
	}
	
	@Override
	public MethodVisitor visitMethod(int methodAccess, String methodName, String descriptor,
	        String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(methodAccess, methodName, descriptor, signature,
                exceptions);
		
		// skipping instrumentation Opcodes.ACC_SYNTHETIC & Opcodes.ACC_BRIDG ? not understand
		mv = new JSRInlinerAdapter(mv, methodAccess, methodName, descriptor, signature, exceptions);

		if ((methodAccess & Opcodes.ACC_SYNTHETIC) != 0
		        || (methodAccess & Opcodes.ACC_BRIDGE) != 0) {
			//System.out.println("Skiping ACC_SYNTHETIC/ACC_BRIDGE");
			return mv;
		}
		
		// skipping enumerate 
		if (isEnum) {
			if(methodName.equals("valueOf") || methodName.equals("values")) {
				//System.out.println("Skipping enum valueOf");
				return mv;
			}
		    if (methodName.equals("<init>") && descriptor.equals("(Ljava/lang/String;I)V")) {
		    	//System.out.println("Skipping enum default constructor");
				return mv;
			}
		}
		
		// skipping final class
		if(isFinal){
	    	//System.out.println("Skipping final classes " + className);
			return mv;
		}
		
		// add mutation instrumentation adapter
		mv = new MutationMethodAdapter(classLoader, className, methodAccess, methodName, 
				descriptor, signature, exceptions, mv);
		
		return mv;
		
		
	}

}
