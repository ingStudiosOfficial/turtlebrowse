package dev.ingstudios.turtlebrowse.handlers;

import java.io.File;
import java.awt.print.PrinterJob;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import org.cef.browser.CefBrowser;
import org.cef.callback.CefPrintDialogCallback;
import org.cef.callback.CefPrintJobCallback;
import org.cef.handler.CefPrintHandlerAdapter;

public class TurtlebrowsePrintHandler extends CefPrintHandlerAdapter {
	@Override
	public void onPrintStart(CefBrowser browser) {
		System.out.println("Print starting.");
	}

	@Override
	public boolean onPrintDialog(
			CefBrowser browser, boolean hasSelection, CefPrintDialogCallback callback) {
		System.out.println("On print dialog called.");
		return false;
	}

	@Override
	public boolean onPrintJob(CefBrowser browser, String documentName, String pdfFilePath,
			CefPrintJobCallback callback) {
		System.out.println("Creating print job for: " + documentName);

		final File pdfFile = new File(pdfFilePath);

		Thread.ofVirtual().start(() -> {
			try (PDDocument document = Loader.loadPDF(pdfFile)) {
				final PrinterJob printerJob = PrinterJob.getPrinterJob();
				printerJob.setJobName(documentName);
				printerJob.setPageable(new PDFPageable(document));
				if (printerJob.printDialog()) {
					printerJob.print();
				} else {
					System.out.println("User cancelled printer job.");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		return true;
	}
}
