package myjunit.framework;

import java.util.ArrayList;
import java.util.List;

public class TestResult {
	protected List<TestFailure> fFailures;
	protected List<TestFailure> fErrors;
	protected List<TestListener> fListeners;
	protected int fRunTests;
	private boolean fStop;
	
	public TestResult() {
		this.fFailures = new ArrayList<TestFailure>();
		this.fErrors = new ArrayList<TestFailure>();
		this.fListeners = new ArrayList<TestListener>();
		this.fRunTests = 0;
		this.fStop = false;
	}
	
	public synchronized void addError(Test test, Throwable t) {
		fErrors.add(new TestFailure(test, t));
		for (TestListener each : fListeners) {
			each.addError(test, t);
		}
		
	}
	
	public synchronized void addFailure(Test test, AssertionFailedError t) {
		fFailures.add(new TestFailure(test, t));
		for (TestListener each : fListeners) {
			each.addFailure(test, t);
		}
	}
	
	public synchronized void addListener(TestListener listener) {
		fListeners.add(listener);
	}
	
	public synchronized void removeListener(TestListener listener) {
		fListeners.remove(listener);
	}
	
	private synchronized List<TestListener> cloneListeners() {
		List<TestListener> result = new ArrayList<TestListener>();
		result.addAll(fListeners);
		return result;
	}
	
	public void endTest(Test test) {
		for (TestListener each : cloneListeners()) {
			each.endTest(test);
		}
	}
	
	public synchronized int errorCount() {
		return fErrors.size();
	}
	
	public synchronized List<TestFailure> errors() {
		return fErrors;
	}
	
	public synchronized int failureCount() {
		return fFailures.size();
	}
	
	public synchronized List<TestFailure> failures() {
		return fFailures;
	}
	
	protected void run(final TestCase test) {
		
	}
}
