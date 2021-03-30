package lg.sec.loginsecurity.listeners;

import lg.sec.loginsecurity.LoginSecurity;
import lg.sec.loginsecurity.listeners.events.LoginEvent;
import lg.sec.loginsecurity.listeners.events.RegisterEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AuthListener implements Listener {

    private LoginSecurity loginSecurity;

    public void init() {
        this.loginSecurity = LoginSecurity.getInstance();
        this.loginSecurity.getServer().getPluginManager().registerEvents(this, this.loginSecurity);
    }

    @EventHandler
    private void onLogin(LoginEvent event) {

    }

    @EventHandler
    private void onRegister(RegisterEvent event) {

    }


}
