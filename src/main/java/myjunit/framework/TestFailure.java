package myjunit.framework;

import java.io.PrintWriter;
import java.io.StringWriter;

public class TestFailure {
	
	protected Test fFailedTest;
	protected Throwable fThrownException;
	
	
	public TestFailure(Test fFailedTest, Throwable fThrownException) {
		this.fFailedTest = fFailedTest;
		this.fThrownException = fThrownException;
	}
	
	public Test failedTest() {
		return this.fFailedTest;
	}
	
	public Throwable thrownException() {
		return this.fThrownException;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(fFailedTest + ": " + fThrownException.getMessage());
		return buffer.toString();
	}
	
	public String trace() {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		thrownException().printStackTrace(writer);
		StringBuffer buffer = stringWriter.getBuffer();
		return buffer.toString();
	}
	
	public String exceptionMessage() {
		return thrownException().getMessage();
	}
	
	public Boolean isFailure() {
		return thrownException() instanceof AssertionFailedError;
	}
	
	

}
