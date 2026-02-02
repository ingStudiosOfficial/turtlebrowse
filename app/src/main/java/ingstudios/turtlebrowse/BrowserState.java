package ingstudios.turtlebrowse;

public class BrowserState {
    private boolean browserFocus;

    public BrowserState(boolean initialState) {
        this.browserFocus = initialState;
    }

    public boolean getBrowserFocus() {
        return this.browserFocus;
    }

    public void setBrowserFocus(boolean focused) {
        this.browserFocus = focused;
    }
}
