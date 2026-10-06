package dev.evilbrowse.terminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.jediterm.terminal.TtyConnector;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;

public class PtyProcessTtyConnecter implements TtyConnector {
	private final PtyProcess process;
	private final InputStreamReader reader;
	private final String sessionName;
	private final InputStream inputStream;
	private final OutputStream outputStream;

	public PtyProcessTtyConnecter() throws IOException {
		String[] command;

		final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

		if (isWindows) {
			command = new String[] { "cmd.exe" };
			sessionName = "Command Prompt";
		} else {
			final String shell = getDefaultShell();
			System.out.println("Shell: " + shell);
			command = new String[] { shell != null ? shell : "/bin/bash", "-l", "-i" };
			sessionName = shell != null ? shell : "/bin/bash";
		}

		final String version = System.getProperties().getProperty("version", "1.0.0");
		final String userHome = System.getProperty("user.home");

		final Map<String, String> env = new HashMap<>(System.getenv());
		env.put("TERM", "xterm-256color");
		env.put("TERM_PROGRAM", "evilbrowse");
		env.put("TERM_PROGRAM_VERSION", version);
		env.put("LC_TERMINAL", "evilbrowse");
		env.put("LC_TERMINAL_VERSION", version);

		process = new PtyProcessBuilder().setCommand(command).setEnvironment(env).setConsole(false)
				.setDirectory(userHome).setUseWinConPty(true).start();

		reader = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8);
		inputStream = process.getInputStream();
		outputStream = process.getOutputStream();
	}

	@Override
	public int read(char[] buf, int offset, int length) throws IOException {
		return reader.read(buf, offset, length);
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		outputStream.write(bytes);
		outputStream.flush();
	}

	@Override
	public void write(String string) throws IOException {
		write(string.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public boolean isConnected() {
		return process.isAlive();
	}

	@Override
	public int waitFor() throws InterruptedException {
		process.waitFor();
		return 0;
	}

	@Override
	public boolean ready() throws IOException {
		return reader.ready();
	}

	@Override
	public String getName() {
		return sessionName;
	}

	@Override
	public void close() {
		process.destroy();
		try {
			inputStream.close();
		} catch (Exception ignored) {
		}
		try {
			outputStream.close();
		} catch (Exception ignored) {
		}
	}

	private String getDefaultShell() {
		boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
		if (isWindows) {
			return "cmd.exe";
		}

		String shell = System.getenv("SHELL");
		if (shell != null && !shell.isBlank()) {
			return shell;
		}

		try {
			String username = System.getProperty("user.name");
			Process process = new ProcessBuilder("getent", "passwd", username).start();
			String output = new String(process.getInputStream().readAllBytes()).trim();
			if (!output.isEmpty()) {
				String[] parts = output.split(":");
				if (parts.length >= 7 && !parts[6].isBlank()) {
					return parts[6];
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "/bin/bash";
	}
}
