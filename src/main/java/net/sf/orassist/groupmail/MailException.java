package net.sf.orassist.groupmail;

public class MailException extends Exception {
	private String message;
	
	public MailException(Throwable t, String message) {
		super(t);
		this.message = message;
	}
	
	public String get() {
		return super.getLocalizedMessage() + " / " + message;
	}
	
	public String getMessage() {
		return super.getMessage() + " / " + message;
	}
}
