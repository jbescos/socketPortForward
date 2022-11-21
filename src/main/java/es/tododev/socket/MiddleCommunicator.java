package es.tododev.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MiddleCommunicator {

    private final Socket readerSocket;
    private final String readerId;
    private final Socket writerSocket;
    private final String writerId;
    private final DebugLogger logger;
    private final boolean originToForward;

    public MiddleCommunicator(Socket readerSocket, Socket writerSocket, DebugLogger logger, boolean originToForward) {
        this.readerSocket = readerSocket;
        this.writerSocket = writerSocket;
        this.logger = logger;
        this.originToForward = originToForward;
        this.readerId = readerSocket.getInetAddress().getHostAddress() + ":" + readerSocket.getPort();
        this.writerId = writerSocket.getInetAddress().getHostAddress() + ":" + writerSocket.getPort();
    }

    public void start() {
        Thread reader = new Thread(originToForward ? new OriginToForwardReader() : new ForwardToOriginReader());
        reader.start();
    }

    public void stop() {
        logger.log(this + " > " + writerId + " stopped.");
        try {
            readerSocket.close();
        } catch (IOException e) {
            logger.logException(MiddleCommunicator.this + " cannot close reader socket.", e);
        }
        try {
            writerSocket.close();
        } catch (IOException e) {
            logger.logException(MiddleCommunicator.this + " cannot close writer socket.", e);
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
            try (BufferedOutputStream writer = new BufferedOutputStream(writerSocket.getOutputStream());
                    BufferedInputStream reader = new BufferedInputStream(readerSocket.getInputStream());) {
                int read;
                while ((read = reader.read(buffer)) != -1) {
                    logger.debug(MiddleCommunicator.this + " " + read + " bytes");
                    logger.debug(new String(buffer, 0, read));
                    writer.write(buffer, 0, read);
                    writer.flush();
                }
            } catch (IOException e) {
                // Any socket closed
            }
            stop();
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
