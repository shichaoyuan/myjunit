package myjunit.framework;

public class ComparisonFailure extends AssertionFailedError {

	private static final int MAX_CONTEXT_LENGTH = 20;
	
	private String expected;
	private String actual;
	
	public ComparisonFailure(String message, String expected, String actual) {
		super(message);
		this.expected = expected;
		this.actual = actual;
	}

	@Override
	public String getMessage() {
		return new ComparisonCompactor(MAX_CONTEXT_LENGTH, expected, actual).compact(super.getMessage());
	}

	public String getExpected() {
		return expected;
	}

	public String getActual() {
		return actual;
	}

	
	
}
