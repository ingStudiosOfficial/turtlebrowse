package dev.ingstudios.turtlebrowse.handlers;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.cef.browser.CefBrowser;
import org.cef.callback.CefFileDialogCallback;
import org.cef.handler.CefDialogHandler;

import javafx.application.Platform;
import javafx.stage.FileChooser;

public class TurtlebrowseDialogHandler implements CefDialogHandler {
    @Override
    public boolean onFileDialog(CefBrowser browser, FileDialogMode mode, String title, String defaultFilePath,
            Vector<String> acceptFilters, Vector<String> acceptExtensions, Vector<String> acceptDescriptions,
            CefFileDialogCallback callback) {
        Platform.runLater(() -> {
            final FileChooser chooser = new FileChooser();
            chooser.setTitle(title != null ? title : "Select File");

            for (int i = 0; i < acceptDescriptions.size(); i++) {
                String desc = acceptDescriptions.get(i);
                String rawExts = acceptExtensions.get(i);

                if (desc == null || desc.trim().isBlank()) {
                    desc = "Supported Files";
                }

                String[] parts = rawExts.split(";");

                for (int j = 0; j < parts.length; j++) {
                    String clean = parts[j].trim();
                    if (clean.startsWith(".")) {
                        parts[j] = "*" + clean;
                    } else {
                        parts[j] = "*." + clean;
                    }
                }

                chooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter(desc, parts));
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
            } else {
                final File file = chooser.showOpenDialog(null);
                if (file == null) {
                    callback.Cancel();
                    return;
                }
                paths.add(file.getAbsolutePath());
            }

            callback.Continue(paths);
        });

        return true;
    }
}
