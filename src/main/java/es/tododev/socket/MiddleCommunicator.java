package es.tododev.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class MiddleCommunicator {

    private static final int BUFFER_SIZE = 1024 * 1024;
    private final ExecutorService executor;
    private final Socket readerSocket;
    private final String readerId;
    private final Socket writerSocket;
    private final String writerId;
    private final DebugLogger logger;
    private final boolean originToForward;
    private final Reader reader;

    public MiddleCommunicator(ExecutorService executor, Socket readerSocket, Socket writerSocket, DebugLogger logger, boolean originToForward) {
        this.executor = executor;
        this.readerSocket = readerSocket;
        this.writerSocket = writerSocket;
        this.logger = logger;
        this.originToForward = originToForward;
        this.readerId = readerSocket.getInetAddress().getHostAddress() + ":" + readerSocket.getPort();
        this.writerId = writerSocket.getInetAddress().getHostAddress() + ":" + writerSocket.getPort();
        this.reader = originToForward ? new OriginToForwardReader() : new ForwardToOriginReader();
    }

    public void start() {
        executor.submit(reader);
    }

    public void stop() {
        try {
            logger.log(this + " > " + readerSocket + " stopped.");
            readerSocket.close();
        } catch (IOException e) {
            logger.logException(MiddleCommunicator.this + " cannot close reader socket.", e);
        }
        try {
            logger.log(this + " > " + writerSocket + " stopped.");
            writerSocket.close();
        } catch (IOException e) {
            logger.logException(MiddleCommunicator.this + " cannot close writer socket.", e);
        }
    }

    @Override
    public String toString() {
        return originToForward ? "OriginToForward [" + readerId + "]" : "ForwardToOrigin [" + writerId + "]";
    }

    private abstract class Reader implements Runnable {

        @Override
        public void run() {
            // 1 KB
            byte[] buffer = new byte[BUFFER_SIZE];
            try {
                int read;
                InputStream in = readerSocket.getInputStream();
                OutputStream out = writerSocket.getOutputStream();
                while ((read = in.read(buffer)) != -1) {
                    if (logger.isDebug()) {
                        logger.debug(MiddleCommunicator.this + " " + read + " bytes");
                        logger.debug(new String(buffer, 0, read));
                    }
                    out.write(buffer, 0, read);
                    out.flush();
                }
            } catch (IOException e) {
                logger.logException("Error reading or writing from socket", e);
            } finally {
                stop();
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
