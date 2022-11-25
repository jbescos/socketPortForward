package es.tododev.socket;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final String LISTEN_PARAM = "-listen";
    private static final String FORWARD_PARAM = "-forward";
    private static final String DEBUG_PARAM = "-debug";
    private static final int TIMEOUT = 5000;

    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newCachedThreadPool();
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
                        Socket origin = server.accept();
                        origin.setSoTimeout(TIMEOUT);
                        Socket forwardSocket = new Socket(host, port);
                        forwardSocket.setSoTimeout(TIMEOUT);
                        MiddleCommunicator originToForward = new MiddleCommunicator(executor, origin, forwardSocket, logger,
                                true);
                        MiddleCommunicator forwardToOrigin = new MiddleCommunicator(executor, forwardSocket, origin, logger,
                                false);
                        listener = origin.getInetAddress().getHostAddress() + ":" + origin.getPort();
                        logger.log(listener + " Incoming connection stablished ");
                        originToForward.start();
                        forwardToOrigin.start();
                    }
                }
            }
        }
        executor.shutdown();
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
