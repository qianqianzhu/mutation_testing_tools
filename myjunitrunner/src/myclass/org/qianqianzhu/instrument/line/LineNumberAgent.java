package org.qianqianzhu.instrument.line;

import java.lang.instrument.Instrumentation;


public class LineNumberAgent {

	// for all the class loaded, premain will be called
	public static void premain(String agentArgs, Instrumentation inst) {
		System.out.println("Executing premain.........");
		inst.addTransformer(new LineNumberTransformer());
	}
}
