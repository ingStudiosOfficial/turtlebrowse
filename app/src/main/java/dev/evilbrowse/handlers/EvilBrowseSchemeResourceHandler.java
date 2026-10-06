package dev.evilbrowse.handlers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

import org.cef.callback.CefCallback;
import org.cef.callback.CefResourceReadCallback;
import org.cef.handler.CefResourceHandlerAdapter;
import org.cef.misc.BoolRef;
import org.cef.misc.IntRef;
import org.cef.misc.StringRef;
import org.cef.network.CefPostData;
import org.cef.network.CefPostDataElement;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;

import dev.evilbrowse.managers.WallpaperManager;
import dev.evilbrowse.managers.WallpaperManager.WallpaperMetadata;
import dev.evilbrowse.windows.MainWindow;

public class EvilBrowseSchemeResourceHandler extends CefResourceHandlerAdapter {
	private byte[] data;
	private int offset = 0;
	private String mimeType = "text/html";
	private MainWindow parent;
	private int statusCode = 404;

	public EvilBrowseSchemeResourceHandler(MainWindow parent) {
		System.out.println("Setting scheme resource handler parent: " + parent);
		this.parent = parent;
	}

	private void loadResource(String resourcePath) {
		// Normalize and block path traversal: "/web" + user path must stay under /web or /dino.
		String safePath = resourcePath != null ? resourcePath.replace("\\", "/") : "";
		if (safePath.contains("..")) {
			this.data = "<html><body>400 Bad Request</body></html>".getBytes(StandardCharsets.UTF_8);
			this.mimeType = "text/html";
			this.statusCode = 400;
			return;
		}
		try (var inputStream = getClass().getResourceAsStream(safePath)) {
			if (inputStream != null) {
				this.data = inputStream.readAllBytes();

				if (safePath.endsWith(".js"))
					mimeType = "application/javascript";
				else if (safePath.endsWith(".css"))
					mimeType = "text/css";
				else if (safePath.endsWith(".svg"))
					mimeType = "image/svg+xml";
				else if (safePath.endsWith(".png"))
					mimeType = "image/png";
				else if (safePath.endsWith(".jpeg") || safePath.endsWith(".jpg"))
					mimeType = "image/jpeg";
				else
					mimeType = "text/html";

				this.statusCode = 200;
			} else {
				this.data = "<html><body><h1>404 Not Found</h1><p>EvilBrowse could not find this internal page.</p></body></html>"
						.getBytes(StandardCharsets.UTF_8);
				this.mimeType = "text/html";
				this.statusCode = 404;
			}
		} catch (IOException e) {
			e.printStackTrace();
			this.data = "<html><body>500 Internal Error</body></html>".getBytes(StandardCharsets.UTF_8);
			this.mimeType = "text/html";
			this.statusCode = 500;
		}
	}

	private static String normalizeScheme(String url) {
		if (url != null && url.startsWith("turtlebrowse://")) {
			// Legacy Turtlebrowse URLs (history/bookmarks) resolve to EvilBrowse pages.
			return "evilbrowse://" + url.substring("turtlebrowse://".length());
		}
		return url;
	}

