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
		printDefects(result.getErrors(), result.errorCount(), "error");
	}
	
	protected void printFailures(TestResult result) {
		printDefects(result.getFailures(), result.failureCount(), "failure");
	}
	
	protected void printDefects(List<TestFailure> booBoo, int count, String type) {
		if (count == 0)
			return;
		if (count == 1) {
			getWriter().println("There was " + count + " " + type + ":");
		} else {
			getWriter().println("There were " + count + " " + type + "s:");
		}
		for (int i = 1; i <= booBoo.size(); i++) {
			printDefect(booBoo.get(i-1), i);
		}
	}
	
	public void printDefect(TestFailure booBoo, int count) {
		printDefectHeader(booBoo, count);
		printDefectTrace(booBoo);
	}
	
	protected void printDefectHeader(TestFailure booBoo, int count) {
		getWriter().print(count + ") " + booBoo.getFailedTest());
	}
	
	protected void printDefectTrace(TestFailure booBoo) {
		getWriter().print(BaseTestRunner.getFilteredTrace(booBoo.trace()));
	}
	
	protected void printFooter(TestResult result) {
		if (result.wasSuccessful()) {
			getWriter().println();
			getWriter().print("OK");
			getWriter().println(" (" + result.getRunTestCount() + " test" + (result.getRunTestCount() == 1 ? "" : "s") + ")");
		} else {
			getWriter().println();
			getWriter().println("FAILURES!!!");
			getWriter().println("Test run: " + result.getRunTestCount() +
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

	public void addError(Test test, Throwable t) {
		getWriter().print("E");
	}

	public void addFailure(Test test, AssertionFailedError t) {
		getWriter().print("F");
	}

	public void endTest(Test test) {

	}

	public void startTest(Test test) {
		getWriter().print(".");
		if (column++ >= 40) {
			getWriter().println();
			column = 0;
		}
	}

}
