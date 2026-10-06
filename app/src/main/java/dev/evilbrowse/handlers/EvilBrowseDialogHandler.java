package dev.evilbrowse.handlers;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefFileDialogCallback;
import org.cef.handler.CefDialogHandler;

import javafx.application.Platform;
import javafx.stage.FileChooser;

public class EvilBrowseDialogHandler implements CefDialogHandler {
    @Override
    public boolean onFileDialog(CefBrowser browser, FileDialogMode mode, String title, String defaultFilePath,
            Vector<String> acceptFilters, Vector<String> acceptExtensions, Vector<String> acceptDescriptions,
            CefFileDialogCallback callback) {
        Platform.runLater(() -> {
            try {
                final FileChooser chooser = new FileChooser();
                chooser.setTitle(title != null ? title : "Select File");

                // acceptDescriptions and acceptExtensions are parallel vectors in theory,
                // but CEF does not guarantee equal sizes. Guard to avoid IndexOutOfBounds.
                final int filterCount = Math.min(
                        acceptDescriptions != null ? acceptDescriptions.size() : 0,
                        acceptExtensions != null ? acceptExtensions.size() : 0);
                for (int i = 0; i < filterCount; i++) {
                    String desc = acceptDescriptions.get(i);
                    String rawExts = acceptExtensions.get(i);

                    if (desc == null || desc.trim().isBlank()) {
                        desc = "Supported Files";
                    }
                    if (rawExts == null || rawExts.isBlank()) {
                        continue;
                    }

                    String[] parts = rawExts.split(";");

                    for (int j = 0; j < parts.length; j++) {
                        String clean = parts[j].trim();
                        if (clean.isEmpty()) {
                            continue;
                        }
                        if (clean.startsWith(".")) {
                            parts[j] = "*" + clean;
                        } else if (!clean.startsWith("*")) {
                            parts[j] = "*." + clean;
                        }
                    }

                    try {
                        chooser.getExtensionFilters().add(
                                new FileChooser.ExtensionFilter(desc, parts));
                    } catch (Exception ignored) {
                    }
                }

                final Vector<String> paths = new Vector<>();

                if (mode == FileDialogMode.FILE_DIALOG_OPEN_MULTIPLE) {
                    final List<File> files = chooser.showOpenMultipleDialog(null);
                    if (files == null) {
                        callback.Cancel();
                        return;
                    }
                    for (File file : files) {
                        paths.add(file.getAbsolutePath());
                    }
                    callback.Continue(paths);
                } else if (mode == FileDialogMode.FILE_DIALOG_SAVE) {
                    final File file = chooser.showSaveDialog(null);
                    if (file == null) {
                        callback.Cancel();
                        return;
                    }
                    paths.add(file.getAbsolutePath());
                    callback.Continue(paths);
                } else {
                    final File file = chooser.showOpenDialog(null);
                    if (file == null) {
                        callback.Cancel();
                        return;
                    }
                    paths.add(file.getAbsolutePath());
                    callback.Continue(paths);
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    callback.Cancel();
                } catch (Exception ignored) {
                }
            }
        });

        return true;
    }
}
