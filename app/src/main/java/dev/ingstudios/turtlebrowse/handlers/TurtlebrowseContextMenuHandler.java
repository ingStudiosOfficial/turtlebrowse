package dev.ingstudios.turtlebrowse.handlers;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
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

import dev.ingstudios.turtlebrowse.windows.MainWindow;

public class TurtlebrowseContextMenuHandler extends CefContextMenuHandlerAdapter {
	private static final int ID_COPY_IMAGE = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 1;
	private static final int ID_SUMMARIZE = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 2;
	private static final int ID_REWRITE = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 3;
	private static final int ID_SUMMARIZE_PAGE = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 4;
	private static final int ID_DEVTOOLS = CefMenuModel.MenuId.MENU_ID_USER_FIRST + 5;
	private final MainWindow parent;
	private final CefStringVisitor stringVisitor = new CefStringVisitor() {
		@Override
		public void visit(String html) {
			TurtlebrowseContextMenuHandler.this.parent.aiSidebar.summarizePage(html);
		}
	};

	public TurtlebrowseContextMenuHandler(MainWindow parent) {
		this.parent = parent;
	}

	@Override
	public void onBeforeContextMenu(CefBrowser browser, CefFrame frame, CefContextMenuParams params,
			CefMenuModel model) {
		final String selectedText = params.getSelectionText();
		final boolean hasText = selectedText != null && !selectedText.isEmpty();
		final boolean isImage = params.getMediaType() == MediaType.CM_MEDIATYPE_IMAGE;

		if (isImage) {
			model.addItem(ID_COPY_IMAGE, "Copy image");
		}

		if (hasText) {
			model.addItem(ID_SUMMARIZE, "Summarize with AI");
		}

		if (hasText && params.isEditable()) {
			model.addItem(ID_REWRITE, "Rewrite with AI");
		}

		model.addItem(ID_SUMMARIZE_PAGE, "Summarize page with AI");
		model.addItem(ID_DEVTOOLS, "Open DevTools");
	}

	@Override
	public boolean onContextMenuCommand(CefBrowser browser, CefFrame frame, CefContextMenuParams params, int commandId,
			int eventFlags) {
		final String selectedText = params.getSelectionText();

		if (commandId == ID_COPY_IMAGE) {
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
		parent.aiSidebar.summarize(selection);
	}

	private void rewriteSelection(String selection) {
		parent.aiSidebar.rewrite(selection);
	}

	private void summarizePage(CefBrowser browser) {
		browser.getSource(stringVisitor);
	}

	private void copyImage(String src) {
		try {
			final URI url = new URI(src);
			final Image image = ImageIO.read(url.toURL());

			if (image != null) {
				final Transferable transferable = new ImageTransferable(image);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
			}
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
