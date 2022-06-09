package es.tododev.socket;

public class DebugLogger {
	
	private final boolean debug;

	public DebugLogger(boolean debug) {
		this.debug = debug;
	}
	
	public void debug(String message) {
		if (debug) {
			log(message);
		}
	}
	
	public synchronized void log(String message) {
		System.out.println(message);
	}
	
	public synchronized void logException(String message, Throwable t) {
	    log(message);
	    t.printStackTrace();
	}

	public boolean isDebug() {
		return debug;
	}
}
