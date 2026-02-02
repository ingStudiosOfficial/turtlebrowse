package ingstudios.turtlebrowse;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.OS;
import org.cef.CefApp.CefAppState;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.cef.handler.CefFocusHandlerAdapter;

import me.friwi.jcefmaven.*;

public class MainWindow extends JFrame {
    final String START_URL = "https://google.com";
    final boolean USE_OSR = false;

    CefApp cefApp;
    CefClient cefClient;
    CefMessageRouter cefMessageRouter;
    CefBrowser cefBrowser;
    CefSettings cefSettings;
    Component browserUi;
    JPanel root;
    BrowserState browserFocused = new BrowserState(true);

    public MainWindow() {
        super("Turtlebrowse");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        final Image icon = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/logo_full_trans.png"));
        setIconImage(icon);

        root = new JPanel(new BorderLayout());
        setContentPane(root);

        setSize(1200, 800);
        setLocationRelativeTo(null);

        //root.add(new JLabel("Hello Turtlebrowse"), BorderLayout.CENTER);

        // CEF browser setup
        try {
            cefApp = creatCefApp();
        } catch (RuntimeException error) {
            System.exit(1);
        }

        cefClient = cefApp.createClient();
        cefMessageRouter = CefMessageRouter.create();
        cefClient.addMessageRouter(cefMessageRouter);

        cefBrowser = cefClient.createBrowser(START_URL, USE_OSR, false);
        browserUi = cefBrowser.getUIComponent();

        cefClient.addFocusHandler(new CefFocusHandlerAdapter() {
            @Override
            public void onGotFocus(CefBrowser browser) {
                if (browserFocused.getBrowserFocus()) return;
                browserFocused.setBrowserFocus(true);
                KeyboardFocusManager.getCurrentKeyboardFocusManager().clearFocusOwner();
                browser.setFocus(true);
            }

            @Override
            public void onTakeFocus(CefBrowser browser, boolean next) {
                browserFocused.setBrowserFocus(false);
            }
            
            @Override
            public boolean onSetFocus(CefBrowser browser, FocusSource source) {
                if (!browserFocused.getBrowserFocus()) {
                    return true;
                }
                return false;
            }
        });

        // Address bar
        final AddressBar addressBar = new AddressBar(cefClient, cefBrowser, START_URL, browserFocused);

        // Tab bar
        final TabBar tabBar = new TabBar();

        // Top panel (address + tab)
        final JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(tabBar, BorderLayout.NORTH);
        topPanel.add(addressBar, BorderLayout.SOUTH);

        root.add(topPanel, BorderLayout.NORTH);
        root.add(browserUi, BorderLayout.CENTER);

        cefClient.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onTitleChange(CefBrowser browser, String title) {
                SwingUtilities.invokeLater(() -> {
                    updateWindowTitle(title);
                });
            }

            @Override
            public void onAddressChange(CefBrowser cefBrowser, CefFrame frame, String url) {
                System.out.print("Navigated to:");
                System.out.println(url);
                addressBar.updateUrl(url);
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                CefApp.getInstance().dispose();
                dispose();
            }
        });
    }

    private CefApp creatCefApp() {
        CefAppBuilder builder = new CefAppBuilder();

        cefSettings = builder.getCefSettings();

        cefSettings.windowless_rendering_enabled = USE_OSR;

        try {
            String cachePath = getCachePath();
            cefSettings.cache_path = cachePath;
        } catch (Exception error) {
            System.out.print("Error while getting cache path, defaulting: ");
            System.out.println(error);
        }

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

    private String getCachePath() {
        Path cachePath;

        String appName = "Turtlebrowse";
        String cacheDir = "cef-cache";

        String userHome = System.getProperty("user.home");

        if (OS.isWindows()) {
            String localAppData = System.getenv("LOCALAPPDATA");
            cachePath = Paths.get(localAppData, "ingStudios", appName, cacheDir);
        } else if (OS.isLinux()) {
            String xdgDataHome = System.getenv("XDG_DATA_HOME");
            cachePath = Paths.get(xdgDataHome, "ingStudios", appName, cacheDir);
        } else if (OS.isMacintosh()) {
            cachePath = Paths.get(userHome, "Library", "Application Support", appName, cacheDir);
        } else {
            throw new RuntimeException("Unknown operating system");
        }

        return cachePath.toString();
    }

    public void updateWindowTitle(String pageTitle) {
        this.setTitle(pageTitle + " - Turtlebrowse");
    }

    /*
    private void setIconImage() {
        // Set icon image in the future
    }
    */

    @Override
    public void dispose() {
        System.out.println("Closing...");

        if (cefBrowser != null) {
            cefBrowser.close(true);
        }

        if (cefApp != null) {
            cefApp.dispose();
        }

        super.dispose();

        System.out.println("Successfully closed browser.");
    }
}
