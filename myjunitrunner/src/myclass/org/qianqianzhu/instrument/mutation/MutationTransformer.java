package org.qianqianzhu.instrument.mutation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.qianqianzhu.instrument.mutation.MutationClassAdapter;
import org.qianqianzhu.instrument.util.Properties;


//this class will be registered by java agent
public class MutationTransformer implements ClassFileTransformer {

	public byte[] transform(ClassLoader loader, String className,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {

		byte[] bytecode = classfileBuffer;

		if(Properties.classNeedInstrumentation.contains(className)){
			System.out.println("instrumenting...");

			ClassReader cr = new ClassReader(bytecode);
			ClassWriter cw = new ClassWriter(cr,ClassWriter.COMPUTE_FRAMES);
			ClassVisitor cv = cw;
			// for debugging
			//cv = new TraceClassVisitor(cv, new PrintWriter(System.err));
			cv = new MutationClassAdapter(loader, cv, className);
			cr.accept(cv,ClassReader.SKIP_FRAMES);
			bytecode = cw.toByteArray();

			// write transformed bytecode to temp file
//			try {
//				String path = Properties.TARGET_PROJECT_DIR.substring(0,Properties.TARGET_PROJECT_DIR.lastIndexOf("/"))
//						+"/instrumentation/"
//						+className.substring(0, className.lastIndexOf("/"));
//				File dir = new File(path);
//				if(!dir.exists()){
//					boolean created = dir.mkdirs();
//					if(!created){
//						String msg = "Cannot create report dir: "+Properties.REPORT_DIR;
//						System.err.println(msg);
//						throw new RuntimeException(msg);
//					}		
//				}
//				String fileName = className.substring(className.lastIndexOf("/"))+".class";
//				File f = new File(path+ File.separator + fileName);
//				FileOutputStream output = new FileOutputStream(f);
//				output.write(bytecode);
//				output.close();
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
		}
		return bytecode;
	}




}
