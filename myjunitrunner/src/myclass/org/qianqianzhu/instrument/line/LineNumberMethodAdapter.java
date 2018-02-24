package org.qianqianzhu.instrument.line;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.qianqianzhu.instrument.util.ExecutionTracer;
import org.qianqianzhu.instrument.util.PackageInfo;

public class LineNumberMethodAdapter extends MethodVisitor {
	
	private final String fullMethodName;
	private final String methodName;
	private final String className;
	private boolean hadInvokeSpecial = false;
	private List<Integer> skippedLines = new ArrayList<Integer>();
	int currentLine = 0;

	public LineNumberMethodAdapter(MethodVisitor mv, String className, String methodName,
	        String desc) {
		super(Opcodes.ASM5, mv);
		fullMethodName = methodName + desc;
		this.className = className;
		this.methodName = methodName;
		if (!methodName.equals("<init>"))
			hadInvokeSpecial = true;
	}
	
	private void addLineNumberInstrumentation(int line) {
		
		LinePool.addLine(className, fullMethodName, line);
		this.visitLdcInsn(className);
		this.visitLdcInsn(fullMethodName);
		this.visitLdcInsn(line);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC,
				PackageInfo.getNameWithSlash(ExecutionTracer.class),
				"passedLine", "(Ljava/lang/String;Ljava/lang/String;I)V", false);
	}
	
	@Override
	public void visitLineNumber(int line, Label start) {
		super.visitLineNumber(line, start);
		currentLine = line;

		if (methodName.equals("<clinit>"))
			return;

		if (!hadInvokeSpecial) {
			skippedLines.add(line);
			return;
		}
		
		LinePool.addLine(className, fullMethodName, line);		
		addLineNumberInstrumentation(line);
	}
	
	
	
	

}
