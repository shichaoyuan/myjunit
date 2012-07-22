package myjunit.framework;

import java.io.PrintWriter;
import java.io.StringWriter;

public class TestFailure {
	
	protected Test failedTest;
	protected Throwable thrownException;
	
	
	public TestFailure(Test failedTest, Throwable thrownException) {
		this.failedTest = failedTest;
		this.thrownException = thrownException;
	}
	
	public Test getFailedTest() {
		return this.failedTest;
	}
	
	public Throwable getThrownException() {
		return this.thrownException;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(failedTest + ": " + thrownException.getMessage());
		return buffer.toString();
	}
	
	public String trace() {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		getThrownException().printStackTrace(writer);
		StringBuffer buffer = stringWriter.getBuffer();
		return buffer.toString();
	}
	
	public String exceptionMessage() {
		return getThrownException().getMessage();
	}
	
	public Boolean isFailure() {
		return getThrownException() instanceof AssertionFailedError;
	}
	
	

}
