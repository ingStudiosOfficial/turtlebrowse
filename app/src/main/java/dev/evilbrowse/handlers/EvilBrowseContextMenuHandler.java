package dev.evilbrowse.handlers;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URI;

import javax.imageio.ImageIO;

import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.callback.CefStringVisitor;
import org.cef.callback.CefContextMenuParams.MediaType;
import org.cef.handler.CefContextMenuHandlerAdapter;

import dev.evilbrowse.windows.MainWindow;

public class EvilBrowseContextMenuHandler extends CefContextMenuHandlerAdapter {
	private static final int ID_OPEN_URL = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 1;
	private static final int ID_COPY_URL = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 2;
	private static final int ID_COPY_IMAGE = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 3;
	private static final int ID_SUMMARIZE = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 4;
	private static final int ID_REWRITE = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 5;
	private static final int ID_SUMMARIZE_PAGE = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 6;
	private static final int ID_DEVTOOLS = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 7;
	private final MainWindow parent;
	private final CefStringVisitor stringVisitor = new CefStringVisitor() {
		@Override
		public void visit(String html) {
			try {
				if (parent.isAIEnabled() && parent.ensureAISidebar() != null) {
					parent.ensureAISidebar().summarizePage(html);
				}
			} catch (Exception ignored) {
			}
		}
	};

	public EvilBrowseContextMenuHandler(MainWindow parent) {
		this.parent = parent;
	}

	@Override
	public void onBeforeContextMenu(CefBrowser browser, CefFrame frame, CefContextMenuParams params,
			CefMenuModel model) {
		final String selectedText = params.getSelectionText();
		final boolean hasText = selectedText != null && !selectedText.isBlank();
		final boolean isImage = params.getMediaType() == MediaType.CM_MEDIATYPE_IMAGE;
		final boolean isLink = !params.getLinkUrl().isBlank();

		if (isLink) {
			model.addItem(ID_OPEN_URL, "Open link in new tab");
			model.addItem(ID_COPY_URL, "Copy link address");
		}

		if (isImage) {
			model.addItem(ID_COPY_IMAGE, "Copy image");
		}

		// AI actions are opt-in: hide them when AI is disabled so the menu
		// never offers dead actions. Startup stays AI-free.
		final boolean aiEnabled;
		try {
			aiEnabled = parent.isAIEnabled();
		} catch (Exception e) {
			return;
		}
		if (aiEnabled) {
			if (hasText) {
				model.addItem(ID_SUMMARIZE, "Summarize with AI");
			}

			if (hasText && params.isEditable()) {
				model.addItem(ID_REWRITE, "Rewrite with AI");
			}

			model.addItem(ID_SUMMARIZE_PAGE, "Summarize page with AI");
		}
		model.addItem(ID_DEVTOOLS, "Open DevTools");
	}

	@Override
	public boolean onContextMenuCommand(CefBrowser browser, CefFrame frame, CefContextMenuParams params, int commandId,
			int eventFlags) {
		final String selectedText = params.getSelectionText();

		if (commandId == ID_OPEN_URL) {
			final String url = params.getLinkUrl();
			parent.createTab(url);
		} else if (commandId == ID_COPY_URL) {
			final String url = params.getLinkUrl();
			copyUrl(url);
		} else if (commandId == ID_COPY_IMAGE) {
			final String imageUrl = params.getSourceUrl();
			Thread.ofVirtual().start(() -> {
				copyImage(imageUrl);
			});
		} else if (commandId == ID_SUMMARIZE) {
			System.out.printf("Selected: %s\n", selectedText);
			summarizeSelection(selectedText);
			return true;
		} else if (commandId == ID_REWRITE) {
			rewriteSelection(selectedText);
		} else if (commandId == ID_SUMMARIZE_PAGE) {
			summarizePage(browser);
		} else if (commandId == ID_DEVTOOLS) {
			browser.openDevTools();
		}

		return false;
	}

	private void summarizeSelection(String selection) {
		try {
			if (!parent.isAIEnabled()) {
				parent.createTab("evilbrowse://settings");
				return;
			}
			dev.evilbrowse.components.AISidebar sidebar = parent.ensureAISidebar();
			if (sidebar != null) {
				sidebar.summarize(selection);
			}
		} catch (Exception ignored) {
		}
	}

	private void rewriteSelection(String selection) {
		try {
			if (!parent.isAIEnabled()) {
				parent.createTab("evilbrowse://settings");
				return;
			}
			dev.evilbrowse.components.AISidebar sidebar = parent.ensureAISidebar();
			if (sidebar != null) {
				sidebar.rewrite(selection);
			}
		} catch (Exception ignored) {
		}
	}

	private void summarizePage(CefBrowser browser) {
		browser.getSource(stringVisitor);
	}

	private void copyImage(String src) {
		try {
			final URI url = URI.create(src);
			final Image image = ImageIO.read(url.toURL());

			if (image != null) {
				final Transferable transferable = new ImageTransferable(image);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void copyUrl(String address) {
		try {
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(address), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class ImageTransferable implements Transferable {
	private final Image image;

	public ImageTransferable(Image image) {
		this.image = image;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DataFlavor.imageFlavor };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return DataFlavor.imageFlavor.equals(flavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (!DataFlavor.imageFlavor.equals(flavor))
			throw new UnsupportedFlavorException(flavor);
		return image;
	}
}
