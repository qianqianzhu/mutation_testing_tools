package org.qianqianzhu.myjunitrunner.instrument;

import java.util.Iterator;
import java.util.List;

import org.qianqianzhu.myjunitrunner.example.Hello;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LineNumberAttribute;


public class TestHello {
	public static void main(String[] args) throws Exception{
		ClassPool cp = ClassPool.getDefault();
		// need to set the whole package prefix
		CtClass cc = cp.get("org.qianqianzhu.myjunitrunner.example.Hello");
		CtMethod[] methods = cc.getDeclaredMethods();
		for(CtMethod m : methods){
			CodeAttribute ca = m.getMethodInfo().getCodeAttribute();
			// Access the LineNumberAttribute
		    LineNumberAttribute la = (LineNumberAttribute) ca.getAttribute(LineNumberAttribute.tag);
		 // Index in bytecode array where the instruction starts
		    int startPc = la.toLineNumber(0);
			m.insertBefore("{ System.out.println(\"Hello:\");}");
			System.out.println(m.getMethodInfo2().getName());		
		}
		
			Class<?> c = cc.toClass();
			Hello h = (Hello) c.newInstance();
			h.say();
//			h.smile();
		
	}
}
