package dev.evilbrowse.handlers;

import java.io.File;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefBeforeDownloadCallback;
import org.cef.callback.CefDownloadItem;
import org.cef.handler.CefDownloadHandlerAdapter;

import javafx.application.Platform;
import javafx.stage.FileChooser;

public class EvilBrowseDownloadHandler extends CefDownloadHandlerAdapter {
    @Override
    public boolean onBeforeDownload(CefBrowser browser, CefDownloadItem downloadItem, String suggestedName, CefBeforeDownloadCallback callback) {
        Platform.runLater(() -> {
            try {
                final FileChooser chooser = new FileChooser();
                chooser.setTitle("Save File As...");
                chooser.setInitialFileName(suggestedName != null ? suggestedName : "download");

                final File selectedFile = chooser.showSaveDialog(null);

                if (selectedFile != null) {
                    callback.Continue(selectedFile.getAbsolutePath(), false);
                } else {
                    // CefBeforeDownloadCallback has no Cancel method: passing an
                    // empty path settles the callback so CEF does not wait forever.
                    // Previously canceling the dialog left the download hanging.
                    try {
                        callback.Continue("", false);
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    callback.Continue("", false);
                } catch (Exception ignored) {
                }
            }
        });

        return true;
    }
}
