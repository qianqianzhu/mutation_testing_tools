package org.qianqianzhu.instrument.util;

public class TestExecutionResult {

	private final boolean testResult;
	private final long executionTime;
	
	public TestExecutionResult(boolean testResult, long executionTime){
		this.testResult = testResult;
		this.executionTime = executionTime;
	}
	
	public long getExecutionTime(){
		return executionTime;
	}
	public boolean getTestResult(){
		return testResult;
	}
	
	
}
