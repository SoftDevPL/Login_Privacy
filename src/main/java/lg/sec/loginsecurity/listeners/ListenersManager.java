package lg.sec.loginsecurity.listeners;

public class ListenersManager {

    private final AuthListener authListener;

    public ListenersManager() {
        authListener = new AuthListener();
    }

    public void init() {
        this.authListener.init();
    }


}
