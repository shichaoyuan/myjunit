package myjunit.framework;


/**
 * A <em>Protectable</em> can be run and can throw a Throwable.
 *
 * @see TestResult
 */
public interface Protectable {
	/**
	 * Run the the following method protected.
	 */
	public void protect() throws Throwable;
}
