package org.qianqianzhu.instrument.line;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.qianqianzhu.instrument.util.Properties;

// this class will be registered by java agent
public class LineNumberTransformer implements ClassFileTransformer {
	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		
		if(Properties.classNeedInstrumentation.contains(className)){
			byte[] bytecode = classfileBuffer;
			ClassReader cr = new ClassReader(bytecode);
			ClassWriter cw = new ClassWriter(cr,ClassWriter.COMPUTE_FRAMES);
			ClassVisitor cv = new MyClassVisitor(cw, className);
			cr.accept(cv, ClassReader.SKIP_FRAMES);
			return cw.toByteArray();
		}
		return classfileBuffer;
	}


}
