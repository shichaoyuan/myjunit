package myjunit.textui;

import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.List;

import myjunit.framework.AssertionFailedError;
import myjunit.framework.Test;
import myjunit.framework.TestFailure;
import myjunit.framework.TestListener;
import myjunit.framework.TestResult;
import myjunit.runner.BaseTestRunner;

public class ResultPrinter implements TestListener {
	PrintStream writer;
	int column = 0;
	
	public ResultPrinter(PrintStream writer) {
		this.writer = writer;
	}
	
	synchronized void print(TestResult result, long runTime) {
		printHeader(runTime);
		printErrors(result);
		printFailures(result);
		printFooter(result);
	}
	
	void printWaitPrompt() {
		getWriter().println();
		getWriter().println("<RETURN> to continue");
	}
	
	protected void printHeader(long runTime) {
		getWriter().println();
		getWriter().println("Time: " + elapsedTimeAsString(runTime));
	}
	
	protected void printErrors(TestResult result) {
		printDefects(result.errors(), result.errorCount(), "error");
	}
	
	protected void printFailures(TestResult result) {
		printDefects(result.failures(), result.failureCount(), "failure");
	}
	
	protected void printDefects(List<TestFailure> booBoo, int count, String type) {
		if (count == 0)
			return;
		if (count == 1) {
			getWriter().println("There was " + count + " " + type + ":");
		} else {
			getWriter().println("There were " + count + " " + type + "s:");
		}
		for (int i = 0; i < booBoo.size(); i++) {
			printDefect(booBoo.get(i), i);
		}
	}
	
	public void printDefect(TestFailure booBoo, int count) {
		printDefectHeader(booBoo, count);
		printDefectTrace(booBoo);
	}
	
	protected void printDefectHeader(TestFailure booBoo, int count) {
		getWriter().print(count + ") " + booBoo.failedTest());
	}
	
	protected void printDefectTrace(TestFailure booBoo) {
		getWriter().print(BaseTestRunner.getFilteredTrace(booBoo.trace()));
	}
	
	protected void printFooter(TestResult result) {
		if (result.wasSuccessful()) {
			getWriter().println();
			getWriter().print("OK");
			getWriter().println(" (" + result.runCount() + " test" + (result.runCount() == 1 ? "" : "s") + ")");
		} else {
			getWriter().println();
			getWriter().println("FAILURES!!!");
			getWriter().println("Test run: " + result.runCount() +
					", Failures: " + result.failureCount() +
					", Errors: " + result.errorCount());
		}
		getWriter().println();
	}
	
	protected String elapsedTimeAsString(long runTime) {
		return NumberFormat.getIntegerInstance().format((double)runTime/1000);
	}
	
	
	public PrintStream getWriter() {
		return this.writer;
	}

	@Override
	public void addError(Test test, Throwable t) {
		getWriter().print("E");
	}

	@Override
	public void addFailure(Test test, AssertionFailedError t) {
		getWriter().print("F");
	}

	@Override
	public void endTest(Test test) {

	}

	@Override
	public void startTest(Test test) {
		getWriter().print(".");
		if (column++ >= 40) {
			getWriter().println();
			column = 0;
		}
	}

}
