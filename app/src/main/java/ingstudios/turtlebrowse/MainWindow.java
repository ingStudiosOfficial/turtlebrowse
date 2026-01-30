package ingstudios.turtlebrowse;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefApp.CefAppState;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefMessageRouter;

import me.friwi.jcefmaven.*;

public class MainWindow extends JFrame {
    final String START_URL = "https://google.com";
    final Boolean USE_OSR = false;

    CefApp cefApp;
    CefClient cefClient;
    CefMessageRouter cefMessageRouter;
    CefBrowser cefBrowser;
    Component browserUi;

    public MainWindow() {
        super("Turtlebrowse");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout());
        setContentPane(root);

        setSize(1200, 800);
        setLocationRelativeTo(null);

        //root.add(new JLabel("Hello Turtlebrowse"), BorderLayout.CENTER);

        try {
            cefApp = creatCefApp();
        } catch (RuntimeException error) {
            System.exit(1);
        }

        cefClient = cefApp.createClient();
        cefMessageRouter = CefMessageRouter.create();
        cefClient.addMessageRouter(cefMessageRouter);

        cefBrowser = cefClient.createBrowser(START_URL, USE_OSR, false);
        browserUi
    }

    private CefApp creatCefApp() {
        CefAppBuilder builder = new CefAppBuilder();

        builder.getCefSettings().windowless_rendering_enabled = USE_OSR;

        builder.setAppHandler(new MavenCefAppHandlerAdapter() {
            @Override
            public void stateHasChanged(CefAppState state) {
                if (state == CefAppState.TERMINATED) System.exit(0);
            }
        });

        try {
            CefApp cefApp = builder.build();
            return cefApp;
        } catch (Exception error) {
            System.out.print("Error while building CEF app:");
            System.out.println(error);
            throw new RuntimeException("Error while building CEF app:", error);
        }
    }

    /*
    private void setIconImage() {
        // Set icon image in the future
    }
    */
}
