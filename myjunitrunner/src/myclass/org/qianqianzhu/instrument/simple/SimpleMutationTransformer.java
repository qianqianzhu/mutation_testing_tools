package org.qianqianzhu.instrument.simple;

import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;

public class SimpleMutationTransformer implements ClassFileTransformer{
	
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		
		byte[] bytecode = classfileBuffer;
		
		if(className.equals("org/apache/commons/math3/analysis/function/Gaussian")){
			System.out.println("Instrumenting......");
			ClassReader cr = new ClassReader(bytecode);
			ClassWriter cw = new ClassWriter(cr,0);
			// for debugging
			TraceClassVisitor tc = new TraceClassVisitor(cw, new PrintWriter(System.err));
			ClassVisitor cv = new SimpleMutationClassAdapter(loader, tc, className);
			cr.accept(cv, 0);
			return cw.toByteArray();
		}
		return classfileBuffer;
	}

}
