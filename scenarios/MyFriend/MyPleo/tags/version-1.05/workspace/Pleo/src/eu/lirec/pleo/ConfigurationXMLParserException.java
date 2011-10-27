package eu.lirec.pleo;

public class ConfigurationXMLParserException extends Exception {
	private static final long serialVersionUID = 858514508056806059L;
	
	public ConfigurationXMLParserException() {
	}

	public ConfigurationXMLParserException(String detailMessage) {
		super(detailMessage);
	}

	public ConfigurationXMLParserException(Throwable throwable) {
		super(throwable);
	}

	public ConfigurationXMLParserException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}
}