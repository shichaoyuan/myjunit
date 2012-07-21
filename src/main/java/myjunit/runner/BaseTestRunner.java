package myjunit.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Properties;

import myjunit.framework.AssertionFailedError;
import myjunit.framework.Test;
import myjunit.framework.TestListener;
import myjunit.framework.TestSuite;

public abstract class BaseTestRunner implements TestListener {

	public static final String SUITE_METHODNAME = "suite";

	private static Properties preferences;
	static int maxMessageLength = 500;
	static boolean filterStack = true;
	boolean loading = true;
	
	static {
		maxMessageLength = getPreference("maxmessage", maxMessageLength);
	}

	@Override
	public synchronized void startTest(Test test) {
		testStarted(test.toString());
	}

	@Override
	public synchronized void endTest(Test test) {
		testEnded(test.toString());
	}

	@Override
	public synchronized void addError(Test test, Throwable t) {
		testFailed(TestRunListener.STATUS_ERROR, test, t);
	}

	@Override
	public synchronized void addFailure(Test test, AssertionFailedError t) {
		testFailed(TestRunListener.STATUS_FAILURE, test, t);
	}

	public abstract void testStarted(String testName);

	public abstract void testEnded(String testName);

	public abstract void testFailed(int status, Test test, Throwable t);

	protected abstract void runFailed(String message);

	public Test getTest(String suiteClassName) {
		if (suiteClassName.length() <= 0) {
			clearStatus();
			return null;
		}
		Class<?> testClass = null;
		try {
			testClass = loadSuiteClass(suiteClassName);
		} catch (ClassNotFoundException e) {
			String className = e.getMessage();
			if (className == null) {
				className = suiteClassName;
			}
			runFailed("Class not found \"" + className + "\"");
			return null;
		} catch (Exception e) {
			runFailed("Error: " + e.toString());
			return null;
		}
		Method suiteMethod = null;
		try {
			suiteMethod = testClass.getMethod(SUITE_METHODNAME, new Class[0]);
		} catch (Exception e) {
			clearStatus();
			return new TestSuite(testClass);
		}
		if (!Modifier.isStatic(suiteMethod.getModifiers())) {
			runFailed("Suite() method must be static");
			return null;
		}
		Test test = null;
		try {
			test = (Test) suiteMethod.invoke(null, (Object[]) new Class[0]);
			if (test == null)
				return test;
		} catch (InvocationTargetException e) {
			runFailed("Failed to invoke suite(): "
					+ e.getTargetException().toString());
			return null;
		} catch (IllegalAccessException e) {
			runFailed("Failed to invoke suite(): " + e.toString());
			return null;
		}
		clearStatus();
		return test;
	}

	protected String processArguments(String[] args) {
		String suiteName = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-noloading")) {
				setLoading(false);
			} else if (args[i].equals("-nofilterstack")) {
				filterStack = false;
			} else if (args[i].equals("-c")) {
				if (args.length > i + 1) {
					suiteName = extractClassName(args[i + 1]);
				} else {
					System.out.println("Missing Test class name");
				}
				i++;
			} else {
				suiteName = args[i];
			}
		}
		return suiteName;
	}

	public void setLoading(boolean enable) {
		loading = enable;
	}

	public String extractClassName(String className) {
		if (className.startsWith("Default package for")) {
			return className.substring(className.lastIndexOf(".") + 1);
		}
		return className;
	}

	protected Class<?> loadSuiteClass(String suiteClassName)
			throws ClassNotFoundException {
		return Class.forName(suiteClassName);
	}

	protected void clearStatus() {
	}

	protected boolean useReloadingTestSuiteLoader() {
		return getPreference("loading").equals("true") && loading;
	}

	protected static void setPreferences(Properties p) {
		preferences = p;
	}

	protected static Properties getPreferences() {
		if (preferences == null) {
			preferences = new Properties();
			preferences.put("loading", "true");
			preferences.put("filterstack", "true");
			readPreferences();
		}
		return preferences;
	}

	private static void readPreferences() {
		InputStream is = null;
		try {
			is = new FileInputStream(getPreferencesFile());
			setPreferences(new Properties(getPreferences()));
			getPreferences().load(is);
		} catch (IOException e) {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e1) {
			}
		}
	}

	private static File getPreferencesFile() {
		String home = System.getProperty("user.home");
		return new File(home, "myjunit.properties");
	}

	public static String getPreference(String key) {
		return getPreferences().getProperty(key);
	}

	public static int getPreference(String key, int dlft) {
		String value = getPreference(key);
		int intValue = dlft;
		if (value == null) {
			return dlft;
		}
		try {
			intValue = Integer.parseInt(value);
		} catch (NumberFormatException e) {
		}
		return intValue;
	}

	public static String getFilteredTrace(Throwable t) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		t.printStackTrace(writer);
		StringBuffer buffer = stringWriter.getBuffer();
		String trace = buffer.toString();
		return getFilteredTrace(trace);
	}

	public static String getFilteredTrace(String stack) {
		if (showStackRaw()) {
			return stack;
		}

		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		StringReader stringReader = new StringReader(stack);
		BufferedReader reader = new BufferedReader(stringReader);

		String line;
		try {
			while ((line = reader.readLine()) != null) {
				if (!filterLine(line)) {
					writer.println(line);
				}
			}
		} catch (Exception e) {
			return stack;
		}
		return stringWriter.toString();
	}

	protected static boolean showStackRaw() {
		return !getPreference("filterstack").equals("true")
				|| filterStack == false;
	}

	static boolean filterLine(String line) {
		String[] patterns = new String[] {
			"myjunit.framework.TestCase",
			"myjunit.framework.TestResult",
			"myjunit.framework.TestSuite",
			"myjunit.framework.Assert.",
			"myjunit.swingui.TestRunner",
			"myjunit.awtui.TestRunner",
			"myjunit.textui.TestRunner",
			"java.lang.reflect.Method.invoke("
		};
		for (int i = 0; i < patterns.length; i++) {
			if (line.indexOf(patterns[i]) >= 0) {
				return true;
			}
		}
		return false;
	}

	public static void savePreferences() throws IOException {
		FileOutputStream fos = new FileOutputStream(getPreferencesFile());
		try {
			getPreferences().store(fos, "");
		} finally {
			fos.close();
		}
	}

	public static void setPreference(String key, String value) {
		getPreferences().put(key, value);
	}

}
