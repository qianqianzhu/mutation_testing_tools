package org.qianqianzhu.instrument.simple;

import java.lang.instrument.Instrumentation;

import org.qianqianzhu.instrument.mutation.MutationTransformer;

public class SimpleMutationAgent {
	
	// for all the class loaded, premain will be called
	public static void premain(String agentArgs, Instrumentation inst) {
		
		System.out.println("Executing premain.........");
		inst.addTransformer(new MutationTransformer());
	}

}
