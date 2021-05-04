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
	private final AtomicBoolean running = new AtomicBoolean(true);

	public MiddleCommunicator(Socket readerSocket, Socket writerSocket, CountDownLatch latch, DebugLogger logger) {
		this.readerSocket = readerSocket;
		this.writerSocket = writerSocket;
		this.latch = latch;
		this.logger = logger;
		this.readerId = readerSocket.getInetAddress().getHostAddress() + ":" + readerSocket.getPort();
		this.writerId = writerSocket.getInetAddress().getHostAddress() + ":" + writerSocket.getPort();
	}

	public void start() {
		Thread reader = new Thread(new Reader());
		reader.start();
	}

	@Override
	public void close() throws Exception {
		stop();
	}

	public void stop() {
		if (running.getAndSet(false)) {
			logger.log(readerId + " > " + writerId + " stopped.");
			latch.countDown();
		}
	}

	private class Reader implements Runnable {

		@Override
		public void run() {
			byte[] buffer = new byte[1024];
			while(running.get()) {
				try (BufferedInputStream reader = new BufferedInputStream(readerSocket.getInputStream());
						BufferedOutputStream writer = new BufferedOutputStream(writerSocket.getOutputStream());) {
					int read;
					while(running.get() && (read = reader.read(buffer)) != -1) {
						logger.debug(read + " bytes from " + readerId);
						if (logger.isDebug()) {
							logger.debug(new String(buffer, 0, read));
						}
						writer.write(buffer, 0, read);
						writer.flush();
					}
				} catch (IOException e) {
					e.printStackTrace();
					stop();
				}
			}
		}
	}

}
