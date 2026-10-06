package dev.evilbrowse.handlers;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;

import org.cef.callback.CefCallback;
import org.cef.callback.CefResourceReadCallback;
import org.cef.handler.CefResourceHandlerAdapter;
import org.cef.misc.BoolRef;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;

import com.google.gson.Gson;

import dev.evilbrowse.windows.MainWindow;

public class StreamingSchemeResourceHandler extends CefResourceHandlerAdapter {
	private PipedInputStream pipedIn;
	private PipedOutputStream pipedOut;
	private final Gson gson = new Gson();

	public StreamingSchemeResourceHandler(String prompt, MainWindow parent) {
		System.out.println("New StreamingSchemeResourceHandler created.");

		try {
			pipedIn = new PipedInputStream(65536);
			pipedOut = new PipedOutputStream(pipedIn);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		final String safePrompt = prompt != null ? prompt : "";
		Thread.ofVirtual().start(() -> {
			if (parent == null || !parent.isAIEnabled()) {
				writeSSE("error",
						"AI is disabled. Enable it in Settings -> AI integrations (requires local Ollama).");
				closeStream();
				return;
			}
			parent.ensureOllamaSessionAsync(chat -> {
				if (chat == null) {
					writeSSE("error", "AI session could not be started. Is Ollama running?");
					closeStream();
					return;
				}
				try {
					chat.prompt(safePrompt,
							chunk -> writeSSE("think", chunk),
							chunk -> writeSSE("response", chunk),
							result -> {
								writeSSE("done", result);
								closeStream();
							},
							error -> {
								writeSSE("error", error);
								closeStream();
							});
				} catch (Exception e) {
					writeSSE("error", "AI request failed: " + e.getMessage());
					closeStream();
				}
			}, err -> {
				writeSSE("error", err != null ? err : "AI unavailable.");
				closeStream();
			});
		});
	}

	private void writeSSE(String type, String data) {
		try {
			String json = "{\"type\":\"" + type + "\",\"data\":" + gson.toJson(data) + "}";
			pipedOut.write(("data: " + json + "\n\n").getBytes(StandardCharsets.UTF_8));
			pipedOut.flush();
		} catch (IOException ignored) {
		}
	}

	private void closeStream() {
		try {
			pipedOut.close();
		} catch (IOException ignored) {
		}
	}

	@Override
	public boolean open(CefRequest request, BoolRef handleRequest, CefCallback callback) {
		handleRequest.set(true);
		callback.Continue();
		return true;
	}

	@Override
	public void getResponseHeaders(CefResponse response, IntRef responseLength, StringRef redirectUrl) {
		response.setStatus(200);
		response.setMimeType("text/event-stream");
		response.setHeaderByName("Cache-Control", "no-cache", true);
		response.setHeaderByName("Access-Control-Allow-Origin", "*", true);
		responseLength.set(-1);
	}

	@Override
	public boolean read(byte[] dataOut, int bytesToRead, IntRef bytesRead, CefResourceReadCallback callback) {
		try {
			final int available = pipedIn.available();
			if (available > 0) {
				int n = pipedIn.read(dataOut, 0, Math.min(available, bytesToRead));
				bytesRead.set(n > 0 ? n : 0);
				return n > 0;
			}

			Thread.ofVirtual().start(() -> {
				try {
					final int b = pipedIn.read();
					if (b == -1) {
						bytesRead.set(0);
						callback.Continue(0);
						return;
					}
					dataOut[0] = (byte) b;
					int extra = Math.min(pipedIn.available(), bytesToRead - 1);
					int n = extra > 0 ? pipedIn.read(dataOut, 1, extra) : 0;
					bytesRead.set(1 + Math.max(n, 0));
					callback.Continue(1 + Math.max(n, 0));
				} catch (IOException e) {
					bytesRead.set(0);
					callback.Continue(0);
				}
			});

			return true;
		} catch (IOException e) {
			bytesRead.set(0);
			return false;
		}
	}
}
