package es.tododev.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class MiddleCommunicator implements AutoCloseable {

	private final Socket readerSocket;
	private final String readerId;
	private final Socket writerSocket;
	private final String writerId;
	private final CountDownLatch latch;
	private final DebugLogger logger;
	private final boolean originToForward;
	private final AtomicBoolean running = new AtomicBoolean(true);

	public MiddleCommunicator(Socket readerSocket, Socket writerSocket, CountDownLatch latch, DebugLogger logger, boolean originToForward) {
		this.readerSocket = readerSocket;
		this.writerSocket = writerSocket;
		this.latch = latch;
		this.logger = logger;
		this.originToForward = originToForward;
		this.readerId = readerSocket.getInetAddress().getHostAddress() + ":" + readerSocket.getPort();
		this.writerId = writerSocket.getInetAddress().getHostAddress() + ":" + writerSocket.getPort();
	}

	public void start() {
		Thread reader = new Thread(originToForward ? new OriginToForwardReader() : new ForwardToOriginReader());
		reader.start();
	}

	@Override
	public void close() throws Exception {
		stop();
	}

	public void stop() {
		if (running.getAndSet(false)) {
			logger.log(this + " > " + writerId + " stopped.");
			latch.countDown();
		}
	}

    @Override
    public String toString() {
        return originToForward ? "OriginToForward [" + readerId + "]" : "ForwardToOrigin [" + readerId + "]";
    }

	private abstract class Reader implements Runnable {

		@Override
		public void run() {
		    // 1 MB
			byte[] buffer = new byte[1024 * 1024];
			while(running.get()) {
			    try(BufferedOutputStream writer = new BufferedOutputStream(writerSocket.getOutputStream());) {
    				try (BufferedInputStream reader = new BufferedInputStream(readerSocket.getInputStream());) {
    					int read;
    					while(running.get() && (read = reader.read(buffer)) != -1) {
    						logger.debug(MiddleCommunicator.this + " " + read + " bytes");
    						logger.debug(new String(buffer, 0, read));
    						writer.write(buffer, 0, read);
    						writer.flush();
    					}
    				} catch (IOException e) {
    				    logger.logException(MiddleCommunicator.this + " cannot read.", e);
    				    stop();
    				}
			    } catch (IOException e1) {
			        logger.logException(MiddleCommunicator.this + " cannot write.", e1);
			        stop();
                }
			}
		}
	}

	// Make it easy to understand stacktraces
	private class OriginToForwardReader extends Reader {
        @Override
        public void run() {
            super.run();
        }
	}
	private class ForwardToOriginReader extends Reader {
        @Override
        public void run() {
            super.run();
        }
	}
}
