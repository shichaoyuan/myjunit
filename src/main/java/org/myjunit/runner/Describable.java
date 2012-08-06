package org.myjunit.runner;

/**
 * Represents an object that can describe itself
 */
public interface Describable {
	/**
	 * @return a {@link Description} showing the tests to be run by the receiver
	 */
	public Description getDescription();
}