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
	
	public void log(String message) {
		System.out.println(message);
	}

	public boolean isDebug() {
		return debug;
	}
}
