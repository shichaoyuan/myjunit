package myjunit.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract class TestCase extends Assert implements Test {

	private String testMethodName;
	
	public TestCase() {
		testMethodName = null;
	}
	
	public TestCase(String testMethodName) {
		this.testMethodName = testMethodName;
	}

	@Override
	public void run(TestResult result) {
		result.run(this);
	}

	@Override
	public int countTestCases() {
		return 1;
	}
	
	public void runBare() throws Throwable {
		Throwable exception = null;
		setUp();
		try {
			runTest();
		} catch (Throwable running) {
			exception = running;
		} finally {
			try {
				tearDown();
			} catch (Throwable tearingDown) {
				if (exception == null)
					exception = tearingDown;
			}
		}
		if (exception != null)
			throw exception;
	}
	
	protected void runTest() throws Throwable {
		assertNotNull("TestCase.testMethodName cannot be null", testMethodName);
		Method runMethod = null;
		try {
			runMethod = getClass().getMethod(testMethodName, new Class[0]);
		} catch (NoSuchMethodException e) {
			fail("Method \"" + testMethodName + "\" not found");
		}
		if (!Modifier.isPublic(runMethod.getModifiers())) {
			fail("Method \"" + testMethodName + "\" should be public");
		}
		
		try {
			runMethod.invoke(this);
		} catch (InvocationTargetException e) {
			e.fillInStackTrace();
			throw e.getTargetException();
		} catch (IllegalAccessException e) {
			e.fillInStackTrace();
			throw e;
		}
	}
	
	
	@Override
	public String toString() {
		return getTestMethodName() + "(" + getClass().getName() + ")";
	}

	
	protected void setUp() throws Exception {	
	}
	protected void tearDown() throws Exception {	
	}
	
	
	public String getTestMethodName() {
		return testMethodName;
	}
	public void setTestMethodName(String testMethodName) {
		this.testMethodName = testMethodName;
	}
}
