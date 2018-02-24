package org.qianqianzhu.instrument.line;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MyClassVisitor extends ClassVisitor {
	private String className;
	public MyClassVisitor(ClassVisitor cv, String pClassName){
		super(Opcodes.ASM5,cv);
		 className = pClassName;
	}
	@Override
public MethodVisitor visitMethod(int access, String methodName, String desc,
            String signature, String[] exceptions){
		MethodVisitor mv = super.visitMethod(access, methodName, desc, signature, exceptions);
		//System.out.println("class:"+className+" method:"+methodName);
		return new LineNumberMethodAdapter(mv,className,methodName,desc);
		
	}

}
