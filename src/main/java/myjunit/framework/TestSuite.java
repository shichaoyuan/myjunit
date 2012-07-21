package myjunit.framework;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class TestSuite implements Test {
	
	private String name;
	private List<Test> tests = new ArrayList<Test>();
	
	public TestSuite() {
	}
	
	public TestSuite(final Class<?> clazz) {
		addTestsFromTestCase(clazz);
	}
	
	public TestSuite(Class<? extends TestCase> clazz, String name) {
		this(clazz);
		setName(name);
	}
	
	public TestSuite(String name) {
		setName(name);
	}
	
	public TestSuite(Class<?>... classes) {
		for (Class<?> clazz : classes) {
			addTest(testCaseForClass(clazz));
		}
	}
	
	public TestSuite(Class<? extends TestCase>[] classes, String name) {
		this(classes);
		setName(name);
	}
	
	public void addTest(Test test) {
		tests.add(test);
	}
	
	public void addTestSuite(Class<? extends TestCase> clazz) {
		addTest(new TestSuite(clazz));
	}
	
	private void addTestMethod(Method m, List<String> names, Class<?> clazz) {
		String name = m.getName();
		if (names.contains(name)) {
			return;
		}
		if (!isPublicTestMethod(m)) {
			if (isTestMethod(m)) {
				addTest(warning("Test method isn't public: " + m.getName() + " (" + clazz.getCanonicalName() + ")"));
			}
			return;
		}
		names.add(name);
		addTest(createTest(clazz, name));
	}
	
	private boolean isPublicTestMethod(Method m) {
		return isTestMethod(m) && Modifier.isPublic(m.getModifiers());
	}
	
	private boolean isTestMethod(Method m) {
		return m.getParameterTypes().length == 0 &&
				m.getName().startsWith("test") &&
				m.getReturnType().equals(Void.TYPE);
	}
	
	private Test testCaseForClass(Class<?> clazz) {
		if (TestCase.class.isAssignableFrom(clazz)) {
			return new TestSuite(clazz.asSubclass(TestCase.class));
		} else {
			return warning(clazz.getCanonicalName() + "does not extend TestCase");
		}
	}
	
	private void addTestsFromTestCase(final Class<?> clazz) {
		name = clazz.getName();
		try {
			getTestConstructor(clazz);
		} catch (NoSuchMethodException e) {
			addTest(warning("Class " + clazz.getName() + " has no public constructor TestCase(String name) or TestCase()"));
			return;
		}
		
		if (!Modifier.isPublic(clazz.getModifiers())) {
			addTest(warning("Class " + clazz.getName() + " is not public"));
			return;
		}
		
		Class<?> superClazz = clazz;
		List<String> names = new ArrayList<String>();
		while (Test.class.isAssignableFrom(superClazz)) {
			for (Method method : superClazz.getDeclaredMethods()) {
				addTestMethod(method, names, clazz);
			}
			superClazz = superClazz.getSuperclass();
		}
		if (tests.size() == 0) {
			addTest(warning("No tests found in " + clazz.getName()));
		}
	}

	public static Test createTest(Class<?> clazz, String name) {
		Constructor<?> constructor;
		try {
			constructor = getTestConstructor(clazz);
		} catch (NoSuchMethodException e) {
			return warning("Class "
					+ clazz.getName()
					+ " has no public constructor TestCase(String name) or TestCase()");
		}
		Object test;
		try {
			if (constructor.getParameterTypes().length == 0) {
				test = constructor.newInstance(new Object[0]);
				if (test instanceof TestCase) {
					((TestCase) test).setName(name);
				}
			} else {
				test = constructor.newInstance(new Object[] { name });
			}
		} catch (InstantiationException e) {
			return warning("Cannot instantiate test case: " + name + " (" + exceptionToString(e) + ")");
		} catch (InvocationTargetException e) {
			return warning("Exception in constructor: " + name + " (" + exceptionToString(e.getTargetException()) + ")");
		} catch (IllegalAccessException e) {
			return warning("Cannot access test case: " + name + " (" + exceptionToString(e) + ")");
		}
		return (Test) test;
	}

	public static Constructor<?> getTestConstructor(Class<?> clazz)
			throws NoSuchMethodException {
		try {
			return clazz.getConstructor(String.class);
		} catch (NoSuchMethodException e) {
		}
		return clazz.getConstructor(new Class[0]);
	}
	
	public static Test warning(final String message) {
		return new TestCase("warning" ) {
			@Override
			public void runTest() {
				fail(message);
			}
		};
	}
	
	private static String exceptionToString(Throwable t) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		t.printStackTrace(printWriter);
		return stringWriter.toString();
	}
	
	public void runTest(Test test, TestResult result) {
		test.run(result);
	}
	
	public Test testAt(int index) {
		return tests.get(index);
	}
	
	public int testCount() {
		return tests.size();
	}
	
	public List<Test> tests() {
		return tests;
	}
	
	@Override
	public String toString() {
		if (getName() != null) {
			return getName();
		}
		return super.toString();
	}

	@Override
	public void run(TestResult result) {
		for (Test test : tests) {
			if (result.shouldStop())
				break;
			runTest(test, result);
		}
	}

	@Override
	public int countTestCases() {
		int count = 0;
		for (Test test : tests) {
			count += test.countTestCases();
		}
 		return count;
	}
	
	private void setName(String name) {
		this.name = name;
	}
	
	private String getName() {
		return name;
	}

}
