package es.tododev.socket;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class DebugLogger {
	
    private final static Logger LOGGER;
	private final boolean debug;
	
	static {
	    try (InputStream configFile = DebugLogger.class.getResourceAsStream("/logging.properties")) {
	        LogManager.getLogManager().readConfiguration(configFile);
	        LOGGER = Logger.getLogger(DebugLogger.class.getName());
	    } catch (IOException e) {
            throw new IllegalStateException("Cannot initialize the logger", e);
        }
	}

	public DebugLogger(boolean debug) {
		this.debug = debug;
	}
	
	public void debug(String message) {
		if (debug) {
			log(message);
		}
	}
	
	public void log(String message) {
	    LOGGER.info("[" + Thread.currentThread() + "] " + message);
	}
	
	public void logException(String message, Throwable t) {
	    log(message);
	    LOGGER.log(Level.SEVERE, message, t);
	}

	public boolean isDebug() {
		return debug;
	}
}
