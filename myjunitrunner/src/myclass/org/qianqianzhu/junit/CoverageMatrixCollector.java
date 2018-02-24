package org.qianqianzhu.junit;

import java.util.Map;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.qianqianzhu.instrument.util.ExecutionTracer;

public class CoverageMatrixCollector extends BlockJUnit4ClassRunner {
	
	private final String className;

	public CoverageMatrixCollector(Class<?> klass) throws InitializationError {
		super(klass);
		this.className = klass.getName();
	}
	
	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier) {
		String fullMethodName = className + ":"+method.getName();
		notifier.addListener(new JUnitExecutionListener());
		
		super.runChild(method, notifier);
		Map<String, Map<String, Map<Integer, Integer>>> touchedLine = ExecutionTracer.getExecutionTracer().getTrace().getLineCoverage();
		System.out.println(touchedLine.toString());
		ExecutionTracer.getExecutionTracer().clear();
	}
	

}