	@Override
	public boolean open(CefRequest request, BoolRef handleRequest, CefCallback callback) {
		final String rawUrl = request.getURL();
		final String url = normalizeScheme(rawUrl);
		System.out.println("Scheme handler open() called with URL: " + rawUrl);

		if (url.startsWith("evilbrowse://newtab")) {
			final String path = url.substring("evilbrowse://newtab".length());
			System.out.println("Parsed path: '" + path + "'");

			if (path.isBlank() || path.equals("/")) {
				loadResource("/web/newtab.html");
			} else {
				loadResource("/web" + path);
			}

			System.out.println("Data loaded, length: " + (data != null ? data.length : "NULL"));
			System.out.println("MIME type: " + mimeType);

			handleRequest.set(true);
			callback.Continue();
			return true;
		} else if (url.startsWith("evilbrowse://chat")) {
			final String path = url.substring("evilbrowse://chat".length());
			System.out.println("Parsed path: '" + path + "'");

			if (path.isBlank() || path.equals("/")) {
				loadResource("/web/chat.html");
			} else {
				loadResource("/web" + path);
			}

			System.out.println("Data loaded, length: " + (data != null ? data.length : "NULL"));
			System.out.println("MIME type: " + mimeType);

			handleRequest.set(true);
			callback.Continue();
			return true;
		} else if (url.startsWith("evilbrowse://settings")) {
			final String path = url.substring("evilbrowse://settings".length());
			System.out.println("Parsed path: '" + path + "'");

			if (path.isBlank() || path.equals("/")) {
				loadResource("/web/settings.html");
			} else {
				loadResource("/web" + path);
			}

			System.out.println("Data loaded, length: " + (data != null ? data.length : "NULL"));
			System.out.println("MIME type: " + mimeType);

			handleRequest.set(true);
			callback.Continue();
			return true;
		} else if (url.startsWith("evilbrowse://history")) {
			final String path = url.substring("evilbrowse://history".length());
			System.out.println("Parsed path: '" + path + "'");

			if (path.isBlank() || path.equals("/")) {
				loadResource("/web/history.html");
			} else {
				loadResource("/web" + path);
			}

			System.out.println("Data loaded, length: " + (data != null ? data.length : "NULL"));
			System.out.println("MIME type: " + mimeType);

			handleRequest.set(true);
			callback.Continue();
			return true;
		} else if (url.startsWith("evilbrowse://dino")) {
			final String path = url.substring("evilbrowse://dino".length());
			System.out.println("Parsed path: '" + path + "'");

			if (path.isBlank() || path.equals("/")) {
				loadResource("/dino/dino.html");
			} else {
				loadResource("/dino" + path);
			}

			System.out.println("Data loaded, length: " + (data != null ? data.length : "NULL"));
			System.out.println("MIME type: " + mimeType);

			handleRequest.set(true);
			callback.Continue();
			return true;
		} else if (url.startsWith("evilbrowse://api")) {
			final String action = url.replace("evilbrowse://api/", "");

			System.out.printf("Action: %s\nURL: %s\n", action, url);

			if (action.contains("get-wallpaper")) {
				System.out.println("Getting wallpaper...");
				final WallpaperMetadata wallpaperMetadata = WallpaperManager.getInstance(parent).getWallpaper();

				if (wallpaperMetadata == null) {
					System.out.println("Wallpaper metadata is null.");
					this.data = new byte[0];
					this.mimeType = "application/json";
					this.statusCode = 404;
				} else {
					System.out.printf("Wallpaper metadata: %s\n", wallpaperMetadata.toString());
					this.data = wallpaperMetadata.imageBytes();
					this.mimeType = wallpaperMetadata.mimeType();
					this.statusCode = 200;
				}

				handleRequest.set(true);
				callback.Continue();
				return true;
			} else if (action.equals("set-wallpaper")) {
				System.out.println("Setting wallpaper...");
				WallpaperManager.getInstance(parent).setWallpaper(request);
				final String result = "\"ok\"";
				this.data = result.getBytes();
				this.mimeType = "application/json";
				this.statusCode = 200;
				handleRequest.set(true);
				callback.Continue();
				return true;
			}

			final CefPostData postData = request.getPostData();
			String body = "{}";

			if (postData != null) {
				final Vector<CefPostDataElement> elements = new Vector<>();
				postData.getElements(elements);
				if (elements != null && elements.size() > 0) {
					final CefPostDataElement element = elements.firstElement();
					final int elementSize = (int) element.getBytesCount();
					final byte[] buffer = new byte[elementSize];
					element.getBytes(elementSize, buffer);
					body = new String(buffer, StandardCharsets.UTF_8);
				}
			}

			final String result = parent.handleApiFromClient(action, body);
			this.data = result.getBytes(StandardCharsets.UTF_8);
			this.mimeType = "application/json";
			this.statusCode = 200;
			handleRequest.set(true);
			callback.Continue();
			return true;
		}

		return false;
	}

	@Override
	public void getResponseHeaders(CefResponse response, IntRef responseLength, StringRef redirectUrl) {
		if (data == null) {
			data = "<html><body>500 Internal Error</body></html>".getBytes(StandardCharsets.UTF_8);
			mimeType = "text/html";
			response.setStatus(500);
		} else {
			response.setStatus(this.statusCode);
		}
		response.setMimeType(mimeType);
		response.setHeaderByName("Access-Control-Allow-Origin", "*", true);
		response.setHeaderByName("Access-Control-Allow-Methods", "GET, OPTIONS", true);
		response.setHeaderByName("Access-Control-Allow-Headers", "*", true);
		responseLength.set(data.length);
	}

	@Override
	public boolean read(byte[] dataOut, int bytesToRead, IntRef bytesRead, CefResourceReadCallback callback) {
		if (offset >= data.length) {
			bytesRead.set(0);
			return false;
		}

		int available = data.length - offset;
		int toCopy = Math.min(available, bytesToRead);

		System.arraycopy(data, offset, dataOut, 0, toCopy);
		offset += toCopy;

		bytesRead.set(toCopy);
		return true;
	}

	@Override
	public void cancel() {
		offset = 0;
	}
}
