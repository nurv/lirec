package eu.lirec.pleo;

public class MyPleoNeedsParserException extends Exception {
	private static final long serialVersionUID = 5982262403778452993L;

	public MyPleoNeedsParserException() {
	}

	public MyPleoNeedsParserException(String detailMessage) {
		super(detailMessage);
	}

	public MyPleoNeedsParserException(Throwable throwable) {
		super(throwable);
	}

	public MyPleoNeedsParserException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
