package dev.ingstudios.turtlebrowse.managers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import dev.ingstudios.turtlebrowse.Main;
import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class InstanceManager {
	private static InstanceManager instance;
	private ServerSocket serverSocket;

	private InstanceManager() {
	}

	public static synchronized InstanceManager getInstance() {
		if (instance == null) {
			instance = new InstanceManager();
		}
		return instance;
	}

	public int getPort(String profileId) {
		final int hash = Math.abs(profileId.hashCode());
		return 49152 + (hash % (65535 - 49152));
	}

	public void startListening(String profileId, MainWindow parent) {
		try {
			serverSocket = new ServerSocket(getPort(profileId), 50,
					InetAddress.getByName("127.0.0.1"));

			Thread.ofVirtual().start(() -> {
				while (!serverSocket.isClosed()) {
					try (Socket clientSocket = serverSocket.accept()) {
						final BufferedReader in = new BufferedReader(
								new InputStreamReader(clientSocket.getInputStream()));

						String received = "";
						while ((received = in.readLine()) != null) {
							if (received.isBlank()) {
								continue;
							}

							parent.toFront();
							parent.requestFocusInWindow();

							final String[] args = received.split("\0");

							final String launchUrl = Main.getLaunchUrl(args);
							if (launchUrl != null && !launchUrl.isBlank()) {
								parent.createTab(launchUrl);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).setDaemon(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopListening() {
		try {
			serverSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isPortInUse(String host, int port) {
		try (Socket _ = new Socket(host, port)) {
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void broadcastToInstance(String host, int port, String payload) {
		try (Socket socket = new Socket(host, port)) {
			final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(payload);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
