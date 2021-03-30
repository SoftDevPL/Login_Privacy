package lg.sec.loginprivacy.listeners;

import lg.sec.loginprivacy.LoginPrivacy;
import lg.sec.loginprivacy.listeners.events.LoginEvent;
import lg.sec.loginprivacy.listeners.events.RegisterEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AuthListener implements Listener {

    private LoginPrivacy loginPrivacy;

    public void init() {
        this.loginPrivacy = LoginPrivacy.getInstance();
        this.loginPrivacy.getServer().getPluginManager().registerEvents(this, this.loginPrivacy);
    }

    @EventHandler
    private void onLogin(LoginEvent event) {

    }

    @EventHandler
    private void onRegister(RegisterEvent event) {

    }


}
