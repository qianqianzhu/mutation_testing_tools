package org.qianqianzhu.instrument.mutation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.qianqianzhu.instrument.util.JunitHelper;
import org.qianqianzhu.instrument.util.Properties;

public class InsertInstrumentation {

	public static void main(String[] args) throws FileNotFoundException, IOException{

		String projectDir = "/Users/zhuqianqian/workspace/mutation/sut/jpacman-framework/target/classes";
		Set<String> classNeedInstrumentation = JunitHelper.findClasses(true,
				projectDir,
				projectDir);
		long startTime = System.currentTimeMillis();
		for(String className : classNeedInstrumentation){
			System.out.println("processing class: "+className);
			String filePath = projectDir + File.separator + className + ".class";
			ClassReader cr = new ClassReader(new FileInputStream(new File(filePath)));
			ClassWriter cw = new ClassWriter(cr,ClassWriter.COMPUTE_FRAMES);
			ClassVisitor cv = cw;
			cv = new MutationClassAdapter(cv, className);
			cr.accept(cv,ClassReader.SKIP_FRAMES);

			byte[] bytecode;
			// try to avoid too large method code exception
			try{
				 bytecode= cw.toByteArray();
			}catch(RuntimeException e){
				e.printStackTrace();
				bytecode = cr.b;
			}

				// write transformed bytecode to temperate file
				try {
					String path = projectDir.substring(0,projectDir.lastIndexOf("/"))
							+"/test_instrumentation/"
							+className.substring(0, className.lastIndexOf("/"));
					File dir = new File(path);
					if(!dir.exists()){
						boolean created = dir.mkdirs();
						if(!created){
							String msg = "Cannot create report dir: "+Properties.REPORT_DIR;
							System.err.println(msg);
							throw new RuntimeException(msg);
						}		
					}
					String newfilePath = className.substring(className.lastIndexOf("/"))+".class";
					File f = new File(path+ File.separator + newfilePath);
					FileOutputStream output = new FileOutputStream(f);
					output.write(bytecode);
					output.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

		}
		
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);
		System.out.println("Total execution time: "+duration + " ms, mutationNo:" + MutationPool.getMutationSize());

	}
}
