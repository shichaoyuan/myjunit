package myjunit.framework;

import java.util.ArrayList;
import java.util.List;
/**
 * A <code>TestResult</code> collects the results of executing
 * a test case. It is an instance of the Collecting Parameter pattern.
 * The test framework distinguishes between <i>failures</i> and <i>errors</i>.
 * A failure is anticipated and checked for with assertions. Errors are
 * unanticipated problems like an {@link ArrayIndexOutOfBoundsException}.
 *
 * @see Test
 */
public class TestResult {
	protected List<TestFailure> failures;
	protected List<TestFailure> errors;
	protected List<TestListener> listeners;
	protected int runTestCount;
	private boolean stop;
	
	public TestResult() {
		this.failures = new ArrayList<TestFailure>();
		this.errors = new ArrayList<TestFailure>();
		this.listeners = new ArrayList<TestListener>();
		this.runTestCount = 0;
		this.stop = false;
	}
	
	public synchronized void addError(Test test, Throwable t) {
		errors.add(new TestFailure(test, t));
		for (TestListener each : listeners) {
			each.addError(test, t);
		}
	}
	public synchronized void addFailure(Test test, AssertionFailedError t) {
		failures.add(new TestFailure(test, t));
		for (TestListener each : listeners) {
			each.addFailure(test, t);
		}
	}
	
	public synchronized void addListener(TestListener listener) {
		listeners.add(listener);
	}
	public synchronized void removeListener(TestListener listener) {
		listeners.remove(listener);
	}
	
	private synchronized List<TestListener> cloneListeners() {
		List<TestListener> result = new ArrayList<TestListener>();
		result.addAll(listeners);
		return result;
	}
	
	public void startTest(Test test) {
		final int count = test.countTestCases();
		synchronized (this) {
			runTestCount += count;
		}
		for (TestListener each : cloneListeners()) {
			each.startTest(test);
		}
	}
	
	public void endTest(Test test) {
		for (TestListener each : cloneListeners()) {
			each.endTest(test);
		}
	}
	
	public synchronized int errorCount() {
		return errors.size();
	}
	
	public synchronized List<TestFailure> getErrors() {
		return errors;
	}
	
	public synchronized int failureCount() {
		return failures.size();
	}
	
	public synchronized List<TestFailure> getFailures() {
		return failures;
	}
	
	public synchronized int getRunTestCount() {
		return runTestCount;
	}
	
	protected void run(final TestCase test) {
		startTest(test);
		Protectable p = new Protectable() {
			public void protect() throws Throwable {
				test.runBare();
			}
		};
		runProtected(test, p);
		endTest(test);
	}
	
	public void runProtected(final Test test, Protectable p) {
		try {
			p.protect();
		} catch (AssertionFailedError e) {
			addFailure(test, e);
		} catch (ThreadDeath e) {
			throw e;
		} catch (Throwable e) {
			addError(test, e);
		}
	}
	
	public synchronized boolean shouldStop() {
		return stop;
	}
	
	public synchronized void stop() {
		stop = true;
	}
	
	public synchronized boolean wasSuccessful() {
		return failureCount() == 0 && errorCount() == 0;
	}
}
