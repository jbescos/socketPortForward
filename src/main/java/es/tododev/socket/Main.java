package es.tododev.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class Main {

    private static final String LISTEN_PARAM = "-listen";
    private static final String FORWARD_PARAM = "-forward";
    private static final String DEBUG_PARAM = "-debug";

    public static void main(String[] args) throws Exception {
        String listen = value(LISTEN_PARAM, args);
        String forward = value(FORWARD_PARAM, args);
        boolean debug = exists(DEBUG_PARAM, args);
        DebugLogger logger = new DebugLogger(debug);
        if (listen == null || forward == null) {
            logger.log(
                    "Incorrect parameters. It has to be executed in the next way:\n -listen <port to listen new connectons> -forward <host:port>");
        } else {
            String[] hostPort = forward.split(":");
            if (hostPort.length != 2) {
                logger.log(forward + " is invalid. It requires the next format <host:port>");
            } else {
                String host = hostPort[0];
                int port = Integer.parseInt(hostPort[1]);
                try (ServerSocket server = new ServerSocket(Integer.parseInt(listen))) {
                    while (true) {
                        logger.log("Listening connections in " + listen);
                        String listener = "";
                        CountDownLatch latch = new CountDownLatch(1);
                        try (Socket origin = server.accept();
                                Socket forwardSocket = new Socket(host, port);
                                MiddleCommunicator originToForward = new MiddleCommunicator(origin, forwardSocket,
                                        latch, logger, true);
                                MiddleCommunicator forwardToOrigin = new MiddleCommunicator(forwardSocket, origin,
                                        latch, logger, false);) {
                            listener = origin.getInetAddress().getHostAddress() + ":" + origin.getPort();
                            logger.log(listener + " Incoming connection stablished ");
                            originToForward.start();
                            forwardToOrigin.start();
                            latch.await();
                        } catch (IOException e) {
                            logger.log(listener + " Socket disconnected. Reason: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private static boolean exists(String param, String[] args) {
        for (String arg : args) {
            if (param.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static String value(String param, String[] args) {
        for (int i = 0; i < args.length; i++) {
            String value = args[i];
            if (param.equals(value) && (i + 1) < args.length) {
                return args[i + 1];
            }
        }
        return null;
    }

}
