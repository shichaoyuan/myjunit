package myjunit.framework;


/**
 * A <em>Test</em> can be run and collect its results.
 *
 * @see TestResult
 */
public interface Test {
	/**
	 * Runs a test and collects its result in a TestResult instance.
	 */
	public void run(TestResult result);
	/**
	 * Counts the number of test cases that will be run by this test.
	 */
	public int countTestCases();
}
