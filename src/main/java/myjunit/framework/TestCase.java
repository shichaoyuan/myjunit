package myjunit.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public abstract class TestCase extends Assert implements Test {

	private String name;
	
	public TestCase() {
		name = null;
	}
	
	public TestCase(String name) {
		this.name = name;
	}

	@Override
	public void run(TestResult result) {
		result.run(this);
	}

	@Override
	public int countTestCases() {
		return 1;
	}
	
	protected TestResult createResult() {
		return new TestResult();
	}
	
	public TestResult run() {
		TestResult result = createResult();
		run(result);
		return result;
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
		assertNotNull("TestCase.name cannot be null", name);
		Method runMethod = null;
		try {
			runMethod = getClass().getMethod(name, (Class[])null);
		} catch (NoSuchMethodException e) {
			fail("Method \"" + name + "\" not found");
		}
		if (!Modifier.isPublic(runMethod.getModifiers())) {
			fail("Method \"" + name + "\" should be public");
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
	
	protected void setUp() throws Exception {
		
	}
	
	protected void tearDown() throws Exception {
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return getName() + "(" + getClass().getName() + ")";
	}
	
	
	
}
