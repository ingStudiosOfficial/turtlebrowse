package dev.ingstudios.turtlebrowse.components;

import java.awt.BorderLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2OutlinedAL;

import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import com.jfoenix.controls.JFXButton;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;

import dev.ingstudios.turtlebrowse.windows.MainWindow;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;

public class TerminalSidebar extends ToolSidebar {
	private final java.awt.Dimension preferredDim = new java.awt.Dimension(0, 800);
	public boolean isOpen = false;
	private final MainWindow parent;
	private final JediTermWidget terminal;
	private boolean focused;

	public TerminalSidebar(MainWindow parent) {
		this.parent = parent;

		this.setLayout(new java.awt.BorderLayout());
		this.setPreferredSize(preferredDim);

		final JFXPanel actionsBarJfxPanel = new JFXPanel();
		actionsBarJfxPanel.setFocusable(true);
		actionsBarJfxPanel.setPreferredSize(new java.awt.Dimension(preferredDim.width, 50));

		Platform.runLater(() -> {
			final HBox actionsBar = new HBox();
			actionsBar.setStyle("-fx-spacing: 10px; -fx-padding: 10px;");
			actionsBar.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
				final Paint backgroundColor = parent.profileMaterialColorScheme.getSurface().get();
				return new Background(new BackgroundFill(backgroundColor, null, null));
			}, parent.profileMaterialColorScheme.getSurface()));
			actionsBar.setAlignment(Pos.CENTER_RIGHT);

			final Scene actionsBarScene = new Scene(actionsBar);
			actionsBarScene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
			actionsBarJfxPanel.setScene(actionsBarScene);
			actionsBar.prefWidthProperty().bind(actionsBarScene.widthProperty());
			actionsBar.prefHeightProperty().bind(actionsBarScene.heightProperty());

			final JFXButton closeButton = buildButton(Material2OutlinedAL.CLOSE);
			closeButton.setOnAction(event -> {
				closeSidebar();
			});

			actionsBar.getChildren().addAll(closeButton);
		});

		terminal = new JediTermWidget(new DefaultSettingsProvider());
		terminal.getTerminalPanel().addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				System.out.println("Terminal focus gained.");
				focused = true;
			}

			@Override
			public void focusLost(FocusEvent e) {
				System.out.println("Terminal focus lost.");
				focused = false;
			}
		});

		try {
			final PtyProcessTtyConnecter connecter = new PtyProcessTtyConnecter();
			terminal.setTtyConnector(connecter);
			terminal.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.add(actionsBarJfxPanel, BorderLayout.NORTH);
		this.add(terminal, BorderLayout.CENTER);
	}

	private JFXButton buildButton(Ikon iconName) {
		final JFXButton button = new JFXButton("");
		final FontIcon icon = new FontIcon(iconName);
		icon.setIconColor(parent.profileMaterialColorScheme.getOnSurface().get());
		parent.profileMaterialColorScheme.getOnSurface().addListener((observable, oldPaint, newPaint) -> {
			icon.setIconColor(newPaint);
		});
		button.setGraphic(icon);
		button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		button.setStyle("-fx-padding: 10px;");
		button.backgroundProperty().bind(Bindings.createObjectBinding(() -> {
			final Paint backgroundColor = parent.profileMaterialColorScheme.getSurfaceContainer().get();
			return new Background(new BackgroundFill(backgroundColor, new CornerRadii(25), null));
		}, parent.profileMaterialColorScheme.getSurfaceContainer()));
		button.setOnMouseEntered(event -> {
			button.setCursor(Cursor.HAND);
		});
		button.setOnMouseExited(event -> {
			button.setCursor(Cursor.DEFAULT);
		});
		return button;
	}

	@Override
	public void toggleSidebar() {
		if (isOpen) {
			closeSidebar();
		} else {
			openSidebar();
		}
	}

	@Override
	public void openSidebar() {
		preferredDim.width = 800;
		isOpen = true;
		this.revalidate();
		parent.revalidate();
		parent.repaint();
	}

	@Override
	public void closeSidebar() {
		preferredDim.width = 0;
		isOpen = false;
		this.revalidate();
		parent.revalidate();
		parent.repaint();
	}

	public void shutdown() {
		terminal.close();
	}

	public boolean isFocused() {
		return focused;
	}
}

class PtyProcessTtyConnecter implements TtyConnector {
	private final PtyProcess process;
	private final InputStreamReader reader;
	private final OutputStreamWriter writer;
	private final String sessionName;

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
		env.put("TERM_PROGRAM", "turtlebrowse");
		env.put("TERM_PROGRAM_VERSION", version);
		env.put("LC_TERMINAL", "turtlebrowse");
		env.put("LC_TERMINAL_VERSION", version);

		process = new PtyProcessBuilder().setCommand(command).setEnvironment(env).setConsole(false)
				.setDirectory(userHome).start();

		reader = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8);
		writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8);
	}

	@Override
	public int read(char[] buf, int offset, int length) throws IOException {
		return reader.read(buf, offset, length);
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		process.getOutputStream().write(bytes);
		process.getOutputStream().flush();
	}

	@Override
	public void write(String string) throws IOException {
		writer.write(string);
		writer.flush();
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
