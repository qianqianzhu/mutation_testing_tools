package org.qianqianzhu.junit;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class JUnitExecutionListener extends RunListener {
	
//    Thread watcherThread ;
//    Thread junitTestThread;
//    final static int testTimeout = 10000;  // time control (10s)

	@Override
	public void testRunStarted(Description description) throws Exception {
		System.out.println("Number of tests to execute: " + description.testCount());
	}

	@Override
	public void testRunFinished(Result result) throws Exception {
		System.out.println("Number of tests executed: " + result.getRunCount());
	}

	@Override
	public void testStarted(Description description) throws Exception {
		System.out.println("Starting: " + description.getMethodName());
//		junitTestThread = Thread.currentThread();
//      watcherThread = new Thread()
//      {
//			@Override 
//          public void run()
//          {
//                  try {
//                      Thread.sleep(testTimeout);
//                      junitTestThread.stop();
//                  } catch (InterruptedException e) {
//                      e.printStackTrace();
//                  }
//          }
//      };
//      watcherThread.start();
	}

	@Override
	public void testFinished(Description description) throws Exception {
		System.out.println("Finished: " + description.getMethodName());
//		watcherThread.stop();
	}

	@Override
	public void testFailure(Failure failure) throws Exception {
			System.out.println("Failed: " + failure.getDescription().getMethodName()+failure.getTrace());
	}

	@Override
	public void testAssumptionFailure(Failure failure) {
		System.out.println("Failed: " + failure.getDescription().getMethodName());
	}

	@Override
	public void testIgnored(Description description) throws Exception {
		System.out.println("Ignored: " + description.getMethodName());
	}
}
