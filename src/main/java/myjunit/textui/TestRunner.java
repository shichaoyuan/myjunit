package myjunit.textui;

import java.io.IOException;
import java.io.PrintStream;

import myjunit.framework.Test;
import myjunit.framework.TestCase;
import myjunit.framework.TestResult;
import myjunit.framework.TestSuite;
import myjunit.runner.BaseTestRunner;

public class TestRunner extends BaseTestRunner {
	private ResultPrinter printer;
	
	public static final int SUCCESS_EXIT = 0;
	public static final int FAILURE_EXIT = 1;
	public static final int EXCEPTION_EXIT = 2;
	
	public TestRunner() {
		this(System.out);
	}
	
	public TestRunner(PrintStream writer) {
		this(new ResultPrinter(writer));
	}
	
	public TestRunner(ResultPrinter printer) {
		this.printer = printer;
	}
	
	static public void run(Class<? extends TestCase> testClass) {
		run(new TestSuite(testClass));
	}
	
	static public TestResult run(Test test) {
		TestRunner runner = new TestRunner();
		return runner.doRun(test);
	}
	
	static public void runAndWait(Test suite) {
		TestRunner runner = new TestRunner();
		runner.doRun(suite, true);
	}
	
	@Override
	public void testStarted(String testName) {
	}

	@Override
	public void testEnded(String testName) {
	}

	@Override
	public void testFailed(int status, Test test, Throwable t) {
	}
	
	protected TestResult createTestResult() {
		return new TestResult();
	}
	
	public TestResult doRun(Test test) {
		return doRun(test, false);
	}
	
	public TestResult doRun(Test suite, boolean wait) {
		TestResult result = createTestResult();
		result.addListener(printer);
		long startTime = System.currentTimeMillis();
		suite.run(result);
		long endTime = System.currentTimeMillis();
		long runTime = endTime - startTime;
		printer.print(result, runTime);
		
		pause(wait);
		return result;
	}
	
	protected void pause(boolean wait) {
		if (!wait)
			return;
		try {
			System.in.read();
		} catch (IOException e) {
		}
	}
	
	@Override
	protected void runFailed(String message) {
		System.err.println(message);
		System.exit(FAILURE_EXIT);		
	}

	public void setPrinter(ResultPrinter printer) {
		this.printer= printer;
	}
}
