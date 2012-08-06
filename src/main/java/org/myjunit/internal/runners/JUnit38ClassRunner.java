package org.myjunit.internal.runners;

import myjunit.framework.AssertionFailedError;
import myjunit.framework.Test;
import myjunit.framework.TestCase;
import myjunit.framework.TestListener;
import myjunit.framework.TestResult;
import myjunit.framework.TestSuite;

import org.myjunit.runner.Describable;
import org.myjunit.runner.Description;
import org.myjunit.runner.Runner;
import org.myjunit.runner.manipulation.Filter;
import org.myjunit.runner.manipulation.Filterable;
import org.myjunit.runner.manipulation.NoTestsRemainException;
import org.myjunit.runner.manipulation.Sortable;
import org.myjunit.runner.manipulation.Sorter;
import org.myjunit.runner.notification.Failure;
import org.myjunit.runner.notification.RunNotifier;

public class JUnit38ClassRunner extends Runner implements Filterable, Sortable {

	private Test test;

	public JUnit38ClassRunner(Class<?> clazz) {
		this(new TestSuite(clazz.asSubclass(TestCase.class)));
	}

	public JUnit38ClassRunner(Test test) {
		super();
		setTest(test);
	}

	@Override
	public void run(RunNotifier notifier) {
		TestResult result = new TestResult();
		result.addListener(createAdaptingListener(notifier));
		getTest().run(result);
	}

	@Override
	public Description getDescription() {
		return makeDescription(getTest());
	}

	private static Description makeDescription(Test test) {
		if (test instanceof TestCase) {
			TestCase tc = (TestCase) test;
			return Description.createTestDescription(
					tc.getClass(), tc.getTestMethodName());
		} else if (test instanceof TestSuite) {
			TestSuite ts = (TestSuite) test;
			String name = ts.getTestClassName() == null 
					? createSuiteDescription(ts)
					: ts.getTestClassName();
			Description description = Description.createSuiteDescription(name);
			int n = ts.getTestsCount();
			for (int i = 0; i < n; i++) {
				Description made = makeDescription(ts.testAt(i));
				description.addChild(made);
			}
			return description;
		} else if (test instanceof Describable) {
			Describable adapter = (Describable) test;
			return adapter.getDescription();
		} else if (test instanceof TestDecorator) {
			TestDecorator decorator = (TestDecorator) test;
			return makeDescription(decorator.getTest());
		} else {
			return Description.createSuiteDescription(test.getClass());
		}
	}
	
	private static String createSuiteDescription(TestSuite ts) {
		int count= ts.countTestCases();
		String example = count == 0 ? "" : String.format(" [example: %s]", ts.testAt(0));
		return String.format("TestSuite with %s tests%s", count, example);
	}

	public TestListener createAdaptingListener(final RunNotifier notifier) {
		return new OldTestClassAdaptingListener(notifier);
	}

	public Test getTest() {
		return test;
	}

	public void setTest(Test test) {
		this.test = test;
	}

	private final class OldTestClassAdaptingListener implements TestListener {

		private final RunNotifier notifier;

		public OldTestClassAdaptingListener(RunNotifier notifier) {
			this.notifier = notifier;
		}

		@Override
		public void addError(Test test, Throwable t) {
			Failure failure = new Failure(asDescription(test), t);
			notifier.fireTestFailure(failure);
		}

		@Override
		public void addFailure(Test test, AssertionFailedError t) {
			addError(test, t);
		}

		@Override
		public void endTest(Test test) {
			notifier.fireTestFinished(asDescription(test));
		}

		@Override
		public void startTest(Test test) {
			notifier.fireTestStarted(asDescription(test));
		}

		private Description asDescription(Test test) {
			if (test instanceof Describable) {
				Describable facade = (Describable) test;
				return facade.getDescription();
			}
			return Description.createTestDescription(getEffectiveClass(test),
					getName(test));
		}

		private Class<? extends Test> getEffectiveClass(Test test) {
			return test.getClass();
		}

		private String getName(Test test) {
			if (test instanceof TestCase)
				return ((TestCase) test).getTestMethodName();
			else
				return test.toString();
		}
	}
}
