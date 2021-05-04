package es.tododev.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class Main {

	private static final String LISTEN_PARAM = "-listen";
	private static final String FORWARD_PARAM = "-forward";

	public static void main(String[] args) throws Exception {
		String listen = value(LISTEN_PARAM, args);
		String forward = value(FORWARD_PARAM, args);
		if (listen == null || forward == null) {
			System.out.println(
					"Incorrect parameters. It has to be executed in the next way:\n -listen <port to listen new connectons> -forward <host:port>");
		} else {
			String[] hostPort = forward.split(":");
			if (hostPort.length != 2) {
				System.out.println(forward + " is invalid. It requires the next format <host:port>");
			} else {
				try (Socket forwardSocket = new Socket(hostPort[0], Integer.parseInt(hostPort[1]))) {
					while (true) {
						System.out.println("Listening connections in " + listen);
						try (ServerSocket server = new ServerSocket(Integer.parseInt(listen))) {
							String listener = "";
							CountDownLatch latch = new CountDownLatch(1);
							try (Socket origin = server.accept();
									MiddleCommunicator originToForward = new MiddleCommunicator(origin, forwardSocket,
											latch);
									MiddleCommunicator forwardToOrigin = new MiddleCommunicator(forwardSocket, origin,
											latch);) {
								listener = origin.getInetAddress().getHostAddress() + ":" + origin.getPort();
								System.out.println(listener + " Incoming connection stablished ");
								originToForward.start();
								forwardToOrigin.start();
								latch.await();
							} catch (IOException e) {
								System.out.println(listener + " Socket disconnected. Reason: " + e.getMessage());
							}
						}
					}
				}
			}
		}
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
